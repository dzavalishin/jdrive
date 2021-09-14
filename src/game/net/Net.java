package game.net;

import game.Global;

public class Net implements NetDefs 
{

	public static NetworkGameList _network_game_list;

	public static NetworkGameInfo _network_game_info;
	public static NetworkPlayerInfo [] _network_player_info = new NetworkPlayerInfo[Global.MAX_PLAYERS];
	public static NetworkClientInfo [] _network_client_info = new NetworkClientInfo[MAX_CLIENT_INFO];

	public static String _network_player_name;
	public static String _network_default_ip;

	public static int _network_own_client_index;
	public static String _network_unique_id; // Our own unique ID

	public static int _frame_counter_server; // The frame_counter of the server, if in network-mode
	public static int _frame_counter_max; // To where we may go with our clients

	public static int _last_sync_frame; // Used in the server to store the last time a sync packet was sent to clients.

	// networking settings
	public static int [] _network_ip_list = new int[MAX_INTERFACES + 1]; // Network IPs
	public static int _network_game_count;

	public static int _network_lobby_company_count;

	public static int _network_server_port;
	/* We use bind_ip and bind_ip_host, where bind_ip_host is the readable form of
	    bind_ip_host, and bind_ip the numeric value, because we want a nice number
	    in the openttd.cfg, but we wants to use the int internally.. */
	public static int _network_server_bind_ip;
	public static String _network_server_bind_ip_host;
	public static boolean _is_network_server; // Does this client wants to be a network-server?
	public static String _network_server_name;
	public static String _network_server_password;
	public static String _network_rcon_password;

	public static int _network_max_join_time;             //! Time a client can max take to join
	public static boolean _network_pause_on_join;               //! Pause the game when a client tries to join (more chance of succeeding join)

	public static int _redirect_console_to_client;

	public static int _network_sync_freq;
	public static int _network_frame_freq;

	public static int _sync_seed_1, _sync_seed_2;
	public static int _sync_frame;
	public static boolean _network_first_time;
	// Vars needed for the join-GUI
	public static NetworkJoinStatus _network_join_status;
	public static int _network_join_waiting;
	public static int _network_join_kbytes;
	public static int _network_join_kbytes_total;

	public static String  _network_last_host;
	public static short _network_last_port;
	public static int _network_last_host_ip;
	public static int _network_reconnect;

	public static boolean _network_udp_server;
	public static int _network_udp_broadcast;

	public static int _network_lan_internet;

	public static boolean _network_advertise;
	public static int _network_last_advertise_date;
	public static int _network_advertise_retries;

	public static boolean _network_autoclean_companies;
	public static int _network_autoclean_unprotected; // Remove a company after X months
	public static int _network_autoclean_protected;   // Unprotect a company after X months

	public static int _network_restart_game_date;    // If this year is reached, the server automaticly restarts

	public static String [] _network_host_list = new String[MAX_SAVED_SERVERS];
	public static String [] _network_ban_list = new String[MAX_BANS];
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
