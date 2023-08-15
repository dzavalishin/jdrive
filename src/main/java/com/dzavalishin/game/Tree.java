package com.dzavalishin.game;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.struct.Point;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.struct.TileIndexDiffC;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.tables.TreeTables;
import com.dzavalishin.util.ArrayPtr;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Sound;
import com.dzavalishin.util.TownTables;
import com.dzavalishin.xui.ViewPort;


public class Tree extends TreeTables {

	private static int _trees_tick_ctr;

	static int GetRandomTreeType(TileIndex tile, int seed)
	{
		switch (GameOptions._opt.landscape) {
		case Landscape.LT_NORMAL:
			return seed * 12 >> 8;

		case Landscape.LT_HILLY:
			return (seed >> 5) + 12;

		case Landscape.LT_DESERT:
			switch (tile.GetMapExtraBits()) {
			case 0:  return (seed >> 6) + 28;
			case 1:  return (seed > 12) ? -1 : 27;
			default: return (seed * 7 >> 8) + 20;
			}

		default:
			return (seed * 9 >> 8) + 32;
		}
	}

	static void PlaceTree(TileIndex tile, int r, int m5_or)
	{
		int tree = GetRandomTreeType(tile, BitOps.GB(r, 24, 8));
		int m5;

		if (tree >= 0) {
			tile.SetTileType(TileTypes.MP_TREES);

			m5 = BitOps.GB(r, 16, 8);
			if (BitOps.GB(m5, 0, 3) == 7) m5--; // there is no growth state 7

			tile.getMap().m5 = (m5 & 0x07);	// growth state;
			tile.getMap().m5 |=  m5 & 0xC0;	// amount of trees

			tile.getMap().m3 = tree;		// set type of tree
			tile.getMap().m4 = 0;		// no hedge

			// above snowline?
			if (GameOptions._opt.landscape == Landscape.LT_HILLY && tile.GetTileZ() > GameOptions._opt.snow_line) {
				tile.getMap().m2 = 0xE0;	// set land type to snow
				tile.getMap().m2 |= BitOps.GB(r, 24, 3); // randomize counter
			} else {
				tile.getMap().m2 = BitOps.GB(r, 24, 5); // randomize counter and ground
			}
		}
	}

	static void DoPlaceMoreTrees(TileIndex tile)
	{
		int i;

		for (i = 0; i < 1000; i++) {
			int r = Hal.Random();
			int x = BitOps.GB(r, 0, 5) - 16;
			int y = BitOps.GB(r, 8, 5) - 16;

			int dist = Math.abs(x) + Math.abs(y);

			TileIndex cur_tile = new TileIndex( TileIndex.TILE_MASK(tile.getTile() + TileIndex.TileDiffXY(x, y).diff) );

			/* Only on tiles within 13 squares from tile,
			    on clear tiles, and NOT on farm-tiles or rocks */
			if (dist <= 13 && cur_tile.IsTileType(TileTypes.MP_CLEAR) &&
					(cur_tile.getMap().m5 & 0x1F) != 0x0F && (cur_tile.getMap().m5 & 0x1C) != 8) {
				PlaceTree(cur_tile, r, dist <= 6 ? 0xC0 : 0);
			}
		}
	}

	static void PlaceMoreTrees()
	{
		int i = Map.ScaleByMapSize(BitOps.GB(Hal.Random(), 0, 5) + 25);
		do {
			DoPlaceMoreTrees(TileIndex.RandomTile());
		} while (--i > 0);
	}

	public static void PlaceTreesRandomly()
	{
		int i;

		i = Map.ScaleByMapSize(1000);
		do {
			int r = Hal.Random();
			//TileIndex tile = TileIndex.RandomTileSeed(r);
			TileIndex tile = TileIndex.RandomTile();
			/* Only on clear tiles, and NOT on farm-tiles or rocks */
			if (tile.IsTileType( TileTypes.MP_CLEAR) && (tile.getMap().m5 & 0x1F) != 0x0F && (tile.getMap().m5 & 0x1C) != 8) {
				PlaceTree(tile, r, 0);
			}
		} while (--i > 0);

		/* place extra trees at rainforest area */
		if (GameOptions._opt.landscape == Landscape.LT_DESERT) {
			i = Map.ScaleByMapSize(15000);

			do {
				int r = Hal.Random();
				TileIndex tile = TileIndex.RandomTileSeed(r);
				if (tile.IsTileType( TileTypes.MP_CLEAR) && tile.GetMapExtraBits() == 2) {
					PlaceTree(tile, r, 0);
				}
			} while (--i > 0);
		}
	}

