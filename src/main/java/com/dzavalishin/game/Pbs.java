package com.dzavalishin.game;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.struct.FindLengthOfTunnelResult;
import com.dzavalishin.util.BitOps;



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
	//enum PBSStatus {
	public static final int PBS_STAT_NONE = 0;
	public static final int PBS_STAT_HAS_PATH = 1;
	public static final int PBS_STAT_NEED_PATH = 2;
	//};

	// TODO Move to Rail.java
	// these are the maximums used for updating signal blocks, and checking if a depot is in a pbs block
	//enum {
	public static final int NUM_SSD_ENTRY = 256; // max amount of blocks
	public static final int NUM_SSD_STACK = 32;// max amount of blocks to check recursively
	//};

	public static final int _TRACKDIR_BIT_MASK = Rail.TRACKDIR_BIT_MASK;//  (0x3F3F),

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

	static void PBSReserveTrack(TileIndex tile, /*Track*/ int track) 
	{
		assert(tile.IsValidTile());
		assert(track <= 5);
		
		switch (tile.GetTileType()) 
		{
			case MP_RAILWAY:
				if (WayPoint.IsRailWaypoint(tile)) {
					// waypoint
					tile.getMap().m3 = BitOps.RETSETBIT(tile.getMap().m3, 6);
				} else {
					// normal rail track
					int encrt = BitOps.GB(tile.getMap().m4, 4, 4); // get current encoded info (see comments at top of file)

					if (encrt == 0) // nothing reserved before
						encrt = track + 1;
					else if (encrt == (track^1) + 1) // opposite track reserved before
						encrt |= 8;

					tile.getMap().m4 =  BitOps.RETSB(tile.getMap().m4, 4, 4, encrt);
				}
				break;
			case MP_TUNNELBRIDGE:
				tile.getMap().m4 |= (1 << track) & 3;
				break;
			case MP_STATION:
				tile.getMap().m3 = BitOps.RETSETBIT(tile.getMap().m3, 6);
				break;
			case MP_STREET:
				// make sure it is a railroad crossing
				if (!tile.IsLevelCrossing()) return;
				tile.getMap().m5 = BitOps.RETSETBIT(tile.getMap().m5, 0);
				break;
			default:
				return;
		}
		// if debugging, mark tile dirty to show reserved status
		if (Global._debug_pbs_level >= 1)
			tile.MarkTileDirtyByTile();
	}

	static int PBSTileReserved(TileIndex tile) 
	{
		assert(tile.IsValidTile());
		
		switch (tile.GetTileType()) 
		{
			case MP_RAILWAY:
				if (WayPoint.IsRailWaypoint(tile)) {
					// waypoint
					// check if its reserved
					if (!BitOps.HASBIT(tile.getMap().m3, 6)) return 0;
					// return the track for the correct direction
					return BitOps.HASBIT(tile.getMap().m5, 0) ? 2 : 1;
				} else {
					// normal track
					int res = encrt_to_reserved[BitOps.GB(tile.getMap().m4, 4, 4)];
					assert(res != 0xFF);
					return res;
				}
			case MP_TUNNELBRIDGE:
				return BitOps.GB(tile.getMap().m4, 0, 2);
			case MP_STATION:
				// check if its reserved
				if (!BitOps.HASBIT(tile.getMap().m3, 6)) return 0;
				// return the track for the correct direction
				return BitOps.HASBIT(tile.getMap().m5, 0) ? 2 : 1;
			case MP_STREET:
				// make sure its a railroad crossing
				if (!tile.IsLevelCrossing()) return 0;
				// check if its reserved
				if (!BitOps.HASBIT(tile.getMap().m5, 0)) return 0;
				// return the track for the correct direction
				return BitOps.HASBIT(tile.getMap().m5, 3) ? 1 : 2;
			default:
				return 0;
		}
	}

	public static int PBSTileUnavail(TileIndex tile) {
		assert(tile.IsValidTile());
		switch (tile.GetTileType()) {
			case MP_RAILWAY:
				if (WayPoint.IsRailWaypoint(tile)) {
					// waypoint
					return BitOps.HASBIT(tile.getMap().m3, 6) ? _TRACKDIR_BIT_MASK : 0;
				} else {
					// normal track
					int res = encrt_to_unavail[BitOps.GB(tile.getMap().m4, 4, 4)];
					assert(res != 0xFFFF);
					return res;
				}
			case MP_TUNNELBRIDGE:
				return BitOps.GB(tile.getMap().m4, 0, 2) | (BitOps.GB(tile.getMap().m4, 0, 2) << 8);
			case MP_STATION:
				return BitOps.HASBIT(tile.getMap().m3, 6) ? _TRACKDIR_BIT_MASK : 0;
			case MP_STREET:
				// make sure its a railroad crossing
				if (!tile.IsLevelCrossing()) return 0;
				// check if its reserved
				return (BitOps.HASBIT(tile.getMap().m5, 0)) ? _TRACKDIR_BIT_MASK : 0;
			default:
				return 0;
		}
	}

	static void PBSClearTrack(TileIndex tile, /*Track*/int track) 
	{
		assert(tile.IsValidTile());
		assert(track <= 5);
		
		switch (tile.GetTileType()) 
		{
			case MP_RAILWAY:
				if (WayPoint.IsRailWaypoint(tile)) {
					// waypoint
					tile.getMap().m3 = BitOps.RETCLRBIT(tile.getMap().m3, 6);
				} else {
					// normal rail track
					int encrt = BitOps.GB(tile.getMap().m4, 4, 4);

					if (encrt == track + 1)
						encrt = 0;
					else if (encrt == track + 1 + 8)
						encrt = (track^1) + 1;
					else if (encrt == (track^1) + 1 + 8)
						encrt &= 7;

					tile.getMap().m4 =  BitOps.RETSB(tile.getMap().m4, 4, 4, encrt);
				}
				break;
			case MP_TUNNELBRIDGE:
				tile.getMap().m4 &= ~((1 << track) & 3);
				break;
			case MP_STATION:
				tile.getMap().m3 = BitOps.RETCLRBIT(tile.getMap().m3, 6);
				break;
			case MP_STREET:
				// make sure it is a railroad crossing
				if (!tile.IsLevelCrossing()) return;
				tile.getMap().m5 = BitOps.RETCLRBIT(tile.getMap().m5, 0);
				break;
			default:
				return;
		}
		// if debugging, mark tile dirty to show reserved status
		if (Global._debug_pbs_level >= 1)
			tile.MarkTileDirtyByTile();
	}

	static void PBSClearPath(TileIndex tile, /*Trackdir*/ int trackdir, TileIndex end_tile, /*Trackdir*/ int end_trackdir) {
		int res;
		FindLengthOfTunnelResult flotr;
		
		assert(tile.IsValidTile());
		assert(Rail.IsValidTrackdir(trackdir));

		do {
			PBSClearTrack(tile, Rail.TrackdirToTrack(trackdir));

			if (tile.equals(end_tile) && Rail.TrackdirToTrack(trackdir) == Rail.TrackdirToTrack(end_trackdir))
				return;

			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) &&
					BitOps.GB(tile.getMap().m5, 4, 4) == 0 &&
					BitOps.GB(tile.getMap().m5, 0, 2) == Rail.TrackdirToExitdir(trackdir)) {
				// this is a tunnel
				flotr = Pathfind.FindLengthOfTunnel(tile, Rail.TrackdirToExitdir(trackdir));

				tile = flotr.tile;
			} else {
				int exitdir = Rail.TrackdirToExitdir(trackdir);
				tile = TileIndex.AddTileIndexDiffCWrap(tile, TileIndex.TileIndexDiffCByDir(exitdir));
			}

			res = PBSTileReserved(tile);
			res |= res << 8;
			res &= Rail.TrackdirReachesTrackdirs(trackdir);
			trackdir = BitOps.FindFirstBit2x64(res);

		} while (res != 0);
	}

	static boolean PBSIsPbsSignal(TileIndex tile, /*Trackdir*/ int trackdir)
	{
		assert(tile.IsValidTile());
		assert(Rail.IsValidTrackdir(trackdir));

		if (!Global._patches.new_pathfinding_all)
			return false;

		if (!tile.IsTileType( TileTypes.MP_RAILWAY))
			return false;

		if (Rail.GetRailTileType(tile) != Rail.RAIL_TYPE_SIGNALS)
			return false;

		if (!Rail.HasSignalOnTrackdir(tile, trackdir))
			return false;

		/*if (Rail.GetSignalType(tile, Rail.TrackdirToTrack(trackdir)) == 4)
			return true;
		else
			return false;*/
		return Rail.GetSignalType(tile, Rail.TrackdirToTrack(trackdir)) == 4;
	}

	static class SetSignalsDataPbs {
		int cur;

		// these are used to keep track of the signals.
		final byte [] bit = new byte[NUM_SSD_ENTRY];
		final TileIndex [] tile = new TileIndex[NUM_SSD_ENTRY];
	} 

	// This function stores the signals inside the SetSignalsDataPbs struct, passed as callback to FollowTrack() in the PBSIsPbsSegment() function below
	//static boolean SetSignalsEnumProcPBS(TileIndex tile, SetSignalsDataPbs ssd, int trackdir, int length, int[] state)
	static boolean SetSignalsEnumProcPBS(TileIndex tile, Object o, int trackdir, int length, int[] state)
	{
		SetSignalsDataPbs ssd = (SetSignalsDataPbs) o;
		//TileIndex tile = new TileIndex(itile);
		//int itile = tile.tile;
		// the tile has signals?
		if (tile.IsTileType( TileTypes.MP_RAILWAY)) 
		{
			if (Rail.HasSignalOnTrack(tile, Rail.TrackdirToTrack(trackdir))) {

					if (ssd.cur != NUM_SSD_ENTRY) {
						ssd.tile[ssd.cur] = tile; // remember the tile index
						ssd.bit[ssd.cur] = (byte) Rail.TrackdirToTrack(trackdir); // and the controlling bit number
						ssd.cur++;
					}
					return true;
			} else if (Depot.IsTileDepotType(tile, TransportType.Rail))
				return true; // don't look further if the tile is a depot
		}
		return false;
	}

	/**<
	 * Checks if a signal/depot leads to a pbs block.
	 * This means that the block needs to have at least 1 signal, and that all signals in it need to be pbs signals.
	 * @param tile The tile to check
	 * @param trackdir The direction in which to check
	 * @return True when the depot is inside a pbs block
	 */

	static boolean PBSIsPbsSegment(TileIndex tilep, /*Trackdir*/ int trackdir)
	//boolean PBSIsPbsSegment(int tilep, Trackdir trackdir)
	{
		SetSignalsDataPbs ssd = new SetSignalsDataPbs();
		boolean result = PBSIsPbsSignal(tilep, trackdir);
		/*DiagDirection*/ int direction = Rail.TrackdirToExitdir(trackdir);//GetDepotDirection(tile,TRANSPORT_RAIL);
		int i;

		ssd.cur = 0;

		Pathfind.FollowTrack(tilep, TransportType.Rail, 0xC000, direction, Pbs::SetSignalsEnumProcPBS, null, ssd);
		
		for(i=0; i!=ssd.cur; i++) {
			TileIndex tile = ssd.tile[i];
			byte bit = ssd.bit[i];
			if (!PBSIsPbsSignal(tile, bit) && !PBSIsPbsSignal(tile, bit | 8))
				return false;
			result = true;
		}

		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
