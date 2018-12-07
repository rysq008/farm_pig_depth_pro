package com.xiangchuang.risks.model.bean;

import java.util.List;

public class PollingListBean {

    /**
     * data : {"list0":[{"count":140,"insureFlg":"","juanList":[{"animalSubType":"","animalSubTypeName":"","count":140,"juanId":56,"name":"两个猪圈2","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":62,"name":"呃呃","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":63,"name":"1","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":64,"name":"猪圈1","remark":""}],"name":"一个猪舍1","remark":"","sheId":30},{"count":40,"insureFlg":"","juanList":[{"animalSubType":"","animalSubTypeName":"","count":40,"juanId":57,"name":"圈1","remark":""}],"name":"两个猪舍2","remark":"","sheId":33},{"count":0,"insureFlg":"","juanList":[{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":59,"name":"猪圈32","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":60,"name":"猪圈33","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":61,"name":"猪圈34","remark":""}],"name":"猪舍3","remark":"","sheId":34},{"count":0,"insureFlg":"","juanList":[],"name":"猪舍4","remark":"","sheId":35},{"count":0,"insureFlg":"","juanList":[],"name":"猪舍5","remark":"","sheId":36},{"count":0,"insureFlg":"","juanList":[],"name":"猪舍6","remark":"","sheId":37}],"list0Nums":180,"list1":[{"count":210,"insureFlg":"","juanList":[{"animalSubType":"","animalSubTypeName":"","count":210,"juanId":55,"name":"一个猪圈","remark":""}],"name":"一个猪舍1","remark":"","sheId":30},{"count":230,"insureFlg":"","juanList":[{"animalSubType":"","animalSubTypeName":"","count":230,"juanId":58,"name":"猪圈1","remark":""}],"name":"猪舍3","remark":"","sheId":34}],"list1Nums":440,"toBeInsure":180,"totalNums":620}
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
         * list0 : [{"count":140,"insureFlg":"","juanList":[{"animalSubType":"","animalSubTypeName":"","count":140,"juanId":56,"name":"两个猪圈2","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":62,"name":"呃呃","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":63,"name":"1","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":64,"name":"猪圈1","remark":""}],"name":"一个猪舍1","remark":"","sheId":30},{"count":40,"insureFlg":"","juanList":[{"animalSubType":"","animalSubTypeName":"","count":40,"juanId":57,"name":"圈1","remark":""}],"name":"两个猪舍2","remark":"","sheId":33},{"count":0,"insureFlg":"","juanList":[{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":59,"name":"猪圈32","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":60,"name":"猪圈33","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":61,"name":"猪圈34","remark":""}],"name":"猪舍3","remark":"","sheId":34},{"count":0,"insureFlg":"","juanList":[],"name":"猪舍4","remark":"","sheId":35},{"count":0,"insureFlg":"","juanList":[],"name":"猪舍5","remark":"","sheId":36},{"count":0,"insureFlg":"","juanList":[],"name":"猪舍6","remark":"","sheId":37}]
         * list0Nums : 180
         * list1 : [{"count":210,"insureFlg":"","juanList":[{"animalSubType":"","animalSubTypeName":"","count":210,"juanId":55,"name":"一个猪圈","remark":""}],"name":"一个猪舍1","remark":"","sheId":30},{"count":230,"insureFlg":"","juanList":[{"animalSubType":"","animalSubTypeName":"","count":230,"juanId":58,"name":"猪圈1","remark":""}],"name":"猪舍3","remark":"","sheId":34}]
         * list1Nums : 440
         * toBeInsure : 180
         * totalNums : 620
         */

        private int list0Nums;
        private int list1Nums;
        private int toBeInsure;
        private int totalNums;
        private List<List0Bean> list0;
        private List<List1Bean> list1;

        public int getList0Nums() {
            return list0Nums;
        }

        public void setList0Nums(int list0Nums) {
            this.list0Nums = list0Nums;
        }

        public int getList1Nums() {
            return list1Nums;
        }

        public void setList1Nums(int list1Nums) {
            this.list1Nums = list1Nums;
        }

        public int getToBeInsure() {
            return toBeInsure;
        }

        public void setToBeInsure(int toBeInsure) {
            this.toBeInsure = toBeInsure;
        }

