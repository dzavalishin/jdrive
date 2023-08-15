package com.dzavalishin.ai;

import com.dzavalishin.game.Global;
import com.dzavalishin.game.TileIndex;

public interface AiConst {
	/*
	 * These defines can be altered to change the behavoir of the AI
	 *
	 * WARNING:
	 *   This can also alter the AI in a negative way. I will never claim these settings
	 *   are perfect, but don't change them if you don't know what the effect is.
	 */

	// How many times it the H multiplied. The higher, the more it will go straight to the
	//   end point. The lower, how more it will find the route with the lowest cost.
	//   also: the lower, the longer it takes before route is calculated..
	public static final int  AI_PATHFINDER_H_MULTIPLER = 100;

	// How many loops may AyStar do before it stops
	//   0 = infinite
	public static final int  AI_PATHFINDER_LOOPS_PER_TICK = 5;

	// How long may the AI search for one route?
	//   0 = infinite
	// This number is the number of tiles tested.
	//  It takes (AI_PATHFINDER_MAX_SEARCH_NODES / AI_PATHFINDER_LOOPS_PER_TICK) ticks
	//  to get here.. with 5000 / 10 = 500. 500 / 74 (one day) = 8 days till it aborts
	//   (that is: if the AI is on VERY FAST! :p
	public static final int  AI_PATHFINDER_MAX_SEARCH_NODES = 5000;

	// If you enable this, the AI is not allowed to make 90degree turns
	//public static final int  AI_PATHFINDER_NO_90DEGREES_TURN

	// Below are defines for the g-calculation

	// Standard penalty given to a tile
	public static final int  AI_PATHFINDER_PENALTY = 150;
	// The penalty given to a tile that is going up
	public static final int  AI_PATHFINDER_TILE_GOES_UP_PENALTY = 450;
	// The penalty given to a tile which would have to use fundation
	public static final int  AI_PATHFINDER_FOUNDATION_PENALTY = 100;
	// Changing direction is a penalty, to prevent curved ways (with that: slow ways)
	public static final int  AI_PATHFINDER_DIRECTION_CHANGE_PENALTY = 200;
	// Same penalty, only for when road already exists
	public static final int  AI_PATHFINDER_DIRECTION_CHANGE_ON_EXISTING_ROAD_PENALTY = 50;
	// A diagonal track cost the same as a straigh, but a diagonal is faster... so give
	//  a bonus for using diagonal track
	//#ifdef AI_PATHFINDER_NO_90DEGREES_TURN
	//public static final int  AI_PATHFINDER_DIAGONAL_BONUS 95
	//#else
	public static final int  AI_PATHFINDER_DIAGONAL_BONUS = 75;
	//#endif
	// If a roadblock already exists, it gets a bonus
	public static final int  AI_PATHFINDER_ROAD_ALREADY_EXISTS_BONUS = 140;
	// To prevent 3 direction changes in 3 tiles, this penalty is given in such situation
	public static final int  AI_PATHFINDER_CURVE_PENALTY = 200;

	// Penalty a bridge gets per length
	public static final int  AI_PATHFINDER_BRIDGE_PENALTY = 180;
	// The penalty for a bridge going up
	public static final int  AI_PATHFINDER_BRIDGE_GOES_UP_PENALTY = 1000;

	// Tunnels are expensive...
	//  Because of that, every tile the cost is increased with 1/8th of his value
	//  This is also true if you are building a tunnel yourself
	public static final int  AI_PATHFINDER_TUNNEL_PENALTY = 350;

	/*
	 * Ai_New defines
	 */

	// How long may we search cities and industry for a new route?
	public static final int  AI_LOCATE_ROUTE_MAX_COUNTER = 200;

	// How many days must there be between building the first station and the second station
	//  within one city. This number is in days and should be more than 4 months.
	public static final int  AI_CHECKCITY_DATE_BETWEEN = 180;

	// How many cargo is needed for one station in a city?
	public static final int  AI_CHECKCITY_CARGO_PER_STATION = 60;
	// How much cargo must there not be used in a city before we can build a new station?
	public static final int  AI_CHECKCITY_NEEDED_CARGO = 50;
	// When there is already a station which takes the same good and the rating of that
	//  city is higher then this numer, we are not going to attempt to build anything
	//  there
	public static final int  AI_CHECKCITY_CARGO_RATING = 50;
	// But, there is a chance of 1 out of this number, that we do ;)
	public static final int  AI_CHECKCITY_CARGO_RATING_CHANCE = 5;
	// If a city is too small to contain a station, there is a small chance
	//  that we still do so.. just to make the city bigger!
	public static final int  AI_CHECKCITY_CITY_CHANCE = 5;

	// This number indicates for every unit of cargo, how many tiles two stations maybe be away
	//  from eachother. In other words: if we have 120 units of cargo in one station, and 120 units
	//  of the cargo in the other station, both stations can be 96 units away from eachother, if the
	//  next number is 0.4.
	public static final double  AI_LOCATEROUTE_BUS_CARGO_DISTANCE = 0.4;
	public static final double  AI_LOCATEROUTE_TRUCK_CARGO_DISTANCE = 0.7;
	// In whole tiles, the minimum distance for a truck route
	public static final int  AI_LOCATEROUTE_TRUCK_MIN_DISTANCE = 30;

