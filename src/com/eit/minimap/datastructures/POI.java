package com.eit.minimap.datastructures;

import android.content.res.Resources;
import android.graphics.Color;
import com.eit.minimap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Point of interest. Addable to the map. Shows up as a red exclamation mark. Cannot be moved.
 */
public class POI {
    /** GoogleMap marker. */
    private Marker marker;
    private Circle circle;
    private final LatLng position;
    private final int radius;
    private final String description;
    private final long id;
    private final String macAddr;

    /**
     * Makes a new Point of Interest.
     * @param position Position of the point
     * @param radius Radius of the point, in meters
     * @param description Description shown when the point is selected.
     */
    public POI(LatLng position,int radius, String description, String macAddr) {
        this.position=position;
        this.radius=radius;
        this.description=description;
        this.id = System.currentTimeMillis();
        this.macAddr = macAddr;
    }

    public POI(JSONObject object) throws JSONException {
        this.position = new LatLng(
                object.getDouble("lat"),
                object.getDouble("lng"));
        this.radius = object.getInt("rad");
        this.description = object.getString("desc");
        this.id = object.getLong("id");
        this.macAddr = object.getString("macAddr");
    }

    public String getDescription() { return description; }
    public long getId() { return id; }

    /**
     * Adds this point of interest to the map.
     * NOTE: MUST BE CALLED FROM UI THREAD!
     * @param map Map to add marker to
     */
    public void makeMarker(GoogleMap map) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.exclamation);

        marker = map.addMarker(
                new MarkerOptions()
                        .position(position)
                        .snippet(description)
                        .title("POI:"+id)
                        .icon(icon));
        circle = map.addCircle(
                new CircleOptions()
                        .center(position)
                        .radius(radius)
                        .strokeColor(Color.YELLOW)
                        .fillColor(Color.argb(128, 255, 255, 0))
                        .strokeWidth(2));
    }

    /**
     * Removes this Point Of Interest from the map.
     * NOTE: MUST BE CALLED FROM UI THREAD!
     */
    public void removeMarker() {
        marker.remove();
        circle.remove();
    }


    public JSONObject toJson() {
        try{
            return new JSONObject()
                    .put("type", "poi")
                    .put("lat", position.latitude)
                    .put("lng",position.longitude)
                    .put("rad",radius)
                    .put("desc",description)
                    .put("id",id)
                    .put("macAddr", macAddr);
            //Need to add screenName and AvatarImage
        } catch (JSONException ignored) {}
        return null;
    }

    public int hashCode() {
        return (int) (id % 10e7);
    }

    public boolean equals(Object o) {
        if(!(o instanceof POI)) return false;
        return ((POI)o).id == id;
    }
}
