package com.xiangchuangtec.luolu.animalcounter.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.xiangchuang.risks.utils.PigPreferencesUtils;
import com.xiangchuangtec.luolu.animalcounter.PigAppConfig;
import com.xiangchuangtec.luolu.animalcounter.Recognition;
import com.xiangchuangtec.luolu.animalcounter.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * View which draws rectangles.
 */
public class RecognitionView extends View implements TextToSpeech.OnInitListener {
    private final static String TAG = "RecognitionView";
    private float textSize;
    private final List<Recognition> recognitions = new ArrayList<>();
    private Paint rectPaint = new Paint();
    private TextPaint textPaint = new TextPaint();
    private TextToSpeech textToSpeech;


    public RecognitionView(Context context) {
        super(context);
        init(context);
    }

    public RecognitionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecognitionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        textSize = Utils.setAttributes(context, rectPaint, textPaint);
        textToSpeech = new TextToSpeech(PigAppConfig.getAppContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS && null != textToSpeech) {
                    textToSpeech.setLanguage(Locale.CHINESE);
                    textToSpeech.setPitch(0.3f);
                } else {
                    Log.d(TAG, "onInit: --->" + "TextToSpeech init fail !");
                }
            }
        });
    }

    /**
     * Updates rectangles which will be drawn.
     *
     * @param recognitions recognitions to draw.
     */
    public void setRecognitions(@NonNull List<Recognition> recognitions) {
        ensureMainThread();

        this.recognitions.clear();

        this.recognitions.addAll(recognitions);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawRecognitions(canvas, recognitions, rectPaint, textPaint, textSize);
    }

    private void ensureMainThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalThreadStateException("This method must be called from the main thread");
        }
    }

    //    luolu
    public void drawRecognitions(
            Canvas canvas, List<Recognition> recognitions, Paint rectPaint,
            Paint textPaint, float textSize) {
        // luolu
        int count = 0;

        for (Recognition recognition : recognitions) {
            RectF location = recognition.getLocation();

            float left = location.left * canvas.getWidth();
            float right = location.right * canvas.getWidth();
            float top = location.top * canvas.getHeight();
            float bottom = location.bottom * canvas.getHeight();
            Log.i(TAG, canvas.getWidth() + "*" + canvas.getHeight() + ", "
                    + "location = (" + left + "," + top + ")(" + right + "," + bottom + ")");
            canvas.drawRect(left, top, right, bottom, rectPaint);
            count++;

        }
        String toSpeak = count + "头";
        Long currentDateTimeString = System.currentTimeMillis();// && (currentDateTimeString%2)==0
        if (count > 0) {
            Log.i("===isfleg====", PigPreferencesUtils.getBooleanValue("isfleg", PigAppConfig.getAppContext()) + "");
            if (!PigPreferencesUtils.getBooleanValue("isfleg", PigAppConfig.getAppContext())) {
                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_ADD, null);
            }
            textToSpeech.setPitch(0.3f);
        }
    }


    @Override
    public void onInit(int status) {
        textToSpeech.setLanguage(Locale.CHINESE);
        textToSpeech.setPitch(0.3f);
    }
}
