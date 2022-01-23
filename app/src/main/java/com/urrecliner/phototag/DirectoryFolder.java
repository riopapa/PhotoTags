package com.urrecliner.phototag;

import android.graphics.Bitmap;

class DirectoryFolder {

    private  String longFolder;
    private int numberOfPics = 0;
    private Bitmap imageBitmap;

    public DirectoryFolder(){
        imageBitmap = null;
    }

    String getLongFolder() {
        return longFolder;
    }
    void setLongFolder(String longFolder) {
        this.longFolder = longFolder;
    }

    int getNumberOfPics() {
        return numberOfPics;
    }
    void setNumberOfPics(int numberOfPics) {
        this.numberOfPics = numberOfPics;
    }

    void setImageBitmap (Bitmap bitmap) { this.imageBitmap = bitmap;}
    Bitmap getImageBitmap() { return imageBitmap; }
}