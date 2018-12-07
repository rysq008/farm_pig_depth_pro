package com.xiangchuang.risks.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast
 * Created by wuqb on 17-6-17.
 */

public class ToastUtils {

    private static ToastUtils utils;
    private Toast mToast = null;

    public static ToastUtils getInstance() {
        if (null == utils) {
            utils = new ToastUtils();
        }
        return utils;
    }

    public void showLong(Context context, String message) {
        show(context, message, Toast.LENGTH_LONG);
    }

    public void showShort(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT);
    }

    public void showLong(Context context, int textId) {
        show(context, textId, Toast.LENGTH_LONG);
    }

    public void showShort(Context context, int textId) {
        show(context, textId, Toast.LENGTH_SHORT);
    }

    private void show(Context context, String text, int duration) {
        if (null == mToast) {
            mToast = Toast.makeText(context, null, duration);
        }
        mToast.setText(text);

        mToast.show();
    }

    private void show(Context context, int textId, int duration) {
        if (null == mToast) {
            mToast = Toast.makeText(context, textId, duration);
        } else {
            mToast.setText(textId);
        }
        mToast.show();
    }
}
