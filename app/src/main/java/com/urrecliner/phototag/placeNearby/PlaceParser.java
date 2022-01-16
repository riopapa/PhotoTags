package com.urrecliner.phototag.placeNearby;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceParser {

    private HashMap<String, String> onePlace2Map(JSONObject placeJson)
    {
        HashMap<String, String> placeList = new HashMap<>();
        String placeName = "--NA--";
        String vicinity= "--NA--";
        String icon;

        try {
            if (!placeJson.isNull("name")) {
                placeName = placeJson.getString("name");
            }
            if (!placeJson.isNull("vicinity")) {
                vicinity = placeJson.getString("vicinity");
            }
            if (!placeJson.isNull("formatted_address")) {
                vicinity = placeJson.getString("formatted_address");
            }
            vicinity = vicinity.replace("KR","")
                        .replace("대한민국","").replace("서울특별시","");
            String latitude = placeJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            String longitude = placeJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            icon = placeJson.getString("icon");
            String []icons = icon.split("/");
            icon = icons[icons.length-1];
            icons = icon.split("-");
            icon = icons[0];

            placeList.put("name", placeName);
            placeList.put("vicinity", vicinity);
            placeList.put("lat", latitude);
            placeList.put("lng", longitude);
            placeList.put("icon", icon);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return placeList;

    }
    private List<HashMap<String, String>> getAllPlaces(JSONArray jsonArray)
    {
        int count = jsonArray.length();
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap;

        for(int i = 0; i<count;i++)
        {
            try {
                placeMap = onePlace2Map((JSONObject) jsonArray.get(i));
                placelist.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placelist;
    }

    public static final String NO_MORE_PAGE = "no more";
    public static String pageToken;

    public List<HashMap<String, String>> parse(String jsonData, boolean byPlaceName)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;
//        utils.log("json data", jsonData);

        if (byPlaceName) {
            try {
                jsonObject = new JSONObject(jsonData);
                jsonArray = jsonObject.getJSONArray("candidates");
                pageToken = (jsonObject.isNull("next_page_token")) ?
                        NO_MORE_PAGE : jsonObject.getString("next_page_token");
            } catch (JSONException e) {
                pageToken = NO_MORE_PAGE;
                e.printStackTrace();
            }

        } else {
            try {
                jsonObject = new JSONObject(jsonData);
                jsonArray = jsonObject.getJSONArray("results");
                pageToken = (jsonObject.isNull("next_page_token")) ?
                        NO_MORE_PAGE : jsonObject.getString("next_page_token");
            } catch (JSONException e) {
                pageToken = NO_MORE_PAGE;
                e.printStackTrace();
            }
        }
        assert jsonArray != null;
        return getAllPlaces(jsonArray);
    }
}