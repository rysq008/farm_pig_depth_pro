/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xiangchuangtec.luolu.animalcounter.PigAppConfig;
import com.xiangchuangtec.luolu.animalcounter.JPushStatsConfig;
import com.innovation.pig.insurance.R;

import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import innovation.media.MediaInsureItem;
import innovation.media.MediaPayItem;
import innovation.media.MediaProcessor;
import innovation.media.Model;
import innovation.utils.FileUtils;
import innovation.utils.Toast;
import innovation.view.SendView;
import innovation.view.VerticalSeekBar;

import static android.content.ContentValues.TAG;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.timeVideoStart;
import static org.tensorflow.demo.DetectorActivity_pig.tracker;
import static org.tensorflow.demo.DetectorActivity_pig.trackingOverlay;

@SuppressLint("ValidFragment")
public class CameraConnectionFragment_pig extends Fragment implements View.OnClickListener {
    private static final Logger LOGGER = new Logger();

    /**
     * The camera preview size will be chosen to be the smallest frame by pixel size capable of
     * containing a DESIRED_SIZE x DESIRED_SIZE square.
     */
    private static final int MINIMUM_PREVIEW_SIZE = 480;

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final String FRAGMENT_DIALOG = "dialog";

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(
                        final SurfaceTexture texture, final int width, final int height) {
                    Global.FrameWidth = width;
                    Global.FrameHeight = height;
                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(
                        final SurfaceTexture texture, final int width, final int height) {
                    configureTransform(width, height);

                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
                }
            };
    private static Activity activity;
    private CameraCharacteristics characteristics;
    public static TextView textSensorExposureTime;
    private Context context;

    /**
     * Callback for Activities to use to initialize their data once the
     * selected preview size is known.
     */
    public interface ConnectionCallback {
        void onPreviewSizeChosen(Size size, int cameraRotation);
    }

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String cameraId;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private static AutoFitTextureView textureView;

    private static RelativeLayout mReCordLayout;
    /**
     * Button to record video
     */
    /*private TextView testtv;*/
    private static TextView myTest;
    private static TextView mRecordControl;
    private static View mRecordSwitch;
    private TextView mRecordSwitchTxt;
    private View mRecordVerify;
    private TextView mRecordVerifyTxt;
    private static TextView tvNotice;
    //曝光调节
    private VerticalSeekBar seekBar;

    //左脸标识图
    private static ImageView ivLeft;
    //右脸标识图
    private static ImageView ivRight;
    //左脸录制按钮
    private static TextView tvBtnLeft;
    //右脸录制按钮
    @SuppressLint("StaticFieldLeak")
    private static TextView tvBtnRight;
    //全局变量获取当前保险类型
    private int animalType;
    //全局变量判断左右脸否已经采集够了
    private static boolean leftEnough = false;
    private static boolean rightEnough = false;

    //全局定义
    private volatile long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 2000;  // 快速点击间隔

    private static SendView mSendView;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private static CameraCaptureSession captureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice cameraDevice;

    /**
     * The rotation in degrees of the camera sensor from the display.
     */
    private Integer sensorOrientation;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;
    private Size mVideoSize_midia;
    int mVideoSize_midia_width = 0;
    int mVideoSize_midia_height = 0;

    /**
     * MediaRecorder
     */
    private static MediaRecorder mMediaRecorder;

    /**
     * Whether the app is recording video now
     */
    private static boolean mIsRecordingVideo;

    /**
     * The {@link Size} of camera preview.
     */
    private Size previewSize;
    private CameraCharacteristics mCharacteristics;
    private int exposureCompensation = 0;
    private int maxExposureCompensation;
    private int minExposureCompensation;
    private double exposureCompensationStep;

    // private Model mModel = Model.BUILD;

