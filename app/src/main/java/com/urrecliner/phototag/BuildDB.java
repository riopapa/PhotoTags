package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.buildBitMap;
import static com.urrecliner.phototag.Vars.dirInfoReady;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.photoDao;
import static com.urrecliner.phototag.Vars.photoTags;
import static com.urrecliner.phototag.Vars.photoView;
import static com.urrecliner.phototag.Vars.squeezeDB;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;


class BuildDB {

    private static boolean isCanceled = false;
    private static Snackbar snackBar = null;
    private static View mainLayout;

    void fillUp(View view) {

        isCanceled = false;
        mainLayout = view;
        try {
            new buildThumbnailDB().execute("start");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void cancel() {
        isCanceled = true;
        if (snackBar != null) {
            snackBar.dismiss();
            snackBar = null;
        }
    }

    static class buildThumbnailDB extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            photoView.setBackgroundColor(Color.CYAN);
            String s = "Building thumbnails for "+photoTags.size()+" photos";
            snackBar = Snackbar.make(mainLayout, s, Snackbar.LENGTH_INDEFINITE);
            snackBar.setAction("Hide", v -> {
                snackBar.dismiss();
                snackBar = null;
                Toast.makeText(mContext, "Status Bar hidden", Toast.LENGTH_LONG).show();
            });
            snackBar.show();
        }

        final String SAY_COUNT = "sc";
        @Override
        protected String doInBackground(String... inputParams) {

            for (int pos = 0; pos < photoTags.size(); pos++) {
                if (isCanceled)
                    break;
                try {
                    PhotoTag photoTag = photoTags.get(pos);
                    if (photoTag.thumbnail == null) {
                        photoTag = getPhotoWithMap(photoTag);
                        photoTags.set(pos, photoTag);
                        publishProgress(SAY_COUNT);
                    }
                } catch (Exception e) {
                    break;
                }
            }
            return "done";
        }

        @Override
        protected void onPostExecute(String doI) {

            if (snackBar != null) {
                snackBar.dismiss();
                snackBar = null;
            }
            photoView.setBackgroundColor(Color.WHITE);
            if (dirInfoReady)
                squeezeDB.squeeze();
        }
    }

    static PhotoTag getPhotoWithMap(PhotoTag photoIn) {

        PhotoTag photoOut = photoDao.getByPhotoName(photoIn.fullFolder, photoIn.photoName);
        if (photoOut == null) {
            photoOut = buildBitMap.updateThumbnail(photoIn);
            photoDao.insert(photoOut);
            return photoOut;
        }
        if (photoOut.getThumbnail() == null) {
            photoOut = buildBitMap.updateThumbnail(photoIn);
            photoDao.update(photoOut);
            return photoOut;
        }
        return photoOut;
    }
}