package com.xiangchuang.risks.view;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.USBManageAdapter;
import com.xiangchuang.risks.model.adapter.ZhuSheXinXI_item_Adapter;
import com.xiangchuang.risks.model.bean.InSureCompanyBean;
import com.xiangchuang.risks.model.bean.SheXTBean;
import com.xiangchuang.risks.model.bean.UpdateBean;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.model.myinterface.MyInterface;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class USBMenageActivity extends BaseActivity {
    public static String TAG = "USBMenageActivity";
    @BindView(R.id.usbmanage_list)
    ListView usbmanage_list;
    @BindView(R.id.she_right_image)
    TextView she_right_image;
    @BindView(R.id.she_xu)
    EditText she_xu;
    @BindView(R.id.she_name)
    EditText she_name;
    @BindView(R.id.she_she)
    Spinner she_she;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    List<String> shenames = new ArrayList<>();
    List<String> sheids = new ArrayList<>();
    List<SheXTBean> sheXTBeans = new ArrayList<>();
    private List<ZhuSheBean.DataBean> sheList;
    private String shename;
    private String sheid;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_usbmenage;
    }

    @Override
    protected void initData() {
        she_xu.setRawInputType(Configuration.KEYBOARD_QWERTY);
        getZhuShe();
        getDataFromNet();
    }

    private void getZhuShe() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext()) + "");
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.amountFlg, String.valueOf(9));
        mapbody.put(Constants.insureFlg, String.valueOf(9));
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUSHESHOW, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AVOSCloudUtils.saveErrorMessage(e);
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
                                shenames.clear();
                                sheids.clear();
                                sheList = bean.getData();
                                shenames.add("理赔用");
                                sheids.add("-1");
                                if (null != sheList && sheList.size() > 0) {
                                    shename = sheList.get(0).getName();
                                    sheid = sheids.get(0);
                                    for (int i = 0; i < sheList.size(); i++) {
                                        shenames.add(sheList.get(i).getName());
                                        sheids.add(sheList.get(i).getSheId() + "");
                                    }

                                } else {
                                    toastUtils.showLong(MyApplication.getAppContext(), "猪舍为空");
                                }
                                initSpinner();
                            } else {
                                toastUtils.showLong(MyApplication.getAppContext(), bean.getMsg());
                            }
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(USBMenageActivity.this, "查询失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    private void initSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, shenames);
        she_she.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        she_she.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shename = shenames.get(position);
                sheid = sheids.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getDataFromNet() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext()) + "");
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        map.put(Constants.deptIdnew, PreferencesUtils.getStringValue(Constants.deptId, MyApplication.getAppContext()));
        map.put(Constants.id, PreferencesUtils.getStringValue(Constants.id, MyApplication.getAppContext(), "0"));

        OkHttp3Util.doPost(Constants.SHESHOW, null, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AVOSCloudUtils.saveErrorMessage(e);
                Log.i(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(string);
                            int status = jsonObject.getInt("status");
                            String msg = jsonObject.getString("msg");
                            if (status != 1) {
                                showDialogError(msg);
                            } else {
                                sheXTBeans.clear();
                                JSONArray datas = jsonObject.getJSONArray("data");
                                for (int i = 0; i < datas.length(); i++) {
                                    JSONObject jsonObject1 = datas.getJSONObject(i);
                                    String cameraName = jsonObject1.getString("cameraName");
                                    String cameraNo = jsonObject1.getString("cameraNo");
                                    String repair = jsonObject1.getString("repair");
                                    int cameraId = jsonObject1.getInt("cameraId");
                                    String sheId = jsonObject1.getString("sheId");
                                    String sheName = jsonObject1.getString("sheName");
                                    SheXTBean sheXTBean = new SheXTBean(cameraName, cameraNo, repair, cameraId, sheId, sheName);
                                    sheXTBeans.add(sheXTBean);
                                }
                                Log.i("====", sheXTBeans.size() + "");
                                USBManageAdapter usbManageAdapter = new USBManageAdapter(USBMenageActivity.this, sheXTBeans);
                                usbmanage_list.setAdapter(usbManageAdapter);
                                usbManageAdapter.setListner(new MyInterface() {
                                    @Override
                                    public void isOut(Boolean aBoolean) {
                                        if (aBoolean) {
                                            getDataFromNet();
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            AVOSCloudUtils.saveErrorMessage(e);
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }

    @OnClick({R.id.she_right_image, R.id.iv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.she_right_image:
                if (null == she_xu.getText().toString() || "".equals(she_xu.getText().toString())) {
                    toastUtils.showLong(MyApplication.getAppContext(), "未填写序列号");
                } else if (null == she_name.getText().toString() || "".equals(she_name.getText().toString())) {
                    toastUtils.showLong(MyApplication.getAppContext(), "未填写名字");
                } else {
                    addSheXiangTou();
                }
                break;
            case R.id.iv_cancel:
                finish();
            default:
                break;
        }

    }

    private void addSheXiangTou() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext()) + "");
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        map.put(Constants.deptIdnew, PreferencesUtils.getStringValue(Constants.deptId, MyApplication.getAppContext()));
        map.put(Constants.id, PreferencesUtils.getStringValue(Constants.id, MyApplication.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.cameraNo, she_xu.getText().toString());
        mapbody.put(Constants.cameraName, she_name.getText().toString());
        mapbody.put(Constants.sheId, sheid);
        mProgressDialog.show();
        OkHttp3Util.doPost(Constants.SXADD, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AVOSCloudUtils.saveErrorMessage(e);
                mProgressDialog.dismiss();
                Log.i(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                mProgressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                showDialogError(msg);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                toastUtils.showLong(MyApplication.getAppContext(), msg);
                                getDataFromNet();
                            }
                        });

                    }

                } catch (Exception e) {
                    AVOSCloudUtils.saveErrorMessage(e);
                    e.printStackTrace();
                }
            }
        });

    }
}
