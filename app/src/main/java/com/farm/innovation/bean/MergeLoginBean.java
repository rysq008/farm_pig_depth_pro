package com.farm.innovation.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class MergeLoginBean extends BaseBean<MergeLoginBean.MergeLoginBodyBean> {

    public static class MergeLoginBodyBean implements Serializable {
        @SerializedName("ftnData")
        public PigLoginBean ftnData;
        @SerializedName("nxData")
        public FarmerLoginBean nxData;
    }

    public static class FarmerLoginBean implements Serializable {
        public String card;//130628199804210417,
        public String code;//276917,
        public int codedate;//0,
        public String createtime;//2018-08-20 15;//05;//03,
        public int deptid;//28,
        public String email;//,
        public String fullname;//采集员13,
        public String gsUserId;//,
        public String mobile;//15000000013,
        public String password;//e10adc3949ba59abbe56e057f20f883e,
        public int status;//1,
        public String token;//60246b21e14e45c6b0ecf4db16e6855f,
        public int tokendate;//1642490045,
        public int uid;//165,
        public String updatetime;//2019-04-24 15;//14;//05,
        public String username;//

    }

    public static class PigLoginBean implements Serializable {
        @SerializedName("adminUser")
        public AdminUser adminUser;//
        @SerializedName("enUser")
        public EnUser enUser;//
        public int type;//2

        public static class EnUser implements Serializable {
            public String account;//13522771489,
            public String createtime;//2019-03-06 15;//39;//56,
            public String createuser;//,
            public int delFlag;//0,
            public int enId;//227,
            public String enName;//洲际导弹养殖场,
            public int enUserId;//229,
            public String password;//e10adc3949ba59abbe56e057f20f883e,
            public String remark;//,
            public String updatetime;//,
            public String updateuser;//,
            public String userName;//洲际导弹养殖场采集员

        }

        public static class AdminUser implements Serializable {
            public String account;//15000000001,
            public int deptId;//28,
            public String deptName;//业务部,
            public int id;//221,
            public String name;//android_name,
            public List roleList;//Array[1],
            public List roleNames;//Array[1],
            public List roleTips;//Array[1]
        }
    }
}
