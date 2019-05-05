package com.xiangchuang.risks.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.bean.MergeLoginBean;
import com.farm.innovation.biz.login.LoginMergeActivity;
import com.farm.innovation.login.RespObject;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.utils.FarmerShareUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.R.drawable;
import com.innovation.pig.insurance.R.id;
import com.innovation.pig.insurance.R.layout;
import com.innovation.pig.insurance.R.string;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.GsonUtils;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.BaseBean;
import com.xiangchuang.risks.model.bean.QueryVideoFlagDataBean;
import com.xiangchuang.risks.model.bean.QueryVideoFlagDataBean.thresholdList;
import com.xiangchuang.risks.model.bean.StartBean;
import com.xiangchuang.risks.model.bean.UncompletedBean;
import com.xiangchuang.risks.model.bean.WaitNumber;
import com.xiangchuang.risks.update.AppUpgradeService;
import com.xiangchuang.risks.update.UpdateInfoModel;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.AppUpdateUtils;

import org.json.JSONObject;
import org.tensorflow.demo.DetectorActivity;
import org.tensorflow.demo.Global;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import innovation.media.Model;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.farm.innovation.utils.FarmerShareUtils.MERGE_LOGIN_INFO;
import static com.innovation.pig.insurance.netutils.Constants.DISPOSE_UNFINISH;
import static com.innovation.pig.insurance.netutils.Constants.NUMBER;

public class SelectFunctionActivity_new extends BaseActivity implements View.OnClickListener {
    public static String TAG = "SelectFunctionActivity";

    ImageView iv_cancel;
    TextView mselectname;
    TextView mselecttoubao;
    RelativeLayout rel_toubao;
    TextView mselectxunjiandianshu;
    TextView selectYulipei;
    TextView mselectlipei;
    RelativeLayout relLipei;
    TextView selectWebview;
    TextView tvExit;
    RelativeLayout rlBack;
    RelativeLayout rlEdit;
    ImageView ivSign;
    private String companyname;
    private String en_id;
    private int userid;
    private String companyfleg;
    private boolean isLiPei = true;
    private PopupWindow pop;
    private TextView tvPopExit;
    private TextView tvPopUpdate;
    private ImageView ivPopUpdateSign;
    private long firstTime = 0L;
    //无害化处理按钮
    TextView tvInnocentTreatment;
    //待处理数量布局
    RelativeLayout rlCount;
    //待处理数量
    TextView tvCount;

    private int payNum;

    private UncompletedBean.currentStep currentStep;
    private boolean isUpdate;
    private UpdateInfoModel mUpdateInfoModel;

    public SelectFunctionActivity_new() {
    }

    @Override
    public void initView() {
        super.initView();
        this.iv_cancel = (ImageView) this.findViewById(id.iv_cancel);
        this.iv_cancel.setOnClickListener(this);
        this.mselectname = (TextView) this.findViewById(id.select_name);
        this.mselecttoubao = (TextView) this.findViewById(id.select_toubao);
        this.mselecttoubao.setOnClickListener(this);
        this.rel_toubao = (RelativeLayout) this.findViewById(id.rel_toubao);
        this.mselectxunjiandianshu = (TextView) this.findViewById(id.select_xunjiandianshu);
        this.mselectxunjiandianshu.setOnClickListener(this);
        this.selectYulipei = (TextView) this.findViewById(id.select_yulipei);
        this.selectYulipei.setOnClickListener(this);
        this.mselectlipei = (TextView) this.findViewById(id.select_lipei);
        this.mselectlipei.setOnClickListener(this);
        this.relLipei = (RelativeLayout) this.findViewById(id.rel_lipei);
        this.selectWebview = (TextView) this.findViewById(id.select_webview);
        this.selectWebview.setOnClickListener(this);
        this.tvExit = (TextView) this.findViewById(id.tv_exit);
        this.tvExit.setOnClickListener(this);
        this.rlBack = (RelativeLayout) this.findViewById(id.rl_back);
        this.rlEdit = (RelativeLayout) this.findViewById(id.rl_edit);
        this.ivSign = (ImageView) this.findViewById(id.iv_sign);
        this.tvInnocentTreatment = (TextView) this.findViewById(id.tv_innocent_treatment);
        this.tvInnocentTreatment.setOnClickListener(this);
        this.rlCount = (RelativeLayout) this.findViewById(id.rl_count);
        this.tvCount = (TextView) this.findViewById(id.tv_count);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_function_new;
    }

