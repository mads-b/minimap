package com.eit.minimap.datastructures;

import android.app.Activity;
import android.util.Log;
import com.eit.minimap.HardwareManager;
import com.eit.minimap.POIDialog;
import com.eit.minimap.network.NetworkListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler administrating Points Of Interest.
 */
public class POIHandler implements NetworkListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {
    private final HardwareManager manager;
    private final Activity activity;

    private final Map<Long,POI> points = new HashMap<Long,POI>();

    //Listener for POI events. Dummy listener added to avoid checking for null.
    private POIListener listener = new POIListener() {
        public void onPoiAdded(POI poi) {}};

    private final static String TAG = "com.eit.minimap.network.POIHandler";

    public POIHandler(HardwareManager manager, Activity activity){
        this.manager = manager;
        this.activity = activity;
        manager.subscribeToNetworkUpdates(this);
    }

    public void onPackageReceived(JSONObject pack) {
        try{
            String type = pack.getString("type");

            // Check if this is a POI
            if(type.equals("poi")) {
                POI newPoi = new POI(pack);
                points.put(pack.getLong("id"), newPoi);
                listener.onPoiAdded(newPoi);
            }
            // If type is removePoi, remove the point instead of adding it.
            else if(type.equals("removePoi")) {
                final POI poi = points.remove(pack.getLong("id"));
                if(poi != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            poi.removeMarker();
                        }
                    });
                }
            }
        }catch(JSONException error){
            Log.e(TAG, "Error! Certain fields missing in received pack!\n" + pack.toString(), error);
        }
    }

    public void addPOI(POI poi) {
        manager.sendPackage(poi.toJson());
    }

    public void removePOI(POI poi) {
        try {
            //Send a modified POI message to remove it.
            manager.sendPackage(poi.toJson().put("type","removePoi"));
        } catch (JSONException ignored) {}
    }


    public void registerListener(POIListener listener) {
        this.listener = listener;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        new POIDialog(this, activity).makeNewPoiDialog(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //Not interested in anything but POI's
        if(!marker.getTitle().startsWith("POI:")) return false;
        //Fetch id in header of marker, and make dialog.
        long id = Long.parseLong(marker.getTitle().split(":")[1]);
        new POIDialog(this, activity).makeViewPoiDialog(points.get(id));
        return true;
    }

    public interface POIListener {
        void onPoiAdded(POI poi);
    }
}
