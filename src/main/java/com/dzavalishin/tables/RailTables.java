package com.dzavalishin.tables;

import com.dzavalishin.game.RailtypeInfo;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.Tile;
import com.dzavalishin.util.Sprites;

public class RailTables {

	/*
	 * Some enums for accesing the map bytes for rail tiles
	 */

	/** These types are used in the map5 byte for rail tiles. Use GetRailTileType() to
	 * get these values */
	//typedef enum RailTileTypes {
	public static final int RAIL_TYPE_NORMAL         = 0x0;
	public static final int RAIL_TYPE_SIGNALS        = 0x40;
	public static final int RAIL_TYPE_UNUSED         = 0x80; /* XXX: Maybe this could become waypoints? */
	public static final int RAIL_TYPE_DEPOT_WAYPOINT = 0xC0; /* Is really depots and waypoints... */
	public static final int RAIL_TILE_TYPE_MASK      = 0xC0;
	//} RailTileType;

	//enum { /* DEPRECATED TODO: Rewrite all uses of this */
	public static final int RAIL_TYPE_SPECIAL = 0x80; /* This used to say "If this bit is set; then it's
	 * not a regular track."; but currently; you
	 * should rather view map5[6..7] as one type;
	 * containing a value from RailTileTypes above.
	 * This value is only maintained for backwards
	 * compatibility */

	/* There used to be RAIL_BIT_* enums here; they moved to (for now) npf.c as
	 * public static final int TRACK_BIT_* */
	//};







	/** These subtypes are used in the map5 byte when the main rail type is
	 * RAIL_TYPE_DEPOT_WAYPOINT */
	//typedef enum RailTileSubtypes {
	public static final int RAIL_SUBTYPE_DEPOT    = 0x00;
	public static final int RAIL_SUBTYPE_WAYPOINT = 0x04;
	public static final int RAIL_SUBTYPE_MASK     = 0x3C;
	//} RailTileSubtype;

	//typedef enum SignalTypes {
	/* Stored in m4[0..1] for TileTypes.MP_RAILWAY */
	public static final int SIGTYPE_NORMAL  = 0;        // normal signal
	public static final int SIGTYPE_ENTRY   = 1;        // presignal block entry
	public static final int SIGTYPE_EXIT    = 2;        // presignal block exit
	public static final int SIGTYPE_COMBO   = 3;        // presignal inter-block
	public static final int SIGTYPE_PBS     = 4;        // pbs signal
	public static final int SIGTYPE_END = 5;
	public static final int SIGTYPE_MASK    = 7;
	//} SignalType;

	//typedef enum RailTypes {
	public static final int RAILTYPE_RAIL   = 0;
	public static final int RAILTYPE_MONO   = 1;
	public static final int RAILTYPE_MAGLEV = 2;
	public static final int RAILTYPE_END = 3;
	public static final int RAILTYPE_MASK   = 0x3;
	public static final int INVALID_RAILTYPE = 0xFF;
	//} RailType;

	//enum {
	public static final int SIG_SEMAPHORE_MASK = 1 << 3;
	//};

	/** These are used to specify a single track. Can be translated to a trackbit
	 * with TrackToTrackbit */
	//typedef enum Tracks {
	public static final int TRACK_DIAG1 = 0;
	public static final int TRACK_DIAG2 = 1;
	public static final int TRACK_UPPER = 2;
	public static final int TRACK_LOWER = 3;
	public static final int TRACK_LEFT  = 4;
	public static final int TRACK_RIGHT = 5;
	public static final int TRACK_END = 6;
	public static final int INVALID_TRACK = 0xFF;
	//} Track;

	/** These are the bitfield variants of the above */
	//typedef enum TrackBits {
	public static final int TRACK_BIT_DIAG1 = 1;  // 0
	public static final int TRACK_BIT_DIAG2 = 2;  // 1
	public static final int TRACK_BIT_UPPER = 4;  // 2
	public static final int TRACK_BIT_LOWER = 8;  // 3
	public static final int TRACK_BIT_LEFT  = 16; // 4
	public static final int TRACK_BIT_RIGHT = 32; // 5
	public static final int TRACK_BIT_MASK  = 0x3F;
	//} TrackBits;

