package com.xiangchuang.risks.model.bean;

import java.util.List;

public class PinZhongBean {

    /**
     * data : [{"animalSubType":101,"animalSubTypeName":"种猪"},{"animalSubType":102,"animalSubTypeName":"育肥猪"},{"animalSubType":103,"animalSubTypeName":"能繁母猪"}]
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
         * animalSubType : 101
         * animalSubTypeName : 种猪
         */

        private int animalSubType;
        private String animalSubTypeName;

        public int getAnimalSubType() {
            return animalSubType;
        }

        public void setAnimalSubType(int animalSubType) {
            this.animalSubType = animalSubType;
        }

        public String getAnimalSubTypeName() {
            return animalSubTypeName;
        }

        public void setAnimalSubTypeName(String animalSubTypeName) {
            this.animalSubTypeName = animalSubTypeName;
        }
    }
}
