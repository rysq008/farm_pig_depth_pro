package com.xiangchuang.risks.view;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.SheListBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.CounterHelper;
import com.xiangchuang.risks.utils.PermissionsDelegate;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.model.PollingResultAdapter_new;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.GsonUtils;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;

import org.tensorflow.demo.BreedingDetectorActivityBreeding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @authorlxr 2018.08.30
 * 展示巡检结果
 */
public class ShowPollingActivity_new extends BaseActivity {

    TextView mshowpollingname;

    TextView mshowpollingnumber;

    ListView mshowpollingresult_list;

    RelativeLayout rlTitle;


    private String recodetitle;
    private String recodenumber;
    private String no;
    List<SheListBean.DataOffLineBaodanBean> mSheBeans;
    final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private static long internalTime;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_show_polling_new;
    }

    @Override
    public void initView() {
        super.initView();
        mshowpollingname = (TextView) findViewById(R.id.tv_title);
        mshowpollingnumber = (TextView) findViewById(R.id.showpolling_number);
        mshowpollingresult_list = (ListView) findViewById(R.id.showpolling_result_list);
        rlTitle = (RelativeLayout) findViewById(R.id.rl_title);
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        String companyname = PreferencesUtils.getStringValue(Constants.companyname, AppConfig.getAppContext(), "育肥猪农场");
        mshowpollingname.setText(companyname);
//        FarmerPreferencesUtils.saveIntValue(Constants.deptId, mdeptid, ShowPollingActivity_new.this);
    }

    @Override
    protected void initData() {

        String enId = PreferencesUtils.getStringValue(Constants.en_id, ShowPollingActivity_new.this);
        getDataFromNet(enId);
    }

    private void getDataFromNet(String enId) {
        Log.i("ShowPollingActivity", "enId" + enId);
//        String url = "http://47.92.167.61:8081/numberCheck/app/sheList";
        OkHttp3Util.doPost(Constants.SHELIST, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("ShowPollingActivity", e.toString());
                AVOSCloudUtils.saveErrorMessage(e,ShowPollingActivity_new.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("ShowPollingActivity", string);
                final SheListBean bean = GsonUtils.getBean(string, SheListBean.class);
                if (null != bean && null != bean.getData()) {
                    if (1 == bean.getStatus()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String format = new SimpleDateFormat("yyyy年MM月dd日").format(new Date());
                                recodetitle = format + "第" + no + "次巡检";
                                if (bean.getData().size() > 0) {
                                    mSheBeans = bean.getData();

                                    PollingResultAdapter_new resultAdapter = new PollingResultAdapter_new(
                                            ShowPollingActivity_new.this, mSheBeans, new PollingResultAdapter_new.OnDetailClickListener() {
                                        @Override
                                        public void onClick(int position) {
                                            Intent intent = new Intent(ShowPollingActivity_new.this, HogDetailActivity_new.class);
                                            intent.putExtra("sheid", mSheBeans.get(position).getSheId());
                                            intent.putExtra("pigtype",mSheBeans.get(position).getPigType() );
                                            startActivity(intent);
                                        }
                                    });
                                    mshowpollingresult_list.setAdapter(resultAdapter);
                                    resultAdapter.setListener(new PollingResultAdapter_new.OnDetailitemClickListener() {
                                        @Override
                                        public void onClick(int position) {
                                            HashMap<String, UsbDevice> deviceHashMap = ((UsbManager) getSystemService(USB_SERVICE)).getDeviceList();
                                            //Toast.makeText(this, deviceHashMap.size() + "", Toast.LENGTH_LONG).show();
                                            Intent intent = null;
                                            CounterHelper.number = 1;
                                            if (isOPen(ShowPollingActivity_new.this)) {
                                                //判断如果是能繁母猪点数进入新的点数逻辑界面
                                                if (mSheBeans.get(position).getPigType().equals("102")) {
                                                    //延时3s，防止重复启动页面
                                                    if(System.currentTimeMillis() - internalTime > 2000){
                                                        intent = new Intent(ShowPollingActivity_new.this, BreedingDetectorActivityBreeding.class);
                                                        intent.putExtra("recodetitle", recodetitle);
                                                        intent.putExtra("recodenumber", recodenumber);
                                                        intent.putExtra("no", no);
                                                        intent.putExtra("pigcount", String.valueOf(mSheBeans.get(position).getCount()));
                                                        intent.putExtra("duration", mSheBeans.get(position).getTimeLength());
                                                        intent.putExtra("autocount", String.valueOf(mSheBeans.get(position).getAutoCount()));
                                                        intent.putExtra("sheid", String.valueOf(mSheBeans.get(position).getSheId()));
                                                        intent.putExtra("shename", mSheBeans.get(position).getSheName());
                                                        intent.putExtra("juancnt", mSheBeans.get(position).getJuanCnt());
                                                        startActivity(intent);
                                                    }
                                                    internalTime = System.currentTimeMillis();

                                                } else {
                                                    if (deviceHashMap.size() > 0) {
                                                        //摄像头页面
                                                        intent = new Intent(ShowPollingActivity_new.this, USBCameraActivity_newUsb.class);
                                                        intent.putExtra("recodetitle", recodetitle);
                                                        intent.putExtra("recodenumber", recodenumber);
                                                        intent.putExtra("no", no);
                                                        intent.putExtra("pigcount", String.valueOf(mSheBeans.get(position).getCount()));
                                                        intent.putExtra("duration", mSheBeans.get(position).getTimeLength());
                                                        intent.putExtra("autocount", String.valueOf(mSheBeans.get(position).getAutoCount()));
                                                        intent.putExtra("sheid", String.valueOf(mSheBeans.get(position).getSheId()));
                                                        intent.putExtra("shename", mSheBeans.get(position).getSheName());
                                                        intent.putExtra("juancnt", mSheBeans.get(position).getJuanCnt());
                                                        startActivity(intent);
                                                    } else {
                                                        Toast.makeText(ShowPollingActivity_new.this, "请连接外接摄像头。", Toast.LENGTH_LONG).show();
                                                        return;
                                                    /*if (!permissionsDelegate.hasCameraPermission()) {
                                                        permissionsDelegate.requestCameraPermission();
                                                        return;
                                                    } else {
                                                        intent = new Intent(ShowPollingActivity_new.this, CounterActivity_new.class);
                                                    }*/
                                                    }
                                                }
                                            } else {
                                                AlertDialogManager.showMessageDialog(ShowPollingActivity_new.this, "提示", getString(R.string.locationwarning), new AlertDialogManager.DialogInterface() {
                                                    @Override
                                                    public void onPositive() {
                                                        openGPS1(ShowPollingActivity_new.this);
                                                    }

                                                    @Override
                                                    public void onNegative() {

                                                    }
                                                });

                                            }
                                        }
                                    });
                                    setTotalCount();
                                } else {
                                    Toast.makeText(ShowPollingActivity_new.this, "暂无猪舍记录", Toast.LENGTH_LONG).show();
                                }

                            }
                        });
                    }
                }

            }
        });

    }

    private void setTotalCount() {
        int totalCount = 0;
        for (SheListBean.DataOffLineBaodanBean bean : mSheBeans) {
            totalCount += bean.getCount();
        }
        mshowpollingnumber.setText(totalCount + "");
    }


    public void onClickView(View view) {
        int i = view.getId();
        if (i == R.id.iv_cancel) {
            finish();

        } else {
        }

    }

    public static final boolean isOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    private void openGPS1(Context mContext) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        ShowPollingActivity_new activity = (ShowPollingActivity_new) mContext;
        activity.startActivityForResult(intent, 1315);
    }
}
