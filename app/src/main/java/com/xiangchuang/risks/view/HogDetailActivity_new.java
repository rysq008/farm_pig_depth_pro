package com.xiangchuang.risks.view;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.HogDetailAdapter;
import com.xiangchuang.risks.model.bean.HogDetailBean;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HogDetailActivity_new extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView hog_name;
    @BindView(R.id.hog_date)
    TextView hog_date;
    @BindView(R.id.hog_count)
    TextView hog_count;
    @BindView(R.id.gridview_pic)
    GridView gridview_pic;
    @BindView(R.id.iv_cancel)
    ImageView ivCancel;
    @BindView(R.id.vv_sow)
    VideoView vvSow;
    private List<String> picpaths;

    private String mSheId;
    private String pigType;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_hog_detail_new;
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
        Map mapheader = new HashMap();
        mapheader.put(Constants.AppKeyAuthorization, "hopen");
        mapheader.put(Constants.en_id, PreferencesUtils.getStringValue(Constants.en_id, HogDetailActivity_new.this));
        Map mapbody = new HashMap();
        mapbody.put("sheId", mSheId);

        OkHttp3Util.doPost(Constants.SHEDETAIL_NEW, mapbody, mapheader, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("HogDetail", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("HogDetail", string);
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
                                    hog_name.setText(name);
                                    hog_count.setText(count + "");
                                    hog_date.setText(createtime);
                                    picpaths = data.getPics();
                                    Log.i("size", picpaths.size() + "" + picpaths.toString());

                                    if(pigType.equals("102")){
                                        vvSow.setVisibility(View.VISIBLE);
                                        gridview_pic.setVisibility(View.GONE);
                                        playeVideo(picpaths.get(0));
                                    }else{
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
                } catch (JSONException e) {
                    e.printStackTrace();
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
    }
    class MyPlayerOnCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Toast.makeText(HogDetailActivity_new.this, "播放完毕", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}