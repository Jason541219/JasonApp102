package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Activity_Mygroup extends AppCompatActivity implements onHttpPostCallback {
    private final String TAG = "我的群組";
    private TextView tvTitle;
    private ListView listView;
    private TextView txt_idforgroup, label_idforgroup;
    private EditText input_pidforgroup, input_mobileforgroup, input_emailforgroup;
    private Spinner spinner_clinicforgroup;
    private Button btn_addgroupmember;
    private ProgressBar progressBar_mygroup;
    private JSONArray grouplist = new JSONArray();
    private int groupMemberCount = 0;
    LayoutInflater inflater;
    ViewAdapter_Mygroup listAdapter;
    private SingleUser user = SingleUser.getInstance();
    private Utility_ActivityAlert activityAlert = new Utility_ActivityAlert(Activity_Mygroup.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setUIComponent();           // **** 設定UI
        Log.e(TAG, "主帳號" + user.getIsgroupprimary());
        if (user.getIsgroupprimary() == 0){     // ***** 沒有權限使用我的群組
            activityAlert.showAlertDialog(R.string.dialog_msg_mygroupnoauthorization, R.string.dialog_OK, activityAlert.finishActivity);
        } else {
            getClinicList();
        }
//        getGroupMemberList();             在 getClinicList () 之中執行

        // 加入群組
        btn_addgroupmember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clinicnamewi = clinicid.getString(spinner_clinicforgroup.getSelectedItem().toString());
                    if (input_pidforgroup.getText().toString().equals("") || input_emailforgroup.getText().toString().equals("") || input_mobileforgroup.getText().toString().equals("")) {
                        activityAlert.showAlertDialog(R.string.dialog_msg_pidforgroupempty, R.string.dialog_OK, activityAlert.doNothing);
                    } else {
                        Log.e(TAG, "clinicnamewi = " + clinicnamewi);
                        addGroupMember();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // 選取診所
        spinner_clinicforgroup.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Log.e(TAG, " spinner_clinicforgroup.setOnItemSelectedListener cliniclist Size = " + cliniclist.size() + " pos = " + pos);
                String clinicname = cliniclist.get(pos).toString();
                try {
                    clinicnamewi = clinicid.getString(clinicname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }       // end of onCreate

    private String postKey;     // 用來辨識
    private JSONObject clinicid = new JSONObject();
    private JSONArray clinicdblist = new JSONArray();
    private JSONArray groupmemberlist = new JSONArray();
    private JSONObject clinicdbjson = new JSONObject();
    private ArrayList cliniclist = new ArrayList<String>();
    private String clinicnamewi = "";

    private void getClinicList(){
        cliniclist = new ArrayList<String>();
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command",  "GetBpmClinic");
            jqljson.put("clinicnamewi", "RootDB");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressBar_mygroup.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        postKey = "GetBpmClinic";
        HttpPost httpPost = new HttpPost(Activity_Mygroup.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_Mygroup.this,  GlobalVariables.http_url, jqljson.toString());
    }

    // 尋找群組會員
    private void addGroupMember(){
//        cliniclist = new ArrayList<String>();
        JSONObject jqljson = new JSONObject();
        try {
            int _ispaymember = user.getIspayuser();
            if (String.valueOf(_ispaymember).substring(1).equals("00")) {
                jqljson.put("ispaymember", 100);
            } else if (String.valueOf(_ispaymember).substring(1).equals("50")) {
                jqljson.put("ispaymember", 150);
            }
            jqljson.put("command",  "AddShareGroupMember_APP");
            jqljson.put("clinicnamewi", clinicnamewi);
            jqljson.put("groupname", user.getGroupname());
            jqljson.put("mac", user.getBpmMAC());
            jqljson.put("sn", user.getBpmSN());
            jqljson.put("mobile", input_mobileforgroup.getText().toString());
            jqljson.put("email", input_emailforgroup.getText().toString());
            jqljson.put("todate", Utility.getDateNow());
            jqljson.put("type", user.getBpmtype());
            jqljson.put("pid", input_pidforgroup.getText().toString());
            Log.e(TAG, jqljson.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        input_pidforgroup.setText("");
        input_emailforgroup.setText("");
        input_mobileforgroup.setText("");
        progressBar_mygroup.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        postKey = "AddShareGroupMember_APP";
        HttpPost httpPost = new HttpPost(Activity_Mygroup.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_Mygroup.this, GlobalVariables.http_url,  jqljson.toString());
    }

    // 尋找群組會員
    private void getGroupMemberList(){
//        cliniclist = new ArrayList<String>();
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command",  "GetGroupMemberList");
            jqljson.put("clinicnamewi", "RootDB");
            jqljson.put("groupname", user.getGroupname());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressBar_mygroup.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        postKey = "GetGroupMemberList";
        HttpPost httpPost = new HttpPost(Activity_Mygroup.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_Mygroup.this, GlobalVariables.http_url,  jqljson.toString());
    }

    // 設定UI元件
    private void setUIComponent() {
        setContentView(R.layout.activity_mygroup);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_mygroup_title);
        txt_idforgroup = (TextView) findViewById(R.id.txt_idforgroup);
        label_idforgroup = (TextView) findViewById(R.id.label_idforgroup);
        txt_idforgroup.setText(user.getGroupname());        // 群組名稱
        label_idforgroup.setVisibility(View.GONE);
        txt_idforgroup.setVisibility(View.GONE);
        input_pidforgroup = (EditText) findViewById((R.id.input_pidforgroup));
        input_mobileforgroup = (EditText) findViewById(R.id.input_mobileforgroup);
        input_emailforgroup = (EditText) findViewById(R.id.input_emailforgroup);
        spinner_clinicforgroup = (Spinner) findViewById(R.id.spinner_clinicforgroup);
        btn_addgroupmember = (Button) findViewById(R.id.btn_addgroupmember);
        listView = (ListView) findViewById(R.id.mygrouplist);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        progressBar_mygroup = (ProgressBar) findViewById(R.id.progressBar_mygroup);
        progressBar_mygroup.setVisibility(View.GONE);
    }

    @Override
    public void onComplete(String response) {
        try {
            final JSONObject responseJson = new JSONObject(response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar_mygroup.setVisibility(View.GONE);
                    // 恢復觸控功能
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
            if (postKey.equals("GetBpmClinic")) {
                if (responseJson.getString("status").equals("success")) {      //
                    clinicdblist = new JSONArray();
                    clinicdblist = responseJson.getJSONArray("dblist");
                    for (int i = 0; i < clinicdblist.length(); i++) {
                        clinicdbjson = clinicdblist.getJSONObject(i);
                        String clinicnameshort = clinicdbjson.getString("clinicNameShort");
                        cliniclist.add(clinicnameshort);
                        clinicid.put(clinicnameshort, clinicdbjson.getString("clinicDBName"));
                    }
                    // 設定下拉式選單
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter typeAdapter = new ArrayAdapter(Activity_Mygroup.this,
                                    R.layout.style_spinner, cliniclist);
//                            ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
//                                    android.R.layout.simple_dropdown_item_1line, memlist);
//                            typeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                            spinner_clinicforgroup.setAdapter(typeAdapter);
                            getGroupMemberList();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cliniclist.clear();    // 清空下拉式選單
                            cliniclist.add("--");
                            ArrayAdapter typeAdapter = new ArrayAdapter(Activity_Mygroup.this,
                                    R.layout.style_spinner, cliniclist);
//                            ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
//                                    android.R.layout.simple_dropdown_item_1line, memlist);
                            spinner_clinicforgroup.setAdapter(typeAdapter);
                            activityAlert.showAlertDialog(R.string.dialog_msg_nobpmclinic, R.string.dialog_OK, activityAlert.doNothing);
//                            btn_addgroupmember.setEnabled(false);   // 禁能按鍵
                       }
                    });
                }
            } else if (postKey.equals("GetGroupMemberList")){
                if (responseJson.getString("status").equals("success")) {      //
                    Log.e(TAG, postKey + " -> " + response);
                    groupmemberlist = responseJson.getJSONArray("dblist");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listAdapter = new ViewAdapter_Mygroup(groupmemberlist, inflater);
                            listView.setAdapter(listAdapter);
                            groupMemberCount = groupmemberlist.length();
                            if (groupMemberCount >= user.getGroupcapacity()) {
                                Log.e(TAG, "有 " +  groupmemberlist.length() + " 會員");
                                activityAlert.showAlertDialog(R.string.dialog_msg_groupmemberexceeds, R.string.dialog_OK, activityAlert.doNothing);
                                btn_addgroupmember.setEnabled(false);
                            } else {
                                Log.e(TAG, "有 " +  groupmemberlist.length() + " 會員");
                            }
                        }
                    });
                }
            } else if (postKey.equals("AddShareGroupMember_APP")) {
                Log.e(TAG, responseJson.getString("status"));
                if (responseJson.getString("status").equals("success")) {      //
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getGroupMemberList();
                        }
                    });    // end of runOnUiThread
                } else {    //
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activityAlert.showAlertDialog(R.string.dialog_msg_pidforgroupfail, R.string.dialog_OK, activityAlert.doNothing);
                        }
                    });     // end of runOnUiThread
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onFail(String err) {
        Log.e(TAG, err);
    }

    /*
        根據診所及身分證，自動搜尋生日等資料，搜尋不到的請去
     */
}