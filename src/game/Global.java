package game;

import game.util.Paths;

public class Global {

	// TODO INIT 

	public static final int MAX_PLAYERS = 8;

	public static Player[] _players = new Player[MAX_PLAYERS];
	// NOSAVE: can be determined from player structs
	public static byte [] _player_colors = new byte[MAX_PLAYERS];
	public static PlayerID _current_player;

	public static Economy _economy = new Economy();


	public static final int NUM_NORMAL_RAIL_ENGINES = 54;
	public static final int NUM_MONORAIL_ENGINES = 30;
	public static final int NUM_MAGLEV_ENGINES = 32;
	public static final int NUM_TRAIN_ENGINES = NUM_NORMAL_RAIL_ENGINES + NUM_MONORAIL_ENGINES + NUM_MAGLEV_ENGINES;
	public static final int NUM_ROAD_ENGINES = 88;
	public static final int NUM_SHIP_ENGINES = 11;
	public static final int NUM_AIRCRAFT_ENGINES = 41;
	public static final int TOTAL_NUM_ENGINES = NUM_TRAIN_ENGINES + NUM_ROAD_ENGINES + NUM_SHIP_ENGINES + NUM_AIRCRAFT_ENGINES;
	public static final int AIRCRAFT_ENGINES_INDEX = NUM_TRAIN_ENGINES + NUM_ROAD_ENGINES + NUM_SHIP_ENGINES;
	public static final int SHIP_ENGINES_INDEX = NUM_TRAIN_ENGINES + NUM_ROAD_ENGINES;
	public static final int ROAD_ENGINES_INDEX = NUM_TRAIN_ENGINES;

	/* These constants are for now linked to the representation of bridges
	 * and tunnels, so they can be used by GetTileTrackStatus_TunnelBridge
	 * to compare against the map5 array. In an ideal world, these
	 * constants would be used everywhere when accessing tunnels and
	 * bridges. For now, you should just not change the values for road
	 * and rail.
	 */
	public static final int TRANSPORT_RAIL = 0;
	public static final int TRANSPORT_ROAD = 1;
	public static final int TRANSPORT_WATER  = 2;
	public static final int TRANSPORT_END = 3;
	public static final int INVALID_TRANSPORT = 0xff;

	/* AcceptedCargo
	public static final int CT_PASSENGERS = 0;
	public static final int CT_COAL = 1;
	public static final int CT_MAIL = 2;
	public static final int CT_OIL = 3;
	public static final int CT_LIVESTOCK = 4;
	public static final int CT_GOODS = 5;
	public static final int CT_GRAIN = 6;
	public static final int CT_WOOD = 7;
	public static final int CT_IRON_ORE = 8;
	public static final int CT_STEEL = 9;
	public static final int CT_VALUABLES = 10;
	public static final int CT_FOOD = 11;

	// Arctic
	public static final int CT_WHEAT = 6;
	public static final int CT_HILLY_UNUSED = 8;
	public static final int CT_PAPER = 9;
	public static final int CT_GOLD = 10;

	// Tropic
	public static final int CT_RUBBER = 1;
	public static final int CT_FRUIT = 4;
	public static final int CT_MAIZE = 6;
	public static final int CT_COPPER_ORE = 8;
	public static final int CT_WATER = 9;
	public static final int CT_DIAMONDS = 10;

	// Toyland
	public static final int CT_SUGAR = 1;
	public static final int CT_TOYS = 3;
	public static final int CT_BATTERIES = 4;
	public static final int CT_CANDY = 5;
	public static final int CT_TOFFEE = 6;
	public static final int CT_COLA = 7;
	public static final int CT_COTTON_CANDY = 8;
	public static final int CT_BUBBLES = 9;
	public static final int CT_PLASTIC = 10;
	public static final int CT_FIZZY_DRINKS = 11;

	public static final int NUM_CARGO = 12;

	public static final int CT_INVALID = 0xFF;
	
	*/
	

