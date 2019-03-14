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
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVOSCloud;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiangchuang.risks.update.UpdateReceiver;
import com.xiangchuang.risks.utils.ShareUtils;
import com.xiangchuang.risks.view.LoginFamerActivity;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.util.TreeMap;

import innovation.crash.CrashHandler;
import innovation.database.MyObjectBox;
import innovation.entry.UpdateBean;
import innovation.location.LocationManager_new;
import innovation.network_status.NetworkChangedReceiver;
import innovation.utils.HttpRespObject;
import innovation.utils.HttpUtils;
import innovation.utils.ImageLoaderUtils;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


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
                if(! (activity instanceof LoginFamerActivity)){
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
                mUpdateTask = new GetUpDateTask(HttpUtils.GET_UPDATE_URL, null);
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
        private final TreeMap<String, String> mQueryMap;

        GetUpDateTask(String url, TreeMap map) {
            mUrl = url;
            mQueryMap = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                FormBody.Builder builder = new FormBody.Builder();
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
