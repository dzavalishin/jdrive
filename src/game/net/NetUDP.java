package game.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

import game.GameOptions;
import game.Global;
import game.Player;

public class NetUDP extends Net 
{

	//
	// This file handles all the LAN-stuff
	// Stuff like:
	//   - UDP search over the network
	//

	enum PacketUDPType {
		PACKET_UDP_CLIENT_FIND_SERVER,
		PACKET_UDP_SERVER_RESPONSE,
		PACKET_UDP_CLIENT_DETAIL_INFO,
		PACKET_UDP_SERVER_DETAIL_INFO, // Is not used in OpenTTD itself, only for external querying
		PACKET_UDP_SERVER_REGISTER, // Packet to register itself to the master server
		PACKET_UDP_MASTER_ACK_REGISTER, // Packet indicating registration has succedeed
		PACKET_UDP_CLIENT_GET_LIST, // Request for serverlist from master server
		PACKET_UDP_MASTER_RESPONSE_LIST, // Response from master server with server ip's + port's
		PACKET_UDP_SERVER_UNREGISTER, // Request to be removed from the server-list
		PACKET_UDP_END
	} 


	static final int ADVERTISE_NORMAL_INTERVAL = 450;	// interval between advertising in days
	static final int ADVERTISE_RETRY_INTERVAL = 5;			// readvertise when no response after this amount of days
	static final int ADVERTISE_RETRY_TIMES = 3;					// give up readvertising after this much failed retries


	//#define DEF_UDP_RECEIVE_COMMAND(type) void NetworkPacketReceive_ ## type ## _command(Packet p, InetAddress client_addr)
	//void NetworkSendUDP_Packet(Socket udp, Packet p, InetAddress recv);

	static NetworkClientState _udp_cs;

	static void NetworkPacketReceive_PACKET_UDP_CLIENT_FIND_SERVER_command(Packet p, InetAddress client_addr)
	//DEF_UDP_RECEIVE_COMMAND(PACKET_UDP_CLIENT_FIND_SERVER)
	{
		Packet packet;
		// Just a fail-safe.. should never happen
		if (!_network_udp_server)
			return;

		packet = NetworkSend_Init(PacketType.UDP_SERVER_RESPONSE);

		// Update some game_info
		Net._network_game_info.game_date = Global.get_date();
		Net._network_game_info.map_width = Global.MapSizeX();
		Net._network_game_info.map_height = Global.MapSizeY();
		Net._network_game_info.map_set = GameOptions._opt.landscape;

		/*
		NetworkSend_byte (packet, NETWORK_GAME_INFO_VERSION);
		NetworkSend_string(packet, Net._network_game_info.server_name);
		NetworkSend_string(packet, Net._network_game_info.server_revision);
		NetworkSend_byte (packet, Net._network_game_info.server_lang);
		NetworkSend_byte (packet, Net._network_game_info.use_password);
		NetworkSend_byte (packet, Net._network_game_info.clients_max);
		NetworkSend_byte (packet, Net._network_game_info.clients_on);
		NetworkSend_byte (packet, Net._network_game_info.spectators_on);
		NetworkSend_int(packet, Net._network_game_info.game_date);
		NetworkSend_int(packet, Net._network_game_info.start_date);
		NetworkSend_string(packet, Net._network_game_info.map_name);
		NetworkSend_int(packet, Net._network_game_info.map_width);
		NetworkSend_int(packet, Net._network_game_info.map_height);
		NetworkSend_byte (packet, Net._network_game_info.map_set);
		NetworkSend_byte (packet, Net._network_game_info.dedicated);
		 */

		packet.encodeObject(Net._network_game_info);

		// Let the client know that we are here
		NetworkSendUDP_Packet(_udp_server_socket, packet, client_addr);

		//free(packet);

		Global.DEBUG_net( 2, "[NET][UDP] Queried from %s", client_addr);
	}

