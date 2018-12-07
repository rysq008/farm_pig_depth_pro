package com.xiangchuang.risks.base;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangchuangtec.luolu.animalcounter.R;


public abstract class BaseBarActivity extends BaseActivity {
    protected LinearLayout llTitleBar, llTitleSearch, llBarMain, llSearch, llMainCenter, llMainCenter2;
    protected ImageView ivLeft, ivRight;
    protected TextView tvTitle, tvRight, tvLeftTxt, tvTitleParent, tvTitleChild;
    protected ImageView ivCenter;
    protected ImageView ivShop;
    public ImageView iv_cancel;

    @Override
    protected void initData() {
    }


    @Override
    public void initView() {
        super.initView();
        FrameLayout layout = (FrameLayout) findViewById(R.id.fl_main_content);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        View view = layoutView();
        layout.addView(view);
    }

    @Override
    protected int getLayoutId() {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
//设置修改状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//设置状态栏的颜色，和你的app主题或者标题栏颜色设置一致就ok了
          //  window.setStatusBarColor(getResources().getColor(R.color.holo_blue_dark));
            window.setStatusBarColor(Color.TRANSPARENT);
        }*/
        return R.layout.activity_base_bar;
    }

    /**
     * 内容资源文件
     */
    protected abstract View layoutView();

    protected void leftBack() {

    }

    /**
     * 设置左上角图标
     */
    protected void setLeftImage(int resourceId) {
        ivLeft.setImageResource(resourceId);
    }

    /**
     * 设置标题文字
     */
    protected void setBTitle(int resourceId) {
        tvTitle.setText(getResources().getText(resourceId));
    }

    protected void setBTitle(String title) {
        tvTitle.setText(title);
    }

    protected void hideOrShowIvCenter(boolean isShow) {
        ivCenter.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }
}
