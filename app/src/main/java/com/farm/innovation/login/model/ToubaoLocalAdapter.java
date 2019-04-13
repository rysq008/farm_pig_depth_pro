package com.farm.innovation.login.model;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.BaodanBean;
import com.farm.innovation.bean.MultiBaodanBean;
import com.farm.innovation.biz.Insured.LocaleInsuredSaveListener;
import com.farm.innovation.biz.Insured.ResponseBean;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.location.AlertDialogManager;
import com.farm.innovation.login.DatabaseHelper;
import com.farm.innovation.login.Utils;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.login.view.ISExist;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.HttpRespObject;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.OkHttp3Util;
import com.farm.innovation.utils.UploadUtils;
import com.innovation.pig.insurance.R;

import org.json.JSONObject;
import org.tensorflow.demo.FarmDetectorActivity;
import org.tensorflow.demo.FarmGlobal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.farm.innovation.base.FarmAppConfig.OFFLINE_PATH;
import static com.farm.innovation.login.model.MyUIUTILS.getString;
import static com.farm.innovation.login.view.HomeActivity.isOPen;
import static com.farm.innovation.utils.FileUtils.getMD5FromPath;
import static com.farm.innovation.utils.MyTextUtil.replaceBlank;
import static org.tensorflow.demo.FarmCameraConnectionFragment.collectNumberHandler;
import static org.tensorflow.demo.FarmGlobal.waitUploadCount;

public class ToubaoLocalAdapter extends RecyclerView.Adapter<ToubaoLocalAdapter.ViewHolder> {

    private DatabaseHelper databaseHelper;
    //    private List<LocalModel> localInsuredNos;
    private List<LocalModelNongxian> localModelNongxianList;
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
    String insuredNo;
    private String qurreyPid;
    private ResponseBean beanResponse;
    private int[] cnt;
    // zip文件List
    List<String> zipFilePaths;
    // 用户ID
    Integer userId;
    // 时间戳
    String batchId;
    // 处理文件对象Index
    int zipIndex = 0;
    private int status = -44;
    private int waitUploadCountTemp;
    private MultiBaodanBean offlineBuildResult;
    private ISExist isExist;

    private void callZipUpload() {
        if (zipFilePaths.size() > zipIndex) {
            String zipFilePath = zipFilePaths.get(zipIndex);
            String md5String = getMD5FromPath(zipFilePath);
            Log.i("====zipFilePath", zipFilePath);
            offlineZipUpLoad(zipFilePath, md5String, userId.toString(), batchId);
        } else {
            // 处理
            mHandlerLocal.sendEmptyMessage(9);
        }

    }


    public ToubaoLocalAdapter(List<LocalModelNongxian> localInsouredNos, Context mContext, Handler handler, DatabaseHelper databaseHelper) {
        this.localModelNongxianList = localInsouredNos;
        this.mContext = mContext;
        this.databaseHelper = databaseHelper;
        mHandler = handler;
//        waitUploadCount = waitUploadCountTemp;
        offlineBuildResult = new MultiBaodanBean();
    }

    //创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.farm_toubao_local_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    //全局定义
    long lastClickTime = 0L;
    final int FAST_CLICK_DELAY_TIME = 500;  // 快速点击间隔
    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        //保单名称
        viewHolder.baodan_name.setText(localModelNongxianList.get(position).getBaodanName());
        //投保人
        viewHolder.toubao_Pname.setText(localModelNongxianList.get(position).getName());
        //验标单名字
        viewHolder.yanbiao_name.setText(replaceBlank(localModelNongxianList.get(position).getYanBiaoName()));
        String uploadZipFilePathStr = Environment.getExternalStorageDirectory().getPath() +
                OFFLINE_PATH + localModelNongxianList.get(position).getBaodanNo() + "/";
        Log.i("uploadZipvideo===：", uploadZipFilePathStr);
        List<String> zipFilePathsTmp = FileUtils.getAllFile(uploadZipFilePathStr, ".zip");
        waitUploadCount = zipFilePathsTmp.size();
        Log.i("viewHolder待上传数量：", String.valueOf(waitUploadCount));
        viewHolder.cow_to_upload.setText(waitUploadCount + "头信息未上传");
        //日期
        if (!localModelNongxianList.get(position).getInsureDate().equals("")) {
            String mtoubao_date = localModelNongxianList.get(position).getInsureDate();
            viewHolder.toubao_date.setText((null != mtoubao_date && mtoubao_date.length() > 0) ? mtoubao_date.substring(0, 10) : mtoubao_date);
        }
        //证件号
        if (localModelNongxianList.get(position).getCardNo() != null) {
            viewHolder.toubao_idcard.setText(localModelNongxianList.get(position).getCardNo());
        }

