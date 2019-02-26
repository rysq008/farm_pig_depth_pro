package innovation.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.demo.Global;
import org.tensorflow.demo.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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


    public static Bitmap padBitmap2SpRatio(Bitmap bitmap,float ratio) {
        int paddingW = 0;
        int paddingH = 0;
        float paddedWHRatio;
        float srcWHRatio;
        srcWHRatio = (float)bitmap.getWidth()/(float)bitmap.getHeight();
        sLogger.i("srcWHRatio %f=" + srcWHRatio);
        if (srcWHRatio == ratio){
            return bitmap;
        }
        if (bitmap.getWidth() > bitmap.getHeight()) {
            paddingW = 0;
            paddingH = (int)(bitmap.getWidth() / ratio) - bitmap.getHeight();
            sLogger.i("paddingH=" + paddingH);
        } else if (bitmap.getHeight() > bitmap.getWidth()){
            paddingH = 0;
            paddingW = (int)(bitmap.getHeight() * ratio)- bitmap.getWidth();
            sLogger.i("paddingW=" + paddingW);
        }

        Bitmap paddedBitmap = Bitmap.createBitmap(
                bitmap.getWidth() + paddingW,
                bitmap.getHeight() + paddingH,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(paddedBitmap);
        canvas.drawARGB(0x00, 0x00, 0x00, 0x00); // this represents color
        canvas.drawBitmap(
                bitmap,
                paddingW / 2,
                paddingH / 2,
                new Paint(Paint.FILTER_BITMAP_FLAG));


        paddedWHRatio = (float)paddedBitmap.getWidth()/(float)paddedBitmap.getHeight();
        sLogger.i("paddedWHRatio %f=" + paddedWHRatio);

        return paddedBitmap;
    }

    /***
     * 图片的缩放方法
     *
     * @param bgimage
     *            ：源图片资源
     * @param newWidth
     *            ：缩放后宽度
     * @param newHeight
     *            ：缩放后高度
     * @return
     */
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }


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

    public static boolean isBlurByOpenCV_new(Bitmap image) {
        return false;/*
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
//        sLogger.i("mean: " + mean.get(0, 0)[0] + " stdDev: " + stdDev.get(0, 0)[0] + " blur: " + blur);

        return blur < 20;*/
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

    //获取图片亮度值
    public static int getImageBright(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int r, g, b;
        int count = 0;
        int bright = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                count++;
                int localTemp = bm.getPixel(i, j);
                r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
                g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
                b = (localTemp | 0xffffff00) & 0x0000ff;
                bright = (int) (bright + 0.299 * r + 0.587 * g + 0.114 * b);
            }
        }
        return bright / count;
    }

    //检测亮度
    public static int checkImageBright(Bitmap bitmap) {
        //对图像进行模糊度，明暗度判断
        //先缩放再获得亮度
        Bitmap checkBitmap = ImageUtils.getPostScaleBitmap(bitmap);
        long time0 = System.currentTimeMillis();
        int bitBright = ImageUtils.getImageBright(checkBitmap);
        long time1 = System.currentTimeMillis();
        return bitBright;
    }

    /**
     * 缩放图片
     *
     * @param
     */
    public static  Bitmap getPostScaleBitmap(Bitmap bitmap) {
        // Matrix类进行图片处理（缩小或者旋转）
        Matrix matrix = new Matrix();
        // 根据指定高度宽度缩放
        matrix.postScale(0.05f, 0.05f);
        // 生成新的图片
        try {
            Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            if (dstbmp != null) {
                return dstbmp;
            }
        } catch (Exception e) {
            String s = e.getMessage().toString();
            return null;
        }
        return null;
    }

    public static Bitmap rotateBitmap(Bitmap src, float degree) {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        Bitmap bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        return bmp;
    }

    /** Creates and returns a new bitmap which is the same as the provided bitmap
     * but with horizontal or vertical padding (if necessary)
     * either side of the original bitmap
     * so that the resulting bitmap is a square.
     * @param bitmap is the bitmap to pad.
     * @return the padded bitmap.*/
    public static Bitmap padBitmap(Bitmap bitmap) {
        int paddingX;
        int paddingY;

        if (bitmap.getWidth() == bitmap.getHeight()) {
            paddingX = 0;
            paddingY = 0;
        } else if (bitmap.getWidth() > bitmap.getHeight()) {
            paddingX = 0;
            paddingY = bitmap.getWidth() - bitmap.getHeight();
        } else {
            paddingX = bitmap.getHeight() - bitmap.getWidth();
            paddingY = 0;
        }

        Bitmap paddedBitmap = Bitmap.createBitmap(
                bitmap.getWidth() + paddingX,
                bitmap.getHeight() + paddingY,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(paddedBitmap);
        canvas.drawARGB(0x00, 0x00, 0x00, 0x00); // this represents color
        canvas.drawBitmap(
                bitmap,
                paddingX / 2,
                paddingY / 2,
                new Paint(Paint.FILTER_BITMAP_FLAG));

        return paddedBitmap;
    }


    public static Bitmap clipBitmap(Bitmap bitmap, float X0, float Y0, float X1, float Y1, float multiple) {
        Bitmap clipBitmap = null;
        Canvas canvasClipRecognition = new Canvas(bitmap);
        int padSize = Math.max(canvasClipRecognition.getHeight(), canvasClipRecognition.getWidth());
        float left = X0 * canvasClipRecognition.getWidth();
        float top = Y0 * canvasClipRecognition.getHeight();
        float right = X1 * canvasClipRecognition.getWidth();
        float bottom = Y1 * canvasClipRecognition.getHeight();
        int multiX = (int) ((multiple + 1) * left - (multiple - 1) * right) / 2;
        int multiY = (int) ((multiple + 1) * top - (multiple - 1) * bottom) / 2;
        int multiX1 = (int) ((multiple + 1) * right - (multiple - 1) * left) / 2;
        int multiY1 = (int) ((multiple + 1) * bottom - (multiple - 1) * top) / 2;
        if (multiX < 0) {
            multiX = 0;
        }
        if (multiY < 0) {
            multiY = 0;
        }
        if (multiX1 > padSize) {
            multiX1 = padSize;
        }
        if (multiY1 > padSize) {
            multiY1 = padSize;
        }
        clipBitmap = Bitmap.createBitmap(bitmap,
                multiX, multiY,
                multiX1 - multiX,
                multiY1 - multiY);

        return clipBitmap;
    }

    /**
     * 将图片转换成720*960格式并压缩至80%品质
     *
     * @param bitmap 待处理图片
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bitmap) {

        // 图片按比例压缩，以长边=1080为准
        // 图片质量提升为60.
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = 1f;

        int max = Math.max(width, height);
        scale = 960f / max;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            newbm.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            baos.flush();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inPreferredConfig = Bitmap.Config.;
        newbm = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length, options);

        return newbm;
    }

}
