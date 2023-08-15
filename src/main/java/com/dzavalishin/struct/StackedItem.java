package com.dzavalishin.struct;

import com.dzavalishin.game.TileIndex;

public class StackedItem {
	public TileIndex tile;
	public int cur_length; // This is the current length to this tile.
	public int priority; // This is the current length + estimated length to the goal.
	public int track;
	public int depth;
	public int state;
	public int first_track;
}
