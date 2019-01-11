package com.xiangchuang.risks.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import com.xiangchuangtec.luolu.animalcounter.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import innovation.utils.HttpUtils;

public class ShareUtils {

    private static SharedPreferences preferences;

    public static final void init(Context context) {
        if (null == preferences)
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        preferences = context.getSharedPreferences(context.getPackageName()+"_preferences",Context.MODE_PRIVATE);
    }

    public static final String getHost(String key) {
        return preferences.getString(key, "http://60.205.209.245:8081/nongxian2/");
    }

    public static final boolean saveHost(String key, String val) {
        return preferences.edit().putString(key, val).commit();
    }

    public static final void saveString(String key, String val) {
        preferences.edit().putString(key, val).apply();
    }

    public static final void saveInt(String key, Integer val) {
        preferences.edit().putInt(key, val).apply();
    }

    public static final void saveBool(String key, Boolean val) {
        preferences.edit().putBoolean(key, val).apply();
    }

    public static final String getString(String key) {
        return preferences.getString(key, "");
    }

    public static final int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    public static final boolean getBool(String key) {
        return preferences.getBoolean(key, false);
    }

    public static final List<String> getIPList() {
        List<String> list = new ArrayList<>();
        try {
            Map<String, Object> map = (Map<String, Object>) preferences.getAll();
            for (Map.Entry entry : map.entrySet()) {
                if (entry.getValue().equals("ip")) {
                    list.add(entry.getKey().toString());
                }
            }
        } catch (Exception e) {

        }
        return list;
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
                                    Toast.makeText(ct, "pwd=" + pwd, Toast.LENGTH_SHORT).show();
                                    if ("http://60.205.209.245:8081/nongxian2/".equals(HttpUtils.baseUrl)) {
                                        Toast.makeText(ct, "当前处于正式环境", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ct, "当前处于测试环境", Toast.LENGTH_SHORT).show();
                                    }
                                    if (pwd.equals("321")) {

                                        EditText et = new EditText(ct);
                                        Drawable drawable = ct.getResources().getDrawable(R.drawable.bg_tel);
                                        et.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                                        List<String> slist = new ArrayList<>();
                                        final String[] list = {"http://60.205.209.245:8081/nongxian2/", "http://47.92.167.61:8081/nongxian2/"};//要填充的数据
                                        slist.addAll(Arrays.asList(list));
                                        slist.addAll(ShareUtils.getIPList());
                                        AlertDialog enterDialog = new AlertDialog.Builder(ct).setTitle("提示").setMessage("请转入地址")
                                                .setView(et).setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                        String str = et.getText().toString().trim();
                                                        if (TextUtils.isEmpty(str)) return;
                                                        if (!slist.contains(str))
                                                            ShareUtils.saveString(et.getText().toString().trim(), "ip");
                                                        if (ShareUtils.saveHost("host", str)) {
                                                            ((Activity) ct).finish();
                                                            Intent it = ct.getPackageManager().getLaunchIntentForPackage(ct.getPackageName());
                                                            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                            ct.startActivity(it);
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
                                                    Toast.makeText(ct, "右边的drawright被点击了", Toast.LENGTH_SHORT).show();
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
                                                            if (ShareUtils.saveHost("host", str)) {
                                                                ((Activity) ct).finish();
                                                                Intent it = ct.getPackageManager().getLaunchIntentForPackage(ct.getPackageName());
                                                                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                                ct.startActivity(it);
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
