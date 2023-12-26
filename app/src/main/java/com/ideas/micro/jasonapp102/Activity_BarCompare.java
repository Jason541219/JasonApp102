package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Activity_BarCompare extends AppCompatActivity implements onHttpPostCallback {
    private final String TAG = "前後測";
    private TextView tvTitle;
    private TextView txt_colorafter, txt_colorbefore;
    private String colorBefore, colorAfter;
    private int positionBefore, positionAfter;
    private String postKey;
    private ProgressBar progressBar_barchart;
    private final SingleUser user =  SingleUser.getInstance();
    private ListView listView;
    private DrawChartView drawChartView;
    private Spinner spinner_barchart;
    private LinearLayout barchartlayout;
    LayoutInflater inflater;
    ViewAdapter_BarChartList listAdapter;
    JSONArray recordlist = new JSONArray();
    JSONArray comparelist = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcompare);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_barcompare_title);
        txt_colorafter = (TextView) findViewById(R.id.txt_colorafter);
        txt_colorbefore = (TextView) findViewById(R.id.txt_colorbefore);
        progressBar_barchart = (ProgressBar) findViewById(R.id.progressBar_barchart);
        progressBar_barchart.setVisibility(View.GONE);   // 預設不顯示
        barchartlayout = (LinearLayout) findViewById(R.id.barchartlayout);
        spinner_barchart = (Spinner) findViewById(R.id.spinner_barchart);
        spinner_barchart.setVisibility(View.GONE);
        spinner_barchart.setOnItemSelectedListener(spnOnItemSelected);
        setDrawLayout();

        listView = (ListView) findViewById(R.id.barchartlist);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        getMyRecordList();  // 查詢 (預設一個月內)

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {   // 沒有反應
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "OnItemClick on position " + position);
                hasChoosed[position] = ! hasChoosed[position];
