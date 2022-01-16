package com.urrecliner.phototag.placeNearby;

import android.content.Context;

import com.urrecliner.phototag.R;

public class PlaceRetrieve {

    static boolean byPlaceName = false;
    public PlaceRetrieve(Context mContext, double latitude, double longitude, String placeType, String pageToken,  String radius, String placeName) {

        StringBuilder url;
        if (!placeName.equals("")) {
            byPlaceName = true;
            placeName = placeName.replace(" ","%20");
            url = new StringBuilder("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?");
            url.append("input=").append(placeName).append("&inputtype=textquery");
            url.append("&locationbias=circle:").append(radius)
                    .append("@").append(latitude).append(",").append(longitude);
       } else {
            byPlaceName = false;
            url = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            if (!placeType.equals("all"))
                url.append("&type=").append(placeType);
            url.append("&location=").append(latitude).append(",").append(longitude);
            url.append("&radius=").append(radius);
        }
        url.append("&language=ko");
        if (pageToken != null && !pageToken.equals("no more")) {
            url.append("&pagetoken=").append(pageToken);
//            Toast.makeText(mContext, "retrieving more places " + size, Toast.LENGTH_LONG).show();
        } else {
            url.append("&fields=formatted_address,name,icon,geometry");
        }
//        googlePlaceUrl.append("&fields=formatted_address,name,na");
        url.append("&key=").append(mContext.getString(R.string.maps_api_key));
        PlaceInfoBuild placeInfoBuild = new PlaceInfoBuild();
        placeInfoBuild.execute(url.toString(), placeName);
    }

}