package game;

import java.util.HashMap;
import java.util.Map;

public class PlayerID extends AbstractID {


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

	private static Map<Integer,PlayerID> ids = new HashMap<Integer,PlayerID>();
	public static PlayerID get(int player) {

		PlayerID old = ids.get(player);
		if( old == null ) 
		{
			old = new PlayerID(player);
			ids.put(player, old);
		}
		return old;
	}


	public boolean IS_HUMAN_PLAYER()
	{
		return 0 == Player.GetPlayer(this).is_ai;
	}

	public boolean IS_INTERACTIVE_PLAYER()
	{
		return equals( Global._local_player );
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlayerID) {
			PlayerID him = (PlayerID) obj;
			return him.id == id;
		}
		return false;
	}

}
