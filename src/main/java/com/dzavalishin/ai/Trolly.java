package com.dzavalishin.ai;

import java.util.Iterator;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Depot;
import com.dzavalishin.game.Engine;
import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Industry;
import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.Map;
import com.dzavalishin.game.Order;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.TileInfo;
import com.dzavalishin.game.Town;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.aystar.AyStar;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.util.BitOps;

public class Trolly extends AiTools  
{

	/*
	 * This AI was created as a direct reaction to the big demand for some good AIs
	 * in OTTD. Too bad it never left alpha-stage, and it is considered dead in its
	 * current form.
	 * By the time of writing this, we, the creator of this AI and a good friend of
	 * mine, are designing a whole new AI-system that allows us to create AIs
	 * easier and without all the fuzz we encountered while I was working on this
	 * AI. By the time that system is finished, you can expect that this AI will
	 * dissapear, because it is pretty obselete and bad programmed.
	 *
	 * Meanwhile I wish you all much fun with this AI; if you are interested as
	 * AI-developer in this AI, I advise you not stare too long to some code, some
	 * things in here really are... strange ;) But in either way: enjoy :)
	 *
	 *  -- TrueLight :: 2005-09-01
	 */


	// This function is called after StartUp. It is the init of an AI
	static void AiNew_State_FirstTime(Player p)
	{
		// This assert is used to protect those function from misuse
		//   You have quickly a small mistake in the state-array
		//   With that, everything would go wrong. Finding that, is almost impossible
		//   With this assert, that problem can never happen.
		assert(p.ainew.state == AiState.FIRST_TIME);
		// We first have to init some things

		/* TODO alpha AI message
		if (PlayerID.getCurrent().id == 1 || Ai._ai.network_client) {
			Global.ShowErrorMessage(Str.INVALID_STRING_ID().id, Str.TEMP_AI_IN_PROGRESS, 0, 0);
		} */

		// The PathFinder (AyStar)
		// TODO: Maybe when an AI goes bankrupt, this is de-init
		//  or when coming from a savegame.. should be checked out!
		p.ainew.path_info.start_tile_tl = null;
		p.ainew.path_info.start_tile_br = null;
		p.ainew.path_info.end_tile_tl = null;
		p.ainew.path_info.end_tile_br = null;
		//p.ainew.pathfinder = new_AyStar_AiPathFinder(12, p.ainew.path_info);

		p.ainew.idle = 0;
		p.ainew.last_vehiclecheck_date = Global.get_date();

		// We ALWAYS start with a bus route.. just some basic money ;)
		p.ainew.action = AiAction.BUS_ROUTE;

		// Let's popup the news, and after that, start building..
		p.ainew.state = AiState.WAKE_UP;
	}


	// This function just waste some time
	//  It keeps it more real. The AI can build on such tempo no normal user
	//  can ever keep up with that. The competitor_speed already delays a bit
	//  but after the AI finished a track it really needs to go to sleep.
	//
	// Let's say, we sleep between one and three days if the AI is put on Very Fast.
	//  This means that on Very Slow it will be between 16 and 48 days.. slow enough?
	static void AiNew_State_Nothing(Player p)
	{
		assert(p.ainew.state == AiState.NOTHING);
		// If we are done idling, start over again
		if (p.ainew.idle == 0) p.ainew.idle = Ai.AI_RandomRange(Global.DAY_TICKS * 2) + Global.DAY_TICKS;
		if (--p.ainew.idle == 0) {
			// We are done idling.. what you say? Let's do something!
			// I mean.. the next tick ;)
			p.ainew.state = AiState.WAKE_UP;
		}
	}


	// This function picks out a task we are going to do.
	//  Currently supported:
	//	    - Make new route
	//	    - Check route
	//	    - Build HQ
	static void AiNew_State_WakeUp(Player p)
	{
		assert(p.ainew.state == AiState.WAKE_UP);
		// First, check if we have a HQ
		if (p.getLocation_of_house() == null) {
			// We have no HQ yet, build one on a random place
			// Random till we found a place for it!
			// TODO: this should not be on a random place..
			AiNew_Build_CompanyHQ(p, TileIndex.get( Ai.AI_Random() % Global.MapSize()) );
			// Enough for now, but we want to come back here the next time
			//  so we do not change any status
			return;
		}

		long money = p.getMoney() - AI_MINIMUM_MONEY;

		// Let's pick an action!
		if (p.ainew.action == AiAction.NONE) {
			int c = Ai.AI_Random() & 0xFF;
			if (p.getCurrent_loan() > 0 &&
					p.old_economy[1].income > AI_MINIMUM_INCOME_FOR_LOAN &&
					c < 10) {
				p.ainew.action = AiAction.REPAY_LOAN;
			} else if (p.ainew.last_vehiclecheck_date + AI_DAYS_BETWEEN_VEHICLE_CHECKS < Global.get_date()) {
				// Check all vehicles once in a while
				p.ainew.action = AiAction.CHECK_ALL_VEHICLES;
				p.ainew.last_vehiclecheck_date = Global.get_date();
			} else if (c < 100 && !Global._patches.ai_disable_veh_roadveh.get()) {
				// Do we have any spots for road-vehicles left open?
				if (Vehicle.GetFreeUnitNumber(Vehicle.VEH_Road).id <= Global._patches.max_roadveh) {
					if (c < 85)
						p.ainew.action = AiAction.TRUCK_ROUTE;
					else
						p.ainew.action = AiAction.BUS_ROUTE;
				}
			} else if (c < 200 && !Global._patches.ai_disable_veh_train.get()) {
				if (Vehicle.GetFreeUnitNumber(Vehicle.VEH_Train).id <= Global._patches.max_trains) {
					// TODO p.ainew.action = AiAction.TRAIN_ROUTE;
				}
			}

			p.ainew.counter = 0;
		}

		if (p.ainew.counter++ > AI_MAX_TRIES_FOR_SAME_ROUTE) {
			p.ainew.action = AiAction.NONE;
			return;
		}

		if (Global._patches.ai_disable_veh_roadveh.get() && (
				p.ainew.action == AiAction.BUS_ROUTE ||
				p.ainew.action == AiAction.TRUCK_ROUTE
				)) {
			p.ainew.action = AiAction.NONE;
			return;
		}

		if (Global._patches.ai_disable_veh_roadveh.get() && (
				p.ainew.action == AiAction.BUS_ROUTE ||
				p.ainew.action == AiAction.TRUCK_ROUTE
				)) {
			p.ainew.action = AiAction.NONE;
			return;
		}

		if (p.ainew.action == AiAction.REPAY_LOAN &&
				money > AI_MINIMUM_LOAN_REPAY_MONEY) {
			// We start repaying some money..
			p.ainew.state = AiState.REPAY_MONEY;
			return;
		}

		if (p.ainew.action == AiAction.CHECK_ALL_VEHICLES) {
			p.ainew.state = AiState.CHECK_ALL_VEHICLES;
			return;
		}

		// It is useless to start finding a route if we don't have enough money
		//  to build the route anyway..
		if (p.ainew.action == AiAction.BUS_ROUTE &&
				money > AI_MINIMUM_BUS_ROUTE_MONEY) {
			if (Vehicle.GetFreeUnitNumber(Vehicle.VEH_Road).id > Global._patches.max_roadveh) {
				p.ainew.action = AiAction.NONE;
				return;
			}
			p.ainew.cargo = AI_NEED_CARGO;
			p.ainew.state = AiState.LOCATE_ROUTE;
			p.ainew.tbt = AI_BUS; // Bus-route
			return;
		}
		if (p.ainew.action == AiAction.TRUCK_ROUTE &&
				money > AI_MINIMUM_TRUCK_ROUTE_MONEY) {
			if (Vehicle.GetFreeUnitNumber(Vehicle.VEH_Road).id > Global._patches.max_roadveh) {
				p.ainew.action = AiAction.NONE;
				return;
			}
			p.ainew.cargo = AI_NEED_CARGO;
			p.ainew.last_id = 0;
			p.ainew.state = AiState.LOCATE_ROUTE;
			p.ainew.tbt = AI_TRUCK;
			return;
		}

		p.ainew.state = AiState.NOTHING;
	}


