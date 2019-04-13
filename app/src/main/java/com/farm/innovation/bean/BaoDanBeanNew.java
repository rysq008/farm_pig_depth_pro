package com.farm.innovation.bean;

public class BaoDanBeanNew {
    public String bankName;
    public int baodanType;
    public int id;
    public String createtime;
    public String toubaoPname;
    public double baodanRate;
    public String toubaoTypeString;

    public BaoDanBeanNew(String bankName, int baodanType, int id, String createtime, String toubaoPname, double baodanRate, String toubaoTypeString) {
        this.bankName = bankName;
        this.baodanType = baodanType;
        this.id = id;
        this.createtime = createtime;
        this.toubaoPname = toubaoPname;
        this.baodanRate = baodanRate;
        this.toubaoTypeString = toubaoTypeString;
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

    public int getBaodanType() {
        return baodanType;
    }

    public void setBaodanType(int baodanType) {
        this.baodanType = baodanType;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getToubaoPname() {
        return toubaoPname;
    }

    public void setToubaoPname(String toubaoPname) {
        this.toubaoPname = toubaoPname;
    }

    public double getBaodanRate() {
        return baodanRate;
    }

    public void setBaodanRate(double baodanRate) {
        this.baodanRate = baodanRate;
    }
}
