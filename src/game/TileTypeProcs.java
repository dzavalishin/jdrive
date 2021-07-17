package game;

public abstract class TileTypeProcs {
	abstract void draw_tile_proc(TileInfo ti);
	abstract int get_slope_z_proc(TileInfo ti);
	abstract int clear_tile_proc(TileIndex tile, byte flags);
	abstract AcceptedCargo get_accepted_cargo_proc(TileIndex tile);
	abstract TileDesc get_tile_desc_proc(TileIndex tile);

	/**
	 * GetTileTrackStatusProcs return a value that contains the possible tracks
	 * that can be taken on a given tile by a given transport. The return value is
	 * composed as follows: 0xaabbccdd. ccdd and aabb are bitmasks of trackdirs,
	 * where bit n corresponds to trackdir n. ccdd are the trackdirs that are
	 * present in the tile (1==present, 0==not present), aabb is the signal
	 * status, if applicable (0==green/no signal, 1==red, note that this is
	 * reversed from map3/2[tile] for railway signals).
	 *
	 * The result (let's call it ts) is often used as follows:
	 * tracks = (byte)(ts | ts >>8)
	 * This effectively converts the present part of the result (ccdd) to a
	 * track bitmask, which disregards directions. Normally, this is the same as just
	 * doing (byte)ts I think, although I am not really sure
	 *
	 * A trackdir is combination of a track and a dir, where the lower three bits
	 * are a track, the fourth bit is the direction. these give 12 (or 14)
	 * possible options: 0-5 and 8-13, so we need 14 bits for a trackdir bitmask
	 * above.
	 */
	
	
	abstract int get_tile_track_status_proc(TileIndex tile, TransportType mode);

	
	abstract void click_tile_proc(TileIndex tile);
	abstract void animate_tile_proc(TileIndex tile);
	abstract void tile_loop_proc(TileIndex tile);
	abstract void change_tile_owner_proc(TileIndex tile, PlayerID old_player, PlayerID new_player);
	
	abstract byte[] get_produced_cargo_proc(TileIndex tile);

	/**
	 *  Return value has bit 0x2 set, when the vehicle enters a station. Then,
	 * result << 8 contains the id of the station entered. If the return value has
	 * bit 0x8 set, the vehicle could not and did not enter the tile. Are there
	 * other bits that can be set? */
	
	abstract int vehicle_enter_tile_proc(Vehicle v, TileIndex tile, int x, int y);
	abstract void vehicle_leave_tile_proc(Vehicle v, TileIndex tile, int x, int y);
	
	abstract int get_slope_tileh_proc(final TileInfo ti);

}
