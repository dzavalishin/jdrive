package com.dzavalishin.net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.dzavalishin.ai.Ai;
import com.dzavalishin.console.ConsoleFactory;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Main;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.SaveLoad;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Version;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Strings;
import com.dzavalishin.xui.Gui;
import com.dzavalishin.xui.Window;

public interface NetClient extends NetTools, NetDefs 
{
	
	// TODO convert to Packet methods
	public static void NetworkSend_byte(Packet p, byte b) {
		p.append(b);	
	}

	public static void NetworkSend_int(Packet p, int i) {
		p.appendInt(i);		
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

	
	
	
	// So we don't make too much typos ;)
	//#define MY_CLIENT() DEREF_CLIENT(0)

	static NetworkClientState MY_CLIENT() {
		return Net._clients.get(0); // [dz] can it be just a static object?
	}
	

	// **********
	// Sending functions
	//   void NetworkPacketSend_ ## type ## _command() has no parameters
	// **********

	static void NetworkPacketSend_PACKET_CLIENT_COMPANY_INFO_command()
	{
		//
		// Packet: CLIENT_COMPANY_INFO
		// Function: Request company-info (in detail)
		// Data:
		//    <none>
		//
		Packet p;
		Net._network_join_status = NetworkJoinStatus.GETTING_COMPANY_INFO;
		Window.InvalidateWindow(Window.WC_NETWORK_STATUS_WINDOW, 0);

		p = new Packet(PacketType.CLIENT_COMPANY_INFO);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}


	static void NetworkPacketSend_PACKET_CLIENT_JOIN_command() throws IOException
	{
		//
		// Packet: CLIENT_JOIN
		// Function: Try to join the server
		// Data:
		//    String: OpenTTD Revision (norev000 if no revision)
		//    String: Player Name (max NETWORK_NAME_LENGTH)
		//    byte:  Play as Player id (1..Global.MAX_PLAYERS)
		//    byte:  Language ID
		//    String: Unique id to find the player back in server-listing
		//

		Packet p;
		Net._network_join_status = NetworkJoinStatus.AUTHORIZING;
		Window.InvalidateWindow(Window.WC_NETWORK_STATUS_WINDOW, 0);

		p = new Packet(PacketType.CLIENT_JOIN);
		NetworkSend_string(p, Version.NAME);
		NetworkSend_string(p, Net._network_player_name); // Player name
		NetworkSend_byte(p, (byte) Global._network_playas); // PlayAs
		NetworkSend_byte(p, (byte) NETLANG_ANY); // Language
		NetworkSend_string(p, Net._network_unique_id);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_PASSWORD_command(NetworkPasswordType type, final String password) throws IOException
	{
		//
		// Packet: CLIENT_PASSWORD
		// Function: Send a password to the server to authorize
		// Data:
		//    byte:  NetworkPasswordType
		//    String: Password
		//
		Packet p = new Packet(PacketType.CLIENT_PASSWORD);
		NetworkSend_byte(p, (byte) type.ordinal());
		NetworkSend_string(p, password);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}



	static void NetworkPacketSend_PACKET_CLIENT_GETMAP_command()
	{
		//
		// Packet: CLIENT_GETMAP
		// Function: Request the map from the server
		// Data:
		//    <none>
		//

		Packet p = new Packet(PacketType.CLIENT_GETMAP);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_MAP_OK_command()
	{
		//
		// Packet: CLIENT_MAP_OK
		// Function: Tell the server that we are done receiving/loading the map
		// Data:
		//    <none>
		//

		Packet p = new Packet(PacketType.CLIENT_MAP_OK);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_ACK_command()
	{
		//
		// Packet: CLIENT_ACK
		// Function: Tell the server we are done with this frame
		// Data:
		//    int: current FrameCounter of the client
		//

		Packet p = new Packet(PacketType.CLIENT_ACK);

		NetworkSend_int(p, Global._frame_counter);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}



	// Send a command packet to the server
	static void NetworkPacketSend_PACKET_CLIENT_COMMAND_command(CommandPacket cp) throws IOException
	{
		//
		// Packet: CLIENT_COMMAND
		// Function: Send a DoCommand to the Server
		// Data:
		//    byte:  PlayerID (0..Global.MAX_PLAYERS-1)
		//    int: CommandID (see command.h)
		//    int: P1 (free variables used in DoCommand)
		//    int: P2
		//    int: Tile
		//    string: text
		//    byte:  CallBackID (see callback_table.c)
		//

		Packet p = new Packet(PacketType.CLIENT_COMMAND);

		NetworkSend_byte(p, (byte)cp.player.id);
		NetworkSend_int(p, cp.cmd);
		NetworkSend_int(p, cp.p1);
		NetworkSend_int(p, cp.p2);
		NetworkSend_int(p, cp.tile.getTile());
		NetworkSend_string(p, cp.text);
		NetworkSend_byte(p, (byte) cp.callback);

		Net.NetworkSend_Packet(p, MY_CLIENT());
	}

	// Send a chat-packet over the network
	static void NetworkPacketSend_PACKET_CLIENT_CHAT_command(NetworkAction action, DestType desttype, int dest, final String msg) throws IOException
	{
		//
		// Packet: CLIENT_CHAT
		// Function: Send a chat-packet to the serve
		// Data:
		//    byte:  ActionID (see network_data.h, NetworkAction)
		//    byte:  Destination Type (see network_data.h, DestType);
		//    byte:  Destination Player (1..Global.MAX_PLAYERS)
		//    String: Message (max MAX_TEXT_MSG_LEN)
		//

		Packet p = new Packet(PacketType.CLIENT_CHAT);

		NetworkSend_byte(p, (byte)action.ordinal());
		NetworkSend_byte(p, (byte) desttype.ordinal());
		NetworkSend_byte(p, (byte) dest);
		NetworkSend_string(p, msg);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}

	// Send an error-packet over the network
	static void NetworkPacketSend_PACKET_CLIENT_ERROR_command(NetworkErrorCode errorno)
	{
		//
		// Packet: CLIENT_ERROR
		// Function: The client made an error and is quiting the game
		// Data:
		//    byte:  ErrorID (see network_data.h, NetworkErrorCode)
		//
		Packet p = new Packet(PacketType.CLIENT_ERROR);

		NetworkSend_byte(p, (byte) errorno.ordinal());
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_SET_PASSWORD_command(final String password) throws IOException
	{
		//
		// Packet: PACKET_CLIENT_SET_PASSWORD
		// Function: Set the password for the clients current company
		// Data:
		//    String: Password
		//
		Packet p = new Packet(PacketType.CLIENT_SET_PASSWORD);

		NetworkSend_string(p, password);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_SET_NAME_command(final String name) throws IOException
	{
		//
		// Packet: PACKET_CLIENT_SET_NAME
		// Function: Gives the player a new name
		// Data:
		//    String: Name
		//
		Packet p = new Packet(PacketType.CLIENT_SET_NAME);

		NetworkSend_string(p, name);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}

	// Send an quit-packet over the network
	static void NetworkPacketSend_PACKET_CLIENT_QUIT_command(final String leavemsg) throws IOException
	{
		//
		// Packet: CLIENT_QUIT
		// Function: The client is quiting the game
		// Data:
		//    String: leave-message
		//
		Packet p = new Packet(PacketType.CLIENT_QUIT);

		NetworkSend_string(p, leavemsg);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_RCON_command(final String pass, final String command) throws IOException
	{
		Packet p = new Packet(PacketType.CLIENT_RCON);
		NetworkSend_string(p, pass);
		NetworkSend_string(p, command);
		Net.NetworkSend_Packet(p, MY_CLIENT());
	}


	// **********
	// Receiving functions
	//   void NetworkPacketReceive_ ## type ## _command(NetworkClientState cs, Packet p) has parameter: Packet p
	// **********

	//extern boolean SafeSaveOrLoad(final String filename, int mode, int newgm);

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_FULL_command(NetworkClientState cs, Packet p)
	{
		// We try to join a server which is full
		Global._switch_mode_errorstr = new StringID( Str.STR_NETWORK_ERR_SERVER_FULL );
		Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);

		return NetworkRecvStatus.SERVER_FULL;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_BANNED_command(NetworkClientState cs, Packet p)
	{
		// We try to join a server where we are banned
		Global._switch_mode_errorstr = new StringID( Str.STR_NETWORK_ERR_SERVER_BANNED );
		Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);

		return NetworkRecvStatus.SERVER_BANNED;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_COMPANY_INFO_command(NetworkClientState cs, Packet p)
	{
		byte company_info_version;
		int i;

		company_info_version = NetworkRecv_byte(MY_CLIENT(), p);

		if (!MY_CLIENT().quited && company_info_version == NETWORK_COMPANY_INFO_VERSION) {
			byte total;
			byte current;

			total = NetworkRecv_byte(MY_CLIENT(), p);

			// There is no data at all..
			if (total == 0)
				return NetworkRecvStatus.CLOSE_QUERY;

			current = NetworkRecv_byte(MY_CLIENT(), p);
			if (current >= Global.MAX_PLAYERS)
				return NetworkRecvStatus.CLOSE_QUERY;

			Net._network_lobby_company_count++;

			Net._network_player_info[current].company_name = NetworkRecv_string(MY_CLIENT(), p);
			Net._network_player_info[current].inaugurated_year = NetworkRecv_byte(MY_CLIENT(), p);
			Net._network_player_info[current].company_value = NetworkRecv_int64(MY_CLIENT(), p);
			Net._network_player_info[current].money = NetworkRecv_int64(MY_CLIENT(), p);
			Net._network_player_info[current].income = NetworkRecv_int64(MY_CLIENT(), p);
			Net._network_player_info[current].performance = NetworkRecv_int(MY_CLIENT(), p);
			Net._network_player_info[current].use_password = NetworkRecv_byte(MY_CLIENT(), p);
			for (i = 0; i < NETWORK_VEHICLE_TYPES; i++)
				Net._network_player_info[current].num_vehicle[i] = NetworkRecv_int(MY_CLIENT(), p);
			for (i = 0; i < NETWORK_STATION_TYPES; i++)
				Net._network_player_info[current].num_station[i] = NetworkRecv_int(MY_CLIENT(), p);

			Net._network_player_info[current].players = NetworkRecv_string(MY_CLIENT(), p);

			Window.InvalidateWindow(Window.WC_NETWORK_WINDOW, 0);

			return NetworkRecvStatus.OKAY;
		}

		return NetworkRecvStatus.CLOSE_QUERY;
	}



	// This packet contains info about the client (playas and name)
	//  as client we save this in NetworkClientInfo, linked via 'index'
	//  which is always an unique number on a server.
	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_CLIENT_INFO_command(NetworkClientState cs, Packet p)
	{
		NetworkClientInfo ci;
		int index = NetworkRecv_int(MY_CLIENT(), p);
		byte playas = NetworkRecv_byte(MY_CLIENT(), p);
		//char name[NETWORK_NAME_LENGTH];
		//char unique_id[NETWORK_NAME_LENGTH];

		String name = NetworkRecv_string(MY_CLIENT(), p);
		String unique_id = NetworkRecv_string(MY_CLIENT(), p);

		if (MY_CLIENT().quited)
			return NetworkRecvStatus.CONN_LOST;

		/* Do we receive a change of data? Most likely we changed playas */
		if (index == Net._network_own_client_index) {
			Global._network_playas = playas;

			/* Are we a ai-network-client? Are we not joining as a SPECTATOR (playas == 0, means SPECTATOR) */
			if (Ai._ai.network_client && playas != 0) {
				if (Ai._ai.network_playas == Owner.OWNER_SPECTATOR)
					Ai.AI_StartNewAI( PlayerID.get( playas - 1 ) );

				Ai._ai.network_playas = playas - 1;
			}
		}

		ci = Net.NetworkFindClientInfoFromIndex(index);
		if (ci != null) {
			if (playas == ci.client_playas && !name.equals(ci.client_name)) {
				// Client name changed, display the change
				Net.NetworkTextMessage(NetworkAction.NAME_CHANGE, 1, false, ci.client_name, "%s", name);
			} else if (playas != ci.client_playas) {
				// The player changed from client-player..
				// Do not display that for now
			}

			ci.client_playas = playas;
			ci.client_name = name;

			Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

			return NetworkRecvStatus.OKAY;
		}

		// We don't have this index yet, find an empty index, and put the data there
		ci = Net.NetworkFindClientInfoFromIndex(NETWORK_EMPTY_INDEX);
		if (ci != null) {
			ci.client_index = index;
			ci.client_playas = playas;

			ci.client_name = name;
			ci.unique_id = unique_id;

			Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

			return NetworkRecvStatus.OKAY;
		}

		// Here the program should never ever come.....
		return NetworkRecvStatus.MALFORMED_PACKET;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_ERROR_command(NetworkClientState cs, Packet p)
	{
		NetworkErrorCode error = NetworkErrorCode.value( NetworkRecv_byte(MY_CLIENT(), p) );

		if (error == NetworkErrorCode.NOT_AUTHORIZED || error == NetworkErrorCode.NOT_EXPECTED ||
				error == NetworkErrorCode.PLAYER_MISMATCH) {
			// We made an error in the protocol, and our connection is closed.... :(
			Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_SERVER_ERROR);
		} else if (error == NetworkErrorCode.WRONG_REVISION) {
			// Wrong revision :(
			Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_WRONG_REVISION);
		} else if (error == NetworkErrorCode.WRONG_PASSWORD) {
			// Wrong password
			Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_WRONG_PASSWORD );
		} else if (error == NetworkErrorCode.KICKED) {
			Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_KICKED );
		} else if (error == NetworkErrorCode.CHEATER) {
			Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_CHEATER );
		}

		Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);

		return NetworkRecvStatus.SERVER_ERROR;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_NEED_PASSWORD_command(NetworkClientState cs, Packet p)
	{
		NetworkPasswordType type;
		type = NetworkPasswordType.value( NetworkRecv_byte(MY_CLIENT(), p) );

		if (type == NetworkPasswordType.NETWORK_GAME_PASSWORD) {
			Gui.ShowNetworkNeedGamePassword();
			return NetworkRecvStatus.OKAY;
		} else if (type == NetworkPasswordType.NETWORK_COMPANY_PASSWORD) {
			Gui.ShowNetworkNeedCompanyPassword();
			return NetworkRecvStatus.OKAY;
		}

		return NetworkRecvStatus.MALFORMED_PACKET;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_WELCOME_command(NetworkClientState cs, Packet p)
	{
		Net._network_own_client_index = NetworkRecv_int(MY_CLIENT(), p);

		// Start receiving the map
		NetworkPacketSend_PACKET_CLIENT_GETMAP_command();
		return NetworkRecvStatus.OKAY;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_WAIT_command(NetworkClientState cs, Packet p)
	{
		Net._network_join_status = NetworkJoinStatus.WAITING;
		Net._network_join_waiting = NetworkRecv_byte(MY_CLIENT(), p);
		Window.InvalidateWindow(Window.WC_NETWORK_STATUS_WINDOW, 0);

		// We are put on hold for receiving the map.. we need GUI for this ;)
		Global.DEBUG_net( 1, "[NET] The server is currently busy sending the map to someone else.. please hold..." );
		Global.DEBUG_net( 1, "[NET]  There are %d clients in front of you", Net._network_join_waiting);

		return NetworkRecvStatus.OKAY;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_MAP_command(NetworkClientState cs, Packet p)
	{
		//static FILE *file_pointer;

		byte maptype;

		maptype = NetworkRecv_byte(MY_CLIENT(), p);

		if (MY_CLIENT().quited)
			return NetworkRecvStatus.CONN_LOST;

		// First packet, init some stuff
		if (maptype == MapPacket.MAP_PACKET_START.ordinal()) {
			// The name for the temp-map
			Net.recvMapFilename = String.format( "%s%snetwork_client.tmp",  Global._path.autosave_dir, File.separator);

			File f = new File(Net.recvMapFilename);
			try {
				Net.client_file_pointer = new RandomAccessFile(f, "w");
			} catch (FileNotFoundException e) {
				// e.printStackTrace();
				Global.error(e);
				Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_SAVEGAMEERROR);
				return NetworkRecvStatus.SAVEGAME;
			} //new FileOutputStream(recvMapFilename);
			//file_pointer = fopen(recvMapFilename, "wb");
			/*if (Net.client_file_pointer == null) {
				Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_SAVEGAMEERROR);
				return NetworkRecvStatus.SAVEGAME;
			}*/

			Global._frame_counter = Net._frame_counter_server = Net._frame_counter_max = NetworkRecv_int(MY_CLIENT(), p);

			Net._network_join_status = NetworkJoinStatus.DOWNLOADING;
			Net._network_join_kbytes = 0;
			Net._network_join_kbytes_total = NetworkRecv_int(MY_CLIENT(), p) / 1024;
			Window.InvalidateWindow(Window.WC_NETWORK_STATUS_WINDOW, 0);

			// The first packet does not contain any more data
			return NetworkRecvStatus.OKAY;
		}

		if (maptype == MapPacket.MAP_PACKET_NORMAL.ordinal()) {
			// We are still receiving data, put it to the file
			//fwrite(p.buffer + p.pos, 1, p.size - p.pos, file_pointer);
			byte []  buf = p.asByteArray();
			try {
				Net.client_file_pointer.write(buf, 0, buf.length);
			} catch (IOException e) {
				Global.error(e);
				Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_SAVEGAMEERROR);
				return NetworkRecvStatus.SAVEGAME; // TODO right?
			}
			
			//Net._network_join_kbytes = ftell(file_pointer) / 1024;
			try {
				Net._network_join_kbytes = (int) (Net.client_file_pointer.getFilePointer() / 1024);
			} catch (IOException e) {
				Global.error(e);
			}
			Window.InvalidateWindow(Window.WC_NETWORK_STATUS_WINDOW, 0);
		}

		if (maptype == MapPacket.MAP_PACKET_PATCH.ordinal()) {
			NetworkRecvPatchSettings(MY_CLIENT(), p);
		}

		// Check if this was the last packet
		if (maptype == MapPacket.MAP_PACKET_END.ordinal()) {
			try {
				Net.client_file_pointer.close();
			} catch (IOException e) {
				Global.error(e);
				Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_SAVEGAMEERROR);
				return NetworkRecvStatus.SAVEGAME; // TODO [dz] is this error handling correct?
			}

			Net._network_join_status = NetworkJoinStatus.PROCESSING;
			Window.InvalidateWindow(Window.WC_NETWORK_STATUS_WINDOW, 0);

			// The map is done downloading, load it
			// Load the map
			if (!Main.SafeSaveOrLoad(Net.recvMapFilename, SaveLoad.SL_LOAD, GameModes.GM_NORMAL)) {
				Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);
				Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_ERR_SAVEGAMEERROR);
				return NetworkRecvStatus.SAVEGAME;
			}

			GameOptions._opt_ptr = GameOptions._opt; // during a network game you are always in-game

			// Say we received the map and loaded it correctly!
			NetworkPacketSend_PACKET_CLIENT_MAP_OK_command();

			if (Global._network_playas == 0 || Global._network_playas > Global.MAX_PLAYERS ||
					!Player.GetPlayer(Global._network_playas - 1).isActive()) {

				if (Global._network_playas == Owner.OWNER_SPECTATOR) {
					// The client wants to be a spectator..
					Global.gs._local_player = PlayerID.get(Owner.OWNER_SPECTATOR);
					Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);
				} else {
					/* We have arrived and ready to start playing; send a command to make a new player;
					 * the server will give us a client-id and let us in */
					Global.gs._local_player = PlayerID.get(0);
					Net.NetworkSend_Command(null, 0, 0, Cmd.CMD_PLAYER_CTRL, null);
					Global.gs._local_player = PlayerID.get(Owner.OWNER_SPECTATOR);
				}
			} else {
				// take control over an existing company
				Global.gs._local_player = PlayerID.get(Global._network_playas - 1);
				final Player lp = Player.GetPlayer(Global.gs._local_player);
				Global._patches.autorenew.set( lp.isEngine_renew() );
				Global._patches.autorenew_months = lp.getEngine_renew_months();
				Global._patches.autorenew_money = lp.getEngine_renew_money();
				Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);
			}

			/* Check if we are an ai-network-client, and if so, disable GUI */
			if (Ai._ai.network_client) {
				Ai._ai.network_playas = Global.gs._local_player.id;
				Global.gs._local_player = PlayerID.get(Owner.OWNER_SPECTATOR);

				if (Ai._ai.network_playas != Owner.OWNER_SPECTATOR) {
					/* If we didn't join the game as a spectator, activate the AI */
					Ai.AI_StartNewAI(PlayerID.get(Ai._ai.network_playas));
				}
			}
		}

