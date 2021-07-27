package game;

public class RailtypeInfo 
{

	public static final int SIG_SEMAPHORE_MASK = (1 << 3);

	// these are the maximums used for updating signal blocks, and checking if a depot is in a pbs block
	public static final int NUM_SSD_ENTRY = 256; // max amount of blocks
	public static final int NUM_SSD_STACK = 32;// max amount of blocks to check recursively

	/** Struct containing the main sprites. @note not all sprites are listed, but only
	 *  the ones used directly in the code */
	RailBaseSprites base_sprites;

	/** struct containing the sprites for the rail GUI. @note only sprites referred to
	 * directly in the code are listed */
	RailGuiSprites gui_sprites;

	RailCursors cursor;

	StringID toolbar_caption;

	/** sprite number difference between a piece of track on a snowy ground and the corresponding one on normal ground */
	SpriteID snow_offset;

	/** bitmask to the OTHER railtypes that can be used by an engine of THIS railtype */
	byte compatible_railtypes;

	/**
	 * Offset between the current railtype and normal rail. This means that:<p>
	 * 1) All the sprites in a railset MUST be in the same order. This order
	 *    is determined by normal rail. Check sprites 1005 and following for this order<p>
	 * 2) The position where the railtype is loaded must always be the same, otherwise
	 *    the offset will fail.<p>
	 * @note: Something more flexible might be desirable in the future.
	 */
	SpriteID total_offset;

	/**
	 * Bridge offset
	 */
	SpriteID bridge_offset;



	public RailtypeInfo(
			int[] base, 
			int[] gui, 
			int[] cursors, 
			int captionStr, 
			int snowOffset,
			int i, int j, int k) 
	{
		base_sprites = new RailBaseSprites(base);
		gui_sprites = new RailGuiSprites(gui);
		cursor = new RailCursors(cursors);

		toolbar_caption = new StringID(captionStr);
		snow_offset = SpriteID.get(snowOffset);
		compatible_railtypes = (byte) i;
		total_offset = SpriteID.get(j);
		bridge_offset = SpriteID.get(k);

	}


	// ------------------------------------------------------------



} 

//final RailtypeInfo _railtypes[RAILTYPE_END];

class RailCursors {
	CursorID rail_ns;
	CursorID rail_swne;
	CursorID rail_ew;
	CursorID rail_nwse;
	CursorID autorail;
	CursorID depot;
	CursorID tunnel;
	CursorID convert;

	public RailCursors(int[] cur) {
		int i = 0;
		 rail_ns = CursorID.get( cur[i++]);
		 rail_swne = CursorID.get( cur[i++]);
		 rail_ew = CursorID.get( cur[i++]);
		 rail_nwse = CursorID.get( cur[i++]);
		 autorail = CursorID.get( cur[i++]);
		 depot = CursorID.get( cur[i++]);
		 tunnel = CursorID.get( cur[i++]);
		 convert = CursorID.get( cur[i++]);
	}
}

class RailGuiSprites {
	SpriteID build_ns_rail;      ///< button for building single rail in N-S direction
	SpriteID build_x_rail;       ///< button for building single rail in X direction
	SpriteID build_ew_rail;      ///< button for building single rail in E-W direction
	SpriteID build_y_rail;       ///< button for building single rail in Y direction
	SpriteID auto_rail;          ///< button for the autorail construction
	SpriteID build_depot;        ///< button for building depots
	SpriteID build_tunnel;       ///< button for building a tunnel
	SpriteID convert_rail;       ///< button for converting rail

	public RailGuiSprites(int[] spr) 
	{
		int i = 0;
		build_ns_rail = SpriteID.get( spr[i++]);      
		build_x_rail = SpriteID.get( spr[i++]);       
		build_ew_rail = SpriteID.get( spr[i++]);      
		build_y_rail = SpriteID.get( spr[i++]);       
		auto_rail = SpriteID.get( spr[i++]);          
		build_depot = SpriteID.get( spr[i++]);        
		build_tunnel = SpriteID.get( spr[i++]);       
		convert_rail = SpriteID.get( spr[i++]);       
	}

}