	static void AiNew_State_ActionDone(Player p)
	{
		p.ainew.action = AiAction.NONE;
		p.ainew.state = AiState.NOTHING;
	}


	// Check if a city or industry is good enough to start a route there
	static boolean AiNew_Check_City_or_Industry(Player p, int ic, int type)
	{
		//Station st;
		if (type == AI_CITY) {
			Town t = Town.GetTown(ic);
			//Station *st;
			int count = 0;
			int j = 0;

			// We don't like roadconstructions, don't even true such a city
			if (t.getRoad_build_months() != 0) return false;

			// Check if the rating in a city is high enough
			//  If not, take a chance if we want to continue
			if (t.getRatings(PlayerID.getCurrent().id) < 0 && Ai.AI_CHANCE16(1,4)) return false;

			if (t.max_pass - t.act_pass < AI_CHECKCITY_NEEDED_CARGO && !Ai.AI_CHANCE16(1,AI_CHECKCITY_CITY_CHANCE)) return false;

			// Check if we have build a station in this town the last 6 months
			//  else we don't do it. This is done, because stat updates can be slow
			//  and sometimes it takes up to 4 months before the stats are corectly.
			//  This way we don't get 12 busstations in one city of 100 population ;)
			//FOR_ALL_STATIONS(st)
			Iterator<Station> ii = Station.getIterator();
			while(ii.hasNext())
			{
				Station st = ii.next();
				// Is it an active station
				if (!st.isValid()) continue;

				// Do we own it?
				if (st.getOwner().isCurrentPlayer()) {
					// Are we talking busses?
					if (p.ainew.tbt == AI_BUS && (Station.FACIL_BUS_STOP & st.getFacilities()) != Station.FACIL_BUS_STOP) continue;
					// Is it the same city as we are in now?
					if (st.town != t) continue;
					// When was this station build?
					if (Global.get_date() - st.getBuild_date() < AI_CHECKCITY_DATE_BETWEEN) return false;
					// Cound the amount of stations in this city that we own
					count++;
				} else {
					// We do not own it, request some info about the station
					//  we want to know if this station gets the same good. If so,
					//  we want to know its rating. If it is too high, we are not going
					//  to build there
					if (0 == st.goods[AcceptedCargo.CT_PASSENGERS].last_speed) continue;
					// Is it around our city
					if (Map.DistanceManhattan(st.getXy(), t.getXy()) > 10) continue;
					// It does take this cargo.. what is his rating?
					if (st.goods[AcceptedCargo.CT_PASSENGERS].rating < AI_CHECKCITY_CARGO_RATING) continue;
					j++;
					// When this is the first station, we build a second with no problem ;)
					if (j == 1) continue;
					// The rating is high.. second station...
					//  a little chance that we still continue
					//  But if there are 3 stations of this size, we never go on...
					if (j == 2 && Ai.AI_CHANCE16(1, AI_CHECKCITY_CARGO_RATING_CHANCE)) continue;
					// We don't like this station :(
					return false;
				}
			}

			// We are about to add one...
			count++;
			// Check if we the city can provide enough cargo for this amount of stations..
			if (count * AI_CHECKCITY_CARGO_PER_STATION > t.max_pass) return false;

			// All check are okay, so we can build here!
			return true;
		}
		if (type == AI_INDUSTRY) {
			Industry i = Industry.GetIndustry(ic);
			//Station st;
			int count = 0;
			int j = 0;

			Town town = i.getTown();

			if (town != null && town.getRatings(PlayerID.getCurrent().id) < 0 && Ai.AI_CHANCE16(1,4)) return false;

			// No limits on delevering stations!
			//  Or for industry that does not give anything yet
			if (i.getProduced_cargo(0) == 0xFF || i.getTotal_production(0) == 0) return true;

			if (i.getTotal_production(0) - i.getTotal_transported(0) < AI_CHECKCITY_NEEDED_CARGO) return false;

			// Check if we have build a station in this town the last 6 months
			//  else we don't do it. This is done, because stat updates can be slow
			//  and sometimes it takes up to 4 months before the stats are corectly.
			//FOR_ALL_STATIONS(st) 
			Iterator<Station> ii = Station.getIterator();
			while(ii.hasNext())
			{
				Station st = ii.next();
				// Is it an active station
				if (!st.isValid()) continue;

				// Do we own it?
				if (st.getOwner().isCurrentPlayer()) {
					// Are we talking trucks?
					if (p.ainew.tbt == AI_TRUCK && (Station.FACIL_TRUCK_STOP & st.getFacilities()) != Station.FACIL_TRUCK_STOP) continue;
					// Is it the same city as we are in now?
					if (st.town != town) continue;
					// When was this station build?
					if (Global.get_date() - st.getBuild_date() < AI_CHECKCITY_DATE_BETWEEN) return false;
					// Cound the amount of stations in this city that we own
					count++;
				} else {
					// We do not own it, request some info about the station
					//  we want to know if this station gets the same good. If so,
					//  we want to know its rating. If it is too high, we are not going
					//  to build there
					if (i.getProduced_cargo(0) == 0xFF) continue;
					// It does not take this cargo
					if (0 == st.goods[i.getProduced_cargo(0)].last_speed) continue;
					// Is it around our industry
					if (Map.DistanceManhattan(st.getXy(), i.xy) > 5) continue;
					// It does take this cargo.. what is his rating?
					if (st.goods[i.getProduced_cargo(0)].rating < AI_CHECKCITY_CARGO_RATING) continue;
					j++;
					// The rating is high.. a little chance that we still continue
					//  But if there are 2 stations of this size, we never go on...
					if (j == 1 && Ai.AI_CHANCE16(1, AI_CHECKCITY_CARGO_RATING_CHANCE)) continue;
					// We don't like this station :(
					return false;
				}
			}

			// We are about to add one...
			count++;
			// Check if we the city can provide enough cargo for this amount of stations..
			if (count * AI_CHECKCITY_CARGO_PER_STATION > i.getTotal_production(0)) return false;

			// All check are okay, so we can build here!
			return true;
		}

		return true;
	}


