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

public class View_BPM extends View {
    public JSONArray pulsewave1 = new JSONArray();      // 脈診波一
    public JSONArray pulsewave2 = new JSONArray();      // 脈診波二
    public JSONArray bpwave = new JSONArray();              // 血壓波
    public JSONArray wavedata = new JSONArray();
    private final String TAG = "View_BPM";
    private int xshift, yshift;
    private int chartW, chartH;
    private float xspan, yspan;
    public int Ymin, Ymax, Xmin, Xmax;
    public int isWaving = -1;
    public int bpmin = Integer.MAX_VALUE, bpmax = 0, bpdif = 1;
    public int p1min =  Integer.MAX_VALUE, p1max = 0, p1dif = 1;
    public int p2min =  Integer.MAX_VALUE, p2max = 0, p2dif = 1;

    public View_BPM(Context context) {  // 建構式
        super(context);
        wavedata = new JSONArray();
        Ymin = 8000000; Ymax = 8500000;       // 首次設定
        Xmin = 0; Xmax = 1220;
    }

    private void setParameter(Canvas canvas){
//            Log.e(TAG, "設定參數 setParameter");
        xshift = 10;            yshift = 10;
        //Ymin = 8000000; Ymax = 8500000;       由藍牙波段設定
        chartW = canvas.getWidth() - xshift - xshift;
        chartH = canvas.getHeight() - yshift - yshift;
        xspan = Float.valueOf(chartW) / (Xmax - Xmin);
        yspan = Float.valueOf(chartH) / (Ymax - Ymin);
        //Log.e(TAG, "參數：xspan=" + xspan + " yspan=" + yspan );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
            Paint p = new Paint();								// 創建畫筆
            p.setAntiAlias(true);									// 設置畫筆的鋸齒效果。 true是去除。
            p.setColor(Color.RED);								// 設置紅色
            setParameter(canvas);
            drawBackFrame(canvas, p);
            drawGrid(canvas, p);
            //drawWave(canvas, p, wavetoshow);//
           drawWavearray(canvas, p, wavedata, "#00FF00");
//            if (isWaving == 1){
//                drawWavearray(canvas, p, wavedata, "#00FF00");
//            } else if (isWaving == 2) {
//                drawWavearray(canvas, p, bpwave, "#FFFFFF");
//            } else if (isWaving == 3) {
//                draw3Wavearray(canvas, p, bpwave, "#FFFFFF", 0);
//                draw3Wavearray(canvas, p, pulsewave1, "#FFFF00",1);
//                draw3Wavearray(canvas, p, pulsewave2, "#00FF00",2);
//            }
            drawOutsideFrame(canvas, p);
            //p.setTextSize(48);										// 設置文字的大小為 16。
            //canvas.drawText("time：" + timeelapsed + " ms",chartW + xshift - 450,chartH + yshift - 30,p);		// 寫一段文字
            //canvas.drawCircle(80,20,20,p);
        }

        // 繪製底框
        private void drawBackFrame(Canvas canvas, Paint p){
//            Log.e(TAG, "背景 drawBackFrame");
            p.setColor(Color.parseColor("#000000"));
            p.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
        }

        // 繪製外框
        private void drawOutsideFrame(Canvas canvas, Paint p){
//             Log.e(TAG, "外框 drawOutsideFrame");
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
                    float ypos = ybase - (yspan * ( wave2show.getInt(waveindex + startindex) - Ymin)) +yshift;
                    wave.lineTo(get_xpos(waveindex),  ypos);
                }
            } catch (JSONException e) {
                Log.e(TAG, "繪圖區錯誤 " + e.toString());
                e.printStackTrace();
            }
            //wave.close();//封閉
            canvas.drawPath(wave, p);
        }
        private float get_xpos(int Xdata) {
            return xshift + xspan * (Xdata - Xmin);
        }

        private float get_ypos(int Ydata){
        return  chartH -  (yspan * (Ydata - Ymin)) + yshift;
        }
    }   //  end of class View_BPM
