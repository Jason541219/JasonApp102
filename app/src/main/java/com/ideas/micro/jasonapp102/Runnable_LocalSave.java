package com.ideas.micro.jasonapp102;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.ideas.micro.jasonapp102.database.AppDatabase;
import com.ideas.micro.jasonapp102.database.MDJsonRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Runnable_LocalSave implements Runnable {
    private AppCompatActivity activity;
    private Context context = null;
    private  JSONObject data = new JSONObject();

    public Runnable_LocalSave(AppCompatActivity activity, JSONObject data){
        this.activity = activity;
        this.data = data;
    }

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        try {
            data.put("recordID", calendar.getTimeInMillis());
            MDJsonRecord mDrecord = new MDJsonRecord(data.toString());
            AppDatabase.getInstance(activity).getMDjsonrecordDao().insertOneRecord(mDrecord);
            // 顯示對話框
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Utility_Alert.showAlertDialog(activity,
//                            R.string.dialog_msg_bpmsavesuccess,
//                            R.string.dialog_nextmember, goNextMember,
//                            R.string.dialog_switchhand, finishActivity);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