//                int haschoosedcounnt = hasChoosedCount();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (hasChoosedCount() < 2) {
                            listAdapter.setHasChoosed(position);
                            listAdapter.setColor(colorAfter, colorBefore);
                            listAdapter.setChoosedPosition(positionAfter, positionBefore);
                            listView.invalidateViews();
                            Log.e(TAG, "前 " + positionBefore + " 後 " + positionAfter);
                            writeHx2Table("ampv"); //      預設能量變異
                        } else if (hasChoosedCount() == 2) {
                            listAdapter.setHasChoosed(position);
                            listAdapter.setColor(colorAfter, colorBefore);
                            listAdapter.setChoosedPosition(positionAfter, positionBefore);
                            listView.invalidateViews();
                            Log.e(TAG, "前 " + positionBefore + " 後 " + positionAfter);
//                            writeHx2Table(spinner_barchart.getSelectedItemPosition() == 0? "ampir":"ampv");
                            writeHx2Table("ampv"); //      預設能量變異
                        } else if (hasChoosedCount() > 2) {
                            hasChoosed[position] = ! hasChoosed[position];
                            Log.e(TAG, "前 " + positionBefore + " 後 " + positionAfter);
                            Utility_Alert.showAlertDialog(Activity_BarCompare.this,
                                    "請勿選取超過兩組以上的資料。您可以再點選一下已選取的資料將之取消。",
                                    R.string.dialog_OK, Utility_Alert.doNothing);
                        }
                    }
                });
            }
        });

    }   // end of onCreate

    // 計算有幾個資料被選取
    private int hasChoosedCount(){
        int count = 0;
        comparelist = new JSONArray();
        positionBefore = -1;
        positionAfter = -1;
        for (int i = 0; i<hasChoosed.length; i++){
            if (hasChoosed[i]) {
                count ++;
                try {
                    if (count == 1){
                        positionAfter = i;
                    } else if (count == 2){
                        positionBefore = i;
                    }
                    comparelist.put(recordlist.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return count;
    }

    private AdapterView.OnItemSelectedListener spnOnItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            if (hasChoosedCount() == 2) {
                writeHx2Table(position == 0 ? "ampir" : "ampv");
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private void drawCompareBar(){

    }

    private void getMyRecordList(){     // 一個月內的紀錄 包括 FFT 資料
        int recordrange_month = -3;
        int userid = user.getUserID();
        JSONObject jqljson = new JSONObject();
        Calendar calendar = Calendar.getInstance();
        String DateTimeNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date(calendar.getTimeInMillis()));
        calendar.add(Calendar.MONTH, recordrange_month);        // 預設三個月內
        String DateTimeBefore =  new SimpleDateFormat("yyyy-MM-dd").format(new Date(calendar.getTimeInMillis()));
        Log.e(TAG, "診所名稱 " + GlobalVariables.Login_Clinic);
        try {
            // 只選擇 fromID = 0 的紀錄
//            jqljson.put("command",  "GetBPMWaveRecordListByMem_old");
            jqljson.put("command",  "GetBPMWaveRecordListByMem_mychart");
            jqljson.put("mid",  userid);
            jqljson.put("sdate", DateTimeBefore);
            jqljson.put("edate", DateTimeNow);
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "GetBPMWaveRecordListByMem";
        progressBar_barchart.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        HttpPost httpPost = new HttpPost(Activity_BarCompare.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_BarCompare.this, GlobalVariables.http_url, jqljson.toString());
    }

    // 設定日期列表資料
    private boolean[] hasChoosed;

    private void setListData(JSONArray dblist) {
        recordlist = dblist;
        listAdapter = new ViewAdapter_BarChartList(recordlist, inflater);
        listView.setAdapter(listAdapter);
        hasChoosed = new boolean[dblist.length()];  // 預設為 False
        Log.e(TAG, "hasChoosed[0] = " + hasChoosed[0]);
        ArrayAdapter<CharSequence> arrAdapSpn
                = ArrayAdapter.createFromResource(Activity_BarCompare.this, //對應的Context
                R.array.chartmode_array, //選項資料內容 R.array.chartmode_array
                R.layout.spinner_item); //自訂getView()介面格式(Spinner介面未展開時的View) R.layout.spinner_item

        arrAdapSpn.setDropDownViewResource(R.layout.style_spinner);
//        arrAdapSpn.setDropDownViewResource(R.layout.spinner_dropdown_item);
        //自訂getDropDownView()介面格式(Spinner介面展開時，View所使用的每個item格式)
        spinner_barchart.setAdapter(arrAdapSpn); //將宣告好的 Adapter 設定給 Spinner
    }

    @Override
    public void onComplete(String response) {
//        Log.e(TAG, response);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar_barchart.setVisibility(View.GONE);                // 恢復觸控功能
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
        try {
            final JSONObject responseJson = new JSONObject(response);
            if (responseJson.getString("status").equals("success")){      //
                final JSONArray dblist = responseJson.getJSONArray("dblist");
                Log.e(TAG, response);
                // 設定下拉式選單
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray comparearray = new JSONArray();
                            for (int dbindex = 0; dbindex < dblist.length(); dbindex++) {
                                JSONObject dbjson = dblist.getJSONObject(dbindex);
                                String fftstr = dbjson.getString("fft");
                                String bestcut = dbjson.getString("bestCut");
                                if (fftstr.equals("") || bestcut.equals("") || bestcut.equals("-1")) {

                                } else {
                                    comparearray.put(dbjson);
                                }
                            }
                            setListData(comparearray);
                        } catch (JSONException e){
                            Log.e(TAG, "comparearray JSON exception");
                        }
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_BarCompare.this);
                        String msg = null;
                        msg = getResources().getString(R.string.dialog_msg_norecorddatafountin3month);
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
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onFail(String err) {

    }


    // 將資料轉換為表格
    private JSONObject chartjson = new JSONObject();
    private void writeHx2Table(String mode){   // mode = ampv 或是 ampir
        JSONArray xarray = new JSONArray();
        JSONArray yarray = new JSONArray();
        JSONArray chartdata = new JSONArray();
        JSONObject chartoption = new JSONObject();
        Log.e(TAG, "Write Hx2Table");
        try {
            chartjson.put("x", new JSONArray());
            chartjson.put("before", new JSONArray());
            chartjson.put("after", new JSONArray());
//                chartjson.put("enable", hxLineEnable);
            for (int hxindex = 0; hxindex < 11; hxindex ++){
                chartjson.getJSONArray("x").put("H" + hxindex);
            }
            Log.e(TAG, "chartjson = " +  chartjson.toString());
            for (int tindex = 0; tindex < comparelist.length() ; tindex++){
                JSONObject jlist = comparelist.getJSONObject(tindex);
                String fftstr = jlist.getString("fft");
                String bestcut = jlist.getString("bestCut");
                if (fftstr == null || fftstr.equals("") || bestcut.equals("-1") || bestcut.equals("")) {      // 沒有資料就放棄
                    Log.e(TAG, "tablelist[" + tindex + "].bestcut = " + bestcut);
                    continue;
                } else {
                    JSONArray resultarray = Utility_Meridian.umed_getHxList(mode, fftstr, bestcut);
                    for (int hxindex = 0; hxindex < 11; hxindex++) {
                        float floatvalue = Float.parseFloat(resultarray.getString(hxindex));
                        if (tindex == 0) {                    // comparelist [ 0 ] 是後測                            // comparelist [ 1 ] 是前測
                            chartjson.getJSONArray("after").put(floatvalue);
                        } else {
                            chartjson.getJSONArray("before").put(floatvalue);
                        }
                    }
                }
            }
            Log.e(TAG, chartjson.toString());
            drawChartView.setWavedata(chartjson);
            chartoption.put("canvas_w",  1100);
            chartoption.put("canvas_h",  300);;
            chartoption.put("xmin",  0);
            chartoption.put("xmax",  11);
            chartoption.put("ymin", 0);
            chartoption.put("ymax", 10);;
            chartoption.put("ylabelw_l", 80);
            chartoption.put("ylabelw_r", 45);
            chartoption.put("xlabelh_u", 45);
            chartoption.put("xlabelh_d", 60);
            chartoption.put("charttype", "hx_compare");
            drawChartView.setOption(chartoption);
        } catch (JSONException e){
            Log.e(TAG, "JSONException at WriteHx2Table");
        }
        drawChartView.refreshDraw();
    }

    // **** 繪圖類別區域 **** //
    // 設定繪圖布局
    private void setDrawLayout() {
        //Log.e(TAG, "設定DrawLayout布局");
        barchartlayout = (LinearLayout) findViewById(R.id.barchartlayout);
        drawChartView = new DrawChartView(Activity_BarCompare.this);
        drawChartView.setMinimumHeight(500);
        drawChartView.setMinimumWidth(300);
        barchartlayout.addView(drawChartView);
        colorBefore = drawChartView.uchart_color_comparebar[0];
        colorAfter = drawChartView.uchart_color_comparebar[1];
        txt_colorbefore.setTextColor(Color.parseColor(colorBefore));
        txt_colorafter.setTextColor(Color.parseColor(colorAfter));

    }
}