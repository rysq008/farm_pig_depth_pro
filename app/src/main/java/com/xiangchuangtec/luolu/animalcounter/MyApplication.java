package com.xiangchuangtec.luolu.animalcounter;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by luolu on 2018/3/6.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PigAppConfig.newInstance().onCreate(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PigAppConfig.newInstance().onTerminate();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
