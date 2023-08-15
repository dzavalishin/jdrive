package com.dzavalishin.net;

public enum NetworkErrorCode {
	GENERAL, // Try to use thisone like never

	// Signals from clients
	DESYNC,
	SAVEGAME_FAILED,
	CONNECTION_LOST,
	ILLEGAL_PACKET,

	// Signals from servers
	NOT_AUTHORIZED,
	NOT_EXPECTED,
	WRONG_REVISION,
	NAME_IN_USE,
	WRONG_PASSWORD,
	PLAYER_MISMATCH, // Happens in CLIENT_COMMAND
	KICKED,
	CHEATER,;

	static NetworkErrorCode values[] = values();
	
	static NetworkErrorCode value(int ord) {
		return values[ord];
	}
}