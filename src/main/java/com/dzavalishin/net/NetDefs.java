package com.dzavalishin.net;

import com.dzavalishin.game.Global;

public interface NetDefs {

	public static final int NETWORK_MASTER_SERVER_VERSION = 0; // 0 for 1st release of NextTTD
	public static final int NETWORK_COMPANY_INFO_VERSION  = 0; // 0 for 1st release of NextTTD

	// If this line is enable, every frame will have a sync test
	//  this is not needed in normal games. Normal is like 1 sync in 100
	//  frames. You can enable this if you have a lot of desyncs on a certain
	//  game.
	// Remember: both client and server have to be compiled with this
	//  option enabled to make it to work. If one of the two has it disabled
	//  nothing will happen.
	//public static final int  ENABLE_NETWORK_SYNC_EVERY_FRAME

	// In theory sending 1 of the 2 seeds is enough to check for desyncs
	//   so in theory, this next define can be left off.
	//public static final int  NETWORK_SEND_DOUBLE_SEED

	// How many clients can we have? Like.. MAX_PLAYERS - 1 is the amount of
	//  players that can really play.. so.. a max of 4 spectators.. gives us..
	//  MAX_PLAYERS + 3
	public static final int MAX_CLIENTS = (Global.MAX_PLAYERS + 3);


	// Do not change this next line. It should _ALWAYS_ be MAX_CLIENTS + 1
	public static final int  MAX_CLIENT_INFO = (MAX_CLIENTS + 1);

	/* Stuff for the master-server */
	public static final int  NETWORK_MASTER_SERVER_PORT = 3978; // TODO port
	public static final String  NETWORK_MASTER_SERVER_HOST  = "master.openttd.org"; // TODO host
	public static final String  NETWORK_MASTER_SERVER_WELCOME_MESSAGE = "OpenTTDRegister";

	public static final int  NETWORK_DEFAULT_PORT = 3979; // TODO port

	public static final int  MAX_INTERFACES = 9;


	// How many vehicle/station types we put over the network
	public static final int  NETWORK_VEHICLE_TYPES = 5;
	public static final int  NETWORK_STATION_TYPES = 5;

	//enum {
	public static final int  NETWORK_NAME_LENGTH        = 80;
	public static final int  NETWORK_HOSTNAME_LENGTH    = 80;
	public static final int  NETWORK_REVISION_LENGTH    = 10;
	public static final int  NETWORK_PASSWORD_LENGTH    = 20;
	public static final int  NETWORK_PLAYERS_LENGTH     = 200;
	public static final int  NETWORK_CLIENT_NAME_LENGTH = 25;
	public static final int  NETWORK_RCONCOMMAND_LENGTH = 500;

	public static final int  NETWORK_NUM_LANGUAGES      = 4;
	//};



	// language ids for server_lang and client_lang
	//enum NetworkLanguage {
	public static final int NETLANG_ANY = 0;
	public static final int NETLANG_ENGLISH = 1;
	public static final int NETLANG_GERMAN = 2;
	public static final int NETLANG_FRENCH = 3;
	//} ;

	//NetworkGameList *NetworkQueryServer(const char* host, unsigned short port, boolean game_info);



	// Those variables must always be registered!
	public static final int  MAX_SAVED_SERVERS = 10;
	public static final int  MAX_BANS = 25;
	//boolean _networking;
	//boolean _network_available;  // is network mode available?
	//boolean _network_server; // network-server is active
	//boolean _network_dedicated; // are we a dedicated server?
	//byte _network_playas; // an id to play as..
	// The client-info-server-index is always 1
	public static final int   NETWORK_SERVER_INDEX = 1;
	public static final int   NETWORK_EMPTY_INDEX = 0; 
	


}
