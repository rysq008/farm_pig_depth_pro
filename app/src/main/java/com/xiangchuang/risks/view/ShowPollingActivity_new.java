package com.xiangchuang.risks.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.SheListBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.CounterHelper;
import com.xiangchuang.risks.utils.PermissionsDelegate;
import com.xiangchuangtec.luolu.animalcounter.AppConfig;
import com.xiangchuangtec.luolu.animalcounter.model.PollingResultAdapter_new;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.tensorflow.demo.BreedingDetectorActivity_pig;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import innovation.database.SheInfo;
import innovation.utils.Toast;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xiangchuangtec.luolu.animalcounter.AppConfig.offLineModle;

/**
 * @authorlxr 2018.08.30
 * 展示巡检结果
 */
public class ShowPollingActivity_new extends BaseActivity implements View.OnClickListener {

    TextView mshowpollingname;
    TextView mshowpollingnumber;
    ListView mshowpollingresult_list;
    RelativeLayout rlTitle;
    ImageView ivCancel;

    private String recodetitle;
    private String recodenumber;
    private String no;
    List<SheListBean.DataOffLineBaodanBean> mSheBeans;
    final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);

    private List<SheInfo> sheInfoList;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_show_polling_new;
    }

    @Override
    public void initView() {
        super.initView();
        mshowpollingname = findViewById(R.id.tv_title);
        mshowpollingnumber = findViewById(R.id.showpolling_number);
        mshowpollingresult_list = findViewById(R.id.showpolling_result_list);
        rlTitle = findViewById(R.id.rl_title);
        ivCancel = findViewById(R.id.iv_cancel);

        ivCancel.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        String companyname = PreferencesUtils.getStringValue(Constants.companyname, AppConfig.getAppContext(), "育肥猪农场");
        mshowpollingname.setText("智能点数");

        String enId = PreferencesUtils.getStringValue(Constants.en_id, ShowPollingActivity_new.this);
        getDataFromNet(enId);
    }

    private void getDataFromNet(String enId) {
        Log.i("ShowPollingActivity", "enId" + enId);
//        String url = "http://test1.innovationai.cn:8081/numberCheck/app/sheList";
        mLoadProgressDialog.show();
        OkHttp3Util.doPost(Constants.SHELIST, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("ShowPollingActivity", e.toString());
                mLoadProgressDialog.show();
                AVOSCloudUtils.saveErrorMessage(e, ShowPollingActivity_new.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("ShowPollingActivity", string);
                mLoadProgressDialog.dismiss();
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
                                            intent.putExtra("pigtype", mSheBeans.get(position).getPigType());
                                            startActivity(intent);
                                        }
                                    });
                                    mshowpollingresult_list.setAdapter(resultAdapter);
                                    resultAdapter.setListener(new PollingResultAdapter_new.OnDetailitemClickListener() {
                                        @Override
                                        public void onClick(int position) {
                                            SelectFunctionActivity_new.g_SheID = String.valueOf(mSheBeans.get(position).getSheId());
                                            SelectFunctionActivity_new.g_PigType = mSheBeans.get(position).getPigType();
                                            //Toast.makeText(this, deviceHashMap.size() + "", Toast.LENGTH_LONG).show();
                                            CounterHelper.number = 1;
                                            if (isOPen(ShowPollingActivity_new.this)) {
                                                //判断如果是能繁母猪点数进入新的点数逻辑界面
                                                if (mSheBeans.get(position).getPigType().equals("102")) {
                                                    //改完
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowPollingActivity_new.this);
                                                    LayoutInflater inflater = LayoutInflater.from(ShowPollingActivity_new.this);
                                                    View v = inflater.inflate(R.layout.breeding_select_dialog_layout, null);
                                                    Dialog dialog = builder.create();
                                                    dialog.show();
                                                    dialog.getWindow().setContentView(v);

                                                    TextView location = v.findViewById(R.id.tv_location_select);
                                                    TextView captivity = v.findViewById(R.id.tv_captivity_select);

                                                    TextView close = v.findViewById(R.id.iv_close);
                                                    //定位栏  能繁
                                                    location.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            dialog.dismiss();
//                                                            g_CaptivityMap.put(g_SheID, g_CaptivityMap.get(g_SheID));
                                                            Intent intent = new Intent(ShowPollingActivity_new.this, BreedingDetectorActivity_pig.class);
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
                                                    });
                                                    //圈养
                                                    captivity.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            HashMap<String, UsbDevice> deviceHashMap = ((UsbManager) getSystemService(USB_SERVICE)).getDeviceList();
                                                            if (deviceHashMap.size() > 0) {
                                                                dialog.dismiss();
//                                                                g_CaptivityMap.put(g_SheID, null);
                                                                //摄像头页面
                                                                Intent intent = new Intent(ShowPollingActivity_new.this, USBCameraActivity_new.class);
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
                                                                ToastUtils.show("请连接外接摄像头。");
                                                                return;
                                                            }
                                                        }
                                                    });
                                                    close.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            dialog.dismiss();
                                                        }
                                                    });

                                                    dialog.getWindow().setGravity(Gravity.CENTER);
                                                    dialog.setCancelable(false);

                                                } else {
                                                    HashMap<String, UsbDevice> deviceHashMap = ((UsbManager) getSystemService(USB_SERVICE)).getDeviceList();
                                                    if (deviceHashMap.size() > 0) {
//                                                        g_CaptivityMap.put(g_SheID, null);
                                                        //摄像头页面
                                                        Intent intent = new Intent(ShowPollingActivity_new.this, USBCameraActivity_new.class);
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
//                                                        ToastUtils.show("请连接外接摄像头。");
                                                        Toast.makeText(ShowPollingActivity_new.this, "请连接外接摄像头。", Toast.LENGTH_LONG).show();
                                                        return;
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
                                    ToastUtils.show("暂无猪舍记录");
//                                    Toast.makeText(ShowPollingActivity_new.this, "暂无猪舍记录", Toast.LENGTH_LONG).show();
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
        if (offLineModle) {
            for (SheInfo bean : sheInfoList) {
                totalCount += Integer.parseInt(bean.count);
            }
        } else {
            for (SheListBean.DataOffLineBaodanBean bean : mSheBeans) {
                totalCount += bean.getCount();
            }
        }

        mshowpollingnumber.setText(totalCount + "");
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_cancel) {
            finish();
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
