package com.xiangchuang.risks.model.bean;

public class JuanSTBean {
    public  int operation ;
    public String cameraName ;
    public String name ;
    public int juanId ;

    public JuanSTBean(int operation, String cameraName, String name, int juanId) {
        this.operation = operation;
        this.cameraName = cameraName;
        this.name = name;
        this.juanId = juanId;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getJuanId() {
        return juanId;
    }

    public void setJuanId(int juanId) {
        this.juanId = juanId;
    }
}
