package com.urrecliner.phototag;

import android.graphics.Bitmap;

import java.io.File;

import static com.urrecliner.phototag.Vars.databaseIO;

class Photo {
    private File fullFileName;
    private String shortName;
    private boolean checked;
    private int orientation;
    private Bitmap bitMap;

    Photo(File fullFileName) {
        this.fullFileName = fullFileName;
        this.shortName = fullFileName.getName();
        this.checked = false;
        this.orientation = 99;
        this.bitMap = null;
    }

    File getFullFileName() {
        return fullFileName;
    }
    void setFullFileName(File fullFileName) {
        this.fullFileName = fullFileName;
        this.shortName = fullFileName.getName();
    }

    int getOrientation() { return orientation; }

    void setOrientation(int orientation) { this.orientation = orientation; }

    String getShortName() {
        return shortName;
    }

    boolean isChecked() {
        return checked;
    }
    void setChecked(boolean checked) {
        this.checked = checked;
    }

    Bitmap getBitmap () {
        if (bitMap != null)
            return bitMap;
        return databaseIO.retrieveBitMap(fullFileName.toString());
    }
    void setBitmap (Bitmap bitmap) {this.bitMap = bitmap; }
}