package game;

import game.util.BitOps;
import game.util.GenLandTable;

public class Landscape extends GenLandTable
{

	
	 static final TileTypeProcs
		_tile_type_clear_procs,
		_tile_type_rail_procs,
		_tile_type_road_procs,
		_tile_type_town_procs,
		_tile_type_trees_procs,
		_tile_type_station_procs,
		_tile_type_water_procs,
		_tile_type_dummy_procs,
		_tile_type_industry_procs,
		_tile_type_tunnelbridge_procs,
		_tile_type_unmovable_procs;

	static final TileTypeProcs  static final _tile_type_procs[] = {
		&_tile_type_clear_procs,
		&_tile_type_rail_procs,
		&_tile_type_road_procs,
		&_tile_type_town_procs,
		&_tile_type_trees_procs,
		&_tile_type_station_procs,
		&_tile_type_water_procs,
		&_tile_type_dummy_procs,
		&_tile_type_industry_procs,
		&_tile_type_tunnelbridge_procs,
		&_tile_type_unmovable_procs,
	};
	 *
	/* landscape slope => sprite */
	static final byte[] _tileh_to_sprite = {
			0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,0,
			0,0,0,0,0,0,0,16,0,0,0,17,0,15,18,0,
	};

	static final byte _inclined_tileh[] = {
			3, 9, 3, 6, 12, 6, 12, 9
	};


	static void FindLandscapeHeightByTile(TileInfo ti, TileIndex tile)
	{
		assert(tile.getTile() < Global.MapSize());
		
		IntContainer ic = new IntContainer();
		
		ti.tile = tile;
		ti.map5 = tile.getMap().m5;
		ti.type = tile.GetTileType().ordinal();
		ti.tileh = tile.GetTileSlope( ic );
		ti.z = ic.v;
	}

	/* find the landscape height for the coordinates x y */
	static void FindLandscapeHeight(TileInfo ti, int x, int y)
	{
		ti.x = x;
		ti.y = y;

		if (x >= Global.MapMaxX() * 16 - 1 || y >= Global.MapMaxY() * 16 - 1) {
			ti.tileh = 0;
			ti.type = TileTypes.MP_VOID.ordinal();
			ti.tile = null;
			ti.map5 = 0;
			ti.z = 0;
			return;
		}

		FindLandscapeHeightByTile(ti, TileIndex.TileVirtXY(x, y));
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
		TileInfo ti;

		FindLandscapeHeight(ti, x, y);

		return _tile_type_procs[ti.type].get_slope_z_proc(ti);
	}

	// direction=true:  check for foundation in east and south corner
	// direction=false: check for foundation in west and south corner
	static boolean hasFoundation(static final TileInfo ti, boolean direction)
	{
		boolean south, other; // southern corner and east/west corner
		int slope = _tile_type_procs[ti.type].get_slope_tileh_proc(ti);
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

	void DrawFoundation(TileInfo ti, int f)
	{
		int sprite_base = SPR_SLOPES_BASE-14;

		TileInfo ti2;
		FindLandscapeHeight(ti2, ti.x, ti.y - 1);
		if (hasFoundation(ti2, true)) sprite_base += 22;		// foundation in NW direction
		FindLandscapeHeight(ti2, ti.x - 1, ti.y);
		if (hasFoundation(ti2, false)) sprite_base += 22 * 2;	// foundation in NE direction

		if (f < 15) {
			// leveled foundation
			if (sprite_base < SPR_SLOPES_BASE) sprite_base = SPR_FOUNDATION_BASE + 1; // use original slope sprites

			AddSortableSpriteToDraw(f - 1 + sprite_base, ti.x, ti.y, 16, 16, 7, ti.z);
			ti.z += 8;
			ti.tileh = 0;
			OffsetGroundSprite(31, 1);
		} else {
			// inclined foundation
			sprite_base += 14;

			AddSortableSpriteToDraw(
					BitOps.HASBIT((1<<1) | (1<<2) | (1<<4) | (1<<8), ti.tileh) ? sprite_base + (f - 15) : SPR_FOUNDATION_BASE + ti.tileh,
							ti.x, ti.y, 1, 1, 1, ti.z
					);

			ti.tileh = _inclined_tileh[f - 15];
			OffsetGroundSprite(31, 9);
		}
	}

	static void DoClearSquare(TileIndex tile)
	{
		ModifyTile(tile,
				MP_SETTYPE(MP_CLEAR) |
				MP_MAP2_CLEAR | MP_MAP3LO_CLEAR | MP_MAP3HI_CLEAR | MP_MAPOWNER | MP_MAP5,
				OWNER_NONE, /* map_owner */
				_generating_world ? 3 : 0 /* map5 */
				);
	}

	static int GetTileTrackStatus(TileIndex tile, TransportType mode)
	{
		return _tile_type_procs[GetTileType(tile)].get_tile_track_status_proc(tile, mode);
	}

	static void ChangeTileOwner(TileIndex tile, byte old_player, byte new_player)
	{
		_tile_type_procs[GetTileType(tile)].change_tile_owner_proc(tile, old_player, new_player);
	}

	static void GetAcceptedCargo(TileIndex tile, AcceptedCargo ac)
	{
		//memset(ac, 0, sizeof(AcceptedCargo));
		ac.clear();
		_tile_type_procs[GetTileType(tile)].get_accepted_cargo_proc(tile, ac);
	}

	static void AnimateTile(TileIndex tile)
	{
		_tile_type_procs[GetTileType(tile)].animate_tile_proc(tile);
	}

	static void ClickTile(TileIndex tile)
	{
		_tile_type_procs[GetTileType(tile)].click_tile_proc(tile);
	}

	static void DrawTile(TileInfo ti)
	{
		_tile_type_procs[ti.type].draw_tile_proc(ti);
	}

	static void GetTileDesc(TileIndex tile, TileDesc td)
	{
		_tile_type_procs[GetTileType(tile)].get_tile_desc_proc(tile, td);
	}

	/** Clear a piece of landscape
	 * @param x,y coordinates of clearance
	 * @param p1 unused
	 * @param p2 unused
	 */
	static int CmdLandscapeClear(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);

		SET_EXPENSES_TYPE(EXPENSES_CONSTRUCTION);

		return _tile_type_procs[GetTileType(tile)].clear_tile_proc(tile, flags);
	}

