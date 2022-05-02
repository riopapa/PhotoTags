package com.urrecliner.phototag;

import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.Timer;

import static com.urrecliner.phototag.Vars.folderInfos;
import static com.urrecliner.phototag.Vars.makeFolderSumNail;
import static com.urrecliner.phototag.Vars.photoDao;

class SqueezeDB {

    Timer timer = null;

    void squeeze() {

        List<String> allFolders = photoDao.getAllFolders();
        for (int i = 0; i < allFolders.size();) {
            if (allFolders.get(i).startsWith("@"))
                allFolders.remove(i);
            else
                i++;
        }
        for (String folderName: allFolders) {
            boolean isExist = false;
            for (FolderInfo dirFolder: folderInfos) {
                if (folderName.equals(dirFolder.longFolder)) {
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
        for (int i = 0; i < folderInfos.size(); i++) {
            String fullFolder = folderInfos.get(i).longFolder;

            List<Integer> dirCounts = photoDao.getRowCount(fullFolder);
            if (dirCounts.get(0) == 0)
                continue;;
            if (dirCounts.get(0) != folderInfos.get(i).numberOfPics) {
                List<String> daoPhotos = photoDao.getAllInFolder(fullFolder);
                for (int p = 0; p < daoPhotos.size(); p++) {
                    String shortName = daoPhotos.get(p);
                    File file = new File(fullFolder, shortName);
                    if (!file.exists()) {
                        photoTag.fullFolder = fullFolder;
                        photoTag.photoName = shortName;
                        photoDao.delete(photoTag);
                        makeFolderSumNail.makeReady();
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