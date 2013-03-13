package com.eit.minimap.datastructures;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
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
    private UserStoreListener listener;
    private final static int MIN_POS_SEND_INTERVAL = 1000;
    private final static String TAG = "com.eit.minimap.datastructures.UserStore";
    /** Mac adress of the phone running this application. */
    private final User myUser;

    // Handler for Hardware interaction, like network, GPS etc..
    private final HardwareManager hardwareManager;

    private long timeSinceLastSentPacket;

    public UserStore(HardwareManager manager, String screenName){
        this.hardwareManager = manager;
        myUser = new User(manager.getMacAddress(), screenName);
        // Subscribe to some data
        manager.subscribeToNetworkUpdates(this);
        manager.subscribeToLocationUpdates(this);

        //Add our user!
        users.put(myUser.getMacAddr(),myUser);

        timeSinceLastSentPacket = 0;
        //sending pInfo packet to server
        JSONObject pInfoPacket = costructPInfoPacket();
        hardwareManager.sendPackage(pInfoPacket);

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
                if(listener!=null) {
                    listener.userPositionChanged(this, usr);
                }
            }
            // Got user information
            else if(type.equals("pInfo")){
                User newUser = new User(mcAdr,pack.getString("screenName"));
                users.put(newUser.getMacAddr(), newUser);
                Log.d(TAG,"Added new user with name "+newUser.getScreenName()+" and MAC "+newUser.getMacAddr());
                if(listener!=null) {
                    listener.userChanged(this, newUser);
                }
            }
            // Got disconnect message.
            else if(users.containsKey(mcAdr) && type.equals("disc")){
                User discUser = users.get(mcAdr);
                users.remove(discUser.getMacAddr());
                if(listener!=null) {
                    listener.userChanged(this, discUser);
                }
            }else{
                Log.e(TAG,"Received unknown packet or failed to receive packet. Contents: "+pack.toString());
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
        users.get(myUser.getMacAddr()).addPosition(coord);

        if(System.currentTimeMillis()- timeSinceLastSentPacket > MIN_POS_SEND_INTERVAL ){
            JSONObject posPacket = coord.convertToJSON();
            try {
                posPacket.put("macAddr",myUser.getMacAddr());
            } catch (JSONException ignored) {}
            // Send new user position
            hardwareManager.sendPackage(posPacket);
            timeSinceLastSentPacket = System.currentTimeMillis();
        }

        if(listener!=null) {
            listener.userPositionChanged(this,users.get(myUser.getMacAddr()));
        }
    }

    public Collection<User> getUsers(){
        return Collections.unmodifiableCollection(users.values());
    }

    public void registerListener(UserStoreListener listener) {
        this.listener=listener;
    }

    public interface UserStoreListener {
        /**
         * Called when a user position is changed.
         * @param store This store
         * @param user The user whose position changed
         */
        void userPositionChanged(UserStore store, User user);

        /**
         * Called when a user is added or removed from the store, or otherwise changed.
         * @param store This store
         * @param user The user in question.
         */
        void userChanged(UserStore store, User user);
    }

    public JSONObject costructPInfoPacket(){
        try{
            JSONObject pInfoPacket = new JSONObject();
            pInfoPacket.put("type", "pInfo");
            pInfoPacket.put("macAddr", myUser.getMacAddr());
            pInfoPacket.put("screenName",myUser.getScreenName());
            //Need to add screenName and AvatarImage
            return pInfoPacket;
        }catch(JSONException error){
            Log.e(TAG,"Error parsing JSON.",error);
            return null;

        }
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
