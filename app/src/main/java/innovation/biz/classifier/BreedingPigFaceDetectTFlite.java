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

import com.xiangchuang.risks.model.bean.shanTiaoBean;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import innovation.biz.iterm.PostureItem;
import innovation.biz.iterm.PredictRotationIterm;

import static com.xiangchuangtec.luolu.animalcounter.MyApplication.currentPadSize;
import static com.xiangchuangtec.luolu.animalcounter.MyApplication.lastXmin;
import static com.xiangchuangtec.luolu.animalcounter.MyApplication.sowCount;
import static innovation.utils.ImageUtils.padBitmap2SpRatio;
import static innovation.utils.ImageUtils.zoomImage;
import static org.tensorflow.demo.DetectorActivity_new.offsetX;
import static org.tensorflow.demo.DetectorActivity_new.offsetY;

/**
 *
 */
public class BreedingPigFaceDetectTFlite {
    private static final Logger sLogger = new Logger(BreedingPigFaceDetectTFlite.class);

    // 2018/12/18 hedazhi edit start
    //private static final float MIN_CONFIDENCE = (float) 0.7;

    // 检测模型分数阈值：0.5
    private static final float MIN_CONFIDENCE = 0.5f;

    // 2018/12/18 hedazhi edit end
    private static final String TAG = "PigDetectTFlite";
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
    //记录前一帧的排序后矩形集合
    private ArrayList<BreedingPigFaceDetectTFlite.Recognition> lastRecognitions = new ArrayList<>();

    int padSize;

    //记录闪跳部分的集合
    private List<shanTiaoBean> shanTiaoBeanList = new ArrayList<>();
    //x轴偏移量
    float centerXOffset = 0;
    //x轴最大位置 超出此位置不记录数量
    float maxBase = 0;

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
        padSize = height;
        currentPadSize = padSize;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        srcPigBitmapName = sdf.format(new Date(System.currentTimeMillis())) + ".jpeg";
//        saveBitMap(bitmap, "pigSrcImage", srcPigBitmapName);
        sLogger.i("padBitmap padSize %d:" + padSize);
        maxBase = 0.7f * padSize - offsetY;

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

        for (int i = 0; i < outputScores[0].length; ++i) {
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

            Log.e(TAG, "outputLocations: Xmin=" + modelX0 + ";Ymin="
                    + modelY0 + ";Xmax=" + modelX1 + ";Ymax=" + modelY1);

            //判断当前xmin是否大于 基线0.15
            if ((modelY0 + modelY1) / 2 < 0.15) {
                continue;
            }

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

            PostureItem posture = new PostureItem(0, 0, 0,
                    modelX0, modelY0, modelX1, modelY1, outputScores[0][i],
                    modelY0 * padSize, modelX0 * padSize,
                    modelY1 * padSize, modelX1 * padSize, resizeClipBitmap, bitmap);

            postureItemList.add(posture);
        }

        Trace.endSection(); // "recognizeImage"

        Collections.sort(recognitions, new Comparator<Recognition>() {
            @Override
            public int compare(Recognition o1, Recognition o2) {
                return Float.compare((o1.location.left + o1.location.right) / 2, (o2.location.left + o2.location.right) / 2);
            }
        });

        //获取的画框集合size大于0 时
        if (recognitions.size() > 0) {
//            float cIou = 0;
//            Log.e(TAG, "lastRecognitions.size: " + lastRecognitions.size());
//            Log.d("数组 赋值前", "lrecognitions: " + lastRecognitions.toString());
//
//            if (lastRecognitions.size() > 0) {
//                //计算重合面积百分比
//                cIou = calculateIou(recognitions, lastRecognitions);
//            }
//            Log.e("cIou", "cIou== " + cIou);
//
//            //获取去当前识别中心点
//            float center = (recognitions.get(0).getLocation().left + recognitions.get(0).getLocation().right) / 2;
//            Log.e("center", "center: " + center);
//            //如果上次的xmin 大于当前的xmin 则判断是有新的对象进入 计数器++  lastxmin从新赋值
//            if (lastXmin > center && cIou < 0.53f) {
//                sowCount++;
//            }

            if (lastRecognitions.size() > 0) {
                //进入计数计算
                calculateListIou(recognitions, lastRecognitions);
            }

            //如果计数器当前值为0，取当前集合的size赋值给计数器
            if (sowCount == 0) {
                sowCount = recognitions.size();
            }

            lastRecognitions.clear();
            lastRecognitions.addAll(recognitions);
//            lastXmin = center;
        }

