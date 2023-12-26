package com.ideas.micro.jasonapp102;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Float.isNaN;

public class Utility_Meridian {
    private static String TAG ="Utility_Meridian";
    private static String  reportColsStr = "{\"amp\":6, \"ampv\":9, \"ampir\":7, \"ampr\":8, \"phs\":10, \"phsv\":13, \"phsir\":11, \"phsr\":12}";

    // 取得最佳的切波段指數
    private static int umed_getMaxCutSectionIndex(String _fftstr){	// 先選切亂度最小 再選取切波最多 2021-09-02
        JSONObject fft_4array = new JSONObject();;
        float[] phsvvalue = new float[]{0,0,0,0};
        int  wavecutmax = -1;
        float phsvmin = 99999;
        String arraykey = "s0";
        int arrayindex = 0;
        try {
            fft_4array = new JSONObject(_fftstr.replaceAll("'", "\"")); // 要將單引號轉變為雙引號
            for (int s = 0; s < 4;  s++) {
                String skey = "s" + s;
                float phsv =(float) fft_4array.getJSONArray(skey).getDouble(18 + 13);        // H0 index 13 為phsv 相位變異
                phsvvalue[s] = phsv;
                if (phsv < phsvmin) phsvmin = phsv;
            }
            for (int i = 0; i < 4; i++) {
                if (phsvvalue[i] == phsvmin) {    // 亂度 = 最小亂度
                    String skey = "s" + i;
                    int cutnumber = fft_4array.getJSONArray(skey).getInt(0);
                    if (cutnumber >= wavecutmax) {
                        wavecutmax = cutnumber;
                        arrayindex = i;
                    }
                }
            }
        } catch (JSONException e){

        }
//        Log.e(TAG, "Best Cut Index " + arrayindex);
        return arrayindex;
    }

