package game;

public class WaterCmd 
{


	final /*SpriteID*/ int _water_shore_sprites[] = {
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
	int CmdBuildShipDepot(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile, tile2;

		int cost, ret;
		Depot depot;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (p1 > 1) return Cmd.CMD_ERROR;

		tile = TileIndex.TileVirtXY(x, y);
		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		tile2 = tile + (p1 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0));
		if (!EnsureNoVehicle(tile2)) return Cmd.CMD_ERROR;

		if (!IsClearWaterTile(tile) || !IsClearWaterTile(tile2))
			return Cmd.return_cmd_error(Str.STR_3801_MUST_BE_BUILandscape.LT_ON_WATER);

		ret = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
		ret = Cmd.DoCommandByTile(tile2, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;

		// pretend that we're not making land from the water even though we actually are.
		cost = 0;

		depot = AllocateDepot();
		if (depot == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC) ) {
			depot.xy = tile;
			_last_built_ship_depot_tile = tile;
			depot.town_index = ClosestTownFromTile(tile, (int)-1).index;

			ModifyTile(tile,
				TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | TileTypes.MP_MAPOwner.OWNER_CURRENT | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR,
				(0x80 + p1*2)
			);

			ModifyTile(tile2,
				TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | TileTypes.MP_MAPOwner.OWNER_CURRENT | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR,
				(0x81 + p1*2)
			);
		}

		return cost + Global._price.build_ship_depot;
	}

