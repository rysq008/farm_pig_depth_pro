package com.xiangchuang.risks.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.xiangchuang.risks.model.bean.RecognitionResult;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.Utils;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;
import com.xiangchuangtec.luolu.animalcounter.view.ShowPollingActivity_new;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.utils.ZipUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public final class CounterHelper {

    private static final Map<String, String> mHeaderMap = new HashMap<>();
    private static final String URL_TEST = "http://58.132.169.38:1011/test";

    static {
        mHeaderMap.put("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
    }

    public interface OnImageRecognitionListener {
        void onCompleted(int count, Bitmap bitmap);
    }

    public interface OnUploadResultListener {
        void onCompleted(boolean succeed, String resutl);
    }

    public static void uploadRecognitionResult(String sheId, String sheName, int duration,
                                               List<RecognitionResult> results,
                                               Context context, OnUploadResultListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = FileUtils.createTempDir(context);
                File[] files = new File[results.size()];
                int totalCount = 0;
                int mAutoCount = 0;
                String locationString = "";
                try {
                    JSONArray arrays = new JSONArray();
                    for (RecognitionResult recognitionResult : results) {
                        String fileName = String.format("%s/%d.jpg", path, recognitionResult.index);
                        saveBitmap(recognitionResult.bitmap, fileName);
                        files[recognitionResult.index] = new File(fileName);
                        JSONObject jsonObject = new JSONObject();
                        String[] split = fileName.split("/");
                        String picname = split[split.length - 1];
                        //经度 纬度 猪圈名字 图片名字 当前猪圈数
                        jsonObject.put("lat", recognitionResult.lat);
                        jsonObject.put("lon", recognitionResult.lon);
                        jsonObject.put("name", "猪圈" + (recognitionResult.index + 1));
                        jsonObject.put("picName", picname);
                        jsonObject.put("count", recognitionResult.count);
                        jsonObject.put("autoCount", recognitionResult.autoCount);
                        arrays.put(jsonObject);
                        totalCount += recognitionResult.count;
                        mAutoCount += recognitionResult.autoCount;

                    }
                    JSONObject root = new JSONObject();
                    root.put("pigsty", arrays);
                    locationString = root.toString();
                } catch (JSONException e) {
                    listener.onCompleted(false, "");
                    return;
                }

                File zipFile = new File(path, "out.zip");
                ZipUtil.zipFiles(files, zipFile);
                Map map = new HashMap();
                map.put(Constants.AppKeyAuthorization, "hopen");
                map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, context));

//                String url = "http://47.92.167.61:8081/numberCheck/app/sheCommit";
                Map<String, String> param = new HashMap<>();
                param.put("sheId", sheId);
                param.put("name", sheName);
                param.put("count", "" + totalCount);
                param.put("autoCount", "" + mAutoCount);
                param.put("location", locationString);
                param.put("timeLength", "" + duration);
                param.put("juanCnt", "" + results.size());
                param.put("createuser", "" + PreferencesUtils.getIntValue(Constants.userid, MyApplication.getAppContext()));
                OkHttp3Util.uploadPreFile(Constants.SHECOMMIT, zipFile, "out.zip", param, map, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        listener.onCompleted(false, "");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == 200) {
                            String resutl = response.body().string();
                            listener.onCompleted(true, resutl);
                        } else{
                            listener.onCompleted(false, "");
                        }
                    }
                });
            }
        }).start();
    }

    public static void recognitionFromNet(final Bitmap bitmap, final OnImageRecognitionListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map param = new HashMap();
                param.put("imgBase64", getImgStr(bitmap));
                OkHttp3Util.doPost(URL_TEST, param, mHeaderMap, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        listener.onCompleted(-1, null);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int count = -1;
                        Bitmap resultBitmap = null;
                        try {
                            if (response.code() == 200) {
                                String responseStr = response.body().string();
                                try {
                                    JsonParser parser = new JsonParser();  //创建JSON解析器
                                    JsonObject object = (JsonObject) parser.parse(responseStr);  //创建JsonObject对象

                                    // 点数结果
                                    count = object.get("Num").getAsInt();

                                    JsonArray array = object.get("Box").getAsJsonArray();    //得到为json的数组
                                    //resultBitmap = drawNewBitmap(resultImageFile.getAbsolutePath(), array, String.valueOf(count));

                                    // 给result.jpeg画框
                                    resultBitmap = drawNewBitmap(bitmap, array, String.valueOf(count));
                                } catch (JsonParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } finally {
                            if (response.body() != null)
                                response.body().close();
                            listener.onCompleted(count, resultBitmap);
                        }
                    }
                });
            }
        }).start();
    }

    private static void saveBitmap(final Bitmap bitmap, final String filename) {
        final File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        Matrix matrix = new Matrix();
        matrix.postScale((float) 0.5, (float) 0.5);
        // 得到新的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        try {
            final FileOutputStream out = new FileOutputStream(file);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
            out.flush();
            out.close();
        } catch (final Exception e) {
        }
    }

    public static int number = 1;

    private static Bitmap drawNewBitmap(Bitmap bitmap, JsonArray array, String text) {
        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(copyBitmap);

        Paint rectPaint = new Paint();
        Paint textPaint = new Paint();

        Utils.setAttributes(MyApplication.getAppContext(), rectPaint, textPaint);
        rectPaint.setTextSize(1 / 2);

        Paint pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setAntiAlias(true);
        pointPaint.setAlpha(100);

        FileOutputStream out = null;
        try {
            final double width = canvas.getWidth();
            final double height = canvas.getHeight();

            int tempCount = 0;
            for (int i = 0; i < array.size(); i++) {
                tempCount ++;
                JsonArray xyArray = array.get(i).getAsJsonArray();
//                xyArray.get(0).getAsDouble();
//                xyArray.get(1).getAsDouble();
//                xyArray.get(2).getAsDouble();
//                xyArray.get(3).getAsDouble();

                double x = width * xyArray.get(0).getAsDouble();
                double y = height * xyArray.get(1).getAsDouble();

                double w = width * xyArray.get(2).getAsDouble();
                double h = height * xyArray.get(3).getAsDouble();

                int left = (int) (x - w / 2);
                int top = (int) (y - h / 2);
                int right = (int) (x + w / 2);
                int bottom = (int) (y + h / 2);


                Log.i("xyaywidthheight", width + "," + height);
                Log.i("xyay", xyArray.toString());
                Log.i("xyayleftbottom", left + "," + top + "," + right + "," + bottom);

                //canvas.drawRect(left, top, right, bottom, rectPaint);
                canvas.drawCircle((float)x,(float)y,35,pointPaint);
                textPaint.setColor(Color.YELLOW);
                canvas.drawText(tempCount+"", (float)x,(float)y+12f,textPaint);
            }
//            canvas.drawText("圈" + number, 150,
//                    canvas.getHeight() - 100, textPaint);
//            canvas.drawText(text + "头", canvas.getWidth() - 200,
//                    canvas.getHeight() - 100, textPaint);
            textPaint.setColor(Color.RED);
            canvas.drawText("圈" + number, 80,
                    120, textPaint);
            canvas.drawText("识别"+text + "头", 300,
                    120, textPaint);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return copyBitmap;
    }

    public static Bitmap drawModifierBitmap(Bitmap bitmap, String text) {
        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(copyBitmap);

        Paint textPaint = new Paint();

        float textSize = MyApplication.getAppContext().getResources().getDimensionPixelSize(R.dimen.draw_text_size);
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.RED);

        canvas.drawText("修正为" + text+"头", canvas.getWidth() - 200,
                    120, textPaint);
        return copyBitmap;
    }

    /**
     * 将图片转换成Base64编码
     *
     * @param bitmap 待处理图片
     * @return
     */
    private static String getImgStr(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 图片按比例压缩，以长边=1080为准
        // 图片质量提升为60.
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = 1f;

        int max = Math.max(width, height);
        if(max > 1080) {
            scale = 1080f / max;
        }
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        newbm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = new ByteArrayInputStream(baos.toByteArray());
        byte[] data = null;
        //读取图片字节数组
        try {
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String base64Str = Base64.encodeToString(data, Base64.DEFAULT);
        Log.i("base64Str", base64Str);
        return base64Str;
    }
}
