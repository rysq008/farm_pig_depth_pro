package com.farm.innovation.biz.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.ResultBean;
import com.farm.innovation.location.AlertDialogManager;
import com.farm.innovation.login.RespObject;
import com.farm.innovation.login.ResponseProcessor;
import com.farm.innovation.login.TokenResp;
import com.farm.innovation.login.Utils;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.update.UploadService;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.OkHttp3Util;
import com.google.gson.Gson;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.innovation.pig.insurance.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.farm.innovation.base.FarmAppConfig.DEPARTMENT_ID;
import static com.farm.innovation.base.FarmAppConfig.IDENTITY_CARD;
import static com.farm.innovation.base.FarmAppConfig.NAME;
import static com.farm.innovation.base.FarmAppConfig.PHONE_NUMBER;
import static com.farm.innovation.base.FarmAppConfig.TOKEY;
import static com.farm.innovation.base.FarmAppConfig.USER_ID;

/**
 * @author 56861
 */
public class LoginFamerAarActivity extends BaseActivity {

    Intent mIntent;

    private boolean isRequest = false;

    @Override
    public void initView() {
        super.initView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_login_aar;
    }

    public void requestPermission() {
        XXPermissions.with(LoginFamerAarActivity.this)
                //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.LOCATION) //不指定权限则自动获取清单中的危险权限
                .permission(Permission.READ_PHONE_STATE)
                .permission(Permission.Group.STORAGE)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            // FarmerPreferencesUtils.saveBooleanValue("isallow", true, WelcomeActivity.this);
                            // toastUtils.showLong(FarmAppConfig.getAppContext(), "获取权限成功");
                            if (Build.VERSION.SDK_INT > 9) {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                            }

                            if (!FarmAppConfig.isNetConnected) {
                                Toast.makeText(LoginFamerAarActivity.this, "断网了，请联网后重试。", Toast.LENGTH_LONG).show();
                                LoginFamerAarActivity.this.finish();
                                return;
                            }
                            getDataFromNet("15000000001", "123456");

                        } else {
                            Toast.makeText(LoginFamerAarActivity.this, "is not all permission", Toast.LENGTH_LONG).show();
                            LoginFamerAarActivity.this.finish();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            Toast.makeText(FarmAppConfig.getApplication(), "被永久拒绝授权，请手动授予权限", Toast.LENGTH_LONG).show();
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(FarmAppConfig.getApplication());
                            LoginFamerAarActivity.this.finish();
                        } else {
                            Toast.makeText(FarmAppConfig.getApplication(), "获取权限失败", Toast.LENGTH_LONG).show();
//                                        AppManager.getAppManager().AppExit(LoginFamerAarActivity.this);
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(FarmAppConfig.getApplication());
                            LoginFamerAarActivity.this.finish();
                        }
                    }
                });
    }

    Map mMapbody = new HashMap();

    @Override
    protected void initData() {
        mIntent = getIntent();
//        mIntent.putExtra(TOKEY, "android_token");
//        mIntent.putExtra(USER_ID, "android_userid3");
//        mIntent.putExtra(PHONE_NUMBER, "19000000003");
//        mIntent.putExtra(NAME, "android_name");
//        mIntent.putExtra(DEPARTMENT_ID, "14079900"/*"android_department"*/);
//        mIntent.putExtra(IDENTITY_CARD, "android_identitry");
        startService(new Intent(this, UploadService.class));
//        if (FarmerPreferencesUtils.getBooleanValue(ISLOGIN, FarmAppConfig.getAppContext())) {
//            String type = FarmerPreferencesUtils.getStringValue(companyfleg, FarmAppConfig.getAppContext());
//            if (type.equals("1")) {
//                goToActivity(CompanyActivity.class, null);
//                LoginFamerAarActivity.this.finish();
//            } else if (type.equals("2")) {
//                goToActivity(SelectFunctionActivity_new.class, null);
//                LoginFamerAarActivity.this.finish();
//            }
//            return;
//        }
        if (mIntent == null) {
            Toast.makeText(this, "请传入intent数据", Toast.LENGTH_LONG).show();
            LoginFamerAarActivity.this.finish();
            return;
        }

        if (!this.isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    LoginFamerAarActivity.this.finish();
                    return;
                }
            }
        }

        if (XXPermissions.isHasPermission(LoginFamerAarActivity.this, Permission.Group.LOCATION,
                Permission.Group.STORAGE,
                new String[]{Permission.READ_PHONE_STATE})) {
            requestPermission();
        } else {
            AlertDialogManager.showMessageDialog(LoginFamerAarActivity.this, "提示", getString(R.string.appwarning), new AlertDialogManager.DialogInterface() {
                @Override
                public void onPositive() {
                    requestPermission();
                }

                @Override
                public void onNegative() {
                    LoginFamerAarActivity.this.finish();
                }
            });
        }
    }

    /**
     * 登录
     *
     * @param musername
     * @param muserpass
     */
    private void getDataFromNet(String musername, String muserpass) {
        if (isRequest) return;
        isRequest = true;
        Map<String, String> mapbody = new HashMap<>();
//        mapbody.put(account, musername);
//        mapbody.put(password, muserpass);
        mMapbody.clear();
        if (TextUtils.isEmpty(mIntent.getStringExtra(TOKEY))) {
            Toast.makeText(this, "请传入token", Toast.LENGTH_LONG).show();
            LoginFamerAarActivity.this.finish();
            return;
        }
        mMapbody.put(TOKEY, mIntent.getStringExtra(TOKEY));

        if (TextUtils.isEmpty(mIntent.getStringExtra(DEPARTMENT_ID))) {
            Toast.makeText(this, "请传入国寿财系统部门id", Toast.LENGTH_LONG).show();
            LoginFamerAarActivity.this.finish();
            return;
        }
        mMapbody.put(DEPARTMENT_ID, mIntent.getStringExtra(DEPARTMENT_ID));//国寿财系统的部门id

        if (TextUtils.isEmpty(mIntent.getStringExtra(USER_ID))) {
            Toast.makeText(this, "请传入用户id", Toast.LENGTH_LONG).show();
            LoginFamerAarActivity.this.finish();
            return;
        }
        mMapbody.put(USER_ID, mIntent.getStringExtra(USER_ID));

        if (TextUtils.isEmpty(mIntent.getStringExtra(NAME))) {
            Toast.makeText(this, "请传入用户名", Toast.LENGTH_LONG).show();
            LoginFamerAarActivity.this.finish();
            return;
        }
        mMapbody.put(NAME, mIntent.getStringExtra(NAME));

        if (TextUtils.isEmpty(mIntent.getStringExtra(PHONE_NUMBER))) {
//            Toast.makeText(this, "请传入电话号码", Toast.LENGTH_LONG).show();
//            LoginFamerAarActivity.this.finish();
//            return;
            String phone = mIntent.getStringExtra(USER_ID);
            mMapbody.put(PHONE_NUMBER, (phone.substring(phone.length() - 11)));
        } else
            mMapbody.put(PHONE_NUMBER, mIntent.getStringExtra(PHONE_NUMBER));

        if (TextUtils.isEmpty(mIntent.getStringExtra(IDENTITY_CARD))) {
//            Toast.makeText(this, "请传入身份证号", Toast.LENGTH_LONG).show();
//            LoginFamerAarActivity.this.finish();
            mMapbody.put(IDENTITY_CARD, "");
        } else
            mMapbody.put(IDENTITY_CARD, mIntent.getStringExtra(IDENTITY_CARD));

        mapbody.putAll(mMapbody);
//        mapbody.put(TOKEY, mIntent.getStringExtra(TOKEY));
//        mapbody.put(DEPARTMENT_ID, mIntent.getStringExtra(DEPARTMENT_ID));//国寿财系统的部门id
//        mapbody.put(USER_ID, mIntent.getStringExtra(USER_ID));
//        mapbody.put(NAME, mIntent.getStringExtra(NAME));
//        mapbody.put(PHONE_NUMBER, mIntent.getStringExtra(PHONE_NUMBER));
//        mapbody.put(IDENTITY_CARD, mIntent.getStringExtra(IDENTITY_CARD));
        mProgressDialog.show();
//        String url = "http://192.168.1.175:8081/app/ftnAarLogin";
//        String url = "http://47.92.167.61:8081/nongxian2/app/ftnAarLogin";
        String url = "http://test1.innovationai.cn:8081/nongxian2/app/aarLogin";
        OkHttp3Util.doPost(/*AAR_LOGINURLNEW*/url, mapbody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i("LoginFamerAarActivity", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginFamerAarActivity.this, "登录失败，请检查网络后重试。", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, LoginFamerAarActivity.class.getSimpleName());
                isRequest = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("LoginFamerAarActivity", string);

                Gson gson = new Gson();
                ResultBean resultBean = gson.fromJson(string, ResultBean.class);
                if (resultBean != null) {
                    if (resultBean.getStatus() == 1) {
                        {
                            TokenResp tokenresp = (TokenResp) ResponseProcessor.processResp(string, Utils.LOGIN_GET_TOKEN_URL);
                            if (tokenresp == null || TextUtils.isEmpty(tokenresp.token) || tokenresp.user_status != RespObject.USER_STATUS_1) {
                                Toast.makeText(LoginFamerAarActivity.this, "数据返回异常！", Toast.LENGTH_LONG).show();
                                LoginFamerAarActivity.this.finish();
                                return;
                            }

                            if ((String.valueOf(tokenresp.uid)).equals(FarmerPreferencesUtils.getStringValue(HttpUtils.user_id, LoginFamerAarActivity.this))) {
                                FarmerPreferencesUtils.saveBooleanValue("isone", true, LoginFamerAarActivity.this);
                            } else {
                                FarmerPreferencesUtils.saveBooleanValue("isone", false, LoginFamerAarActivity.this);
                            }

                            //  存储用户信息
                            SharedPreferences userinfo = getApplicationContext().getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = userinfo.edit();
                            editor.putString("token", tokenresp.token);
                            //  int 类型的可能需要修�?
                            //  验证码的有效期，应该在获取验证码的时候返回才�?
                            editor.putInt("tokendate", tokenresp.tokendate);
                            editor.putInt("uid", tokenresp.uid);
                            editor.putString("username", tokenresp.user_username);
                            editor.putString("fullname", tokenresp.user_fullname);
                            editor.putString("codedate", tokenresp.codedate);
                            //用户创建时间
                            editor.putString("createtime", tokenresp.createtime);
                            //  editor.putInt("deptid", tokenresp.deptid);
                            editor.apply();
                            int i = tokenresp.deptid;
                            FarmerPreferencesUtils.saveIntValue(HttpUtils.deptId, tokenresp.deptid, FarmAppConfig.getApplication());
                            FarmerPreferencesUtils.saveKeyValue(HttpUtils.user_id, String.valueOf(tokenresp.uid), FarmAppConfig.getApplication());
                            Log.i("===id==", tokenresp.uid + "");
                        }
                        Intent add_intent = new Intent(LoginFamerAarActivity.this, HomeActivity.class);
                        startActivity(add_intent);
                        LoginFamerAarActivity.this.finish();
                        isRequest = false;
                        return;
                    } else {
//                        mProgressHandler.sendEmptyMessage(44);
                        Toast.makeText(LoginFamerAarActivity.this, resultBean.getMsg(), Toast.LENGTH_SHORT).show();
                        LoginFamerAarActivity.this.finish();
                        isRequest = false;
                        return;
                    }

                } else {
//                    Snackbar.make(nestedScrollView, "服务器错误，请稍后再试！", Snackbar.LENGTH_LONG).setText("服务器错误，请稍后再试！").show();
                    Toast.makeText(LoginFamerAarActivity.this, "服务器错误，请稍后再试！", Toast.LENGTH_SHORT).show();
                    LoginFamerAarActivity.this.finish();
                    isRequest = false;
                    return;
//                    mProgressHandler.sendEmptyMessage(41);
                }
            }
        });
    }

}
