package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Setting_RequestPush extends AppCompatActivity implements onHttpPostCallback {
    private final String TAG = "推播設定";
    private EditText input_pushaccount;
    private ListView listView;
    private LayoutInflater inflater;
    private String httpcommand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_requestpush);
        Log.e(TAG, "onCreate");
        input_pushaccount = (EditText) findViewById(R.id.input_pushaccount);
        ImageButton btn_requestpush = (ImageButton) findViewById(R.id.btn_requestpush);
        listView = (ListView) findViewById(R.id.pushlist);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        listView.setOnItemClickListener(onClickListView);       //  沒有點擊指定事件 Method
        getPushList();

        // 新增推播
        btn_requestpush.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (input_pushaccount.getText().toString().equals("")){
                    showAlert(R.string.dialog_msg_familyfieldempty);
                } else {    // 資料上傳
                    JSONObject jqljson = new JSONObject();
                    try {
                        if (GlobalVariables.Login_Role.equals("A")){    // 醫療人員沒有加入親屬功能
                        } else {
                            jqljson.put("command", "RequestPush");
                            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
                            jqljson.put("fromaccount", input_pushaccount.getText().toString());    // 親屬帳號
                            jqljson.put("toid", SingleUser.getInstance().getUserID() );         // 自己的ID
                            jqljson.put("requesttime", Utility.getDateTimeNow());
                            jqljson.put("torole", "M");
                            httpcommand = "RequestPush";
                            HttpPost httpPost = new HttpPost(Setting_RequestPush.this);
                            httpPost.startPost(Setting_RequestPush.this, GlobalVariables.http_url,  jqljson.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }   // end of onclick

        });
    }

    protected void onStart() {
        super.onStart();
        LogInOut.log("Setting_RequestPush", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Setting_RequestPush", false);
    }

    @Override
    public void onComplete(String response) {
        try {
            int msgres = 0;
            final JSONObject responseJson = new JSONObject(response);
                if (httpcommand.equals("GetRequestPushList")) {
                    if (responseJson.getString("status").equals("success")) {      //
                        final JSONArray dblist = responseJson.getJSONArray("dblist");
                        // 設定下拉式選單
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setListData(dblist);
                            }
                        });
                    }
                }   // end of GetRequestPushList
            else if (httpcommand.equals("RequestPush")) {
                    if (responseJson.getString("status").equals("success")) {
                        msgres = R.string.dialog_msg_familysettingok;
                    } else if (responseJson.getString("status").equals("fail")) {
                        switch (responseJson.getString("result")) {
                            case "familyalready":
                                msgres = R.string.dialog_msg_familyalready;
                                break;
                            case "insertfail":
                                msgres = R.string.dialog_msg_insertfamilyfail;
                                break;
                            case "updatefail":
                                msgres = R.string.dialog_msg_updatefamilyfail;
                                break;
                            case "incorrectfamilyinfo":
                                msgres = R.string.dialog_msg_incorrectfamilyinfo;
                                break;
                        }
                    }
                    final int finalMsgres = msgres;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAlert(finalMsgres);
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

    }

    private void getPushList() {
        httpcommand = "GetRequestPushList";
        JSONObject sqljson = new JSONObject();
        try {
            sqljson.put("command", "GetRequestPushList");
            sqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            sqljson.put("id", SingleUser.getInstance().getUserID());         // 自己的ID
            sqljson.put("role", "M");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpPost httpPost = new HttpPost(Setting_RequestPush.this);
        httpPost.startPost(Setting_RequestPush.this, GlobalVariables.http_url,  sqljson.toString());
    }

    private void setListData(JSONArray dblist) {
        ViewAdapter_AcceptFamily listAdapter = new ViewAdapter_AcceptFamily(dblist, inflater, Setting_RequestPush.this);
        listView.setAdapter(listAdapter);
    }

    /***
     * 點擊ListView事件Method
     */
    // 沒有點擊事件
//    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
//        @Override
//        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//            //Toast.makeText(MainFunctionActivity.this,"點選第 "+(position +1) +" 個 \n內容："+listdata[position][1], Toast.LENGTH_SHORT).show();
//            try {
//                Intent intent = new Intent(Setting_RequestPush.this, Activity_ChartWave.class);
//                long recordid = recordlist.getJSONObject(position).getLong("rid");
//                intent.putExtra("recordID", recordid);
//                Log.e(TAG, "recordid = " + recordid);
//                startActivity(intent);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    };

    private void showAlert(int res){
        showAlert(getResources().getString(res));
    }

    private void showAlert(String res) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Setting_RequestPush.this);
        builder.setMessage(res)
                .setTitle(R.string.dialog_systemcomment)
                .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}