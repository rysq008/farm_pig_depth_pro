package com.farm.innovation.login.model;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.BaoDanNetBean;
import com.farm.innovation.bean.BaodanBean;
import com.farm.innovation.biz.Insured.LocaleInsuredSaveListener;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.data.source.InsuredNos;
import com.farm.innovation.location.AlertDialogManager;
import com.farm.innovation.login.DatabaseHelper;
import com.farm.innovation.login.ToubaoDetailActivity;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.login.view.ISExist;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.ConstUtils;
import com.farm.innovation.utils.HttpRespObject;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.PreferencesUtils;
import com.innovation.pig.insurance.R;

import org.tensorflow.demo.FarmDetectorActivity;
import org.tensorflow.demo.FarmGlobal;

import java.util.List;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.farm.innovation.base.FarmAppConfig.getStringTouboaExtra;
import static com.farm.innovation.login.model.MyUIUTILS.getString;
import static com.farm.innovation.login.view.HomeActivity.isOPen;
import static com.farm.innovation.utils.MyTextUtil.replaceBlank;
import static org.tensorflow.demo.FarmCameraConnectionFragment.collectNumberHandler;

public class ToubaoNetAdapter extends RecyclerView.Adapter<ToubaoNetAdapter.ViewHolder> {
    private List<BaoDanNetBean> baoDanNetBeans;
    private DatabaseHelper databaseHelper;
    //private ArrayList<QueryBaodanBean> newsBeanArrayList;
    private Context mContext;
    private RecyclerViewOnItemClickListener mOnItemClickListener;
    private RecyclerViewOnItemClickListener mOnItemClickListenerZiliao;
    //保单保存到本地的回调
    private LocaleInsuredSaveListener mLocaleInsuredSaveListener;

    private String result;
    private ToubaoTask mToubaoTask;
    private BaodanBean insurresp;
    private String errStr;
    Handler mHandler;
    private View view;
    private ISExist isExist;
    private String mtoubao_date;
    private String type;

    public ToubaoNetAdapter(List<BaoDanNetBean> baoDanNetBeans, Context mContext, DatabaseHelper databaseHelper) {
        // this.newsBeanArrayList = newsBeanArrayList;
        this.baoDanNetBeans = baoDanNetBeans;
        this.mContext = mContext;
        this.databaseHelper = databaseHelper;
    }

    //创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.farm_toubao_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        //保单名称
        viewHolder.baodan_name.setText(baoDanNetBeans.get(position).getBaodanName());
        //投保人
        viewHolder.toubao_Pname.setText(baoDanNetBeans.get(position).getName());
        //验标单名字
        viewHolder.yanbiao_name.setText(replaceBlank(baoDanNetBeans.get(position).getYanBiaoName()));
        //头数
        viewHolder.tvTouShu.setText("已验标"+baoDanNetBeans.get(position).getCollectAmount()+"头");

