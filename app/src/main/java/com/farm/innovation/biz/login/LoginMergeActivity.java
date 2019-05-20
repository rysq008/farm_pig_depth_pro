package com.farm.innovation.biz.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.MergeLoginBean;
import com.farm.innovation.bean.MultiBaodanBean;
import com.farm.innovation.login.InputValidation;
import com.farm.innovation.login.RegisterActivity;
import com.farm.innovation.login.RespObject;
import com.farm.innovation.login.Utils;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.ConstUtils;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.FarmerShareUtils;
import com.farm.innovation.utils.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.PreferencesUtils;
import com.xiangchuang.risks.view.CompanyActivity;
import com.xiangchuang.risks.view.SelectFunctionActivity_new;

import org.tensorflow.demo.env.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.farm.innovation.utils.FarmerShareUtils.MERGE_LOGIN_INFO;

public class LoginMergeActivity extends BaseActivity implements ILoginView {

    private static final String TAG = "LoginFamerActivity";
    private final AppCompatActivity activity = LoginMergeActivity.this;
    private final Logger mLogger = new Logger(LoginMergeActivity.class.getSimpleName());


    AppCompatEditText textInputEditPhoneNumber;

    TextInputLayout textInputLayoutPhoneNumber;

    AppCompatEditText textInputEditPassword;

    TextInputLayout textInputLayoutPassword;

    TextView versionName;

    NestedScrollView nestedScrollView;
    private InputValidation inputValidation;


    private String errString = "";
    private UserLoginTask mAuthTask;

    private LoginPresenter loginPresenter;
    private MultiBaodanBean loginResult;