    /**
     * {@link CameraDevice.StateCallback}
     * is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(final CameraDevice cd) {
                    // This method is called when the camera is opened.  We start camera preview here.
                    cameraOpenCloseLock.release();
                    cameraDevice = cd;
                    startPreview();
                    if (null != textureView) {
                        configureTransform(textureView.getWidth(), textureView.getHeight());
                    }
                    boolean mAutoMode = true;
                    if (mAutoMode) {
                        // 首先仍旧需要获得支持的自动模式
                        int[] modes = mCharacteristics.get(
                                CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
                        android.util.Range<Integer> range = mCharacteristics.get(
                                CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                        maxExposureCompensation = range.getUpper().intValue();
                        minExposureCompensation = range.getLower().intValue();
                        Rational step = mCharacteristics.get(
                                CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);
                        exposureCompensationStep = step.doubleValue();
                        if (modes == null || modes.length == 0 ||
                                (modes.length == 1 && modes[0] == CameraCharacteristics.CONTROL_AE_MODE_OFF)) {
                            // 如果不支持自动模式，则使用手动模式
                            mAutoMode = false;
                            try {
                                CaptureRequest.Builder mCaptureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                                CaptureRequest req = mCaptureBuilder.build();

                            } catch (CameraAccessException e) {

                            }

//                             reqBuilder.set(CaptureRequest.CONTROL_AE_MODE,
//                                    CaptureRequest.CONTROL_AE_MODE_OFF);
                        } else {
                            // 如果支持自动模式，则（可以根据闪光灯模式）选择一个自动模式
                            //reqBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                            //       CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        }
                    } else {
                        // 关闭自动模式，使用手动模式
                        //reqBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                        //        CaptureRequest.CONTROL_AE_MODE_OFF);
                    }
                }

                @Override
                public void onDisconnected(final CameraDevice cd) {
                    cameraOpenCloseLock.release();
                    cd.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(final CameraDevice cd, final int error) {
                    cameraOpenCloseLock.release();
                    cd.close();
                    cameraDevice = null;
                    final Activity activity = getActivity();
                    if (null != activity) {
                        activity.finish();
                    }
                }
            };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread backgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler backgroundHandler;

    /**
     * An {@link ImageReader} that handles preview frame capture.
     */
    private ImageReader previewReader;

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder previewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #previewRequestBuilder}
     */
    private CaptureRequest previewRequest;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private final Semaphore cameraOpenCloseLock = new Semaphore(1);

    /**
     * A {@link OnImageAvailableListener} to receive frames as they are available.
     */
    private final OnImageAvailableListener imageListener;

    /**
     * The input size in pixels desired by TensorFlow (width and height of a square bitmap).
     */
    private final Size inputSize;

    /**
     * The layout identifier to inflate for this Fragment.
     */
    private final int layout;


    private final ConnectionCallback cameraConnectionCallback;

