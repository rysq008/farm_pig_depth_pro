package com.xiangchuang.risks.model.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.InSureCompanyBean;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import java.util.List;

public class HogDetailAdapter extends BaseAdapter {

    private Context context;
    private List<String> hogimages;


    public HogDetailAdapter(Context context, List<String> hogimages) {
        this.context = context;
        this.hogimages = hogimages;
    }

    @Override
    public int getCount() {
        return hogimages.size();
    }

    @Override
    public Object getItem(int position) {
        return hogimages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.hog_detail_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.hog_image = (ImageView) convertView.findViewById(R.id.hog_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Glide.with(context)
                .load(hogimages.get(position))
                .error(R.drawable.ic_launcher)
                .placeholder(R.drawable.ic_launcher)
                .into(viewHolder.hog_image);
        return convertView;
    }

    class ViewHolder {
        ImageView hog_image;
    }
}
