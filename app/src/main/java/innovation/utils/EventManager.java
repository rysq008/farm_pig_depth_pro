package innovation.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.netutils.Constants;
import com.xiangchuang.risks.view.LoginPigAarActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Lucas.Cui
 * 时   间：2019/5/22
 * 简   述：<功能简述>
 */
public class EventManager {

    private static EventManager eventManager;
//    private Map<OnEventListener, Object> mEventList = new HashMap<>();
    private Map<Object, Handler> mHandlerMap = new HashMap<>();

    public static class Holder {
        public static EventManager instance = new EventManager();
    }

    public static EventManager getInstance() {
        return Holder.instance;
    }

//    public boolean addEvent(Object object, OnEventListener listener) {
//        if (listener == null) return false;
//        mEventList.put(listener, object);
//        return true;
//    }

    public boolean addEvent(Object object, Handler.Callback callback) {
        mHandlerMap.put(object, new Handler(callback));
        return true;
    }

    public boolean removeHandler(Object object) {
        mHandlerMap.remove(object);
        return true;
    }

//    public boolean removeEvent(OnEventListener listener) {
//        if (listener == null) return false;
//        mEventList.remove(listener);
//        return true;
//    }

    public boolean removeEvent(Object object) {
//        Iterator<Map.Entry<OnEventListener, Object>> it = mEventList.entrySet().iterator();
//        while (it.hasNext()) {
//            if (it.next().getValue().equals(object))
//                it.remove();
//        }
        mHandlerMap.remove(object);
        return true;
    }

    public void clearEvent() {
//        mEventList.clear();
        mHandlerMap.clear();
    }

//    public void postEventEvent(Map<String, Object> map) {
//        for (Map.Entry<OnEventListener, Object> listener : mEventList.entrySet()) {
//            listener.getKey().onReceive(listener.getValue(), map);
//        }
//    }

    public <T>void postEventEvent(T map) {
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

    public void requestWeightApi(Context context, String appid, String token, Handler.Callback callback) {
        requestWeightApi(context, "","","", callback);
    }

    public void requestWeightApi(Context context, String phone, String userid, String pid, Handler.Callback callback) {
        if (TextUtils.isEmpty(phone)) {
            phone = PreferenceManager.getDefaultSharedPreferences(context).getString("phone", "19000000001");
        } else {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("phone", phone).apply();
        }
        if (TextUtils.isEmpty(userid)) {
            userid = PreferenceManager.getDefaultSharedPreferences(context).getString("userid", "android_userid6");
        } else {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("userid", userid).apply();
        }
        if (TextUtils.isEmpty(pid)) {
            pid = PreferenceManager.getDefaultSharedPreferences(context).getString("pid", "28");
        } else {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pid", pid).apply();
        }
        Toast.makeText(context, "nb", Toast.LENGTH_LONG).show();
        Intent mIntent = new Intent(context, LoginPigAarActivity.class);
        mIntent.putExtra(AppConfig.TOKEY, "android_token");
        mIntent.putExtra(AppConfig.USER_ID, userid/*"android_userid3"*/);
        mIntent.putExtra(AppConfig.PHONE_NUMBER, phone);
        mIntent.putExtra(AppConfig.NAME, "android_name");
        mIntent.putExtra(AppConfig.DEPARTMENT_ID, pid/*"14079900"*//*"android_department"*/);
        mIntent.putExtra(AppConfig.IDENTITY_CARD, "android_identitry");
        context.startActivity(mIntent);
        if (callback != null)
            addEvent(context, callback);
    }
}
