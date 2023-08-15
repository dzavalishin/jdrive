package com.dzavalishin.util;

import java.util.HashMap;

import com.dzavalishin.game.TileIndex;

public class Hash {
	//private HashMap<HashKey,PathNode> map;
	private final HashMap<HashKey,Object> map = new HashMap<>();

	public void Hash_Set(TileIndex tile, int direction, Object content) {
		HashKey key = new HashKey( tile, direction);
		map.put(key, content);
	}

	public Object Hash_Get(TileIndex tile, int direction) {
		HashKey key = new HashKey( tile, direction);
		return map.get(key);
	}

	public void clear_Hash(boolean b) {
		map.clear();		
	}

	public void Hash_Delete(TileIndex tile, int direction) {
		HashKey key = new HashKey( tile, direction);
		map.remove(key);
	}

	public int Hash_Size() {
		return map.size();
	}

	
}


class HashKey implements Comparable<HashKey>
{
	final TileIndex tile;
	final int direction;
	
	public HashKey(TileIndex tile, int direction) {
		this.tile = tile;
		this.direction = direction;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HashKey) {
			HashKey k = (HashKey) obj;
			
			return tile.equals(k.tile) && direction == k.direction; 
					
		}
		return super.equals(obj);
	}

	@Override
	public int compareTo(HashKey k) {
		int i = tile.getTile() - k.tile.getTile();
		if( i == 0 )
			i = direction - k.direction;
		
		return i;
	}
	

	@Override
	public int hashCode() {		
		return (tile.getTile() << 2) + direction;
	}
	
	
}