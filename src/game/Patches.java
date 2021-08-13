package game;

public class Patches 
{
	boolean modified_catchment;	//different-size catchment areas
	boolean vehicle_speed;			// show vehicle speed
	boolean build_on_slopes;		// allow building on slopes
	boolean auto_pbs_placement;// automatic pbs signal placement
	boolean mammoth_trains;		// allow very long trains
	boolean join_stations;			// allow joining of train stations
	boolean full_load_any;			// new full load calculation, any cargo must be full
	boolean improved_load;			// improved loading algorithm
	byte station_spread;		// amount a station may spread
	boolean inflation;					// disable inflation
	boolean selectgoods;       // only send the goods to station if a train has been there
	boolean longbridges;				// allow 100 tile long bridges
	boolean gotodepot;					// allow goto depot in orders
	boolean build_rawmaterial_ind;	 // allow building raw material industries
	boolean multiple_industry_per_town;	// allow many industries of the same type per town
	boolean same_industry_close;	// allow same type industries to be built close to each other
	int lost_train_days;	// if a train doesn't switch order in this amount of days, a train is lost warning is shown
	byte order_review_system;
	boolean train_income_warn; // if train is generating little income, show a warning
	boolean status_long_date;		// always show long date in status bar
	boolean signal_side;				// show signals on right side
	boolean show_finances;			// show finances at end of year
	boolean new_nonstop;				// ttdpatch compatible nonstop handling
	boolean roadveh_queue;			// buggy road vehicle queueing
	boolean autoscroll = true;				// scroll when moving mouse to the edge.
	byte errmsg_duration;		// duration of error message
	byte snow_line_height;	// a number 0-15 that configured snow line height
	boolean bribe;							// enable bribing the local authority
	boolean nonuniform_stations;// allow nonuniform train stations
	boolean always_small_airport; // always allow small airports
	boolean allow_municipal_airports; // allow town to build airports
	int municipal_airports_tax; // tax rate of municipal airports
	boolean realistic_acceleration; // realistic acceleration for trains
	boolean wagon_speed_limits; // enable wagon speed limits
	boolean forbid_90_deg; // forbid trains to make 90 deg turns
	boolean invisible_trees; // don't show trees when buildings are transparent
	boolean no_servicing_if_no_breakdowns; // dont send vehicles to depot when breakdowns are disabled
	boolean link_terraform_toolbar; // display terraform toolbar when displaying rail, road, water and airport toolbars
	boolean reverse_scroll; // Right-Click-Scrolling scrolls in the opposite direction

	byte toolbar_pos;			// position of toolbars, 0=left, 1=center, 2=right
	byte window_snap_radius; // Windows snap at each other if closer than this

	UnitID max_trains;				//max trains in game per player (these are 16bit because the unitnumber field can't hold more)
	UnitID max_roadveh;				//max trucks in game per player
	UnitID max_aircraft;			//max planes in game per player
	UnitID max_ships;					//max ships in game per player

	boolean servint_ispercent;	// service intervals are in percents
	int servint_trains;	// service interval for trains
	int servint_roadveh;	// service interval for road vehicles
	int servint_aircraft;// service interval for aircraft
	int servint_ships;		// service interval for ships

	boolean autorenew;
	int autorenew_months;
	long autorenew_money;

	byte pf_maxdepth;				// maximum recursion depth when searching for a train route for new pathfinder
	int pf_maxlength;		// maximum length when searching for a train route for new pathfinder


	boolean bridge_pillars;		// show bridge pillars for high bridges

	boolean ai_disable_veh_train;		// disable types for AI
	boolean ai_disable_veh_roadveh;		// disable types for AI
	boolean ai_disable_veh_aircraft;		// disable types for AI
	boolean ai_disable_veh_ship;		// disable types for AI
	int starting_date;		// starting date
	int ending_date;		// end of the game (just show highscore)
	int colored_news_date; // when does newspaper become colored?

	boolean keep_all_autosave;		// name the autosave in a different way.
	boolean autosave_on_exit;		// save an autosave when you quit the game, but do not ask "Do you really want to quit?"
	byte max_num_autosaves;		// controls how many autosavegames are made before the game starts to overwrite (names them 0 to max_num_autosaves - 1)
	boolean extra_dynamite;			// extra dynamite

	boolean never_expire_vehicles; // never expire vehicles
	byte extend_vehicle_life;	// extend vehicle life by this many years

	boolean auto_euro;						// automatically switch to euro in 2002
	boolean serviceathelipad;	// service helicopters at helipads automatically (no need to send to depot)
	boolean smooth_economy;		// smooth economy
	boolean allow_shares;			// allow the buying/selling of shares
	byte dist_local_authority;		// distance for town local authority, default 20

	byte wait_oneway_signal;	//waitingtime in days before a oneway signal
	byte wait_twoway_signal;	//waitingtime in days before a twoway signal

	int map_x = 8; // Size of map
	int map_y = 8;

	byte drag_signals_density; // many signals density
	boolean ainew_active;  // Is the new AI active?
	boolean ai_in_multiplayer; // Do we allow AIs in multiplayer

	boolean aircraft_queueing; // Aircraft queueing patch
	int aircraft_speed_coeff; // Coefficient of aircraft speed, based on Benben's patch

	/*
	 * New Path Finding
	 */
	boolean new_pathfinding_all; /* Use the newest pathfinding algorithm for all */

	/**
	 * The maximum amount of search nodes a single NPF run should take. This
	 * limit should make sure performance stays at acceptable levels at the cost
	 * of not being perfect anymore. This will probably be fixed in a more
	 * sophisticated way sometime soon
	 */
	int npf_max_search_nodes;

	int npf_rail_firstred_penalty; /* The penalty for when the first signal is red (and it is not an exit or combo signal) */
	int npf_rail_firstred_exit_penalty; /* The penalty for when the first signal is red (and it is an exit or combo signal) */
	int npf_rail_lastred_penalty; /* The penalty for when the last signal is red */
	int npf_rail_station_penalty; /* The penalty for station tiles */
	int npf_rail_slope_penalty; /* The penalty for sloping upwards */
	int npf_rail_curve_penalty; /* The penalty for curves */
	int npf_rail_depot_reverse_penalty; /* The penalty for reversing in depots */
	int npf_buoy_penalty; /* The penalty for going over (through) a buoy */
	int npf_water_curve_penalty; /* The penalty for curves */
	int npf_road_curve_penalty; /* The penalty for curves */
 	int npf_crossing_penalty; /* The penalty for level crossings */

	boolean population_in_label; // Show the population of a town in his label?
	byte day_length;		// Multiplyer for length of one day

}
