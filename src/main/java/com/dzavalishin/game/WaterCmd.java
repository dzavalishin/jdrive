package com.dzavalishin.game;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.struct.Point;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.struct.TileIndexDiff;
import com.dzavalishin.struct.TileIndexDiffC;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.tables.WaterTables;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.ShipGui;
import com.dzavalishin.xui.VehicleGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

public class WaterCmd extends WaterTables
{


	final static /*SpriteID*/ int _water_shore_sprites[] = {
		0,
		Sprite.SPR_SHORE_TILEH_1,
		Sprite.SPR_SHORE_TILEH_2,
		Sprite.SPR_SHORE_TILEH_3,
		Sprite.SPR_SHORE_TILEH_4,
		0,
		Sprite.SPR_SHORE_TILEH_6,
		0,
		Sprite.SPR_SHORE_TILEH_8,
		Sprite.SPR_SHORE_TILEH_9,
		0,
		0,
		Sprite.SPR_SHORE_TILEH_12,
		0,
		0
	};


	//static void FloodVehicle(Vehicle v);

	static boolean IsClearWaterTile(TileIndex tile)
	{
		return
			tile.IsTileType( TileTypes.MP_WATER) &&
			tile.getMap().m5 == 0 &&
					tile.GetTileSlope(null) == 0;
	}

	/** Build a ship depot.
	 * @param x,y tile coordinates where ship depot is built
	 * @param p1 depot direction (0 == X or 1 == Y)
	 * @param p2 unused
	 */
	static int CmdBuildShipDepot(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile, tile2;

		int cost, ret;
		Depot depot;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (p1 > 1) return Cmd.CMD_ERROR;

		tile = TileIndex.TileVirtXY(x, y);
		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		tile2 = tile.iadd( (p1!=0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0)) );
		if (!tile2.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		if (!IsClearWaterTile(tile) || !IsClearWaterTile(tile2))
			return Cmd.return_cmd_error(Str.STR_3801_MUST_BE_BUILT_ON_WATER);

		ret = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
		ret = Cmd.DoCommandByTile(tile2, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;

		// pretend that we're not making land from the water even though we actually are.
		cost = 0;

		depot = Depot.AllocateDepot();
		if (depot == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC) ) {
			depot.xy = tile;
			Depot._last_built_ship_depot_tile = tile;
			depot.town_index = Town.ClosestTownFromTile(tile, -1).index;

			Landscape.ModifyTile(tile, TileTypes.MP_WATER,
				//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
				TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR,
				(0x80 + p1*2)
			);

			Landscape.ModifyTile(tile2, TileTypes.MP_WATER,
				//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
				TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR,
				(0x81 + p1*2)
			);
		}

		return (int) (cost + Global._price.build_ship_depot);
	}

	static int RemoveShipDepot(TileIndex tile, int flags)
	{
		TileIndex tile2;

		if (!Player.CheckTileOwnership(tile))
			return Cmd.CMD_ERROR;

		if (!tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		tile2 = tile.iadd(((tile.getMap().m5 & 2) != 0) ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0));

		if (!tile2.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC) ) {
			/* Kill the depot */
			Depot.DoDeleteDepot(tile);

			/* Make the tiles water */
			Landscape.ModifyTile(tile, TileTypes.MP_WATER, 
					//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
					TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0);
			Landscape.ModifyTile(tile2, TileTypes.MP_WATER, 
					//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
					TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0);
		}

