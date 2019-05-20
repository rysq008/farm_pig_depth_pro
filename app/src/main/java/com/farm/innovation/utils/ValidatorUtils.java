package com.farm.innovation.utils;

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
//        String telRegex = "^((13[0-9])|(14[5,7,9])|(15[^4])|(18[0-9])|(17[0,1,3,5,6,7,8]))\\d{8}$";
//        if (TextUtils.isEmpty(mobileNums)) {
//            return false;
//        }
//        else {
//            return mobileNums.matches(telRegex);
//        }
        String telRegex = "[1][3456789]\\d{9}";// "[1]"代表第1位为数字1,"[358]"代表第二位可以为3、5、8中的一个,"//d{9}"代表后面是可以是0~9的数字,有9位。
        if (TextUtils.isEmpty(mobileNums))
            return false;
        else
            return mobileNums.matches(telRegex);

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
        if (phone.length() > 9) {
            m = p1.matcher(phone);
            b = m.matches();
        } else {
            m = p2.matcher(phone);
            b = m.matches();
        }
        return b;
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
     * 营业执照 统一社会信用代码（18位）
     * @param license
     * @return
     */
    public static boolean isLicense18(String license) {
        if ((license.equals("")) || license.length() != 18) {
            return false;
        }
        return true;
		/* 由于下面的校验导致正确的营业执照号无法通过，暂时关闭校验
        String baseCode = "0123456789ABCDEFGHJKLMNPQRTUWXY";
        char[] baseCodeArray = baseCode.toCharArray();
        Map<Character, Integer> codes = new HashMap<>();
        for (int i = 0; i < baseCode.length(); i++) {
            codes.put(baseCodeArray[i], i);
        }
        char[] businessCodeArray = businessCode.toCharArray();
        Character check = businessCodeArray[17];
        if (baseCode.indexOf(check) == -1) {
            return false;
        }
        int[] wi = {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            Character key = businessCodeArray[i];
            if (baseCode.indexOf(key) == -1) {
                return false;
            }
            sum += (codes.get(key) * wi[i]);
        }
        int value = 31 - sum % 31;
        return value == codes.get(check);
		*/
    }
}

