package com.xiangchuang.risks.view;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.ResultBean;
import com.farm.innovation.login.RespObject;
import com.farm.innovation.login.ResponseProcessor;
import com.farm.innovation.login.TokenResp;
import com.farm.innovation.login.Utils;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.utils.ConstUtils;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.HttpUtils;
import com.google.gson.Gson;
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

import static com.farm.innovation.base.FarmAppConfig.DEPARTMENT_ID;
import static com.farm.innovation.base.FarmAppConfig.IDENTITY_CARD;
import static com.farm.innovation.base.FarmAppConfig.NAME;
import static com.farm.innovation.base.FarmAppConfig.PHONE_NUMBER;
import static com.farm.innovation.base.FarmAppConfig.TOKEY;
import static com.farm.innovation.base.FarmAppConfig.USER_ID;

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
                            // toastUtils.showLong(AppConfig.getAppContext(), "获取权限成功");
                            if (Build.VERSION.SDK_INT > 9) {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                            }

                            if (!NetworkUtil.isNetworkConnect(LoginPigAarActivity.this)) {
                                Toast.makeText(LoginPigAarActivity.this, "断网了，请联网后重试。", Toast.LENGTH_LONG).show();
                                LoginPigAarActivity.this.finish();
                                return;
                            }
                            showTypeDialog();
