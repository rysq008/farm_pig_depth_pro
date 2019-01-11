package com.xiangchuangtec.luolu.animalcounter.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;
import com.xiangchuang.risks.model.bean.RecognitionResult;
import com.xiangchuang.risks.utils.CommonUtils;
import com.xiangchuang.risks.utils.CounterHelper;
import com.xiangchuangtec.luolu.animalcounter.JuanCountAdapter;
import com.xiangchuangtec.luolu.animalcounter.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import innovation.location.LocationManager;
import innovation.location.LocationManager_new;


public final class USBCameraActivity_new extends BaseActivity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {
    private static final boolean DEBUG = true;    // TODO set false on release

    private static final String TAG = "USBCameraActivity";

    /**
     * set true if you want to record movie using MediaSurfaceEncoder
     * (writing frame data into Surface camera from MediaCodec
     * by almost same way as USBCameratest2)
     * set false if you want to record movie using MediaVideoEncoder
     */
    private static final boolean USE_SURFACE_ENCODER = false;

    /**
     * preview resolution(width)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_WIDTH = 640;
    /**
     * preview resolution(height)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_HEIGHT = 480;
    /**
     * preview mode
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     * 0:YUYV, other:MJPEG
     */
    private static final int PREVIEW_MODE = 1;

    protected static final int SETTINGS_HIDE_DELAY_MS = 2500;

    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;
    /**
     * Handler to execute camera related methods sequentially on private thread
     */
    private UVCCameraHandler mCameraHandler;
    /**
     * for camera preview display
     */
    private CameraViewInterface mUVCCameraView;
    /**
     * for open&start / stop&close camera preview
     */
    private Button mTakePictureButton;
    /**
     * button for start/stop recording
     */

    //private PigNumDetector pigNumDetector;
    private UVCCameraTextureView mCameraTextureView;
    @BindView(R.id.count_name)
    TextView mCountName;
    @BindView(R.id.total_count)
    TextView mTotalCountTextView;

    @BindView(R.id.usb_camera_activity)
    RelativeLayout counter_activity;
    @BindView(R.id.usb_recogn)
    ImageView mResultImageView;

    @BindView(R.id.juan_list)
    ListView juan_list;

    private Button mCountCompleted;
    private Button mGoonButton;
    private Button mNextButton;
    private final AtomicInteger mTotalCount = new AtomicInteger(0);
    private final AtomicInteger mAutolCount = new AtomicInteger(0);
    private String mSheId;
    private String mSheName;
    private String mOldTotalCount;
    private String mOldAutoCount;
    private String mOldJuanCnt;
    private String mOldDuration;
    private long mStartTime;
    private Bitmap mCurrentBitmap;
    private final List<RecognitionResult> mRecognitionResults = new ArrayList<>();
    private RecognitionResult mCurrentRecognitionResult;
    private JuanCountAdapter mJuanCountAdapter;
    private Button count_detail;
    private AlertDialog dialogcreate;

    private LinearLayout llModifier;
    private Button btnModifierMinus;
    private Button btnModifierPositive;
    private EditText etModifier;
    //记录每次识别获取的头数
    private int tempNum = 0;

    private Dialog dialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (DEBUG) Log.v(TAG, "onCreate:");
        setContentView(R.layout.activity_remote_camera_new);
        ButterKnife.bind(this);
        recognitionView = findViewById(R.id.recognition_view);
        mTakePictureButton = (Button) findViewById(R.id.take_picture_button);
        final View view = findViewById(R.id.camera_view);
        //view.setOnLongClickListener(mOnLongClickListener);
        mUVCCameraView = (CameraViewInterface) view;
        mUVCCameraView.setAspectRatio(PREVIEW_WIDTH / (float) PREVIEW_HEIGHT);

        llModifier = findViewById(R.id.ll_modifier);

        etModifier = findViewById(R.id.tv_modifier);

        mCameraTextureView = (UVCCameraTextureView) view;
        mCameraTextureView.setCallback(this);

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mResultImageView.getLayoutParams();
        float height = (PREVIEW_WIDTH / (float) PREVIEW_HEIGHT) * param.width;
        param.height = (int) height;
        mResultImageView.setLayoutParams(param);

