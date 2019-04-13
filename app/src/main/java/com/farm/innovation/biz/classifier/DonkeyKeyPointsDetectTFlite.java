package com.farm.innovation.biz.classifier;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.os.Trace;

import com.farm.innovation.biz.iterm.DonkeyFaceKeyPointsItem;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.biz.iterm.PredictRotationIterm;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.PointFloat;

import org.tensorflow.demo.FarmClassifier;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.farm.innovation.utils.ImageUtils.compressBitmap;
import static com.farm.innovation.utils.ImageUtils.padBitmap;
import static org.tensorflow.demo.FarmDetectorActivity.kTimes;

/**
 * Author by luolu, Date on 2018/10/9.
 * COMPANY：InnovationAI
 */

public class DonkeyKeyPointsDetectTFlite implements FarmClassifier {
    private static final Logger sLogger = new Logger(DonkeyKeyPointsDetectTFlite.class);
    private static final boolean DEBUG = false;
    private static final float MIN_CONFIDENCE = (float) 0.6;
    private static final String TAG = "DonkeyKeyPointsDetectTFlite";

    static {
        System.loadLibrary("tensorflow_demo");
    }

    // Only return this many results.
    private static final int NUM_DETECTIONS = 10;
    private boolean isModelQuantized;
    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;
    // Number of threads in the java app
    private static final int NUM_THREADS = 4;
    // Config values.
    private int inputSize;
    private int[] intValues;
    private byte[][] keyPoints;
    private byte[][] exists;

