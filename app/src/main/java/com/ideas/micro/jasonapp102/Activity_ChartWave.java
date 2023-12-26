package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Activity_ChartWave extends AppCompatActivity implements onHttpPostCallback {
    private final String TAG = "波形圖表";
    private TextView tvTitle;
    private LinearLayout pulsewavelayout;
    private ImageView img_waveavatar;
    private ProgressBar progressBar_chart;
    private Button btn_readfftreport;
    private TextView text_charttime, text_chartpressure;
    private TextView text_bpwave, text_pulsewave1, text_pulsewave2;
    private DrawView bpDrawView, pulseDrawView;
    private int FFT_flag = 0;
    private String FFT_report;
    private long recordID;
    private int dochasread;
    private int position, posture;
    private int sbp, dbp, hr;
    private String rdate, rtime, bestcut;
    private JSONArray bpwave ;
    private JSONArray p1wave ;
    private JSONArray p2wave ;
    private Bitmap imageBitmap=null;
    SingleUser user = SingleUser.getInstance();
    Utility_ActivityAlert activityAlert = new Utility_ActivityAlert(this);
    Helper_MHB helper_mhb = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Activity_ChartWave onCreate");
        setContentView(R.layout.activity_chartwave);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_chartwave_title);
        btn_readfftreport = (Button) findViewById(R.id.btn_readfftreport);
        text_charttime = (TextView) findViewById(R.id.text_charttime);
        text_chartpressure = (TextView) findViewById(R.id.text_chartpressure);
        text_bpwave = (TextView) findViewById(R.id.text_bpwave);
        text_pulsewave1 = (TextView) findViewById(R.id.text_pulsewave1);
        text_pulsewave2 = (TextView) findViewById(R.id.text_pulsewave2);
        text_bpwave.setText(R.string.bpwavename_color);
