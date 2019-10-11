package com.xiangchuang.risks.model.bean;

import android.graphics.Bitmap;

public final class RecognitionResult {
    public int index;
    public int count;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap bitmap;
    public final String fileName;
    public double lat, lon;
    public String juanName;
    public int autoCount;
    public String time;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "RecognitionResult{" +
                "index=" + index +
                ", count=" + count +
                ", bitmap=" + bitmap +
                ", fileName='" + fileName + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", juanName='" + juanName + '\'' +
                ", autoCount=" + autoCount +
                ", time='" + time + '\'' +
                '}';
    }

    public RecognitionResult(int index, int autoCount, Bitmap bitmap, String fileName, String time) {
        this.index = index;
        this.autoCount = autoCount;
        this.bitmap = bitmap;
        this.fileName = fileName;
        this.count = autoCount;
        this.time = time;
    }

    public RecognitionResult(int index, int autoCount, Bitmap bitmap, String fileName){
        this(index,autoCount,bitmap,fileName,"");
    }
}
