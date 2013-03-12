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
import android.widget.Toast;
import com.eit.minimap.datastructures.Coordinate;
import com.eit.minimap.datastructures.User;
import com.eit.minimap.datastructures.UserStore;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements UserStore.UserStoreListener, MenuItem.OnMenuItemClickListener {
    private UserStore userStore;
    private GoogleMap map;
    private MenuItem progressBar;
    private HardwareManager hardwareManager;

    // Time scrubbing stuff.
    private boolean timeScrubbingActivated = false;

    // Remember the last state the network was in.
    private HardwareManager.NetworkState lastState;

    private final static String TAG = "com.eit.minimap.MapActivity";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            Log.d(TAG,"AnyLocation: "+loc.getLatitude()+" x "+loc.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(),loc.getLongitude())));
            map.animateCamera(CameraUpdateFactory.zoomBy(15f),5000,null);
        }

        /*
         * Init hardwaremanager. This will cause it to make the connection to the server.
         */
        hardwareManager = new HardwareManager(this);
        // Make HardwareManager start setting up positioning and networking.
        hardwareManager.init();

        userStore = new UserStore(hardwareManager);
        // Listen for changes in user data.
        userStore.registerListener(this);
        // Sets up a thread to periodically check what state the network is in.
        getNetworkStatePeriodically();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Action bar inflation
        new MenuInflater(this).inflate(R.menu.action_menu,menu);
        progressBar = menu.findItem(R.id.connection_progress);
        // Listen for events when time scrubbing is selected.
        menu.findItem(R.id.toggleScrubbing).setOnMenuItemClickListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void userPositionChanged(UserStore store, User user) {
        // Map doesn't care about users without positions.
        if(user.getPosition()==null) return;
        Coordinate coord = user.getPosition();

        // Has a position, but no marker..
        if(user.getMarker()==null) {
            user.setMarker(map.addMarker(new MarkerOptions().position(coord.getLatLng())));
        }
        // Has position and marker.
        else if(user.getMarker()!=null) {
            user.getMarker().setPosition(coord.getLatLng());
            //Remake polyline if time scrubbing is activated.
            if(timeScrubbingActivated)
                user.makePolyline(this.map);
        }
    }

    @Override
    public void userChanged(UserStore store, User user) {
        if(user.getPosition() == null) { // A user without a position is a new user!
            String connectMsg = this.getString(R.string.user_connected, user.getScreenName());
            Toast.makeText(this,connectMsg,Toast.LENGTH_LONG);
        } else { // Second case: If he has a position, he is in the process of disconnecting.
            String disconnectMsg = this.getString(R.string.user_disconnected, user.getScreenName());
            Toast.makeText(this,disconnectMsg,Toast.LENGTH_LONG);
        }
    }

    private void getNetworkStatePeriodically() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    HardwareManager.NetworkState newState = hardwareManager.getState();
                    if(lastState == newState || progressBar == null) continue; // No need to update.
                    lastState = newState;

                    switch (hardwareManager.getState()) {
                        case CONNECTING:
                            progressBar.setActionView(R.layout.actionbar_indeterminate_progress);
                            break;
                        case CONNECTED:
                            progressBar.setActionView(null);
                            progressBar.setIcon(getResources().getDrawable(R.drawable.check_mark));
                            break;
                        case DISCONNECTED:
                            progressBar.setActionView(null);
                            progressBar.setIcon(getResources().getDrawable(R.drawable.x_mark));
                            break;
                    }
                    //Check network state every .5 seconds.
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {}
                }
            }
        }).start();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggleScrubbing:
                timeScrubbingActivated ^= true;
                // Iterate over all users, making or adding "tail" depending on time scrubbing toggle.
                for(User user : userStore.getUsers()) {
                    if(timeScrubbingActivated)
                        user.makePolyline(this.map);
                    else
                        user.removePolyline();
                }
                break;
        }
        return true;
    }
}