        viewHolder.toubao_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME){
                    return;
                }
                lastClickTime = System.currentTimeMillis();
                LocalModelNongxian qb = localModelNongxianList.get(position);
                insuredNo = qb.getBaodanNo();
                int netWorkStates = com.farm.innovation.network_status.NetworkUtil.getNetWorkStates(mContext);
                Log.i("netWorkStates", netWorkStates + "");
                if (-1 == netWorkStates) {
                    Toast.makeText(mContext, "无网络", Toast.LENGTH_LONG).show();
                    return;
                }
                queryStatus();
            }
        });

        viewHolder.toubao_continue.setOnClickListener(v -> {
            if (isOPen(mContext)) {
                // TODO: 2018/8/8   进入离线流程
                LocalModelNongxian qb = localModelNongxianList.get(position);
                insuredNo = qb.getBaodanNo();
                FarmAppConfig.isOfflineMode = true;
                FarmAppConfig.offLineInsuredNo = insuredNo;
                FarmAppConfig.getStringTouboaExtra = insuredNo;
                Log.i("离线保单号:", FarmAppConfig.getStringTouboaExtra);
                showNormalDialog();
            } else {
                openGPS1(mContext);
            }

        });
        viewHolder.deletedInsuredButon.setOnClickListener(v -> {
            insuredNo = localModelNongxianList.get(position).getBaodanNo();
            FarmAppConfig.getStringTouboaExtra = insuredNo;
            showDialog();
            Log.i("Tbl保单号:", insuredNo);
        });


    }

    private void queryStatus() {
        Map<String, String> map = new HashMap<>();
        map.put(HttpUtils.AppKeyAuthorization, "hopen");
        map.put("Content-Type", "application/x-www-form-urlencoded");
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put("baodanNo", insuredNo);

        OkHttp3Util.doPost(HttpUtils.STATE_YANBIAO, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AVOSCloudUtils.saveErrorMessage(e, ToubaoLocalAdapter.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("querystaus", string);
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status == 1) {
                        String uploadZipFilePathStr = Environment.getExternalStorageDirectory().getPath() + OFFLINE_PATH + insuredNo + "/";
                        zipFilePaths = FileUtils.getAllFile(uploadZipFilePathStr, ".zip");
                        SharedPreferences pref = mContext.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
                        userId = pref.getInt("uid", 0);
                        SimpleDateFormat tmpSimpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault());
                        batchId = tmpSimpleDateFormat.format(new Date(System.currentTimeMillis()));
                        if (zipFilePaths.size() > 0) {
                            // 显示：正在上传
                            mHandlerLocal.sendEmptyMessage(8);
                            // 调用上传方法
                            zipIndex = 0;
                            callZipUpload();

                        } else {
                            // TODO: 2018/8/11 By:LuoLu  无需上传文件时反馈
                            mHandlerLocal.sendEmptyMessage(100);
                        }
                    } else {
                        // 该保单已审核无法上传离线投保数据时
                        mHandlerLocal.sendEmptyMessage(101);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e, ToubaoLocalAdapter.class.getSimpleName());
                }
            }
        });
    }

    private void openGPS1(Context mContext) {
        AlertDialogManager.showMessageDialog(mContext, "提示", getString(R.string.locationwarning), new AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                HomeActivity activity = (HomeActivity) mContext;
                activity.startActivityForResult(intent, 1315);
            }

            @Override
            public void onNegative() {

            }
        });

    }


    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setIcon(R.drawable.farm_cowface);
        builder.setMessage(waitUploadCount > 0 ? "还有" + waitUploadCount + "条数据未上传,确定取消收藏吗?" : "确定取消收藏吗?");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (databaseHelper.deleteLocalDataFromBaodanNo(insuredNo)) {
                    Toast.makeText(mContext, "删除成功", Toast.LENGTH_LONG).show();
                    isExist.isexist(true);
                } else {
                    isExist.isexist(false);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.setCancelable(false);
        builder.show();
    }

    //获取数据的数量
    @Override
    public int getItemCount() {
        return localModelNongxianList.size();
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView baodan_name, toubao_Pname;
        public TextView yanbiao_name;
        public TextView isnot_lipei;
        public TextView toubao_date;
        public TextView cow_to_upload;
        public RelativeLayout toubao_upload;
        public RelativeLayout toubao_continue;
        public TextView toubao_idcard;
        public RelativeLayout deletedInsuredButon;


        public ViewHolder(View view) {
            super(view);
            baodan_name = (TextView) view.findViewById(R.id.baodan_name);
            yanbiao_name = (TextView) view.findViewById(R.id.yanbiao_name);
            toubao_Pname = (TextView) view.findViewById(R.id.toubao_Pname);
            toubao_date = (TextView) view.findViewById(R.id.toubao_date);
            toubao_idcard = (TextView) view.findViewById(R.id.toubao_idcard);
            isnot_lipei = (TextView) view.findViewById(R.id.isnot_lipei);
            cow_to_upload = (TextView) view.findViewById(R.id.cow_to_upload);
            toubao_upload = (RelativeLayout) view.findViewById(R.id.toubao_upload);
            toubao_continue = (RelativeLayout) view.findViewById(R.id.toubao_local_continue);
            deletedInsuredButon = view.findViewById(R.id.deleteInsured);
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

    /*
     * 设置接口回调更新数据
     * */
    public void setListner(ISExist isExist) {
        Log.i("setListner:", "localsetListner");
        this.isExist = isExist;
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
                AVOSCloudUtils.saveErrorMessage(e, ToubaoLocalAdapter.class.getSimpleName());
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

    private void showNormalDialog() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        //normalDialog.setIcon(R.drawable.farm_icon_dialog);
        //      normalDialog.setTitle("我是一个普通Dialog")

        normalDialog.setIcon(R.drawable.farm_cowface);
        normalDialog.setTitle("提示");
        normalDialog.setMessage("进入离线模式");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext, "进入离线模式", Toast.LENGTH_SHORT).show();
                        FarmGlobal.model = Model.BUILD.value();
                        Intent intent = new Intent();
                        intent.putExtra("ToubaoTempNumber", insuredNo);
                        intent.setClass(mContext, FarmDetectorActivity.class);
                        mContext.startActivity(intent);
                        collectNumberHandler.sendEmptyMessage(2);
//                        HomeActivity mContext = (HomeActivity) ToubaoLocalAdapter.this.mContext;
//                        mContext.finish();
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.setCancelable(false);
        normalDialog.show();
    }

    private void offlineZipUpLoad(String file, String md5, String userId, String batchId) {
        UploadUtils.upload(file, md5, userId.toString(), batchId, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure:", "onFailure离线投保上传失败");
                mHandlerLocal.sendEmptyMessage(14);
                AVOSCloudUtils.saveErrorMessage(e, ToubaoLocalAdapter.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                Log.i("responseString:", responseString);
                offlineBuildResult = HttpUtils.processResp_new_detail_query(responseString);
                status = offlineBuildResult.status;
                Log.e("onResponse", response.toString());


                switch (status) {
                    case 1:
                        // 处理成功
                        Log.e("status == 1: ", file);
                        mHandlerLocal.sendEmptyMessage(1);
                        FileUtils.delFile(file);
                        break;
                    case 0:
                        // 处理失败
                        Log.e("status == 0: ", file);
                        mHandlerLocal.sendEmptyMessage(0);
//                        FileUtils.delFile(file);
                        break;
                    case -2:
                        // 处理失败
                        Log.e("status == -2: ", file);
                        mHandlerLocal.sendEmptyMessage(-2);
                        break;
                    case -4:
                        // 处理失败
                        Log.e("status == -4: ", file);
                        mHandlerLocal.sendEmptyMessage(-4);
                        break;
                    default:
                }
            }
        });
    }

    // TODO: 2018/8/9 By:LuoLu
    @SuppressLint("HandlerLeak")
    private Handler mHandlerLocal = new Handler() {
        // 成功头数
        private int temp1 = 0;
        // 失败头数
        private int temp0 = 0;

        // 上传失败次数
        private int failCnt = 0;

        // 上传时显示用对话框
        ProgressDialog mProgressDialog = null;  //new ProgressDialog(mContext);

        @Override
        public void handleMessage(Message msg) {
            //  Auto-generated method stub
//            ProgressDialog mProgressDialog = null;
            int what = msg.what;
            switch (what) {
                case 1:
                    failCnt = 0;
                    temp1++;
                    mProgressDialog.setTitle(R.string.dialog_title);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setIcon(R.drawable.farm_cowface);
                    mProgressDialog.setMessage("处理成功" + temp1 + "头\n失败" + temp0 + "头");
                    mProgressDialog.show();
                    mProgressDialog.closeOptionsMenu();
                    zipIndex++;
                    callZipUpload();
                    break;

                case 0:
                    failCnt = 0;
                    temp0++;
                    mProgressDialog.setTitle(R.string.dialog_title);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setIcon(R.drawable.farm_cowface);
                    mProgressDialog.setMessage("处理成功" + temp1 + "头\n失败" + temp0 + "头");
                    mProgressDialog.show();
                    mProgressDialog.closeOptionsMenu();


                    zipIndex++;
                    callZipUpload();

                    break;
                case -2:

                    failCnt++;
                    if (failCnt > 3) {
                        zipIndex++;
                        failCnt = 0;
                    }
                    callZipUpload();
                    break;


                case 8:
                    // 点击上传按钮时
                    failCnt = 0;
                    mProgressDialog = new ProgressDialog(mContext);
                    mProgressDialog.setTitle(R.string.dialog_title);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setIcon(R.drawable.farm_cowface);
                    mProgressDialog.setMessage("正在上传......");
                    mProgressDialog.show();
                    mProgressDialog.closeOptionsMenu();
                    break;

                case 9:
                    failCnt = 0;
                    mProgressDialog.dismiss();
                    AlertDialog.Builder innerBuilder9 = new AlertDialog.Builder(mContext)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("上传成功" + temp1 + "头" + "\n失败" + temp0 + "头")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    waitUploadCount = 0;
                                    notifyDataSetChanged();
                                }
                            });
                    innerBuilder9.create();
                    innerBuilder9.setCancelable(false);
                    innerBuilder9.show();
                    break;

                case 14:
                case -4:
                    failCnt = 0;
                    mProgressDialog.dismiss();
                    AlertDialog.Builder innerBuilder14 = new AlertDialog.Builder(mContext)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("服务端异常！请稍后再试")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                }
                            });
                    innerBuilder14.create();
                    innerBuilder14.setCancelable(false);
                    innerBuilder14.show();
                    break;

                case 100:
                    Toast.makeText(mContext, "没有需要上传的离线验标单！", Toast.LENGTH_SHORT).show();
                    break;
                case 101:
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("该保单已审核, 无法继续上传牲畜信息。")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();
                    break;
                default:
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    break;
            }
        }
    };


}
