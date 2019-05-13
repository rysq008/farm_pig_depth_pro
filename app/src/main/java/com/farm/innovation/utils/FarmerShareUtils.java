package com.farm.innovation.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.os.SystemClock;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.MergeLoginBean;
import com.farm.innovation.login.RespObject;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.utils.ShareUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static android.util.Base64.DEFAULT;
import static com.xiangchuang.risks.utils.ShareUtils.preferences_pig;

public class FarmerShareUtils {

    public static SharedPreferences preferences_farm;
    public static String MERGE_LOGIN_INFO = "merge_login_info";

    public static final void init(Context context) {
//        preferences_farm = PreferenceManager.getDefaultSharedPreferences(context);
//        preferences_farm = context.getSharedPreferences("yabinlee",Context.MODE_PRIVATE);
        preferences_farm = context.getSharedPreferences(context.getPackageName() + "_farm", Context.MODE_PRIVATE);
    }

    public static final String getHost(String key) {
//        return null == preferences_farm ? "http://60.205.209.245:8081/nongxian2/" : preferences_farm.getString(key, "http://60.205.209.245:8081/nongxian2/");
        return null == preferences_farm ? "http://47.92.167.61:8081/nongxian2/" : preferences_farm.getString(key, "http://47.92.167.61:8081/nongxian2/");
    }

    public static final boolean saveHost(String key, String val) {
        if(!FarmAppConfig.isOriginApk()){
            if(ShareUtils.saveHost(key,val)){
                return preferences_farm.edit().putString(key, val).commit();
            }
        }
        return preferences_farm.edit().putString(key, val).commit();
    }

    public static final void saveString(String key, String val) {
        preferences_farm.edit().putString(key, val).apply();
    }

    public static final void saveInt(String key, Integer val) {
        preferences_farm.edit().putInt(key, val).apply();
    }

    public static final void saveBool(String key, Boolean val) {
        preferences_farm.edit().putBoolean(key, val).apply();
    }

    public static final String getString(String key) {
        return preferences_farm.getString(key, "");
    }

    public static final int getInt(String key) {
        return preferences_farm.getInt(key, 0);
    }

    public static final boolean getBool(String key) {
        return preferences_farm.getBoolean(key, false);
    }

    public static final List<String> getIPList() {
        List<String> list = new ArrayList<>();
        try {
            Map<String, Object> map = (Map<String, Object>) preferences_farm.getAll();
            for (Map.Entry entry : map.entrySet()) {
                if (entry.getValue().equals("ip")) {
                    list.add(entry.getKey().toString());
                }
            }
        } catch (Exception e) {
        }
        return list;
    }

