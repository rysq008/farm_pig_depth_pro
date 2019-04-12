package com.innovation.pig.insurance.netutils;

import innovation.utils.HttpUtils;

public class Constants {
    //生产
    // public static final String BASE = "http://192.168.2.103:8089/app/";
    //测试
//    public static final String BASE = "http://47.92.167.61:8081/numberCheck/app/";
//    public static final String LOGINURL = BASE + "appLogin";
//    public static final String XUNJIANURL = BASE + "inspect";
//    public static final String XUNJIANTIJIAO = BASE + "commit";
//    public static final String XUNJIANCHAXUN = BASE + "result";
//    public static final String XUNFINISH = BASE + "finish";
//    public static final String NUMFIND = BASE + "search";
    /**
     * 标记是否登录的key
     */
    public static final String ISLOGIN = "islogin";

    public static final String username = "username";
    public static final String password = "password";
    public static final String deptId = "deptId";
    public static final String fullname = "fullname";
    public static final String sty1Id = "sty1Id";
    public static final String sty2Id = "sty2Id";
    public static final String count = "count";
    public static final String manualcount = "manualcount";
    public static final String userid = "userid";
    public static final String no = "no";
    public static final String inspectNo = "inspectNo";
    public static final String file = "file";

    /**
     * 阈值集合
     */
    public static final String THRESHOLD_LIST = "thresholdlist";
    public static String lipeia = "lipeia";//30 ;
    public static String lipeib = "lipeib";//30 ;
    public static String lipein = "lipein";//120 ;
    public static String lipeim = "lipeim";//240 ;
    public static String phone = "kefuphone";
    public static String customServ = "kefucustomServ";



    //    60.205.209.245:8081;   47.92.167.61:8081
//    public static final String BASENEW =  "http://47.92.167.61:8081/nongxian2/app/";
    public static String BASENEW = HttpUtils.baseUrl + "app/";
    public static String LOGINURLNEW = BASENEW + "ftnLogin";
    public static String AAR_LOGINURLNEW = BASENEW + "ftnAarLogin";

    public static String ZHUSHESHOW = BASENEW + "ftnShe/list";

    public static String ZHUSHEADD = BASENEW + "ftnShe/add";
    public static String ZHUSHEUPDATE = BASENEW + "ftnShe/update";
    public static String ZHUSHEDELETE = BASENEW + "ftnShe/delete";

    public static String ZHUJUANSHOW = BASENEW + "ftnJuan/list";

    public static String ZHUJUANUPDATE = BASENEW + "ftnJuan/update";
    public static String ZHUJUANDELETE = BASENEW + "ftnJuan/delete";
    public static String ZHUJUANADD = BASENEW + "ftnJuan/add";
    public static String ZHUJUANOUT = BASENEW + "ftnJuan/out";
    //调起猪舍内的摄像头
    public static String PRESTART = BASENEW + "payment/prePayStart";
    //预先理赔提交
    public static String PRECOMMIT = BASENEW + "payment/prePayCommit";
    //上传预理赔补充视频
    public static String ADDPREPAYVIDEO = BASENEW + "payment/addPrePayVideo";

    public static String PREJIXU = BASENEW + "payment/prePayEnd";
    public static String PRESTOP = BASENEW + "payment/stopVideo";
    public static String INSURE = BASENEW + "ftnInsure/list";
    public static String INSURECOMMIT = BASENEW + "ftnInsure/commit";
    public static String PINZHONG = BASENEW + "ftnJuan/animalType";
    //验证是否存在预理赔对象
    public static String LiSTART = BASENEW + "payment/payStart";
    //理赔上传
    public static String LICOMMIT = BASENEW + "payment/payCommit";
    public static String LIEDD2 = BASENEW + "payment/payEnd2";
    public static String LIEDD1 = BASENEW + "payment/payEnd1";
    // 验证是否存在有效投保
    public static String CHECKBAODAN = BASENEW + "payment/allStart";
    public static String POLLINGLIST = BASENEW + "inspect/inspect";
    public static String XUNJIANTIJIAONEW = BASENEW + "inspect/commit";
    public static String XUNJIANCHAXUNEW = BASENEW + "inspect/result";
    public static String XUNFINISHNEW = BASENEW + "inspect/finish";
    public static String DIANSHULIST = BASENEW + "ftnInsure/list";
    public static final String en_id = "en_id";
    public static final String companyname = "companyname";
    public static final String defaultpig = "defaultpig";
    public static final String AppKeyAuthorization = "AppKeyAuthorization";
    public static final String en_user_id = "en_user_id";
    public static final String sheId = "sheId";
    public static final String name = "name";
    public static final String amountFlg = "amountFlg";
    public static final String insureFlg = "insureFlg";
    public static final String animalSubType = "animalSubType";
    public static final String insureAmount = "insureAmount";
    public static final String juanId = "juanId";
    public static final String account = "account";
    public static final String insureNo = "insureNo";
    public static final String reason = "reason";
    public static final String preCompensateVideoId = "preCompensateVideoId";
    public static final String address = "address";
    public static final String longitude = "longitude";
    public static final String latitude = "latitude";
    public static final String preVideoId = "preCompensateVideoId";
    public static final String lipeiId = "lipeiId";
    public static final String videoId = "videoId";
    public static final String fleg = "fleg";
    public static final String userLibId = "userLibId";
    public static final String animalId = "animalId";
    public static final String cutoCount = "autoCount";
    public static final String inspectId = "inspectId";
    public static final String compensateVideoId = "compensateVideoId";
    public static final String timesFlag = "timesFlag";

