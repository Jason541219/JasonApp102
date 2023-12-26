package com.ideas.micro.jasonapp102;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Utility_Alert {


    // 一個選擇 (說明為系統ID)
    public static void showAlertDialog(Context context, int msg, int postiveButton, DialogInterface.OnClickListener postiveListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_systemcomment)
                .setMessage(msg)
                .setPositiveButton(postiveButton, postiveListener);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);        // 點擊對話框以外區域 不會取消對話框
        dialog.show();
    }

    // 一個選擇 (說明為字串)
    public static void showAlertDialog(Context context, String msg, int postiveButton, DialogInterface.OnClickListener postiveListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_systemcomment)
                .setMessage(msg)
                .setPositiveButton(postiveButton, postiveListener);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);        // 點擊對話框以外區域 不會取消對話框
        dialog.show();
    }

    // 兩個選擇
    public static void showAlertDialog(Context context, int msg,
                                       int postiveButton, DialogInterface.OnClickListener postiveListener,
                                       int negativeButton, DialogInterface.OnClickListener negativeListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_systemcomment)
                .setMessage(msg)
                .setPositiveButton(postiveButton, postiveListener)
                .setNegativeButton(negativeButton, negativeListener);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);        // 點擊對話框以外區域 不會取消對話框
        dialog.show();
    }

    // 兩個選擇
    public static void showAlertDialog(Context context, String msg,
                                       int postiveButton, DialogInterface.OnClickListener postiveListener,
                                       int negativeButton, DialogInterface.OnClickListener negativeListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_systemcomment)
                .setMessage(msg)
                .setPositiveButton(postiveButton, postiveListener)
                .setNegativeButton(negativeButton, negativeListener);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);        // 點擊對話框以外區域 不會取消對話框
        dialog.show();
    }

    // 甚麼是也不用做
    public static DialogInterface.OnClickListener doNothing = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {

        }
    };
}
