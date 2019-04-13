package com.farm.innovation.biz.insurance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.location.LocationManager;
import com.farm.innovation.login.Utils;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.ConstUtils;
import com.farm.innovation.utils.EditTextJudgeNumberWatcher;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.OkHttp3Util;
import com.farm.innovation.utils.PreferencesUtils;
import com.innovation.pig.insurance.R;

import org.json.JSONObject;
import org.tensorflow.demo.FarmGlobal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.farm.innovation.utils.MyTextUtil.isEmojiCharacter;
import static com.farm.innovation.utils.MyTextUtil.replaceBlank;


/**
 * Created by Luolu on 2018/9/19.
 * InnovationAI
 * luolu@innovationai.cn
 */
public class InsuranceAcitivity extends BaseActivity {
    public static String TAG = "InsuranceAcitivity";

    ImageView ivCancel;

    TextView tvTitle;

    TextView tvExit;

    RelativeLayout rlTitle;

    EditText baodanName;

    RadioButton radioButtonEnterprise;

    RadioButton radioButtonOrganization;

    RadioGroup radioGroupBaodanType;

    Spinner spinnerInsuranceType;

    EditText insuranceRate;

    RadioButton scaleFarming;

    RadioButton freeRangeFarming;

    RadioGroup farmFormRadioGroup;

    EditText unitInsuranceCost;

    EditText baodanApplyAddress;

    EditText baodanApplyName;

    Button btnNext;

    Button btnFinish;

    LinearLayout lin_yan;
    private int baodanType;
    private AMapLocationClient mLocationClient;
    private int farmForm = -1;
    private String mTempToubaoNumber;
    private String strtype;
    private int insuredType;
    private String sbaodanName;
    private String sinsuranceRate;
    private String sunitInsuranceCost;
    private String sbaodanApplyAddress;
    private String sbaodanApplyName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void initView() {
        super.initView();
        ivCancel = (ImageView) findViewById(R.id.iv_cancel);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvExit = (TextView) findViewById(R.id.tv_exit);
        rlTitle = (RelativeLayout) findViewById(R.id.rl_title);
        baodanName = (EditText) findViewById(R.id.baodanName);
        radioButtonEnterprise = (RadioButton) findViewById(R.id.radioButtonEnterprise);
        radioButtonOrganization = (RadioButton) findViewById(R.id.radioButtonOrganization);
        radioGroupBaodanType = (RadioGroup) findViewById(R.id.radioGroupBaodanType);
        spinnerInsuranceType = (Spinner) findViewById(R.id.spinnerInsuranceType);
        insuranceRate = (EditText) findViewById(R.id.insuranceRate);
        scaleFarming = (RadioButton) findViewById(R.id.scaleFarming);
        freeRangeFarming = (RadioButton) findViewById(R.id.freeRangeFarming);
        farmFormRadioGroup = (RadioGroup) findViewById(R.id.farmFormRadioGroup);
        unitInsuranceCost = (EditText) findViewById(R.id.unitInsuranceCost);
        baodanApplyAddress = (EditText) findViewById(R.id.baodanApplyAddress);
        baodanApplyName = (EditText) findViewById(R.id.baodanApplyName);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnFinish = (Button) findViewById(R.id.btnFinish);
        lin_yan = (LinearLayout) findViewById(R.id.lin_yan);
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.btnFinish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_insurance;
    }

