package com.xiangchuangtec.luolu.animalcounter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.base.BaseActivity;
import innovation.utils.Toast;

import com.google.gson.Gson;

import com.xiangchuang.risks.model.bean.RecognitionResult;
import com.xiangchuang.risks.utils.CommonUtils;
import com.xiangchuang.risks.utils.CounterHelper;
import com.xiangchuang.risks.utils.PigPreferencesUtils;

import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.tensorflow.demo.env.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import innovation.crash.CrashHandler;
import innovation.location.LocationManager_new;
import innovation.media.DormNextInfoDialog;
import innovation.utils.DeviceUtil;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.selector.ResolutionSelectorsKt;
import io.fotoapparat.view.CameraView;

import static io.fotoapparat.log.LoggersKt.fileLogger;
import static io.fotoapparat.log.LoggersKt.logcat;
import static io.fotoapparat.log.LoggersKt.loggers;
import static io.fotoapparat.selector.FlashSelectorsKt.autoFlash;
import static io.fotoapparat.selector.FlashSelectorsKt.autoRedEye;
import static io.fotoapparat.selector.FlashSelectorsKt.off;
import static io.fotoapparat.selector.FlashSelectorsKt.torch;
import static io.fotoapparat.selector.FocusModeSelectorsKt.autoFocus;
import static io.fotoapparat.selector.FocusModeSelectorsKt.continuousFocusPicture;
import static io.fotoapparat.selector.FocusModeSelectorsKt.fixed;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;
import static io.fotoapparat.selector.ResolutionSelectorsKt.highestResolution;
import static io.fotoapparat.selector.SelectorsKt.firstAvailable;


public class CounterActivity_new extends BaseActivity implements View.OnClickListener, SurfaceHolder.Callback {
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private static Logger mlogger = new Logger(CounterActivity_new.class.getName());
    private String date = new SimpleDateFormat("yyyy年MM月dd日HH时mm分").format(new Date());
    private static final int SELECT_FILE = 1;
    private static String TAG = "CounterActivity";
    private static final int PERMISSIONS_REQUEST = 1;
    final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_VIDEO = Manifest.permission.RECORD_AUDIO;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String PERMISSION_PHONE = Manifest.permission.READ_PHONE_STATE;

    boolean hasCameraPermission;
    private CameraView cameraView;
    private static Fotoapparat camera;
    private static Button takePictureButton;
    //private PigNumDetector pigNumDetector = null;
    //    luolu
    // private static DormNumInfoDialog dormNumInfoDialog;
    private static DormNextInfoDialog dormNextInfoDialog;
    //private FarmInfoDialog farmInfoDialog;
    private String imei;
    private ProgressDialog mProgressDialog;
    private int tempDormNum = 1;
    private CrashHandler crashHandler;
    private FileOutputStream outStream = null;
    private byte[] gpsBytes;
    private byte[] imeiBytes;
    private File pigNumber;
    private String timeStamp;
    String editFarmInfo;
    private View.OnClickListener dormNumStartListener;
    //    private View.OnClickListener dormNumExitListener;
    private int sum = 0;
    private StringBuilder sbDormNumAndCount;
    ResultDetailFragment resultDetailFragment;
    private int maxCount;
    private Gson gson;
    private String dataResult;
    private Button mCountCompleted;
    private AlertDialog dialogcreate;

    TextView mcountname;
    TextView mTotalCountTextView;
    LinearLayout counter_activity;
    ListView juan_list;

    private String recodetitle;
    private String recodenumber;

    private List<String> stringadapter1;
    private String no;
    private String zhusheselectid;
    private String zhujuanid;
    private String tou;

    private String zhusheselect;
    private String sel;
    private List<String> newList;
    private List<String> newzhusheidList;
    private String zhusheidone;
    private String zhujuanidone;
    private String zhushename;
    private String zhujuanname;
    private Map<String, List<Map<String, Integer>>> zhusheMap = new HashMap<>();
    private ImageView mResultImageView;
    private Button goon_button;
    // private File file;
    private PopupWindow pop;
    private Button mNextButton;
    public int juanfleg = 0;
    private ImageView imageView;
    private SurfaceView mSurfaceview;
    private Camera camera1;
    private boolean mStartedFlg = false;//是否正在录像
    private boolean mIsPlay = false;//是否正在播放录像
    private MediaRecorder mRecorder;
    private File videoFile;
    private String videoFileName;
    private Camera.Parameters mParam;
    private String saveVideoPath;
    private boolean isRecorder;
    private boolean granted = false;
    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码
    private File dataDir;
    private int anInt;
    private final MyPreviewProcessor mPreviewProcessor = new MyPreviewProcessor();
    private final AtomicInteger mTotalCount = new AtomicInteger(0);

