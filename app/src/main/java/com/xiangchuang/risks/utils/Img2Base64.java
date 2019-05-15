package com.xiangchuang.risks.utils;

import android.util.Base64;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 将图片转换为Base64<br>
 * 将base64编码字符串解码成img图片
 */
public class Img2Base64 {
    /**
     * 将图片转换成Base64编码
     * @param imgFile 待处理图片
     * @return
     */
    public static String getImgStr(String imgFile){
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(imgFile);        
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String base64Str = Base64.encodeToString(data, Base64.DEFAULT);
        Logger.i("base64Str" + base64Str);
        return base64Str;
    }
}


