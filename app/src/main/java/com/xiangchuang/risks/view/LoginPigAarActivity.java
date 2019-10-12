package com.xiangchuang.risks.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.farm.innovation.bean.GscLoginBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.utils.PigPreferencesUtils;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.BaseBean;
import com.xiangchuang.risks.model.bean.GscFarmInfoBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuangtec.luolu.animalcounter.PigAppConfig;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.login.Utils;
import innovation.network_status.NetworkUtil;
import innovation.upload.UploadService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.FARM_NAME;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.ID_CARD;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.OFFICE_CODE;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.OFFICE_LEVEL;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.OFFICE_NAME;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.PARENT_CODE;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.PARENT_OFFICE_CODES;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.PARENT_OFFICE_NAMES;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.PHONE;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.TASK_ID;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.TOKEY;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.TYPE;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.USER_ID;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.USER_NAME;
import static innovation.utils.HttpUtils.GSC_PIG_AAR_FARM_INFO;
import static innovation.utils.HttpUtils.GSC_PIG_AAR_LOGINURLNEW;

/**
 * @author 56861
 */
public class LoginPigAarActivity extends BaseActivity {

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
        XXPermissions.with(LoginPigAarActivity.this)
                //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.LOCATION) //不指定权限则自动获取清单中的危险权限
                .permission(Permission.READ_PHONE_STATE)
                .permission(Permission.Group.STORAGE)
                .permission(Permission.CAMERA)
                .permission(Permission.RECORD_AUDIO)
                .permission(Permission.READ_CONTACTS)
                .permission(Manifest.permission.INTERNET)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            // FarmerPreferencesUtils.saveBooleanValue("isallow", true, WelcomeActivity.this);
                            // toastUtils.showLong(PigAppConfig.getAppContext(), "获取权限成功");
                            if (Build.VERSION.SDK_INT > 9) {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                            }

