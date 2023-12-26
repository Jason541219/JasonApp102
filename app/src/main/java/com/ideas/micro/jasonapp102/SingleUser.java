package com.ideas.micro.jasonapp102;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SingleUser {
    // 一開始就建立物件，這樣只要一直回傳這個物件就是簡單的singleton
    private static final String TAG = "SingleUser";
    private static SingleUser instance;

    private JSONObject userjson;
    private int userID;
    private int servicecenterid;
    private String useraccount;
    private String username;
    private String usergender;
    private String usermobile;
    private int userattending;
    private String userpid;
    private String useremail;
    private String userbirthday;
    private String userfcmtoken;
    private int ispayuser;
    private String responsestatus;
    private String userqrcode;
    private String useravatar;
    private String userprivacy;
    private String usermhbagreement;
    private String userhasread;
    private String bpmsn;
    private String bpmmac;
    private String bindmac;
    private String bpmtype;
    private String usergroupname;
    private int isgroupprimary;
    private int groupCapacity;


    // private constructor，這樣其他物件就沒辦法直接用new來取得新的實體
    // 因為constructor已經private，所以需要另外提供方法讓其他程式調用這個類別
    private SingleUser(){
        userjson = new JSONObject();
    }

    // Guest 賓客基本資料
    public String getGuestJsonString(){
        JSONObject guestjson = new JSONObject();
        try {
            guestjson.put("userID", 0);
            guestjson.put("servicecenterid", 0);
            guestjson.put("useraccount", "guest");
            guestjson.put("username", "賓客");
            guestjson.put("usergender", "M");
            guestjson.put("usermobile", "0912345678");
            guestjson.put("userpid", "F111222333");
            guestjson.put("useremail", "guest@gmail.com");
            guestjson.put("userbirthday", "2000-01-01");
            guestjson.put("userfcmtoken", "");
            guestjson.put("ispayuser", 0);
            guestjson.put("responsestatus", "");
            guestjson.put("userqrcode", "");
            guestjson.put("useravatar", "");
            guestjson.put("userprivacy", "");
            guestjson.put("usermhbagreement", "");
            guestjson.put("userhasreadreport", "");
            guestjson.put("bpmsn", "");
            guestjson.put("bpmmac", "");
            guestjson.put("bindmac","");
            guestjson.put("bpmtype", "");
        } catch (JSONException e){

        }
        return guestjson.toString();
    }

    // 第一次被呼叫的時候再建立物件
    // 多執行緒時，當物件需要被建立時才使用synchronized保證Singleton一定是單一的 ，增加程式校能
    public static SingleUser getInstance(){
        if(instance == null){
            synchronized(SingleUser.class){
                if(instance == null){
                    instance = new SingleUser();
                }
            }
        }
        return instance;
    }

    public void setUserjson(String userjsonstr){
        try {
            Log.e(TAG, userjsonstr);
            userjson = new JSONObject(userjsonstr);
        } catch (JSONException e) {
            Log.e(TAG, "UserJson Transfer Error : " + e);
        }
    }

    public void setBindmac(String _mac){
        try {
            userjson.put("bindmac", _mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setUserqrcode(String _userqrcode){
//        Log.e("QRCode", "SingleUser.setUserqrcode " + _userqrcode);
        try {
            userjson.put("userqrcode", _userqrcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //this.userqrcode = _userqrcode;
    }

    public void setUserprivacy(String _userprivacy){
        try {
            this.userjson.put("userprivacy", _userprivacy);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setUserhasread(String hasRead) {
        try {
            this.userjson.put("userhasread", hasRead);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 唯讀
    public int getUserID() {
        return userjson.optInt("userID", 0);
    }
    public int getServicecenterid() {
        return userjson.optInt("servicecenterid", 0);
    }
    public String getUseraccount() {
        return getUserCharacter("useraccount");
    }
    public String getUsername() {
        return getUserCharacter("username");
    }
    public String getUsergender() {
        return getUserCharacter("usergender");
    }
    public String getUsermobile() {
        return getUserCharacter("usermobile");
    }
    public String getUserpid() {
        return getUserCharacter("userpid");
    }
    public String getUseremail() {
        return getUserCharacter("useremail");
    }
    public String getUserbirthday() {
        return getUserCharacter("userbirthday");
    }
    public String getUserfcmtoken(){
        return getUserCharacter("userfcmtoken");
    }
    public int getIspayuser(){
        return userjson.optInt("ispayuser", 0);
    }
    public String getResponsestatus(){
        return getUserCharacter("status");
    }
    public String getUserqrcode() {return getUserCharacter("userqrcode");}
    public String getUseravatar() {return getUserCharacter("useravatar");}
    public String getUserprivacy() {return getUserCharacter("userprivacy");}
    public String getUsermhbagreement() {return getUserCharacter("usermhbagreement");}
    public int getUserattending(){return userjson.optInt("userattending", 0);}
    public String getUserhasread() {return getUserCharacter("userhasread");}
    public int getIsgroupprimary() {return userjson.optInt("isgroupprimary", 0);}
    public int getGroupcapacity() {return userjson.optInt("groupcapacity", 0);}
    public String getBpmSN(){return getUserCharacter("bpmsn");}
    public String getBpmMAC(){return getUserCharacter("bpmmac");}
    public String getBindMAC(){return getUserCharacter("bindmac");}
    public String getGroupname(){return getUserCharacter("groupname");}
    public String getBpmtype(){return getUserCharacter("bpmtype");}
    public String getMHBAgreeSigned(){return getUserCharacter("mhbagreesigned");}
    public String getMHBNotedDate(){return getUserCharacter("mhbnoteddate");}
    public String getMHBReportDate(){return getUserCharacter("mhbreportdate");}
    public String getReportSubscriptS(){return getUserCharacter("subscripts");}
    public String getReportSubscriptE(){return getUserCharacter("subscripte");}
    public String getSettingParam(){return getUserCharacter("settingparam");}
    public String getBpmRentS(){return getUserCharacter("bpmrents");}
    public String getBpmRentE(){return getUserCharacter("bpmrente");}

    public String getUserCharacter(String character) {
        String charvalue = "";
        try {
            charvalue = userjson.getString(character);
        } catch (JSONException e) {
            Log.e(TAG, "There is not " + character + " in UserJson");
            e.printStackTrace();
        }
        return charvalue;
    }
}
