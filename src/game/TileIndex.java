package game;

import java.util.function.Consumer;

import game.util.BitOps;

public class TileIndex {
	private int tile;


	/**
	 * Iterate over a tiles rectangle.
	 * 
	 * TODO Rename to forEach
	 * 
	 * @param w - rectangle width
	 * @param h - height
	 * @param tile - start pos
	 * @param c - code to run for each tile
	 */
	public static void forAll( int w, int h, int tile, Consumer<TileIndex> c )
	{
		//TileIndex ti = new TileIndex(tile);


		{                                                        
			int h_cur = h;                                         
			int var = tile;                                       
			do {                                                   
				int w_cur = w;                                       
				do {

					//ti.tile = var;
					//c.accept(ti);
					c.accept( new TileIndex(var) );

					++var;
				} while ( --w_cur != 0);
				//int diff = (y * Global.MapSizeX()) + x);
				var += Global.MapSizeX() - w;
				//} while (var += TileDiffXY(0, 1) - (w), --h_cur != 0);				
			} while ( --h_cur != 0); 
		}


	}

	public TileIndex sub(TileIndexDiff diff)
	{
		tile -= diff.diff;
		return this;
	}

	public TileIndex add(TileIndexDiff diff)
	{
		tile += diff.diff;
		return this;
	}

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

	public static  TileIndex TileXY(int x, int y)
	{
		return new TileIndex(x, y);
	}


	public TileIndex(TileIndex src)
	{
		tile = src.tile;
	}


	// TODO rename to getTileIndex
	public int getTile() {
		return tile;
	}

