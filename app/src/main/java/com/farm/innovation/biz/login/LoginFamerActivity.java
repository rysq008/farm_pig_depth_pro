package com.farm.innovation.biz.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.MultiBaodanBean;
import com.farm.innovation.bean.ResultBean;
import com.farm.innovation.login.InputValidation;
import com.farm.innovation.login.RegisterActivity;
import com.farm.innovation.login.RespObject;
import com.farm.innovation.login.ResponseProcessor;
import com.farm.innovation.login.TokenResp;
import com.farm.innovation.login.Utils;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.FarmerShareUtils;
import com.farm.innovation.utils.HttpUtils;
import com.google.gson.Gson;
import com.innovation.pig.insurance.R;

import org.tensorflow.demo.env.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class LoginFamerActivity extends BaseActivity implements ILoginView {

    private static final String TAG = "LoginFamerActivity";
    private final AppCompatActivity activity = LoginFamerActivity.this;
    private final Logger mLogger = new Logger(LoginFamerActivity.class.getSimpleName());


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
    private ResultBean resultBean;
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

        SharedPreferences pref = this.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        if (!TextUtils.isEmpty(pref.getString("token", ""))) {
            Intent add_intent = new Intent(LoginFamerActivity.this, HomeActivity.class);
            startActivity(add_intent);
            finish();
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
        mAuthTask = new UserLoginTask(HttpUtils.PIC_LOGIN_URL, query);
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
                AVOSCloudUtils.saveErrorMessage(e, LoginFamerActivity.class.getSimpleName());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            gson = new Gson();
            resultBean = gson.fromJson(responseUserLoginTask, ResultBean.class);
            if (resultBean != null) {
                Log.d(TAG, mUrl + "\nresponseUserLoginTask:" + responseUserLoginTask);
                if (resultBean.getStatus() == 1) {
                    if (HttpUtils.PIC_LOGIN_URL.equalsIgnoreCase(mUrl)) {
                        TokenResp tokenresp = (TokenResp) ResponseProcessor.processResp(responseUserLoginTask, mUrl);
                        if (tokenresp == null || TextUtils.isEmpty(tokenresp.token) || tokenresp.user_status != RespObject.USER_STATUS_1) {
                            return;

                        }

                        if ((String.valueOf(tokenresp.uid)).equals(FarmerPreferencesUtils.getStringValue(HttpUtils.user_id, LoginFamerActivity.this))) {
                            FarmerPreferencesUtils.saveBooleanValue("isone", true, LoginFamerActivity.this);
                        } else {
                            FarmerPreferencesUtils.saveBooleanValue("isone", false, LoginFamerActivity.this);
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
                    Intent add_intent = new Intent(LoginFamerActivity.this, HomeActivity.class);
                    startActivity(add_intent);
                    finish();
                } else if (resultBean.getStatus() == 0) {
                    Snackbar.make(nestedScrollView, resultBean.getMsg(), Snackbar.LENGTH_SHORT).setText(resultBean.getMsg()).show();
//                        mProgressHandler.sendEmptyMessage(24);
                    return;
                } else {
//                        mProgressHandler.sendEmptyMessage(44);
                    Snackbar.make(nestedScrollView, resultBean.getMsg(), Snackbar.LENGTH_SHORT).setText(resultBean.getMsg()).show();
                    return;
                }

            } else {
                Snackbar.make(nestedScrollView, "服务器错误，请稍后再试！", Snackbar.LENGTH_LONG).setText("服务器错误，请稍后再试！").show();
//                    mProgressHandler.sendEmptyMessage(41);
            }


            if (success & HttpUtils.PIC_LOGIN_URL.equalsIgnoreCase(mUrl)) {
                Intent add_intent = new Intent(LoginFamerActivity.this, HomeActivity.class);
                startActivity(add_intent);
                finish();
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


    // TODO: 2018/9/3 By:LuoLu
    @SuppressLint("HandlerLeak")
    private Handler mProgressHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 41:
                    Toast.makeText(LoginFamerActivity.this, "服务器异常，请稍后再试！", Toast.LENGTH_SHORT).show();
                    break;
                case 44:
                    Toast.makeText(LoginFamerActivity.this, resultBean.getMsg(), Toast.LENGTH_SHORT).show();
                    break;
                case 24:
                    Toast.makeText(LoginFamerActivity.this, resultBean.getMsg(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // AppManager.getAppManager().finishActivity(this);
    }

    private long firstTime = 0;

    @Override
    public void onBackPressed() {
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            firstTime = secondTime;
        } else {
//            SharedPreferences pref = getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = pref.edit();
//            editor.clear();
//            editor.commit();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }
}

