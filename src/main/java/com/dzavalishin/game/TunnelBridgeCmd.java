package com.dzavalishin.game;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.tables.TunnelBridgeTables;
import com.dzavalishin.util.TownTables;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.struct.TileIndexDiff;
import com.dzavalishin.tables.RailTables;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.IntContainer;

/**
 * This class deals with tunnels and bridges (non-gui stuff)
 *
 * TODO seperate this file into two
 */
public class TunnelBridgeCmd extends TunnelBridgeTables
{

	static int _build_tunnel_bh;
	static int _build_tunnel_railtype;



	//extern final byte _track_sloped_sprites[14];
	//extern final SpriteID _water_shore_sprites[15];

	//extern void DrawCanalWater(TileIndex tile);


	//static final Bridge [] _bridge = new Bridge[Bridge.MAX_BRIDGES];


	// calculate the price factor for building a long bridge.
	// basically the cost delta is 1,1, 1, 2,2, 3,3,3, 4,4,4,4, 5,5,5,5,5, 6,6,6,6,6,6,  7,7,7,7,7,7,7,  8,8,8,8,8,8,8,8,
	static int CalcBridgeLenCostFactor(int x)
	{
		int n;
		int r;

		if (x < 2) return x;
		x -= 2;
		for (n = 0, r = 2;; n++) {
			if (x <= n) return r + x * n;
			r += n * n;
			x -= n;
		}
	}

	//enum {
	// foundation, whole tile is leveled up (tileh's 7, 11, 13, 14) -. 3 corners raised
	static final int BRIDGE_FULL_LEVELED_FOUNDATION = 1 << 7 | 1 << 11 | 1 << 13 | 1 << 14;
	// foundation, tile is partly leveled up (tileh's 1, 2, 4, 8) -. 1 corner raised
	static final int BRIDGE_PARTLY_LEVELED_FOUNDATION = 1 << 1 | 1 << 2 | 1 << 4 | 1 << 8;
	// no foundations (X,Y direction) (tileh's 0, 3, 6, 9, 12)
	static final int BRIDGE_NO_FOUNDATION = 1 << 0 | 1 << 3 | 1 << 6 | 1 << 9 | 1 << 12;
	//};

	static /*PalSpriteID*/ int [] GetBridgeSpriteTable(int index, int table)
	{
		final Bridge bridge = Bridge._bridge[index];
		assert(table < 7);
		if (bridge.sprite_table == null || bridge.sprite_table[table] == null) {
			return _bridge_sprite_table[index][table];
		} else {
			return bridge.sprite_table[table];
		}
	}

	/**
	 * Determines which piece of a bridge is contained in the current tile
	 * @param tile The tile to analyze
	 * @return the piece
	 */
	static  int GetBridgePiece(TileIndex tile)
	{
		return BitOps.GB(tile.getMap().m2, 0, 4);
	}

	/**
	 * Determines the type of bridge on a tile
	 * @param tile The tile to analyze
	 * @return The bridge type
	 */
	static  int GetBridgeType(TileIndex tile)
	{
		return BitOps.GB(tile.getMap().m2, 4, 4);
	}

	/**	check if bridge can be built on slope
	 *	direction 0 = X-axis, direction 1 = Y-axis
	 *	is_start_tile = false		<-- end tile
	 *	is_start_tile = true		<-- start tile
	 */
	static int CheckBridgeSlope(int direction, int tileh, boolean is_start_tile)
	{
		if (TileIndex.IsSteepTileh(tileh)) return Cmd.CMD_ERROR;

		if (is_start_tile) {
			/* check slope at start tile
				- no extra cost
				- direction X: tiles 0, 12
				- direction Y: tiles 0,  9
			 */
			if(0 != (((direction != 0) ? 0x201 : 0x1001) & (1 << tileh))) return 0;

			// disallow certain start tiles to avoid certain crooked bridges
			if (tileh == 2) return Cmd.CMD_ERROR;
		} else {
			/*	check slope at end tile
				- no extra cost
				- direction X: tiles 0, 3
				- direction Y: tiles 0, 6
			 */
			if(0 != ((direction != 0? 0x41 : 0x9) & (1 << tileh))) return 0;

			// disallow certain end tiles to avoid certain crooked bridges
			if (tileh == 8) return Cmd.CMD_ERROR;
		}

		/*	disallow common start/end tiles to avoid certain crooked bridges e.g.
		 *	start-tile:	X 2,1 Y 2,4 (2 was disabled before)
		 *	end-tile:		X 8,4 Y 8,1 (8 was disabled before)
		 */
		if ((tileh == 1 && is_start_tile != BitOps.i2b(direction) ) ||
				(tileh == 4 && is_start_tile == BitOps.i2b(direction) )) {
			return Cmd.CMD_ERROR;
		}

		// slope foundations
		if ((0 !=(BRIDGE_FULL_LEVELED_FOUNDATION & (1 << tileh))) || (0 !=(BRIDGE_PARTLY_LEVELED_FOUNDATION & (1 << tileh))) )
			return (int) Global._price.terraform;

		return Cmd.CMD_ERROR;
	}

	public static int GetBridgeLength(TileIndex begin, TileIndex end)
	{
		int x1 = begin.TileX();
		int y1 = begin.TileY();
		int x2 = end.TileX();
		int y2 = end.TileY();

		return Math.abs(x2 + y2 - x1 - y1) - 1;
	}

	public static boolean CheckBridge_Stuff(int bridge_type, int bridge_len)
	{
		final Bridge b = Bridge._bridge[bridge_type];
		int max; // max possible length of a bridge (with patch 100)

		if (bridge_type >= Bridge.MAX_BRIDGES) return false;
		if (b.avail_year > Global.get_cur_year()) return false;

		max = b.max_length;
		if (max >= 16 && Global._patches.longbridges) max = 100;

		return b.min_length <= bridge_len && bridge_len <= max;
	}

