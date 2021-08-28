package game;

import game.util.BitOps;

public class Map {

	

	//void AllocateMap(int size_x, int size_y);


	// Scale a number relative to the map size
	//int ScaleByMapSize(int); // Scale relative to the number of tiles
	//int ScaleByMapSize1D(int); // Scale relative to the circumference of the map


	/*
	static  TileIndexDiff TileDiffXY(int x, int y)
	{
		// Multiplication gives much better optimization on MSVC than shifting.
		// 0 << shift isn't optimized to 0 properly.
		// Typically x and y are constants, and then this doesn't result
		// in any actual multiplication in the assembly code..
		return (y * MapSizeX()) + x;
	}* /

	static  TileIndex TileVirtXY(int x, int y)
	{
		return (y >> 4 << MapLogX()) + (x >> 4);
	}
	*/
	/*
	typedef enum {
		OWNER_TOWN			= 0xf,	// a town owns the tile
		OWNER_NONE			= 0x10,	// nobody owns the tile
		OWNER_WATER			= 0x11,	// "water" owns the tile
		OWNER_SPECTATOR	= 0xff,	// spectator in MP or in scenario editor
	} Owner;
	*/
	//enum {
	public static final TileIndex INVALID_TILE = TileIndex.getInvalid(); //new TileIndex(-1);
	//};


	/*
	#ifndef _DEBUG
		#define TILE_ADD(x,y) ((x) + (y))
	#else
		extern TileIndex TileAdd(TileIndex tile, TileIndexDiff add,
			final char *exp, final char *file, int line);
		#define TILE_ADD(x, y) (TileAdd((x), (y), #x " + " #y, __FILE__, __LINE__))
	#endif

	#define TILE_ADDXY(tile, x, y) TILE_ADD(tile, TileDiffXY(x, y))
	*/
	//int TileAddWrap(TileIndex tile, int addx, int addy);


	// Functions to calculate distances
	//int DistanceManhattan(TileIndex, TileIndex); // also known as L1-Norm. Is the shortest distance one could go over diagonal tracks (or roads)
	//int DistanceSquare(TileIndex, TileIndex); // euclidian- or L2-Norm squared
	//int DistanceMax(TileIndex, TileIndex); // also known as L-Infinity-Norm
	//int DistanceMaxPlusManhattan(TileIndex, TileIndex); // Max + Manhattan
	//int DistanceFromEdge(TileIndex); // shortest distance from any edge of the map




	/* Approximation of the length of a straight track, relative to a diagonal
	 * track (ie the size of a tile side). #defined instead of final so it can
	 * stay integer. (no runtime float operations) Is this needed?
	 * Watch out! There are _no_ brackets around here, to prevent intermediate
	 * rounding! Be careful when using this!
	 * This value should be sqrt(2)/2 ~ 0.7071 */
	public static final int STRAIGHT_TRACK_LENGTH = 7071/10000;

	
	


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

		Global._map_log_x = BitOps.FindFirstBit(size_x);
		Global._map_size_x = size_x;
		Global._map_size_y = size_y;
		Global._map_size = size_x * size_y;
		Global._map_tile_mask = Global._map_size - 1;

		Global._m = new Tile[Global._map_size];

		if (Global._m == null) Global.error("Failed to allocate memory for the map");
	}


	/*
	#ifdef _DEBUG
	TileIndex TileAdd(TileIndex tile, TileIndexDiff add,
		final char *exp, final char *file, int line)
	{
		int dx;
		int dy;
		int x;
		int y;

		dx = add & MapMaxX();
		if (dx >= (int)MapSizeX() / 2) dx -= MapSizeX();
		dy = (add - dx) / (int)MapSizeX();

		x = TileX(tile) + dx;
		y = TileY(tile) + dy;

		if (x >= MapSizeX() || y >= MapSizeY()) {
			char buf[512];

			sprintf(buf, "TILE_ADD(%s) when adding 0x%.4X and 0x%.4X failed",
				exp, tile, add);
	#if !defined(_MSC_VER)
			fprintf(stderr, "%s:%d %s\n", file, line, buf);
	#else
			_assert(buf, (char*)file, line);
	#endif
		}

		assert(TileXY(x,y) == TILE_MASK(tile + add));

		return TileXY(x,y);
	}
	#endif
	*/

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



	static int DistanceManhattan(TileIndex t0, TileIndex t1)
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
