package game.ids;

import java.util.HashMap;
import java.util.Map;

import game.Engine;

public class EngineID extends AbstractID {

	private EngineID(int i) {
		super(i);
	}

	/*
	public EngineID() {
		id = -1;
	}*/

	private static Map<Integer,EngineID> ids = new HashMap<Integer,EngineID>();
	public static EngineID get(int player) 
	{
		EngineID old = ids.get(player);
		if( old == null ) 
		{
			old = new EngineID(player);
			ids.put(player, old);
		}
		return old;
	}

	public static EngineID getInvalid() {
		return get(Engine.INVALID_ENGINE);
	}
	
	
}
