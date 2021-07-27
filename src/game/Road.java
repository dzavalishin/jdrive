package game;

import game.util.BitOps;

public class Road 
{





	/* When true, GetTrackStatus for roads will treat roads under refinalruction
	 * as normal roads instead of impassable. This is used when detecting whether
	 * a road can be removed. This is of course ugly, but I don't know a better
	 * solution just like that... */
	static boolean _road_special_gettrackstatus;

	//void RoadVehEnterDepot(Vehicle v);


	static boolean HasTileRoadAt(TileIndex tile, int i)
	{
		int mask;
		byte b;

		switch (tile.GetTileType()) {
		case MP_STREET:
			b = tile.getMap().m5;

			if ((b & 0xF0) == 0) {
			} else if (tile.IsLevelCrossing()) {
				b = (b&8)?5:10;
			} else if ((b & 0xF0) == 0x20) {
				return (~b & 3) == i;
			} else
				return false;
			break;

		case MP_STATION:
			b = tile.getMap().m5;
			if (!BitOps.IS_INT_INSIDE(b, 0x43, 0x43+8))
				return false;
			return ((~(b - 0x43) & 3) == i);

		case MP_TUNNELBRIDGE:
			mask = GetRoadBitsByTile(tile);
			b = 10; if(0 != (mask & 1)) break;
			b = 5;  if(0 != (mask & 2)) break;
			return false;

		default:
			return false;
		}

		return BitOps.HASBIT(b, i);
	}

	static boolean CheckAllowRemoveRoad(TileIndex tile, int br, boolean []edge_road)
	{
		int blocks;
		byte owner;
		int n;
		edge_road[0] = true;

		if (Global._game_mode == GameModes.GM_EDITOR)
			return true;

		blocks = GetRoadBitsByTile(tile);
		if (blocks == 0)
			return true;

		// Only do the special processing for actual players.
		if (Global._current_player.id >= Global.MAX_PLAYERS)
			return true;

		// A railway crossing has the road owner in the map3_lo byte.
		if (tile.IsTileType( TileTypes.MP_STREET) && tile.IsLevelCrossing()) {
			owner = tile.getMap().m3;
		} else {
			owner = tile.GetTileOwner();
		}
		// Only do the special processing if the road is owned
		// by a town
		if (owner != Owner.OWNER_TOWN) {
			return owner == Owner.OWNER_NONE || Player.CheckOwnership(owner);
		}

		if (Global._cheats.magic_bulldozer.value)
			return true;

		// Get a bitmask of which neighbouring roads has a tile
		n = 0;
		if (blocks&0x25 && HasTileRoadAt(TILE_ADDXY(tile,-1, 0), 1)) n |= 8;
		if (blocks&0x2A && HasTileRoadAt(TILE_ADDXY(tile, 0, 1), 0)) n |= 4;
		if (blocks&0x19 && HasTileRoadAt(TILE_ADDXY(tile, 1, 0), 3)) n |= 2;
		if (blocks&0x16 && HasTileRoadAt(TILE_ADDXY(tile, 0,-1), 2)) n |= 1;

		// If 0 or 1 bits are set in n, or if no bits that match the bits to remove,
		// then allow it
		if ((n & (n-1)) != 0 && (n & br) != 0) {
			Town t;
			edge_road[0] = false;
			// you can remove all kind of roads with extra dynamite
			if (Global._patches.extra_dynamite)
				return true;

			t = Town.ClosestTownFromTile(tile, Global._patches.dist_local_authority);

			Global.SetDParam(0, t.index);
			Global._error_message = Str.STR_2009_LOCAL_AUTHORITY_REFUSES;
			return false;
		}

		return true;
	}

	static int GetRoadBitsByTile(TileIndex tile)
	{
		int r = GetTileTrackStatus(tile, Global.TRANSPORT_ROAD);
		return (byte)(r | (r >> 8));
	}

	/** Delete a piece of road.
	 * @param x,y tile coordinates for road finalruction
	 * @param p1 road piece flags
	 * @param p2 unused
	 */
	int CmdRemoveRoad(int x, int y, int flags, int p1, int p2)
	{
		// cost for removing inner/edge -roads
		static final int road_remove_cost[] = {50, 18};

		TileInfo ti;
		int cost;
		TileIndex tile;
		PlayerID owner;
		Town t;
		/* true if the roadpiece was always removeable,
		 * false if it was a center piece. Affects town ratings drop */
		boolean edge_road;
		byte pieces = (byte)p1;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		/* Road pieces are max 4 bitset values (NE, NW, SE, SW) */
		if (pieces >> 4) return Cmd.CMD_ERROR;

		FindLandscapeHeight(&ti, x, y);
		tile = ti.tile;

		if (!tile.IsTileType( TileTypes.MP_STREET) && !tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)) return Cmd.CMD_ERROR;

		// owner for railroad crossing is stored somewhere else
		// XXX - Fix this so for a given tiletype the owner of the type is in the same variable
		owner = IsLevelCrossing(tile) ? tile.getMap().m3 : GetTileOwner(tile);

