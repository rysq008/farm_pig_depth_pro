package innovation.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.innovation.pig.insurance.netutils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import innovation.entry.AddPigObject;
import innovation.entry.BuildObject;
import innovation.entry.MultiBaodanBean;
import innovation.entry.NewBuildObject;
import innovation.entry.NewBuildResultObject;
import innovation.entry.PayObject;
import innovation.entry.UpdateBean;
import innovation.entry.UploadImageObject;
import innovation.entry.UserRegisterBean;
import innovation.entry.VerifyCodeBean;
import innovation.entry.VerifyObject;
import innovation.entry.baodanBean;
import innovation.location.LocationManager;
import innovation.login.Utils;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static innovation.entry.InnApplication.ANIMAL_TYPE;


/**
 * Author by luolu, Date on 2018/8/15.
 * COMPANY：InnovationAI
 */

public class HttpUtils {

    private static String TAG = "HttpUtils";
    // TODO: 2018/8/17 By:LuoLu
//    60.205.209.245:8081;   47.92.167.61:8081
    //  http://47.92.167.61:8081/nongxian2/
    //  TODO: 备注查看监控信息的链接地址 需要修改
    public static String baseUrl = "http://47.92.167.61:8081/nongxian2/";
    //新增投保保单接口
    public static String INSUR_NEW_URL = baseUrl + "baodan/addApp";
    //新增投保保单接口
    public static String INSUR_UPDATE_URL = baseUrl + "baodan/updateApp";
    //投保保单查询接口
    public static String INSUR_QUERY_URL = baseUrl + "baodan/detailApp";
    //投保保单查询接口(根据临时保单号、姓名、身份证号查询)
    public static String INSUR_DETAIL_QUERY_URL = baseUrl + "baodan/detailQueryApp";
    //新增牲畜信息接口
    public static String INSUR_ADDPIG_URL = baseUrl + "pigInfo/addApp";
    //zip文件上传接口
    public static String PIG_FILEUPLOAD = baseUrl + "pigApp/upload";
    //图片上传接口
    public static String PIG_IMAGEUPLOAD = baseUrl + "pigApp/uploadImage";
    //    //投保建库
//    public static String PIG_BUILD = baseUrl + "pigApp/build";
    //建库
    public static String PIG_BUILD = baseUrl + "pigApp/cow";
    //模型查询结果
    public static String PIG_BUILD_RESULT = baseUrl + "pigApp/queryResult";
    //新增理赔
    public static String LIPEI_NEW_URL = baseUrl + "pigLipei/addApp";
    //理赔验证
    public static String PIG_VERIFY = baseUrl + "pigApp/recognition";
    //短信验证码接口
    public static String GET_SMSCODE_URL = baseUrl + "app/sendcode";
    //注册接口
    public static String GET_REGISTER_URL = baseUrl + "app/register";

    //更新接口
    public static String GET_UPDATE_URL = baseUrl + "app/appVersion/zxupdate";

    public static void resetIp(String baseUrl) {
        com.farm.innovation.utils.HttpUtils.resetIp(baseUrl);
        HttpUtils.baseUrl = baseUrl;
        Constants.resetBaseIp(baseUrl);
        //新增投保保单接口
        INSUR_NEW_URL = baseUrl + "baodan/addApp";
        //新增投保保单接口
        INSUR_UPDATE_URL = baseUrl + "baodan/updateApp";
        //投保保单查询接口
        INSUR_QUERY_URL = baseUrl + "baodan/detailApp";
        //投保保单查询接口(根据临时保单号、姓名、身份证号查询)
        INSUR_DETAIL_QUERY_URL = baseUrl + "baodan/detailQueryApp";
        //新增牲畜信息接口
        INSUR_ADDPIG_URL = baseUrl + "pigInfo/addApp";
        //zip文件上传接口
        PIG_FILEUPLOAD = baseUrl + "pigApp/upload";
        //图片上传接口
        PIG_IMAGEUPLOAD = baseUrl + "pigApp/uploadImage";
        //    //投保建库
//    PIG_BUILD = baseUrl + "pigApp/build";
        //建库
        PIG_BUILD = baseUrl + "pigApp/cow";
        //模型查询结果
        PIG_BUILD_RESULT = baseUrl + "pigApp/queryResult";
        //新增理赔
        LIPEI_NEW_URL = baseUrl + "pigLipei/addApp";
        //理赔验证
        PIG_VERIFY = baseUrl + "pigApp/recognition";
        //短信验证码接口
        GET_SMSCODE_URL = baseUrl + "app/sendcode";
        //注册接口
        GET_REGISTER_URL = baseUrl + "app/register";
        //更新接口
        GET_UPDATE_URL = baseUrl + "app/appVersion/zxupdate";
    }

