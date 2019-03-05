package com.xiangchuang.risks.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.os.SystemClock;
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
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.AppManager;
import com.xiangchuang.risks.utils.ShareUtils;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import innovation.database.VideoUploadTable;
import innovation.database.VideoUploadTable_;
import innovation.entry.InnApplication;
import innovation.network_status.NetworkUtil;
import innovation.upload.UploadService;
import innovation.utils.HttpUtils;
import io.objectbox.Box;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.DataObserver;
import io.objectbox.reactive.DataSubscriptionList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author 56861
 */
public class LoginFamerActivity extends BaseActivity {

    @BindView(R.id.loginfamer_login)
    Button mloginfamerlogin;
    @BindView(R.id.loginfamer_userid)
    EditText mloginfameruserid;
    @BindView(R.id.loginfamer_pass)
    EditText mloginfamerpass;
    @BindView(R.id.pass_show)
    ImageView passshow;
    @BindView(R.id.pass_tv)
    TextView passTv;
    @BindView(R.id.pass_hide)
    ImageView passhide;
    private static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login_famer;
    }

    @Override
    protected void initData() {

        startService(new Intent(this, UploadService.class));


//        Box<VideoUploadTable> box = MyApplication.getBoxStore().boxFor(VideoUploadTable.class);
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
            AlertDialogManager.showMessageDialog(LoginFamerActivity.this, "提示", getString(R.string.appwarning), new AlertDialogManager.DialogInterface() {
                @Override
                public void onPositive() {
                    XXPermissions.with(LoginFamerActivity.this)
                            //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                            .permission(Permission.Group.LOCATION) //不指定权限则自动获取清单中的危险权限
                            .request(new OnPermission() {
                                @Override
                                public void hasPermission(List<String> granted, boolean isAll) {
                                    if (isAll) {
                                        // PreferencesUtils.saveBooleanValue("isallow", true, WelcomeActivity.this);
                                        // toastUtils.showLong(MyApplication.getAppContext(), "获取权限成功");
                                        if (android.os.Build.VERSION.SDK_INT > 9) {
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

                }

                @Override
                public void onNegative() {
                    finish();
                }
            });
        } else {
            //根据保存的标记判断是否登录
            if (PreferencesUtils.getBooleanValue(Constants.ISLOGIN, MyApplication.getAppContext())) {
                String type = PreferencesUtils.getStringValue(Constants.companyfleg, MyApplication.getAppContext());
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
            Toast.makeText(LoginFamerActivity.this, ShareUtils.getHost("host"), Toast.LENGTH_LONG).show();
        ShareUtils.setUpGlobalHost(LoginFamerActivity.this, passTv);
    }

    @OnClick({R.id.loginfamer_login, R.id.pass_hide, R.id.pass_show})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginfamer_login:
                if (!NetworkUtil.isNetworkConnect(LoginFamerActivity.this)) {
                    Toast.makeText(this, "断网了，请联网后重试。", Toast.LENGTH_SHORT).show();
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
                break;
            case R.id.pass_hide:
                mloginfamerpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passshow.setVisibility(View.VISIBLE);
                passhide.setVisibility(View.GONE);
                break;
            case R.id.pass_show:
                mloginfamerpass.setInputType(InputType.TYPE_CLASS_TEXT);
                passhide.setVisibility(View.VISIBLE);
                passshow.setVisibility(View.GONE);
                break;
            default:
                break;
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
                Log.i("LoginFamerActivity", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginFamerActivity.this, "登录失败，请检查网络后重试。", Toast.LENGTH_SHORT).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e);
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
                                    PreferencesUtils.saveKeyValue(Constants.companyfleg, type + "", MyApplication.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.username, musername + "", MyApplication.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.password, muserpass + "", MyApplication.getAppContext());
                                    PreferencesUtils.saveBooleanValue(Constants.ISLOGIN, true, MyApplication.getAppContext());

                                    //1 保险公司  2 猪场企业
                                    if (type == 1) {
                                        JSONObject adminUser = data.getJSONObject("adminUser");
                                        String deptName = adminUser.getString("deptName");
                                        String name = adminUser.getString("name");
                                        int deptId = adminUser.getInt("deptId");
                                        int id = adminUser.getInt("id");
                                        PreferencesUtils.saveKeyValue(Constants.companyuser, name, MyApplication.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.insurecompany, deptName, MyApplication.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.deptId, deptId + "", MyApplication.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.id, id + "", MyApplication.getAppContext());
                                        goToActivity(CompanyActivity.class, null);
                                        finish();
                                    } else {
                                        JSONObject enUser = data.getJSONObject("enUser");
                                        int enId = enUser.getInt("enId");
                                        int enUserId = enUser.getInt("enUserId");
                                        String enName = enUser.getString("enName");
                                        PreferencesUtils.saveKeyValue(Constants.en_id, enId + "", MyApplication.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.companyname, enName, MyApplication.getAppContext());
                                        PreferencesUtils.saveIntValue(Constants.en_user_id, enUserId, MyApplication.getAppContext());
                                        goToActivity(SelectFunctionActivity_new.class, null);
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                        });
                       /* final LoginBean bean = GsonUtils.getBean(string, LoginBean.class);
                        if (null != bean) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.dismiss();
                                    int type = bean.getData().getType();
                                    PreferencesUtils.saveKeyValue(Constants.companyfleg, type + "", MyApplication.getAppContext());
                                    //1 保险公司  2 企业
                                    if (type == 1) {
                                        PreferencesUtils.saveKeyValue(Constants.companyuser, bean.getData().getAdminUser().getName(), MyApplication.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.insurecompany, bean.getData().getAdminUser().getDeptName(), MyApplication.getAppContext());
                                        goToActivity(CompanyActivity.class, null);
                                    } else {
                                        PreferencesUtils.saveKeyValue(Constants.en_id, bean.getData().getEnUser().getEnId() + "", MyApplication.getAppContext());
                                        PreferencesUtils.saveKeyValue(Constants.companyname, bean.getData().getEnUser().getEnName() + "", MyApplication.getAppContext());
                                        PreferencesUtils.saveIntValue(Constants.en_user_id, bean.getData().getEnUser().getEnUserId(), MyApplication.getAppContext());
                                        goToActivity(SelectFunctionActivity.class, null);
                                    }
                                }

                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.dismiss();
                                    toastUtils.showLong(LoginFamerActivity.this, bean.getMsg());
                                }
                            });
                        }*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e);
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
