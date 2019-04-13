package com.farm.innovation.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.AddPigObject;
import com.farm.innovation.bean.BaodanBean;
import com.farm.innovation.bean.BuildObject;
import com.farm.innovation.bean.MultiBaodanBean;
import com.farm.innovation.bean.NewBuildObject;
import com.farm.innovation.bean.NewBuildResultObject;
import com.farm.innovation.bean.PayObject;
import com.farm.innovation.bean.UpdateBean;
import com.farm.innovation.bean.UploadImageObject;
import com.farm.innovation.bean.UserRegisterBean;
import com.farm.innovation.bean.VerifyCodeBean;
import com.farm.innovation.bean.VerifyObject;
import com.farm.innovation.location.AlertDialogManager;
import com.farm.innovation.location.LocationManager;
import com.farm.innovation.login.Utils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Author by luolu, Date on 2018/8/15.
 * COMPANY：InnovationAI
 */

public class HttpUtils {

    private static String TAG = "HttpUtils";
    // TODO: 2018/8/17 By:LuoLu
//    正式地址：60.205.209.245:8081;   测试地址：47.92.167.61:8081
//    public static final String baseUrl = "http://60.205.209.245:8081/nongxian2/";
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
    //图片上传接口
    public static String PIG_IMAGEUPLOAD = baseUrl + "pigApp/uploadImage";
    //建库
    public static String PIG_BUILD = baseUrl + "pigApp/cow";
    //上传地址（*********************）
    public static String UPLOAD_URL = baseUrl + "upload/";

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
    public static String USERINFO_SHAREFILE = "userinfo_sharefile";
    //登陆
    public static String PIC_LOGIN_URL = baseUrl + "app/login";
    public static String AAR_PIC_LOGIN_URL = baseUrl + "app/aarLogin";

    //获取公司
    public static String GET_ALL_COMPANY_URL = baseUrl + "app/queryAppDept";

    //获取版本更新json /app/appVersion/nxupdate
    public static String GET_UPDATE_URL = baseUrl + "app/appVersion/nxupdate";
    // TODO: 2018/8/9 By:LuoLu
    public static String BUILD_UPLOAD_OFFLINE_URL = baseUrl + "pigApp/uploadOffline";
    // TODO: 2018/9/18 By:LuoLu
//   	理赔信息校验接口
    public static String ANIMAL_PAY_INFOCHECK = baseUrl + "appCoreV2/lipeiInfoCheck";
    //	 理赔图像信息上传接口
    public static String PAY_LIBUPLOAD = baseUrl + "appCoreV2/libUpload";
    //  理赔强制上传接口
    public static String FORCE_LIPEI_UPLOAD = baseUrl + "appCoreV2/forceLipeiUpload";
    //	 投保图像信息上传接口
    public static String INSURE_IMAGE_UPLOAD = baseUrl + "appCore/toubaoUpload";
    //  理赔比对接口
    public static String PAY_INFO_CONTRAST = baseUrl + "appCoreV2/lipeiContrast";
    //	理赔申请处理接口
    public static String PAY_APPLY = baseUrl + "appCoreV2/lipeiApply";
    //投保新增牲畜
    public static String ADD_ANIMAL = baseUrl + "appCore/addAnimal";
    //    查看是否录制视频接口
    public static String QUERY_VIDEOFLAG = baseUrl + "appCoreV2/queryVideoFlag";
    public static String QUERY_VIDEOFLAG_NEW = baseUrl + "appCoreV2/queryVideoFlag";
    //    首页获取验标单
    public static String SEARCH_YANBIAO = baseUrl + "baodan/searchYanbiao";
    //    查询验标单的状态
    public static String STATE_YANBIAO = baseUrl + "baodan/yanbiaoStatus";
    // 获取tips信息
    public static String GET_NOTICE = baseUrl + "appNotice/get";
    // TODO: 2018/9/30 By:LuoLu
    public static String vertify_URL = baseUrl + "app/queryAppCode";
    public static String BaoDanList = baseUrl + "baodanSum/baodanList";
    public static String BaoDanadd = baseUrl + "baodanSum/addBaodan";
    public static String BaoDannametest = baseUrl + "baodanSum/testName";
    public static String BaoDanaddyan = baseUrl + "baodanSum/addYanbiao";
    public static String AppKeyAuthorization = "AppKeyAuthorization";
    public static String code = "code";
    public static String deptId = "deptId";
    public static String baodanName = "baodanName";
    public static String baodanType = "baodanType";
    public static String InsuranceType = "InsuranceType";
    public static String insuranceRate = "insuranceRate";
    public static String farmForm = "farmForm";
    public static String InsuranceCost = "InsuranceCost";
    public static String baodanApplyAddress = "baodanApplyAddress";
    public static String baodanApplyName = "baodanApplyName";
    public static String id = "id";
    public static String user_id = "user_id";
    public static String createyan = "yes";
    public static String reason = "reason";
    public static String offlineupdate = "offlineupdate";
    public static String upload = baseUrl + "uploadImg";
    //如果数据库改变，增加版本号
    public static final int DATABSAE_VERSION = 8;


