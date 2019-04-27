package com.farm.innovation.biz.insurance;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.BaoDanBeanNew;
import com.farm.innovation.biz.insurance.adapter.YanBiaoAdapter;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.OkHttp3Util;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.innovation.pig.insurance.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.demo.FarmGlobal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class YanBiaoDanActivity extends BaseActivity {

    public static String TAG = "YanBiaoDanActivity";

    ListView yanbiaodan_listview;

    TextView tv_title;

    ImageView ivCancel;

    ImageView btn_yanbiao_add;
    private int deptId;
    List<BaoDanBeanNew> baoDanBeanNewList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        super.initView();
        yanbiaodan_listview = (ListView) findViewById(R.id.yanbiaodan_listview);
        tv_title = (TextView) findViewById(R.id.tv_title);
        ivCancel = (ImageView) findViewById(R.id.iv_cancel);
        btn_yanbiao_add = (ImageView) findViewById(R.id.btn_yanbiao_add);
        findViewById(R.id.btn_yanbiao_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_yan_biao_dan;
    }

    @Override
    protected void initData() {
        tv_title.setText("保单列表");
        deptId = FarmerPreferencesUtils.getIntValue(HttpUtils.deptId, FarmAppConfig.getApplication());
        FarmGlobal.model = Model.BUILD.value();
        getDataFromNet();
        ivCancel.setVisibility(View.VISIBLE);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void getDataFromNet() {
        Map<String, String> map = new HashMap<>();
        map.put(HttpUtils.AppKeyAuthorization, "hopen");
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put(HttpUtils.deptId, deptId + "");
        Log.i("deptId", deptId + "");
        OkHttp3Util.doPost(HttpUtils.BaoDanList, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e, YanBiaoDanActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
//                                showDialogError(msg);
                            }
                        });
                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    baoDanBeanNewList.clear();
                                    JSONArray datas = jsonObject.getJSONArray("data");
                                    for (int i = 0; i < datas.length(); i++) {
                                        JSONObject jsonObject1 = datas.getJSONObject(i);
                                        String bankName = jsonObject1.getString("baodanName");
                                        String createtime = jsonObject1.getString("createtime");
                                        String toubaoPname = jsonObject1.getString("name");
                                        String toubaoTypeString = jsonObject1.getString("toubaoTypeString");
                                        int baodanType = jsonObject1.getInt("baodanType");
                                        int id = jsonObject1.getInt("id");
                                        double baodanRate = jsonObject1.getDouble("baodanRate");
                                        BaoDanBeanNew baoDanBeanNew = new BaoDanBeanNew(bankName, baodanType, id, createtime, toubaoPname, baodanRate,toubaoTypeString);
                                        baoDanBeanNewList.add(baoDanBeanNew);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                yanbiaodan_listview.setAdapter(new YanBiaoAdapter(YanBiaoDanActivity.this, baoDanBeanNewList));
                              /*  yanbiaodan_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        FarmerPreferencesUtils.saveKeyValue(HttpUtils.baodanType, baoDanBeanNewList.get(position).baodanType + "", FarmAppConfig.getApplication());
                                        FarmerPreferencesUtils.saveKeyValue(HttpUtils.id, baoDanBeanNewList.get(position).id + "", FarmAppConfig.getApplication());
                                        // activity.goToActivity(HomeActivity.class, null);
                                        goToActivity(CreateYanActivity.class, null);
                                    }
                                });*/
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e, YanBiaoDanActivity.class.getSimpleName());
                }


            }
        });
    }


    public void onClickView(View view) {
        int i = view.getId();
        if (i == R.id.iv_cancel) {
            finish();

        } else if (i == R.id.btn_yanbiao_add) {
            goToActivity(InsuranceAcitivity.class, null);

        } else {
        }

    }
}
