package com.xiangchuang.risks.model.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.model.bean.SheListBean;

public class PigHouseListAdapter extends BaseQuickAdapter<SheListBean.DataOffLineBaodanBean, BaseViewHolder> {
    public PigHouseListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(BaseViewHolder helper, SheListBean.DataOffLineBaodanBean item) {
        helper.setText(R.id.tv_pig_house_name, item.getSheName())
        .setText(R.id.tv_pig_house_type, item.getPigTypeName())
        .setText(R.id.tv_pig_house_time,item.getCreatetime());
    }
}
