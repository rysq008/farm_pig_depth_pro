package com.xiangchuang.risks.model.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.model.bean.PigTypeBean;


public class PigTypeListAdapter extends BaseQuickAdapter<PigTypeBean.DataBean, BaseViewHolder> {
    public PigTypeListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(BaseViewHolder holder, PigTypeBean.DataBean item) {
        holder.setText(R.id.tv_type_name,item.getPigTypeName());
    }
}
