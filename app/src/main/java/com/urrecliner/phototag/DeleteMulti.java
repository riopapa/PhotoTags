package com.urrecliner.phototag;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import static com.urrecliner.phototag.Vars.fabUndo;
import static com.urrecliner.phototag.Vars.mActivity;
import static com.urrecliner.phototag.Vars.mainMenu;
import static com.urrecliner.phototag.Vars.photoAdapter;
import static com.urrecliner.phototag.Vars.photoTags;

class DeleteMulti {

    static void run() {

        try {
            new deletePhotoLoop().execute("start");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private class deletePhotoLoop extends AsyncTask<String, String, String> {

        StringBuilder msg;
        int deleteCount;

        @Override
        protected void onPreExecute() {
            deleteCount = 0;
            msg = new StringBuilder("Following are deleted");
        }

        @Override
        protected String doInBackground(String... inputParams) {

            for (int pos = photoTags.size() - 1; pos >= 0; pos--) {  // should be last to first
                PhotoTag photoTag = photoTags.get(pos);
                if (photoTag.isChecked()) {
                    File file2del = new File (photoTag.fullFolder, photoTag.photoName);
                    if (file2del.delete()) {
                        mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file2del)));
                        deleteCount++;
                        msg.append("\n");
                        msg.append(file2del.getName());
                        publishProgress("(" + pos + ") " + photoTag.photoName + " deleted", "" + pos);
                    }
                }
            }
            return "done";
        }

        @Override
        protected void onProgressUpdate(String... values) {
//            String debugText = values[0];
//            Toast.makeText(mContext, debugText, Toast.LENGTH_SHORT).show();
            int pos = Integer.parseInt(values[1]);
            photoTags.remove(pos);
            photoAdapter.notifyItemRemoved(pos);
//            photoAdapter.notifyItemRangeChanged(pos, photoTags.size());
        }

        @Override
        protected void onPostExecute(String doI) {
            msg.append("\nTotal ");
            msg.append(deleteCount);
            msg.append(" photos deleted");
            Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fabUndo.setVisibility(View.INVISIBLE);
                    MenuItem item = mainMenu.findItem(R.id.action_Delete);
                    item.setVisible(false);
                }
            });
        }
    }
}