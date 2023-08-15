package com.dzavalishin.console;

import java.io.IOException;

import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Player;
import com.dzavalishin.net.Net;
import com.dzavalishin.net.NetServer;
import com.dzavalishin.net.NetworkClientInfo;
import com.dzavalishin.net.NetworkErrorCode;
import com.dzavalishin.net.NetClient;

public class ConsoleCmds extends DefaultConsole
{

	// ** scriptfile handling ** //
	//static FILE *_script_file;
	static boolean _script_running;

	// ** console command / variable defines ** //

	// Also use from within player_gui to change the password graphically 
	public static boolean NetworkChangeCompanyPassword(String ... argv)
	{
		int  lpid = Global.gs._local_player.id;
		
		if (argv.length == 0) {
			if (lpid >= Global.MAX_PLAYERS) return true; // dedicated server
			IConsolePrintF(_icolour_warn, "Current value for 'company_pw': %s", Net._network_player_info[lpid].password);
			return true;
		}

		if (lpid >= Global.MAX_PLAYERS) {
			IConsoleError("You have to own a company to make use of this command.");
			return false;
		}

		if (argv.length != 1) return false;

		if (argv[0].equals("*"))
			argv[0] = "";

		Net._network_player_info[lpid].password = argv[0];

		if (!Global._network_server)
			try {
				NetClient.NetworkPacketSend_PACKET_CLIENT_SET_PASSWORD_command(Net._network_player_info[lpid].password);
			} catch (IOException e) {
				Global.error(e);
			}

		IConsolePrintF(_icolour_warn, "'company_pw' changed to:  %s", Net._network_player_info[lpid].password);

		return true;
	}
	

	/* **************************** */
	/* variable and command hooks   */
	/* **************************** */

	//*#ifdef ENABLE_NETWORK

	static boolean NetworkAvailable()
	{
		if (!Global._network_available) {
			IConsoleError("You cannot use this command because there is no network available.");
			return false;
		}
		return true;
	}

	static boolean ConHookServerOnly()
	{
		if (!NetworkAvailable()) return false;

		if (!Global._network_server) {
			IConsoleError("This command/variable is only available to a network server.");
			return false;
		}
		return true;
	}

	static boolean ConHookClientOnly()
	{
		if (!NetworkAvailable()) return false;

		if (Global._network_server) {
			IConsoleError("This command/variable is not available to a network server.");
			return false;
		}
		return true;
	}

	static boolean ConHookNeedNetwork()
	{
		if (!NetworkAvailable()) return false;

		if (!Global._networking) {
			IConsoleError("Not connected. This command/variable is only available in multiplayer.");
			return false;
		}
		return true;
	}

	static boolean ConHookNoNetwork()
	{
		if (Global._networking) {
			IConsoleError("This command/variable is forbidden in multiplayer.");
			return false;
		}
		return true;
	}

	//#endif /* ENABLE_NETWORK */

	static void IConsoleHelp(String str)
	{
		DefaultConsole.IConsolePrintF(DefaultConsole._icolour_warn, "- %s", str);
	}


