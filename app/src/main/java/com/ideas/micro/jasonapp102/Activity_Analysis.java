package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Activity_Analysis extends AppCompatActivity {
    private String TAG = "資料分析";
    private final int C0Rowindex = 1;       // C0 在第二個Row, index = 1
    private int meridiancount = 11;         // C0 - C11 十二個脈象
    private int chartindex = 0;                 // 第一張圖
    private JSONObject reportName = new JSONObject();
    private JSONObject reportData = new JSONObject();
    private JSONObject reportString = new JSONObject();
    private JSONObject reportCols = new JSONObject();
    private String[] meridiankeys = {"amp", "ampv", "ampir", "ampr", "phs", "phsv", "phsir", "phsr"};
    private String[] meridianname = {"能量", "能量變異", "能量虛實", "能量比", "相位", "相位變異", "相位虛實", "相位比"};
    private int[] meridiancols = {6, 9, 7, 8, 10, 13, 11, 12};
    private String[] displaykeys =  {"amp", "ampv","phs", "phsir","phs", "phsv", "phsir"};
    private int chartcount = displaykeys.length;     // 顯示的內容數量

    private TextView tvTitle;
    private ListView listView;
    private Button btn_prev, btn_next;
    private TextView text_bpmanalysischart, name_energydensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate...");
        initializeParameter();      // 初始化數據參數
        setContentView(R.layout.activity_analysis);        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_analysis_title);
        text_bpmanalysischart = (TextView) findViewById(R.id.text_bpmanalysischart);
        name_energydensity = (TextView) findViewById(R.id.name_energydensity);
        btn_prev = (Button) findViewById(R.id.btn_prev);        // 上一筆
        btn_next = (Button) findViewById(R.id.btn_next);        // 下一筆
        listView = (ListView) findViewById(R.id.analysislist);

        // 連線資料庫不用
        //        recordID = getintent.getIntExtra("recordID", -1);
        //        getFFTresult(recordID);  // 透過資料庫下載
        // 直接從上一個頁面傳遞資料過來
        Intent getintent = getIntent();
        Log.e(TAG, "FFT_report -> " + getintent.getStringExtra("FFT_report"));
        setFFTData(getintent.getStringExtra("FFT_report"));

        //listView.setOnItemClickListener(onClickListView);       //指定事件 Method
        btn_prev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (chartindex > 0 ){
                    chartindex = chartindex - 1;
                }
                showChartAndData();
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (chartindex < chartcount - 1){
                    chartindex = chartindex  + 1;
                }
                showChartAndData();
            }
        });
    }       // end of oncreate

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart...");
        LogInOut.log("Activity_Analysis", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop...");
        LogInOut.log("Activity_Analysis", false);
    }

     //初始化參數
    private void initializeParameter(){
        chartindex = 0;         // 第一張圖
        try {
            for (int i = 0; i < chartcount; i++) {
                String key = displaykeys[i];        // 只挑選要顯示的key
                reportName.put(key, meridianname[i]);
                reportData.put(key, new JSONArray());
                reportString.put(key, new JSONArray());
                reportCols.put(key,meridiancols[i]);
            }
        } catch (JSONException e){
                e.printStackTrace();
        }
    }

    //    setFFTData -> setDrawData
    private void setFFTData(String fft){
        //            JSONObject fft_json = new JSONObject(fft);
//            int fft_jsonsection = fft_json.names().length();    // 總共有幾個元素
//            Log.e(TAG, "最後一個 Section = " + fft_json.names().getString(fft_jsonsection - 1) );
        JSONArray fft_array = Utility_Meridian.umed_getMaxCutSection(fft);// fft_json.getJSONArray("s" + (fft_jsonsection - 1));     // 取用最後一組資料 s3
        if (fft_array.length() == 18 * 14){
            // 確認資料長度正確
            setDrawData(fft_array);
        } else {
            Utility.showAlert(Activity_Analysis.this, R.string.dialog_msg_fftcounterror);
        }
    }       // end of setFFTData

    //    // 整理顯示圖表的資料
    private void setDrawData(JSONArray fft_array) {
        Log.e(TAG, "開始整理資料準備繪圖");
        int dataindex = 0;
            for (int i = 0; i < meridiancount; i++) {        // C0 - C11
                dataindex = (i + C0Rowindex) * 18;   // 第 i 行起始index
                for (int k = 0; k < chartcount; k++) {
                    String key = displaykeys[k];
                    Log.e(TAG, "setDrawData with meridianIndex = " + i + " chartIndex = " + k  + " keyname = " + key);
                    try {
                        float value = (float) fft_array.getDouble(dataindex + reportCols.getInt(key));
                        if (i == 0 && key.equals("amp")) {
                            reportData.getJSONArray(key).put(i, value / 100);
                            reportString.getJSONArray(key).put(i, String.format("%.2f",  value/100));
//                            ampphsdata0.getJSONArray(key).put(i, value / 100);          //] / averagejson[key][r_pos]['H' + i] - 1) * 100;
                        } else {
                            reportData.getJSONArray(key).put(i, value);
                            reportString.getJSONArray(key).put(i, String.format("%.2f",  value));
//                            ampphsdata0.getJSONArray(key).put(i, value);                                    //] / averagejson[key][r_pos]['H' + i] - 1) * 100;
                        }
                    } catch (JSONException e) {
                        try {
                            Log.e(TAG, "N/A");
                            reportData.getJSONArray(key).put(i, 0);
                            reportString.getJSONArray(key).put(i, "N/A");
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                    }
                }   // end of k
            }   // end of i
            Log.e(TAG, reportString.toString());
            layout = (LinearLayout) findViewById(R.id.chartlayout);
            drawView = new AnalysisChartView(this);
            drawView.setMinimumHeight(500);
            drawView.setMinimumWidth(300);
            drawView.invalidate();      // 通知 drawView組件重繪
            layout.addView(drawView);
            showChartAndData();
    }

    private void showChartAndData(){
        String key = displaykeys[chartindex];
        String[] dataStr = new String[meridiancount];
        dataVal = new float[meridiancount];     // 全域變數
        try {
            text_bpmanalysischart.setText(getResources().getString(R.string.label_bpmanalysischart) + "(" + reportName.getString(key) + ")");
            name_energydensity.setText(reportName.getString(key));
            JSONArray dataArray = reportData.getJSONArray(key);
            Log.e(TAG, "dataArray.length() = "  + dataArray.length());
            for (int i=0; i<meridiancount; i++){
                dataVal[i]= (float) dataArray.getDouble(i);
                dataStr[i] = reportString.getJSONArray(key).getString(i);
                Log.e(TAG, "" + dataVal[i]);
            }
            //                        String.format("%.2f", fft_array.getDouble(dataindex + Col_phs))};
        } catch (JSONException e) {
            Log.e(TAG, "無法取得 namearray 的名稱字串");
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewAdapter_Analysis adapter = null;
        Log.e(TAG, "chartindex = " + chartindex);
        adapter = new ViewAdapter_Analysis(dataStr,inflater);
        listView.setAdapter(adapter);
        drawView.invalidate();      // 通知 drawView組件重繪
    }



    // **** 繪圖類別區域 **** //
    AnalysisChartView drawView;
    LinearLayout layout;
    private int wavetoshow;
    Canvas canvas;
    private int timeelapsed = 0;    // 開始量測後的時間
    private int timestep = 50;      // 心電圖更新時間 50ms
    private float[] dataVal;

    public void refreshDraw(){
        drawView.invalidate();
    }

    // 自訂繪圖類別
    private  class AnalysisChartView extends View {
        private int Xmin, Xmax;
        private int Ymin, Ymax;
        private int chartW, chartH;
        private float unit_w, unit_h;
        private int  Yaxismax = 1, Yaxismin = 0;	// Y座標軸
        private int Ypow = 1;
        private int xlabelh_u = 50, xlabelh_d = 40;		// X 坐標軸標籤高度
        private int ylabelw_l = 25, ylabelw_r = 10; 	// Y 坐標軸標籤寬度
        private String color_Background = "#000000";
        private String color_Text = "#FFFFFF";
        private String color_Canvasframe = "#000000";
        private String color_Chartframe = "#FF0000";
        private String color_Ygrid = "#AAAAAA";
        private String color_Bar0 = "#AAFFAA";
        private String color_Bar1 = "#FFAAAA";
        private int size_Text = 28;
        // constructor 建構式
        public AnalysisChartView(Context context){
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint p = new Paint();								// 創建畫筆
            p.setAntiAlias(true);									// 設置畫筆的鋸齒效果。 true是去除。
            p.setColor(Color.RED);								// 設置紅色
            getYscale(canvas, maximum(dataVal), minimum(dataVal));
            drawBackFrame(canvas, p);
            drawGrid(canvas, p);
            drawWave(canvas, p);
            drawOutsideFrame(canvas, p);
            try {
                text_Title(canvas, p, reportName.getString(displaykeys[chartindex]) + " (x" + Ypow + ")");	// 標題
            } catch (JSONException e) {
                e.printStackTrace();
            }
            text_Xaxis(canvas, p);	// X軸文字
//            text_Yaxis(canvas, p);

        }

        // 繪製底框
        private void drawBackFrame(Canvas canvas, Paint p){
            p.setColor(Color.parseColor(color_Background));
            p.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
        }

        // 繪製外框
        private void drawOutsideFrame(Canvas canvas, Paint p){
            p.setColor(Color.parseColor(color_Background));       // 黑色
            p.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, canvas.getWidth(), xlabelh_u, p);     // 上邊
            canvas.drawRect(0, 0, ylabelw_l, canvas.getHeight(), p);    //  左邊
            canvas.drawRect(0, chartH + xlabelh_u, canvas.getWidth(), canvas.getHeight(), p);   // 下邊
            canvas.drawRect(chartW + ylabelw_l, 0, canvas.getWidth(), canvas.getHeight(), p);   // 右邊
            // 繪圖框
            p.setColor(Color.parseColor(color_Chartframe));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(4);
            canvas.drawRect(ylabelw_l, xlabelh_u, chartW + ylabelw_l, chartH + xlabelh_u, p);
        }

        // 繪製格線
        private void drawGrid(Canvas canvas, Paint p){
            p.setColor(Color.parseColor(color_Ygrid));
            p.setStrokeWidth(1);
            for (int yindex = Yaxismin; yindex <=Yaxismax; yindex ++){    // 畫 1 條水平的格線
                float ygridposition = chartH + xlabelh_u - chartH * (yindex - Yaxismin)/ (Yaxismax - Yaxismin) ;
                canvas.drawLine(ylabelw_l, ygridposition, chartW + ylabelw_l, ygridposition, p);
            }
        }
        // 標題文字
        private void text_Title(Canvas canvas, Paint p, String title){
            p.setColor(Color.parseColor(color_Text));       // 黑色
            p.setTextSize(size_Text);										// 設置文字的大小為 16。
            p.setTypeface(Typeface.create("Arial",Typeface.NORMAL));
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(title,canvas.getWidth()/2,xlabelh_u,p);		// 寫一段文字
        }

        // 數值顯示
//        private void text_Value(value, val_x, val_y, val_w){
////			alert(value + val_x + val_y + val_w);
//            ctx.fillStyle = color_Text;	// 黑色
//            ctx.textAlign = "center";
//            ctx.font = "12px Arial";
////			alert("parseFloat(value/Ypow).toFixed(2) = " + parseFloat(value/Ypow).toFixed(2));
//            ctx.fillText(parseFloat(value/Ypow).toFixed(2), val_x, val_y, val_w);
//        }

        // X軸文字
        private void text_Xaxis(Canvas canvas, Paint p){
            p.setColor(Color.parseColor(color_Text));       // 黑色
            p.setTextSize(size_Text);										// 設置文字的大小為 16。
            p.setTextAlign(Paint.Align.CENTER);
            for (int i = 0; i<meridiancount; i++){
                canvas.drawText("H" + i,get_xpos(i, 0.5f),canvas.getHeight(),p);		// 寫一段文字
            }
        }

        // 繪製波型
        private void drawWave(Canvas canvas, Paint p) {
            Log.e(TAG, "onDraw");
            String key =  displaykeys[chartindex];
            p.setColor(Color.parseColor("#00FF00"));
            p.setStyle(Paint.Style.FILL);
            Log.e(TAG, "meridiancount = " + meridiancount);
            Float[] chart2show = new Float[meridiancount];
                Log.e(TAG,  "chartindex =" + chartindex);
                Log.e(TAG,  "chart2show.length =" + chart2show.length);

            for (int i = 0; i < meridiancount; i++) {
                if (dataVal[i] > 0) {
                    canvas.drawRect(get_xpos(i, 0.1f), get_ypos(dataVal[i]), get_xpos(i, 0.8f), get_ypos(0f), p);
                } else {
                    canvas.drawRect(get_xpos(i, 0.1f), get_ypos(0f), get_xpos(i, 0.8f), get_ypos(dataVal[i]), p);
                }
            }
        }

        private float get_xpos(int Xdata, float shiftx){
            return ylabelw_l + unit_w * (Xdata - Xmin) + unit_w * shiftx;
        }

        private float  get_width(float w){
            return unit_w * w;
        }

        private float get_ypos(Float Ydata){
            return  (float) (chartH + xlabelh_u -  (unit_h * (Ydata - Ymin)));
        }

        private float get_height(float h){
            return unit_h * h;
        }

        private void getYscale(Canvas canvas, float ymax, float ymin){
            float absymin = ymin<0?-ymin:ymin;
            float absymax = ymax<0?-ymax:ymax;
            float yscale = absymax > absymin?absymax:absymin;
                if (yscale != 0){
                    //alert("Math.log(" + yscale + ") = " + Math.log(yscale)/Math.log(10));
                    int ylog =(int) Math.floor(Math.log10(yscale));
                    Ypow = (int) Math.pow(10, ylog);
                    int Yscale = (int) Math.ceil(yscale/Ypow);
                    if (ymin > 0){	//	ymin > 0  表示 ymax, ymin 都>0
                        Ymax = Yscale * Ypow;	Yaxismax = Yscale;
                        Ymin = 0;				Yaxismin = 0;
                    } else if (ymax < 0) {	// 表示 ymax, ymin 都< 0
                        Ymax = 0;				Yaxismax = 0;
                        Ymin = -Yscale * Ypow;	Yaxismin = -Yscale;
                    } else {
                        if (ymax + ymin > 0) {	// ymax正 ymin負
                            Ymax = Yscale * Ypow; Ymin = -Ymax;	Yaxismax = Yscale; Yaxismin = -Yscale;
                        } else {
                            Ymin = -Yscale * Ypow; Ymax = -Ymin;Yaxismax = Yscale; Yaxismin = -Yscale;
                        }
                    }
                }
            Xmin = 0;           Xmax = meridiancount-1;
            chartW = canvas.getWidth() - ylabelw_l - ylabelw_r;
            chartH = canvas.getHeight() - xlabelh_d - xlabelh_u;
            unit_w = Float.valueOf(chartW) / (meridiancount);            // 切成 meridiancount 段
            unit_h = Float.valueOf(chartH) / (Ymax - Ymin);
//	    	alert("ymin" + ymin + " ymax" + ymax + " Yaxismin" + Yaxismin + " Yaxismax" + Yaxismax);
        }

        private void chart_scale(){
            unit_h = chartH / (Ymax - Ymin);			// 圖表單位高度
            unit_w = chartW / (Xmax - Xmin);			// 圖表單位寬度
        }

        // 兩組鎮列取最大值
        private float maximum2(JSONArray datalist0, JSONArray datalist1){
            float m0 = Utility.Max_JSONArray(datalist0);
            float m1 = Utility.Max_JSONArray(datalist1);
            return m0>m1?m0:m1;
        }

        // 兩組陣列取最小值
        private float minimum2(JSONArray datalist0, JSONArray datalist1){
            float m0 = Utility.Min_JSONArray(datalist0);
            float m1 = Utility.Min_JSONArray(datalist1);
            return m0<m1?m0:m1;
        }

        // 陣列取最大值
        private float maximum(float[] datalist){
            float max = Float.MIN_VALUE;
            for (int i=0; i<datalist.length; i++){
                max = max>datalist[i]?max:datalist[i];
            }
            return max;
        }

        // 陣列取最小值
        private float minimum(float[] datalist){
            float min = Float.MAX_VALUE;
            for (int i=0; i<datalist.length; i++){
                min = min<datalist[i]?min:datalist[i];
            }
            return min;
        }
    }
}