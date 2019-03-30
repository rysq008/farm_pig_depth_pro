package innovation.media;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.innovation.pig.insurance.R;

import org.json.JSONException;
import org.json.JSONObject;

import innovation.login.Utils;
import innovation.utils.JsonHelper;
import innovation.utils.ScreenUtil;

/**
 * @author wbs on 12/16/17.
 */

public class Angle16InfoDialog extends Dialog {

    private TextView mTitle;
    private ImageView mImage1;
    private ImageView mImage6;
    private Button mPositiveBtn;
    private Button mNegativeBtn;

    public Angle16InfoDialog(Context context) {
        super(context, R.style.Alert_Dialog_Style);
        setContentView(R.layout.angle16_info_dialog_layout);
        mTitle = (TextView) findViewById(R.id.tv_title16);

        mImage1 = (ImageView) findViewById(R.id.image1);
        mImage6 = (ImageView) findViewById(R.id.image6);
        mPositiveBtn = (Button) findViewById(R.id.positiveAngle16Info);
        mNegativeBtn = (Button) findViewById(R.id.negativeAngle16Info);
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
