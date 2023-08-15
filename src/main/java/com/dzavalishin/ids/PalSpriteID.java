package com.dzavalishin.ids;

import java.util.HashMap;
import java.util.Map;

public class PalSpriteID extends AbstractID 
{
	private static final long serialVersionUID = 1L;
	
	private PalSpriteID(int i) {
		super(i);
	}
	
	private static final Map<Integer,PalSpriteID> ids = new HashMap<>();
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

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
