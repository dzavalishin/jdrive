package game;

import java.util.ArrayList;
import java.util.List;

//#define MAX_BACKUP_ORDER_COUNT 40


public class BackuppedOrders 
{
	VehicleID clone;
	OrderID orderindex;
	
	//Order order[MAX_BACKUP_ORDER_COUNT + 1];
	List<Order> order = new ArrayList<Order>();
	
	int service_interval;
	String name;

}
