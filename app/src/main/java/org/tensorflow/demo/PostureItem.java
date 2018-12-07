package org.tensorflow.demo;

import android.graphics.Bitmap;

/**
 * Created by haojie on 2018/2/9.
 */

public class PostureItem {
    public float rot_x; //原始X轴弧度值
    public float rot_y; //原始Y轴弧度值
    public float rot_z; //原始Z轴弧度值
    public float original_x1; //屏幕x1
    public float original_y1; //屏幕y1
    public float original_x2; //屏幕x2
    public float original_y2; //屏幕y2

    public Bitmap original_bitmap; //保存的原始大小的图片

    public Bitmap big_bitmap; //保存的放大到指定倍数的图片

    public PostureItem(float x, float y, float z, Bitmap bitmap)
    {
        rot_x = x;
        rot_y = y;
        rot_z = z;
        original_bitmap = bitmap;
    }
    public PostureItem(float x, float y, float z, float x1, float y1, float x2, float y2, Bitmap originalBitmap, Bitmap bigBitmap)
    {
        rot_x = x;
        rot_y = y;
        rot_z = z;
        original_x1 = x1;
        original_y1 = y1;
        original_x2 = x2;
        original_y2 = y2;
        original_bitmap = originalBitmap;
        big_bitmap = bigBitmap;
    }

    public PostureItem()
    {

    }

}
