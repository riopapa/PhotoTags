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

import static com.urrecliner.phototag.Vars.fullFolder;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.photoDao;

class SqueezeDB {

    private static int deleteCount;
    private List<PhotoTag> tags;
    Timer timer = null;

    void getAll() {
        tags = photoDao.getAllPhotos();
    }
    void run() {

        deleteCount = 0;
        if (tags.size() == 0)
            return;
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (tags.size() > 0) {
                    PhotoTag pt = tags.get(0);
                    tags.remove(0);
                    File file = new File(pt.fullFolder, pt.photoName);
                    if (!file.exists()) {
                        photoDao.delete(pt);
                        Log.w("Delete " + deleteCount++, file.toString());
                    }
                } else {
                    timer.cancel();
                }
            }
        };
        timer.schedule(timerTask, 0, 100);
    }


    void cancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}