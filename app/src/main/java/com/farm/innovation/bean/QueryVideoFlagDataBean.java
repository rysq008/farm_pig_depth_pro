package com.farm.innovation.bean;

/**
 * Created by Luolu on 2018/10/26.
 * InnovationAI
 * luolu@innovationai.cn
 */
public class QueryVideoFlagDataBean {

    /**
     * data : {"toubaoVideoFlag":"0","lipeiVideoFlag":"0"}
     * msg : 查询成功！
     * status : 1
     */

    private DataOffLineBaodanBean data;
    private String msg;
    private int status;

    @Override
    public String toString() {
        return "QueryVideoFlagDataBean{" +
                "data=" + data +
                ", msg='" + msg + '\'' +
                ", status=" + status +
                '}';
    }

    public DataOffLineBaodanBean getData() {
        return data;
    }

    public void setData(DataOffLineBaodanBean data) {
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

    public static class DataOffLineBaodanBean {
        /**
         * toubaoVideoFlag : 0
         * lipeiVideoFlag : 0
         */

        private String toubaoVideoFlag;
        private String lipeiVideoFlag;
        private String leftNum;
        private String middleNum;
        private String rightNum;
        //阈值集合
        private String threshold;
        //客服电话
        private String serviceTelephone;

        public String getThreshold() {
            return threshold;
        }

        public void setThreshold(String threshold) {
            this.threshold = threshold;
        }

        public String getServiceTelephone() {
            return serviceTelephone;
        }

        public void setServiceTelephone(String serviceTelephone) {
            this.serviceTelephone = serviceTelephone;
        }

        public String getToubaoVideoFlag() {
            return toubaoVideoFlag;
        }

        public void setToubaoVideoFlag(String toubaoVideoFlag) {
            this.toubaoVideoFlag = toubaoVideoFlag;
        }

        public String getLipeiVideoFlag() {
            return lipeiVideoFlag;
        }

        public void setLipeiVideoFlag(String lipeiVideoFlag) {
            this.lipeiVideoFlag = lipeiVideoFlag;
        }

        public String getLeftNum() {
            return leftNum;
        }

        public void setLeftNum(String leftNum) {
            this.leftNum = leftNum;
        }

        public String getMiddleNum() {
            return middleNum;
        }

        public void setMiddleNum(String middleNum) {
            this.middleNum = middleNum;
        }

        public String getRightNum() {
            return rightNum;
        }

        public void setRightNum(String rightNum) {
            this.rightNum = rightNum;
        }
    }


    public class thresholdList{

        //"{\"pigtoubao\":\"0.1\",\"piglipei1\":\"0.2\",\"piglipei2\":\"0.3\",
        // \"cowtoubao\":\"0.1\",\"cowlipei1\":\"0.2\",\"cowlipei2\":\"0.3\",
        // \"donkeytoubao\":\"0.1\",\"donkeylipei1\":\"0.2\",\"donkeylipei2\":\"0.3\",
        // \"lipeiA\":\"30\",\"lipeiB\":\"30\",\"lipeiM\":\"240\",\"lipeiN\":\"120\"}",

        private String pigtoubao;
        private String piglipei1;
        private String piglipei2;
        private String cowtoubao;
        private String cowlipei1;
        private String cowlipei2;
        private String donkeytoubao;
        private String donkeylipei1;
        private String donkeylipei2;
        private String lipeiA;
        private String lipeiB;
        private String lipeiM;
        private String lipeiN;
        private String customServ;

        @Override
        public String toString() {
            return "thresholdList{" +
                    "pigtoubao='" + pigtoubao + '\'' +
                    ", piglipei1='" + piglipei1 + '\'' +
                    ", piglipei2='" + piglipei2 + '\'' +
                    ", cowtoubao='" + cowtoubao + '\'' +
                    ", cowlipei1='" + cowlipei1 + '\'' +
                    ", cowlipei2='" + cowlipei2 + '\'' +
                    ", donkeytoubao='" + donkeytoubao + '\'' +
                    ", donkeylipei1='" + donkeylipei1 + '\'' +
                    ", donkeylipei2='" + donkeylipei2 + '\'' +
                    ", lipeiA='" + lipeiA + '\'' +
                    ", lipeiB='" + lipeiB + '\'' +
                    ", lipeiM='" + lipeiM + '\'' +
                    ", lipeiN='" + lipeiN + '\'' +
                    ", customServ='" + customServ + '\'' +
                    '}';
        }

        public String getCustomServ() {
            return customServ;
        }

        public void setCustomServ(String customServ) {
            this.customServ = customServ;
        }

        public String getLipeiA() {
            return lipeiA;
        }

        public void setLipeiA(String lipeiA) {
            this.lipeiA = lipeiA;
        }

        public String getLipeiB() {
            return lipeiB;
        }

        public void setLipeiB(String lipeiB) {
            this.lipeiB = lipeiB;
        }

        public String getLipeiM() {
            return lipeiM;
        }

        public void setLipeiM(String lipeiM) {
            this.lipeiM = lipeiM;
        }

        public String getLipeiN() {
            return lipeiN;
        }

        public void setLipeiN(String lipeiN) {
            this.lipeiN = lipeiN;
        }

        public String getPigtoubao() {
            return pigtoubao;
        }

        public void setPigtoubao(String pigtoubao) {
            this.pigtoubao = pigtoubao;
        }

        public String getPiglipei1() {
            return piglipei1;
        }

        public void setPiglipei1(String piglipei1) {
            this.piglipei1 = piglipei1;
        }

        public String getPiglipei2() {
            return piglipei2;
        }

        public void setPiglipei2(String piglipei2) {
            this.piglipei2 = piglipei2;
        }

        public String getCowtoubao() {
            return cowtoubao;
        }

        public void setCowtoubao(String cowtoubao) {
            this.cowtoubao = cowtoubao;
        }

        public String getCowlipei1() {
            return cowlipei1;
        }

        public void setCowlipei1(String cowlipei1) {
            this.cowlipei1 = cowlipei1;
        }

        public String getCowlipei2() {
            return cowlipei2;
        }

        public void setCowlipei2(String cowlipei2) {
            this.cowlipei2 = cowlipei2;
        }

        public String getDonkeytoubao() {
            return donkeytoubao;
        }

        public void setDonkeytoubao(String donkeytoubao) {
            this.donkeytoubao = donkeytoubao;
        }

        public String getDonkeylipei1() {
            return donkeylipei1;
        }

        public void setDonkeylipei1(String donkeylipei1) {
            this.donkeylipei1 = donkeylipei1;
        }

        public String getDonkeylipei2() {
            return donkeylipei2;
        }

        public void setDonkeylipei2(String donkeylipei2) {
            this.donkeylipei2 = donkeylipei2;
        }
    }

}
