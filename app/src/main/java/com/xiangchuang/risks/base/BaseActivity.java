package com.xiangchuang.risks.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.update.AppUpgradeService;
import com.xiangchuang.risks.update.UpdateInfoModel;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.ToastUtils;
import com.xiangchuang.risks.utils.statusBarUtils.StatusBarUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by lxr on 2018/4/27.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected ViewGroup title_bar = null;
    public ProgressDialog mProgressDialog, mLoadProgressDialog;
    private boolean isPause = false;
    public ToastUtils toastUtils;
    protected Activity mActivity;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;

        //这里注意下 调用setRootViewFitsSystemWindows 里面 winContent.getChildCount()=0 导致代码无法继续
        //是因为需要在setContentView之后才可以调用 setRootViewFitsSystemWindows
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        /*if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this,0x55000000);
        }*/
        setTheme(R.style.AppFullScreenTheme);
        toastUtils = new ToastUtils();
        AppConfig.verifyStoragePermissions(this);
        showProgressDialog(this);
        showLoadProgressDialog();
        if (getLayoutId() > 0) {
            this.setContentView(this.getLayoutId());//缺少这一行
            // setContentView(getLayoutId());
            ButterKnife.bind(this);
            initView();
            initData();
        }
        EventBus.getDefault().register(this);
    }

    public void initView() {

    }

    /**
     * 布局ID
     *
     * @return
     */
    protected abstract int getLayoutId();

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
        Intent mIntent = new Intent(mActivity, AppUpgradeService.class);
        mActivity.stopService(mIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPause) {
            initData();
        }
//        if (null != AppConfig.getUpdateInfoModel())
//            EventBus.getDefault().post(AppConfig.getUpdateInfoModel());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMain(UpdateInfoModel bean) {
        if (bean == null) return;
    }

    /**
     * 初始化数据
     */

    protected abstract void initData();


    /* *跳转*/

    public void goToActivity(Class activity, Bundle bundle) {
        Intent intent = new Intent(this, activity);
        //携带数据
        if (bundle != null && bundle.size() != 0) {
            intent.putExtra("data", bundle);
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        runOnUiThread(() -> {
            if (null != mLoadProgressDialog) {
                mLoadProgressDialog.dismiss();
            }
            if (null != mProgressDialog) {
                mProgressDialog.dismiss();
            }
        });
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }

    public void showProgressDialog(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("开始处理......");
        Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
        if (positive != null) {
            positive.setVisibility(View.GONE);
        }
        Button negative = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
        if (negative != null) {
            negative.setVisibility(View.GONE);
        }
    }

    public void showLoadProgressDialog() {
        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }
        mLoadProgressDialog = new ProgressDialog(mActivity);
        mLoadProgressDialog.setTitle(R.string.dialog_title);
        mLoadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadProgressDialog.setCancelable(false);
        mLoadProgressDialog.setCanceledOnTouchOutside(false);
        mLoadProgressDialog.setMessage("加载中......");
        Button positive = mLoadProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
        if (positive != null) {
            positive.setVisibility(View.GONE);
        }
        Button negative = mLoadProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
        if (negative != null) {
            negative.setVisibility(View.GONE);
        }
    }

    protected void dismissLoadDialog() {
        if (mActivity != null && mLoadProgressDialog != null && mLoadProgressDialog.isShowing()) {
            mLoadProgressDialog.dismiss();
        }
    }

    public void showDialogError(String s) {
        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }
        AlertDialogManager.showMessageDialogOne(this, "提示", s, new AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {

            }

            @Override
            public void onNegative() {

            }
        });
    }

    public void showDialogNone() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View inflate = View.inflate(AppConfig.getAppContext(), R.layout.she_none_layout, null);
        TextView none_sure = inflate.findViewById(R.id.none_sure);
        dialog.setView(inflate);
        AlertDialog dialogcreate = dialog.create();
        dialogcreate.setCanceledOnTouchOutside(false);
        dialogcreate.show();
        none_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 请注意这段代码强烈建议不要放到实际开发中，因为用户屏蔽通知栏和开启应用状态下的情况极少，可以忽略不计

        // 如果通知栏的权限被手动关闭了
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled() &&
                !"SupportToast".equals(com.hjq.toast.ToastUtils.getToast().getClass().getSimpleName())) {
            try {
                // 因为吐司只有初始化的时候才会判断通知权限有没有开启，根据这个通知开关来显示原生的吐司还是兼容的吐司
                com.hjq.toast.ToastUtils.init(getApplication());
                recreate();
            } catch (Exception ignored) {
            }
        }
    }
}