	/** Clear a big piece of landscape
	 * @param x,y end coordinates of area dragging
	 * @param p1 start tile of area dragging
	 * @param p2 unused
	 */
	static int CmdClearArea(int ex, int ey, int flags, int p1, int p2)
	{
		int cost, ret, money;
		int sx,sy;
		int x,y;
		boolean success = false;

		if (p1 > Global.MapSize()) return CMD_ERROR;

		TileIndex pi1 = new TileIndex(p1);

		SET_EXPENSES_TYPE(EXPENSES_CONSTRUCTION);

		// make sure sx,sy are smaller than ex,ey
		sx = pi1.TileX() * 16;
		sy = pi1.TileY() * 16;
		if (ex < sx) intswap(ex, sx);
		if (ey < sy) intswap(ey, sy);

		money = GetAvailableMoneyForCommand();
		cost = 0;

		for (x = sx; x <= ex; x += 16) {
			for (y = sy; y <= ey; y += 16) {
				ret = DoCommandByTile(TileIndex.TileVirtXY(x, y), 0, 0, flags & ~DC_EXEC, CMD_LANDSCAPE_CLEAR);
				if (CmdFailed(ret)) continue;
				cost += ret;
				success = true;

				if (flags & DC_EXEC) {
					if (ret > 0 && (money -= ret) < 0) {
						_additional_cash_required = ret;
						return cost - ret;
					}
					DoCommandByTile(TileIndex.TileVirtXY(x, y), 0, 0, flags, CMD_LANDSCAPE_CLEAR);

					// draw explosion animation...
					if ((x == sx || x == ex) && (y == sy || y == ey)) {
						// big explosion in each corner, or small explosion for single tiles
						CreateEffectVehicleAbove(x + 8, y + 8, 2,
								sy == ey && sx == ex ? EV_EXPLOSION_SMALL : EV_EXPLOSION_LARGE
								);
					}
				}
			}
		}

		return (success) ? cost : CMD_ERROR;
	}


