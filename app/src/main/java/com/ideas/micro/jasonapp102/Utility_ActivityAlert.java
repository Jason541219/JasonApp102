package com.ideas.micro.jasonapp102;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatActivity;

public class Utility_ActivityAlert extends AppCompatActivity {
    private Context context;
    private AppCompatActivity activity;

    public Utility_ActivityAlert (AppCompatActivity activity){
        this.activity = activity;
    }

    // 一個選擇 (說明為系統ID)
    public void showAlertDialog(int msg, int postiveButton, DialogInterface.OnClickListener postiveListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.dialog_systemcomment)
                .setMessage(msg)
                .setPositiveButton(postiveButton, postiveListener);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);        // 點擊對話框以外區域 不會取消對話框
        dialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;
        dialog.show();
    }

    // 一個選擇 (說明為字串)
    public void showAlertDialog(String msg, int postiveButton, DialogInterface.OnClickListener postiveListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.dialog_systemcomment)
                .setMessage(msg)
                .setPositiveButton(postiveButton, postiveListener);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);        // 點擊對話框以外區域 不會取消對話框
        dialog.show();
    }

    // 兩個選擇
    public void showAlertDialog(int msg,
                                       int postiveButton, DialogInterface.OnClickListener postiveListener,
                                       int negativeButton, DialogInterface.OnClickListener negativeListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.dialog_systemcomment)
                .setMessage(msg)
                .setPositiveButton(postiveButton, postiveListener)
                .setNegativeButton(negativeButton, negativeListener);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);        // 點擊對話框以外區域 不會取消對話框
        dialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;
        dialog.show();
    }

    // 兩個選擇 (說明為字串)
    public void showAlertDialog(String msg,
                                       int postiveButton, DialogInterface.OnClickListener postiveListener,
                                       int negativeButton, DialogInterface.OnClickListener negativeListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.dialog_systemcomment)
                .setMessage(msg)
                .setPositiveButton(postiveButton, postiveListener)
                .setNegativeButton(negativeButton, negativeListener);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);        // 點擊對話框以外區域 不會取消對話框
        dialog.show();
    }

    // 甚麼事也不用做
    public DialogInterface.OnClickListener doNothing = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
        }
    };

    // 警告對話框的關閉Activity監聽器
    public DialogInterface.OnClickListener finishActivity = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            activity.finish();   // finish this activity
        }
    };

}
