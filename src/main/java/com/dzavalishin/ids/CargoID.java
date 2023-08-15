package com.dzavalishin.ids;

import java.util.HashMap;
import java.util.Map;

public class CargoID extends AbstractID {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CargoID(int i) {
		super(i);
	}
	
	private static final Map<Integer,CargoID> ids = new HashMap<>();
	public static CargoID get(int player) 
	{
		CargoID old = ids.get(player);
		if( old == null ) 
		{
			old = new CargoID(player);
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