	public static final boolean AYSTAR_DEBUG = true;

	static public Hal hal = new JavaHal();

	public static GameModes _game_mode;
	public static boolean _exit_game = false;

	public static int _map_log_x;
	public static int _map_size_x;
	public static int _map_size_y;
	public static int _map_tile_mask;
	public static int _map_size;

	public static int _date;
	public static int _date_fract;

	public static int _tick_counter;
	public static int _frame_counter;

	public static Paths _path = new Paths();

	public static Tile _m[];

	// keybd
	public static int _pressed_key; // Low 8 bits = ASCII, High 16 bits = keycode
	public static boolean _ctrl_pressed;  // Is Ctrl pressed?
	public static boolean _shift_pressed;  // Is Alt pressed?
	public static byte _dirkeys;				// 1=left, 2=up, 4=right, 8=down

	// main/startup
	public static String _config_file;
	public static boolean _dedicated_forks;
	public static SwitchModes _switch_mode;
	public static boolean _pause = false;
	public static boolean _networking = false;
	public static byte _display_opt;
	public static boolean _do_autosave;
	public static boolean _use_dos_palette = false;



	// binary logarithm of the map size, try to avoid using this one
	public static int MapLogX()  { return _map_log_x; }
	/* The size of the map */
	public static int MapSizeX() { return _map_size_x; }
	public static int MapSizeY() { return _map_size_y; }
	/* The maximum coordinates */
	public static int MapMaxX() { return _map_size_x - 1; }
	public static int MapMaxY() { return _map_size_y - 1; }
	/* The number of tiles in the map */
	public static int MapSize() { return _map_size; }


	public static void printf(String s, Object ... arg) {
		String buf = String.format(s, arg);
		System.out.print(buf);		
	}

	public static void error(String s, Object ... arg) {
		String buf = String.format(s, arg);
		System.err.print(buf);		
	}

	public static final StringID  INVALID_STRING_ID = new StringID(0xFFFF);


	//void DEBUG(name, level) if (level == 0 || _debug_ ## name ## _level >= level) debug

	int _debug_ai_level = 0;
	int _debug_driver_level = 0;
	int _debug_grf_level = 0;
	int _debug_map_level = 0;
	int _debug_misc_level = 0;
	int _debug_ms_level = 0;
	int _debug_net_level = 0;
	int _debug_spritecache_level = 0;
	int _debug_oldloader_level = 0;
	int _debug_pbs_level = 0;
	int _debug_ntp_level = 0;
	int _debug_npf_level = 0;



	void debug(String s, Object ... arg)
	{
		String buf = String.format(s, arg);

		error( "dbg: %s\n", buf);
		Console.IConsoleDebug(buf);
	}

	// instead of DEBUG(ai, 0)( printf args)
	public void DEBUG_ai( int level, String s, Object ... arg )
	{
		if( level >= _debug_ai_level )
			debug( s, arg );
	}

	public void DEBUG_misc( int level, String s, Object ... arg )
	{
		if( level >= _debug_misc_level )
			debug( s, arg );
	}

	public static int Random() {
		return hal.Random();
	}

	
	
/* dont use me
static inline void SetDParamX(uint32 *s, uint n, uint32 v)
{
	s[n] = v;
}

static inline uint32 GetDParamX(const uint32 *s, uint n)
{
	return s[n];
}
*/
static private int _decode_parameters[] = new int[20];


static void SetDParam(int n, int v)
{
	//assert(n < _decode_parameters.length);
	_decode_parameters[n] = v;
}

static void SetDParam64(int n, long v)
{
	//assert(n + 1 < lengthof(_decode_parameters));
	_decode_parameters[n + 0] = (int) (v & 0xffffffff);
	_decode_parameters[n + 1] = (int) (v >> 32);
}

static int GetDParam(int n)
{
	//assert(n < lengthof(_decode_parameters));
	return _decode_parameters[n];
}
	
	
	
}
/*
class DebugLevel {
	String name;
	IntContainer level;
} */

