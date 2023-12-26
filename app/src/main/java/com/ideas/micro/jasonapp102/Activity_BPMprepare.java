package com.ideas.micro.jasonapp102;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.ideas.micro.jasonapp102.database.AppDatabase;
import com.ideas.micro.jasonapp102.database.MDmember;
import com.ideas.micro.jasonapp102.database.MDmemberDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Activity_BPMprepare extends AppCompatActivity implements onHttpPostCallback {
    // Spinner
    //https://stackoverflow.com/questions/16693941/spinner-text-size-does-not-change/16694058
    //https://thumbb13555.pixnet.net/blog/post/323036806-spinner
    private final String TAG = "BPM準備";
    static final int REQUEST_IMAGE_CAPTURE = 2705;
    private final int CAMERA_REQUEST_CODE = 1070;
    private TextView tvTitle;
    private ImageButton btn_bemeasuredmember;
    private Spinner spinner_bemeasuredmember;           // 下拉式選單
    private Spinner spinner_deviceversion;
    private TextView txt_bemeasuredmemberdetail, txt_bemeasuredmember;
    private TextView txt_oxygensaturation, txt_oxygensaturationunit;
    private TextView txt_deviceversion;
    private RadioButton radio_position10, radio_position20;
    private RadioButton radio_posture10, radio_posture20;
    private RadioGroup radio_posture, radio_position;
    private EditText input_oxygensaturation;
    private ImageView img_bpmavatar;
    private Button ready2measure;
    private ProgressBar progressBar_bpmprepare;
    private int position = 0;
    private int posture = 0;
    private int oxygen = 0;
    private String nameaccount = "";    // 受測者帳號
    private String selectedmemname = "";
    private int mid = 0;
    private Bitmap imageBitmap = null;
    private String encoded64 = "";
    private Bundle _savedInstanceState;
    private String ClinicNamew;
    private String deviceversion = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bpmprepare);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_bpmprepare_title);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        btn_bemeasuredmember = (ImageButton) findViewById(R.id.btn_bemeasuredmember);
        spinner_bemeasuredmember = (Spinner) findViewById(R.id.spinner_bemeasuredmember);
        spinner_deviceversion = (Spinner) findViewById(R.id.spinner_deviceversion) ;
        spinner_deviceversion.setVisibility(View.GONE);
        txt_bemeasuredmember = (TextView) findViewById(R.id.txt_bemeasuredmember);
        txt_bemeasuredmemberdetail = (TextView) findViewById(R.id.txt_bemeasuredmemberdetail);
        txt_oxygensaturation = (TextView) findViewById(R.id.txt_oxygensaturation);
        txt_oxygensaturationunit = (TextView) findViewById(R.id.txt_oxygensaturationunit);
        input_oxygensaturation = (EditText) findViewById(R.id.input_oxygensaturation) ;
        txt_deviceversion = (TextView) findViewById(R.id.txt_deviceversion);
        txt_deviceversion.setText(getResources().getString(R.string.txt_deviceversion) + "    " + GlobalVariables.Device_Type);  // 血壓計版本 + 空格 + 版次
        radio_position10 = (RadioButton) findViewById(R.id.radio_position10);
        radio_position20 = (RadioButton) findViewById(R.id.radio_position20);
        radio_posture10 = (RadioButton) findViewById(R.id.radio_posture10);
        radio_posture20 = (RadioButton) findViewById(R.id.radio_posture20);
        radio_position = (RadioGroup) findViewById(R.id.radio_position);
        radio_posture = (RadioGroup) findViewById(R.id.radio_posture);
        img_bpmavatar = (ImageView) findViewById(R.id.img_bpmavatar);
        img_bpmavatar.setImageResource(R.drawable.face800);     // 設定頭像圖案為照相機
        ready2measure = (Button) findViewById(R.id.ready2measure);
        ready2measure.setEnabled(false);    // 等到搜尋到會員之後再開啟
        progressBar_bpmprepare = (ProgressBar) findViewById(R.id.progressBar_bpmprepare) ;
        progressBar_bpmprepare.setVisibility(View.GONE);

        getDeviceversionList();     // 設定血壓計版次
        deviceversion = deviceversionlist.get(0).toString();
        if (savedInstanceState!=null){

        }
