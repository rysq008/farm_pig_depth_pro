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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.model.bean.RecognitionResult;
import com.xiangchuang.risks.utils.CounterHelper;
import com.xiangchuangtec.luolu.animalcounter.CounterActivity_new;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;
import com.xiangchuangtec.luolu.animalcounter.view.ShowPollingActivity_new;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker_new;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import innovation.media.MediaInsureItem;
import innovation.media.MediaPayItem;
import innovation.media.Model;
import innovation.utils.FileUtils;
import innovation.utils.ZipUtil;
import innovation.view.SendView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.ContentValues.TAG;
import static com.xiangchuangtec.luolu.animalcounter.MyApplication.lastXmin;
import static com.xiangchuangtec.luolu.animalcounter.MyApplication.sowCount;
import static org.tensorflow.demo.DetectorActivity_new.trackingOverlay;
import static org.tensorflow.demo.Global.mediaPayItem;

@SuppressLint("ValidFragment")
public class CameraConnectionFragment_new extends Fragment implements View.OnClickListener, FragmentCompat.OnRequestPermissionsResultCallback {
    private static final Logger LOGGER = new Logger();

    /**
     * The camera preview size will be chosen to be the smallest frame by pixel size capable of
     * containing a DESIRED_SIZE x DESIRED_SIZE square.
     */
    private static final int MINIMUM_PREVIEW_SIZE = 320;

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
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
                @TargetApi(Build.VERSION_CODES.M)
                @RequiresApi(api = Build.VERSION_CODES.M)
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
    private boolean mManualFocusEngaged;
    private static Activity activity;
    private CameraCharacteristics characteristics;
    public static TextView textSensorExposureTime;
    private File mfile;
    private String mfleg;
    private String videoFileName;


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
    public static AutoFitTextureView textureView;

    private static RelativeLayout mReCordLayout;
    /**
     * Button to record video
     */
    private static TextView mRecordControl;
    private static View mRecordSwitch;
    private TextView mRecordSwitchTxt;
    private View mRecordVerify;
    private TextView mRecordVerifyTxt;

    private static SendView mSendView;

    //haojie add
    private RelativeLayout mToolLayout;
    private static long tmieVideoStart;
    private long tmieVideoEnd;

    //全局定义
    private volatile long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 2000;  // 快速点击间隔


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
    private CameraCaptureSession captureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice cameraDevice;

    /**
     * The rotation in degrees of the camera sensor from the display.
     */
    private Integer sensorOrientation;

    /**
     * The {@link Size} of video recording.
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

    // private Model mModel = Model.BUILD;
    private int exposureCompensation = 0;

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
    // private final Size inputSize;
    private final int inputSize;
    /**
     * The layout identifier to inflate for this Fragment.
     */
    private final int layout;


    private final ConnectionCallback cameraConnectionCallback;

