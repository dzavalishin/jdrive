package com.dzavalishin.ai;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.game.Bridge;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Engine;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.TunnelBridgeCmd;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.aystar.AyStar;
import com.dzavalishin.enums.RoadStopType;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.struct.PathNode;

public class AiTools implements AiConst 
{

	private static final int  TEST_STATION_NO_DIR = 0xFF;

	// Tests if a station can be build on the given spot
	// TODO: make it train compatible
	static boolean TestCanBuildStationHere(TileIndex tile, int dir)
	{
		Player p = Player.GetCurrentPlayer();

		if (dir == TEST_STATION_NO_DIR) {
			int ret;
			// TODO: currently we only allow spots that can be access from al 4 directions...
			//  should be fixed!!!
			for (dir = 0; dir < 4; dir++) {
				ret = AiNew_Build_Station(p, p.ainew.tbt, tile, 1, 1, dir, Cmd.DC_QUERY_COST);
				if (!Cmd.CmdFailed(ret)) return true;
			}
			return false;
		}

		// return true if command succeeded, so the inverse of CmdFailed()
		return !Cmd.CmdFailed(AiNew_Build_Station(p, p.ainew.tbt, tile, 1, 1, dir, Cmd.DC_QUERY_COST));
	}







	/* [dz] ignored, standard hash func used. Must be ok?
	// Calculates the hash
	//   Currently it is a 10 bit hash, so the hash array has a max depth of 6 bits (so 64)
	static int AiPathFinder_Hash(int key1, int key2)
	{
		return (TileX(key1) & 0x1F) + ((TileY(key1) & 0x1F) << 5);
	}*/





	// This creates the AiPathFinder
	static AyStar new_AyStar_AiPathFinder(int max_tiles_around, Ai_PathFinderInfo PathFinderInfo)
	{
		int x;
		int y;
		// Create AyStar
		AyStar result = new AIAyStar(); //malloc(sizeof(AyStar));
		//init_AyStar(result, AiPathFinder_Hash, 1 << 10);
		// Set the function pointers
		/*result.CalculateG = AyStar_AiPathFinder_CalculateG;
		result.CalculateH = AyStar_AiPathFinder_CalculateH;
		result.EndNodeCheck = AyStar_AiPathFinder_EndNodeCheck;
		result.FoundEndNode = AyStar_AiPathFinder_FoundEndNode;
		result.GetNeighbours = AyStar_AiPathFinder_GetNeighbours;

		result.BeforeExit = null;

		result.free = AyStar_AiPathFinder_Free; */

		// Set some information
		result.setLoops_per_tick( AI_PATHFINDER_LOOPS_PER_TICK );
		result.setMax_path_cost( 0 );
		result.setMax_search_nodes( AI_PATHFINDER_MAX_SEARCH_NODES );

		// Set the user_data to the PathFinderInfo
		result.user_target = PathFinderInfo;

		// Set the start node
		PathNode start_node = new PathNode();
		start_node.parent = null;
		start_node.node.direction = 0;
		start_node.node.user_data[0] = 0;

		// Now we add all the starting tiles
		for (x = PathFinderInfo.start_tile_tl.TileX(); x <= PathFinderInfo.start_tile_br.TileX(); x++) {
			for (y = PathFinderInfo.start_tile_tl.TileY(); y <= PathFinderInfo.start_tile_br.TileY(); y++) {
				start_node.node.tile = TileIndex.TileXY(x, y);
				result.addStartNode(start_node.node, 0);
			}
		}

		return result;
	}


