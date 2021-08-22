package game.ids;

import java.util.HashMap;
import java.util.Map;

public class OrderID extends AbstractID {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OrderID(int i) {
		super(i);
	}
	
	private static final Map<Integer,OrderID> ids = new HashMap<Integer,OrderID>();
	public static OrderID get(int player) 
	{
		OrderID old = ids.get(player);
		if( old == null ) 
		{
			old = new OrderID(player);
			ids.put(player, old);
		}
		return old;
	}
	
}
