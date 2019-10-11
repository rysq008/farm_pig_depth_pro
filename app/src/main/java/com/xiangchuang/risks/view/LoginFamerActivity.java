package com.xiangchuang.risks.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.innovation.pig.insurance.R;
import com.orhanobut.logger.Logger;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.GSCPigBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.AppManager;
import com.xiangchuangtec.luolu.animalcounter.AppConfig;
import com.xiangchuangtec.luolu.animalcounter.JPushStatsConfig;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.entry.InnApplication;
import innovation.network_status.NetworkUtil;
import innovation.utils.PigInnovationAiOpen;
import innovation.utils.Toast;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author 56861
 */
public class LoginFamerActivity extends BaseActivity implements View.OnClickListener {

    Button mloginfamerlogin;
    EditText mloginfameruserid;
    EditText mloginfamerpass;
    ImageView passshow;
    ImageView passhide, ivPass;
    LinearLayout ll_pass_hide, ll_pass_show;
    private static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login_famer;
    }

    @Override
    public void initView() {
        super.initView();
        mloginfamerlogin = findViewById(R.id.loginfamer_login);
        mloginfameruserid = findViewById(R.id.loginfamer_userid);
        mloginfamerpass = findViewById(R.id.loginfamer_pass);
        passshow = findViewById(R.id.pass_show);
//        ivPass = findViewById(R.id.iv_pass);
        passhide = findViewById(R.id.pass_hide);
//        ll_pass_hide = findViewById(R.id.ll_pass_hide);
//        ll_pass_show = findViewById(R.id.ll_pass_show);


        mloginfamerlogin.setOnClickListener(this);
        ll_pass_hide.setOnClickListener(this);
        ll_pass_show.setOnClickListener(this);
    }

    @Override
    protected void initData() {

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mActivity.startForegroundService(new Intent(mActivity, UploadService.class));
//        } else {
//            mActivity.startService(new Intent(mActivity, UploadService.class));
//        }
//
//        // 避免从桌面启动程序后，会重新实例化入口类的activity
//        // 判断当前activity是不是所在任务栈的根
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
//
//        if (!hasPermission2()) {
//            AlertDialogManager.showMessageDialog(LoginFamerActivity.this, "提示", getString(R.string.appwarning), new AlertDialogManager.DialogInterface() {
//                @Override
//                public void onPositive() {
//                    XXPermissions.with(LoginFamerActivity.this)
//                            //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
//                            .permission(Permission.Group.LOCATION) //不指定权限则自动获取清单中的危险权限
//                            .permission(Permission.READ_PHONE_STATE)
//                            .permission(Permission.CAMERA)
//                            .request(new OnPermission() {
//                                @Override
//                                public void hasPermission(List<String> granted, boolean isAll) {
//                                    if (isAll) {
//                                        // PreferencesUtils.saveBooleanValue("isallow", true, WelcomeActivity.this);
//
//                                        if (android.os.Build.VERSION.SDK_INT > 9) {
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
//                                        if (LoginFamerActivity.this != null) {
//                                            Toast.makeText(LoginFamerActivity.this, "获取权限失败", Toast.LENGTH_SHORT).show();
//                                            AppManager.getAppManager().AppExit(LoginFamerActivity.this);
//                                        } else {
//                                            finish();
//                                        }
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
//        } else {
//            //根据保存的标记判断是否登录
//            if (PreferencesUtils.getBooleanValue(Constants.ISLOGIN, AppConfig.getAppContext())) {
////                String type = PreferencesUtils.getStringValue(Constants.companyfleg, AppConfig.getAppContext());
////                if (type.equals("1")) {
////                    goToActivity(CompanyActivity.class, null);
////                    finish();
////                } else if (type.equals("2")) {
////                    goToActivity(SelectFunctionActivity_new.class, null);
////                    finish();
////                }
//
//            }
//        }
//        if (!HttpUtils.isOfficialHost()) {
//            Toast.makeText(LoginFamerActivity.this, ShareUtils.getHost("host"), Toast.LENGTH_LONG).show();
//        }
//        ShareUtils.setUpGlobalHost(LoginFamerActivity.this, ivPass);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.loginfamer_login) {

            if (true) {
                PigInnovationAiOpen.getInstance().requestInnovationApi(LoginFamerActivity.this, "98765432101", "89979dc663caa2580164f88b57796251",
                        "14112100", "文水县支公司", "3", "14119900", "总公司/山西分公司/吕梁市中心支公司/文水县支公司",
                        "00000000,14000000,14119900,", "文水县养殖场", PigInnovationAiOpen.INSURE, "test", new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message msg) {
                                List<GSCPigBean> beanS = (List<GSCPigBean>) msg.obj;
                                for (GSCPigBean bean : beanS) {
                                    Log.d("aaaaaa", "handleMessage: ---->" + bean.string());
                                }
                                if (msg.what == PigInnovationAiOpen.INSURE) {
                                    Toast.makeText(LoginFamerActivity.this, "投保返回", Toast.LENGTH_LONG).show();
                                }

                                if (msg.what == PigInnovationAiOpen.PAY) {

                                    List<GSCPigBean> beans = (List<GSCPigBean>) msg.obj;
                                    Toast.makeText(LoginFamerActivity.this, "理赔返回", Toast.LENGTH_LONG).show();
                                }

                                return true;
                            }
                        });
                return;
            }
            if (!NetworkUtil.isNetworkConnect(LoginFamerActivity.this)) {
                Toast.makeText(this, "断网了，请联网后重试。", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ActivityCompat.checkSelfPermission(AppConfig.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                XXPermissions.with(LoginFamerActivity.this)
                        //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                        .permission(Permission.READ_PHONE_STATE)
                        .request(new OnPermission() {
                            @Override
                            public void hasPermission(List<String> granted, boolean isAll) {
                                if (isAll) {
                                    // PreferencesUtils.saveBooleanValue("isallow", true, WelcomeActivity.this);

                                    if (Build.VERSION.SDK_INT > 9) {
                                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                        StrictMode.setThreadPolicy(policy);
                                    }
                                }
                            }

                            @Override
                            public void noPermission(List<String> denied, boolean quick) {
                                if (quick) {
                                    Toast.makeText(InnApplication.getAppContext(), "被永久拒绝授权，请手动授予权限", Toast.LENGTH_SHORT).show();
                                    //如果是被永久拒绝就跳转到应用权限系统设置页面
                                    XXPermissions.gotoPermissionSettings(InnApplication.getAppContext());
                                    finish();
                                } else {
                                    Toast.makeText(InnApplication.getAppContext(), "获取权限失败", Toast.LENGTH_SHORT).show();
                                    AppManager.getAppManager().AppExit(LoginFamerActivity.this);
                                }
                            }
                        });
                return;
            }
            String musername = mloginfameruserid.getText().toString();
            String muserpass = mloginfamerpass.getText().toString();
            if (musername.length() < 6 || musername.length() > 20) {
                Toast.makeText(AppConfig.getAppContext(), "账号长度不正确，应为6-20位字符", Toast.LENGTH_LONG).show();
                return;
            }
            if (!TextUtils.isEmpty(musername) && !TextUtils.isEmpty(muserpass)) {
                getDataFromNet(musername, muserpass);
            } else {
                Toast.makeText(AppConfig.getAppContext(), "账号或者密码为空", Toast.LENGTH_LONG).show();
            }
        }
//        else if (i == R.id.ll_pass_hide) {
//            mloginfamerpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//            passshow.setVisibility(View.VISIBLE);
//            passhide.setVisibility(View.GONE);
//        } else if (i == R.id.ll_pass_show) {
//            mloginfamerpass.setInputType(InputType.TYPE_CLASS_TEXT);
//            passhide.setVisibility(View.VISIBLE);
//            passshow.setVisibility(View.GONE);
//        }

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
                Logger.i(e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginFamerActivity.this, "登录失败，请检查网络后重试。", Toast.LENGTH_SHORT).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, LoginFamerActivity.class.getSimpleName());
                JPushStatsConfig.onLoginEvent(LoginFamerActivity.this, "登录", false, JPushStatsConfig.makeMapData(e.toString(), mapbody));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Logger.i(string);
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                AlertDialogManager.showMessageDialog(LoginFamerActivity.this, "提示", msg, new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {

                                    }

                                    @Override
                                    public void onNegative() {

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
                                    String token = data.getString("token");
//                                    int myToken = data.getInt("token");
//                                    PreferencesUtils.saveKeyValue(Constants.token, myToken + "", AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.companyfleg, type + "", AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.username, musername + "", AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.password, muserpass + "", AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.TOKEN, token, AppConfig.getAppContext());
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
                                        if (PreferencesUtils.saveKeyValueForRes(Constants.id, id + "", AppConfig.getAppContext())) {
                                            goToActivity(CompanyActivity.class, null);
                                            finish();
                                        } else {
                                            ToastUtils.show("登录失败，请重试。");
                                        }
                                    } else {
                                        JSONObject enUser = data.getJSONObject("enUser");
                                        int enId = enUser.getInt("enId");
                                        int enUserId = enUser.getInt("enUserId");
                                        String enName = enUser.getString("enName");

                                        PreferencesUtils.saveKeyValue(Constants.companyname, enName, AppConfig.getAppContext());
                                        if (PreferencesUtils.saveKeyValueForRes(Constants.en_id, enId + "", AppConfig.getAppContext())
                                                && PreferencesUtils.saveIntValueForRes(Constants.en_user_id, enUserId, AppConfig.getAppContext())) {
                                            goToActivity(SelectFunctionActivity_new.class, null);
                                            finish();
                                        } else {
                                            ToastUtils.show("登录失败，请重试。");
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e, LoginFamerActivity.class.getSimpleName());
                }
                JPushStatsConfig.onLoginEvent(LoginFamerActivity.this, "登录", true, JPushStatsConfig.makeMapData(string, mapbody));

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

}
