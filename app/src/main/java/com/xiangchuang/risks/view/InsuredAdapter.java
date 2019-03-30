package com.xiangchuang.risks.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiangchuang.risks.model.bean.InsureListBean;
import com.innovation.pig.insurance.R;

import java.util.List;

public class InsuredAdapter extends RecyclerView.Adapter<InsuredAdapter.MyViewHolder> {
    private List<InsureListBean.DataBean> list;
    private Context context;
    public InsuredAdapter(Context context,List<InsureListBean.DataBean> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_insuredadapter, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.clas.setText(list.get(position).getPigTypeName());
        holder.heardnum.setText(list.get(position).getAmount()+"");
        holder.coefficient.setText(list.get(position).getRatio()+"");
        holder.time.setText(list.get(position).getTerm());
        if(list.get(position).getBaodanStatus()==1){
            holder.yes.setVisibility(View.VISIBLE);
            holder.no.setVisibility(View.GONE);
        }else {
            holder.no.setVisibility(View.VISIBLE);
            holder.yes.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView yes;
        TextView no;
        TextView clas;
        TextView heardnum;
        TextView coefficient;
        TextView time;


        public MyViewHolder(View itemView) {
            super(itemView);
            yes = itemView.findViewById(R.id.yes);
            no = itemView.findViewById(R.id.no);
            clas = itemView.findViewById(R.id.clas);
            heardnum = itemView.findViewById(R.id.heardnum);
            coefficient = itemView.findViewById(R.id.coefficient);
            time = itemView.findViewById(R.id.time);
        }
    }
}
