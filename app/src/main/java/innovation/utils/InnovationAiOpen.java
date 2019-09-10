package innovation.utils;

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

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.QueryVideoFlagDataBean;
import com.farm.innovation.bean.ResultBean;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.HttpUtils;
import com.google.gson.Gson;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.view.LoginPigAarActivity;

import org.tensorflow.demo.FarmGlobal;
import org.tensorflow.demo.Global;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class InnovationAiOpen {
    public static final int INSURE = 0;
    public static final int PAY = 1;

    private Map<Object, Handler> mHandlerMap = new HashMap<>();

    private static class Holder {
        public static InnovationAiOpen instance = new InnovationAiOpen();
    }

    public static InnovationAiOpen getInstance() {
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
//        Message msg = Message.obtain();
//        msg.obj = obj;
        for (Map.Entry<Object, Handler> entry : mHandlerMap.entrySet()) {
            Handler handler = entry.getValue();
//            if(handler.obtainMessage(msg.what,msg.obj)!=null){
//                Message _msg = new Message();
//                _msg.what = msg.what;
//                _msg.obj= msg.obj;
//                msg = _msg;
//            }
            Message msg = new Message();
            msg.obj = obj;
            handler.sendMessage(msg);
        }
    }

    public void requestWeightApi(Context context, Bundle bundle, Handler.Callback callback) {
        Intent it = new Intent(context, LoginPigAarActivity.class);
        it.putExtras(bundle);
        context.startActivity(it);
        if (callback != null)
            addEvent(context, callback);
    }

    public void requestInnovationApi(Context context, String actionId, String userid, String officeCode, String officeName, String parentOfficeName, String parentOfficeCodes, int type, String idcard, String username, Handler.Callback callback) {
        requestInnovationApi(context, actionId, userid, officeCode, officeName, "", "", parentOfficeName, parentOfficeCodes, type, "", idcard, username, callback);
    }

    /**
     * @param context
     * @param actionId          任务号或者保单号（必填）
     * @param userid            userid 用户id（必填）
     * @param officeCode        officeCode 机构编码（必填）
     * @param officeName        officeName 机构名称（必填）
     * @param officeLevel       officeLevel 机构层级
     * @param parentCode        parentCode  父机构编码
     * @param parentOfficeName  机构层级（必填）
     * @param parentOfficeCodes 机构层级编码（必填）
     * @param type              操作类型（必填）
     * @param phone             手机号
     * @param idcard            身份证号（必填）
     * @param username          用户名（必填）
     * @param callback
     */
    public void requestInnovationApi(Context context, String actionId, String userid, String officeCode, String officeName, String officeLevel, String parentCode, String parentOfficeName, String parentOfficeCodes, int type, String phone, String idcard, String username, Handler.Callback callback) {
        if (type != 0 && type != 1) {
            Toast.makeText(context, "无效业务类型", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(userid)) {
            Toast.makeText(context, "用户id不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(actionId)) {
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
        if (TextUtils.isEmpty(idcard)) {
            Toast.makeText(context, "身份证号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(context, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            phone = "";
        }

        FarmGlobal.model = (type == 1 ? Model.VERIFY.value() : Model.BUILD.value());
        Global.model = FarmGlobal.model;
//        Toast.makeText(context, "nb", Toast.LENGTH_LONG).show();
        String finalPhone = phone;
        queryVideoFlag(context, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {
                    if (callback != null)
                        addEvent(context, callback);
                    skipTo(context, actionId, userid, officeCode, officeName, officeLevel, parentCode, parentOfficeName, parentOfficeCodes, type, finalPhone, idcard, username);
                } else {
                    AlertDialog.Builder builder14 = new AlertDialog.Builder(context)
                            .setIcon(R.drawable.farm_cowface)
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

    private void skipTo(Context context, String actionId, String userid, String officeCode, String officeName,
                        String officeLevel, String parentCode, String parentOfficeName, String parentOfficeCodes, int type, String phone, String idcard, String username) {
        Intent mIntent = new Intent(context, LoginPigAarActivity.class);
        mIntent.putExtra(FarmAppConfig.ACTION_ID, actionId);
        mIntent.putExtra(FarmAppConfig.USER_ID, userid);
        mIntent.putExtra(FarmAppConfig.OFFICE_CODE, officeCode);
        mIntent.putExtra(FarmAppConfig.OFFICE_NAME, officeName);
        mIntent.putExtra(FarmAppConfig.OFFICE_LEVEL, officeLevel);
        mIntent.putExtra(FarmAppConfig.PARENT_CODE, parentCode);
        mIntent.putExtra(FarmAppConfig.PARENT_OFFICE_NAMES, parentOfficeName);
        mIntent.putExtra(FarmAppConfig.PARENT_OFFICE_CODES, parentOfficeCodes);
        mIntent.putExtra(FarmAppConfig.TYPE, type + "");
        mIntent.putExtra(FarmAppConfig.PHONE, phone);
        mIntent.putExtra(FarmAppConfig.ID_CARD, idcard);
        mIntent.putExtra(FarmAppConfig.USER_NAME, username);
        mIntent.putExtra(FarmAppConfig.TOKEY, "android_token");
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
                    String queryVideoFlagResponse = HttpUtils.post(HttpUtils.QUERY_VIDEOFLAG_NEW, formBody);
                    Log.e("queryVideoFlag", "queryVideoFlag: START");
                    if (queryVideoFlagResponse != null) {
                        Gson gson = new Gson();
                        ResultBean queryVideoFlagResultBean = gson.fromJson(queryVideoFlagResponse, ResultBean.class);
                        if (queryVideoFlagResultBean.getStatus() == 1) {
                            QueryVideoFlagDataBean queryVideoFlagData = gson.fromJson(queryVideoFlagResponse, QueryVideoFlagDataBean.class);
                            FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.touBaoVieoFlag, queryVideoFlagData.getData().getToubaoVideoFlag(), context);
                            //  FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.touBaoVieoFlag, 1 + "", context);
                            FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.liPeiVieoFlag, queryVideoFlagData.getData().getLipeiVideoFlag(), context);

                            QueryVideoFlagDataBean.thresholdList thresholdList = gson.fromJson(queryVideoFlagData.getData().getThreshold(), QueryVideoFlagDataBean.thresholdList.class);

                            //存储理赔的时间条件信息
                            FarmerPreferencesUtils.saveIntValue(FarmAppConfig.lipeia, Integer.parseInt(thresholdList.getLipeiA()), context);
                            FarmerPreferencesUtils.saveIntValue(FarmAppConfig.lipeib, Integer.parseInt(thresholdList.getLipeiB()), context);
                            FarmerPreferencesUtils.saveIntValue(FarmAppConfig.lipein, Integer.parseInt(thresholdList.getLipeiN()), context);
                            FarmerPreferencesUtils.saveIntValue(FarmAppConfig.lipeim, Integer.parseInt(thresholdList.getLipeiM()), context);

                            FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.phone, queryVideoFlagData.getData().getServiceTelephone(), context);
                            FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.customServ, thresholdList.getCustomServ(), context);

                            FarmerPreferencesUtils.saveKeyValue(FarmAppConfig.THRESHOLD_LIST, queryVideoFlagData.getData().getThreshold(), context);
                            if (null != queryVideoFlagData.getData() && !"".equals(queryVideoFlagData.getData())) {
                                String left = (queryVideoFlagData.getData().getLeftNum() == null) ? "8" : queryVideoFlagData.getData().getLeftNum();
                                String middleNum = (queryVideoFlagData.getData().getLeftNum() == null) ? "8" : queryVideoFlagData.getData().getMiddleNum();
                                String rightNum = (queryVideoFlagData.getData().getLeftNum() == null) ? "8" : queryVideoFlagData.getData().getRightNum();
                                FarmerPreferencesUtils.saveKeyValue(FarmerPreferencesUtils.FACE_ANGLE_MAX_LEFT, left, context);
                                FarmerPreferencesUtils.saveKeyValue(FarmerPreferencesUtils.FACE_ANGLE_MAX_MIDDLE, middleNum, context);
                                FarmerPreferencesUtils.saveKeyValue(FarmerPreferencesUtils.FACE_ANGLE_MAX_RIGHT, rightNum, context);

                            }
                            msg.what = 1;
                            msg.obj = queryVideoFlagResultBean.getMsg();
                        } else if (queryVideoFlagResultBean.getStatus() == 0) {
                            msg.what = 0;
                            msg.obj = queryVideoFlagResultBean.getMsg();
                        } else {
                            msg.obj = queryVideoFlagResultBean.getMsg();
                            msg.what = 0;
                        }

                    } else {
                        msg.what = 0;
                        msg.obj = "获取采集阈值失败！";
                    }
                } catch (Exception e) {
                    // Toast.makeText(context, "查看是否录制视频接口异常！", Toast.LENGTH_SHORT).show();
                    AVOSCloudUtils.saveErrorMessage(e, HomeActivity.class.getSimpleName());
                    msg.what = 0;
                    msg.obj = "获取采集阈值失败！";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(Message msg) {
                if (progressDialogs[0] != null)
                    progressDialogs[0].dismiss();
                if (!isCancelled() && msg.what == 1) {
                    msg.what = 1;
                } else {
                    msg.what = 0;
                }
                if (null != callback)
                    callback.handleMessage(msg);

            }
        }.execute();

    }
}
