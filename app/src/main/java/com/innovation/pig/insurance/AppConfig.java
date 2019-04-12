package com.innovation.pig.insurance;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVOSCloud;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.PreferencesUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiangchuang.risks.update.UpdateReceiver;
import com.xiangchuang.risks.utils.ShareUtils;
import com.xiangchuang.risks.view.LoginFamerActivity;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import innovation.crash.CrashHandler;
import innovation.database.MyObjectBox;
import innovation.entry.UpdateBean;
import innovation.location.LocationManager_new;
import innovation.network_status.NetworkChangedReceiver;
import innovation.utils.GlobalDialogUtils;
import innovation.utils.HttpRespObject;
import innovation.utils.HttpUtils;
import innovation.utils.ImageLoaderUtils;
import io.objectbox.BoxStore;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

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

    public static boolean needUpDate = false;

    public UpdateReceiver mUpdateReceiver;
    private IntentFilter mIntentFilter;
    private GetUpDateTask mUpdateTask;
    private UpdateBean insurresp_company;
    private String errStr_company;

    private int isFirst = 0;

    public void onCreate(Application application) {
        app = application;
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(app);

        //        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(app, "sraDTfcMG5cUdE454yDX5Dv1-gzGzoHsz", "qQwz83LLwnWW6LyH8qkWU6J7");
        ShareUtils.init(app);
        HttpUtils.baseUrl = ShareUtils.getHost("host");
        HttpUtils.resetIp(HttpUtils.baseUrl);
        UploadService.NAMESPACE = app.getPackageName()/*BuildConfig.APPLICATION_ID*/;
        OkHttpClient client = new OkHttpClient();
        // create your own OkHttp client
        UploadService.HTTP_STACK = new OkHttpStack(client);
        // make the library use your own OkHttp client

        //初始化 ImageLoader
        ImageLoaderUtils.initImageLoader(app);
        //初始化 bugly
        CrashReport.initCrashReport(app, "2d3ff546dd", false);
        networkChangedReceiver = new NetworkChangedReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        app.registerReceiver(networkChangedReceiver, intentFilter);

        locationThread = new LocationThread();
        locationThread.start();

        version = getVersionName();

        registerBroadcast();

        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                if(null == activity)
//                {
//                    HttpUtils.baseUrl = ShareUtils.getHost("host");
//                    HttpUtils.resetIp(HttpUtils.baseUrl);
//                    Toast.makeText(activity, "------->>"+HttpUtils.baseUrl, Toast.LENGTH_LONG).show();
//                }
                AppConfig.activity = activity;
                if (activity != null && ! (activity instanceof LoginFamerActivity)) {
                    GlobalDialogUtils.getNotice(activity.getClass().getCanonicalName(), activity);
                }

                if (!(activity instanceof LoginFamerActivity)) {
                    isFirst++;
                    if ("com.xiangchuangtec.luolu.animalcounter".equals(AppConfig.getAppContext().getPackageName()))
                        doUpDateTask();
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

        boxStore = MyObjectBox.builder().androidContext(app).build();
//        if (AppConfig.isApkInDebug())
//            new AndroidObjectBrowser(boxStore).start(app);
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

    private void doUpDateTask() {
        if (isFirst == 1) {
            if (activity.getIntent().getFlags() != Intent.FLAG_ACTIVITY_SINGLE_TOP) {
                if (AppConfig.isApkInDebug()) {
                    Toast.makeText(activity, "getFlags==" + activity.getIntent().getFlags(), Toast.LENGTH_SHORT).show();
                }

                Map<String, String> query = new HashMap<>();

                String type = PreferencesUtils.getStringValue(Constants.companyfleg, app);
                if (("1").equals(type)) {
                    query.put("uid", PreferencesUtils.getStringValue(Constants.id, app));
                } else {
                    query.put("uid", PreferencesUtils.getIntValue(Constants.en_user_id, app) + "");
                }
                query.put("enId", PreferencesUtils.getStringValue(Constants.en_id, app));
                query.put("longitude", PreferencesUtils.getStringValue(Constants.longitude, app));
                query.put("latitude", PreferencesUtils.getStringValue(Constants.latitude, app));
                query.put("phoneModel", android.os.Build.MODEL);
                query.put("timestamp", System.currentTimeMillis() + "");

                TelephonyManager phone = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
                //IMEI
                if (ActivityCompat.checkSelfPermission(app, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    query.put("phoneImei", "");
                } else {
                    query.put("phoneImei", phone.getDeviceId() + "");
                }
                query.put("version", version);

                Set set = query.keySet();
                for (Object aSet : set) {
                    String key = (String) aSet;
                    String value = query.get(key);
                    Log.e("设备信息", "\nkey:" + key + "==========value:" + value);
                }


                mUpdateTask = new GetUpDateTask(HttpUtils.GET_UPDATE_URL, query);
                mUpdateTask.execute((Void) null);
            }
        }
    }

    private void registerBroadcast() {
        mUpdateReceiver = new UpdateReceiver(false);
        mIntentFilter = new IntentFilter(UpdateReceiver.UPDATE_ACTION);
        app.registerReceiver(mUpdateReceiver, mIntentFilter);
    }


    private class GetUpDateTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final Map<String, String> mQueryMap;

        GetUpDateTask(String url, Map map) {
            mUrl = url;
            mQueryMap = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                FormBody.Builder builder = new FormBody.Builder();
                // Add Params to Builder
                for (Map.Entry<String, String> entry : mQueryMap.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }
                // Create RequestBody
                RequestBody formBody = builder.build();

                String response = HttpUtils.post(mUrl, formBody);
                if (response == null) {
                    return false;
                }
                Log.d("MyApplication", mUrl + "\nresponse:\n" + response);

                if (HttpUtils.GET_UPDATE_URL.equalsIgnoreCase(mUrl)) {
                    insurresp_company = (UpdateBean) HttpUtils.processResp_update(response);

                    Log.e("MyApplication", "insurresp_company: " + insurresp_company.toString());

                    if (insurresp_company == null) {
                        errStr_company = "请求错误！";
                        return false;
                    }
                    if (insurresp_company.status != HttpRespObject.STATUS_OK) {
                        errStr_company = insurresp_company.msg;
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                errStr_company = "服务器错误！";
                return false;
            }
            //  register the new account here.

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUpdateTask = null;

            if (success & HttpUtils.GET_UPDATE_URL.equalsIgnoreCase(mUrl)) {
                Intent intent = new Intent();
                intent.setAction(UpdateReceiver.UPDATE_ACTION);
                intent.putExtra("result_json", String.valueOf(insurresp_company.data));

                //发送广播
                app.sendBroadcast(intent);


            } else if (!success) {
                Toast.makeText(activity, "网络接口请求异常！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mUpdateTask = null;
        }
    }

    public void onTerminate() {
        app.unregisterReceiver(networkChangedReceiver);
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
