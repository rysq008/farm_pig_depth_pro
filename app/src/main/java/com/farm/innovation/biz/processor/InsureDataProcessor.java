package com.farm.innovation.biz.processor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.ResultBean;
import com.farm.innovation.bean.ToubaoUploadBean;
import com.farm.innovation.biz.Insured.AddAnimalActivity;
import com.farm.innovation.biz.dialog.InsureDialog;
import com.farm.innovation.biz.dialog.ReviewImageDialog;
import com.farm.innovation.biz.dialog.ReviewVideoDialog;
import com.farm.innovation.biz.dialog.ToubaoResultDialog;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.location.LocationManager;
import com.farm.innovation.login.DatabaseHelper;
import com.farm.innovation.login.RespObject;
import com.farm.innovation.login.Utils;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.HttpRespObject;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.PreferencesUtils;
import com.farm.innovation.utils.UploadObject;
import com.farm.innovation.utils.ZipUtil;
import com.google.gson.Gson;
import com.innovation.pig.insurance.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.demo.FarmDetectorActivity;
import org.tensorflow.demo.FarmGlobal;
import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.farm.innovation.base.FarmAppConfig.getStringTouboaExtra;
import static com.farm.innovation.base.FarmAppConfig.getlipeiTempNumber;
import static com.farm.innovation.utils.HttpUtils.INSURE_IMAGE_UPLOAD;
import static com.farm.innovation.utils.HttpUtils.getEnvInfo;
import static org.tensorflow.demo.FarmCameraConnectionFragment.collectNumberHandler;
import static org.tensorflow.demo.FarmDetectorActivity.type1Count;
import static org.tensorflow.demo.FarmDetectorActivity.type2Count;
import static org.tensorflow.demo.FarmDetectorActivity.type3Count;

/**
 * Author by luolu, Date on 2018/8/16.
 * COMPANY：InnovationAI
 */

public class InsureDataProcessor {

    private static final int MSG_PROCESSOR_ZIP = 101;
    private static final int MSG_PROCESSOR_UPLOAD_INSURE_ONE = 102;
    private static final int MSG_PROCESSOR_UPLOAD_PAY_ONE = 103;
    private static final int MSG_PROCESSOR_UPLOAD_ALL = 104;
    private static final int MSG_PROCESSOR_TEST = 105;
    private static final int MSG_PROCESSOR_UPLOAD_IMAGEONE = 106;

    private static final int MSG_UI_PROGRESS_ZIP_IMG = 3;
    private static final int MSG_UI_PROGRESS_ZIP_VIDEO = 33;
    private static final int MSG_UI_PROGRESS_ZIP_VIDEO_UPLOAD = 47;
    private static final int MSG_UI_FINISH_ZIP_IMG_FAILED = 7;
    private static final int MSG_UI_FINISH_ZIP_VIDEO_FAILED = 8;
    private static final int MSG_UI_FINISH_UPLOAD_IMG_ONE_FAILED = 9;
    private static final int MSG_UI_FINISH_UPLOAD_IMG_ONE_SUCCESS = 10;
    private static final int MSG_UI_PROGRESS_UPLOAD_IMG_ONE = 12;
    private static final int MSG_UI_PROGRESS_UPLOAD_ALL = 14;
    private static final int MSG_UI_FINISH_UPLOAD_ALL = 15;
    private static final int MSG_UI_FINISH_NOZIP = 16;
    private static final int MSG_UI_FINISH_BUILD = 21;
    private static final int MSG_UI_FINISH_ZIP_FILE_NULL = 22;
    private final Logger mLogger = new Logger(InsureDataProcessor.class.getSimpleName());
    private static InsureDataProcessor sInstance;
    private final Context mContext;
    private final DatabaseHelper databaseHelper;
    private Activity mActivity = null;
    private ProgressDialog mProgressDialog;
    private InsureDialog mInsureDialog = null;
    private final Handler mProcessorHandler_new;
    private final Handler mUiHandler_new;
    private ReviewImageDialog mReviewDialogImage = null;
    private ReviewVideoDialog mReviewDialogVideo = null;
    private Gson gson;
    private ToubaoUploadBean toubaoUploadBean;
    private ResultBean resultBean;

    public static InsureDataProcessor getInstance(Context context) {
        if (sInstance == null) {
            synchronized (InsureDataProcessor.class) {
                if (sInstance == null) {
                    sInstance = new InsureDataProcessor(context);
                }
            }
        }
        return sInstance;
    }

    public InsureDataProcessor(Context context) {
        mContext = FarmAppConfig.getApplication();
        HandlerThread mProcessorThread = new HandlerThread("processor-thread");
        mProcessorThread.start();
        mProcessorHandler_new = new ProcessorHandler_new(mProcessorThread.getLooper());
        mUiHandler_new = new UiHandler_new(Looper.getMainLooper());
        gson = new Gson();
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public void handleMediaResource_build(final Activity activity) {
        LocationManager.getInstance(activity).startLocation();
        mActivity = activity;
        initDialogs(activity);
    }

    public void handleMediaResource_destroy() {
        destroyDialogs();
    }

    private void showProgressDialog(Activity activity) {
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIcon(R.drawable.farm_cowface);
        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "确定", mPOSITIVEClickListener);
        mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消", mNEGATIVEClickListener);
        mProgressDialog.setMessage("开始处理......");
        mProgressDialog.show();
        Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
        if (positive != null) {
            positive.setVisibility(View.GONE);
        }
        Button negative = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
        if (negative != null) {
            negative.setVisibility(View.GONE);
        }
    }

