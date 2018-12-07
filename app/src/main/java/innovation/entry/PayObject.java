package innovation.entry;



import org.json.JSONObject;

import innovation.utils.HttpRespObject;

public class PayObject extends HttpRespObject {
    baodanBean obj_baodan;
    LipeiBean obj_lipei;

    @Override
    public void setdata(JSONObject data) {
        if(data == null)
            return;
        JSONObject json;
        obj_baodan = new baodanBean();
        json = data.optJSONObject("baodan");
        obj_baodan.setdata(json);

        obj_lipei = new LipeiBean();
        json = data.optJSONObject("lipei");
        obj_lipei.setdata(json);

    }

}
