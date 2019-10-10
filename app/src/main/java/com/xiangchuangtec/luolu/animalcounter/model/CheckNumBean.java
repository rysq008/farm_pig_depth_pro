package com.xiangchuangtec.luolu.animalcounter.model;

import com.google.gson.annotations.SerializedName;

public class CheckNumBean {

    @SerializedName("@type")
    private String _$Type75; // FIXME check this code
    private DataBean data;
    private String msg;
    private int status;

    public String get_$Type75() {
        return _$Type75;
    }

    public void set_$Type75(String _$Type75) {
        this._$Type75 = _$Type75;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
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

    public static class DataBean {
        @SerializedName("@type")
        private String _$Type150; // FIXME check this code
        private int manualcount;

        public String get_$Type150() {
            return _$Type150;
        }

        public void set_$Type150(String _$Type150) {
            this._$Type150 = _$Type150;
        }

        public int getManualcount() {
            return manualcount;
        }

        public void setManualcount(int manualcount) {
            this.manualcount = manualcount;
        }
    }
}
