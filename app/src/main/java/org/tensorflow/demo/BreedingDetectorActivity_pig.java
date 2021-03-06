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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Trace;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import innovation.utils.Toast;

import com.innovation.pig.insurance.R;
import com.xiangchuangtec.luolu.animalcounter.PigAppConfig;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;

import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.env.BorderedText_Breeding;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker_Breeding;

import java.util.LinkedList;
import java.util.List;

import innovation.biz.classifier.BreedingPigFaceDetectTFlite;
import static org.tensorflow.demo.Global.dilogIsShowing;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class BreedingDetectorActivity_pig extends BreedingCameraActivity_pig implements OnImageAvailableListener {
    private static final String TAG = "BreedingDetectActivity";
    private static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged multibox model.

    private static final int MB_INPUT_SIZE = 128;

    private static final int YOLO_INPUT_SIZE = 416;
    // Default to the included multibox model.
    private static final boolean USE_YOLO = false;

    private static final int CROP_SIZE = USE_YOLO ? YOLO_INPUT_SIZE : MB_INPUT_SIZE;

    // Minimum detection confidence to track a detection.
    private static final float TEXT_SIZE_DIP = 10;

    private Integer sensorOrientation;

    private byte[][] yuvBytes;
    private int[] rgbBytes = null;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    public static MultiBoxTracker_Breeding tracker;

    private byte[] luminance;

    private BorderedText_Breeding borderedText;
    //haojie add
    private boolean imageok = true;//图像良好标识
    private String imageErrMsg = "";

    private String sheId;
    private String inspectNo;
    private String reason;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private BreedingPigFaceDetectTFlite pigTFliteDetector;

    private int previewWidth = 0;
    private int previewHeight = 0;

    private int imageCounter = 0;
    private int imageDarkCounter = 0;//图像过暗次数
    private int imageBlurCounter = 0;//图像模糊次数
    private int imageBrightCounter = 0;//图像过亮次数

    private static final int CHECK_COUNTER = 20; //允许的最大错误图像次数   //定义提示类型
    //end add
    private int imageCount = 10;// add for test
    private static final int TFLITE_INPUT_SIZE = 300;
    private static final boolean TFLITE_IS_QUANTIZED = true;
    private static final String PIG_TFLITE_DETECT_MODEL_FILE = "detect_02.tflite";

    public static int offsetX;
    public static int offsetY;

    private static long last_toast_time = 0;
    private static long internalTime;
    public static void start(Activity context) {
        //延时2s，防止重复启动页面
        if(System.currentTimeMillis() - internalTime > 2000){
            Intent intent = new Intent(context, BreedingDetectorActivity_pig.class);
            context.startActivity(intent);
        }
        internalTime = System.currentTimeMillis();
    }

    @Override
    protected void initData() {

    }

    @Override
    public synchronized void onResume() {
        if(!dilogIsShowing){
            PigAppConfig.sowCount = 0;
            PigAppConfig.lastXmin = 0f;
            Intent intent = getIntent();
            sheId = intent.getStringExtra(Constants.sheId);
            inspectNo = intent.getStringExtra(Constants.inspectNo);
            reason = intent.getStringExtra(Constants.reason);
        }
        super.onResume();

    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        Log.i("====== " ,"===再次=======");
        if (sheId != null && inspectNo != null && reason != null) {
            mFragment.setParmes(sheId,inspectNo, reason);
        }
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText_Breeding(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);
        tracker = new MultiBoxTracker_Breeding(this);

        //猪脸识别
        try {
            pigTFliteDetector =
                    BreedingPigFaceDetectTFlite.create(
                            getAssets(),
                            PIG_TFLITE_DETECT_MODEL_FILE,
                            "",
                            TFLITE_INPUT_SIZE,
                            TFLITE_IS_QUANTIZED);
        } catch (final Exception e) {
            throw new RuntimeException("Error initializing pig TensorFlowLite!", e);
        }
        /*
        try {
            detector = MediaProcessor.getInstance(getApplicationContext()).getFaceDetector_new();
        } catch (final Exception e) {
            throw new RuntimeException("Error initializing TensorFlow!", e);
        }*/

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        LOGGER.i("previewWidth: " + previewWidth);
        LOGGER.i("previewHeight: " + previewHeight);

        final Display display = getWindowManager().getDefaultDisplay();
        final int screenOrientation = display.getRotation();

        LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation);
        // 20180223
        sensorOrientation = rotation - getScreenOrientation();

        LOGGER.i("Initializing sensorOrientation: %d", sensorOrientation);
        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbBytes = new int[previewWidth * previewHeight];
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);

        croppedBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight, previewWidth, previewHeight, screenOrientation, true);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
        yuvBytes = new byte[3][];

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(new DrawCallback() {
            @Override
            public void drawCallback(final Canvas canvas) {
//                mLocationTracker.draw(canvas);
                tracker.draw(canvas, 1);
            }
        });
    }

    public static OverlayView trackingOverlay;

    private long endTime = 0L;
    @Override
    public void onImageAvailable(final ImageReader reader) {

        Image image = null;
        ++timestamp;
        final long currTimestamp = timestamp;
        try {
            image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
            Trace.beginSection("imageAvailable");
            final Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);

            tracker.onFrame(
                    previewWidth,
                    previewHeight,
                    planes[0].getRowStride(),
                    90,
                    yuvBytes[0],
                    timestamp);

            //mLocationTracker.onFrame(previewWidth, previewHeight, planes[0].getRowStride(), sensorOrientation, yuvBytes[0], timestamp);
            trackingOverlay.postInvalidate();

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            //Log.d("DetectorActivity_pig.java", "haojie---dark test ----------------");

            ImageUtils.convertYUV420ToARGB8888(
                    yuvBytes[0],
                    yuvBytes[1],
                    yuvBytes[2],
                    rgbBytes,
                    previewWidth,
                    previewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    false);

            image.close();
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            LOGGER.e(e, "Exception!");
            Trace.endSection();
            return;
        }

        if (System.currentTimeMillis() - endTime < 200) {
            Log.e(TAG, "onImageAvailable: 时间=="+ (System.currentTimeMillis() - endTime));
            return;
        }
        endTime = System.currentTimeMillis();

        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        if (luminance == null) {
            luminance = new byte[yuvBytes[0].length];
        }
        System.arraycopy(yuvBytes[0], 0, luminance, 0, luminance.length);

        final Paint paint = new Paint();
        Bitmap padBitmap = null;

        // 检测图片是否清晰，给出提示，用于用户调整
        imageErrMsg = "";

        if (!Global.VIDEO_PROCESS) {
            return;
        }
        long checkImageStar = System.currentTimeMillis();
        Log.e(TAG, "checkImageStar: "+checkImageStar);
        // 图像质量检查
        checkImageQuality(croppedBitmap);
        Log.e(TAG, "checkImageEnd: "+(System.currentTimeMillis()-checkImageStar));
        if (imageok) {
            long pidStar = System.currentTimeMillis();
            Log.e(TAG, "pidStar: "+pidStar);
            Bitmap rotateBitmap = innovation.utils.ImageUtils.rotateBitmap(croppedBitmap, 90);

            //com.innovation.utils.ImageUtils.saveImage(rotateBitmap);
            padBitmap = innovation.utils.ImageUtils.padBitmap(rotateBitmap);

            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);
            int croppedBitmapHeight = croppedBitmap.getHeight();
            int croppedBitmapWidth = croppedBitmap.getWidth();
            int padSize = padBitmap.getWidth();
            offsetX = (padSize - croppedBitmapWidth) / 2;
            offsetY = (padSize - croppedBitmapHeight) / 2;
            Log.e(TAG, "pidEnd: "+(System.currentTimeMillis()-pidStar));
        } else {
            LOGGER.i("图像质量不合格！" + "不合格原因：" + imageErrMsg);
            return;
        }


        Log.d(TAG, "猪分类器");
        long gotoDetectStar = System.currentTimeMillis();
        Log.e(TAG, "gotoDetectStar: "+gotoDetectStar );
        pigTFliteDetector.pigRecognitionAndPostureItemTFlite(padBitmap);
        if (BreedingPigFaceDetectTFlite.recognitionAndPostureItem != null) {

            long trackAnimalResultsStar = System.currentTimeMillis();
            tracker.trackAnimalResults(BreedingPigFaceDetectTFlite.recognitionAndPostureItem.getPostureItem());

            final List<BreedingPigFaceDetectTFlite.Recognition> mappedRecognitions =  new LinkedList<BreedingPigFaceDetectTFlite.Recognition>();

            if (BreedingPigFaceDetectTFlite.recognitionAndPostureItem.getList() != null) {

                Log.e("ListSize", "--"+ BreedingPigFaceDetectTFlite.recognitionAndPostureItem.getList().size());
                BreedingPigFaceDetectTFlite.Recognition recognition;
                long neixunhuanStar = System.currentTimeMillis();
                for (final BreedingPigFaceDetectTFlite.Recognition result : BreedingPigFaceDetectTFlite.recognitionAndPostureItem.getList()) {

                    final RectF location = result.getLocation();
                    Log.e("RectF", "RectF: " + location);
                    if (location != null) {
                        canvas.drawRect(location, paint);

                        Matrix tempMatrix = new Matrix();
                        tempMatrix.invert(cropToFrameTransform);
                        tempMatrix.postRotate(270, 0, 0);
                        tempMatrix.postTranslate(0, previewHeight);

                        tempMatrix.mapRect(location);

                        recognition = new BreedingPigFaceDetectTFlite.Recognition(
                                "",
                                "pigLite",
                                result.getConfidence(),
                                location, null);

//                        resultStatus.setLocation(location);
                        mappedRecognitions.add(recognition);
                    }
                }
                Log.e("mappedRecognitions", "mappedRecognitions "+mappedRecognitions.size());
                tracker.trackResults(mappedRecognitions, luminance, currTimestamp);
            }
        }
        Log.e(TAG, "gotoDetectEnd: "+(System.currentTimeMillis()-gotoDetectStar));

        trackingOverlay.postInvalidate();
        Trace.endSection();


    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking_breedingpig;
    }

    @Override
    protected int getDesiredPreviewFrameSize() {
        return CROP_SIZE;
    }

    //检测图片质量
    private void checkImageQuality(Bitmap bitmap) {
        //检测图片质量
        int bright = innovation.utils.ImageUtils.checkImageBright(bitmap);
        boolean ifDark = false;
        boolean ifBright = false;
        boolean isBlur = false;
        imageCounter++;
        if (bright > 180) {
            ifBright = true;
            imageBrightCounter++;
            Log.d(TAG, "图像过亮，请重新选择" + "--bright ===" + bright);
            imageErrMsg += "图像过亮！" + "--bright ===" + bright;
        } else if (bright < 40) {
            ifDark = true;
            imageDarkCounter++;
            Log.d(TAG, "图像过暗，请重新选择" + "--bright ===" + bright);
            imageErrMsg += "图像过暗！" + "--intensityValue ===" + bright;
        } else {

            isBlur = innovation.utils.ImageUtils.isBlurByOpenCV_new(bitmap);
            if (isBlur) {
                imageBlurCounter++;
                Log.d(TAG, "图像模糊，请重新选择" + "--isblur ===" + imageBlurCounter);
                imageErrMsg += "图像模糊！" + "--isblur ===" + isBlur;
            }
        }

        if (!ifDark && !ifBright && !isBlur) {
            imageok = true;
            Log.d(TAG, "图像 质量良好 imageok ===" + imageok);
        } else {
            imageok = false; //图片质量有问题，不进行捕捉图片的保存
            Log.d(TAG, "图像 质量差 imageok ===" + imageok);
        }
        checkImageresult();
    }

    private void checkImageresult() {
        if(imageCounter < CHECK_COUNTER){
            return;
        }
        //判断图片质量，用于图片错误提示（过亮、过暗、模糊）

        String tipMsg = "";
        double tmpOk = CHECK_COUNTER * 0.6;
        double tmpDark = CHECK_COUNTER * 0.7;
        double tmpBright = CHECK_COUNTER * 0.7;
        double tmpBlur = CHECK_COUNTER * 0.9;

        if (imageDarkCounter > tmpDark) {
            tipMsg = "当前环境光线过暗，不利于采集";
        }else if (imageBrightCounter > tmpBright) {
            tipMsg = "当前环境光线过亮，不利于采集";
        } else if (imageBlurCounter > tmpBlur) {
            tipMsg = "采集图像模糊，请调整拍摄距离";
        }

        if ((tipMsg.length() > 0)) {
            Log.d(TAG, tipMsg + "== 图片不可用" + last_toast_time);
            if(System.currentTimeMillis() - last_toast_time > 5000) {
                Log.d(TAG, "DetectorActivity_pig.parent = " + BreedingDetectorActivity_pig.this );

                String finalTipMsg = tipMsg;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BreedingDetectorActivity_pig.this, finalTipMsg, Toast.LENGTH_SHORT).show();
                        last_toast_time = System.currentTimeMillis();
                    }
                });
            }
        } else {
            Log.d(TAG, "--set_checkcount---tmpok==" + tmpOk + "==图片可用");
        }
        initImageCheckPara();
    }

    private void initImageCheckPara() {
        imageCounter = 0;
        imageDarkCounter = 0;
        imageBlurCounter = 0;
        imageBrightCounter = 0;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            Log.i("detonKeyDown:", "返回");
            boolean b = moveTaskToBack(false);
            finish();
            Log.i("detonKeyDown:", "==" + b);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "返回");
    }
}
