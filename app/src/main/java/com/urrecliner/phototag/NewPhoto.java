package com.urrecliner.phototag;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.File;

import static com.urrecliner.phototag.Vars.SUFFIX_JPG;
import static com.urrecliner.phototag.Vars.buildBitMap;
import static com.urrecliner.phototag.Vars.mActivity;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.nowPlace;
import static com.urrecliner.phototag.Vars.photoAdapter;
import static com.urrecliner.phototag.Vars.photoDao;
import static com.urrecliner.phototag.Vars.utils;

class NewPhoto {

    // create new photo file and insert to dao

    static PhotoTag save(PhotoTag orgPhoto)  {
        PhotoTag newPhoto = null;
        try {
            newPhoto = (PhotoTag) orgPhoto.clone();
        } catch (CloneNotSupportedException e ) {
            e.printStackTrace();
        }
        File orgFile = new File(orgPhoto.fullFolder, orgPhoto.photoName);
        long timeStamp = utils.getFileDate(orgFile);
        Bitmap bitmap = BitmapFactory.decodeFile(orgFile.getAbsolutePath());
        String orient = orgPhoto.orient;
        if (!orient.equals("1")) {
            Matrix matrix = new Matrix();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int degree = 0;
            switch (orient) {
                case "6":
                    degree = 90;
                    break;
                case "8":
                    degree = -90;
                    break;
                case "3":
                    degree = 180;
                    break;
            }
            matrix.postRotate(degree);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
            newPhoto.orient = "1";
        }
        buildBitMap.init(mActivity, mContext, orient);
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
        bitmap = buildBitMap.markDateLocSignature(bitmap, timeStamp, sFood, sPlace, sAddress);
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
        newPhoto.setSumNailMap(null);
        photoDao.insert(newPhoto);
        return newPhoto;
    }

}