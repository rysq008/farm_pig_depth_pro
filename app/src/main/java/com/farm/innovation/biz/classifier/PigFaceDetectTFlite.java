package com.farm.innovation.biz.classifier;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Trace;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.biz.iterm.AnimalClassifierResultIterm;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.biz.iterm.PostureItem;
import com.farm.innovation.biz.iterm.PredictRotationIterm;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.PointFloat;

import org.tensorflow.demo.FarmCameraConnectionFragment;
import org.tensorflow.demo.FarmClassifier;
import org.tensorflow.demo.FarmDetectorActivity;
import org.tensorflow.demo.FarmGlobal;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.farm.innovation.utils.ImageUtils.compressBitmap;
import static com.farm.innovation.utils.ImageUtils.padBitmap2SpRatio;
import static com.farm.innovation.utils.ImageUtils.zoomImage;
import static org.tensorflow.demo.FarmDetectorActivity.aNumber;
import static org.tensorflow.demo.FarmDetectorActivity.aTime;
import static org.tensorflow.demo.FarmDetectorActivity.aTimes;
import static org.tensorflow.demo.FarmDetectorActivity.allNumber;
import static org.tensorflow.demo.FarmDetectorActivity.dNumber;
import static org.tensorflow.demo.FarmDetectorActivity.dTime;
import static org.tensorflow.demo.FarmDetectorActivity.dTimes;
import static org.tensorflow.demo.FarmDetectorActivity.kNumber;
import static org.tensorflow.demo.FarmDetectorActivity.kTime;
import static org.tensorflow.demo.FarmDetectorActivity.kTimes;
import static org.tensorflow.demo.FarmDetectorActivity.offsetX;
import static org.tensorflow.demo.FarmDetectorActivity.offsetY;
import static org.tensorflow.demo.FarmDetectorActivity.preWidth;

/**
 * @author luolu .2018/8/4
 */
public class PigFaceDetectTFlite implements FarmClassifier {
    private static final Logger sLogger = new Logger(PigFaceDetectTFlite.class);
    private static final boolean DEBUG = false;
    public static float MIN_CONFIDENCE;
    private static final String TAG = "PigFaceDetectTFlite";
    // Only return this many results.
    private static final int NUM_DETECTIONS = 10;
    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;
    // Number of threads in the java app
    private static final int NUM_THREADS = 4;
    private static final String PIG_TFLITE_PREDICTION_MODEL_FILE = "pig_tflite_pose1022.tflite";
    private static final String PIG_TFLITE_KEYPOINTS_MODEL_FILE = "pig_1026_keypoint_tflite_xincai2.tflite";

    //    private final FarmClassifier donkeyFaceKeyPointsTFDetector;
    private boolean isModelQuantized;
    // Config values.
    private int inputSize;
    private int[] intValues;
    private float[][][] outputLocations;
    private float[][] outputScores;
    private float[] outputDetectNum;
    private float[][] outputClassifyResult;
    private ByteBuffer imgData;
    private Interpreter tfLite;
    private FarmClassifier pigFaceRotationDetector;
    private FarmClassifier pigFaceKeyPointsDetector;
    private Context context;
    public static String srcPigBitmapName;
    public static RecognitionAndPostureItem pigTFliteRecognitionAndPostureItem;

    //时间戳  控制Toast密度
    private long lastToastTime = 0;

    private PigFaceDetectTFlite() {
        try {
            pigFaceRotationDetector =
                    PigRotationPrediction.create(
                            FarmAppConfig.getApplication().getAssets(),
                            PIG_TFLITE_PREDICTION_MODEL_FILE,
                            "",
                            192,
                            true);
            pigFaceKeyPointsDetector =
                    PigKeyPointsDetectTFlite.create(
                            FarmAppConfig.getApplication().getAssets(),
                            PIG_TFLITE_KEYPOINTS_MODEL_FILE,
                            "",
                            192,
                            true);

        } catch (final Exception e) {
            throw new RuntimeException("pigFaceRotationDetector or pigFaceKeyPointsDetector: Error initializing!", e);
        }
    }

    /**
     * Memory-map the model file in Assets.
     */
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager  The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param inputSize     The size of image input
     * @param isQuantized   Boolean representing model is quantized or not
     */
    public static FarmClassifier create(
            final AssetManager assetManager,
            final String modelFilename,
            final String labelFilename,
            final int inputSize,
            final boolean isQuantized)
            throws IOException {
        final PigFaceDetectTFlite d = new PigFaceDetectTFlite();
        d.inputSize = inputSize;
        try {
            d.tfLite = new Interpreter(loadModelFile(assetManager, modelFilename));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        d.isModelQuantized = isQuantized;
        // Pre-allocate buffers.
        int numBytesPerChannel;
        if (isQuantized) {
            numBytesPerChannel = 1; // Quantized
        } else {
            numBytesPerChannel = 4; // Floating point
        }
        d.imgData = ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel);
        d.imgData.order(ByteOrder.nativeOrder());
        d.intValues = new int[d.inputSize * d.inputSize];
        d.tfLite.setNumThreads(NUM_THREADS);
        d.outputLocations = new float[1][NUM_DETECTIONS][4];
        d.outputClassifyResult = new float[1][10];
        d.outputScores = new float[1][10];
        d.outputDetectNum = new float[1];
        return d;
    }

