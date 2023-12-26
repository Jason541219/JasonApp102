package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class Activity_Setting extends AppCompatActivity {
    private final String TAG = "設定";
    ListView listView;
    private TextView tvTitle;

    // 醫護人員設定功能頁
    public String[][] listdata_a;
    public int[] listids_a;

    // 一般用戶使用的主功能
    public String[][] listdata_m;
    public int[] listids_m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_setting_title);
        // 醫護人員沒有設定功能頁
        listdata_a =new String[][] {
                {getResources().getString(R.string.mainfunctiontitle_rememberaccount),getResources().getString(R.string.mainfunctiontdes_rememberaccount)},
                {getResources().getString(R.string.mainfunctiontitle_passquestion),getResources().getString(R.string.mainfunctiontdes_passquestion)},
                {getResources().getString(R.string.mainfunctiontitle_changepass),  getResources().getString(R.string.mainfunctiondes_changepass)}
        };
        listids_a = new int[] {
                R.drawable.main_notes,
                R.drawable.main_passquestion,
                R.drawable.main_changepass
        };

        // 一般用戶使用的主功能
        listdata_m =new String[][] {
//                {getResources().getString(R.string.mainfunctiontitle_me),  getResources().getString(R.string.mainfunctiondes_me)},
//                {getResources().getString(R.string.mainfunctiontitle_family),  getResources().getString(R.string.mainfunctiondes_family)},
//                {getResources().getString(R.string.mainfunctiontitle_alarm),  getResources().getString(R.string.mainfunctiondes_alarm)},
//                {getResources().getString(R.string.mainfunctiontitle_myqrcode),  getResources().getString(R.string.mainfunctiondes_myqrcode)},
//                {getResources().getString(R.string.mainfunctiontitle_rememberaccount),getResources().getString(R.string.mainfunctiontdes_rememberaccount)},
//                {getResources().getString(R.string.mainfunctiontitle_passquestion),getResources().getString(R.string.mainfunctiontdes_passquestion)},
                {getResources().getString(R.string.mainfunctiontitle_changepass),  getResources().getString(R.string.mainfunctiondes_changepass)}
        };
        listids_m = new int[] {
//                R.drawable.main_me,
//                R.drawable.main_family,
//                R.drawable.main_alarm,
//                R.drawable.main_myqrcode,
//                R.drawable.main_notes,
//                R.drawable.main_passquestion,
                R.drawable.main_changepass
        };
        //        // Get the Intent that started this activity and extract the string
        //        Intent intent = getIntent();
        //        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        //listView = (ListView) findViewById(R.id.list);
        //ListAdapter adapter = new ArrayAdapter<>(this , android.R.layout.simple_list_item_1 ,values);
        //listView.setAdapter(adapter);
        listView = (ListView) findViewById(R.id.list);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewAdapter_MainFunction adapter = null;
        if (GlobalVariables.Login_Role.equals("A")) {
            adapter = new ViewAdapter_MainFunction(listdata_a, listids_a, inflater);
        } else if (GlobalVariables.Login_Role.equals("M")) {
            adapter = new ViewAdapter_MainFunction(listdata_m, listids_m, inflater);
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onClickListView);       //指定事件 Method
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogInOut.log("Activity_Setting", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Activity_Setting", false);
    }

    /***
     * 點擊ListView事件Method
     */
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent;
            String selecteditem[] =(String[]) adapterView.getItemAtPosition(position);            //  取得功能名稱陣列
            switch(selecteditem[0]){
                case "關於我": //
                    intent = new Intent(Activity_Setting.this, Setting_Me.class);
                    startActivity(intent);
                    break;
                case "變更密碼": //
                    intent = new Intent(Activity_Setting.this, Setting_ChangePass.class);
                    startActivity(intent);
                    break;
                case "推播設定": // 設定
                    intent = new Intent(Activity_Setting.this, Setting_RequestPush.class);
                    startActivity(intent);
                    break;
                case "脈診提醒": // 設定
                    intent = new Intent(Activity_Setting.this, Setting_Alarm.class);
                    startActivity(intent);
                    break;
                case "二維條碼":
                    intent = new Intent(Activity_Setting.this, Setting_MyQRCode.class);
                    startActivity(intent);
                    break;
                case "密碼提示":
                    intent = new Intent(Activity_Setting.this, Setting_PassQuestion.class);
                    startActivity(intent);
                    break;
                case "記憶帳號":
                    intent = new Intent(Activity_Setting.this, Setting_RememberLogin.class);
                    startActivity(intent);
                    break;

            }
        }
    };
}