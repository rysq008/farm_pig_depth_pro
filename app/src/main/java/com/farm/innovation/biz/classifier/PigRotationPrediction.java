package com.farm.innovation.biz.classifier;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.os.Trace;

import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.biz.iterm.PredictRotationIterm;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.PointFloat;
import com.farm.innovation.utils.Rot2AngleType;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static com.farm.innovation.biz.classifier.PigFaceDetectTFlite.srcPigBitmapName;
import static com.farm.innovation.utils.ImageUtils.compressBitmap;
import static com.farm.innovation.utils.ImageUtils.padBitmap;
import static org.tensorflow.demo.FarmDetectorActivity.aTimes;

/**
 * @author luolu .2018/8/4
 */
public class PigRotationPrediction implements FarmClassifier {
    private static final Logger sLogger = new Logger(PigRotationPrediction.class);
    private static final boolean DEBUG = false;
    private static final float MIN_CONFIDENCE = (float) 0.8;
    private static final String TAG = "FaceDetector";

    static {
        System.loadLibrary("tensorflow_demo");
    }

    // Only return this many results.
    private static final int NUM_DETECTIONS = 1;
    private boolean isModelQuantized;
    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;
    // Number of threads in the java app
    private static final int NUM_THREADS = 4;
    // Config values.
    private int inputSize;
    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private int[] intValues;
    private byte[][] detectRotation;

    private ByteBuffer imgData;

    private Interpreter tfLite;
    public static int pigPredictAngleType;


    /** Memory-map the model file in Assets. */
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
     *  @param assetManager The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param inputSize The size of image input
     * @param isQuantized Boolean representing model is quantized or not
     */
    public static FarmClassifier create(
            final AssetManager assetManager,
            final String modelFilename,
            final String labelFilename,
            final int inputSize,
            final boolean isQuantized)
            throws IOException {
        final PigRotationPrediction d = new PigRotationPrediction();

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
        d.detectRotation = new byte[1][3];
        return d;
    }

    private PigRotationPrediction() {}

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
    public PredictRotationIterm pigRotationPredictionItemTFlite(Bitmap bitmap, Bitmap oriBitmap) {
        PredictRotationIterm predictRotationIterm = null;
        if (bitmap == null) {
            return null;
        }
        pigPredictAngleType = 10;
        sLogger.i("bitmap height:" + bitmap.getHeight());
        sLogger.i("bitmap width:" + bitmap.getWidth());
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


        float[] inputValues = new float[inputSize * inputSize * 3];
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

        detectRotation = new byte[1][3];

        sLogger.i("inputSize:" + inputSize);

        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, detectRotation);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        final long startTime = SystemClock.uptimeMillis();
        tfLite.run(imgData, detectRotation);
        sLogger.i("RotationPredict pigtflite cost:" + (SystemClock.uptimeMillis() - startTime));
        int quantization =  128;
        double quantizationScale =  0.0175615;
        float predictRotX;
        float predictRotY;
        float predictRotZ;
        float rotScale = (float) 57.6;
        char charsOutRotation0 = convertByte2Uint8(detectRotation[0][0]);
        char charsOutRotation1 = convertByte2Uint8(detectRotation[0][1]);
        char charsOutRotation2 = convertByte2Uint8(detectRotation[0][2]);

        predictRotX = (float)((charsOutRotation0 - quantization) * quantizationScale);
        predictRotY = (float)((charsOutRotation1 - quantization) * quantizationScale);
        predictRotZ = (float)((charsOutRotation2 - quantization) * quantizationScale);
//        sLogger.i("predictRotX %f:" + predictRotX );
//        sLogger.i("predictRotY %f:" + predictRotY );
//        sLogger.i("predictRotZ %f:" + predictRotZ );

        predictRotationIterm = new PredictRotationIterm(
                predictRotX,
                predictRotY,
                predictRotZ);

