package andbase.com.mytestone;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.innovation.pig.insurance.AppConfig;
import com.xiangchuang.risks.view.LoginFamerActivity;

public class MyApp extends Application {

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
