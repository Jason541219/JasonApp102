package com.ideas.micro.jasonapp102;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewAdapter_UploadData extends BaseAdapter {
    private final String TAG = "Upload適配器";
    private JSONArray uploadJson ;   //資料
    private LayoutInflater inflater;    //加載layout
    private int indentionBase;          //item缩排
    private JSONObject hasChecked = new JSONObject();

    //優化Listview 避免重新加載
    //這邊宣告你會動到的Item元件
    static class ViewHolder{
        TextView uploaddata_date;
        TextView uploaddata_time;
        TextView uploaddata_name;
//        CheckBox chk_uploaddata;
//        LinearLayout layout_checkbox;
//        LinearLayout layout_datetime;
        LinearLayout layout_uploaddatalist;
    }

    //初始化
    public ViewAdapter_UploadData(JSONArray data, LayoutInflater inflater){
        this.uploadJson = data;
        this.inflater = inflater;
        indentionBase = 100;
//        Log.e(TAG, "ViewAdapter_UploadData 結構式");
//        for (int i=0; i<data.length(); i++){
//            try {
//                hasChecked.put(data.getJSONObject(i).getString("uidstr"), true);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
    }

    //取得數量
    @Override
    public int getCount() {
        return uploadJson.length();
    }
    //取得Item

    @Override
    public JSONObject getItem(int position) {
        try {
            return uploadJson.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    //此範例沒有特別設計ID所以隨便回傳一個值
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewAdapter_UploadData.ViewHolder holder;
        //當ListView被拖拉時會不斷觸發getView，為了避免重複加載必須加上這個判斷
        if (convertView == null) {
            holder = new ViewAdapter_UploadData.ViewHolder();
            convertView = inflater.inflate(R.layout.listview_uploaddata, null);
            holder.uploaddata_date = (TextView) convertView.findViewById(R.id.uploaddata_date);
            holder.uploaddata_time = (TextView) convertView.findViewById(R.id.uploaddata_time);
            holder.uploaddata_name = (TextView) convertView.findViewById(R.id.uploaddata_name);
//            holder.chk_uploaddata = (CheckBox) convertView.findViewById(R.id.chk_uploaddata);
//            holder.layout_checkbox = (LinearLayout) convertView.findViewById(R.id.layout_checkbox);
            holder.layout_uploaddatalist = (LinearLayout) convertView.findViewById(R.id.layout_uploaddatalist);
            convertView.setTag(holder);
        } else {
            holder = (ViewAdapter_UploadData.ViewHolder) convertView.getTag();
        }
        try {
            JSONObject itemjson = getItem(position);
//            Log.e(TAG, "itemjson = " + itemjson.toString());
//            boolean uploadJson_checked = hasChecked.getBoolean(itemjson.getString("uidstr"));
//            holder.chk_uploaddata.setChecked(uploadJson_checked);
            JSONObject recordjson = itemjson.getJSONObject("record_json");
//            Log.e(TAG, "recordjson = " + recordjson.toString());
            holder.uploaddata_date.setText(recordjson.getString("date"));
            holder.uploaddata_time.setText(recordjson.getString("time"));
            holder.uploaddata_name.setText(recordjson.getString("name"));
//            holder.chk_uploaddata.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    try {
//                        String uidstr = itemjson.getString("uidstr");
//                        boolean uploadJson_checked = hasChecked.getBoolean(uidstr);
//                        hasChecked.put(uidstr, !uploadJson_checked);
//                        holder.chk_uploaddata.setChecked(! uploadJson_checked);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });

            if (position%2 == 0) {
//                Log.e(TAG, "position = " + position + " 底灰");
//                holder.layout_checkbox.setBackgroundColor(0xDDDDDD);
                holder.layout_uploaddatalist.setBackgroundColor(0xDDDDDD);
            } else {
//                Log.e(TAG, "position = " + position + " 底白");
//                holder.layout_checkbox.setBackgroundColor(0xFFFFFF);
                holder.layout_uploaddatalist.setBackgroundColor(0xFFFFFF);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        return convertView;
    }
}
