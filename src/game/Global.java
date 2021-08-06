package game;

import java.util.HashMap;

import java.util.Map;
import java.util.function.Consumer;

import game.util.GameDate;
import game.util.Paths;
import game.util.Prices;
import game.util.Strings;
import game.util.YearMonthDay;
import game.Cheat.Cheats;
import game.tables.CargoConst;
import game.util.BitOps;

public class Global {

	// TODO INIT 

	public static final int MAX_PLAYERS = 8;
	public static final int MAX_SCREEN_WIDTH = 2048;
	public static final int MAX_SCREEN_HEIGHT = 1200;

	public static Player[] _players = new Player[MAX_PLAYERS];
	// NOSAVE: can be determined from player structs
	public static byte [] _player_colors = new byte[MAX_PLAYERS];
	public static PlayerID _current_player;
	public static PlayerID _local_player;
	public static boolean _is_old_ai_player = false;

	public static Economy _economy = new Economy();
	public static final Cheats  _cheats = Cheat.cs;

	public static Consumer<TileIndex> _place_proc;

	/* Display Options */
	//enum {
	public static final int DO_SHOW_TOWN_NAMES =    1 << 0;
	public static final int DO_SHOW_STATION_NAMES = 1 << 1;
	public static final int DO_SHOW_SIGNS =         1 << 2;
	public static final int DO_FULL_ANIMATION =     1 << 3;
	public static final int DO_TRANS_BUILDINGS =    1 << 4;
	public static final int DO_FULL_DETAIL =        1 << 5;
	public static final int DO_WAYPOINTS =          1 << 6;
	public static final int DO_TRANS_SIGNS =        1 << 7;


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
	public static final int NPF_TILE_LENGTH = 100;

	/* In certain windows you navigate with the arrow keys. Do not scroll the
	 * gameview when here. Bitencoded variable that only allows scrolling if all
	 * elements are zero */
	//enum {
	public static final int SCROLL_CON =  0;
	public static final int SCROLL_EDIT = 1;
	public static final int SCROLL_SAVE = 2;
	public static final int SCROLL_CHAT = 4;
	//};
	public static final int SLD_LOAD_GAME = 0;
	public static final int SLD_LOAD_SCENARIO = 1;
	public static final int SLD_SAVE_GAME = 2;
	public static final int SLD_SAVE_SCENARIO = 3;
	public static final int SLD_NEW_GAME = 4;
	
	
	static public Hal hal = new JavaHal();
	static public Patches _patches = new Patches();

	public static GameModes _game_mode;
	public static boolean _exit_game = false;
	public static int _saveload_mode;

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

	public static int[] _cur_resolution = new int[2];

	public static int [][] _random_seeds = new int[2][2];

	public static ColorList [] _color_list = new ColorList[16];


	public static String _cmd_text = null;

	//public static StringID _error_message;
	//public static StringID _error_message_2;
	public static int _error_message;
	public static int _error_message_2;
	public static int _additional_cash_required;
	public static StringID _switch_mode_errorstr;

	// NOSAVE: Return values from various commands.
	public static VehicleID 	_new_train_id;
	public static VehicleID 	_new_wagon_id;
	public static VehicleID 	_new_aircraft_id;
	public static VehicleID 	_new_ship_id;
	public static VehicleID 	_new_roadveh_id;
	public static VehicleID 	_new_vehicle_id;
	public static int 			_aircraft_refit_capacity;
	public static int 			_cmd_build_rail_veh_score;

	public static TileIndex _backup_orders_tile;
	public static BackuppedOrders [] _backup_orders_data = new BackuppedOrders[1];

	/* Access Vehicle Data */
	//#include "table/engines.h"
	//public static  finalEngineInfo orig_engine_info[TOTAL_NUM_ENGINES];
	//public static  finalRailVehicleInfo orig_rail_vehicle_info[NUM_TRAIN_ENGINES];
	//public static  finalShipVehicleInfo orig_ship_vehicle_info[NUM_SHIP_ENGINES];
	//public static  finalAircraftVehicleInfo orig_aircraft_vehicle_info[NUM_AIRCRAFT_ENGINES];
	//public static  finalRoadVehicleInfo orig_road_vehicle_info[NUM_ROAD_ENGINES];

	public static  EngineInfo []			_engine_info = new EngineInfo[TOTAL_NUM_ENGINES];
	public static  RailVehicleInfo []		_rail_vehicle_info = new RailVehicleInfo[NUM_TRAIN_ENGINES];
	public static  ShipVehicleInfo []		_ship_vehicle_info = new ShipVehicleInfo[NUM_SHIP_ENGINES];
	public static  AircraftVehicleInfo []	_aircraft_vehicle_info = new AircraftVehicleInfo[NUM_AIRCRAFT_ENGINES];
	public static  RoadVehicleInfo []		_road_vehicle_info = new RoadVehicleInfo[NUM_ROAD_ENGINES];
	
	

