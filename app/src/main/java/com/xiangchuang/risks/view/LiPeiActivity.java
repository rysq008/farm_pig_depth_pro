package com.xiangchuang.risks.view;

import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.StartBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.tensorflow.demo.DetectorActivity;
import org.tensorflow.demo.Global;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import innovation.media.Model;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LiPeiActivity extends BaseActivity {
    public static String TAG = "LiPeiActivity";
    @BindView(R.id.lipei_name)
    TextView mlipeiname;
    @BindView(R.id.yulipei)
    TextView myulipei;
    @BindView(R.id.lipeishenqing)
    TextView lipeishenqing;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    private String en_id;
    private int userid;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_li_pei;
    }

    @Override
    protected void initData() {
        tv_title.setText("理赔");
        en_id = PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0");
        userid = PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext());
        mlipeiname.setText(PreferencesUtils.getStringValue(Constants.companyname, MyApplication.getAppContext()));
        //checkBaoDan();
    }

    @OnClick({R.id.yulipei, R.id.lipeishenqing, R.id.iv_cancel})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.yulipei:
                Intent intent = new Intent(this, PreparedLiPeiActivity.class);
                PreferencesUtils.saveKeyValue(Constants.fleg, "pre", MyApplication.getAppContext());
                startActivity(intent);
                break;
            case R.id.lipeishenqing:
                collectToNetForLiPei();
                break;
            case R.id.iv_cancel:
                finish();
                break;
            default:
                break;
        }

    }

   /* private void checkBaoDan() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(userid));
        map.put(Constants.en_id, en_id);
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.CHECKBAODAN, null, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                final StartBean bean = GsonUtils.getBean(string, StartBean.class);
                if (null != bean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                mlipeiname.setClickable(true);
                                myulipei.setClickable(true);
                            } else if (bean.getStatus() == 0) {
                                mlipeiname.setClickable(false);
                                myulipei.setClickable(false);
                               // Toast.makeText(MyApplication.getAppContext(), bean.getMsg(), Toast.LENGTH_LONG).show();
                                showDialogError(bean.getMsg());
                            } else if (bean.getStatus() == -1) {
                                mlipeiname.setClickable(false);
                                myulipei.setClickable(false);
                                showDialogError(bean.getMsg());
                               // Toast.makeText(MyApplication.getAppContext(), bean.getMsg(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LiPeiActivity.this, "开始采集失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }*/

    private void collectToNetForLiPei() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(userid));
        map.put(Constants.en_id, en_id);
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.LiSTART, null, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                final StartBean bean = GsonUtils.getBean(string, StartBean.class);
                if (null != bean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                PreferencesUtils.saveKeyValue(Constants.fleg, "lipei", MyApplication.getAppContext());
                                //PreferencesUtils.saveKeyValue(Constants.preVideoId, bean.getData(), MyApplication.getAppContext());
                                Global.model = Model.VERIFY.value();
                                Intent intent = new Intent(LiPeiActivity.this, DetectorActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                //showDialogError(bean.getMsg());
                                AlertDialogManager.showMessageDialog(LiPeiActivity.this, "提示", bean.getMsg(), new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {

                                    }

                                    @Override
                                    public void onNegative() {

                                    }
                                });



                            }
                            /*else if (bean.getStatus() == 0) {
                                Toast.makeText(MyApplication.getAppContext(), bean.getMsg(), Toast.LENGTH_LONG).show();
                            } else if (bean.getStatus() == -1) {
                                Toast.makeText(MyApplication.getAppContext(), bean.getMsg(), Toast.LENGTH_LONG).show();
                            }*/
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LiPeiActivity.this, "开始采集失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }
}
