package innovation.media;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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

import com.xiangchuang.risks.model.bean.ZhuJuanBean;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.utils.LocationManager;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.model.Commit;
import com.xiangchuangtec.luolu.animalcounter.model.DatabaseHelper;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.utils.ScreenUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;

/**
 * @author wbs on 12/16/17.
 */

public class DormNextInfoDialog extends Dialog implements View.OnClickListener {

    private TextView textZhuJuan;
    private TextView textZhuShe;
    private Spinner SpinnerZhuJuan;
    private Spinner SpinnerZhuShe;
    private String zhujuanname;
    private String zhushename;
    private File rcgImageFile;
    private int maxCount;
    private String no;
    private String recodenumber;
    private EditText numshoudong;
    private TextView numzidong;
    private Context context;
    private String zhujuanselect;
    private String zhusheselect;
    private TextView mTitle;
    private ImageView mImage1;
    private ProgressDialog mProgressDialog;
    private final DatabaseHelper databaseHelper;
    List<String> zhushenames = new ArrayList<>();
    List<String> zhujuannames = new ArrayList<>();
    private List<ZhuSheBean.DataBean> sheList;
    private final Activity activity;
    private List<ZhuJuanBean.DataBean> dataBeans;
    private int sheId;
    private String zhushesel;
    private String juansel;
    private int juanId;

    public void setParam(int maxCount) {
        this.maxCount = maxCount;
        numzidong.setText(String.valueOf(maxCount));
        numshoudong.setText(String.valueOf(maxCount));
       /* textViewZhuShe.setText(zhushename);
        textViewZhuJuan.setText(zhujuanname);*/
        numshoudong.setSelection(numshoudong.getText().length());
    }

    public DormNextInfoDialog(Context context,  int maxCount, File rcgImageFile) {
        super(context, R.style.Alert_Dialog_Style);
        this.context = context;
        this.maxCount = maxCount;
        this.rcgImageFile = rcgImageFile;
        setContentView(R.layout.next_dorm_info_dialog_layout);
        mTitle = (TextView) findViewById(R.id.tv_titleUpload);
        numzidong = (TextView) findViewById(R.id.num_zidong);
        numshoudong = (EditText) findViewById(R.id.num_shoudong);
        SpinnerZhuShe = (Spinner) findViewById(R.id.SpinnerZhuShe);
        SpinnerZhuJuan = (Spinner) findViewById(R.id.SpinnerZhuJuan);
        mImage1 = (ImageView) findViewById(R.id.image1);
        textZhuShe = (TextView) findViewById(R.id.textZhuShe);
        textZhuJuan = (TextView) findViewById(R.id.textZhuJuan);
        numzidong.setText(String.valueOf(maxCount));
        numshoudong.setText(String.valueOf(maxCount));
        Button sure = (Button) findViewById(R.id.sure);
        Button cancel = (Button) findViewById(R.id.cancel);
        sure.setOnClickListener(this);
        cancel.setOnClickListener(this);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.alpha = 1.0f;
        params.width = (int) (ScreenUtil.getScreenWidth() - 35 * ScreenUtil.getDensity());
        window.setAttributes(params);
        setCanceledOnTouchOutside(false);
       /* textViewZhuShe.setText(zhushename);
        textViewZhuJuan.setText(zhujuanname);*/
        numshoudong.setSelection(numshoudong.getText().length());
        numshoudong.setRawInputType(Configuration.KEYBOARD_QWERTY);
        showProgressDialog(context);
        databaseHelper = new DatabaseHelper(context);
        activity = (Activity) context;

        textZhuShe.setText(PreferencesUtils.getStringValue(Constants.shename, context));
        textZhuJuan.setText(PreferencesUtils.getStringValue(Constants.juanname, context));
        querySheJuanMessage();
    }