	public static int _news_display_opt = 0;
	public static boolean _news_ticker_sound = false;
	public static NewsItem _statusbar_news_item = null;



	public static final int NUM_PRICES = 49;

	public static Prices _price;
	public static int [] _price_frac = new int[NUM_PRICES];

	public static int []_cargo_payment_rates = new int[AcceptedCargo.NUM_CARGO];
	public static int [] _cargo_payment_rates_frac = new int[AcceptedCargo.NUM_CARGO];
	public static CargoConst _cargoc = new CargoConst();

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
	public static int _cur_year;
	public static int _cur_month;

	public static int _tick_counter;
	public static int _frame_counter;
	public static int _timer_counter;

	public static Paths _path = new Paths();

	public static Tile _m[];

	// keybd
	public static int _pressed_key; // Low 8 bits = ASCII, High 16 bits = keycode
	public static boolean _ctrl_pressed;  // Is Ctrl pressed?
	public static boolean _shift_pressed;  // Is Alt pressed?
	public static byte _dirkeys;				// 1=left, 2=up, 4=right, 8=down

	public static byte _no_scroll;

	// IN/OUT parameters to commands
	// byte _yearly_expenses_type;
	public static TileIndex _terraform_err_tile;
	public static TileIndex _build_tunnel_endtile;
	public static boolean _generating_world = false;
	public static int _new_town_size;
	public static int _returned_refit_amount;


	// etc
	// Skip aging of cargo?
	public static int _age_cargo_skip_counter;
	public static Point _tile_fract_coords; // TODO to ViewPort?
	public static String _screenshot_name;
	public static byte _vehicle_design_names;
	public static int _get_z_hint;
	public static Town _cleared_town = null;
	public static int _cleared_town_rating;
	public static int _caret_timer;
	public static Vehicle _place_clicked_vehicle;
	public static int _num_resolutions = 1; // TODO _num_resolutions
	
	// Available aircraft types
	public static int _avail_aircraft = 0; // TODO who assigns?

	// Position in tile loop
	public static TileIndex _cur_tileloop_tile = null;
	//public static int _cur_tileloop_tile;

	// Also save scrollpos_x, scrollpos_y and zoom
	public static int _disaster_delay;

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

