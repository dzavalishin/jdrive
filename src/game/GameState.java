package game;

import game.ids.PlayerID;

/**
 * 
 * Must contain all the data that describe game state.
 * Saving/loading this object is saving/loading game.
 * 
 * @author dz
 *
 */

public class GameState 
{

	// Player
	public Player[] _players = new Player[Global.MAX_PLAYERS];
	public int [] _player_colors = new int[Global.MAX_PLAYERS];
	
	public PlayerID _current_player;
	public PlayerID _local_player;
	public boolean _is_old_ai_player = false;
	int _yearly_expenses_type; // TODO fixme, use parameter where possible
	int _cur_player_tick_index;
	int _next_competitor_start;
	
	
}
