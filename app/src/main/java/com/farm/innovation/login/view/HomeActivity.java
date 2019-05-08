package com.farm.innovation.login.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.LiPeiLocalBean;
import com.farm.innovation.bean.MergeLoginBean;
import com.farm.innovation.bean.QueryVideoFlagDataBean;
import com.farm.innovation.bean.ResultBean;
import com.farm.innovation.biz.iterm.MediaPayItem;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.biz.login.LoginMergeActivity;
import com.farm.innovation.login.DatabaseHelper;
import com.farm.innovation.login.FixedSpeedScroller;
import com.farm.innovation.login.MyFragmentPagerAdapter;
import com.farm.innovation.login.Utils;
import com.farm.innovation.update.UpdateInformation;
import com.farm.innovation.update.UploadService;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.ConstUtils;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.FarmerShareUtils;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.view.CustomViewPager;
import com.google.gson.Gson;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.update.AppUpgradeService;
import com.xiangchuang.risks.update.UpdateInfoModel;
import com.xiangchuang.risks.view.CompanyActivity;
import com.xiangchuang.risks.view.SelectFunctionActivity_new;

import org.tensorflow.demo.FarmGlobal;
import org.tensorflow.demo.env.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.farm.innovation.base.FarmAppConfig.needUpDate;
import static com.farm.innovation.utils.ConstUtils.ANIMAL_TYPE_NONE;
import static com.farm.innovation.utils.ConstUtils.getInsureAnimalTypeName;
import static com.farm.innovation.utils.FarmerShareUtils.MERGE_LOGIN_INFO;

//add by xuly 2018-06-12
public class HomeActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private static String TAG = "HomeActivity";
    //    private SearchView searchView;
    private CustomViewPager myviewpager;
    //fragment的集合，对应每个子页面
    private ArrayList<Fragment> fragments;
    //选项卡中的按钮
    private RadioButton btn_first;
    private RadioButton btn_second;

//    private TextView tv_title;
//    private ImageView iv_cancel;

    //作为指示标签的按钮
    private ImageView cursor;
    //    标志指示标签的横坐标
    float cursorX = 0;
    //所有按钮的宽度的集合
    private int[] widthArgs;
    //所有按钮的集合
    private Button[] btnArgs;

    private Dialog dialog;
    private TextView versionName;

    private TextView tv_title;
    private ImageView iv_cancel;
    private static final Logger logger = new Logger();
    private Gson gson;
    private ResultBean queryVideoFlagResultBean;

