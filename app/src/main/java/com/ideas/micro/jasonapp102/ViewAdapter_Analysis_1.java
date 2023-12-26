package com.ideas.micro.jasonapp102;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewAdapter_Analysis_1 extends BaseAdapter {
    private String TAG = "分析轉接";
    private String[] displayData ;   //資料
    private JSONObject dataJson;
    private int[] resids;
    private LayoutInflater inflater;    //加載layout
    private int indentionBase;          //item缩排
    private String[] harmonic = {"諧波", "H0", "H1", "H2", "H3", "H4", "H5", "H6", "H7", "H8", "H9", "H10"};

    //優化Listview 避免重新加載
    //這邊宣告你會動到的Item元件
    static class ViewHolder{
        LinearLayout layout_analysis;
        TextView label_meridiancode;
        TextView label_item1, label_item2, label_item3, label_item4;

    }
    public ViewAdapter_Analysis_1(String[] displayitem, JSONObject data, LayoutInflater inflater){
        this.displayData = displayitem;
        this.dataJson = data;
        this.inflater = inflater;

    }

    @Override
    public int getCount() {
        return harmonic.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewAdapter_Analysis_1.ViewHolder holder;
        //當ListView被拖拉時會不斷觸發getView，為了避免重複加載必須加上這個判斷
        if (convertView == null) {
            holder = new ViewAdapter_Analysis_1.ViewHolder();
            if (displayData.length == 1) {
                convertView = inflater.inflate(R.layout.listview_analysis_1column, null);
                holder.layout_analysis = (LinearLayout) convertView.findViewById(R.id.layout_listview_analysis);
                holder.label_meridiancode = (TextView) convertView.findViewById(R.id.label_meridiancode);
                holder.label_item1 = (TextView) convertView.findViewById(R.id.label_1item1);
                convertView.setTag(holder);
            }  else  if (displayData.length == 2) {
                convertView = inflater.inflate(R.layout.listview_analysis_2column, null);
                holder.layout_analysis = (LinearLayout) convertView.findViewById(R.id.layout_listview_analysis);
                holder.label_meridiancode = (TextView) convertView.findViewById(R.id.label_meridiancode);
                holder.label_item1 = (TextView) convertView.findViewById(R.id.label_2item1);
                holder.label_item2 = (TextView) convertView.findViewById(R.id.label_2item2);
                convertView.setTag(holder);
            } else if (displayData.length == 4) {
                convertView = inflater.inflate(R.layout.listview_analysis_4column, null);
                holder.layout_analysis = (LinearLayout) convertView.findViewById(R.id.layout_listview_analysis);
                holder.label_meridiancode = (TextView) convertView.findViewById(R.id.label_meridiancode);
                holder.label_item1 = (TextView) convertView.findViewById(R.id.label_4item1);
                holder.label_item2 = (TextView) convertView.findViewById(R.id.label_4item2);
                holder.label_item3 = (TextView) convertView.findViewById(R.id.label_4item3);
                holder.label_item4 = (TextView) convertView.findViewById(R.id.label_4item4);
                convertView.setTag(holder);
            }
        } else {
            holder = (ViewAdapter_Analysis_1.ViewHolder) convertView.getTag();
        }
        if (position % 2 == 0 ){
            holder.layout_analysis.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.layout_analysis.setBackgroundColor(Color.parseColor("#DDDDDD"));
        }
        try {
            if (displayData.length == 1) {
                holder.label_meridiancode.setText(harmonic[position]);
                if (position > 0) {     // position = 0 標題  無法轉換為數字    displayData[1] 為能量變異
                    float ampv = Float.parseFloat(dataJson.getJSONArray(displayData[0]).getString(position));
                    Log.e(TAG, "能量變異 = " + ampv);
                    if (position >= 1 && position <= 5 && ampv > 5) { // H0, H1, H2, H3, H4
                        holder.label_item1.setTextColor(0xFFFF0000);
                    } else if (position >= 6 && ampv > 10) {    // H5 以上
                        holder.label_item1.setTextColor(0xFFFF0000);
                    }
                }
                holder.label_item1.setText(dataJson.getJSONArray(displayData[0]).getString(position));
            } else if (displayData.length == 2) {
                holder.label_meridiancode.setText(harmonic[position]);
                if (position > 0) {     // position = 0 標題  無法轉換為數字    displayData[1] 為能量變異
                    float ampv = Float.parseFloat(dataJson.getJSONArray(displayData[1]).getString(position));
                    Log.e(TAG, "能量變異 = " + ampv);
                    if (position >= 1 && position <= 5 && ampv > 5) { // H0, H1, H2, H3, H4
                        holder.label_item2.setTextColor(0xFFFF0000);
                    } else if (position >= 6 && ampv > 10) {    // H5 以上
                        holder.label_item2.setTextColor(0xFFFF0000);
                    }
                }
                holder.label_item1.setText(dataJson.getJSONArray(displayData[0]).getString(position));
                holder.label_item2.setText(dataJson.getJSONArray(displayData[1]).getString(position));
            } else if (displayData.length == 4) {
                holder.label_meridiancode.setText(harmonic[position]);
                if (position > 0) {     // position = 0 標題  無法轉換為數字
                    float ampv = Float.parseFloat(dataJson.getJSONArray(displayData[1]).getString(position));
                    Log.e(TAG, "能量變異 = " + ampv);
                    if (position >= 1 && position <= 5 && ampv > 5) { // H0, H1, H2, H3, H4
                        holder.label_item2.setTextColor(0xFFFF0000);
                    } else if (position >= 6 && ampv > 10) {    // H5 以上
                        holder.label_item2.setTextColor(0xFFFF0000);
                    }
                }
                holder.label_item1.setText(dataJson.getJSONArray(displayData[0]).getString(position));
                holder.label_item2.setText(dataJson.getJSONArray(displayData[1]).getString(position));
                holder.label_item3.setText(dataJson.getJSONArray(displayData[2]).getString(position));
                holder.label_item4.setText(dataJson.getJSONArray(displayData[3]).getString(position));
            }
        } catch (JSONException e) {
        }
        return convertView;
    }
}


