package com.dzavalishin.game;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.struct.TileIndexDiffC;
import com.dzavalishin.tables.ClearTables;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.xui.ViewPort;


@SuppressWarnings("EmptyMethod")
public class Clear extends ClearTables {

	static class TerraformerHeightMod {
		TileIndex tile;
		int height;
	} 

	static class TerraformerState {
		int [] height = new int[4];
		int flags;

		int direction;
		int modheight_count;
		int tile_table_count;

		int cost;

		TileIndex [] tile_table;
		TerraformerHeightMod [] modheight;

	}

	static int TerraformAllowTileProcess(TerraformerState ts, TileIndex tile)
	{
		//MutableTileIndex t;
		//int count;

		if (tile.TileX() == Global.MapMaxX() || tile.TileY() == Global.MapMaxY()) 
			return -1;

		/*
		t = new MutableTileIndex( ts.tile_table );
		for (count = ts.tile_table_count; count != 0; count--) {
			if (t == tile) return 0;
			t.madd(1);
		}*/

		for( int i = 0; i < ts.tile_table_count; i++ )
		{
			TileIndex t = ts.tile_table[i];
			if (t.getTile() == tile.getTile()) return 0;
		}
		
		return 1;
	}

	static int TerraformGetHeightOfTile(TerraformerState ts, TileIndex tile)
	{
		/*
		TerraformerHeightMod mod = ts.modheight;
		int count;

		for (count = ts.modheight_count; count != 0;  mod++) {
			if (mod.tile == tile) return mod.height;
			count--;
		}*/
		// TODO check modheight_count?
		int i = 0;
		for(TerraformerHeightMod mod : ts.modheight )
		{
			if( i++ >= ts.modheight_count)
				break;
			
			if (mod.tile.getTile() == tile.getTile()) return mod.height;
		}

		return tile.TileHeight();
	}

