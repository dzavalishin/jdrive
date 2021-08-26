package game.util;

import game.Global;
import game.ids.PlayerID;

/**
 * 
 * @author dz
 *
 * try (PushPlayer pp = new PushPlayer( v.owner )) {
 *     // code
 * }
 * 
 * // Will return to prevoius player here or if exception is thrown
 *
 */

public class PushPlayer implements AutoCloseable 
{

	private PlayerID old_player;

	public PushPlayer(PlayerID player) {
		old_player = Global.gs._current_player;
		Global.gs._current_player = player;
	}
	
	@Override
	public void close() //throws Exception 
	{
		Global.gs._current_player = old_player;
	}

}
