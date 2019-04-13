
package com.farm.innovation.login;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.bean.Company;
import com.farm.innovation.bean.MultiBaodanBean;
import com.farm.innovation.bean.ResultBean;
import com.farm.innovation.bean.company_child;
import com.farm.innovation.bean.company_total;
import com.farm.innovation.biz.login.LoginActivity;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.HttpRespObject;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.OkHttp3Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.innovation.pig.insurance.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.demo.env.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.READ_CONTACTS;
import static com.farm.innovation.login.InputValidation.isInputEditTextFilled;
import static com.farm.innovation.login.InputValidation.isMobile;
import static com.farm.innovation.login.InputValidation.isMobileNO;

public class RegisterActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private static String TAG = "RegisterActivity";
    private final AppCompatActivity activity = RegisterActivity.this;
    private Logger mLogger = new Logger(RegisterActivity.class.getSimpleName());
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask;

    private NestedScrollView nestedScrollView;

    private TextInputLayout textInputLayoutName;
    private TextInputLayout textInputLayoutIDNumber;
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutConfirmPassword;
    private TextInputLayout textInputLayoutPhoneNumber;
    private TextInputLayout textInputLayoutSMScode;

    private AppCompatEditText textInputEditTextName;
    private AppCompatEditText textInputEditIDNumber;
    private AppCompatEditText textInputEditTextPassword;
    private AppCompatEditText textInputEditTextConfirmPassword;
    private AppCompatEditText textInputEditPhoneNumber;
    private AppCompatEditText textInputSMScode;

    private TextInputLayout textInputLayoutCompany;
    private AppCompatEditText textInputEditCompany;

    private AppCompatButton appCompatButtonRegister;
    private AppCompatTextView appCompatTextViewLogin;

    private InputValidation inputValidation;
    private DatabaseHelper databaseHelper;

    private AppCompatEditText mPhoneNumView;
    private Button mGetCheckCodeBtn;
    private AppCompatEditText mPasswordView;
    private EditText getSMScodeCheck;
    private View mProgressView;
    private View mLoginFormView;
    private LoginResp loginresp = null;
    private TokenResp tokenresp = null;
    private GetVerifyCodeTask mGetVerifyCodeTask;
    private HttpRespObject insurresp;
    private String errStr = "";

    private boolean tag = true;
    private int i = 30;
    Thread thread = null;

    //所属公司下拉框
    private String pickerTag = "";
    private static ArrayList<String> company_totals = new ArrayList<>();
    private List<company_total> company_totalsList;
    private List<company_total> queryList;
    private ArrayList<String> company_childs;
    private List<company_child> company_childsList;
    private AppCompatEditText company_code;
    private GETCOMPANYTask mCompanyTask;
    private MultiBaodanBean insurresp_company;
    private String errStr_company;
    private TextInputLayout textInputLayoutCompanyCode;

    private Map<String, Integer> companyMap;
    private Map<String, Integer> deptMap;
    private AppCompatEditText textInputEditInviteCode;
    private TextView register_verify;
    private int deptid = 0;
    private Gson gson;
    private String registerResponse;
    private ResultBean resultBean;
    private ImageView iv_cancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.farm_activity_register);
        mCompanyTask = new GETCOMPANYTask(HttpUtils.GET_ALL_COMPANY_URL, null);
        mCompanyTask.execute((Void) null);

        initViews();
        initListeners();
        initObjects();

        mPhoneNumView = (AppCompatEditText) findViewById(R.id.textInputEditPhoneNumber);
        mPasswordView = (AppCompatEditText) findViewById(R.id.textInputSMScode);
        mGetCheckCodeBtn = (Button) findViewById(R.id.get_checkcode_btn1);
