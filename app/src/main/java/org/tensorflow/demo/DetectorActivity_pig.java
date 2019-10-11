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

import com.innovation.pig.insurance.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;

import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import innovation.biz.classifier.NewFaceDetectTFlite;
import innovation.biz.classifier.NewKeyPointsDetectTFlite;
import innovation.utils.Toast;

import static com.xiangchuangtec.luolu.animalcounter.Utils.getThreshold;


/**
 * on 2018/6/17.
 */

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity_pig extends CameraActivity_pig implements OnImageAvailableListener {
    private static final String TAG = "DetectorActivity_pig";
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

    private Classifier detector;

    private byte[][] yuvBytes;
    private int[] rgbBytes = null;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    public static MultiBoxTracker tracker;

    private byte[] luminance;

    private BorderedText borderedTextPig;

    private boolean imageok = true;//图像良好标识
    private String imageErrMsg = "";

    private String sheId;
    private String inspectNo;
    private String reason;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);

    private Classifier pigTFliteDetector;

    private int previewWidth = 0;
    private int previewHeight = 0;

    private int imageCounter = 0;
    private int imageOkCounter = 0;  //图像良好次数
    private int imageDarkCounter = 0;//图像过暗次数
    private int imageBlurCounter = 0;//图像模糊次数
    private int imageBrightCounter = 0;//图像过亮次数

    private static final int CHECK_COUNTER = 20; //允许的最大错误图像次数   //定义提示类型
    //end add
    private int imageCount = 10;// add for test
    private static final int TFLITE_INPUT_SIZE = 192;
    private static final boolean TFLITE_IS_QUANTIZED = true;
    //0510ssd_mobilenet_v2_focal_quantized_80000_201905130926.tflite //518展示用app 模型
    //0307ssd_mobilenet_v2_detect.tflite
    //diepig_20190418.tflite
    private static final String PIG_TFLITE_DETECT_MODEL_FILE = "diepig_20190418.tflite";


    public static int type1Count = 0;
    public static int type2Count = 0;
    public static int type3Count = 0;
    public static int AngleTrackType = 0;
    public static int offsetX;
    public static int offsetY;

    private static long last_toast_time = 0;

    private Bitmap cropCopyBitmap;

    //大于标准值
    private boolean aboveStandard = false;
    //不能识别状态下 保存的原图
    private int saveImgOricount = 0;

    //记录进检测模型的图片总数量
    public static int allNumber = 0;
    //记录进畜脸识别模型成功的图片数量
    public static int dNumber = 0;
    //记录进角度模型成功的图片数量
    public static int aNumber = 0;
    //记录进关键点模型成功的图片数量
    public static int kNumber = 0;

    //记录总时间
    public static long allTime = 0;
    //记录检测时间
    public static long dTime = 0;
    //记录角度时间
    public static long aTime = 0;
    //记录关键点时间
    public static long kTime = 0;

    //记录检测失败次数
    public static int dTimes = 0;
    //记录角度失败次数
    public static int aTimes = 0;
    //记录关键点失败次数
    public static int kTimes = 0;

    public static int preWidth;

    @Override
    protected void initData() {

    }

    @Override
    public synchronized void onResume() {

        allNumber = 0;
        dNumber = 0;
        aNumber = 0;
        kNumber = 0;
        dTimes = 0;
        aTimes = 0;
        kTimes = 0;
        allTime = 0;
        resetParameter();

        super.onResume();
        Intent intent = getIntent();
        sheId = intent.getStringExtra(Constants.sheId);
        inspectNo = intent.getStringExtra(Constants.inspectNo);
        reason = intent.getStringExtra(Constants.reason);
    }

    public static void resetParameter() {
        dTime = 0;
        aTime = 0;
        kTime = 0;
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        Log.i("====== ", "===再次=======");

        if (sheId != null && inspectNo != null && reason != null) {
            mFragment.setParmes(sheId, inspectNo, reason);
        }
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedTextPig = new BorderedText(textSizePx);
        borderedTextPig.setTypeface(Typeface.MONOSPACE);

        //猪脸识别
        try {
            pigTFliteDetector =
                    NewFaceDetectTFlite.create(
                            getAssets(),
                            PIG_TFLITE_DETECT_MODEL_FILE,
                            "",
                            TFLITE_INPUT_SIZE,
                            TFLITE_IS_QUANTIZED);
            getThreshold();
        } catch (final Exception e) {
            throw new RuntimeException("Error initializing pig TensorFlowLite!", e);
        }
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
                tracker.draw(canvas, 1);
                if (isDebug()) {
                    tracker.drawDebug(canvas);
                }
            }
        });

        addCallback(new DrawCallback() {
            @Override
            public void drawCallback(final Canvas canvas) {
                if (!isDebug()) {
                    return;
                }
                final Bitmap copy = cropCopyBitmap;
                if (copy == null) {
                    return;
                }

                final int backgroundColor = Color.argb(100, 0, 0, 0);
                canvas.drawColor(backgroundColor);

                final Matrix matrix = new Matrix();
                final float scaleFactor = 2;
                matrix.postScale(scaleFactor, scaleFactor);
                matrix.postTranslate(
                        canvas.getWidth() - copy.getWidth() * scaleFactor,
                        canvas.getHeight() - copy.getHeight() * scaleFactor);
                canvas.drawBitmap(copy, matrix, new Paint());

                final Vector<String> lines = new Vector<String>();
                if (detector != null) {
                    final String statString = detector.getStatString();
                    final String[] statLines = statString.split("\n");
                    for (final String line : statLines) {
                        lines.add(line);
                    }
                }
                lines.add("");

                lines.add("Frame: " + previewWidth + "x" + previewHeight);
                lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
                lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
                lines.add("Rotation: " + sensorOrientation);


                borderedTextPig.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
            }
        });
    }

    public static OverlayView trackingOverlay;

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Log.i("====== ", "===onImageAvailable=======");
        Image image = null;
        ++timestamp;
        final long currTimestamp = timestamp;
        try {
            image = reader.acquireLatestImage();
            if (image == null) {
                Log.i("====== ", "===onImageAvailable1=======");
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
            Log.i("====== ", "===onImageAvailable4=======");
            return;
        }

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
            Log.i("====== ", "===onImageAvailable3=======");
            return;
        }
        allTime = System.currentTimeMillis();
        //图像质量检查
        checkImageQuality(croppedBitmap);
        preWidth = croppedBitmap.getWidth();

        Bitmap rotateBitmap;
        if (imageok) {
            //图片resize 长边960
            Bitmap rCroppedBitmap = resizeBitmap(croppedBitmap);

            //图片旋转90度
            rotateBitmap = innovation.utils.ImageUtils.rotateBitmap(rCroppedBitmap, 90);

            //com.innovation.utils.ImageUtils.saveImage(rotateBitmap);
            //给图片边界添加填充
            padBitmap = innovation.utils.ImageUtils.padBitmap(rotateBitmap);
            cropCopyBitmap = Bitmap.createBitmap(padBitmap);

            //final Canvas canvas = new Canvas(cropCopyBitmap);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);
            int croppedBitmapHeight = croppedBitmap.getHeight();
            int croppedBitmapWidth = croppedBitmap.getWidth();
            int padSize = padBitmap.getWidth();
            offsetX = (padSize - croppedBitmapWidth) / 2;
            offsetY = (padSize - croppedBitmapHeight) / 2;
        } else {
            LOGGER.i("图像质量不合格！" + "不合格原因：" + imageErrMsg);
            return;
        }

        Log.d(TAG, "猪脸分类器");
        pigTFliteDetector.pigRecognitionAndPostureItemTFlite(padBitmap, rotateBitmap);
        if (NewFaceDetectTFlite.pigTFliteRecognitionAndPostureItem != null) {
            Log.i("====== ", "===onImageAvailable7=======");
            tracker.trackAnimalResults(NewFaceDetectTFlite.pigTFliteRecognitionAndPostureItem.getPostureItem(), NewKeyPointsDetectTFlite.pigPredictAngleTypeR);
            final List<Classifier.Recognition> mappedRecognitions = new LinkedList<Classifier.Recognition>();
            if (NewFaceDetectTFlite.pigTFliteRecognitionAndPostureItem.getList() != null) {
                for (final Classifier.Recognition result : NewFaceDetectTFlite.pigTFliteRecognitionAndPostureItem.getList()) {

                    Log.i("====== ", "===onImageAvailable8=======");
                    final RectF location = result.getLocation();
                    Log.e("RectF", "RectF: " + location);
                    if (location != null) {
                        Log.i("====== ", "===onImageAvailable9=======");
                        canvas.drawRect(location, paint);

                        Matrix tempMatrix = new Matrix();
                        tempMatrix.invert(cropToFrameTransform);
                        tempMatrix.postRotate(270, 0, 0);
                        tempMatrix.postTranslate(0, previewHeight);

                        tempMatrix.mapRect(location);
                        result.setLocation(location);
                        mappedRecognitions.add(result);
                    }
                }
                Log.i("====== ", "===onImageAvailableA=======");
                tracker.trackResults(mappedRecognitions, luminance, currTimestamp);
            }
        }
        trackingOverlay.postInvalidate();
        requestRender();
        Trace.endSection();
    }

    @Override
    protected int getLayoutId() {
        //return R.layout.camera_connection_fragment_tracking; //haojie del for test
        return R.layout.camera_connection_fragment_tracking_new;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onSetDebug(final boolean debug) {
        pigTFliteDetector.enableStatLogging(debug);
    }

    public void reInitCurrentCounter(int a, int b, int c) {
        type1Count = a;
        type2Count = b;
        type3Count = c;
        LOGGER.i("reInitCurrentCounter-a:", type1Count);
        LOGGER.i("reInitCurrentCounter-b:", type2Count);
        LOGGER.i("reInitCurrentCounter-c:", type3Count);
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
            imageOkCounter++;
            Log.d(TAG, "图像 质量良好 imageok ===" + imageok);
        } else {
            imageok = false; //图片质量有问题，不进行捕捉图片的保存
            Log.d(TAG, "图像 质量差 imageok ===" + imageok);
        }
        checkImageresult();
    }

    private void checkImageresult() {
        if (imageCounter < CHECK_COUNTER) {
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
        } else if (imageBrightCounter > tmpBright) {
            tipMsg = "当前环境光线过亮，不利于采集";
        } else if (imageBlurCounter > tmpBlur) {
            tipMsg = "采集图像模糊，请调整拍摄距离";
        }

        if ((tipMsg.length() > 0)) {
            Log.d(TAG, tipMsg + "== 图片不可用" + last_toast_time);
            if (System.currentTimeMillis() - last_toast_time > 5000) {
                Log.d(TAG, "DetectorActivity_pig.parent = " + DetectorActivity_pig.this);

                String finalTipMsg = tipMsg;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetectorActivity_pig.this, finalTipMsg, Toast.LENGTH_SHORT).show();
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
        imageOkCounter = 0;
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


    private static Bitmap resizeBitmap(Bitmap bitmap) {
        // 图片按比例压缩，以长边=192为准
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = 1f;

        int max = Math.max(width, height);
        if (max > 960) {
            scale = 960f / max;
        }
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return newbm;
    }
}
