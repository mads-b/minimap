package com.eit.minimap.network;

import org.json.JSONObject;

public interface NetworkListener {
	void packageReceived(JSONObject pack);

}
