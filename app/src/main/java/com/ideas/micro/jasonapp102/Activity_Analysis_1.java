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

public class Activity_Analysis_1 extends AppCompatActivity {
    private String TAG = "資料分析_1";
    private final int C0Rowindex = 1;       // C0 在第二個Row, index = 1
    private int meridiancount = 11;         // C0 - C10 十一個脈象
    private int chartindex = 0;                 // 第一張圖
    private JSONObject reportName = new JSONObject();
    private JSONObject reportData = new JSONObject();
    private JSONObject reportString = new JSONObject();
    private JSONObject reportCols = new JSONObject();
    private String[] meridiankeys = {"amp", "ampv", "ampir", "ampr", "phs", "phsv", "phsir", "phsr"};
    private String[] meridianname = {"能量", "血壓諧波變異係數", "能量虛實", "能量比", "相位", "相位變異", "相位虛實", "相位比"};
    private int[] meridianresource = {
        R.string.amplitudes ,R.string.amplitudes_variance,R.string.amplitudes_realimg,R.string.amplitudes_ratio,
        R.string.phase,R.string.phase_variance,R.string.phase_realimg,R.string.phase_ratio};
    private int[] meridiancols = {6, 9, 7, 8, 10, 13, 11, 12};
    private String[] displaykeys ; //= {"amp", "ampv", "phs", "phsir", "phs", "phsv", "phsir"}; // 顯示用
    private float[][] fft_dataarray = new float[14][18];
    private float hrvariant;   // 心跳變異