	/** These are a combination of tracks and directions. Values are 0-5 in one
		direction (corresponding to the Track enum) and 8-13 in the other direction. */
	//typedef enum Trackdirs {
	public static final int TRACKDIR_DIAG1_NE = 0;
	public static final int TRACKDIR_DIAG2_SE = 1;
	public static final int TRACKDIR_UPPER_E  = 2;
	public static final int TRACKDIR_LOWER_E  = 3;
	public static final int TRACKDIR_LEFT_S   = 4;
	public static final int TRACKDIR_RIGHT_S  = 5;
	/* Note the two missing values here. This enables trackdir . track
	 * conversion by doing (trackdir & 7) */
	public static final int TRACKDIR_DIAG1_SW = 8;
	public static final int TRACKDIR_DIAG2_NW = 9;
	public static final int TRACKDIR_UPPER_W  = 10;
	public static final int TRACKDIR_LOWER_W  = 11;
	public static final int TRACKDIR_LEFT_N   = 12;
	public static final int TRACKDIR_RIGHT_N  = 13;
	public static final int TRACKDIR_END = 14;
	public static final int INVALID_TRACKDIR  = 0xFF;
	//} Trackdir;

	/** These are a combination of tracks and directions. Values are 0-5 in one
		direction (corresponding to the Track enum) and 8-13 in the other direction. */


	//typedef enum TrackdirBits {
	public static final int TRACKDIR_BIT_DIAG1_NE = 0x1;
	public static final int TRACKDIR_BIT_DIAG2_SE = 0x2;
	public static final int TRACKDIR_BIT_UPPER_E  = 0x4;
	public static final int TRACKDIR_BIT_LOWER_E  = 0x8;
	public static final int TRACKDIR_BIT_LEFT_S   = 0x10;
	public static final int TRACKDIR_BIT_RIGHT_S  = 0x20;
	// Again; note the two missing values here. This enables trackdir . track conversion by doing (trackdir & 0xFF) 
	public static final int TRACKDIR_BIT_DIAG1_SW = 0x0100;
	public static final int TRACKDIR_BIT_DIAG2_NW = 0x0200;
	public static final int TRACKDIR_BIT_UPPER_W  = 0x0400;
	public static final int TRACKDIR_BIT_LOWER_W  = 0x0800;
	public static final int TRACKDIR_BIT_LEFT_N   = 0x1000;
	public static final int TRACKDIR_BIT_RIGHT_N  = 0x2000;
	public static final int TRACKDIR_BIT_MASK			= 0x3F3F;
	public static final int INVALID_TRACKDIR_BIT  = 0xFFFF;
	//} TrackdirBits; */

	/** These are states in which a signal can be. Currently these are only two; so
	 * simple booleanean logic will do. But do try to compare to this enum instead of
	 * normal booleanean evaluation; since that will make future additions easier.
	 */
	//typedef enum SignalStates {
	public static final int SIGNAL_STATE_RED = 0;
	public static final int SIGNAL_STATE_GREEN = 1;
	//} SignalState;









	//static final byte _valid_tileh_slopes[4][15] = 
	protected static final byte _valid_tileh_slopes[][] = 
		{

				// set of normal ones
				{
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,
					TRACK_BIT_RIGHT,
					TRACK_BIT_UPPER,
					TRACK_BIT_DIAG1,

					TRACK_BIT_LEFT,
					0,
					TRACK_BIT_DIAG2,
					TRACK_BIT_LOWER,

					TRACK_BIT_LOWER,
					TRACK_BIT_DIAG2,
					0,
					TRACK_BIT_LEFT,

					TRACK_BIT_DIAG1,
					TRACK_BIT_UPPER,
					TRACK_BIT_RIGHT,
				},

				// allowed rail for an evenly raised platform
				{
					0,
					TRACK_BIT_LEFT,
					TRACK_BIT_LOWER,
					TRACK_BIT_DIAG2 | TRACK_BIT_LOWER | TRACK_BIT_LEFT,

					TRACK_BIT_RIGHT,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,
					TRACK_BIT_DIAG1 | TRACK_BIT_LOWER | TRACK_BIT_RIGHT,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,

					TRACK_BIT_UPPER,
					TRACK_BIT_DIAG1 | TRACK_BIT_UPPER | TRACK_BIT_LEFT,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,

					TRACK_BIT_DIAG2 | TRACK_BIT_UPPER | TRACK_BIT_RIGHT,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,
				},

				// allowed rail on coast tile
				{
					0,
					TRACK_BIT_LEFT,
					TRACK_BIT_LOWER,
					TRACK_BIT_DIAG2|TRACK_BIT_LEFT|TRACK_BIT_LOWER,

					TRACK_BIT_RIGHT,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,
					TRACK_BIT_DIAG1|TRACK_BIT_RIGHT|TRACK_BIT_LOWER,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,

					TRACK_BIT_UPPER,
					TRACK_BIT_DIAG1|TRACK_BIT_LEFT|TRACK_BIT_UPPER,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,

					TRACK_BIT_DIAG2|TRACK_BIT_RIGHT|TRACK_BIT_UPPER,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,
					TRACK_BIT_DIAG1|TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LOWER|TRACK_BIT_LEFT|TRACK_BIT_RIGHT,
				},

				// valid railway crossings on slopes
				{
					1, 0, 0, // 0, 1, 2
					0, 0, 1, // 3, 4, 5
					0, 1, 0, // 6, 7, 8
					0, 1, 1, // 9, 10, 11
					0, 1, 1, // 12, 13, 14
				}
		};











