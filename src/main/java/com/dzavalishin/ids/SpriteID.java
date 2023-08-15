package com.dzavalishin.ids;

import java.util.HashMap;
import java.util.Map;

public class SpriteID extends AbstractID 
{
	private static final long serialVersionUID = 1L;
	
	private SpriteID(int i) {
		super(i);
	}
	
	private static final Map<Integer,SpriteID> ids = new HashMap<>();
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

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
}
