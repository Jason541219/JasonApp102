package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ideas.micro.jasonapp102.database.AppDatabase;
import com.ideas.micro.jasonapp102.database.MDJsonRecord;
import com.nhi.mhbsdk.MHB;

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

//import com.nhi.mhbsdk.MHB;
//
//import net.lingala.zip4j.io.inputstream.ZipInputStream;
//import net.lingala.zip4j.model.LocalFileHeader;
//
//import javax.crypto.SecretKey;
//import javax.crypto.SecretKeyFactory;
//import javax.crypto.spec.PBEKeySpec;


public class MainFunctionActivity extends AppCompatActivity implements onHttpPostCallback{
    private final String TAG = "主功能頁";
    private String postkey = "";
    private TextView tvTitle;
    ListView listView;
    private SingleUser user = SingleUser.getInstance();
    Utility_ActivityAlert activityAlert;
    Helper_MHB helperMHB;
    private String measurement;
    private String upload ;
    private String cuffreset;
    private String customersn;
    private String dbswitch;
    private String myrecord;
    private String mysubscript;
    private String mygroup;
    private String myfolder;
    SharedPreferences sharedPreferences;

    // 醫護人員使用的主功能
    public String[][] listdata_a;
    public int[] listids_a;

    // 一般用戶使用的主功能
    public String[][] listdata_m;
    public int[] listids_m;

