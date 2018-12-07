package com.xiangchuang.risks.model.bean;

public class CommitBean {

    /**
     * data : {"lipeiId":2,"similarFlg":0,"similarImgUrl":""}
     * msg : 预理赔处理成功
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
         * lipeiId : 2
         * similarFlg : 0
         * similarImgUrl :
         */

        private int lipeiId;
        private int similarFlg;
        private String similarImgUrl;

        public int getLipeiId() {
            return lipeiId;
        }

        public void setLipeiId(int lipeiId) {
            this.lipeiId = lipeiId;
        }

        public int getSimilarFlg() {
            return similarFlg;
        }

        public void setSimilarFlg(int similarFlg) {
            this.similarFlg = similarFlg;
        }

        public String getSimilarImgUrl() {
            return similarImgUrl;
        }

        public void setSimilarImgUrl(String similarImgUrl) {
            this.similarImgUrl = similarImgUrl;
        }
    }
}
