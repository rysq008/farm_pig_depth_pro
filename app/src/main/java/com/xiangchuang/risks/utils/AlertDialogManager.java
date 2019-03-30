package com.xiangchuang.risks.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import com.innovation.pig.insurance.R;


/**
 * @author wbs on 11/30/17.
 */

public class AlertDialogManager {
    private static boolean isShow = false;
    private static AlertDialog alertDialog;

    public static void showMessageDialog(Context context, String title, String message, final DialogInterface dialogInterface) {
        showMessageDialog(context, title, message, true, dialogInterface);
    }
    public static void showMessageDialogOne(Context context, String title, String message, final DialogInterface dialogInterface) {
        showMessageDialogone(context, title, message, true, dialogInterface);
    }
    public static void showMessageDialogRetry(Context context, String title, String message, final DialogInterface dialogInterface) {
        showMessageDialogretry(context, title, message, true, dialogInterface);
    }
    public static void showMessageDialog(Context context, String title, String message, boolean isCancelShow, final DialogInterface dialogInterface) {
        showMessageDialog(context, title, message, isCancelShow, null, dialogInterface);
    }
    public static void showMessageDialogone(Context context, String title, String message, boolean isCancelShow, final DialogInterface dialogInterface) {
        showMessageDialogone(context, title, message, isCancelShow, null, dialogInterface);
    }
    public static void showMessageDialog(Context context, String title, String message, boolean isCancelShow, String sureTxt, final DialogInterface dialogInterface) {
        if (((Activity) context).isFinishing()) {
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setIcon(R.drawable.ic_launcher)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialogInterface.onPositive();
                    }
                })
                .setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialogInterface.onNegative();
                    }
                });
        dialog.setCancelable(false);
        dialog.show();

    }
    public static void showMessageDialogone(Context context, String title, String message, boolean isCancelShow, String sureTxt, final DialogInterface dialogInterface) {
        if (((Activity) context).isFinishing()) {
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setIcon(R.drawable.ic_launcher)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialogInterface.onPositive();
                    }
                });
        dialog.setCancelable(false);
        dialog.show();

    }

    public static void showMessageDialogretry(Context context, String title, String message, boolean isCancelShow, final DialogInterface dialogInterface) {
        if (((Activity) context).isFinishing()) {
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setIcon(R.drawable.ic_launcher)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("重试", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialogInterface.onPositive();
                    }
                })
                .setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialogInterface.onNegative();
                    }
                });;
        dialog.setCancelable(false);
        dialog.show();

    }

    public interface DialogInterface {
        void onPositive();
        void onNegative();
    }
}
