package com.eit.minimap.datastructures;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simple data object containing a GPS coordinate and the time this coordinate was set.
 */
public class Coordinate {
    private final double latitude;
    private final double longitude;
    private final long timestamp;

    private final static String TAG = "com.eit.minimap.datastructures.Coordinate";

    public Coordinate(double lat, double lon, long time) {
        this.latitude = lat;
        this.longitude = lon;
        this.timestamp = time;
    }

    public Coordinate(JSONObject obj) throws JSONException {
        this.latitude = obj.getDouble("lat");
        this.longitude = obj.getDouble("lon");
        this.timestamp = obj.getLong("time");
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public JSONObject convertToJSON(){
        try{
            JSONObject posPacket = new JSONObject();
            posPacket.put("lat", this.getLatitude());
            posPacket.put("lon", this.getLongitude());
            posPacket.put("time", this.getTimestamp());
            return posPacket;
        }catch(JSONException error){
            Log.e(TAG,"Error parsing JSON.",error);
            return null;

        }

    }
}
