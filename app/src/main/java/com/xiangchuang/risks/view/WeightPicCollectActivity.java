package com.xiangchuang.risks.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.utils.ToastUtils;
import com.xiangchuang.risks.view.camera.CameraSurfaceView;
import com.xiangchuang.risks.view.camera.CameraUtils;
import com.xiangchuang.risks.view.camera.ImageUtils;
import com.xiangchuang.risks.view.camera.SpiritView;
import com.xiangchuangtec.luolu.animalcounter.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import innovation.utils.PathUtils;

/**
 * @Author: Lucas.Cui
 * 时   间：2019/1/29
 * 简   述：<功能简述>
 */
public class WeightPicCollectActivity extends BaseActivity implements SensorEventListener, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "WeightPicCollectActivit";
    @BindView(R.id.iv_preview)
    ImageView iv_preview;
    @BindView(R.id.btn_upload)
    ImageView btn_upload;
    @BindView(R.id.btn_finish)
    ImageView btn_finish;
    //定义水平仪的仪表盘
    @BindView(R.id.spiritwiew)
    SpiritView spiritwiew;
    @BindView(R.id.camera_surfaceview)
    CameraSurfaceView camera_surfaceview;
    //定义水平仪能处理的最大倾斜角度，超过该角度气泡直接位于边界
    private int MAX_ANGLE = 30;
    private static final String[] NEEDED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private static final int ACTION_REQUEST_PERMISSIONS = 1;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float timestamp;
    private float angle[] = new float[3];
    private String mFileDirectory, mFilePath;
    private int mOrientation;
    private boolean mSafeToTakePicture = true, mGrantedCameraRequested, isCanTakePic;

    public static void start(AppCompatActivity context) {
        Intent intent = new Intent(context, WeightPicCollectActivity.class);
        context.startActivityForResult(intent, 1);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_weight_piccollect;
    }

    @Override
    protected void initData() {
//        mFilePath = getIntent().getStringExtra("path");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new CameraSurfaceView(this);
        mFileDirectory = PathUtils.weightcollect;
        mFilePath = mFileDirectory + "/" + System.currentTimeMillis() + ".jpg";
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        camera_surfaceview.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {

        camera_surfaceview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (checkPermissions(NEEDED_PERMISSIONS)) {
            initCamera();
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        }
    }

    /**
     * 初始化View
     */
    private void initCamera() {
        mOrientation = CameraUtils.calculateCameraPreviewOrientation(WeightPicCollectActivity.this);
    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ACTION_REQUEST_PERMISSIONS:
                boolean isAllGranted = true;
                for (int grantResult : grantResults) {
                    isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
                }
                if (isAllGranted) {
                    mGrantedCameraRequested = true;
                    initCamera();
                } else {
                    Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGrantedCameraRequested) {
            CameraUtils.startPreview();
        }
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL, null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        CameraUtils.stopPreview();
        mSensorManager.unregisterListener(this, mSensor);
    }

    @Override
    protected void onStop() {
        super.onStop();
        CameraUtils.stopPreview();
    }


    /**
     * 拍照
     */
    private void takePicture() {
        CameraUtils.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                CameraUtils.startPreview();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (bitmap != null) {
                    bitmap = ImageUtils.getRotatedBitmap(bitmap, mOrientation);
                    btn_upload.setVisibility(View.VISIBLE);
                    iv_preview.setVisibility(View.VISIBLE);
                    iv_preview.setImageBitmap(bitmap);
                    btn_finish.setImageDrawable(getResources().getDrawable(R.mipmap.iv_clear));
                    File file = new File(mFileDirectory);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    try {
                        FileOutputStream fout = new FileOutputStream(mFilePath);
                        BufferedOutputStream bos = new BufferedOutputStream(fout);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        bos.flush();
                        bos.close();
                        fout.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                CameraUtils.startPreview();

                mSafeToTakePicture = true;
            }
        });
    }

    public void setResultData(String filePath) {
        Intent intent = new Intent();
        intent.putExtra("path", filePath);
        setResult(Activity.RESULT_OK, intent);
    }

    @OnClick({R.id.spiritwiew, R.id.btn_finish, R.id.btn_upload, R.id.camera_surfaceview})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.spiritwiew:
                if (btn_upload.getVisibility() == View.VISIBLE || !isCanTakePic) {
                    return;
                }
                if (mSafeToTakePicture) {
                    mSafeToTakePicture = false;
                    takePicture();
                }
                break;
            case R.id.btn_upload:
                if (btn_upload.getVisibility() != View.VISIBLE) {
                    ToastUtils.getInstance().showShort(this, "请先拍照");
                    return;
                }
                setResultData(mFilePath);
                finish();
                break;
            case R.id.btn_finish:
                if (btn_upload.getVisibility() == View.VISIBLE) {
                    btn_upload.setVisibility(View.GONE);
                    iv_preview.setVisibility(View.GONE);
                    btn_finish.setImageDrawable(getResources().getDrawable(R.mipmap.iv_round_back));
                } else {
                    setResultData("");
                    finish();
                }
                break;
            case R.id.camera_surfaceview:
                CameraUtils.doAutoFocus();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            setResultData("");
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (btn_upload.getVisibility() == View.VISIBLE) {
            spiritwiew.setColor(255);
            spiritwiew.postInvalidate();
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
// x,y,z分别存储坐标轴x,y,z上的加速度
            float x = event.values[0];

            float y = event.values[1];

            float z = event.values[2];
// 根据三个方向上的加速度值得到总的加速度值a
            float a = (float) Math.sqrt(x * x + y * y + z * z);

// 传感器从外界采集数据的时间间隔为10000微秒
//            System.out.println("magneticSensor.getMinDelay()-------->" + magneticSensor.getMinDelay());
// 加速度传感器的最大量程
//            System.out.println("event.sensor.getMaximumRange()-------->" + event.sensor.getMaximumRange());


        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

// 三个坐标轴方向上的电磁强度，单位是微特拉斯(micro-Tesla)，用uT表示，也可以是高斯(Gauss),1Tesla=10000Gauss
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

//从 x、y、z 轴的正向位置观看处于原始方位的设备，如果设备逆时针旋转，将会收到正值；否则，为负值

            if (timestamp != 0) {

// 得到两次检测到手机旋转的时间差（纳秒），并将其转化为秒

                final float dT = 1;//(event.timestamp - timestamp) * NS2S;

// 将手机在各个轴上的旋转角度相加，即可得到当前位置相对于初始位置的旋转弧度
                angle[0] += event.values[0] * dT;

                angle[1] += event.values[1] * dT;

                angle[2] += event.values[2] * dT;

                float anglex = (float) Math.toDegrees(angle[0]);

                float angley = (float) Math.toDegrees(angle[1]);

                float anglez = (float) Math.toDegrees(angle[2]);

            }
            timestamp = event.timestamp;
        } else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