//        getSMScodeCheck = (EditText) findViewById(R.id.getSMScodeCheck);

        SharedPreferences pref = this.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        if (!TextUtils.isEmpty(pref.getString("token", ""))) {
//            startDetectActity();
//            finish();
        }

        // Set up the login form.
        populateAutoComplete();

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.login1 || id == EditorInfo.IME_NULL) {
                if (id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mGetCheckCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isMobileNO(textInputEditPhoneNumber.getText().toString().trim())) {
                    Toast.makeText(RegisterActivity.this, "请输正确手机号。", Toast.LENGTH_LONG).show();
                    return;
                }

//                if (!isMobile(textInputEditPhoneNumber, textInputLayoutPhoneNumber, getString(R.string.error_message_phone_number))) {
//                    return;
//                }

                mGetCheckCodeBtn.setClickable(true);
                changeBtnGetCode();
                TreeMap query = new TreeMap<String, String>();
                query.put("mobilephone", mPhoneNumView.getText().toString());
                mGetVerifyCodeTask = new GetVerifyCodeTask(HttpUtils.GET_SMSCODE_URL, query);
                mGetVerifyCodeTask.execute((Void) null);


            }
        });

    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_register;
    }


    private void initViews() {

        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        textInputLayoutName = (TextInputLayout) findViewById(R.id.textInputLayoutName);
        textInputLayoutIDNumber = (TextInputLayout) findViewById(R.id.textInputLayoutIDNumber);
        textInputLayoutCompany = (TextInputLayout) findViewById(R.id.textInputLayoutCompany);
        textInputLayoutCompanyCode = (TextInputLayout) findViewById(R.id.textInputLayoutCompanyCode);
        textInputLayoutConfirmPassword = (TextInputLayout) findViewById(R.id.textInputLayoutConfirmPassword);
        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputLayoutPassword);
        textInputLayoutPhoneNumber = (TextInputLayout) findViewById(R.id.textInputLayoutPhoneNumber);
        textInputLayoutSMScode = (TextInputLayout) findViewById(R.id.textInputLayoutSMScode);

        textInputEditTextName = (AppCompatEditText) findViewById(R.id.textInputEditName);
        textInputEditIDNumber = (AppCompatEditText) findViewById(R.id.textInputEditIDNumber);
        textInputEditCompany = (AppCompatEditText) findViewById(R.id.textInputEditCompany);
        textInputEditTextPassword = (AppCompatEditText) findViewById(R.id.textInputEditPassword);
        textInputEditTextConfirmPassword = (AppCompatEditText) findViewById(R.id.textInputEditConfirmPassword);
        textInputEditPhoneNumber = (AppCompatEditText) findViewById(R.id.textInputEditPhoneNumber);
        textInputSMScode = (AppCompatEditText) findViewById(R.id.textInputSMScode);
        textInputEditInviteCode = (AppCompatEditText) findViewById(R.id.textInputEditInviteCode);
        register_verify = (TextView) findViewById(R.id.register_verify);

        textInputEditCompany.setInputType(InputType.TYPE_NULL);
