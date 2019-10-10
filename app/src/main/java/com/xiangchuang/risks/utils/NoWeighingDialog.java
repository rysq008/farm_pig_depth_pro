package com.xiangchuang.risks.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.model.adapter.PayListAdapter;
import com.xiangchuang.risks.model.bean.PayInfo;
import com.xiangchuang.risks.view.AddPigPicActivity;

import java.util.List;

import innovation.utils.ScreenUtil;

/**
 * 未称重dialog
 */
public class NoWeighingDialog {

    private static Context mcontext;

    /**
     * 未称重dialog
     */
    public static Dialog showNoWeighingDialog(Context context, List<PayInfo> data){
        mcontext = context;
        //改完
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.alert_dialog_style);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.no_weighing_dialog_layout, null);

        Dialog dialog = builder.create();
        dialog.show();

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.width =(int) (ScreenUtil.getScreenWidth() - 40 * SystemUtil.getDensity());
        layoutParams.height = (int) (ScreenUtil.getScreenHeight()- 100 * SystemUtil.getDensity());//
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setContentView(v);


        RecyclerView recyclerView = v.findViewById(R.id.rv_no_weighing_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        PayListAdapter payListAdapter = new PayListAdapter(R.layout.item_no_weighing);
        payListAdapter.setNewData(data);
        recyclerView.setAdapter(payListAdapter);

        RelativeLayout rBack = v.findViewById(R.id.rl_dialog_back);
        rBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)context).finish();
            }
        });

        payListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                PayInfo payInfo = (PayInfo) adapter.getData().get(position);
                context.startActivity(new Intent(context, AddPigPicActivity.class)
                        .putExtra("lipeiid", payInfo.getLipeiId())
                        .putExtra("insureNo", payInfo.getBaodanNo())
                        .putExtra("lipeiNo", payInfo.getLipeiNo())
                        .putExtra("timesFlag", payInfo.getTimesFlag())
                        .putExtra("cPigType", payInfo.getPigTypeId())
                );
                dialog.dismiss();
            }
        });

        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);

        dialog.setOnKeyListener(onKeyListener);

        return dialog;
    }

    /**
     * add a keylistener for progress dialog
     */
    private static DialogInterface.OnKeyListener onKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                ((Activity)mcontext).finish();
            }
            return false;
        }
    };


}
