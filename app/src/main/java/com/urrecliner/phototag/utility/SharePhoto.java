package com.urrecliner.phototag.Utility;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

public class SharePhoto {

    public void send(Context context, ArrayList<File> fileList) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ArrayList<Parcelable> list = new ArrayList<Parcelable>();
        for(File f : fileList){
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", f);
            list.add(contentUri);
        }
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, list);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, shareIntent, 0);
        try {
            pendingIntent.send();
        } catch(PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

}