package com.xiangchuang.risks.model.bean;

public class SheXTBean {
    public String cameraName;
    public String cameraNo;
    public String repair;
    public int cameraId;
    public String sheId;
    public String sheName;

    public SheXTBean(String cameraName, String cameraNo, String repair, int cameraId, String sheId, String sheName) {
        this.cameraName = cameraName;
        this.cameraNo = cameraNo;
        this.repair = repair;
        this.cameraId = cameraId;
        this.sheId = sheId;
        this.sheName = sheName;
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

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getCameraNo() {
        return cameraNo;
    }

    public void setCameraNo(String cameraNo) {
        this.cameraNo = cameraNo;
    }

    public String getRepair() {
        return repair;
    }

    public void setRepair(String repair) {
        this.repair = repair;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }
}
