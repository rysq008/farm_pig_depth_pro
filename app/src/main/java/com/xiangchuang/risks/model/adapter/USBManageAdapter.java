package com.xiangchuang.risks.model.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.SheXTBean;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.model.myinterface.MyInterface;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.PigPreferencesUtils;
import com.xiangchuang.risks.view.JuanSettingActivity;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.GsonUtils;
import com.innovation.pig.insurance.netutils.OkHttp3Util;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class USBManageAdapter extends BaseAdapter {
    private List<SheXTBean> sheXTBeans;
    private Context context;

    public static String TAG = "USBManageAdapter";
    private BaseActivity activity;

    List<String> shenames = new ArrayList<>();
    List<String> sheids = new ArrayList<>();
    private List<ZhuSheBean.DataBean> sheList;
    private String shename;
    private String sheid;
    private MyInterface myInterface;

    public USBManageAdapter(Context context, List<SheXTBean> sheXTBeans) {
        this.context = context;
        this.sheXTBeans = sheXTBeans;
    }

    @Override
    public int getCount() {
        return sheXTBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return sheXTBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.usbmanage_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.usb_xuliehao = (TextView) convertView.findViewById(R.id.usb_xuliehao);
            viewHolder.usb_name = (EditText) convertView.findViewById(R.id.usb_name);
            viewHolder.usb_spinner = (Spinner) convertView.findViewById(R.id.usb_spinner);
            viewHolder.usb_yanzheng = (TextView) convertView.findViewById(R.id.usb_yanzheng);
            viewHolder.usb_go = (TextView) convertView.findViewById(R.id.usb_go);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.usb_xuliehao.setText(sheXTBeans.get(position).cameraNo);
        viewHolder.usb_name.setText(sheXTBeans.get(position).cameraName);
        activity = (BaseActivity) context;
        if (Integer.valueOf(sheXTBeans.get(position).repair) == 0) {
            viewHolder.usb_go.setVisibility(View.VISIBLE);
            viewHolder.usb_yanzheng.setVisibility(View.GONE);
        } else if (Integer.valueOf(sheXTBeans.get(position).repair) == 1) {
            viewHolder.usb_go.setVisibility(View.GONE);
            viewHolder.usb_yanzheng.setVisibility(View.VISIBLE);
        }
        getZhuShe(viewHolder.usb_spinner, viewHolder.usb_yanzheng, viewHolder.usb_name, position);
        viewHolder.usb_yanzheng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PigPreferencesUtils.saveKeyValue(Constants.sheId, sheXTBeans.get(position).sheId, AppConfig.getAppContext());
                PigPreferencesUtils.saveKeyValue(Constants.cameraId, sheXTBeans.get(position).cameraId + "", AppConfig.getAppContext());
                PigPreferencesUtils.saveKeyValue(Constants.xu, sheXTBeans.get(position).cameraNo, AppConfig.getAppContext());
                PigPreferencesUtils.saveKeyValue(Constants.touname, sheXTBeans.get(position).cameraName, AppConfig.getAppContext());
                PigPreferencesUtils.saveKeyValue(Constants.shename, viewHolder.usb_spinner.getSelectedItem().toString(), AppConfig.getAppContext());
                String cameraNo = sheXTBeans.get(position).cameraNo;
                showDialog(cameraNo);
                /*if (Integer.valueOf(sheXTBeans.get(position).repair) == 1) {
                    String cameraNo = sheXTBeans.get(position).cameraNo;
                    showDialog(cameraNo);
                } else if (Integer.valueOf(sheXTBeans.get(position).repair) == 0) {
                    activity.goToActivity(JuanSettingActivity.class, null);
                }*/

            }
        });
        viewHolder.usb_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PigPreferencesUtils.saveKeyValue(Constants.sheId, sheXTBeans.get(position).sheId, AppConfig.getAppContext());
                PigPreferencesUtils.saveKeyValue(Constants.cameraId, sheXTBeans.get(position).cameraId + "", AppConfig.getAppContext());
                PigPreferencesUtils.saveKeyValue(Constants.xu, sheXTBeans.get(position).cameraNo, AppConfig.getAppContext());
                PigPreferencesUtils.saveKeyValue(Constants.touname, sheXTBeans.get(position).cameraName, AppConfig.getAppContext());
                String s = viewHolder.usb_spinner.getSelectedItem().toString();
                PigPreferencesUtils.saveKeyValue(Constants.shename, s, AppConfig.getAppContext());
                Log.i("==sheid=", sheXTBeans.get(position).sheId + "");
                if (!"-1".equals(sheXTBeans.get(position).sheId)) {
                    activity.goToActivity(JuanSettingActivity.class, null);
                }
            }
        });
        viewHolder.usb_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String musb_name = viewHolder.usb_name.getText().toString();
                    if ("".equals(musb_name)) {
                        activity.toastUtils.showLong(context, "摄像头名字为空");
                    } else if (!musb_name.equals(sheXTBeans.get(position).cameraName)) {
                        String s = viewHolder.usb_spinner.getSelectedItem().toString();
                        for (int i = 0; i < shenames.size(); i++) {
                            if (s.equals(shenames.get(i))) {
                                sheid = sheids.get(i);
                                Log.i("sheidnew====", i + "sheid" + sheid);
                            }
                        }
                        updateCamera(sheXTBeans.get(position).cameraNo, musb_name, sheXTBeans.get(position).cameraId, position, viewHolder.usb_spinner);
                    }
                }
            }
        });

        return convertView;
    }

    private void updateCamera(String cameraNo, String cameraName, int cameraId, int po, Spinner usbspinner) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PigPreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext()) + "");
        map.put(Constants.en_id, PigPreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0"));
        map.put(Constants.deptId, PigPreferencesUtils.getStringValue(Constants.deptId, AppConfig.getAppContext()));
        map.put(Constants.id, PigPreferencesUtils.getStringValue(Constants.id, AppConfig.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.cameraNo, cameraNo);
        mapbody.put(Constants.cameraName, cameraName);
        mapbody.put(Constants.cameraId, cameraId + "");
        mapbody.put(Constants.sheId, sheid);
        Log.i("updatesheid", sheid);
        OkHttp3Util.doPost(Constants.SXUPDATE, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e,USBManageAdapter.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("ppppppp", string);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(string);
                            int status = jsonObject.getInt("status");
                            String msg = jsonObject.getString("msg");
                            if (status != 1) {
                                activity.showDialogError(msg);
                            } else {
                                activity.toastUtils.showLong(context, msg);
                                sheXTBeans.get(po).setSheName(usbspinner.getSelectedItem().toString());
                                sheXTBeans.get(po).setCameraName(cameraName);
                                sheXTBeans.get(po).setSheId(sheid);
                                //myInterface.isOut(true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            AVOSCloudUtils.saveErrorMessage(e,USBManageAdapter.class.getSimpleName());
                        }
                    }
                });

            }
        });
    }

    private void getZhuShe(Spinner usbspinner, TextView usb_yanzheng, EditText usb_name, int mposition) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PigPreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext()) + "");
        map.put(Constants.en_id, PigPreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.amountFlg, String.valueOf(9));
        mapbody.put(Constants.insureFlg, String.valueOf(9));
        OkHttp3Util.doPost(Constants.ZHUSHESHOW, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e,USBManageAdapter.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                final ZhuSheBean bean = GsonUtils.getBean(string, ZhuSheBean.class);
                if (null != bean) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bean.getStatus() == 1) {
                                shenames.clear();
                                sheids.clear();
                                sheList = bean.getData();
                                shenames.add("理赔用");
                                sheids.add("-1");
                                if (null != sheList && sheList.size() > 0) {
                                    for (int i = 0; i < sheList.size(); i++) {
                                        shenames.add(sheList.get(i).getName());
                                        sheids.add(sheList.get(i).getSheId() + "");
                                    }
                                } else {
                                    activity.toastUtils.showLong(AppConfig.getAppContext(), "猪舍为空");
                                }
                                initSpinner(usbspinner, usb_yanzheng, sheXTBeans.get(mposition).sheName, usb_name, mposition);
                            } else {
                                activity.toastUtils.showLong(AppConfig.getAppContext(), bean.getMsg());
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

    private void initSpinner(Spinner usbspinner, TextView usb_yanzheng, String sheName, EditText usb_name, int mposition) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, shenames);
        usbspinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Log.i("sheName====", sheName + "mposition" + mposition);
        for (int i = 0; i < shenames.size(); i++) {
            if (sheName.equals(shenames.get(i))) {
                usbspinner.setSelection(i);
                sheid = sheids.get(i);
                Log.i("select====", i + "sheid" + sheid);
            }
        }
        usbspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shename = shenames.get(position);
                sheid = sheids.get(position);
                Log.i("==sheid:", sheid + "position" + position + "shename" + shename);
               /* if (!"-1".equals(sheid)) {
                    usb_yanzheng.setVisibility(View.VISIBLE);
                } else {
                    usb_yanzheng.setVisibility(View.GONE);
                }*/
                Log.i("==spsel:", usbspinner.getSelectedItem().toString() + "==sheXTBeansname:" + sheXTBeans.get(mposition).sheName + "position:" + mposition);
                if (!usbspinner.getSelectedItem().toString().equals(sheXTBeans.get(mposition).sheName)) {
                    updateCamera(sheXTBeans.get(mposition).cameraNo, usb_name.getText().toString(), sheXTBeans.get(mposition).cameraId, mposition, usbspinner);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showDialog(String cameraNo) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View inflate = View.inflate(context, R.layout.toubao_dialog_layout, null);
        TextView alert_top = inflate.findViewById(R.id.alert_top);
        TextView toubaodialogtitle = inflate.findViewById(R.id.toubao_dialog_title);
        TextView toubaodialogcancel = inflate.findViewById(R.id.toubao_dialog_cancel);
        TextView toubaodialogsure = inflate.findViewById(R.id.toubao_dialog_sure);
        EditText yancode = inflate.findViewById(R.id.yancode);
        yancode.setVisibility(View.GONE);
        dialog.setView(inflate);
        alert_top.setText("摄像头未验证");
        toubaodialogtitle.setText("序列号为" + cameraNo + "的摄像头验证码还未输入，无法绑定猪圈");
        AlertDialog dialogcreate = dialog.create();
        dialogcreate.setCanceledOnTouchOutside(false);
        dialogcreate.show();
        toubaodialogsure.setText("输入验证码");
        toubaodialogsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
                numberDialog(cameraNo);
            }
        });
        toubaodialogcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
            }
        });
    }

    private void numberDialog(String cameraNo) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View inflate = View.inflate(context, R.layout.toubao_dialog_layout, null);
        TextView alert_top = inflate.findViewById(R.id.alert_top);
        TextView toubaodialogtitle = inflate.findViewById(R.id.toubao_dialog_title);
        TextView toubaodialogcancel = inflate.findViewById(R.id.toubao_dialog_cancel);
        TextView toubaodialogsure = inflate.findViewById(R.id.toubao_dialog_sure);
        EditText yancode = inflate.findViewById(R.id.yancode);
        yancode.setVisibility(View.VISIBLE);
        yancode.setRawInputType(Configuration.KEYBOARD_QWERTY);
        dialog.setView(inflate);
        alert_top.setText("摄像头验证");
        toubaodialogtitle.setText("请输入序列号为" + cameraNo + "的摄像头验证码");
        AlertDialog dialogcreate = dialog.create();
        dialogcreate.setCanceledOnTouchOutside(false);
        dialogcreate.show();
        toubaodialogsure.setText("确定");
        toubaodialogsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
               /* if ("".equals(yancode.getText().toString())) {
                    activity.toastUtils.showLong(context, "验证码为空");
                } else {
                }*/
                checkverify(cameraNo, yancode.getText().toString(), dialogcreate);
            }
        });
        toubaodialogcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
            }
        });
    }

    private void checkverify(String cameraNo, String yancode, AlertDialog dialogcreate) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PigPreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext()) + "");
        map.put(Constants.en_id, PigPreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0"));
        map.put(Constants.deptIdnew, PigPreferencesUtils.getStringValue(Constants.deptId, AppConfig.getAppContext()));
        map.put(Constants.id, PigPreferencesUtils.getStringValue(Constants.id, AppConfig.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.cameraNo, cameraNo);
        mapbody.put(Constants.verificationCode, yancode);
        OkHttp3Util.doPost(Constants.YANZHENG, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e,USBManageAdapter.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("======", string);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(string);
                            int status = jsonObject.getInt("status");
                            String msg = jsonObject.getString("msg");
                            if (status != 1) {
                                dialogcreate.dismiss();
                                activity.showDialogError(msg);
                            } else {
                                dialogcreate.dismiss();
                                if (!"-1".equals(sheid)) {
                                    activity.toastUtils.showLong(context, msg);
                                    activity.goToActivity(JuanSettingActivity.class, null);
                                } else {
                                    myInterface.isOut(true);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            AVOSCloudUtils.saveErrorMessage(e,USBManageAdapter.class.getSimpleName());
                        }
                    }
                });

            }
        });
    }

    public void setListner(MyInterface myInterface) {
        this.myInterface = myInterface;
    }

    class ViewHolder {
        TextView usb_xuliehao, usb_yanzheng, usb_go;
        Spinner usb_spinner;
        EditText usb_name;
    }
}
