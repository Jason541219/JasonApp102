package com.ideas.micro.jasonapp102;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewAdapter_MainFunction extends BaseAdapter {
    private String[][] ElementsData ;   //資料
    private int[] resids;
    private LayoutInflater inflater;    //加載layout
    private int indentionBase;          //item缩排

    //優化Listview 避免重新加載
    //這邊宣告你會動到的Item元件
    static class ViewHolder{
        ImageView mainfunctionimg;
        TextView mainfunctiontitle;
        TextView mainfunctiondes;
    }

    //初始化
    public ViewAdapter_MainFunction(String[][] data, int[] resids, LayoutInflater inflater){
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
    public String[] getItem(int position) {
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
            convertView = inflater.inflate(R.layout.listview_mainfuntion, null);
            holder.mainfunctiontitle = (TextView) convertView.findViewById(R.id.label_mainfunctiontitle);
            holder.mainfunctiondes = (TextView) convertView.findViewById(R.id.label_mainfunctiondes);
            holder.mainfunctionimg = (ImageView) convertView.findViewById(R.id.img_mainfunctionimgview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mainfunctiontitle.setText(ElementsData[position][0]);
        holder.mainfunctiondes.setText(ElementsData[position][1]);
        holder.mainfunctionimg.setImageResource(resids[position]);
        return convertView;
    }
}