        Log.d("数组", "crecognitions: " + recognitions.toString());
        Log.d("数组 赋值以后", "lrecognitions: " + lastRecognitions.toString());

        Log.e("sowCount", "sowCount" + sowCount);
        recognitionAndPostureItem.setList(recognitions);

        recognitionAndPostureItem.setPostureItem(postureItemList);

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


    public static class Recognition {
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

        /**
         * Optional location within the source image for the location of the recognized object.
         */
        private RectF location;
        private List<Point> points;


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

    //计算重合区域面积与总面积的百分比
    private float calculateIou(ArrayList<Recognition> currents, ArrayList<Recognition> lasts) {
        float iou = 0;

        Log.e(TAG, "calculateIou: +currents.left" + currents.get(0).getLocation().left);
        Log.e(TAG, "calculateIou: +lasts.left" + lasts.get(0).getLocation().left);

        Log.e(TAG, "calculateIou: +currents.right" + currents.get(0).getLocation().right);
        Log.e(TAG, "calculateIou: +lasts.right" + lasts.get(0).getLocation().right);

        Log.e(TAG, "calculateIou: +currents.top" + currents.get(0).getLocation().top);
        Log.e(TAG, "calculateIou: +lasts.top" + lasts.get(0).getLocation().top);

        Log.e(TAG, "calculateIou: +currents.bottom" + currents.get(0).getLocation().bottom);
        Log.e(TAG, "calculateIou: +lasts.bottom" + lasts.get(0).getLocation().bottom);


        float leftx1 = Math.max(currents.get(0).getLocation().left, lasts.get(0).getLocation().left);
        float lefty1 = Math.max(currents.get(0).getLocation().top, lasts.get(0).getLocation().top);

        float rightx1 = Math.min(currents.get(0).getLocation().right, lasts.get(0).getLocation().right);
        float righty1 = Math.min(currents.get(0).getLocation().bottom, lasts.get(0).getLocation().bottom);

        float w = rightx1 - leftx1;
        float h = righty1 - lefty1;

        //重合部分面积
        float wh = w * h;

        iou = wh /
                ((currents.get(0).getLocation().right - currents.get(0).getLocation().left) * (currents.get(0).getLocation().bottom - currents.get(0).getLocation().top) +
                        (lasts.get(0).getLocation().right - lasts.get(0).getLocation().left) * (lasts.get(0).getLocation().bottom - lasts.get(0).getLocation().left) - wh);
        return iou;
    }