    /*   CameraConnectionFragment(
               final ConnectionCallback connectionCallback,
               final OnImageAvailableListener imageListener,
               final int layout, final Size inputSize) {
           this.cameraConnectionCallback = connectionCallback;
           this.imageListener = imageListener;
           this.layout = layout;
           this.inputSize = inputSize;
       }*/
    CameraConnectionFragment_new(
            final ConnectionCallback connectionCallback,
            final OnImageAvailableListener imageListener,
            final int layout, final int inputSize) {
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

   /* public static CameraConnectionFragment newInstance(
            final ConnectionCallback callback,
            final OnImageAvailableListener imageListener, final int layout, final Size inputSize) {
        return new CameraConnectionFragment(callback, imageListener, layout, inputSize);
    }*/

    public static CameraConnectionFragment_new newInstance(
            final ConnectionCallback callback,
            final OnImageAvailableListener imageListener, final int layout, final int inputSize) {
        return new CameraConnectionFragment_new(callback, imageListener, layout, inputSize);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CameraConntFragment:", "CameraConnectionFragment onDestroy()!");
        Activity activity = getActivity();
        collectNumberHandler.sendEmptyMessage(2);
//        MediaProcessor.getInstance(activity).handleMediaResource_destroy();
//        InsureDataProcessor.getInstance(activity).handleMediaResource_destroy();
//        PayDataProcessor.getInstance(activity).handleMediaResource_destroy();
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(layout, container, false);
    }
    private static String mSheId;
    private static String mSheName;
    private static String mOldAutoCount;
    private static String mOldDuration;
    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        LOGGER.i("luolu Global.model1: " + Model.BUILD.value());
        textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mReCordLayout = (RelativeLayout) view.findViewById(R.id.record_layout);
        mRecordControl = (TextView) view.findViewById(R.id.record_control);
        mRecordControl.setOnClickListener(this);
        mSendView = (SendView) view.findViewById(R.id.view_send);
        mSendView.backLayout.setOnClickListener(mCancelClickListener);
        mSendView.selectLayout.setOnClickListener(mSaveClickListener);
        mSendView.stopAnim();

        view.findViewById(R.id.tv_notice).setVisibility(View.GONE);
        view.findViewById(R.id.IV_left).setVisibility(View.GONE);
        view.findViewById(R.id.IV_right).setVisibility(View.GONE);
        view.findViewById(R.id.TV_left).setVisibility(View.GONE);
        view.findViewById(R.id.TV_right).setVisibility(View.GONE);



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

        LOGGER.i("luolu Global.model2: " + Model.BUILD.value());
        activity = getActivity();

        Intent intent = activity.getIntent();
        mSheId = intent.getStringExtra("sheid");
        mSheName  = intent.getStringExtra("shename");;
        mOldAutoCount = intent.getStringExtra("autocount");
        mOldDuration = intent.getStringExtra("duration");

        if (Global.mediaInsureItem == null) {
            Global.mediaInsureItem = new MediaInsureItem(activity);
        }
        if (Global.mediaPayItem == null) {
            mediaPayItem = new MediaPayItem(activity);
        }
        //String videoFileName = Global.mediaInsureItem.getVideoFileName();
        videoFileName = "/storage/emulated/0/innovation/animal/ZipImage/video.mp4";
        Log.i("===videoFileName===", videoFileName);
        mfile = new File(videoFileName);
        if (!mfile.exists()) {
            try {
                mfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mfleg = PreferencesUtils.getStringValue(Constants.fleg, MyApplication.getAppContext());
        mReCordLayout.setVisibility(View.VISIBLE);
        // TODO: 2018/9/26 By:LuoLu  currentInit dir
       /* if (Global.model == Model.BUILD.value()) {
            InsureDataProcessor.getInstance(activity).handleMediaResource_build(activity);
            Global.mediaInsureItem.currentDel();
            Global.mediaInsureItem.currentInit();
        } else if (Global.model == Model.VERIFY.value()) {
            PayDataProcessor.getInstance(activity).handleMediaResource_build(activity);
            mediaPayItem.currentDel();
            mediaPayItem.currentInit();
        }*/

        //textSensorExposureTime = view.findViewById(R.id.textSensorExposureTime);

    }


    private boolean isMeteringAreaAFSupported() {
        final CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;
        try {

            for (final String cameraId : manager.getCameraIdList()) {
                characteristics = manager.getCameraCharacteristics(cameraId);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) >= 1;
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

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                openCamera(textureView.getWidth(), textureView.getHeight());
            }
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.record_control:

                Log.e(TAG, "onClick: " + lastClickTime);
                if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME) {
                    return;
                }
                lastClickTime = System.currentTimeMillis();
                Log.e(TAG, "onClick:ok " + lastClickTime);
                mRecordControl.setClickable(false);
                if (mIsRecordingVideo) {
                    stopRecordingVideo(false);
                    Global.VIDEO_PROCESS = false;
                    /*try {

                        mMediaRecorder.reset();
                    } catch (Exception e) {
                        Log.i("停止视频录制", e.toString());
                    }*/
                   collectNumberHandler.sendEmptyMessage(1);
                } else {
                    try {
                        Global.VIDEO_PROCESS = true;
                        tmieVideoStart = System.currentTimeMillis();
                        startRecordingVideo();
                    } catch (Exception e) {
                        Log.e(TAG, "record_control_IOException: " + e.toString());
                        e.printStackTrace();
                    }

                }
                break;
            case R.id.record_switch:
                if (Global.model != Model.BUILD.value()) {
                    Global.model = Model.BUILD.value();
                    mRecordSwitchTxt.setTextColor(Color.WHITE);
                    mRecordVerifyTxt.setTextColor(Color.DKGRAY);
                }
                break;
            case R.id.record_verify:
                if (Global.model != Model.VERIFY.value()) {
                    Global.model = Model.VERIFY.value();
                    mRecordVerifyTxt.setTextColor(Color.WHITE);
                    mRecordSwitchTxt.setTextColor(Color.DKGRAY);
                }
                break;

            default:
                break;
        }
    }