	static int RemoveShipDepot(TileIndex tile, int flags)
	{
		TileIndex tile2;

		if (!Player.CheckTileOwnership(tile))
			return Cmd.CMD_ERROR;

		if (!tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		tile2 = tile + ((tile.getMap().m5 & 2) ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0));

		if (!EnsureNoVehicle(tile2))
			return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC) ) {
			/* Kill the depot */
			DoDeleteDepot(tile);

			/* Make the tiles water */
			ModifyTile(tile, TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0);
			ModifyTile(tile2, TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0);
		}

		return Global._price.remove_ship_depot;
	}

	// build a shiplift
	static int DoBuildShiplift(TileIndex tile, int dir, int flags)
	{
		int ret;
		int delta;

		// middle tile
		ret = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;

		delta = TileOffsByDir(dir);
		// lower tile
		ret = Cmd.DoCommandByTile(tile - delta, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
		if (GetTileSlope(tile - delta, null)) return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);

		// upper tile
		ret = Cmd.DoCommandByTile(tile + delta, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
		if (GetTileSlope(tile + delta, null)) return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);

		if(0 != (flags & Cmd.DC_EXEC) ) {
			ModifyTile(tile, TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0x10 + dir);
			ModifyTile(tile - delta, TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0x14 + dir);
			ModifyTile(tile + delta, TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0x18 + dir);
		}

		return Global._price.clear_water * 22 >> 3;
	}

	static int RemoveShiplift(TileIndex tile, int flags)
	{
		TileIndexDiff delta = TileOffsByDir(BitOps.GB(tile.getMap().m5, 0, 2));

		// make sure no vehicle is on the tile.
		if (!tile.EnsureNoVehicle() || !EnsureNoVehicle(tile + delta) || !EnsureNoVehicle(tile - delta))
			return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC) ) {
			DoClearSquare(tile);
			DoClearSquare(tile + delta);
			DoClearSquare(tile - delta);
		}

		return Global._price.clear_water * 2;
	}

	static void MarkTilesAroundDirty(TileIndex tile)
	{
		MarkTileDirtyByTile(TILE_ADDXY(tile, 0, 1));
		MarkTileDirtyByTile(TILE_ADDXY(tile, 0, -1));
		MarkTileDirtyByTile(TILE_ADDXY(tile, 1, 0));
		MarkTileDirtyByTile(TILE_ADDXY(tile, -1, 0));
	}

	static final byte _shiplift_dirs[] = {0, 0, 0, 2, 0, 0, 1, 0, 0, 3, 0, 0, 0};

	/** Builds a lock (ship-lift)
	 * @param x,y tile coordinates where to place the lock
	 * @param p1 unused
	 * @param p2 unused
	 */
	int CmdBuildLock(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		int tileh;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);
		tileh = GetTileSlope(tile, null);

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
	int CmdBuildCanal(int x, int y, int flags, int p1, int p2)
	{
		int ret, cost;
		int size_x, size_y;
		int sx, sy;

		if (p1 > MapSize()) return Cmd.CMD_ERROR;

		sx = TileX(p1);
		sy = TileY(p1);
		/* x,y are in pixel-coordinates, transform to tile-coordinates
		 * to be able to use the BEGIN_TILE_LOOP() macro */
		x >>= 4; y >>= 4;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (x < sx) intswap(x, sx);
		if (y < sy) intswap(y, sy);
		size_x = (x - sx) + 1;
		size_y = (y - sy) + 1;

		/* Outside the editor you can only drag canals, and not areas */
		if (Global._game_mode != GameModes.GM_EDITOR && (sx != x && sy != y)) return Cmd.CMD_ERROR;

		cost = 0;
		BEGIN_TILE_LOOP(tile, size_x, size_y, TileXY(sx, sy)) {
			ret = 0;
			if (GetTileSlope(tile, null) != 0) return Cmd.return_cmd_error(Str.STR_0007_FLAT_LAND_REQUIRED);

				// can't make water of water!
				if (tile.IsTileType( TileTypes.MP_WATER)) {
					Global._error_message = Str.STR_1007_ALREADY_BUILT;
				} else {
					/* is middle piece of a bridge? */
					if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && tile.getMap().m5 & 0x40) { /* build under bridge */
						if (tile.getMap().m5 & 0x20) // transport route under bridge
							return Cmd.return_cmd_error(Str.STR_5800_OBJEAcceptedCargo.CT_IN_THE_WAY);

						if (tile.getMap().m5 & 0x18) // already water under bridge
							return Cmd.return_cmd_error(Str.STR_1007_ALREADY_BUILT);
					/* no bridge? then try to clear it. */
					} else
						ret = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);

					if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
					cost += ret;

					/* execute modifications */
					if(0 != (flags & Cmd.DC_EXEC) ) {
						if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)) {
							// change owner to Owner.OWNER_WATER and set land under bridge bit to water
							ModifyTile(tile, TileTypes.MP_MAP5 | TileTypes.MP_MAPOWNER, Owner.OWNER_WATER, tile.getMap().m5 | 0x08);
						} else {
							ModifyTile(tile, TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0);
						}
						// mark the tiles around dirty too
						MarkTilesAroundDirty(tile);
					}

					cost += Global._price.clear_water;
				}
		} END_TILE_LOOP(tile, size_x, size_y, 0);

		return (cost == 0) ? Cmd.CMD_ERROR : cost;
	}

	
	static final TileIndexDiffC _shiplift_tomiddle_offs[] = {
			{ 0,  0}, {0,  0}, { 0, 0}, {0,  0}, // middle
			{-1,  0}, {0,  1}, { 1, 0}, {0, -1}, // lower
			{ 1,  0}, {0, -1}, {-1, 0}, {0,  1}, // upper
		};
	
	static int ClearTile_Water(TileIndex tile, byte flags)
	{
		byte m5 = tile.getMap().m5;
		int slope;

		if (m5 <= 1) { // water and shore
			// Allow building on water? It's ok to build on shores.
			if (flags & Cmd.DC_NO_WATER && m5 != 1)
				return Cmd.return_cmd_error(Str.STR_3807_CAN_T_BUILD_ON_WATER);

			// Make sure no vehicle is on the tile
			if (!tile.EnsureNoVehicle())
				return Cmd.CMD_ERROR;

			// Make sure it's not an edge tile.
			if (!(BitOps.IS_INT_INSIDE(tile.TileX(), 1, MapMaxX() - 1) &&
					BitOps.IS_INT_INSIDE(tile.TileY(), 1, MapMaxY() - 1)))
				return Cmd.return_cmd_error(Str.STR_0002_TOO_CLOSE_TO_EDGE_OF_MAP);

			if (m5 == 0) {
				if (flags & Cmd.DC_EXEC)
					DoClearSquare(tile);
				return Global._price.clear_water;
			} else if (m5 == 1) {
				slope = GetTileSlope(tile,null);
				if (slope == 8 || slope == 4 || slope == 2 || slope == 1) {
					if (flags & Cmd.DC_EXEC)
						DoClearSquare(tile);
					return Global._price.clear_water;
				}
				if (flags & Cmd.DC_EXEC)
					DoClearSquare(tile);
				return Global._price.purchase_land;
			} else
				return Cmd.CMD_ERROR;
		} else if ((m5 & 0x10) == 0x10) {
			// shiplift


			if(0 != (flags & Cmd.DC_AUTO) )return Cmd.return_cmd_error(Str.STR_2004_BUILDING_MUST_BE_DEMOLISHED);
			// don't allow water to delete it.
			if (Global._current_player.id == Owner.OWNER_WATER) return Cmd.CMD_ERROR;
			// move to the middle tile..
			return RemoveShiplift(tile + ToTileIndexDiff(_shiplift_tomiddle_offs[m5 & 0xF]), flags);
		} else {
			// ship depot
			if(0 != (flags & Cmd.DC_AUTO) )return Cmd.return_cmd_error(Str.STR_2004_BUILDING_MUST_BE_DEMOLISHED);

			switch (m5) {
				case 0x80: break;
				case 0x81: tile -= TileIndex.TileDiffXY(1, 0); break;
				case 0x82: break;
				case 0x83: tile -= TileIndex.TileDiffXY(0, 1); break;
				default:   return Cmd.CMD_ERROR;
			}

			return RemoveShipDepot(tile,flags);
		}
	}

	// return true if a tile is a water tile.
	static boolean IsWateredTile(TileIndex tile)
	{
		byte m5 = tile.getMap().m5;

		switch (tile.GetTileType()) {
			case TileTypes.MP_WATER:
				// true, if not coast/riverbank
				return m5 != 1;

			case TileTypes.MP_STATION:
				// returns true if it is a dock-station
				// m5 inside values is m5 < 75 all stations, 83 <= m5 <= 114 new airports
				return !(m5 < 75 || (m5 >= 83 && m5 <= 114));

			case TileTypes.MP_TUNNELBRIDGE:
				// true, if tile is middle part of bridge with water underneath
				return (m5 & 0xF8) == 0xC8;

			default:
				return false;
		}
	}

	// draw a canal styled water tile with dikes around
	void DrawCanalWater(TileIndex tile)
	{
		int wa;

		// determine the edges around with water.
		wa = IsWateredTile(TILE_ADDXY(tile, -1, 0)) << 0;
		wa += IsWateredTile(TILE_ADDXY(tile, 0, 1)) << 1;
		wa += IsWateredTile(TILE_ADDXY(tile, 1, 0)) << 2;
		wa += IsWateredTile(TILE_ADDXY(tile, 0, -1)) << 3;

		if (!(wa & 1)) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57);
		if (!(wa & 2)) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 58);
		if (!(wa & 4)) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 59);
		if (!(wa & 8)) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 60);

		// right corner
		if ((wa & 3) == 0) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 4);
		else if ((wa & 3) == 3 && !IsWateredTile(TILE_ADDXY(tile, -1, 1))) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 8);

		// bottom corner
		if ((wa & 6) == 0) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 5);
		else if ((wa & 6) == 6 && !IsWateredTile(TILE_ADDXY(tile, 1, 1))) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 9);

		// left corner
		if ((wa & 12) == 0) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 6);
		else if ((wa & 12) == 12 && !IsWateredTile(TILE_ADDXY(tile, 1, -1))) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 10);

		// upper corner
		if ((wa & 9) == 0) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 7);
		else if ((wa & 9) == 9 && !IsWateredTile(TILE_ADDXY(tile, -1, -1))) DrawGroundSprite(Sprite.SPR_CANALS_BASE + 57 + 11);
	}

	static class LocksDrawTileStruct {
		int delta_x, delta_y, delta_z;
		int width, height, depth;
		//SpriteID 
		int image;
	} 



	static void DrawWaterStuff(final TileInfo ti, final WaterDrawTileStruct wdts,
		int palette, int base
	)
	{
		int image;

		DrawGroundSprite(wdts++.image);

		for (; wdts.delta_x != 0x80; wdts++) {
			image =	wdts.image + base;
			if (_displayGameOptions._opt & DO_TRANS_BUILDINGS) {
				MAKE_TRANSPARENT(image);
			} else {
				image |= palette;
			}
			AddSortableSpriteToDraw(image, ti.x + wdts.delta_x, ti.y + wdts.delta_y, wdts.width, wdts.height, wdts.unk, ti.z + wdts.delta_z);
		}
	}

	static void DrawTile_Water(TileInfo ti)
	{
		// draw water tile
		if (ti.map5 == 0) {
			DrawGroundSprite(Sprite.SPR_FLAT_WATER_TILE);
			if (ti.z != 0) DrawCanalWater(ti.tile);
			return;
		}

		// draw shore
		if (ti.map5 == 1) {
			assert(ti.tileh < 16);
			DrawGroundSprite(_water_shore_sprites[ti.tileh]);
			return;
		}

		// draw shiplift
		if ((ti.map5 & 0xF0) == 0x10) {
			final WaterDrawTileStruct t = _shiplift_display_seq[ti.map5 & 0xF];
			DrawWaterStuff(ti, t, 0, ti.z > t[3].delta_y ? 24 : 0);
			return;
		}

		DrawWaterStuff(ti, _shipdepot_display_seq[ti.map5 & 0x7F], PLAYER_SPRITE_COLOR(GetTileOwner(ti.tile)), 0);
	}

	void DrawShipDepotSprite(int x, int y, int image)
	{
		final WaterDrawTileStruct *wdts = _shipdepot_display_seq[image];

		Gfx.DrawSprite(wdts++.image, x, y);

		for (; wdts.delta_x != 0x80; wdts++) {
			Point pt = RemapCoords(wdts.delta_x, wdts.delta_y, wdts.delta_z);
			Gfx.DrawSprite(wdts.image + PLAYER_SPRITE_COLOR(Global._local_player), x + pt.x, y + pt.y);
		}
	}


	static int GetSlopeZ_Water(final TileInfo  ti)
	{
		return GetPartialZ(ti.x & 0xF, ti.y & 0xF, ti.tileh) + ti.z;
	}

	static int GetSlopeTileh_Water(final TileInfo ti)
	{
		return ti.tileh;
	}

	static void GetAcceptedCargo_Water(TileIndex tile, AcceptedCargo ac)
	{
		/* not used */
	}

	static void GetTileDesc_Water(TileIndex tile, TileDesc *td)
	{
		if (tile.getMap().m5 == 0 && TilePixelHeight(tile) == 0)
			td.str = Str.STR_3804_WATER;
		else if (tile.getMap().m5 == 0)
			td.str = Str.STR_LANDINFO_CANAL;
		else if (tile.getMap().m5 == 1)
			td.str = Str.STR_3805_COAST_OR_RIVERBANK;
		else if ((tile.getMap().m5&0xF0) == 0x10)
			td.str = Str.STR_LANDINFO_LOCK;
		else
			td.str = Str.STR_3806_SHIP_DEPOT;

		td.owner = GetTileOwner(tile);
	}

	static void AnimateTile_Water(TileIndex tile)
	{
		/* not used */
	}

	static void TileLoopWaterHelper(TileIndex tile, final TileIndexDiffC *offs)
	{
		TileIndex target = TILE_ADD(tile, ToTileIndexDiff(offs[0]));

		// type of this tile mustn't be water already.
		if (IsTileType(target, TileTypes.MP_WATER)) return;

		if (TileHeight(TILE_ADD(tile, ToTileIndexDiff(offs[1]))) != 0 ||
				TileHeight(TILE_ADD(tile, ToTileIndexDiff(offs[2]))) != 0) {
			return;
		}

		if (TileHeight(TILE_ADD(tile, ToTileIndexDiff(offs[3]))) != 0 ||
				TileHeight(TILE_ADD(tile, ToTileIndexDiff(offs[4]))) != 0) {
			// make coast..
			switch (GetTileType(target)) {
				case TileTypes.MP_RAILWAY: {
					int slope = GetTileSlope(target, null);
					byte tracks = BitOps.GB(_m[target].m5, 0, 6);
					if (!(
							(slope == 1 && tracks == 0x20) ||
							(slope == 2 && tracks == 0x04) ||
							(slope == 4 && tracks == 0x10) ||
							(slope == 8 && tracks == 0x08)))
						break;
				}
				/* FALLTHROUGH */

				case TileTypes.MP_CLEAR:
				case TileTypes.MP_TREES:
					Global._current_player = Owner.OWNER_WATER;
					if (!Cmd.CmdFailed(Cmd.DoCommandByTile(target, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR))) {
						ModifyTile(
							target,
							TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR |
								TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR,
							Owner.OWNER_WATER, 1
						);
					}
					break;

				case TileTypes.MP_TUNNELBRIDGE:
					// Middle part of bridge with clear land below?
					if ((_m[target].m5 & 0xF8) == 0xC0) {
						_m[target].m5 |= 0x08;
						MarkTileDirtyByTile(tile);
					}
					break;

				default:
					break;
			}
		} else {
			if (IsTileType(target, TileTypes.MP_TUNNELBRIDGE)) {
				byte m5 = _m[target].m5;
				if ((m5 & 0xF8) == 0xC8 || (m5 & 0xF8) == 0xF0)
					return;

				if ((m5 & 0xC0) == 0xC0) {
					ModifyTile(target, TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5, Owner.OWNER_WATER, (m5 & ~0x38) | 0x8);
					return;
				}
			}

			Global._current_player = Owner.OWNER_WATER;
			{
				Vehicle v = FindVehicleOnTileZ(target, 0);
				if (v != null) FloodVehicle(v);
			}

			if (!Cmd.CmdFailed(Cmd.DoCommandByTile(target, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR))) {
				ModifyTile(
					target,
					TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR |
						TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR,
					Owner.OWNER_WATER,
					0
				);
			}
		}
	}

	static void FloodVehicle(Vehicle v)
	{
		if (!(v.vehstatus & VS_CRASHED)) {
			int pass = 0;

			if (v.type == Vehicle.VEH_Road) {	// flood bus/truck
				pass = 1;	// driver
				if (v.cargo_type == AcceptedCargo.CT_PASSENGERS)
					pass += v.cargo_count;

				v.vehstatus |= VS_CRASHED;
				v.u.road.crashed_ctr = 2000;	// max 2220, disappear pretty fast
				RebuildVehicleLists();
			} else if (v.type == Vehicle.VEH_Train) {
				Vehicle  u;

				v = GetFirstVehicleInChain(v);
				u = v;
				if (IsFrontEngine(v)) pass = 4; // driver

				// crash all wagons, and count passangers
				BEGIN_ENUM_WAGONS(v)
					if (v.cargo_type == AcceptedCargo.CT_PASSENGERS) pass += v.cargo_count;
					v.vehstatus |= VS_CRASHED;
				END_ENUM_WAGONS(v)

				v = u;
				v.u.rail.crash_anim_pos = 4000; // max 4440, disappear pretty fast
				RebuildVehicleLists();
			} else {
				return;
			}

			InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile);

			Global.SetDParam(0, pass);
			AddNewsItem(Str.STR_B006_FLOOD_VEHICLE_DESTROYED,
				NEWS_FLAGS(NM_THIN, NF_VIEWPORT|NF_VEHICLE, NT_ACCIDENT, 0),
				v.index,
				0);
			CreateEffectVehicleRel(v, 4, 4, 8, EV_EXPLOSION_LARGE);
			SndPlayVehicleFx(SND_12_EXPLOSION, v);
		}
	}

	// called from tunnelbridge_cmd
	void TileLoop_Water(TileIndex tile)
	{
		int i;
		static final TileIndexDiffC _tile_loop_offs_array[][5] = {
			// tile to mod																shore?				shore?
			{{-1,  0}, {0, 0}, {0, 1}, {-1,  0}, {-1,  1}},
			{{ 0,  1}, {0, 1}, {1, 1}, { 0,  2}, { 1,  2}},
			{{ 1,  0}, {1, 0}, {1, 1}, { 2,  0}, { 2,  1}},
			{{ 0, -1}, {0, 0}, {1, 0}, { 0, -1}, { 1, -1}}
		};

		if (BitOps.IS_INT_INSIDE(tile.TileX(), 1, MapSizeX() - 3 + 1) &&
				BitOps.IS_INT_INSIDE(tile.TileY(), 1, MapSizeY() - 3 + 1)) {
			for (i = 0; i != lengthof(_tile_loop_offs_array); i++) {
				TileLoopWaterHelper(tile, _tile_loop_offs_array[i]);
			}
		}
		// Global._current_player can be changed by TileLoopWaterHelper.. reset it back
		//   here
		Global._current_player = Owner.OWNER_NONE;

		// edges
		if (tile.TileX() == 0 && BitOps.IS_INT_INSIDE(tile.TileY(), 1, MapSizeY() - 3 + 1)) //NE
			TileLoopWaterHelper(tile, _tile_loop_offs_array[2]);

		if (tile.TileX() == MapSizeX() - 2 && BitOps.IS_INT_INSIDE(tile.TileY(), 1, MapSizeY() - 3 + 1)) { //SW
			TileLoopWaterHelper(tile, _tile_loop_offs_array[0]);
		}

		if (tile.TileY() == 0 && BitOps.IS_INT_INSIDE(tile.TileX(), 1, MapSizeX() - 3 + 1)) { //NW
			TileLoopWaterHelper(tile, _tile_loop_offs_array[1]);
		}

		if (tile.TileY() == MapSizeY() - 2 && BitOps.IS_INT_INSIDE(tile.TileX(), 1, MapSizeX() - 3 + 1)) { //SE
			TileLoopWaterHelper(tile, _tile_loop_offs_array[3]);
		}
	}


	static final byte _coast_tracks[16] = {0, 32, 4, 0, 16, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0};
	static final byte _shipdepot_tracks[4] = {1,1,2,2};
	static final byte _shiplift_tracks[12] = {1,2,1,2,1,2,1,2,1,2,1,2};
	static int GetTileTrackStatus_Water(TileIndex tile, TransportType mode)
	{
		int m5;
		int b;

		if (mode != TRANSPORT_WATER)
			return 0;

		m5 = tile.getMap().m5;
		if (m5 == 0)
			return 0x3F3F;

		if (m5 == 1) {
			b = _coast_tracks[GetTileSlope(tile, null)&0xF];
			return b + (b<<8);
		}

		if ( (m5 & 0x10) == 0x10) {
			//
			b = _shiplift_tracks[m5 & 0xF];
			return b + (b<<8);
		}

		if (!(m5 & 0x80))
			return 0;

		b = _shipdepot_tracks[m5 & 0x7F];
		return b + (b<<8);
	}

	extern void ShowShipDepotWindow(TileIndex tile);

	static void ClickTile_Water(TileIndex tile)
	{
		byte m5 = (byte) (tile.getMap().m5 - 0x80);

		if (BitOps.IS_BYTE_INSIDE(m5, 0, 3+1)) {
			if (m5 & 1)
				tile += (m5 == 1) ? TileIndex.TileDiffXY(-1, 0) : TileIndex.TileDiffXY(0, -1);
			ShowShipDepotWindow(tile);
		}
	}

	static void ChangeTileOwner_Water(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		if (!tile.IsTileOwner( old_player)) return;

		if (new_player != Owner.OWNER_SPECTATOR) {
			SetTileOwner(tile, new_player);
		} else {
			Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
		}
	}

	static int VehicleEnter_Water(Vehicle v, TileIndex tile, int x, int y)
	{
		return 0;
	}

	void InitializeDock()
	{
		_last_built_ship_depot_tile = 0;
	}

	final TileTypeProcs _tile_type_water_procs = new TileTypeProcs(
		DrawTile_Water,						/* draw_tile_proc */
		GetSlopeZ_Water,					/* get_slope_z_proc */
		ClearTile_Water,					/* clear_tile_proc */
		GetAcceptedCargo_Water,		/* get_accepted_cargo_proc */
		GetTileDesc_Water,				/* get_tile_desc_proc */
		GetTileTrackStatus_Water,	/* get_tile_track_status_proc */
		ClickTile_Water,					/* click_tile_proc */
		AnimateTile_Water,				/* animate_tile_proc */
		TileLoop_Water,						/* tile_loop_clear */
		ChangeTileOwner_Water,		/* change_tile_owner_clear */
		null,											/* get_produced_cargo_proc */
		VehicleEnter_Water,				/* vehicle_enter_tile_proc */
		null,											/* vehicle_leave_tile_proc */
		GetSlopeTileh_Water,			/* get_slope_tileh_proc */
	);
	
	
}