	/*
	//extern boolean SafeSaveOrLoad(String filename, int mode, int newgm);
	//extern void BuildFileList(void);
	//extern void SetFiosType(final byte fiostype);

	// Save the map to a file 
	static boolean function(String ... argv)(ConSave)
	{
		if (argv.length == 0) {
			IConsoleHelp("Save the current game. Usage: 'save <filename>'");
			return true;
		}

		if (argv.length == 2) {
			char buf[200];

			snprintf(buf, lengthof(buf), "%s%s%s.sav", _path.save_dir, PATHSEP, argv[1]);
			IConsolePrint(_icolour_def, "Saving map...");

			if (SaveOrLoad(buf, SL_SAVE) != SL_OK) {
				IConsolePrint(_icolour_err, "SaveMap failed");
			} else
				IConsolePrintF(_icolour_def, "Map sucessfully saved to %s", buf);
			return true;
		}

		return false;
	}

	static final FiosItem* GetFiosItem(final char* file)
	{
		int i;

		_saveload_mode = SLD_LOAD_GAME;
		BuildFileList();

		for (i = 0; i < _fios_num; i++) {
			if (strcmp(file, _fios_list[i].name) == 0) break;
			if (strcmp(file, _fios_list[i].title) == 0) break;
		}

		if (i == _fios_num) { // If no name matches, try to parse it as number 
			char* endptr;

			i = strtol(file, &endptr, 10);
			if (file == endptr || *endptr != '\0') i = -1;
		}

		return IS_INT_INSIDE(i, 0, _fios_num) ? &_fios_list[i] : null;
	}


	static boolean function(String ... argv)(ConLoad)
	{
		final FiosItem *item;
		String file;

		if (argv.length == 0) {
			IConsoleHelp("Load a game by name or index. Usage: 'load <file | number>'");
			return true;
		}

		if (argv.length != 2) return false;

		file = argv[1];
		item = GetFiosItem(file);
		if (item != null) {
			switch (item.type) {
				case FIOS_TYPE_FILE: case FIOS_TYPE_OLDFILE: {
					_switch_mode = SM_LOAD;
					SetFiosType(item.type);

					ttd_strlcpy(_file_to_saveload.name, FiosBrowseTo(item), sizeof(_file_to_saveload.name));
					ttd_strlcpy(_file_to_saveload.title, item.title, sizeof(_file_to_saveload.title));
				} break;
				default: IConsolePrintF(_icolour_err, "%s: Not a savegame.", file);
			}
		} else
			IConsolePrintF(_icolour_err, "%s: No such file or directory.", file);

		FiosFreeSavegameList();
		return true;
	}


	static boolean function(String ... argv)(ConRemove)
	{
		final FiosItem* item;
		final char* file;

		if (argv.length == 0) {
			IConsoleHelp("Remove a savegame by name or index. Usage: 'rm <file | number>'");
			return true;
		}

		if (argv.length != 2) return false;

		file = argv[1];
		item = GetFiosItem(file);
		if (item != null) {
			if (!FiosDelete(item.name))
				IConsolePrintF(_icolour_err, "%s: Failed to delete file", file);
		} else
			IConsolePrintF(_icolour_err, "%s: No such file or directory.", file);

		FiosFreeSavegameList();
		return true;
	}


	// List all the files in the current dir via console 
	static boolean function(String ... argv)(ConListFiles)
	{
		int i;

		if (argv.length == 0) {
			IConsoleHelp("List all loadable savegames and directories in the current dir via console. Usage: 'ls | dir'");
			return true;
		}

		BuildFileList();

		for (i = 0; i < _fios_num; i++) {
			final FiosItem *item = &_fios_list[i];
			IConsolePrintF(_icolour_def, "%d) %s", i, item.title);
		}

		FiosFreeSavegameList();
		return true;
	}

	// Change the dir via console 
	static boolean function(String ... argv)(ConChangeDirectory)
	{
		final FiosItem *item;
		String file;

		if (argv.length == 0) {
			IConsoleHelp("Change the dir via console. Usage: 'cd <directory | number>'");
			return true;
		}

		if (argv.length != 2) return false;

		file = argv[1];
		item = GetFiosItem(file);
		if (item != null) {
			switch (item.type) {
				case FIOS_TYPE_DIR: case FIOS_TYPE_DRIVE: case FIOS_TYPE_PARENT:
					FiosBrowseTo(item);
					break;
				default: IConsolePrintF(_icolour_err, "%s: Not a directory.", file);
			}
		} else
			IConsolePrintF(_icolour_err, "%s: No such file or directory.", file);

		FiosFreeSavegameList();
		return true;
	}
	*/
	// ********************************* //
	// * Network Core Console Commands * //
	// ********************************* //
	//#ifdef ENABLE_NETWORK

	// TODO convert to new style command classes

	static boolean ConUnBan(String ... argv)
	{
		int i, index;

		if (argv.length == 0) {
			IConsoleHelp("Unban a player from a network game. Usage: 'unban <ip | id>'");
			IConsoleHelp("For a list of banned IP's, see the command 'banlist'");
			return true;
		}

		if (argv.length != 2) return false;

		index = (argv[1].indexOf('.') < 0) ? Integer.parseInt(argv[1]) : 0;
		index--;

		for (i = 0; i < Net._network_ban_list.length; i++) {
			if (Net._network_ban_list[i] == null || Net._network_ban_list[i].isBlank())
				continue;

			if (Net._network_ban_list[i].equals(argv[1]) || index == i) {
				Net._network_ban_list[i] = null;
				IConsolePrint(_icolour_def, "IP unbanned.");
				return true;
			}
		}

		IConsolePrint(_icolour_def, "IP not in ban-list.");
		return true;
	}

