package com.xiangchuang.risks.model.bean;

public class JudgeRecordVideo {
    // 是否录制视频(0 未录制, 1 已录制)
    private int isRecordVideo;
    private String lipeiId;
    private String timesFlag;

    public int getIsRecordVideo() {
        return isRecordVideo;
    }

    public void setIsRecordVideo(int isRecordVideo) {
        this.isRecordVideo = isRecordVideo;
    }

    public String getLipeiId() {
        return lipeiId;
    }

    public void setLipeiId(String lipeiId) {
        this.lipeiId = lipeiId;
    }

    public String getTimesFlag() {
        return timesFlag;
    }

    public void setTimesFlag(String timesFlag) {
        this.timesFlag = timesFlag;
    }
}
