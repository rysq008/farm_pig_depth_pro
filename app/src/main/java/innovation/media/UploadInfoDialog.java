package innovation.media;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.innovation.pig.insurance.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.demo.DetectorActivity;
import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.io.IOException;

import innovation.entry.InnApplication;
import innovation.login.Utils;
import innovation.utils.FileUtils;
import innovation.utils.JsonHelper;
import innovation.utils.ScreenUtil;
import innovation.utils.StorageUtils;

/**
 * @author wbs on 12/16/17.
 */

public class UploadInfoDialog extends Dialog {

    private TextView mTitle;
    private ImageView mImage1;
    private Button uploadAll;
    private Button nextPig;
    private Button uploadThisPig;
    private ProgressDialog mProgressDialog;
    private static Logger mLogger = new Logger(UploadInfoDialog.class.getName());
    File zipImageDir = StorageUtils.getZipImageDir(getContext());
    File zipVideoDir = StorageUtils.getZipVideoDir(getContext());

    public UploadInfoDialog(Context context) {
        super(context, R.style.Alert_Dialog_Style);
        setContentView(R.layout.upload_info_dialog_layout);
        mTitle = (TextView) findViewById(R.id.tv_titleUpload);

        mImage1 = (ImageView) findViewById(R.id.image1);
        uploadAll = (Button) findViewById(R.id.uploadAll);
        nextPig = (Button) findViewById(R.id.nextPig);
        uploadThisPig = (Button) findViewById(R.id.uploadThisPig);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.alpha = 1.0f;
        params.width = (int) (ScreenUtil.getScreenWidth() - 35 * ScreenUtil.getDensity());
        window.setAttributes(params);
        setCanceledOnTouchOutside(false);

        showProgressDialog(context);

    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setMessage(String msg) {
        setInfoViewVisible(false);
//        mMessage.setText(msg);
    }

    public void setInfo(int model, String info) {
        setInfoViewVisible(true);
        initView(model, info);
    }

    private void initView(int model, String info) {
        JSONObject jo = null;
        JSONObject jo_env = null;
        try {
            jo = new JSONObject(info);
            jo_env = JsonHelper.getJsonObj(jo, Utils.Upload.LIB_ENVINFO);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jo == null) {
            mTitle.setText(model == Model.BUILD.value() ? "投保失败" : "理赔失败");
//            mMessage.setText("错误信息: " + info);
            setInfoViewVisible(false);
            return;
        }

        String fullName = JsonHelper.getString(jo, Utils.Upload.FULL_NAME);
        int id = JsonHelper.getInt(jo, Utils.Upload.LIB_ID);
        String time = JsonHelper.getString(jo, Utils.Upload.LIB_CREATE_TIME);
        String location = JsonHelper.getString(jo_env, Utils.Upload.GPS);
        String image = JsonHelper.getString(jo, Utils.Upload.IMAGE_URL);
//        mFullName.setText("投保人： " + fullName);
//        mId.setText("序号： " + id);
//        mTime.setText("投保时间： " + time);
//        mLocation.setText((model == Model.BUILD.value() ? "养殖场地点： " : "投保地点： ") + location);
////        ImageLoader.getInstance().displayImage(image, mImage, ImageLoaderUtils.getDefaultOptions());
    }

    private void setInfoViewVisible(boolean visible) {
//        mMessage.setVisibility(visible ? View.GONE : View.VISIBLE);
//        mFullName.setVisibility(visible ? View.VISIBLE : View.GONE);
//        mId.setVisibility(visible ? View.VISIBLE : View.GONE);
//        mTime.setVisibility(visible ? View.VISIBLE : View.GONE);
//        mLocation.setVisibility(visible ? View.VISIBLE : View.GONE);
//        mImage.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setUploadAll(String text, View.OnClickListener listener) {
        uploadAll.setText(text);
        uploadAll.setVisibility(View.VISIBLE);
        uploadAll.setOnClickListener(listener);
        uploadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                File[] filesZipImageDir = zipImageDir.listFiles();
                File[] filesZipVideoDir = zipVideoDir.listFiles();
                if (filesZipImageDir.length == 0) {
                  Toast.makeText(getContext(),"图片资料夹为空！！",Toast.LENGTH_SHORT).show();
                }
                if (filesZipVideoDir.length == 0) {
                  Toast.makeText(getContext(),"视频资料夹为空！！",Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    public void setNextPig(String text, View.OnClickListener listener) {
        nextPig.setText(text);
        nextPig.setVisibility(View.VISIBLE);
//        nextPig.setOnClickListener(listener);
        nextPig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Intent intent = new Intent();
                intent.setClass(InnApplication.getAppContext(), DetectorActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                InnApplication.getAppContext().startActivity(intent);
                try {
                    FileUtils.copy(StorageUtils.getSrcImageDir(getContext()),StorageUtils.getZipImageDir(getContext()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setUploadThisPig(String text, View.OnClickListener listener) {
        uploadThisPig.setText(text);
        uploadThisPig.setVisibility(View.VISIBLE);
        uploadThisPig.setOnClickListener(listener);
    }

    private void showProgressDialog(Context context) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);//false


        mProgressDialog.setCanceledOnTouchOutside(false);//false
        mProgressDialog.setIcon(R.drawable.ic_launcher);
//        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "确定", mProgClickListener);
        mProgressDialog.setMessage("正在处理......");
//        mProgressDialog.show();
//        Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
//        if (positive != null) {
//            positive.setVisibility(View.GONE);
//        }
    }


}
