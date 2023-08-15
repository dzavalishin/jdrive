package com.dzavalishin.util;

import com.dzavalishin.ids.PlayerID;

/**
 * 
 * @author dz
 *
 * try (PushPlayer pp = new PushPlayer( v.owner )) {
 *     // code
 * }
 * 
 * // Will return to previous player here or if exception is thrown
 *
 */

public class PushPlayer implements AutoCloseable 
{

	private PlayerID old_player;

	public PushPlayer(PlayerID player) {
		old_player = PlayerID.getCurrent();
		PlayerID.setCurrent( player );
	}
	
	@Override
	public void close() //throws Exception 
	{
		PlayerID.setCurrent(old_player);
	}

}