    private void saveLibId(String id) {
        SharedPreferences idinfo = mActivity.getSharedPreferences(
                Utils.LIBIDINFO_SHAREFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = idinfo.edit();
        editor.putString("libid", id);
        editor.apply();
    }

    private String getLibId() {
        //读取用户信息
        SharedPreferences pref_user = mActivity.getSharedPreferences(Utils.LIBIDINFO_SHAREFILE, Context.MODE_PRIVATE);
        return pref_user.getString("libid", "");
    }

    private void initDialogs(final Activity activity) {
        updateInsureDialog(activity);
    }

    private void destroyDialogs() {
        if (mInsureDialog != null) {
            mInsureDialog.dismiss();
        }
        mInsureDialog = null;
        if (mReviewDialogImage != null) {
            mReviewDialogImage.dismiss();
        }
        mReviewDialogImage = null;
        if (mReviewDialogVideo != null) {
            mReviewDialogVideo.dismiss();
        }
        mReviewDialogVideo = null;
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
    }

    private void initReviewDialog_image(final Activity activity) {
        String imageDri = "";
        if (FarmGlobal.model == Model.BUILD.value()) {
            imageDri = FarmGlobal.mediaInsureItem.getImageDir();///storage/emulated/0/innovation/animal/投保/Current/图片
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            imageDri = FarmGlobal.mediaPayItem.getImageDir();///storage/emulated/0/innovation/animal/理赔/Current/图片
        }
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        final View conView = layoutInflater.inflate(R.layout.farm_review_dialog_layout, null);
        mReviewDialogImage = new ReviewImageDialog(activity, conView, imageDri);
        mReviewDialogImage.setTitle(R.string.dialog_title);
        mReviewDialogImage.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mReviewDialogImage.dismiss();
                updateInsureDialog(activity);
            }
            return false;
        });

    }

    private void initReviewDialog_video(final Activity activity) {
        String imageDri = "";
        if (FarmGlobal.model == Model.BUILD.value()) {
            imageDri = FarmGlobal.mediaInsureItem.getVideoDir();///storage/emulated/0/innovation/animal/投保/Current/视频
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            imageDri = FarmGlobal.mediaPayItem.getVideoDir();///storage/emulated/0/innovation/animal/理赔/Current/视频
        }
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        final View conView = layoutInflater.inflate(R.layout.farm_review_dialog_layout, null);
        mReviewDialogVideo = new ReviewVideoDialog(activity, conView, imageDri);
        mReviewDialogVideo.setTitle(R.string.dialog_title);
        mReviewDialogVideo.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mReviewDialogVideo.dismiss();
                updateInsureDialog(activity);
            }
            return false;
        });
    }

    private void initInsureDialog(final Activity activity) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        final View conView = layoutInflater.inflate(R.layout.farm_insure_dialog_layout, null); //3个角度加载
        mInsureDialog = new InsureDialog(activity, conView);
        final EditText meditText;
        meditText = conView.findViewById(R.id.insure_number);
        mInsureDialog.setTitle(R.string.dialog_title);

        View.OnClickListener listener_abort = v -> {
            Log.i("initInsureDialog:", "listener_abort");

/*            String pignum = meditText.getText().toString().trim();
            if (pignum.length() > 0) {
                saveLibId(pignum);
            }*/

            mInsureDialog.dismiss();

            Intent intent = new Intent(activity, FarmDetectorActivity.class);
            intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
            intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
            activity.startActivity(intent);


            reInitCurrentDir();
            collectNumberHandler.sendEmptyMessage(2);
        };

        View.OnClickListener listener_upload_one = v -> {
            Log.i("==uoload=", "上传");

            if (FarmAppConfig.isOfflineMode) {
                // 投保的离线保存处理
                if (mInsureDialog != null && mInsureDialog.isShowing()) {
                    mInsureDialog.dismiss();
                }
                showProgressDialog(activity);
                dialogProcessUploadOneImage();
            } else {
                if(!FarmAppConfig.isNetConnected){
                    Toast.makeText(activity, "断网了，请联网后重试", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 理赔的离线保存处理
                if (mInsureDialog != null && mInsureDialog.isShowing()) {
                    mInsureDialog.dismiss();
                }
                String pignum = meditText.getText().toString().trim();
                if (pignum.length() == 0) {
                    pignum = "110";
                }
                saveLibId(pignum);
                showProgressDialog(activity);
                writeNumnerFile(pignum);
                dialogProcessUploadOneImage();
            }

        };

        boolean isli = PreferencesUtils.getBooleanValue("isli", mContext);
        Log.i("==isli====", "" + isli);
        if (isli) {
            // 离线理赔时的处理
            mInsureDialog.setUploadOneButton("完成", listener_upload_one);
            mInsureDialog.setAbortButton("重新拍摄", listener_abort);
        } else {
            // 非离线理赔时的处理
            mInsureDialog.setUploadOneButton("本次上传", listener_upload_one);
            mInsureDialog.setAbortButton("放弃", listener_abort);
        }


        updateInsureDialog(activity);
    }

    private void updateReviewDialog_image(final Activity activity) {
        if (mReviewDialogImage == null) {
            initReviewDialog_image(activity);
        }
        mReviewDialogImage.updateView();
        mReviewDialogImage.show();
    }

    private void updateReviewDialog_video(final Activity activity) {
        if (mReviewDialogVideo == null) {
            initReviewDialog_video(activity);
        }
        mReviewDialogVideo.updateView();
        mReviewDialogVideo.show();

    }

    private void updateInsureDialog(final Activity activity) {
        if (mInsureDialog == null) {
            initInsureDialog(activity);
        }
        boolean imagehave = testImageHave(); //图片目录是否存在图片文件
        Map<String, String> showMap = getCaptureAngles();//存在角度图片
        String libid = getLibId();
        boolean ziphave = testZipHave(); //是否存在待上传文件
        boolean videohave = testVideoHave(); //是否存在mp4文件
        mInsureDialog.updateView(showMap, imagehave, ziphave, libid, videohave);
        if (type1Count == 0 && type2Count == 0 && type3Count == 0) {
            Log.i("updateInsureDialog:", "type1Count = " + type1Count);
            Log.i("updateInsureDialog:", "type2Count = " + type2Count);
            Log.i("updateInsureDialog:", "type3Count = " + type3Count);
            mInsureDialog.dismiss();
        } else {
            FarmGlobal.VIDEO_PROCESS = false;
            Log.i("%%%%%%%%%%1:", "%%%%%%%%%" + FarmGlobal.VIDEO_PROCESS);
            mInsureDialog.show();
        }
    }

    //判断图片目录下是否已经存在图片文件
    private boolean testImageHave() {
        boolean ifhave = false;
        //获取图片文件
        String imageDri = "";
        if (FarmGlobal.model == Model.BUILD.value()) {
            imageDri = FarmGlobal.mediaInsureItem.getImageDir();///storage/emulated/0/innovation/animal/投保/Current/图片
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            imageDri = FarmGlobal.mediaPayItem.getImageDir();///storage/emulated/0/innovation/animal/理赔/Current/图片
        }
        File imageDir_new = new File(imageDri);//图片目录下的文件
        File[] files_image = imageDir_new.listFiles();
        if (!imageDir_new.exists() || files_image.length == 0) {
            return false;
        }

        File tmpFile;
        boolean imagehave;
        for (File aFiles_image : files_image) {
            tmpFile = aFiles_image;//tmpFile===/storage/emulated/0/innovation/animal/Current/图片/1
            String abspath = tmpFile.getAbsolutePath();
            imagehave = testFileHave(abspath);
            if (!imagehave) {
                continue;
            } else {
                return true;
            }
        }
        return ifhave;

    }

    //判断图片目录下是否已经存在图片文件
    private boolean testVideoHave() {
        //获取图片文件
        String videoDri = "";
        if (FarmGlobal.model == Model.BUILD.value()) {
            videoDri = FarmGlobal.mediaInsureItem.getVideoDir();///storage/emulated/0/innovation/animal/投保/Current/视频
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            videoDri = FarmGlobal.mediaPayItem.getVideoDir();///storage/emulated/0/innovation/animal/理赔/Current/视频
        }

        List<String> list = FileUtils.GetFiles(videoDri, FarmGlobal.VIDEO_MP4, true);
        return (list != null) && (list.size() > 0);
    }

    //判断图片目录下是否已经存在Zip文件
    private boolean testZipHave() {
        //投保目录
        String zipimageDri = FarmGlobal.mediaInsureItem.getZipImageDir();///storage/emulated/0/innovation/animal/投保/ZipImge
        List<String> list_image = FileUtils.GetFiles(zipimageDri, "zip", true);
        if ((list_image != null) && (list_image.size() > 0)) {
            return true;
        }

        String zipvideoDri = FarmGlobal.mediaInsureItem.getZipVideoDir();///storage/emulated/0/innovation/animal/投保/ZipVideo
        List<String> list_video = FileUtils.GetFiles(zipvideoDri, "zip", true);
        return (list_video != null) && (list_video.size() > 0);

    }

    //判断指定目录下是否已经存在指定类型的文件
    private boolean testFileHave(String filePath) {
        File file_parent = new File(filePath);
        List<String> list_all = FileUtils.GetFiles(filePath, FarmGlobal.IMAGE_JPEG, true);
        return list_all != null && (!file_parent.exists() || list_all.size() == 0);
    }

    //判断指定目录下是否已经存在指定类型的文件
    private int testFileHaveCount(String filePath) {
        File file_parent = new File(filePath);
        List<String> list_all = FileUtils.GetFiles(filePath, FarmGlobal.IMAGE_JPEG, true);
        if (list_all == null){
            return 0;}
        if (!file_parent.exists()) {
            return 0;
        }
        return list_all.size();
    }

    //获得Dialog显示框中需要显示的角度图(已经捕获的角度图片)
    private Map<String, String> getCaptureAngles() {
        //ArrayList<HashMap<String,String>> missArray = new ArrayList<HashMap<String,String>>();
        Map<String, String> showMap = new TreeMap<>();//TreeMap方式创建可以对map进行升序排序
        //获取图片文件
        String imageDri = "";
        if (FarmGlobal.model == Model.BUILD.value()){
            imageDri = FarmGlobal.mediaInsureItem.getImageDir();}///storage/emulated/0/innovation/animal/投保/Current/图片
        else if (FarmGlobal.model == Model.VERIFY.value()){
            imageDri = FarmGlobal.mediaPayItem.getImageDir();}///storage/emulated/0/innovation/animal/理赔/Current/图片
        File imageDir_new = new File(imageDri);//图片目录下的文件
        File[] files_image = imageDir_new.listFiles();
        if (!imageDir_new.exists() || files_image.length == 0) {
            return showMap;
        }
        //角度类型图片不完整，提示 ，需加上，测试阶段暂不加（防止角度缺失，始终不上传图像）
        ArrayList<Integer> typelist = new ArrayList<>();
        typelist.add(1);
        typelist.add(2);
        typelist.add(3);
        typelist.add(4);
        File tmpFile;
        String tmptype;
        boolean ifneed;
        int imagecount;
        for (int i = 0; i < files_image.length; i++) {
            tmptype = i + "";
            ifneed = typelist.contains(i);
            if (!ifneed) {//不是要显示的角度
                continue;
            }
            tmpFile = files_image[i];//tmpFile===/storage/emulated/0/innovation/animal/Current/图片/1
            String abspath = tmpFile.getAbsolutePath();
            imagecount = testFileHaveCount(abspath);
            showMap.put(tmptype, imagecount + "");
        }
        return showMap;
    }

    //重新初始化Current文件
    private void reInitCurrentDir() {
        Log.i("reInitCurrentDir:", "重新初始化Current文件");
        if (FarmGlobal.model == Model.BUILD.value()) {
            FarmGlobal.mediaInsureItem.currentDel();
            FarmGlobal.mediaInsureItem.currentInit();
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            FarmGlobal.mediaPayItem.currentDel();
            FarmGlobal.mediaPayItem.currentInit();
        }
    }

    private void writeNumnerFile(String number) {
        File file_num = null;
        if (FarmGlobal.model == Model.BUILD.value()) {
            file_num = FarmGlobal.mediaInsureItem.getNumberFile();
            if (file_num.exists()) {
                FileUtils.deleteFile(file_num);
                file_num = FarmGlobal.mediaInsureItem.getNumberFile();
            }
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            file_num = FarmGlobal.mediaPayItem.getNumberFile();
            if (file_num.exists()) {
                FileUtils.deleteFile(file_num);
                file_num = FarmGlobal.mediaPayItem.getNumberFile();
            }
        }
        if (file_num != null) {
            String str_num = file_num.getAbsolutePath();
            FileUtils.saveInfoToTxtFile(str_num, number);
        }
    }

    private void UploadOneInsure() {
        String zipimageDir = FarmGlobal.mediaInsureItem.getZipImageDir();//storage/emulated/0/innovation/animal/投保/ZipImage
        File file_current = new File(zipimageDir);
        File zipFile_image = new File(file_current.getParentFile(), FarmGlobal.ZipFileName + ".zip");
        dialogProcessUploadOneInsure(zipFile_image);
        Log.d("UploadOneInsure", "processUpload_zipImage_one file name = " + zipFile_image.getAbsolutePath());
    }

    //压缩图片和视频为zip文件
    private void dialogProcessZip() {
        Message msg = Message.obtain(mProcessorHandler_new, MSG_PROCESSOR_ZIP);
        mProcessorHandler_new.sendMessage(msg);
    }

    //上传投保图片zip文件
    private void dialogProcessUploadOneImage() {
        Message msg = Message.obtain(mProcessorHandler_new, MSG_PROCESSOR_UPLOAD_IMAGEONE);
        mProcessorHandler_new.sendMessage(msg);
    }

    //上传投保图片zip文件
    private void dialogProcessUploadOneInsure(File file) {
        Message msg = Message.obtain(mProcessorHandler_new, MSG_PROCESSOR_UPLOAD_INSURE_ONE, file);
        mProcessorHandler_new.sendMessage(msg);
    }

    private void dialogProcessUploadAll() {
        Message msg = Message.obtain(mProcessorHandler_new, MSG_PROCESSOR_UPLOAD_ALL);
        mProcessorHandler_new.sendMessage(msg);
    }

    public void transerPayData(String s, String s1, String s2) {

    }

    private class ProcessorHandler_new extends Handler {
        ProcessorHandler_new(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            mLogger.i("ProcessorHandler message: %d, obj: %s", msg.what, msg.obj);
            switch (msg.what) {
                case MSG_PROCESSOR_ZIP:
                    processZip(true);
                    break;
                case MSG_PROCESSOR_UPLOAD_INSURE_ONE:
                    processUploadOneInsure();
                    break;
                case MSG_PROCESSOR_UPLOAD_PAY_ONE:
                    break;
                case MSG_PROCESSOR_UPLOAD_ALL:
                    break;
                case MSG_PROCESSOR_UPLOAD_IMAGEONE:
                    processZip(false);

                    // 投保时处理
                    if (FarmGlobal.model == Model.BUILD.value()) {
                        String zipImageDir = FarmGlobal.mediaInsureItem.getZipImageDir();//storage/emulated/0/innovation/animal/ZipImage
                        File file_current = new File(zipImageDir);
                        String zipVideoDir = FarmGlobal.mediaInsureItem.getZipVideoDir();//storage/emulated/0/innovation/animal/ZipImage
                        File file_video = new File(zipVideoDir);
                        if (FarmAppConfig.isOfflineMode) {
                            // 投保离线完成处理
                            offLineUploadPic(file_current);
                        } else {
                            // 离线理赔完成处理
                            Log.i("file_current", file_current.getParentFile() + "");
                            Log.i("file_video", file_video.getAbsolutePath() + "");
                            File zipFile_image = new File(file_current.getAbsolutePath(), FarmGlobal.ZipFileName + ".zip");
                            File zipFile_video = new File(file_video.getAbsolutePath(), FarmGlobal.ZipFileName + ".zip");
                            if (PreferencesUtils.getBooleanValue("isli", mContext)) {
                                String lipeidate = PreferencesUtils.getStringValue("lipeidate", mContext);
                                int updatepathnum = databaseHelper.updateLiPeiLocalFromzipPath(zipFile_image.getAbsolutePath(), lipeidate);
                                Log.i("updatepath", updatepathnum + "");
                                int lipeirecordernum = databaseHelper.updateLiPeiLocalFromrecordeText("2", PreferencesUtils.getStringValue("lipeidate", mContext));
                                Log.i("=lipeirecordernum===", lipeirecordernum + "");
                                String insurename = "将覆盖已录入的理赔牲畜信息，确定重新录入？";
                                int lipeirecordermsg = databaseHelper.updateLiPeiLocalFromrecordeMsg(insurename, PreferencesUtils.getStringValue("lipeidate", mContext));
                                Log.i("=lipeirecordernum===", lipeirecordermsg + "");

                                Log.i("videopath", zipFile_video.getAbsolutePath());
                                int videocount = databaseHelper.updateLiPeiLocalFromVideozipPath(zipFile_video.getAbsolutePath(), lipeidate);
                                Log.i("upvideocount", videocount + "条");


                                FarmDetectorActivity detectorActivity = (FarmDetectorActivity) mActivity;
                                detectorActivity.finish();
                            } else {
                                // 理赔/投保在线上传处理
                                processUploadOneInsure();
                            }

                            Log.d("MediaProcess.java", "processUpload_zipImage_one file name = " + zipFile_image.getAbsolutePath());
                        }
                    }
                    break;
                case MSG_PROCESSOR_TEST:
                    break;
                    default:
            }
        }

        private void publishProgress(int what) {
            mUiHandler_new.sendEmptyMessage(what);
        }

        private void publishProgress(int model, int status) {
            Message msg = Message.obtain(mUiHandler_new, MSG_UI_FINISH_UPLOAD_IMG_ONE_FAILED, model, status);
            mUiHandler_new.sendMessage(msg);
        }

        private void publishProgress(int what, int model, int status, Object obj) {
            Message msg = Message.obtain(mUiHandler_new, what, model, status, obj);
            mUiHandler_new.sendMessage(msg);
        }

        //压缩图片文件
        private void processZip(boolean ifCloseDialog) {
            File file_num = null;
            String namepre = "";
            String imageDri = "";
            String zipimageDri = "";
            String videoDri = "";
            String zipvideoDri = "";

            // 等待对话框显示开始
            publishProgress(MSG_UI_PROGRESS_ZIP_IMG);//"压缩图片请等待";
            if (FarmGlobal.model == Model.BUILD.value()) {
                //获取编号文件
                file_num = FarmGlobal.mediaInsureItem.getNumberFile();
                namepre = FarmGlobal.mediaInsureItem.getZipFileName();
                imageDri = FarmGlobal.mediaInsureItem.getImageDir();//storage/emulated/0/innovation/animal/20180227//File detectDir = new File(item.getDetectedDir(mContext));
                zipimageDri = FarmGlobal.mediaInsureItem.getZipImageDir();//storage/emulated/0/innovation/animal/ZipImage
                videoDri = FarmGlobal.mediaInsureItem.getVideoDir();
                FarmGlobal.mediaInsureItem.getZipVideoDir();//storage/emulated/0/innovation/animal/ZipVideo
                zipvideoDri = FarmGlobal.mediaInsureItem.getZipVideoDir();
            }
            //获取图片文件
            File imageDir_new = new File(imageDri);//图片目录下的文件
            File[] files_image = imageDir_new.listFiles();
            if (files_image == null) {
                Log.i("imageFile==", "文件不存在");
                publishProgress(MSG_UI_FINISH_ZIP_FILE_NULL);//"文件不存在
                return;
            }
            if (files_image.length == 0) {
                Log.i("imageFile==", "文件不存在....");
                publishProgress(MSG_UI_FINISH_ZIP_FILE_NULL);//"文件不存在
                return;
            }

            // 4. zip recognized image
            //加入编号文件
            File[] fs_image = new File[files_image.length + 1];
            for (int i = 0; i < files_image.length; i++) {
                fs_image[i] = files_image[i];
            }
            fs_image[files_image.length] = file_num;

            //打包图片文件
            File file_current = new File(zipimageDri);
            File zipFile_image = new File(file_current, namepre + ".zip");
            ZipUtil.zipFiles(fs_image, zipFile_image);
            if (!zipFile_image.exists()) {
                publishProgress(MSG_UI_FINISH_ZIP_IMG_FAILED);//"压缩图片出错，请重新录制";
                reInitCurrentDir();
                return;
            }
            String touBaoVieoFlag = PreferencesUtils.getStringValue(FarmAppConfig.touBaoVieoFlag, mContext);
            String liPeiVieoFlag = PreferencesUtils.getStringValue(FarmAppConfig.liPeiVieoFlag, mContext);
            Log.i("imageFile==", zipFile_image.getAbsolutePath());
            // TODO: 2018/8/15 By:LuoLu  zip video
            //  if (FarmGlobal.UPLOAD_VIDEO_FLAG == true) {
            if ("1".equals(touBaoVieoFlag) || "1".equals(liPeiVieoFlag)) {
                publishProgress(MSG_UI_PROGRESS_ZIP_VIDEO);
                File videoDir_new = new File(videoDri);//视频目录下的文件
                File[] files_video = videoDir_new.listFiles();

                //20180425
                if (files_video == null) {
                    publishProgress(MSG_UI_FINISH_ZIP_FILE_NULL);
                    return;
                }
                if (files_video.length == 0) {
                    publishProgress(MSG_UI_FINISH_ZIP_FILE_NULL);
                    return;
                }

                File[] fs_video = new File[files_video.length + 1];
                for (int i = 0; i < files_video.length; i++) {
                    fs_video[i] = files_video[i];
                }
                fs_video[files_video.length] = file_num;

                file_current = new File(zipvideoDri);
                File zipFile_video = new File(file_current, FarmGlobal.ZipFileName + ".zip");
                Log.i("zipFile_video==", zipFile_video.getAbsolutePath());
                ZipUtil.zipFiles(fs_video, zipFile_video);
                if (!zipFile_video.exists()) {
                    publishProgress(MSG_UI_FINISH_ZIP_VIDEO_FAILED);
                    reInitCurrentDir();
                    return;
                }
            }

            File videoDir_new = new File(videoDri);//当前视频目录下的文件
            File imageDri_new = new File(imageDri);//当前图片目录下的文件
            // boolean deleteCurrentVideoResult = FileUtils.deleteFile(videoDir_new);
            // boolean deleteCurrentImageResult = FileUtils.deleteFile(imageDri_new);
//            if (deleteCurrentVideoResult == true) {
//                mLogger.i("当前视频文件夹删除成功！");
//            }
//            if (deleteCurrentImageResult == true) {
//                mLogger.i("当前图片文件夹删除成功！");
//            }

            reInitCurrentDir();
            if (FarmGlobal.model == Model.BUILD.value()) {
                if (ifCloseDialog){
                    mProgressDialog.dismiss();}
            }
        }

        public void processUploadOneInsure() {
            int model = FarmGlobal.model;
            publishProgress(MSG_UI_PROGRESS_UPLOAD_IMG_ONE);
            // TODO: 2018/8/18 By:LuoLu  清空当前图片文件夹
            String imageDri = "";
            String videoDri = "";
            if (FarmGlobal.model == Model.BUILD.value()) {
                //获取编号文件
                imageDri = FarmGlobal.mediaInsureItem.getImageDir();
                videoDri = FarmGlobal.mediaInsureItem.getVideoDir();
                FarmGlobal.mediaInsureItem.getZipVideoDir();
            }
            File videoDir_new = new File(videoDri);//当前视频目录下的文件
            File imageDri_new = new File(imageDri);//当前图片目录下的文件
            boolean deleteCurrentVideoResult = FileUtils.deleteFile(videoDir_new);
            boolean deleteCurrentImageResult = FileUtils.deleteFile(imageDri_new);
            if (deleteCurrentVideoResult == true) {
                mLogger.i("当前投保视频文件夹删除成功！");
            }
            if (deleteCurrentImageResult == true) {
                mLogger.i("当前投保图片文件夹删除成功！");
            }


            // 投保图片上传处理
            // TODO: 2018/8/16 By:LuoLu  投保建库，上传图片包
            String zipImageDir = FarmGlobal.mediaInsureItem.getZipImageDir();
            File file_zip = new File(zipImageDir);
            String fname_image = FarmGlobal.ZipFileName + ".zip";
            Log.i("toubao fname_image:", fname_image);
            File zipFile_image2 = new File(file_zip, fname_image); //要上传的文件
            if (!zipFile_image2.exists()) {
                Log.i("zipFile_image2:", "压缩图片文件夹不存在！！");
                insuranceDataHandler.sendEmptyMessage(401);
                return;
            }

            //读取用户信息
            SharedPreferences pref_user = mActivity.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
            // TODO: 2018/8/20 By:LuoLu
            if (pref_user == null) {
                insuranceDataHandler.sendEmptyMessage(400);
            } else {
                int userId = pref_user.getInt("uid", 0);
                mLogger.i("baodanNO: " + getStringTouboaExtra);
                upload_zipImage(model, zipFile_image2, userId, getStringTouboaExtra);
            }

            // 是否传视频设置
            // if (FarmGlobal.UPLOAD_VIDEO_FLAG == true) {
            String touBaoVieoFlag = PreferencesUtils.getStringValue(FarmAppConfig.touBaoVieoFlag, mContext);
            if ("1".equals(touBaoVieoFlag)) {
                // 上传视频处理
                // 投保建库上传视频包
                String zipVideoDir = FarmGlobal.mediaInsureItem.getZipVideoDir();
                Log.i("zipVideoDir:", zipVideoDir);
                File file_zipVideo = new File(zipVideoDir);
                Log.i("fname_video:", fname_image);
                File zipFile_video2 = new File(file_zipVideo, fname_image); //要上传的视频文件
                if (zipFile_video2.exists()) {
                    //读编号信息
                    String fname_num = "number.txt";
                    String contentVideo = null;
                    try {
                        contentVideo = FileUtils.getZipFileContent(zipFile_video2, fname_num);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!TextUtils.isEmpty(contentVideo)) {
                        if (pref_user != null) {
                            int userId = pref_user.getInt("uid", 0);
                            // 上传视频，不判断上传结果是否正常
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    upload_zipVideo(model, toubaoUploadBean.getData().getLibId(), zipFile_video2, userId, getStringTouboaExtra);
                                }
                            }.start();

                        }
                    }
                }
            }
        }

        private void upload_zipImage(int model, File zipFile_image, int uid, String libMum) {
            String gps = null;
            try {
                TreeMap<String, String> treeMap = new TreeMap<>();
                treeMap.put(Utils.UploadNew.USERID, uid + "");
                treeMap.put(Utils.UploadNew.LIB_NUM, libMum);
                treeMap.put(Utils.UploadNew.TYPE, model + "");
                treeMap.put(Utils.UploadNew.LIBD_SOURCE, 1 + "");
                treeMap.put(Utils.UploadNew.LIB_ENVINFO, getEnvInfo(mActivity, gps));
                treeMap.put(Utils.UploadNew.COLLECT_TIME, FarmAppConfig.during/1000+"");

                MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
                FormBody.Builder builder = new FormBody.Builder();
                for (TreeMap.Entry<String, String> entry : treeMap.entrySet()) {
                    requestBody.addFormDataPart(entry.getKey(), entry.getValue());
                }
                requestBody.addFormDataPart("zipFile", zipFile_image.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), zipFile_image));
                // TODO: 2018/8/4

                //Log.e("投保图片上传接口请求报文：", treeMap.toString() + "\n请求地址：" + INSURE_IMAGE_UPLOAD);
                String responseInsureImageUpload = HttpUtils.post(INSURE_IMAGE_UPLOAD, requestBody.build());
                resultBean = null;
                if (responseInsureImageUpload != null) {
                    publishProgress(MSG_UI_FINISH_UPLOAD_IMG_ONE_SUCCESS);
                    //mLogger.e("投保图片文件上传接口返回：\n" + INSURE_IMAGE_UPLOAD + "\nresponseInsureImageUpload:" + responseInsureImageUpload);
                    resultBean = gson.fromJson(responseInsureImageUpload, ResultBean.class);
                    if (resultBean.getStatus() == 1) {
                        //成功
                        mLogger.i("responseInsureImageUpload data:" + resultBean.getData().toString());
                        toubaoUploadBean = gson.fromJson(responseInsureImageUpload, ToubaoUploadBean.class);
                        mLogger.i("投保图片 libID:" + toubaoUploadBean.getData().getLibId());
                        AddAnimalActivity.addAnimalLibID = String.valueOf(toubaoUploadBean.getData().getLibId());
                        insuranceDataHandler.sendEmptyMessage(18);
                    } else if (resultBean.getStatus() == 0) {
                        //上传图片模糊等异常处理
                        insuranceDataHandler.sendEmptyMessage(19);
                    } else {
                        //异常时处理
                        insuranceDataHandler.sendEmptyMessage(42);
                    }

                } else {
                    //异常时处理
                    insuranceDataHandler.sendEmptyMessage(42);
                }
            } catch (Exception e) {
                //异常时处理
                insuranceDataHandler.sendEmptyMessage(42);
                e.printStackTrace();
                AVOSCloudUtils.saveErrorMessage(e, InsureDataProcessor.class.getSimpleName());
            }
        }

        // TODO: 2018/8/15 By:LuoLu  upload video
        private UploadObject upload_zipVideo(int model, int lib_id, File videoZipFile, int uid, String libMum) {
            UploadObject imgResp = HttpUtils.uploadVideo(mContext, model, lib_id, videoZipFile, uid, libMum);
            if (imgResp == null || imgResp.status != HttpRespObject.STATUS_OK) {
                int status = imgResp == null ? -1 : imgResp.status;
                mLogger.e("upload video failed, status: %d", status);
                return imgResp;
            }
            boolean result = FileUtils.deleteFile(videoZipFile);
            if (result == true) {
                mLogger.i("本地视频打包文件删除成功！！");
            }
            return imgResp;
        }

        /**
         * 离线上传图片
         */
        private void offLineUploadPic(File file) {
            File zipFile_image = new File(file.getAbsoluteFile(), FarmGlobal.ZipFileName + ".zip");
            String insureNoPath = Environment.getExternalStorageDirectory().getPath() + FarmAppConfig.OFFLINE_TEMP_PATH;
            File newDir = new File(insureNoPath);

            FileUtils.deleteFileAll(newDir);
            newDir.mkdirs();
//            mLogger.i("离线文件路径：" + newDir);

            // 取得自动生成的zip文件的文件名
            String[] pathArray = zipFile_image.getPath().split("/");
            String localImageFielName = pathArray[pathArray.length - 1];
            //图片文件名称
            String imagePathFile = insureNoPath + localImageFielName;

//            mLogger.i("new离线文件路径：" + imagePathFile);
            FileUtils.moveFile(zipFile_image.getPath(), imagePathFile);//移动文件

            // 离线模式时为对话框正常显示提供给UI的假数据
            UploadObject imgResp = new UploadObject();
            imgResp.animalInfo = "{\"lib_id\":0,\"lib_envinfo\":{\"imei\":\"\",\"gps\":\"\"},\"lib_createtime\":\"\"}";
            Intent intent = new Intent(mActivity, AddAnimalActivity.class);
            mActivity.startActivity(intent);
            mProgressDialog.dismiss();
            mActivity.finish();
//            publishProgress(MSG_UI_FINISH_BUILD, FarmGlobal.model, 0, imgResp.animalInfo);
        }
    }

    private static boolean emptyPigInfo(String pigInfo) {
        if (TextUtils.isEmpty(pigInfo)) {
            return true;
        }
        JSONObject jo = null;
        try {
            jo = new JSONObject(pigInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo == null;
    }

    private class UiHandler_new extends Handler {
        UiHandler_new(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            mLogger.i("UiHandler message: %d", msg.what);
            final String showMessage;
            switch (msg.what) {
                case MSG_UI_PROGRESS_ZIP_IMG:
                    showMessage = "压缩图片，请等待......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_PROGRESS_ZIP_VIDEO:
                    showMessage = "压缩视频，请等待......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_PROGRESS_ZIP_VIDEO_UPLOAD:
                    showMessage = "正在上传视频，请等待......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_FINISH_ZIP_IMG_FAILED:
                    showMessage = "压缩图片出错，请重新录制";
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_FINISH_ZIP_VIDEO_FAILED:
                    showMessage = "压缩视频出错，请重新录制";
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_FINISH_ZIP_FILE_NULL:
                    showMessage = "待压缩的文件不存在，请重新录制";
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_PROGRESS_UPLOAD_IMG_ONE:
                    showMessage = "正在上传建库的图片......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_FINISH_NOZIP:
                    showMessage = "没有需要上传的文件......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_FINISH_UPLOAD_IMG_ONE_FAILED:
                    showMessage = createUploadImgFailedMsg(msg.arg1, msg.arg2);
                    updateProgressDialogTwoButton(showMessage);
                    break;
                case MSG_UI_FINISH_UPLOAD_IMG_ONE_SUCCESS:
                    showMessage = "图片上传成功......";
                    mProgressDialog.setMessage(showMessage);
                    break;
                case MSG_UI_PROGRESS_UPLOAD_ALL:
                    showMessage = "开始上传全部文件......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_FINISH_UPLOAD_ALL:
                    showMessage = "全部文件上传完成......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialogOneButton(showMessage);
                    break;
                default:
                    showMessage = ".....";
                    mProgressDialog.setMessage(showMessage);
            }
        }

        private void updateProgressDialog(String showMessage) {
            Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setVisibility(View.GONE);
            }
            Button negative = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
            if (negative != null) {
                negative.setVisibility(View.GONE);
            }
            mProgressDialog.setMessage(showMessage);
        }

        private void updateProgressDialogOneButton(String showMessage) {
            Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setVisibility(View.VISIBLE);
            }
            mProgressDialog.setMessage(showMessage);
        }

        private void updateProgressDialogTwoButton(String showMessage) {
            Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
            positive.setText("重试");
            positive.setOnClickListener(view -> {
                if (FarmGlobal.model == Model.BUILD.value()) {
                    UploadOneInsure();
                }
            });
            if (positive != null) {
                positive.setVisibility(View.VISIBLE);
            }
            Button negative = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
            negative.setText("重新采集");
            negative.setOnClickListener(view -> {

                mProgressDialog.dismiss();

                Intent intent = new Intent(mActivity, FarmDetectorActivity.class);
                intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                mActivity.startActivity(intent);
                mActivity.finish();

            });
            if (negative != null) {
                negative.setVisibility(View.VISIBLE);
            }
            mProgressDialog.setMessage(showMessage);
        }


        private String createUploadImgFailedMsg(int model, int status) {
            String msg = model == Model.BUILD.value() ? "投保建库失败！" : "理赔建库失败！";
            switch (status) {
                case RespObject.STATUS_NET_ERR:
                    msg += "网络连接异常，请重试。";
                    break;
                case RespObject.STATUS_101:
                    msg += "上传失败，参数错误";
                    break;
                case RespObject.STATUS_102:
                    msg += "理赔失败，未找相关信息，请投保";
                    break;
                case RespObject.STATUS_103:
                    msg += "保存数据失败";
                    break;
                case RespObject.STATUS_104:
                    msg += "请登录后再拍摄视频";
                    break;
                case RespObject.STATUS_105:
                    msg += "返回数据为空";
                    break;
                case RespObject.STATUS_106:
                    msg += "操作不成功";
                    break;
                default:
                    msg += "";
            }
            return msg;
        }
    }

    private final DialogInterface.OnClickListener mPOSITIVEClickListener = (dialog, which) -> {
        dialog.dismiss();
        initDialogs(mActivity);
    };

    private final DialogInterface.OnClickListener mNEGATIVEClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            mProgressDialog = null;
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler insuranceDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 18:
                    closeMProgressDialog();
                    if (toubaoUploadBean.getData().getResultFlag() == 1) {
                        final ToubaoResultDialog dialogToubaoUploadResult = new ToubaoResultDialog(mActivity);
                        View.OnClickListener listenerBtnContinue = v -> {
                            dialogToubaoUploadResult.dismiss();
                            Intent intent = new Intent(mActivity, AddAnimalActivity.class);
                            try {
                                intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                mActivity.startActivity(intent);
                                mActivity.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        };
                        View.OnClickListener listener_ReCollect = v -> {
                            FarmAppConfig.during = 0;
                            dialogToubaoUploadResult.dismiss();
                            Intent intent = new Intent(mActivity, FarmDetectorActivity.class);
                            intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                            mActivity.startActivity(intent);
                            reInitCurrentDir();
                            collectNumberHandler.sendEmptyMessage(2);
                            //mActivity.finish();
                        };

                        dialogToubaoUploadResult.setTitle("验证结果");
                        dialogToubaoUploadResult.setToubaoResultmessage(toubaoUploadBean.getData().getResultMsg()
                                + "\n"
                                + toubaoUploadBean.getData().getResultPic().get(0).getDetail() + "\n");
                        dialogToubaoUploadResult.setImage(toubaoUploadBean.getData().getResultPic().get(0).getPic());
                        dialogToubaoUploadResult.setBtnContinueToubao("继续投保", listenerBtnContinue);
                        dialogToubaoUploadResult.setBtnReCollect("重新拍摄", listener_ReCollect);
                        dialogToubaoUploadResult.show();
                    } else {
                        Intent intent = new Intent(mActivity, AddAnimalActivity.class);
                        try {
                            intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                            intent.putExtra("libid", toubaoUploadBean.getData().getLibId());
                            mActivity.startActivity(intent);
                            mActivity.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case 19:
                    closeMProgressDialog();
                    AlertDialog.Builder builder34 = new AlertDialog.Builder(mActivity)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage((resultBean == null?"网络异常上传失败，请重试。":resultBean.getMsg())+"\n如果网络状况较差，建议使用离线模式。")
                            .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent inten = new Intent(mActivity, FarmDetectorActivity.class);
                                    inten.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                    mActivity.startActivity(inten);
                                    reInitCurrentDir();
                                    collectNumberHandler.sendEmptyMessage(2);
                                    mActivity.finish();
                                }
                            })
                            .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