    private AlertDialog.Builder builder;
    private RadioGroup animalTypeRadioGroup;
    private View v;
    private LayoutInflater inflater;
    private Dialog dialog;
    private Button okButton;
    private Gson gson;
    //    private ResultBean resultBean;
    private MergeLoginBean resultBean;
    private String responseUserLoginTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_login;
    }

    @Override
    protected void initData() {
        textInputEditPhoneNumber = (AppCompatEditText) findViewById(R.id.textInputEditPhoneNumber);
        textInputLayoutPhoneNumber = (TextInputLayout) findViewById(R.id.textInputLayoutPhoneNumber);
        textInputEditPassword = (AppCompatEditText) findViewById(R.id.textInputEditPassword);
        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputLayoutPassword);
        versionName = (TextView) findViewById(R.id.version_name);
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);
        findViewById(R.id.textViewLinkRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.appCompatButtonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });

        MergeLoginBean bean = FarmerShareUtils.getData(MERGE_LOGIN_INFO);
        if (bean != null) {
            if (bean.data.nxData != null && !TextUtils.isEmpty(bean.data.nxData.token) && bean.data.nxData.status == RespObject.USER_STATUS_1) {
                showTypeDialog(bean);
            } else {
                if (bean.data.ftnData != null) {
                    if (bean.data.ftnData.type == 1) {
                        goToActivity(CompanyActivity.class, null);
                        finish();
                    } else if (bean.data.ftnData.type == 2) {
                        goToActivity(SelectFunctionActivity_new.class, null);
                        finish();
                    }
                }
            }
        }
        inputValidation = new InputValidation(this.getBaseContext());
        loginPresenter = new LoginPresenter(this);
        versionName.setText(getString(R.string.version_name) + "v" + getVersionName());

        FarmerShareUtils.setUpGlobalHost(this, versionName);
    }


    /**
     * get App versionName
     *
     * @return versionName
     */
    private String getVersionName() {
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        FarmAppConfig.version = versionName;
        return versionName;
    }


    @Override
    public void onAuthenticate() {

    }

    @Override
    public int onLoginError(String message) {
        return 0;
    }

    @Override
    public void onLoginSuccess(String message) {

    }

    @Override
    public void onLogin() {

        if (!InputValidation.isMobile(textInputEditPhoneNumber, textInputLayoutPhoneNumber, getString(R.string.error_message_phone_number))) {
            return;
        }
        if (!InputValidation.isInputEditTextFilled(textInputEditPassword, textInputLayoutPassword, getString(R.string.error_message_password))) {
            return;
        }
        mLogger.i("databaseHelper.checkUser: " + textInputEditPhoneNumber.getText().toString().trim());
        mLogger.i("databaseHelper.checkUser Password: " + textInputEditPassword.getText().toString().trim());
        Map<String, String> query = new HashMap<>();
        query.put("mobilephone", textInputEditPhoneNumber.getText().toString().trim());
        query.put("password", textInputEditPassword.getText().toString().trim());
        query.put("imageCode", "1234");
        mAuthTask = new UserLoginTask(HttpUtils.MERGE_LOGIN_URL, query);
        mAuthTask.execute((Void) null);
    }

    public void onViewClicked(View view) {
        int i = view.getId();
        if (i == R.id.appCompatButtonLogin) {
            View v = activity.getCurrentFocus();
            if (v != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            if (!FarmAppConfig.isNetConnected) {
                Toast.makeText(activity, "断网了，请联网后重试", Toast.LENGTH_SHORT).show();
                return;
            }
            onLogin();

        } else if (i == R.id.textViewLinkRegister) {
            Intent intentRegister = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intentRegister);

        } else {
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mUrl;
        private final Map<String, String> mQueryMap;

        UserLoginTask(String url, Map<String, String> map) {
            mUrl = url;
            mQueryMap = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                FormBody.Builder builder = new FormBody.Builder();
                for (TreeMap.Entry<String, String> entry : mQueryMap.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }
                RequestBody formBody = builder.build();
                responseUserLoginTask = HttpUtils.post(mUrl, formBody);
                if (responseUserLoginTask != null) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                errString = "服务器错误！";
                AVOSCloudUtils.saveErrorMessage(e, LoginMergeActivity.class.getSimpleName());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            gson = new Gson();

            if (success) {
                try {
                    resultBean = gson.fromJson(responseUserLoginTask, MergeLoginBean.class);
                    FarmerShareUtils.saveData(MERGE_LOGIN_INFO, resultBean);
                    if (resultBean != null) {
                        Log.d(TAG, mUrl + "\nresponseUserLoginTask:" + responseUserLoginTask);
                        if (resultBean.isSuccess()) {
                            MergeLoginBean.FarmerLoginBean fbean = resultBean.data.nxData;
                            MergeLoginBean.PigLoginBean pbean = resultBean.data.ftnData;
                            if (pbean != null) {
                                PreferencesUtils.saveKeyValue(Constants.companyfleg, pbean.type + "", AppConfig.getAppContext());
                                PreferencesUtils.saveKeyValue(Constants.username, textInputEditPhoneNumber.getText().toString().trim(), AppConfig.getAppContext());
                                PreferencesUtils.saveKeyValue(Constants.password, textInputEditPassword.getText().toString().trim(), AppConfig.getAppContext());
                                PreferencesUtils.saveBooleanValue(Constants.ISLOGIN, true, AppConfig.getAppContext());

                                //1 保险公司  2 猪场企业
                                if (pbean.type == 1) {
                                    String deptName = pbean.adminUser.deptName;// adminUser.getString("deptName");
                                    String name = pbean.adminUser.name;// adminUser.getString("name");
                                    int deptId = pbean.adminUser.deptId;// adminUser.getInt("deptId");
                                    int id = pbean.adminUser.id;// adminUser.getInt("id");
                                    PreferencesUtils.saveKeyValue(Constants.companyuser, name, AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.insurecompany, deptName, AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.deptId, deptId + "", AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.id, id + "", AppConfig.getAppContext());

                                    if (TextUtils.isEmpty(fbean.token) || fbean.status != RespObject.USER_STATUS_1) {
                                        goToActivity(CompanyActivity.class, null);
                                        finish();
                                    }
                                } else if(pbean.type == 2){
                                    int enId = pbean.enUser.enId;// enUser.getInt("enId");
                                    int enUserId = pbean.enUser.enUserId;// enUser.getInt("enUserId");
                                    String enName = pbean.enUser.enName;// enUser.getString("enName");
                                    PreferencesUtils.saveKeyValue(Constants.en_id, enId + "", AppConfig.getAppContext());
                                    PreferencesUtils.saveKeyValue(Constants.companyname, enName, AppConfig.getAppContext());
                                    PreferencesUtils.saveIntValue(Constants.en_user_id, enUserId, AppConfig.getAppContext());
                                    if (TextUtils.isEmpty(fbean.token) || fbean.status != RespObject.USER_STATUS_1) {
                                        goToActivity(SelectFunctionActivity_new.class, null);
                                        finish();
                                    }
                                }else{
                                    FarmerShareUtils.clearMergeLoginInfo();
                                    Snackbar.make(nestedScrollView, "服务器错误，无法识别用户身份！！", Snackbar.LENGTH_LONG).setText("服务器错误，无法识别用户身份！").show();
                                    return;
                                }
                            }
                            if (fbean == null || TextUtils.isEmpty(fbean.token) || fbean.status != RespObject.USER_STATUS_1) {
//                            return;
                                Snackbar.make(nestedScrollView, "服务器错误，请稍后再试！", Snackbar.LENGTH_LONG).setText("服务器错误，请稍后再试！").show();
                            } else {
                                //  存储用户信息
                                SharedPreferences userinfo = getApplicationContext().getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = userinfo.edit();
                                editor.putString("token", fbean.token);
                                //  int 类型的可能需要修�?
                                //  验证码的有效期，应该在获取验证码的时候返回才�?
                                editor.putInt("tokendate", fbean.tokendate);
                                editor.putInt("uid", fbean.uid);
                                editor.putString("username", fbean.username);
                                editor.putString("fullname", fbean.fullname);
                                editor.putString("codedate", String.valueOf(fbean.codedate));
                                //用户创建时间
                                editor.putString("createtime", fbean.createtime);
                                //  editor.putInt("deptid", tokenresp.deptid);
                                editor.apply();
                                int i = fbean.deptid;
                                FarmerPreferencesUtils.saveIntValue(HttpUtils.deptId, fbean.deptid, FarmAppConfig.getApplication());
                                FarmerPreferencesUtils.saveKeyValue(HttpUtils.user_id, String.valueOf(fbean.uid), FarmAppConfig.getApplication());
                                Log.i("===id==", fbean.uid + "");

                                showTypeDialog(resultBean);
                            }

                        } else if (resultBean.status == 0) {
                            Snackbar.make(nestedScrollView, resultBean.msg, Snackbar.LENGTH_SHORT).setText(resultBean.msg).show();
                            return;
                        } else {
                            Snackbar.make(nestedScrollView, resultBean.msg, Snackbar.LENGTH_SHORT).setText(resultBean.msg).show();
                            return;
                        }

                    } else {
                        Snackbar.make(nestedScrollView, "服务器错误，请稍后再试！", Snackbar.LENGTH_LONG).setText("服务器错误，请稍后再试！").show();
                    }
                } catch (JsonSyntaxException e) {
                    Toast.makeText(activity, "登录数据解析异常", Toast.LENGTH_SHORT).show();
                }
                ;
            } else if (!success) {
                // 显示失败
                Snackbar.make(nestedScrollView, errString, Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    private void showTypeDialog(MergeLoginBean bean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginMergeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(LoginMergeActivity.this);
        View v = inflater.inflate(R.layout.select_dialog_layout, null);
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(v);

        v.findViewById(R.id.merge_select_layout).setVisibility(View.VISIBLE);
        v.findViewById(R.id.pig_select_layout).setVisibility(View.GONE);
        TextView famer = v.findViewById(R.id.tv_famer_select);
        TextView donkey = v.findViewById(R.id.tv_donkey_select);
        TextView yak = v.findViewById(R.id.tv_yak_select);
        TextView pig = v.findViewById(R.id.tv_pig_select);
        View card_farmer = v.findViewById(R.id.cardview_farmer);
        View card_pig = v.findViewById(R.id.cardview_pig);
        ImageView close = v.findViewById(R.id.iv_close);

        famer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Log.e("famer", "onClick: famer");
                FarmerPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_CATTLE, LoginMergeActivity.this);
                Intent add_intent = new Intent(LoginMergeActivity.this, HomeActivity.class);
                startActivity(add_intent);
                bean.enterByStatus = 1;
                finish();
            }
        });

        donkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                FarmerPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_DONKEY, LoginMergeActivity.this);
                Intent add_intent = new Intent(LoginMergeActivity.this, HomeActivity.class);
                startActivity(add_intent);
                bean.enterByStatus = 1;
                finish();
            }
        });

        yak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                FarmerPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_YAK, LoginMergeActivity.this);
                Intent add_intent = new Intent(LoginMergeActivity.this, HomeActivity.class);
                startActivity(add_intent);
                bean.enterByStatus = 1;
                finish();
            }
        });
        card_farmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent add_intent = new Intent(LoginMergeActivity.this, HomeActivity.class);
                startActivity(add_intent);
                bean.enterByStatus = 1;
                finish();
            }
        });
        pig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                bean.enterByStatus = 2;
                if (bean.data.ftnData.type == 1) {
                    goToActivity(CompanyActivity.class, null);
                    finish();
                } else if (bean.data.ftnData.type == 2) {
                    goToActivity(SelectFunctionActivity_new.class, null);
                    finish();
                }
            }
        });
        card_pig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                bean.enterByStatus = 2;
                if (bean.data.ftnData.type == 1) {
                    goToActivity(CompanyActivity.class, null);
                    finish();
                } else if (bean.data.ftnData.type == 2) {
                    goToActivity(SelectFunctionActivity_new.class, null);
                    finish();
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                LoginMergeActivity.this.finish();
                Process.killProcess(Process.myPid());
                System.exit(0);
            }
        });
        if (bean.enterByStatus == 1) {
            card_farmer.performClick();
        } else if (bean.enterByStatus == 2) {
            card_pig.performClick();
        }
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                FarmerShareUtils.saveData(MERGE_LOGIN_INFO,bean);
            }
        });
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private long firstTime = 0;

    @Override
    public void onBackPressed() {
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            firstTime = secondTime;
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }
}