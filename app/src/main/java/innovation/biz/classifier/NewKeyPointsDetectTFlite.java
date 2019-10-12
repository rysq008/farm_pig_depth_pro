
package innovation.biz.classifier;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import org.tensorflow.demo.Classifier;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.biz.iterm.NewPigKeyPointAndRotationItem;
import innovation.biz.iterm.PigFaceKeyPointsItem;
import innovation.biz.iterm.PredictRotationIterm;
import innovation.utils.FileUtils;
import innovation.utils.PointFloat;
import innovation.utils.Rot2AngleType;

import static innovation.biz.classifier.NewFaceDetectTFlite.srcPigBitmapName;
import static innovation.utils.ImageUtils.compressBitmap;
import static innovation.utils.ImageUtils.padBitmap;
import static org.tensorflow.demo.DetectorActivity_pig.aTimes;
import static org.tensorflow.demo.DetectorActivity_pig.kTimes;


/**
 * Author by luolu, Date on 2018/10/9.
 * COMPANY：InnovationAI
 */


public class NewKeyPointsDetectTFlite implements Classifier {
    private static final Logger S_LOGGER = new Logger(NewKeyPointsDetectTFlite.class);
    private static final boolean DEBUG = false;
    private static final float MIN_CONFIDENCE = (float) 0.6;
    private static final String TAG = "PigKeyPointsDetectTFlite";

    // Only return this many results.
    private static final int NUM_DETECTIONS = 10;
    private boolean isModelQuantized;
    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;
    // WaitNumber of threads in the java app
    private static final int NUM_THREADS = 4;
    // Config values.
    private int inputSize;
    private int[] intValues;
    private byte[][] keyPoints;
    private byte[][] exists;

    private byte[][][][] heatmap;

    private ByteBuffer imgData;
    private Interpreter tfLite;
    public static boolean pigKeypointsK1;
    public static boolean pigKeypointsK2;
    public static boolean pigKeypointsK3;

    public static int pigPredictAngleTypeR;

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
        final NewKeyPointsDetectTFlite d = new NewKeyPointsDetectTFlite();

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

    private NewKeyPointsDetectTFlite() {
    }

