package game.struct;

import game.TileIndex;

public class StackedItem {
	TileIndex tile;
	int cur_length; // This is the current length to this tile.
	int priority; // This is the current length + estimated length to the goal.
	int track;
	int depth;
	int state;
	int first_track;

}
