package com.ideas.micro.jasonapp102;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

public class ViewAdapter_Analysis extends BaseAdapter {
    private String[] ElementsData ;   //資料
    private int[] resids;
    private LayoutInflater inflater;    //加載layout
    private int indentionBase;          //item缩排

    //優化Listview 避免重新加載
    //這邊宣告你會動到的Item元件
    static class ViewHolder{
        LinearLayout layout_analysis;
        TextView label_meridiancode;
        TextView label_energydensity;
        TextView label_energyvariance;
    }

    //初始化
    public ViewAdapter_Analysis(String[] data, LayoutInflater inflater){
        this.ElementsData = data;
        this.inflater = inflater;
        this.resids = resids;
        indentionBase = 100;
    }

    //取得數量
    @Override
    public int getCount() {
        return ElementsData.length;
    }
    //取得Item
    @Override
    public Object getItem(int position) {
            return ElementsData[position];
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
            convertView = inflater.inflate(R.layout.listview_analysis, null);
            holder.layout_analysis = (LinearLayout) convertView.findViewById(R.id.layout_listview_analysis);
            holder.label_meridiancode = (TextView) convertView.findViewById(R.id.label_meridiancode);
            holder.label_energydensity = (TextView) convertView.findViewById(R.id.label_energydensity);
            //holder.label_energyvariance = (TextView) convertView.findViewById(R.id.label_energyvariance);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position % 2 == 1 ){
            holder.layout_analysis.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.layout_analysis.setBackgroundColor(Color.parseColor("#DDDDDD"));
        }
        holder.label_meridiancode.setText("H" + position);
        holder.label_energydensity.setText(ElementsData[position]);
        //holder.label_energyvariance.setText(ElementsData[position][2]);
        return convertView;
    }
}
