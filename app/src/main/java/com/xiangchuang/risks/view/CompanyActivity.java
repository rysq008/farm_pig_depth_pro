package com.xiangchuang.risks.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    @BindView(R.id.tv_exit)
    TextView tvExit;
    @BindView(R.id.rl_edit)
    RelativeLayout rl_edit;

    private String en_id;
    private int userid;
    public static String TAG = "CompanyActivity";
    private List<InSureCompanyBean> inSureCompanyBeanlists = new ArrayList<>();
    private PopupWindow pop;
    private TextView loginExit;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_company;
    }

    @Override
    protected void initData() {
        company_name.setText("企业列表");
        getDataFromNet();

        pop = new PopupWindow(CompanyActivity.this);
        View popview = getLayoutInflater().inflate(R.layout.item_setting, null);
        loginExit = popview.findViewById(R.id.login_exit);
        pop.setWidth(300);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(popview);

    }

    @OnClick({R.id.addcompany, R.id.iv_cancel, R.id.tv_exit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addcompany:
                Bundle bundle = new Bundle();
                bundle.putBoolean("type", false);
                goToActivity(AddCompanyActivity.class, bundle);
                break;
            case R.id.iv_cancel:
                finish();
                break;
            case R.id.tv_exit:
                pop.showAsDropDown(rl_edit);
                loginExit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pop.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(CompanyActivity.this)
                                .setIcon(R.drawable.cowface).setTitle("提示")
                                .setMessage("退出登录")
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //如果退出，清空保存的相关状态， 跳转到登录页
                                        PreferencesUtils.removeAllKey(CompanyActivity.this);
                                        Intent addIntent = new Intent(CompanyActivity.this, LoginFamerActivity.class);
                                        startActivity(addIntent);
                                        finish();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        builder.setCancelable(false);
                        builder.show();
                    }
                });
                break;

            default:
                break;
        }

    }

    private void getDataFromNet() {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.AppKeyAuthorization, "hopen");
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put(Constants.deptId, PreferencesUtils.getStringValue(Constants.deptId, MyApplication.getAppContext()));
        OkHttp3Util.doPost(Constants.ENLIST, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
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

    private long firstTime = 0;

    @Override
    public void onBackPressed() {
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            firstTime = secondTime;
        } else {
//            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

}
