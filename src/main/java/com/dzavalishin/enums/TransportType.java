package com.dzavalishin.enums;


/**
 * 
 * Transport type
 * 
 * 
 * 
 * <p>These constants are for now linked to the representation of bridges
 * and tunnels, so they can be used by GetTileTrackStatus_TunnelBridge
 * to compare against the map5 array. In an ideal world, these
 * constants would be used everywhere when accessing tunnels and
 * bridges. For now, you should just not change the values for road
 * and rail.
 */

public enum TransportType {

	Rail( 0 ),
	Road( 1 ),
	Water( 2 ),
	//public static final int TRANSPORT_END = 3;
	Invalid( 0xff );
	
	private final int value;
	
	TransportType(int value)
	{ 
		this.value = value; 
	}
	
	public int getValue() {
		return value;
	}
	
	//public static TransportType [] values = values();
	
	TransportType getEnum(int val)
	{
		TransportType ev = Invalid;
		
		switch(val) {
			case 0: ev = Rail; break;
			case 1: ev = Road; break;
			case 2: ev = Water; break;
		}
		
		return ev;
	}
		
}
