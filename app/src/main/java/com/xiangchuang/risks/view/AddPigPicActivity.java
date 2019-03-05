package com.xiangchuang.risks.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.*;

import butterknife.BindView;
import butterknife.OnClick;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.StartBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.CounterHelper;
import com.xiangchuangtec.luolu.animalcounter.BuildConfig;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;

import innovation.utils.FileUtils;
import innovation.utils.MyWatcher;
import innovation.utils.PathUtils;
import innovation.view.dialog.DialogHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddPigPicActivity extends BaseActivity {

    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.etPigAge)
    EditText etPigAge;
    @BindView(R.id.etAnimalWeight)
    EditText etAnimalWeight;
    @BindView(R.id.btnPersonAndAnimal)
    ImageView btnPersonAndAnimal;
    @BindView(R.id.tvPersonAndAnimalpath)
    TextView tvPersonAndAnimalpath;
    @BindView(R.id.btnbuchongleft)
    ImageView btnbuchongleft;
    @BindView(R.id.tvbuchongleft)
    TextView tvbuchongleft;
    @BindView(R.id.btnbuchongright)
    ImageView btnbuchongright;
    @BindView(R.id.tvbuchongright)
    TextView tvbuchongright;
    @BindView(R.id.btnCommit)
    Button btnCommit;
    @BindView(R.id.seekbar)
    SeekBar seekbar;
    @BindView(R.id.tv_adjust)
    TextView tv_adjust;

    private static final int REQUESTCODE_PICK = 0;        // 相册选图标记
    private static final int REQUESTCODE_TAKE = 1;        // 相机拍照标记
    private static final int REQUESTCODE_CUTTING = 2;    // 图片裁切标记

    private String lipeiId = "";
    private String timesFlag = "";

    private PopupWindow pop = null;
    private LinearLayout llPopup;

    private int picType = 0;
    private View parentView;

    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 10000;
    private static long lastClickTime;

    private String autoWeight;

    File tempFile;
    //0最小值1返回称重值2最大值 3差值
    private float[] mWeightRange = new float[4];

    private static int failureTime = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_pig_pic;
    }

    @Override
    protected void initData() {
        lipeiId = getIntent().getStringExtra("lipeiid");
        timesFlag = getIntent().getStringExtra("timesFlag");
        tvTitle.setText("资料采集");
        iv_cancel.setVisibility(View.GONE);
        parentView = getWindow().getDecorView();
        etAnimalWeight.addTextChangedListener(new MyWatcher(3, 1));

        if (BuildConfig.DEBUG){
            Toast.makeText(this, "lipeiId="+lipeiId+"---timesFlag="+timesFlag, Toast.LENGTH_SHORT).show();
        }


        //选择图片
        pop = new PopupWindow(getApplicationContext());
        View view = getLayoutInflater().inflate(R.layout.item_popupwindows, null);
        llPopup = (LinearLayout) view.findViewById(R.id.ll_popup);
        view.findViewById(R.id.ll_item_popupwindows_Photo).setVisibility(View.GONE);
        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);

        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent);
        //相机
        Button bt1 = view.findViewById(R.id.item_popupwindows_camera);
        //相册
        Button bt2 = view.findViewById(R.id.item_popupwindows_Photo);
        //取消
        Button bt3 = view.findViewById(R.id.item_popupwindows_cancel);

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                llPopup.clearAnimation();
            }
        });
        //相机
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeightPicCollectActivity.start(AddPigPicActivity.this);
//                Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                //下面这句指定调用相机拍照后的照片存储的路径
//                takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", tempFile));
//                startActivityForResult(takeIntent, REQUESTCODE_TAKE);
                pop.dismiss();
                llPopup.clearAnimation();
            }
        });
        //相册
//        bt2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 如果朋友们要限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
//                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
//                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpeg");
//                startActivityForResult(pickIntent, REQUESTCODE_PICK);
//                overridePendingTransition(R.anim.activity_translate_in, R.anim.activity_translate_out);
//                pop.dismiss();
//                llPopup.clearAnimation();
//            }
//        });
        //取消
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                llPopup.clearAnimation();
            }
        });

        seekbar.setMax(20);
        seekbar.getThumb().setColorFilter(Color.parseColor("#00adff"), PorterDuff.Mode.SRC_ATOP);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float currentValues = mWeightRange[1] + (seekBar.getProgress() - 10.0f) / 10.0f * mWeightRange[3];
                if (currentValues < mWeightRange[0]) currentValues = mWeightRange[0];
                if (currentValues > mWeightRange[2]) currentValues = mWeightRange[2];
                etAnimalWeight.setText(String.valueOf(currentValues));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            // 直接从相册获取
