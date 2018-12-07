package com.xiangchuang.risks.model.bean;

import java.util.List;

public class StartBean_new {

    /**
     * data : ["prepay2018091210102211"]
     * msg : 开始采集成功
     * status : 1
     */

    private List<String> data;
    private String msg;
    private int status;

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