	static boolean ConBanList(String ... argv)
	{
		if (argv.length == 0) {
			IConsoleHelp("List the IP's of banned clients: Usage 'banlist'");
			return true;
		}

		IConsolePrint(_icolour_def, "Banlist: ");

		for (int i = 0; i < Net._network_ban_list.length; i++) {
			if (Net._network_ban_list[i] == null || Net._network_ban_list[i].isBlank())
				continue;

			IConsolePrintF(_icolour_def, "  %d) %s", i + 1, Net._network_ban_list[i]);
		}

		return true;
	}

	static boolean ConPauseGame(String ... argv)
	{
		if (argv.length == 0) {
			IConsoleHelp("Pause a network game. Usage: 'pause'");
			return true;
		}

		if (Global._pause == 0) {
			Cmd.DoCommandP(null, 1, 0, null, Cmd.CMD_PAUSE);
			IConsolePrint(_icolour_def, "Game paused.");
		} else
			IConsolePrint(_icolour_def, "Game is already paused.");

		return true;
	}

	static boolean ConUnPauseGame(String ... argv)
	{
		if (argv.length == 0) {
			IConsoleHelp("Unpause a network game. Usage: 'unpause'");
			return true;
		}

		if (Global._pause != 0) {
			Cmd.DoCommandP(null, 0, 0, null, Cmd.CMD_PAUSE);
			IConsolePrint(_icolour_def, "Game unpaused.");
		} else
			IConsolePrint(_icolour_def, "Game is already unpaused.");

		return true;
	}


	

	static boolean ConKick(String ... argv)
	{
		NetworkClientInfo ci;
		int index;

		if (argv.length == 0) {
			IConsoleHelp("Kick a player from a network game. Usage: 'kick <client-id>'");
			IConsoleHelp("For client-id's, see the command 'clients'");
			return true;
		}

		if (argv.length != 2) return false;

		index = Integer.parseInt(argv[1]);
		if (index == Net.NETWORK_SERVER_INDEX) {
			IConsolePrint(_icolour_def, "Silly boy, you can not kick yourself!");
			return true;
		}
		if (index == 0) {
			IConsoleError("Invalid client-id");
			return true;
		}

		ci = Net.NetworkFindClientInfoFromIndex(index);

		if (ci != null) {
			//SEND_COMMAND(PACKET_SERVER_ERROR)(NetworkFindClientStateFromIndex(index), NetworkErrorCode.KICKED);
			NetServer.NetworkPacketSend_PACKET_SERVER_ERROR_command(Net.NetworkFindClientStateFromIndex(index), NetworkErrorCode.KICKED);
		} else
			IConsoleError("Client-id not found");

		return true;
	}

	static boolean ConResetCompany(String ... argv)
	{
		Player p;
		//NetworkClientState cs;
		NetworkClientInfo ci;

		if (argv.length == 0) {
			IConsoleHelp("Remove an idle company from the game. Usage: 'reset_company <company-id>'");
			IConsoleHelp("For company-id's, see the list of companies from the dropdown menu. Player 1 is 1, etc.");
			return true;
		}

		if (argv.length != 2) return false;

		int index = Integer.parseInt(argv[1]);

		// Check valid range 
		if (index < 1 || index > Global.MAX_PLAYERS) {
			IConsolePrintF(_icolour_err, "Company does not exist. Company-id must be between 1 and %d.", Global.MAX_PLAYERS);
			return true;
		}

		// Check if company does exist 
		index--;
		p = Player.GetPlayer(index);
		if (!p.isActive()) {
			IConsoleError("Company does not exist.");
			return true;
		}

		if (p.isAi()) {
			IConsoleError("Company is owned by an AI.");
			return true;
		}

		Net.companyHasPlayers(index);
		
		// TODO [dz] does NetworkFindClientInfoFromIndex really return server record for Net.NETWORK_SERVER_INDEX?
		ci = Net.NetworkFindClientInfoFromIndex(Net.NETWORK_SERVER_INDEX);
		if (ci.client_playas - 1 == index) {
			IConsoleError("Cannot remove company: the server is connected to that company.");
			return true;
		}

		// It is safe to remove this company 
		Cmd.DoCommandP(null, 2, index, null, Cmd.CMD_PLAYER_CTRL);
		IConsolePrint(_icolour_def, "Company deleted.");

		return true;
	}



	//#endif /* ENABLE_NETWORK */

