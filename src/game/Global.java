package game;

import game.util.Paths;

public class Global {

	// TODO INIT 

	public static final int MAX_PLAYERS = 8;

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

	public static int _map_log_x;
	public static int _map_size_x;
	public static int _map_size_y;
	public static int _map_tile_mask;
	public static int _map_size;

	public static int _date;
	public static int _date_fract;

	public static int _tick_counter;
	
	public static Paths _path = new Paths();
	
	public static Tile _m[];


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

	 int _debug_ai_level;
	 int _debug_driver_level;
	 int _debug_grf_level;
	 int _debug_map_level;
	 int _debug_misc_level;
	 int _debug_ms_level;
	 int _debug_net_level;
	 int _debug_spritecache_level;
	 int _debug_oldloader_level;
	 int _debug_pbs_level;
	 int _debug_ntp_level;
	 int _debug_npf_level;
	
	
	 
	 void debug(String s, Object ... arg)
	 {
	 	String buf = String.format(s, arg);

	 	error( "dbg: %s\n", buf);
	 	Console.IConsoleDebug(buf);
	 }
	 
	 
}
/*
class DebugLevel {
	String name;
	IntContainer level;
} */

