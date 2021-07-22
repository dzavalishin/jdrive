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



	
	
}


class Command {
	CommandProc proc;
	byte flags;
} 
