package com.ideas.micro.jasonapp102;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewAdapter_Mygroup extends BaseAdapter {
    private final String TAG = "Mygroup適配器";
    private JSONArray ElementsData ;   //資料
    private int[] resids;
    private LayoutInflater inflater;    //加載layout
    private int indentionBase;          //item缩排
    private JSONObject textColor = new JSONObject();

    //優化Listview 避免重新加載
    //這邊宣告你會動到的Item元件
    static class ViewHolder{
        TextView txt_clinicname;
        TextView txt_membername;
        LinearLayout layout_listview_mygroup;
    }

    //初始化
    public ViewAdapter_Mygroup(JSONArray data, LayoutInflater inflater){
        this.ElementsData = data;
        this.inflater = inflater;
        this.resids = resids;
        indentionBase = 100;
    }

    //取得數量
    @Override
    public int getCount() {
        return ElementsData.length();
    }
    //取得Item

    @Override
    public JSONObject getItem(int position) {
        try {
            return ElementsData.getJSONObject(position);
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
        ViewAdapter_Mygroup.ViewHolder holder;
        //當ListView被拖拉時會不斷觸發getView，為了避免重複加載必須加上這個判斷
        if (convertView == null) {
            holder = new ViewAdapter_Mygroup.ViewHolder();
            convertView = inflater.inflate(R.layout.listview_mygroup, null);
            holder.txt_clinicname = (TextView) convertView.findViewById(R.id.txt_clinicname);
            holder.txt_membername = (TextView) convertView.findViewById(R.id.txt_membername);
            holder.layout_listview_mygroup = (LinearLayout) convertView.findViewById(R.id.layout_listview_mygroup);
            convertView.setTag(holder);
        } else {
            holder = (ViewAdapter_Mygroup.ViewHolder) convertView.getTag();
        }

        JSONObject measuredata = getItem(position);
        try {
//            holder.text_measuretime.setText(measuredata.getString("rdate")  + " " +  measuredata.getString("rtime"));
                holder.txt_clinicname.setText(measuredata.getString("clinicNameShort"));
                holder.txt_membername.setText(measuredata.getString("memberName"));
            if (position%2 == 0) {
                Log.e(TAG, "position = " + position + " 底灰");
                holder.layout_listview_mygroup.setBackgroundColor(0xDDDDDD);
            } else {
                Log.e(TAG, "position = " + position + " 底白");
                holder.layout_listview_mygroup.setBackgroundColor(0xFFFFFF);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
