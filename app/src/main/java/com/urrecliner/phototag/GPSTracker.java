package com.urrecliner.phototag;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import static android.content.Context.LOCATION_SERVICE;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.utils;

public class GPSTracker implements LocationListener {

    private static final float MIN_DISTANCE_WALK = 10; // meters
    private static final long MIN_TIME_WALK_UPDATES = 1000; // miliSecs
    protected LocationManager locationManager;

    public static double hLatitude = 0;
    public static double hLongitude = 0;
    public static double hAltitude = 0;
    static double oLatitude, oLongitude;

    void get() {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            assert locationManager != null;
            locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_WALK_UPDATES,
                        MIN_DISTANCE_WALK, this);
            if (locationManager != null) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    hLatitude = location.getLatitude();
                    hLongitude = location.getLongitude();
                    hAltitude = location.getAltitude();
                    oLatitude = hLatitude;
                    oLongitude = hLongitude;
                }
            }

        } catch (Exception e) {
            utils.logE("GPS", "Start Error");
        }

        while (hLatitude == 0) {
            SystemClock.sleep((1000));
            Log.w("wait","location update");
        }
        oLatitude = hLatitude; oLongitude = hLongitude;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (hLatitude == 0 ) {
            hLatitude = location.getLatitude();
            hLongitude = location.getLongitude();
            hAltitude = location.getAltitude();
        }
        utils.log("location changed",location.getLatitude()+","+location.getLongitude()+","+location.getAltitude());
        locationManager.removeUpdates(this);
    }
    @Override
    public void onProviderDisabled(String provider) { }
    @Override
    public void onProviderEnabled(String provider) { }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

}