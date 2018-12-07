package com.xiangchuang.risks.model.bean;

import com.google.gson.annotations.SerializedName;
import com.xiangchuang.risks.utils.CommonUtils;

import java.util.List;

public class SheListBean {

    @SerializedName("@type")
    private String _$Type222; // FIXME check this code
    private String msg;
    private int status;
    private List<DataOffLineBaodanBean> data;

    public String get_$Type222() {
        return _$Type222;
    }

    public void set_$Type222(String _$Type222) {
        this._$Type222 = _$Type222;
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

    public List<DataOffLineBaodanBean> getData() {
        return data;
    }

    public void setData(List<DataOffLineBaodanBean> data) {
        this.data = data;
    }

    public static class DataOffLineBaodanBean {
        // FIXME check this code
        @SerializedName("@type")
        private String _$Type102;
        private String autoCount;
        private int count;
        private String createtime;
        private String location;
        private String createuser;
        private String delFlag;
        private String dianshuTime;
        private String enId;
        private String juanCnt;
        private String latitude;
        private String longitude;
        private String pigType;
        private String pigTypeName;
        private String remark;
        private String sheId;
        private String sheName;
        private String timeLength;
        private String updatetime;
        private String updateuser;
        private String distance;

        public String get_$Type102() {
            return _$Type102;
        }

        public void set_$Type102(String _$Type102) {
            this._$Type102 = _$Type102;
        }

        public String getAutoCount() {
            return autoCount;
        }

        public void setAutoCount(String autoCount) {
            this.autoCount = autoCount;
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

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getCreateuser() {
            return createuser;
        }

        public void setCreateuser(String createuser) {
            this.createuser = createuser;
        }

        public String getDelFlag() {
            return delFlag;
        }

        public void setDelFlag(String delFlag) {
            this.delFlag = delFlag;
        }

        public String getDianshuTime() {
            return dianshuTime;
        }

        public void setDianshuTime(String dianshuTime) {
            this.dianshuTime = dianshuTime;
        }

        public String getEnId() {
            return enId;
        }

        public void setEnId(String enId) {
            this.enId = enId;
        }

        public String getJuanCnt() {
            return juanCnt;
        }

        public void setJuanCnt(String juanCnt) {
            this.juanCnt = juanCnt;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getPigType() {
            return pigType;
        }

        public void setPigType(String pigType) {
            this.pigType = pigType;
        }

        public String getPigTypeName() {
            return pigTypeName;
        }

        public void setPigTypeName(String pigTypeName) {
            this.pigTypeName = pigTypeName;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getSheId() {
            return sheId;
        }

        public void setSheId(String sheId) {
            this.sheId = sheId;
        }

        public String getSheName() {
            return sheName;
        }

        public void setSheName(String sheName) {
            this.sheName = sheName;
        }

        public String getTimeLength() {
            return timeLength;
        }

        public void setTimeLength(String timeLength) {
            this.timeLength = timeLength;
        }

        public String getUpdatetime() {
            return updatetime;
        }

        public void setUpdatetime(String updatetime) {
            this.updatetime = updatetime;
        }

        public String getUpdateuser() {
            return updateuser;
        }

        public void setUpdateuser(String updateuser) {
            this.updateuser = updateuser;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }
    }
}