    CameraConnectionFragment_pig(
            final ConnectionCallback connectionCallback,
            final OnImageAvailableListener imageListener,
            final int layout, final Size inputSize) {
        this.cameraConnectionCallback = connectionCallback;
        this.imageListener = imageListener;
        this.layout = layout;
        this.inputSize = inputSize;
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    public static void showToast(final String text) {
//        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        LOGGER.e("Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param width   The minimum desired width
     * @param height  The minimum desired height
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    protected static Size chooseOptimalSize(final Size[] choices, final int width, final int height) {
        final int minSize = Math.max(Math.min(width, height), MINIMUM_PREVIEW_SIZE);
        final Size desiredSize = new Size(width, height);

        // Collect the supported resolutions that are at least as big as the preview Surface
        boolean exactSizeFound = false;
        final List<Size> bigEnough = new ArrayList<Size>();
        final List<Size> tooSmall = new ArrayList<Size>();
        for (final Size option : choices) {
            if (option.equals(desiredSize)) {
                // Set the size but don't return yet so that remaining sizes will still be logged.
                exactSizeFound = true;
            }

            if (option.getHeight() >= minSize && option.getWidth() >= minSize) {
                bigEnough.add(option);
            } else {
                tooSmall.add(option);
            }
        }

        LOGGER.i("Desired size: " + desiredSize + ", min size: " + minSize + "x" + minSize);
        LOGGER.i("Valid preview sizes: [" + TextUtils.join(", ", bigEnough) + "]");
        LOGGER.i("Rejected preview sizes: [" + TextUtils.join(", ", tooSmall) + "]");

        if (exactSizeFound) {
            LOGGER.i("Exact size match found.");
            return desiredSize;
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            final Size chosenSize = Collections.min(bigEnough, new CompareSizesByArea());
            LOGGER.i("Chosen size: " + chosenSize.getWidth() + "x" + chosenSize.getHeight());
            return chosenSize;
        } else {
            LOGGER.e("Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static CameraConnectionFragment_pig newInstance(
            final ConnectionCallback callback,
            final OnImageAvailableListener imageListener, final int layout, final Size inputSize) {
        return new CameraConnectionFragment_pig(callback, imageListener, layout, inputSize);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CameraConntFragment:", "CameraConnectionFragment_pig onDestroy()!");
        Activity activity = getActivity();
        mIsRecordingVideo = false;
        MediaProcessor.getInstance(activity).handleMediaResource_destroy();
//        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(layout, container, false);
    }

    //手势滑动
    private float mPosX, mPosY;//开始位置
    private float mCurPosX, mCurPosY;//结束位置

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
//        InnApplication.during = 0;
        Global.VIDEO_PROCESS = false;
        LOGGER.i("luolu Global.model1: " + Model.BUILD.value());
        textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mReCordLayout = (RelativeLayout) view.findViewById(R.id.record_layout);
        mRecordControl = (TextView) view.findViewById(R.id.record_control);
        mRecordControl.setOnClickListener(this);
        mSendView = (SendView) view.findViewById(R.id.view_send);
        mSendView.backLayout.setOnClickListener(mCancelClickListener);
        mSendView.selectLayout.setOnClickListener(mSaveClickListener);
        mSendView.stopAnim();

        myTest = (TextView) view.findViewById(R.id.myTest);
        myTest.setOnClickListener(this);

        tvNotice = view.findViewById(R.id.tv_notice);

        ivLeft = view.findViewById(R.id.IV_left);
        ivRight = view.findViewById(R.id.IV_right);
        tvBtnLeft = view.findViewById(R.id.TV_left);
        tvBtnRight = view.findViewById(R.id.TV_right);

        mRecordSwitch = view.findViewById(R.id.record_switch);
        mRecordSwitchTxt = (TextView) view.findViewById(R.id.record_switch_txt);
        mRecordSwitchTxt.setTextColor(Color.WHITE);
        mRecordSwitch.setOnClickListener(this);
        mRecordVerify = view.findViewById(R.id.record_verify);
        mRecordVerifyTxt = (TextView) view.findViewById(R.id.record_verify_txt);
        mRecordVerifyTxt.setVisibility(View.GONE);
        mRecordVerifyTxt.setTextColor(Color.DKGRAY);
        mRecordSwitch.setEnabled(true);
        mRecordVerify.setEnabled(false);

        tvBtnLeft.setText("提示\n左脸");
        tvBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivRight.setVisibility(View.GONE);
                if (ivLeft.getVisibility() == View.GONE) {
                    ivLeft.setVisibility(View.VISIBLE);
                } else {
                    ivLeft.setVisibility(View.GONE);
                }
            }
        });
        tvBtnRight.setText("提示\n右脸");
        tvBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivLeft.setVisibility(View.GONE);
                if (ivRight.getVisibility() == View.GONE) {
                    ivRight.setVisibility(View.VISIBLE);
                } else {
                    ivRight.setVisibility(View.GONE);
                }
            }
        });


        LOGGER.i("luolu Global.model2: " + Model.BUILD.value());
        activity = getActivity();
        context = getActivity().getApplicationContext();
        if (Global.mediaInsureItem == null) {
            Global.mediaInsureItem = new MediaInsureItem(activity);
        }
        if (Global.mediaPayItem == null) {
            Global.mediaPayItem = new MediaPayItem(activity);
        }

        //每次初始化成功后清空图片信息
        if (Global.model == Model.BUILD.value()) {
            Global.mediaInsureItem.currentDel();
            Global.mediaInsureItem.currentInit();
        } else if (Global.model == Model.VERIFY.value()) {
            Global.mediaPayItem.currentDel();
            Global.mediaPayItem.currentInit();
        }

        seekBar = view.findViewById(R.id.seekbar);
        seekBar.setMax(24);
        seekBar.setProgress(12);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i > 12) {
                    exposureCompensation = i - 12;
                    updatePreview();
                }
                if (i < 12) {
                    exposureCompensation = i - 12;
                    updatePreview();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        //初始化时候获取当前动物类型
        leftEnough = false;
        rightEnough = false;

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {

        if (mIsRecordingVideo) {
            // 停止按钮点击时
            PigAppConfig.during += System.currentTimeMillis() - timeVideoStart;
            //Toast.makeText(activity, InnApplication.during+"", Toast.LENGTH_SHORT).show();
            stopRecordingVideo(false);
        }

        Global.VIDEO_PROCESS = false;

        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();//录制/暂定切换
        if (id == R.id.record_control) {
            Log.e(TAG, "onClick: " + lastClickTime);
            if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME) {
                return;
            }
            lastClickTime = System.currentTimeMillis();
            Log.e(TAG, "onClick:ok " + lastClickTime);
            mRecordControl.setClickable(false);

            if (mIsRecordingVideo) {
                // 停止按钮点击时
                PigAppConfig.during += System.currentTimeMillis() - timeVideoStart;
                //Toast.makeText(activity, InnApplication.during+"", Toast.LENGTH_SHORT).show();
                stopRecordingVideo(false);
                Global.VIDEO_PROCESS = false;

                try {
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            collectNumberHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mMediaRecorder.stop();
                                    } catch (IllegalStateException e) {
                                        Log.e(TAG, " mMediaRecorder.stop:Exception " + e);
                                        // TODO 如果当前java状态和jni里面的状态不一致，
                                        //e.printStackTrace();
                                        mMediaRecorder = null;
                                        mMediaRecorder = new MediaRecorder();
                                    } catch (RuntimeException e) {
                                        Log.e(TAG, " mMediaRecorder.stop:Exception " + e);
                                        // TODO 如果当前java状态和jni里面的状态不一致，
                                        //e.printStackTrace();
                                        mMediaRecorder = null;
                                        mMediaRecorder = new MediaRecorder();
                                    }
                                    mMediaRecorder.reset();
                                }
                            });
                        }
                    };
                    new Timer().schedule(timerTask, 30);
                } catch (Exception e) {
                    Log.e("-----停止视频录制-----------", "---->>>>>>>>>" + e);
                    e.printStackTrace();
                }

            } else {
                // 录制按钮点击时
                try {
                    Global.VIDEO_PROCESS = true;
                    timeVideoStart = System.currentTimeMillis();
                    startRecordingVideo();
                } catch (IOException e) {
                    Log.e(TAG, "record_control_IOException: " + e.toString());
                    e.printStackTrace();
                }

            }
        } else if (id == R.id.record_switch) {
            if (Global.model != Model.BUILD.value()) {
                Global.model = Model.BUILD.value();
                mRecordSwitchTxt.setTextColor(Color.WHITE);
                mRecordVerifyTxt.setTextColor(Color.DKGRAY);
            }
        } else if (id == R.id.record_verify) {
            if (Global.model != Model.VERIFY.value()) {
                Global.model = Model.VERIFY.value();
                mRecordVerifyTxt.setTextColor(Color.WHITE);
                mRecordSwitchTxt.setTextColor(Color.DKGRAY);
            }
        } else if (id == R.id.myTest) {
            try {
                Global.VIDEO_PROCESS = true;
                timeVideoStart = System.currentTimeMillis();
                startRecordingVideo();
            } catch (Exception e) {
                Log.e(TAG, "record_control_IOException: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private View.OnClickListener mCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mReCordLayout.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(Global.VideoFileName)) {
                FileUtils.deleteFile(new File(Global.VideoFileName));
            }
        }
    };

    private View.OnClickListener mSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mReCordLayout.setVisibility(View.VISIBLE);
            Activity activity = getActivity();
            MediaProcessor.getInstance(activity).handleMediaResource_build(activity);
        }
    };

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(final int width, final int height) {
        final Activity activity = getActivity();
        final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
//                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                characteristics = manager.getCameraCharacteristics(cameraId);
                mCharacteristics = characteristics;

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                // We don't use a front facing camera in this sample.
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                LOGGER.i("map：" + map.toString());
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                final Size largest =
                        Collections.max(
                                Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)),
                                new CompareSizesByArea());

                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);//获取摄像头的旋转角度

                mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));//map.getOutputSizes(MediaRecorder.class)--获取图片输出的尺�?
                previewSize =
                        chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                                inputSize.getWidth(),
                                inputSize.getHeight());

                mVideoSize_midia = mVideoSize;

                mVideoSize_midia_width = mVideoSize.getWidth();
                mVideoSize_midia_height = mVideoSize.getHeight();

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                final int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(width, height);
                } else {
//                    textureView.setAspectRatio(width, height);
                    textureView.setAspectRatio(3, 4);
                }

                CameraConnectionFragment_pig.this.cameraId = cameraId;

                cameraConnectionCallback.onPreviewSizeChosen(previewSize, 0);
                return;
            }
        } catch (final CameraAccessException e) {
            LOGGER.e(e, "Exception!");
        } catch (final NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }

    }

    /**
     * Opens the camera specified by {@link CameraConnectionFragment_pig#cameraId}.
     */
    @SuppressLint("MissingPermission")
    private void openCamera(final int width, final int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        mMediaRecorder = new MediaRecorder();
        final Activity activity = getActivity();
        final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //  Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (final CameraAccessException e) {
            LOGGER.e(e, "Exception!");
        } catch (final InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }

    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != previewReader) {
                previewReader.close();
                previewReader = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("ImageListener");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }
    }

    private final CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(
                        final CameraCaptureSession session,
                        final CaptureRequest request,
                        final CaptureResult partialResult) {
                }

                @Override
                public void onCaptureCompleted(
                        final CameraCaptureSession session,
                        final CaptureRequest request,
                        final TotalCaptureResult result) {
                }
            };

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void startPreview() {
        if (null == cameraDevice || !textureView.isAvailable() || null == previewSize) {
            return;
        }
        try {
            closePreviewSession();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // This is the output Surface we need to start preview.
            final Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            LOGGER.i("Opening camera preview: " + previewSize.getWidth() + "x" + previewSize.getHeight());

            // Create the reader for the preview frames.
            previewReader =
                    ImageReader.newInstance(
                            previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 2);

            previewReader.setOnImageAvailableListener(imageListener, backgroundHandler);
            previewRequestBuilder.addTarget(previewReader.getSurface());

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice.createCaptureSession(
                    Arrays.asList(surface, previewReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(final CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return;
                            }
                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(final CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed----------");
                        }
                    },
                    null);
        } catch (final CameraAccessException e) {
            LOGGER.e(e, "Exception!");
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == cameraDevice) {
            return;
        }
        try {
            // Auto focus should be continuous for camera preview.
            previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureCompensation);
            previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
            // Finally, we start displaying the camera preview.
            previewRequest = previewRequestBuilder.build();
            captureSession.stopRepeating();


            captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(final int viewWidth, final int viewHeight) {
        final Activity activity = getActivity();
        if (null == textureView || null == previewSize || null == activity) {
            return;
        }
        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        final Matrix matrix = new Matrix();
        final RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        final RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        final float centerX = viewRect.centerX();
        final float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            final float scale =
                    Math.max(
                            (float) viewHeight / previewSize.getHeight(),
                            (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    //保存视频
    private void setUpMediaRecorder() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        //mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (Global.model == Model.BUILD.value()) {
            Global.VideoFileName = Global.mediaInsureItem.getVideoFileName();
        } else if (Global.model == Model.VERIFY.value()) {
            Global.VideoFileName = Global.mediaPayItem.getVideoFileName();
        }
        Log.i("Global.model:", Global.model + "");
        Log.i("VideoFileName:", Global.VideoFileName);
        mMediaRecorder.setOutputFile(Global.VideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(600000);
        mMediaRecorder.setVideoFrameRate(30);
//        if (previewSize.getWidth() <= 3000) {
//            mMediaRecorder.setVideoSize(previewSize.getWidth(), previewSize.getHeight());
//        } else {
//            mMediaRecorder.setVideoSize(mVideoSize_midia_width, mVideoSize_midia_height);
//        }
        mMediaRecorder.setVideoSize(640, 480);

        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //解决录制视频, 播放器横向问题
        mMediaRecorder.setOrientationHint(90);
        try {
            /**
             * 准备记录器开始捕捉和编码数据。此方法必须在设置所需的音频和视频源、编码器、文件格式等之后调用，但在start()之前调用。
             * 如果在调用之后抛出IllegalStateException
             * start()或在setOutputFormat()之前。
             * 如果准备失败，则抛出IOException。
             */
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            mMediaRecorder.release();
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            mMediaRecorder.release();
        }
    }

    private void startRecordingVideo() throws IOException {
        if (null == cameraDevice || !textureView.isAvailable() || null == previewSize) {
            return;
        }
        Global.VIDEO_PROCESS = true;
        try {
            closePreviewSession();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setUpMediaRecorder();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            List<Surface> surfaces = new ArrayList<>();
            // Set up Surface for the camera preview
            Surface textureSurface = new Surface(texture);
            previewReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 4);
            previewReader.setOnImageAvailableListener(imageListener, backgroundHandler);
            Surface imageSurface = previewReader.getSurface();

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            previewRequestBuilder.addTarget(textureSurface);
            previewRequestBuilder.addTarget(recorderSurface);
            previewRequestBuilder.addTarget(imageSurface);
            List<Surface> surfaceList = Arrays.asList(textureSurface, recorderSurface, imageSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            cameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }
                    captureSession = cameraCaptureSession;
                    updatePreview();

                    getActivity().runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void run() {

                            tvNotice.setVisibility(View.VISIBLE);

                            if (leftEnough) {
                                tvBtnLeft.setVisibility(View.GONE);
                            } else {
                                tvBtnLeft.setVisibility(View.VISIBLE);
                            }
                            if (rightEnough) {
                                tvBtnRight.setVisibility(View.GONE);
                            } else {
                                tvBtnRight.setVisibility(View.VISIBLE);
                            }

                            if (Global.UPLOAD_VIDEO_FLAG) {
                                //  if (touBaoVieoFlag.equals("1")|| liPeiVieoFlag.equals("1")) {
                                Log.i("===startrecord==", "开始录制");
                                try {
                                    try {
                                        Thread.sleep(200);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    // UI
                                    mRecordControl.setText(R.string.pause);

                                    mIsRecordingVideo = true;
                                    // Start recording
                                    mMediaRecorder.start();
                                    // disable switch action
                                    mRecordSwitch.setEnabled(false);
                                    mRecordVerify.setEnabled(false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showToast("视频录制异常！");
                                }
                                mRecordControl.setClickable(true);
                            } else {
                                // UI
                                mRecordControl.setText(R.string.pause);

                                mIsRecordingVideo = true;
                                try {
                                    mMediaRecorder.prepare();
                                } catch (IllegalStateException e) {
                                    Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
                                } catch (IOException e) {
                                    Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
                                }
                                mRecordSwitch.setEnabled(false);
                                mRecordControl.setClickable(true);
                            }


                        }
                    });
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Camera Failed!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JPushStatsConfig.onCountEvent(getActivity(), "start_collecting", null);
    }

    private void stopRecordingVideo(boolean save) {
        // UI
        //mMediaRecorder = new MediaRecorder();
        mIsRecordingVideo = false;

        mRecordSwitch.setEnabled(true);
        tvNotice.setVisibility(View.GONE);
        tvBtnLeft.setVisibility(View.GONE);
        tvBtnRight.setVisibility(View.GONE);

        ivLeft.setVisibility(View.GONE);
        ivRight.setVisibility(View.GONE);

//        mMediaRecorder.reset();
        Global.VIDEO_PROCESS = false;
        startPreview();
//        lastClickTime = 0L;
        mRecordControl.setText(R.string.record);
        mRecordControl.setClickable(true);
        JPushStatsConfig.onCountEvent(getActivity(), "end_of_acquisition", null);
    }

    private void closePreviewSession() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(final Size lhs, final Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {
        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(final String message) {
            final ErrorDialog dialog = new ErrorDialog();
            final Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(
                            android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialogInterface, final int i) {
                                    activity.finish();
                                }
                            })
                    .create();
        }
    }


    // TODO: 2018/8/7 By:LuoLu
    @SuppressLint("HandlerLeak")
    public static Handler collectNumberHandler = new Handler() {
        @Override
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    try {
                        if (mMediaRecorder == null) {
                            //mMediaRecorder = new MediaRecorder();
                        }
                        PigAppConfig.during += System.currentTimeMillis() - timeVideoStart;
                        //Toast.makeText(activity, ""+InnApplication.during, Toast.LENGTH_SHORT).show();
                        Global.VIDEO_PROCESS = false;
                        // 录制、暂停按钮所在布局隐藏
                        mReCordLayout.setVisibility(View.GONE);
                        mIsRecordingVideo = false;
                        mRecordControl.setText(R.string.record);
                        tvNotice.setVisibility(View.GONE);
                        tvBtnLeft.setVisibility(View.GONE);
                        tvBtnRight.setVisibility(View.GONE);
                        ivLeft.setVisibility(View.GONE);
                        ivRight.setVisibility(View.GONE);
                        // 采集？ 按钮
                        mRecordSwitch.setEnabled(true);
                        // 停止视频录制
                        Log.i("停止视频录制", "start ");

                        try {
                            captureSession.stopRepeating();
                            captureSession.abortCaptures();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }

                        try {
                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    collectNumberHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
//                                            if (mMediaRecorder != null) {
//                                                // clear recorder configuration
//                                                mMediaRecorder.reset();
//                                                // release the recorder object
//                                                mMediaRecorder.release();
//                                                mMediaRecorder = null;
//                                            }
                                            try {
                                                mMediaRecorder.stop();
                                            } catch (IllegalStateException e) {
                                                Log.e(TAG, " mMediaRecorder.stop:Exception " + e);
                                                // TODO 如果当前java状态和jni里面的状态不一致，
                                                //e.printStackTrace();
                                                mMediaRecorder = null;
                                                mMediaRecorder = new MediaRecorder();
                                            } catch (RuntimeException e) {
                                                Log.e(TAG, " mMediaRecorder.stop:Exception " + e);
                                                // TODO 如果当前java状态和jni里面的状态不一致，
                                                //e.printStackTrace();
                                                mMediaRecorder = null;
                                                mMediaRecorder = new MediaRecorder();
                                            }
                                            mMediaRecorder.reset();
                                        }
                                    });
                                }
                            };
                            new Timer().schedule(timerTask, 30);
                        } catch (RuntimeException e) {
                            Log.e("-----停止视频录制-----------", "---->>>>>>>>>" + e);
                            e.printStackTrace();
                        }


