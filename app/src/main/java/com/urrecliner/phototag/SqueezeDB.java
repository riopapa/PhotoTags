package com.urrecliner.phototag;

import android.util.Log;

import java.io.File;
import java.util.List;

import static com.urrecliner.phototag.Vars.albumFolders;
import static com.urrecliner.phototag.Vars.makeFolderThumbnail;
import static com.urrecliner.phototag.Vars.photoDao;

class SqueezeDB {

    void squeeze() {

        List<String> daoFolders = photoDao.getDaoFolders();
        for (int i = 0; i < daoFolders.size(); i++) {   // remove leading '@'
            daoFolders.set(i, daoFolders.get(i).substring(1));
        }

        for (String daoFolder: daoFolders) {
            boolean isExist = false;
            for (AlbumInfo albumFolder: albumFolders) {
                if (daoFolder.equals(albumFolder.longFolder)) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                photoDao.deleteFolder(daoFolder);
                photoDao.deleteFolder("@"+daoFolder);
                Log.w("squeeze", daoFolder+" folder mass deleted ///");
            }
        }

        PhotoTag photoTag = new PhotoTag();
        for (int i = 0; i < albumFolders.size(); i++) {
            String album = albumFolders.get(i).longFolder;
            List<Integer> dirCounts = photoDao.getRowCount(album);
            if (dirCounts.get(0) == 0)
                continue;
            if (dirCounts.get(0) != albumFolders.get(i).numberOfPics) {
                List<String> daoPhotos = photoDao.getAllInFolder(album);
                for (int p = 0; p < daoPhotos.size(); p++) {
                    String shortName = daoPhotos.get(p);
                    File file = new File(album, shortName);
                    if (!file.exists()) {
                        photoTag.fullFolder = album;
                        photoTag.photoName = shortName;
                        photoDao.delete(photoTag);
                        makeFolderThumbnail.makeReady();
                        Log.w("squeeze", "Delete " + album + " / " + shortName);
                    }
                }
            }
        }
    }

}