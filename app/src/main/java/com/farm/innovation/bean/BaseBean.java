package com.farm.innovation.bean;

import java.io.Serializable;

public class BaseBean<T> implements Serializable {
    /**
     * data : {"libId ":123456,"picQuality":1}
     * msg : 新增保单成功
     * status : 1
     */

    public T data;
    public String msg;
    public int status;

    public boolean isSuccess(){
        return status == 1;
    }
}
