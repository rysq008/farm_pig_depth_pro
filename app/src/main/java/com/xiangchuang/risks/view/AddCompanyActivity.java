package com.xiangchuang.risks.view;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bumptech.glide.Glide;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.xiangchuang.risks.base.BaseBarActivity;
import com.xiangchuang.risks.model.bean.CompanyInfoBean;
import com.xiangchuang.risks.utils.IDCard;
import com.xiangchuangtec.luolu.animalcounter.BuildConfig;
import com.xiangchuangtec.luolu.animalcounter.MyApplication;
import com.xiangchuangtec.luolu.animalcounter.R;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;
import com.xiangchuangtec.luolu.animalcounter.netutils.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import innovation.login.IDCardValidate;
import innovation.utils.FileUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xiangchuang.risks.utils.MyTextUtil.isEmojiCharacter;
import static com.xiangchuang.risks.utils.ValidatorUtils.isLicense;
import static com.xiangchuang.risks.utils.ValidatorUtils.isMobileNO;
import static com.xiangchuang.risks.utils.ValidatorUtils.isPhone;

public class AddCompanyActivity extends BaseBarActivity implements View.OnClickListener {
    //证件类型
    private int certificateType = 1;
    private String imageType;
    private static String IMAGE_FILE_NAME = "";// 头像文件名称
    private static final int REQUESTCODE_TAKE = 11111;        // 相机拍照标记
    private static final int REQUESTCODE_CUTTING = 22222;    // 图片裁切标记
    private Uri uritempFile;
    public static String TAG = "AddCompanyActivity";
    private AMapLocationClient mLocationClient;

    private String str_idcard_zheng = "";
    private String str_idcard_fan = "";
    private String str_bank = "";
    private RadioGroup certificateTypeRadioGroup;
    ConstraintLayout idCardNegativePhotoConstraintLayout;
    TextView tvIdPositive;
    ImageView btnIdcardZhengUpload;
    ImageView btnIdcardFanUpload;
    ImageView btnBankUpload;
    Button btnwancheng;
    EditText tvBaodanAddress;
    EditText tvBaodanPeople;
    EditText tv_qiyename;
    EditText tvBaodanIdcard;
    EditText tv_baodan_tel;
    EditText tvBaodanOpenbank;
    EditText tvBaodanBankNum;
    TextView qiyezhanghu;
    EditText qiyepassword;
    private boolean type;//判断进入方式，true列表进入，false新建
    File tempFile;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (type) {
            getDataInfo();
        }
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        type = bundle.getBoolean("type");
        Log.i("addcompanyactivity", type + "!!!!!!!!!!!!!!!");

