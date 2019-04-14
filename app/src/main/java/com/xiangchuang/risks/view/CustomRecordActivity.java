package com.xiangchuang.risks.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.PreferencesUtils;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.utils.TimeUtil;
import com.xiangchuang.risks.utils.ToastUtils;

import innovation.location.LocationManager_new;
import innovation.utils.PathUtils;
import innovation.utils.UIUtils;

/**
 * @Author: Lucas.Cui
 * 时   间：2019/03/19
 * 简   述：<功能简述>
 * 自定义录制视频
 */

public class CustomRecordActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "CustomRecordActivity";

    //UI
    private ImageView mRecordControl;
    private SurfaceView surfaceView;
    private FrameLayout framelayout;
    private SurfaceHolder mSurfaceHolder;
    private Chronometer mRecordTime;
    private TextView tv_date, tv_longitude, tv_latitude, tv_position;

    //录像机状态标识
    private int mRecorderState;

    public static final int STATE_INIT = 0;
    public static final int STATE_RECORDING = 1;
    public static final int STATE_PAUSE = 2;
    public static final int REQUEST_RECORDER = 1000;

//    private boolean isPause; //暂停标识

    // 存储文件
    private Camera mCamera;
    private MediaRecorder mediaRecorder;
    private String currentVideoFilePath;
    private String saveVideoPath = "";
    private Camera.Size mVideoSize;
    public static final int PREVIEW_SIZE = 1;
    public static final int PICTURE_SIZE = 2;
    public static final int VIDEO_SIZE = 3;
    public int mIntentRecordTime;

    public static void start(Activity context, int time) {

        Intent intent = new Intent(context, CustomRecordActivity.class);
        intent.putExtra("time", time);
        context.startActivityForResult(intent, REQUEST_RECORDER);
    }

    private MediaRecorder.OnErrorListener OnErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mediaRecorder, int what, int extra) {
            try {
                if (mediaRecorder != null) {
                    mediaRecorder.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //为计时器绑定监听事件
    Chronometer.OnChronometerTickListener onChronometerTickListener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer ch) {
            // 如果从开始计时到现在超过了60s
            if (SystemClock.elapsedRealtime() - ch.getBase() > mIntentRecordTime * 1000) {
                ch.stop();
                stoppingRecord();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        mIntentRecordTime = getIntent().getIntExtra("time", 10);
        XXPermissions.with(CustomRecordActivity.this)
                //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.RECORD_AUDIO) //不指定权限则自动获取清单中的危险权限
                .permission(Permission.CAMERA)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            initView();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        ToastUtils.getInstance().showShort(CustomRecordActivity.this, "获取权限失败");
                        finish();
                    }
                });
    }

    public void initView() {
        framelayout = (FrameLayout) findViewById(R.id.framelayout);
        mRecordControl = (ImageView) findViewById(R.id.record_control);
        mRecordTime = (Chronometer) findViewById(R.id.record_time);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_longitude = (TextView) findViewById(R.id.tv_longitude);
        tv_latitude = (TextView) findViewById(R.id.tv_latitude);
        tv_position = (TextView) findViewById(R.id.tv_position);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String dateString = formatter.format(new Date());
        tv_date.setText(dateString);
        tv_longitude.setText("经度：" + PreferencesUtils.getStringValue(Constants.longitude, AppConfig.getAppContext()));
        tv_latitude.setText("纬度：" + PreferencesUtils.getStringValue(Constants.latitude, AppConfig.getAppContext()));
        tv_position.setText("位置：" + LocationManager_new.getInstance(CustomRecordActivity.this).str_address);
        mRecordControl.setOnClickListener(this);
        surfaceView = new SurfaceView(CustomRecordActivity.this);
        framelayout.addView(surfaceView);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
        params.width = UIUtils.getWidthPixels(this);
        params.height = (int) (4.0 / 3 * UIUtils.getWidthPixels(this));
        surfaceView.setLayoutParams(params);
        //配置SurfaceHolder
        mSurfaceHolder = surfaceView.getHolder();
        // 设置Surface不需要维护自己的缓冲区
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置分辨率
        mSurfaceHolder.setFixedSize(320, 280);
        // 设置该组件不会让屏幕自动关闭
        mSurfaceHolder.setKeepScreenOn(true);
        //回调接口
        mSurfaceHolder.addCallback(mSurfaceCallBack);
        mRecordTime.setFormat("%s/" + TimeUtil.tansTime(mIntentRecordTime * 1000));
        mRecordTime.setOnChronometerTickListener(onChronometerTickListener);
        mRecordTime.requestLayout();
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void initData() {

    }


    private SurfaceHolder.Callback mSurfaceCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            initCamera();
            Log.e(TAG, "============surfaceCreated=========");
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            Log.d(TAG, "============surfaceChanged=========");
            if (mSurfaceHolder.getSurface() == null) {
                return;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "============surfaceDestroyed=========");
            releaseCamera();
        }
    };


    /**
     * 初始化摄像头
     *
     * @throws IOException
     * @author liuzhongjun
     */
    private void initCamera() {

        if (mCamera != null) {
            releaseCamera();
        }

        mCamera = Camera.open();
        if (mCamera == null) {
            ToastUtils.getInstance().showShort(this, "未能获取到相机！");
            return;
        }
        try {
            //将相机与SurfaceHolder绑定
            mCamera.setPreviewDisplay(mSurfaceHolder);
            //配置CameraParams
            configCameraParams();
            //启动相机预览
            mCamera.startPreview();
        } catch (IOException e) {
            //有的手机会因为兼容问题报错，这就需要开发者针对特定机型去做适配了
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }


    /**
     * 设置摄像头为竖屏
     *
     * @author lip
     * @date 2015-3-16
     */
    private void configCameraParams() {
        Camera.Parameters params = mCamera.getParameters();
        //设置相机的横竖屏(竖屏需要旋转90°)
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            params.set("orientation", "portrait");
            mCamera.setDisplayOrientation(90);
        } else {
            params.set("orientation", "landscape");
            mCamera.setDisplayOrientation(0);
        }
        //设置聚焦模式
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        //缩短Recording启动时间
        params.setRecordingHint(true);
        //影像稳定能力
        if (params.isVideoStabilizationSupported())
            params.setVideoStabilization(true);
        mCamera.setParameters(params);
        try {
            setPreviewSize(mCamera, UIUtils.getWidthPixels(this), UIUtils.getHeightPixels(this));
            setPictureSize(mCamera, UIUtils.getWidthPixels(this), UIUtils.getHeightPixels(this));
            Camera.Parameters parameters = mCamera.getParameters();
            mVideoSize = getBestSize(UIUtils.getWidthPixels(this), UIUtils.getHeightPixels(this), parameters.getSupportedVideoSizes(), VIDEO_SIZE);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置预览大小
     *
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    public void setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestSize(expectWidth, expectHeight, parameters.getSupportedPreviewSizes(), PREVIEW_SIZE);
            parameters.setPreviewSize(size.width, size.height);
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置拍摄的照片大小
     *
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    public void setPictureSize(Camera camera, int expectWidth, int expectHeight) {


        try {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestSize(expectWidth, expectHeight, parameters.getSupportedPictureSizes(), PICTURE_SIZE);
            parameters.setPictureSize(size.width, size.height);
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //获取与指定宽高相等或最接近的尺寸
    private Camera.Size getBestSize(float width, float height, List<
            Camera.Size> sizeList, int isPic) {
        Camera.Size bestSize = null;
        float targetRatio = (4.0f / 3);  //目标大小的宽高比
        float minDiff = targetRatio;

        sortList(sizeList); // 根据宽度进行排序
//        for (Camera.Size size : sizeList) {
//            int supportedRatio = (size.width / size.height);
//            AbLogUtil.d("", "系统支持的尺寸 :"+size.width + "* "+size.height +",    比例$supportedRatio");
//        }
        bestSize = sizeList.get(0);
        for (Camera.Size size : sizeList) {
            float supportedRatio = ((float) size.width / size.height);
            if (Math.abs(supportedRatio - targetRatio) < minDiff || bestSize.height < 240) {
                if (isPic == PICTURE_SIZE && size.height < width) continue;
                minDiff = Math.abs(supportedRatio - targetRatio);
                bestSize = size;
                if (isPic == VIDEO_SIZE && minDiff <= 0.01 && bestSize.height >= 480) break;
                if (isPic == PREVIEW_SIZE && minDiff <= 0.01 && bestSize.height >= 640) break;
            }
        }
        Log.d("", "目标尺寸 ：$targetWidth * $targetHeight ，   比例  $targetRatio");
        Log.d("", "最优尺寸 ：${bestSize?.height} * ${bestSize?.width}");

        return bestSize;
    }

    /**
     * 排序
     *
     * @param list
     */
    private static void sortList(List<Camera.Size> list) {
        Collections.sort(list, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size pre, Camera.Size after) {
                if (pre.width > after.width) {
                    return 1;
                } else if (pre.width < after.width) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * 释放摄像头资源
     *
     * @author liuzhongjun
     * @date 2016-2-5
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 开始录制视频
     */
    public boolean startRecord() {

        initCamera();
        //录制视频前必须先解锁Camera
        mCamera.unlock();
        configMediaRecorder();
        try {
            //开始录制
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 停止录制视频
     */
    public void stopRecord() {
        // 设置后不会崩
        if(mediaRecorder != null){
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setPreviewDisplay(null);
            //停止录制
            mediaRecorder.stop();
            mediaRecorder.reset();
            //释放资源
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    /**
     * 点击中间按钮，执行的UI更新操作
     */
    private void refreshControlUI() {
        if (mRecorderState == STATE_INIT) {
            //录像时间计时
            mRecordTime.setBase(SystemClock.elapsedRealtime());
            mRecordTime.start();

            mRecordControl.setImageResource(R.drawable.recordvideo_stop);
            //1s后才能按停止录制按钮
            mRecordControl.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecordControl.setEnabled(true);
                }
            }, 1000);


        } else if (mRecorderState == STATE_RECORDING) {
            if(mRecordTime != null){
                mRecordTime.stop();
            }
        }

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.record_control) {
            if (mRecorderState == STATE_INIT) {
                initRecorder();
            } else if (mRecorderState == STATE_RECORDING) {
                stoppingRecord();
            }
        }

    }

    private void initRecorder() {

        if (getSDPath(getApplicationContext()) == null)
            return;

        //视频文件保存路径，configMediaRecorder方法中会设置
        // 初始化图片保存路径
        String photo_dir = PathUtils.app_data_video;
        if (TextUtils.isEmpty(photo_dir)) {
            ToastUtils.getInstance().showShort(CustomRecordActivity.this, "存储卡不存在");
        } else {
            File file = new File(photo_dir);
            if (!file.exists()) {
                file.mkdir();
            }

            currentVideoFilePath = photo_dir + "/" + getVideoName();
        }

        //开始录制视频
        if (!startRecord())
            return;

        refreshControlUI();

        mRecorderState = STATE_RECORDING;
    }

    private void stoppingRecord() {
        //停止视频录制
        stopRecord();
        //先给Camera加锁后再释放相机
        if(mCamera != null){
            mCamera.lock();
        }

        releaseCamera();

        refreshControlUI();

        //判断是否进行视频合并
        if ("".equals(saveVideoPath)) {
            saveVideoPath = currentVideoFilePath;
        }

        //延迟一秒跳转到播放器，（确保视频合并完成后跳转） TODO 具体的逻辑可根据自己的使用场景跳转
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent();
                intent.putExtra("videoPath", saveVideoPath);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }, 1000);
    }

    /**
     * 配置MediaRecorder()
     */

    private void configMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setOnErrorListener(OnErrorListener);

        //使用SurfaceView预览
        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        //1.设置采集声音
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置采集图像
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //2.设置视频，音频的输出格式 mp4
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //3.设置音频的编码格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //设置图像的编码格式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //设置立体声
//        mediaRecorder.setAudioChannels(2);
        //设置最大录像时间 单位：毫秒
//        mediaRecorder.setMaxDuration(60 * 1000);
        //设置最大录制的大小 单位，字节
//        mediaRecorder.setMaxFileSize(1024 * 1024);
        //音频一秒钟包含多少数据位


        // 这里有点投机取巧的方式，不过证明方法也是不错的
        // 录制出来10S的视频，大概1.2M，清晰度不错，
        // 而且避免了因为手动设置参数导致无法录制的情况
        // 手机一般都有这个格式CamcorderProfile.QUALITY_480P,
        // 因为单单录制480P的视频还是很大的，
        // 所以我们在手动根据预览尺寸配置一下videoBitRate,值越高越大
        // QUALITY_QVGA清晰度一般，不过视频很小，一般10S才几百K
        // 判断有没有这个手机有没有这个参数
        CamcorderProfile profile = null;
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_CIF);
//            mediaRecorder.setProfile(profile);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
//            mediaRecorder.setProfile(profile);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
//            mediaRecorder.setProfile(profile);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA)) {
//            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA));
        } else {
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoEncodingBitRate(600000);
            mediaRecorder.setVideoFrameRate(20);
        }
        mediaRecorder.setAudioEncodingBitRate(44100);

        //设置码率
        mediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024);
        //设置帧率
        mediaRecorder.setVideoFrameRate(20);
//        }
        //设置选择角度，顺时针方向，因为默认是逆向90度的，这样图像就是正常显示了,这里设置的是观看保存后的视频的角度
        mediaRecorder.setOrientationHint(90);
        //设置录像的分辨率
        mediaRecorder.setVideoSize(mVideoSize.width, mVideoSize.height);
        //设置录像视频输出地址
        mediaRecorder.setOutputFile(currentVideoFilePath);
    }

    /**
     * 创建视频文件保存路径
     */
    public String getSDPath(Context context) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(context, "请查看您的SD卡是否存在！", Toast.LENGTH_SHORT).show();
            return null;
        }

        String sdDir = PathUtils.app_data_video;
        File eis = new File(sdDir);
        if (!eis.exists()) {
            eis.mkdir();
        }
        return eis.getPath() + "/" + getVideoName();
    }

    private String getVideoName() {
        return "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
    }


}
