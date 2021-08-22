package game.ids;

import java.util.HashMap;
import java.util.Map;

public class UnitID extends AbstractID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UnitID(int i) {
		super(i);
	}
	
	private static Map<Integer,UnitID> ids = new HashMap<Integer,UnitID>();
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

}
