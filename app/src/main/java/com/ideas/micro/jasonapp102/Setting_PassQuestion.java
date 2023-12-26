package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class Setting_PassQuestion extends AppCompatActivity implements onHttpPostCallback {
    private String TAG = "密碼提示";
    private TextView tvTitle;
    EditText input_questionpid;
    EditText input_questionbirthday;
    EditText input_questionmobile;
    EditText input_questionrank;
    EditText input_questionfloor;
    Button btn_confirmpassquestion;
    private String postKey = "";
    SingleUser user = SingleUser.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_passquestion);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.setting_passquestion_title);
        input_questionpid = (EditText) findViewById(R.id.input_questionpid);
        input_questionbirthday = (EditText) findViewById(R.id.input_questionbirthday);
        input_questionmobile = (EditText) findViewById(R.id.input_questionmobile);
        input_questionrank = (EditText) findViewById(R.id.input_questionrank);
        input_questionfloor = (EditText) findViewById(R.id.input_questionfloor);
        btn_confirmpassquestion = (Button) findViewById(R.id.btn_confirmpassquestion);
        init_question();
        // 送出註冊需求
        btn_confirmpassquestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (! checkinputfield()) return;
                //Log.e(TAG, "性別選項 position = " + r_usergender.getSelectedItemPosition());  position 從 0 開始
                JSONObject jqljson = new JSONObject();
                try {
                    jqljson.put("command","UpdatePassQuestion");
                    jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
                    jqljson.put("ID", user.getUserID());
                    jqljson.put("Role", GlobalVariables.Login_Role);
                    jqljson.put("Pid5", input_questionpid.getText().toString());
                    jqljson.put("Brd4", input_questionbirthday.getText().toString());
                    jqljson.put("Mbl5", input_questionmobile.getText().toString());
                    jqljson.put("Rnk", Integer.parseInt(input_questionrank.getText().toString()));
                    jqljson.put("Flr", Integer.parseInt(input_questionfloor.getText().toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                postKey = "UpdatePassQuestion";
                HttpPost httpPost = new HttpPost(Setting_PassQuestion.this);
                httpPost.startPost(Setting_PassQuestion.this, GlobalVariables.http_url,  jqljson.toString());
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        LogInOut.log("Setting_PassQuestion", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Setting_PassQuestion", false);
    }

    // 檢查輸入項目
    private boolean checkinputfield() {
        boolean checkresult = true;
        int errorfieldid = 0;
        Pattern p_pid5 = Pattern.compile("^[0-9]{5}$");
        Pattern p_brd4 = Pattern.compile("^[0-9]{4}$");
        Pattern p_rank = Pattern.compile("^[1-9]{1}[0-9]{0,1}$");
        Pattern p_floor = Pattern.compile("^[1-9]{1}[0-9]{0,2}$");

        // 生日檢查是否有選取 不需要判別
        if (!p_pid5.matcher(input_questionpid.getText().toString()).find()) {
            checkresult = false;    // PID
            errorfieldid = R.string.msg_forgetpid_error;;
        } else if (!p_brd4.matcher(input_questionbirthday.getText().toString()).find()) {
            checkresult = false;    // Mobile Phone
            errorfieldid = R.string.msg_forgetbirthday_error;
        } else if (!p_pid5.matcher(input_questionmobile.getText().toString()).find()) {
            checkresult = false;    // email
            errorfieldid = R.string.msg_forgetmobile_error;
        } else if (!p_rank.matcher(input_questionrank.getText().toString()).find()) {
            checkresult = false;    // email
            errorfieldid = R.string.msg_forgetrank_error;
        } else if (!p_floor.matcher(input_questionfloor.getText().toString()).find()) {
            checkresult = false;    // email
            errorfieldid = R.string.msg_forgetfloor_error;
        }

        if (!checkresult) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Setting_PassQuestion.this);
            builder.setMessage(getResources().getString(errorfieldid))
                    .setTitle(R.string.dialog_systemcomment)
                    .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // 不需要做事情
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return checkresult;
    }

    // 初始值設定
    private void init_question(){
        if (! user.getUserpid().equals("")) {       // 身分證末五碼
            String pid = user.getUserpid();
            input_questionpid.setText(pid.substring(pid.length() - 5));
        }
        if (! user.getUserbirthday().equals("")) {      // 生日末四碼
            String brd = user.getUserbirthday();
            input_questionbirthday.setText(brd.substring(5, 7) + brd.substring(8, 10));
        }
        if (! user.getUsermobile().equals("")) {       // 手機末五碼
            String mobile = user.getUsermobile();
            input_questionmobile.setText(mobile.substring(mobile.length() - 5));
        }
    }

    @Override
    public void onComplete(String response) {
        try {
            final JSONObject responseJson = new JSONObject(response);
            if (postKey.equals("UpdatePassQuestion")) {    // 只有Login的動作才能處理 SingleUse , SingleAdmin
                if (responseJson.getString("status").equals("success")) {      //
                    showAlert(R.string.dialog_msg_bpmuploadsuccess);
                } else {
                    showAlert(R.string.dialog_msg_bpmuploaderror);
                }
            }  // eod of  postkey.equals("UpdatePassQuestion"))
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onFail(String err) {

    }

    private void showAlert(int message){
        showAlert(getResources().getString(message));
    }

    private void showAlert(String message){
        final String msg = message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(Setting_PassQuestion.this);
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
}