package com.eit.minimap.gps;

import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;
import com.eit.minimap.R;

public class LocationProcessor implements LocationListener {
    private boolean displayedLocationProviderSelectionScreen = false;
    final GpsStatus status = null;
    private final LocationManager locationManager;
    private String provider;
    private final GpsStatusListener gpsStatusListener;
    private final Context context;

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
        provider = LocationManager.GPS_PROVIDER;
        if (!locationManager.isProviderEnabled(provider) && !displayedLocationProviderSelectionScreen) {
            displayedLocationProviderSelectionScreen = true;
            openSettings();
            Toast.makeText(context, R.string.gps_disabled, Toast.LENGTH_LONG).show();
            if (!locationManager.isProviderEnabled(provider) && !displayedLocationProviderSelectionScreen) {
                displayedLocationProviderSelectionScreen = true;
                openSettings();
                Toast.makeText(context, R.string.gps_disabled, Toast.LENGTH_LONG).show();
            }
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
     */
    public void startProvider() {
        // Requesting updates
        float minDistance = 0;
        //Update every second.
        long minTime = 1;
        locationManager.requestLocationUpdates(provider, minTime, minDistance,this);
    }

    /**
     * Stops the GPS.
     */
    public void stopProvider() {
        try{
            locationManager.removeUpdates(this);
            locationManager.removeGpsStatusListener(gpsStatusListener);
        }
        catch(IllegalArgumentException ignored) {} //Called if we call stop twice. No matter.
    }

    public void onDestroy() {
        stopProvider();
    }

    /**
     * Callback method from the GPS manager.
     * Will be called once every time we have an updated position.
     * @param location Newest GPS position.
     */
    public void onLocationChanged(Location location) {
        //TODO: Listener pattern: Send new GPS result to our listener
    }


    public void onProviderDisabled(String arg0) {}


    public void onProviderEnabled(String arg0) {}

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}



}

