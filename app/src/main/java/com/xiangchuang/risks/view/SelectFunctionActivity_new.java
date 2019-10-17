//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.xiangchuang.risks.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hjq.toast.ToastUtils;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.orhanobut.logger.Logger;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.BaseBean;
import com.xiangchuang.risks.model.bean.BubbleDataBean;
import com.xiangchuang.risks.model.bean.GSCPigBean;
import com.xiangchuang.risks.model.bean.JudgeRecordVideo;
import com.xiangchuang.risks.model.bean.PayInfo;
import com.xiangchuang.risks.model.bean.StartBean;
import com.xiangchuang.risks.model.bean.UncompletedBean;
import com.xiangchuang.risks.model.bean.WaitNumber;
import com.xiangchuang.risks.update.UpdateInfoModel;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.AppUpdateUtils;
import com.xiangchuang.risks.utils.NoWeighingDialog;
import com.xiangchuang.risks.utils.PigPreferencesUtils;
import com.xiangchuangtec.luolu.animalcounter.JPushStatsConfig;
import com.xiangchuangtec.luolu.animalcounter.PigAppConfig;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;

import org.tensorflow.demo.DetectorActivity_pig;
import org.tensorflow.demo.Global;
import org.tensorflow.demo.SmallVideoActivity;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import innovation.database.CompanyInfo;
import innovation.database.CompanyInfo_;
import innovation.database.OffLineDataInfos;
import innovation.database.OffLineDataInfos_;
import innovation.database.SheInfo;
import innovation.database.SheInfo_;
import innovation.media.Model;
import innovation.utils.HttpUtils;
import innovation.utils.PigInnovationAiOpen;
import innovation.utils.Toast;
import io.objectbox.Box;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.PIG_DEPTH_JOIN;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.offLineModle;
import static com.xiangchuangtec.luolu.animalcounter.netutils.Constants.BUBBLE_DATA;
import static com.xiangchuangtec.luolu.animalcounter.netutils.Constants.DISPOSE_UNFINISH;
import static com.xiangchuangtec.luolu.animalcounter.netutils.Constants.GET_PAY_LIST;
import static com.xiangchuangtec.luolu.animalcounter.netutils.Constants.JUDGE_RECORD_VIDEO;
import static com.xiangchuangtec.luolu.animalcounter.netutils.Constants.NUMBER;

public class SelectFunctionActivity_new extends BaseActivity implements OnClickListener, PigAppConfig.eventListener {

    public static String TAG = "SelectFunctionActivity";
    ImageView iv_cancel;
    TextView mselectname, tv_farm_name, tv_farm_address, tv_seven_text, tv_user_id;
    CircleImageView iv_user_icon;
    RelativeLayout rl_one_func, rl_two_func, rl_three_func, rl_four_func, rl_five_func, rl_sax_func;
    LinearLayout ll_one_line, ll_two_line, ll_three_line, ll_four_line, rl_seven_func;
    TextView tvExit;
    RelativeLayout rlBack;
    RelativeLayout rlEdit;
    ImageView ivSign;
    RelativeLayout rl_three_func_check_number;
    LinearLayout ll_check_number_layout;
    Button submitBtn;
    private String companyname;
    private String companyfleg;
    private boolean isLiPei = true;

    private LinearLayout rlCompanyInfo;

    private long firstTime = 0L;
    //无害化处理按钮
    private RelativeLayout rl_video_monitor;
    //待处理数量布局
    private RelativeLayout rlCount;
    //待处理数量
    private TextView tvCount;

    //投保数量布局
    private RelativeLayout rl_toubao_count, rl_lipei_count;
    //投保数量
    private TextView tv_toubao_count, tv_lipei_count;

    private int payNum;
    private boolean isUpdate;
    private boolean mIsHaveInnocuous;
    private UpdateInfoModel mUpdateInfoModel;
    private UncompletedBean.currentStep currentStep;
    private String taskId = "";

    Dialog dialog;
    FileWriter fileWriter;

    public SelectFunctionActivity_new() {
    }