    public static JSONArray umed_getMaxCutSection(String _fftstr){
        JSONObject fft_4array = null;
        String arraykey = "s0";
        JSONArray fft_array = new JSONArray();
        try {
            fft_4array = new JSONObject(_fftstr.replaceAll("'", "\""));
            arraykey = "s" + umed_getMaxCutSectionIndex(_fftstr);
            fft_array =  fft_4array.getJSONArray(arraykey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fft_array;
    }


    // get AlertLight (根據血氧及脈診決定警示燈號)
    public static String umed_getAlertLight(int oxygen, String fftstr, int sbp, int dbp, int hr) {
//	alert("utility_meridian.umed_getAlertLight ");
        boolean isoverlimit = false;
        boolean isunderlimit = false;
        JSONArray fft_array = new JSONArray();
        if (fftstr.equals("")) {
            return "light_w.png";
        } else {
            fft_array =umed_getMaxCutSection(fftstr); // new JSONObject(fftstr.replaceAll("'", "\"")).getJSONArray("s3");		//      // 取用最後一組資料 s3 " +
            isoverlimit = umed_isOverLimit(fft_array) || bp_isOverLimit(sbp, dbp, hr);
            isunderlimit = umed_isUnderLimit(fft_array) && bp_isUnderLimit(sbp, dbp, hr);
            if (oxygen > 94) {
                return isoverlimit?"light_r.png":(isunderlimit?"light_g.png":"light_y.png");
            } else if (oxygen > 0 && oxygen <= 94){
                return "light_r.png";
            } else {	// no oxygen data
                return isoverlimit?"light_r.png":(isunderlimit?"light_g.png":"light_y.png");
            }
        }
    }

    // is Over MeridianLimit
    private static boolean umed_isOverLimit(JSONArray fft_array){
        // ampv, phsv(H1>5 , H4>7)
//    var meridiankeys = ['amp', 'ampv', 'ampir', 'ampr', 'phs', 'phsv', 'phsir', 'phsr'];
//    var meridianname = ['能量', '能量變異', '能量虛實', '能量比', '相位', '相位變異', '相位虛實', '相位比'];
//    var meridiancols = [6, 9, 7, 8, 10, 13, 11, 12];
        boolean isoverlimit = false;
        int colnum = 18;
        int ampvindex = 9, phsvindex = 13;
        int h1limit_h = 10, h4limit_h = 15;
        int h1limit_l = 3, h4limit_l = 5;
        try {
            boolean ampvh1 = fft_array.getDouble(colnum * 2 + ampvindex) >= h1limit_h;        
            boolean phsvh1 = fft_array.getDouble(colnum * 2 + phsvindex) >= h1limit_h;
            boolean ampvh4 = fft_array.getDouble(colnum * 5 + ampvindex) >= h4limit_h;
            boolean phsvh4 = fft_array.getDouble(colnum * 5 + phsvindex) >= h4limit_h;
            isoverlimit = ampvh1 || phsvh1 || ampvh4 || phsvh4;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isoverlimit;
    }

    //is Over MeridianLimit
    private static boolean umed_isUnderLimit(JSONArray fft_array){
        // ampv, phsv(H1>5 , H4>7)
//    var meridiankeys = ['amp', 'ampv', 'ampir', 'ampr', 'phs', 'phsv', 'phsir', 'phsr'];
//    var meridianname = ['能量', '能量變異', '能量虛實', '能量比', '相位', '相位變異', '相位虛實', '相位比'];
//    var meridiancols = [6, 9, 7, 8, 10, 13, 11, 12];
        boolean isunderlimit = false;
        int colnum = 18;
        int ampvindex = 9, phsvindex = 13;
        int h1limit_h = 10, h4limit_h = 15;
        int h1limit_l = 3, h4limit_l = 5;
        try {
            boolean ampvh1 = fft_array.getDouble(colnum * 2 + ampvindex) < h1limit_l;
            boolean phsvh1 = fft_array.getDouble(colnum * 2 + phsvindex) < h1limit_l;
            boolean ampvh4 = fft_array.getDouble(colnum * 5 + ampvindex) < h4limit_l;
            boolean phsvh4 = fft_array.getDouble(colnum * 5 + phsvindex) < h4limit_l;
            isunderlimit = ampvh1 && phsvh1 && ampvh4 && phsvh4;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isunderlimit;
    }

    //is BP OverLimit
    private static boolean bp_isOverLimit(int sbp, int dbp, int hr){
        boolean sbph = (sbp >= sbp_redover) || (sbp <= sbp_redunder);
        boolean dbph = (dbp >= dbp_redover) || (dbp <= dbp_redunder);
        boolean hrh = (hr >= hr_redover) || (hr <= hr_redunder);
        return sbph || dbph || hrh;
    }

    // is BP UnderLimit
    private static boolean bp_isUnderLimit(int sbp, int dbp, int hr){
        boolean sbpl =  (sbp < sbp_greentop) && (sbp >= sbp_greenbottom);
        boolean dbpl = (dbp < dbp_greentop) && (dbp >=dbp_greenbottom );
        boolean hrl = (hr < hr_greentop) && (hr > hr_greenbottom);
        return sbpl && dbpl && hrl;
    }

    public static int getSbpTextColor(int bp){
        int color = textYellow;
        if ( (bp >= sbp_redover) || (bp <= sbp_redunder)) {
            color = textRed;
        } else if ( (bp < sbp_greentop) && (bp >= sbp_greenbottom)){
            color = textGreen;
        }
        return color;
    }

    public static int getDbpTextColor(int bp){
        int color = textYellow;
        if ( (bp >= dbp_redover) || (bp <= dbp_redunder)) {
            color = textRed;
        } else if ( (bp < dbp_greentop) && (bp >= dbp_greenbottom)){
            color = textGreen;
        }
        return color;
    }

    public static int getHrTextColor(int hr){
        int color = textYellow;
        if ( (hr >= hr_redover) || (hr <= hr_redunder)) {
            color = textRed;
        } else if ( (hr < hr_greentop) && (hr >= hr_greenbottom)){
            color = textGreen;
        }
        return color;
    }

    private static int sbp_redover = 140, sbp_redunder = 90, sbp_greentop = 130, sbp_greenbottom = 120;
    private static int dbp_redover = 90, dbp_redunder = 60, dbp_greentop = 85, dbp_greenbottom = 80;
    private static int hr_redover = 96, hr_redunder = 60, hr_greentop = 80, hr_greenbottom = 65;
    private static int textRed = Color.rgb(255, 0,0);
    private static int textGreen = Color.rgb(0, 160, 0);
    private static int textYellow = Color.rgb(255, 208, 0);

    // 取的H0 到 H10 的資料
    public static JSONArray  umed_getHxList(String hxmode, String fftstr, String bestcut){
//        Log.e(TAG, "umed_getHxList");
        // H0  ampv, phsv 要乘以 100
        String  tablehtml = "";
        JSONArray resultarray = new JSONArray();
        double factor = 1;
        try {
            JSONObject umed_reportCols = new JSONObject(reportColsStr);
//            fft_4array = new JSONObject(_fftstr.replaceAll("'", "\""));
//            arraykey = "s" + umed_getMaxCutSectionIndex(_fftstr);
//            fft_array =  fft_4array.getJSONArray(arraykey);
//            JSONArray fft_array = umed_getMaxCutSection(fftstr);
            JSONArray fft_array = new JSONObject(fftstr.replaceAll("'", "\"")).getJSONArray("s" + bestcut);
            int col = umed_reportCols.getInt(hxmode);
//            Log.e(TAG, "col of " + hxmode + " = " + col);
            for (int i = 1; i <= 11; i++) {    // H0 - H10
                factor = 1;
                if (i == 1) {   // H0
                    if (hxmode.equals("amp")) {        // H0  振幅 要除以 100
                        factor = 0.01;
                    } else if (hxmode.equals("ampv") || hxmode.equals("phsv")) {        // H0  ampv, phsv 要乘以 100
                        factor = 100;
                    }
                }
                float valx = (float) (fft_array.getDouble(i * 18 + col) * factor);
                //		valx = valx==='0.0'?'0.0':(valx===' '?'--':(isNaN(valx)?valx:(valx.toFixed(2))));
                if (isNaN(valx)) {
                    Log.e(TAG, "valx is not number");
                    valx = 0;
                } else {
//                    valx = (valx * umed_getFormatFactorByColindex(i, col)).toFixed(umed_getFormatFixedByColindex(i, col));
                }
                resultarray.put(valx);
            }
        } catch (JSONException e) {

        }
        return resultarray;
    }
}