    @Override
    protected void initData() {
        this.companyname = PreferencesUtils.getStringValue("companyname", AppConfig.getAppContext(), "育肥猪农场");
        this.companyfleg = PreferencesUtils.getStringValue("companyfleg", AppConfig.getAppContext(), "0");
        Log.i("==companyfleg=", this.companyfleg + "");
        this.mselectname.setText(this.companyname);
        this.en_id = PreferencesUtils.getStringValue("en_id", AppConfig.getAppContext(), "0");
        this.userid = PreferencesUtils.getIntValue("en_user_id", AppConfig.getAppContext());
        if ("1".equals(this.companyfleg)) {
            this.rel_toubao.setVisibility(View.VISIBLE);
            this.relLipei.setVisibility(View.VISIBLE);
            this.iv_cancel.setVisibility(View.VISIBLE);
            this.rlEdit.setVisibility(View.GONE);
        } else if ("2".equals(this.companyfleg)) {
            this.iv_cancel.setVisibility(View.GONE);
            if (AppConfig.isOriginApk()) {
                rlEdit.setVisibility(View.VISIBLE);
            } else {
                rlEdit.setVisibility(View.GONE);
            }
            this.rel_toubao.setVisibility(View.GONE);
            this.relLipei.setVisibility(View.VISIBLE);
        }

        this.queryVideoFlag();
        this.pop = new PopupWindow(this);
        View popview = this.getLayoutInflater().inflate(layout.item_setting, (ViewGroup) null);
        this.tvPopExit = (TextView) popview.findViewById(id.tv_pop_exit);
        this.tvPopUpdate = (TextView) popview.findViewById(id.tv_pop_update);
        this.ivPopUpdateSign = (ImageView) popview.findViewById(id.iv_pop_update_sign);

        TextView enter_farmer = popview.findViewById(R.id.enter_farmer);

        MergeLoginBean bean = FarmerShareUtils.getData(MERGE_LOGIN_INFO);
        if (bean != null) {
            if (bean.data.nxData != null && !TextUtils.isEmpty(bean.data.nxData.token) && bean.data.nxData.status == RespObject.USER_STATUS_1) {
                enter_farmer.setVisibility(View.VISIBLE);
                enter_farmer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bean.data.ftnData != null) {
                            bean.enterByStatus = 1;
                            FarmerShareUtils.saveData(MERGE_LOGIN_INFO,bean);
                            Intent add_intent = new Intent(SelectFunctionActivity_new.this, HomeActivity.class);
                            startActivity(add_intent);
                            finish();
                        }
                    }
                });
            } else {
                enter_farmer.setVisibility(View.GONE);
            }
        } else {
            enter_farmer.setVisibility(View.GONE);
        }

        this.pop.setWidth(300);
        this.pop.setHeight(-2);
        this.pop.setBackgroundDrawable(new BitmapDrawable());
        this.pop.setFocusable(true);
        this.pop.setOutsideTouchable(true);
        this.pop.setContentView(popview);

        getNumber();
        getDisposeStep();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onEventMain(AppConfig.getUpdateInfoModel());
    }

    @Override
    public void onEventMain(UpdateInfoModel bean) {
        if(bean == null)return;
        SelectFunctionActivity_new.this.isUpdate = bean.isUpdate();
        SelectFunctionActivity_new.this.mUpdateInfoModel = bean;
        if(ivPopUpdateSign == null || ivSign == null)return;
        if (isUpdate && AppConfig.isOriginApk()) {
            ivPopUpdateSign.setVisibility(View.VISIBLE);
            ivSign.setVisibility(View.VISIBLE);
        } else {
            ivPopUpdateSign.setVisibility(View.GONE);
            ivSign.setVisibility(View.GONE);
        }
    }

    private void queryVideoFlag() {
        Map<String, String> map = new HashMap();
        map.put("animalType", "1");
        this.mProgressDialog.show();
        OkHttp3Util.doPost(Constants.QUERY_VIDEOFLAG_NEW, (Map) null, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                AVOSCloudUtils.saveErrorMessage(e, SelectFunctionActivity_new.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                Log.i(SelectFunctionActivity_new.TAG, string);
                final QueryVideoFlagDataBean queryVideoFlagData = (QueryVideoFlagDataBean) GsonUtils.getBean(string, QueryVideoFlagDataBean.class);
                if (queryVideoFlagData.getStatus() == 1) {
                    thresholdList thresholdList = (thresholdList) GsonUtils.getBean(queryVideoFlagData.getData().getThreshold(), thresholdList.class);
                    Log.e(SelectFunctionActivity_new.TAG, "queryVideoFlag thresholdList: " + thresholdList.toString());
                    PreferencesUtils.saveIntValue(Constants.lipeia, Integer.parseInt(thresholdList.getLipeiA()), SelectFunctionActivity_new.this);
                    PreferencesUtils.saveIntValue(Constants.lipeib, Integer.parseInt(thresholdList.getLipeiB()), SelectFunctionActivity_new.this);
                    PreferencesUtils.saveIntValue(Constants.lipein, Integer.parseInt(thresholdList.getLipeiN()), SelectFunctionActivity_new.this);
                    PreferencesUtils.saveIntValue(Constants.lipeim, Integer.parseInt(thresholdList.getLipeiM()), SelectFunctionActivity_new.this);
                    PreferencesUtils.saveKeyValue(Constants.phone, queryVideoFlagData.getData().getServiceTelephone(), SelectFunctionActivity_new.this);
                    PreferencesUtils.saveKeyValue(Constants.customServ, thresholdList.getCustomServ(), SelectFunctionActivity_new.this);
                    PreferencesUtils.saveKeyValue("thresholdlist", queryVideoFlagData.getData().getThreshold(), SelectFunctionActivity_new.this);
                    if (null != queryVideoFlagData.getData() && !"".equals(queryVideoFlagData.getData())) {
                        String left = queryVideoFlagData.getData().getLeftNum() == null ? "8" : queryVideoFlagData.getData().getLeftNum();
                        String middleNum = queryVideoFlagData.getData().getLeftNum() == null ? "8" : queryVideoFlagData.getData().getMiddleNum();
                        String rightNum = queryVideoFlagData.getData().getLeftNum() == null ? "8" : queryVideoFlagData.getData().getRightNum();
                        PreferencesUtils.saveKeyValue("leftNum", left, SelectFunctionActivity_new.this);
                        PreferencesUtils.saveKeyValue("middleNum", middleNum, SelectFunctionActivity_new.this);
                        PreferencesUtils.saveKeyValue("rightNum", rightNum, SelectFunctionActivity_new.this);
                    }
                } else {
                    SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            (new AlertDialog.Builder(SelectFunctionActivity_new.this)).setIcon(drawable.cowface).setTitle("提示").setMessage(queryVideoFlagData.getMsg()).setPositiveButton("退出", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    SelectFunctionActivity_new.this.finish();
                                }
                            }).setCancelable(false).show();
                        }
                    });
                }
            }
        });
    }

    private void checkBaoDan() {
        Map<String, String> map = new HashMap();
        map.put("AppKeyAuthorization", "hopen");
        map.put("en_user_id", String.valueOf(this.userid));
        map.put("en_id", this.en_id);
        this.mProgressDialog.show();
        OkHttp3Util.doPost(Constants.CHECKBAODAN, (Map) null, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(SelectFunctionActivity_new.TAG, e.toString());
                SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                AVOSCloudUtils.saveErrorMessage(e, SelectFunctionActivity_new.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(SelectFunctionActivity_new.TAG, string);
                final StartBean bean = (StartBean) GsonUtils.getBean(string, StartBean.class);
                if (null != bean) {
                    SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bean.getStatus() == 1) {
                                if (SelectFunctionActivity_new.this.isLiPei) {
                                    SelectFunctionActivity_new.this.collectToNetForLiPei();
                                } else {
                                    SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                                    Intent intent = new Intent(SelectFunctionActivity_new.this, PreparedLiPeiActivity_new.class);
                                    PreferencesUtils.saveKeyValue("fleg", "pre", AppConfig.getAppContext());
                                    SelectFunctionActivity_new.this.startActivity(intent);
                                }
                            } else {
                                SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                                AlertDialogManager.showMessageDialogOne(SelectFunctionActivity_new.this, "提示", bean.getMsg(), new com.xiangchuang.risks.utils.AlertDialogManager.DialogInterface() {
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
                    SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                    SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SelectFunctionActivity_new.this.toastUtils.showLong(SelectFunctionActivity_new.this, "验证保单失败，请重试。");
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == id.select_toubao) {
            this.goToActivity(InsuredActivity.class, (Bundle) null);
        } else if (i == id.select_lipei) {
            this.isLiPei = true;
            if (!isOPen(this)) {
                this.openGPS1(this);
            } else {
                this.checkBaoDan();
            }
        } else if (i == id.select_xunjiandianshu) {
            this.goToActivity(ShowPollingActivity_new.class, (Bundle) null);
        } else if (i == id.iv_cancel) {
            this.finish();
        } else if (i == id.select_yulipei) {
            this.isLiPei = false;
            if (!isOPen(this)) {
                this.openGPS1(this);
            } else {
                this.checkBaoDan();
            }
        } else if (i == id.select_webview) {
            this.startActivity(new Intent(this, MonitoringActivity.class));
        } else if (i == id.tv_exit) {
            this.ivSign.setVisibility(View.GONE);
            this.pop.showAsDropDown(this.rlEdit);
            this.tvPopExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SelectFunctionActivity_new.this.pop.dismiss();
                    AlertDialog.Builder builder = (new AlertDialog.Builder(SelectFunctionActivity_new.this)).setIcon(drawable.cowface).setTitle("提示").setMessage("退出登录").setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PreferencesUtils.removeAllKey(SelectFunctionActivity_new.this);
                            FarmerShareUtils.clearMergeLoginInfo();
//                            Intent addIntent = new Intent(SelectFunctionActivity_new.this, LoginPigAarActivity.class);
                            Intent addIntent = new Intent(SelectFunctionActivity_new.this, LoginMergeActivity.class);
                            SelectFunctionActivity_new.this.startActivity(addIntent);
                            SelectFunctionActivity_new.this.finish();
                        }
                    }).setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }
            });
            this.tvPopUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SelectFunctionActivity_new.this.pop.dismiss();
                    AlertDialog.Builder mDialog;
                    if (isUpdate) {
                        if (SelectFunctionActivity_new.this.ivSign.getVisibility() == View.VISIBLE) {
                            SelectFunctionActivity_new.this.ivSign.setVisibility(View.GONE);
                        }

                        mDialog = new AlertDialog.Builder(SelectFunctionActivity_new.this);
                        mDialog.setIcon(drawable.cowface);
                        mDialog.setTitle("版本更新");
                        mDialog.setMessage(mUpdateInfoModel.getUpgradeinfo());
                        mDialog.setCancelable(false);
                        mDialog.setPositiveButton("马上升级", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SelectFunctionActivity_new.this.ivPopUpdateSign.setVisibility(View.GONE);
                                Intent mIntent = new Intent(SelectFunctionActivity_new.this, AppUpgradeService.class);
                                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mIntent.putExtra("data", mUpdateInfoModel);
                                SelectFunctionActivity_new.this.startService(mIntent);
                            }
                        }).setNegativeButton("稍后再说", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    } else {
                        mDialog = new AlertDialog.Builder(SelectFunctionActivity_new.this);
                        mDialog.setIcon(drawable.cowface);
                        mDialog.setTitle("提示");
                        mDialog.setMessage("当前已是最新版本");
                        mDialog.setCancelable(false);
                        mDialog.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                }
            });
        } else if (i == id.tv_innocent_treatment) {
            getUnfinish();
        }
    }

    /**
     * 生成无害化处理信息
     */
    private void getUnfinish() {
        this.mProgressDialog.show();
        OkHttp3Util.doPost(DISPOSE_UNFINISH, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                        Toast.makeText(SelectFunctionActivity_new.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, WaitDisposeActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);

                BaseBean<UncompletedBean> result;
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<BaseBean<UncompletedBean>>() {
                    }.getType();
                    result = gson.fromJson(string, type);
                    SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                            if (null != result) {
                                if (result.getStatus() == 1) {
                                    UncompletedBean uncompletedBean = result.getData();
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("Uncompleted", uncompletedBean);
                                    goToActivity(DeadPigProcessStepActivity.class, bundle);
                                } else {
                                    if (payNum > 0) {
                                        SelectFunctionActivity_new.this.goToActivity(WaitDisposeActivity.class, null);
                                    } else {
                                        Toast.makeText(SelectFunctionActivity_new.this, "您还没有理赔数据", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(SelectFunctionActivity_new.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取待处理理赔数量
     */
    private void getNumber() {
        OkHttp3Util.doPost(NUMBER, (Map) null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(SelectFunctionActivity_new.TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e, SelectFunctionActivity_new.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(SelectFunctionActivity_new.TAG, string);

                BaseBean<WaitNumber> result;
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<BaseBean<WaitNumber>>() {
                    }.getType();
                    result = gson.fromJson(string, type);

                    if (null != result) {
                        SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                payNum = result.getData().getNumber();

                                if (payNum > 0) {
                                    rlCount.setVisibility(View.VISIBLE);
                                    tvCount.setText(payNum + "");
                                } else {
                                    rlCount.setVisibility(View.GONE);
                                }
                            }
                        });
                    } else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取处理步骤
     */
    private void getDisposeStep() {
        OkHttp3Util.doPost(Constants.DISPOSE_STEP, (Map) null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(SelectFunctionActivity_new.TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e, SelectFunctionActivity_new.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(SelectFunctionActivity_new.TAG, string);

                BaseBean<UncompletedBean.currentStep> result;
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<BaseBean<UncompletedBean.currentStep>>() {
                    }.getType();
                    result = gson.fromJson(string, type);

                    if (null != result) {
                        currentStep = result.getData();
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void collectToNetForLiPei() {
        OkHttp3Util.doPost(Constants.LiSTART, (Map) null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(SelectFunctionActivity_new.TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e, SelectFunctionActivity_new.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(SelectFunctionActivity_new.TAG, string);
                final StartBean bean = (StartBean) GsonUtils.getBean(string, StartBean.class);
                if (null != bean) {
                    SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                PreferencesUtils.saveKeyValue("fleg", "lipei", AppConfig.getAppContext());
                                PreferencesUtils.saveKeyValue("preCompensateVideoId", bean.getData(), AppConfig.getAppContext());
                                Global.model = Model.VERIFY.value();
                                Intent intent = new Intent(SelectFunctionActivity_new.this, DetectorActivity.class);
                                SelectFunctionActivity_new.this.startActivity(intent);
                            } else {
                                AlertDialogManager.showMessageDialog(SelectFunctionActivity_new.this, "提示", bean.getMsg(), new com.xiangchuang.risks.utils.AlertDialogManager.DialogInterface() {
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
                    SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SelectFunctionActivity_new.this, "开始采集失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void openGPS1(Context mContext) {
        AlertDialogManager.showMessageDialog(mContext, "提示", this.getString(string.locationwarning), new com.xiangchuang.risks.utils.AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {
                Intent intent = new Intent();
                intent.setAction("android.settings.LOCATION_SOURCE_SETTINGS");
                SelectFunctionActivity_new.this.startActivityForResult(intent, 1315);
            }

            @Override
            public void onNegative() {
            }
        });
    }

    public static final boolean isOPen(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    private void getSheData1() {
        Map map = new HashMap();
        map.put("AppKeyAuthorization", "hopen");
        Map mapbody = new HashMap();
        mapbody.put("enId", PreferencesUtils.getStringValue("en_id", this));
        this.mProgressDialog.show();
        OkHttp3Util.doPost(Constants.JUANEXIT, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                Log.i("ShowPollingActivity_new", e.toString());
                AVOSCloudUtils.saveErrorMessage(e, SelectFunctionActivity_new.class.getSimpleName());
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
                            SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                                }
                            });
                        } else if (status == 0) {
                            SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                                    AlertDialogManager.showMessageDialogOne(SelectFunctionActivity_new.this, "提示", "您还未设置猪圈信息", new com.xiangchuang.risks.utils.AlertDialogManager.DialogInterface() {
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
                    } catch (Exception var7) {
                        var7.printStackTrace();
                        AVOSCloudUtils.saveErrorMessage(var7, SelectFunctionActivity_new.class.getSimpleName());
                    }
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        if ("2".equals(this.companyfleg)) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - this.firstTime > 2000L) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                this.firstTime = secondTime;
            } else {
                Process.killProcess(Process.myPid());
                System.exit(0);
            }
        } else {
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