	// This functions tries to locate a good route
	static void AiNew_State_LocateRoute(Player p)
	{
		assert(p.ainew.state == AiState.LOCATE_ROUTE);
		// For now, we only support PASSENGERS, CITY and BUSSES

		// We don't have a route yet
		if (p.ainew.cargo == AI_NEED_CARGO) {
			p.ainew.new_cost = 0; // No cost yet
			p.ainew.temp = -1;
			// Reset the counter
			p.ainew.counter = 0;

			p.ainew.from_ic = -1;
			p.ainew.to_ic = -1;
			if (p.ainew.tbt == AI_BUS) {
				// For now we only have a passenger route
				p.ainew.cargo = AcceptedCargo.CT_PASSENGERS;

				// Find a route to cities
				p.ainew.from_type = AI_CITY;
				p.ainew.to_type = AI_CITY;
			} else if (p.ainew.tbt == AI_TRUCK) {
				p.ainew.cargo = AI_NO_CARGO;

				p.ainew.from_type = AI_INDUSTRY;
				p.ainew.to_type = AI_INDUSTRY;
			}

			// Now we are doing initing, we wait one tick
			return;
		}

		// Increase the counter and abort if it is taking too long!
		p.ainew.counter++;
		if (p.ainew.counter > AI_LOCATE_ROUTE_MAX_COUNTER) {
			// Switch back to doing nothing!
			p.ainew.state = AiState.NOTHING;
			return;
		}

		// We are going to locate a city from where we are going to connect
		if (p.ainew.from_ic == -1) {
			if (p.ainew.temp == -1) {
				// First, we pick a random spot to search from
				if (p.ainew.from_type == AI_CITY)
					p.ainew.temp = Ai.AI_RandomRange(Town.GetCount());
				else
					p.ainew.temp = Ai.AI_RandomRange(Industry.getCount());
			}

			if (!AiNew_Check_City_or_Industry(p, p.ainew.temp, p.ainew.from_type)) {
				// It was not a valid city
				//  increase the temp with one, and return. We will come back later here
				//  to try again
				p.ainew.temp++;
				if (p.ainew.from_type == AI_CITY) {
					if (p.ainew.temp >= Town.GetCount()) p.ainew.temp = 0;
				} else {
					if (p.ainew.temp >= Industry.getCount()) p.ainew.temp = 0;
				}

				// Don't do an attempt if we are trying the same id as the last time...
				if (p.ainew.last_id == p.ainew.temp) return;
				p.ainew.last_id = p.ainew.temp;

				return;
			}

			// We found a good city/industry, save the data of it
			p.ainew.from_ic = p.ainew.temp;

			// Start the next tick with finding a to-city
			p.ainew.temp = -1;
			return;
		}

		// Find a to-city
		if (p.ainew.temp == -1) {
			// First, we pick a random spot to search to
			if (p.ainew.to_type == AI_CITY)
				p.ainew.temp = Ai.AI_RandomRange(Town.GetCount());
			else
				p.ainew.temp = Ai.AI_RandomRange(Industry.getCount());
		}

		// The same city is not allowed
		// Also check if the city is valid
		if (p.ainew.temp != p.ainew.from_ic && AiNew_Check_City_or_Industry(p, p.ainew.temp, p.ainew.to_type)) {
			// Maybe it is valid..

			// We need to know if they are not to far apart from eachother..
			// We do that by checking how much cargo we have to move and how long the route
			//   is.

			if (p.ainew.from_type == AI_CITY && p.ainew.tbt == AI_BUS) {
				int max_cargo = Town.GetTown(p.ainew.from_ic).max_pass + Town.GetTown(p.ainew.temp).max_pass;
				max_cargo -= Town.GetTown(p.ainew.from_ic).act_pass + Town.GetTown(p.ainew.temp).act_pass;
				// max_cargo is now the amount of cargo we can move between the two cities
				// If it is more than the distance, we allow it
				if (Map.DistanceManhattan(Town.GetTown(p.ainew.from_ic).getXy(), Town.GetTown(p.ainew.temp).getXy()) <= max_cargo * AI_LOCATEROUTE_BUS_CARGO_DISTANCE) {
					// We found a good city/industry, save the data of it
					p.ainew.to_ic = p.ainew.temp;
					p.ainew.state = AiState.FIND_STATION;

					Global.DEBUG_ai(1,
							"[AiNew - LocateRoute] Found bus-route of %d tiles long (from %d to %d)",
							Map.DistanceManhattan(Town.GetTown(p.ainew.from_ic).getXy(), Town.GetTown(p.ainew.temp).getXy()),
							p.ainew.from_ic,
							p.ainew.temp
							);

					p.ainew.from_tile = null;
					p.ainew.to_tile = null;

					return;
				}
			} else if (p.ainew.tbt == AI_TRUCK) {
				boolean found = false;
				int max_cargo = 0;
				int i;
				// TODO: in max_cargo, also check other cargo (beside [0])
				// First we check if the from_ic produces cargo that this ic accepts
				final Industry ind_fic = Industry.GetIndustry(p.ainew.from_ic);
				final Industry ind_tmp = Industry.GetIndustry(p.ainew.temp);
				if (ind_fic.getProduced_cargo(0) != 0xFF && ind_fic.getTotal_production(0) != 0) {
					for (i=0;i<3;i++) {
						if (ind_tmp.getAccepts_cargo(i) == 0xFF) break;
						if (ind_fic.getProduced_cargo(0) == ind_tmp.getAccepts_cargo(i)) {
							// Found a compatbiel industry
							max_cargo = ind_fic.getTotal_production(0) - ind_fic.getTotal_transported(0);
							found = true;
							p.ainew.from_deliver = true;
							p.ainew.to_deliver = false;
							break;
						}
					}
				}
				if (!found && ind_tmp.getProduced_cargo(0) != 0xFF && ind_tmp.getTotal_production(0) != 0) {
					// If not check if the current ic produces cargo that the from_ic accepts
					for (i=0;i<3;i++) {
						if (ind_fic.getAccepts_cargo(i) == 0xFF) break;
						if (ind_tmp.getProduced_cargo(0) == ind_fic.getAccepts_cargo(i)) {
							// Found a compatbiel industry
							found = true;
							max_cargo = ind_tmp.getTotal_production(0) - ind_fic.getTotal_transported(0);
							p.ainew.from_deliver = false;
							p.ainew.to_deliver = true;
							break;
						}
					}
				}
				if (found) {
					// Yeah, they are compatible!!!
					// Check the length against the amount of goods
					if (Map.DistanceManhattan(ind_fic.xy, ind_tmp.xy) > AI_LOCATEROUTE_TRUCK_MIN_DISTANCE &&
							Map.DistanceManhattan(ind_fic.xy, ind_tmp.xy) <= max_cargo * AI_LOCATEROUTE_TRUCK_CARGO_DISTANCE) {
						p.ainew.to_ic = p.ainew.temp;
						if (p.ainew.from_deliver) {
							p.ainew.cargo = ind_fic.getProduced_cargo(0);
						} else {
							p.ainew.cargo = ind_tmp.getProduced_cargo(0);
						}
						p.ainew.state = AiState.FIND_STATION;

						Global.DEBUG_ai(1,
								"[AiNew - LocateRoute] Found truck-route of %d tiles long (from %d to %d)",
								Map.DistanceManhattan(ind_fic.xy, ind_tmp.xy),
								p.ainew.from_ic,
								p.ainew.temp
								);

						p.ainew.from_tile = null;
						p.ainew.to_tile = null;

						return;
					}
				}
			}
		}

		// It was not a valid city
		//  increase the temp with one, and return. We will come back later here
		//  to try again
		p.ainew.temp++;
		if (p.ainew.to_type == AI_CITY) {
			if (p.ainew.temp >= Town.GetCount()) p.ainew.temp = 0;
		} else {
			if (p.ainew.temp >= Industry.getCount()) p.ainew.temp = 0;
		}

		// Don't do an attempt if we are trying the same id as the last time...
		if (p.ainew.last_id == p.ainew.temp) return;
		p.ainew.last_id = p.ainew.temp;
	}


