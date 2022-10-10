package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.dirInfoReady;
import static com.urrecliner.phototag.Vars.directoryAdapter;
import static com.urrecliner.phototag.Vars.albumFolders;
import static com.urrecliner.phototag.Vars.mActivity;
import static com.urrecliner.phototag.Vars.photoDao;
import static com.urrecliner.phototag.Vars.sizeX;
import static com.urrecliner.phototag.Vars.squeezeDB;
import static com.urrecliner.phototag.Vars.utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

public class MakeFolderThumbnail {

    void init() {
        albumFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA};
        String selection = MediaStore.Images.Media.DATA + " LIKE ?";
        String []selectionArgs = new String[] {"%.jpg%"};
        Cursor cursor = mActivity.getContentResolver().query(allImagesUri, projection, selection, selectionArgs, null, null); //"_data DESC");
        try {
            cursor.moveToFirst();
            do{
                String fullName = cursor.getString(0);      // DATA
                String fileName = new File(fullName).getName(); //file name
                String longFolder =  fullName.replace(fileName,"");    // remove file name
                if (!picPaths.contains(longFolder)) {
                    picPaths.add(longFolder);
                    AlbumInfo fi = new AlbumInfo();
                    fi.longFolder = longFolder;
                    fi.lastModified = new File(longFolder).lastModified();
                    fi.imageBitmap = null;
                    fi.numberOfPics = utils.getPhotoCount(longFolder);
                    albumFolders.add(fi);
                }
            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            utils.log("1",e.toString());
            e.printStackTrace();
        }

        Collections.sort(albumFolders, new Comparator<AlbumInfo>() {
            @Override
            public int compare(AlbumInfo lhs, AlbumInfo rhs) {
                return lhs.longFolder.compareTo(rhs.longFolder);
            }
        });
    }
    void makeReady() {
        new Timer().schedule(new TimerTask() {
            public void run() {
                new dirTask().execute("");
            }
        }, 5000);
    }

    class dirTask extends AsyncTask<String, String, String> {

        ArrayList<String> photoNames;
        @Override
        protected String doInBackground(String... inputParams) {
            if (albumFolders != null) {
                for (int i = 0; i < albumFolders.size(); i++) {
                    AlbumInfo album = albumFolders.get(i);
                    int photoCount = utils.getPhotoCount(album.longFolder);
                    PhotoTag folderTag = photoDao.getFolderInfo("@"+album.longFolder);
                    if (album.imageBitmap == null || folderTag == null || photoCount != album.numberOfPics) {
                        if (folderTag == null || Long.parseLong(folderTag.photoName) != album.lastModified) {
                            Log.w("dirTask","make/update folder image >> "+album.longFolder);
                            album = updateFolderBitMap(album, folderTag);
                            album.numberOfPics = utils.getPhotoCount(album.longFolder);
                        } else {
                            album.imageBitmap = utils.StringToBitMap(folderTag.thumbnail);
                            album.numberOfPics = photoCount;
                        }
                    }
                    albumFolders.set(i, album);
                }
            }
            return "";
        }

        private AlbumInfo updateFolderBitMap(AlbumInfo fi, PhotoTag pt) {
            if (pt != null)
                photoDao.delete(pt);
            String realFolder = fi.longFolder;
            photoNames = utils.getFilteredFileNames(realFolder);
            File folder = new File(realFolder);
            fi.lastModified = folder.lastModified();
            int photoSize = photoNames.size();
            fi.numberOfPics = photoSize;  // if size zero, ignore that folder
            if (photoSize != 0) {
                File[] photo4 = new File[4];
                try {
                    if (photoSize > 8) {
                        photo4[0] = new File(realFolder, photoNames.get(0));
                        photo4[1] = new File(realFolder, photoNames.get(photoSize-1));
                        photo4[2] = new File(realFolder, photoNames.get((photoSize-1)/3));
                        photo4[3] = new File(realFolder, photoNames.get((photoSize-1)*2/3));
                    } else {
                        int maxCnt = Math.min(photoSize, 4);
                        for (int i = 0; i < maxCnt; i++)
                            photo4[i] = new File(realFolder, photoNames.get(i));
                    }
                } catch (Exception e) {
                    utils.log("df","bad images in "+ fi.longFolder);
                    e.printStackTrace();
                }
                fi.imageBitmap = buildOneDirImage(photo4);
                pt = new PhotoTag();
                pt.fullFolder = "@"+ fi.longFolder;
                pt.photoName = ""+fi.lastModified;
                pt.isChecked = false;
                pt.thumbnail = utils.BitMapToString(fi.imageBitmap);
                pt.orient = "9";  // 9 means it is directory folder
                photoDao.insert(pt);
            }
            return fi;
        }

        @Override
        protected void onCancelled(String result) { }

        @Override
        protected void onPostExecute(String doI) {
            dirInfoReady = true;
            MainActivity.enableFolderIcon();
            squeezeDB.squeeze();
            if (directoryAdapter != null)
                directoryAdapter.notifyDataSetChanged();
        }
    }

    private Bitmap buildOneDirImage(File [] photo4) {
        int x = 0,y = 0;
        int bitmapSize = sizeX / 6;
        Bitmap dirBitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(dirBitmap);
        canvas.drawColor(mActivity.getColor(R.color.colorPrimary));
        Paint paint = new Paint();
        for (int i = 0; i < 4; i++) {
            if (photo4[i] != null) {
                Bitmap oneBit = BitmapFactory.decodeFile(photo4[i].getAbsolutePath());
                int width = oneBit.getWidth();
                int height = oneBit.getHeight();
                int sWidth = Math.min(width, height);

                Bitmap sBitmap = Bitmap.createBitmap(oneBit, (width - sWidth) / 2, (height - sWidth) / 2, sWidth, sWidth);
                sBitmap = Bitmap.createScaledBitmap(sBitmap, bitmapSize/2-2, bitmapSize/2-2, false);
                switch (i) {
                    case 0:
                        x = 0; y = 0; break;
                    case 1:
                        x = 0; y = bitmapSize/2+1; break;
                    case 2:
                        x = bitmapSize/2+1; y = 0; break;
                    case 3:
                        x = bitmapSize/2+1; y = bitmapSize/2+1; break;
                }
                canvas.drawBitmap(sBitmap, x, y, paint);
            }
        }
        return dirBitmap;
    }
}