    private View.OnTouchListener textureViewTouchFocusClickListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            final int actionMasked = motionEvent.getActionMasked();
            if (actionMasked != MotionEvent.ACTION_DOWN) {
                return false;
            }
            if (mManualFocusEngaged) {
                Log.d("=====", "Manual focus already engaged");
                return true;
            }

//                    final Rect sensorArraySize = cameraDevice.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

            // : here I just flip x,y, but this needs to correspond with the sensor orientation (via SENSOR_ORIENTATION)
            final int y = (int) ((motionEvent.getX() / (float) view.getWidth()) * (float) mVideoSize.getHeight());
            final int x = (int) ((motionEvent.getY() / (float) view.getHeight()) * (float) mVideoSize.getWidth());
            final int halfTouchWidth = 150; //(int)motionEvent.getTouchMajor(); // : this doesn't represent actual touch size in pixel. Values range in [3, 10]...
            final int halfTouchHeight = 150; //(int)motionEvent.getTouchMinor();
            MeteringRectangle focusAreaTouch = new MeteringRectangle(Math.max(x - halfTouchWidth, 0),
                    Math.max(y - halfTouchHeight, 0),
                    halfTouchWidth * 2,
                    halfTouchHeight * 2,
                    MeteringRectangle.METERING_WEIGHT_MAX - 1);

            CameraCaptureSession.CaptureCallback captureCallbackHandler = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    mManualFocusEngaged = false;

                    if (request.getTag() == "FOCUS_TAG") {
                        //the focus trigger is complete -
                        //resume repeating (preview surface will get frames), clear AF trigger
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
                        try {
                            captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    Log.e("======", "Manual AF failure: " + failure);
                    mManualFocusEngaged = false;
                }
            };

            //first stop the existing repeating request
            try {
                captureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            //cancel any existing AF trigger (repeated touches, etc.)
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            try {
                captureSession.capture(previewRequestBuilder.build(), captureCallbackHandler, backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            //Now add a new AF trigger with focus region
            if (isMeteringAreaAFSupported()) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{focusAreaTouch});
            }
            previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            previewRequestBuilder.setTag("FOCUS_TAG"); //we'll capture this later for resuming the preview

            //then we ask for a single request (not repeating!)
            try {
                captureSession.capture(previewRequestBuilder.build(), captureCallbackHandler, backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mManualFocusEngaged = true;

            return true;

        }
    };

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
            //InsureDataProcessor.getInstance(activity).handleMediaResource_build(activity);
//            MediaProcessor.getInstance(activity).handleMediaResource_build(activity);

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

                mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));//map.getOutputSizes(MediaRecorder.class)--获取图片输出的尺寸
                previewSize =
                        chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                                width,
                                height);

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

                CameraConnectionFragment_new.this.cameraId = cameraId;