    public static String ENLIST = BASENEW + "ftnEnList";
    public static final String companyfleg = "companyfleg";
    public static final String companyuser = "companyuser";
    public static final String insurecompany = "insurecompany";
    public static String adduser = BASENEW + "addEnAndUser";
    public static String upload = BASENEW + "uploadImg";
    public static String SHESHOW = BASENEW + "ftnCamera/list";
    public static String YANZHENG = BASENEW + "ftnCamera/check";
    public static String SXADD = BASENEW + "ftnCamera/add";
    public static String SXUPDATE = BASENEW + "ftnCamera/update";
    public static String CAMERALIST = BASENEW + "ftnCamera/cameraJuanList";
    public static String CAMERABINDING = BASENEW + "ftnCamera/cameraJuanBind";

    public static String SHELIST = BASENEW + "ftnShe/sheList";
    public static String SHECOMMIT = BASENEW + "inspect/sheCommit";
    //盘查详情
    public static String SHEDETAIL_NEW = BASENEW + "inspect/sheDetail";
    public static String UP_LOAD_IMG = BASENEW + "uploadImg";
    public static String ADD_PAY_INFO = BASENEW + "payment/addPayInfo";

    //public static final String BASE8081= "http://47.92.167.61:8081/";
//    public static final String SHEDETAIL = "http://47.92.167.61:8081/numberCheck/app/sheDetail";

    public static final String deptIdnew = "dept_id";
    public static final String id = "id";
    public static final String cameraNo = "cameraNo";
    public static final String cameraName = "cameraName";
    public static final String verificationCode = "verificationCode";
    public static final String xu = "xu";
    public static final String touname = "touname";
    public static final String shename = "shename";
    public static final String juanname = "juanname";
    public static final String cameraId = "cameraId";
    public static final String startVideoId = "startVideoId";
    public static final String token = "token";

    public static String STARTRECODE = BASENEW + "payment/startVideo";
    public static String JUANEXIT = BASENEW + "ftnJuan/checkJuan";
    //获取保单号
    public static String JUANBAONUM = BASENEW + "payment/insureNo";

    public static String INSURED = BASENEW + "ftnInsure/insure";
    //获取猪种类
    public static String PIGTYPE = BASENEW + "ftnInsure/pigType";//animalType

    public static final String enUserId = "enUserId";
    public static final String amount = "amount";
    public static final String ratio = "ratio";
    public static final String pigType = "pigType";
    //获取企业信息
    public static String GETEN = BASENEW + "getEn";
    //获取保单列表
    public static String INSURELIST = BASENEW + "ftnInsure/insureList";
    //获取强制提交时间参数
    public static String QUERY_VIDEOFLAG_NEW = HttpUtils.baseUrl + "appCoreV2/queryVideoFlag";
    //断点续传检查接口
    public static String UPLOAD_CHECK = BASENEW + "upload/check";
    //视频续传接口
    public static String UPLOAD_VIDEO = BASENEW + "upload/lipeiVideo";
    //预理赔强制提交
    public static String PREPAY_FORCE_COMMIT = BASENEW + "payment/prePayForceCommit";
    //理赔强制提交
    public static String PAY_FORCE_COMMIT = BASENEW + "payment/payForceCommit";
    //强制补充信息
    public static String PAY_FORCE_END = BASENEW + "payment/payForceEnd";
    //全局动态弹框提示
    public static String GET_TIPS_DIALOG = HttpUtils.baseUrl + "appNotice/get";

    //获取数量
    public static String NUMBER = BASENEW + "innocuous/number";

    //获取步骤
    public static String DISPOSE_STEP = BASENEW + "innocuous/step";
    //获取待处理列表
    public static String DISPOSE_LIST = BASENEW + "innocuous/list";
    //添加无害化处理
    public static String DISPOSE_START = BASENEW + "innocuous/start";
    //添加忽略处理
    public static String DISPOSE_IGNORE = BASENEW + "innocuous/ignore";
    //获取未完成处理列表
    public static String DISPOSE_UNFINISH = BASENEW + "innocuous/unfinish";
    //无害化步骤处理，提交
    public static String DEADPIG_PROCESS_STEP_COMMIT = BASENEW + "innocuous/commit";

    public static String TOKEY = "token";
    public static String DEPARTMENT_ID = "departmentId";
    public static String USER_ID = "userId";
    public static String NAME = "name";
    public static String PHONE_NUMBER = "phoneNumber";
    public static String IDENTITY_CARD = "identityCard";


