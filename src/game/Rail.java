package game;

import java.util.function.Consumer;
import game.util.BitOps;
import game.util.Sprites;
import game.ai.Ai;
import game.struct.FindLengthOfTunnelResult;
import game.tables.RailTables;

public class Rail extends RailTables {

	public static final int NUM_SSD_ENTRY = RailtypeInfo.NUM_SSD_ENTRY;
	public static final int NUM_SSD_STACK = RailtypeInfo.NUM_SSD_STACK;// max amount of blocks to check recursively

	private static final TileHighlightData _thd = new TileHighlightData();

	/** This struct contains all the info that is needed to draw and construct tracks.
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
	static  /*TrackdirBits*/ int  TrackdirToTrackdirBits(/*Trackdir*/ int trackdir) 
	{ 
		return 1 << trackdir; 
	}


	/**
	 * Functions to map tracks to the corresponding bits in the signal
	 * presence/status bytes in the map. You should not use these directly, but
	 * wrapper functions below instead. XXX: Which are these?
	 */

	/**
	 * Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction along with the trackdir.
	 */
	//extern final byte _signal_along_trackdir[TRACKDIR_END];
	static  byte SignalAlongTrackdir(/*Trackdir*/ int trackdir) {return (byte) _signal_along_trackdir[trackdir];}

	/**
	 * Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction against the trackdir.
	 */
	static  byte SignalAgainstTrackdir(/*Trackdir*/ int trackdir) {
		//extern final byte _signal_against_trackdir[TRACKDIR_END];
		return (byte) _signal_against_trackdir[trackdir];
	}

	/**
	 * Maps a Track to the bits that store the status of the two signals that can
	 * be present on the given track.
	 */
	static  byte SignalOnTrack(/* Track */ int  track) {
		//extern final byte _signal_on_track[TRACK_END];
		return (byte) _signal_on_track[track];
	}

	/*
	 * Some functions to query rail tiles
	 */









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
	static  /*Trackdir*/ int ReverseTrackdir(/*Trackdir*/ int trackdir) {
		//extern final Trackdir _reverse_trackdir[TRACKDIR_END];
		return _reverse_trackdir[trackdir];
	}


	/**
	 * Returns the Track that a given Trackdir represents
	 */
	static  /* Track */ int  TrackdirToTrack(/*Trackdir*/ int trackdir) { return (trackdir & 0x7); }



	/**
	 * Discards all directional information from the given TrackdirBits. Any
	 * Track which is present in either direction will be present in the result.
	 */
	static  /*TrackBits*/ int  TrackdirBitsToTrackBits(/*TrackdirBits*/ int  bits) { return bits | (bits >> 8); }

	/**
	 * Maps a trackdir to the trackdir that you will end up on if you go straight
	 * ahead. This will be the same trackdir for diagonal trackdirs, but a
	 * different (alternating) one for straight trackdirs
	 */
	static  /*Trackdir*/ int NextTrackdir(/*Trackdir*/ int trackdir) {
		//extern final Trackdir _next_trackdir[TRACKDIR_END];
		return _next_trackdir[trackdir];
	}


	/**
	 * Maps a trackdir to the (4-way) direction the tile is exited when following
	 * that trackdir.
	 */
	static  /*DiagDirection*/ int  TrackdirToExitdir(/*Trackdir*/ int trackdir) {
		//extern final DiagDirection _trackdir_to_exitdir[TRACKDIR_END];
		return _trackdir_to_exitdir[trackdir];
	}

	/**
	 * Maps a track and an (4-way) dir to the trackdir that represents the track
	 * with the exit in the given direction.
	 */
	static  /*Trackdir*/ int TrackExitdirToTrackdir(/* Track */ int  track, /*DiagDirection*/ int  diagdir) {
		//extern final Trackdir _track_exitdir_to_trackdir[TRACK_END][DIAGDIR_END];
		return _track_exitdir_to_trackdir[track][diagdir];
	}

	/**
	 * Maps a track and an (4-way) dir to the trackdir that represents the track
	 * with the exit in the given direction.
	 */
	static  /*Trackdir*/ int TrackEnterdirToTrackdir(/* Track */ int  track, /*DiagDirection*/ int  diagdir) {
		//extern final Trackdir _track_enterdir_to_trackdir[TRACK_END][DIAGDIR_END];
		return _track_enterdir_to_trackdir[track][diagdir];
	}

	/**
	 * Maps a track and a full (8-way) direction to the trackdir that represents
	 * the track running in the given direction.
	 */
	static  /*Trackdir*/ int TrackDirectionToTrackdir(/* Track */ int  track, /*Direction*/ int dir) {
		//extern final Trackdir _track_direction_to_trackdir[TRACK_END][DIR_END];
		return _track_direction_to_trackdir[track][dir];
	}



	//extern final TrackdirBits _exitdir_reaches_trackdirs[DIAGDIR_END];

	/**
	 * Returns all trackdirs that can be reached when entering a tile from a given
	 * (diagonal) direction. This will obviously include 90 degree turns, since no
	 * information is available about the exact angle of entering */
	static  /*TrackdirBits*/ int  DiagdirReachesTrackdirs(/*DiagDirection*/ int  diagdir) { return _exitdir_reaches_trackdirs[diagdir]; }

	/**
	 * Returns all tracks that can be reached when entering a tile from a given
	 * (diagonal) direction. This will obviously include 90 degree turns, since no
	 * information is available about the exact angle of entering */
	static  /*TrackBits*/ int  DiagdirReachesTracks(/*DiagDirection*/ int  diagdir) { return TrackdirBitsToTrackBits(DiagdirReachesTrackdirs(diagdir)); }

	/**
	 * Maps a trackdir to the trackdirs that can be reached from it (ie, when
	 * entering the next tile. This will include 90 degree turns!
	 */
	static  /*TrackdirBits*/ int  TrackdirReachesTrackdirs(/*Trackdir*/ int trackdir) { return _exitdir_reaches_trackdirs[TrackdirToExitdir(trackdir)]; }
	/* Note that there is no direct table for this function (there used to be),
	 * but it uses two simpeler tables to achieve the result */


	/**
	 * Maps a trackdir to all trackdirs that make 90 deg turns with it.
	 */
	static  /*TrackdirBits*/ int  TrackdirCrossesTrackdirs(/*Trackdir*/ int trackdir) {
		//extern final TrackdirBits _track_crosses_trackdirs[TRACKDIR_END];
		return _track_crosses_trackdirs[TrackdirToTrack(trackdir)];
	}

	/**
	 * Maps a (4-way) direction to the reverse.
	 */
	static  /*DiagDirection*/ int  ReverseDiagdir(/*DiagDirection*/ int  diagdir) {
		//extern final DiagDirection _reverse_diagdir[DIAGDIR_END];
		return _reverse_diagdir[diagdir];
	}

	/**
	 * Maps a (8-way) direction to a (4-way) DiagDirection
	 */
	static  /*DiagDirection*/ int  DirToDiagdir(/*Direction*/ int dir) {
		assert(dir < TileIndex.DIR_END);
		return (dir >> 1);
	}


	/* Checks if a given Trackdir is diagonal. */
	static  boolean IsDiagonalTrackdir(/*Trackdir*/ int trackdir) { return IsDiagonalTrack(TrackdirToTrack(trackdir)); }

	/*
	 * Functions quering signals on tiles.
	 */


	/**
	 * Checks for the presence of signals along the given trackdir on the given
	 * rail tile.
	 *
	 * Along meaning if you are currently driving on the given trackdir, this is
	 * the signal that is facing us (for which we stop when it's red).
	 */
	static  boolean HasSignalOnTrackdir(TileIndex tile, /*Trackdir*/ int trackdir)
	{
		assert (IsValidTrackdir(trackdir));
		return (GetRailTileType(tile) == RAIL_TYPE_SIGNALS) && 
				0 != (tile.getMap().m3 & SignalAlongTrackdir(trackdir));
	}


	/**
	 * Gets the type of signal on a given track on a given rail tile with signals.
	 *
	 * Note that currently, the track argument is not used, since
	 * signal types cannot be mixed. This function is trying to be
	 * future-compatible, though.
	 */
	static  /*SignalType*/ int GetSignalType(TileIndex tile, /* Track */ int  track)
	{
		assert(IsValidTrack(track));
		assert(GetRailTileType(tile) == RAIL_TYPE_SIGNALS);
		return (/*SignalType*/ int)(tile.getMap().m4 & SIGTYPE_MASK);
	}


	/**
	 * Return the rail type of tile, or INVALID_RAILTYPE if this is no rail tile.
	 * Note that there is no check if the given trackdir is actually present on
	 * the tile!
	 * The given trackdir is used when there are (could be) multiple rail types on
	 * one tile.
	 */
	//RailType GetTileRailType(TileIndex tile, Trackdir trackdir);

	/**
	 * Returns whether the given tile is a level crossing.
	 * /
	static  boolean IsLevelCrossing(TileIndex tile)
	{
		return (tile.getMap().m5 & 0xF0) == 0x10;
	}


	/**
	 * Returns a pointer to the Railtype information for a given railtype
	 * @param railtype the rail type which the information is requested for
	 * @return The pointer to the RailtypeInfo
	 */
	static final RailtypeInfo GetRailTypeInfo(/*RailType*/ int railtype)
	{
		assert(railtype < RAILTYPE_END);
		return _railtypes[railtype];
	}

	/**
	 * Checks if an engine of the given RailType can drive on a tile with a given
	 * RailType. This would normally just be an equality check, but for electric
	 * rails (which also support non-electric engines).
	 * @return Whether the engine can drive on this tile.
	 * @param  enginetype The RailType of the engine we are considering.
	 * @param  tiletype   The RailType of the tile we are considering.
	 */
	static  boolean IsCompatibleRail(/*RailType*/ int enginetype, /*RailType*/ int tiletype)
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
	static  boolean TracksOverlap(/*TrackBits*/ int  bits)
	{
		/* With no, or only one track, there is no overlap */
		if (bits == 0 || BitOps.KILL_FIRST_BIT(bits) == 0)
			return false;
		/* We know that there are at least two tracks present. When there are more
		 * than 2 tracks, they will surely overlap. When there are two, they will
		 * always overlap unless they are lower & upper or right & left. */
		if ((bits == (TRACK_BIT_UPPER|TRACK_BIT_LOWER)) || (bits == (TRACK_BIT_LEFT | TRACK_BIT_RIGHT)))
			return false;
		return true;
	}

	//void DrawTrackBits(TileInfo ti, TrackBits track, boolean earth, boolean snow, boolean flat);
	//void DrawTrainDepotSprite(int x, int y, int image, RailType railtype);
	//void DrawDefaultWaypointSprite(int x, int y, RailType railtype);
	//#endif /* RAIL_H */


	/* $Id: rail.c 3077 2005-10-22 06:39:32Z tron $ */







	/*RailType*/ int GetTileRailType(TileIndex tile, /*Trackdir*/ int trackdir)
	{
		/*RailType*/ int type = INVALID_RAILTYPE;
		/*DiagDirection*/ int  exitdir = TrackdirToExitdir(trackdir);
		switch (tile.GetTileType()) {
		case MP_RAILWAY:
			/* railway track */
			type = tile.getMap().m3 & RAILTYPE_MASK;
			break;
		case MP_STREET:
			/* rail/road crossing */
			if (tile.IsLevelCrossing())
				type = tile.getMap().m4 & RAILTYPE_MASK;
			break;
		case MP_STATION:
			if (tile.IsTrainStationTile())
				type = tile.getMap().m3 & RAILTYPE_MASK;
			break;
		case MP_TUNNELBRIDGE:
			/* railway tunnel */
			if ((tile.getMap().m5 & 0xFC) == 0) type = tile.getMap().m3 & RAILTYPE_MASK;
			/* railway bridge ending */
			if ((tile.getMap().m5 & 0xC6) == 0x80) type = tile.getMap().m3 & RAILTYPE_MASK;
			/* on railway bridge */
			if ((tile.getMap().m5 & 0xC6) == 0xC0 && (tile.getMap().m5 & 0x1) == (exitdir & 0x1))
				type = (tile.getMap().m3 >> 4) & RAILTYPE_MASK;
			/* under bridge (any type) */
			if ((tile.getMap().m5 & 0xC0) == 0xC0 && (tile.getMap().m5 & 0x1) != (exitdir & 0x1))
				type = tile.getMap().m3 & RAILTYPE_MASK;
			break;
		default:
			break;
		}
		return type;
	}









	/* $Id: rail_cmd.c 3298 2005-12-14 06:28:48Z tron $ */

























	//extern int _custom_sprites_base;


	//void ShowTrainDepotWindow(TileIndex tile);

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

	static boolean CheckTrackCombination(TileIndex tile, /*TrackBits*/ int  to_build, int flags)
	{
		/*RailTileType*/ int  type = GetRailTileType(tile);
		/*TrackBits*/ int  current; /* The current track layout */
		/*TrackBits*/ int  future; /* The track layout we want to build */
		Global._error_message = Str.STR_1001_IMPOSSIBLE_TRACK_COMBINATION;

		if( type != RAIL_TYPE_NORMAL && type != RAIL_TYPE_SIGNALS)
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
		if (0 != (flags & Cmd.DC_NO_RAIL_OVERLAP) || type == RAIL_TYPE_SIGNALS) {
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



	static int GetRailFoundation(int tileh, int bits)
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


	static int CheckRailSlope(int tileh, /*TrackBits*/ int  rail_bits, /*TrackBits*/ int  existing, TileIndex tile)
	{
		// never allow building on top of steep tiles
		if (!TileIndex.IsSteepTileh(tileh)) {
			rail_bits |= existing;

			// don't allow building on the lower side of a coast
			if (tile.IsTileType( TileTypes.MP_WATER) &&
					(0 != (~_valid_tileh_slopes[2][tileh] & rail_bits)) ) {
				return Cmd.return_cmd_error(Str.STR_3807_CAN_T_BUILD_ON_WATER);
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
				} else if (!Global._patches.build_on_slopes || Global._is_old_ai_player) {
					return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
				} else {
					return Global._price.terraform;
				}
			}
		}
		return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
	}

	/* Validate functions for rail building */
	static  boolean ValParamTrackOrientation(/* Track */ int  track) {return IsValidTrack(track);}

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
		/* Track */ int  track = p2;
		/*TrackBits*/ int  trackbit;
		int cost = 0;
		int ret;

		if (!Player.ValParamRailtype(p1) || !ValParamTrackOrientation(track)) return Cmd.CMD_ERROR;

		tile = TileIndex.TileVirtXY(x, y);
		tileh = tile.GetTileSlope(null);
		m5 = tile.getMap().m5;
		trackbit = TrackToTrackBits(track);

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		switch (tile.GetTileType()) {
		case MP_TUNNELBRIDGE:
			if ((m5 & 0xC0) != 0xC0 || // not bridge middle part?
			( (0 != (m5 & 0x01)) ? TRACK_BIT_DIAG1 : TRACK_BIT_DIAG2) != trackbit) { // wrong direction?
				// Get detailed error message
				return Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
			}

			switch (m5 & 0x38) { // what's under the bridge?
			case 0x00: // clear land
				ret = CheckRailSlope(tileh, trackbit, 0, tile);
				if (Cmd.CmdFailed(ret)) return ret;
				cost += ret;

				if(0 != (flags & Cmd.DC_EXEC)) {
					tile.SetTileOwner( Global._current_player);
					tile.getMap().m3 = (byte) BitOps.RETSB(tile.getMap().m3, 0, 4, p1);
					tile.getMap().m5 = (byte) ((m5 & 0xC7) | 0x20); // railroad under bridge
				}
				break;

			case 0x20: // rail already there
				return Cmd.return_cmd_error(Str.STR_1007_ALREADY_BUILT);

			default:
				// Get detailed error message
				return Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
			}
			break;

		case MP_RAILWAY:
			if (!CheckTrackCombination(tile, trackbit, flags) ||
					!tile.EnsureNoVehicle()) {
				return Cmd.CMD_ERROR;
			}
			if ( (0 != (m5 & RAIL_TYPE_SPECIAL)) ||
					!tile.IsTileOwner( Global._current_player) ||
					BitOps.GB(tile.getMap().m3, 0, 4) != p1) {
				// Get detailed error message
				return Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
			}

			ret = CheckRailSlope(tileh, trackbit, GetTrackBits(tile), tile);
			if (Cmd.CmdFailed(ret)) return ret;
			cost += ret;

			if(0 != (flags & Cmd.DC_EXEC)) {
				tile.getMap().m2 &= ~RAIL_MAP2LO_GROUND_MASK; // Bare land
				tile.getMap().m5 = (byte) (m5 | trackbit);
			}
			break;

		case MP_STREET:
			if (0 == _valid_tileh_slopes[3][tileh]) // prevent certain slopes
				return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
			if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

			if ((m5 & 0xF0) == 0 && ( // normal road?
					(track == TRACK_DIAG1 && m5 == 0x05) ||
					(track == TRACK_DIAG2 && m5 == 0x0A) // correct direction?
					)) {
				if(0 != (flags & Cmd.DC_EXEC)) {
					tile.getMap().m3 = GetTileOwner(tile);
					tile.SetTileOwner( Global._current_player);
					tile.getMap().m4 = p1;
					tile.getMap().m5 = 0x10 | (track == TRACK_DIAG1 ? 0x08 : 0x00); // level crossing
				}
				break;
			}

			if (tile.IsLevelCrossing() && (m5 & 0x08 ? TRACK_DIAG1 : TRACK_DIAG2) == track)
				return Cmd.return_cmd_error(Str.STR_1007_ALREADY_BUILT);
			/* FALLTHROUGH */

		default:
			ret = CheckRailSlope(tileh, trackbit, 0, tile);
			if (Cmd.CmdFailed(ret)) return ret;
			cost += ret;

			ret = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
			if (Cmd.CmdFailed(ret)) return ret;
			cost += ret;

			if (flags & Cmd.DC_EXEC) {
				SetTileType(tile, TileTypes.MP_RAILWAY);
				tile.SetTileOwner( Global._current_player);
				tile.getMap().m2 = 0; // Bare land
				tile.getMap().m3 = p1; // No signals, rail type
				tile.getMap().m5 = trackbit;
			}
			break;
		}

		if (flags & Cmd.DC_EXEC) {
			tile.MarkTileDirtyByTile();
			SetSignalsOnBothDir(tile, track);
		}

		return cost + Global._price.build_rail;
	}