//                // TODO: 2018/10/15 By:LuoLu
//                Range<Integer> range2 = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
//                int max1 = range2.getUpper();//10000
//                int min1 = range2.getLower();//100
////                int iso = ((progress * (max1 - min1)) / 100 + min1);
////                mPreviewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
//                LOGGER.i("range2 max："+ max1);
//                LOGGER.i("range2 min："+ min1);
//
//                Range<Long> range = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
//                long max = range.getUpper();
//                long min = range.getLower();
//                long ae = (((max - min)) / 100 + min);
//                previewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, ae);
//                textSensorExposureTime.setText("曝光时间：" + ae);

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

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (FragmentCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d("===", "onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        ErrorDialog.newInstance(getString(R.string.permission_request))
                                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
                        break;
                    }
                }
            } else {
                ErrorDialog.newInstance(getString(R.string.permission_request))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Requests permissions needed for recording video.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            FragmentCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    /**
     * Opens the camera specified by {@link CameraConnectionFragment_new#cameraId}.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    private void openCamera(final int width, final int height) {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions();
            return;
        }
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

    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;

    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_request)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentCompat.requestPermissions(parent, VIDEO_PERMISSIONS,
                                    REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parent.getActivity().finish();
                                }
                            })
                    .create();
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
//            // TODO: 2018/10/16 By:LuoLu
//            Range<Long> range = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
//            long max = range.getUpper();
//            long min = range.getLower();
//            long ae = (((max - min)) / 100 + min);
//            previewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, ae);
//            textSensorExposureTime.setText("曝光时间：" + ae);
//            valueAETime = ae;


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
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("CameraFragment", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            mMediaRecorder.release();
        } catch (IOException e) {
            Log.d("CameraFragment", "IOException preparing MediaRecorder: " + e.getMessage());
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
                        @Override
                        public void run() {

                            Log.i("===startrecord==", "开始录制");
                            try {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                // UI
                                mRecordControl.setText("完成");

                                mIsRecordingVideo = true;
                                // Start recording
                                mMediaRecorder.start();
                                // disable switch action
                                mRecordSwitch.setEnabled(false);
                                mRecordVerify.setEnabled(false);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "run: Exception"+e.toString());
                                showToast("视频录制异常！");
                            }
                            mRecordControl.setClickable(true);

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

    }

    private void stopRecordingVideo(boolean save) {
        // UI
        //mMediaRecorder = new MediaRecorder();
        mIsRecordingVideo = false;
        mRecordControl.setText(R.string.record);
        mRecordSwitch.setEnabled(true);

        mMediaRecorder.reset();
        Global.VIDEO_PROCESS = false;
        startPreview();

        mRecordControl.setText(R.string.record);
        mRecordControl.setClickable(true);
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
                        Global.VIDEO_PROCESS = false;
                        // 录制、暂停按钮所在布局隐藏
                        mReCordLayout.setVisibility(View.GONE);
                        mIsRecordingVideo = false;
                        mRecordControl.setText(R.string.record);

                        mRecordSwitch.setEnabled(true);
                        // 停止视频录制
                        Log.i("停止视频录制", "start ");
                        try {
                            mMediaRecorder.reset();
//                          mMediaRecorder.release();
                        } catch (Exception e) {
                            Log.i("停止视频录制", e.toString());
                        }
                        Log.i("停止视频录制", "end ");
//                        if (Global.UPLOAD_VIDEO_FLAG == false) {
//                            if (!TextUtils.isEmpty(Global.VideoFileName)) {
//                                boolean deleteResult = FileUtils.deleteFile(new File(Global.VideoFileName));
//                                if (deleteResult == true) {
//                                    LOGGER.i("collectNumberHandler录制视频删除成功！");
//                                }
//                            }
//                        }

                        uploadRecognitionResult();

                       /* if (Global.model == Model.VERIFY.value()) {
                            PayDataProcessor.getInstance(activity).handleMediaResource_build(activity);
                        } else if (Global.model == Model.BUILD.value()) {
                            InsureDataProcessor.getInstance(activity).handleMediaResource_build(activity);
                        }*/
                    } catch (WindowManager.BadTokenException e) {
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
//                    new LocationTracker(activity.getResources().getDisplayMetrics()).reInitCounter(0, 0, 0);
//                    trackingOverlay.refreshDrawableState();
//                    textureView.refreshDrawableState();
                    if (mReCordLayout != null) {
                            mReCordLayout.setVisibility(View.VISIBLE);
                        }
                        new DetectorActivity_new().reInitCurrentCounter(0, 0, 0);
                        if (activity != null) {
                            new MultiBoxTracker_new(activity).reInitCounter(0, 0, 0);
                        }
                        if (trackingOverlay != null) {
                            trackingOverlay.refreshDrawableState();
                            trackingOverlay.invalidate();
                        }
                        if (textureView != null) {
                            textureView.refreshDrawableState();
                    }
                    mRecordControl.setClickable(true);

                    LOGGER.i("collectNumberHandler Message 2！");
                    break;

                default:
                    break;
            }
        }

    };

    public void setParmes(String sheId, String inspectNo, String reason) {
        String absolutePath = mfile.getAbsolutePath();
        //猪舍id 猪圈id 保单号  出险原因
//        MediaProcessor.getInstance(this.getActivity()).handleMediaResource_build(getActivity(), sheId, inspectNo, reason, mfile);
    }