	static void NetworkPacketReceive_PACKET_UDP_SERVER_RESPONSE_command(Packet p, InetAddress client_addr)
	//DEF_UDP_RECEIVE_COMMAND(PACKET_UDP_SERVER_RESPONSE)
	{
		NetworkGameList item;
		byte game_info_version;

		// Just a fail-safe.. should never happen
		if (_network_udp_server)
			return;

		game_info_version = NetworkRecv_byte(_udp_cs, p);

		if (_udp_cs.quited)
			return;

		Global.DEBUG_net( 6, "[NET][UDP] Server response from %s:%d", inet_ntoa(client_addr.sin_addr),ntohs(client_addr.sin_port));

		// Find next item
		item = NetworkGameListAddItem(inet_addr(inet_ntoa(client_addr.sin_addr)), ntohs(client_addr.sin_port));

		if (game_info_version == 1) {
			NetworkRecv_string(&_udp_cs, p, item.info.server_name, sizeof(item.info.server_name));
			NetworkRecv_string(&_udp_cs, p, item.info.server_revision, sizeof(item.info.server_revision));
			item.info.server_lang   = NetworkRecv_byte(&_udp_cs, p);
			item.info.use_password  = NetworkRecv_byte(&_udp_cs, p);
			item.info.clients_max   = NetworkRecv_byte(&_udp_cs, p);
			item.info.clients_on    = NetworkRecv_byte(&_udp_cs, p);
			item.info.spectators_on = NetworkRecv_byte(&_udp_cs, p);
			item.info.game_date     = NetworkRecv_int(&_udp_cs, p);
			item.info.start_date    = NetworkRecv_int(&_udp_cs, p);
			NetworkRecv_string(&_udp_cs, p, item.info.map_name, sizeof(item.info.map_name));
			item.info.map_width     = NetworkRecv_int(&_udp_cs, p);
			item.info.map_height    = NetworkRecv_int(&_udp_cs, p);
			item.info.map_set       = NetworkRecv_byte(&_udp_cs, p);
			item.info.dedicated     = NetworkRecv_byte(&_udp_cs, p);

			str_validate(item.info.server_name);
			str_validate(item.info.server_revision);
			str_validate(item.info.map_name);
			if (item.info.server_lang >= NETWORK_NUM_LANGUAGES) item.info.server_lang = 0;
			if (item.info.map_set >= NUM_LANDSCAPE ) item.info.map_set = 0;

			if (item.info.hostname[0] == '\0')
				snprintf(item.info.hostname, sizeof(item.info.hostname), "%s", inet_ntoa(client_addr.sin_addr));
		}

		item.online = true;

		NetGui.UpdateNetworkGameWindow(false);
	}

	static void NetworkPacketReceive_PACKET_UDP_CLIENT_DETAIL_INFO_command(Packet p, InetAddress client_addr)
	//DEF_UDP_RECEIVE_COMMAND(PACKET_UDP_CLIENT_DETAIL_INFO)
	{
		NetworkClientState cs;
		NetworkClientInfo ci;
		Packet packet;
		//Player player;
		int [] active = {0};
		byte current = 0;
		int i;

		// Just a fail-safe.. should never happen
		if (!_network_udp_server)
			return;

		packet = NetworkSend_Init(PacketType.UDP_SERVER_DETAIL_INFO);

		/*FOR_ALL_PLAYERS(player) {
			if (player.is_active)
				active++;
		}*/

		Player.forEach( pl -> { if (pl.isActive()) active[0]++; } );

		/* Send the amount of active companies */
		NetworkSend_byte (packet, NETWORK_COMPANY_INFO_VERSION);
		NetworkSend_byte (packet, active);

		/* Fetch the latest version of everything */
		NetworkPopulateCompanyInfo();

		/* Go through all the players */
		FOR_ALL_PLAYERS(player) {
			/* Skip non-active players */
			if (!player.is_active)
				continue;

			current++;

			/* Send the information */
			NetworkSend_byte (packet, current);

			NetworkSend_string(packet, _network_player_info[player.index].company_name);
			NetworkSend_byte (packet, _network_player_info[player.index].inaugurated_year);
			NetworkSend_int64(packet, _network_player_info[player.index].company_value);
			NetworkSend_int64(packet, _network_player_info[player.index].money);
			NetworkSend_int64(packet, _network_player_info[player.index].income);
			NetworkSend_int(packet, _network_player_info[player.index].performance);

			/* Send 1 if there is a passord for the company else send 0 */
			if (_network_player_info[player.index].password[0] != '\0') {
				NetworkSend_byte (packet, 1);
			} else {
				NetworkSend_byte (packet, 0);
			}

			for (i = 0; i < NETWORK_VEHICLE_TYPES; i++)
				NetworkSend_int(packet, _network_player_info[player.index].num_vehicle[i]);

			for (i = 0; i < NETWORK_STATION_TYPES; i++)
				NetworkSend_int(packet, _network_player_info[player.index].num_station[i]);

			/* Find the clients that are connected to this player */
			FOR_ALL_CLIENTS(cs) {
				ci = DEREF_CLIENT_INFO(cs);
				if ((ci.client_playas - 1) == player.index) {
					/* The byte == 1 indicates that a client is following */
					NetworkSend_byte(packet, 1);
					NetworkSend_string(packet, ci.client_name);
					NetworkSend_string(packet, ci.unique_id);
					NetworkSend_int(packet, ci.join_date);
				}
			}
			/* Also check for the server itself */
			ci = NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX);
			if ((ci.client_playas - 1) == player.index) {
				/* The byte == 1 indicates that a client is following */
				NetworkSend_byte(packet, 1);
				NetworkSend_string(packet, ci.client_name);
				NetworkSend_string(packet, ci.unique_id);
				NetworkSend_int(packet, ci.join_date);
			}

			/* Indicates end of client list */
			NetworkSend_byte(packet, 0);
		}