//        textInputEditCompany.requestFocus();
        textInputEditCompany.setCursorVisible(false);
        textInputEditCompany.setFocusableInTouchMode(false);
        textInputEditCompany.setFocusable(false);

        appCompatButtonRegister = (AppCompatButton) findViewById(R.id.appCompatButtonRegister);
        appCompatTextViewLogin = (AppCompatTextView) findViewById(R.id.textViewLinkLogin);

        company_code = (AppCompatEditText) findViewById(R.id.company_code);


    }

    private void initListeners() {
        appCompatButtonRegister.setOnClickListener(this);
        appCompatTextViewLogin.setOnClickListener(this);
        textInputEditCompany.setOnClickListener(this);
        textInputLayoutCompany.setOnClickListener(this);
        register_verify.setOnClickListener(this);
    }

    public void initObjects() {

        inputValidation = new InputValidation(activity);

        databaseHelper = DatabaseHelper.getInstance(this);

    }

    @Override
    public void onClick(View v) {

        company_childs = null;
        company_childsList = null;
        int i1 = v.getId();
        if (i1 == R.id.appCompatButtonRegister) {
            postDataToSQLite();
//                if (0 == deptid) {
//                    showDialogError("邀请码验证码错误");
//                } else {
//                    postDataToSQLite();
//                }

        } else if (i1 == R.id.textViewLinkLogin) {
            finish();

        } else if (i1 == R.id.textInputLayoutCompany || i1 == R.id.textInputEditCompany) {// pickerTag = "COMP";
            //initMPickerView();
            // mPickerView.setPicker(company_totals);
            //mPickerView.show();

        } else if (i1 == R.id.register_verify) {
            String s = textInputEditInviteCode.getText().toString();
            if ("".equals(s)) {
                Toast.makeText(RegisterActivity.this, "请输入邀请码", Toast.LENGTH_LONG).show();
            } else {
                VerifyCodeFromNet(s);
            }

        } else {
        }
    }

    private void VerifyCodeFromNet(String s) {
        Map<String, String> map = new HashMap<>();
        map.put(HttpUtils.AppKeyAuthorization, "hopen");
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put(HttpUtils.code, s);
        OkHttp3Util.doPost(HttpUtils.vertify_URL, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("RegisterActivity", e.toString());
                AVOSCloudUtils.saveErrorMessage(e, RegisterActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("RegisterActivity", HttpUtils.vertify_URL + "\n" + string);
                deptid = 0;
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status == -1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showDialogError(msg);
                            }
                        });

                    } else if (status == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    String fullname = data.getString("fullname");
                                    deptid = data.getInt("id");
                                    textInputEditCompany.setText(fullname);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                        });
                    } else if (status == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String msg = jsonObject.getString("msg");
                                    textInputEditCompany.setText(msg);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e, RegisterActivity.class.getSimpleName());
                }


            }
        });

    }

    public void showDialogError(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this)
                .setIcon(R.drawable.farm_cowface).setTitle("提示")
                .setMessage(s)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    public void postDataToSQLite() {

        if (!inputValidation.isInputChineseEditTextFilled(textInputEditTextName, textInputLayoutName, getString(R.string.error_message_name))) {
            return;
        }
        if (!inputValidation.isvalidateIDcardNumber(textInputEditIDNumber, textInputLayoutIDNumber, getString(R.string.error_message_idnumber))) {
            return;
        }
        if (0 == deptid) {
            showDialogError("邀请码未通过验证");
            return;
        }
        if (!inputValidation.isInputChineseEditTextFilled(textInputEditCompany, textInputLayoutCompany, getString(R.string.error_message_company))) {
            return;
        }
        if (!isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, getString(R.string.error_message_password))) {
            return;
        }
        if (!inputValidation.isInputEditTextMatches(textInputEditTextPassword, textInputEditTextConfirmPassword, textInputLayoutConfirmPassword, getString(R.string.error_password_match))) {
            return;
        }
        if (!isMobile(textInputEditPhoneNumber, textInputLayoutPhoneNumber, getString(R.string.error_message_phone_number))) {
            return;
        }
        if (!inputValidation.isInputSmscodeValid(textInputSMScode, textInputLayoutSMScode, getString(R.string.error_message_checkcode))) {
            return;
        }

        TreeMap<String, String> query = new TreeMap<>();
        query.put("mobile", mPhoneNumView.getText().toString().trim());
        query.put("fullname", textInputEditTextName.getText().toString().trim());
        query.put("card", textInputEditIDNumber.getText().toString().trim());
        query.put("code", textInputSMScode.getText().toString().trim());
        query.put("password", textInputEditTextPassword.getText().toString().trim());
        query.put("repwd", textInputEditTextConfirmPassword.getText().toString().trim());
        query.put("deptid", deptid + "");
        mAuthTask = new UserRegisterTask(HttpUtils.GET_REGISTER_URL, query);
        mAuthTask.execute((Void) null);

    }

    private void emptyInputEditText() {
        textInputEditTextName.setText(null);
        textInputEditIDNumber.setText(null);
        textInputEditCompany.setText(null);
        textInputEditTextPassword.setText(null);
        textInputEditTextConfirmPassword.setText(null);
        mPhoneNumView.setText(null);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), RegisterActivity.ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