	/** Remove a single piece of track
	 * @param x,y coordinates for removal of track
	 * @param p1 unused
	 * @param p2 rail orientation
	 */
	int CmdRemoveSingleRail(int x, int y, int flags, int p1, int p2)
	{
		/* Track */ int  track = p2;
		/*TrackBits*/ int  trackbit;
		TileIndex tile;
		byte m5;
		int cost = Global._price.remove_rail;

		if (!ValParamTrackOrientation(p2)) return Cmd.CMD_ERROR;
		trackbit = TrackToTrackBits(track);

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		tile = TileIndex.TileVirtXY(x, y);

		if (!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && !tile.IsTileType( TileTypes.MP_STREET) && !tile.IsTileType( TileTypes.MP_RAILWAY))
			return Cmd.CMD_ERROR;

		if (Global._current_player.id != Owner.OWNER_WATER && !Player.CheckTileOwnership(tile))
			return Cmd.CMD_ERROR;

		// allow building rail under bridge
		if (!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && !tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		switch(tile.GetTileType())
		{
		case MP_TUNNELBRIDGE:
			if (!tile.EnsureNoVehicleZ(TilePixelHeight(tile)))
				return Cmd.CMD_ERROR;

			if ((tile.getMap().m5 & 0xF8) != 0xE0)
				return Cmd.CMD_ERROR;

			if ((tile.getMap().m5 & 1 ? TRACK_BIT_DIAG1 : TRACK_BIT_DIAG2) != trackbit)
				return Cmd.CMD_ERROR;

			if (!(flags & Cmd.DC_EXEC))
				return Global._price.remove_rail;

			tile.SetTileOwner( Owner.OWNER_NONE);
			tile.getMap().m5 = tile.getMap().m5 & 0xC7;
			break;

		case TileTypes.MP_STREET:
			if (!tile.IsLevelCrossing()) return Cmd.CMD_ERROR;

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
			tile.SetTileOwner( tile.getMap().m3);
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
				//goto skip_mark_dirty;
				SetSignalsOnBothDir(tile, track);

				return cost;
			}
			break;

		default:
			assert(0);
		}

		/* mark_dirty */
		tile.MarkTileDirtyByTile();

		//skip_mark_dirty:;

		SetSignalsOnBothDir(tile, track);

		return cost;
	}

