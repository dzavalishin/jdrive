package com.dzavalishin.game;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.struct.TileIndexDiffC;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.GenLandTable;
import com.dzavalishin.util.IntContainer;
import com.dzavalishin.xui.ViewPort;

public class Landscape extends GenLandTable
{

	private static final int PLAIN_TERRAIN_MAX = 8; // Orig valuie was 3
	
	/* Landscape types */
	//enum {
	public static final int LT_NORMAL = 0;
	public static final int LT_HILLY = 1;
	public static final int LT_DESERT = 2;
	public static final int LT_CANDY = 3;

	public static final int NUM_LANDSCAPE = 4;





	static final TileTypeProcs _tile_type_procs[] = {
			Clear._tile_type_clear_procs,
			Rail._tile_type_rail_procs,
			Road._tile_type_road_procs,
			Town._tile_type_town_procs,
			Tree._tile_type_trees_procs,
			Station._tile_type_station_procs,
			WaterCmd._tile_type_water_procs,
			DummyLand._tile_type_dummy_procs,
			Industry._tile_type_industry_procs,
			TunnelBridgeCmd._tile_type_tunnelbridge_procs,
			UnmovableCmd._tile_type_unmovable_procs,
	};

	/* landscape slope => sprite */
	public static final byte[] _tileh_to_sprite = {
			0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,0,
			0,0,0,0,0,0,0,16,0,0,0,17,0,15,18,0,
	};

	static final byte _inclined_tileh[] = {
			3, 9, 3, 6, 12, 6, 12, 9
	};


	public static void FindLandscapeHeightByTile(TileInfo ti, TileIndex tile)
	{
		assert(tile.getTile() < Global.MapSize());

		IntContainer ic = new IntContainer();

		ti.tile = tile;
		ti.map5 = 0xFF & tile.getMap().m5;
		ti.type = tile.GetTileType().ordinal();
		ti.tileh = tile.GetTileSlope( ic );
		ti.z = ic.v;
	}

	/** find the landscape height for the coordinates x y */
	public static TileInfo FindLandscapeHeight(int x, int y)
	{
		TileInfo ti = new TileInfo();
		
		ti.x = x;
		ti.y = y;

		if (
				x < 0 || y < 0 ||
				x >= Global.MapMaxX() * 16 - 1 || y >= Global.MapMaxY() * 16 - 1) 
		{
			ti.tileh = 0;
			ti.type = TileTypes.MP_VOID.ordinal();
			ti.tile = null;
			ti.map5 = 0;
			ti.z = 0;
			return ti;
		}

		FindLandscapeHeightByTile(ti, TileIndex.TileVirtXY(x, y));
		return ti;
	}

	static int GetPartialZ(int x, int y, int corners)
	{
		int z = 0;

		switch(corners) {
		case 1:
			if (x - y >= 0)
				z = (x - y) >> 1;
				break;

		case 2:
			y^=0xF;
			if ( (x - y) >= 0)
				z = (x - y) >> 1;
				break;

		case 3:
			z = (x>>1) + 1;
			break;

		case 4:
			if (y - x >= 0)
				z = (y - x) >> 1;
				break;

		case 5:
		case 10:
		case 15:
			z = 4;
			break;

		case 6:
			z = (y>>1) + 1;
			break;

		case 7:
			z = 8;
			y^=0xF;
			if (x - y < 0)
				z += (x - y) >> 1;
			break;

			case 8:
				y ^= 0xF;
				if (y - x >= 0)
					z = (y - x) >> 1;
					break;

			case 9:
				z = (y^0xF)>>1;
				break;

			case 11:
				z = 8;
				if (x - y < 0)
					z += (x - y) >> 1;
				break;

				case 12:
					z = (x^0xF)>>1;
					break;

				case 13:
					z = 8;
					y ^= 0xF;
					if (y - x < 0)
						z += (y - x) >> 1;
					break;

					case 14:
						z = 8;
						if (y - x < 0)
							z += (y - x) >> 1;
						break;

						case 23:
							z = 1 + ((x+y)>>1);
							break;

						case 27:
							z = 1 + ((x+(y^0xF))>>1);
							break;

						case 29:
							z = 1 + (((x^0xF)+(y^0xF))>>1);
							break;

						case 30:
							z = 1 + (((x^0xF)+(y^0xF))>>1);
							break;
		}

		return z;
	}

