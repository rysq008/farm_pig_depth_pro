package com.xiangchuang.risks.model.bean;

public class PayInfo {

    /**
     * payTime : 2019-05-15 12:11:33
     * baodanNo : 321123546465
     * pigType : 育肥舍
     * timesFlag : 15225548822
     * lipeiNo : ***
     * lipeiId : ***
     * pigImg : http://***
     */

    private String payTime;
    private String baodanNo;
    private String pigType;
    private String timesFlag;
    private String lipeiNo;
    private String lipeiId;
    private String pigImg;
    private String sheName;
    private String pigTypeId;

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getBaodanNo() {
        return baodanNo;
    }

    public void setBaodanNo(String baodanNo) {
        this.baodanNo = baodanNo;
    }

    public String getPigType() {
        return pigType;
    }

    public void setPigType(String pigType) {
        this.pigType = pigType;
    }

    public String getTimesFlag() {
        return timesFlag;
    }

    public void setTimesFlag(String timesFlag) {
        this.timesFlag = timesFlag;
    }

    public String getLipeiNo() {
        return lipeiNo;
    }

    public void setLipeiNo(String lipeiNo) {
        this.lipeiNo = lipeiNo;
    }

    public String getLipeiId() {
        return lipeiId;
    }

    public void setLipeiId(String lipeiId) {
        this.lipeiId = lipeiId;
    }

    public String getPigImg() {
        return pigImg;
    }

    public void setPigImg(String pigImg) {
        this.pigImg = pigImg;
    }

    public String getSheName() {
        return sheName;
    }

    public void setSheName(String sheName) {
        this.sheName = sheName;
    }

    public String getPigTypeId() {
        return pigTypeId;
    }

    public void setPigTypeId(String pigTypeId) {
        this.pigTypeId = pigTypeId;
    }

    @Override
    public String toString() {
        return "PayInfo{" +
                "payTime='" + payTime + '\'' +
                ", baodanNo='" + baodanNo + '\'' +
                ", pigType='" + pigType + '\'' +
                ", timesFlag='" + timesFlag + '\'' +
                ", lipeiNo='" + lipeiNo + '\'' +
                ", lipeiId='" + lipeiId + '\'' +
                ", pigImg='" + pigImg + '\'' +
                ", sheName='" + sheName + '\'' +
                ", pigTypeId='" + pigTypeId + '\'' +
                '}';
    }
}