	/**
	 * 
	 * @param trackdir Pointer to single trackdir var - modified
	 * @param x
	 * @param y
	 * @param ex
	 * @param ey
	 * @return
	 */
	static int ValidateAutoDrag(/*Trackdir*/ int [] trackdir, int x, int y, int ex, int ey)
	{
		int dx, dy, trdx, trdy;

		if (!ValParamTrackOrientation(trackdir[0])) return Cmd.CMD_ERROR;

		// calculate delta x,y from start to end tile
		dx = ex - x;
		dy = ey - y;

		// calculate delta x,y for the first direction
		trdx = _railbit.xinc[trackdir[0]];
		trdy = _railbit.yinc[trackdir[0]];

		if (!IsDiagonalTrackdir(trackdir[0])) {
			trdx += _railbit.xinc[trackdir[0] ^ 1];
			trdy += _railbit.yinc[trackdir[0] ^ 1];
		}

		// validate the direction
		while (((trdx <= 0) && (dx > 0)) || ((trdx >= 0) && (dx < 0)) ||
				((trdy <= 0) && (dy > 0)) || ((trdy >= 0) && (dy < 0))) {
			if (!BitOps.HASBIT(trackdir[0], 3)) { // first direction is invalid, try the other
				trackdir[0] = BitOps.RETSETBIT(trackdir[0], 3); // reverse the direction
				trdx = -trdx;
				trdy = -trdy;
			} else // other direction is invalid too, invalid drag
				return Cmd.CMD_ERROR;
		}

		// (for diagonal tracks, this is already made sure of by above test), but:
		// for non-diagonal tracks, check if the start and end tile are on 1 line
		if (!IsDiagonalTrackdir(trackdir[0])) {
			trdx = _railbit.xinc[trackdir[0]];
			trdy = _railbit.yinc[trackdir[0]];
			if ((Math.abs(dx) != Math.abs(dy)) && (Math.abs(dx) + Math.abs(trdy) != Math.abs(dy) + Math.abs(trdx)))
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
		/* Track */ int  track = BitOps.GB(p2, 4, 3);
		/*Trackdir*/ int trackdir;
		byte mode = (byte) (BitOps.HASBIT(p2, 7) ? 1 : 0);
		/*RailType*/ int railtype = BitOps.GB(p2, 0, 4);

		if (!Player.ValParamRailtype(railtype) || !ValParamTrackOrientation(track)) return Cmd.CMD_ERROR;
		if (p1 > Global.MapSize()) return Cmd.CMD_ERROR;
		trackdir = TrackToTrackdir(track);

		TileIndex pp1 = TileIndex.get(p1);
		/* unpack end point */
		ex = pp1.TileX() * TileInfo.TILE_SIZE;
		ey = pp1.TileY() * TileInfo.TILE_SIZE;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		{
			int[] trackdirPtr = { trackdir };
			if (Cmd.CmdFailed(ValidateAutoDrag( trackdirPtr, x, y, ex, ey))) return Cmd.CMD_ERROR;
			trackdir = trackdirPtr[0];
		}
		//if(0!=(flags & Cmd.DC_EXEC)) SndPlayTileFx(SND_20_SPLAT_2, TileIndex.TileVirtXY(x, y));

		for(;;) {
			ret = Cmd.DoCommand(x, y, railtype, TrackdirToTrack(trackdir), flags, (mode == 0) ? Cmd.CMD_BUILD_SINGLE_RAIL : Cmd.CMD_REMOVE_SINGLE_RAIL);

			if (Cmd.CmdFailed(ret)) {
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
		return CmdRailTrackHelper(x, y, flags, p1, BitOps.RETCLRBIT(p2, 7));
	}

	/** Build rail on a stretch of track.
	 * Stub for the unified rail builder/remover
	 * @see CmdRailTrackHelper
	 */
	int CmdRemoveRailroadTrack(int x, int y, int flags, int p1, int p2)
	{
		return CmdRailTrackHelper(x, y, flags, p1, BitOps.RETSETBIT(p2, 7));
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
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		int cost, ret;
		int tileh;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;
		/* check railtype and valid direction for depot (0 through 3), 4 in total */
		if (!Player.ValParamRailtype(p1) || p2 > 3) return Cmd.CMD_ERROR;

		tileh = tile.GetTileSlope(null);

		/* Prohibit finalruction if
			The tile is non-flat AND
			1) The AI is "old-school"
			2) build-on-slopes is disabled
			3) the tile is steep i.e. spans two height levels
			4) the exit points in the wrong direction

		 */

		if (tileh != 0 && (
				Global._is_old_ai_player ||
				!Global._patches.build_on_slopes ||
				TileIndex.IsSteepTileh(tileh) ||
				!Depot.CanBuildDepotByTileh(p2, tileh)
				)) {
			return Cmd.return_cmd_error(Str.STR_0007_FLAT_LAND_REQUIRED);
		}

		ret = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
		cost = ret;

		d = Depot.AllocateDepot();
		if (d == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			if (Player.IsLocalPlayer()) Depot._last_built_train_depot_tile = tile;

			Landscape.ModifyTile(tile,
					TileTypes.MP_SETTYPE(TileTypes.MP_RAILWAY) |
					TileTypes.MP_MAP3LO | TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5,
					p1, /* map3_lo */
					p2 | RAIL_TYPE_DEPOT_WAYPOINT /* map5 */
					);

			d.xy = tile;
			d.town_index = Town.ClosestTownFromTile(tile, (int)-1).index;

			SetSignalsOnBothDir(tile, (p2&1) != 0 ? 2 : 1);

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
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		boolean semaphore;
		boolean pre_signal_cycle;
		byte pre_signal_type;
		/* Track */ int  track = (p1 & 0x7);
		int cost;

		// Same bit, used in different contexts
		//		semaphore = pre_signal = BitOps.HASBIT(p1, 3);

		if (!ValParamTrackOrientation(track) || !tile.IsTileType( TileTypes.MP_RAILWAY) || !tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		/* Protect against invalid signal copying */
		if (p2 != 0 && (p2 & SignalOnTrack(track)) == 0) return Cmd.CMD_ERROR;

		/* You can only build signals on plain rail tiles, and the selected track must exist */
		if (!IsPlainRailTile(tile) || !HasTrack(tile, track)) return Cmd.CMD_ERROR;

		if (!Player.CheckTileOwnership(tile)) return Cmd.CMD_ERROR;

		Global._error_message = Str.STR_1005_NO_SUITABLE_RAILROAD_TRACK;

		{
			/* See if this is a valid track combination for signals, (ie, no overlap) */
			/*TrackBits*/ int  trackbits = GetTrackBits(tile);
			if (BitOps.KILL_FIRST_BIT(trackbits) != 0 && /* More than one track present */
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
						tile.getMap().m4 = BitOps.RETSB(tile.getMap().m4, 0, 3, type);
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

			tile.MarkTileDirtyByTile();
			SetSignalsOnBothDir(tile, track);
		}

		return cost;
	}


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
	static int BuildAutoSignals(int x, int y, /*Trackdir*/ int trackdir, int flags, int p2, byte signals)
	{
		byte signal_density = (byte) (p2 >> 24);
		int signal_ctr = signal_density * 2;
		byte signal_dir = 0;	// direction in which signals are placed 1=forward  2=backward  3=twoway
		int track_mode = 0;	// 128=bridge, 64=tunnel, 192=end of tunnel/bridge, 0=normal track
		byte track_height = 0; // height of tunnel currently in
		int retr, total_cost = 0;
		TileIndex tile = TileIndex.TileVirtXY/*TILE_FROM_XY*/(x, y);
		byte m5 = tile.getMap().m5;
		byte m3 = tile.getMap().m3;
		byte semaphores = (byte) ((tile.getMap().m4 & ~3) != 0 ? 16 : 0);
		int mode = p2 & 0x1;
		int lx, ly;
		byte dir;


		// remember start position and direction
		int sx = x, sy = y;
		/*Trackdir*/ int srb = trackdir;

		// get first signal mode
		if(0 != (signals & _signals_table[trackdir])) signal_dir |= 1;
		if(0 != (signals & _signals_table_other[trackdir])) signal_dir |= 2;

		// check for semaphores
		/*
		if (!(m5 & RAIL_TYPE_SPECIAL) && (m5 & RAIL_BIT_MASK) && (m5 & RAIL_TYPE_SIGNALS))
			semaphores = (tile.getMap().m3 & ~3) ? 16 : 0; // copy signal/semaphores style (independent of GUI)
		 */
		if (signal_dir == 0)
			return Cmd.CMD_ERROR; // no signal on start tile to copy

		semaphores = (byte) (HasSemaphores(tile, TrackdirToTrack(trackdir)) ? 16 : 0); // copy signal/semaphores style (independent of CTRL)

		signals = 0;
		lx = 0;
		ly = 0;

		for(;;) {
			x += _railbit.xinc[trackdir];
			y += _railbit.yinc[trackdir];

			tile = TileIndex.TileVirtXY(x, y);

			m5 = tile.getMap().m5;

			m3 = tile.getMap().m3;

			dir = _dir_from_track[trackdir];

			if(0 != (track_mode & 128)) { // currently on bridge
				if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && ((m5 & 192) == 128))
					// end of bridge
					track_mode = 192;
			} else if(0 != (track_mode & 64)) { // currently in tunnel
				if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)
						&& ((m5 & 0xF0) == 0)
						&& ((m5 & 3) == (dir ^ 2))
						&& (Landscape.GetSlopeZ(x+8, y+8) == track_height))
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
					track_height = (byte) Landscape.GetSlopeZ(x+8, y+8);
				};
			};

			/* for pieces that we cannot build signals on but are not an end of track or a junction, we continue counting. When a signal
				 should be placed in one of these tiles, it is instead placed on the last possible place for signals, and the counting is
				 reset from that place. If a signal already is there, one will be placed one the first possible tile encountered.
			   last place where a signal could be placed is remembered by lx,ly
				 if signal==0 a signal is already on lx,ly
			 */
			if ( (tile.IsTileType( TileTypes.MP_RAILWAY) && ((m5 & ~1) == WayPoint.RAIL_TYPE_WAYPOINT)	&& ((m5 & 1) == (dir & 1))) // check for waypoints
					|| (tile.IsTileType( TileTypes.MP_STREET) && ((m5 >> 4) == 1)										
							&& ((0==(m5 & 8)) != (0==(dir & 1)))) // check for road crossings
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
						retr = Cmd.DoCommand(x, y, TrackdirToTrack(trackdir) | semaphores, signals, flags, (mode == 1) ? Cmd.CMD_REMOVE_SIGNALS : Cmd.CMD_BUILD_SIGNALS);
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

			if ((0!=(m5 & RAIL_TYPE_SPECIAL)) || 0==(m5 & 0x3F))
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
				assert false;
			}

			// calculate signals to place
			signals = 0;
			if(0 != (signal_dir & 1)) signals |= _signals_table[trackdir];
			if(0 != (signal_dir & 2)) signals |= _signals_table_other[trackdir];

			if (x == sx && y == sy && trackdir == srb)
				return total_cost; // back at the start, we are finished

			// remember last place signals could be placed
			lx = x;			ly = y;

			m5 = tile.getMap().m5;
			if (mode != 0)
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
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		boolean error = true;

		int mode = p2 & 0x1;
		/* Track */ int  track = BitOps.GB(p2, 4, 3);
		/*Trackdir*/ int trackdir = TrackToTrackdir(track);
		int semaphores = (BitOps.HASBIT(p2, 3)) ? 16 : 0;
		int signal_density = (p2 >> 24);

		if (p1 > Global.MapSize()) return Cmd.CMD_ERROR;
		if (signal_density == 0 || signal_density > 20) return Cmd.CMD_ERROR;

		if (!tile.IsTileType( TileTypes.MP_RAILWAY)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		/* for vertical/horizontal tracks, double the given signals density
		 * since the original amount will be too dense (shorter tracks) */
		if (!IsDiagonalTrack(track))
			signal_density *= 2;

		{
		// unpack end tile
			TileIndex pp = TileIndex.get(p1);
			ex = pp.TileX() * TileInfo.TILE_SIZE;
			ey = pp.TileY() * TileInfo.TILE_SIZE;
		}
		
		{
			int[] tdp = { trackdir };
			if (Cmd.CmdFailed(ValidateAutoDrag(tdp, x, y, ex, ey))) return Cmd.CMD_ERROR;
			trackdir = tdp[0];
		}
		track = TrackdirToTrack(trackdir); /* trackdir might have changed, keep track in sync */

		// copy the signal-style of the first rail-piece if existing
		if (GetRailTileType(tile) == RAIL_TYPE_SIGNALS && GetTrackBits(tile) != 0) { /* XXX: GetTrackBits check useless? */
			signals = (byte) (tile.getMap().m3 & SignalOnTrack(track));
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
				ret = Cmd.DoCommand(x, y, TrackdirToTrack(trackdir) | semaphores, signals, flags, (mode == 1) ? Cmd.CMD_REMOVE_SIGNALS : Cmd.CMD_BUILD_SIGNALS);

				/* Abort placement for any other error than NOT_SUITABLE_TRACK
				 * This includes vehicles on track, competitor's tracks, etc. */
				if (Cmd.CmdFailed(ret)) {
					if (Global._error_message != Str.STR_1005_NO_SUITABLE_RAILROAD_TRACK && mode != 1) return Cmd.CMD_ERROR;
				} else {
					error = false;
					total_cost += ret;

					/* when autocompletion is on, use that to place the rest of the signals */
					if( BitOps.HASBIT(p2, 1)) {
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
	public static int CmdBuildSignalTrack(int x, int y, int flags, int p1, int p2)
	{
		return CmdSignalTrackHelper(x, y, flags, p1, p2);
	}

	/** Remove signals
	 * @param x,y coordinates where signal is being deleted from
	 * @param p1 track to remove signal from (Track enum)
	 */
	int CmdRemoveSingleSignal(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		/* Track */ int  track = (p1 & 0x7);

		if (!ValParamTrackOrientation(track) || !tile.IsTileType( TileTypes.MP_RAILWAY) || !tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		if (!HasSignalOnTrack(tile, track)) // no signals on track?
			return Cmd.CMD_ERROR;

		/* Only water can remove signals from anyone */
		if (Global._current_player.id != Owner.OWNER_WATER && !Player.CheckTileOwnership(tile)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		/* Do it? */
		if(0 != (flags & Cmd.DC_EXEC)) {
			tile.getMap().m3 &= ~SignalOnTrack(track);

			/* removed last signal from tile? */
			if (BitOps.GB(tile.getMap().m3, 4, 4) == 0) 
			{
				tile.getMap().m2 = BitOps.RETSB(tile.getMap().m2, 4, 4, 0);
				tile.getMap().m5 = (byte) BitOps.RETSB(tile.getMap().m5, 6, 2, RAIL_TYPE_NORMAL >> 6); // XXX >> because the finalant is meant for direct application, not use with SB
				tile.getMap().m4 = BitOps.RETCLRBIT(tile.getMap().m4, 3); // remove any possible semaphores
			}

			SetSignalsOnBothDir(tile, track);

			tile.MarkTileDirtyByTile();
		}

		return Global._price.remove_signals;
	}

	/** Remove signals on a stretch of track.
	 * Stub for the unified signal builder/remover
	 * @see CmdSignalTrackHelper
	 */
	public static int CmdRemoveSignalTrack(int x, int y, int flags, int p1, int p2)
	{
		return CmdSignalTrackHelper(x, y, flags, p1, BitOps.RETSETBIT(p2, 0));
	}

	//typedef int DoConvertRailProc(TileIndex tile, int totype, boolean exec);

	static int DoConvertRail(TileIndex tile, int totype, boolean exec)
	{
		if (!tile.CheckTileOwnership() || !tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		// tile is already of requested type?
		if (GetRailType(tile) == totype) return Cmd.CMD_ERROR;

		// change type.
		if (exec) {
			tile.getMap().m3 = (byte) BitOps.RETSB(tile.getMap().m3, 0, 4, totype);
			tile.MarkTileDirtyByTile();
		}

		return Global._price.build_rail / 2;
	}

	//extern int DoConvertStationRail(TileIndex tile, int totype, boolean exec);
	//extern int DoConvertStreetRail(TileIndex tile, int totype, boolean exec);
	//extern int DoConvertTunnelBridgeRail(TileIndex tile, int totype, boolean exec);

	/** Convert one rail type to the other. You can convert normal rail to
	 * monorail/maglev easily or vice-versa.
	 * @param ex,ey end tile of rail conversion drag
	 * @param p1 start tile of drag
	 * @param p2 new railtype to convert to
	 */
	public static int CmdConvertRail(int ex, int ey, int flags, int p1, int p2)
	{
		int ret, cost, money;
		int sx, sy, x, y;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (!Player.ValParamRailtype(p2)) return Cmd.CMD_ERROR;
		if (p1 > Global.MapSize()) return Cmd.CMD_ERROR;

		// make sure sx,sy are smaller than ex,ey
		sx = TileX(p1) * TILE_SIZE;
		sy = TileY(p1) * TILE_SIZE;
		if (ex < sx) intswap(ex, sx);
		if (ey < sy) intswap(ey, sy);

		money = GetAvailableMoneyForCommand();
		cost = 0;

		for (x = sx; x <= ex; x += TILE_SIZE) {
			for (y = sy; y <= ey; y += TILE_SIZE) {
				TileIndex tile = TileIndex.TileVirtXY(x, y);
				DoConvertRailProc proc;

				switch (tile.GetTileType()) {
				case TileTypes.MP_RAILWAY:      proc = DoConvertRail;             break;
				case TileTypes.MP_STATION:      proc = DoConvertStationRail;      break;
				case TileTypes.MP_STREET:       proc = DoConvertStreetRail;       break;
				case TileTypes.MP_TUNNELBRIDGE: proc = DoConvertTunnelBridgeRail; break;
				default: continue;
				}

				ret = proc(tile, p2, false);
				if (Cmd.CmdFailed(ret)) continue;
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
		if (!Player.CheckTileOwnership(tile) && Global._current_player.id != Owner.OWNER_WATER)
			return Cmd.CMD_ERROR;

		if (!tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			/* Track */ int  track = TrackdirToTrack(DiagdirToDiagTrackdir(Depot.GetDepotDirection(tile, Global.TRANSPORT_RAIL)));

			Depot.DoDeleteDepot(tile);
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

		if(0 != (flags & Cmd.DC_AUTO)) {
			if(0 !=  (m5 & RAIL_TYPE_SPECIAL))
				return Cmd.return_cmd_error(Str.STR_2004_BUILDING_MUST_BE_DEMOLISHED);

			if (!tile.IsTileOwner( Global._current_player.id))
				return Cmd.return_cmd_error(Str.STR_1024_AREA_IS_OWNED_BY_ANOTHER);

			return Cmd.return_cmd_error(Str.STR_1008_MUST_REMOVE_RAILROAD_TRACK);
		}

		cost = 0;

		switch (GetRailTileType(tile)) {
		case RAIL_TYPE_SIGNALS:
			if(0 != (tile.getMap().m3 & _signals_table_both[0])) {
				ret = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_REMOVE_SIGNALS);
				if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
				cost += ret;
			}
			if(0 != (tile.getMap().m3 & _signals_table_both[3])) {
				ret = Cmd.DoCommandByTile(tile, 3, 0, flags, Cmd.CMD_REMOVE_SIGNALS);
				if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
				cost += ret;
			}

			m5 &= TRACK_BIT_MASK;
			if (0==(flags & Cmd.DC_EXEC)) {
				for (; m5 != 0; m5 >>= 1) if(0 != (m5 & 1)) cost += Global._price.remove_rail;
				return cost;
			}
			/* FALLTHROUGH */

		case RAIL_TYPE_NORMAL: {
			int i;

			for (i = 0; m5 != 0; i++, m5 >>= 1) {
				if(0 !=  (m5 & 1)) {
					ret = Cmd.DoCommandByTile(tile, 0, i, flags, Cmd.CMD_REMOVE_SINGLE_RAIL);
					if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
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
				return WayPoint.RemoveTrainWaypoint(tile, flags, false);

			default:
				return Cmd.CMD_ERROR;
			}

		default:
			return Cmd.CMD_ERROR;
		}
	}






	static void DrawSignalHelper(final TileInfo ti, byte condition, int image_and_pos)
	{
		boolean otherside = 0 != (GameOptions._opt.road_side & (Global._patches.signal_side ? 1 : 0));

		int v = _signal_position[(image_and_pos & 0xF) + (otherside ? 12 : 0)];
		int x = ti.x | (v&0xF);
		int y = ti.y | (v>>4);
		int sprite = _signal_base_sprites[(ti.tile.getMap().m4 & 0xF) + (otherside ? 0x10 : 0)] + (image_and_pos>>4) + ((condition != 0) ? 1 : 0);
		ViewPort.AddSortableSpriteToDraw(sprite, x, y, 1, 1, 10, Landscape.GetSlopeZ(x,y));
	}

	static int _drawtile_track_palette;


	static void DrawTrackFence_NW(final TileInfo ti)
	{
		int image = 0x515;
		if (ti.tileh != 0) image = (0 != (ti.tileh & 2)) ? 0x519 : 0x51B;
		ViewPort.AddSortableSpriteToDraw(image | _drawtile_track_palette,
				ti.x, ti.y+1, 16, 1, 4, ti.z);
	}

	static void DrawTrackFence_SE(final TileInfo ti)
	{
		int image = 0x515;
		if (ti.tileh != 0) image = (0 != (ti.tileh & 2)) ? 0x519 : 0x51B;
		ViewPort.AddSortableSpriteToDraw(image | _drawtile_track_palette,
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
		if (ti.tileh != 0) image = (0!=(ti.tileh & 2)) ? 0x51A : 0x51C;
		ViewPort.AddSortableSpriteToDraw(image | _drawtile_track_palette,
				ti.x+1, ti.y, 1, 16, 4, ti.z);
	}

	static void DrawTrackFence_SW(final TileInfo ti)
	{
		int image = 0x516;
		if (ti.tileh != 0) image = (0!=(ti.tileh & 2)) ? 0x51A : 0x51C;
		ViewPort.AddSortableSpriteToDraw(image | _drawtile_track_palette,
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
		if(0!=(ti.tileh & 1)) z += 8;
		ViewPort.AddSortableSpriteToDraw(0x517 | _drawtile_track_palette,
				ti.x + 8, ti.y + 8, 1, 1, 4, z);
	}

	static void DrawTrackFence_NS_2(final TileInfo ti)
	{
		int z = ti.z;
		if(0!=(ti.tileh & 4)) z += 8;
		ViewPort.AddSortableSpriteToDraw(0x517 | _drawtile_track_palette,
				ti.x + 8, ti.y + 8, 1, 1, 4, z);
	}

	static void DrawTrackFence_WE_1(final TileInfo ti)
	{
		int z = ti.z;
		if(0!=(ti.tileh & 8)) z += 8;
		ViewPort.AddSortableSpriteToDraw(0x518 | _drawtile_track_palette,
				ti.x + 8, ti.y + 8, 1, 1, 4, z);
	}

	static void DrawTrackFence_WE_2(final TileInfo ti)
	{
		int z = ti.z;
		if(0 != (ti.tileh & 2)) 
			z += 8;
		ViewPort.AddSortableSpriteToDraw(0x518 | _drawtile_track_palette,
				ti.x + 8, ti.y + 8, 1, 1, 4, z);
	}

	static void DetTrackDrawProc_Null(final TileInfo ti)
	{
		/* nothing should be here */
	}

	//typedef void DetailedTrackProc(final TileInfo ti);
	static final DetailedTrackProc _detailed_track_proc[] = {
			Rail::DetTrackDrawProc_Null,
			Rail::DetTrackDrawProc_Null,

			Rail::DrawTrackFence_NW,
			Rail::DrawTrackFence_SE,
			Rail::DrawTrackFence_NW_SE,

			Rail::DrawTrackFence_NE,
			Rail::DrawTrackFence_SW,
			Rail::DrawTrackFence_NE_SW,

			Rail::DrawTrackFence_NS_1,
			Rail::DrawTrackFence_NS_2,

			Rail::DrawTrackFence_WE_1,
			Rail::DrawTrackFence_WE_2,

			Rail::DetTrackDrawProc_Null,
			Rail::DetTrackDrawProc_Null,
			Rail::DetTrackDrawProc_Null,
			Rail::DetTrackDrawProc_Null,
	};

	static void DrawSpecialBuilding(int image, int offset,
			final TileInfo  ti,
			byte x, byte y, byte z,
			byte xsize, byte ysize, byte zsize)
	{
		if (image & Sprites.PALETTE_MODIFIER_COLOR) image |= _drawtile_track_palette;
		image += offset;
		if (Global._displayGameOptions._opt & DO_TRANS_BUILDINGS) MAKE_TRANSPARENT(image);
		ViewPort.AddSortableSpriteToDraw(image, ti.x + x, ti.y + y, xsize, ysize, zsize, ti.z + z);
	}

	/**
	 * Draw ground sprite and track bits
	 * @param ti TileInfo
	 * @param track TrackBits to draw
	 * @param earth Draw as earth
	 * @param snow Draw as snow
	 * @param flat Always draw foundation
	 */
	void DrawTrackBits(TileInfo ti, /*TrackBits*/ int  track, boolean earth, boolean snow, boolean flat)
	{
		final RailtypeInfo rti = GetRailTypeInfo(GetRailType(ti.tile));
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
			image = (image & SPRITE_MASK) | Sprites.PALETTE_TO_BARE_LAND; // Use brown palette
		} else if (snow) {
			image += rti.snow_offset;
		}

		ViewPort.DrawGroundSprite(image);

		// Draw track pieces individually for junction tiles
		if (junction) {
			if (track & TRACK_BIT_DIAG1) ViewPort.DrawGroundSprite(rti.base_sprites.single_y);
			if (track & TRACK_BIT_DIAG2) ViewPort.DrawGroundSprite(rti.base_sprites.single_x);
			if (track & TRACK_BIT_UPPER) ViewPort.DrawGroundSprite(rti.base_sprites.single_n);
			if (track & TRACK_BIT_LOWER) ViewPort.DrawGroundSprite(rti.base_sprites.single_s);
			if (track & TRACK_BIT_LEFT)  ViewPort.DrawGroundSprite(rti.base_sprites.single_w);
			if (track & TRACK_BIT_RIGHT) ViewPort.DrawGroundSprite(rti.base_sprites.single_e);
		}

		if (_debug_pbs_level >= 1) {
			byte pbs = PBSTileReserved(ti.tile) & track;
			if (pbs & TRACK_BIT_DIAG1) ViewPort.DrawGroundSprite(rti.base_sprites.single_y | Sprites.PALETTE_CRASH);
			if (pbs & TRACK_BIT_DIAG2) ViewPort.DrawGroundSprite(rti.base_sprites.single_x | Sprites.PALETTE_CRASH);
			if (pbs & TRACK_BIT_UPPER) ViewPort.DrawGroundSprite(rti.base_sprites.single_n | Sprites.PALETTE_CRASH);
			if (pbs & TRACK_BIT_LOWER) ViewPort.DrawGroundSprite(rti.base_sprites.single_s | Sprites.PALETTE_CRASH);
			if (pbs & TRACK_BIT_LEFT)  ViewPort.DrawGroundSprite(rti.base_sprites.single_w | Sprites.PALETTE_CRASH);
			if (pbs & TRACK_BIT_RIGHT) ViewPort.DrawGroundSprite(rti.base_sprites.single_e | Sprites.PALETTE_CRASH);
		}
	}


	private static boolean HAS_SIGNAL(int x, int m23) { return 0 != (m23 & (byte)(0x1 << (x))); }
	private static boolean ISON_SIGNAL(int x, int m23) { return 0 != (m23 & (byte)(0x10 << (x))); }

	private static void MAYBE_DRAW_SIGNAL(TileInfo ti, int x, int y, int z, int m23)
	{
		if (HAS_SIGNAL(x,m23)) 
			DrawSignalHelper(ti, ISON_SIGNAL(x,m23), ((y-0x4FB) << 4)|(z));
	}

	static void DrawTile_Track(TileInfo ti)
	{
		byte m5;
		final RailtypeInfo rti = GetRailTypeInfo(GetRailType(ti.tile));
		PalSpriteID image;

		_drawtile_track_palette = SPRITE_PALETTE(PLAYER_SPRITE_COLOR(GetTileOwner(ti.tile)));

		m5 = (byte)ti.map5;
		if (0==(m5 & RAIL_TYPE_SPECIAL)) {
			boolean earth = (ti.tile.getMap().m2 & RAIL_MAP2LO_GROUND_MASK) == RAIL_GROUND_BROWN;
			boolean snow = (ti.tile.getMap().m2 & RAIL_MAP2LO_GROUND_MASK) == RAIL_GROUND_ICE_DESERT;

			DrawTrackBits(ti, m5 & TRACK_BIT_MASK, earth, snow, false);

			if (Global._displayGameOptions._opt & DO_FULL_DETAIL) {
				_detailed_track_proc[ti.tile.getMap().m2 & RAIL_MAP2LO_GROUND_MASK].accept(ti);
			}

			/* draw signals also? */
			if (0==(ti.map5 & RAIL_TYPE_SIGNALS))
				return;

			{
				byte m23 = (byte) ((ti.tile.getMap().m3 >> 4) | (ti.tile.getMap().m2 & 0xF0));


				if (0==(m5 & TRACK_BIT_DIAG2)) {
					if (0==(m5 & TRACK_BIT_DIAG1)) {
						if(0 != (m5 & TRACK_BIT_LEFT)) {
							MAYBE_DRAW_SIGNAL( ti, 2, 0x509, 0, m23);
							MAYBE_DRAW_SIGNAL( ti, 3, 0x507, 1, m23);
						}
						if(0 != (m5 & TRACK_BIT_RIGHT)) {
							MAYBE_DRAW_SIGNAL( ti, 0, 0x509, 2, m23);
							MAYBE_DRAW_SIGNAL( ti, 1, 0x507, 3, m23);
						}
						if(0 != (m5 & TRACK_BIT_UPPER)) {
							MAYBE_DRAW_SIGNAL( ti, 3, 0x505, 4, m23);
							MAYBE_DRAW_SIGNAL( ti, 2, 0x503, 5, m23);
						}
						if(0 != (m5 & TRACK_BIT_LOWER)) {
							MAYBE_DRAW_SIGNAL( ti, 1, 0x505, 6, m23);
							MAYBE_DRAW_SIGNAL( ti, 0, 0x503, 7, m23);
						}
					} else {
						MAYBE_DRAW_SIGNAL( ti, 3, 0x4FB, 8, m23);
						MAYBE_DRAW_SIGNAL( ti, 2, 0x4FD, 9, m23);
					}
				} else {
					MAYBE_DRAW_SIGNAL( ti, 3, 0x4FF, 10, m23);
					MAYBE_DRAW_SIGNAL( ti, 2, 0x501, 11, m23);
				}
			}
		} else {
			/* draw depots / waypoints */
			final DrawTrackSeqStruct drss;
			byte type = (byte) (m5 & 0x3F); // 0-3: depots, 4-5: waypoints

			if (0==(m5 & (RAIL_TILE_TYPE_MASK&~RAIL_TYPE_SPECIAL)))
				/* XXX: There used to be "return;" here, but since I could not find out
				 * why this would ever occur, I put assert(0) here. Let's see if someone
				 * complains about it. If not, we'll remove this check. (Matthijs). */
				assert false;

			if (ti.tileh != 0) DrawFoundation(ti, ti.tileh);

			if (IsRailWaypoint(ti.tile) && BitOps.HASBIT(ti.tile.getMap().m3, 4)) {
				// look for customization
				byte stat_id = WayPoint.GetWaypointByTile(ti.tile).stat_id;
				final StationSpec stat = Station.GetCustomStation(STAT_CLASS_WAYP, stat_id);

				if (stat != null) {
					final DrawTileSeqStruct seq;
					// emulate station tile - open with building
					final DrawTileSprites cust = stat.renderdata[2 + (m5 & 0x1)];
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

					ViewPort.DrawGroundSprite(image);

					// #define foreach_draw_tile_seq(idx, list) for (idx = list; ((byte) idx->delta_x) != 0x80; idx++)
					//foreach_draw_tile_seq(seq, cust.seq)
					for (seq = cust.seq; ((byte) seq.delta_x) != 0x80; seq++)
					{
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
			if (image & Sprite.PALETTE_MODIFIER_COLOR) image = (image & Sprite.SPRITE_MASK) + rti.total_offset;

			// adjust ground tile for desert
			// (don't adjust for arctic depots, because snow in depots looks weird)
			// type >= 4 means waypoints
			if ((ti.tile.getMap().m4 & RAIL_MAP2LO_GROUND_MASK) == RAIL_GROUND_ICE_DESERT && (GameOptions._opt.landscape == Landscape.LT_DESERT || type >= 4)) {
				if (image.id != Sprite.SPR_FLAT_GRASS_TILE) {
					image += rti.snow_offset; // tile with tracks
				} else {
					image.id = Sprite.SPR_FLAT_SNOWY_TILE; // flat ground
				}
			}

			ViewPort.DrawGroundSprite(image.id);

			if (Global._debug_pbs_level >= 1) {
				int pbs = Pbs.PBSTileReserved(ti.tile);
				if(0!=(pbs & TRACK_BIT_DIAG1)) ViewPort.DrawGroundSprite(rti.base_sprites.single_y.id | Sprites.PALETTE_CRASH);
				if(0!=(pbs & TRACK_BIT_DIAG2)) ViewPort.DrawGroundSprite(rti.base_sprites.single_x.id | Sprites.PALETTE_CRASH);
				if(0!=(pbs & TRACK_BIT_UPPER)) ViewPort.DrawGroundSprite(rti.base_sprites.single_n.id | Sprites.PALETTE_CRASH);
				if(0!=(pbs & TRACK_BIT_LOWER)) ViewPort.DrawGroundSprite(rti.base_sprites.single_s.id | Sprites.PALETTE_CRASH);
				if(0!=(pbs & TRACK_BIT_LEFT))  ViewPort.DrawGroundSprite(rti.base_sprites.single_w.id | Sprites.PALETTE_CRASH);
				if(0!=(pbs & TRACK_BIT_RIGHT)) ViewPort.DrawGroundSprite(rti.base_sprites.single_e.id | Sprites.PALETTE_CRASH);
			}

			for (; drss.image != 0; drss++) {
				DrawSpecialBuilding(drss.image, type < 4 ? rti.total_offset : 0, ti,
						drss.subcoord_x, drss.subcoord_y, 0,
						drss.width, drss.height, 0x17);
			}
		}
	}

	static void DrawTrainDepotSprite(int x, int y, int image, /*RailType*/ int railtype)
	{
		int ormod, img;
		final RailtypeInfo rti = GetRailTypeInfo(railtype);
		final DrawTrackSeqStruct dtss;

		ormod = PLAYER_SPRITE_COLOR(Global._local_player);

		dtss = _track_depot_layout_table[image];

		x += 33;
		y += 17;

		img = dtss++.image;
		/* @note This is kind of an ugly hack, as the PALETTE_MODIFIER_COLOR indicates
		 * whether the sprite is railtype dependent. Rewrite this asap */
		if (img & Sprites.PALETTE_MODIFIER_COLOR) img = (img & SPRITE_MASK) + rti.total_offset;
		Gfx.DrawSprite(img, x, y);

		for (; dtss.image != 0; dtss++) {
			Point pt = RemapCoords(dtss.subcoord_x, dtss.subcoord_y, 0);
			image = dtss.image;
			if (image & Sprites.PALETTE_MODIFIER_COLOR) image |= ormod;
			Gfx.DrawSprite(image + rti.total_offset, x + pt.x, y + pt.y);
		}
	}

	void DrawDefaultWaypointSprite(int x, int y, /*RailType*/ int railtype)
	{
		final DrawTrackSeqStruct dtss = _track_depot_layout_table[4];
		final RailtypeInfo rti = GetRailTypeInfo(railtype);
		int img;

		img = dtss++.image;
		if (img & Sprites.PALETTE_MODIFIER_COLOR) img = (img & SPRITE_MASK) + rti.total_offset;
		Gfx.DrawSprite(img, x, y);

		for (; dtss.image != 0; dtss++) {
			Point pt = RemapCoords(dtss.subcoord_x, dtss.subcoord_y, 0);
			img = dtss.image;
			if (img & Sprites.PALETTE_MODIFIER_COLOR) img |= PLAYER_SPRITE_COLOR(Global._local_player);
			Gfx.DrawSprite(img, x + pt.x, y + pt.y);
		}
	}

	class SetSignalsData {
		int cur;
		int cur_stack;
		boolean stop;
		boolean has_presignal;

		//boolean has_pbssignal;
		int has_pbssignal;
		// lowest 2 bits = amount of pbs signals in the block, clamped at 2
		// bit 2 = there is a pbs entry signal in this block
		// bit 3 = there is a pbs exit signal in this block

		// presignal info
		int presignal_exits;
		int presignal_exits_free;

		// these are used to keep track of the signals that change.
		byte [] bit = new byte[NUM_SSD_ENTRY];
		TileIndex[] tile = new TileIndex[NUM_SSD_ENTRY];

		int pbs_cur;
		// these are used to keep track of all signals in the block
		TileIndex [] pbs_tile = new TileIndex[NUM_SSD_ENTRY];

		// these are used to keep track of the stack that modifies presignals recursively
		TileIndex [] next_tile = new TileIndex[NUM_SSD_STACK];
		byte [] next_dir = new byte[NUM_SSD_STACK];

	}

	static boolean SetSignalsEnumProc(TileIndex tile, SetSignalsData ssd, int track, int length, Object state)
	{
		// the tile has signals?
		if (tile.IsTileType( TileTypes.MP_RAILWAY)) {
			if (HasSignalOnTrack(tile, TrackdirToTrack(track))) {
				if ((tile.getMap().m3 & _signals_table[track]) != 0) {
					// yes, add the signal to the list of signals
					if (ssd.cur != NUM_SSD_ENTRY) {
						ssd.tile[ssd.cur] = tile; // remember the tile index
						ssd.bit[ssd.cur] = (byte) track; // and the controlling bit number
						ssd.cur++;
					}

					if (Pbs.PBSIsPbsSignal(tile, ReverseTrackdir(track)))
						ssd.has_pbssignal = BitOps.RETSETBIT(ssd.has_pbssignal, 2);

					// remember if this block has a presignal.
					ssd.has_presignal |= (tile.getMap().m4&1);
				}

				if (Pbs.PBSIsPbsSignal(tile, ReverseTrackdir(track)) || Pbs.PBSIsPbsSignal(tile, track)) 
				{
					byte num = ssd.has_pbssignal & 3;
					num = clamp(num + 1, 0, 2);
					ssd.has_pbssignal &= ~3;
					ssd.has_pbssignal |= num;
				}

				if ((tile.getMap().m3 & _signals_table_both[track]) != 0) {
					ssd.pbs_tile[ssd.pbs_cur] = tile; // remember the tile index
					ssd.pbs_cur++;
				}

				if(0 != (tile.getMap().m3&_signals_table_other[track])) 
				{
					if (tile.getMap().m4&2) {
						// this is an exit signal that points out from the segment
						ssd.presignal_exits++;
						if ((tile.getMap().m2&_signals_table_other[track]) != 0)
							ssd.presignal_exits_free++;
					}
					if (Pbs.PBSIsPbsSignal(tile, track))
						ssd.has_pbssignal = BitOps.RETSETBIT(ssd.has_pbssignal, 3);
				}

				return true;
			} else if (Depot.IsTileDepotType(tile, Global.TRANSPORT_RAIL)) {
				return true; // don't look further if the tile is a depot
			}
		}
		return false;
	}

	/* Struct to parse data from VehicleFromPos to SignalVehicleCheckProc */
	class SignalVehicleCheckStruct {
		TileIndex tile;
		int track;
	}

	static Object SignalVehicleCheckProc(Vehicle v, Object data)
	{
		final SignalVehicleCheckStruct dest = (SignalVehicleCheckStruct) data;
		TileIndex tile;

		if (v.type != Vehicle.VEH_Train) return null;

		/* Find the tile outside the tunnel, for signalling */
		if (v.rail.track == 0x40) {
			tile = v.GetVehicleOutOfTunnelTile();
		} else {
			tile = v.tile;
		}

		/* Wrong tile, or no train? Not a match */
		if (tile != dest.tile) return null;

		/* Are we on the same piece of track? */
		if(0 != (dest.track & (v.rail.track + (v.rail.track << 8)))) return v;

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
			int direction = BitOps.GB(tile.getMap().m5, 0, 2);
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

	static void SetSignalsAfterProc(TrackPathFinder tpf)
	{
		SetSignalsData ssd = tpf.userdata;
		final TrackPathFinderLink link;
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



	static void ChangeSignalStates(SetSignalsData ssd)
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
				tile.getMap().m4 = BitOps.RETSB(tile.getMap().m4, 0, 3, SIGTYPE_PBS);
				tile.MarkTileDirtyByTile();
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
						Global.error("NUM_SSD_STACK too small\n"); /// @todo WTF is this???
					}
				}

				// it changed, so toggle it
				tile.getMap().m2 = m2 ^ bit;
				tile.MarkTileDirtyByTile();
			}
	}


	boolean UpdateSignalsOnSegment(TileIndex tile, byte direction)
	{
		SetSignalsData ssd = new SetSignalsData();
		int result = -1;

		ssd.cur_stack = 0;
		direction >>= 1;

		for(;;) {
			// go through one segment and update all signals pointing into that segment.
			ssd.cur = ssd.pbs_cur = ssd.presignal_exits = ssd.presignal_exits_free = 0;
			ssd.has_presignal = false;
			ssd.has_pbssignal = false;

			FollowTrack(tile, 0xC000 | Global.TRANSPORT_RAIL, direction, (TPFEnumProc)SetSignalsEnumProc, SetSignalsAfterProc, ssd);
			ChangeSignalStates(ssd);

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

	static void SetSignalsOnBothDir(TileIndex tile, int track)
	{
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
			return Landscape.GetPartialZ(ti.x&0xF, ti.y&0xF, th) + z;
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

	static AcceptedCargo GetAcceptedCargo_Track(TileIndex tile)
	{
		AcceptedCargo ac = new AcceptedCargo();
		return ac;
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
		/*TrackBits*/ int  rail;

		old_ground = tile.getMap().m5 & RAIL_TYPE_SPECIAL ? BitOps.GB(tile.getMap().m4, 0, 4) : BitOps.GB(tile.getMap().m2, 0, 4);

		switch (GameOptions._opt.landscape) {
		case Landscape.LT_HILLY:
			if (tile.GetTileZ() > GameOptions._opt.snow_line) { /* convert into snow? */
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
			if( 0 != (tile.getMap().m5 & RAIL_TYPE_SPECIAL)) {
				tile.getMap().m4 = BitOps.RETSB(tile.getMap().m4, 0, 4, new_ground);
			} else {
				tile.getMap().m2 = BitOps.RETSB(tile.getMap().m2, 0, 4, new_ground);
			}
			tile.MarkTileDirtyByTile();
		}
	}


	//static int GetTileTrackStatus_Track(TileIndex tile, TransportType mode)
	static int GetTileTrackStatus_Track(TileIndex tile, int mode)
	{
		byte m5, a;
		int b;
		int ret;

		if (mode != Global.TRANSPORT_RAIL) return 0;

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
			m5 = _train_spec_tracks[m5 & 0x3F];
			ret = (m5 << 8) + m5;
		} else
			return 0;
		return ret;
	}

	static void ClickTile_Track(TileIndex tile)
	{
		if (tile.IsTileDepotType(Global.TRANSPORT_RAIL)) {
			ShowTrainDepotWindow(tile);
		} else if (tile.IsRailWaypoint()) {
			ShowRenameWaypointWindow(GetWaypointByTile(tile));
		}
	}


	private static final /*StringID*/ int signal_type[] = {
			Str.STR_RAILROAD_TRACK_WITH_NORMAL_SIGNALS,
			Str.STR_RAILROAD_TRACK_WITH_PRESIGNALS,
			Str.STR_RAILROAD_TRACK_WITH_EXITSIGNALS,
			Str.STR_RAILROAD_TRACK_WITH_COMBOSIGNALS,
			Str.STR_RAILROAD_TRACK_WITH_PBSSIGNALS,
			Str.STR_NULL, Str.STR_NULL
	};

	static TileDesc GetTileDesc_Track(TileIndex tile)
	{
		TileDesc td = new TileDesc();
		td.owner = tile.GetTileOwner().id;
		switch (GetRailTileType(tile)) {
		case RAIL_TYPE_NORMAL:
			td.str = Str.STR_1021_RAILROAD_TRACK;
			break;

		case RAIL_TYPE_SIGNALS: {

			td.str = signal_type[BitOps.GB(tile.getMap().m4, 0, 3)];
			break;
		}

		case RAIL_TYPE_DEPOT_WAYPOINT:
		default:
			td.str = ((tile.getMap().m5 & RAIL_SUBTYPE_MASK) == RAIL_SUBTYPE_DEPOT) ?
					Str.STR_1023_RAILROAD_TRAIN_DEPOT : Str.STR_LANDINFO_WAYPOINT;
			break;
		}

		return td;
	}

	static void ChangeTileOwner_Track(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		if (!tile.IsTileOwner( old_player.id)) return;

		if (new_player.id != Owner.OWNER_SPECTATOR) {
			tile.SetTileOwner( new_player);
		}	else {
			Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
		}
	}


	static int VehicleEnter_Track(Vehicle v, TileIndex tile, int x, int y)
	{
		byte fract_coord;
		byte fract_coord_leave;
		int dir;
		int length;

		// this routine applies only to trains in depot tiles
		if (v.type != Vehicle.VEH_Train || !tile.IsTileDepotType(Global.TRANSPORT_RAIL)) return 0;

		/* depot direction */
		dir = Depot.GetDepotDirection(tile, Global.TRANSPORT_RAIL);

		/* calculate the point where the following wagon should be activated */
		/* this depends on the length of the current vehicle */
		length = v.rail.cached_veh_length;

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
					PBSClearTrack(v.tile, FIND_FIRST_BIT(v.rail.track));

				v.rail.track = 0x80;
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
					v.rail.track = _depot_track_mask[dir];
					assert(v.rail.track);
				}
			}
		}

		return 0;
	}

	void InitializeRail()
	{
		Global._last_built_train_depot_tile = 0;
	}

	final static TileTypeProcs _tile_type_rail_procs = new TileTypeProcs(
			Rail::DrawTile_Track,						/* draw_tile_proc */
			Rail::GetSlopeZ_Track,					/* get_slope_z_proc */
			Rail::ClearTile_Track,					/* clear_tile_proc */
			Rail::GetAcceptedCargo_Track,		/* get_accepted_cargo_proc */
			Rail::GetTileDesc_Track,				/* get_tile_desc_proc */
			Rail::GetTileTrackStatus_Track,	/* get_tile_track_status_proc */
			Rail::ClickTile_Track,					/* click_tile_proc */
			Rail::AnimateTile_Track,				/* animate_tile_proc */
			Rail::TileLoop_Track,						/* tile_loop_clear */
			Rail::ChangeTileOwner_Track,		/* change_tile_owner_clear */
			null,											/* get_produced_cargo_proc */
			Rail::VehicleEnter_Track,				/* vehicle_enter_tile_proc */
			null,											/* vehicle_leave_tile_proc */
			Rail::GetSlopeTileh_Track			/* get_slope_tileh_proc */
			);














	//static RailType _cur_railtype;
	static int _cur_railtype;

	static boolean _remove_button_clicked;
	static byte _build_depot_direction;
	static byte _waypoint_count = 1;
	static byte _cur_waypoint_type;
	static byte _cur_signal_type;
	static byte _cur_presig_type;
	static boolean _cur_autosig_compl;

	//static final StringID _presig_types_dropdown[] = 
	static final int _presig_types_dropdown[] = 
		{
				Str.STR_SIGNAL_NORMAL,
				Str.STR_SIGNAL_ENTRANCE,
				Str.STR_SIGNAL_EXIT,
				Str.STR_SIGNAL_COMBO,
				Str.STR_SIGNAL_PBS,
				Str.INVALID_STRING_ID.id
		};

	static class _Railstation {
		byte orientation;
		byte numtracks;
		byte platlength;
		boolean dragdrop;
	}

	private static final _Railstation _railstation = new _Railstation();

	//static void HandleStationPlacement(TileIndex start, TileIndex end);
	//static void ShowBuildTrainDepotPicker();
	//static void ShowBuildWaypointPicker();
	//static void ShowStationBuilder();
	//static void ShowSignalBuilder();

	static void CcPlaySound1E(boolean success, TileIndex tile, int p1, int p2)
	{
		//if (success) SndPlayTileFx(SND_20_SPLAT_2, tile);
	}

	static void GenericPlaceRail(TileIndex tile, int cmd)
	{
		Cmd.DoCommandP(tile, _cur_railtype, cmd, Rail::CcPlaySound1E,
				_remove_button_clicked ?
						Cmd.CMD_REMOVE_SINGLE_RAIL | Cmd.CMD_MSG(Str.STR_1012_CAN_T_REMOVE_RAILROAD_TRACK) | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER :
							Cmd.CMD_BUILD_SINGLE_RAIL | Cmd.CMD_MSG(Str.STR_1011_CAN_T_BUILD_RAILROAD_TRACK) | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER
				);
	}

	static void PlaceRail_N(TileIndex tile)
	{
		int cmd = Global._tile_fract_coords.x > Global._tile_fract_coords.y ? 4 : 5;
		GenericPlaceRail(tile, cmd);
	}

	static void PlaceRail_NE(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_FIX_Y);
	}

	static void PlaceRail_E(TileIndex tile)
	{
		int cmd = Global._tile_fract_coords.x + Global._tile_fract_coords.y <= 15 ? 2 : 3;
		GenericPlaceRail(tile, cmd);
	}

	static void PlaceRail_NW(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_FIX_X);
	}

	static void PlaceRail_AutoRail(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_RAILDIRS);
	}

	static void PlaceExtraDepotRail(TileIndex tile, int extra)
	{
		byte b = tile.getMap().m5;

		if (BitOps.GB(b, 6, 2) != RAIL_TYPE_NORMAL >> 6) return;
		if (0 == (b & (extra >> 8))) return;

		Cmd.DoCommandP(tile, _cur_railtype, extra & 0xFF, null, Cmd.CMD_BUILD_SINGLE_RAIL | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER);
	}

	static final int _place_depot_extra[] = {
			0x604,		0x2102,		0x1202,		0x505,
			0x2400,		0x2801,		0x1800,		0x1401,
			0x2203,		0x904,		0x0A05,		0x1103,
	};


	void CcRailDepot(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			int dir = p2;

			//SndPlayTileFx(SND_20_SPLAT_2, tile);
			ViewPort.ResetObjectToPlace();

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
		Cmd.DoCommandP(tile, _cur_railtype, _build_depot_direction, Rail::CcRailDepot,
				Cmd.CMD_BUILD_TRAIN_DEPOT | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_100E_CAN_T_BUILD_TRAIN_DEPOT));
	}

	static void PlaceRail_Waypoint(TileIndex tile)
	{
		if (!_remove_button_clicked) {
			Cmd.DoCommandP(tile, _cur_waypoint_type, 0, 0/*CcPlaySound1E*/, Cmd.CMD_BUILD_TRAIN_WAYPOINT | Cmd.CMD_MSG(Str.STR_CANT_BUILD_TRAIN_WAYPOINT));
		} else {
			Cmd.DoCommandP(tile, 0, 0, 0/*CcPlaySound1E*/, Cmd.CMD_REMOVE_TRAIN_WAYPOINT | Cmd.CMD_MSG(Str.STR_CANT_REMOVE_TRAIN_WAYPOINT));
		}
	}

	void CcStation(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			//SndPlayTileFx(SND_20_SPLAT_2, tile);
			ViewPort.ResetObjectToPlace();
		}
	}

	static void PlaceRail_Station(TileIndex tile)
	{
		if(_remove_button_clicked)
			Cmd.DoCommandP(tile, 0, 0, 0/*CcPlaySound1E*/, Cmd.CMD_REMOVE_FROM_RAILROAD_STATION | Cmd.CMD_MSG(Str.STR_CANT_REMOVE_PART_OF_STATION));
		else if (_railstation.dragdrop) {
			ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_AND_Y_LIMITED);
			VpSetPlaceSizingLimit(Global._patches.station_spread);
		} else {
			// TODO: Custom station selector GUI. Now we just try using first custom station
			// (and fall back to normal stations if it isn't available).
			Cmd.DoCommandP(tile, _railstation.orientation | (_railstation.numtracks<<8) | (_railstation.platlength<<16),_cur_railtype|1<<4, Rail::CcStation,
					Cmd.CMD_BUILD_RAILROAD_STATION | Cmd.CMD_NO_WATER | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_100F_CAN_T_BUILD_RAILROAD_STATION));
		}
	}

	static void GenericPlaceSignals(TileIndex tile)
	{
		int trackstat;
		int i;

		trackstat = (byte)tile.GetTileTrackStatus(Global.TRANSPORT_RAIL);

		if(0 != ((trackstat & 0x30))) // N-S direction
			trackstat = (Global._tile_fract_coords.x <= Global._tile_fract_coords.y) ? 0x20 : 0x10;

		if(0 !=  ((trackstat & 0x0C))) // E-W direction
			trackstat = (Global._tile_fract_coords.x + Global._tile_fract_coords.y <= 15) ? 4 : 8;

		// Lookup the bit index
		i = 0;
		if (trackstat != 0) {
			for (; 0 == (trackstat & 1); trackstat >>= 1) i++;
		}

		if (!_remove_button_clicked) {
			Cmd.DoCommandP(tile, i + (Global._ctrl_pressed ? 8 : 0) +
					(!BitOps.HASBIT(_cur_signal_type, 0) != !Global._ctrl_pressed ? 16 : 0) +
					(_cur_presig_type << 5) ,
					0, Rail::CcPlaySound1E, Cmd.CMD_BUILD_SIGNALS | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_1010_CAN_T_BUILD_SIGNALS_HERE));
		} else {
			Cmd.DoCommandP(tile, i, 0, Rail::CcPlaySound1E,
					Cmd.CMD_REMOVE_SIGNALS | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_1013_CAN_T_REMOVE_SIGNALS_FROM));
		}
	}

	static void PlaceRail_Bridge(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_OR_Y);
	}

	void CcBuildRailTunnel(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			//SndPlayTileFx(SND_20_SPLAT_2, tile);
			ViewPort.ResetObjectToPlace();
		} else {
			ViewPort.SetRedErrorSquare(Global._build_tunnel_endtile);
		}
	}

	static void PlaceRail_Tunnel(TileIndex tile)
	{
		Cmd.DoCommandP(tile, _cur_railtype, 0, Rail::CcBuildRailTunnel,
				Cmd.CMD_BUILD_TUNNEL | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_5016_CAN_T_BUILD_TUNNEL_HERE));
	}

	void PlaceProc_BuyLand(TileIndex tile)
	{
		Cmd.DoCommandP(tile, 0, 0, CcPlaySound1E, Cmd.CMD_PURCHASE_LAND_AREA | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_5806_CAN_T_PURCHASE_THIS_LAND));
	}

	static void PlaceRail_ConvertRail(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_AND_Y | GUI_PlaceProc_ConvertRailArea);
	}

	static void PlaceRail_AutoSignals(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_SIGNALDIRS);
	}

	static void BuildRailClick_N(Window w)
	{
		Gui.HandlePlacePushButton(w, 4, GetRailTypeInfo(_cur_railtype).cursor.rail_ns, 1, Rail::PlaceRail_N);
	}

	static void BuildRailClick_NE(Window w)
	{
		Gui.HandlePlacePushButton(w, 5, GetRailTypeInfo(_cur_railtype).cursor.rail_swne, 1, Rail::PlaceRail_NE);
	}

	static void BuildRailClick_E(Window w)
	{
		Gui.HandlePlacePushButton(w, 6, GetRailTypeInfo(_cur_railtype).cursor.rail_ew, 1, Rail::PlaceRail_E);
	}

	static void BuildRailClick_NW(Window w)
	{
		Gui.HandlePlacePushButton(w, 7, GetRailTypeInfo(_cur_railtype).cursor.rail_nwse, 1, Rail::PlaceRail_NW);
	}

	static void BuildRailClick_AutoRail(Window w)
	{
		Gui.HandlePlacePushButton(w, 8, GetRailTypeInfo(_cur_railtype).cursor.autorail, VHM_RAIL, Rail::PlaceRail_AutoRail);
	}

	static void BuildRailClick_Demolish(Window w)
	{
		Gui.HandlePlacePushButton(w, 9, Sprites.ANIMCURSOR_DEMOLISH, 1, Rail::PlaceProc_DemolishArea);
	}

	static void BuildRailClick_Depot(Window w)
	{
		if (Gui.HandlePlacePushButton(w, 10, GetRailTypeInfo(_cur_railtype).cursor.depot, 1, Rail::PlaceRail_Depot)) {
			ShowBuildTrainDepotPicker();
		}
	}

	static void BuildRailClick_Waypoint(Window w)
	{
		_waypoint_count = GetNumCustomStations(STAT_CLASS_WAYP);
		if (Gui.HandlePlacePushButton(w, 11, Sprite.SPR_CURSOR_WAYPOINT, 1, Rail::PlaceRail_Waypoint) &&
				_waypoint_count > 1) {
			ShowBuildWaypointPicker();
		}
	}

	static void BuildRailClick_Station(Window w)
	{
		if (Gui.HandlePlacePushButton(w, 12, Sprite.SPR_CURSOR_RAIL_STATION, 1, Rail::PlaceRail_Station)) ShowStationBuilder();
	}

	static void BuildRailClick_AutoSignals(Window w)
	{
		if (Gui.HandlePlacePushButton(w, 13, Sprites.ANIMCURSOR_BUILDSIGNALS, ViewPort.VHM_RECT, Rail::PlaceRail_AutoSignals))
			ShowSignalBuilder();
	}

	static void BuildRailClick_Bridge(Window w)
	{
		Gui.HandlePlacePushButton(w, 14, Sprite.SPR_CURSOR_BRIDGE, 1, Rail::PlaceRail_Bridge);
	}

	static void BuildRailClick_Tunnel(Window w)
	{
		Gui.HandlePlacePushButton(w, 15, GetRailTypeInfo(_cur_railtype).cursor.tunnel, 3, Rail::PlaceRail_Tunnel);
	}

	static void BuildRailClick_Remove(Window w)
	{
		if (BitOps.HASBIT(w.disabled_state, 16)) return;
		w.SetWindowDirty();
		//SndPlayFx(SND_15_BEEP);

		w.click_state = BitOps.RETTOGGLEBIT(w.click_state, 16);
		_remove_button_clicked = BitOps.HASBIT(w.click_state, 16);
		ViewPort.SetSelectionRed(BitOps.HASBIT(w.click_state, 16));

		// handle station builder
		if (BitOps.HASBIT(w.click_state, 16)) {
			if (_remove_button_clicked) {
				ViewPort.SetTileSelectSize(1, 1);
			} else {
				Window.BringWindowToFrontById(Window.WC_BUILD_STATION, 0);
			}
		}
	}

	static void BuildRailClick_Convert(Window w)
	{
		Gui.HandlePlacePushButton(w, 17, GetRailTypeInfo(_cur_railtype).cursor.convert, 1, PlaceRail_ConvertRail);
	}

	static void BuildRailClick_Landscaping(Window w)
	{
		Terraform.ShowTerraformToolbar();
	}

	static void DoRailroadTrack(int mode)
	{
		Cmd.DoCommandP(TileIndex.TileVirtXY(_thd.selstart.x, _thd.selstart.y), TileIndex.TileVirtXY(_thd.selend.x, _thd.selend.y).getTile(), _cur_railtype | (mode << 4), null,
				_remove_button_clicked ?
						Cmd.CMD_REMOVE_RAILROAD_TRACK | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1012_CAN_T_REMOVE_RAILROAD_TRACK) :
							Cmd.CMD_BUILD_RAILROAD_TRACK  | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1011_CAN_T_BUILD_RAILROAD_TRACK)
				);
	}

