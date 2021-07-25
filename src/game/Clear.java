package game;
import game.util.BitOps;

// clear_cmd.c
public class Clear {

	class TerraformerHeightMod {
		TileIndex tile;
		byte height;
	} 

	class TerraformerState {
		int [] height = new int[4];
		int flags;

		int direction;
		int modheight_count;
		int tile_table_count;

		int cost;

		TileIndex tile_table;
		TerraformerHeightMod modheight;

	}

	static int TerraformAllowTileProcess(TerraformerState ts, TileIndex tile)
	{
		MutableTileIndex t;
		int count;

		if (tile.TileX() == Global.MapMaxX() || tile.TileY() == Global.MapMaxY()) 
			return -1;

		t = new MutableTileIndex( ts.tile_table );
		for (count = ts.tile_table_count; count != 0; count--) {
			if (t == tile) return 0;
			t.madd(1);
		}

		return 1;
	}

	static int TerraformGetHeightOfTile(TerraformerState ts, TileIndex tile)
	{
		TerraformerHeightMod mod = ts.modheight;
		int count;

		for (count = ts.modheight_count; count != 0; count--, mod++) {
			if (mod.tile == tile) return mod.height;
		}

		return tile.TileHeight();
	}

	static void TerraformAddDirtyTile(TerraformerState ts, TileIndex tile)
	{
		int count;
		MutableTileIndex t;

		count = ts.tile_table_count;

		if (count >= 625) return;

		t = new MutableTileIndex( ts.tile_table );
		
		for(; count != 0; count--) {
			if (t == tile) return;
			t.madd(1);
		}

		ts.tile_table[ts.tile_table_count++] = tile;
	}

	static void TerraformAddDirtyTileAround(TerraformerState ts, TileIndex tile)
	{
		TerraformAddDirtyTile(ts, tile.iadd( TileIndex.TileDiffXY( 0, -1)) );
		TerraformAddDirtyTile(ts, tile.iadd( TileIndex.TileDiffXY(-1, -1)) );
		TerraformAddDirtyTile(ts, tile.iadd( TileIndex.TileDiffXY(-1,  0)) );		
		TerraformAddDirtyTile(ts, tile);
	}

	
	private static final byte _railway_modes[] = {8, 0x10, 4, 0x20};
	private static final byte _railway_dangslopes[] = {0xd, 0xe, 7, 0xb};
	private static final byte _railway_dangslopes2[] = {0x2, 0x1, 0x8, 0x4};
	
	static int TerraformProc(TerraformerState ts, TileIndex tile, int mode)
	{
		int r;
		boolean skip_clear = false;

		assert(tile.getTile() < Global.MapSize());

		if ((r=TerraformAllowTileProcess(ts, tile)) <= 0)
			return r;

		if (tile.IsTileType( TileTypes.MP_RAILWAY)) {

			// Nothing could be built at the steep slope - this avoids a bug
			// when you have a single diagonal track in one corner on a
			// basement and then you raise/lower the other corner.
			int tileh = tile.GetTileSlope(null) & 0xF;
			if (tileh == _railway_dangslopes[mode] ||
					tileh == _railway_dangslopes2[mode]) {
						Global._terraform_err_tile = tile;
				Global._error_message = Str.STR_1008_MUST_REMOVE_RAILROAD_TRACK;
				return -1;
			}

			// If we have a single diagonal track there, the other side of
			// tile can be terraformed.
			if ((tile.getMap().m5 & ~0x40) == _railway_modes[mode]) {
				if (ts.direction == 1) return 0;
				skip_clear = true;
			}
		}

		if (!skip_clear) {
			int ret = Cmd.DoCommandByTile(tile, 0,0, ts.flags & ~Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);

			if (Cmd.CmdFailed(ret)) {
				Global._terraform_err_tile = tile;
				return -1;
			}

			ts.cost += ret;
		}

		if (ts.tile_table_count >= 625) return -1;
		ts.tile_table[ts.tile_table_count++] = tile;

		return 0;
	}