	/* Format of rail map5 byte.
	 * 00 abcdef  => Normal rail
	 * 01 abcdef  => Rail with signals
	 * 10 ??????  => Unused
	 * 11 ????dd  => Depot
	 *
	 * abcdef is a bitmask, which contains ones for all present tracks. Below the
	 * value for each track is given.
	 */

	/*         4
	 *     ---------
	 *    |\       /|
	 *    | \    1/ |
	 *    |  \   /  |
	 *    |   \ /   |
	 *  16|    \    |32
	 *    |   / \2  |
	 *    |  /   \  |
	 *    | /     \ |
	 *    |/       \|
	 *     ---------
	 *         8
	 */


	// Constants for lower part of Map2 byte.
	//enum RailMap2Lower4 {
	public static final int RAIL_MAP2LO_GROUND_MASK = 0xF;
	public static final int RAIL_GROUND_BROWN = 0;
	public static final int RAIL_GROUND_GREEN = 1;
	public static final int RAIL_GROUND_FENCE_NW = 2;
	public static final int RAIL_GROUND_FENCE_SE = 3;
	public static final int RAIL_GROUND_FENCE_SENW = 4;
	public static final int RAIL_GROUND_FENCE_NE = 5;
	public static final int RAIL_GROUND_FENCE_SW = 6;
	public static final int RAIL_GROUND_FENCE_NESW = 7;
	public static final int RAIL_GROUND_FENCE_VERT1 = 8;
	public static final int RAIL_GROUND_FENCE_VERT2 = 9;
	public static final int RAIL_GROUND_FENCE_HORIZ1 = 10;
	public static final int RAIL_GROUND_FENCE_HORIZ2 = 11;
	public static final int RAIL_GROUND_ICE_DESERT = 12;
	//};
















	/* Below 3 tables store duplicate data. Maybe remove some? */
	/* Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction along with the trackdir */
	protected static final int _signal_along_trackdir[] = {
			0x80, 0x80, 0x80, 0x20, 0x40, 0x10, 0, 0,
			0x40, 0x40, 0x40, 0x10, 0x80, 0x20
	};

	/* Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction against the trackdir */
	protected static final int _signal_against_trackdir[] = {
			0x40, 0x40, 0x40, 0x10, 0x80, 0x20, 0, 0,
			0x80, 0x80, 0x80, 0x20, 0x40, 0x10
	};

	/* Maps a Track to the bits that store the status of the two signals that can
	 * be present on the given track */
	protected static final int _signal_on_track[] = {
			0xC0, 0xC0, 0xC0, 0x30, 0xC0, 0x30
	};

	/* Maps a diagonal direction to the all trackdirs that are connected to any
	 * track entering in this direction (including those making 90 degree turns)
	 */
	//final TrackdirBits _exitdir_reaches_trackdirs[] = 
	protected static final int _exitdir_reaches_trackdirs[] = 
		{
				TRACKDIR_BIT_DIAG1_NE | TRACKDIR_BIT_LOWER_E | TRACKDIR_BIT_LEFT_N,  /* DIAGDIR_NE */
				TRACKDIR_BIT_DIAG2_SE | TRACKDIR_BIT_LEFT_S  | TRACKDIR_BIT_UPPER_E, /* DIAGDIR_SE */
				TRACKDIR_BIT_DIAG1_SW | TRACKDIR_BIT_UPPER_W | TRACKDIR_BIT_RIGHT_S, /* DIAGDIR_SW */
				TRACKDIR_BIT_DIAG2_NW | TRACKDIR_BIT_RIGHT_N | TRACKDIR_BIT_LOWER_W  /* DIAGDIR_NW */
		};

	//final Trackdir _next_trackdir[] = 
	protected static final int _next_trackdir[] = 
		{
				TRACKDIR_DIAG1_NE,  TRACKDIR_DIAG2_SE,  TRACKDIR_LOWER_E, TRACKDIR_UPPER_E, TRACKDIR_RIGHT_S, TRACKDIR_LEFT_S, INVALID_TRACKDIR, INVALID_TRACKDIR,
				TRACKDIR_DIAG1_SW,  TRACKDIR_DIAG2_NW,  TRACKDIR_LOWER_W, TRACKDIR_UPPER_W, TRACKDIR_RIGHT_N, TRACKDIR_LEFT_N
		};

