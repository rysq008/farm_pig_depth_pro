package com.farm.innovation.bean;

public class BaoDanNetBean {
    private String baodanNo;
    private String yanBiaoName;
    private String name;
    private String cardNo;
    private String createtime;
    private String baodanName;
    private String baodan_id;
    private String collectAmount;

    public BaoDanNetBean(String baodanNo, String yanBiaoName, String name, String cardNo, String createtime,String baodanName, String collectAmount) {
        this.baodanNo = baodanNo;
        this.yanBiaoName = yanBiaoName;
        this.name = name;
        this.cardNo = cardNo;
        this.createtime = createtime;
        this.baodanName = baodanName;
        this.collectAmount = collectAmount;
    }

    public String getCollectAmount() {
        return collectAmount;
    }

    public void setCollectAmount(String collectAmount) {
        this.collectAmount = collectAmount;
    }

    public void setBaodan_id(String baodan_id) {
        this.baodan_id = baodan_id;
    }

    public String getBaodanName() {
        return baodanName;
    }

    public String getBaodan_id() {
        return baodan_id;
    }

    public String getBaodanNo() {
        return baodanNo;
    }

    public void setBaodanNo(String baodanNo) {
        this.baodanNo = baodanNo;
    }

    public String getYanBiaoName() {
        return yanBiaoName;
    }

    public void setYanBiaoName(String yanBiaoName) {
        this.yanBiaoName = yanBiaoName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    @Override
    public String toString() {
        return "BaoDanNetBean{" +
                "baodanNo='" + baodanNo + '\'' +
                ", yanBiaoName='" + yanBiaoName + '\'' +
                ", name='" + name + '\'' +
                ", cardNo='" + cardNo + '\'' +
                ", createtime='" + createtime + '\'' +
                ", baodanName='" + baodanName + '\'' +
                ", baodan_id='" + baodan_id + '\'' +
                ", collectAmount='" + collectAmount + '\'' +
                '}';
    }
}
