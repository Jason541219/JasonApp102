package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ideas.micro.jasonapp102.database.AppDatabase;
import com.ideas.micro.jasonapp102.database.MDJsonRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Activity_UploadData extends AppCompatActivity implements onHttpPostCallback {
    private final String TAG = "數據上傳";
    private TextView tvTitle;
    private TextView text_uploaddata;
    private ListView uploaddatalist;
    private ProgressBar progressBar_uploaddata;
    private Button btn_uploaddata, btn_deletedata;
    private String postKey = "";
    private long postrid;
    private boolean enableAutoRecord = false;       // 可以自動輸入虛擬資料
    private final SingleUser user =  SingleUser.getInstance();
    LayoutInflater inflater;
    ViewAdapter_UploadData listAdapter;
    JSONObject recordjson = new JSONObject();
    JSONArray recordList = new JSONArray();
    List<MDJsonRecord> dataBaseList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_uploaddata);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_upload_title);
        text_uploaddata = (TextView) findViewById(R.id.text_uploaddata);
        progressBar_uploaddata = (ProgressBar) findViewById(R.id.progressBar_uploaddata);
        progressBar_uploaddata.setVisibility(View.GONE);
        uploaddatalist = (ListView) findViewById(R.id.uploaddatalist);
        btn_deletedata = (Button) findViewById(R.id.btn_deletedata);
        btn_uploaddata = (Button) findViewById(R.id.btn_uploaddata);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        uploaddatalist.setOnItemClickListener(onClickListView);       //指定事件 Method
        // 取得
        new Thread(getUploadDataList).start();

        // 上傳
        btn_uploaddata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "recordList.length = " + recordList.length());
                if (recordList.length() > 0){
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar_uploaddata.setVisibility(View.VISIBLE);
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        });
                        uploadWave(recordList.getJSONObject(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // 此項動作測試用
        text_uploaddata.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (enableAutoRecord){          // 允許自動輸入虛擬資料
                    new Thread(insertRecord).start();       // 新增一個假紀錄
                }
            }
        });

        // 刪除資料
        btn_deletedata.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (dataBaseList.size() > 0) {
                    Utility_Alert.showAlertDialog(Activity_UploadData.this,
                            "刪除資料之後將無法復原。您確定要刪除這些資料？",
                            R.string.dialog_deletedata,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AppDatabase.getInstance(Activity_UploadData.this).getMDjsonrecordDao().delete();
                                            showUploadDataList();
                                        }
                                    }).start();

                                }
                            },
                            R.string.dialog_canceldelete,
                            Utility_Alert.doNothing
                    );

                }   // end of if dataBaseList > 0
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.menu_uploaddata, menu);
        return true;
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
            Log.e(TAG, "第" + position + "項被點選");
        }
    };

    // 將 上傳資料列表轉換為 JSONArray 用來提供 ViewAdapter 的內容
    private void getDBList(List<MDJsonRecord> list){
        recordList = new JSONArray();
        for (MDJsonRecord jrecord : list) {
            try {
                JSONObject json = new JSONObject();
                json.put("uid", jrecord.uid);
                json.put("record_json", new JSONObject(jrecord.mdjsonrecord));
                json.put("localrecordid", jrecord.mdrecordid);
//                Log.e(TAG, json.toString());
                recordList.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // 顯示上傳資料列表 dataBaseList
    private void showUploadDataList(){
        dataBaseList =
                AppDatabase.getInstance(Activity_UploadData.this).getMDjsonrecordDao().findAll();
            getDBList(dataBaseList); // 會產生 recordList
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listAdapter = new ViewAdapter_UploadData(recordList, inflater);
                    uploaddatalist.setAdapter(listAdapter);
                    uploaddatalist.invalidateViews();
                }
            });
    }

    private final Runnable getUploadDataList = new Runnable() {
        @Override
        public void run() {
            showUploadDataList();
            // 必須先執行完取得所有的資料才能檢查
            // 檢查是否有來自 MainFunction 的自動上傳需求
            Intent getintent = getIntent();
            // uploadnow 立刻上傳
            if (getintent.getBooleanExtra("uploadnow", false)) {
                Log.e(TAG, "UPLOAD NOW");
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar_uploaddata.setVisibility(View.VISIBLE);
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    });
                    uploadWave(recordList.getJSONObject(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

   // 上傳一筆資料
    private void uploadWave(JSONObject uploadbpwave){
        try {
//            Log.e(TAG, uploadbpwave.toString() );
            postKey = "UploadBPMWave_FromLocalDB";
            //            postrid = uploadbpwave.getLong("uid");
            //   uid 做為自動產生的 ID，難以追蹤。另外使用記錄在欄位中的 localrecordid 來追蹤
            postrid = uploadbpwave.getLong("localrecordid");
            JSONObject rjson = uploadbpwave.getJSONObject("record_json");
            rjson.put("command", postKey);      //  將指令由 UploadBPMWave_APP  ->  UploadBPMWave_FromLocalDB
            HttpPost httpPost = new HttpPost(Activity_UploadData.this);
            httpPost.startPost(Activity_UploadData.this, GlobalVariables.http_url,  rjson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    // 上傳下一筆資料
    private Runnable uploadNext = new Runnable() {
        @Override
        public void run() {
            // 刪除一筆已經上傳的資料:
            //    @Query("Delete FROM MDjsonrecord WHERE recordid = :uid")
            AppDatabase.getInstance(Activity_UploadData.this).getMDjsonrecordDao().deleteOneRecord(postrid);
            showUploadDataList();
            Log.e(TAG, "下一筆上傳 recordList.length = " + recordList.length());
            if (recordList.length() > 0){
                try {
                    uploadWave(recordList.getJSONObject(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {    // 全部上船完畢
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 關閉進度表
                        progressBar_uploaddata.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Utility_Alert.showAlertDialog(Activity_UploadData.this,
                                R.string.dialog_msg_uploadcomplete,
                                R.string.dialog_OK,
                                backToMainfunction);
                    }
                });
            }
        }
    };


    @Override
    public void onComplete(String response) {
        Log.e(TAG, response);
        if (postKey.equals("UploadBPMWave_FromLocalDB")){
            Log.e(TAG, "HttpPost OnComplete");
            new Thread(uploadNext).start();     // 上傳下一筆
        }
    }

    @Override
    public void onFail(String err) {
        Log.e(TAG, err);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar_uploaddata.setVisibility(View.GONE);
                // 恢復觸控功能
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    // 關閉進度條 ， 返回主功能頁
    private DialogInterface.OnClickListener backToMainfunction = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 關閉進度表
                    progressBar_uploaddata.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Intent intent = new Intent(Activity_UploadData.this, MainFunctionActivity.class);
                    startActivity(intent);
                }
            });
        }
    };

    // 新增一筆假資料
    private Runnable insertRecord = new Runnable() {
        @Override
        public void run() {
            long localrid = Calendar.getInstance().getTimeInMillis();
            JSONObject jqljson = new JSONObject();
            try {
                jqljson.put("command", "UploadBPMWave_FromLocalDB");
                jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
                jqljson.put("recordID", 1645696141954L);        //recordID 由伺服器產生 getTimeInMillis
                jqljson.put("sbp", 160);    // 收縮壓
                jqljson.put("dbp", 95);    // 舒張壓
                jqljson.put("hr", 85);    // 心跳
                jqljson.put("ihb", 0);
                jqljson.put("avatar", "");    // 頭像
                jqljson.put("name", "Name");
                jqljson.put("bpwave", "[]");
                jqljson.put("p1wave", "[]");
                jqljson.put("p2wave", "[]");
                jqljson.put("date", "2021-09-10");
                jqljson.put("time", "11:20:55");
                jqljson.put("position", 10);
                jqljson.put("posture", 20);
                jqljson.put("oxygen", 0);  //可能會改成波形
                jqljson.put("memberid", 1);
                jqljson.put("addr", "");
                jqljson.put("devicetype", "heshi_bt_v2");
                jqljson.put("measureAPP", "android");      // 引用的APP
                if (GlobalVariables.Login_Role.equals("A")) {
                    SingleAdmin _admin = SingleAdmin.getInstance();
                    jqljson.put("measureRole", "A");               // 量測模式為診所
                    jqljson.put("measuredBy", _admin.getAdminID()) ;                  // 實施量測的ID
                } else if (GlobalVariables.Login_Role.equals("M")) {
//                    SingleUser _user = SingleUser.getInstance();
                    jqljson.put("measureRole", "M");              // 量測模式為個人
                    jqljson.put("measuredBy", user.getUserID());                 // 實施量測的ID
                    jqljson.put("attending", user.getUserattending());
                    jqljson.put("fcmmessage", getResources().getString(R.string.dialog_msg_notifyattending));
                }
                // uid 自動產生
                MDJsonRecord mDrecord = new MDJsonRecord(jqljson.toString());
                mDrecord.setMdrecordid(localrid);
                AppDatabase.getInstance(Activity_UploadData.this).getMDjsonrecordDao().insertOneRecord(mDrecord);
                showUploadDataList();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


}