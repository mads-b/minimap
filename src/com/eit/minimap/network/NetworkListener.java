package com.eit.minimap.network;

import org.json.JSONObject;

/**
 * Network listener belonging to a TcpClient.
 * This interface is part of the JsonTcpClient listener pattern.
 */
public interface NetworkListener {
    /**
     * Called by a TcpClient to notify the listener of a received Json object.
     * @param pack Received Json object
     */
	void packageReceived(JSONObject pack);

}