	/* Maps a trackdir to all trackdirs that make 90 deg turns with it. */
	//final TrackdirBits _track_crosses_trackdirs[] = 
	protected static final int _track_crosses_trackdirs[] = 
		{
				TRACKDIR_BIT_DIAG2_SE | TRACKDIR_BIT_DIAG2_NW,                                               /* TRACK_DIAG1 */
				TRACKDIR_BIT_DIAG1_NE | TRACKDIR_BIT_DIAG1_SW,                                               /* TRACK_DIAG2 */
				TRACKDIR_BIT_RIGHT_N  | TRACKDIR_BIT_RIGHT_S  | TRACKDIR_BIT_LEFT_N  | TRACKDIR_BIT_LEFT_S,  /* TRACK_UPPER */
				TRACKDIR_BIT_RIGHT_N  | TRACKDIR_BIT_RIGHT_S  | TRACKDIR_BIT_LEFT_N  | TRACKDIR_BIT_LEFT_S,  /* TRACK_LOWER */
				TRACKDIR_BIT_UPPER_W  | TRACKDIR_BIT_UPPER_E  | TRACKDIR_BIT_LOWER_W | TRACKDIR_BIT_LOWER_E, /* TRACK_LEFT  */
				TRACKDIR_BIT_UPPER_W  | TRACKDIR_BIT_UPPER_E  | TRACKDIR_BIT_LOWER_W | TRACKDIR_BIT_LOWER_E  /* TRACK_RIGHT */
		};

	/* Maps a track to all tracks that make 90 deg turns with it. */
	//final TrackBits _track_crosses_tracks[] = 
	protected static final int _track_crosses_tracks[] = 
		{
				TRACK_BIT_DIAG2,                   /* TRACK_DIAG1 */
				TRACK_BIT_DIAG1,                   /* TRACK_DIAG2 */
				TRACK_BIT_LEFT  | TRACK_BIT_RIGHT, /* TRACK_UPPER */
				TRACK_BIT_LEFT  | TRACK_BIT_RIGHT, /* TRACK_LOWER */
				TRACK_BIT_UPPER | TRACK_BIT_LOWER, /* TRACK_LEFT  */
				TRACK_BIT_UPPER | TRACK_BIT_LOWER  /* TRACK_RIGHT */
		};

	/* Maps a trackdir to the (4-way) direction the tile is exited when following
	 * that trackdir */


	//final DiagDirection _trackdir_to_exitdir[] = 
	protected static final int _trackdir_to_exitdir[] = 
		{
				Tile.DIAGDIR_NE,Tile.DIAGDIR_SE,Tile.DIAGDIR_NE,Tile.DIAGDIR_SE,Tile.DIAGDIR_SW,Tile.DIAGDIR_SE, Tile.DIAGDIR_NE,Tile.DIAGDIR_NE,
				Tile.DIAGDIR_SW,Tile.DIAGDIR_NW,Tile.DIAGDIR_NW,Tile.DIAGDIR_SW,Tile.DIAGDIR_NW,Tile.DIAGDIR_NE,
		}; 

	//final Trackdir _track_exitdir_to_trackdir[][DIAGDIR_END] = 
	protected static final int _track_exitdir_to_trackdir[][] = 
		{
				{TRACKDIR_DIAG1_NE, INVALID_TRACKDIR,  TRACKDIR_DIAG1_SW, INVALID_TRACKDIR},
				{INVALID_TRACKDIR,  TRACKDIR_DIAG2_SE, INVALID_TRACKDIR,  TRACKDIR_DIAG2_NW},
				{TRACKDIR_UPPER_E,  INVALID_TRACKDIR,  INVALID_TRACKDIR,  TRACKDIR_UPPER_W},
				{INVALID_TRACKDIR,  TRACKDIR_LOWER_E,  TRACKDIR_LOWER_W,  INVALID_TRACKDIR},
				{INVALID_TRACKDIR,  INVALID_TRACKDIR,  TRACKDIR_LEFT_S,   TRACKDIR_LEFT_N},
				{TRACKDIR_RIGHT_N,  TRACKDIR_RIGHT_S,  INVALID_TRACKDIR,  INVALID_TRACKDIR}
		};

	//final Trackdir _track_enterdir_to_trackdir[][DIAGDIR_END] = 
	protected static final int _track_enterdir_to_trackdir[][] = 
		{ 
				{TRACKDIR_DIAG1_NE, INVALID_TRACKDIR,  TRACKDIR_DIAG1_SW, INVALID_TRACKDIR},
				{INVALID_TRACKDIR,  TRACKDIR_DIAG2_SE, INVALID_TRACKDIR,  TRACKDIR_DIAG2_NW},
				{INVALID_TRACKDIR,  TRACKDIR_UPPER_E,  TRACKDIR_UPPER_W,  INVALID_TRACKDIR},
				{TRACKDIR_LOWER_E,  INVALID_TRACKDIR,  INVALID_TRACKDIR,  TRACKDIR_LOWER_W},
				{TRACKDIR_LEFT_N,   TRACKDIR_LEFT_S,   INVALID_TRACKDIR,  INVALID_TRACKDIR},
				{INVALID_TRACKDIR,  INVALID_TRACKDIR,  TRACKDIR_RIGHT_S,  TRACKDIR_RIGHT_N}
		};

