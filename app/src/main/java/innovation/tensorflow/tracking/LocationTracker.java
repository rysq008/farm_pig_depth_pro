package innovation.tensorflow.tracking;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.widget.Toast;

import innovation.entry.InnApplication;
import innovation.utils.ScreenUtil;
import innovation.utils.UIUtils;

import org.tensorflow.demo.CameraConnectionFragment;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.Global;
import org.tensorflow.demo.PostureItem;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.tracking.TrackerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
/**
 * Author by luolu, Date on 2018/8/27.
 * COMPANY：InnovationAI
 */

public class LocationTracker {

    private static final float TEXT_SIZE_DIP = 18;
    private final Paint mBoxPaint = new Paint();
    private final Paint mBoxPaint1 = new Paint();
    private final float mTextSizePx;
    private final BorderedText mBorderedText;
    private final BorderedText mBorderedText1;

    private int mFrameWidth;
    private int mFrameHeight;
    private int mSensorOrientation;

    private long showTime_start = 0;
    private long showTime_end = 0;

    //    private List<Pair<Float, RectF>> mFrameRects = new ArrayList<>();
    private List<TrackerItem> mFrameRects = new ArrayList<TrackerItem>();
    private Vector listAngles_capture = new Vector();


    private Matrix mFrameToCanvasMatrix;

    private static int type1Sum;
    private static int type2Sum;
    private static int type3Sum;

    public LocationTracker(final DisplayMetrics metrics) {
        mBoxPaint.setColor(Color.RED);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(6.0f);
        mBoxPaint.setStrokeCap(Paint.Cap.ROUND);
        mBoxPaint.setStrokeJoin(Paint.Join.ROUND);
        mBoxPaint.setStrokeMiter(100);

        mBoxPaint1.setColor(Color.BLUE);
        mBoxPaint1.setStyle(Paint.Style.STROKE);
        mBoxPaint1.setStrokeWidth(6.0f);
        mBoxPaint1.setStrokeCap(Paint.Cap.ROUND);
        mBoxPaint1.setStrokeJoin(Paint.Join.ROUND);
        mBoxPaint1.setStrokeMiter(100);

        mTextSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, metrics);
        mBorderedText = new BorderedText(mTextSizePx);
        mBorderedText1 = new BorderedText(mTextSizePx);
        int holoBlueDark = Color.parseColor("#ff0099cc");
        mBorderedText1.setInteriorColor(holoBlueDark);

