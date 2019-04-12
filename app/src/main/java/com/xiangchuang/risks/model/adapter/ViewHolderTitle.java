package com.xiangchuang.risks.model.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.innovation.pig.insurance.R;

public class ViewHolderTitle extends RecyclerView.ViewHolder {
     TextView mOpenRecordDateTv;
    public ViewHolderTitle(View itemView) {
        super(itemView);
        mOpenRecordDateTv = (TextView) itemView.findViewById(R.id.tv_pay_date);

    }
}
