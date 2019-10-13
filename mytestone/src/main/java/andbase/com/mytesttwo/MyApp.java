package andbase.com.mytesttwo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.innovation.pig.insurance.AppConfig;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppConfig.newInstance().onCreate(this,true);
//        FarmAppConfig.newInstance().onCreate(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AppConfig.newInstance().onTerminate();
//        FarmAppConfig.newInstance().onTerminate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
