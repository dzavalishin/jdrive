package com.dzavalishin.game;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.tables.IndustryTables;
import com.dzavalishin.tables.IndustryTileTable;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.ShortSounds;
import com.dzavalishin.util.Sound;
import com.dzavalishin.util.Strings;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.Gui;
import com.dzavalishin.xui.MiscGui;
import com.dzavalishin.xui.SettingsGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Widget;
import com.dzavalishin.xui.Window;
import com.dzavalishin.xui.WindowDesc;
import com.dzavalishin.xui.WindowEvent;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.IPoolItem;
import com.dzavalishin.ifaces.IPoolItemFactory;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.struct.ProducedCargo;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.struct.TileIndexDiffC;
import com.dzavalishin.tables.DrawIndustrySpec1Struct;
import com.dzavalishin.tables.DrawIndustrySpec4Struct;
import com.dzavalishin.tables.DrawIndustryTileStruct;
import com.dzavalishin.tables.IndustrySpec;

public class Industry extends IndustryTables implements IPoolItem, Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public TileIndex xy;
	int width; /* swapped order of w/h with town */
	int height;
	//public Town town;
	public int townId;

	int produced_cargo[];
	int cargo_waiting[];
	int production_rate[];
	int accepts_cargo[];

	int prod_level;

	int last_mo_production[];
	int last_mo_transported[];
	int pct_transported[];
	int total_production[];
	int total_transported[];

	int counter;

	public int type;
	int owner;
	int color_map;
	int last_prod_year;
	boolean was_cargo_delivered;

	int index;


	private void clear() 
	{
		produced_cargo = new int[2];
		cargo_waiting = new int[2];
		production_rate = new int[2];
		accepts_cargo = new int[3];
		last_mo_production = new int[2];
		last_mo_transported = new int[2];
		pct_transported = new int[2];
		total_production = new int[2];
		total_transported = new int[2];

		xy = null;
		//town = null;
		townId = -1;
		was_cargo_delivered = false;

		width = 0;
		height = 0;
		prod_level = 0;
		counter = 0;
		type = 0;
		owner = 0;
		color_map = 0;
		last_prod_year = 0;
		index = 0;

	}

	public Industry() {
		clear();
	}


	public static final int INDUSTRY_POOL_BLOCK_SIZE_BITS = 3;       /* In bits, so (1 << 3) == 8 */
	public static final int INDUSTRY_POOL_MAX_BLOCKS      = 8000;


	static final IPoolItemFactory<Industry> factory = new IPoolItemFactory<Industry>() 
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Industry createObject() {
			return new Industry();
		}
	};
	@Override
	public void setIndex(int index) {
		this.index = index;
	}


	
	/**
	 * Check if an Industry really exists.
	 */
	public boolean isValid()
	{
		return xy != null; 
	}
	
	public boolean acceptsCargo(int cargoType )
	{
		return cargoType == accepts_cargo[0] 
				|| cargoType == accepts_cargo[1] 
				|| cargoType == accepts_cargo[2];
	}

	public boolean producesCargo(int cargoType) {
		// ind.produced_cargo[0] != AcceptedCargo.CT_INVALID &&	ind.produced_cargo[0] != cargo_type
		return produced_cargo[0] == cargoType || produced_cargo[1] == cargoType;
	}

	
	public void acceptCargo(int cargo_type, int num_pieces) 
	{
		was_cargo_delivered = true;
		// TODO why [0]?
		cargo_waiting[0] = Math.min(cargo_waiting[0] + num_pieces, 0xFFFF);
	}


	

	/**
	 * Get the pointer to the industry with index 'index'
	 */
	public static Industry GetIndustry(int index)
	{
		return Global.gs._industries.GetItemFromPool(index);
	}


	public static Iterator<Industry> getIterator()
	{
		return Global.gs._industries.getIterator(); 
	}

	static void forEach( Consumer<Industry> c )
	{
		Global.gs._industries.forEach(c);
	}

	static void forEachValid( Consumer<Industry> c )
	{
		Global.gs._industries.forEachValid(c);
	}







	static int _industry_sound_ctr;
	static TileIndex _industry_sound_tile;





	static void IndustryDrawTileProc1(final TileInfo ti)
	{
		if (0 == (ti.tile.getMap().m1 & 0x80)) return;

		final DrawIndustrySpec1Struct d = _draw_industry_spec1[ti.tile.getMap().m3];

		ViewPort.AddChildSpriteScreen(0x12A7 + d.image_1, d.x, 0);

		int image = d.image_2;
		if (image != 0) ViewPort.AddChildSpriteScreen(0x12B0 + image - 1, 8, 41);

		image = d.image_3;
		if (image != 0) {
			ViewPort.AddChildSpriteScreen(0x12AC + image - 1,
					_drawtile_proc1_x[image - 1], _drawtile_proc1_y[image - 1]);
		}
	}

	static void IndustryDrawTileProc2(final TileInfo ti)
	{
		int x = 0;

		if(0 != (ti.tile.getMap().m1 & 0x80)) {
			x = _industry_anim_offs[ti.tile.getMap().m3];
			if (x == 0xFF)
				x = 0;
		}

		ViewPort.AddChildSpriteScreen(0x129F, 22 - x, 24 + x);
		ViewPort.AddChildSpriteScreen(0x129E, 6, 0xE);
	}

	static void IndustryDrawTileProc3(final TileInfo ti)
	{
		if(0!=(ti.tile.getMap().m1 & 0x80)) {
			ViewPort.AddChildSpriteScreen(0x128B, 5, _industry_anim_offs_2[ti.tile.getMap().m3]);
		} else {
			ViewPort.AddChildSpriteScreen(4746, 3, 67);
		}
	}

	static void IndustryDrawTileProc4(final TileInfo ti)
	{
		final DrawIndustrySpec4Struct d;

		d = _industry_anim_offs_3[ti.tile.getMap().m3];

		if (d.image_1 != 0xFF) {
			ViewPort.AddChildSpriteScreen(0x126F, 0x32 - d.image_1 * 2, 0x60 + d.image_1);
		}

		if (d.image_2 != 0xFF) {
			ViewPort.AddChildSpriteScreen(0x1270, 0x10 - d.image_2 * 2, 100 + d.image_2);
		}

		ViewPort.AddChildSpriteScreen(0x126E, 7, d.image_3);
		ViewPort.AddChildSpriteScreen(0x126D, 0, 42);
	}

	static void DrawCoalPlantSparkles(final TileInfo ti)
	{
		int image = ti.tile.getMap().m1;
		if(0 != (image & 0x80)) {
			image = BitOps.GB(image, 2, 5);
			if (image != 0 && image < 7) {
				ViewPort.AddChildSpriteScreen(image + 0x806,
						_coal_plant_sparkles_x[image - 1],
						_coal_plant_sparkles_y[image - 1]
						);
			}
		}
	}

	static final IndustryDrawTileProc  _industry_draw_tile_procs[] = {
			Industry::IndustryDrawTileProc1,
			Industry::IndustryDrawTileProc2,
			Industry::IndustryDrawTileProc3,
			Industry::IndustryDrawTileProc4,
			Industry::DrawCoalPlantSparkles,
	};

	static void DrawTile_Industry(TileInfo ti)
	{
		/* Pointer to industry */
		final Industry ind = GetIndustry(ti.tile.getMap().m2);
		int ormod = (ind.color_map + 0x307) << Sprite.PALETTE_SPRITE_START;

		/* Retrieve pointer to the draw industry tile struct */
		final int ii = (ti.map5 << 2) | BitOps.GB(ti.tile.getMap().m1, 0, 2);
		
		if(ii >= _industry_draw_tile_data.length )
		{
			Global.error("DrawTile_Industry m5(%d) too big %d @%d.%d", ti.map5, ii, ti.x/16, ti.y/16 );
			return;
		}
		
		final DrawIndustryTileStruct dits = _industry_draw_tile_data[ii];

		int image = dits.sprite_1;
		if( (0 != (image & Sprite.PALETTE_MODIFIER_COLOR)) && (image & Sprite.PALETTE_SPRITE_MASK) == 0)
			image |= ormod;

		int z =  ti.z;
		/* Add bricks below the industry? */
		if(0 != (ti.tileh & 0xF)) {
			ViewPort.AddSortableSpriteToDraw(Sprite.SPR_FOUNDATION_BASE + (ti.tileh & 0xF), ti.x, ti.y, 16, 16, 7, z);
			ViewPort.AddChildSpriteScreen(image, 0x1F, 1);
			z += 8;
		} else {
			/* Else draw regular ground */
			ViewPort.DrawGroundSprite(image);
		}

		/* Add industry on top of the ground? */
		image = dits.sprite_2;
		if (image != 0) {
			if( (0 != (image & Sprite.PALETTE_MODIFIER_COLOR)) && (image & Sprite.PALETTE_SPRITE_MASK) == 0)
				image |= ormod;

			if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) image = Sprite.RET_MAKE_TRANSPARENT(image);

			ViewPort.AddSortableSpriteToDraw(image,
					ti.x + dits.subtile_x,
					ti.y + dits.subtile_y,
					dits.width  + 1,
					dits.height + 1,
					dits.dz,
					z);

			if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) return;
		}

		{
			int proc = dits.proc - 1;
			if (proc >= 0) _industry_draw_tile_procs[proc].accept(ti);
		}
	}


	static int GetSlopeZ_Industry(final TileInfo  ti)
	{
		return Landscape.GetPartialZ(ti.x & 0xF, ti.y & 0xF, ti.tileh) + ti.z;
	}

	static int GetSlopeTileh_Industry(final TileInfo  ti)
	{
		return 0;
	}

	static AcceptedCargo GetAcceptedCargo_Industry(TileIndex tile)
	{
		AcceptedCargo ac = new AcceptedCargo();
		int m5 = 0xFF & tile.getMap().m5;
		/*CargoID*/ int a;

		if(m5 >= _industry_map5_accepts_1.length)
		{
			Global.error("m5 tpp big @%s", tile.toString() );
			return ac;
		}
		
		a = _industry_map5_accepts_1[m5];
		if (a != AcceptedCargo.CT_INVALID) ac.ct[a] = (a == 0) ? 1 : 8;

		a = _industry_map5_accepts_2[m5];
		if (a != AcceptedCargo.CT_INVALID) ac.ct[a] = 8;

		a = _industry_map5_accepts_3[m5];
		if (a != AcceptedCargo.CT_INVALID) ac.ct[a] = 8;

		return ac;
	}

	static TileDesc GetTileDesc_Industry(TileIndex tile)
	{
		TileDesc td = new TileDesc();
		final Industry  i = GetIndustry(tile.getMap().m2);

		td.owner = i.owner;
		td.str = Str.STR_4802_COAL_MINE + i.type;
		if ((tile.getMap().m1 & 0x80) == 0) {
			Global.SetDParamX(td.dparam, 0, td.str);
			td.str = Str.STR_2058_UNDER_CONSTRUCTION;
		}

		return td;
	}

	static int ClearTile_Industry(TileIndex tile, byte flags)
	{
		Industry i = GetIndustry(tile.getMap().m2);

		/*	* water can destroy industries
		 * in editor you can bulldoze industries
		 * with magic_bulldozer cheat you can destroy industries
		 * (area around OILRIG is water, so water shouldn't flood it
		 */
		if ((!PlayerID.getCurrent().isWater() && Global._game_mode != GameModes.GM_EDITOR &&
				!Global._cheats.magic_bulldozer.value) ||
				(PlayerID.getCurrent().isWater() && i.type == IT_OIL_RIG) ) {
			Global.SetDParam(0, Str.STR_4802_COAL_MINE + i.type);
			return Cmd.return_cmd_error(Str.STR_4800_IN_THE_WAY);
		}

		if( 0!= (flags & Cmd.DC_EXEC)) DeleteIndustry(i);
		return 0;
	}


	static final byte _industry_min_cargo[] = {
			5, 5, 5, 30, 5, 5, 5, 5,
			5, 5, 5, 5, 2, 5, 5, 5,
			5, 5, 5, 15, 15, 5, 5, 5,
			5, 5, 30, 5, 30, 5, 5, 5,
			5, 5, 5, 5, 5,
	};

	static void TransportIndustryGoods(TileIndex tile)
	{
		Industry  i = GetIndustry(tile.getMap().m2);

		// [dz] uncommented check for i.produced_cargo[0] > 0 - why it was commented out?
		int cw = Math.min(i.cargo_waiting[0], 255);
		if (cw > _industry_min_cargo[i.type] && i.produced_cargo[0] > 0) {
			int m5;

			i.cargo_waiting[0] -= cw;

			/* fluctuating economy? */
			if (Global.gs._economy.getFluct() <= 0) cw = (cw + 1) / 2;

			i.last_mo_production[0] += cw;

			int am = Station.MoveGoodsToStation(i.xy, i.width, i.height, i.produced_cargo[0], cw);
			i.last_mo_transported[0] += am;
			if (am != 0 && (m5 = 0xFF & _industry_produce_map5[tile.getMap().m5]) != 0xFF) {
				tile.getMap().m1 = 0x80;
				tile.getMap().m5 = 0xFF & m5;
				tile.MarkTileDirtyByTile();
			}
		}

		cw = Math.min(i.cargo_waiting[1], 255);
		if (cw > _industry_min_cargo[i.type]) {
			i.cargo_waiting[1] -= cw;

			if (Global.gs._economy.getFluct() <= 0) cw = (cw + 1) / 2;

			i.last_mo_production[1] += cw;

			int am = Station.MoveGoodsToStation(i.xy, i.width, i.height, i.produced_cargo[1], cw);
			i.last_mo_transported[1] += am;
		}
	}


	static void AnimateTile_Industry(TileIndex tile)
	{
		int m,n;

		switch(tile.getMap().m5) {
		case 174: // Sugar mine
			if ((Global._tick_counter & 1) == 0) {
				m = tile.getMap().m3 + 1;

				switch(m & 7) {
				case 2:	Sound.SndPlayTileFx(Snd.SND_2D_RIP_2, tile); break;
				case 6: Sound.SndPlayTileFx(Snd.SND_29_RIP, tile); break;
				}

				if (m >= 96) {
					m = 0;
					TextEffect.DeleteAnimatedTile(tile);
				}
				tile.getMap().m3 =  m;

				tile.MarkTileDirtyByTile();
			}
			break;

		case 165: // Toffee mine
			if ((Global._tick_counter & 3) == 0) {
				m = tile.getMap().m3;

				if (_industry_anim_offs[m] == 0xFF) {
					Sound.SndPlayTileFx(Snd.SND_30_CARTOON_SOUND, tile);
				}

				if (++m >= 70) {
					m = 0;
					TextEffect.DeleteAnimatedTile(tile);
				}
				tile.getMap().m3 = m;

				tile.MarkTileDirtyByTile();
			}
			break;

		case 162: // Bubble gen
			if ((Global._tick_counter&1) == 0) {
				m = tile.getMap().m3;

				if (++m >= 40) {
					m = 0;
					TextEffect.DeleteAnimatedTile(tile);
				}
				tile.getMap().m3 =  m;

				tile.MarkTileDirtyByTile();
			}
			break;

			// Sparks on a coal plant
		case 10:
			if ((Global._tick_counter & 3) == 0) {
				m = tile.getMap().m1;
				if (BitOps.GB(m, 2, 5) == 6) {
					tile.getMap().m1 = BitOps.RETSB(tile.getMap().m1, 2, 5, 0);
					TextEffect.DeleteAnimatedTile(tile);
				} else {
					tile.getMap().m1 = m + (1<<2);
					tile.MarkTileDirtyByTile();
				}
			}
			break;

		case 143: // Toy factory
			if ((Global._tick_counter & 1) == 0) {
				m = tile.getMap().m3 + 1;

				if (m == 1) {
					Sound.SndPlayTileFx(Snd.SND_2C_MACHINERY, tile);
				} else if (m == 23) {
					Sound.SndPlayTileFx(Snd.SND_2B_COMEDY_HIT, tile);
				} else if (m == 28) {
					Sound.SndPlayTileFx(Snd.SND_2A_EXTRACT_AND_POP, tile);
				}

				if (m >= 50) {
					m=0;
					if (++tile.getMap().m4 >= 8) {
						tile.getMap().m4 = 0;
						TextEffect.DeleteAnimatedTile(tile);
					}
				}

				tile.getMap().m3 = 0xFF & m;
				tile.MarkTileDirtyByTile();
			}
			break;

			// Plastic fountains
		case 148: case 149: case 150: case 151:
		case 152: case 153: case 154: case 155:
			if ((Global._tick_counter & 3) == 0) {
				m = tile.getMap().m5	+ 1;
				if (m == 155+1) m = 148;
				tile.getMap().m5 = 0xFF & m;
				tile.MarkTileDirtyByTile();
			}
			break;

			// Oil wells
		case 30: case 31: case 32:
			if ((Global._tick_counter & 7) == 0) 
			{
				boolean b = BitOps.CHANCE16(1,7);
				m = tile.getMap().m1;
				m = (m & 3) + 1;
				n = tile.getMap().m5;
				/* implemented below
				if (m == 4 && (m=0,++n) == 32+1 && (n=30,b)) {
					tile.getMap().m1 = 0x83;
					tile.getMap().m5 = 29;
					TextEffect.DeleteAnimatedTile(tile);
				} else {
					tile.getMap().m1 = BitOps.RETSB(tile.getMap().m1, 0, 2, m);
					tile.getMap().m5 = 0xFF & n;
					tile.MarkTileDirtyByTile();
				}
				 */

				boolean doelse = true;

				if (m == 4)
				{
					m=0;
					if(++n == 32+1)
					{
						n=30;
						if(b)
						{
							tile.getMap().m1 = 0x83;
							tile.getMap().m5 = 29;
							TextEffect.DeleteAnimatedTile(tile);
							doelse = false;
						}							
					}					
				}

				if(doelse)
				{
					tile.getMap().m1 = BitOps.RETSB(tile.getMap().m1, 0, 2, m);
					tile.getMap().m5 = 0xFF & n;
					tile.MarkTileDirtyByTile();
				}
			}
			break;

		case 88: // Gold mine
		case 48: // Copper ore mine
		case 1: // Coal mine 
		{
			int state = Global._tick_counter & 0x7FF;

			if ((state -= 0x400) < 0)
				return;

			if (state < 0x1A0) {
				if (state < 0x20 || state >= 0x180) {
					if (0 == (tile.getMap().m1 & 0x40)) {
						tile.getMap().m1 |= 0x40;
						Sound.SndPlayTileFx(Snd.SND_0B_MINING_MACHINERY, tile);
					}
					if(0 != (state & 7))
						return;
				} else {
					if(0 != (state & 3))
						return;
				}
				m = (tile.getMap().m1 + 1) | 0x40;
				if (m > 0xC2) m = 0xC0;
				tile.getMap().m1 = m;
				tile.MarkTileDirtyByTile();
			} else if (state >= 0x200 && state < 0x3A0) {
				int i;
				i = (state < 0x220 || state >= 0x380) ? 7 : 3;
				if(0 != (state & i))
					return;

				m = (tile.getMap().m1 & 0xBF) - 1;
				if (m < 0x80) m = 0x82;
				tile.getMap().m1 = m;				
				tile.MarkTileDirtyByTile();
			}
		} break;
		}
	}

	static void MakeIndustryTileBiggerCase8(TileIndex tile)
	{
		TileInfo ti = Landscape.FindLandscapeHeight(tile.TileX() * 16, tile.TileY() * 16);
		Vehicle.CreateEffectVehicle(ti.x + 15, ti.y + 14, ti.z + 59 + (ti.tileh != 0 ? 8 : 0), Vehicle.EV_CHIMNEY_SMOKE);
	}

	static void MakeIndustryTileBigger(TileIndex tile, int size)
	{
		int b = ((size + (1<<2)) & (3<<2));

		if (b != 0) {
			tile.getMap().m1 = b | (size & 3);
			return;
		}

		size =  ((size + 1) & 3);
		if (size == 3) size |= 0x80;
		tile.getMap().m1 = size | b;

		tile.MarkTileDirtyByTile();

		if (0 == (tile.getMap().m1 & 0x80))
			return;

		switch(tile.getMap().m5) {
		case 8:
			MakeIndustryTileBiggerCase8(tile);
			break;

		case 24:
			if (tile.iadd(0, 1).getMap().m5 == 24) Station.BuildOilRig(tile);
			break;

		case 143:
		case 162:
		case 165:
			tile.getMap().m3 = 0;
			tile.getMap().m4 = 0;
			break;

		case 148: case 149: case 150: case 151:
		case 152: case 153: case 154: case 155:
			TextEffect.AddAnimatedTile(tile);
			break;
		}
	}

	static final int _tileloop_ind_case_161[] = {
			11, 0, -4, -14,
			-4, -10, -4, 1,
			49, 59, 60, 65,
	};

	static void TileLoopIndustryCase161(TileIndex tile)
	{
		Sound.SndPlayTileFx(Snd.SND_2E_EXTRACT_AND_POP, tile);

		int dir = Hal.Random() & 3;

		Vehicle v = Vehicle.CreateEffectVehicleAbove(
				tile.TileX() * 16 + _tileloop_ind_case_161[dir + 0],
				tile.TileY() * 16 + _tileloop_ind_case_161[dir + 4],
				_tileloop_ind_case_161[dir + 8],
				Vehicle.EV_BUBBLE
				);

		if (v != null) v.special.unk2 = dir;
	}

	private static void SET_AND_ANIMATE(TileIndex tile, int a, int b)   { tile.getMap().m5 = 0xFF & a; tile.getMap().m1 = b; TextEffect.AddAnimatedTile(tile); }
	private static void SET_AND_UNANIMATE(TileIndex tile, int a, int b) { tile.getMap().m5 = 0xFF & a; tile.getMap().m1 = b; TextEffect.DeleteAnimatedTile(tile); }


	static void TileLoop_Industry(TileIndex tile)
	{
		int mm5 = 0xFF & tile.getMap().m5;
		// TO DO X XX HACK temp kill me
		/*if(mm5 > _industry_map5_animation_next.length )
			tile.getMap().m5 = mm5 = 0x18; // oilrig? just for fun */

		if (0 == (tile.getMap().m1 & 0x80)) {
			MakeIndustryTileBigger(tile, tile.getMap().m1);
			return;
		}

		if (Global._game_mode == GameModes.GM_EDITOR) return;

		TransportIndustryGoods(tile);
		
		
		int n = _industry_map5_animation_next[mm5];
		if (n != 255) {
			tile.getMap().m1 = 0;
			tile.getMap().m5 = 0xFF & n;
			tile.MarkTileDirtyByTile();
			return;
		}


		switch (0xFF & tile.getMap().m5) {
		case 0x18: // coast line at oilrigs
		case 0x19:
		case 0x1A:
		case 0x1B:
		case 0x1C:
			WaterCmd.TileLoop_Water(tile);
			break;

		case 0:
			if (0 == (Global._tick_counter & 0x400) && BitOps.CHANCE16(1,2))
				SET_AND_ANIMATE(tile,1,0x80);
			break;

		case 47:
			if (0 == (Global._tick_counter & 0x400) && BitOps.CHANCE16(1,2))
				SET_AND_ANIMATE(tile,0x30,0x80);
			break;

		case 79:
			if (0 == (Global._tick_counter & 0x400) && BitOps.CHANCE16(1,2))
				SET_AND_ANIMATE(tile,0x58,0x80);
			break;

		case 29:
			if (BitOps.CHANCE16(1,6))
				SET_AND_ANIMATE(tile,0x1E,0x80);
			break;

		case 1:
			if (0 == (Global._tick_counter & 0x400))
				SET_AND_UNANIMATE(tile, 0, 0x83);
			break;

		case 48:
			if (0 == (Global._tick_counter & 0x400))
				SET_AND_UNANIMATE(tile, 0x2F, 0x83);
			break;

		case 88:
			if (0 == (Global._tick_counter & 0x400))
				SET_AND_UNANIMATE(tile, 0x4F, 0x83);
			break;

		case 10: // Power station
			if (BitOps.CHANCE16(1,3)) {
				Sound.SndPlayTileFx(Snd.SND_0C_ELECTRIC_SPARK, tile);
				TextEffect.AddAnimatedTile(tile);
			}
			break;

		case 49:
			Vehicle.CreateEffectVehicleAbove(tile.TileX() * 16 + 6, tile.TileY() * 16 + 6, 43, Vehicle.EV_SMOKE);
			break;


		case 143: {
			Industry i = GetIndustry(tile.getMap().m2);
			if (i.was_cargo_delivered) {
				i.was_cargo_delivered = false;
				tile.getMap().m4 = 0;
				TextEffect.AddAnimatedTile(tile);
			}
		}
		break;

		case 161:
			TileLoopIndustryCase161(tile);
			break;

		case 165:
			TextEffect.AddAnimatedTile(tile);
			break;

		case 174:
			if (BitOps.CHANCE16(1, 3)) TextEffect.AddAnimatedTile(tile);
			break;
		}
	}


	static void ClickTile_Industry(TileIndex tile)
	{
		ShowIndustryViewWindow(tile.getMap().m2);
	}

	static int GetTileTrackStatus_Industry(TileIndex tile, /*int*/ TransportType mode)
	{
		return 0;
	}

	static ProducedCargo GetProducedCargo_Industry(TileIndex tile)
	{
		ProducedCargo ac = new ProducedCargo();
		final Industry  i = GetIndustry(tile.getMap().m2);

		ac.cargo[0] = i.produced_cargo[0];
		ac.cargo[1] = i.produced_cargo[1];

		return ac;
	}

	static void ChangeTileOwner_Industry(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		/* not used */
	}

	static void DeleteIndustry(Industry i)
	{
		TileIndex.forAll( i.width, i.height, i.xy, (tile_cur) ->
		{
			if (tile_cur.IsTileType( TileTypes.MP_INDUSTRY)) {
				if (tile_cur.getMap().m2 == i.index) {
					Landscape.DoClearSquare(tile_cur);
				}
			} else if (tile_cur.IsTileType( TileTypes.MP_STATION) && tile_cur.getMap().m5 == 0x4B) {
				Station.DeleteOilRig(tile_cur);
			}
			return false;
		});

		i.xy = null;
		_industry_sort_dirty = true;
		Subsidy.DeleteSubsidyWithIndustry(i.index);
		Window.DeleteWindowById(Window.WC_INDUSTRY_VIEW, i.index);
		Window.InvalidateWindow(Window.WC_INDUSTRY_DIRECTORY, 0);
	}

	static final byte _plantfarmfield_type[] = {1, 1, 1, 1, 1, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6};

	static boolean IsBadFarmFieldTile(TileIndex tile)
	{
		switch (tile.GetTileType()) {
		case MP_CLEAR: {
			int m5 = tile.getMap().m5 & 0x1C;
			return m5 == 0xC || m5 == 0x10;
		}

		case MP_TREES:
			return false;

		default:
			return true;
		}
	}

	static boolean IsBadFarmFieldTile2(TileIndex tile)
	{
		switch (tile.GetTileType()) {
		case MP_CLEAR: {
			int m5 = tile.getMap().m5 & 0x1C;
			return m5 == 0x10;
		}

		case MP_TREES:
			return false;

		default:
			return true;
		}
	}

	static void SetupFarmFieldFence(TileIndex itile, int size, int type, int direction)
	{
		int or, and;

		MutableTileIndex tile = new MutableTileIndex(itile);

		do {
			//tile = tile.TILE_MASK();
			tile.TILE_MASK();

			if (tile.IsTileType( TileTypes.MP_CLEAR) || tile.IsTileType( TileTypes.MP_TREES)) {

				or = 0xFF & type;
				if (or == 1 && BitOps.CHANCE16(1, 7)) or = 2;

				or <<= 2;
				and = ~0x1C;
				if (direction != 0) {
					or <<= 3;
					and = ~0xE0;
				}
				tile.getMap().m4 =  ((tile.getMap().m4 & and) | or);
			}

			tile.madd( direction != 0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0) );
		} while (--size > 0);
	}

	static void PlantFarmField(TileIndex itile)
	{
		MutableTileIndex tile = new MutableTileIndex(itile);

		if (GameOptions._opt.landscape == Landscape.LT_HILLY) {
			if (tile.GetTileZ() + 16 >= GameOptions._opt.snow_line)
				return;
		}

		/* determine field size */
		int r = (Hal.Random() & 0x303) + 0x404;
		if (GameOptions._opt.landscape == Landscape.LT_HILLY) r += 0x404;
		
		int size_x = BitOps.GB(r, 0, 8);
		int size_y = BitOps.GB(r, 8, 8);

		/* offset tile to match size */
		tile.msub( TileIndex.TileDiffXY(size_x / 2, size_y / 2) );

		/* check the amount of bad tiles */
		int [] count = {0};

		TileIndex.forAll(size_x, size_y, tile, (cur_tile) ->
		{
			//cur_tile = TILE_MASK(cur_tile);
			count[0] += IsBadFarmFieldTile(cur_tile.TILE_MASK()) ? 1 : 0;
			return false;
		});

		if (count[0] * 2 >= size_x * size_y) return;

		/* determine type of field */
		r = Hal.Random();
		int type = ((r & 0xE0) | 0xF);
		int type2 = BitOps.GB(r, 8, 8) * 9 >> 8;

		final int ftype = type;
		/* make field */
		//BEGIN_TILE_LOOP(cur_tile, size_x, size_y, tile)
		TileIndex.forAll(size_x, size_y, tile, (cur_tile1) ->
		{
			TileIndex cur_tile = cur_tile1.TILE_MASK();
			if (!IsBadFarmFieldTile2(cur_tile)) {
				Landscape.ModifyTile(cur_tile, TileTypes.MP_CLEAR,
						//TileTypes.MP_SETTYPE(TileTypes.MP_CLEAR) |
						TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO | TileTypes.MP_MAP3HI_CLEAR | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5,
						type2,			/* map3_lo */
						Owner.OWNER_NONE,	/* map_owner */
						ftype);			/* map5 */
			}
			return false;
		});
		//END_TILE_LOOP(cur_tile, size_x, size_y, tile)

		type = 3;
		if (GameOptions._opt.landscape != Landscape.LT_HILLY && GameOptions._opt.landscape != Landscape.LT_DESERT) {
			type = _plantfarmfield_type[Hal.Random() & 0xF];
		}

		SetupFarmFieldFence(tile.isub( TileIndex.TileDiffXY(1, 0)), size_y, type, 1);
		SetupFarmFieldFence(tile.isub( TileIndex.TileDiffXY(0, 1)), size_x, type, 0);
		SetupFarmFieldFence(tile.iadd( TileIndex.TileDiffXY(size_x - 1, 0)), size_y, type, 1);
		SetupFarmFieldFence(tile.iadd( TileIndex.TileDiffXY(0, size_y - 1)), size_x, type, 0);
	}

	static void MaybePlantFarmField(final Industry  i)
	{
		if (BitOps.CHANCE16(1, 8)) {
			int x = i.width  / 2 + Hal.Random() % 31 - 16;
			int y = i.height / 2 + Hal.Random() % 31 - 16;
			TileIndex tile = new TileIndex(i.xy).TileAddWrap(x, y);
			if (tile.isValid()) PlantFarmField(tile);
		}
	}

	static final TileIndexDiffC[] _chop_dir = {
			new TileIndexDiffC( 0,  1),
			new TileIndexDiffC( 1,  0),
			new TileIndexDiffC( 0, -1),
			new TileIndexDiffC(-1,  0)
	};

	static void ChopLumberMillTrees(Industry i)
	{
		MutableTileIndex tile = new MutableTileIndex( i.xy );
		int a;

		if ((tile.getMap().m1 & 0x80) == 0) return;

		/* search outwards as a rectangular spiral */
		for (a = 1; a != 41; a += 2) {
			int dir;

			for (dir = 0; dir != 4; dir++) {
				int j = a;

				do {
					tile.TILE_MASK();
					if (tile.IsTileType( TileTypes.MP_TREES)) {
						PlayerID old_player = PlayerID.getCurrent();
						/* found a tree */

						PlayerID.setCurrentToNone();
						_industry_sound_ctr = 1;
						_industry_sound_tile = tile;
						
						//Sound.SndPlayTileFx(Snd.SND_38_CHAINSAW, tile);
						ShortSounds.playSawMillSound();

						Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
						tile.SetMapExtraBits(0);

						i.cargo_waiting[0] = Math.min(0xffff, i.cargo_waiting[0] + 45);

						PlayerID.setCurrent(old_player);
						return;
					}
					tile.madd( TileIndex.ToTileIndexDiff(_chop_dir[dir]) );
				} while (--j > 0);
			}
			tile.msub( TileIndex.TileDiffXY(1, 1) );
		}
	}


	static final int _industry_sounds[][] = {
			{0},
			{0},
			{1, Snd.SND_28_SAWMILL.ordinal() },
			{0},
			{0},
			{0},
			{1, Snd.SND_03_FACTORY_WHISTLE.ordinal() },
			{1, Snd.SND_03_FACTORY_WHISTLE.ordinal() },
			{0},
			{3, Snd.SND_24_SHEEP.ordinal() },
			{0},
			{0},
			{0},
			{0},
			{1, Snd.SND_28_SAWMILL.ordinal() },
			{0},
			{0},
			{0},
			{0},
			{0},
			{0},
			{0},
			{0},
			{1, Snd.SND_03_FACTORY_WHISTLE.ordinal() },
			{0},
			{0},
			{0},
			{0},
			{0},
			{0},
			{0},
			{0},
			{1, Snd.SND_33_PLASTIC_MINE.ordinal() },
			{0},
			{0},
			{0},
			{0},
	};
	 

	static void ProduceIndustryGoods(Industry i)
	{
		int [] r = {0};
		int num;

		/* play a sound? */
		if ((i.counter & 0x3F) == 0) {
			if (BitOps.CHANCE16R(1,14,r) && (num=_industry_sounds[i.type][0]) != 0) {
				Sound.SndPlayTileFx( _industry_sounds[i.type][1] + (((r[0] >> 16) * num) >> 16), i.xy);
			}
		}

		i.counter--;

		/* produce some cargo */
		if ((i.counter & 0xFF) == 0) {
			i.cargo_waiting[0] = Math.min(0xffff, i.cargo_waiting[0] + i.production_rate[0]);
			i.cargo_waiting[1] = Math.min(0xffff, i.cargo_waiting[1] + i.production_rate[1]);

			if (i.type == IT_FARM) {
				MaybePlantFarmField(i);
			} else if (i.type == IT_LUMBER_MILL && (i.counter & 0x1FF) == 0) {
				ChopLumberMillTrees(i);
			}
		}
	}

	static void OnTick_Industry()
	{
		if (_industry_sound_ctr != 0) {
			_industry_sound_ctr++;

			if (_industry_sound_ctr == 75) {
				Sound.SndPlayTileFx(Snd.SND_37_BALLOON_SQUEAK, _industry_sound_tile);
			} else if (_industry_sound_ctr == 160) {
				_industry_sound_ctr = 0;
				Sound.SndPlayTileFx(Snd.SND_36_CARTOON_CRASH, _industry_sound_tile);
			}
		}

		if (Global._game_mode == GameModes.GM_EDITOR) return;

		Industry.forEach( (i) ->
		{
			if (i.isValid()) ProduceIndustryGoods(i);
		});
	}


	static boolean CheckNewIndustry_null(TileIndex tile, int type)
	{
		return true;
	}

	static boolean CheckNewIndustry_Forest(TileIndex tile, int type)
	{
		if (GameOptions._opt.landscape == Landscape.LT_HILLY) {
			if (tile.GetTileZ() < GameOptions._opt.snow_line + 16) {
				Global._error_message = Str.STR_4831_FOREST_CAN_ONLY_BE_PLANTED;
				return false;
			}
		}
		return true;
	}

	//extern boolean _ignore_restrictions;

	/* Oil Rig and Oil Refinery */
	static boolean CheckNewIndustry_Oil(TileIndex tile, int type)
	{
		if (Global._game_mode == GameModes.GM_EDITOR && Gui._ignore_restrictions) return true;
		if (Global._game_mode == GameModes.GM_EDITOR && type != IT_OIL_RIG)   return true;
		if (Map.DistanceFromEdge(tile.iadd(1, 1)) < 16)   return true;

		Global._error_message = Str.STR_483B_CAN_ONLY_BE_POSITIONED;
		return false;
	}

	static boolean CheckNewIndustry_Farm(TileIndex tile, int type)
	{
		if (GameOptions._opt.landscape == Landscape.LT_HILLY) {
			if (tile.GetTileZ() + 16 >= GameOptions._opt.snow_line) {
				Global._error_message = Str.STR_0239_SITE_UNSUITABLE;
				return false;
			}
		}
		return true;
	}

	static boolean CheckNewIndustry_Plantation(TileIndex tile, int type)
	{
		if (tile.GetMapExtraBits() == TileInfo.EXTRABITS_DESERT) {
			Global._error_message = Str.STR_0239_SITE_UNSUITABLE;
			return false;
		}

		return true;
	}

	static boolean CheckNewIndustry_Water(TileIndex tile, int type)
	{
		if (tile.GetMapExtraBits() != TileInfo.EXTRABITS_DESERT) {
			Global._error_message = Str.STR_0318_CAN_ONLY_BE_BUILT_IN_DESERT;
			return false;
		}

		return true;
	}

	static boolean CheckNewIndustry_Lumbermill(TileIndex tile, int type)
	{
		if (tile.GetMapExtraBits() != 2) {
			Global._error_message = Str.STR_0317_CAN_ONLY_BE_BUILT_IN_RAINFOREST;
			return false;
		}
		return true;
	}

	static boolean CheckNewIndustry_BubbleGen(TileIndex tile, int type)
	{
		return tile.GetTileZ() <= 32;
	}

	static final CheckNewIndustryProc _check_new_industry_procs[] = {
			Industry::CheckNewIndustry_null,
			Industry::CheckNewIndustry_Forest,
			Industry::CheckNewIndustry_Oil,
			Industry::CheckNewIndustry_Farm,
			Industry::CheckNewIndustry_Plantation,
			Industry::CheckNewIndustry_Water,
			Industry::CheckNewIndustry_Lumbermill,
			Industry::CheckNewIndustry_BubbleGen,
	};

	static boolean CheckSuitableIndustryPos(TileIndex tile)
	{
		int x = tile.TileX();
		int y = tile.TileY();

		if (x < 2 || y < 2 || x > Global.MapMaxX() - 3 || y > Global.MapMaxY() - 3) {
			Global._error_message = Str.STR_0239_SITE_UNSUITABLE;
			return false;
		}

		return true;
	}

	static Town CheckMultipleIndustryInTown(TileIndex tile, int type)
	{
		final Town t;

		t = Town.ClosestTownFromTile(tile, -1);

		if (Global._patches.multiple_industry_per_town) return t;

		Iterator<Industry> ii = Industry.getIterator();
		while(ii.hasNext())
		{
			final Industry i = ii.next();
			if (i.isValid() &&
					i.type == type &&
					i.getTown() == t) {
				Global._error_message = Str.STR_0287_ONLY_ONE_ALLOWED_PER_TOWN;
				return null;
			}
		}

		return t;
	}

	public Town getTown() {
		return Town.GetTown(townId);
	}


	static final byte _industry_map5_bits[] = {
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 4, 2, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 4, 2, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16, 16,
			16, 16, 16, 16, 16, 16, 16,
	};

	static boolean CheckIfIndustryTilesAreFree(TileIndex tile, final IndustryTileTable [] itt, int type, final Town tt)
	{
		TileInfo ti = new TileInfo();

		Global._error_message = Str.STR_0239_SITE_UNSUITABLE;

		for(final IndustryTileTable it : itt) 
		{
			// kill me and end table markers in tables
			if(it.ti.x == -0x80)
				break;

			TileIndex cur_tile = tile.iadd( TileIndex.ToTileIndexDiff(it.ti) );

			if (!cur_tile.IsValidTile()) {
				if (it.map5 == 0xff) continue;
				return false;
			}

			Landscape.FindLandscapeHeightByTile(ti, cur_tile);

			if (it.map5 == 0xFF) {
				if (ti.type != TileTypes.MP_WATER.ordinal() || ti.tileh != 0) return false;
			} else {
				if (!cur_tile.EnsureNoVehicle()) return false;

				if (type == IT_OIL_RIG)  {
					if (ti.type != TileTypes.MP_WATER.ordinal() || ti.map5 != 0) return false;
				} else {
					if (ti.type == TileTypes.MP_WATER.ordinal() && ti.map5 == 0) return false;
					if (TileIndex.IsSteepTileh(ti.tileh))
						return false;

					if (ti.tileh != 0) {
						int t;
						byte bits = _industry_map5_bits[it.map5];

						if(0 != (bits & 0x10)) return false;

						t = ~ti.tileh;

						if( (0 !=  (bits & 1)) && 0 != (t & (1 + 8)) ) return false;
						if( (0 !=  (bits & 2)) && 0 != (t & (4 + 8)) ) return false;
						if( (0 !=  (bits & 4)) && 0 != (t & (1 + 2)) ) return false;
						if( (0 !=  (bits & 8)) && 0 != (t & (2 + 4)) ) return false;
					}

					if (type == IT_BANK) {
						if (ti.type != TileTypes.MP_HOUSE.ordinal() || tt.population < 1200) {
							Global._error_message = Str.STR_029D_CAN_ONLY_BE_BUILT_IN_TOWNS;
							return false;
						}
					} else if (type == IT_BANK_2) {
						if (ti.type != TileTypes.MP_HOUSE.ordinal()) {
							Global._error_message = Str.STR_030D_CAN_ONLY_BE_BUILT_IN_TOWNS;
							return false;
						}
					} else if (type == IT_TOY_SHOP) {
						if (Map.DistanceMax(tt.getXy(), cur_tile) > 9) return false;
						if (ti.type != TileTypes.MP_HOUSE.ordinal()) {
							//goto do_clear;
							if (Cmd.CmdFailed(Cmd.DoCommandByTile(cur_tile, 0, 0, Cmd.DC_AUTO, Cmd.CMD_LANDSCAPE_CLEAR)))
								return false;
						}
					} else if (type == IT_WATER_TOWER) {
						if (ti.type != TileTypes.MP_HOUSE.ordinal()) {
							Global._error_message = Str.STR_0316_CAN_ONLY_BE_BUILT_IN_TOWNS;
							return false;
						}
					} else {
						//do_clear:
						if (Cmd.CmdFailed(Cmd.DoCommandByTile(cur_tile, 0, 0, Cmd.DC_AUTO, Cmd.CMD_LANDSCAPE_CLEAR)))
							return false;
					}
				}
			}
		}// while ((++it).ti.x != -0x80);

		return true;
	}

	static boolean CheckIfTooCloseToIndustry(TileIndex tile, int type)
	{
		final IndustrySpec spec = _industry_spec[type];

		// accepting industries won't be close, not even with patch
		if (Global._patches.same_industry_close && spec.accepts_cargo[0] == AcceptedCargo.CT_INVALID)
			return true;

		Iterator<Industry> ii = Industry.getIterator();
		while(ii.hasNext())
		{
			Industry i = ii.next();
			// check if an industry that accepts the same goods is nearby
			if (i.isValid() &&
					Map.DistanceMax(tile, i.xy) <= 14 &&
					spec.accepts_cargo[0] != AcceptedCargo.CT_INVALID &&
					spec.accepts_cargo[0] == i.accepts_cargo[0] && (
							Global._game_mode != GameModes.GM_EDITOR ||
							!Global._patches.same_industry_close ||
							!Global._patches.multiple_industry_per_town
							)) {
				Global._error_message = Str.STR_INDUSTRY_TOO_CLOSE;
				return false;
			}

			// check "not close to" field.
			if (i.isValid() &&
					(i.type == spec.a || i.type == spec.b || i.type == spec.c) &&
					Map.DistanceMax(tile, i.xy) <= 14) {
				Global._error_message = Str.STR_INDUSTRY_TOO_CLOSE;
				return false;
			}
		}
		return true;
	}

	static Industry AllocateIndustry()
	{
		//Industry i;

		Iterator<Industry> ii = getIterator();
		while(ii.hasNext())
		{
			Industry i = ii.next();

			if (!i.isValid()) {
				int index = i.index;

				if (i.index > _total_industries) _total_industries = i.index;

				i.clear();
				i.index = index;

				return i;
			}
		}

		/* Check if we can add a block to the pool */
		return Global.gs._industries.AddBlockToPool() ? AllocateIndustry() : null;
	}



	static void DoCreateNewIndustry(Industry  i, TileIndex tile, int type, final IndustryTileTable [] itt, final Town t, int owner)
	{
		int r;
		int j;

		i.xy = tile;
		i.width = i.height = 0;
		i.type = type;

		final IndustrySpec spec = _industry_spec[type];

		i.produced_cargo[0] = spec.produced_cargo[0];
		i.produced_cargo[1] = spec.produced_cargo[1];
		i.accepts_cargo[0] = spec.accepts_cargo[0];
		i.accepts_cargo[1] = spec.accepts_cargo[1];
		i.accepts_cargo[2] = spec.accepts_cargo[2];
		i.production_rate[0] = spec.production_rate[0];
		i.production_rate[1] = spec.production_rate[1];

		if (Global._patches.smooth_economy.get()) {
			i.production_rate[0] =  Math.min((Hal.RandomRange(256) + 128) * i.production_rate[0] >> 8 , 255);
			i.production_rate[1] =  Math.min((Hal.RandomRange(256) + 128) * i.production_rate[1] >> 8 , 255);
		}

		i.townId = t.index;
		i.owner =  owner;

		r = Hal.Random();
		i.color_map =  BitOps.GB(r, 8, 4);
		i.counter = BitOps.GB(r, 0, 12);
		i.cargo_waiting[0] = 0;
		i.cargo_waiting[1] = 0;
		i.last_mo_production[0] = 0;
		i.last_mo_production[1] = 0;
		i.last_mo_transported[0] = 0;
		i.last_mo_transported[1] = 0;
		i.pct_transported[0] = 0;
		i.pct_transported[1] = 0;
		i.total_transported[0] = 0;
		i.total_transported[1] = 0;
		i.was_cargo_delivered = false;
		i.last_prod_year =  Global.get_cur_year();
		i.total_production[0] = i.production_rate[0] * 8;
		i.total_production[1] = i.production_rate[1] * 8;

		if (!Global._generating_world) i.total_production[0] = i.total_production[1] = 0;

		i.prod_level = 0x10;

		for(final IndustryTileTable it : itt)
		{
			// kill me and end of list markers in arrays
			if(it.ti.x == -0x80)
				break;
			
			TileIndex cur_tile = tile.iadd( TileIndex.ToTileIndexDiff(it.ti) );

			if (it.map5 != 0xFF) {
				int size;

				size = it.ti.x;
				if (size > i.width) i.width =  size;
				size = it.ti.y;
				if (size > i.height)i.height =  size;

				Cmd.DoCommandByTile(cur_tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);

				cur_tile.SetTileType(TileTypes.MP_INDUSTRY);
				cur_tile.getMap().m5 = 0xFF & it.map5;
				cur_tile.getMap().m2 = i.index;
				cur_tile.getMap().m1 = Global._generating_world ? 0x1E : 0; /* maturity */
				
				//Global.printf("industry m5 = %x @%d.%d\n", cur_tile.getMap().m5, cur_tile.TileX(), cur_tile.TileY() );
			}
		} // while ((++it).ti.x != -0x80);

		i.width++;
		i.height++;

		if (i.type == IT_FARM || i.type == IT_FARM_2) {
			tile = i.xy.iadd(i.width / 2, i.height / 2);
			for (j = 0; j != 50; j++) {
				int x = Hal.Random() % 31 - 16;
				int y = Hal.Random() % 31 - 16;
				TileIndex new_tile = tile.TileAddWrap(x, y);

				if (new_tile != TileIndex.INVALID_TILE) PlantFarmField(new_tile);
			}
		}
		_industry_sort_dirty = true;
		Window.InvalidateWindow(Window.WC_INDUSTRY_DIRECTORY, 0);
	}

	/** Build/Fund an industry
	 * @param x,y coordinates where industry is built
	 * @param p1 industry type @see build_industry.h and @see industry.h
	 * @param p2 unused
	 */
	public static int CmdBuildIndustry(int x, int y, int flags, int p1, int p2)
	{
		final Town t;
		Industry i;
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		int num;
		final IndustryTileTable [][] itt;
		//final IndustryTileTable [] itt;
		IndustryTileTable [] it;
		//final IndustryTileTable  it;
		//final IndustrySpec [] spec;
		final IndustrySpec  spec;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_OTHER);

		if (!CheckSuitableIndustryPos(tile)) return Cmd.CMD_ERROR;

		/* Check if the to-be built/founded industry is available for this climate.
		 * Unfortunately we have no easy way of checking, except for looping the table */
		{
			//final byte* i;
			int i1;
			boolean found = false;

			/* for (
					i = &_build_industry_types[GameOptions._opt_ptr.landscape][0]; 
					i != endof(_build_industry_types[GameOptions._opt_ptr.landscape]); 
					i++) */
			byte [] array = _build_industry_types[GameOptions._opt_ptr.landscape];
			for( i1 = 0; i1 < array.length; i1++ )
			{
				if (array[i1] == p1) {
					found = true;
					break;
				}
			}
			if (!found) return Cmd.CMD_ERROR;
		}

		spec = _industry_spec[p1];
		/* If the patch for raw-material industries is not on, you cannot build raw-material industries.
		 * Raw material industries are industries that do not accept cargo (at least for now)
		 * Exclude the lumber mill (only "raw" industry that can be built) */
		if (!Global._patches.build_rawmaterial_ind.get() &&
				spec.accepts_cargo[0] == AcceptedCargo.CT_INVALID &&
				spec.accepts_cargo[1] == AcceptedCargo.CT_INVALID &&
				spec.accepts_cargo[2] == AcceptedCargo.CT_INVALID &&
				p1 != IT_LUMBER_MILL) {
			return Cmd.CMD_ERROR;
		}

		if (!_check_new_industry_procs[spec.check_proc].check(tile, p1)) return Cmd.CMD_ERROR;

		t = CheckMultipleIndustryInTown(tile, p1);
		if (t == null) return Cmd.CMD_ERROR;

		//num = spec.num_table;
		num = spec.table.length;
		itt = spec.table;

		do {
			if (--num < 0) return Cmd.return_cmd_error(Str.STR_0239_SITE_UNSUITABLE);
		} while (!CheckIfIndustryTilesAreFree(tile, it = itt[num], p1, t));


		if (!CheckIfTooCloseToIndustry(tile, p1)) return Cmd.CMD_ERROR;

		i = AllocateIndustry();
		if (i == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) DoCreateNewIndustry(i, tile, p1, it, t, Owner.OWNER_NONE);

		return ( ((int)Global._price.build_industry) >> 5) * _industry_type_costs[p1];
	}


	public static Industry CreateNewIndustry(TileIndex tile, int type)
	{
		if (!CheckSuitableIndustryPos(tile)) return null;

		final IndustrySpec spec = _industry_spec[type];

		if (!_check_new_industry_procs[spec.check_proc].check(tile, type)) return null;

		final Town t = CheckMultipleIndustryInTown(tile, type);
		if (t == null) return null;

		/* pick a random layout */
		final IndustryTileTable [] it = spec.table[Hal.RandomRange(spec.table.length)];

		if (!CheckIfIndustryTilesAreFree(tile, it, type, t)) return null;
		if (!CheckIfTooCloseToIndustry(tile, type)) return null;

		Industry i = AllocateIndustry();
		if (i == null) return null;

		DoCreateNewIndustry(i, tile, type, it, t, Owner.OWNER_NONE);

		return i;
	}

	static final byte _numof_industry_table[][] = {
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5},
			{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
			{0, 2, 3, 4, 6, 7, 8, 9, 10, 10, 10},
	};

	static void PlaceInitialIndustry(byte type, int amount)
	{
		int num = _numof_industry_table[GameOptions._opt.diff.number_industries][amount];

		if (type == IT_OIL_REFINERY || type == IT_OIL_RIG) {
			// These are always placed next to the coastline, so we scale by the perimeter instead.
			num = Map.ScaleByMapSize1D(num);
		} else {
			num = Map.ScaleByMapSize(num);
		}

		if (GameOptions._opt.diff.number_industries != 0) {
			PlayerID old_player = PlayerID.getCurrent();
			PlayerID.setCurrentToNone();
			assert(num > 0);

			do {
				for (int i = 0; i < 2000; i++) {
					if (CreateNewIndustry(Hal.RandomTile(), type) != null) break;
				}
			} while (--num > 0);

			PlayerID.setCurrent(old_player);
		}
	}

	public static void GenerateIndustries()
	{
		int i = 0;
		final byte [] b = _industry_create_table[GameOptions._opt.landscape];
		do {
			PlaceInitialIndustry(b[i+1], b[i+0]);
			i += 2;
		} while( i < b.length-1);
		//while ( (b+=2)[0] != 0);
	}

	static void ExtChangeIndustryProduction(Industry i)
	{
		boolean closeit = true;
		int j;

		switch (_industry_close_mode[i.type]) {
		case INDUSTRY_NOT_CLOSABLE:
			return;

		case INDUSTRY_CLOSABLE:
			if ((Global.get_cur_year() - i.last_prod_year) < 5 || !BitOps.CHANCE16(1, 180))
				closeit = false;
			break;

		default: /* INDUSTRY_PRODUCTION */
			for (j = 0; j < 2 && i.produced_cargo[j] != AcceptedCargo.CT_INVALID; j++){
				int r = Hal.Random();
				int old, inew, percent;
				int mag;

				inew = old = i.production_rate[j];
				if (BitOps.CHANCE16I(20, 1024, r))
					inew -= ((Hal.RandomRange(50) + 10) * old) >> 8;
			if (BitOps.CHANCE16I(20 + (i.pct_transported[j] * 20 >> 8), 1024, r >> 16))
				inew += ((Hal.RandomRange(50) + 10) * old) >> 8;

				inew = BitOps.clamp(inew, 0, 255);
				if (inew == old) {
					closeit = false;
					continue;
				}

				percent = inew * 100 / old - 100;
				i.production_rate[j] =  inew;

				if (inew >= _industry_spec[i.type].production_rate[j] / 4)
					closeit = false;

				mag = Math.abs(percent);
				if (mag >= 10) {
					Global.SetDParam(2, mag);
					Global.SetDParam(0, Global._cargoc.names_s[i.produced_cargo[j]]);
					Global.SetDParam(1, i.index);
					NewsItem.AddNewsItem(
							percent >= 0 ? Str.STR_INDUSTRY_PROD_GOUP : Str.STR_INDUSTRY_PROD_GODOWN,
									NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_TILE, NewsItem.NT_ECONOMY, 0),
									i.xy.iadd(1, 1).tile, 0
							);
				}
			}
			break;
		}

		if (closeit) {
			i.prod_level = 0;
			Global.SetDParam(0, i.index);
			NewsItem.AddNewsItem(
					_industry_close_strings[i.type],
					NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_TILE, NewsItem.NT_ECONOMY, 0),
					i.xy.iadd(1, 1).tile, 0
					);
		}
	}


	static void UpdateIndustryStatistics(Industry i)
	{
		int pct;

		if (i.produced_cargo[0] != AcceptedCargo.CT_INVALID) {
			pct = 0;
			if (i.last_mo_production[0] != 0) {
				i.last_prod_year =  Global.get_cur_year();
				pct =  Math.min(i.last_mo_transported[0] * 256 / i.last_mo_production[0],255);
			}
			i.pct_transported[0] = pct;

			i.total_production[0] = i.last_mo_production[0];
			i.last_mo_production[0] = 0;

			i.total_transported[0] = i.last_mo_transported[0];
			i.last_mo_transported[0] = 0;
		}

		if (i.produced_cargo[1] != AcceptedCargo.CT_INVALID) {
			pct = 0;
			if (i.last_mo_production[1] != 0) {
				i.last_prod_year =  Global.get_cur_year();
				pct =  Math.min(i.last_mo_transported[1] * 256 / i.last_mo_production[1],255);
			}
			i.pct_transported[1] = pct;

			i.total_production[1] = i.last_mo_production[1];
			i.last_mo_production[1] = 0;

			i.total_transported[1] = i.last_mo_transported[1];
			i.last_mo_transported[1] = 0;
		}


		if (i.produced_cargo[0] != AcceptedCargo.CT_INVALID || i.produced_cargo[1] != AcceptedCargo.CT_INVALID)
			Window.InvalidateWindow(Window.WC_INDUSTRY_VIEW, i.index);

		if (i.prod_level == 0) {
			DeleteIndustry(i);
		} else if (Global._patches.smooth_economy.get()) {
			ExtChangeIndustryProduction(i);
		}
	}

	static final byte _new_industry_rand[][] = {
			{12,12,12,12,12,12,12, 0, 0, 6, 6, 9, 9, 3, 3, 3,18,18, 4, 4, 2, 2, 5, 5, 5, 5, 5, 5, 1, 1, 8, 8},
			{16,16,16, 0, 0, 0, 9, 9, 9, 9,13,13, 3, 3, 3, 3,15,15,15, 4, 4,11,11,11,11,11,14,14, 1, 1, 7, 7},
			{21,21,21,24,22,22,22,22,23,23,12,12,12, 4, 4,19,19,19,13,13,20,20,20,11,11,11,17,17,17,10,10,10},
			{30,30,30,36,36,31,31,31,27,27,27,28,28,28,26,26,26,34,34,34,35,35,35,29,29,29,32,32,32,33,33,33},
	};

	static void MaybeNewIndustry(int r)
	{
		int type;
		int j;
		Industry i;

		type = _new_industry_rand[GameOptions._opt.landscape][BitOps.GB(r, 16, 5)];

		if (type == IT_OIL_WELL && Global.get_date() > 10958) return;
		if (type == IT_OIL_RIG  && Global.get_date() < 14610) return;

		j = 2000;
		for (;;) {
			i = CreateNewIndustry(Hal.RandomTile(), type);
			if (i != null) break;
			if (--j == 0) return;
		}

		Global.SetDParam(0, type + Str.STR_4802_COAL_MINE);
		Global.SetDParam(1, i.townId);
		NewsItem.AddNewsItem(
				(type != IT_FOREST) ?
						Str.STR_482D_NEW_UNDER_CONSTRUCTION : Str.STR_482E_NEW_BEING_PLANTED_NEAR,
						NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_TILE, NewsItem.NT_ECONOMY,0), i.xy.tile, 0
				);
	}

	static void ChangeIndustryProduction(Industry i)
	{
		boolean only_decrease = false;
		/*StringID*/ int str = Str.STR_NULL;
		int type = i.type;

		switch (_industry_close_mode[type]) {
		case INDUSTRY_NOT_CLOSABLE:
			return;

		case INDUSTRY_PRODUCTION:
			/* decrease or increase */
			if (type == IT_OIL_WELL && GameOptions._opt.landscape == Landscape.LT_NORMAL)
				only_decrease = true;

			if (only_decrease || BitOps.CHANCE16(1,3)) {
				/* If you transport > 60%, 66% chance we increase, else 33% chance we increase */
				if (!only_decrease && (i.pct_transported[0] > 153) != BitOps.CHANCE16(1,3)) {
					/* Increase production */
					if (i.prod_level != 0x80) {
						int b;

						i.prod_level <<= 1;

						b = i.production_rate[0] * 2;
						if (i.production_rate[0] >= 128)
							b = 0xFF;
						i.production_rate[0] =  b;

						b = i.production_rate[1] * 2;
						if (i.production_rate[1] >= 128)
							b = 0xFF;
						i.production_rate[1] =  b;

						str = _industry_prod_up_strings[type];
					}
				} else {
					/* Decrease production */
					if (i.prod_level == 4) {
						i.prod_level = 0;
						str = _industry_close_strings[type];
					} else {
						i.prod_level >>= 1;
						i.production_rate[0] =  ((i.production_rate[0] + 1) >> 1);
						i.production_rate[1] =  ((i.production_rate[1] + 1) >> 1);

						str = _industry_prod_down_strings[type];
					}
				}
			}
			break;

		case INDUSTRY_CLOSABLE:
			/* maybe close */
			if ( (Global.get_cur_year() - i.last_prod_year) >= 5 && BitOps.CHANCE16(1,2)) {
				i.prod_level = 0;
				str = _industry_close_strings[type];
			}
			break;
		}

		if (str != Str.STR_NULL) {
			Global.SetDParam(0, i.index);
			NewsItem.AddNewsItem(str, NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_TILE, NewsItem.NT_ECONOMY, 0), i.xy.iadd(1, 1).tile, 0);
		}
	}

	public static void IndustryMonthlyLoop()
	{
		PlayerID old_player = PlayerID.getCurrent();
		PlayerID.setCurrentToNone();

		Industry.forEach( (i) ->
		{
			if (i.isValid()) UpdateIndustryStatistics(i);
		});

		/* 3% chance that we start a new industry */
		if (BitOps.CHANCE16(3, 100)) {
			MaybeNewIndustry(Hal.Random());
		} else if (!Global._patches.smooth_economy.get() && _total_industries > 0) {
			Industry i = GetIndustry(Hal.RandomRange(_total_industries));
			if (i != null && i.isValid()) ChangeIndustryProduction(i);
		}

		PlayerID.setCurrent(old_player);

		// production-change
		_industry_sort_dirty = true;
		Window.InvalidateWindow(Window.WC_INDUSTRY_DIRECTORY, 0);
	}


	static void InitializeIndustries()
	{
		Global.gs._industries.CleanPool();
		Global.gs._industries.AddBlockToPool();

		_total_industries = 0;
		_industry_sort_dirty = true;
	}

	final static TileTypeProcs _tile_type_industry_procs = new TileTypeProcs(
			Industry::DrawTile_Industry,					/* draw_tile_proc */
			Industry::GetSlopeZ_Industry,					/* get_slope_z_proc */
			Industry::ClearTile_Industry,					/* clear_tile_proc */
			Industry::GetAcceptedCargo_Industry,	/* get_accepted_cargo_proc */
			Industry::GetTileDesc_Industry,				/* get_tile_desc_proc */
			Industry::GetTileTrackStatus_Industry,/* get_tile_track_status_proc */
			Industry::ClickTile_Industry,					/* click_tile_proc */
			Industry::AnimateTile_Industry,				/* animate_tile_proc */
			Industry::TileLoop_Industry,					/* tile_loop_proc */
			Industry::ChangeTileOwner_Industry,		/* change_tile_owner_proc */
			Industry::GetProducedCargo_Industry,  /* get_produced_cargo_proc */
			null,												/* vehicle_enter_tile_proc */
			null,												/* vehicle_leave_tile_proc */
			Industry::GetSlopeTileh_Industry			/* get_slope_tileh_proc */
			);
	/*
	static final SaveLoad _industry_desc[] = {
		SLE_CONDVAR(Industry, xy,					SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Industry, xy,					SLE_UINT32, 6, 255),
		SLE_VAR(Industry,width,						SLE_UINT8),
		SLE_VAR(Industry,height,					SLE_UINT8),
		SLE_REF(Industry,town,						REF_TOWN),
		SLE_ARR(Industry,produced_cargo,  SLE_UINT8, 2),
		SLE_ARR(Industry,cargo_waiting,   SLE_UINT16, 2),
		SLE_ARR(Industry,production_rate, SLE_UINT8, 2),
		SLE_ARR(Industry,accepts_cargo,		SLE_UINT8, 3),
		SLE_VAR(Industry,prod_level,			SLE_UINT8),
		SLE_ARR(Industry,last_mo_production,SLE_UINT16, 2),
		SLE_ARR(Industry,last_mo_transported,SLE_UINT16, 2),
		SLE_ARR(Industry,pct_transported,SLE_UINT8, 2),
		SLE_ARR(Industry,total_production,SLE_UINT16, 2),
		SLE_ARR(Industry,total_transported,SLE_UINT16, 2),

		SLE_VAR(Industry,counter,					SLE_UINT16),

		SLE_VAR(Industry,type,						SLE_UINT8),
		SLE_VAR(Industry,owner,						SLE_UINT8),
		SLE_VAR(Industry,color_map,				SLE_UINT8),
		SLE_VAR(Industry,last_prod_year,	SLE_UINT8),
		SLE_VAR(Industry,was_cargo_delivered,SLE_UINT8),

		// reserve extra space in savegame here. (currently 32 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U64 | SLE_VAR_null, 4, 2, 255),

		SLE_END()
	};

	static void Save_INDY()
	{
		Industry ind;

		// Write the vehicles
		FOR_ALL_INDUSTRIES(ind) {
			if (ind.xy != 0) {
				SlSetArrayIndex(ind.index);
				SlObject(ind, _industry_desc);
			}
		}
	}

	static void Load_INDY()
	{
		int index;

		_total_industries = 0;

		while ((index = SlIterateArray()) != -1) {
			Industry i;

			if (!AddBlockIfNeeded(&_industry_pool, index))
				error("Industries: failed loading savegame: too many industries");

			i = GetIndustry(index);
			SlObject(i, _industry_desc);

			if (index > _total_industries) _total_industries = index;
		}
	}

	final Chunk Handler _industry_chunk_handlers[] = {
		{ 'INDY', Save_INDY, Load_INDY, CH_ARRAY | CH_LAST},
	};

	 */


















	/* Present in table/build_industry.h" */
	//extern final byte _build_industry_types[4][12];
	//extern final byte _industry_type_costs[37];

	//static void UpdateIndustryProduction(Industry i);
	//extern void DrawArrowButtons(int x, int y, int state);

	static void BuildIndustryWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT:
			w.DrawWindowWidgets();
			if (ViewPort._thd.place_mode == 1 && ViewPort._thd.window_class == Window.WC_BUILD_INDUSTRY) {
				int ind_type = _build_industry_types[GameOptions._opt_ptr.landscape][w.as_def_d().data_1];

				Global.SetDParam(0, (((int)Global._price.build_industry) >> 5) * _industry_type_costs[ind_type]);
				Gfx.DrawStringCentered(85, w.getHeight() - 21, Str.STR_482F_COST, 0);
			}
			break;

		case WE_CLICK: {
			int wid = e.widget;
			if (wid >= 3) {
				if (Gui.HandlePlacePushButton(w, wid, Sprite.SPR_CURSOR_INDUSTRY, 1, null))
					w.as_def_d().data_1 = wid - 3;
			}
		} break;

		case WE_PLACE_OBJ:
			if (Cmd.DoCommandP(e.tile, _build_industry_types[GameOptions._opt_ptr.landscape][w.as_def_d().data_1], 0, null, Cmd.CMD_BUILD_INDUSTRY | Cmd.CMD_MSG(Str.STR_4830_CAN_T_CONSTRUCT_THIS_INDUSTRY)))
				ViewPort.ResetObjectToPlace();
			break;

		case WE_ABORT_PLACE_OBJ:
			w.click_state = 0;
			w.SetWindowDirty();
			break;
		default:
			break;
		}
	}

	static final Widget _build_industry_land0_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,											Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_0314_FUND_NEW_INDUSTRY,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   169,    14,   115, 0x0,														Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_0241_POWER_STATION,				Str.STR_0263_CONSTRUCT_POWER_STATION),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    29,    40, Str.STR_0242_SAWMILL,							Str.STR_0264_CONSTRUCT_SAWMILL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_0244_OIL_REFINERY,					Str.STR_0266_CONSTRUCT_OIL_REFINERY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_0246_FACTORY,							Str.STR_0268_CONSTRUCT_FACTORY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68,    79, Str.STR_0247_STEEL_MILL,						Str.STR_0269_CONSTRUCT_STEEL_MILL),

	};

	static final Widget _build_industry_land1_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,											Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_0314_FUND_NEW_INDUSTRY,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   169,    14,   115, 0x0,														Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_0241_POWER_STATION,				Str.STR_0263_CONSTRUCT_POWER_STATION),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    29,    40, Str.STR_024C_PAPER_MILL,						Str.STR_026E_CONSTRUCT_PAPER_MILL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_0244_OIL_REFINERY,					Str.STR_0266_CONSTRUCT_OIL_REFINERY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_024D_FOOD_PROCESSING_PLANT,Str.STR_026F_CONSTRUCT_FOOD_PROCESSING),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68,    79, Str.STR_024E_PRINTING_WORKS,				Str.STR_0270_CONSTRUCT_PRINTING_WORKS),

	};

	static final Widget _build_industry_land2_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,											Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_0314_FUND_NEW_INDUSTRY,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   169,    14,   115, 0x0,														Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_0250_LUMBER_MILL,					Str.STR_0273_CONSTRUCT_LUMBER_MILL_TO),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    29,    40, Str.STR_024D_FOOD_PROCESSING_PLANT,Str.STR_026F_CONSTRUCT_FOOD_PROCESSING),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_0244_OIL_REFINERY,					Str.STR_0266_CONSTRUCT_OIL_REFINERY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_0246_FACTORY,							Str.STR_0268_CONSTRUCT_FACTORY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68,    79, Str.STR_0254_WATER_TOWER,					Str.STR_0277_CONSTRUCT_WATER_TOWER_CAN),

	};

	static final Widget _build_industry_land3_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,											Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_0314_FUND_NEW_INDUSTRY,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   169,    14,   115, 0x0,														Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_0258_CANDY_FACTORY,				Str.STR_027B_CONSTRUCT_CANDY_FACTORY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    29,    40, Str.STR_025B_TOY_SHOP,							Str.STR_027E_CONSTRUCT_TOY_SHOP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_025C_TOY_FACTORY,					Str.STR_027F_CONSTRUCT_TOY_FACTORY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_025E_FIZZY_DRINK_FACTORY,	Str.STR_0281_CONSTRUCT_FIZZY_DRINK_FACTORY),

	};

	static final Widget _build_industry_land0_widgets_extra[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_0314_FUND_NEW_INDUSTRY,Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   169,    14,   187, 0x0,										Str.STR_NULL),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_0241_POWER_STATION,Str.STR_0263_CONSTRUCT_POWER_STATION),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    29,    40, Str.STR_0242_SAWMILL,			Str.STR_0264_CONSTRUCT_SAWMILL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_0244_OIL_REFINERY,	Str.STR_0266_CONSTRUCT_OIL_REFINERY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_0246_FACTORY,					Str.STR_0268_CONSTRUCT_FACTORY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68,    79, Str.STR_0247_STEEL_MILL,		Str.STR_0269_CONSTRUCT_STEEL_MILL),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    84,    95, Str.STR_0240_COAL_MINE,		Str.STR_CONSTRUCT_COAL_MINE_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    97,    108, Str.STR_0243_FOREST,			Str.STR_CONSTRUCT_FOREST_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    110,    121, Str.STR_0245_OIL_RIG,		Str.STR_CONSTRUCT_OIL_RIG_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    123,    134, Str.STR_0248_FARM,						Str.STR_CONSTRUCT_FARM_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    136,    147, Str.STR_024A_OIL_WELLS,			Str.STR_CONSTRUCT_OIL_WELLS_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    149,    160, Str.STR_0249_IRON_ORE_MINE,	Str.STR_CONSTRUCT_IRON_ORE_MINE_TIP),


	};

	static final Widget _build_industry_land1_widgets_extra[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_0314_FUND_NEW_INDUSTRY,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   169,    14,   174, 0x0,											Str.STR_NULL),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_0241_POWER_STATION,	Str.STR_0263_CONSTRUCT_POWER_STATION),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    29,    40, Str.STR_024C_PAPER_MILL,			Str.STR_026E_CONSTRUCT_PAPER_MILL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_0244_OIL_REFINERY,		Str.STR_0266_CONSTRUCT_OIL_REFINERY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_024D_FOOD_PROCESSING_PLANT,Str.STR_026F_CONSTRUCT_FOOD_PROCESSING),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68,    79, Str.STR_024E_PRINTING_WORKS,	Str.STR_0270_CONSTRUCT_PRINTING_WORKS),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    81+3,    92+3, Str.STR_0240_COAL_MINE,	Str.STR_CONSTRUCT_COAL_MINE_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    94+3,   105+3, Str.STR_0243_FOREST,			Str.STR_CONSTRUCT_FOREST_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    107+3,  118+3, Str.STR_0248_FARM,				Str.STR_CONSTRUCT_FARM_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    120+3,  131+3, Str.STR_024A_OIL_WELLS,	Str.STR_CONSTRUCT_OIL_WELLS_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    133+3,  144+3, Str.STR_024F_GOLD_MINE,	Str.STR_CONSTRUCT_GOLD_MINE_TIP),

	};

	static final Widget _build_industry_land2_widgets_extra[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_0314_FUND_NEW_INDUSTRY,			Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   169,    14,   200, 0x0,										Str.STR_NULL),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_0250_LUMBER_MILL,	Str.STR_0273_CONSTRUCT_LUMBER_MILL_TO),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    29,    40, Str.STR_024D_FOOD_PROCESSING_PLANT,Str.STR_026F_CONSTRUCT_FOOD_PROCESSING),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_0244_OIL_REFINERY,	Str.STR_0266_CONSTRUCT_OIL_REFINERY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_0246_FACTORY,			Str.STR_0268_CONSTRUCT_FACTORY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68,    79, Str.STR_0254_WATER_TOWER,	Str.STR_0277_CONSTRUCT_WATER_TOWER_CAN),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    81+3,    92+3, Str.STR_024A_OIL_WELLS,Str.STR_CONSTRUCT_OIL_WELLS_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    94+3,    105+3, Str.STR_0255_DIAMOND_MINE,			Str.STR_CONSTRUCT_DIAMOND_MINE_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    107+3,    118+3, Str.STR_0256_COPPER_ORE_MINE,	Str.STR_CONSTRUCT_COPPER_ORE_MINE_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    120+3,    131+3, Str.STR_0248_FARM,		Str.STR_CONSTRUCT_FARM_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    133+3,    144+3, Str.STR_0251_FRUIT_PLANTATION,	Str.STR_CONSTRUCT_FRUIT_PLANTATION_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    146+3,    157+3, Str.STR_0252_RUBBER_PLANTATION,Str.STR_CONSTRUCT_RUBBER_PLANTATION_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    159+3,    170+3, Str.STR_0253_WATER_SUPPLY,			Str.STR_CONSTRUCT_WATER_SUPPLY_TIP),

	};

	static final Widget _build_industry_land3_widgets_extra[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_0314_FUND_NEW_INDUSTRY,			Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   169,    14,   187, 0x0,	Str.STR_NULL),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_0258_CANDY_FACTORY,	Str.STR_027B_CONSTRUCT_CANDY_FACTORY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    29,    40, Str.STR_025B_TOY_SHOP,				Str.STR_027E_CONSTRUCT_TOY_SHOP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_025C_TOY_FACTORY,		Str.STR_027F_CONSTRUCT_TOY_FACTORY),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_025E_FIZZY_DRINK_FACTORY,		Str.STR_0281_CONSTRUCT_FIZZY_DRINK_FACTORY),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68+3,    79+3, Str.STR_0257_COTTON_CANDY_FOREST,Str.STR_CONSTRUCT_COTTON_CANDY_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    81+3,    92+3, Str.STR_0259_BATTERY_FARM,				Str.STR_CONSTRUCT_BATTERY_FARM_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    94+3,    105+3, Str.STR_025A_COLA_WELLS,				Str.STR_CONSTRUCT_COLA_WELLS_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    107+3,    118+3, Str.STR_025D_PLASTIC_FOUNTAINS,Str.STR_CONSTRUCT_PLASTIC_FOUNTAINS_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    120+3,    131+3, Str.STR_025F_BUBBLE_GENERATOR,	Str.STR_CONSTRUCT_BUBBLE_GENERATOR_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    133+3,    144+3, Str.STR_0260_TOFFEE_QUARRY,		Str.STR_CONSTRUCT_TOFFEE_QUARRY_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    146+3,    157+3, Str.STR_0261_SUGAR_MINE,				Str.STR_CONSTRUCT_SUGAR_MINE_TIP),

	};


	static final WindowDesc _build_industry_land0_desc = new WindowDesc(
			-1, -1, 170, 116,
			Window.WC_BUILD_INDUSTRY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_industry_land0_widgets,
			Industry::BuildIndustryWndProc
			);

	static final WindowDesc _build_industry_land1_desc = new WindowDesc(
			-1, -1, 170, 116,
			Window.WC_BUILD_INDUSTRY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_industry_land1_widgets,
			Industry::BuildIndustryWndProc
			);

	static final WindowDesc _build_industry_land2_desc = new WindowDesc(
			-1, -1, 170, 116,
			Window.WC_BUILD_INDUSTRY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_industry_land2_widgets,
			Industry::BuildIndustryWndProc
			);

	static final WindowDesc _build_industry_land3_desc = new WindowDesc(
			-1, -1, 170, 116,
			Window.WC_BUILD_INDUSTRY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_industry_land3_widgets,
			Industry::BuildIndustryWndProc
			);

	static final WindowDesc _build_industry_land0_desc_extra = new WindowDesc(
			-1, -1, 170, 188,
			Window.WC_BUILD_INDUSTRY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_industry_land0_widgets_extra,
			Industry::BuildIndustryWndProc
			);

	static final WindowDesc _build_industry_land1_desc_extra = new WindowDesc(
			-1, -1, 170, 175,
			Window.WC_BUILD_INDUSTRY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_industry_land1_widgets_extra,
			Industry::BuildIndustryWndProc
			);

	static final WindowDesc _build_industry_land2_desc_extra = new WindowDesc(
			-1, -1, 170, 201,
			Window.WC_BUILD_INDUSTRY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_industry_land2_widgets_extra,
			Industry::BuildIndustryWndProc
			);

	static final WindowDesc _build_industry_land3_desc_extra = new WindowDesc(
			-1, -1, 170, 188,
			Window.WC_BUILD_INDUSTRY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_industry_land3_widgets_extra,
			Industry::BuildIndustryWndProc
			);

	static final WindowDesc _industry_window_desc[][] = {
			{
				_build_industry_land0_desc,
				_build_industry_land1_desc,
				_build_industry_land2_desc,
				_build_industry_land3_desc,
			},
			{
				_build_industry_land0_desc_extra,
				_build_industry_land1_desc_extra,
				_build_industry_land2_desc_extra,
				_build_industry_land3_desc_extra,
			},
	};

	public static void ShowBuildIndustryWindow()
	{
		Window.AllocateWindowDescFront(_industry_window_desc[BitOps.b2i(Global._patches.build_rawmaterial_ind.get())][GameOptions._opt_ptr.landscape],0);
	}

	private static boolean NEED_ALTERB(Industry i) 
	{ 
		return 	(Global._game_mode == GameModes.GM_EDITOR || Global._cheats.setup_prod.value) 
				&& (i.accepts_cargo[0] == AcceptedCargo.CT_INVALID || i.accepts_cargo[0] == AcceptedCargo.CT_VALUABLES);
	}

	static void IndustryViewWndProc(Window w, WindowEvent e)
	{
		// w.as_vp2_d().data_1 is for the editbox line
		// w.as_vp2_d().data_2 is for the clickline
		// w.as_vp2_d().data_3 is for the click pos (left or right)

		switch(e.event) {
		case WE_PAINT: {
			final Industry i;
			//StringID 
			int str;

			i = GetIndustry(w.window_number);
			Global.SetDParam(0, w.window_number);
			w.DrawWindowWidgets();

			if (i.accepts_cargo[0] != AcceptedCargo.CT_INVALID) {
				Global.SetDParam(0, Global._cargoc.names_s[i.accepts_cargo[0]]);
				str = Str.STR_4827_REQUIRES;
				if (i.accepts_cargo[1] != AcceptedCargo.CT_INVALID) {
					Global.SetDParam(1, Global._cargoc.names_s[i.accepts_cargo[1]]);
					str++;
					if (i.accepts_cargo[2] != AcceptedCargo.CT_INVALID) {
						Global.SetDParam(2, Global._cargoc.names_s[i.accepts_cargo[2]]);
						str++;
					}
				}
				Gfx.DrawString(2, 107, str, 0);
			}

			if (i.produced_cargo[0] != AcceptedCargo.CT_INVALID) {
				Gfx.DrawString(2, 117, Str.STR_482A_PRODUCTION_LAST_MONTH, 0);

				Global.SetDParam(0, Global._cargoc.names_long[i.produced_cargo[0]]);
				Global.SetDParam(1, i.total_production[0]);

				Global.SetDParam(2, i.pct_transported[0] * 100 >> 8);
				Gfx.DrawString(4 + (NEED_ALTERB(i) ? 30 : 0), 127, Str.STR_482B_TRANSPORTED, 0);
				// Let's put out those buttons..
				if (NEED_ALTERB(i))
					SettingsGui.DrawArrowButtons(5, 127, (w.as_vp2_d().data_2 == 1 ? w.as_vp2_d().data_3 : 0));

				if (i.produced_cargo[1] != AcceptedCargo.CT_INVALID) {
					Global.SetDParam(0, Global._cargoc.names_long[i.produced_cargo[1]]);
					Global.SetDParam(1, i.total_production[1]);
					Global.SetDParam(2, i.pct_transported[1] * 100 >> 8);
					Gfx.DrawString(4 + (NEED_ALTERB(i) ? 30 : 0), 137, Str.STR_482B_TRANSPORTED, 0);
					// Let's put out those buttons..
					if (NEED_ALTERB(i))
						SettingsGui.DrawArrowButtons(5, 137, (w.as_vp2_d().data_2 == 2 ? w.as_vp2_d().data_3 : 0));
				}
			}

			w.DrawWindowViewport();
		}
		break;

		case WE_CLICK: {
			Industry i;

			switch(e.widget) {
			case 5: {
				int line;
				int x;
				int b;

				i = GetIndustry(w.window_number);

				// We should work if needed..
				if (!NEED_ALTERB(i))
					return;

				x = e.pt.x;
				line = (e.pt.y - 127) / 10;
				if (e.pt.y >= 127 && BitOps.IS_INT_INSIDE(line, 0, 2) && 0 != i.produced_cargo[line]) {
					if (BitOps.IS_INT_INSIDE(x, 5, 25) ) {
						// clicked buttons
						if (x < 15) {
							// decrease
							i.production_rate[line] /= 2;
							if (i.production_rate[line] < 4)
								i.production_rate[line] = 4;
						} else {
							// increase
							b =  (i.production_rate[line] * 2);
							if (i.production_rate[line] >= 128)
								b= 255;
							i.production_rate[line] = b;
						}
						UpdateIndustryProduction(i);
						w.SetWindowDirty();
						w.setTimeout(5);
						w.as_vp2_d().data_2 =  (line+1);
						w.as_vp2_d().data_3 =  (x < 15 ? 1 : 2);
					} else if (BitOps.IS_INT_INSIDE(x, 34, 160)) {
						// clicked the text
						w.as_vp2_d().data_1 =  line;
						Global.SetDParam(0, i.production_rate[line] * 8);
						MiscGui.ShowQueryString( new StringID( Str.STR_CONFIG_PATCHES_INT32 ),
								new StringID( Str.STR_CONFIG_GAME_PRODUCTION ),
								10, 100, w.getWindowClass(),
								w.window_number);
					}
				}
			}
			break;
			case 6:
				i = GetIndustry(w.window_number);
				ViewPort.ScrollMainWindowToTile(i.xy.iadd(1, 1));
				break;
			}
		}
		break;
		case WE_TIMEOUT:
			w.as_vp2_d().data_2 = 0;
			w.as_vp2_d().data_3 = 0;
			w.SetWindowDirty();
			break;

		case WE_ON_EDIT_TEXT:
			if (e.str != null) {
				Industry i;
				int val;
				int line;

				i = GetIndustry(w.window_number);
				line = w.as_vp2_d().data_1;
				val = Integer.parseInt(e.str);
				if (!BitOps.IS_INT_INSIDE(val, 32, 2040)) {
					if (val < 32) val = 32;
					else val = 2040;
				}
				i.production_rate[line] = (val / 8);
				UpdateIndustryProduction(i);
				w.SetWindowDirty();
			}
		default:
			break;
		}
	}

	static void UpdateIndustryProduction(Industry i)
	{
		if (i.produced_cargo[0] != AcceptedCargo.CT_INVALID)
			i.total_production[0] = 8 * i.production_rate[0];

		if (i.produced_cargo[1] != AcceptedCargo.CT_INVALID)
			i.total_production[1] = 8 * i.production_rate[1];
	}

	static final Widget _industry_view_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     9,     0,    10,     0,    13, Str.STR_00C5,	Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     9,    11,   247,     0,    13, Str.STR_4801,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     9,   248,   259,     0,    13, 0x0,       Str.STR_STICKY_BUTTON),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     9,     0,   259,    14,   105, 0x0,				Str.STR_NULL),
			new Widget(	  Window.WWT_6,   Window.RESIZE_NONE,     9,     2,   257,    16,   103, 0x0,				Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     9,     0,   259,   106,   147, 0x0,				Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     9,     0,   129,   148,   159, Str.STR_00E4_LOCATION,	Str.STR_482C_CENTER_THE_MAIN_VIEW_ON),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     9,   130,   259,   148,   159, 0x0,				Str.STR_NULL),

	};

	static final WindowDesc _industry_view_desc = new WindowDesc(
			-1, -1, 260, 160,
			Window.WC_INDUSTRY_VIEW,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON,
			_industry_view_widgets,
			Industry::IndustryViewWndProc
			);

	static void ShowIndustryViewWindow(int industry)
	{
		Window w;
		Industry i;

		w = Window.AllocateWindowDescFront(_industry_view_desc, industry);
		if (w != null) {
			w.disableVpScroll();
			w.as_vp2_d().data_1 = 0;
			w.as_vp2_d().data_2 = 0;
			w.as_vp2_d().data_3 = 0;
			i = GetIndustry(w.window_number);
			ViewPort.AssignWindowViewport( w, 3, 17, 0xFE, 0x56, i.xy.iadd(1, 1).tile, 1);
		}
	}

	static final Widget _industry_directory_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    13,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    13,    11,   495,     0,    13, Str.STR_INDUSTRYDIR_CAPTION,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,    13,   496,   507,     0,    13, 0x0,											Str.STR_STICKY_BUTTON),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,     0,   100,    14,    25, Str.STR_SORT_BY_NAME,					Str.STR_SORT_ORDER_TIP),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,   101,   200,    14,    25, Str.STR_SORT_BY_TYPE,					Str.STR_SORT_ORDER_TIP),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,   201,   300,    14,    25, Str.STR_SORT_BY_PRODUCTION,		Str.STR_SORT_ORDER_TIP),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,   301,   400,    14,    25, Str.STR_SORT_BY_TRANSPORTED,	Str.STR_SORT_ORDER_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    13,   401,   495,    14,    25, 0x0,											Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN, Window.RESIZE_BOTTOM,    13,     0,   495,    26,   189, 0x0,											Str.STR_200A_TOWN_NAMES_CLICK_ON_NAME),
			new Widget(  Window.WWT_SCROLLBAR, Window.RESIZE_BOTTOM,    13,   496,   507,    14,   177, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_RESIZEBOX,     Window.RESIZE_TB,    13,   496,   507,   178,   189, 0x0,											Str.STR_RESIZE_BUTTON),

	};


	//static char _bufcache[96];
	//static int _last_industry_idx;

	private static boolean _industry_sort_dirty = true;
	static int _industry_sort_order;
	private static Industry[] _industry_sort = null;
	public static int _total_industries = 0;


	private static class IndustryComparator implements Comparator<Industry> {
		public int compare(Industry i, Industry j) 
		{
			//int val;
			int r = 0;

			switch (_industry_sort_order >> 1) {
			/* case 0: Sort by Name (handled later) */
			case 1: /* Sort by Type */
				r = i.type - j.type;
				break;
				// FIXME - Production & Transported sort need to be inversed...but, WTF it does not wanna!
				// FIXME - And no simple -. "if (!(_industry_sort_order & 1)) r = -r;" hack at the bottom!!
			case 2: { /* Sort by Production */
				if (i.produced_cargo[0] != AcceptedCargo.CT_INVALID && j.produced_cargo[0] != AcceptedCargo.CT_INVALID) 
				{ // both industries produce cargo?
					if (i.produced_cargo[1] == AcceptedCargo.CT_INVALID) // producing one or two things?
						r = j.total_production[0] - i.total_production[0];
					else
						r = (j.total_production[0] + j.total_production[1]) / 2 - (i.total_production[0] + i.total_production[1]) / 2;
				} else if (i.produced_cargo[0] == AcceptedCargo.CT_INVALID && j.produced_cargo[0] == AcceptedCargo.CT_INVALID) // none of them producing anything, let them go to the name-sorting
					r = 0;
				else if (i.produced_cargo[0] == AcceptedCargo.CT_INVALID) // end up the non-producer industry first/last in list
					r = 1;
				else
					r = -1;
				break;
			}
			case 3: /* Sort by Transported amount */
				if (i.produced_cargo[0] != AcceptedCargo.CT_INVALID && j.produced_cargo[0] != AcceptedCargo.CT_INVALID) { // both industries produce cargo?
					if (i.produced_cargo[1] == AcceptedCargo.CT_INVALID) // producing one or two things?
						r = (j.pct_transported[0] * 100 >> 8) - (i.pct_transported[0] * 100 >> 8);
					else
						r = ((j.pct_transported[0] * 100 >> 8) + (j.pct_transported[1] * 100 >> 8)) / 2 - ((i.pct_transported[0] * 100 >> 8) + (i.pct_transported[1] * 100 >> 8)) / 2;
				} else if (i.produced_cargo[0] == AcceptedCargo.CT_INVALID && j.produced_cargo[0] == AcceptedCargo.CT_INVALID) // none of them producing anything, let them go to the name-sorting
					r = 0;
				else if (i.produced_cargo[0] == AcceptedCargo.CT_INVALID) // end up the non-producer industry first/last in list
					r = 1;
				else
					r = -1;
				break;
			}

			// default to string sorting if they are otherwise equal
			if (r == 0) {
				Global.SetDParam(0, i.townId);
				String buf1 = Strings.GetString(Str.STR_TOWN);

				Global.SetDParam(0, j.townId);
				String buf2 = Strings.GetString(Str.STR_TOWN);

				r = buf1.compareToIgnoreCase(buf2);
			}

			if( 0 != (_industry_sort_order & 1)) r = -r;
			return r;
		}
	}


	static void MakeSortedIndustryList()
	{
		_industry_sort_dirty = false;
		/* Create array for sorting */
		_industry_sort = Global.gs._industries.getValuesArray();

		if (_industry_sort == null)
			Global.fail("Could not allocate memory for the industry-sorting-list");

		Arrays.sort(_industry_sort, new IndustryComparator());

		Global.DEBUG_misc( 1, "Resorting Industries list...");
	}


	private static final int _indicator_positions[] = {88, 187, 284, 387};
	static void IndustryDirectoryWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			int n;
			int p;
			Industry i;

			if (_industry_sort_dirty) {
				MakeSortedIndustryList();
			}

			w.SetVScrollCount( _industry_sort.length);

			w.DrawWindowWidgets();
			Gfx.DoDrawString(0 != (_industry_sort_order & 1) ? Gfx.DOWNARROW : Gfx.UPARROW, _indicator_positions[_industry_sort_order>>1], 15, 0x10);

			p = w.vscroll.getPos();
			n = 0;

			while (p < _industry_sort.length) 
			{
				//i = GetIndustry(_industry_sort[p]);
				i = _industry_sort[p];
				Global.SetDParam(0, i.index);
				if (i.produced_cargo[0] != AcceptedCargo.CT_INVALID) {
					Global.SetDParam(1, Global._cargoc.names_long[i.produced_cargo[0]]);
					Global.SetDParam(2, i.total_production[0]);

					if (i.produced_cargo[1] != AcceptedCargo.CT_INVALID) {
						Global.SetDParam(3, Global._cargoc.names_long[i.produced_cargo[1]]);
						Global.SetDParam(4, i.total_production[1]);
						Global.SetDParam(5, i.pct_transported[0] * 100 >> 8);
						Global.SetDParam(6, i.pct_transported[1] * 100 >> 8);
						Gfx.DrawString(4, 28+n*10, Str.STR_INDUSTRYDIR_ITEM_TWO, 0);
					} else {
						Global.SetDParam(3, i.pct_transported[0] * 100 >> 8);
						Gfx.DrawString(4, 28+n*10, Str.STR_INDUSTRYDIR_ITEM, 0);
					}
				} else {
					Gfx.DrawString(4, 28+n*10, Str.STR_INDUSTRYDIR_ITEM_NOPROD, 0);
				}
				p++;
				if (++n == w.vscroll.getCap())
					break;
			}
		} break;

		case WE_CLICK:
			switch(e.widget) {
			case 3: {
				_industry_sort_order =  (_industry_sort_order==0 ? 1 : 0);
				_industry_sort_dirty = true;
				w.SetWindowDirty();
			} break;

			case 4: {
				_industry_sort_order = _industry_sort_order==2 ? 3 : 2;
				_industry_sort_dirty = true;
				w.SetWindowDirty();
			} break;

			case 5: {
				_industry_sort_order = _industry_sort_order==4 ? 5 : 4;
				_industry_sort_dirty = true;
				w.SetWindowDirty();
			} break;

			case 6: {
				_industry_sort_order = _industry_sort_order==6 ? 7 : 6;
				_industry_sort_dirty = true;
				w.SetWindowDirty();
			} break;

			case 8: {
				int y = (e.pt.y - 28) / 10;
				int p;
				Industry c;

				if (!BitOps.IS_INT_INSIDE(y, 0, w.vscroll.getCap()))
					return;
				p = y + w.vscroll.getPos();
				if (p < _industry_sort.length) {
					//c = Industry.GetIndustry(_industry_sort[p]);
					c = _industry_sort[p];
					ViewPort.ScrollMainWindowToTile(c.xy);
				}
			} break;
			}
			break;

		case WE_4:
			w.SetWindowDirty();
			break;

		case WE_RESIZE:
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / 10);
			break;
		default:
			break;
		}
	}


	/* Industry List */
	static final WindowDesc _industry_directory_desc = new WindowDesc(
			-1, -1, 508, 190,
			Window.WC_INDUSTRY_DIRECTORY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_industry_directory_widgets,
			Industry::IndustryDirectoryWndProc
			);



	public static void ShowIndustryDirectory()
	{
		/* Industry List */
		Window w = Window.AllocateWindowDescFront(_industry_directory_desc, 0);
		if (w != null) {
			w.vscroll.setCap(16);
			w.resize.height = w.getHeight() - 6 * 10; // minimum 10 items
			w.resize.step_height = 10;
			w.SetWindowDirty();
		}
	}



	/**
	 * 
	 * @return Total number of industries in game.
	 */
	public static int getCount()
	{
		return _total_industries;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getProduced_cargo(int i) {
		return produced_cargo[i];
	}

	public int getTotal_production(int i) {
		return total_production[i];
	}

	public int getTotal_transported(int i) {
		return total_transported[i];
	}

	public int getAccepts_cargo(int i) {
		return accepts_cargo[i];
	}



}


//typedef void IndustryDrawTileProc(final TileInfo ti);

@FunctionalInterface
interface IndustryDrawTileProc extends Consumer<TileInfo> {}


//typedef boolean CheckNewIndustryProc(TileIndex tile, int type);

@FunctionalInterface
interface CheckNewIndustryProc  {
	boolean check(TileIndex tile, int type);
}


