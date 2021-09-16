package game.net;

import game.Global;
import game.Str;
import game.ids.StringID;
import game.xui.Window;

public interface NetClient {

	// So we don't make too much typos ;)
	//#define MY_CLIENT() DEREF_CLIENT(0)

	static int last_ack_frame;

	static Object MY_CLIENT() {
		 TODO 
		return null;
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
		NetworkSend_Packet(p, MY_CLIENT());
	}


	static void NetworkPacketSend_PACKET_CLIENT_JOIN_command()
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
		NetworkSend_string(p, _openttd_revision);
		NetworkSend_string(p, _network_player_name); // Player name
		NetworkSend_byte(p, _network_playas); // PlayAs
		NetworkSend_byte(p, NETLANG_ANY); // Language
		NetworkSend_string(p, _network_unique_id);
		NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_PASSWORD_command(NetworkPasswordType type, final String password)
	{
		//
		// Packet: CLIENT_PASSWORD
		// Function: Send a password to the server to authorize
		// Data:
		//    byte:  NetworkPasswordType
		//    String: Password
		//
		Packet p = new Packet(PacketType.CLIENT_PASSWORD);
		NetworkSend_byte(p, type);
		NetworkSend_string(p, password);
		NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_GETMAP_command()
	{
		//
		// Packet: CLIENT_GETMAP
		// Function: Request the map from the server
		// Data:
		//    <none>
		//

		Packet p = new Packet(PACKET_CLIENT_GETMAP);
		NetworkSend_Packet(p, MY_CLIENT());
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
		NetworkSend_Packet(p, MY_CLIENT());
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

		NetworkSend_int(p, _frame_counter);
		NetworkSend_Packet(p, MY_CLIENT());
	}

	// Send a command packet to the server
	static void NetworkPacketSend_PACKET_CLIENT_COMMAND_command(CommandPacket cp)
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

		NetworkSend_byte(p, cp.player);
		NetworkSend_int(p, cp.cmd);
		NetworkSend_int(p, cp.p1);
		NetworkSend_int(p, cp.p2);
		NetworkSend_int(p, (int)cp.tile);
		NetworkSend_string(p, cp.text);
		NetworkSend_byte(p, cp.callback);

		NetworkSend_Packet(p, MY_CLIENT());
	}

	// Send a chat-packet over the network
	static void NetworkPacketSend_PACKET_CLIENT_CHAT_command(NetworkAction action, DestType desttype, int dest, final String msg)
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

		NetworkSend_byte(p, action);
		NetworkSend_byte(p, desttype);
		NetworkSend_byte(p, dest);
		NetworkSend_string(p, msg);
		NetworkSend_Packet(p, MY_CLIENT());
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

		NetworkSend_byte(p, errorno);
		NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_SET_PASSWORD_command(final String password)
	{
		//
		// Packet: PACKET_CLIENT_SET_PASSWORD
		// Function: Set the password for the clients current company
		// Data:
		//    String: Password
		//
		Packet p = new Packet(PacketType.CLIENT_SET_PASSWORD);

		NetworkSend_string(p, password);
		NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_SET_NAME_command(final String name)
	{
		//
		// Packet: PACKET_CLIENT_SET_NAME
		// Function: Gives the player a new name
		// Data:
		//    String: Name
		//
		Packet p = new Packet(PacketType.CLIENT_SET_NAME);

		NetworkSend_string(p, name);
		NetworkSend_Packet(p, MY_CLIENT());
	}

	// Send an quit-packet over the network
	static void NetworkPacketSend_PACKET_CLIENT_QUIT_command(final String leavemsg)
	{
		//
		// Packet: CLIENT_QUIT
		// Function: The client is quiting the game
		// Data:
		//    String: leave-message
		//
		Packet p = new Packet(PacketType.CLIENT_QUIT);

		NetworkSend_string(p, leavemsg);
		NetworkSend_Packet(p, MY_CLIENT());
	}

	static void NetworkPacketSend_PACKET_CLIENT_RCON_command(final String pass, final String command)
	{
		Packet p = new Packet(PacketType.CLIENT_RCON);
		NetworkSend_string(p, pass);
		NetworkSend_string(p, command);
		NetworkSend_Packet(p, MY_CLIENT());
	}


	// **********
	// Receiving functions
	//   void NetworkPacketReceive_ ## type ## _command(NetworkClientState cs, Packet p) has parameter: Packet p
	// **********

	//extern boolean SafeSaveOrLoad(final String filename, int mode, int newgm);

	static void NetworkPacketReceive_PACKET_SERVER_FULL_command(NetworkClientState cs, Packet p)
	{
		// We try to join a server which is full
		_switch_mode_errorstr = Str.STR_NETWORK_ERR_SERVER_FULL;
		Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);

		return NetworkRecvStatus.SERVER_FULL;
	}

