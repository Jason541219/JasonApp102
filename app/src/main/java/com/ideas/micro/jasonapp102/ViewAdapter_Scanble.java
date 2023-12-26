package com.ideas.micro.jasonapp102;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewAdapter_Scanble extends BaseAdapter {
    private final String TAG="ScanBLE適配器";
    private JSONArray ElementsData;
    private LayoutInflater inflater;    //加載layout
    private int indentionBase;

    //優化Listview 避免重新加載
    //這邊宣告你會動到的Item元件
    static class ViewHolder{
        TextView scan_deviceName;
        TextView scan_deviceAddress;
    }

    public ViewAdapter_Scanble(JSONArray data, LayoutInflater inflater){
        this.ElementsData = data;
        this.inflater = inflater;
        indentionBase = 100;
    }

    @Override
    public int getCount() {
        return ElementsData.length();
    }

    @Override
    public JSONObject getItem(int position) {
        try {
            return ElementsData.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        //當ListView被拖拉時會不斷觸發getView，為了避免重複加載必須加上這個判斷
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_scanble, null);
            holder.scan_deviceName = (TextView) convertView.findViewById(R.id.scan_deviceName);
            holder.scan_deviceAddress = (TextView) convertView.findViewById(R.id.scan_deviceAddress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject measuredata = getItem(position);

        try {
            holder.scan_deviceName.setText(measuredata.getString("name"));
            holder.scan_deviceAddress.setText(String.valueOf(measuredata.getInt("address")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
