package com.dzavalishin.net;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;
import java.util.Iterator;

import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Player;

public class NetUDP extends Net 
{
	// TODO convert to Packet methods
	public static void NetworkSend_byte(Packet p, byte b) {
		p.append(b);	
	}

	public static void NetworkSend_int(Packet p, int i) {
		p.appendInt(i);		
	}

	public static void NetworkSend_int64(Packet p, long l) {
		p.appendLong(l);		
	}

	public static void NetworkSend_string(Packet p, String s) throws IOException {
		//p.appendInt(s.length());
		p.append(s);

	}


	public static byte NetworkRecv_byte(NetworkClientState my_CLIENT, Packet p) {
		return p.nextByte();
	}

	public static int NetworkRecv_int(NetworkClientState my_CLIENT, Packet p) {
		return p.nextInt();
	}

	public static long NetworkRecv_int64(NetworkClientState my_CLIENT, Packet p) {
		return p.nextLong();
	}

	public static String NetworkRecv_string(NetworkClientState cs, Packet p) {
		return p.nextString();
	}

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

	static void NetworkPacketReceive_PACKET_UDP_CLIENT_FIND_SERVER_command(Packet p, SocketAddress client_addr) throws IOException
	//DEF_UDP_RECEIVE_COMMAND(PACKET_UDP_CLIENT_FIND_SERVER)
	{
		Packet packet;
		// Just a fail-safe.. should never happen
		if (!_network_udp_server)
			return;

		packet = new Packet(PacketType.UDP_SERVER_RESPONSE);

		// Update some game_info
		_network_game_info.game_date = Global.get_date();
		_network_game_info.map_width = Global.MapSizeX();
		_network_game_info.map_height = Global.MapSizeY();
		_network_game_info.map_set = GameOptions._opt.landscape;

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

		packet.encodeObject(_network_game_info);

		// Let the client know that we are here
		NetworkSendUDP_Packet(_udp_server_socket[0], packet, client_addr);

		Global.DEBUG_net( 2, "[NET][UDP] Queried from %s", client_addr);
	}

	static void NetworkPacketReceive_PACKET_UDP_SERVER_RESPONSE_command(Packet p, SocketAddress client_addr) throws ClassNotFoundException, IOException
	//DEF_UDP_RECEIVE_COMMAND(PACKET_UDP_SERVER_RESPONSE)
	{
		// Just a fail-safe.. should never happen
		if (_network_udp_server)
			return;

		//byte game_info_version = NetworkRecv_byte(_udp_cs, p);

		if (_udp_cs.quited)
			return;

		//Global.DEBUG_net( 6, "[NET][UDP] Server response from %s:%d", inet_ntoa(client_addr.sin_addr),ntohs(client_addr.sin_port));
		Global.DEBUG_net( 6, "[NET][UDP] Server response from %s", client_addr);

		// Find next item
		NetworkGameList item = NetworkGameList.addItem(client_addr);

		item.info = (NetworkGameInfo) p.decodeObject();
		
		/*
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
		*/
		item.online = true;

		NetGui.UpdateNetworkGameWindow(false);
	}

