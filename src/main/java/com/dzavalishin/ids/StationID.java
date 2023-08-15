package com.dzavalishin.ids;

import java.util.HashMap;
import java.util.Map;

public class StationID extends AbstractID 
{
	private static final long serialVersionUID = 1L;
	
	private StationID(int i) {
		super(i);
	}
	
	private static final Map<Integer,StationID> ids = new HashMap<>();
	public static StationID get(int player) 
	{
		StationID old = ids.get(player);
		if( old == null ) 
		{
			old = new StationID(player);
			ids.put(player, old);
		}
		return old;
	}

	public static StationID getInvalid() {
		return get(-1);
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