		return (int) Global._price.remove_ship_depot;
	}

	// build a shiplift
	static int DoBuildShiplift(TileIndex tile, int dir, int flags)
	{
		int ret;
		int delta;

		// middle tile
		ret = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;

		delta = TileIndex.TileOffsByDir(dir).diff;
		// lower tile
		ret = Cmd.DoCommandByTile(tile.isub(delta), 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
		if (tile.isub(delta).GetTileSlope(null) != 0) return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);

		// upper tile
		ret = Cmd.DoCommandByTile(tile.iadd(delta), 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
		if (tile.iadd(delta).GetTileSlope(null) != 0) return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);

		if(0 != (flags & Cmd.DC_EXEC) ) {
			Landscape.ModifyTile(tile, TileTypes.MP_WATER,
					//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
					TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0x10 + dir);
			Landscape.ModifyTile(tile.isub(delta), TileTypes.MP_WATER,
					//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
					TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0x14 + dir);
			Landscape.ModifyTile(tile.iadd(delta), TileTypes.MP_WATER, 
					//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
					TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0x18 + dir);
		}

		return (int) (Global._price.clear_water * 22 / 8);
	}

	static int RemoveShiplift(TileIndex tile, int flags)
	{
		TileIndexDiff delta = TileIndex.TileOffsByDir(BitOps.GB(tile.getMap().m5, 0, 2));

		// make sure no vehicle is on the tile.
		if (!tile.EnsureNoVehicle() || !tile.iadd(delta).EnsureNoVehicle() || !tile.isub(delta).EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC) ) {
			Landscape.DoClearSquare(tile);
			Landscape.DoClearSquare(tile.iadd(delta));
			Landscape.DoClearSquare(tile.isub(delta));
		}

		return (int) (Global._price.clear_water * 2);
	}

	static void MarkTilesAroundDirty(TileIndex tile)
	{
		TileIndex.TILE_ADDXY(tile, 0, 1).MarkTileDirtyByTile();
		TileIndex.TILE_ADDXY(tile, 0, -1).MarkTileDirtyByTile();
		TileIndex.TILE_ADDXY(tile, 1, 0).MarkTileDirtyByTile();
		TileIndex.TILE_ADDXY(tile, -1, 0).MarkTileDirtyByTile();
	}

	static final byte _shiplift_dirs[] = {0, 0, 0, 2, 0, 0, 1, 0, 0, 3, 0, 0, 0};

	/** Builds a lock (ship-lift)
	 * @param x,y tile coordinates where to place the lock
	 * @param p1 unused
	 * @param p2 unused
	 */
	static int CmdBuildLock(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		int tileh;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);
		tileh = tile.GetTileSlope(null);

		if (tileh == 3 || tileh == 6 || tileh == 9 || tileh == 12) {
			return DoBuildShiplift(tile, _shiplift_dirs[tileh], flags);
		}

		return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
	}

	/** Build a piece of canal.
	 * @param x,y end tile of stretch-dragging
	 * @param p1 start tile of stretch-dragging
	 * @param p2 unused
	 */
	static int CmdBuildCanal(int x, int y, int flags, int p1, int p2)
	{
		int size_x, size_y;
		int sx, sy;

		if (p1 > Global.MapSize()) return Cmd.CMD_ERROR;

		TileIndex pp1 = new TileIndex(p1);
		sx = pp1.TileX();
		sy = pp1.TileY();
		/* x,y are in pixel-coordinates, transform to tile-coordinates
		 * to be able to use the BEGIN_TILE_LOOP() macro */
		x >>= 4; y >>= 4;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (x < sx) { int t = sx; sx = x; x = t; } // intswap(x, sx);
		if (y < sy) { int t = sy; sy = y; y = t; } // intswap(y, sy);
		size_x = (x - sx) + 1;
		size_y = (y - sy) + 1;

		/* Outside the editor you can only drag canals, and not areas */
		if (Global._game_mode != GameModes.GM_EDITOR && (sx != x && sy != y)) return Cmd.CMD_ERROR;

		int [] cost = {0};
		int [] err = {0};
		//BEGIN_TILE_LOOP(tile, size_x, size_y, TileXY(sx, sy)) 
		TileIndex.forAll( size_x, size_y, TileIndex.TileXY(sx, sy), (tile) ->
		{
			int ret = 0;
			if (tile.GetTileSlope(null) != 0)
			{
				err[0] = Cmd.return_cmd_error(Str.STR_0007_FLAT_LAND_REQUIRED);
				return true;
			}

				// can't make water of water!
				if (tile.IsTileType( TileTypes.MP_WATER)) {
					Global._error_message = Str.STR_1007_ALREADY_BUILT;
				} else {
					/* is middle piece of a bridge? */
					if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && 0 != (tile.getMap().m5 & 0x40) ) { /* build under bridge */
						if(0 != (tile.getMap().m5 & 0x20)) // transport route under bridge
						{
							err[0] = Cmd.return_cmd_error(Str.STR_5800_OBJECT_IN_THE_WAY);
							return true;
						}

						if(0 != (tile.getMap().m5 & 0x18)) // already water under bridge
						{
							err[0] = Cmd.return_cmd_error(Str.STR_1007_ALREADY_BUILT);
							return true;
						}
					/* no bridge? then try to clear it. */
					} else
						ret = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);

					if (Cmd.CmdFailed(ret))
					{
						err[0] = Cmd.CMD_ERROR;
						return true;
					}
					cost[0] += ret;

					/* execute modifications */
					if(0 != (flags & Cmd.DC_EXEC) ) {
						if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)) {
							// change owner to Owner.OWNER_WATER and set land under bridge bit to water
							Landscape.ModifyTile(tile, TileTypes.MP_NOCHANGE,  
									TileTypes.MP_MAP5 | TileTypes.MP_MAPOWNER, Owner.OWNER_WATER, tile.getMap().m5 | 0x08);
						} else {
							Landscape.ModifyTile(tile, TileTypes.MP_WATER,
									//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
									TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0);
						}
						// mark the tiles around dirty too
						MarkTilesAroundDirty(tile);
					}

					cost[0] += Global._price.clear_water;
					
					return false;
				}
			return false;
		}); // END_TILE_LOOP(tile, size_x, size_y, 0);

		if( err[0] != 0  ) return err[0];
		
		return (cost[0] == 0) ? Cmd.CMD_ERROR : cost[0];
	}

	
	static final TileIndexDiffC _shiplift_tomiddle_offs[] = {
			new TileIndexDiffC( 0,  0), new TileIndexDiffC(0,  0), new TileIndexDiffC( 0, 0), new TileIndexDiffC(0,  0), // middle
			new TileIndexDiffC(-1,  0), new TileIndexDiffC(0,  1), new TileIndexDiffC( 1, 0), new TileIndexDiffC(0, -1), // lower
			new TileIndexDiffC( 1,  0), new TileIndexDiffC(0, -1), new TileIndexDiffC(-1, 0), new TileIndexDiffC(0,  1), // upper
		};
	
	static int ClearTile_Water(TileIndex tile, byte flags)
	{
		int m5 = tile.getMap().m5;
		int slope;

		if (m5 <= 1) { // water and shore
			// Allow building on water? It's ok to build on shores.
			if ((0 !=(flags & Cmd.DC_NO_WATER)) && m5 != 1)
				return Cmd.return_cmd_error(Str.STR_3807_CAN_T_BUILD_ON_WATER);

			// Make sure no vehicle is on the tile
			if (!tile.EnsureNoVehicle())
				return Cmd.CMD_ERROR;

			// Make sure it's not an edge tile.
			if( !(BitOps.IS_INT_INSIDE(tile.TileX(), 1, Global.MapMaxX() - 1) &&
					BitOps.IS_INT_INSIDE(tile.TileY(), 1, Global.MapMaxY() - 1)) )
				return Cmd.return_cmd_error(Str.STR_0002_TOO_CLOSE_TO_EDGE_OF_MAP);

			if (m5 == 0) {
				if(0 != (flags & Cmd.DC_EXEC) )
					Landscape.DoClearSquare(tile);
				return (int) Global._price.clear_water;
			} else if (m5 == 1) {
				slope = tile.GetTileSlope(null);
				if (slope == 8 || slope == 4 || slope == 2 || slope == 1) {
					if(0 != (flags & Cmd.DC_EXEC) )
						Landscape.DoClearSquare(tile);
					return (int) Global._price.clear_water;
				}
				if(0 != (flags & Cmd.DC_EXEC) )
					Landscape.DoClearSquare(tile);
				return (int) Global._price.purchase_land;
			} else
				return Cmd.CMD_ERROR;
		} else if ((m5 & 0x10) == 0x10) {
			// shiplift


			if(0 != (flags & Cmd.DC_AUTO) )return Cmd.return_cmd_error(Str.STR_2004_BUILDING_MUST_BE_DEMOLISHED);
			// don't allow water to delete it.
			if (PlayerID.getCurrent().isWater()) return Cmd.CMD_ERROR;
			// move to the middle tile..
			return RemoveShiplift(tile.iadd( TileIndex.ToTileIndexDiff(_shiplift_tomiddle_offs[m5 & 0xF])), flags) ;
		} else {
			// ship depot
			if(0 != (flags & Cmd.DC_AUTO) )return Cmd.return_cmd_error(Str.STR_2004_BUILDING_MUST_BE_DEMOLISHED);

			switch (m5) {
				case 0x80: break;
				case 0x81: tile = tile.isub(TileIndex.TileDiffXY(1, 0)); break;
				case 0x82: break;
				case 0x83: tile = tile.isub(TileIndex.TileDiffXY(0, 1)); break;
				default:   return Cmd.CMD_ERROR;
			}

			return RemoveShipDepot(tile,flags);
		}
	}

	// return true if a tile is a water tile.
	static boolean IsWateredTile(TileIndex tile)
	{
		int m5 = tile.getMap().m5;

		switch (tile.GetTileType()) {
			case MP_WATER:
				// true, if not coast/riverbank
				return m5 != 1;

			case MP_STATION:
				// returns true if it is a dock-station
				// m5 inside values is m5 < 75 all stations, 83 <= m5 <= 114 new airports
				return !(m5 < 75 || (m5 >= 83 && m5 <= 114));

			case MP_TUNNELBRIDGE:
				// true, if tile is middle part of bridge with water underneath
				return (m5 & 0xF8) == 0xC8;

			default:
				return false;
		}
	}

	static // draw a canal styled water tile with dikes around
	void DrawCanalWater(TileIndex tile)
	{
		int wa;

		// determine the edges around with water.
		wa = IsWateredTile(TileIndex.TILE_ADDXY(tile, -1, 0)) ? 1  << 0 : 0;
		wa += IsWateredTile(TileIndex.TILE_ADDXY(tile, 0, 1)) ? 1  << 1 : 0;
		wa += IsWateredTile(TileIndex.TILE_ADDXY(tile, 1, 0)) ? 1  << 2 : 0;
		wa += IsWateredTile(TileIndex.TILE_ADDXY(tile, 0, -1)) ? 1 << 3 : 0;

		if (0==(wa & 1)) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57);
		if (0==(wa & 2)) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 58);
		if (0==(wa & 4)) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 59);
		if (0==(wa & 8)) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 60);

		// right corner
		if ((wa & 3) == 0) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 4);
		else if ((wa & 3) == 3 && !IsWateredTile(TileIndex.TILE_ADDXY(tile, -1, 1))) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 8);

		// bottom corner
		if ((wa & 6) == 0) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 5);
		else if ((wa & 6) == 6 && !IsWateredTile(TileIndex.TILE_ADDXY(tile, 1, 1))) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 9);

		// left corner
		if ((wa & 12) == 0) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 6);
		else if ((wa & 12) == 12 && !IsWateredTile(TileIndex.TILE_ADDXY(tile, 1, -1))) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 10);

		// upper corner
		if ((wa & 9) == 0) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 7);
		else if ((wa & 9) == 9 && !IsWateredTile(TileIndex.TILE_ADDXY(tile, -1, -1))) ViewPort.DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 11);
	}

	static class LocksDrawTileStruct {
		int delta_x, delta_y, delta_z;
		int width, height, depth;
		//SpriteID 
		int image;
	} 



	static void DrawWaterStuff(final TileInfo ti, final WaterDrawTileStruct[] wdtsa,
		int palette, int base
	)
	{
		int image;

		int i = 0;
		
		ViewPort.DrawGroundSprite(wdtsa[i++].image);

		for (; ; ) 
		{
			WaterDrawTileStruct wdts = wdtsa[i++];
			
			if(wdts.delta_x == 0x80)
				break;
			
			image =	wdts.image + base;
			if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) {
				image = Sprite.RET_MAKE_TRANSPARENT(image);
			} else {
				image |= palette;
			}
			ViewPort.AddSortableSpriteToDraw(image, ti.x + wdts.delta_x, ti.y + wdts.delta_y, wdts.width, wdts.height, wdts.unk, ti.z + wdts.delta_z);
		}
	}

	static void DrawTile_Water(TileInfo ti)
	{
		// draw water tile
		if (ti.map5 == 0) {
			ViewPort.DrawGroundSprite(Sprite.SPR_FLAT_WATER_TILE);
			if (ti.z != 0) WaterCmd.DrawCanalWater(ti.tile);
			return;
		}

		// draw shore
		if (ti.map5 == 1) {
			assert(ti.tileh < 16);
			ViewPort.DrawGroundSprite(_water_shore_sprites[ti.tileh]);
			return;
		}

		// draw shiplift
		if ((ti.map5 & 0xF0) == 0x10) {
			final WaterDrawTileStruct[] t = _shiplift_display_seq[ti.map5 & 0xF];
			DrawWaterStuff(ti, t, 0, ti.z > t[3].delta_y ? 24 : 0);
			return;
		}

		// [dz] added array size check, overflows
		final int nSeq = ti.map5 & 0x7F;
		if( nSeq < _shipdepot_display_seq.length)
			DrawWaterStuff(ti, _shipdepot_display_seq[nSeq], Sprite.PLAYER_SPRITE_COLOR(ti.tile.GetTileOwner()), 0);
	}

	public static void DrawShipDepotSprite(int x, int y, int image)
	{
		final WaterDrawTileStruct[] wdtsa = _shipdepot_display_seq[image];

		int i = 0;
		Gfx.DrawSprite(wdtsa[i++].image, x, y);

		for (;;) 
		{
			final WaterDrawTileStruct wdts = wdtsa[i++];
			
			if(wdts.delta_x == 0x80) break;
			
			Point pt = Point.RemapCoords(wdts.delta_x, wdts.delta_y, wdts.delta_z);
			Gfx.DrawSprite(wdts.image + Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player), x + pt.x, y + pt.y);
		}
	}


	static int GetSlopeZ_Water(final TileInfo  ti)
	{
		return Landscape.GetPartialZ(ti.x & 0xF, ti.y & 0xF, ti.tileh) + ti.z;
	}

	static int GetSlopeTileh_Water(final TileInfo ti)
	{
		return ti.tileh;
	}

	static AcceptedCargo GetAcceptedCargo_Water(TileIndex tile)
	{
		return new AcceptedCargo();
		/* not used */
	}

	static TileDesc GetTileDesc_Water(TileIndex tile)
	{
		TileDesc td = new TileDesc();
		
		if (tile.getMap().m5 == 0 && tile.TilePixelHeight() == 0)
			td.str = Str.STR_3804_WATER;
		else if (tile.getMap().m5 == 0)
			td.str = Str.STR_LANDINFO_CANAL;
		else if (tile.getMap().m5 == 1)
			td.str = Str.STR_3805_COAST_OR_RIVERBANK;
		else if ((tile.getMap().m5&0xF0) == 0x10)
			td.str = Str.STR_LANDINFO_LOCK;
		else
			td.str = Str.STR_3806_SHIP_DEPOT;

		td.owner = tile.GetTileOwner().id;
		
		return td;
	}

	static void AnimateTile_Water(TileIndex tile)
	{
		/* not used */
	}

	static void TileLoopWaterHelper(TileIndex tile, final TileIndexDiffC[] offs)
	{
		TileIndex target = TileIndex.TILE_ADD(tile, TileIndex.ToTileIndexDiff(offs[0]));

		// type of this tile mustn't be water already.
		if (target.IsTileType(TileTypes.MP_WATER)) return;

		if (TileIndex.TILE_ADD(tile, TileIndex.ToTileIndexDiff(offs[1])).TileHeight() != 0 ||
				TileIndex.TILE_ADD(tile, TileIndex.ToTileIndexDiff(offs[2])).TileHeight() != 0) {
			return;
		}

		if (TileIndex.TILE_ADD(tile, TileIndex.ToTileIndexDiff(offs[3])).TileHeight() != 0 ||
				TileIndex.TILE_ADD(tile, TileIndex.ToTileIndexDiff(offs[4])).TileHeight() != 0) {
			// make coast..
			switch (target.GetTileType()) {
				case MP_RAILWAY: {
					int slope = target.GetTileSlope(null);
					int tracks = BitOps.GB(target.getMap().m5, 0, 6);
					if (!(
							(slope == 1 && tracks == 0x20) ||
							(slope == 2 && tracks == 0x04) ||
							(slope == 4 && tracks == 0x10) ||
							(slope == 8 && tracks == 0x08)))
						break;
				}
				/* FALLTHROUGH */

				case MP_CLEAR:
				case MP_TREES:
					PlayerID.setCurrent( PlayerID.getWater() );
					if (!Cmd.CmdFailed(Cmd.DoCommandByTile(target, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR))) {
						Landscape.ModifyTile(
							target, TileTypes.MP_WATER,
							//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
							TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR |
								TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR,
							Owner.OWNER_WATER, 1
						);
					}
					break;

				case MP_TUNNELBRIDGE:
					// Middle part of bridge with clear land below?
					if ((target.getMap().m5 & 0xF8) == 0xC0) {
						target.getMap().m5 |= 0x08;
						tile.MarkTileDirtyByTile();
					}
					break;

				default:
					break;
			}
		} else {
			if (target.IsTileType(TileTypes.MP_TUNNELBRIDGE)) {
				int m5 = target.getMap().m5;
				if ((m5 & 0xF8) == 0xC8 || (m5 & 0xF8) == 0xF0)
					return;

				if ((m5 & 0xC0) == 0xC0) {
					Landscape.ModifyTile(target, TileTypes.MP_NOCHANGE, TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5, Owner.OWNER_WATER, (m5 & ~0x38) | 0x8);
					return;
				}
			}

			PlayerID.setCurrent( PlayerID.getWater() );
			{
				Vehicle v = Vehicle.FindVehicleOnTileZ(target, 0);
				if (v != null) FloodVehicle(v);
			}

			if (!Cmd.CmdFailed(Cmd.DoCommandByTile(target, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR))) {
				Landscape.ModifyTile(
					target, TileTypes.MP_WATER,
					//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
					TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR |
						TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR,
					Owner.OWNER_WATER,
					0
				);
			}
		}
	}

	static void FloodVehicle(Vehicle v)
	{
		if (!v.isCrashed()) {
			int[] pass = {0};

			if (v.type == Vehicle.VEH_Road) {	// flood bus/truck
				pass[0] = 1;	// driver
				if (v.getCargo_type() == AcceptedCargo.CT_PASSENGERS)
					pass[0] += v.cargo_count;

				v.setCrashed(true);
				v.road.crashed_ctr = 2000;	// max 2220, disappear pretty fast
				VehicleGui.RebuildVehicleLists();
			} else if (v.type == Vehicle.VEH_Train) {
				//Vehicle  u;

				v = v.GetFirstVehicleInChain();
				//u = v;
				if (v.IsFrontEngine()) pass[0] = 4; // driver

				// crash all wagons, and count passangers
				v.forEachWagon( (vw) -> 
				{
					if (vw.getCargo_type() == AcceptedCargo.CT_PASSENGERS) pass[0] += vw.cargo_count;
					vw.setCrashed(true);
				});

				//v = u;
				v.rail.crash_anim_pos = 4000; // max 4440, disappear pretty fast
				VehicleGui.RebuildVehicleLists();
			} else {
				return;
			}

			Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, AirCraft.STATUS_BAR);
			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);

			Global.SetDParam(0, pass[0]);
			NewsItem.AddNewsItem(Str.STR_B006_FLOOD_VEHICLE_DESTROYED,
					NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ACCIDENT, 0),
				v.index,
				0);
			v.CreateEffectVehicleRel(4, 4, 8, Vehicle.EV_EXPLOSION_LARGE);
			v.SndPlayVehicleFx(Snd.SND_12_EXPLOSION);
		}
	}

	private static final TileIndexDiffC _tile_loop_offs_array[][] = {
			// tile to mod																shore?				shore?
			{new TileIndexDiffC(-1,  0), new TileIndexDiffC(0, 0), new TileIndexDiffC(0, 1), new TileIndexDiffC(-1,  0), new TileIndexDiffC(-1,  1)},
			{new TileIndexDiffC( 0,  1), new TileIndexDiffC(0, 1), new TileIndexDiffC(1, 1), new TileIndexDiffC( 0,  2), new TileIndexDiffC( 1,  2)},
			{new TileIndexDiffC( 1,  0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 1), new TileIndexDiffC( 2,  0), new TileIndexDiffC( 2,  1)},
			{new TileIndexDiffC( 0, -1), new TileIndexDiffC(0, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC( 0, -1), new TileIndexDiffC( 1, -1)}
		};
	
	// called from tunnelbridge_cmd
	static void TileLoop_Water(TileIndex tile)
	{
		int i;

		if (BitOps.IS_INT_INSIDE(tile.TileX(), 1, Global.MapSizeX() - 3 + 1) &&
				BitOps.IS_INT_INSIDE(tile.TileY(), 1, Global.MapSizeY() - 3 + 1)) {
			for (i = 0; i != _tile_loop_offs_array.length; i++) {
				TileLoopWaterHelper(tile, _tile_loop_offs_array[i]);
			}
		}
		// Global.gs._current_player can be changed by TileLoopWaterHelper.. reset it back
		//   here
		PlayerID.setCurrentToNone();

		// edges
		if (tile.TileX() == 0 && BitOps.IS_INT_INSIDE(tile.TileY(), 1, Global.MapSizeY() - 3 + 1)) //NE
			TileLoopWaterHelper(tile, _tile_loop_offs_array[2]);

		if (tile.TileX() == Global.MapSizeX() - 2 && BitOps.IS_INT_INSIDE(tile.TileY(), 1, Global.MapSizeY() - 3 + 1)) { //SW
			TileLoopWaterHelper(tile, _tile_loop_offs_array[0]);
		}

		if (tile.TileY() == 0 && BitOps.IS_INT_INSIDE(tile.TileX(), 1, Global.MapSizeX() - 3 + 1)) { //NW
			TileLoopWaterHelper(tile, _tile_loop_offs_array[1]);
		}

		if (tile.TileY() == Global.MapSizeY() - 2 && BitOps.IS_INT_INSIDE(tile.TileX(), 1, Global.MapSizeX() - 3 + 1)) { //SE
			TileLoopWaterHelper(tile, _tile_loop_offs_array[3]);
		}
	}


	private static final byte _coast_tracks[] = {0, 32, 4, 0, 16, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0};
	private static final byte _shipdepot_tracks[] = {1,1,2,2};
	private static final byte _shiplift_tracks[] = {1,2,1,2,1,2,1,2,1,2,1,2};

	static int GetTileTrackStatus_Water(TileIndex tile, /*int*/ TransportType mode)
	{
		int m5;
		int b;

		if (mode != TransportType.Water)
			return 0;

		m5 = tile.getMap().m5;
		if (m5 == 0)
			return 0x3F3F;

		if (m5 == 1) {
			b = _coast_tracks[tile.GetTileSlope(null)&0xF];
			return b + (b<<8);
		}

		if ( (m5 & 0x10) == 0x10) {
			//
			b = _shiplift_tracks[m5 & 0xF];
			return b + (b<<8);
		}

		if (0==(m5 & 0x80))
			return 0;

		b = _shipdepot_tracks[m5 & 0x7F];
		return b + (b<<8);
	}

	//extern void ShowShipDepotWindow(TileIndex tile);

	static void ClickTile_Water(TileIndex tile)
	{
		int m5 = 0xFF & (tile.getMap().m5 - 0x80);

		if (BitOps.IS_INT_INSIDE(m5, 0, 3+1)) {
			if(0 != (m5 & 1))
				tile = tile.iadd( (m5 == 1) ? TileIndex.TileDiffXY(-1, 0) : TileIndex.TileDiffXY(0, -1) );
			ShipGui.ShowShipDepotWindow(tile);
		}
	}

	static void ChangeTileOwner_Water(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		if (!tile.IsTileOwner( old_player)) return;

		if (new_player.id != Owner.OWNER_SPECTATOR) {
			tile.SetTileOwner( new_player);
		} else {
			Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
		}
	}

	static int VehicleEnter_Water(Vehicle v, TileIndex tile, int x, int y)
	{
		return 0;
	}

	static void InitializeDock()
	{
		Depot._last_built_ship_depot_tile = null;
	}

	final static TileTypeProcs _tile_type_water_procs = new TileTypeProcs(
		WaterCmd::DrawTile_Water,						/* draw_tile_proc */
		WaterCmd::GetSlopeZ_Water,					/* get_slope_z_proc */
		WaterCmd::ClearTile_Water,					/* clear_tile_proc */
		WaterCmd::GetAcceptedCargo_Water,		/* get_accepted_cargo_proc */
		WaterCmd::GetTileDesc_Water,				/* get_tile_desc_proc */
		WaterCmd::GetTileTrackStatus_Water,	/* get_tile_track_status_proc */
		WaterCmd::ClickTile_Water,					/* click_tile_proc */
		WaterCmd::AnimateTile_Water,				/* animate_tile_proc */
		WaterCmd::TileLoop_Water,						/* tile_loop_clear */
		WaterCmd::ChangeTileOwner_Water,		/* change_tile_owner_clear */
		null,											/* get_produced_cargo_proc */
		WaterCmd::VehicleEnter_Water,				/* vehicle_enter_tile_proc */
		null,											/* vehicle_leave_tile_proc */
		WaterCmd::GetSlopeTileh_Water			/* get_slope_tileh_proc */
	);
	
	
}
