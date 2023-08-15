package com.dzavalishin.ids;

import java.util.HashMap;
import java.util.Map;

public class CursorID extends AbstractID 
{
	private static final long serialVersionUID = 1L;
	
	private CursorID(int i) {
		super(i);
	}
	
	private static final Map<Integer,CursorID> ids = new HashMap<>();
	public static CursorID get(int player) 
	{
		CursorID old = ids.get(player);
		if( old == null ) 
		{
			old = new CursorID(player);
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
