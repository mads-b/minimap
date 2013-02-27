package com.eit.minimap;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.eit.minimap.datastructures.Coordinate;
import com.eit.minimap.datastructures.User;
import com.eit.minimap.datastructures.UserStore;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collection;

public class MapActivity extends Activity implements UserStore.UserStoreListener {
    private GoogleMap map;

    private final static String TAG = "com.eit.minimap.MapActivity";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The following three lines activates the GPS and connects to server.
        //UserStore users = new UserStore(this);
        //users.registerListener(this);
        //users.init();

        setContentView(R.layout.map);
        final MapFragment mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = mapFrag.getMap();
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Start of fetching ANY location. Only for centering the map on a logical area.
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        final Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.NO_REQUIREMENT);
        final String provider = lm.getBestProvider(crit, true);
        final Location loc = lm.getLastKnownLocation(provider);
        if(loc != null) {
            Log.d(TAG,"AnyLocation: "+loc.getLatitude()+" x "+loc.getLongitude()+"Vis?: "+mapFrag.isVisible());
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(),loc.getLongitude())));
            map.animateCamera(CameraUpdateFactory.zoomBy(15f),5000,null);
        }
    }

    @Override
    public void userPositionsChanged(UserStore store) {
        for(User user : store.getUsers()) {
            if(user.getPosition()==null) continue;
            Coordinate coord = user.getPosition();
            LatLng latLng = new LatLng(coord.getLatitude(),coord.getLongitude());

            // Has a position, but no marker..
            if(user.getMarker()==null) {
                user.setMarker(map.addMarker(new MarkerOptions().position(latLng)));
            } else if(user.getMarker()!=null) {
                user.getMarker().setPosition(latLng);
            }
        }
    }

    @Override
    public void usersChanged(UserStore store) {
    }
}
