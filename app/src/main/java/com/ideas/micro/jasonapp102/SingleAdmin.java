package com.ideas.micro.jasonapp102;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SingleAdmin {
    // 一開始就建立物件，這樣只要一直回傳這個物件就是簡單的singleton
    private static final String TAG = "SingleUser";
    private static SingleAdmin instance;

    private JSONObject adminjson;
    private int adminID;
//    private int servicecenterid;
    private String adminaccount;
    private String adminname;
    private String admingender;
    private String adminmobile;
    private String adminpid;
    private String adminfcmtoken;
    private int adminlevel;
    private String responsestatus;
    // private constructor，這樣其他物件就沒辦法直接用new來取得新的實體
    // 因為constructor已經private，所以需要另外提供方法讓其他程式調用這個類別
    private SingleAdmin(){
        adminjson = new JSONObject();
        adminID = 0 ;
//        servicecenterid = 0 ;
        adminaccount = "";
        adminname = "";
        admingender = "";
        adminmobile = "";
        adminpid = "";
        adminfcmtoken = "";
        adminlevel = 999 ;  // 預設不是醫護人員
        responsestatus = "";
    }

    // 第一次被呼叫的時候再建立物件
    // 多執行緒時，當物件需要被建立時才使用synchronized保證Singleton一定是單一的 ，增加程式校能
    public static SingleAdmin getInstance(){
        if(instance == null){
            synchronized(SingleAdmin.class){
                if(instance == null){
                    instance = new SingleAdmin();
                }
            }
        }
        return instance;
    }

    public void setAdminjson(String adminjsonstr){
        try {
            adminjson = new JSONObject(adminjsonstr);
        } catch (JSONException e) {
            Log.e(TAG, "UserJson Transfer Error : " + e);
        }
    }

    // 唯讀
    public int getAdminID() {
        return adminjson.optInt("adminid", 0);
    }
//    public int getServicecenterid() {
//        return adminjson.optInt("servicecenterid", 0);
//    }
    public String getAdminaccount() {
        return adminjson.optString("adminaccount", "");
    }
    public String getAdminname() {
        return adminjson.optString("adminname", "");
    }
    public String getAdmingender() {
        return adminjson.optString("admingender", "");
    }
    public String getAdminmobile() {
        return adminjson.optString("adminmobile", "");
    }
    public String getAdminpid() {
        return adminjson.optString("adminpid", "");
    }
    public String getAdminfcmtoken(){
        return adminjson.optString("adminfcmtoken", "");
    }
    public int getAdminlevel(){
        return adminjson.optInt("adminlevel", 0);
    }
    public String getResponsestatus(){
        return adminjson.optString("status", "error");
    }

}

