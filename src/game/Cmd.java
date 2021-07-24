package game;

public class Cmd {


	public static final int CMD_BUILD_RAILROAD_TRACK = 0;
	public static final int CMD_REMOVE_RAILROAD_TRACK = 1;
	public static final int CMD_BUILD_SINGLE_RAIL = 2;
	public static final int CMD_REMOVE_SINGLE_RAIL = 3;
	public static final int CMD_LANDSCAPE_CLEAR = 4;
	public static final int CMD_BUILD_BRIDGE = 5;
	public static final int CMD_BUILD_RAILROAD_STATION = 6;
	public static final int CMD_BUILD_TRAIN_DEPOT = 7;
	public static final int CMD_BUILD_SIGNALS = 8;
	public static final int CMD_REMOVE_SIGNALS = 9;
	public static final int CMD_TERRAFORM_LAND = 10;
	public static final int CMD_PURCHASE_LAND_AREA = 11;
	public static final int CMD_SELL_LAND_AREA = 12;
	public static final int CMD_BUILD_TUNNEL = 13;

	public static final int CMD_REMOVE_FROM_RAILROAD_STATION = 14;
	public static final int CMD_CONVERT_RAIL = 15;

	public static final int CMD_BUILD_TRAIN_WAYPOINT = 16;
	public static final int CMD_RENAME_WAYPOINT = 17;
	public static final int CMD_REMOVE_TRAIN_WAYPOINT = 18;

	public static final int CMD_BUILD_ROAD_STOP = 21;
	public static final int CMD_BUILD_LONG_ROAD = 23;
	public static final int CMD_REMOVE_LONG_ROAD = 24;
	public static final int CMD_BUILD_ROAD = 25;
	public static final int CMD_REMOVE_ROAD = 26;
	public static final int CMD_BUILD_ROAD_DEPOT = 27;

	public static final int CMD_BUILD_AIRPORT = 29;

	public static final int CMD_BUILD_DOCK = 30;

	public static final int CMD_BUILD_SHIP_DEPOT = 31;
	public static final int CMD_BUILD_BUOY = 32;

	public static final int CMD_PLANT_TREE = 33;

	public static final int CMD_BUILD_RAIL_VEHICLE = 34;
	public static final int CMD_MOVE_RAIL_VEHICLE = 35;

	public static final int CMD_START_STOP_TRAIN = 36;

	public static final int CMD_SELL_RAIL_WAGON = 38;

	public static final int CMD_TRAIN_GOTO_DEPOT = 39;
	public static final int CMD_FORCE_TRAIN_PROCEED = 40;
	public static final int CMD_REVERSE_TRAIN_DIRECTION = 41;

	public static final int CMD_MODIFY_ORDER = 42;
	public static final int CMD_SKIP_ORDER = 43;
	public static final int CMD_DELETE_ORDER = 44;
	public static final int CMD_INSERT_ORDER = 45;

	public static final int CMD_CHANGE_TRAIN_SERVICE_INT = 46;

	public static final int CMD_BUILD_INDUSTRY = 47;

	public static final int CMD_BUILD_COMPANY_HQ = 48;
	public static final int CMD_SET_PLAYER_FACE = 49;
	public static final int CMD_SET_PLAYER_COLOR = 50;

	public static final int CMD_INCREASE_LOAN = 51;
	public static final int CMD_DECREASE_LOAN = 52;

	public static final int CMD_WANT_ENGINE_PREVIEW = 53;

	public static final int CMD_NAME_VEHICLE = 54;
	public static final int CMD_RENAME_ENGINE = 55;
	public static final int CMD_CHANGE_COMPANY_NAME = 56;
	public static final int CMD_CHANGE_PRESIDENT_NAME = 57;
	public static final int CMD_RENAME_STATION = 58;

