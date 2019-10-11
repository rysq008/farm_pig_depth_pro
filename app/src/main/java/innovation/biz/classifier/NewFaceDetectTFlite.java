package innovation.biz.classifier;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Trace;

import com.xiangchuangtec.luolu.animalcounter.AppConfig;

import org.tensorflow.demo.CameraConnectionFragment_pig;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.DetectorActivity_pig;
import org.tensorflow.demo.Global;
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

import innovation.biz.iterm.AnimalClassifierResultIterm;
import innovation.biz.iterm.NewPigKeyPointAndRotationItem;
import innovation.biz.iterm.PostureItem;
import innovation.biz.iterm.PredictRotationIterm;
import innovation.utils.FileUtils;
import innovation.utils.PointFloat;

import static innovation.utils.ImageUtils.compressBitmap;
import static innovation.utils.ImageUtils.padBitmap2SpRatio;
import static innovation.utils.ImageUtils.zoomImage;
import static org.tensorflow.demo.DetectorActivity_pig.aNumber;
import static org.tensorflow.demo.DetectorActivity_pig.aTimes;
import static org.tensorflow.demo.DetectorActivity_pig.allNumber;
import static org.tensorflow.demo.DetectorActivity_pig.dNumber;
import static org.tensorflow.demo.DetectorActivity_pig.dTime;
import static org.tensorflow.demo.DetectorActivity_pig.dTimes;
import static org.tensorflow.demo.DetectorActivity_pig.kTime;
import static org.tensorflow.demo.DetectorActivity_pig.kTimes;
import static org.tensorflow.demo.DetectorActivity_pig.offsetX;
import static org.tensorflow.demo.DetectorActivity_pig.offsetY;

/**
 * @author luolu .2018/8/4
 */
public class NewFaceDetectTFlite implements Classifier {
    private static final Logger S_LOGGER = new Logger(NewFaceDetectTFlite.class);
    private static final boolean DEBUG = false;

    // 2018/12/18 hedazhi edit start
    //private static final float MIN_CONFIDENCE = (float) 0.7;
    public static float MIN_CONFIDENCE;
    // 检测模型阈值：理赔时 0.3 投保时 0.5
    //private static final float MIN_CONFIDENCE = (Global.model == Model.VERIFY.value()) ? 0.3f : 0.5f;

    // 2018/12/18 hedazhi edit end

    private static final String TAG = "PigFaceDetectTFlite";
    // Only return this many results.
    private static final int NUM_DETECTIONS = 10;
    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;
    // WaitNumber of threads in the java app
    private static final int NUM_THREADS = 4;
    //保存图片的尺寸
    private static final int ZOOM = 480;
    private static final String PIG_TFLITE_KEYPOINTS_MODEL_FILE = "0617_kp_rot.tflite";

    //    private final Classifier donkeyFaceKeyPointsTFDetector;
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
    private Classifier pigFaceKeyPointsDetector;
    private Context context;
    public static String srcPigBitmapName;
    public static RecognitionAndPostureItem pigTFliteRecognitionAndPostureItem;

    //时间戳  控制Toast密度
    private long lastToastTime = 0;

