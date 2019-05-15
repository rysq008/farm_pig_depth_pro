package com.xiangchuang.risks.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.ShareUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import innovation.upload.UploadService;
import innovation.utils.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.serenegiant.utils.UIThreadHelper.runOnUiThread;
import static com.xiangchuang.risks.view.OutHurdleActivity.TAG;

/**
 * @author 56861
 */
public class LoginFamerServer extends Service {

    public ProgressDialog mProgressDialog;
    private static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    protected void initData() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, UploadService.class));
        } else {
            startService(new Intent(this, UploadService.class));
        }



//        Box<VideoUploadTable> box = AppConfig.getBoxStore().boxFor(VideoUploadTable.class);
//        box.removeAll();
//        List<VideoUploadTable> list = new ArrayList<>();
//        for (int i = 0; i <5 ; i++) {
//            VideoUploadTable bean = new VideoUploadTable();
//            bean.iscomplete = false;
//            bean.timesflag = SystemClock.uptimeMillis()+"";
//            bean.fpath="/mnt/sdcard";
//            box.put(bean);
//            list.add(bean);
//        }
//        box.put(list);

//        VideoUploadTable videoUploadTable = box.query().equal(VideoUploadTable_.timesflag,"205843552").build().findUnique();
//
//        if (videoUploadTable != null) {
//            videoUploadTable.fpath = "ASDF";
//            box.put(videoUploadTable);
//        }

//        List<VideoUploadTable> slist = box.query().equal(VideoUploadTable_.iscomplete,false).order(VideoUploadTable_._id).build().find();

        // 避免从桌面启动程序后，会重新实例化入口类的activity
        // 判断当前activity是不是所在任务栈的根
//        if (!this.isTaskRoot()) {
//            Intent intent = getIntent();
//            if (intent != null) {
//                String action = intent.getAction();
//                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
//                    finish();
//                    return;
//                }
//            }
//        }