	public static final int CMD_SELL_AIRCRAFT = 59;
	public static final int CMD_START_STOP_AIRCRAFT = 60;
	public static final int CMD_BUILD_AIRCRAFT = 61;
	public static final int CMD_SEND_AIRCRAFT_TO_HANGAR = 62;
	public static final int CMD_CHANGE_AIRCRAFT_SERVICE_INT = 63;
	public static final int CMD_REFIT_AIRCRAFT = 64;

	public static final int CMD_PLACE_SIGN = 65;
	public static final int CMD_RENAME_SIGN = 66;

	public static final int CMD_BUILD_ROAD_VEH = 67;
	public static final int CMD_START_STOP_ROADVEH = 68;
	public static final int CMD_SELL_ROAD_VEH = 69;
	public static final int CMD_SEND_ROADVEH_TO_DEPOT = 70;
	public static final int CMD_TURN_ROADVEH = 71;
	public static final int CMD_CHANGE_ROADVEH_SERVICE_INT = 72;

	public static final int CMD_PAUSE = 73;

	public static final int CMD_BUY_SHARE_IN_COMPANY = 74;
	public static final int CMD_SELL_SHARE_IN_COMPANY = 75;
	public static final int CMD_BUY_COMPANY = 76;

	public static final int CMD_BUILD_TOWN = 77;

	public static final int CMD_RENAME_TOWN = 80;
	public static final int CMD_DO_TOWN_ACTION = 81;

	public static final int CMD_SET_ROAD_DRIVE_SIDE = 82;

	public static final int CMD_CHANGE_DIFFICULTY_LEVEL = 85;

	public static final int CMD_START_STOP_SHIP = 86;
	public static final int CMD_SELL_SHIP = 87;
	public static final int CMD_BUILD_SHIP = 88;
	public static final int CMD_SEND_SHIP_TO_DEPOT = 89;
	public static final int CMD_CHANGE_SHIP_SERVICE_INT = 90;
	public static final int CMD_REFIT_SHIP = 91;

	public static final int CMD_CLONE_ORDER = 99;
	public static final int CMD_CLEAR_AREA = 100;

	public static final int CMD_MONEY_CHEAT = 102;
	public static final int CMD_BUILD_CANAL = 103;

	public static final int CMD_PLAYER_CTRL = 104; // used in multiplayer to create a new player etc.
	public static final int CMD_LEVEL_LAND = 105;	// level land

	public static final int CMD_REFIT_RAIL_VEHICLE = 106;
	public static final int CMD_RESTORE_ORDER_INDEX = 107;
	public static final int CMD_BUILD_LOCK = 108;

	public static final int CMD_BUILD_SIGNAL_TRACK  = 110;
	public static final int CMD_REMOVE_SIGNAL_TRACK = 111;

	public static final int CMD_GIVE_MONEY = 113;
	public static final int CMD_CHANGE_PATCH_SETTING = 114;

	public static final int CMD_REPLACE_VEHICLE = 115;

	public static final int CMD_CLONE_VEHICLE = 116;


	public static final int DC_EXEC = 1;
	public static final int DC_AUTO = 2;								// don't allow building on structures
	public static final int DC_QUERY_COST = 4;					// query cost only; don't build.
	public static final int DC_NO_WATER = 8;						// don't allow building on water
	public static final int DC_NO_RAIL_OVERLAP = 0x10;	// don't allow overlap of rails (used in buildrail)
	public static final int DC_AI_BUILDING = 0x20;			// special building rules for AI
	public static final int DC_NO_TOWN_RATING = 0x40;		// town rating does not disallow you from building
	public static final int DC_FORCETEST = 0x80;				// force test too.

	public static final int CMD_ERROR = ((int)0x80000000);

	//#define public static final int CMD_MSG(x) ((x)<<16)

	public static final int CMD_AUTO = 0x200;
	public static final int CMD_NO_WATER = 0x400;
	public static final int CMD_NETWORK_COMMAND = 0x800;		// execute the command without sending it on the network
	public static final int CMD_NO_TEST_IF_IN_NETWORK = 0x1000; // When enabled; the command will bypass the no-DC_EXEC round if in network
	public static final int CMD_SHOW_NO_ERROR = 0x2000;

