package com.dzavalishin.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * 
 * Can store vector of items per key.
 * 
 * @author dz
 *
 * @param <ItemType> Item to store.
 */

public class TileHash<ItemType>
{
	final Map<Integer, List<ItemType> > map = new HashMap<>();
	

	public void add(TileIndex tileIndex, ItemType item)
	{
		add(tileIndex.getTile(), item);
	}
	
	public void add(int tileIndex, ItemType item)
	{
		List<ItemType> mi = map.get(tileIndex);
		if( mi == null )
		{
			mi = new ArrayList<>();
			mi.add(item);
			map.put(tileIndex, mi);
		}
		else
			mi.add(item);			
	}

	
	
	public void remove(TileIndex tileIndex, ItemType item)
	{
		remove(tileIndex.getTile(), item);
	}
	
	public void remove(int tileIndex, ItemType item)
	{
		List<ItemType> mi = map.get(tileIndex);
		if( mi == null )
		{
			Global.error("TileHash attempt to remove %s (%s)", item.toString(), item.getClass().getName());
		}
		else
			mi.remove(item);			
	}

	public List<ItemType> get(TileIndex tileIndex)
	{
		return get(tileIndex.getTile());
	}
	
	
	public List<ItemType> get(int tileIndex)
	{
		return map.get(tileIndex);
	}
	


	
	public List<ItemType> clear(TileIndex tileIndex)
	{
		return clear(tileIndex.getTile());
	}
	
	public List<ItemType> clear(int tileIndex)
	{
		return map.remove(tileIndex);
	}
	
	
	
}
