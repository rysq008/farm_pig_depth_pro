package com.xiangchuang.risks.utils;

/**
 * Created by AnHuiNews on 2017/5/18.
 */

public class TimeUtil {

    public static String tansTime(int time) {
        time = time / 1000;
        String s = "";
        if (time / 60 < 10) {
            s += "0" + time / 60;
        } else {
            s += time / 60;
        }
        s += ":";
        if (time % 60 < 10) {
            s += "0" + time % 60;
        } else {
            s += time % 60;
        }
        return s;
    }
}