    @Override
    protected void initData() {
        FarmGlobal.model = Model.BUILD.value();
        tvTitle.setText("新建保单");
        ivCancel.setVisibility(View.VISIBLE);
        FarmGlobal.model = Model.BUILD.value();
        radioGroupBaodanType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonEnterprise) {
                baodanType = 1;
                btnNext.setVisibility(View.VISIBLE);
                btnFinish.setVisibility(View.GONE);
                //lin_yan.setVisibility(View.VISIBLE);

            } else if (checkedId == R.id.radioButtonOrganization) {
                baodanType = 2;
                btnFinish.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.GONE);
                // lin_yan.setVisibility(View.GONE);

            } else {
            }
        });

        farmFormRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.scaleFarming) {
                farmForm = 1;
                strtype = "规模化养殖";

            } else if (checkedId == R.id.freeRangeFarming) {
                farmForm = 2;
                strtype = "散养";

            } else {
            }
        });
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ConstUtils.geInsureTypeCaptions(PreferencesUtils.getAnimalType(InsuranceAcitivity.this)));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInsuranceType.setAdapter(arrayAdapter);
        //getCurrentLocationLatLng();
        LocationManager instance = LocationManager.getInstance(InsuranceAcitivity.this);
        instance.startLocation();
        instance.setAddress(new LocationManager.GetAddress() {
            @Override
            public void getaddress(String address) {
                if ("".equals(baodanApplyAddress.getText().toString()) || "未知地址".equals(baodanApplyAddress.getText().toString())) {
                    baodanApplyAddress.setText(address);
                }

            }
        });
        String str_random1 = createCode();
        mTempToubaoNumber = stampToDate(System.currentTimeMillis()) + str_random1;
        insuranceRate.addTextChangedListener(new EditTextJudgeNumberWatcher(insuranceRate));
    }

    public String str = "";


    public void onViewClicked(View view) {
        int i = view.getId();
        if (i == R.id.btnNext) {
            str = "qiye";
            afterClicked();

        } else if (i == R.id.btnFinish) {
            str = "zuzhi";
            afterClicked();

        } else if (i == R.id.iv_cancel) {
            finish();

        } else {
        }
    }

    /**
     * 点击后校验输入信息合法性
     * saveMeaasge(); 保存相关信息
     * chackName(); 请求接口
     */
    private void afterClicked(){
        if (isEmo()) {
            Toast.makeText(InsuranceAcitivity.this, "保单名称不能包含特殊字符", Toast.LENGTH_LONG).show();
            return;
        }
        //过滤掉字符串中的回车换行
        sbaodanName = replaceBlank(sbaodanName);

        //新建企业验标单
        saveMeaasge();
        if ("".equals(sbaodanName)) {
            Toast.makeText(InsuranceAcitivity.this, "保单为空", Toast.LENGTH_LONG).show();
        } else if ("".equals(sinsuranceRate)) {
            Toast.makeText(InsuranceAcitivity.this, "保险费率为空", Toast.LENGTH_LONG).show();
        } else if (Double.valueOf(sinsuranceRate) > 10 || Double.valueOf(sinsuranceRate) <= 0) {
            Toast.makeText(InsuranceAcitivity.this, "非法的费率格式", Toast.LENGTH_LONG).show();
        } else if (farmForm == -1) {
            Toast.makeText(getApplicationContext(), "请选择饲养方式", Toast.LENGTH_SHORT).show();
        } else if ("".equals(sunitInsuranceCost)) {
            Toast.makeText(InsuranceAcitivity.this, "保险金额为空", Toast.LENGTH_LONG).show();
        } else if ("".equals(sbaodanApplyAddress)) {
            Toast.makeText(InsuranceAcitivity.this, "地址为空", Toast.LENGTH_LONG).show();
        } else {
            chackName();
        }
    }

    private void chackName() {
        Map<String, String> map = new HashMap<>();
        map.put(HttpUtils.AppKeyAuthorization, "hopen");
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put(HttpUtils.deptId, PreferencesUtils.getIntValue(HttpUtils.deptId, FarmAppConfig.getApplication()) + "");
        mapbody.put("baodanName", PreferencesUtils.getStringValue(HttpUtils.baodanName, FarmAppConfig.getApplication()));
        OkHttp3Util.doPost(HttpUtils.BaoDannametest, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e, InsuranceAcitivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(string);
                            int status = jsonObject.getInt("status");
                            String msg = jsonObject.getString("msg");
                            if (status == -1) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        showDialogError(msg);
                                    }
                                });
                            } else if (status == 0) {
                                Toast.makeText(InsuranceAcitivity.this, msg, Toast.LENGTH_LONG).show();
                            } else if (status == 1) {
                                // Toast.makeText(InsuranceAcitivity.this, msg, Toast.LENGTH_LONG).show();
                                if ("qiye".equals(str)) {
                                    goToActivity(EnterpriseBaodanActivity.class, null);
                                    finish();
                                } else if ("zuzhi".equals(str)) {
                                    createDan();
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            AVOSCloudUtils.saveErrorMessage(e, InsuranceAcitivity.class.getSimpleName());
                        }
                    }
                });


            }
        });
    }

    private void createDan() {
        Map<String, String> map = new HashMap<>();
        map.put(HttpUtils.AppKeyAuthorization, "hopen");
//        String id = PreferencesUtils.getStringValue(HttpUtils.id, FarmAppConfig.getApplication());
//        Log.i("id====",id+"");
        SharedPreferences pref = getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        int userid = pref.getInt("uid", 0);
        map.put(HttpUtils.id, userid + "");
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put("baodanNo", mTempToubaoNumber);
        mapbody.put("baodanName", PreferencesUtils.getStringValue(HttpUtils.baodanName, FarmAppConfig.getApplication()));
        mapbody.put("baodanType", PreferencesUtils.getStringValue(HttpUtils.baodanType, FarmAppConfig.getApplication()));
        mapbody.put("animalType", String.valueOf(PreferencesUtils.getAnimalType(InsuranceAcitivity.this)));
        mapbody.put("toubaoType", PreferencesUtils.getStringValue(HttpUtils.InsuranceType, FarmAppConfig.getApplication()));
        mapbody.put("baodanRate", PreferencesUtils.getStringValue(HttpUtils.insuranceRate, FarmAppConfig.getApplication()));
        mapbody.put("shiyangMethod", PreferencesUtils.getStringValue(HttpUtils.farmForm, FarmAppConfig.getApplication()));
        mapbody.put("toubaoCost", PreferencesUtils.getStringValue(HttpUtils.InsuranceCost, FarmAppConfig.getApplication()));
        mapbody.put("address", baodanApplyAddress.getText().toString().trim());
        mapbody.put("uid", userid + "");
        mapbody.put(HttpUtils.deptId, PreferencesUtils.getIntValue(HttpUtils.deptId, FarmAppConfig.getApplication()) + "");

        mapbody.put("longitude", String.valueOf(LocationManager.getInstance(InsuranceAcitivity.this).currentLat));
        mapbody.put("latitude", String.valueOf(LocationManager.getInstance(InsuranceAcitivity.this).currentLon));

        OkHttp3Util.doPost(HttpUtils.BaoDanadd, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e, InsuranceAcitivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(string);
                            int status = jsonObject.getInt("status");
                            String msg = jsonObject.getString("msg");
                            if (status == -1) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        showDialogError(msg);
                                    }
                                });
                            } else if (status == 0) {
                                Toast.makeText(InsuranceAcitivity.this, msg, Toast.LENGTH_LONG).show();
                            } else if (status == 1) {
                                Toast.makeText(InsuranceAcitivity.this, msg, Toast.LENGTH_LONG).show();
                                finish();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            AVOSCloudUtils.saveErrorMessage(e, InsuranceAcitivity.class.getSimpleName());
                        }
                    }
                });


            }
        });
    }

    /**
     * 验证字符串是否包含表情符号
     */
    private boolean isEmo() {
        sbaodanName = baodanName.getText().toString();
        boolean isEmo = false;
        for (int i = 0; i < sbaodanName.length(); i++) {
            isEmo = isEmojiCharacter(sbaodanName.charAt(i));
            if (isEmo) {
                break;
            }
        }
        return isEmo;
    }

    /**
     * 保存相关信息
     */
    private void saveMeaasge() {
        PreferencesUtils.saveKeyValue(HttpUtils.baodanName, sbaodanName, FarmAppConfig.getApplication());
        PreferencesUtils.saveKeyValue(HttpUtils.baodanType, baodanType + "", FarmAppConfig.getApplication());
        if (spinnerInsuranceType != null) {
            String sXZ = spinnerInsuranceType.getSelectedItem().toString();
            insuredType = ConstUtils.getInsureTypeCodeIntByCaption(sXZ);
        }
        PreferencesUtils.saveKeyValue(HttpUtils.InsuranceType, insuredType + "", FarmAppConfig.getApplication());
        sinsuranceRate = insuranceRate.getText().toString().trim();
        PreferencesUtils.saveKeyValue(HttpUtils.insuranceRate, sinsuranceRate, FarmAppConfig.getApplication());
        PreferencesUtils.saveKeyValue(HttpUtils.farmForm, strtype + "", FarmAppConfig.getApplication());
        sunitInsuranceCost = unitInsuranceCost.getText().toString();
        PreferencesUtils.saveKeyValue(HttpUtils.InsuranceCost, sunitInsuranceCost, FarmAppConfig.getApplication());
        sbaodanApplyAddress = baodanApplyAddress.getText().toString();
        PreferencesUtils.saveKeyValue(HttpUtils.baodanApplyAddress, sbaodanApplyAddress, FarmAppConfig.getApplication());
        sbaodanApplyName = baodanApplyName.getText().toString();
        PreferencesUtils.saveKeyValue(HttpUtils.baodanApplyName, sbaodanApplyName, FarmAppConfig.getApplication());

    }


}
