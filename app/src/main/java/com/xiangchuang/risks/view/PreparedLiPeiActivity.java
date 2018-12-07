package com.xiangchuang.risks.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.StartBean;
import com.xiangchuang.risks.model.bean.ZhuJuanBean;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.json.JSONObject;
import org.tensorflow.demo.DetectorActivity;
import org.tensorflow.demo.Global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import innovation.media.Model;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PreparedLiPeiActivity extends BaseActivity {
    public static String TAG = "PreparedLiPeiActivity";
    @BindView(R.id.pre_zhushe)
    Spinner mprezhushe;
    @BindView(R.id.pre_zhujuan)
    Spinner mprezhujuan;
    @BindView(R.id.prepared_begin)
    TextView mpreparedbegin;
    @BindView(R.id.pre_li_title)
    TextView mprelititle;
    @BindView(R.id.prepared_apply)
    TextView mpreparedapply;
    @BindView(R.id.pre_zhujuan_chuxian)
    Spinner mprezhujuanchuxian;
    @BindView(R.id.chuxian_num)
    TextView mchuxiannum;

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;

    private String en_id;
    private int defaultpig;
    List<String> zhushename = new ArrayList<>();
    List<String> zhujuanname = new ArrayList<>();
    List<String> reasons = new ArrayList<>();
    private int userid;
    private List<ZhuSheBean.DataBean> sheList;
    private List<ZhuJuanBean.DataBean> juanList;
    private int juanId;
    private int sheId;
    private String outreson;
    private String fleg;
    private String userLibId;
    private AMapLocationClient mLocationClient;
    private String msg;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_prepared_li_pei;
    }

    @Override
    protected void initData() {
        String mfleg = PreferencesUtils.getStringValue(Constants.fleg, MyApplication.getAppContext());
        //预理赔
        if ("pre".equals(mfleg)) {
            mpreparedbegin.setVisibility(View.VISIBLE);
            mpreparedapply.setVisibility(View.GONE);
            //mprelititle.setText("预理赔");
            tv_title.setText("预理赔");
            //理赔
        } else if ("lipei".equals(mfleg)) {
            mpreparedbegin.setVisibility(View.GONE);
            mpreparedapply.setVisibility(View.GONE);
            // mprelititle.setText("理赔信息");
            tv_title.setText("理赔");
        }
        en_id = PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0");
        userid = PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext());
        addreasons();
        getDataFromNet();
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void addreasons() {
        // strReason = new String[]{"传染病（疫病）", "非传染病", "疫病/疾病免疫副反应", "中毒", "扑杀", "意外事故", "难产"};
        reasons.clear();
        reasons.add("传染病/疫病");
        reasons.add("非传染病");
        reasons.add("疫病/疾病免疫副反应");
        reasons.add("中毒");
        reasons.add("扑杀");
        reasons.add("意外事故");
        reasons.add("难产");
        outreson = reasons.get(0);
        initReasonSpinner();
    }

    private void getDataFromNet() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(userid));
        map.put(Constants.en_id, en_id);
        Map mapbody = new HashMap();
        mapbody.put(Constants.amountFlg, String.valueOf(9));
        mapbody.put(Constants.insureFlg, String.valueOf(1));
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUSHESHOW, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                final ZhuSheBean bean = GsonUtils.getBean(string, ZhuSheBean.class);
                if (null != bean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                zhushename.clear();
                                sheList = bean.getData();
                                if (sheList.size() > 0) {
                                    sheId = sheList.get(0).getSheId();
                                    for (int i = 0; i < sheList.size(); i++) {
                                        zhushename.add(sheList.get(i).getName());
                                    }
                                    initSpinner();
                                    getzhujuanMessage(sheId + "");
                                } else {
                                    Toast.makeText(PreparedLiPeiActivity.this, "没有猪圈", Toast.LENGTH_LONG).show();
                                }
                            } else if (bean.getStatus() == 0) {
                                Toast.makeText(PreparedLiPeiActivity.this, bean.getMsg(), Toast.LENGTH_LONG).show();
                            } else if (bean.getStatus() == -1) {
                                Toast.makeText(PreparedLiPeiActivity.this, bean.getMsg(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PreparedLiPeiActivity.this, "查询失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });


    }

    private void getzhujuanMessage(String pighouseid) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, userid + "");
        map.put(Constants.en_id, en_id);
        Map mapbody = new HashMap();
        mapbody.put(Constants.amountFlg, 9 + "");
        mapbody.put(Constants.insureFlg, 1 + "");
        mapbody.put(Constants.sheId, pighouseid);
        OkHttp3Util.doPost(Constants.ZHUJUANSHOW, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                final ZhuJuanBean bean = GsonUtils.getBean(string, ZhuJuanBean.class);
                if (null != bean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bean.getStatus() == 1) {
                                juanList = bean.getData();
                                if (juanList.size() > 0) {
                                    zhujuanname.clear();
                                    for (int i = 0; i < juanList.size(); i++) {
                                        zhujuanname.add(juanList.get(i).getName());
                                    }
                                    juanId = juanList.get(0).getJuanId();
                                    initJuanSpinner();
                                } else {
                                    mprezhujuan.setAdapter(null);
                                }
                            } else if (bean.getStatus() == 0) {
                                Toast.makeText(PreparedLiPeiActivity.this, bean.getMsg(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PreparedLiPeiActivity.this, "添加失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });


    }

    private void initJuanSpinner() {
        //猪圈
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, zhujuanname);
        mprezhujuan.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mprezhujuan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                juanId = juanList.get(position).getJuanId();
                getBaoDanNumber();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getBaoDanNumber() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put("Content-Type", "application/x-www-form-urlencoded");
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.juanId, String.valueOf(juanId));
        OkHttp3Util.doPost(Constants.JUANBAONUM, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("end2", e.getLocalizedMessage());
                showErrorDialogLiTimeOut();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject jsonObject = null;
                            try {
                                String s = response.body().string();
                                jsonObject = new JSONObject(s);
                                int status = jsonObject.getInt("status");

                                if (status == 1) {
                                    String data = jsonObject.optString("data");
                                    mchuxiannum.setText(data);
                                } else {
                                    String msg = jsonObject.optString("msg");
                                    mchuxiannum.setText(msg);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });

                }
            }
        });


    }

    private void initSpinner() {
        //猪舍
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, zhushename);
        mprezhushe.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mprezhushe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sheId = sheList.get(position).getSheId();
                getzhujuanMessage(sheList.get(position).getSheId() + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void initReasonSpinner() {
        //出险原因
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, reasons);
        mprezhujuanchuxian.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mprezhujuanchuxian.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                outreson = reasons.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @OnClick({R.id.prepared_begin, R.id.prepared_apply})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.prepared_begin:
                if (null == mchuxiannum.getText().toString() || "".equals(mchuxiannum.getText().toString())) {
                    Toast.makeText(MyApplication.getAppContext(), "请填写有效的保单号", Toast.LENGTH_LONG).show();
                } else {
                    collectToNet();
                }
                break;
            case R.id.prepared_apply:
                if (null == mchuxiannum.getText().toString() || "".equals(mchuxiannum.getText().toString())) {
                    Toast.makeText(MyApplication.getAppContext(), "请填写有效的保单号", Toast.LENGTH_LONG).show();
                } else {
                    userLibId = PreferencesUtils.getStringValue(Constants.userLibId, MyApplication.getAppContext(), "0");
                    goonLi();
                }
                break;
            default:
                break;
        }

    }

    private void goonLi() {
        getCurrentLocationLatLng();
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext())));
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.userLibId, String.valueOf(userLibId));
        mapbody.put(Constants.juanId, String.valueOf(juanId));
        mapbody.put(Constants.sheId, String.valueOf(sheId));
        mapbody.put(Constants.insureNo, String.valueOf(mchuxiannum.getText().toString()));
        mapbody.put(Constants.reason, outreson);
        mapbody.put(Constants.compensateVideoId, PreferencesUtils.getStringValue(Constants.preVideoId, MyApplication.getAppContext(), "0"));
        mapbody.put(Constants.address, str_address);
        //经度
        mapbody.put(Constants.longitude, String.valueOf(currentLon));
        //维度
        mapbody.put(Constants.latitude, String.valueOf(currentLat));
        OkHttp3Util.doPost(Constants.LIEDD2, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("end2", e.getLocalizedMessage());
                showErrorDialogLiTimeOut();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject jsonObject = null;
                            try {
                                String s = response.body().string();
                                Log.e("goonPreAct", "--" + s);
                                jsonObject = new JSONObject(s);
                                int status = jsonObject.getInt("status");
                                String msg = jsonObject.getString("msg");
                                if (status == -1 || 0 == status) {
                                    showErrorDialogLi(msg);
                                } else {
                                    StartBean bean = GsonUtils.getBean(s, StartBean.class);
                                    if (1 == bean.getStatus()) {
                                        msg = bean.getMsg();
                                        showSucessDialog(msg);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });

                }
            }
        });
    }


    private void collectToNet() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(userid));
        map.put(Constants.en_id, en_id);
        Map mapbody = new HashMap();
        mapbody.put(Constants.juanId, String.valueOf(juanId));
        mapbody.put(Constants.insureNo, mchuxiannum.getText().toString());

        Log.i("juanId", juanId + "");
        Log.i("insureNo", mchuxiannum.getText().toString());
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.PRESTART, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                final StartBean bean = GsonUtils.getBean(string, StartBean.class);
                if (null != bean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                //PreferencesUtils.saveKeyValue(Constants.preVideoId, bean.getData(), MyApplication.getAppContext());
                                Global.model = Model.VERIFY.value();
                                Intent intent = new Intent(PreparedLiPeiActivity.this, DetectorActivity.class);
                                intent.putExtra(Constants.sheId, sheId + "");
                                intent.putExtra(Constants.juanId, juanId + "");
                                intent.putExtra(Constants.inspectNo, mchuxiannum.getText().toString());
                                intent.putExtra(Constants.reason, outreson);
                                startActivity(intent);
                                // finish();
                            } else {
                                AlertDialogManager.showMessageDialog(PreparedLiPeiActivity.this, "提示", bean.getMsg(), new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {

                                    }

                                    @Override
                                    public void onNegative() {

                                    }
                                });
                                //Toast.makeText(MyApplication.getAppContext(), bean.getMsg(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PreparedLiPeiActivity.this, "开始采集失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    private void showErrorDialogLiTimeOut() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View inflate = View.inflate(this, R.layout.pre_timeout, null);
        TextView timeout_resert = inflate.findViewById(R.id.timeout_resert);
        TextView timeout_cancel = inflate.findViewById(R.id.timeout_cancel);
        dialog.setView(inflate);
        AlertDialog dialogcreate = dialog.create();
        dialogcreate.setCanceledOnTouchOutside(false);
        dialogcreate.show();
        timeout_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();

            }
        });
        timeout_resert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
                goonLi();
            }
        });
    }

    private void showErrorDialogLi(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View inflate = View.inflate(this, R.layout.lipei_result4, null);
        TextView result4_msg = inflate.findViewById(R.id.result4_msg);
        TextView result4_edit = inflate.findViewById(R.id.result4_edit);
        result4_msg.setText(msg);
        dialog.setView(inflate);
        AlertDialog dialogcreate = dialog.create();
        dialogcreate.setCanceledOnTouchOutside(false);
        dialogcreate.show();
        result4_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
            }
        });
    }

    private void showSucessDialog(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View inflate = View.inflate(this, R.layout.prelipei_result2, null);
        TextView result2_edit = inflate.findViewById(R.id.result2_edit);
        TextView success_msg = inflate.findViewById(R.id.success_msg);
        success_msg.setText(msg);
        dialog.setView(inflate);
        AlertDialog dialogcreate = dialog.create();
        dialogcreate.setCanceledOnTouchOutside(false);
        dialogcreate.show();
        result2_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) { //表示按返回键 时的操作
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getCurrentLocationLatLng() {
        //初始化定位
        mLocationClient = new AMapLocationClient(PreparedLiPeiActivity.this);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setOnceLocation(true);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。默认连续定位 切最低时间间隔为1000ms
        mLocationOption.setInterval(3500);
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }


    private double currentLat;
    private double currentLon;
    private String str_address = "";
    private final AMapLocationListener mLocationListener = amapLocation -> {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                currentLat = amapLocation.getLatitude();//获取纬度
                currentLon = amapLocation.getLongitude();//获取经度
                //  str_address = amapLocation.getAddress();
                str_address = mLocationClient.getLastKnownLocation().getAddress();
                ;

                amapLocation.getAccuracy();//获取精度信息
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    };
}
