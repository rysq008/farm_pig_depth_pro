package com.xiangchuang.risks.view;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.UpdateBean;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.model.adapter.ZhuSheXinXI_item_Adapter;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PigHouseInformationActivity extends BaseActivity {
    public static String TAG = "PigHouseInformationActivity";
    @BindView(R.id.zhushe_edit_text)
    EditText mzhusheedittext;
    @BindView(R.id.zhushe_right_image)
    TextView mzhusherightimage;
    @BindView(R.id.zhushe_list_view)
    ListView mzhushelistview;
    @BindView(R.id.zhushe_zhujuanxinxi)
    TextView mzhushezhujuanxinxi;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    private String en_id;
    private int userid;
    private List<ZhuSheBean.DataBean> sheList;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pig_house_information;
    }

    @Override
    protected void initData() {
        en_id = PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0");
        userid = PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext());
        tv_title.setText("猪舍信息");
        if (!en_id.equals(0)) {
            getDataFromNet();
        } else {
            toastUtils.showLong(MyApplication.getAppContext(), "请稍候");
            //Toast.makeText(MyApplication.getAppContext(), "请稍候", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick({R.id.zhushe_right_image, R.id.zhushe_zhujuanxinxi,R.id.iv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.zhushe_right_image:
                if (null == mzhusheedittext.getText().toString() || "".equals(mzhusheedittext.getText().toString())) {
                    toastUtils.showLong(MyApplication.getAppContext(), "猪舍信息为空");
                    // Toast.makeText(PigHouseInformationActivity.this, "猪舍信息为空", Toast.LENGTH_LONG).show();
                } else {
                    //添加猪舍
                    addZhuShe();
                }
                break;
            case R.id.zhushe_zhujuanxinxi:
                getSheData1();
                break;
            case R.id.iv_cancel:
                finish();
                break;
            default:
                break;
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
                                        toastUtils.showLong(MyApplication.getAppContext(), bean.getMsg());
                                        getDataFromNet();
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
                                    PreferencesUtils.saveIntValue(Constants.defaultpig, sheList.get(0).getSheId(), MyApplication.getAppContext());
                                    mzhushelistview.setAdapter(new ZhuSheXinXI_item_Adapter(PigHouseInformationActivity.this, sheList));
                                } else {
                                    toastUtils.showLong(MyApplication.getAppContext(), "猪舍为空");
                                    // Toast.makeText(PigHouseInformationActivity.this, "猪舍为空", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                toastUtils.showLong(MyApplication.getAppContext(), bean.getMsg());
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