	//final Trackdir _track_direction_to_trackdir[][DIR_END] = 
	protected static final int _track_direction_to_trackdir[][] = 
		{
				{INVALID_TRACKDIR, TRACKDIR_DIAG1_NE, INVALID_TRACKDIR, INVALID_TRACKDIR,  INVALID_TRACKDIR, TRACKDIR_DIAG1_SW, INVALID_TRACKDIR, INVALID_TRACKDIR},
				{INVALID_TRACKDIR, INVALID_TRACKDIR,  INVALID_TRACKDIR, TRACKDIR_DIAG2_SE, INVALID_TRACKDIR, INVALID_TRACKDIR,  INVALID_TRACKDIR, TRACKDIR_DIAG2_NW},
				{INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_UPPER_E, INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_UPPER_W, INVALID_TRACKDIR},
				{INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_LOWER_E, INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_LOWER_W, INVALID_TRACKDIR},
				{TRACKDIR_LEFT_N,  INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_LEFT_S,  INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR},
				{TRACKDIR_RIGHT_N, INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_RIGHT_S, INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR}
		};

	//final Trackdir _dir_to_diag_trackdir[] = 
	protected static final int _dir_to_diag_trackdir[] = 
		{
				TRACKDIR_DIAG1_NE, TRACKDIR_DIAG2_SE, TRACKDIR_DIAG1_SW, TRACKDIR_DIAG2_NW,
		};

	//final DiagDirection _reverse_diagdir[] = 
	protected static final int _reverse_diagdir[] = 
		{
				Tile.DIAGDIR_SW, Tile.DIAGDIR_NW, Tile.DIAGDIR_NE, Tile.DIAGDIR_SE
		};

	//final Trackdir _reverse_trackdir[] = 
	protected static final int _reverse_trackdir[] = 
		{
				TRACKDIR_DIAG1_SW, TRACKDIR_DIAG2_NW, TRACKDIR_UPPER_W, TRACKDIR_LOWER_W, TRACKDIR_LEFT_N, TRACKDIR_RIGHT_N, INVALID_TRACKDIR, INVALID_TRACKDIR,
				TRACKDIR_DIAG1_NE, TRACKDIR_DIAG2_SE, TRACKDIR_UPPER_E, TRACKDIR_LOWER_E, TRACKDIR_LEFT_S, TRACKDIR_RIGHT_S
		};







	public static final byte _track_sloped_sprites[] = {
			14, 15, 22, 13,
			0, 21, 17, 12,
			23,  0, 18, 20,
			19, 16
	};




	protected static final int _signals_table[] = {
			0x40, 0x40, 0x40, 0x10, 0x80, 0x20, 0, 0, // direction 1
			0x80, 0x80, 0x80, 0x20, 0x40, 0x10, 0, 0  // direction 2
	};

	protected static final int _signals_table_other[] = {
			0x80, 0x80, 0x80, 0x20, 0x40, 0x10, 0, 0, // direction 1
			0x40, 0x40, 0x40, 0x10, 0x80, 0x20, 0, 0  // direction 2
	};

	protected static final int _signals_table_both[] = {
			0xC0, 0xC0, 0xC0, 0x30, 0xC0, 0x30, 0, 0,	// both directions combined
			0xC0, 0xC0, 0xC0, 0x30, 0xC0, 0x30, 0, 0
	};


	private static final int [] xinc = {
			//  0   1   2   3   4   5
			-16,  0,-16,  0, 16,  0,    0,  0,
			16,  0,  0, 16,  0,-16,    0,  0,
	};

	private static final int [] yinc = {
			0, 16,  0, 16,  0, 16,    0,  0,
			0,-16,-16,  0,-16,  0,    0,  0,
	};		

	public static class _RailBit {
		public final int[] xinc;
		public final int[] yinc;
		public _RailBit(int[] xinc2, int[] yinc2) {
			xinc = xinc2;
			yinc = yinc2;
		}
	}

	public static final _RailBit _railbit = new _RailBit( xinc, yinc );



	public static final byte _dir_from_track[] = {
			0,1,0,1,2,1, 0,0,
			2,3,3,2,3,0,
	};