    @Override
    public List<PointFloat> recognizePointImage(Bitmap bitmap, Bitmap oriBitmap) {
        return null;
    }

    @Override
    public RecognitionAndPostureItem cowRecognitionAndPostureItem(Bitmap bitmap) {
        return null;
    }

    @Override
    public RecognitionAndPostureItem donkeyRecognitionAndPostureItem(Bitmap bitmap) {
        return null;
    }

    @Override
    public RecognitionAndPostureItem pigRecognitionAndPostureItem(Bitmap bitmap) {
        return null;
    }

    @Override
    public RecognitionAndPostureItem yakRecognitionAndPostureItem(Bitmap bitmap) {
        return null;
    }

    @Override
    public RecognitionAndPostureItem donkeyRecognitionAndPostureItemTFlite(Bitmap bitmap, Bitmap oriBitmap) {
        return null;
    }

    @Override
    public RecognitionAndPostureItem cowRecognitionAndPostureItemTFlite(Bitmap bitmap, Bitmap oriBitmap) {
        return null;
    }

    @Override
    public PredictRotationIterm cowRotationPredictionItemTFlite(Bitmap bitmap, Bitmap oriBitmap) {
        return null;
    }

    @Override
    public PredictRotationIterm pigRotationPredictionItemTFlite(Bitmap bitmap, Bitmap oriBitmap) {
        return null;
    }

    @Override
    public RecognitionAndPostureItem yakRecognitionAndPostureItemTFlite(Bitmap bitmap, Bitmap originalBitmap) {
        return null;
    }

    @Override
    public PredictRotationIterm yakRotationPredictionItemTFlite(Bitmap bitmap,Bitmap originalBitmap) {
        return null;
    }

    @Override
    public PredictRotationIterm donkeyRotationPredictionItemTFlite(Bitmap bitmap, Bitmap oriBitmap) {
        return null;
    }

    @Override
    public void enableStatLogging(boolean debug) {
        //inferenceInterface.enableStatLogging(debug);
    }

    @Override
    public String getStatString() {
        return "tflite";
    }

    @Override
    public void close() {
        tfLite.close();
    }

    private void saveBitMap(Bitmap bmp, String childFileName, String bitmapFileName) {
        if (DEBUG) {
            com.farm.innovation.utils.ImageUtils.saveBitmap(bmp, childFileName, bitmapFileName);
        }
    }

