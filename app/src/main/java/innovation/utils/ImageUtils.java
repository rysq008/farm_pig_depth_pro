package innovation.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.demo.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wbs on 12/10/17.
 */

public class ImageUtils {
    private static Logger sLogger = new Logger("ImageUtils");

    public static boolean isBlurByOpenCV(Bitmap image) {
        Mat matImage = new Mat();
        Utils.bitmapToMat(image, matImage);
        Mat matImageGrey = new Mat();
        Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY); // 图像灰度化

        Mat laplacianImage = new Mat();
        Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U); // 拉普拉斯变换

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stdDev = new MatOfDouble();
        Core.meanStdDev(laplacianImage, mean, stdDev);

        matImage.release();
        laplacianImage.release();

        float blur = (float) (stdDev.get(0, 0)[0] * stdDev.get(0, 0)[0]);
        sLogger.i("mean: " + mean.get(0, 0)[0] + " stdDev: " + stdDev.get(0, 0)[0] + " blur: " + blur);
        return blur < 100;
    }

    public static boolean isBlurByOpenCV(String picFilePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 通过path得到一个不超过2000*2000的Bitmap
        Bitmap image = decodeSampledBitmapFromFile(picFilePath, options, 2000, 2000);
        return isBlurByOpenCV(image);
    }

    private static Bitmap decodeSampledBitmapFromFile(String imgPath, BitmapFactory.Options options, int reqWidth, int reqHeight) {
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, options);
        // inSampleSize为缩放比例，举例：options.inSampleSize = 2表示缩小为原来的1/2，3则是1/3，以此类推
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imgPath, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
        sLogger.d("inSampleSize=" + inSampleSize);
        return inSampleSize;
    }

    //    luolu
    private static boolean isBlurredImage(Bitmap image) {
        try {
            if (image != null) {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inDither = true;
                opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
                int l = CvType.CV_8UC1;
                Mat matImage = new Mat();
                Utils.bitmapToMat(image, matImage);
                Mat matImageGrey = new Mat();
                Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

                Mat dst2 = new Mat();
                Utils.bitmapToMat(image, dst2);

                Mat laplacianImage = new Mat();
                dst2.convertTo(laplacianImage, l);
                Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
                Mat laplacianImage8bit = new Mat();
                laplacianImage.convertTo(laplacianImage8bit, l);
                System.gc();

                Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(),
                        laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);

                Utils.matToBitmap(laplacianImage8bit, bmp);

                int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
                bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
                        bmp.getHeight());
                if (bmp != null)
                    if (!bmp.isRecycled()) {
                        bmp.recycle();

                    }
                int maxLap = -16777216;

                for (int i = 0; i < pixels.length; i++) {

                    if (pixels[i] > maxLap) {
                        maxLap = pixels[i];
                    }
                }
                int soglia = -6118750;

                if (maxLap < soglia || maxLap == soglia) {


                    return true;
                } else {


                    return false;
                }
            } else {
                return false;
            }
        } catch (NullPointerException e) {
            return false;
        } catch (OutOfMemoryError e) {
            return false;
        }
    }

    //haojie add
    public static boolean isBlurByOpenCV_new(Bitmap image) {
        Mat matImage = new Mat();
        Utils.bitmapToMat(image, matImage);
        Mat matImageGrey = new Mat();
        Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY); // 图像灰度化

        Mat laplacianImage = new Mat();
        Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U); // 拉普拉斯变换

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stdDev = new MatOfDouble();
        Core.meanStdDev(laplacianImage, mean, stdDev);

        matImage.release();
        laplacianImage.release();

        float blur = (float) (stdDev.get(0, 0)[0] * stdDev.get(0, 0)[0]);
        sLogger.i("mean: " + mean.get(0, 0)[0] + " stdDev: " + stdDev.get(0, 0)[0] + " blur: " + blur);
       // Log.d(TAG, "图像imgae-------blur test --set_checkcount----------blur = " + blur);
        return blur < 15;
    }

    /**
     * @param cutImgPositionX	图片左上角在屏幕的X坐标
     * @param cutImgPositionY	图片左上角在屏幕的Y坐标
     * @param cutImgWidth		图片宽度(图像像素)
     * @param cutImgHeight		图片高度(图像像素)
     * @param imgDispRate		图像显示比例 (屏幕显示像素/图像像素)
     * @param modelX			模型生成的x坐标(0.0-1.0)
     * @param modelY			模型生成的y坐标(0.0-1.0)
     * @return
     */
    public static Map<String, Integer> calculateScreenPosition(int cutImgPositionX, int cutImgPositionY, int cutImgWidth, int cutImgHeight, float imgDispRate, float modelX, float modelY) {
        Map<String, Integer> resultMap = new HashMap<String, Integer>();
        int pointX = cutImgPositionX + (int) ((cutImgWidth * imgDispRate) * modelX);
        int pointY = cutImgPositionY + (int) ((cutImgHeight * imgDispRate) * modelY);
        resultMap.put("X", pointX);
        resultMap.put("Y", pointY);
        return resultMap;
    }
}