        XXPermissions.with(this)
                //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                //.permission(Permission.SYSTEM_ALERT_WINDOW, Permission.REQUEST_INSTALL_PACKAGES) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.Group.STORAGE, Permission.Group.CALENDAR, Permission.Group.LOCATION) //不指定权限则自动获取清单中的危险权限
                .permission(Permission.CAMERA)
                .request(new OnPermission() {

                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            // toastUtils.showLong(MyApplication.getAppContext(), "获取权限成功");
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            Toast.makeText(MyApplication.getAppContext(), "被永久拒绝授权，请手动授予权限", Toast.LENGTH_SHORT).show();
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(MyApplication.getAppContext());
                        } else {
                            Toast.makeText(MyApplication.getAppContext(), "获取权限失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        IMAGE_FILE_NAME = stampToDate(System.currentTimeMillis()) + ".jpg";
        getCurrentLocationLatLng();
        certificateTypeRadioGroup = findViewById(R.id.certificate_type_radioGroup);
        idCardNegativePhotoConstraintLayout = findViewById(R.id.id_card_negative_photo_constraint_layout);
        tvIdPositive = findViewById(R.id.tv_id_positive);
        btnwancheng = findViewById(R.id.btn_wancheng);

        tvBaodanAddress = findViewById(R.id.tv_baodan_address);
        tvBaodanPeople = findViewById(R.id.tv_baodan_people);
        tv_qiyename = findViewById(R.id.tv_qiyename);

        tvBaodanIdcard = findViewById(R.id.tv_baodan_idcard);
        tv_baodan_tel = findViewById(R.id.tv_baodan_tel);
        tvBaodanOpenbank = findViewById(R.id.tv_baodan_openbank);

        tvBaodanBankNum = findViewById(R.id.tv_baodan_bank_num);
        qiyezhanghu = findViewById(R.id.qiyezhanghu);
        qiyepassword = findViewById(R.id.qiyepassword);

        btnIdcardZhengUpload = findViewById(R.id.btn_idcard_zheng_upload);
        btnIdcardFanUpload = findViewById(R.id.btn_idcard_fan_upload);
        btnBankUpload = findViewById(R.id.btn_bank_upload);

        btnIdcardZhengUpload.setOnClickListener(this);
        btnwancheng.setOnClickListener(this);
        btnBankUpload.setOnClickListener(this);
        btnIdcardFanUpload.setOnClickListener(this);
        iv_cancel.setOnClickListener(this);

        certificateTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.id_card_radio_button:
                    certificateType = 1;
                    idCardNegativePhotoConstraintLayout.setVisibility(View.VISIBLE);
                    tvIdPositive.setText(getString(R.string.idPostive));
                    //更新照片中文字
                    break;
                case R.id.id_business_licens:
                    certificateType = 2;
                    idCardNegativePhotoConstraintLayout.setVisibility(View.INVISIBLE);
                    tvIdPositive.setText(R.string.businessLicense);
                    //更新照片中文字
                    break;

                default:
                    break;
            }
        });

