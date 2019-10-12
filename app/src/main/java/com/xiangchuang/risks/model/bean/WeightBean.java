package com.xiangchuang.risks.model.bean;

import java.io.Serializable;

/**
 * @Author: Lucas.Cui
 * 时   间：2019/6/26
 * 简   述：<功能简述>
 */
public class WeightBean implements Serializable {

    private static final long serialVersionUID = 56882686901550691L;
    /**
     * weight : 0.0
     * status : 2
     * errorcode : 211
     * msg : 状态异常，没尺子或光线太暗，请重新拍！
     */

    private float weight;
    private float length;
    private float area;
    private int status;
    private int errorcode;
    private String msg;


    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(int errorcode) {
        this.errorcode = errorcode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