	/* utility function used to modify a tile */
	static void ModifyTile(TileIndex tile, int flags, int ... args)
	{
		int i;
		int p = 0;

		if ((i = BitOps.GB(flags, 8, 4)) != 0) {
			tile.SetTileType( TileTypes.values[(i - 1)] );
		}

		if (flags & (MP_MAP2_CLEAR | MP_MAP2)) {
			int x = 0;
			if (flags & MP_MAP2) x = args[p++];
			tile.getMap().m2 = x;
		}

		if (flags & (MP_MAP3LO_CLEAR | MP_MAP3LO)) {
			int x = 0;
			if (flags & MP_MAP3LO) x = args[p++];
			tile.getMap().m3 = x;
		}

		if (flags & (MP_MAP3HI_CLEAR | MP_MAP3HI)) {
			int x = 0;
			if (flags & MP_MAP3HI) x = args[p++];
			tile.getMap().m4 = x;
		}

		if (flags & (MP_MAPOWNER|MP_MAPOWNER_CURRENT)) {
			PlayerID x = _current_player;
			if (flags & MP_MAPOWNER) x = args[p++];
			tile.getMap().m1 = x;
		}

		if (flags & MP_MAP5) {
			tile.getMap().m5 = args[p++];
		}


		if (!(flags & MP_NODIRTY))
			MarkTileDirtyByTile(tile);
	}

	/*
	#define TILELOOP_BITS 4
	#define TILELOOP_SIZE (1 << TILELOOP_BITS)
	#define TILELOOP_ASSERTMASK ((TILELOOP_SIZE-1) + ((TILELOOP_SIZE-1) << MapLogX()))
	#define TILELOOP_CHKMASK (((1 << (MapLogX() - TILELOOP_BITS))-1) << TILELOOP_BITS)
	 */
	static void RunTileLoop()
	{
		TileIndex tile;
		int count;

		tile = _cur_tileloop_tile;

		assert( (tile & ~TILELOOP_ASSERTMASK) == 0);
		count = (MapSizeX() / TILELOOP_SIZE) * (MapSizeY() / TILELOOP_SIZE);
		do {
			_tile_type_procs[GetTileType(tile)].tile_loop_proc(tile);

			if (TileX(tile) < MapSizeX() - TILELOOP_SIZE) {
				tile += TILELOOP_SIZE; /* no overflow */
			} else {
				tile = TILE_MASK(tile - TILELOOP_SIZE * (MapSizeX() / TILELOOP_SIZE - 1) + TileDiffXY(0, TILELOOP_SIZE)); /* x would overflow, also increase y */
			}
		} while (--count);
		assert( (tile & ~TILELOOP_ASSERTMASK) == 0);

		tile += 9;
		if (tile & TILELOOP_CHKMASK)
			tile = (tile + MapSizeX()) & TILELOOP_ASSERTMASK;
		_cur_tileloop_tile = tile;
	}

	static void InitializeLandscape()
	{
		int map_size;
		int i;

		map_size = Global.MapSize();
		for (i = 0; i < map_size; i++) {
			Global._m[i].type        = TileTypes.MP_CLEAR.ordinal();
			Global._m[i].height      = 0;
			Global._m[i].m1          = OWNER_NONE;
			Global._m[i].m2          = 0;
			Global._m[i].m3          = 0;
			Global._m[i].m4          = 0;
			Global._m[i].m5          = 3;
			Global._m[i].extra       = 0;
		}

		// create void tiles at the border
		for (i = 0; i < Global.MapMaxY(); ++i)
			SetTileType(i * Global.MapSizeX() + Global.MapMaxX(), TileTypes.MP_VOID);
		for (i = 0; i < Global.MapSizeX(); ++i)
			SetTileType(Global.MapSizeX() * Global.MapMaxY() + i, TileTypes.MP_VOID);
	}

	static void ConvertGroundTilesIntoWaterTiles()
	{
		IntContainer h;

		for (int ti = 0; ti < Global.MapSize(); ++ti) 
		{
			TileIndex tile = new TileIndex(ti);

			if (tile.IsTileType(TileTypes.MP_CLEAR) && tile.GetTileSlope(h) == 0 && h.v == 0) {
				tile.SetTileType(TileTypes.MP_WATER);
				tile.getMap().m5 = 0;
				tile.SetTileOwner(OWNER_WATER);
			}
		}
	}

	static final byte _genterrain_tbl_1[] = { 10, 22, 33, 37, 4 };
	static final byte _genterrain_tbl_2[] = { 0, 0, 0, 0, 33 };

