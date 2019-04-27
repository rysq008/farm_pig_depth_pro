package org.tensorflow.demo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.AddPigObject;
import com.farm.innovation.bean.NewBuildResultObject;
import com.farm.innovation.bean.UploadImageObject;
import com.farm.innovation.login.Utils;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.ConstUtils;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.HttpRespObject;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.JsonHelper;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.UUID;




import org.tensorflow.demo.env.Logger;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.farm.innovation.base.FarmAppConfig.getStringTouboaExtra;
import static com.farm.innovation.utils.ImageUtils.fileToBitmap;


/**
 * Author by luolu, Date on 2018/8/16.
 * COMPANY：InnovationAI
 */

public class FarmToubaoCowInfoActivity extends BaseActivity {

    private static String TAG = "FarmToubaoCowInfoActivity";

    Spinner areaSpinner;

    Spinner area2Spinner;

    Spinner area3Spinner;
    private Logger mLogger = new Logger(FarmToubaoCowInfoActivity.class.getSimpleName());
    private TextView tv_title;
    private EditText tv_cow_earsNumber;
    private TextView et_baodan_number;
    private ImageView iv_cancel;
    private Button btn_commit;
    private ArrayAdapter<String> adapter;
    private String stringTouboaExtra;
    private String row_number;
    private String strcownumber;

    private AddCowTask mAddCowTask;
    private AddPigObject insurresp;
    private String errStr;
    private int userid;
    private String stringLibidExtra;

    String getCowEarID = "";
    private String stringPagetype = "";
    private String stringLipeiExtra;
    private String stringCowinfo;
    private LinearLayout ll_cow_number;
    private String stringPid;
    private ImageView img1;
    private Spinner spinnerInsuranceType;
    private String[] InsuranceType;
    private EditText tv_cow_age;

    private static final int DECIMAL_DIGITS = 1;
    private EditText tv_baoxian_perpay;
    private ImageView btn_renniu_photo;

    private View parentView;
    private PopupWindow pop = null;
    private LinearLayout ll_popup;

    private static final int REQUESTCODE_PICK = 0;        // 相册选图标记
    private static final int REQUESTCODE_TAKE = 1;        // 相机拍照标记
    private static final int REQUESTCODE_CUTTING = 2;    // 图片裁切标记

