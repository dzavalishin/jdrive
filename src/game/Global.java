package game;

import java.util.HashMap;
import java.util.Map;

import game.util.Paths;
import game.util.Prices;

public class Global {

	// TODO INIT 

	public static final int MAX_PLAYERS = 8;

	public static Player[] _players = new Player[MAX_PLAYERS];
	// NOSAVE: can be determined from player structs
	public static byte [] _player_colors = new byte[MAX_PLAYERS];
	public static PlayerID _current_player;
	public static PlayerID _local_player;

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


	public static final boolean AYSTAR_DEBUG = true;

	static public Hal hal = new JavaHal();
	static public Patches _patches = new Patches();

	public static GameModes _game_mode;
	public static boolean _exit_game = false;

	public static boolean _fullscreen;
	public static boolean _double_size;
	public static boolean _force_full_redraw;
	public static boolean _fast_forward;
	public static boolean _rightclick_emulate;

	public static int _display_hz;
	public static int _fullscreen_bpp;
	
	
	public static int _map_log_x;
	public static int _map_size_x;
	public static int _map_size_y;
	public static int _map_tile_mask;
	public static int _map_size;


	public static ColorList [] _color_list = new ColorList[16];
	
	
	public static String _cmd_text = null;
	
	//public static StringID _error_message;
	//public static StringID _error_message_2;
	public static int _error_message;
	public static int _error_message_2;
	public static int _additional_cash_required;
	
	
	
	
	public static final int NUM_PRICES = 49;
	
	public static Prices _price;
	public static int [] _price_frac = new int[NUM_PRICES];

	public static int []_cargo_payment_rates = new int[NUM_CARGO];
	public static int [] _cargo_payment_rates_frac = new int[NUM_CARGO];


	/* --- 1 Day is 74 ticks ---
	 * The game's internal structure is dictated by ticks. The date counter (date_fract) is an integer of
	 * uint16 type, so it can have a max value of 65536. Every tick this variable (date_fract) is
	 * increased by 885. When it overflows, the new day loop is called.
	 * * this that means 1 day is : 65536 / 885 = 74 ticks
	 * * 1 tick is approximately 27ms.
	 * * 1 day is thus about 2 seconds (74*27 = 1998) on a machine that can run OpenTTD normally
	 */
	public static int DAY_TICKS = 74;
	public static int MAX_YEAR_BEGIN_REAL = 1920;
	public static int MAX_YEAR_END_REAL = 2090;
	public static int MAX_YEAR_END = 170;

	public static int _date;
	public static int _date_fract;

	public static int _tick_counter;
	public static int _frame_counter;

	public static int _cur_tileloop_tile;



	public static Paths _path = new Paths();

	public static Tile _m[];

	// keybd
	public static int _pressed_key; // Low 8 bits = ASCII, High 16 bits = keycode
	public static boolean _ctrl_pressed;  // Is Ctrl pressed?
	public static boolean _shift_pressed;  // Is Alt pressed?
	public static byte _dirkeys;				// 1=left, 2=up, 4=right, 8=down

	public static byte _no_scroll;

	public static boolean _generating_world = false;

	// Net
	public static boolean _networking = false;
	public static boolean _network_available = false;  // is network mode available?
	public static boolean _network_server = false; // network-server is active
	public static boolean _network_dedicated = false; // are we a dedicated server?
	public static byte _network_playas; // an id to play as..

	// main/startup
	public static String _config_file;
	public static boolean _dedicated_forks;
	public static SwitchModes _switch_mode;
	public static boolean _pause = false;
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

	static int _debug_ai_level = 0;
	static int _debug_driver_level = 0;
	static int _debug_grf_level = 0;
	static int _debug_map_level = 0;
	static int _debug_misc_level = 0;
	static int _debug_ms_level = 0;
	static int _debug_net_level = 0;
	static int _debug_spritecache_level = 0;
	static int _debug_oldloader_level = 0;
	static int _debug_pbs_level = 0;
	static int _debug_ntp_level = 0;
	static int _debug_npf_level = 0;



	static void debug(String s, Object ... arg)
	{
		String buf = String.format(s, arg);

		error( "dbg: %s\n", buf);
		Console.IConsoleDebug(buf);
	}

	// instead of DEBUG(ai, 0)( printf args)
	public static void DEBUG_ai( int level, String s, Object ... arg )
	{
		if( level >= _debug_ai_level )
			debug( s, arg );
	}

	public static void DEBUG_misc( int level, String s, Object ... arg )
	{
		if( level >= _debug_misc_level )
			debug( s, arg );
	}

	public static void DEBUG_grf( int level, String s, Object ... arg )
	{
		if( level >= _debug_grf_level )
			debug( s, arg );
	}

	public static void DEBUG_spritecache( int level, String s, Object ... arg )
	{
		if( level >= _debug_spritecache_level )
			debug( s, arg );
	}

	public static void DEBUG_map( int level, String s, Object ... arg )
	{
		if( level >= _debug_map_level )
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
	public static Object[] _decode_parameters = new Object[20];


	public static void SetDParam(int n, int v)
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

	static Object GetDParam(int n)
	{
		//assert(n < lengthof(_decode_parameters));
		return _decode_parameters[n];
	}

	static void InjectDParam(int amount)
	{
		//memmove(_decode_parameters + amount, _decode_parameters, sizeof(_decode_parameters) - amount * sizeof(int));
		System.arraycopy(_decode_parameters, amount, _decode_parameters, 0, _decode_parameters.length - amount);
	}



	private static int next_name_id = 0;
	private static Map<Integer,String> _name_array = new HashMap<Integer,String>();


	public static void DeleteName(int id)
	{
		if ((id & 0xF800) == 0x7800) {
			_name_array.remove(id & 0x1FF);
		}
	}

	public static String GetName(int id)
	{
		return new String(_name_array.get(id & ~0x600));
	}


	public static StringID AllocateNameUnique(String name, byte skip) { return RealAllocateName(name, skip, true); }
	public static StringID AllocateName(String name, byte skip) { return RealAllocateName(name, skip, false); }

	static void InitializeNameMgr()
	{
		_name_array.clear();
	}

	public static StringID RealAllocateName(String name, byte skip, boolean check_double)
	{

		if(check_double)
		{
			if( _name_array.containsValue(name) )
			{
				_error_message = STR_0132_CHOSEN_NAME_IN_USE_ALREADY;
				return new StringID(0);
			}
		}


		int tries = 0x1FF;
		while(true)
		{
			if( tries-- <= 0)
			{
				_error_message = STR_0131_TOO_MANY_NAMES_DEFINED;
				return new StringID(0);
			}

			if( null == _name_array.get(next_name_id & 0x1FF) )
			{
				int freeid = next_name_id++;
				_name_array.put(freeid, name);
				return new StringID(freeid | 0x7800 | (skip << 8));
			}

			next_name_id++;
		}

	}
	
	public static void DeleteName(StringID str) {
		DeleteName(str.id);		
	}



}
/*
class DebugLevel {
	String name;
	IntContainer level;
} */

