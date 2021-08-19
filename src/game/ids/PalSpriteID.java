package game.ids;

import java.util.HashMap;
import java.util.Map;

public class PalSpriteID extends AbstractID {
	private PalSpriteID(int i) {
		super(i);
	}
	
	private static Map<Integer,PalSpriteID> ids = new HashMap<Integer,PalSpriteID>();
	public static PalSpriteID get(int player) 
	{
		PalSpriteID old = ids.get(player);
		if( old == null ) 
		{
			old = new PalSpriteID(player);
			ids.put(player, old);
		}
		return old;
	}

}
