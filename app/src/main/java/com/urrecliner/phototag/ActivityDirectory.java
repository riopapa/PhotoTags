package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.buildDB;
import static com.urrecliner.phototag.Vars.dirActivity;
import static com.urrecliner.phototag.Vars.directoryAdapter;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.Timer;
import java.util.TimerTask;

public class ActivityDirectory extends AppCompatActivity {

    RecyclerView dirView;

    final long BACK_DELAY = 500;
    long backKeyPressedTime;
    @Override
    public void onBackPressed() {

        if(System.currentTimeMillis()<backKeyPressedTime+BACK_DELAY){
            buildDB.cancel();
            finish();
            new Timer().schedule(new TimerTask() {
                public void run() {
                    finishAffinity();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }, 100);
        } else {
            Toast.makeText(this, "Press BackKey again to quit",Toast.LENGTH_SHORT).show();
            backKeyPressedTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);
        dirActivity = this;
        dirView = findViewById(R.id.pathView);
        StaggeredGridLayoutManager SGL = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        dirView.setLayoutManager(SGL);
        dirView.addItemDecoration(new DividerItemDecoration(this, SGL.getOrientation()));
        dirView.setLayoutManager(SGL);
        directoryAdapter = new DirectoryAdapter();
        dirView.setAdapter(directoryAdapter);
    }

}