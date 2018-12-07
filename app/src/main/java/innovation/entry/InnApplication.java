package innovation.entry;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.serenegiant.net.NetworkChangedReceiver;
import com.tencent.bugly.crashreport.CrashReport;


import org.opencv.android.OpenCVLoader;
import org.tensorflow.demo.env.Logger;

import innovation.crash.CrashHandler;
import innovation.utils.ConstUtils;
import innovation.utils.ImageLoaderUtils;

/**
 * Author by luolu, Date on 2018/8/24.
 * COMPANY：InnovationAI
 */

public class InnApplication extends Application {
    private final Logger mLogger = new Logger("InnApplication");
    private static Context context;
    /**
     * 离线模式
     */
    public static boolean isOfflineMode = false;
    NetworkChangedReceiver networkChangedReceiver;
    public static String offLineInsuredNo = "";
    public static final String OFFLINE_PATH = "/Android/data/com.innovation.animal_cowface/cache/innovation/animal/投保/offline/";
    public static final String OFFLINE_TEMP_PATH = "/Android/data/com.innovation.animal_cowface/cache/innovation/animal/offline_temp/";
    // 当APP适用不同动物种类时修改此处
    public static int   ANIMAL_TYPE = ConstUtils.ANIMAL_TYPE_PIG;
    public static int SCREEN_ORIENTATION = 0;
    public static String getlipeiTempNumber;
    public static String getStringTouboaExtra;
    public static String getCowEarNumber;
    public static String getCowType;
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        ImageLoaderUtils.initImageLoader(this);
        //UploadThread.getInstance(getApplicationContext()).upload(); //haojie del
        // init OpenCV
        if (!OpenCVLoader.initDebug()) {
            mLogger.e("Can't use OpenCV");
        }

        // TODO: 2018/8/13 By:LuoLu 牛险36cbcd676d；驴险0990b67c63；农险4a5d85637e
        InnApplication.setContext(getApplicationContext());
        InnApplication.context = getAppContext();
        CrashReport.initCrashReport(getApplicationContext(), "4a5d85637e", false);
        networkChangedReceiver = new NetworkChangedReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangedReceiver, intentFilter);
    }

    @Override
    public void onTerminate() {
        unregisterReceiver(networkChangedReceiver);
        super.onTerminate();
    }

    // TODO: 2018/8/13 By:LuoLu
    public static void setContext(Context cntxt) {
        context = cntxt;
    }
    public static Context getAppContext() {
        return InnApplication.context;
    }
}