		return NetworkRecvStatus.OKAY;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_FRAME_command(NetworkClientState cs, Packet p)
	{
		Net._frame_counter_server = NetworkRecv_int(MY_CLIENT(), p);
		Net._frame_counter_max = NetworkRecv_int(MY_CLIENT(), p);
	//#ifdef ENABLE_NETWORK_SYNC_EVERY_FRAME
		// Test if the server supports this option
		//  and if we are at the frame the server is
		// TODO XXX [dz] I don't get it
		/*if (p.pos < p.size) {
			Net._sync_frame = Net._frame_counter_server;
			Net._sync_seed_1 = NetworkRecv_int(MY_CLIENT(), p);
	//#ifdef NETWORK_SEND_DOUBLE_SEED
	//		_sync_seed_2 = NetworkRecv_int(MY_CLIENT(), p);
	//#endif
		} */
	//#endif
		Global.DEBUG_net( 7, "[NET] Received FRAME %d", Net._frame_counter_server);

		// Let the server know that we received this frame correctly
		//  We do this only once per day, to save some bandwidth ;)
		if (!Net._network_first_time && Net.client_last_ack_frame < Global._frame_counter) {
			Net.client_last_ack_frame = Global._frame_counter + Global.DAY_TICKS;
			Global.DEBUG_net(6, "[NET] Sent ACK at %d", Global._frame_counter);
			NetworkPacketSend_PACKET_CLIENT_ACK_command();
		}

		return NetworkRecvStatus.OKAY;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_SYNC_command(NetworkClientState cs, Packet p)
	{
		Net._sync_frame = NetworkRecv_int(MY_CLIENT(), p);
		Net._sync_seed_1 = NetworkRecv_int(MY_CLIENT(), p);
	//#ifdef NETWORK_SEND_DOUBLE_SEED
	//	_sync_seed_2 = NetworkRecv_int(MY_CLIENT(), p);
	//#endif

		return NetworkRecvStatus.OKAY;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_COMMAND_command(NetworkClientState cs, Packet p)
	{
		CommandPacket cp = new CommandPacket();
		cp.player = PlayerID.get( NetworkRecv_byte(MY_CLIENT(), p) );
		cp.cmd = NetworkRecv_int(MY_CLIENT(), p);
		cp.p1 = NetworkRecv_int(MY_CLIENT(), p);
		cp.p2 = NetworkRecv_int(MY_CLIENT(), p);
		cp.tile = TileIndex.get( NetworkRecv_int(MY_CLIENT(), p) );
		cp.text = NetworkRecv_string(MY_CLIENT(), p);
		cp.callback = NetworkRecv_byte(MY_CLIENT(), p);
		cp.frame = NetworkRecv_int(MY_CLIENT(), p);
		//cp.next = null;

		// The server did send us this command..
		//  queue it in our own queue, so we can handle it in the upcoming frame!

		Net._local_command_queue.add(cp);
		
		/*
		if (_local_command_queue == null) {
			_local_command_queue = cp;
		} else {
			// Find last packet
			CommandPacket c = _local_command_queue;
			while (c.next != null) c = c.next;
			c.next = cp;
		}*/

		return NetworkRecvStatus.OKAY;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_CHAT_command(NetworkClientState cs, Packet p)
	{
		NetworkAction action = NetworkAction.value( NetworkRecv_byte(MY_CLIENT(), p) );
		String msg;
		NetworkClientInfo ci = null, ci_to;
		int index;
		String name = null;
		boolean self_send;

		index = NetworkRecv_int(MY_CLIENT(), p);
		self_send = BitOps.i2b( NetworkRecv_byte(MY_CLIENT(), p) );
		msg = NetworkRecv_string(MY_CLIENT(), p);

		ci_to = Net.NetworkFindClientInfoFromIndex(index);
		if (ci_to == null) return NetworkRecvStatus.OKAY;

		/* Do we display the action locally? */
		if (self_send) {
			switch (action) {
				case CHAT_CLIENT:
					/* For speak to client we need the client-name */
					name = ci_to.client_name;
					ci = Net.NetworkFindClientInfoFromIndex(Net._network_own_client_index);
					break;
				case CHAT_PLAYER:
				case GIVE_MONEY:
					/* For speak to player or give money, we need the player-name */
					if (ci_to.client_playas > Global.MAX_PLAYERS)
						return NetworkRecvStatus.OKAY; // This should never happen
					name = Strings.GetString(Player.GetPlayer(ci_to.client_playas-1).getName_1());
					ci = Net.NetworkFindClientInfoFromIndex(Net._network_own_client_index);
					break;
				default:
					/* This should never happen */
					//NOT_REACHED();
					assert false;
					break;
			}
		} else {
			/* Display message from somebody else */
			name = ci_to.client_name;
			ci = ci_to;
		}

		if (ci != null)
			Net.NetworkTextMessage(action, Hal.GetDrawStringPlayerColor(ci.client_playas-1), self_send, name, "%s", msg);
		return NetworkRecvStatus.OKAY;
	}


	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_ERROR_QUIT_command(NetworkClientState cs, Packet p)
	{
		int errorno;		
		int index;
		NetworkClientInfo ci;

		index = NetworkRecv_int(MY_CLIENT(), p);
		errorno = NetworkRecv_byte(MY_CLIENT(), p);

		String str = Strings.GetString(Str.STR_NETWORK_ERR_CLIENT_GENERAL + errorno);

		ci = Net.NetworkFindClientInfoFromIndex(index);
		if (ci != null) {
			Net.NetworkTextMessage(NetworkAction.LEAVE, 1, false, ci.client_name, "%s", str);

			// The client is gone, give the NetworkClientInfo free
			ci.client_index = NETWORK_EMPTY_INDEX;
		}

		Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

		return NetworkRecvStatus.OKAY;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_QUIT_command(NetworkClientState cs, Packet p)
	{
		int index;
		NetworkClientInfo ci;

		index = NetworkRecv_int(MY_CLIENT(), p);
		String str = NetworkRecv_string(MY_CLIENT(), p);

		ci = Net.NetworkFindClientInfoFromIndex(index);
		if (ci != null) {
			Net.NetworkTextMessage(NetworkAction.LEAVE, 1, false, ci.client_name, "%s", str);

			// The client is gone, give the NetworkClientInfo free
			ci.client_index = NETWORK_EMPTY_INDEX;
		} else {
			Global.DEBUG_net( 0, "[NET] Error - unknown client (%d) is leaving the game", index);
		}

		Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

		// If we come here it means we could not locate the client.. strange :s
		return NetworkRecvStatus.OKAY;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_JOIN_command(NetworkClientState cs, Packet p)
	{
		int index;
		NetworkClientInfo ci;

		index = NetworkRecv_int(MY_CLIENT(), p);

		ci = Net.NetworkFindClientInfoFromIndex(index);
		if (ci != null)
			Net.NetworkTextMessage(NetworkAction.JOIN, 1, false, ci.client_name, "");

		Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

		return NetworkRecvStatus.OKAY;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_SHUTDOWN_command(NetworkClientState cs, Packet p)
	{
		Global._switch_mode_errorstr = new StringID( Str.STR_NETWORK_SERVER_SHUTDOWN );

		return NetworkRecvStatus.SERVER_ERROR;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_NEWGAME_command(NetworkClientState cs, Packet p)
	{
		// To trottle the reconnects a bit, every clients waits
		//  his Global.gs._local_player value before reconnecting
		// Owner.OWNER_SPECTATOR is currently 255, so to avoid long wait periods
		//  set the max to 10.
		Net._network_reconnect = Math.min(Global.gs._local_player.id + 1, 10);
		Global._switch_mode_errorstr = new StringID(Str.STR_NETWORK_SERVER_REBOOT);

		return NetworkRecvStatus.SERVER_ERROR;
	}

	static NetworkRecvStatus NetworkPacketReceive_PACKET_SERVER_RCON_command(NetworkClientState cs, Packet p)
	{
		int color_code = NetworkRecv_int(MY_CLIENT(), p);
		String rcon_out = NetworkRecv_string(MY_CLIENT(), p);

		//Console.IConsolePrint(color_code, rcon_out);
		ConsoleFactory.INSTANCE.getConsole().println(rcon_out, color_code);

		return NetworkRecvStatus.OKAY;
	}



	// The layout for the receive-functions by the client
	//typedef NetworkRecvStatus NetworkClientPacket(Packet p);

	// This array matches PacketType. At an incoming
	//  packet it is matches against this array
	//  and that way the right function to handle that
	//  packet is found.
	static NetworkClientPacket _network_client_packet[] = {
		NetClient::NetworkPacketReceive_PACKET_SERVER_FULL_command,
		NetClient::NetworkPacketReceive_PACKET_SERVER_BANNED_command,
		null, /*PACKET_CLIENT_JOIN,*/
		NetClient::NetworkPacketReceive_PACKET_SERVER_ERROR_command,
		null, /*PACKET_CLIENT_COMPANY_INFO,*/
		NetClient::NetworkPacketReceive_PACKET_SERVER_COMPANY_INFO_command,
		NetClient::NetworkPacketReceive_PACKET_SERVER_CLIENT_INFO_command,
		NetClient::NetworkPacketReceive_PACKET_SERVER_NEED_PASSWORD_command,
		null, /*PACKET_CLIENT_PASSWORD,*/
		NetClient::NetworkPacketReceive_PACKET_SERVER_WELCOME_command,
		null, /*PACKET_CLIENT_GETMAP,*/
		NetClient::NetworkPacketReceive_PACKET_SERVER_WAIT_command,
		NetClient::NetworkPacketReceive_PACKET_SERVER_MAP_command,
		null, /*PACKET_CLIENT_MAP_OK,*/
		NetClient::NetworkPacketReceive_PACKET_SERVER_JOIN_command,
		NetClient::NetworkPacketReceive_PACKET_SERVER_FRAME_command,
		NetClient::NetworkPacketReceive_PACKET_SERVER_SYNC_command,
		null, /*PACKET_CLIENT_ACK,*/
		null, /*PACKET_CLIENT_COMMAND,*/
		NetClient::NetworkPacketReceive_PACKET_SERVER_COMMAND_command,
		null, /*PACKET_CLIENT_CHAT,*/
		NetClient::NetworkPacketReceive_PACKET_SERVER_CHAT_command,
		null, /*PACKET_CLIENT_SET_PASSWORD,*/
		null, /*PACKET_CLIENT_SET_NAME,*/
		null, /*PACKET_CLIENT_QUIT,*/
		null, /*PACKET_CLIENT_ERROR,*/
		NetClient::NetworkPacketReceive_PACKET_SERVER_QUIT_command,
		NetClient::NetworkPacketReceive_PACKET_SERVER_ERROR_QUIT_command,
		NetClient::NetworkPacketReceive_PACKET_SERVER_SHUTDOWN_command,
		NetClient::NetworkPacketReceive_PACKET_SERVER_NEWGAME_command,
		NetClient::NetworkPacketReceive_PACKET_SERVER_RCON_command,
		null, /*PACKET_CLIENT_RCON,*/
	};

	// If this fails, check the array above with network_data.h
	//assert_compile(lengthof(_network_client_packet) == PACKET_END);

	//extern final SettingDesc patch_settings[];

	// This is a TEMPORARY solution to get the patch-settings
	//  to the client. When the patch-settings are saved in the savegame
	//  this should be removed!!
	static void NetworkRecvPatchSettings(NetworkClientState cs, Packet p)
	{
		//final SettingDesc item = patch_settings;

		/* TODO
		while (item.name != null) {
			switch (item.flags) {
				case SDT_BOOL:
				case SDT_INT8:
				case SDT_UINT8:
					*(byte *)(item.ptr) = NetworkRecv_byte(cs, p);
					break;
				case SDT_INT16:
				case SDT_UINT16:
					*(int *)(item.ptr) = NetworkRecv_int(cs, p);
					break;
				case SDT_INT32:
				case SDT_UINT32:
					*(int *)(item.ptr) = NetworkRecv_int(cs, p);
					break;
			}
			item++;
		} */
	}

	// Is called after a client is connected to the server
	static void NetworkClient_Connected() throws IOException
	{
		// Set the frame-counter to 0 so nothing happens till we are ready
		Global._frame_counter = 0;
		Net._frame_counter_server = 0;
		Net.client_last_ack_frame = 0;
		// Request the game-info
		NetworkPacketSend_PACKET_CLIENT_JOIN_command();
	}

	// Reads the packets from the socket-stream, if available
	static NetworkRecvStatus NetworkClient_ReadPackets(NetworkClientState cs) throws IOException
	{
		Packet p;
		NetworkRecvStatus [] res = { NetworkRecvStatus.OKAY };

		while (res[0] == NetworkRecvStatus.OKAY && (p = Net.NetworkRecv_Packet(cs, res)) != null) {
			byte type = NetworkRecv_byte(MY_CLIENT(), p);
			if (type < PacketType.END.ordinal() && _network_client_packet[type] != null && !MY_CLIENT().quited) {
				res[0] = _network_client_packet[type].accept(cs,p);
			} else {
				res[0] = NetworkRecvStatus.MALFORMED_PACKET;
				Global.DEBUG_net( 0, "[NET][client] Received invalid packet type %d", type);
			}

			//free(p);
		}

		return res[0];
	}


}


@FunctionalInterface
interface NetworkClientPacket {
	NetworkRecvStatus accept(NetworkClientState cs, Packet p);
}
