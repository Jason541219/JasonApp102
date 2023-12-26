package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class Setting_ChangePass extends AppCompatActivity implements onHttpPostCallback {
    private EditText input_password00;
    private EditText input_password10;
    private EditText input_password11;
    private TextView tvTitle;
    private Button btn_confirmchangepass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_changepass);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.setting_changepass_title);
        input_password00 = (EditText) findViewById(R.id.input_password00);
        input_password10 = (EditText) findViewById(R.id.input_password10);
        input_password11 = (EditText) findViewById(R.id.input_password11);
        btn_confirmchangepass = (Button) findViewById(R.id.btn_confirmchangepass);

        btn_confirmchangepass.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (hasEmptyField()) {  // 空欄位
                    showAlert(R.string.dialog_msg_changepassempty);
                    return;
                } else if (newPassConflict()) { // 新密碼不同
                    showAlert(R.string.dialog_msg_changepassconflict);
                    return;
                } else if (!GlobalVariables.p_pass.matcher(input_password00.getText().toString()).find()) { // 密碼格式錯誤
                    showAlert(R.string.msg_registerpass_error);
                    return;
                } else if (!GlobalVariables.p_pass.matcher(input_password10.getText().toString()).find()) { // 密碼格式錯誤
                    showAlert(R.string.msg_registerpass_error);
                    return;
                } else {    // 資料上傳
                    JSONObject jqljson = new JSONObject();
                    try {
                        jqljson.put("command", "ChangePass");
                        jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
                        jqljson.put("pass0", input_password00.getText().toString());    // 原密碼
                        jqljson.put("pass1", input_password10.getText().toString());    // 新密碼
                        if (GlobalVariables.Login_Role.equals("A")){
                            jqljson.put("role", "A");    // 腳色
                            jqljson.put("id", SingleAdmin.getInstance().getAdminID());
                        } else {
                            jqljson.put("role", "M");    //
                            jqljson.put("id", SingleUser.getInstance().getUserID());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    HttpPost httpPost = new HttpPost(Setting_ChangePass.this);
                    httpPost.startPost(Setting_ChangePass.this, GlobalVariables.http_url,  jqljson.toString());
                }

            }   // end of onclick
        });
    }       // end of onCreate

    protected void onStart() {
        super.onStart();
        LogInOut.log("Setting_ChangePass", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Setting_ChangePass", false);
    }

    @Override
    public void onComplete(final String response) {
        try {
            final JSONObject completeJson = new JSONObject(response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int response_msg = 0;
                    try {
                        if (completeJson.getString("status").equals("success")) {      //
                            response_msg = R.string.dialog_msg_changepasssuccess;
                            Intent intent = new Intent(Setting_ChangePass.this, MainActivity.class);
                            startActivity(intent);
                        } else if (completeJson.getString("status").equals("fail")) {
                            switch (completeJson.getString("result")){
                                case "updatefail":
                                    response_msg = R.string.dialog_msg_changepassupdatefail;
                                    break;
                                case "incorrectpass":
                                    response_msg = R.string.dialog_msg_changepassincorrect;
                                    break;
                                default:
                                    response_msg = R.string.dialog_msg_changepassdb;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showAlert(response_msg);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onFail(String err) {
        try {
            final JSONObject failJson = new JSONObject(err);
            if (failJson.getString("status").equals("exception")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int msg = 0;
                            msg = R.string.dialog_msg_changepassdb;
                            showAlert(msg);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 檢查是否有空欄位
    private boolean hasEmptyField(){
        return
                input_password00.getText().toString().equals("") ||
                input_password10.getText().toString().equals("") ||
                input_password11.getText().toString().equals("");
    }

    // 檢查新密碼是否相同
    private boolean newPassConflict(){
        return !(input_password10.getText().toString().equals(input_password11.getText().toString()));
    }

    private void showAlert(int res) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Setting_ChangePass.this);
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