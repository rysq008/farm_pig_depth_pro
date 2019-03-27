package com.xiangchuang.risks.model.bean;

import java.io.Serializable;

/**
 * @Author: Lucas.Cui
 * 时   间：2019/3/27
 * 简   述：<功能简述> 基类Bean
 */
public class BaseBean<T> implements Serializable{

    private int status;
    private String msg;
    private T data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseBean{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
