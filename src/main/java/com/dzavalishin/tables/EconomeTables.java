package com.dzavalishin.tables;

import com.dzavalishin.struct.ScoreInfo;

public class EconomeTables {

	public static final int SCORE_VEHICLES = 0;
	public static final int SCORE_STATIONS = 1;
	public static final int SCORE_MIN_PROFIT = 2;
	public static final int SCORE_MIN_INCOME = 3;
	public static final int SCORE_MAX_INCOME = 4;
	public static final int SCORE_DELIVERED = 5;
	public static final int SCORE_CARGO = 6;
	public static final int SCORE_MONEY = 7;
	public static final int SCORE_LOAN = 8;
	public static final int SCORE_TOTAL = 9; // This must always be the last entry

	public static final int NUM_SCORE = 10; // How many scores are there..

	public static final int SCORE_MAX = 1000; 	// The max score that can be in the performance history
	//  the scores together of public static final int SCORE_info is allowed to be more!
	
	
	// Score info
	public static final ScoreInfo _score_info[] = {
			new ScoreInfo( SCORE_VEHICLES,		120, 		100),
			new ScoreInfo( SCORE_STATIONS,		80, 		100),
			new ScoreInfo( SCORE_MIN_PROFIT,	10000,		100),
			new ScoreInfo( SCORE_MIN_INCOME,	50000,		50),
			new ScoreInfo( SCORE_MAX_INCOME,	100000,		100),
			new ScoreInfo( SCORE_DELIVERED,		40000, 		400),
			new ScoreInfo( SCORE_CARGO,			8,			50),
			new ScoreInfo( SCORE_MONEY,			10000000,	50),
			new ScoreInfo( SCORE_LOAN,			250000,		50),
			new ScoreInfo( SCORE_TOTAL,			0,			0)
	};
	
	
	
	protected static final byte[] _price_category = {
			0, 2, 2, 2, 2, 2, 2, 2,
			2, 2, 2, 2, 2, 2, 2, 2,
			2, 2, 2, 2, 2, 2, 2, 2,
			2, 2, 2, 2, 2, 2, 2, 2,
			2, 2, 2, 2, 2, 2, 2, 2,
			2, 2, 1, 1, 1, 1, 1, 1,
			2,
	};

	protected static final int _price_base[] = {
			100,		// station_value
			100,		// build_rail
			95,			// build_road
			65,			// build_signals
			275,		// build_bridge
			600,		// build_train_depot
			500,		// build_road_depot
			700,		// build_ship_depot
			450,		// build_tunnel
			200,		// train_station_track
			180,		// train_station_length
			600,		// build_airport
			200,		// build_bus_station
			200,		// build_truck_station
			350,		// build_dock
			400000,	// build_railvehicle
			2000,		// build_railwagon
			700000,	// aircraft_base
			14000,	// roadveh_base
			65000,	// ship_base
			20,			// build_trees
			250,		// terraform
			20,			// clear_1
			40,			// purchase_land
			200,		// clear_2
			500,		// clear_3
			20,			// remove_trees
			-70,		// remove_rail
			10,			// remove_signals
			50,			// clear_bridge
			80,			// remove_train_depot
			80,			// remove_road_depot
			90,			// remove_ship_depot
			30,			// clear_tunnel
			10000,	// clear_water
			50,			// remove_rail_station
			30,			// remove_airport
			50,			// remove_bus_station
			50,			// remove_truck_station
			55,			// remove_dock
			1600,		// remove_house
			40,			// remove_road
			5600,		// running_rail[0] railroad
			5200,		// running_rail[1] monorail
			4800,		// running_rail[2] maglev
			9600,		// aircraft_running
			1600,		// roadveh_running
			5600,		// ship_running
			1000000, // build_industry
	};
	
}
