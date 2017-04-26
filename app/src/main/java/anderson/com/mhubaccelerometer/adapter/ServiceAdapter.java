package anderson.com.mhubaccelerometer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.util.ArrayList;

import anderson.com.mhubaccelerometer.R;
import anderson.com.mhubaccelerometer.domain.ServiceItem;

/**
 * Created by lcmuniz on 20/12/16.
 */

public class ServiceAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<ServiceItem> items;

    public ServiceAdapter(Context context, ArrayList<ServiceItem> items) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ServiceItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.layout_listitem_service, parent, false);

        final CheckedTextView ctv = (CheckedTextView) rowView;
        ctv.setText(getItem(position).serviceName);
        ctv.setChecked(getItem(position).selected);
        ctv.setSelected(getItem(position).selected);

        final ListView lv = (ListView) parent;
        lv.setItemChecked(position, getItem(position).selected);

        return ctv;
    }

}
