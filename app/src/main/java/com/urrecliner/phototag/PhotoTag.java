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
    public String photoName;        // if orient == 9, last modified date

    @ColumnInfo (name = "orient")  // 1-8       if orient == 9, then it is directory
    public String orient;

    public boolean isChecked;

    @ColumnInfo (name = "sumNailMap")
    public String sumNailMap;       // photo sumNail or directory sumNail

    public Bitmap getSumNailMap() { return (sumNailMap == null) ? null: utils.StringToBitMap(sumNailMap); }
    public void setSumNailMap(Bitmap sumNailMap) { this.sumNailMap = utils.BitMapToString(sumNailMap);
    }

    public PhotoTag clone() throws CloneNotSupportedException {
        return (PhotoTag) super.clone();
    }
}