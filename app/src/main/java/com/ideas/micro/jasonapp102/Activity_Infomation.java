package com.ideas.micro.jasonapp102;

import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Activity_Infomation extends AppCompatActivity implements onHttpPostCallback {
    private final String TAG = "系統公告";
    private TextView tvTitle;
    private TextView txt_infomation;
    private  String postKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infomation);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_infomation_title);
        txt_infomation = (TextView) findViewById(R.id.txt_infomation);
        getSystemInfomation();
    }

    private void getSystemInfomation(){
        Log.e(TAG, "GetSystemInfomation");
        JSONObject jqljson = new JSONObject();
        String today = Utility.getDateNow();
        try {
            jqljson.put("command",  "GetList");    // from Auto -> APP
            jqljson.put("table", "Bulletin");
            jqljson.put("where", "WHERE " + today + " >= activate AND " + today + " <= expired AND status = 1" );
            jqljson.put("clinicnamewi", "RootDB");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "GetSystemInfomation_APP";
        HttpPost httpPost = new HttpPost(Activity_Infomation.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_Infomation.this, GlobalVariables.http_url,  jqljson.toString());
    }

    private void displaySystemInfomation(JSONObject responsejson) {
        String msg = "";
        try {
            JSONArray infoarray = new JSONArray(responsejson.getJSONArray("dblist"));
            int msgcount = infoarray.length();
            if (msgcount > 0) {
                for (int i = 0; i < infoarray.length(); i++) {
                    JSONObject infojson = infoarray.getJSONObject(i);
                    msg += "<div>";
                    msg += "<h4>日期：" + infojson.getString("activate") + " - " + infojson.getString("expired") + "</h4>";
                    msg += "<p>" + infojson.getString("message") + "</p>";
                    msg += "</div>";
                }
            } else {    // msgcount == 0
                msg += "<div>";
                msg += "<h4>目前沒有系統訊息公告<h4>";
                msg += "</div>";
            }
            txt_infomation.setText(Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY));
        } catch (JSONException e) {
            Log.e(TAG, "displaySystemInfomation JSONException : " + e.toString());
        }
    }

    @Override
    public void onComplete(String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject responseJson = new JSONObject(response);
                    Log.e(TAG, "response = " + response);
                    switch (postKey) {
                        case "GetSystemInfomation_APP":
                            displaySystemInfomation(responseJson);
                            break;
                    }
                } catch (JSONException e) {
                }
            }
        }); // enf of runOnUiThread
    }   // end of onComplete

    @Override
    public void onFail(String err) {

    }


}