        showTime_start = System.currentTimeMillis();
    }

    public synchronized void draw(Canvas canvas) {
        int canvasW = ScreenUtil.getScreenWidth();
        int canvasH = ScreenUtil.getScreenHeight();
        String reminderMsgText = "请将脸放入框中";
        Point centerOfCanvas = new Point(canvasW / 2, canvasH / 2);
        int rectW = 600;
        int rectH = 600;
        int left = centerOfCanvas.x - (rectW / 2);
        int top = centerOfCanvas.y - (rectH / 2);
        int right = centerOfCanvas.x + (rectW / 2);
        int bottom = centerOfCanvas.y + (rectH / 2);
        Rect rect = new Rect(left, top, right, bottom);
//        canvas.drawRect(rect, mBoxPaint1);
        listAngles_capture.clear();
        getCurrentTypeList();

        if (mFrameRects.isEmpty()) {
            //return;
            showTime_end = System.currentTimeMillis();
            //输出所有捕获的角度
            long during = showTime_end - showTime_start;
            //    Log.i("during", String.valueOf(during));
            if(during > 2000){
                showTime_start = System.currentTimeMillis();
            }
            int drawY_capture = (int) (mBorderedText.getTextSize() *  listAngles_capture.size());
            mBorderedText.drawLines(canvas, 100, drawY_capture + 50,  listAngles_capture);
            return;
        }

        Log.i("canvas height", String.valueOf(canvas.getHeight()));
        Log.i("canvas width", String.valueOf(canvas.getWidth()));

        for(TrackerItem item : mFrameRects){
            RectF trackRectF = item.mRect;
            float cornerSize = Math.min(trackRectF.width(), trackRectF.height()) / 8.0f;
            canvas.drawRoundRect(trackRectF, cornerSize, cornerSize, mBoxPaint);
            String tempAngle = "未知";
            if (item.mAngletype == 1){
                tempAngle = "左脸";
            }else if (item.mAngletype == 2){
                tempAngle = "正脸";
            }else if (item.mAngletype == 3){
                tempAngle = "右脸";
            }else if (item.mAngletype == 10){
                tempAngle = "未识别";
            }
//            String s1 = "角度："+ item.mAngletype + "\r\n";
            String s1 = tempAngle + "\r\n";
            String s2 = "rot_x："+ item.mRot_x + "\r\n";
            String s3 = "rot_y："+ item.mRot_y + "\r\n";
            String s4 = "rot_z："+ item.mRot_z + "\r\n";
            Vector<String> vec = new Vector<String>();
            vec.add(s1);//把字符串str压进容器
            mBorderedText.drawLines(canvas, (trackRectF.left + trackRectF.right)/2, (trackRectF.top +trackRectF.bottom)/2, vec);
            //在屏幕上输出已采集到的角度值
            showTime_start = System.currentTimeMillis();
            int drawY_capture = (int) (mBorderedText.getTextSize() *  listAngles_capture.size());
            mBorderedText.drawLines(canvas, 100, drawY_capture + 50,  listAngles_capture);

        }
        mBorderedText.drawText(canvas, left + 50, (top + bottom)/2,  getReminderMsgText());
    }

    public synchronized void onFrame(int frameWidth, int frameHeight, int rowStride, int sensorOrientation, byte[] frame, long timestamp) {
        mFrameWidth = frameWidth;
        mFrameHeight = frameHeight;
        mSensorOrientation = sensorOrientation;
    }

    public synchronized void trackResults(List<Classifier.Recognition> results, byte[] luminance, long currTimestamp) {
        mFrameRects.clear();
        if (results.isEmpty()) return;
        for (Classifier.Recognition result : results) {
            if (result.getLocation() == null) {
                continue;
            }
            //mFrameRects.add(new Pair<>(result.getConfidence(), result.getLocation()));
        }
    }


    public synchronized void trackResults_new(PostureItem posture, int angletype) {
        mFrameRects.clear();
        if (posture == null) {
            return;
        }
        RectF trackRectF = new RectF(posture.original_x1, posture.original_y1, posture.original_x2, posture.original_y2);
        TrackerItem item = new TrackerItem(angletype, trackRectF, posture.rot_x, posture.rot_y, posture.rot_z);
        mFrameRects.add(item);
    }

    // TODO: 2018/8/27 By:LuoLu
    public void getCountOfCurrentImage(int type1,int type2,int type3){
        type1Sum = type1;
        type2Sum = type2;
        type3Sum = type3;

        Log.d("LocationTrackerType1：", String.valueOf(type1));
        Log.d("LocationTrackerType2：", String.valueOf(type2));
        Log.d("LocationTrackerType3：", String.valueOf(type3));
    }
    public void reInitCounter(int type1,int type2,int type3){
        type1Sum = type1;
        type2Sum = type2;
        type3Sum = type3;
        listAngles_capture.clear();
        Log.d("LocreInitCounterType1：", String.valueOf(type1));
        Log.d("LocreInitCounterType2：", String.valueOf(type2));
        Log.d("LocreInitCounterType3：", String.valueOf(type3));
    }

    public void getCurrentTypeList(){
        // 左脸   Global.MAX_FACE_LEFT / 2 -1
        if(type1Sum < 3) {
            listAngles_capture.add("左脸，数量：" + type1Sum + "(不足)");
        } else if (type1Sum >= Global.MAX_FACE_LEFT){
            listAngles_capture.add("左脸，数量：" + type1Sum + "(上限)");
        } else {
            listAngles_capture.add("左脸，数量：" + type1Sum + "(OK)");
        }

        // 正脸   Global.MAX_FACE_MIDDLE / 2 -1
        if(type2Sum < 7) {
            listAngles_capture.add("正脸，数量：" + type2Sum + "(不足)");
        } else if (type2Sum >= Global.MAX_FACE_MIDDLE){
            listAngles_capture.add("正脸，数量：" + type2Sum + "(上限)");
        } else {
            listAngles_capture.add("正脸，数量：" + type2Sum + "(OK)");
        }

        // 右脸   Global.MAX_FACE_RIGHT / 2 -1
        if(type3Sum < 3) {
            listAngles_capture.add("右脸，数量：" + type3Sum + "(不足)");
        } else if (type3Sum >= Global.MAX_FACE_RIGHT){
            listAngles_capture.add("右脸，数量：" + type3Sum + "(上限)");
        } else {
            listAngles_capture.add("右脸，数量：" + type3Sum + "(OK)");
        }
    }
    public String getReminderMsgText() {
        boolean b = false;
        if(type1Sum < 3 && type2Sum < 7 && type3Sum < 3){
            return "请将脸放入框中";
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("请将");
            if(type1Sum < 3) {
                sb.append("左");
                b = true;
            }
            if(type2Sum < 7) {
                if(b)   sb.append("/");
                sb.append("正");
                b = true;
            }
            if(type3Sum < 3) {
                if(b)   sb.append("/");
                sb.append("右");
            }
            sb.append("脸放入框中");
            return sb.toString();
        }
    }

}
