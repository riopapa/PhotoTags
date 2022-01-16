package com.urrecliner.phototag.placeNearby;


import static com.urrecliner.phototag.GPSTracker.hLatitude;
import static com.urrecliner.phototag.GPSTracker.hLongitude;

public class PlaceInfo {
    String oName;
    String oAddress;
    String oIcon;
    String oLat;
    String oLng;
    String distance;
    Double lat, lng;    // derived from string

    public String getoName() { return oName; }
    public String getoAddress() { return oAddress; }
    public String getoIcon() { return oIcon; }


    public String getDistance() {
        return distance;
    }


    public PlaceInfo(String oName, String oAddress, String oIcon, String oLat, String oLng) {
        this.oName = oName;
        this.oAddress = oAddress;
        this.oIcon = oIcon;
        this.oLat = oLat;
        this.oLng = oLng;
        lat = Double.parseDouble(oLat);
        lng = Double.parseDouble(oLng);
        distance = ((Math.sqrt((hLatitude-lat)*(hLatitude-lat)+(hLongitude-lng)*(hLongitude-lng))*1000L+1000L)+"");
    }

    public void setoName(String oName) {
        this.oName = oName;
    }
}