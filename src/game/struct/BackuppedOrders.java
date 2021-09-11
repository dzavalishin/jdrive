package game.struct;

import java.util.ArrayList;
import java.util.List;

import game.Order;
import game.ids.VehicleID;

public class BackuppedOrders 
{
	public VehicleID clone;
	public int currentOrderIndex;
	
	public final List<Order> order = new ArrayList<>();
	
	public int service_interval;
	public String name;

}
