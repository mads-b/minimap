package com.eit.minimap.datastructures;

import android.location.Location;

public class User {
		//private Location pos;
		private String macAddr;
		private String screenName;
		private double lon;
		private double alt;
		
		
		User(Location pos, String macAddr, String screenName){
			this.lon = pos.getLongitude();
			this.alt = pos.getAltitude();
			this.macAddr = macAddr; 
			this.screenName = screenName;
		}
		
		public String getMacAddr(){
			return macAddr;
		}
		
}