	static void NetworkPacketReceive_PACKET_SERVER_BANNED_command(NetworkClientState cs, Packet p)
	{
		// We try to join a server where we are banned
		Global._switch_mode_errorstr = new StringID( Str.STR_NETWORK_ERR_SERVER_BANNED );
		Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);

		return NetworkRecvStatus.SERVER_BANNED;
	}

	static void NetworkPacketReceive_PACKET_SERVER_COMPANY_INFO_command(NetworkClientState cs, Packet p)
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

			_network_lobby_company_count++;

			NetworkRecv_string(MY_CLIENT(), p, _network_player_info[current].company_name, sizeof(_network_player_info[current].company_name));
			_network_player_info[current].inaugurated_year = NetworkRecv_byte(MY_CLIENT(), p);
			_network_player_info[current].company_value = NetworkRecv_int64(MY_CLIENT(), p);
			_network_player_info[current].money = NetworkRecv_int64(MY_CLIENT(), p);
			_network_player_info[current].income = NetworkRecv_int64(MY_CLIENT(), p);
			_network_player_info[current].performance = NetworkRecv_int(MY_CLIENT(), p);
			_network_player_info[current].use_password = NetworkRecv_byte(MY_CLIENT(), p);
			for (i = 0; i < NETWORK_VEHICLE_TYPES; i++)
				_network_player_info[current].num_vehicle[i] = NetworkRecv_int(MY_CLIENT(), p);
			for (i = 0; i < NETWORK_STATION_TYPES; i++)
				_network_player_info[current].num_station[i] = NetworkRecv_int(MY_CLIENT(), p);

			NetworkRecv_string(MY_CLIENT(), p, _network_player_info[current].players, sizeof(_network_player_info[current].players));

			Window.InvalidateWindow(Window.WC_NETWORK_WINDOW, 0);

			return NetworkRecvStatus.OKAY;
		}

		return NetworkRecvStatus.CLOSE_QUERY;
	}

	// This packet contains info about the client (playas and name)
	//  as client we save this in NetworkClientInfo, linked via 'index'
	//  which is always an unique number on a server.
	static void NetworkPacketReceive_PACKET_SERVER_CLIENT_INFO_command(NetworkClientState cs, Packet p)
	{
		NetworkClientInfo *ci;
		int index = NetworkRecv_int(MY_CLIENT(), p);
		byte playas = NetworkRecv_byte(MY_CLIENT(), p);
		char name[NETWORK_NAME_LENGTH];
		char unique_id[NETWORK_NAME_LENGTH];

		NetworkRecv_string(MY_CLIENT(), p, name, sizeof(name));
		NetworkRecv_string(MY_CLIENT(), p, unique_id, sizeof(unique_id));

		if (MY_CLIENT().quited)
			return NetworkRecvStatus.CONN_LOST;

		/* Do we receive a change of data? Most likely we changed playas */
		if (index == _network_own_client_index) {
			_network_playas = playas;

			/* Are we a ai-network-client? Are we not joining as a SPECTATOR (playas == 0, means SPECTATOR) */
			if (Ai._ai.network_client && playas != 0) {
				if (Ai._ai.network_playas == Owner.OWNER_SPECTATOR)
					AI_StartNewAI(playas - 1);

				Ai._ai.network_playas = playas - 1;
			}
		}

		ci = NetworkFindClientInfoFromIndex(index);
		if (ci != null) {
			if (playas == ci.client_playas && strcmp(name, ci.client_name) != 0) {
				// Client name changed, display the change
				NetworkTextMessage(NETWORK_ACTION_NAME_CHANGE, 1, false, ci.client_name, "%s", name);
			} else if (playas != ci.client_playas) {
				// The player changed from client-player..
				// Do not display that for now
			}

			ci.client_playas = playas;
			ttd_strlcpy(ci.client_name, name, sizeof(ci.client_name));

			Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

			return NetworkRecvStatus.OKAY;
		}

		// We don't have this index yet, find an empty index, and put the data there
		ci = NetworkFindClientInfoFromIndex(NETWORK_EMPTY_INDEX);
		if (ci != null) {
			ci.client_index = index;
			ci.client_playas = playas;

			ttd_strlcpy(ci.client_name, name, sizeof(ci.client_name));
			ttd_strlcpy(ci.unique_id, unique_id, sizeof(ci.unique_id));

			Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

			return NetworkRecvStatus.OKAY;
		}

		// Here the program should never ever come.....
		return NetworkRecvStatus.MALFORMED_PACKET;
	}

	static void NetworkPacketReceive_PACKET_SERVER_ERROR_command(NetworkClientState cs, Packet p)
	{
		NetworkErrorCode error = NetworkRecv_byte(MY_CLIENT(), p);

		if (error == NETWORK_ERROR_NOT_AUTHORIZED || error == NETWORK_ERROR_NOT_EXPECTED ||
				error == NETWORK_ERROR_PLAYER_MISMATCH) {
			// We made an error in the protocol, and our connection is closed.... :(
			_switch_mode_errorstr = Str.STR_NETWORK_ERR_SERVER_ERROR;
		} else if (error == NETWORK_ERROR_WRONG_REVISION) {
			// Wrong revision :(
			_switch_mode_errorstr = Str.STR_NETWORK_ERR_WRONG_REVISION;
		} else if (error == NETWORK_ERROR_WRONG_PASSWORD) {
			// Wrong password
			_switch_mode_errorstr = Str.STR_NETWORK_ERR_WRONG_PASSWORD;
		} else if (error == NETWORK_ERROR_KICKED) {
			_switch_mode_errorstr = Str.STR_NETWORK_ERR_KICKED;
		} else if (error == NETWORK_ERROR_CHEATER) {
			_switch_mode_errorstr = Str.STR_NETWORK_ERR_CHEATER;
		}

		Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);

		return NetworkRecvStatus.SERVER_ERROR;
	}

	static void NetworkPacketReceive_PACKET_SERVER_NEED_PASSWORD_command(NetworkClientState cs, Packet p)
	{
		NetworkPasswordType type;
		type = NetworkRecv_byte(MY_CLIENT(), p);

		if (type == NETWORK_GAME_PASSWORD) {
			ShowNetworkNeedGamePassword();
			return NetworkRecvStatus.OKAY;
		} else if (type == NETWORK_COMPANY_PASSWORD) {
			ShowNetworkNeedCompanyPassword();
			return NetworkRecvStatus.OKAY;
		}

		return NetworkRecvStatus.MALFORMED_PACKET;
	}

	static void NetworkPacketReceive_PACKET_SERVER_WELCOME_command(NetworkClientState cs, Packet p)
	{
		_network_own_client_index = NetworkRecv_int(MY_CLIENT(), p);

		// Start receiving the map
		NetworkPacketSend_PACKET_CLIENT_GETMAP_command();
		return NetworkRecvStatus.OKAY;
	}

	static void NetworkPacketReceive_PACKET_SERVER_WAIT_command(NetworkClientState cs, Packet p)
	{
		Net._network_join_status = NetworkJoinStatus.WAITING;
		Net._network_join_waiting = NetworkRecv_byte(MY_CLIENT(), p);
		Window.InvalidateWindow(Window.WC_NETWORK_STATUS_WINDOW, 0);

		// We are put on hold for receiving the map.. we need GUI for this ;)
		Global.DEBUG_net( 1, "[NET] The server is currently busy sending the map to someone else.. please hold..." );
		Global.DEBUG_net( 1, "[NET]  There are %d clients in front of you", Net._network_join_waiting);

		return NetworkRecvStatus.OKAY;
	}

	static void NetworkPacketReceive_PACKET_SERVER_MAP_command(NetworkClientState cs, Packet p)
	{
		static String filename;
		//static FILE *file_pointer;

		byte maptype;

		maptype = NetworkRecv_byte(MY_CLIENT(), p);

		if (MY_CLIENT().quited)
			return NetworkRecvStatus.CONN_LOST;

		// First packet, init some stuff
		if (maptype == MAP_PACKET_START) {
			// The name for the temp-map
			sprintf(filename, "%s%snetwork_client.tmp",  _path.autosave_dir, PATHSEP);

			file_pointer = fopen(filename, "wb");
			if (file_pointer == null) {
				_switch_mode_errorstr = Str.STR_NETWORK_ERR_SAVEGAMEERROR;
				return NetworkRecvStatus.SAVEGAME;
			}

			_frame_counter = _frame_counter_server = _frame_counter_max = NetworkRecv_int(MY_CLIENT(), p);

			Net._network_join_status = NetworkJoinStatus.DOWNLOADING;
			_network_join_kbytes = 0;
			_network_join_kbytes_total = NetworkRecv_int(MY_CLIENT(), p) / 1024;
			Window.InvalidateWindow(Window.WC_NETWORK_STATUS_WINDOW, 0);

			// The first packet does not contain any more data
			return NetworkRecvStatus.OKAY;
		}

		if (maptype == MAP_PACKET_NORMAL) {
			// We are still receiving data, put it to the file
			fwrite(p.buffer + p.pos, 1, p.size - p.pos, file_pointer);

			_network_join_kbytes = ftell(file_pointer) / 1024;
			Window.InvalidateWindow(Window.WC_NETWORK_STATUS_WINDOW, 0);
		}

		if (maptype == MAP_PACKET_PATCH) {
			NetworkRecvPatchSettings(MY_CLIENT(), p);
		}

		// Check if this was the last packet
		if (maptype == MAP_PACKET_END) {
			fclose(file_pointer);

			Net._network_join_status = NetworkJoinStatus.PROCESSING;
			Window.InvalidateWindow(Window.WC_NETWORK_STATUS_WINDOW, 0);

			// The map is done downloading, load it
			// Load the map
			if (!SafeSaveOrLoad(filename, SL_LOAD, GameModes.GM_NORMAL)) {
				Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);
				_switch_mode_errorstr = Str.STR_NETWORK_ERR_SAVEGAMEERROR;
				return NetworkRecvStatus.SAVEGAME;
			}

			GameOptions._opt_ptr = GameOptions._opt; // during a network game you are always in-game

			// Say we received the map and loaded it correctly!
			NetworkPacketSend_PACKET_CLIENT_MAP_OK_command();

			if (_network_playas == 0 || _network_playas > Global.MAX_PLAYERS ||
					!GetPlayer(_network_playas - 1).is_active) {

				if (_network_playas == Owner.OWNER_SPECTATOR) {
					// The client wants to be a spectator..
					Global.gs._local_player = Owner.OWNER_SPECTATOR;
					Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);
				} else {
					/* We have arrived and ready to start playing; send a command to make a new player;
					 * the server will give us a client-id and let us in */
					Global.gs._local_player = 0;
					NetworkSend_Command(0, 0, 0, Cmd.CMD_PLAYER_CTRL, null);
					Global.gs._local_player = Owner.OWNER_SPECTATOR;
				}
			} else {
				// take control over an existing company
				Global.gs._local_player = _network_playas - 1;
				Global._patches.autorenew = GetPlayer(Global.gs._local_player).engine_renew;
				Global._patches.autorenew_months = GetPlayer(Global.gs._local_player).engine_renew_months;
				Global._patches.autorenew_money = GetPlayer(Global.gs._local_player).engine_renew_money;
				Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);
			}

			/* Check if we are an ai-network-client, and if so, disable GUI */
			if (Ai._ai.network_client) {
				Ai._ai.network_playas = Global.gs._local_player;
				Global.gs._local_player      = Owner.OWNER_SPECTATOR;

				if (Ai._ai.network_playas != Owner.OWNER_SPECTATOR) {
					/* If we didn't join the game as a spectator, activate the AI */
					AI_StartNewAI(Ai._ai.network_playas);
				}
			}
		}

		return NetworkRecvStatus.OKAY;
	}

	static void NetworkPacketReceive_PACKET_SERVER_FRAME_command(NetworkClientState cs, Packet p)
	{
		_frame_counter_server = NetworkRecv_int(MY_CLIENT(), p);
		_frame_counter_max = NetworkRecv_int(MY_CLIENT(), p);
	//#ifdef ENABLE_NETWORK_SYNC_EVERY_FRAME
		// Test if the server supports this option
		//  and if we are at the frame the server is
		if (p.pos < p.size) {
			_sync_frame = _frame_counter_server;
			_sync_seed_1 = NetworkRecv_int(MY_CLIENT(), p);
	//#ifdef NETWORK_SEND_DOUBLE_SEED
	//		_sync_seed_2 = NetworkRecv_int(MY_CLIENT(), p);
	//#endif
		}
	//#endif
		Global.DEBUG_net( 7, "[NET] Received FRAME %d",_frame_counter_server);

		// Let the server know that we received this frame correctly
		//  We do this only once per day, to save some bandwidth ;)
		if (!_network_first_time && last_ack_frame < _frame_counter) {
			last_ack_frame = _frame_counter + DAY_TICKS;
			DEBUG(net,6, "[NET] Sent ACK at %d", _frame_counter);
			NetworkPacketSend_PACKET_CLIENT_ACK_command();
		}

		return NetworkRecvStatus.OKAY;
	}

	static void NetworkPacketReceive_PACKET_SERVER_SYNC_command(NetworkClientState cs, Packet p)
	{
		_sync_frame = NetworkRecv_int(MY_CLIENT(), p);
		_sync_seed_1 = NetworkRecv_int(MY_CLIENT(), p);
	//#ifdef NETWORK_SEND_DOUBLE_SEED
	//	_sync_seed_2 = NetworkRecv_int(MY_CLIENT(), p);
	//#endif

		return NetworkRecvStatus.OKAY;
	}

	static void NetworkPacketReceive_PACKET_SERVER_COMMAND_command(NetworkClientState cs, Packet p)
	{
		CommandPacket cp = new CommandPacket();
		cp.player = NetworkRecv_byte(MY_CLIENT(), p);
		cp.cmd = NetworkRecv_int(MY_CLIENT(), p);
		cp.p1 = NetworkRecv_int(MY_CLIENT(), p);
		cp.p2 = NetworkRecv_int(MY_CLIENT(), p);
		cp.tile = NetworkRecv_int(MY_CLIENT(), p);
		NetworkRecv_string(MY_CLIENT(), p, cp.text, sizeof(cp.text));
		cp.callback = NetworkRecv_byte(MY_CLIENT(), p);
		cp.frame = NetworkRecv_int(MY_CLIENT(), p);
		cp.next = null;

		// The server did send us this command..
		//  queue it in our own queue, so we can handle it in the upcoming frame!

		if (_local_command_queue == null) {
			_local_command_queue = cp;
		} else {
			// Find last packet
			CommandPacket c = _local_command_queue;
			while (c.next != null) c = c.next;
			c.next = cp;
		}

		return NetworkRecvStatus.OKAY;
	}

	static void NetworkPacketReceive_PACKET_SERVER_CHAT_command(NetworkClientState cs, Packet p)
	{
		NetworkAction action = NetworkRecv_byte(MY_CLIENT(), p);
		char msg[MAX_TEXT_MSG_LEN];
		NetworkClientInfo *ci = null, *ci_to;
		int index;
		char name[NETWORK_NAME_LENGTH];
		boolean self_send;

		index = NetworkRecv_int(MY_CLIENT(), p);
		self_send = NetworkRecv_byte(MY_CLIENT(), p);
		NetworkRecv_string(MY_CLIENT(), p, msg, MAX_TEXT_MSG_LEN);

		ci_to = NetworkFindClientInfoFromIndex(index);
		if (ci_to == null) return NetworkRecvStatus.OKAY;

		/* Do we display the action locally? */
		if (self_send) {
			switch (action) {
				case NETWORK_ACTION_CHAT_CLIENT:
					/* For speak to client we need the client-name */
					snprintf(name, sizeof(name), "%s", ci_to.client_name);
					ci = NetworkFindClientInfoFromIndex(_network_own_client_index);
					break;
				case NETWORK_ACTION_CHAT_PLAYER:
				case NETWORK_ACTION_GIVE_MONEY:
					/* For speak to player or give money, we need the player-name */
					if (ci_to.client_playas > Global.MAX_PLAYERS)
						return NetworkRecvStatus.OKAY; // This should never happen
					Global.GetString(name, GetPlayer(ci_to.client_playas-1).name_1);
					ci = NetworkFindClientInfoFromIndex(_network_own_client_index);
					break;
				default:
					/* This should never happen */
					NOT_REACHED();
					break;
			}
		} else {
			/* Display message from somebody else */
			snprintf(name, sizeof(name), "%s", ci_to.client_name);
			ci = ci_to;
		}

		if (ci != null)
			NetworkTextMessage(action, GetDrawStringPlayerColor(ci.client_playas-1), self_send, name, "%s", msg);
		return NetworkRecvStatus.OKAY;
	}

	static void NetworkPacketReceive_PACKET_SERVER_ERROR_QUIT_command(NetworkClientState cs, Packet p)
	{
		int errorno;
		char str[100];
		int index;
		NetworkClientInfo *ci;

		index = NetworkRecv_int(MY_CLIENT(), p);
		errorno = NetworkRecv_byte(MY_CLIENT(), p);

		Global.GetString(str, Str.STR_NETWORK_ERR_CLIENT_GENERAL + errorno);

		ci = NetworkFindClientInfoFromIndex(index);
		if (ci != null) {
			NetworkTextMessage(NETWORK_ACTION_LEAVE, 1, false, ci.client_name, "%s", str);

			// The client is gone, give the NetworkClientInfo free
			ci.client_index = NETWORK_EMPTY_INDEX;
		}

		Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

		return NetworkRecvStatus.OKAY;
	}

	static void NetworkPacketReceive_PACKET_SERVER_QUIT_command(NetworkClientState cs, Packet p)
	{
		char str[100];
		int index;
		NetworkClientInfo *ci;

		index = NetworkRecv_int(MY_CLIENT(), p);
		NetworkRecv_string(MY_CLIENT(), p, str, lengthof(str));

		ci = NetworkFindClientInfoFromIndex(index);
		if (ci != null) {
			NetworkTextMessage(NETWORK_ACTION_LEAVE, 1, false, ci.client_name, "%s", str);

			// The client is gone, give the NetworkClientInfo free
			ci.client_index = NETWORK_EMPTY_INDEX;
		} else {
			Global.DEBUG_net( 0, "[NET] Error - unknown client (%d) is leaving the game", index);
		}

		Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

		// If we come here it means we could not locate the client.. strange :s
		return NetworkRecvStatus.OKAY;
	}

	static void NetworkPacketReceive_PACKET_SERVER_JOIN_command(NetworkClientState cs, Packet p)
	{
		int index;
		NetworkClientInfo *ci;

		index = NetworkRecv_int(MY_CLIENT(), p);

		ci = NetworkFindClientInfoFromIndex(index);
		if (ci != null)
			NetworkTextMessage(NETWORK_ACTION_JOIN, 1, false, ci.client_name, "");

		Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

		return NetworkRecvStatus.OKAY;
	}

	static void NetworkPacketReceive_PACKET_SERVER_SHUTDOWN_command(NetworkClientState cs, Packet p)
	{
		_switch_mode_errorstr = Str.STR_NETWORK_SERVER_SHUTDOWN;

		return NetworkRecvStatus.SERVER_ERROR;
	}

	static void NetworkPacketReceive_PACKET_SERVER_NEWGAME_command(NetworkClientState cs, Packet p)
	{
		// To trottle the reconnects a bit, every clients waits
		//  his Global.gs._local_player value before reconnecting
		// Owner.OWNER_SPECTATOR is currently 255, so to avoid long wait periods
		//  set the max to 10.
		_network_reconnect = Math.min(Global.gs._local_player + 1, 10);
		_switch_mode_errorstr = Str.STR_NETWORK_SERVER_REBOOT;

		return NetworkRecvStatus.SERVER_ERROR;
	}

	static void NetworkPacketReceive_PACKET_SERVER_RCON_command(NetworkClientState cs, Packet p)
	{
		char rcon_out[NETWORK_RCONCOMMAND_LENGTH];
		int color_code;

		color_code = NetworkRecv_int(MY_CLIENT(), p);
		NetworkRecv_string(MY_CLIENT(), p, rcon_out, sizeof(rcon_out));

		IConsolePrint(color_code, rcon_out);

		return NetworkRecvStatus.OKAY;
	}



	// The layout for the receive-functions by the client
	//typedef NetworkRecvStatus NetworkClientPacket(Packet p);

	// This array matches PacketType. At an incoming
	//  packet it is matches against this array
	//  and that way the right function to handle that
	//  packet is found.
	static NetworkClientPacket _network_client_packet[] = {
		RECEIVE_COMMAND(PACKET_SERVER_FULL),
		RECEIVE_COMMAND(PACKET_SERVER_BANNED),
		null, /*PACKET_CLIENT_JOIN,*/
		RECEIVE_COMMAND(PACKET_SERVER_ERROR),
		null, /*PACKET_CLIENT_COMPANY_INFO,*/
		RECEIVE_COMMAND(PACKET_SERVER_COMPANY_INFO),
		RECEIVE_COMMAND(PACKET_SERVER_CLIENT_INFO),
		RECEIVE_COMMAND(PACKET_SERVER_NEED_PASSWORD),
		null, /*PACKET_CLIENT_PASSWORD,*/
		RECEIVE_COMMAND(PACKET_SERVER_WELCOME),
		null, /*PACKET_CLIENT_GETMAP,*/
		RECEIVE_COMMAND(PACKET_SERVER_WAIT),
		RECEIVE_COMMAND(PACKET_SERVER_MAP),
		null, /*PACKET_CLIENT_MAP_OK,*/
		RECEIVE_COMMAND(PACKET_SERVER_JOIN),
		RECEIVE_COMMAND(PACKET_SERVER_FRAME),
		RECEIVE_COMMAND(PACKET_SERVER_SYNC),
		null, /*PACKET_CLIENT_ACK,*/
		null, /*PACKET_CLIENT_COMMAND,*/
		RECEIVE_COMMAND(PACKET_SERVER_COMMAND),
		null, /*PACKET_CLIENT_CHAT,*/
		RECEIVE_COMMAND(PACKET_SERVER_CHAT),
		null, /*PACKET_CLIENT_SET_PASSWORD,*/
		null, /*PACKET_CLIENT_SET_NAME,*/
		null, /*PACKET_CLIENT_QUIT,*/
		null, /*PACKET_CLIENT_ERROR,*/
		RECEIVE_COMMAND(PACKET_SERVER_QUIT),
		RECEIVE_COMMAND(PACKET_SERVER_ERROR_QUIT),
		RECEIVE_COMMAND(PACKET_SERVER_SHUTDOWN),
		RECEIVE_COMMAND(PACKET_SERVER_NEWGAME),
		RECEIVE_COMMAND(PACKET_SERVER_RCON),
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
		final SettingDesc item;

		item = patch_settings;

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
	static void NetworkClient_Connected()
	{
		// Set the frame-counter to 0 so nothing happens till we are ready
		_frame_counter = 0;
		_frame_counter_server = 0;
		last_ack_frame = 0;
		// Request the game-info
		NetworkPacketSend_ ## type ## _command(PACKET_CLIENT_JOIN)();
	}

	// Reads the packets from the socket-stream, if available
	NetworkRecvStatus NetworkClient_ReadPackets(NetworkClientState cs)
	{
		Packet p;
		NetworkRecvStatus res = NetworkRecvStatus.OKAY;

		while (res == NetworkRecvStatus.OKAY && (p = NetworkRecv_Packet(cs, &res)) != null) {
			byte type = NetworkRecv_byte(MY_CLIENT(), p);
			if (type < PACKET_END && _network_client_packet[type] != null && !MY_CLIENT().quited) {
				res = _network_client_packet[type](p);
			} else {
				res = NetworkRecvStatus.MALFORMED_PACKET;
				Global.DEBUG_net( 0, "[NET][client] Received invalid packet type %d", type);
			}

			//free(p);
		}

		return res;
	}


}
