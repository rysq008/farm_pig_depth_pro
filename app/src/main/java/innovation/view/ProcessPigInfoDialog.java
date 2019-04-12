package innovation.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.model.bean.UncompletedBean;
import com.xiangchuang.risks.utils.SystemUtil;

import java.util.ArrayList;
import java.util.List;

import innovation.utils.ScreenUtil;


/**
 * @Author: Lucas.Cui
 * 时   间：2019/4/11
 * 简   述：<功能简述>
 */
public class ProcessPigInfoDialog extends Dialog {

    private RecyclerView recyclerView;
    private PigListAdapter mAdapter;
    private List<UncompletedBean.payInfo> mLiPeiInfo = new ArrayList<>();

    @SuppressLint("JavascriptInterface")
    public ProcessPigInfoDialog(Context context) {
        super(context, R.style.alert_dialog_style);
        setContentView(R.layout.dialog_processpig_info);
//        setContentView(view);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.alpha = 1.0f;
        params.width = (int) (ScreenUtil.getScreenWidth() - 35 * SystemUtil.getDensity());
        window.setAttributes(params);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //设置LayoutManager为LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        //设置Adapter
        mAdapter = new PigListAdapter(R.layout.item_pigprocess_info);
        recyclerView.setAdapter(mAdapter);
    }

    public void setData(UncompletedBean uncompletedBean) {
        if (uncompletedBean != null) {
            for (int i = 0; i < uncompletedBean.getLipeiInfos().size(); i++) {
                for (int j = 0; j < uncompletedBean.getLipeiInfos().get(i).getPayInfoList().size(); j++) {
                    mLiPeiInfo.add(uncompletedBean.getLipeiInfos().get(i).getPayInfoList().get(j));
                }
            }
            mAdapter.setNewData(mLiPeiInfo);
        }
    }

    public static class PigListAdapter extends BaseQuickAdapter<UncompletedBean.payInfo, BaseViewHolder> {
        public PigListAdapter(int layoutResId) {
            super(layoutResId);
        }

        @Override
        protected void convert(BaseViewHolder helper, UncompletedBean.payInfo item) {
            helper.setText(R.id.tv_pig_type, "投保种类：" + item.getPigTypeName());
            helper.setText(R.id.tv_pig_pay_date, "理赔日期：" + item.getLipeiDate());
            helper.setText(R.id.tv_pig_baodan_no, "保单号：" + item.getBaodanNo());
            helper.setText(R.id.tv_pig_remark_code, "标的识别码：" + item.getSeqNo());
            helper.setText(R.id.tv_pig_dead_time, "死亡时间：" + item.getDeathTime());
        }
    }
}