	private static final TileIndexDiffC _terraform_tilepos[] = {
			{ 1,  0},
			{-2,  0},
			{ 1,  1},
			{ 0, -2}
		};
	
	static boolean TerraformTileHeight(TerraformerState ts, TileIndex tile, int height)
	{
		int nh;
		TerraformerHeightMod mod;
		int count;

		assert(tile.getTile() < Global.MapSize());

		if (height < 0) {
			Global._error_message = Str.STR_1003_ALREADY_AT_SEA_LEVEL;
			return false;
		}

		Global._error_message = Str.STR_1004_TOO_HIGH;

		if (height > 15) return false;

		nh = TerraformGetHeightOfTile(ts, tile);
		if (nh < 0 || height == nh) return false;

		if (TerraformProc(ts, tile, 0) < 0) return false;
		if (TerraformProc(ts, tile.iadd( TileIndex.TileDiffXY( 0, -1)), 1) < 0) return false;
		if (TerraformProc(ts, tile.iadd( TileIndex.TileDiffXY(-1, -1)), 2) < 0) return false;
		if (TerraformProc(ts, tile.iadd( TileIndex.TileDiffXY(-1,  0)), 3) < 0) return false;

		mod = ts.modheight;
		count = ts.modheight_count;

		for (;;) {
			if (count == 0) {
				if (ts.modheight_count >= 576)
					return false;
				ts.modheight_count++;
				break;
			}
			if (mod.tile == tile) break;
			mod++;
			count--;
		}

		mod.tile = tile;
		mod.height = (byte)height;

		ts.cost += Global._price.terraform;

		{
			int direction = ts.direction, r;
			final TileIndexDiffC ttm;


			for(ttm = _terraform_tilepos; ttm != endof(_terraform_tilepos); ttm++) {
				tile += ToTileIndexDiff(*ttm);

				r = TerraformGetHeightOfTile(ts, tile);
				if (r != height && r-direction != height && r+direction != height) {
					if (!TerraformTileHeight(ts, tile, r+direction))
						return false;
				}
			}
		}

		return true;
	}

