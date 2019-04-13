package com.farm.innovation.bean;

import com.farm.innovation.utils.HttpRespObject;

import org.json.JSONObject;

import org.tensorflow.demo.FarmGlobal;

public class AddPigObject extends HttpRespObject {

    public HttpRespObject obj_base; //基本信息（保单/理赔）
    public PigObject obj_pig;

    @Override
    public void setdata(JSONObject data) {
        obj_base = set_base(data);
        JSONObject json;

        obj_pig = new PigObject();
        json = data.optJSONObject("pig");
        obj_pig.setdata(json);
    }
    private static HttpRespObject set_base(JSONObject data) {
        HttpRespObject respObj;
        JSONObject json;
        if (FarmGlobal.Func_type == FarmGlobal.Func_Insurance) {
            respObj = new BaodanBean();
            json = data.optJSONObject("baodan");
            ((BaodanBean)respObj).setdata(json);
        } else if (FarmGlobal.Func_type == FarmGlobal.Func_Pay) {
            respObj = new LipeiBean();
            json = data.optJSONObject("lipei");
            ((LipeiBean)respObj).setdata(json);
        } else {
            return null;
        }
        return respObj;
    }


}