package com.eit.minimap;

import android.content.Context;
import android.location.LocationListener;
import android.net.wifi.WifiManager;
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
public class HardwareManager implements ClientConnectThread.TcpClientRecipient,NetworkListener {
    private JsonTcpClient networkClient;
    private final LocationProcessor locationProcessor;
    private final Context context;

    // Cache to store all NetworkListeners before we have a working network.
    private final Set<NetworkListener> listenerCache = new HashSet<NetworkListener>();

    public HardwareManager(Context context) {
        this.context = context;
        locationProcessor = new LocationProcessor(context);
    }

    public void init() {
        // Init networking.
        new ClientConnectThread(context,this).execute();

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
    public void receiveTcpClient(JsonTcpClient client) {
        this.networkClient = client;
        for(NetworkListener listener : listenerCache) {
            client.addListener(listener);
        }
        client.addListener(this);
    }

    @Override
    public void onPackageReceived(JSONObject pack) {}

    @Override
    public void onConnectionChanged(Change c) {
        if(c == Change.FAILED) {
            new ClientConnectThread(context,this).execute();
        }
    }
}
