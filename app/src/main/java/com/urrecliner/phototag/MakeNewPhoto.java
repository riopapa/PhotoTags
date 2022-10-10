package com.urrecliner.phototag;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

import static com.urrecliner.phototag.Vars.SUFFIX_JPG;
import static com.urrecliner.phototag.Vars.buildBitMap;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.nowPlace;
import static com.urrecliner.phototag.Vars.photoDao;
import static com.urrecliner.phototag.Vars.utils;

class MakeNewPhoto {

    // create new photo file and insert to dao

    PhotoTag save(PhotoTag orgPhoto)  {
        PhotoTag newPhoto = null;
        try {
            newPhoto = orgPhoto.clone();
        } catch (CloneNotSupportedException e ) {
            e.printStackTrace();
        }
        File orgFile = new File(orgPhoto.fullFolder, orgPhoto.photoName);
        long timeStamp = utils.getFileDate(orgFile);
        Bitmap bitmap = BitmapFactory.decodeFile(orgFile.getAbsolutePath());

        String sFood = " ", sPlace = " ", sAddress = " ";
        if (nowPlace != null) {
            String [] s = nowPlace.split("\n");
            if (s.length > 2) {
                sFood = s[0]; sPlace = s[1]; sAddress = s[2];
            } else if (s.length == 2) {
                sPlace = s[0]; sAddress = s[1];
            } else
                sAddress = s[0];
        }
        bitmap = buildBitMap.addDateLocSignature(mContext, bitmap, timeStamp, sFood, sPlace, sAddress);
        String orgName = orgPhoto.photoName;
        String tgtName = orgName.substring(0, orgName.length() - 4) + "_";

        if (sFood.equals(" "))
            tgtName += sPlace;
        else {
            tgtName += sPlace+"("+sFood+")";
        }
        tgtName += SUFFIX_JPG;
        utils.createPhotoFile(orgPhoto.fullFolder, orgName, tgtName, bitmap, "1");
        newPhoto.photoName = tgtName;
        newPhoto.thumbnail = null;
        photoDao.insert(newPhoto);
        return newPhoto;
    }

}