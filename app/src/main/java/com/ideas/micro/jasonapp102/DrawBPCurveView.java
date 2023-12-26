package com.ideas.micro.jasonapp102;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

public class DrawBPCurveView extends View {
    private final String TAG = "DrawView";
    private Context context;
    private boolean isDrawing = false;
    private  JSONArray wavedata;
    private int Ymin, Ymax, Xmin, Xmax;
    private boolean hasYaxisScale;
    private int yaxis_L, yaxis_H;

//    private int xshift = 10, yshift = 10;
    private int xlabelh_u = 15, xlabelh_d = 15, ylabelw_l = 15, ylabelw_r = 15;     // X軸上下，Y軸左右
    private int ylabeltextw_l = 0;   // Y軸左側文字寬度
    private int ylabeltextsize = 24;
    private int chartW, chartH;     // 必須先取得 canvas
    private int canvasW, canvasH;
    private float xspan, yspan;
    private JSONArray bpwave = new JSONArray();
    private JSONArray pulsewave1 = new JSONArray();
    private JSONArray pulsewave2 = new JSONArray();
    private int bpmin, bpmax, p1min, p1max, p2min,p2max;
    private int isWaving = 0;

    // constructor 建構式多型 I
    public DrawBPCurveView(Context context){
        super(context);
        wavedata = new JSONArray();     // 繪圖用資料
        Ymin = 8000000; Ymax = 8500000;       // 首次設定
        Xmin = 0; Xmax = 1220;
        setWillNotDraw(false);
    }

    // constructor 建構式多型 II
    public DrawBPCurveView(Context context, int ymin, int ymax, int xmin, int xmax, boolean hasYaxisScale, int yaxix_L, int yaxis_H) {
        super(context);
        this.context = context;
        this.wavedata = new JSONArray();     // 繪圖用資料
        this.Ymin = ymin;
        this.Ymax = ymax;       // 首次設定
        this.Xmin = xmin;
        this.Xmax = xmax;
        this.hasYaxisScale = hasYaxisScale;
        if (hasYaxisScale){
            Paint p = new Paint();
            p.setTextSize(ylabeltextsize);
            ylabeltextw_l = (int) p.measureText("999");   // 取三位整數
            xlabelh_u = (int) (p.descent() - p.ascent());
            xlabelh_d = (int) (p.descent() - p.ascent());
            Log.e(TAG, "p.descent() = " + p.descent() + " p.ascent() = " + p.ascent());
            ylabelw_l = ylabeltextw_l + 20;
        } else {
            // 不需要修改座標軸寬度
        }
        this.yaxis_L = yaxix_L; this.yaxis_H = yaxis_H;
        setWillNotDraw(false);
    }

