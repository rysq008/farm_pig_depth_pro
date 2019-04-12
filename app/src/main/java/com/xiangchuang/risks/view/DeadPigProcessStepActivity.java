package com.xiangchuang.risks.view;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.UncompletedBean;
import com.xiangchuang.risks.model.custom.DiyVideoView;
import com.xiangchuang.risks.utils.TimeUtil;
import com.xiangchuang.risks.utils.ToastUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import innovation.location.LocationManager_new;
import innovation.utils.FileUtils;
import innovation.utils.UIUtils;
import innovation.view.ProcessPigInfoDialog;
import innovation.view.dialog.DialogHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * @Author: Lucas.Cui
 * 时   间：2019/4/9
 * 简   述： 无害化处理步骤处理
 */
public class DeadPigProcessStepActivity extends BaseActivity implements View.OnClickListener {
    ImageView iv_cancel, iv_pic, iv_opencamera;
    Button btn_process_pre_step, btn_process_skip_step, btn_play_video, btn_collect_info, btn_process_next_step;
    LinearLayout ll_process_pre, ll_processing;
    TextView tvTitle, tv_step_position, tv_step_detail, tv_process_pig_num;
    DiyVideoView vv_video;
    ImageView back_img, iv_questionmark;
    RelativeLayout video_contral_layer;
    RelativeLayout rl_content;
    ProgressBar progressBar;
    SeekBar seekbar;
    ImageView play_img;
    TextView play_progress_time;
    public static final int PROCESS_PRE_STEP = 1000;
    public static final int PROCESS_NEXT_STEP = 1001;
    public static final int PROCESS_SKIP_STEP = 1002;
    /**
     * 处理类型 0 拍摄图片，1 录制视频
     */
    public static final int DEAL_TYPE_PICTURE = 0;
    public static final int DEAL_TYPE_VIDEO = 1;
    private UncompletedBean processStepBean;
    private UncompletedBean.currentStepInfo infoBean;
    private String mImgPath, mVideoPath, filePath = "", fileName = "";
    private long mCurrentProgress = 0;
    private int mCurrentStep;
    private boolean isCreateOrder;