        //日期
        if (!baoDanNetBeans.get(position).getCreatetime().equals("")) {
            String mtoubao_date = baoDanNetBeans.get(position).getCreatetime();
            viewHolder.toubao_date.setText((null != mtoubao_date && mtoubao_date.length() > 0) ? mtoubao_date.substring(0, 10) : mtoubao_date);
        }
        //证件号
        if (baoDanNetBeans.get(position).getCardNo() != null) {
            viewHolder.toubao_idcard.setText(baoDanNetBeans.get(position).getCardNo());
        }
        //查询数据库，如果存在则隐藏添加到离线的图标
        if (baoDanNetBeans.size() > 0 && baoDanNetBeans.get(position).getBaodanNo() != null) {
            List<LocalModelNongxian> localModels = databaseHelper.queryLocalDataFromBaodanNo(baoDanNetBeans.get(position).getBaodanNo());
            if (localModels != null && localModels.size() > 0) {
                viewHolder.selectedTagrel.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.selectedTagrel.setVisibility(View.VISIBLE);
            }
        }
        viewHolder.toubao_xiangqing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("baodanNumber", baoDanNetBeans.get(position).getBaodanNo());
                intent.putExtra("baodanId", String.valueOf(baoDanNetBeans.get(position).getBaodan_id()));
                getStringTouboaExtra = baoDanNetBeans.get(position).getBaodanNo();
                intent.setClass(mContext, ToubaoDetailActivity.class);
                mContext.startActivity(intent);


            }
        });

        viewHolder.toubao_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOPen(mContext)) {
                    TreeMap query = new TreeMap<String, String>();
                    query.put("baodanNo", baoDanNetBeans.get(position).getBaodanNo());
                    getStringTouboaExtra = baoDanNetBeans.get(position).getBaodanNo();
                    if(!FarmAppConfig.isNetConnected){
                        Toast.makeText(mContext, "当前网络异常，请检查网络连接状态后重试。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mToubaoTask = new ToubaoTask(HttpUtils.INSUR_QUERY_URL, query);
                    mToubaoTask.execute((Void) null);
                } else {
//                    openGPS(mContext);
                    AlertDialogManager.showMessageDialog(mContext, "提示", getString(R.string.locationwarning), new AlertDialogManager.DialogInterface() {
                        @Override
                        public void onPositive() {
                            openGPS1(mContext);
                        }

                        @Override
                        public void onNegative() {

                        }
                    });

                }


            }
        });
        viewHolder.selectedTagrel.setOnClickListener((View v) -> {
            //  Log.i("====", newsBeanArrayList.size() + "");
            if (baoDanNetBeans.size() > 0) {
                //点击离线缓存添加到数据库
                int animalType = Integer.valueOf(PreferencesUtils.getStringValue("animalType", mContext));
                if (animalType == ConstUtils.ANIMAL_TYPE_CATTLE) {
                    type = "牛";
                } else if (animalType == ConstUtils.ANIMAL_TYPE_DONKEY) {
                    type = "驴";
                } else if (animalType == ConstUtils.ANIMAL_TYPE_PIG) {
                    type = "猪";
                }
                Log.i("==animalType==", animalType + "");
                if (!baoDanNetBeans.get(position).getCreatetime().equals("")) {
                    mtoubao_date = baoDanNetBeans.get(position).getCreatetime();
                }

//                LocalModel localModel = new LocalModel(newsBeanArrayList.get(position).baodanNo,
//                        newsBeanArrayList.get(position).name, newsBeanArrayList.get(position).cardNo, mtoubao_date,type);
//                databaseHelper.addLocalData(localModel);

                LocalModelNongxian localModelNongxian = new LocalModelNongxian(
                        baoDanNetBeans.get(position).getBaodanNo(),
                        baoDanNetBeans.get(position).getName(),
                        baoDanNetBeans.get(position).getCardNo(),
                        mtoubao_date, animalType + "", baoDanNetBeans.get(position).getYanBiaoName(), baoDanNetBeans.get(position).getBaodanName());
                databaseHelper.addLocalNongxianData(localModelNongxian);
                isExist.isexist(true);
                viewHolder.selectedTagrel.setVisibility(View.INVISIBLE);
            }
        });


    }

    InsuredNos insuredNos = new InsuredNos();

    //获取数据的数量
    @Override
    public int getItemCount() {
        // TODO: 2018/8/9 By:LuoLu  newsBeanArrayList.size()
        return baoDanNetBeans.size();
//        return 2;
    }

    public void setListner(ISExist isExist) {
        this.isExist = isExist;
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView baodan_name;
        public TextView yanbiao_name;
        public TextView isnot_lipei;
        public TextView toubao_date;
        public TextView toubao_detail;
        public RelativeLayout toubao_continue;
        public TextView toubao_idcard, toubao_Pname;
        public ImageView selectedTag;
        public RelativeLayout selectedTagrel, toubao_xiangqing;
        public TextView tvTouShu;

        public ViewHolder(View view) {
            super(view);
            baodan_name = (TextView) view.findViewById(R.id.baodan_name);
            yanbiao_name = (TextView) view.findViewById(R.id.yanbiao_name);
            toubao_date = (TextView) view.findViewById(R.id.toubao_date);
            toubao_Pname = (TextView) view.findViewById(R.id.toubao_Pname);
            toubao_idcard = (TextView) view.findViewById(R.id.toubao_idcard);
            isnot_lipei = (TextView) view.findViewById(R.id.isnot_lipei);
            toubao_detail = (TextView) view.findViewById(R.id.toubao_upload);
            toubao_continue = (RelativeLayout) view.findViewById(R.id.toubao_continue);
            toubao_xiangqing = (RelativeLayout) view.findViewById(R.id.toubao_xiangqing);
            selectedTag = view.findViewById(R.id.selectedTag);
            selectedTagrel = view.findViewById(R.id.selectedTagrel);
            tvTouShu = (TextView) view.findViewById(R.id.tv_toushu);

        }
    }


    /**
     * 设置点击事件
     */
    public void setRecyclerViewOnItemClickListener(RecyclerViewOnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    /**
     * 设置点击事件
     */
    public void setRecyclerViewOnItemClickListenerZiliao(RecyclerViewOnItemClickListener onItemClickListener) {
        this.mOnItemClickListenerZiliao = onItemClickListener;
    }

    /**
     * 点击事件接口
     */
    public interface RecyclerViewOnItemClickListener {
        void onItemClickListener(View view, int position);
    }

    public class ToubaoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final TreeMap<String, String> mQueryMap;

        ToubaoTask(String url, TreeMap map) {
            mUrl = url;
            mQueryMap = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //  attempt authentication against a network service.
            try {
                FormBody.Builder builder = new FormBody.Builder();
                // Add Params to Builder
                for (TreeMap.Entry<String, String> entry : mQueryMap.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }
                // Create RequestBody
                RequestBody formBody = builder.build();

                String response = HttpUtils.post(mUrl, formBody);

                if (HttpUtils.INSUR_QUERY_URL.equalsIgnoreCase(mUrl)) {
                    insurresp = (BaodanBean) HttpUtils.processResp_insurInfo(response, mUrl);
                    if (insurresp == null) {
//                        errStr = getString(R.string.error_newwork);
                        errStr = "请求错误！";
                        return false;
                    }
                    if (insurresp.status != HttpRespObject.STATUS_OK) {
                        errStr = insurresp.msg;
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                errStr = "服务器错误！";
                AVOSCloudUtils.saveErrorMessage(e, ToubaoNetAdapter.class.getSimpleName());
                return false;
            }
            //  register the new account here.

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mToubaoTask = null;
            if (success & HttpUtils.INSUR_QUERY_URL.equalsIgnoreCase(mUrl)) {
                if (!insurresp.ibaodanNoReal.equals("")) {
                    Toast.makeText(mContext, "保单已审核，不能继续录入", Toast.LENGTH_SHORT).show();
                } else {
                    PreferencesUtils.saveBooleanValue("isli", false, mContext);
                    FarmGlobal.model = Model.BUILD.value();
                    Intent intent = new Intent();
                    intent.putExtra("ToubaoTempNumber", insurresp.ibaodanNo);
                    intent.setClass(mContext, FarmDetectorActivity.class);
                    mContext.startActivity(intent);
                    collectNumberHandler.sendEmptyMessage(2);
                }

            } else if (!success) {
                //  显示失败
//                tv_info.setText(errStr);
            }
        }

        @Override
        protected void onCancelled() {
            mToubaoTask = null;
        }
    }

    private void openGPS1(Context mContext) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        HomeActivity activity = (HomeActivity) mContext;
        activity.startActivityForResult(intent, 1315);
    }

}