		/* And check if we have any spectators */
		FOR_ALL_CLIENTS(cs) {
			ci = DEREF_CLIENT_INFO(cs);
			if ((ci.client_playas - 1) > Global.MAX_PLAYERS) {
				/* The byte == 1 indicates that a client is following */
				NetworkSend_byte(packet, 1);
				NetworkSend_string(packet, ci.client_name);
				NetworkSend_string(packet, ci.unique_id);
				NetworkSend_int(packet, ci.join_date);
			}
		}
		/* Also check for the server itself */
		ci = NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX);
		if ((ci.client_playas - 1) > Global.MAX_PLAYERS) {
			/* The byte == 1 indicates that a client is following */
			NetworkSend_byte(packet, 1);
			NetworkSend_string(packet, ci.client_name);
			NetworkSend_string(packet, ci.unique_id);
			NetworkSend_int(packet, ci.join_date);
		}

		/* Indicates end of client list */
		NetworkSend_byte(packet, 0);

		NetworkSendUDP_Packet(_udp_server_socket, packet, client_addr);

		free(packet);
	}

	static void NetworkPacketReceive_PACKET_UDP_MASTER_RESPONSE_LIST_command(Packet p, InetAddress client_addr)
	//DEF_UDP_RECEIVE_COMMAND(PACKET_UDP_MASTER_RESPONSE_LIST) 
	{
		int i;
		//struct in_addr ip;
		int port;
		byte ver;

		/* packet begins with the protocol version (byte)
		 * then an int which indicates how many
		 * ip:port pairs are in this packet, after that
		 * an int (ip) and an int (port) for each pair
		 */

		ver = NetworkRecv_byte(_udp_cs, p);

		if (_udp_cs.quited)
			return;

		if (ver == 1) {
			for (i = NetworkRecv_int(&_udp_cs, p); i != 0 ; i--) {
				ip.s_addr = TO_LE32(NetworkRecv_int(&_udp_cs, p));
				port = NetworkRecv_int(&_udp_cs, p);
				NetworkUDPQueryServer(inet_ntoa(ip), port);
			}
		}
	}

	//DEF_UDP_RECEIVE_COMMAND(PACKET_UDP_MASTER_ACK_REGISTER) 
	static void NetworkPacketReceive_PACKET_UDP_MASTER_ACK_REGISTER_command(Packet p, InetAddress client_addr)
	{
		_network_advertise_retries = 0;
		Global.DEBUG_net( 2,"[NET][UDP] We are advertised on the master-server!");

		if (!_network_advertise)
			/* We are advertised, but we don't want to! */
			NetworkUDPRemoveAdvertise();
	}



	static NetworkUDPPacket _network_udp_packet[] = {
			//RECEIVE_COMMAND(PACKET_UDP_CLIENT_FIND_SERVER),
			NetUDP::NetworkPacketReceive_PACKET_UDP_CLIENT_FIND_SERVER_command,
			//RECEIVE_COMMAND(PACKET_UDP_SERVER_RESPONSE),
			NetUDP::NetworkPacketReceive_PACKET_UDP_SERVER_RESPONSE_command,
			//RECEIVE_COMMAND(PACKET_UDP_CLIENT_DETAIL_INFO),
			NetUDP::NetworkPacketReceive_PACKET_UDP_CLIENT_DETAIL_INFO_command,
			null,
			null,
			//RECEIVE_COMMAND(PACKET_UDP_MASTER_ACK_REGISTER),
			NetUDP::NetworkPacketReceive_PACKET_UDP_MASTER_ACK_REGISTER_command,
			null,
			//RECEIVE_COMMAND(PACKET_UDP_MASTER_RESPONSE_LIST),
			NetUDP::NetworkPacketReceive_PACKET_UDP_MASTER_RESPONSE_LIST_command,
			null
	};


	// If this fails, check the array above with network_data.h
	//assert_compile(lengthof(_network_udp_packet) == PACKET_UDP_END);


	void NetworkHandleUDPPacket(Packet p, InetAddress client_addr)
	{
		/* Fake a client, so we can see when there is an illegal packet */
		_udp_cs.socket = null;
		_udp_cs.quited = false;

		int type = p.getType(); //NetworkRecv_byte(_udp_cs, p);

		if (PacketType.isUdpRange(type) && _network_udp_packet[type] != null && !_udp_cs.quited) {
			_network_udp_packet[type].accept(p, client_addr);
		}	else {
			Global.DEBUG_net( 0, "[NET][UDP] Received invalid packet type %d", type);
		}
	}


	// Send a packet over UDP
	static void NetworkSendUDP_Packet(DatagramSocket udp, Packet p, SocketAddress a)
	{
		try {			
			p.sendTo(udp,a);
		} catch (IOException e) {
			//  e.printStackTrace();
			Global.DEBUG_net( 1, "[NET][UDP] Send error: %s", e );
		}
		/*
		int res;

		// Put the length in the buffer
		p.buffer[0] = p.size & 0xFF;
		p.buffer[1] = p.size >> 8;

		// Send the buffer
		res = sendto(udp, p.buffer, p.size, 0, recv, sizeof(recv));

		// Check for any errors, but ignore it for the rest
		if (res == -1) {
			Global.DEBUG_net( 1, "[NET][UDP] Send error: %i", GET_LAST_ERROR());
		} */
	}

	// Start UDP listener
	static boolean NetworkUDPListen(DatagramSocket [] udp, int host, int port, boolean broadcast)
	{
		InetAddress sin;

		// Make sure socket is closed
		if( null != udp[0]) udp[0].close();		
		
		//udp = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
		udp[0] = new DatagramSocket(Net.NETWORK_DEFAULT_PORT);

		if (udp[0] == null) {
			Global.DEBUG_net( 1, "[NET][UDP] Failed to start UDP support");
			return false;
		}
		

		// set nonblocking mode for socket
		{
			long blocking = 1;
			setsockopt(udp, SOL_SOCKET, SO_NONBLOCK, blocking, null);
		}

		sin.sin_family = AF_INET;
		// Listen on all IPs
		sin.sin_addr.s_addr = host;
		sin.sin_port = htons(port);

		if (bind(udp, sin, sizeof(sin)) != 0) {
			Global.DEBUG_net( 1, "[NET][UDP] error: bind failed on %s:%d", host, port);
			return false;
		}

		if (broadcast)
			udp[0].setBroadcast(true);

		Global.DEBUG_net( 1, "[NET][UDP] Listening on port %s:%d", host, port);

		return true;
	}

	// Close UDP connection
	void NetworkUDPClose()
	{
		Global.DEBUG_net( 1, "[NET][UDP] Closed listeners");

		if (_network_udp_server) {
			if (_udp_server_socket != null) {
				closesocket(_udp_server_socket);
				_udp_server_socket = null;
			}

			if (_udp_master_socket != null) {
				closesocket(_udp_master_socket);
				_udp_master_socket = null;
			}

			_network_udp_server = false;
			_network_udp_broadcast = 0;
		} else {
			if (_udp_client_socket != null) {
				closesocket(_udp_client_socket);
				_udp_client_socket = null;
			}
			_network_udp_broadcast = 0;
		}
	}

	// Receive something on UDP level
	void NetworkUDPReceive(Socket udp)
	{
		InetAddress client_addr;
		//socklen_t client_len;
		int nbytes;
		//static Packet p = null;
		int packet_len;

		// If p is null, malloc him.. this prevents unneeded mallocs
		//if (p == null)			p = new Packet();

		//packet_len = sizeof(p.buffer);
		client_len = sizeof(client_addr);

		byte [] pbuffer = new byte[2048];
		
		// Try to receive anything
		nbytes = recvfrom(udp, pbuffer, pbuffer.length, 0, client_addr, client_len);

		// We got some bytes.. just asume we receive the whole packet
		if (nbytes > 0) {
			/*/ Get the size of the buffer
			p.size = (int)p.buffer[0];
			p.size += (int)p.buffer[1] << 8;
			// Put the position on the right place
			p.pos = 2;
			p.next = null; */
			Packet p = new Packet(pbuffer);

			// Handle the packet
			NetworkHandleUDPPacket(p, client_addr);

			// Free the packet
			//free(p);
			//p = null;
		}
	}

	// Broadcast to all ips
	void NetworkUDPBroadCast(DatagramSocket udp) throws SocketException
	{
		InetAddress out_addr;
		Packet p;

		// Init the packet
		p = NetworkSend_Init(PacketType.UDP_CLIENT_FIND_SERVER);

		// Go through all the ips on this pc
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) 
		{
			NetworkInterface networkInterface = interfaces.nextElement();
			if (networkInterface.isLoopback())
				continue;    // Do not want to use the loopback interface.
			for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) 
			{
				InetAddress broadcast = interfaceAddress.getBroadcast();
				if (broadcast == null)
					continue;

				Global.DEBUG_net( 6, "[NET][UDP] Broadcasting to %s", broadcast);
				
				NetworkSendUDP_Packet(udp, p, broadcast);

			}
		}
	}


	// Request the the server-list from the master server
	static void NetworkUDPQueryMasterServer()
	{
		if (_udp_client_socket[0] == null)
		{
			if (!NetworkUDPListen(_udp_client_socket, 0, 0, true))
				return;
		}

		Packet p = new Packet(PacketType.UDP_CLIENT_GET_LIST);

		InetAddress ha = NetworkResolveHost(NETWORK_MASTER_SERVER_HOST);
		SocketAddress out_addr = new InetSocketAddress(ha, NETWORK_MASTER_SERVER_PORT);
		

		// packet only contains protocol version
		p.append((byte)Net.NETWORK_MASTER_SERVER_VERSION);

		NetworkSendUDP_Packet(_udp_client_socket[0], p, out_addr);

		Global.DEBUG_net( 2, "[NET][UDP] Queried Master Server at %s:%d", inet_ntoa(out_addr.sin_addr),ntohs(out_addr.sin_port));
	}

	// Find all servers
	static void NetworkUDPSearchGame()
	{
		// We are still searching..
		if (_network_udp_broadcast > 0)
			return;

		// No UDP-socket yet..
		if (_udp_client_socket == null)
			if (!NetworkUDPListen(_udp_client_socket, 0, 0, true))
				return;

		Global.DEBUG_net( 0, "[NET][UDP] Searching server");

		NetworkUDPBroadCast(_udp_client_socket);
		_network_udp_broadcast = 300; // Stay searching for 300 ticks
	}

	NetworkGameList NetworkUDPQueryServer(final String host, int port)
	{
		InetAddress out_addr;
		Packet p;
		NetworkGameList item;
		String hostname;

		// No UDP-socket yet..
		if (_udp_client_socket == null)
			if (!NetworkUDPListen(_udp_client_socket, 0, 0, true))
				return null;

		ttd_strlcpy(hostname, host, sizeof(hostname));

		out_addr.sin_family = AF_INET;
		out_addr.sin_port = htons(port);
		out_addr.sin_addr.s_addr = NetworkResolveHost(host);

		// Clear item in gamelist
		item = NetworkGameListAddItem(inet_addr(inet_ntoa(out_addr.sin_addr)), ntohs(out_addr.sin_port));
		memset(&item.info, 0, sizeof(item.info));
		snprintf(item.info.server_name, sizeof(item.info.server_name), "%s", hostname);
		snprintf(item.info.hostname, sizeof(item.info.hostname), "%s", hostname);
		item.online = false;

		// Init the packet
		p = NetworkSend_Init(PACKET_UDP_CLIENT_FIND_SERVER);

		NetworkSendUDP_Packet(_udp_client_socket, p, &out_addr);

		free(p);

		UpdateNetworkGameWindow(false);
		return item;
	}

	/* Remove our advertise from the master-server */
	static void NetworkUDPRemoveAdvertise()
	{
		InetAddress out_addr;
		Packet p;

		/* Check if we are advertising */
		if (!_networking || !_network_server || !_network_udp_server)
			return;

		/* check for socket */
		if (_udp_master_socket == null)
			if (!NetworkUDPListen(&_udp_master_socket, _network_server_bind_ip, 0, false))
				return;

		Global.DEBUG_net( 2, "[NET][UDP] Removing advertise..");

		/* Find somewhere to send */
		out_addr.sin_family = AF_INET;
		out_addr.sin_port = htons(NETWORK_MASTER_SERVER_PORT);
		out_addr.sin_addr.s_addr = NetworkResolveHost(NETWORK_MASTER_SERVER_HOST);

		/* Send the packet */
		p = NetworkSend_Init(PACKET_UDP_SERVER_UNREGISTER);
		/* Packet is: Version, server_port */
		NetworkSend_byte(p, NETWORK_MASTER_SERVER_VERSION);
		NetworkSend_int(p, _network_server_port);
		NetworkSendUDP_Packet(_udp_master_socket, p, &out_addr);

		free(p);
	}

	/* Register us to the master server
	     This function checks if it needs to send an advertise */
	void NetworkUDPAdvertise()
	{
		InetAddress out_addr;
		Packet p;

		/* Check if we should send an advertise */
		if (!_networking || !_network_server || !_network_udp_server || !_network_advertise)
			return;

		/* check for socket */
		if (_udp_master_socket == null)
			if (!NetworkUDPListen(&_udp_master_socket, _network_server_bind_ip, 0, false))
				return;

		/* Only send once in the 450 game-days (about 15 minutes) */
		if (_network_advertise_retries == 0) {
			if ( (_network_last_advertise_date + ADVERTISE_NORMAL_INTERVAL) > _date)
				return;
			_network_advertise_retries = ADVERTISE_RETRY_TIMES;
		}

		if ( (_network_last_advertise_date + ADVERTISE_RETRY_INTERVAL) > _date)
			return;

		_network_advertise_retries--;
		_network_last_advertise_date = _date;

		/* Find somewhere to send */
		out_addr.sin_family = AF_INET;
		out_addr.sin_port = htons(NETWORK_MASTER_SERVER_PORT);
		out_addr.sin_addr.s_addr = NetworkResolveHost(NETWORK_MASTER_SERVER_HOST);

		Global.DEBUG_net( 1,"[NET][UDP] Advertising to master server");

		/* Send the packet */
		p = NetworkSend_Init(PACKET_UDP_SERVER_REGISTER);
		/* Packet is: WELCOME_MESSAGE, Version, server_port */
		NetworkSend_string(p, NETWORK_MASTER_SERVER_WELCOME_MESSAGE);
		NetworkSend_byte(p, NETWORK_MASTER_SERVER_VERSION);
		NetworkSend_int(p, _network_server_port);
		NetworkSendUDP_Packet(_udp_master_socket, p, &out_addr);

		free(p);
	}

	void NetworkUDPInitialize()
	{
		_udp_client_socket = null;
		_udp_server_socket = null;
		_udp_master_socket = null;

		_network_udp_server = false;
		_network_udp_broadcast = 0;
	}


}


// The layout for the receive-functions by UDP
//typedef void NetworkUDPPacket(Packet p, InetAddress client_addr);
@FunctionalInterface
interface NetworkUDPPacket
{
	void accept(Packet p, InetAddress client_addr);
}
