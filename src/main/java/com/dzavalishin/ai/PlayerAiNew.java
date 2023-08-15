package com.dzavalishin.ai;

import java.io.Serializable;

import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.aystar.AyStar;

public class PlayerAiNew implements AiConst, Serializable 
{

	AiState state = AiState.STARTUP;
	int tick;
	int idle;

	int temp; 	// A value used in more than one function, but it just temporary
				// The use is pretty simple: with this we can 'think' about stuff
				//   in more than one tick, and more than one AI. A static will not
				//   do, because they are not saved. This way, the AI is almost human ;)
	int counter; 	// For the same reason as temp, we have counter. It can count how
					//  long we are trying something, and just abort if it takes too long

	// Pathfinder stuff
	Ai_PathFinderInfo path_info = new Ai_PathFinderInfo();
	AyStar pathfinder;

	// Route stuff

	int cargo;
	int tbt; // train/bus/truck 0/1/2 AI_TRAIN/AI_BUS/AI_TRUCK TODO enum
	int new_cost;

	AiAction action;

	int last_id; // here is stored the last id of the searched city/industry
	int last_vehiclecheck_date; // Used in CheckVehicle
	final Ai_SpecialVehicle[] special_vehicles; // Some vehicles have some special flags

	TileIndex from_tile;
	TileIndex to_tile;

	int from_direction;
	int to_direction;

	boolean from_deliver; // True if this is the station that GIVES cargo
	boolean to_deliver;

	TileIndex depot_tile;
	int depot_direction;

	int amount_veh; // How many vehicles we are going to build in this route
	int cur_veh; // How many vehicles did we bought?
	VehicleID veh_id; // Used when bought a vehicle
	VehicleID veh_main_id; // The ID of the first vehicle, for shared copy

	int from_ic; // ic = industry/city. This is the ID of them
	int from_type; // AI_NO_TYPE/AI_CITY/AI_INDUSTRY
	int to_ic;
	int to_type;
	
	
	public PlayerAiNew() {
		special_vehicles = new Ai_SpecialVehicle[AI_MAX_SPECIAL_VEHICLES];
	}
}
