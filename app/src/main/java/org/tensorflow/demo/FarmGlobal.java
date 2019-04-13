package org.tensorflow.demo;

import com.farm.innovation.biz.iterm.MediaInsureItem;
import com.farm.innovation.biz.iterm.MediaPayItem;
import com.farm.innovation.biz.iterm.Model;

import java.util.Vector;


/**
 * Author by luolu, Date on 2018/8/16.
 * COMPANY：InnovationAI
 */

public class FarmGlobal {

    public static int FrameWidth = 0;
    public static int FrameHeight = 0;
    public static int GloabalHeight = 0;
    public static int GloabalWidth = 0;
    //    public static HashMap<Integer, PostureItem> pigFaceRadian = new HashMap<Integer, PostureItem>();//保存猪脸的弧度值  //图片直接存入内存对应角度值的目录下
    public static MediaInsureItem mediaInsureItem = null;
    public static MediaPayItem mediaPayItem = null;
    public static String zipVideoFileName = "";
    public static String ZipImageFileName = "";
    public static String ZipFileName = "";
    public static int model = Model.BUILD.value();



    // 当APP采集并上传视频时设为TRUE
    public static  Boolean UPLOAD_VIDEO_FLAG = false;

    public static boolean VIDEO_PROCESS = false;

    //Constants
    public static final String IMAGE_JPEG = "jpg";
    public static final String VIDEO_MP4 = "mp4";
    public static final String IMAGE_SUFFIX = ".jpg";
    public static final String VIDEO_SUFFIX = ".mp4";

    public static final String FILEPRE_IMAGE = "image";
    public static final String FILEPRE_VIDEO = "video";

    public static boolean DeviceOrientation = false;

    public static String VideoFileName = "";

    public static Vector<String> listAngles_capture = new Vector<String>();

    public static int Func_type;
    public static final int Func_Insurance = 1;
    public static final int Func_Pay = 2;
    public static int waitUploadCount ;


}
