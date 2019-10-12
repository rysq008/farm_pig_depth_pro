package innovation.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.utils.AlertDialogManager;
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
    //尺子太大
    public static final int ERROR_CODE_RULER_LARGE_TWO = 223;
    //没有猪
    public static final int ERROR_CODE_NO_PIG = 216;
    //图片传输中解码出错
    public static final int ERROR_CODE_PICTURE_DECODE_ERROR = 221;


    /**
     * 拍照称重前提示用户拍整猪照 Dialog
     *
     * @param activity
     */
    public static void weightCheckDialog(final Activity activity) {
        //改完
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_common_one_layout, null);
        TextView dialog_content_tv1 = dialogView.findViewById(R.id.dialog_content_tv1);
        TextView dialog_ok_btn = dialogView.findViewById(R.id.dialog_ok_btn);
        TextView dialog_tips_tv = dialogView.findViewById(R.id.dialog_tips_tv);

        dialog_tips_tv.setText("提示");
        dialog_content_tv1.setText("请将整只死猪放在拍摄范围内进行拍摄");
        builder.setView(dialogView);

        Dialog d = builder.create();
        dialog_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.setCancelable(false);
        d.show();
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
        //改完
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_common_one_layout, null);
        TextView dialog_content_tv1 = dialogView.findViewById(R.id.dialog_content_tv1);
        TextView dialog_ok_btn = dialogView.findViewById(R.id.dialog_ok_btn);
        TextView dialog_tips_tv = dialogView.findViewById(R.id.dialog_tips_tv);

        dialog_tips_tv.setText("提示");
        dialog_content_tv1.setText(errorMsg);
        builder.setView(dialogView);

        Dialog d = builder.create();
        dialog_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeightPicCollectActivity.start(activity);
                d.dismiss();
            }
        });
        d.setCancelable(false);
        d.show();
    }

    /**
     * 拍照称重多次失败提示用户手动输入 Dialog
     *
     * @param activity
     */
    public static void weightCheckFailureDialog(final Activity activity, String errormsg, View.OnClickListener submit) {
        //改完
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_common_two_layout, null);
        TextView dialog_content_tv1 = dialogView.findViewById(R.id.dialog_content_tv1);
        TextView dialog_ok_btn = dialogView.findViewById(R.id.dialog_ok_btn);
        TextView dialog_cancel_btn = dialogView.findViewById(R.id.dialog_cancel_btn);
        TextView dialog_tips_tv = dialogView.findViewById(R.id.dialog_tips_tv);

        dialog_tips_tv.setText("提示");
        dialog_ok_btn.setText("提交");
        dialog_cancel_btn.setText("重新拍照");
        dialog_content_tv1.setText(errormsg);
        builder.setView(dialogView);

        Dialog d = builder.create();

        dialog_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                submit.onClick(v);
            }
        });

        dialog_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                WeightPicCollectActivity.start(activity);
            }
        });
        d.setCancelable(false);
        d.show();
    }

    /**
     * 推出称重
     *
     * @param activity
     */
    public static void exitCheckDialog(final Activity activity) {

        AlertDialogManager.showMessageDialog(activity, "提示",
                "理赔审核需要填写畜龄、死亡时间及拍摄称重照片否则可能审核失败，您确定要退出吗？", new AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {
                activity.finish();
            }

            @Override
            public void onNegative() {

            }
        });
    }

    /**
     * 无害化处理退到上一步，跳过弹框提醒
     *
     * @param activity
     */
    public static void deadPigProcessExitTip(final Activity activity, CallBackListener listener, int step, String msg) {

        AlertDialogManager.showMessageDialog(activity, "提示",msg, new AlertDialogManager.DialogInterface() {
                    @Override
                    public void onPositive() {
                        if (listener != null) {
                            listener.onSuccess(step);
                        }
                    }

                    @Override
                    public void onNegative() {

                    }
                });
    }

    /**
     * 退出无害化处理步骤
     *
     * @param activity
     */
    public static void exitDeadPigProcessDialog(final Activity activity, boolean isCreateOrder) {

        AlertDialogManager.showMessageDialog(activity, "提示",
                "此步骤的无害化处理未提交，此时退出该页面当前页面的数据将清除，您确定退出吗？", new AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {
                activity.finish();
                if (!isCreateOrder) {
                    Intent intent = new Intent(activity, SelectFunctionActivity_new.class);
                    activity.startActivity(intent);
                }
            }

            @Override
            public void onNegative() {

            }
        });
    }

    public interface CallBackListener {
        void onSuccess(int step);

        void onFailed(int step);
    }
}
