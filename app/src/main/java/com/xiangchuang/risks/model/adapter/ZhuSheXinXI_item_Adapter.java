package com.xiangchuang.risks.model.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.UpdateBean;
import com.xiangchuang.risks.model.bean.ZhuSheBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.GsonUtils;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ZhuSheXinXI_item_Adapter extends BaseAdapter {
    private List<ZhuSheBean.DataBean> sheList;
    private Context context;
    private String trim;

    public ZhuSheXinXI_item_Adapter(Context context, List<ZhuSheBean.DataBean> sheList) {
        this.context = context;
        this.sheList = sheList;
    }

    @Override
    public int getCount() {
        return sheList.size();
    }

    @Override
    public Object getItem(int position) {
        return sheList.get(position);
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
            convertView = View.inflate(context, R.layout.zhushexinxi_item, null);
            viewHolder.zhusherightimage = (TextView) convertView.findViewById(R.id.zhushe_right_image);
            viewHolder.zhusheedittext = (EditText) convertView.findViewById(R.id.zhushe_edit_text);
            viewHolder.zhushedeleteimage = (TextView) convertView.findViewById(R.id.zhushe_delete_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.zhusheedittext.setText(sheList.get(position).getName());
        //>0可删  否则不可删
        if (sheList.get(position).getCount() == 0) {
            viewHolder.zhusherightimage.setVisibility(View.VISIBLE);
            viewHolder.zhushedeleteimage.setVisibility(View.GONE);
        } else {
            viewHolder.zhusherightimage.setVisibility(View.GONE);
            viewHolder.zhushedeleteimage.setVisibility(View.VISIBLE);
        }
        viewHolder.zhusheedittext.setText(sheList.get(position).getName());
      /*  if (sheList.get(position).getCount() > 0) {
            viewHolder.zhusherightimage.setVisibility(View.VISIBLE);
            viewHolder.zhushedeleteimage.setVisibility(View.GONE);
        } else {
            viewHolder.zhusherightimage.setVisibility(View.GONE);
            viewHolder.zhushedeleteimage.setVisibility(View.VISIBLE);
        }*/
        viewHolder.zhusheedittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (viewHolder.zhusheedittext.getText().toString().isEmpty()) {
                        Toast.makeText(context, "猪舍信息为空", Toast.LENGTH_LONG).show();
                    } else if (sheList.get(position).getName().compareToIgnoreCase(viewHolder.zhusheedittext.getText().toString()) != 0) {
                        //修改猪舍
                        updateZhuShe(sheList.get(position).getSheId(), viewHolder.zhusheedittext.getText().toString().trim(), position);
                    }
                }
            }
        });
        viewHolder.zhusherightimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除猪舍
                deleteZhuShe(position, sheList.get(position).getSheId() + "");
            }
        });
        return convertView;
    }

    private void deleteZhuShe(int position, String sheid) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, PreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext()) + "");
        map.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0") + "");
        Map mapbody = new HashMap();
        mapbody.put(Constants.sheId, sheid);
        Log.i("shedeletesheid:", sheid);
        BaseActivity activity = (BaseActivity) context;
        activity.mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUSHEDELETE, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activity.mProgressDialog.dismiss();
                Log.i("--", e.toString());
                AVOSCloudUtils.saveErrorMessage(e,ZhuSheXinXI_item_Adapter.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("zhushedelete", string);
                final UpdateBean bean = GsonUtils.getBean(string, UpdateBean.class);
                if (null != bean) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                activity.toastUtils.showLong(context, bean.getMsg());
                                sheList.remove(position);
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

    private void updateZhuShe(int sheid, String sname, int position) {
        Map map = new HashMap();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.en_user_id, String.valueOf(PreferencesUtils.getIntValue(Constants.en_user_id, AppConfig.getAppContext())));
        map.put(Constants.en_id, String.valueOf(PreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0")));
        Map mapbody = new HashMap();
        mapbody.put(Constants.sheId, String.valueOf(sheid));
        mapbody.put(Constants.name, sname);
        Log.i("update sheId=", sheid + "");
        Log.i("update name=", sname + "");
        BaseActivity activity = (BaseActivity) context;
        activity.mProgressDialog.show();
        OkHttp3Util.doPost(Constants.ZHUSHEUPDATE, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activity.mProgressDialog.dismiss();
                Log.i("--", e.toString());
                AVOSCloudUtils.saveErrorMessage(e,ZhuSheXinXI_item_Adapter.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                final UpdateBean bean = GsonUtils.getBean(string, UpdateBean.class);
                if (null != bean) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sheList.get(position).setName(sname);
                            activity.mProgressDialog.dismiss();
                            if (bean.getStatus() == 1) {
                                activity.toastUtils.showLong(context, bean.getMsg());
                            } else {
                                activity.toastUtils.showLong(context, bean.getMsg());
                            }
                        }
                    });

                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.toastUtils.showLong(context, "修改失败");
                        }
                    });
                }

            }
        });
    }

    class ViewHolder {
        TextView zhusherightimage, zhushedeleteimage;
        EditText zhusheedittext;
    }
}