    //计算重合区域面积与总面积的百分比
    private void calculateListIou(ArrayList<Recognition> currents, ArrayList<Recognition> lasts) {
        //循环判断前一帧中是否有单独的对象框
        for (int i = 0; i < lasts.size(); i++) {
            //标记是否是单独对象
            boolean isDan = true;
            for (int j = 0; j < currents.size(); j++) {
                //获取两帧中的xMin最大值
                float leftx1 = Math.max(lasts.get(i).getLocation().left, currents.get(j).getLocation().left);
                //获取两帧中的yMin最大值
                float lefty1 = Math.max(lasts.get(i).getLocation().top, currents.get(j).getLocation().top);
                //获取两帧中的xMax最大值
                float rightx1 = Math.min(lasts.get(i).getLocation().right, currents.get(j).getLocation().right);
                //获取两帧中的yMax最大值
                float righty1 = Math.min(lasts.get(i).getLocation().bottom, currents.get(j).getLocation().bottom);
                //计算宽、高
                float w = rightx1 - leftx1;
                float h = righty1 - lefty1;
                //计算重合部分面积
                float wh = w * h;
                float iou = wh /
                        ((lasts.get(i).getLocation().right - lasts.get(i).getLocation().left)
                                * (lasts.get(i).getLocation().bottom - lasts.get(i).getLocation().top) +
                                (currents.get(j).getLocation().right - currents.get(j).getLocation().left)
                                        * (currents.get(j).getLocation().bottom - currents.get(j).getLocation().left) - wh);
                //判断重合部分面积是否大于指定阈值， 大于则证明是已存在对象并计算出水平位移， 否则单独是对象
                if (iou > 0.53f) {
                    isDan = false;
                    centerXOffset = ((lasts.get(i).getLocation().right + lasts.get(i).getLocation().left) / 2) -
                            ((currents.get(j).getLocation().right + currents.get(j).getLocation().left) / 2);
                    break;
                } else {
                    isDan = true;
                }
            }
            if (isDan) {
                float c = ((lasts.get(i).getLocation().right + lasts.get(i).getLocation().left) / 2);
                //判断闪跳对象是不是在基线范围内
                if (c < maxBase) {
                    //将闪跳对象添加到集合保存
                    shanTiaoBeanList.add(new shanTiaoBean(c, c));
                }
            }
        }
        //更新偏移集合
        if (shanTiaoBeanList.size() > 0) {
            for (int k = 0; k < shanTiaoBeanList.size(); k++) {
                float xMi = shanTiaoBeanList.get(k).getxMin();
                float xMa = shanTiaoBeanList.get(k).getxMax();
                shanTiaoBeanList.get(k).setxMin(xMi + 0.5f * centerXOffset);
                shanTiaoBeanList.get(k).setxMax(xMa + 1.5f * centerXOffset);
                float tempCenter = (shanTiaoBeanList.get(k).getxMax() + shanTiaoBeanList.get(k).getxMin()) / 2;
                //判断闪跳集合中的对象中心点是否超出范围 超出则删除当前对象
                if (tempCenter > maxBase) {
                    shanTiaoBeanList.remove(k);
                }
            }
        }
        //循环判断后一帧中是否有单独对象
        for (int i = 0; i < currents.size(); i++) {
            boolean isDan = false;
            for (int j = 0; j < lasts.size(); j++) {
                float leftx1 = Math.max(lasts.get(j).getLocation().left, currents.get(i).getLocation().left);
                float lefty1 = Math.max(lasts.get(j).getLocation().top, currents.get(i).getLocation().top);
                float rightx1 = Math.min(lasts.get(j).getLocation().right, currents.get(i).getLocation().right);
                float righty1 = Math.min(lasts.get(j).getLocation().bottom, currents.get(i).getLocation().bottom);
                //计算宽、高
                float w = rightx1 - leftx1;
                float h = righty1 - lefty1;
                //计算重合部分面积
                float wh = w * h;
                float iou = wh /
                        ((lasts.get(j).getLocation().right - lasts.get(j).getLocation().left)
                                * (lasts.get(j).getLocation().bottom - lasts.get(j).getLocation().top) +
                                (currents.get(i).getLocation().right - currents.get(i).getLocation().left)
                                        * (currents.get(i).getLocation().bottom - currents.get(i).getLocation().left) - wh);
                if (iou > 0.53f) {
                    isDan = false;
                    break;
                } else {
                    isDan = true;
                }
            }
            if (isDan) {
                float c = (currents.get(i).getLocation().right + currents.get(i).getLocation().left) / 2;
                float lastC = (lasts.get(0).getLocation().right + lasts.get(0).getLocation().left) / 2;
                if (shanTiaoBeanList.size() > 0) {
                    boolean isInXOffset = false;
                    for (int k = 0; k < shanTiaoBeanList.size(); k++) {
                        float xMi = shanTiaoBeanList.get(k).getxMin();
                        float xMa = shanTiaoBeanList.get(k).getxMax();
                        float tempCenter = (xMi + xMa) / 2;
                        //判断闪跳集合中的对象中心点是否超出范围 超出则删除当前对象
                        if (tempCenter > maxBase) {
                            isInXOffset = true;
                            shanTiaoBeanList.remove(k);
                        } else {
                            //判断单独对象是否在震荡范围内， 如果在不增加数量 否则增加数量
                            if (c > shanTiaoBeanList.get(k).getxMin() && c < shanTiaoBeanList.get(k).getxMax()) {
                                isInXOffset = true;
                                shanTiaoBeanList.remove(k);
                                break;
                            }
                        }
                    }
                    if (!isInXOffset && c < lastC) {
                        sowCount++;
                    }
                }
            }
        }
    }
}