	static void GenerateTerrain(int type, int flag)
	{
		int r;
		int x;
		int y;
		int w;
		int h;
		static final Sprite template;
		static final byte p[];
		Tile tile;
		byte direction;

		r = Random();
		template = GetSprite((((r >> 24) * _genterrain_tbl_1[type]) >> 8) + _genterrain_tbl_2[type] + 4845);

		x = r & Global.MapMaxX();
		y = (r >> Global.MapLogX()) & Global.MapMaxY();


		if (x < 2 || y < 2)
			return;

		direction = BitOps.GB(r, 22, 2);
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

		tile = new TileIndex(x, y).getMap();

		switch (direction) {
		case 0:
			do {
				Tile tile_cur = tile;
				int w_cur;

				for (w_cur = w; w_cur != 0; --w_cur) {
					if (*p >= tile_cur.type_height) tile_cur.type_height = *p;
					p++;
					tile_cur++;
				}
				tile += TileIndex.TileDiffXY(0, 1);
			} while (--h != 0);
			break;

		case 1:
			do {
				Tile tile_cur = tile;
				int h_cur;

				for (h_cur = h; h_cur != 0; --h_cur) {
					if (*p >= tile_cur.type_height) tile_cur.type_height = *p;
					p++;
					tile_cur += TileIndex.TileDiffXY(0, 1);
				}
				tile++;
			} while (--w != 0);
			break;

		case 2:
			tile += TileIndex.TileDiffXY(w - 1, 0);
			do {
				Tile tile_cur = tile;
				int w_cur;

				for (w_cur = w; w_cur != 0; --w_cur) {
					if (*p >= tile_cur.type_height) tile_cur.type_height = *p;
					p++;
					tile_cur--;
				}
				tile += TileIndex.TileDiffXY(0, 1);
			} while (--h != 0);
			break;

		case 3:
			tile += TileIndex.TileDiffXY(0, h - 1);
			do {
				Tile tile_cur = tile;
				int h_cur;

				for (h_cur = h; h_cur != 0; --h_cur) {
					if (*p >= tile_cur.type_height) tile_cur.type_height = *p;
					p++;
					tile_cur -= TileIndex.TileDiffXY(0, 1);
				}
				tile++;
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
			for( TileIndexDiffC data : _make_desert_or_rainforest_data)
			{
				TileIndex t = new TileIndex( TileIndex.TILE_MASK(ti + TileIndex.ToTileIndexDiff(data).diff) );
				if (t.TileHeight() >= 4 || t.IsTileType(TileTypes.MP_WATER)) 
				{
					broken = true;
					break;
				}
			}
			if( !broken )// (data == endof(_make_desert_or_rainforest_data))
				tile.SetMapExtraBits(1);
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
				if (t.IsTileType(TileTypes.MP_CLEAR) && (t.getMap().m5 & 0x1c) == 0x14) 
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

		if (Global._opt.landscape == LT_HILLY) {
			for (i = ScaleByMapSize((Random() & 0x7F) + 950); i != 0; --i)
				GenerateTerrain(2, 0);

			r = Random();
			flag = GB(r, 0, 2) | 4;
			for (i = ScaleByMapSize(GB(r, 16, 7) + 450); i != 0; --i)
				GenerateTerrain(4, flag);
		} else if (_opt.landscape == LT_DESERT) {
			for (i = ScaleByMapSize((Random()&0x7F) + 170); i != 0; --i)
				GenerateTerrain(0, 0);

			r = Random();
			flag = GB(r, 0, 2) | 4;
			for (i = ScaleByMapSize(GB(r, 16, 8) + 1700); i != 0; --i)
				GenerateTerrain(0, flag);

			flag ^= 2;

			for (i = ScaleByMapSize((Random() & 0x7F) + 410); i != 0; --i)
				GenerateTerrain(3, flag);
		} else {
			i = ScaleByMapSize((Random() & 0x7F) + (3 - _opt.diff.quantity_sea_lakes) * 256 + 100);
			for (; i != 0; --i)
				GenerateTerrain(_opt.diff.terrain_type, 0);
		}

		ConvertGroundTilesIntoWaterTiles();

		if (_opt.landscape == LT_DESERT)
			CreateDesertOrRainForest();
	}


	static void CallLandscapeTick()
	{
		OnTick_Town();
		OnTick_Trees();
		OnTick_Station();
		OnTick_Industry();

		OnTick_Players();
		OnTick_Train();
	}

	static TileIndex AdjustTileCoordRandomly(TileIndex a, byte rng)
	{
		int rn = rng;
		int r = Global.Random();

		return new TileIndex(
				a.TileX() + (BitOps.GB(r, 0, 8) * rn * 2 >> 8) - rn,
				a.TileY() + (BitOps.GB(r, 8, 8) * rn * 2 >> 8) - rn
				).TILE_MASK();
	}

	// TODO move 
	static boolean IsValidTile(TileIndex tile)
	{
		return (tile.getTile() < Global.MapSizeX() * Global.MapMaxY() && tile.TileX() != Global.MapMaxX());
	}



}
