package game;

import java.io.Serializable;
import java.util.function.Function;

import game.enums.TileTypes;
import game.ids.PlayerID;
import game.struct.Point;
import game.struct.TileIndexDiff;
import game.struct.TileIndexDiffC;
import game.util.BitOps;
import game.util.IntContainer;
import game.xui.ViewPort;

// TODO make == work for TileIndex or we're busted - make fabric and disable new(), 
// return same TileIndex for each TileIndex.tile
// same with Mutable

public class TileIndex implements Comparable<TileIndex>, Serializable
{
	private static final long serialVersionUID = 2317687924857389962L;
	
	protected int tile;


	/** static  TileIndex TileXY(int x, int y)
	 * 
	 * @param x
	 * @param y
	 */
	public TileIndex(int x, int y)
	{
		tile = (y * Global.MapSizeX()) + x;
		//assert( tile >= 0 ); // TODO XXX can be out of map? [dz] smallmap uses us out of map :(
		// TODO assert < max
	}

	public static  TileIndex TileXY(int x, int y)
	{
		return new TileIndex(x, y);
	}


	public TileIndex(TileIndex src)
	//public get(TileIndex src)
	{
		tile = src.tile;
	}

	/*
	private static Map<Integer,TileIndex> ids = new HashMap<Integer,TileIndex>();
	public static TileIndex get(int id) 
	{
		TileIndex old = ids.get(id);
		if( old == null ) 
		{
			old = new TileIndex(id);
			ids.put(id, old);
		}
		return old;
	}*/

	public static TileIndex get(int id) 
	{
		return new TileIndex(id);
	}

	public static TileIndex getInvalid() {
		return get(-1);
	}

	public TileIndex(int tile)
	{
		this.tile = tile;
	}

