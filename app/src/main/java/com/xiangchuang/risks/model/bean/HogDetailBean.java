package com.xiangchuang.risks.model.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HogDetailBean {

    @SerializedName("@type")
    private String _$Type8; // FIXME check this code
    private DataBean data;
    private String msg;
    private int status;

    public String get_$Type8() {
        return _$Type8;
    }

    public void set_$Type8(String _$Type8) {
        this._$Type8 = _$Type8;
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
        private String _$Type170; // FIXME check this code
        private int count;
        private String createtime;
        private int id;
        private String location;
        private String name;
        private String path;
        private int sheId;
        private String timeLength;
        private String juanCnt;
        private String autoCount;
        private String createuser;

        public String getAutoCount() {
            return autoCount;
        }

        public void setAutoCount(String autoCount) {
            this.autoCount = autoCount;
        }

        public String getCreateuser() {
            return createuser;
        }

        public void setCreateuser(String createuser) {
            this.createuser = createuser;
        }

        private List<String> pics;

        public String getJuanCnt() {
            return juanCnt;
        }

        public void setJuanCnt(String juanCnt) {
            this.juanCnt = juanCnt;
        }

        public int getSheId() {
            return sheId;
        }

        public void setSheId(int sheId) {
            this.sheId = sheId;
        }

        public String get_$Type170() {
            return _$Type170;
        }

        public void set_$Type170(String _$Type170) {
            this._$Type170 = _$Type170;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getCreatetime() {
            return createtime;
        }

        public void setCreatetime(String createtime) {
            this.createtime = createtime;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getTimeLength() {
            return timeLength;
        }

        public void setTimeLength(String timeLength) {
            this.timeLength = timeLength;
        }

        public List<String> getPics() {
            return pics;
        }

        public void setPics(List<String> pics) {
            this.pics = pics;
        }
    }
}
