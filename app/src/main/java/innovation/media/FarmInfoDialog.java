package innovation.media;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loc.cx;
import com.xiangchuangtec.luolu.animalcounter.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.demo.env.Logger;

import java.io.File;

import innovation.location.LocationManager;
import innovation.login.Utils;
import innovation.utils.JsonHelper;
import innovation.utils.ScreenUtil;
import innovation.utils.StorageUtils;

/**
 * @author wbs on 12/16/17.
 */

public class FarmInfoDialog extends Dialog {
    private TextView mTitle;
    private ImageView mImage1;
    private Button farmExit;
    private Button farmEnter;
    private EditText editFarmInfo;
    private ProgressDialog mProgressDialog;
    private static Logger mLogger = new Logger(FarmInfoDialog.class.getName());
    private Spinner spinner1;
    File zipImageDir = StorageUtils.getZipImageDir(getContext());
    File zipVideoDir = StorageUtils.getZipVideoDir(getContext());
    String textSpinner1;
    String[] items = { "翔创养殖场","新菜养殖场","501养殖场","大兴养殖场","正邦养殖场","国元养殖场","郑州养殖场"
            ,"秦皇岛养殖场","南昌养殖场","石家庄养殖场",
            "成都养殖场","青岛养殖场","哈尔滨养殖场","广州养殖场","涿州养殖场" ,"北京养殖场"};

    public FarmInfoDialog(Context context) {
        super(context, R.style.Alert_Dialog_Style);
        setContentView(R.layout.farm_edit_dialog_layout);

        LocationThread locationThread = new LocationThread();
        locationThread.start();

        mTitle = (TextView) findViewById(R.id.tv_titleFarmInfo);
//        mImage1 = (ImageView) findViewById(R.id.image1);
//        editFarmInfo = findViewById(R.id.editFarmInfo);
//        editFarmInfo.setSelection(editFarmInfo.getText().length());
//        editFarmInfo.requestFocus();
        farmExit = (Button) findViewById(R.id.farmExit);
        farmEnter = (Button) findViewById(R.id.farmEnter);

        farmExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
//                getOwnerActivity().finish();
                System.exit(0);
            }
        });

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.alpha = 1.0f;
        params.width = (int) (ScreenUtil.getScreenWidth() - 35 * ScreenUtil.getDensity());
        window.setAttributes(params);
        setCanceledOnTouchOutside(false);

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_activated_1, items){
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                // Get the current item from ListView
                View view = super.getView(position, convertView, parent);
//                view.setBackgroundColor(Color.RED);
                // Get the Layout Parameters for ListView Current Item View
                ViewGroup.LayoutParams params = view.getLayoutParams();
                // Set the height of the Item View
                params.height = 100;
                view.setLayoutParams(params);

                return view;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                // Get the current item from ListView
                View view = super.getView(position, convertView, parent);
//                view.setBackgroundColor(Color.RED);
                view.setBackgroundTintList(ColorStateList.valueOf(255));
                view.animate();
                // Get the Layout Parameters for ListView Current Item View
                ViewGroup.LayoutParams params = view.getLayoutParams();
                // Set the height of the Item View
                params.height = 100;
                view.setLayoutParams(params);

                return view;
            }
        };
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
//        addListenerOnSpinnerItemSelection();


    }

    public void addListenerOnSpinnerItemSelection() {
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
//        textSpinner1 = parent.getItemAtPosition(pos).toString()
//        mLogger.i("lu,textSpinner1: " + textSpinner1);
    }

    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            textSpinner1 = parent.getItemAtPosition(pos).toString();
            mLogger.i("lu,textSpinner1: " + textSpinner1);

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    public void setTitle(String title) {
        mTitle.setText(textSpinner1);
    }

    public void setTextSpinner1(String textSpinner1) {
        editFarmInfo.setText(textSpinner1);
    }

    public void setEditFarmInfo(String farmInfo) {
        farmInfo = editFarmInfo.getText().toString();
        editFarmInfo.setText(farmInfo);
    }

    public String getTextSpinner1(String farmInfo){
//        if (editFarmInfo == null){
//            this.setEditFarmInfo("输入不能为空！！");
//        }
//       textSpinner1;
        mLogger.i("lu,textSpinner1: " + textSpinner1);
        return textSpinner1;
    }

    public String getEditFarmInfo(String farmInfo){
        if (editFarmInfo == null){
            this.setEditFarmInfo("输入不能为空！！");
        }
        farmInfo = editFarmInfo.getText().toString();
        return farmInfo;
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
        editFarmInfo.setSelection(editFarmInfo.getText().length());
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

    public void setDormNumExit(String text, View.OnClickListener listener) {
        farmExit.setText(text);
        farmExit.setVisibility(View.VISIBLE);
        farmExit.setOnClickListener(listener);
        farmExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

            }
        });
    }

    public void setFarmEnter(String text, View.OnClickListener listener) {
        farmEnter.setText(text);
        farmEnter.setVisibility(View.VISIBLE);
        farmEnter.setOnClickListener(listener);

    }

    private class LocationThread extends Thread {

        @Override
        public void run() {
            LocationManager.getInstance(getContext()).startLocation();
        }

    }


}
