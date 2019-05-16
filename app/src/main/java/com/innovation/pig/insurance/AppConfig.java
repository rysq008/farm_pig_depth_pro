package com.innovation.pig.insurance;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.avos.avoscloud.AVOSCloud;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.biz.welcome.WelcomeActivity;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.update.UpdateInfoModel;
import com.xiangchuang.risks.utils.AppUpdateUtils;
import com.xiangchuang.risks.utils.ShareUtils;
import com.xiangchuang.risks.view.LoginPigAarActivity;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import org.greenrobot.eventbus.EventBus;

import innovation.crash.CrashHandler;
import innovation.database.MyObjectBox;
import innovation.location.LocationManager_new;
import innovation.network_status.NetworkChangedReceiver;
import innovation.utils.GlobalDialogUtils;
import innovation.utils.HttpUtils;
import innovation.utils.ImageLoaderUtils;
import io.objectbox.BoxStore;
import okhttp3.OkHttpClient;

public class AppConfig {
    private static final String TAG = "MyApplication";
    private CrashHandler mCrashHandler;
    private static Application app;
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

    private static Activity activity;

    private static BoxStore boxStore;

    public static String version;

    public static UpdateInfoModel getUpdateInfoModel() {
        return updateInfoModel;
    }

    private static UpdateInfoModel updateInfoModel;

    public void onCreate(Application application) {
        app = application;
        FarmAppConfig.newInstance().onCreate(app);
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(app);
        ShareUtils.init(app);
        boxStore = MyObjectBox.builder().androidContext(app).build();
        //初始化日志库
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return BuildConfig.DEBUG;
            }
        });

//        if (AppConfig.isApkInDebug())
//            new AndroidObjectBrowser(boxStore).start(app);

        if (AppConfig.isOriginApk()) {
            //        // 初始化参数依次为 this, AppId, AppKey
            HttpUtils.baseUrl = ShareUtils.getHost("host");
            HttpUtils.resetIp(HttpUtils.baseUrl);
            UploadService.NAMESPACE = app.getPackageName()/*BuildConfig.APPLICATION_ID*/;
            OkHttpClient client = new OkHttpClient();
            // create your own OkHttp client
            UploadService.HTTP_STACK = new OkHttpStack(client);
            // make the library use your own OkHttp client
            //初始化 bugly
            CrashReport.initCrashReport(app, "2d3ff546dd", false);
            AVOSCloud.initialize(app, "sraDTfcMG5cUdE454yDX5Dv1-gzGzoHsz", "qQwz83LLwnWW6LyH8qkWU6J7");
        }


        //初始化 ImageLoader
        ImageLoaderUtils.initImageLoader(app);

        networkChangedReceiver = new NetworkChangedReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        app.registerReceiver(networkChangedReceiver, intentFilter);

        locationThread = new LocationThread();
        locationThread.start();

        version = getVersionName();

        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                if(null == activity)
//                {
//                    HttpUtils.baseUrl = FarmerShareUtils.getHost("host");
//                    HttpUtils.resetIp(HttpUtils.baseUrl);
//                    Toast.makeText(activity, "------->>"+HttpUtils.baseUrl, Toast.LENGTH_LONG).show();
//                }
                AppConfig.activity = activity;
                if (activity instanceof BaseActivity) {
                    if (activity != null && !(activity instanceof LoginPigAarActivity)) {
                        GlobalDialogUtils.getNotice(activity.getClass().getCanonicalName(), activity);
                    }
                }
                if (activity instanceof WelcomeActivity && AppConfig.isOriginApk()) {
                    AppUpdateUtils.newInstance().appVersionCheck(activity, new AppUpdateUtils.UpdateResultListener() {
                        @Override
                        public void update(boolean isUpdate, UpdateInfoModel bean) {
                            updateInfoModel = bean.setUpdate(isUpdate);
                            EventBus.getDefault().post(updateInfoModel);
                        }
                    });
                }
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

    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkInDebug() {
        try {
            ApplicationInfo info = app.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void onTerminate() {
        app.unregisterReceiver(networkChangedReceiver);
        FarmAppConfig.newInstance().onTerminate();
    }

    private class LocationThread extends Thread {
        @Override
        public void run() {
            LocationManager_new.getInstance(app).startLocation();
        }
    }

    public static Application getAppContext() {
        return app;
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

    public static BoxStore getBoxStore() {
        return boxStore;
    }

    public static Activity getActivity() {
        return activity;
    }

    /**
     * get App versionName
     *
     * @return versionName
     */
    private String getVersionName() {
        PackageManager packageManager = app.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(app.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static boolean isOriginApk() {
        return ("com.xiangchuangtec.luolu.animalcounter".equals(AppConfig.getAppContext().getPackageName()));
    }

    public static AppConfig newInstance() {
        return Holder.appConfig;
    }

    private static class Holder {
        static AppConfig appConfig = new AppConfig();
    }

    public static String TOKEY = "token";
    public static String DEPARTMENT_ID = "departmentId";
    public static String USER_ID = "userId";
    public static String NAME = "name";
    public static String PHONE_NUMBER = "phoneNumber";
    public static String IDENTITY_CARD = "identityCard";
}
