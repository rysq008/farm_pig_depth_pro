package com.xiangchuang.risks.model.bean;

import java.util.List;

public class LoginBean {

    /**
     * data : {"adminUser":"","enUser":{"account":"333","createtime":"2018-09-14 15:15:37","createuser":1,"delFlag":0,"enId":9,"enName":"理赔测试","enUserId":7,"password":"202cb962ac59075b964b07152d234b70","remark":"","updatetime":"","updateuser":"","userName":"脑瓜疼"},"type":2}
     * msg : 登录成功
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
         * adminUser :
         * enUser : {"account":"333","createtime":"2018-09-14 15:15:37","createuser":1,"delFlag":0,"enId":9,"enName":"理赔测试","enUserId":7,"password":"202cb962ac59075b964b07152d234b70","remark":"","updatetime":"","updateuser":"","userName":"脑瓜疼"}
         * type : 2
         */

        private AdminUserBean adminUser;
        private EnUserBean enUser;
        private int type;

        public AdminUserBean getAdminUser() {
            return adminUser;
        }

        public void setAdminUser(AdminUserBean adminUser) {
            this.adminUser = adminUser;
        }

        public EnUserBean getEnUser() {
            return enUser;
        }

        public void setEnUser(EnUserBean enUser) {
            this.enUser = enUser;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public static class EnUserBean {
            /**
             * account : 333
             * createtime : 2018-09-14 15:15:37
             * createuser : 1
             * delFlag : 0
             * enId : 9
             * enName : 理赔测试
             * enUserId : 7
             * password : 202cb962ac59075b964b07152d234b70
             * remark :
             * updatetime :
             * updateuser :
             * userName : 脑瓜疼
             */

            private String account;
            private String createtime;
            private int createuser;
            private int delFlag;
            private int enId;
            private String enName;
            private int enUserId;
            private String password;
            private String remark;
            private String updatetime;
            private String updateuser;
            private String userName;

            public String getAccount() {
                return account;
            }

            public void setAccount(String account) {
                this.account = account;
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

            public int getEnId() {
                return enId;
            }

            public void setEnId(int enId) {
                this.enId = enId;
            }

            public String getEnName() {
                return enName;
            }

            public void setEnName(String enName) {
                this.enName = enName;
            }

            public int getEnUserId() {
                return enUserId;
            }

            public void setEnUserId(int enUserId) {
                this.enUserId = enUserId;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
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

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }
        }
        public static class AdminUserBean {
            /**
             * account : lxr
             * deptId : 25
             * deptName : 开发部
             * id : 136
             * name : 666
             * roleList : [14,15,16]
             * roleNames : ["翔创业务采集员","翔创业务核保员","翔创业务核赔员"]
             * roleTips : ["caiji","hebao","hepei"]
             */

            private String account;
            private int deptId;
            private String deptName;
            private int id;
            private String name;
            private List<Integer> roleList;
            private List<String> roleNames;
            private List<String> roleTips;

            public String getAccount() {
                return account;
            }

            public void setAccount(String account) {
                this.account = account;
            }

            public int getDeptId() {
                return deptId;
            }

            public void setDeptId(int deptId) {
                this.deptId = deptId;
            }

            public String getDeptName() {
                return deptName;
            }

            public void setDeptName(String deptName) {
                this.deptName = deptName;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<Integer> getRoleList() {
                return roleList;
            }

            public void setRoleList(List<Integer> roleList) {
                this.roleList = roleList;
            }

            public List<String> getRoleNames() {
                return roleNames;
            }

            public void setRoleNames(List<String> roleNames) {
                this.roleNames = roleNames;
            }

            public List<String> getRoleTips() {
                return roleTips;
            }

            public void setRoleTips(List<String> roleTips) {
                this.roleTips = roleTips;
            }
        }
    }
}
