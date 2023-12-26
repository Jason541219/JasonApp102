package com.ideas.micro.jasonapp102;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class GlobalVariables {
    public static boolean isTest = false;
    public static boolean isClinicalVersion = false;        // 臨床版本
    public static String appversion = "";
    public static boolean onLine = true;        // default Connect to Network
    public static boolean isEnclypted = false;      // 在取得AESKey 之前 不加密
    public static boolean memberaccount = true;
    public static JSONObject urljson = new JSONObject();
    public static boolean isAutoFillAccount = false;
    public static boolean isBindMAC = true;
    public static boolean isMHBonline = false;
    public static boolean isReportToday = false;
    public static boolean isHxLightEnable = false;
    public static int nextMHBdays = 90;
    public static int reportColumns = 1;

    public static void settingParam(String settingparams){
        try {
            JSONObject settingparamjson = new JSONObject(settingparams);
            Log.e("Global", settingparams);
            isAutoFillAccount = settingparamjson.getInt("isAutoFillAccount") == 1;
            isBindMAC = settingparamjson.getInt("isBindMAC") == 1;
            isMHBonline = settingparamjson.getInt("isMHBonline") == 1;
            isReportToday = settingparamjson.getInt("isReportToday") == 1;
            isHxLightEnable = settingparamjson.getInt("isHxLightEnable") == 1;
            nextMHBdays = settingparamjson.getInt("nextMHBdays");
            reportColumns = settingparamjson.getInt("reportColumns");
            Log.e("Global", "GlobalVariables.isHxLightEnable = " + GlobalVariables.isHxLightEnable );
        } catch (JSONException e) {
            Log.e("Global", "JSON Error " + e.toString());
        }

    }
    private static String meridianmain = "MeridianMain_Enc";
    private static String subscript = "payment/bpm_subscript.jsp?";
    // 允許的藍芽血壓計名稱 (Ostar 有需要再改)
    public static ArrayList<String> allowedBLE = new ArrayList(Arrays.asList("BT-BPM BLE", "Ostar"));
//    public static final String main_url = "https://www.micro2ssi.com/MeridianMain";

//    public static final String main_url_6911= "https://results.echainmedhealth.com:6911/EChainMed/MeridianMain_Enc";
    public static final String main_url_aws = "https://www.echainpulse.com/EChainMed/";
    public static final String mhb_url_aws = "https://test.echainpulse.com/EChainMedExt/";
    public static final String main_url_awstest = "https://test.echainpulse.com/EChainMed/";
//    public static final String main_url_6912= "https://results.echainmedhealth.com:6912/EChainMed/MeridianMain_Enc";
    //    public static final String main_url = "http://localhost:8080/MedMultiClinic/MeridianMain";
    //    public static final String main_url = "http://192.168.99.119:8080/MedMultiClinic/MeridianMain";
    //    public static final String main_url = "http://10.0.2.2:8080/MedMultiClinic/MeridianMain";
    //    public static final String main_url_localhost_clinic= "http://192.168.2.128:8080/Meridian102/MeridianMain";     //  當代漢醫苑

    public static final String main_url_localhost_office = "http://192.168.0.129:8080/EChainMed/";
    public static final String main_url_localhost_home = "http://192.168.0.21:8080/EChainMed/";
    public static final String main_url_localhost_echain = "http://192.168.10.130:8080/EChainMed/";
    public static final String main_url_localhost_echain302 = "http://192.168.1.101:8080/EChainMed/";
    //    public static final String main_url_localhost_p2 = "http://192.168.10.103:8080/EChainMed/MeridianMain";

    private static String main_url = main_url_aws;

    public static String http_url = main_url +meridianmain ;       // 之後由資料庫appVersion 讀取 dbPath
    public static String mhb_url = mhb_url_aws;
    public static String subscript_url = main_url + subscript;

    public static String Login_Role= "";        // A or M
    public static String Login_Clinic = "";
    public static String Device_Name = "";
    public static String Device_Address = "";
    public static String Device_Type = "";

    public static Pattern p_account = Pattern.compile("^[A-Za-z0-9]{6,30}$");         // ^ 起頭 $ 結尾
    public static Pattern p_pass = Pattern.compile("^[A-Za-z0-9]{6,12}$");         // ^ 起頭 $ 結尾
    public static Pattern p_clinic = Pattern.compile("^[\\u4e00-\\u9fa5A-za-z0-9]{2,12}");
    public static Pattern p_pid = Pattern.compile("^[A-Z][0-9]{9}$");
    public static Pattern p_mobile = Pattern.compile("^09[0-9]{8}$");
    public static Pattern p_email = Pattern.compile("^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z]+$");

    public static String getEchainMedDomain() {
        if (GlobalVariables.isTest)
            return "https://test.echainpulse.com";
        else
            return "https://www.echainpulse.com";
    }
}
