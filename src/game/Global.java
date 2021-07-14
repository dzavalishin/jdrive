package game;

public class Global {

	// TODO INIT 
	
	public static final boolean AYSTAR_DEBUG = true;
	
	static int _map_log_x;
	static int _map_size_x;
	static int _map_size_y;
	static int _map_tile_mask;
	static int _map_size;

	

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
	
}
