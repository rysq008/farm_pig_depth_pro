package com.xiangchuang.risks.model.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.model.bean.PayInfo;
import com.xiangchuang.risks.utils.ImageViewExtension;

public class PayListAdapter extends BaseQuickAdapter<PayInfo, BaseViewHolder> {
    public PayListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(BaseViewHolder helper, PayInfo item) {
        helper.setText(R.id.tv_pay_time,item.getPayTime())

                .setText(R.id.tv_pig_type,item.getSheName());
        if(TextUtils.isEmpty(item.getBaodanNo())){
            helper.setText(R.id.tv_insurance_no,"未匹配");
        }else{
            helper.setText(R.id.tv_insurance_no,item.getBaodanNo());
        }

        ImageView imageView = helper.getView(R.id.iv_pay_icon);

        ImageViewExtension.loadImageToComm(mContext,imageView,item.getPigImg());
    }
}
