package com.ideas.micro.jasonapp102;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class DrawChartView extends View {
    private final String TAG = "DrawChart";
    private Context context;
    private float Ymin, Ymax;
    private int Xmin, Xmax;
    private int xlabelh_u, xlabelh_d, ylabelw_l, ylabelw_r;
    private String title = "";
    private 	String[] uchart_color_hxline = new String[]{"#FF2200","#22FF22","#7A237A",
            "#000000","#0000FF","#8A2BE2","#A52A2A","#5F9EA0",
            "#DC143C","#008B8B","#8B008B","#483D8B","#3CB371","#DB7093"};
    public String[] uchart_color_comparebar = new String[]{"#ADD8E6", "#191970"};   // 提供Activity_BarCompare
    private 	String uchart_color_CanvasBackground = "#EEEEEE";
    private 	String uchart_color_ChartBackground = "#FFFFFF";
    private 	String uchart_color_Text = "#000000";		// 黑色
    private 	String uchart_color_Canvasframe = "#000000";
    private 	String uchart_color_Chartframe = "#000000";
    private 	String uchart_color_Chartgrid = "#000000";
    private 	String uchart_color_Ygrid = "#AAAAAA";
    private 	String uchart_color_Bar0 = "#ADD8E6";
    private 	String uchart_color_Bar1 = "#191970";
    private JSONObject wavedata = new JSONObject();
    private float wavedatamax, wavedatamin;
    private JSONObject hxlinecolorjson = new JSONObject();
    private JSONObject chartoption = new JSONObject();

    // constructor 建構式多型 I
    public DrawChartView(Context context){
        super(context);
        this.context = context;
        Ymin = 8000000; Ymax = 8500000;       // 首次設定
        Xmin = 0; Xmax = 1220;
        setWillNotDraw(false);
        setColorJson();
    }

    // constructor 建構式多型 II
    public DrawChartView(Context context, int ymin, int ymax, int xmin, int xmax) {
        super(context);
        this.context = context;
        this.Ymin = ymin;
        this.Ymax = ymax;       // 首次設定
        this.Xmin = xmin;
        this.Xmax = xmax;
        setWillNotDraw(false);
        setColorJson();
    }

    private void setColorJson(){
        try {
            hxlinecolorjson.put("sbp", uchart_color_hxline[0]);
            hxlinecolorjson.put("dbp", uchart_color_hxline[1]);
            hxlinecolorjson.put("hr", uchart_color_hxline[2]);
            for (int hxindex = 0; hxindex < 11; hxindex ++) {
                hxlinecolorjson.put("H" + hxindex, uchart_color_hxline[3 + hxindex]);
            }

        } catch (JSONException e){

        }
    }

    public void refreshDraw() {
        // invalidate 會呼叫 View 物件的 onDraw
        Log.e(TAG, "準備  refreshDraw");
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e(TAG, "繪圖 onDraw 準備繪圖");
        super.onDraw(canvas);
        Paint p = new Paint();								// 創建畫筆
        p.setAntiAlias(true);									// 設置畫筆的鋸齒效果。 true是去除。
        if (wavedata.length() > 0) uchart_Chart(canvas, p);
        Log.e(TAG, "繪圖 onDraw 繪圖結束");
        //p.setTextSize(48);										// 設置文字的大小為 16。
        //canvas.drawText("time：" + timeelapsed + " ms",chartW + xshift - 450,chartH + yshift - 30,p);		// 寫一段文字
        //canvas.drawCircle(80,20,20,p);
    }

    // 繪製折線圖
    private void uchart_Chart(Canvas canvas, Paint p){
        // 取代預設值
        Xmin = chartoption.optInt("xmin", 0);
        Xmax =chartoption.optInt("xmax", 0);
        Ymin = chartoption.optInt("ymin", 0);
        Ymax = chartoption.optInt("ymax", 0);
        xlabelh_u = chartoption.optInt("xlabelh_u", 0);
        xlabelh_d = chartoption.optInt("xlabelh_d", 0);
        ylabelw_l = chartoption.optInt("ylabelw_l", 0);
        ylabelw_r = chartoption.optInt("ylabelw_r", 0);
        Log.e(TAG, "Xmin = " + Xmin + ", Xmax = " + Xmax);
        clear_Canvas(canvas, p,  uchart_color_CanvasBackground);
        clear_Chart(canvas, p,  uchart_color_ChartBackground);
        try {
            String charttype = chartoption.getString("charttype").toLowerCase();
            JSONArray xLabel = new JSONArray();
            switch (charttype) {
                case "hx_line":
                    getWaveDataListMaxMin(false);    //  計算極大極小值，順便反向排序
//                    xLabel = reverseArray(wavedata.getJSONArray("x"));      // 反向排序
                    Log.e(TAG, "Yaxix Max = " + wavedatamax  + " Yaxis Min = " + wavedatamin);
                    int xdatacount = xLabel.length();
                    set_Scale(canvas, Xmin , Xmax,  wavedatamin,  wavedatamax);
                    draw_Grid(canvas, p , charttype,  xLabel, (int) ((Ymax - Ymin) / Ypow),  uchart_color_Chartgrid);
                    Iterator iteratorline = wavedata.keys();
                    while(iteratorline.hasNext()){
                        String key = (String) iteratorline.next();          // Log.e(TAG, "Line Key = " + key);
                        if (! key.equals("x")){                                            // Log.e(TAG, "line color = " + hxlinecolorjson.getString(key));
                            draw_hxline(canvas, p, wavedata.getJSONArray("x"), wavedata.getJSONArray(key), hxlinecolorjson.getString(key));
                        }
                    }
                    break;
                case "hx_compare":
                    Log.e(TAG, "hx_compare : wavedata = " + wavedata.toString());
                    // wavedate = { "x" : [], "after":[], "before":[]}
                    getWaveDataListMaxMin(false);    //  計算極大極小值，不需要反向排序
                    xLabel = wavedata.getJSONArray("x");      //
                    set_Scale(canvas, 0 , xLabel.length(),  wavedatamin,  wavedatamax);
                    draw_Grid(canvas, p , charttype,  xLabel, (int) ((Ymax - Ymin) / Ypow),  uchart_color_Chartgrid);
                    draw_comparebar(canvas, p, "before", uchart_color_comparebar[0]);
                    draw_comparebar(canvas, p, "after", uchart_color_comparebar[1]);
                    break;
            }
            draw_CanvasFrame(canvas, p,uchart_color_CanvasBackground);		// Canvas邊框
            draw_ChartFrame(canvas, p, uchart_color_Chartframe);		// Chart邊框
            draw_Xlabel(canvas, p, charttype,  xLabel , uchart_color_Chartgrid);    // 有別於用 index 表示
            draw_Ylabel(canvas, p, charttype,  (int) ((Ymax - Ymin) / Ypow) , uchart_color_Chartgrid);    // 有別於用 index 表示
            draw_Title(canvas, p, title);
            draw_Label(canvas, p);
        } catch (JSONException e){

        }
    }

    private void getWaveDataListMaxMin(boolean reverseArray){
        float arraymax= Float.MIN_VALUE;
        float arraymin = Float.MAX_VALUE;
        Iterator iterator = wavedata.keys();
        while(iterator.hasNext()){
            String key = (String) iterator.next();
            if (! key.equals("x")){                //                            Log.e(TAG, wavedata.getJSONObject(hxindex).getJSONArray("x").toString());
                try {
                    if (reverseArray) wavedata.put(key, reverseArray(wavedata.getJSONArray(key)));      // 將資料反向排序
                    JSONArray datalist = wavedata.getJSONArray(key);
                    float datalistmax = getArraymaximum(datalist);
                    float datalistmin = getArrayminimum(datalist);
                    Log.e(TAG, "陣列最大值 = " + datalistmax + " 陣列最小值 = " + datalistmin);
                    if (arraymax < datalistmax) arraymax = datalistmax;
                    if (arraymin > datalistmin) arraymin = datalistmin;
                } catch (JSONException e) {
                }   // end of try catch
            }   // end of if not x
        } //  end of while iterator
        wavedatamax = arraymax; wavedatamin = arraymin;
    }

    // uchart_Chart
    private int canvasH, canvasW;
    private int chartW, chartH;     // 必須先取得 canvas
    private float unit_h, unit_w;

    // 設定比例尺
    private void set_Scale(Canvas chartcanvas, int xmin, int xmax, float ymin, float ymax){
        try{
            Xmax = xmax; Xmin = xmin;
            Log.e(TAG, "set_Scale " + " Xmax " + Xmax + " Xmin " + Xmin);
            getYscale(ymax, ymin);
            float yspan = Ymax - Ymin;
            int xspan = Xmax - Xmin;
            canvasH = chartcanvas.getHeight();
            canvasW = chartcanvas.getWidth();
            chartH = canvasH - xlabelh_u - xlabelh_d;	// 圖表高度(不包括座標軸標籤)
            chartW = canvasW - ylabelw_l - ylabelw_r;		// 圖表寬度(不包括座標軸標籤)
            unit_h = (float) chartH / yspan;			// 圖表單位高度
            unit_w = (float) chartW / xspan;			// 圖表單位寬度
            Log.e(TAG, "unit_h " + unit_h + " unit_w " + unit_w);
            Log.e(TAG, "canvas[" + canvasW + ", " + canvasH + "] chart[" + chartW + ", " + chartH + "] xlabel[" + xlabelh_u + ", " + xlabelh_d + "] ylabel[" + ylabelw_l + ", " + ylabelw_r +"]");
        } catch (ArithmeticException e) {
            Log.e(TAG, "除以零錯誤 : Xmax = " + Xmax + ", Xmin = " + Xmin + ", Ymax = " + Ymax + ", Ymin = " + Ymin);
            Log.e(TAG, e.toString());   // 除以零
        }
//		alert("xmin = " + Xmin + " xmax = " + Xmax + " ymin = " + Ymin + " ymax = " + Ymax);
    }

    // 清除畫布
    private void clear_Canvas(Canvas canvas, Paint p, String bgcolor){
        p.setColor(Color.parseColor(bgcolor));
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvasW, canvasH, p);
    }

    // 清除繪圖區
    private void clear_Chart(Canvas canvas, Paint p, String bgcolor){
        p.setColor(Color.parseColor(bgcolor));
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(ylabelw_l, xlabelh_u, chartW + ylabelw_l, chartH + xlabelh_u, p);
    }

    // Canvas邊框
    private void draw_CanvasFrame(Canvas canvas, Paint p, String canvasframecolor){
        p.setColor(Color.parseColor(canvasframecolor));
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(0,0, canvasW, xlabelh_u, p);
        canvas.drawRect(0, 0, ylabelw_l, canvasH, p);
        canvas.drawRect(0,canvasH - xlabelh_d, canvasW, canvasH, p);
        canvas.drawRect(canvasW - ylabelw_r, 0, canvasW, canvasH, p);
    }

    // Chart邊框
    private void draw_ChartFrame(Canvas canvas, Paint p, String chartframecolor){
        p.setColor(Color.parseColor(chartframecolor));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5);
        canvas.drawRect(ylabelw_l, xlabelh_u, chartW + ylabelw_l, canvasH -  xlabelh_d, p);
    }

    private int xgridmax = 10;      // X 軸上最多的格線
    // 繪製格線
    private void draw_Grid(Canvas canvas, Paint p, String charttype, JSONArray xdata, int ygridcount, String gridcolor){
        p.setColor(Color.parseColor(gridcolor));
        p.setStrokeWidth(1);
        p.setTextSize(24);
        switch (charttype) {        // 根據charttype 有不同的格線
            case "hx_line":
                // xdatacount 是X 資料數量
                try {
                    JSONArray xlabel_text = chartoption.getJSONArray("xlabel_text");
                    JSONArray xlabel_posi = chartoption.getJSONArray("xlabel_posi");
                    int xgridcount = xlabel_text.length();
//                if (xgridcount > xgridmax){
//                    xgridshift = xgridcount / xgridmax + 1;
//                }
                    for (int xindex = 0; xindex < xgridcount; xindex ++) {   // 畫 xgridcount - 1 條垂直的格線
                        Float xgridposition = get_xpos(xlabel_posi.getInt(xindex)); // Float.valueOf(chartW / xgridcount * xindex + ylabelw_l);
//                        Log.e(TAG, "X Axis position = " + xgridposition);
                        canvas.drawLine(xgridposition, xlabelh_u, xgridposition, chartH + xlabelh_u, p);
                    }
                    for (int yindex = 1; yindex < ygridcount; yindex++) {    // 畫 5 條水平的格線
                        Float ygridposition = Float.valueOf(chartH / ygridcount * yindex + xlabelh_u);
                        canvas.drawLine(ylabelw_l, ygridposition, chartW + ylabelw_l, ygridposition, p);
                    }
               } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case "hx_compare":
                for (int yindex = 1; yindex < ygridcount; yindex++) {    // 畫 5 條水平的格線
                    Float ygridposition = Float.valueOf(chartH / ygridcount * yindex + xlabelh_u);
                    canvas.drawLine(ylabelw_l, ygridposition, chartW + ylabelw_l, ygridposition, p);
                }
                break;

        }
    }

    private void draw_Xlabel(Canvas canvas, Paint p, String charttype,  JSONArray xdata, String gridcolor){
        p.setColor(Color.parseColor(gridcolor));
        p.setStrokeWidth(1);
        p.setTextSize(24);
        p.setStyle(Paint.Style.FILL);
//        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//        Log.e(TAG, "Xlabel = " + xdata.toString());

        int xgridcount = 0;
        switch (charttype) {        // 根據charttype 有不同的格線
            case "hx_line":
                // xdatacount 是X 資料數量
                try {
                    int xgridshift = 1;
                    JSONArray xlabel_text = chartoption.getJSONArray("xlabel_text");
                    JSONArray xlabel_posi = chartoption.getJSONArray("xlabel_posi");

                    xgridcount = xlabel_text.length();
                    for (int xindex = 0; xindex < xgridcount ; xindex ++) {   // 畫 xgridcount - 1 條垂直的格線
                        float xgridposition = get_xpos(xlabel_posi.getInt(xindex)); // Float.valueOf(chartW / xgridcount * xindex + ylabelw_l);
                        String xlabel = xlabel_text.getString(xindex);
                        float xPos = xgridposition - (int)(p.measureText(xlabel)/2);
                        Log.e(TAG, "draw XLabel " + xlabel + " posi " + xPos);
                        canvas.drawText(xlabel, xPos, canvasH - 20 - ((p.descent() + p.ascent()) / 2), p);
//                        Log.e(TAG, "XGird xindex " + xindex + " position " + xgridposition);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "draw XLable JSON Exception " + e.toString());
                }
                break;

            case "hx_compare":
                xgridcount = xdata.length();
                try {
                    for (int xindex = 0; xindex < xgridcount ; xindex ++) {   // 畫 xgridcount - 1 條垂直的格線
                        Float xgridposition = get_xpos(xindex + 0.5); // Float.valueOf(chartW / xgridcount * xindex + ylabelw_l);
                        String xlabel = xdata.getString(xindex);
                        float xPos = xgridposition - (int)(p.measureText(xlabel)/2);
                        canvas.drawText(xlabel, xPos, canvasH - 20 - ((p.descent() + p.ascent()) / 2), p);
//                        Log.e(TAG, "XGird xindex " + xindex + " position " + xgridposition);
                    }
                } catch (JSONException e) {

                }
                break;

        }
    }

    private void draw_Ylabel(Canvas canvas, Paint p, String charttype,  int ygridcount , String gridcolor){
        p.setColor(Color.parseColor(gridcolor));
        p.setStrokeWidth(1);
        p.setTextSize(24);
        p.setStyle(Paint.Style.FILL);
//        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        Log.e(TAG, "ygridcount = " + ygridcount);
        switch (charttype) {        // 根據charttype 有不同的格線
            case "hx_line": case "hx_compare":
                    for (int yindex = 0; yindex <= ygridcount ; yindex ++) {   //
                        float ygridvalue = Ymin + (Ymax - Ymin) / ygridcount * yindex;
                        Float ygridposition = get_ypos(ygridvalue);
//                        Log.e(TAG, "Math.round(ygridvalue/Ypow) = " + Math.round(ygridvalue/Ypow) );
//                        Log.e(TAG, "Math.round(ygridvalue/Ypow) * Ypow = " + Math.round(ygridvalue/Ypow) * Ypow );
                        String ylabel = "";
                        if (ylog >= 0) {
                            ylabel = String.valueOf(ygridvalue);
                        } else {
                            ylabel = String.format("%2." + (-ylog) + "f", ygridvalue);
//                            ylabel = String.valueOf(Math.round(ygridvalue / Ypow) * Ypow);
                        }
                        float xPos = ylabelw_l - (int)(p.measureText(ylabel)) - 15;
                        canvas.drawText(ylabel, xPos, ygridposition - ((p.descent() + p.ascent()) / 2), p);
//                        Log.e(TAG, "YGird yindex " + yindex + " label= " + ylabel + " ypos " + ygridposition);
                    }
                break;
        }
    }

    private void draw_Title(Canvas canvas, Paint p, String title){
        if (title.equals("")) return;
        p.setColor(Color.parseColor(uchart_color_Chartgrid));
        p.setStrokeWidth(1);
        p.setTextSize(36);
        p.setStyle(Paint.Style.FILL);
        canvas.drawText(title, ylabelw_l + (chartW / 2) - (int)(p.measureText(title)/2), xlabelh_u - 10, p);
    }

    // 顯示 HxLine 的標籤 收縮壓 舒張壓 脈搏
    private void draw_Label(Canvas canvas, Paint p){
        if (title.equals(context.getResources().getString(R.string.label_bloodpressure))) {
            // 僅限血壓波
            String label = "";
            int linelength = 50;
            float xpos = 0, ypos = 0, line_ypos = 0;
            try {
                p.setStrokeWidth(5);
                p.setTextSize(36);
                p.setStyle(Paint.Style.FILL);
                ypos = xlabelh_u - (p.descent() + p.ascent()) + 20;
                line_ypos = xlabelh_u - (p.descent() + p.ascent()) / 2 + 20;

                label = context.getResources().getString(R.string.label_sbp);
                p.setColor(Color.parseColor(hxlinecolorjson.getString("sbp")));
                xpos = ylabelw_l + (chartW / 4) - (int) (p.measureText(label) / 2);
                canvas.drawLine(xpos - linelength - 10, line_ypos , xpos  - 10, line_ypos, p);
                p.setColor(Color.parseColor("#000000"));
                canvas.drawText(label, xpos, ypos, p);

                label = context.getResources().getString(R.string.label_dbp);
                p.setColor(Color.parseColor(hxlinecolorjson.getString("dbp")));
                xpos = ylabelw_l + (chartW * 2 / 4) - (int) (p.measureText(label) / 2);
                canvas.drawLine(xpos - linelength - 10, line_ypos , xpos  - 10, line_ypos, p);
                p.setColor(Color.parseColor("#000000"));
                canvas.drawText(label, xpos, ypos, p);

                label = context.getResources().getString(R.string.label_hr);
                p.setColor(Color.parseColor(hxlinecolorjson.getString("hr")));
                xpos = ylabelw_l + (chartW * 3 / 4) - (int) (p.measureText(label) / 2);
                canvas.drawLine(xpos - linelength - 10, line_ypos , xpos  - 10, line_ypos, p);
                p.setColor(Color.parseColor("#000000"));
                canvas.drawText(label, xpos, ypos, p);

            } catch (JSONException e){

            }
        }
    }

    // 波形曲線  x軸是wavearray陣列的index 所以不需要 xlabel[]
    private void draw_wave(Canvas canvas, Paint p, JSONArray wave2show, String linecolor){
        p.setColor(Color.parseColor(linecolor));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        Path wave = new Path();
        try {
            int wavedatacount = wave2show.length();    // 實際上使用
            int startindex = 0;
            wave.moveTo(startindex, get_ypos(wave2show.getDouble(0)));
            for (int waveindex = 1; waveindex < wavedatacount; waveindex++) {
                wave.lineTo(waveindex , get_ypos(wave2show.getDouble(waveindex)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "繪圖區錯誤 " + e.toString());
            e.printStackTrace();
        }
        //wave.close();//封閉
        canvas.drawPath(wave, p);
    }

    // 折線  x軸是wavearray陣列的index
    private void draw_hxline(Canvas canvas, Paint p,JSONArray wave_x, JSONArray wave2show, String linecolor){
        try {
            int wavedatacount = wave2show.length();    // 實際上使用
            int startindex = 0;
            Log.e(TAG, "數據長度 =" + wavedatacount);
            if (wavedatacount == 1){
                p.setColor(Color.parseColor(linecolor));
                p.setStyle(Paint.Style.FILL);
                float xpos = get_xpos(wave_x.getInt(0));
                float ypos = get_ypos(wave2show.getDouble(0));
                canvas.drawRect(xpos - 5, ypos -5, xpos + 5, ypos + 5, p);
            } else if (wavedatacount > 1) { //
                p.setColor(Color.parseColor(linecolor));
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2);
                Path wave = new Path();
                for (int waveindex = 0; waveindex < wavedatacount; waveindex++) {
                    float xpos = get_xpos(wave_x.getInt(waveindex));
                    float ypos = get_ypos(wave2show.getDouble(waveindex));
//                    Log.e(TAG, "Index " + waveindex + "數值 [ " + wave_x.getInt(waveindex) + ", " + wave2show.getDouble(waveindex) + " 座標 [" + xpos + ", " + ypos + "]");
                    if (waveindex == 0) {
                        wave.moveTo(xpos, ypos);
                    } else {
                        wave.lineTo(xpos, ypos);
                    }
                }
                canvas.drawPath(wave, p);
            }
        } catch (JSONException e) {
            Log.e(TAG, "繪圖區錯誤 " + e.toString());
            e.printStackTrace();
        }
    }

    // 柱狀圖 x軸是wavearray陣列的index
    private void draw_comparebar(Canvas canvas, Paint p, String key, String linecolor){
        p.setColor(Color.parseColor(linecolor));
        p.setStyle(Paint.Style.FILL);
        try {
            JSONArray wave2show = wavedata.getJSONArray(key);
            Log.e(TAG, "draw_comparebar " + wave2show.toString());
            int wave2showcount = wave2show.length();    // 實際上使用
            Log.e(TAG, "wave2showcount =" + wave2showcount);
            float xposL = 0;
            float xposR = 0;
            for (int waveindex = 0; waveindex < wave2showcount; waveindex++) {
                float ypos = get_ypos(wave2show.getDouble(waveindex));
                if (key.equals("before")){
                    xposL = get_xpos((waveindex + 0.1));
                    xposR = get_xpos((waveindex + 0.45));
                } else {
                    xposL = get_xpos(waveindex + 0.55);
                    xposR = get_xpos(waveindex + 0.9);
                }
                Log.e(TAG, "waveindex = " + waveindex + " xposL = " + xposL + " xposR = " + xposR + " ypos = " + ypos);
                canvas.drawRect(xposL, ypos, xposR, chartH + xlabelh_u, p);
            }
        } catch (JSONException e) {
            Log.e(TAG, "繪圖區錯誤 " + e.toString());
            e.printStackTrace();
        }
    }



    private float get_xpos(int Xdata){
        return ylabelw_l + unit_w * (Xdata - Xmin);
    }

    private float get_xpos(double Xdata){
        return (float) (ylabelw_l + unit_w * (Xdata - Xmin));
    }

    private float get_ypos(int Ydata){
        return  chartH -  (unit_h * (Ydata - Ymin)) + xlabelh_u;
    }

    private float get_ypos(double Ydata){
        return (float)  (chartH -  (unit_h * (Ydata - Ymin)) + xlabelh_u);
    }

    // 設定陣列
    public  void setWavedata(JSONObject wavedata) {
        this.wavedata = wavedata;
    }

    public void setOption(JSONObject option){
        this.chartoption = option;
    }
    // 設定 Ymin
    public void setYmin(int ymin) {
        this.Ymin = ymin;
    }

    // 設定 Ymax
    public void setYmax(int ymax) {
        this.Ymax = ymax;
    }

    // 設定 Yrange
    public void setYrange(int ymin, int ymax){
        this.Ymax = ymax;
        this.Ymin = ymin;
    }

    // 設定 Xmin
    public void setXmin(int xmin) {
        Xmin = xmin;
    }

    // 設定 Xmax
    public void setXmax(int xmax) {
        Xmax = xmax;
    }

    // 設定 Yrange
    public void setXrange(int xmin, int xmax){
        this.Xmax = xmax;
        this.Xmin = xmin;
    }

    // 設定 Tilte
    public void setTitle(String title){
        this.title = title;
    }

    // ********** 函數區 *************************************


    // 標題文字
//    function text_Title(ctx, textcolor, text, x0, y0){
//        ctx.fillStyle = uchart_color_Text;	// 黑色
//        ctx.textAlign = "center";
//        ctx.font = "15px Arial";
//        ctx.fillText(text, x0, y0);
//    }
//
//
//    // 數值顯示
//    function text_Value(value, val_x, val_y, val_w){
////			alert(value + val_x + val_y + val_w);
//        ctx.fillStyle = uchart_color_Text;	// 黑色
//        ctx.textAlign = "center";
//        ctx.font = "12px Arial";
////			alert("parseFloat(value/Ypow).toFixed(2) = " + parseFloat(value/Ypow).toFixed(2));
//        ctx.fillText(parseFloat(value/Ypow).toFixed(2), val_x, val_y, val_w);
//    }
//
//    // X軸文字(文字類型)
//    function label_Xaxis(ctx, xaxislabel, direction){
////		alert("xaxixlabel = " + JSON.stringify(xaxislabel));
//        ctx.fillStyle = uchart_color_Text;	// 黑色
//        ctx.textAlign = "center";
//        ctx.font = "12px Arial";
//        var xaxislabellength = xaxislabel.length;
//        if (direction == 'reverse'){
//            for (var i = 0; i < xaxislabellength; i++) {
//                var xlabel = xaxislabel[xaxislabellength - 1 - i];
//                var labelwidth = ctx.measureText("" + xlabel).width;
//                ctx.fillText(xlabel,get_xpos(i, 0),(chartH + xlabelh_u + xlabelh_d - 10), labelwidth);
//            }
//        } else {
//            for (var i = 0; i < xaxislabellength; i++) {
//                var xlabel = xaxislabel[i];
//                var labelwidth = ctx.measureText("" + xlabel).width;
//                ctx.fillText(xlabel,get_xpos(i, 0),(chartH + xlabelh_u + xlabelh_d - 10),labelwidth);
//            }
//        }
//    }
//
//    // Y軸文字
//    function text_Yaxis(ctx, Yaxismax, Yaxismin){
//        var poststr = "";
//        ctx.fillStyle = uchart_color_Text;	// 黑色
//        ctx.textAlign = "center";
//        ctx.font = "14px Arial";
//        for (var i = Yaxismin; i<=Yaxismax; i+=Ypow){
//            var ygridlocation = chartH + xlabelh_u -  chartH * (i - Yaxismin)/ (Yaxismax - Yaxismin);
//            var labelwidth = ctx.measureText("" + i).width;
//            ctx.fillText(i ,ylabelw_l - labelwidth - 2,(ygridlocation + 5),ylabelw_l*0.8);
//        }
//    }


    private float get_barxpos_left(float Xdata, float xshift){	// 柱狀圖塊的 X (左)
        return ylabelw_l + unit_w * (Xdata - Xmin) -  unit_w * xshift;
    }

    private float get_barwidth(float w){
        return unit_w * w;
    }

    private float get_barheight(float h){
        return unit_h * h;
    }

    private int Yaxismin, Yaxismax;
    private float Ypow;
    private int ylog;
    private void getYscale(float ymax, float ymin){
        float absymin = (float) Math.abs(ymin * 1.1);
        float absymax = (float) Math.abs(ymax * 1.1);
        Log.e(TAG, " ymin =" + ymin + " ymax = " + ymax + " absymin " + absymin + " absymax " + absymax);
        float yscale = absymax > absymin?absymax:absymin;   // get max(absymax, absymin)
            Log.e(TAG, "yscale = absymax > absymin?absymax:absymin " + yscale);
            if (yscale != 0){
                //alert("Math.log(" + yscale + ") = " + Math.log(yscale)/Math.log(10));
                ylog = (int) Math.floor(Math.log(yscale)/Math.log(10));
                Log.e(TAG, "ylog = log10yscale " + ylog);
                Ypow =(float) Math.pow(10, ylog);
                Log.e(TAG, "Ypow = " + Ypow);
                int Yscale =(int) Math.ceil(yscale/Ypow);
                Log.e(TAG, "Ypow " + Ypow + " Yscale = " + Yscale);
                if (ymin >= 0){	//	ymin > 0  表示 ymax, ymin 都>0
                    Log.e(TAG, "ymin > 0");
                    Ymax = Yscale * Ypow;	Yaxismax = Yscale;
                    Ymin = 0;				Yaxismin = 0;
                } else if (ymax < 0) {	// 表示 ymax, ymin 都< 0
                    Log.e(TAG, "ymax < 0");
                    Ymax = 0;				Yaxismax = 0;
                    Ymin = -Yscale * Ypow;	Yaxismin = -Yscale;
                } else {
                    if (ymax + ymin > 0) {	// ymax正 ymin負
                        Log.e(TAG, "ymax + ymin > 0");
                        Ymax = Yscale * Ypow; Ymin = -Ymax;	Yaxismax = Yscale; Yaxismin = -Yscale;
                    } else {
                        Log.e(TAG, "ymax + ymin < 0");
                        Ymin = -Yscale * Ypow; Ymax = -Ymin;Yaxismax = Yscale; Yaxismin = -Yscale;
                    }
                }
            }
            Log.e(TAG, "ymin" + ymin + " ymax" + ymax + " Yaxismin" + Yaxismin + " Yaxismax" + Yaxismax);
    }

    // 陣列反向
    private JSONArray reverseArray(JSONArray array){
        JSONArray reverse = new JSONArray();
        int arraylen = array.length();
        try {
            for (int i = 0; i < arraylen; i++) {
                reverse.put(array.get(arraylen - 1 - i));
            }
        } catch (JSONException e){

        }
        return reverse;
    }

    // 陣列取最大值
    private float getArraymaximum(JSONArray datalist){
        double max = 0;
        try {
            max = datalist.getDouble(0);
            int arraylength = datalist.length();
            for (int i=0; i<arraylength; i++){
                if (max < datalist.getDouble(i)) max = datalist.getDouble(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return (float) max;
    }

    // 陣列取最大值
    private float getArrayminimum(JSONArray datalist){
        double min = Integer.MAX_VALUE;
        try {
            min = datalist.getDouble(0);
            int arraylength = datalist.length();
            for (int i=0; i<arraylength; i++){
                if (min > datalist.getDouble(i)) min = datalist.getDouble(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return (float) min;
    }
}   // end of Class DrawChartView