package com.ideas.micro.jasonapp102;

import android.content.res.Resources;
import android.graphics.Color;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewAdapter_Myrecords extends BaseAdapter {
    private final String TAG = "Myrecord適配器";
    private JSONArray ElementsData ;   //資料
    private int[] resids;
    private LayoutInflater inflater;    //加載layout
    private int indentionBase;          //item缩排
    private JSONObject textColor = new JSONObject();
    private JSONObject lightGuide;
    private boolean lightEnable = true;        // 預設不使用 recordLight 功能
    private int sbp_red_limith, sbp_red_limitl, dbp_red_limith, dbp_red_limitl, hr_red_limith, hr_red_limitl;
    private int sbp_green_limith, sbp_green_limitl, dbp_green_limith, dbp_green_limitl, hr_green_limith, hr_green_limitl;
    private int oxy_red_limit = 94;
    private int h1_red_limit, h1_green_limit, h4_red_limit, h4_green_limit;
    int ampvindex = 9, phsvindex = 13;
    int fft_colnum = 18;

    //優化Listview 避免重新加載
    //這邊宣告你會動到的Item元件
    static class ViewHolder{
        TextView text_measuretime;
        TextView text_sbp;
        TextView text_dbp;
        TextView text_hr;
        ImageView img_light;
        ImageView img_hasread;
        LinearLayout layout_listview_myrecords_1, layout_listview_myrecords_2;
    }

    //初始化
    public ViewAdapter_Myrecords(JSONArray data, JSONObject lightguide, LayoutInflater inflater){
        Log.e(TAG, "ViewAdapter_Myrecords");
        this.ElementsData = data;
        this.lightGuide = lightguide;
        this.inflater = inflater;
        this.resids = resids;
        indentionBase = 100;
        try {
            textColor.put("r", Color.rgb(255, 0, 0));
            textColor.put("g", Color.rgb(0, 160, 0));
            textColor.put("y", Color.rgb(255, 208, 0));
            //	alert($('#settinglight').val());
             sbp_red_limith = lightGuide.getInt("sbp_red_limith");        // 收縮壓上限
             sbp_red_limitl = lightGuide.getInt("sbp_red_limitl");        // 收縮壓下限
             dbp_red_limith = lightGuide.getInt("dbp_red_limith");        // 舒張壓上限
             dbp_red_limitl = lightGuide.getInt("dbp_red_limitl");        // 舒張壓下限
             hr_red_limith = lightGuide.getInt("hr_red_limith");            // 脈搏上限
             hr_red_limitl = lightGuide.getInt("hr_red_limitl");            // 脈搏下限

             sbp_green_limith = lightGuide.getInt("sbp_green_limith");        // 收縮壓安全值 120 - 129
             sbp_green_limitl = lightGuide.getInt("sbp_green_limitl");
             dbp_green_limith = lightGuide.getInt("dbp_green_limith");        // 舒張壓安全值 80 - 84
             dbp_green_limitl = lightGuide.getInt("dbp_green_limitl");
             hr_green_limith = lightGuide.getInt("hr_green_limith");        //	脈搏安全值 66 - 79
             hr_green_limitl = lightGuide.getInt("hr_green_limitl");

             oxy_red_limit = 94;

             h1_red_limit = lightGuide.getInt("h1_red_limit");		// 超過10 紅燈
             h1_green_limit = lightGuide.getInt("h1_green_limit");		// 低於5綠燈
             h4_red_limit = lightGuide.getInt("h4_red_limit");
             h4_green_limit = lightGuide.getInt("h4_green_limit");
        } catch (JSONException e) {

        }
    }

    //取得數量
    @Override
    public int getCount() {
        return ElementsData.length();
    }
    //取得Item

    @Override
    public JSONObject getItem(int position) {
        try {
            return ElementsData.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    //此範例沒有特別設計ID所以隨便回傳一個值
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.e(TAG, "ViewHolder");
        ViewHolder holder;
        //當ListView被拖拉時會不斷觸發getView，為了避免重複加載必須加上這個判斷
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_myrecords, null);
            holder.text_measuretime = (TextView) convertView.findViewById(R.id.text_measuretime);
            holder.text_sbp = (TextView) convertView.findViewById(R.id.text_sbp);
            holder.text_dbp = (TextView) convertView.findViewById(R.id.text_dbp);
            holder.text_hr = (TextView) convertView.findViewById(R.id.text_hr);
            holder.img_light = (ImageView) convertView.findViewById(R.id.img_light);
            holder.img_hasread = (ImageView) convertView.findViewById(R.id.img_hasread);
            holder.layout_listview_myrecords_1 = (LinearLayout) convertView.findViewById(R.id.layout_listview_myrecords_1);
            holder.layout_listview_myrecords_2 = (LinearLayout) convertView.findViewById(R.id.layout_listview_myrecords_2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject measuredata = getItem(position);
        try {
            holder.text_measuretime.setText(measuredata.getString("rdate")  + " " +  measuredata.getString("rtime"));
            int _sbp = measuredata.getInt("sbp");
            int _dbp = measuredata.getInt("dbp");
            int _hr = measuredata.getInt("hr");
            int _oxygen = measuredata.getInt("oxygen");
            float _ampvh1 = (float) measuredata.optDouble("ampvh1", 0);
            float _phsvh1 = (float) measuredata.optDouble("phsvh1", 0);
            float _ampvh4 = (float) measuredata.optDouble("ampvh4", 0);
            float _phsvh4 = (float) measuredata.optDouble("phsvh4", 0);
            String _fft = measuredata.optString("fft", "");           // 必須在 MeridianMain_Enc.GetBPMWaveRecordListByMem_APP 加入 R.recordFFT AS fft,
            String _light = "light_w";
            Log.e(TAG, "BP " + _sbp + ", " + _dbp);
            if (lightEnable) {      // 要顯示紅黃綠燈
                holder.text_sbp.setText(String.valueOf(_sbp));
                holder.text_dbp.setText(String.valueOf(_dbp));
                holder.text_hr.setText(String.valueOf(_hr));        // 沒有作用 setBackgroundColor
                holder.text_sbp.setTextColor(textColor.getInt(getSbpLight(_sbp)));
                holder.text_dbp.setTextColor(textColor.getInt(getDbpLight(_dbp)));
                holder.text_hr.setTextColor(textColor.getInt(getHrLight(_hr)));
                if (GlobalVariables.isHxLightEnable) {
                    _light = getLightByH1H4BP(_oxygen, 1, _sbp, _dbp, _hr, _fft, _ampvh1, _phsvh1, _ampvh4, _phsvh4);
                } else {
                    _light = getLightByBP(_oxygen, 1, _sbp, _dbp, _hr);
                }
                switch (_light) {
                    case "light_w":
                        holder.img_light.setImageResource(R.drawable.light_w);
                        break;
                    case "light_r":
                        holder.img_light.setImageResource(R.drawable.light_r);
                        break;
                    case "light_y":
                        holder.img_light.setImageResource(R.drawable.light_y);
                        break;
                    case "light_g":
                        holder.img_light.setImageResource(R.drawable.light_g);
                        break;
                }
                // 顯示已讀未讀
                if (measuredata.getInt("hasread") == 0) {
                    holder.img_hasread.setImageResource(R.drawable.document0);
                } else {
                    holder.img_hasread.setImageResource(R.drawable.document1);
                }
                Log.e(TAG, getLightByBP(_oxygen, 1, _sbp, _dbp, _hr));
            } else {            // 不要顯示紅黃綠燈 改用是否已讀
                holder.text_sbp.setText(String.valueOf(_sbp));
                holder.text_dbp.setText(String.valueOf(_dbp));
                holder.text_hr.setText(String.valueOf(_hr));        // 沒有作用 setBackgroundColor
                holder.text_sbp.setTextColor(textColor.getInt(getSbpLight(_sbp)));
                holder.text_dbp.setTextColor(textColor.getInt(getDbpLight(_dbp)));
                holder.text_hr.setTextColor(textColor.getInt(getHrLight(_hr)));
                if (measuredata.getInt("hasread") == 0) {
                    holder.img_hasread.setImageResource(R.drawable.document0);
                } else {
                    holder.img_hasread.setImageResource(R.drawable.document1);
                }
                holder.img_light.setVisibility(View.GONE);          // 隱藏燈號
                Log.e(TAG, "provedby =" +  measuredata.getString("provedby") + "=" );
                // 顯示等待或是已確認 醫師處置的圖
//                holder.img_light.setImageResource(measuredata.getInt("provedby")!=0?R.drawable.provedby:R.drawable.waiting);
            }

            if (position%2 == 0) {
                Log.e(TAG, "position = " + position + " 底灰");
                holder.layout_listview_myrecords_1.setBackgroundColor(0xDDDDDD);
                holder.layout_listview_myrecords_2.setBackgroundColor(0xDDDDDD);
            } else {
                Log.e(TAG, "position = " + position + " 底白");
                holder.layout_listview_myrecords_1.setBackgroundColor(0xFFFFFF);
                holder.layout_listview_myrecords_2.setBackgroundColor(0xFFFFFF);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return convertView;
    }

    //get AlertLight (根據FFT是否完成決定警示燈號，僅限P1)
    private String getLLightpByFlag(int fftflag) {
        //	只考慮資料是否處理完畢
        if (fftflag!=1) {
            return "light_w";
        } else {
            return "light_g";
        }
    }

    //get ToolTip (根據FFFT旗標 == 1決定提示內容，僅限P1)
    private String getToolTipByFlag(int fftflag) {
        if (fftflag!=1) {
            return "資料處理尚未完成";
        } else {
            return "資料處理完畢";
        }
    }

    //get AlertLight (只根據脈診決定警示燈號)
    private String getLightByH1H4( int fftflag, float ampvh1, float phsvh1, float ampvh4, float phsvh4) {
        //	alert("utility_meridian.umed_getAlertLight ");
        if (fftflag!=1) {
            return "light_w";
        } else {
            boolean isoverlimit = isH1Red(ampvh1, phsvh1) || isH4Red(ampvh4, phsvh4);
            boolean isunderlimit = isH1Green(ampvh1, phsvh1) && isH4Green(ampvh4, phsvh4);
            return isoverlimit?"light_r":(isunderlimit?"light_g":"light_y");
        }
    }


    //get AlertLight (根據血氧及及血壓決定警示燈號)
    private String getLightByBP(int oxygen, int fftflag, int sbp, int dbp, int hr) {
        //	alert("utility_meridian.umed_getAlertLight ");
        boolean isoverlimit = false;
        boolean isunderlimit = false;
        isoverlimit = isSbpRed(sbp) || isDbpRed(dbp) || isHrRed(hr);
        isunderlimit = isSbpGreen(sbp) && isDbpGreen(dbp) && isHrGreen(hr);
        if (fftflag!=1) {
            return "light_w";
        } else {
            if (oxygen == 0 || oxygen > 94) {      // 血氧正常 或沒有血氧資料
                return isoverlimit?"light_r":(isunderlimit?"light_g":"light_y");
            } else if (oxygen > 0 && oxygen <= 94){
                return "light_r";
            } else {	// no oxygen data
                return isoverlimit?"light_r":(isunderlimit?"light_g":"light_y");
            }
        }
    }

    //get ToolTip (根據血氧及血壓決定提示內容)
    private String getToolTipByBP(int oxygen, int fftflag, int sbp, int dbp, int hr) {
        //	alert("utility_meridian.umed_getAlertLight ");	
        String tooltip = "";
        if (fftflag!=1) {
            tooltip = "資料處理尚未完成";
        } else {
            tooltip = isOxyRed(oxygen)?"血氧值不足<br>":"";
            tooltip += getSbpTooltip(sbp);
            tooltip += getDbpTooltip(dbp);
            tooltip += getHrTooltip(hr);
        }
        return tooltip;
    }

    // get AlertLight (根據血氧及脈診決定警示燈號)
    private String getLightByH1H4BP(int oxygen, int fftflag, int sbp, int dbp, int hr, String best_fftstr, float ampvh1, float phsvh1, float ampvh4, float phsvh4) {
        boolean isoverlimit = false;
        boolean isunderlimit = false;
        if (best_fftstr.equals("NA")) {
            return "light_w";
        } else {
            isoverlimit = isH1Red(ampvh1, phsvh1) || isH4Red(ampvh4, phsvh4) || isSbpRed(sbp) || isDbpRed(dbp) || isHrRed(hr);
            isunderlimit = isH1Green(ampvh1, phsvh1) && isH4Green(ampvh4, phsvh4) && isSbpGreen(sbp) && isDbpGreen(dbp) && isHrGreen(hr);
            if (oxygen > 94) {
                return isoverlimit?"light_r":(isunderlimit?"light_g":"light_y");
            } else if (oxygen > 0 && oxygen <= 94){
                return "light_r";
            } else {	// no oxygen data
                return isoverlimit?"light_r":(isunderlimit?"light_g":"light_y");
            }
        }
    }

    //get ToolTip (根據血氧及脈診決定提示內容)
    private String getToolTipByH1H4BP(int oxygen, int fftflag, int sbp, int dbp, int hr,
                                      float ampvh1, float phsvh1, float ampvh4, float phsvh4) {
        String tooltip = "";
        if (fftflag!=1) {
            tooltip = "資料處理尚未完成";
        } else {
            tooltip += getH1Tooltip(ampvh1, phsvh1);
            tooltip += getH4Tooltip(ampvh4, phsvh4);
            tooltip += isOxyRed(oxygen)?"血氧值不足<br>":"";
            tooltip += getSbpTooltip(sbp);
            tooltip += getDbpTooltip(dbp);
            tooltip += getHrTooltip(hr);
        }
        return tooltip;
    }


    private String getSbpTooltip(int sbp){
        String tooltip = "";
        if (sbp >= sbp_red_limith) {
            tooltip = "收縮壓" + sbp + "過高(" + sbp_red_limith + ")<br>";
        } else if (sbp <= sbp_red_limitl) {
            tooltip = "收縮壓" + sbp + "過低(" + sbp_red_limitl + ")<br>";
        } else if (sbp > sbp_green_limith) {
            tooltip = "收縮壓" + sbp + "偏高(" + sbp_green_limith + ")<br>";
        } else if (sbp < sbp_green_limitl){
            tooltip = "收縮壓" + sbp + "偏低(" + sbp_green_limitl + ")<br>";
        }
        return tooltip;
    }

    private String getDbpTooltip(int dbp){
        String tooltip = "";
        if (dbp >= dbp_red_limith) {
            tooltip = "舒張壓" + dbp + "過高(" + dbp_red_limith + ")<br>";
        } else if (dbp <= dbp_red_limitl) {
            tooltip = "舒張壓" + dbp + "過低(" + dbp_red_limitl + ")<br>";
        } else if (dbp > dbp_green_limith) {
            tooltip = "舒張壓" + dbp + "偏高(" + dbp_green_limith + ")<br>";
        } else if (dbp < dbp_green_limitl){
            tooltip = "舒張壓" + dbp + "偏低(" + dbp_green_limitl + ")<br>";
        }
        return tooltip;
    }

    private String getHrTooltip(int hr){
        String tooltip = "";
        if (hr >= hr_red_limith) {
            tooltip = "脈搏" + hr + "過高(" + hr_red_limith + ")<br>";
        } else if (hr <= hr_red_limitl) {
            tooltip = "脈搏" + hr + "過低(" + hr_red_limitl + ")<br>";
        } else if (hr > hr_green_limith) {
            tooltip = "脈搏" + hr + "偏高(" + hr_green_limith + ")<br>";
        } else if (hr < hr_green_limitl){
            tooltip = "脈搏" + hr + "偏低(" + hr_green_limitl + ")<br>";
        }
        return tooltip;
    }

    private boolean isSbpRed(int sbp){
        boolean isred = false;
        isred = (sbp >= sbp_red_limith) || (sbp <= sbp_red_limitl);
        return isred;
    }

    private boolean isSbpGreen(int sbp){
        boolean isgreen = false;
        isgreen = (sbp >= sbp_green_limitl) && (sbp <= sbp_green_limith);
        return isgreen;
    }

    private boolean isDbpRed(int dbp){
        boolean isred = false;
        isred = (dbp >= dbp_red_limith) || (dbp <= dbp_red_limitl);
        return isred;
    }

    private boolean  isDbpGreen(int dbp){
        boolean isgreen = false;
        isgreen = (dbp >= dbp_green_limitl) && (dbp <= dbp_green_limith);
        return isgreen;
    }

    private boolean  isHrRed(int hr){
        boolean isred = false;
        isred = (hr >= hr_red_limith) || (hr <= hr_red_limitl);
        return isred;
    }

    private boolean  isHrGreen(int hr){
        boolean isgreen = false;
        isgreen = (hr >= hr_green_limitl) && (hr <= hr_green_limith);
        return isgreen;
    }

    private boolean  isOxyRed(int oxygen){
        boolean isred = false;
        isred = (oxygen > 0) && (oxygen <= oxy_red_limit);
        return isred;
    }

    private boolean isOxyGreen(int oxygen){
        boolean isgreen = false;
        isgreen = (oxygen > oxy_red_limit);
        return isgreen;
    }

    private boolean isH1Red(float ampv, float phsv){
        boolean isred = false;
        isred = (ampv >= h1_red_limit) || (phsv >= h1_red_limit);
        return isred;
    }

    private boolean isH1Green(float ampv, float phsv){
        boolean isgreen = false;
        isgreen = (ampv < h1_green_limit) && (phsv < h1_green_limit);
        return isgreen;
    }

    private boolean isH4Red(float ampv, float phsv){
        boolean isred = false;
        isred = (ampv >= h4_red_limit) || (phsv >= h4_red_limit);
        return isred;
    }

    private boolean isH4Green(float ampv, float phsv){
        boolean isgreen = false;
        isgreen = (ampv < h4_green_limit) && (phsv < h4_green_limit);
        return isgreen;
    }

    private String floatformat = "%.1f";
    private String getH1Tooltip(float ampv, float phsv){
        //	var tooltip = "H1 能量變異" + ampv + "H1 相位變異" + phsv + "<br>";
        String tooltip = "";
        if (ampv >= h1_red_limit) {
            tooltip = "H1 能量變異" + String.format(floatformat, ampv)  + "過高(" + h1_red_limit + ")<br>";
        } else if (ampv >= h1_green_limit) {
            tooltip = "H1 能量變異" +  String.format(floatformat, ampv) + "偏高(" + h1_green_limit + ")<br>";
        }
        if (phsv >= h1_red_limit) {
            tooltip += "H1 相位變異" +  String.format(floatformat, phsv) + "過高(" + h1_red_limit + ")<br>";
        } else if (phsv >= h1_green_limit) {
            tooltip += "H1 相位變異" +  String.format(floatformat, phsv) + "偏高(" + h1_green_limit + ")<br>";
        }
        return tooltip;
    }

    private String  getH4Tooltip(float ampv, float phsv){
        //	var tooltip = "H4 能量變異" + ampv + "H4 相位變異" + phsv + "<br>";
        String tooltip = "";
        if (ampv >= h4_red_limit) {
            tooltip = "H4 能量變異" +  String.format(floatformat, ampv) + "過高(" + h4_red_limit + ")<br>";
        } else if (ampv >= h4_green_limit) {
            tooltip = "H4 能量變異" +  String.format(floatformat, ampv) + "偏高(" + h4_green_limit + ")<br>";
        }

        if (phsv >= h4_red_limit) {
            tooltip += "H4 相位變異" +  String.format(floatformat, phsv) + "過高(" + h4_red_limit + ")<br>";
        } else if (phsv >= h4_green_limit) {
            tooltip += "H4 相位變異" +  String.format(floatformat, phsv) + "偏高(" + h4_green_limit + ")<br>";
        }
        return tooltip;
    }


    //is BP OverLimit
    private String getSbpLight(int sbp){
        String light = "";
        light = isSbpRed(sbp)?"r":(isSbpGreen(sbp)?"g":"y");
        return light;
    }

    private String getDbpLight(int dbp){
        String light = "";
        light = isDbpRed(dbp)?"r":(isDbpGreen(dbp)?"g":"y");
        return light;
    }

    private String getHrLight(int hr){
        String light = "";
        light = isHrRed(hr)?"r":(isHrGreen(hr)?"g":"y");
        return light;
    }

    private float getHxValue(String fft, int startindex){
        // fft 必須是最佳切波段的 FFT 陣列字串
        int index_s = Utility_FFT.findIndexOfNthChar(fft, ",", startindex -1);
        int index_e = fft.indexOf(",", index_s + 1);
        String str_val = fft.substring(index_s + 1, index_e - index_s -1);
        return Float.parseFloat(str_val);
    }


}
