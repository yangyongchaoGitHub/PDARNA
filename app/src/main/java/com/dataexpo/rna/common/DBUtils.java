package com.dataexpo.rna.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.dataexpo.rna.pojo.SignData;

import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class DBUtils {
    private final String TAG = DBUtils.class.getSimpleName();
    private final String dbname = "zmtdata";
    private final String dbnamePath = "/zmt.db";
    private SQLiteDatabase db;

    private static class HolderClass {
        private static final DBUtils instance = new DBUtils();
    }

    /**
     * 单例模式
     */
    public static DBUtils getInstance() {
        return HolderClass.instance;
    }

    /**
     * 创建数据表
     * @param contenxt 上下文对象
     */
    public void create(Context contenxt) {
        String path = contenxt.getDir("databases", MODE_PRIVATE).getPath() + dbnamePath;
        Log.i(TAG, "path========="+ path);
        db = SQLiteDatabase.openOrCreateDatabase(path, null);
        String sql = "create table if not exists " + dbname +
                "(id integer primary key autoincrement," +
                "eucode nchar(64),time nchar(64),name nchar(64), idcard nchar(64), address nchar(200), temperature nchar(32), modetype int)";
        db.execSQL(sql);//创建表
    }

    /**
     * 添加数据
     * bsid 添加的数据ID
     * name 添加数据名称
     */
    public long insertData(String eucode, String time, String name, String idcard, String temperature, Integer modetype, String address) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("eucode", eucode);
        contentValues.put("time", time);
        contentValues.put("name", name);
        contentValues.put("idcard", idcard);
        contentValues.put("temperature", temperature);
        contentValues.put("modetype", modetype);
        contentValues.put("address", address);
        //Log.i(TAG, "insertData====" + eucode + " == " + eufilecode  + " == " + printtime);
        return db.insert(dbname, null, contentValues);
    }

    public long insertData(SignData saveData) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("eucode", saveData.getEucode());
        contentValues.put("time", saveData.getTime());
        contentValues.put("name", saveData.getName());
        contentValues.put("idcard", saveData.getIdcard());
        contentValues.put("temperature", saveData.getTemp());
        contentValues.put("modetype", saveData.getModeType());
        contentValues.put("address", saveData.getAddress());
        return db.insert(dbname, null, contentValues);
        //Log.i(TAG, "insertData====" + eucode + " == " + eufilecode  + " == " + printtime);
        //return dataSize;
    }

    /**
     * 查询离线模式保存的数据
     * 返回List
     */
    public ArrayList<SignData> listAllOffLine() {
        ArrayList<SignData> list = new ArrayList<>();
        Cursor cursor = db.query(dbname, null, "modetype = ?", new String[]{"0"}, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String eucode = cursor.getString(cursor.getColumnIndex("eucode"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String idcard = cursor.getString(cursor.getColumnIndex("idcard"));
            String temperature = cursor.getString(cursor.getColumnIndex("temperature"));
            Integer modetype = cursor.getInt(cursor.getColumnIndex("modetype"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            list.add(new SignData(id, eucode, time, name, idcard, temperature, modetype, address));

            //Log.i(TAG, "selectis=========" + id + "==" + eucode + "==" + printtime + " == " + eufilecode);
        }
        cursor.close();
        return list;
    }

    /**
     * 查询在线模式保存的数据
     * 返回List
     */
    public ArrayList<SignData> listAllOnLine() {
        ArrayList<SignData> list = new ArrayList<>();
        Cursor cursor = db.query(dbname, null, "modetype = ?", new String[]{"1"}, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String eucode = cursor.getString(cursor.getColumnIndex("eucode"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String idcard = cursor.getString(cursor.getColumnIndex("idcard"));
            String temperature = cursor.getString(cursor.getColumnIndex("temperature"));
            Integer modetype = cursor.getInt(cursor.getColumnIndex("modetype"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            list.add(new SignData(id, eucode, time, name, idcard, temperature, modetype, address));

            //Log.i(TAG, "selectis=========" + id + "==" + eucode + "==" + printtime + " == " + eufilecode);
        }
        cursor.close();
        return list;
    }

    /**
     * 根据ID删除数据
     * id 删除id
     */
    public int delData(int id) {
        //Log.e(TAG, "id==============" + id);
        int inde = db.delete(dbname, "id = ?", new String[]{String.valueOf(id)});
        //Log.e(TAG, "删除了==============" + inde);
        return inde;
    }

    /**
     * 根据
     *
     */
    public int delDataAllOffline() {
        int inde = db.delete(dbname,"modetype = ?", new String[]{"0"});
        Log.e("--Main--", "删除了==============" + inde);
        return inde;
    }

    public int delDataAllOnline() {
        int inde = db.delete(dbname,"modetype = ?", new String[]{"1"});
        Log.e("--Main--", "删除了==============" + inde);
        return inde;
    }

    /**
     * 根据ID修改数据
     * id 修改条码的id
     * bsid 修改的ID
     * name 修改的数据库
     */
    public int modifyData(int id, int bsid, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("bsid", id);
        int index = db.update(dbname, contentValues, "id = ?", new String[]{String.valueOf(id)});
        Log.e("--Main--", "修改了===============" + index);
        return index;
    }

    /**
     * 查询code单个数据
     * @param code
     * @return
     */
    public boolean selectisData(String code) {
        //查询数据库
        Cursor cursor = db.query(dbname, null, "eucode = ?", new String[]{code}, null, null, null);
        while (cursor.moveToNext()) {
            return true;
        }
        return false;
    }

    public int count(String code) {
        int result = 0;
        Cursor cursor = db.query(dbname, null, "eufilecode = ?", new String[]{code}, null, null, null);
        while (cursor.moveToNext()) {
            result++;
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    public int countToDay() {
        int result = 0;
        String printtime = Utils.formatTime(new Date().getTime(), "yyyy-MM-dd");
        //tv_welcome.setText(date);

        Cursor cursor = db.query(dbname, new String[]{"time"}, "time like ? ",
                new String[]{"%" + printtime + "%"}, null, null, null);
        while (cursor.moveToNext()) {
            result++;
        }
        cursor.close();

        return result;
    }
}