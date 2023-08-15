package com.dzavalishin.game;

public class TileInfo {
	public int x;
	public int y;
	public int tileh;
	public int type;
	public int map5;
	public TileIndex tile;
	public int z;

	
	static final public int TILE_SIZE   = 16;   // Tiles are 16x16 "units" in size 
	static final public int TILE_PIXELS = 32;   // a tile is 32x32 pixels 
	static final public int TILE_HEIGHT = 8;    // The standard height-difference between tiles on two levels is 8 (z-diff 8) 
	
	public static final int EXTRABITS_DESERT = 1;
	
}
