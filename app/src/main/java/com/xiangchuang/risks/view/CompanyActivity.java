package com.xiangchuang.risks.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.CompanyAdapter;
import com.xiangchuang.risks.model.bean.CompanyBean;
import com.xiangchuang.risks.model.bean.InSureCompanyBean;
import com.xiangchuang.risks.model.bean.ZhuJuanBean;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CompanyActivity extends BaseActivity {
    @BindView(R.id.company_name)
    TextView company_name;
    @BindView(R.id.addcompany)
    TextView addcompany;
    @BindView(R.id.company_listview)
    ListView company_listview;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    private String en_id;
    private int userid;
    public static String TAG = "CompanyActivity";
    private List<InSureCompanyBean> inSureCompanyBeanlists = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_company;
    }

    @Override
    protected void initData() {
        String companyuser = PreferencesUtils.getStringValue(Constants.companyuser, MyApplication.getAppContext());
        String insurecompany = PreferencesUtils.getStringValue(Constants.insurecompany, MyApplication.getAppContext());
        company_name.setText("企业列表");
        getDataFromNet();
    }

    @OnClick({R.id.addcompany, R.id.iv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addcompany:
                Bundle bundle = new Bundle();
                bundle.putBoolean("type",false);
                goToActivity(AddCompanyActivity.class, bundle);
                break;
            case R.id.iv_cancel:
                finish();
                break;
            default:
                break;
        }

    }

    private void getDataFromNet() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        Map mapbody = new HashMap();
        mapbody.put(Constants.deptId, PreferencesUtils.getStringValue(Constants.deptId, MyApplication.getAppContext()));
        OkHttp3Util.doPost(Constants.ENLIST, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showDialogError(msg);
                                    }
                                });
                            }
                        });
                    } else {
                        inSureCompanyBeanlists.clear();
                        JSONArray datas = jsonObject.getJSONArray("data");
                        for (int i = 0; i < datas.length(); i++) {
                            JSONObject jsonObject1 = datas.getJSONObject(i);
                            String enId = jsonObject1.getString("enId");
                            String enName = jsonObject1.getString("enName");
                            String enUserId = jsonObject1.getString("enUserId");
                            String enUserName = jsonObject1.getString("enUserName");
                            String canUse = jsonObject1.getString("canUse");
                            InSureCompanyBean inSureCompanyBean = new InSureCompanyBean();
                            inSureCompanyBean.setEnId(enId);
                            inSureCompanyBean.setEnName(enName);
                            inSureCompanyBean.setEnUserId(enUserId);
                            inSureCompanyBean.setEnUserName(enUserName);
                            inSureCompanyBean.setCanUse(canUse);
                            inSureCompanyBeanlists.add(inSureCompanyBean);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                company_listview.setAdapter(new CompanyAdapter(CompanyActivity.this, inSureCompanyBeanlists));
                            }
                        });

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }

}
