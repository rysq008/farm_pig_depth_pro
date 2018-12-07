package com.xiangchuang.risks.model.bean;

import java.util.List;

public class CommitLiBean {


    /**
     * data : {"similarFlg":1,"similarList":[{"animalId":120,"insureNo":"88888","juanName":"大猪圈2","lipeiId":"83","preCompensateTime":"Thu Sep 13 16:57:43 CST 2018","sheName":"大猪舍1","similarImgUrl":"http://47.92.167.61:3389/20180913/1/lib/pic/Data/88888/20542/Time-2018_09_13_045742/Angle-03/20180913045735552.jpg","similarityDegree":"90.39%"}],"userLibId":20543}
     * msg : 预理赔库中找到相似对象
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
         * similarFlg : 1
         * similarList : [{"animalId":120,"insureNo":"88888","juanName":"大猪圈2","lipeiId":"83","preCompensateTime":"Thu Sep 13 16:57:43 CST 2018","sheName":"大猪舍1","similarImgUrl":"http://47.92.167.61:3389/20180913/1/lib/pic/Data/88888/20542/Time-2018_09_13_045742/Angle-03/20180913045735552.jpg","similarityDegree":"90.39%"}]
         * userLibId : 20543
         */

        private int similarFlg;
        private int userLibId;
        private List<SimilarListBean> similarList;

        public int getSimilarFlg() {
            return similarFlg;
        }

        public void setSimilarFlg(int similarFlg) {
            this.similarFlg = similarFlg;
        }

        public int getUserLibId() {
            return userLibId;
        }

        public void setUserLibId(int userLibId) {
            this.userLibId = userLibId;
        }

        public List<SimilarListBean> getSimilarList() {
            return similarList;
        }

        public void setSimilarList(List<SimilarListBean> similarList) {
            this.similarList = similarList;
        }

        public static class SimilarListBean {
            /**
             * animalId : 120
             * insureNo : 88888
             * juanName : 大猪圈2
             * lipeiId : 83
             * preCompensateTime : Thu Sep 13 16:57:43 CST 2018
             * sheName : 大猪舍1
             * similarImgUrl : http://47.92.167.61:3389/20180913/1/lib/pic/Data/88888/20542/Time-2018_09_13_045742/Angle-03/20180913045735552.jpg
             * similarityDegree : 90.39%
             * seqNo:""
             */

            private int animalId;
            private String insureNo;
            private String juanName;
            private String lipeiId;
            private String preCompensateTime;
            private String sheName;
            private String similarImgUrl;
            private String similarityDegree;
            private String seqNo;
            private String compensateTime;

            public String getCompensateTime() {
                return compensateTime;
            }

            public void setCompensateTime(String compensateTime) {
                this.compensateTime = compensateTime;
            }

            public String getSeqNo() {
                return seqNo;
            }

            public void setSeqNo(String seqNo) {
                this.seqNo = seqNo;
            }

            public int getAnimalId() {
                return animalId;
            }

            public void setAnimalId(int animalId) {
                this.animalId = animalId;
            }

            public String getInsureNo() {
                return insureNo;
            }

            public void setInsureNo(String insureNo) {
                this.insureNo = insureNo;
            }

            public String getJuanName() {
                return juanName;
            }

            public void setJuanName(String juanName) {
                this.juanName = juanName;
            }

            public String getLipeiId() {
                return lipeiId;
            }

            public void setLipeiId(String lipeiId) {
                this.lipeiId = lipeiId;
            }

            public String getPreCompensateTime() {
                return preCompensateTime;
            }

            public void setPreCompensateTime(String preCompensateTime) {
                this.preCompensateTime = preCompensateTime;
            }

            public String getSheName() {
                return sheName;
            }

            public void setSheName(String sheName) {
                this.sheName = sheName;
            }

            public String getSimilarImgUrl() {
                return similarImgUrl;
            }

            public void setSimilarImgUrl(String similarImgUrl) {
                this.similarImgUrl = similarImgUrl;
            }

            public String getSimilarityDegree() {
                return similarityDegree;
            }

            public void setSimilarityDegree(String similarityDegree) {
                this.similarityDegree = similarityDegree;
            }
        }
    }
}
