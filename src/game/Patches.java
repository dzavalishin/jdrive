package game;

public class Patches 
{
	boolean modified_catchment;	//different-size catchment areas
	boolean vehicle_speed = true;			// show vehicle speed
	boolean build_on_slopes = true;		// allow building on slopes
	boolean auto_pbs_placement = true;// automatic pbs signal placement
	boolean mammoth_trains = true;		// allow very long trains
	boolean join_stations;			// allow joining of train stations
	boolean full_load_any = true;			// new full load calculation, any cargo must be full
	boolean improved_load;			// improved loading algorithm
	byte station_spread = 12;		// amount a station may spread
	boolean inflation = true;					// disable inflation
	boolean selectgoods;       // only send the goods to station if a train has been there
	boolean longbridges;				// allow 100 tile long bridges
	boolean gotodepot = true;					// allow goto depot in orders
	boolean build_rawmaterial_ind = true;	 // allow building raw material industries
	boolean multiple_industry_per_town;	// allow many industries of the same type per town
	boolean same_industry_close;	// allow same type industries to be built close to each other
	int lost_train_days = 180;	// if a train doesn't switch order in this amount of days, a train is lost warning is shown
	byte order_review_system = 2;
	boolean train_income_warn; // if train is generating little income, show a warning
	boolean status_long_date;		// always show long date in status bar
	boolean signal_side = true;				// show signals on right side
	boolean show_finances;			// show finances at end of year
	boolean new_nonstop;				// ttdpatch compatible nonstop handling
	boolean roadveh_queue = true;			// buggy road vehicle queueing
	boolean autoscroll = true;				// scroll when moving mouse to the edge.
	byte errmsg_duration = 100;		// duration of error message
	byte snow_line_height = 7;	// a number 0-15 that configured snow line height
	boolean bribe = true;							// enable bribing the local authority
	boolean nonuniform_stations;// allow nonuniform train stations
	boolean always_small_airport = true; // always allow small airports
	boolean allow_municipal_airports; // allow town to build airports
	int municipal_airports_tax; // tax rate of municipal airports
	boolean realistic_acceleration; // realistic acceleration for trains
	boolean wagon_speed_limits; // enable wagon speed limits
	boolean forbid_90_deg; // forbid trains to make 90 deg turns
	boolean invisible_trees = true; // don't show trees when buildings are transparent
	boolean no_servicing_if_no_breakdowns; // dont send vehicles to depot when breakdowns are disabled
	boolean link_terraform_toolbar = true; // display terraform toolbar when displaying rail, road, water and airport toolbars
	boolean reverse_scroll; // Right-Click-Scrolling scrolls in the opposite direction

	byte toolbar_pos;			// position of toolbars, 0=left, 1=center, 2=right
	byte window_snap_radius = 6; // Windows snap at each other if closer than this

	int max_trains = 1000;				//max trains in game per player (these are 16bit because the unitnumber field can't hold more)
	int max_roadveh = 1000;				//max trucks in game per player
	int max_aircraft = 1000;			//max planes in game per player
	int max_ships = 1000;					//max ships in game per player

	boolean servint_ispercent;	// service intervals are in percents
	int servint_trains = 20;	// service interval for trains
	int servint_roadveh = 20;	// service interval for road vehicles
	int servint_aircraft = 20;// service interval for aircraft
	int servint_ships = 40;		// service interval for ships

	boolean autorenew = true;
	int autorenew_months = 10;
	long autorenew_money = 1000;

	byte pf_maxdepth = 16;				// maximum recursion depth when searching for a train route for new pathfinder
	int pf_maxlength = 512;		// maximum length when searching for a train route for new pathfinder


	boolean bridge_pillars = true;		// show bridge pillars for high bridges

	boolean ai_disable_veh_train;		// disable types for AI
	boolean ai_disable_veh_roadveh;		// disable types for AI
	boolean ai_disable_veh_aircraft;		// disable types for AI
	boolean ai_disable_veh_ship;		// disable types for AI
	int starting_date;		// starting date
	int ending_date;		// end of the game (just show highscore)
	int colored_news_date = 1920; // when does newspaper become colored?

	boolean keep_all_autosave;		// name the autosave in a different way.
	boolean autosave_on_exit;		// save an autosave when you quit the game, but do not ask "Do you really want to quit?"
	byte max_num_autosaves;		// controls how many autosavegames are made before the game starts to overwrite (names them 0 to max_num_autosaves - 1)
	boolean extra_dynamite = true;			// extra dynamite

	boolean never_expire_vehicles; // never expire vehicles
	byte extend_vehicle_life;	// extend vehicle life by this many years

	boolean auto_euro;						// automatically switch to euro in 2002
	boolean serviceathelipad = true;	// service helicopters at helipads automatically (no need to send to depot)
	boolean smooth_economy = true;		// smooth economy
	boolean allow_shares;			// allow the buying/selling of shares
	byte dist_local_authority;		// distance for town local authority, default 20

	byte wait_oneway_signal;	//waitingtime in days before a oneway signal
	byte wait_twoway_signal;	//waitingtime in days before a twoway signal

	int map_x = 8; // Size of map
	int map_y = 8;

	byte drag_signals_density = 5; // many signals density
	boolean ainew_active;  // Is the new AI active?
	boolean ai_in_multiplayer; // Do we allow AIs in multiplayer

	boolean aircraft_queueing = true; // Aircraft queueing patch
	int aircraft_speed_coeff = 1; // Coefficient of aircraft speed, based on Benben's patch

	/*
	 * New Path Finding
	 */
	boolean new_pathfinding_all = true; /* Use the newest pathfinding algorithm for all */

	/**
	 * The maximum amount of search nodes a single NPF run should take. This
	 * limit should make sure performance stays at acceptable levels at the cost
	 * of not being perfect anymore. This will probably be fixed in a more
	 * sophisticated way sometime soon
	 */
	int npf_max_search_nodes = 10000;

	int npf_rail_firstred_penalty = 10 * Npf.NPF_TILE_LENGTH; /* The penalty for when the first signal is red (and it is not an exit or combo signal) */
	int npf_rail_firstred_exit_penalty = 100 * Npf.NPF_TILE_LENGTH; /* The penalty for when the first signal is red (and it is an exit or combo signal) */
	int npf_rail_lastred_penalty = 10 * Npf.NPF_TILE_LENGTH; /* The penalty for when the last signal is red */
	int npf_rail_station_penalty = 1 * Npf.NPF_TILE_LENGTH; /* The penalty for station tiles */
	int npf_rail_slope_penalty = 1 * Npf.NPF_TILE_LENGTH; /* The penalty for sloping upwards */
	int npf_rail_curve_penalty = 1; /* The penalty for curves */
	int npf_rail_depot_reverse_penalty = 50 * Npf.NPF_TILE_LENGTH; /* The penalty for reversing in depots */
	int npf_buoy_penalty = 1 * Npf.NPF_TILE_LENGTH; /* The penalty for going over (through) a buoy */
	int npf_water_curve_penalty = Npf.NPF_TILE_LENGTH/4; /* The penalty for curves */
	int npf_road_curve_penalty = 1; /* The penalty for curves */
 	int npf_crossing_penalty = 2 * Npf.NPF_TILE_LENGTH; /* The penalty for level crossings */

	boolean population_in_label = true; // Show the population of a town in his label?
	byte day_length = 1;		// Multiplyer for length of one day

}
