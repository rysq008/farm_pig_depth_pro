package innovation.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;

import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.view.SelectFunctionActivity_new;
import com.xiangchuang.risks.view.WeightPicCollectActivity;


/**
 * @Author: Lucas.Cui
 * 时   间：2019/1/14
 * 简   述：<功能简述>
 */
public class DialogHelper {
    //猪在图片中占比太小
    public static final int ERROR_CODE_PIG_SMALL = 201;
    //猪在图片中占比太大
    public static final int ERROR_CODE_PIG_LARGE = 202;
    //没检测到尺子；（太暗或没有放尺子）
    public static final int ERROR_CODE_NO_FIND_RULER = 211;
    //尺子太小
    public static final int ERROR_CODE_RULER_SMALL = 212;
    //尺子太大
    public static final int ERROR_CODE_RULER_LARGE = 213;
    //图片传输中解码出错
    public static final int ERROR_CODE_PICTURE_DECODE_ERROR = 221;


    /**
     * 拍照称重前提示用户拍整猪照 Dialog
     *
     * @param activity
     */
    public static void weightCheckDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setIcon(R.mipmap.ic_launcher).setTitle("提示")
                .setMessage("请将整只死猪放在拍摄范围内进行拍摄")
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
     * 拍照称重前提示用户拍整猪照 Dialog
     *
     * @param activity
     */
    public static void weightCheckDialog1(final Activity activity, String errorMsg) {
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "图片提交失败！请重新拍摄并确保整只死猪在拍摄范围内。";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setIcon(R.mipmap.ic_launcher).setTitle("提示")
                .setMessage(errorMsg)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WeightPicCollectActivity.start(activity);
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    /**
     * 拍照称重多次失败提示用户手动输入 Dialog
     *
     * @param activity
     */
    public static void weightCheckFailureDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setIcon(R.mipmap.ic_launcher).setTitle("提示")
                .setMessage("您多次照片拍摄不合格，系统将根据填写的畜龄计算出重量。")
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
     * 推出称重
     *
     * @param activity
     */
    public static void exitCheckDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setIcon(R.mipmap.ic_launcher).setTitle("提示")
                .setMessage("理赔审核需要填写畜龄、死亡时间及拍摄称重照片否则可能审核失败，您确定要退出吗？")
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

    /**
     * 无害化处理退到上一步，跳过弹框提醒
     *
     * @param activity
     */
    public static void deadPigProcessExitTip(final Activity activity, CallBackListener listener,int step, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setIcon(R.mipmap.ic_launcher).setTitle("提示")
                .setMessage(msg)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(listener != null){
                            listener.onSuccess(step);
                        }
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

    /**
     * 退出无害化处理步骤
     *
     * @param activity
     */
    public static void exitDeadPigProcessDialog(final Activity activity,boolean isCreateOrder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setIcon(R.mipmap.ic_launcher).setTitle("提示")
                .setMessage("此步骤的无害化处理数据还未提交，此时退出该页面后当前页面的数据将被清除，您确定退出吗？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.finish();
                        if(!isCreateOrder){
                            Intent intent = new Intent(activity, SelectFunctionActivity_new.class);
                            activity.startActivity(intent);
                        }
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

    public interface CallBackListener {
        void onSuccess(int step);

        void onFailed(int step);
    }
}