                            if (!NetworkUtil.isNetworkConnect(LoginPigAarActivity.this)) {
                                Toast.makeText(LoginPigAarActivity.this, "断网了，请联网后重试。", Toast.LENGTH_LONG).show();
                                LoginPigAarActivity.this.finish();
                                return;
                            }
//                            showTypeDialog();
//                            PigPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_CATTLE, LoginPigAarActivity.this);
                            getDataFarmFromNet("", "");
//                            getDataFromNet("", "");
                        } else {
                            Toast.makeText(LoginPigAarActivity.this, "is not all permission", Toast.LENGTH_LONG).show();
                            LoginPigAarActivity.this.finish();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            Toast.makeText(PigAppConfig.getAppContext(), "被永久拒绝授权，请手动授予权限", Toast.LENGTH_LONG).show();
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(PigAppConfig.getAppContext());
                            LoginPigAarActivity.this.finish();
                        } else {
                            Toast.makeText(PigAppConfig.getAppContext(), "获取权限失败", Toast.LENGTH_LONG).show();
//                                        AppManager.getAppManager().AppExit(LoginPigAarActivity.this);
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(PigAppConfig.getAppContext());
                            LoginPigAarActivity.this.finish();
                        }
                    }
                });
    }

    Map mMapbody = new HashMap();

    @Override
    protected void initData() {
        mIntent = getIntent();
        startService(new Intent(this, UploadService.class));

        if (mIntent == null) {
            Toast.makeText(this, "请传入intent数据", Toast.LENGTH_LONG).show();
            LoginPigAarActivity.this.finish();
            return;
        }

        if (!this.isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    LoginPigAarActivity.this.finish();
                    return;
                }
            }
        }

        if (XXPermissions.isHasPermission(LoginPigAarActivity.this, Permission.Group.LOCATION,
                Permission.Group.STORAGE,
                new String[]{Permission.READ_PHONE_STATE})) {
            requestPermission();
        } else {
            AlertDialogManager.showMessageDialog(LoginPigAarActivity.this, "提示", getString(R.string.appwarning), new AlertDialogManager.DialogInterface() {
                @Override
                public void onPositive() {
                    requestPermission();
                }

                @Override
                public void onNegative() {
                    LoginPigAarActivity.this.finish();
                }
            });
        }
    }

    private void getDataFarmFromNet(String musername, String muserpass) {
        if (isRequest) {
            return;
        }
        isRequest = true;
        Map<String, String> mapbody = new HashMap<>();
//        mapbody.put(account, musername);
//        mapbody.put(password, muserpass);
        mMapbody.clear();
        {
            showProgressDialog(this);
            mMapbody.put(TOKEY, mIntent.getStringExtra(TOKEY));
            mMapbody.put(TASK_ID, mIntent.getStringExtra(TASK_ID));
            mMapbody.put(USER_ID, mIntent.getStringExtra(USER_ID));
            mMapbody.put(USER_NAME, mIntent.getStringExtra(USER_NAME));
            mMapbody.put(OFFICE_CODE, mIntent.getStringExtra(OFFICE_CODE));
            mMapbody.put(OFFICE_NAME, mIntent.getStringExtra(OFFICE_NAME));
            mMapbody.put(OFFICE_LEVEL, mIntent.getStringExtra(OFFICE_LEVEL));
            mMapbody.put(PARENT_CODE, mIntent.getStringExtra(PARENT_CODE));
            mMapbody.put(PARENT_OFFICE_NAMES, mIntent.getStringExtra(PARENT_OFFICE_NAMES));
            mMapbody.put(PARENT_OFFICE_CODES, mIntent.getStringExtra(PARENT_OFFICE_CODES));
            mMapbody.put(TYPE, mIntent.getStringExtra(TYPE));
            mMapbody.put(FARM_NAME, mIntent.getStringExtra(FARM_NAME));
            mMapbody.put(PHONE, mIntent.getStringExtra(PHONE));
            mMapbody.put(ID_CARD, mIntent.getStringExtra(ID_CARD));
        }
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
        String url = PigAppConfig.PIG_DEPTH_JOIN ? GSC_PIG_AAR_LOGINURLNEW : "http://test1.innovationai.cn:8081/nongxian2/app/aarLogin";

        OkHttp3Util.doPost(url, mapbody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                mProgressDialog.dismiss();
                Log.i("LoginPigAarActivity", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mProgressDialog) {
                            mProgressDialog.dismiss();
                        }
                        Toast.makeText(LoginPigAarActivity.this, "登录失败，请检查网络后重试。", Toast.LENGTH_LONG).show();
                        LoginPigAarActivity.this.finish();
                        return;
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, LoginPigAarActivity.class.getSimpleName());
                isRequest = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("LoginPigAarActivity", string);
                runOnUiThread(() -> {

                    Gson gson = new Gson();
                    if (PigAppConfig.PIG_DEPTH_JOIN) {
                        try {
                            BaseBean<GscLoginBean> gscLoginBean = gson.fromJson(string, new TypeToken<BaseBean<GscLoginBean>>() {
                            }.getType());
                            GscLoginBean tokenresp = gscLoginBean.getData();
                            {
                                if (gscLoginBean.isSuccess()) {
                                    //  存储用户信息
                                    SharedPreferences userinfo = getApplicationContext().getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = userinfo.edit();
                                    editor.putString("token", tokenresp.token);
                                    //  int 类型的可能需要修??
                                    //  验证码的有效期，应该在获取验证码的时候返回才??
                                    editor.putInt("tokendate", tokenresp.tokendate);
                                    editor.putInt("uid", tokenresp.uid);
                                    editor.putString("username", tokenresp.username);
                                    editor.putString("fullname", tokenresp.fullname);
                                    editor.putString("codedate", String.valueOf(tokenresp.codedate));
                                    //用户创建时间
                                    editor.putString("createtime", tokenresp.createtime);
                                    //  editor.putInt("deptid", tokenresp.deptid);
                                    editor.apply();
                                    PigPreferencesUtils.saveIntValue(Constants.deptId, tokenresp.deptid, PigAppConfig.getAppContext());

                                    {
                                        PigPreferencesUtils.saveKeyValue(Constants.TOKEN, tokenresp.token, PigAppConfig.getAppContext());
                                        PigPreferencesUtils.saveKeyValue(Constants.username, musername + "", PigAppConfig.getAppContext());
                                        PigPreferencesUtils.saveKeyValue(Constants.password, muserpass + "", PigAppConfig.getAppContext());
                                        PigPreferencesUtils.saveBooleanValue(Constants.ISLOGIN, true, PigAppConfig.getAppContext());

                                        //1 保险公司  2 猪场企业
                                        PigPreferencesUtils.saveKeyValue(Constants.deptId, tokenresp.deptid + "", PigAppConfig.getAppContext());

                                        //继续请求接口然后跳转TODO
                                        mapbody.clear();
                                        mapbody.put("enName", mIntent.getStringExtra(FARM_NAME));
                                        mapbody.put("deptId", String.valueOf(tokenresp.deptid));
                                        mapbody.put("gsUserid", tokenresp.gsUserId);
                                        OkHttp3Util.doPost(GSC_PIG_AAR_FARM_INFO, mapbody, new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                runOnUiThread(() -> {
                                                    if (null != mProgressDialog) {
                                                        mProgressDialog.dismiss();
                                                    }
                                                    Toast.makeText(LoginPigAarActivity.this, "服务器错误，请稍后再试！", Toast.LENGTH_SHORT).show();
                                                    LoginPigAarActivity.this.finish();
                                                    isRequest = false;
                                                    return;
                                                });
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                String result = response.body().string();
                                                runOnUiThread(() -> {
                                                    if (null != mProgressDialog) {
                                                        mProgressDialog.dismiss();
                                                    }
                                                    BaseBean<GscFarmInfoBean> gscBean = gson.fromJson(result, new TypeToken<BaseBean<GscFarmInfoBean>>() {
                                                    }.getType());
                                                    if (gscBean.isSuccess()) {
                                                        GscFarmInfoBean farmInfoBean = gscBean.getData();

                                                        PigPreferencesUtils.saveKeyValue(Constants.companyfleg, 2 + "", PigAppConfig.getAppContext());
                                                        PigPreferencesUtils.saveKeyValue(Constants.companyname, farmInfoBean.enName, PigAppConfig.getAppContext());
                                                        PigPreferencesUtils.saveKeyValueForRes(Constants.en_id, farmInfoBean.enId + "", PigAppConfig.getAppContext());
                                                        PigPreferencesUtils.saveIntValueForRes(Constants.en_user_id, Integer.parseInt(farmInfoBean.enUserId), PigAppConfig.getAppContext());

                                                        goToActivity(SelectFunctionActivity_new.class, mIntent.getExtras());
                                                        LoginPigAarActivity.this.finish();
                                                        isRequest = false;
                                                        return;
                                                    } else {
                                                        Toast.makeText(LoginPigAarActivity.this, gscBean.getMsg(), Toast.LENGTH_SHORT).show();
                                                        LoginPigAarActivity.this.finish();
                                                        isRequest = false;
                                                        return;
                                                    }
                                                });

                                            }
                                        });
                                    }

                                    isRequest = false;
                                    return;
                                } else {
                                    if (null != mProgressDialog) {
                                        mProgressDialog.dismiss();
                                    }
                                    Toast.makeText(LoginPigAarActivity.this, gscLoginBean.getMsg(), Toast.LENGTH_SHORT).show();
                                    LoginPigAarActivity.this.finish();
                                    isRequest = false;
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            if (null != mProgressDialog) {
                                mProgressDialog.dismiss();
                            }
                            Toast.makeText(LoginPigAarActivity.this, "系统异常，请稍后再试！", Toast.LENGTH_SHORT).show();
                            LoginPigAarActivity.this.finish();
                            isRequest = false;
                            return;
                        }
                    }
                });

            }
        });


    }


}


