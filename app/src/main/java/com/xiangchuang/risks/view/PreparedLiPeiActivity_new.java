package com.xiangchuang.risks.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.USBManageAdapter;
import com.xiangchuang.risks.model.bean.SheListBean;
import com.xiangchuang.risks.model.bean.StartBean;
import com.xiangchuang.risks.model.bean.StartBean_new;
import com.xiangchuang.risks.model.bean.ZhuJuanBean;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.CounterHelper;
import com.xiangchuangtec.luolu.animalcounter.CounterActivity_new;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.model.PollingResultAdapter_new;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;
import com.xiangchuangtec.luolu.animalcounter.view.ShowPollingActivity_new;
import com.xiangchuangtec.luolu.animalcounter.view.USBCameraActivity_new;

import org.json.JSONObject;
import org.tensorflow.demo.DetectorActivity;
import org.tensorflow.demo.Global;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import innovation.media.Model;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xiangchuangtec.luolu.animalcounter.MyApplication.isNoCamera;

/**
 * 预理赔
 */
public class PreparedLiPeiActivity_new extends BaseActivity {
    public static String TAG = "PreparedLiPeiActivity_new";
    @BindView(R.id.pre_zhushe)
    Spinner mprezhushe;
    @BindView(R.id.prepared_begin)
    TextView mpreparedbegin;
    @BindView(R.id.pre_li_title)
    TextView mprelititle;
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
    List<String> reasons = new ArrayList<>();
    private int userid;
    private String sheId;
    private String outreson;
    private String fleg;
    private String userLibId;
    private AMapLocationClient mLocationClient;
    private String msg;
    private List<SheListBean.DataOffLineBaodanBean> mSheBeans;
    private boolean hasInNo = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_prepared_li_pei_new;
    }

    @Override
    protected void initData() {
        String mfleg = PreferencesUtils.getStringValue(Constants.fleg, MyApplication.getAppContext());
        String companyName = PreferencesUtils.getStringValue(Constants.companyname, MyApplication.getAppContext());
        mprelititle.setText(companyName);
        //预理赔
        if ("pre".equals(mfleg)) {
            mpreparedbegin.setVisibility(View.VISIBLE);
            tv_title.setText("预理赔");
            //理赔
        } else if ("lipei".equals(mfleg)) {
            mpreparedbegin.setVisibility(View.GONE);
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

    //获取猪舍列表
    private void getDataFromNet() {
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.SHELIST,null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i("PreparedLiPeiActivity", e.toString());
                AVOSCloudUtils.saveErrorMessage(e,PreparedLiPeiActivity_new.class.getSimpleName());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mProgressDialog.dismiss();
                String string = response.body().string();
                Log.i("PreparedLiPeiActivity", string);
                try{
                    final SheListBean bean = GsonUtils.getBean(string, SheListBean.class);
                    if (null != bean && null != bean.getData()) {
                        if (1 == bean.getStatus()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (bean.getData().size() > 0) {
                                        mSheBeans = bean.getData();

                                        zhushename.clear();
                                        if (mSheBeans.size() > 0) {
                                            sheId = mSheBeans.get(0).getSheId();
                                            for (int i = 0; i < mSheBeans.size(); i++) {
                                                zhushename.add(mSheBeans.get(i).getSheName()+"_"+mSheBeans.get(i).getPigTypeName());
                                            }
                                            initSpinner();
                                        } else {
                                            Toast.makeText(PreparedLiPeiActivity_new.this, "没有猪圈", Toast.LENGTH_LONG).show();
                                        }

                                    } else {
                                        Toast.makeText(PreparedLiPeiActivity_new.this, "暂无猪舍记录", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(PreparedLiPeiActivity_new.this, "暂无猪舍记录", Toast.LENGTH_LONG).show();
                    AVOSCloudUtils.saveErrorMessage(e,PreparedLiPeiActivity_new.class.getSimpleName());
                }
            }
        });

    }

    private void getBaoDanNumber() {
//        Map map = new HashMap();
//        map.put("Content-Type", "application/x-www-form-urlencoded");
        Map mapbody = new HashMap();
        mapbody.put(Constants.sheId, sheId);
        OkHttp3Util.doPost(Constants.JUANBAONUM, mapbody,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("end2", e.getLocalizedMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorDialogLiTimeOut();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e,PreparedLiPeiActivity_new.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("PreparedLiPeiActivity", string);
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(string);
                                int status = jsonObject.getInt("status");

                                if (status == 1) {
                                    String data = jsonObject.optString("data");
                                    mchuxiannum.setText(data);
                                    hasInNo = true;
                                } else {
                                    String msg = jsonObject.optString("msg");
                                    mchuxiannum.setText(msg);
                                    hasInNo = false;
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                AVOSCloudUtils.saveErrorMessage(e,PreparedLiPeiActivity_new.class.getSimpleName());
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
                if(position!=0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialogManager.showMessageDialog(PreparedLiPeiActivity_new.this, "提示",
                                    //【猪舍1】距当前位置较远(80米)，距当前位置最近的猪舍为【舍2】，距离50米。
                                    //请确认选择的猪舍是否正确。

                                    "所选猪舍距当前位置较远(" + mSheBeans.get(position).getDistance() +"米)，" +
                                            "\n距当前位置最近的猪舍为：\n"+zhushename.get(0)+",距离"+mSheBeans.get(0).getDistance()+"米。" +
                                            "\n请确认选择的猪舍是否正确。", new AlertDialogManager.DialogInterface() {
                                @Override
                                public void onPositive() {

                                }

                                @Override
                                public void onNegative() {
                                    mprezhushe.setSelection(0);
                                }
                            });
                        }
                    });

                }
                sheId = mSheBeans.get(position).getSheId();
                getBaoDanNumber();
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

    @OnClick({R.id.prepared_begin})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.prepared_begin:
                if (!hasInNo) {
                    Toast.makeText(MyApplication.getAppContext(), "保单号为空，无法申请预理赔。", Toast.LENGTH_LONG).show();
                } else {
                    collectToNet();
                }
                break;
            default:
                break;
        }

    }

    private void collectToNet() {
        Map map = new HashMap();
        map.put(Constants.en_user_id, String.valueOf(userid));
        Map mapbody = new HashMap();
        mapbody.put(Constants.sheId, sheId);
        mapbody.put(Constants.insureNo, mchuxiannum.getText().toString());
        Log.i("insureNo", mchuxiannum.getText().toString());
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.PRESTART, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e,PreparedLiPeiActivity_new.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mProgressDialog.dismiss();
                String string = response.body().string();
                Log.i(TAG, string);
                final StartBean_new bean = GsonUtils.getBean(string, StartBean_new.class);
                if (null != bean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           StringBuffer  stringBuffer= new StringBuffer();
                           String str = null;
                            if (bean.getStatus() == 1) {
                                if(bean.getData().size()>0&&!bean.getData().isEmpty()){
                                    for(int i = 0;i<bean.getData().size();i++){
                                        stringBuffer.append(bean.getData().get(i));
                                        if(i<bean.getData().size()-1) {
                                            stringBuffer.append(",");
                                        }
                                    }
                                    str = stringBuffer.toString();
                                    Log.i("stringbuffer",str);
                                }
                                isNoCamera = false;
                                PreferencesUtils.saveKeyValue(Constants.preVideoId,str , MyApplication.getAppContext());
                                Global.model = Model.VERIFY.value();
                                Intent intent = new Intent(PreparedLiPeiActivity_new.this, DetectorActivity.class);
                                intent.putExtra(Constants.sheId, sheId + "");
                                intent.putExtra(Constants.inspectNo, mchuxiannum.getText().toString());
                                intent.putExtra(Constants.reason, outreson);
                                startActivity(intent);
                                // finish();
                            } else if(bean.getStatus() == 0) {
                                isNoCamera = true;
                                Global.model = Model.VERIFY.value();
                                PreferencesUtils.saveKeyValue(Constants.preVideoId,"" , MyApplication.getAppContext());
                                Intent intent = new Intent(PreparedLiPeiActivity_new.this, DetectorActivity.class);
                                intent.putExtra(Constants.sheId, sheId + "");
                                intent.putExtra(Constants.inspectNo, mchuxiannum.getText().toString());
                                intent.putExtra(Constants.reason, outreson);
                                startActivity(intent);
                            }else{
                                AlertDialogManager.showMessageDialog(PreparedLiPeiActivity_new.this, "提示", bean.getMsg(), new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() { }

                                    @Override
                                    public void onNegative() { }
                                });
                                    Toast.makeText(MyApplication.getAppContext(), bean.getMsg(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PreparedLiPeiActivity_new.this, "开始采集失败,请重试。", Toast.LENGTH_LONG).show();
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
                getBaoDanNumber();
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
        mLocationClient = new AMapLocationClient(PreparedLiPeiActivity_new.this);
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