	/** Terraform land
	 * @param x,y coordinates to terraform
	 * @param p1 corners to terraform.
	 * @param p2 direction; eg up or down
	 */
	int CmdTerraformLand(int x, int y, int flags, int p1, int p2)
	{
		TerraformerState ts = new TerraformerState();
		TileIndex tile;
		int direction;

		TerraformerHeightMod [] modheight_data = new TerraformerHeightMod[576];
		TileIndex[] tile_table_data = new TileIndex[625];

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		Global._error_message = INVALID_STRING_ID;
		Global._terraform_err_tile = 0;

		ts.direction = direction = (p2 != 0) ? 1 : -1;
		ts.flags = flags;
		ts.modheight_count = ts.tile_table_count = 0;
		ts.cost = 0;
		ts.modheight = modheight_data;
		ts.tile_table = tile_table_data;

		tile = TileIndex.TileVirtXY(x, y);

		/* Make an extra check for map-bounds cause we add tiles to the originating tile */
		if (tile.iadd( TileIndex.TileDiffXY(1, 1)).getTile() > Global.MapSize()) return Cmd.CMD_ERROR;

		if(0 != (p1 & 1)) {
			if (!TerraformTileHeight(ts, tile.iadd( TileIndex.TileDiffXY(1, 0) ),
					TileHeight(tile.iadd(1, 0)) + direction))
						return Cmd.CMD_ERROR;
		}

		if(0 != (p1 & 2)) {
			if (!TerraformTileHeight(ts, tile.iadd( TileIndex.TileDiffXY(1, 1) ),
					TileHeight(tile.iadd(1, 1)) + direction))
						return Cmd.CMD_ERROR;
		}

		if(0 != (p1 & 4)) {
			if (!TerraformTileHeight(ts, tile.iadd(0, 1),
					TileHeight(tile.iadd(0, 1)) + direction))
						return Cmd.CMD_ERROR;
		}

		if(0 != (p1 & 8)) {
			if (!TerraformTileHeight(ts, tile + TileIndex.TileDiffXY(0, 0),
					TileHeight(tile + TileIndex.TileDiffXY(0, 0)) + direction))
						return Cmd.CMD_ERROR;
		}

		if (direction == -1) {
			/* Check if tunnel would take damage */
			int count;
			MutableTileIndex ti = new MutableTileIndex( ts.tile_table );

			for (count = ts.tile_table_count; count != 0; count--) {
				int z, t;
				TileIndex tilei = new TileIndex( ti );

				z = TerraformGetHeightOfTile(ts, tilei.iadd( TileIndex.TileDiffXY(0, 0)));
				t = TerraformGetHeightOfTile(ts, tilei.iadd( TileIndex.TileDiffXY(1, 0)));
				if (t <= z) z = t;
				t = TerraformGetHeightOfTile(ts, tilei.iadd( TileIndex.TileDiffXY(1, 1)));
				if (t <= z) z = t;
				t = TerraformGetHeightOfTile(ts, tilei.iadd( TileIndex.TileDiffXY(0, 1)));
				if (t <= z) z = t;

				if (!CheckTunnelInWay(tile, z * 8)) {
					return_cmd_error(Str.STR_1002_EXCAVATION_WOULD_DAMAGE);
					
				ti.madd(1);
				}
			}
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			/* Clear the landscape at the tiles */
			{
				int count;
				TileIndex ti = new TileIndex(ts.tile_table);
				for (count = ts.tile_table_count; count != 0; count--) {
					DoCommandByTile(ti, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
					ti.add(1);
				}
			}

			/* change the height */
			{
				int count;
				TerraformerHeightMod mod;

				mod = ts.modheight;
				for (count = ts.modheight_count; count != 0; count--, mod++) {
					TileIndex til = mod.tile;

					SetTileHeight(til, mod.height);
					TerraformAddDirtyTileAround(ts, til);
				}
			}

			/* finally mark the dirty tiles dirty */
			{
				int count;
				TileIndex ti = new TileIndex( ts.tile_table );
				for (count = ts.tile_table_count; count != 0; count--) {
					ti.MarkTileDirtyByTile();
					ti.add(1); // TODO check all .add / .sub for modification of TileIndex stored elsewhere
				}
			}
		}
		return ts.cost;
	}


	/** Levels a selected (rectangle) area of land
	 * @param x,y end tile of area-drag
	 * @param p1 start tile of area drag
	 * @param p2 unused
	 */
	int CmdLevelLand(int ex, int ey, int flags, int p1, int p2)
	{
		//int size_x, size_y;
		int sx, sy;
		int h;
		TileIndex tile;
		int ret, cost, money;

		if (p1 > Global.MapSize()) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		// remember level height
		h = p1.TileHeight();

		ex >>= 4; ey >>= 4;

		// make sure sx,sy are smaller than ex,ey
		sx = p1.TileX();
		sy = p1.TileY();
		if (ex < sx) intswap(ex, sx);
		if (ey < sy) intswap(ey, sy);
		tile = TileIndex.TileXY(sx, sy);

		int size_x = ex-sx+1;
		int size_y = ey-sy+1;

		money = GetAvailableMoneyForCommand();
		cost = 0;

		//BEGIN_TILE_LOOP(tile2, size_x, size_y, tile) 
		TileIndex.forAll( size_x, size_y, tile, (tile2) ->
		{
			int curh = tile2.TileHeight();
			while (curh != h) {
				ret = Cmd.DoCommandByTile(tile2, 8, (curh > h) ? 0 : 1, flags & ~Cmd.DC_EXEC, Cmd.CMD_TERRAFORM_LAND);
				if (Cmd.CmdFailed(ret)) break;
				cost += ret;

				if (flags & Cmd.DC_EXEC) {
					if ((money -= ret) < 0) {
						_additional_cash_required = ret;
						cost = cost - ret;
						return true;
					}
					Cmd.DoCommandByTile(tile2, 8, (curh > h) ? 0 : 1, flags, Cmd.CMD_TERRAFORM_LAND);
				}

				curh += (curh > h) ? -1 : 1;
			}
			return false;
		}); //END_TILE_LOOP(tile2, size_x, size_y, tile)

		return (cost == 0) ? Cmd.CMD_ERROR : cost;
	}

	/** Purchase a land area. Actually you only purchase one tile, so
	 * the name is a bit confusing ;p
	 * @param x,y the tile the player is purchasing
	 * @param p1 unused
	 * @param p2 unused
	 */
	int CmdPurchaseLandArea(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile;
		int cost;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		tile = TileIndex.TileVirtXY(x, y);

		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		if (tile.IsTileType( TileTypes.MP_UNMOVABLE) && tile.getMap().m5 == 3 &&
				tile.IsTileOwner(Global._current_player.id))
			return_cmd_error(Str.STR_5807_YOU_ALREADY_OWN_IT);

		cost = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(cost)) return Cmd.CMD_ERROR;

		if (flags & Cmd.DC_EXEC) {
			ModifyTile(tile,
				TileTypes.MP_SETTYPE(TileTypes.MP_UNMOVABLE) | TileTypes.MP_MAPOwner.OWNER_CURRENT | TileTypes.MP_MAP5,
				3 /* map5 */
				);
		}

		return cost + Global._price.purchase_land * 10;
	}


