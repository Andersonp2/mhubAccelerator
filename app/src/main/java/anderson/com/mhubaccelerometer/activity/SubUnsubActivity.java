package anderson.com.mhubaccelerometer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.util.ArrayList;

import anderson.com.mhubaccelerometer.MainApplication;
import anderson.com.mhubaccelerometer.R;
import anderson.com.mhubaccelerometer.adapter.ServiceAdapter;
import anderson.com.mhubaccelerometer.controller.AppController;
import anderson.com.mhubaccelerometer.domain.ServiceItem;

public class SubUnsubActivity extends Activity {

    private AppController app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_unsub);

        final ListView services = (ListView) findViewById(R.id.activity_sub_unsub_services);
        services.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        app = ((MainApplication) getApplication()).getAppController();

        final ArrayList<ServiceItem> items = app.getServices();

        final ServiceAdapter adapter = new ServiceAdapter(this, items);
        services.setAdapter(adapter);
        services.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final CheckedTextView ctv = (CheckedTextView) view;
                items.get(position).selected = ctv.isChecked();
            }
        });

        final Button close = (Button) findViewById(R.id.activity_sub_unsub_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (ServiceItem si : items) {

                    if (si.selected) {
                        app.getSubscriber().subscribeSensorDataTopicByPublisherIdAndServiceName(si.publisher_id, si.serviceName);
                    } else {
                        app.getSubscriber().unsubscribeSensorDataTopicByPublisherIdAndServiceName(si.publisher_id, si.serviceName);
                    }

                }

                finish();

            }
        });

    }

}