    private static String IMAGE_FILE_NAME = "";// 头像文件名称
    private String urlpath;
    private TextView btn_renniu_path;
    private RadioGroup radio_siyang;
    private Uri uritempFile;

    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    private String str_address = "";
    private TextView et_baodan_cowtype;
    //投保类型
    private int insuredType = -1;

    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 2000;
    private static long lastClickTime;
    private Gson gsonInsuredResult;
    private NewBuildResultObject animalBuildResult;
    private AlertDialog dialog;
    File tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_toubao_cow_info;
    }

    @Override
    protected void initData() {
        areaSpinner = (Spinner) findViewById(R.id.areaSpinner);
        area2Spinner = (Spinner) findViewById(R.id.area2Spinner);
        area3Spinner = (Spinner) findViewById(R.id.area3Spinner);

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText("资料采集");
        parentView = getWindow().getDecorView();
        SharedPreferences pref = getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        userid = pref.getInt("uid", 0);
        btn_renniu_path = findViewById(R.id.btn_renniu_path);
        btn_renniu_photo = findViewById(R.id.btn_renniu_photo);
        btn_renniu_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ll_popup.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.farm_activity_translate_in));
                pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
            }
        });

        tv_cow_age = findViewById(R.id.tv_cow_age);
        animalBuildResult = new NewBuildResultObject();


        et_baodan_number = findViewById(R.id.et_baodan_number);
        tv_cow_earsNumber = findViewById(R.id.tv_cow_earsNumber);
        stringLipeiExtra = getIntent().getStringExtra("LipeiTempNumber");
        stringTouboaExtra = getIntent().getStringExtra("ToubaoTempNumber");
        stringLibidExtra = getIntent().getStringExtra("libid");
        stringPagetype = getIntent().getStringExtra("pagetype") == null ? "" : getIntent().getStringExtra("pagetype");
        stringCowinfo = getIntent().getStringExtra("cowinfo");
        stringPid = getIntent().getStringExtra("pid");

        strcownumber = stampToDate(System.currentTimeMillis());
        et_baodan_number.setText(getStringTouboaExtra);

        //获取地址
        getCurrentLocationLatLng();


        setPoint(tv_cow_age);

        btn_commit = findViewById(R.id.btn_commit);
        btn_commit.setOnClickListener(btn_commitClickListener);

        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);

        //选择图片
        pop = new PopupWindow(getApplicationContext());

        View view = getLayoutInflater().inflate(R.layout.farm_item_popupwindows, null);

        ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);

        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);

        IMAGE_FILE_NAME = stampToDate(System.currentTimeMillis()) + ".jpg";

        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent);
        Button bt1 = (Button) view.findViewById(R.id.item_popupwindows_camera);
        Button bt2 = (Button) view.findViewById(R.id.item_popupwindows_Photo);
        Button bt3 = (Button) view.findViewById(R.id.item_popupwindows_cancel);
        parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //  Auto-generated method stub
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tempFile = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
                Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //下面这句指定调用相机拍照后的照片存储的路径
                takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), AppConfig.getAppContext().getPackageName().concat(".animalcounter.provider"), tempFile));
                startActivityForResult(takeIntent, REQUESTCODE_TAKE);
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                // 如果朋友们要限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpeg");
                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                overridePendingTransition(R.anim.farm_activity_translate_in, R.anim.farm_activity_translate_out);
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ll_popup.clearAnimation();
            }
        });
        // TODO: 2018/8/14 By:LuoLu
        spinnerInsuranceType = findViewById(R.id.spinnerInsuranceType);

        et_baodan_cowtype = findViewById(R.id.et_baodan_cowtype);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ConstUtils.geInsureTypeCaptions(FarmerPreferencesUtils.getAnimalType(FarmToubaoCowInfoActivity.this)));
        spinnerInsuranceType.setAdapter(adapter);

        // TODO: 2018/8/5
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.area));
        ArrayAdapter array2Adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.area2));
        ArrayAdapter array3Adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.area3));
        areaSpinner.setAdapter(arrayAdapter);
        area2Spinner.setAdapter(array2Adapter);
        area3Spinner.setAdapter(array3Adapter);
        areaSpinner.setSelection(0, true);
        area2Spinner.setSelection(0, true);
        area3Spinner.setSelection(0, true);


        // TODO: 2018/8/21 By:LuoLu
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUESTCODE_PICK:// 直接从相册获取
                try {
                    startPhotoZoom(data.getData());
                } catch (NullPointerException e) {
                    e.printStackTrace();// 用户点击取消操作
                }
                break;
            case REQUESTCODE_TAKE:// 调用相机拍照
