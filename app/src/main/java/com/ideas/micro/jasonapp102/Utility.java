package com.ideas.micro.jasonapp102;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.commons.text.RandomStringGenerator;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class Utility {
    public static String getDateTimeNow(){
        Calendar calendar = Calendar.getInstance();
        String DateTimeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(calendar.getTimeInMillis()));
        return DateTimeNow;
    }
    public static String getDateNow(){
        return getDateTimeNow().substring(0, 10);
    }
    public static String getTimeNow(){
        return getDateTimeNow().substring(11);
    }

    // 距離現在幾天以後的日期
    public static String getDateAfter (int after) {
        Calendar calendar = Calendar.getInstance();
        long aftertimeinmillis = calendar.getTimeInMillis() + after * 24 * 60 * 60 * 1000L;
        String DateAfter = new SimpleDateFormat("yyyy-MM-dd").format(new Date(aftertimeinmillis));
        return DateAfter;
    }

    public static Boolean isTodayLaterThan(String latedate){
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd");
        Boolean islate = true;
        try {
            Date lated = spf.parse(latedate);
            Date thisdate = new Date();
            islate =  lated.compareTo(thisdate) < 0;        // 已經過期
        }catch (ParseException e) {

        }
        return islate;
    }

    public static void showAlert(Context context, int msg){
        showAlert(context, context.getResources().getString(msg));
    }

    public static void showAlert(Context context, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setTitle(R.string.dialog_systemcomment)
                .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 亂數文字字串 16 字母(大寫)
    public static String GetRandomString(int length) {
        // Generates a 20 code point string, using only the letters a-z
//        RandomStringGenerator generator = new RandomStringGenerator
//                .Builder().withinRange('a', 'z').build();
//        String randomLetters = generator.generate(length);
//        return randomLetters.toUpperCase();
        String basestr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int baselen = basestr.length();
//        Log.e("Util", "baselen = " + baselen);
        long timestamp = Calendar.getInstance().getTimeInMillis();
        Random random = new Random(timestamp);
        String randomstr = "";
        for (int i=0 ; i<length; i++){
//            Log.e("Util", "baselen = " + baselen);
            randomstr += basestr.charAt(random.nextInt(baselen));
        }
        return randomstr;
    }

    // 產生QRCode圖 Bitmap
    public static Bitmap GetQRCodeImage(String content, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        Bitmap bmp = null;
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (WriterException e)  {
            e.printStackTrace();
        }
        return bmp;
    }

    // 計算年紀
    public static int GetAge(String birthday){
        Calendar cal_now = Calendar.getInstance();
        int now_yyyy = cal_now.get(Calendar.YEAR);
        int now_mm = cal_now.get(Calendar.MONTH) + 1;  //取月份 - 月份是從0開始，+1成為一般人了解的月份
        int now_dd = cal_now.get(Calendar.DAY_OF_MONTH);
        int bir_yyyy = Integer.parseInt(birthday.substring(0, 4));
        int bir_mm = Integer.parseInt(birthday.substring(5, 7));
        int bir_dd = Integer.parseInt(birthday.substring(8, 10));
        int birthmonth = bir_yyyy * 12 + bir_mm;
        int nowmonth = now_yyyy * 12 + now_mm;
        int monthdif = 0;       // 月份差
        if (now_dd >= bir_dd) {
            monthdif = nowmonth - birthmonth;   // 今天大於等於生日  ->  表示今年已經過了生日
        } else {
            monthdif = nowmonth - birthmonth - 1;
        }
        return monthdif/12;
    }

    // 取得JSONArray 的最大值
    public static float Max_JSONArray(JSONArray ja){
        int len = ja.length();
        int max = Integer.MIN_VALUE;
        for (int i = 0 ; i< len; i++){
            try {
                max = max>ja.getInt(i)?max:ja.getInt(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return max;
    }
    // 取得JSONArray 的最小值
    public static float Min_JSONArray(JSONArray ja){
        int len = ja.length();
        int min = Integer.MAX_VALUE;
        for (int i = 0 ; i< len; i++){
            try {
                min = min<ja.getInt(i)?min:ja.getInt(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return min;
    }

    public static void showAlertDialog(Context context, int messageID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(messageID)
                .setTitle(R.string.dialog_systemcomment)
                .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 動作指示
    public static JSONObject setActionJSON(String _ver){
        JSONObject actionjson = new JSONObject();
        switch (_ver){
            case "v2":
                actionjson = setActionJSONv2();
                break;
            case "v4.5.1": case "v4.x": case "v3":
                actionjson = setActionJSONv451();
                break;
        }
        return actionjson;
    }

    // 血壓計狀態
    public static JSONObject setBpmStatusJSON(String _ver){
        JSONObject bpmsatusjson = new JSONObject();
        switch (_ver){
            case "v2":
                bpmsatusjson = setBpmstatusJSONv2();
                break;
            case "v4.5.1": case "v4.x": case "v3":
                bpmsatusjson = setBpmstatusJSONv451();
                break;
        }
        return bpmsatusjson;
    }

    // 血壓計階段
    public static JSONObject setBpmStageJSON(String _ver){
        JSONObject bpmstagejson = new JSONObject();
        switch (_ver){
            case "v2":
                bpmstagejson = setBpmstageJSONv2();
                break;
            case "v4.5.1": case "v4.x": case "v3":
                bpmstagejson = setBpmstageJSONv451();
                break;
        }
        return bpmstagejson;
    }


    // 以下為 Utility 內部函式
    // ***********************************************************************************************************
    @NotNull
    private static JSONObject setActionJSONv2(){
        JSONObject actionhintjson = new JSONObject();
        try {
            actionhintjson.put("00", R.string.actionhint_checkinflating);
            actionhintjson.put("01", R.string.actionhint_keepstill);
            actionhintjson.put("02", R.string.actionhint_pressMem);
            actionhintjson.put("10", R.string.actionhint_checkinflating);
            actionhintjson.put("11", R.string.actionhint_keepstill);
            actionhintjson.put("12", R.string.actionhint_keepstill);
            actionhintjson.put("13", R.string.actionhint_keepstill);
        } catch (JSONException e){
        }
        return actionhintjson;
    }

    @NotNull
    private static JSONObject setActionJSONv451(){
        JSONObject actionhintjson = new JSONObject();
        try {
            actionhintjson.put("00", R.string.bpmstatus_M0P0);
            actionhintjson.put("01", R.string.actionhint_checkinflating);
            actionhintjson.put("02", R.string.actionhint_keepstill);
            actionhintjson.put("03", R.string.actionhint_pressMem);
            actionhintjson.put("10", R.string.bpmstatus_M1P0);
            actionhintjson.put("11", R.string.actionhint_checkinflating);
            actionhintjson.put("12", R.string.actionhint_keepstill);
            actionhintjson.put("13", R.string.actionhint_keepstill);
            actionhintjson.put("14", R.string.bpmstatus_M1P3);//血壓波量測結束</string>
        } catch (JSONException e){
        }
        return actionhintjson;
    }


    @NotNull
    private static JSONObject setBpmstatusJSONv2(){
        JSONObject bpmstatusjson = new JSONObject();
        try {
            bpmstatusjson.put("00", R.string.bpmstatus_M0P0);
            bpmstatusjson.put("01", R.string.bpmstatus_M0P1);
            bpmstatusjson.put("02", R.string.bpmstatus_M0P2);
            bpmstatusjson.put("10", R.string.bpmstatus_M1P0);
            bpmstatusjson.put("11", R.string.bpmstatus_M1P1);
            bpmstatusjson.put("12", R.string.bpmstatus_M1P2);
            bpmstatusjson.put("13", R.string.bpmstatus_M1P3);
        } catch (JSONException e){
        }
        return bpmstatusjson;
    }

    @NotNull
    private static JSONObject setBpmstatusJSONv451(){
        JSONObject bpmstatusjson = new JSONObject();
        try {
            bpmstatusjson.put("00", R.string.bpmstatus_M0P0); //血壓量測開始</string>
            bpmstatusjson.put("01", R.string.bpmstatus_M0P3);//充氣中</string>
            bpmstatusjson.put("02", R.string.bpmstatus_M0P1);//血壓量測中...</string>
            bpmstatusjson.put("03", R.string.bpmstatus_M0P2);//血壓量測結束</string>
            bpmstatusjson.put("10", R.string.bpmstatus_M1P0);//血壓波量測開始</string>
            bpmstatusjson.put("11", R.string.bpmstatus_M0P3);//充氣中</string>
            bpmstatusjson.put("12", R.string.bpmstatus_M1P1);//血壓波(一)量測中...</string>
            bpmstatusjson.put("13", R.string.bpmstatus_M1P2);//血壓波(二)量測中..</string>
            bpmstatusjson.put("14", R.string.bpmstatus_M1P3);//血壓波量測結束</string>
        } catch (JSONException e){
        }
        return bpmstatusjson;
    }

    @NotNull
    private static JSONObject setBpmstageJSONv2(){
        JSONObject bpmstagejson = new JSONObject();
        try {
            bpmstagejson.put("00", 0);
            bpmstagejson.put("01", 1);
            bpmstagejson.put("02", 2);
            bpmstagejson.put("10", 3);
            bpmstagejson.put("11", 4);
            bpmstagejson.put("12", 5);
            bpmstagejson.put("13", 6);
        } catch (JSONException e){
        }
        return bpmstagejson;
    }




    @NotNull
    private static JSONObject setBpmstageJSONv451(){
        JSONObject bpmstagejson = new JSONObject();
        try {
            bpmstagejson.put("00", 0);
            bpmstagejson.put("01", 1);
            bpmstagejson.put("02", 2);
            bpmstagejson.put("03", 3);
            bpmstagejson.put("10", 4);
            bpmstagejson.put("11", 5);
            bpmstagejson.put("12", 6);
            bpmstagejson.put("13", 7);
            bpmstagejson.put("14", 8);
        } catch (JSONException e){
        }
        return bpmstagejson;
    }



    // ***********************************************************************************************************

}