//        text_pulsewave1.setText(R.string.pulsewave1_color);       // hidden
//        text_pulsewave2.setText(R.string.pulsewave2_color);       // hidden
        progressBar_chart = (ProgressBar) findViewById(R.id.progressBar_chart);
        progressBar_chart.setVisibility(View.GONE);
        int ispayuser = user.getIspayuser();
        if (ispayuser < 200) btn_readfftreport.setEnabled(false);   // case 0: case 50: case 100: case 150:

        // 讀取經脈報告
        btn_readfftreport.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String today = Utility.getDateNow();
                if (FFT_flag != 1) {    // FFT_flag == 0 (unable fft) OR FFT_flag == 2 (still calculating.....)
                    activityAlert.showAlertDialog(R.string.dialog_msg_fftreportnotready, R.string.dialog_OK, activityAlert.doNothing);
                } else if (dochasread == 1 ) {  // 已經讀取
                    openReport();
                } else if (GlobalVariables.isMHBonline &&
                        (user.getMHBReportDate().equals("") || Utility.isTodayLaterThan(user.getMHBReportDate()))   ) {     // 已經超過期限
                    activityAlert.showAlertDialog(R.string.dialog_msg_mhbaccessnoted,
                            R.string.dialog_IAgree, new Helper_MHB(Activity_ChartWave.this).mhb_launch,
                            R.string.dialog_NextTime, activityAlert.doNothing);
                } else if (GlobalVariables.isReportToday && !today.equals(rdate)){     // 您只能讀取今日量測的報告
                    activityAlert.showAlertDialog(R.string.dialog_msg_reporttodayonly,
                            R.string.dialog_OK, activityAlert.doNothing);
                } else {
                    switch (user.getIspayuser()) {
                        case 200:
                        case 250:     // 每日一次
                            if (user.getUserhasread().equals(today + "-01")) {
                                // 你今天讀取報告的額度已經超過
                                activityAlert.showAlertDialog(
                                        R.string.dialog_msg_readreportover1, R.string.dialog_OK, activityAlert.doNothing);
                            } else {
                                setHasReadReport(recordID, today + "-01");
                                user.setUserhasread(today + "-01");
                                user = SingleUser.getInstance();
                                openReport();
                            }
                            break;
                        case 300:
                        case 350:     // 每日兩次
                            if (user.getUserhasread().equals(today + "-02")) {
                                // 你今天讀取報告的額度已經超過
                                activityAlert.showAlertDialog(
                                        R.string.dialog_msg_readreportover2, R.string.dialog_OK, activityAlert.doNothing);
                            } else if (user.getUserhasread().equals(today + "-01")) {
                                setHasReadReport(recordID, today + "-02");
                                user.setUserhasread(today + "-02");
                                user = SingleUser.getInstance();
                                openReport();
                            } else {
                                setHasReadReport(recordID, today + "-01");
                                user.setUserhasread(today + "-01");
                                user = SingleUser.getInstance();
                                openReport();
                            }
                            break;
                    }
                    }
            }   // end of  onClick
        });     // end Of  btn_readfftreport.Click

        img_waveavatar = (ImageView) findViewById(R.id.img_waveavatar);
        pulsewavelayout = (LinearLayout) findViewById(R.id.pulsewavelayout);
        pulseDrawView = new DrawView(this, "pulse");
        pulseDrawView.setMinimumHeight(500);
        pulseDrawView.setMinimumWidth(300);
        pulseDrawView.invalidate();      // 通知 drawView組件重繪
        pulsewavelayout.addView(pulseDrawView);


        // 連線資料庫取的 Wave
        Intent getintent = getIntent();
        recordID = getintent.getLongExtra("recordID", -1);
        dochasread = getintent.getIntExtra("hasread", 0);
        if (recordID > 0) {
            getWave(recordID);
        } else {
            // recordID 不正確
            activityAlert.showAlertDialog(R.string.dialog_msg_recordiderror, R.string.dialog_OK, activityAlert.doNothing);
        }
        Log.e(TAG, "recordID = " +recordID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            bpwave = new JSONArray("[]");
            p1wave = new JSONArray("[]");
            p2wave = new JSONArray("[]");
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        LogInOut.log("Activity_ChartWave", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        LogInOut.log("Activity_ChartWave", false);
        if(imageBitmap != null && !imageBitmap.isRecycled()){   // 清除 avatar
            imageBitmap.recycle();
            imageBitmap = null;
        }
    }

    private void setFFTreport(JSONObject fftjson){
        try {
            FFT_flag = fftjson.getInt("fftflag");
            FFT_report = fftjson.getString("fft");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 開啟報告頁面 Activity_Analysis_1
    private void openReport() {
        Intent intent = new Intent(Activity_ChartWave.this, Activity_Analysis_1.class);
        intent.putExtra("FFT_report", FFT_report);      // FFT_report  = recordFFT
        intent.putExtra("bestCut", bestcut);
        intent.putExtra("position", position);
        intent.putExtra("posture", posture);
        intent.putExtra("sbp", sbp);
        intent.putExtra("dbp", dbp);
        intent.putExtra("hr", hr);
        intent.putExtra("date", rdate);
        intent.putExtra("time", rtime);
        Log.e(TAG, "進入分析報告");
        startActivity(intent);
    }

    private void setWaveChart(JSONObject wavejson){
        try {
            if (wavejson.has("avatar")) {
                getAvatarImage(wavejson.getString("avatar"));
            }
            if (wavejson.has("bpwave")){ bpwave = new JSONArray( wavejson.getString("bpwave"));}
//            if (wavejson.has("p1wave")){ p1wave = new JSONArray( wavejson.getString("p1wave"));}
//            if (wavejson.has("p2wave")){ p2wave = new JSONArray( wavejson.getString("p2wave"));}
            Log.e(TAG, "pulseDrawView.invalidate()");
            pulseDrawView.invalidate();      // 通知 drawView組件重繪

        } catch (JSONException e) {
            activityAlert.showAlertDialog(R.string.dialog_msg_bpwavearrayerror, R.string.dialog_OK, activityAlert.doNothing);
            Log.e(TAG, "setWaveChart Error " +  e.toString());
        }
    }

    public void getAvatarImage(String base64Str) throws IllegalArgumentException {
        if (!base64Str.equals("")) {
            byte[] decodedBytes = Base64.decode(
                    base64Str.substring(base64Str.indexOf(",") + 1),
                    Base64.DEFAULT
            );
            imageBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            img_waveavatar.setImageBitmap(imageBitmap);
        }
    }

    // **** 繪圖類別區域 **** //
    private int bpmax = 0, bpmin = Integer.MAX_VALUE;
    private int p1max = 0, p1min = Integer.MAX_VALUE;
    private int p2max = 0, p2min = Integer.MAX_VALUE;

    // 自訂繪圖類別
    public  class DrawView extends View {
        Canvas canvas;
        private String charttype = "";
        private int xshift, yshift;
        private int chartW, chartH;
        private float xspan, yspan;
        private int Ymin, Ymax, Xmin, Xmax;

        // constructor 建構式
        public DrawView(Context context, String charttype){
            super(context);
            this.charttype = charttype;
            Ymin = 8000000; Ymax = 8500000;       // 首次設定
            Xmin = 0; Xmax = 1220;
        }

        private void setParameter(Canvas canvas){
            xshift = 10;            yshift = 10;
            //Ymin = 8000000; Ymax = 8500000;       由藍牙波段設定
            chartW = canvas.getWidth() - xshift - xshift;
            chartH = canvas.getHeight() - yshift - yshift;
            xspan = Float.valueOf(chartW) / (Xmax - Xmin);
            yspan = Float.valueOf(chartH) / (Ymax - Ymin);
        }

        @Override
        protected void onDraw(Canvas canvas) {
//            int p1length = 0, p2length = 0;
//            int p1max = 0, p1min = Integer.MAX_VALUE;
//            int p2max = 0, p2min = Integer.MAX_VALUE;
//            int p1value = 0, p2value = 0;
//            int ymin0 = 0, ymax0 = 0;

            super.onDraw(canvas);
            Paint p = new Paint();								// 創建畫筆
            p.setAntiAlias(true);									// 設置畫筆的鋸齒效果。 true是去除。
            p.setColor(Color.RED);								// 設置紅色
            drawBackFrame(canvas, p);
            drawGrid(canvas, p);
            //drawWave(canvas, p, wavetoshow);
//            try {
//                if (charttype.equals("bp")) {
//                    p1length = bpwave.length();
//                    Xmax = p1length;
//                    for (int i = 0; i < p1length-1; i++) {
//                        p1value = bpwave.getInt(i);
//                        if (p1value > p1max) p1max = p1value;
//                        if (p1value < p1min) p1min = p1value;
//                    }
//                    ymin0 = p1min;
//                    ymax0 = p1max;
//                    Ymin = (int) (ymin0 - (ymax0 - ymin0) * 0.1);
//                    Ymax = (int) (ymax0 + (ymax0 - ymin0) * 0.1);
//                    Log.e(TAG, "BPWave Ymin = " + Ymin + " Ymax = " + Ymax);
//                    setParameter(canvas);
//                    drawWavearray(canvas, p, bpwave, "#00FF00");
//                    drawOutsideFrame(canvas, p);
//                } else if (charttype.equals("pulse")) {
//                    p1length = p1wave.length();
//                    p2length = p2wave.length();
//                    Xmax = p1length > p2length ? p1length : p2length;
//                    //Xmax = 10 * 488;
//                    for (int i = 0; i < p1length-1; i++) {
//                        p1value = p1wave.getInt(i);
//                        if (p1value > p1max) p1max = p1value;
//                        if (p1value < p1min) p1min = p1value;
//                    }
//                    for (int i = 0; i < p2length-1; i++) {
//                        p2value = p2wave.getInt(i);
//                        if (p2value > p2max) p2max = p2value;
//                        if (p2value < p2min) p2min = p2value;
//                    }
//                    ymin0 = p1min < p2min ? p1min : p2min;
//                    ymax0 = p1max > p2max ? p1max : p2max;
//                    Ymin = (int) (ymin0 - (ymax0 - ymin0) * 0.1);
//                    Ymax = (int) (ymax0 + (ymax0 - ymin0) * 0.1);
////                    Ymin = ymin0;
////                    Ymax = ymax0;
//                    Log.e(TAG, "PulseWave Ymin = " + Ymin + " Ymax = " + Ymax);
//                    setParameter(canvas);
//                    drawWavearray(canvas, p, p1wave, "#FFFF00");
//                    drawWavearray(canvas, p, p2wave, "#00FF00");
//                    drawOutsideFrame(canvas, p);
//                }
//            }catch (JSONException e) {
//
//            }
            // 畫三條線
//            drawWavearray(canvas, p, bpwave, "#FFFFFF", 0, 3);
//            drawWavearray(canvas, p, p1wave, "#FFFF00",1, 3);
//            drawWavearray(canvas, p, p2wave, "#00FF00",2, 3);
            drawWavearray(canvas, p, bpwave, "#FFFFFF", 0, 1);
        }

        // 繪製底框
        private void drawBackFrame(Canvas canvas, Paint p){
            p.setColor(Color.parseColor("#000000"));
            p.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
        }

        // 繪製外框
        private void drawOutsideFrame(Canvas canvas, Paint p){
            p.setColor(Color.parseColor("#000000"));       // 黑色
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(xshift);
            canvas.drawRect(xshift/2, xshift/2, canvas.getWidth() - xshift/2, canvas.getHeight() - xshift/2, p);
            p.setColor(Color.parseColor("#FFFFFF"));           // 白色
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            canvas.drawRect(xshift, yshift, chartW + xshift, chartH + yshift, p);
        }

        // 繪製格線
        private void drawGrid(Canvas canvas, Paint p){
            p.setColor(Color.parseColor("#FFFFFF"));
            p.setStrokeWidth(1);
            for (int xindex = 1; xindex < 10; xindex ++){   // 畫 9 條垂直的格線
                Float xgridposition = Float.valueOf(chartW / 10 * xindex + xshift);
                canvas.drawLine(xgridposition, yshift, xgridposition, chartH + yshift, p);
            }
            for (int yindex = 1; yindex < 5; yindex ++){    // 畫 5 條水平的格線
                Float ygridposition = Float.valueOf(chartH / 5 * yindex + yshift);
                canvas.drawLine(xshift, ygridposition, chartW + xshift, ygridposition, p);
            }
        }

        // 繪製波型
        // 將繪製的陣列傳入 partition = 將繪圖區分成幾個區域， 最下方的index = 0, 最上方的index = partition -1
        private void drawWavearray(Canvas canvas, @NotNull Paint p, @NotNull JSONArray wave2show, String linecolor, int index, int partition) {
            Log.e(TAG, "onDraw at drawWavearray");

            float ybase = chartH -  chartH * index / partition;
            int wavedatacount = wave2show.length();    // 實際上使用
            Log.e(TAG, "wave2show.length() = " + wave2show.length() );
            Xmax = wavedatacount;
            Ymin = Integer.MAX_VALUE;   Ymax = 0;
            int wavevalue = 0;

            try {
                for (int i = 0; i < Xmax -1; i++) {         // 最後一筆不列入計算 Max Min
                    Log.e(TAG, "wave2show.getInt(" + i + ") = " + wave2show.getInt(i));
                    wavevalue = wave2show.getInt(i);
                            if (wavevalue > Ymax) Ymax = wavevalue;
                            if (wavevalue < Ymin) Ymin = wavevalue;
                }
                int ydiff = (Ymax - Ymin) / 10;
                Ymax += ydiff;
                Ymin -= ydiff;          // 上下各留 10 % 空間
                setParameter(canvas);
                p.setColor(Color.parseColor(linecolor));
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2);
                Path wave = new Path();
                //int wavedatacount = wave2show;               // 虛擬測試
                int startindex = 0;
                startindex = wavedatacount - Xmax;
                xspan = Float.valueOf(chartW) / (Xmax - Xmin);
                yspan = Float.valueOf(chartH) / (Ymax - Ymin) / partition;
                wave.moveTo(get_xpos(0), get_ypos(wave2show.getInt(startindex)));
                for (int waveindex = 0; waveindex < Xmax; waveindex++) {
                    float ypos = ybase - (yspan * ( wave2show.getInt(waveindex + startindex) - Ymin)) +yshift;
                    wave.lineTo(get_xpos(waveindex),  ypos);
                }
                //wave.close();//封閉
                canvas.drawPath(wave, p);
            } catch (JSONException e) {

                Log.e(TAG, "繪圖區錯誤 " + e.toString());
                e.printStackTrace();
            }
        }

        private float get_xpos(int Xdata){
            return xshift + xspan * (Xdata - Xmin);
        }
        private float get_ypos(int Ydata){
            return  chartH -  (yspan * (Ydata - Ymin)) + yshift;
        }
    }   //  end of Draw類別

    private String postKey;
    private void setHasReadReport(long recordid, String hasread){
        Log.e(TAG, "recordID = " + recordID);
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command",  "SetHasReadReport_APP");    // from Auto -> APP
            jqljson.put("rid",  recordID);
            Log.e(TAG, "rid = " + recordID);
            jqljson.put("hasread", hasread);
            jqljson.put("mid", user.getUserID());
            jqljson.put("rdate", Utility.getDateNow());
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "SetHasReadReport_APP";
        progressBar_chart.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        HttpPost httpPost = new HttpPost(Activity_ChartWave.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_ChartWave.this, GlobalVariables.http_url,  jqljson.toString());
    }

    private void getWave(long rid){
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command",  "GetBPMWaveRecordByID_APP");    // from Auto -> APP
            jqljson.put("rid",  rid);
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "GetBPMWaveRecordByID_APP";
        progressBar_chart.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        HttpPost httpPost = new HttpPost(Activity_ChartWave.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_ChartWave.this, GlobalVariables.http_url,  jqljson.toString());
    }

    @Override
    public void onComplete(String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar_chart.setVisibility(View.GONE);                // 恢復觸控功能
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                try {
                    final JSONObject responseJson = new JSONObject(response);
//                    Log.e(TAG, "response = " + response);
                    switch (postKey) {
                        case "SetHasReadReport_APP":
                            setHasReadReport_APP(responseJson);
                            break;
                        case "GetBPMWaveRecordByID_APP":
                            getBPMWaveRecordByID_APP(responseJson);
                            break;
                    }
                } catch (JSONException e) {
                }
            }
        }); // enf of runOnUiThread
    }   // end of onComplete

    private void setHasReadReport_APP(JSONObject jchart) {
        try {
            if (jchart.getString("status").equals("success")) {
//                openRepert();
            } else if (jchart.getString("status").equals("fail")){
                if (jchart.getInt("count") == 2){
                    openReport();
                }
            } else {
                // 資料庫查詢錯誤
                String msg = getResources().getString(R.string.dialog_msg_requestfail) + "\n" + jchart.getString("result");
                activityAlert.showAlertDialog(msg, R.string.dialog_OK, activityAlert.doNothing);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getBPMWaveRecordByID_APP(JSONObject jchart) {
        try {
            if (jchart.getString("status").equals("success")) {
    //                            JSONObject jchart = dblist.getJSONObject(0);
    //                            JSONObject jchart = new JSONObject(response);
//                Log.e(TAG, jchart.toString());
                setWaveChart(jchart);  // 只取一筆，也只有這一筆
                setFFTreport(jchart);
                position = jchart.getInt("position");
                posture = jchart.getInt("posture");
                sbp = jchart.getInt("recordSBP");
                dbp = jchart.getInt("recordDBP");
                hr = jchart.getInt("recordHR");
                rdate = jchart.getString("recordDate");
                rtime = jchart.getString("recordTime");
                bestcut = jchart.getString("bestCut");
                String bp = getResources().getString(R.string.label_bloodpressure) + " " + sbp + " / " + dbp;
                String hrate = getResources().getString(R.string.label_heartbeatrate) + " " + hr;
                text_charttime.setText(getResources().getString(R.string.name_measuretime) + rdate + " " + rtime);
                text_chartpressure.setText(bp + "  " + hrate);
            } else {
                // 資料庫查詢錯誤
                String msg = getResources().getString(R.string.dialog_msg_requestfail) + "\n" + jchart.getString("result");
                activityAlert.showAlertDialog(msg, R.string.dialog_OK, activityAlert.doNothing);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFail(String err) {
        Log.e(TAG, err);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar_chart.setVisibility(View.GONE);
                // 恢復觸控功能
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
        try {
            final JSONObject failJson = new JSONObject(err);
            if (failJson.getString("status").equals("exception")){      // notfound 表示沒有找到對應的帳號
                // 設定下拉式選單
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_ChartWave.this);
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
                        dialog.show();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}