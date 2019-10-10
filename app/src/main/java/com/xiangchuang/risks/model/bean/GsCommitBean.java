package com.xiangchuang.risks.model.bean;

public class GsCommitBean {

    /**
     * data : {"buildStatus":0,"resultMsg":"在近期历史理赔库中找到相似对象","xiangsidu":"93.21%"}
     * msg : 理赔比对完毕
     * status : 1
     */

    private DataBean data;
    private String msg;
    private int status;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static class DataBean {
        /**
         * buildStatus : 0
         * resultMsg : 在近期历史理赔库中找到相似对象
         * xiangsidu : 93.21%
         */

        private int buildStatus;
        private String resultMsg;
        private String xiangsidu;

        public int getBuildStatus() {
            return buildStatus;
        }

        public void setBuildStatus(int buildStatus) {
            this.buildStatus = buildStatus;
        }

        public String getResultMsg() {
            return resultMsg;
        }

        public void setResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
        }

        public String getXiangsidu() {
            return xiangsidu;
        }

        public void setXiangsidu(String xiangsidu) {
            this.xiangsidu = xiangsidu;
        }
    }

    @Override
    public String toString() {
        return "GsCommitBean{" +
                "data=" + data +
                ", msg='" + msg + '\'' +
                ", status=" + status +
                '}';
    }
}