	// used for presignals
	//static final SpriteID _signal_base_sprites[32] = {
	protected static final int _signal_base_sprites[] = {
			0x4FB,
			0x1323,
			0x1333,
			0x1343,

			// pbs signals
			0x1393,
			0x13A3,  // not used (yet?)
			0x13B3,  // not used (yet?)
			0x13C3,  // not used (yet?)

			// semaphores
			0x1353,
			0x1363,
			0x1373,
			0x1383,

			// pbs semaphores
			0x13D3,
			0x13E3,  // not used (yet?)
			0x13F3,  // not used (yet?)
			0x1403,  // not used (yet?)


			// mirrored versions
			0x4FB,
			0x1323,
			0x1333,
			0x1343,

			// pbs signals
			0x1393,
			0x13A3,  // not used (yet?)
			0x13B3,  // not used (yet?)
			0x13C3,  // not used (yet?)

			// semaphores
			0x1446,
			0x1456,
			0x1466,
			0x1476,

			// pbs semaphores
			0x14C6,
			0x14D6,  // not used (yet?)
			0x14E6,  // not used (yet?)
			0x14F6,  // not used (yet?)
	};

	// used to determine the side of the road for the signal
	protected static final int _signal_position[] = {
			/* original: left side position */
			0x58,0x1E,0xE1,0xB9,0x01,0xA3,0x4B,0xEE,0x3B,0xD4,0x43,0xBD,
			/* patch: ride side position */
			0x1E,0xAC,0x64,0xE1,0x4A,0x10,0xEE,0xC5,0xDB,0x34,0x4D,0xB3
	};


	protected static final int _fractcoords_behind[] = { 0x8F, 0x8, 0x80, 0xF8 };
	protected static final int _fractcoords_enter[] = { 0x8A, 0x48, 0x84, 0xA8 };
	protected static final int _deltacoord_leaveoffset[] = {
			-1,  0,  1,  0, /* x */
			0,  1,  0, -1  /* y */
	};
	protected static final int _enter_directions[] = {5, 7, 1, 3};
	protected static final int _leave_directions[] = {1, 3, 5, 7};
	protected static final int _depot_track_mask[] = {1, 2, 1, 2};

	protected static final byte _train_spec_tracks[] = {1,2,1,2,1,2};


	protected static final byte _search_dir_1[] = {1, 3, 1, 3, 5, 3};
	protected static final byte _search_dir_2[] = {5, 7, 7, 5, 7, 1};




	/** Enum referring to the widgets of the build rail toolbar
	 */
	//typedef enum {
	public static final int RTW_CAPTION = 1;
	public static final int RTW_BUILD_NS = 4;
	public static final int RTW_BUILD_X = 5;
	public static final int RTW_BUILD_EW = 6;
	public static final int RTW_BUILD_Y = 7;
	public static final int RTW_AUTORAIL = 8;
	public static final int RTW_BUILD_DEPOT = 10;
	public static final int RTW_BUILD_TUNNEL = 15;
	public static final int RTW_CONVERT_RAIL = 17;
	//} RailToolbarWidgets;
















	/* Railway Main Sprites */
	private static final int [] spr_rail_1 = { Sprites.SPR_RAIL_TRACK_Y, Sprites.SPR_RAIL_TRACK_N_S, Sprites.SPR_RAIL_TRACK_BASE, Sprites.SPR_RAIL_SINGLE_Y, Sprites.SPR_RAIL_SINGLE_X,
			Sprites.SPR_RAIL_SINGLE_NORTH, Sprites.SPR_RAIL_SINGLE_SOUTH, Sprites.SPR_RAIL_SINGLE_EAST, Sprites.SPR_RAIL_SINGLE_WEST,
			Sprites.SPR_CROSSING_OFF_X_RAIL,
			Sprites.SPR_TUNNEL_ENTRY_REAR_RAIL
	};
	private static final int [] spr_rail_gui = { 0x4E3, 0x4E4, 0x4E5, 0x4E6,
			Sprites.SPR_IMG_AUTORAIL,
			Sprites.SPR_IMG_DEPOT_RAIL,
			Sprites.SPR_IMG_TUNNEL_RAIL,
			Sprites.SPR_IMG_CONVERT_RAIL
	}; 
	private static final int [] spr_rail_cursors = {
			Sprites.SPR_CURSOR_NS_TRACK,
			Sprites.SPR_CURSOR_SWNE_TRACK,
			Sprites.SPR_CURSOR_EW_TRACK,
			Sprites.SPR_CURSOR_NWSE_TRACK,
			Sprites.SPR_CURSOR_AUTORAIL,
			Sprites.SPR_CURSOR_RAIL_DEPOT,
			Sprites.SPR_CURSOR_TUNNEL_RAIL,
			Sprites.SPR_CURSOR_CONVERT_RAIL
	}; 
	//private static final int [] spr_rail_str = {  };
	
	
	private static final int [] spr_mono_main = 
		{ Sprites.SPR_MONO_TRACK_Y, Sprites.SPR_MONO_TRACK_N_S, Sprites.SPR_MONO_TRACK_BASE, Sprites.SPR_MONO_SINGLE_Y, Sprites.SPR_MONO_SINGLE_X,
				Sprites.SPR_MONO_SINGLE_NORTH, Sprites.SPR_MONO_SINGLE_SOUTH, Sprites.SPR_MONO_SINGLE_EAST, Sprites.SPR_MONO_SINGLE_WEST,
				Sprites.SPR_CROSSING_OFF_X_MONO,
				Sprites.SPR_TUNNEL_ENTRY_REAR_MONO
			}; 
	private static final int [] spr_mono_gui = 
		{ 0x4E7, 0x4E8, 0x4E9, 0x4EA,
			Sprites.SPR_IMG_AUTOMONO,
			Sprites.SPR_IMG_DEPOT_MONO,
			Sprites.SPR_IMG_TUNNEL_MONO,
			Sprites.SPR_IMG_CONVERT_MONO
		}; 
	private static final int [] spr_mono_cursors =
		{
				Sprites.SPR_CURSOR_NS_MONO,
				Sprites.SPR_CURSOR_SWNE_MONO,
				Sprites.SPR_CURSOR_EW_MONO,
				Sprites.SPR_CURSOR_NWSE_MONO,
				Sprites.SPR_CURSOR_AUTOMONO,
				Sprites.SPR_CURSOR_MONO_DEPOT,
				Sprites.SPR_CURSOR_TUNNEL_MONO,
				Sprites.SPR_CURSOR_CONVERT_MONO
			};
	//private static final int [] spr_mono_str = { Str.STR_100B_MONORAIL_CONSTRUCTION };

