package com.eit.minimap;

import android.content.Context;
import android.location.LocationListener;
import android.net.wifi.WifiManager;
import android.widget.Toast;
import com.eit.minimap.gps.LocationProcessor;
import com.eit.minimap.network.ClientConnectThread;
import com.eit.minimap.network.JsonTcpClient;
import com.eit.minimap.network.NetworkListener;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Main class for managing hardware resources, connections and data extraction.
 * Pass this instance around to classes needing hardware access of any kind.
 */
public class HardwareManager implements ClientConnectThread.TcpClientRecipient {
    private JsonTcpClient networkClient;
    private final LocationProcessor locationProcessor;
    private final Context context;
    private NetworkState state = NetworkState.DISCONNECTED;

    // Cache to store all NetworkListeners before we have a working network.
    private final Set<NetworkListener> listenerCache = new HashSet<NetworkListener>();

    public HardwareManager(Context context) {
        this.context = context;
        locationProcessor = new LocationProcessor(context);
    }

    public void init() {
        // Init networking.
        new ClientConnectThread(context,this).execute();
        state = NetworkState.CONNECTING;

        // Init GPS. Does not start polling for updates.
        locationProcessor.initializeProvider();
    }

    public String getMacAddress() {
        // Get MAC address:
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConnectionInfo().getMacAddress();
    }

    /**
     * Starts the GPS. Listener receives GPS coords.
     * @param listener Listener to receive coords.
     */
    public void subscribeToLocationUpdates(LocationListener listener) {
        locationProcessor.startProvider(listener);
    }

    public void subscribeToNetworkUpdates(NetworkListener listener) {
        if(networkClient != null) networkClient.addListener(listener);
        listenerCache.add(listener);
    }

    public void sendPackage(JSONObject object) {
        if(networkClient != null) {
            networkClient.sendData(object);
        }
    }

    @Override
    public void receiveTcpClient(JsonTcpClient client, String error) {
        if(client == null) {
            //Connection failed. Try again.
            Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
            new ClientConnectThread(context,this).execute();

            return; //Failed. return.
        }

        this.networkClient = client;
        // Received a TCP client. Add all network listeners we know of from before.
        for(NetworkListener listener : listenerCache) {
            client.addListener(listener);
        }
    }

    public NetworkState getState() { return state; }

    public enum NetworkState  {
        DISCONNECTED,CONNECTED,CONNECTING;
    }
}
