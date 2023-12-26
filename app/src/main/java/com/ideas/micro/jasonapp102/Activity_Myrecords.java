package com.ideas.micro.jasonapp102;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Activity_Myrecords extends AppCompatActivity implements onHttpPostCallback {
    private final String TAG = "我的紀錄";
    private RadioButton recordrange3month;
    private RadioButton recordrange12month;
    private RadioGroup radiogroup_recordrange;
    private TextView tvTitle;
    private ListView listView;
    private ProgressBar progressBar_myrecord;
    private final SingleUser user =  SingleUser.getInstance();
    LayoutInflater inflater;
    ViewAdapter_Myrecords listAdapter;
    JSONArray recordlist = new JSONArray();
    String recordliststr = "{\"recordlist\":[{\"time\":\"2020/07/30\",\"sbp\":140, \"dbp\":90, \"hr\":74,\"level\":0,\"des\":\"OK\"},            {\"time\":\"2020/07/29\",\"sbp\":136, \"dbp\":86, \"hr\":74,\"level\":1,\"des\":\"OK\"},            {\"time\":\"2020/07/28\",\"sbp\":142, \"dbp\":88, \"hr\":74,\"level\":1,\"des\":\"OK\"},            {\"time\":\"2020/07/27\",\"sbp\":132, \"dbp\":92, \"hr\":74,\"level\":0,\"des\":\"OK\"},            {\"time\":\"2020/07/26\",\"sbp\":130, \"dbp\":85, \"hr\":74,\"level\":2,\"des\":\"OK\"},            {\"time\":\"2020/07/25\",\"sbp\":138, \"dbp\":90, \"hr\":74,\"level\":0,\"des\":\"OK\"},            {\"time\":\"2020/07/24\",\"sbp\":142, \"dbp\":88, \"hr\":74,\"level\":1,\"des\":\"OK\"},            {\"time\":\"2020/07/23\",\"sbp\":140, \"dbp\":92, \"hr\":74,\"level\":2,\"des\":\"OK\"},            {\"time\":\"2020/07/22\",\"sbp\":138, \"dbp\":94, \"hr\":74,\"level\":0,\"des\":\"OK\"},                    {\"time\":\"2020/07/21\",\"sbp\":140, \"dbp\":82, \"hr\":74,\"level\":0,\"des\":\"OK\"},            {\"time\":\"2020/07/20\",\"sbp\":136, \"dbp\":85, \"hr\":74,\"level\":1,\"des\":\"OK\"},            {\"time\":\"2020/07/19\",\"sbp\":132, \"dbp\":86, \"hr\":74,\"level\":1,\"des\":\"OK\"},            {\"time\":\"2020/07/18\",\"sbp\":136, \"dbp\":92, \"hr\":74,\"level\":2,\"des\":\"OK\"},            {\"time\":\"2020/07/17\",\"sbp\":142, \"dbp\":88, \"hr\":74,\"level\":0,\"des\":\"OK\"},            {\"time\":\"2020/07/16\",\"sbp\":140, \"dbp\":87, \"hr\":74,\"level\":2,\"des\":\"OK\"},            {\"time\":\"2020/07/15\",\"sbp\":140, \"dbp\":85, \"hr\":74,\"level\":2,\"des\":\"OK\"},            {\"time\":\"2020/07/14\",\"sbp\":140, \"dbp\":90, \"hr\":74,\"level\":0,\"des\":\"OK\"},            {\"time\":\"2020/07/13\",\"sbp\":140, \"dbp\":90, \"hr\":74,\"level\":0,\"des\":\"OK\"},            {\"time\":\"2020/07/12\",\"sbp\":140, \"dbp\":90, \"hr\":74,\"level\":0,\"des\":\"OK\"}]}";


    private void setListData(JSONArray dblist, JSONObject lightguide) {
        recordlist = dblist;
        listAdapter = new ViewAdapter_Myrecords(recordlist, lightguide, inflater);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_myrecords);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_myrecords_title);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
//        radiogroup_recordrange = (RadioGroup) findViewById(R.id.radiogroup_recordrange);
        progressBar_myrecord = (ProgressBar) findViewById(R.id.progressBar_myrecord);
        progressBar_myrecord.setVisibility(View.GONE);
        listView = (ListView) findViewById(R.id.myrecordslist);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        getMyRecordList();  // 查詢 (預設三個月內)
        listView.setOnItemClickListener(onClickListView);       //指定事件 Method
