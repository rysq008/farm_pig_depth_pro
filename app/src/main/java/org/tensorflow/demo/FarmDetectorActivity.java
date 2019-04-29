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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
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
import android.widget.Toast;



import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.biz.classifier.CowFaceDetectTFlite;
import com.farm.innovation.biz.classifier.CowRotationPrediction;
import com.farm.innovation.biz.classifier.DonkeyFaceDetectTFlite;
import com.farm.innovation.biz.classifier.DonkeyRotationPrediction;
import com.farm.innovation.biz.classifier.PigFaceDetectTFlite;
import com.farm.innovation.biz.classifier.PigRotationPrediction;
import com.farm.innovation.biz.classifier.YakFaceDetectTFlite;
import com.farm.innovation.biz.classifier.YakRotationPrediction;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.login.Utils;
import com.farm.innovation.utils.ConstUtils;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.FileUtils;
import com.innovation.pig.insurance.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.FarmMultiBoxTracker;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static com.farm.innovation.base.FarmAppConfig.lastCurrentTime;
import static com.farm.innovation.login.Utils.getThreshold;
import static com.farm.innovation.login.Utils.setLowThreshold;
import static com.farm.innovation.utils.ImageUtils.compressBitmap;
import static org.tensorflow.demo.FarmCameraConnectionFragment.collectNumberHandler;


