package com.ideas.micro.jasonapp102;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nhi.mhbsdk.MHB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements onHttpPostCallback{
    private final String TAG = "Activity_Main";
    Button btnlogina;
//    Button btnloginp;     // 目前不用
    EditText username;
    EditText userpass;
    EditText clinic;
    private RadioButton radio_roleuser, radio_roleadmin;
    private RadioGroup radio_role;
    TextView txtregisteraccount;     // 目前不用
    TextView txtforgetpass;     // 目前不用
    TextView label_maintitlecompany;
    TextView label_version;
    ImageView img_eyepass, img_eyeaccount;
    CheckBox chk_rememberme;
    private TextView tvTitle;
    long appversioncode;
    boolean isLastestVersion = false;       // 要透過檢查變更
    boolean isFCMcompleted = false;
    private boolean isAutoFill = true;
    ProgressBar progressBar_main;
    String fcmToken = "";
    private String postkey = "";
    Context main_context;
    SharedPreferences sharedPreferences;
    long appversion = 0;
    private Utility_ActivityAlert activityAlert = new Utility_ActivityAlert(MainActivity.this);
    SingleUser user = SingleUser.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        main_context = MainActivity.this;
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_login_title);
        GlobalVariables.onLine = true;      // 預設為連線模式
        seetUIComponents();
        // *********** FCM ***************************************************//
        FirebaseMessaging.getInstance().subscribeToTopic("meridian");
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful())return;
                fcmToken = task.getResult();
                isFCMcompleted = ! fcmToken.equals("");
                if (! isFCMcompleted)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 您的設備不支援推播服務，無法取得Token。請關閉APP。
                        activityAlert.showAlertDialog(R.string.dialog_msg_fcmtokenunknown, R.string.dialog_OK, activityAlert.doNothing);
                    }
                });
                btnlogina.setEnabled(isFCMcompleted && isLastestVersion);
                Log.e(TAG, "推播 onComplete: "+fcmToken + " isFCMCompleted " + isFCMcompleted + " isLastestVersion" + isLastestVersion);
                // 要記錄到 memberfcmToken
            }
        });
        // *********** FCM ***************************************************//

        Stetho.initializeWithDefaults(this);//設置資料庫監視
        sharedPreferences = getSharedPreferences("meridianSharedPreferences", MODE_PRIVATE);
        // 禁止自動填入密碼
        // 禁止自動填入帳號
        clinic.setText(sharedPreferences.getString("clinic", ""));      // 自動填入診所   預設空字串
        setRole(sharedPreferences.getString("role", "M"));              //  自動填入角色    預設用戶

        try {       // 版次管理
            PackageManager pm = this.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(this.getPackageName(), 0);
            String versionname = pInfo.versionName;
            appversioncode = pInfo.getLongVersionCode();      // 數字 (必須 API  28 以上)
            label_version.setText("version " + versionname);            //   填入系統版次字串 1.0.60
            GlobalVariables.appversion = String.valueOf(appversioncode);
            getAESKey();    // -> checkAPPVersion
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // 登入
        btnlogina.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                GlobalVariables.Login_Role = getRole();     // 取得角色  A / M\
                if (isuserdataempty()) {
                    // do nothing
                } else if (username.getText().toString().equals("guest") && userpass.getText().toString().equals("guest")
                                && GlobalVariables.Login_Role.equals("M")) {
                            Log.e(TAG, "Guest 登入的腳色 " + GlobalVariables.Login_Role);
                            SingleUser user = SingleUser.getInstance();
                            user.setUserjson(user.getGuestJsonString());
                            GlobalVariables.Login_Clinic = clinic.getText().toString();
                            GlobalVariables.isBindMAC = false;     // Guest 不需要綁定MAC
                            // guest 不需要考慮隱私權
                                Intent intent = new Intent(MainActivity.this, Activity_ScanBLE.class);
                                intent.putExtra("nextstep", "bpmprepare");     // 掃描之後的下一步
                                startActivity(intent);      // 在MainFunctionActivity 中 依Global Variable LoginRole 設定
                } else {
                    if (GlobalVariables.onLine) {       // 連線設定時 到伺服器驗證
                        String httpenccommand = getRole().equals("A") ? "GetEncAdminAuthorization_APP" : "GetEncMemberAuthorization_APP";
                        String clinicnamewi = getRole().equals("A") ? "RootDB" : clinic.getText().toString();
                        JSONObject jqljson = new JSONObject();
                        try {
//                        g.setUsrPas(username.getText().toString(), userpass.getText().toString());
                            jqljson.put("command", httpenccommand);
                            jqljson.put("versionTag", "1.1.0");
                            jqljson.put("clinicnamewi", clinicnamewi);
                            jqljson.put("clinicDBname", clinic.getText().toString());
                            jqljson.put("account", Utility_AES.Encrypt(username.getText().toString()));
                            jqljson.put("pass", Utility_AES.Encrypt(userpass.getText().toString()));
                            jqljson.put("token", fcmToken);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        postkey = "GetAuthorization";
                        progressBar_main.setVisibility(View.VISIBLE);
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        HttpPost httpPost = new HttpPost(MainActivity.this);
                        httpPost.startPost(MainActivity.this, GlobalVariables.http_url,  jqljson.toString());
                    } else {    // 離線時檢查
//                        Intent intentqr = new Intent(MainActivity.this, Activity_ScanQR.class);
//                        startActivity(intentqr);
                        CheckAuthorizationOffLine();
                    }
                }
            }
        });

        img_eyepass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        Log.e(TAG, "ACTION _ DOWN");
                        userpass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