        public int getTotalNums() {
            return totalNums;
        }

        public void setTotalNums(int totalNums) {
            this.totalNums = totalNums;
        }

        public List<List0Bean> getList0() {
            return list0;
        }

        public void setList0(List<List0Bean> list0) {
            this.list0 = list0;
        }

        public List<List1Bean> getList1() {
            return list1;
        }

        public void setList1(List<List1Bean> list1) {
            this.list1 = list1;
        }

        public static class List0Bean {
            /**
             * count : 140
             * insureFlg :
             * juanList : [{"animalSubType":"","animalSubTypeName":"","count":140,"juanId":56,"name":"两个猪圈2","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":62,"name":"呃呃","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":63,"name":"1","remark":""},{"animalSubType":"","animalSubTypeName":"","count":0,"juanId":64,"name":"猪圈1","remark":""}]
             * name : 一个猪舍1
             * remark :
             * sheId : 30
             */

            private int count;
            private String insureFlg;
            private String name;
            private String remark;
            private int sheId;
            private List<JuanListBean> juanList;

            public int getCount() {
                return count;
            }

            public void setCount(int count) {
                this.count = count;
            }

            public String getInsureFlg() {
                return insureFlg;
            }

            public void setInsureFlg(String insureFlg) {
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

            public List<JuanListBean> getJuanList() {
                return juanList;
            }

            public void setJuanList(List<JuanListBean> juanList) {
                this.juanList = juanList;
            }

            public static class JuanListBean {
                /**
                 * animalSubType :
                 * animalSubTypeName :
                 * count : 140
                 * juanId : 56
                 * name : 两个猪圈2
                 * remark :
                 */

                private String animalSubType;
                private String animalSubTypeName;
                private int count;
                private int juanId;
                private String name;
                private String remark;

                public String getAnimalSubType() {
                    return animalSubType;
                }

                public void setAnimalSubType(String animalSubType) {
                    this.animalSubType = animalSubType;
                }

                public String getAnimalSubTypeName() {
                    return animalSubTypeName;
                }

                public void setAnimalSubTypeName(String animalSubTypeName) {
                    this.animalSubTypeName = animalSubTypeName;
                }

                public int getCount() {
                    return count;
                }

                public void setCount(int count) {
                    this.count = count;
                }

                public int getJuanId() {
                    return juanId;
                }

                public void setJuanId(int juanId) {
                    this.juanId = juanId;
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
            }
        }

        public static class List1Bean {
            /**
             * count : 210
             * insureFlg :
             * juanList : [{"animalSubType":"","animalSubTypeName":"","count":210,"juanId":55,"name":"一个猪圈","remark":""}]
             * name : 一个猪舍1
             * remark :
             * sheId : 30
             */

            private int count;
            private String insureFlg;
            private String name;
            private String remark;
            private int sheId;
            private List<JuanListBeanX> juanList;

            public int getCount() {
                return count;
            }

            public void setCount(int count) {
                this.count = count;
            }

            public String getInsureFlg() {
                return insureFlg;
            }

            public void setInsureFlg(String insureFlg) {
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

            public List<JuanListBeanX> getJuanList() {
                return juanList;
            }

            public void setJuanList(List<JuanListBeanX> juanList) {
                this.juanList = juanList;
            }

            public static class JuanListBeanX {
                /**
                 * animalSubType :
                 * animalSubTypeName :
                 * count : 210
                 * juanId : 55
                 * name : 一个猪圈
                 * remark :
                 */

                private String animalSubType;
                private String animalSubTypeName;
                private int count;
                private int juanId;
                private String name;
                private String remark;

                public String getAnimalSubType() {
                    return animalSubType;
                }

                public void setAnimalSubType(String animalSubType) {
                    this.animalSubType = animalSubType;
                }

                public String getAnimalSubTypeName() {
                    return animalSubTypeName;
                }

                public void setAnimalSubTypeName(String animalSubTypeName) {
                    this.animalSubTypeName = animalSubTypeName;
                }

                public int getCount() {
                    return count;
                }

                public void setCount(int count) {
                    this.count = count;
                }

                public int getJuanId() {
                    return juanId;
                }

                public void setJuanId(int juanId) {
                    this.juanId = juanId;
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
            }
        }
    }
}
