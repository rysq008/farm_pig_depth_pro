package com.xiangchuang.risks.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.xiangchuang.risks.update.AppUpgradeService;
import com.xiangchuang.risks.update.UpdateInfoModel;
import com.innovation.pig.insurance.R;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import innovation.entry.UpdateBean;
import innovation.utils.HttpUtils;
import innovation.utils.PreferencesUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * @Author: Lucas.Cui
 * 时   间：2019/4/24
 * 简   述：<功能简述>
 */
public class AppUpdateUtils {
    private Activity mActivity;
    private UpdateResultListener mListener;
    private AlertDialog.Builder mDialog;

    public void appVersionCheck(Activity activity, UpdateResultListener listener) {
        this.mActivity = activity;
        this.mListener = listener;
        Map<String, String> query = new HashMap<>();
        String type = PreferencesUtils.getStringValue(Constants.companyfleg, mActivity);
        if (("1").equals(type)) {
            query.put("uid", PreferencesUtils.getStringValue(Constants.id, mActivity));
        } else {
            query.put("uid", PreferencesUtils.getIntValue(Constants.en_user_id, mActivity) + "");
        }
        query.put("enId", PreferencesUtils.getStringValue(Constants.en_id, mActivity));
        query.put("longitude", PreferencesUtils.getStringValue(Constants.longitude, mActivity));
        query.put("latitude", PreferencesUtils.getStringValue(Constants.latitude, mActivity));
        query.put("phoneModel", android.os.Build.MODEL);
        query.put("timestamp", System.currentTimeMillis() + "");

        //IMEI
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            query.put("phoneImei", "");
        } else {
            query.put("phoneImei", SystemUtil.getIMEI(mActivity) + "");
        }
        query.put("version", AppConfig.version);

        try {
            OkHttp3Util.doPost(HttpUtils.GET_UPDATE_URL, query, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String content = response.body().string();
                    if (mActivity == null || mActivity.isFinishing()) return;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateInfoModel model = null;
                            UpdateBean insurresp_company = null;
                            try {
                                insurresp_company = (UpdateBean) HttpUtils.processResp_update(content);
                                Gson gson = new Gson();
                                model = gson.fromJson(insurresp_company.data, UpdateInfoModel.class);
                                //检查版本
                                checkVersion(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查版本更新
     */
    private void checkVersion(UpdateInfoModel bean) throws Exception {
        if (SystemUtil.getLocalVersion() < Integer.valueOf(bean.getServerVersion())) {
            //更新
            if (Integer.valueOf(bean.getServerFlag()) == 1) {
                // 官方推荐升级
                if (!TextUtils.isEmpty(bean.getLastForce()) && SystemUtil.getLocalVersion() < Integer.valueOf(bean.getLastForce())) {
                    //强制升级
                    forceUpdate(bean);
                } else {
                    //正常升级
                    normalUpdate(bean);
                }
            } else if (Integer.valueOf(bean.getServerFlag()) == 2) {
                // 官方强制升级
                forceUpdate(bean);
            }
        } else {
            mListener.update(false, bean);
        }
    }

    /**
     * 强制升级 ，如果不点击确定升级，直接退出应用
     */
    private void forceUpdate(UpdateInfoModel bean) {
        if (mActivity == null || mActivity.isFinishing()) return;
        mDialog = new AlertDialog.Builder(mActivity);
        mDialog.setIcon(R.drawable.cowface);
        mDialog.setTitle("版本更新");
        mDialog.setMessage(bean.getUpgradeinfo());

        mDialog.setPositiveButton("马上升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent mIntent = new Intent(mActivity, AppUpgradeService.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //传递数据
                mIntent.putExtra("data", bean);
                mActivity.startService(mIntent);

            }
        }).setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 直接退出应用
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }).setCancelable(false).create().show();
    }

    /**
     * 正常升级，用户可以选择是否取消升级
     */
    private void normalUpdate(UpdateInfoModel bean) {
        if (mActivity == null || mActivity.isFinishing()) return;
        mListener.update(true, bean);
    }

    public interface UpdateResultListener {
        void update(boolean isUpdate, UpdateInfoModel bean);
    }
}
