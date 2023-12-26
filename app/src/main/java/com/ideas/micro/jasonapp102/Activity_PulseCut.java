package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Activity_PulseCut extends AppCompatActivity implements onHttpPostCallback{
    private String TAG = "脈診切波";
    private LinearLayout layout;
    private ViewCutWave drawView;
    private static JSONArray wavep2 = new JSONArray();
    private static JSONObject recordcut;
    private int wavetoshow;
    private int Ymin, Ymax, Xmin, Xmax;
    private String postKey;
    private long recordID;
    private JSONObject memid = new JSONObject();
//    private JSONObject memage = new JSONObject();
//    private JSONObject memgender = new JSONObject();
//    private JSONObject membirthday = new JSONObject();
//    private JSONArray recordlist = new JSONArray();
//    private int recordrange_month;          //  搜尋紀錄期間 3month or 12month
//    private int selectedMem;
    private ArrayList memlist = new ArrayList<String>();
    SingleAdmin admin = SingleAdmin.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_pulsecut);
        setDrawLayout();    // 設定繪圖布局
        Intent getintent = getIntent();
        recordID = getintent.getLongExtra("recordID", -1);
        Log.e(TAG, "recordID = " + recordID);
        getWave(recordID);
    }

    // 向資料庫讀取波形( 脈診報告ID)
    private void getWave(long rid){
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command",  "GetWaveCutHistoryByID");
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            jqljson.put("rid",  rid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "GetWaveCutHistoryByID";
        HttpPost httpPost = new HttpPost(Activity_PulseCut.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_PulseCut.this, GlobalVariables.http_url,  jqljson.toString());
    }

    // 設定繪圖布局
    private void setDrawLayout() {
        layout = (LinearLayout) findViewById(R.id.pulsecutlayout);
        drawView = new ViewCutWave(this);
        drawView.setMinimumHeight(500);
        drawView.setMinimumWidth(300);
        drawView.invalidate();      // 通知 drawView組件重繪
        layout.addView(drawView);
    }

    @Override
    public void onComplete(String response) {
        Log.e(TAG, response);
        try {
            if (postKey.equals("GetWaveCutHistoryByID")) {
                final JSONObject responseJson = new JSONObject(response);
                if (responseJson.getString("status").equals("success")) {      //
                    setWave(responseJson.getJSONArray("dblist"));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onFail(String err) {

    }

    private void setWave(JSONArray _dblist){
        Log.e(TAG, "setWave");
        final JSONArray dblist = _dblist;
        try {
            for (int i = 0; i < dblist.length(); i++) {
                JSONObject datajson = dblist.getJSONObject(i);
                Log.e(TAG, "recordid = " + datajson.getLong("rid"));
                if (datajson.getLong("rid") == recordID) {
                    wavep2 = new JSONArray(datajson.getString("p2"));       //  p2 波形
                    recordcut = new JSONObject(datajson.getString("cut").replaceAll("'", "\""));
                    Log.e(TAG, "wavep2.length = " + wavep2.length());
                    int wavemax = (int) Utility.Max_JSONArray(wavep2);
                    int wavemin = (int) Utility.Min_JSONArray(wavep2);
                    Ymax = wavemax + (wavemax - wavemin) / 10;	// 往上加 10% 空間
                    Ymin = wavemin - (wavemax - wavemin) / 10;	// 往下減 10% 空間
                    drawView.invalidate();
                } else {

                }
            }   // end of  for
        } catch (JSONException e) {
            e.printStackTrace();
        }
//                        String nameaccount = dblist.getJSONObject(i).getString("memberName") + "(" + dblist.getJSONObject(i).getString("memberAccount") + ")";
//                        memlist.add(nameaccount);
//                        memid.put(nameaccount, dblist.getJSONObject(i).getString("memberID"));
//                        memgender.put(nameaccount, dblist.getJSONObject(i).getString("memberGender"));
//                        memage.put(nameaccount, Utility.GetAge(dblist.getJSONObject(i).getString("memberBirthday")));   // 紀錄年紀
//                        membirthday.put(nameaccount, dblist.getJSONObject(i).getString("memberBirthday"));  // 紀錄生日

//        // 設定下拉式選單
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                setListData(dblist);
//            }
//        });
    }



    // 自訂繪圖類別
    public  class ViewCutWave extends View {
        private int xshift = 10, yshift =10;
        private int chartW, chartH;
        private float xspan, yspan;
        private String color_WaveLine = "#00FF00";
        private String color_CutLine = "#FFFF00";
        private String color_Back = "#000000";
        private String color_Frame = "#FFFFFF";
        private String color_Grid = "#AAAAAA";

        // constructor 建構式
        public ViewCutWave(Context context){
            super(context);
            Ymin = 8000000; Ymax = 8500000;       // 首次設定
            Xmin = 0; Xmax = 3000;
        }

        @Override
        protected void onDraw(Canvas canvas) {      // canvas 由onDraw 自行帶出
            super.onDraw(canvas);
            Paint p = new Paint();								// 創建畫筆
            p.setAntiAlias(true);									// 設置畫筆的鋸齒效果。 true是去除。
            p.setColor(Color.RED);								// 設置紅色
            setParameter(canvas);                               // 設定參數
            drawBackFrame(canvas, p);                       // 設定背景
            drawGrid(canvas, p);                                // 設定格線
            drawWavearray(canvas, p, wavep2, color_WaveLine);      // 繪製曲線
//            drawCutarray(canvas, p, wavep2, color_CutLine);
            drawOutsideFrame(canvas, p);
        }

        // 設定參數
        private void setParameter(Canvas canvas){
            chartW = canvas.getWidth() - xshift - xshift;   // 實際作圖寬度
            chartH = canvas.getHeight() - yshift - yshift;  // 實際作圖高度
            xspan = Float.valueOf(chartW) / (Xmax - Xmin);
            yspan = Float.valueOf(chartH) / (Ymax - Ymin);
        }

        // 繪製底框
        private void drawBackFrame(Canvas canvas, Paint p){
//            Log.e(TAG, "背景 drawBackFrame");
            p.setColor(Color.parseColor(color_Back));
            p.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
        }

        // 繪製外框
        private void drawOutsideFrame(Canvas canvas, Paint p){
//             Log.e(TAG, "外框 drawOutsideFrame");
            p.setColor(Color.parseColor(color_Back));       // 黑色
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(xshift);
            canvas.drawRect(xshift/2, xshift/2, canvas.getWidth() - xshift/2, canvas.getHeight() - xshift/2, p);
            p.setColor(Color.parseColor(color_Frame));           // 白色
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            canvas.drawRect(xshift, yshift, chartW + xshift, chartH + yshift, p);
        }

        // 繪製格線
        private void drawGrid(Canvas canvas, Paint p){
            p.setColor(Color.parseColor(color_Grid));
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
        // 將繪製的陣列傳入
        private void drawWavearray(Canvas canvas, Paint p, JSONArray wave2show, String linecolor) {
            Log.e(TAG, "onDraw at wave2show ");
            p.setColor(Color.parseColor(linecolor));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2);
            Path wave = new Path();
            try {
                int wavedatacount = wave2show.length();    // 實際上使用
                //int wavedatacount = wave2show;               // 虛擬測試
                int startindex = 0;
                if (wavedatacount > Xmax) {
                    startindex = wavedatacount - Xmax;
                    wave.moveTo(get_xpos(0), get_ypos(wave2show.getInt(startindex)));
                    for (int waveindex = 0; waveindex < Xmax; waveindex++) {
                        wave.lineTo(get_xpos(waveindex), get_ypos(wave2show.getInt(waveindex + startindex)));
                    }
                } else {
                    startindex = 0;
                    wave.moveTo(get_xpos(startindex), get_ypos((Ymax + Ymin) / 2));
                    wave.lineTo(get_xpos(Xmax - wavedatacount), get_ypos((Ymax + Ymin) / 2));
                    for (int waveindex = 0; waveindex < wavedatacount; waveindex++) {
                        wave.lineTo(get_xpos(waveindex + Xmax - wavedatacount), get_ypos(wave2show.getInt(waveindex)));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "繪圖區錯誤 " + e.toString());
                e.printStackTrace();
            }
            //wave.close();//封閉
            canvas.drawPath(wave, p);
        }

        // 將繪製的陣列傳入
        private void drawCutarray(Canvas canvas, Paint p, JSONArray wave2show, String linecolor) {
        }

            private float get_xpos(int Xdata){
            return xshift + xspan * (Xdata - Xmin);
        }
        private float get_ypos(int Ydata){
            return  chartH -  (yspan * (Ydata - Ymin)) + yshift;
        }
    }   //  end of Draw類別
}