package innovation.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.biz.iterm.Model;
import com.xiangchuang.risks.view.LoginPigAarActivity;

import org.tensorflow.demo.FarmGlobal;
import org.tensorflow.demo.Global;

import java.util.HashMap;
import java.util.Map;

public class InnovationAiOpen {
    public static final int INSURE = 0;
    public static final int PAY = 1;

    private Map<Object, Handler> mHandlerMap = new HashMap<>();

    public static class Holder {
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
        Message msg = Message.obtain();
        msg.obj = obj;
        for (Map.Entry<Object, Handler> entry : mHandlerMap.entrySet()) {
            entry.getValue().sendMessage(msg);
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
        Toast.makeText(context, "nb", Toast.LENGTH_LONG).show();

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
        if (callback != null)
            addEvent(context, callback);
    }
}
