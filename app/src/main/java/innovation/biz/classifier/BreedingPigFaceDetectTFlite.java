package innovation.biz.classifier;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.lite.Interpreter;

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

import innovation.biz.iterm.PostureItem;
import innovation.biz.iterm.PredictRotationIterm;

import static innovation.utils.ImageUtils.padBitmap2SpRatio;
import static innovation.utils.ImageUtils.zoomImage;
import static org.tensorflow.demo.DetectorActivity_new.offsetX;
import static org.tensorflow.demo.DetectorActivity_new.offsetY;

/**
 * @author luolu .2018/8/4
 */
public class BreedingPigFaceDetectTFlite{
    private static final Logger sLogger = new Logger(BreedingPigFaceDetectTFlite.class);

    // 2018/12/18 hedazhi edit start
    //private static final float MIN_CONFIDENCE = (float) 0.7;

    // 检测模型阈值：0.5
    private static final float MIN_CONFIDENCE = 0.5f;

    // 2018/12/18 hedazhi edit end
    private static final String TAG = "PigFaceDetectTFlite";
    // Only return this many results.
    private static final int NUM_DETECTIONS = 10;
    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;
    // Number of threads in the java app
    private static final int NUM_THREADS = 4;

    //private final Classifier donkeyFaceKeyPointsTFDetector;
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
    private Context context;
    public static String srcPigBitmapName;