//                                    Intent intent = new Intent(mActivity, FarmDetectorActivity.class);
////                                    intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
////                                    mActivity.startActivity(intent);
////                                    reInitCurrentDir();
////                                    collectNumberHandler.sendEmptyMessage(2);
////                                    mActivity.finish();

                                    if (FarmGlobal.model == Model.BUILD.value()) {
                                        showProgressDialog(mActivity);
                                        UploadOneInsure();
                                    }
                                }
                            });
                    builder34.setCancelable(false);
                    builder34.show();
                    break;
                case 42:
                    closeMProgressDialog();
                    AlertDialog.Builder builder42 = new AlertDialog.Builder(mActivity)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("网络异常上传失败，请重试。\n如果网络状况较差，建议使用离线模式。")
                            .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mActivity.finish();
                                }
                            })
                            .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    mActivity.finish();
                                    if (FarmGlobal.model == Model.BUILD.value()) {
                                        showProgressDialog(mActivity);
                                        UploadOneInsure();
                                    }
                                }
                            });
                    builder42.setCancelable(false);
                    builder42.show();
                    break;

                case 400:
                    closeMProgressDialog();
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("获取用户ID失败！")
                            .setPositiveButton("确认", (dialog, which) -> mActivity.finish());
                    builder.setCancelable(false);
                    builder.show();
                    break;
                case 401:
                    closeMProgressDialog();
                    AlertDialog.Builder builder401 = new AlertDialog.Builder(mActivity)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("压缩图片文件夹不存在，请重新录制。")
                            .setPositiveButton("确认", (dialog, which) -> mActivity.finish());
                    builder401.setCancelable(false);
                    builder401.show();
                    break;
                case 94:
                    break;
                default:
                    break;
            }

        }
    };

    private void closeMProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
