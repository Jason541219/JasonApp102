package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

//import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class Activity_Privacy extends AppCompatActivity implements onHttpPostCallback{
    private String TAG="Privacy";
    private String postKey = "";

    CheckBox checkbox_privacyagree;
    private TextView tvTitle;
    private TextView txt_privacy;
    private ProgressBar progressBar_privacy;
    private String registerType;
    private String signdate;
    private String privacy, personal, useauthorization;
    private int signstage = 0;
    private Utility_ActivityAlert activityAlert = new Utility_ActivityAlert(Activity_Privacy.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_privacy_title);
        txt_privacy = (TextView) findViewById(R.id.txt_privacy);
        checkbox_privacyagree = (CheckBox) findViewById(R.id.checkbox_privacyagree);
        progressBar_privacy = (ProgressBar) findViewById(R.id.progressBar_privacy);
        progressBar_privacy.setVisibility(View.GONE);
        Intent getintent = getIntent();
        registerType = getintent.getStringExtra("type");        // 來自前一個Activity 的 intent
        signstage = 0;
        getPolicyHtml();

        checkbox_privacyagree.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (checkbox_privacyagree.isChecked()){
                    switch (signstage) {
                        case 0:
                            txt_privacy.setText(Html.fromHtml(personal, Html.FROM_HTML_MODE_LEGACY));
                            signstage = 1;  // 進到個人資訊
                            checkbox_privacyagree.setChecked(false);
                            break;

                        case 1:
                            txt_privacy.setText(Html.fromHtml(useauthorization, Html.FROM_HTML_MODE_LEGACY));
                            signstage = 2;  // 進到使用者授權
                            checkbox_privacyagree.setChecked(false);
                            break;

                        case 2:
                            signdate = Utility.getDateTimeNow();
                            switch (registerType) {
                                case "newRegister":     // 新註冊
                                    Intent intent = new Intent (Activity_Privacy.this, Activity_Register.class);
                                    intent.putExtra("signdate", Utility.getDateTimeNow());
                                    startActivity(intent);  // 到註冊資訊頁面再記錄時間
                                    break;
                                case "requestSign":
                                    saveSign();
                                    break;
                            }
                            break;
                    }

                }
            }
        });

    }       // end of onCreate

//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    //
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.menu, menu)
//        return true
//    }
    @Override
    protected void onStart() {
        super.onStart();
        LogInOut.log("Activity_Privacy", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkbox_privacyagree.setChecked(false);    // 撤銷勾選
        LogInOut.log("Activity_Privacy", false);
    }

    private void getPolicyHtml(){
        JSONObject sqljson = new JSONObject();
        try {
            sqljson.put("command", "getPolicyHtml");
            sqljson.put("clinicnamewi", "RootDB");
            postKey = "getPolicyHtml";
            HttpPost httpPost = new HttpPost(Activity_Privacy.this);
            httpPost.startPost(Activity_Privacy.this,  GlobalVariables.http_url, sqljson.toString());
            progressBar_privacy.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveSign(){    // 紀錄簽屬
        JSONObject sqljson = new JSONObject();
        try {
            sqljson.put("command", "RecordSignDate");
            sqljson.put("mid", SingleUser.getInstance().getUserID());
            sqljson.put("date", signdate);
            sqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            postKey = "RecordSignDate";
            HttpPost httpPost = new HttpPost(Activity_Privacy.this);
            httpPost.startPost(Activity_Privacy.this, GlobalVariables.http_url,  sqljson.toString());
            progressBar_privacy.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onComplete(String response) {
        try {
            final JSONObject responseJson = new JSONObject(response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar_privacy.setVisibility(View.GONE);
                    // 恢復觸控功能
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
            switch (postKey) {
                case "getPolicyHtml":
                    if (responseJson.getString("status").equals("success")) {
//                        Log.e(TAG, responseJson.getJSONArray("dblist").toString());
                        JSONObject jhtml = responseJson.getJSONArray("dblist").getJSONObject(0);
//                        Log.e(TAG, jhtml.toString());
                        privacy = jhtml.getString("privacy");
                        personal = jhtml.getString("personal");
                        useauthorization = jhtml.getString("useauthorization");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 預設為隱私權聲明
                                txt_privacy.setText(Html.fromHtml(privacy, Html.FROM_HTML_MODE_LEGACY));
                            }
                        });
                    }
                break;

                case  "RecordSignDate":
                    if (responseJson.getString("status").equals("success")) {
                        SingleUser.getInstance().setUserprivacy(signdate);  // 單獨設定 Privacy Date
                        Intent intent = new Intent(Activity_Privacy.this, MainFunctionActivity.class);
                        startActivity(intent);      // 在MainFunctionActivity 中 依Global Variable LoginRole 設定
                    }
                    break;
            }

            } catch (JSONException e) {

            }
        }


        @Override
    public void onFail(String err) {

    }

}