	/** Build a Bridge
	 * @param x,y end tile coord
	 * @param p1 packed start tile coords (~ dx)
	 * @param p2 various bitstuffed elements
	 * - p2 = (bit 0- 7) - bridge type (hi bh)
	 * - p2 = (bit 8-..) - rail type. bit15 ((x>>8)&0x80) means road bridge.
	 */
	static int CmdBuildBridge(int x, int y, int flags, int p1, int p2)
	{
		int bridge_type;
		int rail_or_road, railtype;
		int sx,sy;
		//TileInfo ti_start = new TileInfo(); /* OPT: only 2 of those are ever used */
		//TileInfo ti_end = new TileInfo(); /* OPT: only 2 of those are ever used */
		//TileInfo ti = new TileInfo(); /* OPT: only 2 of those are ever used */
		int bridge_len;
		int odd_middle_part;
		int direction;
		int i;
		int cost, terraformcost, ret;
		boolean allow_on_slopes;
		
		int m5 = 0; // [dz] = 0? Originally non inited

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		/* unpack parameters */
		bridge_type = BitOps.GB(p2, 0, 8);
		railtype    = BitOps.GB(p2, 8, 8);

		if (p1 > Global.MapSize()) return Cmd.CMD_ERROR;

		// type of bridge
		if (BitOps.HASBIT(railtype, 7)) { // bit 15 of original p2 param
			railtype = 0;
			rail_or_road = 2;
		} else {
			if (!Player.ValParamRailtype(railtype)) return Cmd.CMD_ERROR;
			rail_or_road = 0;
		}

		TileIndex pp1 = new TileIndex(p1); 
		sx = pp1.TileX() * 16;
		sy = pp1.TileY() * 16;

		direction = 0;

		/* check if valid, and make sure that (x,y) are smaller than (sx,sy) */
		if (x == sx) {
			if (y == sy) return Cmd.return_cmd_error(Str.STR_5008_CANNOT_START_AND_END_ON);
			direction = 1;
			if (y > sy) {
				{ int t = sy; sy = y; y = t; } // intswap(y,sy);
				{ int t = sx; sx = x; x = t; } // intswap(x,sx);
			}
		} else if (y == sy) {
			if (x > sx) {
				{ int t = sy; sy = y; y = t; } // intswap(y,sy);
				{ int t = sx; sx = x; x = t; } // intswap(x,sx);
			}
		} else {
			return Cmd.return_cmd_error(Str.STR_500A_START_AND_END_MUST_BE_IN);
		}

		/* set and test bridge length, availability */
		bridge_len = ((sx + sy - x - y) >> 4) - 1;
		if (!CheckBridge_Stuff(bridge_type, bridge_len)) return Cmd.return_cmd_error(Str.STR_5015_CAN_T_BUILD_BRIDGE_HERE);

		/* retrieve landscape height and ensure it's on land */
		TileInfo ti_end = Landscape.FindLandscapeHeight(sx, sy);
		TileInfo ti_start = Landscape.FindLandscapeHeight(x, y);
		if (
				((ti_end.type == TileTypes.MP_WATER.ordinal()) && ti_end.map5 == 0) ||
				((ti_start.type == TileTypes.MP_WATER.ordinal()) && ti_start.map5 == 0)
				)
			return Cmd.return_cmd_error(Str.STR_02A0_ENDS_OF_BRIDGE_MUST_BOTH);

		if(0 != (BRIDGE_FULL_LEVELED_FOUNDATION & (1 << ti_start.tileh)) ) {
			ti_start.z += 8;
			ti_start.tileh = 0;
		}

		if(0 != (BRIDGE_FULL_LEVELED_FOUNDATION & (1 << ti_end.tileh)) ) {
			ti_end.z += 8;
			ti_end.tileh = 0;
		}

		if (ti_start.z != ti_end.z)
			return Cmd.return_cmd_error(Str.STR_5009_LEVEL_LAND_OR_WATER_REQUIRED);


		// Towns are not allowed to use bridges on slopes.
		//allow_on_slopes = (!Global.gs._is_old_ai_player && Global.gs._current_player.id != Owner.OWNER_TOWN && Global._patches.build_on_slopes);
		allow_on_slopes = (!Global.gs._is_old_ai_player && PlayerID.getCurrent().isTown() && Global._patches.build_on_slopes);

		/* Try and clear the start landscape */

		if (Cmd.CmdFailed(ret = Cmd.DoCommandByTile(ti_start.tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR)))
			return Cmd.CMD_ERROR;
		cost = ret;

		// true - bridge-start-tile, false - bridge-end-tile
		terraformcost = CheckBridgeSlope(direction, ti_start.tileh, true);
		if (Cmd.CmdFailed(terraformcost) || ( (terraformcost!=0) && !allow_on_slopes))
			return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
		cost += terraformcost;

		/* Try and clear the end landscape */

		ret = Cmd.DoCommandByTile(ti_end.tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
		cost += ret;

		// false - end tile slope check
		terraformcost = CheckBridgeSlope(direction, ti_end.tileh, false);
		if (Cmd.CmdFailed(terraformcost) || ( (terraformcost != 0) && !allow_on_slopes))
			return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
		cost += terraformcost;


		/* do the drill? */
		if(0 != (flags & Cmd.DC_EXEC)) {
			/* build the start tile */
			Landscape.ModifyTile(ti_start.tile, TileTypes.MP_TUNNELBRIDGE,
					//TileTypes.MP_SETTYPE(TileTypes.MP_TUNNELBRIDGE) |
					TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO | TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5,
					(bridge_type << 4), /* map2 */
					railtype, /* map3_lo */
					0x80 | direction | rail_or_road /* map5 */
					);

			/* build the end tile */
			Landscape.ModifyTile(ti_end.tile, TileTypes.MP_TUNNELBRIDGE,
					//TileTypes.MP_SETTYPE(TileTypes.MP_TUNNELBRIDGE) |
					TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO | TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5,
					(bridge_type << 4), /* map2 */
					railtype, /* map3_lo */
					0x80 | 0x20 | direction | rail_or_road /* map5 */
					);
		}

		// position of middle part of the odd bridge (larger than MAX(i) otherwise)
		odd_middle_part = (0 !=(bridge_len % 2)) ? (bridge_len / 2) : bridge_len;

		for (i = 0; i != bridge_len; i++) {
			if (direction != 0) {
				y += 16;
			} else {
				x += 16;
			}

			TileInfo ti = Landscape.FindLandscapeHeight(x, y);

			Global._error_message = Str.STR_5009_LEVEL_LAND_OR_WATER_REQUIRED;
			if (ti.tileh != 0 && ti.z >= ti_start.z) return Cmd.CMD_ERROR;

			// Find ship below
			if (ti.type == TileTypes.MP_WATER.ordinal() && !ti.tile.EnsureNoVehicle()) {
				Global._error_message = Str.STR_980E_SHIP_IN_THE_WAY;
				return Cmd.CMD_ERROR;
			}

			boolean not_valid_below = false;
			do {
				if (ti.type == TileTypes.MP_WATER.ordinal()) {
					if (ti.map5 > 1) { not_valid_below = true; break; } //goto not_valid_below;
					m5 = 0xC8;
				} else if (ti.type == TileTypes.MP_RAILWAY.ordinal()) {
					if (direction == 0) {
						if (ti.map5 != 2) { not_valid_below = true; break; } //goto not_valid_below;
					} else {
						if (ti.map5 != 1) { not_valid_below = true; break; } //goto not_valid_below;
					}
					m5 = 0xE0;
				} else if (ti.type == TileTypes.MP_STREET.ordinal()) {
					if (direction == 0) {
						if (ti.map5 != 5) { not_valid_below = true; break; } //goto not_valid_below;
					} else {
						if (ti.map5 != 10) { not_valid_below = true; break; } //goto not_valid_below;
					}
					m5 = 0xE8;
				} else {
					not_valid_below = true; break;
				}

			} while(false);

			if(not_valid_below)
			{
				/* try and clear the middle landscape */
				ret = Cmd.DoCommandByTile(ti.tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
				if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
				cost += ret;
				m5 = 0xC0;
			}

			/* do middle part of bridge */
			if(0 != (flags & Cmd.DC_EXEC) ) {
				ti.tile.getMap().m5 = 0xFF & (m5 | direction | rail_or_road);
				ti.tile.SetTileType(TileTypes.MP_TUNNELBRIDGE);

				//bridges pieces sequence (middle parts)
				// bridge len 1: 0
				// bridge len 2: 0 1
				// bridge len 3: 0 4 1
				// bridge len 4: 0 2 3 1
				// bridge len 5: 0 2 5 3 1
				// bridge len 6: 0 2 3 2 3 1
				// bridge len 7: 0 2 3 4 2 3 1
				// #0 - always as first, #1 - always as last (if len>1)
				// #2,#3 are to pair in order
				// for odd bridges: #5 is going in the bridge middle if on even position, #4 on odd (counting from 0)

				if (i == 0) { // first tile
					m5 = 0;
				} else if (i == bridge_len - 1) { // last tile
					m5 = 1;
				} else if (i == odd_middle_part) { // we are on the middle of odd bridge: #5 on even pos, #4 on odd
					m5 = 5 - (i % 2);
				} else {
					// generate #2 and #3 in turns [i%2==0], after the middle of odd bridge
					// this sequence swaps [... XOR (i>odd_middle_part)],
					// for even bridges XOR does not apply as odd_middle_part==bridge_len
					m5 = 2 + (BitOps.b2i(i % 2 == 0) ^ BitOps.b2i(i > odd_middle_part));
				}

				ti.tile.getMap().m2 = (bridge_type << 4) | m5;
				ti.tile.getMap().m3 =  BitOps.RETSB(ti.tile.getMap().m3, 4, 4, railtype);

				ti.tile.MarkTileDirtyByTile();
			}
		}

		Rail.SetSignalsOnBothDir(ti_start.tile, (0 !=(direction & 1)) ? 1 : 0);

		/*	for human player that builds the bridge he gets a selection to choose from bridges (Cmd.DC_QUERY_COST)
			It's unnecessary to execute this command every time for every bridge. So it is done only
			and cost is computed in "bridge_gui.c". For AI, Towns this has to be of course calculated
		 */
		if (0==(flags & Cmd.DC_QUERY_COST)) {
			final Bridge b = Bridge._bridge[bridge_type];

			bridge_len += 2;	// begin and end tiles/ramps

			if (!PlayerID.getCurrent().isSpecial() && !Global.gs._is_old_ai_player)
				bridge_len = CalcBridgeLenCostFactor(bridge_len);

			cost += ((long)(bridge_len * Global._price.build_bridge * b.price)) >> 8;
		}

		return cost;
	}

	static boolean DoCheckTunnelInWay(TileIndex tile, int z, int dir)
	{
		TileIndexDiff delta = TileIndex.TileOffsByDir(dir);
		TileInfo ti = new TileInfo();

		do {
			tile = tile.isub( delta );
			assert tile.IsValidTile();

			Landscape.FindLandscapeHeightByTile(ti, tile);
		} while (z < ti.z);

		if (z == ti.z &&
				ti.type == TileTypes.MP_TUNNELBRIDGE.ordinal() &&
				BitOps.GB(ti.map5, 4, 4) == 0 &&
				BitOps.GB(ti.map5, 0, 2) == dir) {
			Global._error_message = Str.STR_5003_ANOTHER_TUNNEL_IN_THE_WAY;
			return false;
		}

		return true;
	}

	static boolean CheckTunnelInWay(TileIndex tile, int z)
	{
		return DoCheckTunnelInWay(tile,z,0) &&
				DoCheckTunnelInWay(tile,z,1) &&
				DoCheckTunnelInWay(tile,z,2) &&
				DoCheckTunnelInWay(tile,z,3);
	}


	static int DoBuildTunnel(int x, int y, int x2, int y2, int flags, int exc_tile)
	{
		TileIndex end_tile;
		int direction;
		int cost, ret;
		//TileInfo ti = new TileInfo();
		int z;

		if (x > Global.MapMaxX() * 16 - 1 || y > Global.MapMaxY() * 16 - 1)
			return Cmd.CMD_ERROR;

		/* check if valid, and make sure that (x,y) is smaller than (x2,y2) */
		direction = 0;
		if (x == x2) {
			if (y == y2)
				return Cmd.return_cmd_error(Str.STR_5008_CANNOT_START_AND_END_ON);
			direction++;
			if (y > y2) {
				{ int t = y; y = y2; y2 = t; } // intswap(y,y2);
				//{ int t = x; x = x2; x2 = t; } // intswap(x,x2);
				exc_tile|=2;
			}
		} else if (y == y2) {
			if (x > x2) {
				//{ int t = y; y = y2; y2 = t; } // intswap(y,y2);
				{ int t = x; x = x2; x2 = t; } // intswap(x,x2);
				exc_tile|=2;
			}
		} else
			return Cmd.return_cmd_error(Str.STR_500A_START_AND_END_MUST_BE_IN);

		cost = 0;

		TileInfo ti = Landscape.FindLandscapeHeight(x2, y2);
		end_tile = ti.tile;
		z = ti.z;

		if (exc_tile != 3) {
			if ((direction != 0 ? 9 : 12) != ti.tileh)
				return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);
			ret = Cmd.DoCommandByTile(ti.tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
			if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
			cost += ret;
		}
		cost += Global._price.build_tunnel;

		for (;;) {
			if (direction!=0) y2-=16; else x2-=16;

			if (x2 == x && y2 == y) break;

			ti = Landscape.FindLandscapeHeight(x2, y2);
			if (ti.z <= z) return Cmd.CMD_ERROR;

			if (!Global._cheats.crossing_tunnels.value && !TunnelBridgeCmd.CheckTunnelInWay(ti.tile, z))
				return Cmd.CMD_ERROR;

			cost += Global._price.build_tunnel;
			cost += (cost >> 3);

			if (cost >= 400000000) cost = 400000000;
		}

		ti = Landscape.FindLandscapeHeight(x2, y2);
		if (ti.z != z) return Cmd.CMD_ERROR;

		if (exc_tile != 1) {
			if ((direction!=0 ? 6 : 3) != ti.tileh)
				return Cmd.return_cmd_error(Str.STR_1000_LAND_SLOPED_IN_WRONG_DIRECTION);

			ret = Cmd.DoCommandByTile(ti.tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
			if (Cmd.CmdFailed(ret)) return Cmd.CMD_ERROR;
			cost += ret;
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			Landscape.ModifyTile(ti.tile, TileTypes.MP_TUNNELBRIDGE,
					//TileTypes.MP_SETTYPE(TileTypes.MP_TUNNELBRIDGE) |
					TileTypes.MP_MAP3LO | TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5,
					_build_tunnel_railtype, /* map3lo */
					((_build_tunnel_bh << 1) | 2) - direction /* map5 */
					);

			Landscape.ModifyTile(end_tile, TileTypes.MP_TUNNELBRIDGE,
					//TileTypes.MP_SETTYPE(TileTypes.MP_TUNNELBRIDGE) |
					TileTypes.MP_MAP3LO | TileTypes.MP_MAPOWNER_CURRENT | TileTypes.MP_MAP5,
					_build_tunnel_railtype, /* map3lo */
					(_build_tunnel_bh << 1) | ((direction!=0) ? 3:0)/* map5 */
					);

			Rail.UpdateSignalsOnSegment(end_tile, (direction!=0)?7:1);
		}

		return (int) (cost + Global._price.build_tunnel);
	}

	/** Build Tunnel.
	 * @param x,y start tile coord of tunnel
	 * @param p1 railtype, 0x200 for road tunnel
	 * @param p2 unused
	 */
	static int CmdBuildTunnel(int x, int y, int flags, int p1, int p2)
	{
		//TileInfo ti = new TileInfo();
		//TileInfo tiorg = new TileInfo();
		int direction;
		int z;
		//TileIndex 
		int excavated_tile = 0;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		if (p1 != 0x200 && !Player.ValParamRailtype(p1)) return Cmd.CMD_ERROR;

		_build_tunnel_railtype = BitOps.GB(p1, 0, 8);
		_build_tunnel_bh       = BitOps.GB(p1, 8, 8);

		Global._build_tunnel_endtile = null;
		//excavated_tile = null;

		TileInfo tiorg = Landscape.FindLandscapeHeight(x, y);

		if (!tiorg.tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		/*
		if (
				!(direction=0, tiorg.tileh == 12) &&
				!(direction++, tiorg.tileh ==  6) &&
				!(direction++, tiorg.tileh ==  3) &&
				!(direction++, tiorg.tileh ==  9)) {
			return Cmd.return_cmd_error(Str.STR_500B_SITE_UNSUITABLE_FOR_TUNNEL);
		} */
		direction=0;
		if(tiorg.tileh != 12) {
			direction++;
			if(tiorg.tileh != 6) {
				direction++;
				if(tiorg.tileh != 3) {
					direction++;
					if(tiorg.tileh != 9)
						return Cmd.return_cmd_error(Str.STR_500B_SITE_UNSUITABLE_FOR_TUNNEL);
				}
			}			
		}

		z = tiorg.z;
		TileInfo ti;
		do {
			x += _build_tunnel_coord_mod[direction];
			y += _build_tunnel_coord_mod[direction+1];
			ti = Landscape.FindLandscapeHeight(x, y);
		} while (z != ti.z);
		Global._build_tunnel_endtile = ti.tile;


		if (!ti.tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		if (ti.tileh != _build_tunnel_tileh[direction]) {
			if (Cmd.CmdFailed(Cmd.DoCommandByTile(ti.tile, ti.tileh & ~_build_tunnel_tileh[direction], 0, flags, Cmd.CMD_TERRAFORM_LAND)))
				return Cmd.return_cmd_error(Str.STR_5005_UNABLE_TO_EXCAVATE_LAND);
			excavated_tile = 1;
		}

		return DoBuildTunnel(x, y, tiorg.x, tiorg.y, flags, excavated_tile);
	}

	static TileIndex CheckTunnelBusy(TileIndex tile, int []length)
	{
		int z = tile.GetTileZ();
		int m5 = tile.getMap().m5;
		TileIndexDiff delta = TileIndex.TileOffsByDir(m5 & 3);
		int len = 0;
		TileIndex starttile = tile;
		Vehicle v;

		do {
			tile = tile.iadd(delta);
			len++;
		} while (
				!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) ||
				BitOps.GB(tile.getMap().m5, 4, 4) != 0 ||
				(tile.getMap().m5 ^ 2) != m5 ||
				tile.GetTileZ() != z
				);

		v = Vehicle.FindVehicleBetween(starttile, tile, z);
		if (v != null) {
			Global._error_message = v.type == Vehicle.VEH_Train ?
					Str.STR_5000_TRAIN_IN_TUNNEL : Str.STR_5001_ROAD_VEHICLE_IN_TUNNEL;
			return TileIndex.INVALID_TILE;
		}

		if (length != null) length[0] = len;
		return tile;
	}

	static int DoClearTunnel(TileIndex tile, int flags)
	{
		Town t;
		TileIndex endtile;
		int [] length = {0};

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		// in scenario editor you can always destroy tunnels
		if (Global._game_mode != GameModes.GM_EDITOR && !Player.CheckTileOwnership(tile)) {
			if (!(Global._patches.extra_dynamite || Global._cheats.magic_bulldozer.value) || !tile.IsTileOwner(Owner.OWNER_TOWN))
				return Cmd.CMD_ERROR;
		}

		endtile = CheckTunnelBusy(tile, length);
		if (!endtile.isValid()) return Cmd.CMD_ERROR;

		Global._build_tunnel_endtile = endtile;

		t = Town.ClosestTownFromTile(tile, -1); //needed for town rating penalty
		// check if you're allowed to remove the tunnel owned by a town
		// removal allowal depends on difficulty settings
		if (tile.IsTileOwner(Owner.OWNER_TOWN) && Global._game_mode != GameModes.GM_EDITOR) {
			if (!Town.CheckforTownRating(tile, flags, t, TownTables.TUNNELBRIDGE_REMOVE)) {
				Global.SetDParam(0, t.index);
				return Cmd.return_cmd_error(Str.STR_2009_LOCAL_AUTHORITY_REFUSES);
			}
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			// We first need to request the direction before calling DoClearSquare
			//  else the direction is always 0.. dah!! ;)
			int tile_dir = BitOps.GB(tile.getMap().m5, 0, 2);
			int endtile_dir = BitOps.GB(endtile.getMap().m5, 0, 2);
			Landscape.DoClearSquare(tile);
			Landscape.DoClearSquare(endtile);
			Rail.UpdateSignalsOnSegment(tile, _updsignals_tunnel_dir[tile_dir]);
			Rail.UpdateSignalsOnSegment(endtile, _updsignals_tunnel_dir[endtile_dir]);
			if (tile.IsTileOwner(Owner.OWNER_TOWN) && Global._game_mode != GameModes.GM_EDITOR)
				t.ChangeTownRating(TownTables.RATING_TUNNEL_BRIDGE_DOWN_STEP, TownTables.RATING_TUNNEL_BRIDGE_MINIMUM);
		}
		return (int) (Global._price.clear_tunnel * (length[0] + 1));
	}

	static TileIndex FindEdgesOfBridge(TileIndex tile, TileIndex [] endtile)
	{
		int direction = BitOps.GB(tile.getMap().m5, 0, 1);
		TileIndex start;

		// find start of bridge
		for(;;) {
			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && (tile.getMap().m5 & 0xE0) == 0x80)
				break;
			tile = tile.iadd( direction != 0 ? TileIndex.TileDiffXY(0, -1) : TileIndex.TileDiffXY(-1, 0) );
		}

		start = tile;

		// find end of bridge
		for(;;) {
			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && (tile.getMap().m5 & 0xE0) == 0xA0)
				break;
			tile = tile.iadd( direction != 0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0) );
		}

		endtile[0] = tile;

		return start;
	}

	static int DoClearBridge(TileIndex tile, int flags)
	{
		TileIndex [] endtile = { null };
		Vehicle v;
		Town t;
		int direction;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		direction = BitOps.GB(tile.getMap().m5, 0, 1);

		/* delete stuff under the middle part if there's a transport route there..? */
		if ((tile.getMap().m5 & 0xE0) == 0xE0) {
			int cost;

			// check if we own the tile below the bridge..
			if (!PlayerID.getCurrent().isWater() && (!Player.CheckTileOwnership(tile) || !Vehicle.EnsureNoVehicleZ(tile, tile.TilePixelHeight())))
				return Cmd.CMD_ERROR;

			cost = (int) (0 != (tile.getMap().m5 & 8) ? Global._price.remove_road * 2 : Global._price.remove_rail);

			if(0 != (flags & Cmd.DC_EXEC)) {
				tile.getMap().m5 = (tile.getMap().m5 & ~0x38);
				tile.SetTileOwner(Owner.OWNER_NONE);
				tile.MarkTileDirtyByTile();
			}
			return cost;

			/* delete canal under bridge */
		} else if ((tile.getMap().m5 & 0xC8) == 0xC8 && tile.TilePixelHeight() != 0) {
			int cost;

			// check for vehicles under bridge
			if (!Vehicle.EnsureNoVehicleZ(tile, tile.TilePixelHeight())) return Cmd.CMD_ERROR;
			cost = (int) Global._price.clear_water;
			if(0 != (flags & Cmd.DC_EXEC)) {
				tile.getMap().m5 = (tile.getMap().m5 & ~0x38);
				tile.SetTileOwner(Owner.OWNER_NONE);
				tile.MarkTileDirtyByTile();
			}
			return cost;
		}

		tile = FindEdgesOfBridge(tile, endtile);

		// floods, scenario editor can always destroy bridges
		if (!PlayerID.getCurrent().isWater() && Global._game_mode != GameModes.GM_EDITOR && !Player.CheckTileOwnership(tile)) {
			if (!(Global._patches.extra_dynamite || Global._cheats.magic_bulldozer.value) || !tile.IsTileOwner(Owner.OWNER_TOWN))
				return Cmd.CMD_ERROR;
		}

		if (!tile.EnsureNoVehicle() || !Vehicle.EnsureNoVehicle(endtile[0])) return Cmd.CMD_ERROR;

		/*	Make sure there's no vehicle on the bridge
			Omit tile and endtile, since these are already checked, thus solving the problem
			of bridges over water, or higher bridges, where z is not increased, eg level bridge
		 */
		tile	   = tile.iadd( direction!=0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0) );
		endtile[0] = endtile[0].isub( direction!=0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0) );
		/* Bridges on slopes might have their Z-value offset..correct this */
		v = Vehicle.FindVehicleBetween(tile, endtile[0], tile.TilePixelHeight() + 8 + Vehicle.GetCorrectTileHeight(tile));
		if (v != null) {
			v.VehicleInTheWayErrMsg();
			return Cmd.CMD_ERROR;
		}