	static final int empty = 0;
	static final int[] clear_price_table = {
		empty,
		Global._price.clear_1,
		Global._price.clear_1,
		Global._price.clear_1,
		Global._price.purchase_land,
		Global._price.purchase_land,
		Global._price.purchase_land,
		Global._price.purchase_land,
		Global._price.clear_2,
		Global._price.clear_2,
		Global._price.clear_2,
		Global._price.clear_2,
		Global._price.clear_3,
		Global._price.clear_3,
		Global._price.clear_3,
		Global._price.clear_3,
		Global._price.purchase_land,
		Global._price.purchase_land,
		Global._price.purchase_land,
		Global._price.purchase_land,
		Global._price.purchase_land,
		Global._price.purchase_land,
		Global._price.purchase_land,
		Global._price.purchase_land,
		Global._price.clear_2,
		Global._price.clear_2,
		Global._price.clear_2,
		Global._price.clear_2,
	};
	
	static int ClearTile_Clear(TileIndex tile, byte flags)
	{
		final int price = clear_price_table[BitOps.GB(tile.getMap().m5, 0, 5)];

		if( 0 != (flags & Cmd.DC_EXEC)) 
			DoClearSquare(tile);

		return price;
	}

	/** Sell a land area. Actually you only sell one tile, so
	 * the name is a bit confusing ;p
	 * @param x,y the tile the player is selling
	 * @param p1 unused
	 * @param p2 unused
	 */
	int CmdSellLandArea(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		tile = TileIndex.TileVirtXY(x, y);

		if (!tile.IsTileType( TileTypes.MP_UNMOVABLE) || tile.getMap().m5 != 3) return Cmd.CMD_ERROR;
		if (!tile.CheckTileOwnership() && Global._current_player.id != Owner.OWNER_WATER) return Cmd.CMD_ERROR;


		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		if( 0 != (flags & Cmd.DC_EXEC) )
			DoClearSquare(tile);

		return - Global._price.purchase_land * 2;
	}





	void DrawClearLandTile(final TileInfo ti, byte set)
	{
		DrawGroundSprite(Sprite.SPR_FLAT_BARE_LAND + _tileh_to_sprite[ti.tileh] + set * 19);
	}

	void DrawHillyLandTile(final TileInfo ti)
	{
		if (ti.tileh != 0) {
			DrawGroundSprite(Sprite.SPR_FLAT_ROUGH_LAND + _tileh_to_sprite[ti.tileh]);
		} else {
			DrawGroundSprite(_landscape_clear_sprites[BitOps.GB(ti.x ^ ti.y, 4, 3)]);
		}
	}

