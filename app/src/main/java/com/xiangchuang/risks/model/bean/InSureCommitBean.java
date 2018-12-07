package com.xiangchuang.risks.model.bean;

public class InSureCommitBean {

    /**
     * data : {"baodanNo":"20180913134855798"}
     * msg : 投保申请0头,验标编号：20180913134855798,请联系保险公司业务员进行投保审核.
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
         * baodanNo : 20180913134855798
         */

        private String baodanNo;

        public String getBaodanNo() {
            return baodanNo;
        }

        public void setBaodanNo(String baodanNo) {
            this.baodanNo = baodanNo;
        }
    }
}