	static void NetworkPacketReceive_PACKET_UDP_CLIENT_DETAIL_INFO_command(Packet p, SocketAddress client_addr) throws IOException
	//DEF_UDP_RECEIVE_COMMAND(PACKET_UDP_CLIENT_DETAIL_INFO)
	{
		//NetworkClientState cs;
		NetworkClientInfo ci;
		//Player player;
		int [] active = {0};
		byte current = 0;
		int i;

		// Just a fail-safe.. should never happen
		if (!_network_udp_server)
			return;

		Packet packet = new Packet(PacketType.UDP_SERVER_DETAIL_INFO);

		/*FOR_ALL_PLAYERS(player) {
			if (player.is_active)
				active++;
		}*/

		Player.forEach( pl -> { if (pl.isActive()) active[0]++; } );

		// Send the amount of active companies
		NetworkSend_byte (packet, (byte) NETWORK_COMPANY_INFO_VERSION);
		NetworkSend_byte (packet, (byte) active[0]);

		// Fetch the latest version of everything 
		NetServer.NetworkPopulateCompanyInfo();

		// Go through all the players 
		//FOR_ALL_PLAYERS(player) 
		Iterator<Player> ii = Player.getIterator();
		while(ii.hasNext())
		{
			Player player = ii.next();
			
			// Skip non-active players 
			if (!player.isActive())
				continue;

			current++;

			int pindex = player.getIndex().id;
			
			// Send the information
			NetworkSend_byte (packet, current);

			NetworkSend_string(packet, _network_player_info[pindex].company_name);
			NetworkSend_byte (packet, (byte) _network_player_info[pindex].inaugurated_year);
			NetworkSend_int64(packet, _network_player_info[pindex].company_value);
			NetworkSend_int64(packet, _network_player_info[pindex].money);
			NetworkSend_int64(packet, _network_player_info[pindex].income);
			NetworkSend_int(packet, _network_player_info[pindex].performance);

			// Send 1 if there is a passord for the company else send 0
			if (!_network_player_info[pindex].password.isBlank()) {
				NetworkSend_byte (packet, (byte) 1);
			} else {
				NetworkSend_byte (packet, (byte) 0);
			}

			for (i = 0; i < NETWORK_VEHICLE_TYPES; i++)
				NetworkSend_int(packet, _network_player_info[pindex].num_vehicle[i]);

			for (i = 0; i < NETWORK_STATION_TYPES; i++)
				NetworkSend_int(packet, _network_player_info[pindex].num_station[i]);

			// Find the clients that are connected to this player
			//FOR_ALL_CLIENTS(cs) 
			for( NetworkClientState cs : _clients)
			{
				ci = cs.ci; // DEREF_CLIENT_INFO(cs);
				if ((ci.client_playas - 1) == pindex) {
					// The byte == 1 indicates that a client is following
					NetworkSend_byte(packet, (byte) 1);
					NetworkSend_string(packet, ci.client_name);
					NetworkSend_string(packet, ci.unique_id);
					NetworkSend_int(packet, ci.join_date);
				}
			}
			// Also check for the server itself
			ci = NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX);
			if ((ci.client_playas - 1) == pindex) {
				// The byte == 1 indicates that a client is following
				NetworkSend_byte(packet, (byte) 1);
				NetworkSend_string(packet, ci.client_name);
				NetworkSend_string(packet, ci.unique_id);
				NetworkSend_int(packet, ci.join_date);
			}

			// Indicates end of client list
			NetworkSend_byte(packet, (byte) 0);
		}

