package com.xiangchuang.risks.model.bean;

import java.util.List;

public class ZhuJuanBean {


    /**
     * data : [{"animalSubType":1,"animalSubTypeName":"肉牛","count":0,"juanId":21,"name":"zhu1","remark":""}]
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
         * animalSubType : 1
         * animalSubTypeName : 肉牛
         * count : 0
         * juanId : 21
         * name : zhu1
         * remark :
         */

        private int animalSubType;
        private String animalSubTypeName;
        private int count;
        private int juanId;
        private String name;
        private String remark;

        public int getAnimalSubType() {
            return animalSubType;
        }

        public void setAnimalSubType(int animalSubType) {
            this.animalSubType = animalSubType;
            if (animalSubType == 101) {
                setAnimalSubTypeName("种猪");
            } else if (animalSubType == 102) {
                setAnimalSubTypeName("育肥猪");
            } else if (animalSubType == 103) {
                setAnimalSubTypeName("能繁母猪");
            }
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
