package com.xiangchuangtec.luolu.animalcounter.model;

public class PigBean {
   public String pigHouse;
   public String pigzhujuan;
   public String zhusheId;
   public String zhujuanId;
   public String xunjiannum;


    public PigBean(String pigHouse, String pigzhujuan, String zhusheId, String zhujuanId) {
        this.pigHouse = pigHouse;
        this.pigzhujuan = pigzhujuan;
        this.zhusheId = zhusheId;
        this.zhujuanId = zhujuanId;
    }

    public String getXunjiannum() {
        return xunjiannum;
    }

    public void setXunjiannum(String xunjiannum) {
        this.xunjiannum = xunjiannum;
    }

    public String getPigHouse() {
        return pigHouse;
    }

    public void setPigHouse(String pigHouse) {
        this.pigHouse = pigHouse;
    }

    public String getPigzhujuan() {
        return pigzhujuan;
    }

    public void setPigzhujuan(String pigzhujuan) {
        this.pigzhujuan = pigzhujuan;
    }

    public String getZhusheId() {
        return zhusheId;
    }

    public void setZhusheId(String zhusheId) {
        this.zhusheId = zhusheId;
    }

    public String getZhujuanId() {
        return zhujuanId;
    }

    public void setZhujuanId(String zhujuanId) {
        this.zhujuanId = zhujuanId;
    }
}
