package com.xiangchuang.risks.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.MyApplication;

import java.util.Locale;

/**
 * 系统工具类
 * Created by zhuwentao on 2016-07-18.
 */
public class SystemUtil {

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return 语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机SDK版本
     *
     * @return 设备SDK版本
     */
    public static String getSDKVersion() {
        return android.os.Build.VERSION.SDK; // 设备SDK版本
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return 手机IMEI
     */
    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        if ((tm != null) && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return tm.getDeviceId();
        }
        return "";
    }

    /**
     * 获取本地软件版本号
     */
    public static int getLocalVersion() {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = AppConfig.getAppContext().getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(AppConfig.getAppContext().getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localVersion;
    }



    /**
     * 获取本地软件版本号名称
     */
    public static String getLocalVersionName() {
        String localVersion = "";
        try {
            PackageInfo packageInfo = AppConfig.getAppContext().getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(AppConfig.getAppContext().getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localVersion;
    }


    /**
     * 获取屏幕密度
     * @return
     */
    public static float getDensity() {
        return Resources.getSystem().getDisplayMetrics().density;
    }


}