    // 檢查是否有預設藍芽
    public boolean hasBTconnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_function);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_mainfunction_title);
        activityAlert = new Utility_ActivityAlert(this);
        sharedPreferences = getSharedPreferences("meridianSharedPreferences", MODE_PRIVATE);
        checkMHBNotedDate();        // 檢查 MHB 的更新日期
        measurement = getResources().getString(R.string.mainfunctiontitle_bpm);
        upload = getResources().getString(R.string.mainfunctiontitle_upload);
        cuffreset = getResources().getString(R.string.mainfunctiontitle_cuffreset);
        customersn = "制定序號";
        dbswitch = getResources().getString(R.string.mainfunctiontitle_dbswitch);
        myrecord = getResources().getString(R.string.mainfunctiontitle_myrecords);
        mysubscript = getResources().getString(R.string.mainfunctiontitle_mysubscript);
        mygroup = getResources().getString(R.string.mainfunctiontitle_mygroup);
        myfolder = getResources().getString(R.string.mainfunctiontitle_myfolder);

        // 醫護人員使用的主功能
        if (GlobalVariables.Login_Clinic.equals("EChain")) {
            Log.e(TAG, GlobalVariables.Login_Clinic);
            listdata_a = new String[][]{
                    {getResources().getString(R.string.mainfunctiontitle_bpm), getResources().getString(R.string.mainfunctiondes_bpm)},
                    {getResources().getString(R.string.mainfunctiontitle_upload), getResources().getString(R.string.mainfunctiondes_upload)},
                    {getResources().getString(R.string.mainfunctiontitle_cuffreset), getResources().getString(R.string.mainfunctiondes_cuffreset)},
                    {getResources().getString(R.string.mainfunctiontitle_customersn), getResources().getString(R.string.mainfunctiondes_customersn)},
                    {getResources().getString(R.string.mainfunctiontitle_dbswitch), getResources().getString(R.string.mainfunctiondes_dbswitch)}

//                {getResources().getString(R.string.mainfunctiontitle_cut),  getResources().getString(R.string.mainfunctiondes_cut)},
//                {getResources().getString(R.string.mainfunctiontitle_setting),  getResources().getString(R.string.mainfunctiondes_setting)}
            };
            listids_a = new int[]{
                    R.drawable.main_pulse,
                    R.drawable.main_upload,
                    R.drawable.main_cuffreset,
                    R.drawable.main_rocket,
                    R.drawable.main_dbswitch
//                R.drawable.main_cut,
//                R.drawable.main_settings
            };
            // 一般用戶使用的主功能
            listdata_m = new String[][]{
                    {getResources().getString(R.string.mainfunctiontitle_bpm), getResources().getString(R.string.mainfunctiondes_bpm)},
                    {getResources().getString(R.string.mainfunctiontitle_myrecords), getResources().getString(R.string.mainfunctiondes_myrecords)},
                    {getResources().getString(R.string.mainfunctiontitle_mygroup), getResources().getString(R.string.mainfunctiondes_mygroup)},
                    {getResources().getString(R.string.mainfunctiontitle_mysubscript), getResources().getString(R.string.mainfunctiondes_mysubscript)},
                    {getResources().getString(R.string.mainfunctiontitle_myfolder), getResources().getString(R.string.mainfunctiondes_myfolder)}
//                {getResources().getString(R.string.mainfunctiontitle_payment), getResources().getString(R.string.mainfunctiondes_payment)},
//                {getResources().getString(R.string.mainfunctiontitle_reservation),  getResources().getString(R.string.mainfunctiondes_reservation)},
//                {getResources().getString(R.string.mainfunctiontitle_setting), getResources().getString(R.string.mainfunctiondes_setting)}
            };
            listids_m = new int[]{
                    R.drawable.main_pulse,
                    R.drawable.main_myrecord,
                    R.drawable.main_group,
                    R.drawable.main_subscript,
                    R.drawable.main_folder
//                R.drawable.main_payment,
//                R.drawable.main_calendar,
//                R.drawable.main_settings
            };


        } else {
            listdata_a = new String[][]{
                    {getResources().getString(R.string.mainfunctiontitle_bpm), getResources().getString(R.string.mainfunctiondes_bpm)},
                    {getResources().getString(R.string.mainfunctiontitle_upload), getResources().getString(R.string.mainfunctiondes_upload)}
            };
            listids_a = new int[]{
                    R.drawable.main_pulse,
                    R.drawable.main_upload
            };
            // 一般用戶使用的主功能
            listdata_m = new String[][]{
                    {getResources().getString(R.string.mainfunctiontitle_bpm), getResources().getString(R.string.mainfunctiondes_bpm)},
                    {getResources().getString(R.string.mainfunctiontitle_myrecords), getResources().getString(R.string.mainfunctiondes_myrecords)},
                    {getResources().getString(R.string.mainfunctiontitle_mygroup), getResources().getString(R.string.mainfunctiondes_mygroup)},
                    {getResources().getString(R.string.mainfunctiontitle_mysubscript), getResources().getString(R.string.mainfunctiondes_mysubscript)},
                    {getResources().getString(R.string.mainfunctiontitle_myfolder), getResources().getString(R.string.mainfunctiondes_myfolder)}
//                {getResources().getString(R.string.mainfunctiontitle_payment), getResources().getString(R.string.mainfunctiondes_payment)},
//                {getResources().getString(R.string.mainfunctiontitle_reservation),  getResources().getString(R.string.mainfunctiondes_reservation)},
//                {getResources().getString(R.string.mainfunctiontitle_setting), getResources().getString(R.string.mainfunctiondes_setting)}
            };
            listids_m = new int[]{
                    R.drawable.main_pulse,
                    R.drawable.main_myrecord,
                    R.drawable.main_group,
                    R.drawable.main_subscript,
                    R.drawable.main_folder
//                R.drawable.main_payment,
//                R.drawable.main_calendar,
//                R.drawable.main_settings
            };
        }

        //        // Get the Intent that started this activity and extract the string


        //        // Get the Intent that started this activity and extract the string
        //        Intent intent = getIntent();
        //        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        //listView = (ListView) findViewById(R.id.list);
        //ListAdapter adapter = new ArrayAdapter<>(this , android.R.layout.simple_list_item_1 ,values);
        //listView.setAdapter(adapter);

        SharedPreferences msp = getSharedPreferences("meridianSharedPreferences", MODE_PRIVATE);
        hasBTconnected = !msp.getString("btdeviceaddress", "").equals(""); // 預設為空字串
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
        if (GlobalVariables.onLine) {    // 連線時詢問是否要上傳
//            new Thread(getUploadDataList).start();
        }


    }       // end of onCreate

    @Override
    protected void onResume() {
        super.onResume();
        if (helperMHB != null) helperMHB.parseMHBFileTicket();
//        parseMHBFileTicket();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogInOut.log("MainFunctionActivity", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("MainFunctionActivity", false);
    }

    private void checkMHBNotedDate() {
        if (GlobalVariables.Login_Role.equals("M")) {
            if (GlobalVariables.isMHBonline &&                // MHB 上線後才執行
                    (user.getMHBNotedDate().equals("") || Utility.isTodayLaterThan(user.getMHBNotedDate()))) {      // 尚未同意MHB 或是 超過MHB同意存取 90 日
                Log.e(TAG, "紀錄下一次檢查MHB的日期 " + Utility.getDateAfter(GlobalVariables.nextMHBdays));         // nextMHBdays = 90;
                recordMHBNotedDate(); // 紀錄
                activityAlert.showAlertDialog(R.string.dialog_msg_mhbaccessnoted,
                        R.string.dialog_IAgree, goMHB,
                        R.string.dialog_NextTime, activityAlert.doNothing);
            }
        }
    }

    // 前往主功能頁
    private DialogInterface.OnClickListener goMHB = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            helperMHB = new Helper_MHB(MainFunctionActivity.this);
        }
    } ;

    // 記錄下一次詢問的日期
    private void recordMHBNotedDate(){
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command", "RecordMHBNotedDate");
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            jqljson.put("mid", user.getUserID());
            jqljson.put("date", Utility.getDateAfter(GlobalVariables.nextMHBdays));      // 紀錄 90 天之後的日期
            Log.e(TAG, "90天後日期 = " + Utility.getDateAfter(GlobalVariables.nextMHBdays));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postkey = "RecordMHBNotedDate";
        HttpPost httpPost = new HttpPost(MainFunctionActivity.this);
        httpPost.startPost(MainFunctionActivity.this,  GlobalVariables.http_url, jqljson.toString());
    }

    JSONArray recordList = new JSONArray();
    List<MDJsonRecord> dataBaseList = null;

    private Runnable getUploadDataList = new Runnable() {
        @Override
        public void run() {
            dataBaseList =
                    AppDatabase.getInstance(MainFunctionActivity.this).getMDjsonrecordDao().findAll();
            Log.e(TAG, "getUploadDataList.size = " + dataBaseList.size());
//            recordList = getDBList(dataBaseList);
            if (dataBaseList.size() > 0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utility_Alert.showAlertDialog(MainFunctionActivity.this,
                                R.string.dialog_msg_askuploaddata,
                                R.string.dialog_uploaddata,
                                uploaddata,
                                R.string.dialog_uploadlater,
                                Utility_Alert.doNothing);
                    }
                });
            }
        }
    };

    // 上傳資料
    private DialogInterface.OnClickListener uploaddata = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(MainFunctionActivity.this, Activity_UploadData.class);
            intent.putExtra("uploadnow", true);
            startActivity(intent);
        }
    };

    /***
     * 點擊ListView事件Method
     */
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
//        final String measurement = getResources().getString(R.string.mainfunctiontitle_bpm);

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            Intent intent;
            String selecteditem[] =(String[]) adapterView.getItemAtPosition(position);            //  取得功能名稱陣列
            if (selecteditem[0].equals(measurement)) {
                intent = new Intent(MainFunctionActivity.this, Activity_ScanBLE.class);
                intent.putExtra("nextstep", "bpmprepare");      // 掃描以後進入 BPMPrepare
                startActivity(intent);
            } else if (selecteditem[0].equals(upload)) {
                if (GlobalVariables.onLine) {    // 連線時才能處理數據上傳作業
                    intent = new Intent(MainFunctionActivity.this, Activity_UploadData.class);
                    startActivity(intent);
                } else {
                    Utility_Alert.showAlertDialog(MainFunctionActivity.this,
                            R.string.dialog_msg_unableuploadinofflinemode,
                            R.string.dialog_OK,
                            Utility_Alert.doNothing);
                }
            } else if (selecteditem[0].equals(cuffreset)) {
                intent = new Intent(MainFunctionActivity.this, Activity_ScanBLE.class);
                intent.putExtra("nextstep", "cuffreset");       // 掃描以後進入 CuffReset
                startActivity(intent);
            } else if (selecteditem[0].equals(customersn)) {
                intent = new Intent(MainFunctionActivity.this, Activity_ScanBLE.class);
                intent.putExtra("nextstep", "customersn");       // 掃描以後進入 CuffReset
                startActivity(intent);
            } else if (selecteditem[0].equals(dbswitch)) {
                switch_DB();
            }  else if (selecteditem[0].equals(myrecord)) {
                intent = new Intent(MainFunctionActivity.this, Activity_ChooseRecords.class);
                startActivity(intent);
            }  else if (selecteditem[0].equals(mygroup)) {
                if (user.getIsgroupprimary() == 0){     // ***** 沒有權限使用我的群組
                    activityAlert.showAlertDialog(R.string.dialog_msg_mygroupnoauthorization, R.string.dialog_OK, activityAlert.doNothing);
                } else {
                    intent = new Intent(MainFunctionActivity.this, Activity_Mygroup.class);
                    startActivity(intent);
                }
            } else  if (selecteditem[0].equals(mysubscript)) {
                // 根據訂閱報告日期截止日確認是否有訂閱報告
                if (user.getReportSubscriptE().equals("")) {  // 沒有訂閱報告截止日  -> 健康存摺同意書
                    // 前往同意書頁面
                    Log.e(TAG, "訂閱報告 - > 健康存摺同意書");
                    intent = new Intent(MainFunctionActivity.this, Activity_MHBagreement.class);
                    startActivity(intent);
                } else {    // 有訂閱日期紀錄
                    SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date subscript_end = spf.parse(user.getReportSubscriptE());
                        Date thisdate = new Date();
                        if (subscript_end.compareTo(thisdate) < 0) {    // 訂閱已經過期
                            activityAlert.showAlertDialog(R.string.dialog_msg_reportsubscriptexpired,
                                    R.string.dialog_OK, continuesubscribe,
                                    R.string.dialog_Cancel, activityAlert.doNothing);
                        } else {
                            String requestSubscribe = getRequestSubscribe(user.getReportSubscriptE());
                            activityAlert.showAlertDialog(requestSubscribe,
                                    R.string.dialog_OK, activityAlert.doNothing);
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "Date Parsing Error " + e.toString());
                        e.printStackTrace();
                    }
                }
            } else if (selecteditem[0].equals(myfolder)) {
                helperMHB = new Helper_MHB(MainFunctionActivity.this);
            }

