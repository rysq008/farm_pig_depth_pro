package com.xiangchuang.risks.model.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.innovation.pig.insurance.R;

public class ViewHolderContent extends RecyclerView.ViewHolder {
    TextView tvBaodanNum;
    TextView tvDeadDete;
    TextView tvWeight;
    TextView tvPigType;
    TextView tvBiaoNum;
    TextView tvIsRepeat;
    CheckBox checkBox;
    RelativeLayout rlWaitDisposeSub;


    public ViewHolderContent(View view) {
        super(view);
        tvBaodanNum = (TextView) view.findViewById(R.id.tv_baodan_num);
        tvDeadDete = (TextView) view.findViewById(R.id.tv_dead_dete);
        tvWeight = (TextView) view.findViewById(R.id.tv_weight);
        tvPigType = (TextView) view.findViewById(R.id.tv_pigType);
        tvBiaoNum = (TextView) view.findViewById(R.id.tv_biao_num);
        tvIsRepeat = (TextView) view.findViewById(R.id.tv_is_repeat);
        checkBox = (CheckBox) view.findViewById(R.id.cb_checked);
        rlWaitDisposeSub = (RelativeLayout)view.findViewById(R.id.rl_wait_dispose_sub);


    }
}
