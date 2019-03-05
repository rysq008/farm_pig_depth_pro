package com.xiangchuang.risks.view;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.PinZhongBean;
import com.xiangchuang.risks.model.bean.UpdateBean;
import com.xiangchuang.risks.model.bean.ZhuJuanBean;
import com.xiangchuang.risks.model.adapter.ZhuJuanXinXI_item_Adapter;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
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
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PigHogInformationActivity extends BaseActivity {
    public static String TAG = "PigHogInformationActivity";
    @BindView(R.id.zhujuanxinxi_spinnerzhushe)
    Spinner mzhujuanxinxispinnerzhushe;
    @BindView(R.id.zhujuanxinxi_spinnerpinzhong)
    Spinner mzhujuanxinxispinnerpinzhong;
    @BindView(R.id.zhujuanxinxi_edit_text)
    EditText mzhujuaname;
    @BindView(R.id.zhujuanxinxi_right_image)
    TextView mzhujuanxinxirightimage;
    @BindView(R.id.zhujuanxinxi_list_view)
    ListView mzhujuanxinxilistview;
    @BindView(R.id.zhushe_setting)
    TextView mzhushesetting;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    List<String> zhushename = new ArrayList<>();
    List<String> zhujuan_pinzhong = new ArrayList<>();
    private String pinzhong;
    private String addzhujuan;
    private String en_id;
    private int defaultpig;
    private int userid;
    private List<ZhuSheBean.DataBean> sheList;
    private int sheId;
    private List<ZhuJuanBean.DataBean> dataBeans;
    private List<PinZhongBean.DataBean> pinzhongdata;
    private int manimalSubType;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_pig_hog_information;
    }

    @Override
    protected void initData() {
        defaultpig = PreferencesUtils.getIntValue(Constants.defaultpig, MyApplication.getAppContext());
        en_id = PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0");
        userid = PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext());
        tv_title.setText("猪圈信息");
        if (!en_id.equals(0)) {
            getDataFromNet();
        } else {
            toastUtils.showLong(MyApplication.getAppContext(), "请稍后");
        }
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
                final ZhuSheBean bean = GsonUtils.getBean(string, ZhuSheBean.class);
                if (null != bean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                zhushename.clear();
                                sheList = bean.getData();
                                for (int i = 0; i < sheList.size(); i++) {
                                    zhushename.add(sheList.get(i).getName());
                                }
                                initSpinner();
                            } else {
                                toastUtils.showLong(MyApplication.getAppContext(), bean.getMsg());
                            }

                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toastUtils.showLong(MyApplication.getAppContext(), bean.getMsg());
                        }
                    });
                }

            }
        });

        getzhujuanMessage(defaultpig + "");

    }

    private void getPinzhongMessage() {
        zhujuan_pinzhong.clear();
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        OkHttp3Util.doPost(Constants.PINZHONG, null, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e);
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
                                showDialogError(msg);
                            }
                        });
                    } else {
                        PinZhongBean bean = GsonUtils.getBean(string, PinZhongBean.class);
                        if (null != bean) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //品种
                                    pinzhongdata = bean.getData();
                                    if (null != pinzhongdata && pinzhongdata.size() > 0) {
                                        for (int i = 0; i < pinzhongdata.size(); i++) {
                                            zhujuan_pinzhong.add(pinzhongdata.get(i).getAnimalSubTypeName());
                                        }
                                        initPinZhong();
                                        //猪圈
                                        if (null != sheList && sheList.size() > 0) {
                                            mzhujuanxinxilistview.setAdapter(new ZhuJuanXinXI_item_Adapter(PigHogInformationActivity.this, dataBeans, pinzhongdata));
                                        } else {
                                            toastUtils.showLong(MyApplication.getAppContext(), "猪圈为空");
                                        }
                                    } else {
                                        toastUtils.showLong(MyApplication.getAppContext(), "品种为空");
                                    }
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toastUtils.showLong(MyApplication.getAppContext(), "添加失败");
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e);
                }
            }
        });
    }

    private void initPinZhong() {
        //品种
        ArrayAdapter<String> pzadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, zhujuan_pinzhong);
        mzhujuanxinxispinnerpinzhong.setAdapter(pzadapter);
        pzadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mzhujuanxinxispinnerpinzhong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (zhujuan_pinzhong.size() > 0) {
                    pinzhong = zhujuan_pinzhong.get(position);
                    addzhujuan = mzhujuaname.getText().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initSpinner() {
        //猪舍
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, zhushename);
        mzhujuanxinxispinnerzhushe.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mzhujuanxinxispinnerzhushe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sheId = sheList.get(position).getSheId();
                //猪舍id
                getzhujuanMessage(sheId + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void getzhujuanMessage(String pighouseid) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, userid + "");
        map.put(Constants.en_id, en_id);
        Map mapbody = new HashMap();
        mapbody.put(Constants.amountFlg, "" + 9);
        mapbody.put(Constants.insureFlg, "" + 9);
        Log.i("pighouseid=", pighouseid);
        mapbody.put(Constants.sheId, pighouseid);
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
                        ZhuJuanBean bean = GsonUtils.getBean(string, ZhuJuanBean.class);
                        if (null != bean) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dataBeans = bean.getData();
                                    getPinzhongMessage();
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PigHogInformationActivity.this, "添加失败", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e);
                }


            }
        });


    }


    @OnClick({R.id.zhujuanxinxi_right_image, R.id.zhushe_setting,R.id.iv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.zhujuanxinxi_right_image:
                String selpinzhong = mzhujuanxinxispinnerpinzhong.getSelectedItem().toString();
                for (int i = 0; i < pinzhongdata.size(); i++) {
                    if (pinzhongdata.get(i).getAnimalSubTypeName().equals(selpinzhong)) {
                        manimalSubType = pinzhongdata.get(i).getAnimalSubType();
                    }
                }
                if (!"".equals(mzhujuaname.getText().toString())) {
                    addDataToNet();
                }
                break;
            case R.id.zhushe_setting:
                goToActivity(PigHouseInformationActivity.class, null);
                finish();
                break;
            case R.id.iv_cancel:
                finish();
                break;
            default:
                break;
        }
    }

    private void addDataToNet() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(userid));
        map.put(Constants.en_id, en_id);
        Map mapbody = new HashMap();
        mapbody.put(Constants.sheId, String.valueOf(sheId));
        mapbody.put(Constants.name, mzhujuaname.getText().toString());
        mapbody.put(Constants.animalSubType, manimalSubType + "");


        Log.i("sheId", sheId + "");
        Log.i("name", addzhujuan);

        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUJUANADD, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e);
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
                                showDialogError(msg);
                            }
                        });
                    } else {
                        final UpdateBean bean = GsonUtils.getBean(string, UpdateBean.class);
                        if (null != bean) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.dismiss();
                                    if (bean.getStatus() == 1) {
                                        toastUtils.showLong(MyApplication.getAppContext(), bean.getMsg());
                                        getzhujuanMessage(sheId + "");
                                    } else {
                                        toastUtils.showLong(MyApplication.getAppContext(), bean.getMsg());
                                    }
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toastUtils.showLong(MyApplication.getAppContext(), "添加失败");
                                    // Toast.makeText(PigHogInformationActivity.this, "添加失败", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e);
                }
            }
        });

    }
}
