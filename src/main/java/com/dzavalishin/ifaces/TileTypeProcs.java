package com.dzavalishin.ifaces;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;

import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.TileInfo;
import com.dzavalishin.struct.ProducedCargo;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.enums.TransportType;

public class TileTypeProcs implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TileTypeProcs(
			Consumer<TileInfo> draw_tile,
			Function<TileInfo,Integer> get_slope_z,
			ToIntBiFunction<TileIndex, Byte> clear_tile,
			Function<TileIndex, AcceptedCargo> get_accepted_cargo,
			Function<TileIndex, TileDesc> get_tile_desc,
			ToIntBiFunction<TileIndex,TransportType> get_tile_track_status, 
			Consumer<TileIndex> click_tile,
			Consumer<TileIndex> animate_tile,
			Consumer<TileIndex> tile_loop,
			ChangeOwnerInterface change_tile_owner,
			Function<TileIndex, ProducedCargo> get_produced_cargo,
			TileVehicleInterface vehicle_enter_tile,
			TileVehicleInterface vehicle_leave_tile,
			Function<TileInfo,Integer> get_slope_tileh
			) 
	{
		draw_tile_proc = draw_tile;
		get_slope_z_proc = get_slope_z;
		clear_tile_proc = clear_tile;
		get_accepted_cargo_proc = get_accepted_cargo;
		get_tile_desc_proc = get_tile_desc;
		get_tile_track_status_proc = get_tile_track_status;
		click_tile_proc = click_tile;
		animate_tile_proc = animate_tile;
		tile_loop_proc = tile_loop;
		change_tile_owner_proc = change_tile_owner;
		get_produced_cargo_proc = get_produced_cargo;
		vehicle_enter_tile_proc = vehicle_enter_tile;
		vehicle_leave_tile_proc = vehicle_leave_tile;
		get_slope_tileh_proc = get_slope_tileh;
	}

	//abstract void draw_tile_proc(TileInfo ti);
	public final Consumer<TileInfo> draw_tile_proc;
	
	//abstract int get_slope_z_proc(TileInfo ti);
	//Consumer<TileInfo> get_slope_z_proc;
	public final Function<TileInfo,Integer> get_slope_z_proc;
	
	//abstract int clear_tile_proc(TileIndex tile, byte flags);
	//BiConsumer<TileIndex, Byte> clear_tile_proc;
	public final ToIntBiFunction<TileIndex, Byte> clear_tile_proc;
	
	//abstract AcceptedCargo get_accepted_cargo_proc(TileIndex tile);
	public final Function<TileIndex,AcceptedCargo> get_accepted_cargo_proc;
	
	//abstract TileDesc get_tile_desc_proc(TileIndex tile);
	public final Function<TileIndex,TileDesc> get_tile_desc_proc;

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
	
	
	//abstract int get_tile_track_status_proc(TileIndex tile, TransportType mode);
	//ToIntBiFunction<TileIndex,TransportType> get_tile_track_status_proc; 
	public final ToIntBiFunction<TileIndex,TransportType> get_tile_track_status_proc;
	
	//abstract void click_tile_proc(TileIndex tile);
	public final Consumer<TileIndex> click_tile_proc;
	
	//abstract void animate_tile_proc(TileIndex tile);
	public final Consumer<TileIndex> animate_tile_proc;
	
	//abstract void tile_loop_proc(TileIndex tile);
	public final Consumer<TileIndex> tile_loop_proc;
	
	//abstract void change_tile_owner_proc(TileIndex tile, PlayerID old_player, PlayerID new_player);
	public final ChangeOwnerInterface change_tile_owner_proc;
	
	//abstract byte[] get_produced_cargo_proc(TileIndex tile);
	public final Function<TileIndex,ProducedCargo> get_produced_cargo_proc;

	/**
	 *  Return value has bit 0x2 set, when the vehicle enters a station. Then,
	 * result << 8 contains the id of the station entered. If the return value has
	 * bit 0x8 set, the vehicle could not and did not enter the tile. Are there
	 * other bits that can be set? */
	
	//abstract int vehicle_enter_tile_proc(Vehicle v, TileIndex tile, int x, int y);
	//abstract void vehicle_leave_tile_proc(Vehicle v, TileIndex tile, int x, int y);
	
	public final TileVehicleInterface vehicle_enter_tile_proc;
	public final TileVehicleInterface vehicle_leave_tile_proc;
	
	//abstract int get_slope_tileh_proc(final TileInfo ti);
	public final Function<TileInfo,Integer> get_slope_tileh_proc;

}

    