//        this._savedInstanceState = savedInstanceState;
//        restoreSavedInstances(savedInstanceState);

        // 檢查相機授權
        if (ContextCompat.checkSelfPermission(Activity_BPMprepare.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Activity_BPMprepare.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }

        // 頭像區開啟相機
        img_bpmavatar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } else {
                    Log.e(TAG, "takePictureInent is  NULL");
                }
            }
        });

        spinner_deviceversion.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                deviceversion = deviceversionlist.get(pos).toString();
                Log.e(TAG, "選取血壓計版次 " + deviceversion);
                if (deviceversion.equals("v4.x")){      //  臨床版   修改血氧輸入說明
                    txt_oxygensaturation.setText("量測序號");
                    txt_oxygensaturationunit.setVisibility(View.GONE);
                } else {
                    txt_oxygensaturation.setText(R.string.txt_oxygensaturation);
                    txt_oxygensaturationunit.setVisibility(View.VISIBLE);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        //  Select Member to Measure  選取受測者
        spinner_bemeasuredmember.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                nameaccount = memlist.get(pos).toString();
                try {
                    selectedmemname = memname.getString(nameaccount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    mid = memid.getInt(nameaccount);
                    String genderstr = memgender.getString(nameaccount).equals("M")?
                            getResources().getString(R.string.radio_registermale):getResources().getString(R.string.radio_registerfemale);
                    txt_bemeasuredmemberdetail.setText(genderstr +  "(" + membirthday.getString(nameaccount) + " : " + memage.getInt(nameaccount) + getResources().getString(R.string.age) + ")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        //   Get All Member  長按取得所有個案
        txt_bemeasuredmember.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                switch (GlobalVariables.Login_Role){
                    case "A":
                        getMemberList("GetMemberList");
                    break;      // 所有的會員
                    case "M":getMyList();break;
                }
            }
        });

        // 刷新今天掛號的被測者
        btn_bemeasuredmember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (GlobalVariables.Login_Role){
                    case "A":getMemberList("GetBPMMeasureMemberList_APP");break;
                    case "M":getMyList();break;
                }
            }
        });

        // 開始量測
        ready2measure.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                position = getPosition();
                posture = getPosture();
                // v4.x 臨床版本
                if (input_oxygensaturation.getText().toString().equals("") && deviceversion.equals("v4.x")) {
                    Utility_Alert.showAlertDialog(Activity_BPMprepare.this, "請輸入測試序號",
                            R.string.dialog_OK, Utility_Alert.doNothing);
                    return;
                }
                try {
                    if (input_oxygensaturation.getText().toString().equals("")) {
                        input_oxygensaturation.setText("0");         // 不輸入就是零
                    }
                    oxygen = Integer.parseInt(input_oxygensaturation.getText().toString());     // 有可能出現例外
                    Log.e(TAG, "選取 position = " + position + " posture = " + posture + " memberID = " + mid);
                    Intent intent = null;
                    SharedPreferences msp = getSharedPreferences("meridianSharedPreferences", MODE_PRIVATE);
                    String mdevicename = msp.getString("btdevicename", "");

                    if (GlobalVariables.Device_Name.equals("BT-BPM BLE")) {
                        intent = new Intent(Activity_BPMprepare.this, Activity_BPM_Btbpm.class);
                    } else if (GlobalVariables.Device_Name.equals("OSTAR_BLE")) {
//                        intent = new Intent(Activity_BPMprepare.this, Activity_BPM_Ostar.class);
                    }
                    selectedmemname = memname.getString(spinner_bemeasuredmember.getSelectedItem().toString());

                    Log.e(TAG, "選取 " + selectedmemname);
                    intent.putExtra("position", position);
                    intent.putExtra("posture", posture);
                    intent.putExtra("gender", memgender.getString(spinner_bemeasuredmember.getSelectedItem().toString()));
                    intent.putExtra("age", memage.getInt(spinner_bemeasuredmember.getSelectedItem().toString()));
                    intent.putExtra("oxygen", oxygen);
                    intent.putExtra("memberid", mid);
                    intent.putExtra("avatar", encoded64);
                    intent.putExtra("memname", selectedmemname);
                    intent.putExtra("deviceversion", GlobalVariables.Device_Type.substring(9));    // v2, v3, v4.5.1
                    startActivity(intent); // william this is used to change to another page
                } catch (NumberFormatException e) {
                    if (deviceversion.equals("v4.x")) {
                        Utility_Alert.showAlertDialog(Activity_BPMprepare.this, "測試序號輸入錯誤",
                                R.string.dialog_OK, Utility_Alert.doNothing);
                    } else {
                        Utility_Alert.showAlertDialog(Activity_BPMprepare.this, R.string.dialog_msg_oxygenparseIntFail,
                                R.string.dialog_OK, Utility_Alert.doNothing);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });             // end of ready2measure


                // 暫時關閉強制照相功能