//                        try {
//                            mMediaRecorder.reset();
////                          mMediaRecorder.release();
//                        } catch (Exception e) {
//                            Log.i("停止视频录制", e.toString());
//                        }
                        Log.i("停止视频录制", "end ");
                        if (!Global.UPLOAD_VIDEO_FLAG) {
                            if (!TextUtils.isEmpty(Global.VideoFileName)) {
                                //将已录制的视频删除
//                                boolean deleteResult = FileUtils.deleteFile(new File(Global.VideoFileName));
//                                if (deleteResult == true) {
//                                    LOGGER.i("collectNumberHandler录制视频删除成功！");
//                                }
                            }
                        }
                        MediaProcessor.getInstance(activity).handleMediaResource_build(activity);
                        MediaProcessor.getInstance(activity).showInsureDialog();

                    } catch (WindowManager.BadTokenException e) {
                        e.printStackTrace();
                        //use a log message
                        AlertDialog.Builder builderApplyFinish = new AlertDialog.Builder(activity)
                                .setIcon(R.drawable.cowface)
                                .setTitle("提示")
                                .setMessage("文件处理异常！！")
                                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        activity.finish();
                                    }
                                });
                        builderApplyFinish.setCancelable(false);
                        builderApplyFinish.show();
                    }
                    break;
                case 2:
                    if (mReCordLayout != null) {
                        mReCordLayout.setVisibility(View.VISIBLE);
                    }