	public static final TileIndex INVALID_TILE = getInvalid(); //new TileIndex(-1);

	
	
	
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TileIndex) {
			TileIndex him = (TileIndex) obj;
			return him.tile == tile;
		}
		return super.equals(obj);
	}


	@Override
	public int compareTo(TileIndex o) {
		// TODO Auto-generated method stub
		return this.tile - o.tile;
	}

	@Override
	public String toString() {
		return String.format("%d.%d", getX(), getY() );
	}

	@Override
	public int hashCode() {
		return tile;
	}









	/**
	 * Iterate over a tiles rectangle.
	 * 
	 * TODO Rename to forEach
	 * 
	 * @param w - rectangle width
	 * @param h - height
	 * @param tile - start pos
	 * @param c - code to run for each tile, breaks loop if returns true
	 */
	public static void forAll( int w, int h, TileIndex tile, Function<TileIndex,Boolean> c )
	{
		forAll( w, h, tile.getTile(), c );		
	}	
	/**
	 * Iterate over a tiles rectangle.
	 * 
	 * TODO Rename to forEach
	 * 
	 * @param w - rectangle width
	 * @param h - height
	 * @param tile - start pos
	 * @param c - code to run for each tile, breaks loop if returns true
	 */
	public static void forAll( int w, int h, int tile, Function<TileIndex,Boolean> c )
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
					if( c.apply( new TileIndex(var) ) )
						return;

					++var;
				} while ( --w_cur != 0);
				//int diff = (y * Global.MapSizeX()) + x);
				var += Global.MapSizeX() - w;
				//} while (var += TileDiffXY(0, 1) - (w), --h_cur != 0);				
			} while ( --h_cur != 0); 
		}


	}




	/**
	 * Iterate over a tiles rectangle.
	 * 
	 * @param w - rectangle width
	 * @param h - height
	 * @param tile - start pos
	 * @param c - code to run for each tile, breaks loop if returns true
	 */
	public static void forEach( int w, int h, int tile, TileIterator c )
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
					if( c.apply( new TileIndex(var), h_cur, w_cur ) )
						return;

					++var;
				} while ( --w_cur != 0);
				//int diff = (y * Global.MapSizeX()) + x);
				var += Global.MapSizeX() - w;
				//} while (var += TileDiffXY(0, 1) - (w), --h_cur != 0);				
			} while ( --h_cur != 0); 
		}


	}

















	// TODO rename to getTileIndex
	public int getTile() {
		return tile;
	}

	public Tile getMap()
	{
		return Global._m[tile];
	}

	public Tile M()
	{
		return Global._m[tile];
	}


	public int TileX()
	{
		return tile & Global.MapMaxX();
	}
	
	public int getX()
	{
		return tile & Global.MapMaxX();
	}

	public int TileY()
	{
		return tile >> Global.MapLogX();
	}

	public int getY()
	{
		return tile >> Global.MapLogX();
	}

	static boolean CorrectZ(int tileh)
	{
		/* tile height must be corrected if the north corner is not raised, but
		 * any other corner is. These are the cases 1 till 7 */
		return BitOps.IS_INT_INSIDE(tileh, 1, 8);
	}


	public void SetMapExtraBits(int i)
	{
		Global._m[tile].extra =  BitOps.RETSB(Global._m[tile].extra, 0, 2, i & 3);
	}

	public int GetMapExtraBits()
	{
		return BitOps.GB(Global._m[tile].extra, 0, 2);
	}

	public void MarkTileDirtyByTile()
	{
		Point pt = Point.RemapCoords(TileX() * 16, TileY() * 16, GetTileZ());
		ViewPort.MarkAllViewportsDirty(
				pt.x - 31,
				pt.y - 122,
				pt.x - 31 + 67,
				pt.y - 122 + 154
				);
	}

	public int GetTileSlope(IntContainer h)
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

	public int GetTileZ()
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
	public static int TileAddWrap(TileIndex tile, int addx, int addy)
	{
		int x = tile.TileX() + addx;
		int y = tile.TileY() + addy;

		// Are we about to wrap?
		if (x < Global.MapMaxX() && y < Global.MapMaxY())
			return tile.getTile() + TileIndex.TileDiffXY(addx, addy).diff;

		return INVALID_TILE.getTile();
	}

	public TileIndex TileAddWrap(int addx, int addy)
	{
		return new TileIndex(TileAddWrap(this, addx, addy));
	}


	final static TileIndexDiffC _tileoffs_by_dir[] = {
			new TileIndexDiffC( -1,  0),
			new TileIndexDiffC(  0,  1),
			new TileIndexDiffC(  1,  0),
			new TileIndexDiffC(  0, -1)
	};


	public static TileIndexDiff TileOffsByDir(int dir)
	{
		//extern final TileIndexDiffC _tileoffs_by_dir[4];

		assert(dir < _tileoffs_by_dir.length);
		return TileIndex.ToTileIndexDiff(_tileoffs_by_dir[dir]);
	}


	public static TileIndexDiffC TileIndexDiffCByDir(int dir) {
		//extern final TileIndexDiffC _tileoffs_by_dir[4];
		return _tileoffs_by_dir[dir];
	}

	/* Returns tile + the diff given in diff. If the result tile would end up
	 * outside of the map, INVALID_TILE is returned instead.
	 */
	public static  TileIndex AddTileIndexDiffCWrap(TileIndex tile, TileIndexDiffC diff) {
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
	public static int STRAIGHT_TRACK_LENGTH = game.Map.STRAIGHT_TRACK_LENGTH;







	public static TileIndexDiff TileDiffXY(int x, int y)
	{
		// Multiplication gives much better optimization on MSVC than shifting.
		// 0 << shift isn't optimized to 0 properly.
		// Typically x and y are constants, and then this doesn't result
		// in any actual multiplication in the assembly code..
		return new TileIndexDiff((y * Global.MapSizeX()) + x);
	}



	public static TileIndex TileVirtXY(int x, int y)
	{
		return new TileIndex((y >> 4 << Global.MapLogX()) + (x >> 4));
	}


	public int TileHeight()
	{
		//assert(tile < MapSize());
		return Global._m[tile].height;
	}

	public static int TileHeight(int index)
	{
		assert(index < Global.MapSize());
		assert(index >= 0); 
		return Global._m[index].height;
	}

	public int TilePixelHeight()
	{
		return TileHeight() * 8;
	}

	public static  boolean IsSteepTileh(int tileh)
	{
		return 0 != (tileh & 0x10);
	}

	public void SetTileHeight(int height)
	{
		//assert(tile < MapSize());
		assert(height < 16);
		Global._m[tile].height = height;
	}

	public TileTypes GetTileType()
	{
		//assert(tile < MapSize());
		//return new TileType(Global._m[tile].type);
		return TileTypes.values[Global._m[tile].type];

	}

	public void SetTileType(TileTypes type)
	{
		//assert(tile < MapSize());
		Global._m[tile].type = type.ordinal();
	}

	public boolean IsTileType(int type)
	{
		return GetTileType().ordinal() == type;
	}

	public boolean IsTileType(TileType type)
	{
		return GetTileType().ordinal() == type.type;
	}

	public boolean IsTileType(TileTypes type)
	{
		return GetTileType() == type;
	}

	public boolean IsTunnelTile()
	{
		return IsTileType(TileTypes.MP_TUNNELBRIDGE) && BitOps.GB(Global._m[tile].m5, 4, 4) == 0;
	}

	public boolean IsWaterTile() { return IsTileType(TileTypes.MP_WATER) && getMap().m5 == 0; }

	//Owner GetTileOwner()
	public PlayerID GetTileOwner()
	{
		//assert(tile < MapSize());
		assert(!IsTileType(TileTypes.MP_HOUSE));
		assert(!IsTileType(TileTypes.MP_VOID));
		assert(!IsTileType(TileTypes.MP_INDUSTRY));

		//return new Owner(Global._m[tile].m1);
		return PlayerID.get(Global._m[tile].m1);
	}

	public void SetTileOwner(PlayerID owner)
	{
		//assert(tile < MapSize());
		assert(!IsTileType(TileTypes.MP_HOUSE));
		assert(!IsTileType(TileTypes.MP_VOID));
		assert(!IsTileType(TileTypes.MP_INDUSTRY));

		Global._m[tile].m1 = owner.id;
	}

	public void SetTileOwner(int owner)
	{
		//assert(tile < MapSize());
		assert(!IsTileType(TileTypes.MP_HOUSE));
		assert(!IsTileType(TileTypes.MP_VOID));
		assert(!IsTileType(TileTypes.MP_INDUSTRY));

		Global._m[tile].m1 = owner;
	}

	/*
	boolean IsTileOwner(Owner owner)
	{
		return GetTileOwner().id == owner.owner;
	}*/

	public boolean IsTileOwner(PlayerID owner)
	{
		return GetTileOwner() == owner;
	}


	public boolean IsTileOwner(int owner)
	{
		return GetTileOwner().id == owner;
	}


	public static TileIndexDiff ToTileIndexDiff(TileIndexDiffC tidc)
	{
		return new TileIndexDiff((tidc.y << Global.MapLogX()) + tidc.x);
	}


	public static int TILE_MASK(int x) { return (x & Global._map_tile_mask); }
	public static void TILE_ASSERT(int x) { assert TILE_MASK(x) == x; }
	
	public void TILE_ASSERT() { assert TILE_MASK(tile) == tile; }


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

	// TODO must be in Mutable only!
	public TileIndex TILE_MASK() {
		tile = TILE_MASK(tile);
		return this;
	}



	public boolean IsTrainStationTile()
	{
		return IsTileType(TileTypes.MP_STATION) && BitOps.IS_BYTE_INSIDE(getMap().m5, 0, 8);
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


	// TODO use code below
	public static TileIndex RandomTileSeed(int r) 
	{ 
		return new TileIndex( TILE_MASK(r) ); 
	}

	public static TileIndex RandomTile() 
	{
		while(true)
		{
		 TileIndex t = new TileIndex(
				 Hal.Random() % Global.MapMaxX(),
				 Hal.Random() % Global.MapMaxY()
				 );
		 
		 if( t.IsValidTile())
			 return t;		 
		}		
	}



	/**
	 * Immutable sub
	 * @param diff
	 * @return returns modified TileIndex, original one is not changed
	 */
	public TileIndex isub(TileIndexDiff diff)
	{
		TileIndex ni = new TileIndex(this);
		ni.tile -= diff.diff;
		return ni;
	}

	/**
	 * Immutable add
	 * @param diff
	 * @return returns modified TileIndex, original one is not changed
	 */
	public TileIndex iadd(TileIndexDiff diff)
	{
		TileIndex ni = new TileIndex(this);
		ni.tile += diff.diff;
		return ni;
	}

	/**
	 * Immutable sub
	 * @param diff
	 * @return returns modified TileIndex, original one is not changed
	 */
	public TileIndex isub(int diff)
	{
		TileIndex ni = new TileIndex(this);
		ni.tile -= diff;
		return ni;
	}

	/**
	 * Immutable add
	 * @param diff
	 * @return returns modified TileIndex, original one is not changed
	 */
	public TileIndex iadd(int diff)
	{
		TileIndex ni = new TileIndex(this);
		ni.tile += diff;
		// TODO tile mask?
		return ni;
	}


	/**
	 * Immutable add - steps with given x and y 
	 * @param x
	 * @param y
	 * @return returns modified TileIndex, original one is not changed
	 */
	public TileIndex iadd(int x, int y)
	{
		TileIndex ni = new TileIndex(this);
		ni.tile += (y * Global.MapSizeX()) + x;
		return ni;
	}
	
	/**
	 * Immutable sub - steps back with given x and y 
	 * @param x
	 * @param y
	 * @return returns modified TileIndex, original one is not changed
	 */
	public TileIndex isub(int x, int y)
	{
		TileIndex ni = new TileIndex(this);
		ni.tile -= (y * Global.MapSizeX()) + x;
		return ni;
	}
	
	
	public boolean IsTileDepotType(int transportType) {
		return Depot.IsTileDepotType(this, transportType);
	}

	public boolean IsValidTile() 
	{
		return (tile >=0) && (tile < Global.MapSizeX() * Global.MapMaxY() && TileX() != Global.MapMaxX());
	}

	public boolean isValid() {
		return IsValidTile();
	}


	/**
	 * Returns whether the given tile is a level crossing.
	 */
	public boolean IsLevelCrossing()
	{
		return (getMap().m5 & 0xF0) == 0x10;
	}

	/* Direction as commonly used in v->direction, 8 way. */
	//typedef enum Directions {
	public static final int DIR_N   = 0;
	public static final int DIR_NE  = 1;      /* Northeast; upper right on your monitor */
	public static final int DIR_E   = 2;
	public static final int DIR_SE  = 3;
	public static final int DIR_S   = 4;
	public static final int DIR_SW  = 5;
	public static final int DIR_W   = 6;
	public static final int DIR_NW  = 7;
	public static final int DIR_END = 8;
	public static final int INVALID_DIR = 0xFF;
	//} Direction;


	
	// -----------------------------------------------
	// Ex-macros
	// -----------------------------------------------
	
	public static TileIndex TILE_ADDXY(TileIndex tile, int i, int j) {		
		return tile.iadd(i, j);
	}

	public static TileIndex TILE_ADD(TileIndex tile, TileIndexDiff diff) {
		return tile.iadd(diff);
	}

	public static TileIndex TILE_ADD(TileIndex tile, int diff) {
		return tile.iadd(diff);
	}
	
	
	// -----------------------------------------------
	// Delegates
	// -----------------------------------------------
	
	public boolean EnsureNoVehicle() {
		return Vehicle.EnsureNoVehicle(this);
	}

	public boolean CheckTileOwnership() {
		return Player.CheckTileOwnership(this);
	}

	public static int GetPartialZ(int x, int y, int corners) {
		return Landscape.GetPartialZ(x, y, corners);
	}
	
	public int GetTileTrackStatus(int mode) {
		return Landscape.GetTileTrackStatus(this, mode);
	}

	public boolean IsRailWaypoint() {
		return WayPoint.IsRailWaypoint(this);
	}

	public int GetRailTileType() {
		return Rail.GetRailTileType(this);
	}


}




@FunctionalInterface
interface TileIterator 
{
	boolean apply( TileIndex ti, int h_cur, int v_cur );
}

