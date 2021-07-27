package game;

import game.util.BitOps;

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


	// ------------------------------------------------------------





	/**
	 * Maps a Trackdir to the corresponding TrackdirBits value
	 */
	//static  TrackdirBits TrackdirToTrackdirBits(Trackdir trackdir) { return TrackdirBits.values[(1 << trackdir.getValue())]; }
	static int TrackdirToTrackdirBits(int trackdir) 
	{ 
		return 1 << trackdir; 
	}

	/**
	 * These functions check the validity of Tracks and Trackdirs. assert against
	 * them when convenient.
	 */
	//static  boolean IsValidTrack(Track track) { return track.getValue() < Track.TRACK_END.getValue(); }
	static  boolean IsValidTrack(int track) { return track < TRACK_END; }


	//static  boolean IsValidTrackdir(Trackdir trackdir) { return (TrackdirToTrackdirBits(trackdir).getValue() & TrackdirBits.TRACKDIR_BIT_MASK.getValue()) != 0; }
	static  boolean IsValidTrackdir( int trackdir ) 
	{ 
		return (TrackdirToTrackdirBits(trackdir) & TRACKDIR_BIT_MASK) != 0; 
	}

	/**
	 * Functions to map tracks to the corresponding bits in the signal
	 * presence/status bytes in the map. You should not use these directly, but
	 * wrapper functions below instead. XXX: Which are these?
	 */

	/**
	 * Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction along with the trackdir.
	 * / TODO who fills map arrays?
final byte _signal_along_trackdir[TRACKDIR_END];
static  byte SignalAlongTrackdir(Trackdir trackdir) {return _signal_along_trackdir[trackdir];}

/**
	 * Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction against the trackdir.
	 * /
static  byte SignalAgainstTrackdir(Trackdir trackdir) {
	final byte _signal_against_trackdir[TRACKDIR_END];
	return _signal_against_trackdir[trackdir];
}

/**
	 * Maps a Track to the bits that store the status of the two signals that can
	 * be present on the given track.
	 * /
static  byte SignalOnTrack(Track track) {
	final byte _signal_on_track[TRACK_END];
	return _signal_on_track[track];
}

/*
	 * Some functions to query rail tiles
	 */

	/**
	 * Returns the RailTileType of a given rail tile. (ie normal, with signals,
	 * depot, etc.)
	 */
	//static  RailTileType GetRailTileType(TileIndex tile)
	static int GetRailTileType(TileIndex tile)
	{
		assert(tile.IsTileType(TileTypes.MP_RAILWAY));
		return  Global._m[tile.getTile()].m5 & RAIL_TILE_TYPE_MASK;
	}

	/**
	 * Returns the rail type of the given rail tile (ie rail, mono, maglev).
	 */
	//static  RailType GetRailType(TileIndex tile) 
	static int GetRailType(TileIndex tile) 
	{ 
		return Global._m[tile.getTile()].m3 & RAILTYPE_MASK; 
	}

	/**
	 * Checks if a rail tile has signals.
	 */
	static boolean HasSignals(TileIndex tile)
	{
		return GetRailTileType(tile) == RailTileType.RAIL_TYPE_SIGNALS;
	}

	/**
	 * Returns the RailTileSubtype of a given rail tile with type
	 * RAIL_TYPE_DEPOT_WAYPOINT
	 */
	//static  RailTileSubtype GetRailTileSubtype(TileIndex tile)
	static int GetRailTileSubtype(TileIndex tile)
	{
		assert(GetRailTileType(tile) == RailTileType.RAIL_TYPE_DEPOT_WAYPOINT);
		return Global._m[tile.getTile()].m5 & RAIL_SUBTYPE_MASK;
	}

	/**
	 * Returns whether this is plain rails, with or without signals. Iow, if this
	 * tiles RailTileType is RAIL_TYPE_NORMAL or RAIL_TYPE_SIGNALS.
	 */
	static  boolean IsPlainRailTile(TileIndex tile)
	{
		RailTileType rtt = GetRailTileType(tile);
		return rtt == RailTileType.RAIL_TYPE_NORMAL || rtt == RailTileType.RAIL_TYPE_SIGNALS;
	}

	/**
	 * Returns the tracks present on the given plain rail tile (IsPlainRailTile())
	 */
	//static  TrackBits GetTrackBits(TileIndex tile)
	static int GetTrackBits(TileIndex tile)
	{
		assert(GetRailTileType(tile) == RailTileType.RAIL_TYPE_NORMAL || GetRailTileType(tile) == RAIL_TYPE_SIGNALS);
		return Global._m[tile.getTile()].m5 & TRACK_BIT_MASK;
	}

	/**
	 * Returns whether the given track is present on the given tile. Tile must be
	 * a plain rail tile (IsPlainRailTile()).
	 */
	//static  boolean HasTrack(TileIndex tile, Track track)
	static boolean HasTrack(TileIndex tile, int track)
	{
		assert(IsValidTrack(track));
		return BitOps.HASBIT(GetTrackBits(tile), track);
	}

	/*
	 * Functions describing logical relations between Tracks, TrackBits, Trackdirs
	 * TrackdirBits, Direction and DiagDirections.
	 *
	 * TODO: Add #unndefs or something similar to remove the arrays used below
	 * from the global scope and expose direct uses of them.
	 */

	/**
	 * Maps a trackdir to the reverse trackdir.
	 */
	//static  Trackdir ReverseTrackdir(Trackdir trackdir) 
	static int ReverseTrackdir(int trackdir) 
	{
		//final Trackdir _reverse_trackdir[TRACKDIR_END];
		return _reverse_trackdir[trackdir];
	}

	/**
	 * Maps a Track to the corresponding TrackBits value
	 */
	//static TrackBits TrackToTrackBits(Track track) 
	static int TrackToTrackBits(int track) 
	{ 
		return 1 << track; 
	}

	/**
	 * Returns the Track that a given Trackdir represents
	 */
	//static  Track TrackdirToTrack(Trackdir trackdir) { return new Track(trackdir.getValue() & 0x7); }
	static  /*Track*/ int TrackdirToTrack(int trackdir) { return trackdir & 0x7; }

	/**
	 * Returns a Trackdir for the given Track. Since every Track corresponds to
	 * two Trackdirs, we choose the one which points between NE and S.
	 * Note that the actual implementation is quite futile, but this might change
	 * in the future.
	 */
	//static  Trackdir TrackToTrackdir(Track track) { return Trackdir.getFor(track); }
	static int TrackToTrackdir(int track) { return track; }

	/**
	 * Returns a TrackdirBit mask that contains the two TrackdirBits that
	 * correspond with the given Track (one for each direction).
	 */
	//static  TrackdirBits TrackToTrackdirBits(Track track) 
	static int TrackToTrackdirBits(int track) 
	{ 
		/*Trackdir*/ int td = TrackToTrackdir(track); 
		return TrackdirToTrackdirBits(td) | TrackdirToTrackdirBits(ReverseTrackdir(td));
	}

	/**
	 * Discards all directional information from the given TrackdirBits. Any
	 * Track which is present in either direction will be present in the result.
	 */
	//static  TrackBits TrackdirBitsToTrackBits(TrackdirBits bits) { return bits | (bits >> 8); }
	static int TrackdirBitsToTrackBits(int bits) { return bits | (bits >> 8); }

	/**
	 * Maps a trackdir to the trackdir that you will end up on if you go straight
	 * ahead. This will be the same trackdir for diagonal trackdirs, but a
	 * different (alternating) one for straight trackdirs
	 */
	//static  Trackdir NextTrackdir(Trackdir trackdir) 
	static int NextTrackdir(int trackdir) 
	{
		//final Trackdir _next_trackdir[TRACKDIR_END];
		return _next_trackdir[trackdir];
	}

	/**
	 * Maps a track to all tracks that make 90 deg turns with it.
	 */
	//static  TrackBits TrackCrossesTracks(Track track) 
	static int TrackCrossesTracks(int track) 
	{
		//final TrackBits _track_crosses_tracks[TRACK_END];
		return _track_crosses_tracks[track];
	}

	/**
	 * Maps a trackdir to the (4-way) direction the tile is exited when following
	 * that trackdir.
	 * /
	static  DiagDirection TrackdirToExitdir(Trackdir trackdir) {
		final DiagDirection _trackdir_to_exitdir[TRACKDIR_END];
		return _trackdir_to_exitdir[trackdir];
	} */

	/**
	 * Maps a track and an (4-way) dir to the trackdir that represents the track
	 * with the exit in the given direction.
	 */
	//static  Trackdir TrackExitdirToTrackdir(Track track, DiagDirection diagdir) 
	static int TrackExitdirToTrackdir(int track, int diagdir) 
	{
		//final Trackdir _track_exitdir_to_trackdir[TRACK_END][DIAGDIR_END];
		return _track_exitdir_to_trackdir[track][diagdir];
	}

	/**
	 * Maps a track and an (4-way) dir to the trackdir that represents the track
	 * with the exit in the given direction.
	 */
	//static  Trackdir TrackEnterdirToTrackdir(Track track, DiagDirection diagdir) 
	static int TrackEnterdirToTrackdir(int track, int diagdir) 
	{
		//final Trackdir _track_enterdir_to_trackdir[TRACK_END][DIAGDIR_END];
		return _track_enterdir_to_trackdir[track][diagdir];
	}

	/**
	 * Maps a track and a full (8-way) direction to the trackdir that represents
	 * the track running in the given direction.
	 */
	//static  Trackdir TrackDirectionToTrackdir(Track track, Direction dir) 
	static int TrackDirectionToTrackdir(int track, int dir) 
	{
		//final Trackdir _track_direction_to_trackdir[TRACK_END][DIR_END];
		return _track_direction_to_trackdir[track][dir];
	}

	/**
	 * Maps a (4-way) direction to the diagonal trackdir that runs in that
	 * direction.
	 */
	//static  Trackdir DiagdirToDiagTrackdir(DiagDirection diagdir) 
	static int DiagdirToDiagTrackdir(int diagdir) 
	{
		//final Trackdir _dir_to_diag_trackdir[DIAGDIR_END];
		return _dir_to_diag_trackdir[diagdir];
	}

	final TrackdirBits _exitdir_reaches_trackdirs[DIAGDIR_END];

	/**
	 * Returns all trackdirs that can be reached when entering a tile from a given
	 * (diagonal) direction. This will obviously include 90 degree turns, since no
	 * information is available about the exact angle of entering */
	//static  TrackdirBits DiagdirReachesTrackdirs(DiagDirection diagdir) 
	static int DiagdirReachesTrackdirs(int diagdir) 
	{ 
		return _exitdir_reaches_trackdirs[diagdir]; 
	}

	/**
	 * Returns all tracks that can be reached when entering a tile from a given
	 * (diagonal) direction. This will obviously include 90 degree turns, since no
	 * information is available about the exact angle of entering */
	//static  TrackBits DiagdirReachesTracks(DiagDirection diagdir) { return TrackdirBitsToTrackBits(DiagdirReachesTrackdirs(diagdir)); }
	static int DiagdirReachesTracks(int diagdir) 
	{ return TrackdirBitsToTrackBits(DiagdirReachesTrackdirs(diagdir)); }

	/**
	 * Maps a trackdir to the trackdirs that can be reached from it (ie, when
	 * entering the next tile. This will include 90 degree turns!
	 */
	//static  TrackdirBits TrackdirReachesTrackdirs(Trackdir trackdir) { return _exitdir_reaches_trackdirs[TrackdirToExitdir(trackdir)]; }
	static int TrackdirReachesTrackdirs(int trackdir) { return _exitdir_reaches_trackdirs[TrackdirToExitdir(trackdir)]; }
	/* Note that there is no direct table for this function (there used to be),
	 * but it uses two simpler tables to achieve the result */


	/**
	 * Maps a trackdir to all trackdirs that make 90 deg turns with it.
	 */
	//static  TrackdirBits TrackdirCrossesTrackdirs(Trackdir trackdir) 
	static int TrackdirCrossesTrackdirs(int trackdir) 
	{
		//final TrackdirBits _track_crosses_trackdirs[TRACKDIR_END];
		return _track_crosses_trackdirs[TrackdirToTrack(trackdir)];
	}

	/**
	 * Maps a (4-way) direction to the reverse.
	 */
	//static  DiagDirection ReverseDiagdir(DiagDirection diagdir) 
	static int ReverseDiagdir(int diagdir) 
	{
		//final DiagDirection _reverse_diagdir[DIAGDIR_END];
		return _reverse_diagdir[diagdir];
	}

	/**
	 * Maps a (8-way) direction to a (4-way) DiagDirection
	 */
	//static  DiagDirection DirToDiagdir(Direction dir) 
	static int DirToDiagdir(int dir) 
	{
		assert(dir < DIR_END);
		return (dir >> 1);
	}

	/* Checks if a given Track is diagonal */
	//static  boolean IsDiagonalTrack(Track track) 
	static  boolean IsDiagonalTrack(int track) 
	{ 
		return (track == TRACK_DIAG1) || (track == TRACK_DIAG2); 
	}

	/* Checks if a given Trackdir is diagonal. */
	//static  boolean IsDiagonalTrackdir(Trackdir trackdir) 
	static  boolean IsDiagonalTrackdir(int trackdir) 
	{ return IsDiagonalTrack(TrackdirToTrack(trackdir)); }

	/*
	 * Functions quering signals on tiles.
	 */

	/**
	 * Checks for the presence of signals (either way) on the given track on the
	 * given rail tile.
	 */
	//static  boolean HasSignalOnTrack(TileIndex tile, Track track)
	static boolean HasSignalOnTrack(TileIndex tile, int track)
	{
		assert(IsValidTrack(track));
		return (
				(GetRailTileType(tile) == RailTileType.RAIL_TYPE_SIGNALS.ordinal()) 
				&& ((Global._m[tile.getTile()].m3 & SignalOnTrack(track)) != 0));
	}

	/**
	 * Checks for the presence of signals along the given trackdir on the given
	 * rail tile.
	 *
	 * Along meaning if you are currently driving on the given trackdir, this is
	 * the signal that is facing us (for which we stop when it's red).
	 */
	static  boolean HasSignalOnTrackdir(TileIndex tile, Trackdir trackdir)
	{
		assert (IsValidTrackdir(trackdir));
		return (GetRailTileType(tile) == RailTileType.RAIL_TYPE_SIGNALS) && (Global._m[tile.getTile()].m3 & SignalAlongTrackdir(trackdir));
	}

	/**
	 * Gets the state of the signal along the given trackdir.
	 *
	 * Along meaning if you are currently driving on the given trackdir, this is
	 * the signal that is facing us (for which we stop when it's red).
	 */
	//static  SignalState GetSignalState(TileIndex tile, Trackdir trackdir)
	static int GetSignalState(TileIndex tile, Trackdir trackdir)
	{
		assert(IsValidTrackdir(trackdir));
		assert(HasSignalOnTrack(tile, TrackdirToTrack(trackdir)));
		return ((Global._m[tile.getTile()].m2 & SignalAlongTrackdir(trackdir))
				?
						SIGNAL_STATE_GREEN : SIGNAL_STATE_RED);
	}

	/**
	 * Gets the type of signal on a given track on a given rail tile with signals.
	 *
	 * Note that currently, the track argument is not used, since
	 * signal types cannot be mixed. This function is trying to be
	 * future-compatible, though.
	 */
	static  SignalType GetSignalType(TileIndex tile, Track track)
	{
		assert(IsValidTrack(track));
		assert(GetRailTileType(tile) == RailTileType.RAIL_TYPE_SIGNALS);
		return SignalType.values[(Global._m[tile.getTile()].m4 & SignalType.SIGTYPE_MASK.getValue())];
	}

	/**
	 * Checks if this tile contains semaphores (returns true) or normal signals
	 * (returns false) on the given track. Does not check if there are actually
	 * signals on the track, you should use HasSignalsOnTrack() for that.
	 *
	 * Note that currently, the track argument is not used, since
	 * semaphores/electric signals cannot be mixed. This function is trying to be
	 * future-compatible, though.
	 */
	//static  boolean HasSemaphores(TileIndex tile, Track track)
	static  boolean HasSemaphores(TileIndex tile, int track)
	{
		assert(IsValidTrack(track));
		return (Global._m[tile.getTile()].m4 & SIG_SEMAPHORE_MASK) != 0;
	}

	/**
	 * Return the rail type of tile, or INVALID_RAILTYPE if this is no rail tile.
	 * Note that there is no check if the given trackdir is actually present on
	 * the tile!
	 * The given trackdir is used when there are (could be) multiple rail types on
	 * one tile.
	 */
	///* RailType */ int GetTileRailType(TileIndex tile, Trackdir trackdir);

	/**
	 * Returns whether the given tile is a level crossing.
	 */
	static  boolean IsLevelCrossing(TileIndex tile)
	{
		return (Global._m[tile.getTile()].m5 & 0xF0) == 0x10;
	}

	/**
	 * Gets the transport type of the given track on the given crossing tile.
	 * @return  The transport type of the given track, either TRANSPORT_ROAD,
	 * TRANSPORT_RAIL.
	 */
	//static  /*TransportType*/ int GetCrossingTransportType(TileIndex tile, Track track)
	static  /*TransportType*/ int GetCrossingTransportType(TileIndex tile, int track)
	{
		/* XXX: Nicer way to write this? */
		switch(track)
		{
		/* When map5 bit 3 is set, the road runs in the y direction (DIAG2) */
		case TRACK_DIAG1:
			return (BitOps.HASBIT(Global._m[tile.getTile()].m5, 3) ? TransportType.TRANSPORT_RAIL : TransportType.TRANSPORT_ROAD);
		case TRACK_DIAG2:
			return (BitOps.HASBIT(tile.getMap().m5, 3) ? TransportType.TRANSPORT_ROAD : TransportType.TRANSPORT_RAIL);
		default:
			assert(0);
		}
		return INVALID_TRANSPORT;
	}

	/**
	 * Returns a pointer to the Railtype information for a given railtype
	 * @param railtype the rail type which the information is requested for
	 * @return The pointer to the RailtypeInfo
	 */
	static  final RailtypeInfo GetRailTypeInfo(/* RailType */ int railtype)
	{
		assert(railtype < RAILTYPE_END);
		return &_railtypes[railtype];
	}

	/**
	 * Checks if an engine of the given RailType can drive on a tile with a given
	 * RailType. This would normally just be an equality check, but for electric
	 * rails (which also support non-electric engines).
	 * @return Whether the engine can drive on this tile.
	 * @param  enginetype The RailType of the engine we are considering.
	 * @param  tiletype   The RailType of the tile we are considering.
	 */
	static  boolean IsCompatibleRail(/* RailType */ int enginetype, /* /* RailType */ int */ int tiletype)
	{
		return BitOps.HASBIT(GetRailTypeInfo(enginetype)->compatible_railtypes, tiletype);
	}

	/**
	 * Checks if the given tracks overlap, ie form a crossing. Basically this
	 * means when there is more than one track on the tile, exept when there are
	 * two parallel tracks.
	 * @param  bits The tracks present.
	 * @return Whether the tracks present overlap in any way.
	 */
	//static  boolean TracksOverlap(TrackBits bits)
	static boolean TracksOverlap(int bits)
	{
		/* With no, or only one track, there is no overlap */
		if (bits == 0 || BitOps.KILL_FIRST_BIT(bits) == 0)
			return false;
		/* We know that there are at least two tracks present. When there are more
		 * than 2 tracks, they will surely overlap. When there are two, they will
		 * always overlap unless they are lower & upper or right & left. */
		if ((
				bits == (TRACK_BIT_UPPER | TRACK_BIT_LOWER)) || 
				(bits == (TRACK_BIT_LEFT | TRACK_BIT_RIGHT)))
			return false;
		return true;
	}




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
} 















/** These types are used in the map5 byte for rail tiles. Use GetRailTileType() to
 * get these values */
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
 * RAIL_TYPE_DEPOT_WAYPOINT */
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
	/* Stored in m4[0..1] for MP_RAILWAY */
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

} 

