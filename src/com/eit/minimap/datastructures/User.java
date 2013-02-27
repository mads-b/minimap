package com.eit.minimap.datastructures;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    //private Location pos;
    private String macAddr;
    private String screenName;

    /** GoogleMap marker. */
    private Marker marker;
    private List<Coordinate> positions = new ArrayList<Coordinate>();


    User(String macAddr, String screenName){
        this.macAddr = macAddr;
        this.screenName = screenName;
    }

    public String getMacAddr(){
        return macAddr;
    }

    public void addPosition(Coordinate pos) {
        //TODO: Contemplate removing old elements from list if we have too many here.
        positions.add(pos);
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
    }
}
