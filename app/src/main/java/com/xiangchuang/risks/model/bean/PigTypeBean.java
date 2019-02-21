package com.xiangchuang.risks.model.bean;


import java.util.List;

public class PigTypeBean {

    /**
     * data : [ {"count":"20","pigType":101,"pigTypeName":"育肥猪"},
     *          {"count":"未点数","pigType":102,"pigTypeName":"能繁母猪"}]
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
         * count : 20
         * pigType : 101
         * pigTypeName : 育肥猪
         */

        private String count;
        private int pigType;
        private String pigTypeName;

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
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
    }
}
