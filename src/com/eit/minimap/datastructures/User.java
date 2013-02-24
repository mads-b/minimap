package com.eit.minimap.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    //private Location pos;
    private String macAddr;
    private String screenName;
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
}
