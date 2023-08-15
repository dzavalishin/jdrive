package com.dzavalishin.game;

import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.struct.FindLengthOfTunnelResult;
import com.dzavalishin.tables.TrackPathFinderTables;
import com.dzavalishin.util.BitOps;

public abstract class Pathfind extends TrackPathFinderTables 
{


	//#define PF_BENCH // perform simple benchmarks on the train pathfinder (not
	//supported on all archs)


	//#define PATHFIND_GET_LINK_OFFS(tpf, link) ((byte*)(link) - (byte*)tpf.links)
	//#define PATHFIND_GET_LINK_PTR(tpf, link_offs) (TrackPathFinderLink*)((byte*)tpf.links + (link_offs))

	/* y7 y6 y5 y4 y3 y2 y1 y0 x7 x6 x5 x4 x3 x2 x1 x0
	 * y7 y6 y5 y4 y3 y2 y1 y0 x4 x3 x2 x1 x0  0  0  0
	 *  0  0 y7 y6 y5 y4 y3 y2 y1 y0 x4 x3 x2 x1 x0  0
	 *  0  0  0  0 y5 y4 y3 y2 y1 y0 x4 x3 x2 x1 x0  0
	 */
	//#define PATHFIND_HASH_TILE(tile) (tile.TileX() & 0x1F) + ((tile.TileY() & 0x1F) << 5)

	static int PATHFIND_HASH_TILE(TileIndex tile) 
	{ 
		return (tile.TileX() & 0x1F) + ((tile.TileY() & 0x1F) << 5); 
	}




	//void FollowTrack(TileIndex tile, int flags, byte direction, TPFEnumProc *enum_proc, TPFAfterProc *after_proc, void *data);

	/*class {
		TileIndex tile;
		int length;
	} FindLengthOfTunnelResult; */
	//FindLengthOfTunnelResult FindLengthOfTunnel(TileIndex tile, int direction);

	//void NewTrainPathfind(TileIndex tile, TileIndex dest, byte direction, NTPEnumProc *enum_proc, void *data);



	
	
	






	/* Returns the end tile and the length of a tunnel. The length does not
	 * include the starting tile (entry), it does include the end tile (exit).
	 */
	public static FindLengthOfTunnelResult FindLengthOfTunnel(TileIndex tile, int direction)
	{
		FindLengthOfTunnelResult flotr = new FindLengthOfTunnelResult();
		int x,y;
		int z;

		flotr.length = 0;

		x = tile.TileX() * 16;
		y = tile.TileY() * 16;

		z = Landscape.GetSlopeZ(x+8, y+8);

		for(;;) {
			flotr.length++;

			x += _get_tunlen_inc[direction];
			y += _get_tunlen_inc[direction+1];

			tile = TileIndex.TileVirtXY(x, y);

			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) &&
					BitOps.GB(tile.getMap().m5, 4, 4) == 0 &&               // tunnel entrance/exit
					// BitOps.GB(tile.getMap().m5, 2, 2) == type &&            // rail/road-tunnel <-- This is not necesary to check, right?
					(BitOps.GB(tile.getMap().m5, 0, 2) ^ 2) == direction && // entrance towards: 0 = NE, 1 = SE, 2 = SW, 3 = NW
							Landscape.GetSlopeZ(x + 8, y + 8) == z) {
				break;
			}
		}

		flotr.tile = tile;
		return flotr;
	}




	static void FollowTrack(TileIndex tile, TransportType type, int flags, int direction, TPFEnumProc enum_proc, TPFAfterProc after_proc, Object data)
	{
		TrackPathFinder tpf = new TrackPathFinder();

		assert(direction < 4);

		/* initialize path finder variables */
		tpf.userdata = data;
		tpf.enum_proc = enum_proc;
		//tpf.new_link = tpf.links[0];
		//tpf.num_links_left = tpf.links.length;

		tpf.rd.cur_length = 0;
		tpf.rd.depth = 0;
		tpf.rd.pft_var6 = 0;

		tpf.var2 = BitOps.HASBIT(flags, 15) ? 0x43 : 0xFF; /* 0x8000 */

		tpf.disable_tile_hash = BitOps.HASBIT(flags, 12);     /* 0x1000 */
		tpf.hasbit_13 = BitOps.HASBIT(flags, 13);		 /* 0x2000 */


		tpf.tracktype = type; // (byte)flags;

		if (BitOps.HASBIT(flags, 11)) {
			tpf.rd.pft_var6 = 0xFF;
			tpf.enum_proc.enumerate(tile, data, 0, 0, null);
			tpf.TPFMode2(tile, direction);
		} else {
			/* clear the hash_heads */
			//memset(tpf.hash_head, 0, sizeof(tpf.hash_head));
			//tpf.hash_head.clear();
			tpf.TPFMode1(tile, direction);
		}

		if (after_proc != null)
			after_proc.accept(tpf);
	}



	//#define NTP_GET_LINK_OFFS(tpf, link) ((byte*)(link) - (byte*)tpf.links)
	//#define NTP_GET_LINK_PTR(tpf, link_offs) (HashLink*)((byte*)tpf.links + (link_offs))

	//#define ARR(i) tpf.stack[(i)-1]









	static int DistanceMoo(TileIndex t0, TileIndex t1)
	{
		final int dx = Math.abs(t0.TileX() - t1.TileX());
		final int dy = Math.abs(t0.TileY() - t1.TileY());

		final int straightTracks = 2 * Math.min(dx, dy); /* The number of straight (not full length) tracks */
		/* OPTIMISATION:
		 * Original: diagTracks = Math.max(dx, dy) - Math.min(dx,dy);
		 * Proof:
		 * (dx-dy) - straightTracks  == (min + max) - straightTracks = min + // max - 2 * min = max - min */
		final int diagTracks = dx + dy - straightTracks; /* The number of diagonal (full tile length) tracks. */

		return diagTracks*DIAG_FACTOR + straightTracks*STR_FACTOR;
	}




	// new pathfinder for trains. better and faster.
	static void NewTrainPathfind(TileIndex tile, TileIndex dest, int direction, NTPEnumProc enum_proc, Object data)
	{
		NewTrackPathFinder tpf = new NewTrackPathFinder();

		tpf.dest = dest;
		tpf.userdata = data;
		tpf.enum_proc = enum_proc;
		tpf.tracktype = 0;
		tpf.maxlength = Math.min(Global._patches.pf_maxlength * 3, 10000);
		//tpf.nstack = 0;
		//tpf.new_link = tpf.links[0];
		//tpf.num_links_left = tpf.links.length;
		
		//memset(tpf.hash_head, 0, sizeof(tpf.hash_head));
		//tpf.hash_head.clear();
		//tpf.hash_head = new int[0x400]; // hash heads. 0 means unused. 0xFFFC = length, 0x3 = dir

		tpf.NTPEnum(tile, direction);
	}
	
	
}

//typedef boolean NTPEnumProc(TileIndex tile, void *data, int track, int length);
@FunctionalInterface
interface NTPEnumProc
{
	boolean enumerate(TileIndex tile, Object data, int track, int length);
}

//typedef boolean TPFEnumProc(TileIndex tile, void *data, int track, int length, byte *state);
@FunctionalInterface
interface TPFEnumProc
{
	boolean enumerate(TileIndex tile, Object data, int track, int length, int[] state);
}

//typedef void TPFAfterProc(TrackPathFinder *tpf);
@FunctionalInterface
interface TPFAfterProc
{
	void accept(TrackPathFinder tpf);
}