	static void DrawClearLandFence(final TileInfo ti)
	{
		byte m4 = ti.tile.getMap().m4;
		byte z = (byte) ti.z;

		if(0 != (ti.tileh & 2)) {
			z += 8;
			if (ti.tileh == 0x17) z += 8;
		}

		if (BitOps.GB(m4, 5, 3) != 0) {
			ViewPort.DrawGroundSpriteAt(_clear_land_fence_sprites_1[BitOps.GB(m4, 5, 3) - 1] + _fence_mod_by_tileh[ti.tileh], ti.x, ti.y, z);
		}

		if (BitOps.GB(m4, 2, 3) != 0) {
			ViewPort.DrawGroundSpriteAt(_clear_land_fence_sprites_1[BitOps.GB(m4, 2, 3) - 1] + _fence_mod_by_tileh_2[ti.tileh], ti.x, ti.y, z);
		}
	}

	static void DrawTile_Clear(TileInfo ti)
	{
		switch (BitOps.GB(ti.map5, 2, 3)) {
		case 0:
			DrawClearLandTile(ti, BitOps.GB(ti.map5, 0, 2));
			break;

		case 1:
			DrawHillyLandTile(ti);
			break;

		case 2:
			DrawGroundSprite(Sprite.SPR_FLAT_ROCKY_LAND_1 + _tileh_to_sprite[ti.tileh]);
			break;

		case 3:
			DrawGroundSprite(_clear_land_sprites_1[BitOps.GB(_m[ti.tile].m3, 0, 4)] + _tileh_to_sprite[ti.tileh]);
			break;

		case 4:
			DrawGroundSprite(_clear_land_sprites_2[BitOps.GB(ti.map5, 0, 2)] + _tileh_to_sprite[ti.tileh]);
			break;

		case 5:
			DrawGroundSprite(_clear_land_sprites_3[BitOps.GB(ti.map5, 0, 2)] + _tileh_to_sprite[ti.tileh]);
			break;
		}

		DrawClearLandFence(ti);
	}

	static int GetSlopeZ_Clear(final TileInfo  ti)
	{
		return TileIndex.GetPartialZ(ti.x & 0xF, ti.y & 0xF, ti.tileh) + ti.z;
	}

	static int GetSlopeTileh_Clear(final TileInfo ti)
	{
		return ti.tileh;
	}

	static void GetAcceptedCargo_Clear(TileIndex tile, AcceptedCargo ac)
	{
		/* unused */
	}

	static void AnimateTile_Clear(TileIndex tile)
	{
		/* unused */
	}

	static void TileLoopClearHelper(TileIndex tile)
	{
		boolean self, neighbour;
		TileIndex dirty = TileIndex.INVALID_TILE;

		switch (tile.GetTileType()) {
			case MP_CLEAR:
				self = (BitOps.GB(tile.getMap().m5, 0, 5) == 15);
				break;

			default:
				self = false;
				break;
		}

		switch (GetTileType(TILE_ADDXY(tile, 1, 0))) {
			case TileTypes.MP_CLEAR:
				neighbour = (BitOps.GB(_m[TILE_ADDXY(tile, 1, 0)].m5, 0, 5) == 15);
				break;

			default:
				neighbour = false;
				break;
		}

		if (BitOps.GB(tile.getMap().m4, 5, 3) == 0) {
			if (self != neighbour) {
				tile.getMap().m4 = (byte) BitOps.RETSB(tile.getMap().m4, 5, 3, 3);
				dirty = tile;
			}
		} else {
			if (self == false && neighbour == false) {
				tile.getMap().m4 = (byte) BitOps.RETSB(tile.getMap().m4, 5, 3, 0);
				dirty = tile;
			}
		}

		switch (GetTileType(TILE_ADDXY(tile, 0, 1))) {
			case TileTypes.MP_CLEAR:
				neighbour = (BitOps.GB(_m[TILE_ADDXY(tile, 0, 1)].m5, 0, 5) == 15);
				break;

			default:
				neighbour = false;
				break;
		}

		if (BitOps.GB(tile.getMap().m4, 2, 3) == 0) {
			if (self != neighbour) {
				tile.getMap().m4 = (byte) BitOps.RETSB(tile.getMap().m4, 2, 3, 3);
				dirty = tile;
			}
		} else {
			if (self == false && neighbour == false) {
				tile.getMap().m4 = (byte) BitOps.RETSB(tile.getMap().m4, 2, 3, 0);
				dirty = tile;
			}
		}

		if (dirty != TileIndex.INVALID_TILE) dirty.MarkTileDirtyByTile();
	}


