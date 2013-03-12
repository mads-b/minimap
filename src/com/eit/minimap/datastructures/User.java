package com.eit.minimap.datastructures;

import android.graphics.Color;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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


    User(String macAddr, String screenName){
        this.macAddr = macAddr;
        this.screenName = screenName;
        //TODO: Make unique colors for every user.
        polylineOptions.color(Color.GREEN);
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
        if(marker != null) {
            //Move our marker
            marker.setPosition(pos.getLatLng());

            //Add some text to the onClick bubble to show how long it is since this user provided a coordinate.
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM:ss");
            String time = simpleDateFormat.format(new Date(pos.getTimestamp()));
            marker.setSnippet("Last seen: "+time);
        }



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
     * @param map Map to add polyline to.
     */
    public void makePolyline(GoogleMap map) {
        removePolyline();
        polyline = map.addPolyline(polylineOptions);
    }

    /**
     * Removes the polyline for this user, is set.
     */
    public void removePolyline() {
        if(polyline != null) polyline.remove();
    }
}
