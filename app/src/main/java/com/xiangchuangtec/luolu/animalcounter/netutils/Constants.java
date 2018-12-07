package com.xiangchuangtec.luolu.animalcounter.netutils;

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


    public static final String BASENEW = "http://47.92.167.61:8084/nongxian3/app/";
//    public static final String BASENEW = "http://192.168.1.198:8081/app/";//测试
    public static final String LOGINURLNEW = BASENEW + "ftnLogin";

    public static final String ZHUSHESHOW = BASENEW + "ftnShe/list";

    public static final String ZHUSHEADD = BASENEW + "ftnShe/add";
    public static final String ZHUSHEUPDATE = BASENEW + "ftnShe/update";
    public static final String ZHUSHEDELETE = BASENEW + "ftnShe/delete";

    public static final String ZHUJUANSHOW = BASENEW + "ftnJuan/list";

    public static final String ZHUJUANUPDATE = BASENEW + "ftnJuan/update";
    public static final String ZHUJUANDELETE = BASENEW + "ftnJuan/delete";
    public static final String ZHUJUANADD = BASENEW + "ftnJuan/add";
    public static final String ZHUJUANOUT = BASENEW + "ftnJuan/out";
    public static final String PRESTART = BASENEW + "payment/prePayStart";

    public static final String PRECOMMIT = BASENEW + "payment/prePayCommit";

    public static final String PREJIXU = BASENEW + "payment/prePayEnd";
    public static final String PRESTOP = BASENEW + "payment/stopVideo";
    public static final String INSURE = BASENEW + "ftnInsure/list";
    public static final String INSURECOMMIT = BASENEW + "ftnInsure/commit";
    public static final String PINZHONG = BASENEW + "ftnJuan/animalType";
    //理赔调取摄像头
    public static final String LiSTART = BASENEW + "payment/payStart";
    public static final String LICOMMIT = BASENEW + "payment/payCommit";
    public static final String LIEDD2 = BASENEW + "payment/payEnd2";
    public static final String LIEDD1 = BASENEW + "payment/payEnd1";
    //判断是否投保
    public static final String CHECKBAODAN = BASENEW + "payment/allStart";
    public static final String POLLINGLIST = BASENEW + "inspect/inspect";
    public static final String XUNJIANTIJIAONEW = BASENEW + "inspect/commit";
    public static final String XUNJIANCHAXUNEW = BASENEW + "inspect/result";
    public static final String XUNFINISHNEW = BASENEW + "inspect/finish";
    public static final String DIANSHULIST = BASENEW + "ftnInsure/list";
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

    public static final String ENLIST = BASENEW + "ftnEnList";
    public static final String companyfleg = "companyfleg";
    public static final String companyuser = "companyuser";
    public static final String insurecompany = "insurecompany";
    public static final String adduser = BASENEW +"addEnAndUser";
    public static final String upload = BASENEW +"uploadImg";
    public static final String SHESHOW = BASENEW + "ftnCamera/list";
    public static final String YANZHENG = BASENEW + "ftnCamera/check";
    public static final String SXADD = BASENEW + "ftnCamera/add";
    public static final String SXUPDATE = BASENEW + "ftnCamera/update";
    public static final String CAMERALIST = BASENEW + "ftnCamera/cameraJuanList";
    public static final String CAMERABINDING = BASENEW + "ftnCamera/cameraJuanBind";

    public static final String SHELIST = BASENEW + "ftnShe/sheList";
    public static final String SHECOMMIT = BASENEW + "inspect/sheCommit";
    public static final String SHEDETAIL_NEW = BASENEW + "inspect/sheDetail";
    public static final String UP_LOAD_IMG = BASENEW + "uploadImg";
    public static final String ADD_PAY_INFO = BASENEW + "payment/addPayInfo";


    //public static final String BASE8081= "http://47.92.167.61:8081/";
    public static final String SHEDETAIL = "http://47.92.167.61:8081/numberCheck/app/sheDetail";




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

    public static final String STARTRECODE = BASENEW + "payment/startVideo";
    public static final String JUANEXIT = BASENEW + "ftnJuan/checkJuan";
    //获取保单号
    public static final String JUANBAONUM = BASENEW + "payment/insureNo";

    public static final String INSURED = BASENEW+"ftnInsure/insure";
    //获取猪种类
    public static final String PIGTYPE = BASENEW+"ftnInsure/pigType";//animalType

    public static final String enUserId = "enUserId";
    public static final String amount = "amount";
    public static final String ratio = "ratio";
    public static final String pigType = "pigType";
    //获取企业信息
    public static final String GETEN = BASENEW+"getEn";
    //获取保单列表
    public static final String INSURELIST= BASENEW+"ftnInsure/insureList";

}
