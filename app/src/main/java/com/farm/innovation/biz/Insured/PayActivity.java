package com.farm.innovation.biz.Insured;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.LiPeiLocalBean;
import com.farm.innovation.bean.PayInfoCheckResultBean;
import com.farm.innovation.bean.ResultBean;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.biz.processor.PayDataProcessor;
import com.farm.innovation.location.AlertDialogManager;
import com.farm.innovation.location.LocationManager;
import com.farm.innovation.login.DatabaseHelper;
import com.farm.innovation.login.Utils;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.HttpUtils;
import com.google.gson.Gson;
import com.innovation.pig.insurance.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


import org.tensorflow.demo.FarmDetectorActivity;
import org.tensorflow.demo.FarmGlobal;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.farm.innovation.login.view.HomeActivity.isOPen;
import static org.tensorflow.demo.FarmCameraConnectionFragment.collectNumberHandler;


/**
 * Created by Luolu on 2018/9/18.
 * InnovationAI
 * luolu@innovationai.cn
 */
public class PayActivity extends BaseActivity {


    ImageView ivCancel;

    TextView tvTitle;

    TextView tvExit;

    RelativeLayout rlTitle;

    EditText checkedBaodanNo;
//    @BindView(R.id.idCardRadioButton)
//    RadioButton idCardRadioButton;
//    @BindView(R.id.idBusinessLicens)
//    RadioButton idBusinessLicens;
//    @BindView(R.id.certificateTypeRadioGroup)
//    RadioGroup certificateTypeRadioGroup;

    EditText etLipeiIdcard;

    Spinner payReasonSpinner;

    Spinner quSpinner;

    Spinner sheSpinner;

    Spinner lanSpinner;

    EditText animalEarsTagNo;

    Button payImageAcquisition;

    LinearLayout linearLayout;
    private int userId;
    private PayAnimalApplyTask payAnimalApplyTask;
    private String TAG = "PayActivity";
    private String errString;
    private PayInfoCheckResultBean payInfoCheckResultBean;
    private Gson gson;
//    private int certificateType = -1;
    private ResultBean bean;
    private String lipname;
     private DatabaseHelper databaseHelper;

