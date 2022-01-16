package com.urrecliner.phototag;

import android.database.Cursor;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;

import static com.urrecliner.phototag.Vars.databaseIO;
import static com.urrecliner.phototag.Vars.mContext;

class SqueezeDB {

    private static int deleteCount = 0;
    private static Cursor cursor = null;
    private static boolean isCanceled;

    void run() {


        cursor = databaseIO.retrieveAll();
        if (cursor == null || cursor.getCount() == 0)
            return;
        cursor.moveToFirst();
        deleteCount = 0;
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(() -> {
            int interval = 100;
            int maxTime = cursor.getCount() * interval;
            CountDownTimer countDownTimer = new CountDownTimer(maxTime, interval) {
                public void onTick(long millisUntilFinished) {
                    if (!isCanceled) {
                        File fullFileName = new File(cursor.getString(1));
                        if (!fullFileName.exists()) {
                            databaseIO.delete(fullFileName);
                            deleteCount++;
                        }
                        if (!cursor.moveToNext())
                            isCanceled = true;
                    }
                }
                public void onFinish() {
                    if (deleteCount > 0)
                        Toast.makeText(mContext,"Total "+deleteCount+" removed photos squeezed",Toast.LENGTH_LONG).show();
                }
            };
            countDownTimer.start();
        }, 1);
    }

    void cancel() {
        isCanceled = true;
    }

}