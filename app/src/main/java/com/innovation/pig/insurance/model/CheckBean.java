package com.innovation.pig.insurance.model;

import java.util.List;

public class CheckBean {

    /**
     * data : {"num":4,"resultDetails":[{"name":"超级无敌猪舍","nums":4,"records":[{"name":"超级猪圈1","nums":2},{"name":"超级猪圈2","nums":2}]},{"name":"一般般小猪舍","nums":0,"records":[{"name":"小小猪圈1","nums":-1},{"name":"小小猪圈2","nums":1}]}]}
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
         * num : 4
         * resultDetails : [{"name":"超级无敌猪舍","nums":4,"records":[{"name":"超级猪圈1","nums":2},{"name":"超级猪圈2","nums":2}]},{"name":"一般般小猪舍","nums":0,"records":[{"name":"小小猪圈1","nums":-1},{"name":"小小猪圈2","nums":1}]}]
         */

        private int num;
        private List<ResultDetailsBean> resultDetails;

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public List<ResultDetailsBean> getResultDetails() {
            return resultDetails;
        }

        public void setResultDetails(List<ResultDetailsBean> resultDetails) {
            this.resultDetails = resultDetails;
        }

        public static class ResultDetailsBean {
            /**
             * name : 超级无敌猪舍
             * nums : 4
             * records : [{"name":"超级猪圈1","nums":2},{"name":"超级猪圈2","nums":2}]
             */

            private String name;
            private int nums;
            private List<RecordsBean> records;

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

            public List<RecordsBean> getRecords() {
                return records;
            }

            public void setRecords(List<RecordsBean> records) {
                this.records = records;
            }

            public static class RecordsBean {
                /**
                 * name : 超级猪圈1
                 * nums : 2
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
}
