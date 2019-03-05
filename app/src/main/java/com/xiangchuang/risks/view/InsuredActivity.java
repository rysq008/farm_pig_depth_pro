package com.xiangchuang.risks.view;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.InsureListBean;
import com.xiangchuang.risks.model.bean.PigTypeBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.LocationManager;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import innovation.utils.MyWatcher;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class InsuredActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.companyname)
    TextView companyname;
    @BindView(R.id.headnumber)
    EditText headnumber;
    @BindView(R.id.coefficient)
    EditText coefficient;
    @BindView(R.id.type_spinner)
    Spinner typespinner;
    @BindView(R.id.submit)
    TextView submit;
    @BindView(R.id.num)
    TextView num;
    @BindView(R.id.noinfo)
    TextView noinfo;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    private List<String> typeNameList = new ArrayList<>();
    private String pigType = null;
    private String count;
    private List<PigTypeBean.DataBean> pigTypeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_insured;
    }

    @Override
    protected void initData() {
        getPigType();
        getInsureList();
        companyname.setText(PreferencesUtils.getStringValue(Constants.companyname, InsuredActivity.this));
        tv_title.setText("投保");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submits();
            }
        });
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        coefficient.addTextChangedListener(new MyWatcher(2, 2));
    }

    private void initTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typeNameList);
        typespinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) parent.getAdapter();
                for (int i = 0; i < pigTypeList.size(); i++) {
                    if (arrayAdapter.getItem(position).equals(pigTypeList.get(i).getPigTypeName())) {
                        pigType = pigTypeList.get(i).getPigType() + "";
                        num.setText(pigTypeList.get(i).getCount());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getPigType() {
//        String en_id = PreferencesUtils.getStringValue(Constants.en_id, InsuredActivity.this);
//        String enUserId = PreferencesUtils.getIntValue(Constants.en_user_id, InsuredActivity.this) + "";
//        if (en_id.isEmpty() || en_id == null) {
//            Toast.makeText(this, "en_id is null", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (enUserId.isEmpty() || enUserId == null) {
//            Toast.makeText(this, "enUserId is null", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Map map = new HashMap();
//        map.put(Constants.AppKeyAuthorization, "hopen");
//        map.put(Constants.en_id, en_id);
//        map.put(Constants.enUserId, enUserId);
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.PIGTYPE, null, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AVOSCloudUtils.saveErrorMessage(e);
                mProgressDialog.dismiss();
                Log.i("InsuredActivity", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String string = response.body().string();
                Log.i("InsuredActivity", string);
                PigTypeBean bean = GsonUtils.getBean(string, PigTypeBean.class);
                if (bean != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                pigTypeList = bean.getData();
                                if (pigTypeList.size() > 0) {
                                    typeNameList.clear();
                                    for (int i = 0; i < pigTypeList.size(); i++) {
                                        typeNameList.add(pigTypeList.get(i).getPigTypeName());
                                    }
                                    pigType = pigTypeList.get(0).getPigType() + "";
                                    count = pigTypeList.get(0).getCount();
                                    initTypeSpinner();
                                    num.setText(count);
                                } else {
                                    pigType = null;
                                }
                            } else {
                                Toast.makeText(InsuredActivity.this, bean.getMsg(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            Toast.makeText(InsuredActivity.this, "获取猪种类失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }


            }
        });

    }

    private void submits() {
//        String en_id = PreferencesUtils.getStringValue(Constants.en_id, InsuredActivity.this);
//        String enUserId = PreferencesUtils.getIntValue(Constants.en_user_id, InsuredActivity.this) + "";
        String amount = headnumber.getText().toString().trim();
        String ratio = coefficient.getText().toString().trim();
        if (ratio != null && !"".equals(ratio)) {

            if (ratio.equals("0")) {
                Toast.makeText(this, "请输入大于0的投保系数", Toast.LENGTH_SHORT).show();
                coefficient.setFocusable(true);
                coefficient.setFocusableInTouchMode(true);
                return;
            }
            if (ratio.contains(".")) {
                Log.i("+++++++++", ratio);
//                String befor = ratio.substring(0, ratio.indexOf("."));
                float f = Float.parseFloat(ratio);
//                int a = Integer.valueOf(befor).intValue();
                if (f > 2.8f) {
                    Toast.makeText(this, "请输入1~2.8之间的投保系数", Toast.LENGTH_SHORT).show();
                    coefficient.setFocusable(true);
                    coefficient.setFocusableInTouchMode(true);
                    return;
                }

                if(f < 1.0f){
                    Toast.makeText(this, "请输入1~2.8之间的投保系数", Toast.LENGTH_SHORT).show();
                    coefficient.setFocusable(true);
                    coefficient.setFocusableInTouchMode(true);
                    return;
                }

            } else {
                int a = Integer.valueOf(ratio);
                if (a >= 3) {
                    Toast.makeText(this, "请输入1~2.8之间的投保系数", Toast.LENGTH_SHORT).show();
                    coefficient.setFocusable(true);
                    coefficient.setFocusableInTouchMode(true);
                    return;
                }

                if (a < 1) {
                    Toast.makeText(this, "请输入1~2.8之间的投保系数", Toast.LENGTH_SHORT).show();
                    coefficient.setFocusable(true);
                    coefficient.setFocusableInTouchMode(true);
                    return;
                }

            }
        }

//        if (en_id.isEmpty() || en_id == null) {
//            Toast.makeText(this, "en_id is null", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (enUserId.isEmpty() || enUserId == null) {
//            Toast.makeText(this, "enUserId is null", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (amount.isEmpty() || amount == null) {
            Toast.makeText(this, "请输入投保数量", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ratio.isEmpty() || ratio == null) {
            Toast.makeText(this, "请输入投保系数", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("".equals(pigType) || pigType == null) {
            Toast.makeText(this, "请选择投保种类", Toast.LENGTH_SHORT).show();
            return;
        }
//        Map map = new HashMap();
//        map.put(Constants.AppKeyAuthorization, "hopen");
//        map.put(Constants.en_id, en_id);
//        map.put(Constants.enUserId, enUserId);
        Map mapbody = new HashMap();
        mapbody.put(Constants.amount, amount);
        mapbody.put(Constants.ratio, ratio);
        mapbody.put(Constants.pigType, pigType);
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.INSURED, mapbody, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AVOSCloudUtils.saveErrorMessage(e);
                mProgressDialog.dismiss();
                Log.i("InsuredActivity", e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("InsuredActivity", string);
                try {
                    JSONObject jsonObject = new JSONObject(string);

                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                AlertDialogManager.showMessageDialog(InsuredActivity.this, "提示", msg, new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {

                                    }

                                    @Override
                                    public void onNegative() {

                                    }
                                });
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                Log.i("InsuredActivity", msg);
                                InsuredActivity.this.finish();

                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e);
                }

            }
        });

    }

    private void getInsureList() {

        OkHttp3Util.doPost(Constants.INSURELIST, null, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("InsuredActivity", e.toString());
                AVOSCloudUtils.saveErrorMessage(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("InsuredActivity", string);
                InsureListBean bean = GsonUtils.getBean(string, InsureListBean.class);
                if (bean != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bean.getStatus() == 1) {
                                if (bean.getData().size() > 0) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    noinfo.setVisibility(View.GONE);
                                    recyclerView.setAdapter(new InsuredAdapter(InsuredActivity.this, bean.getData()));
                                } else {
                                    recyclerView.setVisibility(View.GONE);
                                    noinfo.setVisibility(View.VISIBLE);
                                }
                            } else {
                                recyclerView.setVisibility(View.GONE);
                                noinfo.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setVisibility(View.GONE);
                            noinfo.setVisibility(View.VISIBLE);
                            mProgressDialog.dismiss();
                            Toast.makeText(InsuredActivity.this, "获取已投保信息失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }


            }
        });
    }
}