	private static final int [] spr_mag_main = 
		{ Sprites.SPR_MGLV_TRACK_Y, Sprites.SPR_MGLV_TRACK_N_S, Sprites.SPR_MGLV_TRACK_BASE, Sprites.SPR_MGLV_SINGLE_Y, Sprites.SPR_MGLV_SINGLE_X,
			Sprites.SPR_MGLV_SINGLE_NORTH, Sprites.SPR_MGLV_SINGLE_SOUTH, Sprites.SPR_MGLV_SINGLE_EAST, Sprites.SPR_MGLV_SINGLE_WEST,
			Sprites.SPR_CROSSING_OFF_X_MAGLEV,
			Sprites.SPR_TUNNEL_ENTRY_REAR_MAGLEV
		}; 
	private static final int [] spr_mag_gui = 
		{ 0x4EB, 0x4EC, 0x4EE, 0x4ED,
				Sprites.SPR_IMG_AUTOMAGLEV,
				Sprites.SPR_IMG_DEPOT_MAGLEV,
				Sprites.SPR_IMG_TUNNEL_MAGLEV,
				Sprites.SPR_IMG_CONVERT_MAGLEV
			}; 
	private static final int [] spr_mag_cursors = 
		{
				Sprites.SPR_CURSOR_NS_MAGLEV,
				Sprites.SPR_CURSOR_SWNE_MAGLEV,
				Sprites.SPR_CURSOR_EW_MAGLEV,
				Sprites.SPR_CURSOR_NWSE_MAGLEV,
				Sprites.SPR_CURSOR_AUTOMAGLEV,
				Sprites.SPR_CURSOR_MAGLEV_DEPOT,
				Sprites.SPR_CURSOR_TUNNEL_MAGLEV,
				Sprites.SPR_CURSOR_CONVERT_MAGLEV
			};
	//private static final int [] spr_mag_str = { Str.STR_100C_MAGLEV_CONSTRUCTION }; 	
	
	/** Global Railtype definition
	 */
	public static final RailtypeInfo _railtypes[] = {
			/** Railway */
			new RailtypeInfo( /* Main Sprites */
					spr_rail_1,
					/* GUI sprites */
					spr_rail_gui,
					spr_rail_cursors,
					/* strings */
					Str.STR_100A_RAILROAD_CONSTRUCTION,
					/* Offset of snow tiles */
					Sprites.SPR_RAIL_SNOW_OFFSET,
					/* Compatible railtypes */
					(1 << RAILTYPE_RAIL),
					/* main offset */
					0,
					/* bridge offset */
					0
					),

			/** Monorail */
			new RailtypeInfo( /* Main Sprites */
					spr_mono_main,
					/* GUI sprites */
					spr_mono_gui,
					spr_mono_cursors,
					/* strings */
					Str.STR_100B_MONORAIL_CONSTRUCTION,
					/* Offset of snow tiles */
					Sprites.SPR_MONO_SNOW_OFFSET,
					/* Compatible Railtypes */
					(1 << RAILTYPE_MONO),
					/* main offset */
					82,
					/* bridge offset */
					16
					),

			/** Maglev */
			new RailtypeInfo( /* Main sprites */
					spr_mag_main,
					/* GUI sprites */
					spr_mag_gui,
					spr_mag_cursors,
					/* strings */
					Str.STR_100C_MAGLEV_CONSTRUCTION,
					/* Offset of snow tiles */
					Sprites.SPR_MGLV_SNOW_OFFSET,
					/* Compatible Railtypes */
					(1 << RAILTYPE_MAGLEV),
					/* main offset */
					164,
					/* bridge offset */
					24
					),
	};





	
	public static class DrawTrackSeqStruct {
		public DrawTrackSeqStruct(int x) {
			 image = x;
			 subcoord_x = subcoord_y =
			 width = height = 0;
		}
		