//                File temp = new File(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
//                crop(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
                if(tempFile.exists()){
                    try {
                        setPicToView(tempFile);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                break;
            case REQUESTCODE_CUTTING:// 取得裁剪后的图片
                if (data != null) {
                    try {
//                        setPicToView();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 裁剪图片方法实现
     */
    public void crop(String imagePath) {
        File file = new File(imagePath);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/jpeg");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/jpeg");
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
//        intent.putExtra("return-data", true);

        //裁剪后的图片Uri路径，uritempFile为Uri类变量
        uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    String zipFilePaht = "";

    /**
     * 保存裁剪之后的图片数据
     */
    private void setPicToView(File file) throws Exception {
        // 取得SDCard图片路径做显示
//        Bitmap photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uritempFile));

        Bitmap photo = fileToBitmap(file.getAbsolutePath(), 4);

        Drawable drawable = new BitmapDrawable(null, photo);
        urlpath = FileUtils.saveFile(getApplicationContext(), "temphead.jpg", photo);

        btn_renniu_photo.setImageDrawable(drawable);


        File file_zip = new File(urlpath);


        // 新线程后台上传服务端
//            pd = ProgressDialog.show(MineInfoActivity.this, null, "正在上传图片，请稍候...");
//            new Thread(uploadImageRunnable).start();
//            postFile();
        if (FarmAppConfig.isOfflineMode) {
            // 将合影图片存入temp文件夹
            FileUtils.moveFile(urlpath, Environment.getExternalStorageDirectory().getPath() + FarmAppConfig.OFFLINE_TEMP_PATH + "heyingPic.jpg");
        } else {
            UploadImageObject imgResp = upload_zipImage(file_zip, userid);
            int status;
            if (imgResp == null || imgResp.status != HttpRespObject.STATUS_OK) {
                status = imgResp == null ? -1 : imgResp.status;
                return;
            }
        }

    }

    private UploadImageObject upload_zipImage(File zipFile_image, int uid) {
        UploadImageObject imgResp = HttpUtils.uploadImage(zipFile_image, uid);

        if (imgResp == null || imgResp.status != HttpRespObject.STATUS_OK) {
            int status = imgResp == null ? -1 : imgResp.status;
            Toast.makeText(getApplicationContext(), imgResp.msg, Toast.LENGTH_SHORT).show();
            return imgResp;
        }
        btn_renniu_path.setText(imgResp.upload_imagePath);
        return imgResp;
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


    private View.OnClickListener btn_commitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // mis-clicking prevention, using threshold of 1000 ms
            if (SystemClock.elapsedRealtime() - lastClickTime < MIN_CLICK_DELAY_TIME) {
                Toast.makeText(FarmToubaoCowInfoActivity.this, "正在处理，请勿连续多次点击！", Toast.LENGTH_SHORT).show();
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();


            insuredType = ConstUtils.getInsureTypeCodeIntByCaption(spinnerInsuranceType.getSelectedItem().toString());

            String str_cow_age = tv_cow_age.getText().toString();
            String str_renniu_path = btn_renniu_path.getText().toString();

            String seqNo = stampToDate(System.currentTimeMillis());
            if (tv_cow_earsNumber.getText().toString().equals("")) {
                getCowEarID = seqNo;
            } else {
                getCowEarID = tv_cow_earsNumber.getText().toString();
            }

            mLogger.i("保单号：" + getStringTouboaExtra);
            mLogger.i("耳标号：" + getCowEarID);

            if (str_cow_age.equals("")) {
                Toast.makeText(getApplicationContext(), "请输入畜龄", Toast.LENGTH_SHORT).show();
                return;
            }
            mProgressHandler.sendEmptyMessage(51);

            str_address = str_address.equals("") ? "未知位置" : str_address;
            //读取用户信息
            SharedPreferences pref_user = FarmToubaoCowInfoActivity.this.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
            String username = pref_user.getString("fullname", "");


            TreeMap<String, String> query = new TreeMap<>();
            query.put("baodanNo", getStringTouboaExtra);
            query.put("pigNo", getCowEarID.trim());
            query.put("type", "1");
            query.put("address", str_address);
            query.put("person", username);
            query.put("pigType", String.valueOf(insuredType));
            query.put("libId", stringLibidExtra);
            query.put("seqNo", seqNo);
            query.put("toubaoType", String.valueOf(insuredType));
            query.put("xuling", str_cow_age.trim());
            query.put("heyingPic", str_renniu_path.trim());
            query.put("yiji", areaSpinner.getSelectedItem().toString());
            query.put("erji", area2Spinner.getSelectedItem().toString());
            query.put("sanji", area3Spinner.getSelectedItem().toString());

            query.put("level1", areaSpinner.getSelectedItem().toString());
            query.put("level2", area2Spinner.getSelectedItem().toString());
            query.put("level3", area3Spinner.getSelectedItem().toString());
            query.put("level5", "");


            if (FarmAppConfig.isOfflineMode) {
                StringBuilder builder = new StringBuilder();
                JsonHelper.mapToJson(query, builder);
                String offLineTempFile = Environment.getExternalStorageDirectory().getPath() + FarmAppConfig.OFFLINE_TEMP_PATH;
                File newDir = new File(offLineTempFile);
                if (!newDir.exists()) {
                    newDir.mkdirs();
                }
                String jsonFileName = newDir.getAbsolutePath() + "/info.json";
                File jsonFile = new File(jsonFileName);
                try {
                    jsonFile.createNewFile();
                    /* 保存人畜合影   */
                    if (!str_renniu_path.trim().equals("")) {
                        String picFileName = newDir.getAbsolutePath() + "/heyingPic.jpg";
                        File picFile = new File(picFileName);
                        picFile.createNewFile();
                        //移动文件
                        FileUtils.moveFile(offLineTempFile, picFileName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //文件生成
                String jsonString = builder.toString();
                com.farm.innovation.tuil2.FileUtils.writeDatesToSDCard(jsonFile, jsonString);
                // TODO: 2018/8/9 压缩大文件
                String uploadZipFilePathStr = Environment.getExternalStorageDirectory().getPath() + FarmAppConfig.OFFLINE_PATH + FarmAppConfig.offLineInsuredNo + "/";
                File uploadZipFilePath = new File(uploadZipFilePathStr);
                if (!uploadZipFilePath.exists()) {
                    uploadZipFilePath.mkdirs();
                }

                String uuidFileName = uploadZipFilePathStr + UUID.randomUUID().toString().replaceAll("-", "") + ".zip";
                File insuredNoFile = new File(offLineTempFile);
                File[] files = insuredNoFile.listFiles();
                boolean zipResultOK = ZipUtil.zipFiles(files, new File(uuidFileName));

                String resultStr;

                if (zipResultOK) {
                    resultStr = "离线验标成功";
                } else {
                    resultStr = "离线验标失败，请再试";
                }
                mProgressHandler.sendEmptyMessage(44);
                AlertDialog.Builder innerBuilder = new AlertDialog.Builder(FarmToubaoCowInfoActivity.this)
                        .setIcon(R.drawable.farm_cowface)
                        .setTitle("提示")
                        .setMessage(resultStr)
                        .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();

                            }
                        })
                        .setNegativeButton("继续录入", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(FarmToubaoCowInfoActivity.this, FarmDetectorActivity.class);
                                intent.putExtra("ToubaoTempNumber", FarmAppConfig.offLineInsuredNo);
                                startActivity(intent);
                                FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(2);
                                finish();
                            }
                        });
                innerBuilder.create();
                innerBuilder.setCancelable(false);
                innerBuilder.show();


            } else {
                mAddCowTask = new AddCowTask(HttpUtils.INSUR_ADDPIG_URL, query);
                mAddCowTask.execute((Void) null);
            }


        }
    };


    public class AddCowTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final TreeMap<String, String> mQueryMap;

        AddCowTask(String url, TreeMap map) {
            mUrl = url;
            mQueryMap = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                FormBody.Builder builder = new FormBody.Builder();
                // Add Params to Builder
                for (TreeMap.Entry<String, String> entry : mQueryMap.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }
                // Create RequestBody
                RequestBody formBody = builder.build();

                String response = HttpUtils.post(mUrl, formBody);
                Log.d(TAG, mUrl + ":\nresponse:" + response);
                if (HttpUtils.INSUR_ADDPIG_URL.equalsIgnoreCase(mUrl)) {
                    insurresp = (AddPigObject) HttpUtils.processResp_insurInfo(response, mUrl);
                    if (insurresp == null) {
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
                AVOSCloudUtils.saveErrorMessage(e, FarmToubaoCowInfoActivity.class.getSimpleName());
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddCowTask = null;
            if (success & HttpUtils.INSUR_ADDPIG_URL.equalsIgnoreCase(mUrl)) {

// TODO: 2018/8/23 By:LuoLu
                animalBuildResult = new NewBuildResultObject();
                animalBuildResult = HttpUtils.upload_build("1", stringLibidExtra, getCowEarID, getStringTouboaExtra, getApplicationContext());
                if (animalBuildResult == null) {
                    mProgressHandler.sendEmptyMessage(24);
                    return;
                }
                Log.d("animalBuildResult:", animalBuildResult.toString());
                mHandler.sendEmptyMessage(100);

            } else if (!success) {
                //  显示失败
                Log.d(TAG, errStr);
                Toast.makeText(getApplicationContext(), "onPostExecute:" + errStr, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAddCowTask = null;
        }
    }


    public static void setPoint(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > DECIMAL_DIGITS) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + DECIMAL_DIGITS + 1);
                        editText.setText(s);
                        editText.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    editText.setText(s);
                    editText.setSelection(2);
                }
                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        editText.setText(s.subSequence(0, 1));
                        editText.setSelection(1);
                        return;
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public String stampToDate(long timeMillis) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (msg.what) {
                case 100:
                    if (animalBuildResult.status == 1) {
                        if (animalBuildResult.buildStatus == 1) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FarmToubaoCowInfoActivity.this)
                                    .setIcon(R.drawable.farm_cowface)
                                    .setTitle("提示")
                                    .setMessage("第" + animalBuildResult.buildSum + "头验标成功")
                                    .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();

                                        }
                                    })
                                    .setNegativeButton("继续录入", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(FarmToubaoCowInfoActivity.this, FarmDetectorActivity.class);
                                            intent.putExtra("ToubaoTempNumber", stringTouboaExtra.trim());
                                            startActivity(intent);
                                            FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(2);
                                            finish();
                                        }
                                    });
                            builder.create();
                            builder.setCancelable(false);
                            builder.show();
                        } else if (animalBuildResult.buildStatus == 2) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FarmToubaoCowInfoActivity.this)
                                    .setIcon(R.drawable.farm_cowface)
                                    .setTitle("提示")
                                    .setMessage("投保失败，图片质量不合格！")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    })
                                    .setNegativeButton("重新投保", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(FarmToubaoCowInfoActivity.this, FarmDetectorActivity.class);
                                            intent.putExtra("ToubaoTempNumber", stringTouboaExtra.trim());
                                            startActivity(intent);
                                            FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(2);
                                            finish();
                                        }
                                    });
                            builder.create();
                            builder.setCancelable(false);
                            builder.show();
                        } else if (animalBuildResult.buildStatus == 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FarmToubaoCowInfoActivity.this)
                                    .setIcon(R.drawable.farm_cowface)
                                    .setTitle("提示")
                                    .setMessage("投保失败，服务端异常，请稍后再试！")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    })
                                    .setNegativeButton("重新投保", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(FarmToubaoCowInfoActivity.this, FarmDetectorActivity.class);
                                            intent.putExtra("ToubaoTempNumber", stringTouboaExtra.trim());
                                            startActivity(intent);
                                            FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(2);
                                            finish();
                                        }
                                    });
                            builder.create();
                            builder.setCancelable(false);
                            builder.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FarmToubaoCowInfoActivity.this)
                                    .setIcon(R.drawable.farm_cowface)
                                    .setTitle("提示")
                                    .setMessage("未知错误！")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(FarmToubaoCowInfoActivity.this, FarmDetectorActivity.class);
                                            intent.putExtra("ToubaoTempNumber", stringTouboaExtra.trim());
                                            startActivity(intent);
                                            FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(2);
                                            finish();
                                        }
                                    });
                            builder.create();
                            builder.setCancelable(false);
                            builder.show();
                        }

                    } else if (animalBuildResult.status == -4) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FarmToubaoCowInfoActivity.this)
                                .setIcon(R.drawable.farm_cowface)
                                .setTitle("提示")
                                .setMessage("服务异常，请稍后再试！")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                })
                                .setNegativeButton("重新投保", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(FarmToubaoCowInfoActivity.this, FarmDetectorActivity.class);
                                        intent.putExtra("ToubaoTempNumber", stringTouboaExtra.trim());
                                        startActivity(intent);
                                        FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(2);
                                        finish();
                                    }
                                });
                        builder.create();
                        builder.setCancelable(false);
                        builder.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FarmToubaoCowInfoActivity.this)
                                .setIcon(R.drawable.farm_cowface)
                                .setTitle("提示")
                                .setMessage("服务异常，请稍后再试！")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                })
                                .setNegativeButton("重新投保", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(FarmToubaoCowInfoActivity.this, FarmDetectorActivity.class);
                                        intent.putExtra("ToubaoTempNumber", stringTouboaExtra.trim());
                                        startActivity(intent);
                                        FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(2);
                                        finish();
                                    }
                                });
                        builder.create();
                        builder.setCancelable(false);
                        builder.show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        mHandler.removeMessages(100);
        mProgressHandler.removeMessages(51);
        mProgressHandler.removeMessages(61);
        mProgressHandler.removeMessages(41);
        mProgressHandler.removeMessages(44);
        if (dialog != null) {
            dialog.dismiss();
        }
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();

    }

    private ProgressDialog mProgressDialog = null;
    private Handler mProgressHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 51:
                    mProgressDialog = new ProgressDialog(FarmToubaoCowInfoActivity.this);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setIcon(R.drawable.farm_cowface);
                    mProgressDialog.setMessage("正在处理......");
                    mProgressDialog.show();
                    break;
                case 61:
                    mProgressDialog = new ProgressDialog(FarmToubaoCowInfoActivity.this);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setIcon(R.drawable.farm_cowface);
                    mProgressDialog.setMessage("处理成功......");
                    mProgressDialog.show();
                case 41:
                    mProgressDialog.dismiss();
                    AlertDialog.Builder tcaBuilder = new AlertDialog.Builder(FarmToubaoCowInfoActivity.this)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("网络异常！请重试")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(getApplicationContext(), FarmDetectorActivity.class);
                                    intent.putExtra("ToubaoTempNumber", stringTouboaExtra.trim());
                                    startActivity(intent);
                                    FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(2);
                                    finish();
                                }
                            });
                    tcaBuilder.create();
                    tcaBuilder.setCancelable(false);
                    tcaBuilder.show();
                    break;
                case 44:
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    break;
                case 24:
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(FarmToubaoCowInfoActivity.this)
                            .setIcon(R.drawable.farm_cowface)
                            .setTitle("提示")
                            .setMessage("网络超时，请稍后再试！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setNegativeButton("重新投保", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(getApplicationContext(), FarmDetectorActivity.class);
                                    intent.putExtra("ToubaoTempNumber", stringTouboaExtra.trim());
                                    startActivity(intent);
                                    FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(2);
                                    finish();
                                }
                            });
                    builder.create();
                    builder.setCancelable(false);
                    builder.show();
                    break;
                default:
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    break;
            }
        }
    };


    private void getCurrentLocationLatLng() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        // 同时使用网络定位和GPS定位,优先返回最高精度的定位结果,以及对应的地址描述信息
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(true);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。默认连续定位 切最低时间间隔为1000ms
        mLocationOption.setInterval(3500);
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {

            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    double currentLat = amapLocation.getLatitude();//获取纬度
                    double currentLon = amapLocation.getLongitude();//获取经度
                    str_address = amapLocation.getAddress();
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
}
