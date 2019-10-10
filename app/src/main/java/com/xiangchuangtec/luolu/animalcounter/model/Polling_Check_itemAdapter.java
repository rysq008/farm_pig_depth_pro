package com.xiangchuangtec.luolu.animalcounter.model;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiangchuangtec.luolu.animalcounter.R;

import java.util.List;


public class Polling_Check_itemAdapter extends BaseAdapter {
    private  List<CheckBean.DataBean.ResultDetailsBean.RecordsBean> recordsBeans;
    private Context context;


    public Polling_Check_itemAdapter(Context context, List<CheckBean.DataBean.ResultDetailsBean.RecordsBean> recordsBeans) {
        this.context = context;
        this.recordsBeans = recordsBeans;
    }


    @Override
    public int getCount() {
        return recordsBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return recordsBeans.get(position);
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
            convertView = View.inflate(context, R.layout.result_child_list_item, null);
            viewHolder.childname =(TextView)convertView.findViewById(R.id.child_name);
            viewHolder.childcount = (TextView)convertView.findViewById(R.id.child_count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.childname.setText(recordsBeans.get(position).getName());
        int nums = recordsBeans.get(position).getNums();
        Log.i("PollingCheckitemAdapter", nums+"");
        viewHolder.childcount.setText(String.valueOf(nums));
        return convertView;
    }

    class ViewHolder {
        TextView childname, childcount;
    }
}
