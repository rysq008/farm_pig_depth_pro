package com.farm.innovation.biz.insurance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.location.AlertDialogManager;
import com.farm.innovation.login.Utils;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.OkHttp3Util;
import com.innovation.pig.insurance.R;

import org.json.JSONObject;
import org.tensorflow.demo.FarmDetectorActivity;
import org.tensorflow.demo.FarmGlobal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.farm.innovation.login.view.HomeActivity.isOPen;
import static com.farm.innovation.utils.MyTextUtil.isEmojiCharacter;
import static com.farm.innovation.utils.MyTextUtil.replaceBlank;

public class CreateYanActivity extends BaseActivity {
    public static String TAG = "CreateYanActivity";

    EditText baodanApplyName;

    Button btnyanNext;

    Button btncaiji;

    Button btnyanWan;

    TextView tv_title;

    ImageView iv_cancel;
    private String mTempToubaoNumber;
    private AMapLocationClient mLocationClient;
    private int userid;
    private String str = "";
    private String bdApplyName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void initView() {
        super.initView();
        baodanApplyName = (EditText) findViewById(R.id.baodanApplyName);
        btnyanNext = (Button) findViewById(R.id.btnyanNext);
        btncaiji = (Button) findViewById(R.id.btncaiji);
        btnyanWan = (Button) findViewById(R.id.btnyanWan);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_create_yan;
    }

    @Override
    protected void initData() {
        tv_title.setText("新建验标单");
        SharedPreferences pref = CreateYanActivity.this.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        userid = pref.getInt("uid", 0);
        FarmGlobal.model = Model.BUILD.value();
        iv_cancel.setVisibility(View.VISIBLE);
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String stringValue = FarmerPreferencesUtils.getStringValue(HttpUtils.baodanType, FarmAppConfig.getApplication());
        if ("1".equals(stringValue)) {
            btnyanWan.setVisibility(View.VISIBLE);
            btncaiji.setVisibility(View.VISIBLE);
            btnyanNext.setVisibility(View.GONE);
        } else if ("2".equals(stringValue)) {
            btnyanWan.setVisibility(View.GONE);
            btncaiji.setVisibility(View.GONE);
            btnyanNext.setVisibility(View.VISIBLE);
        }
        btnyanWan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bdApplyName = baodanApplyName.getText().toString().trim();
                Log.e(TAG, "bdApplyName: "+bdApplyName );
                //过滤回车换行
                bdApplyName = replaceBlank(bdApplyName);

                if ("".equals(bdApplyName)) {
                    Toast.makeText(CreateYanActivity.this, "验标单为空", Toast.LENGTH_LONG).show();
                    return;
                }
                if(isEmo(bdApplyName)){
                    Toast.makeText(CreateYanActivity.this, "验标单名称不能包含特殊字符", Toast.LENGTH_LONG).show();
                    return;
                }

                String str_random1 = createCode();
                mTempToubaoNumber = stampToDate(System.currentTimeMillis()) + str_random1;
                str = "wancheng";
                createYan();

            }
        });
        btnyanNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bdApplyName = baodanApplyName.getText().toString().trim();
                //过滤回车换行
                bdApplyName = replaceBlank(bdApplyName);
                if ("".equals(bdApplyName)) {
                    Toast.makeText(CreateYanActivity.this, "验标单为空", Toast.LENGTH_LONG).show();
                    return;
                }
                if (isEmo(bdApplyName)) {
                    Toast.makeText(CreateYanActivity.this, "验标单名称不能包含特殊字符", Toast.LENGTH_LONG).show();
                    return;
                }
                FarmerPreferencesUtils.saveKeyValue("zuYan", bdApplyName, CreateYanActivity.this);
                goToActivity(OrganizationBaodanActivity.class, null);
                finish();

            }
        });
        btncaiji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bdApplyName = baodanApplyName.getText().toString().trim();
                //过滤回车换行
                bdApplyName = replaceBlank(bdApplyName);
                if (isOPen(CreateYanActivity.this)) {
                    if ("".equals(bdApplyName)) {
                        Toast.makeText(CreateYanActivity.this, "验标单为空", Toast.LENGTH_LONG).show();
                    } else {
                        if(isEmo(bdApplyName)){
                            Toast.makeText(CreateYanActivity.this, "验标单名称不能包含特殊字符", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String str_random1 = createCode();
                        mTempToubaoNumber = stampToDate(System.currentTimeMillis()) + str_random1;
                        str = "caiji";
                        createYan();
                    }
                } else {
                    openGPS1(CreateYanActivity.this);
                }

            }
        });
        getCurrentLocationLatLng();
    }


    /**
     * 验证字符串是否包含表情符号
     */
    private boolean isEmo(String s) {
        boolean isEmo = false;
        for (int i = 0; i < s.length(); i++) {
            isEmo = isEmojiCharacter(s.charAt(i));
            if (isEmo) {
                break;
            }
        }
        return isEmo;
    }

    private void openGPS1(Context mContext) {
        AlertDialogManager.showMessageDialog(mContext, "提示", getString(R.string.locationwarning), new AlertDialogManager.DialogInterface() {
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

    private void createYan() {
        Map<String, String> map = new HashMap<>();
        map.put(HttpUtils.AppKeyAuthorization, "hopen");
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put("baodanNo", mTempToubaoNumber);
        mapbody.put("bdsId", FarmerPreferencesUtils.getStringValue(HttpUtils.id, FarmAppConfig.getApplication()));
        mapbody.put("longitude", String.valueOf(currentLat));
        mapbody.put("latitude", String.valueOf(currentLon));
        mapbody.put("yanBiaoName", bdApplyName);
        mapbody.put("uid", String.valueOf(userid));
        FarmAppConfig.getStringTouboaExtra = String.valueOf(mTempToubaoNumber);

        OkHttp3Util.doPost(HttpUtils.BaoDanaddyan, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e, CreateYanActivity.class.getSimpleName());
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
                                Toast.makeText(CreateYanActivity.this, msg, Toast.LENGTH_LONG).show();
                            } else if (status == 1) {
                                Toast.makeText(CreateYanActivity.this, msg, Toast.LENGTH_LONG).show();
                                if ("wancheng".equals(str)) {
                                    Intent intent = new Intent(CreateYanActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if ("caiji".equals(str)) {
                                    Intent intent = new Intent(CreateYanActivity.this, FarmDetectorActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            AVOSCloudUtils.saveErrorMessage(e, CreateYanActivity.class.getSimpleName());
                        }
                    }
                });


            }
        });
    }

    private void getCurrentLocationLatLng() {
        //初始化定位
        mLocationClient = new AMapLocationClient(CreateYanActivity.this);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setOnceLocation(true);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。默认连续定位 切最低时间间隔为1000ms
        mLocationOption.setInterval(3500);
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    private double currentLat;
    private double currentLon;
    private String str_address = "";
    private final AMapLocationListener mLocationListener = amapLocation -> {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                currentLat = amapLocation.getLatitude();//获取纬度
                currentLon = amapLocation.getLongitude();//获取经度
                //  str_address = amapLocation.getAddress();
                str_address = mLocationClient.getLastKnownLocation().getAddress();
                ;

                amapLocation.getAccuracy();//获取精度信息
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    };

}
