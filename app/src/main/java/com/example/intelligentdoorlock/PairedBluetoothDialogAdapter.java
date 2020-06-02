package com.example.intelligentdoorlock;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class PairedBluetoothDialogAdapter extends BaseAdapter {
    public static final String TAG = "ListViewAdapter";
    private Context context;
    private List<HashMap> arrayList;

    public PairedBluetoothDialogAdapter(Context context, List<HashMap> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        LayoutInflater mInflater = LayoutInflater.from(context);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_paired_bluetooth, null);
            holder.tvName = (TextView) convertView.findViewById(R.id.item_name);
            holder.tvAddress = (TextView) convertView.findViewById(R.id.item_address);
            convertView.setTag(holder);
        } else {
            Log.d(TAG, "not_null " + position);
            holder = (ViewHolder) convertView.getTag();
        }
        holder.device = (BluetoothDevice) ((HashMap) arrayList.get(position)).get("blue_device");
        holder.tvName.setText((String) ((HashMap) arrayList.get(position)).get("blue_name"));
        holder.tvAddress.setText((String) ((HashMap) arrayList.get(position)).get("blue_address"));
        return convertView;
    }

    static class ViewHolder {
        public BluetoothDevice device;//不是用来显示，用来在item点击时返回连接对象
        public TextView tvName;
        public TextView tvAddress;
    }
}