        tv_baodan_tel.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                qiyezhanghu.setText(tv_baodan_tel.getText().toString());
            }
        });
    }

    private void getDataInfo() {
        OkHttp3Util.doPost(Constants.GETEN, null, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("AddCompanyActivity", e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i("AddCompanyActivity", string);
                CompanyInfoBean bean = GsonUtils.getBean(string, CompanyInfoBean.class);
                if (bean != null) {
                    if (bean.getStatus() == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!bean.getData().getEnName().isEmpty()) {
                                    tv_qiyename.setText(bean.getData().getEnName());
                                }
                                if (!bean.getData().getEnPerson().isEmpty()) {
                                    tvBaodanPeople.setText(bean.getData().getEnPerson());
                                }

                                if (!bean.getData().getEnLicenseNo().isEmpty()) {
                                    tvBaodanIdcard.setText(bean.getData().getEnLicenseNo());
                                }
                                try {
                                    if (!IDCard.IDCardValidate(bean.getData().getEnLicenseNo())) {
                                        certificateType = 2;
                                        certificateTypeRadioGroup.check(R.id.id_business_licens);

                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (!bean.getData().getCardFront().isEmpty()) {
                                    str_idcard_zheng = bean.getData().getCardFront();
                                    Glide.with(AddCompanyActivity.this).load(bean.getData().getCardFront())
                                            .into(btnIdcardZhengUpload);
                                }
                                if (!bean.getData().getCardBack().isEmpty()) {
                                    str_idcard_fan = bean.getData().getCardBack();
                                    Glide.with(AddCompanyActivity.this).load(bean.getData().getCardBack())
                                            .into(btnIdcardFanUpload);
                                }
                                if (!bean.getData().getBankName().isEmpty()) {
                                    tvBaodanOpenbank.setText(bean.getData().getBankName());
                                }
                                if (!bean.getData().getBankNo().isEmpty()) {
                                    tvBaodanBankNum.setText(bean.getData().getBankNo());
                                }
                                if (!bean.getData().getBankFront().isEmpty()) {
                                    str_bank = bean.getData().getBankFront();
                                    Glide.with(AddCompanyActivity.this).load(bean.getData().getBankFront())
                                            .into(btnBankUpload);
                                }
                                if (!bean.getData().getEnPhone().isEmpty()) {
                                    tv_baodan_tel.setText(bean.getData().getEnPhone());
                                    qiyezhanghu.setText(tv_baodan_tel.getText().toString());
                                }
                                if (!bean.getData().getAddress().isEmpty()) {
                                    tvBaodanAddress.setText(bean.getData().getAddress());
                                }
                            }
                        });
                    }
                } else {

                }

            }
        });
    }

    @Override
    protected View layoutView() {
        View inflate = View.inflate(this, R.layout.activity_add_company, null);
        return inflate;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_idcard_zheng_upload:
                photograph("idcard_zheng");
                break;
            case R.id.btn_idcard_fan_upload:
                photograph("idcard_fan");
                break;
            case R.id.btn_bank_upload:
                photograph("bank");
                break;
            case R.id.btn_wancheng:

                if (!isMobileNO(tv_baodan_tel.getText().toString().trim()) && !isPhone(tv_baodan_tel.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "联系方式填写有误", Toast.LENGTH_SHORT).show();
                    return;
                }


                if ("".equals(tv_qiyename.getText().toString())) {
                    toastUtils.showLong(this, "企业名称为空");
                    return;
                }
                if (isEmo(tv_qiyename.getText().toString())) {
                    Toast.makeText(AddCompanyActivity.this, "企业名称不能包含特殊字符", Toast.LENGTH_LONG).show();
                    return;
                }


                if ("".equals(tvBaodanPeople.getText().toString())) {
                    toastUtils.showLong(this, "企业负责人为空");
                    return;
                }
                if (isEmo(tvBaodanPeople.getText().toString())) {
                    Toast.makeText(AddCompanyActivity.this, "企业负责人名不能包含特殊字符", Toast.LENGTH_LONG).show();
                    return;
                }


                if (certificateType == 1) {
                    String strIDcard = IDCardValidate.validateIDcardNumber(tvBaodanIdcard.getText().toString().trim(), true);
                    if ("".equals(tvBaodanIdcard.getText().toString())) {
                        toastUtils.showLong(this, "身份证号为空");
                        return;
                    }
                    if (!(strIDcard.length() == 15 || strIDcard.length() == 18)) {
                        Toast.makeText(getApplicationContext(), strIDcard, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if (certificateType == 2) {

                    if ("".equals(tvBaodanIdcard.getText().toString())) {
                        toastUtils.showLong(this, "营业执照号为空");
                        return;
                    }
                    if (!isLicense(tvBaodanIdcard.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(), "请输入正确的营业执照号码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (certificateType == 1) {
                    if ("".equals(str_idcard_zheng.trim())) {
                        Toast.makeText(AddCompanyActivity.this, "请上传身份证正面照片", Toast.LENGTH_LONG).show();
                        return;
                    } else if ("".equals(str_idcard_fan.trim())) {
                        Toast.makeText(AddCompanyActivity.this, "请上传身份证反面照片", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else if (certificateType == 2) {
                    if ("".equals(str_idcard_zheng.trim())) {
                        Toast.makeText(AddCompanyActivity.this, "请上传营业执照正面照片", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if ("".equals(tvBaodanOpenbank.getText().toString())) {
                    toastUtils.showLong(this, "开户行名字为空");
                    return;
                }
                if (isEmo(tvBaodanOpenbank.getText().toString())) {
                    Toast.makeText(AddCompanyActivity.this, "开户行名称不能包含特殊字符", Toast.LENGTH_LONG).show();
                    return;
                }


                if ("".equals(tvBaodanBankNum.getText().toString())) {
                    toastUtils.showLong(this, "银行账户为空");
                    return;
                }
                if (str_bank.equals("")) {
                    Toast.makeText(getApplicationContext(), "请上传银行卡正面照片", Toast.LENGTH_SHORT).show();
                    return;
                }

                if ("".equals(tv_baodan_tel.getText().toString())) {
                    toastUtils.showLong(this, "联系方式为空");
                    return;
                }

//                if (!isMobileNO(tv_baodan_tel.getText().toString().trim()) && !isPhone(tv_baodan_tel.getText().toString().trim())) {
//                    Toast.makeText(getApplicationContext(), "联系方式填写有误", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                if ("".equals(tvBaodanAddress.getText().toString())) {
                    toastUtils.showLong(this, "企业地址为空");
                    return;
                }
                if (isEmo(tvBaodanAddress.getText().toString())) {
                    Toast.makeText(AddCompanyActivity.this, "企业地址不能包含特殊字符", Toast.LENGTH_LONG).show();
                    return;
                }

                if ("".equals(qiyepassword.getText().toString())) {
                    toastUtils.showLong(this, "密码为空");
                    return;
                }
                saveMessageFromNet();

                break;
            case R.id.iv_cancel:
                finish();
                break;
            default:
                break;
        }
    }

    //判断是否包含表情符号
    private boolean isEmo(String s) {
        boolean isemo = false;
        for (int i = 0; i < s.length(); i++) {
            isemo = isEmojiCharacter(s.charAt(i));
            if (isemo) {
                break;
            }
        }
        return isemo;
    }

    private void saveMessageFromNet() {
        Map<String,String> map = new HashMap<>();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.deptIdnew, PreferencesUtils.getStringValue(Constants.deptId, MyApplication.getAppContext()));

        map.put(Constants.id, PreferencesUtils.getStringValue(Constants.id, MyApplication.getAppContext(), "0"));

        Map<String,String> mapbody = new HashMap<>();
        mapbody.put("enName", tv_qiyename.getText().toString());
        mapbody.put("enPerson", tvBaodanPeople.getText().toString());
        mapbody.put("enPhone", tv_baodan_tel.getText().toString());
        mapbody.put("enLicenseNo", tvBaodanIdcard.getText().toString());
        mapbody.put("bankName", tvBaodanOpenbank.getText().toString());
        mapbody.put("bankNo", tvBaodanBankNum.getText().toString());
        mapbody.put("address", tvBaodanAddress.getText().toString());
        mapbody.put("account", qiyezhanghu.getText().toString());
        mapbody.put("password", qiyepassword.getText().toString());

        mapbody.put("cardFront", str_idcard_zheng);
        mapbody.put("cardBack", str_idcard_fan);
        mapbody.put("bankFront", str_bank);
        mapbody.put("bankBack", "");

        if (type) {
            mapbody.put("enId", PreferencesUtils.getStringValue(Constants.en_id, MyApplication.getAppContext()));
        }

        OkHttp3Util.doPost(Constants.adduser, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status == -1 || status == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                showDialogError(msg);
                            }
                        });
                    } else {
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 拍照
     *
     * @param imageType 图片采集类型
     */
    private void photograph(String imageType) {
        this.imageType = imageType;
        tempFile = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //下面这句指定调用相机拍照后的照片存储的路径
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(AddCompanyActivity.this, BuildConfig.APPLICATION_ID + ".provider", tempFile));
        startActivityForResult(takeIntent, REQUESTCODE_TAKE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_TAKE:// 调用相机拍照
//                if(data !=null){
//                crop(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
//                }
//                File temp = new File(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);

                if(tempFile.exists()){
                    try {
                        setPicToView(tempFile);
                    } catch (Exception e) {
//                    toastUtils.showLong(this, e.getMessage());
                        e.printStackTrace();
                    }
                }
                break;
            case REQUESTCODE_CUTTING:// 取得裁剪后的图片
                if (data != null) {
                    try {
//                        setPicToView();
                    } catch (Exception e) {
                        toastUtils.showLong(this, e.getMessage());
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 将时间戳转换为时间
     */
    private String stampToDate(long timeMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }

    /**
     * 裁剪图片方法实现
     */
    private void crop(String imagePath) {
        // TODO: 2018/8/24 By:LuoLu  "No Activity found to handle Intent"
        try {
            if (imagePath != null) {
                //call the standard crop action intent (the user device may not support it)
                Intent intent = new Intent("com.android.camera.action.CROP");
                //indicate image type and Uri
                intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/*");
                intent.putExtra("crop", "false");
                intent.putExtra("scale", true);
                intent.putExtra("return-data", false);
                uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                startActivityForResult(intent, REQUESTCODE_CUTTING);
            }
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "你的设备不支持裁剪照片，请更换设备。";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * 转换 content:// uri
     *
     * @param imageFile 图片文件
     * @return url
     */
    private Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null)) {
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
    }

    /**
     * 保存裁剪之后的图片数据
     */
    private void setPicToView(File file) throws Exception {
        // 取得SDCard图片路径做显示
//        Bitmap photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uritempFile));
        Bitmap photo = BitmapFactory.decodeFile(file.getAbsolutePath());
        String urlpath = FileUtils.saveFile(AddCompanyActivity.this, stampToDate(System.currentTimeMillis()) + "temphead.jpg", photo);

        // TODO: 2018/8/21 By:LuoLu
        File newFile = new File(urlpath);
        Log.e("newFile", newFile.getPath());
        newFile.mkdirs();
        long i = System.currentTimeMillis();
        newFile = new File(newFile.getPath());
        Log.e("fileNew：", newFile.getPath());
        OutputStream out = new FileOutputStream(newFile.getPath());
        boolean flag = photo.compress(Bitmap.CompressFormat.JPEG, 30, out);
        Log.e("flag:", "图片压缩成功" + flag);

        File fileURLPath = new File(urlpath);
        //upload_zipImage(fileURLPath, userId);
        uploadImage(fileURLPath);
    }

    private void uploadImage(File fileURLPath) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.AppKeyAuthorization, "hopen");
        map.put(Constants.id, PreferencesUtils.getStringValue(Constants.id, MyApplication.getAppContext(), "0"));
        OkHttp3Util.uploadPreFile(Constants.upload, fileURLPath, "a.jpg", null, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("uploadImage:", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            String s = null;
                            try {
                                s = response.body().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.e("uploadImage:", s);
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                int status = jsonObject.getInt("status");
                                String msg = jsonObject.getString("msg");
                                if (status != 1) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialogError(msg);
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            toastUtils.showLong(AddCompanyActivity.this, msg);
                                            try {
                                                String data = jsonObject.getString("data");

                                                Drawable drawable = new BitmapDrawable(null, BitmapFactory.decodeFile(fileURLPath.getAbsolutePath()));
                                                if (imageType.contains("idcard_zheng")) {
                                                    str_idcard_zheng = data;
                                                    btnIdcardZhengUpload.setImageDrawable(drawable);
                                                } else if (imageType.contains("idcard_fan")) {
                                                    str_idcard_fan = data;
                                                    btnIdcardFanUpload.setImageDrawable(drawable);
                                                } else if (imageType.contains("bank")) {
                                                    str_bank = data;
                                                    btnBankUpload.setImageDrawable(drawable);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    private void getCurrentLocationLatLng() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        // 同时使用网络定位和GPS定位,优先返回最高精度的定位结果,以及对应的地址描述信息
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(true);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。默认连续定位 切最低时间间隔为1000ms
        mLocationOption.setInterval(3500);
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    AMapLocation amapLocation;
    private final AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    amapLocation = amapLocation;
                    String str_address = amapLocation.getAddress();
                    tvBaodanAddress.setText(str_address);
                    amapLocation.getAccuracy();//获取精度信息
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };

    private boolean hasPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private static final int PERMISSIONS_REQUEST = 1;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stopLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLocationClient != null) {
            mLocationClient.startLocation(); // 启动定位
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();//停止定位
        }
    }


    private void controlKeyboardLayout(final ScrollView root, final Activity context) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                root.getWindowVisibleDisplayFrame(rect);
                int rootInvisibleHeight = root.getRootView().getHeight() - rect.bottom;
                //若不可视区域高度大于100，则键盘显示
                if (rootInvisibleHeight > 100) {
                    int[] location = new int[2];
                    View focus = context.getCurrentFocus();
                    if (focus != null) {
                        focus.getLocationInWindow(location);
                        int scrollHeight = (location[1] + focus.getHeight()) - rect.bottom;
                        if (rect.bottom < location[1] + focus.getHeight()) {
                            root.scrollTo(0, scrollHeight);
                        }
                    }
                } else {
                    //键盘隐藏
                    root.scrollTo(0, 0);
                }
            }
        });
    }
}
