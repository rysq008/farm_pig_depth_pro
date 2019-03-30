package com.xiangchuang.risks.model.adapter;

import android.content.Context;
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
import com.xiangchuang.risks.model.bean.PinZhongBean;
import com.xiangchuang.risks.model.bean.UpdateBean;
import com.xiangchuang.risks.model.bean.ZhuJuanBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.GsonUtils;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ZhuJuanXinXI_item_Adapter extends BaseAdapter {
    private List<PinZhongBean.DataBean> pinzhongs;
    private List<ZhuJuanBean.DataBean> juanList;
    private Context context;

    private List<String> pinzhonglist;
    private int manimalSubType;
    private int animalSubType;

    public ZhuJuanXinXI_item_Adapter(Context context, List<ZhuJuanBean.DataBean> juanList, List<PinZhongBean.DataBean> pinzhongs) {
        this.context = context;
        this.juanList = juanList;
        this.pinzhongs = pinzhongs;
    }


    @Override
    public int getCount() {
        return juanList.size();
    }

    @Override
    public Object getItem(int position) {
        return juanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        pinzhonglist = new ArrayList<>();
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(context, R.layout.zhujuanxinxi_item_layout, null);
            viewHolder.zhujuanpinzhongspinner = (Spinner) convertView.findViewById(R.id.zhujuan_pinzhong_spinner);
            viewHolder.zhujuanname = (EditText) convertView.findViewById(R.id.zhujuan_name);
            viewHolder.zhujuanrightimage = (TextView) convertView.findViewById(R.id.zhujuan_right_image);
            viewHolder.zhujuandeleteimage = (TextView) convertView.findViewById(R.id.zhujuan_delete_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        for (int i = 0; i < pinzhongs.size(); i++) {
            pinzhonglist.add(pinzhongs.get(i).getAnimalSubTypeName());
        }
        String animalSubTypeName = juanList.get(position).getAnimalSubTypeName();
        initSpinner(viewHolder.zhujuanpinzhongspinner, pinzhonglist, animalSubTypeName, juanList.get(position).getJuanId(), viewHolder.zhujuanname.getText().toString(), position, viewHolder.zhujuanname);
        viewHolder.zhujuanname.setText(juanList.get(position).getName());
        if (juanList.get(position).getCount() == 0) {
            viewHolder.zhujuanrightimage.setVisibility(View.VISIBLE);
            viewHolder.zhujuandeleteimage.setVisibility(View.GONE);
        } else {
            viewHolder.zhujuanrightimage.setVisibility(View.GONE);
            viewHolder.zhujuandeleteimage.setVisibility(View.VISIBLE);
        }
        viewHolder.zhujuanname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (juanList.size() > 0) {
                        Log.i("======", juanList.get(position).getName());
                        if (viewHolder.zhujuanname.getText().toString().isEmpty()) {
                            Toast.makeText(context, "猪圈信息为空", Toast.LENGTH_LONG).show();
                        } else if (juanList.get(position).getName().compareToIgnoreCase(viewHolder.zhujuanname.getText().toString()) != 0) {
                            String selpinzhong = viewHolder.zhujuanpinzhongspinner.getSelectedItem().toString();
                            for (int i = 0; i < pinzhongs.size(); i++) {
                                if (pinzhongs.get(i).getAnimalSubTypeName().equals(selpinzhong)) {
                                    manimalSubType = pinzhongs.get(i).getAnimalSubType();
                                }
                            }
                            //修改猪圈
                            updateZhuJuan(juanList.get(position).getJuanId(), viewHolder.zhujuanname.getText().toString(), position, manimalSubType + "");
                        }
                    }
                }
            }
        });
        //删除猪圈
        viewHolder.zhujuanrightimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteZhuJuan(position, juanList.get(position).getJuanId() + "");
            }
        });
        return convertView;
    }

    private void deleteZhuJuan(int position, String juanid) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, "" + PreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext()));
        map.put(Constants.en_id, "" + PreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.juanId, juanid);
        Log.i("juandeleteid", juanid);
        BaseActivity activity = (BaseActivity) context;
        activity.mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUJUANDELETE, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activity.mProgressDialog.dismiss();
                Log.i("---", e.toString());
                AVOSCloudUtils.saveErrorMessage(e,ZhuJuanXinXI_item_Adapter.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("juandelete", string);
                final UpdateBean bean = GsonUtils.getBean(string, UpdateBean.class);
                if (null != bean) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                activity.toastUtils.showLong(context, bean.getMsg());
                                juanList.remove(position);
                                notifyDataSetChanged();
                            } else {
                                activity.toastUtils.showLong(context, bean.getMsg());
                            }
                        }
                    });

                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.toastUtils.showLong(context, "删除失败");
                        }
                    });
                }

            }
        });
    }

    private void updateZhuJuan(int juanId, String mstring, int position, String animalSubType) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, "" + PreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext()));
        map.put(Constants.en_id, "" + PreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0"));
        Map mapbody = new HashMap();
        mapbody.put(Constants.juanId, String.valueOf(juanId));
        mapbody.put(Constants.name, mstring.trim());
        mapbody.put(Constants.animalSubType, String.valueOf(animalSubType));
        Log.i("update juanId=", juanId + "");
        Log.i("update name=", mstring + "");
        BaseActivity activity = (BaseActivity) context;
        activity.mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUJUANUPDATE, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activity.mProgressDialog.dismiss();
                Log.i("ZhuJuanXinXI_item", e.toString());
                AVOSCloudUtils.saveErrorMessage(e,ZhuJuanXinXI_item_Adapter.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("ZhuJuanXinXI_item", string);

                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status != 1) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.mProgressDialog.dismiss();
                                activity.showDialogError(msg);
                            }
                        });
                    } else {
                        final UpdateBean bean = GsonUtils.getBean(string, UpdateBean.class);
                        if (null != bean) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.mProgressDialog.dismiss();
                                    if (bean.getStatus() == 1) {
                                        ZhuJuanBean.DataBean dataBean = juanList.get(position);
                                        dataBean.setName(mstring);
                                        Log.i("====updateTypeName==", animalSubType + "");
                                        dataBean.setAnimalSubType(Integer.valueOf(animalSubType));
                                        //  Toast.makeText(context, bean.getMsg(), Toast.LENGTH_LONG).show();
                                        activity.toastUtils.showLong(context, bean.getMsg());
                                    }
                                }
                            });

                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.toastUtils.showLong(context, "更新失败");
                                    // Toast.makeText(context, "更新失败", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e,ZhuJuanXinXI_item_Adapter.class.getSimpleName());
                }


            }
        });

    }

    private void initSpinner(Spinner mzhujuanpinzhongspinner, List<String> mpinzhonglist, String animalSubTypeName, int juanId, String subTypeName, int wei, EditText zhujuanname) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, mpinzhonglist);
        mzhujuanpinzhongspinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < pinzhongs.size(); i++) {
            if (pinzhongs.get(i).getAnimalSubTypeName().equals(animalSubTypeName)) {
                mzhujuanpinzhongspinner.setSelection(i);
            }
        }
        mzhujuanpinzhongspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!mzhujuanpinzhongspinner.getSelectedItem().toString().equals(juanList.get(wei).getAnimalSubTypeName())) {
                    Log.i("position", position + "");
                    for (int i = 0; i < pinzhongs.size(); i++) {
                        if (pinzhongs.get(i).getAnimalSubTypeName().equals(mzhujuanpinzhongspinner.getSelectedItem().toString())) {
                            animalSubType = pinzhongs.get(i).getAnimalSubType();

                        }
                    }
                    animalSubType = pinzhongs.get(position).getAnimalSubType();
                    updateZhuJuan(juanId, zhujuanname.getText().toString(), wei, animalSubType + "");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    class ViewHolder {
        Spinner zhujuanpinzhongspinner;
        EditText zhujuanname;
        TextView zhujuanrightimage, zhujuandeleteimage;
    }
}
