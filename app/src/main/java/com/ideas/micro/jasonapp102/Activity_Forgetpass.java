package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class Activity_Forgetpass extends AppCompatActivity implements onHttpPostCallback {
    final String TAG="忘記密碼";
    TextView label_forgetrole;
    EditText input_forgetclinic;
    EditText input_forgetaccount;
    EditText input_forgetpid;
    EditText input_forgetbirthday;
    EditText input_forgetmobile;
    EditText input_forgetrank;
    EditText input_forgetfloor;
    private TextView tvTitle;
    Button btn_forgetpass;
    private RadioButton radio_imadmin, radio_imuser;
    private RadioGroup radio_forgetrole;
    private String postKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpass);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_forgetpass_title);
        label_forgetrole = (TextView) findViewById(R.id.label_forgetrole);
        input_forgetclinic = (EditText) findViewById(R.id.input_forgetclinic);
        input_forgetaccount = (EditText) findViewById(R.id.input_forgetaccount);
        input_forgetpid = (EditText) findViewById(R.id.input_forgetpid);
        input_forgetbirthday = (EditText) findViewById(R.id.input_forgetbirthday);
        input_forgetmobile = (EditText) findViewById(R.id.input_forgetmobile);
        input_forgetrank = (EditText) findViewById(R.id.input_forgetrank);
        input_forgetfloor = (EditText) findViewById(R.id.input_forgetfloor);
        btn_forgetpass = (Button) findViewById(R.id.btn_forgetpass);
        radio_forgetrole = (RadioGroup) findViewById(R.id.radio_forgetrole);
        radio_imadmin = (RadioButton) findViewById(R.id.radio_imadmin);
        radio_imuser = (RadioButton) findViewById(R.id.radio_imuser);

        label_forgetrole.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                input_forgetclinic.setText("內部測試");
                input_forgetaccount.setText("test002");
                input_forgetpid.setText("56789");
                input_forgetbirthday.setText("0106");
                input_forgetmobile.setText("45678");
                input_forgetrank.setText("");
                input_forgetfloor.setText("");
                radio_forgetrole.check(R.id.radio_imuser);
            }
        });

        // 送出註冊需求
        btn_forgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility_Alert.showAlertDialog(Activity_Forgetpass.this, R.string.dialog_msg_forgetpassask,
                        R.string.dialog_goforgetpass, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sendForgetPass();
                            }
                        },
                        R.string.dialog_abordforgetpass, Utility_Alert.doNothing);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        LogInOut.log("Activity_Forgetpass", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Activity_Forgetpass", false);
    }

    private void sendForgetPass(){
        if (! checkinputfield()) return;
        Log.e(TAG, "輸入內容檢查OK");
        JSONObject registerjson = new JSONObject();
        try {
            switch (radio_forgetrole.getCheckedRadioButtonId()) {
                case R.id.radio_imadmin:
                    registerjson.put("userrole", "A");
                    break;
                case R.id.radio_imuser:
                    registerjson.put("userrole", "M");
                    break;
            }
            Log.e(TAG, registerjson.toString());
            registerjson.put("command","GetForgetPass");
            registerjson.put("tomail", "jason541219@hotmail.com");
            Log.e(TAG, registerjson.toString());
            registerjson.put("clinicnamewi", input_forgetclinic.getText().toString());
            registerjson.put("account", input_forgetaccount.getText().toString());
            Log.e(TAG, registerjson.toString());
            registerjson.put("pid5", "_____" +  input_forgetpid.getText().toString());
            registerjson.put("month", Integer.parseInt(input_forgetbirthday.getText().toString().substring(0, 2)));
            registerjson.put("date", Integer.parseInt(input_forgetbirthday.getText().toString().substring(2)));

//            registerjson.put("brd4", "____-" +input_forgetbirthday.getText().toString().substring(0, 2) + "-" + input_forgetbirthday.getText().toString().substring(2));
            registerjson.put("mbl5", "_____" + input_forgetmobile.getText().toString());
            Log.e(TAG, registerjson.toString());
            registerjson.put("rnk", input_forgetrank.getText().toString());
            registerjson.put("flr", input_forgetfloor.getText().toString());
            Log.e(TAG, registerjson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "GetForgetPass";
        HttpPost httpPost = new HttpPost(Activity_Forgetpass.this);
        httpPost.startPost(Activity_Forgetpass.this, GlobalVariables.http_url,  registerjson.toString());
    }

    // 檢查輸入項目
    private boolean checkinputfield() {
        boolean checkresult = true;
        int errorfieldid = 0;
        Pattern p_account = Pattern.compile("^[A-Za-z0-9]{6,12}$");         // ^ 起頭 $ 結尾
        Pattern p_pid5 = Pattern.compile("^[0-9]{5}$");
        Pattern p_brd4 = Pattern.compile("^[0-9]{4}$");
        Pattern p_rank = Pattern.compile("^[1-9]{1}[0-9]{0,1}$");
        Pattern p_floor = Pattern.compile("^[1-9]{1}[0-9]{0,2}$");

        // 生日檢查是否有選取 不需要判別
        if (!p_account.matcher(input_forgetaccount.getText().toString()).find()) {
            checkresult = false;    // Account
            errorfieldid = R.string.msg_registeraccount_error;
        } else if (!p_pid5.matcher(input_forgetpid.getText().toString()).find()) {
            checkresult = false;    // PID
            errorfieldid = R.string.msg_forgetpid_error;;
        } else if (!p_brd4.matcher(input_forgetbirthday.getText().toString()).find()) {
            checkresult = false;    // Mobile Phone
            errorfieldid = R.string.msg_forgetbirthday_error;
        } else if (!p_pid5.matcher(input_forgetmobile.getText().toString()).find()) {
            checkresult = false;    // email
            errorfieldid = R.string.msg_forgetmobile_error;
        } else if ((!input_forgetrank.getText().toString().equals("")) &&  !p_rank.matcher(input_forgetrank.getText().toString()).find()) {
            checkresult = false;    // email
            errorfieldid = R.string.msg_forgetrank_error;
        } else if ((!input_forgetfloor.getText().toString().equals("")) && !p_floor.matcher(input_forgetfloor.getText().toString()).find()) {
            checkresult = false;    // email
            errorfieldid = R.string.msg_forgetfloor_error;
        }

        if (!checkresult) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Forgetpass.this);
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

    @Override
    public void onComplete(String response) {
        try {
            Log.e(TAG, response);
            final JSONObject responseJson = new JSONObject(response);
            if (postKey.equals("GetForgetPass")) {
                if (responseJson.getString("status").equals("success")) {      //
                    Log.e(TAG, "success");
                    String yourpassis = Activity_Forgetpass.this.getResources().getString(R.string.dialog_msg_yourpassis) + "\n" + responseJson.getJSONArray("dblist").getJSONObject(0).getString("pass");
                    showAlert(yourpassis);
                } else {
                    showAlert(R.string.dialog_msg_forgetpassanswererror);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Forgetpass.this);
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