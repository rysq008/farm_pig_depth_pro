package com.xiangchuangtec.luolu.animalcounter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiangchuang.risks.model.bean.RecognitionResult;

import java.util.ArrayList;
import java.util.List;


public class JuanCountAdapter extends BaseAdapter {
    private final List<RecognitionResult> mRecognitionResults;
    private Context context;
    private JuanInterface listner;


    public JuanCountAdapter(Context context) {
        this.context = context;
        this.mRecognitionResults = new ArrayList<>();
    }

    public void addResult(RecognitionResult recognitionResult) {
        mRecognitionResults.add(recognitionResult);
        notifyDataSetChanged();
    }

    public void reset() {
        mRecognitionResults.clear();
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return mRecognitionResults.size();
    }

    @Override
    public Object getItem(int position) {
        return mRecognitionResults.get(position);
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
            convertView = View.inflate(context, R.layout.juan_item_layout, null);
            viewHolder.left_juan = (TextView) convertView.findViewById(R.id.left_juan);
            viewHolder.right_count = (TextView) convertView.findViewById(R.id.right_count);
            viewHolder.auto_count = (TextView) convertView.findViewById(R.id.auto_count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.left_juan.setText("猪圈" + (mRecognitionResults.get(position).index + 1));
        viewHolder.right_count.setText(mRecognitionResults.get(position).count +"头");
        viewHolder.auto_count.setText(mRecognitionResults.get(position).autoCount +"头");
        listner.getname("猪圈" + (mRecognitionResults.get(position).index + 1));
        return convertView;
    }

    class ViewHolder {
        TextView left_juan, right_count, auto_count;
    }
    public  interface  JuanInterface{
        void getname(String jname);
    }
    public  void  setListner(JuanInterface listner){
        this.listner=listner;
    }
}
