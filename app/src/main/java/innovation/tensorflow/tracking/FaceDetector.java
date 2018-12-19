package innovation.tensorflow.tracking;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;


import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.PostureItem;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.utils.FileUtils;
import innovation.utils.ScreenUtil;

import static innovation.utils.ImageUtils.calculateScreenPosition;


/**
 * Author by luolu, Date on 2018/8/23.
 * COMPANY：InnovationAI
 */

/*public class FaceDetector implements Classifier {
    private static final Logger sLogger = new Logger(FaceDetector.class);
    private static final boolean DEBUG = false;
    private static final float MIN_CONFIDENCE = (float) 0.6;
    private static final String TAG = "FaceDetector";

    static {
        System.loadLibrary("tensorflow_demo");
    }

    private static final int MAX_RESULTS = Integer.MAX_VALUE;

    private String inputName;
    private int inputSize;
    private int imageMean;
    private float imageStd;

    private int[] intValues;
    private float[] floatValues;
    private float[] outputLocations;
    private float[] outputScores;
    private String[] outputNames;
    private int numLocations;

    private TensorFlowInferenceInterface inferenceInterface;

    private float[] boxPriors;

    public static FaceDetector create(final AssetManager assetManager, final String modelFilename, final int numLocations, final int inputSize, final int imageMean, final float imageStd, final String inputName, final String outputName) {
        FaceDetector d = new FaceDetector();
        d.inputName = inputName;
        d.inputSize = inputSize;
        d.imageMean = imageMean;
        d.imageStd = imageStd;
        d.numLocations = numLocations;
        d.boxPriors = new float[numLocations * 8];

        // Pre-allocate buffers.
        d.outputNames = outputName.split(",");
        d.intValues = new int[inputSize * inputSize];
        d.floatValues = new float[inputSize * inputSize * 3];
        d.outputScores = new float[numLocations];
        d.outputLocations = new float[numLocations * 4];
        d.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

        return d;
    }

    @Override
    public int recognizeImage(Bitmap bitmap, File detectedfile) {

        return 0;
    }


    @Override
    public PostureItem recognizeImagePig(Bitmap bitmap, File detectedfile) {
        PostureItem posture = null;
        if (bitmap == null) {
            return null;
        }
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int padSize = Math.max(height, width);
        int offsetX = (padSize - width) / 2;
        int offsetY = (padSize - height) / 2;
        int[] bitmapValues = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(bitmapValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int[] padValues = new int[padSize * padSize];
        long start = System.currentTimeMillis();
        for (int i = 0; i < height; i++) {
            System.arraycopy(bitmapValues, i * width, padValues, (offsetY + i) * padSize + offsetX, width);
        }

        sLogger.i("pre-process bitmap " + (System.currentTimeMillis() - start));
        Bitmap padBitmap = Bitmap.createBitmap(padSize, padSize, Bitmap.Config.ARGB_8888);
        padBitmap.setPixels(padValues, 0, padSize, 0, 0, padSize, padSize);

        Matrix frameToCropTransform = ImageUtils.getTransformationMatrix(padBitmap.getWidth(), padBitmap.getHeight(),
                inputSize, inputSize, 0, true);
        Matrix cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
        Bitmap croppedBitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(padBitmap, frameToCropTransform, null);
        if (detectedfile != null && DEBUG) {
            FileUtils.saveBitmapToFile(padBitmap, new File(detectedfile.getParent() + File.separator + detectedfile.getName().replace("detected", "temp1")));
        }

        float[] inputValues = new float[inputSize * inputSize * 3];
        //int[] inputValues_int = new int[inputSize * inputSize * 3];
        croppedBitmap.getPixels(intValues, 0, croppedBitmap.getWidth(), 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight());
        if (detectedfile != null && DEBUG) {
            FileUtils.saveBitmapToFile(croppedBitmap, new File(detectedfile.getParent() + File.separator + detectedfile.getName().replace("detected", "temp2")));
        }
        for (int i = 0; i < intValues.length; i++) {
            inputValues[i * 3 + 0] = ((float) ((intValues[i] & 0xFF) * 1.0)) / 255.0f;
            inputValues[i * 3 + 1] = ((float) (((intValues[i] >> 8) & 0xFF) * 1.0)) / 255.0f;
            inputValues[i * 3 + 2] = ((float) (((intValues[i] >> 16) & 0xFF) * 1.0)) / 255.0f;
        }

        inferenceInterface.feed(inputName, inputValues, inputSize, inputSize, 3);

        inferenceInterface.run(outputNames);
        sLogger.i("pig pb run cost: " + (System.currentTimeMillis() - start));
        float[] outputExistEncoding = new float[1];
        float[] outputPredict = new float[1 * 8];
        inferenceInterface.fetch(outputNames[0], outputExistEncoding);
        inferenceInterface.fetch(outputNames[1], outputPredict);

        float exist = outputExistEncoding[0];
        sLogger.i("exist face: " + exist);
        if (exist > MIN_CONFIDENCE) {
            if (outputPredict == null || outputPredict.length != 8) {
                return null;
            }

            if (outputPredict[3] < 0 || outputPredict[4] < 0 || outputPredict[5] < 0 || outputPredict[6] < 0
                    || outputPredict[3] > 1 || outputPredict[4] > 1 || outputPredict[5] > 1 || outputPredict[6] > 1) {//超出边框舍弃
                Log.d(TAG, "-Matrix---outputPredict[3]===" + outputPredict[3] + "===outputPredict[4]====" + outputPredict[4] + "===outputPredict[5]====" + outputPredict[5] + "==outputPredict[6]====" + outputPredict[6]);
                return null;
            }
            //1.传回的坐标转屏幕坐标
            //(rot_x, rot_y, rot_z, h_1, w_1, h_2, w_2)，其中角度为弧度，
            //1.1 相对在padSize中值
            float fx1, fx2, fy1, fy2;
            float fx1_new_loc, fx2_new_loc, fy1_new_loc, fy2_new_loc;
            fx1 = (float) outputPredict[4] * padSize;
            fx2 = (float) outputPredict[6] * padSize;
            fy1 = (float) outputPredict[3] * padSize;
            fy2 = (float) outputPredict[5] * padSize;


            Map<String, Integer> positionMap1 = new HashMap<String, Integer>();
            Map<String, Integer> positionMap2 = new HashMap<String, Integer>();
//跟踪框画框位置信息
            float fy1_new0 = (float) outputPredict[3];
            float fx1_new0 = (float) outputPredict[4];
            float fy2_new0 = (float) outputPredict[5];
            float fx2_new0 = (float) outputPredict[6];

            //图片左上角在屏幕的X坐标
            int cutImgPositionX = 0;
            //图片左上角在屏幕的Y坐标
            int cutImgPositionY = ScreenUtil.getScreenHeight()/ 6;
//                int cutImgPositionY = ScreenUtil.getScreenHight() + (ScreenUtil.getScreenHight() - ScreenUtil.getScreenWidth()) / 2;
            //图片宽度(图像像素)
            int cutImgWidth = width;
            //图片高度(图像像素)
            int cutImgHeight = height;
            //图像显示比例 (屏幕显示像素/图像像素)
            float imgDispRate = ScreenUtil.getScreenWidth() * 1.0f / padSize;
            //模型生成的x坐标(0.0-1.0)
            float modelX = fx1_new0;
            //模型生成的y坐标(0.0-1.0)
            float modelY = fy1_new0;

            sLogger.i("cutImgPositionY: d%" + cutImgPositionY);
            sLogger.i("cutImgWidth: d%" + cutImgWidth);
            sLogger.i("cutImgHeight: d%" + cutImgHeight);
            sLogger.i("ScreenUtil.getScreenWidth(): d%" + ScreenUtil.getScreenWidth());
            sLogger.i("imgDispRate: f%" + imgDispRate);
            sLogger.i("modelX: f%" + modelX);
            sLogger.i("modelY: f%" + modelY);
//                传给跟踪框画框函数
            positionMap1 = calculateScreenPosition(cutImgPositionX, cutImgPositionY, cutImgWidth, cutImgHeight, imgDispRate, fx1_new0, fy1_new0);
            positionMap2 = calculateScreenPosition(cutImgPositionX, cutImgPositionY, cutImgWidth, cutImgHeight, imgDispRate, fx2_new0, fy2_new0);

            //1.2 计算显示框大小
            float multiple = 1.0f;
            float frameW, frameH;
            float padx1, padx2, pady1, pady2;
            frameW = fx2 - fx1;
            frameH = fy2 - fy1;
            padx1 = fx1 - (multiple - 1) * frameW / 2;
            padx2 = fx2 + (multiple - 1) * frameW / 2;
            pady1 = fy1 - (multiple - 1) * frameH / 2;
            pady2 = fy2 + (multiple - 1) * frameH / 2;

            //1.4 转屏幕坐标
            float srcx1, srcx2, srcy1, srcy2;
            srcx1 = padx1;
            srcx2 = padx2;
            srcy1 = pady1;
            srcy2 = pady2;

            //获取框选内图片
            float padBitmap_new_width = Math.abs(padx2 - padx1);
            float padBitmap_new_height = Math.abs(pady2 - pady1);
            //原比例图
            int padBitmap_border = Math.max((int) padBitmap_new_height, (int) padBitmap_new_width);
            Bitmap padBitmap_new = Bitmap.createBitmap(padBitmap, (int) srcx1, (int) srcy1, (int) padBitmap_new_width, (int) padBitmap_new_height);
            //缩放到320*320的图片

            //padding成一个正方形
            Bitmap padBitmap_final = Bitmap.createBitmap(padBitmap_border, padBitmap_border, Bitmap.Config.ARGB_8888);
            int[] finalBitmapValues = new int[padBitmap_new.getWidth() * padBitmap_new.getHeight()];

            padBitmap_new.getPixels(finalBitmapValues, 0, padBitmap_new.getWidth(), 0, 0, padBitmap_new.getWidth(), padBitmap_new.getHeight());
            int offset_X = (int) ((padBitmap_border - padBitmap_new_width) / 2.);
            int offset_Y = (int) ((padBitmap_border - padBitmap_new_height) / 2.);
            padBitmap_final.setPixels(finalBitmapValues, 0, padBitmap_new.getWidth(), offset_X, offset_Y, padBitmap_new.getWidth(), padBitmap_new.getHeight());


            int width_zoom = 320, height_zoom = 320;
            Matrix frameToCropTransform_zoom = ImageUtils.getTransformationMatrix(padBitmap_final.getWidth(), padBitmap_final.getHeight(),
                    width_zoom, height_zoom, 0, true);
            Bitmap padBitmap_zoom = Bitmap.createBitmap(width_zoom, height_zoom, Bitmap.Config.ARGB_8888);
            Canvas canvas_zoom = new Canvas(padBitmap_zoom);
            canvas_zoom.drawBitmap(padBitmap_final, frameToCropTransform_zoom, null);

            //获得1.4倍图
            //1.2 计算显示框大小并放大1.4倍
//            float multiple_big = 1.4f;
            float multiple_big = 1.0f;
            float centerX_big, centerY_big, half_frameW_big, half_frameH_big;
            centerX_big = (fx1 + fx2) / 2;
            centerY_big = (fy1 + fy2) / 2;
            half_frameW_big = (fx2 - fx1) * multiple_big / 2;
            half_frameH_big = (fy2 - fy1) * multiple_big / 2;
            //1.3 padSize中框坐标
            float padx1_big, padx2_big, pady1_big, pady2_big;
            padx1_big = centerX_big - half_frameW_big;
            padx2_big = centerX_big + half_frameW_big;
            pady1_big = centerY_big - half_frameH_big;
            pady2_big = centerY_big + half_frameH_big;
            //2.1 计算图片宽高
            float padBitmap_new_width_big = padx2_big - padx1_big;
            float padBitmap_new_height_big = pady2_big - pady1_big;
            //2.2 获取图片
            int x1 = (int) padx1_big + offsetX;
            int x2 = (int) padx2_big + offsetX;
            int y1 = (int) pady1_big + offsetY;
            int y2 = (int) pady2_big + offsetY;

            Rect rect_src, rect_dest;
            int off_x_big = 0, off_y_big = 0;
            if (x1 < 0) {
                off_x_big = (int) (0 - x1);
                x1 = 0;
            }
            if (y1 < 0) {
                off_y_big = (int) (0 - y1);
                y1 = 0;
            }

            rect_src = new Rect(x1, y1, x2, y2);
            rect_dest = new Rect(off_x_big, off_y_big, (int) padBitmap_new_width_big, (int) padBitmap_new_height_big);
            Bitmap padBitmap_big = Bitmap.createBitmap((int) padBitmap_new_width_big, (int) padBitmap_new_height_big, Bitmap.Config.ARGB_8888);
            Canvas canvas_big = new Canvas(padBitmap_big);
            canvas_big.drawBitmap(padBitmap, rect_src, rect_dest, null);
            //缩放到320*320的图片
            int width_big_zoom = 320, height_big_zoom = 320;
            int padBitmap_border_big = Math.max(padBitmap_big.getWidth(), padBitmap_big.getHeight());
            Bitmap padBitmap_final_big = Bitmap.createBitmap(padBitmap_border_big, padBitmap_border_big, Bitmap.Config.ARGB_8888);
            int[] finalBitmapValues_big = new int[padBitmap_big.getWidth() * padBitmap_big.getHeight()];

            padBitmap_big.getPixels(finalBitmapValues_big, 0, padBitmap_big.getWidth(), 0, 0, padBitmap_big.getWidth(), padBitmap_big.getHeight());
            offset_X = (int) ((padBitmap_border_big - padBitmap_big.getWidth()) / 2.);
            offset_Y = (int) ((padBitmap_border_big - padBitmap_big.getHeight()) / 2.);
            padBitmap_final_big.setPixels(finalBitmapValues_big, 0, padBitmap_big.getWidth(), offset_X, offset_Y, padBitmap_big.getWidth(), padBitmap_big.getHeight());

            Matrix frameToCropTransform_big_zoom = ImageUtils.getTransformationMatrix(padBitmap_final_big.getWidth(), padBitmap_final_big.getHeight(),
                    width_big_zoom, height_big_zoom, 0, true);
            Bitmap padBitmap_big_zoom = Bitmap.createBitmap(width_big_zoom, height_big_zoom, Bitmap.Config.ARGB_8888);
            Canvas canvas_big_zoom = new Canvas(padBitmap_big_zoom);
            canvas_big_zoom.drawBitmap(padBitmap_final_big, frameToCropTransform_big_zoom, null);
//            posture = new PostureItem(outputPredict[0], outputPredict[1], outputPredict[2], srcx11, srcy11, srcx22, srcy22, padBitmap_zoom, padBitmap_big_zoom);
            posture = new PostureItem(outputPredict[0], outputPredict[1], outputPredict[2], positionMap1.get("X"), positionMap1.get("Y"),
                    positionMap2.get("X"), positionMap2.get("Y"), padBitmap_zoom, padBitmap_big_zoom);

            Log.d(TAG, "getAngleCategory==rot_x" + outputPredict[0] + "==rot_y==" + outputPredict[1]);

        }

        return posture;
    }


    @Override
    public void enableStatLogging(boolean debug) {
        //inferenceInterface.enableStatLogging(debug);
    }

    @Override
    public String getStatString() {
        return inferenceInterface.getStatString();
    }

    @Override
    public void close() {
        inferenceInterface.close();
    }
}*/