	/* convert into snowy tiles */
	static void TileLoopClearAlps(TileIndex tile)
	{
		int k;
		byte m5,tmp;

		/* distance from snow line, in steps of 8 */
		k = tile.GetTileZ() - GameOptions._opt.snow_line;

		m5  = (byte) (tile.getMap().m5 & 0x1C);
		tmp = (byte) (tile.getMap().m5 & 3);

		if (k < -8) {
			/* snow_m2_down */
			if (m5 != 0x10)
				return;
			if (tmp == 0)
				m5 = 3;
		} else if (k == -8) {
			/* snow_m1 */
			if (m5 != 0x10) {
				m5 = 0x10;
			} else if (tmp != 0) {
				m5 = (byte) ((tmp - 1) + 0x10);
			} else
				return;
		} else if (k < 8) {
			/* snow_0 */
			if (m5 != 0x10) {
				m5 = 0x10;
			} else if (tmp != 1) {
				m5 = 1;
				if (tmp != 0)
					m5 = (byte) (tmp - 1);
				m5 += 0x10;
			} else
				return;
		} else if (k == 8) {
			/* snow_p1 */
			if (m5 != 0x10) {
				m5 = 0x10;
			} else if (tmp != 2) {
				m5 = 2;
				if (tmp <= 2)
					m5 = (byte) (tmp + 1);
				m5 += 0x10;
			} else
				return;
		} else {
			/* snow_p2_up */
			if (m5 != 0x10) {
				m5 = 0x10;
			} else if (tmp != 3) {
				m5 = (byte) (tmp + 1 + 0x10);
			} else
				return;
		}

		tile.getMap().m5 = m5;
		tile.MarkTileDirtyByTile();
	}

	static void TileLoopClearDesert(TileIndex tile)
	{
	 	if ((tile.getMap().m5 & 0x1C) == 0x14) return;

		if (tile.GetMapExtraBits() == 1) {
			tile.getMap().m5 = 0x17;
		} else {
			if (tile.iadd( TileIndex.TileDiffXY( 1,  0)).GetMapExtraBits() != 1 &&
					tile.iadd( TileIndex.TileDiffXY(-1,  0)).GetMapExtraBits() != 1 &&
					tile.iadd( TileIndex.TileDiffXY( 0,  1)).GetMapExtraBits() != 1 &&
					tile.iadd( TileIndex.TileDiffXY( 0, -1)).GetMapExtraBits() != 1)
				return;
			tile.getMap().m5 = 0x15;
		}

		tile.MarkTileDirtyByTile();
	}

	static void TileLoop_Clear(TileIndex tile)
	{
		byte m5,m3;

		TileLoopClearHelper(tile);

		if (GameOptions._opt.landscape == Landscape.LT_DESERT) {
			TileLoopClearDesert(tile);
		} else if (GameOptions._opt.landscape == Landscape.LT_HILLY) {
			TileLoopClearAlps(tile);
		}

		m5 = tile.getMap().m5;
		if ((m5 & 0x1C) == 0x10 || (m5 & 0x1C) == 0x14) return;

		if ((m5 & 0x1C) != 0xC) {
			if ((m5 & 3) == 3) return;

			if (Global._game_mode != GameModes.GM_EDITOR) {
				m5 += 0x20;
				if (m5 >= 0x20) {
					// Didn't overflow
					tile.getMap().m5 = m5;
					return;
				}
				/* did overflow, so continue */
			} else {
				m5 = (byte) ((BitOps.GB(Hal.Random(), 0, 8) > 21) ? 2 : 6);
			}
			m5++;
		} else if (Global._game_mode != GameModes.GM_EDITOR) {
			/* handle farm field */
			m5 += 0x20;
			if (m5 >= 0x20) {
				// Didn't overflow
				tile.getMap().m5 = m5;
				return;
			}
			/* overflowed */
			m3 = (byte) (tile.getMap().m3 + 1);
			assert( (m3 & 0xF) != 0);
			if ( (m3 & 0xF) >= 9) /* NOTE: will not work properly if m3&0xF == 0xF */
				m3 &= ~0xF;
			tile.getMap().m3 = m3;
		}

		tile.getMap().m5 = m5;
		tile.MarkTileDirtyByTile();
	}

