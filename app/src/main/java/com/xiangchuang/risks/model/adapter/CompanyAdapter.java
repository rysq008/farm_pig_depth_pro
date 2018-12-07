package com.xiangchuang.risks.model.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.CompanyBean;
import com.xiangchuang.risks.model.bean.InSureCompanyBean;
import com.xiangchuang.risks.view.AddCompanyActivity;
import com.xiangchuang.risks.view.CompanyActivity;
import com.xiangchuang.risks.view.SelectFunctionActivity_new;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import java.util.List;

public class CompanyAdapter extends BaseAdapter {
    private List<InSureCompanyBean> companyBeans;
    private Context context;


    public CompanyAdapter(Context context, List<InSureCompanyBean> companyBeans) {
        this.context = context;
        this.companyBeans = companyBeans;
    }

    @Override
    public int getCount() {
        return companyBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return companyBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.companyitem_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.companyitem_name = (TextView) convertView.findViewById(R.id.companyitem_name);
            viewHolder.no_over = convertView.findViewById(R.id.no_over);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.companyitem_name.setText(companyBeans.get(position).getEnName());
        if(companyBeans.get(position).getCanUse().equals("1")){
            viewHolder.no_over.setVisibility(View.GONE);
        }
        if(companyBeans.get(position).getCanUse().equals("0")){
            viewHolder.no_over.setVisibility(View.VISIBLE);
        }
        viewHolder.companyitem_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseActivity activity = (BaseActivity) context;
                if(companyBeans.get(position).getEnId()!=null&&!companyBeans.get(position).getEnId().isEmpty()){
                    PreferencesUtils.saveKeyValue(Constants.en_id, companyBeans.get(position).getEnId(), MyApplication.getAppContext());
                }
                if(companyBeans.get(position).getEnName()!=null&&!companyBeans.get(position).getEnName().isEmpty()){
                    PreferencesUtils.saveKeyValue(Constants.companyname, companyBeans.get(position).getEnName(), MyApplication.getAppContext());
                }
                if(companyBeans.get(position).getEnUserId()!=null&&!companyBeans.get(position).getEnUserId().equals("")){
                    PreferencesUtils.saveIntValue(Constants.en_user_id, Integer.valueOf(companyBeans.get(position).getEnUserId()), MyApplication.getAppContext());
                }

                String type = companyBeans.get(position).getCanUse();
                if(type.equals("1")) {
                    activity.goToActivity(SelectFunctionActivity_new.class, null);
                }
                if(type.equals("0")){
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("type",true);
                    activity.goToActivity(AddCompanyActivity.class, bundle);
                }
            }
        });

        return convertView;
    }

    class ViewHolder {
        TextView companyitem_name;
        TextView no_over;
    }
}
