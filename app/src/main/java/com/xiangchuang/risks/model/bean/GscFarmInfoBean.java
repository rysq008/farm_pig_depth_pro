package com.xiangchuang.risks.model.bean;

import java.io.Serializable;
import java.util.List;

public class GscFarmInfoBean implements Serializable {

    /**
     * canUse : 1
     * enAddress : 国寿用户地址e6yk1
     * enId : 427
     * enName : 金老板养殖场
     * enUserId : 399
     * enUserName : 国寿用户邓禹翔
     * pids : 
     * sheInfo : [{"pigType":101,"sheName":"222222","juanCnt":0,"insureNo":"34324202495213IOMSN","autoCount":0,"count":0,"dianshuTime":"","enId":427,"endTime":"","sheId":1384,"pigTypeName":"育肥舍"},{"pigType":101,"sheName":"1111111","juanCnt":0,"insureNo":"34324202495213IOMSN","autoCount":0,"count":0,"dianshuTime":"","enId":427,"endTime":"","sheId":1383,"pigTypeName":"育肥舍"}]
     */

    public int canUse;
    public String enAddress;
    public String enId;
    public String enName;
    public String enUserId;
    public String enUserName;
    public String pids;
    public List<SheInfoBean> sheInfo;
    
    public static class SheInfoBean {
        /**
         * pigType : 101
         * sheName : 222222
         * juanCnt : 0
         * insureNo : 34324202495213IOMSN
         * autoCount : 0
         * count : 0
         * dianshuTime : 
         * enId : 427
         * endTime : 
         * sheId : 1384
         * pigTypeName : 育肥舍
         */

        public int pigType;
        public String sheName;
        public int juanCnt;
        public String insureNo;
        public int autoCount;
        public int count;
        public String dianshuTime;
        public int enId;
        public String endTime;
        public int sheId;
        public String pigTypeName;
    }
}