		/* Put the tiles back to start/end position */
		tile	= tile.isub( (0 != direction) ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0) );
		endtile[0]	= endtile[0].iadd( (0 != direction) ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0) );


		t = Town.ClosestTownFromTile(tile, -1); //needed for town rating penalty
		// check if you're allowed to remove the bridge owned by a town.
		// removal allowal depends on difficulty settings
		if (tile.IsTileOwner(Owner.OWNER_TOWN) && Global._game_mode != GameModes.GM_EDITOR) {
			if (!Town.CheckforTownRating(tile, flags, t, TownTables.TUNNELBRIDGE_REMOVE))
				return Cmd.CMD_ERROR;
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			int m5;
			MutableTileIndex c = new MutableTileIndex( tile );
			int new_data;
			int pbs;

			//checks if the owner is town then decrease town rating by RATING_TUNNEL_BRIDGE_DOWN_STEP until
			// you have a "Poor" (0) town rating
			if (tile.IsTileOwner(Owner.OWNER_TOWN) && Global._game_mode != GameModes.GM_EDITOR)
				t.ChangeTownRating(TownTables.RATING_TUNNEL_BRIDGE_DOWN_STEP, TownTables.RATING_TUNNEL_BRIDGE_MINIMUM);

			do {
				m5 = c.getMap().m5;
				pbs = Pbs.PBSTileReserved(c);

				if(0 != (m5 & 0x40)) {
					if(0 !=  (m5 & 0x20)) {
						new_data = _new_data_table[((m5 & 0x18) >> 2) | (m5 & 1)];
					}	else {
						if (BitOps.GB(m5, 3, 2) == 0)
						{
							//goto clear_it;
							Landscape.DoClearSquare(c);
							c.madd( direction != 0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0) );
							continue;
						}
						new_data = (c.GetTileSlope(null) == 0) ? 0x6000 : 0x6001;
					}

					c.SetTileType(TileTypes.values[ new_data >> 12]);
					c.getMap().m5 = 0xFF & new_data;
					c.getMap().m2 = 0;
					c.getMap().m4 &= 0x0F;
					if (direction!=0 ? BitOps.HASBIT(pbs,0) : BitOps.HASBIT(pbs,1))
						Pbs.PBSReserveTrack(c, direction!=0 ? 0 : 1);

					c.MarkTileDirtyByTile();

				} else {
					//clear_it:;
					Landscape.DoClearSquare(c);
				}
				c.madd( direction!=0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0) );
			} while (c.getTile() <= endtile[0].getTile());

			Rail.SetSignalsOnBothDir(tile, direction);
			Rail.SetSignalsOnBothDir(endtile[0], direction);

		}

		return (int) (((((endtile[0].getTile() - tile.getTile()) >> (direction!=0?8:0))&0xFF)+1) * Global._price.clear_bridge);
	}

	static int ClearTile_TunnelBridge(TileIndex tile, byte flags)
	{
		int m5 = tile.getMap().m5;

		if ((m5 & 0xF0) == 0) {
			if(0 != (flags & Cmd.DC_AUTO)) 
				return Cmd.return_cmd_error(Str.STR_5006_MUST_DEMOLISH_TUNNEL_FIRST);

			return DoClearTunnel(tile, flags);
		} else if(0 != (m5 & 0x80)) {
			if(0 != (flags & Cmd.DC_AUTO)) return Cmd.return_cmd_error(Str.STR_5007_MUST_DEMOLISH_BRIDGE_FIRST);

			return DoClearBridge(tile, flags);
		}

		return Cmd.CMD_ERROR;
	}

	public static int DoConvertTunnelBridgeRail(TileIndex tile, int totype, boolean exec)
	{
		TileIndex [] end2 = { null };// = new TileIndex();
		TileIndex end1;// = new TileIndex();
		int [] length = {0};
		Vehicle v;

		if ((tile.getMap().m5 & 0xFC) == 0x00) {
			// railway tunnel
			if (!Player.CheckTileOwnership(tile)) return Cmd.CMD_ERROR;

			if (BitOps.GB(tile.getMap().m3, 0, 4) == totype) return Cmd.CMD_ERROR;

			end1 = CheckTunnelBusy(tile, length);
			if (end1 == TileIndex.INVALID_TILE) return Cmd.CMD_ERROR;

			if (exec) {
				tile.getMap().m3 =  BitOps.RETSB(tile.getMap().m3, 0, 4, totype);
				end1.getMap().m3 =  BitOps.RETSB(end1.getMap().m3, 0, 4, totype);
				tile.MarkTileDirtyByTile();
				end1.MarkTileDirtyByTile();
			}
			return (length[0] + 1) * (int)(Global._price.build_rail/2);
		} else if ((tile.getMap().m5 & 0xF8) == 0xE0) {
			// bridge middle part with rail below
			// only check for train under bridge
			if (!Player.CheckTileOwnership(tile) || !Vehicle.EnsureNoVehicleZ(tile, tile.TilePixelHeight()))
				return Cmd.CMD_ERROR;

			// tile is already of requested type?
			if (BitOps.GB(tile.getMap().m3, 0, 4) == totype) return Cmd.CMD_ERROR;
			// change type.
			if (exec) {
				tile.getMap().m3 =  BitOps.RETSB(tile.getMap().m3, 0, 4, totype);
				tile.MarkTileDirtyByTile();
			}
			return (int) (Global._price.build_rail/2);
		} else if ((tile.getMap().m5 & 0xC6) == 0x80) {
			TileIndex starttile;
			int cost;
			int z = tile.TilePixelHeight();

			z += 8;

			if (!Player.CheckTileOwnership(tile)) return Cmd.CMD_ERROR;

			// railway bridge
			starttile = tile = FindEdgesOfBridge(tile, end2);
			// Make sure there's no vehicle on the bridge
			v = Vehicle.FindVehicleBetween(tile, end2[0], z);
			if (v != null) {
				v.VehicleInTheWayErrMsg();
				return Cmd.CMD_ERROR;
			}

			if (!starttile.EnsureNoVehicle() || !end2[0].EnsureNoVehicle()) {
				Global._error_message = Str.STR_8803_TRAIN_IN_THE_WAY;
				return Cmd.CMD_ERROR;
			}

			if (BitOps.GB(tile.getMap().m3, 0, 4) == totype) return Cmd.CMD_ERROR;
			cost = 0;
			do {
				if (exec) {
					if (tile.equals(starttile) || tile.equals(end2[0])) {
						tile.getMap().m3 =  BitOps.RETSB(tile.getMap().m3, 0, 4, totype);
					} else {
						tile.getMap().m3 =  BitOps.RETSB(tile.getMap().m3, 4, 4, totype);
					}
					tile.MarkTileDirtyByTile();
				}
				cost += Global._price.build_rail/2;
	tile = tile.iadd( BitOps.GB(tile.getMap().m5, 0, 1)!=0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0) );
			} while (tile.getTile() <= end2[0].getTile());

			return cost;
		} else
			return Cmd.CMD_ERROR;
	}


	// fast routine for getting the height of a middle bridge tile. 'tile' MUST be a middle bridge tile.
	static int GetBridgeHeight(final TileInfo ti)
	{
		TileIndexDiff delta;
		MutableTileIndex tile = new MutableTileIndex( ti.tile );

		// find the end tile of the bridge.
		delta = 0 != BitOps.GB(tile.getMap().m5, 0, 1) ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0);
		do {
			assert((tile.getMap().m5 & 0xC0) == 0xC0);	// bridge and middle part
			tile.madd( delta );

			assert tile.IsValidTile();
		} while (0 != (tile.getMap().m5 & 0x40));	// while bridge middle parts

		/* Return the height there (the height of the NORTH CORNER)
		 * If the end of the bridge is on a tileh 7 (all raised, except north corner),
		 * the z coordinate is 1 height level too low. Compensate for that */
		return tile.TilePixelHeight() + (tile.GetTileSlope(null) == 7 ? 8 : 0);
	}


	//extern final byte _road_sloped_sprites[14];

	static void DrawBridgePillars(final TileInfo ti, int x, int y, int z)
	{
		//final PalSpriteID
		int [] b;
		//PalSpriteID 
		int image;
		int piece;

		b = _bridge_poles_table[GetBridgeType(ti.tile)];

		// Draw first piece
		// (necessary for cantilever bridges)

		image = b[12 + BitOps.GB(ti.map5, 0, 1)];
		piece = GetBridgePiece(ti.tile);

		if (image != 0 && piece != 0) {
			if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) image = Sprite.RET_MAKE_TRANSPARENT(image);
			ViewPort.DrawGroundSpriteAt(image, x, y, z);
		}

		image = b[BitOps.GB(ti.map5, 0, 1) * 6 + piece];

		if (image != 0) {
			int back_height, front_height, i=z;
			final byte [] p;


			if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) image = Sprite.RET_MAKE_TRANSPARENT(image);

			p = _tileh_bits[(image & 1) * 2 + (ti.map5&0x01)];
			front_height = ti.z + ((ti.tileh & p[0])!=0?8:0);
			back_height = ti.z + ((ti.tileh & p[1])!=0?8:0);

			if (TileIndex.IsSteepTileh(ti.tileh)) {
				if (0==(ti.tileh & p[2])) front_height += 8;
				if (0==(ti.tileh & p[3])) back_height += 8;
			}

			for(; z>=front_height || z>=back_height; z=z-8) {
				if (z>=front_height) ViewPort.AddSortableSpriteToDraw(image, x,y, p[4], p[5], 0x28, z); // front facing pillar
				if (z>=back_height && z<i-8) ViewPort.AddSortableSpriteToDraw(image, x - p[6], y - p[7], p[4], p[5], 0x28, z); // back facing pillar
			}
		}
	}

	public static int GetBridgeFoundation(int tileh, int direction)
	{
		//int i;
		// normal level sloped building (7, 11, 13, 14)
		if(0 != (BRIDGE_FULL_LEVELED_FOUNDATION & (1 << tileh)) )
			return tileh;

		/*
		// inclined sloped building
		if (	((i=0, tileh == 1) || (i+=2, tileh == 2) || (i+=2, tileh == 4) || (i+=2, tileh == 8)) &&
				( direction == 0 || (i++, direction == 1)) )
			return i + 15;
		*/
		int i = 0;
		switch(direction)
		{
		case 1: i++;
		case 0: break;
		
		default: return 0;
		}
		
		switch(tileh)
		{
			case 8: i+=2; // fall
			case 4:  i+=2; // fall
			case 2:  i+=2; // fall
			case 1:  return i;			
		}
		
		return 0;
	}

	/**
	 * Draws a tunnel of bridge tile.
	 * For tunnels, this is rather simple, as you only needa draw the entrance.
	 * Bridges are a bit more complex. base_offset is where the sprite selection comes into play
	 * and it works a bit like a bitmask.<p> For bridge heads:
	 * <ul><li>Bit 0: direction</li>
	 * <li>Bit 1: northern or southern heads</li>
	 * <li>Bit 2: Set if the bridge head is sloped</li>
	 * <li>Bit 3 and more: Railtype Specific subset</li>
	 * </ul>
	 * For middle parts:
	 * <ul><li>Bits 0-1: need to be 0</li>
	 * <li>Bit 2: direction</li>
	 * <li>Bit 3 and above: Railtype Specific subset</li>
	 * </ul>
	 * Please note that in this code, "roads" are treated as railtype 1, whilst the real railtypes are 0, 2 and 3
	 */
	static void DrawTile_TunnelBridge(TileInfo ti)
	{
		int image;
		//final PalSpriteID *b;
		final int [] b;
		boolean ice = 0 != (ti.tile.getMap().m4 & 0x80);

		// draw tunnel?
		if ((ti.map5 & 0xF0) == 0) {
			if (BitOps.GB(ti.map5, 2, 2) == 0) { /* Rail tunnel? */
				image = Rail.GetRailTypeInfo(BitOps.GB(ti.tile.getMap().m3, 0, 4)).base_sprites.tunnel.id;
			} else {
				image = Sprite.SPR_TUNNEL_ENTRY_REAR_ROAD;
			}

			if (ice) image += 32;

			image += BitOps.GB(ti.map5, 0, 2) * 2;
			ViewPort.DrawGroundSprite(image);

			ViewPort.AddSortableSpriteToDraw(image+1, ti.x + 15, ti.y + 15, 1, 1, 8, ti.z);
			// draw bridge?
		} else if(0 != (ti.map5 & 0x80)) {
			//RailType
			int rt;
			int base_offset;

			if (BitOps.HASBIT(ti.map5, 1)) { /* This is a road bridge */
				base_offset = 8;
			} else { /* Rail bridge */
				if (BitOps.HASBIT(ti.map5, 6)) { /* The bits we need depend on the fact whether it is a bridge head or not */
					rt = BitOps.GB(ti.tile.getMap().m3, 4, 3);
				} else {
					rt = BitOps.GB(ti.tile.getMap().m3, 0, 3);
				}

				base_offset = Rail.GetRailTypeInfo(rt).bridge_offset.id;
				assert(base_offset != 8); /* This one is used for roads */
			}

			/* as the lower 3 bits are used for other stuff, make sure they are clear */
			assert( (base_offset & 0x07) == 0x00);

			if (0==(ti.map5 & 0x40)) {	// bridge ramps
				if (0==(BRIDGE_NO_FOUNDATION & (1 << ti.tileh))) {	// no foundations for 0, 3, 6, 9, 12
					int f = GetBridgeFoundation(ti.tileh, ti.map5 & 0x1);	// pass direction
					if (f!=0) Landscape.DrawFoundation(ti, f);

					// default sloped sprites..
					if (ti.tileh != 0) image = Sprite.SPR_RAIL_TRACK_Y + Rail._track_sloped_sprites[ti.tileh - 1];
				}

				/* Cope for the direction of the bridge */
				if (BitOps.HASBIT(ti.map5, 0)) base_offset++;

				if(0 != (ti.map5 & 0x20)) base_offset += 2; // which side
				if(ti.tileh == 0) base_offset += 4; // sloped bridge head

				/* Table number 6 always refers to the bridge heads for any bridge type */
				image = GetBridgeSpriteTable(GetBridgeType(ti.tile), 6)[base_offset];

				if (!ice) {
					Clear.DrawClearLandTile(ti, 3);
				} else {
					ViewPort.DrawGroundSprite(Sprite.SPR_FLAT_SNOWY_TILE + Landscape._tileh_to_sprite[ti.tileh]);
				}

				// draw ramp
				if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS) ) image = Sprite.RET_MAKE_TRANSPARENT(image);
				ViewPort.AddSortableSpriteToDraw(image, ti.x, ti.y, 16, 16, 7, ti.z);
			} else {
				// bridge middle part.
				int z;
				int x,y;

				image = BitOps.GB(ti.map5, 3, 2); // type of stuff under bridge (only defined for 0,1)
				/** @todo So why do we even WASTE that one bit?! (map5, bit 4) */
				assert(image <= 1);

				if (0==(ti.map5 & 0x20)) {
					// draw land under bridge
					if (ice) image += 2;

					if (image != 1 || ti.tileh == 0)
						ViewPort.DrawGroundSprite(_bridge_land_below[image] + Landscape._tileh_to_sprite[ti.tileh]);
					else
						ViewPort.DrawGroundSprite(WaterCmd._water_shore_sprites[ti.tileh]);

					// draw canal water?
					if( (0 != (ti.map5 & 8)) && ti.z != 0) WaterCmd.DrawCanalWater(ti.tile);
				} else {
					// draw transport route under bridge

					// draw foundation?
					if(0 != (ti.tileh) ) {
						int f = _bridge_foundations[ti.map5&1][ti.tileh];
						if(0 != f) Landscape.DrawFoundation(ti, f);
					}

					if (0==(image&1)) {
						final RailtypeInfo rti = Rail.GetRailTypeInfo(BitOps.GB(ti.tile.getMap().m3, 0, 4));
						// railway
						image = Sprite.SPR_RAIL_TRACK_Y + (ti.map5 & 1);
						if (ti.tileh != 0) image = Sprite.SPR_RAIL_TRACK_Y + RailTables._track_sloped_sprites[ti.tileh - 1];
						image += rti.total_offset.id;
						if (ice) image += rti.snow_offset.id;
					} else {
						// road
						image = Sprite.SPR_ROAD_Y + (ti.map5 & 1);
						if (ti.tileh != 0) image = Road._road_sloped_sprites[ti.tileh - 1] + 0x53F;
						if (ice) image += 19;
					}
					ViewPort.DrawGroundSprite(image);
				}

				/* Cope for the direction of the bridge */
				if (BitOps.HASBIT(ti.map5, 0)) base_offset += 4;

				/*  base_offset needs to be 0 due to the structure of the sprite table see table/bridge_land.h */
				assert( (base_offset & 0x03) == 0x00);
				// get bridge sprites
				//b = GetBridgeSpriteTable(GetBridgeType(ti.tile), GetBridgePiece(ti.tile)) + base_offset;
				b = GetBridgeSpriteTable(GetBridgeType(ti.tile), GetBridgePiece(ti.tile)); // + base_offset;
				int boff = base_offset;

				z = GetBridgeHeight(ti) + 5;

				// draw rail or road component
				image = b[0+boff];
				if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) image = Sprite.RET_MAKE_TRANSPARENT(image);
				ViewPort.AddSortableSpriteToDraw(image, ti.x, ti.y, (0 !=(ti.map5&1))?11:16, (0 !=(ti.map5&1))?16:11, 1, z);

				x = ti.x;
				y = ti.y;
				image = b[1+boff];
				if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) image = Sprite.RET_MAKE_TRANSPARENT(image);

				// draw roof, the component of the bridge which is logically between the vehicle and the camera
				if(0 != (ti.map5&1) ) {
					x += 12;
					if(0 != (image & Sprite.SPRITE_MASK)) ViewPort.AddSortableSpriteToDraw(image, x,y, 1, 16, 0x28, z);
				} else {
					y += 12;
					if(0 != (image & Sprite.SPRITE_MASK)) ViewPort.AddSortableSpriteToDraw(image, x,y, 16, 1, 0x28, z);
				}

				if (ti.z + 5 == z ) {
					// draw poles below for small bridges
					image = b[2+boff];
					if(0 != image) {
						if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) image = Sprite.RET_MAKE_TRANSPARENT(image);
						ViewPort.DrawGroundSpriteAt(image, x, y, z);
					}
				} else if (Global._patches.bridge_pillars) {
					// draw pillars below for high bridges
					DrawBridgePillars(ti, x, y, z);
				}
			}
		}

		if (Global._debug_pbs_level >= 1) {
			int pbs = Pbs.PBSTileReserved(ti.tile);
			if(0 != (pbs & Rail.TRACK_BIT_DIAG1)) ViewPort.DrawGroundSprite(0x3ED | Sprite.PALETTE_CRASH);
			if(0 != (pbs & Rail.TRACK_BIT_DIAG2)) ViewPort.DrawGroundSprite(0x3EE | Sprite.PALETTE_CRASH);
			if(0 != (pbs & Rail.TRACK_BIT_UPPER)) ViewPort.DrawGroundSprite(0x3EF | Sprite.PALETTE_CRASH);
			if(0 != (pbs & Rail.TRACK_BIT_LOWER)) ViewPort.DrawGroundSprite(0x3F0 | Sprite.PALETTE_CRASH);
			if(0 != (pbs & Rail.TRACK_BIT_LEFT))  ViewPort.DrawGroundSprite(0x3F2 | Sprite.PALETTE_CRASH);
			if(0 != (pbs & Rail.TRACK_BIT_RIGHT)) ViewPort.DrawGroundSprite(0x3F1 | Sprite.PALETTE_CRASH);
		}
	}

	static int GetSlopeZ_TunnelBridge(final TileInfo  ti)
	{
		int z = ti.z;
		int x = ti.x & 0xF;
		int y = ti.y & 0xF;
		int tileh = ti.tileh;

		// swap directions if Y tunnel/bridge to let the code handle the X case only.
		if(0 != (ti.map5 & 1)) 
		{
			int t = x; x = y; y = t; //intswap(x,y);
		}

		// to the side of the tunnel/bridge?
		if (BitOps.IS_INT_INSIDE(y, 5, 10+1)) {
			// tunnel?
			if ((ti.map5 & 0xF0) == 0) return z;

			// bridge?
			if(0 != (ti.map5 & 0x80)) {
				// bridge ending?
				if (0==(ti.map5 & 0x40)) {
					if(0 != (BRIDGE_FULL_LEVELED_FOUNDATION & (1 << tileh))) // 7, 11, 13, 14
						z += 8;

					// no ramp for bridge ending
					if( (  (0 !=(BRIDGE_PARTLY_LEVELED_FOUNDATION & (1 << tileh))) || (0 !=(BRIDGE_NO_FOUNDATION & (1 << tileh)))) && tileh != 0) 
					{
						return z + 8;
					} else if (0==(ti.map5 & 0x20)) { // northern / southern ending
						// ramp
						return (z + (x>>1) + 1);
					} else {
						// ramp in opposite dir
						return (z + ((x^0xF)>>1));
					}

					// bridge middle part
				} else {
					// build on slopes?
					if (tileh != 0) z += 8;

					// keep the same elevation because we're on the bridge?
					if (Global._get_z_hint >= z + 8) return Global._get_z_hint;

					// actually on the bridge, but not yet in the shared area.
					if (!BitOps.IS_INT_INSIDE(x, 5, 10 + 1)) return GetBridgeHeight(ti) + 8;

					// in the shared area, assume that we're below the bridge, cause otherwise the hint would've caught it.
					// if rail or road below then it means it's possibly build on slope below the bridge.
					if(0 != (ti.map5 & 0x20)) {
						int f = _bridge_foundations[ti.map5 & 1][tileh];
						// make sure that the slope is not inclined foundation
						if (BitOps.IS_INT_INSIDE(f, 1, 15)) return z;

						// change foundation type? XXX - should be final; accessor function!
						if (f != 0) tileh = Landscape._inclined_tileh[f - 15];
					}

					// no transport route, fallback to default
				}
			}
		} else {
			// if it's a bridge middle with transport route below, then we need to compensate for build on slopes
			if ((ti.map5 & (0x80 | 0x40 | 0x20)) == (0x80 | 0x40 | 0x20)) {
				int f;
				if (tileh != 0) z += 8;
				f = _bridge_foundations[ti.map5 & 1][tileh];
				if (BitOps.IS_INT_INSIDE(f, 1, 15)) return z;
				if (f != 0) tileh = Landscape._inclined_tileh[f - 15];
			}
		}

		// default case
		return Landscape.GetPartialZ(ti.x & 0xF, ti.y & 0xF, tileh) + ti.z;
	}

	static int GetSlopeTileh_TunnelBridge(final TileInfo  ti)
	{
		// not accurate, but good enough for slope graphics drawing
		return 0;
	}


	static AcceptedCargo GetAcceptedCargo_TunnelBridge(TileIndex tile)
	{
		return new AcceptedCargo();
		/* not used */
	}


	static TileDesc GetTileDesc_TunnelBridge(TileIndex tile)
	{
		TileDesc td = new TileDesc();

		if ((tile.getMap().m5 & 0x80) == 0) {
			td.str =
					(BitOps.GB(tile.getMap().m5, 2, 2) == 0) ? Str.STR_5017_RAILROAD_TUNNEL : Str.STR_5018_ROAD_TUNNEL;
		} else {
			td.str = _bridge_tile_str[BitOps.GB(tile.getMap().m5, 1, 2) << 4 | BitOps.GB(tile.getMap().m2, 4, 4)];

			/* scan to the end of the bridge, that's where the owner is stored */
			if(0 != (tile.getMap().m5 & 0x40)) {
				TileIndexDiff delta = BitOps.GB(tile.getMap().m5, 0, 1) != 0 ? TileIndex.TileDiffXY(0, -1) : TileIndex.TileDiffXY(-1, 0);

				do tile = tile.iadd( delta ); while (0 != (tile.getMap().m5 & 0x40));
			}
		}
		td.owner = tile.GetTileOwner().id;
		return td;
	}


	static void AnimateTile_TunnelBridge(TileIndex tile)
	{
		/* not used */
	}

	static void TileLoop_TunnelBridge(TileIndex tile)
	{
		if (GameOptions._opt.landscape == Landscape.LT_HILLY) {
			if (tile.GetTileZ() > GameOptions._opt.snow_line) {
				if (0==(tile.getMap().m4 & 0x80)) {
					tile.getMap().m4 |= 0x80;
					tile.MarkTileDirtyByTile();
				}
			} else {
				if(0 != (tile.getMap().m4 & 0x80)) {
					tile.getMap().m4 &= ~0x80;
					tile.MarkTileDirtyByTile();
				}
			}
		} else if (GameOptions._opt.landscape == Landscape.LT_DESERT) {
			if (tile.GetMapExtraBits() == TileInfo.EXTRABITS_DESERT && 0==(tile.getMap().m4&0x80)) {
				tile.getMap().m4 |= 0x80;
				tile.MarkTileDirtyByTile();
			}
		}

		// if it's a bridge with water below, call tileloop_water on it.
		if ((tile.getMap().m5 & 0xF8) == 0xC8) WaterCmd.TileLoop_Water(tile);
	}

	static void ClickTile_TunnelBridge(TileIndex tile)
	{
		/* not used */
	}


	static int GetTileTrackStatus_TunnelBridge(TileIndex tile, /*int*/ TransportType mode)
	{
		int result;
		int m5 = tile.getMap().m5;

		if ((m5 & 0xF0) == 0) {
			/* This is a tunnel */
			if (BitOps.GB(m5, 2, 2) == mode.getValue()) {
				/* Tranport in the tunnel is compatible */
				return (0 !=(m5&1)) ? 0x202 : 0x101;
			}
		} else if(0 != (m5 & 0x80)) {
			/* This is a bridge */
			result = 0;
			if (BitOps.GB(m5, 1, 2) == mode.getValue()) {
				/* Transport over the bridge is compatible */
				result = (0 !=(m5 & 1)) ? 0x202 : 0x101;
			}
			if(0 != (m5 & 0x40)) {
				/* Bridge middle part */
				if (0==(m5 & 0x20)) {
					/* Clear ground or water underneath */
					if ((m5 & 0x18) != 8) {
						/* Clear ground */
						return result;
					} else {
						if (mode != TransportType.Water) return result;
					}
				} else {
					/* Transport underneath */
					if (BitOps.GB(m5, 3, 2) != mode.getValue()) {
						/* Incompatible transport underneath */
						return result;
					}
				}
				/* If we've not returned yet, there is a compatible
				 * transport or water beneath, so we can add it to
				 * result */
				/* Why is this xor'd ? Can't it just be or'd? */
				result ^= (0 !=(m5 & 1)) ? 0x101 : 0x202;
			}
			return result;
		} else {
			assert false; /* This should never occur */
		}
		return 0;
	}

	static void ChangeTileOwner_TunnelBridge(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		if (!tile.IsTileOwner(old_player)) return;

		if (new_player.id != Owner.OWNER_SPECTATOR) {
			tile.SetTileOwner(new_player);
		}	else {
			if ((tile.getMap().m5 & 0xC0) == 0xC0) {
				// the stuff BELOW the middle part is owned by the deleted player.
				if (0==(tile.getMap().m5 & (1 << 4 | 1 << 3))) {
					// convert railway into grass.
					tile.getMap().m5 &= ~(1 << 5 | 1 << 4 | 1 << 3); // no transport route under bridge anymore..
				} else {
					// for road, change the owner of the road to local authority
					tile.SetTileOwner(Owner.OWNER_NONE);
				}
			} else {
				Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
			}
		}
	}



	static int VehicleEnter_TunnelBridge(Vehicle v, TileIndex tile, int x, int y)
	{
		int z;
		int dir, vdir;
		int fc;

		if (BitOps.GB(tile.getMap().m5, 4, 4) == 0) {
			z = Landscape.GetSlopeZ(x, y) - v.z_pos;
			if (Math.abs(z) > 2)
				return 8;

			if (v.type == Vehicle.VEH_Train) {
				fc = (x&0xF)+(y<<4);

				dir = BitOps.GB(tile.getMap().m5, 0, 2);
				vdir = v.direction >> 1;

				if (!v.rail.isInTunnel() && dir == vdir) {
					if (v.IsFrontEngine() && fc == _tunnel_fractcoord_1[dir]) {
						if (v.spritenum < 4)
							v.SndPlayVehicleFx(Snd.SND_05_TRAIN_THROUGH_TUNNEL);
							return 0;
					}
					if (fc == _tunnel_fractcoord_2[dir]) {
						if (v.next == null)
							Pbs.PBSClearTrack(v.tile, BitOps.FIND_FIRST_BIT(v.rail.track));
						v.tile = tile;
						v.rail.setInTunnel();
						v.setHidden(true);
						return 4;
					}
				}

				if (dir == (vdir^2) && fc == _tunnel_fractcoord_3[dir] && z == 0) {
					/* We're at the tunnel exit ?? */
					v.tile = tile;
					v.rail.track =  _exit_tunnel_track[dir];
					assert(v.rail.track != 0);
					v.setHidden(false);
					return 4;
				}
			} else if (v.type == Vehicle.VEH_Road) {
				fc = (x&0xF)+(y<<4);
				dir = BitOps.GB(tile.getMap().m5, 0, 2);
				vdir = v.direction >> 1;

				// Enter tunnel?
				if ((!v.road.isInTunnel()) && dir == vdir) {
					if (fc == _tunnel_fractcoord_4[dir] ||
							fc == _tunnel_fractcoord_5[dir]) {

						v.tile = tile;
						v.road.setInTunnel();
						v.setHidden(true);
						return 4;
					} else {
						return 0;
					}
				}

				if (dir == (vdir^2) && (
						/* We're at the tunnel exit ?? */
						fc == _tunnel_fractcoord_6[dir] ||
						fc == _tunnel_fractcoord_7[dir]) &&
						z == 0) {
					v.tile = tile;
					v.road.state = _road_exit_tunnel_state[dir];
					v.road.frame = _road_exit_tunnel_frame[dir];
					v.setHidden(false);
					return 4;
				}
			}
		} else if(0 != (tile.getMap().m5 & 0x80)) {
			if (v.type == Vehicle.VEH_Road || (v.type == Vehicle.VEH_Train && v.IsFrontEngine())) 
			{
				IntContainer h = new IntContainer();

				if (tile.GetTileSlope(h) != 0)
					h.v += 8; // Compensate for possible foundation
				if (0==(tile.getMap().m5 & 0x40) || // start/end tile of bridge
						Math.abs(h.v - v.z_pos) > 2) { // high above the ground . on the bridge
					/* modify speed of vehicle */
					int spd = Bridge._bridge[GetBridgeType(tile)].speed;
					if (v.type == Vehicle.VEH_Road) spd *= 2;
					if (spd < v.cur_speed) v.cur_speed = spd;
				}
			}
		}
		return 0;
	}

	static TileIndex GetVehicleOutOfTunnelTile(final Vehicle v)
	{
		TileIndexDiff delta = (v.direction & 2) != 0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0);
		int z = v.z_pos;

		MutableTileIndex tile = new MutableTileIndex(v.tile);

		for (;; tile.madd(delta) ) {
			if (tile.IsTileType( TileTypes.MP_TUNNELBRIDGE) && BitOps.GB(tile.getMap().m5, 4, 4) == 0 &&
					tile.GetTileZ() == z)
				break;

			assert tile.IsValidTile();
		}
		return tile;
	}

	final static TileTypeProcs _tile_type_tunnelbridge_procs = new TileTypeProcs(
			TunnelBridgeCmd::DrawTile_TunnelBridge,					/* draw_tile_proc */
			TunnelBridgeCmd::GetSlopeZ_TunnelBridge,					/* get_slope_z_proc */
			TunnelBridgeCmd::ClearTile_TunnelBridge,					/* clear_tile_proc */
			TunnelBridgeCmd::GetAcceptedCargo_TunnelBridge,	/* get_accepted_cargo_proc */
			TunnelBridgeCmd::GetTileDesc_TunnelBridge,				/* get_tile_desc_proc */
			TunnelBridgeCmd::GetTileTrackStatus_TunnelBridge,/* get_tile_track_status_proc */
			TunnelBridgeCmd::ClickTile_TunnelBridge,					/* click_tile_proc */
			TunnelBridgeCmd::AnimateTile_TunnelBridge,				/* animate_tile_proc */
			TunnelBridgeCmd::TileLoop_TunnelBridge,					/* tile_loop_clear */
			TunnelBridgeCmd::ChangeTileOwner_TunnelBridge,		/* change_tile_owner_clear */
			null,														/* get_produced_cargo_proc */
			TunnelBridgeCmd::VehicleEnter_TunnelBridge,			/* vehicle_enter_tile_proc */
			null,														/* vehicle_leave_tile_proc */
			TunnelBridgeCmd::GetSlopeTileh_TunnelBridge			/* get_slope_tileh_proc */
			);


}
