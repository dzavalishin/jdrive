package com.dzavalishin.net;

import com.dzavalishin.console.ConsoleFactory;
import com.dzavalishin.console.DefaultConsole;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.SwitchModes;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Main;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.TextEffect;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Version;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.ifaces.CommandCallback;
import com.dzavalishin.util.GameDate;
import com.dzavalishin.util.Strings;
import com.dzavalishin.xui.Window;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

// #define FOR_ALL_CLIENTS(cs) for (cs = _clients; cs != &_clients[MAX_CLIENTS] && cs->socket != INVALID_SOCKET; cs++)
//#define DEREF_CLIENT_INFO(cs) (&_network_client_info[cs - _clients])

public class Net implements NetDefs, NetClient 
{
	public static RandomAccessFile client_file_pointer = null;
	public static int client_last_ack_frame = -1; // TODO [dz] -1?
	public static String recvMapFilename = "";


	public static RandomAccessFile _server_file_pointer = null;
	public static int server_sent_packets = 0; // How many packets we did send succecfully last time

	
	//public static NetworkGameList _network_game_list;

	public static NetworkGameInfo _network_game_info = new NetworkGameInfo();
	public static NetworkPlayerInfo [] _network_player_info = new NetworkPlayerInfo[Global.MAX_PLAYERS];
	//public static NetworkClientInfo [] _network_client_info = new NetworkClientInfo[MAX_CLIENT_INFO];

	public static String _network_player_name = "Player";
	public static String _network_default_ip;

	public static int _network_own_client_index;
	public static String _network_unique_id = null; // Our own unique ID

	public static int _frame_counter_server; // The frame_counter of the server, if in network-mode
	public static int _frame_counter_max; // To where we may go with our clients

	public static int _last_sync_frame; // Used in the server to store the last time a sync packet was sent to clients.

	// networking settings
	//public static int [] _network_ip_list = new int[MAX_INTERFACES + 1]; // Network IPs
	public static final List<InetAddress> _network_ip_list= new ArrayList<>();
	public static int _network_game_count;

	public static int _network_lobby_company_count;

	public static int _network_server_port;
	/* We use bind_ip and bind_ip_host, where bind_ip_host is the readable form of
	    bind_ip_host, and bind_ip the numeric value, because we want a nice number
	    in the openttd.cfg, but we wants to use the int internally.. */
	public static InetAddress _network_server_bind_ip;
	public static String _network_server_bind_ip_host;
	public static boolean _is_network_server; // Does this client wants to be a network-server?
	public static String _network_server_name = "";
	public static String _network_server_password = "";
	public static String _network_rcon_password;

	public static int _network_max_join_time;             //! Time a client can max take to join
	public static boolean _network_pause_on_join;               //! Pause the game when a client tries to join (more chance of succeeding join)

	//public static int _redirect_console_to_client;

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

	//static final NetworkClientState [] _clients = new NetworkClientState[MAX_CLIENTS];
	//static CommandPacket _local_command_queue;
	static final List<CommandPacket> _local_command_queue = new ArrayList<>();

	static int _network_client_index = NETWORK_SERVER_INDEX + 1;
	//static ServerSocket _listensocket;
	static ServerSocketChannel _listensocket;

	static final List<NetworkClientState> _clients = new ArrayList<>();

	static DatagramChannel [] _udp_client_socket = {null};
	static DatagramChannel [] _udp_server_socket = {null};
	static DatagramChannel [] _udp_master_socket = {null};

















	// Function that looks up the CI for a given client-index
	public static NetworkClientInfo NetworkFindClientInfoFromIndex(int client_index)
	{
		/*
		for (NetworkClientInfo ci : _network_client_info)
			if (ci.client_index == client_index)
				return ci;
		 */
		for( NetworkClientState cs : _clients )
		{
			if( cs.getIndex() == client_index )
				return cs.ci;
		}

		return null;
	}

	// Function that looks up the CS for a given client-index
	public static NetworkClientState NetworkFindClientStateFromIndex(int client_index)
	{
		for (NetworkClientState cs : _clients)
			if (cs.getIndex() == client_index)
				return cs;

		return null;
	}

	// NetworkGetClientName is a server-safe function to get the name of the client
	//  if the user did not send it yet, Client #<no> is used.
	static String NetworkGetClientName(final NetworkClientState cs)
	{
		NetworkClientInfo ci = cs.ci; // _network_client_info[cs - _clients];
		if (ci.client_name.length() == 0)
			return String.format("Client #%d", cs.getIndex());
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
				Global.SetDParam(1, Integer.parseInt(buf));
				temp = Strings.GetString(Str.STR_NETWORK_GAVE_MONEY_AWAY);
				message = String.format("*** %s", temp);
			} else {
				Global.SetDParam(0, Integer.parseInt(buf));
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

		//Console.IConsolePrintF(color, "%s", );
		ConsoleFactory.INSTANCE.getConsole().println(message, color);
		TextEffect.AddTextMessage(color, duration, "%s", message);
	}

	// Calculate the frame-lag of a client
	public static int NetworkCalculateLag(final NetworkClientState cs)
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
			//SEND_COMMAND(PacketType.CLIENT_ERROR, errorno);
			NetClient.NetworkPacketSend_PACKET_CLIENT_ERROR_command(errorno);

