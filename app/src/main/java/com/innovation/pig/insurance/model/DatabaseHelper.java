package com.innovation.pig.insurance.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luolu on 08/01/2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABSAE_VERSION = 1;
    private static final String DATABASE_NAME = "pigger.db";
    private static final String TABLE_LOCAL = "tPig";

    //private String CREATE_LOCAL_TABLE = "CREATE TABLE " + TABLE_LOCAL + "(id integer primary key autoincrement,pigHouse text not null,pigzhujuan text not null,zhusheId text not null,zhujuanId text not null)";
    private String CREATE_LOCAL_TABLE = "CREATE TABLE " + TABLE_LOCAL + "(id integer primary key autoincrement,pigHouse text not null,pigzhujuan text not null,zhusheId text not null,zhujuanId text not null)";

    private String DROP_LOCAL_TABLE = "DROP TABLE IF EXISTS " + TABLE_LOCAL;

    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABSAE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_LOCAL_TABLE);
        onCreate(db);
    }

    public void addPig(PigBean pigBean) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pigHouse", pigBean.pigHouse);
        values.put("pigzhujuan", pigBean.pigzhujuan);
        values.put("zhusheId", pigBean.zhusheId);
        values.put("zhujuanId", pigBean.zhujuanId);
        db.insert(TABLE_LOCAL, null, values);
        db.close();
    }
    public List<PigBean> queryPigFromPigHouse() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<PigBean> Piglists = null;
        Cursor cursor = db.query(TABLE_LOCAL, null, null, null,
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            Piglists = new ArrayList<PigBean>();
            while (cursor.moveToNext()) {
                String pigzhujuan = cursor.getString(cursor.getColumnIndex("pigzhujuan"));
                String zhusheId = cursor.getString(cursor.getColumnIndex("zhusheId"));
                String zhujuanId = cursor.getString(cursor.getColumnIndex("zhujuanId"));
               // String count = cursor.getString(cursor.getColumnIndex("count"));
                String pigHouse = cursor.getString(cursor.getColumnIndex("pigHouse"));
                PigBean pigBean = new PigBean(pigHouse, pigzhujuan, zhusheId, zhujuanId);
                Piglists.add(pigBean);
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        db.close();
        db = null;
        return Piglists;
    }
    public List<PigBean> queryPigFromPigHouse(String paghouse) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<PigBean> Piglists = null;
        String[] columns = {"pigHouse","pigzhujuan", "zhusheId", "zhujuanId"};
        String selection = "pigHouse=?";
        String[] selectionArgs = {paghouse};
        Cursor cursor = db.query(TABLE_LOCAL, columns, selection, selectionArgs,
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            Piglists = new ArrayList<PigBean>();
            while (cursor.moveToNext()) {
                String pigzhujuan = cursor.getString(cursor.getColumnIndex("pigzhujuan"));
                String zhusheId = cursor.getString(cursor.getColumnIndex("zhusheId"));
                String zhujuanId = cursor.getString(cursor.getColumnIndex("zhujuanId"));
               // String count = cursor.getString(cursor.getColumnIndex("count"));
                String pigHouse = cursor.getString(cursor.getColumnIndex("pigHouse"));
                PigBean pigBean = new PigBean(pigHouse, pigzhujuan, zhusheId, zhujuanId);
                Piglists.add(pigBean);
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        db.close();
        db = null;
        return Piglists;
    }
    public List<PigBean> queryZhuSheDatas() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<PigBean> pigBeans = new ArrayList<PigBean>();
        if (db.isOpen()) {
            String[] columns = {"pigHouse","pigzhujuan", "zhusheId", "zhujuanId"};

            Cursor cursor = db.query(TABLE_LOCAL, columns, null,
                    null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String pigzhujuan = cursor.getString(cursor.getColumnIndex("pigzhujuan"));
                    String zhusheId = cursor.getString(cursor.getColumnIndex("zhusheId"));
                    String zhujuanId = cursor.getString(cursor.getColumnIndex("zhujuanId"));
                    //String count = cursor.getString(cursor.getColumnIndex("count"));
                    String paghouse = cursor.getString(cursor.getColumnIndex("pigHouse"));
                    PigBean pigBean = new PigBean(paghouse, pigzhujuan, zhusheId, zhujuanId);
                    pigBeans.add(pigBean);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return pigBeans;
    }

    public List<String> queryZhuSheid() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> strings = new ArrayList<String>();
        if (db.isOpen()) {
            String[] columns = {"zhusheId"};

            Cursor cursor = db.query(TABLE_LOCAL, columns, null,
                    null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String pigHouse = cursor.getString(cursor.getColumnIndex("zhusheId"));
                    strings.add(pigHouse);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return strings;
    }

    public List<String> queryZhujuanid() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> strings = new ArrayList<String>();
        if (db.isOpen()) {
            String[] columns = {"zhujuanId"};

            Cursor cursor = db.query(TABLE_LOCAL, columns, null,
                    null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String pigHouse = cursor.getString(cursor.getColumnIndex("zhujuanId"));
                    strings.add(pigHouse);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return strings;
    }
    public List<PigBean> queryPigFromPigHouse(String zhusheid,String pigzhujuanid) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<PigBean> Piglists = null;
        String[] columns = {"zhusheId","pigzhujuan", "zhujuanId", "pigHouse"};
    //    String selection = "zhusheId=?";
        String selection = "zhusheId=? and zhujuanId=?";
        String[] selectionArgs = {zhusheid,pigzhujuanid};
        Cursor cursor = db.query(TABLE_LOCAL, columns, selection, selectionArgs,
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            Piglists = new ArrayList<PigBean>();
            while (cursor.moveToNext()) {
                String pigzhujuan = cursor.getString(cursor.getColumnIndex("pigzhujuan"));
                String zhusheId = cursor.getString(cursor.getColumnIndex("zhusheId"));
                String zhujuanId = cursor.getString(cursor.getColumnIndex("zhujuanId"));
               // String count = cursor.getString(cursor.getColumnIndex("count"));
                String paghouse = cursor.getString(cursor.getColumnIndex("pigHouse"));
                PigBean pigBean = new PigBean(paghouse, pigzhujuan, zhusheId, zhujuanId);
               // pigBean.setCount(count);
                Piglists.add(pigBean);
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        db.close();
        db = null;
        return Piglists;
    }
    public int checkPigFromPigcount(String zhujuanId, String mcount) {
        ContentValues values = new ContentValues();
        values.put("count", mcount);
        SQLiteDatabase db = this.getWritableDatabase();
        int update = db.update(TABLE_LOCAL, values, "zhujuanId=?", new String[]{zhujuanId});
        db.close();
        return update;
    }

    public void deletePig() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCAL, null, null);
        db.close();
    }
   /* public boolean deleteLocalDataFromBaodanNo(String baodanNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "baodanNo=?";
        String[] selectionArgs = {baodanNo};
        int delete = db.delete(TABLE_LOCAL, selection, selectionArgs);
        db.close();
        return delete>0?true:false;
    }

    public List<LocalModel> queryLocalDatas() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<LocalModel> listLocalDatas = new ArrayList<LocalModel>();
        if (db.isOpen()) {
            String[] columns = {"baodanNo", "name", "cardNo", "insureDate","animalType"};

            Cursor cursor = db.query(TABLE_LOCAL, columns, null,
                    null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String baodanNo = cursor.getString(cursor.getColumnIndex("baodanNo"));
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String cardNo = cursor.getString(cursor.getColumnIndex("cardNo"));
                    String insureDate = cursor.getString(cursor.getColumnIndex("insureDate"));
                    String type = cursor.getString(cursor.getColumnIndex("animalType"));
                    LocalModel localData = new LocalModel(baodanNo, name, cardNo, insureDate, type);
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
//        values.put(COLUMN_ID, company.geten_id());
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
                city.seten_id(sCode);
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
    }*/
}
