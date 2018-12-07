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
import android.graphics.Typeface;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.Toast;


import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import innovation.media.MediaInsureItem;
import innovation.media.MediaProcessor;
import innovation.media.Model;
import innovation.tensorflow.tracking.LocationTracker;
import innovation.utils.FileUtils;

import static innovation.entry.InnApplication.ANIMAL_TYPE;
import static innovation.entry.InnApplication.SCREEN_ORIENTATION;
import static org.tensorflow.demo.CameraConnectionFragment.textureView;


/**
 * @author luolu on 2018/6/17.
 */

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    private static final String TAG = "DetectorActivity";
    private static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged multibox model.
    private static final int MB_NUM_LOCATIONS = 784;
    private static final int MB_INPUT_SIZE = 128;
    private static final int MB_IMAGE_MEAN = 128;
    private static final float MB_IMAGE_STD = 128;
    private static final String MB_INPUT_NAME = "ResizeBilinear";
    private static final String MB_OUTPUT_NAMES = "output_locations/Reshape,output_scores/Reshape";
    private static final String MB_MODEL_FILE = "file:///android_asset/multibox_model.pb";
    private static final String MB_LOCATION_FILE = "file:///android_asset/multibox_location_priors.pb";
    private static final String YOLO_MODEL_FILE = "file:///android_asset/graph-tiny-yolo-voc.pb";
    private static final int YOLO_INPUT_SIZE = 416;
    private static final String YOLO_INPUT_NAME = "input";
    private static final String YOLO_OUTPUT_NAMES = "output";
    private static final int YOLO_BLOCK_SIZE = 32;

    // Default to the included multibox model.
    private static final boolean USE_YOLO = false;

    private static final int CROP_SIZE = USE_YOLO ? YOLO_INPUT_SIZE : MB_INPUT_SIZE;

    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE = USE_YOLO ? 0.0f : 0.1f;

    private static final boolean MAINTAIN_ASPECT = USE_YOLO;

    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;

    private Integer sensorOrientation;

    private Classifier detector;

    private int previewWidth = 0;
    private int previewHeight = 0;
    private byte[][] yuvBytes;
    private int[] rgbBytes = null;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap cropped2SquareBitmap = null;
    private Bitmap croppedBitmap = null;

    private boolean computing = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private Bitmap cropCopyBitmap;

    private MultiBoxTracker tracker;

    private byte[] luminance;

    private BorderedText borderedText;

    private long lastProcessingTimeMs;
    private LocationTracker mLocationTracker;


    //haojie add
    private int image_count = 0;
    private int ok_count = 0;  //图像良好次数
    private int dark_count = 0;//图像过暗次数
    private int blur_count = 0;//图像模糊次数
    private int bright_count = 0;//图像过亮次数
    private boolean imageok = true;//图像良好标识
    private String imageErrMsg = "";
    private static final int CHECK_COUNT = 1; //允许的最大错误图像次数   //定义提示类型

    //end add
    private int imageCount = 10;//haojie add for test

    private static int type1Count = 0;
    private static int type2Count = 0;
    private static int type3Count = 0;
    public static int imageWidth;
    public static int imageHeight;
    private String sheId;
    private String juanId;
    private String inspectNo;
    private String reason;
    private String mfleg;


    @Override

    public synchronized void onResume() {
        type1Count = 0;
        type2Count = 0;
        type3Count = 0;
        super.onResume();
        Intent intent = getIntent();
        sheId = intent.getStringExtra(Constants.sheId);
        inspectNo = intent.getStringExtra(Constants.inspectNo);
        reason = intent.getStringExtra(Constants.reason);



    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        Log.i("====== " ,"===再次=======");
        type1Count = 0;
        type2Count = 0;
        type3Count = 0;
        if (sheId != null && inspectNo != null && reason != null) {
            mFragment.setParmes(sheId,inspectNo, reason);
        }
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);
        tracker = new MultiBoxTracker(getResources().getDisplayMetrics());
        mLocationTracker = new LocationTracker(getResources().getDisplayMetrics());

        //猪脸识别
        try {
            detector = MediaProcessor.getInstance(getApplicationContext()).getFaceDetector_new();
        } catch (final Exception e) {
            throw new RuntimeException("Error initializing TensorFlow!", e);
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        LOGGER.i("previewWidth: " + previewWidth);
        LOGGER.i("previewHeight: " + previewHeight);

        final Display display = getWindowManager().getDefaultDisplay();
        final int screenOrientation = display.getRotation();

        LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation);
        // 20180223
        sensorOrientation = rotation + screenOrientation;


        LOGGER.i("Initializing sensorOrientation: %d", sensorOrientation);
        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbBytes = new int[previewWidth * previewHeight];
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);

        croppedBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);

        if (SCREEN_ORIENTATION == 0) {
            frameToCropTransform = ImageUtils.getTransformationMatrix(previewWidth, previewHeight, previewWidth, previewHeight, 90, true);
        } else if (SCREEN_ORIENTATION == 1) {
            frameToCropTransform = ImageUtils.getTransformationMatrix(previewWidth, previewHeight, previewWidth, previewHeight, sensorOrientation, true);
        }
        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
        yuvBytes = new byte[3][];

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(new DrawCallback() {
            @Override
            public void drawCallback(final Canvas canvas) {
                mLocationTracker.draw(canvas);
//                        tracker.draw(canvas);
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
                lines.add("Inference time: " + lastProcessingTimeMs + "ms");

                borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
            }
        });
        new DetectorActivity().reInitCurrentCounter(0, 0, 0);
        new LocationTracker(getResources().getDisplayMetrics()).reInitCounter(0, 0, 0);
        trackingOverlay.refreshDrawableState();
        textureView.refreshDrawableState();
    }

    public static OverlayView trackingOverlay;

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
            mLocationTracker.onFrame(previewWidth, previewHeight, planes[0].getRowStride(), sensorOrientation, yuvBytes[0], timestamp);
            trackingOverlay.postInvalidate();

            // No mutex needed as this method is not reentrant.
            if (computing) {
                image.close();
                return;
            }
            computing = true;

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            //Log.d("DetectorActivity.java", "haojie---dark test ----------------");

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

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        final long startTime = SystemClock.uptimeMillis();

                        PostureItem posture = null;
                        int angletype = 0;
                        if (Global.VIDEO_PROCESS) {//仅在录像时进行图片采集

                            // 检测图片是否清晰，给出提示，用于用户调整
                            imageok = true;
                            imageErrMsg = "";
                            Log.d(TAG, "图像采集开始");
                            //图片进入模型文件
                            if (ANIMAL_TYPE == 1) {
                                posture = detector.recognizeImagePig(croppedBitmap, null);
                                Log.d(TAG, "猪脸分类器");
                            } else if (ANIMAL_TYPE == 2) {
                                posture = detector.recognizeImageCow(croppedBitmap, null);
                                Log.d(TAG, "牛脸分类器");
                            } else if (ANIMAL_TYPE == 3) {
                                Log.d(TAG, "驴脸分类器");
//                                 TODO: 2018/9/1 By:LuoLu
                                if (croppedBitmap.getWidth() >= croppedBitmap.getHeight()) {
                                    cropped2SquareBitmap = Bitmap.createBitmap(
                                            croppedBitmap,
                                            croppedBitmap.getWidth() / 2 - croppedBitmap.getHeight() / 2,
                                            0,
                                            croppedBitmap.getHeight(),
                                            croppedBitmap.getHeight()
                                    );
                                } else {
                                    cropped2SquareBitmap = Bitmap.createBitmap(
                                            croppedBitmap,
                                            0,
                                            croppedBitmap.getHeight() / 2 - croppedBitmap.getWidth() / 2,
                                            croppedBitmap.getWidth(),
                                            croppedBitmap.getWidth()
                                    );
                                }
                                posture = detector.recognizeImageDonkey(cropped2SquareBitmap, null);
                            }
                            if (posture != null) {
                                //  startTestDeviceAngle();
                                Log.d(TAG, "图像采集成功-");
                                if (imageok) {//质量合格的图片才进行计算采集
                                    //角度计算
                                    angletype = angleCalculate(posture);
                                    saveImage(posture.original_bitmap);

                                    Log.d(TAG, "图像合格----");
                                    // TODO: 2018/8/25 By:LuoLu
//                                    mLocationTracker.trackResults_new(posture, angletype);
//                                    trackingOverlay.postInvalidate();
//                                    requestRender();
//                                    computing = false;

                                } else {
                                    Log.d(TAG, "图像不合格原因：----" + imageErrMsg);
                                }
                            }
                        }
                        //获取显示红框
                        mLocationTracker.trackResults_new(posture, angletype);
                        trackingOverlay.postInvalidate();
                        requestRender();
                        computing = false;
                    }
                });

        Trace.endSection();
    }


    /**
     * 缩放图片
     *
     * @param
     */
    private Bitmap getPostScaleBitmap(Bitmap bitmap) {
        // Matrix类进行图片处理（缩小或者旋转）
        Matrix matrix = new Matrix();
        // 根据指定高度宽度缩放
        matrix.postScale(0.05f, 0.05f);
        // 生成新的图片
        try {
            Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            if (dstbmp != null) {
                return dstbmp;
            }
        } catch (Exception e) {
            String s = e.getMessage().toString();
            Log.d(TAG, "图像imgae----getPostScaleBitmap except===" + s);
            return null;
        }
        return null;
    }


    //haojie add
    //判断模糊，true:模糊 false:清晰
    private boolean getImageBlur(Bitmap bitmap) {
        return innovation.utils.ImageUtils.isBlurByOpenCV_new(bitmap);
    }

    //获取图片亮度值
    public int getImageBright(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int r, g, b;
        int count = 0;
        int bright = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                count++;
                int localTemp = bm.getPixel(i, j);
                r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
                g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
                b = (localTemp | 0xffffff00) & 0x0000ff;
                bright = (int) (bright + 0.299 * r + 0.587 * g + 0.114 * b);
            }
        }
        return bright / count;
    }

    //检测亮度
    private int check_imageBright(Bitmap bitmap) {
        //对图像进行模糊度，明暗度判断
        //先缩放再获得亮度
        Bitmap check_image = getPostScaleBitmap(bitmap);
        long time0 = System.currentTimeMillis();
        int bitBright = getImageBright(check_image);
        long time1 = System.currentTimeMillis();
        Log.d(TAG, "图像imgae----bitBright===" + bitBright + "--spent time ====" + (time1 - time0));
        return bitBright;
    }

    //检测图片质量
    private void check_imageQuality(Bitmap bitmap) {
        int bright = check_imageBright(bitmap);
        boolean ifdark = false;
        boolean ifbright = false;
        boolean isblur = false;
        if (bright > 160) {
            ifbright = true;
            bright_count++;
            Log.d(TAG, "图像过亮，请重新选择" + "--bright ===" + bright);
            imageErrMsg += "图像过亮！" + "--bright ===" + bright;
        } else if (bright < 30) {
            ifdark = true;
            dark_count++;
            Log.d(TAG, "图像过暗，请重新选择" + "--bright ===" + bright);
            imageErrMsg += "图像过暗！" + "--intensityValue ===" + bright;
        }
        isblur = check_blur(bitmap);
        if (isblur) {
            blur_count++;
            Log.d(TAG, "图像模糊，请重新选择" + "--isblur ===" + isblur);
            imageErrMsg += "图像模糊！" + "--isblur ===" + isblur;
        }

        if (!ifdark && !ifbright && !isblur) {
            imageok = true;
            ok_count++;
            Log.d(TAG, "图像 质量良好 imageok ===" + imageok);
        } else {
            imageok = false; //图片质量有问题，不进行捕捉图片的保存
            Log.d(TAG, "图像 质量差 imageok ===" + imageok);
        }
        check_imageresult();
    }

    //判断模糊，true:模糊 false:清晰
    private boolean check_blur(Bitmap bitmap) {
        return innovation.utils.ImageUtils.isBlurByOpenCV_new(bitmap);
    }

    //判断图片质量，用于图片错误提示（过亮、过暗、模糊）
    private void check_imageresult() {
        if (image_count > CHECK_COUNT) {
            int error_count = dark_count + bright_count + blur_count;
            String tipmsg = "";
            double tmpok = CHECK_COUNT * 0.6;
            double tmpdark = CHECK_COUNT * 0.3;
            double tmpbright = CHECK_COUNT * 0.3;
            double tmpblur = CHECK_COUNT * 0.2;

            if (dark_count > tmpdark) {
                if (tipmsg.length() > 0)
                    tipmsg = tipmsg + "、";
                tipmsg = tipmsg + "过暗";
            }
            if (bright_count > tmpbright) {
                if (tipmsg.length() > 0)
                    tipmsg = tipmsg + "、";
                tipmsg = tipmsg + "过亮";
            }
            if (blur_count > tmpblur) {
                if (tipmsg.length() > 0)
                    tipmsg = tipmsg + "、";
                tipmsg = tipmsg + "模糊";
            }

            if ((ok_count < tmpok) && (tipmsg.length() > 0)) {
                tipmsg = "当前图片" + tipmsg + "，请调整";
                Toast.makeText(DetectorActivity.this, tipmsg, Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "haojie---set_checkcount---tmpok==" + tmpok + "==图片可用");
            }
            init_checkpara();
        } else {
            image_count++;
        }
    }

    private void init_checkpara() {
        image_count = 0;
        ok_count = 0;
        dark_count = 0;
        blur_count = 0;
        bright_count = 0;
    }

    //获得角度类型
    private int getAngleCategory(float x, float y) {
        int type = 10;
        float new_x, new_y;
        //弧度转角度
        new_x = (float) (x * 180 / 3.14);
        new_y = (float) (y * 180 / 3.14);
// TODO: 2018/8/22 By:LuoLu  根据ANIMAL_TYPE进行角度分类
        final int[][] ANGEL_PARAMS = {
                // animal type=0
                {0, 0, 0, 0, 0, 0, 0, 0},
                {-60, 60, -80, -8, -8, 8, 8, 80},  // animal type=Global.ANIMAL_TYPE_PIG
                {0, 40, -60, -15, -8, 8, 15, 60},  // animal type=Global.ANIMAL_TYPE_CATTLE
                {0, 60, -70, -15, -8, 8, 15, 70}   // animal type=Global.ANIMAL_TYPE_DONKEY
        };

        if (new_x >= ANGEL_PARAMS[ANIMAL_TYPE][0] && new_x <= ANGEL_PARAMS[ANIMAL_TYPE][1]) {
            if (new_y >= ANGEL_PARAMS[ANIMAL_TYPE][2] && new_y <= ANGEL_PARAMS[ANIMAL_TYPE][3]) {
                type = 1;
            } else if (new_y > ANGEL_PARAMS[ANIMAL_TYPE][4] && new_y <= ANGEL_PARAMS[ANIMAL_TYPE][5]) {
                type = 2;
            } else if (new_y > ANGEL_PARAMS[ANIMAL_TYPE][6] && new_y <= ANGEL_PARAMS[ANIMAL_TYPE][7]) {
                type = 3;
            } else {
                type = 10;
            }
        }

        Log.d("DetectorActivity.java", "getAngleCategory==rot_x" + x + "==rot_y==" + y + "===new_x==" + new_x + "===new_y===" + y + "===type==" + type);

        return type;
    }

    //角度计算，将角度照片存入对应map下
    private int angleCalculate(PostureItem posture) {
        int type;
        type = getAngleCategory(posture.rot_x, posture.rot_y);
        String imagefilename = "";
        String txtfilename = "";
        if (Global.model == Model.BUILD.value()) {
            imagefilename = Global.mediaInsureItem.getBitmapFileName(type);//storage/emulated/0/Android/data/com.innovation.animial/cache/innovation/animal/投保/Current/图片/Angle-01/20180423093314.jpg
            txtfilename = Global.mediaInsureItem.getTxtFileNme(type);
        }
        if (Global.model == Model.VERIFY.value()) {
            imagefilename = Global.mediaPayItem.getBitmapFileName(type);
            txtfilename = Global.mediaPayItem.getTxtFileNme(type);
        }
        //保存角度信息
        String contentpre = imagefilename.substring(imagefilename.lastIndexOf("/") + 1);
        contentpre += ":";
        contentpre += "rot_x = " + posture.rot_x + "; ";
        contentpre += "rot_y = " + posture.rot_y + "; ";
        contentpre += "rot_z = " + posture.rot_z + "; ";
        // TODO: 2018/8/13 By:LuoLu  图片数量达上限，不保存
        if (type1Count < Global.MAX_FACE_LEFT && type == 1) {
            Log.d("图像保存1，角度：", type + "");
            LOGGER.i("type1数量:" + type1Count);
            FileUtils.saveInfoToTxtFile(txtfilename, contentpre + ":" + type);
            //保存图片
            File tmpimagefile = new File(imagefilename);
            boolean result = FileUtils.saveBitmapToFile(posture.big_bitmap, tmpimagefile);//保存放大后的图片
            if (result == true) {
                type1Count++;
            }
        }
        if (type2Count < Global.MAX_FACE_MIDDLE && type == 2) {
            Log.d("图像保存2，角度：", type + "");
            LOGGER.i("type2数量:" + type2Count);
            FileUtils.saveInfoToTxtFile(txtfilename, contentpre + ":" + type);
            //保存图片
            File tmpimagefile = new File(imagefilename);
            boolean result = FileUtils.saveBitmapToFile(posture.big_bitmap, tmpimagefile);//保存放大后的图片
            if (result == true) {
                type2Count++;
            }
        }
        if (type3Count < Global.MAX_FACE_RIGHT && type == 3) {
            Log.d("图像保存3，角度：", type + "");
            LOGGER.i("type3数量:" + type3Count);
            FileUtils.saveInfoToTxtFile(txtfilename, contentpre + ":" + type);
            //保存图片
            File tmpimagefile = new File(imagefilename);
            boolean result = FileUtils.saveBitmapToFile(posture.big_bitmap, tmpimagefile);//保存放大后的图片
            if (result == true) {
                type3Count++;
            }
        }

        mLocationTracker.getCountOfCurrentImage(type1Count,
                type2Count, type3Count);
        if (type1Count > 2 && type2Count > 6 && type3Count > 2) {
            Log.i("DetectorTypeCountSum:", String.valueOf(type3Count + type2Count + type1Count));
            Global.numberOk = true;
            CameraConnectionFragment.collectNumberHandler.sendEmptyMessage(1);
            Log.i("Global.numberOk:", String.valueOf(Global.numberOk));
        }


        return type;
    }

    @Override
    protected int getLayoutId() {
        //return R.layout.camera_connection_fragment_tracking; //haojie del for test
        return R.layout.camera_connection_fragment_tracking_new;
    }

    @Override
    protected int getDesiredPreviewFrameSize() {
        return CROP_SIZE;
    }

    @Override
    public void onSetDebug(final boolean debug) {
        detector.enableStatLogging(debug);
    }

    public void reInitCurrentCounter(int a, int b, int c) {
        type1Count = a;
        type2Count = b;
        type3Count = c;
        LOGGER.i("reInitCurrentCounter-a:", type1Count);
        LOGGER.i("reInitCurrentCounter-b:", type2Count);
        LOGGER.i("reInitCurrentCounter-c:", type3Count);
    }


    public static File saveImage(Bitmap bmp) {
       // File appDir = new File(new File(Environment.getExternalStorageDirectory(), "innovation"), "test94");
        new File(Environment.getExternalStorageDirectory(), "animal/ZipImage");
        File appDir = new File(new File(Environment.getExternalStorageDirectory(), "innovation/animal"),"ZipImage");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        SimpleDateFormat tmpSimpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS", Locale.getDefault());
        String fileName = tmpSimpleDateFormat.format(new Date(System.currentTimeMillis()));
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.i("Lu,save to local,path: " + appDir.toString());
        return file;
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
