package com.xiangchuang.risks.model.bean;

import java.util.List;

public class InsureListBean {

    /**
     * data : [{"amount":500,"baodanId":117,"baodanNo":"20181128153728292","baodanNoReal":"123123","baodanStatus":1,"baodanStatusName":"已审核","costSum":"","count":"","createtime":"2018-11-28 15:37:28","createuser":104,"delFlag":0,"enId":45,"enName":"","pigType":102,"pigTypeName":"能繁母猪","ratio":2,"remark":"","seqNo":"","seqNoRange":"","term":"20181130-20181230","updatetime":"","updateuser":""},{"amount":2000,"baodanId":122,"baodanNo":"20181129171426373","baodanNoReal":"1233","baodanStatus":1,"baodanStatusName":"已审核","costSum":"","count":"","createtime":"2018-11-29 17:14:26","createuser":122,"delFlag":0,"enId":45,"enName":"","pigType":104,"pigTypeName":"后备猪","ratio":63,"remark":"","seqNo":"","seqNoRange":"","term":"20181130-20181230","updatetime":"2018-11-29 19:08:37","updateuser":122}]
     * msg : 投保成功！
     * status : 1
     */

    private String msg;
    private int status;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * amount : 500
         * baodanId : 117
         * baodanNo : 20181128153728292
         * baodanNoReal : 123123
         * baodanStatus : 1
         * baodanStatusName : 已审核
         * costSum :
         * count :
         * createtime : 2018-11-28 15:37:28
         * createuser : 104
         * delFlag : 0
         * enId : 45
         * enName :
         * pigType : 102
         * pigTypeName : 能繁母猪
         * ratio : 2
         * remark :
         * seqNo :
         * seqNoRange :
         * term : 20181130-20181230
         * updatetime :
         * updateuser :
         */

        private int amount;
        private int baodanId;
        private String baodanNo;
        private String baodanNoReal;
        private int baodanStatus;
        private String baodanStatusName;
        private String costSum;
        private String count;
        private String createtime;
        private int createuser;
        private int delFlag;
        private int enId;
        private String enName;
        private int pigType;
        private String pigTypeName;
        private double ratio;
        private String remark;
        private String seqNo;
        private String seqNoRange;
        private String endTime;
        private String updatetime;
        private String updateuser;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getBaodanId() {
            return baodanId;
        }

        public void setBaodanId(int baodanId) {
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

        public int getBaodanStatus() {
            return baodanStatus;
        }

        public void setBaodanStatus(int baodanStatus) {
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

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public String getCreatetime() {
            return createtime;
        }

        public void setCreatetime(String createtime) {
            this.createtime = createtime;
        }

        public int getCreateuser() {
            return createuser;
        }

        public void setCreateuser(int createuser) {
            this.createuser = createuser;
        }

        public int getDelFlag() {
            return delFlag;
        }

        public void setDelFlag(int delFlag) {
            this.delFlag = delFlag;
        }

        public int getEnId() {
            return enId;
        }

        public void setEnId(int enId) {
            this.enId = enId;
        }

        public String getEnName() {
            return enName;
        }

        public void setEnName(String enName) {
            this.enName = enName;
        }

        public int getPigType() {
            return pigType;
        }

        public void setPigType(int pigType) {
            this.pigType = pigType;
        }

        public String getPigTypeName() {
            return pigTypeName;
        }

        public void setPigTypeName(String pigTypeName) {
            this.pigTypeName = pigTypeName;
        }

        public double getRatio() {
            return ratio;
        }

        public void setRatio(double ratio) {
            this.ratio = ratio;
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

        public String getTerm() {
            return endTime;
        }

        public void setTerm(String endTime) {
            this.endTime = endTime;
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
