package com.farm.innovation.update;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.farm.innovation.bean.VideoUpLoadBean;
import com.farm.innovation.login.DatabaseHelper;
import com.farm.innovation.utils.UploadUtils;
import com.farm.mainaer.wjoklib.okhttp.upload.UploadTask;
import com.farm.mainaer.wjoklib.okhttp.upload.UploadTaskListener;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;


public class UploadService extends Service implements UploadTaskListener {
    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = DatabaseHelper.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
//                databaseHelper.queryAllTABLE_LIPEI();
                List<VideoUpLoadBean> datas = databaseHelper.queryVideoUpLoadDataByStatus(0);
                UploadUtils.uploadFile(UploadService.this, UploadService.this, datas);
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
//        databaseHelper = null;
//        UploadManager.getInstance().clear();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onUploading(UploadTask uploadTask, String percent, int position) {

    }

    @Override
    public void onUploadSuccess(UploadTask uploadTask, File file) {
//        databaseHelper.updateLiPeiLocalUpSuccess("1", uploadTask.getId());
        VideoUpLoadBean bean = (VideoUpLoadBean) uploadTask.getT();
        bean.uploadComplete = 1 + "";
        databaseHelper.updataVideoUpLoadBean(bean);
    }

    @Override
    public void onError(UploadTask uploadTask, int errorCode, int position) {

    }

    @Override
    public void onPause(UploadTask uploadTask) {

    }
}
