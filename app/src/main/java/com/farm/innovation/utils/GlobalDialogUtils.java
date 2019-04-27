package com.farm.innovation.utils;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.TipsBean;
import com.farm.innovation.biz.dialog.TipsDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @Author: Lucas.Cui
 * 时   间：2019/3/27
 * 简   述：<功能简述> 全局弹框处理
 */
public class GlobalDialogUtils {
    private static final String TAG = "GlobalDialogUtils";

    //全局提示
//    private TipsDialog tipsDialog;
    public static void getNotice(String useCase, Activity activity) {
        Log.d(TAG, "========name======" + activity.getLocalClassName());
        TipsDialog tipsDialog = new TipsDialog(activity);
        Map<String, String> mapHeader = new HashMap<>();
        mapHeader.put("imei", SystemUtil.getIMEI(activity));
        Map<String, String> mapBody = new HashMap<>();
        mapBody.put("appType", "1");
        mapBody.put("useCase", useCase);
        mapBody.put("userId", FarmerPreferencesUtils.getStringValue(HttpUtils.user_id, FarmAppConfig.getActivity()));
        OkHttp3Util.doPost(HttpUtils.GET_NOTICE, mapBody, mapHeader, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "======onFailure======" + e.toString());
                AVOSCloudUtils.saveErrorMessage(e, GlobalDialogUtils.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TipsBean result;
                        try {
                            Gson gson = new Gson();
                            Type type = new TypeToken<TipsBean>() {
                            }.getType();
                            result = gson.fromJson(string, type);

                            //如果获取的是1就弹出提示
                            if (result.getData().getStatus() == 1) {
                                View.OnClickListener listenerReCollect = v -> {
                                    tipsDialog.dismiss();
                                };
                                tipsDialog.setTitlemessage(result.getData().getTitle());
                                //tipsDialog.setContentmessage(tipsBean.getData().getContent());
                                if (result.getData().getType().equals("2")) {
                                    tipsDialog.setContentmessage(result.getData().getContent(), new TipsDialog.handleDialogListener() {
                                        @Override
                                        public void handleWv(WebView wvContent, String s) {
                                            wvContent.loadUrl(s);
                                            wvContent.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(wvContent != null){
                                                        wvContent.requestLayout();
                                                        wvContent.reload();
                                                    }
                                                }
                                            },1000);

                                        }
                                    });
                                } else {
                                    tipsDialog.setContentmessage(result.getData().getContent(), null);
                                }
                                tipsDialog.setBtnReCollectListener(listenerReCollect);
                                tipsDialog.show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            AVOSCloudUtils.saveErrorMessage(e, GlobalDialogUtils.class.getSimpleName());
                        }
                    }
                });
            }
        });
    }
}