    @Override
    public void initView() {
        super.initView();
        this.iv_cancel = (ImageView) this.findViewById(R.id.iv_cancel);
        this.iv_cancel.setOnClickListener(this);
        this.mselectname = (TextView) this.findViewById(R.id.select_name);
        this.tvExit = (TextView) this.findViewById(R.id.tv_exit);
        this.tvExit.setOnClickListener(this);
        this.rlBack = (RelativeLayout) this.findViewById(R.id.rl_back);
        this.rlEdit = (RelativeLayout) this.findViewById(R.id.rl_edit);
        this.ivSign = (ImageView) this.findViewById(R.id.iv_sign);
        this.rl_video_monitor = this.findViewById(R.id.rl_video_monitor);
        this.rl_video_monitor.setOnClickListener(this);
        iv_user_icon = findViewById(R.id.iv_user_icon);
        tv_farm_name = findViewById(R.id.tv_farm_name);
        tv_user_id = findViewById(R.id.tv_user_id);
        tv_farm_address = findViewById(R.id.tv_farm_address);
        rl_one_func = findViewById(R.id.rl_one_func);
        rl_two_func = findViewById(R.id.rl_two_func);
        rl_three_func = findViewById(R.id.rl_three_func);
        rl_four_func = findViewById(R.id.rl_four_func);
        rl_five_func = findViewById(R.id.rl_five_func);
        rl_sax_func = findViewById(R.id.rl_sax_func);
        rl_seven_func = findViewById(R.id.rl_seven_func);

        ll_four_line = findViewById(R.id.ll_four_line);
        ll_one_line = findViewById(R.id.ll_one_line);
        ll_two_line = findViewById(R.id.ll_two_line);
        ll_three_line = findViewById(R.id.ll_three_line);
        rlCompanyInfo = findViewById(R.id.rl_company_info);
        rl_toubao_count = findViewById(R.id.rl_toubao_count);
        tv_toubao_count = findViewById(R.id.tv_toubao_count);

        rl_lipei_count = findViewById(R.id.rl_lipei_count);
        tv_lipei_count = findViewById(R.id.tv_lipei_count);

        rlCount = findViewById(R.id.rl_position_one_count);
        tvCount = findViewById(R.id.tv_position_one_count);

        rl_one_func.setOnClickListener(this);
        rl_two_func.setOnClickListener(this);
        rl_three_func.setOnClickListener(this);
        rl_four_func.setOnClickListener(this);
        rl_five_func.setOnClickListener(this);
        rl_sax_func.setOnClickListener(this);
        rl_seven_func.setOnClickListener(this);

        ll_four_line.setOnClickListener(this);
        if (!PIG_DEPTH_JOIN) {
            rlCompanyInfo.setOnClickListener(this);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_function_new;
    }

    @Override
    protected void initData() {
        if (null != getIntent().getBundleExtra("data")) {
            taskId = getIntent().getBundleExtra("data").getString(PigAppConfig.TASK_ID, "");
        }

        this.companyname = PigPreferencesUtils.getStringValue("companyname", PigAppConfig.getAppContext(), "育肥猪农场");
        this.companyfleg = PigPreferencesUtils.getStringValue("companyfleg", PigAppConfig.getAppContext(), "0");
        Log.i("==companyfleg=", this.companyfleg + "");
        this.mselectname.setText(this.companyname);
//        companyfleg = "2";

        //  1 保险公司 2 养殖场
        if ("2".equals(this.companyfleg)) {
            AppUpdateUtils appUpdateUtils = new AppUpdateUtils();
            appUpdateUtils.appVersionCheck(SelectFunctionActivity_new.this, new AppUpdateUtils.UpdateResultListener() {
                @Override
                public void update(boolean isUpdate, UpdateInfoModel bean) {
                    //todo nothing 此处不再做非强制更新状态处理，非强制更新在设置中处理
                    if (isUpdate) {
                        ivSign.setVisibility(View.VISIBLE);
                        PigPreferencesUtils.saveKeyValue(Constants.isUpdate, "1", PigAppConfig.getAppContext());
                    } else {
                        PigPreferencesUtils.saveKeyValue(Constants.isUpdate, "0", PigAppConfig.getAppContext());
                    }
                }
            });
            getEnDetail();
        } else if ("1".equals(this.companyfleg)) {
            String enId = PigPreferencesUtils.getStringValue(Constants.en_id, mActivity);
            Box<CompanyInfo> companyInfoBox = PigAppConfig.getBoxStore().boxFor(CompanyInfo.class);
            CompanyInfo companyInfo = companyInfoBox.query().equal(CompanyInfo_.enId, enId).build().findUnique();
            if (companyInfo != null) {
                String showText = companyInfo.enName + "    账户ID：" + companyInfo.enId;
                SpannableString spannableString = new SpannableString(showText);
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#666666")),
                        companyInfo.enName.length(), showText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new AbsoluteSizeSpan(14, true),
                        companyInfo.enName.length(), showText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                tv_farm_name.setText(companyInfo.enName);
                tv_user_id.setText("账户ID：" + companyInfo.enId);
                tv_farm_address.setText("地址：" + companyInfo.enAddress);
            } else {
                getEnDetail();
            }
            getEnDetail();
        }

        String enName = PigPreferencesUtils.getStringValue(Constants.companyname, mActivity);
        int enUId = PigPreferencesUtils.getIntValue(Constants.en_user_id, mActivity);
        if (!TextUtils.isEmpty(enName)) {
            String showText = enName + "    账户ID：" + enUId;
            SpannableString spannableString = new SpannableString(showText);
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#666666")), enName.length(), showText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new AbsoluteSizeSpan(14, true), enName.length(), showText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            tv_farm_name.setText(enName);
            tv_user_id.setText("账户ID：" + enUId);
        }

        refreshFuncList();

        if (dialog == null || !dialog.isShowing()) {
            getNumber();
            getDisposeStep();
            getPayList();
            getTouBao();
        }
        PigAppConfig.registEvent(this);
        if (offLineModle) {
            if (PigAppConfig.PIG_DEPTH_JOIN) {
                android.widget.Toast.makeText(mActivity, "请打开网络链接！", Toast.LENGTH_SHORT).show();
            } else {
//                goToActivity(SelectFunctionActivity_OffLine.class, null);
            }
            finish();
        }
    }

    private void refreshFuncList() {
        if (PigAppConfig.PIG_DEPTH_JOIN) {
            mIsHaveInnocuous = false;
        }
        if ("1".equals(this.companyfleg)) {
            this.iv_cancel.setVisibility(View.VISIBLE);
            this.rlEdit.setVisibility(View.GONE);

            if (mIsHaveInnocuous) {
                ll_four_line.setVisibility(View.VISIBLE);
            } else {
                ll_four_line.setVisibility(View.GONE);
            }
            this.rlCount = (RelativeLayout) this.findViewById(R.id.rl_position_one_count);
            this.tvCount = (TextView) this.findViewById(R.id.tv_position_one_count);
        } else if ("2".equals(this.companyfleg)) {
            this.iv_cancel.setVisibility(View.GONE);
            this.rlEdit.setVisibility(View.VISIBLE);
            this.ivSign.setVisibility(View.GONE);
            ll_one_line.setVisibility(View.GONE);

            if (mIsHaveInnocuous) {
                ll_four_line.setVisibility(View.VISIBLE);
            } else {
                ll_four_line.setVisibility(View.GONE);
            }
            this.rlCount = (RelativeLayout) this.findViewById(R.id.rl_position_one_count);
            this.tvCount = (TextView) this.findViewById(R.id.tv_position_one_count);
        }

        if (PigAppConfig.PIG_DEPTH_JOIN) {
            runOnUiThread(() -> {
                rlEdit.setVisibility(View.INVISIBLE);
                ll_check_number_layout = findViewById(R.id.ll_check_number_layout);
                submitBtn = findViewById(R.id.function_submit_btn);
                rl_three_func_check_number = findViewById(R.id.rl_three_func_check_number);

                if (Global.model == PigInnovationAiOpen.INSURE) {
                    ll_check_number_layout.setVisibility(View.VISIBLE);
                    ll_one_line.setVisibility(View.VISIBLE);
                    ll_two_line.setVisibility(View.GONE);
                    ll_three_line.setVisibility(View.GONE);
                    ll_four_line.setVisibility(View.GONE);
                } else {
                    ll_check_number_layout.setVisibility(View.GONE);
                    ll_one_line.setVisibility(View.GONE);
                    ll_two_line.setVisibility(View.GONE);
                    ll_three_line.setVisibility(View.VISIBLE);
                    findViewById(R.id.cv_sax_func).setVisibility(View.INVISIBLE);
                    ll_four_line.setVisibility(View.GONE);
                }
                submitBtn.setVisibility(View.VISIBLE);
                submitBtn.setOnClickListener(this);
                rl_three_func_check_number.setOnClickListener(this);
            });
            try {
                fileWriter = new FileWriter(getExternalCacheDir().getAbsolutePath().concat("pig.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkBaoDan() {
        this.mProgressDialog.show();
        OkHttp3Util.doPost(Constants.CHECKBAODAN, (Map) null, new Callback() {
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
                                    PigPreferencesUtils.saveKeyValue("fleg", "pre", PigAppConfig.getAppContext());
                                    SelectFunctionActivity_new.this.startActivity(intent);
                                }
                            } else {
                                SelectFunctionActivity_new.this.mProgressDialog.dismiss();
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
                    SelectFunctionActivity_new.this.mProgressDialog.dismiss();
                    SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SelectFunctionActivity_new.this, "验证保单失败，请重试。", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });

    }

    public static HashMap<String, List<GSCPigBean>> g_CaptivityMap = new HashMap<>();
    public static HashMap<String, List<GSCPigBean>> g_LocationMap = new HashMap<>();
    public static String g_SheID = "";
    public static String g_PigType = "";
    //    public static int totalFarmPigs = 0;
    public static HashMap<String, Integer> g_TotalMap = new HashMap<>();

    public static List<GSCPigBean> gscPigBeans = new ArrayList<>();

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.rl_one_func) {
            //投保
            JPushStatsConfig.onCountEvent(SelectFunctionActivity_new.this, "insure", null);
            this.goToActivity(InsuredActivity.class, (Bundle) null);
        } else if (i == R.id.rl_two_func) {
            //猪舍
            JPushStatsConfig.onCountEvent(SelectFunctionActivity_new.this, "hoggery", null);
            this.goToActivity(PigHouseListActivity.class, (Bundle) null);
        } else if (i == R.id.rl_three_func || i == R.id.rl_three_func_check_number) {
            //点数
            JPushStatsConfig.onCountEvent(SelectFunctionActivity_new.this, "count", null);
            this.goToActivity(ShowPollingActivity_new.class, (Bundle) null);
        } else if (i == R.id.rl_four_func) {
            //预理赔
            JPushStatsConfig.onCountEvent(SelectFunctionActivity_new.this, "preliminary_adjustments", null);
            this.isLiPei = false;
            List<OffLineDataInfos> offLineDataInfosList = new ArrayList<>();
            String enId = PigPreferencesUtils.getStringValue(Constants.en_id, PigAppConfig.getAppContext(), "");
            Box<OffLineDataInfos> offLineBox = PigAppConfig.getBoxStore().boxFor(OffLineDataInfos.class);
            offLineDataInfosList = offLineBox.query().equal(OffLineDataInfos_.enId, enId).build().find();

            if (offLineDataInfosList.size() > 0) {
                goToPrePayList();
            } else {
                if (!isOPen(this)) {
                    this.openGPS1(this);
                } else {
                    getJudgeRecordVideo();
                }
            }
        } else if (i == R.id.rl_five_func) {
            //理赔
            this.isLiPei = true;
            JPushStatsConfig.onCountEvent(SelectFunctionActivity_new.this, "claim_settlement", null);
            if (PIG_DEPTH_JOIN) {
                if (!isOPen(this)) {
                    this.openGPS1(this);
                } else {
                    getJudgeRecordVideo();
                }
                return;
            }
            List<OffLineDataInfos> offLineDataInfosList = new ArrayList<>();
            String enId = PigPreferencesUtils.getStringValue(Constants.en_id, PigAppConfig.getAppContext(), "");
            Box<OffLineDataInfos> offLineBox = PigAppConfig.getBoxStore().boxFor(OffLineDataInfos.class);
            offLineDataInfosList = offLineBox.query().equal(OffLineDataInfos_.enId, enId).build().find();

            if (offLineDataInfosList.size() > 0) {
                goToPrePayList();
            } else {
                if (!isOPen(this)) {
                    this.openGPS1(this);
                } else {
                    getJudgeRecordVideo();
                }
            }
        } else if (i == R.id.rl_sax_func) {
            JPushStatsConfig.onCountEvent(this, "piggery_monitoring", null);
            this.startActivity(new Intent(this, MonitoringActivity.class));
        } else if (i == R.id.rl_seven_func) {
            JPushStatsConfig.onCountEvent(this, "innocent_treatment", null);
            getUnfinish();
        } else if (i == R.id.iv_cancel) {
            this.finish();
        } else if (i == R.id.tv_exit) {
            ivSign.setVisibility(View.GONE);
            this.goToActivity(SettingActivity.class, (Bundle) null);
        } else if (i == R.id.rl_company_info) {
//            this.goToActivity(CompanyInfoActivity.class, (Bundle) null);
        } else if (i == R.id.function_submit_btn) {
            if (Global.model == PigInnovationAiOpen.INSURE) {
                if (g_CaptivityMap.size() == 0 && g_LocationMap.size() == 0) {
                    android.widget.Toast.makeText(mActivity, "提交数据不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                int[] callbackCount = {0, 0};//
                view.setEnabled(false);
                int farm_total_cnt = 0;
                //第一次遍历获取：投保某一种类猪的存栏数量、猪种类、猪厂采集总数
                for (Map.Entry<String, List<GSCPigBean>> entry : g_CaptivityMap.entrySet()) {//圈养
                    List<GSCPigBean> beanList = entry.getValue();
                    if (beanList != null) {
                        GSCPigBean gscPigBean = beanList.get(0);
                        int totalCnt = gscPigBean.totalPigs;
                        farm_total_cnt += totalCnt;
                        if (g_TotalMap.containsKey(gscPigBean.pigType)) {
                            totalCnt += g_TotalMap.get(gscPigBean.pigType);
                            g_TotalMap.put(gscPigBean.pigType, totalCnt);
                        } else {
                            g_TotalMap.put(gscPigBean.pigType, totalCnt);
                        }
                    }
                }
                for (Map.Entry<String, List<GSCPigBean>> entry : g_LocationMap.entrySet()) {//定位栏
                    List<GSCPigBean> beanList = entry.getValue();
                    if (beanList != null) {
                        GSCPigBean gscPigBean = beanList.get(0);
                        int totalCnt = gscPigBean.totalPigs;
                        farm_total_cnt += totalCnt;
                        if (g_TotalMap.containsKey(gscPigBean.pigType)) {
                            totalCnt += g_TotalMap.get(gscPigBean.pigType);
                            g_TotalMap.put(gscPigBean.pigType, totalCnt);
                        } else {
                            g_TotalMap.put(gscPigBean.pigType, totalCnt);
                        }
                    }
                }
                //第二次遍历给GSCPigBean的totalFarmPigs字段赋值
                ArrayList<GSCPigBean> arrayList = new ArrayList<GSCPigBean>();
                for (Map.Entry<String, List<GSCPigBean>> entry : g_CaptivityMap.entrySet()) {
                    List<GSCPigBean> beanList = entry.getValue();
                    if (beanList != null) {
                        for (GSCPigBean sgscPigBean : beanList) {
                            if (null != g_LocationMap.get(entry.getKey())) {
                                sgscPigBean.totalPigs += g_LocationMap.get(entry.getKey()).get(0).totalPigs;
                            }
                            sgscPigBean.totalFarmPigs = farm_total_cnt;
                        }
                        arrayList.addAll(beanList);
                    }
                }
                for (Map.Entry<String, List<GSCPigBean>> entry : g_LocationMap.entrySet()) {
                    List<GSCPigBean> beanList = entry.getValue();
                    if (beanList != null) {
                        for (GSCPigBean sgscPigBean : beanList) {
                            if (null != g_CaptivityMap.get(entry.getKey())) {
                                sgscPigBean.totalPigs += g_CaptivityMap.get(entry.getKey()).get(0).totalPigs;
                            }
                            sgscPigBean.totalFarmPigs = farm_total_cnt;
                        }
                        arrayList.addAll(beanList);
                    }
                }
//                try {
//                    for (GSCPigBean pigBean : arrayList) {
//                        fileWriter.append(pigBean.string());
//                    }
//                    fileWriter.flush();
//                    fileWriter.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                if (AppConfig.isSDK_DEBUG())
                    android.widget.Toast.makeText(mActivity, "本次投保猪类型总数--1："+g_TotalMap.size(), Toast.LENGTH_SHORT).show();
                ProgressDialog progressDialog = ProgressDialog.show(this, "", "数据处理中。。。", false);
                for (Map.Entry<String, Integer> entry : g_TotalMap.entrySet()) {
                    Map map = new HashMap();
                    map.put("taskId", taskId);
                    map.put("enUserId", PigPreferencesUtils.getIntValue(Constants.en_user_id, PigAppConfig.getAppContext()) + "");
                    map.put("enId", PigPreferencesUtils.getStringValue(Constants.en_id, this));
                    map.put("amount", entry.getValue().toString());
                    map.put("ratio", "1");
                    map.put("pigType", entry.getKey());
                    OkHttp3Util.doPost(HttpUtils.GSC_INSURE_IMAGE_UPLOAD, map, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            callbackCount[0]++;
                            if (AppConfig.isSDK_DEBUG())
                                runOnUiThread(() -> {
                                    android.widget.Toast.makeText(mActivity, "猪投保失败了!--2" + callbackCount[0], Toast.LENGTH_SHORT).show();
                                });
                            if (callbackCount[0] == g_TotalMap.size() && callbackCount[1] == 0) {
                                callbackCount[1] = 1;
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    view.setEnabled(true);
                                    Message msg = Message.obtain();
                                    msg.obj = null;
                                    msg.what = PigInnovationAiOpen.INSURE;
                                    PigInnovationAiOpen.getInstance().postEventEvent(msg);
                                    SelectFunctionActivity_new.this.finish();
                                });

                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            callbackCount[0]++;
                            if (AppConfig.isSDK_DEBUG())
                                runOnUiThread(() -> {
                                    android.widget.Toast.makeText(mActivity, "猪投保成功了!--3" + callbackCount[0], Toast.LENGTH_SHORT).show();
                                });
                            if (callbackCount[0] == g_TotalMap.size() && callbackCount[1] == 0) {
                                callbackCount[1] = 1;
                                String result = "";
                                List<GSCPigBean> listBean = null;
                                if (response.isSuccessful()) {
                                    result = response.body().string();
                                    Type listType = new TypeToken<BaseBean<List<GSCPigBean>>>() {
                                    }.getType();
                                    Gson gson = new Gson();
                                    BaseBean<List<GSCPigBean>> baseBean = gson.fromJson(result, listType);
                                    listBean = baseBean.getData();
                                }
                                List<GSCPigBean> finalListBean = listBean;
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    view.setEnabled(true);
                                    Message msg = Message.obtain();
                                    msg.obj = finalListBean;//arrayList;
                                    msg.what = PigInnovationAiOpen.INSURE;
                                    PigInnovationAiOpen.getInstance().postEventEvent(msg);
                                    SelectFunctionActivity_new.this.finish();
                                });
                            }
                        }
                    });
                }
            } else {
                if (!(gscPigBeans.size() > 0)) {
                    ToastUtils.show("提交数据不能为空！");
                    return;
                }
                view.setEnabled(false);
                Message msg = Message.obtain();
                msg.obj = gscPigBeans;
                msg.what = PigInnovationAiOpen.PAY;
                PigInnovationAiOpen.getInstance().postEventEvent(msg);
                SelectFunctionActivity_new.this.finish();
                view.setEnabled(true);
            }
        }
    }

    private void goToPrePayList() {
        AlertDialogManager.showMessageDialog(mActivity, "提示", "当前存在离线数据，请先上传离线数据后再进行操作。", new AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {
//                SelectFunctionActivity_new.this.startActivity(new Intent(SelectFunctionActivity_new.this, PrePayListActivity.class));
            }

            @Override
            public void onNegative() {

            }
        });
    }

    /**
     * 获取未完成的无害化处理信息
     */
    private void getUnfinish() {
        if (PigAppConfig.PIG_DEPTH_JOIN) {
            return;
        }
        this.mLoadProgressDialog.show();
        OkHttp3Util.doPost(DISPOSE_UNFINISH, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SelectFunctionActivity_new.this.mLoadProgressDialog.dismiss();
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
                            SelectFunctionActivity_new.this.mLoadProgressDialog.dismiss();
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
     * 获取待无害化处理数量
     */
    private void getNumber() {
        if (PigAppConfig.PIG_DEPTH_JOIN) {
            return;
        }
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
        if (PigAppConfig.PIG_DEPTH_JOIN) {
            return;
        }
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
                        SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (0 == result.getStatus()) {
                                    mIsHaveInnocuous = false;
                                    refreshFuncList();
                                } else if (1 == result.getStatus()) {
                                    mIsHaveInnocuous = true;
                                    refreshFuncList();
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
                                PigPreferencesUtils.saveKeyValue("fleg", "lipei", PigAppConfig.getAppContext());
                                PigPreferencesUtils.saveKeyValue("preCompensateVideoId", bean.getData(), PigAppConfig.getAppContext());
                                PigPreferencesUtils.saveKeyValue(Constants.preTimesFlag, "", PigAppConfig.getAppContext());
                                Global.model = Model.VERIFY.value();
                                Intent intent = new Intent(SelectFunctionActivity_new.this, DetectorActivity_pig.class);
                                SelectFunctionActivity_new.this.startActivity(intent);
                            } else {
                                AlertDialogManager.showMessageDialog(SelectFunctionActivity_new.this, "提示", bean.getMsg(), new AlertDialogManager.DialogInterface() {
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
        AlertDialogManager.showMessageDialog(mContext, "提示", this.getString(R.string.locationwarning), new AlertDialogManager.DialogInterface() {
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

    /**
     * 获取养殖场详情信息
     */
    private void getEnDetail() {
        OkHttp3Util.doPost(Constants.ENDETAIL, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AVOSCloudUtils.saveErrorMessage(e, SelectFunctionActivity_new.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        BaseBean<CompanyInfo> result;
                        String string = response.body().string();
                        Log.i("ShowPollingActivity_new", string);
                        Gson gson = new Gson();
                        Type type = new TypeToken<BaseBean<CompanyInfo>>() {
                        }.getType();
                        result = gson.fromJson(string, type);
                        if (result.getStatus() == 1) {
                            SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CompanyInfo rInfo = result.getData();
                                    String showText = rInfo.enName + "    账户ID：" + rInfo.enId;
                                    SpannableString spannableString = new SpannableString(showText);
                                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#666666")), rInfo.enName.length(), showText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spannableString.setSpan(new AbsoluteSizeSpan(14, true), rInfo.enName.length(), showText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                                    tv_farm_name.setText(rInfo.enName);
                                    tv_user_id.setText("账户ID：" + rInfo.enId);
                                    tv_farm_address.setText("地址：" + rInfo.enAddress);

                                    /* 将数据插入数据库 */
                                    Box<CompanyInfo> box = PigAppConfig.getBoxStore().boxFor(CompanyInfo.class);
                                    CompanyInfo cInfo = box.query().equal(CompanyInfo_.enId, rInfo.enId).build().findUnique();

                                    Box<SheInfo> sheBox = PigAppConfig.getBoxStore().boxFor(SheInfo.class);

                                    List<SheInfo> sheInfoList = new ArrayList<>(rInfo.sheInfo);
                                    if (cInfo == null) {
                                        box.put(rInfo);
                                    }
                                    //更新数据
                                    List<SheInfo> sheInfos = sheBox.query().equal(SheInfo_.enId, rInfo.enId).build().find();
                                    if (sheInfos != null && sheInfos.size() > 0) {
                                        sheBox.remove(sheInfos);
                                    }
                                    sheBox.put(sheInfoList);

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

    /**
     * 判断预理赔是否录制视频接口
     */
    private void getJudgeRecordVideo() {
        OkHttp3Util.doPost(JUDGE_RECORD_VIDEO, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SelectFunctionActivity_new.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, WaitDisposeActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Logger.i(string);
                BaseBean<JudgeRecordVideo> result;
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<BaseBean<JudgeRecordVideo>>() {
                    }.getType();
                    result = gson.fromJson(string, type);
                    SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (null != result) {
                                if (result.getStatus() == 1) {
                                    JudgeRecordVideo judgeRecordVideo = result.getData();
                                    //0未录制  1已录制
                                    if (judgeRecordVideo.getIsRecordVideo() == 0) {

                                        AlertDialog.Builder dialog = new AlertDialog.Builder(SelectFunctionActivity_new.this);

                                        final View dialogView = LayoutInflater.from(SelectFunctionActivity_new.this).inflate(R.layout.dialog_common_one_layout, null);
                                        TextView dialog_content_tv1 = dialogView.findViewById(R.id.dialog_content_tv1);
                                        TextView dialog_ok_btn = dialogView.findViewById(R.id.dialog_ok_btn);
                                        TextView dialog_tips_tv = dialogView.findViewById(R.id.dialog_tips_tv);

                                        dialog_tips_tv.setText("提示");
                                        dialog_content_tv1.setText("预理赔录制未完成\n" + "请先拍摄圈舍360角度视频并上传");
                                        dialog_content_tv1.setGravity(Gravity.CENTER_HORIZONTAL);
                                        dialog.setView(dialogView);

                                        Dialog d = dialog.create();

                                        dialog_ok_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                d.dismiss();
                                                mActivity.startActivity(new Intent(mActivity, SmallVideoActivity.class)
                                                        .putExtra("lipeiid", judgeRecordVideo.getLipeiId()));
                                            }
                                        });
                                        d.setCancelable(false);
                                        d.show();
                                    } else {
                                        if (PIG_DEPTH_JOIN) {
                                            Intent intent = new Intent(SelectFunctionActivity_new.this, DetectorActivity_pig.class);
                                            SelectFunctionActivity_new.this.startActivity(intent);
                                            return;
                                        }
                                        checkBaoDan();
                                    }
                                } else {
                                    Toast.makeText(SelectFunctionActivity_new.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(SelectFunctionActivity_new.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SelectFunctionActivity_new.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * 获取投保数数量
     */
    private void getTouBao() {
        OkHttp3Util.doPost(BUBBLE_DATA, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SelectFunctionActivity_new.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, WaitDisposeActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Logger.i(string);
                BaseBean<BubbleDataBean> result;
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<BaseBean<BubbleDataBean>>() {
                    }.getType();
                    result = gson.fromJson(string, type);
                    SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (null != result) {
                                if (result.getStatus() == 1) {
                                    BubbleDataBean bubbleDataBean = result.getData();
                                    if (!PigAppConfig.PIG_DEPTH_JOIN) {
                                        if (bubbleDataBean.getToubaoCount() > 0) {
                                            rl_toubao_count.setVisibility(View.VISIBLE);
                                            tv_toubao_count.setText(bubbleDataBean.getToubaoCount() + "");
                                        } else {
                                            rl_toubao_count.setVisibility(View.GONE);
                                        }
                                    }

                                    if (bubbleDataBean.getLipeiCount() > 0) {
                                        rl_lipei_count.setVisibility(View.VISIBLE);
                                        tv_lipei_count.setText(bubbleDataBean.getLipeiCount() + "");
                                    } else {
                                        rl_lipei_count.setVisibility(View.GONE);
                                    }
                                } else {

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
     * 获取未测重列表
     */
    private void getPayList() {
        this.mLoadProgressDialog.show();
        OkHttp3Util.doPost(GET_PAY_LIST, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SelectFunctionActivity_new.this.mLoadProgressDialog.dismiss();
                        Toast.makeText(SelectFunctionActivity_new.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, WaitDisposeActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Logger.i(string);
                SelectFunctionActivity_new.this.mLoadProgressDialog.dismiss();
                BaseBean<List<PayInfo>> result;
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<BaseBean<List<PayInfo>>>() {
                    }.getType();
                    result = gson.fromJson(string, type);
                    SelectFunctionActivity_new.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (null != result) {
                                if (result.getStatus() == 1) {
                                    List<PayInfo> payInfoList = result.getData();
                                    if (payInfoList.size() > 0) {
                                        dialog = NoWeighingDialog.showNoWeighingDialog(SelectFunctionActivity_new.this, payInfoList);
                                    }
                                } else {

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


    @Override
    public void onBackPressed() {
        if ("2".equals(this.companyfleg)) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - this.firstTime > 2000L) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                this.firstTime = secondTime;
            } else {
                if (PigAppConfig.PIG_DEPTH_JOIN) {
                    finish();
                    return;
                }
                Process.killProcess(Process.myPid());
                System.exit(1);
            }
        } else {
            this.finish();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        PigAppConfig.UnRegistEvent(this);
    }

    @Override
    protected void onDestroy() {
        g_CaptivityMap.clear();
        g_LocationMap.clear();
        g_TotalMap.clear();
        g_SheID = null;
        g_PigType = null;
        gscPigBeans.clear();
        PigAppConfig.UnRegistEvent(this);
        super.onDestroy();
    }

    @Override
    public void receiveEvent(Object o) {
        if (offLineModle) {
            if (PigAppConfig.PIG_DEPTH_JOIN) {
                android.widget.Toast.makeText(mActivity, "请打开网络链接！", Toast.LENGTH_SHORT).show();
            } else {
//                goToActivity(SelectFunctionActivity_OffLine.class, null);
            }
            finish();
        }
    }
}
