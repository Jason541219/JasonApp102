package com.ideas.micro.jasonapp102;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpinnerArrayAdapter extends ArrayAdapter {
    private ArrayList<String> list = new ArrayList();

    public SpinnerArrayAdapter(@NonNull Context context, @NonNull ArrayList<String> list) {
        super(context, 0, list);
        this.list = list;
    }

    // getView 為尚未點開時的Spinner畫面
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getDropDownView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent){
        convertView = LayoutInflater.from(getContext()).inflate(//綁定介面
                R.layout.customer_spinner, parent, false);
        TextView tvName = convertView.findViewById(R.id.text_customer_spinner);//控制介面元件
        tvName.setText(list.get(position));
        return convertView;
    }//複寫介面
}