	void GenerateClearTile()
	{
		int i;
		TileIndex tile;

		/* add hills */
		i = Map.ScaleByMapSize(BitOps.GB(Hal.Random(), 0, 10) + 0x400);
		do {
			tile = TileIndex.RandomTile();
			if (tile.IsTileType( TileTypes.MP_CLEAR)) 
				tile.getMap().m5 = BitOps.RETSB(tile.getMap().m5, 2, 2, 1);
		} while (--i > 0);

		/* add grey squares */
		i = Map.ScaleByMapSize(BitOps.GB(Hal.Random(), 0, 7) + 0x80);
		do {
			int r = Hal.Random();
			tile = TileIndex.RandomTileSeed(r);
			if (tile.IsTileType( TileTypes.MP_CLEAR)) {
				int j = BitOps.GB(r, 16, 4) + 5;
				for(;;) {
					TileIndex tile_new;

					tile.getMap().m5 = (byte) BitOps.RETSB(tile.getMap().m5, 2, 2, 2);
					do {
						if (--j == 0) goto get_out;
						tile_new = tile + TileOffsByDir(BitOps.GB(Hal.Random(), 0, 2));
					} while (!tile.IsTileType( TileTypes.MP_CLEAR));
					tile = tile_new;
				}
	get_out:;
			}
		} while (--i > 0);
	}

	static void ClickTile_Clear(TileIndex tile)
	{
		/* not used */
	}

	static int GetTileTrackStatus_Clear(TileIndex tile, int mode)
	{
		return 0;
	}

	static final int _clear_land_str[] = {
		Str.STR_080B_ROUGH_LAND,
		Str.STR_080A_ROCKS,
		Str.STR_080E_FIELDS,
		Str.STR_080F_SNOW_COVERED_LAND,
		Str.STR_0810_DESERT,
		0,
		0,
		Str.STR_080C_BARE_LAND,
		Str.STR_080D_GRASS,
		Str.STR_080D_GRASS,
		Str.STR_080D_GRASS,
	};

	static void GetTileDesc_Clear(TileIndex tile, TileDesc td)
	{
		int i = BitOps.GB(tile.getMap().m5, 2, 3);
		if (i == 0) i = BitOps.GB(tile.getMap().m5, 0, 2) + 8;
		td.str = _clear_land_str[i - 1];
		td.owner = (byte) tile.GetTileOwner().owner;
	}

	static void ChangeTileOwner_Clear(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		return;
	}

	void InitializeClearLand()
	{
		GameOptions._opt.snow_line = (byte) (Global._patches.snow_line_height * 8);
	}

	final TileTypeProcs _tile_type_clear_procs = new TileTypeProcs(
		Clear::DrawTile_Clear,						/* draw_tile_proc */
		Clear::GetSlopeZ_Clear,					/* get_slope_z_proc */
		Clear::ClearTile_Clear,					/* clear_tile_proc */
		Clear::GetAcceptedCargo_Clear,		/* get_accepted_cargo_proc */
		Clear::GetTileDesc_Clear,				/* get_tile_desc_proc */
		Clear::GetTileTrackStatus_Clear,	/* get_tile_track_status_proc */
		Clear::ClickTile_Clear,					/* click_tile_proc */
		Clear::AnimateTile_Clear,				/* animate_tile_proc */
		Clear::TileLoop_Clear,						/* tile_loop_clear */
		Clear::ChangeTileOwner_Clear,		/* change_tile_owner_clear */
		null,											/* get_produced_cargo_proc */
		null,											/* vehicle_enter_tile_proc */
		null,											/* vehicle_leave_tile_proc */
		Clear::GetSlopeTileh_Clear,			/* get_slope_tileh_proc */
	);

}