    public static void resetBaseIp(String host) {
        HttpUtils.baseUrl = host;
        BASENEW = HttpUtils.baseUrl + "app/";
        LOGINURLNEW = BASENEW + "ftnLogin";
        AAR_LOGINURLNEW = BASENEW + "ftnAarLogin";

        ZHUSHESHOW = BASENEW + "ftnShe/list";

        ZHUSHEADD = BASENEW + "ftnShe/add";
        ZHUSHEUPDATE = BASENEW + "ftnShe/update";
        ZHUSHEDELETE = BASENEW + "ftnShe/delete";

        ZHUJUANSHOW = BASENEW + "ftnJuan/list";

        ZHUJUANUPDATE = BASENEW + "ftnJuan/update";
        ZHUJUANDELETE = BASENEW + "ftnJuan/delete";
        ZHUJUANADD = BASENEW + "ftnJuan/add";
        ZHUJUANOUT = BASENEW + "ftnJuan/out";
//调起猪舍内的摄像头
        PRESTART = BASENEW + "payment/prePayStart";
//预先理赔提交
        PRECOMMIT = BASENEW + "payment/prePayCommit";
//上传预理赔补充视频
        ADDPREPAYVIDEO = BASENEW + "payment/addPrePayVideo";

        PREJIXU = BASENEW + "payment/prePayEnd";
        PRESTOP = BASENEW + "payment/stopVideo";
        INSURE = BASENEW + "ftnInsure/list";
        INSURECOMMIT = BASENEW + "ftnInsure/commit";
        PINZHONG = BASENEW + "ftnJuan/animalType";
//验证是否存在预理赔对象
        LiSTART = BASENEW + "payment/payStart";
//理赔上传
        LICOMMIT = BASENEW + "payment/payCommit";
        LIEDD2 = BASENEW + "payment/payEnd2";
        LIEDD1 = BASENEW + "payment/payEnd1";
// 验证是否存在有效投保
        CHECKBAODAN = BASENEW + "payment/allStart";
        POLLINGLIST = BASENEW + "inspect/inspect";
        XUNJIANTIJIAONEW = BASENEW + "inspect/commit";
        XUNJIANCHAXUNEW = BASENEW + "inspect/result";
        XUNFINISHNEW = BASENEW + "inspect/finish";
        DIANSHULIST = BASENEW + "ftnInsure/list";
        ENLIST = BASENEW + "ftnEnList";
        adduser = BASENEW + "addEnAndUser";
        upload = BASENEW + "uploadImg";
        SHESHOW = BASENEW + "ftnCamera/list";
        YANZHENG = BASENEW + "ftnCamera/check";
        SXADD = BASENEW + "ftnCamera/add";
        SXUPDATE = BASENEW + "ftnCamera/update";
        CAMERALIST = BASENEW + "ftnCamera/cameraJuanList";
        CAMERABINDING = BASENEW + "ftnCamera/cameraJuanBind";

        SHELIST = BASENEW + "ftnShe/sheList";
        SHECOMMIT = BASENEW + "inspect/sheCommit";
//盘查详情
        SHEDETAIL_NEW = BASENEW + "inspect/sheDetail";
        UP_LOAD_IMG = BASENEW + "uploadImg";
        ADD_PAY_INFO = BASENEW + "payment/addPayInfo";
        STARTRECODE = BASENEW + "payment/startVideo";
        JUANEXIT = BASENEW + "ftnJuan/checkJuan";
//获取保单号
        JUANBAONUM = BASENEW + "payment/insureNo";

        INSURED = BASENEW + "ftnInsure/insure";
//获取猪种类
        PIGTYPE = BASENEW + "ftnInsure/pigType";//animalType
        GETEN = BASENEW + "getEn";
//获取保单列表
        INSURELIST = BASENEW + "ftnInsure/insureList";

        //获取强制提交时间参数
        QUERY_VIDEOFLAG_NEW = HttpUtils.baseUrl + "appCoreV2/queryVideoFlag";

        //断点续传检查接口
        UPLOAD_CHECK = BASENEW + "upload/check";
        //视频续传接口
        UPLOAD_VIDEO = BASENEW + "upload/lipeiVideo";
        //预理赔强制提交
        PREPAY_FORCE_COMMIT = BASENEW + "payment/prePayForceCommit";
        //理赔强制提交
        PAY_FORCE_COMMIT = BASENEW + "payment/payForceCommit";
        //强制补充信息
        PAY_FORCE_END = BASENEW + "payment/payForceEnd";

        GET_TIPS_DIALOG = HttpUtils.baseUrl + "appNotice/get";

        NUMBER = BASENEW + "innocuous/number";
        DISPOSE_STEP = BASENEW + "innocuous/step";
        DISPOSE_LIST = BASENEW + "innocuous/list";
        DISPOSE_START = BASENEW + "innocuous/start";
        DISPOSE_IGNORE = BASENEW + "innocuous/ignore";
        DISPOSE_UNFINISH = BASENEW + "innocuous/unfinish";
        DEADPIG_PROCESS_STEP_COMMIT = BASENEW + "innocuous/commit";
    }
}