	// Check if there are not more than a certain amount of vehicles pointed to a certain
	//  station. This to prevent 10 busses going to one station, which gives... problems ;)
	static boolean AiNew_CheckVehicleStation(Player p, Station st)
	{
		int [] count = {0};
		/*
		Vehicle v;

		// Also check if we don't have already a lot of busses to this city...
		FOR_ALL_VEHICLES(v) {
			if (v.owner == _current_player) {
				final Order *order;

				FOR_VEHICLE_ORDERS(v, order) {
					if (order.type == OT_GOTO_STATION && GetStation(order.station) == st) {
						// This vehicle has this city in its list
						count++;
					}
				}
			}
		}*/

		Vehicle.forEach( v -> {
			v.forEachOrder( order -> {
				if (order.getType() == Order.OT_GOTO_STATION 
						&& Station.GetStation(order.getStation()).equals(st) ) {
					// This vehicle has this city in its list
					count[0]++;
				}

			});
		});

		if (count[0] > AI_CHECK_MAX_VEHICLE_PER_STATION) return false;
		return true;
	}

	// This function finds a good spot for a station
	static void AiNew_State_FindStation(Player p)
	{
		TileIndex tile;
		//Station st;
		int count = 0;
		TileIndex new_tile = null;
		int direction = 0;
		Town town = null;
		Industry industry = null;
		assert(p.ainew.state == AiState.FIND_STATION);

		if (p.ainew.from_tile == null) {
			// First we scan for a station in the from-city
			if (p.ainew.from_type == AI_CITY) {
				town = Town.GetTown(p.ainew.from_ic);
				tile = town.getXy();
			} else {
				industry = Industry.GetIndustry(p.ainew.from_ic);
				tile = industry.xy;
			}
		} else if (p.ainew.to_tile == null) {
			// Second we scan for a station in the to-city
			if (p.ainew.to_type == AI_CITY) {
				town = Town.GetTown(p.ainew.to_ic);
				tile = town.getXy();
			} else {
				industry = Industry.GetIndustry(p.ainew.to_ic);
				tile = industry.xy;
			}
		} else {
			// Unsupported request
			// Go to FIND_PATH
			p.ainew.temp = -1;
			p.ainew.state = AiState.FIND_PATH;
			return;
		}

		// First, we are going to look at the stations that already exist inside the city
		//  If there is enough cargo left in the station, we take that station
		//  If that is not possible, and there are more than 2 stations in the city, abort
		int vi = AiNew_PickVehicle(p);
		// Euhmz, this should not happen _EVER_
		// Quit finding a route...
		if (vi == -1) { p.ainew.state = AiState.NOTHING; return; }

		//FOR_ALL_STATIONS(st) 
		Iterator<Station> ii = Station.getIterator();
		while(ii.hasNext())
		{
			Station st = ii.next();

			if (!st.isValid()) continue; 

			if (!st.getOwner().isCurrentPlayer()) continue;

			if (p.ainew.tbt == AI_BUS && (Station.FACIL_BUS_STOP & st.getFacilities()) == Station.FACIL_BUS_STOP) {
				if (st.town == town) {
					// Check how much cargo there is left in the station
					if ((st.goods[p.ainew.cargo].waiting_acceptance & 0xFFF) > Engine.RoadVehInfo(vi).capacity * AI_STATION_REUSE_MULTIPLER) {
						if (AiNew_CheckVehicleStation(p, st)) {
							// We did found a station that was good enough!
							new_tile = st.getXy();
							// Cheap way to get the direction of the station...
							//  Bus stations save it as 0x47 .. 0x4A, so decrease it with 0x47, and tada!
							//direction = _m[st.xy].m5 - 0x47;
							direction = st.getXy().M().m5 - 0x47;
							break;
						}
					}
					count++;
				}
			}


		}
		// We are going to add a new station...
		if (new_tile == null) count++;
		// No more than 2 stations allowed in a city
		//  This is because only the best 2 stations of one cargo do get any cargo
		if (count > 2) {
			p.ainew.state = AiState.NOTHING;
			return;
		}

		if (new_tile == null && p.ainew.tbt == AI_BUS) {
			int x, y, i = 0;
			int r;
			int best;
			//int [] accepts = new int[AcceptedCargo.NUM_CARGO];
			TileIndex [] found_spot = new TileIndex[AI_FINDSTATION_TILE_RANGE*AI_FINDSTATION_TILE_RANGE*4];
			int [] found_best = new int[AI_FINDSTATION_TILE_RANGE*AI_FINDSTATION_TILE_RANGE*4];
			// To find a good spot we scan a range from the center, a get the point
			//  where we get the most cargo and where it is buildable.
			// TODO: also check for station of myself and make sure we are not
			//   taking eachothers passangers away (bad result when it does not)
			for (x = tile.TileX() - AI_FINDSTATION_TILE_RANGE; x <= tile.TileX() + AI_FINDSTATION_TILE_RANGE; x++) {
				for (y = tile.TileY() - AI_FINDSTATION_TILE_RANGE; y <= tile.TileY() + AI_FINDSTATION_TILE_RANGE; y++) {
					new_tile = TileIndex.TileXY(x, y);
					if (new_tile.IsTileType(TileTypes.MP_CLEAR) || new_tile.IsTileType(TileTypes.MP_TREES)) {
						AcceptedCargo accepts = new AcceptedCargo();
						// This tile we can build on!
						// Check acceptance
						// XXX - Get the catchment area
						Station.GetAcceptanceAroundTiles(accepts, new_tile, 1, 1, 4);
						// >> 3 == 0 means no cargo
						if (accepts.ct[p.ainew.cargo] >> 3 == 0) continue;
						// See if we can build the station
						r = AiNew_Build_Station(p, p.ainew.tbt, new_tile, 0, 0, 0, Cmd.DC_QUERY_COST);
						if (Cmd.CmdFailed(r)) continue;
						// We can build it, so add it to found_spot
						found_spot[i] = new_tile;
						found_best[i++] = accepts.ct[p.ainew.cargo];
					}
				}
			}

			// If i is still zero, we did not found anything :(
			if (i == 0) {
				p.ainew.state = AiState.NOTHING;
				return;
			}

			// Go through all the found_best and check which has the highest value
			best = 0;
			new_tile = null;

			for (x=0;x<i;x++) {
				if (found_best[x] > best ||
						(found_best[x] == best && Map.DistanceManhattan(tile, new_tile) > Map.DistanceManhattan(tile, found_spot[x]))) {
					new_tile = found_spot[x];
					best = found_best[x];
				}
			}

			// See how much it is going to cost us...
			r = AiNew_Build_Station(p, p.ainew.tbt, new_tile, 0, 0, 0, Cmd.DC_QUERY_COST);
			p.ainew.new_cost += r;

			direction = AI_PATHFINDER_NO_DIRECTION;
		} else if (new_tile == null && p.ainew.tbt == AI_TRUCK) {
			// Truck station locater works differently.. a station can be on any place
			//  as long as it is in range. So we give back code AI_STATION_RANGE
			//  so the pathfinder routine can work it out!
			new_tile = AiConst.AI_STATION_RANGE();
			direction = AI_PATHFINDER_NO_DIRECTION;
		}

		if (p.ainew.from_tile == null) {
			p.ainew.from_tile = new_tile;
			p.ainew.from_direction = direction;
			// Now we found thisone, go in for to_tile
			return;
		} else if (p.ainew.to_tile == null) {
			p.ainew.to_tile = new_tile;
			p.ainew.to_direction = direction;
			// K, done placing stations!
			p.ainew.temp = -1;
			p.ainew.state = AiState.FIND_PATH;
			return;
		}
	}


