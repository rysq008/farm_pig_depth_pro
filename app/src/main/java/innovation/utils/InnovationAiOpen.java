package innovation.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.farm.innovation.biz.iterm.Model;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.netutils.Constants;
import com.xiangchuang.risks.view.LoginPigAarActivity;

import org.tensorflow.demo.FarmGlobal;

import java.util.HashMap;
import java.util.Map;

public class InnovationAiOpen {
    public static final int INSURE = 0;
    public static final int PAY = 1;

    private static InnovationAiOpen innovationAiOpen;
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

    public <T> void postEventEvent(T map) {
        Message msg = Message.obtain();
        msg.obj = map;
        for (Map.Entry<Object, Handler> entry : mHandlerMap.entrySet()) {
            entry.getValue().sendMessage(msg);
        }
    }

    public void requestWeightApi(Context context, Bundle bundle, Handler.Callback callback) {
        Intent it = new Intent(context, LoginPigAarActivity.class);
        it.putExtra(Constants.ACTION_BUNDLE, bundle);
        context.startActivity(it);
        if (callback != null)
            addEvent(context, callback);
    }

    public void requestInnovationApi(Context context, String actionId, String userid, String pid, int type, Handler.Callback callback) {
        requestInnovationApi(context, actionId, "", "", "", type, callback);
    }

    public void requestInnovationApi(Context context, String actionId, String userid, String phone, String pid, int type, Handler.Callback callback) {
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
        if (TextUtils.isEmpty(phone)) {
            phone = PreferenceManager.getDefaultSharedPreferences(context).getString("phone", "19000000001");
        } else {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("phone", phone).apply();
        }
        if (TextUtils.isEmpty(pid)) {
            pid = PreferenceManager.getDefaultSharedPreferences(context).getString("pid", "28");
        } else {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pid", pid).apply();
        }
        FarmGlobal.model = (type == 1 ? Model.VERIFY.value() : Model.BUILD.value());
        Toast.makeText(context, "nb", Toast.LENGTH_LONG).show();
        Intent mIntent = new Intent(context, LoginPigAarActivity.class);
        mIntent.putExtra(AppConfig.TOKEY, "android_token");
        mIntent.putExtra(AppConfig.USER_ID, userid/*"android_userid3"*/);
        mIntent.putExtra(AppConfig.PHONE_NUMBER, phone);
        mIntent.putExtra(AppConfig.NAME, "android_name");
        mIntent.putExtra(AppConfig.DEPARTMENT_ID, pid/*"14079900"*//*"android_department"*/);
        mIntent.putExtra(AppConfig.IDENTITY_CARD, "android_identitry");
        mIntent.putExtra(AppConfig.ACTION_ID, actionId);
        context.startActivity(mIntent);
        if (callback != null)
            addEvent(context, callback);
    }
}