	public static int GetSlopeZ(int x,  int y)
	{
		TileInfo ti = FindLandscapeHeight(x, y);
		return _tile_type_procs[ti.type].get_slope_z_proc.apply(ti);
	}

	// direction=true:  check for foundation in east and south corner
	// direction=false: check for foundation in west and south corner
	static boolean hasFoundation(final TileInfo ti, boolean direction)
	{
		boolean south, other; // southern corner and east/west corner
		int slope = _tile_type_procs[ti.type].get_slope_tileh_proc.apply(ti);
		int tileh = ti.tileh;

		if (slope == 0 && slope != tileh) tileh = 15;
		south = (tileh & 2) != (slope & 2);

		if (direction) {
			other = (tileh & 4) != (slope & 4);
		} else {
			other = (tileh & 1) != (slope & 1);
		}
		return south || other;
	}

	static void DrawFoundation(TileInfo ti, int f)
	{
		int sprite_base = Sprite.SPR_SLOPES_BASE-14;

		TileInfo ti2;// = new TileInfo();

		ti2 = FindLandscapeHeight(ti.x, ti.y - 1);
		if (hasFoundation(ti2, true)) sprite_base += 22;		// foundation in NW direction
		ti2 = FindLandscapeHeight(ti.x - 1, ti.y);
		if (hasFoundation(ti2, false)) sprite_base += 22 * 2;	// foundation in NE direction

		if (f < 15) {
			// leveled foundation
			if (sprite_base < Sprite.SPR_SLOPES_BASE) sprite_base = Sprite.SPR_FOUNDATION_BASE + 1; // use original slope sprites

			ViewPort.AddSortableSpriteToDraw(f - 1 + sprite_base, ti.x, ti.y, 16, 16, 7, ti.z);
			ti.z += 8;
			ti.tileh = 0;
			ViewPort.OffsetGroundSprite(31, 1);
		} else {
			// inclined foundation
			sprite_base += 14;

			ViewPort.AddSortableSpriteToDraw(
					BitOps.HASBIT((1<<1) | (1<<2) | (1<<4) | (1<<8), ti.tileh) ? sprite_base + (f - 15) : Sprite.SPR_FOUNDATION_BASE + ti.tileh,
							ti.x, ti.y, 1, 1, 1, ti.z
					);

			ti.tileh = _inclined_tileh[f - 15];
			ViewPort.OffsetGroundSprite(31, 9);
		}
	}

	public static void DoClearSquare(TileIndex tile)
	{
		Landscape.ModifyTile(tile, TileTypes.MP_CLEAR,
				//TileTypes.MP_SETTYPE(TileTypes.MP_CLEAR) |
				TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5,
				Owner.OWNER_NONE, /* map_owner */
				Global._generating_world ? 3 : 0 /* map5 */
				);
	}

	//static int GetTileTrackStatus(TileIndex tile, TransportType mode)
	public static int GetTileTrackStatus(TileIndex tile, /*int*/ TransportType mode)
	{
		return _tile_type_procs[tile.GetTileType().ordinal()].get_tile_track_status_proc.applyAsInt(tile, mode);
	}

	static void ChangeTileOwner(TileIndex tile, int old_player, int new_player)
	{
		_tile_type_procs[tile.GetTileType().ordinal()].change_tile_owner_proc.apply(tile, PlayerID.get( old_player ), PlayerID.get( new_player) );
	}

	public static AcceptedCargo GetAcceptedCargo(TileIndex tile)
	{
		//AcceptedCargo ac = new AcceptedCargo();
		//memset(ac, 0, sizeof(AcceptedCargo));
		//ac.clear();
		return _tile_type_procs[tile.GetTileType().ordinal()].get_accepted_cargo_proc.apply(tile);
	}

	static void AnimateTile(TileIndex tile)
	{
		_tile_type_procs[tile.GetTileType().ordinal()].animate_tile_proc.accept(tile);
	}

	public static void ClickTile(TileIndex tile)
	{
		_tile_type_procs[tile.GetTileType().ordinal()].click_tile_proc.accept(tile);
	}

