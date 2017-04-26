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
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.ISubscriberListener;
import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.message.QueryResponseMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.SensorDataMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ServiceInformationMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.services.LocalDirectoryService;
import br.pucrio.inf.lac.mhubcddl.cddl.services.QoCEvaluatorService;
import br.pucrio.inf.lac.mhubcddl.mhub.components.AppUtils;
import br.pucrio.inf.lac.mhubcddl.mhub.services.S2PAService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final int MY_ACCESS_FINE_LOCATION = 9;

    private long returnCode = 881821;

    private AppController app;

    private ArrayList<SensorDataMessage> listItems;

    private SensorDataMessageAdapter adapter;

    private boolean servicesStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        app = ((MainApplication) getApplication()).getAppController();
        app.getSubscriber().setSubscriberListener(subscriberListener);

        final ListView msgs = (ListView) findViewById(R.id.activity_main_msgs);
        listItems = new ArrayList<SensorDataMessage>();

        adapter = new SensorDataMessageAdapter(this);

        msgs.setAdapter(adapter);


    }

    @Override
    protected void onResume() {
        super.onResume();
        startServices();
        servicesStarted = false;
    }

    @Override
    protected void onStop() {
        servicesStarted = true;
        super.onStop();
        stopServices();
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

    private ISubscriberListener subscriberListener = new ISubscriberListener() {

        @Override
        public void onConnect() {

        }

        @Override
        public void onDisconnect() {

        }

        @Override
        public void onMessageArrived(Message message)  {
            if(message instanceof SensorDataMessage){
                final SensorDataMessage sensorDataMessage = (SensorDataMessage) message;
                AppUtils.logger('d', TAG, "onMessageArrived: " + sensorDataMessage);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add(sensorDataMessage);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
            else if(message instanceof QueryResponseMessage){
                final QueryResponseMessage queryResponseMessage = (QueryResponseMessage) message;
                AppUtils.logger('d', TAG, "onMessageArrived: " + queryResponseMessage);

                if (queryResponseMessage.getQueryMessage().getReturnCode() == returnCode) {

                    final ArrayList<ServiceItem> services = app.getServices();

                    for (ServiceInformationMessage sim : queryResponseMessage.getServiceInformationMessageList()) {
                        final ServiceItem si = new ServiceItem();
                        si.publisher_id = sim.getPublisherID();
                        si.serviceName = sim.getServiceName();

                        if (!services.contains(si)) {
                            services.add(si);
                        }

                    }

                    final Intent intent = new Intent(MainActivity.this, SubUnsubActivity.class);
                    startActivity(intent);
                    return;
                }

                for (ServiceInformationMessage sim : queryResponseMessage.getServiceInformationMessageList()) {
                    app.getSubscriber().subscribeSensorDataTopicByServiceInformationMessage(sim);
                }

            }
        }

    };

}