	/* ******************************** */
	/*   script file console commands   */
	/* ******************************** */
	/*
	static boolean function(String ... argv)(ConExec)
	{
		char cmdline[ICON_CMDLN_SIZE];
		char *cmdptr;

		if (argv.length == 0) {
			IConsoleHelp("Execute a local script file. Usage: 'exec <script> <?>'");
			return true;
		}

		if (argv.length < 2) return false;

		_script_file = fopen(argv[1], "r");

		if (_script_file == null) {
			if (argv.length == 2 || atoi(argv[2]) != 0) IConsoleError("script file not found");
			return true;
		}

		_script_running = true;

		while (_script_running && fgets(cmdline, sizeof(cmdline), _script_file) != null) {
			// Remove newline characters from the executing script 
			for (cmdptr = cmdline; *cmdptr != '\0'; cmdptr++) {
				if (*cmdptr == '\n' || *cmdptr == '\r') {
					*cmdptr = '\0';
					break;
				}
			}
			IConsoleCmdExec(cmdline);
		}

		if (ferror(_script_file))
			IConsoleError("Encountered errror while trying to read from script file");

		_script_running = false;
		fclose(_script_file);
		return true;
	}

	static boolean function(String ... argv)(ConReturn)
	{
		if (argv.length == 0) {
			IConsoleHelp("Stop executing a running script. Usage: 'return'");
			return true;
		}

		_script_running = false;
		return true;
	}

	/* **************************** */
	/*   default console commands   */
	/* **************************** */
	//extern boolean CloseConsoleLogIfActive(void);
	/*
	static boolean function(String ... argv)(ConScript)
	{
		extern FILE* _iconsole_output_file;

		if (argv.length == 0) {
			IConsoleHelp("Start or stop logging console output to a file. Usage: 'script <filename>'");
			IConsoleHelp("If filename is omitted, a running log is stopped if it is active");
			return true;
		}

		if (!CloseConsoleLogIfActive()) {
			if (argv.length < 2) return false;

			IConsolePrintF(_icolour_def, "file output started to: %s", argv[1]);
			_iconsole_output_file = fopen(argv[1], "ab");
			if (_iconsole_output_file == null) IConsoleError("could not open file");
		}

		return true;
	}


	extern void SwitchMode(int new_mode);

	static boolean function(String ... argv)(ConNewGame)
	{
		if (argv.length == 0) {
			IConsoleHelp("Start a new game. Usage: 'newgame'");
			IConsoleHelp("The server can force a new game using 'newgame', any client using it will part and start a single-player game");
			return true;
		}

		GenRandomNewGame(Random(), InteractiveRandom());
		return true;
	}
	*/

	/*
	static boolean function(String ... argv)(ConScreenShot)
	{
		if (argv.length == 0) {
			IConsoleHelp("Create a screenshot of the game. Usage: 'screenshot [big | no_con]'");
			IConsoleHelp("'big' makes a screenshot of the whole map, 'no_con' hides the console to create the screenshot");
			return true;
		}

		if (argv.length > 3) return false;

		_make_screenshot = 1;
		if (argv.length > 1) {
			if (strcmp(argv[1], "big") == 0 || (argv.length == 3 && strcmp(argv[2], "big") == 0))
				_make_screenshot = 2;

			if (strcmp(argv[1], "no_con") == 0 || (argv.length == 3 && strcmp(argv[2], "no_con") == 0))
				IConsoleClose();
		}

		return true;
	}

	static boolean function(String ... argv)(ConInfoVar)
	{
		static String _icon_vartypes[] = {"boolean", "byte", "uint16", "uint32", "int16", "int32", "string"};
		final IConsoleVar *var;

		if (argv.length == 0) {
			IConsoleHelp("Print out debugging information about a variable. Usage: 'info_var <var>'");
			return true;
		}

		if (argv.length < 2) return false;

		var = IConsoleVarGet(argv[1]);
		if (var == null) {
			IConsoleError("the given variable was not found");
			return true;
		}

		IConsolePrintF(_icolour_def, "variable name: %s", var.name);
		IConsolePrintF(_icolour_def, "variable type: %s", _icon_vartypes[var.type]);
		IConsolePrintF(_icolour_def, "variable addr: 0x%X", var.addr);

		if (var.hook.access) IConsoleWarning("variable is access hooked");
		if (var.hook.pre) IConsoleWarning("variable is pre hooked");
		if (var.hook.post) IConsoleWarning("variable is post hooked");
		return true;
	}


	static boolean function(String ... argv)(ConInfoCmd)
	{
		final IConsoleCmd *cmd;

		if (argv.length == 0) {
			IConsoleHelp("Print out debugging information about a command. Usage: 'info_cmd <cmd>'");
			return true;
		}

		if (argv.length < 2) return false;

		cmd = IConsoleCmdGet(argv[1]);
		if (cmd == null) {
			IConsoleError("the given command was not found");
			return true;
		}

		IConsolePrintF(_icolour_def, "command name: %s", cmd.name);
		IConsolePrintF(_icolour_def, "command proc: 0x%X", cmd.proc);

		if (cmd.hook.access) IConsoleWarning("command is access hooked");
		if (cmd.hook.pre) IConsoleWarning("command is pre hooked");
		if (cmd.hook.post) IConsoleWarning("command is post hooked");

		return true;
	}

	static boolean function(String ... argv)(ConDebugLevel)
	{
		if (argv.length == 0) {
			IConsoleHelp("Get/set the default debugging level for the game. Usage: 'debug_level [<level>]'");
			IConsoleHelp("Level can be any combination of names, levels. Eg 'net=5 ms=4'. Remember to enclose it in \"'s");
			return true;
		}

		if (argv.length > 2) return false;

		if (argv.length == 1) {
			IConsolePrintF(_icolour_def, "Current debug-level: '%s'", GetDebugString());
		} else SetDebugString(argv[1]);

		return true;
	}

	static boolean function(String ... argv)(ConPart)
	{
		if (argv.length == 0) {
			IConsoleHelp("Leave the currently joined/running game (only ingame). Usage: 'part'");
			return true;
		}

		if (_game_mode != GM_NORMAL) return false;

		_switch_mode = SM_MENU;
		return true;
	}
*/