    private TextView tvTitle;
    private ListView listView;
    private Button btn_prev, btn_next;
    private TextView text_bpmanalysisreport, text_personal, text_time, text_bloodpresure;
    private SingleUser user = SingleUser.getInstance();
    private Intent getintent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate...");
        initializeParameter();      // 初始化數據參數
        setContentView(R.layout.activity_analysis_1);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_analysis_title);
        text_bpmanalysisreport = (TextView) findViewById(R.id.text_bpmanalysisreport);
        text_personal = (TextView) findViewById(R.id.text_personal);
        text_time = (TextView) findViewById(R.id.text_time);
        text_bloodpresure = (TextView) findViewById(R.id.text_bloodpresure);
        listView = (ListView) findViewById(R.id.analysislist);
        setFFTData();       // 先執行 setFFTData 才能找到心跳變異
        setReportHead();
        //listView.setOnItemClickListener(onClickListView);       //指定事件 Method

    }       // end of oncreate

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop...");
    }

    // 設計報告表頭
    private void setReportHead() {
        // 直接從上一個頁面傳遞資料過來
        getintent = this.getIntent();
        String gender = user.getUsergender().equals("M") ? getResources().getString(R.string.radio_registermale) :getResources().getString(R.string.radio_registerfemale);
        String age = Utility.GetAge(user.getUserbirthday()) + getResources().getString(R.string.age);
        String position = getintent.getIntExtra("position", 0) == 10 ? getResources().getString(R.string.switch_lefthand) :getResources().getString(R.string.switch_righthand);
        String posture = getintent.getIntExtra("posture", 0) == 10 ? getResources().getString(R.string.switch_sitting) :getResources().getString(R.string.switch_lie);
        String datetime = getintent.getStringExtra("date") + " " + getintent.getStringExtra("time");
        String bp = getResources().getString(R.string.label_bloodpressure) + " " + getintent.getIntExtra("sbp", 0) + " / " + getintent.getIntExtra("dbp", 0);
        String hr = getResources().getString(R.string.label_heartbeatrate) + " " + getintent.getIntExtra("hr", 0);
        String heartratevariant = getResources().getString(R.string.heartrate_variance) + " " + String.format("%.2f", hrvariant * 100) + "%";
        Log.e(TAG, bp + hr + heartratevariant);
        text_personal.setText(user.getUsername() + "( " + gender + " " + age + ")" + " " + position + " " + posture);
        text_time.setText(getResources().getString(R.string.name_measuretime) + datetime);
        text_bloodpresure.setText(bp + "  " +  hr + "  " + heartratevariant );
    }

    //初始化參數
    private void initializeParameter() {
        chartindex = 0;         // 第一張圖
        try {
            for (int i = 0; i < meridiankeys.length; i++) {
                String key = meridiankeys[i];       // 所有的key
//                reportName.put(key, getResources().getString(meridianresource[i]));
                reportName.put(key, meridianname[i]);
                reportData.put(key, new JSONArray());
                reportString.put(key, new JSONArray());
                reportCols.put(key, meridiancols[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //    setFFTData -> setDrawData
    private void setFFTData() {
        String fft = this.getIntent().getStringExtra("FFT_report"); // Data from Activity_ChartWave
        String bestCut = this.getIntent().getStringExtra("bestCut");
        Log.e(TAG, "bestCut = " + bestCut + "\n FFT_report" + fft);
        float[] fft_ampv = new float[14];
        float[] fft_ampv_limit = new float[]  {5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f};
        int redCount = 0;
        try {
            // 改寫以下取的fft_array 的方法，改用 bestCut 欄位
            // JSONArray fft_array = Utility_Meridian.umed_getMaxCutSection(fft); // fft_json.getJSONArray("s" + (fft_jsonsection - 1));     // 取用最後一組資料 s3
            Log.e(TAG, "bestCut = " + bestCut);
            JSONArray fft_array = new JSONObject(fft.replaceAll("'", "\"")).getJSONArray("s" + bestCut);

            if (fft_array.length() == 18 * 14) {
                // 確認資料長度正確
                // 將一維資料轉換為二維資料
                Log.e(TAG, "資料長度 18 * 14  fft_array = " + fft_array.toString());
                int ampvCol = 9;
                for (int colindex = 0; colindex<14; colindex++) {
                    fft_ampv[colindex] = (float) fft_array.getDouble(colindex * 18 + ampvCol);
                    for (int rowindex = 0; rowindex<18; rowindex++){
                        try {
                            fft_dataarray[colindex][rowindex] = (float) fft_array.getDouble(colindex * 18 + rowindex);
                        } catch (JSONException ee){
                            Log.e(TAG, "JSON 錯誤 at col" + colindex + " row" + rowindex);
                        }
                    }
                }
                hrvariant = fft_dataarray[1][reportCols.getInt("phsv")];        // 心跳變異 = H0 相位變異
//                 統計紅字數量
                Log.e(TAG, "FFT_能量變異 H0 ~ H10 = " + fft_ampv.toString());
                for (int harmonicindex = 0; harmonicindex < 11; harmonicindex ++){
                    if (fft_ampv[harmonicindex + 1] > fft_ampv_limit[harmonicindex]) {
                        redCount++;
                    }
                }
                Log.e(TAG, "FFT_能量變異有 " + redCount + " 個紅字");
                if (redCount > 0){
//                    Utility_Alert.showAlertDialog(Activity_Analysis_1.this, R.string.dialog_msg_bpmanlaysisresult,
//                            R.string.dialog_OK, Utility_Alert.doNothing);
                }
                Log.e(TAG, "資料轉換完畢");
                setDrawData();
            } else {
                Utility_Alert.showAlertDialog(Activity_Analysis_1.this, R.string.dialog_msg_fftcounterror,
                        R.string.dialog_OK, Utility_Alert.doNothing);
            }
        } catch (JSONException e) {
            // JSONArray 轉換失敗
            Utility_Alert.showAlertDialog(Activity_Analysis_1.this, R.string.dialog_msg_jsontranserror,
                    R.string.dialog_OK, Utility_Alert.doNothing);
            e.printStackTrace();
        }
    }       // end of setFFTData

    //    // 整理顯示圖表的資料
    private void setDrawData(){
        Log.e(TAG, "開始整理資料準備繪圖");
        // 根據會員資格選取顯示內容
//                if (user.getUserLevel == 0) {
//                    displaykeys =new String[] {"amp", "ampv"}; // 顯示用
//                } else {
//                    displaykeys =new String[] {"amp", "ampv"}; // 顯示用
//                }
//        displaykeys =new String[] {"ampir", "ampv"}; // 振幅變異要放在虛實後面，作為紅字判斷的基準
        displaykeys =new String[] {"ampv"}; // 振幅變異要放在虛實後面，作為紅字判斷的基準
            for (int keyindex = 0; keyindex < displaykeys.length; keyindex++) {  // chartcount 表示要顯示幾個欄位
                String key = displaykeys[keyindex];     // key 表示要處理的特徵
                try {
                    reportString.getJSONArray(key).put(0, reportName.getString(key));
                } catch (JSONException e) {

                }
                for (int i = 0; i < meridiancount; i++) {        // i = 0 顯示欄位名稱  C0 - C10  meridiancount = 11
                    try {
                        float value = fft_dataarray[i + 1][reportCols.getInt(key)];
                        if (i == 0) {
                            if (key.equals("amp")) {        // H0  振幅 要除以 100
                                reportData.getJSONArray(key).put(i, value / 100);
                                reportString.getJSONArray(key).put(i + 1, String.format("%.2f", value / 100));
                            } else if (key.equals("ampv") || key.equals("phsv")){        // H0  ampv, phsv 要乘以 100
                                reportData.getJSONArray(key).put(i, value * 100);
                                reportString.getJSONArray(key).put(i + 1, String.format("%.2f", value * 100));
                            } else {
                                reportData.getJSONArray(key).put(i, value);
                                reportString.getJSONArray(key).put(i + 1, String.format("%.2f", value));
                            }
                        } else {
                            reportData.getJSONArray(key).put(i, value);
                            reportString.getJSONArray(key).put(i + 1, String.format("%.2f", value));
                            //                            ampphsdata0.getJSONArray(key).put(i, value);                                    //] / averagejson[key][r_pos]['H' + i] - 1) * 100;
                        }
                    } catch (JSONException e) {
                        try {
                            Log.e(TAG, "N/A");
                            reportData.getJSONArray(key).put(i, 0);
                            reportString.getJSONArray(key).put(i + 1, "N/A");
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                    } //
                }   // end of i
            } // end of keyindex
        showChartAndData();
    }

    private void showChartAndData() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewAdapter_Analysis_1 adapter = null;
        Log.e(TAG, "chartindex = " + chartindex);
        adapter = new ViewAdapter_Analysis_1(displaykeys, reportString, inflater);
        listView.setAdapter(adapter);
    }

}