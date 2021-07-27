package game;

/** @file pbs.h Path-Based-Signalling header file
 *  @see pbs.c */

/** @file pbs.c Path-Based-Signalling implementation file
 *  @see pbs.h */

/* reserved track encoding:
 normal railway tracks:
   map3hi bits 4..6 = 'Track'number of reserved track + 1, if this is zero it means nothing is reserved on this tile
   map3hi bit  7    = if this is set, then the opposite track ('Track'number^1) is also reserved
 waypoints/stations:
   map3lo bit 6 set = track is reserved
 tunnels/bridges:
   map3hi bit 0 set = track with 'Track'number 0 is reserved
   map3hi bit 1 set = track with 'Track'number 1 is reserved
 level crossings:
   map5 bit 0 set = the rail track is reserved
*/

public class Pbs {

	/**
	 * finalants used for pbs_mode argument of npf-functions
	 */
	//enum pbs_modes {
	public static final int PBS_MODE_NONE = 0;    // no pbs
	public static final int PBS_MODE_GREEN = 1;   // look for green exit signal from pbs block
	public static final int PBS_MODE_ANY = 2;     // look for any exit signal from block
	//};
//
	/**
	 * finalants used for v.u.rail.pbs_status
	 */
	enum PBSStatus {
		PBS_STAT_NONE = 0,
		PBS_STAT_HAS_PATH = 1,
		PBS_STAT_NEED_PATH = 2,
	};


	//void PBSReserveTrack(TileIndex tile, Track track);
	/**<
	 * Marks a track as reserved.
	 * @param tile The tile of the track.
	 * @param track The track to reserve, valid values 0-5.
	 */

	//byte PBSTileReserved(TileIndex tile);
	/**<
	 * Check which tracks are reserved on a tile.
	 * @param tile The tile which you want to check.
	 * @return The tracks reserved on that tile, each of the bits 0-5 is set when the corresponding track is reserved.
	 */

	//int PBSTileUnavail(TileIndex tile);
	/**<
	 * Check which trackdirs are unavailable due to reserved tracks on a tile.
	 * @param tile The tile which you want to check.
	 * @return The tracks reserved on that tile, each of the bits 0-5,8-13 is set when the corresponding trackdir is unavailable.
	 */

	//void PBSClearTrack(TileIndex tile, Track track);
	/**<
	 * Unreserves a track.
	 * @param tile The tile of the track.
	 * @param track The track to unreserve, valid values 0-5.
	 */

	//void PBSClearPath(TileIndex tile, Trackdir trackdir, TileIndex end_tile, Trackdir end_trackdir);
	/**<
	 * Follows a planned(reserved) path, and unreserves the tracks.
	 * @param tile The tile on which the path starts
	 * @param trackdir The trackdirection in which the path starts
	 * @param end_tile The tile on which the path ends
	 * @param end_trackdir The trackdirection in which the path ends
	 */

	//boolean PBSIsPbsSignal(TileIndex tile, Trackdir trackdir);
	/**<
	 * Checks if there are pbs signals on a track.
	 * @param tile The tile you want to check
	 * @param trackdir The trackdir you want to check
	 * @return True when there are pbs signals on that tile
	 */

	//boolean PBSIsPbsSegment(int tile, Trackdir trackdir);
	/**<
	 * Checks if a signal/depot leads to a pbs block.
	 * This means that the block needs to have at least 1 signal, and that all signals in it need to be pbs signals.
	 * @param tile The tile to check
	 * @param trackdir The direction in which to check
	 * @return True when the depot is inside a pbs block
	 */


	
	
	
	
	
	
	
	



	/**
	 * maps an encoded reserved track (from map3lo bits 4..7)
	 * to the tracks that are reserved.
	 * 0xFF are invalid entries and should never be accessed.
	 */
	static final int encrt_to_reserved[] = {
		0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0xFF,
		0xFF, 0xFF, 0xFF, 0x0C, 0x0C, 0x30, 0x30, 0xFF
	};

	/**
	 * maps an encoded reserved track (from map3lo bits 4..7)
	 * to the track(dir)s that are unavailable due to reservations.
	 * 0xFFFF are invalid entries and should never be accessed.
	 */
	static final int encrt_to_unavail[] = {
		0x0000, 0x3F3F, 0x3F3F, 0x3737, 0x3B3B, 0x1F1F, 0x2F2F, 0xFFFF,
		0xFFFF, 0xFFFF, 0xFFFF, 0x3F3F, 0x3F3F, 0x3F3F, 0x3F3F, 0xFFFF
	};