    private final AtomicInteger mAutolCount = new AtomicInteger(0);
    private String mSheId;
    private String mSheName;
    private String mOldTotalCount;
    private String mOldAutoCount;
    private String mOldJuanCnt;
    private String mOldDuration;
    private long mStartTime;

    private final List<RecognitionResult> mRecognitionResults = new ArrayList<>();
    private RecognitionResult mCurrentRecognitionResult;
    private JuanCountAdapter mJuanCountAdapter;
    private Button count_detail;


    private LinearLayout llModifier;
    private Button btnModifierMinus;
    private Button btnModifierPositive;
    private EditText etModifier;
    //记录每次识别获取的头数
    private int tempNum = 0;

    private Dialog dialog;

    public void goToActivity(Class activity, Bundle bundle) {
        Intent intent = new Intent(this, activity);
        //携带数据
        if (bundle != null && bundle.size() != 0) {
            intent.putExtra("data", bundle);
        }
        startActivity(intent);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceview = null;
        mSurfaceHolder = null;
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
            Log.d(TAG, "surfaceDestroyed release mRecorder");
        }
        if (camera1 != null) {
            camera1.release();
            camera1 = null;
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_counter_new;
    }

    @Override
    protected void initData() {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"WrongViewCast", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        luolu
        showProgressDialog(CounterActivity_new.this);

        mcountname = findViewById(R.id.count_name);
        mTotalCountTextView  = findViewById(R.id.total_count);
        counter_activity = findViewById(R.id.counter_activity);
        juan_list = findViewById(R.id.juan_list);

        hasCameraPermission = permissionsDelegate.hasCameraPermission();
        Intent intent = getIntent();
        recodetitle = intent.getStringExtra("recodetitle");
        recodenumber = intent.getStringExtra("recodenumber");
        no = intent.getStringExtra("no");

        cameraView = findViewById(R.id.camera_view);
        mResultImageView = findViewById(R.id.recogn);
        mSurfaceview = (SurfaceView) findViewById(R.id.sufaceview);
        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.addCallback(this);
        mSheId = intent.getStringExtra("sheid");
        mSheName = intent.getStringExtra("shename");
        mOldTotalCount = intent.getStringExtra("pigcount");
        mOldAutoCount = intent.getStringExtra("autocount");
        mOldJuanCnt = intent.getStringExtra("juancnt");
        mOldDuration = intent.getStringExtra("duration");
        mStartTime = System.currentTimeMillis();
        mcountname.setText(mSheName);

        llModifier = findViewById(R.id.ll_modifier);

        etModifier = findViewById(R.id.tv_modifier);


        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        dataDir = new File("/sdcard/innovation/animal/image/"
                + "/");
        if (null != dataDir && dataDir.exists()) {
            delAllFile("/sdcard/innovation/animal/image/");
        }
        File newvideo = new File("/sdcard/innovation/animal/image/"
                + "/");
        if (!newvideo.exists()) {
            newvideo.mkdirs();
        }
        videoFileName = newvideo.getAbsolutePath() + "/video.mp4";
        videoFile = new File(videoFileName);
        try {
            videoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        camera = createFotoapparat();
        if (hasCameraPermission) {
            cameraView.setVisibility(View.VISIBLE);
        } else {
            permissionsDelegate.requestCameraPermission();
        }


        //本次详情
        count_detail = findViewById(R.id.count_detail);
        count_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(CounterActivity_new.this);
                View inflate = View.inflate(CounterActivity_new.this, R.layout.hog_result_layout, null);
                ListView result_list_2 = inflate.findViewById(R.id.juan_list);
                TextView textView = inflate.findViewById(R.id.hog_sure);
                result_list_2.setAdapter(mJuanCountAdapter);
                dialog.setView(inflate);
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
        //点数完毕
        mCountCompleted = findViewById(R.id.count_completed);
        mCountCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (CounterActivity_new.this) {

                    //最后清点的猪圈尚未保存
                    if (goon_button.getVisibility() == View.VISIBLE && mNextButton.getVisibility() == View.VISIBLE) {
                        // "最后清点的猪圈尚未保存，请先选择\n[重新采集]或[下一圈]";
                        //保存当前圈几
                        int tempJuanNum = CounterHelper.number;
                        int tempEt = CommonUtils.parseInt(etModifier.getText().toString().trim());
                        String text = String.format("圈%d信息尚未保存:\n" +
                                "自动识别%d头 修正后%d头\n" +
                                "完成盘查前请先选择[保存]或[放弃]后完成盘查", tempJuanNum, tempNum, tempEt);

                        AlertDialog.Builder builder = new AlertDialog.Builder(CounterActivity_new.this);

                        LayoutInflater inflater = LayoutInflater.from(CounterActivity_new.this);
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

                                CounterHelper.number += 1;
                                RecognitionResult result = saveResultToList();
                                Bitmap temp = CounterHelper.drawModifierBitmap(result.getBitmap(), String.valueOf(result.count));
                                result.setBitmap(temp);
                                mJuanCountAdapter.addResult(result);
                                mNextButton.setVisibility(View.GONE);
                                goon_button.setVisibility(View.GONE);
                                mResultImageView.setVisibility(View.GONE);
                                takePictureButton.setVisibility(View.VISIBLE);
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
                    if (mRecognitionResults.size() > 0) {
                        uploadRecognitionResult();
                    } else {
                        Toast.makeText(CounterActivity_new.this, "您还未清点猪舍", Toast.LENGTH_LONG).show();
                    }


                }
            }
        });
        //识别
        takePictureButton = findViewById(R.id.take_picture_button);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitmap = mPreviewProcessor.getCurrentImage();
                mResultImageView.setVisibility(View.GONE);
                if (bitmap == null) {
                    return;
                }
                showPop();
                CounterHelper.recognitionFromNet(CounterActivity_new.this,bitmap, new CounterHelper.OnImageRecognitionListener() {
                    @Override
                    public void onCompleted(int count, Bitmap bitmap, String time) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pop.dismiss();
                                if (count >= 0) {
                                    tempNum = count;
                                    mResultImageView.setVisibility(View.VISIBLE);
                                    mResultImageView.setImageBitmap(bitmap);
                                    takePictureButton.setVisibility(View.GONE);
                                    goon_button.setVisibility(View.VISIBLE);
                                    mNextButton.setVisibility(View.VISIBLE);
                                    llModifier.setVisibility(View.VISIBLE);
                                    etModifier.setText(count + "");
                                    setCurrentResult(count, bitmap, null, time);
                                } else {
                                    Toast.makeText(CounterActivity_new.this, "识别失败！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });
        //重新采集
        goon_button = findViewById(R.id.goon_button);
        goon_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goon_button.setVisibility(View.GONE);
                mNextButton.setVisibility(View.GONE);
                takePictureButton.setVisibility(View.VISIBLE);
                mResultImageView.setVisibility(View.GONE);
                llModifier.setVisibility(View.GONE);
                etModifier.setText("");
            }
        });
        mJuanCountAdapter = new JuanCountAdapter(this);
        juan_list.setAdapter(mJuanCountAdapter);
        //下一圈
        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CounterHelper.number += 1;

                RecognitionResult result = saveResultToList();
                Bitmap temp = CounterHelper.drawModifierBitmap(result.getBitmap(), String.valueOf(result.count));
                result.setBitmap(temp);
                mJuanCountAdapter.addResult(result);
                mNextButton.setVisibility(View.GONE);
                goon_button.setVisibility(View.GONE);
                mResultImageView.setVisibility(View.GONE);
                takePictureButton.setVisibility(View.VISIBLE);
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
                    if (tempCount > 0) {
                        tempCount--;
                    }
                    if ((tempNum - tempCount) > 3) {
                        Toast.makeText(CounterActivity_new.this, "修正数值已达上限", Toast.LENGTH_SHORT).show();
                    } else {
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
                    if (tempCount >= 0) {
                        tempCount++;
                    }

                    if ((tempCount - tempNum) > 3) {
                        Toast.makeText(CounterActivity_new.this, "修正数值已达上限", Toast.LENGTH_SHORT).show();
                    } else {
                        etModifier.setText("" + tempCount);
                    }

                }
            }
        });

