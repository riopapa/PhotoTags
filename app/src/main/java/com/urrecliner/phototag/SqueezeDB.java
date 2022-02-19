package com.urrecliner.phototag;

import android.database.Cursor;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.phototag.Vars.dirFolders;
import static com.urrecliner.phototag.Vars.fullFolder;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.photoDao;
import static com.urrecliner.phototag.Vars.photoTags;
import static com.urrecliner.phototag.Vars.utils;

class SqueezeDB {

    Timer timer = null;

    void squeeze() {

        List<String> allFolders = photoDao.getAllFolders();
        for (String folderName: allFolders) {
            boolean isExist = false;
            for (DirectoryFolder dirFolder: dirFolders) {
                if (folderName.equals(dirFolder.getLongFolder())) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                photoDao.deleteFolder(folderName);
                Log.w("squeeze", folderName+" mass deleted ///");
            }
        }

        PhotoTag photoTag = new PhotoTag();
        for (int i = 0; i < dirFolders.size(); i++) {
            String fullFolder = dirFolders.get(i).getLongFolder();

            List<Integer> dirCounts = photoDao.getRowCount(fullFolder);
            if (dirCounts.get(0) == 0)
                continue;;
            if (dirCounts.get(0) != dirFolders.get(i).getNumberOfPics()) {
                List<String> daoPhotos = photoDao.getAllInFolder(fullFolder);
                for (int p = 0; p < daoPhotos.size(); p++) {
                    String shortName = daoPhotos.get(p);
                    File file = new File(fullFolder, shortName);
                    if (!file.exists()) {
                        photoTag.fullFolder = fullFolder;
                        photoTag.photoName = shortName;
                        photoDao.delete(photoTag);
                        Log.w("squeeze", "Delete " + fullFolder + " / " + shortName);
                    }
                }
            }
        }
    }

    void cancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}