package com.farm.innovation.login;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.LiPeiLocalBean;
import com.farm.innovation.bean.VideoUpLoadBean;
import com.farm.innovation.bean.company_child;
import com.farm.innovation.bean.company_total;
import com.farm.innovation.login.model.LocalModelNongxian;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.PreferencesUtils;
import com.farm.innovation.utils.StorageUtils;
import com.farm.innovation.utils.ZipUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static com.farm.innovation.utils.HttpUtils.uploadImage;


/**
 * Created by luolu on 08/01/2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserManager8.db";

    private Semaphore semaphore = new Semaphore(1);

    private static DatabaseHelper databaseHelper;

    private static final String TABLE_USER = "user";
    private static final String TABLE_COMPANY = "tCompany";
    private static final String TABLE_LOCAL = "tLocal";
    private static final String TABLE_LIPEI = "tLipei";
    private static final String TABLE_UP_LOAD_VIDEO = "upLoadVideo";

    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_USER_IDNUMBER = "user_idnumber";
    private static final String COLUMN_USER_PASSWORD = "user_password";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "fullname";
    private static final String COLUMN_PID = "pid";
    private final Context context;

    private String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_PHONE_NUMBER + " TEXT,"
            + COLUMN_USER_IDNUMBER + " TEXT," + COLUMN_USER_PASSWORD + " TEXT" + ")";

    private String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + TABLE_USER;

    private String CREATE_COMPANY_TABLE = "CREATE TABLE " + TABLE_COMPANY + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT,"
            + COLUMN_PID + " INTEGER)";

    private String DROP_COMPANY_TABLE = "DROP TABLE IF EXISTS " + TABLE_COMPANY;

    private String CREATE_LOCAL_TABLE =
            "CREATE TABLE " + TABLE_LOCAL +
                    "(id integer primary key autoincrement,baodanNo text not null,name text not null," +
                    "cardNo text not null,insureDate text not null,animalType text not null,yanBiaoName text not null," +
                    "userid TEXT,baodanName TEXT)";
    private String CREATE_LIPEI_TABLE =
            "CREATE TABLE " + TABLE_LIPEI +
                    "(id integer primary key autoincrement,baodanNo text not null,insurename text not null," +
                    "cardNo text not null,insureReason text not null,insureQSL text not null,insureDate text not null," +
                    "longitude text not null,latitude text not null,animalType text not null,earsTagNo TEXT,zippath TEXT," +
                    "recordeText TEXT,recordeMsg TEXT,userid TEXT,videozippath TEXT,baodanname text,yanbaoname text, " +
                    "videoupsuccess text, isforce text default '0', during text default '0')";

    private String CREATE_UP_LOAD_VIDEO_TABLE = "CREATE TABLE " + TABLE_UP_LOAD_VIDEO +
            "(id integer primary key autoincrement,libNub text not null,userId text not null," +
            "libEnvinfo text not null,animalType text not null,collectTimes text not null,timesFlag text not null," +
            "collectTime text not null,uploadComplete text default '0', videoFilePath text not null)";

    private String DROP_LOCAL_TABLE = "DROP TABLE IF EXISTS " + TABLE_LOCAL;
    private String DROP_LIPEI_TABLE = "DROP TABLE IF EXISTS " + TABLE_LIPEI;
    private String DROP_UP_LOAD_VIDEO_TABLE = "DROP TABLE IF EXISTS " + TABLE_UP_LOAD_VIDEO;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, HttpUtils.DATABSAE_VERSION);
        this.context = context;
    }

    public static DatabaseHelper getInstance(Context context) {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context);
        }
        return databaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_COMPANY_TABLE);
        db.execSQL(CREATE_LOCAL_TABLE);
        db.execSQL(CREATE_LIPEI_TABLE);
        db.execSQL(CREATE_UP_LOAD_VIDEO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("oldverson", oldVersion + "");
        Log.i("newverson", newVersion + "");
//        db.execSQL(DROP_USER_TABLE);
//        db.execSQL(DROP_COMPANY_TABLE);
//        db.execSQL(DROP_LOCAL_TABLE);
//        db.execSQL(DROP_LIPEI_TABLE);
        switch (oldVersion) {
            case 7:
                db.execSQL("CREATE TABLE " + TABLE_UP_LOAD_VIDEO +
                    "(id integer primary key autoincrement,libNub text not null,userId text not null," +
                        "libEnvinfo text not null,animalType text not null,collectTimes text not null,timesFlag text not null," +
                        "collectTime text not null,uploadComplete varchar default '0', videoFilePath text not null)");
                // 将表名改为临时表
                db.execSQL("ALTER TABLE "+TABLE_LIPEI+" RENAME TO tempTable");
                // 创建新表
                db.execSQL("CREATE TABLE " + TABLE_LIPEI +
                        "(id integer primary key autoincrement,baodanNo text not null,insurename text not null," +
                        "cardNo text not null,insureReason text not null,insureQSL text not null,insureDate text not null," +
                        "longitude text not null,latitude text not null,animalType text not null,earsTagNo TEXT,zippath TEXT," +
                        "recordeText TEXT,recordeMsg TEXT,userid TEXT,videozippath TEXT,baodanname text,yanbaoname text)");
                // 导入数据
                db.execSQL("INSERT INTO "+ TABLE_LIPEI +" SELECT * FROM tempTable");

                db.execSQL("DROP TABLE tempTable");
                db.execSQL("Alter table " + TABLE_LIPEI + " add column videoupsuccess text ");
                db.execSQL("Alter table " + TABLE_LIPEI + " add column isforce text default '0'");
                db.execSQL("Alter table " + TABLE_LIPEI + " add column during text default '0'");

                break;
            default:
        }
    }

    public void addLiPeiLocalData(LiPeiLocalBean liPeiLocalBean) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("baodanNo", liPeiLocalBean.pbaodanNo);
        values.put("insurename", liPeiLocalBean.pinsurename);
        values.put("cardNo", liPeiLocalBean.pcardNo);
        values.put("insureReason", liPeiLocalBean.pinsureReason);
        values.put("insureQSL", liPeiLocalBean.pinsureQSL);
        values.put("insureDate", liPeiLocalBean.pinsureDate);
        values.put("longitude", liPeiLocalBean.plongitude);
        values.put("latitude", liPeiLocalBean.platitude);
        values.put("animalType", liPeiLocalBean.panimalType);
        values.put("earsTagNo", liPeiLocalBean.earsTagNo);
        values.put("zippath", liPeiLocalBean.pzippath);
        values.put("videozippath", liPeiLocalBean.pVideozippath);
        values.put("recordeText", liPeiLocalBean.precordeText);
        values.put("baodanname", liPeiLocalBean.pbaodanName);
        values.put("yanbaoname", liPeiLocalBean.pyanbiaodanName);
        values.put("videoupsuccess", liPeiLocalBean.videoUpSuccess);
        values.put("isforce", liPeiLocalBean.isForce);
        values.put("during", liPeiLocalBean.during);

        values.put("userid", PreferencesUtils.getStringValue(HttpUtils.user_id, FarmAppConfig.getApplication()));

        db.insert(TABLE_LIPEI, null, values);
        db.close();
    }

    public synchronized void queryAllTABLE_LIPEI() {
        try {
            queryAll(TABLE_LIPEI);
            queryAll(TABLE_USER);
            queryAll(TABLE_COMPANY);
            queryAll(TABLE_LOCAL);
//            queryAll(TABLE_UP_LOAD_VIDEO);

            File file = new File(StorageUtils.getExternalCacheDir(FarmAppConfig.getActivity()) + "/Scan/db");
            processZip(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //压缩图片文件
    private void processZip(File file) {
        File file_num = null;
        String zipimageDri = "";

        zipimageDri = StorageUtils.getExternalCacheDir(FarmAppConfig.getActivity()) + "/Scan";

        //获取图片文件
        File[] files_image = file.listFiles();
        if (files_image == null) {
            //"文件不存在
            return;
        }
        if (files_image.length == 0) {
            //"文件不存在
            return;
        }
        // 4. zip recognized image
        //加入编号文件
        File[] fs_image = new File[files_image.length + 1];
        for (int i = 0; i < files_image.length; i++) {
            fs_image[i] = files_image[i];
        }
        fs_image[files_image.length] = file_num;

        //打包图片文件
        File file_current = new File(zipimageDri);
        File zipFile_image = new File(file_current, "Scan" + ".zip");
        ZipUtil.zipFiles(fs_image, zipFile_image);
        if (!zipFile_image.exists()) {
            //"压缩图片出错，请重新录制";
            Log.e("ZipUtil", "压缩失败");
            return;
        }
        Log.i("imageFile==", zipFile_image.getAbsolutePath());
        SharedPreferences pref = context.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        int userid = pref.getInt("uid", 0);

        //File newfile = new File(getSDPath()+ "/Scan.jpeg");

        //zipFile_image.renameTo(newfile);

        uploadImage(zipFile_image, userid);
    }


    public synchronized void queryAll(String tableName) {
        String fileName = StorageUtils.getExternalCacheDir(FarmAppConfig.getActivity()) + "/Scan/db/" + tableName + ".txt";
        File f = new File(fileName);
        if (f.exists()) {
            f.delete();
        }
        File file = new File(StorageUtils.getExternalCacheDir(FarmAppConfig.getActivity()) + "/Scan/db");
        makeDir(file);

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            semaphore.acquire();
            db.beginTransaction();
            Cursor cursor = db.query(tableName, null, null, null, null, null, null);
            Log.e("Cursor", "Cursor: " + cursor);
            Map<String, String> rowMap;
            if (cursor.moveToFirst()) {
                do {
                    rowMap = new HashMap<String, String>();
                    String[] columnNames = cursor.getColumnNames();
                    for (String columnName : columnNames) {
                        rowMap.put(columnName, cursor.getString(cursor.getColumnIndex(columnName)));
                    }
                    writeTxt(fileName, rowMap.toString() + "\n");
                } while (cursor.moveToNext());
            }
            cursor.close();

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
            semaphore.release();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private static void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdirs();
    }

    private static void writeTxt(String fileName, String content) {
        try {   //要指定编码方式，否则会出现乱码
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName, true), "gbk");
            osw.write(content);
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        String dir = sdDir.toString();
        return dir;
    }

    /**
     * @param status 0:上传失败，1:上传成功，其他否为全部
     * @return
     */
    public synchronized List<VideoUpLoadBean> queryVideoUpLoadDataByStatus(int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<VideoUpLoadBean> list = new ArrayList<>();
        String selection = ((status == 1 || status == 0) ? "uploadComplete=?" : null);
        String[] selectionArgs = ((status == 1 || status == 0) ? new String[]{status + ""} : null);
        Cursor cursor = db.query(TABLE_UP_LOAD_VIDEO, null, selection, selectionArgs, null, null, null);

        if (null != cursor) {
            while (cursor.moveToNext()) {
                VideoUpLoadBean bean = new VideoUpLoadBean();
//                bean.id = cursor.getString(cursor.getColumnIndex("id"));
                bean.libNub = cursor.getString(cursor.getColumnIndex("libNub"));
                bean.userId = cursor.getString(cursor.getColumnIndex("userId"));
                bean.libEnvinfo = cursor.getString(cursor.getColumnIndex("libEnvinfo"));
                bean.animalType = cursor.getString(cursor.getColumnIndex("animalType"));
                bean.collectTimes = cursor.getString(cursor.getColumnIndex("collectTimes"));
                bean.timesFlag = cursor.getString(cursor.getColumnIndex("timesFlag"));
                bean.collectTime = cursor.getString(cursor.getColumnIndex("collectTime"));
                bean.uploadComplete = cursor.getString(cursor.getColumnIndex("uploadComplete"));
                bean.videoFilePath = cursor.getString(cursor.getColumnIndex("videoFilePath"));
                list.add(bean);
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    public String queryVideoUpLoadDataBytimesFlag(String timesFlag) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = ("timesFlag=?");
        String[] selectionArgs = (new String[]{timesFlag});
        Cursor cursor = db.query(TABLE_UP_LOAD_VIDEO, null, selection, selectionArgs, null, null, null);
        String complete = "0";
        if (null != cursor && cursor.moveToFirst()) {
            complete = cursor.getString(cursor.getColumnIndex("uploadComplete"));
            cursor.close();
        }
        db.close();
        return complete;
    }

    public void inserVideoUpLoadBean(VideoUpLoadBean bean) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues cv = new ContentValues();
        Class cls = bean.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                String val = (f.get(bean) == null) ? "" : (f.get(bean).toString());
                String key = f.getName();
                if (!"$change".equals(key) && !"serialVersionUID".equals(key))
                    cv.put(f.getName(), val);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        db.insert(TABLE_UP_LOAD_VIDEO, null, cv);
        db.close();
    }

    public void updataVideoUpLoadBean(VideoUpLoadBean bean) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Class cls = bean.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                String val = (f.get(bean) == null) ? "" : (f.get(bean).toString());
                String key = f.getName();
                if (!"$change".equals(key) && !"serialVersionUID".equals(key))
                    cv.put(f.getName(), val);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        db.update(TABLE_UP_LOAD_VIDEO, cv, "timesFlag=?", new String[]{bean.timesFlag});
        db.close();
    }

    public void deleteVideoUpLoadBean(VideoUpLoadBean bean) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_UP_LOAD_VIDEO, "timesFlag=?", new String[]{bean.timesFlag});
        db.close();
    }

    public List<LiPeiLocalBean> queryLocalDataFromLiPeiNotUp() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<LiPeiLocalBean> LiPeiLocalBeans = null;
        String[] columns = {"baodanNo", "insurename", "cardNo", "insureReason", "insureQSL", "insureDate",
                "longitude", "latitude", "animalType", "earsTagNo", "zippath", "recordeText", "recordeMsg",
                "videozippath", "baodanname", "yanbaoname", "videoupsuccess", "isforce", "during"};

        String selection = "videoupsuccess=?";

        Cursor cursor = db.query(TABLE_LIPEI, columns, selection, new String[]{"0"},
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            LiPeiLocalBeans = new ArrayList<LiPeiLocalBean>();
            while (cursor.moveToNext()) {
                String baodanNo = cursor.getString(cursor.getColumnIndex("baodanNo"));
                String insurename = cursor.getString(cursor.getColumnIndex("insurename"));
                String cardNo = cursor.getString(cursor.getColumnIndex("cardNo"));
                String insureReason = cursor.getString(cursor.getColumnIndex("insureReason"));
                String insureQSL = cursor.getString(cursor.getColumnIndex("insureQSL"));
                String insureDate = cursor.getString(cursor.getColumnIndex("insureDate"));
                String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                String animalType = cursor.getString(cursor.getColumnIndex("animalType"));
                String earsTagNo = cursor.getString(cursor.getColumnIndex("earsTagNo"));
                String zippath = cursor.getString(cursor.getColumnIndex("zippath"));
                String recordeText = cursor.getString(cursor.getColumnIndex("recordeText"));
                String recordeMsg = cursor.getString(cursor.getColumnIndex("recordeMsg"));
                String videozippath = cursor.getString(cursor.getColumnIndex("videozippath"));
                String lipbaodanname = cursor.getString(cursor.getColumnIndex("baodanname"));
                String lipyanbaoname = cursor.getString(cursor.getColumnIndex("yanbaoname"));
                String videoupsuccess = cursor.getString(cursor.getColumnIndex("videoupsuccess"));

                String isforce = cursor.getString(cursor.getColumnIndex("isforce"));
                String during = cursor.getString(cursor.getColumnIndex("during"));
                LiPeiLocalBean localData = new LiPeiLocalBean(baodanNo, insurename, cardNo, insureReason, insureQSL,
                        insureDate, longitude, latitude, animalType, earsTagNo, zippath, recordeText, recordeMsg,
                        videozippath, lipbaodanname, lipyanbaoname, videoupsuccess, isforce, during);
                LiPeiLocalBeans.add(localData);
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        db.close();
        db = null;
        return LiPeiLocalBeans;
    }

    public List<LiPeiLocalBean> queryLocalDataFromLiPei(String muserid) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<LiPeiLocalBean> LiPeiLocalBeans = null;
        String[] columns = {"baodanNo", "insurename", "cardNo", "insureReason", "insureQSL", "insureDate", "longitude", "latitude",
                "animalType", "earsTagNo", "zippath", "recordeText", "recordeMsg", "videozippath", "baodanname", "yanbaoname", "videoupsuccess",
                "isforce", "during"};
        String selection = "userid=? and animalType=?";

        String[] selectionArgs = {muserid, String.valueOf(PreferencesUtils.getAnimalType(context))};

        Cursor cursor = db.query(TABLE_LIPEI, columns, selection, selectionArgs,
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            LiPeiLocalBeans = new ArrayList<LiPeiLocalBean>();
            while (cursor.moveToNext()) {
                String baodanNo = cursor.getString(cursor.getColumnIndex("baodanNo"));
                String insurename = cursor.getString(cursor.getColumnIndex("insurename"));
                String cardNo = cursor.getString(cursor.getColumnIndex("cardNo"));
                String insureReason = cursor.getString(cursor.getColumnIndex("insureReason"));
                String insureQSL = cursor.getString(cursor.getColumnIndex("insureQSL"));
                String insureDate = cursor.getString(cursor.getColumnIndex("insureDate"));
                String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                String animalType = cursor.getString(cursor.getColumnIndex("animalType"));
                String earsTagNo = cursor.getString(cursor.getColumnIndex("earsTagNo"));
                String zippath = cursor.getString(cursor.getColumnIndex("zippath"));
                String recordeText = cursor.getString(cursor.getColumnIndex("recordeText"));
                String recordeMsg = cursor.getString(cursor.getColumnIndex("recordeMsg"));
                String videozippath = cursor.getString(cursor.getColumnIndex("videozippath"));
                String lipbaodanname = cursor.getString(cursor.getColumnIndex("baodanname"));
                String lipyanbaoname = cursor.getString(cursor.getColumnIndex("yanbaoname"));
                String videoupsuccess = cursor.getString(cursor.getColumnIndex("videoupsuccess"));
                String isforce = cursor.getString(cursor.getColumnIndex("isforce"));
                String during = cursor.getString(cursor.getColumnIndex("during"));
                LiPeiLocalBean localData = new LiPeiLocalBean(baodanNo, insurename, cardNo, insureReason, insureQSL, insureDate, longitude,
                        latitude, animalType, earsTagNo, zippath, recordeText, recordeMsg, videozippath, lipbaodanname, lipyanbaoname,
                        videoupsuccess, isforce, during);

                LiPeiLocalBeans.add(localData);
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        db.close();
        db = null;
        return LiPeiLocalBeans;
    }

    public boolean deleteLocalDataFromzippath(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "zippath=?";
        String[] selectionArgs = {path};
        int delete = db.delete(TABLE_LIPEI, selection, selectionArgs);
        Log.i("===delete===", delete + "");
        db.close();
        return delete > 0 ? true : false;
    }

    public boolean deleteLocalDataFromdate(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "insureDate=?";
        String[] selectionArgs = {date};
        int delete = db.delete(TABLE_LIPEI, selection, selectionArgs);
        Log.i("===delete===", delete + "");
        db.close();
        return delete > 0 ? true : false;
    }


    public int updateLiPeiLocalFromzipPath(String path, String lipeidate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("zippath", path);
        int update = db.update(TABLE_LIPEI, values, "insureDate=?", new String[]{lipeidate});
        db.close();
        return update;
    }

    public int updateLiPeiLocalFromVideozipPath(String path, String lipeidate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("videozippath", path);
        int update = db.update(TABLE_LIPEI, values, "insureDate=?", new String[]{lipeidate});
        db.close();
        return update;
    }

    public int updateLiPeiLocalFromrecordeText(String num, String lipeidate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("recordeText", num);
        int update = db.update(TABLE_LIPEI, values, "insureDate=?", new String[]{lipeidate});
        db.close();
        return update;
    }

    public int updateLiPeiLocalFromrecordeMsg(String msg, String lipeidate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("recordeMsg", msg);
        int update = db.update(TABLE_LIPEI, values, "insureDate=?", new String[]{lipeidate});
        db.close();
        return update;
    }

    public int updateLiPeiLocalFromDuring(String msg, String lipeidate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("during", msg);
        int update = db.update(TABLE_LIPEI, values, "insureDate=?", new String[]{lipeidate});
        db.close();
        return update;
    }

    public int updateLiPeiLocalUpSuccess(String videoupsuccess, String lipeidate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("videoupsuccess", videoupsuccess);
        int update = db.update(TABLE_LIPEI, values, "insureDate=?", new String[]{lipeidate});
        db.close();
        return update;
    }

    public int updateLiPeiLocalIsForce(String isforce, String lipeidate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isforce", isforce);
        int update = db.update(TABLE_LIPEI, values, "insureDate=?", new String[]{lipeidate});
        db.close();
        return update;
    }

    public void addLocalNongxianData(LocalModelNongxian localData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("baodanNo", localData.getBaodanNo());
        values.put("name", localData.getName());
        values.put("cardNo", localData.getCardNo());
        values.put("insureDate", localData.getInsureDate());
        values.put("animalType", localData.getType());
        values.put("yanBiaoName", localData.getYanBiaoName());
        values.put("userid", PreferencesUtils.getStringValue(HttpUtils.user_id, FarmAppConfig.getApplication()));
        values.put("baodanName", localData.getBaodanName());
        db.insert(TABLE_LOCAL, null, values);
        db.close();
    }

    public List<LocalModelNongxian> queryLocalDataFromBaodanNo(String baodanNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<LocalModelNongxian> LocalModellist = null;
        String[] columns = {"baodanNo", "name", "cardNo", "insureDate", "animalType", "yanBiaoName", "baodanName"};
        String selection = "baodanNo=?";
        String[] selectionArgs = {baodanNo};
        Cursor cursor = db.query(TABLE_LOCAL, columns, selection, selectionArgs,
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            LocalModellist = new ArrayList<LocalModelNongxian>();
            while (cursor.moveToNext()) {
                String baodanNo1 = cursor.getString(cursor.getColumnIndex("baodanNo"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String cardNo = cursor.getString(cursor.getColumnIndex("cardNo"));
                String insureDate = cursor.getString(cursor.getColumnIndex("insureDate"));
                String type = cursor.getString(cursor.getColumnIndex("animalType"));
                String yanBiaoName = cursor.getString(cursor.getColumnIndex("yanBiaoName"));
                String baodanName = cursor.getString(cursor.getColumnIndex("baodanName"));
                LocalModelNongxian localData = new LocalModelNongxian(baodanNo1, name, cardNo, insureDate, type, yanBiaoName, baodanName);
                LocalModellist.add(localData);
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        db.close();
        db = null;
        return LocalModellist;
    }

    public boolean deleteLocalDataFromBaodanNo(String baodanNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "baodanNo=?";
        String[] selectionArgs = {baodanNo};
        int delete = db.delete(TABLE_LOCAL, selection, selectionArgs);
        db.close();
        return delete > 0 ? true : false;
    }

    public List<LocalModelNongxian> queryLocalDatas(String muserid) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<LocalModelNongxian> listLocalDatas = new ArrayList<LocalModelNongxian>();
        if (db.isOpen()) {
            String[] columns = {"baodanNo", "name", "cardNo", "insureDate", "yanBiaoName", "baodanName"};
            Cursor cursor = db.query(TABLE_LOCAL, columns, "userid=? and animalType=?", new String[]{muserid, String.valueOf(PreferencesUtils.getAnimalType(context))}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String baodanNo = cursor.getString(cursor.getColumnIndex("baodanNo"));
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String cardNo = cursor.getString(cursor.getColumnIndex("cardNo"));
                    String insureDate = cursor.getString(cursor.getColumnIndex("insureDate"));
                    String yanBiaoName = cursor.getString(cursor.getColumnIndex("yanBiaoName"));
                    String baodanName = cursor.getString(cursor.getColumnIndex("baodanName"));
                    LocalModelNongxian localData = new LocalModelNongxian(baodanNo, name, cardNo, insureDate, String.valueOf(PreferencesUtils.getAnimalType(context)), yanBiaoName, baodanName);
                    listLocalDatas.add(localData);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return listLocalDatas;
    }

    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_PHONE_NUMBER, user.getPhoneNumber());
        values.put(COLUMN_USER_IDNUMBER, user.getIDNumber());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());

        db.insert(TABLE_USER, null, values);
        db.close();
    }

    public void addCompany(List<String> sqls) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_ID, company.getCompanyId());
//        values.put(COLUMN_NAME, company.getCompanyName());
//        values.put(COLUMN_PID, company.getCompanyPid());
//
//        db.insert(TABLE_COMPANY, null, values);
//        db.close();

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (String sql : sqls) {
                db.execSQL(sql);
            }
// 设置事务标志为成功，当结束事务时就会提交事务
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
// 结束事务
            db.endTransaction();
            db.close();
        }
    }

    public boolean checkUser(String phoneNumber, String password) {
        String[] columns = {COLUMN_USER_ID};
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_USER_PHONE_NUMBER + " = ?" + " AND " + COLUMN_USER_PASSWORD + " = ? ";
        String[] selectionArgs = {phoneNumber, password};

        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        if (cursorCount > 0) {
            return true;
        }
        return false;
    }

    public boolean checkUser(String phone_number) {
        String[] columns = {COLUMN_USER_ID};
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_USER_PHONE_NUMBER + " = ?";
        String[] selectionArgs = {phone_number};

        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        if (cursorCount > 0) {
            return true;
        }
        return false;
    }

    public List<company_total> queryProvince() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<company_total> listProvinces = new ArrayList<company_total>();
        if (db.isOpen()) {
            String[] columns = {"id", "fullname"};

            String selection = "pid=?";
            String[] selectionArgs = {"0"};
            Cursor cursor = db.query("tCompany", columns, selection,
                    selectionArgs, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int sCode = cursor.getInt(0);
                    String provinceName = cursor.getString(1);
                    company_total province = new company_total(sCode, provinceName);
                    listProvinces.add(province);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return listProvinces;
    }

    public List<company_child> queryCity(int code) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<company_child> cityList = null;
        String[] columns = {"id", "fullname"};

        String selection = "pid=?";
        String[] selectionArgs = {String.valueOf(code)};
        Cursor cursor = db.query("tCompany", columns, selection, selectionArgs,
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cityList = new ArrayList<company_child>();
            while (cursor.moveToNext()) {
                int sCode = cursor.getInt(0);
                String cName = cursor.getString(1);
                company_child city = new company_child();
                city.setCompanyId(sCode);
                city.setCompanyName(cName);
                cityList.add(city);
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        db.close();
        db = null;
        return cityList;
    }
}