    private ByteBuffer imgData;
    private Interpreter tfLite;
    private int counterSum;
    public static boolean donkeyKeypointsK1;
    public static boolean donkeyKeypointsK2;
    public static boolean donkeyKeypointsK3;


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
     *  @param assetManager  The asset manager to be used to load assets.
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
        final DonkeyKeyPointsDetectTFlite d = new DonkeyKeyPointsDetectTFlite();

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
        d.keyPoints = new byte[1][22];
        d.exists = new byte[1][11];
        return d;
    }

    private DonkeyKeyPointsDetectTFlite() {
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
    public PredictRotationIterm donkeyRotationPredictionItemTFlite(Bitmap bitmap, Bitmap oriBitmap) {
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
    public RecognitionAndPostureItem pigRecognitionAndPostureItemTFlite(Bitmap bitmap, Bitmap oriBitmap) {
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


    public static char[] convertBytes2Uint8s(byte[] bytes) {
        int len = bytes.length;
        char[] uint8s = new char[len];
        for (int i = 0; i < len; i++) {
            uint8s[i] = convertByte2Uint8(bytes[i]);
        }
        return uint8s;
    }

    public static char convertByte2Uint8(byte b) {
        // char will be promoted to int for char don't support & operator
        // & 0xff could make negatvie value to positive
        return (char) (b & 0xff);
    }
    @Override
    public List<PointFloat> recognizePointImage(Bitmap bitmap, Bitmap oriBitmap) {
        if (bitmap == null) {
            return null;
        }
        donkeyKeypointsK1 = false;
        donkeyKeypointsK2 = false;
        donkeyKeypointsK3 = false;
//        sLogger.i("bitmap height:" + bitmap.getHeight());
//        sLogger.i("bitmap width:" + bitmap.getWidth());
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int padSize = Math.max(height, width);
        Bitmap padBitmap = padBitmap(bitmap);

        Matrix frameToCropTransform = ImageUtils.getTransformationMatrix(padBitmap.getWidth(), padBitmap.getHeight(),
                inputSize, inputSize, 0, true);
        Matrix cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
        Bitmap croppedBitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(padBitmap, frameToCropTransform, null);
        // TODO: 2018/10/5 By:LuoLu
        Matrix frameToCropTransform1 = ImageUtils.getTransformationMatrix(padBitmap.getWidth(), padBitmap.getHeight(),
                width, height, 0, true);
        Matrix cropToFrameTransform1 = new Matrix();
        frameToCropTransform1.invert(cropToFrameTransform1);
        Canvas canvas1 = new Canvas(padBitmap);
        canvas1.drawBitmap(padBitmap, frameToCropTransform1, null);

        float[] inputValues = new float[inputSize * inputSize * 3];
        croppedBitmap.getPixels(intValues, 0, croppedBitmap.getWidth(), 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight());
        imgData.rewind();
//    floatBuf.rewind();


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

        keyPoints = new byte[1][22];
        exists = new byte[1][11];

        sLogger.i("inputSize:" + inputSize);

        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, keyPoints);
        outputMap.put(1, exists);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        final long startTime = SystemClock.uptimeMillis();
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
        sLogger.i("耗时4测试,DonkeyKeyPointsDetectTFlite cost:" + (SystemClock.uptimeMillis() - startTime));

        Trace.endSection();
        float x = 404, y = 404;
        List<PointFloat> points = new ArrayList<>();
        PointFloat point0 = new PointFloat(x, y);
        PointFloat point1 = new PointFloat(x, y);
        PointFloat point2 = new PointFloat(x, y);
        PointFloat point3 = new PointFloat(x, y);
        PointFloat point4 = new PointFloat(x, y);
        PointFloat point5 = new PointFloat(x, y);
        PointFloat point6 = new PointFloat(x, y);
        PointFloat point7 = new PointFloat(x, y);
        PointFloat point8 = new PointFloat(x, y);
        PointFloat point9 = new PointFloat(x, y);
        PointFloat point10 = new PointFloat(x, y);

        char charsOutkeyPoints0 = convertByte2Uint8(keyPoints[0][0]);
        char charsOutkeyPoints1 = convertByte2Uint8(keyPoints[0][1]);
        char charsOutkeyPoints2 = convertByte2Uint8(keyPoints[0][2]);
        char charsOutkeyPoints3 = convertByte2Uint8(keyPoints[0][3]);
        char charsOutkeyPoints4 = convertByte2Uint8(keyPoints[0][4]);
        char charsOutkeyPoints5 = convertByte2Uint8(keyPoints[0][5]);
        char charsOutkeyPoints6 = convertByte2Uint8(keyPoints[0][6]);
        char charsOutkeyPoints7 = convertByte2Uint8(keyPoints[0][7]);
        char charsOutkeyPoints8 = convertByte2Uint8(keyPoints[0][8]);
        char charsOutkeyPoints9 = convertByte2Uint8(keyPoints[0][9]);
        char charsOutkeyPoints10 = convertByte2Uint8(keyPoints[0][10]);
        char charsOutkeyPoints11 = convertByte2Uint8(keyPoints[0][11]);
        char charsOutkeyPoints12 = convertByte2Uint8(keyPoints[0][12]);
        char charsOutkeyPoints13 = convertByte2Uint8(keyPoints[0][13]);
        char charsOutkeyPoints14 = convertByte2Uint8(keyPoints[0][14]);
        char charsOutkeyPoints15 = convertByte2Uint8(keyPoints[0][15]);
        char charsOutkeyPoints16 = convertByte2Uint8(keyPoints[0][16]);
        char charsOutkeyPoints17 = convertByte2Uint8(keyPoints[0][17]);
        char charsOutkeyPoints18 = convertByte2Uint8(keyPoints[0][18]);
        char charsOutkeyPoints19 = convertByte2Uint8(keyPoints[0][19]);
        char charsOutkeyPoints20 = convertByte2Uint8(keyPoints[0][20]);
        char charsOutkeyPoints21 = convertByte2Uint8(keyPoints[0][21]);
        char charsOutExists0 = convertByte2Uint8(exists[0][0]);
        char charsOutExists1 = convertByte2Uint8(exists[0][1]);
        char charsOutExists2 = convertByte2Uint8(exists[0][2]);
        char charsOutExists3 = convertByte2Uint8(exists[0][3]);
        char charsOutExists4 = convertByte2Uint8(exists[0][4]);
        char charsOutExists5 = convertByte2Uint8(exists[0][5]);
        char charsOutExists6 = convertByte2Uint8(exists[0][6]);
        char charsOutExists7 = convertByte2Uint8(exists[0][7]);
        char charsOutExists8 = convertByte2Uint8(exists[0][8]);
        char charsOutExists9 = convertByte2Uint8(exists[0][9]);
        char charsOutExists10 = convertByte2Uint8(exists[0][10]);

        double quantization = 0.00390625;
        double exists = 0.5;

        int[] pointsExists = new int[11];
        DonkeyFaceKeyPointsItem donkeyFaceKeyPointsItem = DonkeyFaceKeyPointsItem.getInstance();

        if ((charsOutExists0 - 0) * quantization > exists) {
            point0.set((float) (charsOutkeyPoints0 * quantization ), (float) (charsOutkeyPoints1 * quantization ));
            points.add(point0);
            pointsExists[0] = 1;
            donkeyFaceKeyPointsItem.setPointsExists0(pointsExists[0]);
            donkeyFaceKeyPointsItem.setPointFloat0(point0);
            sLogger.i("关键点1:");
            sLogger.i("获取的point1 %d:" + point0.toString());
        }else {
            pointsExists[0] = 0;
            donkeyFaceKeyPointsItem.setPointsExists0(pointsExists[0]);
            donkeyFaceKeyPointsItem.setPointFloat0(point0);
        }
        if ((charsOutExists1 - 0) * quantization > exists) {
            point1.set((float) (charsOutkeyPoints2 * quantization ), (float) (charsOutkeyPoints3 * quantization ));
            points.add(point1);
            pointsExists[1] = 1;
            donkeyFaceKeyPointsItem.setPointsExists1(pointsExists[1]);
            donkeyFaceKeyPointsItem.setPointFloat1(point1);
            sLogger.i("关键点2:");
            sLogger.i("获取的point2 %d:" + point1.toString());
        }else {
            pointsExists[1] = 0;
            donkeyFaceKeyPointsItem.setPointsExists1(pointsExists[1]);
            donkeyFaceKeyPointsItem.setPointFloat1(point1);
        }
        if ((charsOutExists2 - 0) * quantization > exists) {
            point2.set((float) (charsOutkeyPoints4 * quantization ), (float) (charsOutkeyPoints5 * quantization ));
            points.add(point2);
            pointsExists[2] = 1;
            donkeyFaceKeyPointsItem.setPointsExists2(pointsExists[2]);
            donkeyFaceKeyPointsItem.setPointFloat2(point2);
            sLogger.i("关键点3:");
            sLogger.i("获取的point3 %d:" + point2.toString());
        }else {
            pointsExists[2] = 0;
            donkeyFaceKeyPointsItem.setPointsExists2(pointsExists[2]);
            donkeyFaceKeyPointsItem.setPointFloat2(point2);
        }
        if ((charsOutExists3 - 0) * quantization > exists) {
            point3.set((float) (charsOutkeyPoints6 * quantization ), (float) (charsOutkeyPoints7 * quantization ));
            points.add(point3);
            pointsExists[3] = 1;
            donkeyFaceKeyPointsItem.setPointsExists3(pointsExists[3]);
            donkeyFaceKeyPointsItem.setPointFloat3(point3);
            sLogger.i("关键点4:");
            sLogger.i("获取的point4 %d:" + point3.toString());
        }else {
            pointsExists[3] = 0;
            donkeyFaceKeyPointsItem.setPointsExists3(pointsExists[3]);
            donkeyFaceKeyPointsItem.setPointFloat3(point3);
        }
        if ((charsOutExists4 - 0) * quantization > exists) {
            point4.set((float) (charsOutkeyPoints8 * quantization ), (float) (charsOutkeyPoints9 * quantization ));
            points.add(point4);
            pointsExists[4] = 1;
            donkeyFaceKeyPointsItem.setPointsExists4(pointsExists[4]);
            donkeyFaceKeyPointsItem.setPointFloat4(point4);
            sLogger.i("关键点5:");
            sLogger.i("获取的point5 %d:" + point4.toString());
        }else {
            pointsExists[4] = 0;
            donkeyFaceKeyPointsItem.setPointsExists4(pointsExists[4]);
            donkeyFaceKeyPointsItem.setPointFloat4(point4);
        }
        if ((charsOutExists5 - 0) * quantization > exists) {
            point5.set((float) (charsOutkeyPoints10 * quantization ), (float) (charsOutkeyPoints11 * quantization ));
            points.add(point5);
            pointsExists[5] = 1;
            donkeyFaceKeyPointsItem.setPointsExists5(pointsExists[5]);
            donkeyFaceKeyPointsItem.setPointFloat5(point5);
            sLogger.i("关键点6:");
            sLogger.i("获取的point6 %d:" + point5.toString());
        }else {
            pointsExists[5] = 0;
            donkeyFaceKeyPointsItem.setPointsExists5(pointsExists[5]);
            donkeyFaceKeyPointsItem.setPointFloat5(point5);
        }
        if ((charsOutExists6 - 0) * quantization > exists) {
            point6.set((float) (charsOutkeyPoints12 * quantization ), (float) (charsOutkeyPoints13 * quantization ));
            points.add(point6);
            pointsExists[6] = 1;
            donkeyFaceKeyPointsItem.setPointsExists6(pointsExists[6]);
            donkeyFaceKeyPointsItem.setPointFloat6(point6);
            sLogger.i("关键点7:");
            sLogger.i("获取的point7 %d:" + point6.toString());
        }else {
            pointsExists[6] = 0;
            donkeyFaceKeyPointsItem.setPointsExists6(pointsExists[6]);
            donkeyFaceKeyPointsItem.setPointFloat6(point6);
        }
        if ((charsOutExists7 - 0) * quantization > exists) {
            point7.set((float) (charsOutkeyPoints14 * quantization ), (float) (charsOutkeyPoints15 * quantization ));
            points.add(point7);
            pointsExists[7] = 1;
            donkeyFaceKeyPointsItem.setPointsExists7(pointsExists[7]);
            donkeyFaceKeyPointsItem.setPointFloat7(point7);
            sLogger.i("关键点8:");
            sLogger.i("获取的point8 %d:" + point7.toString());
        }else {
            pointsExists[7] = 0;
            donkeyFaceKeyPointsItem.setPointsExists7(pointsExists[7]);
            donkeyFaceKeyPointsItem.setPointFloat7(point7);
        }
        if ((charsOutExists8 - 0) * quantization > exists) {
            point8.set((float) (charsOutkeyPoints16 * quantization ), (float) (charsOutkeyPoints17 * quantization ));
            points.add(point8);
            pointsExists[8] = 1;
            donkeyFaceKeyPointsItem.setPointsExists8(pointsExists[8]);
            donkeyFaceKeyPointsItem.setPointFloat8(point8);
            sLogger.i("关键点9:");
            sLogger.i("获取的point9 %d:" + point8.toString());
        }else {
            pointsExists[8] = 0;
            donkeyFaceKeyPointsItem.setPointsExists8(pointsExists[8]);
            donkeyFaceKeyPointsItem.setPointFloat8(point8);
        }
        if ((charsOutExists9 - 0) * quantization > exists) {
            point9.set((float) (charsOutkeyPoints18 * quantization ), (float) (charsOutkeyPoints19 * quantization ));
            points.add(point9);
            pointsExists[9] = 1;
            donkeyFaceKeyPointsItem.setPointsExists9(pointsExists[9]);
            donkeyFaceKeyPointsItem.setPointFloat9(point9);
            sLogger.i("关键点10:");
            sLogger.i("获取的point10 %d:" + point9.toString());
        }else {
            pointsExists[9] = 0;
            donkeyFaceKeyPointsItem.setPointsExists9(pointsExists[9]);
            donkeyFaceKeyPointsItem.setPointFloat9(point9);
        }
        if ((charsOutExists10 - 0) * quantization > exists) {
            point10.set((float) (charsOutkeyPoints20 * quantization ), (float) (charsOutkeyPoints21 * quantization ));
            points.add(point10);
            pointsExists[10] = 1;
            donkeyFaceKeyPointsItem.setPointsExists10(pointsExists[10]);
            donkeyFaceKeyPointsItem.setPointFloat10(point10);
            sLogger.i("关键点11:");
            sLogger.i("获取的point11 %d:" + point10.toString());
        }else {
            pointsExists[10] = 0;
            donkeyFaceKeyPointsItem.setPointsExists10(pointsExists[10]);
            donkeyFaceKeyPointsItem.setPointFloat10(point10);
        }
        sLogger.i("获取的关键点 %d:" + points.toString());

        String unsuccessTXTPath = "";
        if (FarmGlobal.model == Model.BUILD.value()) {
            unsuccessTXTPath = FarmGlobal.mediaInsureItem.getUnsuccessInfoTXTFileName();
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            unsuccessTXTPath = FarmGlobal.mediaPayItem.getUnsuccessInfoTXTFileName();
        }
        String contenType = "KeypointResult：";
        contenType += DonkeyFaceDetectTFlite.srcDonkeyBitmapName + "; ";
        contenType += "point0 = " + donkeyFaceKeyPointsItem.getPointFloat0().toString() + "; ";
        contenType += "point1 = " + donkeyFaceKeyPointsItem.getPointFloat1().toString() + "; ";
        contenType += "point2 = " + donkeyFaceKeyPointsItem.getPointFloat2().toString() + "; ";
        contenType += "point3 = " + donkeyFaceKeyPointsItem.getPointFloat3().toString() + "; ";
        contenType += "point4 = " + donkeyFaceKeyPointsItem.getPointFloat4().toString() + "; ";
        contenType += "point5 = " + donkeyFaceKeyPointsItem.getPointFloat5().toString() + "; ";
        contenType += "point6 = " + donkeyFaceKeyPointsItem.getPointFloat6().toString() + "; ";
        contenType += "point7 = " + donkeyFaceKeyPointsItem.getPointFloat7().toString() + "; ";
        contenType += "point8 = " + donkeyFaceKeyPointsItem.getPointFloat8().toString() + "; ";
        contenType += "point9 = " + donkeyFaceKeyPointsItem.getPointFloat9().toString() + "; ";
        contenType += "point10 = " + donkeyFaceKeyPointsItem.getPointFloat10().toString() + "; ";


        //画关键点
//        Canvas canvasDrawPoints = new Canvas(padBitmap);
        if (pointsExists[6] + pointsExists[7] +pointsExists[8]+ pointsExists[9]+ pointsExists[10] == 5
                && pointsExists[1]+ pointsExists[2]+ pointsExists[3] == 0){
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point6,padSize,"6");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point7,padSize,"7");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point8,padSize,"8");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point9,padSize,"9");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point10,padSize,"10");
//            com.farm.innovation.utils.ImageUtils.saveBitmap(padBitmap,"donkeyKeyP1",srcDonkeyBitmapName);
            donkeyKeypointsK1 = true;
        }else if (pointsExists[0] + pointsExists[5] +pointsExists[6] == 3
                && pointsExists[7]+ pointsExists[8]+ pointsExists[9] > 0
                && pointsExists[1]+ pointsExists[2]+ pointsExists[3] > 0){
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point0,padSize,"0");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point1,padSize,"1");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point2,padSize,"2");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point3,padSize,"3");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point5,padSize,"5");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point6,padSize,"6");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point7,padSize,"7");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point8,padSize,"8");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point9,padSize,"9");
//            com.farm.innovation.utils.ImageUtils.saveBitmap(padBitmap,"donkeyKeyP2",srcDonkeyBitmapName);
            donkeyKeypointsK2 = true;
        }else if (pointsExists[0] + pointsExists[1] +pointsExists[2]+ pointsExists[3]+ pointsExists[4] == 5
                && pointsExists[7]+ pointsExists[8]+ pointsExists[9] == 0){
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point0,padSize,"0");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point1,padSize,"1");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point2,padSize,"2");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point3,padSize,"3");
//            com.farm.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point4,padSize,"4");
//            com.farm.innovation.utils.ImageUtils.saveBitmap(padBitmap,"donkeyKeyP3",srcDonkeyBitmapName);
            donkeyKeypointsK3 = true;
        }else {
//            if (points != null) {
//                Paint boxPaint = new Paint();
//                boxPaint.setColor(Color.RED);
//                boxPaint.setStyle(Paint.Style.STROKE);
//                boxPaint.setStrokeWidth(8.0f);
//                boxPaint.setStrokeCap(Paint.Cap.ROUND);
//                boxPaint.setStrokeJoin(Paint.Join.ROUND);
//                boxPaint.setStrokeMiter(100);
//                boxPaint.setColor(Color.YELLOW);
//                for (PointFloat point : points) {
//                    canvasDrawPoints.drawCircle(point.getX() * padSize, point.getY() * padSize, 3, boxPaint);
//                }
//
//            }
//            com.farm.innovation.utils.ImageUtils.saveBitmap(padBitmap,"donkeyKeyP10",srcDonkeyBitmapName);
            if(kTimes < 4){
                String mPath = null;
                if (FarmGlobal.model == Model.BUILD.value()) {
                    mPath = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("/key");
                } else if (FarmGlobal.model == Model.VERIFY.value()) {
                    mPath = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("/key");
                }
                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            return null;


        }

//        sLogger.i("关键点分类器图片数量:" + counterSum);
        return points;
    }
}
