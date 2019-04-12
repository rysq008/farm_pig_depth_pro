package com.xiangchuang.risks.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


import butterknife.OnClick;

import com.innovation.pig.insurance.AppConfig;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.StartBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.CounterHelper;
import com.xiangchuang.risks.utils.PigWeightUtils;

import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.GsonUtils;
import com.innovation.pig.insurance.netutils.OkHttp3Util;

import innovation.utils.FileUtils;
import innovation.utils.MyWatcher;
import innovation.utils.PathUtils;
import innovation.view.dialog.DialogHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddPigPicActivity extends BaseActivity {


    ImageView iv_cancel;

    TextView tvTitle;

    EditText etPigAge;

    EditText etAnimalWeight;

    ImageView btnPersonAndAnimal;

    TextView tvPersonAndAnimalpath;

    ImageView btnbuchongleft;

    TextView tvbuchongleft;

    ImageView btnbuchongright;

    TextView tvbuchongright;

    Button btnCommit;

    SeekBar seekbar;

    TextView tv_adjust;

    TextView tvPrompt;

    LinearLayout ll_default;

    EditText etPigDeathTime;

    private static final int REQUESTCODE_PICK = 0;        // 相册选图标记
    public static final int REQUESTCODE_TAKE = 1;        // 相机拍照标记
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
    private int pigAge;

    File tempFile;
    //0最小值1返回称重值2最大值 3差值
    private float[] mWeightRange = new float[4];

    //记录称重接口不能识别的次数
    private int failureTime = 0;

    @Override
    public void initView() {
        super.initView();
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        etPigAge = (EditText) findViewById(R.id.etPigAge);
        etAnimalWeight = (EditText) findViewById(R.id.etAnimalWeight);
        btnPersonAndAnimal = (ImageView) findViewById(R.id.btnPersonAndAnimal);
        tvPersonAndAnimalpath = (TextView) findViewById(R.id.tvPersonAndAnimalpath);
        btnbuchongleft = (ImageView) findViewById(R.id.btnbuchongleft);
        tvbuchongleft = (TextView) findViewById(R.id.tvbuchongleft);
        btnbuchongright = (ImageView) findViewById(R.id.btnbuchongright);
        tvbuchongright = (TextView) findViewById(R.id.tvbuchongright);
        btnCommit = (Button) findViewById(R.id.btnCommit);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        tv_adjust = (TextView) findViewById(R.id.tv_adjust);
        tvPrompt = (TextView) findViewById(R.id.tv_prompt);
        ll_default = (LinearLayout) findViewById(R.id.ll_default);
        etPigDeathTime = (EditText) findViewById(R.id.etPigDeathTime);
        findViewById(R.id.etPigDeathTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.btnCommit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.btnbuchongright).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.btnbuchongleft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.ll_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.btnPersonAndAnimal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
    }

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

        if (AppConfig.isApkInDebug()) {
            Toast.makeText(this, "lipeiId=" + lipeiId + "---timesFlag=" + timesFlag, Toast.LENGTH_SHORT).show();
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
                if (TextUtils.isEmpty(etPigAge.getText())) {
                    Toast.makeText(getApplicationContext(), "请先填写畜龄。", Toast.LENGTH_SHORT).show();
                } else {
                    pigAge = Integer.parseInt(etPigAge.getText().toString().trim());
                    WeightPicCollectActivity.start(AddPigPicActivity.this);
//                Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                //下面这句指定调用相机拍照后的照片存储的路径
//                takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", tempFile));
//                startActivityForResult(takeIntent, REQUESTCODE_TAKE);
                }
                pop.dismiss();
                llPopup.clearAnimation();

            }
        });
        //相册