//            values[0]: Azimuth(方位)，地磁北方向与y轴的角度，围绕z轴旋转(0到359)。0=North, 90=East, 180=South, 270=West 
//            values[1]: Pitch(俯仰),围绕X轴旋转(-180 to 180), 当Z轴向Y轴运动时是正值
//            values[2]: Roll(滚)，围绕Y轴旋转(-90 to 90)，当X轴向Z轴运动时是正值 -02,0.2
            float anglex = (float) (event.values[0]);

            float angley = (float) (event.values[1]);

            float anglez = (float) (event.values[2]);
            long curr_time = SystemClock.elapsedRealtime();
            long last_time = spiritwiew.getTag() == null ? 0 : (long) (spiritwiew.getTag());
            if (curr_time - last_time < 1000)
                return;
            if (angley > -10 && angley < 5 && anglez > -5 && anglez < 4) {
//                btn_take.setVisibility(View.VISIBLE);
                spiritwiew.setColor(255);
                isCanTakePic = true;
            } else {
                spiritwiew.setColor(150);
                isCanTakePic = false;
//                btn_take.setVisibility(View.GONE);
            }
//            tv_position.setText("x: " + (int) anglex + "  y: " + (int) angley + "   z: " + (int) anglez);
        }
        checkPosition(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void checkPosition(SensorEvent sensorEvent) {
        float values[] = sensorEvent.values;
        //获取传感器的类型
        int sensorType = sensorEvent.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ORIENTATION:
                //获取与Y轴的夹角
                float yAngle = values[1];
                //获取与Z轴的夹角
                float zAngle = values[2];
                //气泡位于中间时（水平仪完全水平）
                int x = (spiritwiew.getBack().getWidth() - spiritwiew.getBubble().getWidth()) / 2;
                int y = (spiritwiew.getBack().getHeight() - spiritwiew.getBubble().getHeight()) / 2;
                //如果与Z轴的倾斜角还在最大角度之内
                if (Math.abs(zAngle) <= MAX_ANGLE) {
                    //根据与Z轴的倾斜角度计算X坐标轴的变化值
                    int deltaX = (int) ((spiritwiew.getBack().getWidth() - spiritwiew.getBubble().getWidth()) / 2
                            * zAngle / MAX_ANGLE);
                    x += deltaX;
                }
                //如果与Z轴的倾斜角已经大于MAX_ANGLE，气泡应到最左边
                else if (zAngle > MAX_ANGLE) {
                    x = 0;
                }
                //如果与Z轴的倾斜角已经小于负的Max_ANGLE,气泡应到最右边
                else {
                    x = spiritwiew.getBack().getWidth() - spiritwiew.getBubble().getWidth();
                }

                //如果与Y轴的倾斜角还在最大角度之内
                if (Math.abs(yAngle) <= MAX_ANGLE) {
                    //根据与Z轴的倾斜角度计算X坐标轴的变化值
                    int deltaY = (int) ((spiritwiew.getBack().getHeight() - spiritwiew.getBubble().getHeight()) / 2
                            * yAngle / MAX_ANGLE);
                    y += deltaY;
                }
                //如果与Y轴的倾斜角已经大于MAX_ANGLE，气泡应到最下边
                else if (yAngle > MAX_ANGLE) {
                    y = spiritwiew.getBack().getHeight() - spiritwiew.getBubble().getHeight();
                }
                //如果与Y轴的倾斜角已经小于负的Max_ANGLE,气泡应到最上边
                else {
                    y = 0;
                }
//                int r = spiritwiew.getBack().getWidth() / 2;
//                double ypos = y < r ? r - y : y - r;
//                double xpos = x < r ? r - x : x - r;
//                //如果计算出来的X，Y坐标还位于水平仪的仪表盘之内，则更新水平仪气泡坐标
//                boolean isMove = Math.sqrt(Math.pow(xpos, 2) + Math.pow(ypos, 2)) < r;
//                Log.d("", "===============" + xpos + "  " + ypos + "  " + isMove + "   " + r);
//                boolean isMove = xpos * xpos + ypos * ypos <= r * r;
//                if (x == r || y == r || isMove) {
                if (true) {
                    spiritwiew.bubbleX = x;
                    spiritwiew.bubbleY = y;
                    //Toast.makeText(Spirit.this, "在仪表盘内", Toast.LENGTH_SHORT).show();
                }
                //通知组件更新
                spiritwiew.postInvalidate();
                //show.invalidate();
                break;
        }
    }
}
