package game;

public class Rail {

	/* $Id: rail.h 3329 2005-12-21 13:53:44Z matthijs $ */

	/** @file rail.h */

	#ifndef RAIL_H
	#define RAIL_H



	/*
	 * Some enums for accesing the map bytes for rail tiles
	 */

	/** These types are used in the map5 byte for rail tiles. Use GetRailTileType() to
	 * get these values */
	typedef enum RailTileTypes {
		RAIL_TYPE_NORMAL         = 0x0,
		RAIL_TYPE_SIGNALS        = 0x40,
		RAIL_TYPE_UNUSED         = 0x80, /* XXX: Maybe this could become waypoints? */
		RAIL_TYPE_DEPOT_WAYPOINT = 0xC0, /* Is really depots and waypoints... */
		RAIL_TILE_TYPE_MASK      = 0xC0,
	} RailTileType;

	enum { /* DEPRECATED TODO: Rewrite all uses of this */
		RAIL_TYPE_SPECIAL = 0x80, /* This used to say "If this bit is set, then it's
															 * not a regular track.", but currently, you
															 * should rather view map5[6..7] as one type,
															 * containing a value from RailTileTypes above.
															 * This value is only maintained for backwards
															 * compatibility */

		/* There used to be RAIL_BIT_* enums here, they moved to (for now) npf.c as
		 * TRACK_BIT_* */
	};

	/** These subtypes are used in the map5 byte when the main rail type is
	 * RAIL_TYPE_DEPOT_WAYPOINT */
	typedef enum RailTileSubtypes {
		RAIL_SUBTYPE_DEPOT    = 0x00,
		RAIL_SUBTYPE_WAYPOINT = 0x04,
		RAIL_SUBTYPE_MASK     = 0x3C,
	} RailTileSubtype;

	typedef enum SignalTypes {
		/* Stored in m4[0..1] for TileTypes.MP_RAILWAY */
		SIGTYPE_NORMAL  = 0,        // normal signal
		SIGTYPE_ENTRY   = 1,        // presignal block entry
		SIGTYPE_EXIT    = 2,        // presignal block exit
		SIGTYPE_COMBO   = 3,        // presignal inter-block
		SIGTYPE_PBS     = 4,        // pbs signal
		SIGTYPE_END,
		SIGTYPE_MASK    = 7,
	} SignalType;

	typedef enum RailTypes {
		RAILTYPE_RAIL   = 0,
		RAILTYPE_MONO   = 1,
		RAILTYPE_MAGLEV = 2,
		RAILTYPE_END,
		RAILTYPE_MASK   = 0x3,
		INVALID_RAILTYPE = 0xFF,
	} RailType;

	enum {
		SIG_SEMAPHORE_MASK = 1 << 3,
	};

	/** These are used to specify a single track. Can be translated to a trackbit
	 * with TrackToTrackbit */
	typedef enum Tracks {
		TRACK_DIAG1 = 0,
		TRACK_DIAG2 = 1,
		TRACK_UPPER = 2,
		TRACK_LOWER = 3,
		TRACK_LEFT  = 4,
		TRACK_RIGHT = 5,
		TRACK_END,
		INVALID_TRACK = 0xFF,
	} Track;

	/** These are the bitfield variants of the above */
	typedef enum TrackBits {
		TRACK_BIT_DIAG1 = 1,  // 0
		TRACK_BIT_DIAG2 = 2,  // 1
		TRACK_BIT_UPPER = 4,  // 2
		TRACK_BIT_LOWER = 8,  // 3
		TRACK_BIT_LEFT  = 16, // 4
		TRACK_BIT_RIGHT = 32, // 5
		TRACK_BIT_MASK  = 0x3F,
	} TrackBits;

	/** These are a combination of tracks and directions. Values are 0-5 in one
	direction (corresponding to the Track enum) and 8-13 in the other direction. */
	typedef enum Trackdirs {
		TRACKDIR_DIAG1_NE = 0,
		TRACKDIR_DIAG2_SE = 1,
		TRACKDIR_UPPER_E  = 2,
		TRACKDIR_LOWER_E  = 3,
		TRACKDIR_LEFT_S   = 4,
		TRACKDIR_RIGHT_S  = 5,
		/* Note the two missing values here. This enables trackdir . track
		 * conversion by doing (trackdir & 7) */
		TRACKDIR_DIAG1_SW = 8,
		TRACKDIR_DIAG2_NW = 9,
		TRACKDIR_UPPER_W  = 10,
		TRACKDIR_LOWER_W  = 11,
		TRACKDIR_LEFT_N   = 12,
		TRACKDIR_RIGHT_N  = 13,
		TRACKDIR_END,
		INVALID_TRACKDIR  = 0xFF,
	} Trackdir;

	/** These are a combination of tracks and directions. Values are 0-5 in one
	direction (corresponding to the Track enum) and 8-13 in the other direction. * /
	typedef enum TrackdirBits {
		TRACKDIR_BIT_DIAG1_NE = 0x1,
		TRACKDIR_BIT_DIAG2_SE = 0x2,
		TRACKDIR_BIT_UPPER_E  = 0x4,
		TRACKDIR_BIT_LOWER_E  = 0x8,
		TRACKDIR_BIT_LEFT_S   = 0x10,
		TRACKDIR_BIT_RIGHT_S  = 0x20,
		/* Again, note the two missing values here. This enables trackdir . track conversion by doing (trackdir & 0xFF) * /
		TRACKDIR_BIT_DIAG1_SW = 0x0100,
		TRACKDIR_BIT_DIAG2_NW = 0x0200,
		TRACKDIR_BIT_UPPER_W  = 0x0400,
		TRACKDIR_BIT_LOWER_W  = 0x0800,
		TRACKDIR_BIT_LEFT_N   = 0x1000,
		TRACKDIR_BIT_RIGHT_N  = 0x2000,
		TRACKDIR_BIT_MASK			= 0x3F3F,
		INVALID_TRACKDIR_BIT  = 0xFFFF,
	} TrackdirBits; */

	/** These are states in which a signal can be. Currently these are only two, so
	 * simple booleanean logic will do. But do try to compare to this enum instead of
	 * normal booleanean evaluation, since that will make future additions easier.
	 */
	typedef enum SignalStates {
		SIGNAL_STATE_RED = 0,
		SIGNAL_STATE_GREEN = 1,
	} SignalState;

	/** This struct contains all the info that is needed to draw and finalruct tracks.
	 * /
	class RailtypeInfo {
		/** Struct containing the main sprites. @note not all sprites are listed, but only
		 *  the ones used directly in the code * /
		struct {
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
		} base_sprites;

		/** struct containing the sprites for the rail GUI. @note only sprites referred to
		 * directly in the code are listed * /
		struct {
			SpriteID build_ns_rail;      ///< button for building single rail in N-S direction
			SpriteID build_x_rail;       ///< button for building single rail in X direction
			SpriteID build_ew_rail;      ///< button for building single rail in E-W direction
			SpriteID build_y_rail;       ///< button for building single rail in Y direction
			SpriteID auto_rail;          ///< button for the autorail finalruction
			SpriteID build_depot;        ///< button for building depots
			SpriteID build_tunnel;       ///< button for building a tunnel
			SpriteID convert_rail;       ///< button for converting rail
		} gui_sprites;

		struct {
			CursorID rail_ns;
			CursorID rail_swne;
			CursorID rail_ew;
			CursorID rail_nwse;
			CursorID autorail;
			CursorID depot;
			CursorID tunnel;
			CursorID convert;
		} cursor;

		struct {
			StringID toolbar_caption;
		} strings;

		/** sprite number difference between a piece of track on a snowy ground and the corresponding one on normal ground * /
		SpriteID snow_offset;

		/** bitmask to the OTHER railtypes that can be used by an engine of THIS railtype * /
		byte compatible_railtypes;

		/**
		 * Offset between the current railtype and normal rail. This means that:<p>
		 * 1) All the sprites in a railset MUST be in the same order. This order
		 *    is determined by normal rail. Check sprites 1005 and following for this order<p>
		 * 2) The position where the railtype is loaded must always be the same, otherwise
		 *    the offset will fail.<p>
		 * @note: Something more flexible might be desirable in the future.
		 * /
		SpriteID total_offset;

		/**
		  * Bridge offset
		  * /
		SpriteID bridge_offset;
	} RailtypeInfo;

	extern final RailtypeInfo _railtypes[RAILTYPE_END];
	* /
	// these are the maximums used for updating signal blocks, and checking if a depot is in a pbs block
	enum {
		NUM_SSD_ENTRY = 256, // max amount of blocks
		NUM_SSD_STACK = 32 ,// max amount of blocks to check recursively
	};

	/**
	 * Maps a Trackdir to the corresponding TrackdirBits value
	 */
	static inline TrackdirBits TrackdirToTrackdirBits(Trackdir trackdir) { return (TrackdirBits)(1 << trackdir); }

	/**
	 * These functions check the validity of Tracks and Trackdirs. assert against
	 * them when convenient.
	 */
	static inline boolean IsValidTrack(Track track) { return track < TRACK_END; }
	static inline boolean IsValidTrackdir(Trackdir trackdir) { return (TrackdirToTrackdirBits(trackdir) & TRACKDIR_BIT_MASK) != 0; }

	/**
	 * Functions to map tracks to the corresponding bits in the signal
	 * presence/status bytes in the map. You should not use these directly, but
	 * wrapper functions below instead. XXX: Which are these?
	 */

	/**
	 * Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction along with the trackdir.
	 */
	extern final byte _signal_along_trackdir[TRACKDIR_END];
	static inline byte SignalAlongTrackdir(Trackdir trackdir) {return _signal_along_trackdir[trackdir];}

	/**
	 * Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction against the trackdir.
	 */
	static inline byte SignalAgainstTrackdir(Trackdir trackdir) {
		extern final byte _signal_against_trackdir[TRACKDIR_END];
		return _signal_against_trackdir[trackdir];
	}

	/**
	 * Maps a Track to the bits that store the status of the two signals that can
	 * be present on the given track.
	 */
	static inline byte SignalOnTrack(Track track) {
		extern final byte _signal_on_track[TRACK_END];
		return _signal_on_track[track];
	}

	/*
	 * Some functions to query rail tiles
	 */

	/**
	 * Returns the RailTileType of a given rail tile. (ie normal, with signals,
	 * depot, etc.)
	 */
	static inline RailTileType GetRailTileType(TileIndex tile)
	{
		assert(tile.IsTileType( TileTypes.MP_RAILWAY));
		return tile.getMap().m5 & RAIL_TILE_TYPE_MASK;
	}

	/**
	 * Returns the rail type of the given rail tile (ie rail, mono, maglev).
	 */
	static inline RailType GetRailType(TileIndex tile) { return (RailType)(tile.getMap().m3 & RAILTYPE_MASK); }

	/**
	 * Checks if a rail tile has signals.
	 */
	static inline boolean HasSignals(TileIndex tile)
	{
		return GetRailTileType(tile) == RAIL_TYPE_SIGNALS;
	}

	/**
	 * Returns the RailTileSubtype of a given rail tile with type
	 * RAIL_TYPE_DEPOT_WAYPOINT
	 */
	static inline RailTileSubtype GetRailTileSubtype(TileIndex tile)
	{
		assert(GetRailTileType(tile) == RAIL_TYPE_DEPOT_WAYPOINT);
		return (RailTileSubtype)(tile.getMap().m5 & RAIL_SUBTYPE_MASK);
	}

	/**
	 * Returns whether this is plain rails, with or without signals. Iow, if this
	 * tiles RailTileType is RAIL_TYPE_NORMAL or RAIL_TYPE_SIGNALS.
	 */
	static inline boolean IsPlainRailTile(TileIndex tile)
	{
		RailTileType rtt = GetRailTileType(tile);
		return rtt == RAIL_TYPE_NORMAL || rtt == RAIL_TYPE_SIGNALS;
	}

	/**
	 * Returns the tracks present on the given plain rail tile (IsPlainRailTile())
	 */
	static inline TrackBits GetTrackBits(TileIndex tile)
	{
		assert(GetRailTileType(tile) == RAIL_TYPE_NORMAL || GetRailTileType(tile) == RAIL_TYPE_SIGNALS);
		return (TrackBits)(tile.getMap().m5 & TRACK_BIT_MASK);
	}

	/**
	 * Returns whether the given track is present on the given tile. Tile must be
	 * a plain rail tile (IsPlainRailTile()).
	 */
	static inline boolean HasTrack(TileIndex tile, Track track)
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
	static inline Trackdir ReverseTrackdir(Trackdir trackdir) {
		extern final Trackdir _reverse_trackdir[TRACKDIR_END];
		return _reverse_trackdir[trackdir];
	}

	/**
	 * Maps a Track to the corresponding TrackBits value
	 */
	static inline TrackBits TrackToTrackBits(Track track) { return (TrackBits)(1 << track); }

	/**
	 * Returns the Track that a given Trackdir represents
	 */
	static inline Track TrackdirToTrack(Trackdir trackdir) { return (Track)(trackdir & 0x7); }

	/**
	 * Returns a Trackdir for the given Track. Since every Track corresponds to
	 * two Trackdirs, we choose the one which points between NE and S.
	 * Note that the actual implementation is quite futile, but this might change
	 * in the future.
	 */
	static inline Trackdir TrackToTrackdir(Track track) { return (Trackdir)track; }

	/**
	 * Returns a TrackdirBit mask that contains the two TrackdirBits that
	 * correspond with the given Track (one for each direction).
	 */
	static inline TrackdirBits TrackToTrackdirBits(Track track) { Trackdir td = TrackToTrackdir(track); return TrackdirToTrackdirBits(td) | TrackdirToTrackdirBits(ReverseTrackdir(td));}

	/**
	 * Discards all directional information from the given TrackdirBits. Any
	 * Track which is present in either direction will be present in the result.
	 */
	static inline TrackBits TrackdirBitsToTrackBits(TrackdirBits bits) { return bits | (bits >> 8); }

	/**
	 * Maps a trackdir to the trackdir that you will end up on if you go straight
	 * ahead. This will be the same trackdir for diagonal trackdirs, but a
	 * different (alternating) one for straight trackdirs
	 */
	static inline Trackdir NextTrackdir(Trackdir trackdir) {
		extern final Trackdir _next_trackdir[TRACKDIR_END];
		return _next_trackdir[trackdir];
	}

	/**
	 * Maps a track to all tracks that make 90 deg turns with it.
	 */
	static inline TrackBits TrackCrossesTracks(Track track) {
		extern final TrackBits _track_crosses_tracks[TRACK_END];
		return _track_crosses_tracks[track];
	}

	/**
	 * Maps a trackdir to the (4-way) direction the tile is exited when following
	 * that trackdir.
	 */
	static inline DiagDirection TrackdirToExitdir(Trackdir trackdir) {
		extern final DiagDirection _trackdir_to_exitdir[TRACKDIR_END];
		return _trackdir_to_exitdir[trackdir];
	}

	/**
	 * Maps a track and an (4-way) dir to the trackdir that represents the track
	 * with the exit in the given direction.
	 */
	static inline Trackdir TrackExitdirToTrackdir(Track track, DiagDirection diagdir) {
		extern final Trackdir _track_exitdir_to_trackdir[TRACK_END][DIAGDIR_END];
		return _track_exitdir_to_trackdir[track][diagdir];
	}

	/**
	 * Maps a track and an (4-way) dir to the trackdir that represents the track
	 * with the exit in the given direction.
	 */
	static inline Trackdir TrackEnterdirToTrackdir(Track track, DiagDirection diagdir) {
		extern final Trackdir _track_enterdir_to_trackdir[TRACK_END][DIAGDIR_END];
		return _track_enterdir_to_trackdir[track][diagdir];
	}

	/**
	 * Maps a track and a full (8-way) direction to the trackdir that represents
	 * the track running in the given direction.
	 */
	static inline Trackdir TrackDirectionToTrackdir(Track track, Direction dir) {
		extern final Trackdir _track_direction_to_trackdir[TRACK_END][DIR_END];
		return _track_direction_to_trackdir[track][dir];
	}

	/**
	 * Maps a (4-way) direction to the diagonal trackdir that runs in that
	 * direction.
	 */
	static inline Trackdir DiagdirToDiagTrackdir(DiagDirection diagdir) {
		extern final Trackdir _dir_to_diag_trackdir[DIAGDIR_END];
		return _dir_to_diag_trackdir[diagdir];
	}

	extern final TrackdirBits _exitdir_reaches_trackdirs[DIAGDIR_END];

	/**
	 * Returns all trackdirs that can be reached when entering a tile from a given
	 * (diagonal) direction. This will obviously include 90 degree turns, since no
	 * information is available about the exact angle of entering */
	static inline TrackdirBits DiagdirReachesTrackdirs(DiagDirection diagdir) { return _exitdir_reaches_trackdirs[diagdir]; }

	/**
	 * Returns all tracks that can be reached when entering a tile from a given
	 * (diagonal) direction. This will obviously include 90 degree turns, since no
	 * information is available about the exact angle of entering */
	static inline TrackBits DiagdirReachesTracks(DiagDirection diagdir) { return TrackdirBitsToTrackBits(DiagdirReachesTrackdirs(diagdir)); }

	/**
	 * Maps a trackdir to the trackdirs that can be reached from it (ie, when
	 * entering the next tile. This will include 90 degree turns!
	 */
	static inline TrackdirBits TrackdirReachesTrackdirs(Trackdir trackdir) { return _exitdir_reaches_trackdirs[TrackdirToExitdir(trackdir)]; }
	/* Note that there is no direct table for this function (there used to be),
	 * but it uses two simpeler tables to achieve the result */


	/**
	 * Maps a trackdir to all trackdirs that make 90 deg turns with it.
	 */
	static inline TrackdirBits TrackdirCrossesTrackdirs(Trackdir trackdir) {
		extern final TrackdirBits _track_crosses_trackdirs[TRACKDIR_END];
		return _track_crosses_trackdirs[TrackdirToTrack(trackdir)];
	}

	/**
	 * Maps a (4-way) direction to the reverse.
	 */
	static inline DiagDirection ReverseDiagdir(DiagDirection diagdir) {
		extern final DiagDirection _reverse_diagdir[DIAGDIR_END];
		return _reverse_diagdir[diagdir];
	}

	/**
	 * Maps a (8-way) direction to a (4-way) DiagDirection
	 */
	static inline DiagDirection DirToDiagdir(Direction dir) {
		assert(dir < DIR_END);
		return (DiagDirection)(dir >> 1);
	}

	/* Checks if a given Track is diagonal */
	static inline boolean IsDiagonalTrack(Track track) { return (track == TRACK_DIAG1) || (track == TRACK_DIAG2); }

	/* Checks if a given Trackdir is diagonal. */
	static inline boolean IsDiagonalTrackdir(Trackdir trackdir) { return IsDiagonalTrack(TrackdirToTrack(trackdir)); }

	/*
	 * Functions quering signals on tiles.
	 */

	/**
	 * Checks for the presence of signals (either way) on the given track on the
	 * given rail tile.
	 */
	static inline boolean HasSignalOnTrack(TileIndex tile, Track track)
	{
		assert(IsValidTrack(track));
		return ((GetRailTileType(tile) == RAIL_TYPE_SIGNALS) && ((tile.getMap().m3 & SignalOnTrack(track)) != 0));
	}

	/**
	 * Checks for the presence of signals along the given trackdir on the given
	 * rail tile.
	 *
	 * Along meaning if you are currently driving on the given trackdir, this is
	 * the signal that is facing us (for which we stop when it's red).
	 */
	static inline boolean HasSignalOnTrackdir(TileIndex tile, Trackdir trackdir)
	{
		assert (IsValidTrackdir(trackdir));
		return (GetRailTileType(tile) == RAIL_TYPE_SIGNALS) && (tile.getMap().m3 & SignalAlongTrackdir(trackdir));
	}

	/**
	 * Gets the state of the signal along the given trackdir.
	 *
	 * Along meaning if you are currently driving on the given trackdir, this is
	 * the signal that is facing us (for which we stop when it's red).
	 */
	static inline SignalState GetSignalState(TileIndex tile, Trackdir trackdir)
	{
		assert(IsValidTrackdir(trackdir));
		assert(HasSignalOnTrack(tile, TrackdirToTrack(trackdir)));
		return ((tile.getMap().m2 & SignalAlongTrackdir(trackdir))?SIGNAL_STATE_GREEN:SIGNAL_STATE_RED);
	}