        count_detail = findViewById(R.id.count_detail);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mCameraHandler = UVCCameraHandler.createHandler(this, mUVCCameraView,
                USE_SURFACE_ENCODER ? 0 : 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);
        LocationManager.getInstance(this).startLocation();
        //本猪舍点数完毕
        mCountCompleted = findViewById(R.id.count_completed);
        mCountCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (USBCameraActivity_new.this) {
                    //最后清点的猪圈尚未保存，请点击[重新采集]或[下一圈]
                    if( mGoonButton.getVisibility() == View.VISIBLE && mNextButton.getVisibility() == View.VISIBLE){
                        // "最后清点的猪圈尚未保存，请先选择\n[重新采集]或[下一圈]";
                        //保存当前圈几
                        int tempJuanNum = CounterHelper.number;
                        int tempEt = CommonUtils.parseInt(etModifier.getText().toString().trim());
                        String text = String.format("圈%d信息尚未保存:\n" +
                                "自动识别%d头 修正后%d头\n" +
                                "完成盘查前请先选择[保存]或[放弃]后完成盘查", tempJuanNum, tempNum, tempEt);

                        AlertDialog.Builder builder = new AlertDialog.Builder(USBCameraActivity_new.this);

                        LayoutInflater inflater = LayoutInflater.from(USBCameraActivity_new.this);
                        View view = inflater.inflate(R.layout.hog_finish_layout, null);
                        TextView msg = view.findViewById(R.id.TV_msg);
                        msg.setText(text);
                        dialog = builder.create();
                        dialog.show();
                        dialog.getWindow().setContentView(view);

                        view.findViewById(R.id.TV_close).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });


