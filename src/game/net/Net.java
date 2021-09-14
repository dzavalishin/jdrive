package game.net;

import java.util.function.Consumer;

import game.Global;
import game.Main;
import game.Str;
import game.TextEffect;
import game.console.Console;
import game.enums.SwitchModes;
import game.ids.StringID;
import game.util.Strings;
import game.xui.Window;

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
	public static int _network_last_port;
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

	private static int _network_clients_connected = 0;
	
	static final NetworkClientState [] _clients = new NetworkClientState[MAX_CLIENTS];
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	// Function that looks up the CI for a given client-index
	NetworkClientInfo NetworkFindClientInfoFromIndex(int client_index)
	{
		for (NetworkClientInfo ci : _network_client_info)
			if (ci.client_index == client_index)
				return ci;

		return null;
	}

	// Function that looks up the CS for a given client-index
	NetworkClientState NetworkFindClientStateFromIndex(int client_index)
	{
		for (NetworkClientState cs : _clients)
			if (cs.index == client_index)
				return cs;

		return null;
	}

	// NetworkGetClientName is a server-safe function to get the name of the client
	//  if the user did not send it yet, Client #<no> is used.
	static String NetworkGetClientName(final NetworkClientState cs)
	{
		NetworkClientInfo ci = DEREF_CLIENT_INFO(cs);
		if (ci.client_name.length() == 0)
			return String.format("Client #%d", cs.index);
		else
			return String.format("%s", ci.client_name);
	}

	// This puts a text-message to the console, or in the future, the chat-box,
	//  (to keep it all a bit more general)
	// If 'self_send' is true, this is the client who is sending the message
	static void NetworkTextMessage(NetworkAction action, int color, boolean self_send, final String name, final String str, Object ... args)
	{
		//char buf[1024];
		final int duration = 10; // Game days the messages stay visible
		String message;
		String temp;

		String buf  = String.format(str, args);

		switch (action) {
			case JOIN:
				temp = Strings.GetString(Str.STR_NETWORK_CLIENT_JOINED);
				message = String.format("*** %s %s", name, temp);
				break;
			case LEAVE:
				temp = Strings.GetString(Str.STR_NETWORK_ERR_LEFT);
				message = String.format("*** %s %s (%s)", name, temp, buf);
				break;
			case GIVE_MONEY:
				if (self_send) {
					Strings.SetDParamStr(0, name);
					SetDParam(1, atoi(buf));
					temp = Strings.GetString(Str.STR_NETWORK_GAVE_MONEY_AWAY);
					message = String.format("*** %s", temp);
				} else {
					SetDParam(0, atoi(buf));
					temp = Strings.GetString(Str.STR_NETWORK_GIVE_MONEY);
					message = String.format("*** %s %s", name, temp);
				}
				break;
			case CHAT_PLAYER:
				if (self_send) {
					Strings.SetDParamStr(0, name);
					temp = Strings.GetString(Str.STR_NETWORK_CHAT_TO_COMPANY);
					message = String.format("%s %s", temp, buf);
				} else {
					Strings.SetDParamStr(0, name);
					temp = Strings.GetString(Str.STR_NETWORK_CHAT_COMPANY);
					message = String.format("%s %s", temp, buf);
				}
				break;
			case CHAT_CLIENT:
				if (self_send) {
					Strings.SetDParamStr(0, name);
					temp = Strings.GetString(Str.STR_NETWORK_CHAT_TO_CLIENT);
					message = String.format("%s %s", temp, buf);
				} else {
					Strings.SetDParamStr(0, name);
					temp = Strings.GetString(Str.STR_NETWORK_CHAT_CLIENT);
					message = String.format("%s %s", temp, buf);
				}
				break;
			case NAME_CHANGE:
				temp = Strings.GetString(Str.STR_NETWORK_NAME_CHANGE);
				message = String.format("*** %s %s %s", name, temp, buf);
				break;
			default:
				Strings.SetDParamStr(0, name);
				temp = Strings.GetString(Str.STR_NETWORK_CHAT_ALL);
				message = String.format("%s %s", temp, buf);
				break;
		}

		Console.IConsolePrintF(color, "%s", message);
		TextEffect.AddTextMessage(color, duration, "%s", message);
	}

	// Calculate the frame-lag of a client
	int NetworkCalculateLag(final NetworkClientState cs)
	{
		int lag = cs.last_frame_server - cs.last_frame;
		// This client has missed his ACK packet after 1 DAY_TICKS..
		//  so we increase his lag for every frame that passes!
		// The packet can be out by a max of _net_frame_freq
		if (cs.last_frame_server + Global.DAY_TICKS + _network_frame_freq < Global._frame_counter)
			lag += Global._frame_counter - (cs.last_frame_server + Global.DAY_TICKS + _network_frame_freq);

		return lag;
	}


	// There was a non-recoverable error, drop back to the main menu with a nice
	//  error
	static void NetworkError(StringID error_string)
	{
		Global._switch_mode = SwitchModes.SM_MENU;
		Global._switch_mode_errorstr = error_string;
	}
	static void NetworkError(int error_string)
	{
		NetworkError(new StringID(error_string));
	}
	static void ClientStartError(final String error)
	{
		Global.DEBUG_net( 0, "[NET] Client could not start network: %s",error);
		NetworkError(Str.STR_NETWORK_ERR_CLIENT_START);
	}

	static void ServerStartError(final String error)
	{
		Global.DEBUG_net( 0, "[NET] Server could not start network: %s",error);
		NetworkError(Str.STR_NETWORK_ERR_SERVER_START);
	}

	static void NetworkClientError(NetworkRecvStatus res, NetworkClientState cs) {
		// First, send a CLIENT_ERROR to the server, so he knows we are
		//  disconnection (and why!)
		NetworkErrorCode errorno;

		// We just want to close the connection..
		if (res == NetworkRecvStatus.CLOSE_QUERY) {
			cs.quited = true;
			NetworkCloseClient(cs);
			Global._networking = false;

			Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);
			return;
		}

		switch(res) {
			case DESYNC: errorno = NetworkErrorCode.DESYNC; break;
			case SAVEGAME: errorno = NetworkErrorCode.SAVEGAME_FAILED; break;
			default: errorno = NetworkErrorCode.GENERAL;
		}
		// This means we fucked up and the server closed the connection
		if (res != NetworkRecvStatus.SERVER_ERROR && res != NetworkRecvStatus.SERVER_FULL &&
				res != NetworkRecvStatus.SERVER_BANNED) {
			SEND_COMMAND(PACKET_CLIENT_ERROR, errorno);

			// Dequeue all commands before closing the socket
			NetworkSend_Packets(DEREF_CLIENT(0));
		}

		Global._switch_mode = SwitchModes.SM_MENU;
		NetworkCloseClient(cs);
		Global._networking = false;
	}

	// Find all IP-aliases for this host
	static void NetworkFindIPs()
	{
		int i, last;

		ifaddrs ifap, ifa;

		// If something fails, make sure the list is empty
		_network_ip_list[0] = 0;

		if (getifaddrs(&ifap) != 0)
			return;

		i = 0;
		for (ifa = ifap; ifa != null; ifa = ifa.ifa_next) {
			if (ifa.ifa_addr == null || ifa.ifa_addr.sa_family != AF_INET)
				continue;
			_network_ip_list[i] = ((struct sockaddr_in *)ifa.ifa_addr).sin_addr.s_addr;
			i++;
		}
		freeifaddrs(ifap);


		_network_ip_list[i] = 0;
		last = i - 1;

		Global.DEBUG_net( 3, "Detected IPs:");
		// Now display to the debug all the detected ips
		i = 0;
		while (_network_ip_list[i] != 0) {
			// Also check for non-used ips (127.0.0.1)
			if (_network_ip_list[i] == inet_addr("127.0.0.1")) {
				// If there is an ip after thisone, put him in here
				if (last > i)
					_network_ip_list[i] = _network_ip_list[last];
				// Clear the last ip
				_network_ip_list[last] = 0;
				// And we have 1 ip less
				last--;
				continue;
			}

			Global.DEBUG_net( 3, " %d) %s", i, inet_ntoa(*(struct in_addr *)&_network_ip_list[i]));//inet_ntoa(inaddr));
			i++;
		}
	}

	// Resolve a hostname to a inet_addr
	long NetworkResolveHost(final String hostname)
	{
		in_addr_t ip;

		// First try: is it an ip address?
		ip = inet_addr(hostname);

		// If not try to resolve the name
		if (ip == INADDR_NONE) {
			struct hostent he = gethostbyname(hostname);
			if (he == null) {
				Global.DEBUG_net( 0, "[NET] Cannot resolve %s", hostname);
			} else {
				struct in_addr addr = *(struct in_addr *)he.h_addr_list[0];
				Global.DEBUG_net( 1, "[NET] Resolved %s to %s", hostname, inet_ntoa(addr));
				ip = addr.s_addr;
			}
		}
		return ip;
	}

	/**
	// Converts a string to ip/port/player
	* @param s  Format: IP#player:port
	*/
	boolean ParseConnectionString(final String []player, final String []port, String [] host, String s)
	{
		int epl = s.indexOf(':');
		//if(epl < 0) return false;
		if(epl == 0) return false;
		if( epl > 0)
		{
			s = s.substring(0, epl);
			port[0] = s.substring(epl+1);
		}

		int eip = s.indexOf('#');
		//if(eip < 0) return false;
		if(eip == 0) return false;
		
		if(eip > 0)
		{
			host[0] = s.substring(0,eip);
			player[0] = s.substring(eip+1);		
		}
		else 
			host[0] = s;
		
		return true;
		/*String p;
		for (p = connection_string; *p != '\0'; p++) {
			if (*p == '#') {
				*player = p + 1;
				*p = '\0';
			} else if (*p == ':') {
				*port = p + 1;
				*p = '\0';
			}
		}*/
	}

	// Creates a new client from a socket
	//   Used both by the server and the client
	static NetworkClientState NetworkAllocClient(SOCKET s)
	{
		NetworkClientState cs;
		NetworkClientInfo ci;
		int client_no = 0;

		if (Global._network_server) {
			// Can we handle a new client?
			if (_network_clients_connected  >= MAX_CLIENTS)
				return null;

			if (_network_game_info.clients_on >= _network_game_info.clients_max)
				return null;

			// Register the login
			client_no = _network_clients_connected++;
		}

		cs = &_clients[client_no];
		memset(cs, 0, sizeof(*cs));
		cs.socket = s;
		cs.last_frame = 0;
		cs.quited = false;

		cs.last_frame = Global._frame_counter;
		cs.last_frame_server = Global._frame_counter;

		if (Global._network_server) {
			ci = DEREF_CLIENT_INFO(cs);
			memset(ci, 0, sizeof(*ci));

			cs.index = _network_client_index++;
			ci.client_index = cs.index;
			ci.join_date = _date;

			Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);
		}

		return cs;
	}

	// Close a connection
	static void NetworkCloseClient(NetworkClientState cs)
	{
		NetworkClientInfo ci;
		// Socket is already dead
		if (cs.socket == INVALID_SOCKET) {
			cs.quited = true;
			return;
		}

		Global.DEBUG_net( 1, "[NET] Closed client connection");

		if (!cs.quited && Global._network_server && cs.status > STATUS_INACTIVE) {
			// We did not receive a leave message from this client...
			NetworkErrorCode errorno = NetworkErrorCode.CONNECTION_LOST;
			String str;
			String client_name;
			NetworkClientState new_cs;

			client_name = NetworkGetClientName(cs);

			// TODO check ordinals?
			str = Strings.GetString(Str.STR_NETWORK_ERR_CLIENT_GENERAL + errorno.ordinal());

			NetworkTextMessage(NetworkAction.LEAVE, 1, false, client_name, "%s", str);

			// Inform other clients of this... strange leaving ;)
			FOR_ALL_CLIENTS(new_cs) {
				if (new_cs.status > STATUS_AUTH && cs != new_cs) {
					SEND_COMMAND(PACKET_SERVER_ERROR_QUIT, new_cs, cs.index, errorno);
				}
			}
		}

		/* When the client was PRE_ACTIVE, the server was in pause mode, so unpause */
		if (cs.status == STATUS_PRE_ACTIVE && _network_pause_on_join) {
			DoCommandP(0, 0, 0, null, CMD_PAUSE);
			NetworkServer_HandleChat(NetworkAction.CHAT, DESTTYPE_BROADCAST, 0, "Game unpaused", NETWORK_SERVER_INDEX);
		}

		closesocket(cs.socket);
		cs.writable = false;
		cs.quited = true;

		// Free all pending and partially received packets
		while (cs.packet_queue != null) {
			Packet p = cs.packet_queue.next;
			//free(cs.packet_queue);
			cs.packet_queue = p;
		}
		//free(cs.packet_recv);
		cs.packet_recv = null;

		while (cs.command_queue != null) {
			CommandPacket p = cs.command_queue.next;
			//free(cs.command_queue);
			cs.command_queue = p;
		}

		// Close the gap in the client-list
		ci = DEREF_CLIENT_INFO(cs);

		if (Global._network_server) {
			// We just lost one client :(
			if (cs.status > STATUS_INACTIVE)
				_network_game_info.clients_on--;
			_network_clients_connected--;

			while ((cs + 1) != DEREF_CLIENT(MAX_CLIENTS) && (cs + 1).socket != INVALID_SOCKET) {
				*cs = *(cs + 1);
				*ci = *(ci + 1);
				cs++;
				ci++;
			}

			InvalidateWindow(WC_CLIENT_LIST, 0);
		}

		// Reset the status of the last socket
		cs.socket = INVALID_SOCKET;
		cs.status = STATUS_INACTIVE;
		cs.index = NETWORK_EMPTY_INDEX;
		ci.client_index = NETWORK_EMPTY_INDEX;
	}

	// A client wants to connect to a server
	static boolean NetworkConnect(final String hostname, int port)
	{
		SOCKET s;
		sockaddr_in sin;

		Global.DEBUG_net( 1, "[NET] Connecting to %s %d", hostname, port);

		s = socket(AF_INET, SOCK_STREAM, 0);
		if (s == INVALID_SOCKET) {
			ClientStartError("socket() failed");
			return false;
		}

		if (!SetNoDelay(s))
			Global.DEBUG_net( 1, "[NET] Setting TCP_NODELAY failed");

		sin.sin_family = AF_INET;
		sin.sin_addr.s_addr = NetworkResolveHost(hostname);
		sin.sin_port = htons(port);
		_network_last_host_ip = sin.sin_addr.s_addr;

		if (connect(s, (struct sockaddr*) &sin, sizeof(sin)) != 0) {
			// We failed to connect for which reason what so ever
			return false;
		}

		if (!SetNonBlocking(s))
			Global.DEBUG_net( 0, "[NET] Setting non-blocking failed"); // XXX should this be an error?

		// in client mode, only the first client field is used. it's pointing to the server.
		NetworkAllocClient(s);

		ShowJoinStatusWindow();

		//memcpy(&network_tmp_patches, &Global._patches, sizeof(_patches));
		memcpy(network_tmp_patches, Global._patches );

		return true;
	}

	// For the server, to accept new clients
	static void NetworkAcceptClients()
	{
		sockaddr_in sin;
		SOCKET s;
		NetworkClientState cs;
		int i;
		boolean banned;

		// Should never ever happen.. is it possible??
		assert(_listensocket != INVALID_SOCKET);

		for (;;) {
			socklen_t sin_len;

			sin_len = sizeof(sin);
			s = accept(_listensocket, (struct sockaddr*)&sin, &sin_len);
			if (s == INVALID_SOCKET) return;

			SetNonBlocking(s); // XXX error handling?

			Global.DEBUG_net( 1, "[NET] Client connected from %s on frame %d", inet_ntoa(sin.sin_addr), Global._frame_counter);

			SetNoDelay(s); // XXX error handling?

			/* Check if the client is banned */
			banned = false;
			for (i = 0; i < lengthof(_network_ban_list); i++) {
				if (_network_ban_list[i] == null)
					continue;

				if (sin.sin_addr.s_addr == inet_addr(_network_ban_list[i])) {
					Packet *p = NetworkSend_Init(PACKET_SERVER_BANNED);

					Global.DEBUG_net( 1, "[NET] Banned ip tried to join (%s), refused", _network_ban_list[i]);

					p.buffer[0] = p.size & 0xFF;
					p.buffer[1] = p.size >> 8;

					send(s, p.buffer, p.size, 0);
					closesocket(s);

					free(p);

					banned = true;
					break;
				}
			}
			/* If this client is banned, continue with next client */
			if (banned)
				continue;

			cs = NetworkAllocClient(s);
			if (cs == null) {
				// no more clients allowed?
				// Send to the client that we are full!
				Packet *p = NetworkSend_Init(PACKET_SERVER_FULL);

				p.buffer[0] = p.size & 0xFF;
				p.buffer[1] = p.size >> 8;

				send(s, p.buffer, p.size, 0);
				closesocket(s);

				free(p);

				continue;
			}

			// a new client has connected. We set him at inactive for now
			//  maybe he is only requesting server-info. Till he has sent a PACKET_CLIENT_MAP_OK
			//  the client stays inactive
			cs.status = STATUS_INACTIVE;

			{
				// Save the IP of the client
				NetworkClientInfo ci;
				ci = DEREF_CLIENT_INFO(cs);
				ci.client_ip = sin.sin_addr.s_addr;
			}
		}
	}

	// Set up the listen socket for the server
	static boolean NetworkListen()
	{
		SOCKET ls;
		sockaddr_in sin;
		int port;

		port = _network_server_port;

		Global.DEBUG_net( 1, "[NET] Listening on %s:%d", _network_server_bind_ip_host, port);

		ls = socket(AF_INET, SOCK_STREAM, 0);
		if (ls == INVALID_SOCKET) {
			ServerStartError("socket() on listen socket failed");
			return false;
		}

		{ // reuse the socket
			int reuse = 1;
			// The (final char*) cast is needed for windows!!
			if (setsockopt(ls, SOL_SOCKET, SO_REUSEADDR, (final char*)&reuse, sizeof(reuse)) == -1) {
				ServerStartError("setsockopt() on listen socket failed");
				return false;
			}
		}

		if (!SetNonBlocking(ls))
			Global.DEBUG_net( 0, "[NET] Setting non-blocking failed"); // XXX should this be an error?

		sin.sin_family = AF_INET;
		sin.sin_addr.s_addr = _network_server_bind_ip;
		sin.sin_port = htons(port);

		if (bind(ls, (struct sockaddr*)&sin, sizeof(sin)) != 0) {
			ServerStartError("bind() failed");
			return false;
		}

		if (listen(ls, 1) != 0) {
			ServerStartError("listen() failed");
			return false;
		}

		_listensocket = ls;

		return true;
	}

	// Close all current connections
	static void NetworkClose()
	{
		NetworkClientState cs;

		FOR_ALL_CLIENTS(cs) {
			if (!Global._network_server) {
				SEND_COMMAND(PACKET_CLIENT_QUIT, "leaving");
				NetworkSend_Packets(cs);
			}
			NetworkCloseClient(cs);
		}

		if (Global._network_server) {
			// We are a server, also close the listensocket
			closesocket(_listensocket);
			_listensocket = INVALID_SOCKET;
			Global.DEBUG_net( 1, "[NET] Closed listener");
			NetworkUDPClose();
		}
	}

	// Inits the network (cleans sockets and stuff)
	static void NetworkInitialize()
	{
		NetworkClientState cs;

		_local_command_queue = null;

		// Clean all client-sockets
		memset(_clients, 0, sizeof(_clients));
		for (cs : _clients) {
			cs.socket = INVALID_SOCKET;
			cs.status = STATUS_INACTIVE;
			cs.command_queue = null;
		}

		// Clean the client_info memory
		memset(_network_client_info, 0, sizeof(_network_client_info));
		memset(_network_player_info, 0, sizeof(_network_player_info));
		_network_lobby_company_count = 0;

		_sync_frame = 0;
		_network_first_time = true;

		_network_reconnect = 0;

		NetworkUDPInitialize();
	}

	// Query a server to fetch his game-info
	//  If game_info is true, only the gameinfo is fetched,
	//   else only the client_info is fetched
	NetworkGameList NetworkQueryServer(final String host, int port, boolean game_info)
	{
		if (!Global._network_available) return null;

		NetworkDisconnect();

		if (game_info) {
			return NetworkUDPQueryServer(host, port);
		}

		NetworkInitialize();

		Global._network_server = false;

		// Try to connect
		Global._networking = NetworkConnect(host, port);

//		ttd_strlcpy(_network_last_host, host, sizeof(_network_last_host));
//		_network_last_port = port;

		// We are connected
		if (Global._networking) {
			SEND_COMMAND(PACKET_CLIENT_COMPANY_INFO );
			return null;
		}

		// No networking, close everything down again
		NetworkDisconnect();
		return null;
	}

	/* Validates an address entered as a string and adds the server to
	 * the list. If you use this functions, the games will be marked
	 * as manually added. */
	void NetworkAddServer(final String b)
	{
		if (b.length() != 0) {
			NetworkGameList item;
			String [] port = {null};
			String [] player = {null};
			String [] host = {null};

			//host = b;
			_network_default_ip = b;

			if(!ParseConnectionString(player, port, host, b)) return; // TODO message?

			if (player[0] != null) Global._network_playas = Integer.parseInt(player[0]);

			int rport = NETWORK_DEFAULT_PORT;
			if (port[0] != null) rport = Integer.parseInt(port[0]);

			item = NetworkQueryServer(host[0], rport, true);
			item.manually = true;
		}
	}

	/* Generates the list of manually added hosts from NetworkGameList and
	 * dumps them into the array _network_host_list. This array is needed
	 * by the function that generates the config file. */
	void NetworkRebuildHostList()
	{
		int i = 0;
		NetworkGameList item = _network_game_list;
		while (item != null && i != _network_host_list.length) {
			if (item.manually)
				_network_host_list[i++] = String.format("%s:%d", item.info.hostname, item.port);
			item = item.next;
		}

		for (; i < _network_host_list.length; i++) {
			_network_host_list[i] = "";
		}
	}

	// Used by clients, to connect to a server
	boolean NetworkClientConnectGame(final String host, int port)
	{
		if (!Global._network_available) return false;

		if (port == 0) return false;

		_network_last_host = host;
		_network_last_port = port;

		NetworkDisconnect();
		NetworkUDPClose();
		NetworkInitialize();

		// Try to connect
		Global._networking = NetworkConnect(host, port);

		// We are connected
		if (Global._networking) {
			IConsoleCmdExec("exec scripts/on_client.scr 0");
			NetworkClient_Connected();
		} else {
			// Connecting failed
			NetworkError(Str.STR_NETWORK_ERR_NOCONNECTION);
		}

		return Global._networking;
	}

	static void NetworkInitGameInfo()
	{
		NetworkClientInfo ci;

		_network_game_info.server_name = _network_server_name;
		_network_game_info.server_password = _network_server_password;
		_network_game_info.rcon_password = _network_rcon_password;
		if (_network_game_info.server_name.length() == 0)
			_network_game_info.server_name = "Unnamed Server";

		// The server is a client too ;)
		if (_network_dedicated) {
			_network_game_info.clients_on = 0;
			_network_game_info.dedicated = true;
		} else {
			_network_game_info.clients_on = 1;
			_network_game_info.dedicated = false;
		}
		ttd_strlcpy(_network_game_info.server_revision, _openttd_revision, sizeof(_network_game_info.server_revision));
		_network_game_info.spectators_on = 0;
		_network_game_info.game_date = _date;
		_network_game_info.start_date = ConvertIntDate(_patches.starting_date);
		_network_game_info.map_width = MapSizeX();
		_network_game_info.map_height = MapSizeY();
		_network_game_info.map_set = _opt.landscape;

		_network_game_info.use_password = (_network_server_password[0] != '\0');

		// We use _network_client_info[MAX_CLIENT_INFO - 1] to store the server-data in it
		//  The index is NETWORK_SERVER_INDEX ( = 1)
		ci = &_network_client_info[MAX_CLIENT_INFO - 1];
		memset(ci, 0, sizeof(*ci));

		ci.client_index = NETWORK_SERVER_INDEX;
		if (_network_dedicated)
			ci.client_playas = OWNER_SPECTATOR;
		else
			ci.client_playas = _local_player + 1;
		ci.client_name = _network_player_name;
		ci.unique_id = _network_unique_id;
	}

	boolean NetworkServerStart()
	{
		if (!Global._network_available) return false;

		/* Call the pre-scripts */
		IConsoleCmdExec("exec scripts/pre_server.scr 0");
		if (_network_dedicated) IConsoleCmdExec("exec scripts/pre_dedicated.scr 0");

		NetworkInitialize();
		if (!NetworkListen())
			return false;

		// Try to start UDP-server
		_network_udp_server = true;
		_network_udp_server = NetworkUDPListen(&_udp_server_socket, _network_server_bind_ip, _network_server_port, false);

		Global._network_server = true;
		Global._networking = true;
		Global._frame_counter = 0;
		_frame_counter_server = 0;
		_frame_counter_max = 0;
		_last_sync_frame = 0;
		_network_own_client_index = NETWORK_SERVER_INDEX;

		if (!_network_dedicated)
			_network_playas = 1;

		_network_clients_connected = 0;

		NetworkInitGameInfo();

		// execute server initialization script
		IConsoleCmdExec("exec scripts/on_server.scr 0");
		// if the server is dedicated ... add some other script
		if (_network_dedicated) IConsoleCmdExec("exec scripts/on_dedicated.scr 0");

		/* Try to register us to the master server */
		_network_last_advertise_date = 0;
		NetworkUDPAdvertise();
		return true;
	}

	// The server is rebooting...
	// The only difference with NetworkDisconnect, is the packets that is sent
	void NetworkReboot()
	{
		if (Global._network_server) {
			NetworkClientState cs;
			FOR_ALL_CLIENTS(cs) {
				SEND_COMMAND(PACKET_SERVER_NEWGAME, cs);
				NetworkSend_Packets(cs);
			}
		}

		NetworkClose();

		// Free all queued commands
		/*while (_local_command_queue != null) {
			CommandPacket p = _local_command_queue;
			_local_command_queue = _local_command_queue.next;
			//free(p);
		}*/
		_local_command_queue = null;

		Global._networking = false;
		Global._network_server = false;
	}

	// We want to disconnect from the host/clients
	void NetworkDisconnect()
	{
		if (Global._network_server) {
			//NetworkClientState cs;
			FOR_ALL_CLIENTS(cs -> {
				SEND_COMMAND(PACKET_SERVER_SHUTDOWN, cs);
				NetworkSend_Packets(cs);
			});
		}

		if (_network_advertise)
			NetworkUDPRemoveAdvertise();

		DeleteWindowById(WC_NETWORK_STATUS_WINDOW, 0);

		NetworkClose();

		// Free all queued commands
		/*while (_local_command_queue != null) {
			CommandPacket p = _local_command_queue;
			_local_command_queue = _local_command_queue.next;
			free(p);
		}*/
		_local_command_queue = null;

		if (Global._networking && !Global._network_server) {
			//memcpy(&_patches, &network_tmp_patches, sizeof(_patches));
			memcpy(_patches, network_tmp_patches);
		}

		Global._networking = false;
		Global._network_server = false;
	}

	static void FOR_ALL_CLIENTS(Consumer<NetworkClientState> s) 
	{
		for (NetworkClientState cs : _clients)
		{
			if( cs.hasValidSocket() )
				s.accept(cs);
		}
	}
	
	// Receives something from the network
	static boolean NetworkReceive()
	{
		//NetworkClientState cs;
		int n;
		fd_set read_fd, write_fd;
		 timeval tv;

		FD_ZERO(&read_fd);
		FD_ZERO(&write_fd);

		FOR_ALL_CLIENTS(cs) {
			FD_SET(cs.socket, &read_fd);
			FD_SET(cs.socket, &write_fd);
		}

		// take care of listener port
		if (Global._network_server) {
			FD_SET(_listensocket, &read_fd);
		}

		tv.tv_sec = tv.tv_usec = 0; // don't block at all.

		n = select(FD_SETSIZE, &read_fd, &write_fd, null, &tv);
		if (n == -1 && !Global._network_server) NetworkError(Str.STR_NETWORK_ERR_LOSTCONNECTION);

		// accept clients..
		if (Global._network_server && FD_ISSET(_listensocket, &read_fd))
			NetworkAcceptClients();

		// read stuff from clients
		for(cs : _clients)
			{
			if(!cs.hasValidSocket()) continue;
			
			cs.writable = !!FD_ISSET(cs.socket, &write_fd);
			if (FD_ISSET(cs.socket, &read_fd)) {
				if (Global._network_server)
					NetworkServer_ReadPackets(cs);
				else {
					byte res;
					// The client already was quiting!
					if (cs.quited) return false;
					if ((res = NetworkClient_ReadPackets(cs)) != NetworkRecvStatus.OKAY) {
						// The client made an error of which we can not recover
						//   close the client and drop back to main menu

						NetworkClientError(res, cs);
						return false;
					}
				}
			}
		});
		return true;
	}

	// This sends all buffered commands (if possible)
	static void NetworkSend()
	{
		//NetworkClientState cs;
		FOR_ALL_CLIENTS(cs -> {
			if (cs.writable) {
				NetworkSend_Packets(cs);

				if (cs.status == STATUS_MAP) {
					// This client is in the middle of a map-send, call the function for that
					SEND_COMMAND(PACKET_SERVER_MAP, cs);
				}
			}
		});
	}

	// Handle the local-command-queue
	static void NetworkHandleLocalQueue()
	{
		CommandPacket cp;
		CommandPacket cp_prev;

		cp_prev = _local_command_queue;

		while ( (cp = cp_prev) != null) {

			// The queue is always in order, which means
			// that the first element will be executed first.
			if (Global._frame_counter < cp.frame)
				break;

			if (Global._frame_counter > cp.frame) {
				// If we reach here, it means for whatever reason, we've already executed
				// past the command we need to execute.
				Global.DEBUG_net( 0, "[NET] Trying to execute a packet in the past!");
				assert(0);
			}

			// We can execute this command
			NetworkExecuteCommand(cp);

			cp_prev = cp.next;
			//free(cp);
		}

		// Just a safety check, to be removed in the future.
		// Make sure that no older command appears towards the end of the queue
		// In that case we missed executing it. This will never happen.
		for(cp = _local_command_queue; cp; cp = cp.next) {
			assert(Global._frame_counter < cp.frame);
		}

	}

	static boolean NetworkDoClientLoop()
	{
		Global._frame_counter++;

		NetworkHandleLocalQueue();

		Main.StateGameLoop();

		// Check if we are in sync!
		if (_sync_frame != 0) {
			if (_sync_frame == Global._frame_counter) {
	//#ifdef NETWORK_SEND_DOUBLE_SEED
	//			if (_sync_seed_1 != _random_seeds[0][0] || _sync_seed_2 != _random_seeds[0][1]) {
	//#else
				if (_sync_seed_1 != Global._random_seeds[0][0]) {
	//#endif
					NetworkError(Str.STR_NETWORK_ERR_DESYNC);
					Global.DEBUG_net( 0, "[NET] Sync error detected!");
					NetworkClientError(NetworkRecvStatus.DESYNC, DEREF_CLIENT(0));
					return false;
				}

				// If this is the first time we have a sync-frame, we
				//   need to let the server know that we are ready and at the same
				//   frame as he is.. so we can start playing!
				if (_network_first_time) {
					_network_first_time = false;
					SEND_COMMAND(PACKET_CLIENT_ACK);
				}

				_sync_frame = 0;
			} else if (_sync_frame < Global._frame_counter) {
				Global.DEBUG_net( 1, "[NET] Missed frame for sync-test (%d / %d)", _sync_frame, Global._frame_counter);
				_sync_frame = 0;
			}
		}

		return true;
	}

	// We have to do some UDP checking
	void NetworkUDPGameLoop()
	{
		if (_network_udp_server) {
			NetworkUDPReceive(_udp_server_socket);
			if (_udp_master_socket != INVALID_SOCKET) {
				NetworkUDPReceive(_udp_master_socket);
			}
		}
		else if (_udp_client_socket != INVALID_SOCKET) {
			NetworkUDPReceive(_udp_client_socket);
			if (_network_udp_broadcast > 0)
				_network_udp_broadcast--;
		}
	}

	// The main loop called from ttd.c
	//  Here we also have to do StateGameLoop if needed!
	void NetworkGameLoop()
	{
		if (!Global._networking) return;

		if (!NetworkReceive()) return;

		if (Global._network_server) {
			boolean send_frame = false;

			// We first increase the _frame_counter
			Global._frame_counter++;
			// Update max-frame-counter
			if (Global._frame_counter > _frame_counter_max) {
				_frame_counter_max = Global._frame_counter + _network_frame_freq;
				send_frame = true;
			}

			NetworkHandleLocalQueue();

			// Then we make the frame
			Main.StateGameLoop();

			_sync_seed_1 = Global._random_seeds[0][0];
	//#ifdef NETWORK_SEND_DOUBLE_SEED
	//		_sync_seed_2 = _random_seeds[0][1];
	//#endif

			NetworkServer_Tick(send_frame);
		} else {
			// Client

			// Make sure we are at the frame were the server is (quick-frames)
			if (_frame_counter_server > Global._frame_counter) {
				while (_frame_counter_server > Global._frame_counter) {
					if (!NetworkDoClientLoop()) break;
				}
			} else {
				// Else, keep on going till _frame_counter_max
				if (_frame_counter_max > Global._frame_counter) {
					NetworkDoClientLoop();
				}
			}
		}

		NetworkSend();
	}

	static void NetworkGenerateUniqueId()
	{
		md5_state_t state;
		md5_byte_t digest[16];
		char hex_output[16*2 + 1];
		char coding_string[NETWORK_NAME_LENGTH];
		int di;

		snprintf(coding_string, sizeof(coding_string), "%d%s", (int)Random(), "OpenTTD Unique ID");

		/* Generate the MD5 hash */
		md5_init(&state);
		md5_append(&state, (final md5_byte_t*)coding_string, strlen(coding_string));
		md5_finish(&state, digest);

		for (di = 0; di < 16; ++di)
			sprintf(hex_output + di * 2, "%02x", digest[di]);

		/* _network_unique_id is our id */
		snprintf(_network_unique_id, sizeof(_network_unique_id), "%s", hex_output);
	}

	// This tries to launch the network for a given OS
	void NetworkStartUp()
	{
		Global.DEBUG_net( 3, "[NET][Core] Starting network...");

	    // Network is available
		Global._network_available = true;
		Global._network_dedicated = false;
		_network_last_advertise_date = 0;
		_network_advertise_retries = 0;

		/* Load the ip from the openttd.cfg */
		_network_server_bind_ip = inet_addr(_network_server_bind_ip_host);
		/* And put the data back in it in case it was an invalid ip */
		//snprintf(_network_server_bind_ip_host, sizeof(_network_server_bind_ip_host), "%s", inet_ntoa(*(struct in_addr *)&_network_server_bind_ip));
		_network_server_bind_ip_host = ntoa(_network_server_bind_ip);

		/* Generate an unique id when there is none yet */
		if (_network_unique_id[0] == '\0')
			NetworkGenerateUniqueId();

		//memset(&_network_game_info, 0, sizeof(_network_game_info));
		_network_game_info = new NetworkGameInfo();

		/* XXX - Hard number here, because the strings can currently handle no more
		    than 10 clients -- TrueLight */
		_network_game_info.clients_max = 10;


		NetworkInitialize();
		Global.DEBUG_net( 3, "[NET][Core] Network online. Multiplayer available.");
		NetworkFindIPs();
	}

	// This shuts the network down
	void NetworkShutDown()
	{
		Global.DEBUG_net( 3, "[NET][Core] Shutting down the network.");
		Global._network_available = false;
	}
	
	
	
	
	
	
	
}