	void PBSReserveTrack(TileIndex tile, Track track) {
		assert(IsValidTile(tile));
		assert(track <= 5);
		switch (GetTileType(tile)) {
			case TileTypes.MP_RAILWAY:
				if (IsRailWaypoint(tile)) {
					// waypoint
					SETBIT(tile.getMap().m3, 6);
				} else {
					// normal rail track
					byte encrt = BitOps.GB(tile.getMap().m4, 4, 4); // get current encoded info (see comments at top of file)

					if (encrt == 0) // nothing reserved before
						encrt = track + 1;
					else if (encrt == (track^1) + 1) // opposite track reserved before
						encrt |= 8;

					BitOps.RET SB(tile.getMap().m4, 4, 4, encrt);
				}
				break;
			case TileTypes.MP_TUNNELBRIDGE:
				tile.getMap().m4 |= (1 << track) & 3;
				break;
			case TileTypes.MP_STATION:
				SETBIT(tile.getMap().m3, 6);
				break;
			case TileTypes.MP_STREET:
				// make sure it is a railroad crossing
				if (!IsLevelCrossing(tile)) return;
				SETBIT(tile.getMap().m5, 0);
				break;
			default:
				return;
		}
		// if debugging, mark tile dirty to show reserved status
		if (_debug_pbs_level >= 1)
			MarkTileDirtyByTile(tile);
	}

	byte PBSTileReserved(TileIndex tile) {
		assert(IsValidTile(tile));
		switch (GetTileType(tile)) {
			case TileTypes.MP_RAILWAY:
				if (IsRailWaypoint(tile)) {
					// waypoint
					// check if its reserved
					if (!BitOps.HASBIT(tile.getMap().m3, 6)) return 0;
					// return the track for the correct direction
					return BitOps.HASBIT(tile.getMap().m5, 0) ? 2 : 1;
				} else {
					// normal track
					byte res = encrt_to_reserved[BitOps.GB(tile.getMap().m4, 4, 4)];
					assert(res != 0xFF);
					return res;
				}
			case TileTypes.MP_TUNNELBRIDGE:
				return BitOps.GB(tile.getMap().m4, 0, 2);
			case TileTypes.MP_STATION:
				// check if its reserved
				if (!BitOps.HASBIT(tile.getMap().m3, 6)) return 0;
				// return the track for the correct direction
				return BitOps.HASBIT(tile.getMap().m5, 0) ? 2 : 1;
			case TileTypes.MP_STREET:
				// make sure its a railroad crossing
				if (!IsLevelCrossing(tile)) return 0;
				// check if its reserved
				if (!BitOps.HASBIT(tile.getMap().m5, 0)) return 0;
				// return the track for the correct direction
				return BitOps.HASBIT(tile.getMap().m5, 3) ? 1 : 2;
			default:
				return 0;
		}
	}

	int PBSTileUnavail(TileIndex tile) {
		assert(IsValidTile(tile));
		switch (GetTileType(tile)) {
			case TileTypes.MP_RAILWAY:
				if (IsRailWaypoint(tile)) {
					// waypoint
					return BitOps.HASBIT(tile.getMap().m3, 6) ? TRACKDIR_BIT_MASK : 0;
				} else {
					// normal track
					int res = encrt_to_unavail[BitOps.GB(tile.getMap().m4, 4, 4)];
					assert(res != 0xFFFF);
					return res;
				}
			case TileTypes.MP_TUNNELBRIDGE:
				return BitOps.GB(tile.getMap().m4, 0, 2) | (BitOps.GB(tile.getMap().m4, 0, 2) << 8);
			case TileTypes.MP_STATION:
				return BitOps.HASBIT(tile.getMap().m3, 6) ? TRACKDIR_BIT_MASK : 0;
			case TileTypes.MP_STREET:
				// make sure its a railroad crossing
				if (!IsLevelCrossing(tile)) return 0;
				// check if its reserved
				return (BitOps.HASBIT(tile.getMap().m5, 0)) ? TRACKDIR_BIT_MASK : 0;
			default:
				return 0;
		}
	}