	/*
	#ifdef ENABLE_NETWORK

	static boolean function(String ... argv)(ConSay)
	{
		if (argv.length == 0) {
			IConsoleHelp("Chat to your fellow players in a multiplayer game. Usage: 'say \"<msg>\"'");
			return true;
		}

		if (argv.length != 2) return false;

		if (!_network_server) {
			SEND_COMMAND(PACKET_CLIENT_CHAT)(NETWORK_ACTION_CHAT, DESTTYPE_BROADCAST, 0 /* param does not matter * /, argv[1]);
		} else
			NetworkServer_HandleChat(NETWORK_ACTION_CHAT, DESTTYPE_BROADCAST, 0, argv[1], NETWORK_SERVER_INDEX);

		return true;
	}

	static boolean function(String ... argv)(ConSayPlayer)
	{
		if (argv.length == 0) {
			IConsoleHelp("Chat to a certain player in a multiplayer game. Usage: 'say_player <player-no> \"<msg>\"'");
			IConsoleHelp("PlayerNo is the player that plays as company <playerno>, 1 through max_players");
			return true;
		}

		if (argv.length != 3) return false;

		if (atoi(argv[1]) < 1 || atoi(argv[1]) > MAX_PLAYERS) {
			IConsolePrintF(_icolour_def, "Unknown player. Player range is between 1 and %d.", MAX_PLAYERS);
			return true;
		}

		if (!_network_server) {
			SEND_COMMAND(PACKET_CLIENT_CHAT)(NETWORK_ACTION_CHAT_PLAYER, DESTTYPE_PLAYER, atoi(argv[1]), argv[2]);
		} else
			NetworkServer_HandleChat(NETWORK_ACTION_CHAT_PLAYER, DESTTYPE_PLAYER, atoi(argv[1]), argv[2], NETWORK_SERVER_INDEX);

		return true;
	}

	static boolean function(String ... argv)(ConSayClient)
	{
		if (argv.length == 0) {
			IConsoleHelp("Chat to a certain player in a multiplayer game. Usage: 'say_client <client-no> \"<msg>\"'");
			IConsoleHelp("For client-id's, see the command 'clients'");
			return true;
		}

		if (argv.length != 3) return false;

		if (!_network_server) {
			SEND_COMMAND(PACKET_CLIENT_CHAT)(NETWORK_ACTION_CHAT_CLIENT, DESTTYPE_CLIENT, atoi(argv[1]), argv[2]);
		} else
			NetworkServer_HandleChat(NETWORK_ACTION_CHAT_CLIENT, DESTTYPE_CLIENT, atoi(argv[1]), argv[2], NETWORK_SERVER_INDEX);

		return true;
	}

	static boolean ConHookServerPW()
	{
		if (strncmp(_network_server_password, "*", NETWORK_PASSWORD_LENGTH) == 0) {
			_network_server_password[0] = '\0';
			_network_game_info.use_password = 0;
		} else
			_network_game_info.use_password = 1;

		return true;
	}

	static boolean ConHookRconPW()
	{
		if (strncmp(_network_rcon_password, "*", NETWORK_PASSWORD_LENGTH) == 0)
			_network_rcon_password[0] = '\0';

		ttd_strlcpy(_network_game_info.rcon_password, _network_rcon_password, sizeof(_network_game_info.rcon_password));

		return true;
	}


	static boolean ConProcPlayerName()
	{
		NetworkClientInfo *ci = NetworkFindClientInfoFromIndex(_network_own_client_index);

		if (ci == null) return false;

		// Don't change the name if it is the same as the old name
		if (strcmp(ci.client_name, _network_player_name) != 0) {
			if (!_network_server) {
				SEND_COMMAND(PACKET_CLIENT_SET_NAME)(_network_player_name);
			} else {
				if (NetworkFindName(_network_player_name)) {
					NetworkTextMessage(NETWORK_ACTION_NAME_CHANGE, 1, false, ci.client_name, "%s", _network_player_name);
					ttd_strlcpy(ci.client_name, _network_player_name, sizeof(ci.client_name));
					NetworkUpdateClientInfo(NETWORK_SERVER_INDEX);
				}
			}
		}

		return true;
	}

	static boolean ConHookServerName()
	{
		ttd_strlcpy(_network_game_info.server_name, _network_server_name, sizeof(_network_game_info.server_name));
		return true;
	}

	static boolean ConHookServerAdvertise()
	{
		if (!_network_advertise) // remove us from advertising
			NetworkUDPRemoveAdvertise();

		return true;
	}

	static boolean function(String ... argv)(ConProcServerIP)
	{
		if (argv.length == 0) {
			IConsolePrintF(_icolour_warn, "Current value for 'server_ip': %s", inet_ntoa(*(struct in_addr *)&_network_server_bind_ip));
			return true;
		}

		if (argv.length != 1) return false;

		_network_server_bind_ip = (strcmp(argv[0], "all") == 0) ? inet_addr("0.0.0.0") : inet_addr(argv[0]);
		snprintf(_network_server_bind_ip_host, sizeof(_network_server_bind_ip_host), "%s", inet_ntoa(*(struct in_addr *)&_network_server_bind_ip));
		IConsolePrintF(_icolour_warn, "'server_ip' changed to:  %s", inet_ntoa(*(struct in_addr *)&_network_server_bind_ip));
		return true;
	}

	#endif /* ENABLE_NETWORK */

//	static boolean ConListDumpVariables(String ... argv)
//	{
//
//		if (argv.length == 0) {
//			IConsoleHelp("List all variables with their value. Usage: 'dump_vars [<pre-filter>]'");
//			return true;
//		}
//
//
//		for (IConsoleVar ivar : Console._iconsole_vars.values()) {
//			if (argv[1] == null || ivar.name.equals(argv[1]))
//				ivar.IConsoleVarPrintGetValue();
//		}
//
//		return true;
//	}


