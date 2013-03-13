package com.eit.minimap.datastructures;

import android.graphics.Color;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class User {
    //private Location pos;
    private final String macAddr;
    private final String screenName;

    /** GoogleMap marker. */
    private Marker marker;
    /** GoogleMap polyline stuff */
    private final PolylineOptions polylineOptions = new PolylineOptions();
    private Polyline polyline;

    /** Previously known user positions. */
    private final List<Coordinate> positions = new ArrayList<Coordinate>();


    public User(String macAddr, String screenName){
        this.macAddr = macAddr;
        this.screenName = screenName;
        //TODO: Make unique colors for every user.
        polylineOptions.color(Color.GREEN);
    }

    public User(JSONObject json) throws JSONException {
        macAddr = json.getString("macAddr");
        screenName = json.getString("screenName");
    }

    public String getMacAddr(){
        return macAddr;
    }

    public String getScreenName() {
        return screenName;
    }

    public void addPosition(Coordinate pos) {
        //TODO: Contemplate removing old elements from list if we have too many here.
        positions.add(pos);
        //Update polyline data:
        polylineOptions.add(pos.getLatLng());
    }

    /**
     * Fetches all the GPS positions registered by this User object.
     * @return List of positions. The last element is the newest. The list is immutable.
     */
    public List<Coordinate> getPositions() {
        return Collections.unmodifiableList(positions);
    }

    public Coordinate getPosition() {
        if(positions.size()!=0)
            return positions.get(positions.size()-1);
        return null;
    }

    public JSONObject toJson() {
        try{
            return new JSONObject()
                    .put("type", "pInfo")
                    .put("macAddr", macAddr)
                    .put("screenName", screenName);
            //Need to add screenName and AvatarImage
        } catch (JSONException ignored) {}
        return null;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        // We'll set marker settings here..
        marker.setTitle(screenName);
    }

    /**
     * Removes old polyline and makes another and adds it to the provided map.
     * This is the only way to edit it.
     * MUST BE CALLED FROM UI THREAD!
     * @param map Map to add polyline to.
     */
    public void makePolyline(GoogleMap map) {
        removePolyline();
        polyline = map.addPolyline(polylineOptions);
    }

    /**
     * Updates the marker to use last position and timestamp
     * MUST BE CALLED FROM UI THREAD!
     */
    public void updateMarker() {
        if(marker != null) {
            Coordinate pos = positions.get(positions.size()-1);
            //Move our marker
            marker.setPosition(pos.getLatLng());

            //Add some text to the onClick bubble to show how long it is since this user provided a coordinate.
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM:ss");
            String time = simpleDateFormat.format(new Date(pos.getTimestamp()));
            marker.setSnippet("Last seen: "+time);
        }
    }

    /**
     * Removes the polyline for this user, is set.
     */
    public void removePolyline() {
        if(polyline != null) polyline.remove();
    }
}
