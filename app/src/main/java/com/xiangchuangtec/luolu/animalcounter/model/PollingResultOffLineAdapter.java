package com.xiangchuangtec.luolu.animalcounter.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.view.ShowPollingActivity_new;
import com.xiangchuangtec.luolu.animalcounter.R;

import java.util.List;

import innovation.database.SheInfo;


public class PollingResultOffLineAdapter extends BaseAdapter {
    private Context context;
    private List<SheInfo> recordList;
    private OnDetailClickListener mListener;
    private OnDetailitemClickListener onDetailitemClickListener;

    public interface OnDetailClickListener {
//        public void onClick(int sheId);
        public void onClick(int position);
    }

    public PollingResultOffLineAdapter(Context context, List<SheInfo> recordList,
                                       OnDetailClickListener listener) {
        this.context = context;
        this.recordList = recordList;
        mListener = listener;
    }


    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int position) {
        return recordList.get(position);
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
            convertView = View.inflate(context, R.layout.result_list_item_new, null);
            viewHolder.polltitle = (TextView) convertView.findViewById(R.id.poll_title);
            viewHolder.pollcount = (TextView) convertView.findViewById(R.id.poll_count);
            viewHolder.pollDetail = (LinearLayout) convertView.findViewById(R.id.poll_detail);
            viewHolder.pollDate = (TextView) convertView.findViewById(R.id.poll_date);
            viewHolder.poll_countHog = (TextView) convertView.findViewById(R.id.poll_countHog);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.polltitle.setText(recordList.get(position).sheName+"_"+recordList.get(position).pigTypeName);
        viewHolder.pollcount.setText(recordList.get(position).count);
        viewHolder.pollDetail.setTag(position);
        viewHolder.pollDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("0".equals(recordList.get(position).count)) {

                    AlertDialogManager.showMessageDialogOne(context, "提示", "该猪舍猪只数量为0", new AlertDialogManager.DialogInterface() {
                        @Override
                        public void onPositive() {

                        }

                        @Override
                        public void onNegative() {

                        }
                    });
                } else {
                    Integer position = (Integer) v.getTag();
//                    mListener.onClick(Integer.valueOf(recordList.get(position).getSheId()));
                    mListener.onClick(position);
                }
            }
        });
        ShowPollingActivity_new activity= (ShowPollingActivity_new) context;
        viewHolder.poll_countHog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDetailitemClickListener != null){
                    onDetailitemClickListener.onClick(position);
                }
            }
        });
        String time = recordList.get(position).dianshuTime;
        if (time != null) {
            if (time.length() > 10)
                time = time.substring(0, 10);
            viewHolder.pollDate.setText(time);
        }

        return convertView;
    }
    public  void  setListener(OnDetailitemClickListener onDetailitemClickListener){
        this.onDetailitemClickListener=onDetailitemClickListener;
    };

    class ViewHolder {
        TextView polltitle, pollcount, pollDate,poll_countHog ;
         LinearLayout  pollDetail;
    }
    public interface OnDetailitemClickListener {
        public void onClick(int position);
    }

}
