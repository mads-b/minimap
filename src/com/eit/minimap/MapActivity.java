package com.eit.minimap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.eit.minimap.datastructures.MessageHandler;
import com.eit.minimap.datastructures.POI;
import com.eit.minimap.datastructures.POIHandler;
import com.eit.minimap.datastructures.User;
import com.eit.minimap.datastructures.UserStore;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

public class MapActivity extends Activity implements
        UserStore.UserStoreListener,
        MenuItem.OnMenuItemClickListener,
        POIHandler.POIListener {
    private UserStore userStore;
    private MessageHandler messageHandler;
    private ChatDialogHandler chat;
    private GoogleMap map;
    private MenuItem progressBar;
    private HardwareManager hardwareManager;
    private POIHandler poiHandler;

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
        if(provider == null) return;
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
        // Get some preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String ourScreenName = prefs.getString("yourName", getResources().getString(R.string.def_your_name));
        int icon = prefs.getInt("iconName", getResources().getInteger(R.integer.def_icon_name));

        //Make Userstore and subscribe to updates
        userStore = new UserStore(hardwareManager,ourScreenName,UserIcons.ICONS[icon]);
        userStore.registerListener(this);

        //Make our MessageHandler and subscribe to updates
        messageHandler = new MessageHandler(hardwareManager);
        chat = new ChatDialogHandler(this,userStore,messageHandler);

        //Make Point Of Interest handler and subscribe to updates. Also, give PoiHandler info when markers or map is long clicked.
        poiHandler = new POIHandler(hardwareManager,this);
        poiHandler.registerListener(this);
        map.setOnMarkerClickListener(poiHandler);
        map.setOnMapLongClickListener(poiHandler);

        // Make HardwareManager start setting up positioning and networking.
        hardwareManager.init();

        // Sets up a thread to periodically check what state the network is in.
        getNetworkStatePeriodically();
    }

    public GoogleMap getMap() { return map; }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Action bar inflation
        new MenuInflater(this).inflate(R.menu.action_menu,menu);
        progressBar = menu.findItem(R.id.connection_progress);
        // Listen for events when tools are selected.
        menu.findItem(R.id.toggleScrubbing).setOnMenuItemClickListener(this);
        menu.findItem(R.id.chatDialog).setOnMenuItemClickListener(this);
        menu.findItem(R.id.exitApp).setOnMenuItemClickListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void userPositionChanged(final User user) {
        //Remake polyline if time scrubbing is activated.
        final MapActivity t = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                user.updateMarker(t);
                if(!timeScrubbingActivated) return;
                user.makePolyline(t.getMap());
            }
        });
    }

    @Override
    public void userChanged(final User user) {
        final String connectMsg = this.getString(R.string.user_connected, user.getScreenName());
        final String disconnectMsg = this.getString(R.string.user_disconnected, user.getScreenName());
        final Context context = this;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(user.getPosition() == null) { // A user without a position is a new user!
                    //Just toast. User handles the marker stuff himself.
                    Toast.makeText(context,connectMsg,Toast.LENGTH_LONG).show();
                } else { // Second case: If he has a position, he is in the process of disconnecting.

                    Toast.makeText(context,disconnectMsg,Toast.LENGTH_LONG).show();
                }
            }});
    }

    private void getNetworkStatePeriodically() {
        new Thread(new Runnable() {
            final Runnable check = new Runnable() {
                @Override
                public void run() {
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
                }
            };

            @Override
            public void run() {
                while(true) {
                    HardwareManager.NetworkState newState = hardwareManager.getState();
                    if(lastState != newState && progressBar != null) {
                        lastState = newState;
                        runOnUiThread(check);
                    }
                    //Check network state every second.
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }).start();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.toggleScrubbing:
                Log.d(TAG,"Toggled time scrubbing");
                timeScrubbingActivated ^= true;
                // Iterate over all users, making or adding "tail" depending on time scrubbing toggle.
                for(User user : userStore.getUsers()) {
                    if(timeScrubbingActivated)
                        user.makePolyline(this.map);
                    else
                        user.removePolyline();
                }
                break;
            case R.id.chatDialog:
                Log.d(TAG,"Showing Chat Dialog");
                chat.show();
                break;
            case R.id.exitApp:
                shutdown();
                break;
        }
        return true;
    }

    @Override
    public void onPoiAdded(final POI poi) {
        // POI added. Just add the marker.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                poi.makeMarker(map);
            }
        });
    }

    private void shutdown() {
        Log.d(TAG,"Shutting down the multiplayer map. Notifying all recipients.");
        try {
            hardwareManager.sendPackage(new JSONObject()
                    .put("type", "disc")
                    .put("macAddr", userStore.getMyUser().getMacAddr()));
        } catch (JSONException ignored) {}
        hardwareManager.shutdown();
        Log.d(TAG,"Hardware deactivated. Changing activity to MainActivity.");
        // After shutdown, return to main menu.
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
    }
}
