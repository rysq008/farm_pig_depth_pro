package com.xiangchuang.risks.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.JuanSetting_item_Adapter;
import com.xiangchuang.risks.model.bean.JuanSTBean;
import com.xiangchuang.risks.model.myinterface.MyInterface;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class JuanSettingActivity extends BaseActivity {
    public static String TAG = "JuanSettingActivity";

    ListView setting_listview;

    TextView mxu;

    TextView mtouname;

    TextView mshename;

    ImageView iv_cancel;
    List<JuanSTBean> juanSTBeans = new ArrayList<>();

    @Override
    public void initView() {
        super.initView();
        setting_listview = (ListView) findViewById(R.id.setting_listview);
        mxu = (TextView) findViewById(R.id.xu);
        mtouname = (TextView) findViewById(R.id.touname);
        mshename = (TextView) findViewById(R.id.mshename);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_juan_setting;
    }

    @Override
    protected void initData() {
        String xu = PreferencesUtils.getStringValue(Constants.xu, AppConfig.getAppContext(), "0");
        String touname = PreferencesUtils.getStringValue(Constants.touname, AppConfig.getAppContext(), "0");
        String shename = PreferencesUtils.getStringValue(Constants.shename, AppConfig.getAppContext(), "0");
        String sheId = PreferencesUtils.getStringValue(Constants.sheId, AppConfig.getAppContext(), "0");
        String cameraId = PreferencesUtils.getStringValue(Constants.cameraId, AppConfig.getAppContext(), "0");
        mxu.setText(xu);
        mtouname.setText(touname);
        mshename.setText(shename);
        getDataFromNet(sheId, cameraId);
    }

    private void getDataFromNet(String sheId, String cameraId) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext()) + "");
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0"));
        map.put(Constants.deptIdnew, PreferencesUtils.getStringValue(Constants.deptId, AppConfig.getAppContext()));
        map.put(Constants.id, PreferencesUtils.getStringValue(Constants.id, AppConfig.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.cameraId, cameraId);
        mapbody.put(Constants.sheId, sheId);
        OkHttp3Util.doPost(Constants.CAMERALIST, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e,JuanSettingActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                juanSTBeans.clear();
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                showDialogError(msg);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONArray datas = jsonObject.getJSONArray("data");
                                    for (int i = 0; i < datas.length(); i++) {
                                        JSONObject jsonObject1 = datas.getJSONObject(i);
                                        int operation = jsonObject1.getInt("operation");
                                        String cameraName = jsonObject1.getString("cameraName");
                                        String name = jsonObject1.getString("name");
                                        int juanId = jsonObject1.getInt("juanId");
                                        JuanSTBean juanSTBean = new JuanSTBean(operation, cameraName, name, juanId);
                                        juanSTBeans.add(juanSTBean);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                JuanSetting_item_Adapter juanSetting_item_adapter = new JuanSetting_item_Adapter(JuanSettingActivity.this, juanSTBeans);
                                setting_listview.setAdapter(juanSetting_item_adapter);
                                juanSetting_item_adapter.setListner(new MyInterface() {
                                    @Override
                                    public void isOut(Boolean aBoolean) {
                                        if (aBoolean) {
                                            Log.i("=sheId==", sheId);
                                            Log.i("=cameraId==", cameraId);
                                            getDataFromNet(sheId, cameraId);
                                        }
                                    }
                                });
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e,JuanSettingActivity.class.getSimpleName());
                }


            }
        });
    }


    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.iv_cancel:
                finish();
                break;
            default:
                break;
        }

    }

}
