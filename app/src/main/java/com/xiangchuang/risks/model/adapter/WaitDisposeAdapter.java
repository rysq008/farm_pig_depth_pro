package com.xiangchuang.risks.model.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.model.bean.UncompletedBean;

import java.util.List;

public class WaitDisposeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int ITEM_TITLE = 1;
    private int ITEM_CONTENT = 2;
    private List<Object> objects;

    /**
     * 传入数据
     * @param objects
     */
    public void setDate(List<Object> objects) {
        this.objects = objects;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        RecyclerView.ViewHolder holder = null;
        if (viewType == ITEM_TITLE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wait_dispose_title, parent, false);
            holder = new ViewHolderTitle(view);
        } else if (viewType == ITEM_CONTENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wait_dispose_sub, parent, false);
            holder = new ViewHolderContent(view);

        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderTitle) {
            String title = (String) objects.get(position);
            ((ViewHolderTitle) holder).mOpenRecordDateTv.setText(title);
        }else if (holder instanceof ViewHolderContent) {
            UncompletedBean.payInfo bean = (UncompletedBean.payInfo) objects.get(position);

            ((ViewHolderContent) holder).tvBaodanNum.setText(bean.getBaodanNo());
            ((ViewHolderContent) holder).tvDeadDete.setText(String.valueOf(bean.getDeathTime()));
            ((ViewHolderContent) holder).tvWeight.setText(bean.getWeight());
            ((ViewHolderContent) holder).tvPigType.setText(bean.getPigTypeName());
            ((ViewHolderContent) holder).tvBiaoNum.setText(bean.getSeqNo());
            ((ViewHolderContent) holder).tvIsRepeat.setText(bean.getRepeat());
            ((ViewHolderContent) holder).checkBox.setChecked(bean.isSelected());

            ((ViewHolderContent) holder).rlWaitDisposeSub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((ViewHolderContent) holder).checkBox.isChecked()){
                        ((ViewHolderContent) holder).checkBox.setChecked(false);
                    }else{
                        ((ViewHolderContent) holder).checkBox.setChecked(true);
                    }
                    onItemCheckListener.callBack(bean.getLipeiNo(), ((ViewHolderContent) holder).checkBox.isChecked());
                }
            });

            ((ViewHolderContent) holder).rlWaitDisposeSub.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemLongClickListener.callBack(bean);
                    return false;
                }
            });

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (objects.get(position) instanceof String) {
            return ITEM_TITLE;
        } else if (objects.get(position) instanceof UncompletedBean.payInfo) {
            return ITEM_CONTENT;
        }
        return super.getItemViewType(position);
    }


    @Override
    public int getItemCount() {
        return objects == null ? 0 : objects.size();
    }



    private OnItemCheckListener onItemCheckListener;

    public void setOnItemCheckListener(OnItemCheckListener listener){
        onItemCheckListener = listener;
    }

    public interface OnItemCheckListener {
        void callBack(String id, boolean checked);
    }

    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        onItemLongClickListener = listener;
    }

    public interface OnItemLongClickListener{
        void callBack(UncompletedBean.payInfo payInfo);
    }


}







