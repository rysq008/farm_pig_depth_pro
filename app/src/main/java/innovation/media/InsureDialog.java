package innovation.media;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.PreferencesUtils;

import java.util.Map;

import innovation.utils.ScreenUtil;

import static android.content.ContentValues.TAG;

/**
 * Author by luolu, Date on 2018/8/25.
 * COMPANY：InnovationAI
 */

public class InsureDialog extends Dialog {


    private Button insurecancel;
    private ImageView mImage2, mImage7;
    private ImageView mImage6, mImage8, mImage3, mImage1;
    private Button mAbortBtn;
    private Button mAddBtn;
    private Button mNextBtn, mUpOneBtn, mUpAllBtn, caijiRetry, insure_cowInfo;
    ;
    private TextView mTips;
    public static EditText mNumberEdit;
    public static TextView mLipeiNumber;
    public static TextView mcowear_number;
    public static TextView mcowType;
    private String missInfo = "";
    private boolean angleOK = true; // 每头猪所需要的必要角度是否完整
    //    private final static int minCount = 5;
    private Button mSeeImage, mSeeVideo;


    public InsureDialog(Context context, View view) {
        super(context, R.style.Alert_Dialog_Style);
        //setContentView(R.layout.insure_dialog_layout);
        setContentView(view);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.alpha = 1.0f;
        params.width = (int) (ScreenUtil.getScreenWidth() - 35 * ScreenUtil.getDensity());
        window.setAttributes(params);
        setCanceledOnTouchOutside(false);

        mImage2 = (ImageView) findViewById(R.id.insure_image2);
        mImage3 = (ImageView) findViewById(R.id.insure_image3);
        mImage1 = (ImageView) findViewById(R.id.insure_image1);
        mImage6 = (ImageView) findViewById(R.id.insure_image6);
        mImage7 = (ImageView) findViewById(R.id.insure_image7);
        mImage8 = (ImageView) findViewById(R.id.insure_image8);
        mNumberEdit = (EditText) findViewById(R.id.insure_number);
        mLipeiNumber = (TextView) findViewById(R.id.lipei_number);
        mcowear_number = (TextView) findViewById(R.id.cowear_number);
        mcowType = (TextView) findViewById(R.id.cow_type);
        mTips = (TextView) findViewById(R.id.tv_tips);
        mAbortBtn = (Button) findViewById(R.id.insure_abort);
        mAddBtn = (Button) findViewById(R.id.insure_add);
        mNextBtn = (Button) findViewById(R.id.btn_next);
        mUpOneBtn = (Button) findViewById(R.id.btn_uploadone);
        mUpAllBtn = (Button) findViewById(R.id.btn_uploadall);
        mSeeImage = (Button) findViewById(R.id.insure_seeimage);
        mSeeVideo = (Button) findViewById(R.id.insure_seevideo);
        insurecancel = (Button) findViewById(R.id.insure_cancel);
//        caijiRetry = (Button) findViewById(R.id.caijiRetry);
//        insure_cowInfo  = (Button) findViewById(R.id.insure_cowInfo);
    }

    //判断是否获得必要角度
    public boolean getAngleOk(Map<String, String> map) {
        boolean angleok = true;
        int count2 = 0;
        int count7 = 0;
        for (String v : map.keySet()) {
            int count = Integer.parseInt(map.get(v));
            switch (v) {
                case "2":
                    count2 += count;
                    break;
                case "7":
                    count7 += count;
                    break;
                default:
                    break;
            }
        }
        if ((count2 == 0) && (count7 == 0)) {
            angleok = false;
        }

        return angleok;
    }

    private boolean getAngleMissData(Map<String, String> map) {

        Log.e(TAG, "img_map: " + map.toString());

        boolean angleok = true;
        missInfo = "";
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;
        for (String v : map.keySet()) {
            int count = Integer.parseInt(map.get(v));
            switch (v) {
                case "3":
                    count1 += count;
                    break;
                case "2":
                    count2 += count;
                    break;
                case "1":
                    count3 += count;
                    break;
                default:
                    break;
            }
        }

        if (count1 < 3) {
            missInfo += "3" + "， ";
            angleok = false;
        } else {
            //  mImage1.setBackgroundResource(R.drawable.cow_angle2);
        }
        if (count2 < 7) {
            missInfo += "2" + "， ";
            angleok = false;
        } else {
            //mImage2.setBackgroundResource(R.drawable.cow_angle1);
        }
        if (count3 < 3) {
            missInfo += "1" + "， ";
            angleok = false;
        } else {
            //  mImage3.setBackgroundResource(R.drawable.cow_angle0);
        }
        // TODO: 2018/8/25 By:LuoLu
        if (count1 >= 3 && count2 >= 7 && count3 >= 3) {
            mAddBtn.setVisibility(View.GONE);
        }

        Log.e("shuling", "count1: " + count1 + "===count2: " + count2 + "===count3: " + count3);


//        if (!angleok) {
//            missInfo = "待采集：左脸、右脸和正脸";
//        }
        return angleok;
    }

    private void initView() {
        mImage1.setBackgroundResource(R.drawable.pig1);
        mImage2.setBackgroundResource(R.drawable.pig2);
        mImage3.setBackgroundResource(R.drawable.pig3);
        mNumberEdit.setText("");
        mNumberEdit.clearFocus();
        mTips.setText("");
    }

