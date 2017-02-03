package com.voidpointer.selfgauge;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;

/**
 * Created by SHIN on 2016-08-16.
 */
public class DBHelper {
    private static final String DATABASE_NAME = "selfgauge.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mContext;

    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(Tables.Usage._CREATE_STRING);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+Tables.Usage._TABLENAME);
            onCreate(sqLiteDatabase);
        }
    }

    public DBHelper(Context context) {
        mContext = context;
    }

    public DBHelper open() throws SQLException {
        File sd = Environment.getExternalStorageDirectory();
        String strPackage = mContext.getPackageName();
        String dbDir = sd.toString() + "/" + strPackage + "/database";
        String dbPath = dbDir + "/" + DATABASE_NAME;

        File fileDir = new File(dbDir);
        if (!fileDir.exists()) {
            // 디렉토리가 존재하지 않으면 디렉토리 생성
            fileDir.mkdirs();
        }

        mDBHelper = new DatabaseHelper(mContext, dbPath, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        mDB.close();
    }

    //DATE: long not null
    //TYPE: text not null
    //USAGE: integer not null
    //DELETED: text not null

    // Insert DB
    public long insertColumn(long datetime, String type, int usage, String deleted ){
        ContentValues values = new ContentValues();
        values.put(Tables.Usage.DATE, datetime);
        values.put(Tables.Usage.TYPE, type);
        values.put(Tables.Usage.USAGE, usage);
        values.put(Tables.Usage.DELETED, deleted);
        return mDB.insert(Tables.Usage._TABLENAME, null, values);
    }

    // Update DB
    public boolean updateColumn(long id, long datetime, String type, int usage, String deleted){
        ContentValues values = new ContentValues();
        values.put(Tables.Usage.DATE, datetime);
        values.put(Tables.Usage.TYPE, type);
        values.put(Tables.Usage.USAGE, usage);
        values.put(Tables.Usage.DELETED, deleted);
        return mDB.update(Tables.Usage._TABLENAME, values, "_id="+id, null) > 0;
    }

    // Delete ID
    public boolean deleteColumn(long id){
        return mDB.delete(Tables.Usage._TABLENAME, "_id="+id, null) > 0;
    }

    // Delete Contact
    public boolean deleteColumn(String number){
        return mDB.delete(Tables.Usage._TABLENAME, "contact="+number, null) > 0;
    }

    // Select All
    public Cursor getAllColumns(){
        return mDB.query(Tables.Usage._TABLENAME, null, null, null, null, null, "date asc");
    }

    public Cursor getRangeColumns(Calendar calStart, Calendar calEnd){
        String selection = String.format("%s >= %d AND %s <= %d",
                Tables.Usage.DATE, calStart.getTimeInMillis(),
                Tables.Usage.DATE, calEnd.getTimeInMillis());
        Log.v("ywshin", String.format("[getRangeColumns] %s", selection));
        return mDB.query(Tables.Usage._TABLENAME, null, selection, null, null, null, "date asc");
    }

    // ID 컬럼 얻어 오기
    public Cursor getColumn(long id){
        Cursor c = mDB.query(Tables.Usage._TABLENAME, null,
                "_id="+id, null, null, null, null);
        if(c != null && c.getCount() != 0)
            c.moveToFirst();
        return c;
    }

    // 이름 검색 하기 (rawQuery)
    public Cursor getMatch(long datetime, String type){
        String query = String.format("SELECT * FROM %s WHERE %s=%d AND %s='%s'", Tables.Usage._TABLENAME, Tables.Usage.DATE, datetime, Tables.Usage.TYPE, type );
        Log.v("ywshin", query);
        Cursor c = mDB.rawQuery( query, null );
        if(c != null && c.getCount() != 0) {
            c.moveToFirst();
        }
        return c;
    }

    public void exportDB() {
        try {
            File data = Environment.getDataDirectory();
            File sd = Environment.getExternalStorageDirectory();
            String strPackage = mContext.getPackageName();

            if (sd.canWrite()) {
                String currentDBPath = data.toString() + "/" + strPackage + "/databases/" + DATABASE_NAME;
                String backupDir = sd.toString() + "/" + strPackage;
                String backupDBPath = "/" + strPackage + "/" + DATABASE_NAME;

                File fileDir = new File(backupDir);
                if ( !fileDir.exists() ){
                    // 디렉토리가 존재하지 않으면 디렉토리 생성
                    fileDir.mkdir();
                }

                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(mContext, "데이터를 백업했어요.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        catch (Exception e) {
            String errorMessage = e.getMessage();
            Toast.makeText(mContext, "데이터 백업을 실패했어요.", Toast.LENGTH_LONG)
                 .show();
        }
    }

    /*
    private void importDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "//data//" + "<package name>"
                        + "//databases//" + "<database name>";
                String backupDBPath = "<backup db filename>"; // From SD directory.
                File backupDB = new File(data, currentDBPath);
                File currentDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(backupDB).getChannel();
                FileChannel dst = new FileOutputStream(currentDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getApplicationContext(), "Import Successful!",
                        Toast.LENGTH_SHORT).show();

            }
        } catch (Exception e) {

            Toast.makeText(getApplicationContext(), "Import Failed!", Toast.LENGTH_SHORT)
                    .show();

        }
    }
    */




}