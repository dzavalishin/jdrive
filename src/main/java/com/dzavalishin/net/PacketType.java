package com.dzavalishin.net;

public enum PacketType {

	SERVER_FULL,
	SERVER_BANNED,
	CLIENT_JOIN,
	SERVER_ERROR,
	CLIENT_COMPANY_INFO,
	SERVER_COMPANY_INFO,
	SERVER_CLIENT_INFO,
	SERVER_NEED_PASSWORD,
	CLIENT_PASSWORD,
	SERVER_WELCOME,
	CLIENT_GETMAP,
	SERVER_WAIT,
	SERVER_MAP,
	CLIENT_MAP_OK,
	SERVER_JOIN,
	SERVER_FRAME,
	SERVER_SYNC,
	CLIENT_ACK,
	CLIENT_COMMAND,
	SERVER_COMMAND,
	CLIENT_CHAT,
	SERVER_CHAT,
	CLIENT_SET_PASSWORD,
	CLIENT_SET_NAME,
	CLIENT_QUIT,
	CLIENT_ERROR,
	SERVER_QUIT,
	SERVER_ERROR_QUIT,
	SERVER_SHUTDOWN,
	SERVER_NEWGAME,
	SERVER_RCON,
	CLIENT_RCON,
	END, // Should ALWAYS be on the end of this list!! (period)

	UDP_CLIENT_FIND_SERVER,
	UDP_SERVER_RESPONSE,
	UDP_CLIENT_DETAIL_INFO,
	UDP_SERVER_DETAIL_INFO, // Is not used in OpenTTD itself, only for external querying
	UDP_SERVER_REGISTER, // Packet to register itself to the master server
	UDP_MASTER_ACK_REGISTER, // Packet indicating registration has succedeed
	UDP_CLIENT_GET_LIST, // Request for serverlist from master server
	UDP_MASTER_RESPONSE_LIST, // Response from master server with server ip's + port's
	UDP_SERVER_UNREGISTER, // Request to be removed from the server-list
	PACKET_UDP_END,
	;

	static boolean isUdpRange(int type) {
		return type >= UDP_CLIENT_FIND_SERVER.ordinal() && type < PACKET_UDP_END.ordinal();
	} 

	boolean isUdpRange() { return isUdpRange(ordinal()); }
	
	
	
}
