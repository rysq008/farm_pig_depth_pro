package com.xiangchuang.risks.view;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.OutHurdleAdapter;
import com.xiangchuang.risks.model.bean.ZhuJuanBean;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OutHurdleActivity extends BaseActivity {
    public static String TAG = "OutHurdleActivity";
    @BindView(R.id.chulan_zhushe_spinner)
    Spinner mchulanzhushespinner;
    @BindView(R.id.chulan_list_view)
    ListView mchulanlistview;
    List<String> chulanzhushe = new ArrayList<>();
    List<Object> PigHousebean = new ArrayList<>();
    private int defaultpig;
    private String en_id;
    private int userid;
    private ZhuSheBean bean;
    private String chuShe;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_out_hurdle;
    }

    @Override
    protected void initData() {
        defaultpig = PreferencesUtils.getIntValue(Constants.defaultpig, MyApplication.getAppContext());
        en_id = PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0");
        userid = PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext());
        //查询猪舍信息
        getDataFromNet();
    }

    private void getDataFromNet() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(userid));
        map.put(Constants.en_id, en_id);
        Map mapbody = new HashMap();
        mapbody.put(Constants.amountFlg, String.valueOf(9));
        mapbody.put(Constants.insureFlg, String.valueOf(9));
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUSHESHOW, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                bean = GsonUtils.getBean(string, ZhuSheBean.class);
                if (null != bean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                for (int i = 0; i < bean.getData().size(); i++) {
                                    chulanzhushe.add(bean.getData().get(i).getName());
                                }
                                initSpinner();
                                chuShe = chulanzhushe.get(0);
                                getzhujuanMessage(defaultpig, chuShe);
                            } else if (bean.getStatus() == 0) {
                                Toast.makeText(OutHurdleActivity.this, bean.getMsg(), Toast.LENGTH_LONG).show();
                            } else if (bean.getStatus() == -1) {
                                Toast.makeText(OutHurdleActivity.this, bean.getMsg(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OutHurdleActivity.this, "查询失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    private void initSpinner() {
        //猪舍
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, chulanzhushe);
        mchulanzhushespinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mchulanzhushespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                defaultpig = bean.getData().get(position).getSheId();
                chuShe = chulanzhushe.get(position);
                getzhujuanMessage(defaultpig, chuShe);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void getzhujuanMessage(int pighouseid, String pighousename) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(userid));
        map.put(Constants.en_id, en_id);
        Map mapbody = new HashMap();
        mapbody.put(Constants.amountFlg, 9 + "");
        mapbody.put(Constants.insureFlg, String.valueOf(9));
        Log.i("==pighouseid====", pighouseid + "");
        mapbody.put(Constants.sheId, String.valueOf(pighouseid));

        OkHttp3Util.doPost(Constants.ZHUJUANSHOW, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                final ZhuJuanBean bean = GsonUtils.getBean(string, ZhuJuanBean.class);
                if (null != bean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bean.getStatus() == 1) {
                                if (null != bean.getData()) {
                                    OutHurdleAdapter outHurdleAdapter = new OutHurdleAdapter(OutHurdleActivity.this, bean.getData(), pighousename);
                                    mchulanlistview.setAdapter(outHurdleAdapter);
                                    outHurdleAdapter.setListner(new OutHurdleAdapter.MyInterface() {
                                        @Override
                                        public void isOut(Boolean aBoolean) {
                                            Log.i("==aBoolean====", aBoolean + "");
                                            if (aBoolean) {
                                                getzhujuanMessage(defaultpig, chuShe);
                                            }
                                        }
                                    });
                                }
                            } else if (bean.getStatus() == 0) {
                                Toast.makeText(OutHurdleActivity.this, bean.getMsg(), Toast.LENGTH_LONG).show();
                            } else if (bean.getStatus() == -1) {
                                Toast.makeText(OutHurdleActivity.this, bean.getMsg(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OutHurdleActivity.this, "查询失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });


    }
}