	//#ifdef _DEBUG
	/* ****************************************** */
	/*  debug commands and variables */
	/* ****************************************** */

	static void IConsoleDebugLibRegister()
	{
		// debugging variables and functions
		//extern boolean _stdlib_con_developer; 

		//IConsoleVarRegister("con_developer",    &_stdlib_con_developer, ICONSOLE_VAR_BOOLEAN, "Enable/disable console debugging information (internal)");
		//IConsoleAliasRegister("dbg_echo",       "echo %A; echo %B");
		//IConsoleAliasRegister("dbg_echo2",      "echo %!");
	}
	//#endif

	/* ****************************************** */
	/*  console command and variable registration */
	/* ****************************************** */

	public static void IConsoleStdLibRegister()
	{
		// stdlib

		// default variables and functions
		/*
		IConsoleCmdRegister("debug_level",  ConDebugLevel);
		IConsoleCmdRegister("dump_vars",    ConListDumpVariables);
		IConsoleCmdRegister("exec",         ConExec);
		IConsoleCmdRegister("part",         ConPart);
		IConsoleCmdRegister("info_cmd",     ConInfoCmd);
		IConsoleCmdRegister("info_var",     ConInfoVar);
		IConsoleCmdRegister("newgame",      ConNewGame);
		IConsoleCmdRegister("return",       ConReturn);
		IConsoleCmdRegister("screenshot",   ConScreenShot);
		IConsoleCmdRegister("script",       ConScript);
		IConsoleCmdRegister("load",         ConLoad);
		IConsoleCmdRegister("rm",           ConRemove);
		IConsoleCmdRegister("save",         ConSave);
		IConsoleCmdRegister("ls",           ConListFiles);
		IConsoleCmdRegister("cd",           ConChangeDirectory);
		*/

		/*
		IConsoleAliasRegister("dir",      "ls");
		IConsoleAliasRegister("del",      "rm %+");
		IConsoleAliasRegister("newmap",   "newgame");
		IConsoleAliasRegister("new_map",  "newgame");
		IConsoleAliasRegister("new_game", "newgame");
		*/

		/* networking variables and functions */
		/*#ifdef ENABLE_NETWORK
		// Networking commands 
		IConsoleCmdRegister("say",             ConSay);
		IConsoleCmdHookAdd("say",              ICONSOLE_HOOK_ACCESS, ConHookNeedNetwork);
		IConsoleCmdRegister("say_player",      ConSayPlayer);
		IConsoleCmdHookAdd("say_player",       ICONSOLE_HOOK_ACCESS, ConHookNeedNetwork);
		IConsoleCmdRegister("say_client",      ConSayClient);
		IConsoleCmdHookAdd("say_client",       ICONSOLE_HOOK_ACCESS, ConHookNeedNetwork);
		IConsoleCmdRegister("kick",            ConKick);
		IConsoleCmdHookAdd("kick",             ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleCmdRegister("reset_company",   ConResetCompany);
		IConsoleCmdHookAdd("reset_company",    ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleAliasRegister("clean_company", "reset_company %A");
		IConsoleCmdRegister("connect",         ConNetworkConnect);
		IConsoleAliasRegister("join",          "connect %A");
		IConsoleCmdHookAdd("connect",          ICONSOLE_HOOK_ACCESS, ConHookClientOnly);
		IConsoleCmdRegister("clients",         ConNetworkClients);
		IConsoleCmdHookAdd("clients",          ICONSOLE_HOOK_ACCESS, ConHookNeedNetwork);
		IConsoleCmdRegister("status",          constatus);
		IConsoleCmdHookAdd("status",           ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleCmdHookAdd("resetengines",     ICONSOLE_HOOK_ACCESS, ConHookNoNetwork);
		IConsoleCmdHookAdd("stopall",          ICONSOLE_HOOK_ACCESS, ConHookNoNetwork);

		IConsoleCmdRegister("rcon",            ConRcon);
		IConsoleCmdHookAdd("rcon",             ICONSOLE_HOOK_ACCESS, ConHookNeedNetwork);

		IConsoleCmdRegister("ban",             ConBan);
		IConsoleCmdHookAdd("ban",              ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleCmdRegister("unban",           ConUnBan);
		IConsoleCmdHookAdd("unban",            ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleCmdRegister("banlist",         ConBanList);
		IConsoleCmdHookAdd("banlist",          ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleCmdRegister("pause",           ConPauseGame);
		IConsoleCmdHookAdd("pause",            ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleCmdRegister("unpause",         ConUnPauseGame);
		IConsoleCmdHookAdd("unpause",          ICONSOLE_HOOK_ACCESS, ConHookServerOnly);

		IConsoleCmdRegister("patch",           ConPatch);
		IConsoleCmdHookAdd("patch",            ICONSOLE_HOOK_ACCESS, ConHookServerOnly);

		// Networking variables 
		IConsoleVarRegister("net_frame_freq",        &_network_frame_freq, ICONSOLE_VAR_BYTE, "The amount of frames before a command will be (visibly) executed. Default value: 1");
		IConsoleVarHookAdd("net_frame_freq",         ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleVarRegister("net_sync_freq",         &_network_sync_freq,  ICONSOLE_VAR_UINT16, "The amount of frames to check if the game is still in sync. Default value: 100");
		IConsoleVarHookAdd("net_sync_freq",          ICONSOLE_HOOK_ACCESS, ConHookServerOnly);

		IConsoleVarStringRegister("server_pw",       &_network_server_password, sizeof(_network_server_password), "Set the server password to protect your server. Use '*' to clear the password");
		IConsoleVarHookAdd("server_pw",              ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleVarHookAdd("server_pw",              ICONSOLE_HOOK_POST_ACTION, ConHookServerPW);
		IConsoleAliasRegister("server_password",     "server_pw %+");

		IConsoleVarStringRegister("rcon_pw",         &_network_rcon_password, sizeof(_network_rcon_password), "Set the rcon-password to change server behaviour. Use '*' to disable rcon");
		IConsoleVarHookAdd("rcon_pw",                ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleVarHookAdd("rcon_pw",                ICONSOLE_HOOK_POST_ACTION, ConHookRconPW);
		IConsoleAliasRegister("rcon_password",       "rcon_pw %+");

		IConsoleVarStringRegister("company_pw",      null, 0, "Set a password for your company, so no one without the correct password can join. Use '*' to clear the password");
		IConsoleVarHookAdd("company_pw",             ICONSOLE_HOOK_ACCESS, ConHookNeedNetwork);
		IConsoleVarProcAdd("company_pw",             NetworkChangeCompanyPassword);
		IConsoleAliasRegister("company_password",    "company_pw %+");

		IConsoleVarStringRegister("name",            &_network_player_name, sizeof(_network_player_name), "Set your name for multiplayer");
		IConsoleVarHookAdd("name",                   ICONSOLE_HOOK_ACCESS, ConHookNeedNetwork);
		IConsoleVarHookAdd("name",                   ICONSOLE_HOOK_POST_ACTION, ConProcPlayerName);

		IConsoleVarStringRegister("server_name",     &_network_server_name, sizeof(_network_server_name), "Set the name of the server for multiplayer");
		IConsoleVarHookAdd("server_name",            ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleVarHookAdd("server_name",            ICONSOLE_HOOK_POST_ACTION, ConHookServerName);

		IConsoleVarRegister("server_port",           &_network_server_port, ICONSOLE_VAR_UINT32, "Set the server port. Changes take effect the next time you start a server");

		IConsoleVarRegister("server_ip",             &_network_server_bind_ip, ICONSOLE_VAR_UINT32, "Set the IP the server binds to. Changes take effect the next time you start a server. Use 'all' to bind to any IP.");
		IConsoleVarProcAdd("server_ip",              ConProcServerIP);
		IConsoleAliasRegister("server_bind_ip",      "server_ip %+");
		IConsoleAliasRegister("server_ip_bind",      "server_ip %+");
		IConsoleAliasRegister("server_bind",         "server_ip %+");

		IConsoleVarRegister("max_join_time",         &_network_max_join_time, ICONSOLE_VAR_UINT16, "Set the maximum amount of time (ticks) a client is allowed to join. Default value: 500");

		IConsoleVarRegister("server_advertise",      &_network_advertise, ICONSOLE_VAR_BOOLEAN, "Set if the server will advertise to the master server and show up there");
		IConsoleVarHookAdd("server_advertise",       ICONSOLE_HOOK_ACCESS, ConHookServerOnly);
		IConsoleVarHookAdd("server_advertise",       ICONSOLE_HOOK_POST_ACTION, ConHookServerAdvertise);

		IConsoleVarRegister("pause_on_join",         &_network_pause_on_join, ICONSOLE_VAR_BOOLEAN, "Set if the server should pause gameplay while a client is joining. This might help slow users");
		IConsoleVarHookAdd("pause_on_join",          ICONSOLE_HOOK_ACCESS, ConHookServerOnly);

		IConsoleVarRegister("autoclean_companies",   &_network_autoclean_companies, ICONSOLE_VAR_BOOLEAN, "Automatically shut down inactive companies to free them up for other players. Customize with 'autoclean_(un)protected'");
		IConsoleVarHookAdd("autoclean_companies",    ICONSOLE_HOOK_ACCESS, ConHookServerOnly);

		IConsoleVarRegister("autoclean_protected",   &_network_autoclean_protected, ICONSOLE_VAR_BYTE, "Automatically remove the password from an inactive company after the given amount of months");
		IConsoleVarHookAdd("autoclean_protected",    ICONSOLE_HOOK_ACCESS, ConHookServerOnly);

		IConsoleVarRegister("autoclean_unprotected", &_network_autoclean_unprotected, ICONSOLE_VAR_BYTE, "Automatically shut down inactive companies after the given amount of months");
		IConsoleVarHookAdd("autoclean_unprotected",  ICONSOLE_HOOK_ACCESS, ConHookServerOnly);

		IConsoleVarRegister("restart_game_date",     &_network_restart_game_date, ICONSOLE_VAR_UINT16, "Auto-restart the server when Jan 1st of the set year is reached. Use '0' to disable this");
		IConsoleVarHookAdd("restart_game_date",      ICONSOLE_HOOK_ACCESS, ConHookServerOnly);

	#endif /* ENABLE_NETWORK */

		// debugging stuff
		if(Global.debugEnabled)
			IConsoleDebugLibRegister();
	}

}