                        view.findViewById(R.id.TV_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                uploadRecognitionResult();
                            }
                        });

                        view.findViewById(R.id.TV_submit).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                CounterHelper.number++;
                                RecognitionResult result = saveResultToList();
                                Bitmap temp = CounterHelper.drawModifierBitmap(result.getBitmap(), String.valueOf(result.count));
                                result.setBitmap(temp);
                                mJuanCountAdapter.addResult(result);
                                mNextButton.setVisibility(View.GONE);
                                mGoonButton.setVisibility(View.GONE);
                                mResultImageView.setVisibility(View.GONE);
                                mTakePictureButton.setVisibility(View.VISIBLE);
                                llModifier.setVisibility(View.GONE);
                                mTotalCountTextView.setText("总数" + mTotalCount.addAndGet(result.count) + "头");
                                mAutolCount.addAndGet(result.autoCount);

                                uploadRecognitionResult();
                            }
                        });
                        dialog.getWindow().setGravity(Gravity.CENTER);
                        dialog.setCancelable(true);
                        return;
                    }
                    if (mRecognitionResults.size() > 0 ) {
                        uploadRecognitionResult();
                    } else{
                        Toast.makeText(USBCameraActivity_new.this, "您还未清点猪舍", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        mSheId = intent.getStringExtra("sheid");
        mSheName = intent.getStringExtra("shename");
        mOldTotalCount = intent.getStringExtra("pigcount");
        mOldAutoCount = intent.getStringExtra("autocount");
        mOldJuanCnt = intent.getStringExtra("juancnt");
        mOldDuration = intent.getStringExtra("duration");
        mStartTime = System.currentTimeMillis();
        mCountName.setText(mSheName);
        mTotalCountTextView.setText("总数" + mTotalCount.get() + "头");

        mJuanCountAdapter = new JuanCountAdapter(this);
        juan_list.setAdapter(mJuanCountAdapter);

        mTakePictureButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                wakeUpCamera();
                return false;
            }
        });
        //识别按钮
        mTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentBitmap(mCameraTextureView.getBitmap());
                mResultImageView.setVisibility(View.GONE);
                Bitmap bitmap = getCurrentBitmap();
                if(bitmap == null){
                    return;
                }
                showPop();
                CounterHelper.recognitionFromNet(bitmap, new CounterHelper.OnImageRecognitionListener() {
                    @Override
                    public void onCompleted(int count, Bitmap bitmap) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pop.dismiss();
                                if (count >= 0) {
                                    tempNum = count;
                                    mResultImageView.setVisibility(View.VISIBLE);
                                    mResultImageView.setImageBitmap(bitmap);
                                    mTakePictureButton.setVisibility(View.GONE);
                                    mGoonButton.setVisibility(View.VISIBLE);
                                    mNextButton.setVisibility(View.VISIBLE);
                                    llModifier.setVisibility(View.VISIBLE);
                                    etModifier.setText(count + "");
                                    setCurrentResult(count, bitmap, null);
                                } else {
                                    Toast.makeText(USBCameraActivity_new.this, "识别失败！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });
        //重新采集
        mGoonButton = findViewById(R.id.usb_goon_button);
        mGoonButton.setText("重新\n点数");
        mGoonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoonButton.setVisibility(View.GONE);
                mNextButton.setVisibility(View.GONE);
                mTakePictureButton.setVisibility(View.VISIBLE);
                mResultImageView.setVisibility(View.GONE);
                llModifier.setVisibility(View.GONE);
                etModifier.setText("");
            }
        });
        //下一圈
        mNextButton = findViewById(R.id.usb_next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CounterHelper.number++;
                RecognitionResult result = saveResultToList();

                Bitmap temp = CounterHelper.drawModifierBitmap(result.getBitmap(), String.valueOf(result.count));
                result.setBitmap(temp);

                mJuanCountAdapter.addResult(result);
                mNextButton.setVisibility(View.GONE);
                mGoonButton.setVisibility(View.GONE);
                mResultImageView.setVisibility(View.GONE);
                mTakePictureButton.setVisibility(View.VISIBLE);
                llModifier.setVisibility(View.GONE);
                mTotalCountTextView.setText("总数" + mTotalCount.addAndGet(result.count) + "头");
                mAutolCount.addAndGet(result.autoCount);
            }
        });

        btnModifierMinus = findViewById(R.id.btn_modifier_minus);
        //修改数量 减少按钮
        btnModifierMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llModifier.getVisibility() == View.VISIBLE) {
                    int tempCount = CommonUtils.parseInt(etModifier.getText().toString());
                    if(tempCount > 0){
                        tempCount -- ;
                    }
                    if ((tempNum - tempCount) > 3) {
                        Toast.makeText(USBCameraActivity_new.this, "修正数值已达上限", Toast.LENGTH_SHORT).show();
                    }else{
                        etModifier.setText("" + tempCount);
                    }
                }
            }
        });

        btnModifierPositive = findViewById(R.id.btn_modifier_positive);
        //修改数量 增加按钮
        btnModifierPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llModifier.getVisibility() == View.VISIBLE) {
                    int tempCount = CommonUtils.parseInt(etModifier.getText().toString());
                    if(tempCount >= 0){
                        tempCount ++;
                    }

                    if ((tempCount - tempNum)  > 3) {
                        Toast.makeText(USBCameraActivity_new.this, "修正数值已达上限", Toast.LENGTH_SHORT).show();
                    }else {
                        etModifier.setText("" + tempCount);
                    }

                }
            }
        });

        //详情
        count_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(USBCameraActivity_new.this);
                View inflate = View.inflate(USBCameraActivity_new.this, R.layout.hog_result_layout, null);
                dialog.setView(inflate);
                ListView result_list_2 = inflate.findViewById(R.id.juan_list);
                TextView textView = inflate.findViewById(R.id.hog_sure);
                result_list_2.setAdapter(mJuanCountAdapter);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogcreate.dismiss();
                    }
                });
                dialogcreate = dialog.create();
                dialogcreate.setCanceledOnTouchOutside(false);
                dialogcreate.show();
            }
        });

    }


    private void uploadRecognitionResult(){
        String text = String.format("本次点数采集:\n" +
                        "合计%d圈 %d头 修正后%d头 时长%d秒\n" +
                        "上次点数采集:\n" +
                        "合计%s圈 %s头 修正后%s头 时长%s秒", mRecognitionResults.size(),mAutolCount.get(), mTotalCount.get(),
                (System.currentTimeMillis() - mStartTime) / 1000, mOldJuanCnt, mOldAutoCount,  mOldTotalCount, mOldDuration);

        AlertDialog.Builder builder = new AlertDialog.Builder(USBCameraActivity_new.this);
        LayoutInflater inflater = LayoutInflater.from(USBCameraActivity_new.this);
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
                synchronized (USBCameraActivity_new.this) {
                    dialog.dismiss();

                    mGoonButton.setVisibility(View.GONE);
                    mNextButton.setVisibility(View.GONE);
                    mTakePictureButton.setVisibility(View.VISIBLE);
                    mResultImageView.setVisibility(View.GONE);
                    llModifier.setVisibility(View.GONE);
                    etModifier.setText("");

                    CounterHelper.number = 1;
                    mRecognitionResults.clear();
                    mStartTime = System.currentTimeMillis();
                    mTotalCount.set(0);
                    mTotalCountTextView.setText("总数" + 0 + "头");
                    mJuanCountAdapter.reset();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPop();
                List<RecognitionResult> results = new ArrayList<>(mRecognitionResults);
                CounterHelper.uploadRecognitionResult(mSheId, mSheName, (int) ((System.currentTimeMillis() - mStartTime) / 1000),
                        results, USBCameraActivity_new.this, new CounterHelper.OnUploadResultListener() {
                            @Override
                            public void onCompleted(boolean succeed, String resutl) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pop.dismiss();
                                        if (succeed) {
                                            try {
                                                JSONObject jsonObject = new JSONObject(resutl);
                                                int status = jsonObject.getInt("status");
                                                String msg = jsonObject.getString("msg");
                                                if (status != 1) {
                                                    Toast.makeText(USBCameraActivity_new.this, "上传失败！"+msg, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(USBCameraActivity_new.this, "上传成功！", Toast.LENGTH_SHORT).show();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            finish();
                                                        }
                                                    }, 500);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Toast.makeText(USBCameraActivity_new.this, "上传失败！", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(USBCameraActivity_new.this, "上传失败！", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.v(TAG, "onStart:");
        mUSBMonitor.register();
        if (mUVCCameraView != null)
            mUVCCameraView.onResume();
        if (!mCameraHandler.isOpened()) {
            if (DEBUG) Log.v(TAG, "onStart:" + "------!isOpened-----");
            synchronized (USBCameraActivity_new.this) {
                CameraDialog.openCamera(USBCameraActivity_new.this);
            }
        } else {
            if (DEBUG) Log.v(TAG, "onStart:" + "-----isOpened------");
//            mCameraHandler.close();
//            setCameraButton(false);
        }
    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.v(TAG, "onStop:");
        mCameraHandler.close();
        if (mUVCCameraView != null)
            mUVCCameraView.onPause();
//        setCameraButton(false);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        if (mCameraHandler != null) {
            mCameraHandler.release();
            mCameraHandler = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraView = null;
        super.onDestroy();
    }

    /**
     * capture still image when you long click on preview image(not on buttons)
     */
    private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View view) {
            switch (view.getId()) {
                case R.id.camera_view:
                    if (mCameraHandler.isOpened()) {
                        if (checkPermissionWriteExternalStorage()) {
                            mCameraHandler.captureStill();
                        }
                        return true;
                    }
            }
            return false;
        }
    };

//    private void setCameraButton(final boolean isOn) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (mCameraButton != null) {
//                    try {
//                        mCameraButton.setOnCheckedChangeListener(null);
//                        mCameraButton.setChecked(isOn);
//                    } finally {
//                        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
//                    }
//                }
//                if (!isOn && (mCaptureButton != null)) {
//                    mCaptureButton.setVisibility(View.INVISIBLE);
//                }
//            }
//        }, 0);
//        updateItems();
//    }

    private void startPreview() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                if (st != null) {
                    mCameraHandler.startPreview(new Surface(st));
                } else {
                    if (DEBUG) Log.v(TAG, "SurfaceTexture==" + st);
                    startPreview();
                }
            }
        }, 500);
//        final SurfaceTexture  st = mUVCCameraView.getSurfaceTexture();
//        if (st != null) {
//            if (DEBUG) Log.v(TAG, "SurfaceTexture!=Null"+st);
//            mCameraHandler.startPreview(new Surface(st));
//        }else {
//            if (DEBUG) Log.v(TAG, "SurfaceTexture==Null"+st);
//            //startPreview();
//            wakeUpCamera();
//        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mCaptureButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            //Toast.makeText(USBCameraActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:");
            //Toast.makeText(USBCameraActivity.this, "USB_DEVICE_CONNECT", Toast.LENGTH_SHORT).show();
            mCameraHandler.open(ctrlBlock);
            startPreview();
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:");
            if (mCameraHandler != null) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        if (mCameraHandler != null) {
                            try {
                                mCameraHandler.close();
                            } catch (Exception e) {

                            }
                        }
                    }
                }, 0);
