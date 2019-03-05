package com.xiangchuang.risks.utils;

import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @Author: Lucas.Cui
 * 时   间：2019/3/4
 * 简   述：<功能简述>
 */
public class AVOSCloudUtils {

    public static void saveErrorMessage(Exception e) {
//         测试 SDK 是否正常工作的代码
        AVObject avObject = new AVObject("Android_phone");
//        avObject.put("Cookie", SharedPreUtil.getSessionId());
        avObject.put("brand", SystemUtil.getDeviceBrand());
        avObject.put("model", SystemUtil.getSystemModel());
        avObject.put("systemversion", SystemUtil.getSystemVersion());
        avObject.put("sdkVersion", SystemUtil.getSDKVersion());
        avObject.put("versionName", SystemUtil.getLocalVersionName());
//        avObject.put("registrationid", JPushInterface.getRegistrationID(App.getInstance()));
        avObject.put("plam", System.getProperty("os.name"));

        StringBuilder sb = new StringBuilder();

        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        Throwable cause = e.getCause();
        // 循环取出Cause
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = e.getCause();
        }
        pw.close();
        String result = writer.toString();
        sb.append(result);
        avObject.put("error", e.toString() + "--＞" + sb.toString());

        avObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Log.d("saved", "success!");
                }
            }
        });

    }
}
