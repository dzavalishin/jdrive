package com.dzavalishin.aystar;

import java.util.Arrays;

import com.dzavalishin.game.TileIndex;

public class AyStarNode 
{
	public TileIndex tile;
	public int direction;
	public final int[] user_data;

	
	public AyStarNode() {
		user_data = new int[2];
	}

	public AyStarNode(AyStarNode node) {
		tile = node.tile;
		direction = node.direction;
		user_data = new int[2];
		System.arraycopy(node.user_data, 0, user_data, 0, user_data.length);						
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof AyStarNode) {
			AyStarNode him = (AyStarNode) obj;

			if( direction != him.direction ) return false;
			if( !tile.equals(him.tile) ) return false;
			return Arrays.equals(user_data, him.user_data);
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return (tile.getTile() << 2) + direction;
	}
	
}
