package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IClientQoSListener;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IPublisherListener;
import br.pucrio.inf.lac.mhubcddl.cddl.message.CancelQueryMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.EventQueryMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.EventQueryResponseMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.LivelinessMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.message.MessageGroup;
import br.pucrio.inf.lac.mhubcddl.cddl.message.QueryMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.QueryResponseMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.SensorDataMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ServiceInformationMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.ontology.QueryDestiny;
import br.pucrio.inf.lac.mhubcddl.cddl.ontology.QueryType;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.History;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.LatencyBudgetQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.LifespanQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.LivelinessQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Provider;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Topic;


public class PublisherImpl extends ClientImpl implements Publisher {

    private AssertLivelinessTimerTask assertLivelinessTimerTask = null;

    private ConcurrentHashMap<String, MessageGroup> messageGroupMap = new ConcurrentHashMap<String, MessageGroup>();

    private static final String TAG = PublisherImpl.class.getSimpleName();

    private IPublisherListener publisherListener;

    protected boolean isDeadlineMissed(long deadline) {
        Message lastDeadlineMessage = getLastDeadlineMessage();
        if (lastDeadlineMessage == null || lastDeadlineMessage.getPublicationTimestamp() > deadline) {
            return true;
        } else {
            setLastDeadlineMessage(null);
            return false;
        }
    }


    @Override
    protected boolean isLivelinessMissed(long leaseDuration) {
        Message lastLivelinessMessage = getLastLivelinessMessage();
        Message lastMessage = getLastMessage();
        if ((lastLivelinessMessage != null && lastLivelinessMessage.getPublicationTimestamp()< leaseDuration) || (lastMessage!=null && lastMessage.getPublicationTimestamp() < leaseDuration)) {
            setLastLivelinessMessage(null);
            setLastMessage(null);
            return false;

        } else {
            return true;
        }
    }

    @Override
    public void setLivelinessQoS(LivelinessQoS livelinessQoS) {

        LivelinessQoS currentLivelinessQoS = getLivelinessQoS();

        int nextLeaseDuration = 1 + (int) (livelinessQoS.getLeaseDuration() * Math.random());

        if (currentLivelinessQoS.getKind() == LivelinessQoS.MANUAL && livelinessQoS.getKind() == LivelinessQoS.AUTOMATIC && livelinessQoS.getLeaseDuration() > LivelinessQoS.DEFAULT_LEASE_DURANTION) {
            assertLivelinessTimerTask = new AssertLivelinessTimerTask();
            timer.scheduleAtFixedRate(assertLivelinessTimerTask, nextLeaseDuration, nextLeaseDuration);
        } else if (currentLivelinessQoS.getKind() == LivelinessQoS.AUTOMATIC && livelinessQoS.getKind() == LivelinessQoS.MANUAL) {
            assertLivelinessTimerTask.cancel();
        } else if (currentLivelinessQoS.getKind() == LivelinessQoS.AUTOMATIC && livelinessQoS.getKind() == LivelinessQoS.AUTOMATIC && currentLivelinessQoS.getLeaseDuration() != livelinessQoS.getLeaseDuration() && assertLivelinessTimerTask!=null) {
            assertLivelinessTimerTask.cancel();
            if (livelinessQoS.getLeaseDuration() > LivelinessQoS.DEFAULT_LEASE_DURANTION) {
                timer.scheduleAtFixedRate(assertLivelinessTimerTask, nextLeaseDuration, nextLeaseDuration);
            }
        }

        super.setLivelinessQoS(livelinessQoS);
    }


    public void assertLiveliness() {
      //  count++;
      //  if(count<=100) {
        LivelinessMessage livelinessMessage = new LivelinessMessage();
        livelinessMessage.setMeasurementTime(time.getCurrentTimestamp());
        setLastLivelinessMessage(livelinessMessage);
        publish(livelinessMessage);
      //  }
    }

    @Override
    public void setPublisherListener(IPublisherListener publisherListener) {
        this.publisherListener = publisherListener;
    }

    @Override
    public void setPublisherQoCListener(IClientQoSListener publisherQoCListener) {
        super.clientQoSListener = publisherQoCListener;
    }

    private class AssertLivelinessTimerTask extends TimerTask {

        public AssertLivelinessTimerTask() {
        }

        @Override
        public void run() {
            assertLiveliness();
        }

    }

    public void checkLifespanAndPublish(Message message) {
        LifespanQoS lifespanQoS = getLifespanQoS();
        History history = getHistory();
        message.setPublicationTimestamp(time.getCurrentTimestamp());
        long lifeTime = getLifeTime(message, true);
        if (lifespanQoS.getExpirationTime() == LifespanQoS.INFINITE_DURATION || lifeTime > 0) {
            message.setExpirationTime(lifeTime);
            history.insert(message);
            startLisfespanClock(message, lifeTime);
            setLastDeadlineMessage(message);
            setLastMessage(message);
            publish(message, message.getTopic());
        }
    }

    public void checkLifespanAndPublish(MessageGroup message) {
        LifespanQoS lifespanQoS = getLifespanQoS();
        History history = getHistory();
        message.setPublicationTimestamp(time.getCurrentTimestamp());
        for (Message submessage : message.getAll()) {
            long lifeTime = getLifeTime(message, true);
            if (lifespanQoS.getExpirationTime() == LifespanQoS.INFINITE_DURATION || lifeTime > 0) {
                submessage.setExpirationTime(lifeTime);
                history.insert(message);
                setLastDeadlineMessage(message);
                setLastMessage(message);
                startLisfespanClock(submessage, lifeTime);
            } else {
                message.remove(submessage);
            }
        }
        if (!message.isEmpty()) {
            publish(message, message.getTopic());
        }
    }