//            switch(selecteditem[0]){
//                case  "血壓波量測":         // 脈診量測 -> 藍芽掃描
////                    if (hasBTconnected) {
////                        intent = new Intent(MainFunctionActivity.this, Activity_BPMprepare.class);
////                    } else {
////                        intent = new Intent(MainFunctionActivity.this, Activity_ScanBLE.class);
////                    }
//                    intent = new Intent(MainFunctionActivity.this, Activity_ScanBLE.class);
//                    intent.putExtra("nextstep", "bpmprepare");      // 掃描以後進入 BPMPrepare
//                    startActivity(intent);
//                    break;
//                case "數據上傳": case "Upload":       // 脈診量測 -> 藍芽掃描
//                    if (GlobalVariables.onLine) {    // 連線時才能處理數據上傳作業
//                        intent = new Intent(MainFunctionActivity.this, Activity_UploadData.class);
//                        startActivity(intent);
//                    } else {
//                        Utility_Alert.showAlertDialog(MainFunctionActivity.this,
//                                R.string.dialog_msg_unableuploadinofflinemode,
//                                R.string.dialog_OK,
//                                Utility_Alert.doNothing);
//                    }
//                    break;
//                case "壓脈帶更新": case "Cuff Reset":
////                    if (hasBTconnected) {
////                        intent = new Intent(MainFunctionActivity.this, Activity_BPMprepare.class);
////                    } else {
////                        intent = new Intent(MainFunctionActivity.this, Activity_ScanBLE.class);
////                    }
//                    intent = new Intent(MainFunctionActivity.this, Activity_ScanBLE.class);
//                    intent.putExtra("nextstep", "cuffreset");       // 掃描以後進入 CuffReset
//                    startActivity(intent);
//                    break;
//                case "血壓波紀錄": //  讀取我的脈診紀錄
//                    intent = new Intent(MainFunctionActivity.this, Activity_ChooseRecords.class);
//                    startActivity(intent);
//                    break;
////                case "線上金流": //
////                    intent = new Intent(MainFunctionActivity.this, Activity_PayOrder.class);
////                    startActivity(intent);
////                    URL url = null;
////                    try {
////                        String pid = GlobalVariables.Login_Role.equals("A")?SingleAdmin.getInstance().getAdminpid():SingleUser.getInstance().getUserpid();
////                        url = new URL( "http://192.168.0.129:8080/EChainMed/payment/payment_app.jsp?pid=" + pid);
////                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(java.lang.String.valueOf(url)));
////                        startActivity(browserIntent);
////                    } catch (MalformedURLException e) {
////                        e.printStackTrace();
////                    }
////                    break;
//                case "變更密碼": //
//                    intent = new Intent(MainFunctionActivity.this, Setting_ChangePass.class);
//                    startActivity(intent);
//                    break;
//                case "我的群組": // 設定
//                    intent = new Intent(MainFunctionActivity.this, Activity_Mygroup.class);
//                    startActivity(intent);
//                    break;
//                case "個人設定": // 設定
//                    intent = new Intent(MainFunctionActivity.this, Activity_Setting.class);
//                    startActivity(intent);
//                    break;
//
//                case "訂閱報告": // 訂閱報告
//                    // 根據訂閱報告日期截止日確認是否有訂閱報告
//                    if (user.getReportSubscriptE().equals("")) {  // 沒有訂閱報告截止日  -> 健康存摺同意書
//                        // 前往同意書頁面
//                        Log.e(TAG, "訂閱報告 - > 健康存摺同意書");
//                        intent = new Intent(MainFunctionActivity.this, Activity_MHBagreement.class);
//                        startActivity(intent);
//                    } else {    // 有訂閱日期紀錄
//                        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd");
//                        try {
//                            Date subscript_end = spf.parse(user.getReportSubscriptE());
//                            Date thisdate = new Date();
//                            if (subscript_end.compareTo(thisdate) < 0) {    // 訂閱已經過期
//                                activityAlert.showAlertDialog(R.string.dialog_msg_reportsubscriptexpired,
//                                        R.string.dialog_OK, continuesubscribe,
//                                        R.string.dialog_Cancel, activityAlert.doNothing);
//                            } else {
//                                String requestSubscribe = getRequestSubscribe(user.getReportSubscriptE());
//                                activityAlert.showAlertDialog(requestSubscribe,
//                                        R.string.dialog_OK, activityAlert.doNothing);
//                            }
//                        } catch (ParseException e) {
//                            Log.e(TAG, "Date Parsing Error " + e.toString());
//                            e.printStackTrace();
//                        }
//                    }
//                    break;
//                case "健康存摺": // 設定
//                    helperMHB = new Helper_MHB(MainFunctionActivity.this);
////                    helperMHB.getApiKey();
////                    activityAlert.showAlertDialog(R.string.dialog_msg_goingtomhb,
////                                R.string.dialog_OK, helperMHB.mhb_launch,
////                                R.string.dialog_Cancel, activityAlert.doNothing);
//                    break;
//                default:
//                    break;
//            }
        }
    };


    private String getRequestSubscribe(String subend){
        String requeststr = getResources().getString(R.string.dialog_msg_prereportsubscript1) +
                "【" + subend + "】" +
                getResources().getString(R.string.dialog_msg_prereportsubscript2);
        return  requeststr;
    }

    // 續訂
    private DialogInterface.OnClickListener continuesubscribe = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (!user.getUsermhbagreement().equals("")) {   // 已經同意健康存摺存取
                String cid = GlobalVariables.Login_Clinic;
                String pid = Utility_AES.Encrypt(user.getUserpid());     // 加密
                String uri_subscript = "";
                uri_subscript = GlobalVariables.subscript_url + "cid=" + cid + "&pid=" + pid;
                //                        uri_subscript =  GlobalVariables.subscript_url  + "cid=100003&pid=" + pid;
                Log.e(TAG, uri_subscript);
                Intent go2subscript = new Intent(Intent.ACTION_VIEW, Uri.parse(uri_subscript));
                startActivity(go2subscript);
            }
        }
    };


    @Override
    public void onComplete(String response) {

    }

    @Override
    public void onFail(String err) {

    }

    private void switch_DB(){
        String dbdomain = sharedPreferences.getString("dbdomain", "www");
        if (dbdomain.equals("www")) {
            dbdomain = "test";
        } else if (dbdomain.equals("test")) {
            dbdomain = "www";
        }
        sharedPreferences.edit()
                .putString("dbdomain", dbdomain)
                .commit();
    }
}