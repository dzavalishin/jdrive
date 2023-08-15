package com.dzavalishin.game;

import java.util.Iterator;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.struct.DrawTileSeqStruct;
import com.dzavalishin.struct.DrawTileSprites;
import com.dzavalishin.struct.DrawTileUnmovableStruct;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.struct.TileIndexDiffC;
import com.dzavalishin.tables.UnmovableTables;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.IntContainer;
import com.dzavalishin.xui.PlayerGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

public class UnmovableCmd extends UnmovableTables {

	/** Destroy a HQ.
	 * During normal gameplay you can only implicitely destroy a HQ when you are
	 * rebuilding it. Otherwise, only water can destroy it.
	 * @param tile tile coordinates where HQ is located to destroy
	 * @param flags docommand flags of calling function
	 */
	static int DestroyCompanyHQ(TileIndex tile, int flags)
	{
		Player.SET_EXPENSES_TYPE(Player.EXPENSES_PROPERTY);
		Player p = null;

		/* Find player that has HQ flooded, and reset their location_of_house */
		if (PlayerID.getCurrent().isWater()) {
			boolean dodelete = false;

			Iterator<Player> ii = Player.getIterator();
			while(ii.hasNext())
			{
				Player pp = ii.next();
				if (pp.location_of_house.equals(tile)) {
					dodelete = true;
					break;
				}
			}
			if (!dodelete) return Cmd.CMD_ERROR;
		} else /* Destruction was initiated by player */
			p = Player.GetCurrentPlayer();

		if (p.location_of_house == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			Landscape.DoClearSquare(p.location_of_house.iadd(0, 0));
			Landscape.DoClearSquare(p.location_of_house.iadd(0, 1));
			Landscape.DoClearSquare(p.location_of_house.iadd(1, 0));
			Landscape.DoClearSquare(p.location_of_house.iadd(1, 1));
			p.location_of_house = null; // reset HQ position
			Window.InvalidateWindow(Window.WC_COMPANY, p.index);
		}

		assert p != null;

		// cost of relocating company is 1% of company value
		return (int) (Economy.CalculateCompanyValue(p) / 100);
	}

	/** Build or relocate the HQ. This depends if the HQ is already built or not
	 * @param x,y the coordinates where the HQ will be built or relocated to
	 * @param p1 unused
	 * @param p2 unused
	 */
	//extern int CheckFlatLandBelow(TileIndex tile, int w, int h, int flags, int invalid_dirs, int *);
	static int CmdBuildCompanyHQ(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		Player p = Player.GetCurrentPlayer();
		int cost;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_PROPERTY);

		cost = Station.CheckFlatLandBelow(tile, 2, 2, flags, 0, null);
		if (Cmd.CmdFailed(cost)) return Cmd.CMD_ERROR;

		if (p.location_of_house != null) { /* Moving HQ */
			int ret = DestroyCompanyHQ(p.location_of_house, flags);
			if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
			cost += ret;
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			int score = Economy.UpdateCompanyRatingAndValue(p, false);

			p.location_of_house = tile;

			Landscape.ModifyTile(tile.iadd(0, 0), TileTypes.MP_UNMOVABLE, 
					//TileTypes.MP_SETTYPE(TileTypes.MP_UNMOVABLE) | 
					TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5, 0x80);
			Landscape.ModifyTile(tile.iadd(0, 1), TileTypes.MP_UNMOVABLE, 
					//TileTypes.MP_SETTYPE(TileTypes.MP_UNMOVABLE) | 
					TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5, 0x81);
			Landscape.ModifyTile(tile.iadd(1, 0), TileTypes.MP_UNMOVABLE, 
					//TileTypes.MP_SETTYPE(TileTypes.MP_UNMOVABLE) | 
					TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5, 0x82);
			Landscape.ModifyTile(tile.iadd(1, 1), TileTypes.MP_UNMOVABLE, 
					//TileTypes.MP_SETTYPE(TileTypes.MP_UNMOVABLE) | 
					TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5, 0x83);
			Economy.UpdatePlayerHouse(p, score);
			Window.InvalidateWindow(Window.WC_COMPANY, p.index.id);
		}

