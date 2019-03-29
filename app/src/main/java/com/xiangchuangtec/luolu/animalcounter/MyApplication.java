package com.xiangchuangtec.luolu.animalcounter;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVOSCloud;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiangchuang.risks.update.UpdateReceiver;
import com.xiangchuang.risks.utils.ShareUtils;
import com.xiangchuang.risks.view.LoginFamerActivity;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import io.objectbox.android.AndroidObjectBrowser;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static innovation.entry.InnApplication.getCowType;
import static innovation.entry.InnApplication.getlipeiTempNumber;


/**
 * Created by luolu on 2018/3/6.
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
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

    private static Activity acontext;

    private static BoxStore boxStore;

    public static String version;

    public static boolean needUpDate = false;

    public UpdateReceiver mUpdateReceiver;
    private IntentFilter mIntentFilter;
    private GetUpDateTask mUpdateTask;
    private UpdateBean insurresp_company;
    private String errStr_company;

    private int isFirst = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(getApplicationContext());

        //        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this, "sraDTfcMG5cUdE454yDX5Dv1-gzGzoHsz", "qQwz83LLwnWW6LyH8qkWU6J7");
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

        version = getVersionName();

        registerBroadcast();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                if(null == acontext)
//                {
//                    HttpUtils.baseUrl = ShareUtils.getHost("host");
//                    HttpUtils.resetIp(HttpUtils.baseUrl);
//                    Toast.makeText(activity, "------->>"+HttpUtils.baseUrl, Toast.LENGTH_LONG).show();
//                }
                acontext = activity;
                if(activity != null && !activity.getClass().getCanonicalName().contains("LoginFamerActivity")){
                    GlobalDialogUtils.getNotice(activity.getClass().getCanonicalName(), activity);
                }

                if(!(activity instanceof LoginFamerActivity)){
                    isFirst++;
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

        boxStore = MyObjectBox.builder().androidContext(this).build();
        if (BuildConfig.DEBUG)
            new AndroidObjectBrowser(boxStore).start(this);
    }

    private void doUpDateTask(){
        if(isFirst == 1){
            if (acontext.getIntent().getFlags() != Intent.FLAG_ACTIVITY_SINGLE_TOP) {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(acontext, "getFlags==" + acontext.getIntent().getFlags(), Toast.LENGTH_SHORT).show();
                }

                Map<String, String> query = new HashMap<>();

                String type = PreferencesUtils.getStringValue(Constants.companyfleg, MyApplication.getAppContext());
                if (("1").equals(type)) {
                    query.put("uid", PreferencesUtils.getStringValue(Constants.id, MyApplication.getAppContext()));
                } else {
                    query.put("uid", PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext()) + "");
                }
                query.put("enId", PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext()));
                query.put("longitude", PreferencesUtils.getStringValue(Constants.longitude, MyApplication.getAppContext()));
                query.put("latitude", PreferencesUtils.getStringValue(Constants.latitude, MyApplication.getAppContext()));
                query.put("phoneModel", android.os.Build.MODEL);
                query.put("timestamp", System.currentTimeMillis() + "");

                TelephonyManager phone = (TelephonyManager) MyApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
                //IMEI
                if (ActivityCompat.checkSelfPermission(MyApplication.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    query.put("phoneImei",  "");
                }else {
                    query.put("phoneImei", phone.getDeviceId() + "");
                }
                query.put("version",MyApplication.version);

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
        this.registerReceiver(mUpdateReceiver, mIntentFilter);
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
                sendBroadcast(intent);


            } else if (!success) {
                Toast.makeText(acontext, "网络接口请求异常！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mUpdateTask = null;
        }
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

    public static BoxStore getBoxStore() {
        return boxStore;
    }

    public static Context getContext() {
        return MyApplication.acontext;
    }

    /**
     * get App versionName
     *
     * @return versionName
     */
    private String getVersionName() {
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
