package com.xiangchuang.risks.model.bean;

import java.util.List;

public class InSureBean {


    /**
     * data : {"ftnBaodanList":[{"amount":"100","baodanId":"1","baodanNo":"201809071820","baodanNoReal":"","baodanStatus":"0","baodanStatusName":"","costSum":"1000","createtime":"2018-09-08 17:18:09","createuser":"","delFlag":"0","enId":"1","enName":"","remark":"测试数据001","seqNo":"1","seqNoRange":"1-100","updatetime":"","updateuser":""}],"insureNums":"0","toBeInsure":"0","totalNums":"0"}
     * msg : 查询成功
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
         * ftnBaodanList : [{"amount":"100","baodanId":"1","baodanNo":"201809071820","baodanNoReal":"","baodanStatus":"0","baodanStatusName":"","costSum":"1000","createtime":"2018-09-08 17:18:09","createuser":"","delFlag":"0","enId":"1","enName":"","remark":"测试数据001","seqNo":"1","seqNoRange":"1-100","updatetime":"","updateuser":""}]
         * insureNums : 0
         * toBeInsure : 0
         * totalNums : 0
         */

        private String insureNums;
        private String toBeInsure;
        private String totalNums;
        private List<FtnBaodanListBean> ftnBaodanList;

        public String getInsureNums() {
            return insureNums;
        }

        public void setInsureNums(String insureNums) {
            this.insureNums = insureNums;
        }

        public String getToBeInsure() {
            return toBeInsure;
        }

        public void setToBeInsure(String toBeInsure) {
            this.toBeInsure = toBeInsure;
        }

        public String getTotalNums() {
            return totalNums;
        }

        public void setTotalNums(String totalNums) {
            this.totalNums = totalNums;
        }

        public List<FtnBaodanListBean> getFtnBaodanList() {
            return ftnBaodanList;
        }

        public void setFtnBaodanList(List<FtnBaodanListBean> ftnBaodanList) {
            this.ftnBaodanList = ftnBaodanList;
        }

        public static class FtnBaodanListBean {
            /**
             * amount : 100
             * baodanId : 1
             * baodanNo : 201809071820
             * baodanNoReal :
             * baodanStatus : 0
             * baodanStatusName :
             * costSum : 1000
             * createtime : 2018-09-08 17:18:09
             * createuser :
             * delFlag : 0
             * enId : 1
             * enName :
             * remark : 测试数据001
             * seqNo : 1
             * seqNoRange : 1-100
             * updatetime :
             * updateuser :
             */

            private String amount;
            private String baodanId;
            private String baodanNo;
            private String baodanNoReal;
            private String baodanStatus;
            private String baodanStatusName;
            private String costSum;
            private String createtime;
            private String createuser;
            private String delFlag;
            private String enId;
            private String enName;
            private String remark;
            private String seqNo;
            private String seqNoRange;
            private String updatetime;
            private String updateuser;

            public String getAmount() {
                return amount;
            }

            public void setAmount(String amount) {
                this.amount = amount;
            }

            public String getBaodanId() {
                return baodanId;
            }

            public void setBaodanId(String baodanId) {
                this.baodanId = baodanId;
            }

            public String getBaodanNo() {
                return baodanNo;
            }

            public void setBaodanNo(String baodanNo) {
                this.baodanNo = baodanNo;
            }

            public String getBaodanNoReal() {
                return baodanNoReal;
            }

            public void setBaodanNoReal(String baodanNoReal) {
                this.baodanNoReal = baodanNoReal;
            }

            public String getBaodanStatus() {
                return baodanStatus;
            }

            public void setBaodanStatus(String baodanStatus) {
                this.baodanStatus = baodanStatus;
            }

            public String getBaodanStatusName() {
                return baodanStatusName;
            }

            public void setBaodanStatusName(String baodanStatusName) {
                this.baodanStatusName = baodanStatusName;
            }

            public String getCostSum() {
                return costSum;
            }

            public void setCostSum(String costSum) {
                this.costSum = costSum;
            }

            public String getCreatetime() {
                return createtime;
            }

            public void setCreatetime(String createtime) {
                this.createtime = createtime;
            }

            public String getCreateuser() {
                return createuser;
            }

            public void setCreateuser(String createuser) {
                this.createuser = createuser;
            }

            public String getDelFlag() {
                return delFlag;
            }

            public void setDelFlag(String delFlag) {
                this.delFlag = delFlag;
            }

            public String getEnId() {
                return enId;
            }

            public void setEnId(String enId) {
                this.enId = enId;
            }

            public String getEnName() {
                return enName;
            }

            public void setEnName(String enName) {
                this.enName = enName;
            }

            public String getRemark() {
                return remark;
            }

            public void setRemark(String remark) {
                this.remark = remark;
            }

            public String getSeqNo() {
                return seqNo;
            }

            public void setSeqNo(String seqNo) {
                this.seqNo = seqNo;
            }

            public String getSeqNoRange() {
                return seqNoRange;
            }

            public void setSeqNoRange(String seqNoRange) {
                this.seqNoRange = seqNoRange;
            }

            public String getUpdatetime() {
                return updatetime;
            }

            public void setUpdatetime(String updatetime) {
                this.updatetime = updatetime;
            }

            public String getUpdateuser() {
                return updateuser;
            }

            public void setUpdateuser(String updateuser) {
                this.updateuser = updateuser;
            }
        }
    }
}
