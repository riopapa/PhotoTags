package com.urrecliner.phototag;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.phototag.Vars.dirFolders;
import static com.urrecliner.phototag.Vars.dirInfoReady;
import static com.urrecliner.phototag.Vars.mActivity;
import static com.urrecliner.phototag.Vars.sizeX;
import static com.urrecliner.phototag.Vars.squeezeDB;
import static com.urrecliner.phototag.Vars.utils;

public class MakeDirFolder {

    void makeReady() {
        new Timer().schedule(new TimerTask() {
            public void run() {
                dirFolders = getPicFolders();
                new dirTask().execute("");
            }
        }, 5000);
    }

    class dirTask extends AsyncTask<String, String, String> {

        ArrayList<String> photoNames;
        @Override
        protected String doInBackground(String... inputParams) {
            int index = 0;
            if (dirFolders == null)
                return "";
//            utils.log("dir","size ="+dirFolders.size());
            for (DirectoryFolder df: dirFolders) {
//                utils.log("dir","df ="+df.getLongFolder());
                String folderName = df.getLongFolder();
                photoNames = utils.getFilteredFileNames(folderName);
                if (photoNames.size() != 0) {
//               Collections.sort(photoFiles, Collections.<File>reverseOrder());
                    int photoSize = photoNames.size();
                    df.setNumberOfPics(photoSize);
                    File[] photo4 = new File[4];
                    try {
                        if (photoSize > 8) {
                            photo4[0] = new File(folderName, photoNames.get(0));
                            photo4[1] = new File(folderName, photoNames.get(photoSize-1));
                            photo4[2] = new File(folderName, photoNames.get((photoSize-1)/3));
                            photo4[3] = new File(folderName, photoNames.get((photoSize-1)*2/3));
                        } else {
                            int maxCnt = Math.min(photoSize, 4);
                            for (int i = 0; i < maxCnt; i++)
                                photo4[i] = new File(folderName, photoNames.get(i));
                        }
                    } catch (Exception e) {
                        utils.log("df","bad images in "+df.getLongFolder());
                        e.printStackTrace();
                    }
                    df.setImageBitmap(buildOneDirImage(photo4));
                    dirFolders.set(index, df);
                    index++;
                }
                else
                    dirFolders.remove(index);
//            directoryAdapter.notifyDataSetChanged();
            }
            return "";
        }
        @Override
        protected void onCancelled(String result) { }

        @Override
        protected void onPostExecute(String doI) {
            dirInfoReady = true;
            MainActivity.enableFolderIcon();
            squeezeDB.squeeze();
        }
    }

    private ArrayList<DirectoryFolder> getPicFolders() {
        ArrayList<DirectoryFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA};
        String selection = MediaStore.Images.Media.DATA + " LIKE ?";
        String []selectionArgs = new String[] {"%.jpg%"};
        Cursor cursor = mActivity.getContentResolver().query(allImagesUri, projection, selection, selectionArgs, null, null); //"_data DESC");
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            else
                return null;
            do{
                String fullName = cursor.getString(0);      // DATA
                String fileName = new File(fullName).getName(); //file name
                String longFolder =  fullName.replace(fileName,"");    // remove file name
                if (!picPaths.contains(longFolder)) {
                    picPaths.add(longFolder);
                    DirectoryFolder df = new DirectoryFolder();
                    df.setLongFolder(longFolder);
                    picFolders.add(df);
                }
            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            utils.log("1",e.toString());
            e.printStackTrace();
        }

        Collections.sort(picFolders, new Comparator<DirectoryFolder>() {
            @Override
            public int compare(DirectoryFolder lhs, DirectoryFolder rhs) {
//                String lStr = utils.getUpperFolder(lhs.getLongFolder(), lhs.getShortFolder()) + " / " + lhs.getShortFolder();
//                String rStr = utils.getUpperFolder(rhs.getLongFolder(), rhs.getShortFolder()) + " / " + rhs.getShortFolder();
                return lhs.getLongFolder().compareTo(rhs.getLongFolder());
            }
        });
        return picFolders;
    }


    private Bitmap buildOneDirImage(File [] photo4) {
        int x = 0,y = 0;
        int bitmapSize = sizeX / 4;
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