package com.xiangchuang.risks.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.PreferencesUtils;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.view.camera.CameraSurfaceView;
import com.xiangchuang.risks.view.camera.CameraUtils;
import com.xiangchuang.risks.view.camera.ImageUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import innovation.location.LocationManager_new;
import innovation.utils.PathUtils;
import innovation.utils.UIUtils;


/**
 * @Author: Lucas.Cui
 * 时   间：2019/4/9
 * 简   述：<功能简述>
 */
public class CameraPicActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener, View.OnClickListener {
    private static final String TAG = "CameraPicActivity";
    private ImageView iv_preview, btn_takepic;
    private FrameLayout framelayout;
    private CameraSurfaceView camera_surfaceview;
    private TextView btn_finish, tv_date, tv_longitude, tv_latitude, tv_position;
    private static final String[] NEEDED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private static final int ACTION_REQUEST_PERMISSIONS = 1;
    private String mFileDirectory, mFilePath;
    private int mOrientation;
    private boolean mSafeToTakePicture = true, mGrantedCameraRequested;

    public static void start(Activity context) {
        Intent intent = new Intent(context, CameraPicActivity.class);
        context.startActivityForResult(intent, 1);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_camerapic;
    }

    @Override
    public void initView() {
        super.initView();
        iv_preview = findViewById(R.id.iv_preview);
        btn_takepic = findViewById(R.id.btn_takepic);
        btn_finish = findViewById(R.id.btn_finish);
        framelayout = findViewById(R.id.framelayout);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_longitude = (TextView) findViewById(R.id.tv_longitude);
        tv_latitude = (TextView) findViewById(R.id.tv_latitude);
        tv_position = (TextView) findViewById(R.id.tv_position);
        btn_takepic.setOnClickListener(this);
        btn_finish.setOnClickListener(this);

    }

    @Override
    protected void initData() {
//        mFilePath = getIntent().getStringExtra("path");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String dateString = formatter.format(new Date());
        tv_date.setText(dateString);
        tv_longitude.setText("经度：" + PreferencesUtils.getStringValue(Constants.longitude, AppConfig.getAppContext()));
        tv_latitude.setText("纬度：" + PreferencesUtils.getStringValue(Constants.latitude, AppConfig.getAppContext()));
        tv_position.setText("位置：" + LocationManager_new.getInstance(CameraPicActivity.this).str_address);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileDirectory = PathUtils.weightcollect;
        mFilePath = mFileDirectory + "/" + System.currentTimeMillis() + ".jpg";
        framelayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        CameraUtils.setPreviewHeight(UIUtils.getHeightPixels(this));
        CameraUtils.setPreviewWidth(UIUtils.getWidthPixels(this));
    }

    @Override
    public void onGlobalLayout() {

        framelayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
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
        camera_surfaceview = new CameraSurfaceView(CameraPicActivity.this);
        framelayout.addView(camera_surfaceview);
        camera_surfaceview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraUtils.doAutoFocus();
            }
        });
        mOrientation = CameraUtils.calculateCameraPreviewOrientation(CameraPicActivity.this);
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
    }

    @Override
    protected void onPause() {
        super.onPause();

        CameraUtils.stopPreview();
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
        CameraUtils.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                if (bitmap != null) {
                    bitmap = ImageUtils.getRotatedBitmap(bitmap, mOrientation);
                    bitmap = innovation.utils.ImageUtils.compressBitmap(bitmap);
                    iv_preview.setVisibility(View.VISIBLE);
                    iv_preview.setImageBitmap(bitmap);
//                    btn_finish.setText("重拍");
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
                        setResultData(mFilePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                mSafeToTakePicture = true;
            }
        });
    }

    public void setResultData(String filePath) {
        Intent intent = new Intent();
        intent.putExtra("path", filePath);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btn_takepic) {
            if (mSafeToTakePicture) {
                mSafeToTakePicture = false;
                takePicture();
            }

        } else if (i == R.id.btn_finish) {
            finish();

        } else  {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


}
