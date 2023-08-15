package com.dzavalishin.net;

public enum NetworkRecvStatus {
	OKAY,
	DESYNC,
	SAVEGAME,
	CONN_LOST,
	MALFORMED_PACKET,
	SERVER_ERROR, // The server told us we made an error
	SERVER_FULL,
	SERVER_BANNED,
	CLOSE_QUERY, // Done quering the server
}