/**
 * @author luolu on 2018/6/17.
 */

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class FarmDetectorActivity extends FarmCameraActivity implements OnImageAvailableListener {
    private static final String TAG = "FarmDetectorActivity";
    private static final Logger LOGGER = new Logger();
    private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);
    private static final float TEXT_SIZE_DIP = 10;
    private Integer sensorOrientation;
    private FarmClassifier detector;
    private FarmClassifier donkeyTFliteDetector;
    private FarmClassifier cowTFliteDetector;
    private FarmClassifier pigTFliteDetector;

    private FarmClassifier yakTFliteDetector;

    private int previewWidth = 0;
    private int previewHeight = 0;
    private byte[][] yuvBytes;
    private int[] rgbBytes = null;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private Bitmap cropCopyBitmap;

    public static FarmMultiBoxTracker tracker;

    private byte[] luminance;

    private BorderedText borderedText;

    private long lastProcessingTimeMs;


    private int imageCounter = 0;
    private int imageOkCounter = 0;  //图像良好次数
    private int imageDarkCounter = 0;//图像过暗次数
    private int imageBlurCounter = 0;//图像模糊次数
    private int imageBrightCounter = 0;//图像过亮次数
    private boolean imageok = true;//图像良好标识
    private String imageErrMsg = "";
    private static final int CHECK_COUNTER = 20; //允许的最大错误图像次数   //定义提示类型
    private static final boolean MAINTAIN_ASPECT = false;
    //end add
    private int imageCount = 10;// add for test
    private static final int TFLITE_INPUT_SIZE = 192;
    private static final boolean TFLITE_IS_QUANTIZED = true;
    private static final String DONKEY_TFLITE_DETECT_MODEL_FILE = "donkey_1216_silv_tf10_192_detect.tflite";//"donkey_detection_ssdlite_mobilenet_v2_focal_192_uint8_1019.tflite";
    private static final String COW_TFLITE_DETECT_MODEL_FILE = "cow_detect_1029_tf10.tflite";
    private static final String PIG_TFLITE_DETECT_MODEL_FILE = "pig_1026_detect_xincai_addbg.tflite";
    //牦牛TF文件
    private static final String YAK_TFLITE_DETECT_MODEL_FILE = "yak_detect_0320.tflite";
    int[] intValues = new int[192 * 192];
    public static int type1Count = 0;
    public static int type2Count = 0;
    public static int type3Count = 0;
    public static int AngleTrackType = 0;
    public static int offsetX;
    public static int offsetY;

    private static long last_toast_time = 0;

    //大于标准值
    private boolean aboveStandard = false;

    //记录进检测模型的图片总数量
    public static int allNumber = 0 ;
    //记录进畜脸识别模型成功的图片数量
    public static int dNumber = 0 ;
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

    @Subscribe
    @Override
    public synchronized void onResume() {
        allNumber = 0 ;
        dNumber = 0 ;
        aNumber = 0;
        kNumber = 0;
        dTimes = 0;
        aTimes = 0;
        kTimes = 0;
        allTime = 0;
        resetParameter();

        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void initData() {

    }

    public static void resetParameter(){
        dTime = 0;
        aTime = 0;
        kTime = 0;
    }

    @Subscribe
    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onReceiveEventBus(String str) {
        if (str.equals("finish")) {
            FarmDetectorActivity.this.finish();
//            Toast.makeText(this, "----->finish!!!!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        Log.i(TAG, "onPreviewSizeChosen start");
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new FarmMultiBoxTracker(this);

        try {
            int animalType = FarmerPreferencesUtils.getAnimalType(FarmDetectorActivity.this);
            if (animalType == ConstUtils.ANIMAL_TYPE_PIG) {
                pigTFliteDetector =
                        PigFaceDetectTFlite.create(
                                getAssets(),
                                PIG_TFLITE_DETECT_MODEL_FILE,
                                "",
                                TFLITE_INPUT_SIZE,
                                TFLITE_IS_QUANTIZED);
            } else if (animalType == ConstUtils.ANIMAL_TYPE_DONKEY) {
                donkeyTFliteDetector =
                        DonkeyFaceDetectTFlite.create(
                                getAssets(),
                                DONKEY_TFLITE_DETECT_MODEL_FILE,
                                "",
                                TFLITE_INPUT_SIZE,
                                TFLITE_IS_QUANTIZED);

            } else if (animalType == ConstUtils.ANIMAL_TYPE_CATTLE) {
                long startTime = System.currentTimeMillis(); // 获取开始时间
                // doThing(); // 测试的代码段
                cowTFliteDetector =
                        CowFaceDetectTFlite.create(
                                getAssets(),
                                COW_TFLITE_DETECT_MODEL_FILE,
                                "",
                                TFLITE_INPUT_SIZE,
                                TFLITE_IS_QUANTIZED);

            } else if(animalType == ConstUtils.ANIMAL_TYPE_YAK){
                long startTime = System.currentTimeMillis(); // 获取开始时间
                // doThing(); // 测试的代码段
                yakTFliteDetector =
                        YakFaceDetectTFlite.create(
                                getAssets(),
                                YAK_TFLITE_DETECT_MODEL_FILE,
                                "",
                                TFLITE_INPUT_SIZE,
                                TFLITE_IS_QUANTIZED);
            }
            getThreshold();
        } catch (final Exception e) {
            throw new RuntimeException("Error initializing donkey,cow,pig TensorFlowLite!", e);
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        LOGGER.i("previewWidth: " + previewWidth);
        LOGGER.i("previewHeight: " + previewHeight);

        final Display display = getWindowManager().getDefaultDisplay();
        final int screenOrientation = display.getRotation();

        LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation);

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
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas, FarmerPreferencesUtils.getAnimalType(FarmDetectorActivity.this));
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        addCallback(
                new OverlayView.DrawCallback() {
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
                        lines.add("Inference time: " + lastProcessingTimeMs + "ms");

                        borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
                    }
                });

        Log.i(TAG, "onPreviewSizeChosen end");
    }

    public static OverlayView trackingOverlay;

    private int saveImgOricount = 0;

    @Override
    public void onImageAvailable(final ImageReader reader) {

        Image image = null;
        ++timestamp;
        final long currTimestamp = timestamp;

        //CowFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(FarmerPreferencesUtils.getStringValue(THRESHOLD_COWLIPEI1, FarmAppConfig.getActivity()));

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

            trackingOverlay.postInvalidate();

            // No mutex needed as this method is not reentrant.

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
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

        if (!FarmGlobal.VIDEO_PROCESS) {
            return;
        }

        allTime = System.currentTimeMillis();
        // 图像质量检查
        checkImageQuality(croppedBitmap);
        Log.e("时间收集", "图片质量检测时间: " + (System.currentTimeMillis()-allTime ));
        preWidth = croppedBitmap.getWidth();


        // TODO: 2018/10/18 By:LuoLu
        Bitmap rotateBitmap;
        if (imageok) {
            //图片resize 长边960
            Bitmap rCroppedBitmap = resizeBitmap(croppedBitmap);

            //图片旋转90度
            rotateBitmap = com.farm.innovation.utils.ImageUtils.rotateBitmap(rCroppedBitmap, 90);
            //com.farm.innovation.utils.ImageUtils.saveImage(rotateBitmap);
            //给图片边界添加填充
            padBitmap = com.farm.innovation.utils.ImageUtils.padBitmap(rotateBitmap);

            cropCopyBitmap = Bitmap.createBitmap(padBitmap);

            //final Canvas canvas = new Canvas(cropCopyBitmap);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);
            int croppedBitmapHeight = croppedBitmap.getHeight();
            int croppedBitmapWidth = croppedBitmap.getWidth();
            offsetX = (preWidth - croppedBitmapWidth) / 2;
            offsetY = (preWidth - croppedBitmapHeight) / 2;
            Log.e("时间收集高", "padBitmap"+padBitmap.getHeight());
            Log.e("时间收集宽", "padBitmap"+padBitmap.getWidth());

        } else {
            LOGGER.i("图像质量不合格！" + "不合格原因：" + imageErrMsg);
            return;
        }

        if (FarmGlobal.model == Model.VERIFY.value()) {
//            int a = 30;
//            int b = 30;
//            int n = 120;
//            int m = 240;
            int a = FarmerPreferencesUtils.getIntValue(FarmAppConfig.lipeia, 30, FarmAppConfig.getActivity());
            int b = FarmerPreferencesUtils.getIntValue(FarmAppConfig.lipeib, 30, FarmAppConfig.getActivity());
            int n = FarmerPreferencesUtils.getIntValue(FarmAppConfig.lipein, 120, FarmAppConfig.getActivity());
            int m = FarmerPreferencesUtils.getIntValue(FarmAppConfig.lipeim, 240, FarmAppConfig.getActivity());

            int pastSeconds = 5;
            //获取当前时间戳
            long c = System.currentTimeMillis();

            // 未达到判定图片数量是否达标时
            long duringTime = Utils.getDuring(c) / 1000;
            if (!aboveStandard) {
                aboveStandard = (Utils.getDuring(c) / 1000) > a;
            } else {
                //图片数量未达标,且（初次保存图片，或距离上次保存图片时间超过5秒）
                if (Utils.notUpToStandard(c, pastSeconds) && (lastCurrentTime == 0 || (c - lastCurrentTime) > pastSeconds * 1000)) {
                    if (saveImgOricount < 15) {
                        //存图
                        //保存原图
                        File file = new File(FarmGlobal.mediaPayItem.getOriBitmapFileName());
                        FileUtils.saveBitmapToFile(compressBitmap(rotateBitmap), file);
                        saveImgOricount++;
                        Log.i("图片数量未达标", duringTime + ":" + String.valueOf(c));
                        //保存当前存图的时间戳
                        lastCurrentTime = c;
                    }
                }
            }

            /**
             * b时间后自动降低拍摄的阈值
             * FarmAppConfig.lipeib
             */
            if ((Utils.getDuring(c) / 1000) > b) {
                setLowThreshold();
            }

            /*
             * m时间后 停止拍摄弹出强制上传
             * FarmAppConfig.lipeim
             */
            if ((Utils.getDuring(c) / 1000) > m && FarmAppConfig.debugNub >= 0) {
                FarmAppConfig.debugNub = 2;
                collectNumberHandler.sendEmptyMessage(6);
                if (FarmAppConfig.isApkDebugable())
                    Toast.makeText(this, "m时间后 停止拍摄弹出强制上传", Toast.LENGTH_LONG).show();
                return;
            }

            /*
             * n时间后 停止拍摄弹出是否强制上传或重新拍摄
             * FarmAppConfig.lipein
             */
            if ((Utils.getDuring(c) / 1000) > n && FarmAppConfig.debugNub != 1 && FarmAppConfig.debugNub >= 0) {
                FarmAppConfig.debugNub = 1;
                collectNumberHandler.sendEmptyMessage(6);
                if (FarmAppConfig.isApkDebugable())
                    Toast.makeText(this, " n时间后 停止拍摄弹出是否强制上传或重新拍摄", Toast.LENGTH_LONG).show();
            }
        }

        dTime = System.currentTimeMillis();
        int animalType = FarmerPreferencesUtils.getAnimalType(FarmDetectorActivity.this);
        if (animalType == ConstUtils.ANIMAL_TYPE_PIG) {
            Log.d(TAG, "猪脸分类器");
            pigTFliteDetector.pigRecognitionAndPostureItemTFlite(padBitmap, rotateBitmap);
            if (PigFaceDetectTFlite.pigTFliteRecognitionAndPostureItem != null) {
                tracker.trackAnimalResults(PigFaceDetectTFlite.pigTFliteRecognitionAndPostureItem.getPostureItem(), PigRotationPrediction.pigPredictAngleType);
                final List<FarmClassifier.Recognition> mappedRecognitions =
                        new LinkedList<FarmClassifier.Recognition>();
                if (PigFaceDetectTFlite.pigTFliteRecognitionAndPostureItem.getList() != null) {
                    for (final FarmClassifier.Recognition result : PigFaceDetectTFlite.pigTFliteRecognitionAndPostureItem.getList()) {
                        final RectF location = result.getLocation();
                        if (location != null) {
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
                    tracker.trackResults(mappedRecognitions, luminance, currTimestamp);
                }
            }
        } else if (animalType == ConstUtils.ANIMAL_TYPE_CATTLE) {
            Log.d(TAG, "牛脸分类器");
            cowTFliteDetector.cowRecognitionAndPostureItemTFlite(padBitmap, rotateBitmap);
            if (CowFaceDetectTFlite.cowRecognitionAndPostureItemTFlite != null) {
                tracker.trackAnimalResults(CowFaceDetectTFlite.cowRecognitionAndPostureItemTFlite.getPostureItem(), CowRotationPrediction.cowPredictAngleType);
                final List<FarmClassifier.Recognition> mappedRecognitions =
                        new LinkedList<FarmClassifier.Recognition>();
                if (CowFaceDetectTFlite.cowRecognitionAndPostureItemTFlite.getList() != null) {
                    for (final FarmClassifier.Recognition result : CowFaceDetectTFlite.cowRecognitionAndPostureItemTFlite.getList()) {
                        final RectF location = result.getLocation();
                        if (location != null) {
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
                    tracker.trackResults(mappedRecognitions, luminance, currTimestamp);
                }
            }
        } else if (animalType == ConstUtils.ANIMAL_TYPE_DONKEY) {
            Log.d(TAG, "驴脸分类器");
            donkeyTFliteDetector.donkeyRecognitionAndPostureItemTFlite(padBitmap, rotateBitmap);
            if (DonkeyFaceDetectTFlite.donkeyRecognitionAndPostureItemTFlite != null) {
                tracker.trackAnimalResults(DonkeyFaceDetectTFlite.donkeyRecognitionAndPostureItemTFlite.getPostureItem(), DonkeyRotationPrediction.donkeyPredictAngleType);
                final List<FarmClassifier.Recognition> mappedRecognitions =
                        new LinkedList<FarmClassifier.Recognition>();
                if (DonkeyFaceDetectTFlite.donkeyRecognitionAndPostureItemTFlite.getList() != null) {
                    for (final FarmClassifier.Recognition result : DonkeyFaceDetectTFlite.donkeyRecognitionAndPostureItemTFlite.getList()) {
                        final RectF location = result.getLocation();
                        List<Point> points = result.getPoints();
                        if (location != null) {
                            canvas.drawRect(location, paint);

                            Matrix tempMatrix = new Matrix();
                            tempMatrix.invert(cropToFrameTransform);
                            tempMatrix.postRotate(270, 0, 0);
                            tempMatrix.postTranslate(0, previewHeight);

                            tempMatrix.mapRect(location);
                            result.setLocation(location);
                            result.setPoints(points);
                            mappedRecognitions.add(result);
                        }
                    }
                    tracker.trackResults(mappedRecognitions, luminance, currTimestamp);
                }
            }
        }else if (animalType == ConstUtils.ANIMAL_TYPE_YAK) {
            Log.d(TAG, "牦牛脸分类器");
            yakTFliteDetector.yakRecognitionAndPostureItemTFlite(padBitmap, rotateBitmap);
            if (YakFaceDetectTFlite.yakRecognitionAndPostureItemTFlite != null) {
                tracker.trackAnimalResults(YakFaceDetectTFlite.yakRecognitionAndPostureItemTFlite.getPostureItem(), YakRotationPrediction.yakPredictAngleType);
                final List<FarmClassifier.Recognition> mappedRecognitions =
                        new LinkedList<FarmClassifier.Recognition>();
                if (YakFaceDetectTFlite.yakRecognitionAndPostureItemTFlite.getList() != null) {
                    for (final FarmClassifier.Recognition result : YakFaceDetectTFlite.yakRecognitionAndPostureItemTFlite.getList()) {
                        final RectF location = result.getLocation();
                        if (location != null) {
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
                    tracker.trackResults(mappedRecognitions, luminance, currTimestamp);
                }
            }
        }
        trackingOverlay.postInvalidate();
        requestRender();
        Trace.endSection();
    }

    //检测图片质量
    private void checkImageQuality(Bitmap bitmap) {
        //检测图片质量
        Object[] objs = com.farm.innovation.utils.ImageUtils.checkImageBright(bitmap);
        int bright = (int) objs[0];
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
            isBlur = com.farm.innovation.utils.ImageUtils.isBlurByOpenCV_YXS((Bitmap) objs[1]);
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
                Log.d(TAG, "FarmDetectorActivity.parent = " + FarmDetectorActivity.this);

                String finalTipMsg = tipMsg;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FarmDetectorActivity.this, finalTipMsg, Toast.LENGTH_SHORT).show();
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
    protected int getLayoutId() {

        return R.layout.farm_camera_connection_fragment_tracking_new;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onSetDebug(final boolean debug) {
        int animalType = FarmerPreferencesUtils.getAnimalType(FarmDetectorActivity.this);
        if (animalType == ConstUtils.ANIMAL_TYPE_PIG) {
            pigTFliteDetector.enableStatLogging(debug);
        } else if (animalType == ConstUtils.ANIMAL_TYPE_DONKEY) {
            donkeyTFliteDetector.enableStatLogging(debug);
        } else if (animalType == ConstUtils.ANIMAL_TYPE_CATTLE) {
            long startTime = System.currentTimeMillis(); // 获取开始时间
            // doThing(); // 测试的代码段
            cowTFliteDetector.enableStatLogging(debug);
        }
    }

    public void reInitCurrentCounter(int a, int b, int c) {
        type1Count = a;
        type2Count = b;
        type3Count = c;
        LOGGER.i("reInitCurrentCounter-a :%d", type1Count);
        LOGGER.i("reInitCurrentCounter-b :%d", type2Count);
        LOGGER.i("reInitCurrentCounter-c :%d", type3Count);
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
