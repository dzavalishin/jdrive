package com.dzavalishin.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dzavalishin.game.Global;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.struct.Point;

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
	final Map<Integer, Set<VehicleID>> map = new HashMap<>();

	public List<VehicleID> get(int x1, int y1, int x2, int y2) {
		x1 >>= DELETE_BITS; // down
		x2 = (x2 >> DELETE_BITS) + 1; // up

		y1 >>= DELETE_BITS;
		y2 = (y2 >> DELETE_BITS) + 1;

		ArrayList<VehicleID> list = new ArrayList<>();  

		for(int x = x1; x <= x2; x++ )
		{
			for(int y = y1; y <= y2; y++ )
			{
				Set<VehicleID> vehicles = map.get(hashFunc(x, y));
				if (vehicles != null) {
					list.addAll(vehicles);
				}
			}
		}

		return list;
	}

	public void update(Point prev, Point tobe, Vehicle vehicle)
	{
		int hash1 = hashFunc(prev);
		int hash2 = hashFunc(tobe);

		VehicleID vid = VehicleID.get(vehicle.index);
		Set<VehicleID> removeFrom = map.get(hash1);
		if (removeFrom != null) {
			removeFrom.remove(vid);
			if (removeFrom.isEmpty()) {
				map.remove(hash1);
			}
		}

		Set<VehicleID> addTo = map.computeIfAbsent(hash2, (x) -> new HashSet<>());
		addTo.add(vid);
	}

	public void clear() {
		map.clear();		
	}

	public void remove(Point point, Vehicle vehicle) {
		int hash1 = hashFunc(point);
		VehicleID vid = VehicleID.get(vehicle.index);
		boolean removed = false;
		Set<VehicleID> removeFrom = map.get(hash1);
		if (removeFrom != null) {
			removed = removeFrom.remove(vid);
			if (removeFrom.isEmpty()) {
				map.remove(hash1);
			}
		}
		
		if (!removed) //|| old.id != vehicle.index )
		{
			Global.error("can't remove vehicle from position hash");

			Iterator<Entry<Integer, Set<VehicleID>>> iterator = map.entrySet().iterator();
			while (iterator.hasNext())
			{
				Set<VehicleID> set = iterator.next().getValue();
				if (set.remove(vid)) {
					if (set.isEmpty()) {
						iterator.remove();
					}
					Global.error("removed iterating");
					return;
				}
			}
			Global.error("unable to remove iterating");
		}
		
		// TODO debug me
		//assert old.id == vehicle.index;

	}

}
