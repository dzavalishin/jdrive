package com.dzavalishin.game;

import java.util.Map.Entry;
import java.util.function.Consumer;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.game.TrackPathFinder.TPFHashEnt;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.StationClassID;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.struct.DrawTileSeqStruct;
import com.dzavalishin.struct.DrawTileSprites;
import com.dzavalishin.struct.FindLengthOfTunnelResult;
import com.dzavalishin.struct.Point;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.tables.RailTables;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Sound;
import com.dzavalishin.util.Sprites;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.Gui;
import com.dzavalishin.xui.TrainGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

@SuppressWarnings("SuspiciousNameCombination")
public class Rail extends RailTables {

	public static final int NUM_SSD_ENTRY = RailtypeInfo.NUM_SSD_ENTRY;
	public static final int NUM_SSD_STACK = RailtypeInfo.NUM_SSD_STACK;// max amount of blocks to check recursively

	//private static final TileHighlightData _thd = new TileHighlightData();

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
	 *
	 * @apiNote  Something more flexible might be desirable in the future.
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
	public static  /*TrackdirBits*/ int  TrackdirToTrackdirBits(/*Trackdir*/ int trackdir) 
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
	public static  int SignalAlongTrackdir(/*Trackdir*/ int trackdir) {return  _signal_along_trackdir[trackdir];}

	/**
	 * Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction against the trackdir.
	 */
	public static  int SignalAgainstTrackdir(/*Trackdir*/ int trackdir) {
		//extern final byte _signal_against_trackdir[TRACKDIR_END];
		return  _signal_against_trackdir[trackdir];
	}

	/**
	 * Maps a Track to the bits that store the status of the two signals that can
	 * be present on the given track.
	 */
	public static  int SignalOnTrack(/* Track */ int  track) {
		//extern final byte _signal_on_track[TRACK_END];
		return  _signal_on_track[track];
	}

	/*
	 * Some functions to query rail tiles
	 */









	/*
	 *
	 * Functions describing logical relations between Tracks, TrackBits, Trackdirs
	 * TrackdirBits, Direction and DiagDirections.
	 *
	 */

	/**
	 * Maps a trackdir to the reverse trackdir.
	 */
	public static  /*Trackdir*/ int ReverseTrackdir(/*Trackdir*/ int trackdir) {
		//extern final Trackdir _reverse_trackdir[TRACKDIR_END];
		return _reverse_trackdir[trackdir];
	}


	/**
	 * Returns the Track that a given Trackdir represents
	 */
	public static  /* Track */ int  TrackdirToTrack(/*Trackdir*/ int trackdir) { return (trackdir & 0x7); }



	/**
	 * Discards all directional information from the given TrackdirBits. Any
	 * Track which is present in either direction will be present in the result.
	 */
	public static  /*TrackBits*/ int  TrackdirBitsToTrackBits(/*TrackdirBits*/ int  bits) { return bits | (bits >> 8); }

	/**
	 * Maps a trackdir to the trackdir that you will end up on if you go straight
	 * ahead. This will be the same trackdir for diagonal trackdirs, but a
	 * different (alternating) one for straight trackdirs
	 */
	public static  /*Trackdir*/ int NextTrackdir(/*Trackdir*/ int trackdir) {
		//extern final Trackdir _next_trackdir[TRACKDIR_END];
		return _next_trackdir[trackdir];
	}


	/**
	 * Maps a trackdir to the (4-way) direction the tile is exited when following
	 * that trackdir.
	 */
	public static  /*DiagDirection*/ int  TrackdirToExitdir(/*Trackdir*/ int trackdir) {
		return _trackdir_to_exitdir[trackdir];
	}

	/**
	 * Maps a track and an (4-way) dir to the trackdir that represents the track
	 * with the exit in the given direction.
	 */
	public static  /*Trackdir*/ int TrackExitdirToTrackdir(/* Track */ int  track, /*DiagDirection*/ int  diagdir) {
		//extern final Trackdir _track_exitdir_to_trackdir[TRACK_END][DIAGDIR_END];
		return _track_exitdir_to_trackdir[track][diagdir];
	}

	/**
	 * Maps a track and an (4-way) dir to the trackdir that represents the track
	 * with the exit in the given direction.
	 */
	public static  /*Trackdir*/ int TrackEnterdirToTrackdir(/* Track */ int  track, /*DiagDirection*/ int  diagdir) {
		//extern final Trackdir _track_enterdir_to_trackdir[TRACK_END][DIAGDIR_END];
		return _track_enterdir_to_trackdir[track][diagdir];
	}

	/**
	 * Maps a track and a full (8-way) direction to the trackdir that represents
	 * the track running in the given direction.
	 */
	public static  /*Trackdir*/ int TrackDirectionToTrackdir(/* Track */ int  track, /*Direction*/ int dir) {
		//extern final Trackdir _track_direction_to_trackdir[TRACK_END][DIR_END];
		return _track_direction_to_trackdir[track][dir];
	}



	//extern final TrackdirBits _exitdir_reaches_trackdirs[DIAGDIR_END];

	/**
	 * Returns all trackdirs that can be reached when entering a tile from a given
	 * (diagonal) direction. This will obviously include 90 degree turns, since no
	 * information is available about the exact angle of entering */
	public static  /*TrackdirBits*/ int  DiagdirReachesTrackdirs(/*DiagDirection*/ int  diagdir) { return _exitdir_reaches_trackdirs[diagdir]; }

	/**
	 * Returns all tracks that can be reached when entering a tile from a given
	 * (diagonal) direction. This will obviously include 90 degree turns, since no
	 * information is available about the exact angle of entering */
	public static  /*TrackBits*/ int  DiagdirReachesTracks(/*DiagDirection*/ int  diagdir) { return TrackdirBitsToTrackBits(DiagdirReachesTrackdirs(diagdir)); }

	/**
	 * Maps a trackdir to the trackdirs that can be reached from it (ie, when
	 * entering the next tile. This will include 90 degree turns!
	 */
	public static  /*TrackdirBits*/ int  TrackdirReachesTrackdirs(/*Trackdir*/ int trackdir) { return _exitdir_reaches_trackdirs[TrackdirToExitdir(trackdir)]; }
	/* Note that there is no direct table for this function (there used to be),
	 * but it uses two simpeler tables to achieve the result */


	/**
	 * Maps a trackdir to all trackdirs that make 90 deg turns with it.
	 */
	public static  /*TrackdirBits*/ int  TrackdirCrossesTrackdirs(/*Trackdir*/ int trackdir) {
		//extern final TrackdirBits _track_crosses_trackdirs[TRACKDIR_END];
		return _track_crosses_trackdirs[TrackdirToTrack(trackdir)];
	}

	/**
	 * Maps a (4-way) direction to the reverse.
	 */
	public static  /*DiagDirection*/ int  ReverseDiagdir(/*DiagDirection*/ int  diagdir) {
		//extern final DiagDirection _reverse_diagdir[DIAGDIR_END];
		return _reverse_diagdir[diagdir];
	}

	/**
	 * Maps a (8-way) direction to a (4-way) DiagDirection
	 */
	public static  /*DiagDirection*/ int  DirToDiagdir(/*Direction*/ int dir) {
		assert(dir < TileIndex.DIR_END);
		return (dir >> 1);
	}


