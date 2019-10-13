package com.xiangchuangtec.luolu.animalcounter;

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
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastAliPayStyle;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.BuildConfig;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiangchuang.risks.utils.PigShareUtils;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import innovation.crash.CrashHandler;
import innovation.database.MyObjectBox;
import innovation.location.LocationManager_new;
import innovation.network_status.NetworkChangedReceiver;
import innovation.utils.GlobalDialogUtils;
import innovation.utils.HttpUtils;
import innovation.utils.ImageLoaderUtils;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import okhttp3.OkHttpClient;

public class PigAppConfig {
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

    public static boolean needUpDate = false;

    public static boolean offLineModle = false;

    private static List<Activity> oList;


    public void onCreate(Application app) {
        PigAppConfig.app = app;
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(app);
        oList = new ArrayList<>();
        PigShareUtils.init(app);

        HttpUtils.baseUrl = !AppConfig.isSDK_DEBUG() ? "http://f14e.innovationai.cn/nongxian2/" : "http://test1.innovationai.cn:8081/nongxian2/";
        HttpUtils.resetIp(HttpUtils.baseUrl);
        if (AppConfig.isOriginApk()) {
            //        // 初始化参数依次为 this, AppId, AppKey
            AVOSCloud.initialize(app, "sraDTfcMG5cUdE454yDX5Dv1-gzGzoHsz", "qQwz83LLwnWW6LyH8qkWU6J7");
            HttpUtils.baseUrl = PigShareUtils.getHost("host");
            HttpUtils.resetIp(HttpUtils.baseUrl);
            UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
            OkHttpClient client = new OkHttpClient();
            // create your own OkHttp client
            UploadService.HTTP_STACK = new OkHttpStack(client);
            CrashReport.initCrashReport(app, "2d3ff546dd", false);


            boxStore = MyObjectBox.builder().androidContext(app).build();
            if (isApkDebugable()) {
                new AndroidObjectBrowser(boxStore).start(app);
            }

            JPushStatsConfig.initStats(app);
            JPushStatsConfig.openCrashLog();
            //        //初始化 bugly

        }
        networkChangedReceiver = new NetworkChangedReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        app.registerReceiver(networkChangedReceiver, intentFilter);

        locationThread = new LocationThread();
        locationThread.start();

        version = getVersionName();

        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                if(null == acontext)
//                {
//                    HttpUtils.baseUrl = PigShareUtils.getHost("host");
//                    HttpUtils.resetIp(HttpUtils.baseUrl);
//                    Toast.makeText(activity, "------->>"+HttpUtils.baseUrl, Toast.LENGTH_LONG).show();
//                }
                addActivity(activity);
                PigAppConfig.activity = activity;
                if (activity != null && !activity.getClass().getCanonicalName().contains("LoginFamerActivity")) {
                    GlobalDialogUtils.getNotice(activity.getClass().getCanonicalName(), activity);
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                PigAppConfig.activity = activity;
                JPushStatsConfig.onPageStart(activity, activity.getClass().getCanonicalName());
            }

            @Override
            public void onActivityPaused(Activity activity) {
                JPushStatsConfig.onPageEnd(activity, activity.getClass().getCanonicalName());
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                removeActivity(activity);
            }
        });
        // make the library use your own OkHttp client
        //初始化 ImageLoader
        ImageLoaderUtils.initImageLoader(app);
        //初始化日志库
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return isApkDebugable();
            }
        });

        ToastUtils.init(app);
        ToastUtils.initStyle(new ToastAliPayStyle());


    }

    /**
     * 添加Activity
     */
    public void addActivity(Activity activity) {
// 判断当前集合中不存在该Activity
        if (!oList.contains(activity)) {
            oList.add(activity);//把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    public static void removeActivity(Activity activity) {
//判断当前集合中存在该Activity
//        if (oList.contains(activity)) {
//            oList.remove(activity);//从集合中移除
//            activity.finish();//销毁当前Activity
//        }
    }

    /**
     * 销毁所有的Activity
     */
    public synchronized static void removeALLActivity() {
        //通过循环，把集合中的所有Activity销毁
        Iterator<Activity> it = oList.iterator();
        while (it.hasNext()) {
            it.next().finish();
            it.remove();
        }
    }

    public void onTerminate() {
        app.unregisterReceiver(networkChangedReceiver);
        app.onTerminate();
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
        return boxStore = AppConfig.getBoxStore();
    }

    public static Activity getContext() {
        return PigAppConfig.activity;
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

    public interface eventListener {
        public void receiveEvent(Object o);
    }

    static List<eventListener> eventListeners = new ArrayList<eventListener>();

    public static void registEvent(eventListener e) {
        eventListeners.add(e);
    }

    public static void UnRegistEvent(eventListener e) {
        eventListeners.remove(e);
    }

    public static void postEvent(Object o) {
        for (eventListener el : eventListeners) {
            el.receiveEvent(o);
        }
    }

    public static boolean isOriginApk() {
        return "com.xiangchuangtec.luolu.animalcounter".equals(app.getPackageName());
    }

    public static boolean isApkDebugable() {
        try {
            ApplicationInfo info = activity.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }

    private static class Holder {
        static PigAppConfig PigAppConfig = new PigAppConfig();
    }

    public static PigAppConfig newInstance() {
        return Holder.PigAppConfig;
    }

    public static String TOKEY = "token";
    public static String DEPARTMENT_ID = "departmentId";
    public static String DEPARTMENT_NAME = "departmentName";
    public static String DEPARTMENT_STRUCT = "departmentStruct";
    public static String NAME = "userName";
    public static String PHONE_NUMBER = "phoneNumber";
    public static String IDENTITY_CARD = "idCard";
    public static boolean PIG_DEPTH_JOIN = true;
    public static String TASK_ID = "taskId";// 任务号或者保单号（必填） 1075274131248574464
    public static String USER_ID = "userid";// 用户id（必填）      89979dc663caa2580164f88b57796251
    public static String USER_NAME = "username";// 用户名（必填）
    public static String OFFICE_CODE = "officeCode";// 机构编码（必填）         14112100
    public static String OFFICE_NAME = "officeName";// 机构名称（必填）     文水县支公司
    public static String OFFICE_LEVEL = "officeLevel";// 机构层级                  3
    public static String PARENT_CODE = "parentCode";//  父机构编码               14119900
    public static String PARENT_OFFICE_NAMES = "parentOfficeNames";// 机构层级（必填）   总公司/山西分公司/吕梁市中心支公司/文水县支公司
    public static String PARENT_OFFICE_CODES = "parentOfficeCodes";
    public static String TYPE = "type";// 操作类型（必填）
    public static String PHONE = "phone";
    public static String ID_CARD = "idcard";
    public static String FARM_NAME = "farmName";// 养殖场名称（必填）

}
