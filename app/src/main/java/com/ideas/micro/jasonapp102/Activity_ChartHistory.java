package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Activity_ChartHistory extends AppCompatActivity  implements onHttpPostCallback {
    private final String TAG = "歷史折線";
    private TextView tvTitle;
    private boolean[] hxLineEnable = new boolean[]{true, true, true, true, true, true, true, true, true, true, true };
    private DrawChartView drawChartView;
    private final SingleUser user =  SingleUser.getInstance();
    private String postKey;
    private ProgressBar progressBar_chartupload;
    private int recordrange_month;
    private CheckBox check_sbp, check_dbp, check_hr;
    private CheckBox check_H0, check_H1, check_H2, check_H3, check_H4, check_H5, check_H6;
    private CheckBox check_H7, check_H8, check_H9, check_H10;
    private Spinner spinner_chart;
    private boolean isOnItemSelectedFirst = true;
    private JSONArray dblist = new JSONArray();
    private Utility_ActivityAlert activityAlert;
    private String date0, datef;
    private int dateint0, dateintf;
    private JSONArray xlabel_text = new JSONArray();
    private JSONArray xlabel_posi = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate(Bundle savedInstanceState)");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_history);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 給左上角圖標的左邊加上一個返回的圖標
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_charthistory_title);
        activityAlert = new Utility_ActivityAlert(this);
        setDrawLayout();
        spinner_chart = (Spinner) findViewById(R.id.spinner_chart);
