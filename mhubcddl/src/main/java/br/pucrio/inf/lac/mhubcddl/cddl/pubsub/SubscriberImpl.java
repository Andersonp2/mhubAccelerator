package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

import java.util.Vector;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IClientQoSListener;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IConnectionListener;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.ISubscriberListener;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IMonitorListener;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ConnectionChangedStatusMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.LivelinessMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.message.MessageGroup;
import br.pucrio.inf.lac.mhubcddl.cddl.message.SensorDataMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ServiceInformationMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.History;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.LatencyBudgetQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.LifespanQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Provider;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Topic;


public class SubscriberImpl extends ClientImpl implements Subscriber, IConnectionListener {

    private final Vector<Message> latencyBudgestMessages = new Vector<Message>();

    private static final String TAG = SubscriberImpl.class.getSimpleName();

    private ISubscriberListener subscriberListener;

    private final Monitor monitor = Provider.newMonitor();

    private final Filter filter = Provider.newFilter(this);

    public SubscriberImpl(ISubscriberListener subscriberListener, IClientQoSListener clientQoSListener) {
        super(clientQoSListener);
        this.subscriberListener = subscriberListener;
        subscribeQueryResponseTopic();
        subscribeEventQueryResponseTopic();
    }

    public SubscriberImpl() {
        super();
        subscribeQueryResponseTopic();
        subscribeEventQueryResponseTopic();
    }
    

    @Override
    protected boolean isDeadlineMissed(long deadline) {
        Message lastMessage = getLastDeadlineMessage();
        if (lastMessage == null || lastMessage.getReceptionTimestamp() > deadline) {
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

        if ((lastLivelinessMessage != null && lastLivelinessMessage.getReceptionTimestamp() < leaseDuration) || (lastMessage != null && lastMessage.getReceptionTimestamp() < leaseDuration)) {
            setLastLivelinessMessage(null);
            setLastMessage(null);
            return false;

        } else {
            return true;
        }
    }

    protected void on_message_avaliable(Message message) {

        if (message.getClassName().equals(SensorDataMessage.class.getName()) && filter.isSet()) {
            filter.process(message);
        }
        else {
            send(message, message.getTopic());
        }

    }

    public void send(Message message, String topic) {
        if (subscriberListener != null) {
            try {
                subscriberListener.onMessageArrived(message);
                if (monitor.getNumQueries() > 0) monitor.messageArrived(message);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    protected void on_message_avaliable(MessageGroup messageGroup) {
        for (Message message : messageGroup.getAll()) {
            on_message_avaliable(message);
        }
    }


    @Override
    protected void on_message_received(Message message) {
        LatencyBudgetQoS latencyBudgetQoS = getLatencyBudgetQoS();
        if (latencyBudgetQoS.getDelay() == LatencyBudgetQoS.DEFAULT_DELAY) {
            message.setReceptionTimestamp(time.getCurrentTimestamp());
            History history = getHistory();
            long lifeTime = getLifeTime(message, false);
            LifespanQoS lifespanQoS = getLifespanQoS();
            if (lifespanQoS.getExpirationTime() == LifespanQoS.INFINITE_DURATION || lifeTime > 0) {
                history.insert(message);
                startLisfespanClock(message, lifeTime);
            }
            setLastDeadlineMessage(message);
            setLastMessage(message);
            on_message_avaliable(message);
        } else {
            latencyBudgestMessages.add(message);
        }
    }


    protected void on_latency_budget_timer_finish() {
        History history = getHistory();
        Vector<Message> messagesToRemove = new Vector<Message>(latencyBudgestMessages);
        for (Message message : messagesToRemove) {
            message.setReceptionTimestamp(time.getCurrentTimestamp());
            long lifeTime = getLifeTime(message, false);
            LifespanQoS lifespanQoS = getLifespanQoS();
            if (lifespanQoS.getExpirationTime() == LifespanQoS.INFINITE_DURATION || lifeTime > 0) {
                history.insert(message);
                startLisfespanClock(message, lifeTime);
            }
            on_message_avaliable(message);
        }
        if (!messagesToRemove.isEmpty()) {
            Message lastMessage = messagesToRemove.get(messagesToRemove.size() - 1);
            setLastDeadlineMessage(lastMessage);
            setLastMessage(lastMessage);
        }
        latencyBudgestMessages.removeAll(messagesToRemove);
    }


    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void onMessageArrived(Message message) {

        boolean acceptdRetained = getDurabilityQoS().isRetained();

        if (!acceptdRetained && message.isRetained()) {
            return;
        } else if (message instanceof LivelinessMessage) {
            setLastLivelinessMessage((LivelinessMessage) message);
            if (subscriberListener != null) subscriberListener.onMessageArrived(message);
        } else if (message instanceof ConnectionChangedStatusMessage) {
            clientQoSListener.onClientConnectionChangedStatus(message.getPublisherID(), ((ConnectionChangedStatusMessage) message).getStatus());
            if (subscriberListener != null) subscriberListener.onMessageArrived(message);

        } else {
            addToQueue(message);
        }
    }


    public void subscribeSensorDataTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.SENSOR_DATA_TOPIC + "/#";
        subscribe(topic);
    }

    public void unsubscribeSensorDataTopicByServiceName() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.SENSOR_DATA_TOPIC + "/#";
        unsubscribe(topic);
    }

    public void subscribeSensorDataTopicByServiceName(String serviceName) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.SENSOR_DATA_TOPIC + "/" + serviceName;
        subscribe(topic);
    }

    public void unsubscribeSensorDataTopicByServiceName(String serviceName) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.SENSOR_DATA_TOPIC + "/" + serviceName;
        unsubscribe(topic);
    }

