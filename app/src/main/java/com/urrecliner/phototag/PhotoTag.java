package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.utils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity (primaryKeys = {"fullFolder","photoName"})
public class PhotoTag implements Cloneable {

    @NonNull
    @ColumnInfo (name = "fullFolder")   // sdcard/DCIM/camera
    public String fullFolder;

    @NonNull
    @ColumnInfo (name = "photoName")    // 20220101_140159.jpg
    public String photoName;

    @ColumnInfo (name = "orient")  // 1-8
    public String orient;

    public boolean isChecked;
    @ColumnInfo (name = "sumNailMap")
    public String sumNailMap;

//    public PhotoTag(String fullFolder, String photoName) {
//        this.fullFolder = fullFolder;
//        this.photoName = photoName;
//        this.orient = "x";
//        this.isChecked = false;
//        this.sumNailMap = null;
//    }
//
    public String getFullFolder() { return fullFolder; }
    public void setFullFolder(String fullFolder) { this.fullFolder = fullFolder; }

    public String getPhotoName() { return photoName; }
    public void setPhotoName(String photoName) { this.photoName = photoName; }

    public String getOrient() { return orient; }
    public void setOrient(String orient) { this.orient = orient; }

    public Bitmap getSumNailMap() { return (sumNailMap == null) ? null: utils.StringToBitMap(sumNailMap); }
    public void setSumNailMap(Bitmap sumNailMap) { this.sumNailMap = utils.BitMapToString(sumNailMap);
    }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }

    public PhotoTag clone() throws CloneNotSupportedException {
        return (PhotoTag) super.clone();
    }
}