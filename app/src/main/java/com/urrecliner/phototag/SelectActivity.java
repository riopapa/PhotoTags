package com.urrecliner.phototag;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.urrecliner.phototag.placeNearby.PlaceRetrieve;

import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.phototag.GPSTracker.hLatitude;
import static com.urrecliner.phototag.GPSTracker.hLongitude;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.nowDownLoading;
import static com.urrecliner.phototag.Vars.placeInfos;
import static com.urrecliner.phototag.Vars.placeType;
import static com.urrecliner.phototag.Vars.selectActivity;
import static com.urrecliner.phototag.Vars.sharedRadius;
import static com.urrecliner.phototag.Vars.sharedSort;
import static com.urrecliner.phototag.placeNearby.PlaceParser.pageToken;
import static com.urrecliner.phototag.placeNearby.PlaceParser.NO_MORE_PAGE;

public class SelectActivity extends AppCompatActivity {

    static CountDownTimer waitTimer = null;
    RecyclerView placeRecycleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placelist);
        selectActivity = this;
        placeRecycleView = findViewById(R.id.place_recycler);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        placeRecycleView.setLayoutManager(mLinearLayoutManager);

        waitTimer = new CountDownTimer(20000, 200) {
            public void onTick(long millisUntilFinished) {
                if (!nowDownLoading) {
                    waitTimer.cancel();
                    if (!pageToken.equals(NO_MORE_PAGE)) {
//                        EditText et = findViewById(R.id.placeAddress);
//                        String placeName = et.getText().toString();
//                        if (placeName != null && placeName.startsWith("?")) {
//                            String[] placeNames = placeName.split("\n");
//                            placeName = placeNames[0].substring(1);
//                        } else
//                            placeName = "";
                        new PlaceRetrieve(mContext, hLatitude, hLongitude, placeType, pageToken, sharedRadius, "");
                        new Timer().schedule(new TimerTask() {
                            public void run() {
                                waitTimer.start();
                            }
                        }, 1500);
                    } else {
                        sortPlaceInfos();
                        String s = "Total "+placeInfos.size()+" places retrieved";
                        Toast.makeText(mContext,s, Toast.LENGTH_SHORT).show();
                        PlaceAdapter placeAdapter = new PlaceAdapter();
                        placeRecycleView.setAdapter(placeAdapter);
                    }
                }
            }
            public void onFinish() { }
        }.start();
    }

    private void sortPlaceInfos() {

        switch(sharedSort) {
            case "??????":
                placeInfos.sort((arg0, arg1) -> arg0.getoName().compareTo(arg1.getoName()));
                break;
            case "??????":
                placeInfos.sort((arg0, arg1) -> arg0.getDistance().compareTo(arg1.getDistance()));
                break;
            default:
        }
    }

}