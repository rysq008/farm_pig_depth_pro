package com.farm.innovation.utils;

import android.content.Context;
import android.util.Log;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.VideoUpLoadBean;
import com.farm.innvocation.upload.OkHttpRequestUtils;
import com.farm.mainaer.wjoklib.okhttp.upload.UploadManager;
import com.farm.mainaer.wjoklib.okhttp.upload.UploadTask;
import com.farm.mainaer.wjoklib.okhttp.upload.UploadTaskListener;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.farm.innovation.utils.HttpUtils.BUILD_UPLOAD_OFFLINE_URL;

/**
 * Created by devlop on 2018/8/8.
 */

public class UploadUtils {
    private static OkHttpClient mOkHttpClient;

    private static OkHttpClient getOkHttpClient(int timeOut) throws Exception {
        mOkHttpClient = new OkHttpClient();//读取时长
        OkHttpClient.Builder builder = mOkHttpClient.newBuilder();
        builder.connectTimeout(timeOut, TimeUnit.SECONDS)//连接时长
                .writeTimeout(timeOut, TimeUnit.SECONDS)//写入时长
                .readTimeout(timeOut, TimeUnit.SECONDS);//读取时长
        return builder.build();
    }

    /**
     * 获得Request实例
     *
     * @param fileNames 完整的文件路径
     * @return
     */
    private static Request getRequest(String MD5, String userId, List<String> fileNames, String batchId) {
        Request.Builder builder = new Request.Builder();
        builder.addHeader("AppKeyAuthorization", "hopen");
        builder.addHeader("animalType", String.valueOf(PreferencesUtils.getAnimalType(FarmAppConfig.getApplication())));
        builder.url(BUILD_UPLOAD_OFFLINE_URL)//BUILD_UPLOAD_OFFLINE_URL
                .post(getRequestBody(MD5, userId, fileNames, batchId));
        return builder.build();
    }

    /**
     * 通过上传的文件的完整路径生成RequestBody
     *
     * @param fileNames 完整的文件路径
     * @return
     */
    private static RequestBody getRequestBody(String MD5, String userId, List<String> fileNames, String batchId) {
        //创建MultipartBody.Builder，用于添加请求的数据
        MultipartBody.Builder builder = initOKHttpParas();
        builder.addFormDataPart("userId", userId);
        for (int i = 0; i < fileNames.size(); i++) { //对文件进行遍历
            File file = new File(fileNames.get(i)); //生成文件
            //根据文件的后缀名，获得文件类型
            String fileType = getMimeType(file.getName());
            builder.addFormDataPart("md5", MD5);
            builder.addFormDataPart("batchId", batchId);
            builder.addFormDataPart( //给Builder添加上传的文件
                    "file",  //请求的名字
                    file.getName(), //文件的文字，服务器端用来解析的
                    RequestBody.create(MediaType.parse(fileType), file) //创建RequestBody，把上传的文件放入
            );
        }
        return builder.build(); //根据Builder创建请求
    }

    public static String getMimeType(String fileName) {
        return "application/octet-stream";
    }

    /**
     * 根据url，发送异步Post请求
     *
     * @param fileNames 完整的上传的文件的路径名
     * @param callback  OkHttp的回调接口
     */
    private static void upLoadFile(String MD5, String userId, List<String> fileNames, String batchId, Callback callback) {
        OkHttpClient okHttpClient = null;
        try {
            okHttpClient = getOkHttpClient(100);
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, UploadUtils.class.getSimpleName());
        }
        Call call = okHttpClient.newCall(getRequest(MD5, userId, fileNames, batchId));
        call.enqueue(callback);
    }

    public static MultipartBody.Builder initOKHttpParas() {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
    }

    /**
     * 作用：上传
     *
     * @author pimian
     * create at 2018/5/22 14:34
     */
    public static void upload(String file, String md5, String userId, String batchId, Callback callback) {
        List<String> files = new ArrayList<>();
        files.add(file);

        upLoadFile(md5, userId, files, batchId, callback);
    }

    public static Map makeBean2Map(VideoUpLoadBean bean) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("libNum", bean.libNub);//保单号
        params.put("userId", bean.userId);//用户id
        params.put("libEnvinfo", bean.libEnvinfo);//设备信息
        params.put("animalType", bean.animalType);//牲畜类型
        params.put("collectTimes", bean.collectTimes);//
        params.put("timesFlag", bean.timesFlag);//
        params.put("collectTime", bean.collectTime);//
        return params;
    }

    public static void uploadFile(Context context, UploadTaskListener uploadTaskListener, List<VideoUpLoadBean> datas) {
        for (VideoUpLoadBean bean : datas) {
            File file = new File(bean.videoFilePath);
            if (!file.exists()) {
                continue;
            }
            Map<String, String> params = new HashMap<String, String>();
            params.put("filename", file.getName());
            params.put("timesFlag", bean.timesFlag);
            Log.e("uploadFile", "断点开始....");
            OkHttpRequestUtils.getInstance(context).OkHttpGetStringRequest(HttpUtils.UPLOAD_URL + "check", params, new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {

                }

                @Override
                public void onResponse(String response, int id) {
                    Log.i("ckeck", response);
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response);
//                        Toast.makeText(context, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
                        if (jsonObject.optInt("status") != 1) return;
                        final int chunk = Integer.valueOf(jsonObject.get("data").toString());
                        params.putAll(makeBean2Map(bean));
                        UploadTask task = new UploadTask.Builder().setT(bean).setParams(params).setId(bean.timesFlag).setUrl(HttpUtils.UPLOAD_URL)
                                .setChunck(chunk).setFileName(bean.videoFilePath).setListener(uploadTaskListener).build();
                        UploadManager.getInstance().addUploadTask(task);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AVOSCloudUtils.saveErrorMessage(e, UploadUtils.class.getSimpleName());
                    }
                }
            });
        }
    }
}