	static void GenerateTrees()
	{
		int i;

		if (GameOptions._opt.landscape != Landscape.LT_CANDY) PlaceMoreTrees();

		for (i = GameOptions._opt.landscape == Landscape.LT_HILLY ? 15 : 6; i != 0; i--) {
			PlaceTreesRandomly();
		}
	}

	/** Plant a tree.
	 * @param x,y start tile of area-drag of tree plantation
	 * @param p1 tree type, -1 means random.
	 * @param p2 end tile of area-drag
	 */
	static int CmdPlantTree(int ex, int ey, int flags, int p1, int p2)
	{
		int cost;
		int sx, sy, x, y;

		if (p2 > Global.MapSize()) return Cmd.CMD_ERROR;
		/* Check the tree type. It can be random or some valid value within the current climate */
		if (p1 != -1 && p1 - _tree_base_by_landscape[GameOptions._opt.landscape] >= _tree_count_by_landscape[GameOptions._opt.landscape]) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_OTHER);

		TileIndex tp2 = new TileIndex(p2);

		// make sure sx,sy are smaller than ex,ey
		sx = tp2.TileX();
		sy = tp2.TileY();
		ex /= 16; ey /= 16;
		if (ex < sx) { int t = sx; sx = ex; ex = t; } // intswap(ex, sx);
		if (ey < sy) { int t = sy; sy = ey; ey = t; } // intswap(ey, sy);

		cost = 0; // total cost

		for (x = sx; x <= ex; x++) {
			for (y = sy; y <= ey; y++) {
				TileIndex tile = TileIndex.TileXY(x, y);

				if (!tile.EnsureNoVehicle()) continue;

				switch (tile.GetTileType()) {
				case MP_TREES:
					// no more space for trees?
					if (Global._game_mode != GameModes.GM_EDITOR && (tile.getMap().m5 & 0xC0) == 0xC0) {
						Global._error_message = Str.STR_2803_TREE_ALREADY_HERE;
						continue;
					}

					if( 0 != (flags & Cmd.DC_EXEC)) {
						tile.getMap().m5 += 0x40;
						tile.getMap().m5 &= 0xFF;
						tile.MarkTileDirtyByTile();
					}
					// 2x as expensive to add more trees to an existing tile
					cost += Global._price.build_trees * 2;
					break;

				case MP_CLEAR:
					if (!tile.IsTileOwner(Owner.OWNER_NONE)) {
						Global._error_message = Str.STR_2804_SITE_UNSUITABLE;
						continue;
					}

					// it's expensive to clear farmland
					if ((tile.getMap().m5 & 0x1F) == 0xF)
						cost += Global._price.clear_3;
					else if ((tile.getMap().m5 & 0x1C) == 8)
						cost += Global._price.clear_2;

					if( 0 != (flags & Cmd.DC_EXEC)) {
						int treetype;
						int m2;

						if (Global._game_mode != GameModes.GM_EDITOR && !PlayerID.getCurrent().isSpecial()) {
							Town t = Town.ClosestTownFromTile(tile, Global._patches.dist_local_authority);
							if (t != null)
								t.ChangeTownRating(TownTables.RATING_TREE_UP_STEP, TownTables.RATING_TREE_MAXIMUM);
						}

						switch (tile.getMap().m5 & 0x1C) {
						case 0x04:
							m2 = 16;
							break;

						case 0x10:
							m2 = ((tile.getMap().m5 & 3) << 6) | 0x20;
							break;

						default:
							m2 = 0;
							break;
						}

						treetype = p1;
						if (treetype == -1) {
							treetype = GetRandomTreeType(tile, BitOps.GB(Hal.Random(), 24, 8));
							if (treetype == -1) treetype = 27;
						}

						Landscape.ModifyTile(tile, TileTypes.MP_TREES,
								//TileTypes.MP_SETTYPE(TileTypes.MP_TREES) |
								TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO | TileTypes.MP_MAP3HI_CLEAR | TileTypes.MP_MAP5,
								m2, /* map2 */
								treetype, /* map3lo */
								Global._game_mode == GameModes.GM_EDITOR ? 3 : 0 /* map5 */
								);

						if (Global._game_mode == GameModes.GM_EDITOR && BitOps.IS_INT_INSIDE(treetype, 0x14, 0x1B))
							tile.SetMapExtraBits(2);
					}
					cost += Global._price.build_trees;
					break;

				default:
					Global._error_message = Str.STR_2804_SITE_UNSUITABLE;
					break;
				}
			}
		}

