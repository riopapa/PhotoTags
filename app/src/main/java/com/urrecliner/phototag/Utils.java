package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.mActivity;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.sharedAlpha;
import static com.urrecliner.phototag.Vars.sharedAutoLoad;
import static com.urrecliner.phototag.Vars.sharedPref;
import static com.urrecliner.phototag.Vars.sharedRadius;
import static com.urrecliner.phototag.Vars.sharedSigNbr;
import static com.urrecliner.phototag.Vars.sharedSort;
import static com.urrecliner.phototag.Vars.sharedSpan;
import static com.urrecliner.phototag.Vars.short1Folder;
import static com.urrecliner.phototag.Vars.short2Folder;
import static com.urrecliner.phototag.Vars.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

class Utils {

    final private String PREFIX = "log_";

    void log(String tag, String text) {
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        String log = traceName(traces[6].getMethodName()) + traceName(traces[5].getMethodName()) + traceName(traces[4].getMethodName()) + traceClassName(traces[3].getClassName())+"> "+traces[3].getMethodName() + "#" + traces[3].getLineNumber() + " {"+ tag + "} " + text;
        Log.w(tag , log);
        append2file(sdfDateTimeLog.format(new Date())+" " +log);
    }

    private String traceName (String s) {
        if (s.equals("performResume") || s.equals("performCreate") || s.equals("callActivityOnResume") || s.equals("access$1200")
                || s.equals("access$000") || s.equals("handleReceiver") || s.equals("handleMessage") || s.equals("dispatchMessage"))
            return "";
        else
            return s + "> ";
    }

    private final SimpleDateFormat sdfDateTimeLog = new SimpleDateFormat("MM-dd HH.mm.ss sss", Locale.US);
    private static final SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd", Locale.US);

    private String traceClassName(String s) {
        return s.substring(s.lastIndexOf(".")+1);
    }

    void logE(String tag, String text) {
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        String log = traceName(traces[5].getMethodName()) + traceName(traces[4].getMethodName()) + traceClassName(traces[3].getClassName())+"> "+traces[3].getMethodName() + "#" + traces[3].getLineNumber() + " {"+ tag + "} " + text;
        Log.e("<" + tag + ">" , log);
        append2file(sdfDateTimeLog.format(new Date())+" : " +log);
    }

    private void append2file(String textLine) {

        File directory = getPackageDirectory();
        BufferedWriter bw = null;
        FileWriter fw = null;
        String fullName = directory.toString() + "/" + PREFIX + sdfDate.format(new Date())+".txt";
        try {
            File file = new File(fullName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    logE("createFile", " Error");
                }
            }
            String outText = "\n"+textLine+"\n";
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(outText);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    File getPackageDirectory() {
        File directory = new File(Environment.getExternalStorageDirectory(),  getAppLabel(mContext));
        try {
            if (!directory.exists()) {
                if(directory.mkdirs()) {
                    Log.e("mkdirs","Failed "+directory);
                }
            }
        } catch (Exception e) {
            Log.e("creating Directory error", directory + "_" + e);
        }
        return directory;
    }

    ArrayList <String> getFilteredFileNames(String fullPath) {
        String[] shortFileNames = new File(fullPath).list((dir, name) ->
                ((name.endsWith("jpg") || name.endsWith("JPG")
                        || name.endsWith("png") || name.endsWith("PNG")) && !name.startsWith(".")));
        ArrayList<String> sortedFileList = new ArrayList<>();
        if (shortFileNames != null)
            sortedFileList.addAll(Arrays.asList(shortFileNames));
        return sortedFileList;
    }

    int getPhotoCount(String fullPath) {
        String[] shortFileNames = new File(fullPath).list((dir, name) ->
                ((name.endsWith("jpg") || name.endsWith("JPG")
                        || name.endsWith("png") || name.endsWith("PNG")) && !name.startsWith(".")));
        if (shortFileNames != null)
            return shortFileNames.length;
        else
            return 0;
    }

    private String getAppLabel(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    long getFileDate(File file) {

        String dateTimeS = null;
        ExifInterface exif;
        try {
            exif = new ExifInterface(file.getAbsolutePath());
            dateTimeS = exif.getAttribute(ExifInterface.TAG_DATETIME);
        } catch (IOException e) {
//            return 0;
        }
        if (dateTimeS != null) {
            try {
                Date date = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).parse(dateTimeS);
                return date.getTime() - 5000;
            } catch (ParseException e) {
                Log.e("Parse","Exception dateTimeS "+dateTimeS);
                return System.currentTimeMillis();
            }
        }
        dateTimeS = file.getName();
        String prefixStr = dateTimeS.substring(0, 4);
        if (!prefixStr.equals("IMG_")){
            dateTimeS = dateTimeS.substring(4);
            Log.w("new datetime",dateTimeS);
        }

        String regex = "^\\d";
        String numbers = dateTimeS.replaceAll(regex, "");
        numbers = numbers.substring(0,14);
        try {
            Date date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).parse(numbers);
            return date.getTime() - 5000;
        } catch (ParseException e) {
            Log.e("Parse","Exception numbers "+numbers);
            return new Date(file.lastModified()).getTime();
        }
    }

