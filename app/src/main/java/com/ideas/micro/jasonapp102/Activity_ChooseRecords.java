package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

public class Activity_ChooseRecords extends AppCompatActivity {
    private TextView tvTitle;
    ListView listView;
    ImageView btn_linechart;
    ImageView btn_datatable;
    Intent intent;
    SingleUser user = SingleUser.getInstance();
    Utility_ActivityAlert activityAlert = new Utility_ActivityAlert(this);
    // 一般用戶使用的主功能
    private String[][] listdata_m;
    private int[] listids_m;
    private String record_datatable;
    private String record_datalinechart ;
    private String record_databarchart;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooserecords);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_chooserecords_title);
        record_datatable = getResources().getString(R.string.record_datatable);
        record_datalinechart = getResources().getString(R.string.record_datalinechart);
        record_databarchart = getResources().getString(R.string.record_databarchart);
//        btn_datatable = (ImageView) findViewById(R.id.btn_datatable);
//        btn_linechart = (ImageView) findViewById(R.id.btn_linechart);
        // 一般用戶使用的主功能
            listdata_m = new String[][]{
                    {getResources().getString(R.string.record_datatable), getResources().getString(R.string.recorddes_datatable)},
                    {getResources().getString(R.string.record_datalinechart), getResources().getString(R.string.recorddes_datalinechart)},
                    {getResources().getString(R.string.record_databarchart), getResources().getString(R.string.recorddes_databarchart)}
            };
            listids_m = new int[]{
                    R.drawable.data_table,
                    R.drawable.line_chart,
                    R.drawable.bar_chart
            };

        listView = (ListView) findViewById(R.id.chooserecordlist);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewAdapter_MainFunction adapter = null;
        adapter = new ViewAdapter_MainFunction(listdata_m, listids_m, inflater);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onClickListView);       //指定事件 Method
//        btn_linechart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent = new Intent(Activity_ChooseRecords.this, Activity_ChartHistory.class);
//                startActivity(intent);
//            }
//        });

//        btn_datatable.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent = new Intent(Activity_ChooseRecords.this, Activity_Myrecords.class);
//                startActivity(intent);
//            }
//        });
    }   // end of onCreate

    /***
     * 點擊ListView事件Method
     */
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent;
            String selecteditem[] =(String[]) adapterView.getItemAtPosition(position);            //  取得功能名稱陣列
            if (selecteditem[0].equals(record_datatable)) {
                intent = new Intent(Activity_ChooseRecords.this, Activity_Myrecords.class);
                startActivity(intent);
            } else if (selecteditem[0].equals(record_datalinechart)) {
                if (user.getIspayuser() < 200) {    // 50 借用  100 基礎型  150 基礎租賃型
                    activityAlert.showAlertDialog(R.string.dialog_msg_authenticationdeny, R.string.dialog_OK, activityAlert.doNothing);
                } else {
                    intent = new Intent(Activity_ChooseRecords.this, Activity_ChartHistory2.class);
                    startActivity(intent);
                }
            } else if (selecteditem[0].equals(record_databarchart)) {
                if (user.getIspayuser() < 200) {   // 50 借用  100 基礎型  150 基礎租賃型
                    activityAlert.showAlertDialog(R.string.dialog_msg_authenticationdeny, R.string.dialog_OK, activityAlert.doNothing);
                } else {
                    intent = new Intent(Activity_ChooseRecords.this, Activity_BarCompare.class);
                    startActivity(intent);
                }
            }

//            switch(selecteditem[0]){
//                case "歷史數據表":         // 脈診量測 -> 藍芽掃描
//                    intent = new Intent(Activity_ChooseRecords.this, Activity_Myrecords.class);
//                    startActivity(intent);
//                    break;
//                case "歷史折線圖":         // 脈診量測 -> 藍芽掃描
//                    if (user.getIspayuser() < 200) {    // 50 借用  100 基礎型  150 基礎租賃型
//                        activityAlert.showAlertDialog(R.string.dialog_msg_authenticationdeny, R.string.dialog_OK, activityAlert.doNothing);
//                    } else {
//                        intent = new Intent(Activity_ChooseRecords.this, Activity_ChartHistory2.class);
//                        startActivity(intent);
//                    }
//                    break;
//                case "前後測比較": //  讀取我的脈診紀錄
//                    if (user.getIspayuser() < 200) {   // 50 借用  100 基礎型  150 基礎租賃型
//                        activityAlert.showAlertDialog(R.string.dialog_msg_authenticationdeny, R.string.dialog_OK, activityAlert.doNothing);
//                    } else {
//                        intent = new Intent(Activity_ChooseRecords.this, Activity_BarCompare.class);
//                        startActivity(intent);
//                    }
//                    break;
//
//            }
        }
    };
}