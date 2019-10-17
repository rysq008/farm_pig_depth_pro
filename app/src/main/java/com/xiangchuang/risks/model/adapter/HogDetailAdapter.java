package com.xiangchuang.risks.model.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.innovation.pig.insurance.R;

import java.util.List;

import innovation.utils.UIUtils;

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

        int x = UIUtils.getWidthPixels(context);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.hog_image.getLayoutParams();
        params.width = x / 2;
        params.height = x / 8 * 3;
        viewHolder.hog_image.setLayoutParams(params);

        Glide.with(context)
                .load(hogimages.get(position))
                .apply(new RequestOptions().error(R.drawable.pig_ic_launcher).placeholder(R.drawable.pig_ic_launcher))
                .into(viewHolder.hog_image);
        return convertView;
    }

    class ViewHolder {
        ImageView hog_image;
    }
}