//                            getDataFromNet("15000000001", "123456");

                        } else {
                            Toast.makeText(LoginPigAarActivity.this, "is not all permission", Toast.LENGTH_LONG).show();
                            LoginPigAarActivity.this.finish();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            Toast.makeText(AppConfig.getAppContext(), "被永久拒绝授权，请手动授予权限", Toast.LENGTH_LONG).show();
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(AppConfig.getAppContext());
                            LoginPigAarActivity.this.finish();
                        } else {
                            Toast.makeText(AppConfig.getAppContext(), "获取权限失败", Toast.LENGTH_LONG).show();
//                                        AppManager.getAppManager().AppExit(LoginPigAarActivity.this);
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(AppConfig.getAppContext());
                            LoginPigAarActivity.this.finish();
                        }
                    }
                });
    }

    Map mMapbody = new HashMap();

    @Override
    protected void initData() {
        mIntent = getIntent();
//        mIntent.putExtra(Constants.TOKEY, "android_token");
//        mIntent.putExtra(Constants.USER_ID, "android_userid5");
//        mIntent.putExtra(Constants.PHONE_NUMBER, "15000000001");
//        mIntent.putExtra(Constants.NAME, "android_name");
//        mIntent.putExtra(Constants.DEPARTMENT_ID, "28"/*"android_department"*/);
//        mIntent.putExtra(Constants.IDENTITY_CARD, "android_identitry");
        startService(new Intent(this, UploadService.class));
//        if (FarmerPreferencesUtils.getBooleanValue(Constants.ISLOGIN, AppConfig.getAppContext())) {
//            String type = FarmerPreferencesUtils.getStringValue(Constants.companyfleg, AppConfig.getAppContext());
//            if (type.equals("1")) {
//                goToActivity(CompanyActivity.class, null);
//                LoginPigAarActivity.this.finish();
//            } else if (type.equals("2")) {
//                goToActivity(SelectFunctionActivity_new.class, null);
//                LoginPigAarActivity.this.finish();
//            }
//            return;
//        }
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
            LoginPigAarActivity.this.finish();
            return;
        }
        mMapbody.put(Constants.TOKEY, mIntent.getStringExtra(Constants.TOKEY));

        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.DEPARTMENT_ID))) {
            Toast.makeText(this, "请传入国寿财系统部门id", Toast.LENGTH_LONG).show();
            LoginPigAarActivity.this.finish();
            return;
        }
        mMapbody.put(Constants.DEPARTMENT_ID, mIntent.getStringExtra(Constants.DEPARTMENT_ID));//国寿财系统的部门id

        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.USER_ID))) {
            Toast.makeText(this, "请传入用户id", Toast.LENGTH_LONG).show();
            LoginPigAarActivity.this.finish();
            return;
        }
        mMapbody.put(Constants.USER_ID, mIntent.getStringExtra(Constants.USER_ID));

        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.NAME))) {
            Toast.makeText(this, "请传入用户名", Toast.LENGTH_LONG).show();
            LoginPigAarActivity.this.finish();
            return;
        }
        mMapbody.put(Constants.NAME, mIntent.getStringExtra(Constants.NAME));

        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.PHONE_NUMBER))) {
//            Toast.makeText(this, "请传入电话号码", Toast.LENGTH_LONG).show();
//            LoginPigAarActivity.this.finish();
//            return;
            String phone = mIntent.getStringExtra(Constants.USER_ID);
            mMapbody.put(Constants.PHONE_NUMBER, (phone.substring(phone.length() - 11)));
        } else
            mMapbody.put(Constants.PHONE_NUMBER, mIntent.getStringExtra(Constants.PHONE_NUMBER));

        if (TextUtils.isEmpty(mIntent.getStringExtra(Constants.IDENTITY_CARD))) {
//            Toast.makeText(this, "请传入身份证号", Toast.LENGTH_LONG).show();
//            LoginPigAarActivity.this.finish();
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
                Log.i("LoginPigAarActivity", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginPigAarActivity.this, "登录失败，请检查网络后重试。", Toast.LENGTH_LONG).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, LoginPigAarActivity.class.getSimpleName());
                isRequest = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("LoginPigAarActivity", string);
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                AlertDialogManager.showMessageDialog(LoginPigAarActivity.this, "提示", msg, new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {
                                        LoginPigAarActivity.this.finish();
                                    }

                                    @Override
                                    public void onNegative() {
                                        LoginPigAarActivity.this.finish();
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
//                                    FarmerPreferencesUtils.saveKeyValue(Constants.token, myToken + "", AppConfig.getAppContext());
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
                                        LoginPigAarActivity.this.finish();
                                    } else {
                                        JSONObject enUser = data.getJSONObject("enUser");
                                        int enId = enUser.getInt("enId");
                                        int enUserId = enUser.getInt("enUserId");
                                        String enName = enUser.getString("enName");
                                        PreferencesUtils.saveKeyValue(Constants.en_id, enId + "", AppConfig.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.companyname, enName, AppConfig.getAppContext());
                                        PreferencesUtils.saveIntValue(Constants.en_user_id, enUserId, AppConfig.getAppContext());
                                        goToActivity(SelectFunctionActivity_new.class, null);
                                        LoginPigAarActivity.this.finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e, LoginPigAarActivity.class.getSimpleName());
                } finally {
                    isRequest = false;
                }
            }
        });
    }

    /**
     * 选择险种dialog
     */
    private void showTypeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginPigAarActivity.this);
        LayoutInflater inflater = LayoutInflater.from(LoginPigAarActivity.this);
        View v = inflater.inflate(R.layout.select_dialog_layout, null);
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(v);

        TextView famer = v.findViewById(R.id.tv_famer_select);
        TextView donkey = v.findViewById(R.id.tv_donkey_select);
        TextView yak = v.findViewById(R.id.tv_yak_select);
        TextView pig = v.findViewById(R.id.tv_pig_select);
        ImageView close = v.findViewById(R.id.iv_close);

        famer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("famer", "onClick: famer");
                FarmerPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_CATTLE, LoginPigAarActivity.this);
                getDataFarmFromNet("","");
            }
        });

        donkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FarmerPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_DONKEY, LoginPigAarActivity.this);
                getDataFarmFromNet("","");
            }
        });

        yak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FarmerPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_YAK, LoginPigAarActivity.this);
                getDataFarmFromNet("","");
            }
        });

        pig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataFromNet("","");
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                LoginPigAarActivity.this.finish();
        }
        });

        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
    }


    private void getDataFarmFromNet(String musername, String muserpass) {
        if (isRequest) return;
        isRequest = true;
        Map<String, String> mapbody = new HashMap<>();
//        mapbody.put(account, musername);
//        mapbody.put(password, muserpass);
        mMapbody.clear();
        if (TextUtils.isEmpty(mIntent.getStringExtra(TOKEY))) {
            Toast.makeText(this, "请传入token", Toast.LENGTH_LONG).show();
            LoginPigAarActivity.this.finish();
            return;
        }
        mMapbody.put(TOKEY, mIntent.getStringExtra(TOKEY));

        if (TextUtils.isEmpty(mIntent.getStringExtra(DEPARTMENT_ID))) {
            Toast.makeText(this, "请传入国寿财系统部门id", Toast.LENGTH_LONG).show();
            LoginPigAarActivity.this.finish();
            return;
        }
        mMapbody.put(DEPARTMENT_ID, mIntent.getStringExtra(DEPARTMENT_ID));//国寿财系统的部门id

        if (TextUtils.isEmpty(mIntent.getStringExtra(USER_ID))) {
            Toast.makeText(this, "请传入用户id", Toast.LENGTH_LONG).show();
            LoginPigAarActivity.this.finish();
            return;
        }
        mMapbody.put(USER_ID, mIntent.getStringExtra(USER_ID));

        if (TextUtils.isEmpty(mIntent.getStringExtra(NAME))) {
            Toast.makeText(this, "请传入用户名", Toast.LENGTH_LONG).show();
            LoginPigAarActivity.this.finish();
            return;
        }
        mMapbody.put(NAME, mIntent.getStringExtra(NAME));

        if (TextUtils.isEmpty(mIntent.getStringExtra(PHONE_NUMBER))) {
//            Toast.makeText(this, "请传入电话号码", Toast.LENGTH_LONG).show();
//            LoginPigAarActivity.this.finish();
//            return;
            String phone = mIntent.getStringExtra(USER_ID);
            mMapbody.put(PHONE_NUMBER, (phone.substring(phone.length() - 11)));
        } else
            mMapbody.put(PHONE_NUMBER, mIntent.getStringExtra(PHONE_NUMBER));

        if (TextUtils.isEmpty(mIntent.getStringExtra(IDENTITY_CARD))) {
//            Toast.makeText(this, "请传入身份证号", Toast.LENGTH_LONG).show();
//            LoginPigAarActivity.this.finish();
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
        String url = "http://47.92.167.61:8081/nongxian2/app/aarLogin";
        com.farm.innovation.utils.OkHttp3Util.doPost(/*AAR_LOGINURLNEW*/url, mapbody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i("LoginPigAarActivity", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginPigAarActivity.this, "登录失败，请检查网络后重试。", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                });
                com.farm.innovation.utils.AVOSCloudUtils.saveErrorMessage(e, LoginPigAarActivity.class.getSimpleName());
                isRequest = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("LoginPigAarActivity", string);

                Gson gson = new Gson();
                ResultBean resultBean = gson.fromJson(string, ResultBean.class);
                if (resultBean != null) {
                    if (resultBean.getStatus() == 1) {
                        {
                            TokenResp tokenresp = (TokenResp) ResponseProcessor.processResp(string, Utils.LOGIN_GET_TOKEN_URL);
                            if (tokenresp == null || TextUtils.isEmpty(tokenresp.token) || tokenresp.user_status != RespObject.USER_STATUS_1) {
                                Toast.makeText(LoginPigAarActivity.this, "数据返回异常！", Toast.LENGTH_LONG).show();
                                LoginPigAarActivity.this.finish();
                                return;
                            }

                            if ((String.valueOf(tokenresp.uid)).equals(FarmerPreferencesUtils.getStringValue(HttpUtils.user_id, LoginPigAarActivity.this))) {
                                FarmerPreferencesUtils.saveBooleanValue("isone", true, LoginPigAarActivity.this);
                            } else {
                                FarmerPreferencesUtils.saveBooleanValue("isone", false, LoginPigAarActivity.this);
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
                        Intent add_intent = new Intent(LoginPigAarActivity.this, HomeActivity.class);
                        startActivity(add_intent);
                        LoginPigAarActivity.this.finish();
                        isRequest = false;
                        return;
                    } else {
//                        mProgressHandler.sendEmptyMessage(44);
                        Toast.makeText(LoginPigAarActivity.this, resultBean.getMsg(), Toast.LENGTH_SHORT).show();
                        LoginPigAarActivity.this.finish();
                        isRequest = false;
                        return;
                    }

                } else {
//                    Snackbar.make(nestedScrollView, "服务器错误，请稍后再试！", Snackbar.LENGTH_LONG).setText("服务器错误，请稍后再试！").show();
                    Toast.makeText(LoginPigAarActivity.this, "服务器错误，请稍后再试！", Toast.LENGTH_SHORT).show();
                    LoginPigAarActivity.this.finish();
                    isRequest = false;
                    return;
//                    mProgressHandler.sendEmptyMessage(41);
                }
            }
        });
    }


}
