package com.dzavalishin.game;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.IPoolItem;
import com.dzavalishin.ifaces.IPoolItemFactory;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.ifaces.TownActionProc;
import com.dzavalishin.ifaces.TownDrawTileProc;
import com.dzavalishin.struct.DrawTownTileStruct;
import com.dzavalishin.struct.FindLengthOfTunnelResult;
import com.dzavalishin.struct.Point;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.struct.TileIndexDiffC;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.IntContainer;
import com.dzavalishin.util.MemoryPool;
import com.dzavalishin.util.Strings;
import com.dzavalishin.util.TownTables;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.TownGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

public class Town implements IPoolItem, Serializable 
{

	private static final long serialVersionUID = 1L;

	private TileIndex xy;

	// Current population of people and amount of houses.
	int num_houses;
	int population;

	// Town name
	public int townnametype;
	public int townnameparts;

	// NOSAVE: Location of name sign, UpdateTownVirtCoord updates this.
	//transient 
	ViewportSign sign;

	// Makes sure we don't build certain house types twice.
	int flags12;

	// Which players have a statue?
	int statues;

	// Sort index in listings
	int sort_index_obsolete;

	// Player ratings as well as a mask that determines which players have a rating.
	int have_ratings;
	int unwanted[]; // how many months companies aren't wanted by towns (bribe)
	PlayerID exclusivity;        // which player has exslusivity
	int exclusive_counter;     // months till the exclusivity expires
	int ratings[];

	// Maximum amount of passengers and mail that can be transported.
	public int max_pass;
	public int max_mail;
	int new_max_pass;
	int new_max_mail;
	public int act_pass;
	public int act_mail;
	int new_act_pass;
	int new_act_mail;

	// Amount of passengers that were transported.
	int pct_pass_transported;
	int pct_mail_transported;

	// Amount of food and paper that was transported. Actually a bit mask would be enough.
	int act_food;
	int act_water;
	int new_act_food;
	int new_act_water;

	// Time until we rebuild a house.
	int time_until_rebuild;

	// When to grow town next time.
	int grow_counter;
	int growth_rate;

	// Fund buildings program in action?
	int fund_buildings_months;

	// Fund road refinalruction in action?
	int road_build_months;

	// Index in town array
	public int index;

	// NOSAVE: UpdateTownRadius updates this given the house count.
	int radius[];

	// flags 12 bits
	private static final int GROW_BIT            = 0x01;
	private static final int TOWN_HAS_CHURCH     = 0x02;
	private static final int TOWN_HAS_STADIUM    = 0x04;

	private void clear()
	{
		xy = null;
		sign = new ViewportSign();
		unwanted = new int[Global.MAX_PLAYERS];
		exclusivity = null;
		radius = null;
		population = 0;
		townnametype = 0;
		townnameparts = 0;
		ratings = new int[Global.MAX_PLAYERS];

		exclusive_counter = max_pass = max_mail = new_max_pass =
				new_max_mail = act_pass = act_mail = new_act_pass =
				new_act_mail = act_food = act_water = new_act_food =
				new_act_water = 0;

		num_houses = 0;
		index = 0;

		flags12 = statues = sort_index_obsolete = have_ratings =
				pct_pass_transported = pct_mail_transported = time_until_rebuild = 0;
		grow_counter = 0;
		growth_rate = 0;
		fund_buildings_months = 0;
		road_build_months = 0;
	}


	public static final byte [] _housetype_extra_flags  = TownTables._housetype_extra_flags;


	public Town() {
		radius = new int[5];
		unwanted = new int[Global.MAX_PLAYERS]; // how many months companies aren't wanted by towns (bribe)
		ratings = new int[Global.MAX_PLAYERS];
		clear();
	}