	/** Command flags for the command table
	 * @see _command_proc_table
	 */
	public static final int CMD_SERVER  = 0x1; /// the command can only be initiated by the server
	public static final int CMD_OFFLINE = 0x2; /// the command cannot be executed in a multiplayer game; single-player only


	//#define return_cmd_error(errcode) do { _error_message=(errcode); return CMD_ERROR; } while(0)
	//#define return_cmd_error(errcode) do { return CMD_ERROR | (errcode); } while (0)

	/**
	 * Check the return value of a DoCommand*() function
	 * @param res the resulting value from the command to be checked
	 * @return Return true if the command failed, false otherwise
	 */
	static boolean CmdFailed(int res)
	{
		// lower 16bits are the StringID of the possible error
		return res <= (CMD_ERROR | INVALID_STRING_ID);
	}






	final char* Global._cmd_text = null;
	/*
	#define DEF_COMMAND(yyyy) int yyyy(int x, int y, int flags, int p1, int p2)

	DEF_COMMAND(CmdBuildRailroadTrack);
	DEF_COMMAND(CmdRemoveRailroadTrack);
	DEF_COMMAND(CmdBuildSingleRail);
	DEF_COMMAND(CmdRemoveSingleRail);

	DEF_COMMAND(CmdLandscapeClear);

	DEF_COMMAND(CmdBuildBridge);

	DEF_COMMAND(CmdBuildRailroadStation);
	DEF_COMMAND(CmdRemoveFromRailroadStation);
	DEF_COMMAND(CmdConvertRail);

	DEF_COMMAND(CmdBuildSingleSignal);
	DEF_COMMAND(CmdRemoveSingleSignal);

	DEF_COMMAND(CmdTerraformLand);

	DEF_COMMAND(CmdPurchaseLandArea);
	DEF_COMMAND(CmdSellLandArea);

	DEF_COMMAND(CmdBuildTunnel);

	DEF_COMMAND(CmdBuildTrainDepot);
	DEF_COMMAND(CmdBuildTrainWaypoint);
	DEF_COMMAND(CmdRenameWaypoint);
	DEF_COMMAND(CmdRemoveTrainWaypoint);

	DEF_COMMAND(CmdBuildRoadStop);

	DEF_COMMAND(CmdBuildLongRoad);
	DEF_COMMAND(CmdRemoveLongRoad);
	DEF_COMMAND(CmdBuildRoad);
	DEF_COMMAND(CmdRemoveRoad);

	DEF_COMMAND(CmdBuildRoadDepot);

	DEF_COMMAND(CmdBuildAirport);

	DEF_COMMAND(CmdBuildDock);

	DEF_COMMAND(CmdBuildShipDepot);

	DEF_COMMAND(CmdBuildBuoy);

	DEF_COMMAND(CmdPlantTree);

	DEF_COMMAND(CmdBuildRailVehicle);
	DEF_COMMAND(CmdMoveRailVehicle);

	DEF_COMMAND(CmdStartStopTrain);

	DEF_COMMAND(CmdSellRailWagon);

	DEF_COMMAND(CmdSendTrainToDepot);
	DEF_COMMAND(CmdForceTrainProceed);
	DEF_COMMAND(CmdReverseTrainDirection);

	DEF_COMMAND(CmdModifyOrder);
	DEF_COMMAND(CmdSkipOrder);
	DEF_COMMAND(CmdDeleteOrder);
	DEF_COMMAND(CmdInsertOrder);
	DEF_COMMAND(CmdChangeTrainServiceInt);
	DEF_COMMAND(CmdRestoreOrderIndex);

	DEF_COMMAND(CmdBuildIndustry);

	DEF_COMMAND(CmdBuildCompanyHQ);
	DEF_COMMAND(CmdSetPlayerFace);
	DEF_COMMAND(CmdSetPlayerColor);

	DEF_COMMAND(CmdIncreaseLoan);
	DEF_COMMAND(CmdDecreaseLoan);

	DEF_COMMAND(CmdWantEnginePreview);

	DEF_COMMAND(CmdNameVehicle);
	DEF_COMMAND(CmdRenameEngine);

	DEF_COMMAND(CmdChangeCompanyName);
	DEF_COMMAND(CmdChangePresidentName);

	DEF_COMMAND(CmdRenameStation);

	DEF_COMMAND(CmdSellAircraft);
	DEF_COMMAND(CmdStartStopAircraft);
	DEF_COMMAND(CmdBuildAircraft);
	DEF_COMMAND(CmdSendAircraftToHangar);
	DEF_COMMAND(CmdChangeAircraftServiceInt);
	DEF_COMMAND(CmdRefitAircraft);

	DEF_COMMAND(CmdPlaceSign);
	DEF_COMMAND(CmdRenameSign);

	DEF_COMMAND(CmdBuildRoadVeh);
	DEF_COMMAND(CmdStartStopRoadVeh);
	DEF_COMMAND(CmdSellRoadVeh);
	DEF_COMMAND(CmdSendRoadVehToDepot);
	DEF_COMMAND(CmdTurnRoadVeh);
	DEF_COMMAND(CmdChangeRoadVehServiceInt);

	DEF_COMMAND(CmdPause);

	DEF_COMMAND(CmdBuyShareInCompany);
	DEF_COMMAND(CmdSellShareInCompany);
	DEF_COMMAND(CmdBuyCompany);

	DEF_COMMAND(CmdBuildTown);

	DEF_COMMAND(CmdRenameTown);
	DEF_COMMAND(CmdDoTownAction);

	DEF_COMMAND(CmdSetRoadDriveSide);

	DEF_COMMAND(CmdChangeDifficultyLevel);
	DEF_COMMAND(CmdChangePatchSetting);

	DEF_COMMAND(CmdStartStopShip);
	DEF_COMMAND(CmdSellShip);
	DEF_COMMAND(CmdBuildShip);
	DEF_COMMAND(CmdSendShipToDepot);
	DEF_COMMAND(CmdChangeShipServiceInt);
	DEF_COMMAND(CmdRefitShip);

	DEF_COMMAND(CmdCloneOrder);

	DEF_COMMAND(CmdClearArea);

	DEF_COMMAND(CmdGiveMoney);
	DEF_COMMAND(CmdMoneyCheat);
	DEF_COMMAND(CmdBuildCanal);
	DEF_COMMAND(CmdBuildLock);

	DEF_COMMAND(CmdPlayerCtrl);

	DEF_COMMAND(CmdLevelLand);

	DEF_COMMAND(CmdRefitRailVehicle);

	DEF_COMMAND(CmdBuildSignalTrack);
	DEF_COMMAND(CmdRemoveSignalTrack);

	DEF_COMMAND(CmdReplaceVehicle);

	DEF_COMMAND(CmdCloneVehicle);
	*/

