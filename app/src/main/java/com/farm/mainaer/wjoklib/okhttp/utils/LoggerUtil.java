/*
 * Copyright (C) 20015 MaiNaEr All rights reserved
 */
package com.farm.mainaer.wjoklib.okhttp.utils;


import com.farm.innovation.base.FarmAppConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 类/接口描述
 *
 * @author wangjian
 * @date 2016/3/30.
 */
public class LoggerUtil {

    static Logger mLogger = Logger.getLogger("okhttp");

    public static void i(String msg) {
        if(FarmAppConfig.isApkDebugable()){
            mLogger.log(Level.INFO, msg);
        }

    }

    public static void w(String msg) {
        if(FarmAppConfig.isApkDebugable()){
            mLogger.log(Level.WARNING, msg);
        }
    }

}
