package com.xiangchuang.risks.view;

import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.network_status.NetworkUtil;
import innovation.upload.UploadService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author 56861
 */
public class LoginFamerActivity extends BaseActivity {

    Intent mIntent;
    boolean isRequest = false;

    @Override
    public void initView() {
        super.initView();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login_famer;
    }

    public void requestPermission() {
        XXPermissions.with(LoginFamerActivity.this)
                //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.LOCATION) //不指定权限则自动获取清单中的危险权限
                .permission(Permission.READ_PHONE_STATE)
                .permission(Permission.Group.STORAGE)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            // PreferencesUtils.saveBooleanValue("isallow", true, WelcomeActivity.this);
                            // toastUtils.showLong(AppConfig.getAppContext(), "获取权限成功");
                            if (Build.VERSION.SDK_INT > 9) {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                            }

                            if (!NetworkUtil.isNetworkConnect(LoginFamerActivity.this)) {
                                Toast.makeText(LoginFamerActivity.this, "断网了，请联网后重试。", Toast.LENGTH_LONG).show();
                                LoginFamerActivity.this.finish();
                                return;
                            }
                            getDataFromNet("15000000001", "123456");

                        } else {
                            Toast.makeText(LoginFamerActivity.this, "is not all permission", Toast.LENGTH_LONG).show();
                            LoginFamerActivity.this.finish();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            Toast.makeText(AppConfig.getAppContext(), "被永久拒绝授权，请手动授予权限", Toast.LENGTH_LONG).show();
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(AppConfig.getAppContext());
                            LoginFamerActivity.this.finish();
                        } else {
                            Toast.makeText(AppConfig.getAppContext(), "获取权限失败", Toast.LENGTH_LONG).show();
//                                        AppManager.getAppManager().AppExit(LoginFamerActivity.this);
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(AppConfig.getAppContext());
                            LoginFamerActivity.this.finish();
                        }
                    }
                });
    }

    Map mMapbody = new HashMap();

    @Override
    protected void initData() {
        mIntent = getIntent();
        mIntent.putExtra(Constants.TOKEY, "android_token");
//        mIntent.putExtra(Constants.USER_ID, "android_userid3");
//        mIntent.putExtra(Constants.PHONE_NUMBER, "19000000003");
//        mIntent.putExtra(Constants.NAME, "android_name");
//        mIntent.putExtra(Constants.DEPARTMENT_ID, "14079900"/*"android_department"*/);
//        mIntent.putExtra(Constants.IDENTITY_CARD, "android_identitry");
        startService(new Intent(this, UploadService.class));
//        if (PreferencesUtils.getBooleanValue(Constants.ISLOGIN, AppConfig.getAppContext())) {
//            String type = PreferencesUtils.getStringValue(Constants.companyfleg, AppConfig.getAppContext());
//            if (type.equals("1")) {
//                goToActivity(CompanyActivity.class, null);
//                LoginFamerActivity.this.finish();
//            } else if (type.equals("2")) {
//                goToActivity(SelectFunctionActivity_new.class, null);
//                LoginFamerActivity.this.finish();
//            }
//            return;
//        }
        if (mIntent == null) {
            Toast.makeText(this, "请传入intent数据", Toast.LENGTH_LONG).show();
            LoginFamerActivity.this.finish();
            return;
        }

        if (!this.isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    LoginFamerActivity.this.finish();
                    return;
                }
            }
        }

        if (XXPermissions.isHasPermission(LoginFamerActivity.this, Permission.Group.LOCATION,
                Permission.Group.STORAGE,
                new String[]{Permission.READ_PHONE_STATE})) {
            requestPermission();
        } else {
            AlertDialogManager.showMessageDialog(LoginFamerActivity.this, "提示", getString(R.string.appwarning), new AlertDialogManager.DialogInterface() {
                @Override
                public void onPositive() {
                    requestPermission();
                }

                @Override
                public void onNegative() {
                    LoginFamerActivity.this.finish();
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
        mapbody.put(Constants.account, musername);
        mapbody.put(Constants.password, muserpass);
        mMapbody.clear();
        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.TOKEY))) {
            Toast.makeText(this, "请传入token", Toast.LENGTH_LONG).show();
            LoginFamerActivity.this.finish();
            return;
        }
        mMapbody.put(Constants.TOKEY, mIntent.getStringExtra(Constants.TOKEY));

        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.DEPARTMENT_ID))) {
            Toast.makeText(this, "请传入国寿财系统部门id", Toast.LENGTH_LONG).show();
            LoginFamerActivity.this.finish();
            return;
        }
        mMapbody.put(Constants.DEPARTMENT_ID, mIntent.getStringExtra(Constants.DEPARTMENT_ID));//国寿财系统的部门id

        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.USER_ID))) {
            Toast.makeText(this, "请传入用户id", Toast.LENGTH_LONG).show();
            LoginFamerActivity.this.finish();
            return;
        }
        mMapbody.put(Constants.USER_ID, mIntent.getStringExtra(Constants.USER_ID));

        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.NAME))) {
            Toast.makeText(this, "请传入用户名", Toast.LENGTH_LONG).show();
            LoginFamerActivity.this.finish();
            return;
        }
        mMapbody.put(Constants.NAME, mIntent.getStringExtra(Constants.NAME));

        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.PHONE_NUMBER))) {
//            Toast.makeText(this, "请传入电话号码", Toast.LENGTH_LONG).show();
//            LoginFamerActivity.this.finish();
//            return;
            String phone = mIntent.getStringExtra(Constants.USER_ID);
            mMapbody.put(Constants.PHONE_NUMBER, (phone.substring(phone.length() - 11)));
        } else
            mMapbody.put(Constants.PHONE_NUMBER, mIntent.getStringExtra(Constants.PHONE_NUMBER));

        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.IDENTITY_CARD))) {
//            Toast.makeText(this, "请传入身份证号", Toast.LENGTH_LONG).show();
//            LoginFamerActivity.this.finish();
            mMapbody.put(Constants.IDENTITY_CARD, "");
        } else
            mMapbody.put(Constants.IDENTITY_CARD, mIntent.getStringExtra(Constants.IDENTITY_CARD));

        mapbody.putAll(mMapbody);