//        spinner_chart.setVisibility(View.GONE);
        spinner_chart.setOnItemSelectedListener(spnOnItemSelected);
        ArrayAdapter<CharSequence> arrAdapSpn
                = ArrayAdapter.createFromResource(Activity_ChartHistory.this, //對應的Context
                R.array.chartmode_array, //選項資料內容
                R.layout.spinner_item); //自訂getView()介面格式(Spinner介面未展開時的View)

        arrAdapSpn.setDropDownViewResource(R.layout.spinner_dropdown_item); //自訂getDropDownView()介面格式(Spinner介面展開時，View所使用的每個item格式)
        spinner_chart.setAdapter(arrAdapSpn); //將宣告好的 Adapter 設定給 Spinner

        progressBar_chartupload = (ProgressBar) findViewById(R.id.progressBar_chartupload);
        progressBar_chartupload.setVisibility(View.GONE);   // 預設不顯示
        recordrange_month = 3;            // 預設三個月內
        check_sbp = (CheckBox) findViewById(R.id.check_sbp);
        check_dbp = (CheckBox) findViewById(R.id.check_dbp);
        check_hr = (CheckBox) findViewById(R.id.check_hr);
        check_H0 = (CheckBox) findViewById(R.id.check_H0);
        check_H1 = (CheckBox) findViewById(R.id.check_H1);
        check_H2 = (CheckBox) findViewById(R.id.check_H2);
        check_H3 = (CheckBox) findViewById(R.id.check_H3);
        check_H4 = (CheckBox) findViewById(R.id.check_H4);
        check_H5 = (CheckBox) findViewById(R.id.check_H5);
        check_H6 = (CheckBox) findViewById(R.id.check_H6);
        check_H7 = (CheckBox) findViewById(R.id.check_H7);
        check_H8 = (CheckBox) findViewById(R.id.check_H8);
        check_H9 = (CheckBox) findViewById(R.id.check_H9);
        check_H10 = (CheckBox) findViewById(R.id.check_H10);
        check_sbp.setOnCheckedChangeListener(checkedChangeListener);
        check_dbp.setOnCheckedChangeListener(checkedChangeListener);
        check_hr.setOnCheckedChangeListener(checkedChangeListener);
        check_H0.setOnCheckedChangeListener(checkedChangeListener);
        check_H1.setOnCheckedChangeListener(checkedChangeListener);
        check_H2.setOnCheckedChangeListener(checkedChangeListener);
        check_H3.setOnCheckedChangeListener(checkedChangeListener);
        check_H4.setOnCheckedChangeListener(checkedChangeListener);
        check_H5.setOnCheckedChangeListener(checkedChangeListener);
        check_H6.setOnCheckedChangeListener(checkedChangeListener);
        check_H7.setOnCheckedChangeListener(checkedChangeListener);
        check_H8.setOnCheckedChangeListener(checkedChangeListener);
        check_H9.setOnCheckedChangeListener(checkedChangeListener);
        check_H10.setOnCheckedChangeListener(checkedChangeListener);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, - recordrange_month);        // recordrange_month = -3; 從最早期的開始
        String datex = new SimpleDateFormat("yyyy-MM-dd").format(new Date(calendar.getTimeInMillis()));
        date0 = datex;                                  xlabel_text.put(datex);
        dateint0 = parseDate(date0);      xlabel_posi.put(dateint0 - dateint0 + 1);
        for (int i=0 ; i<recordrange_month; i++){
            calendar.add(Calendar.MONTH, 1);        //  往後一個月;
            datex = new SimpleDateFormat("yyyy-MM-dd").format(new Date(calendar.getTimeInMillis()));
            xlabel_text.put(datex);
            xlabel_posi.put(parseDate(datex) - dateint0 + 1);
        }
        datef = datex;
        dateintf = parseDate(datef);
        Log.e(TAG, "xlabel_text " + xlabel_text.toString());
        Log.e(TAG, "xlabel_pois " + xlabel_posi.toString());
        getMyRecordList();
    }

    private AdapterView.OnItemSelectedListener spnOnItemSelected = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
        {
            Log.e(TAG, "Choose position = " + position);
            if (isOnItemSelectedFirst) {
                isOnItemSelectedFirst = false;
            } else {
                writeHx2Table(position == 0 ? "ampir" : "ampv", dblist);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0)
        {
            // TODO Auto-generated method stub
        }
    };

    private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            onCheckboxChange();
        }
    };

    // 選單功能
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_myrecords, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.mylist_3month:
                recordrange_month = 3;
                break;
            case R.id.mylist_12month:
                recordrange_month = 12;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 讀取資料
    /* GetBPMWaveRecordListByMem_mychart
        僅限自宅量測，合世設備，報告已讀，原始切波
    	// APP 會用到，僅限用 Heshi 在家量測的資料，沒有手動切波。
			case "GetBPMWaveRecordListByMem_mychart":		// mid = -1 indicate all member
				sqlstr = "Select R.recordID AS rid, R.recordDate AS rdate, R.recordTime AS rtime, R.recordSBP AS sbp, R.recordDBP AS dbp, R.recordHR AS hr, " +
						 "F.recordFFT" + (p1p2.equals("2")?GlobalVariables.mdfft2:"") + " AS fft, R.bestCut" + p1p2 + " AS bestCut " +
						 "FROM MDrecord AS R, MDFFT AS F " +
						 "WHERE SUBSTRING(R.deviceType, 1, 5) = 'heshi' AND R.measureRole = 'M'  AND memberRead = 1 AND R.recordFFT_flag = 1 AND R.fromID = 0 AND R.memberID = " + sqljson.getInt("mid") + " AND R.recordID = F.recordID " +
						 "AND R.recordDate Between '" + sqljson.getString("sdate") + "' AND '" + sqljson.getString("edate") + "' " +
						 "Order By R.recordDate DESC, R.recordTime Desc";
				SqlResult = db.getDBList(clinicnamewi, sqlstr);		//
				break;
     */
    private void getMyRecordList(){
        int userid = user.getUserID();
        JSONObject jqljson = new JSONObject();
        try {
            // 只選擇 fromID = 0 的紀錄
            jqljson.put("command",  "GetBPMWaveRecordListByMem_mychart");
//            jqljson.put("command",  "GetBPMWaveRecordListByMem_old");
            jqljson.put("mid",  userid);
            jqljson.put("sdate", date0);
            jqljson.put("edate", datef);
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "紀錄期間 = " + date0 + " 到 " + datef);
        postKey = "GetBPMWaveRecordListByMem";      //
        progressBar_chartupload.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        HttpPost httpPost = new HttpPost(Activity_ChartHistory.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_ChartHistory.this, GlobalVariables.http_url,  jqljson.toString());
    }

    // 將資料轉換為表格
    private JSONObject chartjson = new JSONObject();

    private void writeHx2Table(String mode, JSONArray tablelist){
        JSONArray xarray = new JSONArray();
        JSONArray yarray = new JSONArray();
        JSONArray chartdata = new JSONArray();
        JSONObject chartoption = new JSONObject();
        int tablecount = 0;
        try {
                chartjson.put("x", new JSONArray());
                chartjson.put("sbp", new JSONArray());
                chartjson.put("dbp", new JSONArray());
                chartjson.put("hr", new JSONArray());
//                chartjson.put("enable", hxLineEnable);
                for (int hxindex = 0; hxindex < 11; hxindex ++){    // H0 ~ H10
                    chartjson.put("H" + hxindex, new JSONArray());
                }
                Log.e(TAG, "chartjson = " +  chartjson.toString());
                int tablelistlength = tablelist.length();
                for (int tindex = 0; tindex < tablelistlength ; tindex++){
                    JSONObject jlist = tablelist.getJSONObject(tindex);
                    String fftstr = jlist.getString("fft");
                    String bestcut = jlist.getString("bestCut");
                    if (fftstr == null || fftstr.equals("") || bestcut.equals("")) {      // 沒有資料就放棄
                        Log.e(TAG, "tablelist[" + tindex + "].fft = " + fftstr);
                        continue;
                    } else {
                        JSONArray resultarray = Utility_Meridian.umed_getHxList(mode, fftstr, bestcut);
                        chartjson.getJSONArray("sbp").put( jlist.getInt("sbp"));
                        chartjson.getJSONArray("dbp").put( jlist.getInt("dbp"));
                        chartjson.getJSONArray("hr").put( jlist.getInt("hr"));
                        chartjson.getJSONArray("x").put(parseDate(jlist.getString("rdate")) - dateint0 + 1);
                        for (int hxindex = 0; hxindex < resultarray.length(); hxindex++) {
                            float floatvalue = Float.parseFloat(resultarray.getString(hxindex));
//                            chartdata.getJSONObject(j).getJSONArray("x").put(jlist.getString("rdate").substring(5));
                            chartjson.getJSONArray("H" + hxindex).put(floatvalue);
                        }
                        tablecount ++;
                    }
                }
//                Log.e(TAG, chartjson.toString());
                JSONObject chartobj = setChartArray();
//                Log.e(TAG, chartdata.toString());
                drawChartView.setWavedata(chartobj);
                chartoption.put("canvas_w",  1100);
                chartoption.put("canvas_h",  300);;
                chartoption.put("xmin",  0);
                chartoption.put("xmax",  dateintf - dateint0 + 1 + 1);// 邊界 = Xmax, 最大 Xdata = 邊界減 1
                chartoption.put("ymin", 0);
                chartoption.put("ymax", 10);;
                chartoption.put("ylabelw_l", 80);
                chartoption.put("ylabelw_r", 45);
                chartoption.put("xlabelh_u", 45);
                chartoption.put("xlabelh_d", 60);
                chartoption.put("charttype", "hx_line");
                chartoption.put("xlabel_text", xlabel_text);
                chartoption.put("xlabel_posi", xlabel_posi);
                drawChartView.setOption(chartoption);
            } catch (JSONException e){
                Log.e(TAG, "JSONException at WriteHx2Table");
            }
//        uchart_Chart('hx_line', 'divmodelist', chartdata, chartoption);
            Log.e(TAG, "資料庫中有 " + tablecount + "筆資料");
            if (tablecount == 0){
                activityAlert.showAlertDialog(R.string.dialog_msg_norecorddatafount, R.string.dialog_OK,
                        activityAlert.doNothing);
            } else {
                drawChartView.refreshDraw();
            }
    }



    private JSONObject setChartArray(){
        Log.e(TAG, "setChartArray");
        JSONObject chobj = new JSONObject();
        try {
            chobj.put("x", chartjson.getJSONArray("x"));
//            Log.e(TAG, "x" + chobj.toString());
            if (check_sbp.isChecked())                 chobj.put("sbp", chartjson.getJSONArray("sbp"));
            if (check_dbp.isChecked())                chobj.put("dbp", chartjson.getJSONArray("dbp"));
            if (check_hr.isChecked())                   chobj.put("hr", chartjson.getJSONArray("hr"));
            if (check_H0.isChecked())   chobj.put("H0", chartjson.getJSONArray("H0"));
            if (check_H1.isChecked())   chobj.put("H1", chartjson.getJSONArray("H1"));
            if (check_H2.isChecked())   chobj.put("H2", chartjson.getJSONArray("H2"));
            if (check_H3.isChecked())   chobj.put("H3", chartjson.getJSONArray("H3"));
            if (check_H4.isChecked())   chobj.put("H4", chartjson.getJSONArray("H4"));
            if (check_H5.isChecked())   chobj.put("H5", chartjson.getJSONArray("H5"));
            if (check_H6.isChecked())   chobj.put("H6", chartjson.getJSONArray("H6"));
            if (check_H7.isChecked())   chobj.put("H7", chartjson.getJSONArray("H7"));
            if (check_H8.isChecked())   chobj.put("H8", chartjson.getJSONArray("H8"));
            if (check_H9.isChecked())   chobj.put("H9", chartjson.getJSONArray("H9"));
            if (check_H10.isChecked())   chobj.put("H10", chartjson.getJSONArray("H10"));
        } catch (JSONException e){
            Log.e(TAG, e.toString());
        }
        Log.e(TAG, "setChartArray Done\n" + chobj.toString());
        return chobj;
    }

    // **** 繪圖類別區域 **** //
    LinearLayout layout;
    //    Canvas canvas;

    // 設定繪圖布局
    private void setDrawLayout() {
        //Log.e(TAG, "設定DrawLayout布局");
        layout = (LinearLayout) findViewById(R.id.chartlayout);
        drawChartView = new DrawChartView(Activity_ChartHistory.this);
        drawChartView.setMinimumHeight(500);
        drawChartView.setMinimumWidth(300);
        layout.addView(drawChartView);
    }

    @Override
    public void onComplete(String response) {
        runOnUiThread(new Runnable() {  //  在主執行緒中執行
            @Override
            public void run() {
                try {
                    final JSONObject responseJson = new JSONObject(response);
                    progressBar_chartupload.setVisibility(View.GONE);                // 恢復觸控功能
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    if (responseJson.getString("status").equals("success")) {      //
                        dblist = responseJson.getJSONArray("dblist");
                        Log.e(TAG, "Https dblist.length = " + dblist.length());
                        writeHx2Table("ampv", dblist);     // 預設 ampv
                    } else {    // response not success
                        String msg =
                                getResources().getString(R.string.dialog_msg_requestfail) + "\n" +
                                        responseJson.getString("result");
                        activityAlert.showAlertDialog(msg, R.string.dialog_OK, activityAlert.doNothing);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "onComplete JSONException" + e.toString());
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }           // public void run()
        });     // runOnUiThread
    }

    @Override
    public void onFail(String err) {
         runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar_chartupload.setVisibility(View.GONE);       // 恢復觸控功能
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                try {
                    final JSONObject failJson = new JSONObject(err);
                    if (failJson.getString("status").equals("exception")) {      // notfound 表示沒有找到對應的帳號
                        String msg =
                                getResources().getString(R.string.dialog_msg_systemerror) + "\n" +
                                        failJson.getString("result");
                        activityAlert.showAlertDialog(msg, R.string.dialog_OK, activityAlert.doNothing);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "onFail JSONException" + e.toString());
                }
            }   // public void run
        });     // runOnUiThread
    }

    private void onCheckboxChange (){
        Log.e(TAG, "checkbox changed");
        JSONObject chartobj = setChartArray();
        drawChartView.setWavedata(chartobj);
        drawChartView.refreshDraw();
    }

    private int timestamp_oneday = 24 * 60 * 60 * 1000;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private int parseDate(String _date){
        // 取得日期在 1970-01-01 之後的第幾天
        ParsePosition pp1 = new ParsePosition(0);
        Date date = sdf.parse(_date, pp1);
        int dateint = (int) (date.getTime() / timestamp_oneday);
        return dateint;
    }


}