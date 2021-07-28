package game;

import game.tables.TrackPathFinderTables;

public class NewTrackPathFinder extends TrackPathFinderTables 
{
	// -------------------------------------------------
	// Fields
	// -------------------------------------------------
	NTPEnumProc *enum_proc;
	Object userdata;
	TileIndex dest;

	byte tracktype;
	int maxlength;

	HashLink *new_link;
	int num_links_left;

	int nstack;
	StackedItem stack[256]; // priority queue of stacked items

	int hash_head[0x400]; // hash heads. 0 means unused. 0xFFFC = length, 0x3 = dir
	TileIndex hash_tile[0x400]; // tiles. or links.

	HashLink links[0x400]; // hash links

	// -------------------------------------------------
	// Static data
	// -------------------------------------------------

	
	// -------------------------------------------------
	// Methods
	// -------------------------------------------------


	
	// mark a tile as visited and store the length of the path.
	// if we already had a better path to this tile, return false.
	// otherwise return true.
	static boolean NtpVisit(NewTrackPathFinder tpf, TileIndex tile, int dir, int length)
	{
		int hash,head;
		HashLink *link, *new_link;

		assert(length < 16384-1);

		hash = PATHFIND_HASH_TILE(tile);

		// never visited before?
		if ((head=tpf.hash_head[hash]) == 0) {
			tpf.hash_tile[hash] = tile;
			tpf.hash_head[hash] = dir | (length << 2);
			return true;
		}

		if (head != 0xffff) {
			if (tile == tpf.hash_tile[hash] && (head & 0x3) == dir) {

				// longer length
				if (length >= (head >> 2)) return false;

				tpf.hash_head[hash] = dir | (length << 2);
				return true;
			}
			// two tiles with the same hash, need to make a link
			// allocate a link. if out of links, handle this by returning
			// that a tile was already visisted.
			if (tpf.num_links_left == 0) {
				DEBUG(ntp, 1) ("[NTP] no links left");
				return false;
			}

			tpf.num_links_left--;
			link = tpf.new_link++;

			/* move the data that was previously in the hash_??? variables
			 * to the link struct, and let the hash variables point to the link */
			link.tile = tpf.hash_tile[hash];
			tpf.hash_tile[hash] = NTP_GET_LINK_OFFS(tpf, link);

			link.typelength = tpf.hash_head[hash];
			tpf.hash_head[hash] = 0xFFFF; /* multi link */
			link.next = 0xFFFF;
		} else {
			// a linked list of many tiles,
			// find the one corresponding to the tile, if it exists.
			// otherwise make a new link

			int offs = tpf.hash_tile[hash];
			do {
				link = NTP_GET_LINK_PTR(tpf, offs);
				if (tile == link.tile && (int)(link.typelength & 0x3) == dir) {
					if (length >= (int)(link.typelength >> 2)) return false;
					link.typelength = dir | (length << 2);
					return true;
				}
			} while ((offs=link.next) != 0xFFFF);
		}

		/* get here if we need to add a new link to link,
		 * first, allocate a new link, in the same way as before */
		if (tpf.num_links_left == 0) {
			DEBUG(ntp, 1) ("[NTP] no links left");
			return false;
		}
		tpf.num_links_left--;
		new_link = tpf.new_link++;

		/* then fill the link with the new info, and establish a ptr from the old
		 * link to the new one */
		new_link.tile = tile;
		new_link.typelength = dir | (length << 2);
		new_link.next = 0xFFFF;

		link.next = NTP_GET_LINK_OFFS(tpf, new_link);
		return true;
	}
	
	
	
	
	
	/**
	 * Checks if the shortest path to the given tile/dir so far is still the given
	 * length.
	 * @return true if the length is still the same
	 * @pre    The given tile/dir combination should be present in the hash, by a
	 *         previous call to NtpVisit().
	 */
	public boolean NtpCheck( TileIndex tile, int dir, int length)
	{
		NewTrackPathFinder tpf = this;
		
		int hash,head,offs;
		HashLink *link;

		hash = PATHFIND_HASH_TILE(tile);
		head=tpf.hash_head[hash];
		assert(head);

		if (head != 0xffff) {
			assert( tpf.hash_tile[hash] == tile && (head & 3) == dir);
			assert( (head >> 2) <= length);
			return length == (head >> 2);
		}

		// else it's a linked list of many tiles
		offs = tpf.hash_tile[hash];
		for(;;) {
			link = NTP_GET_LINK_PTR(tpf, offs);
			if (tile == link.tile && (int)(link.typelength & 0x3) == dir) {
				assert( (int)(link.typelength >> 2) <= length);
				return length == (int)(link.typelength >> 2);
			}
			offs = link.next;
			assert(offs != 0xffff);
		}
	}
	
	
	
	// new more optimized pathfinder for trains...
	// Tile is the tile the train is at.
	// direction is the tile the train is moving towards.

