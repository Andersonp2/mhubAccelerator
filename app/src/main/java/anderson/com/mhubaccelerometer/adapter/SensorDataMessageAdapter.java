package anderson.com.mhubaccelerometer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import anderson.com.mhubaccelerometer.R;
import br.pucrio.inf.lac.mhubcddl.cddl.message.SensorDataMessage;

/**
 * Created by lcmuniz on 20/12/16.
 */

public class SensorDataMessageAdapter extends BaseAdapter {

    private final Context context;
    private final Map<String, SensorDataMessage> map;
    private final ArrayList<String> keys;

    public SensorDataMessageAdapter(Context context) {
        map = new HashMap<>();
        keys = new ArrayList<>();
        this.context = context;
    }

    @Override
    public int getCount() {
        return map.size();
    }

    @Override
    public SensorDataMessage getItem(int position) {
        SensorDataMessage sensorDataMessage = map.get(keys.get(position));
        return sensorDataMessage;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.layout_listitem, parent, false);
        TextView serviceName = (TextView) rowView.findViewById(R.id.serviceName);
        TextView serviceValues = (TextView) rowView.findViewById(R.id.serviceValues);

        SensorDataMessage sensorDataMessage = getItem(position);

        serviceName.setText(sensorDataMessage.getServiceName().toString());

        String vs =new Gson().toJson(sensorDataMessage.getServiceValue());
        serviceValues.setText(vs);

        return rowView;
    }

    public void add(SensorDataMessage sensorDataMessage) {
        if (!map.containsKey(sensorDataMessage.getServiceName())) {
            keys.add(sensorDataMessage.getServiceName());
        }
        map.put(sensorDataMessage.getServiceName(), sensorDataMessage);


    }

}
