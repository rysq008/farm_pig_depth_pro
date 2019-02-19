package innovation.media;

import android.content.Context;
import android.util.Log;

import org.tensorflow.demo.Global;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import innovation.utils.FileUtils;
import innovation.utils.StorageUtils;

/**
 * Created by  on 2019/2/15.小视频用相关文件接口
 */

public class MediaSmalVideoItem {

    /**
     * The name of path and bitmap
     */
    private String mDirName = "小视频";//storage/emulated/0/innovation/animal/小视频
    private File mDir = null;//storage/emulated/0/innovation/animal/小视频

    private String mCurrentName = mDirName + "/" + "Current"; //当前捕捉猪的文件（视频）
    private String mVideoName = mCurrentName + "/" + "视频";

    private String mZipVideoName = mDirName + "/" + "ZipVideo"; //保存所有视频zip文件

    private File mZipVideoDir = null;//storage/emulated/0/innovation/animal/小视频/ZipVideo

    private File mCurrentDir = null;//storage/emulated/0/innovation/animal/小视频/Current
    private File mVideoDir = null;//storage/emulated/0/innovation/animal/小视频/Current/视频

    Context mContext;

    public MediaSmalVideoItem(Context context) {
        mContext = context;
        currentInit();
    }


    public void currentInit()
    {
        //创建小视频目录
        if (mDir == null) {
            mDir = new File(StorageUtils.getExternalCacheDir(mContext), mDirName);
            if (!mDir.exists()) {
                mDir.mkdirs();
            }
        }

        //创建Current目录
        if (mCurrentDir == null) {
            mCurrentDir = new File(StorageUtils.getExternalCacheDir(mContext), mCurrentName);
            if (!mCurrentDir.exists()) {
                mCurrentDir.mkdirs();
            }
        }

        //创建视频目录
        if (mVideoDir == null) {
            mVideoDir = new File(StorageUtils.getExternalCacheDir(mContext), mVideoName);
            if (!mVideoDir.exists()) {
                mVideoDir.mkdirs();
            }
        }

        //创建视频zip目录
        if (mZipVideoDir == null) {
            mZipVideoDir = new File(StorageUtils.getExternalCacheDir(mContext), mZipVideoName);
            if (!mZipVideoDir.exists()) {
                mZipVideoDir.mkdirs();
            }
        }
    }

    public void zipVideoNameDel(){
        FileUtils.deleteFile(mZipVideoDir);
        mZipVideoDir = null;
        Log.i("zipVideoNameDel:","删除当前文件夹");
    }

    public void currentDel()    {
        FileUtils.deleteFile(mCurrentDir);
        mCurrentDir = null;
        mVideoDir = null;
        Log.i("currentDel:","删除当前文件夹");
    }
    //获得存储视频的video文件名
    public String getVideoFileName() {
        String strVideofile = null;
        File mtmpBimmapDir = null;
        if (mtmpBimmapDir == null) {
            mtmpBimmapDir = new File(StorageUtils.getExternalCacheDir(mContext), mVideoName);
            if (!mtmpBimmapDir.exists()) {
                mtmpBimmapDir.mkdirs();
            }
        }
        SimpleDateFormat tmpSimpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS", Locale.getDefault());
        String fname = tmpSimpleDateFormat.format(new Date(System.currentTimeMillis()));
        strVideofile = new File(mtmpBimmapDir, fname).getAbsolutePath() + Global.VIDEO_SUFFIX;///storage/emulated/0/innovation/animal/Current/视频/20180301105200.mp4

        return strVideofile;
    }


    public String getCurrentDir() {
        if (mCurrentDir == null) {
            mCurrentDir = new File(StorageUtils.getExternalCacheDir(mContext), mCurrentName);
            if (!mCurrentDir.exists()) {
                mCurrentDir.mkdirs();
            }
        }
        return mCurrentDir.getAbsolutePath();
    }

    public String getVideoDir() {
        if (mVideoDir == null) {
            mVideoDir = new File(StorageUtils.getExternalCacheDir(mContext), mVideoName);
            if (!mVideoDir.exists()) {
                mVideoDir.mkdirs();
            }
        }
        return mVideoDir.getAbsolutePath();
    }

    public String getZipVideoDir() {
        if (mZipVideoDir == null) {
            mZipVideoDir = new File(StorageUtils.getExternalCacheDir(mContext), mZipVideoName);
            if (!mZipVideoDir.exists()) {
                mZipVideoDir.mkdirs();
            }
        }
        return mZipVideoDir.getAbsolutePath();
    }

    //获得压缩文件名
    public String getZipFileName() {
        SimpleDateFormat tmpSimpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS", Locale.getDefault());
        String name =  tmpSimpleDateFormat.format(new Date(System.currentTimeMillis()));
        Global.ZipFileName = name;
        return name;
    }

    //删除Current文件夹下所有内容并重新创建该文件夹
    public void reInitCurrent()
    {
        if (mCurrentDir == null) {
            mCurrentDir = new File(StorageUtils.getExternalCacheDir(mContext), mCurrentName);
            if (mCurrentDir.exists()) {
                FileUtils.deleteFile(mCurrentDir);
            }
        }
        currentInit();
    }

}
