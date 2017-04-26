package br.pucrio.inf.lac.mhubcddl.cddl.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.eclipse.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IConnectionListener;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IPublisherListener;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ConnectionChangedStatusMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ConnectionServiceConnectMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ConnectionServicePublishMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ConnectionServiceSubscribeMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ConnectionServiceUnsubscribeMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.EventQueryMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.EventQueryResponseMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.LivelinessMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.message.MessageGroup;
import br.pucrio.inf.lac.mhubcddl.cddl.message.QueryMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.QueryResponseMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.SensorDataMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ServiceInformationMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Preferences;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Time;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Topic;
import br.pucrio.inf.lac.mhubcddl.mhub.components.AppUtils;


public class ConnectionService extends Service implements IMqttActionListener, MqttCallback {

    public static final String TAG = ConnectionService.class.getSimpleName();

    public static final String TCP = "tcp";
    public static final String SSL = "ssl";
    public static final String ECLIPSE_HOST = "iot.eclipse.org";
    public static final String DASHBOARD_HOST = "broker.mqttdashboard.com";
    public static final String MOSCA_HOST = "test.mosca.io";
    public static final String HIVEMQ_HOST = "broker.hivemq.com";
    public static final String MOSQUITTO_HOST = "test.mosquitto.org";
    public static final String LSD_HOST = "lsd.ufma.br";
    public static final String LOCAL_HOST = "localhost";
    public static final String DEFAULT_PORT = "1883";
    public static final String DEFAULT_WEBSOCKET_PORT = "8080";
    public static final String DEFAULT_PASSWORD_FILE = "";

    private String protocol = TCP;
    private String host = ECLIPSE_HOST;
    private String port = DEFAULT_PORT;
    private String webSocketPort = DEFAULT_WEBSOCKET_PORT;
    private String passwordFile = DEFAULT_PASSWORD_FILE;

    private long automaticReconnectionTime = 1000;
    private boolean cleanSession = true;
    private int connectionTimeout = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
    private int keepAliveInterval = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;
    private MemoryPersistence memoryPersistence = new MemoryPersistence();
    private MqttAsyncClient mqttClient;
    private HashMap<IConnectionListener, List<String>> listeners = new HashMap<>();
    private Time time = Time.getInstance();
    private String clientID = UUID.randomUUID().toString();
    private String lastUri = null;
    private boolean automaticReconected = true;
    private boolean requestDisconnect = false;
    private static ConnectionService instance = null;
    private boolean publishConnectionChangedStatus = false;
    private ArrayList<Message> reliableMessages = new ArrayList<Message>();
    private int messagesCount = 0;
    private RepublishMessagesTimerTask republishMessagesTimerTask = null;
    private Timer timer = new Timer();
    private Server server = null;
    private int maxInflightMessages = MqttConnectOptions.MAX_INFLIGHT_DEFAULT;
    private String username = "";
    private String password = "";
    private MqttConnectOptions options = null;
    private boolean duplicatedID = false;


    private ConnectionService() {
        Collections.synchronizedList(reliableMessages);
    }

