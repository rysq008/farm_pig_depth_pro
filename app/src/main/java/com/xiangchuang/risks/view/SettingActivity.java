package com.xiangchuang.risks.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.GsonUtils;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SettingActivity extends BaseActivity {

    TextView mshesetting;

    TextView mjuansetting;

    TextView select_shexiangtou;

    TextView tv_title;

    ImageView iv_cancel;
    private String en_id;
    private int userid;

    @Override
    public void initView() {
        super.initView();
        mshesetting = (TextView) findViewById(R.id.shesetting);
        mjuansetting = (TextView) findViewById(R.id.juansetting);
        select_shexiangtou = (TextView) findViewById(R.id.select_shexiangtou);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.select_shexiangtou).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.juansetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.shesetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initData() {
        tv_title.setText("设置");
        en_id = PreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0");
        userid = PreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext());
    }


    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.shesetting:
                goToActivity(PigHouseInformationActivity.class, null);
                break;
            case R.id.juansetting:
                getSheData1();
                break;
            case R.id.select_shexiangtou:
                goToActivity(USBMenageActivity.class, null);
                finish();
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
                AVOSCloudUtils.saveErrorMessage(e,SettingActivity.class.getSimpleName());
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
                                } else {
                                    AlertDialogManager.showMessageDialogOne(SettingActivity.this, "提示", "您还未设置猪舍信息", new AlertDialogManager.DialogInterface() {
                                        @Override
                                        public void onPositive() {

                                        }

                                        @Override
                                        public void onNegative() {

                                        }
                                    });
                                }
                            } else {
                                AlertDialogManager.showMessageDialog(SettingActivity.this, "提示", bean.getMsg(), new AlertDialogManager.DialogInterface() {
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

}
