package com.xiangchuang.risks.view;

import android.app.AlertDialog;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.innovation.pig.insurance.R;
import com.orhanobut.logger.Logger;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.PigHouseListAdapter;
import com.xiangchuang.risks.model.adapter.PigTypeListAdapter;
import com.xiangchuang.risks.model.bean.BaseBean;
import com.xiangchuang.risks.model.bean.PigTypeBean;
import com.xiangchuang.risks.model.bean.SheListBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.SystemUtil;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import innovation.utils.ScreenUtil;
import innovation.utils.Toast;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xiangchuang.risks.utils.MyTextUtil.isEmojiCharacter;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.PIG_DEPTH_JOIN;

public class PigHouseListActivity extends BaseActivity {

    private RecyclerView rvPigHouseList;

    private PigHouseListAdapter pigHouseListAdapter;
    private List<SheListBean.DataOffLineBaodanBean> mSheBeans;
    private List<PigTypeBean.DataBean> pigTypeList = new ArrayList<>();

    private PopupWindow popupWindow;
    private EditText etName;
    private TextView tvPigType;

    private PopupWindow popupWindowChild;
    private PigTypeListAdapter pigTypeListAdapter;

    private String pigTypeId;

    private int yufeiNo = 0;
    private int nengfanNo = 0;
    private int baoyuNo = 0;
    private int houbeiNo = 0;
    RelativeLayout add_layout;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pig_house_list;
    }

    @Override
    public void initView() {
        super.initView();
        rvPigHouseList = findViewById(R.id.rv_pig_house_list);

        View mEmptyView = View.inflate(this, R.layout.item_empty_view, null);
        rvPigHouseList.setLayoutManager(new LinearLayoutManager(this));
        pigHouseListAdapter = new PigHouseListAdapter(R.layout.item_pig_house);
        pigHouseListAdapter.setEmptyView(mEmptyView);
        rvPigHouseList.setAdapter(pigHouseListAdapter);

        popupWindow = new PopupWindow(PigHouseListActivity.this);
        View popView = getLayoutInflater().inflate(R.layout.pop_pig_house_add, null);
        etName = popView.findViewById(R.id.et_name);
        tvPigType = popView.findViewById(R.id.tv_pig_type);

        Button btnCommit = popView.findViewById(R.id.btn_commit);
        popView.findViewById(R.id.v_empty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        if (PIG_DEPTH_JOIN)
            popupWindow.setOutsideTouchable(false);
        popupWindow.setContentView(popView);

        pigTypeListAdapter = new PigTypeListAdapter(R.layout.item_type);

        pigTypeListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                popupWindowChild.dismiss();
                PigTypeBean.DataBean dataBean = (PigTypeBean.DataBean) adapter.getData().get(position);
                tvPigType.setText(dataBean.getPigTypeName());
                pigTypeId = dataBean.getPigType() + "";
                setEtName();
            }
        });

        findViewById(R.id.rl_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        add_layout = findViewById(R.id.rl_add_pig_house);
        add_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAtLocation(PigHouseListActivity.this.findViewById(R.id.pig_house_list_layout), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });

        tvPigType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initChildPopupWindow();
                popupWindowChild.showAtLocation(PigHouseListActivity.this.findViewById(R.id.pig_house_list_layout), Gravity.CENTER, 0, 0);
                setTextImage(R.drawable.ic_up);
                popupWindowChild.setOnDismissListener(dismissListener);
            }
        });

        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etName.getText().toString())) {
                    Toast.makeText(PigHouseListActivity.this, "请填写猪舍名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isEmo(etName.getText().toString())) {
                    Toast.makeText(PigHouseListActivity.this, "企业名称不能包含特殊字符", Toast.LENGTH_LONG).show();
                    return;
                }
                addPigHouse();
//                AlertDialogManager.showMessageDialog(PigHouseListActivity.this, "提示", getString(R.string.sure_to_add), new AlertDialogManager.DialogInterface() {
//                    @Override
//                    public void onPositive() {
//
//                    }
//
//                    @Override
//                    public void onNegative() {
//                    }
//                });
            }
        });
    }

    /**
     * 给TextView右边设置图片
     *
     * @param resId
     */
    private void setTextImage(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());// 必须设置图片大小，否则不显示
        tvPigType.setCompoundDrawables(null, null, drawable, null);
    }

    @Override
    protected void initData() {
        getDataFromNet();
    }

    private void getDataFromNet() {
        yufeiNo = 0;
        nengfanNo = 0;
        baoyuNo = 0;
        houbeiNo = 0;
        OkHttp3Util.doPost(Constants.PIG_HOUSE_LIST, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("ShowPollingActivity", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Logger.i(string);
                final SheListBean bean = GsonUtils.getBean(string, SheListBean.class);
                if (null != bean && null != bean.getData()) {
                    if (1 == bean.getStatus()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSheBeans = bean.getData();
                                setData(mSheBeans);
                                getPigType();
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * adapter设置数据
     *
     * @param data
     */
    private void setData(List data) {
        List<SheListBean.DataOffLineBaodanBean> sheInfoList = data;
        for (SheListBean.DataOffLineBaodanBean sheInfo : sheInfoList) {
            if ("101".equals(sheInfo.getPigType())) {
                yufeiNo++;
            } else if ("102".equals(sheInfo.getPigType())) {
                nengfanNo++;
            } else if ("103".equals(sheInfo.getPigType())) {
                baoyuNo++;

            } else if ("104".equals(sheInfo.getPigType())) {
                houbeiNo++;
            }
        }
        Logger.i("育肥：" + yufeiNo + "");
        Logger.i("能繁：" + nengfanNo + "");
        Logger.i("保育：" + baoyuNo + "");
        Logger.i("后备：" + houbeiNo + "");

        pigHouseListAdapter.setNewData(data);
        add_layout.performClick();
    }

    private void getPigType() {
        if (isFinishing()) {
            return;
        }
        mLoadProgressDialog.show();
        OkHttp3Util.doPost(Constants.PIG_TYPE_ALL, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AVOSCloudUtils.saveErrorMessage(e, InsuredActivity.class.getSimpleName());
                mLoadProgressDialog.dismiss();
                Logger.i(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String string = response.body().string();
                Logger.i(string);
                PigTypeBean bean = GsonUtils.getBean(string, PigTypeBean.class);
                if (bean != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoadProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                pigTypeList = bean.getData();
                                if (pigTypeList.size() > 0) {
                                    pigTypeId = pigTypeList.get(0).getPigType() + "";
                                    tvPigType.setText(pigTypeList.get(0).getPigTypeName());
                                    pigTypeListAdapter.setNewData(pigTypeList);
                                    setEtName();
                                } else {
                                    Toast.makeText(PigHouseListActivity.this, "获取猪种类失败", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(PigHouseListActivity.this, bean.getMsg(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoadProgressDialog.dismiss();
                            Toast.makeText(PigHouseListActivity.this, "获取猪种类失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void setEtName() {
        String strName = "";

        int yfNo = yufeiNo;
        int nfNo = nengfanNo;
        int byNo = baoyuNo;
        int hbNo = houbeiNo;

        if ("101".equals(pigTypeId)) {
            yfNo++;
            if (yfNo < 10) {
                strName = "育肥-0" + yfNo;
            } else {
                strName = "育肥-" + yfNo;
            }
        } else if ("102".equals(pigTypeId)) {
            nfNo++;
            if (nfNo < 10) {
                strName = "能繁-0" + nfNo;
            } else {
                strName = "能繁-" + nfNo;
            }
        } else if ("103".equals(pigTypeId)) {
            byNo++;
            if (byNo < 10) {
                strName = "保育-0" + byNo;
            } else {
                strName = "保育-" + byNo;
            }
        } else if ("104".equals(pigTypeId)) {
            hbNo++;
            if (hbNo < 10) {
                strName = "后备-0" + hbNo;
            } else {
                strName = "后备-" + hbNo;
            }
        }
        etName.setText(strName);
    }

    private void initChildPopupWindow() {
        popupWindowChild = new PopupWindow(PigHouseListActivity.this);
        View popViewChild = getLayoutInflater().inflate(R.layout.pop_pig_type_window_layout, null);
        RecyclerView rvList = popViewChild.findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(pigTypeListAdapter);
        // 设置背景图片，不能在布局中设置，要通过代码来设置
        popupWindowChild.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.pop_background));
        popupWindowChild.setWidth((int) (ScreenUtil.getScreenWidth() - 180 * SystemUtil.getDensity()));
//        popupWindowChild.setHeight((int) (ScreenUtil.getScreenHeight() - 435 * SystemUtil.getDensity()));
        popupWindowChild.setFocusable(true);
        popupWindowChild.setOutsideTouchable(true);
        popupWindowChild.setContentView(popViewChild);

    }

    /**
     * 监听popupwindow取消
     */
    private PopupWindow.OnDismissListener dismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            setTextImage(R.drawable.ic_down);
        }
    };


    AlertDialog mDialog;

    private void addPigHouse() {
        if (isFinishing()) {
            return;
        }
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put("sheName", etName.getText().toString().trim());
        mapbody.put("pigType", pigTypeId);
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ADD_PIG_HOUSE, mapbody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("CompanyActivity", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        Toast.makeText(PigHouseListActivity.this, "添加摄像头失败，请检查网络后重试。", Toast.LENGTH_SHORT).show();
                        if (PIG_DEPTH_JOIN) {
                            PigHouseListActivity.this.finish();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                String string = response.body().string();
                mProgressDialog.dismiss();
                Log.i("CompanyActivity", string);
                try {
                    BaseBean<Object> bean;
                    Gson gson = new Gson();
                    Type type = new TypeToken<BaseBean<Object>>() {
                    }.getType();
                    bean = gson.fromJson(string, type);
                    if (bean.getStatus() != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialogManager.showMessageDialogone(PigHouseListActivity.this, "提示", bean.getMsg(), true, "", new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {
                                        if (PIG_DEPTH_JOIN) {
                                            PigHouseListActivity.this.finish();
                                        }
                                    }

                                    @Override
                                    public void onNegative() {
                                        if (PIG_DEPTH_JOIN) {
                                            PigHouseListActivity.this.finish();
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                popupWindow.dismiss();

                                if (!PIG_DEPTH_JOIN)
                                    getDataFromNet();
                                //改完
                                AlertDialog.Builder builder = new AlertDialog.Builder(PigHouseListActivity.this);
                                builder.setView(R.layout.dialog_custom_view);
                                builder.setCancelable(false);
                                mDialog = builder.create();
                                mDialog.show();

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        mDialog.dismiss();
                                        if (PIG_DEPTH_JOIN)
                                            PigHouseListActivity.this.finish();
                                    }
                                }, 1000);

                            }
                        });
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });
    }

    //判断是否包含表情符号
    private boolean isEmo(String s) {
        boolean isemo = false;
        for (int i = 0; i < s.length(); i++) {
            isemo = isEmojiCharacter(s.charAt(i));
            if (isemo) {
                break;
            }
        }
        return isemo;
    }

}