	static final IPoolItemFactory<Town> factory = new IPoolItemFactory<Town>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Town createObject() { return new Town(); }
	};

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	public TileIndex getXy() {
		return xy;
	}



	/**
	 * Check if a Town really exists.
	 */
	public boolean IsValidTown()
	{
		return xy != null; //xy.isValid();
	}

	public boolean isValid()
	{
		return xy != null; //xy.isValid();
	}

	/**
	 * Get the pointer to the town with index 'index'
	 */
	public static Town GetTown(int index)
	{
		return Global.gs._towns.GetItemFromPool(index);
	}

	/**
	 * Get the current size of the TownPool
	 */
	public static  int GetTownPoolSize()
	{
		return Global.gs._towns.total_items();
	}

	static  boolean IsTownIndex(int index)
	{
		return index >= 0 && index < GetTownPoolSize(); 
	}




















	/* This is the base "normal" number of towns on the 8x8 map, when
	 * one town should get grown per tick. The other numbers of towns
	 * are then scaled based on that. */
	private static final int TOWN_GROWTH_FREQUENCY = 23;




	// Local
	private static int _grow_town_result;
	private static int _cur_town_iter;
	private static int _cur_town_ctr;






	static void TownDrawHouseLift(final TileInfo ti)
	{
		ViewPort.AddChildSpriteScreen(0x5A3, 0xE, 0x3C - BitOps.GB(ti.tile.getMap().m1, 0, 7));
	}

	static final TownDrawTileProc[] _town_draw_tile_procs = {
			Town::TownDrawHouseLift
	};



	static void DrawTile_Town(TileInfo ti)
	{
		final DrawTownTileStruct dcts;
		int z;
		int image;

		/* Retrieve pointer to the draw town tile struct */
		{
			/* this "randomizes" on the (up to) 4 variants of a building */
			int gfx   = ti.tile.getMap().m4;
			int stage =  BitOps.GB(ti.tile.getMap().m3, 6, 2);
			int variant;
			variant  = ti.x >> 4;
		variant ^= ti.x >> 6;
		variant ^= ti.y >> 4;
		variant -= ti.y >> 6;
		variant &= 3;
		dcts = TownTables._town_draw_tile_data[gfx << 4 | variant << 2 | stage];
		}

		z =  ti.z;

		/* Add bricks below the house? */
		if(0 != (ti.tileh)) {
			ViewPort.AddSortableSpriteToDraw(Sprite.SPR_FOUNDATION_BASE + ti.tileh, ti.x, ti.y, 16, 16, 7, z);
			ViewPort.AddChildSpriteScreen(dcts.sprite_1, 0x1F, 1);
			z += 8;
		} else {
			/* Else draw regular ground */
			ViewPort.DrawGroundSprite(dcts.sprite_1);
		}

		/* Add a house on top of the ground? */
		image = dcts.sprite_2;
		if (image != 0) {
			if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) 
				image = Sprite.RET_MAKE_TRANSPARENT(image);

			ViewPort.AddSortableSpriteToDraw(image,
					ti.x + dcts.subtile_x,
					ti.y + dcts.subtile_y,
					dcts.width + 1,
					dcts.height + 1,
					dcts.dz,
					z);

			if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) return;
		}

		{
			int proc = dcts.proc - 1;

			if (proc >= 0) _town_draw_tile_procs[proc].accept(ti);
		}
	}

	static int GetSlopeZ_Town(final TileInfo  ti)
	{
		int z = Landscape.GetPartialZ(ti.x & 0xF, ti.y & 0xF, ti.tileh) + ti.z;
		if (ti.tileh != 0) z = (z & ~7) + 4;
		return z;
	}

	static int GetSlopeTileh_Town(final TileInfo ti)
	{
		return ti.tileh;
	}

	static void AnimateTile_Town(TileIndex tile)
	{
		int old;
		int a,b;

		if( 0 != (Global._tick_counter & 3)) return;

		// If the house is not one with a lift anymore, then stop this animating.
		// Not exactly sure when this happens, but probably when a house changes.
		// Before this was just a return...so it'd leak animated tiles..
		// That bug seems to have been here since day 1??
		if (0 == (TownTables._housetype_extra_flags[tile.getMap().m4] & 0x20)) {
			TextEffect.DeleteAnimatedTile(tile);
			return;
		}

		if (0 == ((old = tile.getMap().m1) & 0x80)) 
		{
			int i;

			tile.getMap().m1 |= 0x80;

			do {
				i = (Hal.Random() & 7) - 1;
			} while (i < 0 || i == 1 || i * 6 == old);

			tile.getMap().m5 = BitOps.RETSB(tile.getMap().m5, 0, 6, i);
		}

		a = BitOps.GB(tile.getMap().m1, 0, 7);
		b = BitOps.GB(tile.getMap().m5, 0, 6) * 6;
		a += (a < b) ? 1 : -1;
		tile.getMap().m1 = BitOps.RETSB(tile.getMap().m1, 0, 7, a);

		if (a == b) {
			tile.getMap().m1 &= 0x7F;
			tile.getMap().m5 &= 0x40;
			TextEffect.DeleteAnimatedTile(tile);
		}

		tile.MarkTileDirtyByTile();
	}


	static boolean IsCloseToTown(TileIndex tile, int dist)
	{
		for(Iterator<Town> i = getIterator(); i.hasNext();)
		{
			final Town t = i.next();
			if (t.isValid() && Map.DistanceManhattan(tile, t.xy) < dist) 
				return true;
		}
		return false;
	}

	void MarkTownSignDirty()
	{
		ViewPort.MarkAllViewportsDirty(
				sign.getLeft()-6,
				sign.getTop()-3,
				sign.getLeft()+sign.getWidth_1()*4+12,
				sign.getTop() + 45
				);
	}

	public void UpdateTownVirtCoord()
	{
		MarkTownSignDirty();

		Point pt = Point.RemapCoords2(xy.TileX() * 16, xy.TileY() * 16);

		Global.SetDParam(0, index);
		Global.SetDParam(1, population);

		ViewPort.UpdateViewportSignPos(sign, pt.x, pt.y - 24,
				Global._patches.population_in_label ? Str.STR_TOWN_LABEL_POP : Str.STR_TOWN_LABEL);

		MarkTownSignDirty();
	}

	void ChangePopulation(int mod)
	{
		population += mod;
		Window.InvalidateWindow(Window.WC_TOWN_VIEW, index);
		UpdateTownVirtCoord();

		if(0 != (TownGui._town_sort_order & 2)) TownGui._town_sort_dirty = true;
	}

	public static int GetWorldPopulation()
	{
		int [] pop = {0};
		Town.forEach( (t) -> pop[0] += t.population );
		return pop[0];
	}

	static void MakeSingleHouseBigger(TileIndex tile)
	{
		assert(tile.IsTileType( TileTypes.MP_HOUSE));

		if( 0!= (tile.getMap().m5 & 0x80)) return;

		tile.getMap().m5 = BitOps.RETAB(tile.getMap().m5, 0, 3, 1);
		if (BitOps.GB(tile.getMap().m5, 0, 3) != 0) return;

		tile.getMap().m3 = 0xFF & (tile.getMap().m3 + 0x40);

		if ((tile.getMap().m3 & 0xC0) == 0xC0) {
			GetTown(tile.getMap().m2).ChangePopulation(TownTables._housetype_population[tile.getMap().m4]);
		}
		tile.MarkTileDirtyByTile();
	}

	static void MakeTownHouseBigger(TileIndex tile)
	{
		int flags = TownTables._house_more_flags[tile.getMap().m4];
		if( 0 != (flags & 8)) MakeSingleHouseBigger(tile.iadd(0, 0));
		if( 0 != (flags & 4)) MakeSingleHouseBigger(tile.iadd(0, 1));
		if( 0 != (flags & 2)) MakeSingleHouseBigger(tile.iadd(1, 0));
		if( 0 != (flags & 1)) MakeSingleHouseBigger(tile.iadd(1, 1));
	}

	static void TileLoop_Town(TileIndex tile)
	{
		int house;
		Town t;
		int r;

		if ((tile.getMap().m3 & 0xC0) != 0xC0) {
			MakeTownHouseBigger(tile);
			return;
		}

		house = tile.getMap().m4;
		if ( 0 != (TownTables._housetype_extra_flags[house] & 0x20) &&
				0 == (tile.getMap().m5 & 0x80) &&
				BitOps.CHANCE16(1, 2) &&
				TextEffect.AddAnimatedTile(tile)) 
		{
			tile.getMap().m5 = ((tile.getMap().m5 & 0x40) | 0x80);
		}

		t = GetTown(tile.getMap().m2);

		r = Hal.Random();

		if (BitOps.GB(r, 0, 8) < TownTables._housetype_population[house]) {
			int amt = BitOps.GB(r, 0, 8) / 8 + 1;
			int moved;

			if (Global.gs._economy.getFluct() <= 0) amt = (amt + 1) >> 1;
			t.new_max_pass += amt;
			moved = Station.MoveGoodsToStation(tile, 1, 1, AcceptedCargo.CT_PASSENGERS, amt);
			t.new_act_pass += moved;
		}

		if (BitOps.GB(r, 8, 8) < TownTables._housetype_mailamount[house] ) {
			int amt = BitOps.GB(r, 8, 8) / 8 + 1;
			int moved;

			if (Global.gs._economy.getFluct() <= 0) amt = (amt + 1) >> 1;
			t.new_max_mail += amt;
			moved = Station.MoveGoodsToStation(tile, 1, 1, AcceptedCargo.CT_MAIL, amt);
			t.new_act_mail += moved;
		}

		if ( 0 != (TownTables._house_more_flags[house] & 8) && 0 != (t.flags12 & GROW_BIT) && --t.time_until_rebuild == 0) {
			t.time_until_rebuild =  (BitOps.GB(r, 16, 6) + 130);

			PlayerID.setCurrent( PlayerID.get( Owner.OWNER_TOWN ) );

			t.ClearTownHouse(tile);

			// rebuild with another house?
			if (BitOps.GB(r, 24, 8) >= 12) DoBuildTownHouse(t, tile);

			PlayerID.setCurrentToNone();
		}
	}

	static void ClickTile_Town(TileIndex tile)
	{
		/* not used */
	}

	static int ClearTile_Town(TileIndex tile, byte flags)
	{
		int house, rating;
		int cost;
		Town t;

		// safety checks
		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;
		if (0 != (flags&Cmd.DC_AUTO) && 0 == (flags&Cmd.DC_AI_BUILDING)) 
			return Cmd.return_cmd_error(Str.STR_2004_BUILDING_MUST_BE_DEMOLISHED);

		house = tile.getMap().m4;
		cost = ((int)(Global._price.remove_house * TownTables._housetype_remove_cost[house])) >> 8;

			rating = TownTables._housetype_remove_ratingmod[house];
			Global._cleared_town_rating += rating;
			Global._cleared_town = t = GetTown(tile.getMap().m2);

			if (!PlayerID.getCurrent().isSpecial()) {
				if (rating > t.ratings[PlayerID.getCurrent().id] 
						&& 0==(flags & Cmd.DC_NO_TOWN_RATING) 
						&& !Global._cheats.magic_bulldozer.value) {
					Global.SetDParam(0, t.index);
					return Cmd.return_cmd_error(Str.STR_2009_LOCAL_AUTHORITY_REFUSES);
				}
			}

			if(0 != (flags & Cmd.DC_EXEC)) {
				t.ChangeTownRating(-rating, TownTables.RATING_HOUSE_MINIMUM);
				t.ClearTownHouse(tile);
			}

			return cost;
	}

	static AcceptedCargo GetAcceptedCargo_Town(TileIndex tile)
	{
		AcceptedCargo ac = new AcceptedCargo();
		int type = tile.getMap().m4;

		ac.ct[AcceptedCargo.CT_PASSENGERS] = TownTables._housetype_cargo_passengers[type];
		ac.ct[AcceptedCargo.CT_MAIL]       = TownTables._housetype_cargo_mail[type];
		ac.ct[AcceptedCargo.CT_GOODS]      = TownTables._housetype_cargo_goods[type];
		ac.ct[AcceptedCargo.CT_FOOD]       = TownTables._housetype_cargo_food[type];

		return ac;
	}

	static TileDesc GetTileDesc_Town(TileIndex tile)
	{
		TileDesc td = new TileDesc();
		td.str = TownTables._town_tile_names[tile.getMap().m4];
		if ((tile.getMap().m3 & 0xC0) != 0xC0) {
			Global.SetDParamX(td.dparam, 0, td.str);
			td.str = Str.STR_2058_UNDER_CONSTRUCTION;
		}

		td.owner = Owner.OWNER_TOWN;
		return td;
	}

	static int GetTileTrackStatus_Town(TileIndex tile, TransportType mode)
	{
		/* not used */
		return 0;
	}

	static void ChangeTileOwner_Town(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		/* not used */
	}


	static final TileIndexDiffC _roadblock_tileadd[] = {
			new TileIndexDiffC( 0, -1),
			new TileIndexDiffC( 1,  0),
			new TileIndexDiffC( 0,  1),
			new TileIndexDiffC(-1,  0),

			// Store the first 3 elements again.
			// Lets us rotate without using &3.
			new TileIndexDiffC( 0, -1),
			new TileIndexDiffC( 1,  0),
			new TileIndexDiffC( 0,  1)
	};

	void TownTickHandler()
	{
		if(0 != (flags12 & GROW_BIT)) 
		{
			int i = grow_counter - 1;
			if (i < 0) {
				if (GrowTown()) {
					i = growth_rate;
				} else {
					i = 0;
				}
			}
			grow_counter =  i;
		}

		UpdateTownRadius();
	}

	static void OnTick_Town()
	{
		if (Global._game_mode == GameModes.GM_EDITOR) return;

		/* Make sure each town's tickhandler invocation frequency is about the
		 * same - TOWN_GROWTH_FREQUENCY - independent on the number of towns. */
		for (_cur_town_iter += GetTownPoolSize();
				_cur_town_iter >= TOWN_GROWTH_FREQUENCY;
				_cur_town_iter -= TOWN_GROWTH_FREQUENCY) {
			int i = _cur_town_ctr;
			Town t;

			if (++_cur_town_ctr >= GetTownPoolSize())
				_cur_town_ctr = 0;

			t = GetTown(i);

			if(t != null && t.isValid()) t.TownTickHandler();
		}
	}

	static byte GetTownRoadMask(TileIndex tile)
	{
		int b = Road.GetRoadBitsByTile(tile);
		byte r = 0;

		if(0 != (b & 0x01)) r |= 10;
		if(0 != (b & 0x02)) r |=  5;
		if(0 != (b & 0x04)) r |=  9;
		if(0 != (b & 0x08)) r |=  6;
		if(0 != (b & 0x10)) r |=  3;
		if(0 != (b & 0x20)) r |= 12;
		return r;
	}

	private static boolean has_roads_around(TileIndex tile, int dir)
	{
		TileIndex t1, t2;

		t1 = tile.iadd( TileIndex.ToTileIndexDiff(_roadblock_tileadd[dir+1]) );
		t1 = t1.iadd( TileIndex.ToTileIndexDiff(_roadblock_tileadd[dir+2]) );

		t2 = tile.iadd( TileIndex.ToTileIndexDiff(_roadblock_tileadd[dir+3]) );
		t2 = t2.iadd( TileIndex.ToTileIndexDiff(_roadblock_tileadd[dir+2]) );

		return 
				BitOps.HASBIT(GetTownRoadMask( tile.iadd(TileIndex.ToTileIndexDiff(_roadblock_tileadd[dir+1]))  ), dir^2) ||
				BitOps.HASBIT(GetTownRoadMask( tile.iadd(TileIndex.ToTileIndexDiff(_roadblock_tileadd[dir+3]))  ), dir^2) ||
				BitOps.HASBIT(GetTownRoadMask(t1), dir) ||
				BitOps.HASBIT(GetTownRoadMask(t2), dir);

	}


	static boolean IsRoadAllowedHere(TileIndex tile, int dir)
	{
		int k;
		int slope;

		// If this assertion fails, it might be because the world contains
		//  land at the edges. This is not ok.
		tile.TILE_ASSERT();

		for (;;) {
			// Check if there already is a road at this point?
			if (Road.GetRoadBitsByTile(tile) == 0) {
				// No, try to build one in the direction.
				// if that fails clear the land, and if that fails exit.
				// This is to make sure that we can build a road here later.
				if (Cmd.CmdFailed(Cmd.DoCommandByTile(tile, (dir&1) != 0?0xA:0x5, 0, Cmd.DC_AUTO, Cmd.CMD_BUILD_ROAD)) &&
						Cmd.CmdFailed(Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_AUTO, Cmd.CMD_LANDSCAPE_CLEAR)))
					return false;
			}

			slope = tile.GetTileSlope(null);
			if (slope == 0) {
				//no_slope:
				// Tile has no slope
				// Disallow the road if any neighboring tile has a road.
				/*
					if (BitOps.HASBIT(GetTownRoadMask(TILE_ADD(tile, ToTileIndexDiff(_roadblock_tileadd[dir+1]))), dir^2) ||
							BitOps.HASBIT(GetTownRoadMask(TILE_ADD(tile, ToTileIndexDiff(_roadblock_tileadd[dir+3]))), dir^2) ||
							BitOps.HASBIT(GetTownRoadMask(TILE_ADD(tile, ToTileIndexDiff(_roadblock_tileadd[dir+1]) + ToTileIndexDiff(_roadblock_tileadd[dir+2]))), dir) ||
							BitOps.HASBIT(GetTownRoadMask(TILE_ADD(tile, ToTileIndexDiff(_roadblock_tileadd[dir+3]) + ToTileIndexDiff(_roadblock_tileadd[dir+2]))), dir))
						return false;
				 */
				if(has_roads_around( tile, dir))
					return false;

				// Otherwise allow
				return true;
			}

			// If the tile is not a slope in the right direction, then
			// maybe terraform some.
			if( ( (k = (dir&1)) != 0 ?0xC:0x9 ) != slope && (k^0xF) != slope) {
				int r = Hal.Random();

				if (BitOps.CHANCE16I(1, 8, r) && !Global._generating_world) {
					int res;

					if (BitOps.CHANCE16I(1, 16, r)) {
						res = Cmd.DoCommandByTile(tile, slope, 0, Cmd.DC_EXEC | Cmd.DC_AUTO | Cmd.DC_NO_WATER,
								Cmd.CMD_TERRAFORM_LAND);
					} else {
						res = Cmd.DoCommandByTile(tile, slope^0xF, 1, Cmd.DC_EXEC | Cmd.DC_AUTO | Cmd.DC_NO_WATER,
								Cmd.CMD_TERRAFORM_LAND);
					}
					if (Cmd.CmdFailed(res) && BitOps.CHANCE16I(1, 3, r)) {
						// We can consider building on the slope, though.
						//goto no_slope;
						return !has_roads_around( tile, dir);
					}
				}
				return false;
			}
			return true;
		}
	}

	static boolean TerraformTownTile(TileIndex tile, int edges, int dir)
	{
		int r;

		tile.TILE_ASSERT();

		r = Cmd.DoCommandByTile(tile, edges, dir, Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_TERRAFORM_LAND);
		if (Cmd.CmdFailed(r) || r >= 126 * 16) return false;
		Cmd.DoCommandByTile(tile, edges, dir, Cmd.DC_AUTO | Cmd.DC_NO_WATER | Cmd.DC_EXEC, Cmd.CMD_TERRAFORM_LAND);
		return true;
	}

	static void LevelTownLand(TileIndex tile)
	{
		TileInfo ti = new TileInfo();

		tile.TILE_ASSERT();

		// Don't terraform if land is plain or if there's a house there.
		Landscape.FindLandscapeHeightByTile(ti, tile);
		if (ti.tileh == 0 || ti.type == TileTypes.MP_HOUSE.ordinal()) return;

		// First try up, then down
		if (!TerraformTownTile(tile, ~ti.tileh & 0xF, 1)) {
			TerraformTownTile(tile, ti.tileh & 0xF, 0);
		}
	}


	private static void build_road_and_exit(TileIndex tile, int rcmd, int t1index )
	{
		//Global.debug("build_road_and_exit @  %s", tile);
		final int cmd = Cmd.DoCommandByTile(tile, rcmd, /*t1.index*/ t1index, Cmd.DC_EXEC | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
		if (!Cmd.CmdFailed(cmd))
			_grow_town_result = -1;
	}

	static void GrowTownInTile(TileIndex [] tile_ptr, int mask, int block, Town t1)
	{
		int a,b,rcmd;
		TileIndex tmptile;
		TileInfo ti = new TileInfo();
		int i;
		int j;
		TileIndex tile = tile_ptr[0];

		tile.TILE_ASSERT();

		if (mask == 0) 
		{
			// Tile has no road. First reset the status counter
			// to say that this is the last iteration.
			_grow_town_result = 0;

			// Remove hills etc
			LevelTownLand(tile);

			// Is a road allowed here?
			if (!IsRoadAllowedHere(tile, block)) return;

			// Randomize new road block numbers
			a = block;
			b = block ^ 2;
			if (BitOps.CHANCE16(1, 4)) {
				do {
					a = BitOps.GB(Hal.Random(), 0, 2);
				} while(a == b);
			}

			if (!IsRoadAllowedHere(tile.iadd( TileIndex.ToTileIndexDiff(_roadblock_tileadd[a])), a)) 
			{
				// A road is not allowed to continue the randomized road,
				//   return if the road we're trying to build is curved.
				if (a != (b ^ 2)) return;

				// Return if neither side of the new road is a house
				if (!tile.iadd( TileIndex.ToTileIndexDiff(_roadblock_tileadd[a + 1])).IsTileType(TileTypes.MP_HOUSE) &&
						!tile.iadd( TileIndex.ToTileIndexDiff(_roadblock_tileadd[a + 3])).IsTileType(TileTypes.MP_HOUSE))
					return;

				// That means that the road is only allowed if there is a house
				//  at any side of the new road.
			}
			rcmd = (1 << a) + (1 << b);

		} else if (block < 5 && !BitOps.HASBIT(mask,block^2)) {
			// Continue building on a partial road.
			// Always OK.
			_grow_town_result = 0;
			rcmd = 1 << (block ^ 2);
		} else {

			// Reached a tunnel? Then continue at the other side of it.
			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && (tile.getMap().m5& ~3) == 4) {
				FindLengthOfTunnelResult flotr = Pathfind.FindLengthOfTunnel(tile, BitOps.GB(tile.getMap().m5, 0, 2));
				tile_ptr[0] = flotr.tile;
				return;
			}

			// For any other kind of tunnel/bridge, bail out.
			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)) return;

			// Possibly extend the road in a direction.
			// Randomize a direction and if it has a road, bail out.
			i = BitOps.GB(Hal.Random(), 0, 2);
			if (BitOps.HASBIT(mask, i)) return;

			// This is the tile we will reach if we extend to this direction.
			tmptile = tile.iadd( TileIndex.ToTileIndexDiff(_roadblock_tileadd[i]));

			// Don't do it if it reaches to water.
			if (tmptile.IsWaterTile()) return;

			// Build a house at the edge. 60% chance or
			//  always ok if no road allowed.
			if (!IsRoadAllowedHere(tmptile, i) || BitOps.CHANCE16(6, 10)) {
				// But not if there already is a house there.
				if (!tmptile.IsTileType(TileTypes.MP_HOUSE)) {
					// Level the land if possible
					LevelTownLand(tmptile);

					// And build a house.
					// Set result to -1 if we managed to build it.
					if (t1.BuildTownHouse(tmptile)) _grow_town_result = -1;
				}
				return;
			}

			_grow_town_result = 0;
			rcmd = 1 << i;
		}

		Landscape.FindLandscapeHeightByTile(ti, tile);

		// Return if a water tile
		if (ti.type == TileTypes.MP_WATER.ordinal() && ti.map5 == 0) return;

		// Determine direction of slope,
		//  and build a road if not a special slope.
		/*if ((i=0,ti.tileh != 3) &&
				(i++,ti.tileh != 9) &&
				(i++,ti.tileh != 12) &&
				(i++,ti.tileh != 6)) {
			//build_road_and_exit:
			//	if (!Cmd.CmdFailed(Cmd.DoCommandByTile(tile, rcmd, t1.index, Cmd.DC_EXEC | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD)))
			//		_grow_town_result = -1;
			build_road_and_exit();
			return;
		} */

		switch(ti.tileh)
		{
		case 3:		i = 0; break;
		case 9:		i = 1; break;
		case 12:	i = 2; break;
		case 6:		i = 3; break;

		default:
			build_road_and_exit(tile, rcmd, t1.index);
			return;
		}

		tmptile = tile;

		// Now it contains the direction of the slope
		j = -11;	// max 11 tile long bridges
		do {
			if (++j == 0)
			{
				//goto build_road_and_exit;
				build_road_and_exit(tile, rcmd, t1.index);
				return;
			}
			tmptile = tmptile.iadd( TileIndex.TileOffsByDir(i)).TILE_MASK();
		} while (tmptile.IsWaterTile());

		// no water tiles in between?
		if (j == -10)
		{
			//goto build_road_and_exit;
			build_road_and_exit(tile, rcmd, t1.index);
			return;
		}
		// Quit if it selecting an appropriate bridge type fails a large number of times.
		j = 22;
		{
			int bridge_len = TunnelBridgeCmd.GetBridgeLength(tile, tmptile);
			do {
				int bridge_type = Hal.RandomRange(Bridge.MAX_BRIDGES - 1);
				if (TunnelBridgeCmd.CheckBridge_Stuff(bridge_type, bridge_len)) {
					if (!Cmd.CmdFailed(Cmd.DoCommandByTile(tile, tmptile.tile, 0x8000 + bridge_type, Cmd.DC_EXEC | Cmd.DC_AUTO, Cmd.CMD_BUILD_BRIDGE)))
						_grow_town_result = -1;

					// obviously, if building any bridge would fail, there is no need to try other bridge-types
					return;
				}
			} while(--j != 0);
		}
	}



	/**
	 * Returns true if a house was built, or no if the build failed.
	 *  
	 * @param itile
	 * @return
	 */
	boolean GrowTownAtRoad(TileIndex itile)
	{
		int mask;
		int block = 5; // special case

		MutableTileIndex tile = new MutableTileIndex(itile);

		tile.TILE_ASSERT();

		// Number of times to search.
		_grow_town_result = 10 + num_houses * 4 / 9;

		do {
			// Get a bitmask of the road blocks on a tile
			mask = GetTownRoadMask(tile);

			// Try to grow the town from this point
			{
				TileIndex [] tip = { tile };				
				GrowTownInTile(tip,mask,block,this);
				tile = new MutableTileIndex( tip[0] );
			}

			// Exclude the source position from the bitmask
			// and return if no more road blocks available
			mask = BitOps.RETCLRBIT(mask, (block ^ 2));
			if (mask == 0)
				return _grow_town_result != 0;

			// Select a random bit from the blockmask, walk a step
			// and continue the search from there.
			do block = Hal.Random() & 3; while (!BitOps.HASBIT(mask,block));
			tile.madd(TileIndex.ToTileIndexDiff(_roadblock_tileadd[block]));

			if (tile.IsTileType( TileTypes.MP_STREET)) {
				/* Don't allow building over roads of other cities */
				if (tile.IsTileOwner(Owner.OWNER_TOWN) && GetTown(tile.getMap().m2) != this)
					_grow_town_result = -1;
				else if (Global._game_mode == GameModes.GM_EDITOR) {
					/* If we are in the SE, and this road-piece has no town owner yet, it just found an
					 *  owner :) (happy happy happy road now) */
					tile.SetTileOwner(Owner.OWNER_TOWN);
					tile.getMap().m2 = this.index;
				}
			}

			// Max number of times is checked.
		} while (--_grow_town_result >= 0);

		return _grow_town_result == -2;
	}

	// Generate a random road block
	// The probability of a straight road
	// is somewhat higher than a curved.
	static int GenRandomRoadBits()
	{
		int r = Hal.Random();
		int a = BitOps.GB(r, 0, 2);
		int b = BitOps.GB(r, 8, 2);
		if (a == b) b ^= 2;
		return (1 << a) + (1 << b);
	}

	static final TileIndexDiffC _town_coord_mod[] = {
			new TileIndexDiffC(-1,  0),
			new TileIndexDiffC( 1,  1),
			new TileIndexDiffC( 1, -1),
			new TileIndexDiffC(-1, -1),
			new TileIndexDiffC(-1,  0),
			new TileIndexDiffC( 0,  2),
			new TileIndexDiffC( 2,  0),
			new TileIndexDiffC( 0, -2),
			new TileIndexDiffC(-1, -1),
			new TileIndexDiffC(-2,  2),
			new TileIndexDiffC( 2,  2),
			new TileIndexDiffC( 2, -2),
			new TileIndexDiffC( 0,  0)
	};


	private boolean doGrowTown()
	{
		TileIndex tile;
		TileInfo ti = new TileInfo();

		// Find a road that we can base the construction on.
		tile = xy;

		for (TileIndexDiffC ptr : _town_coord_mod) 
		{
			if (Road.GetRoadBitsByTile(tile) != 0) 
				return GrowTownAtRoad(tile);
			
			tile = tile.iadd(TileIndex.ToTileIndexDiff(ptr));
		}

		// No road available, try to build a random road block by
		// clearing some land and then building a road there.
		tile = xy;

		for (TileIndexDiffC ptr : _town_coord_mod) 
		{
			Landscape.FindLandscapeHeightByTile(ti, tile);

			// Only work with plain land that not already has a house with map5=0
			if (ti.tileh == 0 && (ti.type != TileTypes.MP_HOUSE.ordinal() || ti.map5 != 0)) {
				if (!Cmd.CmdFailed(Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_AUTO, Cmd.CMD_LANDSCAPE_CLEAR))) {
					Cmd.DoCommandByTile(tile, GenRandomRoadBits(), this.index, Cmd.DC_EXEC | Cmd.DC_AUTO, Cmd.CMD_BUILD_ROAD);
					return true;
				}
			}
			tile = tile.iadd(TileIndex.ToTileIndexDiff(ptr));
		}

		return false;
	}
	
	
	//static boolean disableGrow = true;
	// Grow the town
	// Returns true if a house was built, or no if the build failed.
	boolean GrowTown()
	{
		//if(disableGrow) return false;

		// Current player is a town
		PlayerID old_player = PlayerID.getCurrent();
		PlayerID.setCurrent( Owner.OWNER_TOWN_ID );

		boolean r = doGrowTown();

		PlayerID.setCurrent( old_player );
		return r;
	}

	static final int _town_radius_data[][] = {
			{ 4,  0,  0,  0,  0}, // 0
			{16,  0,  0,  0,  0},
			{25,  0,  0,  0,  0},
			{36,  0,  0,  0,  0},
			{49,  0,  4,  0,  0},
			{64,  0,  4,  0,  0}, // 20
			{64,  0,  9,  0,  1},
			{64,  0,  9,  0,  4},
			{64,  0, 16,  0,  4},
			{81,  0, 16,  0,  4},
			{81,  0, 16,  0,  4}, // 40
			{81,  0, 25,  0,  9},
			{81, 36, 25,  0,  9},
			{81, 36, 25, 16,  9},
			{81, 49,  0, 25,  9},
			{81, 64,  0, 25,  9}, // 60
			{81, 64,  0, 36,  9},
			{81, 64,  0, 36, 16},
			{100, 81,  0, 49, 16},
			{100, 81,  0, 49, 25},
			{121, 81,  0, 49, 25}, // 80
			{121, 81,  0, 49, 25},
			{121, 81,  0, 49, 36}, // 88
	};

	void UpdateTownRadius()
	{

		if (num_houses < 92) {
			radius = new int[5];			
			System.arraycopy(_town_radius_data[num_houses / 4], 0, radius, 0, radius.length);
		} else {
			int mass = num_houses / 8;
			// At least very roughly extrapolate. Empirical numbers dancing between
			// overwhelming by cottages and skyscrapers outskirts.
			radius[0] = mass * mass;
			// Actually we are proportional to sqrt() but that's right because
			// we are covering an area.
			radius[1] = mass * 7;
			radius[2] = 0;
			radius[3] = mass * 4;
			radius[4] = mass * 3;
			//debug("%d (.%d): %d %d %d %d\n", t.num_houses, mass, t.radius[0], t.radius[1], t.radius[3], t.radius[4]);
		}
	}

	static boolean CreateTownName(int []townnameparts)
	{
		//Town t2;
		String buf1;
		String buf2;
		int r;
		/* Do not set too low tries, since when we run out of names, we loop
		 * for #tries only one time anyway - then we stop generating more
		 * towns. Do not show it too high neither, since looping through all
		 * the other towns may take considerable amount of time (10000 is
		 * too much). */
		int tries = 1000;
		int townnametype = Strings.SPECSTR_TOWNNAME_START + GameOptions._opt.town_name;

		assert(townnameparts != null);

		for(;;) {
			//restart:
			r = Hal.Random();

			Global.SetDParam(0, r);
			buf1 = Strings.GetString(townnametype);

			// Check size and width
			//if (strlen(buf1) >= 31 || Global.GetStringWidth(buf1) > 130) continue;
			if (Gfx.GetStringWidth(buf1) > 130) continue; // TODO Why?

			//FOR_ALL_TOWNS(t2)
			Iterator<Town> it = Town.getIterator();
			boolean same = false; 
			while(it.hasNext())
			{
				Town t2 = it.next();
				if (t2.isValid()) {
					// We can't just compare the numbers since
					// several numbers may map to a single name.
					Global.SetDParam(0, t2.index);
					buf2 = Strings.GetString(Str.STR_TOWN);
					if (buf1.equals(buf2)) {
						if (tries-- < 0) return false;
						//goto restart;
						same = true;
						break;
					}
				}
			}

			if(same) continue;

			townnameparts[0] = r;
			return true;
		}
	}

	void UpdateTownMaxPass()
	{
		max_pass = population >> 3;
						max_mail = population >> 4;
	}

	static void DoCreateTown(Town t, TileIndex tile, int townnameparts)
	{
		int x, i;

		// clear the town struct
		i = t.index;

		t.clear();
		t.index = i;

		t.xy = tile;
		t.num_houses = 0;
		t.time_until_rebuild = 10;
		t.UpdateTownRadius();
		t.flags12 = 0;
		t.population = 0;
		t.grow_counter = 0;
		t.growth_rate = 250;
		t.new_max_pass = 0;
		t.new_max_mail = 0;
		t.new_act_pass = 0;
		t.new_act_mail = 0;
		t.max_pass = 0;
		t.max_mail = 0;
		t.act_pass = 0;
		t.act_mail = 0;

		t.pct_pass_transported = 0;
		t.pct_mail_transported = 0;
		t.fund_buildings_months = 0;
		t.new_act_food = 0;
		t.new_act_water = 0;
		t.act_food = 0;
		t.act_water = 0;

		//for(i = 0; i != Global.MAX_PLAYERS; i++)			t.ratings[i] = 500;
		Arrays.fill(t.ratings, 500);

		t.have_ratings = 0;
		t.exclusivity = PlayerID.get(-1);
		t.exclusive_counter = 0;
		t.statues = 0;

		t.townnametype = Strings.SPECSTR_TOWNNAME_START + GameOptions._opt.town_name;
		t.townnameparts = townnameparts;

		t.UpdateTownVirtCoord();
		TownGui._town_sort_dirty = true;

		x = (Hal.Random() & 0xF) + 8;
		if (Global._game_mode == GameModes.GM_EDITOR)
			x = Global._new_town_size * 16 + 3;

		t.num_houses += x;
		t.UpdateTownRadius();

		i = x * 4;
		do {
			t.GrowTown();
		} while (--i > 0);

		t.num_houses -= x;
		t.UpdateTownRadius();
		t.UpdateTownMaxPass();
	}

	static Town AllocateTown()
	{
		Iterator<Town> it = Town.getIterator();
		while(it.hasNext())
		{
			Town t = it.next();
			if (!t.isValid()) {
				int index = t.index;

				//if (t.index > _total_towns)					_total_towns = t.index;

				t.clear();
				t.index = index;

				return t;
			}
		}

		/* Check if we can add a block to the pool */
		if (Global.gs._towns.AddBlockToPool())
			return AllocateTown();

		return null;
	}

	/** Create a new town.
	 * This obviously only works in the scenario editor. Function not removed
	 * as it might be possible in the future to fund your own town :)
	 * @param x,y coordinates where town is built
	 * @param p1 unused
	 * @param p2 unused
	 */
	static int CmdBuildTown(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		TileInfo ti = new TileInfo();
		Town t;
		int [] townnameparts = { 0 };

		/* Only in the scenario editor */
		if (Global._game_mode != GameModes.GM_EDITOR) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_OTHER);

		// Check if too close to the edge of map
		if (Map.DistanceFromEdge(tile) < 12)
			return Cmd.return_cmd_error(Str.STR_0237_TOO_CLOSE_TO_EDGE_OF_MAP);

		// Can only build on clear flat areas.
		Landscape.FindLandscapeHeightByTile(ti, tile);
		if (ti.type != TileTypes.MP_CLEAR.ordinal() || ti.tileh != 0)
			return Cmd.return_cmd_error(Str.STR_0239_SITE_UNSUITABLE);

		// Check distance to all other towns.
		if (IsCloseToTown(tile, 20))
			return Cmd.return_cmd_error(Str.STR_0238_TOO_CLOSE_TO_ANOTHER_TOWN);

		// Get a unique name for the town.
		if (!CreateTownName(townnameparts))
			return Cmd.return_cmd_error(Str.STR_023A_TOO_MANY_TOWNS);

		// Allocate town struct
		t = AllocateTown();
		if (t == null) return Cmd.return_cmd_error(Str.STR_023A_TOO_MANY_TOWNS);

		// Create the town
		if(0 != (flags & Cmd.DC_EXEC)) {
			Global._generating_world = true;
			DoCreateTown(t, tile, townnameparts[0]);
			Global._generating_world = false;
		}
		return 0;
	}

	public static Town CreateRandomTown(int attempts)
	{
		TileIndex tile;
		TileInfo ti = new TileInfo();
		Town t;
		int [] townnameparts = { 0 };

		do {
			// Generate a tile index not too close from the edge
			tile = Hal.RandomTile();
			if (Map.DistanceFromEdge(tile) < 5) // TODO [dz] was 20
				continue;

			// Make sure the tile is plain
			Landscape.FindLandscapeHeightByTile(ti, tile);
			if (ti.type != TileTypes.MP_CLEAR.ordinal() || ti.tileh != 0)
				continue;

			// Check not too close to a town
			if (IsCloseToTown(tile, 20))
				continue;

			// Get a unique name for the town.
			if (!CreateTownName(townnameparts))
				break;

			// Allocate a town struct
			t = AllocateTown();
			if (t == null)
				break;

			DoCreateTown(t, tile, townnameparts[0]);
			return t;
		} while (--attempts > 0);
		return null;
	}

	static final byte _num_initial_towns[] = {11, 23, 46};

	public static boolean GenerateTowns()
	{
		int num = 0;
		int n = Map.ScaleByMapSize(_num_initial_towns[GameOptions._opt.diff.number_towns] + (Hal.Random() & 7));

		do {
			if (CreateRandomTown(40) != null) 	// TODO was 20 -- try 20 times for the first loop
				num++;
		} while (--n > 0);

		// give it a last try, but now more aggressive
		if (num == 0 && CreateRandomTown(10000) == null) {
			//Town t;
			//FOR_ALL_TOWNS(t) 
			Iterator<Town> ii = Town.getIterator();
			while(ii.hasNext())
			{ 
				Town t = ii.next();
				if (t.IsValidTown()) {num = 1; break;}
			}

			//XXX can we handle that more gracefully?
			if (num == 0) Global.error("Could not generate any town");
			return false;
		}

		return true;
	}

	static final byte _masks[] = {
			0xC,0x3,0x9,0x6,
			0x3,0xC,0x6,0x9,
	};

	static boolean CheckBuildHouseMode(Town t1, TileIndex tile, int tileh, int mode)
	{
		int b;
		int slope;


		slope = tile.GetTileSlope(null);
		if(0 != (slope & 0x10))
			return false;

		b = 0;
		if ( 0 != (slope & 0xF) && 0 != (~slope & _masks[mode])) b = ~b;
		if ( 0 != (tileh & 0xF) && 0 != (~tileh & _masks[mode+4])) b = ~b;
		if (b != 0)
			return false;

		return !Cmd.CmdFailed(Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_LANDSCAPE_CLEAR));
	}

	public int GetTownRadiusGroup( /*final Town t,*/ TileIndex tile)
	{
		int dist;
		int i,smallest;

		dist = Map.DistanceSquare(tile, xy);
		if (fund_buildings_months != 0 && dist <= 25)
			return 4;

		smallest = 0;
		for (i = 0; i != radius.length; i++) {
			if (dist < radius[i])
				smallest = i;
		}

		return smallest;
	}

	static final TileIndexDiffC _tile_add[] = {
			new TileIndexDiffC(0    , 0    ),
			new TileIndexDiffC(0 - 0, 1 - 0),
			new TileIndexDiffC(1 - 0, 0 - 1),
			new TileIndexDiffC(1 - 1, 1 - 0)
	};

	static boolean CheckFree2x2Area(Town t1, TileIndex tilep)
	{
		int i;

		MutableTileIndex tile = new MutableTileIndex(tilep);

		for(i=0; i!=4; i++) {
			tile.madd( TileIndex.ToTileIndexDiff(_tile_add[i]) );

			if (tile.GetTileSlope(null) != 0)
				return false;

			if (Cmd.CmdFailed(Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC | Cmd.DC_AUTO | Cmd.DC_NO_WATER | Cmd.DC_FORCETEST, Cmd.CMD_LANDSCAPE_CLEAR)))
				return false;
		}

		return true;
	}

	static void DoBuildTownHouse(Town t, TileIndex itile)
	{
		int i;
		int bitmask;
		int house;
		int slope;
		int z;
		int oneof;

		MutableTileIndex tile = new MutableTileIndex(itile);

		// Above snow?
		IntContainer zp = new IntContainer();
		slope = tile.GetTileSlope(zp);
		z = zp.v;

		// Get the town zone type
		{
			int rad = t.GetTownRadiusGroup(tile);

			int land = GameOptions._opt.landscape;
			if (land == Landscape.LT_HILLY && z >= GameOptions._opt.snow_line)
				land = -1;

			bitmask = (1 << rad) + (1 << (land + 12));
		}

		// bits 0-4 are used
		// bits 11-15 are used
		// bits 5-10 are not used.
		{
			byte [] houses = new byte[TownTables._housetype_flags.length];
			int num = 0;

			// Generate a list of all possible houses that can be built.
			for(i=0; i!=TownTables._housetype_flags.length; i++) {
				if ((~TownTables._housetype_flags[i] & bitmask) == 0)
					houses[num++] = (byte)i;
			}

			for(;;) {
				house = houses[Hal.RandomRange(num)];

				if (Global.get_cur_year() < TownTables._housetype_years[house].min || Global.get_cur_year() > TownTables._housetype_years[house].max)
					continue;

				// Special houses that there can be only one of.
				switch (house) {
				case TownTables.HOUSE_TEMP_CHURCH:
				case TownTables.HOUSE_ARCT_CHURCH:
				case TownTables.HOUSE_SNOW_CHURCH:
				case TownTables.HOUSE_TROP_CHURCH:
				case TownTables.HOUSE_TOY_CHURCH:
					oneof = TOWN_HAS_CHURCH;
					break;
				case TownTables.HOUSE_STADIUM:
				case TownTables.HOUSE_MODERN_STADIUM:
					oneof = TOWN_HAS_STADIUM;
					break;
				default:
					oneof = 0;
					break;
				}

				if(0 != (t.flags12 & oneof))
					continue;
				// Make sure there is no slope?
				if ( ((_housetype_extra_flags[house]&0x12) != 0) && slope != 0)
					continue;

				if(0 != (_housetype_extra_flags[house]&0x10)) {
					if (CheckFree2x2Area(t, tile) ||
							CheckFree2x2Area(t, (tile.madd(-1,  0))) ||
							CheckFree2x2Area(t, (tile.madd( 0, -1))) ||
							CheckFree2x2Area(t, (tile.madd( 1,  0))))
						break;
					tile.madd(0,1);
				} else if (0 != (_housetype_extra_flags[house]&4)) {
					if (CheckBuildHouseMode(t, tile.iadd(1, 0), slope, 0)) break;

					if (CheckBuildHouseMode(t, tile.iadd(-1, 0), slope, 1)) {
						tile.madd(-1, 0);
						break;
					}
				} else if(0 != (_housetype_extra_flags[house]&8)) {
					if (CheckBuildHouseMode(t, tile.iadd(0, 1), slope, 2)) break;

					if (CheckBuildHouseMode(t, tile.iadd(0, -1), slope, 3)) {
						tile.madd(0, -1);
						break;
					}
				} else
					break;
			}
		}

		t.num_houses++;

		// Special houses that there can be only one of.
		t.flags12 |= oneof;

		{
			int m3lo,m5,eflags;

			// ENDING_2
			m3lo = 0;
			m5 = 0;
			if (Global._generating_world) {
				int r = Hal.Random();

				// Value for map3lo
				m3lo = 0xC0;
				if (BitOps.GB(r, 0, 8) >= 220) m3lo &= (r>>8);

				if (m3lo == 0xC0)
					t.ChangePopulation(TownTables._housetype_population[house]);

				// Initial value for map5.
				m5 = BitOps.GB(r, 16, 6);
			}

			assert(tile.IsTileType( TileTypes.MP_CLEAR));

			Landscape.ModifyTile(tile, TileTypes.MP_HOUSE,
					//TileTypes.MP_SETTYPE(TileTypes.MP_HOUSE) | 
					TileTypes.MP_MAP3HI | TileTypes.MP_MAP3LO | TileTypes.MP_MAP2 | TileTypes.MP_MAP5 | TileTypes.MP_MAPOWNER,
					t.index,
					m3lo,   /* map3_lo */
					house,  /* map3_hi */
					0,     /* map_owner */
					m5		 /* map5 */
					);

			eflags = _housetype_extra_flags[house];

			if(0 != (eflags&0x18)) {
				assert(tile.iadd(0, 1).IsTileType(TileTypes.MP_CLEAR));
				Landscape.ModifyTile(tile.iadd(0, 1), TileTypes.MP_HOUSE,
						//TileTypes.MP_SETTYPE(TileTypes.MP_HOUSE) | 
						TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO | TileTypes.MP_MAP3HI | TileTypes.MP_MAP5 | TileTypes.MP_MAPOWNER,
						t.index,
						m3lo,			/* map3_lo */
						++house,	/* map3_hi */
						0,				/* map_owner */
						m5				/* map5 */
						);
			}

			if(0 != (eflags&0x14)) {
				assert(tile.iadd(1, 0).IsTileType(TileTypes.MP_CLEAR));
				Landscape.ModifyTile(tile.iadd(1, 0), TileTypes.MP_HOUSE,
						//TileTypes.MP_SETTYPE(TileTypes.MP_HOUSE) | 
						TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO | TileTypes.MP_MAP3HI | TileTypes.MP_MAP5 | TileTypes.MP_MAPOWNER,
						t.index,
						m3lo,			/* map3_lo */
						++house,	/* map3_hi */
						0,				/* map_owner */
						m5				/* map5 */
						);
			}

			if(0 != (eflags&0x10)) {
				assert(tile.iadd(1, 1).IsTileType(TileTypes.MP_CLEAR));
				Landscape.ModifyTile(tile.iadd(1, 1), TileTypes.MP_HOUSE,
						//TileTypes.MP_SETTYPE(TileTypes.MP_HOUSE) | 
						TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO | TileTypes.MP_MAP3HI | TileTypes.MP_MAP5 | TileTypes.MP_MAPOWNER,
						t.index,
						m3lo,			/* map3_lo */
						++house,	/* map3_hi */
						0,				/* map_owner */
						m5				/* map5 */
						);
			}
		}

		// ENDING
	}

	public boolean BuildTownHouse( /*Town t,*/ TileIndex tile)
	{
		int r;

		// make sure it's possible
		if (!tile.EnsureNoVehicle()) return false;
		if(0 != (tile.GetTileSlope(null) & 0x10)) return false;

		r = Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(r)) return false;

		DoBuildTownHouse(this, tile);
		return true;
	}


	static void DoClearTownHouseHelper(TileIndex tile)
	{
		assert(tile.IsTileType( TileTypes.MP_HOUSE));
		Landscape.DoClearSquare(tile);
		TextEffect.DeleteAnimatedTile(tile);
	}

	void ClearTownHouse(TileIndex tilep)
	{
		int house = tilep.getMap().m4;
		int eflags;

		assert(tilep.IsTileType( TileTypes.MP_HOUSE));

		MutableTileIndex tile = new MutableTileIndex(tilep);

		// need to align the tile to point to the upper left corner of the house
		if (house >= 3) { // house id 0,1,2 MUST be single tile houses, or this code breaks.
			if(0 != (_housetype_extra_flags[house-1] & 0x04)) {
				house--;
				tile.madd(-1, 0);
			} else if(0 != (_housetype_extra_flags[house-1] & 0x18)) {
				house--;
				tile.madd(0, -1);
			} else if(0 != (_housetype_extra_flags[house-2] & 0x10)) {
				house-=2;
				tile.madd(-1, 0);
			} else if(0 != (_housetype_extra_flags[house-3] & 0x10)) {
				house-=3;
				tile.madd(-1, -1);
			}
		}

		// Remove population from the town if the
		// house is finished.
		if ((~tile.getMap().m3 & 0xC0) == 0) {
			ChangePopulation(-TownTables._housetype_population[house]);
		}

		num_houses--;

		// Clear flags for houses that only may exist once/town.
		switch (house) {
		case TownTables.HOUSE_TEMP_CHURCH:
		case TownTables.HOUSE_ARCT_CHURCH:
		case TownTables.HOUSE_SNOW_CHURCH:
		case TownTables.HOUSE_TROP_CHURCH:
		case TownTables.HOUSE_TOY_CHURCH:
			flags12 &= ~TOWN_HAS_CHURCH;
			break;
		case TownTables.HOUSE_STADIUM:
		case TownTables.HOUSE_MODERN_STADIUM:
			flags12 &= ~TOWN_HAS_STADIUM;
			break;
		default:
			break;
		}

		// Do the actual clearing of tiles
		eflags = _housetype_extra_flags[house];
		DoClearTownHouseHelper(tile);
		if(0 != (eflags & 0x14)) DoClearTownHouseHelper(tile.iadd(1, 0));
		if(0 !=  (eflags & 0x18)) DoClearTownHouseHelper(tile.iadd(0, 1));
		if(0 !=  (eflags & 0x10)) DoClearTownHouseHelper(tile.iadd(1, 1));
	}

	/** Rename a town (server-only).
	 * @param x,y unused
	 * @param p1 town ID to rename
	 * @param p2 unused
	 */
	static int CmdRenameTown(int x, int y, int flags, int p1, int p2)
	{
		StringID str;
		Town t;

		if (!IsTownIndex(p1) || Global._cmd_text == null) return Cmd.CMD_ERROR;

		t = GetTown(p1);

		str = Global.AllocateNameUnique(Global._cmd_text, 4);
		//if (str == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			Global.DeleteName(t.townnametype);
			t.townnametype = str.id;

			t.UpdateTownVirtCoord();
			TownGui._town_sort_dirty = true;
			Station.UpdateAllStationVirtCoord();
			Hal.MarkWholeScreenDirty();
		} else {
			Global.DeleteName(str);
		}
		return 0;
	}

	// Called from GUI
	public void DeleteTown()
	{

		// Delete town authority window
		//  and remove from list of sorted towns
		Window.DeleteWindowById(Window.WC_TOWN_VIEW, index);
		TownGui._town_sort_dirty = true;

		// Delete all industries belonging to the town
		Industry.forEach( (i) ->
		{
			if (i.isValid() && i.townId == index)
				Industry.DeleteIndustry(i);
		});

		// Go through all tiles and delete those belonging to the town
		for (int itile = 0; itile < Global.MapSize(); ++itile) 
		{
			TileIndex tile = TileIndex.get(itile);
			switch (tile.GetTileType()) {
			case MP_HOUSE:
				if (GetTown(tile.getMap().m2) == this)
					Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
				break;

			case MP_STREET:
			case MP_TUNNELBRIDGE:
				if (tile.IsTileOwner(Owner.OWNER_TOWN) &&
						ClosestTownFromTile(tile, -1) == this)
					Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
				break;

			default:
				break;
			}
		}

		xy = null;
		Global.DeleteName(townnametype);

		Hal.MarkWholeScreenDirty();
	}

	// Called from GUI
	public void ExpandTown()
	{
		int amount, n;

		Global._generating_world = true;

		/* The more houses, the faster we grow */
		amount = Hal.RandomRange(num_houses / 10) + 3;
		num_houses += amount;
		UpdateTownRadius();

		n = amount * 10;
		do GrowTown(); while (--n > 0);

		num_houses -= amount;
		UpdateTownRadius();

		UpdateTownMaxPass();
		Global._generating_world = false;
	}

	public final static int _town_action_costs[] = {
			2, 4, 9, 35, 48, 53, 117, 175
	};


	static final byte _advertising_amount[] = {0x40, 0x70, (byte)0xA0};
	static final byte _advertising_radius[] = {10,15,20};

	static void TownActionAdvertise(Town t, int action)
	{
		Station.ModifyStationRatingAround(t.xy, PlayerID.getCurrent(),
				_advertising_amount[action],
				_advertising_radius[action]);
	}

	static void TownActionRoadRebuild(Town t, int action)
	{
		final Player  p;

		t.road_build_months = 6;

		Global.SetDParam(0, t.index);

		p = Player.GetCurrentPlayer();
		Global.SetDParam(1, p.name_1);
		Global.SetDParam(2, p.name_2);

		NewsItem.AddNewsItem(Str.STR_2055_TRAFFIC_CHAOS_IN_ROAD_REBUILDING,
				NewsItem.NEWS_FLAGS(NewsItem.NM_NORMAL, NewsItem.NF_TILE, NewsItem.NT_GENERAL, 0), t.xy.getTile(), 0);
	}

	static boolean DoBuildStatueOfCompany(TileIndex tile)
	{
		TileInfo ti = new TileInfo();
		PlayerID old;
		int r;

		Landscape.FindLandscapeHeightByTile(ti, tile);
		if (ti.tileh != 0) return false;

		if (ti.type != TileTypes.MP_HOUSE.ordinal() && ti.type != TileTypes.MP_CLEAR.ordinal() && ti.type != TileTypes.MP_TREES.ordinal())
			return false;


		old = PlayerID.getCurrent();
		PlayerID.setCurrentToNone();
		r = Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
		PlayerID.setCurrent( old );

		if (Cmd.CmdFailed(r)) return false;

		Landscape.ModifyTile(tile, TileTypes.MP_UNMOVABLE,
				//TileTypes.MP_SETTYPE(TileTypes.MP_UNMOVABLE) | 
				TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5,
				2 /* map5 */
				);

		return true;
	}

	// Layouted as an outward spiral
	static final TileIndexDiffC _statue_tiles[] = {
			new TileIndexDiffC(-1, 0),
			new TileIndexDiffC( 0, 1),
			new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0),
			new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1),
			new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0),
			new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1),
			new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0),
			new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1),
			new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0),
			new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1),
			new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0),
			new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1),
			new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0),
			new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1), new TileIndexDiffC( 0, 1),
			new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0), new TileIndexDiffC( 1, 0),
			new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1), new TileIndexDiffC( 0,-1),
			new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0), new TileIndexDiffC(-1, 0),
			new TileIndexDiffC( 0, 0)
	};

	public void TownActionBuildStatue(int action)
	{
		TileIndex tile = xy;

		statues = BitOps.RETSETBIT(statues, PlayerID.getCurrent().id);

		for (TileIndexDiffC statue_tile : _statue_tiles) {
			if (DoBuildStatueOfCompany(tile)) return;
			tile = tile.iadd(TileIndex.ToTileIndexDiff(statue_tile));
		}
	}

	static void TownActionFundBuildings(Town t, int action)
	{
		t.grow_counter = 1;
		t.flags12 |= GROW_BIT;
		t.fund_buildings_months = 3;
	}

	static void TownActionBuyRights(Town t, int action)
	{
		t.exclusive_counter = 12;
		t.exclusivity = PlayerID.getCurrent();

		Station.ModifyStationRatingAround(t.xy, PlayerID.getCurrent(), 130, 17);
	}

	static void TownActionBribe(Town t, int action)
	{
		if (0==Hal.RandomRange(15)) 
		{
			// set as unwanted for 6 months
			t.unwanted[PlayerID.getCurrent().id] = 6;

			// set all close by station ratings to 0
			Station.forEach( (st) ->
			{
				if(st.town == t && st.owner.isCurrentPlayer()) 
				{
					for (int i = 0; i != AcceptedCargo.NUM_CARGO; i++) 
						st.goods[i].rating = 0;
				}
			});

			// only show errormessage to the executing player. All errors are handled command.c
			// but this is special, because it can only 'fail' on a Cmd.DC_EXEC
			if (Player.IsLocalPlayer()) Global.ShowErrorMessage(Str.STR_BRIBE_FAILED_2, Str.STR_BRIBE_FAILED, 0, 0);

			/*	decrease by a lot!
			 *	ChangeTownRating is only for stuff in demolishing. Bribe failure should
			 *	be independent of any cheat settings
			 */
			if (t.ratings[PlayerID.getCurrent().id] > TownTables.RATING_BRIBE_DOWN_TO) {
				t.ratings[PlayerID.getCurrent().id] = TownTables.RATING_BRIBE_DOWN_TO;
			}
		} else {
			t.ChangeTownRating(TownTables.RATING_BRIBE_UP_STEP, TownTables.RATING_BRIBE_MAXIMUM);
		}
	}

	static final TownActionProc[] _town_action_proc = {
			Town::TownActionAdvertise,
			Town::TownActionAdvertise,
			Town::TownActionAdvertise,
			Town::TownActionRoadRebuild,
			Town::TownActionBuildStatue,
			Town::TownActionFundBuildings,
			Town::TownActionBuyRights,
			Town::TownActionBribe
	};


	/** Do a town action.
	 * This performs an action such as advertising, building a statue, funding buildings,
	 * but also bribing the town-council
	 * @param x,y unused
	 * @param p1 town to do the action at
	 * @param p2 action to perform, @see _town_action_proc for the list of available actions
	 */
	static int CmdDoTownAction(int x, int y, int flags, int p1, int p2)
	{
		int cost;
		Town t;

		if (!IsTownIndex(p1) || p2 > _town_action_proc.length) return Cmd.CMD_ERROR;

		t = GetTown(p1);

		if (!BitOps.HASBIT(TownGui.GetMaskOfTownActions(null, PlayerID.getCurrent(), t), p2)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_OTHER);

		cost = (int) ((Global._price.build_industry / 256) * _town_action_costs[p2]);

		if(0 != (flags & Cmd.DC_EXEC)) {
			_town_action_proc[p2].accept(t, p2);
			Window.InvalidateWindow(Window.WC_TOWN_AUTHORITY, p1);
		}

		return cost;
	}

	static final int _grow_count_values1[] = {
			60, 60, 60, 50, 40, 30
	};
	static final int _grow_count_values2[] = {
			210, 150, 110, 80, 50
	};


	static void UpdateTownGrowRate(Town t)
	{
		// Reset player ratings if they're low
		Player.forEach( (p) ->
		{
			if (p.is_active && t.ratings[p.index.id] <= 200) {
				t.ratings[p.index.id] += 5;
			}
		});

		int [] n = { 0 };

		Station.forEach( (st) ->
		{
			if( st.getXy() == null ) return;

			if (Map.DistanceSquare(st.getXy(), t.xy) <= t.radius[0]) {
				if (st.time_since_load <= 20 || st.time_since_unload <= 20) {
					n[0]++;
					if (st.owner.id < Global.MAX_PLAYERS && t.ratings[st.owner.id] <= 1000-12)
						t.ratings[st.owner.id] += 12;
				} else {
					if (st.owner.id < Global.MAX_PLAYERS && t.ratings[st.owner.id] >= -1000+15)
						t.ratings[st.owner.id] -= 15;
				}
			}
		});

		t.flags12 &= ~GROW_BIT;

		int m;

		if (t.fund_buildings_months != 0) {
			m = _grow_count_values1[Math.min(n[0], 5)];
			t.fund_buildings_months--;
		} else if (n[0] == 0) {
			m = 160;
			if (!BitOps.CHANCE16(1, 12))
				return;
		} else {
			m = _grow_count_values2[Math.min(n[0], 5) - 1];
		}

		if (GameOptions._opt.landscape == Landscape.LT_HILLY) {
			if (t.xy.TilePixelHeight() >= GameOptions._opt.snow_line && t.act_food == 0 && t.population > 90)
				return;
		} else if (GameOptions._opt.landscape == Landscape.LT_DESERT) {
			if (t.xy.GetMapExtraBits() == TileInfo.EXTRABITS_DESERT && (t.act_food==0 || t.act_water==0) && t.population > 60)
				return;
		}

		t.growth_rate =  (m / (t.num_houses / 50 + 1));
		if (m <= t.grow_counter)
			t.grow_counter = m;

		t.flags12 |= GROW_BIT;
	}

	static void UpdateTownAmounts(Town t)
	{
		// Using +1 here to prevent overflow and division by zero
		t.pct_pass_transported =  (t.new_act_pass * 256 / (t.new_max_pass + 1));

		t.max_pass = t.new_max_pass; t.new_max_pass = 0;
		t.act_pass = t.new_act_pass; t.new_act_pass = 0;
		t.act_food = t.new_act_food; t.new_act_food = 0;
		t.act_water = t.new_act_water; t.new_act_water = 0;

		// Using +1 here to prevent overflow and division by zero
		t.pct_mail_transported =  (t.new_act_mail * 256 / (t.new_max_mail + 1));
		t.max_mail = t.new_max_mail; t.new_max_mail = 0;
		t.act_mail = t.new_act_mail; t.new_act_mail = 0;

		Window.InvalidateWindow(Window.WC_TOWN_VIEW, t.index);
	}

	static void UpdateTownUnwanted(Town t)
	{
		Player.forEach( (p) ->
		{
			if (t.unwanted[p.index.id] > 0)
				t.unwanted[p.index.id]--;
		});
	}

	static boolean CheckIfAuthorityAllows(TileIndex tile)
	{
		Town t;

		//if (Global.gs._current_player.id >= Global.MAX_PLAYERS)
		if(PlayerID.getCurrent().isSpecial())
			return true;

		t = ClosestTownFromTile(tile, Global._patches.dist_local_authority);
		if (t == null)
			return true;

		if (t.ratings[PlayerID.getCurrent().id] > -200)
			return true;

		Global._error_message = Str.STR_2009_LOCAL_AUTHORITY_REFUSES;
		Global.SetDParam(0, t.index);

		return false;
	}


	public static Town ClosestTownFromTile(TileIndex tile, int threshold)
	{
		int [] best = { threshold >= 0 ? threshold : Integer.MAX_VALUE };
		Town [] best_town = { null };

		if(tile.IsTileType(TileTypes.MP_HOUSE) || (
				tile.IsTileType( TileTypes.MP_STREET) && tile.GetRoadOwner().isTown() ) )
			//(tile.IsLevelCrossing() ? tile.getMap().m3 : tile.GetTileOwner().id) == Owner.OWNER_TOWN) )
			return GetTown(tile.getMap().m2);

		Town.forEach( (t) ->
		{
			if (t.isValid()) {
				int dist = Map.DistanceManhattan(tile, t.xy);
				if (dist < best[0]) {
					best[0] = dist;
					best_town[0] = t;
				}
			}
		});

		return best_town[0];
	}

	void ChangeTownRating(int add, int max)
	{
		int rating;

		//	if magic_bulldozer cheat is active, town doesn't penaltize for removing stuff
		if ( //t == null ||
				//Global.gs._current_player.id >= Global.MAX_PLAYERS 
				PlayerID.getCurrent().isSpecial() 
				||
				(Global._cheats.magic_bulldozer.value && add < 0)) {
			return;
		}

		have_ratings = BitOps.RETSETBIT(have_ratings, PlayerID.getCurrent().id);

		rating = ratings[PlayerID.getCurrent().id];

		if (add < 0) {
			if (rating > max) {
				rating += add;
				if (rating < max) rating = max;
			}
		} else {
			if (rating < max) {
				rating += add;
				if (rating > max) rating = max;
			}
		}
		ratings[PlayerID.getCurrent().id] = rating;
	}

	/*	penalty for removing town-owned stuff */
	static final int _default_rating_settings [][] = {
			// ROAD_REMOVE, TUNNELBRIDGE_REMOVE, INDUSTRY_REMOVE
			{  0, 128, 384},	// Permissive
			{ 48, 192, 480},	// Neutral
			{ 96, 384, 768},	// Hostile
	};

	static boolean CheckforTownRating(TileIndex tile, int flags, Town t, int type)
	{
		int modemod;

		//	if magic_bulldozer cheat is active, town doesn't restrict your destructive actions
		//if (t == null || Global.gs._current_player.id >= Global.MAX_PLAYERS || Global._cheats.magic_bulldozer.value)
		if (t == null || PlayerID.getCurrent().isSpecial() || Global._cheats.magic_bulldozer.value)
			return true;

		/*	check if you're allowed to remove the street/bridge/tunnel/industry
		 *	owned by a town	no removal if rating is lower than ... depends now on
		 *	difficulty setting. Minimum town rating selected by difficulty level
		 */
		modemod = _default_rating_settings[GameOptions._opt.diff.town_council_tolerance][type];

		if (t.ratings[PlayerID.getCurrent().id] < 16 + modemod && 0==(flags & Cmd.DC_NO_TOWN_RATING)) {
			Global.SetDParam(0, t.index);
			Global._error_message = Str.STR_2009_LOCAL_AUTHORITY_REFUSES;
			return false;
		}

		return true;
	}

	public static void TownsMonthlyLoop()
	{
		Town.forEach( (t) ->
		{
			if (t.isValid()) 
			{
				if (t.road_build_months != 0)
					t.road_build_months--;

				if (t.exclusive_counter != 0)
					if(--t.exclusive_counter==0)
						t.exclusivity = PlayerID.get(-1);

				UpdateTownGrowRate(t);
				UpdateTownAmounts(t);
				UpdateTownUnwanted(t);
				mAirport.MunicipalAirport(t);
			}
		});
	}

	static void InitializeTowns()
	{
		/* Clean the town pool and create 1 block in it */
		Global.gs._towns.CleanPool();
		Global.gs._towns.AddBlockToPool();

		Subsidy._subsidies = new Subsidy[Global.MAX_PLAYERS];

		for(int i = 0; i < Global.MAX_PLAYERS; i++)
			Subsidy._subsidies[i] = new Subsidy();


		_cur_town_ctr = 0;
		_cur_town_iter = 0;
		TownGui._town_sort_dirty = true;
	}

	final static TileTypeProcs _tile_type_town_procs = new TileTypeProcs(
			Town::DrawTile_Town,						/* draw_tile_proc */
			Town::GetSlopeZ_Town,						/* get_slope_z_proc */
			Town::ClearTile_Town,						/* clear_tile_proc */
			Town::GetAcceptedCargo_Town,				/* get_accepted_cargo_proc */
			Town::GetTileDesc_Town,						/* get_tile_desc_proc */
			Town::GetTileTrackStatus_Town,				/* get_tile_track_status_proc */
			Town::ClickTile_Town,						/* click_tile_proc */
			Town::AnimateTile_Town,						/* animate_tile_proc */
			Town::TileLoop_Town,						/* tile_loop_clear */
			Town::ChangeTileOwner_Town,					/* change_tile_owner_clear */
			null,										/* get_produced_cargo_proc */
			null,										/* vehicle_enter_tile_proc */
			null,										/* vehicle_leave_tile_proc */
			Town::GetSlopeTileh_Town					/* get_slope_tileh_proc */
			);


	public static void forEach(Consumer<Town> c) {
		Global.gs._towns.forEach(c);
	}

	public static void forEachValid(Consumer<Town> c) {
		Global.gs._towns.forEachValid(c);
	}

	public static Iterator<Town> getIterator()
	{
		return Global.gs._towns.getIterator(); // pool.values().iterator();
	}


	static void AfterLoadTown()
	{
		Town.forEach( (t) ->
		{
			if (t.isValid()) {
				t.UpdateTownRadius();
				t.UpdateTownVirtCoord();
			}
		});
		TownGui._town_sort_dirty = true;
	}



	public static Town getRandomTown() 
	{		
		return GetTown(Hal.RandomRange(Global.gs._towns.size()));
		//return GetTown(Hal.RandomRange(_total_towns));
	}

	/*
	// Save and load of towns.
	static final SaveLoad _town_desc[] = {
			SLE_CONDVAR(Town, xy, SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
			SLE_CONDVAR(Town, xy, SLE_int32, 6, 255),

			SLE_CONDVAR(Town,population,	SLE_FILE_U16 | SLE_VAR_U32, 0, 2),
			SLE_CONDVAR(Town,population,	SLE_int32, 3, 255),


			SLE_VAR(Town,num_houses,	SLE_int16),
			SLE_VAR(Town,townnametype,SLE_int16),
			SLE_VAR(Town,townnameparts,SLE_int32),

			SLE_VAR(Town,flags12,			SLE_int8),
			SLE_VAR(Town,statues,			SLE_int8),

			// sort_index_obsolete was stored here in savegame format 0 - 1
			SLE_CONDARR(NullStruct,null,SLE_FILE_U8 | SLE_VAR_null, 1, 0, 1),

			SLE_VAR(Town,have_ratings,SLE_int8),
			SLE_ARR(Town,ratings,			SLE_INT16, 8),
			// failed bribe attempts are stored since savegame format 4
			SLE_CONDARR(Town,unwanted,			SLE_INT8, 8, 4,255),

			SLE_CONDVAR(Town,max_pass,		SLE_FILE_U16 | SLE_VAR_U32, 0, 8),
			SLE_CONDVAR(Town,max_mail,		SLE_FILE_U16 | SLE_VAR_U32, 0, 8),
			SLE_CONDVAR(Town,new_max_pass,SLE_FILE_U16 | SLE_VAR_U32, 0, 8),
			SLE_CONDVAR(Town,new_max_mail,SLE_FILE_U16 | SLE_VAR_U32, 0, 8),
			SLE_CONDVAR(Town,act_pass,		SLE_FILE_U16 | SLE_VAR_U32, 0, 8),
			SLE_CONDVAR(Town,act_mail,		SLE_FILE_U16 | SLE_VAR_U32, 0, 8),
			SLE_CONDVAR(Town,new_act_pass,SLE_FILE_U16 | SLE_VAR_U32, 0, 8),
			SLE_CONDVAR(Town,new_act_mail,SLE_FILE_U16 | SLE_VAR_U32, 0, 8),

			SLE_CONDVAR(Town,max_pass,		SLE_int32, 9, 255),
			SLE_CONDVAR(Town,max_mail,		SLE_int32, 9, 255),
			SLE_CONDVAR(Town,new_max_pass,SLE_int32, 9, 255),
			SLE_CONDVAR(Town,new_max_mail,SLE_int32, 9, 255),
			SLE_CONDVAR(Town,act_pass,		SLE_int32, 9, 255),
			SLE_CONDVAR(Town,act_mail,		SLE_int32, 9, 255),
			SLE_CONDVAR(Town,new_act_pass,SLE_int32, 9, 255),
			SLE_CONDVAR(Town,new_act_mail,SLE_int32, 9, 255),

			SLE_VAR(Town,pct_pass_transported,SLE_int8),
			SLE_VAR(Town,pct_mail_transported,SLE_int8),

			SLE_VAR(Town,act_food,		SLE_int16),
			SLE_VAR(Town,act_water,		SLE_int16),
			SLE_VAR(Town,new_act_food,SLE_int16),
			SLE_VAR(Town,new_act_water,SLE_int16),

			SLE_VAR(Town,time_until_rebuild,		SLE_int8),
			SLE_VAR(Town,grow_counter,					SLE_int8),
			SLE_VAR(Town,growth_rate,						SLE_int8),
			SLE_VAR(Town,fund_buildings_months,	SLE_int8),
			SLE_VAR(Town,road_build_months,			SLE_int8),

			SLE_VAR(Town,exclusivity,						SLE_int8),
			SLE_VAR(Town,exclusive_counter,			SLE_int8),
			// reserve extra space in savegame here. (currently 30 bytes)
			SLE_CONDARR(NullStruct,null,SLE_FILE_U8 | SLE_VAR_null, 30, 2, 255),

			SLE_END()
	};

	static void Save_TOWN()
	{
		Town t;

		FOR_ALL_TOWNS(t) {
			if (t.xy != 0) {
				SlSetArrayIndex(t.index);
				SlObject(t, _town_desc);
			}
		}
	}

	static void Load_TOWN()
	{
		int index;

		_total_towns = 0;

		while ((index = SlIterateArray()) != -1) {
			Town t;

			if (!AddBlockIfNeeded(&_town_pool, index))
				error("Towns: failed loading savegame: too many towns");

			t = GetTown(index);
			SlObject(t, _town_desc);

			if ((int)index > _total_towns)
				_total_towns = index;
		}

		// This is to ensure all pointers are within the limits of
		//  the size of the TownPool 
		if (_cur_town_ctr >= GetTownPoolSize())
			_cur_town_ctr = 0;
	}



	final ChunkHandler _town_chunk_handlers[] = {
			{ 'CITY', Save_TOWN, Load_TOWN, CH_ARRAY | CH_LAST},
	};
	 */

	public static void loadGame(ObjectInputStream oin)
	{
		//GameState._towns = (MemoryPool<Town>) oin.readObject();
		AfterLoadTown();
	}

	public static void saveGame(ObjectOutputStream oos) 
	{
		//oos.writeObject(GameState._towns);		
	}


	public static class TownPopSorter implements Comparator<Integer> {
		public int compare(Integer a, Integer b) {
			final Town ta = GetTown(a);
			final Town tb = GetTown(b);
			int r = ta.population - tb.population;
			if(0 !=  (TownGui._town_sort_order & 1)) r = -r;
			return r;
		}
	}

	public static class TownNameSorter implements Comparator<Integer> {
		public int compare(Integer a, Integer b) 
		{
			int r;
			Integer [] argv = new Integer[1];

			argv[0] = a;
			String buf1 = Strings.GetStringWithArgs(Str.STR_TOWN, (Object[])argv);

			argv[0] = b;
			String buf2 = Strings.GetStringWithArgs(Str.STR_TOWN, (Object[])argv);

			r = buf1.compareTo(buf2);
			if(0 != (TownGui._town_sort_order & 1)) r = -r;
			return r;
		}
	}

	public int getPopulation() { return population; }
	public ViewportSign getSign() { return sign; }

	public int getNum_houses() {
		return num_houses;
	}

	public boolean isUnwanted(int playerId) {
		return unwanted[playerId] != 0;
	}

	public boolean canBribe(int playerId) {
		return ratings[playerId] < TownTables.RATING_BRIBE_MAXIMUM;
	}

	public boolean hasStatue(int playerId) {
		return BitOps.HASBIT(statues, playerId);
	}

	public boolean hasRatingsFor(int playerId) {
		return BitOps.HASBIT(have_ratings, playerId);
	}

	public boolean isExclusive(int playerId) {
		return exclusivity.id == playerId;
	}

	public int getRatings(int playerId) {
		return ratings[playerId];
	}

	public int getRoad_build_months() {
		return road_build_months;
	}

	
	/**
	 * @return true if at least one valid town exist
	 */
	public static boolean anyTownExist()
	{
		return stream().anyMatch( t -> t.isValid() );
	}

	/**
	 * @return pool of items of this type
	 */
	static MemoryPool<Town> pool() { return Global.gs._towns; }

	/**
	 * @return stream of items of this type
	 */
	static Stream<Town> stream() { return pool().stream(); }

	/**
	 * 
	 * @return Total count of towns in game
	 */
	public static int GetCount() {
		// TODO is it correct?
		return GetTownPoolSize();
	}



}









