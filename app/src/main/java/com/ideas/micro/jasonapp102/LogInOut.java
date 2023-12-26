package com.ideas.micro.jasonapp102;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LogInOut{
    private final static String TAG = "Log_IO";
    private final static JSONObject activityname = new JSONObject();
    static {
        try {
            activityname.put("MainActivity", 1000);
            activityname.put("MainFunctionActivity", 1010);
            activityname.put("Activity_Privacy", 1020);
            activityname.put("Activity_Register", 1030);
            activityname.put("Activity_Forgetpass", 1040);
            activityname.put("Activity_Setting", 1050);

            activityname.put("Activity_ScanBLE", 2010);
            activityname.put("Activity_BPMprepare", 2020);
            activityname.put("Activity_BPM", 2030);
            activityname.put("Activity_PulseCut", 2040);
            activityname.put("Activity_BPMList", 2050);

            activityname.put("Activity_Myrecords", 3010);
            activityname.put("Activity_ChartWave", 3020);
            activityname.put("Activity_Analysis", 3030);

            activityname.put("Setting_ChangePass", 4010);
            activityname.put("Setting_RequestPush", 4020);
            activityname.put("Setting_AcceptFamily", 4030);
            activityname.put("Setting_PassQuestion", 4040);
            activityname.put("Setting_Alarm", 4050);
            activityname.put("Setting_MyQRCode", 4060);
            activityname.put("Setting_Me", 4070);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // 建構式
    public LogInOut(){
    }

    public static void log(String activityName, boolean InOut){
        if (GlobalVariables.Login_Role.equals("A")){    // 醫護人員
            log(activityName, SingleAdmin.getInstance().getAdminID(), "A", InOut);
        } else if (GlobalVariables.Login_Role.equals("M")){     // 用戶
            log(activityName, SingleUser.getInstance().getUserID(), "M", InOut);
        }
    }

    // 在MainActivity 人員ID 及 腳色尚未登入，所以會以 memid = 0, role = "X" 紀錄。
    public static void log(String activityName, int memid, String memrole, boolean InOut){
        // 建立一個新的執行緒，執行Runnable工作
        Log.e(TAG, InOut?"登入":"登出");
        return;
//        final int io = InOut?1:0;
//        final String _activityname = activityName;
//        final int _mid = memid;
//        final String _role = memrole;
//
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String result = "";
//                HttpURLConnection connection;
//                try {
//                    Calendar calendar = Calendar.getInstance();
//                    long timestamp = calendar.getTimeInMillis();
//                    String DateTimeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
//                    JSONObject postjson = new JSONObject();
//                    postjson.put("command", "LogInOut");
//                    postjson.put("activity", activityname.getInt(_activityname));
//                    postjson.put("timestamp", timestamp);
//                    postjson.put("time", DateTimeNow);
//                    postjson.put("IO", io);
//                    postjson.put("mid", _mid);
//                    postjson.put("role", _role);
////                    postjson.put("clinicnamewi", "內部測試");
//                    postjson.put("clinicnamewi", GlobalVariables.Login_Clinic);
//                    URL url = new URL(GlobalVariables.http_url);        // 定義在類別 GlobalVariable 的全域變數
//                    connection = (HttpURLConnection) url.openConnection();
//                    connection.setRequestMethod("POST");
//                    //connection.setRequestProperty("authentication", MainActivity.Authentication);
//                    connection.setDoInput(true);
//                    connection.setDoOutput(true);
//                    connection.setConnectTimeout(5000);
//                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
////                    StringBuilder stringBuilder = new StringBuilder();
////                    //stringBuilder.append(URLEncoder.encode(postdata, "UTF-8"));
////                    stringBuilder.append(postjson.toString());
////                    outputStream.writeBytes(stringBuilder.toString());
////                    outputStream.flush();
////                    outputStream.close();
//                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),
//                            "UTF-8"));
//                    bw.write(postjson.toString());
//                    bw.flush();
//                    int status = connection.getResponseCode();
//                    Log.e(TAG, "回應碼 Response Code : " + String.valueOf(status));
//                    bw.close();
//
//                    InputStream inputStream = connection.getInputStream();
//                    Log.e(TAG, "回應碼 Response Code : " + String.valueOf(status));
//                    if(inputStream != null){
//                        InputStreamReader reader = new InputStreamReader(inputStream,"UTF-8");
//                        BufferedReader in = new BufferedReader(reader);
//                        String line="";
//                        while ((line = in.readLine()) != null) {
//                            result += line;
//                        }
//                        Log.e(TAG, "回應內容 " + result);
//                        if (! result.equals("")) {
//                        } else {
//                            JSONObject responseempty = new JSONObject();
//                            responseempty.put("status", "exception");
//                            responseempty.put("result", "response empty");
//                        }
//                    } else{
//                        JSONObject responsenull = new JSONObject();
//                        responsenull.put("status", "exception");
//                        responsenull.put("result", "response null");
//                    }
//                } catch (Exception e) {
//                    Log.d(TAG, "Exception : " + e.getLocalizedMessage());
//                    e.printStackTrace();
//                    JSONObject responseexception = new JSONObject();
//                    try {
//                        responseexception.put("status", "exception");
//                        responseexception.put("result", e.getLocalizedMessage());
//                    } catch (JSONException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
//        thread.start();
    }
}