package com.farm.innovation.login.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.LiPeiLocalBean;
import com.farm.innovation.bean.PayImageUploadResultBean;
import com.farm.innovation.bean.PayInfoContrastResultBean;
import com.farm.innovation.bean.ResultBean;
import com.farm.innovation.bean.VideoUpLoadBean;
import com.farm.innovation.biz.dialog.LipeiResultDialog;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.biz.processor.PayDataProcessor;
import com.farm.innovation.location.AlertDialogManager;
import com.farm.innovation.location.LocationManager;
import com.farm.innovation.login.DatabaseHelper;
import com.farm.innovation.login.Utils;
import com.farm.innovation.login.view.HomeActivity;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.DeviceUtil;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.JsonHelper;
import com.farm.innovation.utils.OkHttp3Util;
import com.farm.innovation.utils.PreferencesUtils;
import com.farm.innovation.utils.UploadUtils;
import com.farm.mainaer.wjoklib.okhttp.upload.UploadTask;
import com.farm.mainaer.wjoklib.okhttp.upload.UploadTaskListener;
import com.google.gson.Gson;
import com.innovation.pig.insurance.R;

import org.json.JSONObject;
import org.tensorflow.demo.FarmDetectorActivity;
import org.tensorflow.demo.FarmGlobal;
import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.farm.innovation.base.FarmAppConfig.getStringTouboaExtra;
import static com.farm.innovation.base.FarmAppConfig.getlipeiTempNumber;
import static com.farm.innovation.biz.processor.PayDataProcessor.getAnimalEarsTagNo;
import static com.farm.innovation.biz.processor.PayDataProcessor.getPayErji;
import static com.farm.innovation.biz.processor.PayDataProcessor.getPaySanji;
import static com.farm.innovation.biz.processor.PayDataProcessor.getPayYiji;
import static com.farm.innovation.login.model.MyUIUTILS.getString;
import static com.farm.innovation.login.view.HomeActivity.isOPen;
import static com.farm.innovation.utils.HttpUtils.FORCE_LIPEI_UPLOAD;
import static com.farm.innovation.utils.HttpUtils.PAY_LIBUPLOAD;
import static com.farm.innovation.utils.MyTextUtil.replaceBlank;
import static org.tensorflow.demo.FarmCameraConnectionFragment.collectNumberHandler;

public class LipeiLocalAdapter extends RecyclerView.Adapter<LipeiLocalAdapter.ViewHolder> {

    private final DatabaseHelper databaseHelper;
    private Gson gson;
    private ProgressDialog uploadDialog;
    private final LocationManager instance;
    private String responsePayInfoContrast;
    private ProgressDialog progressDialog;
    private View view;
    private LipeiResultDialog dialogLipeiResult;
    private ResultBean resultBean;
    private int userId;
    private String pbaodanNo;
    private String yiji;
    private String erji;
    private String sanji;
    private String erbiao;
    private HomeActivity activity;

    public void setLiPeiLocalBeans(List<LiPeiLocalBean> liPeiLocalBeans) {
        this.liPeiLocalBeans = liPeiLocalBeans;
    }

    private List<LiPeiLocalBean> liPeiLocalBeans;
    private Context context;
    private LipeiLocalAdapter.RecyclerViewOnItemClickListener mOnItemClickListener;
    private LipeiLocalAdapter.RecyclerViewOnItemClickListener mOnItemClickListenerZiliao;

    private String result;
    private String TAG = "LipeiAdapter";
    private StringBuffer strfleg;
    private OnUpdateClickListener listener;
    public int lipeiUploadLibId = 0;

    private final Logger mLogger = new Logger(PayDataProcessor.class.getSimpleName());

    public LipeiLocalAdapter(List<LiPeiLocalBean> liPeiLocalBeans, Context mContext) {
        this.liPeiLocalBeans = liPeiLocalBeans;
        this.context = mContext;
        databaseHelper = DatabaseHelper.getInstance(mContext);
        instance = LocationManager.getInstance(context);
        instance.startLocation();
        gson = new Gson();
        dialogLipeiResult = new LipeiResultDialog(context);
    }

