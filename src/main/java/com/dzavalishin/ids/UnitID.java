package com.dzavalishin.ids;

import java.util.HashMap;
import java.util.Map;

public class UnitID extends AbstractID 
{
	private static final long serialVersionUID = 1L;
	
	private UnitID(int i) {
		super(i);
	}
	
	private static final Map<Integer,UnitID> ids = new HashMap<>();

	public static UnitID get(int player) 
	{
		UnitID old = ids.get(player);
		if( old == null ) 
		{
			old = new UnitID(player);
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
