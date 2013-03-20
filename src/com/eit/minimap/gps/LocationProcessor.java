package com.eit.minimap.gps;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;
import com.eit.minimap.R;

/**
 * Main class for getting GPS coordinates of device position.
 * To use: Instantiate -> initializeProvider() -> startProvider().
 */
public class LocationProcessor {
    private boolean displayedLocationProviderSelectionScreen = false;
    private final LocationManager locationManager;
    private String provider;
    private final GpsStatusListener gpsStatusListener;
    private final Context context;
    private LocationListener listener;

    public LocationProcessor(Context c) {
        this.context = c;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        gpsStatusListener = new GpsStatusListener(context,locationManager);
    }

    /**
     * Sends the user to the settings screen if it is not enabled.
     * This method does not start the GPS to make it start positioning,
     * it merely checks for its existence.
     */
    public void initializeProvider() {
        String gpsProvider = LocationManager.GPS_PROVIDER;
        if (!locationManager.isProviderEnabled(gpsProvider) && !displayedLocationProviderSelectionScreen) {
            displayedLocationProviderSelectionScreen = true;
            openSettings();
            Toast.makeText(context, R.string.gps_disabled, Toast.LENGTH_LONG).show();
        } else {
            locationManager.addGpsStatusListener(gpsStatusListener);
        }
    }

    /**
     * Opens the settings menu for GPSs, to allow the user the possibility of enabling his GPS.
     */
    private void openSettings() {
        try {
            Intent location_settings = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(location_settings);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Starts the GPS. This processor will start receiving location updates shortly.
     * @param listener Listener that will receive GPS status updates.
     */
    public void startProvider(LocationListener listener) {
        this.listener = listener;
        // Requesting updates
        float minDistance = 0;
        //Update every second.
        long minTime = 1;
        // Make a provider that doesn't care about coord quality. GPS should be switched on though.
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        provider = locationManager.getBestProvider(criteria,true);
        locationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
    }

    /**
     * Stops the GPS.
     */
    void stopProvider() {
        try{
            locationManager.removeUpdates(listener);
            locationManager.removeGpsStatusListener(gpsStatusListener);
        }
        catch(IllegalArgumentException ignored) {} //Called if we call stop twice. No matter.
    }

    public void onDestroy() {
        stopProvider();
    }
}

