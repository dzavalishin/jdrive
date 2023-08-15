package com.dzavalishin.game;

import com.dzavalishin.util.BitOps;

public class Map {

	

	public static final TileIndex INVALID_TILE = TileIndex.getInvalid(); //new TileIndex(-1);



	/* Approximation of the length of a straight track, relative to a diagonal
	 * track (ie the size of a tile side). #defined instead of final so it can
	 * stay integer. (no runtime float operations) Is this needed?
	 * Watch out! There are _no_ brackets around here, to prevent intermediate
	 * rounding! Be careful when using this!
	 * This value should be sqrt(2)/2 ~ 0.7071 */
	public static final float STRAIGHT_TRACK_LENGTH = 0.7071f;

	
	

	// TODO move _m and vars below here?
	/*
	int _map_log_x;
	int _map_size_x;
	int _map_size_y;
	int _map_tile_mask;
	int _map_size;

	Tile* _m = null;
	*/

	static void AllocateMap(int size_x, int size_y)
	{
		// Make sure that the map size is within the limits and that
		// the x axis size is a power of 2.
		if (size_x < 64 || size_x > 2048 ||
				size_y < 64 || size_y > 2048 ||
				(size_x&(size_x-1)) != 0 ||
				(size_y&(size_y-1)) != 0)
			Global.error("Invalid map size");

		Global.DEBUG_map( 1, "Allocating map of size %dx%d", size_x, size_y);

		Global.gs._map_log_x = BitOps.FindFirstBit(size_x);
		Global.gs._map_size_x = size_x;
		Global.gs._map_size_y = size_y;
		Global.gs._map_size = size_x * size_y;
		Global.gs._map_tile_mask = Global.gs._map_size - 1;

		Global.gs._m = new Tile[Global.gs._map_size];

		//if (Global._m == null) Global.error("Failed to allocate memory for the map");
	}


	static int ScaleByMapSize(int n)
	{
		// First shift by 12 to prevent integer overflow for large values of n.
		// >>12 is safe since the min mapsize is 64x64
		// Add (1<<4)-1 to round upwards.
		return (n * (Global.MapSize() >> 12) + (1<<4) - 1) >> 4;
	}


	// Scale relative to the circumference of the map
	static int ScaleByMapSize1D(int n)
	{
		// Normal circumference for the X+Y is 256+256 = 1<<9
		// Note, not actually taking the full circumference into account,
		// just half of it.
		// (1<<9) - 1 is there to scale upwards.
		return (n * (Global.MapSizeX() + Global.MapSizeY()) + (1<<9) - 1) >> 9;
	}



	public static int DistanceManhattan(TileIndex t0, TileIndex t1)
	{
		final int dx = Math.abs(t0.TileX() - t1.TileX());
		final int dy = Math.abs(t0.TileY() - t1.TileY());
		return dx + dy;
	}


	static int DistanceSquare(TileIndex t0, TileIndex t1)
	{
		final int dx = t0.TileX() - t1.TileX();
		final int dy = t0.TileY() - t1.TileY();
		return dx * dx + dy * dy;
	}


	static int DistanceMax(TileIndex t0, TileIndex t1)
	{
		final int dx = Math.abs(t0.TileX() - t1.TileX());
		final int dy = Math.abs(t0.TileY() - t1.TileY());
		return Math.max(dx, dy);
	}


	static int DistanceMaxPlusManhattan(TileIndex t0, TileIndex t1)
	{
		final int dx = Math.abs(t0.TileX() - t1.TileX());
		final int dy = Math.abs(t0.TileY() - t1.TileY());
		return dx > dy ? 2 * dx + dy : 2 * dy + dx;
	}

	static int DistanceFromEdge(TileIndex tile)
	{
		final int xl = tile.TileX();
		final int yl = tile.TileY();
		final int xh = Global.MapSizeX() - 1 - xl;
		final int yh = Global.MapSizeY() - 1 - yl;
		final int minl = Math.min(xl, yl);
		final int minh = Math.min(xh, yh);
		return Math.min(minl, minh);
	}
	
}
