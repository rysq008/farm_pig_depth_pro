package innovation.upload;


import org.json.JSONObject;

import innovation.login.RespObject;
import innovation.login.Utils;
import innovation.utils.JsonHelper;

/**
 * @author wbs on 11/25/17.
 */

public class UploadResp extends RespObject {
    public int userid;
    public String token;
    public int type;
    public int lib_id;
    public int libd_source;
    public String pig_info;

    @Override
    public void setdata(JSONObject data) {
        userid = JsonHelper.getInt(data, Utils.Upload.USERID);
        token = JsonHelper.getString(data, Utils.Upload.TOKEN);
        type = JsonHelper.getInt(data, Utils.Upload.TYPE);
        lib_id = JsonHelper.getInt(data, Utils.Upload.LIB_ID);
        libd_source = JsonHelper.getInt(data, Utils.Upload.LIBD_SOURCE);
        pig_info = JsonHelper.getString(data, Utils.Upload.PIG_INFO);
    }
}
