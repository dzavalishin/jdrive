package game;

import java.util.HashMap;

public class Hash {
	//private HashMap<HashKey,PathNode> map;
	private HashMap<HashKey,Object> map;

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


class HashKey
{
	TileIndex tile;
	int direction;
	
	public HashKey(TileIndex tile, int direction) {
		this.tile = tile;
		this.direction = direction;
	}
}