package com.dzavalishin.ids;

import java.util.HashMap;
import java.util.Map;

import com.dzavalishin.game.Vehicle;

public class VehicleID extends AbstractID 
{
	private static final long serialVersionUID = 1L;
	
	private VehicleID(int i) {
		super(i);
	}


	private static final Map<Integer,VehicleID> ids = new HashMap<>();
	public static VehicleID get(int player) 
	{
		VehicleID old = ids.get(player);
		if( old == null ) 
		{
			old = new VehicleID(player);
			ids.put(player, old);
		}
		return old;
	}

	public static VehicleID getInvalid()
	{
		return get(-1);
	}

	public boolean IsVehicleIndex() {
		return Vehicle.IsVehicleIndex(this.id);
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
