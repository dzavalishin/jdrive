package game;

import java.util.HashMap;
import java.util.Map;

public class PlayerID extends AbstractID {

	private static Map<Integer,PlayerID> ids = new HashMap<Integer,PlayerID>();

	private PlayerID(int i) {
		super(i);
	}

	/*public PlayerID(Owner o) {
		id = o.owner;
	}*/

	public Player GetPlayer() {
		return Player.GetPlayer(id);
		//return null;
	}

	public static PlayerID get(Owner o) {
		return get(o.owner);
	}
	
	public static PlayerID get(int player) {

		PlayerID old = ids.get(player);
		if( old == null ) 
		{
			old = new PlayerID(player);
			ids.put(player, old);
		}
		return old;
	}

}
