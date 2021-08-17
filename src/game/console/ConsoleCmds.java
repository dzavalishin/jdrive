package game.console;

import game.Engine;
import game.Landscape;
import game.TileIndex;
import game.Vehicle;
import game.ViewPort;
import game.Window;

public class ConsoleCmds 
{

	// ** scriptfile handling ** //
	//static FILE *_script_file;
	static boolean _script_running;

	// ** console command / variable defines ** //
	//#define DEF_CONSOLE_CMD(function) static boolean function(byte argc, String argv[])
	//#define DEF_CONSOLE_HOOK(function) static boolean function(void)


	/* **************************** */
	/* variable and command hooks   */
	/* **************************** */

	/*#ifdef ENABLE_NETWORK

	static  boolean NetworkAvailable(void)
	{
		if (!_network_available) {
			IConsoleError("You cannot use this command because there is no network available.");
			return false;
		}
		return true;
	}

	DEF_CONSOLE_HOOK(ConHookServerOnly)
	{
		if (!NetworkAvailable()) return false;

		if (!_network_server) {
			IConsoleError("This command/variable is only available to a network server.");
			return false;
		}
		return true;
	}

	DEF_CONSOLE_HOOK(ConHookClientOnly)
	{
		if (!NetworkAvailable()) return false;

		if (_network_server) {
			IConsoleError("This command/variable is not available to a network server.");
			return false;
		}
		return true;
	}

	DEF_CONSOLE_HOOK(ConHookNeedNetwork)
	{
		if (!NetworkAvailable()) return false;

		if (!_networking) {
			IConsoleError("Not connected. This command/variable is only available in multiplayer.");
			return false;
		}
		return true;
	}

	DEF_CONSOLE_HOOK(ConHookNoNetwork)
	{
		if (_networking) {
			IConsoleError("This command/variable is forbidden in multiplayer.");
			return false;
		}
		return true;
	}

	#endif /* ENABLE_NETWORK */

	static void IConsoleHelp(String str)
	{
		IConsolePrintF(_icolour_warn, "- %s", str);
	}

