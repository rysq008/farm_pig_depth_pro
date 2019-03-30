package com.xiangchuang.risks.utils;

import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.PreferencesUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @Author: Lucas.Cui
 * 时   间：2019/3/4
 * 简   述：<功能简述>
 */
public class AVOSCloudUtils {

    public static void saveErrorMessage(Exception e, String className) {
//         测试 SDK 是否正常工作的代码
        AVObject avObject = new AVObject("Android_phone");
//        avObject.put("Cookie", SharedPreUtil.getSessionId());
        String type = PreferencesUtils.getStringValue(Constants.companyfleg, AppConfig.getAppContext());
        if (("1").equals(type)) {
            avObject.put("userId", PreferencesUtils.getStringValue(Constants.id, AppConfig.getAppContext()));
        } else {
            avObject.put("userId", PreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext()) + "");
        }

        avObject.put("brand", SystemUtil.getDeviceBrand());
        avObject.put("model", SystemUtil.getSystemModel());
        avObject.put("systemversion", SystemUtil.getSystemVersion());
        avObject.put("sdkVersion", SystemUtil.getSDKVersion());
        avObject.put("versionName", SystemUtil.getLocalVersionName());
        avObject.put("errorPosition", getPosition(className));
//        avObject.put("registrationid", JPushInterface.getRegistrationID(App.getInstance()));
        avObject.put("plam", System.getProperty("os.name"));

        StringBuilder sb = new StringBuilder();

        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
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

    /**
     * 定位错误位置
     *
     * @param tag
     * @return
     */
    private static String getPosition(String tag) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement element = getTargetStack(tag);

        if (null == element) {
            return "";
        }

        sb.append("(")
                .append(element.getFileName())
                .append(":")
                .append(element.getLineNumber())
                .append(")");
        return sb.toString();
    }

    /**
     * 获取最后调用我们log的StackTraceElement
     *
     * @param tag 目标类的SimpleName
     * @return
     */

    private static StackTraceElement getTargetStack(String tag) {

        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().contains(tag)) {
                //返回调用位置的 element
                return element;
            }
        }
        return null;
    }
}
