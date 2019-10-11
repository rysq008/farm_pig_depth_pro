package innovation.utils;

import android.content.Context;

import com.hjq.toast.ToastUtils;


public class Toast {
    public static final int LENGTH_LONG = 1;
    public static final int LENGTH_SHORT = 0;
    private static String message;
//    private static android.widget.Toast toast;
    private static Toast mToast;

    public static Toast makeText(Context context, String msg, int i) {
        message = msg;
//        toast = android.widget.Toast.makeText(context, msg,i);
        mToast = new Toast();
        return mToast;
    }

    public void show() {
        ToastUtils.show(message);
    }
}