// TODO: 2018/9/18 By:LuoLu
//    /**
//     * Configure a new camera session with output surfaces and type.
//     *
//     * @param camera The CameraDevice to be configured.
//     * @param outputSurfaces The surface list that used for camera output.
//     * @param listener The callback CameraDevice will notify when capture results are available.
//     */
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    public static CameraCaptureSession configureCameraSession(CameraDevice camera, List<Surface> outputSurfaces, boolean isHighSpeed, CameraCaptureSession.StateCallback listener, Handler handler) throws CameraAccessException {
//        BlockingSessionCallback sessionListener = new BlockingSessionCallback(listener);
//        if (isHighSpeed) {
//            camera.createConstrainedHighSpeedCaptureSession(outputSurfaces, sessionListener, handler);
//        } else {
//            camera.createCaptureSession(outputSurfaces, sessionListener, handler);
//        }
//        CameraCaptureSession session = sessionListener.waitAndGetSession(SESSION_CONFIGURE_TIMEOUT_MS);
//        assertFalse("Camera session should not be a reprocessable session", session.isReprocessable());
//        String sessionType = isHighSpeed ? "High Speed" : "Normal";
//        assertTrue("Capture session type must be " + sessionType, isHighSpeed == CameraConstrainedHighSpeedCaptureSession.class.isAssignableFrom(session.getClass()));
//        return session;
//    }



    private static void uploadRecognitionResult() {
        Dialog dialog;
        String text = String.format("本次点数采集:\n" +
                        "合计 %d头 时长%d秒\n" +
                        "上次点数采集:\n" +
                        "合计 %s头 时长%s秒", sowCount, (int) ((System.currentTimeMillis() - tmieVideoStart) / 1000),
               mOldAutoCount, mOldDuration);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.hog_finish_layout, null);
        TextView msg = view.findViewById(R.id.TV_msg);
        msg.setText(text);
        TextView cancel = view.findViewById(R.id.TV_cancel);
        cancel.setText("重点本舍");
        TextView submit = view.findViewById(R.id.TV_submit);
        submit.setText("完成");
        TextView title = view.findViewById(R.id.TV_title);
        title.setText("确认完成");

        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(view);

        view.findViewById(R.id.TV_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (activity) {
                    dialog.dismiss();
                    activity.startActivity(new Intent(activity, DetectorActivity_new.class));
                    collectNumberHandler.sendEmptyMessage(2);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showProgressDialog(activity);
                mProgressDialog.show();
                List<RecognitionResult> results = new ArrayList<>();

                results.add(new RecognitionResult(
                        1,sowCount,null,""
                ));

                uploadRecognitionResult(mSheId, mSheName, (int) ((System.currentTimeMillis() - tmieVideoStart) / 1000),
                        results, activity, new CounterHelper.OnUploadResultListener() {
                            @Override
                            public void onCompleted(boolean succeed, String resutl) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        if (succeed) {
                                            try {
                                                JSONObject jsonObject = new JSONObject(resutl);
                                                int status = jsonObject.getInt("status");
                                                String msg = jsonObject.getString("msg");
                                                if (status != 1) {
                                                    Toast.makeText(activity, "上传失败！"+msg, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(activity, "上传成功！", Toast.LENGTH_SHORT).show();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            activity.finish();
                                                        }
                                                    }, 500);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Toast.makeText(activity, "上传失败！", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(activity, "上传失败！", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
            }
        });

        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(true);
    }

    private static ProgressDialog mProgressDialog;
    private static void showProgressDialog(Context context) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);//false


        mProgressDialog.setCanceledOnTouchOutside(false);//false
        mProgressDialog.setIcon(R.drawable.ic_launcher);
//        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "确定", mProgClickListener);
        mProgressDialog.setMessage("正在识别......");
//        mProgressDialog.show();
//        Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
//        if (positive != null) {
//            positive.setVisibility(View.GONE);
//        }
    }


    public static void uploadRecognitionResult(String sheId, String sheName, int duration,
                                               List<RecognitionResult> results,
                                               Context context, CounterHelper.OnUploadResultListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = com.xiangchuang.risks.utils.FileUtils.createTempDir(context);
                File[] files = new File[results.size()];
                files[0] = new File(Global.mediaPayItem.getVideoDir());;

                int totalCount = 0;
                int mAutoCount = 0;
                String locationString = "";
                try {
                    JSONArray arrays = new JSONArray();
                    for (RecognitionResult recognitionResult : results) {
                        JSONObject jsonObject = new JSONObject();
                        //经度 纬度 猪圈名字 图片名字 当前猪圈数
                        jsonObject.put("lat", recognitionResult.lat);
                        jsonObject.put("lon", recognitionResult.lon);
                        jsonObject.put("name", "猪圈" + (recognitionResult.index + 1));
                        jsonObject.put("picName", "");
                        jsonObject.put("count", recognitionResult.count);
                        jsonObject.put("autoCount", recognitionResult.autoCount);
                        arrays.put(jsonObject);
                        totalCount += recognitionResult.autoCount;
                        mAutoCount += recognitionResult.autoCount;

                    }
                    JSONObject root = new JSONObject();
                    root.put("pigsty", arrays);
                    locationString = root.toString();
                } catch (JSONException e) {
                    listener.onCompleted(false, "");
                    return;
                }

                File zipFile = new File(path, "out.zip");
                ZipUtil.zipFiles(files, zipFile);
                Map map = new HashMap();
                map.put(Constants.AppKeyAuthorization, "hopen");
                map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, context));

//                String url = "http://47.92.167.61:8081/numberCheck/app/sheCommit";
                Map<String, String> param = new HashMap<>();
                param.put("sheId", sheId);
                param.put("name", sheName);
                param.put("count", "" + totalCount);
                param.put("autoCount", "" + mAutoCount);
                param.put("location", locationString);
                param.put("timeLength", "" + duration);
                param.put("juanCnt", "" + results.size());
                param.put("createuser", "" + PreferencesUtils.getIntValue(Constants.userid, MyApplication.getAppContext()));
                OkHttp3Util.uploadPreFile(Constants.SHECOMMIT, zipFile, "out.zip", param, map, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        listener.onCompleted(false, "");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == 200) {
                            String resutl = response.body().string();
                            listener.onCompleted(true, resutl);
                        } else{
                            listener.onCompleted(false, "");
                        }
                    }
                });
            }
        }).start();
    }

}
