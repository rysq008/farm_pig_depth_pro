package com.xiangchuang.risks.utils;

public class CommonUtils {

    public static int parseInt(String s) {
        int result = 0;
        try{
            result  = Integer.parseInt(s);
        } catch (Exception e) {
            // do nothing
        }
        return result;
    }

}