	static void TerraformAddDirtyTile(TerraformerState ts, TileIndex tile)
	{
		int i, count;

		count = ts.tile_table_count;
		if (count >= 625) return;

		for(i = 0; i < count; i++) {
			if (ts.tile_table[i].equals(tile)) return;
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
		ts.tile_table[ts.tile_table_count++] = new TileIndex( tile );

		return 0;
	}

	private static final TileIndexDiffC _terraform_tilepos[] = {
			new TileIndexDiffC( 1,  0),
			new TileIndexDiffC(-2,  0),
			new TileIndexDiffC( 1,  1),
			new TileIndexDiffC( 0, -2)
		};
	
	static boolean TerraformTileHeight(TerraformerState ts, TileIndex tile, int height)
	{
		int nh;
		//TerraformerHeightMod mod;
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

		//mod = ts.modheight;
		count = ts.modheight_count;
		int i;
		for (i = 0; /*i < count*/; i++ ) {
			if (count == 0) {
				if (ts.modheight_count >= 576)
					return false;
				ts.modheight_count++;
				break;
			}
			if (ts.modheight[i].tile.equals(tile)) break;
			//mod++;
			count--;
		}

		ts.modheight[i] = new TerraformerHeightMod();
		ts.modheight[i].tile = tile;
		ts.modheight[i].height = height;

		ts.cost += Global._price.terraform;

		{
			int direction = ts.direction, r;
			//final TileIndexDiffC ttm;


			//for(ttm = _terraform_tilepos; ttm != endof(_terraform_tilepos); ttm++)
			for(final TileIndexDiffC ttm : _terraform_tilepos )
			{
				tile = tile.iadd( TileIndex.ToTileIndexDiff(ttm) );

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
	static int CmdTerraformLand(int x, int y, int flags, int p1, int p2)
	{
		TerraformerState ts = new TerraformerState();
		TileIndex tile;
		int direction;

		TerraformerHeightMod [] modheight_data = new TerraformerHeightMod[576];
		//TileIndex[] tile_table_data = new TileIndex[625];

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		Global._error_message = Str.INVALID_STRING;
		Global._terraform_err_tile = null;

		ts.direction = direction = (p2 != 0) ? 1 : -1;
		ts.flags = flags;
		ts.modheight_count = ts.tile_table_count = 0;
		ts.cost = 0;
		ts.modheight = modheight_data;
		ts.tile_table = new TileIndex[625]; // tile_table_data;

		tile = TileIndex.TileVirtXY(x, y);

		/* Make an extra check for map-bounds cause we add tiles to the originating tile */
		if (tile.iadd( TileIndex.TileDiffXY(1, 1)).getTile() > Global.MapSize()) return Cmd.CMD_ERROR;

		if(0 != (p1 & 1)) {
			if (!TerraformTileHeight(ts, tile.iadd( TileIndex.TileDiffXY(1, 0) ),
					tile.iadd(1, 0).TileHeight() + direction))
						return Cmd.CMD_ERROR;
		}

		if(0 != (p1 & 2)) {
			if (!TerraformTileHeight(ts, tile.iadd( TileIndex.TileDiffXY(1, 1) ),
					tile.iadd(1, 1).TileHeight() + direction))
						return Cmd.CMD_ERROR;
		}

		if(0 != (p1 & 4)) {
			if (!TerraformTileHeight(ts, tile.iadd(0, 1),
					tile.iadd(0, 1).TileHeight() + direction))
						return Cmd.CMD_ERROR;
		}

		if(0 != (p1 & 8)) {
			if (!TerraformTileHeight(ts, tile.iadd(0, 0),
					tile.iadd(0, 0).TileHeight() + direction))
						return Cmd.CMD_ERROR;
		}

		if (direction == -1) {
			/* Check if tunnel would take damage */
			//MutableTileIndex ti = new MutableTileIndex( ts.tile_table );
			int count = ts.tile_table_count;
			//for (count = ts.tile_table_count; count != 0; count--) 
			for (int i = 0; i < count; i++ )
			{
				int z, t;
				TileIndex tilei = new TileIndex( ts.tile_table[i] );

				z = TerraformGetHeightOfTile(ts, tilei.iadd( TileIndex.TileDiffXY(0, 0)));
				t = TerraformGetHeightOfTile(ts, tilei.iadd( TileIndex.TileDiffXY(1, 0)));
				if (t <= z) z = t;
				t = TerraformGetHeightOfTile(ts, tilei.iadd( TileIndex.TileDiffXY(1, 1)));
				if (t <= z) z = t;
				t = TerraformGetHeightOfTile(ts, tilei.iadd( TileIndex.TileDiffXY(0, 1)));
				if (t <= z) z = t;

				if (!TunnelBridgeCmd.CheckTunnelInWay(tile, z * 8)) {
					return Cmd.return_cmd_error(Str.STR_1002_EXCAVATION_WOULD_DAMAGE);
					
				//ti.madd(1);
				}
			}
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			/* Clear the landscape at the tiles */
			{
				/*
				MutableTileIndex ti = new MutableTileIndex(ts.tile_table);
				for (count = ts.tile_table_count; count != 0; count--) {
					Cmd.DoCommandByTile(ti, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
					ti.madd(1);
				}
				*/
				
				int count = 0;
				for( TileIndex ti : ts.tile_table )
				{
					if( count++ >= ts.tile_table_count )
						break;
					Cmd.DoCommandByTile(ti, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
				}
			}

			/* change the height */
			{
				int count = ts.modheight_count;
				//TerraformerHeightMod mod;
				//mod = ts.modheight;
				for (int i = 0; i < count; i++) {
					TileIndex til = ts.modheight[i].tile;

					til.SetTileHeight( ts.modheight[i].height);
					TerraformAddDirtyTileAround(ts, til);
				}
			}

			/* finally mark the dirty tiles dirty */
			{
				int count = ts.tile_table_count;
				for (int i = 0 ; i < count; i++) {
					ts.tile_table[i].MarkTileDirtyByTile();
				}
			}
		}
		return ts.cost;
	}


	/** Levels a selected (rectangle) area of land
	 * @param ex end tile of area-drag
	 * @param ey end tile of area-drag
	 * @param pp1 start tile of area drag
	 * @param p2 unused
	 */
	static int CmdLevelLand(int ex, int ey, int flags, int pp1, int p2)
	{
		//int size_x, size_y;
		int sx, sy;
		int h;
		TileIndex tile;
		//int ret, cost, money;
		int [] cost = {0};
		long [] money = {0};

		if (pp1 > Global.MapSize()) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		TileIndex p1 = new TileIndex(pp1);
		
		// remember level height
		h = p1.TileHeight();

		ex >>= 4; ey >>= 4;

		// make sure sx,sy are smaller than ex,ey
		sx = p1.TileX();
		sy = p1.TileY();
		if (ex < sx) { int t = sx; sx = ex; ex = t; } // intswap(ex, sx);
		if (ey < sy) { int t = sy; sy = ey; ey = t; } // intswap(ey, sy);
		tile = TileIndex.TileXY(sx, sy);

		int size_x = ex-sx+1;
		int size_y = ey-sy+1;

		money[0] = Cmd.GetAvailableMoneyForCommand();
		//cost[0] = 0;

		//BEGIN_TILE_LOOP(tile2, size_x, size_y, tile) 
		TileIndex.forAll( size_x, size_y, tile, (tile2) ->
		{
			int curh = tile2.TileHeight();
			while (curh != h) {
				int ret = Cmd.DoCommandByTile(tile2, 8, (curh > h) ? 0 : 1, flags & ~Cmd.DC_EXEC, Cmd.CMD_TERRAFORM_LAND);
				if (Cmd.CmdFailed(ret)) break;
				cost[0] += ret;

				if(0 != (flags & Cmd.DC_EXEC)) {
					if ((money[0] -= ret) < 0) {
						Global._additional_cash_required = ret;
						cost[0] = cost[0] - ret;
						return true;
					}
					Cmd.DoCommandByTile(tile2, 8, (curh > h) ? 0 : 1, flags, Cmd.CMD_TERRAFORM_LAND);
				}

				curh += (curh > h) ? -1 : 1;
			}
			return false;
		}); //END_TILE_LOOP(tile2, size_x, size_y, tile)

		return (cost[0] == 0) ? Cmd.CMD_ERROR : cost[0];
	}

	/** Purchase a land area. Actually you only purchase one tile, so
	 * the name is a bit confusing ;p
	 * @param x,y the tile the player is purchasing
	 * @param p1 unused
	 * @param p2 unused
	 */
	static int CmdPurchaseLandArea(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile;
		int cost;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		tile = TileIndex.TileVirtXY(x, y);

		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		if (tile.IsTileType( TileTypes.MP_UNMOVABLE) && tile.getMap().m5 == 3 &&
				tile.IsTileOwner(PlayerID.getCurrent()))
			return Cmd.return_cmd_error(Str.STR_5807_YOU_ALREADY_OWN_IT);

		cost = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(cost)) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			Landscape.ModifyTile(tile, TileTypes.MP_UNMOVABLE,
				//TileTypes.MP_SETTYPE(TileTypes.MP_UNMOVABLE) | 
				TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5,
				3 /* map5 */
				);
		}

		return (int) (cost + Global._price.purchase_land * 10);
	}


	static final int empty = 0;
	static final double[] clear_price_table = {
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
		final int price = (int) clear_price_table[BitOps.GB(tile.getMap().m5, 0, 5)];

		if( 0 != (flags & Cmd.DC_EXEC)) 
			Landscape.DoClearSquare(tile);

		return price;
	}

	/** Sell a land area. Actually you only sell one tile, so
	 * the name is a bit confusing ;p
	 * @param x,y the tile the player is selling
	 * @param p1 unused
	 * @param p2 unused
	 */
	static int CmdSellLandArea(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		tile = TileIndex.TileVirtXY(x, y);

		if (!tile.IsTileType( TileTypes.MP_UNMOVABLE) || tile.getMap().m5 != 3) return Cmd.CMD_ERROR;
		if (!tile.CheckTileOwnership() && !PlayerID.getCurrent().isWater()) return Cmd.CMD_ERROR;


		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		if( 0 != (flags & Cmd.DC_EXEC) )
			Landscape.DoClearSquare(tile);

		return (int) (- Global._price.purchase_land * 2);
	}





	static void DrawClearLandTile(final TileInfo ti, int set)
	{
		ViewPort.DrawGroundSprite(Sprite.SPR_FLAT_BARE_LAND + Landscape._tileh_to_sprite[ti.tileh] + set * 19);
	}

	static void DrawHillyLandTile(final TileInfo ti)
	{
		if (ti.tileh != 0) {
			ViewPort.DrawGroundSprite(Sprite.SPR_FLAT_ROUGH_LAND + Landscape._tileh_to_sprite[ti.tileh]);
		} else {
			ViewPort.DrawGroundSprite(_landscape_clear_sprites[BitOps.GB(ti.x ^ ti.y, 4, 3)]);
		}
	}

	static void DrawClearLandFence(final TileInfo ti)
	{
		int m4 = ti.tile.getMap().m4;
		int z =  ti.z;

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
			ViewPort.DrawGroundSprite(Sprite.SPR_FLAT_ROCKY_LAND_1 + Landscape._tileh_to_sprite[ti.tileh]);
			break;

		case 3:
			ViewPort.DrawGroundSprite(_clear_land_sprites_1[BitOps.GB(ti.tile.getMap().m3, 0, 4)] + Landscape._tileh_to_sprite[ti.tileh]);
			break;

		case 4:
			ViewPort.DrawGroundSprite(_clear_land_sprites_2[BitOps.GB(ti.map5, 0, 2)] + Landscape._tileh_to_sprite[ti.tileh]);
			break;

		case 5:
			ViewPort.DrawGroundSprite(_clear_land_sprites_3[BitOps.GB(ti.map5, 0, 2)] + Landscape._tileh_to_sprite[ti.tileh]);
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

	static AcceptedCargo GetAcceptedCargo_Clear(TileIndex tile)
	{
		return new AcceptedCargo();
		/* unused */
	}

	@SuppressWarnings("EmptyMethod")
	static void AnimateTile_Clear(TileIndex tile)
	{
		/* unused */
	}

	static void TileLoopClearHelper(TileIndex tile)
	{
		boolean self, neighbour;
		TileIndex dirty = TileIndex.INVALID_TILE;
		final TileTypes type = tile.GetTileType();
		
		switch (type) {
			case MP_CLEAR:
				// land type == partial desert
				self = (BitOps.GB(tile.getMap().m5, 0, 5) == 15); 
				break;

			default:
				self = false;
				break;
		}

		switch (tile.iadd(1, 0).GetTileType()) {
			case MP_CLEAR:
				// land type == partial desert
				neighbour = (BitOps.GB(tile.iadd(1, 0).M().m5, 0, 5) == 15);
				break;

			default:
				neighbour = false;
				break;
		}

		if (BitOps.GB(tile.getMap().m4, 5, 3) == 0) { //fence?
			if (self != neighbour) {
				tile.getMap().m4 =  BitOps.RETSB(tile.getMap().m4, 5, 3, 3);
				dirty = tile;
			}
		} else {
			if (!self && !neighbour) {
				tile.getMap().m4 =  BitOps.RETSB(tile.getMap().m4, 5, 3, 0);
				dirty = tile;
			}
		}

		switch (tile.iadd(0, 1).GetTileType()) {
			case MP_CLEAR:
				// land type == partial desert
				neighbour = (BitOps.GB(tile.iadd(0, 1).M().m5, 0, 5) == 15);
				break;

			default:
				neighbour = false;
				break;
		}

		if (BitOps.GB(tile.getMap().m4, 2, 3) == 0) {
			if (self != neighbour) {
				tile.getMap().m4 =  BitOps.RETSB(tile.getMap().m4, 2, 3, 3);
				dirty = tile;
			}
		} else {
			if (!self && !neighbour) {
				tile.getMap().m4 =  BitOps.RETSB(tile.getMap().m4, 2, 3, 0);
				dirty = tile;
			}
		}

		if (dirty != TileIndex.INVALID_TILE) dirty.MarkTileDirtyByTile();
	}


	/* convert into snowy tiles */
	static void TileLoopClearAlps(TileIndex tile)
	{
		int k;
		int m5,tmp;

		/* distance from snow line, in steps of 8 */
		k = tile.GetTileZ() - GameOptions._opt.snow_line;

		m5  = (tile.getMap().m5 & 0x1C);
		tmp = (tile.getMap().m5 & 3);

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
				m5 = 0xFF & ((tmp - 1) + 0x10);
			} else
				return;
		} else if (k < 8) {
			/* snow_0 */
			if (m5 != 0x10) {
				m5 = 0x10;
			} else if (tmp != 1) {
				m5 = 1;
				if (tmp != 0)
					m5 =  (tmp - 1);
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
					m5 = 0xFF & (tmp + 1);
				m5 += 0x10;
			} else
				return;
		} else {
			/* snow_p2_up */
			if (m5 != 0x10) {
				m5 = 0x10;
			} else if (tmp != 3) {
				m5 = 0xFF & (tmp + 1 + 0x10);
			} else
				return;
		}

		tile.getMap().m5 = 0xFF & m5;
		tile.MarkTileDirtyByTile();
	}

	static void TileLoopClearDesert(TileIndex tile)
	{
	 	if ((tile.getMap().m5 & 0x1C) == 0x14) return;

		if (tile.GetMapExtraBits() == TileInfo.EXTRABITS_DESERT) {
			tile.getMap().m5 = 0x17;
		} else {
			if (tile.iadd( TileIndex.TileDiffXY( 1,  0)).GetMapExtraBits() != TileInfo.EXTRABITS_DESERT &&
					tile.iadd( TileIndex.TileDiffXY(-1,  0)).GetMapExtraBits() != TileInfo.EXTRABITS_DESERT &&
					tile.iadd( TileIndex.TileDiffXY( 0,  1)).GetMapExtraBits() != TileInfo.EXTRABITS_DESERT &&
					tile.iadd( TileIndex.TileDiffXY( 0, -1)).GetMapExtraBits() != TileInfo.EXTRABITS_DESERT)
				return;
			tile.getMap().m5 = 0x15;
		}

		tile.MarkTileDirtyByTile();
	}

	static void TileLoop_Clear(TileIndex tile)
	{
		int m5,m3;

		TileLoopClearHelper(tile);

		if (GameOptions._opt.landscape == Landscape.LT_DESERT) {
			TileLoopClearDesert(tile);
		} else if (GameOptions._opt.landscape == Landscape.LT_HILLY) {
			TileLoopClearAlps(tile);
		}

		m5 = tile.getMap().m5;
		if ((m5 & 0x1C) == 0x10 || (m5 & 0x1C) == 0x14) return; // 1C == 0001 1100 - ??

		if ((m5 & 0x1C) != 0xC) {
			if ((m5 & 3) == 3) return; // full frass/full show

			if (Global._game_mode != GameModes.GM_EDITOR) {
				if( Hal.RandomRange(20) >= 2) // add some randomness to growth rate
					m5 += 0x20;
				if( Hal.RandomRange(20) < 2) // add some randomness to growth rate
					m5 += 0x20;
				m5 &= 0xFF;
				if (m5 >= 0x20) {
					// Didn't overflow
					tile.getMap().m5 = 0xFF & m5;
					return;
				}
				/* did overflow, so continue */
			} else {
				m5 =  ((BitOps.GB(Hal.Random(), 0, 8) > 21) ? 2 : 6);
			}
			m5++;
		} else if (Global._game_mode != GameModes.GM_EDITOR) {
			/* handle farm field */
			m5 += 0x20;
			m5 &= 0xFF;
			if (m5 >= 0x20) {
				// Didn't overflow
				tile.getMap().m5 = 0xFF & m5;
				return;
			}
			/* overflowed */
			m3 =  (tile.getMap().m3 + 1);
			assert( (m3 & 0xF) != 0);
			if ( (m3 & 0xF) >= 9) /* NOTE: will not work properly if m3&0xF == 0xF */
				m3 &= ~0xF;
			tile.getMap().m3 = 0xFF & m3;
		}

		tile.getMap().m5 = 0xFF & m5;
		tile.MarkTileDirtyByTile();
	}

	static void GenerateClearTile()
	{
		int i;
		TileIndex tile;

		/* add hills */
		i = Map.ScaleByMapSize(BitOps.GB(Hal.Random(), 0, 10) + 0x400);
		do {
			tile = TileIndex.RandomTile();
			if (tile.IsTileType( TileTypes.MP_CLEAR)) 
				tile.getMap().m5 = 0xFF & BitOps.RETSB(tile.getMap().m5, 2, 2, 1);
		} while (--i > 0);

		/* add grey squares */
		i = Map.ScaleByMapSize(BitOps.GB(Hal.Random(), 0, 7) + 0x80);
		do {
			int r = Hal.Random();
			tile = TileIndex.RandomTileSeed(r);
			if (tile.IsTileType( TileTypes.MP_CLEAR)) {
				int j = BitOps.GB(r, 16, 4) + 5;
				for(;;) {
					TileIndex tile_new = null;

					tile.getMap().m5 = 0xFF & BitOps.RETSB(tile.getMap().m5, 2, 2, 2);
					boolean getOut = false;
					do {
						if (--j == 0)
						{
							//goto get_out;
							getOut = true;
							break;
						}
						tile_new = tile.iadd( TileIndex.TileOffsByDir(BitOps.GB(Hal.Random(), 0, 2)) );
					} while (!tile.IsTileType( TileTypes.MP_CLEAR));
					
					if(getOut) break;
					
					assert tile_new != null;
					tile = tile_new;
				}
	//get_out:;
			}
		} while (--i > 0);
	}

	static void ClickTile_Clear(TileIndex tile)
	{
		/* not used */
	}

	static int GetTileTrackStatus_Clear(TileIndex tile, TransportType mode)
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

	static TileDesc GetTileDesc_Clear(TileIndex tile)	
	{
		TileDesc td = new TileDesc();
		int i = BitOps.GB(tile.getMap().m5, 2, 3);
		if (i == 0) i = BitOps.GB(tile.getMap().m5, 0, 2) + 8;
		td.str = _clear_land_str[i - 1];
		td.owner =  tile.GetTileOwner().id;
		return td;
	}

	static void ChangeTileOwner_Clear(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
	}

	static void InitializeClearLand()
	{
		GameOptions._opt.snow_line = (byte) (Global._patches.snow_line_height * 8);
	}

	final static TileTypeProcs _tile_type_clear_procs = new TileTypeProcs(
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
		Clear::GetSlopeTileh_Clear			/* get_slope_tileh_proc */
	);

}