	public void NTPEnum(TileIndex tile, int direction)
	{
		NewTrackPathFinder tpf = this;

		TrackBits bits, allbits;
		int track;
		TileIndex tile_org;
		StackedItem si;
		FindLengthOfTunnelResult flotr;
		int estimation;



		// Need to have a special case for the start.
		// We shouldn't call the callback for the current tile.
		si.cur_length = 1; // Need to start at 1 cause 0 is a reserved value.
		si.depth = 0;
		si.state = 0;
		si.first_track = 0xFF;
		goto start_at;

		for(;;) {
			// Get the next item to search from from the priority queue
			do {
				if (tpf.nstack == 0)
					return; // nothing left? then we're done!
				si = tpf.stack[0];
				tile = si.tile;

				HeapifyDown(tpf);
				// Make sure we havn't already visited this tile.
			} while (!NtpCheck(tpf, tile, _tpf_prev_direction[si.track], si.cur_length));

			// Add the length of this track.
			si.cur_length += _length_of_track[si.track];

	callback_and_continue:
			if (tpf.enum_proc(tile, tpf.userdata, si.first_track, si.cur_length))
				return;

			assert(si.track <= 13);
			direction = _tpf_new_direction[si.track];

	start_at:
			// If the tile is the entry tile of a tunnel, and we're not going out of the tunnel,
			//   need to find the exit of the tunnel.
			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)) {
				if (BitOps.GB(tile.getMap().m5, 4, 4) == 0 &&
						BitOps.GB(tile.getMap().m5, 0, 2) != (direction ^ 2)) {
					/* This is a tunnel tile */
					/* We are not just driving out of the tunnel */
					if (BitOps.GB(tile.getMap().m5, 0, 2) != direction || BitOps.GB(tile.getMap().m5, 2, 2) != tpf.tracktype)
						/* We are not driving into the tunnel, or it
						 * is an invalid tunnel */
						continue;
					flotr = FindLengthOfTunnel(tile, direction);
					si.cur_length += flotr.length * DIAG_FACTOR;
					tile = flotr.tile;
					// tile now points to the exit tile of the tunnel
				}
			}

			// This is a special loop used to go through
			// a rail net and find the first intersection
			tile_org = tile;
			for(;;) {
				assert(direction <= 3);
				tile += TileOffsByDir(direction);

				// too long search length? bail out.
				if (si.cur_length >= tpf.maxlength) {
					DEBUG(ntp,1) ("[NTP] cur_length too big");
					bits = 0;
					break;
				}

				// Not a regular rail tile?
				// Then we can't use the code below, but revert to more general code.
				if (!tile.IsTileType( TileTypes.MP_RAILWAY) || !IsPlainRailTile(tile)) {
					// We found a tile which is not a normal railway tile.
					// Determine which tracks that exist on this tile.
					bits = GetTileTrackStatus(tile, TRANSPORT_RAIL) & _tpfmode1_and[direction];
					bits = (bits | (bits >> 8)) & 0x3F;

					// Check that the tile contains exactly one track
					if (bits == 0 || KILL_FIRST_BIT(bits) != 0) break;

					///////////////////
					// If we reach here, the tile has exactly one track.
					//   tile - index to a tile that is not rail tile, but still straight (with optional signals)
					//   bits - bitmask of which track that exist on the tile (exactly one bit is set)
					//   direction - which direction are we moving in?
					///////////////////
					si.track = _new_track[FIND_FIRST_BIT(bits)][direction];
					si.cur_length += _length_of_track[si.track];
					goto callback_and_continue;
				}

				/* Regular rail tile, determine which tracks exist. */
				allbits = tile.getMap().m5 & TRACK_BIT_MASK;
				/* Which tracks are reachable? */
				bits = allbits & DiagdirReachesTracks(direction);

				/* The tile has no reachable tracks => End of rail segment
				 * or Intersection => End of rail segment. We check this agains all the
				 * bits, not just reachable ones, to prevent infinite loops. */
				if (bits == 0 || TracksOverlap(allbits)) break;

				/* If we reach here, the tile has exactly one track, and this
				 track is reachable => Rail segment continues */

				track = _new_track[FIND_FIRST_BIT(bits)][direction];
				assert(track != 0xff);

				si.cur_length += _length_of_track[track];

				// Check if this rail is an upwards slope. If it is, then add a penalty.
				// Small optimization here.. if (track&7)>1 then it can't be a slope so we avoid calling GetTileSlope
				if ((track & 7) <= 1 && (_is_upwards_slope[GetTileSlope(tile, null)] & (1 << track)) ) {
					// upwards slope. add some penalty.
					si.cur_length += 4*DIAG_FACTOR;
				}

				// railway tile with signals..?
				if (HasSignals(tile)) {
					byte m3;

					m3 = tile.getMap().m3;
					if (!(m3 & SignalAlongTrackdir(track))) {
						// if one way signal not pointing towards us, stop going in this direction => End of rail segment.
						if (m3 & SignalAgainstTrackdir(track)) {
							bits = 0;
							break;
						}
					} else if (tile.getMap().m2 & SignalAlongTrackdir(track)) {
						// green signal in our direction. either one way or two way.
						si.state |= 3;
					} else {
						// reached a red signal.
						if (m3 & SignalAgainstTrackdir(track)) {
							// two way red signal. unless we passed another green signal on the way,
							// stop going in this direction => End of rail segment.
							// this is to prevent us from going into a full platform.
							if (!(si.state&1)) {
								bits = 0;
								break;
							}
						}
						if (!(si.state & 2)) {
							// Is this the first signal we see? And it's red... add penalty
							si.cur_length += 10*DIAG_FACTOR;
							si.state += 2; // remember that we added penalty.
							// Because we added a penalty, we can't just continue as usual.
							// Need to get out and let A* do it's job with
							// possibly finding an even shorter path.
							break;
						}
					}

					if (tpf.enum_proc(tile, tpf.userdata, si.first_track, si.cur_length))
						return; /* Don't process this tile any further */
				}

				// continue with the next track
				direction = _tpf_new_direction[track];

				// safety check if we're running around chasing our tail... (infinite loop)
				if (tile == tile_org) {
					bits = 0;
					break;
				}
			}

