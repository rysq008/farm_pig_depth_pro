package com.innovation.pig.insurance;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.farm.innovation.base.FarmAppConfig;


/**
 * Created by luolu on 2018/3/6.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppConfig.newInstance().onCreate(this);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AppConfig.newInstance().onTerminate();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