		return (cost == 0) ? Cmd.CMD_ERROR : cost;
	}

	static class TreeListEnt {
		int image;
		int x,y;
	} 

	static void DrawTile_Trees(TileInfo ti)
	{
		int m2;
		int z;

		//final int [] s;
		//final TreePos d;
		//final Point [] d;

		m2 = ti.tile.getMap().m2;

		if ((m2 & 0x30) == 0) {
			Clear.DrawClearLandTile(ti, 3);
		} else if ((m2 & 0x30) == 0x20) {
			ViewPort.DrawGroundSprite(_tree_sprites_1[m2 >> 6] + Landscape._tileh_to_sprite[ti.tileh]);
		} else {
			Clear.DrawHillyLandTile(ti);
		}

		Clear.DrawClearLandFence(ti);

		z = ti.z;
		if (ti.tileh != 0) {
			z += 4;
			if (TileIndex.IsSteepTileh(ti.tileh))
				z += 4;
		}
		ArrayPtr<Point> d;
		ArrayPtr<Integer> s;
		{
			int tmp = ti.x;
			int index;

			tmp = BitOps.ROR16(tmp, 2);
			tmp -= ti.y;
			tmp = BitOps.ROR16(tmp, 3);
			tmp -= ti.x;
			tmp = BitOps.ROR16(tmp, 1);
			tmp += ti.y;

			d = new ArrayPtr<>( _tree_layout_xy[BitOps.GB(tmp, 4, 2)] );

			index = BitOps.GB(tmp, 6, 2) + (ti.tile.M().m3 << 2);

			/* different tree styles above one of the grounds */
			if ((m2 & 0xB0) == 0xA0 && index >= 48 && index < 80)
				index += 164 - 48;

			assert(index < _tree_layout_sprite.length);
			int[] sp = _tree_layout_sprite[index];
			Integer[] ia = ArrayPtr.toIntegerArray(sp);
			s = new ArrayPtr<>(ia);
		}

		ViewPort.StartSpriteCombine();

		if (0==(Global._display_opt & Global.DO_TRANS_BUILDINGS) || !Global._patches.invisible_trees) {
			TreeListEnt [] te = new TreeListEnt[4];
			int i;

			/* put the trees to draw in a list */
			i = (ti.map5 >> 6) + 1;
			do {
				int image = s.r() + (--i == 0 ? BitOps.GB(ti.map5, 0, 3) : 3);
				if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS) ) 
					image = Sprite.RET_MAKE_TRANSPARENT(image);
				te[i] = new TreeListEnt();
				te[i].image = image;
				te[i].x =  d.r().x;
				te[i].y =  d.r().y;
				s.inc();
				d.inc();
			} while (i >0 );

			/* draw them in a sorted way */
			for(;;) {
				int min = 0xFF;
				TreeListEnt tep = null;

				i = (ti.map5 >> 6) + 1;
				do {
					if (te[--i].image != 0 && te[i].x + te[i].y < min) {
						min = (te[i].x + te[i].y);
						tep = te[i];
					}
				} while (i > 0);

				if (tep == null) break;

				ViewPort.AddSortableSpriteToDraw(tep.image, ti.x + tep.x, ti.y + tep.y, 5, 5, 0x10, z);
				tep.image = 0;
			}
		}

		ViewPort.EndSpriteCombine();
	}


	static int GetSlopeZ_Trees(final TileInfo  ti)
	{
		return Landscape.GetPartialZ(ti.x & 0xF, ti.y & 0xF, ti.tileh) + ti.z;
	}

	static int GetSlopeTileh_Trees(final TileInfo  ti)
	{
		return ti.tileh;
	}

	static int ClearTile_Trees(TileIndex tile, int flags)
	{
		int num;

		if ( (0 != (flags & Cmd.DC_EXEC)) && !PlayerID.getCurrent().isSpecial()) {
			Town t = Town.ClosestTownFromTile(tile, Global._patches.dist_local_authority);
			if (t != null)
				t.ChangeTownRating(TownTables.RATING_TREE_DOWN_STEP, TownTables.RATING_TREE_MINIMUM);
		}

		num = BitOps.GB(tile.getMap().m5, 6, 2) + 1;
		if (BitOps.IS_INT_INSIDE(tile.getMap().m3, 20, 26 + 1)) num *= 4;

		if(0 != (flags & Cmd.DC_EXEC)) Landscape.DoClearSquare(tile);

		return (int) (num * Global._price.remove_trees);
	}

	static AcceptedCargo GetAcceptedCargo_Trees(TileIndex tile )
	{
		/* not used */
		return null;
	}

	static TileDesc GetTileDesc_Trees(TileIndex tile)
	{
		TileDesc td = new TileDesc();
		int m3;
		//StringID str;
		int str;

		td.owner =  tile.GetTileOwner().id;

		m3 = tile.getMap().m3;
		//(str=Str.STR_2810_CACTUS_PLANTS, b==0x1B) ||
		//(str=Str.STR_280F_RAINFOREST, IS_BYTE_INSIDE(b, 0x14, 0x1A+1)) ||
		//(str=Str.STR_280E_TREES, true);

		str=Str.STR_2810_CACTUS_PLANTS;
		if(m3!=0x1B) 
		{
			str=Str.STR_280F_RAINFOREST;
			if( !BitOps.IS_BYTE_INSIDE(m3, 0x14, (0x1A+1)))
				str=Str.STR_280E_TREES;
		}


		td.str = str;
		return td;
	}

	static void AnimateTile_Trees(TileIndex tile)
	{
		/* not used */
	}

	static final Snd [] forest_sounds = {
			Snd.SND_42_LOON_BIRD,
			Snd.SND_43_LION,
			Snd.SND_44_MONKEYS,
			Snd.SND_48_DISTANT_BIRD
	};

	static void TileLoopTreesDesert(TileIndex tile)
	{

		int b =  tile.GetMapExtraBits();

		if (b == 2) {
			int r = Hal.Random();
			if (BitOps.CHANCE16I(1, 200, r)) Sound.SndPlayTileFx(forest_sounds[BitOps.GB(r, 16, 2)], tile);

		} else if (b == TileInfo.EXTRABITS_DESERT) {
			if (BitOps.GB(tile.getMap().m2, 4, 2) != 2) {
				tile.getMap().m2 = BitOps.RETSB(tile.getMap().m2, 4, 2, 2);
				tile.getMap().m2 = BitOps.RETSB(tile.getMap().m2, 6, 2, 3);
				tile.MarkTileDirtyByTile();
			}
		}
	}

	static void TileLoopTreesAlps(TileIndex tile)
	{
		int tmp, m2;
		int k;

		/* distance from snow line, in steps of 8 */
		k = tile.GetTileZ() - GameOptions._opt.snow_line;

		tmp =  (tile.getMap().m2 & 0xF0);

		if (k < -8) {
			if ((tmp & 0x30) != 0x20) return;
			m2 = 0; // no snow
		} else if (k == -8) {
			m2 = 0x20; // 1/4 snow
			if (tmp == m2) return;
		} else if (k == 0) {
			m2 = 0x60;// 1/2 snow
			if (tmp == m2) return;
		} else if (k == 8) {
			m2 =  0xA0; // 3/4 snow
			if (tmp == m2) return;
		} else {
			if (tmp == 0xE0) {
				int r = Hal.Random();
				if (BitOps.CHANCE16I(1, 200, r)) {
					Sound.SndPlayTileFx((r & 0x80000000) != 0 ? Snd.SND_39_HEAVY_WIND : Snd.SND_34_WIND, tile);
				}
				return;
			} else {
				m2 =  0xE0; // full snow
			}
		}

		tile.getMap().m2 &= 0xF;
		tile.getMap().m2 |= m2;
		tile.MarkTileDirtyByTile();
	}


	private static final TileIndexDiffC _tileloop_trees_dir[] = {
			new TileIndexDiffC(-1, -1),
			new TileIndexDiffC( 0, -1),
			new TileIndexDiffC( 1, -1),
			new TileIndexDiffC(-1,  0),
			new TileIndexDiffC( 1,  0),
			new TileIndexDiffC(-1,  1),
			new TileIndexDiffC( 0,  1),
			new TileIndexDiffC( 1,  1)
	};


	static void TileLoop_Trees(TileIndex tile)
	{
		int m5;
		int m2;

		if (GameOptions._opt.landscape == Landscape.LT_DESERT) {
			TileLoopTreesDesert(tile);
		} else if (GameOptions._opt.landscape == Landscape.LT_HILLY) {
			TileLoopTreesAlps(tile);
		}

		Clear.TileLoopClearHelper(tile);

		/* increase counter */
		tile.getMap().m2 = BitOps.RETAB(tile.getMap().m2, 0, 4, 1);
		if (BitOps.GB(tile.getMap().m2, 0, 4) != 0) return;

		m5 = tile.getMap().m5;
		if (BitOps.GB(m5, 0, 3) == 3) {
			/* regular sized tree */
			if (GameOptions._opt.landscape == Landscape.LT_DESERT && tile.getMap().m3 != 0x1B && tile.GetMapExtraBits() == 1) {
				m5++; /* start destructing */
			} else {
				switch (BitOps.GB(Hal.Random(), 0, 3)) {
				case 0: /* start destructing */
					m5++;
					break;

				case 1: /* add a tree */
					if (m5 < 0xC0) {
						m5 = ((m5 + 0x40) & ~7);
						break;
					}
					/* fall through */

				case 2: { /* add a neighbouring tree */
					int m3 = tile.getMap().m3;

					//tile += TileIndex.ToTileIndexDiff(_tileloop_trees_dir[Hal.Random() & 7]);
					tile = tile.iadd( TileIndex.ToTileIndexDiff(_tileloop_trees_dir[Hal.Random() & 7]) );

					if (!tile.IsTileType( TileTypes.MP_CLEAR)) return;

					if ( (tile.getMap().m5 & 0x1C) == 4) {
						tile.getMap().m2 = 0x10;
					} else if ((tile.getMap().m5 & 0x1C) == 16) {
						tile.getMap().m2 = ((tile.getMap().m5 & 3) << 6) | 0x20;
					} else {
						if ((tile.getMap().m5 & 0x1F) != 3) return;
						tile.getMap().m2 = 0;
					}

					tile.getMap().m3 = m3;
					tile.getMap().m4 = 0;
					tile.SetTileType(TileTypes.MP_TREES);

					m5 = 0;
					break;
				}

				default:
					return;
				}
			}
		} else if (BitOps.GB(m5, 0, 3) == 6) {
			/* final stage of tree destruction */
			if (BitOps.GB(m5, 6, 2) != 0) {
				/* more than one tree, delete it? */
				m5 = (((m5 - 6) - 0x40) + 3);
			} else {
				/* just one tree, change type into TileTypes.MP_CLEAR */
				tile.SetTileType(TileTypes.MP_CLEAR);

				m5 = 3;
				m2 = tile.getMap().m2;
				if ((m2 & 0x30) != 0) { // on snow/desert or rough land
					m5 =  ((m2 >> 6) | 0x10);
					if ((m2 & 0x30) != 0x20) // if not on snow/desert, then on rough land
						m5 = 7;
				}
				tile.SetTileOwner(Owner.OWNER_NONE);
			}
		} else {
			/* in the middle of a transition, change to next */
			m5++;
		}

		tile.getMap().m5 = 0xFF & m5;
		tile.MarkTileDirtyByTile();
	}

	static void OnTick_Trees()
	{
		int r;
		TileIndex tile;
		int m;
		int tree;

		/* place a tree at a random rainforest spot */
		if (GameOptions._opt.landscape == Landscape.LT_DESERT )
		{
			r = Hal.Random();
			tile = TileIndex.RandomTileSeed(r);
			m = tile.getMap().m5 & 0x1C;
			if(
					(tile.GetMapExtraBits() == 2) &&
					tile.IsTileType(TileTypes.MP_CLEAR) &&
					(m <= 4) &&
					(tree = GetRandomTreeType(tile, BitOps.GB(r, 24, 8))) >= 0
					) 
			{

				Landscape.ModifyTile(tile, TileTypes.MP_TREES,
						//TileTypes.MP_SETTYPE(TileTypes.MP_TREES) |
						TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO | TileTypes.MP_MAP3HI | TileTypes.MP_MAP5,
						(m == 4 ? 0x10 : 0),
						tree,
						tile.getMap().m4 & ~3,
						0
						);
			}
		}
		// byte underflow
		if (--_trees_tick_ctr > 0) return;
		_trees_tick_ctr = 0x7F;

		/* place a tree at a random spot */
		r = Hal.Random();
		tile = new TileIndex( TileIndex.TILE_MASK(r) );
		if (tile.IsTileType(TileTypes.MP_CLEAR)) 
		{
			m =  (tile.getMap().m5 & 0x1C);
			if(
					( m == 0 || m == 4 || m == 0x10) &&
					(tree = GetRandomTreeType(tile, BitOps.GB(r, 24, 8))) >= 0) 
			{
				int m2;

				if (m == 0) {
					m2 = 0;
				} else if (m == 4) {
					m2 = 0x10;
				} else {
					m2 = ((tile.getMap().m5 & 3) << 6) | 0x20;
				}

				Landscape.ModifyTile(tile, TileTypes.MP_TREES,
						//TileTypes.MP_SETTYPE(TileTypes.MP_TREES) |
						TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO | TileTypes.MP_MAP3HI | TileTypes.MP_MAP5,
						m2,
						tree,
						tile.getMap().m4 & ~3,
						0
						);
			}
		}
	}

	static void ClickTile_Trees(TileIndex tile)
	{
		/* not used */
	}

	static int GetTileTrackStatus_Trees(TileIndex tile, TransportType mode)
	{
		return 0;
	}

	static void ChangeTileOwner_Trees(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		/* not used */
	}

	static void InitializeTrees()
	{
		_trees_tick_ctr = 0;
	}


	public static final TileTypeProcs _tile_type_trees_procs = new TileTypeProcs(
			Tree::DrawTile_Trees,						/* draw_tile_proc */
			Tree::GetSlopeZ_Trees,					/* get_slope_z_proc */
			Tree::ClearTile_Trees,					/* clear_tile_proc */
			Tree::GetAcceptedCargo_Trees,		/* get_accepted_cargo_proc */
			Tree::GetTileDesc_Trees,				/* get_tile_desc_proc */
			Tree::GetTileTrackStatus_Trees,	/* get_tile_track_status_proc */
			Tree::ClickTile_Trees,					/* click_tile_proc */
			Tree::AnimateTile_Trees,				/* animate_tile_proc */
			Tree::TileLoop_Trees,						/* tile_loop_clear */
			Tree::ChangeTileOwner_Trees,		/* change_tile_owner_clear */
			null,											/* get_produced_cargo_proc */
			null,											/* vehicle_enter_tile_proc */
			null,											/* vehicle_leave_tile_proc */
			Tree::GetSlopeTileh_Trees			/* get_slope_tileh_proc */
			);

}