    public static void resetIp(String baseUrl){
        HttpUtils.baseUrl = baseUrl;
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
        //图片上传接口
        PIG_IMAGEUPLOAD = baseUrl + "pigApp/uploadImage";
        //建库
        PIG_BUILD = baseUrl + "pigApp/cow";
        //上传地址（*********************）
        UPLOAD_URL = baseUrl + "upload/";
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
        USERINFO_SHAREFILE = "userinfo_sharefile";
        //登陆
        PIC_LOGIN_URL = baseUrl + "app/login";
        //获取公司
        GET_ALL_COMPANY_URL = baseUrl + "app/queryAppDept";
        //获取版本更新json
        GET_UPDATE_URL = baseUrl + "app/appVersion/nxupdate";
        BUILD_UPLOAD_OFFLINE_URL = baseUrl + "pigApp/uploadOffline";
        //     理赔信息校验接口
        ANIMAL_PAY_INFOCHECK = baseUrl + "appCoreV2/lipeiInfoCheck";
        //  理赔图像信息上传接口
        PAY_LIBUPLOAD = baseUrl + "appCoreV2/libUpload";
        //  理赔强制上传接口
        FORCE_LIPEI_UPLOAD = baseUrl + "appCoreV2/forceLipeiUpload";
        //  投保图像信息上传接口
        INSURE_IMAGE_UPLOAD = baseUrl + "appCore/toubaoUpload";
        //  理赔比对接口
        PAY_INFO_CONTRAST = baseUrl + "appCoreV2/lipeiContrast";
        // 理赔申请处理接口
        PAY_APPLY = baseUrl + "appCoreV2/lipeiApply";
        //投保新增牲畜
        ADD_ANIMAL = baseUrl + "appCore/addAnimal";
        //    查看是否录制视频接口
        QUERY_VIDEOFLAG = baseUrl + "appCoreV2/queryVideoFlag";
        QUERY_VIDEOFLAG_NEW = baseUrl + "appCoreV2/queryVideoFlag";
        //    首页获取验标单
        SEARCH_YANBIAO = baseUrl + "baodan/searchYanbiao";
        //    查询验标单的状态
        STATE_YANBIAO = baseUrl + "baodan/yanbiaoStatus";
        // 获取tips信息
        GET_NOTICE = baseUrl + "appNotice/get";
        // TODO: 2018/9/30 By:LuoLu
        vertify_URL = baseUrl + "app/queryAppCode";
        BaoDanList = baseUrl + "baodanSum/baodanList";
        BaoDanadd = baseUrl + "baodanSum/addBaodan";
        BaoDannametest = baseUrl + "baodanSum/testName";
        BaoDanaddyan = baseUrl + "baodanSum/addYanbiao";
        upload = baseUrl + "uploadImg";
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
    public static String post(String url, RequestBody body) throws Exception {
        int animalType = PreferencesUtils.getAnimalType(FarmAppConfig.getApplication());
        if (ConstUtils.ANIMAL_TYPE_NONE == animalType
                && !HttpUtils.PIC_LOGIN_URL.equals(url)
                && !HttpUtils.GET_ALL_COMPANY_URL.equals(url)
                && !HttpUtils.vertify_URL.equals(url)
                && !HttpUtils.GET_SMSCODE_URL.equals(url)
                && !HttpUtils.GET_REGISTER_URL.equals(url)
                && !HttpUtils.GET_UPDATE_URL.equals(url)
                && !HttpUtils.QUERY_VIDEOFLAG_NEW.equals(url)
                && !HttpUtils.GET_NOTICE.equals(url)
                ) {
            AlertDialogManager.showMessageDialogOne(FarmAppConfig.getActivity(), "提示", "您还未选择牲畜信息");
            return "";
        } else {
            Request request = new Request.Builder()
                    .url(url)
                    .header("AppKeyAuthorization", "hopen")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("animalType", String.valueOf(animalType))
                    .post(body)
                    .build();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(130, TimeUnit.SECONDS)
                    .readTimeout(160, TimeUnit.SECONDS)
                    .build();
            Response response = null;
//            try {
            response = client.newCall(request).execute();
//            }
//            catch (SocketTimeoutException e){
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e(TAG, "IOException: "+e.toString());
//            }
//            catch (Exception baseE) {
//                baseE.printStackTrace();
//                Log.e(TAG, "Exception: "+baseE.toString());
//            }

            if (response == null) {
                return null;
            } else {
                return response.body().string();
            }
        }
    }

    public static String get(String url) throws IOException {
        int animalType = PreferencesUtils.getAnimalType(FarmAppConfig.getApplication());
        if (ConstUtils.ANIMAL_TYPE_NONE == animalType
                && !HttpUtils.PIC_LOGIN_URL.equals(url)
                && !HttpUtils.GET_ALL_COMPANY_URL.equals(url)
                && !HttpUtils.vertify_URL.equals(url)
                && !HttpUtils.GET_SMSCODE_URL.equals(url)
                && !HttpUtils.GET_REGISTER_URL.equals(url)
                && !HttpUtils.GET_UPDATE_URL.equals(url)
                && !HttpUtils.QUERY_VIDEOFLAG_NEW.equals(url)
                ) {
            AlertDialogManager.showMessageDialogOne(FarmAppConfig.getActivity(), "提示", "您还未选择牲畜信息");
            return "";
        } else {
            Request request = new Request.Builder()
                    .url(url)
                    .header("AppKeyAuthorization", "hopen")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("animalType", String.valueOf(animalType))
                    .build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            if (response == null) {
                return null;
            } else {
                return response.body().string();
            }
        }
    }

    /**
     * @param resp
     */
    public static HttpRespObject processResp_insurInfo(String resp, String url) {
        HttpRespObject respObj;
        if (resp == null) {
            return null;
        }
        try {
            if (HttpUtils.INSUR_NEW_URL.equalsIgnoreCase(url)) {
                respObj = new BaodanBean();
            } else if (HttpUtils.INSUR_UPDATE_URL.equalsIgnoreCase(url)) {
                respObj = new BaodanBean();
            } else if (HttpUtils.INSUR_QUERY_URL.equalsIgnoreCase(url)) {
                respObj = new BaodanBean();
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
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
            return null;
        }
    }


    public static UploadObject processResp_upload(String resp) {
        UploadObject respObj;
        if (resp == null) {
            return null;
        }
        try {
            respObj = new UploadObject();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            String tmpMsg = json.getString("msg");
            if (!TextUtils.isEmpty(tmpMsg)) {
                respObj.msg = json.getString("msg");
            }
            String tmpdata = json.getString("data");
            if (!TextUtils.isEmpty(tmpdata)) {
                respObj.data = json.getJSONObject("data");
                respObj.setdata(respObj.data);
            }
            return respObj;
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
            return null;
        }
    }

    public static UpdateBean processResp_update(String resp) {
        UpdateBean respObj;
        if (resp == null) {
            return null;
        }
        try {
            respObj = new UpdateBean();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            respObj.data = json.getString("data");
            respObj.setdata(respObj.data);


            return respObj;
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
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
            String tmpdata = json.getString("data");
            if (!TextUtils.isEmpty(tmpdata)) {
                respObj.data = json.getJSONObject("data");
                respObj.setdata(respObj.data);
            }
            return respObj;
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
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
        // Create RequestBody
        UploadObject uploadResp = null;
        try {
            TreeMap<String, String> treeMap = new TreeMap<>();
            treeMap.put(Utils.UploadNew.USERID, uid + "");
            treeMap.put(Utils.UploadNew.LIB_NUM, libNum);
            treeMap.put(Utils.UploadNew.TYPE, model + "");
            treeMap.put(Utils.UploadNew.LIBD_SOURCE, source + "");
            treeMap.put(Utils.UploadNew.LIB_ENVINFO, getEnvInfo(context, gps));
            // TODO: 2018/8/4
            Log.e("文件上传接口请求报文：", treeMap.toString() + "\n请求地址：" + PAY_LIBUPLOAD);
            MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
            FormBody.Builder builder = new FormBody.Builder();
            for (TreeMap.Entry<String, String> entry : treeMap.entrySet()) {
                requestBody.addFormDataPart(entry.getKey(), entry.getValue());
            }
            requestBody.addFormDataPart("zipFile", file.getName(),
                    RequestBody.create(MediaType.parse("application/octet-stream"), file));
            String responseUpload = HttpUtils.post(PAY_LIBUPLOAD, requestBody.build());
            Log.e("文件上传接口返回：\n", PAY_LIBUPLOAD + "\nresponse==" + responseUpload);
            uploadResp = (UploadObject) processResp_upload(responseUpload);
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
            return uploadResp;
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
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
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
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
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
        if (resp == null) {
            return null;
        }
        try {
            respObj = new VerifyObject();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            String tmpdata = json.getString("data");
            if (!TextUtils.isEmpty(tmpdata)) {
                respObj.data = json.getJSONObject("data");
                respObj.setdata(respObj.data);
            }
            return respObj;
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
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
            String tmpdata = json.getString("data");
            if (!TextUtils.isEmpty(tmpdata)) {
                respObj.data = json.getJSONObject("data");
                respObj.setdata(respObj.data);
            }
            return respObj;
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
            return null;
        }
    }

    public static MultiBaodanBean processResp_new_detail_query(String resp) {
        MultiBaodanBean respObj;
        if (resp == null) {
            return null;
        }
        try {
            respObj = new MultiBaodanBean();
            JSONObject json = new JSONObject(resp);
            respObj.status = json.getInt("status");
            respObj.msg = json.getString("msg");
            respObj.data = json.getString("data");
            respObj.setdata(respObj.data);
            return respObj;
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
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
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
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
            if (!json.getString("data").isEmpty()) {
                respObj.data = json.getJSONObject("data");
            }

            respObj.setdata(respObj.data);
            return respObj;
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
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
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
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
        } catch (Exception e) {
            String stringException = "";
            if (e instanceof SocketTimeoutException) {
                stringException = "查询结果超时";
            } else {
                stringException = e.getMessage();
            }
            Toast.makeText(context, stringException, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, HttpUtils.class.getSimpleName());
        }
        return respObject;
    }
}
