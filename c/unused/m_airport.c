#if 0
#include "stdafx.h"
#include "openttd.h"
#include "math.h"
#include "variables.h"
#include "town.h"
#include "command.h"
#include "airport.h"
#include "station.h"
#include "news.h"
#include "network.h"
#include "gui.h"
#include "vehicle.h"
#include "order.h"
#include "functions.h"
#include "table/strings.h"

const float PI = 3.141; // duuuuuuuuuuhhhhhh
const uint INT_AIRPORT_YEAR = 1990;
const uint MA_MIN_POPULATION = 15000;
const uint MA_MAX_DISTANCE_FROM_TOWN = 15;
const uint MA_MIN_DISTANCE_FROM_TOWN = 10;
const uint MA_MAX_AIRPORTS_PER_TOWN = 2;
const uint MA_MAX_PLANES_QUOTA = 5;

//Checks if a vehicle serves an MS returns the amount that it serves 
uint MA_VehicleServesMS(Vehicle *v)
{
	Order *order = v->orders;
	uint stationcount = 0;
	Station *st;

	while(order != NULL)
	{
		st = GetStation(order->station);
		if(st->owner == OWNER_TOWN)
			stationcount++;

		order = order->next;
	}
	return stationcount;
}

// finds a municipal station in a vehicles order list the count
// arg is for which municipal station in the list ie. 1st, 2nd 
StationID MA_Find_MS_InVehicleOrders(Vehicle *v, uint count)
{
	Order *order = v->orders;
	Station *st;
	uint stationcount = 0;

	while(order != NULL)
	{
		st = GetStation(order->station);
		if(st->owner == OWNER_TOWN)
			stationcount++;
		if(stationcount == count) 
			return st->index;
		order = order->next;
	}
	return INVALID_STATION;
}

//checks if a plane is allowed to stop
bool MA_VehicleIsAtMunicipalAirport(Vehicle *v)
{
	if(!_patches.allow_municipal_airports)
		return false;
	if(GetTileOwner(v->tile) == OWNER_TOWN)
		return true;

	return false;
}	

//does exactly what it says on the tin
void MA_DestroyAirport(Town* tn)
{
	Station *st;

	FOR_ALL_STATIONS(st)
	{
		if(IsValidStation(st) 
			&& st->owner == OWNER_TOWN
			&& st->town == tn) { 
				DoCommandByTile(st->xy, 0, 0, DC_EXEC,CMD_LANDSCAPE_CLEAR);
				ExpandTown(tn); //this just builds over the gap thats left
		}
	}
	
	return;
}

//converts degrees to radians
float D2R(uint degrees)
{
	return degrees * (PI / 180.0);
}
/*
//calculates tax
void MA_Tax(uint income, Vehicle *v)
{
	uint old_expenses_type = _yearly_expenses_type;

	if(_patches.allow_municipal_airports) {
		float tax = 0;
		tax = (income / 100.0) * 20; //_patches.municipal_airports_tax;

		ShowCostOrIncomeAnimation(v->x_pos ,v->y_pos ,v->z_pos - 13, tax);

		switch(v->type) {

		case VEH_Aircraft:	
			SET_EXPENSES_TYPE(EXPENSES_AIRCRAFT_RUN);
			break;
		case VEH_Train:		
			SET_EXPENSES_TYPE(EXPENSES_TRAIN_RUN);
			break;
		case VEH_Ship:		
			SET_EXPENSES_TYPE(EXPENSES_SHIP_RUN);				
			break;
		case VEH_Road:		
			SET_EXPENSES_TYPE(EXPENSES_ROADVEH_RUN);				
			break;

		}
		
		SubtractMoneyFromPlayer(tax);
		_yearly_expenses_type = old_expenses_type;
	}
	return;
}
*/
//checks to see if we have used our quota at any municipal station;
bool MA_WithinVehicleQuota(Station *st)
{
	Vehicle *v;
	Order *order;
	uint vehiclecount = 0;

	if(!_patches.allow_municipal_airports) 
		return true;

	if(st->owner != OWNER_TOWN)
		return true;

	FOR_ALL_VEHICLES(v)
	{
		order = v->orders;
		while(IsValidVehicle(v) && order != NULL)
		{
			if(IsValidVehicle(v)
				&& v->owner == _current_player
				&& GetStation(order->station) == st
				&& st->owner == OWNER_TOWN) {
					vehiclecount++;
					break;
			}
			order = order->next;
		}// while v->orders
	}//for all vehicles
	if(vehiclecount >= MA_MAX_PLANES_QUOTA) return false;
	else return true;
}

//saves me changing too much code elsewhere
bool MA_OwnerHandler(PlayerID owner)
{

	if(!_patches.allow_municipal_airports) 
		return false;
	
	return (owner == OWNER_TOWN);
	
}

//returns a position of a tile on the circuference of a circle;
TileIndex CircularPos(uint radius, uint angle, TileIndex centre)
{
	return TileXY(TileX(centre) + (cos(D2R(angle)) * radius), TileY(centre) + (sin(D2R(angle)) * radius));
}

