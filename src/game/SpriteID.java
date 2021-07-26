package game;

import java.util.HashMap;
import java.util.Map;

public class SpriteID extends AbstractID {
	private SpriteID(int i) {
		super(i);
	}
	
	private static Map<Integer,SpriteID> ids = new HashMap<Integer,SpriteID>();
	public static SpriteID get(int player) 
	{
		SpriteID old = ids.get(player);
		if( old == null ) 
		{
			old = new SpriteID(player);
			ids.put(player, old);
		}
		return old;
	}

}
