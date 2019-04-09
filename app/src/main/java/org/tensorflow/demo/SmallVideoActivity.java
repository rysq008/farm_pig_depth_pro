package org.tensorflow.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.NavBarUtils;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.OkHttp3Util;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.media.MediaSmalVideoItem;
import innovation.network_status.NetworkUtil;
import innovation.utils.FileUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class SmallVideoActivity extends BaseActivity implements SurfaceHolder.Callback, View.OnTouchListener, BothWayProgressBar.OnProgressEndListener {

    private static final int LISTENER_START = 200;
    private static final String TAG = "MainActivity";
    //预览SurfaceView
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    //底部"按住拍"按钮
    private View mStartButton;
    //进度条
    private BothWayProgressBar mProgressBar;
    //进度条线程
    private Thread mProgressThread;
    //录制视频
    private MediaRecorder mMediaRecorder;
    private SurfaceHolder mSurfaceHolder;
    //屏幕分辨率
    private int videoWidth, videoHeight;
    //判断是否正在录制
    private boolean isRecording;
    //段视频保存的目录
    private File mTargetFile;
    //当前进度/时间
    private float mProgress;
    //录制最大时间
    public static final int MAX_TIME = 10;
    //是否上滑取消
    private boolean isCancel;
    //手势处理, 主要用于变焦 (双击放大缩小)
    private GestureDetector mDetector;
    //是否放大
    private boolean isZoomIn = false;

    private MyHandler mHandler;
    private TextView mTvTip;
    private boolean isRunning;

    private String lipeiId = "";
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        setContentView(R.layout.activity_small_video);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        lipeiId = getIntent().getStringExtra("lipeiid");
        initView();
    }

    public void initView() {
//        int width = getWindowManager().getDefaultDisplay().getWidth();
////        float height = (getWindowManager().getDefaultDisplay().getHeight())/4*3;
////        if(BuildConfig.DEBUG){
////            Toast.makeText(this, "width = "+width+"---height = "+height, Toast.LENGTH_SHORT).show();
////        }

        if(NavBarUtils.hasNavBar(this)){
            NavBarUtils.hideBottomUIMenu(this);
        }


        videoWidth = 640;
        videoHeight = 480;
        mSurfaceView = (SurfaceView) findViewById(R.id.main_surface_view);

//        mDetector = new GestureDetector(this, new ZoomGestureListener());
        /**
         * 单独处理mSurfaceView的双击事件
         */
//        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                mDetector.onTouchEvent(event);
//                return true;
//            }
//        });

        mSurfaceHolder = mSurfaceView.getHolder();
        //设置屏幕分辨率
        mSurfaceHolder.setFixedSize(videoWidth, videoHeight);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        mStartButton = findViewById(R.id.main_press_control);
        mTvTip = (TextView) findViewById(R.id.main_tv_tip);

//        mStartButton.setOnTouchListener(this);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartButton.setVisibility(View.GONE);
                mProgressBar.setCancel(false);
                //显示上滑取消
                mTvTip.setVisibility(View.GONE);
                // TODO: 2016/10/20 开始录制视频, 进度条开始走
                mProgressBar.setVisibility(View.VISIBLE);
                //开始录制
                Toast.makeText(SmallVideoActivity.this, "开始录制", Toast.LENGTH_SHORT).show();

                int width = getWindowManager().getDefaultDisplay().getWidth();

                Log.e(TAG, "onClickView: width" + width);
                Log.e(TAG, "onClickView: " + (float)width/1000);

                mProgressThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            mProgress = 0;
                            isRunning = true;
                            while (isRunning) {
                                mProgress +=(float)width/1000;
                                mHandler.obtainMessage(0).sendToTarget();
                                Thread.sleep(20);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                mProgressThread.start();
                startRecord();
            }
        });


        //自定义双向进度条
        mProgressBar = (BothWayProgressBar) findViewById(R.
                id.main_progress_bar);
        mProgressBar.setOnProgressEndListener(this);
        mHandler = new MyHandler(this);
        mMediaRecorder = new MediaRecorder();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_small_video;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStartButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // SurfaceView回调
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        startPreView(holder);
    }

    /**
     * 开启预览
     *
     * @param holder
     */
    private void startPreView(SurfaceHolder holder) {
        Log.d(TAG, "startPreView: ");

        if (mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        if (mCamera != null) {
            mCamera.setDisplayOrientation(90);
            try {
                mCamera.setPreviewDisplay(holder);
                Camera.Parameters parameters = mCamera.getParameters();
                //实现Camera自动对焦
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes != null) {
                    for (String mode : focusModes) {
                        mode.contains("continuous-video");
                        parameters.setFocusMode("continuous-video");
                    }
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            Log.d(TAG, "surfaceDestroyed: ");
            //停止预览并释放摄像头资源
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // 进度条结束后的回调方法
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onProgressEndListener() {
        //视频停止录制
        stopRecordSave();
    }

    /**
     * 开始录制
     */
    private void startRecord() {
        if (mMediaRecorder != null) {
            //没有外置存储, 直接停止录制
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return;
            }
            try {
                //mMediaRecorder.reset();
                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);
                //从相机采集视频
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                // 从麦克采集音频信息
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                // TODO: 2016/10/20  设置视频格式
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mMediaRecorder.setVideoSize(videoWidth, videoHeight);
                //每秒的帧数
//                mMediaRecorder.setVideoFrameRate(24);
                //编码格式
//                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

//                getOptimalSize(mCamera.getParameters().getSupportedVideoSizes(), videoWidth, videoHeight);

                // 设置帧频率，然后就清晰了
//                mMediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024 * 100);
                mMediaRecorder.setVideoEncodingBitRate(600000);
                /*File targetDir = Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                mTargetFile = new File(targetDir,
                        SystemClock.currentThreadTimeMillis() + ".mp4");
                mMediaRecorder.setOutputFile(mTargetFile.getAbsolutePath());*/

                Global.mediaSmalVideoItem = new MediaSmalVideoItem(SmallVideoActivity.this);
                Global.VideoSmalVideoFileName = Global.mediaSmalVideoItem.getVideoFileName();

                mMediaRecorder.setOutputFile(Global.VideoSmalVideoFileName);
                mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                //解决录制视频, 播放器横向问题
                mMediaRecorder.setOrientationHint(90);


                mMediaRecorder.prepare();
                //正式录制
                mMediaRecorder.start();
                isRecording = true;
            } catch (Exception e) {
                Log.e(TAG, "异常: " + e.toString());
                e.printStackTrace();
            }

        }
    }


    /**
     * 获取手机相机所支持的分辨率,并取第二位的分辨率为拍摄分辨率
     *
     * @return
     */
    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {
        Camera.Size optimalSize = sizes.get(sizes.size() > 1 ? 1 : 0);
         /*
            注释以下代码原因,在有些手机上, 如华为,
            最佳分辨率拍摄会导致,拍摄失败
            java.lang.RuntimeException: start failed
          */
//        float targetRadio = h / (float) w;
//        float optimalDif = Float.MAX_VALUE; //最匹配的比例
//        int optimalMaxDif = Integer.MAX_VALUE;//最优的最大值差距
//        for (Camera.Size size : sizes) {
//            float newOptimal = size.width / (float) size.height;
//            float newDiff = Math.abs(newOptimal - targetRadio);
//            if (newDiff < optimalDif) { //更好的尺寸
//                optimalDif = newDiff;
//                optimalSize = size;
//                optimalMaxDif = Math.abs(h - size.width);
//            } else if (newDiff == optimalDif) {//更好的尺寸
//                int newOptimalMaxDif = Math.abs(h - size.width);
//                if (newOptimalMaxDif < optimalMaxDif) {
//                    optimalDif = newDiff;
//                    optimalSize = size;
//                    optimalMaxDif = newOptimalMaxDif;
//                }
//            }
//        }
        return optimalSize;
    }

    /**
     * 停止录制 并且保存
     */
    private void stopRecordSave() {
        if (isRecording) {
            isRunning = false;
            stopMedia();
            isRecording = false;
            mTargetFile = new File(Global.VideoSmalVideoFileName);
            //Toast.makeText(this, "视频已经放至" + mTargetFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            if (mTargetFile.exists()) {
                upVideo();
            }else{
                mStartButton.setVisibility(View.VISIBLE);
                Toast.makeText(this, "视频未找到，请重新录制。", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopMedia(){
        if (mMediaRecorder != null) {
            //added by ouyang start
            try {
                //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
                //报错为：RuntimeException:stop failed
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                // TODO: handle exception
                Log.i("MyException", Log.getStackTraceString(e));
            }catch (RuntimeException e) {
                // TODO: handle exception
                Log.i("MyException", Log.getStackTraceString(e));
            }catch (Exception e) {
                // TODO: handle exception
                Log.i("MyException", Log.getStackTraceString(e));
            }
            //added by ouyang end

            mMediaRecorder.release();
            mMediaRecorder = null;
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        }
    }



    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIcon(R.drawable.cowface);
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

    private final DialogInterface.OnClickListener mPOSITIVEClickListener = (dialog, which) -> {
        dialog.dismiss();
        mProgressDialog = null;
    };

    private final DialogInterface.OnClickListener mNEGATIVEClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            mProgressDialog = null;
        }
    };

    //上传视频
    private void upVideo(){
        showProgressDialog();
        Map<String,String> mapbody = new HashMap<>();
        mapbody.put(Constants.lipeiId, lipeiId);

        try {
            OkHttp3Util.uploadPreFile(Constants.ADDPREPAYVIDEO, mTargetFile, "a.mp4", mapbody, null, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("precommit", "onFailure======" + e.getLocalizedMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            showTimeOutDialog();
                        }
                    });
                    AVOSCloudUtils.saveErrorMessage(e,SmallVideoActivity.class.getSimpleName());
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String s = "";
                    if (response.isSuccessful()) {
                        s = response.body().string();
                        Log.e("precommit", "上传--" + s);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(s);
                            int status = jsonObject.getInt("status");
                            String msg = jsonObject.getString("msg");
                            if (-1 == status || 0 == status) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        showTimeOutDialog();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(SmallVideoActivity.this, "上传成功。", Toast.LENGTH_SHORT).show();
                                        boolean result = FileUtils.deleteFile(mTargetFile);
                                        if (result) {
                                            Log.i("yulipeidetete:", "本地图片打包文件删除成功！！");
                                            finish();
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("uploadZipVideo", "uploadZipVideo1: " + e.toString());
                            AVOSCloudUtils.saveErrorMessage(e,SmallVideoActivity.class.getSimpleName());
                        }

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("uploadZipVideo", "uploadZipVideo2: " + e.toString());
        }
    }

    private void showTimeOutDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SmallVideoActivity.this);
        View inflate = View.inflate(this, R.layout.pre_timeout, null);
        TextView timeout_resert = inflate.findViewById(R.id.timeout_resert);
        TextView timeout_cancel = inflate.findViewById(R.id.timeout_cancel);
        dialog.setView(inflate);
        timeout_cancel.setVisibility(View.INVISIBLE);
        AlertDialog dialogcreate = dialog.create();
        dialogcreate.setCanceledOnTouchOutside(false);
        dialogcreate.show();
        timeout_resert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtil.isNetworkConnect(SmallVideoActivity.this)) {
                    Toast.makeText(SmallVideoActivity.this, "断网了，请联网后重试。", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialogcreate.dismiss();
                showProgressDialog();
                upVideo();
            }
        });
        /*timeout_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClickView(View v) {
                dialogcreate.dismiss();
                if ("pre".equals(PreferencesUtils.getStringValue(Constants.fleg, AppConfig.getAppContext()))){
                    startActivity(new Intent(SmallVideoActivity.this, PreparedLiPeiActivity.class));
                }
                finish();
            }
        });*/
    }

    /**
     * 停止录制, 不保存
     */
    private void stopRecordUnSave() {
        if (isRecording) {
            isRunning = false;
            mMediaRecorder.stop();
            isRecording = false;
            mTargetFile = new File(Global.VideoSmalVideoFileName);
            if (mTargetFile.exists()) {
                //不保存直接删掉
                mTargetFile.delete();
            }
        }
    }

    /**
     * 相机变焦
     *
     * @param zoomValue
     */
    public void setZoom(int zoomValue) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.isZoomSupported()) {//判断是否支持
                int maxZoom = parameters.getMaxZoom();
                if (maxZoom == 0) {
                    return;
                }
                if (zoomValue > maxZoom) {
                    zoomValue = maxZoom;
                }
                parameters.setZoom(zoomValue);
                mCamera.setParameters(parameters);
            }
        }

    }


    ///////////////////////////////////////////////////////////////////////////
    // Handler处理
    ///////////////////////////////////////////////////////////////////////////
    private static class MyHandler extends Handler {
        private WeakReference<SmallVideoActivity> mReference;
        private SmallVideoActivity mActivity;

        public MyHandler(SmallVideoActivity activity) {
            mReference = new WeakReference<SmallVideoActivity>(activity);
            mActivity = mReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mActivity.mProgressBar.setProgress(mActivity.mProgress);
                    break;
                default:
                    break;
            }

        }
    }

    /**
     * 触摸事件的触发
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean ret = false;
        int action = event.getAction();
        float ey = event.getY();
        float ex = event.getX();
        //只监听中间的按钮处
        int vW = v.getWidth();
        int left = LISTENER_START;
        int right = vW - LISTENER_START;

        float downY = 0;

        int i = v.getId();
        if (i == R.id.main_press_control) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (ex > left && ex < right) {
                        mProgressBar.setCancel(false);
                        //显示上滑取消
                        mTvTip.setVisibility(View.VISIBLE);
                        mTvTip.setText("↑ 上滑取消");
                        //记录按下的Y坐标
                        downY = ey;
                        // TODO: 2016/10/20 开始录制视频, 进度条开始走
                        mProgressBar.setVisibility(View.VISIBLE);
                        //开始录制
                        Toast.makeText(this, "开始录制", Toast.LENGTH_SHORT).show();


                        mProgressThread = new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    mProgress = 0;
                                    isRunning = true;
                                    while (isRunning) {
                                        mProgress++;
                                        mHandler.obtainMessage(0).sendToTarget();
                                        Thread.sleep(20);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        mProgressThread.start();
                        startRecord();
                        ret = true;
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    if (ex > left && ex < right) {
                        mTvTip.setVisibility(View.INVISIBLE);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        //判断是否为录制结束, 或者为成功录制(时间过短)
                        if (!isCancel) {
                            if (mProgress < 50) {
                                //时间太短不保存
                                //stopRecordUnSave();
                                Toast.makeText(this, "时间太短", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            //停止录制
                            stopRecordSave();
                        } else {
                            //现在是取消状态,不保存
                            stopRecordUnSave();
                            isCancel = false;
                            Toast.makeText(this, "取消录制", Toast.LENGTH_SHORT).show();
                            mProgressBar.setCancel(false);
                        }

                        ret = false;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mProgress > 50) {
                        if (ex > left && ex < right) {
                            float currentY = event.getY();
                            if (downY - currentY > 10) {
                                isCancel = true;
                                mProgressBar.setCancel(true);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }

        } else {
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 变焦手势处理类
    ///////////////////////////////////////////////////////////////////////////
    class ZoomGestureListener extends GestureDetector.SimpleOnGestureListener {
        //双击手势事件
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            super.onDoubleTap(e);
            Log.d(TAG, "onDoubleTap: 双击事件");
            if (mMediaRecorder != null) {
                if (!isZoomIn) {
                    setZoom(20);
                    isZoomIn = true;
                } else {
                    setZoom(0);
                    isZoomIn = false;
                }
            }
            return true;
        }
    }


}