    @Override
    public void subscribeSensorDataTopicByPublisherId(String publisherId) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.SENSOR_DATA_TOPIC + "/#";
        subscribe(topic);
    }

    public void unsubscribeSensorDataTopicByPublisherId(String publisherId) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.SENSOR_DATA_TOPIC + "/#";
        unsubscribe(topic);
    }

    @Override
    public void subscribeSensorDataTopicByPublisherIdAndServiceName(String publisherId, String serviceName) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.SENSOR_DATA_TOPIC + "/" + serviceName;
        subscribe(topic);
    }

    @Override
    public void unsubscribeSensorDataTopicByPublisherIdAndServiceName(String publisherId, String serviceName) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.SENSOR_DATA_TOPIC + "/" + serviceName;
        unsubscribe(topic);
    }

    @Override
    public void subscribeSensorDataTopicByServiceInformationMessage(ServiceInformationMessage sim) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + sim.getPublisherID() + "/" + Topic.SENSOR_DATA_TOPIC + "/" + sim.getServiceName();
        subscribe(topic);
    }

    public void unsubscribeSensorDataTopicByServiceInformationMessage(ServiceInformationMessage sim) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + sim.getPublisherID() + "/" + Topic.SENSOR_DATA_TOPIC + "/" + sim.getServiceName();
        unsubscribe(topic);
    }

    public void subscribeLocalQueryTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.LOCAL_QUERY_TOPIC;
        subscribe(topic);
    }

    public void unsubscribeLocalQueryTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.LOCAL_QUERY_TOPIC;
        unsubscribe(topic);
    }

    public void subscribeGlobalQueryTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.GLOBAL_QUERY_TOPIC;
        subscribe(topic);
    }

    public void unsubscribeGlobalQueryTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.GLOBAL_QUERY_TOPIC;
        unsubscribe(topic);
    }


    public void subscribeQueryResponseTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.QUERY_RESPONSE_TOPIC;
        subscribe(topic);
    }

    public void unsubscribeQueryResponseTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.QUERY_RESPONSE_TOPIC;
        unsubscribe(topic);
    }

    public void subscribeLocalEventQueryTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.LOCAL_EVENT_QUERY_TOPIC;
        subscribe(topic);
    }

    public void unsubscribeLocalEventQueryTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.LOCAL_EVENT_QUERY_TOPIC;
        unsubscribe(topic);
    }

    public void subscribeGlobalEventQueryTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.GLOBAL_EVENT_QUERY_TOPIC;
        subscribe(topic);
    }

    public void unsubscribeGlobalEventQueryTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.GLOBAL_EVENT_QUERY_TOPIC;
        unsubscribe(topic);
    }

    public void subscribeEventQueryResponseTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.EVENT_QUERY_RESPONSE_TOPIC;
        subscribe(topic);
    }

    public void unsubscribeEventQueryResponseTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.EVENT_QUERY_RESPONSE_TOPIC;
        unsubscribe(topic);
    }


    public void subscribeEventQueryResponseTopicBySubscriberId(String clientId) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.EVENT_QUERY_RESPONSE_TOPIC;
        subscribe(topic);
    }

    public void unsubscribeEventQueryResponseTopicBySunscriberId(String clientId, IConnectionListener listener) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientId + "/" + Topic.EVENT_QUERY_RESPONSE_TOPIC;
        unsubscribe(topic);
    }


    public void subscribeLivelenessTopicByPublisherId(String publisherId) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.LIVELINESS_TOPIC;
        subscribe(topic);
    }

    public void unsubscribeLivelenessTopicByPublisherId(String publisherId) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.LIVELINESS_TOPIC;
        unsubscribe(topic);
    }

    @Override
    public void subscribeLivelenessTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.LIVELINESS_TOPIC;
        subscribe(topic);
    }

    public void unsubscribeLivelenessTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.LIVELINESS_TOPIC;
        unsubscribe(topic);
    }

    public void subscribeConnectionChangedStatusTopic(String publisherID) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherID + "/" + Topic.CONNECTION_CHANGED_STATUS_TOPIC;
        subscribe(topic);
    }

    public void unsubscribeConnectionChangedStatusTopic(String publisherID) {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherID + "/" + Topic.CONNECTION_CHANGED_STATUS_TOPIC;
        unsubscribe(topic);
    }

    @Override
    public void subscribeConnectionChangedStatusTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + "+" + "/" + Topic.CONNECTION_CHANGED_STATUS_TOPIC;
        subscribe(topic);
    }

    @Override
    public void addMonitorQuery(String query, IMonitorListener monitorListener) {
        monitor.addQuery(query, monitorListener);
    }

    public void unsubscribeConnectionChangedStatusTopic() {
        String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + "+" + "/" + Topic.CONNECTION_CHANGED_STATUS_TOPIC;
        unsubscribe(topic);
    }

    public void subscribe(String topic) {
        int reliability = getReliabilityQoS().getKind();
        connectionService.subscribe(topic,reliability,this);
        //EventBus.getDefault().post(new ConnectionServiceSubscribeMessage(topic, reliability, this));

    }

    public void unsubscribe(String topic) {
        connectionService.unsubscribe(topic,this);
        //EventBus.getDefault().post(new ConnectionServiceUnsubscribeMessage(topic, this));
    }


    public Monitor getMonitor() {
        return monitor;
    }

    @Override
    public void setSubscriberQoSListener(IClientQoSListener subscriberQoSListener) {
        super.setClientQoSListener(subscriberQoSListener);
    }

    @Override
    public void setSubscriberListener(ISubscriberListener subscriberListener) {
        this.subscriberListener = subscriberListener;
    }

    @Override
    public void setFilter(String eplFilter) {
        filter.set(eplFilter);
    }

}