    void createPhotoFile(String folderName, String inpName, String outName, Bitmap bitmap) {
        File file = new File(folderName, outName);
        if (file.exists())
            file.delete();
        bitMap2File(bitmap, file);
        copyExif(folderName, inpName, outName);
        MediaScannerConnection.scanFile(mContext,
                new String[]{file.getPath()}, null, null);
    }

    private void bitMap2File(Bitmap bitmap, File file) {
        FileOutputStream os;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();

        } catch (IOException e) {
            utils.logE("ioException", e.toString());
            Toast.makeText(mActivity, e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    void copyExif(String folderName, String inpName, String outName) {
        File inpFile = new File(folderName, inpName);
        File outFile = new File(folderName, outName);

        String[] attributes = new String[]
                {
                        ExifInterface.TAG_DATETIME,
                        ExifInterface.TAG_DATETIME_DIGITIZED,
                        ExifInterface.TAG_RW2_ISO,
                        ExifInterface.TAG_EXPOSURE_TIME,
                        ExifInterface.TAG_EXPOSURE_INDEX,
                        ExifInterface.TAG_F_NUMBER,
                        ExifInterface.TAG_FOCAL_LENGTH,
                        ExifInterface.TAG_FLASH,
                        ExifInterface.TAG_FOCAL_LENGTH,
                        ExifInterface.TAG_GPS_ALTITUDE,
                        ExifInterface.TAG_GPS_ALTITUDE_REF,
                        ExifInterface.TAG_GPS_DATESTAMP,
                        ExifInterface.TAG_GPS_LATITUDE,
                        ExifInterface.TAG_GPS_LATITUDE_REF,
                        ExifInterface.TAG_GPS_LONGITUDE,
                        ExifInterface.TAG_GPS_LONGITUDE_REF,
                        ExifInterface.TAG_GPS_PROCESSING_METHOD,
                        ExifInterface.TAG_ISO_SPEED,
                        ExifInterface.TAG_GPS_TIMESTAMP,
                        ExifInterface.TAG_MAKE,
                        ExifInterface.TAG_MODEL,
//                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.TAG_SUBSEC_TIME,
                        ExifInterface.TAG_WHITE_BALANCE
                };


        ExifInterface oldExif = null;
        ExifInterface newExif = null;
        try {
            oldExif = new ExifInterface(inpFile);
            newExif = new ExifInterface(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String attribute : attributes) {
            String value = oldExif.getAttribute(attribute);
            if (value != null)
                newExif.setAttribute(attribute, value);
        }
        newExif.setAttribute(ExifInterface.TAG_ORIENTATION,"1");
        try {
            newExif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Timer().schedule(new TimerTask() {
            public void run() {
                outFile.setLastModified(inpFile.lastModified());
            }
        }, 500);
    }

    void deleteOldLogFiles() {

        String oldDate = PREFIX + sdfDate.format(System.currentTimeMillis() - 2*24*60*60*1000L);
        File[] files = getPackageDirectory().listFiles((dir, name) -> name.endsWith(".txt"));
        if (files != null) {
            Collator myCollator = Collator.getInstance();
            for (File file : files) {
                String shortFileName = file.getName();
                if (myCollator.compare(shortFileName, oldDate) < 0) {
                    if (file.delete())
                        utils.log("delete old log",shortFileName);
                    else
                        Log.e("file", "Delete Error " + file);
                }
            }
        }
    }

    void setShortFolderNames(String folderName) {
        String[] s = folderName.split("/");
        int len = s.length;
        if (len >= 2) {
            short1Folder = s[len-2];
            short2Folder = s[len-1];
        } else {
            short1Folder = "";
            short2Folder = s[0];
        }
    }

    void showFolder (ActionBar actionBar) {
        if (short1Folder.equals("0")) {
            actionBar.setTitle(short2Folder);
            actionBar.setSubtitle("");
        }
        else {
            actionBar.setTitle(short1Folder);
            actionBar.setSubtitle(short2Folder);
        }
    }

    void getPreference() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedRadius = sharedPref.getString("radius", "");
        if (sharedRadius.equals("")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("radius", "200");
            editor.putBoolean("autoLoad", true);
            editor.putString("sort", "none");
            editor.putString("span","3");
            editor.putString("alpha","180");
            editor.putInt("signature", 0);
            editor.apply();
        }
        sharedRadius = sharedPref.getString("radius", "200");
        sharedAutoLoad = sharedPref.getBoolean("autoLoad", false);
        sharedSort = sharedPref.getString("sort", "none");
        sharedSpan = sharedPref.getString("span","3");
        sharedAlpha = sharedPref.getString("alpha","180");
        sharedSigNbr = sharedPref.getInt("signature", 0);
    }

    String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,99, baos);
        byte [] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            utils.log("utils", " StringToBitMap Error "+e);
            return null;
        }
    }

}