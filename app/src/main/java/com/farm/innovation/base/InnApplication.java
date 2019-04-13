package com.farm.innovation.base;

import android.app.Application;

//import cn.jpush.android.api.JPushInterface;

/**
 * Author by luolu, Date on 2018/8/24.
 * COMPANY：InnovationAI
 */

public class InnApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FarmAppConfig.newInstance().onCreate(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FarmAppConfig.newInstance().onTerminate();
    }
}
