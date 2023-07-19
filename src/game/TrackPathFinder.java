package game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import game.enums.TileTypes;
import game.enums.TransportType;
import game.struct.FindLengthOfTunnelResult;
import game.struct.RememberData;
import game.util.BitOps;

public class TrackPathFinder extends Pathfind 
{
	// -------------------------------------------------
	// Fields
	// -------------------------------------------------
	//int num_links_left;
	//TrackPathFinderLink new_link;

	TPFEnumProc enum_proc;

	Object userdata;

	RememberData rd = new RememberData();

	int the_dir;

	TransportType tracktype;
	int var2;
	boolean disable_tile_hash;
	boolean hasbit_13;

	//int [] hash_head = new int[0x400];
	//TileIndex [] hash_tile = new TileIndex[0x400]; /* stores the link index when multi link. */
	//TrackPathFinderLink [] links = new TrackPathFinderLink[0x400]; /* hopefully, this is enough. */

	final Map<Integer,TPFHashEnt> tileBits = new HashMap<>();

	// -------------------------------------------------
	// Class
	// -------------------------------------------------

	static class TPFHashEnt
	{
		int bits = 0;
	}

	// -------------------------------------------------
	// Static data
	// -------------------------------------------------


	// -------------------------------------------------
	// Methods
	// -------------------------------------------------


	boolean TPFSetTileBit(TileIndex tile, int dir)
	{
		int bits = 1 << dir;

		if (disable_tile_hash)
			return true;

		TPFHashEnt e = tileBits.get(tile.getTileIndex());
		/* unused hash entry, set the appropriate bit in it and return true
		 * to indicate that a bit was set. */
		if( e == null )
		{
			e = new TPFHashEnt();
			e.bits = bits;
			tileBits.put(tile.getTileIndex(), e);
			return true;
		}
		else
		{
			/* found another bit for the same tile,
			 * check if this bit is already set, if so, return false */
			if(0!=(e.bits & bits))
				return false;

			/* otherwise set the bit and return true to indicate that the bit
			 * was set */
			e.bits |= bits;
			return true;		
		}
	}

	
	public Iterator<TPFHashEnt> getIterator() {
		return tileBits.values().iterator();
	}


	/*
	// remember which tiles we have already visited so we don't visit them again.
	static boolean TPFSetTileBit(TrackPathFinder tpf, TileIndex tile, int dir)
	{
		int hash, val, offs;
		TrackPathFinderLink link, new_link;
		int bits = 1 << dir;

		if (tpf.disable_tile_hash)
			return true;

		hash = Pathfind.PATHFIND_HASH_TILE(tile);

		val = tpf.hash_head[hash];

		if (val == 0) {
			// unused hash entry, set the appropriate bit in it and return true
			// to indicate that a bit was set. *
			tpf.hash_head[hash] = bits;
			tpf.hash_tile[hash] = tile;
			return true;
		} else if (0==(val & 0x8000)) {
			// single tile 

			if (tile.equals(tpf.hash_tile[hash])) {
				//* found another bit for the same tile,
				// * check if this bit is already set, if so, return false *
				if(0!=(val & bits))
					return false;

				//* otherwise set the bit and return true to indicate that the bit
				/ * was set *
				tpf.hash_head[hash] = val | bits;
				return true;
			} else {
				//* two tiles with the same hash, need to make a link 

				//* allocate a link. if out of links, handle this by returning
				// * that a tile was already visisted. *
				if (tpf.num_links_left == 0) {
					return false;
				}
				tpf.num_links_left--;
				link = tpf.new_link++;

				//* move the data that was previously in the hash_??? variables
				// * to the link struct, and let the hash variables point to the link 
				link.tile = tpf.hash_tile[hash];
				tpf.hash_tile[hash] = PATHFIND_GET_LINK_OFFS(tpf, link);

				link.flags = tpf.hash_head[hash];
				tpf.hash_head[hash] = 0xFFFF; // multi link 

				link.next = 0xFFFF;
			}
		} else {
			//* a linked list of many tiles,
			// * find the one corresponding to the tile, if it exists.
			// * otherwise make a new link 

			offs = tpf.hash_tile[hash];
			do {
				link = PATHFIND_GET_LINK_PTR(tpf, offs);
				if (tile.equals(link.tile)) {
					//* found the tile in the link list,
					// * check if the bit was alrady set, if so return false to indicate that the
					// * bit was already set 
					if(0!=(link.flags & bits))
						return false;
					link.flags |= bits;
					return true;
				}
			} while ((offs=link.next) != 0xFFFF);
		}

		//* get here if we need to add a new link to link,
		// * first, allocate a new link, in the same way as before 
		if (tpf.num_links_left == 0) {
			return false;
		}
		tpf.num_links_left--;
		new_link = tpf.new_link++;

		//* then fill the link with the new info, and establish a ptr from the old
		// * link to the new one *
		new_link.tile = tile;
		new_link.flags = bits;
		new_link.next = 0xFFFF;

		link.next = PATHFIND_GET_LINK_OFFS(tpf, new_link);
		return true;
	} */



