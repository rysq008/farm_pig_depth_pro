package com.innovation.pig.insurance.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PollingBean {

    @SerializedName("@type")
    private String _$Type115; // FIXME check this code
    private DataBean data;
    private String msg;
    private int status;

    public String get_$Type115() {
        return _$Type115;
    }

    public void set_$Type115(String _$Type115) {
        this._$Type115 = _$Type115;
    }

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
        @SerializedName("@type")
        private String _$Type160; // FIXME check this code
        private String inspectNo;
        private String no;
        private List<RecordListBean> recordList;

        public String get_$Type160() {
            return _$Type160;
        }

        public void set_$Type160(String _$Type160) {
            this._$Type160 = _$Type160;
        }

        public String getInspectNo() {
            return inspectNo;
        }

        public void setInspectNo(String inspectNo) {
            this.inspectNo = inspectNo;
        }

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public List<RecordListBean> getRecordList() {
            return recordList;
        }

        public void setRecordList(List<RecordListBean> recordList) {
            this.recordList = recordList;
        }

        public static class RecordListBean {
            /**
             * name : 20180831002
             * nums : 155
             */

            private String name;
            private int nums;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getNums() {
                return nums;
            }

            public void setNums(int nums) {
                this.nums = nums;
            }
        }
    }
}
