package innovation.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.xiangchuangtec.luolu.animalcounter.R;


/**
 * @Author: Lucas.Cui
 * 时   间：2019/1/14
 * 简   述：<功能简述>
 */
public class DialogHelper {

    /**
     * 拍照称重前提示用户拍整猪照 Dialog
     *
     * @param activity
     */
    public static void weightCheckDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setIcon(R.mipmap.ic_launcher).setTitle("提示")
                .setMessage("拍照称重需要拍摄整个猪身照片")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    /**
     *  推出称重
     * @param activity
     */
    public static void exitCheckDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setIcon(R.mipmap.ic_launcher).setTitle("提示")
                .setMessage("理赔需要拍照估重提交称重信息，您确定要退出称重吗")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.finish();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }
}
