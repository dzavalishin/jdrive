package com.dzavalishin.net;

public enum DestType {
	BROADCAST,
	PLAYER,
	CLIENT;

	static DestType [] values = values();
	
	public static DestType value(int v) {
		return values[v];
	}
}