	/**
	 * Gets the type of signal on a given track on a given rail tile with signals.
	 *
	 * Note that currently, the track argument is not used, since
	 * signal types cannot be mixed. This function is trying to be
	 * future-compatible, though.
	 */
	static inline SignalType GetSignalType(TileIndex tile, Track track)
	{
		assert(IsValidTrack(track));
		assert(GetRailTileType(tile) == RAIL_TYPE_SIGNALS);
		return (SignalType)(tile.getMap().m4 & SIGTYPE_MASK);
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
	static inline boolean HasSemaphores(TileIndex tile, Track track)
	{
		assert(IsValidTrack(track));
		return (tile.getMap().m4 & SIG_SEMAPHORE_MASK);
	}

	/**
	 * Return the rail type of tile, or INVALID_RAILTYPE if this is no rail tile.
	 * Note that there is no check if the given trackdir is actually present on
	 * the tile!
	 * The given trackdir is used when there are (could be) multiple rail types on
	 * one tile.
	 */
	RailType GetTileRailType(TileIndex tile, Trackdir trackdir);

	/**
	 * Returns whether the given tile is a level crossing.
	 * /
	static inline boolean IsLevelCrossing(TileIndex tile)
	{
		return (tile.getMap().m5 & 0xF0) == 0x10;
	}

	/**
	 * Gets the transport type of the given track on the given crossing tile.
	 * @return  The transport type of the given track, either TRANSPORT_ROAD,
	 * TRANSPORT_RAIL.
	 */
	static inline TransportType GetCrossingTransportType(TileIndex tile, Track track)
	{
		/* XXX: Nicer way to write this? */
		switch(track)
		{
			/* When map5 bit 3 is set, the road runs in the y direction (DIAG2) */
			case TRACK_DIAG1:
				return (BitOps.HASBIT(tile.getMap().m5, 3) ? TRANSPORT_RAIL : TRANSPORT_ROAD);
			case TRACK_DIAG2:
				return (BitOps.HASBIT(tile.getMap().m5, 3) ? TRANSPORT_ROAD : TRANSPORT_RAIL);
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
	static inline final RailtypeInfo *GetRailTypeInfo(RailType railtype)
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
	static inline boolean IsCompatibleRail(RailType enginetype, RailType tiletype)
	{
		return BitOps.HASBIT(GetRailTypeInfo(enginetype).compatible_railtypes, tiletype);
	}

	/**
	 * Checks if the given tracks overlap, ie form a crossing. Basically this
	 * means when there is more than one track on the tile, exept when there are
	 * two parallel tracks.
	 * @param  bits The tracks present.
	 * @return Whether the tracks present overlap in any way.
	 */
	static inline boolean TracksOverlap(TrackBits bits)
	{
	  /* With no, or only one track, there is no overlap */
	  if (bits == 0 || KILL_FIRST_BIT(bits) == 0)
	    return false;
	  /* We know that there are at least two tracks present. When there are more
	   * than 2 tracks, they will surely overlap. When there are two, they will
	   * always overlap unless they are lower & upper or right & left. */
	  if ((bits == (TRACK_BIT_UPPER|TRACK_BIT_LOWER)) || (bits == (TRACK_BIT_LEFT | TRACK_BIT_RIGHT)))
	    return false;
	  return true;
	}

	void DrawTrackBits(TileInfo ti, TrackBits track, boolean earth, boolean snow, boolean flat);
	void DrawTrainDepotSprite(int x, int y, int image, RailType railtype);
	void DrawDefaultWaypointSprite(int x, int y, RailType railtype);
	#endif /* RAIL_H */
	
	
	/* $Id: rail.c 3077 2005-10-22 06:39:32Z tron $ */






	/* XXX: Below 3 tables store duplicate data. Maybe remove some? */
	/* Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction along with the trackdir */
	final byte _signal_along_trackdir[] = {
		0x80, 0x80, 0x80, 0x20, 0x40, 0x10, 0, 0,
		0x40, 0x40, 0x40, 0x10, 0x80, 0x20
	};

	/* Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction against the trackdir */
	final byte _signal_against_trackdir[] = {
		0x40, 0x40, 0x40, 0x10, 0x80, 0x20, 0, 0,
		0x80, 0x80, 0x80, 0x20, 0x40, 0x10
	};

	/* Maps a Track to the bits that store the status of the two signals that can
	 * be present on the given track */
	final byte _signal_on_track[] = {
		0xC0, 0xC0, 0xC0, 0x30, 0xC0, 0x30
	};

	/* Maps a diagonal direction to the all trackdirs that are connected to any
	 * track entering in this direction (including those making 90 degree turns)
	 */
	final TrackdirBits _exitdir_reaches_trackdirs[] = {
		TRACKDIR_BIT_DIAG1_NE | TRACKDIR_BIT_LOWER_E | TRACKDIR_BIT_LEFT_N,  /* DIAGDIR_NE */
		TRACKDIR_BIT_DIAG2_SE | TRACKDIR_BIT_LEFT_S  | TRACKDIR_BIT_UPPER_E, /* DIAGDIR_SE */
		TRACKDIR_BIT_DIAG1_SW | TRACKDIR_BIT_UPPER_W | TRACKDIR_BIT_RIGHT_S, /* DIAGDIR_SW */
		TRACKDIR_BIT_DIAG2_NW | TRACKDIR_BIT_RIGHT_N | TRACKDIR_BIT_LOWER_W  /* DIAGDIR_NW */
	};

	final Trackdir _next_trackdir[] = {
		TRACKDIR_DIAG1_NE,  TRACKDIR_DIAG2_SE,  TRACKDIR_LOWER_E, TRACKDIR_UPPER_E, TRACKDIR_RIGHT_S, TRACKDIR_LEFT_S, INVALID_TRACKDIR, INVALID_TRACKDIR,
		TRACKDIR_DIAG1_SW,  TRACKDIR_DIAG2_NW,  TRACKDIR_LOWER_W, TRACKDIR_UPPER_W, TRACKDIR_RIGHT_N, TRACKDIR_LEFT_N
	};

	/* Maps a trackdir to all trackdirs that make 90 deg turns with it. */
	final TrackdirBits _track_crosses_trackdirs[] = {
		TRACKDIR_BIT_DIAG2_SE | TRACKDIR_BIT_DIAG2_NW,                                               /* TRACK_DIAG1 */
		TRACKDIR_BIT_DIAG1_NE | TRACKDIR_BIT_DIAG1_SW,                                               /* TRACK_DIAG2 */
		TRACKDIR_BIT_RIGHT_N  | TRACKDIR_BIT_RIGHT_S  | TRACKDIR_BIT_LEFT_N  | TRACKDIR_BIT_LEFT_S,  /* TRACK_UPPER */
		TRACKDIR_BIT_RIGHT_N  | TRACKDIR_BIT_RIGHT_S  | TRACKDIR_BIT_LEFT_N  | TRACKDIR_BIT_LEFT_S,  /* TRACK_LOWER */
		TRACKDIR_BIT_UPPER_W  | TRACKDIR_BIT_UPPER_E  | TRACKDIR_BIT_LOWER_W | TRACKDIR_BIT_LOWER_E, /* TRACK_LEFT  */
		TRACKDIR_BIT_UPPER_W  | TRACKDIR_BIT_UPPER_E  | TRACKDIR_BIT_LOWER_W | TRACKDIR_BIT_LOWER_E  /* TRACK_RIGHT */
	};

	/* Maps a track to all tracks that make 90 deg turns with it. */
	final TrackBits _track_crosses_tracks[] = {
		TRACK_BIT_DIAG2,                   /* TRACK_DIAG1 */
		TRACK_BIT_DIAG1,                   /* TRACK_DIAG2 */
		TRACK_BIT_LEFT  | TRACK_BIT_RIGHT, /* TRACK_UPPER */
		TRACK_BIT_LEFT  | TRACK_BIT_RIGHT, /* TRACK_LOWER */
		TRACK_BIT_UPPER | TRACK_BIT_LOWER, /* TRACK_LEFT  */
		TRACK_BIT_UPPER | TRACK_BIT_LOWER  /* TRACK_RIGHT */
	};

	/* Maps a trackdir to the (4-way) direction the tile is exited when following
	 * that trackdir * /
	final DiagDirection _trackdir_to_exitdir[] = {
		DIAGDIR_NE,DIAGDIR_SE,DIAGDIR_NE,DIAGDIR_SE,DIAGDIR_SW,DIAGDIR_SE, DIAGDIR_NE,DIAGDIR_NE,
		DIAGDIR_SW,DIAGDIR_NW,DIAGDIR_NW,DIAGDIR_SW,DIAGDIR_NW,DIAGDIR_NE,
	}; */

	final Trackdir _track_exitdir_to_trackdir[][DIAGDIR_END] = {
		{TRACKDIR_DIAG1_NE, INVALID_TRACKDIR,  TRACKDIR_DIAG1_SW, INVALID_TRACKDIR},
		{INVALID_TRACKDIR,  TRACKDIR_DIAG2_SE, INVALID_TRACKDIR,  TRACKDIR_DIAG2_NW},
		{TRACKDIR_UPPER_E,  INVALID_TRACKDIR,  INVALID_TRACKDIR,  TRACKDIR_UPPER_W},
		{INVALID_TRACKDIR,  TRACKDIR_LOWER_E,  TRACKDIR_LOWER_W,  INVALID_TRACKDIR},
		{INVALID_TRACKDIR,  INVALID_TRACKDIR,  TRACKDIR_LEFT_S,   TRACKDIR_LEFT_N},
		{TRACKDIR_RIGHT_N,  TRACKDIR_RIGHT_S,  INVALID_TRACKDIR,  INVALID_TRACKDIR}
	};

	final Trackdir _track_enterdir_to_trackdir[][DIAGDIR_END] = { // TODO: replace magic with enums
		{TRACKDIR_DIAG1_NE, INVALID_TRACKDIR,  TRACKDIR_DIAG1_SW, INVALID_TRACKDIR},
		{INVALID_TRACKDIR,  TRACKDIR_DIAG2_SE, INVALID_TRACKDIR,  TRACKDIR_DIAG2_NW},
		{INVALID_TRACKDIR,  TRACKDIR_UPPER_E,  TRACKDIR_UPPER_W,  INVALID_TRACKDIR},
		{TRACKDIR_LOWER_E,  INVALID_TRACKDIR,  INVALID_TRACKDIR,  TRACKDIR_LOWER_W},
		{TRACKDIR_LEFT_N,   TRACKDIR_LEFT_S,   INVALID_TRACKDIR,  INVALID_TRACKDIR},
		{INVALID_TRACKDIR,  INVALID_TRACKDIR,  TRACKDIR_RIGHT_S,  TRACKDIR_RIGHT_N}
	};

	final Trackdir _track_direction_to_trackdir[][DIR_END] = {
		{INVALID_TRACKDIR, TRACKDIR_DIAG1_NE, INVALID_TRACKDIR, INVALID_TRACKDIR,  INVALID_TRACKDIR, TRACKDIR_DIAG1_SW, INVALID_TRACKDIR, INVALID_TRACKDIR},
		{INVALID_TRACKDIR, INVALID_TRACKDIR,  INVALID_TRACKDIR, TRACKDIR_DIAG2_SE, INVALID_TRACKDIR, INVALID_TRACKDIR,  INVALID_TRACKDIR, TRACKDIR_DIAG2_NW},
		{INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_UPPER_E, INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_UPPER_W, INVALID_TRACKDIR},
		{INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_LOWER_E, INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_LOWER_W, INVALID_TRACKDIR},
		{TRACKDIR_LEFT_N,  INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_LEFT_S,  INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR},
		{TRACKDIR_RIGHT_N, INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR,  TRACKDIR_RIGHT_S, INVALID_TRACKDIR,  INVALID_TRACKDIR, INVALID_TRACKDIR}
	};

	final Trackdir _dir_to_diag_trackdir[] = {
		TRACKDIR_DIAG1_NE, TRACKDIR_DIAG2_SE, TRACKDIR_DIAG1_SW, TRACKDIR_DIAG2_NW,
	};

	final DiagDirection _reverse_diagdir[] = {
		DIAGDIR_SW, DIAGDIR_NW, DIAGDIR_NE, DIAGDIR_SE
	};

	final Trackdir _reverse_trackdir[] = {
		TRACKDIR_DIAG1_SW, TRACKDIR_DIAG2_NW, TRACKDIR_UPPER_W, TRACKDIR_LOWER_W, TRACKDIR_LEFT_N, TRACKDIR_RIGHT_N, INVALID_TRACKDIR, INVALID_TRACKDIR,
		TRACKDIR_DIAG1_NE, TRACKDIR_DIAG2_SE, TRACKDIR_UPPER_E, TRACKDIR_LOWER_E, TRACKDIR_LEFT_S, TRACKDIR_RIGHT_S
	};

	RailType GetTileRailType(TileIndex tile, Trackdir trackdir)
	{
		RailType type = INVALID_RAILTYPE;
		DiagDirection exitdir = TrackdirToExitdir(trackdir);
		switch (GetTileType(tile)) {
			case TileTypes.MP_RAILWAY:
				/* railway track */
				type = tile.getMap().m3 & RAILTYPE_MASK;
				break;
			case TileTypes.MP_STREET:
				/* rail/road crossing */
				if (IsLevelCrossing(tile))
					type = tile.getMap().m4 & RAILTYPE_MASK;
				break;
			case TileTypes.MP_STATION:
				if (IsTrainStationTile(tile))
					type = tile.getMap().m3 & RAILTYPE_MASK;
				break;
			case TileTypes.MP_TUNNELBRIDGE:
				/* railway tunnel */
				if ((tile.getMap().m5 & 0xFC) == 0) type = tile.getMap().m3 & RAILTYPE_MASK;
				/* railway bridge ending */
				if ((tile.getMap().m5 & 0xC6) == 0x80) type = tile.getMap().m3 & RAILTYPE_MASK;
				/* on railway bridge */
				if ((tile.getMap().m5 & 0xC6) == 0xC0 && ((DiagDirection)(tile.getMap().m5 & 0x1)) == (exitdir & 0x1))
					type = (tile.getMap().m3 >> 4) & RAILTYPE_MASK;
				/* under bridge (any type) */
				if ((tile.getMap().m5 & 0xC0) == 0xC0 && (tile.getMap().m5 & 0x1U) != (exitdir & 0x1))
					type = tile.getMap().m3 & RAILTYPE_MASK;
				break;
			default:
				break;
		}
		return type;
	}

	
	
	
	
	
	
	
	
	/* $Id: rail_cmd.c 3298 2005-12-14 06:28:48Z tron $ */

























	extern int _custom_sprites_base;

	final byte _track_sloped_sprites[14] = {
		14, 15, 22, 13,
		 0, 21, 17, 12,
		23,  0, 18, 20,
		19, 16
	};

	void ShowTrainDepotWindow(TileIndex tile);

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
	enum RailMap2Lower4 {
		RAIL_MAP2LO_GROUND_MASK = 0xF,
		RAIL_GROUND_BROWN = 0,
		RAIL_GROUND_GREEN = 1,
		RAIL_GROUND_FENCE_NW = 2,
		RAIL_GROUND_FENCE_SE = 3,
		RAIL_GROUND_FENCE_SENW = 4,
		RAIL_GROUND_FENCE_NE = 5,
		RAIL_GROUND_FENCE_SW = 6,
		RAIL_GROUND_FENCE_NESW = 7,
		RAIL_GROUND_FENCE_VERT1 = 8,
		RAIL_GROUND_FENCE_VERT2 = 9,
		RAIL_GROUND_FENCE_HORIZ1 = 10,
		RAIL_GROUND_FENCE_HORIZ2 = 11,
		RAIL_GROUND_ICE_DESERT = 12,
	};


	/* MAP2 byte:    abcd???? => Signal On? Same coding as map3lo
	 * MAP3LO byte:  abcd???? => Signal Exists?
	 *				 a and b are for diagonals, upper and left,
	 *				 one for each direction. (ie a == NE.SW, b ==
	 *				 SW.NE, or v.v., I don't know. b and c are
	 *				 similar for lower and right.
	 * MAP2 byte:    ????abcd => Type of ground.
	 * MAP3LO byte:  ????abcd => Type of rail.
	 * MAP5:         00abcdef => rail
	 *               01abcdef => rail w/ signals
	 *               10uuuuuu => unused
	 *               11uuuudd => rail depot
	 */

	static boolean CheckTrackCombination(TileIndex tile, TrackBits to_build, int flags)
	{
		RailTileType type = GetRailTileType(tile);
		TrackBits current; /* The current track layout */
		TrackBits future; /* The track layout we want to build */
		Global._error_message = Str.STR_1001_IMPOSSIBLE_TRACK_COMBINATION;

		if (type != RAIL_TYPE_NORMAL && type != RAIL_TYPE_SIGNALS)
			return false; /* Cannot build anything on depots and checkpoints */

		/* So, we have a tile with tracks on it (and possibly signals). Let's see
		 * what tracks first */
		current = GetTrackBits(tile);
		future = current | to_build;

		/* Are we really building something new? */
		if (current == future) {
			/* Nothing new is being built */
			Global._error_message = Str.STR_1007_ALREADY_BUILT;
			return false;
		}

		/* Let's see if we may build this */
		if ((flags & Cmd.DC_NO_RAIL_OVERLAP) || type == RAIL_TYPE_SIGNALS) {
			/* If we are not allowed to overlap (flag is on for ai players or we have
			 * signals on the tile), check that */
			return
				future == (TRACK_BIT_UPPER | TRACK_BIT_LOWER) ||
				future == (TRACK_BIT_LEFT  | TRACK_BIT_RIGHT);
		} else {
			/* Normally, we may overlap and any combination is valid */
			return true;
		}
	}


	static final byte _valid_tileh_slopes[4][15] = {

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

	int GetRailFoundation(int tileh, int bits)
	{
		int i;

		if ((~_valid_tileh_slopes[0][tileh] & bits) == 0)
			return 0;

		if ((~_valid_tileh_slopes[1][tileh] & bits) == 0)
			return tileh;

		if ( ((i=0, tileh == 1) || (i+=2, tileh == 2) || (i+=2, tileh == 4) || (i+=2, tileh == 8)) && (bits == TRACK_BIT_DIAG1 || (i++, bits == TRACK_BIT_DIAG2)))
			return i + 15;

		return 0;
	}


	static int CheckRailSlope(int tileh, TrackBits rail_bits, TrackBits existing, TileIndex tile)
	{
		// never allow building on top of steep tiles
		if (!IsSteepTileh(tileh)) {
			rail_bits |= existing;

			// don't allow building on the lower side of a coast
			if (tile.IsTileType( TileTypes.MP_WATER) &&
					~_valid_tileh_slopes[2][tileh] & rail_bits) {
				return_cmd_error(Str.STR_3807_CAN_T_BUILD_ON_WATER);
			}

			// no special foundation
			if ((~_valid_tileh_slopes[0][tileh] & rail_bits) == 0)
				return 0;

			if ((~_valid_tileh_slopes[1][tileh] & rail_bits) == 0 || ( // whole tile is leveled up
						(rail_bits == TRACK_BIT_DIAG1 || rail_bits == TRACK_BIT_DIAG2) &&
						(tileh == 1 || tileh == 2 || tileh == 4 || tileh == 8)
					)) { // partly up
				if (existing != 0) {
					return 0;
				} else if (!Global._patches.build_on_slopes || _is_old_ai_player) {
					return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
				} else {
					return Global._price.terraform;
				}
			}
		}
		return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
	}

	/* Validate functions for rail building */
	static inline boolean ValParamTrackOrientation(Track track) {return IsValidTrack(track);}

	/** Build a single piece of rail
	 * @param x,y coordinates on where to build
	 * @param p1 railtype of being built piece (normal, mono, maglev)
	 * @param p2 rail track to build
	 */
	int CmdBuildSingleRail(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile;
		int tileh;
		int m5; /* XXX: Used only as a cache, should probably be removed? */
		Track track = (Track)p2;
		TrackBits trackbit;
		int cost = 0;
		int ret;

		if (!ValParamRailtype(p1) || !ValParamTrackOrientation(track)) return Cmd.CMD_ERROR;

		tile = TileVirtXY(x, y);
		tileh = GetTileSlope(tile, null);
		m5 = tile.getMap().m5;
		trackbit = TrackToTrackBits(track);

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		switch (GetTileType(tile)) {
			case TileTypes.MP_TUNNELBRIDGE:
				if ((m5 & 0xC0) != 0xC0 || // not bridge middle part?
						(m5 & 0x01 ? TRACK_BIT_DIAG1 : TRACK_BIT_DIAG2) != trackbit) { // wrong direction?
					// Get detailed error message
					return DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
				}

				switch (m5 & 0x38) { // what's under the bridge?
					case 0x00: // clear land
						ret = CheckRailSlope(tileh, trackbit, 0, tile);
						if (CmdFailed(ret)) return ret;
						cost += ret;

						if (flags & Cmd.DC_EXEC) {
							SetTileOwner(tile, Global._current_player);
							BitOps.RET SB(tile.getMap().m3, 0, 4, p1);
							tile.getMap().m5 = (m5 & 0xC7) | 0x20; // railroad under bridge
						}
						break;

					case 0x20: // rail already there
						return_cmd_error(Str.STR_1007_ALREADY_BUILT);

					default:
						// Get detailed error message
						return DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
				}
				break;

			case TileTypes.MP_RAILWAY:
				if (!CheckTrackCombination(tile, trackbit, flags) ||
						!EnsureNoVehicle(tile)) {
					return Cmd.CMD_ERROR;
				}
				if (m5 & RAIL_TYPE_SPECIAL ||
						!IsTileOwner(tile, Global._current_player) ||
						BitOps.GB(tile.getMap().m3, 0, 4) != p1) {
					// Get detailed error message
					return DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
				}

				ret = CheckRailSlope(tileh, trackbit, GetTrackBits(tile), tile);
				if (CmdFailed(ret)) return ret;
				cost += ret;

				if (flags & Cmd.DC_EXEC) {
					tile.getMap().m2 &= ~RAIL_MAP2LO_GROUND_MASK; // Bare land
					tile.getMap().m5 = m5 | trackbit;
				}
				break;

			case TileTypes.MP_STREET:
				if (!_valid_tileh_slopes[3][tileh]) // prevent certain slopes
					return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
				if (!EnsureNoVehicle(tile)) return Cmd.CMD_ERROR;

				if ((m5 & 0xF0) == 0 && ( // normal road?
							(track == TRACK_DIAG1 && m5 == 0x05) ||
							(track == TRACK_DIAG2 && m5 == 0x0A) // correct direction?
						)) {
					if (flags & Cmd.DC_EXEC) {
						tile.getMap().m3 = GetTileOwner(tile);
						SetTileOwner(tile, Global._current_player);
						tile.getMap().m4 = p1;
						tile.getMap().m5 = 0x10 | (track == TRACK_DIAG1 ? 0x08 : 0x00); // level crossing
					}
					break;
				}

				if (IsLevelCrossing(tile) && (m5 & 0x08 ? TRACK_DIAG1 : TRACK_DIAG2) == track)
					return_cmd_error(Str.STR_1007_ALREADY_BUILT);
				/* FALLTHROUGH */

			default:
				ret = CheckRailSlope(tileh, trackbit, 0, tile);
				if (CmdFailed(ret)) return ret;
				cost += ret;

				ret = DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
				if (CmdFailed(ret)) return ret;
				cost += ret;

				if (flags & Cmd.DC_EXEC) {
					SetTileType(tile, TileTypes.MP_RAILWAY);
					SetTileOwner(tile, Global._current_player);
					tile.getMap().m2 = 0; // Bare land
					tile.getMap().m3 = p1; // No signals, rail type
					tile.getMap().m5 = trackbit;
				}
				break;
		}

		if (flags & Cmd.DC_EXEC) {
			MarkTileDirtyByTile(tile);
			SetSignalsOnBothDir(tile, track);
		}

		return cost + Global._price.build_rail;
	}

	static final byte _signals_table[] = {
		0x40, 0x40, 0x40, 0x10, 0x80, 0x20, 0, 0, // direction 1
		0x80, 0x80, 0x80, 0x20, 0x40, 0x10, 0, 0  // direction 2
	};

	static final byte _signals_table_other[] = {
		0x80, 0x80, 0x80, 0x20, 0x40, 0x10, 0, 0, // direction 1
		0x40, 0x40, 0x40, 0x10, 0x80, 0x20, 0, 0  // direction 2
	};

	static final byte _signals_table_both[] = {
		0xC0, 0xC0, 0xC0, 0x30, 0xC0, 0x30, 0, 0,	// both directions combined
		0xC0, 0xC0, 0xC0, 0x30, 0xC0, 0x30, 0, 0
	};


	/** Remove a single piece of track
	 * @param x,y coordinates for removal of track
	 * @param p1 unused
	 * @param p2 rail orientation
	 */
	int CmdRemoveSingleRail(int x, int y, int flags, int p1, int p2)
	{
		Track track = (Track)p2;
		TrackBits trackbit;
		TileIndex tile;
		byte m5;
		int cost = Global._price.remove_rail;

		if (!ValParamTrackOrientation(p2)) return Cmd.CMD_ERROR;
		trackbit = TrackToTrackBits(track);

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		tile = TileVirtXY(x, y);

		if (!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && !tile.IsTileType( TileTypes.MP_STREET) && !tile.IsTileType( TileTypes.MP_RAILWAY))
			return Cmd.CMD_ERROR;

		if (Global._current_player != Owner.OWNER_WATER && !CheckTileOwnership(tile))
			return Cmd.CMD_ERROR;

		// allow building rail under bridge
		if (!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && !EnsureNoVehicle(tile))
			return Cmd.CMD_ERROR;

		switch(GetTileType(tile))
		{
			case TileTypes.MP_TUNNELBRIDGE:
				if (!EnsureNoVehicleZ(tile, TilePixelHeight(tile)))
					return Cmd.CMD_ERROR;

				if ((tile.getMap().m5 & 0xF8) != 0xE0)
					return Cmd.CMD_ERROR;

				if ((tile.getMap().m5 & 1 ? TRACK_BIT_DIAG1 : TRACK_BIT_DIAG2) != trackbit)
					return Cmd.CMD_ERROR;

				if (!(flags & Cmd.DC_EXEC))
					return Global._price.remove_rail;

				SetTileOwner(tile, Owner.OWNER_NONE);
				tile.getMap().m5 = tile.getMap().m5 & 0xC7;
				break;

			case TileTypes.MP_STREET:
				if (!IsLevelCrossing(tile)) return Cmd.CMD_ERROR;

				/* This is a crossing, let's check if the direction is correct */
				if (tile.getMap().m5 & 8) {
					m5 = 5;
					if (track != TRACK_DIAG1)
						return Cmd.CMD_ERROR;
				} else {
					m5 = 10;
					if (track != TRACK_DIAG2)
						return Cmd.CMD_ERROR;
				}

				if (!(flags & Cmd.DC_EXEC))
					return Global._price.remove_rail;

				tile.getMap().m5 = m5;
				SetTileOwner(tile, tile.getMap().m3);
				tile.getMap().m2 = 0;
				break;

			case TileTypes.MP_RAILWAY:
				if (!IsPlainRailTile(tile))
					return Cmd.CMD_ERROR;

				/* See if the track to remove is actually there */
				if (!(GetTrackBits(tile) & trackbit))
					return Cmd.CMD_ERROR;

				/* Charge extra to remove signals on the track, if they are there */
				if (HasSignalOnTrack(tile, track))
					cost += DoCommand(x, y, track, 0, flags, Cmd.CMD_REMOVE_SIGNALS);

				if (!(flags & Cmd.DC_EXEC))
					return cost;

				/* We remove the trackbit here. */
				tile.getMap().m5 &= ~trackbit;

				/* Unreserve track for PBS */
				if (PBSTileReserved(tile) & trackbit)
					PBSClearTrack(tile, track);

				if (GetTrackBits(tile)  == 0) {
					/* The tile has no tracks left, it is no longer a rail tile */
					DoClearSquare(tile);
					/* XXX: This is an optimisation, right? Is it really worth the ugly goto? */
					goto skip_mark_dirty;
				}
				break;

			default:
				assert(0);
		}

		/* mark_dirty */
		MarkTileDirtyByTile(tile);

	skip_mark_dirty:;

		SetSignalsOnBothDir(tile, track);

		return cost;
	}

	static final struct {
		int8 xinc[16];
		int8 yinc[16];
	} _railbit = {{
	//  0   1   2   3   4   5
		-16,  0,-16,  0, 16,  0,    0,  0,
		 16,  0,  0, 16,  0,-16,    0,  0,
	},{
		  0, 16,  0, 16,  0, 16,    0,  0,
		  0,-16,-16,  0,-16,  0,    0,  0,
	}};

	static int ValidateAutoDrag(Trackdir *trackdir, int x, int y, int ex, int ey)
	{
		int dx, dy, trdx, trdy;

		if (!ValParamTrackOrientation(*trackdir)) return Cmd.CMD_ERROR;

		// calculate delta x,y from start to end tile
		dx = ex - x;
		dy = ey - y;

		// calculate delta x,y for the first direction
		trdx = _railbit.xinc[*trackdir];
		trdy = _railbit.yinc[*trackdir];

		if (!IsDiagonalTrackdir(*trackdir)) {
			trdx += _railbit.xinc[*trackdir ^ 1];
			trdy += _railbit.yinc[*trackdir ^ 1];
		}

		// validate the direction
		while (((trdx <= 0) && (dx > 0)) || ((trdx >= 0) && (dx < 0)) ||
		       ((trdy <= 0) && (dy > 0)) || ((trdy >= 0) && (dy < 0))) {
			if (!BitOps.HASBIT(*trackdir, 3)) { // first direction is invalid, try the other
				SETBIT(*trackdir, 3); // reverse the direction
				trdx = -trdx;
				trdy = -trdy;
			} else // other direction is invalid too, invalid drag
				return Cmd.CMD_ERROR;
		}

		// (for diagonal tracks, this is already made sure of by above test), but:
		// for non-diagonal tracks, check if the start and end tile are on 1 line
		if (!IsDiagonalTrackdir(*trackdir)) {
			trdx = _railbit.xinc[*trackdir];
			trdy = _railbit.yinc[*trackdir];
			if ((abs(dx) != abs(dy)) && (abs(dx) + abs(trdy) != abs(dy) + abs(trdx)))
				return Cmd.CMD_ERROR;
		}

		return 0;
	}

	/** Build a stretch of railroad tracks.
	 * @param x,y start tile of drag
	 * @param p1 end tile of drag
	 * @param p2 various bitstuffed elements
	 * - p2 = (bit 0-3) - railroad type normal/maglev (0 = normal, 1 = mono, 2 = maglev)
	 * - p2 = (bit 4-6) - track-orientation, valid values: 0-5 (Track enum)
	 * - p2 = (bit 7)   - 0 = build, 1 = remove tracks
	 */
	static int CmdRailTrackHelper(int x, int y, int flags, int p1, int p2)
	{
		int ex, ey;
		int ret, total_cost = 0;
		Track track = (Track)BitOps.GB(p2, 4, 3);
		Trackdir trackdir;
		byte mode = BitOps.HASBIT(p2, 7);
		RailType railtype = (RailType)BitOps.GB(p2, 0, 4);

		if (!ValParamRailtype(railtype) || !ValParamTrackOrientation(track)) return Cmd.CMD_ERROR;
		if (p1 > MapSize()) return Cmd.CMD_ERROR;
		trackdir = TrackToTrackdir(track);

		/* unpack end point */
		ex = TileX(p1) * TILE_SIZE;
		ey = TileY(p1) * TILE_SIZE;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (CmdFailed(ValidateAutoDrag(&trackdir, x, y, ex, ey))) return Cmd.CMD_ERROR;

		if (flags & Cmd.DC_EXEC) SndPlayTileFx(SND_20_SPLAT_2, TileVirtXY(x, y));

		for(;;) {
			ret = DoCommand(x, y, railtype, TrackdirToTrack(trackdir), flags, (mode == 0) ? Cmd.CMD_BUILD_SINGLE_RAIL : Cmd.CMD_REMOVE_SINGLE_RAIL);

			if (CmdFailed(ret)) {
				if ((Global._error_message != Str.STR_1007_ALREADY_BUILT) && (mode == 0))
					break;
			} else
				total_cost += ret;

			if (x == ex && y == ey)
				break;

			x += _railbit.xinc[trackdir];
			y += _railbit.yinc[trackdir];

			// toggle railbit for the non-diagonal tracks
			if (!IsDiagonalTrackdir(trackdir)) trackdir ^= 1;
		}

		return (total_cost == 0) ? Cmd.CMD_ERROR : total_cost;
	}

	/** Build rail on a stretch of track.
	 * Stub for the unified rail builder/remover
	 * @see CmdRailTrackHelper
	 */
	int CmdBuildRailroadTrack(int x, int y, int flags, int p1, int p2)
	{
		return CmdRailTrackHelper(x, y, flags, p1, CLRBIT(p2, 7));
	}

	/** Build rail on a stretch of track.
	 * Stub for the unified rail builder/remover
	 * @see CmdRailTrackHelper
	 */
	int CmdRemoveRailroadTrack(int x, int y, int flags, int p1, int p2)
	{
		return CmdRailTrackHelper(x, y, flags, p1, SETBIT(p2, 7));
	}

	/** Build a train depot
	 * @param x,y position of the train depot
	 * @param p1 rail type
	 * @param p2 depot direction (0 through 3), where 0 is NE, 1 is SE, 2 is SW, 3 is NW
	 *
	 * @todo When checking for the tile slope,
	 * distingush between "Flat land required" and "land sloped in wrong direction"
	 */
	int CmdBuildTrainDepot(int x, int y, int flags, int p1, int p2)
	{
		Depot d;
		TileIndex tile = TileVirtXY(x, y);
		int cost, ret;
		int tileh;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (!EnsureNoVehicle(tile)) return Cmd.CMD_ERROR;
		/* check railtype and valid direction for depot (0 through 3), 4 in total */
		if (!ValParamRailtype(p1) || p2 > 3) return Cmd.CMD_ERROR;

		tileh = GetTileSlope(tile, null);

		/* Prohibit finalruction if
			The tile is non-flat AND
			1) The AI is "old-school"
			2) build-on-slopes is disabled
			3) the tile is steep i.e. spans two height levels
			4) the exit points in the wrong direction

		*/

		if (tileh != 0 && (
					_is_old_ai_player ||
					!Global._patches.build_on_slopes ||
					IsSteepTileh(tileh) ||
					!CanBuildDepotByTileh(p2, tileh)
				)) {
			return_cmd_error(Str.STR_0007_FLAT_LAND_REQUIRED);
		}

		ret = DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (CmdFailed(ret)) return Cmd.CMD_ERROR;
		cost = ret;

		d = AllocateDepot();
		if (d == null) return Cmd.CMD_ERROR;

		if (flags & Cmd.DC_EXEC) {
			if (IsLocalPlayer()) _last_built_train_depot_tile = tile;

			ModifyTile(tile,
				TileTypes.MP_SETTYPE(TileTypes.MP_RAILWAY) |
				TileTypes.MP_MAP3LO | TileTypes.MP_MAPOwner.OWNER_CURRENT | TileTypes.MP_MAP5,
				p1, /* map3_lo */
				p2 | RAIL_TYPE_DEPOT_WAYPOINT /* map5 */
			);

			d.xy = tile;
			d.town_index = ClosestTownFromTile(tile, (int)-1).index;

			SetSignalsOnBothDir(tile, (p2&1) ? 2 : 1);

		}

		return cost + Global._price.build_train_depot;
	}

	/** Build signals, alternate between double/single, signal/semaphore,
	+  * pre/exit/combo-signals, and what-else not
	+  * @param x,y coordinates where signals is being built
	+  * @param p1 various bitstuffed elements
	+  * - p1 = (bit 0-2) - track-orientation, valid values: 0-5 (Track enum)
	+  * - p1 = (bit 3)   - choose semaphores/signals or cycle normal/pre/exit/combo depending on context
	+  * - p1 (bit 0-2) - track-orientation, valid values: 0-5 (Track enum)
	+  * - p1 (bit 3)   - cycle normal/pre/exit/combo (only applies when signals already exist)
	+  * - p1 (bit 4)   - choose semaphores/light signals (only applies when no signals already exist)
	+  * - p1 (bit 5-7) - choose presignal type (only aplies when no signals already exist)
	+  * @param p2 used for CmdBuildManySignals() to copy direction of first signal
	+  * TODO: p2 should be replaced by two bits for "along" and "against" the track.
	+  */
	int CmdBuildSingleSignal(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileVirtXY(x, y);
		boolean semaphore;
		boolean pre_signal_cycle;
		byte pre_signal_type;
		Track track = (Track)(p1 & 0x7);
		int cost;

		// Same bit, used in different contexts
//		semaphore = pre_signal = BitOps.HASBIT(p1, 3);

		if (!ValParamTrackOrientation(track) || !tile.IsTileType( TileTypes.MP_RAILWAY) || !EnsureNoVehicle(tile))
			return Cmd.CMD_ERROR;

		/* Protect against invalid signal copying */
		if (p2 != 0 && (p2 & SignalOnTrack(track)) == 0) return Cmd.CMD_ERROR;

		/* You can only build signals on plain rail tiles, and the selected track must exist */
		if (!IsPlainRailTile(tile) || !HasTrack(tile, track)) return Cmd.CMD_ERROR;

		if (!CheckTileOwnership(tile)) return Cmd.CMD_ERROR;

		Global._error_message = Str.STR_1005_NO_SUITABLE_RAILROAD_TRACK;

		{
	 		/* See if this is a valid track combination for signals, (ie, no overlap) */
	 		TrackBits trackbits = GetTrackBits(tile);
			if (KILL_FIRST_BIT(trackbits) != 0 && /* More than one track present */
					trackbits != (TRACK_BIT_UPPER | TRACK_BIT_LOWER) && /* Horizontal parallel, non-intersecting tracks */
					trackbits != (TRACK_BIT_LEFT | TRACK_BIT_RIGHT) /* Vertical parallel, non-intersecting tracks */
			)
				return Cmd.CMD_ERROR;
		}

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		// for when signals already exist
		pre_signal_cycle = BitOps.HASBIT(p1, 3);
		// for placing new signals
		semaphore = BitOps.HASBIT(p1, 4);
	 	pre_signal_type = (p1 >> 5) & 7;
	 

		//if ((tile.getMap().m3 & _signals_table_both[track]) == 0) {
		if (!HasSignalOnTrack(tile, track)) {
			// build new signals
			cost = Global._price.build_signals;
		} else {
			if (p2 != 0 && semaphore != HasSemaphores(tile, track)) {
				// convert signals <. semaphores
				cost = Global._price.build_signals + Global._price.remove_signals;
			} else {
				// it is free to change orientation/pre-exit-combo signals
				cost = 0;
			}
		}

		if (flags & Cmd.DC_EXEC) {
			if (GetRailTileType(tile) != RAIL_TYPE_SIGNALS) {
				// there are no signals at all on this tile yet
	 			tile.getMap().m5 |= RAIL_TYPE_SIGNALS; // change into signals
	 			tile.getMap().m2 |= 0xF0;              // all signals are on
	 			tile.getMap().m3 &= ~0xF0;          // no signals built by default
				tile.getMap().m4 = (semaphore ? 0x08 : 0) + pre_signal_type;
			}

			if (p2 == 0) {
				if (!HasSignalOnTrack(tile, track)) {
					// build new signals
					tile.getMap().m3 |= SignalOnTrack(track);
				} else {
					if (pre_signal_cycle) {
						// cycle between normal . pre . exit . combo . pbs ....
						byte type = ((GetSignalType(tile, track) + 1) % 5);
						BitOps.RET SB(tile.getMap().m4, 0, 3, type);
					} else {
						// cycle between two-way . one-way . one-way . ...
						/* TODO: Rewrite switch into something more general */
						switch (track) {
							case TRACK_LOWER:
							case TRACK_RIGHT: {
								byte signal = (tile.getMap().m3 - 0x10) & 0x30;
								if (signal == 0) signal = 0x30;
								tile.getMap().m3 &= ~0x30;
								tile.getMap().m3 |= signal;
								break;
							}

							default: {
								byte signal = (tile.getMap().m3 - 0x40) & 0xC0;
								if (signal == 0) signal = 0xC0;
								tile.getMap().m3 &= ~0xC0;
								tile.getMap().m3 |= signal;
								break;
							}
						}
					}
				}
			} else {
				/* If CmdBuildManySignals is called with copying signals, just copy the
				 * direction of the first signal given as parameter by CmdBuildManySignals */
				tile.getMap().m3 &= ~SignalOnTrack(track);
				tile.getMap().m3 |= p2 & SignalOnTrack(track);
				// convert between signal<.semaphores when dragging
				if (semaphore) {
					SETBIT(tile.getMap().m4, 3);
				} else {
					CLRBIT(tile.getMap().m4, 3);
				}
			}

			MarkTileDirtyByTile(tile);
			SetSignalsOnBothDir(tile, track);
		}

		return cost;
	}

	static final byte _dir_from_track[14] = {
		0,1,0,1,2,1, 0,0,
		2,3,3,2,3,0,
	};

	/**  Build many signals automagically,
	 * Copy a signal along the entire length of connected rail, stopping only when a junction is reached
	 * @param x,y tile to start from
	 * @param railbit the railbit (direction in which to start placing signals)
	 * @param signals type of signals to copy
	 * @param p2 various bitstuffed elements
	 * - p2 (bit 0)     - 1 = remove signals, 0 = build signals
	 * - p2 (bit 3)     - 0 = signals, 1 = semaphores
	 * - p2 (bit 24-31) - user defined signals_density
	 */
	int BuildAutoSignals(int x, int y, Trackdir trackdir, int flags, int p2, byte signals)
	{
	  byte signal_density = (p2 >> 24);
		int signal_ctr = signal_density * 2;
		byte signal_dir = 0;	// direction in which signals are placed 1=forward  2=backward  3=twoway
		byte track_mode = 0;	// 128=bridge, 64=tunnel, 192=end of tunnel/bridge, 0=normal track
		byte track_height = 0; // height of tunnel currently in
		int retr, total_cost = 0;
		TileIndex tile = TileVirtXY/*TILE_FROM_XY*/(x, y);
		byte m5 = tile.getMap().m5;
		byte m3 = tile.getMap().m3;
		byte semaphores = (tile.getMap().m4 & ~3) ? 16 : 0;
		int mode = p2 & 0x1;
		int lx, ly;
		byte dir;


		// remember start position and direction
		int sx = x, sy = y;
		Trackdir srb = trackdir;

		// get first signal mode
		if (signals & _signals_table[trackdir]) signal_dir |= 1;
		if (signals & _signals_table_other[trackdir]) signal_dir |= 2;

		// check for semaphores
	/*
		if (!(m5 & RAIL_TYPE_SPECIAL) && (m5 & RAIL_BIT_MASK) && (m5 & RAIL_TYPE_SIGNALS))
			semaphores = (tile.getMap().m3 & ~3) ? 16 : 0; // copy signal/semaphores style (independent of GUI)
	*/
		if (signal_dir == 0)
			return Cmd.CMD_ERROR; // no signal on start tile to copy

		semaphores = (HasSemaphores(tile, TrackdirToTrack(trackdir)) ? 16 : 0); // copy signal/semaphores style (independent of CTRL)
		
		signals = 0;
		lx = 0;
		ly = 0;

		for(;;) {
			x += _railbit.xinc[trackdir];
			y += _railbit.yinc[trackdir];

			tile = TileVirtXY(x, y);

			m5 = tile.getMap().m5;

			m3 = tile.getMap().m3;

			dir = _dir_from_track[trackdir];

			if (track_mode & 128) { // currently on bridge
				if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && ((m5 & 192) == 128))
					// end of bridge
					track_mode = 192;
			} else if (track_mode & 64) { // currently in tunnel
				if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)
				&& ((m5 & 0xF0) == 0)
				&& ((m5 & 3) == (dir ^ 2))
				&& (GetSlopeZ(x+8, y+8) == track_height))
	 				// end of tunnel
						track_mode = 192;
			} else { // currently not on bridge/in tunnel
				if (tile.IsTileType(TileTypes.MP_TUNNELBRIDGE)
				&& (((m5 >> 1) & 3) == 0)
				&& ((m5 & 192) == 128)
				&& ((m5 & 1) == (dir & 1)) ) {
					// start of bridge
					track_mode = 128;
				} else if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)
				&& ((m5 & 0xF0) == 0)
				&& (((m5 >> 2) & 3) == 0) ) {
					// start of tunnel
					track_mode = 64;
					track_height = GetSlopeZ(x+8, y+8);
				};
			};

			/* for pieces that we cannot build signals on but are not an end of track or a junction, we continue counting. When a signal
				 should be placed in one of these tiles, it is instead placed on the last possible place for signals, and the counting is
				 reset from that place. If a signal already is there, one will be placed one the first possible tile encountered.
			   last place where a signal could be placed is remembered by lx,ly
				 if signal==0 a signal is already on lx,ly
			*/
			if ( (tile.IsTileType( TileTypes.MP_RAILWAY) && ((m5 & ~1) == RAIL_TYPE_WAYPOINT)	&& ((m5 & 1) == (dir & 1))) // check for waypoints
				|| (tile.IsTileType( TileTypes.MP_STREET) && ((m5 >> 4) == 1)										&& (!(m5 & 8) != !(dir & 1))) // check for road crossings
				|| (tile.IsTileType(TileTypes.MP_TUNNELBRIDGE) && ((m5 & 0xF8) == 0xE0)					&& ((m5 & 1) != (dir & 1))) // check overhanging bridges
				|| (track_mode != 0) // are we on a bridge/in a tunnel
			) {
				if (track_mode == 192) track_mode = 0; // end of tunnel/bridge
				signal_ctr -= 2; // these pieces are always diagonal, so count faster
				if (signal_ctr <= 0) {
					if (signals == 0) {
						// signal will be placed on next available tile
						signal_ctr = 1;
					} else {
						// signal will be placed on last possible tile, counting will reset from there
						signal_ctr += signal_density * 2;
						x = lx;
						y = ly;
						// Place Signal
						retr = DoCommand(x, y, TrackdirToTrack(trackdir) | semaphores, signals, flags, (mode == 1) ? Cmd.CMD_REMOVE_SIGNALS : Cmd.CMD_BUILD_SIGNALS);
						if (retr == Cmd.CMD_ERROR) return Cmd.CMD_ERROR;
						total_cost += retr;
						signals = 0;
						track_mode = 0;
					};
				};
				continue;
			};

			if (!tile.IsTileType( TileTypes.MP_RAILWAY))
				return total_cost;  // no more track, we are finished

			if ((m5 & RAIL_TYPE_SPECIAL) || !(m5 & 0x3F))
				return total_cost;  // no more track, we are finished

			// check for valid track combination, and calculate the railbit
			m5 &= 0x3F;
			switch (trackdir) {
				case 0: case 2: case 13: { // from SW
					if (m5 == TRACK_BIT_DIAG1) {	// SW to NE track
						trackdir = 0;
					} else if ((m5 & ~TRACK_BIT_UPPER) == TRACK_BIT_LOWER) { // SW to SE track
						trackdir = 3;
					} else if ((m5 & ~TRACK_BIT_RIGHT) == TRACK_BIT_LEFT) { // SW to NW track
						trackdir = 12;
					} else {
						return total_cost; // unsuitable track for signals, we are finished
					}
				} break;
				case 8: case 4: case 11: { // from NE
					if (m5 == TRACK_BIT_DIAG1) { // NE to SW track
						trackdir = 8;
					} else if ((m5 & ~TRACK_BIT_LOWER) == TRACK_BIT_UPPER) { // NE to NW track
						trackdir = 10;
					} else if ((m5 & ~TRACK_BIT_LEFT) == TRACK_BIT_RIGHT) { // NE to SE track
						trackdir = 5;
					} else {
						return total_cost; // unsuitable track for signals, we are finished
					}
				} break;
				case 9: case 10: case 12: { // from SE
					if (m5 == TRACK_BIT_DIAG2) { // SE to NW track
						trackdir = 9;
					} else if ((m5 & ~TRACK_BIT_UPPER) == TRACK_BIT_LOWER ) { // SE to SW track
						trackdir = 11;
					} else if ((m5 & ~TRACK_BIT_LEFT) == TRACK_BIT_RIGHT) { // SE to NE track
						trackdir = 13;
					} else {
						return total_cost; // unsuitable track for signals, we are finished
					}
				} break;
				case 1: case 3: case 5: { // from NW
					if (m5 == TRACK_BIT_DIAG2) { // NW to SE track
						trackdir = 1;
					} else if ((m5 & ~TRACK_BIT_LOWER) == TRACK_BIT_UPPER) { // NW to NE track
						trackdir = 2;
					} else if((m5 & ~TRACK_BIT_RIGHT) == TRACK_BIT_LEFT) { // NW to SW track
						trackdir = 4;
					} else {
						return total_cost; // unsuitable track for signals, we are finished
					}
				} break;
				default:
					assert(0);
			}

			// calculate signals to place
			signals = 0;
			if (signal_dir & 1) signals |= _signals_table[trackdir];
			if (signal_dir & 2) signals |= _signals_table_other[trackdir];

			if (x == sx && y == sy && trackdir == srb)
				return total_cost; // back at the start, we are finished

			// remember last place signals could be placed
			lx = x;			ly = y;

			m5 = tile.getMap().m5;
			if (mode)
				// when removing signals, remove all signals we encounter
				signal_ctr =( (((m5 & RAIL_TILE_TYPE_MASK) == RAIL_TYPE_SIGNALS)) && (m3 & _signals_table_both[trackdir]) ) ? 0 : 1;
			else if (m5 & 0x3)
				// count faster on diagonal tracks
				signal_ctr -= 2;
			else
				signal_ctr -= 1;

			if (signal_ctr <= 0) {
				signal_ctr += signal_density * 2;
				// Place Signal
				retr = DoCommand(lx, ly, (trackdir & 7) | semaphores, signals , flags, (mode == 1) ? Cmd.CMD_REMOVE_SIGNALS : Cmd.CMD_BUILD_SIGNALS);
				if (retr == Cmd.CMD_ERROR) return Cmd.CMD_ERROR;
				total_cost += retr;
				signals = 0;
			};

			// when removing signals, the last position is always handled
			if (mode) signals = 0;

		};


	};



	/**	Build many signals by dragging; AutoSignals
	 * @param x,y start tile of drag
	 * @param p1  end tile of drag
	 * @param p2 various bitstuffed elements
	 * - p2 = (bit 0)     - 0 = build, 1 = remove signals
	 * - p2 = (bit 1)     - 1 = autocompletion on, 0 = off
	 * - p2 = (bit 3)     - 0 = signals, 1 = semaphores
	 * - p2 = (bit 4- 6)  - track-orientation, valid values: 0-5
	 * - p2 = (bit 24-31) - user defined signals_density
	 */
	static int CmdSignalTrackHelper(int x, int y, int flags, int p1, int p2)
	{
		int ex, ey;
		int ret, total_cost, signal_ctr;
		byte signals;
		TileIndex tile = TileVirtXY(x, y);
		boolean error = true;

		int mode = p2 & 0x1;
		Track track = BitOps.GB(p2, 4, 3);
		Trackdir trackdir = TrackToTrackdir(track);
		byte semaphores = (BitOps.HASBIT(p2, 3)) ? 16 : 0;
		byte signal_density = (p2 >> 24);

		if (p1 > MapSize()) return Cmd.CMD_ERROR;
		if (signal_density == 0 || signal_density > 20) return Cmd.CMD_ERROR;

		if (!tile.IsTileType( TileTypes.MP_RAILWAY)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		/* for vertical/horizontal tracks, double the given signals density
		* since the original amount will be too dense (shorter tracks) */
		if (!IsDiagonalTrack(track))
			signal_density *= 2;

		// unpack end tile
		ex = TileX(p1) * TILE_SIZE;
		ey = TileY(p1) * TILE_SIZE;

		if (CmdFailed(ValidateAutoDrag(&trackdir, x, y, ex, ey))) return Cmd.CMD_ERROR;

		track = TrackdirToTrack(trackdir); /* trackdir might have changed, keep track in sync */

		// copy the signal-style of the first rail-piece if existing
		if (GetRailTileType(tile) == RAIL_TYPE_SIGNALS && GetTrackBits(tile) != 0) { /* XXX: GetTrackBits check useless? */
			signals = tile.getMap().m3 & SignalOnTrack(track);
			if (signals == 0) signals = SignalOnTrack(track); /* Can this actually occur? */

			semaphores = (HasSemaphores(tile, track) ? 16 : 0); // copy signal/semaphores style (independent of CTRL)
		} else // no signals exist, drag a two-way signal stretch
			signals = SignalOnTrack(track);

		/* signal_ctr         - amount of tiles already processed
		 * signals_density    - patch setting to put signal on every Nth tile (double space on |, -- tracks)
		 **********
		 * trackdir   - trackdir to build with autorail
		 * semaphores - semaphores or signals
		 * signals    - is there a signal/semaphore on the first tile, copy its style (two-way/single-way)
		                and convert all others to semaphore/signal
		 * mode       - 1 remove signals, 0 build signals */
		signal_ctr = total_cost = 0;
		for (;;) {
			// only build/remove signals with the specified density
			if ((signal_ctr % signal_density) == 0 ) {
				ret = DoCommand(x, y, TrackdirToTrack(trackdir) | semaphores, signals, flags, (mode == 1) ? Cmd.CMD_REMOVE_SIGNALS : Cmd.CMD_BUILD_SIGNALS);

				/* Abort placement for any other error than NOT_SUITABLE_TRACK
				 * This includes vehicles on track, competitor's tracks, etc. */
				if (CmdFailed(ret)) {
					if (Global._error_message != Str.STR_1005_NO_SUITABLE_RAILROAD_TRACK && mode != 1) return Cmd.CMD_ERROR;
				} else {
					error = false;
					total_cost += ret;

					/* when autocompletion is on, use that to place the rest of the signals */
					if BitOps.HASBIT(p2, 1) {
						ret = BuildAutoSignals(x, y, trackdir, flags, p2, signals);
						if (ret == Cmd.CMD_ERROR)
							return Cmd.CMD_ERROR;
						total_cost += ret;
						return total_cost;
					}
				}
			}

			if (ex == x && ey == y) break; // reached end of drag

			x += _railbit.xinc[trackdir];
			y += _railbit.yinc[trackdir];
			signal_ctr++;

			// toggle railbit for the non-diagonal tracks (|, -- tracks)
			if (!IsDiagonalTrackdir(trackdir)) trackdir ^= 1;
		}

		return (error) ? Cmd.CMD_ERROR : total_cost;
	}

	/** Build signals on a stretch of track.
	 * Stub for the unified signal builder/remover
	 * @see CmdSignalTrackHelper
	 */
	int CmdBuildSignalTrack(int x, int y, int flags, int p1, int p2)
	{
		return CmdSignalTrackHelper(x, y, flags, p1, p2);
	}

	/** Remove signals
	 * @param x,y coordinates where signal is being deleted from
	 * @param p1 track to remove signal from (Track enum)
	 */
	int CmdRemoveSingleSignal(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileVirtXY(x, y);
		Track track = (Track)(p1 & 0x7);

		if (!ValParamTrackOrientation(track) || !tile.IsTileType( TileTypes.MP_RAILWAY) || !EnsureNoVehicle(tile))
			return Cmd.CMD_ERROR;

		if (!HasSignalOnTrack(tile, track)) // no signals on track?
			return Cmd.CMD_ERROR;

		/* Only water can remove signals from anyone */
		if (Global._current_player != Owner.OWNER_WATER && !CheckTileOwnership(tile)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		/* Do it? */
		if (flags & Cmd.DC_EXEC) {
			tile.getMap().m3 &= ~SignalOnTrack(track);

			/* removed last signal from tile? */
			if (BitOps.GB(tile.getMap().m3, 4, 4) == 0) {
				BitOps.RET SB(tile.getMap().m2, 4, 4, 0);
				BitOps.RET SB(tile.getMap().m5, 6, 2, RAIL_TYPE_NORMAL >> 6); // XXX >> because the finalant is meant for direct application, not use with SB
				CLRBIT(tile.getMap().m4, 3); // remove any possible semaphores
			}

			SetSignalsOnBothDir(tile, track);

			MarkTileDirtyByTile(tile);
		}

		return Global._price.remove_signals;
	}

	/** Remove signals on a stretch of track.
	 * Stub for the unified signal builder/remover
	 * @see CmdSignalTrackHelper
	 */
	int CmdRemoveSignalTrack(int x, int y, int flags, int p1, int p2)
	{
		return CmdSignalTrackHelper(x, y, flags, p1, SETBIT(p2, 0));
	}

	typedef int DoConvertRailProc(TileIndex tile, int totype, boolean exec);

	static int DoConvertRail(TileIndex tile, int totype, boolean exec)
	{
		if (!CheckTileOwnership(tile) || !EnsureNoVehicle(tile)) return Cmd.CMD_ERROR;

		// tile is already of requested type?
		if (GetRailType(tile) == totype) return Cmd.CMD_ERROR;

		// change type.
		if (exec) {
			BitOps.RET SB(tile.getMap().m3, 0, 4, totype);
			MarkTileDirtyByTile(tile);
		}

		return Global._price.build_rail / 2;
	}

	extern int DoConvertStationRail(TileIndex tile, int totype, boolean exec);
	extern int DoConvertStreetRail(TileIndex tile, int totype, boolean exec);
	extern int DoConvertTunnelBridgeRail(TileIndex tile, int totype, boolean exec);

	/** Convert one rail type to the other. You can convert normal rail to
	 * monorail/maglev easily or vice-versa.
	 * @param ex,ey end tile of rail conversion drag
	 * @param p1 start tile of drag
	 * @param p2 new railtype to convert to
	 */
	int CmdConvertRail(int ex, int ey, int flags, int p1, int p2)
	{
		int ret, cost, money;
		int sx, sy, x, y;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (!ValParamRailtype(p2)) return Cmd.CMD_ERROR;
		if (p1 > MapSize()) return Cmd.CMD_ERROR;

		// make sure sx,sy are smaller than ex,ey
		sx = TileX(p1) * TILE_SIZE;
		sy = TileY(p1) * TILE_SIZE;
		if (ex < sx) intswap(ex, sx);
		if (ey < sy) intswap(ey, sy);

		money = GetAvailableMoneyForCommand();
		cost = 0;

		for (x = sx; x <= ex; x += TILE_SIZE) {
			for (y = sy; y <= ey; y += TILE_SIZE) {
				TileIndex tile = TileVirtXY(x, y);
				DoConvertRailProc* proc;

				switch (GetTileType(tile)) {
					case TileTypes.MP_RAILWAY:      proc = DoConvertRail;             break;
					case TileTypes.MP_STATION:      proc = DoConvertStationRail;      break;
					case TileTypes.MP_STREET:       proc = DoConvertStreetRail;       break;
					case TileTypes.MP_TUNNELBRIDGE: proc = DoConvertTunnelBridgeRail; break;
					default: continue;
				}

				ret = proc(tile, p2, false);
				if (CmdFailed(ret)) continue;
				cost += ret;

				if (flags & Cmd.DC_EXEC) {
					money -= ret;
					if (money < 0) {
						_additional_cash_required = ret;
						return cost - ret;
					}
					proc(tile, p2, true);
				}
			}
		}

		return (cost == 0) ? Cmd.CMD_ERROR : cost;
	}

	static int RemoveTrainDepot(TileIndex tile, int flags)
	{
		if (!CheckTileOwnership(tile) && Global._current_player != Owner.OWNER_WATER)
			return Cmd.CMD_ERROR;

		if (!EnsureNoVehicle(tile))
			return Cmd.CMD_ERROR;

		if (flags & Cmd.DC_EXEC) {
			Track track = TrackdirToTrack(DiagdirToDiagTrackdir(GetDepotDirection(tile, TRANSPORT_RAIL)));

			DoDeleteDepot(tile);
			SetSignalsOnBothDir(tile, track);
		}

		return Global._price.remove_train_depot;
	}

	static int ClearTile_Track(TileIndex tile, byte flags)
	{
		int cost;
		int ret;
		byte m5;

		m5 = tile.getMap().m5;

		if (flags & Cmd.DC_AUTO) {
			if (m5 & RAIL_TYPE_SPECIAL)
				return_cmd_error(Str.STR_2004_BUILDING_MUST_BE_DEMOLISHED);

			if (!IsTileOwner(tile, Global._current_player))
				return_cmd_error(Str.STR_1024_AREA_IS_OWNED_BY_ANOTHER);

			return_cmd_error(Str.STR_1008_MUST_REMOVE_RAILROAD_TRACK);
		}

		cost = 0;

		switch (GetRailTileType(tile)) {
			case RAIL_TYPE_SIGNALS:
				if (tile.getMap().m3 & _signals_table_both[0]) {
					ret = DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_REMOVE_SIGNALS);
					if (CmdFailed(ret)) return Cmd.CMD_ERROR;
					cost += ret;
				}
				if (tile.getMap().m3 & _signals_table_both[3]) {
					ret = DoCommandByTile(tile, 3, 0, flags, Cmd.CMD_REMOVE_SIGNALS);
					if (CmdFailed(ret)) return Cmd.CMD_ERROR;
					cost += ret;
				}

				m5 &= TRACK_BIT_MASK;
				if (!(flags & Cmd.DC_EXEC)) {
					for (; m5 != 0; m5 >>= 1) if (m5 & 1) cost += Global._price.remove_rail;
					return cost;
				}
				/* FALLTHROUGH */

			case RAIL_TYPE_NORMAL: {
				int i;

				for (i = 0; m5 != 0; i++, m5 >>= 1) {
					if (m5 & 1) {
						ret = DoCommandByTile(tile, 0, i, flags, Cmd.CMD_REMOVE_SINGLE_RAIL);
						if (CmdFailed(ret)) return Cmd.CMD_ERROR;
						cost += ret;
					}
				}
				return cost;
			}

			case RAIL_TYPE_DEPOT_WAYPOINT:
				switch (m5 & RAIL_SUBTYPE_MASK) {
					case RAIL_SUBTYPE_DEPOT:
						return RemoveTrainDepot(tile, flags);

					case RAIL_SUBTYPE_WAYPOINT:
						return RemoveTrainWaypoint(tile, flags, false);

					default:
						return Cmd.CMD_ERROR;
				}

			default:
				return Cmd.CMD_ERROR;
		}
	}





	// used for presignals
	static final SpriteID _signal_base_sprites[32] = {
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
	static final byte _signal_position[24] = {
		/* original: left side position */
		0x58,0x1E,0xE1,0xB9,0x01,0xA3,0x4B,0xEE,0x3B,0xD4,0x43,0xBD,
		/* patch: ride side position */
		0x1E,0xAC,0x64,0xE1,0x4A,0x10,0xEE,0xC5,0xDB,0x34,0x4D,0xB3
	};

	static void DrawSignalHelper(final TileInfo ti, byte condition, int image_and_pos)
	{
		boolean otherside = GameOptions._opt.road_side & Global._patches.signal_side;

		int v = _signal_position[(image_and_pos & 0xF) + (otherside ? 12 : 0)];
		int x = ti.x | (v&0xF);
		int y = ti.y | (v>>4);
		int sprite = _signal_base_sprites[(_m[ti.tile].m4 & 0xF) + (otherside ? 0x10 : 0)] + (image_and_pos>>4) + ((condition != 0) ? 1 : 0);
		AddSortableSpriteToDraw(sprite, x, y, 1, 1, 10, GetSlopeZ(x,y));
	}

	static int _drawtile_track_palette;


	static void DrawTrackFence_NW(final TileInfo ti)
	{
		int image = 0x515;
		if (ti.tileh != 0) image = (ti.tileh & 2) ? 0x519 : 0x51B;
		AddSortableSpriteToDraw(image | _drawtile_track_palette,
			ti.x, ti.y+1, 16, 1, 4, ti.z);
	}

	static void DrawTrackFence_SE(final TileInfo ti)
	{
		int image = 0x515;
		if (ti.tileh != 0) image = (ti.tileh & 2) ? 0x519 : 0x51B;
		AddSortableSpriteToDraw(image | _drawtile_track_palette,
			ti.x, ti.y+15, 16, 1, 4, ti.z);
	}

	static void DrawTrackFence_NW_SE(final TileInfo ti)
	{
		DrawTrackFence_NW(ti);
		DrawTrackFence_SE(ti);
	}

	static void DrawTrackFence_NE(final TileInfo ti)
	{
		int image = 0x516;
		if (ti.tileh != 0) image = (ti.tileh & 2) ? 0x51A : 0x51C;
		AddSortableSpriteToDraw(image | _drawtile_track_palette,
			ti.x+1, ti.y, 1, 16, 4, ti.z);
	}

	static void DrawTrackFence_SW(final TileInfo ti)
	{
		int image = 0x516;
		if (ti.tileh != 0) image = (ti.tileh & 2) ? 0x51A : 0x51C;
		AddSortableSpriteToDraw(image | _drawtile_track_palette,
			ti.x+15, ti.y, 1, 16, 4, ti.z);
	}

	static void DrawTrackFence_NE_SW(final TileInfo ti)
	{
		DrawTrackFence_NE(ti);
		DrawTrackFence_SW(ti);
	}

	static void DrawTrackFence_NS_1(final TileInfo ti)
	{
		int z = ti.z;
		if (ti.tileh & 1) z += 8;
		AddSortableSpriteToDraw(0x517 | _drawtile_track_palette,
			ti.x + 8, ti.y + 8, 1, 1, 4, z);
	}

	static void DrawTrackFence_NS_2(final TileInfo ti)
	{
		int z = ti.z;
		if (ti.tileh & 4) z += 8;
		AddSortableSpriteToDraw(0x517 | _drawtile_track_palette,
			ti.x + 8, ti.y + 8, 1, 1, 4, z);
	}

	static void DrawTrackFence_WindowEvents.WE_1(final TileInfo ti)
	{
		int z = ti.z;
		if (ti.tileh & 8) z += 8;
		AddSortableSpriteToDraw(0x518 | _drawtile_track_palette,
			ti.x + 8, ti.y + 8, 1, 1, 4, z);
	}

	static void DrawTrackFence_WindowEvents.WE_2(final TileInfo ti)
	{
		int z = ti.z;
		if (ti.tileh & 2) z += 8;
		AddSortableSpriteToDraw(0x518 | _drawtile_track_palette,
			ti.x + 8, ti.y + 8, 1, 1, 4, z);
	}

	static void DetTrackDrawProc_Null(final TileInfo ti)
	{
		/* nothing should be here */
	}

	typedef void DetailedTrackProc(final TileInfo ti);
	static DetailedTrackProc* final _detailed_track_proc[] = {
		DetTrackDrawProc_Null,
		DetTrackDrawProc_Null,

		DrawTrackFence_NW,
		DrawTrackFence_SE,
		DrawTrackFence_NW_SE,

		DrawTrackFence_NE,
		DrawTrackFence_SW,
		DrawTrackFence_NE_SW,

		DrawTrackFence_NS_1,
		DrawTrackFence_NS_2,

		DrawTrackFence_WindowEvents.WE_1,
		DrawTrackFence_WindowEvents.WE_2,

		DetTrackDrawProc_Null,
		DetTrackDrawProc_Null,
		DetTrackDrawProc_Null,
		DetTrackDrawProc_Null,
	};

	static void DrawSpecialBuilding(int image, int offset,
	                                final TileInfo  ti,
	                                byte x, byte y, byte z,
	                                byte xsize, byte ysize, byte zsize)
	{
		if (image & PALETTE_MODIFIER_COLOR) image |= _drawtile_track_palette;
		image += offset;
		if (_displayGameOptions._opt & DO_TRANS_BUILDINGS) MAKE_TRANSPARENT(image);
		AddSortableSpriteToDraw(image, ti.x + x, ti.y + y, xsize, ysize, zsize, ti.z + z);
	}

	/**
	 * Draw ground sprite and track bits
	 * @param ti TileInfo
	 * @param track TrackBits to draw
	 * @param earth Draw as earth
	 * @param snow Draw as snow
	 * @param flat Always draw foundation
	 */
	void DrawTrackBits(TileInfo ti, TrackBits track, boolean earth, boolean snow, boolean flat)
	{
		final RailtypeInfo *rti = GetRailTypeInfo(GetRailType(ti.tile));
		PalSpriteID image;
		boolean junction = false;

		// Select the sprite to use.
		(image = rti.base_sprites.track_y, track == TRACK_BIT_DIAG2) ||
		(image++,                           track == TRACK_BIT_DIAG1) ||
		(image++,                           track == TRACK_BIT_UPPER) ||
		(image++,                           track == TRACK_BIT_LOWER) ||
		(image++,                           track == TRACK_BIT_RIGHT) ||
		(image++,                           track == TRACK_BIT_LEFT) ||
		(image++,                           track == (TRACK_BIT_DIAG1 | TRACK_BIT_DIAG2)) ||

		(image = rti.base_sprites.track_ns, track == (TRACK_BIT_UPPER | TRACK_BIT_LOWER)) ||
		(image++,                            track == (TRACK_BIT_LEFT | TRACK_BIT_RIGHT)) ||

		(junction = true, false) ||
		(image = rti.base_sprites.ground, !(track & (TRACK_BIT_RIGHT | TRACK_BIT_UPPER | TRACK_BIT_DIAG1))) ||
		(image++,                          !(track & (TRACK_BIT_LEFT | TRACK_BIT_LOWER | TRACK_BIT_DIAG1))) ||
		(image++,                          !(track & (TRACK_BIT_LEFT | TRACK_BIT_UPPER | TRACK_BIT_DIAG2))) ||
		(image++,                          !(track & (TRACK_BIT_RIGHT | TRACK_BIT_LOWER | TRACK_BIT_DIAG2))) ||
		(image++, true);

		if (ti.tileh != 0) {
			int foundation;

			if (flat) {
				foundation = ti.tileh;
			} else {
				foundation = GetRailFoundation(ti.tileh, track);
			}

			if (foundation != 0)
				DrawFoundation(ti, foundation);

			// DrawFoundation() modifies ti.
			// Default sloped sprites..
			if (ti.tileh != 0)
				image = _track_sloped_sprites[ti.tileh - 1] + rti.base_sprites.track_y;
		}

		if (earth) {
			image = (image & SPRITE_MASK) | PALETTE_TO_BARE_LAND; // Use brown palette
		} else if (snow) {
			image += rti.snow_offset;
		}

		DrawGroundSprite(image);

		// Draw track pieces individually for junction tiles
		if (junction) {
			if (track & TRACK_BIT_DIAG1) DrawGroundSprite(rti.base_sprites.single_y);
			if (track & TRACK_BIT_DIAG2) DrawGroundSprite(rti.base_sprites.single_x);
			if (track & TRACK_BIT_UPPER) DrawGroundSprite(rti.base_sprites.single_n);
			if (track & TRACK_BIT_LOWER) DrawGroundSprite(rti.base_sprites.single_s);
			if (track & TRACK_BIT_LEFT)  DrawGroundSprite(rti.base_sprites.single_w);
			if (track & TRACK_BIT_RIGHT) DrawGroundSprite(rti.base_sprites.single_e);
		}

		if (_debug_pbs_level >= 1) {
			byte pbs = PBSTileReserved(ti.tile) & track;
			if (pbs & TRACK_BIT_DIAG1) DrawGroundSprite(rti.base_sprites.single_y | PALETTE_CRASH);
			if (pbs & TRACK_BIT_DIAG2) DrawGroundSprite(rti.base_sprites.single_x | PALETTE_CRASH);
			if (pbs & TRACK_BIT_UPPER) DrawGroundSprite(rti.base_sprites.single_n | PALETTE_CRASH);
			if (pbs & TRACK_BIT_LOWER) DrawGroundSprite(rti.base_sprites.single_s | PALETTE_CRASH);
			if (pbs & TRACK_BIT_LEFT)  DrawGroundSprite(rti.base_sprites.single_w | PALETTE_CRASH);
			if (pbs & TRACK_BIT_RIGHT) DrawGroundSprite(rti.base_sprites.single_e | PALETTE_CRASH);
		}
	}

	static void DrawTile_Track(TileInfo ti)
	{
		byte m5;
		final RailtypeInfo *rti = GetRailTypeInfo(GetRailType(ti.tile));
		PalSpriteID image;

		_drawtile_track_palette = SPRITE_PALETTE(PLAYER_SPRITE_COLOR(GetTileOwner(ti.tile)));

		m5 = (byte)ti.map5;
		if (!(m5 & RAIL_TYPE_SPECIAL)) {
			boolean earth = (_m[ti.tile].m2 & RAIL_MAP2LO_GROUND_MASK) == RAIL_GROUND_BROWN;
			boolean snow = (_m[ti.tile].m2 & RAIL_MAP2LO_GROUND_MASK) == RAIL_GROUND_ICE_DESERT;

			DrawTrackBits(ti, m5 & TRACK_BIT_MASK, earth, snow, false);

			if (_displayGameOptions._opt & DO_FULL_DETAIL) {
				_detailed_track_proc[_m[ti.tile].m2 & RAIL_MAP2LO_GROUND_MASK](ti);
			}

			/* draw signals also? */
			if (!(ti.map5 & RAIL_TYPE_SIGNALS))
				return;

			{
				byte m23;

				m23 = (_m[ti.tile].m3 >> 4) | (_m[ti.tile].m2 & 0xF0);

	#define HAS_SIGNAL(x) (m23 & (byte)(0x1 << (x)))
	#define ISON_SIGNAL(x) (m23 & (byte)(0x10 << (x)))
	#define MAYBE_DRAW_SIGNAL(x,y,z) if (HAS_SIGNAL(x)) DrawSignalHelper(ti, ISON_SIGNAL(x), ((y-0x4FB) << 4)|(z))

			if (!(m5 & TRACK_BIT_DIAG2)) {
				if (!(m5 & TRACK_BIT_DIAG1)) {
					if (m5 & TRACK_BIT_LEFT) {
						MAYBE_DRAW_SIGNAL(2, 0x509, 0);
						MAYBE_DRAW_SIGNAL(3, 0x507, 1);
					}
					if (m5 & TRACK_BIT_RIGHT) {
						MAYBE_DRAW_SIGNAL(0, 0x509, 2);
						MAYBE_DRAW_SIGNAL(1, 0x507, 3);
					}
					if (m5 & TRACK_BIT_UPPER) {
						MAYBE_DRAW_SIGNAL(3, 0x505, 4);
						MAYBE_DRAW_SIGNAL(2, 0x503, 5);
					}
					if (m5 & TRACK_BIT_LOWER) {
						MAYBE_DRAW_SIGNAL(1, 0x505, 6);
						MAYBE_DRAW_SIGNAL(0, 0x503, 7);
					}
				} else {
					MAYBE_DRAW_SIGNAL(3, 0x4FB, 8);
					MAYBE_DRAW_SIGNAL(2, 0x4FD, 9);
				}
			} else {
				MAYBE_DRAW_SIGNAL(3, 0x4FF, 10);
				MAYBE_DRAW_SIGNAL(2, 0x501, 11);
			}
			}
		} else {
			/* draw depots / waypoints */
			final DrawTrackSeqStruct *drss;
			byte type = m5 & 0x3F; // 0-3: depots, 4-5: waypoints

			if (!(m5 & (RAIL_TILE_TYPE_MASK&~RAIL_TYPE_SPECIAL)))
				/* XXX: There used to be "return;" here, but since I could not find out
				 * why this would ever occur, I put assert(0) here. Let's see if someone
				 * complains about it. If not, we'll remove this check. (Matthijs). */
				 assert(0);

			if (ti.tileh != 0) DrawFoundation(ti, ti.tileh);

			if (IsRailWaypoint(ti.tile) && BitOps.HASBIT(_m[ti.tile].m3, 4)) {
				// look for customization
				byte stat_id = GetWaypointByTile(ti.tile).stat_id;
				final StationSpec *stat = GetCustomStation(STAT_CLASS_WAYP, stat_id);

				if (stat != null) {
					DrawTileSeqStruct final *seq;
					// emulate station tile - open with building
					final DrawTileSprites *cust = &stat.renderdata[2 + (m5 & 0x1)];
					int relocation = GetCustomStationRelocation(stat, ComposeWaypointStation(ti.tile), 0);

					/* We don't touch the 0x8000 bit. In all this
					 * waypoint code, it is used to indicate that
					 * we should offset by railtype, but we always
					 * do that for custom ground sprites and never
					 * for station sprites. And in the drawing
					 * code, it is used to indicate that the sprite
					 * should be drawn in company colors, and it's
					 * up to the GRF file to decide that. */

					image = cust.ground_sprite;
					image += (image < _custom_sprites_base) ? rti.total_offset : GetRailType(ti.tile);

					DrawGroundSprite(image);

					foreach_draw_tile_seq(seq, cust.seq) {
						int image = seq.image + relocation;
						DrawSpecialBuilding(image, 0, ti,
						                    seq.delta_x, seq.delta_y, seq.delta_z,
						                    seq.width, seq.height, seq.unk);
					}
					return;
				}
			}

			drss = _track_depot_layout_table[type];

			image = drss++.image;
			/* @note This is kind of an ugly hack, as the PALETTE_MODIFIER_COLOR indicates
		 	 * whether the sprite is railtype dependent. Rewrite this asap */
			if (image & PALETTE_MODIFIER_COLOR) image = (image & SPRITE_MASK) + rti.total_offset;

			// adjust ground tile for desert
			// (don't adjust for arctic depots, because snow in depots looks weird)
			// type >= 4 means waypoints
			if ((_m[ti.tile].m4 & RAIL_MAP2LO_GROUND_MASK) == RAIL_GROUND_ICE_DESERT && (GameOptions._opt.landscape == Landscape.LT_DESERT || type >= 4)) {
				if (image != Sprite.SPR_FLAT_GRASS_TILE) {
					image += rti.snow_offset; // tile with tracks
				} else {
					image = Sprite.SPR_FLAT_SNOWY_TILE; // flat ground
				}
			}

			DrawGroundSprite(image);

			if (_debug_pbs_level >= 1) {
				byte pbs = PBSTileReserved(ti.tile);
				if (pbs & TRACK_BIT_DIAG1) DrawGroundSprite(rti.base_sprites.single_y | PALETTE_CRASH);
				if (pbs & TRACK_BIT_DIAG2) DrawGroundSprite(rti.base_sprites.single_x | PALETTE_CRASH);
				if (pbs & TRACK_BIT_UPPER) DrawGroundSprite(rti.base_sprites.single_n | PALETTE_CRASH);
				if (pbs & TRACK_BIT_LOWER) DrawGroundSprite(rti.base_sprites.single_s | PALETTE_CRASH);
				if (pbs & TRACK_BIT_LEFT)  DrawGroundSprite(rti.base_sprites.single_w | PALETTE_CRASH);
				if (pbs & TRACK_BIT_RIGHT) DrawGroundSprite(rti.base_sprites.single_e | PALETTE_CRASH);
			}

			for (; drss.image != 0; drss++) {
				DrawSpecialBuilding(drss.image, type < 4 ? rti.total_offset : 0, ti,
				                    drss.subcoord_x, drss.subcoord_y, 0,
				                    drss.width, drss.height, 0x17);
			}
		}
	}

	void DrawTrainDepotSprite(int x, int y, int image, RailType railtype)
	{
		int ormod, img;
		final RailtypeInfo *rti = GetRailTypeInfo(railtype);
		final DrawTrackSeqStruct *dtss;

		ormod = PLAYER_SPRITE_COLOR(Global._local_player);

		dtss = _track_depot_layout_table[image];

		x += 33;
		y += 17;

		img = dtss++.image;
		/* @note This is kind of an ugly hack, as the PALETTE_MODIFIER_COLOR indicates
		 * whether the sprite is railtype dependent. Rewrite this asap */
		if (img & PALETTE_MODIFIER_COLOR) img = (img & SPRITE_MASK) + rti.total_offset;
		Gfx.DrawSprite(img, x, y);

		for (; dtss.image != 0; dtss++) {
			Point pt = RemapCoords(dtss.subcoord_x, dtss.subcoord_y, 0);
			image = dtss.image;
			if (image & PALETTE_MODIFIER_COLOR) image |= ormod;
			Gfx.DrawSprite(image + rti.total_offset, x + pt.x, y + pt.y);
		}
	}

	void DrawDefaultWaypointSprite(int x, int y, RailType railtype)
	{
		final DrawTrackSeqStruct *dtss = _track_depot_layout_table[4];
		final RailtypeInfo *rti = GetRailTypeInfo(railtype);
		int img;

		img = dtss++.image;
		if (img & PALETTE_MODIFIER_COLOR) img = (img & SPRITE_MASK) + rti.total_offset;
		Gfx.DrawSprite(img, x, y);

		for (; dtss.image != 0; dtss++) {
			Point pt = RemapCoords(dtss.subcoord_x, dtss.subcoord_y, 0);
			img = dtss.image;
			if (img & PALETTE_MODIFIER_COLOR) img |= PLAYER_SPRITE_COLOR(Global._local_player);
			Gfx.DrawSprite(img, x + pt.x, y + pt.y);
		}
	}

	class SetSignalsData {
		int cur;
		int cur_stack;
		boolean stop;
		boolean has_presignal;

		boolean has_pbssignal;
			// lowest 2 bits = amount of pbs signals in the block, clamped at 2
			// bit 2 = there is a pbs entry signal in this block
			// bit 3 = there is a pbs exit signal in this block

		// presignal info
		int presignal_exits;
		int presignal_exits_free;

		// these are used to keep track of the signals that change.
		byte bit[NUM_SSD_ENTRY];
		TileIndex tile[NUM_SSD_ENTRY];

		int pbs_cur;
		// these are used to keep track of all signals in the block
		TileIndex pbs_tile[NUM_SSD_ENTRY];

		// these are used to keep track of the stack that modifies presignals recursively
		TileIndex next_tile[NUM_SSD_STACK];
		byte next_dir[NUM_SSD_STACK];

	} SetSignalsData;

	static boolean SetSignalsEnumProc(TileIndex tile, SetSignalsData *ssd, int track, int length, byte *state)
	{
		// the tile has signals?
		if (tile.IsTileType( TileTypes.MP_RAILWAY)) {
			if (HasSignalOnTrack(tile, TrackdirToTrack(track))) {
				if ((tile.getMap().m3 & _signals_table[track]) != 0) {
					// yes, add the signal to the list of signals
					if (ssd.cur != NUM_SSD_ENTRY) {
						ssd.tile[ssd.cur] = tile; // remember the tile index
						ssd.bit[ssd.cur] = track; // and the controlling bit number
						ssd.cur++;
					}

				if (PBSIsPbsSignal(tile, ReverseTrackdir(track)))
					SETBIT(ssd.has_pbssignal, 2);

					// remember if this block has a presignal.
					ssd.has_presignal |= (tile.getMap().m4&1);
				}

				if (PBSIsPbsSignal(tile, ReverseTrackdir(track)) || PBSIsPbsSignal(tile, track)) {
					byte num = ssd.has_pbssignal & 3;
					num = clamp(num + 1, 0, 2);
					ssd.has_pbssignal &= ~3;
					ssd.has_pbssignal |= num;
				}

				if ((tile.getMap().m3 & _signals_table_both[track]) != 0) {
					ssd.pbs_tile[ssd.pbs_cur] = tile; // remember the tile index
					ssd.pbs_cur++;
				}

				if (tile.getMap().m3&_signals_table_other[track]) {
					if (tile.getMap().m4&2) {
						// this is an exit signal that points out from the segment
						ssd.presignal_exits++;
						if ((tile.getMap().m2&_signals_table_other[track]) != 0)
							ssd.presignal_exits_free++;
					}
					if (PBSIsPbsSignal(tile, track))
						SETBIT(ssd.has_pbssignal, 3);
				}

				return true;
			} else if (IsTileDepotType(tile, TRANSPORT_RAIL)) {
				return true; // don't look further if the tile is a depot
			}
		}
		return false;
	}

	/* Struct to parse data from VehicleFromPos to SignalVehicleCheckProc */
	class SignalVehicleCheckStruct {
		TileIndex tile;
		int track;
	} SignalVehicleCheckStruct;

	static void *SignalVehicleCheckProc(Vehicle v, void *data)
	{
		final SignalVehicleCheckStruct* dest = data;
		TileIndex tile;

		if (v.type != Vehicle.VEH_Train) return null;

		/* Find the tile outside the tunnel, for signalling */
		if (v.u.rail.track == 0x40) {
			tile = GetVehicleOutOfTunnelTile(v);
		} else {
			tile = v.tile;
		}

		/* Wrong tile, or no train? Not a match */
		if (tile != dest.tile) return null;

		/* Are we on the same piece of track? */
		if (dest.track & (v.u.rail.track + (v.u.rail.track << 8))) return v;

		return null;
	}

	/* Special check for SetSignalsAfterProc, to see if there is a vehicle on this tile */
	static boolean SignalVehicleCheck(TileIndex tile, int track)
	{
		SignalVehicleCheckStruct dest;

		dest.tile = tile;
		dest.track = track;

		/** @todo "Hackish" fix for the tunnel problems. This is needed because a tunnel
		 * is some kind of invisible black hole, and there is some special magic going
		 * on in there. This 'workaround' can be removed once the maprewrite is done.
		 */
		if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && BitOps.GB(tile.getMap().m5, 4, 4) == 0) {
			// It is a tunnel we're checking, we need to do some special stuff
			// because VehicleFromPos will not find the vihicle otherwise
			byte direction = BitOps.GB(tile.getMap().m5, 0, 2);
			FindLengthOfTunnelResult flotr;
			flotr = FindLengthOfTunnel(tile, direction);
			dest.track = 1 << (direction & 1); // get the trackbit the vehicle would have if it has not entered the tunnel yet (ie is still visible)

			// check for a vehicle with that trackdir on the start tile of the tunnel
			if (VehicleFromPos(tile, &dest, SignalVehicleCheckProc) != null) return true;

			// check for a vehicle with that trackdir on the end tile of the tunnel
			if (VehicleFromPos(flotr.tile, &dest, SignalVehicleCheckProc) != null) return true;

			// now check all tiles from start to end for a "hidden" vehicle
			// NOTE: the hashes for tiles may overlap, so this could maybe be optimised a bit by not checking every tile?
			dest.track = 0x40; // trackbit for vehicles "hidden" inside a tunnel
			for (; tile != flotr.tile; tile += TileOffsByDir(direction)) {
				if (VehicleFromPos(tile, &dest, SignalVehicleCheckProc) != null)
					return true;
			}

			// no vehicle found
			return false;
		}

		return VehicleFromPos(tile, &dest, SignalVehicleCheckProc) != null;
	}

	static void SetSignalsAfterProc(TrackPathFinder *tpf)
	{
		SetSignalsData *ssd = tpf.userdata;
		final TrackPathFinderLink* link;
		int offs;
		int i;

		ssd.stop = false;

		/* Go through all the PF tiles */
		for (i = 0; i < lengthof(tpf.hash_head); i++) {
			/* Empty hash item */
			if (tpf.hash_head[i] == 0) continue;

			/* If 0x8000 is not set, there is only 1 item */
			if (!(tpf.hash_head[i] & 0x8000)) {
				/* Check if there is a vehicle on this tile */
				if (SignalVehicleCheck(tpf.hash_tile[i], tpf.hash_head[i])) {
					ssd.stop = true;
					return;
				}
			} else {
				/* There are multiple items, where hash_tile points to the first item in the list */
				offs = tpf.hash_tile[i];
				do {
					/* Find the next item */
					link = PATHFIND_GET_LINK_PTR(tpf, offs);
					/* Check if there is a vehicle on this tile */
					if (SignalVehicleCheck(link.tile, link.flags)) {
						ssd.stop = true;
						return;
					}
					/* Goto the next item */
				} while ((offs=link.next) != 0xFFFF);
			}
		}
	}



	static void ChangeSignalStates(SetSignalsData *ssd)
	{
		int i;

		// thinking about presignals...
		// the presignal is green if,
		//   if no train is in the segment AND
		//   there is at least one green exit signal OR
		//   there are no exit signals in the segment

		// convert the block to pbs, if needed
		if (Global._patches.auto_pbs_placement && !(ssd.stop) && (ssd.has_pbssignal == 0xE) && !ssd.has_presignal && (ssd.presignal_exits == 0)) // 0xE means at least 2 pbs signals, and at least 1 entry and 1 exit, see comments ssd.has_pbssignal
		for (i = 0; i != ssd.pbs_cur; i++) {
			TileIndex tile = ssd.pbs_tile[i];
			BitOps.RET SB(tile.getMap().m4, 0, 3, SIGTYPE_PBS);
			MarkTileDirtyByTile(tile);
		};

		// then mark the signals in the segment accordingly
		for (i = 0; i != ssd.cur; i++) {
			TileIndex tile = ssd.tile[i];
			byte bit = _signals_table[ssd.bit[i]];
			int m2 = tile.getMap().m2;

			// presignals don't turn green if there is at least one presignal exit and none are free
			if (tile.getMap().m4 & 1) {
				int ex = ssd.presignal_exits, exfree = ssd.presignal_exits_free;

				// subtract for dual combo signals so they don't count themselves
				if (tile.getMap().m4&2 && tile.getMap().m3&_signals_table_other[ssd.bit[i]]) {
					ex--;
					if ((tile.getMap().m2&_signals_table_other[ssd.bit[i]]) != 0) exfree--;
				}

				// if we have exits and none are free, make red.
				if (ex && !exfree) goto make_red;
			}

			// check if the signal is unaffected.
			if (ssd.stop) {
	make_red:
				// turn red
				if ( (bit&m2) == 0 )
					continue;
			} else {
				// turn green
				if ( (bit&m2) != 0 )
					continue;
			}

			/* Update signals on the other side of this exit-combo signal; it changed. */
			if (tile.getMap().m4 & 2 ) {
				if (ssd.cur_stack != NUM_SSD_STACK) {
					ssd.next_tile[ssd.cur_stack] = tile;
					ssd.next_dir[ssd.cur_stack] = _dir_from_track[ssd.bit[i]];
					ssd.cur_stack++;
				} else {
					printf("NUM_SSD_STACK too small\n"); /// @todo WTF is this???
				}
			}

			// it changed, so toggle it
			tile.getMap().m2 = m2 ^ bit;
			MarkTileDirtyByTile(tile);
		}
	}


	boolean UpdateSignalsOnSegment(TileIndex tile, byte direction)
	{
		SetSignalsData ssd;
		int result = -1;

		ssd.cur_stack = 0;
		direction >>= 1;

		for(;;) {
			// go through one segment and update all signals pointing into that segment.
			ssd.cur = ssd.pbs_cur = ssd.presignal_exits = ssd.presignal_exits_free = 0;
			ssd.has_presignal = false;
			ssd.has_pbssignal = false;

			FollowTrack(tile, 0xC000 | TRANSPORT_RAIL, direction, (TPFEnumProc*)SetSignalsEnumProc, SetSignalsAfterProc, &ssd);
			ChangeSignalStates(&ssd);

			// remember the result only for the first iteration.
			if (result < 0) result = ssd.stop;

			// if any exit signals were changed, we need to keep going to modify the stuff behind those.
			if (ssd.cur_stack == 0) break;

			// one or more exit signals were changed, so we need to update another segment too.
			tile = ssd.next_tile[--ssd.cur_stack];
			direction = ssd.next_dir[ssd.cur_stack];
		}

		return (boolean)result;
	}

	void SetSignalsOnBothDir(TileIndex tile, byte track)
	{
		static final byte _search_dir_1[6] = {1, 3, 1, 3, 5, 3};
		static final byte _search_dir_2[6] = {5, 7, 7, 5, 7, 1};

		UpdateSignalsOnSegment(tile, _search_dir_1[track]);
		UpdateSignalsOnSegment(tile, _search_dir_2[track]);
	}

	static int GetSlopeZ_Track(final TileInfo  ti)
	{
		int z = ti.z;
		int th = ti.tileh;

		// check if it's a foundation
		if (ti.tileh != 0) {
			if ((ti.map5 & 0x80) == 0) {
				int f = GetRailFoundation(ti.tileh, ti.map5 & 0x3F);
				if (f != 0) {
					if (f < 15) {
						// leveled foundation
						return z + 8;
					}
					// inclined foundation
					th = _inclined_tileh[f - 15];
				}
			} else if ((ti.map5 & 0xC0) == 0xC0) {
				// depot or waypoint
				return z + 8;
			}
			return GetPartialZ(ti.x&0xF, ti.y&0xF, th) + z;
		}
		return z;
	}

	static int GetSlopeTileh_Track(final TileInfo ti)
	{
		// check if it's a foundation
		if (ti.tileh != 0) {
			if ((ti.map5 & 0x80) == 0) {
				int f = GetRailFoundation(ti.tileh, ti.map5 & 0x3F);
				if (f != 0) {
					if (f < 15) {
						// leveled foundation
						return 0;
					}
					// inclined foundation
					return _inclined_tileh[f - 15];
				}
			} else if ((ti.map5 & 0xC0) == 0xC0) {
				// depot or waypoint
				return 0;
			}
		}
		return ti.tileh;
	}

	static void GetAcceptedCargo_Track(TileIndex tile, AcceptedCargo ac)
	{
		/* not used */
	}

	static void AnimateTile_Track(TileIndex tile)
	{
		/* not used */
	}

	static void TileLoop_Track(TileIndex tile)
	{
		byte old_ground;
		byte new_ground;
		TrackBits rail;

		old_ground = tile.getMap().m5 & RAIL_TYPE_SPECIAL ? BitOps.GB(tile.getMap().m4, 0, 4) : BitOps.GB(tile.getMap().m2, 0, 4);

		switch (GameOptions._opt.landscape) {
			case Landscape.LT_HILLY:
				if (GetTileZ(tile) > GameOptions._opt.snow_line) { /* convert into snow? */
					new_ground = RAIL_GROUND_ICE_DESERT;
					goto modify_me;
				}
				break;

			case Landscape.LT_DESERT:
				if (GetMapExtraBits(tile) == 1) { /* convert into desert? */
					new_ground = RAIL_GROUND_ICE_DESERT;
					goto modify_me;
				}
				break;
		}

		// Don't continue tile loop for depots
		if (tile.getMap().m5 & RAIL_TYPE_SPECIAL) return;

		new_ground = RAIL_GROUND_GREEN;

		if (old_ground != RAIL_GROUND_BROWN) { /* wait until bottom is green */
			/* determine direction of fence */
			rail = tile.getMap().m5 & TRACK_BIT_MASK;

			if (rail == TRACK_BIT_UPPER) {
				new_ground = RAIL_GROUND_FENCE_HORIZ1;
			} else if (rail == TRACK_BIT_LOWER) {
				new_ground = RAIL_GROUND_FENCE_HORIZ2;
			} else if (rail == TRACK_BIT_LEFT) {
				new_ground = RAIL_GROUND_FENCE_VERT1;
			} else if (rail == TRACK_BIT_RIGHT) {
				new_ground = RAIL_GROUND_FENCE_VERT2;
			} else {
				PlayerID owner = GetTileOwner(tile);

				if ( (!(rail&(TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LEFT)) && (rail&TRACK_BIT_DIAG1)) || rail==(TRACK_BIT_LOWER|TRACK_BIT_RIGHT)) {
					if (!IsTileType(tile + TileDiffXY(0, -1), TileTypes.MP_RAILWAY) ||
							!IsTileOwner(tile + TileDiffXY(0, -1), owner) ||
							(_m[tile + TileDiffXY(0, -1)].m5 == TRACK_BIT_UPPER || _m[tile + TileDiffXY(0, -1)].m5 == TRACK_BIT_LEFT))
								new_ground = RAIL_GROUND_FENCE_NW;
				}

				if ( (!(rail&(TRACK_BIT_DIAG2|TRACK_BIT_LOWER|TRACK_BIT_RIGHT)) && (rail&TRACK_BIT_DIAG1)) || rail==(TRACK_BIT_UPPER|TRACK_BIT_LEFT)) {
					if (!IsTileType(tile + TileDiffXY(0, 1), TileTypes.MP_RAILWAY) ||
							!IsTileOwner(tile + TileDiffXY(0, 1), owner) ||
							(_m[tile + TileDiffXY(0, 1)].m5 == TRACK_BIT_LOWER || _m[tile + TileDiffXY(0, 1)].m5 == TRACK_BIT_RIGHT))
								new_ground = (new_ground == RAIL_GROUND_FENCE_NW) ? RAIL_GROUND_FENCE_SENW : RAIL_GROUND_FENCE_SE;
				}

				if ( (!(rail&(TRACK_BIT_DIAG1|TRACK_BIT_UPPER|TRACK_BIT_RIGHT)) && (rail&TRACK_BIT_DIAG2)) || rail==(TRACK_BIT_LOWER|TRACK_BIT_LEFT)) {
					if (!IsTileType(tile + TileDiffXY(-1, 0), TileTypes.MP_RAILWAY) ||
							!IsTileOwner(tile + TileDiffXY(-1, 0), owner) ||
							(_m[tile + TileDiffXY(-1, 0)].m5 == TRACK_BIT_UPPER || _m[tile + TileDiffXY(-1, 0)].m5 == TRACK_BIT_RIGHT))
								new_ground = RAIL_GROUND_FENCE_NE;
				}

				if ( (!(rail&(TRACK_BIT_DIAG1|TRACK_BIT_LOWER|TRACK_BIT_LEFT)) && (rail&TRACK_BIT_DIAG2)) || rail==(TRACK_BIT_UPPER|TRACK_BIT_RIGHT)) {
					if (!IsTileType(tile + TileDiffXY(1, 0), TileTypes.MP_RAILWAY) ||
							!IsTileOwner(tile + TileDiffXY(1, 0), owner) ||
							(_m[tile + TileDiffXY(1, 0)].m5 == TRACK_BIT_LOWER || _m[tile + TileDiffXY(1, 0)].m5 == TRACK_BIT_LEFT))
								new_ground = (new_ground == RAIL_GROUND_FENCE_NE) ? RAIL_GROUND_FENCE_NESW : RAIL_GROUND_FENCE_SW;
				}
			}
		}

	modify_me:;
		/* tile changed? */
		if (old_ground != new_ground) {
			if (tile.getMap().m5 & RAIL_TYPE_SPECIAL) {
				BitOps.RET SB(tile.getMap().m4, 0, 4, new_ground);
			} else {
				BitOps.RET SB(tile.getMap().m2, 0, 4, new_ground);
			}
			MarkTileDirtyByTile(tile);
		}
	}


	static int GetTileTrackStatus_Track(TileIndex tile, TransportType mode)
	{
		byte m5, a;
		int b;
		int ret;

		if (mode != TRANSPORT_RAIL) return 0;

		m5 = tile.getMap().m5;

		if (!(m5 & RAIL_TYPE_SPECIAL)) {
			ret = (m5 | (m5 << 8)) & 0x3F3F;
			if (!(m5 & RAIL_TYPE_SIGNALS)) {
				if ( (ret & 0xFF) == 3)
				/* Diagonal crossing? */
					ret |= 0x40;
			} else {
				/* has_signals */

				a = tile.getMap().m3;
				b = tile.getMap().m2;

				b &= a;

				/* When signals are not present (in neither
				 * direction), we pretend them to be green. (So if
				 * signals are only one way, the other way will
				 * implicitely become `red' */
				if ((a & 0xC0) == 0) b |= 0xC0;
				if ((a & 0x30) == 0) b |= 0x30;

				if ((b & 0x80) == 0) ret |= 0x10070000;
				if ((b & 0x40) == 0) ret |= 0x07100000;
				if ((b & 0x20) == 0) ret |= 0x20080000;
				if ((b & 0x10) == 0) ret |= 0x08200000;
			}
		} else if (m5 & 0x40) {
			static final byte _train_spec_tracks[6] = {1,2,1,2,1,2};
			m5 = _train_spec_tracks[m5 & 0x3F];
			ret = (m5 << 8) + m5;
		} else
			return 0;
		return ret;
	}

	static void ClickTile_Track(TileIndex tile)
	{
		if (IsTileDepotType(tile, TRANSPORT_RAIL)) {
			ShowTrainDepotWindow(tile);
		} else if (IsRailWaypoint(tile)) {
			ShowRenameWaypointWindow(GetWaypointByTile(tile));
		}
	}

	static void GetTileDesc_Track(TileIndex tile, TileDesc *td)
	{
		td.owner = GetTileOwner(tile);
		switch (GetRailTileType(tile)) {
			case RAIL_TYPE_NORMAL:
				td.str = Str.STR_1021_RAILROAD_TRACK;
				break;

			case RAIL_TYPE_SIGNALS: {
				final StringID signal_type[7] = {
					Str.STR_RAILROAD_TRACK_WITH_NORMAL_SIGNALS,
					Str.STR_RAILROAD_TRACK_WITH_PRESIGNALS,
					Str.STR_RAILROAD_TRACK_WITH_EXITSIGNALS,
					Str.STR_RAILROAD_TRACK_WITH_COMBOSIGNALS,
					Str.STR_RAILROAD_TRACK_WITH_PBSSIGNALS,
					Str.STR_NULL, Str.STR_NULL
				};

				td.str = signal_type[BitOps.GB(tile.getMap().m4, 0, 3)];
				break;
			}

			case RAIL_TYPE_DEPOT_WAYPOINT:
			default:
				td.str = ((tile.getMap().m5 & RAIL_SUBTYPE_MASK) == RAIL_SUBTYPE_DEPOT) ?
					Str.STR_1023_RAILROAD_TRAIN_DEPOT : Str.STR_LANDINFO_WAYPOINT;
				break;
		}
	}

	static void ChangeTileOwner_Track(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		if (!IsTileOwner(tile, old_player)) return;

		if (new_player != Owner.OWNER_SPECTATOR) {
			SetTileOwner(tile, new_player);
		}	else {
			DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
		}
	}

	static final byte _fractcoords_behind[4] = { 0x8F, 0x8, 0x80, 0xF8 };
	static final byte _fractcoords_enter[4] = { 0x8A, 0x48, 0x84, 0xA8 };
	static final byte _deltacoord_leaveoffset[8] = {
		-1,  0,  1,  0, /* x */
		 0,  1,  0, -1  /* y */
	};
	static final byte _enter_directions[4] = {5, 7, 1, 3};
	static final byte _leave_directions[4] = {1, 3, 5, 7};
	static final byte _depot_track_mask[4] = {1, 2, 1, 2};

	static int VehicleEnter_Track(Vehicle v, TileIndex tile, int x, int y)
	{
		byte fract_coord;
		byte fract_coord_leave;
		int dir;
		int length;

		// this routine applies only to trains in depot tiles
		if (v.type != Vehicle.VEH_Train || !IsTileDepotType(tile, TRANSPORT_RAIL)) return 0;

		/* depot direction */
		dir = GetDepotDirection(tile, TRANSPORT_RAIL);

		/* calculate the point where the following wagon should be activated */
		/* this depends on the length of the current vehicle */
		length = v.u.rail.cached_veh_length;

		fract_coord_leave =
			((_fractcoords_enter[dir] & 0x0F) +				// x
				(length + 1) * _deltacoord_leaveoffset[dir]) +
			(((_fractcoords_enter[dir] >> 4) +				// y
				((length + 1) * _deltacoord_leaveoffset[dir+4])) << 4);

		fract_coord = (x & 0xF) + ((y & 0xF) << 4);

		if (_fractcoords_behind[dir] == fract_coord) {
			/* make sure a train is not entering the tile from behind */
			return 8;
		} else if (_fractcoords_enter[dir] == fract_coord) {
			if (_enter_directions[dir] == v.direction) {
				/* enter the depot */
				if (v.next == null)
					PBSClearTrack(v.tile, FIND_FIRST_BIT(v.u.rail.track));
				v.u.rail.track = 0x80,
				v.vehstatus |= VS_HIDDEN; /* hide it */
				v.direction ^= 4;
				if (v.next == null)
					TrainEnterDepot(v, tile);
				v.tile = tile;
				Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, tile);
				return 4;
			}
		} else if (fract_coord_leave == fract_coord) {
			if (_leave_directions[dir] == v.direction) {
				/* leave the depot? */
				if ((v=v.next) != null) {
					v.vehstatus &= ~VS_HIDDEN;
					v.u.rail.track = _depot_track_mask[dir];
					assert(v.u.rail.track);
				}
			}
		}

		return 0;
	}

	void InitializeRail()
	{
		_last_built_train_depot_tile = 0;
	}

	final TileTypeProcs _tile_type_rail_procs = {
		DrawTile_Track,						/* draw_tile_proc */
		GetSlopeZ_Track,					/* get_slope_z_proc */
		ClearTile_Track,					/* clear_tile_proc */
		GetAcceptedCargo_Track,		/* get_accepted_cargo_proc */
		GetTileDesc_Track,				/* get_tile_desc_proc */
		GetTileTrackStatus_Track,	/* get_tile_track_status_proc */
		ClickTile_Track,					/* click_tile_proc */
		AnimateTile_Track,				/* animate_tile_proc */
		TileLoop_Track,						/* tile_loop_clear */
		ChangeTileOwner_Track,		/* change_tile_owner_clear */
		null,											/* get_produced_cargo_proc */
		VehicleEnter_Track,				/* vehicle_enter_tile_proc */
		null,											/* vehicle_leave_tile_proc */
		GetSlopeTileh_Track,			/* get_slope_tileh_proc */
	};
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	

	
	
	
	
	
	
	
	
	
	
	
	
	/* $Id: rail_gui.c 3270 2005-12-07 15:48:52Z peter1138 $ */

	/** @file rail_gui.c File for dealing with rail finalruction user interface */




















	static RailType _cur_railtype;
	static boolean _remove_button_clicked;
	static byte _build_depot_direction;
	static byte _waypoint_count = 1;
	static byte _cur_waypoint_type;
	static byte _cur_signal_type;
	static byte _cur_presig_type;
	static boolean _cur_autosig_compl;
	 
	static final StringID _presig_types_dropdown[] = {
		Str.STR_SIGNAL_NORMAL,
		Str.STR_SIGNAL_ENTRANCE,
		Str.STR_SIGNAL_EXIT,
		Str.STR_SIGNAL_COMBO,
		Str.STR_SIGNAL_PBS,
		INVALID_STRING_ID
	};

	static struct {
		byte orientation;
		byte numtracks;
		byte platlength;
		boolean dragdrop;
	} _railstation;


	static void HandleStationPlacement(TileIndex start, TileIndex end);
	static void ShowBuildTrainDepotPicker();
	static void ShowBuildWaypointPicker();
	static void ShowStationBuilder();
	static void ShowSignalBuilder();

	void CcPlaySound1E(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) SndPlayTileFx(SND_20_SPLAT_2, tile);
	}

	static void GenericPlaceRail(TileIndex tile, int cmd)
	{
		DoCommandP(tile, _cur_railtype, cmd, CcPlaySound1E,
			_remove_button_clicked ?
			Cmd.CMD_REMOVE_SINGLE_RAIL | Cmd.CMD_MSG(Str.STR_1012_CAN_T_REMOVE_RAILROAD_TRACK) | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER :
			Cmd.CMD_BUILD_SINGLE_RAIL | Cmd.CMD_MSG(Str.STR_1011_CAN_T_BUILD_RAILROAD_TRACK) | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER
		);
	}

	static void PlaceRail_N(TileIndex tile)
	{
		int cmd = _tile_fract_coords.x > _tile_fract_coords.y ? 4 : 5;
		GenericPlaceRail(tile, cmd);
	}

	static void PlaceRail_NE(TileIndex tile)
	{
		VpStartPlaceSizing(tile, VPM_FIX_Y);
	}

	static void PlaceRail_E(TileIndex tile)
	{
		int cmd = _tile_fract_coords.x + _tile_fract_coords.y <= 15 ? 2 : 3;
		GenericPlaceRail(tile, cmd);
	}

	static void PlaceRail_NW(TileIndex tile)
	{
		VpStartPlaceSizing(tile, VPM_FIX_X);
	}

	static void PlaceRail_AutoRail(TileIndex tile)
	{
		VpStartPlaceSizing(tile, VPM_RAILDIRS);
	}

	static void PlaceExtraDepotRail(TileIndex tile, int extra)
	{
		byte b = tile.getMap().m5;

		if (BitOps.GB(b, 6, 2) != RAIL_TYPE_NORMAL >> 6) return;
		if (!(b & (extra >> 8))) return;

		DoCommandP(tile, _cur_railtype, extra & 0xFF, null, Cmd.CMD_BUILD_SINGLE_RAIL | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER);
	}

	static final int _place_depot_extra[12] = {
		0x604,		0x2102,		0x1202,		0x505,
		0x2400,		0x2801,		0x1800,		0x1401,
		0x2203,		0x904,		0x0A05,		0x1103,
	};


	void CcRailDepot(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			int dir = p2;

			SndPlayTileFx(SND_20_SPLAT_2, tile);
			ResetObjectToPlace();

			tile += TileOffsByDir(dir);

			if (tile.IsTileType( TileTypes.MP_RAILWAY)) {
				PlaceExtraDepotRail(tile, _place_depot_extra[dir]);
				PlaceExtraDepotRail(tile, _place_depot_extra[dir + 4]);
				PlaceExtraDepotRail(tile, _place_depot_extra[dir + 8]);
			}
		}
	}

	static void PlaceRail_Depot(TileIndex tile)
	{
		DoCommandP(tile, _cur_railtype, _build_depot_direction, CcRailDepot,
			Cmd.CMD_BUILD_TRAIN_DEPOT | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_100E_CAN_T_BUILD_TRAIN_DEPOT));
	}

	static void PlaceRail_Waypoint(TileIndex tile)
	{
		if (!_remove_button_clicked) {
			DoCommandP(tile, _cur_waypoint_type, 0, CcPlaySound1E, Cmd.CMD_BUILD_TRAIN_WAYPOINT | Cmd.CMD_MSG(Str.STR_CANT_BUILD_TRAIN_WAYPOINT));
		} else {
			DoCommandP(tile, 0, 0, CcPlaySound1E, Cmd.CMD_REMOVE_TRAIN_WAYPOINT | Cmd.CMD_MSG(Str.STR_CANT_REMOVE_TRAIN_WAYPOINT));
		}
	}

	void CcStation(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			SndPlayTileFx(SND_20_SPLAT_2, tile);
			ResetObjectToPlace();
		}
	}

	static void PlaceRail_Station(TileIndex tile)
	{
		if(_remove_button_clicked)
			DoCommandP(tile, 0, 0, CcPlaySound1E, Cmd.CMD_REMOVE_FROM_RAILROAD_STATION | Cmd.CMD_MSG(Str.STR_CANT_REMOVE_PART_OF_STATION));
		else if (_railstation.dragdrop) {
			VpStartPlaceSizing(tile, VPM_X_AND_Y_LIMITED);
			VpSetPlaceSizingLimit(Global._patches.station_spread);
		} else {
			// TODO: Custom station selector GUI. Now we just try using first custom station
			// (and fall back to normal stations if it isn't available).
			DoCommandP(tile, _railstation.orientation | (_railstation.numtracks<<8) | (_railstation.platlength<<16),_cur_railtype|1<<4, CcStation,
					Cmd.CMD_BUILD_RAILROAD_STATION | Cmd.CMD_NO_WATER | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_100F_CAN_T_BUILD_RAILROAD_STATION));
		}
	}

	static void GenericPlaceSignals(TileIndex tile)
	{
		int trackstat;
		int i;

		trackstat = (byte)GetTileTrackStatus(tile, TRANSPORT_RAIL);

		if ((trackstat & 0x30)) // N-S direction
			trackstat = (_tile_fract_coords.x <= _tile_fract_coords.y) ? 0x20 : 0x10;

		if ((trackstat & 0x0C)) // E-W direction
			trackstat = (_tile_fract_coords.x + _tile_fract_coords.y <= 15) ? 4 : 8;

		// Lookup the bit index
		i = 0;
		if (trackstat != 0) {
			for (; !(trackstat & 1); trackstat >>= 1) i++;
		}

		if (!_remove_button_clicked) {
			DoCommandP(tile, i + (_ctrl_pressed ? 8 : 0) +
			                 (!BitOps.HASBIT(_cur_signal_type, 0) != !_ctrl_pressed ? 16 : 0) +
			                 (_cur_presig_type << 5) ,
			                 0, CcPlaySound1E, Cmd.CMD_BUILD_SIGNALS | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_1010_CAN_T_BUILD_SIGNALS_HERE));
		} else {
			DoCommandP(tile, i, 0, CcPlaySound1E,
				Cmd.CMD_REMOVE_SIGNALS | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_1013_CAN_T_REMOVE_SIGNALS_FROM));
		}
	}

	static void PlaceRail_Bridge(TileIndex tile)
	{
		VpStartPlaceSizing(tile, VPM_X_OR_Y);
	}

	void CcBuildRailTunnel(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			SndPlayTileFx(SND_20_SPLAT_2, tile);
			ResetObjectToPlace();
		} else {
			SetRedErrorSquare(_build_tunnel_endtile);
		}
	}

	static void PlaceRail_Tunnel(TileIndex tile)
	{
		DoCommandP(tile, _cur_railtype, 0, CcBuildRailTunnel,
			Cmd.CMD_BUILD_TUNNEL | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_5016_CAN_T_BUILD_TUNNEL_HERE));
	}

	void PlaceProc_BuyLand(TileIndex tile)
	{
		DoCommandP(tile, 0, 0, CcPlaySound1E, Cmd.CMD_PURCHASE_LAND_AREA | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_5806_CAN_T_PURCHASE_THIS_LAND));
	}

	static void PlaceRail_ConvertRail(TileIndex tile)
	{
		VpStartPlaceSizing(tile, VPM_X_AND_Y | GUI_PlaceProc_ConvertRailArea);
	}

	static void PlaceRail_AutoSignals(TileIndex tile)
	{
		VpStartPlaceSizing(tile, VPM_SIGNALDIRS);
	}

	static void BuildRailClick_N(Window w)
	{
		HandlePlacePushButton(w, 4, GetRailTypeInfo(_cur_railtype).cursor.rail_ns, 1, PlaceRail_N);
	}

	static void BuildRailClick_NE(Window w)
	{
		HandlePlacePushButton(w, 5, GetRailTypeInfo(_cur_railtype).cursor.rail_swne, 1, PlaceRail_NE);
	}

	static void BuildRailClick_E(Window w)
	{
		HandlePlacePushButton(w, 6, GetRailTypeInfo(_cur_railtype).cursor.rail_ew, 1, PlaceRail_E);
	}

	static void BuildRailClick_NW(Window w)
	{
		HandlePlacePushButton(w, 7, GetRailTypeInfo(_cur_railtype).cursor.rail_nwse, 1, PlaceRail_NW);
	}

	static void BuildRailClick_AutoRail(Window w)
	{
		HandlePlacePushButton(w, 8, GetRailTypeInfo(_cur_railtype).cursor.autorail, VHM_RAIL, PlaceRail_AutoRail);
	}

	static void BuildRailClick_Demolish(Window w)
	{
		HandlePlacePushButton(w, 9, ANIMCURSOR_DEMOLISH, 1, PlaceProc_DemolishArea);
	}

	static void BuildRailClick_Depot(Window w)
	{
		if (HandlePlacePushButton(w, 10, GetRailTypeInfo(_cur_railtype).cursor.depot, 1, PlaceRail_Depot)) {
			ShowBuildTrainDepotPicker();
		}
	}

	static void BuildRailClick_Waypoint(Window w)
	{
		_waypoint_count = GetNumCustomStations(STAT_CLASS_WAYP);
		if (HandlePlacePushButton(w, 11, Sprite.SPR_CURSOR_WAYPOINT, 1, PlaceRail_Waypoint) &&
				_waypoint_count > 1) {
			ShowBuildWaypointPicker();
		}
	}

	static void BuildRailClick_Station(Window w)
	{
		if (HandlePlacePushButton(w, 12, Sprite.SPR_CURSOR_RAIL_STATION, 1, PlaceRail_Station)) ShowStationBuilder();
	}

	static void BuildRailClick_AutoSignals(Window w)
	{
		if (HandlePlacePushButton(w, 13, ANIMCURSOR_BUILDSIGNALS, VHM_RECT, PlaceRail_AutoSignals))
			ShowSignalBuilder();
	}

	static void BuildRailClick_Bridge(Window w)
	{
		HandlePlacePushButton(w, 14, Sprite.SPR_CURSOR_BRIDGE, 1, PlaceRail_Bridge);
	}

	static void BuildRailClick_Tunnel(Window w)
	{
		HandlePlacePushButton(w, 15, GetRailTypeInfo(_cur_railtype).cursor.tunnel, 3, PlaceRail_Tunnel);
	}

	static void BuildRailClick_Remove(Window w)
	{
		if (BitOps.HASBIT(w.disabled_state, 16)) return;
		SetWindowDirty(w);
		SndPlayFx(SND_15_BEEP);

		TOGGLEBIT(w.click_state, 16);
		_remove_button_clicked = BitOps.HASBIT(w.click_state, 16) != 0;
		SetSelectionRed(BitOps.HASBIT(w.click_state, 16) != 0);

		// handle station builder
		if (BitOps.HASBIT(w.click_state, 16)) {
			if (_remove_button_clicked) {
				SetTileSelectSize(1, 1);
			} else {
				BringWindowToFrontById(Window.WC_BUILD_STATION, 0);
			}
		}
	}

	static void BuildRailClick_Convert(Window w)
	{
		HandlePlacePushButton(w, 17, GetRailTypeInfo(_cur_railtype).cursor.convert, 1, PlaceRail_ConvertRail);
	}

	static void BuildRailClick_Landscaping(Window w)
	{
		ShowTerraformToolbar();
	}

	static void DoRailroadTrack(int mode)
	{
		DoCommandP(TileVirtXY(_thd.selstart.x, _thd.selstart.y), TileVirtXY(_thd.selend.x, _thd.selend.y), _cur_railtype | (mode << 4), null,
			_remove_button_clicked ?
			Cmd.CMD_REMOVE_RAILROAD_TRACK | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1012_CAN_T_REMOVE_RAILROAD_TRACK) :
			Cmd.CMD_BUILD_RAILROAD_TRACK  | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1011_CAN_T_BUILD_RAILROAD_TRACK)
		);
	}

	static void HandleAutodirPlacement()
	{
		TileHighlightData *thd = &_thd;
		int trackstat = thd.drawstyle & 0xF; // 0..5

		if (thd.drawstyle & HT_RAIL) { // one tile case
			GenericPlaceRail(TileVirtXY(thd.selend.x, thd.selend.y), trackstat);
			return;
		}

		DoRailroadTrack(trackstat);
	}

	static void HandleAutoSignalPlacement()
	{
		TileHighlightData *thd = &_thd;
		byte trackstat = thd.drawstyle & 0xF; // 0..5

		if (thd.drawstyle == HT_RECT) { // one tile case
			GenericPlaceSignals(TileVirtXY(thd.selend.x, thd.selend.y));
			return;
		}

		// Global._patches.drag_signals_density is given as a parameter such that each user in a network
		// game can specify his/her own signal density
		DoCommandP(
			TileVirtXY(thd.selstart.x, thd.selstart.y),
			TileVirtXY(thd.selend.x, thd.selend.y),
			(_ctrl_pressed ? 1 << 3 : 0) | (trackstat << 4) | (Global._patches.drag_signals_density << 24),
			CcPlaySound1E,
			_remove_button_clicked ?
				Cmd.CMD_REMOVE_SIGNAL_TRACK | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1013_CAN_T_REMOVE_SIGNALS_FROM) :
				Cmd.CMD_BUILD_SIGNAL_TRACK  | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1010_CAN_T_BUILD_SIGNALS_HERE)
		);
		(!BitOps.HASBIT(_cur_signal_type, 0) != !_ctrl_pressed ? 1 << 3 : 0) | 
		(trackstat << 4) |
		(Global._patches.drag_signals_density << 24) | 
		(_cur_autosig_compl ? 2 : 0),
		CcPlaySound1E,
		(_remove_button_clicked ? Cmd.CMD_REMOVE_SIGNAL_TRACK | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | 
		Cmd.CMD_MSG(Str.STR_1013_CAN_T_REMOVE_SIGNALS_FROM) :
	        Cmd.CMD_BUILD_SIGNAL_TRACK  | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1010_CAN_T_BUILD_SIGNALS_HERE) );
	}


	typedef void OnButtonClick(Window w);

	static OnButtonClick * final _build_railroad_button_proc[] = {
		BuildRailClick_N,
		BuildRailClick_NE,
		BuildRailClick_E,
		BuildRailClick_NW,
		BuildRailClick_AutoRail,
		BuildRailClick_Demolish,
		BuildRailClick_Depot,
		BuildRailClick_Waypoint,
		BuildRailClick_Station,
		BuildRailClick_AutoSignals,
		BuildRailClick_Bridge,
		BuildRailClick_Tunnel,
		BuildRailClick_Remove,
		BuildRailClick_Convert,
		BuildRailClick_Landscaping,
	};

	static final int _rail_keycodes[] = {
		'1',
		'2',
		'3',
		'4',
		'5',
		'6',
		'7', // depot
		'8', // waypoint
		'9', // station
		'S', // signals
		'B', // bridge
		'T', // tunnel
		'R', // remove
		'C', // convert rail
		'L', // landscaping
	};


	static void BuildRailToolbWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT:
			w.disabled_state &= ~(1 << 16);
			if (!(w.click_state & ((1<<4)|(1<<5)|(1<<6)|(1<<7)|(1<<8)|(1<<11)|(1<<12)|(1<<13)))) {
				w.disabled_state |= (1 << 16);
				w.click_state &= ~(1<<16);
			}
			DrawWindowWidgets(w);
			break;

		case WindowEvents.WE_CLICK:
			if (e.click.widget >= 4) {
				_remove_button_clicked = false;
				_build_railroad_button_proc[e.click.widget - 4](w);
			}
		break;

		case WindowEvents.WE_KEYPRESS: {
			int i;

			for (i = 0; i != lengthof(_rail_keycodes); i++) {
				if (e.keypress.keycode == _rail_keycodes[i]) {
					e.keypress.cont = false;
					_remove_button_clicked = false;
					_build_railroad_button_proc[i](w);
					break;
				}
			}
			MarkTileDirty(_thd.pos.x, _thd.pos.y); // redraw tile selection
			break;
		}

		case WindowEvents.WE_PLACE_OBJ:
			_place_proc(e.place.tile);
			return;

		case WindowEvents.WE_PLACE_DRAG: {
			VpSelectTilesWithMethod(e.place.pt.x, e.place.pt.y, e.place.userdata & 0xF);
			return;
		}

		case WindowEvents.WE_PLACE_MOUSEUP:
			if (e.click.pt.x != -1) {
				TileIndex start_tile = e.place.starttile;
				TileIndex end_tile = e.place.tile;

				if (e.place.userdata == VPM_X_OR_Y) {
					ResetObjectToPlace();
					ShowBuildBridgeWindow(start_tile, end_tile, _cur_railtype);
				} else if (e.place.userdata == VPM_RAILDIRS) {
					boolean old = _remove_button_clicked;
					if (_ctrl_pressed) _remove_button_clicked = true;
					HandleAutodirPlacement();
					_remove_button_clicked = old;
				} else if (e.place.userdata == VPM_SIGNALDIRS) {
					HandleAutoSignalPlacement();
				} else if ((e.place.userdata & 0xF) == VPM_X_AND_Y) {
					if (GUIPlaceProcDragXY(e)) break;

					if ((e.place.userdata >> 4) == GUI_PlaceProc_ConvertRailArea >> 4)
						DoCommandP(end_tile, start_tile, _cur_railtype, CcPlaySound10, Cmd.CMD_CONVERT_RAIL | Cmd.CMD_MSG(Str.STR_CANT_CONVERT_RAIL));
				} else if (e.place.userdata == VPM_X_AND_Y_LIMITED) {
					HandleStationPlacement(start_tile, end_tile);
				} else
					DoRailroadTrack(e.place.userdata & 1);
			}
			break;

		case WindowEvents.WE_ABORT_PLACE_OBJ:
			UnclickWindowButtons(w);
			SetWindowDirty(w);

			w = FindWindowById(Window.WC_BUILD_STATION, 0);
			if (w != null) WP(w,def_d).close = true;
			w = FindWindowById(Window.WC_BUILD_DEPOT, 0);
			if (w != null) WP(w,def_d).close = true;
			w = FindWindowById(Window.WC_BUILD_SIGNALS, 0);
			if (w != null) WP(w,def_d).close=true;

			break;

		case WindowEvents.WE_PLACE_PRESIZE: {
			TileIndex tile = e.place.tile;

			DoCommandByTile(tile, 0, 0, Cmd.DC_AUTO, Cmd.CMD_BUILD_TUNNEL);
			VpSetPresizeRange(tile, _build_tunnel_endtile == 0 ? tile : _build_tunnel_endtile);
		} break;

		case WindowEvents.WE_DESTROY:
			if (Global._patches.link_terraform_toolbar) Window.DeleteWindowById(Window.WC_SCEN_LAND_GEN, 0);
			break;
		}
	}


	static final Widget _build_rail_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   359,     0,    13, Str.STR_100A_RAILROAD_CONSTRUCTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7,   360,   371,     0,    13, 0x0,     Str.STR_STICKY_BUTTON},

	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   110,   113,    14,    35, 0x0,			Str.STR_NULL},

	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    0,     21,    14,    35, 0x4E3,		Str.STR_1018_BUILD_RAILROAD_TRACK},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    22,    43,    14,    35, 0x4E4,		Str.STR_1018_BUILD_RAILROAD_TRACK},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    44,    65,    14,    35, 0x4E5,		Str.STR_1018_BUILD_RAILROAD_TRACK},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    66,    87,    14,    35, 0x4E6,		Str.STR_1018_BUILD_RAILROAD_TRACK},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    88,   109,    14,    35, Sprite.SPR_IMG_AUTORAIL, Str.STR_BUILD_AUTORAIL_TIP},

	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   114,   135,    14,    35, 0x2BF,		Str.STR_018D_DEMOLISH_BUILDINGS_ETC},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   136,   157,    14,    35, 0x50E,		Str.STR_1019_BUILD_TRAIN_DEPOT_FOR_BUILDING},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   158,   179,    14,    35, Sprite.SPR_IMG_WAYPOINT, Str.STR_CONVERT_RAIL_TO_WAYPOINT_TIP},

	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   180,   221,    14,    35, 0x512,		Str.STR_101A_BUILD_RAILROAD_STATION},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   222,   243,    14,    35, 0x50B,		Str.STR_101B_BUILD_RAILROAD_SIGNALS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   244,   285,    14,    35, 0xA22,		Str.STR_101C_BUILD_RAILROAD_BRIDGE},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   286,   305,    14,    35, Sprite.SPR_IMG_TUNNEL_RAIL, Str.STR_101D_BUILD_RAILROAD_TUNNEL},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   306,   327,    14,    35, 0x2CA,		Str.STR_101E_TOGGLE_BUILD_REMOVE_FOR},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   328,   349,    14,    35, Sprite.SPR_IMG_CONVERT_RAIL, Str.STR_CONVERT_RAIL_TIP},

	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   350,   371,    14,    35, Sprite.SPR_IMG_LANDSCAPING,	Str.STR_LANDSCAPING_TOOLBAR_TIP},

	{   WIDGETS_END},
	};

	static final WindowDesc _build_rail_desc = {
		640-372, 22, 372, 36,
		Window.WC_BUILD_TOOLBAR,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
		_build_rail_widgets,
		BuildRailToolbWndProc
	};

	/** Enum referring to the widgets of the build rail toolbar
	 */
	typedef enum {
		RTW_CAPTION = 1,
		RTW_BUILD_NS = 4,
		RTW_BUILD_X = 5,
		RTW_BUILD_EW = 6,
		RTW_BUILD_Y = 7,
		RTW_AUTORAIL = 8,
		RTW_BUILD_DEPOT = 10,
		RTW_BUILD_TUNNEL = 15,
		RTW_CONVERT_RAIL = 17
	} RailToolbarWidgets;

	/** Configures the rail toolbar for railtype given
	 * @param railtype the railtype to display
	 * @param w the window to modify
	 */
	static void SetupRailToolbar(RailType railtype, Window  w)
	{
		final RailtypeInfo *rti = GetRailTypeInfo(railtype);

		assert(railtype < RAILTYPE_END);
		w.widget[RTW_CAPTION].unkA = rti.strings.toolbar_caption;
		w.widget[RTW_BUILD_NS].unkA = rti.gui_sprites.build_ns_rail;
		w.widget[RTW_BUILD_X].unkA = rti.gui_sprites.build_x_rail;
		w.widget[RTW_BUILD_EW].unkA = rti.gui_sprites.build_ew_rail;
		w.widget[RTW_BUILD_Y].unkA = rti.gui_sprites.build_y_rail;
		w.widget[RTW_AUTORAIL].unkA = rti.gui_sprites.auto_rail;
		w.widget[RTW_BUILD_DEPOT].unkA = rti.gui_sprites.build_depot;
		w.widget[RTW_CONVERT_RAIL].unkA = rti.gui_sprites.convert_rail;
		w.widget[RTW_BUILD_TUNNEL].unkA = rti.gui_sprites.build_tunnel;
	}

	void ShowBuildRailToolbar(RailType railtype, int button)
	{
		Window w;

		if (Global._current_player == Owner.OWNER_SPECTATOR) return;

		// don't recreate the window if we're clicking on a button and the window exists.
		if (button < 0 || !(w = FindWindowById(Window.WC_BUILD_TOOLBAR, 0)) || w.wndproc != BuildRailToolbWndProc) {
			Window.DeleteWindowById(Window.WC_BUILD_TOOLBAR, 0);
			_cur_railtype = railtype;
			w = AllocateWindowDesc(&_build_rail_desc);
			SetupRailToolbar(railtype, w);
		}

		_remove_button_clicked = false;
		if (w != null && button >= 0) _build_railroad_button_proc[button](w);
		if (Global._patches.link_terraform_toolbar) ShowTerraformToolbar();
	}

	/* TODO: For custom stations, respect their allowed platforms/lengths bitmasks!
	 * --pasky */

	static void HandleStationPlacement(TileIndex start, TileIndex end)
	{
		int sx = TileX(start);
		int sy = TileY(start);
		int ex = TileX(end);
		int ey = TileY(end);
		int w,h;

		if (sx > ex) intswap(sx,ex);
		if (sy > ey) intswap(sy,ey);
		w = ex - sx + 1;
		h = ey - sy + 1;
		if (!_railstation.orientation) intswap(w,h);

		// TODO: Custom station selector GUI. Now we just try using first custom station
		// (and fall back to normal stations if it isn't available).
		DoCommandP(TileXY(sx, sy), _railstation.orientation | (w << 8) | (h << 16), _cur_railtype | 1 << 4, CcStation,
			Cmd.CMD_BUILD_RAILROAD_STATION | Cmd.CMD_NO_WATER | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_100F_CAN_T_BUILD_RAILROAD_STATION));
	}

	static void StationBuildWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT: {
			int rad;
			int bits;

			if (WP(w,def_d).close) return;

			bits = (1<<3) << ( _railstation.orientation);
			if (_railstation.dragdrop) {
				bits |= (1<<19);
			} else {
				bits |= (1<<(5-1)) << (_railstation.numtracks);
				bits |= (1<<(12-1)) << (_railstation.platlength);
			}
			bits |= (1<<20) << (_station_show_coverage);
			w.click_state = bits;

			if (_railstation.dragdrop) {
				SetTileSelectSize(1, 1);
			} else {
				int x = _railstation.numtracks;
				int y = _railstation.platlength;
				if (_railstation.orientation == 0) intswap(x,y);
				if(!_remove_button_clicked)
					SetTileSelectSize(x, y);
			}

			rad = (Global._patches.modified_catchment) ? CA_TRAIN : 4;

			if (_station_show_coverage)
				SetTileSelectBigSize(-rad, -rad, 2 * rad, 2 * rad);

			/* Update buttons for correct spread value */
			w.disabled_state = 0;
			for (bits = Global._patches.station_spread; bits < 7; bits++) {
				SETBIT(w.disabled_state, bits + 5);
				SETBIT(w.disabled_state, bits + 12);
			}

			DrawWindowWidgets(w);

			StationPickerGfx.DrawSprite(39, 42, _cur_railtype, 2);
			StationPickerGfx.DrawSprite(107, 42, _cur_railtype, 3);

			DrawStringCentered(74, 15, Str.STR_3002_ORIENTATION, 0);
			DrawStringCentered(74, 76, Str.STR_3003_NUMBER_OF_TRACKS, 0);
			DrawStringCentered(74, 101, Str.STR_3004_PLATFORM_LENGTH, 0);
			DrawStringCentered(74, 141, Str.STR_3066_COVERAGE_AREA_HIGHLIGHT, 0);

			DrawStationCoverageAreaText(2, 166, (int)-1, rad);
		} break;

		case WindowEvents.WE_CLICK: {
			switch (e.click.widget) {
			case 3:
			case 4:
				_railstation.orientation = e.click.widget - 3;
				SndPlayFx(SND_15_BEEP);
				SetWindowDirty(w);
				break;

			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				_railstation.numtracks = (e.click.widget - 5) + 1;
				_railstation.dragdrop = false;
				SndPlayFx(SND_15_BEEP);
				SetWindowDirty(w);
				break;

			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
				_railstation.platlength = (e.click.widget - 12) + 1;
				_railstation.dragdrop = false;
				SndPlayFx(SND_15_BEEP);
				SetWindowDirty(w);
				break;

			case 19:
				_railstation.dragdrop ^= true;
				SndPlayFx(SND_15_BEEP);
				SetWindowDirty(w);
				break;

			case 20:
			case 21:
				_station_show_coverage = e.click.widget - 20;
				SndPlayFx(SND_15_BEEP);
				SetWindowDirty(w);
				break;
			}
		} break;

		case WindowEvents.WE_MOUSELOOP:
			if (WP(w,def_d).close) {
				DeleteWindow(w);
				return;
			}
			CheckRedrawStationCoverage(w);
			break;

		case WindowEvents.WE_DESTROY:
			if (!WP(w,def_d).close) ResetObjectToPlace();
			break;
		}
	}

	static final Widget _station_builder_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,		Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   147,     0,    13, Str.STR_3000_RAIL_STATION_SELECTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   147,    14,   199, 0x0,					Str.STR_NULL},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     7,    72,    26,    73, 0x0,					Str.STR_304E_SELEAcceptedCargo.CT_RAILROAD_STATION},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    75,   140,    26,    73, 0x0,					Str.STR_304E_SELEAcceptedCargo.CT_RAILROAD_STATION},

	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    22,    36,    87,    98, Str.STR_00CB_1,	Str.STR_304F_SELEAcceptedCargo.CT_NUMBER_OF_PLATFORMS},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    37,    51,    87,    98, Str.STR_00CC_2,	Str.STR_304F_SELEAcceptedCargo.CT_NUMBER_OF_PLATFORMS},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    52,    66,    87,    98, Str.STR_00CD_3,	Str.STR_304F_SELEAcceptedCargo.CT_NUMBER_OF_PLATFORMS},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    67,    81,    87,    98, Str.STR_00CE_4,	Str.STR_304F_SELEAcceptedCargo.CT_NUMBER_OF_PLATFORMS},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    82,    96,    87,    98, Str.STR_00CF_5,	Str.STR_304F_SELEAcceptedCargo.CT_NUMBER_OF_PLATFORMS},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    97,   111,    87,    98, Str.STR_0335_6,	Str.STR_304F_SELEAcceptedCargo.CT_NUMBER_OF_PLATFORMS},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   112,   126,    87,    98, Str.STR_0336_7,	Str.STR_304F_SELEAcceptedCargo.CT_NUMBER_OF_PLATFORMS},

	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    22,    36,   112,   123, Str.STR_00CB_1,	Str.STR_3050_SELEAcceptedCargo.CT_LENGTH_OF_RAILROAD},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    37,    51,   112,   123, Str.STR_00CC_2,	Str.STR_3050_SELEAcceptedCargo.CT_LENGTH_OF_RAILROAD},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    52,    66,   112,   123, Str.STR_00CD_3,	Str.STR_3050_SELEAcceptedCargo.CT_LENGTH_OF_RAILROAD},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    67,    81,   112,   123, Str.STR_00CE_4,	Str.STR_3050_SELEAcceptedCargo.CT_LENGTH_OF_RAILROAD},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    82,    96,   112,   123, Str.STR_00CF_5,	Str.STR_3050_SELEAcceptedCargo.CT_LENGTH_OF_RAILROAD},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    97,   111,   112,   123, Str.STR_0335_6,	Str.STR_3050_SELEAcceptedCargo.CT_LENGTH_OF_RAILROAD},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   112,   126,   112,   123, Str.STR_0336_7,	Str.STR_3050_SELEAcceptedCargo.CT_LENGTH_OF_RAILROAD},

	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    37,   111,   126,   137, Str.STR_DRAG_DROP, Str.STR_STATION_DRAG_DROP},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    14,    73,   152,   163, Str.STR_02DB_OFF, Str.STR_3065_DON_T_HIGHLIGHT_COVERAGE},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    74,   133,   152,   163, Str.STR_02DA_ON, Str.STR_3064_HIGHLIGHT_COVERAGE_AREA},
	{   WIDGETS_END},
	};

	static final WindowDesc _station_builder_desc = {
		-1, -1, 148, 200,
		Window.WC_BUILD_STATION,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_station_builder_widgets,
		StationBuildWndProc
	};

	static void ShowStationBuilder()
	{
		AllocateWindowDesc(&_station_builder_desc);
	}

	static void BuildTrainDepotWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WindowEvents.WE_PAINT: {
			RailType r;

			w.click_state = (1 << 3) << _build_depot_direction;
			DrawWindowWidgets(w);

			r = _cur_railtype;
			DrawTrainDepotSprite(70, 17, 0, r);
			DrawTrainDepotSprite(70, 69, 1, r);
			DrawTrainDepotSprite( 2, 69, 2, r);
			DrawTrainDepotSprite( 2, 17, 3, r);
			break;
			}

		case WindowEvents.WE_CLICK:
			switch (e.click.widget) {
				case 3:
				case 4:
				case 5:
				case 6:
					_build_depot_direction = e.click.widget - 3;
					SndPlayFx(SND_15_BEEP);
					SetWindowDirty(w);
					break;
			}
			break;

		case WindowEvents.WE_MOUSELOOP:
			if (WP(w,def_d).close) DeleteWindow(w);
			return;

		case WindowEvents.WE_DESTROY:
			if (!WP(w,def_d).close) ResetObjectToPlace();
			break;
		}
	}

	static final Widget _build_depot_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   139,     0,    13, Str.STR_1014_TRAIN_DEPOT_ORIENTATION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   139,    14,   121, 0x0,			Str.STR_NULL},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    17,    66, 0x0,			Str.STR_1020_SELEAcceptedCargo.CT_RAILROAD_DEPOT_ORIENTATIO},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    69,   118, 0x0,			Str.STR_1020_SELEAcceptedCargo.CT_RAILROAD_DEPOT_ORIENTATIO},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    69,   118, 0x0,			Str.STR_1020_SELEAcceptedCargo.CT_RAILROAD_DEPOT_ORIENTATIO},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    17,    66, 0x0,			Str.STR_1020_SELEAcceptedCargo.CT_RAILROAD_DEPOT_ORIENTATIO},
	{   WIDGETS_END},
	};

	static final WindowDesc _build_depot_desc = {
		-1,-1, 140, 122,
		Window.WC_BUILD_DEPOT,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_build_depot_widgets,
		BuildTrainDepotWndProc
	};

	static void ShowBuildTrainDepotPicker()
	{
		AllocateWindowDesc(&_build_depot_desc);
	}


	static void BuildWaypointWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT: {
			int i;

			w.click_state = (1 << 3) << (_cur_waypoint_type - w.hscroll.pos);
			DrawWindowWidgets(w);

			for (i = 0; i < 5; i++) {
				if (w.hscroll.pos + i < _waypoint_count) {
					DrawWaypointSprite(2 + i * 68, 25, w.hscroll.pos + i, _cur_railtype);
				}
			}
			break;
		}
		case WindowEvents.WE_CLICK: {
			switch (e.click.widget) {
			case 3: case 4: case 5: case 6: case 7:
				_cur_waypoint_type = e.click.widget - 3 + w.hscroll.pos;
				SndPlayFx(SND_15_BEEP);
				SetWindowDirty(w);
				break;
			}
			break;
		}

		case WindowEvents.WE_MOUSELOOP:
			if (WP(w,def_d).close) DeleteWindow(w);
			break;

		case WindowEvents.WE_DESTROY:
			if (!WP(w,def_d).close) ResetObjectToPlace();
			break;
		}
	}

	static final Widget _build_waypoint_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   343,     0,    13, Str.STR_WAYPOINT,Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   343,    14,    91, 0x0, 0},

	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     3,    68,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    71,   136,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   139,   204,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   207,   272,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   275,   340,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP},

	{ Window.WWT_HSCROLLBAR,   Window.RESIZE_NONE,    7,     1,   343,     80,    91, 0x0, Str.STR_0190_SCROLL_BAR_SCROLLS_LIST},
	{    WIDGETS_END},
	};

	static final WindowDesc _build_waypoint_desc = {
		-1,-1, 344, 92,
		Window.WC_BUILD_DEPOT,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_build_waypoint_widgets,
		BuildWaypointWndProc
	};

	static void ShowBuildWaypointPicker()
	{
		Window w = AllocateWindowDesc(&_build_waypoint_desc);
		w.hscroll.cap = 5;
		w.hscroll.count = _waypoint_count;
	}

	static void BuildSignalWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WindowEvents.WE_PAINT: {
			/* XXX TODO: dont always hide the buttons when more than 2 signal types are available */
			w.hidden_state = (1 << 3) | (1 << 6);

			/* XXX TODO: take into account the scroll position for setting the click state */
			w.click_state = ((1 << 4) << _cur_signal_type) | (_cur_autosig_compl ? 1 << 9 : 0);

			Global.SetDParam(10, _presig_types_dropdown[_cur_presig_type]);
			DrawWindowWidgets(w);

			// Draw the string for current signal type
			DrawStringCentered(69, 49, Str.STR_SIGNAL_TYPE_STANDARD + _cur_signal_type, 0);

			// Draw the strings for drag density
			DrawStringCentered(69, 60, Str.STR_SIGNAL_DENSITY_DESC, 0);
			Global.SetDParam(0, Global._patches.drag_signals_density);
			DrawString( 50, 71, Str.STR_SIGNAL_DENSITY_TILES , 0);

			// Draw the '<' and '>' characters for the decrease/increase buttons
			DrawStringCentered(30, 72, Str.STR_6819, 0);
			DrawStringCentered(40, 72, Str.STR_681A, 0);

			break;
			}
		case WindowEvents.WE_CLICK: {
			switch(e.click.widget) {
				case 3: case 6: // scroll signal types
					/* XXX TODO: implement scrolling */
					break;
				case 4: case 5: // select signal type
					/* XXX TODO: take into account the scroll position for changing selected type */
					_cur_signal_type = e.click.widget - 4;
					SndPlayFx(SND_15_BEEP);
					SetWindowDirty(w);
					break;
				case 7: // decrease drag density
					if (Global._patches.drag_signals_density > 1) {
						Global._patches.drag_signals_density--;
						SndPlayFx(SND_15_BEEP);
						SetWindowDirty(w);
					};
					break;
				case 8: // increase drag density
					if (Global._patches.drag_signals_density < 20) {
						Global._patches.drag_signals_density++;
						SndPlayFx(SND_15_BEEP);
						SetWindowDirty(w);
					};
					break;
				case 9: // autosignal mode toggle button
					_cur_autosig_compl ^= 1;
					SndPlayFx(SND_15_BEEP);
					SetWindowDirty(w);
					break;
				case 10: case 11: // presignal-type dropdown list
					ShowDropDownMenu(w, _presig_types_dropdown, _cur_presig_type, 11, 0, 0);
					break;
			}
			break;
		case WindowEvents.WE_DROPDOWN_SELECT: // change presignal type
			_cur_presig_type = e.dropdown.index;
			SetWindowDirty(w);
			break;
		}

		case WindowEvents.WE_MOUSELOOP:
			if (WP(w,def_d).close)
				DeleteWindow(w);
			return;

		case WindowEvents.WE_DESTROY:
			if (!WP(w,def_d).close)
				ResetObjectToPlace();
			break;
		}
	}

	static final Widget _build_signal_widgets[] = {
	{   Window.WWT_CLOSEBOX, Window.RESIZE_NONE,    7,    0,   10,    0,   13, Str.STR_00C5                 , Str.STR_018B_CLOSE_WINDOW},
	{   Window.WWT_CAPTION,  Window.RESIZE_NONE,    7,   11,  139,    0,   13, Str.STR_SIGNAL_SELECTION     , Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,    0,  139,   14,  114, 0x0                      , Str.STR_NULL},
	{   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,   22,   30,   29,   39, Sprite.SPR_ARROW_LEFT           , Str.STR_SIGNAL_TYPE_TIP},
	{   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,   43,   64,   24,   45, 0x50B                    , Str.STR_SIGNAL_TYPE_TIP},
	{   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,   75,   96,   24,   45, Sprite.SPR_SEMA                 , Str.STR_SIGNAL_TYPE_TIP},
	{   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,  109,  117,   29,   39, Sprite.SPR_ARROW_RIGHT          , Str.STR_SIGNAL_TYPE_TIP},
	{   Window.WWT_IMGBTN,   Window.RESIZE_NONE,    3,   25,   34,   72,   80, 0x0                      , Str.STR_SIGNAL_DENSITY_TIP},
	{   Window.WWT_IMGBTN,   Window.RESIZE_NONE,    3,   35,   44,   72,   80, 0x0                      , Str.STR_SIGNAL_DENSITY_TIP},
	{   Window.WWT_TEXTBTN,  Window.RESIZE_NONE,    7,   20,  119,   84,   95, Str.STR_SIGNAL_COMPLETION    , Str.STR_SIGNAL_COMPLETION_TIP},
	{   Window.WWT_6,        Window.RESIZE_NONE,    7,   10,  129,   99,  110, Str.STR_SIGNAL_PRESIG_COMBO  , Str.STR_SIGNAL_PRESIG_TIP},
	{   Window.WWT_CLOSEBOX, Window.RESIZE_NONE,    7,  118,  128,  100,  109, Str.STR_0225                 , Str.STR_SIGNAL_PRESIG_TIP},
	{   WIDGETS_END},
	};

	static final WindowDesc _build_signal_desc = {
		-1,-1, 140, 115,
		Window.WC_BUILD_SIGNALS,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_build_signal_widgets,
		BuildSignalWndProc
	};

	static void ShowSignalBuilder()
	{
		_cur_presig_type = 0;
		AllocateWindowDesc(&_build_signal_desc);
	}



	void InitializeRailGui()
	{
		_build_depot_direction = 3;
		_railstation.numtracks = 1;
		_railstation.platlength = 1;
		_railstation.dragdrop = true;
		_cur_signal_type = 0;
		_cur_presig_type = 0;
		_cur_autosig_compl = false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