	void TPFMode1(TileIndex tilep, int direction)
	{
		//TrackPathFinder tpf = this;
		MutableTileIndex tile = new MutableTileIndex(tilep); 

		int bits;
		//int i;
		//RememberData rdCopy;
		TileIndex tile_org = tile;

		if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && BitOps.GB(tile.getMap().m5, 4, 4) == 0) {
			if (BitOps.GB(tile.getMap().m5, 0, 2) != direction ||
					BitOps.GB(tile.getMap().m5, 2, 2) != tracktype.getValue()) {
				return;
			}
			tile = new MutableTileIndex( SkipToEndOfTunnel(tile, direction) );
		}
		tile.madd( TileIndex.TileOffsByDir(direction) );

		/* Check in case of rail if the owner is the same */
		if (tracktype == TransportType.Rail) {
			if (tile.IsTileType( TileTypes.MP_RAILWAY) || tile.IsTileType( TileTypes.MP_STATION) || tile.IsTileType( TileTypes.MP_TUNNELBRIDGE))
				if (tile.IsTileType( TileTypes.MP_RAILWAY) || tile.IsTileType( TileTypes.MP_STATION) || tile.IsTileType( TileTypes.MP_TUNNELBRIDGE))
					/* Check if we are on a bridge (middle parts don't have an owner */
					if (!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) || (tile.getMap().m5 & 0xC0) != 0xC0)
						if (!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) || (tile_org.getMap().m5 & 0xC0) != 0xC0)
							if (!tile_org.GetTileOwner().equals(tile.GetTileOwner()))
								return;
		}

		rd.cur_length++;

		bits = Landscape.GetTileTrackStatus(tile, tracktype);

		if (bits != var2) {
			bits &= _tpfmode1_and[direction];
			bits = bits | (bits>>8);
		}
		bits &= 0xBF;

		if (bits != 0) {
			if (!disable_tile_hash || (rd.cur_length <= 64 && (BitOps.KILL_FIRST_BIT(bits) == 0 || ++rd.depth <= 7))) {
				do {
					int i = BitOps.FIND_FIRST_BIT(bits);
					bits = BitOps.KILL_FIRST_BIT(bits);

					the_dir = (_otherdir_mask[direction] & (1 << i)) != 0 ? (i+8) : i;
					RememberData rdCopy = new RememberData(rd);

					{
						int [] iptr = { rd.pft_var6 };
						if (TPFSetTileBit(tile, the_dir) &&
								!enum_proc.enumerate(tile, userdata, the_dir, rd.cur_length, iptr) ) 
						{
							rd.pft_var6 = iptr[0];
							TPFMode1(tile, _tpf_new_direction[the_dir]);
						}
						else
						{
							rd.pft_var6 = iptr[0];
						}
					}
					rd = rdCopy;
				} while (bits != 0);
			}
		}

		/* the next is only used when signals are checked.
		 * seems to go in 2 directions simultaneously */

		/* if i can get rid of this, tail end recursion can be used to minimize
		 * stack space dramatically. */

		/* If we are doing signal setting, we must reverse at evere tile, so we
		 * iterate all the tracks in a signal block, even when a normal train would
		 * not reach it (for example, when two lines merge */
		if (hasbit_13)
			return;

		tile = new MutableTileIndex( tile_org );
		direction ^= 2;

		bits = Landscape.GetTileTrackStatus(tile, tracktype);
		bits |= (bits >> 8);

		if ( (0xFF & bits) != var2) {
			bits &= _bits_mask[direction];
		}

		bits &= 0xBF;
		if (bits == 0)
			return;

		do {
			int i = BitOps.FIND_FIRST_BIT(bits);
			bits = BitOps.KILL_FIRST_BIT(bits);

			the_dir = (_otherdir_mask[direction] & (1 << i)) != 0 ? (i+8) : i;
			RememberData rdCopy = new RememberData(rd);
			if (TPFSetTileBit(tile, the_dir) ) 
			{
				int [] iptr = { rd.pft_var6 };
				boolean ret = enum_proc.enumerate(tile, userdata, the_dir, rd.cur_length, iptr);
				rd.pft_var6 = iptr[0];

				if(!ret ) {
					TPFMode1(tile, _tpf_new_direction[the_dir]);
				}
			}
			rd = rdCopy;
		} while (bits != 0);
	}




	public void TPFMode2( TileIndex tile, int direction)
	{
		//TrackPathFinder tpf = this;

		int bits;
		int i = 0;
		RememberData _rd = null;
		int owner = -1;

		/* XXX: Mode 2 is currently only used for ships, why is this code here? */
		if (tracktype == TransportType.Rail) {
			if (tile.IsTileType( TileTypes.MP_RAILWAY) || tile.IsTileType( TileTypes.MP_STATION) || tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)) {
				owner = tile.GetTileOwner().id;
				/* Check if we are on the middle of a bridge (has no owner) */
				if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && (tile.getMap().m5 & 0xC0) == 0xC0)
					owner = -1;
			}
		}

		// This addition will sometimes overflow by a single tile.
		// The use of TILE_MASK here makes sure that we still point at a valid
		// tile, and then this tile will be in the sentinel row/col, so GetTileTrackStatus will fail.
		//tile = TILE_MASK(tile + TileIndex.TileOffsByDir(direction));
		tile = tile.iadd(TileIndex.TileOffsByDir(direction));
		tile.TILE_MASK();

		/* Check in case of rail if the owner is the same */
		if (tracktype == TransportType.Rail) {
			if (tile.IsTileType( TileTypes.MP_RAILWAY) || tile.IsTileType( TileTypes.MP_STATION) || tile.IsTileType( TileTypes.MP_TUNNELBRIDGE))
				/* Check if we are on the middle of a bridge (has no owner) */
				if (!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) || (tile.getMap().m5 & 0xC0) != 0xC0)
					if (owner != -1 && !tile.IsTileOwner(owner))
						return;
		}

		if (++rd.cur_length > 50)
			return;

		bits = Landscape.GetTileTrackStatus(tile, tracktype);
		bits = 0xFF & ((bits | (bits >> 8)) & _bits_mask[direction]);
		if (bits == 0)
			return;

		assert(tile.TileX() != Global.MapMaxX() && tile.TileY() != Global.MapMaxY());

		boolean skipStart = false;

		if ( (bits & (bits - 1)) == 0 ) {
			/* only one direction */
			i = 0;
			while (0==(bits&1))
			{
				i++;
				bits>>=1;
			}

			_rd = new RememberData(rd);
			//goto continue_here;
			skipStart = true;
		}
		/* several directions */
		if(!skipStart) i=0;
		do {
			if( !skipStart )
			{
				if (0==(bits & 1))
				{
					++i;
					continue;
				}
				_rd = new RememberData(rd);

				// Change direction 4 times only
				if ((byte)i != rd.pft_var6) {
					if(++rd.depth > 4) {
						rd = _rd;
						return;
					}
					rd.pft_var6 = i;
				}
			}
			//continue_here:;
			skipStart = false;

			the_dir = BitOps.HASBIT(_otherdir_mask[direction],i) ? (i+8) : i;

			if (!enum_proc.enumerate(tile, userdata, the_dir, rd.cur_length, null)) {
				TPFMode2( tile, _tpf_new_direction[the_dir]);
			}

			rd = _rd;
			
			++i;
		} while ( (bits>>>=1) != 0);

	}












	public Set<Entry<Integer, TPFHashEnt>> entrySet() {
		return tileBits.entrySet();
	}




	// -------------------------------------------------
	// Static methods
	// -------------------------------------------------


	TileIndex SkipToEndOfTunnel(TileIndex tile, int direction)
	{
		TPFSetTileBit(tile, 14);
		FindLengthOfTunnelResult flotr = FindLengthOfTunnel(tile, direction);
		rd.cur_length += flotr.length;
		TPFSetTileBit(flotr.tile, 14);
		return flotr.tile;
	}





}
