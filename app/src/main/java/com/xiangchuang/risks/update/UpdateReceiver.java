package com.xiangchuang.risks.update;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.xiangchuang.risks.view.CompanyActivity;
import com.xiangchuang.risks.view.SelectFunctionActivity_new;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import static com.innovation.pig.insurance.AppConfig.needUpDate;


public class UpdateReceiver extends BroadcastReceiver {
    private AlertDialog.Builder mDialog;
    public static final String UPDATE_ACTION = "pigInsurance_app";
    //    private SharedPreferencesHelper mSharedPreferencesHelper;
    private boolean isShowDialog;
    private String result_json = "";

    public UpdateReceiver(boolean isShowDialog) {
        super();
        this.isShowDialog = isShowDialog;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("onReceive", "onReceive: ");
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        result_json = intent.getStringExtra("result_json");
        JSONObject object = null;
        try {
            object = new JSONObject(result_json);
            UpdateInfoModel model = new UpdateInfoModel();
            model.setAppname(object.getString("appname"));
            model.setLastForce(object.getString("lastForce"));
            model.setServerFlag(object.getString("serverFlag"));
            model.setServerVersion(object.getString("serverVersion"));
            model.setUpdateurl(object.getString("updateurl"));
            model.setUpgradeinfo(object.getString("upgradeinfo"));
            tempMap.put("app_update", model);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        UpdateInfoModel model = (UpdateInfoModel) tempMap
                //就是一个标志
                .get("app_update");
        try {

            /**
             * 获取到当前的本地版本
             */
            UpdateInformation.localVersion = AppConfig.getAppContext().getPackageManager().getPackageInfo(AppConfig.getAppContext().getPackageName(), 0).versionCode;
            Log.e("onReceive", "onReceive: " + UpdateInformation.localVersion);
            /**
             * 获取到当前的版本名字
             */
            UpdateInformation.versionName = AppConfig.getAppContext()
                    .getPackageManager()
                    .getPackageInfo(
                            AppConfig.getAppContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (model != null) {
            //app名字
            UpdateInformation.appname = model.getAppname();
            //服务器版本
            UpdateInformation.serverVersion = Integer.parseInt(model.getServerVersion());
            //服务器标志
            UpdateInformation.serverFlag = Integer.parseInt(model.getServerFlag());
            //强制升级
            UpdateInformation.lastForce = Integer.parseInt(model.getLastForce());
            //升级地址
            UpdateInformation.updateurl = model.getUpdateurl();
            //升级信息
            UpdateInformation.upgradeinfo = model.getUpgradeinfo();

            //检查版本
            checkVersion();
        }

    }

    /**
     * 检查版本更新
     *
     */
    public void checkVersion() {
        if (UpdateInformation.localVersion < UpdateInformation.serverVersion) {
            // 需要进行更新
//            mSharedPreferencesHelper.putIntValue(
//                    //有新版本
//                    SharedPreferencesTag.IS_HAVE_NEW_VERSION, 1);
            //更新
            update();
            Log.e("checkVersion", "checkVersion: " + UpdateInformation.serverVersion);
        } else {
//            mSharedPreferencesHelper.putIntValue(
//                    SharedPreferencesTag.IS_HAVE_NEW_VERSION, 0);
            if (isShowDialog) {
                //没有最新版本，不用升级
                noNewVersion();
            }
            clearUpateFile();
            needUpDate = false;
        }
    }

    /**
     * 进行升级
     *
     */
    private void update() {
        if (UpdateInformation.serverFlag == 1) {
            // 官方推荐升级
            if (UpdateInformation.localVersion < UpdateInformation.lastForce) {
                //强制升级
                forceUpdate();
            } else {
                //正常升级
                normalUpdate();
            }

        } else if (UpdateInformation.serverFlag == 2) {
            // 官方强制升级
            forceUpdate();
        }
    }

    /**
     * 没有新版本
     *
     */
    private void noNewVersion() {
        mDialog = new AlertDialog.Builder(AppConfig.getActivity());
        mDialog.setIcon(R.drawable.cowface);
        mDialog.setTitle("版本更新");
        mDialog.setMessage("当前为最新版本");
        mDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    /**
     * 强制升级 ，如果不点击确定升级，直接退出应用
     *
     */
    private void forceUpdate() {
        mDialog = new AlertDialog.Builder(AppConfig.getActivity());
        mDialog.setIcon(R.drawable.cowface);
        mDialog.setTitle("版本更新");
        mDialog.setMessage(UpdateInformation.upgradeinfo);

        mDialog.setPositiveButton("马上升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent mIntent = new Intent(AppConfig.getActivity(), AppUpgradeService.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //传递数据
//                mIntent.putExtra("appname", UpdateInformation.appname);
                mIntent.putExtra("mDownloadUrl", UpdateInformation.updateurl);
                mIntent.putExtra("appname", UpdateInformation.appname);
                AppConfig.getActivity().startService(mIntent);

            }
        }).setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 直接退出应用
                //ManagerActivity.getInstance().finishActivity();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }).setCancelable(false).create().show();
    }

    /**
     * 正常升级，用户可以选择是否取消升级
     *
     */
    private void normalUpdate() {
        needUpDate = true;

//        if(AppConfig.getActivity() instanceof SelectFunctionActivity_new){
//            ((SelectFunctionActivity_new)AppConfig.getActivity()).setSign();
//        }else{
//            ((CompanyActivity)AppConfig.getActivity()).setSign();
//        }

//        mDialog = new AlertDialog.Builder(AppConfig.getActivity());
//        mDialog.setIcon(R.drawable.cowface);
//        mDialog.setTitle("版本更新");
//        mDialog.setMessage(UpdateInformation.upgradeinfo);
//        mDialog.setCancelable(false);
//        mDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClickView(DialogInterface dialog, int which) {
//                Intent mIntent = new Intent(context, AppUpgradeService.class);
//                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                //传递数据
////                mIntent.putExtra("appname", UpdateInformation.appname);
//                mIntent.putExtra("mDownloadUrl", UpdateInformation.updateurl);
//                mIntent.putExtra("appname", UpdateInformation.appname);
//                context.startService(mIntent);
//            }
//        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClickView(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        }).create().show();
    }

    /**
     * 清理升级文件
     *
     */
    private void clearUpateFile() {
        File updateDir;
        File updateFile;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            updateDir = new File(Environment.getExternalStorageDirectory(),
                    UpdateInformation.downloadDir);
        } else {
            updateDir = AppConfig.getActivity().getFilesDir();
        }
        updateFile = new File(updateDir.getPath(), AppConfig.getActivity().getResources()
                .getString(R.string.app_name) + ".apk");
        if (updateFile.exists()) {
            Log.d("update", "升级包存在，删除升级包");
            updateFile.delete();
        } else {
            Log.d("update", "升级包不存在，不用删除升级包");
        }
    }


}