	public static void DrawTile(TileInfo ti)
	{
		_tile_type_procs[ti.type].draw_tile_proc.accept(ti);
	}

	public static TileDesc GetTileDesc(TileIndex tile)
	{
		return _tile_type_procs[tile.GetTileType().ordinal()].get_tile_desc_proc.apply(tile);
	}

	/** Clear a piece of landscape
	 * @param x,y coordinates of clearance
	 * @param p1 unused
	 * @param p2 unused
	 */
	static int CmdLandscapeClear(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		final int ordinal = tile.GetTileType().ordinal();
		final TileTypeProcs func = _tile_type_procs[ordinal];
		return func.clear_tile_proc.applyAsInt(tile, (byte)flags);
	}

	/** Clear a big piece of landscape
	 * @param x,y end coordinates of area dragging
	 * @param p1 start tile of area dragging
	 * @param p2 unused
	 */
	static int CmdClearArea(int ex, int ey, int flags, int p1, int p2)
	{
		int cost, ret; 
		long money;
		int sx,sy;
		int x,y;
		boolean success = false;

		if (p1 > Global.MapSize()) return Cmd.CMD_ERROR;

		TileIndex pi1 = new TileIndex(p1);

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		// make sure sx,sy are smaller than ex,ey
		sx = pi1.TileX() * 16;
		sy = pi1.TileY() * 16;
		if (ex < sx) { int t = sx; sx = ex; ex = t; } // intswap(ex, sx);
		if (ey < sy) { int t = sy; sy = ey; ey = t; } // intswap(ey, sy);

		money = Cmd.GetAvailableMoneyForCommand();
		cost = 0;

		for (x = sx; x <= ex; x += 16) {
			for (y = sy; y <= ey; y += 16) {
				ret = Cmd.DoCommandByTile(TileIndex.TileVirtXY(x, y), 0, 0, flags & ~Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
				if (Cmd.CmdFailed(ret)) continue;
				cost += ret;
				success = true;

				if(0 != (flags & Cmd.DC_EXEC)) {
					if (ret > 0 && (money -= ret) < 0) {
						Global._additional_cash_required = ret;
						return cost - ret;
					}
					Cmd.DoCommandByTile(TileIndex.TileVirtXY(x, y), 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);

					// draw explosion animation...
					if ((x == sx || x == ex) && (y == sy || y == ey)) {
						// big explosion in each corner, or small explosion for single tiles
						Vehicle.CreateEffectVehicleAbove(x + 8, y + 8, 2,
								sy == ey && sx == ex ? Vehicle.EV_EXPLOSION_SMALL : Vehicle.EV_EXPLOSION_LARGE
								);
					}
				}
			}
		}

		return (success) ? cost : Cmd.CMD_ERROR;
	}


	/* utility function used to modify a tile */
	public static void ModifyTile(TileIndex tile, TileTypes type, int flags, int ... args)
	{
		//int i;
		int p = 0;

		/*
		if ((i = BitOps.GB(flags, 8, 4)) != 0) {
			tile.SetTileType( TileTypes.values[(i - 1)] );
		}*/
		// TODO use Optional<TileTypes>
		assert 0 == BitOps.GB(flags, 8, 4); // type was here
		if(type != TileTypes.MP_NOCHANGE) tile.SetTileType( type );

		if( 0 != (flags & (TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP2)) ) {
			int x = 0;
			if(0 != (flags & TileTypes.MP_MAP2)) x = args[p++];
			tile.getMap().m2 = x;
		}

		if( 0 != (flags & (TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3LO)) ) {
			int x = 0;
			if( 0 != (flags & TileTypes.MP_MAP3LO) ) x = args[p++];
			tile.getMap().m3 =  x;
		}

		if( 0 !=  (flags & (TileTypes.MP_MAP3HI_CLEAR | TileTypes.MP_MAP3HI)) ) {
			int x = 0;
			if( 0 !=  (flags & TileTypes.MP_MAP3HI) ) x = args[p++];
			tile.getMap().m4 =  x;
		}

		if( 0 != (flags & (TileTypes.MP_MAPOWNER|TileTypes.MP_MAPOWNER_CURRENT)) ) {
			/*PlayerID*/ int x = PlayerID.getCurrent().id;
			if(0 != (flags & TileTypes.MP_MAPOWNER) ) x = args[p++];
			tile.getMap().m1 = x;
		}

		if( 0 != (flags & TileTypes.MP_MAP5) ) {
			tile.getMap().m5 = 0xFF & args[p++];
		}


		if ( 0 == (flags & TileTypes.MP_NODIRTY))
			tile.MarkTileDirtyByTile();
	}

	private static final int TILELOOP_BITS = 4;
	private static final int TILELOOP_SIZE = (1 << TILELOOP_BITS);
	private static final int TILELOOP_ASSERTMASK = ((TILELOOP_SIZE-1) + ((TILELOOP_SIZE-1) << Global.MapLogX()));
	private static final int TILELOOP_CHKMASK  =(((1 << (Global.MapLogX() - TILELOOP_BITS))-1) << TILELOOP_BITS);

	static void RunTileLoop()
	{
		int tile = Global._cur_tileloop_tile;

		assert( (tile & ~TILELOOP_ASSERTMASK) == 0);
		int count = (Global.MapSizeX() / TILELOOP_SIZE) * (Global.MapSizeY() / TILELOOP_SIZE);
		do {
			TileIndex itile = new TileIndex(tile);
			final int ordinal = itile.GetTileType().ordinal();
			final TileTypeProcs func = _tile_type_procs[ordinal];
			func.tile_loop_proc.accept(itile);

			if (itile.TileX() < Global.MapSizeX() - TILELOOP_SIZE) {
				tile += TILELOOP_SIZE; /* no overflow */
			} else {
				tile = TileIndex.TILE_MASK(
						tile 
						- TILELOOP_SIZE * (Global.MapSizeX() / TILELOOP_SIZE - 1) 
						+ TileIndex.TileDiffXY(0, TILELOOP_SIZE).diff ); /* x would overflow, also increase y */
			}
		} while (--count != 0);
		assert( (tile & ~TILELOOP_ASSERTMASK) == 0);

		tile += 9;
		if(0 != (tile & TILELOOP_CHKMASK) )
			tile = (tile + Global.MapSizeX()) & TILELOOP_ASSERTMASK;
		Global._cur_tileloop_tile = tile;
	}

	static void InitializeLandscape()
	{
		int map_size = Global.MapSize();
		int i;

		Global.gs._m = new Tile[map_size];

		for (i = 0; i < map_size; i++) 
		{
			Global.gs._m[i] = new Tile();

			/*
			Global._m[i].type        = TileTypes.MP_CLEAR.ordinal();
			Global._m[i].height      = 0;
			Global._m[i].m1          = Owner.OWNER_NONE;
			Global._m[i].m2          = 0;
			Global._m[i].m3          = 0;
			Global._m[i].m4          = 0;
			Global._m[i].m5          = 3;
			Global._m[i].extra       = 0;
			*/
		}

		// create void tiles at the border
		for (i = 0; i < Global.MapMaxY(); ++i)
		{			
			TileIndex t = new TileIndex(i * Global.MapSizeX() + Global.MapMaxX());
			t.SetTileType(TileTypes.MP_VOID);
		}

		for (i = 0; i < Global.MapSizeX(); ++i)
		{
			TileIndex t = new TileIndex(Global.MapSizeX() * Global.MapMaxY() + i);
			t.SetTileType(TileTypes.MP_VOID);
		}
	}

	static void ConvertGroundTilesIntoWaterTiles()
	{
		IntContainer h = new IntContainer();

		for (int ti = 0; ti < Global.MapSize(); ++ti) 
		{
			TileIndex tile = new TileIndex(ti);

			if (tile.IsTileType(TileTypes.MP_CLEAR) && tile.GetTileSlope(h) == 0 && h.v == 0) {
				tile.SetTileType(TileTypes.MP_WATER);
				tile.getMap().m5 = 0;
				tile.SetTileOwner(Owner.OWNER_WATER);
			}
		}
	}

	static final byte _genterrain_tbl_1[] = { 10, 22, 33, 37, 4 };
	static final byte _genterrain_tbl_2[] = { 0, 0, 0, 0, 33 };

	static void GenerateTerrain(int type, int flag)
	{
		long r;
		int x;
		int y;
		int w;
		int h;

		final Sprite template;		
		final byte[] p;

		int pi = 0; // p index
		//TileIndex tile;
		//Tile tile;
		int direction;

		r = Hal.Random32();
		template = SpriteCache.GetSprite((int)(((r >> 24) * _genterrain_tbl_1[type]) >> 8) + _genterrain_tbl_2[type] + 4845);

		x = (int)(r & Global.MapMaxX());
		y = (int)((r >> Global.MapLogX()) & Global.MapMaxY());


		if (x < 2 || y < 2)
			return;

		direction =  BitOps.GB((int)r, 22, 2);
		if (0 != (direction & 1)) {
			w = template.height;
			h = template.width;
		} else {
			w = template.width;
			h = template.height;
		}
		p = template.data;

		if (0 != (flag & 4)) {
			int xw = x * Global.MapSizeY();
			int yw = y * Global.MapSizeX();
			int bias = (Global.MapSizeX() + Global.MapSizeY()) * 16;

			switch (flag & 3) {
			case 0:
				if (xw + yw > Global.MapSize() - bias) return;
				break;

			case 1:
				if (yw < xw + bias) return;
				break;

			case 2:
				if (xw + yw < Global.MapSize() + bias) return;
				break;

			case 3:
				if (xw < yw + bias) return;
				break;
			}
		}

		if (x + w >= Global.MapMaxX() - 1)
			return;

		if (y + h >= Global.MapMaxY() - 1)
			return;

		MutableTileIndex tile = new MutableTileIndex(new TileIndex(x, y));//.getMap();

		switch (direction) {
		case 0:
			do {
				MutableTileIndex tile_cur = new MutableTileIndex(tile);
				int w_cur;

				for (w_cur = w; w_cur != 0; --w_cur) {
					if (p[pi] >= tile_cur.getMap().get_type_height()) tile_cur.getMap().set_type_height( p[pi] );
					pi++;
					tile_cur.madd(1);
				}
				tile.madd( TileIndex.TileDiffXY(0, 1) );
			} while (--h != 0);
			break;

		case 1:
			do {
				MutableTileIndex tile_cur = new MutableTileIndex(tile);
				int h_cur;

				for (h_cur = h; h_cur != 0; --h_cur) {
					if (p[pi] >= tile_cur.getMap().get_type_height()) tile_cur.getMap().set_type_height( p[pi] );
					pi++;
					tile_cur.madd( TileIndex.TileDiffXY(0, 1) );
				}
				tile.madd(1);
			} while (--w != 0);
			break;

		case 2:
			tile.madd( TileIndex.TileDiffXY(w - 1, 0) );
			do {
				MutableTileIndex tile_cur = new MutableTileIndex(tile);
				int w_cur;

				for (w_cur = w; w_cur != 0; --w_cur) {
					if (p[pi] >= tile_cur.getMap().get_type_height()) tile_cur.getMap().set_type_height( p[pi] );
					pi++;
					tile_cur.msub(1);
				}
				tile.madd( TileIndex.TileDiffXY(0, 1) );
			} while (--h != 0);
			break;

		case 3:
			tile.madd( TileIndex.TileDiffXY(0, h - 1) );
			do {
				MutableTileIndex tile_cur = new MutableTileIndex(tile);
				int h_cur;

				for (h_cur = h; h_cur != 0; --h_cur) {
					if (p[pi] >= tile_cur.getMap().get_type_height()) tile_cur.getMap().set_type_height( p[pi] );
					pi++;
					tile_cur.msub( TileIndex.TileDiffXY(0, 1) );
				}
				tile.madd(1);
			} while (--w != 0);
			break;
		}
	}


	//#include "table/genland.h"

	static void CreateDesertOrRainForest()
	{
		//TileIndex tile;
		//TileIndexDiffC data;
		int i;
		boolean broken = false;

		for(int ti = 0; ti != Global.MapSize(); ++ti) 
		{
			TileIndex tile = new TileIndex(ti);

			//for (data = _make_desert_or_rainforest_data;
			//		data != endof(_make_desert_or_rainforest_data); ++data)
			broken = false;
			for( TileIndexDiffC data : _make_desert_or_rainforest_data)
			{
				final int scan = ti + TileIndex.ToTileIndexDiff(data).diff;
				//TileIndex t = new TileIndex( TileIndex.TILE_MASK(scan) );
				TileIndex t = new TileIndex( scan );
				if(!t.isValid()) 
				{
					broken = true;
					break;
				}
				
				final int tileHeight = t.TileHeight();
				if (tileHeight >= 4 
						|| t.IsTileType(TileTypes.MP_WATER)) 
				{
					broken = true;
					break;
				}
			}
			if( !broken )// (data == endof(_make_desert_or_rainforest_data))
			{
				//Global.debug("desert ");
				tile.SetMapExtraBits(TileInfo.EXTRABITS_DESERT);
			}
		}

		for (i = 0; i != 256; i++)
			RunTileLoop();

		broken = false;
		for(int ti = 0; ti != Global.MapSize(); ++ti) 
		{
			TileIndex tile = new TileIndex(ti);

			//for (data = _make_desert_or_rainforest_data;
			//		data != endof(_make_desert_or_rainforest_data); ++data) {
			for( TileIndexDiffC data : _make_desert_or_rainforest_data)
			{
				TileIndex t = new TileIndex( TileIndex.TILE_MASK(ti + TileIndex.ToTileIndexDiff(data).diff));
				if (t.IsTileType(TileTypes.MP_CLEAR) 
						&& (t.getMap().m5 & 0x1c) == 0x14) 
				{
					broken = true;
					break;
				}
			}
			if( !broken )//if (data == endof(_make_desert_or_rainforest_data))
				tile.SetMapExtraBits(2);
		}
	}

	static void GenerateLandscape()
	{
		int i;
		int flag;
		int r;

		if (GameOptions._opt.landscape == LT_HILLY) {
			for (i = Map.ScaleByMapSize((Hal.Random() & 0x7F) + 950); i != 0; --i)
				GenerateTerrain(2, 0);

			r = Hal.Random();
			flag = BitOps.GB(r, 0, 2) | 4;
			for (i = Map.ScaleByMapSize(BitOps.GB(r, 16, 7) + 450); i != 0; --i)
				GenerateTerrain(4, flag);
		} else if (GameOptions._opt.landscape == LT_DESERT) {
			for (i = Map.ScaleByMapSize((Hal.Random()&0x7F) + 170); i != 0; --i)
				GenerateTerrain(0, 0);

			r = Hal.Random();
			flag = BitOps.GB(r, 0, 2) | 4;
			for (i = Map.ScaleByMapSize(BitOps.GB(r, 16, 8) + 1700); i != 0; --i)
				GenerateTerrain(0, flag);

			flag ^= 2;

			for (i = Map.ScaleByMapSize((Hal.Random() & 0x7F) + 410); i != 0; --i)
				GenerateTerrain(3, flag);
		} else {
			i = Map.ScaleByMapSize((Hal.Random() & 0x7F) + (PLAIN_TERRAIN_MAX - GameOptions._opt.diff.quantity_sea_lakes) * 256 + 100);
			for (; i != 0; --i)
				GenerateTerrain(GameOptions._opt.diff.terrain_type, 0);
		}

		ConvertGroundTilesIntoWaterTiles();

		if (GameOptions._opt.landscape == LT_DESERT)
			CreateDesertOrRainForest();
	}


	static void CallLandscapeTick()
	{
		Town.OnTick_Town();
		Tree.OnTick_Trees();
		Station.OnTick_Station();
		Industry.OnTick_Industry();

		Player.OnTick_Players();
		TrainCmd.OnTick_Train();
	}

	public static TileIndex AdjustTileCoordRandomly(TileIndex a, int rng)
	{
		int r = Hal.Random();

		return new TileIndex(
				a.TileX() + (BitOps.GB(r, 0, 8) * rng * 2 >> 8) - rng,
				a.TileY() + (BitOps.GB(r, 8, 8) * rng * 2 >> 8) - rng
				).TILE_MASK();
	}




}
