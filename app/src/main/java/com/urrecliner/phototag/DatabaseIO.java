package com.urrecliner.phototag;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.utils;

public class DatabaseIO extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "phototag.db";
    private static final String TABLE_NAME = "phototag";
    private static final int SCHEMA_VERSION = 1;
    private static final String logID = "dbIO";
    private static SQLiteDatabase dbIO = null;

    DatabaseIO() {
        super(mContext, utils.getPackageDirectory().toString()+ "/" + DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        create_Table(db);
    }

    private void create_Table(SQLiteDatabase db) {
        String sqlCommand = "CREATE TABLE if not exists " + TABLE_NAME + " " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +    // 0
                "fullFileName TEXT," +  // 1
                " orientation INTEGER, " + // 2
                " bitMap LONGTEXT) ;"; // 3
        db.execSQL(sqlCommand);
        utils.log(logID, "onCreate, sqlCommand: " + sqlCommand);
    }

    void insert(Photo photo) {
        if (dbIO == null)
            dbIO = getWritableDatabase();
        // verify if exits
        String fullFileName = photo.getFullFileName().toString();
        String sqlCommand = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                "fullFileName=\""+fullFileName+"\";";
        Cursor result;
        try {
            result = dbIO.rawQuery(sqlCommand, null);
            if (result.getCount() >= 0) {
                int id = result.getInt(0);
                ContentValues cv = new ContentValues();
                cv.put("fullFileName", fullFileName);
                cv.put("orientation", photo.getOrientation());
                cv.put("bitMap", BitMapToString(photo.getBitmap()));
                dbIO.update(TABLE_NAME, cv, "_id=" + id, null);
                utils.log("Update "+id, ""+photo.getShortName());
            }
        } catch (Exception e) {
//            not found utils.logE(logID, " retrievePhoto exception "+fullFileName+" "+e.toString());
        }

        ContentValues cv = new ContentValues();
        String bitMap = BitMapToString(photo.getBitmap());
        int orientation = photo.getOrientation();

        cv.put("fullFileName", fullFileName);
        cv.put("orientation", orientation);
        cv.put("bitMap", bitMap);
        dbIO.insert(TABLE_NAME, null, cv);
//        utils.log("Inert "+newCount++,"new "+photo.getShortName());
    }

    void delete(File fullFileName) {
        if (dbIO == null)
            dbIO = getWritableDatabase();
        String[] args = {fullFileName.toString()};
        dbIO.delete(TABLE_NAME, "fullFileName=?", args);
    }

    Photo retrievePhoto(Photo photo) {

        if (dbIO == null)
            dbIO = getWritableDatabase();
        String fullFileName = photo.getFullFileName().toString();
        String sqlCommand = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                "fullFileName=\""+fullFileName+"\";";
        Cursor result;
        try {
            result = dbIO.rawQuery(sqlCommand, null);
        } catch (Exception e) {
            utils.log(logID, " retrievePhoto exception "+fullFileName+" "+e.toString());
            return photo;
        }
        if (result.getCount() == 0)
            return photo;
        result.moveToFirst();
        photo.setOrientation(result.getInt(2));
        photo.setBitmap(StringToBitMap(result.getString(3)));
        return photo;
    }

    Bitmap retrieveBitMap (String fullFileName) {

        if (dbIO == null)
            dbIO = getWritableDatabase();
        String sqlCommand = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                "fullFileName=\""+fullFileName+"\";";
        Cursor result;
        try {
            result = dbIO.rawQuery(sqlCommand, null);
        } catch (Exception e) {
            utils.log(logID, " retrievePhoto exception "+fullFileName+" "+e.toString());
            return null;
        }
        if (result.getCount() == 0)
            return null;
//        utils.log(logID, "result deleteCount "+result.getCount());
        result.moveToFirst();
        return StringToBitMap(result.getString(3));
    }

    Cursor retrieveAll() {
        if (dbIO == null)
            dbIO = getWritableDatabase();
        String sqlCommand = "SELECT * FROM " + TABLE_NAME + " ;";
        try {
            return dbIO.rawQuery(sqlCommand, null);
        } catch (Exception e) {
            utils.logE(logID, " retrievePhoto All exception ");
            return null;
        }
    }

    public void close() {
        dbIO.close();
    }

    private String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            utils.log(logID, " StringToBitMap Error "+e.toString());
            return null;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override protected void finalize() throws Throwable { this.close(); super.finalize(); }

}