package com.xiangchuangtec.luolu.animalcounter.netutils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.farm.innovation.biz.login.LoginFamerActivity;
import com.hjq.toast.ToastUtils;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.PigPreferencesUtils;
import com.xiangchuangtec.luolu.animalcounter.PigAppConfig;
import com.xiangchuangtec.luolu.animalcounter.model.Commit;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import innovation.media.DormNextInfoDialog;
import innovation.utils.Toast;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class OkHttp3Util {
    private static OkHttpClient okHttpClient = null;


    private OkHttp3Util() {
    }

    public static OkHttpClient getInstance() {
        if (okHttpClient == null) {
            //加同步安全
            synchronized (OkHttp3Util.class) {
                if (okHttpClient == null) {
                    //okhttp可以缓存数据....指定缓存路径
                    File sdcache = new File(Environment.getExternalStorageDirectory(), "cache");
                    //指定缓存大小
                    int cacheSize = 10 * 1024 * 1024;

                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)//连接超时
                            .writeTimeout(60, TimeUnit.SECONDS)//写入超时
                            .readTimeout(60, TimeUnit.SECONDS)//读取超时
                            .addInterceptor((Interceptor) new CommonParamsInterceptor())//添加的是应用拦截器...公共参数
                            //.addNetworkInterceptor(new CacheInterceptor())//添加的网络拦截器

                            .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize))//设置缓存
                            .build();
                }
            }

        }

        return okHttpClient;
    }

    /**
     * 为HttpGet 的 url 添加多个name value 参数。
     *
     * @param url
     * @param params
     * @return
     */
    public static String attachHttpGetParams(String url, Map params) {

        Iterator<String> keys = params.keySet().iterator();
        Iterator<String> values = params.values().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("?");

        for (int i = 0; i < params.size(); i++) {
            String value = null;
            try {
                value = URLEncoder.encode(values.next(), "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            stringBuffer.append(keys.next() + "=" + value);
            if (i != params.size() - 1) {
                stringBuffer.append("&");
            }
        }

        return url + stringBuffer.toString();
    }

    /**
     * get请求
     * 参数1 url
     * 参数2 回调Callback
     */

    public static void doGet(String url, Callback callback) {

        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //创建Request
        Request request = new Request.Builder().url(url).build();
        //得到Call对象
        Call call = okHttpClient.newCall(request);
        //执行异步请求
        call.enqueue(callback);


    }

    /**
     * post请求
     * 参数1 url
     * 参数2 Map<String, String> params post请求的时候给服务器传的数据
     * add..("","")
     * add()
     */

    public static void doPost(String url, Map<String, String> params, Callback callback) {

        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //3.x版本post请求换成FormBody 封装键值对参数

        FormBody.Builder builder = new FormBody.Builder();
        //遍历集合
        if (null != params && params.size() > 0) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }


        //创建Request
        Request.Builder request = new Request.Builder()
                .url(url)
                .post(builder.build());
        request.addHeader("AppKeyAuthorization", "hopen");
        String type = PigPreferencesUtils.getStringValue(Constants.companyfleg, PigAppConfig.getAppContext());
        if (type.equals("1")) {
            request.addHeader("uid", PigPreferencesUtils.getStringValue(Constants.id, PigAppConfig.getAppContext()));
        } else {
            request.addHeader("uid", PigPreferencesUtils.getIntValue(Constants.en_user_id, PigAppConfig.getAppContext()) + "");
        }
        request.addHeader("type", type);
        request.addHeader("en_id", PigPreferencesUtils.getStringValue(Constants.en_id, PigAppConfig.getAppContext()));

        request.addHeader("longitude", PigPreferencesUtils.getStringValue(Constants.longitude, PigAppConfig.getAppContext()));
        request.addHeader("latitude", PigPreferencesUtils.getStringValue(Constants.latitude, PigAppConfig.getAppContext()));
        //机型
        request.addHeader("phone_model", android.os.Build.MODEL);
        //时间
        request.addHeader("timestamp", SystemClock.currentThreadTimeMillis() + "");
        request.addHeader("token", PigPreferencesUtils.getStringValue(Constants.TOKEN, PigAppConfig.getAppContext()));

        TelephonyManager phone = (TelephonyManager) PigAppConfig.getContext().getSystemService(Context.TELEPHONY_SERVICE);

        //IMEI
        if (ActivityCompat.checkSelfPermission(PigAppConfig.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            request.addHeader("phone_imei", SystemClock.currentThreadTimeMillis() + "");
        } else {
            request.addHeader("phone_imei", phone.getDeviceId() + "");
        }

        //版本
        request.addHeader("version", PigAppConfig.version);
        request.addHeader("Accept-Encoding", "identity");


//        request.addHeader("token", PigPigPreferencesUtils.getIntValue(Constants.token, PigAppConfig.getAppContext())+"");

        Request build = request.build();
        Call call = okHttpClient.newCall(build);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody mresponse = response.peekBody(response.body().contentLength());
                Response response1 = new Response.Builder().request(response.request()).protocol(response.protocol()).code(response.code()).message(response.message()).body(mresponse).build();
                String string = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status == -4) {
                        PigPreferencesUtils.removeAllKey(PigAppConfig.getContext());
                        PigAppConfig.removeALLActivity();
                        ToastUtils.show("登录超时，请重新登录。");
                        Intent addIntent = new Intent(PigAppConfig.getContext(), LoginFamerActivity.class);
                        PigAppConfig.getContext().startActivity(addIntent);
                    }else{
                        callback.onResponse(call, response1);
                    }
                } catch (Exception ignored) {
                    Log.e("ignored", "onResponse: "+ignored.toString() );
                }finally {

                }
            }
        });

    }

    public static void doPost(String url, Map<String, String> params, Map<String, String> headerParams, Callback callback) {
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //3.x版本post请求换成FormBody 封装键值对参数
        FormBody.Builder builder = new FormBody.Builder();
        //遍历集合
        if (null != params && params.size() > 0) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }
        //创建Request
        Request.Builder request = new Request.Builder()
                .url(url)
                .post(builder.build());
        if (null != headerParams && headerParams.size() > 0) {
            for (String key : headerParams.keySet()) {
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(headerParams.get(key))) {
                    request.addHeader(key, headerParams.get(key));
                }
            }
        }

        request.addHeader("AppKeyAuthorization", "hopen");
        String type = PigPreferencesUtils.getStringValue(Constants.companyfleg, PigAppConfig.getAppContext());
        if (type.equals("1")) {
            request.addHeader("uid", PigPreferencesUtils.getStringValue(Constants.id, PigAppConfig.getAppContext()));
        } else {
            request.addHeader("uid", PigPreferencesUtils.getIntValue(Constants.en_user_id, PigAppConfig.getAppContext()) + "");
        }
        request.addHeader("en_id", PigPreferencesUtils.getStringValue(Constants.en_id, PigAppConfig.getAppContext()));
        request.addHeader("longitude", PigPreferencesUtils.getStringValue(Constants.longitude, PigAppConfig.getAppContext()));
        request.addHeader("latitude", PigPreferencesUtils.getStringValue(Constants.latitude, PigAppConfig.getAppContext()));
        //        request.addHeader("token", PigPigPreferencesUtils.getIntValue(Constants.token, PigAppConfig.getAppContext())+"");
        //机型
        request.addHeader("phone_model", android.os.Build.MODEL);
        //时间
        request.addHeader("timestamp", SystemClock.currentThreadTimeMillis() + "");
        request.addHeader("token", PigPreferencesUtils.getStringValue(Constants.TOKEN, PigAppConfig.getAppContext()));
        TelephonyManager phone = (TelephonyManager) PigAppConfig.getContext().getSystemService(Context.TELEPHONY_SERVICE);

        //IMEI
        if (ActivityCompat.checkSelfPermission(PigAppConfig.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            request.addHeader("phone_imei", "");
        } else {
            request.addHeader("phone_imei", phone.getDeviceId() + "");
        }

        //版本
        request.addHeader("version", PigAppConfig.version);
        request.addHeader("Accept-Encoding", "identity");

        Request build = request.build();
        Call call = okHttpClient.newCall(build);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                ResponseBody mresponse = response.peekBody(response.body().contentLength());
                Response response1 = new Response.Builder().request(response.request()).protocol(response.protocol()).code(response.code()).message(response.message()).body(mresponse).build();
                String string = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status == -4) {
                        PigPreferencesUtils.removeAllKey(PigAppConfig.getContext());
                        PigAppConfig.removeALLActivity();
                        ToastUtils.show("登录超时，请重新登录。");
                        Intent addIntent = new Intent(PigAppConfig.getContext(), LoginFamerActivity.class);
                        PigAppConfig.getContext().startActivity(addIntent);
                    }else{
                        callback.onResponse(call, response1);
                    }
                } catch (Exception ignored) {

                }
            }
        });

    }

    /**
     * 点数/估重专用接口
     * @param url
     * @param params
     * @param headerParams
     * @param callback
     */
    public static void doPostForWan(String url, Map<String, String> params, Map<String, String> headerParams, Callback callback) {
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //3.x版本post请求换成FormBody 封装键值对参数
        FormBody.Builder builder = new FormBody.Builder();
        //遍历集合
        if (null != params && params.size() > 0) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }
        //创建Request
        Request.Builder request = new Request.Builder()
                .url(url)
                .post(builder.build());
        if (null != headerParams && headerParams.size() > 0) {
            for (String key : headerParams.keySet()) {
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(headerParams.get(key))) {
                    request.addHeader(key, headerParams.get(key));
                }
            }
        }

        request.addHeader("AppKeyAuthorization", "hopen");
        String type = PigPreferencesUtils.getStringValue(Constants.companyfleg, PigAppConfig.getAppContext());
        if (type.equals("1")) {
            request.addHeader("uid", PigPreferencesUtils.getStringValue(Constants.id, PigAppConfig.getAppContext()));
        } else {
            request.addHeader("uid", PigPreferencesUtils.getIntValue(Constants.en_user_id, PigAppConfig.getAppContext()) + "");
        }
        request.addHeader("en_id", PigPreferencesUtils.getStringValue(Constants.en_id, PigAppConfig.getAppContext()));
        request.addHeader("longitude", PigPreferencesUtils.getStringValue(Constants.longitude, PigAppConfig.getAppContext()));
        request.addHeader("latitude", PigPreferencesUtils.getStringValue(Constants.latitude, PigAppConfig.getAppContext()));
        //        request.addHeader("token", PigPigPreferencesUtils.getIntValue(Constants.token, PigAppConfig.getAppContext())+"");
        //机型
        request.addHeader("phone_model", android.os.Build.MODEL);
        //时间
        request.addHeader("timestamp", SystemClock.currentThreadTimeMillis() + "");
        request.addHeader("token", PigPreferencesUtils.getStringValue(Constants.TOKEN, PigAppConfig.getAppContext()));
        TelephonyManager phone = (TelephonyManager) PigAppConfig.getContext().getSystemService(Context.TELEPHONY_SERVICE);

        //IMEI
        if (ActivityCompat.checkSelfPermission(PigAppConfig.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            request.addHeader("phone_imei", "");
        } else {
            request.addHeader("phone_imei", phone.getDeviceId() + "");
        }

        //版本
        request.addHeader("version", PigAppConfig.version);
        Request build = request.build();
        Call call = okHttpClient.newCall(build);
        call.enqueue(callback);

    }



    public static void uploadPreFile(String url, File[] files, String[] partNames, Map<String, String> params, Map<String, String> headerParams, Callback callback) {
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        okHttpClient.newBuilder()
                .connectTimeout(130, TimeUnit.SECONDS)//连接时长
                .writeTimeout(160, TimeUnit.SECONDS)//写入时长
                .readTimeout(160, TimeUnit.SECONDS);//读取时长

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        //参数
        if (params != null) {
            for (String key : params.keySet()) {
                builder.addFormDataPart(key, params.get(key));
            }
        }
        //文件...参数name指的是请求路径中所接受的参数...如果路径接收参数键值是file,此处应该改变
        for (int i = 0; i < files.length; i++) {
            builder.addFormDataPart(partNames[i], files[i].getName(), RequestBody.create(MediaType.parse("application/octet-stream"), files[i]));
        }

        //构建
        //MultipartBody multipartBody = builder.build();
        RequestBody multipartBody = builder.build();

        //创建Request
       /* Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();*/
        Request.Builder request = new Request.Builder()
                .url(url)
                .post(multipartBody);
        if (null != headerParams && headerParams.size() > 0) {
            for (String key : headerParams.keySet()) {
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(headerParams.get(key))) {
                    request.addHeader(key, headerParams.get(key));
                }
            }
        }

        request.addHeader("AppKeyAuthorization", "hopen");
        String type = PigPreferencesUtils.getStringValue(Constants.companyfleg, PigAppConfig.getAppContext());
        if (type.equals("1")) {
            request.addHeader("uid", PigPreferencesUtils.getStringValue(Constants.id, PigAppConfig.getAppContext()));
        } else {
            request.addHeader("uid", PigPreferencesUtils.getIntValue(Constants.en_user_id, PigAppConfig.getAppContext()) + "");
        }
        request.addHeader("en_id", PigPreferencesUtils.getStringValue(Constants.en_id, PigAppConfig.getAppContext()));
        request.addHeader("longitude", PigPreferencesUtils.getStringValue(Constants.longitude, PigAppConfig.getAppContext()));
        request.addHeader("latitude", PigPreferencesUtils.getStringValue(Constants.latitude, PigAppConfig.getAppContext()));
        //机型
        request.addHeader("phone_model", android.os.Build.MODEL);
        //时间
        request.addHeader("timestamp", SystemClock.currentThreadTimeMillis() + "");
        request.addHeader("token", PigPreferencesUtils.getStringValue(Constants.TOKEN, PigAppConfig.getAppContext()));
        TelephonyManager phone = (TelephonyManager) PigAppConfig.getContext().getSystemService(Context.TELEPHONY_SERVICE);

        //IMEI
        if (ActivityCompat.checkSelfPermission(PigAppConfig.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            request.addHeader("phone_imei", SystemClock.currentThreadTimeMillis() + "");
        } else {
            request.addHeader("phone_imei", phone.getDeviceId() + "");
        }

        //版本
        request.addHeader("version", PigAppConfig.version);

        request.addHeader("Accept-Encoding", "identity");
        //得到Call
        Call call = okHttpClient.newCall(request.build());


        try {
            //执行请求
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(call, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    ResponseBody mresponse = response.peekBody(response.body().contentLength());
                    Response response1 = new Response.Builder().request(response.request()).protocol(response.protocol()).code(response.code()).message(response.message()).body(mresponse).build();
                    String string = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(string);
                        int status = jsonObject.getInt("status");
                        String msg = jsonObject.getString("msg");
                        if (status == -4) {
                            PigPreferencesUtils.removeAllKey(PigAppConfig.getContext());
                            PigAppConfig.removeALLActivity();
                            ToastUtils.show("登录超时，请重新登录。");
                            Intent addIntent = new Intent(PigAppConfig.getContext(), LoginFamerActivity.class);
                            PigAppConfig.getContext().startActivity(addIntent);
                        }else{
                            callback.onResponse(call, response1);
                        }
                    } catch (Exception ignored) {

                    }
                }
            });
        } catch (Exception e) {
            Log.e("uploadPreFileException", "uploadPreFile: " + e.toString());
        }
    }

    public static void uploadPreFile(String url, File file, String fileName, Map<String, String> params, Map<String, String> headerParams, Callback callback) {
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        okHttpClient.newBuilder()
                .connectTimeout(130, TimeUnit.SECONDS)//连接时长
                .writeTimeout(160, TimeUnit.SECONDS)//写入时长
                .readTimeout(160, TimeUnit.SECONDS);//读取时长

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        //参数
        if (params != null) {
            for (String key : params.keySet()) {
                builder.addFormDataPart(key, params.get(key));
            }
        }
        //文件...参数name指的是请求路径中所接受的参数...如果路径接收参数键值是file,此处应该改变
        builder.addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("application/octet-stream"), file));

        //构建
        //MultipartBody multipartBody = builder.build();
        RequestBody multipartBody = builder.build();

        //创建Request
       /* Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();*/
        Request.Builder request = new Request.Builder()
                .url(url)
                .post(multipartBody);
        if (null != headerParams && headerParams.size() > 0) {
            for (String key : headerParams.keySet()) {
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(headerParams.get(key))) {
                    request.addHeader(key, headerParams.get(key));
                }
            }
        }

        request.addHeader("AppKeyAuthorization", "hopen");
        String type = PigPreferencesUtils.getStringValue(Constants.companyfleg, PigAppConfig.getAppContext());
        if (type.equals("1")) {
            request.addHeader("uid", PigPreferencesUtils.getStringValue(Constants.id, PigAppConfig.getAppContext()));
        } else {
            request.addHeader("uid", PigPreferencesUtils.getIntValue(Constants.en_user_id, PigAppConfig.getAppContext()) + "");
        }
        request.addHeader("en_id", PigPreferencesUtils.getStringValue(Constants.en_id, PigAppConfig.getAppContext()));
        request.addHeader("longitude", PigPreferencesUtils.getStringValue(Constants.longitude, PigAppConfig.getAppContext()));
        request.addHeader("latitude", PigPreferencesUtils.getStringValue(Constants.latitude, PigAppConfig.getAppContext()));
        //机型
        request.addHeader("phone_model", android.os.Build.MODEL);
        //时间
        request.addHeader("timestamp", SystemClock.currentThreadTimeMillis() + "");
        request.addHeader("token", PigPreferencesUtils.getStringValue(Constants.TOKEN, PigAppConfig.getAppContext()));
        TelephonyManager phone = (TelephonyManager) PigAppConfig.getContext().getSystemService(Context.TELEPHONY_SERVICE);

        //IMEI
        if (ActivityCompat.checkSelfPermission(PigAppConfig.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            request.addHeader("phone_imei", SystemClock.currentThreadTimeMillis() + "");
        } else {
            request.addHeader("phone_imei", phone.getDeviceId() + "");
        }
        //版本
        request.addHeader("version", PigAppConfig.version);
        request.addHeader("Accept-Encoding", "identity");
        //得到Call
        Call call = okHttpClient.newCall(request.build());

        try {
            //执行请求
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(call, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    ResponseBody mresponse = response.peekBody(response.body().contentLength());
                    Response response1 = new Response.Builder().request(response.request()).protocol(response.protocol()).code(response.code()).message(response.message()).body(mresponse).build();
                    String string = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(string);
                        int status = jsonObject.getInt("status");
                        String msg = jsonObject.getString("msg");
                        if (status == -4) {
                            PigPreferencesUtils.removeAllKey(PigAppConfig.getContext());
                            PigAppConfig.removeALLActivity();
                            ToastUtils.show("登录超时，请重新登录。");
                            Intent addIntent = new Intent(PigAppConfig.getContext(), LoginFamerActivity.class);
                            PigAppConfig.getContext().startActivity(addIntent);
                        }else{
                            callback.onResponse(call, response1);
                        }
                    } catch (Exception ignored) {

                    }
                }
            });
        } catch (Exception e) {
            Log.e("uploadPreFileException", "uploadPreFile: " + e.toString());
        }


    }

    /**
     * post请求上传文件....包括图片....流的形式传任意文件...
     * 参数1 url
     * file表示上传的文件
     * fileName....文件的名字,,例如aaa.jpg
     * params ....传递除了file文件 其他的参数放到map集合
     */
    public static void uploadFile(Context context, String url, File file, String fileName, Map<String, String> params, String numshoudongvalue, DormNextInfoDialog dormNextInfoDialog) {
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        //参数
        if (params != null) {
            for (String key : params.keySet()) {
                builder.addFormDataPart(key, params.get(key));
            }
        }
        //文件...参数name指的是请求路径中所接受的参数...如果路径接收参数键值是fileeeee,此处应该改变
        builder.addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("application/octet-stream"), file));

        //构建
        //MultipartBody multipartBody = builder.build();
        RequestBody multipartBody = builder.build();

        //创建Request
        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();

        //得到Call
        Call call = okHttpClient.newCall(request);
        //执行请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("upload", e.getLocalizedMessage());
                AVOSCloudUtils.saveErrorMessage(e, OkHttp3Util.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //上传成功回调 目前不需要处理
                if (response.isSuccessful()) {
                    String s = response.body().string();
                    Log.e("upload", "上传--" + s);
                    Activity appContext = (Activity) context;
                    appContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Commit bean = GsonUtils.getBean(s, Commit.class);
                            if (null != bean && bean.getStatus() == 1) {
                                Toast.makeText(PigAppConfig.getAppContext(), "保存成功", Toast.LENGTH_LONG).show();
                                PigPreferencesUtils.saveKeyValue(Constants.manualcount, numshoudongvalue, PigAppConfig.getAppContext());
                            }
                            dormNextInfoDialog.dismiss();
                        }
                    });
                }
            }
        });

    }

    public static void uploadFile(String url, File file, String fileName, Map<String, String> params, Callback callback) {
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        //参数
        if (params != null) {
            for (String key : params.keySet()) {
                builder.addFormDataPart(key, params.get(key));
            }
        }
        //文件...参数name指的是请求路径中所接受的参数...如果路径接收参数键值是fileeeee,此处应该改变
        builder.addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("application/octet-stream"), file));

        //构建
        //MultipartBody multipartBody = builder.build();
        RequestBody multipartBody = builder.build();

        //创建Request
       /* Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();*/
        Request.Builder request = new Request.Builder()
                .url(url)
                .post(multipartBody);
        //得到Call
        Call call = okHttpClient.newCall(request.build());
        //执行请求
        call.enqueue(callback);
    }


    /**
     * Post请求发送JSON数据....{"name":"zhangsan","pwd":"123456"}
     * 参数一：请求Url
     * 参数二：请求的JSON
     * 参数三：请求回调
     */
    public static void doPostJson(String url, String jsonParams, Callback callback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Call call = getInstance().newCall(request);
        call.enqueue(callback);

    }

    /**
     * 下载文件 以流的形式把apk写入的指定文件 得到file后进行安装
     * 参数er：请求Url
     * 参数san：保存文件的文件夹....download
     */
    public static void download(final Activity context, final String url, final String saveDir) {
        Request request = new Request.Builder().url(url).build();
        Call call = getInstance().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //com.orhanobut.logger.Logger.e(e.getLocalizedMessage());
                AVOSCloudUtils.saveErrorMessage(e, OkHttp3Util.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();//以字节流的形式拿回响应实体内容
                    //apk保存路径
                    final String fileDir = isExistDir(saveDir);
                    //文件
                    File file = new File(fileDir, getNameFromUrl(url));

                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }

                    fos.flush();

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "下载成功:" + fileDir + "," + getNameFromUrl(url), Toast.LENGTH_SHORT).show();
                        }
                    });

                    //apk下载完成后 调用系统的安装方法
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    context.startActivity(intent);


                } catch (IOException e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e, OkHttp3Util.class.getSimpleName());
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }


                }
            }
        });

    }

    /**
     * 判断下载目录是否存在......并返回绝对路径
     *
     * @param saveDir
     * @return
     * @throws IOException
     */
    public static String isExistDir(String saveDir) throws IOException {
        // 下载位置
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File downloadFile = new File(Environment.getExternalStorageDirectory(), saveDir);
            if (!downloadFile.mkdirs()) {
                downloadFile.createNewFile();
            }
            String savePath = downloadFile.getAbsolutePath();
            Log.e("savePath", savePath);
            return savePath;
        }
        return null;
    }

    /**
     * @param url
     * @return 从下载连接中解析出文件名
     */
    private static String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 公共参数拦截器
     */
    private static class CommonParamsInterceptor implements Interceptor {

        //拦截的方法
        @Override
        public Response intercept(Chain chain) throws IOException {

            //获取到请求
            Request request = chain.request();
            //获取请求的方式
            String method = request.method();
            //获取请求的路径
            String oldUrl = request.url().toString();

//            Logger.e("---拦截器"+oldUrl + "---" + method + "--" + request.header("User-agent") + "--" + bodyToString(request));

            //要添加的公共参数...map
            Map<String, String> map = new HashMap<>();
            map.put("source", "android");

            if ("GET".equals(method)) {
                // 1.http://www.baoidu.com/login                --------？ key=value&key=value
                // 2.http://www.baoidu.com/login?               --------- key=value&key=value
                // 3.http://www.baoidu.com/login?mobile=11111    -----&key=value&key=value

                StringBuilder stringBuilder = new StringBuilder();//创建一个stringBuilder

                stringBuilder.append(oldUrl);

                if (oldUrl.contains("?")) {
                    //?在最后面....2类型
                    if (oldUrl.indexOf("?") == oldUrl.length() - 1) {

                    } else {
                        //3类型...拼接上&
                        stringBuilder.append("&");
                    }
                } else {
                    //不包含? 属于1类型,,,先拼接上?号
                    stringBuilder.append("?");
                }

                //添加公共参数....
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    //拼接
                    stringBuilder.append(entry.getKey())
                            .append("=")
                            .append(entry.getValue())
                            .append("&");
                }

                //删掉最后一个&符号
                if (stringBuilder.indexOf("&") != -1) {
                    stringBuilder.deleteCharAt(stringBuilder.lastIndexOf("&"));
                }

                String newUrl = stringBuilder.toString();//新的路径

                //拿着新的路径重新构建请求
                request = request.newBuilder()
                        .url(newUrl)
                        .build();


            } else if ("POST".equals(method)) {
                //先获取到老的请求的实体内容
                RequestBody oldRequestBody = request.body();//....之前的请求参数,,,需要放到新的请求实体内容中去

                //如果请求调用的是上面doPost方法
                if (oldRequestBody instanceof FormBody) {
                    FormBody oldBody = (FormBody) oldRequestBody;

                    //构建一个新的请求实体内容
                    FormBody.Builder builder = new FormBody.Builder();
                    //1.添加老的参数
                    for (int i = 0; i < oldBody.size(); i++) {
                        builder.add(oldBody.name(i), oldBody.value(i));
                    }
                    //2.添加公共参数
                    for (Map.Entry<String, String> entry : map.entrySet()) {

                        builder.add(entry.getKey(), entry.getValue());
                    }

                    FormBody newBody = builder.build();//新的请求实体内容

                    //构建一个新的请求
                    request = request.newBuilder()
                            .url(oldUrl)
                            .post(newBody)
                            .build();
                }


            }


            Response response = chain.proceed(request);

            return response;
        }
    }


    private static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            if (copy != null && copy.body() != null) {
                copy.body().writeTo(buffer);
                return buffer.readUtf8();
            }
        } catch (final IOException e) {
            return "something error when show requestBody.";
        }
        return "something error when show requestBody.";
    }

    /**
     * 网络缓存的拦截器......注意在这里更改cache-control头是很危险的,一般客户端不进行更改,,,,服务器端直接指定
     * <p>
     * 没网络取缓存的时候,一般都是在数据库或者sharedPerfernce中取出来的
     */
    private static class CacheInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            //老的响应
            Response oldResponse = chain.proceed(chain.request());

            if (NetUtils.isConnected(PigAppConfig.getAppContext())) {
                int maxAge = 120; // 在线缓存在2分钟内可读取

                return oldResponse.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                int maxStale = 60 * 60 * 24 * 14; // 离线时缓存保存2周
                return oldResponse.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }

        }
    }
}