    //创建新View，被LayoutManager所调用
    @Override
    public LipeiLocalAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.farm_lipeilocal_item, viewGroup, false);
        LipeiLocalAdapter.ViewHolder vh = new LipeiLocalAdapter.ViewHolder(view);
        return vh;
    }


    private String translateJuanshelan(String pinsureQSL) {
        if (pinsureQSL == null || pinsureQSL.length() == 0) {
            return "";
        }

        String[] splitStr = pinsureQSL.split(",");
        StringBuffer sb = new StringBuffer();

        if (splitStr.length > 0 && !"$".equals(splitStr[0])) {
            sb.append(splitStr[0].replace("$", "") + "区");
        }
        if (splitStr.length > 1 && !"$".equals(splitStr[1])) {
            sb.append(splitStr[1].replace("$", "") + "舍");
        }
        if (splitStr.length > 2 && !"$".equals(splitStr[2])) {
            sb.append(splitStr[2].replace("$", "") + "栏");
        }
        return sb.toString();
    }
    //全局定义
    long lastClickTime = 0L;
    final int FAST_CLICK_DELAY_TIME = 500;  // 快速点击间隔
    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(final LipeiLocalAdapter.ViewHolder viewHolder, final int position) {
        activity = (HomeActivity) context;
        //保单名
        viewHolder.baodan_number.setText(liPeiLocalBeans.get(position).pbaodanName);
        //验标单名字
        viewHolder.yanbiao_name.setText(replaceBlank(liPeiLocalBeans.get(position).pyanbiaodanName));
        //投保人
        viewHolder.toubao_Pname.setText(liPeiLocalBeans.get(position).pinsurename);
        String mtoubao_date;
        mtoubao_date = liPeiLocalBeans.get(position).pinsureDate;
        //投保日期
        viewHolder.lipei_date.setText(mtoubao_date);
        //证件号
        if (liPeiLocalBeans.get(position).pcardNo != null) {
            viewHolder.lipei_idcard.setText(liPeiLocalBeans.get(position).pcardNo);
        }

        Log.i("==pzippath===", liPeiLocalBeans.get(position).pzippath);
        if (null != liPeiLocalBeans.get(position).pzippath && !"".equals(liPeiLocalBeans.get(position).pzippath)) {
            viewHolder.lipeicontinue.setText("重新录入");
        } else {
            viewHolder.lipeicontinue.setText("继续录入");
        }
        String precordeText = liPeiLocalBeans.get(position).precordeText;
        Log.i("=precordeText===", precordeText + "");
        //未录入
        if ("1".equals(liPeiLocalBeans.get(position).precordeText)) {
            viewHolder.uploadtext.setVisibility(View.GONE);
            viewHolder.lipei_update.setVisibility(View.INVISIBLE);
            //已录入
        } else if ("2".equals(liPeiLocalBeans.get(position).precordeText)) {
            viewHolder.uploadtext.setVisibility(View.VISIBLE);
            viewHolder.uploadtext.setText("已录入");
            viewHolder.lipei_update.setVisibility(View.VISIBLE);
            //已上传
        } else if ("3".equals(liPeiLocalBeans.get(position).precordeText)) {
            viewHolder.uploadtext.setVisibility(View.VISIBLE);
            viewHolder.uploadtext.setText("已上传");
            viewHolder.lipei_update.setVisibility(View.INVISIBLE);
        }
        //删除理赔单
        viewHolder.deletelocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strfleg = new StringBuffer();
                strfleg.append("取消收藏将无法离线理赔" + "\n" + "是否确定取消收藏?" + "\n");
                strfleg.append("保单信息：" + "\n");
                strfleg.append("  投保人：" + liPeiLocalBeans.get(position).pinsurename + "\n");
                strfleg.append("  保单号：" + liPeiLocalBeans.get(position).pbaodanNo + "\n");

                String pinsureQSL = liPeiLocalBeans.get(position).pinsureQSL;

                StringBuffer sb = new StringBuffer();
                sb.append("  ");
                sb.append(translateJuanshelan(pinsureQSL));
                sb.append("  ");
                if (!"".equals(liPeiLocalBeans.get(position).earsTagNo)) {
                    sb.append("耳标号:" + liPeiLocalBeans.get(position).earsTagNo);
                }
                strfleg.append(sb.toString());

                if (null != liPeiLocalBeans.get(position).pzippath && !"".equals(liPeiLocalBeans.get(position).pzippath)) {
                    strfleg.append(" \n注意！！！该离线理赔单的理赔牲畜信息已完成，删除后将无法恢复！！！");
                }
                AlertDialogManager.showMessageDialog(context, "提示", strfleg.toString(), new AlertDialogManager.DialogInterface() {
                    @Override
                    public void onPositive() {
                        //删除数据库并删除本地文件
                        String pinsureDate = liPeiLocalBeans.get(position).pinsureDate;
                        Log.i("===pzippath===", pinsureDate);
                        boolean b = databaseHelper.deleteLocalDataFromdate(pinsureDate);
                        Toast.makeText(context, "删除" + b, Toast.LENGTH_LONG).show();
                        FileUtils.deleteFile(liPeiLocalBeans.get(position).pzippath);
                        liPeiLocalBeans.remove(position);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onNegative() {

                    }
                });
            }
        });

        //继续录入
        viewHolder.lipei_rl_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOPen(context)) {
                    if ("继续录入".equals(viewHolder.lipeicontinue.getText().toString())) {
                        //FarmGlobal.model = Model.BUILD.value();
                        Intent intent = new Intent();
                        intent.putExtra("ToubaoTempNumber", liPeiLocalBeans.get(position).pbaodanNo);
                        intent.setClass(context, FarmDetectorActivity.class);
                        PreferencesUtils.saveBooleanValue("isli", true, context);
                        PreferencesUtils.saveKeyValue("lipeidate", liPeiLocalBeans.get(position).pinsureDate, context);
                        context.startActivity(intent);
                        collectNumberHandler.sendEmptyMessage(2);
                    } else {
                        AlertDialogManager.showMessageDialog(context, "提示", liPeiLocalBeans.get(position).precordeMsg, new AlertDialogManager.DialogInterface() {
                            @Override
                            public void onPositive() {
                                //FarmGlobal.model = Model.BUILD.value();
                                Intent intent = new Intent();
                                intent.putExtra("ToubaoTempNumber", liPeiLocalBeans.get(position).pbaodanNo);
                                intent.setClass(context, FarmDetectorActivity.class);
                                PreferencesUtils.saveBooleanValue("isli", true, context);
                                PreferencesUtils.saveKeyValue("lipeidate", liPeiLocalBeans.get(position).pinsureDate, context);
                                context.startActivity(intent);
                                collectNumberHandler.sendEmptyMessage(2);
                            }

                            @Override
                            public void onNegative() {

                            }
                        });
                    }
                } else {
//                    openGPS(mContext);
                    AlertDialogManager.showMessageDialog(context, "提示", getString(R.string.locationwarning), new AlertDialogManager.DialogInterface() {
                        @Override
                        public void onPositive() {
                            openGPS1(context);
                        }

                        @Override
                        public void onNegative() {

                        }
                    });

                }
            }
        });
        //读取用户信息
        SharedPreferences pref_user = context.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        userId = pref_user.getInt("uid", 0);
        //理赔单上传
        viewHolder.lipei_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME){
                    return;
                }
                lastClickTime = System.currentTimeMillis();

                String pinsureQSL = liPeiLocalBeans.get(position).pinsureQSL;
                String[] splitStr = pinsureQSL.split(",");
                yiji = splitStr[0].replace("$", "");
                erji = splitStr[1].replace("$", "");
                sanji = splitStr[2].replace("$", "");
                erbiao = liPeiLocalBeans.get(position).earsTagNo;
                int netWorkStates = com.farm.innovation.network_status.NetworkUtil.getNetWorkStates(context);
                Log.i("netWorkStates", netWorkStates + "");
                if (-1 == netWorkStates) {
                    Toast.makeText(context, "无网络", Toast.LENGTH_LONG).show();
                } else {
                    AlertDialogManager.showDialog(context, "提示", "确定上传理赔单，进行理赔申请吗？", new AlertDialogManager.DiaInterface() {
                        @Override
                        public void onPositive() {
                            PreferencesUtils.saveKeyValue("lipeidate", liPeiLocalBeans.get(position).pinsureDate, context);
                            PreferencesUtils.saveBooleanValue(HttpUtils.offlineupdate, true, context);
                            Log.i("==pzippath===", liPeiLocalBeans.get(position).pzippath);
                            Log.i("==panimalType===", liPeiLocalBeans.get(position).panimalType);
                            Log.i("==pVideozippath===", liPeiLocalBeans.get(position).pVideozippath);
                            PreferencesUtils.saveKeyValue(HttpUtils.reason, liPeiLocalBeans.get(position).pinsureReason, context);
                            PreferencesUtils.saveKeyValue("baodannum", liPeiLocalBeans.get(position).pbaodanNo, context);
                            PreferencesUtils.saveKeyValue("cardnum", liPeiLocalBeans.get(position).pcardNo, context);

                            File zipFile_video2 = new File(liPeiLocalBeans.get(position).pVideozippath);
                            int model = Model.VERIFY.value();

                            pbaodanNo = liPeiLocalBeans.get(position).pbaodanNo;
                            File zipFile_image2 = new File(liPeiLocalBeans.get(position).pzippath);

                            String timesFlag = zipFile_video2.getName();

                            String complete = databaseHelper.queryVideoUpLoadDataBytimesFlag(timesFlag);

                            if (complete.equals("0")) {
                                Log.e(TAG, "----timesFlag----: " + timesFlag);
                                VideoUpLoadBean videoUpLoadBean = new VideoUpLoadBean();
                                videoUpLoadBean.libNub = pbaodanNo;
                                videoUpLoadBean.userId = userId + "";
                                videoUpLoadBean.libEnvinfo = getEnvInfo(FarmAppConfig.getActivity(), FarmAppConfig.version);
                                videoUpLoadBean.animalType = PreferencesUtils.getAnimalType(FarmAppConfig.getActivity()) + "";
                                videoUpLoadBean.collectTimes = "99";
                                videoUpLoadBean.timesFlag = timesFlag;
                                videoUpLoadBean.collectTime = liPeiLocalBeans.get(position).during;
                                videoUpLoadBean.uploadComplete = "0";
                                videoUpLoadBean.videoFilePath = liPeiLocalBeans.get(position).pVideozippath;

                                databaseHelper.inserVideoUpLoadBean(videoUpLoadBean);

                                List<VideoUpLoadBean> list = new ArrayList<>();
                                list.add(videoUpLoadBean);
                                UploadUtils.uploadFile(FarmAppConfig.getActivity(), new UploadTaskListener() {
                                    @Override
                                    public void onUploading(UploadTask uploadTask, String percent, int position) {
                                    }

                                    @Override
                                    public void onUploadSuccess(UploadTask uploadTask, File file) {
                                        VideoUpLoadBean videoUpLoadBean = (VideoUpLoadBean) uploadTask.getT();
                                        videoUpLoadBean.uploadComplete = "1";
                                        databaseHelper.updataVideoUpLoadBean(videoUpLoadBean);
                                    }

                                    @Override
                                    public void onError(UploadTask uploadTask, int errorCode, int position) {
                                    }

                                    @Override
                                    public void onPause(UploadTask uploadTask) {
                                    }
                                }, list);
                            }

                            if (liPeiLocalBeans.get(position).isForce.equals("1")) {
                                uploadIsForcZipImage(model, zipFile_image2, userId, pbaodanNo, position, timesFlag);
                            } else {
                                //upLoadZipVideo(model, zipFile_video2, userId, pbaodanNo, position);
                                uploadZipImage(model, zipFile_image2, userId, pbaodanNo, position, timesFlag);
                            }

                            dialogLipeiResult.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    Log.i("==DismissListener===", "刷新");
                                    viewHolder.uploadtext.setVisibility(View.VISIBLE);
                                    viewHolder.lipei_update.setVisibility(View.INVISIBLE);
                                    notifyDataSetChanged();
                                }
                            });
                        }

                        @Override
                        public void onNegative() {
                        }

                        @Override
                        public void showPop() {
                            HomeActivity activity = (HomeActivity) context;
                            progressDialog = showUploadDialog(activity);
                            progressDialog.show();
                        }
                    });

                }
            }
        });
        if (!"".equals(liPeiLocalBeans.get(position).pinsureQSL) || !"".equals(liPeiLocalBeans.get(position).earsTagNo)) {
            viewHolder.describe.setVisibility(View.VISIBLE);
            StringBuffer sb = new StringBuffer();
            sb.append(translateJuanshelan(liPeiLocalBeans.get(position).pinsureQSL));
            if (!"".equals(liPeiLocalBeans.get(position).earsTagNo)) {
                sb.append(" 耳标号:" + liPeiLocalBeans.get(position).earsTagNo);
            }

            viewHolder.describe.setText(sb.toString());

        } else {
            viewHolder.describe.setVisibility(View.GONE);
        }

    }

    public int positionfleg = 0;

    private void comparer(int lipeiUploadGetLibId, int pot) {
        try {

            SharedPreferences pref_user = context.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
            int userId = pref_user.getInt("uid", 0);
            TreeMap<String, String> treeMapContrast = new TreeMap();
            treeMapContrast.put("baodanNoReal", PreferencesUtils.getStringValue("baodannum", context));
            treeMapContrast.put("reason", PreferencesUtils.getStringValue(HttpUtils.reason, context));
            treeMapContrast.put("cardNo", PreferencesUtils.getStringValue("cardnum", context));
            treeMapContrast.put("yiji", yiji == null ? "" : yiji);
            treeMapContrast.put("erji", erji == null ? "" : erji);
            treeMapContrast.put("sanji", sanji == null ? "" : sanji);
            treeMapContrast.put("pigNo", erbiao == null ? "" : erbiao);
            treeMapContrast.put("userId", String.valueOf(userId) == null ? "" : String.valueOf(userId));
            treeMapContrast.put("libId", String.valueOf(lipeiUploadGetLibId));
            treeMapContrast.put("longitude", liPeiLocalBeans.get(pot).plongitude + "");
            treeMapContrast.put("latitude", liPeiLocalBeans.get(pot).platitude + "");
            treeMapContrast.put("collectTimes", String.valueOf(99));

            instance.getAddressByLatlng(Double.parseDouble(liPeiLocalBeans.get(pot).platitude),
                    Double.parseDouble(liPeiLocalBeans.get(pot).plongitude), new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            treeMapContrast.put("address", msg.obj.toString());
                            return false;
                        }
                    });
            try {
                //设置延迟执行500毫秒，如果不能正常返回地址则不传地址参数
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("理赔信息比对接口请求报文：", treeMapContrast.toString());
            FormBody.Builder builder = new FormBody.Builder();
            for (TreeMap.Entry<String, String> entry : treeMapContrast.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
            RequestBody formBody = builder.build();

            responsePayInfoContrast = HttpUtils.post(HttpUtils.PAY_INFO_CONTRAST, formBody);
            Log.i("payInfoHandler:", HttpUtils.PAY_INFO_CONTRAST + "\n理赔信息比对接口responsePayInfoContrast:\n" + responsePayInfoContrast);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("理赔", "理赔信息比对接口异常" + e.toString());
            AVOSCloudUtils.saveErrorMessage(e, LipeiLocalAdapter.class.getSimpleName());
            //Toast.makeText(context, "理赔信息比对接口异常！", Toast.LENGTH_SHORT).show();
        }

        if (null != responsePayInfoContrast) {
            progressDialog.dismiss();
            ResultBean resultBeanPayInfoContrast = gson.fromJson(responsePayInfoContrast, ResultBean.class);
            if (resultBeanPayInfoContrast.getStatus() == 1) {
                positionfleg = pot;
                //   展示比对结果
                payInfoHandler.sendEmptyMessage(18);
            } else if (resultBeanPayInfoContrast.getStatus() == 0) {
                Log.e("理赔", resultBeanPayInfoContrast.getMsg());
                // hedazhi 2018/11/10 0:11 commented start
                resultBean = resultBeanPayInfoContrast;
                payInfoHandler.sendEmptyMessage(422);
                //     collectNumberHandler.sendEmptyMessage(2);
                // hedazhi 2018/11/10 0:11 commented end
//            } else {

                // hedazhi 2018/11/10 0:11 commented start
          /*      AlertDialog.Builder builder22 = new AlertDialog.Builder(context)
                        .setIcon(R.drawable.farm_cowface)
                        .setTitle("提示")
                        .setMessage(resultBeanPayInfoContrast.getMsg())
                        .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClickView(DialogInterface dialog, int which) {
                                liPeiLocalBeans.get(pot).setPrecordeText("3");
                                notifyDataSetChanged();
                                dialog.dismiss();
                                //  mActivity.finish();
                            }
                        });
                builder22.setCancelable(false);
                builder22.show();*/
                // hedazhi 2018/11/10 0:11 commented end
            }
        } else {
            progressDialog.dismiss();
            //                server down
            payInfoHandler.sendEmptyMessage(42);

        }


    }

    private void reInitCurrentDir() {
        Log.i("reInitCurrentDir:", "重新初始化Current文件");
        if (FarmGlobal.model == Model.BUILD.value()) {
            FarmGlobal.mediaInsureItem.currentDel();
            FarmGlobal.mediaInsureItem.currentInit();
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            FarmGlobal.mediaPayItem.currentDel();
            FarmGlobal.mediaPayItem.currentInit();
        }
    }

    public ProgressDialog showUploadDialog(Activity activity) {
        ProgressDialog mUploadDialog = new ProgressDialog(activity);
        mUploadDialog.setTitle(R.string.dialog_title);
        mUploadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mUploadDialog.setCancelable(false);
        mUploadDialog.setCanceledOnTouchOutside(false);
        mUploadDialog.setIcon(R.drawable.farm_cowface);
        mUploadDialog.setMessage("正在处理......");
        Log.i("====show==", "show" + System.currentTimeMillis()
        );
        return mUploadDialog;
    }

    private void openGPS1(Context mContext) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        HomeActivity activity = (HomeActivity) mContext;
        activity.startActivityForResult(intent, 1315);
    }

    //获取数据的数量
    @Override
    public int getItemCount() {
        return liPeiLocalBeans.size();
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView yanbiao_name;
        public TextView toubao_Pname;
        public TextView isnot_lipei;
        public TextView lipei_date;
        public TextView lipei_idcard;
        public RelativeLayout lipei_update;
        public RelativeLayout deletelocal;
        public TextView uploadtext, describe;
        public TextView lipeicontinue, toubao_continue_no, baodan_number;
        public RelativeLayout relative, lipei_rl_continue;

        public ViewHolder(View view) {
            super(view);
            yanbiao_name = (TextView) view.findViewById(R.id.yanbiao_name);
            toubao_Pname = (TextView) view.findViewById(R.id.toubao_Pname);
            lipei_date = (TextView) view.findViewById(R.id.lipei_date);
            lipei_idcard = (TextView) view.findViewById(R.id.lipei_idcard);
            isnot_lipei = (TextView) view.findViewById(R.id.isnot_lipei);
            lipei_update = (RelativeLayout) view.findViewById(R.id.lipei_update);
            deletelocal = (RelativeLayout) view.findViewById(R.id.deletelocal);
            uploadtext = (TextView) view.findViewById(R.id.uploadtext);
            lipeicontinue = (TextView) view.findViewById(R.id.lipei_continue);
            baodan_number = (TextView) view.findViewById(R.id.baodan_number);
            describe = (TextView) view.findViewById(R.id.describe);
            //toubao_continue_no = (TextView) view.findViewById(R.id.toubao_continue_no);
            relative = (RelativeLayout) view.findViewById(R.id.relative);
            lipei_rl_continue = (RelativeLayout) view.findViewById(R.id.lipei_rl_continue);
        }
    }


    /**
     * 设置点击事件
     */
    public void setRecyclerViewOnItemClickListener(LipeiLocalAdapter.RecyclerViewOnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    /**
     * 设置点击事件
     */
    public void setRecyclerViewOnItemClickListenerZiliao(LipeiLocalAdapter.RecyclerViewOnItemClickListener onItemClickListener) {
        this.mOnItemClickListenerZiliao = onItemClickListener;
    }


    /**
     * 点击事件接口
     */
    public interface RecyclerViewOnItemClickListener {
        void onItemClickListener(View view, int position);
    }

    public interface OnUpdateClickListener {
        void onUpdateClick(File uploadFile, int model, int userId, String pbaodanNo);
    }


    public void setOnUpdateClickListener(OnUpdateClickListener listener) {
        this.listener = listener;
    }

    public void uploadIsForcZipImage(int model, File zipFileImage, int uid, String libNum, int position, String timesFlag) {
        //publishProgress(MSG_UI_PROGRESS_UPLOAD_IMG);
        int source = 1;
        String gps = null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put("userId", uid + "");
            map.put("libEnvinfo", getEnvInfo(context, FarmAppConfig.version));
            map.put("baodanNoReal", PreferencesUtils.getStringValue("baodannum", context));
            map.put("cardNo", PreferencesUtils.getStringValue("cardnum", context));
            map.put("reason", PreferencesUtils.getStringValue("reason", context));
            map.put("yiji", getPayYiji == null ? "" : getPayYiji);
            map.put("erji", getPayErji == null ? "" : getPayErji);
            map.put("sanji", getPaySanji == null ? "" : getPaySanji);
            map.put("pigNo", getAnimalEarsTagNo == null ? "" : getAnimalEarsTagNo);
            map.put("longitude", liPeiLocalBeans.get(position).plongitude + "");
            map.put("latitude", liPeiLocalBeans.get(position).platitude + "");
            map.put(Utils.UploadNew.COLLECT_TIME, FarmAppConfig.during / 1000 + "");
            map.put("timesFlag", timesFlag);

            instance.getAddressByLatlng(Double.parseDouble(liPeiLocalBeans.get(position).platitude),
                    Double.parseDouble(liPeiLocalBeans.get(position).plongitude), new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            Log.e("获取地址=", msg.obj.toString());
                            map.put("address", msg.obj.toString());
                            return false;
                        }
                    });
            Map<String, String> header = new HashMap<>();
            header.put("AppKeyAuthorization", "hopen");
            header.put("Content-Type", "application/x-www-form-urlencoded");

            try {
                //设置延迟执行500毫秒，如果不能正常返回地址则不传地址参数
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            OkHttp3Util.uploadPreFile(FORCE_LIPEI_UPLOAD, zipFileImage, zipFileImage.getName(), map, header, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("===e==", e.toString());
                    progressDialog.dismiss();
                    // server down
                    payInfoHandler.sendEmptyMessage(42);
                    AVOSCloudUtils.saveErrorMessage(e, LipeiLocalAdapter.class.getSimpleName());

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //  if (response.isSuccessful()) {
                    String responsePayZipImageUpload = response.body().string();
                    if (responsePayZipImageUpload != null) {
                        //Log.e("理赔图片包上传接口返回：\n", PAY_LIBUPLOAD + "\nresponsePayZipImageUpload:\n" + responsePayZipImageUpload);
                        ResultBean resultPayZipImageBean = gson.fromJson(responsePayZipImageUpload, ResultBean.class);
                        if (resultPayZipImageBean.getStatus() == 1) {

                            int lipeirecordernum = databaseHelper.updateLiPeiLocalFromrecordeText("3", PreferencesUtils.getStringValue("lipeidate", context));
                            Log.i("=lipeirecordernum===", lipeirecordernum + "");
                            liPeiLocalBeans.get(position).setPrecordeText("3");

                            Log.e(TAG, "liPeiLocalBeans" + liPeiLocalBeans.toString());

                            payInfoHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    notifyDataSetChanged();
                                }
                            }, 10);

                            progressDialog.dismiss();
                            payInfoHandler.sendEmptyMessage(999);

                        } else {
                            progressDialog.dismiss();
                            resultBean = resultPayZipImageBean;
                            payInfoHandler.sendEmptyMessage(422);
                        }
                    } else {
                        progressDialog.dismiss();
                        //server down
                        payInfoHandler.sendEmptyMessage(42);
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, LipeiLocalAdapter.class.getSimpleName());
        }
    }

    public void uploadZipImage(int model, File zipFileImage, int uid, String libNum, int position, String timesFlag) {
        //publishProgress(MSG_UI_PROGRESS_UPLOAD_IMG);
        int source = 1;
        String gps = null;
        try {
            TreeMap<String, String> treeMap = new TreeMap<>();
            treeMap.put(Utils.UploadNew.USERID, uid + "");
            treeMap.put(Utils.UploadNew.LIB_NUM, libNum);
            treeMap.put(Utils.UploadNew.TYPE, model + "");
            treeMap.put(Utils.UploadNew.LIBD_SOURCE, source + "");
            //    treeMap.put(Utils.UploadNew.LIB_ENVINFO, getEnvInfo(mActivity, gps));
            treeMap.put(Utils.UploadNew.LIB_ENVINFO, "");
            treeMap.put("collectTimes", String.valueOf(99));
            treeMap.put("timesFlag", timesFlag);
            treeMap.put(Utils.UploadNew.COLLECT_TIME, FarmAppConfig.during / 1000 + "");

            //Log.e("理赔图片包上传接口请求报文：", treeMap.toString() + "\n请求地址：" + PAY_LIBUPLOAD);

            Map<String, String> header = new HashMap<>();
            header.put("AppKeyAuthorization", "hopen");
            header.put("Content-Type", "application/x-www-form-urlencoded");

            OkHttp3Util.uploadPreFile(PAY_LIBUPLOAD, zipFileImage, zipFileImage.getName(), treeMap, header, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("===e==", e.toString());
                    progressDialog.dismiss();
                    // server down
                    payInfoHandler.sendEmptyMessage(42);
                    AVOSCloudUtils.saveErrorMessage(e, LipeiLocalAdapter.class.getSimpleName());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //  if (response.isSuccessful()) {
                    String responsePayZipImageUpload = response.body().string();
                    if (responsePayZipImageUpload != null) {
                        //Log.e("理赔图片包上传接口返回：\n", PAY_LIBUPLOAD + "\nresponsePayZipImageUpload:\n" + responsePayZipImageUpload);
                        ResultBean resultPayZipImageBean = gson.fromJson(responsePayZipImageUpload, ResultBean.class);
                        if (resultPayZipImageBean.getStatus() == 1) {
                            PayImageUploadResultBean payImageUploadResultBean = gson.fromJson(responsePayZipImageUpload, PayImageUploadResultBean.class);
                            // 2018/11/10 0:37 hedazhi commented start
/*                            int lipeirecordernum = databaseHelper.updateLiPeiLocalFromrecordeText("3", PreferencesUtils.getStringValue("lipeidate", context));
                            Log.i("=lipeirecordernum===", lipeirecordernum + "");*/
                            // 2018/11/10 0:37 hedazhi commented end
                            //获取ib_id
                            int lipeiUploadGetLibId = payImageUploadResultBean.getData().getLibId();
                            //publishProgress(MSG_UI_PROGRESS_IMAGE_CONTRAST);
                            // payInfoHandler.sendEmptyMessage(17);
                            comparer(lipeiUploadGetLibId, position);
                        } else {
                            progressDialog.dismiss();
                            resultBean = resultPayZipImageBean;
                            payInfoHandler.sendEmptyMessage(422);
                        }
                    } else {
                        progressDialog.dismiss();
                        //                server down
                        payInfoHandler.sendEmptyMessage(42);
                    }
                }
                // }
            });

        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, LipeiLocalAdapter.class.getSimpleName());
        }

    }

    private void upLoadZipVideo(int model, File zipFileVideo, int uid, String libNum, int position) {
        int source = 2;
        String gps = null;
        try {
            TreeMap<String, String> treeMap = new TreeMap<>();
            treeMap.put(Utils.UploadNew.USERID, uid + "");
            treeMap.put(Utils.UploadNew.LIB_NUM, libNum);
            treeMap.put(Utils.UploadNew.TYPE, model + "");
            treeMap.put(Utils.UploadNew.LIBD_SOURCE, source + "");
            treeMap.put(Utils.UploadNew.COLLECT_TIME, FarmAppConfig.during / 1000 + "");
//            treeMap.put(Utils.UploadNew.LIB_ENVINFO, getEnvInfo(context, gps));
            Map<String, String> header = new HashMap<>();
            header.put("AppKeyAuthorization", "hopen");
            header.put("Content-Type", "application/x-www-form-urlencoded");


            //Log.e("离线理赔视频上传接口请求报文：", treeMap.toString() + "\n请求地址：" + PAY_LIBUPLOAD);
            OkHttp3Util.uploadPreFile(PAY_LIBUPLOAD, zipFileVideo, "video.zip", treeMap, header, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // 离线理赔上传视频时失败，后续处理继续
                    //progressDialog.dismiss();
                    Log.e("离线理赔视频上传接口eee：", e.toString());
                    AVOSCloudUtils.saveErrorMessage(e, LipeiLocalAdapter.class.getSimpleName());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responsePayVideoUpload = response.body().string();
                        //mLogger.e("离线理赔视频文件上传接口返回：\n" + PAY_LIBUPLOAD + "\nresponsePayVideoUpload:" + responsePayVideoUpload);

                        // 2018/11/10 0:21 hedazhi commented start

                       /* if (responsePayVideoUpload != null) {
                            resultBean = gson.fromJson(responsePayVideoUpload, ResultBean.class);
                            if (resultBean.getStatus() == 1) {
//                        upload success
                                mLogger.i("responsePayImageUpload data:" + resultBean.getData().toString());
//                        toubaoUploadBean = gson.fromJson(responsePayImageUpload, ToubaoUploadBean.class);
//                        mLogger.i("理赔视频 libID:" + toubaoUploadBean.getData().getLibId());
//                        addAnimalLibID = String.valueOf(toubaoUploadBean.getData().getLibId());

//                        insuranceDataHandler.sendEmptyMessage(18);
                            } else if (resultBean.getStatus() == 0) {
//                        image bad
                                payInfoHandler.sendEmptyMessage(199);
                            } else {
//                server down
                                payInfoHandler.sendEmptyMessage(422);
                            }
                        }*/
                        // 2018/11/10 0:21 hedazhi commented end
                    }
                }
            });


           /* MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
            FormBody.Builder builder = new FormBody.Builder();
            for (TreeMap.Entry<String, String> entry : treeMap.entrySet()) {
                requestBody.addFormDataPart(entry.getKey(), entry.getValue());
            }
            requestBody.addFormDataPart("zipFile", zipFile_image.getName(),
                    RequestBody.create(MediaType.parse("application/octet-stream"), zipFile_image));
            // TODO: 2018/8/4*/
            // String responsePayVideoUpload = HttpUtils.post(PAY_LIBUPLOAD, requestBody.build());

        } catch (Exception e) {
            e.printStackTrace();
            AVOSCloudUtils.saveErrorMessage(e, LipeiLocalAdapter.class.getSimpleName());
        }
    }

    private SharedPreferences pref_user;
    private int payInfoContrastResultLipeiId;
    @SuppressLint("HandlerLeak")
    private final Handler payInfoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 18:
                    PayInfoContrastResultBean payInfoContrastResultBean = gson.fromJson(responsePayInfoContrast, PayInfoContrastResultBean.class);
                    payInfoContrastResultLipeiId = payInfoContrastResultBean.getData().getLipeiId();
                    dialogLipeiResult.setLipeiResultmessage(payInfoContrastResultBean.getData().getResultMsg());
                    int size = (payInfoContrastResultBean.getData().getResultPic() == null) ? 0 : payInfoContrastResultBean.getData().getResultPic().size();
                    switch (size) {
                        case 1:
                            dialogLipeiResult.setImage2(String.valueOf(payInfoContrastResultBean.getData().getResultPic().get(0).getPic()));
                            dialogLipeiResult.setLipeiResultmessage(payInfoContrastResultBean.getData().getResultMsg()
                                    + "\n"
                                    + payInfoContrastResultBean.getData().getResultPic().get(0).getDetail() + "\n");
                            break;
                        case 2:
                            dialogLipeiResult.setImage1(String.valueOf(payInfoContrastResultBean.getData().getResultPic().get(0).getPic()));
                            dialogLipeiResult.setImage3(String.valueOf(payInfoContrastResultBean.getData().getResultPic().get(1).getPic()));
                            dialogLipeiResult.setLipeiResultmessage(payInfoContrastResultBean.getData().getResultMsg()
                                    + "\n" + payInfoContrastResultBean.getData().getResultPic().get(0).getDetail()
                                    + "\n" + payInfoContrastResultBean.getData().getResultPic().get(1).getDetail() + "\n");
                            break;
                        case 3:
                            dialogLipeiResult.setImage1(String.valueOf(payInfoContrastResultBean.getData().getResultPic().get(0).getPic()));
                            dialogLipeiResult.setImage2(String.valueOf(payInfoContrastResultBean.getData().getResultPic().get(1).getPic()));
                            dialogLipeiResult.setImage3(String.valueOf(payInfoContrastResultBean.getData().getResultPic().get(2).getPic()));
                            dialogLipeiResult.setLipeiResultmessage(payInfoContrastResultBean.getData().getResultMsg()
                                    + "\n" + payInfoContrastResultBean.getData().getResultPic().get(0).getDetail()
                                    + "\n" + payInfoContrastResultBean.getData().getResultPic().get(1).getDetail()
                                    + "\n" + payInfoContrastResultBean.getData().getResultPic().get(2).getDetail() + "\n");
                            break;
                        default:
                    }
                    if (size == 0) {
                        dialogLipeiResult.setImagesViewGone();
                    } else {
                        dialogLipeiResult.setImagesViewVisible();
                    }

                    View.OnClickListener listener_new = v -> {
                        dialogLipeiResult.dismiss();
                        //    1.4	理赔申请处理接口
                        payInfoHandler.sendEmptyMessage(19);

                    };
                    View.OnClickListener listener_ReCollect = v -> {
                        FarmAppConfig.during = 0;
                        // 离线时处理（放弃）
                        if (PreferencesUtils.getBooleanValue(HttpUtils.offlineupdate, context)) {
                            dialogLipeiResult.dismiss();
                        } else {
                            // 重新拍摄
                            dialogLipeiResult.dismiss();
                            Intent intent = new Intent(context, FarmDetectorActivity.class);
                            intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                            intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
                            context.startActivity(intent);
                            reInitCurrentDir();
                            collectNumberHandler.sendEmptyMessage(2);

                        }
                    };

                    dialogLipeiResult.setTitle("验证结果");
                    dialogLipeiResult.setBtnGoApplication("直接申请", listener_new);
                    if (PreferencesUtils.getBooleanValue(HttpUtils.offlineupdate, context)) {
                        dialogLipeiResult.setBtnReCollect("放弃", listener_ReCollect);
                    } else {
                        dialogLipeiResult.setBtnReCollect("重新拍摄", listener_ReCollect);
                    }
                    dialogLipeiResult.show();
                    break;


                case 19:
//                    String responsePayApplyResult = null;
                    try {
                        Map<String, String> mapPayApply = new HashMap<>();
                        mapPayApply.put("lipeiId", String.valueOf(payInfoContrastResultLipeiId));
                        Log.e("理赔申请处理接口", mapPayApply.toString());

                        OkHttp3Util.doPost(HttpUtils.PAY_APPLY, mapPayApply, null, new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                payInfoHandler.sendEmptyMessage(42);
                                AVOSCloudUtils.saveErrorMessage(e, LipeiLocalAdapter.class.getSimpleName());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String responselipeiApply = response.body().string();
                                Log.i("payInfoHandler:", HttpUtils.PAY_APPLY + "\n理赔申请处理接口responsePayApplyResult:\n"
                                        + responselipeiApply);

                                ResultBean resultBeanPayApply = gson.fromJson(responselipeiApply, ResultBean.class);
                                payInfoHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (resultBeanPayApply.getStatus() == 1) {
                                            int lipeirecordernum = databaseHelper.updateLiPeiLocalFromrecordeText("3", PreferencesUtils.getStringValue("lipeidate", context));
                                            Log.i("=lipeirecordernum===", lipeirecordernum + "");
                                            liPeiLocalBeans.get(positionfleg).setPrecordeText("3");
                                            notifyDataSetChanged();
                                            // 2018/11/10 0:42 hedazhi add end
                                            AlertDialog.Builder builderApplyFinish = new AlertDialog.Builder(context)
                                                    .setIcon(R.drawable.farm_cowface)
                                                    .setTitle("提示")
                                                    .setMessage(resultBeanPayApply.getMsg())
                                                    .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                            builderApplyFinish.setCancelable(false);
                                            builderApplyFinish.show();
                                        } else {
                                            AlertDialog.Builder builderApplyFinish = new AlertDialog.Builder(context)
                                                    .setIcon(R.drawable.farm_cowface)
                                                    .setTitle("提示")
                                                    .setMessage(resultBeanPayApply.getMsg())
                                                    .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            payInfoHandler.sendEmptyMessage(19);
                                                        }
                                                    });
                                            builderApplyFinish.setCancelable(false);
                                            builderApplyFinish.show();
                                        }
                                    }
                                }, 100);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertDialog.Builder builderApplyFinish = new AlertDialog.Builder(context)
                                .setIcon(R.drawable.farm_cowface)
                                .setTitle("提示")
                                .setMessage("理赔申请处理接口异常。")
                                .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        payInfoHandler.sendEmptyMessage(19);
                                    }
                                });

                        builderApplyFinish.setCancelable(false);
                        builderApplyFinish.show();
                        AVOSCloudUtils.saveErrorMessage(e, LipeiLocalAdapter.class.getSimpleName());
                    }
                    break;
                case 199:
                    AlertDialog.Builder builder199 = new AlertDialog.Builder(context)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage(resultBean.getMsg())
                            .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(context, FarmDetectorActivity.class);
                                    intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                    context.startActivity(intent);
                                    reInitCurrentDir();
                                    collectNumberHandler.sendEmptyMessage(2);
                                    // context.finish();
                                }
                            });
                    builder199.setCancelable(false);
                    builder199.show();
                    break;
                case 422:
                    AlertDialog.Builder builder422 = new AlertDialog.Builder(context)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage(resultBean.getMsg())
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    //mActivity.finish();
                                }
                            });
                    builder422.setCancelable(false);
                    builder422.show();
                    break;
                case 42:
                    AlertDialog.Builder builder42 = new AlertDialog.Builder(context)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("网络异常，请稍后重试。")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    //mActivity.finish();
                                }
                            });
                    builder42.setCancelable(false);
                    builder42.show();
                    break;
                case 999: // 强制上传后提示拨打电话
                    String customServ = PreferencesUtils.getStringValue(FarmAppConfig.customServ, FarmAppConfig.getActivity());
                    String phone = PreferencesUtils.getStringValue(FarmAppConfig.phone, FarmAppConfig.getActivity());

                    AlertDialog.Builder builder999 = new AlertDialog.Builder(context)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("提交成功，系统无法找到对应牲畜。\n" +
                                    "请用手机直接对着牲畜的左、中、右脸拍摄一段不少于2分钟视频，留存作为档案提交到" + customServ + "处。\n" +
                                    "后台将进行人工复核，复核结果会通过短信方式通知。如有疑问请致电人工坐席服务电话：" + phone + "。")
                            .setPositiveButton("拨打客服电话", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    Uri data = Uri.parse("tel:" + phone);
                                    intent.setData(data);
                                    context.startActivity(intent);
                                }
                            })
                            .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder999.setCancelable(false);
                    builder999.show();
                    break;
                default:
                    break;
            }
        }
    };


    public static String getEnvInfo(Context context, String version) {
        JSONObject jo = new JSONObject();
        String imei = DeviceUtil.getImei(context);
        JsonHelper.putString(jo, Utils.Upload.imei, Utils.getMD5(imei));
        if (TextUtils.isEmpty(version)) {
            version = LocationManager.getInstance(context).getLocationDetail();
        }
        JsonHelper.putString(jo, Utils.Upload.VERSION, version);
        return jo.toString();
    }


}