		if (owner == Owner.OWNER_TOWN && Global._game_mode != GameModes.GM_EDITOR) {
			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)) { // index of town is not saved for bridge (no space)
				t = ClosestTownFromTile(tile, Global._patches.dist_local_authority);
			} else
				t = GetTown(tile.getMap().m2);
		} else
			t = null;

		// allow deleting road under bridge
		if (ti.type != TileTypes.MP_TUNNELBRIDGE && !EnsureNoVehicle(tile))
			return Cmd.CMD_ERROR;

		{
			boolean b;
			_road_special_gettrackstatus = true;
			b = CheckAllowRemoveRoad(tile, pieces, &edge_road);
			_road_special_gettrackstatus = false;
			if (!b) return Cmd.CMD_ERROR;
		}

		if (ti.type == TileTypes.MP_TUNNELBRIDGE) {
			if (!EnsureNoVehicleZ(tile, TilePixelHeight(tile)))
				return Cmd.CMD_ERROR;

			if ((ti.map5 & 0xE9) == 0xE8) {
				if (pieces & 10) goto return_error;
			} else if ((ti.map5 & 0xE9) == 0xE9) {
				if (pieces & 5) goto return_error;
			} else
				goto return_error;

			cost = Global._price.remove_road * 2;

			if (flags & Cmd.DC_EXEC) {
				ChangeTownRating(t, -road_remove_cost[(byte)edge_road], RATING_ROAD_MINIMUM);
				tile.getMap().m5 = ti.map5 & 0xC7;
				SetTileOwner(tile, Owner.OWNER_NONE);
				MarkTileDirtyByTile(tile);
			}
			return cost;
		} else if (ti.type == TileTypes.MP_STREET) {
			// check if you're allowed to remove the street owned by a town
			// removal allowance depends on difficulty setting
			if (!CheckforTownRating(tile, flags, t, ROAD_REMOVE)) return Cmd.CMD_ERROR;

			// XXX - change cascading ifs to switch when doing rewrite
			if ((ti.map5 & 0xF0) == 0) { // normal road
				byte c = pieces, t2;

				if (ti.tileh != 0  && (ti.map5 == 5 || ti.map5 == 10)) {
					c |= (c & 0xC) >> 2;
					c |= (c & 0x3) << 2;
				}

				// limit the bits to delete to the existing bits.
				if ((c &= ti.map5) == 0) goto return_error;

				// calculate the cost
				t2 = c;
				cost = 0;
				do {
					if (t2&1) cost += Global._price.remove_road;
				} while(t2>>=1);

				if (flags & Cmd.DC_EXEC) {
					ChangeTownRating(t, -road_remove_cost[(byte)edge_road], RATING_ROAD_MINIMUM);

					tile.getMap().m5 ^= c;
					if (BitOps.GB(tile.getMap().m5, 0, 4) == 0) {
						DoClearSquare(tile);
					} else {
						MarkTileDirtyByTile(tile);
					}
				}
				return cost;
			} else if ((ti.map5 & 0xE0) == 0) { // railroad crossing
				byte c;

				if (!(ti.map5 & 8)) {
					c = 2;
					if (pieces & 5) goto return_error;
				} else {
					c = 1;
					if (pieces & 10) goto return_error;
				}

				cost = Global._price.remove_road * 2;
				if (flags & Cmd.DC_EXEC) {
					byte pbs_track = PBSTileReserved(tile);
					ChangeTownRating(t, -road_remove_cost[(byte)edge_road], RATING_ROAD_MINIMUM);

					ModifyTile(tile,
						TileTypes.MP_SETTYPE(TileTypes.MP_RAILWAY) |
						TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO | TileTypes.MP_MAP3HI_CLEAR | TileTypes.MP_MAP5,
						tile.getMap().m4 & 0xF, /* map3_lo */
						c											/* map5 */
					);
					if (pbs_track != 0)
						PBSReserveTrack(tile, FIND_FIRST_BIT(pbs_track));
				}
				return cost;
			} else
				goto return_error;

		} else {
	return_error:;
			return Cmd.return_cmd_error(INVALID_STRING_ID);
		}
	}


	//enum {
		public static final int ROAD_NW = 1; // NW road track
		public static final int ROAD_SW = 2; // SW road track
		public static final int ROAD_SE = 4; // SE road track
		public static final int ROAD_NE = 8; // NE road track
		public static final int ROAD_ALL = (ROAD_NW | ROAD_SW | ROAD_SE | ROAD_NE);
	//};

	static final byte _valid_tileh_slopes_road[][] = {
		// set of normal ones
		{
			ROAD_ALL, 0, 0,
			ROAD_SW | ROAD_NE, 0, 0,  // 3, 4, 5
			ROAD_NW | ROAD_SE, 0, 0,
			ROAD_NW | ROAD_SE, 0, 0,  // 9, 10, 11
			ROAD_SW | ROAD_NE, 0, 0
		},
		// allowed road for an evenly raised platform
		{
			0,
			ROAD_SW | ROAD_NW,
			ROAD_SW | ROAD_SE,
			ROAD_NW | ROAD_SE | ROAD_SW,

			ROAD_SE | ROAD_NE, // 4
			ROAD_ALL,
			ROAD_SW | ROAD_NE | ROAD_SE,
			ROAD_ALL,

			ROAD_NW | ROAD_NE, // 8
			ROAD_SW | ROAD_NE | ROAD_NW,
			ROAD_ALL,
			ROAD_ALL,

			ROAD_NW | ROAD_SE | ROAD_NE, // 12
			ROAD_ALL,
			ROAD_ALL
		},
		// valid railway crossings on slopes
		{
			1, 0, 0, // 0, 1, 2
			0, 0, 1, // 3, 4, 5
			0, 1, 0, // 6, 7, 8
			0, 1, 1, // 9, 10, 11
			0, 1, 1, // 12, 13, 14
		}
	};


	static int CheckRoadSlope(int tileh, byte [] pieces, byte existing)
	{
		if (!IsSteepTileh(tileh)) {
			byte road_bits = pieces[0] | existing;

			// no special foundation
			if ((~_valid_tileh_slopes_road[0][tileh] & road_bits) == 0) {
				// force that all bits are set when we have slopes
				if (tileh != 0) pieces[0] |= _valid_tileh_slopes_road[0][tileh];
				return 0; // no extra cost
			}

			// foundation is used. Whole tile is leveled up
			if ((~_valid_tileh_slopes_road[1][tileh] & road_bits) == 0) {
				return 0 !=existing ? 0 : Global._price.terraform;
			}

			// partly leveled up tile, only if there's no road on that tile
			if ( 0 == existing && (tileh == 1 || tileh == 2 || tileh == 4 || tileh == 8) ) {
				// force full pieces.
				pieces[0] |= (pieces[0] & 0xC) >> 2;
				pieces[0] |= (pieces[0] & 0x3) << 2;
				return (pieces[0] == (ROAD_NE|ROAD_SW) || pieces[0] == (ROAD_SE|ROAD_NW)) ? Global._price.terraform : Cmd.CMD_ERROR;
			}
		}
		return Cmd.CMD_ERROR;
	}

	/** Build a piece of road.
	 * @param x,y tile coordinates for road finalruction
	 * @param p1 road piece flags
	 * @param p2 the town that is building the road (0 if not applicable)
	 */
	int CmdBuildRoad(int x, int y, int flags, int p1, int p2)
	{
		TileInfo ti = new TileInfo();
		int cost;
		byte pieces = (byte)p1, existing = 0;
		TileIndex tile;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		/* Road pieces are max 4 bitset values (NE, NW, SE, SW) and town can only be non-zero
		 * if a non-player is building the road */
		if ((pieces >> 4) || (Global._current_player < Global.MAX_PLAYERS && p2 != 0) || !IsTownIndex(p2)) return Cmd.CMD_ERROR;

		FindLandscapeHeight(ti, x, y);
		tile = ti.tile;

		// allow building road under bridge
		if (ti.type != TileTypes.MP_TUNNELBRIDGE && !EnsureNoVehicle(tile)) return Cmd.CMD_ERROR;

		if (ti.type == TileTypes.MP_STREET) {
			if (!(ti.map5 & 0xF0)) {
				if ((pieces & (byte)ti.map5) == pieces)
					return Cmd.return_cmd_error(Str.STR_1007_ALREADY_BUILT);
				existing = ti.map5;
			} else {
				if (!(ti.map5 & 0xE0) && pieces != ((ti.map5 & 8) ? 5 : 10))
					return Cmd.return_cmd_error(Str.STR_1007_ALREADY_BUILT);
				goto do_clear;
			}
		} else if (ti.type == TileTypes.MP_RAILWAY) {
			byte m5;

			if (IsSteepTileh(ti.tileh)) // very steep tile
					return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);

			if(!_valid_tileh_slopes_road[2][ti.tileh]) // prevent certain slopes
					return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);

			if (ti.map5 == 2) {
				if (pieces & 5) goto do_clear;
				m5 = 0x10;
			} else if (ti.map5 == 1) {
				if (pieces & 10) goto do_clear;
				m5 = 0x18;
			} else
				goto do_clear;

			if(0 != (flags & Cmd.DC_EXEC)) 
			{
				byte pbs_track = PBSTileReserved(tile);
				Landscape.ModifyTile(tile,
					TileTypes.MP_SETTYPE(TileTypes.MP_STREET) |
					TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO | TileTypes.MP_MAP3HI | TileTypes.MP_MAP5,
					p2,
					Global._current_player, /* map3_lo */
					tile.getMap().m3 & 0xF, /* map3_hi */
					m5 /* map5 */
				);
				if (pbs_track != 0)
					PBSReserveTrack(tile, FIND_FIRST_BIT(pbs_track));
			}
			return Global._price.build_road * 2;
		} else if (ti.type == TileTypes.MP_TUNNELBRIDGE) {

			/* check for flat land */
			if (IsSteepTileh(ti.tileh)) // very steep tile
					return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);

			/* is this middle part of a bridge? */
			if ((ti.map5 & 0xC0) != 0xC0)
					goto do_clear;

			/* only allow roads pertendicular to bridge */
			if ((pieces & 5U) == (ti.map5 & 0x01U))
					goto do_clear;

			/* check if clear land under bridge */
			if ((ti.map5 & 0xF8) == 0xE8) 			/* road under bridge */
					return Cmd.return_cmd_error(Str.STR_1007_ALREADY_BUILT);
			else if ((ti.map5 & 0xE0) == 0xE0) 	/* other transport route under bridge */
					return Cmd.return_cmd_error(Str.STR_1008_MUST_REMOVE_RAILROAD_TRACK);
			else if ((ti.map5 & 0xF8) == 0xC8) 	/* water under bridge */
					return Cmd.return_cmd_error(Str.STR_3807_CAN_T_BUILD_ON_WATER);

			/* all checked, can build road now! */
			cost = Global._price.build_road * 2;
			if (flags & Cmd.DC_EXEC) {
				ModifyTile(tile,
					TileTypes.MP_MAPOwner.OWNER_CURRENT | TileTypes.MP_MAP5,
					(ti.map5 & 0xC7) | 0x28 // map5
				);
			}
			return cost;
		} else {
	do_clear:;
			if (CmdFailed(DoCommandByTile(tile, 0, 0, flags & ~Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR)))
				return Cmd.CMD_ERROR;
		}

		cost = CheckRoadSlope(ti.tileh, &pieces, existing);
		if (CmdFailed(cost)) return Cmd.return_cmd_error(Str.STR_1800_LAND_SLOPED_IN_WRONG_DIRECTION);

		if (cost && (!Global._patches.build_on_slopes || _is_old_ai_player))
			return Cmd.CMD_ERROR;

		if (ti.type != TileTypes.MP_STREET || (ti.map5 & 0xF0) != 0) {
			cost += DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		} else {
			// Don't put the pieces that already exist
			pieces &= ~ti.map5;
		}

		{
			byte t = pieces;
			while (t) {
				if (t & 1) cost += Global._price.build_road;
				t >>= 1;
			}
		}

		if (flags & Cmd.DC_EXEC) {
			if (ti.type != TileTypes.MP_STREET) {
				SetTileType(tile, TileTypes.MP_STREET);
				tile.getMap().m5 = 0;
				tile.getMap().m2 = p2;
				SetTileOwner(tile, Global._current_player);
			}

			tile.getMap().m5 |= pieces;

			MarkTileDirtyByTile(tile);
		}
		return cost;
	}

	int DoConvertStreetRail(TileIndex tile, int totype, boolean exec)
	{
		// not a railroad crossing?
		if (!tile.IsLevelCrossing()) return Cmd.CMD_ERROR;

		// not owned by me?
		if (!tile.CheckTileOwnership() || !tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		// tile is already of requested type?
		if (BitOps.GB(tile.getMap().m4, 0, 4) == totype) return Cmd.CMD_ERROR;

		if (exec) {
			// change type.
			tile.getMap().m4 = (byte) BitOps.RETSB(tile.getMap().m4, 0, 4, totype);
			tile.MarkTileDirtyByTile();
		}

		return Global._price.build_rail >> 1;
	}


	/** Build a long piece of road.
	 * @param x,y end tile of drag
	 * @param p1 start tile of drag
	 * @param p2 various bitstuffed elements
	 * - p2 = (bit 0) - start tile starts in the 2nd half of tile (p2 & 1)
	 * - p2 = (bit 1) - end tile starts in the 2nd half of tile (p2 & 2)
	 * - p2 = (bit 2) - direction: 0 = along x-axis, 1 = along y-axis (p2 & 4)
	 */
	int CmdBuildLongRoad(int x, int y, int flags, int p1, int p2)
	{
		TileIndex start_tile, end_tile, tile;
		int cost, ret;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (p1 > Global.MapSize()) return Cmd.CMD_ERROR;

		start_tile = p1;
		end_tile = TileVirtXY(x, y);

		/* Only drag in X or Y direction dictated by the direction variable */
		if (!BitOps.HASBIT(p2, 2) && TileY(start_tile) != TileY(end_tile)) return Cmd.CMD_ERROR; // x-axis
		if (BitOps.HASBIT(p2, 2)  && TileX(start_tile) != TileX(end_tile)) return Cmd.CMD_ERROR; // y-axis

		/* Swap start and ending tile, also the half-tile drag var (bit 0 and 1) */
		if (start_tile > end_tile || (start_tile == end_tile && BitOps.HASBIT(p2, 0))) {
			TileIndex t = start_tile;
			start_tile = end_tile;
			end_tile = t;
			p2 ^= BitOps.IS_INT_INSIDE(p2&3, 1, 3) ? 3 : 0;
		}

		cost = 0;
		tile = start_tile;
		// Start tile is the small number.
		for (;;) {
			int bits = BitOps.HASBIT(p2, 2) ? ROAD_SE | ROAD_NW : ROAD_SW | ROAD_NE;
			if (tile == end_tile && !BitOps.HASBIT(p2, 1)) bits &= ROAD_NW | ROAD_NE;
			if (tile == start_tile && BitOps.HASBIT(p2, 0)) bits &= ROAD_SE | ROAD_SW;

			ret = DoCommandByTile(tile, bits, 0, flags, Cmd.CMD_BUILD_ROAD);
			if (CmdFailed(ret)) {
				if (Global._error_message != Str.STR_1007_ALREADY_BUILT) return Cmd.CMD_ERROR;
			} else {
				cost += ret;
			}

			if (tile == end_tile) break;

			tile += BitOps.HASBIT(p2, 2) ? TileDiffXY(0, 1) : TileDiffXY(1, 0);
		}

		return (cost == 0) ? Cmd.CMD_ERROR : cost;
	}

	/** Remove a long piece of road.
	 * @param x,y end tile of drag
	 * @param p1 start tile of drag
	 * @param p2 various bitstuffed elements
	 * - p2 = (bit 0) - start tile starts in the 2nd half of tile (p2 & 1)
	 * - p2 = (bit 1) - end tile starts in the 2nd half of tile (p2 & 2)
	 * - p2 = (bit 2) - direction: 0 = along x-axis, 1 = along y-axis (p2 & 4)
	 */
	int CmdRemoveLongRoad(int x, int y, int flags, int p1, int p2)
	{
		TileIndex start_tile, end_tile, tile;
		int cost, ret;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (p1 > Global.MapSize()) return Cmd.CMD_ERROR;

		start_tile = p1;
		end_tile = TileVirtXY(x, y);

		/* Only drag in X or Y direction dictated by the direction variable */
		if (!BitOps.HASBIT(p2, 2) && TileY(start_tile) != TileY(end_tile)) return Cmd.CMD_ERROR; // x-axis
		if (BitOps.HASBIT(p2, 2)  && TileX(start_tile) != TileX(end_tile)) return Cmd.CMD_ERROR; // y-axis

		/* Swap start and ending tile, also the half-tile drag var (bit 0 and 1) */
		if (start_tile > end_tile || (start_tile == end_tile && BitOps.HASBIT(p2, 0))) {
			TileIndex t = start_tile;
			start_tile = end_tile;
			end_tile = t;
			p2 ^= BitOps.IS_INT_INSIDE(p2&3, 1, 3) ? 3 : 0;
		}

		cost = 0;
		tile = start_tile;
		// Start tile is the small number.
		for (;;) {
			int bits = BitOps.HASBIT(p2, 2) ? ROAD_SE | ROAD_NW : ROAD_SW | ROAD_NE;
			if (tile == end_tile && !BitOps.HASBIT(p2, 1)) bits &= ROAD_NW | ROAD_NE;
			if (tile == start_tile && BitOps.HASBIT(p2, 0)) bits &= ROAD_SE | ROAD_SW;

			// try to remove the halves.
			if (bits) {
				ret = DoCommandByTile(tile, bits, 0, flags, Cmd.CMD_REMOVE_ROAD);
				if (!CmdFailed(ret)) cost += ret;
			}

			if (tile == end_tile) break;

			tile += BitOps.HASBIT(p2, 2) ? TileDiffXY(0, 1) : TileDiffXY(1, 0);
		}

		return (cost == 0) ? Cmd.CMD_ERROR : cost;
	}

	/** Build a road depot.
	 * @param x,y tile coordinates where the depot will be built
	 * @param p1 depot direction (0 through 3), where 0 is NW, 1 is NE, etc.
	 * @param p2 unused
	 *
	 * @todo When checking for the tile slope,
	 * distingush between "Flat land required" and "land sloped in wrong direction"
	 */
	int CmdBuildRoadDepot(int x, int y, int flags, int p1, int p2)
	{
		TileInfo ti = new TileInfo();
		int cost;
		Depot dep;
		TileIndex tile;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (p1 > 3) return Cmd.CMD_ERROR; // check direction

		Landscape.FindLandscapeHeight(ti, x, y);

		tile = ti.tile;

		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		if (ti.tileh != 0 && (
					!Global._patches.build_on_slopes ||
					IsSteepTileh(ti.tileh) ||
					!CanBuildDepotByTileh(p1, ti.tileh)
				)) {
			return Cmd.return_cmd_error(Str.STR_0007_FLAT_LAND_REQUIRED);
		}

		cost = Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(cost)) return Cmd.CMD_ERROR;

		dep = AllocateDepot();
		if (dep == null) return Cmd.CMD_ERROR;

		if (flags & Cmd.DC_EXEC) {
			if (Player.IsLocalPlayer()) Global._last_built_road_depot_tile = tile;

			dep.xy = tile;
			dep.town_index = ClosestTownFromTile(tile, (int)-1).index;

			ModifyTile(tile,
				TileTypes.MP_SETTYPE(TileTypes.MP_STREET) |
				TileTypes.MP_MAPOwner.OWNER_CURRENT | TileTypes.MP_MAP5,
				(p1 | 0x20) /* map5 */
			);

		}
		return cost + Global._price.build_road_depot;
	}

	static int RemoveRoadDepot(TileIndex tile, int flags)
	{
		if (!CheckTileOwnership(tile) && Global._current_player.id != Owner.OWNER_WATER)
			return Cmd.CMD_ERROR;

		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) Depot.DoDeleteDepot(tile);

		return Global._price.remove_road_depot;
	}

	private static int M( int x) { return  (1<<(x)); }

	static int ClearTile_Road(TileIndex tile, byte flags)
	{
		int ret;
		byte m5 = tile.getMap().m5;

		if ( (m5 & 0xF0) == 0) {
			byte b = (byte) (m5 & 0xF);

			if (0 == ((1 << b) & (M(1)|M(2)|M(4)|M(8))) ) {
				if ((0 == (flags & Cmd.DC_AI_BUILDING) || !tile.IsTileOwner( Owner.OWNER_TOWN)) && 0 != (flags & Cmd.DC_AUTO))
					return Cmd.return_cmd_error(Str.STR_1801_MUST_REMOVE_ROAD_FIRST);
			}
			return Cmd.DoCommandByTile(tile, b, 0, flags, Cmd.CMD_REMOVE_ROAD);
		} else if ( (m5 & 0xE0) == 0) {
			if(0 != (flags & Cmd.DC_AUTO))
				return Cmd.return_cmd_error(Str.STR_1801_MUST_REMOVE_ROAD_FIRST);

			ret = Cmd.DoCommandByTile(tile, (m5&8)?5:10, 0, flags, Cmd.CMD_REMOVE_ROAD);
			if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;

			if(0 !=  (flags & Cmd.DC_EXEC)) {
				Cmd.DoCommandByTile(tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
			}

			return ret;
		} else {
			if(0 !=  (flags & Cmd.DC_AUTO))
				return Cmd.return_cmd_error(Str.STR_2004_BUILDING_MUST_BE_DEMOLISHED);
			return RemoveRoadDepot(tile,flags);
		}
	}


	class DrawRoadTileStruct {
		int image;
		byte subcoord_x;
		byte subcoord_y;
	} 

	class DrawRoadSeqStruct {
		int image;
		byte subcoord_x;
		byte subcoord_y;
		byte width;
		byte height;
	} 




	int GetRoadFoundation(int tileh, int bits)
	{
		int i;
		// normal level sloped building
		if ((~_valid_tileh_slopes_road[1][tileh] & bits) == 0)
			return tileh;

		// inclined sloped building
		if ( ((i=0, tileh == 1) || (i+=2, tileh == 2) || (i+=2, tileh == 4) || (i+=2, tileh == 8)) &&
			((bits == (ROAD_SW | ROAD_NE)) || (i++, bits == (ROAD_NW | ROAD_SE))))
			return i + 15;

		// rail crossing
		if ((bits & 0x10) && _valid_tileh_slopes_road[2][tileh])
			return tileh;

		return 0;
	}

	final byte _road_sloped_sprites[] = {
		0,  0,  2,  0,
		0,  1,  0,  0,
		3,  0,  0,  0,
		0,  0
	};

	/**
	 * Draw ground sprite and road pieces
	 * @param ti TileInfo
	 * @param road RoadBits to draw
	 * @param ground_type Ground type
	 * @param snow Draw snow
	 * @param flat Draw foundation
	 */
	void DrawRoadBits(TileInfo ti, byte road, byte ground_type, boolean snow, boolean flat)
	{
		final DrawRoadTileStruct drts;
		PalSpriteID image = null;

		if (ti.tileh != 0) {
			int foundation;
			if (flat) {
				foundation = ti.tileh;
			} else {
				foundation = GetRoadFoundation(ti.tileh, road);
			}

			if (foundation != 0) DrawFoundation(ti, foundation);

			// DrawFoundation() modifies ti.
			// Default sloped sprites..
			if (ti.tileh != 0) image = _road_sloped_sprites[ti.tileh - 1] + 0x53F;
		}

		if (image == 0) image = _road_tile_sprites_1[road];

		if (ground_type == 0) image |= PALETTE_TO_BARE_LAND;

		if (snow) {
			image += 19;
		} else if (ground_type > 1 && ground_type != 6) {
			// Pavement tiles.
			image -= 19;
		}

		DrawGroundSprite(image);

		// Return if full detail is disabled, or we are zoomed fully out.
		if (!(Global._display_opt & DO_FULL_DETAIL) || _cur_dpi.zoom == 2) return;

		if (ground_type >= 6) {
			// Road works
			DrawGroundSprite(BitOps.HASBIT(road, 4) ? Sprite.SPR_EXCAVATION_X : Sprite.SPR_EXCAVATION_Y);
			return;
		}

		// Draw extra details.
		drts = _road_display_table[ground_type][road];
		while ((image = drts.image) != 0) {
			int x = ti.x | drts.subcoord_x;
			int y = ti.y | drts.subcoord_y;
			byte z = ti.z;
			if (ti.tileh != 0) z = GetSlopeZ(x, y);
			ViewPort.AddSortableSpriteToDraw(image, x, y, 2, 2, 0x10, z);
			drts++;
		}
	}

	static void DrawTile_Road(TileInfo ti)
	{
		//PalSpriteID 
		int image;
		int m2;

		if ( (ti.map5 & 0xF0) == 0) { // if it is a road the upper 4 bits are 0
			DrawRoadBits(ti, BitOps.GB(ti.map5, 0, 4), BitOps.GB(_m[ti.tile].m4, 4, 3), BitOps.HASBIT(_m[ti.tile].m4, 7), false);
		} else if ( (ti.map5 & 0xE0) == 0) { // railroad crossing
			int f = GetRoadFoundation(ti.tileh, ti.map5 & 0xF);
			if (f != 0) DrawFoundation(ti, f);

			image = GetRailTypeInfo(BitOps.GB(_m[ti.tile].m4, 0, 4)).base_sprites.crossing;

			if (BitOps.GB(ti.map5, 3, 1) == 0) image++; /* direction */

			if ( (ti.map5 & 4) != 0)
				image += 2;

			if ( _m[ti.tile].m4 & 0x80) {
				image += 8;
			} else {
				m2 = BitOps.GB(_m[ti.tile].m4, 4, 3);
				if (m2 == 0) image |= PALETTE_TO_BARE_LAND;
				if (m2 > 1) image += 4;
			}

			DrawGroundSprite(image);

			if (_debug_pbs_level >= 1) {
				byte pbs = PBSTileReserved(ti.tile);
				if (pbs & TRACK_BIT_DIAG1) DrawGroundSprite(0x3ED | PALETTE_CRASH);
				if (pbs & TRACK_BIT_DIAG2) DrawGroundSprite(0x3EE | PALETTE_CRASH);
				if (pbs & TRACK_BIT_UPPER) DrawGroundSprite(0x3EF | PALETTE_CRASH);
				if (pbs & TRACK_BIT_LOWER) DrawGroundSprite(0x3F0 | PALETTE_CRASH);
				if (pbs & TRACK_BIT_LEFT)  DrawGroundSprite(0x3F2 | PALETTE_CRASH);
				if (pbs & TRACK_BIT_RIGHT) DrawGroundSprite(0x3F1 | PALETTE_CRASH);
			}

		} else {
			int ormod;
			PlayerID player;
			final DrawRoadSeqStruct drss;

			if (ti.tileh != 0) { DrawFoundation(ti, ti.tileh); }

			ormod = PALETTE_TO_GREY;	//was this a bug/problem?
			player = GetTileOwner(ti.tile);
			if (player.id < Global.MAX_PLAYERS)
				ormod = PLAYER_SPRITE_COLOR(player);

			drss = _road_display_datas[ti.map5 & 0xF];

			DrawGroundSprite(drss++.image);

			for (; drss.image != 0; drss++) {
				int image = drss.image;

				if (image & PALETTE_MODIFIER_COLOR)
					image |= ormod;
				if (Global._display_opt & DO_TRANS_BUILDINGS) // show transparent depots
					MAKE_TRANSPARENT(image);

				ViewPort.AddSortableSpriteToDraw(image, ti.x | drss.subcoord_x,
					ti.y | drss.subcoord_y, drss.width, drss.height, 0x14, ti.z);
			}
		}
	}

	void DrawRoadDepotSprite(int x, int y, int image)
	{
		int ormod;
		final DrawRoadSeqStruct dtss;

		ormod = PLAYER_SPRITE_COLOR(Global._local_player);

		dtss = _road_display_datas[image];

		x += 33;
		y += 17;

		Gfx.DrawSprite(dtss++.image, x, y);

		for(; dtss.image != 0; dtss++) {
			Point pt = RemapCoords(dtss.subcoord_x, dtss.subcoord_y, 0);

			image = dtss.image;
			if (image & PALETTE_MODIFIER_COLOR) image |= ormod;

			Gfx.DrawSprite(image, x + pt.x, y + pt.y);
		}
	}

	static int GetSlopeZ_Road(final TileInfo  ti)
	{
		int z = ti.z;
		int th = ti.tileh;

		// check if it's a foundation
		if (ti.tileh != 0) {
			if ((ti.map5 & 0xE0) == 0) { /* road or crossing */
				int f = GetRoadFoundation(ti.tileh, ti.map5 & 0x3F);
				if (f != 0) {
					if (f < 15) {
						// leveled foundation
						return z + 8;
					}
					// inclined foundation
					th = _inclined_tileh[f - 15];
				}
			} else if ((ti.map5 & 0xF0) == 0x20) {
				// depot
				return z + 8;
			}
			return Landscape.GetPartialZ(ti.x&0xF, ti.y&0xF, th) + z;
		}
		return z; // normal Z if no slope
	}

	static int GetSlopeTileh_Road(final TileInfo ti)
	{
		// check if it's a foundation
		if (ti.tileh != 0) {
			if ((ti.map5 & 0xE0) == 0) { /* road or crossing */
				int f = GetRoadFoundation(ti.tileh, ti.map5 & 0x3F);
				if (f != 0) {
					if (f < 15) {
						// leveled foundation
						return 0;
					}
					// inclined foundation
					return _inclined_tileh[f - 15];
				}
			} else if ((ti.map5 & 0xF0) == 0x20) {
				// depot
				return 0;
			}
		}
		return ti.tileh;
	}

	static AcceptedCargo GetAcceptedCargo_Road(TileIndex tile )
	{
		AcceptedCargo ac = new AcceptedCargo();
				return ac;
		/* not used */
	}

	static void AnimateTile_Road(TileIndex tile)
	{
		if (tile.IsLevelCrossing()) tile.MarkTileDirtyByTile();
	}

	static final byte _town_road_types[][] = {
		{1,1},
		{2,2},
		{2,2},
		{5,5},
		{3,2},
	};

	static final byte _town_road_types_2[][] = {
		{1,1},
		{2,2},
		{3,2},
		{3,2},
		{3,2},
	};


	static void TileLoop_Road(TileIndex tile)
	{
		Town t;
		int grp;

		if (GameOptions._opt.landscape == Landscape.LT_HILLY) {
			// Fix snow style if the road is above the snowline
			if ((tile.getMap().m4 & 0x80) != ((tile.GetTileZ() > GameOptions._opt.snow_line) ? 0x80 : 0x00)) {
				tile.getMap().m4 ^= 0x80;
				tile.MarkTileDirtyByTile();
			}
		} else if (GameOptions._opt.landscape == Landscape.LT_DESERT) {
			// Fix desert style
			if (GetMapExtraBits(tile) == 1 && !(tile.getMap().m4 & 0x80)) {
				tile.getMap().m4 |= 0x80;
				tile.MarkTileDirtyByTile();
			}
		}

		if(0 != (tile.getMap().m5 & 0xE0))
			return;

		if (BitOps.GB(tile.getMap().m4, 4, 3) < 6) {
			t = Town.ClosestTownFromTile(tile, (int)-1);

			grp = 0;
			if (t != null) {
				grp = t.GetTownRadiusGroup(tile);

				// Show an animation to indicate road work
				if (t.road_build_months != 0 &&
						!(Map.DistanceManhattan(t.xy, tile) >= 8 && grp == 0) &&
						(tile.getMap().m5==5 || tile.getMap().m5==10)) {
					if (tile.GetTileSlope(null) == 0 && tile.EnsureNoVehicle() && BitOps.CHANCE16(1,20)) {
						tile.getMap().m4 |= (BitOps.GB(tile.getMap().m4, 4, 3) <=  2 ? 7 : 6) << 4;

						//SndPlayTileFx(SND_21_JACKHAMMER, tile);
						CreateEffectVehicleAbove(
								tile.TileX() * 16 + 7,
								tile.TileY() * 16 + 7,
							0,
							EV_BULLDOZER);
						tile.MarkTileDirtyByTile();
						return;
					}
				}
			}

			{
				final byte *p = (GameOptions._opt.landscape == Landscape.LT_CANDY) ? _town_road_types_2[grp] : _town_road_types[grp];
				byte b = (byte) BitOps.GB(tile.getMap().m4, 4, 3);

				if (b == p[0])
					return;

				if (b == p[1]) {
					b = p[0];
				} else if (b == 0) {
					b = p[1];
				} else {
					b = 0;
				}
				tile.getMap().m4 = BitOps.RETSB(tile.getMap().m4, 4, 3, b);
				tile.MarkTileDirtyByTile();
			}
		} else {
			// Handle road work
			//XXX undocumented

			byte b = tile.getMap().m4;
			//roadworks take place only
			//keep roadworks running for 16 loops
			//lower 4 bits of map3_hi store the counter now
			if ((b & 0xF) != 0xF) {
				tile.getMap().m4 = b + 1;
				return;
			}
			//roadworks finished
			tile.getMap().m4 = (BitOps.GB(b, 4, 3) == 6 ? 1 : 2) << 4;
			tile.MarkTileDirtyByTile();
		}
	}

	//void ShowRoadDepotWindow(TileIndex tile);

	static void ClickTile_Road(TileIndex tile)
	{
		if (BitOps.GB(tile.getMap().m5, 4, 4) == 2) ShowRoadDepotWindow(tile);
	}

	static final byte _road_trackbits[] = {
		0x0, 0x0, 0x0, 0x10, 0x0, 0x2, 0x8, 0x1A, 0x0, 0x4, 0x1, 0x15, 0x20, 0x26, 0x29, 0x3F,
	};

	static int GetTileTrackStatus_Road(TileIndex tile, /*TransportType*/ int mode)
	{
		if (mode == Global.TRANSPORT_RAIL) {
			if (!tile.IsLevelCrossing())
				return 0;
			return 0 != (tile.getMap().m5 & 8) ? 0x101 : 0x202;
		} else if  (mode == Global.TRANSPORT_ROAD) {
			byte b = tile.getMap().m5;
			if ((b & 0xF0) == 0) {
				/* Ordinary road */
				if (!_road_special_gettrackstatus && BitOps.GB(tile.getMap().m4, 4, 3) >= 6)
					return 0;
				return _road_trackbits[b&0xF] * 0x101;
			} else if (tile.IsLevelCrossing()) {
				/* Crossing */
				int r = 0x101;
				if (b&8) r <<= 1;

				if (b&4) {
					r *= 0x10001;
				}
				return r;
			}
		}
		return 0;
	}

	static final int _road_tile_strings[] = {
		Str.STR_1818_ROAD_RAIL_LEVEL_CROSSING,
		Str.STR_1817_ROAD_VEHICLE_DEPOT,

		Str.STR_1814_ROAD,
		Str.STR_1814_ROAD,
		Str.STR_1814_ROAD,
		Str.STR_1815_ROAD_WITH_STREETLIGHTS,
		Str.STR_1814_ROAD,
		Str.STR_1816_TREE_LINED_ROAD,
		Str.STR_1814_ROAD,
		Str.STR_1814_ROAD,
	};

	static TileDesc GetTileDesc_Road(TileIndex tile )
	{
		TileDesc td = new TileDesc();
		int i = (tile.getMap().m5 >> 4);
		if (i == 0)
			i = BitOps.GB(tile.getMap().m4, 4, 3) + 3;
		td.str = _road_tile_strings[i - 1];
		td.owner = tile.GetTileOwner().owner;
	}

	static final byte _roadveh_enter_depot_unk0[] = {
		8, 9, 0, 1
	};

	static int VehicleEnter_Road(Vehicle v, TileIndex tile, int x, int y)
	{
		if (tile.IsLevelCrossing()) {
			if (v.type == Vehicle.VEH_Train && BitOps.GB(tile.getMap().m5, 2, 1) == 0) {
				/* train crossing a road */
				//SndPlayVehicleFx(SND_0E_LEVEL_CROSSING, v);
				tile.getMap().m5 = (byte) BitOps.RETSB(tile.getMap().m5, 2, 1, 1);
				tile.MarkTileDirtyByTile();
			}
		} else if (BitOps.GB(tile.getMap().m5, 4, 4) == 2) {
			if (v.type == Vehicle.VEH_Road && v.u.road.frame == 11) {
				if (_roadveh_enter_depot_unk0[BitOps.GB(tile.getMap().m5, 0, 2)] == v.u.road.state) {
					RoadVehEnterDepot(v);
					return 4;
				}
			}
		}
		return 0;
	}

	static void VehicleLeave_Road(Vehicle v, TileIndex tile, int x, int y)
	{
		if (tile.IsLevelCrossing() && v.type == Vehicle.VEH_Train && v.next == null) {
			// Turn off level crossing lights
			tile.getMap().m5 = (byte) BitOps.RETSB(tile.getMap().m5, 2, 1, 0);
			tile.MarkTileDirtyByTile();
		}
	}

	static void ChangeTileOwner_Road(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		// road/rail crossing where the road is owned by the current player?
		if (old_player.id == tile.getMap().m3 && tile.IsLevelCrossing()) {
			tile.getMap().m3 = (new_player.id == Owner.OWNER_SPECTATOR) ? Owner.OWNER_NONE : new_player;
		}

		if (!tile.IsTileOwner(old_player)) return;

		if (new_player.id != Owner.OWNER_SPECTATOR) {
			tile.SetTileOwner(new_player);
		}	else {
			if (BitOps.GB(tile.getMap().m5, 4, 4) == 0) {
				tile.SetTileOwner(Owner.OWNER_NONE);
			} else if (tile.IsLevelCrossing()) {
				tile.getMap().m5 = (tile.getMap().m5&8) ? 0x5 : 0xA;
				tile.SetTileOwner( tile.getMap().m3);
				tile.getMap().m3 = 0;
				tile.getMap().m4 &= 0x80;
			} else {
				Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
			}
		}
	}

	void InitializeRoad()
	{
		Global._last_built_road_depot_tile = 0;
	}

	final TileTypeProcs _tile_type_road_procs = new TileTypeProcs(
		Road::DrawTile_Road,						/* draw_tile_proc */
		Road::GetSlopeZ_Road,						/* get_slope_z_proc */
		Road::ClearTile_Road,						/* clear_tile_proc */
		Road::GetAcceptedCargo_Road,		/* get_accepted_cargo_proc */
		Road::GetTileDesc_Road,					/* get_tile_desc_proc */
		Road::GetTileTrackStatus_Road,	/* get_tile_track_status_proc */
		Road::ClickTile_Road,						/* click_tile_proc */
		Road::AnimateTile_Road,					/* animate_tile_proc */
		Road::TileLoop_Road,						/* tile_loop_clear */
		Road::ChangeTileOwner_Road,			/* change_tile_owner_clear */
		null,											/* get_produced_cargo_proc */
		Road::VehicleEnter_Road,				/* vehicle_enter_tile_proc */
		Road::VehicleLeave_Road,				/* vehicle_leave_tile_proc */
		Road::GetSlopeTileh_Road				/* get_slope_tileh_proc */
	);

}