    public static RecognitionAndPostureItem recognitionAndPostureItem;

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
    public static BreedingPigFaceDetectTFlite create(
            final AssetManager assetManager,
            final String modelFilename,
            final String labelFilename,
            final int inputSize,
            final boolean isQuantized)
            throws IOException {
        final BreedingPigFaceDetectTFlite d = new BreedingPigFaceDetectTFlite();
        d.inputSize = inputSize;
        try {
            d.tfLite = new Interpreter(loadModelFile(assetManager, modelFilename));
        } catch (Exception e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
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

    public String getStatString() {
        return "tflite";
    }

    public void close() {
        tfLite.close();
    }

    private void saveBitMap(Bitmap bmp, String childFileName, String bitmapFileName) {
        return;
    }

    public RecognitionAndPostureItem pigRecognitionAndPostureItemTFlite(Bitmap bitmap) {
        List<PostureItem> postureItemList = new ArrayList<>();

        if (bitmap == null) {
            recognitionAndPostureItem = null;
            return null;
        }

        recognitionAndPostureItem = new RecognitionAndPostureItem();

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int padSize = height;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        srcPigBitmapName = sdf.format(new Date(System.currentTimeMillis())) + ".jpeg";
//        saveBitMap(bitmap, "pigSrcImage", srcPigBitmapName);
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
        outputMap.put(2, outputScores);//分值
        outputMap.put(3, outputDetectNum);//检测框数量
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        final long startTime = SystemClock.uptimeMillis();


        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

        sLogger.i("pig Detect face tflite cost:" + (SystemClock.uptimeMillis() - startTime));

        Trace.endSection();

        /*if (outputDetectNum[0] > 1) {
            if(System.currentTimeMillis() - lastToastTime > 5000) {
                CameraConnectionFragment.showToast("请确保采集范围内只有一头牲畜。");
                lastToastTime = System.currentTimeMillis();
            }
            saveBitMap(bitmap, "pigDetected_ng3", srcPigBitmapName);
            return pigTFliteRecognitionAndPostureItem;
        }*/

        if (outputDetectNum[0] < 1) {
            sLogger.i("对象不足：" + outputDetectNum[0]);
//            saveBitMap(bitmap, "pigDetected_ng4", srcPigBitmapName);
            return recognitionAndPostureItem;
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();

        for(int i = 0; i < outputScores[0].length;++i){
            if (outputScores[0][i] > 1 || outputScores[0][i] < MIN_CONFIDENCE) {
                sLogger.i("分值超出/分值不足：" + outputScores[0][0]);
//            saveBitMap(bitmap, "pigDetected_ng2", srcPigBitmapName);
                continue;
            }
            sLogger.i("outputScores0 %f:" + outputScores[0][0]);
            sLogger.i("OutClassifyResult0 %f:" + outputClassifyResult[0][0]);
            sLogger.i("OutPDetectNum %f:" + outputDetectNum[0]);
            //获取当前坐标
            float modelY0 = (float) outputLocations[0][i][1];
            float modelX0 = (float) outputLocations[0][i][0];
            float modelY1 = (float) outputLocations[0][i][3];
            float modelX1 = (float) outputLocations[0][i][2];

            Log.e(TAG, "outputLocations: Xmin="+modelX0+";Ymin="
                    + modelY0+";Xmax="+modelX1 +";Ymax="+modelY1);
            //计算左上右下
            float left = modelY0 * padSize - offsetY;
            float top = modelX0 * padSize - offsetX;
            float right = modelY1 * padSize - offsetY;
            float bottom = modelX1 * padSize - offsetX;

            //判断是否超出识别范围
            if (left < 0 || top < 0 || right > padSize - 2 * offsetY || bottom > padSize - 2 * offsetX) {
                sLogger.i("识别范围超出图像范围2");
                continue;
            }

            // 设置猪头画框范围
            final RectF detection = new RectF(left, top, right, bottom);
            recognitions.add(
                    new Recognition(
                            "",
                            "pigLite",
                            outputScores[0][i],
                            detection, null));
            //clip image
            Bitmap clipBitmap = innovation.utils.ImageUtils.clipBitmap(bitmap, modelY0, modelX0, modelY1, modelX1, 1.2f);
            if (clipBitmap == null) {
                continue;
            }

            Bitmap padBitmap2SpRatio = padBitmap2SpRatio(clipBitmap, 1.0f);
            int widthZoom = 320, heightZoom = 320;
            Bitmap resizeClipBitmap = zoomImage(padBitmap2SpRatio, widthZoom, heightZoom);

            PostureItem posture  = new PostureItem(
                    0,
                    0,
                    0,
                    modelX0, modelY0, modelX1, modelY1, outputScores[0][i],
                    modelY0 * padSize, modelX0 * padSize,
                    modelY1 * padSize, modelX1 * padSize, resizeClipBitmap, bitmap);

            postureItemList.add(posture);
        }

        Trace.endSection(); // "recognizeImage"
        recognitionAndPostureItem.setList(recognitions);

        recognitionAndPostureItem.setPostureItem(postureItemList);
//        AnimalClassifierResultIterm.pigAngleCalculateTFlite(recognitionAndPostureItem.getPostureItem());

        return recognitionAndPostureItem;
    }


    public class RecognitionAndPostureItem {
        private List<Recognition> list;

//        private PostureItem postureItem;
        private PredictRotationIterm predictRotationIterm;
        private List<PostureItem> postureItemLis;

        public PredictRotationIterm getPredictRotationIterm() {
            return predictRotationIterm;
        }

        public void setPredictRotationIterm(PredictRotationIterm predictRotationIterm) {
            this.predictRotationIterm = predictRotationIterm;
        }

        public List<Recognition> getList() {
            return list;
        }

        public void setList(List<Recognition> list) {
            this.list = list;
        }

        public List<PostureItem> getPostureItem() {
            return postureItemLis;
        }

        public void setPostureItem(List<PostureItem> postureItemLis) {
            this.postureItemLis = postureItemLis;
        }


        //        public PostureItem getPostureItem() {
//            return postureItem;
//        }
//
//        public void setPostureItem(PostureItem postureItem) {
//            this.postureItem = postureItem;
//        }
    }


    public class Recognition {
        /**
         * InSureCompanyBean unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final String id;

        /**
         * Display name for the recognition.
         */
        private final String title;

        /**
         * InSureCompanyBean sortable score for how good the recognition is relative to others. Higher should be better.
         */
        private final Float confidence;

        /** Optional location within the source image for the location of the recognized object. */
        private RectF location;
        private  List<Point> points;


        public Recognition(
                final String id, final String title, final Float confidence,
                final RectF location, List<Point> points) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
            this.points = points;
        }
        public Recognition(
                final String id, final String title, final Float confidence, final RectF location) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }
        public List<Point> getPoints() {
            return points;
        }

        public void setPoints(List<Point> points) {
            this.points = points;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }


}
