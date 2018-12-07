package innovation.utils;

import org.json.JSONObject;

/**
 * Created by haojie on 2018/5/10.
 */

public class UploadObject extends HttpRespObject {

    //环境信息
    public String upload_libEnvinfo = "";
    //唯一猪的标识ID
    public int upload_libId = 0;
    //耳标号
    public String upload_libNum = "";
    //状态
    public int upload_libStatus = 0;
    //类型
    public int upload_libType = 0;
    //用户ID
    public int upload_libUserid = 0;
    //猪信息
    public String upload_pigInfo = "";

    @Override
    public void setdata(JSONObject data) {
        if(data == null)
            return;
        upload_libEnvinfo = data.optString("libEnvinfo", "");
        upload_libId = data.optInt("libId", 0);
        upload_libNum = data.optString("libNum", "");
        upload_libStatus = data.optInt("libStatus", 0);
        upload_libType = data.optInt("libType", 0);
        upload_libUserid = data.optInt("libUserid", 0);
        //upload_pigInfo = data.optString("libPigInfo", "");
        upload_pigInfo = "";
    }
}
