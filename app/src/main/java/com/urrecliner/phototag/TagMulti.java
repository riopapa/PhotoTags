package com.urrecliner.phototag;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;

import static com.urrecliner.phototag.Vars.databaseIO;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.tagOnePhoto;
import static com.urrecliner.phototag.Vars.nowPlace;
import static com.urrecliner.phototag.Vars.photoAdapter;
import static com.urrecliner.phototag.Vars.photos;
import static com.urrecliner.phototag.Vars.utils;

class TagMulti {


    void tag() {
        try {
            new tagLoop().execute("start");
        } catch (Exception e) {
            utils.log("tag", e.toString());
        }
    }

    static class tagLoop extends AsyncTask<String, String, String> {

        StringBuilder msg;
        int makeCount = 0;

        @Override
        protected void onPreExecute() {
            makeCount = 0;
            nowPlace = null;
            msg = new StringBuilder("Following are marked");
        }

        final static String PROGRESS_UPDATE = "(";
        final static String PROGRESS_CHECKUP = "c";

        @Override
        protected String doInBackground(String... inputParams) {

            File fileHa;
            for (int pos = photos.size() - 1; pos >= 0; ) {  // should be last to first
                Photo photo = photos.get(pos);
                File fileOrg = photo.getFullFileName();
                if (photo.isChecked()) {
                    makeCount++;
                    photo.setChecked(false);
                    photos.set(pos, photo);
                    publishProgress(PROGRESS_CHECKUP,""+pos);
                    msg.append("\n").append(fileOrg.getName());
                    fileHa = tagOnePhoto.insertGeoInfo(photo);
                    publishProgress(PROGRESS_UPDATE + makeCount + ") "+fileOrg.getName(), "" + pos, fileHa.toString());
                } else
                    pos--;
            }
            return "done";
        }

        void removeItemView(int position) {
            photos.remove(position);
            photoAdapter.notifyItemRemoved(position);
            photoAdapter.notifyItemRangeChanged(position, photos.size()); // 지워진 만큼 다시 채워넣기.
            photoAdapter.notifyItemChanged(position);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String txt = values[0];
            int pos = Integer.parseInt(values[1]);
            switch (txt.substring(0, 1)) {
                case PROGRESS_CHECKUP:
                    photoAdapter.notifyItemChanged(pos);
                    break;
                case PROGRESS_UPDATE:
                    String newName = values[2];
                    File newFile = new File(newName);
                    Photo newPhoto = new Photo(newFile);
                    newPhoto.setBitmap(newPhoto.getBitmap());
                    newPhoto.setOrientation(1);
                    if (photos.get(pos-1).getFullFileName().toString().equals(newName)) {
                        pos--;
                        removeItemView(pos);
                        databaseIO.delete(newFile);
                    }
                    photos.add(pos, newPhoto);
                    photoAdapter.notifyItemInserted(pos);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onCancelled(String result) {
        }

        @Override
        protected void onPostExecute(String doI) {
            msg.append("\nTotal ").append(makeCount).append(" files marked");
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        }
    }
}