    @Override
    protected void on_message_received(Message message) {
        String topic = message.getTopic();
        message.setReceptionTimestamp(time.getCurrentTimestamp());
        LatencyBudgetQoS latencyBudgetQoS = getLatencyBudgetQoS();
        if (latencyBudgetQoS.getDelay() == LatencyBudgetQoS.DEFAULT_DELAY) {
            checkLifespanAndPublish(message);
        } else {
            if (!messageGroupMap.containsKey(topic)) {
                messageGroupMap.put(topic, new MessageGroup());
            }
            MessageGroup messageGroup = messageGroupMap.get(topic);
            messageGroup.setTopic(topic);
            messageGroup.add(message);
        }
    }

    @Override
    protected void on_latency_budget_timer_finish() {
        Collection<MessageGroup> messages = messageGroupMap.values();
        for (MessageGroup messageGroup : messages) {
            checkLifespanAndPublish(messageGroup);
            messages.remove(messageGroup);
        }
    }

    @Override
    public long query(QueryDestiny queryDestiny, QueryType queryType, String query) {

        long returnCode = System.currentTimeMillis();

        QueryMessage queryMessage = new QueryMessage(clientId, query, queryType, returnCode);

        if (queryDestiny == QueryDestiny.LOCAL) publishLocal(queryMessage);
        if (queryDestiny == QueryDestiny.GLOBAL) publishGlobal(queryMessage);
        if (queryDestiny == QueryDestiny.LOCAL_AND_GLOBAL) {
            publishLocal(queryMessage);
            publishGlobal(queryMessage);
        }

        return  returnCode;

    }

    @Override
    public void cancelQuery(long returnCode) {
        CancelQueryMessage cancelQueryMessage = new CancelQueryMessage(returnCode);
        EventBus.getDefault().post(cancelQueryMessage);
    }

    @Override
    public void publish(SensorDataMessage sensorDataMessage) {
        sensorDataMessage.setPublisherID(clientId);
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.SENSOR_DATA_TOPIC + "/" + sensorDataMessage.getServiceName();
        sensorDataMessage.setTopic(topic);
        addToQueue(sensorDataMessage);
    }

    @Override
    public void setCDDLFilter(String eplFilter) {
        CDDLFilter cddlFilter = Provider.newCDDLFilter(eplFilter);
        EventBus.getDefault().post(cddlFilter);
    }

    @Override
    public void clearCDDLFilter() {
        CDDLFilter cddlFilter = Provider.newCDDLFilter("");
        EventBus.getDefault().post(cddlFilter);
    }

    public void publish(LivelinessMessage livelinessMessage) {

        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/"
                + Topic.LIVELINESS_TOPIC;
        livelinessMessage.setTopic(topic);
        publish(livelinessMessage, topic);

    }



    public void publishGlobal(QueryMessage queryMessage) {

        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.GLOBAL_QUERY_TOPIC;
        queryMessage.setTopic(topic);
        publish(queryMessage, topic);

    }

    private void publishLocal(QueryMessage queryMessage) {
        EventBus.getDefault().post(queryMessage);
    }

    @Override
    public void publish(QueryResponseMessage queryResponseMessage) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + queryResponseMessage.getSubscriberID() + "/" + Topic.QUERY_RESPONSE_TOPIC;
        queryResponseMessage.setTopic(topic);
        publish(queryResponseMessage, topic);
    }

    public void publishLocal(EventQueryMessage eventQueryMessage) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.LOCAL_EVENT_QUERY_TOPIC;
        eventQueryMessage.setTopic(topic);
        publish(eventQueryMessage, topic);

    }

    public void publishGlobal(EventQueryMessage eventQueryMessage) {

        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.GLOBAL_EVENT_QUERY_TOPIC;
        eventQueryMessage.setTopic(topic);
        publish(eventQueryMessage, topic);

    }

    public void publish(EventQueryResponseMessage eventQueryResponseMessage) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + eventQueryResponseMessage.getSubscriberID() + "/" + Topic.EVENT_QUERY_RESPONSE_TOPIC;
        eventQueryResponseMessage.setTopic(topic);
        publish(eventQueryResponseMessage, topic);
    }

    public void publish(ServiceInformationMessage serviceInformationMessage) {
        serviceInformationMessage.setPublisherID(clientId);
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.SERVICE_INFORMATION_TOPIC;
        publish(serviceInformationMessage, topic);

    }

    public void publish(Message message, String topic) {
        send(message, topic); // publica no mqqt
    }

    public void send(Message message, String topic) {

        int reliability = getReliabilityQoS().getKind();
        boolean retained = getDurabilityQoS().isRetained();
        message.setPublisherListener(publisherListener);
        connectionService.publish(message, topic, reliability, retained);

    }

    private void publish(MessageGroup messageGroup, String topic) {
        for (Message message : messageGroup.getAll()) {
            int reliability = getReliabilityQoS().getKind();
            boolean retained = getDurabilityQoS().isRetained();
            message.setPublicationTimestamp(time.getCurrentTimestamp());
            message.setPublisherID(clientId);
            message.setRetained(retained);
            message.setQos(reliability);
            message.toJson();
        }
        publish((Message) messageGroup, topic);
    }

}