		public DrawTrackSeqStruct(int a, int b, int c, int d, int e) {
			 image = a;
			 subcoord_x = b;
			 subcoord_y = c;
			 width = d;
			 height = e;
		}

		//SpriteID
		public final int image;
		public final int subcoord_x;
		public final int subcoord_y;
		public final int width;
		public final int height;
	}

	//#define TILE_SEQ_BEGIN(x) { x, 0, 0, 0, 0 },
	//#define TILE_SEQ_LINE(a, b, c, d, e) { a, b, c, d, e },
	//#define TILE_SEQ_END() { 0, 0, 0, 0, 0 }

	private static DrawTrackSeqStruct TILE_SEQ_BEGIN(int x) { return new DrawTrackSeqStruct(x); }
	private static DrawTrackSeqStruct TILE_SEQ_END() { return new DrawTrackSeqStruct(0); }
	private static DrawTrackSeqStruct TILE_SEQ_LINE(int a, int b, int c, int d, int e) { return new DrawTrackSeqStruct(a, b, c, d, e); }
	
	static final DrawTrackSeqStruct _track_depot_layout_table_0[] = {
		TILE_SEQ_BEGIN(0xF8D),
		TILE_SEQ_LINE(0x42B | Sprite.PALETTE_MODIFIER_COLOR, 2, 13, 13, 1),
		TILE_SEQ_END(),
	};

	static final DrawTrackSeqStruct _track_depot_layout_table_1[] = {
		TILE_SEQ_BEGIN(0x3F3 | Sprite.PALETTE_MODIFIER_COLOR),
		TILE_SEQ_LINE(0x427 | Sprite.PALETTE_MODIFIER_COLOR, 2, 2, 1, 13),
		TILE_SEQ_LINE(0x428 | Sprite.PALETTE_MODIFIER_COLOR, 13, 2, 1, 13),
		TILE_SEQ_END(),
	};

	static final DrawTrackSeqStruct _track_depot_layout_table_2[] = {
		TILE_SEQ_BEGIN(0x3F4 | Sprite.PALETTE_MODIFIER_COLOR),
		TILE_SEQ_LINE(0x429 | Sprite.PALETTE_MODIFIER_COLOR, 2, 2, 13, 1),
		TILE_SEQ_LINE(0x42A | Sprite.PALETTE_MODIFIER_COLOR, 2, 13, 13, 1),
		TILE_SEQ_END(),
	};

	static final DrawTrackSeqStruct _track_depot_layout_table_3[] = {
		TILE_SEQ_BEGIN(0xF8D),
		TILE_SEQ_LINE(0x42C | Sprite.PALETTE_MODIFIER_COLOR, 13, 2, 1, 13),
		TILE_SEQ_END(),
	};

	static final DrawTrackSeqStruct _track_waypoint_table_0[] = {
		TILE_SEQ_BEGIN(0x3F4 | Sprite.PALETTE_MODIFIER_COLOR),
		TILE_SEQ_LINE(Sprite.PALETTE_MODIFIER_COLOR | Sprite.SPR_WAYPOINT_X_1,  0,  0,  16,  5),
		TILE_SEQ_LINE(Sprite.PALETTE_MODIFIER_COLOR | Sprite.SPR_WAYPOINT_X_2,  0, 11,  16,  5),
		TILE_SEQ_END(),
	};

	static final DrawTrackSeqStruct _track_waypoint_table_1[] = {
		TILE_SEQ_BEGIN(0x3F3 | Sprite.PALETTE_MODIFIER_COLOR),
		TILE_SEQ_LINE(Sprite.PALETTE_MODIFIER_COLOR | Sprite.SPR_WAYPOINT_Y_1,   0,  0, 5, 16),
		TILE_SEQ_LINE(Sprite.PALETTE_MODIFIER_COLOR | Sprite.SPR_WAYPOINT_Y_2,  11,  0, 5, 16),
		TILE_SEQ_END(),
	};


	protected static final DrawTrackSeqStruct[] _track_depot_layout_table[] = {
		_track_depot_layout_table_0,
		_track_depot_layout_table_1,
		_track_depot_layout_table_2,
		_track_depot_layout_table_3,

		_track_waypoint_table_0,
		_track_waypoint_table_1,
	};
	




}
