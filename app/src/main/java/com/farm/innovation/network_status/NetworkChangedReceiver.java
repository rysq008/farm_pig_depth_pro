package com.farm.innovation.network_status;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.farm.innovation.base.FarmAppConfig;

public class NetworkChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int netWorkStates = NetworkUtil.getNetWorkStates(context);

        switch (netWorkStates) {
            case NetworkUtil.TYPE_NONE:
            //    FarmAppConfig.isOfflineMode = true;
                Toast.makeText(context, "断网了", Toast.LENGTH_SHORT).show();
                FarmAppConfig.isNetConnected = false;
                //断网了
                break;
            case NetworkUtil.TYPE_MOBILE:
                FarmAppConfig.isNetConnected = true;
                //Toast.makeText(context, "打开了移动网络", Toast.LENGTH_SHORT).show();
                //打开了移动网络
                break;
            case NetworkUtil.TYPE_WIFI:
                FarmAppConfig.isNetConnected = true;
                //Toast.makeText(context, "打开了WIFI", Toast.LENGTH_SHORT).show();
                //打开了WIFI
                break;

            default:
                break;
        }
    }
}