	// We try to find a path between 2 points
	static void AiNew_State_FindPath(Player p)
	{
		int r;
		assert(p.ainew.state == AiState.FIND_PATH);

		// First time, init some data
		if (p.ainew.temp == -1) {
			// Init path_info
			if (p.ainew.from_tile.equals(AiConst.AI_STATION_RANGE()))
			{
				Industry fromi = Industry.GetIndustry(p.ainew.from_ic);
				// For truck routes we take a range around the industry
				p.ainew.path_info.start_tile_tl = fromi.xy.isub(TileIndex.TileDiffXY(1, 1));
				p.ainew.path_info.start_tile_br = fromi.xy.iadd(TileIndex.TileDiffXY(fromi.getWidth()+1, fromi.getHeight()+1));
				p.ainew.path_info.start_direction = p.ainew.from_direction;
			} else {
				p.ainew.path_info.start_tile_tl = p.ainew.from_tile;
				p.ainew.path_info.start_tile_br = p.ainew.from_tile;
				p.ainew.path_info.start_direction = p.ainew.from_direction;
			}

			if (p.ainew.to_tile.equals(AiConst.AI_STATION_RANGE())) {
				Industry toi = Industry.GetIndustry(p.ainew.to_ic);
				p.ainew.path_info.end_tile_tl = toi.xy.isub(TileIndex.TileDiffXY(1, 1));
				p.ainew.path_info.end_tile_br = toi.xy.iadd(TileIndex.TileDiffXY(toi.getWidth()+1, toi.getHeight()+1));
				p.ainew.path_info.end_direction = p.ainew.to_direction;
			} else {
				p.ainew.path_info.end_tile_tl = p.ainew.to_tile;
				p.ainew.path_info.end_tile_br = p.ainew.to_tile;
				p.ainew.path_info.end_direction = p.ainew.to_direction;
			}

			if (p.ainew.tbt == AI_TRAIN)
				p.ainew.path_info.rail_or_road = true;
			else
				p.ainew.path_info.rail_or_road = false;

			//AyStar pathfinder = new AIAyStar();
			p.ainew.pathfinder = new_AyStar_AiPathFinder(12, p.ainew.path_info);
			// First, clean the pathfinder with our new begin and endpoints
			//clean_AyStar_AiPathFinder(pathfinder, p.ainew.path_info);

			p.ainew.temp = 0;
		}



		// Start the pathfinder
		r = p.ainew.pathfinder.main();
		// If it return: no match, stop it...
		if (r == AyStar.AYSTAR_NO_PATH) {
			Global.DEBUG_ai(1,"[AiNew] PathFinder found no route!");
			// Start all over again...
			p.ainew.state = AiState.NOTHING;
			return;
		}
		if (r == AyStar.AYSTAR_FOUND_END_NODE) {
			// We found the end-point
			p.ainew.temp = -1;
			p.ainew.state = AiState.FIND_DEPOT;
			return;
		}
		// In any other case, we are still busy finding the route...
	}


	// This function tries to locate a good place for a depot!
	static void AiNew_State_FindDepot(Player p)
	{
		// To place the depot, we walk through the route, and if we find a lovely spot (MP_CLEAR, MP_TREES), we place it there..
		// Simple, easy, works!
		// To make the depot stand in the middle of the route, we start from the center..
		// But first we walk through the route see if we can find a depot that is ours
		//  this keeps things nice ;)
		int g, i, r;
		//int j;
		TileIndex tile;
		assert(p.ainew.state == AiState.FIND_DEPOT);

		p.ainew.depot_tile = null;

		for (i=2;i<p.ainew.path_info.route_length-2;i++) 
		{
			tile = p.ainew.path_info.route[i];
			for (int j = 0; j < 4; j++) 
			{
				final TileIndex offsetTile = tile.OffsetByDir(j);
				if (offsetTile.typeIs(TileTypes.MP_STREET)) {
					// Its a street, test if it is a depot
					if(0 != (offsetTile.M().m5 & 0x20)) {
						// We found a depot, is it ours? (TELL ME!!!)
						if (offsetTile.ownerIs(PlayerID.getCurrent())) {
							// Now, is it pointing to the right direction.........
							if (BitOps.GB(offsetTile.M().m5, 0, 2) == (j ^ 2)) {
								// Yeah!!!
								p.ainew.depot_tile = offsetTile;
								p.ainew.depot_direction = j ^ 2; // Reverse direction
								p.ainew.state = AiState.VERIFY_ROUTE;
								return;
							}
						}
					}
				}
			}
		}

		// This routine let depot finding start in the middle, and work his way to the stations
		// It makes depot placing nicer :)
		i = p.ainew.path_info.route_length / 2;
		g = 1;
		while (i > 1 && i < p.ainew.path_info.route_length - 2) {
			i += g;
			g *= -1;
			if(g < 0) g--; else g++;

			if (p.ainew.path_info.route_extra[i] != 0 || p.ainew.path_info.route_extra[i+1] != 0) {
				// Bridge or tunnel.. we can't place a depot there
				continue;
			}

			tile = p.ainew.path_info.route[i];

			for (int j = 0; j < 4; j++) {
				// It may not be placed on the road/rail itself
				// And because it is not build yet, we can't see it on the tile..
				// So check the surrounding tiles :)
				if (tile.OffsetByDir(j).equals( p.ainew.path_info.route[i-1] ) ||
						tile.OffsetByDir(j).equals( p.ainew.path_info.route[i+1] ) )
					continue;
				// Not around a bridge?
				if (p.ainew.path_info.route_extra[i] != 0) continue;
				if (tile.IsTileType(TileTypes.MP_TUNNELBRIDGE)) continue;
				// Is the terrain clear?
				if (tile.OffsetByDir(j).typeIs(TileTypes.MP_CLEAR) ||
						tile.OffsetByDir(j).typeIs(TileTypes.MP_TREES)) {
					TileInfo ti = new TileInfo();
					Landscape.FindLandscapeHeightByTile(ti, tile);
					// If the current tile is on a slope (tileh != 0) then we do not allow this
					if (ti.tileh != 0) continue;
					// Check if everything went okay..
					r = AiNew_Build_Depot(p, tile.OffsetByDir(j), j ^ 2, 0);
					if (Cmd.CmdFailed(r)) continue;
					// Found a spot!
					p.ainew.new_cost += r;
					p.ainew.depot_tile = tile.OffsetByDir(j);
					p.ainew.depot_direction = j ^ 2; // Reverse direction
					p.ainew.state = AiState.VERIFY_ROUTE;
					return;
				}
			}
		}

		// Failed to find a depot?
		p.ainew.state = AiState.NOTHING;
	}