    public static void start(Activity context, UncompletedBean processStepBean, int step) {
        Intent intent = new Intent(context, DeadPigProcessStepActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("step", step);
        bundle.putParcelable("Uncompleted", processStepBean);
        intent.putExtra("data", bundle);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_dealpig_processstop;
    }


    @Override
    public void initView() {
        super.initView();
        tvTitle = findViewById(R.id.tv_title);
        iv_cancel = findViewById(R.id.iv_cancel);
        iv_pic = findViewById(R.id.iv_pic);
        iv_opencamera = findViewById(R.id.iv_opencamera);
        btn_process_pre_step = findViewById(R.id.btn_process_pre_step);
        btn_process_skip_step = findViewById(R.id.btn_process_skip_step);
        btn_play_video = findViewById(R.id.btn_play_video);
        btn_collect_info = findViewById(R.id.btn_collect_info);
        btn_process_next_step = findViewById(R.id.btn_process_next_step);
        ll_process_pre = findViewById(R.id.ll_process_pre);
        ll_processing = findViewById(R.id.ll_processing);
        tv_step_position = findViewById(R.id.tv_step_position);
        tv_step_detail = findViewById(R.id.tv_step_detail);
        vv_video = findViewById(R.id.vv_video);
        back_img = findViewById(R.id.item_listvideo_img);
        video_contral_layer = findViewById(R.id.item_listvideo_layer);
        rl_content = findViewById(R.id.rl_content);
        progressBar = findViewById(R.id.item_listvideo_loading);
        seekbar = findViewById(R.id.item_listvideo_seekbar);
        play_img = findViewById(R.id.item_listvideo_play);
        play_progress_time = findViewById(R.id.item_listvideo_time);
        iv_questionmark = findViewById(R.id.iv_questionmark);
        tv_process_pig_num = findViewById(R.id.tv_process_pig_num);
        tvTitle.requestFocus();
        setListener();
    }

    private void setListener() {
        iv_cancel.setOnClickListener(this);
        iv_opencamera.setOnClickListener(this);
        btn_process_skip_step.setOnClickListener(this);
        btn_process_pre_step.setOnClickListener(this);
        btn_process_next_step.setOnClickListener(this);
        btn_collect_info.setOnClickListener(this);
        btn_play_video.setOnClickListener(this);
        iv_questionmark.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        Bundle bundle = (Bundle) getIntent().getBundleExtra("data");
        processStepBean = (UncompletedBean) bundle.getParcelable("Uncompleted");
        isCreateOrder = bundle.getBoolean("isCreateOrder", false);
        if (processStepBean != null) {
            mCurrentStep = bundle.getInt("step", 0);
            if (mCurrentStep == 0) {
                mCurrentStep = Integer.valueOf(processStepBean.getStep()) + 1;
            }
            tvTitle.setText(processStepBean.getCurrentStep().getName());
            infoBean = processStepBean.getCurrentStep().getCurrentStepInfoList().get(mCurrentStep - 1);
            tv_step_position.setText("第" + mCurrentStep + "步：");
            tv_step_detail.setText(infoBean.getDescribe());
//            tv_step_detail.setText(infoBean.get());
            if (infoBean.getDealType() == DEAL_TYPE_PICTURE) {
                btn_play_video.setVisibility(View.GONE);
            }
            if (mCurrentStep == 1) {
                btn_process_pre_step.setVisibility(View.GONE);
            }
            if (mCurrentStep == processStepBean.getCurrentStep().getCurrentStepInfoList().size()) {
                btn_process_next_step.setText("完成");
                btn_process_skip_step.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btn_process_pre_step.getLayoutParams();
                params.rightMargin = 0;
                btn_process_pre_step.setLayoutParams(params);

            }
            int pigNum = 0;
            for (int i = 0; i < processStepBean.getLipeiInfos().size(); i++) {
                for (int j = 0; j < processStepBean.getLipeiInfos().get(i).getPayInfoList().size(); j++) {
                    pigNum = pigNum + 1;
                }
            }
            tv_process_pig_num.setText("处理猪只数量：" + pigNum + "头");
            initVideoData();
        } else {
            ToastUtils.getInstance().showShort(DeadPigProcessStepActivity.this, "没有数据");
            finish();
        }
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.iv_cancel) {
            if (iv_opencamera.getVisibility() == View.GONE) {
                DialogHelper.exitDeadPigProcessDialog(DeadPigProcessStepActivity.this, isCreateOrder);
            } else {
                if (!isCreateOrder) {
                    goToActivity(SelectFunctionActivity_new.class, null);
                }
                finish();
            }
        } else if (i == R.id.iv_opencamera) {
            if (infoBean.getDealType() == DEAL_TYPE_PICTURE) {
                CameraPicActivity.start(DeadPigProcessStepActivity.this);
            } else if (infoBean.getDealType() == DEAL_TYPE_VIDEO) {
                CustomRecordActivity.start(DeadPigProcessStepActivity.this, Integer.valueOf(infoBean.getRecTime()));
            }

        } else if (i == R.id.btn_process_pre_step) {

            DialogHelper.deadPigProcessExitTip(DeadPigProcessStepActivity.this, listener, PROCESS_PRE_STEP, getResources().getString(R.string.deadpig_process_back_tip));
        } else if (i == R.id.btn_process_skip_step) {
            DialogHelper.deadPigProcessExitTip(DeadPigProcessStepActivity.this, listener, PROCESS_SKIP_STEP, getResources().getString(R.string.deadpig_process_skip_tip));
        } else if (i == R.id.btn_process_next_step) {
//            DialogHelper.deadPigProcessExitTip(DeadPigProcessStepActivity.this, listener, PROCESS_NEXT_STEP);
            completeStep(0);
        } else if (i == R.id.btn_collect_info) {
            if (infoBean.getDealType() == DEAL_TYPE_PICTURE) {
                CameraPicActivity.start(DeadPigProcessStepActivity.this);
            } else if (infoBean.getDealType() == DEAL_TYPE_VIDEO) {
                CustomRecordActivity.start(DeadPigProcessStepActivity.this, Integer.valueOf(infoBean.getRecTime()));
            }

        } else if (i == R.id.btn_play_video) {
            playOrPause(mVideoPath);

        }  else if (i == R.id.iv_questionmark) {
            ProcessPigInfoDialog pigInfoDialog = new ProcessPigInfoDialog(DeadPigProcessStepActivity.this);
            pigInfoDialog.setData(processStepBean);
            pigInfoDialog.show();
        } else {

        }
    }

