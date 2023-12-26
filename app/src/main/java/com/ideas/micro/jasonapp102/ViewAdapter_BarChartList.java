package com.ideas.micro.jasonapp102;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewAdapter_BarChartList extends BaseAdapter {
    private static final String TAG = "BarChartListAdapter";
    private JSONArray ElementsData ;   //資料
    private static boolean[] hasChoosed;
    private LayoutInflater inflater;    //加載layout
    private int indentionBase;          //item缩排
    private JSONObject textColor = new JSONObject();
    private boolean lightEnable = false;        // 預設不使用 recordLight 功能
    private String colorBefore;
    private String colorAfter;
    private int positionBefore = -1, positionAfter = -1;

    //優化Listview 避免重新加載
    //這邊宣告你會動到的Item元件
    static class ViewHolder{
        TextView txt_barcharttime;
        TextView txt_checkmark;
        LinearLayout layout_listview_barcompare;
    }

    //初始化
    public ViewAdapter_BarChartList(JSONArray data,LayoutInflater inflater){
        this.ElementsData = data;
        this.inflater = inflater;
        this.hasChoosed = new boolean[data.length()];   // 預設是 False
        indentionBase = 100;
    }

    public void setColor(String colorafter, String colorbefore){
        this.colorAfter = colorafter;
        this.colorBefore = colorbefore;
    }

    public void setChoosedPosition(int positionafter, int positionbefore){
        this.positionAfter = -1; this.positionBefore = -1;
        this.positionBefore = positionbefore;
        this.positionAfter = positionafter;
    }

    public  void setHasChoosed(int posi){
        hasChoosed[posi] = !hasChoosed[posi];
        Log.e(TAG, "設定第 " + posi + " 按鍵為 " + hasChoosed[posi]);
        int count = 0;
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
        ViewHolder holder;
        //當ListView被拖拉時會不斷觸發getView，為了避免重複加載必須加上這個判斷
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_barcomparelist, null);
            holder.txt_barcharttime = (TextView) convertView.findViewById(R.id.txt_barcharttime);
            holder.txt_checkmark = (TextView) convertView.findViewById(R.id.txt_checkmark) ;
//            holder.switch_barchart = (Switch) convertView.findViewById(R.id.switch_barchart);
            holder.layout_listview_barcompare = (LinearLayout) convertView.findViewById(R.id.layout_listview_barcompare);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        JSONObject measuredata = getItem(position);
        try {
            holder.txt_barcharttime.setText(measuredata.getString("rdate")  + " " +  measuredata.getString("rtime") + " ( "  +
                    (measuredata.getInt("position") == 10?"左手":"右手") + " )");
            holder.txt_checkmark.setText(hasChoosed[position]?"V":"");
//            Log.e(TAG, "Holder 前 " + this.positionBefore + " 後 " + this.positionAfter);
            if (position == this.positionBefore) {
                holder.txt_barcharttime.setTextColor(Color.BLACK);
                holder.layout_listview_barcompare.setBackgroundColor(Color.parseColor(colorBefore));
            } else if (position == this.positionAfter) {
                holder.txt_barcharttime.setTextColor(Color.WHITE);
                holder.layout_listview_barcompare.setBackgroundColor(Color.parseColor(colorAfter));
            } else {
                holder.txt_barcharttime.setTextColor(Color.BLACK);
                holder.layout_listview_barcompare.setBackgroundColor(Color.WHITE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
