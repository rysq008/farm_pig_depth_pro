package com.xiangchuang.risks.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.xiangchuang.risks.utils.AppManager;
import com.xiangchuang.risks.utils.ShareUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.entry.InnApplication;
import innovation.network_status.NetworkUtil;
import innovation.upload.UploadService;
import innovation.utils.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author 56861
 */
public class LoginPigActivity extends BaseActivity {


    Button mloginfamerlogin;

    EditText mloginfameruserid;

    EditText mloginfamerpass;

    ImageView passshow;

    TextView passTv;

    ImageView passhide;
    private static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    @Override
    public void initView() {
        super.initView();
        mloginfamerlogin = (Button) findViewById(R.id.loginfamer_login);
        mloginfameruserid = (EditText) findViewById(R.id.loginfamer_userid);
        mloginfamerpass = (EditText) findViewById(R.id.loginfamer_pass);
        passshow = (ImageView) findViewById(R.id.pass_show);
        passTv = (TextView) findViewById(R.id.pass_tv);
        passhide = (ImageView) findViewById(R.id.pass_hide);
        findViewById(R.id.pass_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.pass_hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.loginfamer_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
//        showProgressDialog(this);
//        mloginfameruserid.setText("15000000001");
//        mloginfamerpass.setText("123456");
//        mloginfamerlogin.performClick();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login_famer;
    }

    @Override
    protected void initData() {

        startService(new Intent(this, UploadService.class));

        if (!this.isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }


        if (!hasPermission2()) {
            AlertDialogManager.showMessageDialog(LoginPigActivity.this, "提示", getString(R.string.appwarning), new AlertDialogManager.DialogInterface() {
                @Override
                public void onPositive() {
                    XXPermissions.with(LoginPigActivity.this)
                            //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                            .permission(Permission.Group.LOCATION) //不指定权限则自动获取清单中的危险权限
                            .permission(Permission.READ_PHONE_STATE)
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
                                        AppManager.getAppManager().AppExit(LoginPigActivity.this);
                                    }
                                }
                            });

                }

                @Override
                public void onNegative() {
                    finish();
                }
            });
        } else {
            //根据保存的标记判断是否登录
            if (PreferencesUtils.getBooleanValue(Constants.ISLOGIN, AppConfig.getAppContext())) {
                String type = PreferencesUtils.getStringValue(Constants.companyfleg, AppConfig.getAppContext());
                if (type.equals("1")) {
                    goToActivity(CompanyActivity.class, null);
                    finish();
                } else if (type.equals("2")) {
                    goToActivity(SelectFunctionActivity_new.class, null);
                    finish();
                }
            }
        }
        if (!HttpUtils.isOfficialHost())
            Toast.makeText(LoginPigActivity.this, ShareUtils.getHost("host"), Toast.LENGTH_LONG).show();
        ShareUtils.setUpGlobalHost(LoginPigActivity.this, passTv);
    }


    public void onClickView(View view) {
        int i = view.getId();
        if (i == R.id.loginfamer_login) {
            if (!NetworkUtil.isNetworkConnect(LoginPigActivity.this)) {
                Toast.makeText(this, "断网了，请联网后重试。", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ActivityCompat.checkSelfPermission(AppConfig.getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                XXPermissions.with(LoginPigActivity.this)
                        //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                        .permission(Permission.READ_PHONE_STATE)
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
                                    AppManager.getAppManager().AppExit(LoginPigActivity.this);
                                }
                            }
                        });
                return;
            }
            String musername = mloginfameruserid.getText().toString();
            String muserpass = mloginfamerpass.getText().toString();
            if (musername.length() < 6 || musername.length() > 20) {
                toastUtils.showLong(this, "账号长度不正确，应为6-20位字符");
                return;
            }
            if (!TextUtils.isEmpty(musername) && !TextUtils.isEmpty(muserpass)) {
                getDataFromNet(musername, muserpass);
            } else {
                toastUtils.showLong(this, "账号或者密码为空");
            }

        } else if (i == R.id.pass_hide) {
            mloginfamerpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passshow.setVisibility(View.VISIBLE);
            passhide.setVisibility(View.GONE);

        } else if (i == R.id.pass_show) {
            mloginfamerpass.setInputType(InputType.TYPE_CLASS_TEXT);
            passhide.setVisibility(View.VISIBLE);
            passshow.setVisibility(View.GONE);

        } else {
        }

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
                Log.i("LoginPigAarActivity", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginPigActivity.this, "登录失败，请检查网络后重试。", Toast.LENGTH_SHORT).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, LoginPigActivity.class.getSimpleName());
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
                                AlertDialogManager.showMessageDialog(LoginPigActivity.this, "提示", msg, new AlertDialogManager.DialogInterface() {
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
                                        finish();
                                    } else {
                                        JSONObject enUser = data.getJSONObject("enUser");
                                        int enId = enUser.getInt("enId");
                                        int enUserId = enUser.getInt("enUserId");
                                        String enName = enUser.getString("enName");
                                        PreferencesUtils.saveKeyValue(Constants.en_id, enId + "", AppConfig.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.companyname, enName, AppConfig.getAppContext());
                                        PreferencesUtils.saveIntValue(Constants.en_user_id, enUserId, AppConfig.getAppContext());
                                        goToActivity(SelectFunctionActivity_new.class, null);
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e, LoginPigActivity.class.getSimpleName());
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
}
