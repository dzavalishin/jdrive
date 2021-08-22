package game.ids;

import java.util.HashMap;
import java.util.Map;

import game.Vehicle;

public class VehicleID extends AbstractID {
	/*private VehicleID() {
		id = -1;
	}*/

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private VehicleID(int i) {
		super(i);
	}


	private static Map<Integer,VehicleID> ids = new HashMap<Integer,VehicleID>();
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
	
	
}