	// This function calculates how many vehicles there are needed on this
	//  traject.
	// It works pretty simple: get the length, see how much we move around
	//  and hussle that, and you know how many vehicles there are needed.
	// It returns the cost for the vehicles
	static int AiNew_HowManyVehicles(Player p)
	{
		if (p.ainew.tbt == AI_BUS) {
			// For bus-routes we look at the time before we are back in the station
			int i, length, tiles_a_day;
			int amount;
			i = AiNew_PickVehicle(p);
			if (i == -1) return 0;
			// Passenger run.. how long is the route?
			length = p.ainew.path_info.route_length;
			// Calculating tiles a day a vehicle moves is not easy.. this is how it must be done!
			tiles_a_day = Engine.RoadVehInfo(i).max_speed * Global.DAY_TICKS / 256 / 16;
			// We want a vehicle in a station once a month at least, so, calculate it!
			// (the * 2 is because we have 2 stations ;))
			amount = length * 2 * 2 / tiles_a_day / 30;
			if (amount == 0) amount = 1;
			return amount;
		} else if (p.ainew.tbt == AI_TRUCK) {
			// For truck-routes we look at the cargo
			int i, length, amount, tiles_a_day;
			int max_cargo;
			i = AiNew_PickVehicle(p);
			if (i == -1) return 0;
			// Passenger run.. how long is the route?
			length = p.ainew.path_info.route_length;
			// Calculating tiles a day a vehicle moves is not easy.. this is how it must be done!
			tiles_a_day = Engine.RoadVehInfo(i).max_speed * Global.DAY_TICKS / 256 / 16;
			if (p.ainew.from_deliver)
				max_cargo = Industry.GetIndustry(p.ainew.from_ic).getTotal_production(0);
			else
				max_cargo = Industry.GetIndustry(p.ainew.to_ic).getTotal_production(0);

			// This is because moving 60% is more than we can dream of!
			max_cargo *= 0.6;
			// We want all the cargo to be gone in a month.. so, we know the cargo it delivers
			//  we know what the vehicle takes with him, and we know the time it takes him
			//  to get back here.. now let's do some math!
			amount = 2 * length * max_cargo / tiles_a_day / 30 / Engine.RoadVehInfo(i).capacity;
			amount += 1;
			return amount;
		} else {
			// Currently not supported
			return 0;
		}
	}


	// This function checks:
	//   - If the route went okay
	//   - Calculates the amount of money needed to build the route
	//   - Calculates how much vehicles needed for the route
	static void AiNew_State_VerifyRoute(Player p)
	{
		int res, i;
		assert(p.ainew.state == AiState.VERIFY_ROUTE);

		// Let's calculate the cost of the path..
		//  new_cost already contains the cost of the stations
		p.ainew.path_info.position = -1;

		do {
			p.ainew.path_info.position++;
			p.ainew.new_cost += AiNew_Build_RoutePart(p, p.ainew.path_info, Cmd.DC_QUERY_COST);
		} while (p.ainew.path_info.position != -2);

		// Now we know the price of build station + path. Now check how many vehicles
		//  we need and what the price for that will be
		res = AiNew_HowManyVehicles(p);
		// If res == 0, no vehicle was found, or an other problem did occour
		if (res == 0) {
			p.ainew.state = AiState.NOTHING;
			return;
		}
		p.ainew.amount_veh = res;
		p.ainew.cur_veh = 0;

		// Check how much it it going to cost us..
		for (i=0;i<res;i++) {
			p.ainew.new_cost += AiNew_Build_Vehicle(p, null, Cmd.DC_QUERY_COST);
		}

		// Now we know how much the route is going to cost us
		//  Check if we have enough money for it!
		if (p.ainew.new_cost > p.getMoney() - AI_MINIMUM_MONEY) {
			// Too bad..
			Global.DEBUG_ai(1,"[AiNew] Can't pay for this route (%d)", p.ainew.new_cost);
			p.ainew.state = AiState.NOTHING;
			return;
		}

		// Now we can build the route, check the direction of the stations!
		if (p.ainew.from_direction == AI_PATHFINDER_NO_DIRECTION) {
			p.ainew.from_direction = AiNew_GetDirection(p.ainew.path_info.route[p.ainew.path_info.route_length-1], p.ainew.path_info.route[p.ainew.path_info.route_length-2]);
		}
		if (p.ainew.to_direction == AI_PATHFINDER_NO_DIRECTION) {
			p.ainew.to_direction = AiNew_GetDirection(p.ainew.path_info.route[0], p.ainew.path_info.route[1]);
		}
		if (p.ainew.from_tile.equals(AiConst.AI_STATION_RANGE()))
			p.ainew.from_tile = p.ainew.path_info.route[p.ainew.path_info.route_length-1];
		if (p.ainew.to_tile.equals(AiConst.AI_STATION_RANGE()))
			p.ainew.to_tile = p.ainew.path_info.route[0];

		p.ainew.state = AiState.BUILD_STATION;
		p.ainew.temp = 0;

		Global.DEBUG_ai(1,"[AiNew] The route is set and buildable.. going to build it!");
	}


