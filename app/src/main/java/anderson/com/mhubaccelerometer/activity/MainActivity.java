package anderson.com.mhubaccelerometer.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import anderson.com.mhubaccelerometer.R;
import anderson.com.mhubaccelerometer.adapter.SensorDataMessageAdapter;
import br.pucrio.inf.lac.mhubcddl.cddl.message.SensorDataMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.services.LocalDirectoryService;
import br.pucrio.inf.lac.mhubcddl.cddl.services.QoCEvaluatorService;
import br.pucrio.inf.lac.mhubcddl.mhub.services.S2PAService;

public class MainActivity extends AppCompatActivity {

    ArrayList<SensorDataMessage> listItems;
    //CimArrayAdapter adapter;
    SensorDataMessageAdapter adapter;

    long RETURN_CODE = 881821;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ListView msgs = (ListView) findViewById(R.id.msgs);
        listItems = new ArrayList<SensorDataMessage>();

        //adapter = new CimArrayAdapter(this, listItems);
        adapter = new SensorDataMessageAdapter(this);
        msgs.setAdapter(adapter);


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
}