//checks if site is level and buildable
bool MA_CheckCandidate(TileIndex candidatetile, Town *tn)
{
	candidatetile += TileDiffXY(-4, -4);

	BEGIN_TILE_LOOP(tl, 9, 9, candidatetile) {

		if(!IsValidTile(tl))
			return false;

		if(GetTileType(tl) == MP_UNMOVABLE || GetTileType(tl) == MP_INDUSTRY || GetTileType(tl) == MP_WATER)
			return false;

		if(TileHeight(tl) != TileHeight(candidatetile)) 
			return false;
		
		if(!EnsureNoVehicle(tl))
			return false;
		
		if(CmdFailed(DoCommandByTile(tl, 0, 0, ~DC_EXEC,CMD_LANDSCAPE_CLEAR))) 
			return false;

	}
	END_TILE_LOOP(tl, 9, 9, candidatetile)
	
	return true;
}

//adds a news item for display
void MA_AnnounceAirport(Town *tn, TileIndex tl)
{
	SetDParam(0 ,tn->index);
	AddNewsItem(STR_MA_BUILT_MUNICIPAL_AIRPORT, NEWS_FLAGS(NM_THIN, NF_VIEWPORT|NF_TILE, NT_ECONOMY, 0), tl, 0);
}

//looks for a goodsite (works in outward spiral)
TileIndex MA_FindSite(Town *tn)
{
	uint angle,radius;

	for(radius = MA_MIN_DISTANCE_FROM_TOWN; (radius <= MA_MAX_DISTANCE_FROM_TOWN); radius++) {
		for(angle = 0; (angle <= 360); angle++) {		
			if(MA_CheckCandidate(CircularPos(radius, angle, tn->xy), tn)) return CircularPos(radius, angle, tn->xy);
		}//angle
	}//radius
	return INVALID_TILE;
}

//the fun bit 
void MA_BuildAirport(TileIndex buildtile)
{
	buildtile += TileDiffXY(-3,-3);

	BEGIN_TILE_LOOP(tl, 7, 7, buildtile) {

		DoCommandByTile(tl, 0, 0, DC_EXEC,CMD_LANDSCAPE_CLEAR);
	}
	END_TILE_LOOP(tl, 7, 7, buildtile)
	
	DoCommandByTile(buildtile, AT_INTERNATIONAL, 0, DC_EXEC, CMD_BUILD_AIRPORT);
}

//the main procedure, does the checks and runs the process.
void MunicipalAirport(Town *tn)
{
	Station *st;
	TileIndex tl; 

	PlayerID old_player = _current_player;
	_current_player = OWNER_TOWN;

	if(!_patches.allow_municipal_airports) 
		MA_DestroyAirport(tn);
	
	if(!_patches.allow_municipal_airports 
		|| (_cur_year + 1920) < 1990 
		|| tn->population < MA_MIN_POPULATION) {
		_current_player = old_player;
		return;
	}

	FOR_ALL_STATIONS(st) {

		if(IsValidStation(st)
			&& st->facilities == FACIL_AIRPORT
			&& st->owner == OWNER_TOWN 
			&& st->town == tn) {
			_current_player = old_player;
			return;
		}

	}
	

	
	tl = MA_FindSite(tn);
	
	if(tl == INVALID_TILE) {
		_current_player = old_player;
		return;
	}
	
	MA_BuildAirport(tl);
	MA_AnnounceAirport(tn, tl);
	
	_current_player = old_player;
	
	return;
}

// same as above but isnt as stringent
void MA_EditorAddAirport(Town* tn)
{
	Station *st;
	TileIndex tl;
	PlayerID old_player = _current_player;
	
	_current_player = OWNER_TOWN;
	
	FOR_ALL_STATIONS(st) {
		if(IsValidStation(st)
			&& st->facilities == FACIL_AIRPORT
			&& st->owner == OWNER_TOWN 
			&& st->town == tn
			&& st->airport_type != AT_OILRIG) { //not really needed but you never know
				MA_DestroyAirport(tn);
				_current_player = old_player;
				return;
		}
	}

	if(!_patches.allow_municipal_airports) return;

	if(_cur_year + 1920 < INT_AIRPORT_YEAR) {
		SetDParam(0, tn->index);
		ShowErrorMessage(STR_MA_CANT_BUILD_TOO_EARLY, INVALID_STRING_ID, 300, 300);
		_current_player = old_player;
		return;
	}

	if(tn->population < MA_MIN_POPULATION) {
		SetDParam(0, tn->index);
		ShowErrorMessage(STR_MA_CANT_BUILD_LOW_POPULATION, INVALID_STRING_ID, 300, 300);
		_current_player = old_player;
		return;
	}

	tl = MA_FindSite(tn);

	if(tl == INVALID_TILE) {
		SetDParam(0, tn->index);
		ShowErrorMessage(STR_MA_CANT_BUILD_NO_SITE, INVALID_STRING_ID, 300, 300);
		_current_player = old_player;
		return;
	}
	
	MA_BuildAirport(tl);
	
	
	_current_player = old_player;
	return;
}
