package com.dzavalishin.net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Main;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.SaveLoad;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.console.ConsoleFactory;
import com.dzavalishin.console.DefaultConsole;
import com.dzavalishin.enums.SaveOrLoadResult;
import com.dzavalishin.enums.SwitchModes;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Strings;

public interface NetServer extends NetTools, NetDefs
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



	// Is the network enabled?

	// **********
	// Sending functions
	//   void NetworkPacketSend_ ## type ## _command(NetworkClientState cs) has parameter: NetworkClientState cs
	// **********

	static void NetworkPacketSend_PACKET_SERVER_CLIENT_INFO_command(NetworkClientState cs, NetworkClientInfo ci) throws IOException
	{
		//
		// Packet: SERVER_CLIENT_INFO
		// Function: Sends info about a client
		// Data:
		//    int:  The index of the client (always unique on a server. 1 = server)
		//    byte:  As which player the client is playing
		//    String: The name of the client
		//    String: The unique id of the client
		//

		Packet p;

		if (ci.client_index != NETWORK_EMPTY_INDEX) {
			p = new Packet(PacketType.SERVER_CLIENT_INFO);
			NetworkSend_int(p, ci.client_index);
			NetworkSend_byte (p, (byte) ci.client_playas);
			NetworkSend_string(p, ci.client_name);
			NetworkSend_string(p, ci.unique_id);

			Net.NetworkSend_Packet(p, cs);
		}
	}

	static void NetworkPacketSend_PACKET_SERVER_COMPANY_INFO_command(NetworkClientState cs) throws IOException
	{
		//
		// Packet: SERVER_COMPANY_INFO
		// Function: Sends info about the companies
		// Data:
		//

		//int i;

		int [] active = {0};

		/*FOR_ALL_PLAYERS(player) {
			if (player.is_active)
				active++;
		}*/

		Player.forEach( pl -> { if (pl.isActive()) active[0]++; } );

		if (active[0] == 0) {
			Packet p = new Packet(PacketType.SERVER_COMPANY_INFO);

			NetworkSend_byte (p, (byte) NETWORK_COMPANY_INFO_VERSION);
			NetworkSend_byte (p, (byte)active[0]);

			Net.NetworkSend_Packet(p, cs);
			return;
		}

		NetworkPopulateCompanyInfo();

		//Player.forEach(player -> 
		Iterator<Player> ii = Player.getIterator();
		//Player.forEach(p -> 
		while(ii.hasNext())
		{
			Player player = ii.next();
			
			if (!player.isActive())
				continue;

			Packet p = new Packet(PacketType.SERVER_COMPANY_INFO);

			NetworkSend_byte (p, (byte) NETWORK_COMPANY_INFO_VERSION);
			NetworkSend_byte (p, (byte) active[0]);
			NetworkSend_byte (p, (byte) player.getIndex().id);

			int ix = player.getIndex().id;

			NetworkSend_string(p, Net._network_player_info[ix].company_name);
			NetworkSend_byte (p, (byte) Net._network_player_info[ix].inaugurated_year);
			NetworkSend_int64(p, Net._network_player_info[ix].company_value);
			NetworkSend_int64(p, Net._network_player_info[ix].money);
			NetworkSend_int64(p, Net._network_player_info[ix].income);
			NetworkSend_int(p, Net._network_player_info[ix].performance);

			/* Send 1 if there is a passord for the company else send 0 */
			if (!Net._network_player_info[ix].password.isBlank()) {
				NetworkSend_byte (p, (byte) 1);
			} else {
				NetworkSend_byte (p, (byte) 0);
			}

			for (int i = 0; i < NETWORK_VEHICLE_TYPES; i++)
				NetworkSend_int(p, Net._network_player_info[ix].num_vehicle[i]);

			for (int i = 0; i < NETWORK_STATION_TYPES; i++)
				NetworkSend_int(p, Net._network_player_info[ix].num_station[i]);

			if (Net._network_player_info[ix].players.isBlank())
				NetworkSend_string(p, "<none>");
			else
				NetworkSend_string(p, Net._network_player_info[ix].players);

			Net.NetworkSend_Packet(p, cs);
		}

		Packet p = new Packet(PacketType.SERVER_COMPANY_INFO);

		NetworkSend_byte (p, (byte) NETWORK_COMPANY_INFO_VERSION);
		NetworkSend_byte (p, (byte) 0);

		Net.NetworkSend_Packet(p, cs);
	}


	static void NetworkPacketSend_PACKET_SERVER_ERROR_command(NetworkClientState cs, NetworkErrorCode error)
	{
		//
		// Packet: SERVER_ERROR
		// Function: The client made an error
		// Data:
		//    byte:  ErrorID (see network_data.h, NetworkErrorCode)
		//

		//NetworkClientState new_cs;
		String str;
		String client_name;

		Packet p = new Packet(PacketType.SERVER_ERROR);
		NetworkSend_byte(p, (byte) error.ordinal());
		Net.NetworkSend_Packet(p, cs);

		// Only send when the current client was in game
		if (cs.status.ordinal() > ClientStatus.AUTH.ordinal()) {
			client_name = Net.NetworkGetClientName(cs);

			str = Strings.GetString(Str.STR_NETWORK_ERR_CLIENT_GENERAL + error.ordinal());

			Global.DEBUG_net( 2, "[NET] %s made an error (%s) and his connection is closed", client_name, str);

			Net.NetworkTextMessage(NetworkAction.LEAVE, 1, false, client_name, "%s", str);

			//Net.FOR_ALL_CLIENTS(new_cs -> 
			for( NetworkClientState new_cs : Net._clients )
			{
				if (new_cs.status.ordinal() > ClientStatus.AUTH.ordinal() && new_cs != cs) {
					// Some errors we filter to a more general error. Clients don't have to know the real
					//  reason a joining failed.
					if (error == NetworkErrorCode.NOT_AUTHORIZED || error == NetworkErrorCode.NOT_EXPECTED || error == NetworkErrorCode.WRONG_REVISION)
						error = NetworkErrorCode.ILLEGAL_PACKET;

					NetworkPacketSend_PACKET_SERVER_ERROR_QUIT_command(new_cs, cs.getIndex(), error);
				}
			}
		} else {
			Global.DEBUG_net( 2, "[NET] Clientno %d has made an error and his connection is closed", cs.getIndex());
		}

		cs.quited = true;

		// Make sure the data get's there before we close the connection
		Net.NetworkSend_Packets(cs);

		// The client made a mistake, so drop his connection now!
		Net.NetworkCloseClient(cs);
	}

	static void NetworkPacketSend_PACKET_SERVER_NEED_PASSWORD_command(NetworkClientState cs, NetworkPasswordType type)
	{
		//
		// Packet: SERVER_NEED_PASSWORD
		// Function: Indication to the client that the server needs a password
		// Data:
		//    byte:  Type of password
		//

		Packet p = new Packet(PacketType.SERVER_NEED_PASSWORD);
		NetworkSend_byte(p, (byte) type.ordinal());
		Net.NetworkSend_Packet(p, cs);
	}

	static void NetworkPacketSend_PACKET_SERVER_WELCOME_command(NetworkClientState cs) throws IOException
	{
		//
		// Packet: SERVER_WELCOME
		// Function: The client is joined and ready to receive his map
		// Data:
		//    int:  Own ClientID
		//

		Packet p;
		//NetworkClientState new_cs;

		// Invalid packet when status is AUTH or higher
		if (cs.status.ordinal() >= ClientStatus.AUTH.ordinal())
			return;

		cs.status = ClientStatus.AUTH;
		Net._network_game_info.clients_on++;

		p = new Packet(PacketType.SERVER_WELCOME);
		NetworkSend_int(p, cs.getIndex());
		Net.NetworkSend_Packet(p, cs);

		// Transmit info about all the active clients
		//Net.FOR_ALL_CLIENTS(new_cs -> 
		for( NetworkClientState new_cs : Net._clients )
		{
			if (new_cs != cs && new_cs.status.ordinal() > ClientStatus.AUTH.ordinal())
				NetworkPacketSend_PACKET_SERVER_CLIENT_INFO_command(cs, cs.ci ); //DEREF_CLIENT_INFO(new_cs));
		}
		// Also send the info of the server
		NetworkPacketSend_PACKET_SERVER_CLIENT_INFO_command(cs, Net.NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX));
	}

	static void NetworkPacketSend_PACKET_SERVER_WAIT_command(NetworkClientState cs)
	{
		//
		// Packet: PACKET_SERVER_WAIT
		// Function: The client can not receive the map at the moment because
		//             someone else is already receiving the map
		// Data:
		//    byte:  Clients awaiting map
		//
		int waiting = 0;
		//NetworkClientState new_cs;

		// Count how many players are waiting in the queue
		//FOR_ALL_CLIENTS(new_cs) 
		for( NetworkClientState new_cs : Net._clients )
		{
			if (new_cs.status == ClientStatus.MAP_WAIT)
				waiting++;
		}

		Packet p = new Packet(PacketType.SERVER_WAIT);
		NetworkSend_byte(p, (byte) waiting);
		Net.NetworkSend_Packet(p, cs);
	}


	// This sends the map to the client
	static void NetworkPacketSend_PACKET_SERVER_MAP_command(NetworkClientState cs) throws IOException
	{
		//
		// Packet: SERVER_MAP
		// Function: Sends the map to the client, or a part of it (it is splitted in
		//   a lot of multiple packets)
		// Data:
		//    byte:  packet-type (MapPacket.MAP_PACKET_START, MapPacket.MAP_PACKET_NORMAL and MapPacket.MAP_PACKET_END)
		//  if MapPacket.MapPacket.MAP_PACKET_START:
		//    int: The current FrameCounter
		//  if MapPacket.MAP_PACKET_NORMAL:
		//    piece of the map (till max-size of packet)
		//  if MapPacket.MAP_PACKET_END:
		//    int: seed0 of player
		//    int: seed1 of player
		//      last 2 are repeated Global.MAX_PLAYERS time
		//

		String filename;

		if (cs.status.ordinal() < ClientStatus.AUTH.ordinal()) {
			// Illegal call, return error and ignore the packet
			NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.NOT_AUTHORIZED);
			return;
		}
		if (cs.status == ClientStatus.AUTH) {
			Packet p;

			// Make a dump of the current game
			filename = String.format("%s%snetwork_server.tmp",  Global._path.autosave_dir, File.separator);
			if (SaveLoad.SaveOrLoad(filename, SaveLoad.SL_SAVE) != SaveOrLoadResult.SL_OK) Global.error("network savedump failed");

			//file_pointer = fopen(filename, "rb");
			File f = new File(filename);
			try {
				Net._server_file_pointer = new RandomAccessFile(f, "r");
			} catch (FileNotFoundException e) {
				// e.printStackTrace();
				Global.fail("Send map: %s", e); // Crash TODO - disconnect client
			}
			//fseek(file_pointer, 0, SEEK_END);
			//file_pointer.se

			// Now send the _frame_counter and how many packets are coming
			p = new Packet(PacketType.SERVER_MAP);
			NetworkSend_byte(p, (byte) MapPacket.MAP_PACKET_START.ordinal());
			NetworkSend_int(p, Global._frame_counter);
			NetworkSend_int(p, (int) f.length()); // ftell(file_pointer));
			Net.NetworkSend_Packet(p, cs);

			//fseek(file_pointer, 0, SEEK_SET);
			try {
				Net._server_file_pointer.seek(0);
			} catch (IOException e) {
				// e.printStackTrace();
				Global.fail("Send map: %s", e); // Crash TODO - disconnect client
			}

			Net.server_sent_packets = 4; // We start with trying 4 packets

			cs.status = ClientStatus.MAP;
			/* Mark the start of download */
			cs.last_frame = Global._frame_counter;
			cs.last_frame_server = Global._frame_counter;
		}

		if (cs.status == ClientStatus.MAP) {
			int i;
			//int res;
			//int psize = 0;

			for (i = 0; i < Net.server_sent_packets; i++) {
				Packet p = new Packet(PacketType.SERVER_MAP);
				NetworkSend_byte(p, (byte) MapPacket.MAP_PACKET_NORMAL.ordinal());

				byte [] buffer = new byte[Packet.SEND_MTU];

				//res = fread(p.buffer + p.size, 1, SEND_MTU - p.size, file_pointer);
				int res = -1;
				try {
					res = Net._server_file_pointer.read(buffer);
				} catch (IOException e) {
					// e.printStackTrace();
					Global.fail("Send map: %s", e); // Crash TODO - disconnect client					
				}
				p.append(buffer);

				/* TODO if (ferror(file_pointer)) {
					Global.error("Error reading temporary network savegame!");
				} */

				if( res >= 0 )
				{
					//psize += res;
					Net.NetworkSend_Packet(p, cs);
				}
				else //if (feof(file_pointer))  
				{
					// Done reading!
					// XXX - Delete this when patch-settings are saved in-game
					NetworkSendPatchSettings(cs);
					Packet pe = new Packet(PacketType.SERVER_MAP);
					NetworkSend_byte(pe, (byte) MapPacket.MAP_PACKET_END.ordinal());
					Net.NetworkSend_Packet(pe, cs);

					// Set the status to DONE_MAP, no we will wait for the client
					//  to send it is ready (maybe that happens like never ;))
					cs.status = ClientStatus.DONE_MAP;
					try {
						Net._server_file_pointer.close();
					} catch (IOException e) {
						// e.printStackTrace();
						Global.error(e); // Read - can ignore
					}

					{
						//NetworkClientState new_cs;
						boolean new_map_client = false;
						// Check if there is a client waiting for receiving the map
						//  and start sending him the map
						//FOR_ALL_CLIENTS(new_cs) 
						for( NetworkClientState new_cs : Net._clients )
						{
							if (new_cs.status == ClientStatus.MAP_WAIT) {
								// Check if we already have a new client to send the map to
								if (!new_map_client) {
									// If not, this client will get the map
									new_cs.status = ClientStatus.AUTH;
									new_map_client = true;
									NetworkPacketSend_PACKET_SERVER_MAP_command(new_cs);
								} else {
									// Else, send the other clients how many clients are in front of them
									NetworkPacketSend_PACKET_SERVER_WAIT_command(new_cs);
								}
							}
						}
					}

					// There is no more data, so break the for
					break;
				}
			}

			// Send all packets (forced) and check if we have send it all
			Net.NetworkSend_Packets(cs);
			if (cs.packet_queue.isEmpty()) {
				// All are sent, increase the sent_packets
				Net.server_sent_packets *= 2;
			} else {
				// Not everything is sent, decrease the sent_packets
				if (Net.server_sent_packets > 1) Net.server_sent_packets /= 2;
			}
		}
	}

	static void NetworkPacketSend_PACKET_SERVER_JOIN_command(NetworkClientState cs, int client_index)
	{
		//
		// Packet: SERVER_JOIN
		// Function: A client is joined (all active clients receive this after a
		//     PACKET_CLIENT_MAP_OK) Mostly what directly follows is a
		//     PACKET_SERVER_CLIENT_INFO
		// Data:
		//    int:  Client-Index
		//

		Packet p = new Packet(PacketType.SERVER_JOIN);

		NetworkSend_int(p, client_index);

		Net.NetworkSend_Packet(p, cs);
	}


	static void NetworkPacketSend_PACKET_SERVER_FRAME_command(NetworkClientState cs)
	{
		//
		// Packet: SERVER_FRAME
		// Function: Sends the current frame-counter to the client
		// Data:
		//    int: Frame Counter
		//    int: Frame Counter Max (how far may the client walk before the server?)
		//    [int: general-seed-1]
		//    [int: general-seed-2]
		//      (last two depends on compile-settings, and are not default settings)
		//

		Packet p = new Packet(PacketType.SERVER_FRAME);
		NetworkSend_int(p, Global._frame_counter);
		NetworkSend_int(p, Net._frame_counter_max);
		//#ifdef ENABLE_NETWORK_SYNC_EVERY_FRAME
		NetworkSend_int(p, Net._sync_seed_1);
		//#ifdef NETWORK_SEND_DOUBLE_SEED
		NetworkSend_int(p, Net._sync_seed_2);
		//#endif
		//#endif
		Net.NetworkSend_Packet(p, cs);
	}

	static void NetworkPacketSend_PACKET_SERVER_SYNC_command(NetworkClientState cs)
	{
		//
		// Packet: SERVER_SYNC
		// Function: Sends a sync-check to the client
		// Data:
		//    int: Frame Counter
		//    int: General-seed-1
		//    [int: general-seed-2]
		//      (last one depends on compile-settings, and are not default settings)
		//

		Packet p = new Packet(PacketType.SERVER_SYNC);
		NetworkSend_int(p, Global._frame_counter);
		NetworkSend_int(p, Net._sync_seed_1);

		//#ifdef NETWORK_SEND_DOUBLE_SEED
		NetworkSend_int(p, Net._sync_seed_2);
		//#endif
		Net.NetworkSend_Packet(p, cs);
	}

	static void NetworkPacketSend_PACKET_SERVER_COMMAND_command(NetworkClientState cs, CommandPacket cp) throws IOException
	{
		//
		// Packet: SERVER_COMMAND
		// Function: Sends a DoCommand to the client
		// Data:
		//    byte:  PlayerID (0..Global.MAX_PLAYERS-1)
		//    int: CommandID (see command.h)
		//    int: P1 (free variables used in DoCommand)
		//    int: P2
		//    int: Tile
		//    string: text
		//    byte:  CallBackID (see callback_table.c)
		//    int: Frame of execution
		//

		Packet p = new Packet(PacketType.SERVER_COMMAND);

		NetworkSend_byte(p, (byte) cp.player.id);
		NetworkSend_int(p, cp.cmd);
		NetworkSend_int(p, cp.p1);
		NetworkSend_int(p, cp.p2);
		NetworkSend_int(p, cp.tile.getTile());
		NetworkSend_string(p, cp.text);
		NetworkSend_byte(p, (byte) cp.callback);
		NetworkSend_int(p, cp.frame);

		Net.NetworkSend_Packet(p, cs);
	}

	static void NetworkPacketSend_PACKET_SERVER_CHAT_command(NetworkClientState cs, NetworkAction action, int client_index, boolean self_send, final String msg) throws IOException
	{
		//
		// Packet: SERVER_CHAT
		// Function: Sends a chat-packet to the client
		// Data:
		//    byte:  ActionID (see network_data.h, NetworkAction)
		//    int:  Client-index
		//    String: Message (max MAX_TEXT_MSG_LEN)
		//

		Packet p = new Packet(PacketType.SERVER_CHAT);

		NetworkSend_byte(p, (byte) action.ordinal());
		NetworkSend_int(p, client_index);
		NetworkSend_byte(p, (byte) BitOps.b2i(self_send));
		NetworkSend_string(p, msg);

		Net.NetworkSend_Packet(p, cs);
	}

	
	static void NetworkPacketSend_PACKET_SERVER_ERROR_QUIT_command(NetworkClientState cs, int client_index, int errorno)
	{
		NetworkPacketSend_PACKET_SERVER_ERROR_QUIT_command(cs, client_index, NetworkErrorCode.value(errorno) );		
	}	
	static void NetworkPacketSend_PACKET_SERVER_ERROR_QUIT_command(NetworkClientState cs, int client_index, NetworkErrorCode errorno)
	{
		//
		// Packet: SERVER_ERROR_QUIT
		// Function: One of the clients made an error and is quiting the game
		//      This packet informs the other clients of that.
		// Data:
		//    int:  Client-index
		//    byte:  ErrorID (see network_data.h, NetworkErrorCode)
		//

		Packet p = new Packet(PacketType.SERVER_ERROR_QUIT);

		NetworkSend_int(p, client_index);
		NetworkSend_byte(p, (byte) errorno.ordinal());

		Net.NetworkSend_Packet(p, cs);
	}

	static void NetworkPacketSend_PACKET_SERVER_QUIT_command(NetworkClientState cs, int client_index, final String leavemsg) throws IOException
	{
		//
		// Packet: SERVER_ERROR_QUIT
		// Function: A client left the game, and this packets informs the other clients
		//      of that.
		// Data:
		//    int:  Client-index
		//    String: leave-message
		//

		Packet p = new Packet(PacketType.SERVER_QUIT);

		NetworkSend_int(p, client_index);
		NetworkSend_string(p, leavemsg);

		Net.NetworkSend_Packet(p, cs);
	}

	static void NetworkPacketSend_PACKET_SERVER_SHUTDOWN_command(NetworkClientState cs)
	{
		//
		// Packet: SERVER_SHUTDOWN
		// Function: Let the clients know that the server is closing
		// Data:
		//     <none>
		//

		Packet p = new Packet(PacketType.SERVER_SHUTDOWN);
		Net.NetworkSend_Packet(p, cs);
	}

	static void NetworkPacketSend_PACKET_SERVER_NEWGAME_command(NetworkClientState cs)
	{
		//
		// Packet: PACKET_SERVER_NEWGAME
		// Function: Let the clients know that the server is loading a new map
		// Data:
		//     <none>
		//

		Packet p = new Packet(PacketType.SERVER_NEWGAME);
		Net.NetworkSend_Packet(p, cs);
	}

	static void NetworkPacketSend_PACKET_SERVER_RCON_command(NetworkClientState cs, int color, final String command) throws IOException
	{
		Packet p = new Packet(PacketType.SERVER_RCON);

		NetworkSend_int(p, color);
		NetworkSend_string(p, command);
		Net.NetworkSend_Packet(p, cs);
	}

	// **********
	// Receiving functions
	//   void NetworkPacketReceive_ ## type ## _command(NetworkClientState cs, Packet p) has parameter: NetworkClientState cs, Packet p
	// **********

	static void NetworkPacketReceive_PACKET_CLIENT_COMPANY_INFO_command(NetworkClientState cs, Packet p) throws IOException
	{
		NetworkPacketSend_PACKET_SERVER_COMPANY_INFO_command(cs);
	}

	static void NetworkPacketReceive_PACKET_CLIENT_JOIN_command(NetworkClientState cs, Packet p) throws IOException
	{
		String name;
		String unique_id;
		NetworkClientInfo ci;
		String test_name;
		byte playas;
		//NetworkLanguage client_lang;
		int client_lang;
		String client_revision;


		client_revision = NetworkRecv_string(cs, p);

		//#if defined(WITH_REV) || defined(WITH_REV_HACK)
		// Check if the client has revision control enabled
		if (!NetGui.NOREV_STRING.equals(client_revision)) {
			if (!Net._network_game_info.server_revision.equals(client_revision)) {
				// Different revisions!!
				NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.WRONG_REVISION);

				return;
			}
		}
		//#endif

		name = NetworkRecv_string(cs, p);
		playas = NetworkRecv_byte(cs, p);
		client_lang = NetworkRecv_byte(cs, p);
		unique_id = NetworkRecv_string(cs, p);

		if (cs.quited)
			return;

		// Check if someone else already has that name
		test_name = name;

		if (test_name.isBlank()) {
			// We need a valid name.. make it Player
			test_name = "Player";
		}

		if (!NetworkFindName(test_name)) {
			// We could not create a name for this player
			NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.NAME_IN_USE);
			return;
		}

		ci = cs.ci; // DEREF_CLIENT_INFO(cs);

		ci.client_name = test_name;
		ci.unique_id = unique_id;
		ci.client_playas = playas;
		ci.client_lang = client_lang;

		// We now want a password from the client
		//  else we do not allow him in!
		if (Net._network_game_info.use_password)
			NetworkPacketSend_PACKET_SERVER_NEED_PASSWORD_command(cs, NetworkPasswordType.NETWORK_GAME_PASSWORD);
		else {
			if (ci.client_playas > 0 && ci.client_playas <= Global.MAX_PLAYERS && !Net._network_player_info[ci.client_playas - 1].password.isBlank()) {
				NetworkPacketSend_PACKET_SERVER_NEED_PASSWORD_command(cs, NetworkPasswordType.NETWORK_COMPANY_PASSWORD);
			}
			else {
				NetworkPacketSend_PACKET_SERVER_WELCOME_command(cs);
			}
		}

		/* Make sure companies to who people try to join are not autocleaned */
		if (playas >= 1 && playas <= Global.MAX_PLAYERS)
			Net._network_player_info[playas-1].months_empty = 0;
	}

	static void NetworkPacketReceive_PACKET_CLIENT_PASSWORD_command(NetworkClientState cs, Packet p) throws IOException
	{
		NetworkPasswordType type;
		String password;
		NetworkClientInfo ci;

		type = NetworkPasswordType.value( NetworkRecv_byte(cs, p) );
		password = NetworkRecv_string(cs, p);

		if (cs.status == ClientStatus.INACTIVE && type == NetworkPasswordType.NETWORK_GAME_PASSWORD) {
			// Check game-password
			if (!password.equals(Net._network_game_info.server_password)) {
				// Password is invalid
				NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.WRONG_PASSWORD);
				return;
			}

			ci = cs.ci; //DEREF_CLIENT_INFO(cs);

			if (ci.client_playas <= Global.MAX_PLAYERS && !Net._network_player_info[ci.client_playas - 1].password.isBlank()) {
				NetworkPacketSend_PACKET_SERVER_NEED_PASSWORD_command(cs, NetworkPasswordType.NETWORK_COMPANY_PASSWORD);
				return;
			}

			// Valid password, allow user
			NetworkPacketSend_PACKET_SERVER_WELCOME_command(cs);
			return;
		} else if (cs.status == ClientStatus.INACTIVE && type == NetworkPasswordType.NETWORK_COMPANY_PASSWORD) {
			ci = cs.ci; //DEREF_CLIENT_INFO(cs);

			if (!password.equals(Net._network_player_info[ci.client_playas - 1].password)) {
				// Password is invalid
				NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.WRONG_PASSWORD);
				return;
			}

			NetworkPacketSend_PACKET_SERVER_WELCOME_command(cs);
			return;
		}


		NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.NOT_EXPECTED);
		return;
	}

	static void NetworkPacketReceive_PACKET_CLIENT_GETMAP_command(NetworkClientState cs, Packet p) throws IOException
	{
		//NetworkClientState new_cs;

		// The client was never joined.. so this is impossible, right?
		//  Ignore the packet, give the client a warning, and close his connection
		if (cs.status.ordinal() < ClientStatus.AUTH.ordinal() || cs.quited) {
			NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.NOT_AUTHORIZED);
			return;
		}

		// Check if someone else is receiving the map
		//FOR_ALL_CLIENTS(new_cs) 
		for( NetworkClientState new_cs : Net._clients )
		{
			if (new_cs.status == ClientStatus.MAP) {
				// Tell the new client to wait
				cs.status = ClientStatus.MAP_WAIT;
				NetworkPacketSend_PACKET_SERVER_WAIT_command(cs);
				return;
			}
		}

		// We receive a request to upload the map.. give it to the client!
		NetworkPacketSend_PACKET_SERVER_MAP_command(cs);
	}

	static void NetworkPacketReceive_PACKET_CLIENT_MAP_OK_command(NetworkClientState cs, Packet p) throws IOException
	{
		// Client has the map, now start syncing
		if (cs.status == ClientStatus.DONE_MAP && !cs.quited) {
			;
			//NetworkClientState new_cs;

			String client_name = Net.NetworkGetClientName(cs);

			Net.NetworkTextMessage(NetworkAction.JOIN, 1, false, client_name, "");

			// Mark the client as pre-active, and wait for an ACK
			//  so we know he is done loading and in sync with us
			cs.status = ClientStatus.PRE_ACTIVE;
			NetworkHandleCommandQueue(cs);
			NetworkPacketSend_PACKET_SERVER_FRAME_command(cs);
			NetworkPacketSend_PACKET_SERVER_SYNC_command(cs);

			// This is the frame the client receives
			//  we need it later on to make sure the client is not too slow
			cs.last_frame = Global._frame_counter;
			cs.last_frame_server = Global._frame_counter;

			//Net.FOR_ALL_CLIENTS(new_cs -> 
			for( NetworkClientState new_cs : Net._clients )
			{
				if (new_cs.status.ordinal() > ClientStatus.AUTH.ordinal()) {
					NetworkPacketSend_PACKET_SERVER_CLIENT_INFO_command(new_cs, cs.ci ); // DEREF_CLIENT_INFO(cs));
					NetworkPacketSend_PACKET_SERVER_JOIN_command(new_cs, cs.getIndex());
				}
			}

			if (Net._network_pause_on_join) {
				/* Now pause the game till the client is in sync */
				Cmd.DoCommandP(null, 1, 0, null, Cmd.CMD_PAUSE);

				NetworkServer_HandleChat(NetworkAction.CHAT, DestType.BROADCAST, 0, "Game paused (incoming client)", NETWORK_SERVER_INDEX);
			}
		} else {
			// Wrong status for this packet, give a warning to client, and close connection
			NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.NOT_EXPECTED);
		}
	}

	/** Enforce the command flags.
	 * Eg a server-only command can only be executed by a server, etc.
	 * @param *cp the commandpacket that is going to be checked
	 * @param *ci client information for debugging output to console
	 */
	static boolean CheckCommandFlags(final CommandPacket cp, final NetworkClientInfo ci)
	{
		int flags = Cmd.GetCommandFlags(cp.cmd);

		if ( 0 != (flags & Cmd.CMD_SERVER) && ci.client_index != NETWORK_SERVER_INDEX) {
			DefaultConsole.IConsolePrintF(DefaultConsole._icolour_err, "WARNING: server only command from player %d (IP: %s), kicking...", ci.client_playas, ci.GetPlayerIP());
			return false;
		}

		if(0 != (flags & Cmd.CMD_OFFLINE)) {
			DefaultConsole.IConsolePrintF(DefaultConsole._icolour_err, "WARNING: offline only command from player %d (IP: %s), kicking...", ci.client_playas, ci.GetPlayerIP());
			return false;
		}
		return true;
	}

	/** The client has done a command and wants us to handle it
	 * @param *cs the connected client that has sent the command
	 * @param *p the packet in which the command was sent
	 */
	static void NetworkPacketReceive_PACKET_CLIENT_COMMAND_command(NetworkClientState cs, Packet p )
	{
		//NetworkClientState new_cs;
		final NetworkClientInfo ci;
		byte callback;

		CommandPacket cp = new CommandPacket();

		// The client was never joined.. so this is impossible, right?
		//  Ignore the packet, give the client a warning, and close his connection
		if (cs.status.ordinal() < ClientStatus.DONE_MAP.ordinal() || cs.quited) {
			NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.NOT_EXPECTED);
			return;
		}

		cp.player = PlayerID.get( NetworkRecv_byte(cs, p) );
		cp.cmd    = NetworkRecv_int(cs, p);
		cp.p1     = NetworkRecv_int(cs, p);
		cp.p2     = NetworkRecv_int(cs, p);
		cp.tile   = TileIndex.get( NetworkRecv_int(cs, p) );
		cp.text = NetworkRecv_string(cs, p);

		callback = NetworkRecv_byte(cs, p);

		if (cs.quited) return;

		ci = cs.ci; //DEREF_CLIENT_INFO(cs);

		/* Check if cp.cmd is valid */
		if (!Cmd.IsValidCommand(cp.cmd)) {
			DefaultConsole.IConsolePrintF(DefaultConsole._icolour_err, "WARNING: invalid command from player %d (IP: %s).", ci.client_playas, ci.GetPlayerIP());
			NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.NOT_EXPECTED);
			return;
		}

		if (!CheckCommandFlags(cp, ci)) {
			NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.KICKED);
			return;
		}

		/** Only Cmd.CMD_PLAYER_CTRL is always allowed, for the rest, playas needs
		 * to match the player in the packet. If it doesn't, the client has done
		 * something pretty naughty (or a bug), and will be kicked
		 */
		if (!(cp.cmd == Cmd.CMD_PLAYER_CTRL && cp.p1 == 0) && ci.client_playas - 1 != cp.player.id) {
			DefaultConsole.IConsolePrintF(DefaultConsole._icolour_err, "WARNING: player %d (IP: %s) tried to execute a command as player %d, kicking...",
					ci.client_playas - 1, ci.GetPlayerIP(), cp.player);
			NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.PLAYER_MISMATCH);
			return;
		}

		/** @todo Cmd.CMD_PLAYER_CTRL with p1 = 0 announces a new player to the server. To give the
		 * player the correct ID, the server injects p2 and executes the command. Any other p1
		 * is prohibited. Pretty ugly and should be redone together with its function.
		 * @see CmdPlayerCtrl() players.c:655
		 */
		if (cp.cmd == Cmd.CMD_PLAYER_CTRL) {
			if (cp.p1 != 0) {
				NetworkPacketSend_PACKET_SERVER_ERROR_command(cs, NetworkErrorCode.CHEATER);
				return;
			}

			// XXX [dz] must implement some more robust 
			int csIndex = Net._clients.indexOf(cs);
			assert csIndex >= 0;
			// XXX - UGLY! p2 is mis-used to get the client-id in CmdPlayerCtrl
			cp.p2 = csIndex;//cs - _clients;
		}

		// The frame can be executed in the same frame as the next frame-packet
		//  That frame just before that frame is saved in _frame_counter_max
		cp.frame = Net._frame_counter_max + 1;
		//cp.next  = null;

		// Queue the command for the clients (are send at the end of the frame
		//   if they can handle it ;))
		Net.FOR_ALL_CLIENTS(new_cs -> {
			if (new_cs.status.ordinal() > ClientStatus.AUTH.ordinal()) {
				// Callbacks are only send back to the client who sent them in the
				//  first place. This filters that out.
				cp.callback = (new_cs != cs) ? 0 : callback;
				Net.NetworkAddCommandQueue(new_cs, cp);
			}
		});

		cp.callback = 0;

		Net._local_command_queue.add(cp);

		/*/ Queue the command on the server
		if (_local_command_queue == null) {
			_local_command_queue = cp;
		} else {
			// Find last packet
			CommandPacket c = _local_command_queue;
			while (c.next != null) c = c.next;
			c.next = cp;
		}*/
	}

	static void NetworkPacketReceive_PACKET_CLIENT_ERROR_command(NetworkClientState cs, Packet p)
	{
		// This packets means a client noticed an error and is reporting this
		//  to us. Display the error and report it to the other clients
		//NetworkClientState new_cs;
		byte errorno = NetworkRecv_byte(cs, p);
		String str;
		String client_name;

		// The client was never joined.. thank the client for the packet, but ignore it
		if (cs.status.ordinal() < ClientStatus.DONE_MAP.ordinal() || cs.quited) {
			cs.quited = true;
			return;
		}

		client_name = Net.NetworkGetClientName(cs);

		str = Strings.GetString(Str.STR_NETWORK_ERR_CLIENT_GENERAL + errorno);

		Global.DEBUG_net( 2, "[NET] %s reported an error and is closing his connection (%s)", client_name, str);

		Net.NetworkTextMessage(NetworkAction.LEAVE, 1, false, client_name, "%s", str);

		Net.FOR_ALL_CLIENTS(new_cs -> {
			if (new_cs.status.ordinal() > ClientStatus.AUTH.ordinal()) {
				NetworkPacketSend_PACKET_SERVER_ERROR_QUIT_command(new_cs, cs.getIndex(), errorno);
			}
		});

		cs.quited = true;
	}

	static void NetworkPacketReceive_PACKET_CLIENT_QUIT_command(NetworkClientState cs, Packet p) throws IOException
	{
		// The client wants to leave. Display this and report it to the other
		//  clients.
		//NetworkClientState new_cs;

		// The client was never joined.. thank the client for the packet, but ignore it
		if (cs.status.ordinal() < ClientStatus.DONE_MAP.ordinal() || cs.quited) {
			cs.quited = true;
			return;
		}

		String str = NetworkRecv_string(cs, p);

		String client_name = Net.NetworkGetClientName(cs);

		Net.NetworkTextMessage(NetworkAction.LEAVE, 1, false, client_name, "%s", str);

		//Net.FOR_ALL_CLIENTS(new_cs -> 
		for( NetworkClientState new_cs : Net._clients )
		{
			if (new_cs.status.ordinal() > ClientStatus.AUTH.ordinal()) {
				NetworkPacketSend_PACKET_SERVER_QUIT_command(new_cs, cs.getIndex(), str);
			}
		}

		cs.quited = true;
	}

	static void NetworkPacketReceive_PACKET_CLIENT_ACK_command(NetworkClientState cs, Packet p) throws IOException
	{
		int frame = NetworkRecv_int(cs, p);

		/* The client is trying to catch up with the server */
		if (cs.status == ClientStatus.PRE_ACTIVE) {
			// The client is not yet catched up? 
			if (frame + Global.DAY_TICKS < Global._frame_counter)
				return;

			// Now he is! Unpause the game 
			cs.status = ClientStatus.ACTIVE;

			if (Net._network_pause_on_join) {
				Cmd.DoCommandP(null, 0, 0, null, Cmd.CMD_PAUSE);
				NetworkServer_HandleChat(NetworkAction.CHAT, DestType.BROADCAST, 0, "Game unpaused", NETWORK_SERVER_INDEX);
			}
		} 

		// The client received the frame, make note of it
		cs.last_frame = frame;
		// With those 2 values we can calculate the lag realtime
		cs.last_frame_server = Global._frame_counter;
	}



	static void NetworkServer_HandleChat(NetworkAction action, DestType desttype, int dest, final String msg, int from_index) throws IOException
	{
		NetworkClientInfo ci, ci_own, ci_to;

		switch (desttype) {
		case CLIENT:
			/* Are we sending to the server? */
			if (dest == NETWORK_SERVER_INDEX) {
				ci = Net.NetworkFindClientInfoFromIndex(from_index);
				/* Display the text locally, and that is it */
				if (ci != null)
					Net.NetworkTextMessage(action, Hal.GetDrawStringPlayerColor(ci.client_playas-1), false, ci.client_name, "%s", msg);
			} else {
				/* Else find the client to send the message to */
				//Net.FOR_ALL_CLIENTS(cs -> 
				for( NetworkClientState cs : Net._clients )
				{
					if (cs.getIndex() == dest) {
						NetworkPacketSend_PACKET_SERVER_CHAT_command(cs, action, from_index, false, msg);
						break;
					}
				}
			}

			// Display the message locally (so you know you have sent it)
			if (from_index != dest) {
				if (from_index == NETWORK_SERVER_INDEX) {
					ci = Net.NetworkFindClientInfoFromIndex(from_index);
					ci_to = Net.NetworkFindClientInfoFromIndex(dest);
					if (ci != null && ci_to != null)
						Net.NetworkTextMessage(action, Hal.GetDrawStringPlayerColor(ci.client_playas-1), true, ci_to.client_name, "%s", msg);
				} else {
					//FOR_ALL_CLIENTS(cs) 
					for( NetworkClientState cs : Net._clients )
					{
						if (cs.getIndex() == from_index) {
							NetworkPacketSend_PACKET_SERVER_CHAT_command(cs, action, dest, true, msg);
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
			ci_to = null;
			//FOR_ALL_CLIENTS(cs)
			for( NetworkClientState cs : Net._clients )
			{
				ci = cs.ci; //DEREF_CLIENT_INFO(cs);
				if (ci.client_playas == dest) {
					NetworkPacketSend_PACKET_SERVER_CHAT_command(cs, action, from_index, false, msg);
					if (cs.getIndex() == from_index) {
						show_local = false;
					}
					ci_to = ci; // Remember a client that is in the company for company-name
				}
			}

			ci = Net.NetworkFindClientInfoFromIndex(from_index);
			ci_own = Net.NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX);
			if (ci != null && ci_own != null && ci_own.client_playas == dest) {
				Net.NetworkTextMessage(action, Hal.GetDrawStringPlayerColor(ci.client_playas-1), false, ci.client_name, "%s", msg);
				if (from_index == NETWORK_SERVER_INDEX)
					show_local = false;
				ci_to = ci_own;
			}

			/* There is no such player */
			if (ci_to == null) break;

			// Display the message locally (so you know you have sent it)
			if (ci != null && show_local) {
				if (from_index == NETWORK_SERVER_INDEX) {
					String name = Strings.GetString(Player.GetPlayer(ci_to.client_playas-1).getName_1());
					Net.NetworkTextMessage(action, Hal.GetDrawStringPlayerColor(ci_own.client_playas-1), true, name, "%s", msg);
				} else {
					//FOR_ALL_CLIENTS(cs)
					for( NetworkClientState cs : Net._clients )
					{
						if (cs.getIndex() == from_index) {
							NetworkPacketSend_PACKET_SERVER_CHAT_command(cs, action, ci_to.client_index, true, msg);
						}
					}
				}
			}
		}
		break;
		default:
			Global.DEBUG_net( 0, "[NET][Server] Received unknown destination type %d. Doing broadcast instead.");
			/* fall-through to next case */
		case BROADCAST:
			//Net.FOR_ALL_CLIENTS( (cs) -> 
			for( NetworkClientState cs : Net._clients )
			{
				NetworkPacketSend_PACKET_SERVER_CHAT_command(cs, action, from_index, false, msg);
			}

			ci = Net.NetworkFindClientInfoFromIndex(from_index);
			if (ci != null)
				Net.NetworkTextMessage(action, Hal.GetDrawStringPlayerColor(ci.client_playas-1), false, ci.client_name, "%s", msg);
			break;
		}
	}

	static void NetworkPacketReceive_PACKET_CLIENT_CHAT_command(NetworkClientState cs, Packet p) throws IOException
	{
		NetworkAction action = NetworkAction.value( NetworkRecv_byte(cs, p) );
		DestType desttype = DestType.value( NetworkRecv_byte(cs, p) );
		int dest = NetworkRecv_byte(cs, p);

		String msg = NetworkRecv_string(cs, p);

		NetworkServer_HandleChat(action, desttype, dest, msg, cs.getIndex());
	}

	static void NetworkPacketReceive_PACKET_CLIENT_SET_PASSWORD_command(NetworkClientState cs, Packet p)
	{
		NetworkClientInfo ci;

		String password = NetworkRecv_string(cs, p);
		ci = cs.ci; // DEREF_CLIENT_INFO(cs);

		if (ci.client_playas <= Global.MAX_PLAYERS) {
			Net._network_player_info[ci.client_playas - 1].password = password;
		}
	}

	static void NetworkPacketReceive_PACKET_CLIENT_SET_NAME_command(NetworkClientState cs, Packet p) throws IOException
	{

		NetworkClientInfo ci;

		String client_name = NetworkRecv_string(cs, p);
		ci = cs.ci; // DEREF_CLIENT_INFO(cs);

		if (cs.quited)
			return;

		if (ci != null) {
			// Display change
			if (NetworkFindName(client_name)) {
				Net.NetworkTextMessage(NetworkAction.NAME_CHANGE, 1, false, ci.client_name, "%s", client_name);
				ci.client_name = client_name;
				NetworkUpdateClientInfo(ci.client_index);
			}
		}
	}

	static void NetworkPacketReceive_PACKET_CLIENT_RCON_command(NetworkClientState cs, Packet p)
	{
		if (Net._network_game_info.rcon_password.isBlank())
			return;

		String pass = NetworkRecv_string(cs, p);
		String command = NetworkRecv_string(cs, p);

		if (!pass.equals(Net._network_game_info.rcon_password)) {
			Global.DEBUG_net( 0, "[RCon] Wrong password from client-id %d", cs.getIndex());
			return;
		}

		Global.DEBUG_net( 0, "[RCon] Client-id %d executed: %s", cs.getIndex(), command);

		DefaultConsole._redirect_console_to_client = cs.getIndex();
		//DefaultConsole.IConsoleCmdExec(command);
		ConsoleFactory.INSTANCE.getConsole().IConsoleCmdExec(command);
		DefaultConsole._redirect_console_to_client = 0;
		return;
	}



	// This array matches PacketType. At an incoming
	//  packet it is matches against this array
	//  and that way the right function to handle that
	//  packet is found.
	static NetworkServerPacket  _network_server_packet[] = {
			null, /*PACKET_SERVER_FULL,*/
			null, /*PACKET_SERVER_BANNED,*/
			NetServer::NetworkPacketReceive_PACKET_CLIENT_JOIN_command,
			null, /*PACKET_SERVER_ERROR,*/
			NetServer::NetworkPacketReceive_PACKET_CLIENT_COMPANY_INFO_command,
			null, /*PACKET_SERVER_COMPANY_INFO,*/
			null, /*PACKET_SERVER_CLIENT_INFO,*/
			null, /*PACKET_SERVER_NEED_PASSWORD,*/
			NetServer::NetworkPacketReceive_PACKET_CLIENT_PASSWORD_command,
			null, /*PACKET_SERVER_WELCOME,*/
			NetServer::NetworkPacketReceive_PACKET_CLIENT_GETMAP_command,
			null, /*PACKET_SERVER_WAIT,*/
			null, /*PACKET_SERVER_MAP,*/
			NetServer::NetworkPacketReceive_PACKET_CLIENT_MAP_OK_command,
			null, /*PACKET_SERVER_JOIN,*/
			null, /*PACKET_SERVER_FRAME,*/
			null, /*PACKET_SERVER_SYNC,*/
			NetServer::NetworkPacketReceive_PACKET_CLIENT_ACK_command,
			NetServer::NetworkPacketReceive_PACKET_CLIENT_COMMAND_command,
			null, /*PACKET_SERVER_COMMAND,*/
			NetServer::NetworkPacketReceive_PACKET_CLIENT_CHAT_command,
			null, /*PACKET_SERVER_CHAT,*/
			NetServer::NetworkPacketReceive_PACKET_CLIENT_SET_PASSWORD_command,
			NetServer::NetworkPacketReceive_PACKET_CLIENT_SET_NAME_command,
			NetServer::NetworkPacketReceive_PACKET_CLIENT_QUIT_command,
			NetServer::NetworkPacketReceive_PACKET_CLIENT_ERROR_command,
			null, /*PACKET_SERVER_QUIT,*/
			null, /*PACKET_SERVER_ERROR_QUIT,*/
			null, /*PACKET_SERVER_SHUTDOWN,*/
			null, /*PACKET_SERVER_NEWGAME,*/
			null, /*PACKET_SERVER_RCON,*/
			NetServer::NetworkPacketReceive_PACKET_CLIENT_RCON_command,
	};

	// If this fails, check the array above with network_data.h
	//assert_compile(lengthof(_network_server_packet) == PACKET_END);



	// This is a TEMPORARY solution to get the patch-settings
	//  to the client. When the patch-settings are saved in the savegame
	//  this should be removed!!
	static void NetworkSendPatchSettings(NetworkClientState cs)
	{
		//		final SettingDesc item;
		Packet p = new Packet(PacketType.SERVER_MAP);
		NetworkSend_byte(p, (byte) MapPacket.MAP_PACKET_PATCH.ordinal());
		// Now send all the patch-settings in a pretty order..

		//item = patch_settings;
		/*
		while (item.name != null) {
			switch (item.flags) {
				case SDT_BOOL:
				case SDT_INT8:
				case SDT_UINT8:
					NetworkSend_byte(p, *(byte *)item.ptr);
					break;
				case SDT_INT16:
				case SDT_UINT16:
					NetworkSend_int(p, *(int *)item.ptr);
					break;
				case SDT_INT32:
				case SDT_UINT32:
					NetworkSend_int(p, *(int *)item.ptr);
					break;
			}
			item++;
		}*/

		Net.NetworkSend_Packet(p, cs);
	}

	// This update the company_info-stuff
	public static void NetworkPopulateCompanyInfo()
	{
		//String password;
		//Player p;
		//Vehicle v;
		//Station s;
		//NetworkClientState cs;
		int i;
		int months_empty;

		//FOR_ALL_PLAYERS(p)
		Iterator<Player> ii = Player.getIterator();
		while(ii.hasNext())
		{
			Player p = ii.next();
			int pindex = p.getIndex().id;

			if (!p.isActive()) {
				//memset(&_network_player_info[p.index], 0, sizeof(NetworkPlayerInfo));
				Net._network_player_info[pindex] = new NetworkPlayerInfo(); 
				continue;
			}

			// Clean the info but not the password
			String password = Net._network_player_info[pindex].password;
			months_empty = Net._network_player_info[pindex].months_empty;
			//memset(&_network_player_info[p.index], 0, sizeof(NetworkPlayerInfo));
			Net._network_player_info[pindex] = new NetworkPlayerInfo();
			Net._network_player_info[pindex].months_empty = months_empty;
			Net._network_player_info[pindex].password = password;

			// Grap the company name
			Global.SetDParam(0, p.getName_1());
			Global.SetDParam(1, p.getName_2());
			Net._network_player_info[pindex].company_name = Strings.GetString(Str.STR_JUST_STRING);

			// Check the income
			if (Global.get_cur_year() - 1 == p.getInaugurated_year())
				// The player is here just 1 year, so display [2], else display[1]
				for (i = 0; i < 13; i++)
					Net._network_player_info[pindex].income -= p.getYearly_expenses()[2][i];
			else
				for (i = 0; i < 13; i++)
					Net._network_player_info[pindex].income -= p.getYearly_expenses()[1][i];

			// Set some general stuff
			Net._network_player_info[pindex].inaugurated_year = p.getInaugurated_year();
			Net._network_player_info[pindex].company_value = p.old_economy[0].company_value;
			Net._network_player_info[pindex].money = p.getMoney();
			Net._network_player_info[pindex].performance = p.old_economy[0].performance_history;
		}

		// Go through all vehicles and count the type of vehicles
		//FOR_ALL_VEHICLES(v)
		Iterator<Vehicle> vi = Vehicle.getIterator();
		while(vi.hasNext())
		{
			Vehicle v = vi.next();

			if (v.getOwner().isValid())
			{
				int vowner = v.getOwner().id;
				
				switch (v.getType()) {
				case Vehicle.VEH_Train:
					if (v.IsFrontEngine())
						Net._network_player_info[vowner].num_vehicle[0]++;
					break;
				case Vehicle.VEH_Road:
					if (v.getCargo_type() != AcceptedCargo.CT_PASSENGERS)
						Net._network_player_info[vowner].num_vehicle[1]++;
					else
						Net._network_player_info[vowner].num_vehicle[2]++;
					break;
				case Vehicle.VEH_Aircraft:
					if (v.getSubtype() <= 2)
						Net._network_player_info[vowner].num_vehicle[3]++;
					break;
				case Vehicle.VEH_Ship:
					Net._network_player_info[vowner].num_vehicle[4]++;
					break;
				case Vehicle.VEH_Special:
				case Vehicle.VEH_Disaster:
					break;
				}
			}
		}

		// Go through all stations and count the types of stations
		//FOR_ALL_STATIONS(s)
		Station.forEach( (s) -> 
		{
			if (s.getOwner().isValid()) 
			{
				int f = s.getFacilities();
				int oid = s.getOwner().id;
				if (0 !=(f & Station.FACIL_TRAIN))
					Net._network_player_info[oid].num_station[0]++;
				if (0 !=(f & Station.FACIL_TRUCK_STOP))
					Net._network_player_info[oid].num_station[1]++;
				if (0 !=(f & Station.FACIL_BUS_STOP))
					Net._network_player_info[oid].num_station[2]++;
				if (0 !=(f & Station.FACIL_AIRPORT))
					Net._network_player_info[oid].num_station[3]++;
				if (0 !=(f & Station.FACIL_DOCK))
					Net._network_player_info[oid].num_station[4]++;
			}
		});

		NetworkClientInfo cis = Net.NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX);
		// Register local player (if not dedicated)
		if (cis != null && cis.client_playas > 0  && cis.client_playas <= Global.MAX_PLAYERS)
			Net._network_player_info[cis.client_playas-1].players = cis.client_name;

		Net.FOR_ALL_CLIENTS( (cs) -> {
			String client_name = Net.NetworkGetClientName(cs);

			NetworkClientInfo ci = cs.ci; // DEREF_CLIENT_INFO(cs);
			if (ci != null && ci.client_playas > 0 && ci.client_playas <= Global.MAX_PLAYERS) {
				if(!Net._network_player_info[ci.client_playas-1].players.isBlank())
					Net._network_player_info[ci.client_playas - 1].players += ", ";

				Net._network_player_info[ci.client_playas - 1].players += client_name;
			}
		});
	}

	// Send a packet to all clients with updated info about this client_index
	static void NetworkUpdateClientInfo(int client_index) throws IOException
	{
		NetworkClientInfo ci = Net.NetworkFindClientInfoFromIndex(client_index);

		if (ci == null)
			return;

		//Net.FOR_ALL_CLIENTS( (cs) -> { NetworkPacketSend_PACKET_SERVER_CLIENT_INFO_command(cs, ci); } );		
		for( NetworkClientState cs : Net._clients )
			NetworkPacketSend_PACKET_SERVER_CLIENT_INFO_command(cs, ci);
	}

	//extern void SwitchMode(int new_mode);

	/* Check if we want to restart the map */
	static void NetworkCheckRestartMap()
	{
		if (Net._network_restart_game_date != 0 && Global.get_cur_year() + Global.MAX_YEAR_BEGIN_REAL >= Net._network_restart_game_date) {
			Global.DEBUG_net( 0, "Auto-restarting map. Year %d reached.", Global.get_cur_year() + Global.MAX_YEAR_BEGIN_REAL);

			Global._random_seeds[0][0] = Hal.Random();
			Global._random_seeds[0][1] = Hal.InteractiveRandom();

			Main.SwitchMode(SwitchModes.SM_NEWGAME);
		}
	}

	/* Check if the server has autoclean_companies activated
	    Two things happen:
	      1) If a company is not protected, it is closed after 1 year (for example)
	      2) If a company is protected, protection is disabled after 3 years (for example)
	           (and item 1. happens a year later) */
	static void NetworkAutoCleanCompanies()
	{
		//NetworkClientState cs;
		//NetworkClientInfo ci;
		//Player p;
		boolean [] clients_in_company = new boolean[Global.MAX_PLAYERS];

		if (!Net._network_autoclean_companies)
			return;

		//memset(clients_in_company, 0, sizeof(clients_in_company));

		/* Detect the active companies */
		Net.FOR_ALL_CLIENTS(cs -> {
			NetworkClientInfo ci = cs.ci; // DEREF_CLIENT_INFO(cs);
			if (ci.client_playas >= 1 && ci.client_playas <= Global.MAX_PLAYERS) {
				clients_in_company[ci.client_playas-1] = true;
			}
		});

		if (!Global._network_dedicated) {
			NetworkClientInfo ci = Net.NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX);
			if (ci.client_playas >= 1 && ci.client_playas <= Global.MAX_PLAYERS) {
				clients_in_company[ci.client_playas-1] = true;
			}
		}

		/* Go through all the comapnies */
		//FOR_ALL_PLAYERS(p) 
		Iterator<Player> ii = Player.getIterator();
		//Player.forEach(p -> 
		while(ii.hasNext())
		{
			Player p = ii.next();
			/* Skip the non-active once */
			if (!p.isActive() || p.isAi())
				continue;

			int pindex = p.getIndex().id;
			
			if (!clients_in_company[pindex]) {
				/* The company is empty for one month more */
				Net._network_player_info[pindex].months_empty++;

				/* Is the company empty for autoclean_unprotected-months, and is there no protection? */
				if (Net._network_player_info[pindex].months_empty > Net._network_autoclean_unprotected && Net._network_player_info[pindex].password.isBlank()) {
					/* Shut the company down */
					Cmd.DoCommandP(null, 2, pindex, null, Cmd.CMD_PLAYER_CTRL);
					DefaultConsole.IConsolePrintF(DefaultConsole._icolour_def, "Auto-cleaned company #%d", pindex+1);
				}
				/* Is the compnay empty for autoclean_protected-months, and there is a protection? */
				if (Net._network_player_info[pindex].months_empty > Net._network_autoclean_protected && !Net._network_player_info[pindex].password.isBlank()) {
					/* Unprotect the company */
					Net._network_player_info[pindex].password = "";
					DefaultConsole.IConsolePrintF(DefaultConsole._icolour_def, "Auto-removed protection from company #%d", pindex+1);
					Net._network_player_info[pindex].months_empty = 0;
				}
			} else {
				/* It is not empty, reset the date */
				Net._network_player_info[pindex].months_empty = 0;
			}
		}
	}

	// This function changes new_name to a name that is unique (by adding #1 ...)
	//  and it returns true if that succeeded.
	static boolean NetworkFindName(String new_name)
	{
		//NetworkClientState new_cs;
		NetworkClientInfo ci;
		boolean found_name = false;
		byte number = 0;
		String  original_name;

		// We use NETWORK_NAME_LENGTH in here, because new_name is really a pointer
		original_name =  new_name;

		while (!found_name) {
			found_name = true;
			//FOR_ALL_CLIENTS(new_cs)
			for( NetworkClientState new_cs : Net._clients )
			{
				ci = new_cs.ci; // DEREF_CLIENT_INFO(new_cs);
				if (ci.client_name.equals(new_name)) {
					// Name already in use
					found_name = false;
					break;
				}
			}
			// Check if it is the same as the server-name
			ci = Net.NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX);
			if (ci != null) {
				if (ci.client_name.equals(new_name)) {
					// Name already in use
					found_name = false;
				}
			}

			if (!found_name) {
				// Try a new name (<name> #1, <name> #2, and so on)

				// Stop if we tried for more than 50 times..
				if (number++ > 50) break;
				new_name = String.format("%s #%d", original_name, number);
			}
		}

		return found_name;
	}

	// Reads a packet from the stream
	static boolean NetworkServer_ReadPackets(NetworkClientState cs) throws IOException
	{
		Packet p;
		NetworkRecvStatus [] res = {null};
		while((p = Net.NetworkRecv_Packet(cs, res)) != null) {
			byte type = NetworkRecv_byte(cs, p);
			if (type < PacketType.END.ordinal() && _network_server_packet[type] != null && !cs.quited)
				_network_server_packet[type].accept(cs, p);
			else
				Global.DEBUG_net( 0, "[NET][Server] Received invalid packet type %d", type);
			//free(p);
		}

		return true;
	}

	static // Handle the local command-queue
	void NetworkHandleCommandQueue(NetworkClientState cs) throws IOException {
		//CommandPacket cp;

		for( CommandPacket cp : cs.command_queue)
			NetworkPacketSend_PACKET_SERVER_COMMAND_command(cs, cp);
		
		/*while ( (cp = cs.command_queue) != null) {
			NetworkPacketSend_PACKET_SERVER_COMMAND_command(cs, cp);

			cs.command_queue = cp.next;
			//free(cp);
		}*/
	}

	// This is called every tick if this is a _network_server
	static void NetworkServer_Tick(boolean send_frame) throws IOException
	{
		//NetworkClientState cs;
		//#ifndef ENABLE_NETWORK_SYNC_EVERY_FRAME
		boolean send_sync = false;
		//#endif

		//#ifndef ENABLE_NETWORK_SYNC_EVERY_FRAME
		if (Global._frame_counter >= Net._last_sync_frame + Net._network_sync_freq) {
			Net._last_sync_frame = Global._frame_counter;
			send_sync = true;
		}
		//#endif

		// Now we are done with the frame, inform the clients that they can
		//  do their frame!
		//FOR_ALL_CLIENTS(cs)
		for( NetworkClientState cs : Net._clients )
		{
			// Check if the speed of the client is what we can expect from a client
			if (cs.status == ClientStatus.ACTIVE) {
				// 1 lag-point per day
				int lag = Net.NetworkCalculateLag(cs) / Global.DAY_TICKS;
				if (lag > 0) {
					if (lag > 3) {
						// Client did still not report in after 4 game-day, drop him
						//  (that is, the 3 of above, + 1 before any lag is counted)
						DefaultConsole.IConsolePrintF(DefaultConsole._icolour_err,"Client #%d is dropped because the client did not respond for more than 4 game-days", cs.getIndex());
						Net.NetworkCloseClient(cs);
						continue;
					}

					// Report once per time we detect the lag
					if (cs.lag_test == 0) {
						DefaultConsole.IConsolePrintF(DefaultConsole._icolour_warn,"[%d] Client #%d is slow, try increasing *net_frame_freq to a higher value!", Global._frame_counter, cs.getIndex());
						cs.lag_test = 1;
					}
				} else {
					cs.lag_test = 0;
				}
			} else if (cs.status == ClientStatus.PRE_ACTIVE) {
				int lag = Net.NetworkCalculateLag(cs);
				if (lag > Net._network_max_join_time) {
					DefaultConsole.IConsolePrintF(DefaultConsole._icolour_err,"Client #%d is dropped because it took longer than %d ticks for him to join", cs.getIndex(), Net._network_max_join_time);
					Net.NetworkCloseClient(cs);
				}
			}

			if (cs.status.ordinal() >= ClientStatus.PRE_ACTIVE.ordinal()) {
				// Check if we can send command, and if we have anything in the queue
				NetworkHandleCommandQueue(cs);

				// Send an updated _frame_counter_max to the client
				if (send_frame)
					NetworkPacketSend_PACKET_SERVER_FRAME_command(cs);

				//#ifndef ENABLE_NETWORK_SYNC_EVERY_FRAME
				// Send a sync-check packet
				if (send_sync)
					NetworkPacketSend_PACKET_SERVER_SYNC_command(cs);
				//#endif
			}
		}

		/* See if we need to advertise */
		NetUDP.NetworkUDPAdvertise();
	}

	static void NetworkServerYearlyLoop()
	{
		NetworkCheckRestartMap();
	}

	static void NetworkServerMonthlyLoop()
	{
		NetworkAutoCleanCompanies();
	}



}

// The layout for the receive-functions by the server
//typedef void NetworkServerPacket(NetworkClientState cs, Packet p);

@FunctionalInterface
interface NetworkServerPacket {
	void accept(NetworkClientState cs, Packet p) throws IOException;
}