	/* Checks if a given Trackdir is diagonal. */
	public static  boolean IsDiagonalTrackdir(/*Trackdir*/ int trackdir) { return IsDiagonalTrack(TrackdirToTrack(trackdir)); }

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
	public static  boolean HasSignalOnTrackdir(TileIndex tile, /*Trackdir*/ int trackdir)
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
	public static  /*SignalType*/ int GetSignalType(TileIndex tile, /* Track */ int  track)
	{
		assert(IsValidTrack(track));
		assert(GetRailTileType(tile) == RAIL_TYPE_SIGNALS);
		/*SignalType*/
		return tile.getMap().m4 & SIGTYPE_MASK;
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
	public static RailtypeInfo GetRailTypeInfo(/*RailType*/ int railtype)
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
	public static  boolean IsCompatibleRail(/*RailType*/ int enginetype, /*RailType*/ int tiletype)
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
	public static  boolean TracksOverlap(/*TrackBits*/ int  bits)
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







	public /*RailType*/ static int GetTileRailType(TileIndex tile, /*Trackdir*/ int trackdir)
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

	public static boolean CheckTrackCombination(TileIndex tile, /*TrackBits*/ int  to_build, int flags)
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



	public static int GetRailFoundation(int tileh, int bits)
	{

		if (((~_valid_tileh_slopes[0][tileh]) & bits) == 0)
			return 0;

		if (((~_valid_tileh_slopes[1][tileh]) & bits) == 0)
			return tileh;

		//if ( ((i=0, tileh == 1) || (i+=2, tileh == 2) || (i+=2, tileh == 4) || (i+=2, tileh == 8) ) && (bits == TRACK_BIT_DIAG1 || (i++, bits == TRACK_BIT_DIAG2)))
		//	return i + 15;

		int i = 0;
		if(tileh != 1)
		{
			i+=2;
			if(tileh != 2)
			{
				i+=2;
				if(tileh != 4)
				{
					i+=2;
					if( tileh != 8) return 0;
				}				
			}			
		}

		if(bits == TRACK_BIT_DIAG1) return 15+i;
		if(bits == TRACK_BIT_DIAG2) return 15+i+1;
		
		return 0;
	}


	public static int CheckRailSlope(int tileh, /*TrackBits*/ int  rail_bits, /*TrackBits*/ int  existing, TileIndex tile)
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
				} else if (!Global._patches.build_on_slopes || Global.gs._is_old_ai_player) {
					return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
				} else {
					return (int) Global._price.terraform;
				}
			}
		}
		return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
	}

	/* Validate functions for rail building */
	public static  boolean ValParamTrackOrientation(/* Track */ int  track) {return IsValidTrack(track);}

	/** Build a single piece of rail
	 * @param x,y coordinates on where to build
	 * @param p1 railtype of being built piece (normal, mono, maglev)
	 * @param p2 rail track to build
	 */
	public static int CmdBuildSingleRail(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile;
		int tileh;
		int m5; /* Used only as a cache, should probably be removed? */
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
					tile.SetTileOwner( PlayerID.getCurrent());
					tile.getMap().m3 = BitOps.RETSB(tile.getMap().m3, 0, 4, p1);
					tile.getMap().m5 = ((m5 & 0xC7) | 0x20); // railroad under bridge
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
					!tile.IsTileOwner( PlayerID.getCurrent()) ||
					BitOps.GB(tile.getMap().m3, 0, 4) != p1) {
				// Get detailed error message
				return Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
			}

			ret = CheckRailSlope(tileh, trackbit, GetTrackBits(tile), tile);
			if (Cmd.CmdFailed(ret)) return ret;
			cost += ret;

			if(0 != (flags & Cmd.DC_EXEC)) {
				tile.getMap().m2 &= ~RAIL_MAP2LO_GROUND_MASK; // Bare land
				tile.getMap().m5 = 0xFF & (m5 | trackbit);
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
				if(0 != (flags & Cmd.DC_EXEC)) { // crossing
					tile.getMap().m3 = 0xFF & tile.GetTileOwner().id; // road owner
					tile.SetTileOwner(PlayerID.getCurrent()); // rail owner
					tile.getMap().m4 = 0xFF & p1;
					tile.getMap().m5 = 0xFF & (0x10 | (track == TRACK_DIAG1 ? 0x08 : 0x00)); // level crossing
				}
				break;
			}

			if (tile.IsLevelCrossing() && (0 != (m5 & 0x08) ? TRACK_DIAG1 : TRACK_DIAG2) == track)
				return Cmd.return_cmd_error(Str.STR_1007_ALREADY_BUILT);
			/* FALLTHROUGH */

		default:
			ret = CheckRailSlope(tileh, trackbit, 0, tile);
			if (Cmd.CmdFailed(ret)) return ret;
			cost += ret;

			ret = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
			if (Cmd.CmdFailed(ret)) return ret;
			cost += ret;

			if(0 != (flags & Cmd.DC_EXEC)) {
				tile.SetTileType(TileTypes.MP_RAILWAY);
				tile.SetTileOwner(PlayerID.getCurrent());
				tile.getMap().m2 = 0; // Bare land
				tile.getMap().m3 = 0xFF & p1; // No signals, rail type
				tile.getMap().m5 = 0xFF & trackbit;
			}
			break;
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			tile.MarkTileDirtyByTile();
			SetSignalsOnBothDir(tile, track);
		}

		return (int) (cost + Global._price.build_rail);
	}



	/** Remove a single piece of track
	 * @param x,y coordinates for removal of track
	 * @param p1 unused
	 * @param p2 rail orientation
	 */
	public static int CmdRemoveSingleRail(int x, int y, int flags, int p1, int p2)
	{
		/* Track */ int  track = p2;
		/*TrackBits*/ int  trackbit;
		TileIndex tile;
		byte m5;
		int cost = (int) Global._price.remove_rail;

		if (!ValParamTrackOrientation(p2)) return Cmd.CMD_ERROR;
		trackbit = TrackToTrackBits(track);

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		tile = TileIndex.TileVirtXY(x, y);

		if (!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && !tile.IsTileType( TileTypes.MP_STREET) && !tile.IsTileType( TileTypes.MP_RAILWAY))
			return Cmd.CMD_ERROR;

		if (!PlayerID.getCurrent().isWater() && !Player.CheckTileOwnership(tile))
			return Cmd.CMD_ERROR;

		// allow building rail under bridge
		if (!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && !tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		switch(tile.GetTileType())
		{
		case MP_TUNNELBRIDGE:
			if (!Vehicle.EnsureNoVehicleZ(tile, tile.TilePixelHeight()))
				return Cmd.CMD_ERROR;

			if ((tile.getMap().m5 & 0xF8) != 0xE0)
				return Cmd.CMD_ERROR;

			if (( 0 != (tile.getMap().m5 & 1) ? TRACK_BIT_DIAG1 : TRACK_BIT_DIAG2) != trackbit)
				return Cmd.CMD_ERROR;

			if (0==(flags & Cmd.DC_EXEC))
				return (int) Global._price.remove_rail;

			tile.SetTileOwner( Owner.OWNER_NONE);
			tile.getMap().m5 = tile.getMap().m5 & 0xC7;
			break;

		case MP_STREET:
			if (!tile.IsLevelCrossing()) return Cmd.CMD_ERROR;

			/* This is a crossing, let's check if the direction is correct */
			if(0 != (tile.getMap().m5 & 8)) {
				m5 = 5;
				if (track != TRACK_DIAG1)
					return Cmd.CMD_ERROR;
			} else {
				m5 = 10;
				if (track != TRACK_DIAG2)
					return Cmd.CMD_ERROR;
			}

			if (0==(flags & Cmd.DC_EXEC))
				return (int) Global._price.remove_rail;

			tile.getMap().m5 = m5;
			tile.SetTileOwner( tile.getMap().m3);
			tile.getMap().m2 = 0;
			break;

		case MP_RAILWAY:
			if (!IsPlainRailTile(tile))
				return Cmd.CMD_ERROR;

			/* See if the track to remove is actually there */
			if (0==(GetTrackBits(tile) & trackbit))
				return Cmd.CMD_ERROR;

			/* Charge extra to remove signals on the track, if they are there */
			if (HasSignalOnTrack(tile, track))
				cost += Cmd.DoCommand(x, y, track, 0, flags, Cmd.CMD_REMOVE_SIGNALS);

			if (0==(flags & Cmd.DC_EXEC))
				return cost;

			/* We remove the trackbit here. */
			tile.getMap().m5 &= ~trackbit;

			/* Unreserve track for PBS */
			if(0 != (Pbs.PBSTileReserved(tile) & trackbit) )
				Pbs.PBSClearTrack(tile, track);

			if (GetTrackBits(tile)  == 0) {
				/* The tile has no tracks left, it is no longer a rail tile */
				Landscape.DoClearSquare(tile);

				//goto skip_mark_dirty;
				SetSignalsOnBothDir(tile, track);

				return cost;
			}
			break;

		default:
			assert false;
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
	public static int ValidateAutoDrag(/*Trackdir*/ int [] trackdir, int x, int y, int ex, int ey)
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
	public static int CmdRailTrackHelper(int x, int y, int flags, int p1, int p2)
	{
		int ex, ey;
		int ret, total_cost = 0;
		/* Track */ int  track = BitOps.GB(p2, 4, 3);
		/*Trackdir*/ int trackdir;
		int mode =  (BitOps.HASBIT(p2, 7) ? 1 : 0);
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
		if(0!=(flags & Cmd.DC_EXEC)) Sound.SndPlayTileFx(Snd.SND_20_SPLAT_2, TileIndex.TileVirtXY(x, y));

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
	public static int CmdBuildRailroadTrack(int x, int y, int flags, int p1, int p2)
	{
		return CmdRailTrackHelper(x, y, flags, p1, BitOps.RETCLRBIT(p2, 7));
	}

	/** Build rail on a stretch of track.
	 * Stub for the unified rail builder/remover
	 * @see CmdRailTrackHelper
	 */
	public static int CmdRemoveRailroadTrack(int x, int y, int flags, int p1, int p2)
	{
		return CmdRailTrackHelper(x, y, flags, p1, BitOps.RETSETBIT(p2, 7));
	}

	/** Build a train depot
	 * @param x,y position of the train depot
	 * @param p1 rail type
	 * @param p2 depot direction (0 through 3), where 0 is NE, 1 is SE, 2 is SW, 3 is NW
	 *
	 * TODO When checking for the tile slope,
	 * distinguish between "Flat land required" and "land sloped in wrong direction"
	 */
	public static int CmdBuildTrainDepot(int x, int y, int flags, int p1, int p2)
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
				Global.gs._is_old_ai_player ||
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

			Landscape.ModifyTile(tile, TileTypes.MP_RAILWAY,
					//TileTypes.MP_SETTYPE(TileTypes.MP_RAILWAY) |
					TileTypes.MP_MAP3LO | TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5,
					p1, /* map3_lo */
					p2 | RAIL_TYPE_DEPOT_WAYPOINT /* map5 */
					);

			d.xy = tile;
			d.town_index = Town.ClosestTownFromTile(tile, -1).index;

			SetSignalsOnBothDir(tile, (p2&1) != 0 ? 2 : 1);

		}

		return (int) (cost + Global._price.build_train_depot);
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
	public static int CmdBuildSingleSignal(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		boolean semaphore;
		boolean pre_signal_cycle;
		int pre_signal_type;
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
			cost = (int) Global._price.build_signals;
		} else {
			if (p2 != 0 && semaphore != HasSemaphores(tile, track)) {
				// convert signals <. semaphores
				cost = (int) (Global._price.build_signals + Global._price.remove_signals);
			} else {
				// it is free to change orientation/pre-exit-combo signals
				cost = 0;
			}
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			if (GetRailTileType(tile) != RAIL_TYPE_SIGNALS) {
				// there are no signals at all on this tile yet
				tile.getMap().m5 |= RAIL_TYPE_SIGNALS; // change into signals
				tile.getMap().m2 |= 0xF0;              // all signals are on
				tile.getMap().m3 &= ~0xF0;          // no signals built by default
				tile.getMap().m4 =  ((semaphore ? 0x08 : 0) + pre_signal_type);
			}

			if (p2 == 0) {
				if (!HasSignalOnTrack(tile, track)) {
					// build new signals
					tile.getMap().m3 |= SignalOnTrack(track);
				} else {
					if (pre_signal_cycle) {
						// cycle between normal . pre . exit . combo . pbs ....
						int type = ((GetSignalType(tile, track) + 1) % 5);
						tile.getMap().m4 =  BitOps.RETSB(tile.getMap().m4, 0, 3, type);
					} else {
						// cycle between two-way . one-way . one-way . ...
						/* TODO: Rewrite switch into something more general */
						switch (track) {
						case TRACK_LOWER:
						case TRACK_RIGHT: {
							int signal = (tile.getMap().m3 - 0x10) & 0x30;
							if (signal == 0) signal = 0x30;
							tile.getMap().m3 &= ~0x30;
							tile.getMap().m3 |= signal;
							break;
						}

						default: {
							int signal = (tile.getMap().m3 - 0x40) & 0xC0;
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
					tile.getMap().m4 = BitOps.RETSETBIT(tile.getMap().m4, 3);
				} else {
					tile.getMap().m4 = BitOps.RETCLRBIT(tile.getMap().m4, 3);
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
	public static int BuildAutoSignals(int x, int y, /*Trackdir*/ int trackdir, int flags, int p2, int signals)
	{
		int signal_density =  (p2 >> 24);
		int signal_ctr = signal_density * 2;
		int signal_dir = 0;	// direction in which signals are placed 1=forward  2=backward  3=twoway
		int track_mode = 0;	// 128=bridge, 64=tunnel, 192=end of tunnel/bridge, 0=normal track
		int track_height = 0; // height of tunnel currently in
		int retr, total_cost = 0;
		TileIndex tile = TileIndex.TileVirtXY/*TILE_FROM_XY*/(x, y);
		int m5 = tile.getMap().m5;
		int m3 = tile.getMap().m3;
		int semaphores =  ((tile.getMap().m4 & ~3) != 0 ? 16 : 0);
		int mode = p2 & 0x1;
		int lx, ly;
		int dir;


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

		semaphores =  (HasSemaphores(tile, TrackdirToTrack(trackdir)) ? 16 : 0); // copy signal/semaphores style (independent of CTRL)

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
					track_height =  Landscape.GetSlopeZ(x+8, y+8);
				}
			}

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
					}
				}
				continue;
			}

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
			if (mode != 0) {
				boolean ctr1 = ((m5 & RAIL_TILE_TYPE_MASK) == RAIL_TYPE_SIGNALS);
				boolean ctr2 = 0 != (m3 & _signals_table_both[trackdir]) ;
				signal_ctr =(ctr1 && ctr2) ? 0 : 1;
			} else if(0 != (m5 & 0x3) )
				// count faster on diagonal tracks
				signal_ctr -= 2;
			else
				signal_ctr -= 1;

			if (signal_ctr <= 0) {
				signal_ctr += signal_density * 2;
				// Place Signal
				retr = Cmd.DoCommand(lx, ly, (trackdir & 7) | semaphores, signals , flags, (mode == 1) ? Cmd.CMD_REMOVE_SIGNALS : Cmd.CMD_BUILD_SIGNALS);
				if (retr == Cmd.CMD_ERROR) return Cmd.CMD_ERROR;
				total_cost += retr;
				signals = 0;
			}

			// when removing signals, the last position is always handled
			if (0 != mode) signals = 0;

		}


	}



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
		int signals;
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
			signals =  (tile.getMap().m3 & SignalOnTrack(track));
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
	static int CmdRemoveSingleSignal(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		/* Track */ int  track = (p1 & 0x7);

		if (!ValParamTrackOrientation(track) || !tile.IsTileType( TileTypes.MP_RAILWAY) || !tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		if (!HasSignalOnTrack(tile, track)) // no signals on track?
			return Cmd.CMD_ERROR;

		/* Only water can remove signals from anyone */
		if (!PlayerID.getCurrent().isWater() && !Player.CheckTileOwnership(tile)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		/* Do it? */
		if(0 != (flags & Cmd.DC_EXEC)) {
			tile.getMap().m3 &= ~SignalOnTrack(track);

			/* removed last signal from tile? */
			if (BitOps.GB(tile.getMap().m3, 4, 4) == 0) 
			{
				tile.getMap().m2 = BitOps.RETSB(tile.getMap().m2, 4, 4, 0);
				tile.getMap().m5 = BitOps.RETSB(tile.getMap().m5, 6, 2, RAIL_TYPE_NORMAL >> 6); // >> because the constant is meant for direct application, not use with SB
				tile.getMap().m4 = BitOps.RETCLRBIT(tile.getMap().m4, 3); // remove any possible semaphores
			}

			SetSignalsOnBothDir(tile, track);

			tile.MarkTileDirtyByTile();
		}

		return (int) Global._price.remove_signals;
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
			tile.getMap().m3 =  BitOps.RETSB(tile.getMap().m3, 0, 4, totype);
			tile.MarkTileDirtyByTile();
		}

		return (int) (Global._price.build_rail / 2);
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
		int ret, cost; 
		long money;
		int sx, sy, x, y;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (!Player.ValParamRailtype(p2)) return Cmd.CMD_ERROR;
		if (p1 > Global.MapSize()) return Cmd.CMD_ERROR;

		TileIndex pp1 = TileIndex.get(p1);
		
		// make sure sx,sy are smaller than ex,ey
		sx = pp1.TileX() * TileInfo.TILE_SIZE;
		sy = pp1.TileY() * TileInfo.TILE_SIZE;
		if (ex < sx) { int t = sx; sx = ex; ex = t; } // intswap(ex, sx);
		if (ey < sy) { int t = sy; sy = ey; ey = t; } // intswap(ey, sy);

		money = Cmd.GetAvailableMoneyForCommand();
		cost = 0;

		for (x = sx; x <= ex; x += TileInfo.TILE_SIZE) {
			for (y = sy; y <= ey; y += TileInfo.TILE_SIZE) {
				TileIndex tile = TileIndex.TileVirtXY(x, y);
				DoConvertRailProc proc;

				switch (tile.GetTileType()) {
				case MP_RAILWAY:      proc = Rail::DoConvertRail;             break;
				case MP_STATION:      proc = Station::DoConvertStationRail;      break;
				case MP_STREET:       proc = Road::DoConvertStreetRail;       break;
				case MP_TUNNELBRIDGE: proc = TunnelBridgeCmd::DoConvertTunnelBridgeRail; break;
				default: continue;
				}

				ret = proc.apply(tile, p2, false);
				if (Cmd.CmdFailed(ret)) continue;
				cost += ret;

				if(0 != (flags & Cmd.DC_EXEC)) {
					money -= ret;
					if (money < 0) {
						Global._additional_cash_required = ret;
						return cost - ret;
					}
					proc.apply(tile, p2, true);
				}
			}
		}

		return (cost == 0) ? Cmd.CMD_ERROR : cost;
	}

	static int RemoveTrainDepot(TileIndex tile, int flags)
	{
		if (!Player.CheckTileOwnership(tile) && !PlayerID.getCurrent().isWater())
			return Cmd.CMD_ERROR;

		if (!tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			/* Track */ int  track = TrackdirToTrack(DiagdirToDiagTrackdir(Depot.GetDepotDirection(tile, TransportType.Rail)));

			Depot.DoDeleteDepot(tile);
			SetSignalsOnBothDir(tile, track);
		}

		return (int) Global._price.remove_train_depot;
	}

	static int ClearTile_Track(TileIndex tile, int flags)
	{
		int cost;
		int ret;

		int m5 = tile.getMap().m5;

		if(0 != (flags & Cmd.DC_AUTO)) {
			if(0 !=  (m5 & RAIL_TYPE_SPECIAL))
				return Cmd.return_cmd_error(Str.STR_2004_BUILDING_MUST_BE_DEMOLISHED);

			if (!tile.IsTileOwner(PlayerID.getCurrent()))
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






	static void DrawSignalHelper(final TileInfo ti, int condition, int image_and_pos)
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
			int x, int y, int z,
			int xsize, int ysize, int zsize)
	{
		if(0 != (image & Sprites.PALETTE_MODIFIER_COLOR)) image |= _drawtile_track_palette;
		image += offset;
		if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) image = Sprite.RET_MAKE_TRANSPARENT(image);
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
	static void DrawTrackBits(TileInfo ti, /*TrackBits*/ int  track, boolean earth, boolean snow, boolean flat)
	{
		final RailtypeInfo rti = GetRailTypeInfo(GetRailType(ti.tile));
		//PalSpriteID image;
		//SpriteID image;
		boolean junction = false;

		// Select the sprite to use.
		/*
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
		*/
		RailBaseSprites spr = rti.base_sprites;
		int spri = spr.track_y.id;
		switch(track)
		{
		case (TRACK_BIT_DIAG1 | TRACK_BIT_DIAG2): spri++;
		case TRACK_BIT_LEFT:  spri++; // Fall through
		case TRACK_BIT_RIGHT: spri++; // Fall through
		case TRACK_BIT_LOWER: spri++; // Fall through
		case TRACK_BIT_UPPER: spri++; // Fall through
		case TRACK_BIT_DIAG1: spri++; // Fall through
		case TRACK_BIT_DIAG2: 
			break;
			
		case (TRACK_BIT_UPPER | TRACK_BIT_LOWER):			
			spri = spr.track_ns.id; break;
			
		case (TRACK_BIT_LEFT | TRACK_BIT_RIGHT):
			spri = spr.track_ns.id+1; break;
			
		default:
			junction = true;
			if(0==(track & (TRACK_BIT_RIGHT | TRACK_BIT_UPPER | TRACK_BIT_DIAG1)))			
				spri = spr.ground.id; 
			else if(0 == (track & (TRACK_BIT_LEFT | TRACK_BIT_LOWER | TRACK_BIT_DIAG1)))
				spri = spr.ground.id+1; 
			else if(0 == (track & (TRACK_BIT_LEFT | TRACK_BIT_UPPER | TRACK_BIT_DIAG2)))
				spri = spr.ground.id+2; 
			else if(0 == (track & (TRACK_BIT_RIGHT | TRACK_BIT_LOWER | TRACK_BIT_DIAG2))) 					
				spri = spr.ground.id+3;
			else
				spri = spr.ground.id+4; 
		}
		
		int image = spri;
		
		if (ti.tileh != 0) {
			int foundation;

			if (flat) {
				foundation = ti.tileh;
			} else {
				foundation = GetRailFoundation(ti.tileh, track);
			}

			if (foundation != 0)
				Landscape.DrawFoundation(ti, foundation);

			// DrawFoundation() modifies ti.
			// Default sloped sprites..
			if (ti.tileh != 0)
				image = _track_sloped_sprites[ti.tileh - 1] + rti.base_sprites.track_y.id;
		}

		if (earth) {
			image = (image & Sprite.SPRITE_MASK) | Sprites.PALETTE_TO_BARE_LAND; // Use brown palette
		} else if (snow) {
			image += rti.snow_offset.id;
		}

		ViewPort.DrawGroundSprite(image);

		// Draw track pieces individually for junction tiles
		if (junction) {
			if(0 != (track & TRACK_BIT_DIAG1)) ViewPort.DrawGroundSprite(rti.base_sprites.single_y);
			if(0 != (track & TRACK_BIT_DIAG2)) ViewPort.DrawGroundSprite(rti.base_sprites.single_x);
			if(0 != (track & TRACK_BIT_UPPER)) ViewPort.DrawGroundSprite(rti.base_sprites.single_n);
			if(0 != (track & TRACK_BIT_LOWER)) ViewPort.DrawGroundSprite(rti.base_sprites.single_s);
			if(0 != (track & TRACK_BIT_LEFT))  ViewPort.DrawGroundSprite(rti.base_sprites.single_w);
			if(0 != (track & TRACK_BIT_RIGHT)) ViewPort.DrawGroundSprite(rti.base_sprites.single_e);
		}

		if (Global._debug_pbs_level >= 1) {
			int pbs = Pbs.PBSTileReserved(ti.tile) & track;
			if(0 != (pbs & TRACK_BIT_DIAG1)) ViewPort.DrawGroundSprite(rti.base_sprites.single_y.id | Sprites.PALETTE_CRASH);
			if(0 != (pbs & TRACK_BIT_DIAG2)) ViewPort.DrawGroundSprite(rti.base_sprites.single_x.id | Sprites.PALETTE_CRASH);
			if(0 != (pbs & TRACK_BIT_UPPER)) ViewPort.DrawGroundSprite(rti.base_sprites.single_n.id | Sprites.PALETTE_CRASH);
			if(0 != (pbs & TRACK_BIT_LOWER)) ViewPort.DrawGroundSprite(rti.base_sprites.single_s.id | Sprites.PALETTE_CRASH);
			if(0 != (pbs & TRACK_BIT_LEFT))  ViewPort.DrawGroundSprite(rti.base_sprites.single_w.id | Sprites.PALETTE_CRASH);
			if(0 != (pbs & TRACK_BIT_RIGHT)) ViewPort.DrawGroundSprite(rti.base_sprites.single_e.id | Sprites.PALETTE_CRASH);
		}
	}


	private static boolean HAS_SIGNAL(int x, int m23) { return 0 != (m23 & (0x1 << (x))); }
	private static boolean ISON_SIGNAL(int x, int m23) { return 0 != (m23 & (0x10 << (x))); }

	private static void MAYBE_DRAW_SIGNAL(TileInfo ti, int x, int y, int z, int m23)
	{
		if (HAS_SIGNAL(x,m23)) 
			DrawSignalHelper(ti, ISON_SIGNAL(x,m23) ? 1 : 0, ((y-0x4FB) << 4)|(z));
	}

	static void DrawTile_Track(TileInfo ti)
	{
		int m5;
		final RailtypeInfo rti = GetRailTypeInfo(GetRailType(ti.tile));
		//PalSpriteID image;

		_drawtile_track_palette = Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(ti.tile.GetTileOwner()));

		m5 = 0xFF & ti.map5;
		if (0==(m5 & RAIL_TYPE_SPECIAL)) {
			boolean earth = (ti.tile.getMap().m2 & RAIL_MAP2LO_GROUND_MASK) == RAIL_GROUND_BROWN;
			boolean snow = (ti.tile.getMap().m2 & RAIL_MAP2LO_GROUND_MASK) == RAIL_GROUND_ICE_DESERT;

			DrawTrackBits(ti, m5 & TRACK_BIT_MASK, earth, snow, false);

			if(0 != (Global._display_opt & Global.DO_FULL_DETAIL)) {
				_detailed_track_proc[ti.tile.getMap().m2 & RAIL_MAP2LO_GROUND_MASK].accept(ti);
			}

			/* draw signals also? */
			if (0==(ti.map5 & RAIL_TYPE_SIGNALS))
				return;

			{
				int m23 =  ((ti.tile.getMap().m3 >> 4) | (ti.tile.getMap().m2 & 0xF0));


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
			int type =  (m5 & 0x3F); // 0-3: depots, 4-5: waypoints

			if (0==(m5 & (RAIL_TILE_TYPE_MASK&~RAIL_TYPE_SPECIAL)))
				/* XXX: There used to be "return;" here, but since I could not find out
				 * why this would ever occur, I put assert(0) here. Let's see if someone
				 * complains about it. If not, we'll remove this check. (Matthijs). */
				assert false;

			if (ti.tileh != 0) Landscape.DrawFoundation(ti, ti.tileh);

			if (WayPoint.IsRailWaypoint(ti.tile) && BitOps.HASBIT(ti.tile.getMap().m3, 4)) {
				// look for customization
				int stat_id = WayPoint.GetWaypointByTile(ti.tile).stat_id;
				final StationSpec stat =  StationClass.GetCustomStation(StationClassID.STAT_CLASS_WAYP, stat_id);

				if (stat != null) {
					//final DrawTileSeqStruct seq;
					// emulate station tile - open with building
					final DrawTileSprites cust = stat.renderdata[2 + (m5 & 0x1)];
					int relocation = Station.GetCustomStationRelocation(stat, WayPoint.ComposeWaypointStation(ti.tile), 0);

					//* We don't touch the 0x8000 bit. In all this
					// * waypoint code, it is used to indicate that
					// * we should offset by railtype, but we always
					// * do that for custom ground sprites and never
					//* for station sprites. And in the drawing
					// * code, it is used to indicate that the sprite
					// * should be drawn in company colors, and it's
					// * up to the GRF file to decide that. *

					int simage = cust.ground_sprite;
					simage += (simage < GRFFile._custom_sprites_base) ? rti.total_offset.id : GetRailType(ti.tile);

					ViewPort.DrawGroundSprite(simage);

					// #define foreach_draw_tile_seq(idx, list) for (idx = list; ((byte) idx->delta_x) != 0x80; idx++)
					//foreach_draw_tile_seq(seq, cust.seq)
					//for (seq = cust.seq; ((byte) seq.delta_x) != 0x80; seq++)
					for (DrawTileSeqStruct seq : cust.seq)
					{
						if( (0xFF & seq.delta_x) == 0x80 )
							break;
								
						int image = seq.image + relocation;
						DrawSpecialBuilding(image, 0, ti,
								seq.delta_x, seq.delta_y, seq.delta_z,
								seq.width, seq.height, seq.unk);
					}
					return;
				}
			}

			DrawTrackSeqStruct[] drssa = _track_depot_layout_table[type];
			DrawTrackSeqStruct drss;
			int drssp = 0;
			
			drss = drssa[drssp++];
			//image = drss++.image;
			int image = drss.image;
			/* @note This is kind of an ugly hack, as the PALETTE_MODIFIER_COLOR indicates
			 * whether the sprite is railtype dependent. Rewrite this asap */
			if(0 != (image & Sprite.PALETTE_MODIFIER_COLOR)) image = (image & Sprite.SPRITE_MASK) + rti.total_offset.id;

			// adjust ground tile for desert
			// (don't adjust for arctic depots, because snow in depots looks weird)
			// type >= 4 means waypoints
			if ((ti.tile.getMap().m4 & RAIL_MAP2LO_GROUND_MASK) == RAIL_GROUND_ICE_DESERT && (GameOptions._opt.landscape == Landscape.LT_DESERT || type >= 4)) {
				if (image != Sprite.SPR_FLAT_GRASS_TILE) {
					image += rti.snow_offset.id; // tile with tracks
				} else {
					image = Sprite.SPR_FLAT_SNOWY_TILE; // flat ground
				}
			}

			ViewPort.DrawGroundSprite(image);

			if (Global._debug_pbs_level >= 1) {
				int pbs = Pbs.PBSTileReserved(ti.tile);
				if(0!=(pbs & TRACK_BIT_DIAG1)) ViewPort.DrawGroundSprite(rti.base_sprites.single_y.id | Sprites.PALETTE_CRASH);
				if(0!=(pbs & TRACK_BIT_DIAG2)) ViewPort.DrawGroundSprite(rti.base_sprites.single_x.id | Sprites.PALETTE_CRASH);
				if(0!=(pbs & TRACK_BIT_UPPER)) ViewPort.DrawGroundSprite(rti.base_sprites.single_n.id | Sprites.PALETTE_CRASH);
				if(0!=(pbs & TRACK_BIT_LOWER)) ViewPort.DrawGroundSprite(rti.base_sprites.single_s.id | Sprites.PALETTE_CRASH);
				if(0!=(pbs & TRACK_BIT_LEFT))  ViewPort.DrawGroundSprite(rti.base_sprites.single_w.id | Sprites.PALETTE_CRASH);
				if(0!=(pbs & TRACK_BIT_RIGHT)) ViewPort.DrawGroundSprite(rti.base_sprites.single_e.id | Sprites.PALETTE_CRASH);
			}

			for (; ; ) 
			{
				drss = drssa[drssp++];
				if(drss.image == 0)
					break;
				DrawSpecialBuilding(drss.image, type < 4 ? rti.total_offset.id : 0, ti,
						drss.subcoord_x, drss.subcoord_y, 0,
						drss.width, drss.height, 0x17);
			}
		}
	}

	public static void DrawTrainDepotSprite(int x, int y, int imagei, /*RailType*/ int railtype)
	{
		int ormod, img;
		final RailtypeInfo rti = GetRailTypeInfo(railtype);
		final DrawTrackSeqStruct [] dtssa = _track_depot_layout_table[imagei];

		ormod = Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player);

		//dtss = _track_depot_layout_table[image];

		x += 33;
		y += 17;

		int imagep = 0;
		//img = dtss++.image;
		img = dtssa[imagep++].image;
		/* @note This is kind of an ugly hack, as the PALETTE_MODIFIER_COLOR indicates
		 * whether the sprite is railtype dependent. Rewrite this asap */
		if(0 != (img & Sprites.PALETTE_MODIFIER_COLOR)) img = (img & Sprite.SPRITE_MASK) + rti.total_offset.id;
		Gfx.DrawSprite(img, x, y);

		for (;;) {
			DrawTrackSeqStruct dtss = dtssa[imagep++];
			if(dtss.image == 0) break;
			Point pt = Point.RemapCoords(dtss.subcoord_x, dtss.subcoord_y, 0);
			int image = dtss.image;
			if(0 != (image & Sprites.PALETTE_MODIFIER_COLOR)) image |= ormod;
			Gfx.DrawSprite(image + rti.total_offset.id, x + pt.x, y + pt.y);
		}
	}

	static void DrawDefaultWaypointSprite(int x, int y, /*RailType*/ int railtype)
	{
		final DrawTrackSeqStruct [] dtssa = _track_depot_layout_table[4];
		final RailtypeInfo rti = GetRailTypeInfo(railtype);
		int img;

		int dtssp = 0;
		img = dtssa[dtssp++].image;
		if(0 != (img & Sprites.PALETTE_MODIFIER_COLOR)) img = (img & Sprite.SPRITE_MASK) + rti.total_offset.id;
		Gfx.DrawSprite(img, x, y);

		for (; ; ) {
			DrawTrackSeqStruct dtss = dtssa[dtssp++];
			if(dtss.image == 0) break;
			Point pt = Point.RemapCoords(dtss.subcoord_x, dtss.subcoord_y, 0);
			img = dtss.image;
			if(0 != (img & Sprites.PALETTE_MODIFIER_COLOR)) img |= Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player);
			Gfx.DrawSprite(img, x + pt.x, y + pt.y);
		}
	}

	static class SetSignalsData {
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
		final byte [] bit = new byte[NUM_SSD_ENTRY];
		final TileIndex[] tile = new TileIndex[NUM_SSD_ENTRY];

		int pbs_cur;
		// these are used to keep track of all signals in the block
		final TileIndex [] pbs_tile = new TileIndex[NUM_SSD_ENTRY];

		// these are used to keep track of the stack that modifies presignals recursively
		final TileIndex [] next_tile = new TileIndex[NUM_SSD_STACK];
		final byte [] next_dir = new byte[NUM_SSD_STACK];

	}

	static boolean SetSignalsEnumProc(TileIndex tile, Object o, int track, int length, Object state)
	{
		SetSignalsData ssd = (SetSignalsData) o;
		// the tile has signals?
		if (tile.IsTileType( TileTypes.MP_RAILWAY)) {
			if (HasSignalOnTrack(tile, TrackdirToTrack(track))) {
				if ((tile.getMap().m3 & _signals_table[track]) != 0) {
					// yes, add the signal to the list of signals
					if (ssd.cur != NUM_SSD_ENTRY) {
						ssd.tile[ssd.cur] = tile; // remember the tile index
						ssd.bit[ssd.cur] =  (byte) track; // and the controlling bit number
						ssd.cur++;
					}

					if (Pbs.PBSIsPbsSignal(tile, ReverseTrackdir(track)))
						ssd.has_pbssignal = BitOps.RETSETBIT(ssd.has_pbssignal, 2);

					// remember if this block has a presignal.
					ssd.has_presignal = ssd.has_presignal || 0 != (tile.getMap().m4&1);
				}

				if (Pbs.PBSIsPbsSignal(tile, ReverseTrackdir(track)) || Pbs.PBSIsPbsSignal(tile, track)) 
				{
					int num = ssd.has_pbssignal & 3;
					num = BitOps.clamp(num + 1, 0, 2);
					ssd.has_pbssignal &= ~3;
					ssd.has_pbssignal |= num;
				}

				if ((tile.getMap().m3 & _signals_table_both[track]) != 0) {
					ssd.pbs_tile[ssd.pbs_cur] = tile; // remember the tile index
					ssd.pbs_cur++;
				}

				if(0 != (tile.getMap().m3&_signals_table_other[track])) 
				{
					if(0 != (tile.getMap().m4&2)) {
						// this is an exit signal that points out from the segment
						ssd.presignal_exits++;
						if ((tile.getMap().m2&_signals_table_other[track]) != 0)
							ssd.presignal_exits_free++;
					}
					if (Pbs.PBSIsPbsSignal(tile, track))
						ssd.has_pbssignal = BitOps.RETSETBIT(ssd.has_pbssignal, 3);
				}

				return true;
			} else if (Depot.IsTileDepotType(tile, TransportType.Rail)) {
				return true; // don't look further if the tile is a depot
			}
		}
		return false;
	}

	/* Struct to parse data from VehicleFromPos to SignalVehicleCheckProc */
	static class SignalVehicleCheckStruct {
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
			tile = TunnelBridgeCmd.GetVehicleOutOfTunnelTile(v);
		} else {
			tile = v.tile;
		}

		/* Wrong tile, or no train? Not a match */
		if (!tile.equals(dest.tile)) return null;

		/* Are we on the same piece of track? */
		if(0 != (dest.track & (v.rail.track + (v.rail.track << 8)))) return v;

		return null;
	}

	/* Special check for SetSignalsAfterProc, to see if there is a vehicle on this tile */
	static boolean SignalVehicleCheck(TileIndex tile, int track)
	{
		SignalVehicleCheckStruct dest = new SignalVehicleCheckStruct();

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
			flotr = Pathfind.FindLengthOfTunnel(tile, direction);
			dest.track = 1 << (direction & 1); // get the trackbit the vehicle would have if it has not entered the tunnel yet (ie is still visible)

			// check for a vehicle with that trackdir on the start tile of the tunnel
			if (Vehicle.VehicleFromPos(tile, dest, Rail::SignalVehicleCheckProc) != null) return true;

			// check for a vehicle with that trackdir on the end tile of the tunnel
			if (Vehicle.VehicleFromPos(flotr.tile, dest, Rail::SignalVehicleCheckProc) != null) return true;

			// now check all tiles from start to end for a "hidden" vehicle
			// NOTE: the hashes for tiles may overlap, so this could maybe be optimised a bit by not checking every tile?
			dest.track = 0x40; // trackbit for vehicles "hidden" inside a tunnel
			for (; !tile.equals(flotr.tile); tile = tile.iadd(TileIndex.TileOffsByDir(direction)) ) 
			{
				if (Vehicle.VehicleFromPos(tile, dest, Rail::SignalVehicleCheckProc) != null)
					return true;
			}

			// no vehicle found
			return false;
		}

		return Vehicle.VehicleFromPos(tile, dest, Rail::SignalVehicleCheckProc) != null;
	}



	static void SetSignalsAfterProc(TrackPathFinder tpf)
	{
		SetSignalsData ssd = (SetSignalsData) tpf.userdata;
		//TrackPathFinderLink link;
		//int offs;
		//int i;

		ssd.stop = false;

		for( Entry<Integer, TPFHashEnt> e : tpf.entrySet() )
		{
			if (SignalVehicleCheck( TileIndex.get( e.getKey() ), e.getValue().bits )) 
			{
				ssd.stop = true;
				return;
			}			
		}
		
		/*
		for( Iterator<TPFHashEnt> ii = tpf.getIterator(); ii.hasNext(); )
		{
			TPFHashEnt item = ii.next();
		}*/
		
		/*//Go through all the PF tiles 
		for (i = 0; i < tpf.hash_head.length; i++) {
			// Empty hash item 
			if (tpf.hash_head[i] == 0) continue;

			// If 0x8000 is not set, there is only 1 item 
			if (0==(tpf.hash_head[i] & 0x8000)) {
				// Check if there is a vehicle on this tile 
				if (SignalVehicleCheck(tpf.hash_tile[i], tpf.hash_head[i])) {
					ssd.stop = true;
					return;
				}
			} else {
				// There are multiple items, where hash_tile points to the first item in the list 
				offs = tpf.hash_tile[i].tile;
				do {

					//#define PATHFIND_GET_LINK_PTR(tpf, link_offs) (TrackPathFinderLink*)((byte*)tpf->links + (link_offs))

					// Find the next item 
					//link = PATHFIND_GET_LINK_PTR(tpf, offs);
					link = tpf.links[offs/4]; //   it generates offsets to 32 bit pointers?
					// Check if there is a vehicle on this tile 
					if (SignalVehicleCheck(link.tile, link.flags)) {
						ssd.stop = true;
						return;
					}
					//* Goto the next item 
				} while ((offs=link.next) != 0xFFFF);
			}
		}
		*/
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
			}

			// then mark the signals in the segment accordingly
			for (i = 0; i != ssd.cur; i++) 
			{
				TileIndex tile = ssd.tile[i];
				int bit = _signals_table[ssd.bit[i]];
				int m2 = tile.getMap().m2;

				// presignals don't turn green if there is at least one presignal exit and none are free
				if(0 != (tile.getMap().m4 & 1)) {
					int ex = ssd.presignal_exits, exfree = ssd.presignal_exits_free;

					// subtract for dual combo signals so they don't count themselves
					if ( 0 != (tile.getMap().m4&2) && 0 != (tile.getMap().m3&_signals_table_other[ssd.bit[i]]) ) {
						ex--;
						if ((tile.getMap().m2&_signals_table_other[ssd.bit[i]]) != 0) exfree--;
					}

					// if we have exits and none are free, make red.
					if ( (0 != ex) && 0==exfree) 
					{
						//goto make_red;
						// turn red
						if ( (bit&m2) == 0 )
							continue;
					}
				}

				// check if the signal is unaffected.
				if (ssd.stop) {
					//make_red:
					// turn red
					if ( (bit&m2) == 0 )
						continue;
				} else {
					// turn green
					if ( (bit&m2) != 0 )
						continue;
				}

				/* Update signals on the other side of this exit-combo signal; it changed. */
				if(0 != (tile.getMap().m4 & 2 )) {
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


	static boolean UpdateSignalsOnSegment(TileIndex tile, int direction)
	{
		SetSignalsData ssd = new SetSignalsData();
		int result = -1;

		ssd.cur_stack = 0;
		direction >>= 1;

		for(;;) {
			// go through one segment and update all signals pointing into that segment.
			ssd.cur = ssd.pbs_cur = ssd.presignal_exits = ssd.presignal_exits_free = 0;
			ssd.has_presignal = false;
			ssd.has_pbssignal = 0; //false;

			Pathfind.FollowTrack(tile, TransportType.Rail, 0xC000, direction, Rail::SetSignalsEnumProc, Rail::SetSignalsAfterProc, ssd);
			ChangeSignalStates(ssd);

			// remember the result only for the first iteration.
			if (result < 0) result = ssd.stop ? 1 : 0;

			// if any exit signals were changed, we need to keep going to modify the stuff behind those.
			if (ssd.cur_stack == 0) break;

			// one or more exit signals were changed, so we need to update another segment too.
			tile = ssd.next_tile[--ssd.cur_stack];
			direction = ssd.next_dir[ssd.cur_stack];
		}

		return result != 0;
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
					th = Landscape._inclined_tileh[f - 15];
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
					return Landscape._inclined_tileh[f - 15];
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
		return new AcceptedCargo();
		/* not used */
	}

	static void AnimateTile_Track(TileIndex tile)
	{
		/* not used */
	}

	static void TileLoop_Track(TileIndex tile)
	{
		int old_ground;
		int new_ground = -1;
		/*TrackBits*/ int  rail;

		old_ground = 0 != (tile.getMap().m5 & RAIL_TYPE_SPECIAL) ? BitOps.GB(tile.getMap().m4, 0, 4) : BitOps.GB(tile.getMap().m2, 0, 4);

		do { // for goto replacement
			boolean do_goto = false;
			switch (GameOptions._opt.landscape) {
			case Landscape.LT_HILLY:
				if (tile.GetTileZ() > GameOptions._opt.snow_line) { /* convert into snow? */
					new_ground = RAIL_GROUND_ICE_DESERT;
					//goto modify_me;
					do_goto = true;
				}
				break;

			case Landscape.LT_DESERT:
				if (tile.GetMapExtraBits() == TileInfo.EXTRABITS_DESERT) { /* convert into desert? */
					new_ground = RAIL_GROUND_ICE_DESERT;
					//goto modify_me;
					do_goto = true;
				}
				break;
			}

			if(do_goto) break; // out of do{} while

			// Don't continue tile loop for depots
			if(0 != (tile.getMap().m5 & RAIL_TYPE_SPECIAL)) return;

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
					PlayerID owner = tile.GetTileOwner();

					if ( (0==(rail&(TRACK_BIT_DIAG2|TRACK_BIT_UPPER|TRACK_BIT_LEFT)) && 0!=(rail&TRACK_BIT_DIAG1) ) || rail==(TRACK_BIT_LOWER|TRACK_BIT_RIGHT)) {
						TileIndex tt = tile.iadd(0, -1);
						if (!tt.IsTileType(TileTypes.MP_RAILWAY) ||
								!tt.IsTileOwner(owner) ||
								(tt.M().m5 == TRACK_BIT_UPPER || tt.M().m5 == TRACK_BIT_LEFT))
							new_ground = RAIL_GROUND_FENCE_NW;
					}

					if ( (0==(rail&(TRACK_BIT_DIAG2|TRACK_BIT_LOWER|TRACK_BIT_RIGHT)) && 0!=(rail&TRACK_BIT_DIAG1)) || rail==(TRACK_BIT_UPPER|TRACK_BIT_LEFT)) {
						TileIndex tt = tile.iadd(0, 1);
						if (!tt.IsTileType(TileTypes.MP_RAILWAY) ||
								!tt.IsTileOwner(owner) ||
								(tt.M().m5 == TRACK_BIT_LOWER || tt.M().m5 == TRACK_BIT_RIGHT))
							new_ground = (new_ground == RAIL_GROUND_FENCE_NW) ? RAIL_GROUND_FENCE_SENW : RAIL_GROUND_FENCE_SE;
					}

					if ( (0==(rail&(TRACK_BIT_DIAG1|TRACK_BIT_UPPER|TRACK_BIT_RIGHT)) && 0!=(rail&TRACK_BIT_DIAG2)) || rail==(TRACK_BIT_LOWER|TRACK_BIT_LEFT)) {
						TileIndex tt = tile.iadd(-1, 0);
						if (!tt.IsTileType(TileTypes.MP_RAILWAY) ||
								!tt.IsTileOwner(owner) ||
								(tt.M().m5 == TRACK_BIT_UPPER || tt.M().m5 == TRACK_BIT_RIGHT))
							new_ground = RAIL_GROUND_FENCE_NE;
					}

					if ( (0==(rail&(TRACK_BIT_DIAG1|TRACK_BIT_LOWER|TRACK_BIT_LEFT)) && 0!=(rail&TRACK_BIT_DIAG2)) || rail==(TRACK_BIT_UPPER|TRACK_BIT_RIGHT)) {
						TileIndex tt = tile.iadd(1, 0);
						if (!tt.IsTileType(TileTypes.MP_RAILWAY) ||
								!tt.IsTileOwner(owner) ||
								(tt.M().m5 == TRACK_BIT_LOWER || tt.M().m5 == TRACK_BIT_LEFT))
							new_ground = (new_ground == RAIL_GROUND_FENCE_NE) ? RAIL_GROUND_FENCE_NESW : RAIL_GROUND_FENCE_SW;
					}
				}
			}
		} while(false); // goto target
		//modify_me:;
		/* tile changed? */
		
		assert new_ground != -1;
		
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
	static int GetTileTrackStatus_Track(TileIndex tile, TransportType mode)
	{
		int a;
		int b;
		int ret;

		if (mode != TransportType.Rail) return 0;

		int m5 = tile.getMap().m5;

		if (0==(m5 & RAIL_TYPE_SPECIAL)) {
			ret = (m5 | (m5 << 8)) & 0x3F3F;
			if (0==(m5 & RAIL_TYPE_SIGNALS)) {
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
		} else if(0 != (m5 & 0x40)) {
			m5 = 0xFF & _train_spec_tracks[m5 & 0x3F];
			ret = (m5 << 8) + m5;
		} else
			return 0;
		return ret;
	}

	static void ClickTile_Track(TileIndex tile)
	{
		if (tile.IsTileDepotType(TransportType.Rail)) {
			TrainGui.ShowTrainDepotWindow(tile);
		} else if (tile.IsRailWaypoint()) {
			Gui.ShowRenameWaypointWindow(WayPoint.GetWaypointByTile(tile));
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
		int fract_coord;
		int fract_coord_leave;
		int dir;
		int length;

		// this routine applies only to trains in depot tiles
		if (v.type != Vehicle.VEH_Train || !tile.IsTileDepotType(TransportType.Rail)) return 0;

		/* depot direction */
		dir = Depot.GetDepotDirection(tile, TransportType.Rail);

		/* calculate the point where the following wagon should be activated */
		/* this depends on the length of the current vehicle */
		length = v.rail.getCached_veh_length();

		fract_coord_leave =
				 (((_fractcoords_enter[dir] & 0x0F) +				// x
						(length + 1) * _deltacoord_leaveoffset[dir]) +
				(((_fractcoords_enter[dir] >> 4) +				// y
						((length + 1) * _deltacoord_leaveoffset[dir+4])) << 4));

		fract_coord = (x & 0xF) + ((y & 0xF) << 4);

		if (_fractcoords_behind[dir] == fract_coord) {
			/* make sure a train is not entering the tile from behind */
			return 8;
		} else if (_fractcoords_enter[dir] == fract_coord) {
			if (_enter_directions[dir] == v.direction) {
				/* enter the depot */
				if (v.next == null)
					Pbs.PBSClearTrack(v.tile, BitOps.FIND_FIRST_BIT(v.rail.track));

				v.rail.setInDepot();
				v.setHidden(true); /* hide it */
				v.direction ^= 4;

				if (v.next == null)
					TrainCmd.TrainEnterDepot(v, tile);

				v.tile = tile;
				Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, tile.tile);
				return 4;
			}
		} else if (fract_coord_leave == fract_coord) {
			if (_leave_directions[dir] == v.direction) {
				/* leave the depot? */
				if ((v=v.next) != null) {
					v.setHidden(false); 
					v.rail.track =  _depot_track_mask[dir];
					assert(v.rail.track != 0);
				}
			}
		}

		return 0;
	}

	static void InitializeRail()
	{
		Depot._last_built_train_depot_tile = null;
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
	 * Maps a trackdir to the bit that stores its status in the map arrays, in the
	 * direction along with the trackdir.
	 * / 

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
		//return  Global._m[tile.getTile()].m5 & RAIL_TILE_TYPE_MASK;
		return  tile.M().m5 & RAIL_TILE_TYPE_MASK;
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
		return tile.M().m3 & RAILTYPE_MASK; 
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
		return tile.M().m5 & RAIL_SUBTYPE_MASK;
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
		return tile.M().m5 & TRACK_BIT_MASK;
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
	public static  /*Trackdir*/ int DiagdirToDiagTrackdir(/*DiagDirection*/ int  diagdir) {
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
				&& ((tile.M().m3 & SignalOnTrack(track)) != 0));
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
		return (tile.M().m5 & 0xF0) == 0x10;
	}

	/**
	 * Gets the transport type of the given track on the given crossing tile.
	 * @return  The transport type of the given track, either TRANSPORT_ROAD,
	 * TRANSPORT_RAIL.
	 */
	//static  /*TransportType*/ int GetCrossingTransportType(TileIndex tile, Track track)
	static TransportType GetCrossingTransportType(TileIndex tile, /* Track */ int  track)
	{
		/* XXX: Nicer way to write this? */
		switch(track)
		{
		/* When map5 bit 3 is set, the road runs in the y direction (DIAG2) */
		case TRACK_DIAG1:
			return (BitOps.HASBIT(tile.getMap().m5, 3) ? TransportType.Rail : TransportType.Road);
		case TRACK_DIAG2:
			return (BitOps.HASBIT(tile.getMap().m5, 3) ? TransportType.Road : TransportType.Rail);
		default:
			assert false;
		}
		return TransportType.Invalid; // Global.INVALID_TRANSPORT;
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


//typedef int DoConvertRailProc(TileIndex tile, int totype, boolean exec);

@FunctionalInterface
interface DoConvertRailProc {
	int apply(TileIndex tile, int totype, boolean exec);
}

