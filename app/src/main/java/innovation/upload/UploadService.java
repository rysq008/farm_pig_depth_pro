package innovation.upload;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.mainaer.wjoklib.okhttp.upload.UploadTask;
import com.mainaer.wjoklib.okhttp.upload.UploadTaskListener;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

import innovation.database.VideoUploadTable;
import innovation.database.VideoUploadTable_;
import io.objectbox.Box;


public class UploadService extends Service implements UploadTaskListener {

    private Box<VideoUploadTable> box;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
//                databaseHelper.queryAllTABLE_LIPEI();
                box = MyApplication.getBoxStore().boxFor(VideoUploadTable.class);
                List<VideoUploadTable> datas = box.query().equal(VideoUploadTable_.iscomplete, false).build().find();
                UploadUtils.uploadFile(UploadService.this, UploadService.this, datas);
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        databaseHelper.close();
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
        VideoUploadTable bean = (VideoUploadTable) uploadTask.getT();
        bean.iscomplete = true;
        box.put(bean);
    }

    @Override
    public void onError(UploadTask uploadTask, int errorCode, int position) {

    }

    @Override
    public void onPause(UploadTask uploadTask) {

    }
}
