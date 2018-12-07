package com.xiangchuang.risks.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.JuanSetting_item_Adapter;
import com.xiangchuang.risks.model.adapter.ZhuJuanXinXI_item_Adapter;
import com.xiangchuang.risks.model.bean.JuanSTBean;
import com.xiangchuang.risks.model.bean.SheXTBean;
import com.xiangchuang.risks.model.bean.ZhuJuanBean;
import com.xiangchuang.risks.model.myinterface.MyInterface;
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

public class JuanSettingActivity extends BaseActivity {
    public static String TAG = "JuanSettingActivity";
    @BindView(R.id.setting_listview)
    ListView setting_listview;
    @BindView(R.id.xu)
    TextView mxu;
    @BindView(R.id.touname)
    TextView mtouname;
    @BindView(R.id.mshename)
    TextView mshename;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    List<JuanSTBean> juanSTBeans = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_juan_setting;
    }

    @Override
    protected void initData() {
        String xu = PreferencesUtils.getStringValue(Constants.xu, MyApplication.getAppContext(), "0");
        String touname = PreferencesUtils.getStringValue(Constants.touname, MyApplication.getAppContext(), "0");
        String shename = PreferencesUtils.getStringValue(Constants.shename, MyApplication.getAppContext(), "0");
        String sheId = PreferencesUtils.getStringValue(Constants.sheId, MyApplication.getAppContext(), "0");
        String cameraId = PreferencesUtils.getStringValue(Constants.cameraId, MyApplication.getAppContext(), "0");
        mxu.setText(xu);
        mtouname.setText(touname);
        mshename.setText(shename);
        getDataFromNet(sheId, cameraId);
    }

    private void getDataFromNet(String sheId, String cameraId) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext()) + "");
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        map.put(Constants.deptIdnew, PreferencesUtils.getStringValue(Constants.deptId, MyApplication.getAppContext()));
        map.put(Constants.id, PreferencesUtils.getStringValue(Constants.id, MyApplication.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.cameraId, cameraId);
        mapbody.put(Constants.sheId, sheId);
        OkHttp3Util.doPost(Constants.CAMERALIST, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                juanSTBeans.clear();
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
                                try {
                                    JSONArray datas = jsonObject.getJSONArray("data");
                                    for (int i = 0; i < datas.length(); i++) {
                                        JSONObject jsonObject1 = datas.getJSONObject(i);
                                        int operation = jsonObject1.getInt("operation");
                                        String cameraName = jsonObject1.getString("cameraName");
                                        String name = jsonObject1.getString("name");
                                        int juanId = jsonObject1.getInt("juanId");
                                        JuanSTBean juanSTBean = new JuanSTBean(operation, cameraName, name, juanId);
                                        juanSTBeans.add(juanSTBean);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                JuanSetting_item_Adapter juanSetting_item_adapter = new JuanSetting_item_Adapter(JuanSettingActivity.this, juanSTBeans);
                                setting_listview.setAdapter(juanSetting_item_adapter);
                                juanSetting_item_adapter.setListner(new MyInterface() {
                                    @Override
                                    public void isOut(Boolean aBoolean) {
                                        if (aBoolean) {
                                            Log.i("=sheId==", sheId);
                                            Log.i("=cameraId==", cameraId);
                                            getDataFromNet(sheId, cameraId);
                                        }
                                    }
                                });
                            }
                        });

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    @OnClick({R.id.iv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cancel:
                finish();
                break;
            default:
                break;
        }

    }

}
