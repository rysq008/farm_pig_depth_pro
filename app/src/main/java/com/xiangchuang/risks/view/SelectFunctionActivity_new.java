package com.xiangchuang.risks.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.QueryVideoFlagDataBean;
import com.xiangchuang.risks.model.bean.StartBean;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuangtec.luolu.animalcounter.BuildConfig;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;
import com.xiangchuangtec.luolu.animalcounter.view.ShowPollingActivity_new;

import org.json.JSONObject;
import org.tensorflow.demo.DetectorActivity;
import org.tensorflow.demo.Global;
import org.tensorflow.demo.SmallVideoActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import innovation.media.Model;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SelectFunctionActivity_new extends BaseActivity {
    public static String TAG = "SelectFunctionActivity";
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    @BindView(R.id.select_name)
    TextView mselectname;
    @BindView(R.id.select_toubao)
    TextView mselecttoubao;
    @BindView(R.id.rel_toubao)
    RelativeLayout rel_toubao;
    @BindView(R.id.select_xunjiandianshu)
    TextView mselectxunjiandianshu;
    @BindView(R.id.select_yulipei)
    TextView selectYulipei;
    @BindView(R.id.select_lipei)
    TextView mselectlipei;
    @BindView(R.id.rel_lipei)
    RelativeLayout relLipei;
    @BindView(R.id.select_webview)
    TextView selectWebview;
    @BindView(R.id.tv_exit)
    TextView tvExit;
    @BindView(R.id.rl_back)
    RelativeLayout rlBack;
    @BindView(R.id.rl_edit)
    RelativeLayout rlEdit;

    private String companyname;
    private String en_id;
    private int userid;
    private String companyfleg;
    private boolean isLiPei = true;

    private PopupWindow pop;
    private TextView loginExit;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_function_new;
    }

    @Override
    protected void initData() {
        companyname = PreferencesUtils.getStringValue(Constants.companyname, MyApplication.getAppContext(), "育肥猪农场");
        companyfleg = PreferencesUtils.getStringValue(Constants.companyfleg, MyApplication.getAppContext(), "0");
        Log.i("==companyfleg=", companyfleg + "");
        mselectname.setText(companyname);
        en_id = PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0");
        userid = PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext());
        //保险公司
        if ("1".equals(companyfleg)) {
            rel_toubao.setVisibility(View.VISIBLE);
            relLipei.setVisibility(View.VISIBLE);
            iv_cancel.setVisibility(View.VISIBLE);
            tvExit.setVisibility(View.GONE);
        } else if ("2".equals(companyfleg)) {
            iv_cancel.setVisibility(View.GONE);
            tvExit.setVisibility(View.VISIBLE);
            //企业（养殖场）
            rel_toubao.setVisibility(View.GONE);
            if (MyApplication.isOpenLiPei) {
                relLipei.setVisibility(View.VISIBLE);
            } else {
                relLipei.setVisibility(View.GONE);
            }
        }

        queryVideoFlag();

        pop = new PopupWindow(SelectFunctionActivity_new.this);
        View popview = getLayoutInflater().inflate(R.layout.item_setting, null);
        loginExit = popview.findViewById(R.id.login_exit);
        pop.setWidth(300);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(popview);

    }


    private void queryVideoFlag() {
        Map<String, String> map = new HashMap<>();
        map.put("animalType", "1");
        mProgressDialog.show();

        OkHttp3Util.doPost(Constants.QUERY_VIDEOFLAG_NEW, null, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                mProgressDialog.dismiss();
                Log.i(TAG, string);
                QueryVideoFlagDataBean queryVideoFlagData = GsonUtils.getBean(string, QueryVideoFlagDataBean.class);
                if (queryVideoFlagData.getStatus() == 1) {
                    QueryVideoFlagDataBean.thresholdList thresholdList =
                            GsonUtils.getBean(queryVideoFlagData.getData().getThreshold(), QueryVideoFlagDataBean.thresholdList.class);

                    Log.e(TAG, "queryVideoFlag thresholdList: " + thresholdList.toString());

                    //存储理赔的时间条件信息
                    PreferencesUtils.saveIntValue(Constants.lipeia, Integer.parseInt(thresholdList.getLipeiA()), SelectFunctionActivity_new.this);
                    PreferencesUtils.saveIntValue(Constants.lipeib, Integer.parseInt(thresholdList.getLipeiB()), SelectFunctionActivity_new.this);
                    PreferencesUtils.saveIntValue(Constants.lipein, Integer.parseInt(thresholdList.getLipeiN()), SelectFunctionActivity_new.this);
                    PreferencesUtils.saveIntValue(Constants.lipeim, Integer.parseInt(thresholdList.getLipeiM()), SelectFunctionActivity_new.this);

                    PreferencesUtils.saveKeyValue(Constants.phone, queryVideoFlagData.getData().getServiceTelephone(), SelectFunctionActivity_new.this);
                    PreferencesUtils.saveKeyValue(Constants.customServ, thresholdList.getCustomServ(), SelectFunctionActivity_new.this);

                    PreferencesUtils.saveKeyValue(Constants.THRESHOLD_LIST, queryVideoFlagData.getData().getThreshold(), SelectFunctionActivity_new.this);

                    if (null != queryVideoFlagData.getData() && !"".equals(queryVideoFlagData.getData())) {
                        String left = (queryVideoFlagData.getData().getLeftNum() == null) ? "8" : queryVideoFlagData.getData().getLeftNum();
                        String middleNum = (queryVideoFlagData.getData().getLeftNum() == null) ? "8" : queryVideoFlagData.getData().getMiddleNum();
                        String rightNum = (queryVideoFlagData.getData().getLeftNum() == null) ? "8" : queryVideoFlagData.getData().getRightNum();
                        if(BuildConfig.DEBUG){
                            Log.e(TAG, "\nleft:\n" + left);
                            Log.e(TAG, "\nmiddleNum:\n" + middleNum);
                            Log.e(TAG, "\nrightNum:\n" + rightNum);
                        }
                        PreferencesUtils.saveKeyValue(PreferencesUtils.FACE_ANGLE_MAX_LEFT, left, SelectFunctionActivity_new.this);
                        PreferencesUtils.saveKeyValue(PreferencesUtils.FACE_ANGLE_MAX_MIDDLE, middleNum, SelectFunctionActivity_new.this);
                        PreferencesUtils.saveKeyValue(PreferencesUtils.FACE_ANGLE_MAX_RIGHT, rightNum, SelectFunctionActivity_new.this);
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(SelectFunctionActivity_new.this)
                                    .setIcon(R.drawable.cowface)
                                    .setTitle("提示")
                                    .setMessage(queryVideoFlagData.getMsg())
                                    .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            SelectFunctionActivity_new.this.finish();
                                        }
                                    })
                                    .setCancelable(false).show();
                        }
                    });
                }
            }
        });
    }

    //校验是否有保单
    private void checkBaoDan() {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(userid));
        map.put(Constants.en_id, en_id);
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.CHECKBAODAN, null, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                mProgressDialog.dismiss();
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
                            if (bean.getStatus() == 1) {
                                //理赔
                                if (isLiPei) {
                                    collectToNetForLiPei();
                                } else {
                                    mProgressDialog.dismiss();
                                    //预理赔
                                    Intent intent = new Intent(SelectFunctionActivity_new.this, PreparedLiPeiActivity_new.class);
                                    PreferencesUtils.saveKeyValue(Constants.fleg, "pre", MyApplication.getAppContext());
                                    startActivity(intent);
                                }

                            } else {
                                mProgressDialog.dismiss();
                                AlertDialogManager.showMessageDialogOne(SelectFunctionActivity_new.this, "提示", bean.getMsg(), new AlertDialogManager.DialogInterface() {
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
                } else {
                    mProgressDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toastUtils.showLong(SelectFunctionActivity_new.this, "验证保单失败，请重试。");
                        }
                    });
                }
            }
        });
    }

    @OnClick({R.id.select_toubao, R.id.select_lipei,
            R.id.select_xunjiandianshu, R.id.iv_cancel, R.id.select_yulipei, R.id.select_webview, R.id.tv_exit})
    public void onClick(View view) {
        switch (view.getId()) {
            //投保
            case R.id.select_toubao:
                goToActivity(InsuredActivity.class, null);
                break;
            //理赔
            case R.id.select_lipei:
                isLiPei = true;
                if (!isOPen(SelectFunctionActivity_new.this)) {
                    openGPS1(SelectFunctionActivity_new.this);
                } else {
                    checkBaoDan();

                }
                break;
            //点数
            case R.id.select_xunjiandianshu:
                goToActivity(ShowPollingActivity_new.class, null);
                //getSheData1();
                break;
            case R.id.iv_cancel:
                finish();
                break;
            //预理赔
            case R.id.select_yulipei:
                isLiPei = false;
                if (!isOPen(SelectFunctionActivity_new.this)) {
                    openGPS1(SelectFunctionActivity_new.this);
                } else {
//                    checkBaoDan();
                    startActivity(new Intent(SelectFunctionActivity_new.this, AddPigPicActivity.class));
                }
                break;
            case R.id.select_webview:
                startActivity(new Intent(SelectFunctionActivity_new.this, MonitoringActivity.class));
                break;
            case R.id.tv_exit:
                pop.showAsDropDown(rlEdit);
                loginExit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pop.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(SelectFunctionActivity_new.this)
                                .setIcon(R.drawable.cowface).setTitle("提示")
                                .setMessage("退出登录")
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //如果退出，清空保存的相关状态， 跳转到登录页
                                        PreferencesUtils.removeAllKey(SelectFunctionActivity_new.this);
                                        Intent addIntent = new Intent(SelectFunctionActivity_new.this, LoginFamerActivity.class);
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

    private void collectToNetForLiPei() {
        OkHttp3Util.doPost(Constants.LiSTART, null, new Callback() {
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
                                PreferencesUtils.saveKeyValue(Constants.fleg, "lipei", MyApplication.getAppContext());
                                PreferencesUtils.saveKeyValue(Constants.preVideoId, bean.getData(), MyApplication.getAppContext());
                                Global.model = Model.VERIFY.value();
                                Intent intent = new Intent(SelectFunctionActivity_new.this, DetectorActivity.class);
                                startActivity(intent);
//                                finish();
                            } else {
                                //showDialogError(bean.getMsg());
                                AlertDialogManager.showMessageDialog(SelectFunctionActivity_new.this, "提示", bean.getMsg(), new AlertDialogManager.DialogInterface() {
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
                            Toast.makeText(SelectFunctionActivity_new.this, "开始采集失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    private void openGPS1(Context mContext) {
        AlertDialogManager.showMessageDialog(mContext, "提示", getString(R.string.locationwarning), new AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 1315);
            }

            @Override
            public void onNegative() {

            }
        });

    }

    public static final boolean isOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    //获取猪舍信息列表
    private void getSheData1() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        Map mapbody = new HashMap();
        mapbody.put("enId", PreferencesUtils.getStringValue(Constants.en_id, SelectFunctionActivity_new.this));
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.JUANEXIT, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i("ShowPollingActivity_new", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String string = response.body().string();
                        Log.i("ShowPollingActivity_new", string);
                        JSONObject jsonObject = new JSONObject(string);
                        int status = jsonObject.getInt("status");
                        String msg = jsonObject.getString("msg");
                        if (status == 1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.dismiss();

                                }
                            });

                        } else if (status == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.dismiss();
                                    AlertDialogManager.showMessageDialogOne(SelectFunctionActivity_new.this, "提示", "您还未设置猪圈信息", new AlertDialogManager.DialogInterface() {
                                        @Override
                                        public void onPositive() {

                                        }

                                        @Override
                                        public void onNegative() {

                                        }
                                    });
                                }

                            });

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }


    private long firstTime = 0;

    @Override
    public void onBackPressed() {
        //判断是养殖场登录 可直接退出
        if ("2".equals(companyfleg)) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        } else {
            finish();
        }


    }

}