			// Dequeue all commands before closing the socket
			NetworkSend_Packets(_clients.get(0)); // [dz] why 0?
		}

		Global._switch_mode = SwitchModes.SM_MENU;
		NetworkCloseClient(cs);
		Global._networking = false;
	}

	// Find all IP-aliases for this host
	static void NetworkFindIPs() throws SocketException
	{
		_network_ip_list.clear();

		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		while(e.hasMoreElements())
		{
			NetworkInterface n = e.nextElement();
			Enumeration<InetAddress> ee = n.getInetAddresses();
			while (ee.hasMoreElements())
			{
				InetAddress i = ee.nextElement();
				//System.out.println(i.getHostAddress());

				if(i.isLoopbackAddress()) continue;

				_network_ip_list.add(i);
				Global.DEBUG_net( 3, "my addr %s", i.getHostAddress());
			}
		}		



		/*
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
		 */
	}

	// Resolve a hostname to a inet_addr
	static InetAddress NetworkResolveHost(final String hostname) throws UnknownHostException
	{
		return InetAddress.getByName(hostname); 
		/*
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
		 */
	}

	/**
	// Converts a string to ip/port/player
	 * @param s  Format: IP#player:port
	 */
	public static boolean ParseConnectionString(final String []player, final String []port, String [] host, String s)
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
	static NetworkClientState NetworkAllocClient(SocketChannel sc)
	{
		NetworkClientState cs;
		NetworkClientInfo ci;
		//int client_no = 0;

		if (Global._network_server) {
			// Can we handle a new client?
			if (_network_clients_connected  >= MAX_CLIENTS)
				return null;

			if (_network_game_info.clients_on >= _network_game_info.clients_max)
				return null;

			// Register the login
			//client_no = 
			_network_clients_connected++;
		}

		//cs = &_clients[client_no];
		//memset(cs, 0, sizeof(*cs));
		cs = new NetworkClientState();
		//_clients[client_no] = cs;

		cs.socket = sc;
		cs.last_frame = 0;
		cs.quited = false;

		cs.last_frame = Global._frame_counter;
		cs.last_frame_server = Global._frame_counter;

		if (Global._network_server) {
			ci = new NetworkClientInfo();
			//_network_client_info[cs - _clients] = ci; //DEREF_CLIENT_INFO(cs);
			//memset(ci, 0, sizeof(*ci));
			cs.ci = ci;

			cs.index = _network_client_index++;
			ci.client_index = cs.getIndex();
			ci.join_date = Global.get_date();

			Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);
		}

		_clients.add(cs);

		return cs;
	}

	// Close a connection
	static void NetworkCloseClient(NetworkClientState cs)
	{
		// Socket is already dead
		if (cs.socket == null) {
			cs.quited = true;
			return;
		}

		Global.DEBUG_net( 1, "[NET] Closed client connection");

		if (!cs.quited && Global._network_server && cs.status.ordinal() > ClientStatus.INACTIVE.ordinal()) {
			// We did not receive a leave message from this client...
			NetworkErrorCode errorno = NetworkErrorCode.CONNECTION_LOST;
			String str;
			String client_name;
			//NetworkClientState new_cs;

			client_name = NetworkGetClientName(cs);

			// TODO check ordinals?
			str = Strings.GetString(Str.STR_NETWORK_ERR_CLIENT_GENERAL + errorno.ordinal());

			NetworkTextMessage(NetworkAction.LEAVE, 1, false, client_name, "%s", str);

			// Inform other clients of this... strange leaving ;)
			//FOR_ALL_CLIENTS(new_cs) 
			for(NetworkClientState new_cs : _clients) 
			{
				if( !new_cs.hasValidSocket() ) continue;

				if (new_cs.status.ordinal() > ClientStatus.AUTH.ordinal() && cs != new_cs) {
					//SEND_COMMAND(PacketType.SERVER_ERROR_QUIT, new_cs, cs.index, errorno);
					//SEND_COMMAND(PACKET_SERVER_ERROR_QUIT)(new_cs, cs->index, errorno);
					NetServer.NetworkPacketSend_PACKET_SERVER_ERROR_QUIT_command(new_cs, cs.getIndex(), errorno);
				}
			}
		}

		/* When the client was PRE_ACTIVE, the server was in pause mode, so unpause */
		if (cs.status == ClientStatus.PRE_ACTIVE && _network_pause_on_join) {
			Cmd.DoCommandP(null, 0, 0, null, Cmd.CMD_PAUSE);
			try {
				NetworkServer_HandleChat(NetworkAction.CHAT, DestType.BROADCAST, 0, "Game unpaused", NETWORK_SERVER_INDEX);
			} catch (IOException e) {
				Global.error(e);
			}
		}

		try {
			cs.socket.close();
		} catch (IOException e) {			
			Global.error(e);// e.printStackTrace();
		}
		cs.writable = false;
		cs.quited = true;

		// Free all pending and partially received packets
		/*while (cs.packet_queue != null) {
			Packet p = cs.packet_queue.next;
			//free(cs.packet_queue);
			cs.packet_queue = p;
		}*/
		cs.packet_queue.clear();;
		//cs.packet_queue.
		//free(cs.packet_recv);
		//cs.packet_recv = null;

		/*while (cs.command_queue != null) {
			CommandPacket p = cs.command_queue.next;
			//free(cs.command_queue);
			cs.command_queue = p;
		}*/

		// Close the gap in the client-list
		NetworkClientInfo ci = cs.ci; // [cs - _clients]; // DEREF_CLIENT_INFO(cs);

		if (Global._network_server) {
			// We just lost one client :(
			if (cs.status.ordinal() > ClientStatus.INACTIVE.ordinal())
				_network_game_info.clients_on--;
			_network_clients_connected--;

			_clients.remove(cs);

			/*
			while ((cs + 1) != _clients[MAX_CLIENTS] && (cs + 1).socket != null) {
			 *cs = *(cs + 1);
			 *ci = *(ci + 1);
				cs++;
				ci++;
			} */

			Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);
		}

		// Reset the status of the last socket [dz] meaningless in Java code?
		cs.socket = null;
		cs.status = ClientStatus.INACTIVE;
		cs.index = NETWORK_EMPTY_INDEX;
		if(ci != null) ci.client_index = NETWORK_EMPTY_INDEX;
	}

	// A client wants to connect to a server
	static void NetworkConnect(final String hostname, int port) throws IOException
	{

		Global.DEBUG_net( 1, "[NET] Connecting to %s %d", hostname, port);

		//Socket s = new Socket(hostname,port);

		//s.set

		/*
		sockaddr_in sin;
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
		 */

		// in client mode, only the first client field is used. it's pointing to the server.
		NetworkAllocClient(SocketChannel.open());

		NetGui.ShowJoinStatusWindow();

		//memcpy(&network_tmp_patches, &Global._patches, sizeof(_patches));
		// TODO [dz] XXX memcpy(network_tmp_patches, Global._patches );

		//return true;
	}

	// For the server, to accept new clients
	static void NetworkAcceptClients(SocketChannel sc) throws IOException
	{
		//sockaddr_in sin;
		NetworkClientState cs;
		int i;
		boolean banned;

		sc.configureBlocking(false);
		InetSocketAddress remote = (InetSocketAddress) sc.getRemoteAddress();

		Global.DEBUG_net( 1, "[NET] Client connected from %s.%d on frame %d", remote.getHostString(), remote.getPort(), Global._frame_counter);

		// TODO SetNoDelay(s); // XXX error handling?

		/* Check if the client is banned */
		banned = false;
		for (i = 0; i < _network_ban_list.length; i++) {
			if (_network_ban_list[i] == null)
				continue;

			//if (sin.sin_addr.s_addr == inet_addr(_network_ban_list[i])) {
			if( remote.getAddress().equals(NetworkResolveHost(_network_ban_list[i]))) {
				Packet p = new Packet(PacketType.SERVER_BANNED);

				Global.DEBUG_net( 1, "[NET] Banned ip tried to join (%s), refused", _network_ban_list[i]);

				p.sendTo(sc);
				sc.close();

				//free(p);

				banned = true;
				break;
			}
		}
		/* If this client is banned, continue with next client */
		if (banned)
			return;

		cs = NetworkAllocClient(sc);
		if (cs == null) {
			// no more clients allowed?
			// Send to the client that we are full!
			Packet p = new Packet(PacketType.SERVER_FULL);
			p.sendTo(sc);
			sc.close();
			return;
		}

		// a new client has connected. We set him at inactive for now
		//  maybe he is only requesting server-info. Till he has sent a PacketType.CLIENT_MAP_OK
		//  the client stays inactive
		cs.status = ClientStatus.INACTIVE;

		{
			// Save the IP of the client
			NetworkClientInfo ci = cs.ci; //[cs - _clients]; // DEREF_CLIENT_INFO(cs);
			ci.client_ip = remote.getAddress();
		}
	}


	// Set up the listen socket for the server
	static void NetworkListen() throws IOException
	{
		//sockaddr_in sin;
		int port = _network_server_port;

		Global.DEBUG_net( 1, "[NET] Listening on %s:%d", _network_server_bind_ip_host, port);

		//ServerSocket  ls = new ServerSocket(port);
		/* TODO
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

		if (ls.listen(1) != 0) {
			ServerStartError("listen() failed");
			return false;
		}

		 */

		_listensocket = ServerSocketChannel.open();

		//return true;
	}

	// Close all current connections
	static void NetworkClose()
	{
		for(NetworkClientState cs : _clients) 
		{
			if( !cs.hasValidSocket() ) continue;

			if (!Global._network_server) {
				//SEND_COMMAND(PacketType.CLIENT_QUIT, "leaving");
				try {
					NetClient.NetworkPacketSend_PACKET_CLIENT_QUIT_command("leaving");
				} catch (IOException e) {
					Global.error(e);
				}
				NetworkSend_Packets(cs);
			}
			NetworkCloseClient(cs);
		}

		if (Global._network_server) {
			// We are a server, also close the listensocket
			try {
				_listensocket.close();
			} catch (IOException e) {
				// e.printStackTrace();
				Global.error(e);
			};
			_listensocket = null;
			Global.DEBUG_net( 1, "[NET] Closed listener");
			NetUDP.NetworkUDPClose();
		}
	}

	// Inits the network (cleans sockets and stuff)
	static void NetworkInitialize()
	{
		//NetworkClientState cs;

		//_local_command_queue = null;
		_local_command_queue.clear();

		// Clean all client-sockets
		//memset(_clients, 0, sizeof(_clients));
		for (NetworkClientState cs : _clients) {
			cs.socket = null;
			cs.status = ClientStatus.INACTIVE;
			cs.command_queue = null;
		}

		// Clean the client_info memory
		//memset(_network_client_info, 0, sizeof(_network_client_info));
		//_network_client_info = new NetworkClientInfo[MAX_CLIENT_INFO];
		//memset(_network_player_info, 0, sizeof(_network_player_info));
		_network_player_info = new NetworkPlayerInfo[Global.MAX_PLAYERS];
		_network_lobby_company_count = 0;

		_sync_frame = 0;
		_network_first_time = true;

		_network_reconnect = 0;

		NetUDP.NetworkUDPInitialize();
	}

	// Query a server to fetch his game-info
	//  If game_info is true, only the gameinfo is fetched,
	//   else only the client_info is fetched
	static NetworkGameList NetworkQueryServer(final String host, int port, boolean game_info)
	{
		if (!Global._network_available) return null;

		NetworkDisconnect();

		if (game_info) {
			return NetUDP.NetworkUDPQueryServer(host, port);
		}

		NetworkInitialize();

		Global._network_server = false;

		// Try to connect
		Global._networking = false;

		try {
			NetworkConnect(host, port);
			Global._networking = true;
		} catch (IOException e) {
			// e.printStackTrace();
			Global.error(e);
		}

		//		ttd_strlcpy(_network_last_host, host, sizeof(_network_last_host));
		//		_network_last_port = port;

		// We are connected
		if (Global._networking) {
			//SEND_COMMAND(PacketType.CLIENT_COMPANY_INFO );
			NetClient.NetworkPacketSend_PACKET_CLIENT_COMPANY_INFO_command();
			return null;
		}

		// No networking, close everything down again
		NetworkDisconnect();
		return null;
	}

	/* Validates an address entered as a string and adds the server to
	 * the list. If you use this functions, the games will be marked
	 * as manually added. */
	static void NetworkAddServer(final String b)
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
	static void NetworkRebuildHostList()
	{
		/*NetworkGameList item = _network_game_list;
		while (item != null && i != _network_host_list.length) {
			if (item.manually)
				_network_host_list[i++] = String.format("%s:%d", item.info.hostname, item.port);
			item = item.next;
		}*/

		int i = 0;
		for(NetworkGameList item : NetworkGameList._network_game_list)
		{
			if( i >= _network_host_list.length ) break;

			if (item.manually)
				_network_host_list[i++] = String.format("%s:%d", item.info.hostname, item.port);
		}

		for (; i < _network_host_list.length; i++) {
			_network_host_list[i] = "";
		}
	}

	// Used by clients, to connect to a server
	public static boolean NetworkClientConnectGame(final String host, int port)
	{
		if (!Global._network_available) return false;

		if (port == 0) return false;

		_network_last_host = host;
		_network_last_port = port;

		NetworkDisconnect();
		NetUDP.NetworkUDPClose();
		NetworkInitialize();

		// Try to connect
		//Global._networking = NetworkConnect(host, port);
		Global._networking = false;

		try {
			NetworkConnect(host, port);
			Global._networking = true;
		} catch (IOException e) {
			// e.printStackTrace();
			Global.error(e);
		}

		// We are connected
		if (Global._networking) {
			ConsoleFactory.INSTANCE.getConsole().IConsoleCmdExec("exec scripts/on_client.scr 0");
			try {
				NetClient.NetworkClient_Connected();
			} catch (IOException e) {
				Global.error(e);
				NetworkError(Str.STR_NETWORK_ERR_NOCONNECTION); // TODO disconnect?
			}
		} else {
			// Connecting failed
			NetworkError(Str.STR_NETWORK_ERR_NOCONNECTION);
		}

		return Global._networking;
	}

	static NetworkClientInfo serverCi = new NetworkClientInfo(); // TODO must be in common list? No?

	static void NetworkInitGameInfo()
	{
		NetworkClientInfo ci;

		_network_game_info.server_name = _network_server_name;
		_network_game_info.server_password = _network_server_password;
		_network_game_info.rcon_password = _network_rcon_password;
		if (_network_game_info.server_name.length() == 0)
			_network_game_info.server_name = "Unnamed Server";

		// The server is a client too ;)
		if (Global._network_dedicated) {
			_network_game_info.clients_on = 0;
			_network_game_info.dedicated = true;
		} else {
			_network_game_info.clients_on = 1;
			_network_game_info.dedicated = false;
		}
		_network_game_info.server_revision = Version.NAME;
		_network_game_info.spectators_on = 0;
		_network_game_info.game_date = Global.get_date();
		_network_game_info.start_date = GameDate.ConvertIntDate(Global._patches.starting_date);
		_network_game_info.map_width = Global.MapSizeX();
		_network_game_info.map_height = Global.MapSizeY();
		_network_game_info.map_set = GameOptions._opt.landscape;

		_network_game_info.use_password = !_network_server_password.isBlank();

		// We use _network_client_info[MAX_CLIENT_INFO - 1] to store the server-data in it
		//  The index is NETWORK_SERVER_INDEX ( = 1)
		//ci = _network_client_info[MAX_CLIENT_INFO - 1];
		//memset(ci, 0, sizeof(ci));
		serverCi = new NetworkClientInfo();
		ci = serverCi;

		ci.client_index = NETWORK_SERVER_INDEX;
		if (Global._network_dedicated)
			ci.client_playas = Owner.OWNER_SPECTATOR;
		else
			ci.client_playas = PlayerID.getCurrent().id + 1;
		ci.client_name = _network_player_name;
		ci.unique_id = _network_unique_id;
	}

	public static boolean NetworkServerStart()
	{
		if (!Global._network_available) return false;

		/* Call the pre-scripts */
		
		if (Global._network_dedicated)
			ConsoleFactory.INSTANCE.getConsole().IConsoleCmdExec("exec scripts/pre_server.scr 0");

		NetworkInitialize();
		//if (!NetworkListen())			return false;
		try {
			NetworkListen();
		} catch (IOException e) {
			Global.error(e);
			//e.printStackTrace();
			return false;
		} 

		// Try to start UDP-server
		_network_udp_server = true;
		try {
			NetUDP.NetworkUDPListen(_udp_server_socket, new InetSocketAddress(_network_server_bind_ip, _network_server_port), false);
		} catch (IOException e) {
			// e.printStackTrace();
			Global.error(e);
			_network_udp_server = false;
		}

		Global._network_server = true;
		Global._networking = true;
		Global._frame_counter = 0;
		_frame_counter_server = 0;
		_frame_counter_max = 0;
		_last_sync_frame = 0;
		_network_own_client_index = NETWORK_SERVER_INDEX;

		if (!Global._network_dedicated)
			Global._network_playas = 1;

		_network_clients_connected = 0;

		NetworkInitGameInfo();

		// execute server initialization script
		ConsoleFactory.INSTANCE.getConsole().IConsoleCmdExec("exec scripts/on_server.scr 0");
		// if the server is dedicated ... add some other script
		if (Global._network_dedicated) 
			ConsoleFactory.INSTANCE.getConsole().IConsoleCmdExec("exec scripts/on_dedicated.scr 0");

		/* Try to register us to the master server */
		_network_last_advertise_date = 0;
		try {
			NetUDP.NetworkUDPAdvertise();
		} catch (IOException e) {
			// e.printStackTrace();
			Global.error(e);
		}
		return true;
	}

	// The server is rebooting...
	// The only difference with NetworkDisconnect, is the packets that is sent
	public static void NetworkReboot()
	{
		if (Global._network_server) {
			//NetworkClientState cs;
			//FOR_ALL_CLIENTS(cs) {
			for(NetworkClientState cs : _clients) 
			{
				if( !cs.hasValidSocket() ) continue;
				//SEND_COMMAND(PacketType.SERVER_NEWGAME, cs);
				NetServer.NetworkPacketSend_PACKET_SERVER_NEWGAME_command(cs);
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
		//_local_command_queue = null;
		_local_command_queue.clear();

		Global._networking = false;
		Global._network_server = false;
	}

	// We want to disconnect from the host/clients
	public static void NetworkDisconnect()
	{
		if (Global._network_server) {
			//NetworkClientState cs;
			FOR_ALL_CLIENTS(cs -> {
				NetServer.NetworkPacketSend_PACKET_SERVER_SHUTDOWN_command(cs);
				NetworkSend_Packets(cs);
			});
		}

		if (_network_advertise)
			NetUDP.NetworkUDPRemoveAdvertise();

		Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);

		NetworkClose();

		// Free all queued commands
		/*while (_local_command_queue != null) {
			CommandPacket p = _local_command_queue;
			_local_command_queue = _local_command_queue.next;
			free(p);
		}*/
		_local_command_queue.clear(); // = null;

		if (Global._networking && !Global._network_server) {
			//memcpy(&_patches, &network_tmp_patches, sizeof(_patches));
			// TODO [dz] XXX memcpy(_patches, network_tmp_patches);
		}

		Global._networking = false;
		Global._network_server = false;
	}

	public static void FOR_ALL_CLIENTS(Consumer<NetworkClientState> s) 
	{
		for (NetworkClientState cs : _clients)
		{
			if( cs.hasValidSocket() )
				s.accept(cs);
		}
	}

	// Receives something from the network
	static boolean NetworkReceive() throws IOException
	{
		//NetworkClientState cs;
		//int n;
		//fd_set read_fd, write_fd;
		//timeval tv;

		Selector selector = Selector.open();

		//FD_ZERO(&read_fd);
		//FD_ZERO(&write_fd);

		FOR_ALL_CLIENTS(cs -> {
			//FD_SET(cs.socket, &read_fd);
			//FD_SET(cs.socket, &write_fd);

			SocketChannel socketChannel = cs.socket;

			try {
				socketChannel.configureBlocking(false);
				socketChannel.register(selector, SelectionKey.OP_READ);
				socketChannel.register(selector, SelectionKey.OP_WRITE);
			} catch (IOException e) {
				Global.error("Nonblocking net IO failed: %s", e);
			}
		});

		// take care of listener port
		if (Global._network_server) {
			//FD_SET(_listensocket, &read_fd);
			_listensocket.configureBlocking(false);
			//_listensocket.register(selector, SelectionKey.OP_CONNECT);
			_listensocket.register(selector, SelectionKey.OP_ACCEPT);
		}

		//tv.tv_sec = tv.tv_usec = 0; // don't block at all.

		int count = selector.selectNow();
		//n = select(FD_SETSIZE, &read_fd, &write_fd, null, &tv);
		//if (n == -1 && !Global._network_server) NetworkError(Str.STR_NETWORK_ERR_LOSTCONNECTION);

		if( count == 0 ) 
			return true;

		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectedKeys.iterator();

		while (iterator.hasNext()) {
			SelectionKey key = (SelectionKey) iterator.next();
			iterator.remove();
			if (key.isAcceptable()) {
				SocketChannel sc = _listensocket.accept();
				/*
				sc.register(selector, SelectionKey.
						OP_READ); */
				System.out.println("Connection Accepted: " + sc.getLocalAddress() + "n");
				NetworkAcceptClients(sc);
			}

			if (key.isWritable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				NetworkClientState cs = getClientForSocketChannel(sc);
				if( cs != null ) cs.writable = true;
			}

			if (key.isReadable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				NetworkClientState cs = getClientForSocketChannel(sc);

				if(cs == null)
					continue;
				{
					Global.error("Invalid socket channel %s", sc);
					sc.close();
				}

				if (Global._network_server)
					NetServer.NetworkServer_ReadPackets(cs);
				else {
					NetworkRecvStatus res;
					// The client already was quiting!
					if (cs.quited) return false;
					if ((res = NetClient.NetworkClient_ReadPackets(cs)) != NetworkRecvStatus.OKAY) {
						// The client made an error of which we can not recover
						//   close the client and drop back to main menu

						NetworkClientError(res, cs);
						return false;
					}
				}
			}
		}


		/*
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
		}
		 */
		return true;
	}

	// TODO can we do it better?
	private static NetworkClientState getClientForSocketChannel(SocketChannel sc) throws IOException 
	{
		for(NetworkClientState cs : _clients)
		{
			if(!cs.hasValidSocket()) continue;

			if( cs.socket.equals(sc) )
				return cs;
		}

		Global.error("Invalid socket channel %s", sc);
		sc.close();

		return null;
	}

	// This sends all buffered commands (if possible)
	static void NetworkSend()
	{
		//NetworkClientState cs;
		FOR_ALL_CLIENTS(cs -> {
			if (cs.writable) {
				NetworkSend_Packets(cs);

				if (cs.status == ClientStatus.MAP) {
					// This client is in the middle of a map-send, call the function for that
					try {
						NetServer.NetworkPacketSend_PACKET_SERVER_MAP_command(cs);
					} catch (IOException e) {
						Global.error(e);
					}
				}
			}
		});
	}

	// Handle the local-command-queue
	static void NetworkHandleLocalQueue()
	{
		//CommandPacket cp;
		//CommandPacket cp_prev;

		//cp_prev = _local_command_queue;

		//while ( (cp = cp_prev) != null)
		while(!_local_command_queue.isEmpty())
		{
			CommandPacket cp = _local_command_queue.remove(0); 
			// The queue is always in order, which means
			// that the first element will be executed first.
			if (Global._frame_counter < cp.frame)
				break;

			if (Global._frame_counter > cp.frame) {
				// If we reach here, it means for whatever reason, we've already executed
				// past the command we need to execute.
				Global.DEBUG_net( 0, "[NET] Trying to execute a packet in the past!");
				assert false;
			}

			// We can execute this command
			NetworkExecuteCommand(cp);

			//cp_prev = cp.next;
			//free(cp);
		}

		// Just a safety check, to be removed in the future.
		// Make sure that no older command appears towards the end of the queue
		// In that case we missed executing it. This will never happen.
		//for(cp = _local_command_queue; cp != null; cp = cp.next) {
		//	assert(Global._frame_counter < cp.frame);
		//}
		for(CommandPacket cp : _local_command_queue) 
			assert(Global._frame_counter < cp.frame);		
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
					NetworkClientError(NetworkRecvStatus.DESYNC, _clients.get(0));
					return false;
				}

				// If this is the first time we have a sync-frame, we
				//   need to let the server know that we are ready and at the same
				//   frame as he is.. so we can start playing!
				if (_network_first_time) {
					_network_first_time = false;
					NetClient.NetworkPacketSend_PACKET_CLIENT_ACK_command();
				}

				_sync_frame = 0;
			} else if (_sync_frame < Global._frame_counter) {
				Global.DEBUG_net( 1, "[NET] Missed frame for sync-test (%d / %d)", _sync_frame, Global._frame_counter);
				_sync_frame = 0;
			}
		}

		return true;
	}

	public static void NetworkUDPGameLoop()
	{
		try {
			doNetworkUDPGameLoop();
		} catch (IOException e) {
			// e.printStackTrace();
			Global.error(e);
		}
	}

	// We have to do some UDP checking
	public static void doNetworkUDPGameLoop() throws IOException
	{
		if (_network_udp_server) {
			NetUDP.NetworkUDPReceive(_udp_server_socket[0]);
			if (_udp_master_socket[0] != null) {
				NetUDP.NetworkUDPReceive(_udp_master_socket[0]);
			}
		}
		else if (_udp_client_socket[0] != null) {
			NetUDP.NetworkUDPReceive(_udp_client_socket[0]);
			if (_network_udp_broadcast > 0)
				_network_udp_broadcast--;
		}
	}

	// The main loop called from ttd.c
	//  Here we also have to do StateGameLoop if needed!
	public static void NetworkGameLoop()
	{
		if (!Global._networking) return;

		boolean networkReceiveStatus = false;
		try {
			networkReceiveStatus = NetworkReceive();
		} catch (IOException e) {
			// e.printStackTrace();
			Global.error(e);
		}
		if (!networkReceiveStatus) return;

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

			try {
				NetServer.NetworkServer_Tick(send_frame);
			} catch (IOException e) {
				Global.error(e);
			}
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

	static void NetworkGenerateUniqueId() throws NoSuchAlgorithmException
	{
		//md5_state_t state;
		//md5_byte_t digest[16];
		//char hex_output[16*2 + 1];
		//int di;

		String coding_string = String.format("%d%s", (int)Hal.Random(), "OpenTTD Unique ID");

		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] digest = md.digest(coding_string.getBytes());

		BigInteger bigInt = new BigInteger(1,digest);
		_network_unique_id = bigInt.toString(16);		
		/* Generate the MD5 hash * /
		md5_init(&state);
		md5_append(&state, (final md5_byte_t*)coding_string, strlen(coding_string));
		md5_finish(&state, digest);

		for (di = 0; di < 16; ++di)
			sprintf(hex_output + di * 2, "%02x", digest[di]);

		/* _network_unique_id is our id * /
		snprintf(_network_unique_id, sizeof(_network_unique_id), "%s", hex_output);
		 */
	}





	public static void NetworkStartUp()
	{
		try {
			doNetworkStartUp();
		} catch (UnknownHostException | SocketException e) {
			// e.printStackTrace();
			Global.error(e);
		}
	}

	// This tries to launch the network for a given OS
	static void doNetworkStartUp() throws UnknownHostException, SocketException
	{
		Global.DEBUG_net( 3, "[NET][Core] Starting network...");

		// Network is available
		Global._network_available = true;
		Global._network_dedicated = false;
		_network_last_advertise_date = 0;
		_network_advertise_retries = 0;

		/* Load the ip from the openttd.cfg */
		_network_server_bind_ip = NetworkResolveHost(_network_server_bind_ip_host);
		/* And put the data back in it in case it was an invalid ip */
		//snprintf(_network_server_bind_ip_host, sizeof(_network_server_bind_ip_host), "%s", inet_ntoa(*(struct in_addr *)&_network_server_bind_ip));
		_network_server_bind_ip_host = _network_server_bind_ip.getHostName();

		/* Generate an unique id when there is none yet */
		if (_network_unique_id == null)
		{
			try {
				NetworkGenerateUniqueId();
			} catch (NoSuchAlgorithmException e) {
				//e.printStackTrace();
				Global.fail("NetworkGenerateUniqueId: %s", e);
			}
		}
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
	public static void NetworkShutDown()
	{
		Global.DEBUG_net( 3, "[NET][Core] Shutting down the network.");
		Global._network_available = false;
	}





	/*/ Create a packet for sending
	static Packet NetworkSend_Init(PacketType type)
	{
		return new Packet(type);
		/*
		Packet packet = new Packet();
		// An error is inplace here, because it simply means we ran out of memory.
		//if (packet == null) error("Failed to allocate Packet");

		// Skip the size so we can write that in before sending the packet
		packet.size = sizeof(packet.size);
		packet.buffer[packet.size++] = type;
		packet.pos = 0;

		return packet;
	 * /
	} */


	// Sends all the buffered packets out for this client
	//  it stops when:
	//   1) all packets are send (queue is empty)
	//   2) the OS reports back that it can not send any more
	//        data right now (full network-buffer, it happens ;))
	//   3) sending took too long
	static boolean NetworkSend_Packets(NetworkClientState cs)
	{
		//int res;

		// We can not write to this socket!!
		if (!cs.writable) return false;
		if (cs.socket == null) return false;

		Packet p = cs.packet_queue.remove(0);
		if(null == p) return true;
		//while (p != null) {
		//res = send(cs.socket, p.buffer + p.pos, p.size - p.pos, 0);
		try {
			p.sendTo(cs.socket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		/*if (res == -1) {
				int err = GET_LAST_ERROR();
				if (err != EWOULDBLOCK) {
					// Something went wrong.. close client!
					Global.DEBUG_net(0,"[NET] send() failed with error %d", err);
					CloseConnection(cs);
					return false;
				}
				return true;
			}
			if (res == 0) {
				// Client/server has left us :(
				CloseConnection(cs);
				return false;
			}*/

		//p.pos += res;

		// Is this packet sent?
		//if (p.pos == p.size) 
		/*{
			// Go to the next packet
			cs.packet_queue = p.next;
			//free(p);
			p = cs.packet_queue;
		} */
		//else				return true;
		//}

		return true;
	}

	static Packet NetworkRecv_Packet(NetworkClientState cs, NetworkRecvStatus [] status) throws IOException
	{
		//int res;
		//Packet p;

		status[0] = NetworkRecvStatus.OKAY;

		if (cs.socket == null) return null;

		/*
		if (cs.packet_recv == null) {
			cs.packet_recv = new Packet();
			//if (cs.packet_recv == null) error("Failed to allocate packet");
			// Set pos to zero!
			//cs.packet_recv.pos = 0;
			//cs.packet_recv.size = 0; // Can be ommited, just for safety reasons
		}*/

		//InputStream is = cs.socket.getInputStream();
		//byte [] buffer = new byte[Packet.SEND_MTU];
		ByteBuffer bb = ByteBuffer.allocate(Packet.HEADER_SIZE);
		int len = cs.socket.read(bb);

		if(len != Packet.HEADER_SIZE)
		{
			Global.DEBUG_net(  0, "[NET] recv() failed, can't read header");
			status[0] = CloseConnection(cs);
			return null;
		}

		byte[] header = bb.array();
		//Packet p = new Packet(bb.array());
		//cs.packet_recv = p;
		int packetType = header[2];
		int packetLen = Packet.parseLen(header);

		ByteBuffer data = ByteBuffer.allocate(packetLen);
		len = cs.socket.read(data);

		if(len != packetLen)
		{
			Global.DEBUG_net(  0, "[NET] recv() failed, can't read data (%d)", packetLen);
			status[0] = CloseConnection(cs);
			return null;
		}


		Packet p = new Packet( packetType, data.array() );

		//p.next = null; // Should not be needed, but who knows...

		// Prepare for receiving a new packet
		//cs.packet_recv = null;

		return p;
	}

	// Add a command to the local command queue
	static void NetworkAddCommandQueue(NetworkClientState cs, CommandPacket cp)
	{
		cs.command_queue.add(cp); // TODO make sure it is not modified - Java Record or just final for all fields
		/*
		CommandPacket new_cp = new CommandPacket();

		 *new_cp = *cp;

		if (cs.command_queue == null)
			cs.command_queue = new_cp;
		else {
			CommandPacket c = cs.command_queue;
			while (c.next != null) c = c.next;
			c.next = new_cp;
		}
		 */
	}

	// Prepare a DoCommand to be send over the network
	public static void NetworkSend_Command(TileIndex tile, int p1, int p2, int cmd, CommandCallback callback)
	{
		CommandPacket c = new CommandPacket();
		byte temp_callback;

		c.player =  Global.gs._local_player;
		//c.next = null;
		c.tile = tile;
		c.p1 = p1;
		c.p2 = p2;
		c.cmd = cmd;
		c.callback = 0;

		temp_callback = 0;

		while (temp_callback < CallbackTable._callback_table_count && CallbackTable._callback_table[temp_callback] != callback)
			temp_callback++;
		if (temp_callback == CallbackTable._callback_table_count) {
			Global.DEBUG_net(  0, "[NET] Unknown callback. (Pointer: %p) No callback sent.", callback);
			temp_callback = 0; /* _callback_table[0] == null */
		}

		if (Global._network_server) {
			// We are the server, so set the command to be executed next possible frame
			c.frame = _frame_counter_max + 1;
		} else {
			c.frame = 0; // The client can't tell which frame, so just make it 0
		}

		c.text = (Global._cmd_text != null) ? Global._cmd_text : "";

		if (Global._network_server) {
			// If we are the server, we queue the command in our 'special' queue.
			//   In theory, we could execute the command right away, but then the
			//   client on the server can do everything 1 tick faster than others.
			//   So to keep the game fair, we delay the command with 1 tick
			//   which gives about the same speed as most clients.
			//NetworkClientState cs;

			// And we queue it for delivery to the clients
			FOR_ALL_CLIENTS(cs -> {
				if (cs.status.ordinal() > ClientStatus.AUTH.ordinal()) {
					NetworkAddCommandQueue(cs, c);
				}
			});

			// Only the server gets the callback, because clients should not get them
			c.callback = temp_callback;
			_local_command_queue.add(c);
			/*if (_local_command_queue == null) {
				_local_command_queue = c;
			} else {
				// Find last packet
				CommandPacket cp = _local_command_queue;
				while (cp.next != null) cp = cp.next;
				cp.next = c;
			}*/

			return;
		}

		// Clients send their command to the server and forget all about the packet
		c.callback = temp_callback;
		try {
			NetClient.NetworkPacketSend_PACKET_CLIENT_COMMAND_command(c);
		} catch (IOException e) {
			// TODO error handling?
			e.printStackTrace();
		}

	}

	// Execute a DoCommand we received from the network
	static void NetworkExecuteCommand(CommandPacket cp)
	{
		PlayerID.setCurrent( cp.player );
		Global._cmd_text = cp.text;
		/* cp.callback is unsigned. so we don't need to do lower bounds checking. */
		if (cp.callback > CallbackTable._callback_table_count) {
			Global.DEBUG_net( 0, "[NET] Received out-of-bounds callback! (%d)", cp.callback);
			cp.callback = 0;
		}
		Cmd.DoCommandP(cp.tile, cp.p1, cp.p2, CallbackTable._callback_table[cp.callback], cp.cmd | Cmd.CMD_NETWORK_COMMAND);
	}



	// Functions to help NetworkRecv_Packet/NetworkSend_Packet a bit
	//  A socket can make errors. When that happens
	//  this handles what to do.
	// For clients: close connection and drop back to main-menu
	// For servers: close connection and that is it
	static NetworkRecvStatus CloseConnection(NetworkClientState cs)
	{
		NetworkCloseClient(cs);

		// Clients drop back to the main menu
		if (!Global._network_server) {
			Global._switch_mode = SwitchModes.SM_MENU;
			Global._networking = false;
			Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_LOSTCONNECTION);

			return NetworkRecvStatus.CONN_LOST;
		}

		return NetworkRecvStatus.OKAY;
	}



	static void NetworkServer_HandleChat(NetworkAction action, DestType desttype, int dest, String msg, final int from_index) throws IOException
	{
		//NetworkClientState cs;
		final NetworkClientInfo ci = NetworkFindClientInfoFromIndex(from_index);
		final int playerColor = Hal.GetDrawStringPlayerColor(PlayerID.get(ci.client_playas-1));

		NetworkClientInfo ci_own; //, ci_to;


		switch (desttype) {
		case CLIENT:
			/* Are we sending to the server? */
			if (dest == NETWORK_SERVER_INDEX) {
				/* Display the text locally, and that is it */
				if (ci != null)
					NetworkTextMessage(action, playerColor, false, ci.client_name, "%s", msg);
			} else {
				/* Else find the client to send the message to */
				//FOR_ALL_CLIENTS(cs) {
				for(NetworkClientState cs : _clients) 
				{
					if( !cs.hasValidSocket() ) continue;
					if (cs.getIndex() == dest) {
						//SEND_COMMAND(PacketType.SERVER_CHAT,cs, action, from_index, false, msg);
						NetServer.NetworkPacketSend_PACKET_SERVER_CHAT_command(cs, action, from_index, false, msg);
						break;
					}
				}
			}

			// Display the message locally (so you know you have sent it)
			if (from_index != dest) {
				if (from_index == NETWORK_SERVER_INDEX) {
					//ci = NetworkFindClientInfoFromIndex(from_index);
					NetworkClientInfo ci_to = NetworkFindClientInfoFromIndex(dest);
					if (ci != null && ci_to != null)
						NetworkTextMessage(action, playerColor, true, ci_to.client_name, "%s", msg);
				} else {
					//FOR_ALL_CLIENTS(cs) {
					for(NetworkClientState cs : _clients) 
					{
						if( !cs.hasValidSocket() ) continue;
						if (cs.getIndex() == from_index) {
							//SEND_COMMAND(PacketType.SERVER_CHAT, cs, action, dest, true, msg);
							NetServer.NetworkPacketSend_PACKET_SERVER_CHAT_command(cs, action, dest, true, msg);
							break;
						}
					}
				}
			}
			break;
		case PLAYER: {
			boolean show_local = true; // If this is false, the message is already displayed
			// on the client who did sent it.
			/* Find all clients that belong to this player */
			NetworkClientInfo ci_to = null;
			//FOR_ALL_CLIENTS(cs) {
			for(NetworkClientState cs : _clients) 
			{
				if( !cs.hasValidSocket() ) continue;
				NetworkClientInfo lci = cs.ci; //[cs - _clients];
				if (lci.client_playas == dest) {
					//SEND_COMMAND(PacketType.SERVER_CHAT, cs, action, from_index, false, msg);
					NetServer.NetworkPacketSend_PACKET_SERVER_CHAT_command(cs, action, from_index, false, msg);
					if (cs.getIndex() == from_index) {
						show_local = false;
					}
					ci_to = lci; // Remember a client that is in the company for company-name
				}
			}

			//ci = NetworkFindClientInfoFromIndex(from_index);
			ci_own = NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX);
			if (ci != null && ci_own != null && ci_own.client_playas == dest) {
				NetworkTextMessage(action, playerColor, false, ci.client_name, "%s", msg);
				if (from_index == NETWORK_SERVER_INDEX)
					show_local = false;
				ci_to = ci_own;
			}

			/* There is no such player */
			if (ci_to == null) break;

			final NetworkClientInfo ci_to_final = ci_to;

			// Display the message locally (so you know you have sent it)
			if (ci != null && show_local) {
				if (from_index == NETWORK_SERVER_INDEX) {
					//char name[NETWORK_NAME_LENGTH];
					String name = Strings.GetString(Player.GetPlayer(ci_to.client_playas-1).getName_1());
					final int playerColorOwn = Hal.GetDrawStringPlayerColor(PlayerID.get(ci_own.client_playas-1));
					NetworkTextMessage(action, playerColorOwn, true, name, "%s", msg);
				} else {
					FOR_ALL_CLIENTS(cs -> {
						if (cs.getIndex() == from_index) {
							//SEND_COMMAND(PacketType.SERVER_CHAT, cs, action, ci_to_final.client_index, true, msg);
							try {
								NetServer.NetworkPacketSend_PACKET_SERVER_CHAT_command(cs, action, ci_to_final.client_index, true, msg);
							} catch (IOException e) {
								// e.printStackTrace();
								Global.error(e);
							}
						}
					});
				}
			}
		}
		break;
		default:
			Global.DEBUG_net( 0, "[NET][Server] Received unknown destination type %d. Doing broadcast instead.");
			/* fall-through to next case */
		case BROADCAST:
			FOR_ALL_CLIENTS( (cs) -> {
				try {
					NetServer.NetworkPacketSend_PACKET_SERVER_CHAT_command(cs, action, from_index, false, msg);
				} catch (IOException e) {
					Global.error(e);
				}
			});
			//ci = NetworkFindClientInfoFromIndex(from_index);
			if (ci != null)
				NetworkTextMessage(action, playerColor, false, ci.client_name, "%s", msg);
			break;
		}
	}


	static void NetworkSend_Packet(Packet p, NetworkClientState cs) {
		assert p != null;

		cs.packet_queue.add(p);
		
		/*/ Locate last packet buffered for the client
		p = cs->packet_queue;
		if (p == NULL) {
			// No packets yet
			cs->packet_queue = packet;
		} else {
			// Skip to the last packet
			while (p->next != NULL) p = p->next;
			p->next = packet;
		} */
	}

	public static NetworkClientState getClient(int pid) {
		return _clients.get(pid);
	}

	/**
	 * Check if the company has active players 
	 * @param index company index
	 * @return 
	 */
	public static boolean companyHasPlayers(int index) {
		//FOR_ALL_CLIENTS(cs) 
		for( NetworkClientState cs : Net._clients )
		{
			NetworkClientInfo ci = cs.getCi(); //DEREF_CLIENT_INFO(cs);
			if (ci.client_playas - 1 == index) {
				DefaultConsole.IConsoleError("Cannot remove company: a client is connected to that company.");
				return true;
			}
		}
		return false;
	}


	/*protected static void SEND_COMMAND(PacketType clientAck, Object ... args ) {
		// TODO Auto-generated method stub
		implement me
	}*/


}