	/*/ To reuse AyStar we sometimes have to clean all the memory
	@Deprecated
	static void clean_AyStar_AiPathFinder(AyStar aystar, Ai_PathFinderInfo PathFinderInfo)
	{
		PathNode start_node;
		int x;
		int y;

		aystar.clear();

		// Set the user_data to the PathFinderInfo
		aystar.user_target = PathFinderInfo;

		// Set the start node
		start_node.parent = null;
		start_node.node.direction = 0;
		start_node.node.user_data[0] = 0;
		start_node.node.tile = PathFinderInfo.start_tile_tl;

		// Now we add all the starting tiles
		for (x = TileX(PathFinderInfo.start_tile_tl); x <= TileX(PathFinderInfo.start_tile_br); x++) {
			for (y = TileY(PathFinderInfo.start_tile_tl); y <= TileY(PathFinderInfo.start_tile_br); y++) {
				if (!(IsTileType(TileXY(x, y), MP_CLEAR) || IsTileType(TileXY(x, y), MP_TREES))) continue;
				if (!TestCanBuildStationHere(TileXY(x, y), TEST_STATION_NO_DIR)) continue;
				start_node.node.tile = TileXY(x, y);
				aystar.addstart(aystar, &start_node.node, 0);
			}
		}
	} */






























	// Build HQ
	//  Params:
	//    tile : tile where HQ is going to be build
	static boolean AiNew_Build_CompanyHQ(Player p, TileIndex tile)
	{
		if (Cmd.CmdFailed(Ai.AI_DoCommand(tile, 0, 0, Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_COMPANY_HQ)))
			return false;
		Ai.AI_DoCommand(tile, 0, 0, Cmd.DC_EXEC | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_COMPANY_HQ);
		return true;
	}


	// Build station
	//  Params:
	//    type : AI_TRAIN/AI_BUS/AI_TRUCK : indicates the type of station
	//    tile : tile where station is going to be build
	//    length : in case of AI_TRAIN: length of station
	//    numtracks : in case of AI_TRAIN: tracks of station
	//    direction : the direction of the station
	//    flag : flag passed to DoCommand (normally 0 to get the cost or DC_EXEC to build it)
	static int AiNew_Build_Station(Player p, int type, TileIndex tile, int length, int numtracks, int direction, int flag)
	{
		if (type == AI_TRAIN)
			return Ai.AI_DoCommand(tile.getTile(), direction + (numtracks << 8) + (length << 16), 0, flag | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_RAILROAD_STATION);

		if (type == AI_BUS)
			return Ai.AI_DoCommand(tile.getTile(), direction, RoadStopType.RS_BUS.ordinal(), flag | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD_STOP);

		return Ai.AI_DoCommand(tile.getTile(), direction, RoadStopType.RS_TRUCK.ordinal(), flag | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD_STOP);
	}


	// Builds a brdige. The second best out of the ones available for this player
	//  Params:
	//   tile_a : starting point
	//   tile_b : end point
	//   flag : flag passed to DoCommand
	static int AiNew_Build_Bridge(Player p, TileIndex tile_a, TileIndex tile_b, int flag)
	{
		int bridge_type, bridge_len, type, type2;

		// Find a good bridgetype (the best money can buy)
		bridge_len = TunnelBridgeCmd.GetBridgeLength(tile_a, tile_b);
		type = type2 = 0;
		for (bridge_type = Bridge.MAX_BRIDGES-1; bridge_type >= 0; bridge_type--) {
			if (TunnelBridgeCmd.CheckBridge_Stuff(bridge_type, bridge_len)) {
				type2 = type;
				type = bridge_type;
				// We found two bridges, exit
				if (type2 != 0) break;
			}
		}
		// There is only one bridge that can be build..
		if (type2 == 0 && type != 0) type2 = type;

		// Now, simply, build the bridge!
		if (p.ainew.tbt == AI_TRAIN)
			return Ai.AI_DoCommand(tile_a.getTile(), tile_b.getTile(), (0<<8) + type2, flag | Cmd.DC_AUTO, Cmd.CMD_BUILD_BRIDGE);

		return Ai.AI_DoCommand(tile_a.getTile(), tile_b.getTile(), (0x80 << 8) + type2, flag | Cmd.DC_AUTO, Cmd.CMD_BUILD_BRIDGE);
	}


