package com.urrecliner.phototag;

import android.os.AsyncTask;
import android.widget.Toast;

import static com.urrecliner.phototag.Vars.fullFolder;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.photoDao;
import static com.urrecliner.phototag.Vars.photoTags;
import static com.urrecliner.phototag.Vars.nowPlace;
import static com.urrecliner.phototag.Vars.photoAdapter;
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

            for (int pos = photoTags.size() - 1; pos >= 0; ) {  // should be last to first
                PhotoTag photoTag = photoTags.get(pos);
                if (photoTag.isChecked()) {
                    makeCount++;
                    photoTag.setChecked(false);
                    photoTags.set(pos, photoTag);
                    publishProgress(PROGRESS_CHECKUP,""+pos);
                    msg.append("\n").append(photoTag.photoName);
                    PhotoTag newPhoto = NewPhoto.save(photoTag);
                    publishProgress(PROGRESS_UPDATE + makeCount + ") "+photoTag.photoName, "" + pos, newPhoto.photoName);
                } else
                    pos--;
            }
            return "done";
        }

        void removeItemView(int position) {
            photoTags.remove(position);
            photoAdapter.notifyItemRemoved(position);
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
                    PhotoTag newPhotoTag = new PhotoTag();
                    newPhotoTag.fullFolder = fullFolder;
                    newPhotoTag.photoName = newName;
                    newPhotoTag.setSumNailMap(newPhotoTag.getSumNailMap());
                    newPhotoTag.setOrient("1");
                    if (photoTags.get(pos-1).photoName.equals(newName)) {
                        pos--;
                        removeItemView(pos);
                        photoDao.delete(newPhotoTag);
                    }
                    photoTags.add(pos, newPhotoTag);
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