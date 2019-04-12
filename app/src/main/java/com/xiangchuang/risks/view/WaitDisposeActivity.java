package com.xiangchuang.risks.view;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.WaitDisposeAdapter;
import com.xiangchuang.risks.model.bean.BaseBean;
import com.xiangchuang.risks.model.bean.UncompletedBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.view.ImageShowDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.innovation.pig.insurance.netutils.Constants.DISPOSE_IGNORE;
import static com.innovation.pig.insurance.netutils.Constants.DISPOSE_LIST;
import static com.innovation.pig.insurance.netutils.Constants.DISPOSE_START;


/**
 * 无害化处理待处理列表
 */
public class WaitDisposeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "WaitDisposeActivity";
    private RelativeLayout rlBack;
    private TextView tvTitleName;
    private RecyclerView rvPayList;
    private CheckBox cbAll;
    private TextView tvSelected;
    private TextView tvIgnore;
    private TextView tvStart;
    //具体处理步骤
    private UncompletedBean.currentStep currentStep;

    private List<UncompletedBean.lipeiInfo> list = new ArrayList<>();

    WaitDisposeAdapter waitDisposeAdapter;
    int checkNum = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_wait_dispose;
    }

    @Override
    public void initView() {
        super.initView();
        rlBack = findViewById(R.id.rl_back);
        tvTitleName = findViewById(R.id.tv_title_name);
        rvPayList = findViewById(R.id.rv_paylist);
        cbAll = findViewById(R.id.cb_all);
        tvSelected = findViewById(R.id.tv_selected);
        tvIgnore = findViewById(R.id.tv_ignore);
        tvStart = findViewById(R.id.tv_start);

        rlBack.setOnClickListener(this);
        cbAll.setOnClickListener(this);
        tvIgnore.setOnClickListener(this);
        tvStart.setOnClickListener(this);

    }

    @Override
    protected void initData() {
        getDisposeList();

        rvPayList.setLayoutManager(new LinearLayoutManager(WaitDisposeActivity.this));

        waitDisposeAdapter = new WaitDisposeAdapter();
        rvPayList.setAdapter(waitDisposeAdapter);
        waitDisposeAdapter.setOnItemCheckListener(new WaitDisposeAdapter.OnItemCheckListener() {
            @Override
            public void callBack(String id, boolean checked) {
                itemChecked(id, checked);
            }
        });

        waitDisposeAdapter.setOnItemLongClickListener(new WaitDisposeAdapter.OnItemLongClickListener() {
            @Override
            public void callBack(UncompletedBean.payInfo payInfo) {
                showImage(payInfo);
            }
        });

//        waitDisposeItemAdapter.setOnItemChildLongClickListener(new BaseQuickAdapter.OnItemChildLongClickListener() {
//            @Override
//            public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
//                showImage(adapter, position);
//
//                return false;
//            }
//        });
    }

    /**
     * 获取待处理理赔数量
     */
    private void getDisposeList() {
        this.mProgressDialog.show();
        OkHttp3Util.doPost(DISPOSE_LIST, (Map) null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                WaitDisposeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WaitDisposeActivity.this.mProgressDialog.dismiss();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, WaitDisposeActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                BaseBean<List<UncompletedBean.lipeiInfo>> result;
                
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<BaseBean<List<UncompletedBean.lipeiInfo>>>() {
                    }.getType();
                    result = gson.fromJson(string, type);
                    WaitDisposeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WaitDisposeActivity.this.mProgressDialog.dismiss();
                            if (null != result) {
                                if(result.getStatus() == 1){
                                    list = result.getData();
                                    if(list!=null&& list.size()>0){
                                        waitDisposeAdapter.setDate(sortData(list));
                                    }else{
                                        AlertDialogManager.showMessageDialogOne(WaitDisposeActivity.this,
                                                "提示", "当前还没有待处理的理赔数据",
                                                new com.xiangchuang.risks.utils.AlertDialogManager.DialogInterface() {
                                                    @Override
                                                    public void onPositive() {
                                                        finish();
                                                    }
                                                    @Override
                                                    public void onNegative() {

                                                    }
                                                });
                                    }
                                }else{
                                    showMessageDialogRetry(result.getMsg());
                                }
                            } else {
                                showMessageDialogRetry("获取待处理信息列表失败");
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    // 预理赔/理赔弹框
    private void showMessageDialogRetry(String msg) {
        AlertDialogManager.showMessageDialogRetry(WaitDisposeActivity.this, "提示", msg, new AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {
                getDisposeList();
            }

            @Override
            public void onNegative() {
                finish();
            }
        });
    }
    /**
     * 重新整理数据
     *
     * @param arrays
     */
    private List<Object> sortData(List<UncompletedBean.lipeiInfo> arrays) {
        List<Object> arrays_obj = new ArrayList<>();
        for (UncompletedBean.lipeiInfo array : arrays) {
            List<UncompletedBean.payInfo> logs = array.getPayInfoList();
            arrays_obj.add(array.getDate());
            if (logs != null && logs.size() > 0) {
                for (UncompletedBean.payInfo log : logs) {
                    arrays_obj.add(log);
                }
            }
        }
        return arrays_obj;
    }

    /**
     * 预览左中右照片
     */
    private void showImage(UncompletedBean.payInfo payInfo) {
        List<String> imgList = new ArrayList<>();

        imgList.add(payInfo.getImgLeft());
        imgList.add(payInfo.getImgMiddle());
        imgList.add(payInfo.getImgRight());

        ImageShowDialog imageShowDialog = new ImageShowDialog(WaitDisposeActivity.this);

        View.OnClickListener listenerReCollect = v -> {
            imageShowDialog.dismiss();
        };
        imageShowDialog.setBtnReCollectListener(listenerReCollect);
        imageShowDialog.setContentmessage(imgList);
        imageShowDialog.show();
    }


    /**
     * 单挑选中
     *
     * @param lpId
     */
    private void itemChecked(String lpId, boolean checked) {
        boolean b = true;
        for (UncompletedBean.lipeiInfo bean : list) {
            for (UncompletedBean.payInfo subbean : bean.getPayInfoList()) {
                if (lpId.equals(subbean.getLipeiNo())) {
                    subbean.setSelected(checked);
                }

                if (!subbean.isSelected()) {
                    b = false;
                }
            }
        }
        cbAll.setChecked(b);
        setTotal();
    }

    /**
     * 设置总数
     */
    private void setTotal() {
        int d = 0;
        for (UncompletedBean.lipeiInfo bean : list) {
            for (UncompletedBean.payInfo subbean : bean.getPayInfoList()) {
                if (subbean.isSelected()) {
                    d += 1;
                }
            }
        }
        tvSelected.setText(d + "");
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.rl_back) {
            finish();
        } else if (i == R.id.cb_all) {
            allChecked();
        } else if (i == R.id.tv_ignore) {
            String checkStr = getCheckedList();
            if (checkStr != null) {
                AlertDialogManager.showMessageDialog(WaitDisposeActivity.this,
                        "提示", "忽略后的牲畜将无法再进行无害化处理流程，请确认",
                        new com.xiangchuang.risks.utils.AlertDialogManager.DialogInterface() {
                            @Override
                            public void onPositive() {
                                ignorePayList(checkStr);
                            }

                            @Override
                            public void onNegative() {

                            }
                        });
            }

        } else if (i == R.id.tv_start) {
            String checkStr = getCheckedList();
            if (checkStr != null) {
                AlertDialogManager.showMessageDialog(WaitDisposeActivity.this,
                        "提示", "您选择了" + checkNum + "头牲畜进行无害化处理，请确认",
                        new com.xiangchuang.risks.utils.AlertDialogManager.DialogInterface() {
                            @Override
                            public void onPositive() {
                                startInnocuousId(checkStr);
                            }

                            @Override
                            public void onNegative() {

                            }
                        });

            }

        }
    }

    /**
     * 选中所有
     */
    private void allChecked() {
        for (UncompletedBean.lipeiInfo bean : list) {
            for (UncompletedBean.payInfo subbean : bean.getPayInfoList()) {
                subbean.setSelected(cbAll.isChecked());
            }
        }
        waitDisposeAdapter.setDate(sortData(list));
        waitDisposeAdapter.notifyDataSetChanged();
        setTotal();
    }

    /**
     * 添加忽略
     */
    private void ignorePayList(String ig) {
        this.mProgressDialog.show();
        Map mapbody = new HashMap();
        mapbody.put("lipeiNos", ig);
        OkHttp3Util.doPost(DISPOSE_IGNORE, mapbody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                WaitDisposeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WaitDisposeActivity.this.mProgressDialog.dismiss();
                        Toast.makeText(WaitDisposeActivity.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e, WaitDisposeActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(WaitDisposeActivity.TAG, string);

                BaseBean<Object> result;
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<BaseBean<Object>>() {
                    }.getType();
                    result = gson.fromJson(string, type);
                    WaitDisposeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WaitDisposeActivity.this.mProgressDialog.dismiss();
                            if (null != result) {
                                if (result.getStatus() == 1) {
                                    Toast.makeText(WaitDisposeActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                                    cbAll.setChecked(false);
                                    tvSelected.setText("0");
                                    getDisposeList();
                                } else {
                                    Toast.makeText(WaitDisposeActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            } else {

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
     * 生成无害化处理信息
     */
    private void startInnocuousId(String str) {
        this.mProgressDialog.show();
        Map mapbody = new HashMap();
        mapbody.put("lipeiNos", str);
        OkHttp3Util.doPost(DISPOSE_START, mapbody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                WaitDisposeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WaitDisposeActivity.this.mProgressDialog.dismiss();
                        Toast.makeText(WaitDisposeActivity.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
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
                    WaitDisposeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WaitDisposeActivity.this.mProgressDialog.dismiss();
                            if (null != result) {
                                if(result.getStatus() == 1){
                                    cbAll.setChecked(false);
                                    tvSelected.setText("0");
                                    UncompletedBean uncompletedBean = result.getData();
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("Uncompleted", uncompletedBean);
                                    bundle.putBoolean("isCreateOrder", true);
                                    goToActivity(DeadPigProcessStepActivity.class, bundle);
                                }else{
                                    Toast.makeText(WaitDisposeActivity.this, "开始处理失败，请重试", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(WaitDisposeActivity.this, "开始处理失败，请重试", Toast.LENGTH_SHORT).show();
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
     * 获取选中的信息列表
     *
     * @return payid 字符串 逗号隔开
     */
    private String getCheckedList() {
        checkNum = 0;
        StringBuilder sb = new StringBuilder();
        for (UncompletedBean.lipeiInfo bean : list) {
            for (UncompletedBean.payInfo subbean : bean.getPayInfoList()) {
                if (subbean.isSelected()) {
                    sb.append(subbean.getLipeiNo());
                    sb.append(",");
                    checkNum++;
                }
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            Toast.makeText(WaitDisposeActivity.this, "当前未选中任何理赔信息", Toast.LENGTH_SHORT).show();
            return null;
        }
        Log.e(TAG, "CheckedList: " + sb.toString());
        return sb.toString();
    }
}