    private String lipbaodanname;
    private String lipyanbaoname;
    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_pay;
    }

    @Override
    protected void initData() {
        ivCancel = (ImageView) findViewById(R.id.iv_cancel);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvExit = (TextView) findViewById(R.id.tv_exit);
        rlTitle = (RelativeLayout) findViewById(R.id.rl_title);
        checkedBaodanNo = (EditText) findViewById(R.id.checkedBaodanNo);
        etLipeiIdcard = (EditText) findViewById(R.id.etLipeiIdcard);
        payReasonSpinner = (Spinner) findViewById(R.id.payReasonSpinner);
        quSpinner = (Spinner) findViewById(R.id.quSpinner);
        sheSpinner = (Spinner) findViewById(R.id.sheSpinner);
        lanSpinner = (Spinner) findViewById(R.id.lanSpinner);
        animalEarsTagNo = (EditText) findViewById(R.id.animalEarsTagNo);
        payImageAcquisition = (Button) findViewById(R.id.payImageAcquisition);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        findViewById(R.id.savepay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.payImageAcquisition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });

        FarmGlobal.model = Model.VERIFY.value();
        tvTitle.setText(R.string.pay_apply_animal);
        ivCancel.setVisibility(View.VISIBLE);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String[] strReason = new String[]{"非传染病", "传染病（疫病）", "疫病/疾病免疫副反应", "中毒", "难产", "其它意外事故", "火灾", "风灾", "暴雨", "洪水", "内涝", "冷冻/冻灾", "雷电", "冰雹/雹灾", "地震", "爆炸", "建筑物倒塌/空中运行物体坠落", "泥石流/山体滑坡", "扑杀"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, strReason);
        payReasonSpinner.setAdapter(adapter);

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.area));
        quSpinner.setAdapter(arrayAdapter);
        quSpinner.setSelection(0, true);
        ArrayAdapter arrayAdapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.area2));
        sheSpinner.setAdapter(arrayAdapter2);
        sheSpinner.setSelection(0, true);
        ArrayAdapter arrayAdapter3 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.area3));
        lanSpinner.setAdapter(arrayAdapter3);
        lanSpinner.setSelection(0, true);
        SharedPreferences pref = getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        userId = pref.getInt("uid", 0);

        gson = new Gson();
        payInfoCheckResultBean = new PayInfoCheckResultBean();

        LocationManager.getInstance(PayActivity.this).startLocation();
        databaseHelper = DatabaseHelper.getInstance(this);
    }

    public void showProgressDialog(Activity activity) {
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIcon(R.drawable.farm_cowface);
        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "确定", mPOSITIVEClickListener);
        mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消", mNEGATIVEClickListener);
        mProgressDialog.setMessage("开始处理......");
        mProgressDialog.show();
        Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
        if (positive != null) {
            positive.setVisibility(View.GONE);
        }
        Button negative = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
        if (negative != null) {
            negative.setVisibility(View.GONE);
        }
    }

    private final DialogInterface.OnClickListener mPOSITIVEClickListener = (dialog, which) -> {
        dialog.dismiss();
        mProgressDialog = null;
    };

    private final DialogInterface.OnClickListener mNEGATIVEClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            mProgressDialog = null;
        }
    };

    private String strfleg = "";


    public void onViewClicked(View view) {
        int i = view.getId();/*case R.id.idCardRadioButton:
                certificateType = 1;
                break;
            case R.id.idBusinessLicens:
                certificateType = 2;
                break;
            case R.id.certificateTypeRadioGroup:
                certificateTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.id_card_radio_button:
                            certificateType = 1;
                            break;
                        case R.id.id_business_licens:
                            certificateType = 2;
                            break;
                    }
                });
                break;*///保存离线理赔单
        if (i == R.id.savepay) {
            if (!FarmAppConfig.isNetConnected) {
                Toast.makeText(PayActivity.this, "断网了，请联网后重试", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isOPen(PayActivity.this)) {
                payImageAcquisition.setEnabled(false);

                if (checkedBaodanNo.getText().toString().trim().matches("")) {
                    Toast.makeText(this, "保单号不能为空！", Toast.LENGTH_SHORT).show();
                    payImageAcquisition.setEnabled(true);
                    return;
                }
                String s = etLipeiIdcard.getText().toString().trim();
//                    String strIDcard = IDCardValidate.validateIDcardNumber(etLipeiIdcard.getText().toString().trim(), true);
                if (!(s.length() == 15 || s.length() == 18)) {
                    Toast.makeText(getApplicationContext(), "请输入正确证件号。", Toast.LENGTH_SHORT).show();
                    payImageAcquisition.setEnabled(true);
                    return;
                }
                    /*if (certificateType == -1) {
                        Toast.makeText(getApplicationContext(), "请选择证件类型", Toast.LENGTH_SHORT).show();
                        payImageAcquisition.setEnabled(true);
                        return;
                    }
                    if (certificateType == 1) {

                    } else if (certificateType == 2) {
                        if (!ValidatorUtils.isLicense18(etLipeiIdcard.getText().toString().trim())) {
                            Toast.makeText(getApplicationContext(), "请输入正确的统一社会信用代码", Toast.LENGTH_SHORT).show();
                            payImageAcquisition.setEnabled(true);
                            return;
                        }

                    }*/
                if (payReasonSpinner.getSelectedItem().toString().matches("")) {
                    Toast.makeText(this, "请选择出险原因！", Toast.LENGTH_SHORT).show();
                    payImageAcquisition.setEnabled(true);
                    return;
                }

                TreeMap treeMapInfoCheck = new TreeMap<String, String>();
                treeMapInfoCheck.put("baodanNoReal", checkedBaodanNo.getText().toString().trim());
                treeMapInfoCheck.put("reason", payReasonSpinner.getSelectedItem().toString().trim());
                treeMapInfoCheck.put("cardNo", etLipeiIdcard.getText().toString().trim());
                treeMapInfoCheck.put("uid", String.valueOf(userId) == null ? "" : String.valueOf(userId));
                treeMapInfoCheck.put("yiji", quSpinner.getSelectedItem().toString());
                treeMapInfoCheck.put("erji", sheSpinner.getSelectedItem().toString());
                treeMapInfoCheck.put("sanji", lanSpinner.getSelectedItem().toString());
                treeMapInfoCheck.put("pigNo", animalEarsTagNo.getText().toString().trim());
                FarmerPreferencesUtils.saveKeyValue("reason", payReasonSpinner.getSelectedItem().toString().trim(), PayActivity.this);
                FarmerPreferencesUtils.saveKeyValue("cardnum", etLipeiIdcard.getText().toString().trim(), PayActivity.this);
                FarmerPreferencesUtils.saveKeyValue("baodannum", checkedBaodanNo.getText().toString().trim(), PayActivity.this);
                new PayDataProcessor(PayActivity.this).transerPayData(
                        checkedBaodanNo.getText().toString().trim(),
                        payReasonSpinner.getSelectedItem().toString().trim(),
                        quSpinner.getSelectedItem().toString(),
                        sheSpinner.getSelectedItem().toString(),
                        lanSpinner.getSelectedItem().toString(),
                        animalEarsTagNo.getText().toString().trim(),
                        etLipeiIdcard.getText().toString().trim());
                strfleg = "save";
                showProgressDialog(PayActivity.this);
                payAnimalApplyTask = new PayAnimalApplyTask(HttpUtils.ANIMAL_PAY_INFOCHECK, treeMapInfoCheck);
                payAnimalApplyTask.execute((Void) null);
            } else {
                AlertDialogManager.showMessageDialog(PayActivity.this, "提示", getString(R.string.locationwarning), new AlertDialogManager.DialogInterface() {
                    @Override
                    public void onPositive() {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 1315);
                    }

                    @Override
                    public void onNegative() {

                    }
                });
            }


            //采集图像
        } else if (i == R.id.payImageAcquisition) {
            if (!FarmAppConfig.isNetConnected) {
                Toast.makeText(PayActivity.this, "断网了，请联网后重试", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isOPen(PayActivity.this)) {
                payImageAcquisition.setEnabled(false);

                if (checkedBaodanNo.getText().toString().trim().matches("")) {
                    Toast.makeText(this, "保单号不能为空！", Toast.LENGTH_SHORT).show();
                    payImageAcquisition.setEnabled(true);
                    return;
                }
                String s = etLipeiIdcard.getText().toString().trim();
//                    String strIDcard = IDCardValidate.validateIDcardNumber(etLipeiIdcard.getText().toString().trim(), true);
                if (!(s.length() == 15 || s.length() == 18)) {
                    Toast.makeText(getApplicationContext(), "请输入正确证件号。", Toast.LENGTH_SHORT).show();
                    payImageAcquisition.setEnabled(true);
                    return;
                }
                    /*if (certificateType == -1) {
                        Toast.makeText(getApplicationContext(), "请选择证件类型", Toast.LENGTH_SHORT).show();
                        payImageAcquisition.setEnabled(true);
                        return;
                    }
                    if (certificateType == 1) {
                        String strIDcard = IDCardValidate.validateIDcardNumber(etLipeiIdcard.getText().toString().trim(), true);
                        if (!(strIDcard.length() == 15 || strIDcard.length() == 18)) {
                            Toast.makeText(getApplicationContext(), strIDcard, Toast.LENGTH_SHORT).show();
                            payImageAcquisition.setEnabled(true);
                            return;
                        }
                    } else if (certificateType == 2) {
                        if (!ValidatorUtils.isLicense18(etLipeiIdcard.getText().toString().trim())) {
                            Toast.makeText(getApplicationContext(), "请输入正确的统一社会信用代码", Toast.LENGTH_SHORT).show();
                            payImageAcquisition.setEnabled(true);
                            return;
                        }

                    }*/
                if (payReasonSpinner.getSelectedItem().toString().matches("")) {
                    Toast.makeText(this, "请选择出险原因！", Toast.LENGTH_SHORT).show();
                    payImageAcquisition.setEnabled(true);
                    return;
                }

                TreeMap treeMapInfoCheck = new TreeMap<String, String>();
                treeMapInfoCheck.put("baodanNoReal", checkedBaodanNo.getText().toString().trim());
                treeMapInfoCheck.put("reason", payReasonSpinner.getSelectedItem().toString().trim());
                treeMapInfoCheck.put("cardNo", etLipeiIdcard.getText().toString().trim());
                treeMapInfoCheck.put("uid", String.valueOf(userId) == null ? "" : String.valueOf(userId));
                treeMapInfoCheck.put("yiji", quSpinner.getSelectedItem().toString());
                treeMapInfoCheck.put("erji", sheSpinner.getSelectedItem().toString());
                treeMapInfoCheck.put("sanji", lanSpinner.getSelectedItem().toString());
                treeMapInfoCheck.put("pigNo", animalEarsTagNo.getText().toString().trim());

                new PayDataProcessor(PayActivity.this).transerPayData(
                        checkedBaodanNo.getText().toString().trim(),
                        payReasonSpinner.getSelectedItem().toString().trim(),
                        quSpinner.getSelectedItem().toString(),
                        sheSpinner.getSelectedItem().toString(),
                        lanSpinner.getSelectedItem().toString(),
                        animalEarsTagNo.getText().toString().trim(),
                        etLipeiIdcard.getText().toString().trim());
                strfleg = "collect";
                FarmerPreferencesUtils.saveKeyValue("reason", payReasonSpinner.getSelectedItem().toString().trim(), PayActivity.this);
                FarmerPreferencesUtils.saveKeyValue("cardnum", etLipeiIdcard.getText().toString().trim(), PayActivity.this);
                FarmerPreferencesUtils.saveKeyValue("baodannum", checkedBaodanNo.getText().toString().trim(), PayActivity.this);
                showProgressDialog(PayActivity.this);
                payAnimalApplyTask = new PayAnimalApplyTask(HttpUtils.ANIMAL_PAY_INFOCHECK, treeMapInfoCheck);
                payAnimalApplyTask.execute((Void) null);
            } else {
                AlertDialogManager.showMessageDialog(PayActivity.this, "提示", getString(R.string.locationwarning), new AlertDialogManager.DialogInterface() {
                    @Override
                    public void onPositive() {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 1315);
                    }

                    @Override
                    public void onNegative() {

                    }
                });
            }

        } else {
        }
    }


    class PayAnimalApplyTask extends AsyncTask<Void, Void, Boolean> {
        private final String mUrl;
        private final Map<String, String> mQueryMap;

        PayAnimalApplyTask(String url, Map<String, String> map) {
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
                Log.e(TAG, "理赔信息校验请求报文:" + mQueryMap.toString());
                RequestBody formBody = builder.build();
                String infoCheckResponse = HttpUtils.post(mUrl, formBody);
                if (mProgressDialog != null) {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.cancel();
                    }
                }
                if (null != infoCheckResponse) {
                    bean = gson.fromJson(infoCheckResponse, ResultBean.class);
                    Log.d(TAG, mUrl + "\ninfoCheckResponse:" + infoCheckResponse);
                    if (bean.getStatus() == 1) {
                        Log.d(TAG, "bean.getStatus():" + bean.getStatus());
                        if (null != bean.getData() && !bean.getData().equals("")) {
                            Map<String, String> data = (Map<String, String>) bean.getData();
                            lipname = data.get("name");
                            lipbaodanname = data.get("baodanName");
                            lipyanbaoname = data.get("yanbiaoName");
                        }
                        FarmerPreferencesUtils.saveKeyValue("insurename", lipname, PayActivity.this);
                        return true;
                    } else if (bean.getStatus() == 0) {
                        Log.d(TAG, "bean.getStatus():" + bean.getStatus());
                        payInfoCheckHandler.sendEmptyMessage(12);
                        return false;
                    } else {
                        Log.d(TAG, "bean.getStatus():" + bean.getStatus());
                        payInfoCheckHandler.sendEmptyMessage(13);
                        return false;
                    }
                } else {
                    payInfoCheckHandler.sendEmptyMessage(14);
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                errString = "服务器错误！";
                if (mProgressDialog != null) {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.cancel();
                    }
                }
                AVOSCloudUtils.saveErrorMessage(e, PayActivity.class.getSimpleName());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            payAnimalApplyTask = null;
            if (success & HttpUtils.ANIMAL_PAY_INFOCHECK.equalsIgnoreCase(mUrl)) {
                if (strfleg.equals("save")) {
                    String baodanno = checkedBaodanNo.getText().toString().trim();
                    String cardnum = etLipeiIdcard.getText().toString().trim();
                    String payreason = payReasonSpinner.getSelectedItem().toString();
                    String qu = quSpinner.getSelectedItem().toString();
                    String she = sheSpinner.getSelectedItem().toString();
                    String lan = lanSpinner.getSelectedItem().toString();
                    String Ears = animalEarsTagNo.getText().toString();

                    SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss", Locale.getDefault());
                    String lipeidate = mDateFormat.format(new Date(System.currentTimeMillis()));
                    Log.i("===lipeidate===", lipeidate);
                    String s = "投保人：" + lipname + " ,请确认";
                    LiPeiLocalBean liPeiLocalBean = new LiPeiLocalBean(baodanno, lipname,
                            cardnum, payreason, qu + "$," + she + "$," + lan + "$",
                            lipeidate, LocationManager.getInstance(PayActivity.this).currentLon + "",
                            LocationManager.getInstance(PayActivity.this).currentLat + "",
                            String.valueOf(FarmerPreferencesUtils.getAnimalType(PayActivity.this))
                            , Ears, "", "1", s, "",lipbaodanname,lipyanbaoname, "0", "0", "0");
                    databaseHelper.addLiPeiLocalData(liPeiLocalBean);
                    Log.d(TAG, "理赔信息校验接口，校验通过");
                 /*   Intent infoCheckIntent = new Intent(PayActivity.this, HomeActivity.class);
                    startActivity(infoCheckIntent);*/
                    finish();
                } else if (("collect").equals(strfleg)) {

                    FarmerPreferencesUtils.saveBooleanValue("isli", false, PayActivity.this);
                    FarmerPreferencesUtils.saveBooleanValue(HttpUtils.offlineupdate, false, PayActivity.this);
                    Intent infoCheckIntent = new Intent(PayActivity.this, FarmDetectorActivity.class);
                    startActivity(infoCheckIntent);
                    collectNumberHandler.sendEmptyMessage(2);
                    finish();
                }
            } else if (!success) {
                // 失败
                errString = "理赔单提交失败！";
                Log.d(TAG, errString);
                payImageAcquisition.setEnabled(true);
            }
        }

        @Override
        protected void onCancelled() {
            payAnimalApplyTask = null;
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler payInfoCheckHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (msg.what) {
                case 12:
                case 13:
                    AlertDialog.Builder builder13 = new AlertDialog.Builder(PayActivity.this)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage(bean.getMsg())
                            .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder13.create();
                    builder13.setCancelable(false);
                    builder13.show();
                    break;
                case 14:
                    AlertDialog.Builder builder14 = new AlertDialog.Builder(PayActivity.this)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("服务异常，请稍后再试！")
                            .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder14.create();
                    builder14.setCancelable(false);
                    builder14.show();
                    break;
                case 15:
                    AlertDialog.Builder builder15 = new AlertDialog.Builder(PayActivity.this)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("无法连接服务器，请稍后再试！")
                            .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder15.create();
                    builder15.setCancelable(false);
                    builder15.show();
                    break;
                default:
                    break;
            }
        }
    };
}