	// Build the stations
	static void AiNew_State_BuildStation(Player p)
	{
		int res = 0;
		assert(p.ainew.state == AiState.BUILD_STATION);
		if (p.ainew.temp == 0) {
			if (!p.ainew.from_tile.typeIs(TileTypes.MP_STATION))
				res = AiNew_Build_Station(p, p.ainew.tbt, p.ainew.from_tile, 0, 0, p.ainew.from_direction, Cmd.DC_EXEC);
		} else {
			if (!p.ainew.to_tile.typeIs(TileTypes.MP_STATION))
				res = AiNew_Build_Station(p, p.ainew.tbt, p.ainew.to_tile, 0, 0, p.ainew.to_direction, Cmd.DC_EXEC);
			p.ainew.state = AiState.BUILD_PATH;
		}
		if (Cmd.CmdFailed(res)) {
			Global.DEBUG_ai(0,"[AiNew - BuildStation] Strange but true... station can not be build!");
			p.ainew.state = AiState.NOTHING;
			// If the first station _was_ build, destroy it
			if (p.ainew.temp != 0)
				Ai.AI_DoCommand(p.ainew.from_tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
			return;
		}
		p.ainew.temp++;
	}

	private static final byte _roadbits_by_dir[] = {2,1,8,4};

	// Build the path
	static void AiNew_State_BuildPath(Player p)
	{
		assert(p.ainew.state == AiState.BUILD_PATH);
		// p.ainew.temp is set to -1 when this function is called for the first time
		if (p.ainew.temp == -1) {
			Global.DEBUG_ai(1,"[AiNew] Starting to build the path..");
			// Init the counter
			p.ainew.counter = (4 - GameOptions._opt.diff.competitor_speed) * AI_BUILDPATH_PAUSE + 1;
			// Set the position to the startingplace (-1 because in a minute we do ++)
			p.ainew.path_info.position = -1;
			// And don't do this again
			p.ainew.temp = 0;
		}
		// Building goes very fast on normal rate, so we are going to slow it down..
		//  By let the counter count from AI_BUILDPATH_PAUSE to 0, we have a nice way :)
		if (--p.ainew.counter != 0) return;
		p.ainew.counter = (4 - GameOptions._opt.diff.competitor_speed) * AI_BUILDPATH_PAUSE + 1;

		// Increase the building position
		p.ainew.path_info.position++;
		// Build route
		AiNew_Build_RoutePart(p, p.ainew.path_info, Cmd.DC_EXEC);
		if (p.ainew.path_info.position == -2) {
			// This means we are done building!

			if (p.ainew.tbt == AI_TRUCK && !Global._patches.roadveh_queue) {
				// If they not queue, they have to go up and down to try again at a station...
				// We don't want that, so try building some road left or right of the station
				int dir1, dir2, dir3;
				TileIndex tile;
				int i, ret;
				for (i=0;i<2;i++) {
					if (i == 0) {
						tile = p.ainew.from_tile.OffsetByDir(p.ainew.from_direction);
						dir1 = p.ainew.from_direction - 1;
						if (dir1 < 0) dir1 = 3;
						dir2 = p.ainew.from_direction + 1;
						if (dir2 > 3) dir2 = 0;
						dir3 = p.ainew.from_direction;
					} else {
						tile = p.ainew.to_tile.OffsetByDir(p.ainew.to_direction);
						dir1 = p.ainew.to_direction - 1;
						if (dir1 < 0) dir1 = 3;
						dir2 = p.ainew.to_direction + 1;
						if (dir2 > 3) dir2 = 0;
						dir3 = p.ainew.to_direction;
					}

					ret = Ai.AI_DoCommand(tile, _roadbits_by_dir[dir1], 0, Cmd.DC_EXEC | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
					if (!Cmd.CmdFailed(ret)) {
						TileIndex toff = tile.OffsetByDir(dir1);
						TileIndex toff2 = toff.OffsetByDir(dir1);
						if (toff.IsTileType(TileTypes.MP_CLEAR) || toff.IsTileType(TileTypes.MP_TREES)) {
							ret = Ai.AI_DoCommand(toff, AiNew_GetRoadDirection(tile, toff, toff2), 0, Cmd.DC_EXEC | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
							if (!Cmd.CmdFailed(ret)) {
								if (toff2.IsTileType(TileTypes.MP_CLEAR) || toff2.IsTileType(TileTypes.MP_TREES))
									Ai.AI_DoCommand(toff2, AiNew_GetRoadDirection(toff, toff2, toff2.OffsetByDir(dir1)), 0, Cmd.DC_EXEC | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
							}
						}
					}

					ret = Ai.AI_DoCommand(tile, _roadbits_by_dir[dir2], 0, Cmd.DC_EXEC | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
					if (!Cmd.CmdFailed(ret)) {
						//dir2 = TileOffsByDir(dir2);
						TileIndex toff = tile.OffsetByDir(dir2);
						TileIndex toff2 = toff.OffsetByDir(dir2);
						TileIndex toff3 = toff2.OffsetByDir(dir2);
						if (toff.IsTileType(TileTypes.MP_CLEAR) || toff.IsTileType(TileTypes.MP_TREES)) {
							ret = Ai.AI_DoCommand(toff, AiNew_GetRoadDirection(tile, toff, toff2), 0, Cmd.DC_EXEC | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
							if (!Cmd.CmdFailed(ret)) {
								if(toff2.IsTileType(TileTypes.MP_CLEAR) || toff2.IsTileType(TileTypes.MP_TREES))
									Ai.AI_DoCommand(toff2, AiNew_GetRoadDirection(toff, toff2, toff3), 0, Cmd.DC_EXEC | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
							}
						}
					}

					ret = Ai.AI_DoCommand(tile, _roadbits_by_dir[dir3^2], 0, Cmd.DC_EXEC | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
					if (!Cmd.CmdFailed(ret)) {
						//dir3 = TileOffsByDir(dir3);
						TileIndex toff = tile.OffsetByDir(dir3);
						TileIndex toff2 = toff.OffsetByDir(dir3);
						TileIndex toff3 = toff2.OffsetByDir(dir3);
						if(toff.IsTileType(TileTypes.MP_CLEAR) || toff.IsTileType(TileTypes.MP_TREES)) {
							ret = Ai.AI_DoCommand(toff, AiNew_GetRoadDirection(tile, toff, toff2), 0, Cmd.DC_EXEC | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
							if (!Cmd.CmdFailed(ret)) {
								if (toff2.IsTileType(TileTypes.MP_CLEAR) || toff2.IsTileType(TileTypes.MP_TREES))
									Ai.AI_DoCommand(toff2, AiNew_GetRoadDirection(toff, toff2, toff3), 0, Cmd.DC_EXEC | Cmd.DC_NO_WATER, Cmd.CMD_BUILD_ROAD);
							}
						}
					}
				}
			}


			Global.DEBUG_ai(1,"[AiNew] Done building the path (cost: %d)", p.ainew.new_cost);
			p.ainew.state = AiState.BUILD_DEPOT;
		}
	}


	// Builds the depot
	static void AiNew_State_BuildDepot(Player p)
	{
		int res = 0;
		assert(p.ainew.state == AiState.BUILD_DEPOT);

		if (p.ainew.depot_tile.IsTileType(TileTypes.MP_STREET) && 0 != (p.ainew.depot_tile.M().m5 & 0x20)) {
			if (p.ainew.depot_tile.ownerIs(PlayerID.getCurrent())) {
				// The depot is already builded!
				p.ainew.state = AiState.BUILD_VEHICLE;
				return;
			} else {
				// There is a depot, but not of our team! :(
				p.ainew.state = AiState.NOTHING;
				return;
			}
		}

		// There is a bus on the tile we want to build road on... idle till he is gone! (BAD PERSON! :p)
		if (!p.ainew.depot_tile.OffsetByDir(p.ainew.depot_direction).EnsureNoVehicle())
			return;

		res = AiNew_Build_Depot(p, p.ainew.depot_tile, p.ainew.depot_direction, Cmd.DC_EXEC);
		if (Cmd.CmdFailed(res)) {
			Global.DEBUG_ai(0,"[AiNew - BuildDepot] Strange but true... depot can not be build!");
			p.ainew.state = AiState.NOTHING;
			return;
		}

		p.ainew.state = AiState.BUILD_VEHICLE;
		p.ainew.idle = 10;
		p.ainew.veh_main_id = VehicleID.getInvalid();
	}


	// Build vehicles
	static void AiNew_State_BuildVehicle(Player p)
	{
		int res;
		assert(p.ainew.state == AiState.BUILD_VEHICLE);

		// Check if we need to build a vehicle
		if (p.ainew.amount_veh == 0) {
			// Nope, we are done!
			// This means: we are all done! The route is open.. go back to NOTHING
			//  He will idle some time and it will all start over again.. :)
			p.ainew.state = AiState.ACTION_DONE;
			return;
		}
		if (--p.ainew.idle != 0) return;
		// It is realistic that the AI can only build 1 vehicle a day..
		// This makes sure of that!
		p.ainew.idle = AI_BUILD_VEHICLE_TIME_BETWEEN;

		// Build the vehicle
		res = AiNew_Build_Vehicle(p, p.ainew.depot_tile, Cmd.DC_EXEC);
		if (Cmd.CmdFailed(res)) {
			// This happens when the AI can't build any more vehicles!
			p.ainew.state = AiState.NOTHING;
			return;
		}
		// Increase the current counter
		p.ainew.cur_veh++;
		// Decrease the total counter
		p.ainew.amount_veh--;
		// Go give some orders!
		p.ainew.state = AiState.GIVE_ORDERS;
	}


	// Put the stations in the order list
	static void AiNew_State_GiveOrders(Player p)
	{
		int idx;
		//Order order;

		assert(p.ainew.state == AiState.GIVE_ORDERS);

		// Get the new ID
		/* XXX -- Because this AI isn't using any event-system, this is VERY dangerous!
		 *  There is no way telling if the vehicle is already bought (or delayed by the
		 *  network), and if bought, if not an other vehicle is bought in between.. in
		 *  other words, there is absolutely no way knowing if this id is the true
		 *  id.. soon this will all change, but for now, we needed something to test
		 *  on ;) -- TrueLight -- 21-11-2005 */
		if (p.ainew.tbt == AI_TRAIN) {
		} else {
			p.ainew.veh_id = Global._new_roadveh_id;
		}

		if (!p.ainew.veh_main_id.equals(VehicleID.getInvalid())) {
			Ai.AI_DoCommand(0, p.ainew.veh_id.id + (p.ainew.veh_main_id.id << 16), 0, Cmd.DC_EXEC, Cmd.CMD_CLONE_ORDER);

			p.ainew.state = AiState.START_VEHICLE;
			return;
		} else {
			p.ainew.veh_main_id = p.ainew.veh_id;
		}

		// Very handy for AI, goto depot.. but yeah, it needs to be activated ;)
		if (Global._patches.gotodepot.get()) {
			idx = 0;
			//order.type = OT_GOTO_DEPOT;
			//order.flags = OF_UNLOAD;
			//order.station = GetDepotByTile(p.ainew.depot_tile).index;
			Order order = new Order( Order.OT_GOTO_DEPOT, Order.OF_UNLOAD, Depot.GetDepotByTile(p.ainew.depot_tile).getIndex() );
			Ai.AI_DoCommand(0, p.ainew.veh_id.id + (idx << 16), Order.PackOrder(order), Cmd.DC_EXEC, Cmd.CMD_INSERT_ORDER);
		}

		{
			idx = 0;
			//order.type = OT_GOTO_STATION;
			//order.flags = 0;
			//order.station = _m[p.ainew.to_tile].m2;
			int flags = 0;
			if (p.ainew.tbt == AI_TRUCK && p.ainew.to_deliver)
				flags |= Order.OF_FULL_LOAD;
			//order.flags |= OF_FULL_LOAD;
			Order order = new Order( Order.OT_GOTO_STATION, flags, p.ainew.to_tile.M().m2 ); 
			Ai.AI_DoCommand(0, p.ainew.veh_id.id + (idx << 16), Order.PackOrder(order), Cmd.DC_EXEC, Cmd.CMD_INSERT_ORDER);
		}

		{
			idx = 0;
			//order.type = OT_GOTO_STATION;
			//order.flags = 0;
			//order.station = _m[p.ainew.from_tile].m2;
			int flags = 0;
			if (p.ainew.tbt == AI_TRUCK && p.ainew.from_deliver)
				flags |= Order.OF_FULL_LOAD;
			//order.flags |= OF_FULL_LOAD;
			Order order = new Order( Order.OT_GOTO_STATION, flags, p.ainew.from_tile.M().m2 ); 
			Ai.AI_DoCommand(0, p.ainew.veh_id.id + (idx << 16), Order.PackOrder(order), Cmd.DC_EXEC, Cmd.CMD_INSERT_ORDER);
		}

		// Start the engines!
		p.ainew.state = AiState.START_VEHICLE;
	}


	// Start the vehicle
	static void AiNew_State_StartVehicle(Player p)
	{
		assert(p.ainew.state == AiState.START_VEHICLE);

		// Skip the first order if it is a second vehicle
		//  This to make vehicles go different ways..
		if(0 != (p.ainew.cur_veh & 1))
			Ai.AI_DoCommand(0, p.ainew.veh_id.id, 0, Cmd.DC_EXEC, Cmd.CMD_SKIP_ORDER);

		// 3, 2, 1... go! (give START_STOP command ;))
		Ai.AI_DoCommand(0, p.ainew.veh_id.id, 0, Cmd.DC_EXEC, Cmd.CMD_START_STOP_ROADVEH);
		// Try to build an other vehicle (that function will stop building when needed)
		p.ainew.idle  = 10;
		p.ainew.state = AiState.BUILD_VEHICLE;
	}


	// Repays money
	static void AiNew_State_RepayMoney(Player p)
	{
		int i;
		for (i=0;i<AI_LOAN_REPAY;i++)
			Ai.AI_DoCommand(0, 0, 0, Cmd.DC_EXEC, Cmd.CMD_DECREASE_LOAN);
		p.ainew.state = AiState.ACTION_DONE;
	}


	static void AiNew_CheckVehicle(Player p, Vehicle v)
	{
		// When a vehicle is under the 6 months, we don't check for anything
		if (v.getAge() < 180) return;

		// When a vehicle is older then 1 year, it should make money...
		if (v.getAge() > 360) {
			// If both years together are not more than AI_MINIMUM_ROUTE_PROFIT,
			//  it is not worth the line I guess...
			if (v.getProfit_last_year() + v.getProfit_this_year() < AI_MINIMUM_ROUTE_PROFIT ||
					(v.getReliability() * 100 >> 16) < 40) {
				// There is a possibility that the route is fucked up...
				if (v.getCargo_days() > AI_VEHICLE_LOST_DAYS) {
					// The vehicle is lost.. check the route, or else, get the vehicle
					//  back to a depot
					// TODO: make this piece of code
				}


				// We are already sending him back
				if(0 != (AiNew_GetSpecialVehicleFlag(p, v) & AI_VEHICLEFLAG_SELL)) {
					if (v.getType() == Vehicle.VEH_Road && Depot.IsTileDepotType(v.getTile(), TransportType.Road) &&
							//(v.vehstatus&VS_STOPPED) 
							v.isStopped()) {
						// We are at the depot, sell the vehicle
						Ai.AI_DoCommand(0, v.index, 0, Cmd.DC_EXEC, Cmd.CMD_SELL_ROAD_VEH);
					}
					return;
				}

				if (!AiNew_SetSpecialVehicleFlag(p, v, AI_VEHICLEFLAG_SELL)) return;
				{
					int ret = 0;
					if (v.getType() == Vehicle.VEH_Road)
						ret = Ai.AI_DoCommand(0, v.index, 0, Cmd.DC_EXEC, Cmd.CMD_SEND_ROADVEH_TO_DEPOT);
					// This means we can not find a depot :s
					//				if (CmdFailed(ret))
				}
			}
		}
	}


	// Checks all vehicles if they are still valid and make money and stuff
	static void AiNew_State_CheckAllVehicles(Player p)
	{
		//Vehicle v;

		//FOR_ALL_VEHICLES(v)
		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();

			if (v.getType() == 0) continue;
			if (!v.getOwner().equals(p.getIndex())) continue;
			// Currently, we only know how to handle road-vehicles
			if (v.getType() != Vehicle.VEH_Road) continue;

			AiNew_CheckVehicle(p, v);
		}

		p.ainew.state = AiState.ACTION_DONE;
	}


	// Using the technique simular to the original AI
	//   Keeps things logical
	// It really should be in the same order as the AI_STATE's are!
	static final AiNew_StateFunction _ainew_state[] = {
			null,
			Trolly::AiNew_State_FirstTime,
			Trolly::AiNew_State_Nothing,
			Trolly::AiNew_State_WakeUp,
			Trolly::AiNew_State_LocateRoute,
			Trolly::AiNew_State_FindStation,
			Trolly::AiNew_State_FindPath,
			Trolly::AiNew_State_FindDepot,
			Trolly::AiNew_State_VerifyRoute,
			Trolly::AiNew_State_BuildStation,
			Trolly::AiNew_State_BuildPath,
			Trolly::AiNew_State_BuildDepot,
			Trolly::AiNew_State_BuildVehicle,
			Trolly::AiNew_State_GiveOrders,
			Trolly::AiNew_State_StartVehicle,
			Trolly::AiNew_State_RepayMoney,
			Trolly::AiNew_State_CheckAllVehicles,
			Trolly::AiNew_State_ActionDone,
			null,
	};

	static void AiNew_OnTick(Player p)
	{
		final AiNew_StateFunction func = _ainew_state[p.ainew.state.ordinal()];
		if (func != null)
			func.state(p);
	}


	static void AiNewDoGameLoop(Player p)
	{
		if (p.ainew.state == AiState.STARTUP) {
			// The AI just got alive!
			p.ainew.state = AiState.FIRST_TIME;
			p.ainew.tick = 0;

			// Only startup the AI
			return;
		}

		// We keep a ticker. We use it for competitor_speed
		p.ainew.tick++;

		// If we come here, we can do a tick.. do so!
		AiNew_OnTick(p);
	}












}


@FunctionalInterface
interface AiNew_StateFunction {
	void state(Player p);
}
