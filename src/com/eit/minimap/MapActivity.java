package com.eit.minimap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.eit.minimap.datastructures.Coordinate;
import com.eit.minimap.datastructures.User;
import com.eit.minimap.datastructures.UserStore;
import com.eit.minimap.network.NetworkListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends Activity implements UserStore.UserStoreListener {
    private GoogleMap map;
    private MenuItem connectionProgress;

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

        //Customize ActionBar
        final ActionBar ab = getActionBar();
        // Hide some stuff
        ab.setDisplayOptions(0,ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_USE_LOGO);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Action bar inflation
        new MenuInflater(this).inflate(R.menu.action_menu,menu);
        connectionProgress = menu.findItem(R.id.connection_progress);
        connectionProgress.setActionView(R.layout.actionbar_indeterminate_progress);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void userPositionsChanged(UserStore store) {
        for(User user : store.getUsers()) {
            if(user.getPosition()==null) continue;
            Coordinate coord = user.getPosition();

            // Has a position, but no marker..
            if(user.getMarker()==null) {
                user.setMarker(map.addMarker(new MarkerOptions().position(coord.getLatLng())));
            } else if(user.getMarker()!=null) {
                user.getMarker().setPosition(coord.getLatLng());
            }
        }
    }

    @Override
    public void usersChanged(UserStore store) {
    }

    @Override
    public void connectionChanged(NetworkListener.Change c) {
        if(connectionProgress == null) return;
        switch (c) {
            case CONNECTING:
                connectionProgress.setActionView(R.layout.actionbar_indeterminate_progress);
                break;
            case CONNECTED:
                connectionProgress.setActionView(null); //TODO: Show green check mark instead
                break;
            case DISCONNECTED:
                connectionProgress.setActionView(null); //TODO: Show red cross instead.
                break;
        }
    }

    public PolylineOptions userDrawLine(User user, int clr){
        PolylineOptions tail = new PolylineOptions();
        tail.color(clr);
        for(Coordinate coord : user.getPositions()){
            LatLng latLng = coord.getLatLng();
            tail.add(latLng);
        }
        map.addPolyline(tail);
        return tail;
    }
}
