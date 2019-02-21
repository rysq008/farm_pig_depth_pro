package com.xiangchuangtec.luolu.animalcounter;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;
import com.xiangchuang.risks.utils.ShareUtils;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.lang.ref.WeakReference;

import innovation.crash.CrashHandler;
import innovation.location.LocationManager_new;
import innovation.network_status.NetworkChangedReceiver;
import innovation.utils.HttpUtils;
import innovation.utils.ImageLoaderUtils;
import okhttp3.OkHttpClient;


/**
 * Created by luolu on 2018/3/6.
 */

public class MyApplication extends Application {

    private CrashHandler mCrashHandler;
    private static Context context;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final boolean isOpenLiPei = true;

    public LocationThread locationThread;

    public static boolean isNetConnected = false;
    NetworkChangedReceiver networkChangedReceiver;

    //能繁母猪计数器；
    public static int sowCount = 0;
    //记录每次抓图最小的Xmin值
    public static float lastXmin = 0f;


    public static int currentPadSize;

    public static boolean isNoCamera = false;

    /* 计时器录制时长 */
    public static long during = 0;

    /* 计时器录制开始时间 */
    public static long timeVideoStart;

    /* 存储当前保存图片的时间戳 */
    public static long lastCurrentTime = 0;

    //记录失败次数
    public static int debugNub = 0;

    private static WeakReference<Activity> acontext;



    @Override
    public void onCreate() {
        super.onCreate();
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(getApplicationContext());
        ShareUtils.init(this);
        HttpUtils.baseUrl = ShareUtils.getHost("host");
        HttpUtils.resetIp(HttpUtils.baseUrl);
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        OkHttpClient client = new OkHttpClient();
        // create your own OkHttp client
        UploadService.HTTP_STACK = new OkHttpStack(client);
        // make the library use your own OkHttp client
        MyApplication.context = getApplicationContext();
        //初始化 ImageLoader
        ImageLoaderUtils.initImageLoader(this);
        //初始化 bugly
        CrashReport.initCrashReport(getApplicationContext(), "2d3ff546dd", false);
        networkChangedReceiver = new NetworkChangedReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangedReceiver, intentFilter);

        locationThread = new LocationThread();
        locationThread.start();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                if(null == acontext)
//                {
//                    HttpUtils.baseUrl = ShareUtils.getHost("host");
//                    HttpUtils.resetIp(HttpUtils.baseUrl);
//                    Toast.makeText(activity, "------->>"+HttpUtils.baseUrl, Toast.LENGTH_LONG).show();
//                }
                acontext = new WeakReference<>(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }

    @Override
    public void onTerminate() {
        unregisterReceiver(networkChangedReceiver);
        super.onTerminate();
    }

    private class LocationThread extends Thread {
        @Override
        public void run() {
            LocationManager_new.getInstance(getApplicationContext()).startLocation();
        }
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    public static Context getContext() {
        return MyApplication.acontext.get();
    }
}
