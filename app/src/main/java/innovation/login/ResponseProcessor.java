package innovation.login;

import org.json.JSONException;
import org.json.JSONObject;

import innovation.upload.UploadResp;

/**
 * Created by biao on 22/11/2017.
 */

public class ResponseProcessor {

    public static LoginResp mLoginResp;

    public static TokenResp mTokenResp;

    private ResponseProcessor() {

    }

    /**
     * @param resp
     */
    public static RespObject processResp(String resp, String url) {
        RespObject respObj;
        try {
            if (Utils.LOGIN_URL.equalsIgnoreCase(url)) {
                respObj = new LoginResp();
            } else if (Utils.LOGIN_GET_TOKEN_URL.equalsIgnoreCase(url)) {
                respObj = new TokenResp();
            } else if (Utils.UPLOAD_URL.equalsIgnoreCase(url)) {
                respObj = new UploadResp();
            } else {
                return null;
            }
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
}