		// And check if we have any spectators
		//FOR_ALL_CLIENTS(cs) 
		for( NetworkClientState cs : _clients)
		{
			ci = cs.ci; // DEREF_CLIENT_INFO(cs);
			if ((ci.client_playas - 1) > Global.MAX_PLAYERS) {
				// The byte == 1 indicates that a client is following
				NetworkSend_byte(packet, (byte) 1);
				NetworkSend_string(packet, ci.client_name);
				NetworkSend_string(packet, ci.unique_id);
				NetworkSend_int(packet, ci.join_date);
			}
		}
		// Also check for the server itself
		ci = NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX);
		if ((ci.client_playas - 1) > Global.MAX_PLAYERS) {
			// The byte == 1 indicates that a client is following
			NetworkSend_byte(packet, (byte) 1);
			NetworkSend_string(packet, ci.client_name);
			NetworkSend_string(packet, ci.unique_id);
			NetworkSend_int(packet, ci.join_date);
		}

		// Indicates end of client list
		NetworkSend_byte(packet, (byte) 0);

		NetworkSendUDP_Packet(_udp_server_socket[0], packet, client_addr);
	}

	static void NetworkPacketReceive_PACKET_UDP_MASTER_RESPONSE_LIST_command(Packet p, SocketAddress client_addr) throws UnknownHostException
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
			for (i = NetworkRecv_int(_udp_cs, p); i != 0 ; i--) {
				int ip = NetworkRecv_int(_udp_cs, p);
				port = NetworkRecv_int(_udp_cs, p);
				byte[] bytes = BigInteger.valueOf(ip).toByteArray();
				InetAddress ia = InetAddress.getByAddress(bytes);
				String host = ia.getHostAddress();
				NetworkUDPQueryServer(host, port);
			}
		}

	}

	//DEF_UDP_RECEIVE_COMMAND(PACKET_UDP_MASTER_ACK_REGISTER) 
	static void NetworkPacketReceive_PACKET_UDP_MASTER_ACK_REGISTER_command(Packet p, SocketAddress client_addr)
	{
		_network_advertise_retries = 0;
		Global.DEBUG_net( 2,"[NET][UDP] We are advertised on the master-server!");

		if (!_network_advertise)
			// We are advertised, but we don't want to!
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


	static void NetworkHandleUDPPacket(Packet p, SocketAddress client_addr)
	{
		// Fake a client, so we can see when there is an illegal packet
		_udp_cs.socket = null;
		_udp_cs.quited = false;

		int type = p.getType(); //NetworkRecv_byte(_udp_cs, p);

		if (PacketType.isUdpRange(type) && _network_udp_packet[type] != null && !_udp_cs.quited) {
			try {
				_network_udp_packet[type].accept(p, client_addr);
			} catch (IOException | ClassNotFoundException e) {
				//e.printStackTrace();
				Global.DEBUG_net( 0, "[NET][UDP] Failed to process packet type %d: %s", type, e);
			}
		}	else {
			Global.DEBUG_net( 0, "[NET][UDP] Received invalid packet type %d", type);
		}
	}


	// Send a packet over UDP
	static void NetworkSendUDP_Packet(DatagramChannel _udp_client_socket, Packet p, SocketAddress a)
	{
		try {			
			p.sendTo(_udp_client_socket,a);
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
	//static void NetworkUDPListen(DatagramChannel [] udp, InetAddress host, int port, boolean broadcast) throws IOException
	static void NetworkUDPListen(DatagramChannel [] udp, SocketAddress sa, boolean broadcast) throws IOException
	{
		//InetAddress sin;

		// Make sure socket is closed
		if( null != udp[0]) udp[0].close();		
		
		//udp = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
		//udp[0] = new DatagramSocket(Net.NETWORK_DEFAULT_PORT);
		udp[0] = DatagramChannel.open();

		/*if (udp[0] == null) {
			Global.DEBUG_net( 1, "[NET][UDP] Failed to start UDP support");
			return false;
		}*/
		
		// set nonblocking mode for socket
		udp[0].configureBlocking(false);

		//SocketAddress sa = new InetSocketAddress(host, port);
		if( sa != null ) udp[0].bind(sa);

		/*if (bind(udp, sin, sizeof(sin)) != 0) {
			Global.DEBUG_net( 1, "[NET][UDP] error: bind failed on %s:%d", host, port);
			return false;
		}*/

		if (broadcast)
			udp[0].setOption(StandardSocketOptions.SO_BROADCAST, true);
			//udp[0].setBroadcast(true);

		Global.DEBUG_net( 1, "[NET][UDP] Listening on %s", sa );

		//return true;
	}

	
	// Close UDP connection
	public static void NetworkUDPClose()
	{
		try {
			doNetworkUDPClose();
		} catch (IOException e) {
			// e.printStackTrace();
			Global.error("NetworkUDPClose: %s", e);
		}

	}
	// Close UDP connection
	public static void doNetworkUDPClose() throws IOException
	{
		Global.DEBUG_net( 1, "[NET][UDP] Closed listeners");

		if (_network_udp_server) {
			if (_udp_server_socket[0] != null) {
				_udp_server_socket[0].close();
				_udp_server_socket[0] = null;
			}

			if (_udp_master_socket[0] != null) {
				_udp_master_socket[0].close();
				_udp_master_socket[0] = null;
			}

			_network_udp_server = false;
			_network_udp_broadcast = 0;
		} else {
			if (_udp_client_socket[0] != null) {
				_udp_client_socket[0].close();
				_udp_client_socket[0] = null;
			}
			_network_udp_broadcast = 0;
		}
	}

	// Receive something on UDP level
	static void NetworkUDPReceive(DatagramChannel udp) throws IOException
	{
		//InetAddress client_addr;
		//socklen_t client_len;
		//int nbytes;
		//static Packet p = null;
		//int packet_len;

		// If p is null, malloc him.. this prevents unneeded mallocs
		//if (p == null)			p = new Packet();

		//packet_len = sizeof(p.buffer);
		//client_len = sizeof(client_addr);

		//byte [] pbuffer = new byte[2048];
		
		// Try to receive anything
		//nbytes = recvfrom(udp, pbuffer, pbuffer.length, 0, client_addr, client_len);
		
		ByteBuffer dst = ByteBuffer.allocate(Packet.SEND_MTU*2);
		SocketAddress client_addr = udp.receive(dst);

		// We got some bytes.. just asume we receive the whole packet
		//if (nbytes > 0)
		if(null != client_addr)
		{
			/*/ Get the size of the buffer
			p.size = (int)p.buffer[0];
			p.size += (int)p.buffer[1] << 8;
			// Put the position on the right place
			p.pos = 2;
			p.next = null; */
			Packet p = new Packet(dst.array());

			// Handle the packet
			NetworkHandleUDPPacket(p, client_addr);

			// Free the packet
			//free(p);
			//p = null;
		}
	}

	// Broadcast to all ips
	static void NetworkUDPBroadCast(DatagramChannel udp) throws SocketException
	{
		// Init the packet
		Packet p = new Packet(PacketType.UDP_CLIENT_FIND_SERVER);

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
				
				SocketAddress sa = new InetSocketAddress(broadcast, NETWORK_DEFAULT_PORT); // TODO port?
				NetworkSendUDP_Packet(udp, p, sa);

			}
		}
	}

	private static void checkRestartListen(DatagramChannel [] udp, SocketAddress sa, boolean broadcast) {
		if (udp[0] == null)
		{
			try {
			NetworkUDPListen(udp, sa, broadcast);
			} catch (Exception e) {
				Global.error(e);
				return;
			}
		}
	}

	
	// Request the the server-list from the master server
	static void NetworkUDPQueryMasterServer()
	{
		checkRestartListen(_udp_client_socket, null, true);

		Packet p = new Packet(PacketType.UDP_CLIENT_GET_LIST);

		try {
			InetAddress ha = NetworkResolveHost(NETWORK_MASTER_SERVER_HOST);
			InetSocketAddress out_addr = new InetSocketAddress(ha, NETWORK_MASTER_SERVER_PORT);	

			// packet only contains protocol version
			p.append((byte) NETWORK_MASTER_SERVER_VERSION);

			NetworkSendUDP_Packet(_udp_client_socket[0], p, out_addr);
		} catch (UnknownHostException e) {
			// e.printStackTrace();
			Global.error("Can't QueryMasterServer(): %s", e);
		}

	}


	// Find all servers
	static void NetworkUDPSearchGame()
	{
		// We are still searching..
		if (_network_udp_broadcast > 0)
			return;

		// No UDP-socket yet..
		/*if (_udp_client_socket == null)
			if (!NetworkUDPListen(_udp_client_socket, null, 0, true))
				return; */

		checkRestartListen(_udp_client_socket, null, true);
		
		Global.DEBUG_net( 0, "[NET][UDP] Searching server");

		try {
			NetworkUDPBroadCast(_udp_client_socket[0]);
		} catch (SocketException e) {
			// e.printStackTrace();
			Global.DEBUG_net( 0, "[NET][UDP] Server search exception %s", e);
		}
		_network_udp_broadcast = 300; // Stay searching for 300 ticks
	}

	static NetworkGameList NetworkUDPQueryServer(final String host, int port)
	{
		Packet p;
		NetworkGameList item;
		String hostname;

		// No UDP-socket yet..
		/*if (_udp_client_socket == null)
			if (!NetworkUDPListen(_udp_client_socket, null, 0, true))
				return null; */
		checkRestartListen(_udp_client_socket, null, true);

		hostname = host;
		InetAddress ha = null;
		try {
			ha = NetworkResolveHost(host);
		} catch (UnknownHostException e) {
			//  e.printStackTrace();
			Global.error("Can't resolve %s: %s", host, e);
			return null;
		}
		SocketAddress out_addr = new InetSocketAddress(ha, port); 
		
		// Clear item in gamelist
		item = NetworkGameList.addItem(ha, port);

		item.info = new NetworkGameInfo();
		item.info.server_name = hostname;
		item.info.hostname = hostname;
		item.online = false;

		// Init the packet
		p = new Packet(PacketType.UDP_CLIENT_FIND_SERVER);

		NetworkSendUDP_Packet(_udp_client_socket[0], p, out_addr);

		NetGui.UpdateNetworkGameWindow(false);
		return item;
	}

	// Remove our advertise from the master-server
	static void NetworkUDPRemoveAdvertise()
	{
		// Check if we are advertising
		if (!Global._networking || !Global._network_server || !_network_udp_server)
			return;

		// check for socket
		/*if (_udp_master_socket == null)
			if (!NetworkUDPListen(_udp_master_socket, _network_server_bind_ip, 0, false))
				return; */

		checkRestartListen(_udp_master_socket, new InetSocketAddress(_network_server_bind_ip, 0), false);

		Global.DEBUG_net( 2, "[NET][UDP] Removing advertise..");

		// Find somewhere to send
		InetAddress ha = null;
		try {
			ha = NetworkResolveHost(NETWORK_MASTER_SERVER_HOST);
		} catch (UnknownHostException e) {
			// e.printStackTrace();
			Global.error("Can't adv to master %s: %s", NETWORK_MASTER_SERVER_HOST, e );
			return;
		}
		SocketAddress out_addr = new InetSocketAddress(ha, NETWORK_MASTER_SERVER_PORT);
		
		// Send the packet
		Packet p = new Packet(PacketType.UDP_SERVER_UNREGISTER);
		// Packet is: Version, server_port
		p.append((byte)NETWORK_MASTER_SERVER_VERSION);
		// TODO content p.appendInt(_network_server_port);
		NetworkSendUDP_Packet(_udp_master_socket[0], p, out_addr);
	}

	/* Register us to the master server
	     This function checks if it needs to send an advertise */
	static void NetworkUDPAdvertise() throws IOException
	{
		// Check if we should send an advertise
		if (!Global._networking || !Global._network_server || !_network_udp_server || !_network_advertise)
			return;

		// check for socket
		/*if (_udp_master_socket == null)
			if (!NetworkUDPListen(_udp_master_socket, _network_server_bind_ip, 0, false))
				return; */
		checkRestartListen(_udp_master_socket, new InetSocketAddress(_network_server_bind_ip, 0), false);

		// Only send once in the 450 game-days (about 15 minutes)
		if (_network_advertise_retries == 0) {
			if ( (_network_last_advertise_date + ADVERTISE_NORMAL_INTERVAL) > Global.get_date())
				return;
			_network_advertise_retries = ADVERTISE_RETRY_TIMES;
		}

		if ( (_network_last_advertise_date + ADVERTISE_RETRY_INTERVAL) > Global.get_date())
			return;

		_network_advertise_retries--;
		_network_last_advertise_date = Global.get_date();

		// Find somewhere to send
		InetAddress ha = null;
		try {
			ha = NetworkResolveHost(NETWORK_MASTER_SERVER_HOST);
		} catch (UnknownHostException e) {
			// e.printStackTrace();
			Global.error("Can't adv to master %s: %s", NETWORK_MASTER_SERVER_HOST, e );
			return;
		}
		SocketAddress out_addr = new InetSocketAddress(ha, NETWORK_MASTER_SERVER_PORT);

		Global.DEBUG_net( 1,"[NET][UDP] Advertising to master server");

		// Send the packet
		Packet p = new Packet(PacketType.UDP_SERVER_REGISTER);
		// TODO content Packet is: WELCOME_MESSAGE, Version, server_port
		NetworkSend_string(p, NETWORK_MASTER_SERVER_WELCOME_MESSAGE);
		NetworkSend_byte(p, (byte) NETWORK_MASTER_SERVER_VERSION);
		NetworkSend_int(p, _network_server_port); 
		NetworkSendUDP_Packet(_udp_master_socket[0], p, out_addr);
	}

	static void NetworkUDPInitialize()
	{
		_udp_client_socket[0] = null;
		_udp_server_socket[0] = null;
		_udp_master_socket[0] = null;

		_network_udp_server = false;
		_network_udp_broadcast = 0;
	}


}


// The layout for the receive-functions by UDP
//typedef void NetworkUDPPacket(Packet p, InetAddress client_addr);
@FunctionalInterface
interface NetworkUDPPacket
{
	void accept(Packet p, SocketAddress client_addr)  throws IOException, ClassNotFoundException;
}
