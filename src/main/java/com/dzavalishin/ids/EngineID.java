package com.dzavalishin.ids;

import java.util.HashMap;
import java.util.Map;

import com.dzavalishin.game.Engine;

public class EngineID extends AbstractID 
{
	private static final long serialVersionUID = 1L;
	
	private EngineID(int i) {
		super(i);
	}


	private static final Map<Integer,EngineID> ids = new HashMap<>();
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
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
}