    public static ConnectionService getInstance(){
        if(instance == null) {
            instance = new ConnectionService();
        }
        return instance;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AppUtils.logger('i', TAG, ">> Connection Service Started");

        EventBus.getDefault().register(this);

        connect();


        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        AppUtils.logger('i', TAG, ">> Connection Service Stoped");
        EventBus.getDefault().unregister(this);

        disconnect();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private boolean isConnected() {
        if (mqttClient != null) {
            return mqttClient.isConnected();
        }
        return false;
    }

    private void connect(String clientID, String protocol, String host, String port, long automaticReconnectionTime, boolean cleanSession, int connectionTimeout, int keepAliveInterval, boolean publishConnectionChangedStatus, int maxInflightMessages, String username, String password) {
        if(!isConnected()) {
            try {
                String uri = protocol + "://" + host + ":" + port;
                lastUri = uri;
                this.automaticReconnectionTime = automaticReconnectionTime;
                options = new MqttConnectOptions();
                this.cleanSession = cleanSession;
                options.setCleanSession(cleanSession);
                this.keepAliveInterval = keepAliveInterval;
                options.setKeepAliveInterval(keepAliveInterval);
                this.keepAliveInterval = keepAliveInterval;
                options.setConnectionTimeout(connectionTimeout);
                this.publishConnectionChangedStatus = publishConnectionChangedStatus;
                options.setMaxInflight(maxInflightMessages);
                this.maxInflightMessages = maxInflightMessages;
                options.setPassword(username.toCharArray());
                this.username = username;
                options.setPassword(password.toCharArray());
                this.password = password;
                if (isPublishConnectionChangedStatus()) {
                    ConnectionChangedStatusMessage connectionChangedStatusMessage = new ConnectionChangedStatusMessage();
                    connectionChangedStatusMessage.setStatus(ConnectionChangedStatusMessage.CLIENT_DESCONNECTED_FOR_FAILURE);
                    String topic = Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientID + "/" + Topic.CONNECTION_CHANGED_STATUS_TOPIC;
                    options.setWill(topic, connectionChangedStatusMessage.toString().getBytes(), 2, false);
                }
                mqttClient = new MqttAsyncClient(uri, clientID, memoryPersistence);
                mqttClient.setCallback(this);
                IMqttToken token = mqttClient.connect(options, null, this);

                token.waitForCompletion();
            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void connect() {
        connect(clientID, protocol, host, port, automaticReconnectionTime, cleanSession, connectionTimeout, keepAliveInterval, publishConnectionChangedStatus,maxInflightMessages,username,password);
    }

    private void reconnect() {
        if (!isConnected()) {
            try {
                Thread.sleep(automaticReconnectionTime);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            AppUtils.logger('i', TAG, ">> Reconnecting....");
            connect();


        }
    }

    public void disconnect() {
        requestDisconnect = true;
        if (isConnected()) {
            try {
                if (isPublishConnectionChangedStatus()) {
                    ConnectionChangedStatusMessage connectionChangedStatusMessage = new ConnectionChangedStatusMessage();
                    connectionChangedStatusMessage.setStatus(ConnectionChangedStatusMessage.CLIENT_SELF_DESCONNECTED);
                    publishConnectionStatusMessage(connectionChangedStatusMessage);
                }
                IMqttToken token = mqttClient.disconnect();

                token.waitForCompletion();
                if(!isConnected()){
                    AppUtils.logger('i', TAG, ">> connection finished.");
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }

        }

    }



    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        System.out.println(iMqttToken.getResponse());
        if(iMqttToken.getResponse().toString().equalsIgnoreCase("CONNACK msgId 0 session present:true return code: 0")){
          duplicatedID = true;
        }
        if(republishMessagesTimerTask==null){
            RepublishMessagesTimerTask republishMessagesTimerTask = new RepublishMessagesTimerTask();
            timer.schedule(republishMessagesTimerTask,automaticReconnectionTime);
        }
        AppUtils.logger('i', TAG, ">> connected.");
        requestDisconnect = false;
        if (isPublishConnectionChangedStatus()) {
            ConnectionChangedStatusMessage connectionChangedStatusMessage = new ConnectionChangedStatusMessage();
            connectionChangedStatusMessage.setStatus(ConnectionChangedStatusMessage.CLIENT_CONNECTED);
            publishConnectionStatusMessage(connectionChangedStatusMessage);
        }

    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        AppUtils.logger('e', TAG, ">> connection fail.");
        if (!requestDisconnect && automaticReconected) {
            reconnect();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
       AppUtils.logger('e', TAG, ">> connection lost.");
        if (!requestDisconnect && automaticReconected) {
            reconnect();
        }
    }

    public void publishConnectionStatusMessage(ConnectionChangedStatusMessage connectionChangedStatusMessage) {
        publish(connectionChangedStatusMessage, Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + clientID + "/" + Topic.CONNECTION_CHANGED_STATUS_TOPIC, 2, false);
    }

    public void publish(Message message, String topic, int reliability, boolean retained) {
        if(isConnected()) {
            message.setPublisherID(clientID);
            message.setRetained(retained);
            message.setQos(reliability);
            message.setPublicationTimestamp(time.getCurrentTimestamp());
            message.setPayload(message.toString().getBytes());
            //AppUtils.logger('i', TAG, ">> Publishing Message : " + message);
            try {
                DeliveredMessageActionLister deliveredMessageActionLister = new DeliveredMessageActionLister(message);
                mqttClient.publish(topic, message, null, deliveredMessageActionLister);
            } catch (Exception e) {
                AppUtils.logger('e', TAG, ">> Fail publishing on topic " + topic + ": " + message);
                AppUtils.logger('e', TAG, ">> Publication Failed.");
                if (!cleanSession && message.getQos()>0 && !reliableMessages.contains(message)) {
                    reliableMessages.add(message);
                }

            }
        }
        else  if (!cleanSession && message.getQos()>0 && !reliableMessages.contains(message)) {
            reliableMessages.add(message);
        }

    }

    public void subscribe(String topic, int reliability, IConnectionListener listener) {
        if (isConnected()) {
            try {
                mqttClient.subscribe(topic, reliability);
                registerListenerAndTopic(listener, topic);
                AppUtils.logger('i', TAG, ">> subscribed on topic " + topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void unsubscribe(String topic, IConnectionListener listener) {
        if (isConnected()) {
            try {
                mqttClient.unsubscribe(topic);
                unregisterListenerAndTopic(listener, topic);
                AppUtils.logger('i', TAG, ">> unsubscribed on topic " + topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

        byte[] payload = mqttMessage.getPayload();

        if (payload == null) {
            return;
        } else {

            messagesCount++;

            //AppUtils.logger('d', TAG, "Receive message " + mqttMessage.toString());

            //AppUtils.logger('d', TAG, messagesCount+ " Messages");



            Message message = Message.convertFromPayload(payload);

            if (message == null) {
                for (IConnectionListener listener : listeners.keySet()) {

                    List<String> subscriptions = listeners.get(listener);

                    message = new Message();
                    message.setPayload(mqttMessage.getPayload());

                    if (subscriptions.contains(topic)) {
                        listener.onMessageArrived(message);
                    }

                }
            } else if (message.getClassName().equalsIgnoreCase(MessageGroup.class.getName())) {

                MessageGroup messageGroup = (MessageGroup) Message.convertFromPayload(payload, MessageGroup.class);

                for (Message msg : messageGroup.takeAll()) {

                    payload = msg.toString().getBytes();
                    forward(topic, msg, payload);
                }


            } else {
                forward(topic, message, payload);
            }
        }


    }

    private void forward(String topic, Message message, byte[] payload) {

        if (message.getClassName().equalsIgnoreCase(SensorDataMessage.class.getName())) {
            SensorDataMessage sensorDataMessage = (SensorDataMessage) Message.convertFromPayload(payload, SensorDataMessage.class);

            String publisherId = sensorDataMessage.getPublisherID();
            String service = sensorDataMessage.getServiceName();

            for (IConnectionListener listener : listeners.keySet()) {

                List<String> subscriptions = listeners.get(listener);

                if ((subscriptions.contains(sensorDataMessage.getTopic())) || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.SENSOR_DATA_TOPIC + "/#") || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.SENSOR_DATA_TOPIC + "/" + service) || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.SENSOR_DATA_TOPIC + "/+") || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.SENSOR_DATA_TOPIC + "/#") || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.SENSOR_DATA_TOPIC + "/+") || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.SENSOR_DATA_TOPIC + "/" + service)) {
                    listener.onMessageArrived(sensorDataMessage);
                }


            }

        } else if (message.getClassName().equalsIgnoreCase(QueryMessage.class.getName())) {
            QueryMessage sqm = (QueryMessage) Message.convertFromPayload(payload, QueryMessage.class);

            String[] topic1 = topic.split("/");
            String destiny = topic1[2];

            for (IConnectionListener listener : listeners.keySet()) {

                List<String> subscriptions = listeners.get(listener);
                if (subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + destiny)) {
                    listener.onMessageArrived(sqm);
                }

            }
        } else if (message.getClassName().equalsIgnoreCase(QueryResponseMessage.class.getName())) {
            QueryResponseMessage sqrm = (QueryResponseMessage) Message.convertFromPayload(payload, QueryResponseMessage.class);

            // pega o id do subscriber da resposta
            String[] topic1 = topic.split("/");
            String subscriberId = topic1[1];

            for (IConnectionListener listener : listeners.keySet()) {

                List<String> subscriptions = listeners.get(listener);

                if (subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + subscriberId + "/" + Topic.QUERY_RESPONSE_TOPIC)) {
                    listener.onMessageArrived(sqrm);
                }

            }

        } else if (message.getClassName().equalsIgnoreCase(EventQueryMessage.class.getName())) {
            EventQueryMessage eqm = (EventQueryMessage) Message.convertFromPayload(payload, EventQueryMessage.class);

            String[] topic1 = topic.split("/");
            String destiny = topic1[2];

            for (IConnectionListener listener : listeners.keySet()) {

                List<String> subscriptions = listeners.get(listener);
                if (subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + destiny)) {
                    listener.onMessageArrived(eqm);
                }

            }
        } else if (message.getClassName().equalsIgnoreCase(EventQueryResponseMessage.class.getName())) {
            EventQueryResponseMessage eqrm = (EventQueryResponseMessage) Message.convertFromPayload(payload, EventQueryResponseMessage.class);

            // pega o id do subscriber da resposta
            String[] topic1 = topic.split("/");
            String subscriberId = topic1[1];

            for (IConnectionListener listener : listeners.keySet()) {

                List<String> subscriptions = listeners.get(listener);

                if (subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + subscriberId + "/" + Topic.EVENT_QUERY_RESPONSE_TOPIC)) {
                    listener.onMessageArrived(eqrm);
                }

            }

        } else if (message.getClassName().equalsIgnoreCase(ServiceInformationMessage.class.getName())) {
            ServiceInformationMessage sim = (ServiceInformationMessage) Message.convertFromPayload(payload, ServiceInformationMessage.class);

            String publisherId = sim.getPublisherID();
            for (IConnectionListener listener : listeners.keySet()) {

                List<String> subscriptions = listeners.get(listener);
                if (subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/+/" + Topic.SERVICE_INFORMATION_TOPIC) || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.SERVICE_INFORMATION_TOPIC)) {
                    listener.onMessageArrived(sim);
                }
            }
        } else if (message.getClassName().equalsIgnoreCase(LivelinessMessage.class.getName())) {

            String publisherId = message.getPublisherID();

            LivelinessMessage livelinessMessage = (LivelinessMessage) Message.convertFromPayload(payload, LivelinessMessage.class);

            for (IConnectionListener listener : listeners.keySet()) {

                List<String> subscriptions = listeners.get(listener);

                if (subscriptions.contains(topic) || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.LIVELINESS_TOPIC) || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + "+" + "/" + Topic.LIVELINESS_TOPIC)) {
                    listener.onMessageArrived(livelinessMessage);
                }

            }

        } else if (message.getClassName().equalsIgnoreCase(ConnectionChangedStatusMessage.class.getName())) {

            String publisherId = message.getPublisherID();

            ConnectionChangedStatusMessage connectionChangedStatusMessage = (ConnectionChangedStatusMessage) Message.convertFromPayload(payload, ConnectionChangedStatusMessage.class);

            for (IConnectionListener listener : listeners.keySet()) {

                List<String> subscriptions = listeners.get(listener);

                if (subscriptions.contains(topic) || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + publisherId + "/" + Topic.CONNECTION_CHANGED_STATUS_TOPIC) || subscriptions.contains(Topic.DOMAIN_PARTICIPANT_TOPIC + "/" + "+" + "/" + Topic.CONNECTION_CHANGED_STATUS_TOPIC)) {
                    listener.onMessageArrived(connectionChangedStatusMessage);
                }

            }

        } else {
            for (IConnectionListener listener : listeners.keySet()) {

                List<String> subscriptions = listeners.get(listener);

                if (subscriptions.contains(topic)) {
                    listener.onMessageArrived(message);
                }

            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    private void registerListenerAndTopic(IConnectionListener listener, String topic) {

        if (!listeners.containsKey(listener)) {
            // registra listener se nao existe ainda
            listeners.put(listener, new ArrayList<String>());
        }

        // pega topicos
        List<String> topics = listeners.get(listener);
        if (!topics.contains(topic)) {
            // se topico nao esta registrado, registra
            topics.add(topic);
        }
        ;

    }

    private void unregisterListenerAndTopic(IConnectionListener listener, String topic) {

        if (listeners.containsKey(listener)) {

            List<String> topics = listeners.get(listener);
            if (topics.contains(topic)) {
                topics.remove(topic);
            }

            if (topics.size() == 0) {
                listeners.remove(listener);
            }

        }

    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public long getAutomaticReconnectionTime() {
        return automaticReconnectionTime;
    }

    private void setAutomaticReconnectionTime(long automaticReconnectionTime) {
        this.automaticReconnectionTime = automaticReconnectionTime;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        if(cleanSession){
            reliableMessages.clear();
        }
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    private boolean isAutomaticReconected() {
        return automaticReconected;
    }

    public void setAutomaticReconected(boolean automaticReconected) {
        this.automaticReconected = automaticReconected;
    }

    public boolean isRequestDisconnect() {
        return requestDisconnect;
    }

    public void setRequestDisconnect(boolean requestDisconnect) {
        this.requestDisconnect = requestDisconnect;
    }

    public boolean isPublishConnectionChangedStatus() {
        return publishConnectionChangedStatus;
    }

    public void setPublishConnectionChangedStatus(boolean publishConnectionChangedStatus) {
        this.publishConnectionChangedStatus = publishConnectionChangedStatus;
    }

    public String getWebSocketPort() {
        return webSocketPort;
    }

    public void setWebSocketPort(String webSocketPort) {
        this.webSocketPort = webSocketPort;
    }

    public String getPasswordFile() {
        return passwordFile;
    }

    public void setPasswordFile(String passwordFile) {
        this.passwordFile = passwordFile;
    }

    public int getMaxInflightMessages() {
        return maxInflightMessages;
    }

    public void setMaxInflightMessages(int maxInflightMessages) {
        this.maxInflightMessages = maxInflightMessages;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public class DeliveredMessageActionLister implements IMqttActionListener {

        private Message message;


        public DeliveredMessageActionLister(Message message) {
            super();
            this.message = message;
        }


        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            AppUtils.logger('e', TAG, ">> Message Delivery Failed " + message);
            if (!cleanSession && message.getQos()>0 && !reliableMessages.contains(message)) {
                reliableMessages.add(message);
            }
        }

        @Override
        public void onSuccess(IMqttToken arg0) {
            AppUtils.logger('i', TAG, ">> Message Delivery Success " + message);
            IPublisherListener publisherListener = message.getPublisherListener();
            if(reliableMessages.contains(message)){
                reliableMessages.remove(message);
            }
            if(publisherListener!=null){
                publisherListener.onMessageDelivered(message);
            }

        }


    }


    public class RepublishMessagesTimerTask extends TimerTask{

        @Override
        public void run() {
            if(isConnected()) {
                ArrayList <Message> messages = new ArrayList <Message> (reliableMessages);
                for (Message message : messages) {
                    if (!cleanSession) {
                        AppUtils.logger('i', TAG, ">> Republishing...");
                        publish(message, message.getTopic(), message.getQos(), message.isRetained());
                    }
                }
            }
            republishMessagesTimerTask = new RepublishMessagesTimerTask();
            timer.schedule(republishMessagesTimerTask,automaticReconnectionTime);
            cancel();
        }
    }

    public void startLocalBroker(String host, String port, String webSocketPort, String passwordFile){
        Properties m_properties = new Properties();
        m_properties.put("port", port);
        m_properties.put("host", host);
        m_properties.put("websocket_port", webSocketPort);
        m_properties.put("password_file", passwordFile);

        server = new Server();
        try {
            server.startServer(m_properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startLocalBroker(){
        startLocalBroker("0.0.0.0", DEFAULT_PORT, DEFAULT_WEBSOCKET_PORT,DEFAULT_PASSWORD_FILE);
    }

    public void stopLocalBroker(){
        if (server!=null) {
            server.stopServer();
        }
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(ConnectionServicePublishMessage cspm) {
        publish(cspm.message, cspm.topic, cspm.reliability, cspm.retained);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(ConnectionServiceSubscribeMessage cssm) {
        subscribe(cssm.topic, cssm.reliability, cssm.listener);
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(ConnectionServiceUnsubscribeMessage csum) {
        unsubscribe(csum.topic, csum.listener);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public synchronized  void onMessageEvent(ConnectionServiceConnectMessage cscm) {
        Preferences preferences = new Preferences(this, Preferences.PREFERENCE_FILE_KEY);
        clientID = preferences.getString(Preferences.CLIENT_ID_KEY);
        connect();
    }


}