    public static boolean isOfficialHost() {
        return "http://60.205.209.245:8081/nongxian2/".equals(HttpUtils.baseUrl);
    }

    /**
     * @param url
     * @param body
     * @return response
     * @throws IOException
     */
    public static String post(String url, RequestBody body) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("AppKeyAuthorization", "hopen")
                .header("Content-Type", "application/x-www-form-urlencoded")
                // TODO: 2018/8/16 By:LuoLu  添加请求头 animalType Global.ANIMAL_TYPE)  String.valueOf(Global.ANIMAL_TYPE)
                .header("animalType", String.valueOf(ANIMAL_TYPE))
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newBuilder()
                .connectTimeout(200, TimeUnit.SECONDS)//连接时长
                .writeTimeout(200, TimeUnit.SECONDS)//写入时长
                .readTimeout(200, TimeUnit.SECONDS);//读取时长
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("AppKeyAuthorization", "hopen")
                .header("Content-Type", "application/x-www-form-urlencoded")
                // TODO: 2018/8/16 By:LuoLu  添加请求头 animalType
                .header("animalType", String.valueOf(ANIMAL_TYPE))
                .build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * @param resp
     */
    public static HttpRespObject processResp_insurInfo(String resp, String url) {
        HttpRespObject respObj;
        try {
            if (HttpUtils.INSUR_NEW_URL.equalsIgnoreCase(url)) {
                respObj = new baodanBean();
            } else if (HttpUtils.INSUR_UPDATE_URL.equalsIgnoreCase(url)) {
                respObj = new baodanBean();
            } else if (HttpUtils.INSUR_QUERY_URL.equalsIgnoreCase(url)) {
                respObj = new baodanBean();
            } else if (HttpUtils.INSUR_ADDPIG_URL.equalsIgnoreCase(url)) {
                respObj = new AddPigObject();
            } else if (HttpUtils.GET_SMSCODE_URL.equalsIgnoreCase(url)) {
                respObj = new VerifyCodeBean();
            } else if (HttpUtils.GET_REGISTER_URL.equalsIgnoreCase(url)) {
                respObj = new UserRegisterBean();
            }
//            else if (HttpUtils.PIC_LOGIN_URL.equalsIgnoreCase(url)) {
//                respObj = new UserRegisterBean();
//            }
            else if (HttpUtils.LIPEI_NEW_URL.equalsIgnoreCase(url)) {
                respObj = new PayObject();
            } else {
                return null;
            }

            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            String tmpdata = json.getString("data");
            if (!TextUtils.isEmpty(tmpdata)) {
                respObj.data = json.getJSONObject("data");
                respObj.setdata(respObj.data);
            }
            return respObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static UploadObject processResp_upload(String resp) {
        UploadObject respObj;
        try {
            respObj = new UploadObject();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            respObj.data = json.getJSONObject("data");
            respObj.setdata(respObj.data);
            return respObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static UpdateBean processResp_update(String resp) {
        UpdateBean respObj;
        try {
            respObj = new UpdateBean();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            respObj.data = json.getString("data");
            respObj.setdata(respObj.data);


            return respObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static UploadImageObject processResp_upload_image(String resp) {
        UploadImageObject respObj;
        try {
            respObj = new UploadImageObject();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            respObj.data = json.getJSONObject("data");
            respObj.setdata(respObj.data);
            return respObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static final int TYPE_PIC = 1;
    public static final int TYPE_VIDEO = 2;

    public static UploadObject uploadImages(Context context, int model, File zipFile, int uid, String libNum) {
        return uploadRes(context, model, 0, TYPE_PIC, null, zipFile, uid, libNum);
    }

    public static UploadObject uploadVideo(Context context, int model, int lib_id, File videoZipFile, int uid, String libNum) {
        return uploadRes(context, model, lib_id, TYPE_VIDEO, null, videoZipFile, uid, libNum);
    }

    //文件上传接口
    private static UploadObject uploadRes(Context context, int model, int lib_id, int source, String gps, File file, int uid, String libNum) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put(Utils.UploadNew.USERID, uid + "");
        treeMap.put(Utils.UploadNew.LIB_NUM, libNum);
        treeMap.put(Utils.UploadNew.TYPE, model + "");
        treeMap.put(Utils.UploadNew.LIBD_SOURCE, source + "");
        treeMap.put(Utils.UploadNew.LIB_ENVINFO, getEnvInfo(context, gps));
        if (lib_id != 0)
            treeMap.put(Utils.UploadNew.LIB_ID, lib_id + "");

        Gson gson = new Gson();
        String data = gson.toJson(treeMap);
        // TODO: 2018/8/4
        Set set = treeMap.keySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = treeMap.get(key);
            Log.e("文件上传接口：", "\nkey:" + key + "value:" + value);
        }
        //sLogger.i("data: " + data);
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        FormBody.Builder builder = new FormBody.Builder();
        for (TreeMap.Entry<String, String> entry : treeMap.entrySet()) {
            requestBody.addFormDataPart(entry.getKey(), entry.getValue());
        }
        requestBody.addFormDataPart(Utils.Upload.FILE, file.getName(),
                RequestBody.create(MediaType.parse("application/octet-stream"), file));
        // Create RequestBody
        UploadObject uploadResp = null;
        try {
            String response = HttpUtils.post(PIG_FILEUPLOAD, requestBody.build());
            //sLogger.i("upload res model:%d, source:%d, resp:%s", model, source, response);
            Log.e("文件上传接口：\n", PIG_FILEUPLOAD + "\nresponse==" + response);
            uploadResp = (UploadObject) processResp_upload(response);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return uploadResp;
    }

    //图片上传
    public static UploadImageObject uploadImage(File file, int uid) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put(Utils.UploadNew.USERID, uid + "");

        Gson gson = new Gson();
        String data = gson.toJson(treeMap);
        //sLogger.i("data: " + data);
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        FormBody.Builder builder = new FormBody.Builder();
        for (TreeMap.Entry<String, String> entry : treeMap.entrySet()) {
            requestBody.addFormDataPart(entry.getKey(), entry.getValue());
        }
        requestBody.addFormDataPart(Utils.Upload.FILE, file.getName(),
                RequestBody.create(MediaType.parse("application/octet-stream"), file));
        // Create RequestBody
        UploadImageObject uploadResp = null;
        try {
            String response = HttpUtils.post(PIG_IMAGEUPLOAD, requestBody.build());
            //sLogger.i("upload res model:%d, source:%d, resp:%s", model, source, response);
            Log.e("UploadImage", PIG_IMAGEUPLOAD + "\nresponse==" + response);
            uploadResp = (UploadImageObject) processResp_upload_image(response);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return uploadResp;
    }

    //理赔验证
    public static VerifyObject lipei_verify(int uid, String baodannum, String libid) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put(Utils.UploadNew.USERID, uid + "");
        treeMap.put(Utils.UploadNew.BAODANMUM, baodannum);
        treeMap.put(Utils.UploadNew.LIB_ID, libid);

        FormBody.Builder builder = new FormBody.Builder();
        // Add Params to Builder
        for (TreeMap.Entry<String, String> entry : treeMap.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        // Create RequestBody
        RequestBody formBody = builder.build();
        VerifyObject respObject = null;
        try {
            String response = HttpUtils.post(PIG_VERIFY, formBody);
            Log.e(TAG, PIG_VERIFY + "\nresponse==" + response);
            respObject = (VerifyObject) processResp_verify(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (respObject == null || respObject.status != HttpRespObject.STATUS_OK) {
            int status = respObject == null ? -1 : respObject.status;
            Log.e(TAG, "upload images failed, status: %d" + status);
            return respObject;
        }
        return respObject;
    }


    public static VerifyObject processResp_verify(String resp) {
        VerifyObject respObj;
        try {
            respObj = new VerifyObject();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            respObj.data = json.getJSONObject("data");
            respObj.setdata(respObj.data);
            return respObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BuildObject processResp_build(String resp) {
        BuildObject respObj;
        try {
            respObj = new BuildObject();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            respObj.data = json.getJSONObject("data");
            respObj.setdata(respObj.data);
            return respObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MultiBaodanBean processResp_new_detail_query(String resp) {
        MultiBaodanBean respObj;
        try {
            respObj = new MultiBaodanBean();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            respObj.data = json.getString("data");
            respObj.setdata(respObj.data);
            return respObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NewBuildObject processResp_new_build(String resp) {
        NewBuildObject respObj;
        try {
            respObj = new NewBuildObject();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            respObj.data = json.getString("data");
            respObj.setdata(respObj.data);
            return respObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NewBuildResultObject processResp_new_build_result(String resp) {
        NewBuildResultObject respObj;
        try {

            respObj = new NewBuildResultObject();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            respObj.data = json.getJSONObject("data");


            respObj.setdata(respObj.data);
            return respObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String getEnvInfo(Context context, String gps) {
        JSONObject jo = new JSONObject();
        String imei = DeviceUtil.getImei(context);
        JsonHelper.putString(jo, Utils.Upload.imei, Utils.getMD5(imei));
        if (TextUtils.isEmpty(gps)) {
            gps = LocationManager.getInstance(context).getLocationDetail();
        }
        JsonHelper.putString(jo, Utils.Upload.GPS, gps);
        return jo.toString();
    }

    //投保建库
    public static NewBuildResultObject upload_build(String type, String libId, String libNum, String baodannum, Context context) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put(Utils.UploadNew.TYPE, type);
        treeMap.put(Utils.UploadNew.LIB_ID, libId.trim());
        treeMap.put(Utils.UploadNew.BAODANMUM, baodannum.trim());
        treeMap.put(Utils.UploadNew.LIB_NUM, libNum.trim());
        FormBody.Builder builder = new FormBody.Builder();
        // Add Params to Builder
        for (TreeMap.Entry<String, String> entry : treeMap.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        Set set = treeMap.keySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = treeMap.get(key);
            Log.e("投保建库、理赔验证、已理赔查询接口", "key:" + key + "\nvalue:" + value);
        }
        // Create RequestBody
        RequestBody formBody = builder.build();
        NewBuildResultObject respObject = null;
        try {
            String response = HttpUtils.post(PIG_BUILD, formBody);
            Log.e(TAG, PIG_BUILD + "\nresponse==" + response);
            respObject = (NewBuildResultObject) processResp_new_build_result(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return respObject;
    }

    public static NewBuildResultObject build_result(Context context, String pid) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("pId", pid.trim());


        FormBody.Builder builder = new FormBody.Builder();
        // Add Params to Builder
        for (TreeMap.Entry<String, String> entry : treeMap.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        // Create RequestBody
        RequestBody formBody = builder.build();
        Set set = treeMap.keySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = treeMap.get(key);
            Log.e("模型查询结果", "key:" + key + "\nvalue:" + value);
        }

        NewBuildResultObject respObject = null;
        try {
            String response = HttpUtils.post(PIG_BUILD_RESULT, formBody);
            Log.e(TAG, PIG_BUILD_RESULT + "\nresponse==" + response);
            respObject = processResp_new_build_result(response);
        } catch (IOException e) {
            String stringException = "";
            if (e instanceof SocketTimeoutException) {
                stringException = "查询结果超时";
            } else {
                stringException = e.getMessage();
            }
            Toast.makeText(context, stringException, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return respObject;
    }
}