    /**
     * 跳过或者下一步弹框提醒用户
     */
    DialogHelper.CallBackListener listener = new DialogHelper.CallBackListener() {
        @Override
        public void onSuccess(int step) {
            if (step == PROCESS_SKIP_STEP) {
                completeStep(1);
            } else if (step == PROCESS_NEXT_STEP) {
//                completeStep(0);
            } else if (step == PROCESS_PRE_STEP) {
                DeadPigProcessStepActivity.start(DeadPigProcessStepActivity.this, processStepBean, --mCurrentStep);
                finish();
            }
        }

        @Override
        public void onFailed(int step) {

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case AddPigPicActivity.REQUESTCODE_TAKE:
                if (!TextUtils.isEmpty(mImgPath)) {
                    FileUtils.deleteFile(mImgPath);
                }
                mImgPath = data.getStringExtra("path");
                iv_opencamera.setVisibility(View.GONE);
                rl_content.setVisibility(View.GONE);
                ll_process_pre.setVisibility(View.GONE);
                iv_pic.setVisibility(View.VISIBLE);
                ll_processing.setVisibility(View.VISIBLE);
                Glide.with(DeadPigProcessStepActivity.this).load(Uri.fromFile(new File(mImgPath))).into(iv_pic);
                break;
            case CustomRecordActivity.REQUEST_RECORDER:
                if (!TextUtils.isEmpty(mVideoPath)) {
                    FileUtils.deleteFile(mVideoPath);
                }
                mVideoPath = data.getStringExtra("videoPath");
                iv_opencamera.setVisibility(View.GONE);
                iv_pic.setVisibility(View.GONE);
                ll_process_pre.setVisibility(View.GONE);
                rl_content.setVisibility(View.VISIBLE);
                ll_processing.setVisibility(View.VISIBLE);
                playOrPause(mVideoPath);
                break;
            default:
                break;
        }
    }


