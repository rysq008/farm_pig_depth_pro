package com.farm.innovation.biz.insurance.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.BaoDanBeanNew;
import com.farm.innovation.biz.insurance.CreateYanActivity;
import com.farm.innovation.biz.insurance.YanBiaoDanActivity;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.PreferencesUtils;
import com.innovation.pig.insurance.R;

import java.util.List;

public class YanBiaoAdapter extends BaseAdapter {

    private final List<BaoDanBeanNew> baoDanBeanNews;
    private Context context;


    public YanBiaoAdapter(Context context, List<BaoDanBeanNew> baoDanBeanNews) {
        this.context = context;
        this.baoDanBeanNews = baoDanBeanNews;
    }

    @Override
    public int getCount() {
        return baoDanBeanNews.size();
    }

    @Override
    public Object getItem(int position) {
        return baoDanBeanNews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.farm_yaobiao_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.baodan_name = (TextView) convertView.findViewById(R.id.baodan_name);
            viewHolder.baodan_pinzhong = (TextView) convertView.findViewById(R.id.baodan_pinzhong);
            viewHolder.toubao_Pname = (TextView) convertView.findViewById(R.id.toubao_Pname);
            viewHolder.toubao_rate = (TextView) convertView.findViewById(R.id.toubao_rate);
            viewHolder.toubao_date = (TextView) convertView.findViewById(R.id.toubao_date);
            viewHolder.baodan_type = (TextView) convertView.findViewById(R.id.baodan_type);
            viewHolder.newInsured = (RelativeLayout) convertView.findViewById(R.id.newInsured);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String bankName = baoDanBeanNews.get(position).bankName;
        Log.i("bankName", bankName + "");
        //保单名字
        viewHolder.baodan_name.setText(bankName);
        //保单类型
        int baodanType = baoDanBeanNews.get(position).baodanType;
        viewHolder.baodan_type.setText(1 == baodanType ? "企业" : "组织");
        //牲畜品种
        String toubaoTypeString = baoDanBeanNews.get(position).toubaoTypeString;
        viewHolder.baodan_pinzhong.setText(toubaoTypeString);
        //投保人
        viewHolder.toubao_Pname.setText(baoDanBeanNews.get(position).toubaoPname);
        //费率
        viewHolder.toubao_rate.setText("费率：" + baoDanBeanNews.get(position).baodanRate + "%");
        String createtime = baoDanBeanNews.get(position).createtime;
        viewHolder.toubao_date.setText((null != createtime && createtime.length() > 0) ? createtime.substring(0, 10) : createtime);
        viewHolder.newInsured.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesUtils.saveKeyValue(HttpUtils.baodanType, baoDanBeanNews.get(position).baodanType + "", FarmAppConfig.getApplication());
                PreferencesUtils.saveKeyValue(HttpUtils.id, baoDanBeanNews.get(position).id + "", FarmAppConfig.getApplication());
                // activity.goToActivity(HomeActivity.class, null);
                YanBiaoDanActivity yanBiaoDanActivity = (YanBiaoDanActivity) context;
                yanBiaoDanActivity.goToActivity(CreateYanActivity.class, null);
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView baodan_name, baodan_pinzhong, toubao_Pname, toubao_rate, toubao_date, baodan_type;
        RelativeLayout newInsured;
    }
}
