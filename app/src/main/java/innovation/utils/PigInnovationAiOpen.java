package innovation.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.model.bean.QueryVideoFlagDataBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.PigPreferencesUtils;
import com.xiangchuang.risks.view.LoginFarmAarActivity;
import com.xiangchuang.risks.view.LoginPigAarActivity;
import com.xiangchuang.risks.view.SelectFunctionActivity_new;
import com.xiangchuangtec.luolu.animalcounter.PigAppConfig;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;

import org.tensorflow.demo.Global;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class PigInnovationAiOpen {
    public static final int INSURE = 1;
    public static final int PAY = 2;
    public static final int PAY_POLICY = 3;

    public static String getGscTaskid() {
        return GSC_TASKID;
    }

    private static String GSC_TASKID = null;

    public static int getCurType() {
        return CUR_TYPE;
    }

    private static int CUR_TYPE = 0;

    private Map<Object, Handler> mHandlerMap = new HashMap<>();

    private static class Holder {
        public static PigInnovationAiOpen instance = new PigInnovationAiOpen();
    }

    public static PigInnovationAiOpen getInstance() {
        return Holder.instance;
    }

    public boolean addEvent(Object object, Handler.Callback callback) {
        mHandlerMap.put(object, new Handler(callback));
        return true;
    }

    public boolean removeEvent(Object object) {
        mHandlerMap.remove(object);
        return true;
    }

    public void clearEvent() {
        mHandlerMap.clear();
    }

    public <T> void postEventEvent(T obj) {
        Message msg = (Message) obj;
        for (Map.Entry<Object, Handler> entry : mHandlerMap.entrySet()) {
            Handler handler = entry.getValue();
            Activity act = (Activity) entry.getKey();
//            if(handler.obtainMessage(msg.what,msg.obj)!=null){
//                Message _msg = new Message();
//                _msg.what = msg.what;
//                _msg.obj= msg.obj;
//                msg = _msg;
//            }
            if(AppConfig.isSDK_DEBUG())
                Toast.makeText(act, act.getClass().getSimpleName()+",循环发送结果："+mHandlerMap.size(), Toast.LENGTH_SHORT).show();
            Message _msg = new Message();
            _msg.obj = msg.obj;
            _msg.what = msg.what;
            handler.sendMessage(_msg);
        }
    }


    public void requestWeightApi(Context context, Bundle bundle, Handler.Callback callback) {
        Intent it = new Intent(context, LoginFarmAarActivity.class);
        it.putExtras(bundle);
        context.startActivity(it);
        if (callback != null) {
            addEvent(context, callback);
        }
    }

    public void requestInnovationApi(Context context, String actionId, String userid, String officeCode, String officeName, String officeLevel, String parentCode, String parentOfficeName, String parentOfficeCodes, String farmName, int type, String username, Handler.Callback callback) {
        requestInnovationApi(context, actionId, userid, officeCode, officeName, officeLevel, parentCode, parentOfficeName, parentOfficeCodes, farmName, type, "", "", username, callback);
    }

    /**
     * @param context
     * @param taskId            任务号或者保单号（必填）
     * @param userid            userid 用户id（必填）
     * @param officeCode        officeCode 机构编码（必填）
     * @param officeName        officeName 机构名称（必填）
     * @param officeLevel       officeLevel 机构层级
     * @param parentCode        parentCode  父机构编码
     * @param parentOfficeName  机构层级（必填）
     * @param parentOfficeCodes 机构层级编码（必填）
     * @param farmName          养殖场名称（必填）
     * @param type              操作类型（必填）
     * @param phone             手机号
     * @param idcard            身份证号
     * @param username          用户名（必填）
     * @param callback
     */
    public void requestInnovationApi(Context context, String taskId, String userid, String officeCode, String officeName, String officeLevel, String parentCode, String parentOfficeName, String parentOfficeCodes, String farmName, int type, String phone, String idcard, String username, Handler.Callback callback) {
        if (type != 1 && type != 2 && type != 3) {
            Toast.makeText(context, "无效业务类型", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(userid)) {
            Toast.makeText(context, "用户id不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(taskId)) {
            Toast.makeText(context, type == 0 ? "缺少任务id号" : "缺少保单号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(officeCode)) {
            Toast.makeText(context, "机构编码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(officeName)) {
            Toast.makeText(context, "机构名称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(parentOfficeName)) {
            Toast.makeText(context, "机构层级不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(parentOfficeCodes)) {
            Toast.makeText(context, "机构层级编码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(context, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(farmName)) {
            Toast.makeText(context, "养殖场名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(idcard)) {
//            Toast.makeText(context, "身份证号不能为空", Toast.LENGTH_SHORT).show();
//            return;
            idcard = "";
        }
        if (TextUtils.isEmpty(phone)) {
            phone = "";
        }

        AppConfig.setSdkType(AppConfig.SDK_TYPE.COW);
        Global.model = (type == PAY_POLICY ? PAY : type);//(type == 1 ? Model.VERIFY.value() : Model.BUILD.value());
//        Toast.makeText(context, "nb", Toast.LENGTH_LONG).show();
        String finalPhone = phone;
        String finalIdcard = idcard;
        queryVideoFlag(context, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {
                    if (callback != null) {
                        addEvent(context, callback);
                    }
                    skipTo(context, taskId, userid, officeCode, officeName, officeLevel, parentCode, parentOfficeName, parentOfficeCodes, farmName, type, finalPhone, finalIdcard, username);
                } else {
                    AlertDialog.Builder builder14 = new AlertDialog.Builder(context)
                            .setIcon(R.drawable.pig_ic_launcher)
                            .setTitle("提示")
                            .setMessage((CharSequence) msg.obj)
                            .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder14.setCancelable(false);
                    builder14.show();

                }
                return false;
            }
        });

    }

    private void skipTo(Context context, String taskId, String userid, String officeCode, String officeName,
                        String officeLevel,
                        String parentCode, String parentOfficeName, String parentOfficeCodes,
                        String farmName, int type, String phone, String idcard, String username) {
        GSC_TASKID = taskId;
        CUR_TYPE = type;
        Intent mIntent = new Intent(context, LoginPigAarActivity.class);
        mIntent.putExtra(PigAppConfig.TASK_ID, taskId);
        mIntent.putExtra(PigAppConfig.USER_ID, userid);
        mIntent.putExtra(PigAppConfig.OFFICE_CODE, officeCode);
        mIntent.putExtra(PigAppConfig.OFFICE_NAME, officeName);
        mIntent.putExtra(PigAppConfig.OFFICE_LEVEL, officeLevel);
        mIntent.putExtra(PigAppConfig.PARENT_CODE, parentCode);
        mIntent.putExtra(PigAppConfig.PARENT_OFFICE_NAMES, parentOfficeName);
        mIntent.putExtra(PigAppConfig.PARENT_OFFICE_CODES, parentOfficeCodes);
        mIntent.putExtra(PigAppConfig.TYPE, type + "");
        mIntent.putExtra(PigAppConfig.PHONE, phone);
        mIntent.putExtra(PigAppConfig.ID_CARD, idcard);
        mIntent.putExtra(PigAppConfig.USER_NAME, username);
        mIntent.putExtra(PigAppConfig.FARM_NAME, farmName);
        mIntent.putExtra(PigAppConfig.TOKEY, "android_token");
        context.startActivity(mIntent);
    }

    private void queryVideoFlag(Context context, Handler.Callback callback) {
        new AsyncTask<Void, Void, Message>() {
            final ProgressDialog[] progressDialogs = new ProgressDialog[1];
            AsyncTask me;

            @Override
            protected void onPreExecute() {
                me = this;
                super.onPreExecute();
                progressDialogs[0] = ProgressDialog.show(context, "", "数据初始化中。。。");
                progressDialogs[0].setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            me.cancel(true);
                            progressDialogs[0].dismiss();
                            return true;
                        }
                        return false;
                    }
                });
            }

            @Override
            protected Message doInBackground(Void... voids) {
                Message msg = Message.obtain();
                try {
                    TreeMap<String, String> treeMapQueryVideoFlag = new TreeMap();
                    treeMapQueryVideoFlag.put("", "");
                    FormBody.Builder builder = new FormBody.Builder();
                    for (TreeMap.Entry<String, String> entry : treeMapQueryVideoFlag.entrySet()) {
                        builder.add(entry.getKey(), entry.getValue());
                    }
                    RequestBody formBody = builder.build();
                    String queryVideoFlagResponse = HttpUtils.post(Constants.QUERY_VIDEOFLAG_NEW, formBody);
//                    OkHttp3Util.doPost(Constants.QUERY_VIDEOFLAG_NEW, null, new Callback() {
                    Log.e("queryVideoFlag", "queryVideoFlag: START");
                    if (queryVideoFlagResponse != null) {
                        final QueryVideoFlagDataBean queryVideoFlagData = (QueryVideoFlagDataBean) GsonUtils.getBean(queryVideoFlagResponse, QueryVideoFlagDataBean.class);
                        if (queryVideoFlagData.getStatus() == 1) {
                            QueryVideoFlagDataBean.thresholdList thresholdList = (QueryVideoFlagDataBean.thresholdList) GsonUtils.getBean(queryVideoFlagData.getData().getThreshold(), QueryVideoFlagDataBean.thresholdList.class);
                            Log.e(SelectFunctionActivity_new.TAG, "queryVideoFlag thresholdList: " + thresholdList.toString());
                            PigPreferencesUtils.saveIntValue(Constants.lipeia, Integer.parseInt(thresholdList.getLipeiA()), context);
                            PigPreferencesUtils.saveIntValue(Constants.lipeib, Integer.parseInt(thresholdList.getLipeiB()), context);
                            PigPreferencesUtils.saveIntValue(Constants.lipein, Integer.parseInt(thresholdList.getLipeiN()), context);
                            PigPreferencesUtils.saveIntValue(Constants.lipeim, Integer.parseInt(thresholdList.getLipeiM()), context);
                            PigPreferencesUtils.saveKeyValue(Constants.phone, queryVideoFlagData.getData().getServiceTelephone(), context);
                            PigPreferencesUtils.saveKeyValue(Constants.customServ, thresholdList.getCustomServ(), context);
                            PigPreferencesUtils.saveKeyValue("thresholdlist", queryVideoFlagData.getData().getThreshold(), context);
                            if (null != queryVideoFlagData.getData() && !"".equals(queryVideoFlagData.getData())) {
                                String left = queryVideoFlagData.getData().getLeftNum() == null ? "8" : queryVideoFlagData.getData().getLeftNum();
                                String middleNum = queryVideoFlagData.getData().getLeftNum() == null ? "8" : queryVideoFlagData.getData().getMiddleNum();
                                String rightNum = queryVideoFlagData.getData().getLeftNum() == null ? "8" : queryVideoFlagData.getData().getRightNum();
                                PigPreferencesUtils.saveKeyValue("leftNum", left, context);
                                PigPreferencesUtils.saveKeyValue("middleNum", middleNum, context);
                                PigPreferencesUtils.saveKeyValue("rightNum", rightNum, context);
                            }
                            msg.what = 1;
                            msg.obj = queryVideoFlagData.getMsg();
                        } else if (queryVideoFlagData.getStatus() == 0) {
                            msg.what = 0;
                            msg.obj = queryVideoFlagData.getMsg();
                        } else {
                            msg.obj = queryVideoFlagData.getMsg();
                            msg.what = 0;
                        }

                    } else {
                        msg.what = 0;
                        msg.obj = "获取采集阈值失败！";
                    }
                } catch (Exception e) {
                    // Toast.makeText(context, "查看是否录制视频接口异常！", Toast.LENGTH_SHORT).show();
                    AVOSCloudUtils.saveErrorMessage(e, context.getClass().getSimpleName());
                    msg.what = 0;
                    msg.obj = "获取采集阈值失败！";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(Message msg) {
                if (progressDialogs[0] != null) {
                    progressDialogs[0].dismiss();
                }
                if (!isCancelled() && msg.what == 1) {
                    msg.what = 1;
                } else {
                    msg.what = 0;
                }
                if (null != callback) {
                    callback.handleMessage(msg);
                }

            }
        }.execute();

    }
}