	public static void DEBUG_ms( int level, String s, Object ... arg )
	{
		if( level >= _debug_ms_level )
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

	public static void DEBUG_npf( int level, String s, Object ... arg )
	{
		if( level >= _debug_npf_level )
			debug( s, arg );
	}

	public static void DEBUG_pbs( int level, String s, Object ... arg )
	{
		if( level >= _debug_pbs_level )
			debug( s, arg );
	}

	public static void DEBUG_ntp( int level, String s, Object ... arg )
	{
		if( level >= _debug_ntp_level )
			debug( s, arg );
	}

	
	
	
	public static int Random() {
		return hal.Random();
	}



	@Deprecated
	public static void SetDParamX(int []s, int n, int v)
	{
		s[n] = v;
	}

	@Deprecated
	public static int GetDParamX(int []s, int n)
	{
		return s[n];
	}

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

	public static void COPY_IN_DPARAM(int offs, int [] src, int num) 
	{
		//memcpy(_decode_parameters + offs, src, sizeof(uint32) * (num))

		System.arraycopy(src, 0, _decode_parameters, offs, num );
	}

	public static void COPY_OUT_DPARAM(int [] dst, int offs, int num) 
	{
		//memcpy(dst,_decode_parameters + offs, sizeof(uint32) * (num))
		System.arraycopy( _decode_parameters, offs, dst, 0, num  );
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


	public static StringID AllocateNameUnique(String name, int skip) { return RealAllocateName(name, skip, true); }
	public static StringID AllocateName(String name, int skip) { return RealAllocateName(name, skip, false); }

	static void InitializeNameMgr()
	{
		_name_array.clear();
	}

	public static StringID RealAllocateName(String name, int skip, boolean check_double)
	{

		if(check_double)
		{
			if( _name_array.containsValue(name) )
			{
				_error_message = Str.STR_0132_CHOSEN_NAME_IN_USE_ALREADY;
				return new StringID(0);
			}
		}


		int tries = 0x1FF;
		while(true)
		{
			if( tries-- <= 0)
			{
				_error_message = Str.STR_0131_TOO_MANY_NAMES_DEFINED;
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




	static String GetString(StringID string)
	{
		return Strings.GetString(string);
	}

	public static String GetString(int string)
	{
		return Strings.GetString(string);
	}







	void IncreaseDate()
	{
		YearMonthDay ymd = new YearMonthDay();

		if (Global._game_mode == GameModes.GM_MENU) {
			Global._tick_counter++;
			return;
		}

		Misc.RunVehicleDayProc(Global._date_fract);

		/* increase day, and check if a new day is there? */
		Global._tick_counter++;

		Global._date_fract++;
		if (Global._date_fract < (Global.DAY_TICKS*Global._patches.day_length))
			return;
		Global._date_fract = 0;

		/* yeah, increse day counter and call various daily loops */
		Global._date++;

		TextEffect.TextMessageDailyLoop();

		// TODO DisasterDailyLoop();
		WayPoint.WaypointsDailyLoop();

		if (Global._game_mode != GameModes.GM_MENU) {
			Window.InvalidateWindowWidget(Window.WC_STATUS_BAR, 0, 0);
			EngineGui.EnginesDailyLoop();
		}

		/* check if we entered a new month? */
		GameDate.ConvertDayToYMD(ymd, Global._date);
		if ((byte)ymd.month == Global._cur_month)
			return;
		Global._cur_month = ymd.month;

		/* yes, call various monthly loops */
		if (Global._game_mode != GameModes.GM_MENU) {
			/* TODO
			if (BitOps.HASBIT(Global._autosave_months[_opt.autosave], Global._cur_month)) {
				Global._do_autosave = true;
				RedrawAutosave();
			}*/

			Economy.PlayersMonthlyLoop();
			Engine.EnginesMonthlyLoop();
			Town.TownsMonthlyLoop();
			Industry.IndustryMonthlyLoop();
			//Station._global_station_sort_dirty();
			Station._global_station_sort_dirty = true;
			/*#ifdef ENABLE_NETWORK
			if (_network_server)
				NetworkServerMonthlyLoop();
	#endif /* ENABLE_NETWORK */
		}

		/* check if we entered a new year? */
		if ((byte)ymd.year == Global._cur_year)
			return;
		Global._cur_year = ymd.year;

		/* yes, call various yearly loops */

		Player.PlayersYearlyLoop();
		TrainCmd.TrainsYearlyLoop();
		RoadVehCmd.RoadVehiclesYearlyLoop();
		AirCraft.AircraftYearlyLoop();
		Ship.ShipsYearlyLoop();
		/*#ifdef ENABLE_NETWORK
		if (_network_server)
			NetworkServerYearlyLoop();
	#endif /* ENABLE_NETWORK */

		/* check if we reached end of the game (31 dec 2050) */
		if (Global._cur_year == Global._patches.ending_date - Global.MAX_YEAR_BEGIN_REAL) {
			PlayerGui.ShowEndGameChart();
			/* check if we reached 2090 (MAX_YEAR_END_REAL), that's the maximum year. */
		} else if (Global._cur_year == (Global.MAX_YEAR_END + 1)) {
			//Vehicle v;
			Global._cur_year = Global.MAX_YEAR_END;
			Global._date = 62093;
			//FOR_ALL_VEHICLES(v)
			Vehicle.forEach( (v) ->
			{
				v.date_of_last_service -= 365; // 1 year is 365 days long
			});

			/* Because the _date wraps here, and text-messages expire by game-days, we have to clean out
			 *  all of them if the date is set back, else those messages will hang for ever */
			TextEffect.InitTextMessage();
		}

		if (Global._patches.auto_euro)
			Currency.CheckSwitchToEuro();

		/* XXX: check if year 2050 was reached */
	}


	static void SetDate(int date)
	{
		YearMonthDay ymd = new YearMonthDay();
		GameDate.ConvertDayToYMD(ymd, _date = date);
		_cur_year = ymd.year;
		_cur_month = ymd.month;
	/*#ifdef ENABLE_NETWORK
		_network_last_advertise_date = 0;
	#endif /* ENABLE_NETWORK */
	}
	
	static void ShowErrorMessage(StringID msg_1, StringID msg_2, int x, int y)
	{
		MiscGui.ShowErrorMessage( msg_1, msg_2, x, y);
	}

	static void ShowErrorMessage(int msg_1, int msg_2, int x, int y)
	{
		MiscGui.ShowErrorMessage( new StringID(msg_1), new StringID(msg_2), x, y);
	}













}
/*
class DebugLevel {
	String name;
	IntContainer level;
} */