	/* The master command table */
	static final Command _command_proc_table[] = {
			{CmdBuildRailroadTrack,                  0}, /*   0 */
			{CmdRemoveRailroadTrack,                 0}, /*   1 */
			{CmdBuildSingleRail,                     0}, /*   2 */
			{CmdRemoveSingleRail,                    0}, /*   3 */
			{CmdLandscapeClear,                      0}, /*   4 */
			{CmdBuildBridge,                         0}, /*   5 */
			{CmdBuildRailroadStation,                0}, /*   6 */
			{CmdBuildTrainDepot,                     0}, /*   7 */
			{CmdBuildSingleSignal,                   0}, /*   8 */
			{CmdRemoveSingleSignal,                  0}, /*   9 */
			{CmdTerraformLand,                       0}, /*  10 */
			{CmdPurchaseLandArea,                    0}, /*  11 */
			{CmdSellLandArea,                        0}, /*  12 */
			{CmdBuildTunnel,                         0}, /*  13 */
			{CmdRemoveFromRailroadStation,           0}, /*  14 */
			{CmdConvertRail,                         0}, /*  15 */
			{CmdBuildTrainWaypoint,                  0}, /*  16 */
			{CmdRenameWaypoint,                      0}, /*  17 */
			{CmdRemoveTrainWaypoint,                 0}, /*  18 */
			{null,                                   0}, /*  19 */
			{null,                                   0}, /*  20 */
			{CmdBuildRoadStop,                       0}, /*  21 */
			{null,                                   0}, /*  22 */
			{CmdBuildLongRoad,                       0}, /*  23 */
			{CmdRemoveLongRoad,                      0}, /*  24 */
			{CmdBuildRoad,                           0}, /*  25 */
			{CmdRemoveRoad,                          0}, /*  26 */
			{CmdBuildRoadDepot,                      0}, /*  27 */
			{null,                                   0}, /*  28 */
			{CmdBuildAirport,                        0}, /*  29 */
			{CmdBuildDock,                           0}, /*  30 */
			{CmdBuildShipDepot,                      0}, /*  31 */
			{CmdBuildBuoy,                           0}, /*  32 */
			{CmdPlantTree,                           0}, /*  33 */
			{CmdBuildRailVehicle,                    0}, /*  34 */
			{CmdMoveRailVehicle,                     0}, /*  35 */
			{CmdStartStopTrain,                      0}, /*  36 */
			{null,                                   0}, /*  37 */
			{CmdSellRailWagon,                       0}, /*  38 */
			{CmdSendTrainToDepot,                    0}, /*  39 */
			{CmdForceTrainProceed,                   0}, /*  40 */
			{CmdReverseTrainDirection,               0}, /*  41 */

			{CmdModifyOrder,                         0}, /*  42 */
			{CmdSkipOrder,                           0}, /*  43 */
			{CmdDeleteOrder,                         0}, /*  44 */
			{CmdInsertOrder,                         0}, /*  45 */

			{CmdChangeTrainServiceInt,               0}, /*  46 */

			{CmdBuildIndustry,                       0}, /*  47 */
			{CmdBuildCompanyHQ,                      0}, /*  48 */
			{CmdSetPlayerFace,                       0}, /*  49 */
			{CmdSetPlayerColor,                      0}, /*  50 */

			{CmdIncreaseLoan,                        0}, /*  51 */
			{CmdDecreaseLoan,                        0}, /*  52 */

			{CmdWantEnginePreview,                   0}, /*  53 */

			{CmdNameVehicle,                         0}, /*  54 */
			{CmdRenameEngine,                        0}, /*  55 */

			{CmdChangeCompanyName,                   0}, /*  56 */
			{CmdChangePresidentName,                 0}, /*  57 */

			{CmdRenameStation,                       0}, /*  58 */

			{CmdSellAircraft,                        0}, /*  59 */
			{CmdStartStopAircraft,                   0}, /*  60 */

			{CmdBuildAircraft,                       0}, /*  61 */
			{CmdSendAircraftToHangar,                0}, /*  62 */
			{CmdChangeAircraftServiceInt,            0}, /*  63 */
			{CmdRefitAircraft,                       0}, /*  64 */

			{CmdPlaceSign,                           0}, /*  65 */
			{CmdRenameSign,                          0}, /*  66 */

			{CmdBuildRoadVeh,                        0}, /*  67 */
			{CmdStartStopRoadVeh,                    0}, /*  68 */
			{CmdSellRoadVeh,                         0}, /*  69 */
			{CmdSendRoadVehToDepot,                  0}, /*  70 */
			{CmdTurnRoadVeh,                         0}, /*  71 */
			{CmdChangeRoadVehServiceInt,             0}, /*  72 */

			{CmdPause,                      Cmd.CMD_SERVER}, /*  73 */

			{CmdBuyShareInCompany,                   0}, /*  74 */
			{CmdSellShareInCompany,                  0}, /*  75 */
			{CmdBuyCompany,                          0}, /*  76 */

			{CmdBuildTown,                 Cmd.CMD_OFFLINE}, /*  77 */
			{null,                                   0}, /*  78 */
			{null,                                   0}, /*  79 */
			{CmdRenameTown,                 Cmd.CMD_SERVER}, /*  80 */
			{CmdDoTownAction,                        0}, /*  81 */

			{CmdSetRoadDriveSide,           Cmd.CMD_SERVER}, /*  82 */
			{null,                                   0}, /*  83 */
			{null,                                   0}, /*  84 */
			{CmdChangeDifficultyLevel,      Cmd.CMD_SERVER}, /*  85 */

			{CmdStartStopShip,                       0}, /*  86 */
			{CmdSellShip,                            0}, /*  87 */
			{CmdBuildShip,                           0}, /*  88 */
			{CmdSendShipToDepot,                     0}, /*  89 */
			{CmdChangeShipServiceInt,                0}, /*  90 */
			{CmdRefitShip,                           0}, /*  91 */

			{null,                                   0}, /*  92 */
			{null,                                   0}, /*  93 */
			{null,                                   0}, /*  94 */
			{null,                                   0}, /*  95 */
			{null,                                   0}, /*  96 */
			{null,                                   0}, /*  97 */
			{null,                                   0}, /*  98 */

			{CmdCloneOrder,                          0}, /*  99 */

			{CmdClearArea,                           0}, /* 100 */
			{null,                                   0}, /* 101 */

			{CmdMoneyCheat,                Cmd.CMD_OFFLINE}, /* 102 */
			{CmdBuildCanal,                          0}, /* 103 */
			{CmdPlayerCtrl,                          0}, /* 104 */

			{CmdLevelLand,                           0}, /* 105 */

			{CmdRefitRailVehicle,                    0}, /* 106 */
			{CmdRestoreOrderIndex,                   0}, /* 107 */
			{CmdBuildLock,                           0}, /* 108 */
			{null,                                   0}, /* 109 */
			{CmdBuildSignalTrack,                    0}, /* 110 */
			{CmdRemoveSignalTrack,                   0}, /* 111 */
			{null,                                   0}, /* 112 */
			{CmdGiveMoney,                           0}, /* 113 */
			{CmdChangePatchSetting,         Cmd.CMD_SERVER}, /* 114 */
			{CmdReplaceVehicle,                      0}, /* 115 */
			{CmdCloneVehicle,						 0}, /* 116 */
	};

