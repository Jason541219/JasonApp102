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
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

public class Setting_Me extends AppCompatActivity implements onHttpPostCallback {
    private static final int REQUEST_IMAGE_CAPTURE = 1799;
    private static final int CAMERA_REQUEST_CODE = 1770;
    final String TAG = "Me";
    private TextView tvTitle;
    private EditText r_username;
    private EditText r_useraccount;
    private EditText r_userpid;
    private EditText r_userbirthday;
    private EditText r_useremail;
    private EditText r_usermobile;
    private Spinner spinner_meclinic;
    private RadioGroup r_usergender;
    private Button btn_gome;
    private Button btn_cancelme;
    ImageView img_useravatar;
    DatePickerDialog datepicker;        // 日期選擇器
    String[] genderlist = {"M","F"};        //性別代碼 M = male 男, F=female 女
    private String postKey;
    private int clinic_id = 0;
    private JSONObject clinicID = new JSONObject();
    private JSONObject clinicName = new JSONObject();
    private ArrayList cliniclist = new ArrayList<String>();
    SingleUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_me);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.setting_me_title);
        user = SingleUser.getInstance();
        spinner_meclinic = (Spinner) findViewById(R.id.spinner_meclinic);
        r_username = (EditText) findViewById(R.id.input_mename);
        r_useraccount = (EditText) findViewById(R.id.input_meaccount);
        r_userpid = (EditText) findViewById(R.id.input_mepid);
        r_userbirthday =  (EditText) findViewById(R.id.input_mebirthday);
        r_useremail = (EditText) findViewById(R.id.input_meemail);
        r_usermobile =  (EditText) findViewById(R.id.input_memobile);
        r_usergender = (RadioGroup) findViewById(R.id.radio_megender);
        btn_gome = (Button) findViewById(R.id.btn_gome);
        btn_cancelme = (Button) findViewById(R.id.btn_cancelme);
        img_useravatar = (ImageView) findViewById(R.id.img_meavatar);
        img_useravatar.setImageResource(R.drawable.main_camera);     // 設定頭像圖案為照相機
        if (ContextCompat.checkSelfPermission(Setting_Me.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Setting_Me.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
        getClinicList();
        readUserData();

        // 選取診所
        spinner_meclinic.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String clinicname = cliniclist.get(pos).toString();
                try {
                    clinic_id = clinicID.getInt(clinicname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn_cancelme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();       //  離開 Setting_Me
            }
        });

        // 送出變更
        btn_gome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, r_userpid.getText().toString());
                if (! checkinputfield()) return;
                String usergender = "";
                //Log.e(TAG, "性別選項 position = " + r_usergender.getSelectedItemPosition());  position 從 0 開始
                JSONObject mejson = new JSONObject();
                try {
                    mejson.put("command", "MeMember");  // 會檢查帳號是否在 member 及 admin 中重複
                    mejson.put("clinicnamewi", GlobalVariables.Login_Clinic);
                    mejson.put("userid", user.getUserID());
                    mejson.put("useraccount", r_useraccount.getText().toString());
                    mejson.put("username", r_username.getText().toString());
                    mejson.put("serviceid", clinic_id);       // 根據選取的診所
                    mejson.put("userpid", r_userpid.getText().toString());
                    mejson.put("userbirthday", r_userbirthday.getText().toString());
                    mejson.put("useremail", r_useremail.getText().toString());
                    mejson.put("usermobile", r_usermobile.getText().toString());
//                    mejson.put("userqr", Utility.GetRandomString(20));        // 不顯示不修改QRCode
                    switch (r_usergender.getCheckedRadioButtonId()) {
                        case R.id.radio_memale:
                            usergender = "M";       // 男性
                            break;
                        case R.id.radio_mefemale:
                            usergender = "F";       // 女性
                            break;
                    }
                    mejson.put("usergender", usergender);
                    mejson.put("useravatar", encoded64);
//                    mejson.put("establisheddate", Utility.getDateTimeNow());        // 不顯示不修改資料建立日期
                    postKey = "MeMember";
                    HttpPost httpPost = new HttpPost(Setting_Me.this); // 記得要宣告  implements onHttpPostCallback
                    httpPost.startPost(Setting_Me.this, GlobalVariables.http_url,  mejson.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                String bday = user.getUserbirthday();
                int year = Integer.parseInt(bday.substring(0, 4));
                int month = Integer.parseInt(bday.substring(5, 7));
                int day = Integer.parseInt(bday.substring(8));
//            datepicker.updateDate(y, m, d);
//                final Calendar cldr = Calendar.getInstance();
//                int day = cldr.get(Calendar.DAY_OF_MONTH);
//                int month = cldr.get(Calendar.MONTH);
//                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                datepicker = new DatePickerDialog(Setting_Me.this,
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
        LogInOut.log("Setting_Me", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Setting_Me", false);
        Log.e(TAG, "onStop clear encoded64");
        encoded64 = "";
        if(imageBitmap != null && !imageBitmap.isRecycled()){
            imageBitmap.recycle();
            imageBitmap = null;
        }
    }


    private void readUserData(){
        r_username.setText(user.getUsername());
        r_useraccount.setText(user.getUseraccount());
        r_userpid.setText(user.getUserpid());
        String bday = user.getUserbirthday();
        if (! bday.equals("")){
            r_userbirthday.setText(bday);
        }
        r_useremail.setText(user.getUseremail());
        r_usermobile.setText(user.getUsermobile());
        if (user.getUsergender().equals("M")){      // 設定性別
            r_usergender.check(R.id.radio_memale);
        } else {
            r_usergender.check(R.id.radio_mefemale);
        }
        if (! user.getUseravatar().equals("")) {
            byte[] decodedString = Base64.decode(user.getUseravatar(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            img_useravatar.setImageBitmap(decodedByte);     // 設定頭像圖案為照相機
        } else {
            if (user.getUsergender().equals("M")){      // 設定性別
                img_useravatar.setImageResource(R.drawable.avatar_m);
            } else {
                img_useravatar.setImageResource(R.drawable.avatar_f);
            }
        }
        // 診所設定必須在 getClinicList的response 完成後設定

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
    private void clearmeField(){
        r_username.setText("");
        r_useraccount.setText("");
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
            checkresult = false;
            errorfieldid = R.string.dialog_msg_takeavatarbeforeregister;
        }

        if (!checkresult) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Setting_Me.this);
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


    private void getClinicList(){
        SingleAdmin admin = SingleAdmin.getInstance();
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command",  "GetClinicList_Register");
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "GetClinicList_Register";
        HttpPost httpPost = new HttpPost(Setting_Me.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Setting_Me.this, GlobalVariables.http_url,  jqljson.toString());
    }


    @Override
    public void onComplete(String response) {
        if (postKey.equals("GetClinicList_Register")) {
            Log.e(TAG, "response = " + response);
            getcliniclistresult(response);
        } else if (postKey.equals("RegisterMember")) {
            registermemberresult(response);
        }
    }
    @Override
    public void onFail(String err) {
        try {
            final JSONObject failJson = new JSONObject(err);
            if (failJson.getString("status").equals("exception")){      // notfound 表示沒有找到對應的帳號
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Setting_Me.this);
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
    private void registermemberresult(String response){
        try {
            final JSONObject completeJson = new JSONObject(response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String response_msg = "";
                    try {
                        if (completeJson.getString("status").equals("success")) {      //
                            response_msg = getResources().getString(R.string.dialog_msg_bpmuploadsuccess);
                        } else if (completeJson.getString("status").equals("fail")) {
                            if (completeJson.getString("result").equals("duplicate")) {
                                response_msg = getResources().getString(R.string.dialog_msg_registerduplicate);
                            } else {
                                response_msg = getResources().getString(R.string.dialog_msg_bpmuploaderror);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(Setting_Me.this);
                    final String finalResponse_msg = response_msg;
                    builder.setMessage(response_msg)
                            .setTitle(R.string.dialog_systemcomment)
                            .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (finalResponse_msg.equals(getResources().getString(R.string.dialog_msg_bpmuploadsuccess))) {
                                        clearmeField();
                                        Intent intent = new Intent(Setting_Me.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                    //setBtnBPMstopmeasueText(R.string.btn_nextmeasure);
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

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
                    cliniclist.add(dblist.getJSONObject(i).getString("serviceName"));
                    clinicName.put("" + dblist.getJSONObject(i).getString("serviceCenterID"), dblist.getJSONObject(i).getString("serviceName") );
                    clinicID.put(dblist.getJSONObject(i).getString("serviceName"), dblist.getJSONObject(i).getString("serviceCenterID") );
                }
                // 設定下拉式選單
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter typeAdapter = new ArrayAdapter(Setting_Me.this,
                                android.R.layout.simple_dropdown_item_1line, cliniclist);
                        //                     SpinnerArrayAdapter typeAdapter = new SpinnerArrayAdapter(Activity_BPMprepare.this, memlist);
                        //change the last argument here to your xml above.
                        spinner_meclinic.setAdapter(typeAdapter);
                        Log.e(TAG, "user.getServicecenterid() = " +user.getServicecenterid());
                        if (user.getServicecenterid() > 0) {      // 設定診所
                            try {
                                String scname = clinicName.getString("" + user.getServicecenterid());
                                Log.e(TAG, scname);
                                spinner_meclinic.setSelection(((ArrayAdapter) spinner_meclinic.getAdapter()).getPosition(scname));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Setting_Me.this);
                        String msg = null;
                        try {
                            msg = getResources().getString(R.string.dialog_msg_requestfail) + "\n" +  responseJson.getString("result");
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
}