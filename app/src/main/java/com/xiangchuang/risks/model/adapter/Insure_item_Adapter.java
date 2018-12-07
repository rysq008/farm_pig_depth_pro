package com.xiangchuang.risks.model.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiangchuang.risks.model.bean.InSureBean;
import com.xiangchuangtec.luolu.animalcounter.R;

import java.util.List;


public class Insure_item_Adapter extends BaseAdapter {
    private final List<InSureBean.DataBean.FtnBaodanListBean> ftnBaodanListBeans;
    private Context context;


    public Insure_item_Adapter(Context context, List<InSureBean.DataBean.FtnBaodanListBean> ftnBaodanListBeans) {
        this.context = context;
        this.ftnBaodanListBeans = ftnBaodanListBeans;
    }


    @Override
    public int getCount() {
        return ftnBaodanListBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return ftnBaodanListBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(context, R.layout.insure_item_layout, null);
            viewHolder.insuretime = (TextView) convertView.findViewById(R.id.insure_time);
            viewHolder.insuretou = (TextView) convertView.findViewById(R.id.insure_tou);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.insuretime.setText(ftnBaodanListBeans.get(position).getCreatetime());
        viewHolder.insuretou.setText("投保"+ftnBaodanListBeans.get(position).getAmount() + "头");
        return convertView;
    }

    class ViewHolder {
        TextView insuretime, insuretou;
    }
}