//        cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                emails.add(cursor.getString(RegisterActivity.ProfileQuery.ADDRESS));
//                cursor.moveToNext();
//        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mPhoneNumView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhoneNumView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phonenum = mPhoneNumView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_code));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phonenum)) {
            mPhoneNumView.setError(getString(R.string.error_field_required));
            focusView = mPhoneNumView;
            cancel = true;
        } else if (!isPhonenumValid(phonenum)) {
            mPhoneNumView.setError(getString(R.string.error_invalid_email));
            focusView = mPhoneNumView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

        }
    }

    private boolean isPhonenumValid(String phonenum) {
        // : Replace this with your own logic
        return phonenum.length() == 11;
    }

    private boolean isPasswordValid(String password) {
        // : Replace this with your own logic
        return password.length() > 4;
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
//        ArrayAdapter<String> adapter =
//                new ArrayAdapter<>(RegisterActivity.this,
//                        android.R.layout.farm_simple_dropdown_item_1line, emailAddressCollection);

//        mPhoneNumView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final TreeMap<String, String> mQueryMap;

        UserRegisterTask(String url, TreeMap map) {
            mUrl = url;
            mQueryMap = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //  attempt authentication against a network service.
            try {
                gson = new Gson();
                FormBody.Builder builder = new FormBody.Builder();
                // Add Params to Builder
                for (TreeMap.Entry<String, String> entry : mQueryMap.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }
                // Create RequestBody
                RequestBody formBody = builder.build();
                registerResponse = HttpUtils.post(mUrl, formBody);
                if (registerResponse != null) {
                    return true;
                } else {
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                errStr = "服务器错误！";
                AVOSCloudUtils.saveErrorMessage(e, RegisterActivity.class.getSimpleName());
                return false;
            }
            //  register the new account here.

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success & HttpUtils.GET_REGISTER_URL.equalsIgnoreCase(mUrl)) {

                resultBean = gson.fromJson(registerResponse, ResultBean.class);
                if (resultBean != null) {
                    Log.d(TAG, mUrl + "\nregisterResponse:" + registerResponse);
                    if (resultBean.getStatus() == 1) {
                        Toast.makeText(RegisterActivity.this, resultBean.getMsg(), Toast.LENGTH_SHORT).show();
                        Intent add_intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(add_intent);
                        finish();
                        return;
                    } else if (resultBean.getStatus() == 0) {
                        Snackbar.make(nestedScrollView, resultBean.getMsg(), Snackbar.LENGTH_LONG).show();
                        return;
                    } else {
                        Snackbar.make(nestedScrollView, resultBean.getMsg(), Snackbar.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    Snackbar.make(nestedScrollView, "服务器异常！！", Snackbar.LENGTH_LONG).show();
                }


            } else if (!success) {
                //  显示失败
                Snackbar.make(nestedScrollView, errStr, Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }


    public class GetVerifyCodeTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final TreeMap<String, String> mQueryMap;

        GetVerifyCodeTask(String url, TreeMap map) {
            mUrl = url;
            mQueryMap = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //  attempt authentication against a network service.
            try {
                FormBody.Builder builder = new FormBody.Builder();
                // Add Params to Builder
                for (TreeMap.Entry<String, String> entry : mQueryMap.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }
                // Create RequestBody
                RequestBody formBody = builder.build();

                String response = HttpUtils.post(mUrl, formBody);
                Log.d(TAG, mUrl + "\nresponse:" + response);

                if (HttpUtils.GET_SMSCODE_URL.equalsIgnoreCase(mUrl)) {
                    insurresp = HttpUtils.processResp_insurInfo(response, mUrl);
                    if (insurresp == null) {
//                        errStr = getString(R.string.error_newwork);
                        errStr = "请求错误！";
                        return false;
                    }
                    if (insurresp.status != HttpRespObject.STATUS_OK) {
                        errStr = insurresp.msg;
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                errStr = "服务器错误！";
                AVOSCloudUtils.saveErrorMessage(e, RegisterActivity.class.getSimpleName());
                return false;
            }
            //  register the new account here.

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetVerifyCodeTask = null;
            if (success & HttpUtils.GET_SMSCODE_URL.equalsIgnoreCase(mUrl)) {
                Log.d(TAG, errStr);
                Log.d(TAG, insurresp.msg);

            } else if (!success) {
                //  显示失败
                Log.d(TAG, errStr);
//                tv_info.setText(errStr);
            }
        }

        @Override
        protected void onCancelled() {
            mGetVerifyCodeTask = null;
        }
    }


    private void changeBtnGetCode() {
        thread = new Thread() {
            @Override
            public void run() {
                if (tag) {
                    while (i > 0) {
                        i--;
                        if (this == null) {
                            break;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mGetCheckCodeBtn.setText("获取验证码("
                                        + i + ")");
                                mGetCheckCodeBtn
                                        .setClickable(false);
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    tag = false;
                }
                i = 30;
                tag = true;
                if (this != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mGetCheckCodeBtn.setText("获取验证码");
                            mGetCheckCodeBtn.setClickable(true);
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public class GETCOMPANYTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final TreeMap<String, String> mQueryMap;

        GETCOMPANYTask(String url, TreeMap map) {
            mUrl = url;
            mQueryMap = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //  attempt authentication against a network service.
            try {
                FormBody.Builder builder = new FormBody.Builder();
                RequestBody formBody = builder.build();

                String response = HttpUtils.post(mUrl, formBody);
                Log.d(TAG, mUrl + "\nresponse:" + response);

                if (HttpUtils.GET_ALL_COMPANY_URL.equalsIgnoreCase(mUrl)) {
                    insurresp_company = (MultiBaodanBean) HttpUtils.processResp_new_detail_query(response);
                    if (insurresp_company == null) {
                        errStr_company = "请求错误！";
                        return false;
                    }
                    if (insurresp_company.status != HttpRespObject.STATUS_OK) {
                        errStr_company = insurresp_company.msg;
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                errStr_company = "服务器错误！";
                AVOSCloudUtils.saveErrorMessage(e, RegisterActivity.class.getSimpleName());
                return false;
            }
            // : register the new account here.

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCompanyTask = null;

            if (success & HttpUtils.GET_ALL_COMPANY_URL.equalsIgnoreCase(mUrl)) {
                Gson gson = new Gson();
                List<Company> userList = gson.fromJson(String.valueOf(insurresp_company.data), new TypeToken<List<Company>>() {
                }.getType());
                Log.d(TAG, "userList:" + userList);

                company_totalsList = databaseHelper.queryProvince();
                companyMap = new HashMap<String, Integer>();

                if (company_totalsList.size() <= 0) {
                    List<String> sqls = new ArrayList<String>();

                    for (int i = 0; i < userList.size(); i++) {
                        sqls.add("insert into tCompany (id,fullname,pid) values (" + userList.get(i).getCompanyId() + ",'" + userList.get(i).getCompanyName() + "'," + userList.get(i).getCompanyPid() + ")");
                    }
                    databaseHelper.addCompany(sqls);


                    company_totalsList = databaseHelper.queryProvince();
                    for (company_total companytotal : company_totalsList) {
                        company_totals.add(companytotal.getCompanyName());
                        companyMap.put(companytotal.getCompanyName(), companytotal.getCompanyId());
                    }
                } else {
                    for (company_total companytotal : company_totalsList) {
                        company_totals.add(companytotal.getCompanyName());
                        companyMap.put(companytotal.getCompanyName(), companytotal.getCompanyId());
                    }
                }


            } else if (!success) {
                //  显示失败
                Log.d(TAG, errStr_company);
//                tv_info.setText(errStr);
            }
        }

        @Override
        protected void onCancelled() {
            mCompanyTask = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: ");
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
    }
}




