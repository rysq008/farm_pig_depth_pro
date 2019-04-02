package com.xiangchuang.risks.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.UpdateBean;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.model.adapter.ZhuSheXinXI_item_Adapter;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.GsonUtils;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PigHouseInformationActivity extends BaseActivity {
    public static String TAG = "PigHouseInformationActivity";

    EditText mzhusheedittext;

    TextView mzhusherightimage;

    ListView mzhushelistview;

    TextView mzhushezhujuanxinxi;

    TextView tv_title;

    ImageView iv_cancel;
    private String en_id;
    private int userid;
    private List<ZhuSheBean.DataBean> sheList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mzhusheedittext = (EditText) findViewById(R.id.zhushe_edit_text);
        mzhusherightimage = (TextView) findViewById(R.id.zhushe_right_image);
        mzhushelistview = (ListView) findViewById(R.id.zhushe_list_view);
        mzhushezhujuanxinxi = (TextView) findViewById(R.id.zhushe_zhujuanxinxi);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.zhushe_zhujuanxinxi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.zhushe_right_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_pig_house_information;
    }

    @Override
    protected void initData() {
        en_id = PreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0");
        userid = PreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext());
        tv_title.setText("猪舍信息");
        if (!en_id.equals(0)) {
            getDataFromNet();
        } else {
            toastUtils.showLong(AppConfig.getAppContext(), "请稍候");
            //Toast.makeText(AppConfig.getAppContext(), "请稍候", Toast.LENGTH_LONG).show();
        }
    }


    public void onClickView(View view) {
        int i = view.getId();
        if (i == R.id.zhushe_right_image) {
            if (null == mzhusheedittext.getText().toString() || "".equals(mzhusheedittext.getText().toString())) {
                toastUtils.showLong(AppConfig.getAppContext(), "猪舍信息为空");
                // Toast.makeText(PigHouseInformationActivity.this, "猪舍信息为空", Toast.LENGTH_LONG).show();
            } else {
                //添加猪舍
                addZhuShe();
            }

        } else if (i == R.id.zhushe_zhujuanxinxi) {
            getSheData1();

        } else if (i == R.id.iv_cancel) {
            finish();

        } else {
        }

    }

    private void getSheData1() {
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
                Log.i("ShowPollingActivity", e.toString());
                AVOSCloudUtils.saveErrorMessage(e,PigHouseInformationActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("ShowPollingActivity", string);
                final ZhuSheBean bean = GsonUtils.getBean(string, ZhuSheBean.class);
                if (null != bean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                List<ZhuSheBean.DataBean> sheList = bean.getData();
                                if (null != sheList && sheList.size() > 0) {
                                    goToActivity(PigHogInformationActivity.class, null);
                                    finish();
                                } else {
                                    AlertDialogManager.showMessageDialog(PigHouseInformationActivity.this, "提示", "您还未设置猪舍信息", new AlertDialogManager.DialogInterface() {
                                        @Override
                                        public void onPositive() {

                                        }

                                        @Override
                                        public void onNegative() {

                                        }
                                    });
                                }
                            } else {
                                AlertDialogManager.showMessageDialog(PigHouseInformationActivity.this, "提示", bean.getMsg(), new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {

                                    }

                                    @Override
                                    public void onNegative() {

                                    }
                                });
                            }
                        }
                    });

                }

            }
        });

    }

    private void addZhuShe() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, userid + "");
        map.put(Constants.en_id, en_id);
        Map mapbody = new HashMap();
        mapbody.put(Constants.name, mzhusheedittext.getText().toString());
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUSHEADD, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e,PigHouseInformationActivity.class.getSimpleName());
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
                                        toastUtils.showLong(AppConfig.getAppContext(), bean.getMsg());
                                        getDataFromNet();
                                    } else {
                                        toastUtils.showLong(AppConfig.getAppContext(), bean.getMsg());
                                    }
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toastUtils.showLong(AppConfig.getAppContext(), bean.getMsg());
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e,PigHouseInformationActivity.class.getSimpleName());
                }
            }
        });

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
                AVOSCloudUtils.saveErrorMessage(e,PigHouseInformationActivity.class.getSimpleName());
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
                                List<ZhuSheBean.DataBean> sheList = bean.getData();
                                if (null != sheList && sheList.size() > 0) {
                                    Log.i("defaultpig=", sheList.get(0).getSheId() + "");
                                    PreferencesUtils.saveIntValue(Constants.defaultpig, sheList.get(0).getSheId(), AppConfig.getAppContext());
                                    mzhushelistview.setAdapter(new ZhuSheXinXI_item_Adapter(PigHouseInformationActivity.this, sheList));
                                } else {
                                    toastUtils.showLong(AppConfig.getAppContext(), "猪舍为空");
                                    // Toast.makeText(PigHouseInformationActivity.this, "猪舍为空", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                toastUtils.showLong(AppConfig.getAppContext(), bean.getMsg());
                            }
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PigHouseInformationActivity.this, "查询失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });

    }
}