    public void refreshDraw() {
        // invalidate 會呼叫 View 物件的 onDraw
        Log.e(TAG, "準備  refreshDraw ");
        if  (isDrawing) {
        } else {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e(TAG, "繪圖 onDraw 準備繪圖");
        Log.e(TAG, "繪圖 wavedata 長度 = " + wavedata.length());
//        Log.e(TAG, "繪圖 wavedata = " + wavedata.toString());
        isDrawing = true;
        super.onDraw(canvas);
        canvasW = canvas.getWidth();
        canvasH = canvas.getHeight();
        Paint p = new Paint();								// 創建畫筆
        p.setAntiAlias(true);									// 設置畫筆的鋸齒效果。 true是去除。
        p.setColor(Color.RED);								// 設置紅色
        setParameter(canvas);
        drawBackFrame(canvas, p);
        drawGrid(canvas, p);
        //drawWave(canvas, p, wavetoshow);
        if (isWaving == 1){
            drawWavearray(canvas, p, wavedata, "#00FF00");
        } else if (isWaving == 2) {
            drawWavearray(canvas, p, bpwave, "#FFFFFF");
        } else if (isWaving == 3) {
            draw3Wavearray(canvas, p, bpwave, "#FFFFFF", 0);
            draw3Wavearray(canvas, p, pulsewave1, "#FFFF00",1);
            draw3Wavearray(canvas, p, pulsewave2, "#00FF00",2);
        }
        drawOutsideFrame(canvas, p);
        drawYaxisText(canvas, p);
        isDrawing = false;
        Log.e(TAG, "繪圖 onDraw 繪圖結束");
        //p.setTextSize(48);										// 設置文字的大小為 16。
        //canvas.drawText("time：" + timeelapsed + " ms",chartW + xshift - 450,chartH + yshift - 30,p);		// 寫一段文字
        //canvas.drawCircle(80,20,20,p);
    }

    private void setParameter(Canvas canvas){
        //  canvas 由 onDraw 設定
//            Log.e(TAG, "繪圖 onDraw  設定參數 setParameter");
        //Ymin = 8000000; Ymax = 8500000;       由藍牙波段設定
        try {
            Log.e(TAG, "畫布尺寸 WxH = " + canvas.getWidth() + "x" + canvas.getHeight());
            Log.e(TAG, "座標高度 Ymin = " + Ymin + " Ymax = " + Ymax);
            chartW = canvasW - ylabelw_l - ylabelw_r;
            chartH = canvasH - xlabelh_d - xlabelh_u;
            xspan = Float.valueOf(chartW) / (Xmax - Xmin);
            yspan = Float.valueOf(chartH) / (Ymax - Ymin);
        } catch (ArithmeticException e) {
            Log.e(TAG, "除以零例外 : Xmax = " + Xmax + ", Xmin = " + Xmin + ", Ymax = " + Ymax + ", Ymin = " + Ymin);
            Log.e(TAG, e.toString());   // 除以零
        }
    }

    // 繪製底框
    private void drawBackFrame(Canvas canvas, Paint p){
//            Log.e(TAG, "背景 drawBackFrame");
        p.setColor(Color.parseColor("#000000"));
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
    }

    // 繪製外框 最後的步驟 將超過邊界的曲線塗銷
    private void drawOutsideFrame(Canvas canvas, Paint p){
//             Log.e(TAG, "外框 drawOutsideFrame");
        p.setColor(Color.parseColor("#000000"));       // 黑色
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, ylabelw_l, canvasH + xlabelh_u, p);    // 左側區塊
        canvas.drawRect(0, 0, ylabelw_l+ canvasW, xlabelh_u, p);   // 上方區塊
        canvas.drawRect(0, chartH + xlabelh_u, canvasW , canvasH , p);  // 下方區塊
        canvas.drawRect(chartW + ylabelw_l, 0, canvasW, canvasH, p);  // 右側區塊

//        p.setStyle(Paint.Style.STROKE);
//        p.setStrokeWidth(xshift);
//        canvas.drawRect(xshift/2, xshift/2, canvas.getWidth() - xshift/2, canvas.getHeight() - xshift/2, p);
        p.setColor(Color.parseColor("#FFFFFF"));           // 白色
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5);
        canvas.drawRect(ylabelw_l, xlabelh_u, chartW + ylabelw_l, chartH + xlabelh_u, p);
    }

    // 繪製格線
    private int Yaxis_Span = 4;     // 區分為四等分
    private void drawGrid(Canvas canvas, Paint p){
        p.setColor(Color.parseColor("#FFFFFF"));
        p.setStrokeWidth(1);
        for (int xindex = 1; xindex < 10; xindex ++){   // 畫 9 條垂直的格線
            Float xgridposition = Float.valueOf(chartW / 10 * xindex + ylabelw_l);
            canvas.drawLine(xgridposition, xlabelh_u, xgridposition, chartH + xlabelh_u, p);
        }
        for (int yindex = 1; yindex < Yaxis_Span; yindex ++) {    // 畫 4條水平的格線
            Float ygridposition = Float.valueOf(chartH / Yaxis_Span * yindex + xlabelh_u);
            canvas.drawLine(ylabelw_l, ygridposition, chartW + ylabelw_l, ygridposition, p);
        }
    }


    private void drawYaxisText(Canvas canvas, Paint p) {
        for (int yindex = 0; yindex <= Yaxis_Span; yindex ++) {    //
            Float ygridposition = Float.valueOf(chartH / Yaxis_Span * yindex + xlabelh_d);
            if (hasYaxisScale) {
                int yvalue = yaxis_H - ( yaxis_H - yaxis_L ) * yindex / Yaxis_Span ;
                draw_Ylabel(canvas, p, yvalue, ygridposition, "#FFFFFF");
            }
        }
    }

    //
    private void draw_Ylabel(Canvas canvas, Paint p,  int value, float ypos, String gridcolor){
        p.setColor(Color.parseColor(gridcolor));
        p.setStrokeWidth(1);
        p.setTextSize(ylabeltextsize);
        String _value = String.valueOf(value);
        float xPos = ylabelw_l - p.measureText(_value)  - 10;
        canvas.drawText(_value, xPos, ypos - ((p.descent() + p.ascent()) / 2), p);
        }

    // 繪製波型
    // 將繪製的陣列傳入
    private void drawWavearray(Canvas canvas, Paint p, JSONArray wave2show, String linecolor) {
        //Log.e("DRAW", "onDraw at " + wave2show);
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

    private void draw3Wavearray(Canvas canvas, @NotNull Paint p, @NotNull JSONArray wave2show, String linecolor, int index) {
        Log.e(TAG, "onDraw at draw3Wavearray");
        float ybase = chartH -  chartH * index / 3;
        switch (index) {
            case 0:
                Ymin = bpmin; Ymax = bpmax;
                break;
            case 1:
                Ymin = p1min; Ymax = p1max;
                break;
            case 2:
                Ymin = p2min; Ymax = p2max;
                break;
        }
        p.setColor(Color.parseColor(linecolor));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        Path wave = new Path();
        try {
            int wavedatacount = wave2show.length();    // 實際上使用
            Xmax = wavedatacount;
            //int wavedatacount = wave2show;               // 虛擬測試
            int startindex = 0;
            startindex = wavedatacount - Xmax;
            xspan = Float.valueOf(chartW) / (Xmax - Xmin);
            yspan = Float.valueOf(chartH) / (Ymax - Ymin) / 3;
            wave.moveTo(get_xpos(0), get_ypos(wave2show.getInt(startindex)));
            for (int waveindex = 0; waveindex < Xmax; waveindex++) {
                float ypos = ybase - (yspan * ( wave2show.getInt(waveindex + startindex) - Ymin)) +xlabelh_u;
                wave.lineTo(get_xpos(waveindex),  ypos);
            }
        } catch (JSONException e) {
            Log.e(TAG, "繪圖區錯誤 " + e.toString());
            e.printStackTrace();
        }
        //wave.close();//封閉
        canvas.drawPath(wave, p);
    }


    private float get_xpos(int Xdata){
        return ylabelw_l + xspan * (Xdata - Xmin);
    }
    private float get_ypos(int Ydata){
        return  chartH -  (yspan * (Ydata - Ymin)) + xlabelh_u;
    }

    // 設定陣列
    public  void setWavedata(JSONArray wavedata) {
        this.wavedata = wavedata;
    }

    public void setBpwave(JSONArray bpwave) {
        this.bpwave = bpwave;
    }

    public void setPulsewave1(JSONArray pulsewave1) {
        this.pulsewave1 = pulsewave1;
    }

    public void setPulsewave2(JSONArray pulsewave2) {
        this.pulsewave2 = pulsewave2;
    }

    // 設定 isWaving
    public void setIsWaving(int isWaving){
        this.isWaving = isWaving;
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

    public void setYaxis(int axisL, int axisH) {
        this.yaxis_L = axisL;
        this.yaxis_H = axisH;
    }

}
