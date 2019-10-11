package com.xiangchuangtec.luolu.animalcounter.model;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.innovation.pig.insurance.R;

import java.util.List;


public class PollingCheckAdapter extends BaseAdapter {
    private Context context;
    private List<CheckBean.DataBean.ResultDetailsBean> resultDetailsBeanList;

    public PollingCheckAdapter(Context context, List<CheckBean.DataBean.ResultDetailsBean> resultDetailsBeanList) {
        this.context = context;
        this.resultDetailsBeanList = resultDetailsBeanList;
    }

    @Override
    public int getCount() {
        return resultDetailsBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultDetailsBeanList.get(position);
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
            convertView = View.inflate(context, R.layout.pollingresult_item, null);
            viewHolder.pollingzhushename =(TextView)convertView.findViewById(R.id.polling_zhushe_name);
            viewHolder.pollingzhushecount = (TextView)convertView.findViewById(R.id.polling_zhushe_count);
            viewHolder.pollinglistview = (ListView) convertView.findViewById(R.id.polling_listview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.pollingzhushename.setText(resultDetailsBeanList.get(position).getName());
        int nums = resultDetailsBeanList.get(position).getNums();
        Log.i("ShowPollingAdapter", nums+"");
        viewHolder.pollingzhushecount.setText(String.valueOf(nums));
        viewHolder.pollinglistview.setAdapter(new Polling_Check_itemAdapter(context,resultDetailsBeanList.get(position).getRecords()));
        return convertView;
    }

    class ViewHolder {
        TextView pollingzhushename, pollingzhushecount;
        ListView pollinglistview;
    }
}
