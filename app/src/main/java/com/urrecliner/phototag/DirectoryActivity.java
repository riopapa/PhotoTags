package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.dirActivity;
import static com.urrecliner.phototag.Vars.directoryAdapter;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class DirectoryActivity extends AppCompatActivity {

    RecyclerView dirView;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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