	public Tile getMap()
	{
		return Global._m[tile];
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

	static boolean CorrectZ(int tileh)
	{
		/* tile height must be corrected if the north corner is not raised, but
		 * any other corner is. These are the cases 1 till 7 */
		return BitOps.IS_INT_INSIDE(tileh, 1, 8);
	}


	void SetMapExtraBits(int i)
	{
		//assert(tile < Global.MapSize());
		Global._m[tile].extra = (byte) BitOps.RETSB(Global._m[tile].extra, 0, 2, i & 3);
	}

	int GetMapExtraBits()
	{
		//assert(tile < MapSize());
		return BitOps.GB(Global._m[tile].extra, 0, 2);
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


	// This function checks if we add addx/addy to tile, if we
	//  do wrap around the edges. For example, tile = (10,2) and
	//  addx = +3 and addy = -4. This function will now return
	//  INVALID_TILE, because the y is wrapped. This is needed in
	//  for example, farmland. When the tile is not wrapped,
	//  the result will be tile + TileDiffXY(addx, addy)
	int TileAddWrap(TileIndex tile, int addx, int addy)
	{
		int x = tile.TileX() + addx;
		int y = tile.TileY() + addy;

		// Are we about to wrap?
		if (x < Global.MapMaxX() && y < Global.MapMaxY())
			return tile.getTile() + TileIndex.TileDiffXY(addx, addy).diff;

		return INVALID_TILE.getTile();
	}



	final static TileIndexDiffC _tileoffs_by_dir[] = {
			new TileIndexDiffC( -1,  0),
			new TileIndexDiffC(  0,  1),
			new TileIndexDiffC(  1,  0),
			new TileIndexDiffC(  0, -1)
	};


	static  TileIndexDiff TileOffsByDir(int dir)
	{
		//extern final TileIndexDiffC _tileoffs_by_dir[4];

		assert(dir < _tileoffs_by_dir.length);
		return TileIndex.ToTileIndexDiff(_tileoffs_by_dir[dir]);
	}


	static  TileIndexDiffC TileIndexDiffCByDir(int dir) {
		//extern final TileIndexDiffC _tileoffs_by_dir[4];
		return _tileoffs_by_dir[dir];
	}

	/* Returns tile + the diff given in diff. If the result tile would end up
	 * outside of the map, INVALID_TILE is returned instead.
	 */
	static  TileIndex AddTileIndexDiffCWrap(TileIndex tile, TileIndexDiffC diff) {
		int x = tile.TileX() + diff.x;
		int y = tile.TileY() + diff.y;
		if (x < 0 || y < 0 || x > (int)Global.MapMaxX() || y > (int)Global.MapMaxY())
			return INVALID_TILE;
		else
			return TileXY(x, y);
	}



	/*
	static  TileIndex TileXY(int x, int y)
	{
		return new TileIndex( (y * Global.MapSizeX()) + x );
	}
	 */


	/* Approximation of the length of a straight track, relative to a diagonal
	 * track (ie the size of a tile side). #defined instead of const so it can
	 * stay integer. (no runtime float operations) Is this needed?
	 * Watch out! There are _no_ brackets around here, to prevent intermediate
	 * rounding! Be careful when using this!
	 * This value should be sqrt(2)/2 ~ 0.7071 */
	public static int STRAIGHT_TRACK_LENGTH = Map.STRAIGHT_TRACK_LENGTH;







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

	TileTypes GetTileType()
	{
		//assert(tile < MapSize());
		//return new TileType(Global._m[tile].type);
		return TileTypes.values[Global._m[tile].type];

	}

	void SetTileType(TileTypes type)
	{
		//assert(tile < MapSize());
		Global._m[tile].type = type.ordinal();
	}

	boolean IsTileType(TileType type)
	{
		return GetTileType().ordinal() == type.type;
	}

	boolean IsTileType(TileTypes type)
	{
		return GetTileType() == type;
	}

	boolean IsTunnelTile()
	{
		return IsTileType(TileTypes.MP_TUNNELBRIDGE) && BitOps.GB(Global._m[tile].m5, 4, 4) == 0;
	}

	boolean IsWaterTile() { return IsTileType(TileTypes.MP_WATER) && getMap().m5 == 0; }

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


	public static TileIndexDiff ToTileIndexDiff(TileIndexDiffC tidc)
	{
		return new TileIndexDiff((tidc.y << Global.MapLogX()) + tidc.x);
	}


	public static int TILE_MASK(int x) { return (x & Global._map_tile_mask); }
	public void TILE_ASSERT(int x) { assert TILE_MASK(x) == x; }


	public void clrBit_m1(int i) {		Global._m[tile].m1 = BitOps.RETCLRBIT(Global._m[tile].m1, i);	}
	public void setBit_m1(int i) {		Global._m[tile].m1 = BitOps.RETSETBIT(Global._m[tile].m1, i);	}

	public void clrBit_m2(int i) {		Global._m[tile].m2 = BitOps.RETCLRBIT(Global._m[tile].m2, i);	}
	public void setBit_m2(int i) {		Global._m[tile].m2 = BitOps.RETSETBIT(Global._m[tile].m2, i);	}

	public void clrBit_m3(int i) {		Global._m[tile].m3 = BitOps.RETCLRBIT(Global._m[tile].m3, i);	}
	public void setBit_m3(int i) {		Global._m[tile].m3 = BitOps.RETSETBIT(Global._m[tile].m3, i);	}

	public void clrBit_m4(int i) {		Global._m[tile].m4 = BitOps.RETCLRBIT(Global._m[tile].m4, i);	}
	public void setBit_m4(int i) {		Global._m[tile].m4 = BitOps.RETSETBIT(Global._m[tile].m4, i);	}

	public void clrBit_m5(int i) {		Global._m[tile].m5 = BitOps.RETCLRBIT(Global._m[tile].m5, i);	}
	public void setBit_m5(int i) {		Global._m[tile].m5 = BitOps.RETSETBIT(Global._m[tile].m5, i);	}

	public TileIndex TILE_MASK() {
		tile = TILE_MASK(tile);
		return this;
	}



	public boolean IsTrainStationTile()
	{
		return IsTileType(TileTypes.MP_STATION) && BitOps.IS_BYTE_INSIDE(getMap().m5, (byte)0, (byte)8);
	}

	public boolean IsCompatibleTrainStationTile(TileIndex ref)
	{
		assert(ref.IsTrainStationTile());
		return
				IsTrainStationTile() &&
				BitOps.GB(getMap().m3, 0, 4) == BitOps.GB(ref.getMap().m3, 0, 4) && // same rail type?
				BitOps.GB(getMap().m5, 0, 1) == BitOps.GB(ref.getMap().m5, 0, 1);   // same direction?
	}

	public boolean IsRoadStationTile() {
		return IsTileType(TileTypes.MP_STATION) && BitOps.IS_INT_INSIDE(getMap().m5, 0x43, 0x4B);
	}


	public boolean IsBuoyTile()
	{
		return IsTileType(TileTypes.MP_STATION) && getMap().m5 == 0x52;
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
	MP_UNMOVABLE;

	static TileTypes[] values = values();

}
