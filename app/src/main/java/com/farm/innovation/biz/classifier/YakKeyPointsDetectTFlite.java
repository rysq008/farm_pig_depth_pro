package com.farm.innovation.biz.classifier;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.os.Trace;

import com.farm.innovation.biz.iterm.CowFaceKeyPointsItem;
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

//import android.graphics.PointFloat;

/**
 * Author by luolu, Date on 2018/10/9.
 * COMPANY：InnovationAI
 */

public class YakKeyPointsDetectTFlite implements FarmClassifier {
    private static final Logger sLogger = new Logger(YakKeyPointsDetectTFlite.class);
    private static final boolean DEBUG = false;
    private static final float MIN_CONFIDENCE = (float) 0.6;
    private static final String TAG = "CowKeyPointsDetectTFlite";

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
    public static boolean yakKeypointsDetectedK1;
    public static boolean yakKeypointsDetectedK2;
    public static boolean yakKeypointsDetectedK3;


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
        final YakKeyPointsDetectTFlite d = new YakKeyPointsDetectTFlite();

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
        d.keyPoints = new byte[1][26];
        d.exists = new byte[1][13];
        return d;
    }

    private YakKeyPointsDetectTFlite() {
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
    public RecognitionAndPostureItem donkeyRecognitionAndPostureItemTFlite(Bitmap bitmap , Bitmap oriBitmap) {
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
        yakKeypointsDetectedK1 = false;
        yakKeypointsDetectedK2 = false;
        yakKeypointsDetectedK3 = false;
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

        croppedBitmap.getPixels(intValues, 0, croppedBitmap.getWidth(), 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight());

        sLogger.i("croppedBitmap height:" + croppedBitmap.getHeight());
        sLogger.i("croppedBitmap width:" + croppedBitmap.getWidth());

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

        //定义关键点模型变量
        keyPoints = new byte[1][26];
        exists = new byte[1][13];

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
        sLogger.i("CowKeyPointsDetectTFlite cost:" + (SystemClock.uptimeMillis() - startTime));

        Trace.endSection();
        float x = 404, y = 404;
        List<PointFloat> pointFloats = new ArrayList<>();
        PointFloat pointFloat0 = new PointFloat(x, y);
        PointFloat pointFloat1 = new PointFloat(x, y);
        PointFloat pointFloat2 = new PointFloat(x, y);
        PointFloat pointFloat3 = new PointFloat(x, y);
        PointFloat pointFloat4 = new PointFloat(x, y);
        PointFloat pointFloat5 = new PointFloat(x, y);
        PointFloat pointFloat6 = new PointFloat(x, y);
        PointFloat pointFloat7 = new PointFloat(x, y);
        PointFloat pointFloat8 = new PointFloat(x, y);
        PointFloat pointFloat9 = new PointFloat(x, y);
        PointFloat pointFloat10 = new PointFloat(x, y);
        PointFloat pointFloat11 = new PointFloat(x, y);
        PointFloat pointFloat12 = new PointFloat(x, y);

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
        char charsOutkeyPoints22 = convertByte2Uint8(keyPoints[0][22]);
        char charsOutkeyPoints23 = convertByte2Uint8(keyPoints[0][23]);
        char charsOutkeyPoints24 = convertByte2Uint8(keyPoints[0][24]);
        char charsOutkeyPoints25 = convertByte2Uint8(keyPoints[0][25]);
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
        char charsOutExists11 = convertByte2Uint8(exists[0][11]);
        char charsOutExists12 = convertByte2Uint8(exists[0][12]);

        double quantization = 0.00390625;
        double exists = 0.5;
        int[] pointsExists = new int[13];
        CowFaceKeyPointsItem yakFaceKeyPointsItem = CowFaceKeyPointsItem.getInstance();

        if ((charsOutExists0 - 0) * quantization > exists) {
            pointFloat0.set((float) (charsOutkeyPoints0 * quantization), (float) (charsOutkeyPoints1 * quantization));
            pointFloats.add(pointFloat0);
            pointsExists[0] = 1;
            yakFaceKeyPointsItem.setPointsExists0(pointsExists[0]);
            yakFaceKeyPointsItem.setPointFloat0(pointFloat0);
            sLogger.i("关键点1:");
            sLogger.i("获取的point0 %d:" + pointFloat0.toString());
        } else {
            pointsExists[0] = 0;
            yakFaceKeyPointsItem.setPointsExists0(pointsExists[0]);
            yakFaceKeyPointsItem.setPointFloat0(pointFloat0);
        }
        if ((charsOutExists1 - 0) * quantization > exists) {
            pointFloat1.set((float) (charsOutkeyPoints2 * quantization), (float) (charsOutkeyPoints3 * quantization));
            pointFloats.add(pointFloat1);
            pointsExists[1] = 1;
            yakFaceKeyPointsItem.setPointsExists1(pointsExists[1]);
            yakFaceKeyPointsItem.setPointFloat1(pointFloat1);
            sLogger.i("关键点2:");
            sLogger.i("获取的point1 %d:" + pointFloat1.toString());
        } else {
            pointsExists[1] = 0;
            yakFaceKeyPointsItem.setPointsExists1(pointsExists[1]);
            yakFaceKeyPointsItem.setPointFloat1(pointFloat1);
        }
        if ((charsOutExists2 - 0) * quantization > exists) {
            pointFloat2.set((float) (charsOutkeyPoints4 * quantization), (float) (charsOutkeyPoints5 * quantization));
            pointFloats.add(pointFloat2);
            pointsExists[2] = 1;
            yakFaceKeyPointsItem.setPointsExists2(pointsExists[2]);
            yakFaceKeyPointsItem.setPointFloat2(pointFloat2);
            sLogger.i("关键点3:");
            sLogger.i("获取的point2 %d:" + pointFloat2.toString());
        } else {
            pointsExists[2] = 0;
            yakFaceKeyPointsItem.setPointsExists2(pointsExists[2]);
            yakFaceKeyPointsItem.setPointFloat2(pointFloat2);
        }
        if ((charsOutExists3 - 0) * quantization > exists) {
            pointFloat3.set((float) (charsOutkeyPoints6 * quantization), (float) (charsOutkeyPoints7 * quantization));
            pointFloats.add(pointFloat3);
            pointsExists[3] = 1;
            yakFaceKeyPointsItem.setPointsExists3(pointsExists[3]);
            yakFaceKeyPointsItem.setPointFloat3(pointFloat3);
            sLogger.i("关键点4:");
            sLogger.i("获取的point3 %d:" + pointFloat3.toString());
        } else {
            pointsExists[3] = 0;
            yakFaceKeyPointsItem.setPointsExists3(pointsExists[3]);
            yakFaceKeyPointsItem.setPointFloat3(pointFloat3);
        }
        if ((charsOutExists4 - 0) * quantization > exists) {
            pointFloat4.set((float) (charsOutkeyPoints8 * quantization), (float) (charsOutkeyPoints9 * quantization));
            pointFloats.add(pointFloat4);
            pointsExists[4] = 1;
            yakFaceKeyPointsItem.setPointsExists4(pointsExists[4]);
            yakFaceKeyPointsItem.setPointFloat4(pointFloat4);
            sLogger.i("关键点5:");
            sLogger.i("获取的point4 %d:" + pointFloat4.toString());
        } else {
            pointsExists[4] = 0;
            yakFaceKeyPointsItem.setPointsExists4(pointsExists[4]);
            yakFaceKeyPointsItem.setPointFloat4(pointFloat4);
        }
        if ((charsOutExists5 - 0) * quantization > exists) {
            pointFloat5.set((float) (charsOutkeyPoints10 * quantization), (float) (charsOutkeyPoints11 * quantization));
            pointFloats.add(pointFloat5);
            pointsExists[5] = 1;
            yakFaceKeyPointsItem.setPointsExists5(pointsExists[5]);
            yakFaceKeyPointsItem.setPointFloat5(pointFloat5);
            sLogger.i("关键点6:");
            sLogger.i("获取的point5 %d:" + pointFloat5.toString());
        } else {
            pointsExists[5] = 0;
            yakFaceKeyPointsItem.setPointsExists5(pointsExists[5]);
            yakFaceKeyPointsItem.setPointFloat5(pointFloat5);
        }
        if ((charsOutExists6 - 0) * quantization > exists) {
            pointFloat6.set((float) (charsOutkeyPoints12 * quantization), (float) (charsOutkeyPoints13 * quantization));
            pointFloats.add(pointFloat6);
            pointsExists[6] = 1;
            yakFaceKeyPointsItem.setPointsExists6(pointsExists[6]);
            yakFaceKeyPointsItem.setPointFloat6(pointFloat6);
            sLogger.i("关键点7:");
            sLogger.i("获取的point6 %d:" + pointFloat6.toString());
        } else {
            pointsExists[6] = 0;
            yakFaceKeyPointsItem.setPointsExists6(pointsExists[6]);
            yakFaceKeyPointsItem.setPointFloat6(pointFloat6);
        }
        if ((charsOutExists7 - 0) * quantization > exists) {
            pointFloat7.set((float) (charsOutkeyPoints14 * quantization), (float) (charsOutkeyPoints15 * quantization));
            pointFloats.add(pointFloat7);
            pointsExists[7] = 1;
            yakFaceKeyPointsItem.setPointsExists7(pointsExists[7]);
            yakFaceKeyPointsItem.setPointFloat7(pointFloat7);
            sLogger.i("关键点8:");
            sLogger.i("获取的point7 %d:" + pointFloat7.toString());
        } else {
            pointsExists[7] = 0;
            yakFaceKeyPointsItem.setPointsExists7(pointsExists[7]);
            yakFaceKeyPointsItem.setPointFloat7(pointFloat7);
        }
        if ((charsOutExists8 - 0) * quantization > exists) {
            pointFloat8.set((float) (charsOutkeyPoints16 * quantization), (float) (charsOutkeyPoints17 * quantization));
            pointFloats.add(pointFloat8);
            pointsExists[8] = 1;
            yakFaceKeyPointsItem.setPointsExists8(pointsExists[8]);
            yakFaceKeyPointsItem.setPointFloat8(pointFloat8);
            sLogger.i("关键点9:");
            sLogger.i("获取的point8 %d:" + pointFloat8.toString());
        } else {
            pointsExists[8] = 0;
            yakFaceKeyPointsItem.setPointsExists8(pointsExists[8]);
            yakFaceKeyPointsItem.setPointFloat8(pointFloat8);
        }
        if ((charsOutExists9 - 0) * quantization > exists) {
            pointFloat9.set((float) (charsOutkeyPoints18 * quantization), (float) (charsOutkeyPoints19 * quantization));
            pointFloats.add(pointFloat9);
            pointsExists[9] = 1;
            yakFaceKeyPointsItem.setPointsExists9(pointsExists[9]);
            yakFaceKeyPointsItem.setPointFloat9(pointFloat9);
            sLogger.i("关键点10:");
            sLogger.i("获取的point9 %d:" + pointFloat9.toString());
        } else {
            pointsExists[9] = 0;
            yakFaceKeyPointsItem.setPointsExists9(pointsExists[9]);
            yakFaceKeyPointsItem.setPointFloat9(pointFloat9);
        }
        if ((charsOutExists10 - 0) * quantization > exists) {
            pointFloat10.set((float) (charsOutkeyPoints20 * quantization), (float) (charsOutkeyPoints21 * quantization));
            pointFloats.add(pointFloat10);
            pointsExists[10] = 1;
            yakFaceKeyPointsItem.setPointsExists10(pointsExists[10]);
            yakFaceKeyPointsItem.setPointFloat10(pointFloat10);
            sLogger.i("关键点11:");
            sLogger.i("获取的point10 %d:" + pointFloat10.toString());
        } else {
            pointsExists[10] = 0;
            yakFaceKeyPointsItem.setPointsExists10(pointsExists[10]);
            yakFaceKeyPointsItem.setPointFloat10(pointFloat10);
        }
        if ((charsOutExists11 - 0) * quantization > exists) {
            pointFloat11.set((float) (charsOutkeyPoints22 * quantization), (float) (charsOutkeyPoints23 * quantization));
            pointFloats.add(pointFloat11);
            pointsExists[11] = 1;
            yakFaceKeyPointsItem.setPointsExists11(pointsExists[11]);
            yakFaceKeyPointsItem.setPointFloat11(pointFloat11);
            sLogger.i("关键点12:");
            sLogger.i("获取的point11 %d:" + pointFloat11.toString());
        } else {
            pointsExists[11] = 0;
            yakFaceKeyPointsItem.setPointsExists11(pointsExists[11]);
            yakFaceKeyPointsItem.setPointFloat11(pointFloat11);
        }
        if ((charsOutExists12 - 0) * quantization > exists) {
            pointFloat12.set((float) (charsOutkeyPoints24 * quantization), (float) (charsOutkeyPoints25 * quantization));
            pointFloats.add(pointFloat12);
            pointsExists[12] = 1;
            yakFaceKeyPointsItem.setPointsExists12(pointsExists[12]);
            yakFaceKeyPointsItem.setPointFloat12(pointFloat12);
            sLogger.i("关键点13:");
            sLogger.i("获取的point12 %d:" + pointFloat12.toString());
        } else {
            pointsExists[12] = 0;
            yakFaceKeyPointsItem.setPointsExists12(pointsExists[12]);
            yakFaceKeyPointsItem.setPointFloat12(pointFloat12);
        }


        sLogger.i(YakFaceDetectTFlite.srcYakBitmapName + "获取的关键点 %d:" + pointFloats.toString());

        String unsuccessTXTPath = "";
        if (FarmGlobal.model == Model.BUILD.value()) {
            unsuccessTXTPath = FarmGlobal.mediaInsureItem.getUnsuccessInfoTXTFileName();
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            unsuccessTXTPath = FarmGlobal.mediaPayItem.getUnsuccessInfoTXTFileName();
        }

        String contenType = "KeypointResult：";
        contenType += YakFaceDetectTFlite.srcYakBitmapName + "; ";
        contenType += "point0 = " + yakFaceKeyPointsItem.getPointFloat0().toString() + "; ";
        contenType += "point1 = " + yakFaceKeyPointsItem.getPointFloat1().toString() + "; ";
        contenType += "point2 = " + yakFaceKeyPointsItem.getPointFloat2().toString() + "; ";
        contenType += "point3 = " + yakFaceKeyPointsItem.getPointFloat3().toString() + "; ";
        contenType += "point4 = " + yakFaceKeyPointsItem.getPointFloat4().toString() + "; ";
        contenType += "point5 = " + yakFaceKeyPointsItem.getPointFloat5().toString() + "; ";
        contenType += "point6 = " + yakFaceKeyPointsItem.getPointFloat6().toString() + "; ";
        contenType += "point7 = " + yakFaceKeyPointsItem.getPointFloat7().toString() + "; ";
        contenType += "point8 = " + yakFaceKeyPointsItem.getPointFloat8().toString() + "; ";
        contenType += "point9 = " + yakFaceKeyPointsItem.getPointFloat9().toString() + "; ";
        contenType += "point10 = " + yakFaceKeyPointsItem.getPointFloat10().toString() + "; ";
        contenType += "point11 = " + yakFaceKeyPointsItem.getPointFloat11().toString() + "; ";
        contenType += "point12 = " + yakFaceKeyPointsItem.getPointFloat12().toString() + "; ";


        if (FarmGlobal.model == Model.VERIFY.value()){
            if (pointsExists[11] + pointsExists[12] == 2
                    && pointsExists[7] + pointsExists[8] + pointsExists[9] + pointsExists[10] > 0
                    && pointsExists[3] + pointsExists[4] + pointsExists[5] == 0) {

                yakKeypointsDetectedK1 = true;
            } else if ((pointsExists[4] + pointsExists[6] + pointsExists[11] == 3
                    && (pointsExists[0] + pointsExists[1] + pointsExists[2] + pointsExists[3]
                    + pointsExists[7] + pointsExists[8] + pointsExists[9] + pointsExists[10] > 0))
                    || (pointsExists[3] + pointsExists[6] + pointsExists[10] == 3
                    && (pointsExists[0] + pointsExists[1] + pointsExists[2] + pointsExists[4]
                    + pointsExists[7] + pointsExists[8] + pointsExists[9] + pointsExists[11] > 0))) {

                yakKeypointsDetectedK2 = true;
            } else if (pointsExists[10] + pointsExists[11] + pointsExists[12] == 0
                    && pointsExists[4] + pointsExists[5] == 2
                    && pointsExists[0] + pointsExists[1]+ pointsExists[2]+ pointsExists[3] > 0) {

                yakKeypointsDetectedK3 = true;
            }else {
                if (kTimes < 4) {
                    String mPath = null;
                    mPath = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("/key");

                    //保存原图
                    File file = new File(mPath);
                    FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                    //保存失败检测信息
                    FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
                }
                return null;
            }
        }else {

            if (pointsExists[11] + pointsExists[12] == 2
                    && pointsExists[7] + pointsExists[8] + pointsExists[9] + pointsExists[10] > 0
                    && pointsExists[4] + pointsExists[5] == 0) {

                yakKeypointsDetectedK1 = true;
            } else if ((pointsExists[4] + pointsExists[6] + pointsExists[11] == 3
                    && (pointsExists[0] + pointsExists[1] + pointsExists[2] + pointsExists[3] > 0
                    || pointsExists[7] + pointsExists[8] + pointsExists[9] + pointsExists[10] > 0))
                    || (pointsExists[3] + pointsExists[6] + pointsExists[10] == 3
                    && (pointsExists[0] + pointsExists[1] + pointsExists[2] + pointsExists[4] > 0
                    || pointsExists[7] + pointsExists[8] + pointsExists[9] + pointsExists[11] > 0))) {

                yakKeypointsDetectedK2 = true;
            } else if (pointsExists[11] + pointsExists[12] == 0
                    && pointsExists[4] + pointsExists[5] == 2
                    && pointsExists[0] + pointsExists[1]+ pointsExists[2]+ pointsExists[3] > 0) {

                yakKeypointsDetectedK3 = true;
            }else{
                if(kTimes < 4){
                    String mPath = null;
                    mPath = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("/key");
                    //保存原图
                    File file = new File(mPath);
                    FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                    //保存失败检测信息
                    FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
                }
                return null;
            }
        }

        return pointFloats;
    }

}