    public void setAbortButton(String text, View.OnClickListener listener) {
        mAbortBtn.setOnClickListener(listener);
    }

    public void setCancelButton(String text, View.OnClickListener listener) {
        insurecancel.setOnClickListener(listener);
    }

    public void setAddeButton(String text, View.OnClickListener listener) {
        //mAddBtn.setText(text);
        mAddBtn.setOnClickListener(listener);
    }

    public void setUploadOneButton(String text, View.OnClickListener listener) {
        //mAddBtn.setText(text);
        mUpOneBtn.setOnClickListener(listener);
    }

    public void setUploadAllButton(String text, View.OnClickListener listener) {
        //mAddBtn.setText(text);
        mUpAllBtn.setOnClickListener(listener);
    }

    public void setNexteButton(String text, View.OnClickListener listener) {
        //mAddBtn.setText(text);
        mNextBtn.setOnClickListener(listener);
    }

    public void setSeeImageButton(String text, View.OnClickListener listener) {
        mSeeImage.setOnClickListener(listener);
    }

    public void setSeeVideoButton(String text, View.OnClickListener listener) {
        mSeeVideo.setOnClickListener(listener);
    }

    public void setCaijiRetryButton(String text, View.OnClickListener listener) {
        caijiRetry.setOnClickListener(listener);
    }

    public void updateView(Map<String, String> showMap, boolean haveimage, boolean havezip, String libid, boolean havevideo) {

        initView();

        if (AppConfig.debugNub == 1) {
            int n = PreferencesUtils.getIntValue(Constants.lipein, 120, AppConfig.getActivity());

            mTips.setText("系统检测到您的采集时间已超过" + n + "秒，" +
                    "您可以继续采集理赔图像，也可以强制提交当前已采集的内容，系统进行人工复核。\n" +
                    "请选择 直接提交或继续采集");
            mTips.setVisibility(View.VISIBLE);
            mNextBtn.setVisibility(View.GONE);
            mAddBtn.setVisibility(View.GONE);
            insurecancel.setVisibility(View.GONE);
            mUpOneBtn.setVisibility(View.VISIBLE);
            mAbortBtn.setVisibility(View.VISIBLE);
            mUpOneBtn.setText("直接提交");
            mAbortBtn.setText("继续采集");
        } else if (AppConfig.debugNub == 2) {
            String customServ = PreferencesUtils.getStringValue(Constants.customServ, AppConfig.getActivity());
            int m = PreferencesUtils.getIntValue(Constants.lipeim, 240, AppConfig.getActivity());

            mTips.setText("系统检测到您的采集时间已超过" + m + "秒，" +
                    "建议您强制提交当前已采集的内容，系统将进行人工复核。强制提交后请用手机直接对着牲畜的左、中、右脸拍摄一段不少于2分钟视频，留存作为档案提交到" + customServ + "处。\n" +
                    "请选择 直接提交或重新采集");
            mTips.setVisibility(View.VISIBLE);
            mNextBtn.setVisibility(View.GONE);
            mAddBtn.setVisibility(View.GONE);
            insurecancel.setVisibility(View.GONE);
            mUpOneBtn.setVisibility(View.VISIBLE);
            mUpOneBtn.setText("直接提交");
            mAbortBtn.setVisibility(View.VISIBLE);
            mAbortBtn.setText("重新采集");
        } else {
            mAbortBtn.setVisibility(View.GONE);
            mTips.setVisibility(View.GONE);
            insurecancel.setVisibility(View.VISIBLE);
            mUpOneBtn.setVisibility(View.VISIBLE);
            angleOK = getAngleMissData(showMap);

            if (angleOK)//必要角度齐全
            {
           /* mUpOneBtn.setVisibility(View.VISIBLE);
            insurecancel.setVisibility(View.VISIBLE);
            mNextBtn.setVisibility(View.GONE);
            mAddBtn.setVisibility(View.GONE);
            mAbortBtn.setVisibility(View.GONE);*/
            } else {
           /* insurecancel.setVisibility(View.GONE);
            mUpOneBtn.setVisibility(View.GONE);
            mNextBtn.setVisibility(View.GONE);*/
            }

            if (haveimage)// 图片文件存在，补充按钮可见，回看图片按钮可见
            {
//            mTips.setText(missInfo);
//            mAddBtn.setVisibility(View.VISIBLE);
                mSeeImage.setVisibility(View.GONE);
//            insure_cowInfo.setVisibility(View.VISIBLE);
            } else {
                mAddBtn.setVisibility(View.GONE);
                mSeeImage.setVisibility(View.GONE);
//            insure_cowInfo.setVisibility(View.GONE);
            }

            if (havezip)// zip文件存在，全部上传按钮可见
            {
//            mUpAllBtn.setVisibility(View.VISIBLE);
            } else {
                mUpAllBtn.setVisibility(View.GONE);
            }

            if (havevideo) {// zip文件存在，全部上传按钮可见
                mSeeVideo.setVisibility(View.GONE);
            } else {
                mSeeVideo.setVisibility(View.GONE);
            }
            mNumberEdit.setText(libid);

        }
    }

    public void setTextTips(String text) {
        mTips.setText(text);
    }

}