//                setCameraButton(false);
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            //Toast.makeText(USBCameraActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
//            setCameraButton(false);
        }
    };

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (DEBUG) Log.v(TAG, "onDialogResult:canceled=" + canceled);
        if (canceled) {
//            setCameraButton(false);
        }
    }

    //================================================================================
    private boolean isActive() {
        return mCameraHandler != null && mCameraHandler.isOpened();
    }

    private boolean checkSupportFlag(final int flag) {
        return mCameraHandler != null && mCameraHandler.checkSupportFlag(flag);
    }

    private int getValue(final int flag) {
        return mCameraHandler != null ? mCameraHandler.getValue(flag) : 0;
    }

    private int setValue(final int flag, final int value) {
        return mCameraHandler != null ? mCameraHandler.setValue(flag, value) : 0;
    }

    private int resetValue(final int flag) {
        return mCameraHandler != null ? mCameraHandler.resetValue(flag) : 0;
    }


    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
    }

    Long captureTime = Long.valueOf(0);

    /**
     * 画面改变时 进行相关
     * @param view
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {}

    @Override
    protected void onResume() {
        super.onResume();
    }

    private RecognitionView recognitionView;

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
    }

    private PopupWindow pop;

    private void showPop() {
        pop = new PopupWindow(getApplicationContext());
        View view = getLayoutInflater().inflate(R.layout.item_popupwindow, null);
        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);
        pop.showAtLocation(counter_activity, 0, 0, 0);
    }

    private synchronized void setCurrentBitmap(Bitmap bitmap) {
        mCurrentBitmap = bitmap;
    }

    private synchronized Bitmap getCurrentBitmap() {
        return mCurrentBitmap;
    }

    private synchronized void setCurrentResult(int autoCount, Bitmap bitmap, String fileName) {
        mCurrentRecognitionResult = new RecognitionResult(mRecognitionResults.size(), autoCount, bitmap, fileName);

        mCurrentRecognitionResult.lat = LocationManager_new.getInstance(this).currentLat;
        mCurrentRecognitionResult.lon = LocationManager_new.getInstance(this).currentLon;
        mJuanCountAdapter.setListner(new JuanCountAdapter.JuanInterface() {
            @Override
            public void getname(String jname) {
                mCurrentRecognitionResult.juanName = jname;
            }
        });
    }

    private synchronized RecognitionResult saveResultToList() {
        mCurrentRecognitionResult.setCount(CommonUtils.parseInt(etModifier.getText().toString().trim()));
        mRecognitionResults.add(mCurrentRecognitionResult);
        return mCurrentRecognitionResult;
    }

    private void wakeUpCamera() {
        mCameraHandler.close();
        if (mUVCCameraView != null)
            mUVCCameraView.onPause();
        mUSBMonitor.register();
        if (mUVCCameraView != null)
            mUVCCameraView.onResume();
        if (!mCameraHandler.isOpened()) {
            if (DEBUG) Log.v(TAG, "onStart:" + "------!isOpened-----");
            CameraDialog.openCamera(USBCameraActivity_new.this);
        } else {
            if (DEBUG) Log.v(TAG, "onStart:" + "-----isOpened------");
        }
    }
}