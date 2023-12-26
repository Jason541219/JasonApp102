package com.ideas.micro.jasonapp102;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nhi.mhbsdk.MHB;

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Helper_MHB  implements onHttpPostCallback {
    private Context context;
    private AppCompatActivity activity;
    private String TAG = "MHB輔助";
//    public static String MHB_API_KEY = "34be1fed16d84f9eb8b62f7fb966d01d";
    public static String MHB_API_KEY = "";

    private MHB mhb = null;
    private String  postkey = "";
    private SingleUser singleUser = SingleUser.getInstance();
    private Utility_ActivityAlert activityAlert;
    private HttpPost httpPost;

    // 建構式
    public Helper_MHB (AppCompatActivity activity) {
        this.activity = activity;
        activityAlert = new Utility_ActivityAlert(activity);
        httpPost = new HttpPost(Helper_MHB.this);
        getApiKey();    // -> 取得 API KEY 就登入
    }

    public void getApiKey(){
        Log.e(TAG, "getApiKey");
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("memberID", singleUser.getUserID());
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            jqljson.put("app", "android"); //ios
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postkey = "GetApiKey";
        httpPost.startPost(activity, GlobalVariables.mhb_url + "GetAPIKey",  jqljson.toString());
    }

    //
    MHB.FetchDataCallback fetchcallback = new MHB.FetchDataCallback() {
        @Override
        public void onFetchDataSuccess(FileInputStream fileInputStream, String serverKey) {
            try {
                Log.e(TAG, "讀取資料成功 onFetchDataSuccess");
                int memberID = SingleUser.getInstance().getUserID();
                String clinicName = GlobalVariables.Login_Clinic;
                String fileName = clinicName + "_" + memberID + ".zip";
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
                Map<String, String> fields = new HashMap<>();
                fields.put("clinicName", clinicName);
                fields.put("memberId", String.valueOf(memberID));
                fields.put("serverKey", serverKey);
                fields.put("app", "android");
                postkey = "PostBack";
                httpPost.setUrl(GlobalVariables.getEchainMedDomain() + "/EChainMedExt/MHBRecord");
                httpPost.startPostWithFile(activity, GlobalVariables.mhb_url + "MHBRecord" , fields, headers, "zipFile", fileName, fileInputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onFetchDataFailure(String errorCode) {
            //回傳Error Code
            Log.i(TAG, "讀取資料失敗 Error Code : " + errorCode);
            //如果errorCode是204
            if (errorCode.equals("204")) {
                //等候60秒後重新parse資料
                try {
                    Toast.makeText(activity, "等待健康存摺產製資料", Toast.LENGTH_LONG).show();
                    Thread.sleep(60 * 1000L);
                    parseMHBFileTicket();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(activity, "讀取資料失敗，請重新操作 (" + errorCode + ")", Toast.LENGTH_LONG).show();
                String errMsg = getMHBErrMsg(errorCode);
                if (!errMsg.equals("")) {
                    activityAlert.showAlertDialog(errMsg, R.string.dialog_OK, activityAlert.doNothing);
                }
            }

        }
    };

    MHB.StartProcCallback startcallback = new MHB.StartProcCallback() {
        @Override
        public void onStarProcSuccess() {
            Log.e(TAG, "啟動健康存摺模組成功");
        }

        @Override
        public void onStartProcFailure(String errorCode) {
            String errMsg = getMHBErrMsg(errorCode);
            if (!errMsg.equals("")) {
                Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "啟動健康存摺模組失敗 Error Code : " + errorCode + "\n" + errMsg + "\n請洽系統人員");
            }
        }
    };

     //取得解壓縮密碼
    private String getMHBDecriptKey(String serverKey) {
        byte[] bytes = serverKey.getBytes(StandardCharsets.US_ASCII);
        SecretKeyFactory factory = null;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
        }
        KeySpec spec = new PBEKeySpec(MHB_API_KEY.toCharArray(), bytes, 1000, 256);
        SecretKey tmp = null;
        try {
            tmp = factory.generateSecret(spec);
        } catch (InvalidKeySpecException e2) {
            e2.printStackTrace();
        }
        String finalkey = Base64.encodeToString(tmp.getEncoded(), Base64.DEFAULT).trim();
        return finalkey;
    }

    //解析健康存摺產製的檔案
    public void parseMHBFileTicket() {
        SharedPreferences sharedPreferences =activity.getSharedPreferences(activity.getPackageName(),
                Activity.MODE_PRIVATE);

        //列出檔案識別碼遞回查尋
        Map<String, ?> allFiles = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allFiles.entrySet()) {
            String fName = entry.getKey();
            Log.e(TAG, "parseMHBFileTicket: fName:" + fName);
            if (fName.startsWith("File_Ticket_")) { //檢查已下載的資料，檔案開頭是File_Ticket_
                Log.i(TAG, "已產製資料或產製中:" + fName);
                Toast.makeText(activity, "取得檢康存摺產製資料", Toast.LENGTH_LONG).show();
                mhb.fetchData(fName, fetchcallback);
            }
        }
    }


    //解析健康存摺產製的檔案
    private void parseMHBFileTicket2() {
        SharedPreferences sharedPreferences =activity.getSharedPreferences(activity.getPackageName(),
                Activity.MODE_PRIVATE);

        //列出檔案識別碼遞回查尋
        Map<String, ?> allFiles = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allFiles.entrySet()) {
            String fName = entry.getKey();
            if (fName.startsWith("File_Ticket_")) { //檢查已下載的資料，檔案開頭是File_Ticket_
                Log.e(TAG, "已產製資料:" + fName);
//                Toast.makeText(activity, "取得檢康存摺產製資料", Toast.LENGTH_LONG).show();

                mhb.fetchData(fName, new MHB.FetchDataCallback() {
                    @Override
                    public void onFetchDataSuccess(FileInputStream fis, String serverKey) { //存取檔案時會得到serverKey
                        try {
                            //取得解壓用的密碼
                            String decryptKey = getMHBDecriptKey(serverKey);

                            //解開zip檔案
                            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis), decryptKey.toCharArray());
                            LocalFileHeader fileHeader = null;
                            String content = "";

                            while((fileHeader = zis.getNextEntry()) != null){ //檢視每個檔案(應該只會有一個 xxxx.json)
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];
                                int count;

                                String filename = fileHeader.getFileName();

                                Log.i(TAG, "filename:" + filename);
                                Toast.makeText(activity, "解壓產製資料進行中", Toast.LENGTH_LONG).show();

                                // reading and writing
                                while((count = zis.read(buffer)) != -1){
                                    baos.write(buffer, 0, count);
                                    byte[] bytes = baos.toByteArray();

                                    content += new String(bytes, StandardCharsets.UTF_8);
                                    baos.reset();
                                }

//                                Log.i(TAG, content.replaceAll(
//                                        System.getProperty("line.separator").toString(), "").replaceAll("\r", ""));

//                                Toast.makeText(MainFunctionActivity.this, "解析後的資料:" + content, Toast.LENGTH_LONG).show();

                                //回傳給server
                                postBackMHBData(content.replaceAll(
                                        System.getProperty("line.separator").toString(), "").replaceAll("\r", ""));
                            }

                            zis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFetchDataFailure(String errorCode) {
                        //回傳Error Code
                        Log.i(TAG, "讀取資料失敗 Error Code : " + errorCode);

                        String errMsg = Helper_MHB.getMHBErrMsg(errorCode);

                        if (!errMsg.equals("")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle("提示").setMessage(errMsg);

                            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                            builder.create().show();

//                            Toast.makeText(MainFunctionActivity.this, errMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        }
    }



    //後送資料
    private void postBackMHBData(String content) {
//        Toast.makeText(activity, "開始儲存健康存摺資料", Toast.LENGTH_LONG).show();
////        Log.i(TAG, "clinic:" + GlobalVariables.Login_Clinic);
        try {
//            PackageManager pm = this.getPackageManager();
//            PackageInfo pInfo = pm.getPackageInfo(this.getPackageName(), 0);
//            String versionname = pInfo.versionName;
//
            int memberID = singleUser.getUserID();
            JSONObject jqljson = new JSONObject();
//            Log.e(TAG, GlobalVariables.http_url);
////        GlobalVariables.http_url = GlobalVariables.main_url;
//
            jqljson.put("memberID", memberID);
            jqljson.put("app", "Android");
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            jqljson.put("data", content);
            jqljson.put("serverkey", "");
            httpPost.startPost(activity, GlobalVariables.mhb_url + "MHBRecord",  jqljson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onComplete(String response) {
        try {
            final JSONObject completeJson = new JSONObject(response);
            if (postkey.equals("GetApiKey")) {
                if (completeJson.getString("status").equals("success")) {
                    Log.e(TAG, completeJson.getString("result"));
                    MHB_API_KEY = completeJson.getString("result");
                            mhb = MHB.configure(activity, MHB_API_KEY); // 初始化
                            if (mhb !=null) mhb.startProc(startcallback);
                } else if (completeJson.getString("status").equals("fail")) {
                    MHB_API_KEY = "";
                    activityAlert.showAlertDialog("Fail Get API Key",
                            R.string.dialog_OK, activityAlert.doNothing);
                }
            } else if (postkey.equals("PostBack")) {
                if (completeJson.getString("status").equals("success")) {
                    activityAlert.showAlertDialog("健保資料上傳成功",
                            R.string.dialog_OK, activityAlert.doNothing);
                } else if (completeJson.getString("status").equals("fail")) {
                    MHB_API_KEY = "";
                    activityAlert.showAlertDialog("健保資料上傳失敗，請重新再試",
                            R.string.dialog_OK, activityAlert.doNothing);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Looper.prepare();
        Log.i(TAG, response);
        try {
            JSONObject responseJson = new JSONObject(response);
            String responseStatus = responseJson.getString("status");
            String responseExecTime = responseJson.getString("exec_time");
            String responseResult = responseJson.getString("result");
            Toast.makeText(activity, responseResult, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Looper.loop();
    }

    @Override
    public void onFail(String err) {
        Looper.prepare();
        Toast.makeText(activity, "資料傳送失敗", Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    // 啟動健康存摺
    public DialogInterface.OnClickListener mhb_launch = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            //啟動健康存摺
//            recordMHBReportDate();
            mhb.startProc(new MHB.StartProcCallback() {
                @Override
                public void onStarProcSuccess() { //當啟動完成時呼叫 (是否存在5張沒過時（不超過24小時）的 File_Ticket或存在5 個檔案)
                    Log.e(TAG, "成功啟動健康存摺模組");
                }
                @Override
                public void onStartProcFailure(String errorCode) { //只要不成功的都是失敗
                    String errMsg = Helper_MHB.getMHBErrMsg(errorCode);
                    if (!errMsg.equals("")) {
                        Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "啟動健康存摺模組失敗 Error Code : " + errorCode + "\n請洽系統人員");
                    }
                }
            });
        }
    };

    private void recordMHBReportDate(){
            JSONObject sqljson = new JSONObject();
            try {
                sqljson.put("command", "RecordMHBReportDate");
                sqljson.put("mid", SingleUser.getInstance().getUserID());
                sqljson.put("date", Utility.getDateAfter(GlobalVariables.nextMHBdays));     // 下次下載健康存摺的日期
                sqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
                HttpPost httpPost = new HttpPost(Helper_MHB.this);
                httpPost.startPost(activity,  GlobalVariables.http_url, sqljson.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }


    //健康存摺資料錯誤代碼(部分)
    public static String getMHBErrMsg(String errorCode) {
        String errMsg = "";
        switch (errorCode) {
            case "098":
                errMsg = "無網路可使用，請確認是否開啟";
                break;
            case "099":
                errMsg = "系統忙碌中，請稍後再試";
                break;
            case "201":
                errMsg = "儲存空間不足";
                break;
            case "204":
                errMsg = "檔案尚未產製完成";
                break;
        }
        return errMsg;
    }


}
