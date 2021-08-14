package game.struct;

import java.util.ArrayList;
import java.util.List;

import game.Order;
import game.ids.OrderID;
import game.ids.VehicleID;

//#define MAX_BACKUP_ORDER_COUNT 40


public class BackuppedOrders 
{
	public VehicleID clone;
	public OrderID orderindex;
	
	//Order order[MAX_BACKUP_ORDER_COUNT + 1];
	public List<Order> order = new ArrayList<Order>();
	
	public int service_interval;
	public String name;

}