/*        etModifier.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0){
                    String temp = etModifier.getText().toString().trim();
                    int tempC = CommonUtils.parseInt(temp);
                    setModifierCount(tempC);
                }
            }
        });*/


        imei = DeviceUtil.getImei(getApplicationContext());
        CrashHandler.getInstance().init(getApplicationContext());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sbDormNumAndCount = new StringBuilder();
    }

    private void uploadRecognitionResult() {
        String text = String.format("本次点数采集:\n" +
                        "合计%d圈 %d头 修正后%d头 时长%d秒\n" +
                        "上次点数采集:\n" +
                        "合计%s圈 %s头 修正后%s头 时长%s秒", mRecognitionResults.size(), mAutolCount.get(), mTotalCount.get(),
                (System.currentTimeMillis() - mStartTime) / 1000, mOldJuanCnt, mOldAutoCount, mOldTotalCount, mOldDuration);

        AlertDialog.Builder builder = new AlertDialog.Builder(CounterActivity_new.this);
        LayoutInflater inflater = LayoutInflater.from(CounterActivity_new.this);
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
                synchronized (CounterActivity_new.this) {
                    dialog.dismiss();
                    goon_button.setVisibility(View.GONE);
                    mNextButton.setVisibility(View.GONE);
                    takePictureButton.setVisibility(View.VISIBLE);
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
                dialog.dismiss();
                showProgressDialog(CounterActivity_new.this);
                mProgressDialog.show();
                List<RecognitionResult> results = new ArrayList<>(mRecognitionResults);
                CounterHelper.uploadRecognitionResult(mSheId, mSheName, (int) ((System.currentTimeMillis() - mStartTime) / 1000),
                        results, CounterActivity_new.this, new CounterHelper.OnUploadResultListener() {
                            @Override
                            public void onCompleted(boolean succeed, String resutl) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        if (succeed) {
                                            try {
                                                JSONObject jsonObject = new JSONObject(resutl);
                                                int status = jsonObject.getInt("status");
                                                String msg = jsonObject.getString("msg");
                                                if (status != 1) {
                                                    Toast.makeText(CounterActivity_new.this, "上传失败！"+msg, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(CounterActivity_new.this, "上传成功！", Toast.LENGTH_SHORT).show();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            finish();
                                                        }
                                                    }, 500);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Toast.makeText(CounterActivity_new.this, "上传失败！", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(CounterActivity_new.this, "上传失败！", Toast.LENGTH_SHORT).show();
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    Fotoapparat createFotoapparat() {
        Fotoapparat camera;
        camera = Fotoapparat
                .with(this)
                .into(cameraView)           // view which will draw the camera preview
                .previewScaleType(ScaleType.CenterCrop)  // we want the preview to fill the view
                .photoResolution(
                        highestResolution()
                )  // we want to have the biggest photo possible
                .previewResolution(ResolutionSelectorsKt.highestResolution())
//                .previewResolution(firstAvailable(
//                        standardRatio(highestResolution())
//                ))
                .lensPosition(back())       // we want back camera
                .focusMode(firstAvailable(continuousFocusPicture(),
                        autoFocus(),
                        fixed()
                ))
                .flash(firstAvailable(      // (optional) similar to how it is done for focus mode, this time for flash
                        autoRedEye(),
                        autoFlash(),
                        torch(),
                        off()
                ))
//                .sensorSensitivity(highestSensorSensitivity())
                .previewFpsRange(fpsRanges -> fpsRanges.iterator().next())
                .frameProcessor(mPreviewProcessor)
//                        .getBuilder()
//                        .detector(pigNumDetector)
//                        .listener(recognitions
//                                -> recognitionView.setRecognitions(recognitions))
//                        .build())   // (optional) receives each frame from preview stream
                .logger(loggers(            // (optional) we want to log camera events in 2 places at once
                        logcat(),           // ... in logcat
                        fileLogger(this)    // ... and to file
                ))
                .build();
        return camera;
    }

    private SurfaceHolder mSurfaceHolder;


    @Override
    public void onStart() {
        super.onStart();
        if (hasCameraPermission) {
            camera.start();
        }

        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (hasCameraPermission) {
            camera.stop();
        }
    }

    @Override
    public void onPause() {
        mProgressDialog.dismiss();
        super.onPause();


    }

    @Override
    public void onDestroy() {
       /* if (dormNumInfoDialog.isShowing()) {
            dormNumInfoDialog.dismiss();
        }*/
        if (null != dormNextInfoDialog && dormNextInfoDialog.isShowing()) {
            dormNextInfoDialog.dismiss();
        }
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
      /*  if (farmInfoDialog.isShowing()) {
            farmInfoDialog.dismiss();
        }*/
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        //显示头部信息
        String fullname = PigPreferencesUtils.getStringValue("fullname", CounterActivity_new.this);
        tou = fullname + recodetitle;
        //mcountname.setText(tou);
        mTotalCountTextView.setText("总数:" + mTotalCount.get() + "头");
        //设置默认显示值

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
        }
    }


    public static int getRotation(int frameRotation) {
        int rotation = 0;
        switch (frameRotation) {
            case 270:
                rotation = 90;
                break;
            case 180:
                rotation = 180;
                break;
            case 90:
                rotation = 270;
                break;
            default:
                break;
        }
        return rotation;
    }

    public static Matrix getTransformationMatrix(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation) {
        final Matrix matrix = new Matrix();

        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                Log.w(TAG, "Rotation of " + applyRotation + " % 90 != 0");
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;

        final int inWidth = transpose ? srcHeight : srcWidth;
        final int inHeight = transpose ? srcWidth : srcHeight;

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            final float scaleFactorX = dstWidth / (float) inWidth;
            final float scaleFactorY = dstHeight / (float) inHeight;
            final float scaleFactor = Math.min(scaleFactorX, scaleFactorY);
            matrix.postScale(scaleFactor, scaleFactor);
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;
    }

    private void showProgressDialog(Context context) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);//false


        mProgressDialog.setCanceledOnTouchOutside(false);//false
        mProgressDialog.setIcon(R.drawable.pig_ic_launcher);
//        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "确定", mProgClickListener);
        mProgressDialog.setMessage("正在识别......");
//        mProgressDialog.show();
//        Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
//        if (positive != null) {
//            positive.setVisibility(View.GONE);
//        }
    }

    public static File getSrcImageDir(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        File dataDir = new File("/sdcard/innovation/animal/image/" + timeStamp);
        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                Log.w(TAG, "Unable to create external cache directory");
                return null;
            }
        }
        return dataDir;
    }

    @Override
    public void onClick(View v) {

    }


    public class MyPreviewProcessor implements FrameProcessor {
        private String TAG = "PreviewDetectionProcessor";
        private String date = new SimpleDateFormat("yyyy年MM月dd日HH时mm分").format(new Date());
        private Logger mlogger = new Logger();
        private Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());
        private static final float MINIMUM_CONFIDENCE = 0.85f;
        //  private PigNumDetector pigNumDetector;
        private YuvImage mCurrentYuvImage;

        public MyPreviewProcessor() {
            //  pigNumDetector = PigNumDetector.get(PigAppConfig.getAppContext());
        }

        private synchronized void setYunImage(YuvImage image) {
            mCurrentYuvImage = image;
        }

        public synchronized Bitmap getCurrentImage() {
            Bitmap bitmap = null;
            if (mCurrentYuvImage != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                mCurrentYuvImage.compressToJpeg(new Rect(0, 0, mCurrentYuvImage.getWidth(), mCurrentYuvImage.getHeight()),
                        50, out);
                byte[] bytes = out.toByteArray();
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                int rotation = Utils.getRotation(270);
                if (rotation != 0) {
                    Matrix matrix = new Matrix();
                    matrix.setRotate(rotation);
                    bitmap = Bitmap.createBitmap(
                            bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                }
            }
            return bitmap;
        }

        @Override
        public void process(Frame frame) {
            int width = frame.getSize().width;
            int height = frame.getSize().height;
            YuvImage image = new YuvImage(frame.getImage(), ImageFormat.NV21, width, height, null);
            setYunImage(image);
        }
    }

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    //删除文件夹
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRecord() {
        if (isRecorder) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        if (camera1 == null) {
            Log.i(TAG, "Camera is null");
            stopRecord();
            return;
        }
        camera1.unlock();
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        mRecorder.reset();
        mRecorder.setCamera(camera1);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

      /*  if (mParam == null) {
            mParam = camera1.getParameters();
        }*/

        // mRecorder.setVideoSize(videoSize.width, videoSize.height);

        mRecorder.setMaxDuration(10000);
        mRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        videoFileName = "video_" + System.currentTimeMillis() + ".mp4";
        saveVideoPath = Environment.getExternalStorageDirectory().getPath();
        Log.i("====path=====", saveVideoPath + "/" + videoFileName);
        mRecorder.setOutputFile(videoFileName);
        try {
            mRecorder.prepare();
            mRecorder.start();
            isRecorder = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        if (mStartedFlg) {
            try {
                mRecorder.stop();
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
                if (camera1 != null) {
                    camera1.release();
                    camera1 = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mStartedFlg = false;
    }

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

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            int size = 0;
            if (grantResults.length >= 1) {
                int writeResult = grantResults[0];
                //读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (!writeGranted) {
                    size++;
                }
                //录音权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                //相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }
                if (size == 0) {
                    granted = true;
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
                    camera.start();
                    cameraView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private synchronized void setCurrentResult(int autoCount, Bitmap bitmap, String fileName, String time) {
        mCurrentRecognitionResult = new RecognitionResult(mRecognitionResults.size(), autoCount, bitmap, fileName, time);
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
}

