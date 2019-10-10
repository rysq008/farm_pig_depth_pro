package com.xiangchuangtec.luolu.animalcounter.model;

import com.google.gson.annotations.SerializedName;

public class Commit {

    @SerializedName("@type")
    private String _$Type218; // FIXME check this code
    private String data;
    private String msg;
    private int status;

    public String get_$Type218() {
        return _$Type218;
    }

    public void set_$Type218(String _$Type218) {
        this._$Type218 = _$Type218;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
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
