package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

//import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class Activity_MHBagreement extends AppCompatActivity implements onHttpPostCallback{
    private String TAG="健康存摺同意書";
    private String postKey = "";

    CheckBox checkbox_mhbagreement;
    private TextView tvTitle;
    private Button btn_mhbagreement, btn_mhbdisagree;
    private TextView txt_mhbagreement;
    private ProgressBar progressBar_mhbagreement;
    private String registerType;
    private String signdate;
    private String mhbagreement;
    private int signstage = 0;
    private SingleUser user = SingleUser.getInstance();
    private Utility_ActivityAlert activityAlert = new Utility_ActivityAlert(Activity_MHBagreement.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhbagreement);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_mhbagreement_title);
        txt_mhbagreement = (TextView) findViewById(R.id.txt_mhbagreement);
        checkbox_mhbagreement = (CheckBox) findViewById(R.id.checkbox_mhbagreement);
        progressBar_mhbagreement = (ProgressBar) findViewById(R.id.progressBar_mhbagreement);
        btn_mhbagreement = (Button) findViewById(R.id.btn_mhbagreement);
        btn_mhbagreement.setEnabled(false);
        btn_mhbdisagree = (Button) findViewById(R.id.btn_mhbdisagree);
        progressBar_mhbagreement.setVisibility(View.GONE);
        getPolicyHtml();        // 到資料庫讀取

       btn_mhbdisagree.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v) {    // 返回
               Intent intent = new Intent(Activity_MHBagreement.this, MainFunctionActivity.class);
               startActivity(intent);      // 在MainFunctionActivity 中 依Global Variable LoginRole 設定
           }
       });

       btn_mhbagreement.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v) {
               saveSign();  // 儲存紀錄
           }
       });

        checkbox_mhbagreement.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                btn_mhbagreement.setEnabled(checkbox_mhbagreement.isChecked());
            }
        });
    }       // end of onCreate

    @Override
    protected void onStart() {
        super.onStart();
//        LogInOut.log("Activity_Privacy", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkbox_mhbagreement.setChecked(false);    // 撤銷勾選
//        LogInOut.log("Activity_Privacy", false);
    }

    private void getPolicyHtml(){
        JSONObject sqljson = new JSONObject();
        try {
            sqljson.put("command", "getPolicyHtml");
            sqljson.put("clinicnamewi", "RootDB");
            postKey = "getPolicyHtml";
            HttpPost httpPost = new HttpPost(Activity_MHBagreement.this);
            httpPost.startPost(Activity_MHBagreement.this,  GlobalVariables.http_url, sqljson.toString());
            progressBar_mhbagreement.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveSign(){    // 紀錄簽屬
        JSONObject sqljson = new JSONObject();
        try {
            sqljson.put("command", "RecordMHBSignDate");
            sqljson.put("mid", SingleUser.getInstance().getUserID());
            sqljson.put("date", Utility.getDateTimeNow());
            sqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            postKey = "RecordSignDate";
            HttpPost httpPost = new HttpPost(Activity_MHBagreement.this);
            httpPost.startPost(Activity_MHBagreement.this, GlobalVariables.http_url,  sqljson.toString());
            progressBar_mhbagreement.setVisibility(View.VISIBLE);
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
                    progressBar_mhbagreement.setVisibility(View.GONE);
                    // 恢復觸控功能
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
            switch (postKey) {
                case "getPolicyHtml":
                    if (responseJson.getString("status").equals("success")) {
                        JSONObject jhtml = responseJson.getJSONArray("dblist").getJSONObject(0);
                        mhbagreement = jhtml.getString("mhbagreement");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 預設為隱私權聲明
                                txt_mhbagreement.setText(Html.fromHtml(mhbagreement, Html.FROM_HTML_MODE_LEGACY));
                            }
                        });
                    }
                    break;

                case  "RecordSignDate":
                    if (responseJson.getString("status").equals("success")) {
                        String cid = GlobalVariables.Login_Clinic;
                        String pid = Utility_AES.Encrypt(user.getUserpid());     // 加密
                        String uri_subscript = null;
                        uri_subscript = GlobalVariables.subscript_url + "cid=" + cid + "&pid=" + pid.replace("+", "%2B");
                        Log.e(TAG, String.valueOf(Uri.parse(uri_subscript)));
                        Intent go2subscript = new Intent(Intent.ACTION_VIEW, Uri.parse(uri_subscript));
                        startActivity(go2subscript);
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