class RailBaseSprites {
	SpriteID track_y;      ///< single piece of rail in Y direction, with ground
	SpriteID track_ns;     ///< two pieces of rail in North and South corner (East-West direction)
	SpriteID ground;       ///< ground sprite for a 3-way switch
	SpriteID single_y;     ///< single piece of rail in Y direction, without ground
	SpriteID single_x;     ///< single piece of rail in X direction
	SpriteID single_n;     ///< single piece of rail in the northern corner
	SpriteID single_s;     ///< single piece of rail in the southern corner
	SpriteID single_e;     ///< single piece of rail in the eastern corner
	SpriteID single_w;     ///< single piece of rail in the western corner
	SpriteID crossing;     ///< level crossing, rail in X direction
	SpriteID tunnel;       ///< tunnel sprites base

	public RailBaseSprites(int[] spr) 
	{
		int i = 0;

		track_y = SpriteID.get( spr[i++]);      
		track_ns = SpriteID.get( spr[i++]);     
		ground = SpriteID.get( spr[i++]);       
		single_y = SpriteID.get( spr[i++]);     
		single_x = SpriteID.get( spr[i++]);     
		single_n = SpriteID.get( spr[i++]);     
		single_s = SpriteID.get( spr[i++]);     
		single_e = SpriteID.get( spr[i++]);     
		single_w = SpriteID.get( spr[i++]);     
		crossing = SpriteID.get( spr[i++]);     
		tunnel = SpriteID.get( spr[i++]);       
	}
} 















/** These types are used in the map5 byte for rail tiles. Use GetRailTileType() to
 * get these values * /
enum RailTileType 
{
	RAIL_TYPE_NORMAL         ( 0x0 ),
	RAIL_TYPE_SIGNALS        ( 0x40 ),
	RAIL_TYPE_UNUSED         ( 0x80 ), // XXX: Maybe this could become waypoints? 
	RAIL_TYPE_DEPOT_WAYPOINT ( 0xC0 ), // Is really depots and waypoints... 
	RAIL_TILE_TYPE_MASK      ( 0xC0 );

	private int value; 

	private RailTileType(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}

	public static final RailTileType values[] = values();

	static RailTileType get(int i) {
		return values[i];
	}
} 

//enum { // DEPRECATED TODO: Rewrite all uses of this 
//	RAIL_TYPE_SPECIAL = 0x80, 
/* This used to say "If this bit is set, then it's
 * not a regular track.", but currently, you
 * should rather view map5[6..7] as one type,
 * containing a value from RailTileTypes above.
 * This value is only maintained for backwards
 * compatibility */

/* There used to be RAIL_BIT_* enums here, they moved to (for now) npf.c as
 * TRACK_BIT_* */
//};

/** These subtypes are used in the map5 byte when the main rail type is
 * RAIL_TYPE_DEPOT_WAYPOINT * /
enum RailTileSubtype {
	RAIL_SUBTYPE_DEPOT    ( 0x00 ),
	RAIL_SUBTYPE_WAYPOINT ( 0x04 ),
	RAIL_SUBTYPE_MASK     ( 0x3C );

	private int value; 
	private RailTileSubtype(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}

	public static RailTileSubtype [] values = values();
	public static RailTileSubtype get(int i) {
		return values[i];
	}

} 

enum SignalType {
	// Stored in m4[0..1] for MP_RAILWAY 
	SIGTYPE_NORMAL  ( 0 ),        // normal signal
	SIGTYPE_ENTRY   ( 1 ),        // presignal block entry
	SIGTYPE_EXIT    (2),        // presignal block exit
	SIGTYPE_COMBO   (3),        // presignal inter-block
	SIGTYPE_PBS     (4),        // pbs signal
	SIGTYPE_END(5),
	SIGTYPE_MASK    ( 7 );

	private int value; 
	private SignalType(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}

	public static final SignalType values[] = values();
} 
/*
enum RailType {
	RAILTYPE_RAIL   (0),
	RAILTYPE_MONO   (1),
	RAILTYPE_MAGLEV (2),
	RAILTYPE_END (3),
	RAILTYPE_MASK   ( 0x3),
	INVALID_RAILTYPE (0xFF);

	private int value; 
	private RailType(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}

	public static final RailType values[] = values();

} 
 */

