package com.xiangchuang.risks.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.bean.StartBean;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuang.risks.utils.CounterHelper;
import com.xiangchuangtec.luolu.animalcounter.BuildConfig;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import innovation.utils.FileUtils;
import innovation.utils.MyWatcher;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddPigPicActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.etAnimalAge)
    EditText etAnimalAge;
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
    @BindView(R.id.iv_cancel)
    ImageView ivCancel;

    private String lipeiId = "";

    private PopupWindow pop = null;
    private LinearLayout llPopup;
    private static final int REQUESTCODE_PICK = 0;        // 相册选图标记
    private static final int REQUESTCODE_TAKE = 1;        // 相机拍照标记
    private static final int REQUESTCODE_CUTTING = 2;    // 图片裁切标记

    private static String IMAGE_FILE_NAME = "";// 头像文件名称
    private Uri uritempFile;

    private int picType = 0;
    private View parentView;

    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 10000;
    private static long lastClickTime;

    private String autoWeight;

    File tempFile;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_pig_pic;
    }

    @Override
    protected void initData() {
        tvTitle.setText("资料采集");
        parentView = getWindow().getDecorView();
        lipeiId = getIntent().getStringExtra("lipeiid");
        etAnimalAge.addTextChangedListener(new MyWatcher(3, 1));

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

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        IMAGE_FILE_NAME = stampToDate(System.currentTimeMillis()) + ".jpg";
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
                //  Auto-generated method stub
                pop.dismiss();
                llPopup.clearAnimation();
            }
        });
        //相机
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempFile = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
                Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //下面这句指定调用相机拍照后的照片存储的路径
                takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", tempFile));
                startActivityForResult(takeIntent, REQUESTCODE_TAKE);
                pop.dismiss();
                llPopup.clearAnimation();
            }
        });
        //相册
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                // 如果朋友们要限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpeg");
                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                overridePendingTransition(R.anim.activity_translate_in, R.anim.activity_translate_out);
                pop.dismiss();
                llPopup.clearAnimation();
            }
        });
        //取消
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                llPopup.clearAnimation();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            // 直接从相册获取
            case REQUESTCODE_PICK:
                try {
                    startPhotoZoom(data.getData());
                } catch (NullPointerException e) {
                    // 用户点击取消操作
                    e.printStackTrace();
                }
                break;
            // 调用相机拍照
            case REQUESTCODE_TAKE:
