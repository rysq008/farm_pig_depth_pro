package com.xiangchuang.risks.base;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.xiangchuang.risks.utils.ToastUtils;
import com.xiangchuang.risks.view.AddCompanyActivity;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by lxr on 2018/4/27.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected ViewGroup title_bar = null;
    Unbinder unbinder;
    public ProgressDialog mProgressDialog;
    private boolean isPause = false;
    public ToastUtils toastUtils;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
            Window win = getWindow();
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //透明状态栏
                win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
                win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

                // 部分机型的statusbar会有半透明的黑色背景
                win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                // SDK21
                win.setStatusBarColor(Color.TRANSPARENT);
            }
        }*/
        toastUtils = new ToastUtils();
        this.setContentView(this.getLayoutId());//缺少这一行
       // setContentView(getLayoutId());
        initView();
        ButterKnife.bind(this);
        MyApplication.verifyStoragePermissions(this);
        showProgressDialog(this);
        initData();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPause) {
            initData();
        }
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
        if (this.unbinder != null) {
            this.unbinder.unbind();
        }
        toastUtils=null;
        super.onDestroy();
    }

    private void showProgressDialog(Activity activity) {
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

    public void showDialogError(String s) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View inflate = View.inflate(MyApplication.getAppContext(), R.layout.error_nomal, null);
        TextView error_msg = inflate.findViewById(R.id.error_msg);
        TextView error_sure = inflate.findViewById(R.id.error_sure);
        dialog.setView(inflate);
        error_msg.setText(s);
        AlertDialog dialogcreate = dialog.create();
        dialogcreate.setCanceledOnTouchOutside(false);
        dialogcreate.show();
        error_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
            }
        });
    }

    public void showDialogNone() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View inflate = View.inflate(MyApplication.getAppContext(), R.layout.she_none_layout, null);
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
}