        // TODO: 2018/11/1 By:LuoLu
        pigPredictAngleType = Rot2AngleType.getPigAngleType(predictRotX,
                predictRotY);
        if (pigPredictAngleType == 1){
            // draw rotation
//            Canvas canvasDrawRecognition = new Canvas(padBitmap);
//            com.farm.innovation.utils.ImageUtils.drawText(canvasDrawRecognition,
//                    "角度:"+pigPredictAngleType, 10,30,
//                    "X:"+String.valueOf(predictRotX * rotScale),10,60,
//                    "Y:"+String.valueOf(predictRotY * rotScale),10,90);
//            com.farm.innovation.utils.ImageUtils.saveBitmap(padBitmap,"pigRotP1",srcPigBitmapName);
        }else if (pigPredictAngleType == 2){
            // draw rotation
//            Canvas canvasDrawRecognition = new Canvas(padBitmap);
//            com.farm.innovation.utils.ImageUtils.drawText(canvasDrawRecognition,
//                    "角度:"+pigPredictAngleType, 10,30,
//                    "X:"+String.valueOf(predictRotX * rotScale),10,60,
//                    "Y:"+String.valueOf(predictRotY * rotScale),10,90);
//            com.farm.innovation.utils.ImageUtils.saveBitmap(padBitmap,"pigRotP2",srcPigBitmapName);
        }else if (pigPredictAngleType == 3){
            // draw rotation
//            Canvas canvasDrawRecognition = new Canvas(padBitmap);
//            com.farm.innovation.utils.ImageUtils.drawText(canvasDrawRecognition,
//                    "角度:"+pigPredictAngleType,10,30,
//                    "X:"+String.valueOf(predictRotX * rotScale),10,60,
//                    "Y:"+String.valueOf(predictRotY * rotScale),10,90);
//            com.farm.innovation.utils.ImageUtils.saveBitmap(padBitmap,"pigRotP3",srcPigBitmapName);
        }else {
            // draw rotation
//            Canvas canvasDrawRecognition = new Canvas(padBitmap);
//            com.farm.innovation.utils.ImageUtils.drawText(canvasDrawRecognition,
//                    "角度:"+pigPredictAngleType,10,30,
//                    "X:"+String.valueOf(predictRotX * rotScale),10,60,
//                    "Y:"+String.valueOf(predictRotY * rotScale),10,90);
//            com.farm.innovation.utils.ImageUtils.saveBitmap(padBitmap,"pigRotP10",srcPigBitmapName);
            //角度不是左中右的保存信息
            String unsuccessTXTPath = "";
            if (FarmGlobal.model == Model.BUILD.value()) {
                unsuccessTXTPath = FarmGlobal.mediaInsureItem.getUnsuccessInfoTXTFileName();
            } else if (FarmGlobal.model == Model.VERIFY.value()) {
                unsuccessTXTPath = FarmGlobal.mediaPayItem.getUnsuccessInfoTXTFileName();
            }
//            FileUtils.saveInfoToTxtFile(oriInfoPath, srcYakBitmapName+
//                    "；totalNum：" + allNumber +"；DetectTime："+dTime+"；AngleTime："+(System.currentTimeMillis() - aTime)+"；KeypointTime："+kTime +
//                    "；totalTime：" + (dTime+(System.currentTimeMillis() - aTime)+kTime));

            String contenType = "AngleResult：";
            contenType += srcPigBitmapName + "; ";
            contenType += "rot_x = " + predictRotX + "; ";
            contenType += "rot_y = " + predictRotY + "; ";
            contenType += "rot_z = " + predictRotZ + "; ";

            if(aTimes < 4){
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
            return null;
        }


        return predictRotationIterm;
    }

    @Override
    public RecognitionAndPostureItem yakRecognitionAndPostureItemTFlite(Bitmap bitmap, Bitmap originalBitmap) {
        return null;
    }

    @Override
    public PredictRotationIterm yakRotationPredictionItemTFlite(Bitmap bitmap,Bitmap originalBitmap) {
        return null;
    }
}
