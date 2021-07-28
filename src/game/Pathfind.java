package game;

public class Pathfind 
{


	//#define PF_BENCH // perform simple benchmarks on the train pathfinder (not
	//supported on all archs)

	//class TrackPathFinder TrackPathFinder;
	//typedef boolean TPFEnumProc(TileIndex tile, void *data, int track, int length, byte *state);
	//typedef void TPFAfterProc(TrackPathFinder *tpf);
	//typedef boolean NTPEnumProc(TileIndex tile, void *data, int track, int length);

	//#define PATHFIND_GET_LINK_OFFS(tpf, link) ((byte*)(link) - (byte*)tpf.links)
	//#define PATHFIND_GET_LINK_PTR(tpf, link_offs) (TrackPathFinderLink*)((byte*)tpf.links + (link_offs))

	/* y7 y6 y5 y4 y3 y2 y1 y0 x7 x6 x5 x4 x3 x2 x1 x0
	 * y7 y6 y5 y4 y3 y2 y1 y0 x4 x3 x2 x1 x0  0  0  0
	 *  0  0 y7 y6 y5 y4 y3 y2 y1 y0 x4 x3 x2 x1 x0  0
	 *  0  0  0  0 y5 y4 y3 y2 y1 y0 x4 x3 x2 x1 x0  0
	 */
	//#define PATHFIND_HASH_TILE(tile) (TileX(tile) & 0x1F) + ((TileY(tile) & 0x1F) << 5)

	class TrackPathFinderLink {
		TileIndex tile;
		int flags;
		int next;
	} 

	class RememberData {
		int cur_length;
		byte depth;
		byte pft_var6;
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
	FindLengthOfTunnelResult FindLengthOfTunnel(TileIndex tile, int direction)
	{
		FindLengthOfTunnelResult flotr;
		int x,y;
		byte z;

		flotr.length = 0;

		x = TileX(tile) * 16;
		y = TileY(tile) * 16;

		z = GetSlopeZ(x+8, y+8);

		for(;;) {
			flotr.length++;

			x += _get_tunlen_inc[direction];
			y += _get_tunlen_inc[direction+1];

			tile = TileVirtXY(x, y);

			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) &&
					BitOps.GB(tile.getMap().m5, 4, 4) == 0 &&               // tunnel entrance/exit
					// BitOps.GB(tile.getMap().m5, 2, 2) == type &&            // rail/road-tunnel <-- This is not necesary to check, right?
					(BitOps.GB(tile.getMap().m5, 0, 2) ^ 2) == direction && // entrance towards: 0 = NE, 1 = SE, 2 = SW, 3 = NW
					GetSlopeZ(x + 8, y + 8) == z) {
				break;
			}
		}

		flotr.tile = tile;
		return flotr;
	}




	void FollowTrack(TileIndex tile, int flags, byte direction, TPFEnumProc *enum_proc, TPFAfterProc *after_proc, void *data)
	{
		TrackPathFinder tpf;

		assert(direction < 4);

		/* initialize path finder variables */
		tpf.userdata = data;
		tpf.enum_proc = enum_proc;
		tpf.new_link = tpf.links;
		tpf.num_links_left = lengthof(tpf.links);

		tpf.rd.cur_length = 0;
		tpf.rd.depth = 0;
		tpf.rd.pft_var6 = 0;

		tpf.var2 = BitOps.HASBIT(flags, 15) ? 0x43 : 0xFF; /* 0x8000 */

		tpf.disable_tile_hash = BitOps.HASBIT(flags, 12) != 0;     /* 0x1000 */
		tpf.hasbit_13 = BitOps.HASBIT(flags, 13) != 0;		 /* 0x2000 */


		tpf.tracktype = (byte)flags;

		if (BitOps.HASBIT(flags, 11)) {
			tpf.rd.pft_var6 = 0xFF;
			tpf.enum_proc(tile, data, 0, 0, 0);
			TPFMode2(&tpf, tile, direction);
		} else {
			/* clear the hash_heads */
			memset(tpf.hash_head, 0, sizeof(tpf.hash_head));
			TPFMode1(&tpf, tile, direction);
		}

		if (after_proc != null)
			after_proc(&tpf);
	}

	class {
		TileIndex tile;
		int cur_length; // This is the current length to this tile.
		int priority; // This is the current length + estimated length to the goal.
		byte track;
		byte depth;
		byte state;
		byte first_track;
	} StackedItem;


	class HashLink {
		TileIndex tile;
		int typelength;
		int next;
	} HashLink;


	//#define NTP_GET_LINK_OFFS(tpf, link) ((byte*)(link) - (byte*)tpf.links)
	//#define NTP_GET_LINK_PTR(tpf, link_offs) (HashLink*)((byte*)tpf.links + (link_offs))

	//#define ARR(i) tpf.stack[(i)-1]









	static int DistanceMoo(TileIndex t0, TileIndex t1)
	{
		final int dx = abs(TileX(t0) - TileX(t1));
		final int dy = abs(TileY(t0) - TileY(t1));

		final int straightTracks = 2 * Math.min(dx, dy); /* The number of straight (not full length) tracks */
		/* OPTIMISATION:
		 * Original: diagTracks = Math.max(dx, dy) - Math.min(dx,dy);
		 * Proof:
		 * (dx-dy) - straightTracks  == (min + max) - straightTracks = min + // max - 2 * min = max - min */
		final int diagTracks = dx + dy - straightTracks; /* The number of diagonal (full tile length) tracks. */

		return diagTracks*DIAG_FACTOR + straightTracks*Str.STR_FACTOR;
	}




	// new pathfinder for trains. better and faster.
	void NewTrainPathfind(TileIndex tile, TileIndex dest, byte direction, NTPEnumProc *enum_proc, void *data)
	{
		NewTrackPathFinder tpf;

		tpf.dest = dest;
		tpf.userdata = data;
		tpf.enum_proc = enum_proc;
		tpf.tracktype = 0;
		tpf.maxlength = Math.min(Global._patches.pf_maxlength * 3, 10000);
		tpf.nstack = 0;
		tpf.new_link = tpf.links;
		tpf.num_links_left = lengthof(tpf.links);
		memset(tpf.hash_head, 0, sizeof(tpf.hash_head));

		NTPEnum(&tpf, tile, direction);
	}
	
	
}
