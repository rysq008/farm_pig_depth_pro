package innovation.media;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.innovation.pig.insurance.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import innovation.login.Utils;
import innovation.utils.JsonHelper;
import innovation.utils.ScreenUtil;
import innovation.utils.StorageUtils;

/**
 * @author wbs on 12/16/17.
 */

public class Page1InfoDialog extends Dialog {
private static Logger mlogger = new Logger(Page1InfoDialog.class.getName());
    private TextView mTitle;
    private ImageView mImage1;
    private ImageView mImage2;
    private ImageView mImage3;
    private ImageView mImage6;
    private ImageView mImage7;
    private ImageView mImage8;
    private EditText edt;
    private Button mPositiveBtn;
    private Button mNegativeBtn;
    private Button buttonUpload;
    File zipImageDir = StorageUtils.getZipImageDir(getContext());
    File zipVideoDir = StorageUtils.getZipVideoDir(getContext());


    public Page1InfoDialog(Context context) {

        super(context, R.style.Alert_Dialog_Style);
        setContentView(R.layout.page1_info_dialog_layout);
        mTitle = (TextView) findViewById(R.id.tv_title_page1);
         edt = (EditText)findViewById(R.id.page1InfoDialog);

        mImage1 = (ImageView) findViewById(R.id.image1);
        mImage2 = (ImageView) findViewById(R.id.image2);
        mImage3 = (ImageView) findViewById(R.id.image3);
        mImage6 = (ImageView) findViewById(R.id.image6);
        mImage7 = (ImageView) findViewById(R.id.image7);
        mImage8 = (ImageView) findViewById(R.id.image8);
        mPositiveBtn = (Button) findViewById(R.id.positive);
        mNegativeBtn = (Button) findViewById(R.id.negative);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        mPositiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //luolu
                dismiss();
                File detectDir = StorageUtils.getSrcImageDir(getContext());
                File getSrcVideoDir = StorageUtils.getSrcVideoDir(getContext());
                if (!detectDir.getParentFile().exists()) {
                    detectDir.getParentFile().mkdirs();
                }
                FileOutputStream outStream = null;
                FileOutputStream outStream1 = null;
                File pigNumber = new File(detectDir, "number.txt");
                File pigNumberVideo = new File(getSrcVideoDir, "number.txt");
                mlogger.i("Lu,pigNumber Dir: " + pigNumber);
                try {
                    outStream = new FileOutputStream(pigNumber);
                    outStream1 = new FileOutputStream(pigNumberVideo);
                    outStream.write(edt.getText().toString().getBytes());
                    outStream1.write(edt.getText().toString().getBytes());
                    outStream.close();
                    outStream1.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, "当前编号为：" + edt.getText().toString(), Toast.LENGTH_SHORT).show();

            }
        });
        mNegativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                getOwnerActivity().finish();
                System.exit(0);
            }
        });
        buttonUpload.setOnClickListener(new View.OnClickListener() {
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









//        initView(model, info);
//        mPositiveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClickView(View v) {
//
//            }
//        });

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.alpha = 1.0f;
        params.width = (int) (ScreenUtil.getScreenWidth() - 35 * ScreenUtil.getDensity());
        window.setAttributes(params);
        setCanceledOnTouchOutside(false);
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

    public void setPositiveButton(String text, View.OnClickListener listener) {
        mPositiveBtn.setText(text);
        mPositiveBtn.setVisibility(View.VISIBLE);
        mPositiveBtn.setOnClickListener(listener);
//        mPositiveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClickView(View v) {
//                Intent intent = new Intent();
//                intent.setClass(InnApplication.getAppContext(), DetectorActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                InnApplication.getAppContext().startActivity(intent);
////                InnApplication.getAppContext().finish();
//            }
//        });

    }
//
//public void setPositiveButton(new)
//        luolu
//        Intent it = new Intent(InnApplication.getAppContext(),DetectorActivity.class);
//        InnApplication.getAppContext().startActivity(it);



    public void setNegativeButton(String text, View.OnClickListener listener) {
        mNegativeBtn.setText(text);
        mNegativeBtn.setVisibility(View.VISIBLE);
        mNegativeBtn.setOnClickListener(listener);
    }
}
