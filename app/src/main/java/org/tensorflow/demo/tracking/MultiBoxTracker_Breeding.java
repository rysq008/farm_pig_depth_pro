/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.demo.tracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.widget.Toast;

import innovation.biz.classifier.BreedingPigFaceDetectTFlite;
import innovation.biz.iterm.PostureItem;
import innovation.biz.iterm.TrackerItem;
import innovation.utils.ScreenUtil;

import org.tensorflow.demo.env.BorderedText_Breeding;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import static com.xiangchuangtec.luolu.animalcounter.PigAppConfig.sowCount;


/**
 * A tracker wrapping ObjectTracker that also handles non-max suppression and matching existing
 * objects to new detections.
 */
public class MultiBoxTracker_Breeding {
    private final Logger logger = new Logger();

    public static final float TEXT_SIZE_DIP = 24;

    // Maximum percentage of a box that can be overlapped by another box at detection time. Otherwise
    // the lower scored box (new or old) will be removed.
    private static final float MAX_OVERLAP = 0.2f;

    private static final float MIN_SIZE = 16.0f;

    // Allow replacement of the tracked box with new results if
    // correlation has dropped below this level.
    private static final float MARGINAL_CORRELATION = 0.75f;

    // Consider object to be lost if correlation falls below this threshold.
    private static final float MIN_CORRELATION = 0.3f;

    //    private static final int[] COLORS = {
//            Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.WHITE,
//            Color.parseColor("#55FF55"), Color.parseColor("#FFA500"), Color.parseColor("#FF8888"),
//            Color.parseColor("#AAAAFF"), Color.parseColor("#FFFFAA"), Color.parseColor("#55AAAA"),
//            Color.parseColor("#AA33AA"), Color.parseColor("#0D0068")
//    };
    private static final int[] COLORS = {
            Color.RED
    };

    private final Queue<Integer> availableColors = new LinkedList<Integer>();

    public ObjectTracker objectTracker;

    final List<Pair<Float, RectF>> screenRects = new LinkedList<Pair<Float, RectF>>();
    private List<BreedingPigFaceDetectTFlite.Recognition> keyPointsResult;

    private static class TrackedRecognition {
        ObjectTracker.TrackedObject trackedObject;
        RectF location;
        float detectionConfidence;
        int color;
        String title;
        List<Point> points;
    }

    private final List<TrackedRecognition> trackedObjects = new LinkedList<TrackedRecognition>();

    private final Paint boxPaint = new Paint();

    private float textSizePx;
    private BorderedText_Breeding borderedText;

    private Matrix frameToCanvasMatrix;

    private int frameWidth;
    private int frameHeight;

    private int sensorOrientation;
    private Context context;
    // TODO: 2018/9/14 By:LuoLu
    private List<TrackerItem> mFrameRects = new ArrayList<TrackerItem>();
    private Vector listAngles_capture = new Vector();
    private long showTime_start = 0;
    private long showTime_end = 0;


    public MultiBoxTracker_Breeding(final Context context) {
        this.context = context;
        for (final int color : COLORS) {
            availableColors.add(color);
        }

        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Style.STROKE);
        boxPaint.setStrokeWidth(12.0f);
        boxPaint.setStrokeCap(Cap.ROUND);
        boxPaint.setStrokeJoin(Join.ROUND);
        boxPaint.setStrokeMiter(100);

        textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, context.getResources().getDisplayMetrics());
        borderedText = new BorderedText_Breeding(textSizePx);
        // TODO: 2018/9/14 By:LuoLu
        showTime_start = System.currentTimeMillis();
    }

    private Matrix getFrameToCanvasMatrix() {
        return frameToCanvasMatrix;
    }

    public synchronized void trackResults(
            final List<BreedingPigFaceDetectTFlite.Recognition> results, final byte[] frame, final long timestamp) {
        logger.i("Processing %d results from %d", results.size(), timestamp);
        processResults(timestamp, results, frame);
        keyPointsResult = results;
    }

    public synchronized void draw(final Canvas canvas, int animalType) {
        long drawStar = System.currentTimeMillis();
        Log.e("huakuangdraw", "drawStar "+drawStar );
        int canvasW = ScreenUtil.getScreenWidth();
        int canvasH = ScreenUtil.getScreenHeight();
        Point centerOfCanvas = new Point(canvasW / 2, canvasH / 2);
        int rectW = canvasW / 2;
        int rectH = canvasH / 2;
        int left = centerOfCanvas.x - (rectW);
        int top = centerOfCanvas.y - (rectH / 2);
        int right = centerOfCanvas.x + (rectW);
        int bottom = centerOfCanvas.y + (rectH / 2);
        listAngles_capture.clear();
//        getCurrentTypeList();
//
//        final Paint boxPaint = new Paint();
//        boxPaint.setColor(Color.BLUE);
//        boxPaint.setStyle(Style.STROKE);
//        boxPaint.setStrokeWidth(4f);
//        canvas.drawRect(canvas.getWidth() * 0.15f, frameWidth * 0.18f, canvas.getWidth() * 0.85f, frameWidth * 0.85f, boxPaint);

        //判断是什么动物画框
//        drawCowBorder(canvas);

        if (mFrameRects.isEmpty()) {
            //return;
            showTime_end = System.currentTimeMillis();
            //输出所有捕获的角度
            long during = showTime_end - showTime_start;
            //    Log.i("during", String.valueOf(during));
            if (during > 2000) {
                showTime_start = System.currentTimeMillis();
            }
            int drawY_capture = (int) (borderedText.getTextSize() * listAngles_capture.size());
            borderedText.drawLines(canvas, 100, drawY_capture + 50, listAngles_capture);
            return;
        }


        final boolean rotated = sensorOrientation % 180 == 90;
        final float multiplier =
                Math.min(canvas.getHeight() / (float) (rotated ? frameWidth : frameHeight),
                        canvas.getWidth() / (float) (rotated ? frameHeight : frameWidth));
        frameToCanvasMatrix =
                ImageUtils.getTransformationMatrix(
                        frameWidth,
                        frameHeight,
                        (int) (multiplier * (rotated ? frameHeight : frameWidth)),
                        (int) (multiplier * (rotated ? frameWidth : frameHeight)),
                        sensorOrientation,
                        false);
        Log.e("trackedObjects.size()", sowCount+"---"+trackedObjects.size() );;

        float width = canvas.getWidth();
        float height = canvas.getHeight();

        for (int i = 0; i < trackedObjects.size(); i++) {
            final RectF trackedPos =
                    (objectTracker != null)
                            ? trackedObjects.get(i).trackedObject.getTrackedPositionInPreviewFrame()
                            : new RectF(trackedObjects.get(i).location);
            getFrameToCanvasMatrix().mapRect(trackedPos);
            boxPaint.setColor(trackedObjects.get(i).color);
            final float cornerSize = Math.min(trackedPos.width(), trackedPos.height()) / 8.0f;
            canvas.drawRoundRect(trackedPos, cornerSize, cornerSize, boxPaint);

            //            borderedText.drawText(canvas, trackedPos.left + cornerSize, trackedPos.bottom, (sowCount-i)+"头");
        }
        borderedText.drawText(canvas, 100,100,sowCount+"头");
        Log.e("huakuangdraw", "drawEnd "+(System.currentTimeMillis()-drawStar) );
    }

    /**
     * 画牛框
     * @param canvas
     */
    private void drawCowBorder(final Canvas canvas){
        //1280  高
        Log.e("multibox", "frameWidth: "+frameWidth);
        //960   宽
        Log.e("multibox", "frameHeight: "+frameHeight);
        //1080
        Log.e("multibox", "canvas.getWidth: "+canvas.getWidth());
        //2030
        Log.e("multibox", "canvas.getHeight: "+canvas.getHeight());

        float rate = (float)canvas.getWidth() / (float)frameHeight;

        int realHeight = (int) (frameWidth * rate);
        int realWidth = canvas.getWidth();

        Log.e("multibox", "realHeight: "+realHeight+"---------realWidth:"+realWidth );

        //画竖线
        final Paint boxPaint1 = new Paint();
        boxPaint1.setColor(0xc0006600);
        boxPaint1.setStyle(Style.STROKE);
        boxPaint1.setStrokeWidth(realWidth * 0.3f);
        canvas.drawLine(0f,realHeight*0.15f,0f,realHeight * 0.85f,boxPaint1);
        canvas.drawLine(realWidth,realHeight * 0.15f,realWidth,realHeight * 0.85f,boxPaint1);
        //画横线
        final Paint boxPaint2 = new Paint();
        boxPaint2.setColor(0xc0000000);
        boxPaint2.setStyle(Style.STROKE);
        boxPaint2.setStrokeWidth(realHeight * 0.3f);
        canvas.drawLine(0f, 0f, realWidth, 0f, boxPaint2);
        canvas.drawLine(0f, realHeight, realWidth, realHeight, boxPaint2);
    }

    private boolean initialized = false;

    public synchronized void onFrame(
            final int w,
            final int h,
            final int rowStride,
            final int sensorOrientation,
            final byte[] frame,
            final long timestamp) {
        if (objectTracker == null && !initialized) {
            ObjectTracker.clearInstance();

            logger.i("Initializing ObjectTracker: %dx%d", w, h);
            logger.i("onFrame Initializing sensorOrientation: %d", sensorOrientation);
            objectTracker = ObjectTracker.getInstance(w, h, rowStride, true);
            frameWidth = w;
            frameHeight = h;
            this.sensorOrientation = sensorOrientation;
            initialized = true;

            if (objectTracker == null) {
                String message =
                        "Object tracking support not found. "
                                + "See tensorflow/examples/android/README.md for details.";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                logger.e(message);
            }
        }

        if (objectTracker == null) {
            return;
        }

        objectTracker.nextFrame(frame, null, timestamp, null, true);

        // Clean up any objects not worth tracking any more.
        final LinkedList<TrackedRecognition> copyList =
                new LinkedList<TrackedRecognition>(trackedObjects);
        for (final TrackedRecognition recognition : copyList) {
            final ObjectTracker.TrackedObject trackedObject = recognition.trackedObject;
            final float correlation = trackedObject.getCurrentCorrelation();
            if (correlation < MIN_CORRELATION) {
                logger.v("Removing tracked object %s because NCC is %.2f", trackedObject, correlation);
                trackedObject.stopTracking();
                trackedObjects.remove(recognition);

                availableColors.add(recognition.color);
            }
        }
    }

    private void processResults(
            final long timestamp, final List<BreedingPigFaceDetectTFlite.Recognition> results, final byte[] originalFrame) {
        final List<Pair<Float, BreedingPigFaceDetectTFlite.Recognition>> rectsToTrack = new LinkedList<Pair<Float, BreedingPigFaceDetectTFlite.Recognition>>();

        screenRects.clear();
        final Matrix rgbFrameToScreen = new Matrix(getFrameToCanvasMatrix());

        for (final BreedingPigFaceDetectTFlite.Recognition result : results) {
            if (result.getLocation() == null) {
                continue;
            }
            final RectF detectionFrameRect = new RectF(result.getLocation());

            final RectF detectionScreenRect = new RectF();
            rgbFrameToScreen.mapRect(detectionScreenRect, detectionFrameRect);

            logger.v(
                    "Result! Frame: " + result.getLocation() + " mapped to screen:" + detectionScreenRect);

            screenRects.add(new Pair<Float, RectF>(result.getConfidence(), detectionScreenRect));

            if (detectionFrameRect.width() < MIN_SIZE || detectionFrameRect.height() < MIN_SIZE) {
                logger.w("Degenerate rectangle! " + detectionFrameRect);
                continue;
            }

            rectsToTrack.add(new Pair<Float, BreedingPigFaceDetectTFlite.Recognition>(result.getConfidence(), result));
        }

        if (rectsToTrack.isEmpty()) {
            logger.v("Nothing to track, aborting.");
            return;
        }

        Log.e("rectsToTrack.size", "rectsToTrack.size"+rectsToTrack.size());

        if (objectTracker == null) {
            trackedObjects.clear();
            for (final Pair<Float, BreedingPigFaceDetectTFlite.Recognition> potential : rectsToTrack) {
                final TrackedRecognition trackedRecognition = new TrackedRecognition();
                trackedRecognition.detectionConfidence = potential.first;
                trackedRecognition.location = new RectF(potential.second.getLocation());
                trackedRecognition.trackedObject = null;
                trackedRecognition.title = potential.second.getTitle();
                //???
                trackedRecognition.color = COLORS[trackedObjects.size()];

                trackedRecognition.points = potential.second.getPoints();
                trackedObjects.add(trackedRecognition);

                if (trackedObjects.size() >= COLORS.length) {
                    break;
                }
            }
            Log.e("trackedObjects.size", "trackedObjects.size="+trackedObjects.size());
            return;
        }

        logger.i("%d rects to track", rectsToTrack.size());
        for (final Pair<Float, BreedingPigFaceDetectTFlite.Recognition> potential : rectsToTrack) {
            handleDetection(originalFrame, timestamp, potential);
        }
    }

    private void handleDetection(
            final byte[] frameCopy, final long timestamp, final Pair<Float, BreedingPigFaceDetectTFlite.Recognition> potential) {
        final ObjectTracker.TrackedObject potentialObject =
                objectTracker.trackObject(potential.second.getLocation(), timestamp, frameCopy);

        final float potentialCorrelation = potentialObject.getCurrentCorrelation();
        logger.v(
                "Tracked object went from %s to %s with correlation %.2f",
                potential.second, potentialObject.getTrackedPositionInPreviewFrame(), potentialCorrelation);

        if (potentialCorrelation < MARGINAL_CORRELATION) {
            logger.v("Correlation too low to begin tracking %s.", potentialObject);
            potentialObject.stopTracking();
            return;
        }

        final List<TrackedRecognition> removeList = new LinkedList<TrackedRecognition>();

        float maxIntersect = 0.0f;

        // This is the current tracked object whose color we will take. If left null we'll take the
        // first one from the color queue.
        TrackedRecognition recogToReplace = null;

        // Look for intersections that will be overridden by this object or an intersection that would
        // prevent this one from being placed.
        for (final TrackedRecognition trackedRecognition : trackedObjects) {
            final RectF a = trackedRecognition.trackedObject.getTrackedPositionInPreviewFrame();
            final RectF b = potentialObject.getTrackedPositionInPreviewFrame();
            final RectF intersection = new RectF();
            final boolean intersects = intersection.setIntersect(a, b);

            final float intersectArea = intersection.width() * intersection.height();
            final float totalArea = a.width() * a.height() + b.width() * b.height() - intersectArea;
            final float intersectOverUnion = intersectArea / totalArea;

            // If there is an intersection with this currently tracked box above the maximum overlap
            // percentage allowed, either the new recognition needs to be dismissed or the old
            // recognition needs to be removed and possibly replaced with the new one.
            if (intersects && intersectOverUnion > MAX_OVERLAP) {
                if (potential.first < trackedRecognition.detectionConfidence
                        && trackedRecognition.trackedObject.getCurrentCorrelation() > MARGINAL_CORRELATION) {
                    // If track for the existing object is still going strong and the detection score was
                    // good, reject this new object.
                    potentialObject.stopTracking();
                    return;
                } else {
                    removeList.add(trackedRecognition);

                    // Let the previously tracked object with max intersection amount donate its color to
                    // the new object.
                    if (intersectOverUnion > maxIntersect) {
                        maxIntersect = intersectOverUnion;
                        recogToReplace = trackedRecognition;
                    }
                }
            }
        }

        // If we're already tracking the max object and no intersections were found to bump off,
        // pick the worst current tracked object to remove, if it's also worse than this candidate
        // object.
        if (availableColors.isEmpty() && removeList.isEmpty()) {
            for (final TrackedRecognition candidate : trackedObjects) {
                if (candidate.detectionConfidence < potential.first) {
                    if (recogToReplace == null
                            || candidate.detectionConfidence < recogToReplace.detectionConfidence) {
                        // Save it so that we use this color for the new object.
                        recogToReplace = candidate;
                    }
                }
            }
            if (recogToReplace != null) {
                logger.v("Found non-intersecting object to remove.");
                removeList.add(recogToReplace);
            } else {
                logger.v("No non-intersecting object found to remove");
            }
        }

        // Remove everything that got intersected.
        for (final TrackedRecognition trackedRecognition : removeList) {
            logger.v(
                    "Removing tracked object %s with detection confidence %.2f, correlation %.2f",
                    trackedRecognition.trackedObject,
                    trackedRecognition.detectionConfidence,
                    trackedRecognition.trackedObject.getCurrentCorrelation());
            trackedRecognition.trackedObject.stopTracking();
            trackedObjects.remove(trackedRecognition);
            if (trackedRecognition != recogToReplace) {
                availableColors.add(trackedRecognition.color);
            }
        }

//        if (recogToReplace == null && availableColors.isEmpty()) {
//            logger.e("No room to track this object, aborting.");
//            potentialObject.stopTracking();
//            return;
//        }

        // Finally safe to say we can track this object.
        logger.v(
                "Tracking object %s (%s) with detection confidence %.2f at position %s",
                potentialObject,
                potential.second.getTitle(),
                potential.first,
                potential.second.getLocation());
        final TrackedRecognition trackedRecognition = new TrackedRecognition();
        trackedRecognition.detectionConfidence = potential.first;
        trackedRecognition.trackedObject = potentialObject;
        trackedRecognition.title = potential.second.getTitle();
        trackedRecognition.points = potential.second.getPoints();

        // Use the color from a replaced object before taking one from the color queue.
        trackedRecognition.color = Color.RED;
//                recogToReplace != null ? recogToReplace.color : availableColors.poll();
        trackedObjects.add(trackedRecognition);
    }

    public synchronized void trackAnimalResults(List<PostureItem> postureItemList) {
        mFrameRects.clear();
        if (postureItemList == null) {
            return;
        }
        int canvasW = ScreenUtil.getScreenWidth();
        int canvasH = ScreenUtil.getScreenHeight();
        Point centerOfCanvas = new Point(canvasW / 2, canvasH / 2);
        int rectW = canvasW / 2;
        int rectH = canvasH / 2;
        int left = centerOfCanvas.x - (rectW);
        int top = centerOfCanvas.y - (rectH / 2);
        int right = centerOfCanvas.x + (rectW);
        int bottom = centerOfCanvas.y + (rectH / 2);
        RectF trackRectF = new RectF(left, top, right, bottom);

        for(int i = 0;i< postureItemList.size();++i){
            TrackerItem item = new TrackerItem(trackRectF);
            mFrameRects.add(item);
        }
    }
}
