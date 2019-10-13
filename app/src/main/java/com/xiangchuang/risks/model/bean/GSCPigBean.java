package com.xiangchuang.risks.model.bean;

import android.util.Log;

import static android.content.ContentValues.TAG;

public final class GSCPigBean {
    /**
     * name	        图片名	String	Y
     * picture	    图片数据	Byte[]	Y
     * longitude	    经度	double	Y
     * latitude	    维度	double	Y
     * address	    地点	String	Y
     * time	        拍照时间戳	long 	Y
     * pigHouseNumber	猪舍编号	String	Y
     * photoPigsNumber	照片中包含的猪数量	int	Y
     * totalPigs  	猪舍猪总数	int	Y
     * totalFarmPigs	养殖场猪总数	int	Y
     * videoFlag	    是否视频截图	String	Y	1:视频 0:视频
     */
    public String name;
    //    public byte[] picture;
//    public Bitmap picture;
    public String picture;
    public double longitude;
    public double latitude;
    public String address;
    public long time;
    public String pigHouseNumber;
    public int photoPigsNumber;
    public int totalPigs;
    public int totalFarmPigs;
    public int resultStatus = -1;//投保：0失败，1成功，理赔：0重复，1成功
    public String videoFlag = "0";//1:视频,0:非视频
    public String pigType = "";//辅助判断猪的种类

    public GSCPigBean() {
    }

    public String string() {
        String str = "String: ==>".concat(picture + ",").concat(name + ",").concat(longitude + ",").concat(latitude + ",").concat(address + ",")
                .concat(time + ",").concat(pigHouseNumber + ",").concat(photoPigsNumber + ",").concat(totalPigs + ",").concat(totalFarmPigs + ",").concat(resultStatus + ",")
                .concat(videoFlag + ",").concat(pigType).concat("\n");
        Log.d(TAG, str);
        return str;
    }
}