//        radiogroup_recordrange.setOnCheckedChangeListener(onCheckedChangeListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_myrecords, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.mylist_3month:
                recordrange_month = -3;
            break;
            case R.id.mylist_12month:
                recordrange_month = -12;
            break;
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    protected void onStart() {
        super.onStart();
        LogInOut.log("Activity_Myrecords", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Activity_Myrecords", false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    /***
     * 點擊ListView事件Method
     */
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            //Toast.makeText(MainFunctionActivity.this,"點選第 "+(position +1) +" 個 \n內容："+listdata[position][1], Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(Activity_Myrecords.this, Activity_ChartWave.class);
                long recordid = recordlist.getJSONObject(position).getLong("rid");
                Log.e(TAG, "recordid " + recordid);
                int hasread = recordlist.getJSONObject(position).getInt("hasread");
                Log.e(TAG, "hasread " + hasread);
                intent.putExtra("recordID", recordid);
                intent.putExtra("hasread", hasread);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 變更選取區間
    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            getMyRecordList();      // 查詢
        }
    };

    private String postKey;
    private int recordrange_month = -3;

    private void getMyRecordList(){
        int userid = user.getUserID();
        Log.e(TAG, "USERName = " + user.getUsername() + "  USER ID = " + user.getUserID());
        JSONObject jqljson = new JSONObject();
//        switch (radiogroup_recordrange.getCheckedRadioButtonId()) {
//            case R.id.recordrange3month:
//                recordrange_month = -3;
//                break;
//            case R.id.recordrange12month:
//                recordrange_month = -12;
//                break;
//        }
        Calendar calendar = Calendar.getInstance();
        String DateTimeNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date(calendar.getTimeInMillis()));
        calendar.add(Calendar.MONTH, recordrange_month);
        String DateTimeBefore =  new SimpleDateFormat("yyyy-MM-dd").format(new Date(calendar.getTimeInMillis()));
        Log.e(TAG, "診所名稱 " + GlobalVariables.Login_Clinic);
        try {
            // 只選擇 fromID = 0 的紀錄
            Log.e(TAG, "GlobalVariables.isHxLightEnable = " + GlobalVariables.isHxLightEnable);
            if (GlobalVariables.isHxLightEnable) {  // 為了顯示 Hx 燈號
                jqljson.put("command", "GetBPMWaveRecordListByMemFFT_APP");
            } else {
                jqljson.put("command", "GetBPMWaveRecordListByMem_APP");
            }
            jqljson.put("mid",  userid);
            jqljson.put("sdate", DateTimeBefore);
            jqljson.put("edate", DateTimeNow);
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            Log.e(TAG, jqljson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "紀錄期間 = " + DateTimeBefore + " 到 " + DateTimeNow);
        postKey = "GetBPMWaveRecordListByMem_APP";
        progressBar_myrecord.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        HttpPost httpPost = new HttpPost(Activity_Myrecords.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_Myrecords.this, GlobalVariables.http_url,  jqljson.toString());
    }

    @Override
    public void onComplete(String response) {
//        Log.e(TAG, response);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar_myrecord.setVisibility(View.GONE);
                // 恢復觸控功能
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
        try {
            final JSONObject responseJson = new JSONObject(response);
            if (responseJson.getString("status").equals("success")){      //
                final JSONArray dblist = responseJson.getJSONArray("dblist");
                Log.e(TAG, "lightguidestr = " + responseJson.getString("lightguide"));
                final JSONObject lightguide = new JSONObject(responseJson.getString("lightguide"));
                // 設定下拉式選單
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setListData(dblist, lightguide);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Myrecords.this);
                        String msg = null;
                        msg = getResources().getString(R.string.dialog_msg_norecorddatafountin3month);

                        builder.setMessage(msg)
                                .setTitle(R.string.dialog_systemcomment)
                                .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // 不需要做事情
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onFail(String err) {
        Log.e(TAG, err);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar_myrecord.setVisibility(View.GONE);
                // 恢復觸控功能
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
        try {
            final JSONObject failJson = new JSONObject(err);
            if (failJson.getString("status").equals("exception")){      // notfound 表示沒有找到對應的帳號
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Myrecords.this);
                        String msg = null;
                        try {
                            msg = getResources().getString(R.string.dialog_msg_systemerror) + "\n" +  failJson.getString("result");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        builder.setMessage(msg)
                                .setTitle(R.string.dialog_systemcomment)
                                .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // 不需要做事情
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}