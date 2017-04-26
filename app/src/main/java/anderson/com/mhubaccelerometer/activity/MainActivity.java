package anderson.com.mhubaccelerometer.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import anderson.com.mhubaccelerometer.MainApplication;
import anderson.com.mhubaccelerometer.R;
import anderson.com.mhubaccelerometer.adapter.SensorDataMessageAdapter;
import anderson.com.mhubaccelerometer.controller.AppController;
import anderson.com.mhubaccelerometer.domain.ServiceItem;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IClientQoSListener;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IPublisherListener;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.ISubscriberListener;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ConnectionChangedStatusMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.message.QueryResponseMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.SensorDataMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ServiceInformationMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.LivelinessQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.ReliabilityQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.services.LocalDirectoryService;
import br.pucrio.inf.lac.mhubcddl.cddl.services.QoCEvaluatorService;
import br.pucrio.inf.lac.mhubcddl.mhub.components.AppUtils;
import br.pucrio.inf.lac.mhubcddl.mhub.services.S2PAService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AppController app;
    ArrayList<SensorDataMessage> listItems;
    //CimArrayAdapter adapter;
    SensorDataMessageAdapter adapter;

    long RETURN_CODE = 881821;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        app = ((MainApplication) getApplication()).getAppController();

        final ListView msgs = (ListView) findViewById(R.id.msgs);
        listItems = new ArrayList<SensorDataMessage>();


        //adapter = new CimArrayAdapter(this, listItems);
        adapter = new SensorDataMessageAdapter(this);
        msgs.setAdapter(adapter);

        app.getPublisher().setPublisherListener(publisherListener);
        app.getPublisher().setPublisherQoCListener(publisherQoSListener);

        app.getSubscriber().setSubscriberListener(subscriberListener);
        app.getSubscriber().setSubscriberQoSListener(subscriberQoSListener);

        //QoS
        ReliabilityQoS reliabilityQoS = new ReliabilityQoS();
        reliabilityQoS.setKind(ReliabilityQoS.EXACTLY_ONCE);

        //app.publisher.setReliabilityQoS(reliabilityQoS);
        app.getSubscriber().setReliabilityQoS(reliabilityQoS);


        LivelinessQoS livelinessQoS = new LivelinessQoS();
        livelinessQoS.setkind(LivelinessQoS.AUTOMATIC);
        livelinessQoS.setLeaseDuration(1000);

        ///app.subscriber.setLivelinessQoS(livelinessQoS);
        app.getSubscriber().subscribeLivelenessTopic();
        app.getSubscriber().subscribeConnectionChangedStatusTopic();
        ///app.publisher.setLivelinessQoS(livelinessQoS);


    }


    private void startServices() {

        final Intent s2pa = new Intent(this, S2PAService.class);
        startService(s2pa);

        final Intent qoc = new Intent(this, QoCEvaluatorService.class);
        startService(qoc);

        final Intent ld = new Intent(this, LocalDirectoryService.class);
        startService(ld);

        adapter.notifyDataSetChanged();

    }

    private void stopServices() {

        final Intent s2pa = new Intent(this, S2PAService.class);
        stopService(s2pa);

        final Intent qoc = new Intent(this, QoCEvaluatorService.class);
        stopService(qoc);

        final Intent ld = new Intent(this, LocalDirectoryService.class);
        stopService(ld);

        listItems.clear();
        adapter.notifyDataSetChanged();
    }

    private IPublisherListener publisherListener = new IPublisherListener() {


        @Override
        public void onConnect() {

        }

        @Override
        public void onDisconnect() {

        }

        @Override
        public void onMessageDelivered(Message message) {

        }
    };

    private ISubscriberListener subscriberListener = new ISubscriberListener() {

        @Override
        public void onConnect() {

        }

        @Override
        public void onDisconnect() {

        }

        @Override
        public void onMessageArrived(Message message) {
            if (message instanceof SensorDataMessage) {
                final SensorDataMessage sensorDataMessage = (SensorDataMessage) message;
                AppUtils.logger('d', TAG, "Message arrived on topic " + sensorDataMessage.getTopic() + ": " + sensorDataMessage.getServiceName());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add(sensorDataMessage);
                        adapter.notifyDataSetChanged();
                    }
                });
            } else if (message instanceof QueryResponseMessage) {
                QueryResponseMessage queryResponseMessage = (QueryResponseMessage) message;
                AppUtils.logger('d', TAG, "Response arrived on topic " + queryResponseMessage.getTopic() + ": " + queryResponseMessage);

                if (queryResponseMessage.getQueryMessage().getReturnCode() == RETURN_CODE) {
                    //AppUtils.logger('d', TAG, "This response is for the result code " + qrm.getQueryMessage().getReturnCode());

                    ArrayList<ServiceItem> services = app.getServices();

                    for (ServiceInformationMessage sim : queryResponseMessage.getServiceInformationMessageList()) {
                        ServiceItem si = new ServiceItem();
                        si.publisher_id = sim.getPublisherID();
                        si.serviceName = sim.getServiceName();

                        if (!services.contains(si)) {
                            services.add(si);
                        }

                        //pubsub.unsubscribeSensorDataTopicByServiceInformationMessage(sim);
                    }

//                    Intent intent = new Intent(MainActivity.this, SubUnsubActivity.class);
                    //Gson gs = new Gson();
                    //intent.putExtra("items", gs.toJson(items));
//                    startActivity(intent);
                    return;
                }

                for (ServiceInformationMessage sim : queryResponseMessage.getServiceInformationMessageList()) {
                    app.getSubscriber().subscribeSensorDataTopicByServiceInformationMessage(sim);
                    //pubsub.unsubscribeSensorDataTopicByServiceInformationMessage(sim);
                }
            }

        }


    };

    private IClientQoSListener publisherQoSListener = new IClientQoSListener() {


        @Override
        public void onExpectedDeadlineMissed() {

        }

        @Override
        public void onExpectedDeadlineFulfilled() {

        }

        @Override
        public void onExpectedLivelinessMissed() {

        }

        @Override
        public void onExpectedLivelinessFulfilled() {

        }

        @Override
        public void onLifespanExpired(Message message) {

        }

        @Override
        public void onClientConnectionChangedStatus(String clientId, int status) {

        }


    };

    private IClientQoSListener subscriberQoSListener = new IClientQoSListener() {


        @Override
        public void onExpectedDeadlineMissed() {
            AppUtils.logger('e', TAG, "Deadline Missed");
        }

        @Override
        public void onExpectedDeadlineFulfilled() {
            AppUtils.logger('d', TAG, "Deadline Fulfilled");
        }

        @Override
        public void onExpectedLivelinessMissed() {
            AppUtils.logger('e', TAG, "Liveliness Missed");
        }

        @Override
        public void onExpectedLivelinessFulfilled() {
            AppUtils.logger('d', TAG, "Liveliness Fulfilled");
        }

        @Override
        public void onLifespanExpired(Message message) {
            AppUtils.logger('d', TAG, "Lifespan Expired");
        }

        @Override
        public void onClientConnectionChangedStatus(String clientId, int status) {
            if (status == ConnectionChangedStatusMessage.CLIENT_CONNECTED) {
                AppUtils.logger('d', TAG, "Client " + clientId + " is Connected");
            } else {
                AppUtils.logger('d', TAG, "Client " + clientId + " is Disonnected");
            }
        }


    };

}
