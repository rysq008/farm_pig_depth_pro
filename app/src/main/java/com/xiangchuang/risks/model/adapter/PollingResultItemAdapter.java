package com.xiangchuang.risks.model.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.myinterface.MyInterface;
import com.xiangchuang.risks.model.bean.PollingListBean;
import com.xiangchuang.risks.model.bean.UpdateBean;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class PollingResultItemAdapter extends BaseAdapter {


    private final List<PollingListBean.DataBean.List0Bean.JuanListBean> juanBeans;
    private final String sname;
    private final String fleg;
    private Context context;
    private String juanId;
    private AlertDialog dialogcreate;
    private MyInterface myInterface;


    public PollingResultItemAdapter(Context context, List<PollingListBean.DataBean.List0Bean.JuanListBean> juanBeans, String name, String fleg) {
        this.context = context;
        this.juanBeans = juanBeans;
        this.sname = name;
        this.fleg = fleg;
    }


    @Override
    public int getCount() {
        return juanBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return juanBeans.get(position);
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
            convertView = View.inflate(context, R.layout.she_item_layout, null);
            viewHolder.juanname = (TextView) convertView.findViewById(R.id.juanname);
            viewHolder.polljuan_count = (TextView) convertView.findViewById(R.id.polljuan_count);
            viewHolder.chulan = (TextView) convertView.findViewById(R.id.chulan);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.juanname.setText(juanBeans.get(position).getName());
        viewHolder.polljuan_count.setText(juanBeans.get(position).getCount() + "");
        if (juanBeans.get(position).getCount() > 0) {
            if ("0".equals(fleg)) {
                viewHolder.chulan.setVisibility(View.VISIBLE);
            } else {
                viewHolder.chulan.setVisibility(View.GONE);
            }
        } else {
            viewHolder.chulan.setVisibility(View.GONE);
        }
        viewHolder.chulan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                juanId = juanBeans.get(position).getJuanId() + "";
                showDialog(juanBeans.get(position).getName());
            }
        });
        return convertView;
    }

    private void showDialog(String juanname) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View inflate = View.inflate(context, R.layout.chulan_dialog_layout, null);
        TextView chulandialogtitle = inflate.findViewById(R.id.chulan_dialog_title);
        TextView chulandialogcancel = inflate.findViewById(R.id.chulan_dialog_cancel);
        TextView chulandialogsure = inflate.findViewById(R.id.chulan_dialog_sure);
        dialog.setView(inflate);
        chulandialogtitle.setText(sname + "-" + juanname + "的猪只头数即将变为0头，确认出栏?");
        dialogcreate = dialog.create();
        dialogcreate.setCanceledOnTouchOutside(false);
        dialogcreate.show();
        chulandialogsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outFormNet();

            }
        });
        chulandialogcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogcreate.dismiss();
            }
        });


    }

    private void outFormNet() {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(PreferencesUtils.getIntValue(Constants.en_user_id, MyApplication.getAppContext())));
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.juanId, juanId + "");
        Log.i("outAdapter:juanId", juanId + "");
        BaseActivity activity = (BaseActivity) context;
        activity.mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUJUANOUT, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activity.mProgressDialog.dismiss();
                dialogcreate.dismiss();
                Log.i("outAdapter", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("outAdapter", string);
                activity.mProgressDialog.dismiss();
                final UpdateBean bean = GsonUtils.getBean(string, UpdateBean.class);
                if (null != bean) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, bean.getMsg(), Toast.LENGTH_LONG).show();
                            myInterface.isOut(true);
                            dialogcreate.dismiss();
                        }
                    });

                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyApplication.getAppContext(), "添加失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    public void setListner(MyInterface myInterface) {
        this.myInterface = myInterface;
    }

    class ViewHolder {
        TextView juanname, polljuan_count, chulan;
    }
}
