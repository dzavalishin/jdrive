package game.util;

import java.util.HashMap;
import java.util.Map;

import game.Point;
import game.Vehicle;
import game.VehicleID;

public class VehicleHash 
{

	private final Map<Point,Integer> map = new HashMap<>(0x1000);


	public void put( Point p, Vehicle v)
	{
		int old_x = v.left_coord;
		int old_y = v.top_coord;

		map.remove(new Point(old_x,old_y));		
		map.put(p, v.index );
	}

	public void put( int x, int y, Vehicle v)
	{
		put(new Point(x,y), v);
	}

	/*
	public void put(Vehicle v)
	{

	}*/


	public VehicleID get(int x, int y)
	{
		return get(new Point(x,y));				
	}

	public VehicleID get(Point point) {
		return VehicleID.get( map.get(point) );
	}
	
	public void clear()
	{
		map.clear();
	}

	public void remove(Vehicle v) {
		Point [] k = { null };
		
		map.forEach( (kk,vv) ->
		{
			if(vv == v.index) k[0] = kk; // TODO SLOOOW loop for all!
		});
		
		if(k[0] != null)
			map.remove(k[0]);
		
	}
}