	/* This function range-checks a cmd, and checks if the cmd is not null */
	boolean IsValidCommand(int cmd)
	{
		cmd &= 0xFF;

		return
				cmd < lengthof(_command_proc_table) &&
				_command_proc_table[cmd].proc != null;
	}

	byte GetCommandFlags(int cmd) {return _command_proc_table[cmd & 0xFF].flags;}

	int DoCommandByTile(TileIndex tile, int p1, int p2, int flags, int procc)
	{
		return DoCommand(TileX(tile) * 16, TileY(tile) * 16, p1, p2, flags, procc);
	}


	static int _docommand_recursive;

	int DoCommand(int x, int y, int p1, int p2, int flags, int procc)
	{
		int res;
		CommandProc *proc;

		/* Do not even think about executing out-of-bounds tile-commands */
		if (TileVirtXY(x, y) > MapSize()) {
			Global._cmd_text = null;
			return Cmd.CMD_ERROR;
		}

		proc = _command_proc_table[procc].proc;

		if (_docommand_recursive == 0) Global._error_message = INVALID_STRING_ID;

		_docommand_recursive++;

		// only execute the test call if it's toplevel, or we're not execing.
		if (_docommand_recursive == 1 || !(flags & Cmd.DC_EXEC) || (flags & Cmd.DC_FORCETEST) ) {
			res = proc(x, y, flags&~Cmd.DC_EXEC, p1, p2);
			if (CmdFailed(res)) {
				if (res & 0xFFFF) Global._error_message = res & 0xFFFF;
				goto error;
			}

			if (_docommand_recursive == 1) {
				if (!(flags&Cmd.DC_QUERY_COST) && res != 0 && !CheckPlayerHasMoney(res))
					goto error;
			}

			if (!(flags & Cmd.DC_EXEC)) {
				_docommand_recursive--;
				Global._cmd_text = null;
				return res;
			}
		}

		/* Execute the command here. All cost-relevant functions set the expenses type
		 * themselves with "Player.SET_EXPENSES_TYPE(...);" at the beginning of the function */
		res = proc(x, y, flags, p1, p2);
		if (CmdFailed(res)) {
			if (res & 0xFFFF) Global._error_message = res & 0xFFFF;
			error:
				_docommand_recursive--;
			Global._cmd_text = null;
			return Cmd.CMD_ERROR;
		}

		// if toplevel, subtract the money.
		if (--_docommand_recursive == 0) {
			SubtractMoneyFromPlayer(res);
			// XXX - Old AI hack which doesn't use DoCommandDP; update last build coord of player
			if ( (x|y) != 0 && Global._current_player < Global.MAX_PLAYERS) {
				GetPlayer(Global._current_player).last_build_coordinate = TileVirtXY(x, y);
			}
		}

		Global._cmd_text = null;
		return res;
	}