	static void HandleAutodirPlacement()
	{
		TileHighlightData thd = _thd;
		int trackstat = thd.drawstyle & 0xF; // 0..5

		if(0!=(thd.drawstyle & ViewPort.HT_RAIL)) { // one tile case
			GenericPlaceRail(TileIndex.TileVirtXY(thd.selend.x, thd.selend.y), trackstat);
			return;
		}

		DoRailroadTrack(trackstat);
	}

	static void HandleAutoSignalPlacement()
	{
		TileHighlightData thd = _thd;
		byte trackstat = thd.drawstyle & 0xF; // 0..5

		if (thd.drawstyle == ViewPort.HT_RECT) { // one tile case
			GenericPlaceSignals(TileIndex.TileVirtXY(thd.selend.x, thd.selend.y));
			return;
		}

		// Global._patches.drag_signals_density is given as a parameter such that each user in a network
		// game can specify his/her own signal density
		Cmd.DoCommandP(
				TileIndex.TileVirtXY(thd.selstart.x, thd.selstart.y),
				TileIndex.TileVirtXY(thd.selend.x, thd.selend.y),
				(Global._ctrl_pressed ? 1 << 3 : 0) | (trackstat << 4) | (Global._patches.drag_signals_density << 24),
				Rail::CcPlaySound1E,
				_remove_button_clicked ?
						(Cmd.CMD_REMOVE_SIGNAL_TRACK | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1013_CAN_T_REMOVE_SIGNALS_FROM)) 
						:
							(Cmd.CMD_BUILD_SIGNAL_TRACK  | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1010_CAN_T_BUILD_SIGNALS_HERE))
				);


		/* TODO What the hell is this?

		(!BitOps.HASBIT(_cur_signal_type, 0) != !_ctrl_pressed ? 1 << 3 : 0) | 
		(trackstat << 4) |
		(Global._patches.drag_signals_density << 24) | 
		(_cur_autosig_compl ? 2 : 0),
		Rail::CcPlaySound1E,
		(_remove_button_clicked ? Cmd.CMD_REMOVE_SIGNAL_TRACK | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | 
		Cmd.CMD_MSG(Str.STR_1013_CAN_T_REMOVE_SIGNALS_FROM) :
	        Cmd.CMD_BUILD_SIGNAL_TRACK  | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1010_CAN_T_BUILD_SIGNALS_HERE) );
		 */
	}


	//typedef void OnButtonClick(Window w);

	static final OnButtonClick _build_railroad_button_proc[] = {
			Rail::BuildRailClick_N,
			Rail::BuildRailClick_NE,
			Rail::BuildRailClick_E,
			Rail::BuildRailClick_NW,
			Rail::BuildRailClick_AutoRail,
			Rail::BuildRailClick_Demolish,
			Rail::BuildRailClick_Depot,
			Rail::BuildRailClick_Waypoint,
			Rail::BuildRailClick_Station,
			Rail::BuildRailClick_AutoSignals,
			Rail::BuildRailClick_Bridge,
			Rail::BuildRailClick_Tunnel,
			Rail::BuildRailClick_Remove,
			Rail::BuildRailClick_Convert,
			Rail::BuildRailClick_Landscaping,
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
		case WE_PAINT:
			w.disabled_state &= ~(1 << 16);
			if (0 == (w.click_state & ((1<<4)|(1<<5)|(1<<6)|(1<<7)|(1<<8)|(1<<11)|(1<<12)|(1<<13)))) {
				w.disabled_state |= (1 << 16);
				w.click_state &= ~(1<<16);
			}
			w.DrawWindowWidgets();
			break;

		case WE_CLICK:
			if (e.widget >= 4) {
				_remove_button_clicked = false;
				_build_railroad_button_proc[e.widget - 4].accept(w);
			}
			break;

		case WE_KEYPRESS: {
			int i;

			for (i = 0; i < _rail_keycodes.length; i++) {
				if (e.keycode == _rail_keycodes[i]) {
					e.cont = false;
					_remove_button_clicked = false;
					_build_railroad_button_proc[i].accept(w);
					break;
				}
			}
			ViewPort.MarkTileDirty(_thd.pos.x, _thd.pos.y); // redraw tile selection
			break;
		}

		case WE_PLACE_OBJ:
			Global._place_proc.accept(e.tile);
			return;

		case WE_PLACE_DRAG: {
			ViewPort.VpSelectTilesWithMethod(e.pt.x, e.pt.y, e.userdata & 0xF);
			return;
		}

		case WE_PLACE_MOUSEUP:
			if (e.pt.x != -1) {
				TileIndex start_tile = e.starttile;
				TileIndex end_tile = e.tile;

				if (e.userdata == ViewPort.VPM_X_OR_Y) {
					ViewPort.ResetObjectToPlace();
					Bridge.ShowBuildBridgeWindow(start_tile, end_tile, _cur_railtype);
				} else if (e.userdata == ViewPort.VPM_RAILDIRS) {
					boolean old = _remove_button_clicked;
					if (Global._ctrl_pressed) _remove_button_clicked = true;
					HandleAutodirPlacement();
					_remove_button_clicked = old;
				} else if (e.userdata == ViewPort.VPM_SIGNALDIRS) {
					HandleAutoSignalPlacement();
				} else if ((e.userdata & 0xF) == ViewPort.VPM_X_AND_Y) {
					if (Terraform.GUIPlaceProcDragXY(e)) break;

					if ((e.userdata >> 4) == Gui.GUI_PlaceProc_ConvertRailArea >> 4)
						Cmd.DoCommandP(end_tile, start_tile.tile, _cur_railtype, null/*Rail::CcPlaySound10*/, Cmd.CMD_CONVERT_RAIL | Cmd.CMD_MSG(Str.STR_CANT_CONVERT_RAIL));
				} else if (e.userdata == ViewPort.VPM_X_AND_Y_LIMITED) {
					HandleStationPlacement(start_tile, end_tile);
				} else
					DoRailroadTrack(e.userdata & 1);
			}
			break;

		case WE_ABORT_PLACE_OBJ:
			w.UnclickWindowButtons();
			w.SetWindowDirty();

			w = Window.FindWindowById(Window.WC_BUILD_STATION, 0);
			if (w != null) w.as_def_d().close = true;
			w = Window.FindWindowById(Window.WC_BUILD_DEPOT, 0);
			if (w != null) w.as_def_d().close = true;
			w = Window.FindWindowById(Window.WC_BUILD_SIGNALS, 0);
			if (w != null) w.as_def_d().close=true;

			break;

		case WE_PLACE_PRESIZE: {
			TileIndex tile = e.tile;

			Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_AUTO, Cmd.CMD_BUILD_TUNNEL);
			ViewPort.VpSetPresizeRange(tile, Global._build_tunnel_endtile == null ? tile : Global._build_tunnel_endtile);
		} break;

		case WE_DESTROY:
			if (Global._patches.link_terraform_toolbar) Window.DeleteWindowById(Window.WC_SCEN_LAND_GEN, 0);
			break;
		}
	}


	static final Widget _build_rail_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   359,     0,    13, Str.STR_100A_RAILROAD_CONSTRUCTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7,   360,   371,     0,    13, 0x0,     Str.STR_STICKY_BUTTON),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   110,   113,    14,    35, 0x0,			Str.STR_NULL),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    0,     21,    14,    35, 0x4E3,		Str.STR_1018_BUILD_RAILROAD_TRACK),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    22,    43,    14,    35, 0x4E4,		Str.STR_1018_BUILD_RAILROAD_TRACK),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    44,    65,    14,    35, 0x4E5,		Str.STR_1018_BUILD_RAILROAD_TRACK),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    66,    87,    14,    35, 0x4E6,		Str.STR_1018_BUILD_RAILROAD_TRACK),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    88,   109,    14,    35, Sprite.SPR_IMG_AUTORAIL, Str.STR_BUILD_AUTORAIL_TIP),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   114,   135,    14,    35, 0x2BF,		Str.STR_018D_DEMOLISH_BUILDINGS_ETC),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   136,   157,    14,    35, 0x50E,		Str.STR_1019_BUILD_TRAIN_DEPOT_FOR_BUILDING),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   158,   179,    14,    35, Sprite.SPR_IMG_WAYPOINT, Str.STR_CONVERT_RAIL_TO_WAYPOINT_TIP),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   180,   221,    14,    35, 0x512,		Str.STR_101A_BUILD_RAILROAD_STATION),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   222,   243,    14,    35, 0x50B,		Str.STR_101B_BUILD_RAILROAD_SIGNALS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   244,   285,    14,    35, 0xA22,		Str.STR_101C_BUILD_RAILROAD_BRIDGE),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   286,   305,    14,    35, Sprite.SPR_IMG_TUNNEL_RAIL, Str.STR_101D_BUILD_RAILROAD_TUNNEL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   306,   327,    14,    35, 0x2CA,		Str.STR_101E_TOGGLE_BUILD_REMOVE_FOR),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   328,   349,    14,    35, Sprite.SPR_IMG_CONVERT_RAIL, Str.STR_CONVERT_RAIL_TIP),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   350,   371,    14,    35, Sprite.SPR_IMG_LANDSCAPING,	Str.STR_LANDSCAPING_TOOLBAR_TIP),

			//	{   WIDGETS_END},
	};

	static final WindowDesc _build_rail_desc = new WindowDesc(
			640-372, 22, 372, 36,
			Window.WC_BUILD_TOOLBAR,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
			_build_rail_widgets,
			Rail::BuildRailToolbWndProc
			);


	/** Configures the rail toolbar for railtype given
	 * @param railtype the railtype to display
	 * @param w the window to modify
	 */
	static void SetupRailToolbar(/*RailType*/ int railtype, Window  w)
	{
		final RailtypeInfo rti = GetRailTypeInfo(railtype);

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

	void ShowBuildRailToolbar(/*RailType*/ int railtype, int button)
	{
		Window w;

		if (Global._current_player.id == Owner.OWNER_SPECTATOR) return;

		// don't recreate the window if we're clicking on a button and the window exists.
		if (button < 0 || null == (w = Window.FindWindowById(Window.WC_BUILD_TOOLBAR, 0)) || (w.wndproc != Rail::BuildRailToolbWndProc) ) {
			Window.DeleteWindowById(Window.WC_BUILD_TOOLBAR, 0);
			_cur_railtype = railtype;
			w = Window.AllocateWindowDesc(_build_rail_desc);
			SetupRailToolbar(railtype, w);
		}

		_remove_button_clicked = false;
		if (w != null && button >= 0) _build_railroad_button_proc[button].accept(w);
		if (Global._patches.link_terraform_toolbar) ShowTerraformToolbar();
	}

	/* TODO: For custom stations, respect their allowed platforms/lengths bitmasks!
	 * --pasky */

	static void HandleStationPlacement(TileIndex start, TileIndex end)
	{
		int sx = start.TileX();
		int sy = start.TileY();
		int ex = end.TileX();
		int ey = end.TileY();
		int w,h;

		if (sx > ex) intswap(sx,ex);
		if (sy > ey) intswap(sy,ey);
		w = ex - sx + 1;
		h = ey - sy + 1;
		if (!_railstation.orientation) intswap(w,h);

		// TODO: Custom station selector GUI. Now we just try using first custom station
		// (and fall back to normal stations if it isn't available).
		Cmd.DoCommandP(TileIndex.TileXY(sx, sy), _railstation.orientation | (w << 8) | (h << 16), _cur_railtype | 1 << 4, CcStation,
				Cmd.CMD_BUILD_RAILROAD_STATION | Cmd.CMD_NO_WATER | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_100F_CAN_T_BUILD_RAILROAD_STATION));
	}

	static void StationBuildWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int rad;
			int bits;

			if (w.as_def_d().close) return;

			bits = (1<<3) << ( _railstation.orientation);
			if (_railstation.dragdrop) {
				bits |= (1<<19);
			} else {
				bits |= (1<<(5-1)) << (_railstation.numtracks);
				bits |= (1<<(12-1)) << (_railstation.platlength);
			}
			bits |= (1<<20) << (Gui._station_show_coverage);
			w.click_state = bits;

			if (_railstation.dragdrop) {
				ViewPort.SetTileSelectSize(1, 1);
			} else {
				int x = _railstation.numtracks;
				int y = _railstation.platlength;
				if (_railstation.orientation == 0) intswap(x,y);
				if(!_remove_button_clicked)
					ViewPort.SetTileSelectSize(x, y);
			}

			rad = (Global._patches.modified_catchment) ? CA_TRAIN : 4;

			if (Global._station_show_coverage)
				SetTileSelectBigSize(-rad, -rad, 2 * rad, 2 * rad);

			/* Update buttons for correct spread value */
			w.disabled_state = 0;
			for (bits = Global._patches.station_spread; bits < 7; bits++) {
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, bits + 5);
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, bits + 12);
			}

			w.DrawWindowWidgets();

			StationPickerGfx.DrawSprite(39, 42, _cur_railtype, 2);
			StationPickerGfx.DrawSprite(107, 42, _cur_railtype, 3);

			Gfx.DrawStringCentered(74, 15, Str.STR_3002_ORIENTATION, 0);
			Gfx.DrawStringCentered(74, 76, Str.STR_3003_NUMBER_OF_TRACKS, 0);
			Gfx.DrawStringCentered(74, 101, Str.STR_3004_PLATFORM_LENGTH, 0);
			Gfx.DrawStringCentered(74, 141, Str.STR_3066_COVERAGE_AREA_HIGHLIGHT, 0);

			DrawStationCoverageAreaText(2, 166, (int)-1, rad);
		} break;

		case WE_CLICK: {
			switch (e.widget) {
			case 3:
			case 4:
				Global._railstation.orientation = e.widget - 3;
				//SndPlayFx(SND_15_BEEP);
				w.SetWindowDirty();
				break;

			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				Global._railstation.numtracks = (e.widget - 5) + 1;
				Global._railstation.dragdrop = false;
				//SndPlayFx(SND_15_BEEP);
				w.SetWindowDirty();
				break;

			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
				Global._railstation.platlength = (e.widget - 12) + 1;
				Global._railstation.dragdrop = false;
				//SndPlayFx(SND_15_BEEP);
				w.SetWindowDirty();
				break;

			case 19:
				Global._railstation.dragdrop ^= true;
				//SndPlayFx(SND_15_BEEP);
				w.SetWindowDirty();
				break;

			case 20:
			case 21:
				Global._station_show_coverage = e.widget - 20;
				//SndPlayFx(SND_15_BEEP);
				w.SetWindowDirty();
				break;
			}
		} break;

		case WindowEvents.WE_MOUSELOOP:
			if (w.as_def_d().close) {
				w.DeleteWindow();
				return;
			}
			CheckRedrawStationCoverage(w);
			break;

		case WindowEvents.WE_DESTROY:
			if (!w.as_def_d().close) ViewPort.ResetObjectToPlace();
			break;
		}
	}

	static final Widget _station_builder_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,		Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   147,     0,    13, Str.STR_3000_RAIL_STATION_SELECTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   147,    14,   199, 0x0,					Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     7,    72,    26,    73, 0x0,					Str.STR_304E_SELECT_RAILROAD_STATION),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    75,   140,    26,    73, 0x0,					Str.STR_304E_SELECT_RAILROAD_STATION),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    22,    36,    87,    98, Str.STR_00CB_1,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    37,    51,    87,    98, Str.STR_00CC_2,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    52,    66,    87,    98, Str.STR_00CD_3,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    67,    81,    87,    98, Str.STR_00CE_4,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    82,    96,    87,    98, Str.STR_00CF_5,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    97,   111,    87,    98, Str.STR_0335_6,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   112,   126,    87,    98, Str.STR_0336_7,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    22,    36,   112,   123, Str.STR_00CB_1,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    37,    51,   112,   123, Str.STR_00CC_2,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    52,    66,   112,   123, Str.STR_00CD_3,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    67,    81,   112,   123, Str.STR_00CE_4,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    82,    96,   112,   123, Str.STR_00CF_5,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    97,   111,   112,   123, Str.STR_0335_6,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   112,   126,   112,   123, Str.STR_0336_7,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    37,   111,   126,   137, Str.STR_DRAG_DROP, Str.STR_STATION_DRAG_DROP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    14,    73,   152,   163, Str.STR_02DB_OFF, Str.STR_3065_DON_T_HIGHLIGHT_COVERAGE),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    74,   133,   152,   163, Str.STR_02DA_ON, Str.STR_3064_HIGHLIGHT_COVERAGE_AREA),
			//{   WIDGETS_END},
	};

	static final WindowDesc _station_builder_desc = new WindowDesc(
			-1, -1, 148, 200,
			Window.WC_BUILD_STATION,Window.WC_BUILD_TOOLBAR,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_station_builder_widgets,
			Rail::StationBuildWndProc
			);

	static void ShowStationBuilder()
	{
		Window.AllocateWindowDesc(_station_builder_desc);
	}

	static void BuildTrainDepotWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			/*RailType*/ int r;

			w.click_state = (1 << 3) << _build_depot_direction;
			w.DrawWindowWidgets();

			r = _cur_railtype;
			DrawTrainDepotSprite(70, 17, 0, r);
			DrawTrainDepotSprite(70, 69, 1, r);
			DrawTrainDepotSprite( 2, 69, 2, r);
			DrawTrainDepotSprite( 2, 17, 3, r);
			break;
		}

		case WE_CLICK:
			switch (e.widget) {
			case 3:
			case 4:
			case 5:
			case 6:
				_build_depot_direction = (byte) (e.widget - 3);
				//SndPlayFx(SND_15_BEEP);
				w.SetWindowDirty();
				break;
			}
			break;

		case WE_MOUSELOOP:
			if (w.as_def_d().close) w.DeleteWindow();
			return;

		case WE_DESTROY:
			if (!w.as_def_d().close) ViewPort.ResetObjectToPlace();
			break;
		}
	}

	static final Widget _build_depot_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   139,     0,    13, Str.STR_1014_TRAIN_DEPOT_ORIENTATION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   139,    14,   121, 0x0,			Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    17,    66, 0x0,			Str.STR_1020_SELECT_RAILROAD_DEPOT_ORIENTATIO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    69,   118, 0x0,			Str.STR_1020_SELECT_RAILROAD_DEPOT_ORIENTATIO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    69,   118, 0x0,			Str.STR_1020_SELECT_RAILROAD_DEPOT_ORIENTATIO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    17,    66, 0x0,			Str.STR_1020_SELECT_RAILROAD_DEPOT_ORIENTATIO),
			//{   WIDGETS_END},
	};

	static final WindowDesc _build_depot_desc = new WindowDesc(
			-1,-1, 140, 122,
			Window.WC_BUILD_DEPOT,Window.WC_BUILD_TOOLBAR,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_depot_widgets,
			Rail::BuildTrainDepotWndProc
			);

	static void ShowBuildTrainDepotPicker()
	{
		Window.AllocateWindowDesc(_build_depot_desc);
	}


	static void BuildWaypointWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int i;

			w.click_state = (1 << 3) << (_cur_waypoint_type - w.hscroll.pos);
			w.DrawWindowWidgets();

			for (i = 0; i < 5; i++) {
				if (w.hscroll.pos + i < _waypoint_count) {
					WayPoint.DrawWaypointSprite(2 + i * 68, 25, w.hscroll.pos + i, _cur_railtype);
				}
			}
			break;
		}
		case WE_CLICK: {
			switch (e.widget) {
			case 3: case 4: case 5: case 6: case 7:
				_cur_waypoint_type = (byte) (e.widget - 3 + w.hscroll.pos);
				//SndPlayFx(SND_15_BEEP);
				w.SetWindowDirty();
				break;
			}
			break;
		}

		case WE_MOUSELOOP:
			if (w.as_def_d().close) w.DeleteWindow();
			break;

		case WE_DESTROY:
			if (!w.as_def_d().close) ViewPort.ResetObjectToPlace();
			break;
		}
	}

	static final Widget _build_waypoint_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   343,     0,    13, Str.STR_WAYPOINT,Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   343,    14,    91, 0x0, 0),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     3,    68,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    71,   136,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   139,   204,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   207,   272,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   275,   340,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP),

			new Widget( Window.WWT_HSCROLLBAR,   Window.RESIZE_NONE,    7,     1,   343,     80,    91, 0x0, Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			//{    WIDGETS_END},
	};

	static final WindowDesc _build_waypoint_desc = new WindowDesc(
			-1,-1, 344, 92,
			Window.WC_BUILD_DEPOT,Window.WC_BUILD_TOOLBAR,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_waypoint_widgets,
			Rail::BuildWaypointWndProc
			);

	static void ShowBuildWaypointPicker()
	{
		Window w = Window.AllocateWindowDesc(_build_waypoint_desc);
		w.hscroll.cap = 5;
		w.hscroll.count = _waypoint_count;
	}

	static void BuildSignalWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			/* XXX TODO: dont always hide the buttons when more than 2 signal types are available */
			w.hidden_state = (1 << 3) | (1 << 6);

			/* XXX TODO: take into account the scroll position for setting the click state */
			w.click_state = ((1 << 4) << _cur_signal_type) | (_cur_autosig_compl ? 1 << 9 : 0);

			Global.SetDParam(10, _presig_types_dropdown[_cur_presig_type]);
			w.DrawWindowWidgets();

			// Draw the string for current signal type
			Gfx.DrawStringCentered(69, 49, Str.STR_SIGNAL_TYPE_STANDARD + _cur_signal_type, 0);

			// Draw the strings for drag density
			Gfx.DrawStringCentered(69, 60, Str.STR_SIGNAL_DENSITY_DESC, 0);
			Global.SetDParam(0, Global._patches.drag_signals_density);
			Gfx.DrawString( 50, 71, Str.STR_SIGNAL_DENSITY_TILES , 0);

			// Draw the '<' and '>' characters for the decrease/increase buttons
			Gfx.DrawStringCentered(30, 72, Str.STR_6819, 0);
			Gfx.DrawStringCentered(40, 72, Str.STR_681A, 0);

			break;
		}
		case WE_CLICK: {
			switch(e.widget) {
			case 3: case 6: // scroll signal types
				/* XXX TODO: implement scrolling */
				break;
			case 4: case 5: // select signal type
				/* XXX TODO: take into account the scroll position for changing selected type */
				_cur_signal_type = (byte) (e.widget - 4);
				//SndPlayFx(SND_15_BEEP);
				w.SetWindowDirty();
				break;
			case 7: // decrease drag density
				if (Global._patches.drag_signals_density > 1) {
					Global._patches.drag_signals_density--;
					//SndPlayFx(SND_15_BEEP);
					w.SetWindowDirty();
				};
				break;
			case 8: // increase drag density
				if (Global._patches.drag_signals_density < 20) {
					Global._patches.drag_signals_density++;
					//SndPlayFx(SND_15_BEEP);
					w.SetWindowDirty();
				};
				break;
			case 9: // autosignal mode toggle button
				_cur_autosig_compl = !_cur_autosig_compl;
				//SndPlayFx(SND_15_BEEP);
				w.SetWindowDirty();
				break;
			case 10: case 11: // presignal-type dropdown list
				Window.ShowDropDownMenu(w, _presig_types_dropdown, _cur_presig_type, 11, 0, 0);
				break;
			}
		}
		break;
		case WE_DROPDOWN_SELECT: // change presignal type
			_cur_presig_type = e.index;
			w.SetWindowDirty();
			break;


		case WindowEvents.WE_MOUSELOOP:
			if (w.as_def_d().close)
				w.DeleteWindow();
			return;

		case WindowEvents.WE_DESTROY:
			if (!w.as_def_d().close)
				ViewPort.ResetObjectToPlace();
			break;
		}
	}

	static final Widget _build_signal_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX, Window.RESIZE_NONE,    7,    0,   10,    0,   13, Str.STR_00C5                 , Str.STR_018B_CLOSE_WINDOW),
			new Widget(   Window.WWT_CAPTION,  Window.RESIZE_NONE,    7,   11,  139,    0,   13, Str.STR_SIGNAL_SELECTION     , Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,    0,  139,   14,  114, 0x0                      , Str.STR_NULL),
			new Widget(   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,   22,   30,   29,   39, Sprite.SPR_ARROW_LEFT           , Str.STR_SIGNAL_TYPE_TIP),
			new Widget(   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,   43,   64,   24,   45, 0x50B                    , Str.STR_SIGNAL_TYPE_TIP),
			new Widget(   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,   75,   96,   24,   45, Sprite.SPR_SEMA                 , Str.STR_SIGNAL_TYPE_TIP),
			new Widget(   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,  109,  117,   29,   39, Sprite.SPR_ARROW_RIGHT          , Str.STR_SIGNAL_TYPE_TIP),
			new Widget(   Window.WWT_IMGBTN,   Window.RESIZE_NONE,    3,   25,   34,   72,   80, 0x0                      , Str.STR_SIGNAL_DENSITY_TIP),
			new Widget(   Window.WWT_IMGBTN,   Window.RESIZE_NONE,    3,   35,   44,   72,   80, 0x0                      , Str.STR_SIGNAL_DENSITY_TIP),
			new Widget(   Window.WWT_TEXTBTN,  Window.RESIZE_NONE,    7,   20,  119,   84,   95, Str.STR_SIGNAL_COMPLETION    , Str.STR_SIGNAL_COMPLETION_TIP),
			new Widget(   Window.WWT_6,        Window.RESIZE_NONE,    7,   10,  129,   99,  110, Str.STR_SIGNAL_PRESIG_COMBO  , Str.STR_SIGNAL_PRESIG_TIP),
			new Widget(   Window.WWT_CLOSEBOX, Window.RESIZE_NONE,    7,  118,  128,  100,  109, Str.STR_0225                 , Str.STR_SIGNAL_PRESIG_TIP),
			//{   WIDGETS_END},
	};

	static final WindowDesc _build_signal_desc = new WindowDesc(
			-1,-1, 140, 115,
			Window.WC_BUILD_SIGNALS,Window.WC_BUILD_TOOLBAR,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_signal_widgets,
			Rail::BuildSignalWndProc
			);

	static void ShowSignalBuilder()
	{
		_cur_presig_type = 0;
		Window.AllocateWindowDesc(_build_signal_desc);
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




























	/**
	 * Maps a Trackdir to the corresponding TrackdirBits value
	 * /
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
	 * These functions check the validity of Tracks and Trackdirs. assert against
	 * them when convenient.
	 */
	//static  boolean IsValidTrack(/* Track */ int  track) { return track < TRACK_END; }
	//static  boolean IsValidTrackdir(Trackdir trackdir) { return (TrackdirToTrackdirBits(trackdir) & TRACKDIR_BIT_MASK) != 0; }



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
	 * Returns the RailTileType of a given rail tile. (ie normal, with signals,
	 * depot, etc.)
	 * /
	static  RailTileType GetRailTileType(TileIndex tile)
	{
		assert(tile.IsTileType( TileTypes.MP_RAILWAY));
		return RailTileType.get( tile.getMap().m5 & RAIL_TILE_TYPE_MASK );
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
	 * Returns the rail type of the given rail tile (ie rail, mono, maglev).
	 */
	//static  RailType GetRailType(TileIndex tile) { return (RailType)(tile.getMap().m3 & RAILTYPE_MASK); }
	//static int GetRailType(TileIndex tile) { return tile.getMap().m3 & RAILTYPE_MASK; }

	/**
	 * Checks if a rail tile has signals.
	 */
	static boolean HasSignals(TileIndex tile)
	{
		return GetRailTileType(tile) == RAIL_TYPE_SIGNALS;
	}

	/**
	 * Checks if a rail tile has signals.
	 * /
	static  boolean HasSignals(TileIndex tile)
	{
		return GetRailTileType(tile).ordinal() == RAIL_TYPE_SIGNALS;
	}

	/**
	 * Returns the RailTileSubtype of a given rail tile with type
	 * RAIL_TYPE_DEPOT_WAYPOINT
	 */
	//static  RailTileSubtype GetRailTileSubtype(TileIndex tile)
	static int GetRailTileSubtype(TileIndex tile)
	{
		assert(GetRailTileType(tile) == RAIL_TYPE_DEPOT_WAYPOINT);
		return Global._m[tile.getTile()].m5 & RAIL_SUBTYPE_MASK;
	}
	/**
	 * Returns the RailTileSubtype of a given rail tile with type
	 * RAIL_TYPE_DEPOT_WAYPOINT
	 * /
	static  RailTileSubtype GetRailTileSubtype(TileIndex tile)
	{
		assert(GetRailTileType(tile).ordinal() == RAIL_TYPE_DEPOT_WAYPOINT);
		return RailTileSubtype.get(tile.getMap().m5 & RAIL_SUBTYPE_MASK);
	}



	/**
	 * Returns whether this is plain rails, with or without signals. Iow, if this
	 * tiles RailTileType is RAIL_TYPE_NORMAL or RAIL_TYPE_SIGNALS.
	 * /
	static  boolean IsPlainRailTile(TileIndex tile)
	{
		RailTileType rtt = GetRailTileType(tile);
		return rtt == RailTileType.RAIL_TYPE_NORMAL || rtt == RailTileType.RAIL_TYPE_SIGNALS;
	}

	/**
	 * Returns whether this is plain rails, with or without signals. Iow, if this
	 * tiles RailTileType is RAIL_TYPE_NORMAL or RAIL_TYPE_SIGNALS.
	 */
	static  boolean IsPlainRailTile(TileIndex tile)
	{
		//RailTileType 
		int rtt = GetRailTileType(tile);
		return rtt == RAIL_TYPE_NORMAL || rtt == RAIL_TYPE_SIGNALS;
	}


	/**
	 * Returns the tracks present on the given plain rail tile (IsPlainRailTile())
	 */
	//static  TrackBits GetTrackBits(TileIndex tile)
	static int GetTrackBits(TileIndex tile)
	{
		assert(GetRailTileType(tile) == RAIL_TYPE_NORMAL || GetRailTileType(tile) == RAIL_TYPE_SIGNALS);
		return Global._m[tile.getTile()].m5 & TRACK_BIT_MASK;
	}
	/**
	 * Returns the tracks present on the given plain rail tile (IsPlainRailTile())
	 * /
	static  TrackBits GetTrackBits(TileIndex tile)
	{
		assert(GetRailTileType(tile).ordinal() == RAIL_TYPE_NORMAL || GetRailTileType(tile).ordinal() == RAIL_TYPE_SIGNALS);
		return TrackBits.get(tile.getMap().m5 & TRACK_BIT_MASK);
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

	/**
	 * Returns whether the given track is present on the given tile. Tile must be
	 * a plain rail tile (IsPlainRailTile()).
	 */
	//static  boolean HasTrack(TileIndex tile, /* Track */ int  track)
	//	{
	//	assert(IsValidTrack(track));
	//		return BitOps.HASBIT(GetTrackBits(tile).ordinal(), track.ordinal());
	//}

	/*
	 * Functions describing logical relations between Tracks, TrackBits, Trackdirs
	 * TrackdirBits, Direction and DiagDirections.
	 *
	 * TODO: Add #unndefs or something similar to remove the arrays used below
	 * from the global scope and expose direct uses of them.
	 */

	/**
	 * Maps a trackdir to the reverse trackdir.
	 * /
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
	static /*TrackBits*/int TrackToTrackBits(/* Track */int track) 
	{ 
		return 1 << track; 
	}

	/**
	 * Returns the Track that a given Trackdir represents
	 */
	//static  Track TrackdirToTrack(Trackdir trackdir) { return new Track(trackdir.getValue() & 0x7); }
	//static  /*Track*/ int TrackdirToTrack(int trackdir) { return trackdir & 0x7; }

	/**
	 * Returns a Trackdir for the given Track. Since every Track corresponds to
	 * two Trackdirs, we choose the one which points between NE and S.
	 * Note that the actual implementation is quite futile, but this might change
	 * in the future.
	 */
	//static  Trackdir TrackToTrackdir(Track track) { return Trackdir.getFor(track); }
	static int TrackToTrackdir(int /* Track */track) { return track; }

	/**
	 * Returns a TrackdirBit mask that contains the two TrackdirBits that
	 * correspond with the given Track (one for each direction).
	 */
	//static  TrackdirBits TrackToTrackdirBits(Track track) 
	static int TrackToTrackdirBits(/* Track */int track) 
	{ 
		/*Trackdir*/ int td = TrackToTrackdir(track); 
		return TrackdirToTrackdirBits(td) | TrackdirToTrackdirBits(ReverseTrackdir(td));
	}

	/**
	 * Discards all directional information from the given TrackdirBits. Any
	 * Track which is present in either direction will be present in the result.
	 */
	//static  TrackBits TrackdirBitsToTrackBits(TrackdirBits bits) { return bits | (bits >> 8); }
	//static int TrackdirBitsToTrackBits(int bits) { return bits | (bits >> 8); }

	/**
	 * Maps a trackdir to the trackdir that you will end up on if you go straight
	 * ahead. This will be the same trackdir for diagonal trackdirs, but a
	 * different (alternating) one for straight trackdirs
	 */
	//static  Trackdir NextTrackdir(Trackdir trackdir) 
	/*static int NextTrackdir(int trackdir) 
	{
		//final Trackdir _next_trackdir[TRACKDIR_END];
		return _next_trackdir[trackdir];
	}*/

	/**
	 * Maps a track to all tracks that make 90 deg turns with it.
	 */
	//static  TrackBits TrackCrossesTracks(Track track) 
	static int TrackCrossesTracks( /* Track */ int track) 
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
	/*static int TrackExitdirToTrackdir(int track, int diagdir) 
	{
		//final Trackdir _track_exitdir_to_trackdir[TRACK_END][DIAGDIR_END];
		return _track_exitdir_to_trackdir[track][diagdir];
	}*/

	/**
	 * Maps a track and an (4-way) dir to the trackdir that represents the track
	 * with the exit in the given direction.
	 */
	//static  Trackdir TrackEnterdirToTrackdir(Track track, DiagDirection diagdir) 
	/*static int TrackEnterdirToTrackdir(int track, int diagdir) 
	{
		//final Trackdir _track_enterdir_to_trackdir[TRACK_END][DIAGDIR_END];
		return _track_enterdir_to_trackdir[track][diagdir];
	}*/

	/**
	 * Maps a track and a full (8-way) direction to the trackdir that represents
	 * the track running in the given direction.
	 * /
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
	static  /*Trackdir*/ int DiagdirToDiagTrackdir(/*DiagDirection*/ int  diagdir) {
		//extern final Trackdir _dir_to_diag_trackdir[DIAGDIR_END];
		return _dir_to_diag_trackdir[diagdir];
	}
	//final TrackdirBits _exitdir_reaches_trackdirs[DIAGDIR_END];

	/**
	 * Returns all trackdirs that can be reached when entering a tile from a given
	 * (diagonal) direction. This will obviously include 90 degree turns, since no
	 * information is available about the exact angle of entering * /
	//static  TrackdirBits DiagdirReachesTrackdirs(DiagDirection diagdir) 
	static int DiagdirReachesTrackdirs(int diagdir) 
	{ 
		return _exitdir_reaches_trackdirs[diagdir]; 
	}

	/**
	 * Returns all tracks that can be reached when entering a tile from a given
	 * (diagonal) direction. This will obviously include 90 degree turns, since no
	 * information is available about the exact angle of entering * /
	//static  TrackBits DiagdirReachesTracks(DiagDirection diagdir) { return TrackdirBitsToTrackBits(DiagdirReachesTrackdirs(diagdir)); }
	static int DiagdirReachesTracks(int diagdir) 
	{ return TrackdirBitsToTrackBits(DiagdirReachesTrackdirs(diagdir)); }

	/**
	 * Maps a trackdir to the trackdirs that can be reached from it (ie, when
	 * entering the next tile. This will include 90 degree turns!
	 * /
	//static  TrackdirBits TrackdirReachesTrackdirs(Trackdir trackdir) { return _exitdir_reaches_trackdirs[TrackdirToExitdir(trackdir)]; }
	static int TrackdirReachesTrackdirs(int trackdir) { return _exitdir_reaches_trackdirs[TrackdirToExitdir(trackdir)]; }
	/* Note that there is no direct table for this function (there used to be),
	 * but it uses two simpler tables to achieve the result */


	/**
	 * Maps a trackdir to all trackdirs that make 90 deg turns with it.
	 * /
	//static  TrackdirBits TrackdirCrossesTrackdirs(Trackdir trackdir) 
	static int TrackdirCrossesTrackdirs(int trackdir) 
	{
		//final TrackdirBits _track_crosses_trackdirs[TRACKDIR_END];
		return _track_crosses_trackdirs[TrackdirToTrack(trackdir)];
	}

	/**
	 * Maps a (4-way) direction to the reverse.
	 * /
	//static  DiagDirection ReverseDiagdir(DiagDirection diagdir) 
	static int ReverseDiagdir(int diagdir) 
	{
		//final DiagDirection _reverse_diagdir[DIAGDIR_END];
		return _reverse_diagdir[diagdir];
	}

	/**
	 * Maps a (8-way) direction to a (4-way) DiagDirection
	 * /
	//static  DiagDirection DirToDiagdir(Direction dir) 
	static int DirToDiagdir(int dir) 
	{
		assert(dir < DIR_END);
		return (dir >> 1);
	}

	/* Checks if a given Track is diagonal * /
	//static  boolean IsDiagonalTrack(Track track) 
	static  boolean IsDiagonalTrack(int track) 
	{ 
		return (track == TRACK_DIAG1) || (track == TRACK_DIAG2); 
	}

	/* Checks if a given Track is diagonal */
	static  boolean IsDiagonalTrack(/* Track */ int  track) 
	{ 
		return (track == TRACK_DIAG1) || (track == TRACK_DIAG2); 
	}



	/* Checks if a given Trackdir is diagonal. * /
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
	static boolean HasSignalOnTrack(TileIndex tile, /*Track*/ int track)
	{
		assert(IsValidTrack(track));
		return (
				(GetRailTileType(tile) == RAIL_TYPE_SIGNALS) 
				&& ((Global._m[tile.getTile()].m3 & SignalOnTrack(track)) != 0));
	}

	/**
	 * Checks for the presence of signals along the given trackdir on the given
	 * rail tile.
	 *
	 * Along meaning if you are currently driving on the given trackdir, this is
	 * the signal that is facing us (for which we stop when it's red).
	 * /
	static  boolean HasSignalOnTrackdir(TileIndex tile, Trackdir trackdir)
	{
		assert (IsValidTrackdir(trackdir));
		return (GetRailTileType(tile) == RailTileType.RAIL_TYPE_SIGNALS) && (Global._m[tile.getTile()].m3 & SignalAlongTrackdir(trackdir));
	}
	/**
	 * Checks for the presence of signals along the given trackdir on the given
	 * rail tile.
	 *
	 * Along meaning if you are currently driving on the given trackdir, this is
	 * the signal that is facing us (for which we stop when it's red).
	 * /
	//static  boolean HasSignalOnTrackdir(TileIndex tile, Trackdir trackdir)
	static  boolean HasSignalOnTrackdir(TileIndex tile, int trackdir)
	{
		assert (IsValidTrackdir(trackdir));
		return (GetRailTileType(tile) == RAIL_TYPE_SIGNALS) 
				&& (tile.getMap().m3 & SignalAlongTrackdir(trackdir));
	}

	/**
	 * Gets the state of the signal along the given trackdir.
	 *
	 * Along meaning if you are currently driving on the given trackdir, this is
	 * the signal that is facing us (for which we stop when it's red).
	 * /
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
	 * Gets the state of the signal along the given trackdir.
	 *
	 * Along meaning if you are currently driving on the given trackdir, this is
	 * the signal that is facing us (for which we stop when it's red).
	 */
	//static  SignalState GetSignalState(TileIndex tile, Trackdir trackdir)
	static int GetSignalState(TileIndex tile, int trackdir)
	{
		assert(IsValidTrackdir(trackdir));
		assert(HasSignalOnTrack(tile, TrackdirToTrack(trackdir)));
		return ((tile.getMap().m2 & SignalAlongTrackdir(trackdir)) != 0
				?SIGNAL_STATE_GREEN:SIGNAL_STATE_RED);
	}


	/**
	 * Gets the type of signal on a given track on a given rail tile with signals.
	 *
	 * Note that currently, the track argument is not used, since
	 * signal types cannot be mixed. This function is trying to be
	 * future-compatible, though.
	 * /
	static  /*SignalType* / int GetSignalType(TileIndex tile, /*Track* / int track)
	{
		assert(IsValidTrack(track));
		assert(GetRailTileType(tile) == RAIL_TYPE_SIGNALS);
		return Global._m[tile.getTile()].m4 & SIGTYPE_MASK;
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
	static boolean HasSemaphores(TileIndex tile, /* Track */ int  track)
	{
		assert(IsValidTrack(track));
		return 0 != (tile.getMap().m4 & SIG_SEMAPHORE_MASK);
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
	static  /*TransportType*/ int GetCrossingTransportType(TileIndex tile, /* Track */ int  track)
	{
		/* XXX: Nicer way to write this? */
		switch(track)
		{
		/* When map5 bit 3 is set, the road runs in the y direction (DIAG2) */
		case TRACK_DIAG1:
			return (BitOps.HASBIT(tile.getMap().m5, 3) ? Global.TRANSPORT_RAIL : Global.TRANSPORT_ROAD);
		case TRACK_DIAG2:
			return (BitOps.HASBIT(tile.getMap().m5, 3) ? Global.TRANSPORT_ROAD : Global.TRANSPORT_RAIL);
		default:
			assert false;
		}
		return Global.INVALID_TRANSPORT;
	}

	

	/** Returns the "best" railtype a player can build.
	  * As the AI doesn't know what the BEST one is, we
	  * have our own priority list here. When adding
	  * new railtypes, modify this function
	  * @param p the player "in action"
	  * @return The "best" railtype a player has available
	  */
	public static /*RailType*/ int GetBestRailtype(Player p) {
			if (p.HasRailtypeAvail(RAILTYPE_MAGLEV)) return RAILTYPE_MAGLEV;
			if (p.HasRailtypeAvail(RAILTYPE_MONO)) return RAILTYPE_MONO;
			return RAILTYPE_RAIL;
	}

	/**
	 * Returns a pointer to the Railtype information for a given railtype
	 * @param railtype the rail type which the information is requested for
	 * @return The pointer to the RailtypeInfo
	 * /
	static  final RailtypeInfo GetRailTypeInfo(/* RailType * / int railtype)
	{
		assert(railtype < RAILTYPE_END);
		return _railtypes[railtype];
	}

	/**
	 * Checks if an engine of the given RailType can drive on a tile with a given
	 * RailType. This would normally just be an equality check, but for electric
	 * rails (which also support non-electric engines).
	 * @return Whether the engine can drive on this tile.
	 * @param  enginetype The RailType of the engine we are considering.
	 * @param  tiletype   The RailType of the tile we are considering.
	 * /
	static  boolean IsCompatibleRail(/* RailType * / int enginetype, /* RailType * / int tiletype)
	{
		return BitOps.HASBIT(GetRailTypeInfo(enginetype).compatible_railtypes, tiletype);
	}

	/**
	 * Checks if the given tracks overlap, ie form a crossing. Basically this
	 * means when there is more than one track on the tile, exept when there are
	 * two parallel tracks.
	 * @param  bits The tracks present.
	 * @return Whether the tracks present overlap in any way.
	 * /
	//static  boolean TracksOverlap(TrackBits bits)
	static boolean TracksOverlap(int bits)
	{
		// With no, or only one track, there is no overlap 
		if (bits == 0 || BitOps.KILL_FIRST_BIT(bits) == 0)
			return false;
		// * We know that there are at least two tracks present. When there are more
		// * than 2 tracks, they will surely overlap. When there are two, they will
		// * always overlap unless they are lower & upper or right & left. 
		if ((
				bits == (TRACK_BIT_UPPER | TRACK_BIT_LOWER)) || 
				(bits == (TRACK_BIT_LEFT | TRACK_BIT_RIGHT)))
			return false;
		return true;
	} */








}



//typedef void DetailedTrackProc(final TileInfo ti);

@FunctionalInterface
interface DetailedTrackProc extends Consumer<TileInfo> {}