			// There are no tracks to choose between.
			// Stop searching in this direction
			if (bits == 0)
				continue;

			////////////////
			// We got multiple tracks to choose between (intersection).
			// Branch the search space into several branches.
			////////////////

			// Check if we've already visited this intersection.
			// If we've already visited it with a better length, then
			// there's no point in visiting it again.
			if (!NtpVisit(tpf, tile, direction, si.cur_length))
				continue;

			// Push all possible alternatives that we can reach from here
			// onto the priority heap.
			// 'bits' contains the tracks that we can choose between.

			// First compute the estimated distance to the target.
			// This is used to implement A*
			estimation = 0;
			if (tpf.dest != 0)
				estimation = DistanceMoo(tile, tpf.dest);

			si.depth++;
			if (si.depth == 0)
				continue; /* We overflowed our depth. No more searching in this direction. */
			si.tile = tile;
			do {
				si.track = _new_track[FIND_FIRST_BIT(bits)][direction];
				assert(si.track != 0xFF);
				si.priority = si.cur_length + estimation;

				// out of stack items, bail out?
				if (tpf.nstack >= lengthof(tpf.stack)) {
					DEBUG(ntp, 1) ("[NTP] out of stack");
					break;
				}

				tpf.stack[tpf.nstack] = si;
				HeapifyUp(tpf);
			} while ((bits = KILL_FIRST_BIT(bits)) != 0);

			// If this is the first intersection, we need to fill the first_track member.
			// so the code outside knows which path is better.
			// also randomize the order in which we search through them.
			if (si.depth == 1) {
				assert(tpf.nstack == 1 || tpf.nstack == 2 || tpf.nstack == 3);
				if (tpf.nstack != 1) {
					int r = Hal.Random();
					if (r&1) swap_byte(&tpf.stack[0].track, &tpf.stack[1].track);
					if (tpf.nstack != 2) {
						byte t = tpf.stack[2].track;
						if (r&2) swap_byte(&tpf.stack[0].track, &t);
						if (r&4) swap_byte(&tpf.stack[1].track, &t);
						tpf.stack[2].first_track = tpf.stack[2].track = t;
					}
					tpf.stack[0].first_track = tpf.stack[0].track;
					tpf.stack[1].first_track = tpf.stack[1].track;
				}
			}

			// Continue with the next from the queue...
		}
	}

	
	
	// called after a new element was added in the queue at the last index.
	// move it down to the proper position
	static  void HeapifyUp(NewTrackPathFinder tpf)
	{
		StackedItem si;
		int i = ++tpf.nstack;

		while (i != 1 && ARR(i).priority < ARR(i>>1).priority) {
			// the child element is larger than the parent item.
			// swap the child item and the parent item.
			si = ARR(i); ARR(i) = ARR(i>>1); ARR(i>>1) = si;
			i>>=1;
		}
	}

	
	// called after the element 0 was eaten. fill it with a new element
	void HeapifyDown()
	{
		NewTrackPathFinder tpf = this;
		
		StackedItem si;
		int i = 1, j;
		int n;

		assert(tpf.nstack > 0);
		n = --tpf.nstack;

		if (n == 0) return; // heap is empty so nothing to do?

		// copy the last item to index 0. we use it as base for heapify.
		ARR(1) = ARR(n+1);

		while ((j=i*2) <= n) {
			// figure out which is smaller of the children.
			if (j != n && ARR(j).priority > ARR(j+1).priority)
				j++; // right item is smaller

			assert(i <= n && j <= n);
			if (ARR(i).priority <= ARR(j).priority)
				break; // base elem smaller than smallest, done!

			// swap parent with the child
			si = ARR(i); ARR(i) = ARR(j); ARR(j) = si;
			i = j;
		}
	}
	
	// -------------------------------------------------
	// Static methods
	// -------------------------------------------------
	
	
	
	
	
}
