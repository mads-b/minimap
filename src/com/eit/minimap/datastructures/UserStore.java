package com.eit.minimap.datastructures;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.eit.minimap.gps.LocationProcessor;
import com.eit.minimap.network.ClientConnectThread;
import org.json.JSONException;
import org.json.JSONObject;

import com.eit.minimap.network.JsonTcpClient;
import com.eit.minimap.network.NetworkListener;

public class UserStore implements NetworkListener {
	/** Map containing all the users of this application. The key is the mac adress of the phone. */
    private Map<String, User> users;
    /** Network communicator. Not always set, so check if it's null when using it. */
    private JsonTcpClient network;
    /** Location resolver. Calls this store periodically to update current users' position */
    private LocationProcessor processor;

    private final static String TAG = "com.eit.minimap.datastructures.UserStore";
    /** Mac adress of the phone running this application. */
    private String myMac;

    public UserStore(Context c){
        users = new HashMap<String, User>();

        // Get MAC address:
        WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        String mac = wifiManager.getConnectionInfo().getMacAddress();
        myMac = mac;
        //Add our user!
        users.put(myMac,new User(myMac,"TODO: Screenname here"));

    }

    public void init(Context c) {
        //Starts the connection process to host. Tcp client is received on completion.
        new ClientConnectThread(c,this).execute();
        processor.initializeProvider();
        processor.startProvider();
    }

    public void addUser(User usr){
        users.put(usr.getMacAddr(),usr);
    }
    public void delUser(User usr){
        users.remove(usr);
    }
    @Override
    public void packageReceived(JSONObject pack) {
        try{
            String mcAdr = pack.getString("macAddr");
            if(users.containsKey(mcAdr)){
                User usr = users.get(mcAdr);
                //update Coordinate
                Coordinate newCord = new Coordinate(pack);
                usr.addPosition(newCord);


            }else{
            	//Inkludere screenName i pack? Lar denne stå som someName foreløbig
            	User newUser = new User(mcAdr,"someName");
            	addUser(newUser);
            }
        }catch(JSONException error){
            Log.e(TAG,"Error! Certain fields missing in received pack (missing MacAddr or type?)\n"+pack.toString());
        }
    }

    public void locationChanged(Location location){
        // Re-wrap location
        Coordinate coord = new Coordinate(
                location.getLatitude(),
                location.getLongitude(),
                System.currentTimeMillis());
        users.get(myMac).addPosition(coord);
        // TODO: Send new position to server? Maybe not all the time?
        // Can construct global variable timeSinceLastSent, and check that a significant amount of time has passed.
        // Construct JSON object
        try{
        	sendPosPacket(coord);
        }catch(JSONException error){

        }
    }
    public void sendPosPacket(Coordinate coord) throws JSONException{
    	JSONObject posPacket = coord.convertToJSON();
        posPacket.put("type","pos");
        posPacket.put("macAddr",myMac);
        // call network.sendData(JSON json)
        network.sendData(posPacket);
    }

    @Override
    public void receiveTcpClient(JsonTcpClient client) {
        this.network = client;
        network.addListener(this);
    }
}


