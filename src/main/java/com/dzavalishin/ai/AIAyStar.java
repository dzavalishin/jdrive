package com.dzavalishin.ai;

import com.dzavalishin.game.Bridge;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.Map;
import com.dzavalishin.game.Rail;
import com.dzavalishin.game.Road;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.TileInfo;
import com.dzavalishin.game.TunnelBridgeCmd;
import com.dzavalishin.aystar.AyStar;
import com.dzavalishin.aystar.AyStarNode;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.struct.OpenListNode;
import com.dzavalishin.struct.PathNode;

public class AIAyStar extends AyStar implements AiConst 
{


	//static int AyStar_AiPathFinder_CalculateG(AyStar aystar, AyStarNode current, OpenListNode parent)
	// The most important function: it calculates the g-value
	@Override
	protected int calculateG(AyStarNode current, OpenListNode parent) 
	{
		Ai_PathFinderInfo PathFinderInfo = (Ai_PathFinderInfo)user_target;
		int r, res = 0;
		TileInfo ti = new TileInfo();
		TileInfo parent_ti = new TileInfo();

		// Gather some information about the tile..
		Landscape.FindLandscapeHeightByTile(ti, current.tile);
		Landscape.FindLandscapeHeightByTile(parent_ti, parent.path.node.tile);

		// Check if we hit the end-tile
		if (TileIndex.TILES_BETWEEN(current.tile, PathFinderInfo.end_tile_tl, PathFinderInfo.end_tile_br)) {
			// We are at the end-tile, check if we had a direction or something...
			if (PathFinderInfo.end_direction != AI_PATHFINDER_NO_DIRECTION && AiTools.AiNew_GetDirection(current.tile, parent.path.node.tile) != PathFinderInfo.end_direction) {
				// We are not pointing the right way, invalid tile
				return AYSTAR_INVALID_NODE;
			}
			// If it was valid, drop out.. we don't build on the endtile
			return 0;
		}

		// Give everything a small penalty
		res += AI_PATHFINDER_PENALTY;

		if (!PathFinderInfo.rail_or_road) {
			// Road has the lovely advantage it can use other road... check if
			//  the current tile is road, and if so, give a good bonus
			if (current.tile.isRoad()) {
				res -= AI_PATHFINDER_ROAD_ALREADY_EXISTS_BONUS;
			}
		}

		// We should give a penalty when the tile is going up or down.. this is one way to do so!
		//  Too bad we have to count it from the parent.. but that is not so bad.
		// We also dislike long routes on slopes, since they do not look too realistic
		//  when there is a flat land all around, they are more expensive to build, and
		//  especially they essentially block the ability to connect or cross the road
		//  from one side.
		if (parent_ti.tileh != 0 && parent.path.parent != null) {
			// Skip if the tile was from a bridge or tunnel
			if (parent.path.node.user_data[0] == 0 && current.user_data[0] == 0) {
				if (PathFinderInfo.rail_or_road) {
					r = Rail.GetRailFoundation(parent_ti.tileh, 1 << AiTools.AiNew_GetRailDirection(parent.path.parent.node.tile, parent.path.node.tile, current.tile));
					// Maybe is BRIDGE_NO_FOUNDATION a bit strange here, but it contains just the right information..
					if (r >= 15 || (r == 0 && 0 != (BRIDGE_NO_FOUNDATION & (1 << ti.tileh)))) {
						res += AI_PATHFINDER_TILE_GOES_UP_PENALTY;
					} else {
						res += AI_PATHFINDER_FOUNDATION_PENALTY;
					}
				} else {
					if (!(parent.path.node.tile.isRoad() && parent.path.node.tile.typeIs(TileTypes.MP_TUNNELBRIDGE))) {
						r = Road.GetRoadFoundation(parent_ti.tileh, AiTools.AiNew_GetRoadDirection(parent.path.parent.node.tile, parent.path.node.tile, current.tile));
						if (r >= 15 || r == 0)
							res += AI_PATHFINDER_TILE_GOES_UP_PENALTY;
						else
							res += AI_PATHFINDER_FOUNDATION_PENALTY;
					}
				}
			}
		}

		// Are we part of a tunnel?
		if ((AI_PATHFINDER_FLAG_TUNNEL & current.user_data[0]) != 0) {
			// Tunnels are very expensive when build on long routes..
			// Ironicly, we are using BridgeCode here ;)
			r = AI_PATHFINDER_TUNNEL_PENALTY * TunnelBridgeCmd.GetBridgeLength(current.tile, parent.path.node.tile);
			res += r + (r >> 8);
		}

		// Are we part of a bridge?
		if ((AI_PATHFINDER_FLAG_BRIDGE & current.user_data[0]) != 0) {
			// That means for every length a penalty
			res += AI_PATHFINDER_BRIDGE_PENALTY * TunnelBridgeCmd.GetBridgeLength(current.tile, parent.path.node.tile);
			// Check if we are going up or down, first for the starting point
			// In user_data[0] is at the 8th bit the direction
			if (0==(BRIDGE_NO_FOUNDATION & (1 << parent_ti.tileh))) {
				if (TunnelBridgeCmd.GetBridgeFoundation(parent_ti.tileh, (current.user_data[0] >> 8) & 1) < 15)
					res += AI_PATHFINDER_BRIDGE_GOES_UP_PENALTY;
			}
			// Second for the end point
			if (0==(BRIDGE_NO_FOUNDATION & (1 << ti.tileh))) {
				if (TunnelBridgeCmd.GetBridgeFoundation(ti.tileh, (current.user_data[0] >> 8) & 1) < 15)
					res += AI_PATHFINDER_BRIDGE_GOES_UP_PENALTY;
			}
			if (parent_ti.tileh == 0) res += AI_PATHFINDER_BRIDGE_GOES_UP_PENALTY;
			if (ti.tileh == 0) res += AI_PATHFINDER_BRIDGE_GOES_UP_PENALTY;
		}

		//  To prevent the AI from taking the fastest way in tiles, but not the fastest way
		//    in speed, we have to give a good penalty to direction changing
		//  This way, we get almost the fastest way in tiles, and a very good speed on the track
		if (!PathFinderInfo.rail_or_road) {
			if (parent.path.parent != null &&
					AiTools.AiNew_GetDirection(current.tile, parent.path.node.tile) != AiTools.AiNew_GetDirection(parent.path.node.tile, parent.path.parent.node.tile)) {
				// When road exists, we don't like turning, but its free, so don't be to piggy about it
				if (parent.path.node.tile.isRoad())
					res += AI_PATHFINDER_DIRECTION_CHANGE_ON_EXISTING_ROAD_PENALTY;
				else
					res += AI_PATHFINDER_DIRECTION_CHANGE_PENALTY;
			}
		} else {
			// For rail we have 1 exeption: diagonal rail..
			// So we fetch 2 raildirection. That of the current one, and of the one before that
			if (parent.path.parent != null && parent.path.parent.parent != null) {
				int dir1 = AiTools.AiNew_GetRailDirection(parent.path.parent.node.tile, parent.path.node.tile, current.tile);
				int dir2 = AiTools.AiNew_GetRailDirection(parent.path.parent.parent.node.tile, parent.path.parent.node.tile, parent.path.node.tile);
				// First, see if we are on diagonal path, that is better than straight path
				if (dir1 > 1) { res -= AI_PATHFINDER_DIAGONAL_BONUS; }

				// First see if they are different
				if (dir1 != dir2) {
					// dir 2 and 3 are 1 diagonal track, and 4 and 5.
					if (!(((dir1 == 2 || dir1 == 3) && (dir2 == 2 || dir2 == 3)) || ((dir1 == 4 || dir1 == 5) && (dir2 == 4 || dir2 == 5)))) {
						// It is not, so we changed of direction
						res += AI_PATHFINDER_DIRECTION_CHANGE_PENALTY;
					}
					if (parent.path.parent.parent.parent != null) {
						int dir3 = AiTools.AiNew_GetRailDirection(parent.path.parent.parent.parent.node.tile, parent.path.parent.parent.node.tile, parent.path.parent.node.tile);
						// Check if we changed 3 tiles of direction in 3 tiles.. bad!!!
						if ((dir1 == 0 || dir1 == 1) && dir2 > 1 && (dir3 == 0 || dir3 == 1)) {
							res += AI_PATHFINDER_CURVE_PENALTY;
						}
					}
				}
			}
		}

		return (res < 0) ? 0 : res;
	}




