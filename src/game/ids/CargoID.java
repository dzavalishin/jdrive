package game.ids;

import java.util.HashMap;
import java.util.Map;

public class CargoID extends AbstractID {
	private CargoID(int i) {
		super(i);
	}
	
	private static Map<Integer,CargoID> ids = new HashMap<Integer,CargoID>();
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
	
	
	
}
