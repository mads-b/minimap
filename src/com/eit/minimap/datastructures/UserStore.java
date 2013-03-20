package com.eit.minimap.datastructures;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import com.eit.minimap.HardwareManager;
import com.eit.minimap.network.NetworkListener;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserStore implements NetworkListener,LocationListener {
    /** Map containing all the users of this application. The key is the mac adress of the phone. */
    private final Map<String, User> users = new HashMap<String, User>();

    // Listener to this store. When user states change, this gets called.
    // The listener is set from the get-go to ensure it's never null.
    private UserStoreListener listener = new UserStoreListener() {
        public void userPositionChanged(User user) {}
        public void userChanged(User user) {}};

    private final static int MIN_POS_SEND_INTERVAL = 1000;
    private final static String TAG = "com.eit.minimap.datastructures.UserStore";
    /** Mac adress of the phone running this application. */
    private final User myUser;

    // Handler for Hardware interaction, like network, GPS etc..
    private final HardwareManager hardwareManager;

    private long timeSinceLastSentPacket;

    public UserStore(HardwareManager manager, String screenName, int avatarIcon){
        this.hardwareManager = manager;

        myUser = new User(manager.getMacAddress(), screenName, avatarIcon);
        // Subscribe to some data
        manager.subscribeToNetworkUpdates(this);
        manager.subscribeToLocationUpdates(this);

        //Add our user!
        users.put(myUser.getMacAddr(),myUser);

        timeSinceLastSentPacket = 0;
        //sending pInfo packet to server
        hardwareManager.sendPackage(myUser.toJson());

    }

    @Override
    public void onPackageReceived(JSONObject pack) {
        try{
            String mcAdr = pack.getString("macAddr");
            String type = pack.getString("type");
            // Got a new user position
            if(users.containsKey(mcAdr) && type.equals("pos")){
                User usr = users.get(mcAdr);
                //update Coordinate
                Coordinate newCord = new Coordinate(pack);
                usr.addPosition(newCord);
                listener.userPositionChanged(usr);
            }
            // Got user information
            else if(type.equals("pInfo")){
                Log.d(TAG,"Users: "+users.toString());
                //Return if we got this user already..
                if(users.containsKey(mcAdr)) return;

                User newUser = new User(pack);
                users.put(newUser.getMacAddr(), newUser);
                Log.d(TAG,"Added new user with name "+newUser.getScreenName()+" and MAC "+newUser.getMacAddr());
                listener.userChanged(newUser);
            }
            // Got disconnect message.
            else if(users.containsKey(mcAdr) && type.equals("disc")){
                User discUser = users.get(mcAdr);
                users.remove(discUser.getMacAddr());
                listener.userChanged(discUser);
            }
        }catch(JSONException error){
            Log.e(TAG,"Error! Certain fields missing in received pack (missing MacAddr or type?)\n"+pack.toString(),error);
        }
    }

    @Override
    public void onLocationChanged(Location location){
        // Re-wrap location
        Coordinate coord = new Coordinate(
                new LatLng(location.getLatitude(),location.getLongitude()),
                System.currentTimeMillis());
        //users.get(myUser.getMacAddr()).addPosition(coord);

        if(System.currentTimeMillis()- timeSinceLastSentPacket > MIN_POS_SEND_INTERVAL ){
            JSONObject posPacket = coord.convertToJSON();
            try {
                posPacket.put("macAddr",myUser.getMacAddr());
            } catch (JSONException ignored) {}
            // Send new user position
            hardwareManager.sendPackage(posPacket);
            timeSinceLastSentPacket = System.currentTimeMillis();
        }
        //listener.userPositionChanged(users.get(myUser.getMacAddr()));
    }

    public Collection<User> getUsers(){
        return Collections.unmodifiableCollection(users.values());
    }

    public User getUserWithMac(String mac) {
        return users.get(mac);
    }

    public User getMyUser() {
        return myUser;
    }

    public void registerListener(UserStoreListener listener) {
        this.listener=listener;
    }

    public interface UserStoreListener {
        /**
         * Called when a user position is changed.
         * @param user The user whose position changed
         */
        void userPositionChanged(User user);

        /**
         * Called when a user is added or removed from the store, or otherwise changed.
         * @param user The user in question.
         */
        void userChanged(User user);
    }

    /*
     * Unused methods below. Use if necessary. (Transmitting GPS accuracy and the like.)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
}
