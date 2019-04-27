package com.farm.innovation.base;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVOSCloud;
import com.farm.innovation.bean.UpdateBean;
import com.farm.innovation.biz.welcome.WelcomeActivity;
import com.farm.innovation.crash.CrashHandler;
import com.farm.innovation.location.LocationManager;
import com.farm.innovation.login.Utils;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.network_status.NetworkChangedReceiver;
import com.farm.innovation.update.UpdateReceiver;
import com.farm.innovation.utils.ConstUtils;
import com.farm.innovation.utils.FarmerShareUtils;
import com.farm.innovation.utils.GlobalDialogUtils;
import com.farm.innovation.utils.HttpRespObject;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.ImageLoaderUtils;
import com.tencent.bugly.crashreport.CrashReport;

import org.opencv.android.OpenCVLoader;
import org.tensorflow.demo.env.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class FarmAppConfig {
    private final Logger mLogger = new Logger("InnApplication");
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    /**
     * 离线模式
     */
    public static boolean isOfflineMode = false;
    NetworkChangedReceiver networkChangedReceiver;
    public static String offLineInsuredNo = "";
    public static final String OFFLINE_PATH = "/Android/data/com.farm.innovation.animal_cowface/cache/innovation/animal/投保/offline/";
    public static final String OFFLINE_TEMP_PATH = "/Android/data/com.farm.innovation.animal_cowface/cache/innovation/animal/offline_temp/";
    // 当APP适用不同动物种类时修改此处
    public static int ANIMAL_TYPE = ConstUtils.ANIMAL_TYPE_NONE;
    // TODO: 2018/8/31 By:LuoLu  0：SCREEN_ORIENTATION_PORTRAIT，1：SCREEN_ORIENTATION_LANDSCAPE；
    public static int SCREEN_ORIENTATION = 0;
    public static String getlipeiTempNumber;
    public static String getStringTouboaExtra;
    public static String getCowEarNumber;
    public static String getCowType;
    public static String touBaoVieoFlag = "touBaoVieoFlag";
    public static String liPeiVieoFlag = "liPeiVieoFlag";

    /**
     * 阈值集合
     */
    public static final String THRESHOLD_LIST = "thresholdlist";

    public static boolean isNetConnected = false;

    private static FarmAppConfig mInstance;

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void runUITask(Runnable run) {
        mHandler.post(run);
    }

    /* 计时器录制时长 */
    public static long during = 0;

    /* 计时器录制开始时间 */
    public static long timeVideoStart;

    /* 存储当前保存图片的时间戳 */
    public static long lastCurrentTime = 0;

    private static Activity activity;
    //记录没录制成功次数 最大是2
    public static int badTimes = 0;
    //版本号
    public static String version;
    //记录失败次数
    public static int debugNub = 0;

    public static String lipeia = "lipeia";//30 ;
    public static String lipeib = "lipeib";//30 ;
    public static String lipein = "lipein";//120 ;
    public static String lipeim = "lipeim";//240 ;
    public static String phone = "kefuphone";
    public static String customServ = "kefucustomServ";

    public static boolean needUpDate = false;

    public UpdateReceiver mUpdateReceiver;
    private IntentFilter mIntentFilter;
    private GetUpDateTask mUpdateTask;
    private UpdateBean insurresp_company;
    private String errStr_company;
    private LocationManager locationManager;
    private static Application mApp;

    public void onCreate(Application app) {
        mApp = app;
        CrashHandler.getInstance().init(mApp);
        ImageLoaderUtils.initImageLoader(mApp);
        FarmerShareUtils.init(mApp);

        // init OpenCV
        if (!OpenCVLoader.initDebug()) {
            mLogger.e("Can't use OpenCV");
        }

        if (FarmAppConfig.isOriginApk()) {
            HttpUtils.baseUrl = FarmerShareUtils.getHost("host");
            HttpUtils.resetIp(HttpUtils.baseUrl);
            // TODO: 2018/8/13 By:LuoLu 农险 4a5d85637e
            CrashReport.initCrashReport(mApp, "99f24e923f", false);//4a5d85637e
            // 初始化参数依次为 this, AppId, AppKey
            AVOSCloud.initialize(mApp, "dIrWGUpitcWdncPRNIgqG5uK-gzGzoHsz", "fCWlYciXliHprK4yndyGm7Yi");
        }

        networkChangedReceiver = new NetworkChangedReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mApp.registerReceiver(networkChangedReceiver, intentFilter);

        locationManager = LocationManager.getInstance(mApp);
        locationManager.startLocation();

        registerBroadcast();
//        JPushInterface.init(getApplication());
        mApp.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                FarmAppConfig.activity = activity;

                if(activity instanceof BaseActivity){
                    if (activity != null && !(activity instanceof WelcomeActivity)) {
                        GlobalDialogUtils.getNotice(activity.getClass().getCanonicalName(), activity);
                    }
                    if (activity instanceof HomeActivity) {
                        if (FarmAppConfig.isOriginApk())
                            doUpDateTask();
                    }
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                FarmAppConfig.activity = activity;
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

    private void doUpDateTask() {
        if (activity.getIntent().getFlags() != Intent.FLAG_ACTIVITY_SINGLE_TOP) {
            if (FarmAppConfig.isApkDebugable()) {
                Toast.makeText(activity, "getFlags==" + activity.getIntent().getFlags(), Toast.LENGTH_SHORT).show();
            }
            Map<String, String> query = new HashMap<>();

            SharedPreferences pref = mApp.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
            query.put("uid", pref.getInt("uid", 0) + "");
            query.put("longitude", locationManager.currentLon + "");
            query.put("latitude", locationManager.currentLat + "");
            query.put("phoneModel", android.os.Build.MODEL);
            query.put("timestamp", System.currentTimeMillis() + "");

            TelephonyManager phone = (TelephonyManager) FarmAppConfig.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            //IMEI
            if (ActivityCompat.checkSelfPermission(FarmAppConfig.getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
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
            query.put("version", FarmAppConfig.version);

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

    private void registerBroadcast() {
        mUpdateReceiver = new UpdateReceiver(false);
        mIntentFilter = new IntentFilter(UpdateReceiver.UPDATE_ACTION);
        mApp.registerReceiver(mUpdateReceiver, mIntentFilter);
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
                mApp.sendBroadcast(intent);


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
        mApp.unregisterReceiver(networkChangedReceiver);
        mInstance = this;
    }

    public static Application getApplication() {
        return FarmAppConfig.mApp;
    }

    public static Activity getActivity() {
        return FarmAppConfig.activity;
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

    public static boolean isOriginApk(){
        return "com.farm.innovation.nongxian".equals(FarmAppConfig.getApplication().getPackageName());
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
        static com.farm.innovation.base.FarmAppConfig FarmAppConfig = new FarmAppConfig();
    }

    public static FarmAppConfig newInstance() {
        return Holder.FarmAppConfig;
    }

    public static String TOKEY = "token";
    public static String DEPARTMENT_ID = "departmentId";
    public static String USER_ID = "userId";
    public static String NAME = "name";
    public static String PHONE_NUMBER = "phoneNumber";
    public static String IDENTITY_CARD = "identityCard";
}
