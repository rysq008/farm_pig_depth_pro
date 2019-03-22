package com.xiangchuang.risks.model.bean;

public class CompanyInfoBean {

    /**
     * data : {"address":"北京市朝阳区顺白路936号靠近私享家大厦","animalType":"","bankBack":"http://47.92.167.61:3389","bankBackFile":"","bankFront":"http://47.92.167.61:3389","bankFrontFile":"","bankName":"中国大银行","bankNo":"123456789","cardBack":"","cardFront":"http://47.92.167.61:3389","cardFrontFile":"","createtime":"2018-09-29 15:45:08","createuser":109,"delFlag":0,"deptId":28,"enId":27,"enLicenseNo":"220104197706242014","enName":"翔创猪场二","enPerson":"各大大","enPhone":"13661069878","remark":"","updatetime":"","updateuser":""}
     * msg : 请求成功
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
         * address : 北京市朝阳区顺白路936号靠近私享家大厦
         * animalType :
         * bankBack : http://47.92.167.61:3389
         * bankBackFile :
         * bankFront : http://47.92.167.61:3389
         * bankFrontFile :
         * bankName : 中国大银行
         * bankNo : 123456789
         * cardBack :
         * cardFront : http://47.92.167.61:3389
         * cardFrontFile :
         * createtime : 2018-09-29 15:45:08
         * createuser : 109
         * delFlag : 0
         * deptId : 28
         * enId : 27
         * enLicenseNo : 220104197706242014
         * enName : 翔创猪场二
         * enPerson : 各大大
         * enPhone : 13661069878
         * remark :
         * updatetime :
         * updateuser :
         */
        private String address;
        private String animalType;
        private String bankBack;
        private String bankBackFile;
        private String bankFront;
        private String bankFrontFile;
        private String bankName;
        private String bankNo;
        private String cardBack;
        private String cardFront;
        private String cardFrontFile;
        private String createtime;
        private int createuser;
        private int delFlag;
        private int deptId;
        private int enId;
        private String enLicenseNo;
        private String enName;
        private String enPerson;
        private String enPhone;
        private String remark;
        private String updatetime;
        private String updateuser;
        private int cardType;

        public int getCardType() {
            return cardType;
        }

        public void setCardType(int cardType) {
            this.cardType = cardType;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAnimalType() {
            return animalType;
        }

        public void setAnimalType(String animalType) {
            this.animalType = animalType;
        }

        public String getBankBack() {
            return bankBack;
        }

        public void setBankBack(String bankBack) {
            this.bankBack = bankBack;
        }

        public String getBankBackFile() {
            return bankBackFile;
        }

        public void setBankBackFile(String bankBackFile) {
            this.bankBackFile = bankBackFile;
        }

        public String getBankFront() {
            return bankFront;
        }

        public void setBankFront(String bankFront) {
            this.bankFront = bankFront;
        }

        public String getBankFrontFile() {
            return bankFrontFile;
        }

        public void setBankFrontFile(String bankFrontFile) {
            this.bankFrontFile = bankFrontFile;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getBankNo() {
            return bankNo;
        }

        public void setBankNo(String bankNo) {
            this.bankNo = bankNo;
        }

        public String getCardBack() {
            return cardBack;
        }

        public void setCardBack(String cardBack) {
            this.cardBack = cardBack;
        }

        public String getCardFront() {
            return cardFront;
        }

        public void setCardFront(String cardFront) {
            this.cardFront = cardFront;
        }

        public String getCardFrontFile() {
            return cardFrontFile;
        }

        public void setCardFrontFile(String cardFrontFile) {
            this.cardFrontFile = cardFrontFile;
        }

        public String getCreatetime() {
            return createtime;
        }

        public void setCreatetime(String createtime) {
            this.createtime = createtime;
        }

        public int getCreateuser() {
            return createuser;
        }

        public void setCreateuser(int createuser) {
            this.createuser = createuser;
        }

        public int getDelFlag() {
            return delFlag;
        }

        public void setDelFlag(int delFlag) {
            this.delFlag = delFlag;
        }

        public int getDeptId() {
            return deptId;
        }

        public void setDeptId(int deptId) {
            this.deptId = deptId;
        }

        public int getEnId() {
            return enId;
        }

        public void setEnId(int enId) {
            this.enId = enId;
        }

        public String getEnLicenseNo() {
            return enLicenseNo;
        }

        public void setEnLicenseNo(String enLicenseNo) {
            this.enLicenseNo = enLicenseNo;
        }

        public String getEnName() {
            return enName;
        }

        public void setEnName(String enName) {
            this.enName = enName;
        }

        public String getEnPerson() {
            return enPerson;
        }

        public void setEnPerson(String enPerson) {
            this.enPerson = enPerson;
        }

        public String getEnPhone() {
            return enPhone;
        }

        public void setEnPhone(String enPhone) {
            this.enPhone = enPhone;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getUpdatetime() {
            return updatetime;
        }

        public void setUpdatetime(String updatetime) {
            this.updatetime = updatetime;
        }

        public String getUpdateuser() {
            return updateuser;
        }

        public void setUpdateuser(String updateuser) {
            this.updateuser = updateuser;
        }
    }
}