	// Build the route part by part
	// Basicly what this function do, is build that amount of parts of the route
	//  that go in the same direction. It sets 'part' to the last part of the route builded.
	//  The return value is the cost for the builded parts
	//
	//  Params:
	//   PathFinderInfo : Pointer to the PathFinderInfo used for AiPathFinder
	//   part : Which part we need to build
	//
	// TODO: skip already built road-pieces (e.g.: cityroad)
	static int AiNew_Build_RoutePart(Player p, Ai_PathFinderInfo PathFinderInfo, int flag)
	{
		int part = PathFinderInfo.position;
		int [] route_extra = PathFinderInfo.route_extra;
		TileIndex [] route = PathFinderInfo.route;
		int dir;
		int old_dir = -1;
		int cost = 0;
		int res;
		// We need to calculate the direction with the parent of the parent.. so we skip
		//  the first pieces and the last piece
		if (part < 1) part = 1;
		// When we are done, stop it
		if (part >= PathFinderInfo.route_length - 1) { PathFinderInfo.position = -2; return 0; }


		if (PathFinderInfo.rail_or_road) {
			// Tunnel code
			if ((AI_PATHFINDER_FLAG_TUNNEL & route_extra[part]) != 0) {
				cost += Ai.AI_DoCommand(route[part], 0, 0, flag, Cmd.CMD_BUILD_TUNNEL);
				PathFinderInfo.position++;
				// TODO: problems!
				if (Cmd.CmdFailed(cost)) {
					Global.DEBUG_ai(0, "[AiNew - BuildPath] We have a serious problem: tunnel could not be build!");
					return 0;
				}
				return cost;
			}
			// Bridge code
			if ((AI_PATHFINDER_FLAG_BRIDGE & route_extra[part]) != 0) {
				cost += AiNew_Build_Bridge(p, route[part], route[part-1], flag);
				PathFinderInfo.position++;
				// TODO: problems!
				if (Cmd.CmdFailed(cost)) {
					Global.DEBUG_ai(0, "[AiNew - BuildPath] We have a serious problem: bridge could not be build!");
					return 0;
				}
				return cost;
			}

			// Build normal rail
			// Keep it doing till we go an other way
			if (route_extra[part-1] == 0 && route_extra[part] == 0) {
				while (route_extra[part] == 0) {
					// Get the current direction
					dir = AiNew_GetRailDirection(route[part-1], route[part], route[part+1]);
					// Is it the same as the last one?
					if (old_dir != -1 && old_dir != dir) break;
					old_dir = dir;
					// Build the tile
					res = Ai.AI_DoCommand(route[part], 0, dir, flag, Cmd.CMD_BUILD_SINGLE_RAIL);
					if (Cmd.CmdFailed(res)) {
						// Problem.. let's just abort it all!
						p.ainew.state = AiState.NOTHING;
						return 0;
					}
					cost += res;
					// Go to the next tile
					part++;
					// Check if it is still in range..
					if (part >= PathFinderInfo.route_length - 1) break;
				}
				part--;
			}
			// We want to return the last position, so we go back one
			PathFinderInfo.position = part;
		} else {
			// Tunnel code
			if ((AI_PATHFINDER_FLAG_TUNNEL & route_extra[part]) != 0) {
				cost += Ai.AI_DoCommand(route[part], 0x200, 0, flag, Cmd.CMD_BUILD_TUNNEL);
				PathFinderInfo.position++;
				// TODO: problems!
				if (Cmd.CmdFailed(cost)) {
					Global.DEBUG_ai(0, "[AiNew - BuildPath] We have a serious problem: tunnel could not be build!");
					return 0;
				}
				return cost;
			}
			// Bridge code
			if ((AI_PATHFINDER_FLAG_BRIDGE & route_extra[part]) != 0) {
				cost += AiNew_Build_Bridge(p, route[part], route[part+1], flag);
				PathFinderInfo.position++;
				// TODO: problems!
				if (Cmd.CmdFailed(cost)) {
					Global.DEBUG_ai(0, "[AiNew - BuildPath] We have a serious problem: bridge could not be build!");
					return 0;
				}
				return cost;
			}

			// Build normal road
			// Keep it doing till we go an other way
			// EnsureNoVehicle makes sure we don't build on a tile where a vehicle is. This way
			//  it will wait till the vehicle is gone..
			if (route_extra[part-1] == 0 && route_extra[part] == 0 && (flag != Cmd.DC_EXEC || route[part].EnsureNoVehicle())) {
				while (route_extra[part] == 0 && (flag != Cmd.DC_EXEC || route[part].EnsureNoVehicle())) {
					// Get the current direction
					dir = AiNew_GetRoadDirection(route[part-1], route[part], route[part+1]);
					// Is it the same as the last one?
					if (old_dir != -1 && old_dir != dir) break;
					old_dir = dir;
					// There is already some road, and it is a bridge.. don't build!!!
					if (!route[part].typeIs(TileTypes.MP_TUNNELBRIDGE)) {
						// Build the tile
						res = Ai.AI_DoCommand(route[part], dir, 0, flag | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
						// Currently, we ignore CMD_ERRORs!
						if (Cmd.CmdFailed(res) && flag == Cmd.DC_EXEC && !route[part].typeIs(TileTypes.MP_STREET) && !route[part].EnsureNoVehicle()) {
							// Problem.. let's just abort it all!
							Global.DEBUG_ai(0, "Darn, the route could not be builded.. aborting!");
							p.ainew.state = AiState.NOTHING;
							return 0;
						}

						if (!Cmd.CmdFailed(res)) cost += res;
					}
					// Go to the next tile
					part++;
					// Check if it is still in range..
					if (part >= PathFinderInfo.route_length - 1) break;
				}
				part--;
				// We want to return the last position, so we go back one
			}
			if (!route[part].EnsureNoVehicle() && flag == Cmd.DC_EXEC) part--;
			PathFinderInfo.position = part;
		}

		return cost;
	}


	// This functions tries to find the best vehicle for this type of cargo
	// It returns vehicle_id or -1 if not found
	static int AiNew_PickVehicle(Player p)
	{
		if (p.ainew.tbt == AI_TRAIN) {
			// Not supported yet
			return -1;
		} else {
			int start, count, i, ret = Cmd.CMD_ERROR;
			start = Global._cargoc.ai_roadveh_start[p.ainew.cargo];
			count = Global._cargoc.ai_roadveh_count[p.ainew.cargo];

			// Let's check it backwards.. we simply want to best engine available..
			for (i = start + count - 1; i >= start; i--) {
				// Is it availiable?
				// Also, check if the reliability of the vehicle is above the AI_VEHICLE_MIN_RELIABILTY
				//if (!BitOps.HASBIT(Engine.GetEngine(i).player_avail, PlayerID.getCurrent().id) || Engine.GetEngine(i).getReliability() * 100 < AI_VEHICLE_MIN_RELIABILTY << 16) continue;
				if (!Engine.GetEngine(i).isAvailableTo(PlayerID.getCurrent()) || Engine.GetEngine(i).getReliability() * 100 < AI_VEHICLE_MIN_RELIABILTY << 16) continue;
				// Can we build it?
				ret = Ai.AI_DoCommand(0, i, 0, Cmd.DC_QUERY_COST, Cmd.CMD_BUILD_ROAD_VEH);
				if (!Cmd.CmdFailed(ret)) break;
			}
			// We did not find a vehicle :(
			if (Cmd.CmdFailed(ret)) return -1;
			return i;
		}
	}


	// Builds the best vehicle possible
	static int AiNew_Build_Vehicle(Player p, TileIndex tile, int flag)
	{
		int i = AiNew_PickVehicle(p);
		if (i == -1) return Cmd.CMD_ERROR;

		if (p.ainew.tbt == AI_TRAIN) return Cmd.CMD_ERROR;

		return Ai.AI_DoCommand(tile, i, 0, flag, Cmd.CMD_BUILD_ROAD_VEH);
	}

	private static final byte _roadbits_by_dir[] = {2,1,8,4};

	static int AiNew_Build_Depot(Player p, TileIndex tile, int direction, int flag)
	{
		int ret, ret2;
		if (p.ainew.tbt == AI_TRAIN)
			return Ai.AI_DoCommand(tile.getTile(), 0, direction, flag | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_TRAIN_DEPOT);

		ret = Ai.AI_DoCommand(tile.getTile(), direction, 0, flag | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD_DEPOT);
		if (Cmd.CmdFailed(ret)) return ret;
		// Try to build the road from the depot
		ret2 = Ai.AI_DoCommand(tile.OffsetByDir(direction), _roadbits_by_dir[direction], 0, flag | Cmd.DC_AUTO | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
		// If it fails, ignore it..
		if (Cmd.CmdFailed(ret2)) return ret;
		return ret + ret2;
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	static int AiNew_GetRailDirection(TileIndex tile_a, TileIndex tile_b, TileIndex tile_c)
	{
		// 0 = vert
		// 1 = horz
		// 2 = dig up-left
		// 3 = dig down-right
		// 4 = dig down-left
		// 5 = dig up-right

		int x1, x2, x3;
		int y1, y2, y3;

		x1 = tile_a.TileX();
		x2 = tile_b.TileX();
		x3 = tile_c.TileX();

		y1 = tile_a.TileY();
		y2 = tile_b.TileY();
		y3 = tile_c.TileY();

		if (y1 == y2 && y2 == y3) return 0;
		if (x1 == x2 && x2 == x3) return 1;
		if (y2 > y1) {
			if (x2 > x3) return 2;
			else return 4;
		}
		if (x2 > x1) {
			if (y2 > y3) return 2;
			else return 5;
		}
		if (y1 > y2) {
			if (x2 > x3) return 5;
			else return 3;
		}
		if (x1 > x2) {
			if (y2 > y3) return 4;
			else return 3;
		}

		return 0;
	}

	static int AiNew_GetRoadDirection(TileIndex tile_a, TileIndex tile_b, TileIndex tile_c)
	{
		int x1, x2, x3;
		int y1, y2, y3;
		int r;

		x1 = tile_a.TileX();
		x2 = tile_b.TileX();
		x3 = tile_c.TileX();

		y1 = tile_a.TileY();
		y2 = tile_b.TileY();
		y3 = tile_c.TileY();

		r = 0;

		if (x1 < x2) r += 8;
		if (y1 < y2) r += 1;
		if (x1 > x2) r += 2;
		if (y1 > y2) r += 4;

		if (x2 < x3) r += 2;
		if (y2 < y3) r += 4;
		if (x2 > x3) r += 8;
		if (y2 > y3) r += 1;

		return r;
	}

	// Get's the direction between 2 tiles seen from tile_a
	static int AiNew_GetDirection(TileIndex tile_a, TileIndex tile_b)
	{
		if (tile_a.TileY() < tile_b.TileY()) return 1;
		if (tile_a.TileY() > tile_b.TileY()) return 3;
		if (tile_a.TileX() < tile_b.TileX()) return 2;
		return 0;
	}

	// This functions looks up if this vehicle is special for this AI
	//  and returns his flag
	static int AiNew_GetSpecialVehicleFlag(Player p, Vehicle v) {
		int i;
		for (i=0;i<AI_MAX_SPECIAL_VEHICLES;i++) {
			final Ai_SpecialVehicle sv = p.ainew.special_vehicles[i];
			if (sv != null && sv.veh_id.id == v.index) {
				return sv.flag;
			}
		}

		// Not found :(
		return 0;
	}

	static boolean AiNew_SetSpecialVehicleFlag(Player p, Vehicle v, int flag) {
		int i; //, new_id = -1;
		for (i=0;i<AI_MAX_SPECIAL_VEHICLES;i++) 
		{
			if (p.ainew.special_vehicles[i] == null )
			{
				p.ainew.special_vehicles[i] = new Ai_SpecialVehicle();
				p.ainew.special_vehicles[i].veh_id = VehicleID.get( v.index );
				p.ainew.special_vehicles[i].flag = flag;
				return true;
			}
			
			if (p.ainew.special_vehicles[i].veh_id.id == v.index) {
				p.ainew.special_vehicles[i].flag |= flag;
				return true;
			}
			/*if (new_id == -1 && p.ainew.special_vehicles[i].veh_id == null &&
				p.ainew.special_vehicles[i].flag == 0)
				new_id = i;*/
		}

		// Out of special_vehicle spots :s
		//if (new_id == -1) {
			Global.DEBUG_ai( 1, "special_vehicles list is too small :(");
			return false;
		//}
		//p.ainew.special_vehicles[new_id].veh_id = VehicleID.get( v.index );
		//p.ainew.special_vehicles[new_id].flag = flag;
		//return true;
	}
	

}
