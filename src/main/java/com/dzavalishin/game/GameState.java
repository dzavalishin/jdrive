package com.dzavalishin.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.util.GameDate;
import com.dzavalishin.util.MemoryPool;
import com.dzavalishin.util.VehicleHash;
import com.dzavalishin.xui.Window;

/**
 * 
 * Must contain all the data that describe game state.
 * Saving/loading this object is saving/loading game.
 * 
 * @author dz
 *
 */

public class GameState implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	// -------------------------------------------------------------------
	// Player
	// -------------------------------------------------------------------

	//public PlayerID _current_player = PlayerID.getNone();
	private PlayerID _current_player = PlayerID.getNone();
	public PlayerID _local_player = PlayerID.getNone();
	public final boolean _is_old_ai_player = false;

	public PlayerID getCurrentPlayer() { return _current_player; }
	public void setCurrentPlayer(PlayerID p) { _current_player = p; }
	
	// -------------------------------------------------------------------
	// Big and structured stuff
	// -------------------------------------------------------------------

	/** Game map */
	public Tile _m[];
	
	public final GameDate date = new GameDate();
	public final Economy _economy = new Economy();

	
	// -------------------------------------------------------------------
	// Variables
	// -------------------------------------------------------------------

	public int _map_log_x = 8; //6;
	public int _map_size_x = 256;
	public int _map_size_y = 256;
	public int _map_tile_mask;
	public int _map_size;	
	
	int _yearly_expenses_type; // TODO fixme, use parameter where possible
	int _cur_player_tick_index;
	int _next_competitor_start;
	
	// Window?
	public int _saved_scrollpos_x;
	public int _saved_scrollpos_y;
	public int _saved_scrollpos_zoom;




	// -------------------------------------------------------------------
	// Game object containers
	// -------------------------------------------------------------------

	public final Player[] _players = new Player[Global.MAX_PLAYERS];
	public final int [] _player_colors = new int[Global.MAX_PLAYERS];


	//final TileIndex _animated_tile_list[] = new TileIndex[256];
	final List<TileIndex> _animated_tile_list = new ArrayList<>();


	final MemoryPool<SignStruct> _signs = new MemoryPool<>(SignStruct.factory);
	
	/* Initialize the industry-pool */
	final MemoryPool<Industry> _industries = new MemoryPool<>(Industry.factory);
	//static MemoryPool<Town> _town_pool = new MemoryPool<Town>(Town::new);
	final MemoryPool<Town> _towns = new MemoryPool<>(Town.factory);
	public final Engine [] _engines = new Engine[Global.TOTAL_NUM_ENGINES];
	final MemoryPool<Depot> _depots = new MemoryPool<>(Depot.factory);

	final MemoryPool<Vehicle> _vehicles = new MemoryPool<>(Vehicle.factory);
	final VehicleHash _vehicle_hash = new VehicleHash();

	final MemoryPool<WayPoint> _waypoints = new MemoryPool<>(WayPoint.factory);


	final MemoryPool<Station> _stations = new MemoryPool<>(Station.factory);
	final MemoryPool<RoadStop> _roadstops = new MemoryPool<>(RoadStop.factory);




	public final List<Window> _windows = new ArrayList<>();




	
}