	//static int AyStar_AiPathFinder_CalculateH(AyStar *aystar, AyStarNode *current, OpenListNode *parent)
	// The h-value, simple calculation
	@Override
	protected int calculateH(AyStarNode current, OpenListNode parent) 
	{
		Ai_PathFinderInfo PathFinderInfo = (Ai_PathFinderInfo)user_target;
		int r, r2;
		if (PathFinderInfo.end_direction != AI_PATHFINDER_NO_DIRECTION) {
			// The station is pointing to a direction, add a tile towards that direction, so the H-value is more accurate
			r = Map.DistanceManhattan(current.tile, PathFinderInfo.end_tile_tl.OffsetByDir(PathFinderInfo.end_direction));
			r2 = Map.DistanceManhattan(current.tile, PathFinderInfo.end_tile_br.OffsetByDir(PathFinderInfo.end_direction));
		} else {
			// No direction, so just get the fastest route to the station
			r = Map.DistanceManhattan(current.tile, PathFinderInfo.end_tile_tl);
			r2 = Map.DistanceManhattan(current.tile, PathFinderInfo.end_tile_br);
		}
		// See if the bottomright is faster than the topleft..
		if (r2 < r) r = r2;
		return r * AI_PATHFINDER_H_MULTIPLER;
	}

	//static void AyStar_AiPathFinder_GetNeighbours(AyStar *aystar, OpenListNode *current)
	// What tiles are around us.
	@Override
	protected void getNeighbours(OpenListNode current) 
	{
		int i;
		int ret;
		//int dir;

		Ai_PathFinderInfo PathFinderInfo = (Ai_PathFinderInfo)user_target;

		num_neighbours = 0;

		// Go through all surrounding tiles and check if they are within the limits
		for (i = 0; i < 4; i++) {
			TileIndex ctile = current.path.node.tile; // Current tile
			TileIndex atile = ctile.OffsetByDir(i); // Adjacent tile

			if (atile.TileX() > 1 && atile.TileX() < Global.MapMaxX() - 1 &&
					atile.TileY() > 1 && atile.TileY() < Global.MapMaxY() - 1) {
				// We also directly test if the current tile can connect to this tile..
				//  We do this simply by just building the tile!

				// If the next step is a bridge, we have to enter it the right way
				if (!PathFinderInfo.rail_or_road && atile.isRoad()) {
					if (atile.IsTileType(TileTypes.MP_TUNNELBRIDGE)) {
						// An existing bridge... let's test the direction ;)
						if ((atile.M().m5 & 1) != (i & 1)) continue;
						// This problem only is valid for tunnels:
						// When the last tile was not yet a tunnel, check if we enter from the right side..
						if ((atile.M().m5 & 0x80) == 0) {
							if (i != (atile.M().m5 & 3)) continue;
						}
					}
				}
				// But also if we are on a bridge, we can only move a certain direction
				if (!PathFinderInfo.rail_or_road && ctile.isRoad()) {
					if (ctile.IsTileType(TileTypes.MP_TUNNELBRIDGE)) {
						// An existing bridge/tunnel... let's test the direction ;)
						if ((ctile.M().m5 & 1) != (i & 1)) continue;
					}
				}

				if ((AI_PATHFINDER_FLAG_BRIDGE & current.path.node.user_data[0]) != 0 ||
						(AI_PATHFINDER_FLAG_TUNNEL & current.path.node.user_data[0]) != 0) {
					// We are a bridge/tunnel, how cool!!
					//  This means we can only point forward.. get the direction from the user_data
					if (i != (current.path.node.user_data[0] >> 8)) continue;
				}
				int dir = 0;

				// First, check if we have a parent
				if (current.path.parent == null && current.path.node.user_data[0] == 0) {
					// If not, this means we are at the starting station
					if (PathFinderInfo.start_direction != AI_PATHFINDER_NO_DIRECTION) {
						// We do need a direction?
						if (AiTools.AiNew_GetDirection(ctile, atile) != PathFinderInfo.start_direction) {
							// We are not pointing the right way, invalid tile
							continue;
						}
					}
				} else if (current.path.node.user_data[0] == 0) {
					if (PathFinderInfo.rail_or_road) {
						// Rail check
						dir = AiTools.AiNew_GetRailDirection(current.path.parent.node.tile, ctile, atile);
						ret = Ai.AI_DoCommand(ctile, 0, dir, Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_SINGLE_RAIL);
						if (Cmd.CmdFailed(ret)) continue;
						/*#ifdef AI_PATHFINDER_NO_90DEGREES_TURN
						if (current.path.parent.parent != null) {
							// Check if we don't make a 90degree curve
							int dir1 = AiNew_GetRailDirection(current.path.parent.parent.node.tile, current.path.parent.node.tile, ctile);
							if (_illegal_curves[dir1] == dir || _illegal_curves[dir] == dir1) {
								continue;
							}
						}
						#endif */
					} else {
						// Road check
						dir = AiTools.AiNew_GetRoadDirection(current.path.parent.node.tile, ctile, atile);
						if (ctile.isRoad()) {
							if (ctile.IsTileType(TileTypes.MP_TUNNELBRIDGE)) {
								// We have a bridge, how nicely! We should mark it...
								dir = 0;
							} else {
								// It already has road.. check if we miss any bits!
								if ((ctile.M().m5 & dir) != dir) {
									// We do miss some pieces :(
									dir &= ~ctile.M().m5;
								} else {
									dir = 0;
								}
							}
						}
						// Only destruct things if it is MP_CLEAR of MP_TREES
						if (dir != 0) {
							ret = Ai.AI_DoCommand(ctile, dir, 0, Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
							if (Cmd.CmdFailed(ret)) continue;
						}
					}
				}

				// The tile can be connected
				neighbours[num_neighbours] = new AyStarNode();
				neighbours[num_neighbours].tile = atile;
				neighbours[num_neighbours].user_data[0] = 0;
				neighbours[num_neighbours++].direction = 0;
			}
		}

		// Next step, check for bridges and tunnels
		if (current.path.parent != null && current.path.node.user_data[0] == 0) {
			TileInfo ti = new TileInfo();
			// First we get the dir from this tile and his parent
			int dir = AiTools.AiNew_GetDirection(current.path.parent.node.tile, current.path.node.tile);
			// It means we can only walk with the track, so the bridge has to be in the same direction
			TileIndex tile = current.path.node.tile;
			TileIndex new_tile = tile;

			Landscape.FindLandscapeHeightByTile(ti, tile);

			// Bridges can only be build on land that is not flat
			//  And if there is a road or rail blocking
			if (ti.tileh != 0 ||
					(PathFinderInfo.rail_or_road && tile.OffsetByDir(dir).IsTileType(TileTypes.MP_STREET)) ||
					(!PathFinderInfo.rail_or_road && tile.OffsetByDir(dir).IsTileType(TileTypes.MP_RAILWAY))) {
				for (;;) {
					new_tile = new_tile.OffsetByDir(dir);

					// Precheck, is the length allowed?
					if (!TunnelBridgeCmd.CheckBridge_Stuff(0, TunnelBridgeCmd.GetBridgeLength(tile, new_tile))) break;

					// Check if we hit the station-tile.. we don't like that!
					if (TileIndex.TILES_BETWEEN(new_tile, PathFinderInfo.end_tile_tl, PathFinderInfo.end_tile_br)) break;

					// Try building the bridge..
					ret = Ai.AI_DoCommand(tile, new_tile.getTile(), (0 << 8) + (Bridge.MAX_BRIDGES / 2), Cmd.DC_AUTO, Cmd.CMD_BUILD_BRIDGE);
					if (Cmd.CmdFailed(ret)) continue;
					// We can build a bridge here.. add him to the neighbours
					neighbours[num_neighbours] = new AyStarNode();
					neighbours[num_neighbours].tile = new_tile;
					neighbours[num_neighbours].user_data[0] = AI_PATHFINDER_FLAG_BRIDGE + (dir << 8);
					neighbours[num_neighbours++].direction = 0;
					// We can only have 12 neighbours, and we need 1 left for tunnels
					if (num_neighbours == 11) break;
				}
			}

			// Next, check for tunnels!
			// Tunnels can only be build with tileh of 3, 6, 9 or 12, depending on the direction
			//  For now, we check both sides for this tile.. terraforming gives fuzzy result
			if ((dir == 0 && ti.tileh == 12) ||
					(dir == 1 && ti.tileh == 6) ||
					(dir == 2 && ti.tileh == 3) ||
					(dir == 3 && ti.tileh == 9)) {
				// Now simply check if a tunnel can be build
				ret = Ai.AI_DoCommand(tile, (PathFinderInfo.rail_or_road?0:0x200), 0, Cmd.DC_AUTO, Cmd.CMD_BUILD_TUNNEL);
				if(null != Global._build_tunnel_endtile)
				{
					Landscape.FindLandscapeHeightByTile(ti, Global._build_tunnel_endtile);
					if (!Cmd.CmdFailed(ret) && (ti.tileh == 3 || ti.tileh == 6 || ti.tileh == 9 || ti.tileh == 12)) {
						neighbours[num_neighbours].tile = Global._build_tunnel_endtile;
						neighbours[num_neighbours].user_data[0] = AI_PATHFINDER_FLAG_TUNNEL + (dir << 8);
						neighbours[num_neighbours++].direction = 0;
					}
				}
			}
		}
	}





	//static int AyStar_AiPathFinder_EndNodeCheck(AyStar aystar, OpenListNode current)
	// Check if the current tile is in our end-area
	@Override
	public int endNodeCheck(OpenListNode current)
	{
		Ai_PathFinderInfo PathFinderInfo = (Ai_PathFinderInfo)user_target;
		// It is not allowed to have a station on the end of a bridge or tunnel ;)
		if (current.path.node.user_data[0] != 0) return AyStar.AYSTAR_DONE;
		if (TileIndex.TILES_BETWEEN(current.path.node.tile, PathFinderInfo.end_tile_tl, PathFinderInfo.end_tile_br))
			if (current.path.node.tile.typeIs(TileTypes.MP_CLEAR) || current.path.node.tile.typeIs(TileTypes.MP_TREES))
				if (current.path.parent == null || AiTools.TestCanBuildStationHere(current.path.node.tile, AiTools.AiNew_GetDirection(current.path.parent.node.tile, current.path.node.tile)))
					return AyStar.AYSTAR_FOUND_END_NODE;

		return AyStar.AYSTAR_DONE;
	}

	//static void AyStar_AiPathFinder_FoundEndNode(AyStar *aystar, OpenListNode *current)
	// We found the end.. let's get the route back and put it in an array
	@Override
	protected void foundEndNode(OpenListNode current) 
	{
		Ai_PathFinderInfo PathFinderInfo = (Ai_PathFinderInfo)user_target;
		int i = 0;
		PathNode parent = current.path;

		do {
			PathFinderInfo.route_extra[i] = parent.node.user_data[0];
			PathFinderInfo.route[i++] = parent.node.tile;
			if (i > PathFinderInfo.route.length) {
				// We ran out of space for the PathFinder
				Global.DEBUG_ai( 0,"[AiPathFinder] Ran out of space in the route[] array!!!");
				PathFinderInfo.route_length = -1; // -1 indicates out of space
				return;
			}
			parent = parent.parent;
		} while (parent != null);
		PathFinderInfo.route_length = i;
		Global.DEBUG_ai( 1, "[Ai-PathFinding] Found route of %d nodes long in %d nodes of searching", i, ClosedListHash.Hash_Size());
	}


}