	int GetAvailableMoneyForCommand()
	{
		PlayerID pid = Global._current_player;
		if (pid >= Global.MAX_PLAYERS) return 0x7FFFFFFF; // max int
		return GetPlayer(pid).player_money;
	}

	// toplevel network safe docommand function for the current player. must not be called recursively.
	// the callback is called when the command succeeded or failed.
	boolean DoCommandP(TileIndex tile, int p1, int p2, CommandCallback *callback, int cmd)
	{
		int res = 0,res2;
		CommandProc *proc;
		int flags;
		boolean notest;

		int x = TileX(tile) * 16;
		int y = TileY(tile) * 16;

		/* Do not even think about executing out-of-bounds tile-commands */
		if (tile > MapSize()) {
			Global._cmd_text = null;
			return false;
		}

		assert(_docommand_recursive == 0);

		Global._error_message = INVALID_STRING_ID;
		Global._error_message_2 = cmd >> 16;
		_additional_cash_required = 0;

		/** Spectator has no rights except for the dedicated server which
		 * is a spectator but is the server, so can do anything */
		if (Global._current_player == Owner.OWNER_SPECTATOR && !_network_dedicated) {
			ShowErrorMessage(Global._error_message, Global._error_message_2, x, y);
			Global._cmd_text = null;
			return false;
		}

		flags = 0;
		if (cmd & Cmd.CMD_AUTO) flags |= Cmd.DC_AUTO;
		if (cmd & Cmd.CMD_NO_WATER) flags |= Cmd.DC_NO_WATER;

		// get pointer to command handler
		assert((cmd & 0xFF) < lengthof(_command_proc_table));
		proc = _command_proc_table[cmd & 0xFF].proc;
		if (proc == null) {
			Global._cmd_text = null;
			return false;
		}

		// Some commands have a different output in dryrun than the realrun
		//  e.g.: if you demolish a whole town, the dryrun would say okay.
		//  but by really destroying, your rating drops and at a certain point
		//  it will fail. so res and res2 are different
		// Cmd.CMD_REMOVE_ROAD: This command has special local authority
		// restrictions which may cause the test run to fail (the previous
		// road fragments still stay there and the town won't let you
		// disconnect the road system), but the exec will succeed and this
		// fact will trigger an assertion failure. --pasky
		notest =
				(cmd & 0xFF) == Cmd.CMD_CLEAR_AREA ||
				(cmd & 0xFF) == Cmd.CMD_CONVERT_RAIL ||
				(cmd & 0xFF) == Cmd.CMD_LEVEL_LAND ||
				(cmd & 0xFF) == Cmd.CMD_REMOVE_ROAD ||
				(cmd & 0xFF) == Cmd.CMD_REMOVE_LONG_ROAD;

		_docommand_recursive = 1;

		// cost estimation only?
		if (_shift_pressed && IsLocalPlayer() && !(cmd & (Cmd.CMD_NETWORK_COMMAND | Cmd.CMD_SHOW_NO_ERROR))) {
			// estimate the cost.
			res = proc(x, y, flags, p1, p2);
			if (CmdFailed(res)) {
				if (res & 0xFFFF) Global._error_message = res & 0xFFFF;
				ShowErrorMessage(Global._error_message, Global._error_message_2, x, y);
			} else {
				ShowEstimatedCostOrIncome(res, x, y);
			}

			_docommand_recursive = 0;
			Global._cmd_text = null;
			return false;
		}


		if (!((cmd & Cmd.CMD_NO_TEST_IF_IN_NETWORK) && _networking)) {
			// first test if the command can be executed.
			res = proc(x,y, flags, p1, p2);
			if (CmdFailed(res)) {
				if (res & 0xFFFF) Global._error_message = res & 0xFFFF;
				goto show_error;
			}
			// no money? Only check if notest is off
			if (!notest && res != 0 && !CheckPlayerHasMoney(res)) goto show_error;
		}

		#ifdef ENABLE_NETWORK
		/** If we are in network, and the command is not from the network
		 * send it to the command-queue and abort execution
		 * If we are a dedicated server temporarily switch local player, otherwise
		 * the other parties won't be able to execute our command and will desync.
		 * @todo Rewrite dedicated server to something more than a dirty hack!
		 */
		if (_networking && !(cmd & Cmd.CMD_NETWORK_COMMAND)) {
			if (_network_dedicated) Global._local_player = 0;
			NetworkSend_Command(tile, p1, p2, cmd, callback);
			if (_network_dedicated) Global._local_player = Owner.OWNER_SPECTATOR;
			_docommand_recursive = 0;
			Global._cmd_text = null;
			return true;
		}
		#endif /* ENABLE_NETWORK */

		// update last build coordinate of player.
		if ( tile != 0 && Global._current_player < Global.MAX_PLAYERS) GetPlayer(Global._current_player).last_build_coordinate = tile;

		/* Actually try and execute the command. If no cost-type is given
		 * use the finalruction one */
		_yearly_expenses_type = EXPENSES_CONSTRUCTION;
		res2 = proc(x,y, flags|Cmd.DC_EXEC, p1, p2);

		// If notest is on, it means the result of the test can be different than
		//   the real command.. so ignore the test
		if (!notest && !((cmd & Cmd.CMD_NO_TEST_IF_IN_NETWORK) && _networking)) {
			assert(res == res2); // sanity check
		} else {
			if (CmdFailed(res2)) {
				if (res2 & 0xFFFF) Global._error_message = res2 & 0xFFFF;
				goto show_error;
			}
		}

		SubtractMoneyFromPlayer(res2);

		if (IsLocalPlayer() && Global._game_mode != GameModes.GM_EDITOR) {
			if (res2 != 0)
				ShowCostOrIncomeAnimation(x, y, GetSlopeZ(x, y), res2);
			if (_additional_cash_required) {
				Global.SetDParam(0, _additional_cash_required);
				ShowErrorMessage(Str.STR_0003_NOT_ENOUGH_CASH_REQUIRES, Global._error_message_2, x,y);
				if (res2 == 0) goto callb_err;
			}
		}

		_docommand_recursive = 0;

		if (callback) callback(true, tile, p1, p2);
		Global._cmd_text = null;
		return true;

		show_error:
			// show error message if the command fails?
			if (IsLocalPlayer() && Global._error_message_2 != 0)
				ShowErrorMessage(Global._error_message, Global._error_message_2, x,y);

		callb_err:
			_docommand_recursive = 0;

		if (callback) callback(false, tile, p1, p2);
		Global._cmd_text = null;
		return false;
	}




}


class Command {
	CommandProc proc;
	byte flags;
} 
