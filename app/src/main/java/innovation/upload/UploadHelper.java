package innovation.upload;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.xiangchuang.risks.utils.AVOSCloudUtils;

import org.json.JSONObject;
import java.io.File;
import java.util.TreeMap;

import innovation.env.Logger;
import innovation.location.LocationManager;
import innovation.login.ResponseProcessor;
import innovation.login.Utils;
import innovation.utils.DeviceUtil;
import innovation.utils.JsonHelper;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @author wbs on 11/23/17.
 */

public class UploadHelper {
    private static final Logger sLogger = new Logger(UploadHelper.class);

    public static final int TYPE_PIC = 1;
    public static final int TYPE_VIDEO = 2;

    /**
     * 上传图片或视频文件
     *
     * @param model 建库or验证
     */
    public static UploadResp uploadImages(Context context, int model, File zipFile) {
        return uploadRes(context, model, TYPE_PIC, 0, null, zipFile);
    }

    public static UploadResp uploadVideo(Context context, int model, int lib_id, String gps, File videoZipFile) {
        return uploadRes(context, model, TYPE_VIDEO, lib_id, gps, videoZipFile);
    }

    private static UploadResp uploadRes(Context context, int model, int source, int lib_id, String gps, File file) {
        SharedPreferences pref = context.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put(Utils.Upload.USERID, pref.getInt("uid", 0));
        treeMap.put(Utils.Upload.TOKEN, pref.getString("token", ""));
        treeMap.put(Utils.Upload.TYPE, model);
        treeMap.put(Utils.Upload.LIBD_SOURCE, source);
        if (source == TYPE_VIDEO) {
            treeMap.put(Utils.Upload.LIB_ID, lib_id);
        }
        treeMap.put(Utils.Upload.LIB_ENVINFO, getEnvInfo(context, gps));
        String sn = Utils.createSN(treeMap, Utils.SECRET_KEY);
        treeMap.put(Utils.Upload.SN, sn);
        Gson gson = new Gson();
        String data = gson.toJson(treeMap);
        sLogger.i("data: " + data);
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        requestBody.addFormDataPart(Utils.Upload.DATA, data)
                .addFormDataPart(Utils.Upload.FILE, file.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), file));
        UploadResp uploadResp = null;
        try {
            String response = Utils.post(Utils.UPLOAD_URL, requestBody.build());
            sLogger.i("upload res model:%d, source:%d, resp:%s", model, source, response);
            uploadResp = (UploadResp) ResponseProcessor.processResp(response, Utils.UPLOAD_URL);
        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e);
        }
        return uploadResp;
    }

    public static void saveVideoInfo(final Context context, int model, int lib_id, File file) {
        SharedPreferences pref = context.getSharedPreferences(Utils.VIDEOINFO_SHAREFILE, Context.MODE_PRIVATE);
        String value = model + "|" + file.getAbsolutePath()
                + "|" + LocationManager.getInstance(context).getLocationDetail();
        pref.edit().putString(String.valueOf(lib_id), value).apply();
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
}
