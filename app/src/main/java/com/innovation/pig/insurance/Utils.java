package com.innovation.pig.insurance;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.gson.Gson;
import com.xiangchuang.risks.model.bean.QueryVideoFlagDataBean;
import com.innovation.pig.insurance.netutils.Constants;
import com.xiangchuang.risks.utils.PigPreferencesUtils;

import org.tensorflow.demo.DetectorActivity;

import java.util.List;

import innovation.biz.classifier.PigFaceDetectTFlite;

import static android.app.PendingIntent.getActivity;
import static com.innovation.pig.insurance.AppConfig.timeVideoStart;

/**
 * Created by luolu on 2018/3/1.
 */
public class Utils {
    private static final String TAG = "Utils";
    private static Logger mlogger = new Logger();
    private TextToSpeech textToSpeech;
    private Context mcontex;

    public Utils() {
    }

    public static int getRotation(int frameRotation) {
        int rotation = 0;
        switch (frameRotation) {
            case 270:
                rotation = 90;
                break;
            case 180:
                rotation = 180;
                break;
            case 90:
                rotation = 270;
                break;
        }
        return rotation;
    }

    private static Matrix getTransformationMatrix(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation) {
        final Matrix matrix = new Matrix();

        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                Log.w(TAG, "Rotation of " + applyRotation + " % 90 != 0");
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;

        final int inWidth = transpose ? srcHeight : srcWidth;
        final int inHeight = transpose ? srcWidth : srcHeight;

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            final float scaleFactorX = dstWidth / (float) inWidth;
            final float scaleFactorY = dstHeight / (float) inHeight;
            final float scaleFactor = Math.min(scaleFactorX, scaleFactorY);
            matrix.postScale(scaleFactor, scaleFactor);
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;
    }

    public static void drawRecognitions(
            Canvas canvas, List<Recognition> recognitions, Paint rectPaint,
            Paint textPaint, float textSize) {

        int count = 0;
        for (Recognition recognition : recognitions) {
            RectF location = recognition.getLocation();
            float left = location.left * canvas.getWidth();
            float right = location.right * canvas.getWidth();
            float top = location.top * canvas.getHeight();
            float bottom = location.bottom * canvas.getHeight();
            mlogger.i(TAG, canvas.getWidth() + "*" + canvas.getHeight() + ", "
                    + "location = (" + left + "," + top + ")(" + right + "," + bottom + ")");
//                            synchronized (this) {
            canvas.drawRect(left, top, right, bottom, rectPaint);
//                            }
            count++;

        }
        canvas.drawText(count + "头", canvas.getWidth() - 400,
                canvas.getHeight() - 150, textPaint);
    }

    public static float setAttributes(Context context, Paint rectPaint, Paint textPaint) {
        Resources resources = context.getResources();

//        rectPaint.setColor(resources.getColor(R.color.colorRect));
//        luolu
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.rect_stroke_size));
        rectPaint.setStyle(Paint.Style.STROKE);

        float textSize = resources.getDimensionPixelSize(R.dimen.draw_text_size);
        textPaint.setTextSize(textSize);
        textPaint.setColor(resources.getColor(R.color.colorText));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);

        return textSize;
    }

    /**
     * 获取录制开始到当前节点的时长
     */
    public static Long getDuring(long timestamp) {
        Log.i("图片数量计算---1", "总长" + AppConfig.during + ":当前时间戳" + timestamp + ":开始时间戳" + timeVideoStart);
        return AppConfig.during + (timestamp - timeVideoStart);
    }

    /**
     * 判断已成功采集图片数未达到相应要求
     */
    public static Boolean notUpToStandard(long timestamp, int pastSeconds) {
        return (getDuring(timestamp) / 1000 / (DetectorActivity.type1Count + DetectorActivity.type2Count + DetectorActivity.type3Count + 1)) > pastSeconds;
    }

    /**
     * 获取投保或理赔的牲畜识别阈值
     */
    public static void getThreshold() {
        String tlist = PigPreferencesUtils.getStringValue(Constants.THRESHOLD_LIST, AppConfig.getActivity());
        QueryVideoFlagDataBean.thresholdList thresholdList = new Gson().fromJson(tlist, QueryVideoFlagDataBean.thresholdList.class);
        Log.e("getThreshold", thresholdList.toString());

        PigFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getPigtoubao());

    }

    /**
     * 获取理赔的牲畜识别 降低后的阈值
     */
    public static void setLowThreshold() {
        String tlist = PigPreferencesUtils.getStringValue(Constants.THRESHOLD_LIST, AppConfig.getActivity());
        QueryVideoFlagDataBean.thresholdList thresholdList = new Gson().fromJson(tlist, QueryVideoFlagDataBean.thresholdList.class);
        Log.e("getLowThreshold", thresholdList.toString());

        PigFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getPiglipei2());
    }

}