//                File temp = new File(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
//                crop(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
                if(tempFile.exists()){
                    try {
                        setPicToView(tempFile);
                    } catch (Exception e) {
                        //Toast.makeText(AddPigPicActivity.this, "图片处理异常，请重试。", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }


                break;
            // 取得裁剪后的图片
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    try {
//                        setPicToView(temp);
                    } catch (Exception e) {
                        Toast.makeText(AddPigPicActivity.this, "图片处理异常，请重试。", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(AddPigPicActivity.this, "图片处理异常，请重试。", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪相册图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/jpeg");
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        /*if (picType == 0) {
            // aspectX aspectY 是宽高的比例
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            // outputX outputY 是裁剪图片宽高
            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 300);
        }*/
//        intent.putExtra("return-data", true);
        //裁剪后的图片Uri路径，uritempFile为Uri类变量
        uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * 裁剪相机图片方法实现
     */
    public void crop(String imagePath) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/jpeg");
            intent.putExtra("crop", "true");
            /*if (picType == 0) {
                // aspectX aspectY 是宽高的比例
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 300);
                intent.putExtra("outputY", 300);
            }*/
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            startActivityForResult(intent, REQUESTCODE_CUTTING);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "你的设备不支持图片裁剪，请更换其他设备重试。";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * 保存裁剪之后的图片数据
     * @param file
     */
    private void setPicToView(File file) throws Exception {
//        // 取得SDCard图片路径做显示
//        Bitmap photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uritempFile));
        Bitmap photo = BitmapFactory.decodeFile(file.getAbsolutePath());
        picToView(photo);
    }

    /**
     * 上传死猪图片给后台模型进行称重识别
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
                            etAnimalAge.setText(weight + "");
                        } else {
                            mProgressDialog.dismiss();
                            autoWeight = "";
                            Toast.makeText(AddPigPicActivity.this, "识别失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }


        });
    }

    /**
     * 将照片显示在view上，并调用上传
     * @param photo
     */
    private void picToView(Bitmap photo) {
        Drawable drawable = new BitmapDrawable(null, photo);
        String urlpath = FileUtils.saveFile(getApplicationContext(), "temphead.jpg", photo);

        if (picType == 0) {
            btnPersonAndAnimal.setImageDrawable(drawable);
        } else if (picType == 1) {
            btnbuchongleft.setImageDrawable(drawable);
        } else {
            btnbuchongright.setImageDrawable(drawable);
        }

        File file = new File(urlpath);

        showProgressDialog();
        // 上传图片文件
        upLoadImg(file, photo);
    }

    //上传照片
    private void upLoadImg(File fileImage, Bitmap photo) {
        Log.e("upLoadImg", "path=" + fileImage.getAbsolutePath());
        OkHttp3Util.uploadPreFile(Constants.UP_LOAD_IMG, fileImage, "a.jpg", null, null, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("upLoadImg", e.getLocalizedMessage());
                        mProgressDialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddPigPicActivity.this, "图片上传失败，请检查您的网络。", Toast.LENGTH_SHORT).show();
                            }
                        });
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
                                            Toast.makeText(AddPigPicActivity.this, msg, Toast.LENGTH_SHORT).show();
                                            if (picType == 0) {
                                                tvPersonAndAnimalpath.setText(data);
                                                upDeadPig(photo);
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(AddPigPicActivity.this, "图片上传失败，请检查您的网络。", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    public String stampToDate(long timeMillis) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }


    @OnClick({R.id.btnPersonAndAnimal, R.id.btnbuchongleft, R.id.btnbuchongright, R.id.btnCommit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPersonAndAnimal:
                picType = 0;
                llPopup.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.activity_translate_in));
                pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.btnbuchongleft:
                picType = 1;
                llPopup.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.activity_translate_in));
                pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.btnbuchongright:
                picType = 2;
                llPopup.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.activity_translate_in));
                pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.btnCommit:
                if (SystemClock.elapsedRealtime() - lastClickTime < MIN_CLICK_DELAY_TIME) {
                    Toast.makeText(AddPigPicActivity.this, "正在处理，请勿连续多次点击！", Toast.LENGTH_SHORT).show();
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                String stringPersonAndAnimalpath = tvPersonAndAnimalpath.getText().toString();
//                String buchongLeftstr = tvbuchongleft.getText().toString();
//                String buchongRightstr = tvbuchongright.getText().toString();

                if (stringPersonAndAnimalpath.equals("")) {
                    Toast.makeText(getApplicationContext(), "请先拍摄死猪照片。", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressDialog();
                addPayInfo();

                break;

            default:
                break;
        }
    }

    private void addPayInfo() {
        if(etAnimalAge.getText().toString().trim().isEmpty()){
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "未填写死猪重量", Toast.LENGTH_SHORT).show();
            return;
        }

        String buchongLeftstr = tvbuchongleft.getText().toString();
        String buchongRightstr = tvbuchongright.getText().toString();
        StringBuilder sb = new StringBuilder();

        if (!buchongLeftstr.equals("")) {
            sb.append(buchongLeftstr);
            sb.append(",");
        }

        if (!buchongRightstr.equals("")) {
            sb.append(buchongRightstr);
            sb.append(",");
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        Map<String, String> mapbody = new HashMap<>();
        mapbody.put(Constants.lipeiId, lipeiId);
        mapbody.put("weight", etAnimalAge.getText().toString().trim());
        mapbody.put("weightPic", tvPersonAndAnimalpath.getText().toString().trim());
        mapbody.put("deadPics", sb.toString());
        mapbody.put("provePic", "");//无害化证明照片
        mapbody.put("autoWeight", autoWeight);//自动识别返回重量

        Log.e("mapbody", "mapbody: "+mapbody.toString() );

        OkHttp3Util.doPost(Constants.ADD_PAY_INFO, mapbody, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("pregoon", e.getLocalizedMessage());
                mProgressDialog.dismiss();
                Toast.makeText(AddPigPicActivity.this, "信息提交失败，请检查您的网络。", Toast.LENGTH_SHORT).show();
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
                            }
                        }
                    });
                }else{
                    mProgressDialog.dismiss();
                    showMessageDialogRetry("上传异常，请重试。");
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


}
