package com.xiangchuang.risks.model.bean;

import java.util.List;

public class ZhuSheBean {


    /**
     * data : [{"count":333,"insureFlg":1,"name":"大猪舍","remark":"","sheId":1},{"count":222,"insureFlg":0,"name":"中猪舍","remark":"","sheId":2},{"count":111,"insureFlg":1,"name":"小猪舍","remark":"","sheId":3}]
     * msg : 查询成功
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
         * count : 333
         * insureFlg : 1
         * name : 大猪舍
         * remark :
         * sheId : 1
         */

        private int count;
        private int insureFlg;
        private String name;
        private String remark;
        private int sheId;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getInsureFlg() {
            return insureFlg;
        }

        public void setInsureFlg(int insureFlg) {
            this.insureFlg = insureFlg;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public int getSheId() {
            return sheId;
        }

        public void setSheId(int sheId) {
            this.sheId = sheId;
        }
    }
}