    public void initVideoData() {

        vv_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {

                progressHandler.sendEmptyMessage(0);
                seekbar.setEnabled(true);
                vv_video.seekTo(vv_video.getDuration() * seekbar.getProgress() / 100);
                progressBar.setVisibility(View.GONE);

                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    int mVideoWidth, mVideoHeight, showVideoWidth, showVideoHeight;
                    float scale, showRadio;

                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        //FixMe 获取视频资源的宽度
                        mVideoWidth = mp.getVideoWidth();
                        //FixMe 获取视频资源的高度
                        mVideoHeight = mp.getVideoHeight();
                        //view容器宽高比
                        showRadio = 4.0f / 3;
                        //视频源宽高比
                        scale = (float) mVideoWidth / (float) mVideoHeight;
                        if (scale >= showRadio) {
                            showVideoWidth = UIUtils.getWidthPixels((Activity) DeadPigProcessStepActivity.this) - UIUtils.dp2px(DeadPigProcessStepActivity.this, 60.0f);
                            refreshPortraitScreenAppcatWidth(showVideoWidth);
                        } else {
                            showVideoHeight = (int) ((UIUtils.getWidthPixels((Activity) DeadPigProcessStepActivity.this) - UIUtils.dp2px(DeadPigProcessStepActivity.this, 60.0f)) / showRadio);
                            refreshPortraitScreenAppcatHeight(showVideoHeight);
                        }
                    }

                    //重新刷新 竖屏显示的大小  树屏显示以宽度为准
                    public void refreshPortraitScreenAppcatWidth(int width) {

                        if (mVideoHeight > 0 && mVideoWidth > 0) {
                            //FixMe 设置surfaceview画布大小
                            mVideoHeight = (int) (width / scale);
                            vv_video.getHolder().setFixedSize(width, mVideoHeight);
                            //FixMe 重绘VideoView大小，这个方法是在重写VideoView时对外抛出方法
                            vv_video.setMeasure(width, mVideoHeight);
                            Log.d("================", width + "===============" + mVideoHeight);
                            vv_video.requestLayout();
                        }
                    }

                    //重新刷新 竖屏显示的大小  树屏显示以高度为准
                    public void refreshPortraitScreenAppcatHeight(int height) {

                        if (mVideoHeight > 0 && mVideoWidth > 0) {
                            //FixMe 设置surfaceview画布大小
                            mVideoWidth = (int) (height * scale);
                            vv_video.getHolder().setFixedSize(mVideoWidth, height);
                            //FixMe 重绘VideoView大小，这个方法是在重写VideoView时对外抛出方法
                            vv_video.setMeasure(mVideoWidth, height);
                            Log.d("================", height + "===============" + mVideoWidth);
                            vv_video.requestLayout();
                        }
                    }
                });

            }
        });

        vv_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                vv_video.seekTo(0);
                seekbar.setProgress(0);

                vv_video.stopPlayback();
                seekbar.setEnabled(false);
                play_img.setVisibility(View.VISIBLE);
                play_img.setImageResource(R.mipmap.ic_pause);
                back_img.setVisibility(View.VISIBLE);
                Log.d("", "==============setOnCompletionListener=========");
            }
        });


        vv_video.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.d("", "=====================setOnInfoListener======================" + what);
                if (what == 701) {
                    progressBar.setVisibility(View.VISIBLE);

//                    case io.vov.vitamio.MediaPlayer.MEDIA_INFO_BUFFERING_END://缓冲结束
                } else if (what == 702) {
                    progressBar.setVisibility(View.GONE);

//                    case io.vov.vitamio.MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED://当前网速
                } else if (what == 901) {
                }
                return false;
            }
        });

        play_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playOrPause(mVideoPath);
            }
        });


        rl_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideLayer();
            }
        });
        seekbar.setEnabled(false);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                vv_video.seekTo(vv_video.getDuration() * seekBar.getProgress() / 100);
                play_progress_time.setText(TimeUtil.tansTime(vv_video.getCurrentPosition()));
            }
        });
        normalScreen();
    }

    /**
     * 竖屏
     */
    public void normalScreen() {
        WindowManager.LayoutParams attr = DeadPigProcessStepActivity.this.getWindow().getAttributes();
        attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DeadPigProcessStepActivity.this.getWindow().setAttributes(attr);
        DeadPigProcessStepActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        rl_content.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        rl_content.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;

        ViewGroup.LayoutParams params = rl_content.getLayoutParams();
        params.width = DeadPigProcessStepActivity.this.getWindowManager().getDefaultDisplay().getWidth() - UIUtils.dp2px(DeadPigProcessStepActivity.this, 60.0f);
        //调节屏幕宽高比
        params.height = (int) (params.width / (float) 4.f * 3);
        rl_content.setLayoutParams(params);
    }

    /**
     * 隐藏视频控制浮层
     */
    public void showOrHideLayer() {
        if (!vv_video.isPlaying() && video_contral_layer.getVisibility() == View.VISIBLE) {
            return;
        }
        if (video_contral_layer.getVisibility() == View.VISIBLE) {
            video_contral_layer.setVisibility(View.GONE);
        } else {
            video_contral_layer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置播放进度
     */
    Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int progress = (int) (vv_video.getCurrentPosition() * 100 / vv_video.getDuration());
            if (seekbar != null && vv_video.getCurrentPosition() - mCurrentProgress >= 1000) {
                seekbar.setProgress(progress);
                mCurrentProgress = progress;
                play_progress_time.setText(TimeUtil.tansTime(vv_video.getCurrentPosition()));
            }

            sendEmptyMessage(0);
        }
    };

    /**
     * 控制播放或暂定视频
     */
    public void playOrPause(String url) {

        if (vv_video.isPlaying()) {
            vv_video.pause();
            play_img.setImageResource(R.mipmap.ic_pause);
            play_img.setVisibility(View.VISIBLE);
            progressHandler.removeMessages(0);
        } else {
            vv_video.start();
            vv_video.setVideoPath(url);
            play_img.setImageResource(R.mipmap.ic_start);
            play_img.setVisibility(View.VISIBLE);
            back_img.setVisibility(View.GONE);
            if (vv_video.getCurrentPosition() == 0) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void completeStep(int skip) {
        mProgressDialog.show();
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put("innocuousId", processStepBean.getInnocuousId());//那个订单
        mapbody.put("innocuousStepId", infoBean.getId());//步骤 id
        mapbody.put("step", String.valueOf(mCurrentStep));//第几步
        mapbody.put("skip", String.valueOf(skip));//是否跳过
        mapbody.put("address", LocationManager_new.getInstance(DeadPigProcessStepActivity.this).str_address);//地址
        if (mCurrentStep == processStepBean.getCurrentStep().getCurrentStepInfoList().size()) {
            mapbody.put("over", String.valueOf(1));//是否结束
        } else {
            mapbody.put("over", String.valueOf(0));//是否结束
        }
        if (skip == 0) {
            if (infoBean.getDealType() == DEAL_TYPE_PICTURE) {
                fileName = mImgPath.substring(mImgPath.lastIndexOf("/"));
                filePath = mImgPath;
            } else if (infoBean.getDealType() == DEAL_TYPE_VIDEO) {
                fileName = mVideoPath.substring(mVideoPath.lastIndexOf("/"));
                filePath = mVideoPath;
            }
            OkHttp3Util.uploadPreFile(Constants.DEADPIG_PROCESS_STEP_COMMIT, new File(filePath), fileName, mapbody, null, callback);
        } else {
            OkHttp3Util.doPost(Constants.DEADPIG_PROCESS_STEP_COMMIT, mapbody, null, callback);
        }
    }

    Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.dismiss();
                }
            });
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        JSONObject jsonObject = null;
                        try {
                            String string = response.body().string();
                            Log.e("deadPigProcessing", "--" + string);
                            jsonObject = new JSONObject(string);
                            int status = jsonObject.getInt("status");
                            String msg = jsonObject.getString("msg");
                            if (status == -1 || 0 == status) {
                                ToastUtils.getInstance().showShort(DeadPigProcessStepActivity.this, msg.equals("") ? "信息提交失败，请检查您的网络。" : msg);
                            } else if (status == 1) {
                                ToastUtils.getInstance().showShort(DeadPigProcessStepActivity.this, msg);
                                if (mCurrentStep != processStepBean.getCurrentStep().getCurrentStepInfoList().size()) {
                                    DeadPigProcessStepActivity.start(DeadPigProcessStepActivity.this, processStepBean, ++mCurrentStep);
                                } else {
                                    goToActivity(SelectFunctionActivity_new.class, null);
                                }
                                finish();
                            }
                        } catch (Exception e) {
                            ToastUtils.getInstance().showShort(DeadPigProcessStepActivity.this, "信息提交失败，请检查您的网络。");
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                    }
                });
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (iv_opencamera.getVisibility() == View.GONE) {
            DialogHelper.exitDeadPigProcessDialog(DeadPigProcessStepActivity.this, isCreateOrder);
        } else {
            if (!isCreateOrder) {
                goToActivity(SelectFunctionActivity_new.class, null);
            }
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearCache();
    }

    private void clearCache() {
        FileUtils.deleteFile(mVideoPath);
        FileUtils.deleteFile(mImgPath);
    }
}
