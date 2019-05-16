package com.farm.innovation.login;

import android.util.Log;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.QueryVideoFlagDataBean;
import com.farm.innovation.biz.classifier.CowFaceDetectTFlite;
import com.farm.innovation.biz.classifier.DonkeyFaceDetectTFlite;
import com.farm.innovation.biz.classifier.PigFaceDetectTFlite;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.utils.ConstUtils;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.tensorflow.demo.FarmDetectorActivity;
import org.tensorflow.demo.FarmGlobal;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.farm.innovation.base.FarmAppConfig.timeVideoStart;

/**
 * Created by biao on 21/11/2017.
 * //  后续可以整合到公共utils
 */

public class Utils {
    //登陆接口
    public static String LOGIN_URL = "http://47.94.233.91:8080/login/get-sms-code/";
    //public static String LOGIN_URL = "http://api.innovationai.com/login/get-sms-code/";

    //登录获取token
    public static String LOGIN_GET_TOKEN_URL = "http://47.94.233.91:8080/login/get-token/";
    //public static String LOGIN_GET_TOKEN_URL = "http://api.innovationai.com/login/get-token/";
    // 上传
    public static String UPLOAD_URL = "http://60.205.209.245:8081/nongxian/pigApp/upload";
    //public static String UPLOAD_URL = "http://api.innovationai.com/ai/up/";






    //密钥
    public static String SECRET_KEY = "YOuXiNPaIsEcReT";
    //mobile参数
    public static String QUEREY_MOBILE = "mobile";

    public static String USERINFO_SHAREFILE = "userinfo_sharefile";

    public static String VIDEOINFO_SHAREFILE = "videoinfo";

    public static String LIBIDINFO_SHAREFILE = "libidinfo";


    public static class Upload {
        public static final String USERID = "userid";
        public static final String TOKEN = "token";
        public static final String TYPE = "type";
        public static final String LIB_ID = "lib_id";
        public static final String LIB_ENVINFO = "lib_envinfo";
        public static final String LIBD_SOURCE = "libd_source";
        public static final String PIG_INFO = "pig_info";
        public static final String SN = "sn";
        public static final String DATA = "data";
        public static final String FILE = "file";
        public static final String GPS = "gps";
        public static final String imei = "imei";
        public static final String FULL_NAME = "fullname";
        public static final String LIB_CREATE_TIME = "lib_createtime";
        public static final String IMAGE_URL = "image_url";
        public static final String VERSION = "version";
    }

    public static class UploadNew {
        public static final String USERID = "userId";
        public static final String TYPE = "type";
        public static final String LIB_NUM = "libNum";
        public static final String LIB_ENVINFO = "libEnvinfo";
        public static final String LIBD_SOURCE = "libdSource";
        public static final String FILE = "file";
        public static final String BAODANMUM = "baodanNo";
        public static final String LIB_ID = "libId";
        public static final String COLLECT_TIME = "collectTime";
    }

    /**
     * 生成SN
     * 参数排序:将除sn以外的参数(数组)按键值正序排列(ksort)
     * 生成请求串:将除sn以外的参数(数组) 成请求串QueryString(http_build_query)
     *
     * @param query
     * @param secretkey
     * @return
     */
    public static String createSN(TreeMap<String, Object> query, String secretkey) {
        Set<String> keySet = query.keySet();
        Iterator<String> iter = keySet.iterator();
        StringBuilder sb = new StringBuilder();

        while (iter.hasNext()) {
            String key = iter.next();
            //  hardcode
            if (key.equals("debug")) {
                continue;
            }
            sb.append(key).append("=").append(query.get(key));
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        String sn = getMD5(sb.append(secretkey).toString());
        return sn;
    }

    /**
     * 对字符串md5加密
     *
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8位字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            return e.getMessage() + "  md5加密失败";
        }
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
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(request).execute()) {


            return response.body().string();
        }
    }

    /**
     * 获取录制开始到当前节点的时长
     */
    public static Long getDuring(long timestamp){
        Log.i("图片数量计算---1", "总长" + FarmAppConfig.during + ":当前时间戳" + timestamp+ ":开始时间戳" + timeVideoStart);
        return FarmAppConfig.during +  (timestamp - timeVideoStart);
    }

    /**
     * 判断已成功采集图片数未达到相应要求
     */
    public static Boolean notUpToStandard(long timestamp, int pastSeconds){
        return  (getDuring(timestamp)/ 1000 / (FarmDetectorActivity.type1Count+ FarmDetectorActivity.type2Count+ FarmDetectorActivity.type3Count+1)) > pastSeconds;
    }

    /**
     * 获取投保或理赔的牲畜识别阈值
     */
    public static void getThreshold(){
        String  tlist = FarmerPreferencesUtils.getStringValue(FarmAppConfig.THRESHOLD_LIST, FarmAppConfig.getActivity());
        QueryVideoFlagDataBean.thresholdList thresholdList = new Gson().fromJson(tlist, QueryVideoFlagDataBean.thresholdList.class);
        com.orhanobut.logger.Logger.e("getThreshold"+ thresholdList.toString());

        int animalType = FarmerPreferencesUtils.getAnimalType(FarmAppConfig.getActivity());
        if (FarmGlobal.model == Model.BUILD.value()) {
            if (animalType == ConstUtils.ANIMAL_TYPE_PIG){
                PigFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getPigtoubao());
            }else if(animalType == ConstUtils.ANIMAL_TYPE_CATTLE){
                CowFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getCowtoubao());
            }else if(animalType == ConstUtils.ANIMAL_TYPE_DONKEY){
                DonkeyFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getDonkeytoubao());
            }
        }else if(FarmGlobal.model == Model.VERIFY.value()){
            if (animalType == ConstUtils.ANIMAL_TYPE_PIG){
                PigFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getPiglipei1());
            }else if(animalType == ConstUtils.ANIMAL_TYPE_CATTLE){
                CowFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getCowlipei1());
            }else if(animalType == ConstUtils.ANIMAL_TYPE_DONKEY){
                DonkeyFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getDonkeylipei1());
            }
        }
    }
    /**
     * 获取理赔的牲畜识别 降低后的阈值
     */
    public static void setLowThreshold(){
        String  tlist = FarmerPreferencesUtils.getStringValue(FarmAppConfig.THRESHOLD_LIST, FarmAppConfig.getActivity());
        QueryVideoFlagDataBean.thresholdList thresholdList = new Gson().fromJson(tlist, QueryVideoFlagDataBean.thresholdList.class);
        Logger.e("getLowThreshold"+ thresholdList.toString() );

        int animalType = FarmerPreferencesUtils.getAnimalType(FarmAppConfig.getActivity());
         if(FarmGlobal.model == Model.VERIFY.value()){
            if (animalType == ConstUtils.ANIMAL_TYPE_PIG){
                PigFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getPiglipei2());
            }else if(animalType == ConstUtils.ANIMAL_TYPE_CATTLE){
                CowFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getCowlipei2());
            }else if(animalType == ConstUtils.ANIMAL_TYPE_DONKEY){
                DonkeyFaceDetectTFlite.MIN_CONFIDENCE = Float.parseFloat(thresholdList.getDonkeylipei2());
            }
        }
    }


}
