package com.xiangchuang.risks.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.innovation.pig.insurance.R;

import java.io.File;


/**
 * 描    述： 设置占位图
 */
public class ImageViewExtension {

    /**
     * 跳过缓存  接受图片验证码使用
     *
     * @param context
     * @param imageView
     * @param url
     */
    public static void loadImageSkipCache(Context context, ImageView imageView, String url) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().error(R.drawable.default_pic))
                .into(imageView);

    }

    /**
     * banner
     */
    public static void loadImageToBanner(Context context, ImageView imageView, String url) {
        Glide.with(context).load(url)
                .apply(new RequestOptions().fitCenter())
                .into(imageView);
    }

    /**
     * 通用
     *
     * @param context
     * @param imageView
     * @param url
     */
    public static void loadImageToComm(Context context, ImageView imageView, String url) {
        Glide.with(context).load(url)
                .apply(new RequestOptions().dontAnimate().centerCrop().error(R.drawable.ic_default_icon)).into(imageView);
    }

    public static void loadImageToComm(Activity context, ImageView imageView, String url) {
        Glide.with(context).load(url)
                .apply(new RequestOptions().dontAnimate().centerCrop().error(R.drawable.default_pic))
                .into(imageView);
    }

    public static void loadImageToComm(Fragment context, ImageView imageView, String url) {
        Glide.with(context).
                load(url)
                .apply(new RequestOptions().dontAnimate().centerCrop().error(R.drawable.default_pic))
                .into(imageView);
    }

    /**
     * @param context
     * @param imageView
     */
    public static void loadImageToComm(Context context, ImageView imageView, File file) {
        Glide.with(context).load(file)
                .apply(new RequestOptions().dontAnimate().centerCrop().error(R.drawable.ic_default_icon))
                .into(imageView);
    }

    /**
     * @param context
     * @param imageView
     */
    public static void loadImageToComm(Context context, ImageView imageView, byte[] bytes) {
        Glide.with(context).load(bytes)
                .apply(new RequestOptions().dontAnimate().centerCrop().error(R.drawable.ic_default_icon))
                .into(imageView);
    }

    /**
     * @param context
     * @param imageView
     */
    public static void loadImageToCommBase(Context context, ImageView imageView, String s) {
        Glide.with(context).asBitmap().load(s).apply(new RequestOptions().dontAnimate().centerCrop().error(R.drawable.ic_default_icon))
                .into(imageView);
    }
}
