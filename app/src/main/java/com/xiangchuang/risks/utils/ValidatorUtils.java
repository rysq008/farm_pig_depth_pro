package com.xiangchuang.risks.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Luolu on 2018/9/25.
 * InnovationAI
 * luolu@innovationai.cn
 */

public class ValidatorUtils {

    /**
     * 手机号验证
     * @param  mobileNums
     * @return 验证通过返回true
     */
    public static boolean isMobileNO(String mobileNums) {
        /**
         * 判断字符串是否符合手机号码格式
         * 移动号段: 134,135,136,137,138,139,147,150,151,152,157,158,159,170,178,182,183,184,187,188
         * 联通号段: 130,131,132,145,155,156,170,171,175,176,185,186
         * 电信号段: 133,149,153,170,173,177,180,181,189
         * @param str
         * @return 待检测的字符串
         */
// "[1]"代表下一位为数字可以是几，"[0-9]"代表可以为0-9中的一个，"[5,7,9]"表示可以是5,7,9中的任意一位,[^4]表示除4以外的任何一个,
// \\d{9}"代表后面是可以是0～9的数字，有9位。
        String telRegex = "^((13[0-9])|(14[5,7,9])|(15[^4])|(18[0-9])|(17[0,1,3,5,6,7,8]))\\d{8}$";
        // 验证手机号
        String telRegexNew="^[1](([3][0-9])|([4][5,6,7,8,9])|([5][^4,6,9])|([6][6])|([7][3,4,5,6,7,8])|([8][0-9])|([9][8,9]))[0-9]{8}$";
        if (TextUtils.isEmpty(mobileNums)) {
            return false;
        }
        else {
            return mobileNums.matches(telRegexNew);
        }
    }


    /**
     * 电话号码验证
     * @param phone
     * @return 验证通过返回true
     */
    public static boolean isPhone(String phone) {
        Pattern p1 = null, p2 = null;
        Matcher m = null;
        boolean b = false;
        p1 = Pattern.compile("^[0][0-9]{2,3}-?[0-9]{5,10}$");  // 验证带区号的
        p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");         // 验证没有区号的

        String regexp="^(0[0-9]{2,4})?([2-9][0-9]{5,10})+(-[0-9]{1,4})?$";

//        if (phone.length() > 9) {
//            m = p1.matcher(phone);
//            b = m.matches();
//        } else {
//            m = p2.matcher(phone);
//            b = m.matches();
//        }
        return  phone.matches(regexp);
    }

    /**
     * 我国公民的身份证号码特点如下
     * 1.长度18位
     * 2.第1-17号只能为数字
     * 3.第18位只能是数字或者x
     * 4.第7-14位表示特有人的年月日信息
     * 请实现身份证号码合法性判断的函数，函数返回值：
     * 1.如果身份证合法返回0
     * 2.如果身份证长度不合法返回1
     * 3.如果第1-17位含有非数字的字符返回2
     * 4.如果第18位不是数字也不是x返回3
     * 5.如果身份证号的出生日期非法返回4
     */
    public static boolean validator(String id) {
        String str = "[1-9]{2}[0-9]{4}(19|20)[0-9]{2}"
                + "((0[1-9]{1})|(1[1-2]{1}))((0[1-9]{1})|([1-2]{1}[0-9]{1}|(3[0-1]{1})))"
                + "[0-9]{3}[0-9x]{1}";
        Pattern pattern = Pattern.compile(str);
        return pattern.matcher(id).matches();
    }

    /**
     * 营业执照 统一社会信用代码（15位）
     * @param license
     * @return
     */
    public static boolean isLicense15(String license) {
        if(TextUtils.isEmpty(license)) {
            return false;
        }
        if(license.length() != 15) {
            return false;
        }

        String businesslicensePrex14 = license.substring(0,14);// 获取营业执照注册号前14位数字用来计算校验码
        String businesslicense15 = license.substring(14, license.length());// 获取营业执照号的校验码
        char[] chars = businesslicensePrex14.toCharArray();
        int[] ints = new int[chars.length];
        for(int i=0; i<chars.length;i++) {
            ints[i] = Integer.parseInt(String.valueOf(chars[i]));
        }
        getCheckCode(ints);
        if(businesslicense15.equals(getCheckCode(ints)+"")) {// 比较 填写的营业执照注册号的校验码和计算的校验码是否一致
            return true;
        }
        return false;
    }

    /**
     * 获取 营业执照注册号的校验码
     * @param ints
     * @return
     */
    private static int getCheckCode(int[] ints) {
        if(null != ints && ints.length > 1) {
            int ti = 0;
            int si = 0;// pi|11+ti
            int cj = 0;// （si||10==0？10：si||10）*2
            int pj = 10;// pj=cj|11==0?10:cj|11
            for (int i=0;i<ints.length;i++) {
                ti = ints[i];
                pj = (cj % 11) == 0 ? 10 : (cj % 11);
                si = pj + ti;
                cj = (0 == si % 10 ? 10 : si % 10) * 2;
                if (i == ints.length-1) {
                    pj = (cj % 11) == 0 ? 10 : (cj % 11);
                    return pj == 1 ? 1 : 11 - pj;
                }
            }
        }// end if
        return -1;
    }

    /**
     * 营业执照 统一社会信用代码（18位）
     * @param license
     * @return
     */
    public static boolean isLicense18(String license) {
        if(TextUtils.isEmpty(license)) {
            return false;
        }
        if(license.length() != 18) {
            return false;
        }

        String regex = "^([159Y]{1})([1239]{1})([0-9ABCDEFGHJKLMNPQRTUWXY]{6})([0-9ABCDEFGHJKLMNPQRTUWXY]{9})([0-90-9ABCDEFGHJKLMNPQRTUWXY])$";
        if (!license.matches(regex)) {
            return false;
        }
        String str = "0123456789ABCDEFGHJKLMNPQRTUWXY";
        int[] ws = { 1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28 };
        String[] codes = new String[2];
        codes[0] = license.substring(0, license.length() - 1);
        codes[1] = license.substring(license.length() - 1, license.length());
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += str.indexOf(codes[0].charAt(i)) * ws[i];
        }
        int c18 = 31 - (sum % 31);
        if (c18 == 31) {
            c18 = 'Y';
        } else if (c18 == 30) {
            c18 = '0';
        }
        if (str.charAt(c18) != codes[1].charAt(0)) {
            return false;
        }
        return true;
    }
}

