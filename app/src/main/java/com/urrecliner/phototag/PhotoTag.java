package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.utils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity (primaryKeys = {"fullFolder","photoName"})
public class PhotoTag implements Cloneable {

    @NonNull
    @ColumnInfo (name = "fullFolder")   // sdcard/DCIM/camera
    public String fullFolder;
    // if fullFolder starts with @ then it means this if folder row

    @NonNull
    @ColumnInfo (name = "photoName")    // 20220101_140159.jpg
    public String photoName;        // if orient == 9, last modified date

    @ColumnInfo (name = "orient")  // 1-8       if orient == 9, then it is directory
    public String orient;

    public boolean isChecked;

    @ColumnInfo (name = "thumbnail")
    public String thumbnail;       // photo thumbnail or directory thumbnail

    public Bitmap getThumbnail() { return (thumbnail == null) ? null: utils.StringToBitMap(thumbnail); }
    public void setThumbnail(Bitmap thumbnail) { this.thumbnail = utils.BitMapToString(thumbnail);
    }

    public PhotoTag clone() throws CloneNotSupportedException {
        return (PhotoTag) super.clone();
    }
}