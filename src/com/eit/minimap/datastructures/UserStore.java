package com.eit.minimap.datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.eit.minimap.network.JsonTcpClient;
import com.eit.minimap.network.NetworkListener;

public class UserStore implements NetworkListener{
		private Map<String, User> users;
		private JsonTcpClient network;
		
		UserStore(JsonTcpClient network){
			users = new HashMap<String, User>(); 
			this.network = network;
			network.addListener(this);
		}
		public void addUser(User usr){
			users.put(usr.getMacAddr(),usr);
		}
		public void delUser(User usr){
			users.remove(usr);
		}
		@Override
		public void packageReceived(JSONObject pack) {
			// TODO Auto-generated method stub
			try{
				String mcAdr = pack.get("macAddr");
				if(users.containsKey(mcAdr)){
					User usr = users.get(mcAdr);
					//usr.setAltitude(alt)
				}
			}catch(JSONException error){
				
			}
			
		}
		
}