//            case REQUESTCODE_PICK:
//                try {
////                    startPhotoZoom(data.getData());
//                } catch (NullPointerException e) {
//                    // 用户点击取消操作
//                    e.printStackTrace();
//                }
//                break;
            // 调用相机拍照
            case REQUESTCODE_TAKE:
                if (TextUtils.isEmpty(data.getStringExtra("path"))) {
                    return;
                }
                tempFile = new File(data.getStringExtra("path"));
                if (tempFile.exists()) {
                    try {
                        setPicToView(tempFile);
                    } catch (Exception e) {
                        //Toast.makeText(AddPigPicActivity.this, "图片处理异常，请重试。", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                break;
            // 取得裁剪后的图片
//            case REQUESTCODE_CUTTING:
//                if (data != null) {
//                    try {
////                        setPicToView(temp);
//                    } catch (Exception e) {
//                        Toast.makeText(AddPigPicActivity.this, "图片处理异常，请重试。", Toast.LENGTH_SHORT).show();
//                        e.printStackTrace();
//                    }
//                } else {
//                    Toast.makeText(AddPigPicActivity.this, "图片处理异常，请重试。", Toast.LENGTH_SHORT).show();
//                }
//                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪相册图片方法实现
     *
     * @param uri
     */
//    public void startPhotoZoom(Uri uri) {
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(uri, "image/jpeg");
//        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
//        intent.putExtra("crop", "true");
//        /*if (picType == 0) {
//            // aspectX aspectY 是宽高的比例
//            intent.putExtra("aspectX", 1);
//            intent.putExtra("aspectY", 1);
//            // outputX outputY 是裁剪图片宽高
//            intent.putExtra("outputX", 300);
//            intent.putExtra("outputY", 300);
//        }*/
////        intent.putExtra("return-data", true);
//        //裁剪后的图片Uri路径，uritempFile为Uri类变量
//        uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//        startActivityForResult(intent, REQUESTCODE_CUTTING);
//    }

    /**
     * 裁剪相机图片方法实现
     */
//    public void crop(String imagePath) {
//        try {
//            Intent intent = new Intent("com.android.camera.action.CROP");
//            intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/jpeg");
//            intent.putExtra("crop", "true");
//            /*if (picType == 0) {
//                // aspectX aspectY 是宽高的比例
//                intent.putExtra("aspectX", 1);
//                intent.putExtra("aspectY", 1);
//                intent.putExtra("outputX", 300);
//                intent.putExtra("outputY", 300);
//            }*/
//            intent.putExtra("scale", true);
//            intent.putExtra("return-data", false);
//            uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
//            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//            startActivityForResult(intent, REQUESTCODE_CUTTING);
//        } catch (ActivityNotFoundException anfe) {
//            //display an error message
//            String errorMessage = "你的设备不支持图片裁剪，请更换其他设备重试。";
//            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
//            toast.show();
//        }
//    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param file
     */
    private void setPicToView(File file) throws Exception {
//        // 取得SDCard图片路径做显示
//        Bitmap photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uritempFile));
        Bitmap photo = BitmapFactory.decodeFile(file.getAbsolutePath());
        picToView(photo);
    }

    /**
     * 将照片显示在view上，并调用上传
     *
     * @param photo
     */
    private void picToView(Bitmap photo) {
//        String urlpath = FileUtils.saveFile(getApplicationContext(), "temphead.jpg", photo);
//        File file = new File(urlpath);

        showProgressDialog();
        // 上传图片文件
        upLoadImg(tempFile, photo);
    }

    //上传照片
    private void upLoadImg(File fileImage, Bitmap photo) {
        Log.e("upLoadImg", "path=" + fileImage.getAbsolutePath());
        OkHttp3Util.uploadPreFile(Constants.UP_LOAD_IMG, fileImage, "a.jpg", null, null, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("upLoadImg", e.getLocalizedMessage());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                Toast.makeText(AddPigPicActivity.this, "图片上传失败，请检查您的网络。", Toast.LENGTH_SHORT).show();
                            }
                        });
                        AVOSCloudUtils.saveErrorMessage(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String s = response.body().string();
                            Log.e("upLoadImg", "上传--" + s);
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(s);
                                int status = jsonObject.getInt("status");
                                String msg = jsonObject.getString("msg");
                                String data = jsonObject.getString("data");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (status == -1 || 0 == status) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(AddPigPicActivity.this, "图片上传失败，请检查您的网络。", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Drawable drawable = new BitmapDrawable(null, photo);
                                            if (picType == 0) {
                                                btnPersonAndAnimal.setImageDrawable(drawable);
                                            } else if (picType == 1) {
                                                btnbuchongleft.setImageDrawable(drawable);
                                            } else {
                                                btnbuchongright.setImageDrawable(drawable);
                                            }
                                            Toast.makeText(AddPigPicActivity.this, msg, Toast.LENGTH_SHORT).show();
                                            if (picType == 0) {
                                                upDeadPig(photo);
                                                tvPersonAndAnimalpath.setText(data);
                                            } else if (picType == 1) {
                                                mProgressDialog.dismiss();
                                                tvbuchongleft.setText(data);
                                            } else {
                                                mProgressDialog.dismiss();
                                                tvbuchongright.setText(data);
                                            }
                                            boolean result = FileUtils.deleteFile(fileImage);
                                            if (result) {
                                                Log.i("lipeidetete:", "本地图片打包文件删除成功！！");
                                            }
                                        }
                                    }
                                });
                                FileUtils.deleteFile(PathUtils.weightcollect);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(AddPigPicActivity.this, "图片上传失败，请检查您的网络。", Toast.LENGTH_SHORT).show();
                                AVOSCloudUtils.saveErrorMessage(e);
                            }
                        }
                    }
                }
        );
    }

    /**
     * 上传死猪图片给后台模型进行称重识别
     *
     * @param photo
     */
    private void upDeadPig(Bitmap photo) {
        //上传死猪照片时候调用称重接口
        CounterHelper.recognitionWeightFromNet(photo, new CounterHelper.OnImageRecognitionWeightListener() {
            @Override
            public void onCompleted(float weight, int status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status == 1) {
                            mProgressDialog.dismiss();
                            autoWeight = weight + "";

                            etAnimalWeight.setText(weight + "");
                            seekbar.setVisibility(View.VISIBLE);
                            tv_adjust.setVisibility(View.VISIBLE);
                            mWeightRange[1] = weight;
                            mWeightRange[0] = (float) Math.floor(weight * 0.9);
                            mWeightRange[2] = (float) Math.floor(weight * 1.1);
                            mWeightRange[3] = mWeightRange[1] - mWeightRange[0];

                            seekbar.setProgress(10);
                        } else {
                            etAnimalWeight.setText("");
                            seekbar.setVisibility(View.GONE);
                            tv_adjust.setVisibility(View.GONE);
                            mProgressDialog.dismiss();
                            if(failureTime > 1){
                                autoWeight = "0";
                                etAnimalWeight.setEnabled(true);
                                DialogHelper.weightCheckFailureDialog(AddPigPicActivity.this);
                            }else{
                                autoWeight = "";
                                DialogHelper.weightCheckDialog1(AddPigPicActivity.this);
                                failureTime += 1;
                            }
//                            Toast.makeText(AddPigPicActivity.this, "识别失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

//    public Uri getImageContentUri(File imageFile) {
//        String filePath = imageFile.getAbsolutePath();
//        Cursor cursor = getContentResolver().query(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                new String[]{MediaStore.Images.Media._ID},
//                MediaStore.Images.Media.DATA + "=? ",
//                new String[]{filePath}, null);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            int id = cursor.getInt(cursor
//                    .getColumnIndex(MediaStore.MediaColumns._ID));
//            Uri baseUri = Uri.parse("content://media/external/images/media");
//            return Uri.withAppendedPath(baseUri, "" + id);
//        } else {
//            if (imageFile.exists()) {
//                ContentValues values = new ContentValues();
//                values.put(MediaStore.Images.Media.DATA, filePath);
//                return getContentResolver().insert(
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//            } else {
//                return null;
//            }
//        }
//    }


//    public String stampToDate(long timeMillis) {
//        @SuppressLint("SimpleDateFormat")
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//        Date date = new Date(timeMillis);
//        return simpleDateFormat.format(date);
//    }


    @OnClick({R.id.btnPersonAndAnimal, R.id.btnbuchongleft, R.id.btnbuchongright, R.id.btnCommit, R.id.iv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPersonAndAnimal:
                picType = 0;
//                llPopup.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.activity_translate_in));
//                pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
                WeightPicCollectActivity.start(AddPigPicActivity.this);
                break;
//            case R.id.btnbuchongleft:
//                picType = 1;
//                llPopup.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.activity_translate_in));
//                pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
//                break;
//            case R.id.btnbuchongright:
//                picType = 2;
//                llPopup.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.activity_translate_in));
//                pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
//                break;
            case R.id.btnCommit:
                if (SystemClock.elapsedRealtime() - lastClickTime < MIN_CLICK_DELAY_TIME) {
                    Toast.makeText(AddPigPicActivity.this, "正在处理，请勿连续多次点击！", Toast.LENGTH_SHORT).show();
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
//                String buchongLeftstr = tvbuchongleft.getText().toString();
//                String buchongRightstr = tvbuchongright.getText().toString();

                if (TextUtils.isEmpty(tvPersonAndAnimalpath.getText())) {
                    Toast.makeText(getApplicationContext(), "请先拍摄死猪照片。", Toast.LENGTH_SHORT).show();
                    return;
                }
                addPayInfo();
                break;
            case R.id.iv_cancel:
                finish();
                break;
            default:
                break;
        }
    }

    private void addPayInfo() {
        if (TextUtils.isEmpty(etPigAge.getText())) {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "畜龄不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(etPigAge.getText().toString());
        if(age <= 0 || age > 10000 ){
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "畜龄超出范围", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etAnimalWeight.getText())) {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "未填写死猪重量", Toast.LENGTH_SHORT).show();
            return;
        }



//        String buchongLeftstr = tvbuchongleft.getText().toString();
//        String buchongRightstr = tvbuchongright.getText().toString();
//        StringBuilder sb = new StringBuilder();
//
//        if (!buchongLeftstr.equals("")) {
//            sb.append(buchongLeftstr);
//            sb.append(",");
//        }
//
//        if (!buchongRightstr.equals("")) {
//            sb.append(buchongRightstr);
//            sb.append(",");
//        }
//
//        if (sb.length() > 0) {
//            sb.deleteCharAt(sb.length() - 1);
//        }
        showProgressDialog();
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put(Constants.lipeiId, lipeiId);
        mapbody.put("age", etPigAge.getText().toString().trim());
        mapbody.put("weight", etAnimalWeight.getText().toString().trim());
        mapbody.put("weightPic", tvPersonAndAnimalpath.getText().toString().trim());
        mapbody.put("deadPics", "");
        mapbody.put("provePic", "");//无害化证明照片
        mapbody.put("autoWeight", autoWeight);//自动识别返回重量
        mapbody.put("timesFlag", timesFlag);//强制提交信息

        Log.e("mapbody", "mapbody: " + mapbody.toString());

        OkHttp3Util.doPost(Constants.ADD_PAY_INFO, mapbody, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("pregoon", e.getLocalizedMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        Toast.makeText(AddPigPicActivity.this, "信息提交失败，请检查您的网络。", Toast.LENGTH_SHORT).show();
                    }
                });
                AVOSCloudUtils.saveErrorMessage(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject jsonObject = null;
                            try {
                                String s = response.body().string();
                                Log.e("goonPre", "--" + s);
                                jsonObject = new JSONObject(s);
                                int status = jsonObject.getInt("status");
                                String msg = jsonObject.getString("msg");
                                if (status == -1 || 0 == status) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(AddPigPicActivity.this, msg.equals("") ? "信息提交失败，请检查您的网络。" : msg, Toast.LENGTH_SHORT).show();
                                } else {
                                    mProgressDialog.dismiss();
                                    StartBean bean = GsonUtils.getBean(s, StartBean.class);
                                    if (1 == bean.getStatus()) {
                                        Toast.makeText(AddPigPicActivity.this, msg, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            } catch (Exception e) {
                                mProgressDialog.dismiss();
                                Toast.makeText(AddPigPicActivity.this, "信息提交失败，请检查您的网络。", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                                AVOSCloudUtils.saveErrorMessage(e);
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            showMessageDialogRetry("上传异常，请重试。");
                        }
                    });
                }
            }
        });
    }

    // 预理赔/理赔弹框
    private void showMessageDialogRetry(String msg) {

        AlertDialogManager.showMessageDialogRetry(AddPigPicActivity.this, "提示", msg, new AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {
                addPayInfo();
            }

            @Override
            public void onNegative() {
                finish();
            }
        });
    }

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(AddPigPicActivity.this);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIcon(R.drawable.cowface);
        mProgressDialog.setMessage("开始处理......");
        mProgressDialog.show();
        Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
        if (positive != null) {
            positive.setVisibility(View.GONE);
        }
        Button negative = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
        if (negative != null) {
            negative.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        DialogHelper.exitCheckDialog(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        failureTime = 0;
    }
}