//                    new DetectorActivity_pig().reInitCurrentCounter(0, 0, 0);
//                    if (activity != null) {
//                        new MultiBoxTracker(activity).reInitCounter(0, 0, 0);
//                    }
                    if (activity instanceof DetectorActivity_pig) {
                        ((DetectorActivity_pig) activity).reInitCurrentCounter(0, 0, 0);

                        if (tracker != null) {
                            tracker.reInitCounter(0, 0, 0);
                        } else {
                            new MultiBoxTracker(activity).reInitCounter(0, 0, 0);
                        }
                    }
                    if (trackingOverlay != null) {
                        trackingOverlay.refreshDrawableState();
                        trackingOverlay.invalidate();
                    }
                    if (textureView != null) {
                        textureView.refreshDrawableState();
                    }
                    PigAppConfig.debugNub = 0;
                    PigAppConfig.during = 0;
                    LOGGER.i("collectNumberHandler Message 2！");
                    break;
                //左脸达到数量
                case 3:
                    leftEnough = true;
                    tvBtnLeft.setVisibility(View.GONE);
                    ivLeft.setVisibility(View.GONE);
                    break;
                //右脸达到数量
                case 4:
                    rightEnough = true;
                    tvBtnRight.setVisibility(View.GONE);
                    ivRight.setVisibility(View.GONE);
                    break;
                //继续录制
                case 5:
                    myTest.performClick();
                    break;
                //达到2分钟和4分钟时 停止录制
                case 6:
                    try {
                        if (mMediaRecorder == null) {
                            //mMediaRecorder = new MediaRecorder();
                        }
                        if (PigAppConfig.debugNub == 2) {
                            // 录制、暂停按钮所在布局隐藏
                            mReCordLayout.setVisibility(View.GONE);
                            tvNotice.setVisibility(View.GONE);
                            tvBtnLeft.setVisibility(View.GONE);
                            tvBtnRight.setVisibility(View.GONE);
                            ivLeft.setVisibility(View.GONE);
                            ivRight.setVisibility(View.GONE);
                        }

                        PigAppConfig.during += System.currentTimeMillis() - timeVideoStart;
                        Global.VIDEO_PROCESS = false;

                        mIsRecordingVideo = false;
                        mRecordControl.setText(R.string.record);
                        // 停止视频录制
                        try {
                            captureSession.stopRepeating();
                            captureSession.abortCaptures();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }

                        try {
                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    collectNumberHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
//                                            if (mMediaRecorder != null) {
//                                                // clear recorder configuration
//                                                mMediaRecorder.reset();
//                                                // release the recorder object
//                                                mMediaRecorder.release();
//                                                mMediaRecorder = null;
//                                            }
                                            try {
                                                mMediaRecorder.stop();
                                            } catch (IllegalStateException e) {
                                                Log.e(TAG, " mMediaRecorder.stop:Exception " + e);
                                                // TODO 如果当前java状态和jni里面的状态不一致，
                                                //e.printStackTrace();
                                                mMediaRecorder = null;
                                                mMediaRecorder = new MediaRecorder();
                                            } catch (RuntimeException e) {
                                                Log.e(TAG, " mMediaRecorder.stop:Exception " + e);
                                                // TODO 如果当前java状态和jni里面的状态不一致，
                                                //e.printStackTrace();
                                                mMediaRecorder = null;
                                                mMediaRecorder = new MediaRecorder();
                                            }
                                            mMediaRecorder.reset();
                                        }
                                    });
                                }
                            };
                            new Timer().schedule(timerTask, 30);
                        } catch (RuntimeException e) {
                            Log.e("-----停止视频录制-----------", "---->>>>>>>>>" + e);
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        Log.i("tv_ 停止视频录制", e.toString());
                    }
                    MediaProcessor.getInstance(activity).handleMediaResource_build(activity);
                    MediaProcessor.getInstance(activity).showInsureDialog();
                    break;
                default:
                    break;
            }
        }

    };

    public void setParmes(String sheId, String inspectNo, String reason) {
        //猪舍id 猪圈id 保单号  出险原因
        MediaProcessor.getInstance(this.getActivity()).handleMediaResource_build(getActivity(), sheId, inspectNo, reason);
    }
}