//        if (!hasPermission2())
//        {
//            AlertDialogManager.showMessageDialog(LoginFamerServer.this, "提示", getString(R.string.appwarning), new AlertDialogManager.DialogInterface() {
//                @Override
//                public void onPositive() {
//                    XXPermissions.with(LoginFamerServer.this)
//                            //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
//                            .permission(Permission.Group.LOCATION) //不指定权限则自动获取清单中的危险权限
//                            .permission(Permission.READ_PHONE_STATE)
//                            .request(new OnPermission() {
//                                @Override
//                                public void hasPermission(List<String> granted, boolean isAll) {
//                                    if (isAll) {
//                                        // PreferencesUtils.saveBooleanValue("isallow", true, WelcomeActivity.this);
//                                        // toastUtils.showLong(AppConfig.getAppContext(), "获取权限成功");
//                                        if (Build.VERSION.SDK_INT > 9) {
//                                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//                                            StrictMode.setThreadPolicy(policy);
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void noPermission(List<String> denied, boolean quick) {
//                                    if (quick) {
//                                        Toast.makeText(InnApplication.getAppContext(), "被永久拒绝授权，请手动授予权限", Toast.LENGTH_SHORT).show();
//                                        //如果是被永久拒绝就跳转到应用权限系统设置页面
//                                        XXPermissions.gotoPermissionSettings(InnApplication.getAppContext());
//                                        finish();
//                                    } else {
//                                        Toast.makeText(InnApplication.getAppContext(), "获取权限失败", Toast.LENGTH_SHORT).show();
//                                        AppManager.getAppManager().AppExit(LoginFamerServer.this);
//                                    }
//                                }
//                            });
//
//                }
//
//                @Override
//                public void onNegative() {
//                    finish();
//                }
//            });
//        } else
        {
            //根据保存的标记判断是否登录
            if (PreferencesUtils.getBooleanValue(Constants.ISLOGIN, AppConfig.getAppContext())) {
                String type = PreferencesUtils.getStringValue(Constants.companyfleg, AppConfig.getAppContext());
                if (type.equals("1")) {
                    goToActivity(CompanyActivity.class, null);
                    stopSelf();
                } else if (type.equals("2")) {
                    goToActivity(SelectFunctionActivity_new.class, null);
                    stopSelf();
                }
            }
        }
        if (!HttpUtils.isOfficialHost())
            Toast.makeText(LoginFamerServer.this, ShareUtils.getHost("host"), Toast.LENGTH_LONG).show();
//        ShareUtils.setUpGlobalHost(LoginFamerServer.this, passTv);
    }


    /**
     * 登录
     *
     * @param musername
     * @param muserpass
     */
    private void getDataFromNet(String musername, String muserpass) {
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put(Constants.account, musername);
        mapbody.put(Constants.password, muserpass);
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.LOGINURLNEW, mapbody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i("LoginFamerActivity", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginFamerServer.this, "登录失败，请检查网络后重试。", Toast.LENGTH_SHORT).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, LoginFamerServer.class.getSimpleName());
                stopSelf();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("LoginFamerActivity", string);
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                AlertDialogManager.showMessageDialog(LoginFamerServer.this, "提示", msg, new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {
                                        stopSelf();
                                    }

                                    @Override
                                    public void onNegative() {
                                        stopSelf();
                                    }
                                });
                            }
                        });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                JSONObject data = null;
                                try {
                                    data = jsonObject.getJSONObject("data");
                                    int type = data.getInt("type");
//                                    int myToken = data.getInt("token");
//                                    PreferencesUtils.saveKeyValue(Constants.token, myToken + "", AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.companyfleg, type + "", AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.username, musername + "", AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.password, muserpass + "", AppConfig.getAppContext());
                                    PreferencesUtils.saveBooleanValue(Constants.ISLOGIN, true, AppConfig.getAppContext());

                                    //1 保险公司  2 猪场企业
                                    if (type == 1) {
                                        JSONObject adminUser = data.getJSONObject("adminUser");
                                        String deptName = adminUser.getString("deptName");
                                        String name = adminUser.getString("name");
                                        int deptId = adminUser.getInt("deptId");
                                        int id = adminUser.getInt("id");
                                        PreferencesUtils.saveKeyValue(Constants.companyuser, name, AppConfig.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.insurecompany, deptName, AppConfig.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.deptId, deptId + "", AppConfig.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.id, id + "", AppConfig.getAppContext());

                                        goToActivity(CompanyActivity.class, null);
//                                        stopSelf();
                                    } else {
                                        JSONObject enUser = data.getJSONObject("enUser");
                                        int enId = enUser.getInt("enId");
                                        int enUserId = enUser.getInt("enUserId");
                                        String enName = enUser.getString("enName");
                                        PreferencesUtils.saveKeyValue(Constants.en_id, enId + "", AppConfig.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.companyname, enName, AppConfig.getAppContext());
                                        PreferencesUtils.saveIntValue(Constants.en_user_id, enUserId, AppConfig.getAppContext());
                                        goToActivity(SelectFunctionActivity_new.class, null);
//                                        stopSelf();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
//                                    stopSelf();
                                } finally {
                                    stopSelf();
                                }

                            }

                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e, LoginFamerServer.class.getSimpleName());
                } finally {
                    stopSelf();
                }


            }
        });

    }

    private boolean hasPermission2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        showProgressDialog(this);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
//            openOverlaySettings();
//        }
//    }
//
//    @TargetApi(Build.VERSION_CODES.M)
//    private void openOverlaySettings() {
//        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
//        try {
//            startActivityForResult(intent, RC_OVERLAY);
//        } catch (ActivityNotFoundException e) {
//            Log.e(TAG, e.getMessage());
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case RC_OVERLAY:
//                final boolean overlayEnabled = Settings.canDrawOverlays(this);
//                // Do something...
//                break;
//        }
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getDataFromNet("15000000001", "123456");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void showProgressDialog(Context activity) {
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("开始处理......");
        mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
        if (positive != null) {
            positive.setVisibility(View.GONE);
        }
        Button negative = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
        if (negative != null) {
            negative.setVisibility(View.GONE);
        }
    }

    public void showDialogError(String s) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View inflate = View.inflate(AppConfig.getAppContext(), R.layout.error_nomal, null);
        TextView error_msg = inflate.findViewById(R.id.error_msg);
        TextView error_sure = inflate.findViewById(R.id.error_sure);
        dialog.setView(inflate);
        error_msg.setText(s);
        AlertDialog dialogcreate = dialog.create();
        dialogcreate.setCanceledOnTouchOutside(false);
        dialogcreate.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialogcreate.show();
        error_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
            }
        });
    }
    /* *跳转*/

    public void goToActivity(Class activity, Bundle bundle) {
        Intent intent = new Intent(this, activity);
        //携带数据
        if (bundle != null && bundle.size() != 0) {
            intent.putExtra("data", bundle);
        }
        startActivity(intent);
    }

}
