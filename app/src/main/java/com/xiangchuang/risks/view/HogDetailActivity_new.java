package com.xiangchuang.risks.view;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import innovation.location.LocationManager_new;
import innovation.utils.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.HogDetailAdapter;
import com.xiangchuang.risks.model.bean.HogDetailBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HogDetailActivity_new extends BaseActivity {

    TextView hog_name;
    TextView hog_date;
    TextView hog_count;
    GridView gridview_pic;
    ImageView ivCancel;
    VideoView vvSow;
    ProgressBar mProgressBar;

    TextView tvShelon;
    TextView tvShelat;
    TextView tvSheAddress;

    LinearLayout llSheMsg;

    private List<String> picpaths;

    private String mSheId;
    private String pigType;

    private List<String> paths = new ArrayList<>();
    private int cIndex = 0;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_hog_detail_new;
    }

    @Override
    public void initView() {
        super.initView();
        hog_name = findViewById(R.id.tv_title);
        hog_date = findViewById(R.id.hog_date);
        hog_count = findViewById(R.id.hog_count);
        gridview_pic = findViewById(R.id.gridview_pic);
        ivCancel = findViewById(R.id.iv_cancel);
        vvSow = findViewById(R.id.vv_sow);
        mProgressBar = findViewById(R.id.pb_loading);

        tvShelon = findViewById(R.id.tv_shelon);
        tvShelat = findViewById(R.id.tv_shelat);
        tvSheAddress = findViewById(R.id.tv_sheaddress);
        llSheMsg = findViewById(R.id.ll_she_msg);
    }

    @Override
    protected void initData() {
        ivCancel.setVisibility(View.VISIBLE);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSheId = getIntent().getExtras().getString("sheid");
        pigType = getIntent().getStringExtra("pigtype");
        getHogMessage();


    }

    private void getHogMessage() {
        Map mapbody = new HashMap();
        mapbody.put("sheId", mSheId);
        showLoadProgressDialog();
        OkHttp3Util.doPost(Constants.SHEDETAIL_NEW, mapbody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("HogDetail", e.toString());
                AVOSCloudUtils.saveErrorMessage(e,HogDetailActivity_new.class.getSimpleName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoadDialog();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("HogDetail", string);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoadDialog();
                    }
                });
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //mProgressDialog.dismiss();
                                //showDialogError(msg);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    HogDetailBean hogDetailBean = new Gson().fromJson(string, HogDetailBean.class);
                                    HogDetailBean.DataBean data = hogDetailBean.getData();
                                    int count = data.getCount();
                                    String createtime = data.getCreatetime();
                                    String name = data.getName();
                                    String location = data.getLocation();

                                    hog_name.setText(name);
                                    hog_count.setText(count + "");
                                    hog_date.setText(createtime);
                                    picpaths = data.getPics();
                                    Log.i("size", picpaths.size() + "" + picpaths.toString());


                                    boolean isVideo = false;
                                    if(picpaths != null && picpaths.size() > 0){
                                        isVideo = picpaths.get(0).contains("mp4");
                                    }else{
                                        Toast.makeText(HogDetailActivity_new.this, "无点数详情信息。", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    if(isVideo){
                                        if(picpaths != null && picpaths.size() > 0){
                                            for (int i = 0; i <  picpaths.size(); i++){
                                                paths.add(picpaths.get(i));
                                            }
                                            vvSow.setVisibility(View.VISIBLE);
                                            mProgressBar.setVisibility(View.VISIBLE);
                                            gridview_pic.setVisibility(View.GONE);

                                            cIndex = paths.size()-1;
                                            playeVideo(paths.get(cIndex));
                                            HogDetailBean.locations locations = new Gson().fromJson(location, HogDetailBean.locations.class);

                                            llSheMsg.setVisibility(View.VISIBLE);
                                            tvShelon.setText("经度："+locations.getPigsty().get(0).getLon());
                                            tvShelat.setText("纬度："+locations.getPigsty().get(0).getLat());

                                            LocationManager_new.getInstance(HogDetailActivity_new.this).
                                                    getAddressByLatlng(
                                                            Double.parseDouble(locations.getPigsty().get(0).getLat()),
                                                            Double.parseDouble(locations.getPigsty().get(0).getLon()),
                                                            new Handler.Callback() {
                                                                @Override
                                                                public boolean handleMessage(Message msg) {
                                                                    tvSheAddress.setText("地址："+msg.obj.toString());
                                                                    return false;
                                                                }
                                                            });
                                        }else{
                                            handler.postDelayed(runnable,0);
                                            mProgressBar.setVisibility(View.GONE);
                                            Toast.makeText(HogDetailActivity_new.this, "无点数详情信息。", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        llSheMsg.setVisibility(View.GONE);
                                        vvSow.setVisibility(View.GONE);
                                        gridview_pic.setVisibility(View.VISIBLE);
                                        gridview_pic.setAdapter(new HogDetailAdapter(HogDetailActivity_new.this, picpaths));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e,HogDetailActivity_new.class.getSimpleName());
                }
            }
        });
    }


    private void playeVideo(String url){
        Uri uri = Uri.parse(url);
        //设置视频控制器
        vvSow.setMediaController(new MediaController(this));
        //播放完成回调
        vvSow.setOnCompletionListener( new MyPlayerOnCompletionListener());
        //设置视频路径
        vvSow.setVideoURI(uri);
        //开始播放视频
        vvSow.start();
        handler.postDelayed(runnable,0);
    }


    int old_duration;
    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int duration = vvSow.getCurrentPosition();
            if(vvSow.isPlaying()){
                if (old_duration == duration) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
            old_duration = duration;
            handler.postDelayed(runnable, 500);
        }
    };

    class MyPlayerOnCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            cIndex--;
            if(cIndex >= 0){
                playeVideo(paths.get(cIndex));
            }else{
                Toast.makeText(HogDetailActivity_new.this, "播放完毕", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    //关闭activity时销毁检测线程
    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }
}
