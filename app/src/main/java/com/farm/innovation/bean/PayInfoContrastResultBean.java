package com.farm.innovation.bean;

import java.util.List;

/**
 * Created by Luolu on 2018/9/18.
 * InnovationAI
 * luolu@innovationai.cn
 */
public class PayInfoContrastResultBean {

    /**
     * data : {"lipeiId":2244,"resultPic":[{"detail":"保单号：911理赔操作者：测试采集时间：Mon Sep 17 16:20:56 CST 2018投保人姓名：lk圈号：无舍号：无栏无耳标号无","libId":"20988","pic":"http://47.92.167.61:3389/20180917/128/test/pic/Data/110/Time-2018_09_17_042052/Angle-03/20180917042039855.jpg"}],"resultMsg":"在投保库中有相似对象，相似度90.83%。在历史理赔库中有相似对象，相似度99%"}
     */

    private DataOffLineBaodanBean data;
    private String msg;
    private int status;

    public DataOffLineBaodanBean getData() {
        return data;
    }

    public void setData(DataOffLineBaodanBean data) {
        this.data = data;
    }

    public static class DataOffLineBaodanBean {
        /**
         * resultStatus:0-3
         * lipeiId : 2244
         * resultPic : [{"detail":"保单号：911理赔操作者：测试采集时间：Mon Sep 17 16:20:56 CST 2018投保人姓名：lk圈号：无舍号：无栏无耳标号无","libId":"20988","pic":"http://47.92.167.61:3389/20180917/128/test/pic/Data/110/Time-2018_09_17_042052/Angle-03/20180917042039855.jpg"}]
         * resultMsg : 在投保库中有相似对象，相似度90.83%。在历史理赔库中有相似对象，相似度99%
         */

        private int lipeiId;
        private String resultMsg;
        private List<ResultPicOffLineBaodanBean> resultPic;

        public int getResultStatus() {
            return resultStatus;
        }

        public void setResultStatus(int resultStatus) {
            this.resultStatus = resultStatus;
        }

        private int resultStatus;

        public int getLipeiId() {
            return lipeiId;
        }

        public void setLipeiId(int lipeiId) {
            this.lipeiId = lipeiId;
        }

        public String getResultMsg() {
            return resultMsg;
        }

        public void setResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
        }

        public List<ResultPicOffLineBaodanBean> getResultPic() {
            return resultPic;
        }

        public void setResultPic(List<ResultPicOffLineBaodanBean> resultPic) {
            this.resultPic = resultPic;
        }

        public static class ResultPicOffLineBaodanBean {
            /**
             * detail : 保单号：911理赔操作者：测试采集时间：Mon Sep 17 16:20:56 CST 2018投保人姓名：lk圈号：无舍号：无栏无耳标号无
             * libId : 20988
             * pic : http://47.92.167.61:3389/20180917/128/test/pic/Data/110/Time-2018_09_17_042052/Angle-03/20180917042039855.jpg
             */

            private String detail;
            private String libId;
            private String pic;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            private String id;

            public String getDetail() {
                return detail;
            }

            public void setDetail(String detail) {
                this.detail = detail;
            }

            public String getLibId() {
                return libId;
            }

            public void setLibId(String libId) {
                this.libId = libId;
            }

            public String getPic() {
                return pic;
            }

            public void setPic(String pic) {
                this.pic = pic;
            }
        }
    }
}