/** These are used to specify a single track. Can be translated to a trackbit
 * with TrackToTrackbit */
/*
enum Track {
	TRACK_DIAG1 (0),
	TRACK_DIAG2 (1),
	TRACK_UPPER (2),
	TRACK_LOWER (3),
	TRACK_LEFT  (4),
	TRACK_RIGHT (5),
	TRACK_END   (6),
	INVALID_TRACK (0xFF);

	private int value; 
	private Track(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}

} 

/** These are the bitfield variants of the above */
/*
enum TrackBits {
	TRACK_BIT_DIAG1 (1),  // 0
	TRACK_BIT_DIAG2 (2),  // 1
	TRACK_BIT_UPPER (4),  // 2
	TRACK_BIT_LOWER (8),  // 3
	TRACK_BIT_LEFT  (16), // 4
	TRACK_BIT_RIGHT (32), // 5
	TRACK_BIT_MASK  (0x3F);

	private int value; 
	private TrackBits(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}

	public static final TrackBits values[] = values();
	public static TrackBits get(int i) {
		return values[i];
	}

} 

/** These are a combination of tracks and directions. Values are 0-5 in one
direction (corresponding to the Track enum) and 8-13 in the other direction. */
/*
enum Trackdir {
	TRACKDIR_DIAG1_NE (0),
	TRACKDIR_DIAG2_SE (1),
	TRACKDIR_UPPER_E  (2),
	TRACKDIR_LOWER_E  (3),
	TRACKDIR_LEFT_S   (4),
	TRACKDIR_RIGHT_S  (5),
	// Note the two missing values here. This enables trackdir -> track
	// conversion by doing (trackdir & 7) 
	TRACKDIR_DIAG1_SW (8),
	TRACKDIR_DIAG2_NW (9),
	TRACKDIR_UPPER_W  (10),
	TRACKDIR_LOWER_W  (11),
	TRACKDIR_LEFT_N   (12),
	TRACKDIR_RIGHT_N  (13),
	TRACKDIR_END (14),
	INVALID_TRACKDIR  (0xFF);

	private int value; 
	private Trackdir(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}

} 

/** These are a combination of tracks and directions. Values are 0-5 in one
direction (corresponding to the Track enum) and 8-13 in the other direction. */
/*
enum TrackdirBits {
	TRACKDIR_BIT_DIAG1_NE ( 0x1),
	TRACKDIR_BIT_DIAG2_SE ( 0x2),
	TRACKDIR_BIT_UPPER_E  (0x4),
	TRACKDIR_BIT_LOWER_E  (0x8),
	TRACKDIR_BIT_LEFT_S   (0x10),
	TRACKDIR_BIT_RIGHT_S  (0x20),
	// Again, note the two missing values here. This enables trackdir -> track conversion by doing (trackdir & 0xFF) 
	TRACKDIR_BIT_DIAG1_SW (0x0100),
	TRACKDIR_BIT_DIAG2_NW (0x0200),
	TRACKDIR_BIT_UPPER_W  (0x0400),
	TRACKDIR_BIT_LOWER_W  (0x0800),
	TRACKDIR_BIT_LEFT_N   (0x1000),
	TRACKDIR_BIT_RIGHT_N  (0x2000),
	TRACKDIR_BIT_MASK	  (0x3F3F),
	INVALID_TRACKDIR_BIT   (0xFFFF);

	private int value; 
	private TrackdirBits(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}

	public static final TrackdirBits values[] = values();

} 

/** These are states in which a signal can be. Currently these are only two, so
 * simple boolean logic will do. But do try to compare to this enum instead of
 * normal boolean evaluation, since that will make future additions easier.
 */
/*
enum SignalState {
	SIGNAL_STATE_RED (0),
	SIGNAL_STATE_GREEN (1);

	private int value; 
	private SignalState(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}
*/
 

