package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.utils;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSTracker implements LocationListener {

    protected LocationManager locationManager;

    public static double hLatitude = 0;
    public static double hLongitude = 0;
    public static double hAltitude = 0;

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