//                if (encoded64.equals("")) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_BPMprepare.this);
//                    String msg = null;
//                    builder.setMessage(R.string.dialog_msg_takeavatarbeforebpm)
//                            .setTitle(R.string.dialog_systemcomment)
//                            .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    // 不需要做事情
//                                }
//                            });
//                    AlertDialog dialog = builder.create();
//                dialog.setCanceledOnTouchOutside(false);
//                    dialog.show();
//                } else {



    }       // end of onCreate

    private int getPosition(){
        int posi = 0;
        switch (radio_position.getCheckedRadioButtonId()) {
            case R.id.radio_position10:
                posi = 10;              // 左手
                break;
            case R.id.radio_position20:
                posi = 20;              // 右手
                break;
        }
        return posi;
    }

    private int getPosture(){
        int posu = 0;
        switch (radio_posture.getCheckedRadioButtonId()) {
            case R.id.radio_posture10:
                posu = 10;               // 坐姿
                break;
            case R.id.radio_posture20:
                posu = 20;               // 臥姿
                break;
        }
        return posu;
    }

    private void restoreSavedInstances(Bundle savedInstanceState){
        if (savedInstanceState != null){
            Log.e(TAG, "onCreate savedInstanceState !=null");
            if (savedInstanceState.getInt("position") == 10){
                radio_position.check(R.id.radio_position10);
            } else {
                radio_position.check(R.id.radio_position20);
            }
            if (savedInstanceState.getInt("posture") == 10){
                radio_posture.check(R.id.radio_posture10);
            } else {
                radio_posture.check(R.id.radio_posture20);
            }
            Log.e(TAG, "nameaccount = " +savedInstanceState.getString("nameaccount") );
            nameaccount = savedInstanceState.getString("nameaccount");
        } else {
            Log.e(TAG, "onCreate savedInstanceStates =null");
        }
    }

    //  儲存Activity狀態
    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("position", getPosition());
        savedInstanceState.putInt("posture", getPosture());
        savedInstanceState.putString("nameaccount", spinner_bemeasuredmember.getSelectedItem().toString());
        Log.e(TAG, "選取 at onSaveInstanceState" + spinner_bemeasuredmember.getSelectedItem().toString());
        _savedInstanceState = savedInstanceState;
    }

    @Override
    protected void onStart() {
        // 拍照完畢之後 也會回到 onStart  -> 不可以清空 encoded64  以免相片消失
        super.onStart();
        LogInOut.log("Activity_BPMprepare", true);
        Log.e(TAG, "onStart");
        // 設定選單
        if (GlobalVariables.onLine) {   // 連線時
            switch (GlobalVariables.Login_Role) {
                case "A":
                    getMemberList("GetBPMMeasureMemberList_APP");
                    break;    // 今天掛號的
                case "M":
                    getMyList();
                    break;
            }
        } else {        // 離線時
            memid = new JSONObject();   // 清空
            memage = new JSONObject();
            memgender = new JSONObject();
            membirthday = new JSONObject();
            new Thread(getallmdmember).start();
        }
        if (imageBitmap == null) {
            img_bpmavatar.setImageResource(R.drawable.face800);     // 設定頭像圖案為照相機
        } else {
            img_bpmavatar.setImageBitmap(imageBitmap);
        }
        restoreSavedInstances(_savedInstanceState);
        if (!nameaccount.equals("")) {
            spinner_bemeasuredmember.setSelection(((ArrayAdapter) spinner_bemeasuredmember.getAdapter()).getPosition(nameaccount));
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Activity_BPMprepare", false);
        Log.e(TAG, "onStop clear encoded64");
        encoded64 = "";
        if(imageBitmap != null && !imageBitmap.isRecycled()){
            imageBitmap.recycle();
            imageBitmap = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) { // 要求授權的回應
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
        }
    }

    // 取得頭像
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE  && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            img_bpmavatar.setImageBitmap(imageBitmap);
            // convert bitmap to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            // encode base64 from byte array
            encoded64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.e(TAG, "相片長度 = " + encoded64.length());
        }
    }


    private ArrayList deviceversionlist = new ArrayList<String>();
    // 個案使用時只顯示自己的資料
    private void getDeviceversionList() {
        deviceversionlist.clear();
        deviceversionlist.add("v2");
        deviceversionlist.add("v4.5.1");
        // 設定下拉式選單
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter versionAdapter = new ArrayAdapter(Activity_BPMprepare.this,
                        R.layout.style_spinner,deviceversionlist);
                spinner_deviceversion.setAdapter(versionAdapter);
            }
        });
    }

    // 個案使用時只顯示自己的資料
    private void getMyList() {
        SingleUser user = SingleUser.getInstance();
        spinner_deviceversion.setVisibility(View.GONE);
            memlist.clear();    // 清除
            nameaccount = user.getUsername() + "(" + user.getUseraccount() + ")";
            memlist.add(nameaccount);
            try {
                memid.put(nameaccount, user.getUserID());
                memname.put(nameaccount, user.getUsername());
                memgender.put(nameaccount, user.getUsergender());
                memage.put(nameaccount, Utility.GetAge(user.getUserbirthday()));   // 紀錄年紀
                membirthday.put(nameaccount, user.getUserbirthday());  // 紀錄生日
                selectedmemname = user.getUsername();       // 選取的姓名
                deviceversion = user.getBpmtype();
            } catch (JSONException e){

            }
        // 設定下拉式選單
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
                        R.layout.style_spinner,memlist);
