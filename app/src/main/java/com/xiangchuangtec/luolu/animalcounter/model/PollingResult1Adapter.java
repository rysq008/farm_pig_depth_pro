package com.xiangchuangtec.luolu.animalcounter.model;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiangchuang.risks.model.myinterface.MyInterface;
import com.xiangchuang.risks.model.custom.MyListView;
import com.xiangchuang.risks.model.myinterface.MyNum;
import com.xiangchuang.risks.model.bean.PollingListBean;
import com.xiangchuang.risks.model.adapter.PollingResultItem1Adapter;
import com.xiangchuangtec.luolu.animalcounter.R;

import java.util.List;


public class PollingResult1Adapter extends BaseAdapter {


    private final List<PollingListBean.DataBean.List1Bean> list0Beans;
    private final String fleg;
    private Context context;
    private MyInterface myInterface;
    private MyNum myNum;
    int sum = 0;

    public PollingResult1Adapter(Context context, List<PollingListBean.DataBean.List1Bean> list0Beans, String fleg) {
        this.context = context;
        this.list0Beans = list0Beans;
        this.fleg = fleg;
    }


    @Override
    public int getCount() {
        return list0Beans.size();
    }

    @Override
    public Object getItem(int position) {
        return list0Beans.get(position);
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
            convertView = View.inflate(context, R.layout.result_list_item, null);
            viewHolder.shename = (TextView) convertView.findViewById(R.id.shename);
            viewHolder.pollshe_totlecount = (TextView) convertView.findViewById(R.id.pollshe_totlecount);
            viewHolder.shelist = (MyListView) convertView.findViewById(R.id.shelist);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.shename.setText(list0Beans.get(position).getName());
        viewHolder.pollshe_totlecount.setText(list0Beans.get(position).getCount() + "");
        List<PollingListBean.DataBean.List1Bean.JuanListBeanX> juanList = list0Beans.get(position).getJuanList();
        PollingResultItem1Adapter pollingResultItem1Adapter = new PollingResultItem1Adapter(context, juanList, list0Beans.get(position).getName(),fleg);
        viewHolder.shelist.setAdapter(pollingResultItem1Adapter);
        pollingResultItem1Adapter.setListner(new MyInterface() {
            @Override
            public void isOut(Boolean aBoolean) {
                Log.i("是否出栏",aBoolean+"");
                myInterface.isOut(aBoolean);
            }
        });
        return convertView;
    }

    public void setListner(MyInterface myInterface) {
        this.myInterface = myInterface;
    }

    class ViewHolder {
        TextView shename, pollshe_totlecount;
        MyListView shelist;

    }
}
