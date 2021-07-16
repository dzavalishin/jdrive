package game;

public class TileIndex {
	private int tile;

	
	
	/** static  TileIndex TileXY(int x, int y)
	 * 
	 * @param x
	 * @param y
	 */
	public TileIndex(int x, int y)
	{
		tile = (y * Global.MapSizeX()) + x;
		assert( tile > 0 );
		// TODO assert < max
	}

	// TODO rename to getTileIndex
	public int getTile() {
		return tile;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TileIndex) {
			TileIndex him = (TileIndex) obj;
			return him.tile == tile;
		}
		return super.equals(obj);
	}
	
	public TileIndex(int tile)
	{
		this.tile = tile;
	}
	
	public static TileIndex INVALID_TILE = new TileIndex(-1);
	
	int TileX()
	{
		return tile & Global.MapMaxX();
	}

	int TileY()
	{
		return tile >> Global.MapLogX();
	}
	
	
	int GetTileSlope(IntContainer h)
	{
		int a;
		int b;
		int c;
		int d;
		int min;
		int r;

		assert(tile < Global.MapSize());

		if (TileX() == Global.MapMaxX() || TileY() == Global.MapMaxY()) {
			if (h != null) h.v = 0;
			return 0;
		}

		min = a = TileHeight();
		b = TileHeight(tile + TileDiffXY(1, 0).diff);
		if (min >= b) min = b;
		c = TileHeight(tile + TileDiffXY(0, 1).diff);
		if (min >= c) min = c;
		d = TileHeight(tile + TileDiffXY(1, 1).diff);
		if (min >= d) min = d;

		r = 0;
		if ((a -= min) != 0) r += (--a << 4) + 8;
		if ((c -= min) != 0) r += (--c << 4) + 4;
		if ((d -= min) != 0) r += (--d << 4) + 2;
		if ((b -= min) != 0) r += (--b << 4) + 1;

		if (h != null)
			h.v = min * 8;

		return r;
	}

	int GetTileZ()
	{
		IntContainer h = new IntContainer();
		GetTileSlope(h);
		return h.v;
	}

	
	/* Approximation of the length of a straight track, relative to a diagonal
	 * track (ie the size of a tile side). #defined instead of const so it can
	 * stay integer. (no runtime float operations) Is this needed?
	 * Watch out! There are _no_ brackets around here, to prevent intermediate
	 * rounding! Be careful when using this!
	 * This value should be sqrt(2)/2 ~ 0.7071 */
	public static int STRAIGHT_TRACK_LENGTH = 7071/10000;
	

	
	
	
	
	
	static TileIndexDiff TileDiffXY(int x, int y)
	{
		// Multiplication gives much better optimization on MSVC than shifting.
		// 0 << shift isn't optimized to 0 properly.
		// Typically x and y are constants, and then this doesn't result
		// in any actual multiplication in the assembly code..
		return new TileIndexDiff((y * Global.MapSizeX()) + x);
	}
	
	
	
	static TileIndex TileVirtXY(int x, int y)
	{
		return new TileIndex((y >> 4 << Global.MapLogX()) + (x >> 4));
	}
	
	
	int TileHeight()
	{
		//assert(tile < MapSize());
		return Global._m[tile].height;
	}

	static int TileHeight(int index)
	{
		assert(index < Global.MapSize());
		assert(index > 0);
		return Global._m[index].height;
	}

	int TilePixelHeight()
	{
		return TileHeight() * 8;
	}
	
	static  boolean IsSteepTileh(int tileh)
	{
		return 0 != (tileh & 0x10);
	}

	void SetTileHeight(int height)
	{
		//assert(tile < MapSize());
		assert(height < 16);
		Global._m[tile].height = height;
	}

	TileType GetTileType()
	{
		//assert(tile < MapSize());
		return new TileType(Global._m[tile].type);
	}

	void SetTileType(TileType type)
	{
		//assert(tile < MapSize());
		Global._m[tile].type = type.type;
	}

	boolean IsTileType(TileType type)
	{
		return GetTileType().type == type.type;
	}

	boolean IsTileType(TileTypes type)
	{
		return GetTileType().type == type.ordinal();
	}
	
	boolean IsTunnelTile()
	{
		return IsTileType(TileTypes.MP_TUNNELBRIDGE) && GB(Global._m[tile].m5, 4, 4) == 0;
	}

	Owner GetTileOwner()
	{
		//assert(tile < MapSize());
		assert(!IsTileType(TileTypes.MP_HOUSE));
		assert(!IsTileType(TileTypes.MP_VOID));
		assert(!IsTileType(TileTypes.MP_INDUSTRY));

		return new Owner(Global._m[tile].m1);
	}

	void SetTileOwner(Owner owner)
	{
		//assert(tile < MapSize());
		assert(!IsTileType(TileTypes.MP_HOUSE));
		assert(!IsTileType(TileTypes.MP_VOID));
		assert(!IsTileType(TileTypes.MP_INDUSTRY));

		Global._m[tile].m1 = owner.owner;
	}

	boolean IsTileOwner(Owner owner)
	{
		return GetTileOwner() == owner;
	}
	
}


enum TileTypes {
	MP_CLEAR,
	MP_RAILWAY,
	MP_STREET,
	MP_HOUSE,
	MP_TREES,
	MP_STATION,
	MP_WATER,
	MP_VOID, // invisible tiles at the SW and SE border
	MP_INDUSTRY,
	MP_TUNNELBRIDGE,
	MP_UNMOVABLE,
}
