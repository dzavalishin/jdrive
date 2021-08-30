package game.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import game.Global;
import game.Vehicle;
import game.ids.VehicleID;
import game.struct.Point;

/**
 * 
 * General idea is to reduce coordinates by / 8 and check all possible
 * squares of 256*256 size.
 * 
 * @author dz
 *
 */

public class VehicleHash implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	//private static final int DELETE_BITS = 8;
	private static final int DELETE_BITS = 3;

	static int hashFunc(int x, int y )
	{
		return (x << 16) + (y & 0xFFFF);
	}

	private int hashFunc(Point prev) {
		return hashFunc(prev.x >> DELETE_BITS, prev.y >> DELETE_BITS);
	}

	//ArrayList<VehicleID> list = new ArrayList<VehicleID>();
	final Map<Integer,VehicleID> map = new HashMap<Integer,VehicleID>();

	public List<VehicleID> get(int x1, int y1, int x2, int y2) {
		x1 >>= DELETE_BITS; // down
		x2 = (x2 >> DELETE_BITS) + 1; // up

		y1 >>= DELETE_BITS;
		y2 = (y2 >> DELETE_BITS) + 1;

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

	public void remove(Point point, Vehicle vehicle) {
		int hash1 = hashFunc(point);
		VehicleID old = map.remove(Integer.valueOf(hash1));

		if( old == null ) //|| old.id != vehicle.index )
		{
			Global.error("can't remove vehicle from position hash");
			Iterator<Entry<Integer, VehicleID>> iterator = map.entrySet().iterator();

			while (iterator.hasNext()) 
			{
				Entry<Integer, VehicleID> entry = iterator.next();
				if (vehicle.index == entry.getValue().id ) 
				{
					iterator.remove();
					Global.error("removed iterating");
					return;
				}
			}			
			Global.error("unable to remove iterating");
			return;
		}
		
		// TODO return after aircraft debug
		//assert( old.id == vehicle.index );

	}

}