    private void querySheJuanMessage() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext())));
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.amountFlg, String.valueOf(9));
        mapbody.put(Constants.insureFlg, String.valueOf(9));
        //mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUSHESHOW, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //mProgressDialog.dismiss();
                Log.i("", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("DormNextInfoDialog", string);
                final ZhuSheBean bean = GsonUtils.getBean(string, ZhuSheBean.class);
                if (null != bean) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                zhushenames.clear();
                                sheList = bean.getData();
                                zhushenames.add("请选择猪舍");
                                for (int i = 0; i < sheList.size(); i++) {
                                    zhushenames.add(sheList.get(i).getName());
                                }
                                initSheSpinner();
                                getzhujuanMessage(sheList.get(0).getSheId() + "");
                            } else if (bean.getStatus() == 0) {
                                Toast.makeText(context, bean.getMsg(), Toast.LENGTH_LONG).show();
                            } else if (bean.getStatus() == -1) {
                                Toast.makeText(context, bean.getMsg(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "查询失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    private void getzhujuanMessage(String pighouseid) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext()) + "");
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.amountFlg, "" + 9);
        mapbody.put(Constants.insureFlg, "" + 9);
        Log.i("pighouseid=", pighouseid);
        mapbody.put(Constants.sheId, pighouseid);
        OkHttp3Util.doPost(Constants.ZHUJUANSHOW, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("DormNextInfoDialog", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("juan==", string);
                ZhuJuanBean bean = GsonUtils.getBean(string, ZhuJuanBean.class);
                if (null != bean) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            zhujuannames.clear();
                            dataBeans = bean.getData();
                            zhujuannames.add("请选择猪圈");
                            if (dataBeans.size() > 0) {
                                for (int i = 0; i < dataBeans.size(); i++) {
                                    zhujuannames.add(dataBeans.get(i).getName());
                                }
                                // finnalcount.setText(dataBeans.get(0).getCount() + "");
                                juanId = dataBeans.get(0).getJuanId();
                            }
                            initJuanSpinner();

                        }
                    });

                }

            }
        });


    }

    boolean isSheFirst = true;

    private void initSheSpinner() {
        //猪舍
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, zhushenames);
        SpinnerZhuShe.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerZhuShe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                } else {
                    sheId = sheList.get(position - 1).getSheId();
                    zhushesel = zhushenames.get(position - 1);
                    Log.i("===sheid", sheId + "");
                    //猪舍id
                    getzhujuanMessage(sheId + "");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sheId = sheList.get(0).getSheId();
    }

    boolean isJuanFirst = true;

    private void initJuanSpinner() {
        //猪圈
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, zhujuannames);
        SpinnerZhuJuan.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerZhuJuan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                } else {
                    juanId = dataBeans.get(position - 1).getJuanId();
                    Log.i("===juanId", juanId + "");
                    Log.i("===getSelectedItem", SpinnerZhuShe.getSelectedItem().toString() + "");
                    for (int i = 0; i < sheList.size(); i++) {
                        if (i > 0) {
                            Log.i("===sheListitem", sheList.get(i - 1).getName() + "");
                            if (sheList.get(i - 1).getName().equals(SpinnerZhuShe.getSelectedItem().toString())) {
                                sheId = sheList.get(i - 1).getSheId();
                            }
                        }
                    }


                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        juansel = SpinnerZhuJuan.getSelectedItem().toString();

    }


    public void setTitle(String title) {
        mTitle.setText(title);
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


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sure:
              /*  if (SpinnerZhuJuan.getSelectedItem().toString().equals("请选择猪圈") || SpinnerZhuShe.getSelectedItem().toString().equals("请选择猪舍")) {
                    Toast.makeText(context, "未选择猪舍/猪圈", Toast.LENGTH_LONG).show();
                } else {
                    getDataFromNet();
                }*/
                editRecoed();
                getDataFromNet();
                break;
            case R.id.cancel:
                PreferencesUtils.saveKeyValue(Constants.manualcount, "0", MyApplication.getAppContext());
                PreferencesUtils.saveBooleanValue("isfleg",false,context);
                cancel();
                break;
            default:
                break;
        }
    }

    private void getDataFromNet() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext())));
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        Map mapbody = new HashMap();
        String numzidongvalue = numzidong.getText().toString();
        String numshoudongvalue = numshoudong.getText().toString();
        mapbody.put(Constants.sheId, PreferencesUtils.getStringValue(Constants.sheId, context) + "");
        mapbody.put(Constants.juanId, PreferencesUtils.getStringValue(Constants.juanId, context) + "");
        mapbody.put(Constants.cutoCount, numzidongvalue);
        mapbody.put(Constants.count, numshoudongvalue);
        mapbody.put(Constants.videoId, PreferencesUtils.getStringValue(Constants.startVideoId, context));
        //经度
        mapbody.put(Constants.longitude, String.valueOf(LocationManager.getInstance(context).currentLon));
        //维度
        mapbody.put(Constants.latitude, String.valueOf(LocationManager.getInstance(context).currentLat));
        OkHttp3Util.uploadPreFile(Constants.XUNJIANTIJIAONEW, rcgImageFile, "aa.jpeg", mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String s = response.body().string();
                    Log.e("upload", "上传--" + s);
                    Activity appContext = (Activity) context;
                    appContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Commit bean = GsonUtils.getBean(s, Commit.class);
                            if (null != bean && bean.getStatus() == 1) {
                                SpinnerZhuJuan.setSelection(0);
                                Toast.makeText(MyApplication.getAppContext(), "保存成功", Toast.LENGTH_LONG).show();
                                PreferencesUtils.saveKeyValue(Constants.manualcount, numshoudongvalue, MyApplication.getAppContext());
                            }
                            dismiss();
                            PreferencesUtils.saveBooleanValue("isfleg",false,context);
                            appContext.finish();
                        }
                    });
                }
            }
        });
    }

    private void editRecoed() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext())));
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.videoId, PreferencesUtils.getStringValue(Constants.startVideoId, MyApplication.getAppContext(), "0"));
        OkHttp3Util.doPost(Constants.PRESTOP, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("editRecoed", e.getLocalizedMessage());
                editRecoed();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }


}
