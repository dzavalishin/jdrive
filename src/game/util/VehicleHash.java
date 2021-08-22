package game.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Vehicle;
import game.ids.VehicleID;
import game.struct.Point;

/**
 * 
 * TODO spatial hash to find vehicles intersecting some part of map.
 *
 * General idea is to reduce coordinates by / 8 and check all possible
 * squares of 256*256 size.
 * 
 * @author dz
 *
 */

public class VehicleHash 
{
	/*
	//private final Map<Point,Integer> map = new HashMap<>(0x1000);
	private final Map<Integer,Integer> map = new HashMap<>(0x1000);


	public void put( /*Point p, Vehicle v, int hash)
	{
		/*
		int old_x = v.left_coord;
		int old_y = v.top_coord;

		map.remove(new Point(old_x,old_y)); * /		
		map.put(hash, v.index );
	}

	
	public void put( int x, int y, Vehicle v, int hash)
	{
		put(new Point(x,y), v, hash);
	}


	public void put(Vehicle v)
	{

	}

	
	public VehicleID get(int x, int y)
	{
		return get(new Point(x,y));				
	}

	public VehicleID get(Point point) {
		Integer veh = map.get(point);
		if(veh == null) return null;
		return VehicleID.get( veh );
	}
	
	public void clear()
	{
		map.clear();
	}

	public void remove(Vehicle v) {
		Integer [] k = { null };
		
		map.forEach( (kk,vv) ->
		{
			if(vv == v.index) k[0] = kk; // TODO SLOOOW loop for all!
		});
		
		if(k[0] != null)
			map.remove(k[0]);
		
	}

	public VehicleID get(int i) {
		Integer veh = map.get(i);
		if(veh == null) return null;
		return VehicleID.get( veh );
	}

	public void remove(int old_hash) {
		map.remove(old_hash);		
	}
	*/
	
	static int hashFunc(int x, int y )
	{
		return (x << 16) + (y & 0xFFFF);
	}
	
	private int hashFunc(Point prev) {
		return hashFunc(prev.x >> 8, prev.y >> 8);
	}
	
	//ArrayList<VehicleID> list = new ArrayList<VehicleID>();
	final Map<Integer,VehicleID> map = new HashMap<Integer,VehicleID>();
		
	public List<VehicleID> get(int x1, int y1, int x2, int y2) {
		x1 >>= 8; // down
		x2 = (x2 >> 8) + 1; // up

		y1 >>= 8;
		y2 = (y2 >> 8) + 1;

		ArrayList<VehicleID> list = new ArrayList<VehicleID>();  
		
		for(int x = x1; x <= x2; x++ )
		{
			for(int y = y1; y <= y2; y++ )
			{
				VehicleID item = map.get(hashFunc(x, y));
				if(item != null)
					list.add(item);
			}
		}
		
		return list;
	}
	
	public void update(Point prev, Point tobe, Vehicle vehicle) 
	{
		int hash1 = hashFunc(prev);
		int hash2 = hashFunc(tobe);
		
		//if( hash1 == hash2 ) return;
		
		map.remove(Integer.valueOf(hash1));
		map.put(Integer.valueOf(hash2), VehicleID.get(vehicle.index));
		
	}

	public void clear() {
		map.clear();		
	}
}