    public static final void saveData(String key, Object obj) {
        if (obj instanceof Serializable) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(obj);
                String str = Base64.encodeToString(baos.toByteArray(), DEFAULT);
                preferences_farm.edit().putString(key, str).commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static final void clearMergeLoginInfo() {
        preferences_farm.edit().remove(MERGE_LOGIN_INFO).commit();
    }

    public static final <T> T getData(String key) {
        try {
            String str = preferences_farm.getString(key, "");
            if (TextUtils.isEmpty(str)) return null;
            byte[] bytes = Base64.decode(str.getBytes(), DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isCommpayUser() {
        MergeLoginBean bean = getData(MERGE_LOGIN_INFO);
        if (bean.data.nxData != null && !TextUtils.isEmpty(bean.data.nxData.token) && bean.data.nxData.status == RespObject.USER_STATUS_1) {
            return true;
        }
        return false;
    }

    public static final void setUpGlobalHost(Context ct, View view) {
        view.setOnClickListener(new View.OnClickListener() {
            final static int COUNTS = 5;//点击次数
            final static long DURATION = 1 * 1000;//规定有效时间
            long[] mHits = new long[COUNTS];

            @Override
            public void onClick(View v) {
                /**
                 * 实现双击方法
                 * src 拷贝的源数组
                 * srcPos 从源数组的那个位置开始拷贝.
                 * dst 目标数组
                 * dstPos 从目标数组的那个位子开始写数据
                 * length 拷贝的元素的个数
                 */
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
//                    String tips = "您已在[" + DURATION + "]ms内连续点击【" + mHits.length + "】次了！！！";
//                    Toast.makeText(ct, tips, Toast.LENGTH_SHORT).show();
                    final EditText edittext = new EditText(ct);

                    new AlertDialog.Builder(ct).setTitle("提示").setMessage("请输入6位数密码").
                            setView(edittext).setCancelable(false).
                            setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    String pwd = edittext.getText().toString().trim();
//                                    Toast.makeText(ct, "pwd=" + pwd, Toast.LENGTH_SHORT).show();

//                                    if ("http://60.205.209.245:8081/nongxian2/".equals(HttpUtils.baseUrl)) {
//                                        Toast.makeText(ct, "当前处于正式环境", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Toast.makeText(ct, "当前处于测试环境", Toast.LENGTH_SHORT).show();
//                                    }
                                    if (pwd.equals("321")) {

                                        EditText et = new EditText(ct);
                                        Drawable drawable = ct.getResources().getDrawable(R.drawable.farm_detail_01);
                                        et.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                                        List<String> slist = new ArrayList<>();
                                        final String[] list = {"http://60.205.209.245:8081/nongxian2/", "http://47.92.167.61:8081/nongxian2/"};//要填充的数据
                                        slist.addAll(Arrays.asList(list));
                                        slist.addAll(FarmerShareUtils.getIPList());
                                        AlertDialog enterDialog = new AlertDialog.Builder(ct).setTitle("提示").setMessage("请转入地址")
                                                .setView(et).setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                        String str = et.getText().toString().trim();
                                                        if (TextUtils.isEmpty(str)) return;
                                                        if (!slist.contains(str))
                                                            FarmerShareUtils.saveString(et.getText().toString().trim(), "ip");
                                                        if (FarmerShareUtils.saveHost("host", str)) {
                                                            HttpUtils.resetIp(FarmerShareUtils.getHost("host"));
                                                            innovation.utils.HttpUtils.resetIp(str);
                                                            ((Activity) ct).finish();
                                                            Intent it = ct.getPackageManager().getLaunchIntentForPackage(ct.getPackageName());
                                                            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                            ct.startActivity(it);
                                                            Process.killProcess(Process.myPid());
                                                            System.exit(0);
                                                        }
                                                    }
                                                }).setNegativeButton("取消", null).show();


                                        et.setOnTouchListener(new View.OnTouchListener() {
                                            @Override
                                            public boolean onTouch(View v, MotionEvent event) {
                                                Drawable drawable = et.getCompoundDrawables()[2];
                                                if (drawable == null) {
                                                    return false;
                                                }
                                                //drawleft 是 小于 ,drawright 是 大于
                                                //左右上下分别对应 0  1  2  3
                                                if (event.getX() > et.getWidth() - et.getCompoundDrawables()[2].getBounds().width()) {
                                                    //点击之后执行的事件
//                                                    Toast.makeText(ct, "右边的drawright被点击了", Toast.LENGTH_SHORT).show();
                                                    //textView.setText("我被点击了");
                                                    final ListPopupWindow listPopupWindow;
                                                    listPopupWindow = new ListPopupWindow(ct);
                                                    listPopupWindow.setAdapter(new ArrayAdapter<String>(ct, android.R.layout.simple_list_item_1, slist));//用android内置布局，或设计自己的样式
                                                    listPopupWindow.setAnchorView(et);//以哪个控件为基准，在该处以mEditText为基准
                                                    listPopupWindow.setModal(true);

                                                    listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
                                                        @Override
                                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                            String str = adapterView.getAdapter().getItem(i).toString();
                                                            et.setText(str);//把选择的选项内容展示在EditText上
                                                            listPopupWindow.dismiss();//如果已经选择了，隐藏起来
                                                            enterDialog.cancel();
                                                            if (FarmerShareUtils.saveHost("host", str)) {
                                                                HttpUtils.resetIp(FarmerShareUtils.getHost("host"));
                                                                innovation.utils.HttpUtils.resetIp(str);
                                                                ((Activity) ct).finish();
                                                                Intent it = ct.getPackageManager().getLaunchIntentForPackage(ct.getPackageName());
                                                                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                                ct.startActivity(it);
                                                                Process.killProcess(Process.myPid());
                                                                System.exit(0);
                                                            }
                                                        }
                                                    });
                                                    listPopupWindow.show();//把ListPopWindow展示出来
                                                    return false;
                                                }
                                                return false;
                                            }
                                        });


                                    }
                                }
                            }).
                            setNegativeButton("cancel", null).show();
                }
            }
        });
    }
}