    @Override
    public RecognitionAndPostureItem pigRecognitionAndPostureItem(Bitmap bitmap, Bitmap oriBitmap) {
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
    public NewPigKeyPointAndRotationItem pigRecognizePointImage(Bitmap bitmap, Bitmap oriBitmap) {
        if (bitmap == null) {
            return null;
        }
        pigKeypointsK1 = false;
        pigKeypointsK2 = false;
        pigKeypointsK3 = false;
//        S_LOGGER.i("bitmap height:" + bitmap.getHeight());
//        S_LOGGER.i("bitmap width:" + bitmap.getWidth());
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

//        S_LOGGER.i("croppedBitmap height:" + croppedBitmap.getHeight());
//        S_LOGGER.i("croppedBitmap width:" + croppedBitmap.getWidth());

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

        //定义关键点模型变量
        keyPoints = new byte[1][22];
        exists = new byte[1][11];

        float[][] keyPosition = new float[1][22];

        heatmap = new byte[1][48][48][11];
        byte[][] detectRotation = new byte[1][1];


        S_LOGGER.i("inputSize:" + inputSize);

        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, heatmap);
        outputMap.put(1, detectRotation);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        final long startTime = SystemClock.uptimeMillis();
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
        S_LOGGER.i("PigKeyPointsDetectTFlite cost:" + (SystemClock.uptimeMillis() - startTime));

        NewPigKeyPointAndRotationItem newPigKeyPointAndRotationItem = new NewPigKeyPointAndRotationItem();

        //关键点相关
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

        for (int k = 0; k < 11; k++) {
            float score = 0.0f;
            byte bScore;
            for (int i = 0; i < 48; i++) {
                for (int j = 0; j < 48; j++) {
                    if (((float) convertByte2Uint8(heatmap[0][i][j][k]) - 0) > score) {
                        score = (float) convertByte2Uint8(heatmap[0][i][j][k]) - 0;
                        bScore = heatmap[0][i][j][k];
                        exists[0][k] = bScore;
                        keyPosition[0][2 * k] = (float) i / 48;
                        keyPosition[0][2 * k + 1] = (float) j / 48;
                    }
                }
            }
        }

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

        double quantization = 0.407317;
        double exists = 0.5;
        int[] pointsExists = new int[11];
        PigFaceKeyPointsItem pigFaceKeyPointsItem = PigFaceKeyPointsItem.getInstance();

        if (((charsOutExists0 - 0) * quantization) / 100 > exists) {
            point0.set((float) (keyPosition[0][0] * quantization), (float) (keyPosition[0][1] * quantization));
            points.add(point0);
            pointsExists[0] = 1;
            pigFaceKeyPointsItem.setPointsExists0(pointsExists[0]);
            pigFaceKeyPointsItem.setPointFloat0(point0);
            S_LOGGER.i("关键点1:");
            S_LOGGER.i("获取的point1 %d:" + point0.toString());
        } else {
            pointsExists[0] = 0;
            pigFaceKeyPointsItem.setPointsExists0(pointsExists[0]);
            pigFaceKeyPointsItem.setPointFloat0(point0);
        }
        if (((charsOutExists1 - 0) * quantization) / 100 > exists) {
            point1.set((float) (keyPosition[0][2] * quantization), (float) (keyPosition[0][3] * quantization));
            points.add(point1);
            pointsExists[1] = 1;
            pigFaceKeyPointsItem.setPointsExists1(pointsExists[1]);
            pigFaceKeyPointsItem.setPointFloat1(point1);
            S_LOGGER.i("关键点2:");
            S_LOGGER.i("获取的point2 %d:" + point1.toString());
        } else {
            pointsExists[1] = 0;
            pigFaceKeyPointsItem.setPointsExists1(pointsExists[1]);
            pigFaceKeyPointsItem.setPointFloat1(point1);
        }
        if (((charsOutExists2 - 0) * quantization) / 100 > exists) {
            point2.set((float) (keyPosition[0][4] * quantization), (float) (keyPosition[0][5] * quantization));
            points.add(point2);
            pointsExists[2] = 1;
            pigFaceKeyPointsItem.setPointsExists2(pointsExists[2]);
            pigFaceKeyPointsItem.setPointFloat2(point2);
            S_LOGGER.i("关键点3:");
            S_LOGGER.i("获取的point3 %d:" + point2.toString());
        } else {
            pointsExists[2] = 0;
            pigFaceKeyPointsItem.setPointsExists2(pointsExists[2]);
            pigFaceKeyPointsItem.setPointFloat2(point2);
        }
        if (((charsOutExists3 - 0) * quantization) / 100 > exists) {
            point3.set((float) (keyPosition[0][6] * quantization), (float) (keyPosition[0][7] * quantization));
            points.add(point3);
            pointsExists[3] = 1;
            pigFaceKeyPointsItem.setPointsExists3(pointsExists[3]);
            pigFaceKeyPointsItem.setPointFloat3(point3);
            S_LOGGER.i("关键点4:");
            S_LOGGER.i("获取的point4 %d:" + point3.toString());
        } else {
            pointsExists[3] = 0;
            pigFaceKeyPointsItem.setPointsExists3(pointsExists[3]);
            pigFaceKeyPointsItem.setPointFloat3(point3);
        }
        if (((charsOutExists4 - 0) * quantization) / 100 > exists) {
            point4.set((float) (keyPosition[0][8] * quantization), (float) (keyPosition[0][9] * quantization));
            points.add(point4);
            pointsExists[4] = 1;
            pigFaceKeyPointsItem.setPointsExists4(pointsExists[4]);
            pigFaceKeyPointsItem.setPointFloat4(point4);
            S_LOGGER.i("关键点5:");
            S_LOGGER.i("获取的point5 %d:" + point4.toString());
        } else {
            pointsExists[4] = 0;
            pigFaceKeyPointsItem.setPointsExists4(pointsExists[4]);
            pigFaceKeyPointsItem.setPointFloat4(point4);
        }
        if (((charsOutExists5 - 0) * quantization) / 100 > exists) {
            point5.set((float) (keyPosition[0][10] * quantization), (float) (keyPosition[0][11] * quantization));
            points.add(point5);
            pointsExists[5] = 1;
            pigFaceKeyPointsItem.setPointsExists5(pointsExists[5]);
            pigFaceKeyPointsItem.setPointFloat5(point5);
            S_LOGGER.i("关键点6:");
            S_LOGGER.i("获取的point6 %d:" + point5.toString());
        } else {
            pointsExists[5] = 0;
            pigFaceKeyPointsItem.setPointsExists5(pointsExists[5]);
            pigFaceKeyPointsItem.setPointFloat5(point5);
        }
        if (((charsOutExists6 - 0) * quantization) / 100 > exists) {
            point6.set((float) (keyPosition[0][12] * quantization), (float) (keyPosition[0][13] * quantization));
            points.add(point6);
            pointsExists[6] = 1;
            pigFaceKeyPointsItem.setPointsExists6(pointsExists[6]);
            pigFaceKeyPointsItem.setPointFloat6(point6);
            S_LOGGER.i("关键点7:");
            S_LOGGER.i("获取的point7 %d:" + point6.toString());
        } else {
            pointsExists[6] = 0;
            pigFaceKeyPointsItem.setPointsExists6(pointsExists[6]);
            pigFaceKeyPointsItem.setPointFloat6(point6);
        }
        if (((charsOutExists7 - 0) * quantization) / 100 > exists) {
            point7.set((float) (keyPosition[0][14] * quantization), (float) (keyPosition[0][15] * quantization));
            points.add(point7);
            pointsExists[7] = 1;
            pigFaceKeyPointsItem.setPointsExists7(pointsExists[7]);
            pigFaceKeyPointsItem.setPointFloat7(point7);
            S_LOGGER.i("关键点8:");
            S_LOGGER.i("获取的point8 %d:" + point7.toString());
        } else {
            pointsExists[7] = 0;
            pigFaceKeyPointsItem.setPointsExists7(pointsExists[7]);
            pigFaceKeyPointsItem.setPointFloat7(point7);
        }
        if (((charsOutExists8 - 0) * quantization) / 100 > exists) {
            point8.set((float) (keyPosition[0][16] * quantization), (float) (keyPosition[0][17] * quantization));
            points.add(point8);
            pointsExists[8] = 1;
            pigFaceKeyPointsItem.setPointsExists8(pointsExists[8]);
            pigFaceKeyPointsItem.setPointFloat8(point8);
            S_LOGGER.i("关键点9:");
            S_LOGGER.i("获取的point9 %d:" + point8.toString());
        } else {
            pointsExists[8] = 0;
            pigFaceKeyPointsItem.setPointsExists8(pointsExists[8]);
            pigFaceKeyPointsItem.setPointFloat8(point8);
        }
        if (((charsOutExists9 - 0) * quantization) / 100 > exists) {
            point9.set((float) (keyPosition[0][18] * quantization), (float) (keyPosition[0][19] * quantization));
            points.add(point9);
            pointsExists[9] = 1;
            pigFaceKeyPointsItem.setPointsExists9(pointsExists[9]);
            pigFaceKeyPointsItem.setPointFloat9(point9);
            S_LOGGER.i("关键点10:");
            S_LOGGER.i("获取的point10 %d:" + point9.toString());
        } else {
            pointsExists[9] = 0;
            pigFaceKeyPointsItem.setPointsExists9(pointsExists[9]);
            pigFaceKeyPointsItem.setPointFloat9(point9);
        }
        if (((charsOutExists10 - 0) * quantization) / 100 > exists) {
            point10.set((float) (keyPosition[0][20] * quantization), (float) (keyPosition[0][21] * quantization));
            points.add(point10);
            pointsExists[10] = 1;
            pigFaceKeyPointsItem.setPointsExists10(pointsExists[10]);
            pigFaceKeyPointsItem.setPointFloat10(point10);
            S_LOGGER.i("关键点11:");
            S_LOGGER.i("获取的point11 %d:" + point10.toString());
        } else {
            pointsExists[10] = 0;
            pigFaceKeyPointsItem.setPointsExists10(pointsExists[10]);
            pigFaceKeyPointsItem.setPointFloat10(point10);
        }
        S_LOGGER.i("获取的关键点 %d:" + points.toString());

        String unsuccessTXTPath = "";
        unsuccessTXTPath = Global.mediaPayItem.getUnsuccessInfoTXTFileName();

        String contenType = "KeypointResult：";
        contenType += srcPigBitmapName + "; ";
        contenType += "point0 = " + pigFaceKeyPointsItem.getPointFloat0().toString() + "; ";
        contenType += "point1 = " + pigFaceKeyPointsItem.getPointFloat1().toString() + "; ";
        contenType += "point2 = " + pigFaceKeyPointsItem.getPointFloat2().toString() + "; ";
        contenType += "point3 = " + pigFaceKeyPointsItem.getPointFloat3().toString() + "; ";
        contenType += "point4 = " + pigFaceKeyPointsItem.getPointFloat4().toString() + "; ";
        contenType += "point5 = " + pigFaceKeyPointsItem.getPointFloat5().toString() + "; ";
        contenType += "point6 = " + pigFaceKeyPointsItem.getPointFloat6().toString() + "; ";
        contenType += "point7 = " + pigFaceKeyPointsItem.getPointFloat7().toString() + "; ";
        contenType += "point8 = " + pigFaceKeyPointsItem.getPointFloat8().toString() + "; ";
        contenType += "point9 = " + pigFaceKeyPointsItem.getPointFloat9().toString() + "; ";
        contenType += "point10 = " + pigFaceKeyPointsItem.getPointFloat10().toString() + "; ";


//        Canvas canvasDrawPoints = new Canvas(padBitmap);
        if (pointsExists[4] == 0 && pointsExists[6] + pointsExists[7] == 2 &&
                pointsExists[8] + pointsExists[9] + pointsExists[10] >= 1) {
//            com.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point5,padSize,"5");
//            com.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point9,padSize,"9");
//            com.innovation.utils.ImageUtils.saveBitmap(padBitmap,"pigKeyP1",srcPigBitmapName);
            pigKeypointsK1 = true;
        } else if (pointsExists[0] + pointsExists[4] + pointsExists[7] == 3 &&
                pointsExists[1] + pointsExists[2] + pointsExists[3] >= 1 &&
                pointsExists[8] + pointsExists[9] + pointsExists[10] >= 1) {
//            com.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point3,padSize,"3");
//            com.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point5,padSize,"5");
//            com.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point9,padSize,"9");
//            com.innovation.utils.ImageUtils.saveBitmap(padBitmap,"pigKeyP2",srcPigBitmapName);
            pigKeypointsK2 = true;
        } else if (pointsExists[4] + pointsExists[5] == 2 &&
                pointsExists[7] == 0 &&
                pointsExists[1] + pointsExists[2] + pointsExists[3] >= 1) {
//            com.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point3,padSize,"3");
//            com.innovation.utils.ImageUtils.drawKeypoints(canvasDrawPoints,point5,padSize,"5");
//            com.innovation.utils.ImageUtils.saveBitmap(padBitmap,"pigKeyP3",srcPigBitmapName);
            pigKeypointsK3 = true;
        } else {
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
//                    canvas1.drawCircle(point.getX() * padSize, point.getY() * padSize, 3, boxPaint);
//                }
//
//            }
//            com.innovation.utils.ImageUtils.saveBitmap(padBitmap,"pigKeyP10",srcPigBitmapName);
            if (kTimes < 4) {
                String mPath = null;
                mPath = Global.mediaPayItem.getOriInfoBitmapFileName("/key");
                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPath, contenType);
            }
            points = null;
        }
        //角度相关
        char charsOutRotation0 = convertByte2Uint8(detectRotation[0][0]);
        int quantizationR =  127;
        float predictRotY;
        double quantizationScale =  0.0122552;
        predictRotY = (float)((charsOutRotation0 - quantizationR) * quantizationScale);
        PredictRotationIterm predictRotationIterm = new PredictRotationIterm(predictRotY);


        pigPredictAngleTypeR = Rot2AngleType.getPigAngleType(predictRotY);
        if (pigPredictAngleTypeR != 1 && pigPredictAngleTypeR != 2 && pigPredictAngleTypeR != 3){
            //角度不是左中右的保存信息
            String unsuccessTXTPathR = "";
            unsuccessTXTPathR = Global.mediaPayItem.getUnsuccessInfoTXTFileName();

            String contenTypeR = "AngleResult：";
            contenTypeR += srcPigBitmapName + "; ";
            contenTypeR += "rot_y = " + predictRotY + "; ";

            if(aTimes < 4){
                String mPath = null;
                mPath = Global.mediaPayItem.getOriInfoBitmapFileName("/rota");
                //保存原图
                File file = new File(mPath);
                FileUtils.saveBitmapToFile(compressBitmap(oriBitmap), file);
                //保存失败检测信息
                FileUtils.saveInfoToTxtFile(unsuccessTXTPathR, contenTypeR);
            }
            predictRotationIterm = null;
        }

        newPigKeyPointAndRotationItem.setPoints(points);
        newPigKeyPointAndRotationItem.setPredictRotationIterm(predictRotationIterm);

        return newPigKeyPointAndRotationItem;
    }
}