		return cost;
	}


	static void DrawTile_Unmovable(TileInfo ti)
	{
		int image, ormod;

		if (0==(ti.map5 & 0x80)) 
		{
			if (ti.map5 == 2) {

				// statue
				ViewPort.DrawGroundSprite(Sprite.SPR_STATUE_GROUND);

				image = Sprite.PLAYER_SPRITE_COLOR(ti.tile.GetTileOwner());
				image += Sprite.PALETTE_MODIFIER_COLOR | Sprite.SPR_STATUE_COMPANY;
				if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS) )
					image = Sprite.RET_MAKE_TRANSPARENT(image);
				ViewPort.AddSortableSpriteToDraw(image, ti.x, ti.y, 16, 16, 25, ti.z);
			} else if (ti.map5 == 3) {

				// "owned by" sign
				Clear.DrawClearLandTile(ti, 0);

				ViewPort.AddSortableSpriteToDraw(
						Sprite.PLAYER_SPRITE_COLOR(ti.tile.GetTileOwner()) + Sprite.PALETTE_MODIFIER_COLOR + Sprite.SPR_BOUGHT_LAND,
						ti.x+8, ti.y+8,
						1, 1,
						10,
						Landscape.GetSlopeZ(ti.x+8, ti.y+8)
						);
			} else {
				// lighthouse or transmitter

				DrawTileUnmovableStruct dtus;

				if (ti.tileh != 0) Landscape.DrawFoundation(ti, ti.tileh);
				Clear.DrawClearLandTile(ti, 2);

				dtus = _draw_tile_unmovable_data[ti.map5];

				image = dtus.image;
				if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS) )
					image = Sprite.RET_MAKE_TRANSPARENT(image);

				ViewPort.AddSortableSpriteToDraw(image,
						ti.x | dtus.subcoord_x,
						ti.y | dtus.subcoord_y,
						dtus.width, dtus.height,
						dtus.z_size, ti.z);
			}
		} else {
			DrawTileSeqStruct dtss;
			final DrawTileSprites t;

			if(0 != (ti.tileh)) Landscape.DrawFoundation(ti, ti.tileh);

			ormod = Sprite.PLAYER_SPRITE_COLOR(ti.tile.GetTileOwner());

			t = _unmovable_display_datas[ti.map5 & 0x7F];
			ViewPort.DrawGroundSprite(t.ground_sprite | ormod);

			// #define foreach_draw_tile_seq(idx, list) for (idx = list; ((byte) idx->delta_x) != 0x80; idx++)
			//foreach_draw_tile_seq(dtss, t.seq)
			int pos;
			//for (dtss = t.seq; ((byte) dtss->delta_x) != 0x80; dtss++)
			for(pos = 0; pos < t.seq.length && (t.seq[pos].delta_x != 0x80); pos++)
			{
				//image = dtss.image;
				image = t.seq[pos].image;
				dtss = t.seq[pos];
				if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS) ) {
					image = Sprite.RET_MAKE_TRANSPARENT(image);
				} else {
					image |= ormod;
				}
				ViewPort.AddSortableSpriteToDraw(image, ti.x + dtss.delta_x, ti.y + dtss.delta_y,
						dtss.width, dtss.height, dtss.unk, ti.z + dtss.delta_z);
			}
		}
	}

	static int GetSlopeZ_Unmovable(final TileInfo  ti)
	{
		return Landscape.GetPartialZ(ti.x & 0xF, ti.y & 0xF, ti.tileh) + ti.z;
	}

	static int GetSlopeTileh_Unmovable(final TileInfo ti)
	{
		return 0;
	}

	static int ClearTile_Unmovable(TileIndex tile, byte flags)
	{
		int m5 = tile.getMap().m5;

		if(0 != (m5 & 0x80)) {
			if (PlayerID.getCurrent().isWater()) return DestroyCompanyHQ(tile, Cmd.DC_EXEC);
			return Cmd.return_cmd_error(Str.STR_5804_COMPANY_HEADQUARTERS_IN);
		}

		if (m5 == 3) // company owned land
			return Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_SELL_LAND_AREA);

		// checks if you're allowed to remove unmovable things
		if (Global._game_mode != GameModes.GM_EDITOR 
				&& !PlayerID.getCurrent().isWater()
				&& ((flags & Cmd.DC_AUTO) != 0 || !Global._cheats.magic_bulldozer.value) )
			return Cmd.return_cmd_error(Str.STR_5800_OBJECT_IN_THE_WAY);

		if(0 != (flags & Cmd.DC_EXEC)) {
			Landscape.DoClearSquare(tile);
		}

		return 0;
	}

	static AcceptedCargo GetAcceptedCargo_Unmovable(TileIndex tile)
	{
		AcceptedCargo ac = new AcceptedCargo();
		int m5 = tile.getMap().m5;
		int level; // HQ level (depends on company performance) in the range 1..5.

		if (0==(m5 & 0x80)) {
			/* not used */
			return ac;
		}

		/* HQ accepts passenger and mail; but we have to divide the values
		 * between 4 tiles it occupies! */

		level = (m5 & ~0x80) / 4 + 1;

		// Top town building generates 10, so to make HQ interesting, the top
		// type makes 20.
		ac.ct[AcceptedCargo.CT_PASSENGERS] = Math.max(1, level);

		// Top town building generates 4, HQ can make up to 8. The
		// proportion passengers:mail is different because such a huge
		// commercial building generates unusually high amount of mail
		// correspondence per physical visitor.
		ac.ct[AcceptedCargo.CT_MAIL] = Math.max(1, level / 2);

		return ac;
	}

	static final /*StringID*/ int _unmovable_tile_str[] = {
			Str.STR_5803_COMPANY_HEADQUARTERS,
			Str.STR_5801_TRANSMITTER,
			Str.STR_5802_LIGHTHOUSE,
			Str.STR_2016_STATUE,
			Str.STR_5805_COMPANY_OWNED_LAND,
	};

	static TileDesc GetTileDesc_Unmovable(TileIndex tile)
	{
		TileDesc td = new TileDesc();
		int i = tile.getMap().m5;
		if(0 != (i & 0x80)) i = -1;
		td.str = _unmovable_tile_str[i + 1];
		td.owner = tile.GetTileOwner().id;
		return td;
	}

	static void AnimateTile_Unmovable(TileIndex tile)
	{
		/* not used */
	}

	static void TileLoop_Unmovable(TileIndex tile)
	{
		int m5 = tile.getMap().m5;
		int level; // HQ level (depends on company performance) in the range 1..5.
		int r;

		if (0==(m5 & 0x80)) {
			/* not used */
			return;
		}

		/* HQ accepts passenger and mail; but we have to divide the values
		 * between 4 tiles it occupies! */

		level = BitOps.GB(m5, 0, 7) / 4 + 1;
		assert(level < 6);

		r = Hal.Random();
		// Top town buildings generate 250, so the top HQ type makes 256.
		if (BitOps.GB(r, 0, 8) < (256 / 4 / (6 - level))) {
			int amt = BitOps.GB(r, 0, 8) / 8 / 4 + 1;
			if (Global.gs._economy.getFluct() <= 0) amt = (amt + 1) >> 1;
			Station.MoveGoodsToStation(tile, 2, 2, AcceptedCargo.CT_PASSENGERS, amt);
		}

		// Top town building generates 90, HQ can make up to 196. The
		// proportion passengers:mail is about the same as in the acceptance
		// equations.
		if (BitOps.GB(r, 8, 8) < (196 / 4 / (6 - level))) {
			int amt = BitOps.GB(r, 8, 8) / 8 / 4 + 1;
			if (Global.gs._economy.getFluct() <= 0) amt = (amt + 1) >> 1;
			Station.MoveGoodsToStation(tile, 2, 2, AcceptedCargo.CT_MAIL, amt);
		}
	}


	static int GetTileTrackStatus_Unmovable(TileIndex tile, /*int*/ TransportType mode)
	{
		return 0;
	}

	static void ClickTile_Unmovable(TileIndex tile)
	{
		if(0 != (tile.getMap().m5 & 0x80)) {
			PlayerGui.ShowPlayerCompany(tile.GetTileOwner().id);
		}
	}

	static final TileIndexDiffC _tile_add[] = {
			new TileIndexDiffC( 1,  0),
			new TileIndexDiffC( 0,  1),
			new TileIndexDiffC(-1,  0),
			new TileIndexDiffC( 0, -1)
	};

	/* checks, if a radio tower is within a 9x9 tile square around tile */
	static boolean checkRadioTowerNearby(TileIndex itile)
	{
		TileIndex tile_s = itile.isub( TileIndex.TileDiffXY(4, 4) );

		boolean [] ret = {true};
		//BEGIN_TILE_LOOP(tile, 9, 9, tile_s)
		// already a radio tower here?
		TileIndex.forAll(9, 9, tile_s, (tile) ->
		{
			if (tile.IsTileType( TileTypes.MP_UNMOVABLE) && tile.getMap().m5 == 0)
			{
				ret[0] = false;
				return true; // break
			}
			return false;
		});
		//END_TILE_LOOP(tile, 9, 9, tile_s)
		return ret[0];
	}

	static void GenerateUnmovables()
	{
		int i,j;
		TileIndex tile;
		int r;
		int dir;
		IntContainer h = new IntContainer();

		if (GameOptions._opt.landscape == Landscape.LT_CANDY)
			return;

		/* add radio tower */
		i = Map.ScaleByMapSize(1000);
		j = Map.ScaleByMapSize(40); // maximum number of radio towers on the map
		do {
			tile = Hal.RandomTile();
			if (tile.IsTileType( TileTypes.MP_CLEAR) && tile.GetTileSlope(h) == 0 && h.v >= 32) {
				if(!checkRadioTowerNearby(tile))
					continue;
				tile.SetTileType(TileTypes.MP_UNMOVABLE);
				tile.getMap().m5 = 0;
				tile.SetTileOwner(Owner.OWNER_NONE);
				if (--j == 0)
					break;
			}
		} while (--i > 0);

		if (GameOptions._opt.landscape == Landscape.LT_DESERT)
			return;

		/* add lighthouses */
		i = Map.ScaleByMapSize1D((Hal.Random() & 3) + 7);
		do {
			//restart:
			r = Hal.Random();
			dir = r >>> 30;
			r %= (dir == 0 || dir == 2) ? Global.MapMaxY() : Global.MapMaxX();
			
			final int d3 = (dir == 3) ? TileIndex.TileXY(r, Global.MapMaxY()).tile : 0; // right
			final int d2 = (dir == 2) ? TileIndex.TileXY(Global.MapMaxX(), r).tile : 0 + d3; // top 
			final int d1 = (dir == 1) ? TileIndex.TileXY(r, 0).tile : 0 + d2; // bottom 
			int itile = (dir == 0) ? TileIndex.TileXY(0, r).tile   : 0 + d1; // left
  
			tile = new TileIndex(itile);
			j = 20;
			boolean restart = false;
			do {
				if (--j == 0)
				{
					//goto restart;
					//continue;
					restart = true;
					break;
				}

				tile = tile.iadd( TileIndex.ToTileIndexDiff(_tile_add[dir]) );
				tile.TILE_MASK();

			} while (!(tile.IsTileType( TileTypes.MP_CLEAR) && tile.GetTileSlope(h) == 0 && h.v <= 16));
			if(restart) continue;

			tile.TILE_ASSERT();

			tile.SetTileType(TileTypes.MP_UNMOVABLE);
			tile.getMap().m5 = 1;
			tile.SetTileOwner(Owner.OWNER_NONE);
			--i;
		} while (i > 0);
	}

	static void ChangeTileOwner_Unmovable(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		if (!tile.IsTileOwner(old_player)) return;

		if (tile.getMap().m5 == 3 && new_player.id != Owner.OWNER_SPECTATOR) {
			tile.SetTileOwner(new_player);
		} else {
			Landscape.DoClearSquare(tile);
		}
	}

	final static TileTypeProcs _tile_type_unmovable_procs = new TileTypeProcs(
			UnmovableCmd::DrawTile_Unmovable,             /* draw_tile_proc */
			UnmovableCmd::GetSlopeZ_Unmovable,            /* get_slope_z_proc */
			UnmovableCmd::ClearTile_Unmovable,            /* clear_tile_proc */
			UnmovableCmd::GetAcceptedCargo_Unmovable,     /* get_accepted_cargo_proc */
			UnmovableCmd::GetTileDesc_Unmovable,          /* get_tile_desc_proc */
			UnmovableCmd::GetTileTrackStatus_Unmovable,   /* get_tile_track_status_proc */
			UnmovableCmd::ClickTile_Unmovable,            /* click_tile_proc */
			UnmovableCmd::AnimateTile_Unmovable,          /* animate_tile_proc */
			UnmovableCmd::TileLoop_Unmovable,             /* tile_loop_clear */
			UnmovableCmd::ChangeTileOwner_Unmovable,      /* change_tile_owner_clear */
			null,                           /* get_produced_cargo_proc */
			null,                           /* vehicle_enter_tile_proc */
			null,                           /* vehicle_leave_tile_proc */
			UnmovableCmd::GetSlopeTileh_Unmovable        /* get_slope_tileh_proc */
			);

}