//        mapbody.put(Constants.TOKEY, mIntent.getStringExtra(Constants.TOKEY));
//        mapbody.put(Constants.DEPARTMENT_ID, mIntent.getStringExtra(Constants.DEPARTMENT_ID));//国寿财系统的部门id
//        mapbody.put(Constants.USER_ID, mIntent.getStringExtra(Constants.USER_ID));
//        mapbody.put(Constants.NAME, mIntent.getStringExtra(Constants.NAME));
//        mapbody.put(Constants.PHONE_NUMBER, mIntent.getStringExtra(Constants.PHONE_NUMBER));
//        mapbody.put(Constants.IDENTITY_CARD, mIntent.getStringExtra(Constants.IDENTITY_CARD));
        mProgressDialog.show();
//        String url = "http://192.168.1.175:8081/app/ftnAarLogin";
        String url = "http://47.92.167.61:8081/nongxian2/app/ftnAarLogin";
        OkHttp3Util.doPost(/*Constants.AAR_LOGINURLNEW*/url, mapbody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i("LoginFamerActivity", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginFamerActivity.this, "登录失败，请检查网络后重试。", Toast.LENGTH_LONG).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, LoginFamerActivity.class.getSimpleName());
                isRequest = false;
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
                                AlertDialogManager.showMessageDialog(LoginFamerActivity.this, "提示", msg, new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {
                                        LoginFamerActivity.this.finish();
                                    }

                                    @Override
                                    public void onNegative() {
                                        LoginFamerActivity.this.finish();
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
                                        LoginFamerActivity.this.finish();
                                    } else {
                                        JSONObject enUser = data.getJSONObject("enUser");
                                        int enId = enUser.getInt("enId");
                                        int enUserId = enUser.getInt("enUserId");
                                        String enName = enUser.getString("enName");
                                        PreferencesUtils.saveKeyValue(Constants.en_id, enId + "", AppConfig.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.companyname, enName, AppConfig.getAppContext());
                                        PreferencesUtils.saveIntValue(Constants.en_user_id, enUserId, AppConfig.getAppContext());
                                        goToActivity(SelectFunctionActivity_new.class, null);
                                        LoginFamerActivity.this.finish();
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
                } finally {
                    isRequest = false;
                }
            }
        });
    }

}
