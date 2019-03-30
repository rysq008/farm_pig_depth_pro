package com.xiangchuang.risks.model.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.JuanSTBean;
import com.xiangchuang.risks.model.myinterface.MyInterface;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class JuanSetting_item_Adapter extends BaseAdapter {

    private List<JuanSTBean> juanSTBeans;
    private Context context;
    private BaseActivity activity;
    private MyInterface myInterface;

    public JuanSetting_item_Adapter(Context context, List<JuanSTBean> juanSTBeans) {
        this.context = context;
        this.juanSTBeans = juanSTBeans;
    }


    @Override
    public int getCount() {
        return juanSTBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return juanSTBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(context, R.layout.juan_setting_item_layout, null);
            viewHolder.bang = convertView.findViewById(R.id.bang);
            viewHolder.set_juan = (TextView) convertView.findViewById(R.id.set_juan);
            viewHolder.set_she = (TextView) convertView.findViewById(R.id.set_she);
            viewHolder.jiebang = (TextView) convertView.findViewById(R.id.jiebang);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String cameraName = juanSTBeans.get(position).cameraName;
        String name = juanSTBeans.get(position).name;
        int operation = juanSTBeans.get(position).operation;

        viewHolder.set_juan.setText(name);
        viewHolder.set_she.setText(cameraName + "");
        activity = (BaseActivity) context;
        if (operation == 0) {
            viewHolder.bang.setVisibility(View.GONE);
            viewHolder.jiebang.setVisibility(View.GONE);
        } else if (operation == 1) {
            viewHolder.jiebang.setVisibility(View.VISIBLE);
            viewHolder.bang.setVisibility(View.GONE);
        } else if (operation == 2) {
            viewHolder.bang.setVisibility(View.VISIBLE);
            viewHolder.jiebang.setVisibility(View.GONE);
        }
        viewHolder.bang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BindingCamera(juanSTBeans.get(position).juanId);
            }
        });
        viewHolder.jiebang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BindingCamera(juanSTBeans.get(position).juanId);
            }
        });
        return convertView;
    }

    private void BindingCamera(int juanId) {
        String cameraId = PreferencesUtils.getStringValue(Constants.cameraId, AppConfig.getAppContext(), "0");
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext()) + "");
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0"));
        map.put(Constants.deptIdnew, PreferencesUtils.getStringValue(Constants.deptId, AppConfig.getAppContext()));
        map.put(Constants.id, PreferencesUtils.getStringValue(Constants.id, AppConfig.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.cameraId, cameraId);
        mapbody.put(Constants.juanId, juanId + "");
        OkHttp3Util.doPost(Constants.CAMERABINDING, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("BindingCamera", e.toString());
                AVOSCloudUtils.saveErrorMessage(e,JuanSetting_item_Adapter.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("BindingCamera", string);
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
                                myInterface.isOut(true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            AVOSCloudUtils.saveErrorMessage(e,JuanSetting_item_Adapter.class.getSimpleName());
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
        TextView bang, jiebang, set_juan, set_she;
    }
}