//        bt2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClickView(View v) {
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
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

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

        etPigAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 4) {
                    s.delete(4, 5);
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case REQUESTCODE_TAKE:
                if (data == null || TextUtils.isEmpty(data.getStringExtra("path"))) {
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
            default:
                break;
        }

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
                        AVOSCloudUtils.saveErrorMessage(e, AddPigPicActivity.class.getSimpleName());
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
                                                btnPersonAndAnimal.setVisibility(View.VISIBLE);
                                                btnPersonAndAnimal.setImageDrawable(drawable);
                                                ll_default.setVisibility(View.GONE);
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
                                AVOSCloudUtils.saveErrorMessage(e, AddPigPicActivity.class.getSimpleName());
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
                            tvPrompt.setVisibility(View.VISIBLE);
                            mProgressDialog.dismiss();

                            autoWeight = weight + "";

//                            float currentWeight = PigWeightUtils.correctWeight(pigAge, weight);

                            etAnimalWeight.setText(weight + "");
                            seekbar.setVisibility(View.VISIBLE);
                            tv_adjust.setVisibility(View.VISIBLE);
                            mWeightRange[1] = weight;
                            mWeightRange[0] = (float) Math.floor(weight * 0.9);
                            mWeightRange[2] = (float) Math.floor(weight * 1.1);
                            mWeightRange[3] = mWeightRange[1] - mWeightRange[0];

                            seekbar.setProgress(10);
                        } else {
                            tvPrompt.setVisibility(View.GONE);

                            etAnimalWeight.setText("");
                            seekbar.setVisibility(View.GONE);
                            tv_adjust.setVisibility(View.GONE);
                            mProgressDialog.dismiss();

                            if (failureTime > 1) {
                                autoWeight = weight + "";

                                float currentWeight = PigWeightUtils.correctWeight(pigAge, 0);
                                etAnimalWeight.setText(currentWeight + "");
                                seekbar.setVisibility(View.VISIBLE);
                                tv_adjust.setVisibility(View.VISIBLE);
                                mWeightRange[1] = currentWeight;
                                mWeightRange[0] = (float) Math.floor(currentWeight * 0.9);
                                mWeightRange[2] = (float) Math.floor(currentWeight * 1.1);
                                mWeightRange[3] = mWeightRange[1] - mWeightRange[0];

                                seekbar.setProgress(10);
                                DialogHelper.weightCheckFailureDialog(AddPigPicActivity.this);
                            } else {

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



    public void onClickView(View view) {
        int i = view.getId();
        if (i == R.id.btnPersonAndAnimal || i == R.id.ll_default) {
            picType = 0;
//                llPopup.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.activity_translate_in));
//                pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);

            if (TextUtils.isEmpty(etPigAge.getText())) {
                Toast.makeText(getApplicationContext(), "请先填写畜龄。", Toast.LENGTH_SHORT).show();
            } else {
                pigAge = Integer.parseInt(etPigAge.getText().toString().trim());
                WeightPicCollectActivity.start(AddPigPicActivity.this);
//                Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                //下面这句指定调用相机拍照后的照片存储的路径
//                takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", tempFile));
//                startActivityForResult(takeIntent, REQUESTCODE_TAKE);
            }

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
        } else if (i == R.id.btnCommit) {
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

        } else if (i == R.id.iv_cancel) {
            finish();

        } else if (i == R.id.etPigDeathTime) {
            showDatePickerDialog();

        } else {
        }
    }

    private void addPayInfo() {
        if (TextUtils.isEmpty(etPigAge.getText())) {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "畜龄不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(etPigAge.getText().toString());
        if (age <= 0 || age > 2000) {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "畜龄超出范围", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etPigDeathTime.getText())) {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "未填写死猪死亡时间", Toast.LENGTH_SHORT).show();
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
        mapbody.put("deathTime", etPigDeathTime.getText().toString().trim());
        mapbody.put("deadPics", "");
        mapbody.put("provePic", "");//无害化证明照片
        mapbody.put("autoWeight", autoWeight);//自动识别返回重量
        mapbody.put("timesFlag", timesFlag);//强制提交信息

        Log.e("mMapbody", "mMapbody: " + mapbody.toString());

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
                AVOSCloudUtils.saveErrorMessage(e, AddPigPicActivity.class.getSimpleName());
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
                                AVOSCloudUtils.saveErrorMessage(e, AddPigPicActivity.class.getSimpleName());
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


    public void showDatePickerDialog() {
        SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd");//获取日期格式器对象
        Calendar calendar = Calendar.getInstance(Locale.CHINA);//获取日期格式器对象
        //生成一个DatePickerDialog对象，并显示。显示的DatePickerDialog控件可以选择年月日，并设置
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddPigPicActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //修改日历控件的年，月，日
                //这里的year,monthOfYear,dayOfMonth的值与DatePickerDialog控件设置的最新值一致
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                etPigDeathTime.setText(format.format(calendar.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMaxDate(Calendar.getInstance().getTime().getTime());// 最大日期
        datePickerDialog.show();
    }
}