    private NewFaceDetectTFlite() {
        try {
            pigFaceKeyPointsDetector =
                    NewKeyPointsDetectTFlite.create(
                            AppConfig.getAppContext().getAssets(),
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
    public static Classifier create(
            final AssetManager assetManager,
            final String modelFilename,
            final String labelFilename,
            final int inputSize,
            final boolean isQuantized)
            throws IOException {
        final NewFaceDetectTFlite d = new NewFaceDetectTFlite();
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
    public NewPigKeyPointAndRotationItem pigRecognizePointImage(Bitmap bitmap, Bitmap originalBitmap) {
        return null;
    }

    @Override
    public RecognitionAndPostureItem pigRecognitionAndPostureItem(Bitmap bitmap, Bitmap originalBitmap) {
        return null;
    }


    @Override
    public PredictRotationIterm pigRotationPredictionItemTFlite(Bitmap bitmap, Bitmap originalBitmap) {
        return null;
    }

    @Override
    public List<PointFloat> recognizePointImage(Bitmap bitmap, Bitmap originalBitmap) {
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
        return;
/*        if (DEBUG) {
            innovation.utils.ImageUtils.saveBitmap(bmp, childFileName, bitmapFileName);
        }*/
    }

    @Override
    public RecognitionAndPostureItem pigRecognitionAndPostureItemTFlite(Bitmap bitmap, Bitmap oriBitmap) {
        String oriInfoPath = "";
        String unsuccessTXTPath = "";
        oriInfoPath = Global.mediaPayItem.getOriInfoTXTFileName();
        unsuccessTXTPath = Global.mediaPayItem.getUnsuccessInfoTXTFileName();


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
        S_LOGGER.i("padBitmap padSize %d:" + padSize);
        Matrix frameToCropTransform = ImageUtils.getTransformationMatrix(bitmap.getWidth(), bitmap.getHeight(),
                inputSize, inputSize, 0, true);
        Matrix cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
        Bitmap croppedBitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(bitmap, frameToCropTransform, null);

        croppedBitmap.getPixels(intValues, 0, croppedBitmap.getWidth(), 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight());

        S_LOGGER.i("croppedBitmap height:" + croppedBitmap.getHeight());
        S_LOGGER.i("croppedBitmap width:" + croppedBitmap.getWidth());


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

        S_LOGGER.i("inputSize:" + inputSize);

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
        dTime = SystemClock.uptimeMillis() - startTime;
        S_LOGGER.i("pig Detect face tflite cost:" + (SystemClock.uptimeMillis() - startTime));

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


        int boxNum = 0;

        for (int s = 0; s < outputScores[0].length; s++) {
            if (outputScores[0][s] >= MIN_CONFIDENCE && outputScores[0][s] <= 1) {
                boxNum++;
            }
        }
        int mIndex = -1;
        if (boxNum == 1) {
            mIndex = 0;
        }
        if (boxNum > 1) {
            for (int i = 0; i < boxNum; i++) {
                float x0 = outputLocations[0][i][0];
                float y0 = outputLocations[0][i][1];
                float x1 = outputLocations[0][i][2];
                float y1 = outputLocations[0][i][3];

                if (x0 < 0.5f && y0 < 0.5f && x1 > 0.5f && y1 > 0.5f
                        && ((x1 - x0) > (1 / 6) || (y1 - y0) > (1 / 6))) {
                    mIndex = i;
                    break;
                }
            }

            if (mIndex == -1) {
                if (System.currentTimeMillis() - lastToastTime > 5000) {
                    CameraConnectionFragment_pig.showToast("请确保采集范围内只有一头牲畜。");
                    lastToastTime = System.currentTimeMillis();
                }
                saveBitMap(bitmap, "pigDetected_ng3", srcPigBitmapName);
                FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                        "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime&KeypointTime：" + kTime + "；totalTime：" + (dTime + kTime));
                if (dTimes < 6) {
                    dTimes++;

                    String mPath = null;
                    mPath = Global.mediaPayItem.getOriInfoBitmapFileName("/detect");

                    //保存原图
                    File file = new File(mPath);
                    FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                    //保存失败检测信息
                    FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
                }
                DetectorActivity_pig.resetParameter();
                return pigTFliteRecognitionAndPostureItem;
            }
        }

        if (boxNum < 1) {
            S_LOGGER.i("对象不足：" + boxNum);
            saveBitMap(bitmap, "pigDetected_ng4", srcPigBitmapName);
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime&KeypointTime：" + kTime + "；totalTime：" + (dTime + kTime));
            if (dTimes < 6) {
                dTimes++;

                String mPath = null;
                mPath = Global.mediaPayItem.getOriInfoBitmapFileName("/detect");

                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            DetectorActivity_pig.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }
        if (outputScores[0][mIndex] > 1 || outputScores[0][mIndex] < MIN_CONFIDENCE) {
            S_LOGGER.i("分值超出/分值不足：" + outputScores[0][mIndex]);
            saveBitMap(bitmap, "pigDetected_ng2", srcPigBitmapName);
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime&KeypointTime：" + kTime + "；totalTime：" + (dTime + kTime));
            if (dTimes < 6) {
                dTimes++;

                String mPath = null;
                mPath = Global.mediaPayItem.getOriInfoBitmapFileName("/detect");

                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            DetectorActivity_pig.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }

        S_LOGGER.i("outputScores0 %f:" + outputScores[0][mIndex]);
        S_LOGGER.i("OutClassifyResult0 %f:" + outputClassifyResult[0][mIndex]);
        S_LOGGER.i("OutPDetectNum %f:" + boxNum);
        float modelY0 = (float) outputLocations[0][mIndex][1];
        float modelX0 = (float) outputLocations[0][mIndex][0];
        float modelY1 = (float) outputLocations[0][mIndex][3];
        float modelX1 = (float) outputLocations[0][mIndex][2];

        com.orhanobut.logger.Logger.e("outputLocations: Xmin=" + outputLocations[0][mIndex][0] + ";Ymin="
                + outputLocations[0][mIndex][1] + ";Xmax=" + outputLocations[0][mIndex][2] + ";Ymax=" + outputLocations[0][mIndex][3]);

        float left = modelY0 * padSize - offsetY;
        float top = modelX0 * padSize - offsetX;
        float right = modelY1 * padSize - offsetY;
        float bottom = modelX1 * padSize - offsetX;
        if (left < 0 || top < 0 || right > padSize - 2 * offsetY || bottom > padSize - 2 * offsetX) {
            S_LOGGER.i("识别范围超出图像范围2");
            saveBitMap(bitmap, "pigDetected_ng5", srcPigBitmapName);

            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime&KeypointTime：" + kTime + "；totalTime：" + (dTime + kTime));
            if (dTimes < 6) {
                dTimes++;

                String mPath = null;
                mPath = Global.mediaPayItem.getOriInfoBitmapFileName("/detect");

                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            DetectorActivity_pig.resetParameter();

            return pigTFliteRecognitionAndPostureItem;
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>(1);
        // 设置猪头画框范围
        final RectF detection = new RectF(left, top, right, bottom);
        recognitions.add(
                new Recognition(
                        "",
                        "pigLite",
                        outputScores[0][mIndex],
                        detection, null));

        Trace.endSection(); // "recognizeImage"
        pigTFliteRecognitionAndPostureItem.setList(recognitions);

        saveBitMap(bitmap, "pigDetected_ok", srcPigBitmapName);
        //clip image
        Bitmap clipBitmap = innovation.utils.ImageUtils.clipBitmap(bitmap, modelY0, modelX0, modelY1, modelX1, 1.2f);
        if (clipBitmap == null) {
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime&KeypointTime：" + kTime + "；totalTime：" + (dTime + kTime));
            if (dTimes < 6) {
                dTimes++;
                String mPath = null;
                mPath = Global.mediaPayItem.getOriInfoBitmapFileName("/detect");
                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            DetectorActivity_pig.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }
        dNumber++;
        Bitmap padBitmap2SpRatio = padBitmap2SpRatio(clipBitmap, 1.0f);
//        int widthZoom = ZOOM, heightZoom = ZOOM;
        Bitmap resizeClipBitmap = zoomImage(padBitmap2SpRatio, ZOOM, ZOOM);


        // 调用模型判断关键点
        // keypoint detect
        kTime = System.currentTimeMillis();
        NewPigKeyPointAndRotationItem keypointResults = pigFaceKeyPointsDetector.pigRecognizePointImage(clipBitmap, oriBitmap);
        kTime = System.currentTimeMillis() - kTime;

        PredictRotationIterm predictRotationIterm = keypointResults.getPredictRotationIterm();

        // 调用模型判断角度分类
        if (predictRotationIterm == null) {
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime&KeypointTime：" + kTime + "；totalTime：" + (dTime + kTime));
            if (aTimes < 4) {
                aTimes++;
                String mPath = null;
                mPath = Global.mediaPayItem.getOriInfoBitmapFileName("/rota");
                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);

                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            DetectorActivity_pig.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }
        aNumber++;

        if (keypointResults.getPoints() == null) {
            FileUtils.saveInfoToTxtFile(oriInfoPath, srcPigBitmapName +
                    "；totalNum：" + allNumber + "；DetectTime：" + dTime + "；AngleTime&KeypointTime：" + kTime + "；totalTime：" + (dTime + kTime));
            if (kTimes < 4) {
                kTimes++;
                String mPath = null;
                mPath = Global.mediaPayItem.getOriInfoBitmapFileName("/key");

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
            DetectorActivity_pig.resetParameter();
            return pigTFliteRecognitionAndPostureItem;
        }

        //  keypoint 的判断逻辑
        posture = new PostureItem(
                (float) predictRotationIterm.rot_x,
                (float) predictRotationIterm.rot_y,
                (float) predictRotationIterm.rot_z,
                modelX0, modelY0, modelX1, modelY1, outputScores[0][mIndex],
                modelY0 * padSize, modelX0 * padSize,
                modelY1 * padSize, modelX1 * padSize, resizeClipBitmap, bitmap, oriBitmap);
        pigTFliteRecognitionAndPostureItem.setPostureItem(posture);
        AnimalClassifierResultIterm.pigAngleCalculateTFlite(pigTFliteRecognitionAndPostureItem.getPostureItem());


        return pigTFliteRecognitionAndPostureItem;
    }
}
