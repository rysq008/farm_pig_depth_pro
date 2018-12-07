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
 *//*


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

import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker;
import org.tensorflow.demo.tracking.MultiBoxTracker_new;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import innovation.utils.ConstUtils;


*/
/**
 * @author luolu on 2018/6/17.
 *//*


*/
/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 *//*

public class DetectorActivity_new extends CameraActivity implements OnImageAvailableListener {
    private static final String TAG = "DetectorActivity";
    private static final Logger LOGGER = new Logger();
    private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);
    private static final float TEXT_SIZE_DIP = 10;
    private Integer sensorOrientation;
    private Classifier detector;
    private Classifier donkeyTFliteDetector;
    private Classifier cowTFliteDetector;
    private Classifier pigTFliteDetector;

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

    public static MultiBoxTracker_new tracker;

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
    private static final String DONKEY_TFLITE_DETECT_MODEL_FILE = "donkey_detection_ssdlite_mobilenet_v2_focal_192_uint8_1019.tflite";
    private static final String COW_TFLITE_DETECT_MODEL_FILE = "cow_detect_1029_tf10.tflite";
    private static final String PIG_TFLITE_DETECT_MODEL_FILE = "pig_1026_detect_xincai_addbg.tflite";

    public static int type1Count = 0;
    public static int type2Count = 0;
    public static int type3Count = 0;
    public static int AngleTrackType = 0;
    public static int offsetX;
    public static int offsetY;

    private static long last_toast_time = 0;
    @Override

    public synchronized void onResume() {
        type1Count = 0;
        type2Count = 0;
        type3Count = 0;
        super.onResume();
    }


    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        Log.i(TAG, "onPreviewSizeChosen start");
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker_new(this);


        try {

                pigTFliteDetector =
                        PigFaceDetectTFlite.create(
                                getAssets(),
                                PIG_TFLITE_DETECT_MODEL_FILE,
                                "",
                                TFLITE_INPUT_SIZE,
                                TFLITE_IS_QUANTIZED);
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
                        tracker.draw(canvas, PreferencesUtils.getAnimalType(DetectorActivity.this));
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

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Log.i(TAG + "time 0" , "start");

        long lastTime = System.currentTimeMillis();

        Image image = null;
        ++timestamp;
        final long currTimestamp = timestamp;
        try {
            image = reader.acquireLatestImage();
            Log.i(TAG + "time 1" , String.valueOf(System.currentTimeMillis() - lastTime));
            lastTime = System.currentTimeMillis();
            if (image == null) {
                return;
            }

            Trace.beginSection("imageAvailable");

            Log.i(TAG + "time 2" , String.valueOf(System.currentTimeMillis() - lastTime));
            lastTime = System.currentTimeMillis();

            final Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);

            tracker.onFrame(
                    previewWidth,
                    previewHeight,
                    planes[0].getRowStride(),
                    90,
                    yuvBytes[0],
                    timestamp);


            Log.i(TAG + "time 3" , String.valueOf(System.currentTimeMillis() - lastTime));
            lastTime = System.currentTimeMillis();


            trackingOverlay.postInvalidate();


            Log.i(TAG + "time 4" , String.valueOf(System.currentTimeMillis() - lastTime));
            lastTime = System.currentTimeMillis();

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



            Log.i(TAG + "time 5" , String.valueOf(System.currentTimeMillis() - lastTime));
            lastTime = System.currentTimeMillis();


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



        Log.i(TAG + "time 6" , String.valueOf(System.currentTimeMillis() - lastTime));
        lastTime = System.currentTimeMillis();



        if (luminance == null) {
            luminance = new byte[yuvBytes[0].length];
        }
        System.arraycopy(yuvBytes[0], 0, luminance, 0, luminance.length);


        final Paint paint = new Paint();
        Bitmap padBitmap = null;

        // 检测图片是否清晰，给出提示，用于用户调整
        imageErrMsg = "";


        Log.i(TAG + "time 7" , String.valueOf(System.currentTimeMillis() - lastTime));
        lastTime = System.currentTimeMillis();

        if (!Global.VIDEO_PROCESS) {
            return;
        }

        //                            图像质量检查
        checkImageQuality(croppedBitmap);


        Log.i(TAG + "time 8" , String.valueOf(System.currentTimeMillis() - lastTime));
        lastTime = System.currentTimeMillis();

        // TODO: 2018/10/18 By:LuoLu
        if (imageok) {

            Bitmap rotateBitmap = com.innovation.utils.ImageUtils.rotateBitmap(croppedBitmap, 90);


            Log.i(TAG + "time 8a" , String.valueOf(System.currentTimeMillis() - lastTime));
            lastTime = System.currentTimeMillis();
            //com.innovation.utils.ImageUtils.saveImage(rotateBitmap);
            padBitmap = com.innovation.utils.ImageUtils.padBitmap(rotateBitmap);


            Log.i(TAG + "time 8b" , String.valueOf(System.currentTimeMillis() - lastTime));
            lastTime = System.currentTimeMillis();

            cropCopyBitmap = Bitmap.createBitmap(padBitmap);


            Log.i(TAG + "time 9" , String.valueOf(System.currentTimeMillis() - lastTime));
            lastTime = System.currentTimeMillis();

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
        //image enter classifier
        int animalType = PreferencesUtils.getAnimalType(DetectorActivity.this);
        if (animalType == ConstUtils.ANIMAL_TYPE_PIG) {
            Log.d(TAG, "猪脸分类器");
            pigTFliteDetector.pigRecognitionAndPostureItemTFlite(padBitmap);
            if (PigFaceDetectTFlite.pigTFliteRecognitionAndPostureItem != null) {
                tracker.trackAnimalResults(PigFaceDetectTFlite.pigTFliteRecognitionAndPostureItem.getPostureItem(), PigRotationPrediction.pigPredictAngleType);
                final List<Classifier.Recognition> mappedRecognitions =
                        new LinkedList<Classifier.Recognition>();
                if (PigFaceDetectTFlite.pigTFliteRecognitionAndPostureItem.getList() != null) {
                    for (final Classifier.Recognition result : PigFaceDetectTFlite.pigTFliteRecognitionAndPostureItem.getList()) {
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
            cowTFliteDetector.cowRecognitionAndPostureItemTFlite(padBitmap);
            if (CowFaceDetectTFlite.cowRecognitionAndPostureItemTFlite != null) {
                tracker.trackAnimalResults(CowFaceDetectTFlite.cowRecognitionAndPostureItemTFlite.getPostureItem(), CowRotationPrediction.cowPredictAngleType);
                final List<Classifier.Recognition> mappedRecognitions =
                        new LinkedList<Classifier.Recognition>();
                if (CowFaceDetectTFlite.cowRecognitionAndPostureItemTFlite.getList() != null) {
                    for (final Classifier.Recognition result : CowFaceDetectTFlite.cowRecognitionAndPostureItemTFlite.getList()) {
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
            donkeyTFliteDetector.donkeyRecognitionAndPostureItemTFlite(padBitmap);
            if (DonkeyFaceDetectTFlite.donkeyRecognitionAndPostureItemTFlite != null) {
                tracker.trackAnimalResults(DonkeyFaceDetectTFlite.donkeyRecognitionAndPostureItemTFlite.getPostureItem(), DonkeyRotationPrediction.donkeyPredictAngleType);
                final List<Classifier.Recognition> mappedRecognitions =
                        new LinkedList<Classifier.Recognition>();
                if (DonkeyFaceDetectTFlite.donkeyRecognitionAndPostureItemTFlite.getList() != null) {
                    for (final Classifier.Recognition result : DonkeyFaceDetectTFlite.donkeyRecognitionAndPostureItemTFlite.getList()) {
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
        }
        trackingOverlay.postInvalidate();
        requestRender();
        Trace.endSection();
    }


    private void checkImageQuality(Bitmap bitmap) {
        //检测图片质量
        int bright = com.innovation.utils.ImageUtils.checkImageBright(bitmap);
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

            isBlur = com.innovation.utils.ImageUtils.isBlurByOpenCV_new(bitmap);
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
                Log.d(TAG, "DetectorActivity.parent = " + DetectorActivity.this );

                String finalTipMsg = tipMsg;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetectorActivity.this, finalTipMsg, Toast.LENGTH_SHORT).show();
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
        return R.layout.camera_connection_fragment_tracking_new;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onSetDebug(final boolean debug) {
        detector.enableStatLogging(debug);
    }

    public void reInitCurrentCounter(int a, int b, int c) {
        type1Count = a;
        type2Count = b;
        type3Count = c;
        LOGGER.i("reInitCurrentCounter-a :%d", type1Count);
        LOGGER.i("reInitCurrentCounter-b :%d", type2Count);
        LOGGER.i("reInitCurrentCounter-c :%d", type3Count);
    }

}
*/