	void PBSClearTrack(TileIndex tile, Track track) {
		assert(IsValidTile(tile));
		assert(track <= 5);
		switch (GetTileType(tile)) {
			case TileTypes.MP_RAILWAY:
				if (IsRailWaypoint(tile)) {
					// waypoint
					CLRBIT(tile.getMap().m3, 6);
				} else {
					// normal rail track
					byte encrt = BitOps.GB(tile.getMap().m4, 4, 4);

					if (encrt == track + 1)
						encrt = 0;
					else if (encrt == track + 1 + 8)
						encrt = (track^1) + 1;
					else if (encrt == (track^1) + 1 + 8)
						encrt &= 7;

					BitOps.RET SB(tile.getMap().m4, 4, 4, encrt);
				}
				break;
			case TileTypes.MP_TUNNELBRIDGE:
				tile.getMap().m4 &= ~((1 << track) & 3);
				break;
			case TileTypes.MP_STATION:
				CLRBIT(tile.getMap().m3, 6);
				break;
			case TileTypes.MP_STREET:
				// make sure it is a railroad crossing
				if (!IsLevelCrossing(tile)) return;
				CLRBIT(tile.getMap().m5, 0);
				break;
			default:
				return;
		}
		// if debugging, mark tile dirty to show reserved status
		if (_debug_pbs_level >= 1)
			MarkTileDirtyByTile(tile);
	}

	void PBSClearPath(TileIndex tile, Trackdir trackdir, TileIndex end_tile, Trackdir end_trackdir) {
		int res;
		FindLengthOfTunnelResult flotr;
		assert(IsValidTile(tile));
		assert(IsValidTrackdir(trackdir));

		do {
			PBSClearTrack(tile, TrackdirToTrack(trackdir));

			if (tile == end_tile && TrackdirToTrack(trackdir) == TrackdirToTrack(end_trackdir))
				return;

			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) &&
					BitOps.GB(tile.getMap().m5, 4, 4) == 0 &&
					BitOps.GB(tile.getMap().m5, 0, 2) == TrackdirToExitdir(trackdir)) {
				// this is a tunnel
				flotr = FindLengthOfTunnel(tile, TrackdirToExitdir(trackdir));

				tile = flotr.tile;
			} else {
				byte exitdir = TrackdirToExitdir(trackdir);
				tile = AddTileIndexDiffCWrap(tile, TileIndexDiffCByDir(exitdir));
			}

			res = PBSTileReserved(tile);
			res |= res << 8;
			res &= TrackdirReachesTrackdirs(trackdir);
			trackdir = FindFirstBit2x64(res);

		} while (res != 0);
	}

	boolean PBSIsPbsSignal(TileIndex tile, Trackdir trackdir)
	{
		assert(tile.IsValidTile());
		assert(IsValidTrackdir(trackdir));

		if (!Global._patches.new_pathfinding_all)
			return false;

		if (!tile.IsTileType( TileTypes.MP_RAILWAY))
			return false;

		if (GetRailTileType(tile) != RAIL_TYPE_SIGNALS)
			return false;

		if (!HasSignalOnTrackdir(tile, trackdir))
			return false;

		if (GetSignalType(tile, TrackdirToTrack(trackdir)) == 4)
			return true;
		else
			return false;
	}

	class SetSignalsDataPbs {
		int cur;

		// these are used to keep track of the signals.
		byte bit[NUM_SSD_ENTRY];
		TileIndex tile[NUM_SSD_ENTRY];
	} SetSignalsDataPbs;

	// This function stores the signals inside the SetSignalsDataPbs struct, passed as callback to FollowTrack() in the PBSIsPbsSegment() function below
	static boolean SetSignalsEnumProcPBS(int tile, SetSignalsDataPbs *ssd, int trackdir, int length, byte *state)
	{
		// the tile has signals?
		if (tile.IsTileType( TileTypes.MP_RAILWAY)) {
			if (HasSignalOnTrack(tile, TrackdirToTrack(trackdir))) {

					if (ssd.cur != NUM_SSD_ENTRY) {
						ssd.tile[ssd.cur] = tile; // remember the tile index
						ssd.bit[ssd.cur] = TrackdirToTrack(trackdir); // and the controlling bit number
						ssd.cur++;
					}
					return true;
			} else if (IsTileDepotType(tile, TRANSPORT_RAIL))
				return true; // don't look further if the tile is a depot
		}
		return false;
	}

	boolean PBSIsPbsSegment(int tile, Trackdir trackdir)
	{
		SetSignalsDataPbs ssd;
		boolean result = PBSIsPbsSignal(tile, trackdir);
		DiagDirection direction = TrackdirToExitdir(trackdir);//GetDepotDirection(tile,TRANSPORT_RAIL);
		int i;

		ssd.cur = 0;

		FollowTrack(tile, 0xC000 | TRANSPORT_RAIL, direction, (TPFEnumProc*)SetSignalsEnumProcPBS, null, &ssd);
		for(i=0; i!=ssd.cur; i++) {
			int tile = ssd.tile[i];
			byte bit = ssd.bit[i];
			if (!PBSIsPbsSignal(tile, bit) && !PBSIsPbsSignal(tile, bit | 8))
				return false;
			result = true;
		}

		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