	// The amount of tiles in a square from -X to +X that is scanned for a station spot
	//  (so if this number is 10, 20x20 = 400 tiles are scanned for _the_ perfect spot
	// Safe values are between 15 and 5
	public static final int  AI_FINDSTATION_TILE_RANGE = 10;

	// Building on normal speed goes very fast. Idle this amount of ticks between every
	//  building part. It is calculated like this: (4 - competitor_speed) * num + 1
	//  where competitor_speed is between 0 (very slow) to 4 (very fast)
	public static final int  AI_BUILDPATH_PAUSE = 10;

	// Minimum % of reliabilty a vehicle has to have before the AI buys it
	public static final int  AI_VEHICLE_MIN_RELIABILTY = 60;

	// The minimum amount of money a player should always have
	public static final int  AI_MINIMUM_MONEY = 15000;

	// If the most cheap route is build, how much is it going to cost..
	// This is to prevent the AI from trying to build a route which can not be paid for
	public static final int  AI_MINIMUM_BUS_ROUTE_MONEY = 25000;
	public static final int  AI_MINIMUM_TRUCK_ROUTE_MONEY = 35000;

	// The minimum amount of money before we are going to repay any money
	public static final int  AI_MINIMUM_LOAN_REPAY_MONEY = 40000;
	// How many repays do we do if we have enough money to do so?
	//  Every repay is 10000
	public static final int  AI_LOAN_REPAY = 2;
	// How much income must we have before paying back a loan? Month-based (and looked at the last month)
	public static final int  AI_MINIMUM_INCOME_FOR_LOAN = 7000;

	// If there is <num> time as much cargo in the station then the vehicle can handle
	//  reuse the station instead of building a new one!
	public static final int  AI_STATION_REUSE_MULTIPLER = 2;

	// No more than this amount of vehicles per station..
	public static final int  AI_CHECK_MAX_VEHICLE_PER_STATION = 10;

	// How many thick between building 2 vehicles
	public static final int  AI_BUILD_VEHICLE_TIME_BETWEEN = Global.DAY_TICKS;

	// How many days must there between vehicle checks
	//  The more often, the less non-money-making lines there will be
	//   but the unfair it may seem to a human player
	public static final int  AI_DAYS_BETWEEN_VEHICLE_CHECKS = 30;

	// How money profit does a vehicle needs to make to stay in order
	//  This is the profit of this year + profit of last year
	//  But also for vehicles that are just one year old. In other words:
	//   Vehicles of 2 years do easier meet this setting then vehicles
	//   of one year. This is a very good thing. New vehicles are filtered,
	//   while old vehicles stay longer, because we do get less in return.
	public static final int  AI_MINIMUM_ROUTE_PROFIT = 1000;

	// A vehicle is considered lost when he his cargo is more than 180 days old
	public static final int  AI_VEHICLE_LOST_DAYS = 180;

	// How many times may the AI try to find a route before it gives up
	public static final int  AI_MAX_TRIES_FOR_SAME_ROUTE = 8;

	/*
	 * End of defines
	 */






	// Used for from_type/to_type
	//enum {
	public static final int AI_NO_TYPE = 0;
	public static final int AI_CITY = 1;
	public static final int AI_INDUSTRY = 2;
	//};

	// Flags for in the vehicle
	//enum {
	public static final int AI_VEHICLEFLAG_SELL = 1;
	// Remember, flags must be in power of 2
	//};

	public static final int  AI_NO_CARGO = 0xFF; // Means that there is no cargo defined yet (used for industry)
	public static final int  AI_NEED_CARGO = 0xFE; // Used when the AI needs to find out a cargo for the route

	public static final int  AI_PATHFINDER_NO_DIRECTION = -1;

	// Flags used in user_data
	public static final int  AI_PATHFINDER_FLAG_BRIDGE = 1;
	public static final int  AI_PATHFINDER_FLAG_TUNNEL = 2;

	public static TileIndex  AI_STATION_RANGE() { return TileIndex.TileXY(Global.MapMaxX(), Global.MapMaxY()); }


	public static final int AI_MAX_SPECIAL_VEHICLES = 100;



	// This stops 90degrees curves
	static final int _illegal_curves[] = {
			255, 255, // Horz and vert, don't have the effect
			5, // upleft and upright are not valid
			4, // downright and downleft are not valid
			2, // downleft and upleft are not valid
			3, // upright and downright are not valid
	};

	// Used for tbt (train/bus/truck)
	//enum {
	public static final int AI_TRAIN = 0;
	public static final int AI_BUS = 1;
	public static final int AI_TRUCK = 2;
	//};
	//enum {
	static int BRIDGE_NO_FOUNDATION = 1 << 0 | 1 << 3 | 1 << 6 | 1 << 9 | 1 << 12;
	//};


}