    @Override
    public RecognitionAndPostureItem pigRecognitionAndPostureItemTFlite(Bitmap bitmap, Bitmap oriBitmap) {
        String oriInfoPath = "";
        String unsuccessTXTPath = "";
        if (FarmGlobal.model == Model.BUILD.value()) {
            oriInfoPath = FarmGlobal.mediaInsureItem.getOriInfoTXTFileName();
            unsuccessTXTPath = FarmGlobal.mediaInsureItem.getUnsuccessInfoTXTFileName();
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            oriInfoPath = FarmGlobal.mediaPayItem.getOriInfoTXTFileName();
            unsuccessTXTPath = FarmGlobal.mediaPayItem.getUnsuccessInfoTXTFileName();
        }
        PostureItem posture = null;
        if (bitmap == null) {
            allNumber--;
            pigTFliteRecognitionAndPostureItem = null;
            return null;
        }

        pigTFliteRecognitionAndPostureItem = new RecognitionAndPostureItem();

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int padSize = height;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        srcPigBitmapName = sdf.format(new Date(System.currentTimeMillis())) + ".jpeg";
        saveBitMap(bitmap, "pigSrcImage", srcPigBitmapName);
        sLogger.i("padBitmap padSize %d:" + padSize);

        Matrix frameToCropTransform = ImageUtils.getTransformationMatrix(bitmap.getWidth(), bitmap.getHeight(),
                inputSize, inputSize, 0, true);
        Matrix cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
        Bitmap croppedBitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(bitmap, frameToCropTransform, null);

        croppedBitmap.getPixels(intValues, 0, croppedBitmap.getWidth(), 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight());

        sLogger.i("croppedBitmap height:" + croppedBitmap.getHeight());
        sLogger.i("croppedBitmap width:" + croppedBitmap.getWidth());


        Trace.beginSection("preprocessBitmap");
        imgData.rewind();

        // 输出头部识别图片
        saveBitMap(bitmap, "pigDetected_input", srcPigBitmapName);

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));


                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }
        Trace.endSection(); // preprocessBitmap
        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");

        outputLocations = new float[1][NUM_DETECTIONS][4];
        outputClassifyResult = new float[1][NUM_DETECTIONS];
        outputScores = new float[1][NUM_DETECTIONS];
        outputDetectNum = new float[1];

        sLogger.i("inputSize:" + inputSize);

        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClassifyResult);
        outputMap.put(2, outputScores);
        outputMap.put(3, outputDetectNum);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        final long startTime = SystemClock.uptimeMillis();

        //进模型图片数量++
        allNumber++;
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

        sLogger.i("pig Detect face tflite cost:" + (SystemClock.uptimeMillis() - startTime));
        dTime = SystemClock.uptimeMillis() - startTime;
        Trace.endSection();

        //拼接检测信息字符串
        String contenType = "DetectResult：";
        contenType += srcPigBitmapName + "; ";
        contenType += "box_x0 = " + outputLocations[0][0][0] + "; ";
        contenType += "box_y0 = " + outputLocations[0][0][1] + "; ";
        contenType += "box_x1 = " + outputLocations[0][0][2] + "; ";
        contenType += "box_y1 = " + outputLocations[0][0][3] + "; ";
        contenType += "score = " + outputScores[0][0] + "; ";
        contenType += "detect_num = " + outputDetectNum[0] + "; ";

        if (outputDetectNum[0] > 1) {
            if (System.currentTimeMillis() - lastToastTime > 5000) {
                FarmCameraConnectionFragment.showToast("请确保采集范围内只有一头牲畜。");
                lastToastTime = System.currentTimeMillis();
            }
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime：" + aTime + "；KeypointTime：" + kTime + "；totalTime：" + (dTime + aTime + kTime));
            saveBitMap(bitmap, "pigDetected_ng3", srcPigBitmapName);
            if (dTimes < 6) {
                dTimes++;

                String mPath = null;
                if (FarmGlobal.model == Model.BUILD.value()) {
                    mPath = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("/detect");
                } else if (FarmGlobal.model == Model.VERIFY.value()) {
                    mPath = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("/detect");
                }

                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            FarmDetectorActivity.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }

        if (outputDetectNum[0] < 1) {
            sLogger.i("对象不足：" + outputDetectNum[0]);
            saveBitMap(bitmap, "pigDetected_ng4", srcPigBitmapName);
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime：" + aTime + "；KeypointTime：" + kTime + "；totalTime：" + (dTime + aTime + kTime));
            if (dTimes < 6) {
                dTimes++;

                String mPath = null;
                if (FarmGlobal.model == Model.BUILD.value()) {
                    mPath = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("/detect");
                } else if (FarmGlobal.model == Model.VERIFY.value()) {
                    mPath = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("/detect");
                }

                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            FarmDetectorActivity.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }

        if (outputScores[0][0] > 1 || outputScores[0][0] < MIN_CONFIDENCE) {
            sLogger.i("分值超出/分值不足：" + outputScores[0][0]);
            saveBitMap(bitmap, "pigDetected_ng2", srcPigBitmapName);
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime：" + aTime + "；KeypointTime：" + kTime + "；totalTime：" + (dTime + aTime + kTime));
            if (dTimes < 6) {
                dTimes++;

                String mPath = null;
                if (FarmGlobal.model == Model.BUILD.value()) {
                    mPath = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("/detect");
                } else if (FarmGlobal.model == Model.VERIFY.value()) {
                    mPath = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("/detect");
                }

                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            FarmDetectorActivity.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }

        sLogger.i("outputScores0 %f:" + outputScores[0][0]);
        sLogger.i("OutClassifyResult0 %f:" + outputClassifyResult[0][0]);
        sLogger.i("OutPDetectNum %f:" + outputDetectNum[0]);
        float modelY0 = (float) outputLocations[0][0][1];
        float modelX0 = (float) outputLocations[0][0][0];
        float modelY1 = (float) outputLocations[0][0][3];
        float modelX1 = (float) outputLocations[0][0][2];

        float left = modelY0 * preWidth - offsetY;
        float top = modelX0 * preWidth - offsetX;
        float right = modelY1 * preWidth - offsetY;
        float bottom = modelX1 * preWidth - offsetX;
        if (left < 0 || top < 0 || right > preWidth - 2 * offsetY || bottom > preWidth - 2 * offsetX) {
            sLogger.i("识别范围超出图像范围2");
            saveBitMap(bitmap, "pigDetected_ng5", srcPigBitmapName);
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime：" + aTime + "；KeypointTime：" + kTime + "；totalTime：" + (dTime + aTime + kTime));
            if (dTimes < 6) {
                dTimes++;

                String mPath = null;
                if (FarmGlobal.model == Model.BUILD.value()) {
                    mPath = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("/detect");
                } else if (FarmGlobal.model == Model.VERIFY.value()) {
                    mPath = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("/detect");
                }

                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            FarmDetectorActivity.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>(1);
        // 设置猪头画框范围
        final RectF detection = new RectF(left, top, right, bottom);
        recognitions.add(
                new Recognition(
                        "",
                        "pigLite",
                        outputScores[0][0],
                        detection, null));
        Trace.endSection(); // "recognizeImage"
        pigTFliteRecognitionAndPostureItem.setList(recognitions);

        saveBitMap(bitmap, "pigDetected_ok", srcPigBitmapName);
        //clip image
        Bitmap clipBitmap = com.farm.innovation.utils.ImageUtils.clipBitmap(bitmap, modelY0, modelX0, modelY1, modelX1, 1.2f);
        if (clipBitmap == null) {
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime：" + aTime + "；KeypointTime：" + kTime + "；totalTime：" + (dTime + aTime + kTime));
            if (dTimes < 6) {
                dTimes++;
                String mPath = null;
                if (FarmGlobal.model == Model.BUILD.value()) {
                    mPath = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("/detect");
                } else if (FarmGlobal.model == Model.VERIFY.value()) {
                    mPath = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("/detect");
                }
                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            FarmDetectorActivity.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }
        dNumber++;


        Bitmap padBitmap2SpRatio = padBitmap2SpRatio(clipBitmap, 1.0f);
        int widthZoom = 320, heightZoom = 320;
        Bitmap resizeClipBitmap = zoomImage(padBitmap2SpRatio, widthZoom, heightZoom);

        aTime = System.currentTimeMillis();
        PredictRotationIterm predictRotationIterm = pigFaceRotationDetector.pigRotationPredictionItemTFlite(clipBitmap, oriBitmap);
        aTime = System.currentTimeMillis() - aTime;
        // 调用模型判断角度分类
        if (predictRotationIterm == null) {
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime：" + aTime + "；KeypointTime：" + kTime + "；totalTime：" + (dTime + aTime + kTime));
            if (aTimes < 4) {
                aTimes++;
                String mPath = null;
                if (FarmGlobal.model == Model.BUILD.value()) {
                    mPath = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("/rota");
                } else if (FarmGlobal.model == Model.VERIFY.value()) {
                    mPath = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("/rota");
                }
                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);

                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            FarmDetectorActivity.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }
        aNumber++;


        // 调用模型判断关键点
        // keypoint detect
        kTime = System.currentTimeMillis();
        List<PointFloat> keypointResults = pigFaceKeyPointsDetector.recognizePointImage(clipBitmap, oriBitmap);
        kTime = System.currentTimeMillis() - kTime;
        if (keypointResults == null) {
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime：" + aTime + "；KeypointTime：" + kTime + "；totalTime：" + (dTime + aTime + kTime));
            if (kTimes < 4) {
                kTimes++;
                String mPath = null;
                if (FarmGlobal.model == Model.BUILD.value()) {
                    mPath = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("/key");
                } else if (FarmGlobal.model == Model.VERIFY.value()) {
                    mPath = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("/key");
                }

                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);

                String angleType = "AngleResult：";
                angleType += srcPigBitmapName + "; ";
                angleType += "rot_x = " + predictRotationIterm.rot_x + "; ";
                angleType += "rot_y = " + predictRotationIterm.rot_y + "; ";
                angleType += "rot_z = " + predictRotationIterm.rot_z + "; ";

                //保存关键点失败的角度信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, angleType);
                //保存关键点失败的检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            FarmDetectorActivity.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }
        kNumber++;
        //  keypoint 的判断逻辑
        posture = new PostureItem(
                (float) predictRotationIterm.rot_x,
                (float) predictRotationIterm.rot_y,
                (float) predictRotationIterm.rot_z,
                modelX0, modelY0, modelX1, modelY1, outputScores[0][0],
                modelY0 * padSize, modelX0 * padSize,
                modelY1 * padSize, modelX1 * padSize, resizeClipBitmap, bitmap, oriBitmap);
        pigTFliteRecognitionAndPostureItem.setPostureItem(posture);
        AnimalClassifierResultIterm.pigAngleCalculateTFlite(pigTFliteRecognitionAndPostureItem.getPostureItem());


        return pigTFliteRecognitionAndPostureItem;
    }
}