	static boolean ConStopAllVehicles(byte argc, String argv[])
	{
		//Vehicle v;
		if (argc == 0) {
			IConsoleHelp("Stops all vehicles in the game. For debugging only! Use at your own risk... Usage: 'stopall'");
			return true;
		}

		//FOR_ALL_VEHICLES(v)
		Vehicle.forEach( (v) ->
		{
			if (v.IsValidVehicle()) {
				/* Code ripped from CmdStartStopTrain. Can't call it, because of
				 * ownership problems, so we'll duplicate some code, for now */
				if (v.type == Vehicle.VEH_Train)
					v.rail.days_since_order_progr = 0;
				v.vehstatus |= Vehicle.VS_STOPPED;
				Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, Vehicle.STATUS_BAR);
				Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
			}
		});
		return true;
	}

	
	static boolean ConResetEngines(byte argc, String argv[])
	{
		if (argc == 0) {
			IConsoleHelp("Reset status data of all engines. This might solve some issues with 'lost' engines. Usage: 'resetengines'");
			return true;
		}

		Engine.StartupEngines();
		return true;
	}

	//#ifdef _DEBUG
	static boolean ConResetTile(byte argc, String argv[])
	{
		if (argc == 0) {
			IConsoleHelp("Reset a tile to bare land. Usage: 'resettile <tile>'");
			IConsoleHelp("Tile can be either decimal (34161) or hexadecimal (0x4a5B)");
			return true;
		}

		if (argc == 2) {
			int [] result = {0};
			if (GetArgumentInteger(result, argv[1])) {
				Landscape.DoClearSquare(TileIndex.get(result[0]));
				return true;
			}
		}

		return false;
	}
	//#endif /* _DEBUG */

	static boolean ConScrollToTile(byte argc, String argv[])
	{
		if (argc == 0) {
			IConsoleHelp("Center the screen on a given tile. Usage: 'scrollto <tile>'");
			IConsoleHelp("Tile can be either decimal (34161) or hexadecimal (0x4a5B)");
			return true;
		}

		if (argc == 2) {
			int [] result = {0};
			if (GetArgumentInteger(result, argv[1])) {
				ViewPort.ScrollMainWindowToTile(TileIndex.get(result[0]));
				return true;
			}
		}

		return false;
	}

	/*
	//extern boolean SafeSaveOrLoad(String filename, int mode, int newgm);
	//extern void BuildFileList(void);
	//extern void SetFiosType(const byte fiostype);

	// Save the map to a file 
	static boolean function(byte argc, String argv[])(ConSave)
	{
		if (argc == 0) {
			IConsoleHelp("Save the current game. Usage: 'save <filename>'");
			return true;
		}

		if (argc == 2) {
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

	static const FiosItem* GetFiosItem(const char* file)
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


	static boolean function(byte argc, String argv[])(ConLoad)
	{
		const FiosItem *item;
		String file;

		if (argc == 0) {
			IConsoleHelp("Load a game by name or index. Usage: 'load <file | number>'");
			return true;
		}

		if (argc != 2) return false;

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


	static boolean function(byte argc, String argv[])(ConRemove)
	{
		const FiosItem* item;
		const char* file;

		if (argc == 0) {
			IConsoleHelp("Remove a savegame by name or index. Usage: 'rm <file | number>'");
			return true;
		}

		if (argc != 2) return false;

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
	static boolean function(byte argc, String argv[])(ConListFiles)
	{
		int i;

		if (argc == 0) {
			IConsoleHelp("List all loadable savegames and directories in the current dir via console. Usage: 'ls | dir'");
			return true;
		}

		BuildFileList();

		for (i = 0; i < _fios_num; i++) {
			const FiosItem *item = &_fios_list[i];
			IConsolePrintF(_icolour_def, "%d) %s", i, item.title);
		}

		FiosFreeSavegameList();
		return true;
	}

	// Change the dir via console 
	static boolean function(byte argc, String argv[])(ConChangeDirectory)
	{
		const FiosItem *item;
		String file;

		if (argc == 0) {
			IConsoleHelp("Change the dir via console. Usage: 'cd <directory | number>'");
			return true;
		}

		if (argc != 2) return false;

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

	static boolean function(byte argc, String argv[])(ConPrintWorkingDirectory)
	{
		String path;

		if (argc == 0) {
			IConsoleHelp("Print out the current working directory. Usage: 'pwd'");
			return true;
		}

		// XXX - Workaround for broken file handling
		FiosGetSavegameList(&_fios_num, SLD_LOAD_GAME);
		FiosFreeSavegameList();

		FiosGetDescText(&path, null);
		IConsolePrint(_icolour_def, path);
		return true;
	}

	static boolean function(byte argc, String argv[])(ConClearBuffer)
	{
		if (argc == 0) {
			IConsoleHelp("Clear the console buffer. Usage: 'clear'");
			return true;
		}

		IConsoleClearBuffer();
		InvalidateWindow(WC_CONSOLE, 0);
		return true;
	}


	// ********************************* //
	// * Network Core Console Commands * //
	// ********************************* //
	#ifdef ENABLE_NETWORK

	static boolean function(byte argc, String argv[])(ConBan)
	{
		NetworkClientInfo *ci;
		uint32 index;

		if (argc == 0) {
			IConsoleHelp("Ban a player from a network game. Usage: 'ban <client-id>'");
			IConsoleHelp("For client-id's, see the command 'clients'");
			return true;
		}

		if (argc != 2) return false;

		index = atoi(argv[1]);

		if (index == NETWORK_SERVER_INDEX) {
			IConsolePrint(_icolour_def, "Silly boy, you can not ban yourself!");
			return true;
		}
		if (index == 0) {
			IConsoleError("Invalid Client-ID");
			return true;
		}

		ci = NetworkFindClientInfoFromIndex(index);

		if (ci != null) {
			uint i;
			// Add user to ban-list 
			for (i = 0; i < lengthof(_network_ban_list); i++) {
				if (_network_ban_list[i] == null || _network_ban_list[i][0] == '\0') {
					_network_ban_list[i] = strdup(inet_ntoa(*(struct in_addr *)&ci.client_ip));
					break;
				}
			}

			SEND_COMMAND(PACKET_SERVER_ERROR)(NetworkFindClientStateFromIndex(index), NETWORK_ERROR_KICKED);
		} else
			IConsoleError("Client-ID not found");

		return true;
	}

	static boolean function(byte argc, String argv[])(ConUnBan)
	{
		uint i, index;

		if (argc == 0) {
			IConsoleHelp("Unban a player from a network game. Usage: 'unban <ip | id>'");
			IConsoleHelp("For a list of banned IP's, see the command 'banlist'");
			return true;
		}

		if (argc != 2) return false;

		index = (strchr(argv[1], '.') == null) ? atoi(argv[1]) : 0;
		index--;

		for (i = 0; i < lengthof(_network_ban_list); i++) {
			if (_network_ban_list[i] == null || _network_ban_list[i][0] == '\0')
				continue;

			if (strncmp(_network_ban_list[i], argv[1], strlen(_network_ban_list[i])) == 0 || index == i) {
				_network_ban_list[i][0] = '\0';
				IConsolePrint(_icolour_def, "IP unbanned.");
				return true;
			}
		}

		IConsolePrint(_icolour_def, "IP not in ban-list.");
		return true;
	}

	static boolean function(byte argc, String argv[])(ConBanList)
	{
		uint i;

		if (argc == 0) {
			IConsoleHelp("List the IP's of banned clients: Usage 'banlist'");
			return true;
		}

		IConsolePrint(_icolour_def, "Banlist: ");

		for (i = 0; i < lengthof(_network_ban_list); i++) {
			if (_network_ban_list[i] == null || _network_ban_list[i][0] == '\0')
				continue;

			IConsolePrintF(_icolour_def, "  %d) %s", i + 1, _network_ban_list[i]);
		}

		return true;
	}

	static boolean function(byte argc, String argv[])(ConPauseGame)
	{
		if (argc == 0) {
			IConsoleHelp("Pause a network game. Usage: 'pause'");
			return true;
		}

		if (_pause == 0) {
			DoCommandP(0, 1, 0, null, CMD_PAUSE);
			IConsolePrint(_icolour_def, "Game paused.");
		} else
			IConsolePrint(_icolour_def, "Game is already paused.");

		return true;
	}

	static boolean function(byte argc, String argv[])(ConUnPauseGame)
	{
		if (argc == 0) {
			IConsoleHelp("Unpause a network game. Usage: 'unpause'");
			return true;
		}

		if (_pause != 0) {
			DoCommandP(0, 0, 0, null, CMD_PAUSE);
			IConsolePrint(_icolour_def, "Game unpaused.");
		} else
			IConsolePrint(_icolour_def, "Game is already unpaused.");

		return true;
	}

	static boolean function(byte argc, String argv[])(ConRcon)
	{
		if (argc == 0) {
			IConsoleHelp("Remote control the server from another client. Usage: 'rcon <password> <command>'");
			IConsoleHelp("Remember to enclose the command in quotes, otherwise only the first parameter is sent");
			return true;
		}

		if (argc < 3) return false;

		SEND_COMMAND(PACKET_CLIENT_RCON)(argv[1], argv[2]);
		return true;
	}

	static boolean function(byte argc, String argv[])(ConStatus)
	{
		static String stat_str[] = {"inactive", "authorized", "waiting", "loading map", "map done", "ready", "active"};
		String status;
		const NetworkClientState *cs;

		if (argc == 0) {
			IConsoleHelp("List the status of all clients connected to the server: Usage 'status'");
			return true;
		}

		FOR_ALL_CLIENTS(cs) {
			int lag = NetworkCalculateLag(cs);
			const NetworkClientInfo *ci = DEREF_CLIENT_INFO(cs);

			status = (cs.status <= STATUS_ACTIVE) ? stat_str[cs.status] : "unknown";
			IConsolePrintF(8, "Client #%1d  name: '%s'  status: '%s'  frame-lag: %3d  company: %1d  IP: %s  unique-id: '%s'",
				cs.index, ci.client_name, status, lag, ci.client_playas, GetPlayerIP(ci), ci.unique_id);
		}

		return true;
	}

	static boolean function(byte argc, String argv[])(ConKick)
	{
		NetworkClientInfo *ci;
		uint32 index;

		if (argc == 0) {
			IConsoleHelp("Kick a player from a network game. Usage: 'kick <client-id>'");
			IConsoleHelp("For client-id's, see the command 'clients'");
			return true;
		}

		if (argc != 2) return false;

		index = atoi(argv[1]);
		if (index == NETWORK_SERVER_INDEX) {
			IConsolePrint(_icolour_def, "Silly boy, you can not kick yourself!");
			return true;
		}
		if (index == 0) {
			IConsoleError("Invalid client-id");
			return true;
		}

		ci = NetworkFindClientInfoFromIndex(index);

		if (ci != null) {
			SEND_COMMAND(PACKET_SERVER_ERROR)(NetworkFindClientStateFromIndex(index), NETWORK_ERROR_KICKED);
		} else
			IConsoleError("Client-id not found");

		return true;
	}

	static boolean function(byte argc, String argv[])(ConResetCompany)
	{
		Player *p;
		NetworkClientState *cs;
		NetworkClientInfo *ci;
		byte index;

		if (argc == 0) {
			IConsoleHelp("Remove an idle company from the game. Usage: 'reset_company <company-id>'");
			IConsoleHelp("For company-id's, see the list of companies from the dropdown menu. Player 1 is 1, etc.");
			return true;
		}

		if (argc != 2) return false;

		index = atoi(argv[1]);

		// Check valid range 
		if (index < 1 || index > MAX_PLAYERS) {
			IConsolePrintF(_icolour_err, "Company does not exist. Company-id must be between 1 and %d.", MAX_PLAYERS);
			return true;
		}

		// Check if company does exist 
		index--;
		p = GetPlayer(index);
		if (!p.is_active) {
			IConsoleError("Company does not exist.");
			return true;
		}

		if (p.is_ai) {
			IConsoleError("Company is owned by an AI.");
			return true;
		}

		// Check if the company has active players 
		FOR_ALL_CLIENTS(cs) {
			ci = DEREF_CLIENT_INFO(cs);
			if (ci.client_playas - 1 == index) {
				IConsoleError("Cannot remove company: a client is connected to that company.");
				return true;
			}
		}
		ci = NetworkFindClientInfoFromIndex(NETWORK_SERVER_INDEX);
		if (ci.client_playas - 1 == index) {
			IConsoleError("Cannot remove company: the server is connected to that company.");
			return true;
		}

		// It is safe to remove this company 
		DoCommandP(0, 2, index, null, CMD_PLAYER_CTRL);
		IConsolePrint(_icolour_def, "Company deleted.");

		return true;
	}

	static boolean function(byte argc, String argv[])(ConNetworkClients)
	{
		NetworkClientInfo *ci;

		if (argc == 0) {
			IConsoleHelp("Get a list of connected clients including their ID, name, company-id, and IP. Usage: 'clients'");
			return true;
		}

		for (ci = _network_client_info; ci != &_network_client_info[MAX_CLIENT_INFO]; ci++) {
			if (ci.client_index != NETWORK_EMPTY_INDEX) {
				IConsolePrintF(8, "Client #%1d  name: '%s'  company: %1d  IP: %s",
					ci.client_index, ci.client_name, ci.client_playas, GetPlayerIP(ci));
			}
		}

		return true;
	}

	static boolean function(byte argc, String argv[])(ConNetworkConnect)
	{
		char *ip;
		String port = null;
		String player = null;
		uint16 rport;

		if (argc == 0) {
			IConsoleHelp("Connect to a remote OTTD server and join the game. Usage: 'connect <ip>'");
			IConsoleHelp("IP can contain port and player: 'IP#Player:Port', eg: 'server.ottd.org#2:443'");
			return true;
		}

		if (argc < 2) return false;

		if (_networking) // We are in network-mode, first close it!
			NetworkDisconnect();

		ip = argv[1];
		rport = NETWORK_DEFAULT_PORT;

		ParseConnectionString(&player, &port, ip);

		IConsolePrintF(_icolour_def, "Connecting to %s...", ip);
		if (player != null) {
			_network_playas = atoi(player);
			IConsolePrintF(_icolour_def, "    player-no: %s", player);
		}
		if (port != null) {
			rport = atoi(port);
			IConsolePrintF(_icolour_def, "    port: %s", port);
		}

		NetworkClientConnectGame(ip, rport);

		return true;
	}

	#endif /* ENABLE_NETWORK */

	/* ******************************** */
	/*   script file console commands   */
	/* ******************************** */
	/*
	static boolean function(byte argc, String argv[])(ConExec)
	{
		char cmdline[ICON_CMDLN_SIZE];
		char *cmdptr;

		if (argc == 0) {
			IConsoleHelp("Execute a local script file. Usage: 'exec <script> <?>'");
			return true;
		}

		if (argc < 2) return false;

		_script_file = fopen(argv[1], "r");

		if (_script_file == null) {
			if (argc == 2 || atoi(argv[2]) != 0) IConsoleError("script file not found");
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

	static boolean function(byte argc, String argv[])(ConReturn)
	{
		if (argc == 0) {
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
	static boolean function(byte argc, String argv[])(ConScript)
	{
		extern FILE* _iconsole_output_file;

		if (argc == 0) {
			IConsoleHelp("Start or stop logging console output to a file. Usage: 'script <filename>'");
			IConsoleHelp("If filename is omitted, a running log is stopped if it is active");
			return true;
		}

		if (!CloseConsoleLogIfActive()) {
			if (argc < 2) return false;

			IConsolePrintF(_icolour_def, "file output started to: %s", argv[1]);
			_iconsole_output_file = fopen(argv[1], "ab");
			if (_iconsole_output_file == null) IConsoleError("could not open file");
		}

		return true;
	}


	static boolean function(byte argc, String argv[])(ConEcho)
	{
		if (argc == 0) {
			IConsoleHelp("Print back the first argument to the console. Usage: 'echo <arg>'");
			return true;
		}

		if (argc < 2) return false;
		IConsolePrint(_icolour_def, argv[1]);
		return true;
	}

	static boolean function(byte argc, String argv[])(ConEchoC)
	{
		if (argc == 0) {
			IConsoleHelp("Print back the first argument to the console in a given colour. Usage: 'echoc <colour> <arg2>'");
			return true;
		}

		if (argc < 3) return false;
		IConsolePrint(atoi(argv[1]), argv[2]);
		return true;
	}

	extern void SwitchMode(int new_mode);

	static boolean function(byte argc, String argv[])(ConNewGame)
	{
		if (argc == 0) {
			IConsoleHelp("Start a new game. Usage: 'newgame'");
			IConsoleHelp("The server can force a new game using 'newgame', any client using it will part and start a single-player game");
			return true;
		}

		GenRandomNewGame(Random(), InteractiveRandom());
		return true;
	}

	static boolean function(byte argc, String argv[])(ConAlias)
	{
		IConsoleAlias *alias;

		if (argc == 0) {
			IConsoleHelp("Add a new alias, or redefine the behaviour of an existing alias . Usage: 'alias <name> <command>'");
			return true;
		}

		if (argc < 3) return false;

		alias = IConsoleAliasGet(argv[1]);
		if (alias == null) {
			IConsoleAliasRegister(argv[1], argv[2]);
		} else {
			free(alias.cmdline);
			alias.cmdline = strdup(argv[2]);
		}
		return true;
	}

	static boolean function(byte argc, String argv[])(ConScreenShot)
	{
		if (argc == 0) {
			IConsoleHelp("Create a screenshot of the game. Usage: 'screenshot [big | no_con]'");
			IConsoleHelp("'big' makes a screenshot of the whole map, 'no_con' hides the console to create the screenshot");
			return true;
		}

		if (argc > 3) return false;

		_make_screenshot = 1;
		if (argc > 1) {
			if (strcmp(argv[1], "big") == 0 || (argc == 3 && strcmp(argv[2], "big") == 0))
				_make_screenshot = 2;

			if (strcmp(argv[1], "no_con") == 0 || (argc == 3 && strcmp(argv[2], "no_con") == 0))
				IConsoleClose();
		}

		return true;
	}

	static boolean function(byte argc, String argv[])(ConInfoVar)
	{
		static String _icon_vartypes[] = {"boolean", "byte", "uint16", "uint32", "int16", "int32", "string"};
		const IConsoleVar *var;

		if (argc == 0) {
			IConsoleHelp("Print out debugging information about a variable. Usage: 'info_var <var>'");
			return true;
		}

		if (argc < 2) return false;

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


	static boolean function(byte argc, String argv[])(ConInfoCmd)
	{
		const IConsoleCmd *cmd;

		if (argc == 0) {
			IConsoleHelp("Print out debugging information about a command. Usage: 'info_cmd <cmd>'");
			return true;
		}

		if (argc < 2) return false;

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

	static boolean function(byte argc, String argv[])(ConDebugLevel)
	{
		if (argc == 0) {
			IConsoleHelp("Get/set the default debugging level for the game. Usage: 'debug_level [<level>]'");
			IConsoleHelp("Level can be any combination of names, levels. Eg 'net=5 ms=4'. Remember to enclose it in \"'s");
			return true;
		}

		if (argc > 2) return false;

		if (argc == 1) {
			IConsolePrintF(_icolour_def, "Current debug-level: '%s'", GetDebugString());
		} else SetDebugString(argv[1]);

		return true;
	}

	static boolean function(byte argc, String argv[])(ConExit)
	{
		if (argc == 0) {
			IConsoleHelp("Exit the game. Usage: 'exit'");
			return true;
		}

		_exit_game = true;
		return true;
	}

	static boolean function(byte argc, String argv[])(ConPart)
	{
		if (argc == 0) {
			IConsoleHelp("Leave the currently joined/running game (only ingame). Usage: 'part'");
			return true;
		}

		if (_game_mode != GM_NORMAL) return false;

		_switch_mode = SM_MENU;
		return true;
	}

	static boolean function(byte argc, String argv[])(ConHelp)
	{
		if (argc == 2) {
			const IConsoleCmd *cmd;
			const IConsoleVar *var;
			const IConsoleAlias *alias;

			cmd = IConsoleCmdGet(argv[1]);
			if (cmd != null) {
				cmd.proc(0, null);
				return true;
			}

			alias = IConsoleAliasGet(argv[1]);
			if (alias != null) {
				cmd = IConsoleCmdGet(alias.cmdline);
				if (cmd != null) {
					cmd.proc(0, null);
					return true;
				}
				IConsolePrintF(_icolour_err, "ERROR: alias is of special type, please see its execution-line: '%s'", alias.cmdline);
				return true;
			}

			var = IConsoleVarGet(argv[1]);
			if (var != null && var.help != null) {
				IConsoleHelp(var.help);
				return true;
			}

			IConsoleError("command or variable not found");
			return true;
		}

		IConsolePrint(13, " ---- OpenTTD Console Help ---- ");
		IConsolePrint( 1, " - variables: [command to list all variables: list_vars]");
		IConsolePrint( 1, " set value with '<var> = <value>', use '++/--' to in-or decrement");
		IConsolePrint( 1, " or omit '=' and just '<var> <value>'. get value with typing '<var>'");
		IConsolePrint( 1, " - commands: [command to list all commands: list_cmds]");
		IConsolePrint( 1, " call commands with '<command> <arg2> <arg3>...'");
		IConsolePrint( 1, " - to assign strings, or use them as arguments, enclose it within quotes");
		IConsolePrint( 1, " like this: '<command> \"string argument with spaces\"'");
		IConsolePrint( 1, " - use 'help <command> | <variable>' to get specific information");
		IConsolePrint( 1, " - scroll console output with shift + (up | down) | (pageup | pagedown))");
		IConsolePrint( 1, " - scroll console input history with the up | down arrows");
		IConsolePrint( 1, "");
		return true;
	}

	static boolean function(byte argc, String argv[])(ConListCommands)
	{
		const IConsoleCmd *cmd;
		size_t l = 0;

		if (argc == 0) {
			IConsoleHelp("List all registered commands. Usage: 'list_cmds [<pre-filter>]'");
			return true;
		}

		if (argv[1] != null) l = strlen(argv[1]);

		for (cmd = _iconsole_cmds; cmd != null; cmd = cmd.next) {
			if (argv[1] == null || strncmp(cmd.name, argv[1], l) == 0) {
					IConsolePrintF(_icolour_def, "%s", cmd.name);
			}
		}

		return true;
	}

	static boolean function(byte argc, String argv[])(ConListVariables)
	{
		const IConsoleVar *var;
		size_t l = 0;

		if (argc == 0) {
			IConsoleHelp("List all registered variables. Usage: 'list_vars [<pre-filter>]'");
			return true;
		}

		if (argv[1] != null) l = strlen(argv[1]);

		for (var = _iconsole_vars; var != null; var = var.next) {
			if (argv[1] == null || strncmp(var.name, argv[1], l) == 0)
				IConsolePrintF(_icolour_def, "%s", var.name);
		}

		return true;
	}

	static boolean function(byte argc, String argv[])(ConListAliases)
	{
		const IConsoleAlias *alias;
		size_t l = 0;

		if (argc == 0) {
			IConsoleHelp("List all registered aliases. Usage: 'list_aliases [<pre-filter>]'");
			return true;
		}

		if (argv[1] != null) l = strlen(argv[1]);

		for (alias = _iconsole_aliases; alias != null; alias = alias.next) {
			if (argv[1] == null || strncmp(alias.name, argv[1], l) == 0)
				IConsolePrintF(_icolour_def, "%s => %s", alias.name, alias.cmdline);
		}

		return true;
	}

	#ifdef ENABLE_NETWORK

	static boolean function(byte argc, String argv[])(ConSay)
	{
		if (argc == 0) {
			IConsoleHelp("Chat to your fellow players in a multiplayer game. Usage: 'say \"<msg>\"'");
			return true;
		}

		if (argc != 2) return false;

		if (!_network_server) {
			SEND_COMMAND(PACKET_CLIENT_CHAT)(NETWORK_ACTION_CHAT, DESTTYPE_BROADCAST, 0 /* param does not matter * /, argv[1]);
		} else
			NetworkServer_HandleChat(NETWORK_ACTION_CHAT, DESTTYPE_BROADCAST, 0, argv[1], NETWORK_SERVER_INDEX);

		return true;
	}

	static boolean function(byte argc, String argv[])(ConSayPlayer)
	{
		if (argc == 0) {
			IConsoleHelp("Chat to a certain player in a multiplayer game. Usage: 'say_player <player-no> \"<msg>\"'");
			IConsoleHelp("PlayerNo is the player that plays as company <playerno>, 1 through max_players");
			return true;
		}

		if (argc != 3) return false;

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

	static boolean function(byte argc, String argv[])(ConSayClient)
	{
		if (argc == 0) {
			IConsoleHelp("Chat to a certain player in a multiplayer game. Usage: 'say_client <client-no> \"<msg>\"'");
			IConsoleHelp("For client-id's, see the command 'clients'");
			return true;
		}

		if (argc != 3) return false;

		if (!_network_server) {
			SEND_COMMAND(PACKET_CLIENT_CHAT)(NETWORK_ACTION_CHAT_CLIENT, DESTTYPE_CLIENT, atoi(argv[1]), argv[2]);
		} else
			NetworkServer_HandleChat(NETWORK_ACTION_CHAT_CLIENT, DESTTYPE_CLIENT, atoi(argv[1]), argv[2], NETWORK_SERVER_INDEX);

		return true;
	}

	DEF_CONSOLE_HOOK(ConHookServerPW)
	{
		if (strncmp(_network_server_password, "*", NETWORK_PASSWORD_LENGTH) == 0) {
			_network_server_password[0] = '\0';
			_network_game_info.use_password = 0;
		} else
			_network_game_info.use_password = 1;

		return true;
	}

	DEF_CONSOLE_HOOK(ConHookRconPW)
	{
		if (strncmp(_network_rcon_password, "*", NETWORK_PASSWORD_LENGTH) == 0)
			_network_rcon_password[0] = '\0';

		ttd_strlcpy(_network_game_info.rcon_password, _network_rcon_password, sizeof(_network_game_info.rcon_password));

		return true;
	}

	// Also use from within player_gui to change the password graphically 
	boolean NetworkChangeCompanyPassword(byte argc, String argv[])
	{
		if (argc == 0) {
			if (_local_player >= MAX_PLAYERS) return true; // dedicated server
			IConsolePrintF(_icolour_warn, "Current value for 'company_pw': %s", _network_player_info[_local_player].password);
			return true;
		}

		if (_local_player >= MAX_PLAYERS) {
			IConsoleError("You have to own a company to make use of this command.");
			return false;
		}

		if (argc != 1) return false;

		if (strncmp(argv[0], "*", sizeof(_network_player_info[_local_player].password)) == 0)
			argv[0][0] = '\0';

		ttd_strlcpy(_network_player_info[_local_player].password, argv[0], sizeof(_network_player_info[_local_player].password));

		if (!_network_server)
			SEND_COMMAND(PACKET_CLIENT_SET_PASSWORD)(_network_player_info[_local_player].password);

		IConsolePrintF(_icolour_warn, "'company_pw' changed to:  %s", _network_player_info[_local_player].password);

		return true;
	}

	DEF_CONSOLE_HOOK(ConProcPlayerName)
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

	DEF_CONSOLE_HOOK(ConHookServerName)
	{
		ttd_strlcpy(_network_game_info.server_name, _network_server_name, sizeof(_network_game_info.server_name));
		return true;
	}

	DEF_CONSOLE_HOOK(ConHookServerAdvertise)
	{
		if (!_network_advertise) // remove us from advertising
			NetworkUDPRemoveAdvertise();

		return true;
	}

	static boolean function(byte argc, String argv[])(ConProcServerIP)
	{
		if (argc == 0) {
			IConsolePrintF(_icolour_warn, "Current value for 'server_ip': %s", inet_ntoa(*(struct in_addr *)&_network_server_bind_ip));
			return true;
		}

		if (argc != 1) return false;

		_network_server_bind_ip = (strcmp(argv[0], "all") == 0) ? inet_addr("0.0.0.0") : inet_addr(argv[0]);
		snprintf(_network_server_bind_ip_host, sizeof(_network_server_bind_ip_host), "%s", inet_ntoa(*(struct in_addr *)&_network_server_bind_ip));
		IConsolePrintF(_icolour_warn, "'server_ip' changed to:  %s", inet_ntoa(*(struct in_addr *)&_network_server_bind_ip));
		return true;
	}

	static boolean function(byte argc, String argv[])(ConPatch)
	{
		if (argc == 0) {
			IConsoleHelp("Change patch variables for all players. Usage: 'patch <name> [<value>]'");
			IConsoleHelp("Omitting <value> will print out the current value of the patch-setting.");
			return true;
		}

		if (argc == 1 || argc > 3) return false;

		if (argc == 2) {
			IConsoleGetPatchSetting(argv[1]);
		} else
			IConsoleSetPatchSetting(argv[1], argv[2]);

		return true;
	}
	#endif /* ENABLE_NETWORK */

	static boolean ConListDumpVariables(byte argc, String argv[])
	{
		IConsoleVar var;

		if (argc == 0) {
			IConsoleHelp("List all variables with their value. Usage: 'dump_vars [<pre-filter>]'");
			return true;
		}


		for (var = Console._iconsole_vars; var != null; var = var.next) {
			if (argv[1] == null || var.name.equals(argv[1]))
				var.IConsoleVarPrintGetValue();
		}

		return true;
	}


	//#ifdef _DEBUG
	/* ****************************************** */
	/*  debug commands and variables */
	/* ****************************************** */

	static void IConsoleDebugLibRegister()
	{
		// debugging variables and functions
		//extern boolean _stdlib_con_developer; /* XXX extern in .c */

		//IConsoleVarRegister("con_developer",    &_stdlib_con_developer, ICONSOLE_VAR_BOOLEAN, "Enable/disable console debugging information (internal)");
		IConsoleCmdRegister("resettile",        ConsoleCmds::ConResetTile);
		//IConsoleAliasRegister("dbg_echo",       "echo %A; echo %B");
		//IConsoleAliasRegister("dbg_echo2",      "echo %!");
	}
	//#endif

	/* ****************************************** */
	/*  console command and variable registration */
	/* ****************************************** */

	static void IConsoleStdLibRegister()
	{
		// stdlib
		//extern byte _stdlib_developer; /* XXX extern in .c */

		// default variables and functions
		/*
		IConsoleCmdRegister("debug_level",  ConDebugLevel);
		IConsoleCmdRegister("dump_vars",    ConListDumpVariables);
		IConsoleCmdRegister("echo",         ConEcho);
		IConsoleCmdRegister("echoc",        ConEchoC);
		IConsoleCmdRegister("exec",         ConExec);
		IConsoleCmdRegister("exit",         ConExit);
		IConsoleCmdRegister("part",         ConPart);
		IConsoleCmdRegister("help",         ConHelp);
		IConsoleCmdRegister("info_cmd",     ConInfoCmd);
		IConsoleCmdRegister("info_var",     ConInfoVar);
		IConsoleCmdRegister("list_cmds",    ConListCommands);
		IConsoleCmdRegister("list_vars",    ConListVariables);
		IConsoleCmdRegister("list_aliases", ConListAliases);
		IConsoleCmdRegister("newgame",      ConNewGame);
		IConsoleCmdRegister("quit",         ConExit);
		IConsoleCmdRegister("resetengines", ConResetEngines);
		IConsoleCmdRegister("return",       ConReturn);
		IConsoleCmdRegister("screenshot",   ConScreenShot);
		IConsoleCmdRegister("script",       ConScript);
		IConsoleCmdRegister("scrollto",     ConScrollToTile);
		IConsoleCmdRegister("alias",        ConAlias);
		IConsoleCmdRegister("load",         ConLoad);
		IConsoleCmdRegister("rm",           ConRemove);
		IConsoleCmdRegister("save",         ConSave);
		IConsoleCmdRegister("ls",           ConListFiles);
		IConsoleCmdRegister("cd",           ConChangeDirectory);
		IConsoleCmdRegister("pwd",          ConPrintWorkingDirectory);
		IConsoleCmdRegister("clear",        ConClearBuffer);
		*/
		IConsoleCmdRegister("stopall",      ConsoleCmds::ConStopAllVehicles);

		/*
		IConsoleAliasRegister("dir",      "ls");
		IConsoleAliasRegister("del",      "rm %+");
		IConsoleAliasRegister("newmap",   "newgame");
		IConsoleAliasRegister("new_map",  "newgame");
		IConsoleAliasRegister("new_game", "newgame");
		*/

		IConsoleVarRegister("developer", _stdlib_developer, ICONSOLE_VAR_BYTE, "Redirect debugging output from the console/command line to the ingame console (value 2). Default value: 1");

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
		IConsoleCmdRegister("status",          ConStatus);
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
	/* TODO XXX #ifdef _DEBUG
		IConsoleDebugLibRegister();
	#endif */
	}

}