//    private DatabaseHelper databaseHelper;

    private TextView tv_exit;
    private PopupWindow pop;
    private RelativeLayout rl_edit;
    public ImageView ivSign;
    private ImageView ivPopUpdateSign;
    private UpdateInfoModel mUpdateInfoModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.farm_activity_taskselect);
        gson = new Gson();
        queryVideoFlag();

        tv_title = (TextView) findViewById(R.id.tv_title);
        rl_edit = (RelativeLayout) findViewById(R.id.rl_edit);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        ivSign = (ImageView) findViewById(R.id.iv_sign);
        rl_edit.setVisibility(View.VISIBLE);

        moveFlie();

        pop = new PopupWindow(this);
        View popview = getLayoutInflater().inflate(R.layout.farm_item_setting, null);
        TextView select_type = popview.findViewById(R.id.select_type);
        TextView login_exit = popview.findViewById(R.id.login_exit);
        TextView tvPopUpdate = popview.findViewById(R.id.tv_pop_update);
        TextView enter_pig = popview.findViewById(R.id.enter_pig);
        if (!FarmAppConfig.isOriginApk() && !AppConfig.isOriginApk()) {
            login_exit.setVisibility(View.GONE);
            tvPopUpdate.setVisibility(View.GONE);
            popview.findViewById(R.id.rl_pop_updata).setVisibility(View.GONE);
        }
        ivPopUpdateSign = popview.findViewById(R.id.iv_pop_update_sign);

        setSign();

        MergeLoginBean bean = FarmerShareUtils.getData(MERGE_LOGIN_INFO);
        if (bean != null) {
            if (bean.data.ftnData != null) {
                enter_pig.setVisibility(View.VISIBLE);
                enter_pig.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bean.enterByStatus = 2;
                        FarmerShareUtils.saveData(MERGE_LOGIN_INFO, bean);
                        if (bean.data.ftnData.type == 1) {
                            goToActivity(CompanyActivity.class, null);
                            finish();
                        } else if (bean.data.ftnData.type == 2) {
                            goToActivity(SelectFunctionActivity_new.class, null);
                            finish();
                        }
                    }
                });
            } else {
                enter_pig.setVisibility(View.GONE);
            }
        } else {
            enter_pig.setVisibility(View.GONE);
        }

        pop.setWidth(300);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(popview);

        tv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.showAsDropDown(rl_edit);
                select_type.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pop.dismiss();
                        showTypeDialog();
                    }
                });

                tvPopUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pop.dismiss();
                        if (needUpDate) {
                            if (ivSign.getVisibility() == View.VISIBLE) {
                                ivSign.setVisibility(View.GONE);
                            }

                            AlertDialog.Builder mDialog = new AlertDialog.Builder(HomeActivity.this);
                            mDialog.setIcon(R.drawable.cowface);//farm_cowface
                            mDialog.setTitle("版本更新");
                            mDialog.setMessage(UpdateInformation.upgradeinfo);
                            mDialog.setCancelable(false);
                            mDialog.setPositiveButton("马上升级", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ivPopUpdateSign.setVisibility(View.GONE);
//                                    Intent mIntent = new Intent(HomeActivity.this, FarmerAppUpgradeService.class);
//                                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    //传递数据
//                                    //mIntent.putExtra("appname", UpdateInformation.appname);
//                                    mIntent.putExtra("mDownloadUrl", UpdateInformation.updateurl);
//                                    mIntent.putExtra("appname", UpdateInformation.appname);
//                                    HomeActivity.this.startService(mIntent);
                                    Intent mIntent = new Intent(HomeActivity.this, AppUpgradeService.class);
                                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    //传递数据
                                    mIntent.putExtra("data", mUpdateInfoModel);
                                    HomeActivity.this.startService(mIntent);
                                }
                            }).setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                        } else {
                            AlertDialog.Builder mDialog = new AlertDialog.Builder(HomeActivity.this);
                            mDialog.setIcon(R.drawable.cowface);//farm_cowface
                            mDialog.setTitle("提示");
                            mDialog.setMessage("当前已是最新版本");
                            mDialog.setCancelable(false);
                            mDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                        }
                    }
                });

                login_exit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pop.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this)
                                .setIcon(R.drawable.farm_cowface).setTitle("提示")
                                .setMessage("退出登录")
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences pref = getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.clear();
                                        editor.commit();
                                        FarmerShareUtils.clearMergeLoginInfo();
