
package com.eit.minimap.gps;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.widget.Toast;
import com.eit.minimap.R;

/**
 * GPS status listener. This class only listens to status messages sent from the GPS.
 * This means this class has control over how many satellites we are connected to and
 * whether we have a fix on a position.
 * TODO: Implement a listener pattern so someone can listen in on GPS fixes and satellite quantity.
 */
class GpsStatusListener implements GpsStatus.Listener{

    private final LocationManager locationManager;
    private GpsStatus gpsStatus;
    private final Context context;

    public GpsStatusListener(Context c, LocationManager l) {
        this.locationManager=l;
        this.context = c;
    }

    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                gpsStatus = locationManager.getGpsStatus(gpsStatus);
                int sat_counter = 0;
                for (GpsSatellite gpsSatellite : gpsStatus.getSatellites()) {
                    sat_counter++;
                }
                //TODO: Possibly inform someone of the amount of satellites connected ATM.

                break;
            case GpsStatus.GPS_EVENT_STARTED:

                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Toast.makeText(context, R.string.first_gps_fix, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
