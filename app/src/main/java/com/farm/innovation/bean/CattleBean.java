package com.farm.innovation.bean;

import java.io.Serializable;

public class CattleBean implements Serializable {
    public String name;// 		//图片名
    public byte[] picture;//		//图片数据
    public double longitude;//	//经度
    public double latitude;//	//维度
    public String address;//	//地点
    public long time;//		//拍照时间戳
    public String zipPath;
}