//                        Log.e(TAG, "ACTION _ UP");
                        userpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
            }
        });
        img_eyeaccount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        Log.e(TAG, "ACTION _ DOWN");
                        username.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
//                        Log.e(TAG, "ACTION _ UP");
                        username.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
            }
        });

        txtregisteraccount.setVisibility(View.INVISIBLE);               // 隱藏註冊帳號功能
        // 註冊帳號 先詢問是否已經有帳號了
        txtregisteraccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // 如果您曾經註冊或是已經有診所帳號，請勿再重複註冊。
                activityAlert.showAlertDialog(R.string.dialog_msg_checkHasAccount, R.string.dialog_gotoregister,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, Activity_Privacy.class);
                                intent.putExtra("type", "newRegister");     // 第一次註冊 沒有帳號的情況
                                startActivity(intent);
                            }
                        }, R.string.dialog_Cancel, activityAlert.doNothing);
            }
        });

        // 忘記密碼
        txtforgetpass.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Activity_Forgetpass.class);
                startActivity(intent);
            }
        });

        label_version.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (! isLastestVersion) {      // 如果不是最新版 就開啟連結
                    Intent updateintent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.ideas.micro.jasonapp102"));
                    startActivity(updateintent);
                }
                return false;
            }
        });

        // 自動輸入登入資料
        label_maintitlecompany.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (isAutoFill) {
                    if (GlobalVariables.onLine) {
                        if (getRole().equals("A")){
                            clinic.setText("EChain");
                            username.setText("int000");
                            userpass.setText("12345678");
                        } else {
//                            clinic.setText("EChain");
//                            username.setText("A123456787");
//                            userpass.setText("19700101");
//                            clinic.setText("當代漢醫");
//                            username.setText("E200511292");
//                            userpass.setText("19561226");
                        }
                    } else {        // 斷線

                    }
                }
                return false;
            }
        });

    }   // end of  onCreate

    // 設定離線連線選項
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    // 選取連線或離線模式
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.login_online:
                GlobalVariables.onLine = true;
                break;
            case R.id.login_offline:
                GlobalVariables.onLine = false;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindMAC(int mid, String mac){
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command", "BindMAC");
            jqljson.put("clinicnamewi", clinic.getText());
            jqljson.put("mid", mid);
            jqljson.put("mac", mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postkey = "BindMAC";
        HttpPost httpPost = new HttpPost(MainActivity.this);
        httpPost.startPost(MainActivity.this, GlobalVariables.http_url,  jqljson.toString());
    }

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
        HttpPost httpPost = new HttpPost(MainActivity.this);
        httpPost.startPost(MainActivity.this,  GlobalVariables.http_url, jqljson.toString());
    }

    private void getAESKey(){
        GlobalVariables.isEnclypted = false;
        postkey = "GetAESKey";
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        HttpPost httpPost = new HttpPost(MainActivity.this);
        httpPost.startPost(MainActivity.this, GlobalVariables.http_url,  "GetTransCode");
    }

    private void checkAppVersion(long versioncode) {
        JSONObject jqljson = new JSONObject();
        Log.e(TAG, GlobalVariables.http_url);
//        GlobalVariables.http_url = GlobalVariables.main_url;
        try {
            jqljson.put("command", "CheckAPPVersion");
            jqljson.put("version", versioncode);
            jqljson.put("type", "Android");
            jqljson.put("clinicnamewi", "RootDB");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "checkAPPVersion Data = " + jqljson.toString());
        postkey = "CheckAPPVersion";
        progressBar_main.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        HttpPost httpPost = new HttpPost(MainActivity.this);
        httpPost.startPost(MainActivity.this, GlobalVariables.http_url,  jqljson.toString());
    }

    private void getSystemInfomation(){
        Log.e(TAG, "GetSystemInfomation");
        JSONObject jqljson = new JSONObject();
        String today = Utility.getDateNow();
        try {
            jqljson.put("command",  "GetList");    // from Auto -> APP
            jqljson.put("table", "Bulletin");
            jqljson.put("where", "WHERE '" + today + "' >= activate AND '" + today + "' <= expired AND status = 1" );
            jqljson.put("clinicnamewi", "RootDB");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postkey = "GetSystemInfomation_APP";
        HttpPost httpPost = new HttpPost(MainActivity.this);
        httpPost.startPost(MainActivity.this, GlobalVariables.http_url,  jqljson.toString());
        btnlogina.setEnabled(isFCMcompleted && isLastestVersion);         // 啟用登入按鍵
    }



    // 回應角色點選
    private String getRole(){
        String role  = "M";
        switch (radio_role.getCheckedRadioButtonId()) {
            case R.id.radio_roleadmin:
                role = "A";              // 診所
                break;
            case R.id.radio_roleuser:
                role = "M";              // 右手
                break;
        }
        return role;
    }

    // 設定角色
    private void setRole(String role){
        if (role.equals("A")){
            radio_role.check(R.id.radio_roleadmin);     // 設定診所
        } else {
            radio_role.check(R.id.radio_roleuser);          // 設定用戶
        }
    }

    // 初始化推播
    private void initFirebaseApp (){
        // 在 MainActivity 加入FirebaseInstanceId.getInstance().getInstanceId(); 已經depressed
        // 多了 addOnCompleteListener 是為了方便看每次 token 的變化。
        // Token 通常只有 App 第一次安裝會產一組新的
        FirebaseApp.initializeApp(MainActivity.this);
        //FirebaseInstanceId.getInstance().getInstanceId()
        //        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
        //            @Override
        //            public void onComplete(@NonNull Task<InstanceIdResult> task) {
        //                if (!task.isSuccessful()) {
        //                    return;
        //                }
        //                if( task.getResult() == null)
        //                    return;
        //                // Get new Instance ID token
        //                fcmToken = task.getResult().getToken();
        //                // Log and toast
        //                Log.e(TAG,"token = "+fcmToken);
        //            }
        //        });
    }

    // 在Oreo 以上版本添加 Channel id
    private void createChannelID (){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = "default_notification_channel_id";
            String channelName = "default_notification_channel_name";
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }   // end of if
    }

    private void CheckAuthorizationOffLine(){    // 只有Login的動作才能處理 SingleUse , SingleAdmin
    }



    @Override
    public void onComplete(String response) {
        try {
            Log.e(TAG, "onComplete -> response " + response);
            final JSONObject responseJson = new JSONObject(response);
            final String responseStatus = responseJson.optString("status", "na");
            final String responseResult = responseJson.optString("result", "na");
            runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          progressBar_main.setVisibility(View.GONE);                // 恢復觸控功能
                          getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                          switch (postkey) {
                              case "GetAESKey":
                                  Log.e(TAG, "GetAESKey " +  response);
                                  Utility_AES.SetAESKey(response);
                                  GlobalVariables.isEnclypted = true;
                                  checkAppVersion(appversioncode);                                    //   檢查最新版號);
                                  break;
                              case "GetAuthorization":
                                  if (responseStatus.equals("success")) {    // 登入成功
                                      String _rememberaccount = Utility_AES.Encrypt(username.getText().toString());
                                      Log.e(TAG, "_rememberaccount = " + _rememberaccount);
                                      sharedPreferences.edit()
                                              .putString("clinic", clinic.getText().toString())
                                              .putString("role", getRole())
                                              .putString("account", chk_rememberme.isChecked()?_rememberaccount : "")
                                              .commit();
                                      switch (GlobalVariables.Login_Role) {
                                          case "M":
                                              user.setUserjson(response);
                                              Log.e(TAG, "群組容量 = " + user.getGroupcapacity());
                                              GlobalVariables.Login_Clinic = clinic.getText().toString();
                                              String settingparem = user.getSettingParam();     //  Function Enabled
                                              GlobalVariables.settingParam(settingparem);       // 設定全域參數
                                              if (user.getBpmMAC().equals("")) {
                                                  activityAlert.showAlertDialog(R.string.dialog_msg_nobpmmacbind, R.string.dialog_OK, activityAlert.doNothing);
                                                  userpass.setText("");
                                              } else if (user.getUserprivacy().equals("")) {
                                                  activityAlert.showAlertDialog(R.string.dialog_msg_mustsignprivacypolicy, R.string.dialog_OK,
                                                          signPrivacyPolice);
                                              } else {    // 已經勾選隱私權
                                                  Intent intent = new Intent(MainActivity.this, MainFunctionActivity.class);
                                                  startActivity(intent);      // 在MainFunctionActivity 中 依Global Variable LoginRole 設定
                                              }
                                              break;
                                          case "A":
                                              SingleAdmin admin = SingleAdmin.getInstance();
                                              admin.setAdminjson(response);
                                              GlobalVariables.Login_Clinic = clinic.getText().toString();
                                              Intent intent = new Intent(MainActivity.this, MainFunctionActivity.class);
                                              startActivity(intent);      // 在MainFunctionActivity 中 依Global Variable LoginRole 設定
                                              break;
                                      }   // end of switch

                                  } else {  // 登入失敗  請重新登入
                                      Log.e(TAG, responseResult);
                                      if (responseResult.equals("notfound")) {
                                          if (GlobalVariables.Login_Role.equals("A")) {
                                              activityAlert.showAlertDialog(R.string.dialog_msg_clinicaccountpasserror,
                                                      R.string.dialog_OK, activityAlert.doNothing);
                                          } else {
                                              activityAlert.showAlertDialog(R.string.dialog_msg_accountpasserror,
                                                      R.string.dialog_OK, activityAlert.doNothing);
                                          }
                                      } else if (responseResult.equals("clinicnull")) {
                                          activityAlert.showAlertDialog(R.string.dialog_msg_clinicnameerror,
                                                  R.string.dialog_OK, activityAlert.doNothing);
                                      }
                                  }
                                  break;

                              case "BindMAC":
                                      user.setBindmac(user.getBpmMAC());
                                      if (user.getUserprivacy().equals("")) {
                                          activityAlert.showAlertDialog(R.string.dialog_msg_mustsignprivacypolicy, R.string.dialog_OK,
                                                  signPrivacyPolice);
                                      } else {    // 已經勾選隱私權
                                          Intent intent = new Intent(MainActivity.this, MainFunctionActivity.class);
                                          startActivity(intent);      // 在MainFunctionActivity 中 依Global Variable LoginRole 設定
                                      }
                                  break;

                              case "CheckAPPVersion":
                                  if (responseStatus.equals("success")) {      //  只有檢查 appType = android
                                      try {
                                          JSONObject datajson = responseJson.getJSONArray("dblist").getJSONObject(0);
//                                          GlobalVariables.http_url = datajson.getString("dbPath").toString();
//                                          Log.e(TAG, GlobalVariables.http_url);
//                                          GlobalVariables.http_url = "http://test.echainpulse.com/EChainMed/MeridianMain_Enc";
//                                          Log.e(TAG, datajson.getInt("onMarket") +  String.valueOf((datajson.getInt("onMarket") == 0)));
//                                          Log.e(TAG,  datajson.getLong("appVersion") + appversioncode  + String.valueOf(datajson.getLong("appVersion") == appversioncode));
//                                          GlobalVariables.bindEnable = (datajson.getInt("bindEnable") == 1);
                                          if (datajson.getInt("onMarket") == 0 ||  datajson.getLong("appVersion") == appversioncode) {
                                              isLastestVersion = true;            // 是最新版
                                              String _account = sharedPreferences.getString("account",  "");
                                              Log.e(TAG, "_account = " +  _account);
                                              if (_account.equals("")){
                                                  chk_rememberme.setChecked(false);         // 不要記住帳號
                                              } else {
                                                  chk_rememberme.setChecked(true);          // 記住我的帳號
                                                  username.setText(Utility_AES.Decrypt(_account));
                                              }
                                              getSystemInfomation();
                                              Log.e(TAG, " isFCMCompleted " + isFCMcompleted + " isLastestVersion " + isLastestVersion);
//                                              Log.e(TAG, datajson.getString("dbPath"));
                                          } else {      // 不是最新版
                                              isLastestVersion = false;
                                              btnlogina.setEnabled(isFCMcompleted && isLastestVersion);         // 啟用登入按鍵
                                              activityAlert.showAlertDialog(R.string.dialog_msg_appversionincorrect,
                                                      R.string.dialog_Update, downloadApp,
                                                      R.string.dialog_Cancel, activityAlert.doNothing);
                                          }
                                      } catch (JSONException e) {
                                          Log.e(TAG, e.toString());
                                          e.printStackTrace();
                                      }
                                  } else {
                                      Log.e(TAG, "checkAPPVersion Fail");
                                  }
                                  break;

                              case "GetSystemInfomation_APP":
                                  if (responseStatus.equals("success")) {
                                      try {
                                          JSONArray info_array = responseJson.getJSONArray("dblist");
                                          String infostr = "", infosep = "";
                                          JSONObject info_json = new JSONObject();
                                          for (int infoindex = 0 ; infoindex<info_array.length(); infoindex ++) {
                                                info_json = info_array.getJSONObject(infoindex);
                                                infostr = infostr + infosep + (infoindex + 1) + "【"  + info_json.getString("activate") + " --" +
                                                            info_json.getString("expired") + "】" + "\n" + info_json.getString("message");
                                                infosep = "\n";
                                            }
                                            activityAlert.showAlertDialog(infostr, R.string.dialog_OK, activityAlert.doNothing);
                                      } catch (JSONException e) {
                                          Log.e(TAG, e.toString());
                                          activityAlert.showAlertDialog(e.toString(),
                                                  R.string.dialog_OK, activityAlert.doNothing);
                                      }
                                  } else { // no information
                                      Log.e(TAG, "NO SystemInfomation Fail");
                                  }

                                  break;

                          }  // eod of  switch (postkey)

                      }
            });

        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onFail(String err) {
        try {
            final JSONObject failJson = new JSONObject(err);
            if (failJson.getString("status").equals("exception")){      // notfound 表示沒有找到對應的帳號
                final String failresult = failJson.getString("result");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 恢復觸控功能
                        progressBar_main.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // 系統錯誤 + failresult
                        activityAlert.showAlertDialog(getResources().getString(R.string.dialog_msg_systemerror) + "\n" + failresult,
                                R.string.dialog_OK, activityAlert.doNothing);
                        }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        Log.e(TAG, "onStart");
        super.onStart();
//        LogInOut.log("MainActivity", 0, "X", true);   // 此時尚未確定診所   不需要紀錄
        // 模擬器上想要隱藏鍵盤但是無效 實機上沒有這個問題
        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow( username.getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestory");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
//        LogInOut.log("MainActivity", 0, "X", false);      // 此時尚未確定診所   不需要紀錄
    }

    // 確認帳號密碼是否有輸入
    private boolean isuserdataempty(){
        boolean checkresult = false;
        if (username.getText().toString().equals("") || userpass.getText().toString().equals("") || clinic.getText().toString().equals("")) {
            checkresult = true;
            // 登入資訊不齊全
            activityAlert.showAlertDialog(R.string.dialog_msg_accountpassempty, R.string.dialog_OK,activityAlert.doNothing);
        } else if (username.getText().toString().indexOf(' ') != -1) {
            checkresult = true;
            activityAlert.showAlertDialog(R.string.dialog_msg_accountcontainsspace, R.string.dialog_OK,activityAlert.doNothing);
        } else if (userpass.getText().toString().indexOf(' ') != -1) {
            checkresult = true;
            activityAlert.showAlertDialog(R.string.dialog_msg_passcontainsspace, R.string.dialog_OK,activityAlert.doNothing);
        } else if (clinic.getText().toString().indexOf(' ') != -1) {
            checkresult = true;
            activityAlert.showAlertDialog(R.string.dialog_msg_cliniccontainsspace, R.string.dialog_OK,activityAlert.doNothing);
        } else if (! GlobalVariables.p_clinic.matcher(clinic.getText().toString()).find()){
            checkresult = true;
            activityAlert.showAlertDialog(R.string.msg_registerclinic_error, R.string.dialog_OK,activityAlert.doNothing);
        } else if (! GlobalVariables.p_account.matcher(username.getText().toString()).find()) {
            checkresult = true;
            activityAlert.showAlertDialog(R.string.msg_registeraccount_error, R.string.dialog_OK,activityAlert.doNothing);
        } else if (! GlobalVariables.p_pass.matcher(userpass.getText().toString()).find()) {
            checkresult = true;
            activityAlert.showAlertDialog(R.string.msg_registerpass_error, R.string.dialog_OK,activityAlert.doNothing);
        }
        return checkresult;
    }


    // 將 FCM Token 寫進資料庫
    private void updateFCMToken(String role, int id, String token){
        Log.e(TAG, "updateFCMToken");
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command", "UpdateFCMToken_APP");
            jqljson.put("versionTag", "1.1.0");
            if (role.equals("M")) {
                jqljson.put("clinicnamewi", clinic.getText());
            } else {
                jqljson.put("clinicnamewi", "RootDB");
            }
            jqljson.put("role", role);
            jqljson.put("id", id);
            jqljson.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postkey = "UpdateFCM";
        HttpPost httpPost = new HttpPost(MainActivity.this);
        httpPost.startPost(MainActivity.this, GlobalVariables.http_url,  jqljson.toString());
    }

    private void seetUIComponents() {
        btnlogina = (Button) findViewById(R.id.btn_logina);
        btnlogina.setEnabled(false);        // 預設關閉
        username = (EditText) findViewById(R.id.input_loginaccount);
        userpass = (EditText) findViewById(R.id.input_loginpass);
        clinic = (EditText) findViewById(R.id.input_clinic);
        label_maintitlecompany = (TextView) findViewById(R.id.label_maintitlecompany);
        radio_roleuser = (RadioButton) findViewById(R.id.radio_roleuser);
        radio_roleadmin = (RadioButton) findViewById(R.id.radio_roleadmin);
        radio_role = (RadioGroup) findViewById(R.id.radio_role);
        txtregisteraccount = (TextView) findViewById(R.id.label_registeraccount);
        txtforgetpass = (TextView) findViewById((R.id.label_forgetpass));
        label_version = (TextView) findViewById(R.id.label_version);
        img_eyepass = (ImageView) findViewById(R.id.img_eyepass);
        img_eyeaccount = (ImageView) findViewById(R.id.img_eyeaccount);
        chk_rememberme = (CheckBox) findViewById(R.id.chk_rememberme);
        chk_rememberme.setChecked(false);
        progressBar_main = (ProgressBar) findViewById(R.id.progressBar_main);
        progressBar_main.setVisibility(View.GONE);
        txtforgetpass.setVisibility(View.GONE);
    }

    // 前往隱私權頁面的介面
    private DialogInterface.OnClickListener signPrivacyPolice =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent privatyintent = new Intent(MainActivity.this, Activity_Privacy.class);
                    privatyintent.putExtra("type", "requestSign");
                    startActivity(privatyintent);
                }
            };



    // 下載新版 APP
    private DialogInterface.OnClickListener downloadApp = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent updateintent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.ideas.micro.jasonapp102"));
            startActivity(updateintent);
        }
    };


}