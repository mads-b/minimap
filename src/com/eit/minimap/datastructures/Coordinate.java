package com.eit.minimap.datastructures;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simple data object containing a GPS coordinate and the time this coordinate was set.
 */
public class Coordinate {
    private final long timestamp;
    private final LatLng latLng;
    private final static String TAG = "com.eit.minimap.datastructures.Coordinate";

    public Coordinate(LatLng latLng, long time) {
        //this.latitude = lat;
        //this.longitude = lon;
        this.latLng = latLng;
        this.timestamp = time;
    }

    public Coordinate(JSONObject obj) throws JSONException {
        double latitude = obj.getDouble("lat");
        double longitude = obj.getDouble("lon");
        this.latLng = new LatLng(latitude, longitude);
        this.timestamp = obj.getLong("time");
    }

    public LatLng getLatLng(){
        return latLng;
    }

    long getTimestamp() {
        return timestamp;
    }
    public JSONObject convertToJSON(){
        try{
            LatLng lngToSend = this.getLatLng();
            JSONObject posPacket = new JSONObject();
            posPacket.put("type", "pos");
            posPacket.put("lat", lngToSend.latitude);
            posPacket.put("lon", lngToSend.longitude);
            posPacket.put("time", this.getTimestamp());
            return posPacket;
        }catch(JSONException error){
            Log.e(TAG,"Error parsing JSON.",error);
            return null;

        }

    }
}
