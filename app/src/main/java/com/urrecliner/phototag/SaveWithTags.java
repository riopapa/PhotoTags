package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.SUFFIX_JPG;
import static com.urrecliner.phototag.Vars.buildBitMap;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.nowPlace;
import static com.urrecliner.phototag.Vars.photoDao;
import static com.urrecliner.phototag.Vars.utils;

import android.graphics.Bitmap;

import java.io.File;

class SaveWithTags {

    // create new photo file and insert to dao

    PhotoTag save(PhotoTag orgPhoto, Bitmap viewImage)  {
        PhotoTag newPhoto = null;
        try {
            newPhoto = orgPhoto.clone();
        } catch (CloneNotSupportedException e ) {
            e.printStackTrace();
        }
        File orgFile = new File(orgPhoto.fullFolder, orgPhoto.photoName);
        long timeStamp = utils.getFileDate(orgFile);

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
        viewImage = buildBitMap.addDateLocSignature(mContext, viewImage, timeStamp, sFood, sPlace, sAddress);
        String orgName = orgPhoto.photoName;
        String tgtName = orgName.substring(0, orgName.length() - 4) + "_";

        if (sFood.equals(" "))
            tgtName += sPlace;
        else {
            tgtName += sPlace+"("+sFood+")";
        }
        tgtName += SUFFIX_JPG;
        utils.createPhotoFile(orgPhoto.fullFolder, orgName, tgtName, viewImage);
        newPhoto.photoName = tgtName;
        newPhoto.thumbnail = null;
        photoDao.insert(newPhoto);
        return newPhoto;
    }

}