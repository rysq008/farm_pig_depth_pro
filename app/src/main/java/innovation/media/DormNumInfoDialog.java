package innovation.media;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuangtec.luolu.animalcounter.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.demo.DetectorActivity;
import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import innovation.entry.InnApplication;
import innovation.location.LocationManager;
import innovation.login.Utils;
import innovation.utils.DeviceUtil;
import innovation.utils.FileUtils;
import innovation.utils.JsonHelper;
import innovation.utils.ScreenUtil;
import innovation.utils.StorageUtils;

import static android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

/**
 * @author wbs on 12/16/17.
 */

public class DormNumInfoDialog extends Dialog {
    private TextView mTitle;
    private ImageView mImage1;
    private Button dormNumExit;
    private Button dormNumStart;
    private EditText editDormNum;
    private ProgressDialog mProgressDialog;
    private static Logger mLogger = new Logger(DormNumInfoDialog.class.getName());
    File zipImageDir = StorageUtils.getZipImageDir(getContext());
    File zipVideoDir = StorageUtils.getZipVideoDir(getContext());
    private ListView listViewCounterResult;
    private ArrayList<String> stringArrayList;
    private ArrayAdapter<String> stringArrayAdapter;

    public DormNumInfoDialog(Context context) {
        super(context, R.style.Alert_Dialog_Style);
        setContentView(R.layout.dorm_num_dialog_layout);

        LocationThread locationThread = new LocationThread();
        locationThread.start();

        mTitle = (TextView) findViewById(R.id.tv_titleDormNum);
//        mImage1 = (ImageView) findViewById(R.id.image1);
        editDormNum = findViewById(R.id.editDormNum);
        editDormNum.setSelection(editDormNum.getText().length());
        editDormNum.requestFocus();
        dormNumExit = (Button) findViewById(R.id.dormNumExit);
        dormNumStart = (Button) findViewById(R.id.dormNumStart);
        listViewCounterResult = findViewById(R.id.listViewCounterResult);
        stringArrayList = new ArrayList<String>();
        stringArrayList.add("            巡检记录");

        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    stringArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, stringArrayList) {
                        public View getView(int position, View convertView, ViewGroup parent) {
                            // Get the current item from ListView
                            View view = super.getView(position, convertView, parent);
                            // Get the Layout Parameters for ListView Current Item View
                            ViewGroup.LayoutParams params = view.getLayoutParams();
                            // Set the height of the Item View
                            params.height = 55;
                            view.setLayoutParams(params);

                            return view;
                        }

                        @Override
                        public void notifyDataSetChanged() {
                            super.notifyDataSetChanged();
                        }
                    };

                    listViewCounterResult.setAdapter(stringArrayAdapter);
                    listViewCounterResult.deferNotifyDataSetChanged();

                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        dormNumExit.setOnClickListener(new View.OnClickListener() {
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

    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setStringArrayList(String string) {
        if (string == null) {
            mLogger.i("string为null！！");
        } else {
            mLogger.i("string：" + string);
            stringArrayList.add(string);
            stringArrayAdapter.notifyDataSetChanged();
        }
    }

    public void setEditDormNum(String dormNum) {
        editDormNum.setText(dormNum);
        editDormNum.setSelection(editDormNum.getText().length());
    }

    public String getEdit(String stringEdit){
        stringEdit = editDormNum.getText().toString();
        return stringEdit;
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
        editDormNum.setSelection(editDormNum.getText().length());
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
        dormNumExit.setText(text);
        dormNumExit.setVisibility(View.VISIBLE);
        dormNumExit.setOnClickListener(listener);
    }

    public void setDormNumStart(String text, View.OnClickListener listener) {
        dormNumStart.setText(text);
        dormNumStart.setVisibility(View.VISIBLE);
        dormNumStart.setOnClickListener(listener);
    }

    private class LocationThread extends Thread {

        @Override
        public void run() {
            LocationManager.getInstance(getContext()).startLocation();
        }

    }


}