//                                        Intent add_intent = new Intent(HomeActivity.this, LoginFamerActivity.class);
                                        Intent add_intent = new Intent(HomeActivity.this, LoginMergeActivity.class);
                                        startActivity(add_intent);
                                        HomeActivity.this.finish();
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
            }
        });

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        startService(new Intent(this, UploadService.class));
    }

    public void setSign() {
//        if (needUpDate) {
//            ivPopUpdateSign.setVisibility(View.VISIBLE);
//            ivSign.setVisibility(View.VISIBLE);
//        } else {
//            ivPopUpdateSign.setVisibility(View.GONE);
//            ivSign.setVisibility(View.GONE);
//        }
        if (ivPopUpdateSign == null || ivSign == null) return;
        if (AppConfig.getUpdateInfoModel() != null)
            needUpDate = AppConfig.getUpdateInfoModel().isUpdate();
        if (needUpDate) {
            if (FarmAppConfig.isOriginApk() || AppConfig.isOriginApk()) {
                ivPopUpdateSign.setVisibility(View.VISIBLE);
                ivSign.setVisibility(View.VISIBLE);
            }
        } else {
            ivPopUpdateSign.setVisibility(View.GONE);
            ivSign.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEventMain(UpdateInfoModel bean) {
        super.onEventMain(bean);
        if (bean == null) return;
        needUpDate = bean.isUpdate();
        mUpdateInfoModel = bean;
        setSign();
    }

    /**
     * 选择险种dialog
     */
    private void showTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View v = inflater.inflate(R.layout.farm_animal_type_dialog_layout, null);
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(v);
        RadioGroup animalTypeRadioGroup = v.findViewById(R.id.animalTypeRadioGroup);
        Button okButton = v.findViewById(R.id.okButton);
        okButton.setOnClickListener(okButtonClickListener);
        animalTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.cowRadioButton) {
                FarmerPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_CATTLE, HomeActivity.this);

            } else if (checkedId == R.id.donkeyRadioButton) {
                FarmerPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_DONKEY, HomeActivity.this);

            } else if (checkedId == R.id.pigRadioButton) {
                FarmerPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_PIG, HomeActivity.this);

            } else if (checkedId == R.id.yakRadioButton) {
                FarmerPreferencesUtils.setAnimalType(ConstUtils.ANIMAL_TYPE_YAK, HomeActivity.this);

            } else {
            }
        });
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
    }


    private void queryVideoFlag() {
        try {
            TreeMap<String, String> treeMapQueryVideoFlag = new TreeMap();
            treeMapQueryVideoFlag.put("", "");
            FormBody.Builder builder = new FormBody.Builder();
            for (TreeMap.Entry<String, String> entry : treeMapQueryVideoFlag.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
            RequestBody formBody = builder.build();
            String queryVideoFlagResponse = HttpUtils.post(HttpUtils.QUERY_VIDEOFLAG_NEW, formBody);
            Log.e("queryVideoFlag", "queryVideoFlag: START");
            if (queryVideoFlagResponse != null) {
                logger.e(HttpUtils.QUERY_VIDEOFLAG_NEW + "\nqueryVideoFlagResponse:\n" + queryVideoFlagResponse);
                queryVideoFlagResultBean = gson.fromJson(queryVideoFlagResponse, ResultBean.class);
                if (queryVideoFlagResultBean.getStatus() == 1) {
                    QueryVideoFlagDataBean queryVideoFlagData = gson.fromJson(queryVideoFlagResponse, QueryVideoFlagDataBean.class);
                    FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.touBaoVieoFlag, queryVideoFlagData.getData().getToubaoVideoFlag(), HomeActivity.this);
                    //  FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.touBaoVieoFlag, 1 + "", HomeActivity.this);
                    FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.liPeiVieoFlag, queryVideoFlagData.getData().getLipeiVideoFlag(), HomeActivity.this);

                    QueryVideoFlagDataBean.thresholdList thresholdList = gson.fromJson(queryVideoFlagData.getData().getThreshold(), QueryVideoFlagDataBean.thresholdList.class);

                    Log.e(TAG, "queryVideoFlag thresholdList: " + thresholdList.toString());

                    //存储理赔的时间条件信息
                    FarmerPreferencesUtils.saveIntValue(FarmAppConfig.lipeia, Integer.parseInt(thresholdList.getLipeiA()), HomeActivity.this);
                    FarmerPreferencesUtils.saveIntValue(FarmAppConfig.lipeib, Integer.parseInt(thresholdList.getLipeiB()), HomeActivity.this);
                    FarmerPreferencesUtils.saveIntValue(FarmAppConfig.lipein, Integer.parseInt(thresholdList.getLipeiN()), HomeActivity.this);
                    FarmerPreferencesUtils.saveIntValue(FarmAppConfig.lipeim, Integer.parseInt(thresholdList.getLipeiM()), HomeActivity.this);

                    FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.phone, queryVideoFlagData.getData().getServiceTelephone(), HomeActivity.this);
                    FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.customServ, thresholdList.getCustomServ(), HomeActivity.this);

                    FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.THRESHOLD_LIST, queryVideoFlagData.getData().getThreshold(), HomeActivity.this);
                    if (null != queryVideoFlagData.getData() && !"".equals(queryVideoFlagData.getData())) {
                        String left = (queryVideoFlagData.getData().getLeftNum() == null) ? "8" : queryVideoFlagData.getData().getLeftNum();
                        String middleNum = (queryVideoFlagData.getData().getLeftNum() == null) ? "8" : queryVideoFlagData.getData().getMiddleNum();
                        String rightNum = (queryVideoFlagData.getData().getLeftNum() == null) ? "8" : queryVideoFlagData.getData().getRightNum();
                        logger.e("\nleft:\n" + left);
                        logger.e("\nmiddleNum:\n" + middleNum);
                        logger.e("\nrightNum:\n" + rightNum);
                        FarmerPreferencesUtils.saveKeyValue(FarmerPreferencesUtils.FACE_ANGLE_MAX_LEFT, left, HomeActivity.this);
                        FarmerPreferencesUtils.saveKeyValue(FarmerPreferencesUtils.FACE_ANGLE_MAX_MIDDLE, middleNum, HomeActivity.this);
                        FarmerPreferencesUtils.saveKeyValue(FarmerPreferencesUtils.FACE_ANGLE_MAX_RIGHT, rightNum, HomeActivity.this);

                    }
                } else if (queryVideoFlagResultBean.getStatus() == 0) {
                    homeActivityHandler.sendEmptyMessage(14);
                } else {
                    homeActivityHandler.sendEmptyMessage(15);
                }

            } else {
                // homeActivityHandler.sendEmptyMessage(16);
            }
        } catch (Exception e) {
            // Toast.makeText(HomeActivity.this, "查看是否录制视频接口异常！", Toast.LENGTH_SHORT).show();
            AVOSCloudUtils.saveErrorMessage(e, HomeActivity.class.getSimpleName());
        }
    }

    private View.OnClickListener okButtonClickListener = new View.OnClickListener() {
        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View v) {
            if (FarmerPreferencesUtils.getAnimalType(HomeActivity.this) == ANIMAL_TYPE_NONE) {
                Toast.makeText(HomeActivity.this, "必须选择其中一个农险！！", Toast.LENGTH_SHORT).show();
                return;
            }
            initViews();
            while (dialog != null && dialog.isShowing())
//                dialog.getCurrentFocus().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (dialog != null)
                            dialog.dismiss();
//                        dialog = null;
//                    }
//                });
        }
    };

    public void initViews() {
//        tv_title = (TextView) findViewById(R.id.tv_title);
//        rl_edit = (RelativeLayout) findViewById(R.id.rl_edit);
//        tv_exit = (TextView) findViewById(R.id.tv_exit);
//        ivSign = (ImageView) findViewById(R.id.iv_sign);

        myviewpager = (CustomViewPager) this.findViewById(R.id.myviewpager);
        //禁止滑动
        myviewpager.setScanScroll(true);
        btn_first = (RadioButton) this.findViewById(R.id.btn_first);
        btn_second = (RadioButton) this.findViewById(R.id.btn_second);
        versionName = (TextView) this.findViewById(R.id.tv_version_name);

        btnArgs = new Button[]{btn_first, btn_second};

        cursor = (ImageView) this.findViewById(R.id.cursor_btn);

        cursor.setBackgroundColor(Color.YELLOW);

        myviewpager.setOnPageChangeListener(this);
        versionName.setText("v" + getVersionName());

        btn_first.setOnCheckedChangeListener(new InnerOnCheckedChangeListener());
        btn_second.setOnCheckedChangeListener(new InnerOnCheckedChangeListener());

        Field mScroller = null;
        try {
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(myviewpager.getContext());
            mScroller.set(myviewpager, scroller);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {

        } catch (IllegalAccessException e) {

        }

        fragments = new ArrayList<Fragment>();
        fragments.add(new ToubaoFragment());
        fragments.add(new LipeiFragment());
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        myviewpager.setAdapter(adapter);


        int animalType = FarmerPreferencesUtils.getAnimalType(HomeActivity.this);
        if (btn_first.isChecked()) {
            myviewpager.setCurrentItem(0);
            btn_first.setBackgroundResource(R.drawable.farm_toubao02);
            btn_second.setBackgroundResource(R.drawable.farm_lipei01);
//            cursorAnim(0);
            FarmGlobal.model = Model.BUILD.value();
            tv_title.setText(getInsureAnimalTypeName(animalType) + "投保");
        }

        if (btn_second.isChecked()) {
            myviewpager.setCurrentItem(1);
            btn_first.setBackgroundResource(R.drawable.farm_toubao01);
            btn_second.setBackgroundResource(R.drawable.farm_lipei02);
//            cursorAnim(1);
            FarmGlobal.model = Model.VERIFY.value();
            tv_title.setText(getInsureAnimalTypeName(animalType) + "理赔");
        }

        //重置所有按钮颜色
        //  resetButtonColor();
        //把第一个按钮的颜色设置为红色
        btn_first.setTextColor(Color.YELLOW);
        btn_first.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cursor.getLayoutParams();
                //减去边距*2，以对齐标题栏文字
                lp.width = btn_first.getWidth() - btn_first.getPaddingLeft() * 2;
                cursor.setLayoutParams(lp);
                cursor.setX(btn_first.getPaddingLeft());
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_taskselect;
    }

    /**
     * get App versionName
     *
     * @return versionName
     */
    private String getVersionName() {
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    //把事件的内部类定义出来
    private class InnerOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        //单选按钮选中事件方法
        //buttonView表示谁的状态被改变
        //isChecked上面的参数代表的状态是否选中
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int animalType = FarmerPreferencesUtils.getAnimalType(HomeActivity.this);
            int i = buttonView.getId();
            if (i == R.id.btn_first) {//单选按钮通过参数isChecked去得到当前到底是选中还是未选中
                if (isChecked) {
                    myviewpager.setCurrentItem(0);
                    btn_first.setBackgroundResource(R.drawable.farm_toubao02);
                    btn_second.setBackgroundResource(R.drawable.farm_lipei01);
                    cursorAnim(0);
                    FarmGlobal.model = Model.BUILD.value();
                    tv_title.setText(getInsureAnimalTypeName(animalType) + "投保");
                }

            } else if (i == R.id.btn_second) {//单选按钮通过参数isChecked去得到当前到底是选中还是未选中
                if (isChecked) {
                    myviewpager.setCurrentItem(1);
                    btn_first.setBackgroundResource(R.drawable.farm_toubao01);
                    btn_second.setBackgroundResource(R.drawable.farm_lipei02);
                    cursorAnim(1);
                    FarmGlobal.model = Model.VERIFY.value();
                    tv_title.setText(getInsureAnimalTypeName(animalType) + "理赔");
                }

            } else {
            }

        }


    }

    //重置所有按钮的颜色
    public void resetButtonColor() {
        /*btn_first.setBackgroundColor(Color.parseColor("#ff0099cc"));
        btn_second.setBackgroundColor(Color.parseColor("#ff0099cc"));*/

        btn_first.setTextColor(Color.WHITE);
        btn_second.setTextColor(Color.WHITE);

    }


    @Override
    public void onPageScrollStateChanged(int arg0) {
        //  Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        //  Auto-generated method stub

    }

    @Override
    public void onPageSelected(int arg0) {
        //  Auto-generated method stub
        if (widthArgs == null) {
            widthArgs = new int[]{btn_first.getWidth(),
                    btn_second.getWidth()};
        }
        //每次滑动首先重置所有按钮的颜色
        resetButtonColor();

        //将滑动到的当前按钮颜色设置为红色
        btnArgs[arg0].setTextColor(Color.YELLOW);
        cursorAnim(arg0);

        //把当前页面的单选按钮设置为选中状态
        ((CompoundButton) btnArgs[arg0]).setChecked(true);


    }

    //指示器的跳转，传入当前所处的页面的下标
    public void cursorAnim(int curItem) {
        //每次调用，就将指示器的横坐标设置0，即开始的位置
        cursorX = 0;
        //再根据当前的curItem来设置指示器的宽度
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cursor.getLayoutParams();
        //首先获得当前按钮的宽度，再减去按钮左右边距距，以对齐标题栏文本
        lp.width = widthArgs[curItem] - btnArgs[0].getPaddingLeft() * 2;
        //通过指示标签对象，将标签设置到父容器中
        cursor.setLayoutParams(lp);
        //循环获取当前页之前的所有页面的宽度
        for (int i = 0; i < curItem; i++) {
            cursorX = cursorX + btnArgs[i].getWidth();
        }
        //再加上当前页面的左边距，即为指示器当前应处的位置
        cursor.setX(cursorX + btnArgs[curItem].getPaddingLeft());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FarmerPreferencesUtils.getAnimalType(HomeActivity.this) == ANIMAL_TYPE_NONE) {
            showTypeDialog();
        } else {
            initViews();
        }

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, UploadService.class));
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

    @SuppressLint("HandlerLeak")
    private final Handler homeActivityHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 14:
                    AlertDialog.Builder builder14 = new AlertDialog.Builder(HomeActivity.this)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage(queryVideoFlagResultBean.getMsg())
                            .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    HomeActivity.this.finish();
                                }
                            });
                    builder14.setCancelable(false);
                    builder14.show();
                    break;

                case 15:
                    AlertDialog.Builder builder15 = new AlertDialog.Builder(HomeActivity.this)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage(queryVideoFlagResultBean.getMsg())
                            .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    HomeActivity.this.finish();
                                }
                            });
                    builder15.setCancelable(false);
                    builder15.show();
                    break;
                case 16:
                    AlertDialog.Builder builder16 = new AlertDialog.Builder(HomeActivity.this)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("网络异常！")
                            .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    HomeActivity.this.finish();
                                }
                            });
                    builder16.setCancelable(false);
                    builder16.show();
                    break;

                case 400:
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("获取用户ID失败！")
                            .setPositiveButton("确认", (dialog, which) -> HomeActivity.this.finish());
                    builder.setCancelable(false);
                    builder.show();
                    break;
                case 94:
                    break;
                default:
                    break;
            }

        }
    };

    private List<LiPeiLocalBean> liPeiLocalBeans = new ArrayList<>();

    private void moveFlie() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(HomeActivity.this);
        liPeiLocalBeans.clear();
        if (null != databaseHelper.queryLocalDataFromLiPei(FarmerPreferencesUtils.getStringValue(HttpUtils.user_id, HomeActivity.this))) {
            liPeiLocalBeans.addAll(databaseHelper.queryLocalDataFromLiPei(FarmerPreferencesUtils.getStringValue(HttpUtils.user_id, HomeActivity.this)));
        }
        if (FarmGlobal.mediaPayItem == null) {
            FarmGlobal.mediaPayItem = new MediaPayItem(HomeActivity.this);
        }
        FarmGlobal.mediaPayItem.currentInit();
        for (LiPeiLocalBean bean : liPeiLocalBeans) {
            if (bean.pzippath.contains("cache")) {
                String newPath = renameImgPath(bean.pzippath);
                Log.e(TAG, "newPath: " + newPath);
                FileUtils.moveFile(bean.pzippath, newPath);//移动文件
                databaseHelper.updateLiPeiLocalFromzipPath(newPath, bean.pinsureDate);
            }
            if (bean.pVideozippath.contains("cache")) {
                String newVPath = renameVideoPath(bean.pVideozippath);
                Log.e(TAG, "newPath: " + newVPath);
                FileUtils.moveFile(bean.pVideozippath, newVPath);//移动文件
                databaseHelper.updateLiPeiLocalFromVideozipPath(newVPath, bean.pinsureDate);
            }
        }
    }

    private String renameImgPath(String oldPath) {
        // 取得自动生成的zip文件的文件名
        String[] pathArray = oldPath.split("/");
        String localImageFielName = pathArray[pathArray.length - 1];
        return Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.farm.innovation.nongxian/animal/理赔/ZipImage/" + localImageFielName;
    }

    private String renameVideoPath(String oldPath) {
        // 取得自动生成的zip文件的文件名
        String[] pathArray = oldPath.split("/");
        String localImageFielName = pathArray[pathArray.length - 1];
        return Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.farm.innovation.nongxian/animal/理赔/ZipVideo/" + localImageFielName;
    }

    private long firstTime = 0;

    @Override
    public void onBackPressed() {
        if (FarmAppConfig.isOriginApk() || AppConfig.isOriginApk()) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
//            SharedPreferences pref = getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = pref.edit();
//            editor.clear();
//            editor.commit();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        } else {
            super.onBackPressed();
        }
    }
}


