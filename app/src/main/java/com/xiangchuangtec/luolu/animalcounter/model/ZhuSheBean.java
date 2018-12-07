package com.xiangchuangtec.luolu.animalcounter.model;

public class ZhuSheBean {
    public  String zhushename;
    public  String zhusheid;

    public ZhuSheBean(String zhushename, String zhusheid) {
        this.zhushename = zhushename;
        this.zhusheid = zhusheid;
    }

    public String getZhushename() {
        return zhushename;
    }

    public void setZhushename(String zhushename) {
        this.zhushename = zhushename;
    }

    public String getZhusheid() {
        return zhusheid;
    }

    public void setZhusheid(String zhusheid) {
        this.zhusheid = zhusheid;
    }
}
