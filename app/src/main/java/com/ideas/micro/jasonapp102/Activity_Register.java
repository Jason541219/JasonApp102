package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

public class Activity_Register extends AppCompatActivity implements onHttpPostCallback {
    static final int REQUEST_IMAGE_CAPTURE = 1399;
    private static final int CAMERA_REQUEST_CODE = 1070;
    final String TAG = "REGISTER";
    private TextView tvTitle;
    TextView label_registername;
    EditText r_username;
    EditText r_useraccount;
    EditText r_userpass;
    EditText r_userpid;
    EditText r_userbirthday;
    EditText r_useremail;
    EditText r_usermobile;
    private Spinner spinner_registerclinic;
    RadioGroup r_usergender;
    Button btn_register;
    ProgressBar progressBar_register;
    DatePickerDialog datepicker;        // 日期選擇器
    ImageView img_useravatar;
    String[] genderlist = {"M","F"};        //性別代碼 M = male 男, F=female 女
    private String postKey;
    private int clinic_id = 0;
    private String clinic_db = "";
    private JSONObject clinicID = new JSONObject();
    private JSONObject clinicPosition = new JSONObject();
    private ArrayList cliniclist = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_register_title);
        label_registername = (TextView) findViewById(R.id.label_registername);
        spinner_registerclinic = (Spinner) findViewById(R.id.spinner_registerclinic);
        r_username = (EditText) findViewById(R.id.input_registername);
        r_useraccount = (EditText) findViewById(R.id.input_registeraccount);
        r_userpass =  (EditText) findViewById(R.id.input_registerpass);
        r_userpid = (EditText) findViewById(R.id.input_registerpid);
        r_userbirthday =  (EditText) findViewById(R.id.input_registerbirthday);
        r_useremail = (EditText) findViewById(R.id.input_registeremail);
        r_usermobile =  (EditText) findViewById(R.id.input_registermobile);
        r_usergender = (RadioGroup) findViewById(R.id.radio_registergender);
        btn_register = (Button) findViewById(R.id.btn_goregister);
        progressBar_register = (ProgressBar) findViewById(R.id.progressBar_register);
        progressBar_register.setVisibility(View.GONE);
        img_useravatar = (ImageView) findViewById(R.id.img_registeravatar);
        img_useravatar.setImageResource(R.drawable.main_camera);     // 設定頭像圖案為照相機
        if (ContextCompat.checkSelfPermission(Activity_Register.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Activity_Register.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
        getClinicList();

        label_registername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "Label Name onClick");
                r_username.setText("測試A");
                r_useraccount.setText("TesterA");
                r_userpass.setText("12345678");
                r_userpid.setText("X123456789");
                r_userbirthday.setText("2020-01-01");
                r_useremail.setText("jason541219@hotmail.com");
                r_usermobile.setText("0912345678");
                r_usergender.check(R.id.radio_registerfemale);
                try {
                    spinner_registerclinic.setSelection(clinicPosition.getInt("益謙"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        spinner_registerclinic.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
              public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                  String clinicname = cliniclist.get(pos).toString();
                  try {
//                      clinic_id = clinicID.getInt(clinicname);
                      clinic_db = clinicID.getString(clinicname);
                  } catch (JSONException e) {
                      e.printStackTrace();
                  }
              }
              @Override
              public void onNothingSelected(AdapterView<?> adapterView) {

              }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkDuplication(clinic_db, r_useraccount.getText().toString());    // check Account Available
            }
        });


        // 頭像區開啟相機
        img_useravatar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } else {
                    encoded64 = "";
                    Log.e(TAG, "takePictureInent is  NULL");
                }
            }
        });

        // 點選生日 EditText 彈出日期選擇器
        r_userbirthday.setInputType(InputType.TYPE_NULL);
        r_userbirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                datepicker = new DatePickerDialog(Activity_Register.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String mn = monthOfYear < 10?("0"+monthOfYear):String.valueOf(monthOfYear);
                                String dd = dayOfMonth < 10?("0" + dayOfMonth):String.valueOf(dayOfMonth);
                                r_userbirthday.setText(year + "-" + mn + "-" + dd);     // yyyy-mm-dd
                            }
                        }, year, month, day);
                datepicker.show();
            }
        });
    }       // end of onCreate

    protected void onStart() {
        super.onStart();
        LogInOut.log("Activity_Register", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Activity_Register", false);
        Log.e(TAG, "onStop clear encoded64");
        encoded64 = "";
        if(imageBitmap != null && !imageBitmap.isRecycled()){
            imageBitmap.recycle();
            imageBitmap = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private Bitmap imageBitmap = null;
    private String encoded64 = "";
    // 照相回應
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == this.REQUEST_IMAGE_CAPTURE  && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            img_useravatar.setImageBitmap(imageBitmap);
            // convert bitmap to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            // encode base64 from byte array
            encoded64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.e(TAG, "相片長度 = " + encoded64.length());
        }
    }
    // 清除欄位內容
    private void clearRegisterField(){
        r_username.setText("");
        r_useraccount.setText("");
        r_userpass.setText("");
        r_userpid.setText("");
        r_userbirthday.setText("");
        r_useremail.setText("");
        r_usermobile.setText("");
        encoded64 = "";
        img_useravatar.setImageResource(R.drawable.main_camera);     // 設定頭像圖案為照相機
    }

    private boolean checkinputfield() {  // 檢查輸入項目
        boolean checkresult = true;
        int errorfieldid = 0;
        Pattern p_account = Pattern.compile("^[A-Za-z0-9]{6,12}$");         // ^ 起頭 $ 結尾
        Pattern p_pass = Pattern.compile("^[A-Za-z0-9]{6,12}$");         // ^ 起頭 $ 結尾
        Pattern p_pid = Pattern.compile("^[A-Z][0-9]{9}$");
        Pattern p_mobile = Pattern.compile("^09[0-9]{8}$");
        Pattern p_email = Pattern.compile("^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z]+$");
        // 生日檢查是否有選取 不需要判別
        if (!GlobalVariables.p_account.matcher(r_useraccount.getText().toString()).find()) {
            checkresult = false;    // Account
            errorfieldid = R.string.msg_registeraccount_error;
        } else if (!GlobalVariables.p_pass.matcher(r_userpass.getText().toString()).find()) {
            checkresult = false;    // Password
            errorfieldid = R.string.msg_registerpass_error;
        } else if (!GlobalVariables.p_pid.matcher(r_userpid.getText().toString()).find()) {
            checkresult = false;    // PID
            errorfieldid = R.string.msg_registerpid_error;;
        } else if (!GlobalVariables.p_mobile.matcher(r_usermobile.getText().toString()).find()) {
            checkresult = false;    // Mobile Phone
            errorfieldid = R.string.msg_registermobile_error;
        } else if (!GlobalVariables.p_email.matcher(r_useremail.getText().toString()).find()) {
            checkresult = false;    // email
            errorfieldid = R.string.msg_registeremail_error;
        }  else if (r_userbirthday.getText().toString().equals("")) {
            checkresult = false;    // birthday check empty only
            errorfieldid = R.string.msg_registerbirthday_error;
        } else if (encoded64.equals("")){
//            checkresult = false;
//            errorfieldid = R.string.dialog_msg_takeavatarbeforeregister;
        }

        if (!checkresult) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Register.this);
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

    // Check Account Available
    private void checkDuplication(String clinicname, String account) {
        JSONObject registerjson = new JSONObject();
        try {
            registerjson.put("command", "CheckDuplication_Account");  // 會檢查帳號是否在 member 及 admin 中重複
            registerjson.put("clinicnamewi", clinicname);
            registerjson.put("account", account);
            registerjson.put("role", "M");
            postKey = "CheckDuplication_Account";
            Log.e(TAG, registerjson.toString());
            progressBar_register.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            HttpPost httpPost = new HttpPost(Activity_Register.this); // 記得要宣告  implements onHttpPostCallback
            httpPost.startPost(Activity_Register.this, GlobalVariables.http_url,  registerjson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void registerTempAccount (){
        Log.e(TAG, r_userpid.getText().toString());
        if (! checkinputfield()) return;
        String usergender = "";
        //Log.e(TAG, "性別選項 position = " + r_usergender.getSelectedItemPosition());  position 從 0 開始
        JSONObject registerjson = new JSONObject();
        try {
            registerjson.put("command", "RegisterMember_APP");
            registerjson.put("clinicnamewi", "RootDB");
            registerjson.put("registerCode", Utility.GetRandomString(20));
            registerjson.put("clinicname", clinic_db);
            registerjson.put("useraccount", r_useraccount.getText().toString());
            registerjson.put("username", r_username.getText().toString());
//                    registerjson.put("serviceid", clinic_id);       // 根據選取的診所
            registerjson.put("userpass", r_userpass.getText().toString());
            registerjson.put("userpid", r_userpid.getText().toString());
            registerjson.put("userbirthday", r_userbirthday.getText().toString());
            registerjson.put("useremail", r_useremail.getText().toString());
            registerjson.put("usermobile", r_usermobile.getText().toString());
            registerjson.put("userqr", Utility.GetRandomString(20));
            switch (r_usergender.getCheckedRadioButtonId()) {
                case R.id.radio_registermale:
                    usergender = "M";       // 男性
                    break;
                case R.id.radio_registerfemale:
                    usergender = "F";       // 女性
                    break;
            }
            registerjson.put("usergender", usergender);
            registerjson.put("useravatar", encoded64);
            registerjson.put("establisheddate", Utility.getDateTimeNow());
            postKey = "RegisterMember_APP";
            Log.e(TAG, registerjson.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar_register.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
            HttpPost httpPost = new HttpPost(Activity_Register.this); // 記得要宣告  implements onHttpPostCallback
            httpPost.startPost(Activity_Register.this, GlobalVariables.http_url,  registerjson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

        private void getClinicList(){
        SingleAdmin admin = SingleAdmin.getInstance();
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command",  "GetClinicList_Register");
            jqljson.put("clinicnamewi", "RootDB");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "GetClinicList_Register";
        HttpPost httpPost = new HttpPost(Activity_Register.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_Register.this, GlobalVariables.http_url,  jqljson.toString());
    }


    @Override
    public void onComplete(String response) {
        Log.e(TAG, "onComplete " + response);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar_register.setVisibility(View.GONE);
                // 恢復觸控功能
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

            if (postKey.equals("GetClinicList_Register")) {
                getcliniclistresult(response);
            } else if (postKey.equals("RegisterMember_APP")) {
                Log.e(TAG, response);
                registerMemberResult(response);
            } else if (postKey.equals("CheckDuplication_Account")) {
                try {
                    JSONObject respjson = new JSONObject(response);
                    if (respjson.getString("status").equals("success")) {  // 帳號沒有衝突
                        Log.e(TAG, "帳號沒有衝突");
                        registerTempAccount();
                    } else if (respjson.getString("status").equals("fail")) { // 帳號衝突 提示
                        Log.e(TAG, "帳號重複");
                        showDuplicateAlert();
                    } else {
                        showDBerrAlert();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
    }
    @Override
    public void onFail(String err) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar_register.setVisibility(View.GONE);
                // 恢復觸控功能
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
        Log.e(TAG, err);
        try {
            final JSONObject failJson = new JSONObject(err);
            if (failJson.getString("status").equals("exception")){      // notfound 表示沒有找到對應的帳號
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Register.this);
                        String msg = null;
                        try {
                            msg = failJson.getString("result");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 會員註冊結果
    private void registerMemberResult(String response){
        try {
            final JSONObject completeJson = new JSONObject(response);
                if (completeJson.getString("status").equals("success")) {      //
                    showCertificateMailAlert();
                } else if (completeJson.getString("status").equals("fail")) {
                    showDBerrAlert();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    // 診所列表結果
    private void getcliniclistresult(String response){
        try {
            final JSONObject responseJson = new JSONObject(response);
            if (responseJson.getString("status").equals("success")){      //
                JSONArray dblist = responseJson.getJSONArray("dblist");
                for (int i=0; i<dblist.length(); i++){
                    cliniclist.add(dblist.getJSONObject(i).getString("name"));
                    clinicID.put(dblist.getJSONObject(i).getString("name"), dblist.getJSONObject(i).getString("db"));
                    clinicPosition.put(dblist.getJSONObject(i).getString("name"), i);
                }
                // 設定下拉式選單
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter typeAdapter = new ArrayAdapter(Activity_Register.this,
                                android.R.layout.simple_dropdown_item_1line, cliniclist);
    //                     SpinnerArrayAdapter typeAdapter = new SpinnerArrayAdapter(Activity_BPMprepare.this, memlist);
                        //change the last argument here to your xml above.
                        spinner_registerclinic.setAdapter(typeAdapter);
                    }
                });
            } else {
                showDBerrAlert();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 顯示驗證信件
    // 顯示帳號重複
    private void showCertificateMailAlert(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                String mailcontent = "註冊驗證信件已經寄出，請到您設定的信箱收取(請同時檢查垃圾信件收件夾)。"
                Utility_Alert.showAlertDialog(Activity_Register.this, R.string.dialog_msg_certificationmailhassendtou,
                        R.string.dialog_OK,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                clearRegisterField();
                                Intent intent = new Intent(Activity_Register.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                );
            }
        });
    }

    // 顯示帳號重複
    private void showDuplicateAlert(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utility_Alert.showAlertDialog(Activity_Register.this, R.string.dialog_msg_registerduplicate,
                        R.string.dialog_OK, Utility_Alert.doNothing);
            }
        });
    }

    // 顯示資料庫錯誤
    private void showDBerrAlert() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utility_Alert.showAlertDialog(Activity_Register.this, R.string.dialog_msg_requestfail,
                        R.string.dialog_OK, Utility_Alert.doNothing);
            }
        });
    }
}