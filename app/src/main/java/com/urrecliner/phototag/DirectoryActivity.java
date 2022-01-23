package com.urrecliner.phototag;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import static com.urrecliner.phototag.Vars.dirActivity;
import static com.urrecliner.phototag.Vars.directoryAdapter;
import static com.urrecliner.phototag.Vars.mContext;

public class DirectoryActivity extends AppCompatActivity {

    RecyclerView dirView;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
//        Intent intent = new Intent(mContext, MainActivity.class);
//        startActivity(intent);
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