//                ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
//                        android.R.layout.simple_dropdown_item_1line, memlist);'
                spinner_bemeasuredmember.setAdapter(typeAdapter);
                restoreSavedInstances(_savedInstanceState);
                ready2measure.setEnabled(true);
            }
        });
    }

    // 將 JSONAarray memdblsit 寫到資料庫
    private Runnable saveallmdmember = new Runnable() {
        @Override
        public void run() {
            MDmemberDao _mdmemdao = AppDatabase.getInstance(Activity_BPMprepare.this).getMDmemberDao();
            List<MDmember> _mdmemlist = _mdmemdao.getmemberjson();
            if (_mdmemlist.size() == 0){
                _mdmemdao.insertOneMem(memdblist.toString());
            } else {
                _mdmemdao.updateOneMem(memdblist.toString());
            }
        }
    };

    // 從資料庫中取得列表
    private Runnable getallmdmember = new Runnable() {
        @Override
        public void run() {
            memdblist = new JSONArray();
            MDmemberDao _mdmemdao = AppDatabase.getInstance(Activity_BPMprepare.this).getMDmemberDao();
            List<MDmember> _mdmemlist = _mdmemdao.getmemberjson();
            if (_mdmemlist.size() > 0){
                memlist.clear();
                try {
                    String mdstr = _mdmemlist.get(0).mdmemberjson;
                    memdblist = new JSONArray(mdstr);
                    for (int i = 0; i < memdblist.length(); i++) {
                        nameaccount = memdblist.getJSONObject(i).getString("memberName") + "(" + memdblist.getJSONObject(i).getString("memberAccount") + ")";
                        memlist.add(nameaccount);
                        memid.put(nameaccount, memdblist.getJSONObject(i).getString("memberID"));
                        memname.put(nameaccount, memdblist.getJSONObject(i).getString("memberName"));
                        memgender.put(nameaccount, memdblist.getJSONObject(i).getString("memberGender"));
                        memage.put(nameaccount, Utility.GetAge(memdblist.getJSONObject(i).getString("memberBirthday")));   // 紀錄年紀
                        membirthday.put(nameaccount, memdblist.getJSONObject(i).getString("memberBirthday"));  // 紀錄生日
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
                            R.layout.style_spinner,memlist);
//                ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
//                        android.R.layout.simple_dropdown_item_1line, memlist);'
//                    ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
//                            android.R.layout.simple_dropdown_item_1line, memlist);
//                     SpinnerArrayAdapter typeAdapter = new SpinnerArrayAdapter(Activity_BPMprepare.this, memlist);
                    //change the last argument here to your xml above.
                    spinner_bemeasuredmember.setAdapter(typeAdapter);
                    restoreSavedInstances(_savedInstanceState);
                    ready2measure.setEnabled(true);
                }
            });
        }
    };

    // 到資料庫找親屬相關 (目前用不到)
    private void getFamilyList(){
        MedGlobalStates g = (MedGlobalStates)MedGlobalStates.getInstance();
        int t = g.getData();
        String clinicNamew = g.getClinicName();
        if(t != 217){
            //we should go to the 1st page and re-login
        }
        SingleUser user = SingleUser.getInstance();
        int userID = user.getUserID();
        Log.e(TAG, "使用者 ID = " + userID);
        memlist = new ArrayList<String>();
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command",  "getFamilyList");
            jqljson.put("versionTag", "1.1.0");
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            jqljson.put("uid",  userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "GetFamilyList";
        memid = new JSONObject();   // 清空
        memname = new JSONObject();
        memage = new JSONObject();
        memgender = new JSONObject();
        membirthday = new JSONObject();
        progressBar_bpmprepare.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        HttpPost httpPost = new HttpPost(Activity_BPMprepare.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_BPMprepare.this, GlobalVariables.http_url, jqljson.toString());
    }

    private void getMemberList(String command){
//        MedGlobalStates g = (MedGlobalStates)MedGlobalStates.getInstance();
//        int t = g.getData();
//        String clinicNamew = g.getClinicName();
//        if(t != 217){
//            //we should go to the 1st page and re-login
//        }

        SingleAdmin admin = SingleAdmin.getInstance();
        memlist = new ArrayList<String>();
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command",  command);
            jqljson.put("versionTag", "1.1.0");
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            jqljson.put("level", admin.getAdminlevel());
            jqljson.put("date", Utility.getDateNow());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "GetBPMMeasureMemberList_APP";
        memid = new JSONObject();   // 清空
        memname = new JSONObject();
        memage = new JSONObject();
        memgender = new JSONObject();
        membirthday = new JSONObject();
        progressBar_bpmprepare.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        HttpPost httpPost = new HttpPost(Activity_BPMprepare.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_BPMprepare.this, GlobalVariables.http_url,  jqljson.toString());
    }

    private String postKey;     // 用來辨識
    private JSONObject memid = new JSONObject();
    private JSONObject memage = new JSONObject();
    private JSONObject memgender = new JSONObject();
    private JSONObject membirthday = new JSONObject();
    private JSONObject memname = new JSONObject();
    private ArrayList memlist = new ArrayList<String>();
    private JSONArray memdblist = new JSONArray();

    @Override
    public void onComplete(String response) {
//        Log.e(TAG, "會員列表 at onComplete = " + response);
        try {
            if (postKey.equals("GetBPMMeasureMemberList_APP") || postKey.equals("GetFamilyList") || postKey.equals("GetMemberList")) {
                final JSONObject responseJson = new JSONObject(response);
                if (responseJson.getString("status").equals("success")) {      //
                    memdblist = new JSONArray();
                    memdblist = responseJson.getJSONArray("dblist");
                    new Thread(saveallmdmember).start();
                    for (int i = 0; i < memdblist.length(); i++) {
                        String nameaccount = memdblist.getJSONObject(i).getString("memberName") + "(" + memdblist.getJSONObject(i).getString("memberAccount") + ")";
//                        memlist.add(dblist.getJSONObject(i).getString("memberName") + "");
//                        memid.put(dblist.getJSONObject(i).getString("memberName"), dblist.getJSONObject(i).getString("memberID"));
//                        memgender.put(dblist.getJSONObject(i).getString("memberName"), dblist.getJSONObject(i).getString("memberGender"));
//                        memage.put(dblist.getJSONObject(i).getString("memberName"), Utility.GetAge(dblist.getJSONObject(i).getString("memberBirthday")));   // 紀錄年紀
//                        membirthday.put(dblist.getJSONObject(i).getString("memberName"), dblist.getJSONObject(i).getString("memberBirthday"));  // 紀錄生日
                        memlist.add(nameaccount);
                        memname.put(nameaccount, memdblist.getJSONObject(i).getString("memberName"));
                        memid.put(nameaccount, memdblist.getJSONObject(i).getString("memberID"));
                        memgender.put(nameaccount, memdblist.getJSONObject(i).getString("memberGender"));
                        memage.put(nameaccount, Utility.GetAge(memdblist.getJSONObject(i).getString("memberBirthday")));   // 紀錄年紀
                        membirthday.put(nameaccount, memdblist.getJSONObject(i).getString("memberBirthday"));  // 紀錄生日
                    }
                    // 設定下拉式選單
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar_bpmprepare.setVisibility(View.GONE);
                            // 恢復觸控功能
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
                                    R.layout.style_spinner, memlist);
//                            ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
//                                    android.R.layout.simple_dropdown_item_1line, memlist);
                            typeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                            spinner_bemeasuredmember.setAdapter(typeAdapter);
                            restoreSavedInstances(_savedInstanceState);
                            ready2measure.setEnabled(true);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar_bpmprepare.setVisibility(View.GONE);
                            // 恢復觸控功能
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(Activity_BPMprepare.this);
                            String msg = null;
                            msg = getResources().getString(R.string.dialog_msg_nomemberregistertoday);
                            memlist.clear();    // 清空下拉式選單
                            ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
                                    R.layout.style_spinner, memlist);
//                            ArrayAdapter typeAdapter = new ArrayAdapter(Activity_BPMprepare.this,
//                                    android.R.layout.simple_dropdown_item_1line, memlist);
                            spinner_bemeasuredmember.setAdapter(typeAdapter);
                            
                            ready2measure.setEnabled(false);
                            builder.setMessage(msg)
                                    .setTitle(R.string.dialog_systemcomment)
                                    .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // 不需要做事情
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                        }
                    });
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
        try {
            final JSONObject failJson = new JSONObject(err);
            if (failJson.getString("status").equals("exception")){      // notfound 表示沒有找到對應的帳號
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar_bpmprepare.setVisibility(View.GONE);
                        // 恢復觸控功能
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_BPMprepare.this);
                        String msg = null;
                        try {
                            msg = getResources().getString(R.string.dialog_msg_systemerror) + "\n" + failJson.getString("result");
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
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}