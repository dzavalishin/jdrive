package com.dzavalishin.net;

// Actions that can be used for NetworkTextMessage
public enum NetworkAction {
	JOIN,
	LEAVE,
	CHAT,
	CHAT_PLAYER,
	CHAT_CLIENT,
	GIVE_MONEY,
	NAME_CHANGE,;

	static NetworkAction values[] = values();
	
	static NetworkAction value(int v) {
		return values[v];
	}

	public static NetworkAction uiAction(int id) {
		// NetworkAction.CHAT + (id & 0xFF)
		return value(CHAT.ordinal() + (id & 0xFF) );
	}
}