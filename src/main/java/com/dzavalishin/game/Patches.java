package com.dzavalishin.game;

import com.dzavalishin.tables.BooleanPatchVariable;
import com.dzavalishin.tables.IntegerPatchVariable;

@SuppressWarnings("CanBeFinal")
public class Patches
{
	public boolean modified_catchment;	//different-size catchment areas
	public boolean vehicle_speed = true;			// show vehicle speed
	public boolean build_on_slopes = true;		// allow building on slopes
	public boolean auto_pbs_placement = true;// automatic pbs signal placement
	public boolean mammoth_trains = true;		// allow very long trains
	public boolean join_stations;			// allow joining of train stations
	public BooleanPatchVariable full_load_any = new BooleanPatchVariable(true);			// new full load calculation, any cargo must be full
	public boolean improved_load;			// improved loading algorithm
	public byte station_spread = 12;		// amount a station may spread
	public BooleanPatchVariable inflation = new BooleanPatchVariable(true);					// disable inflation
	public boolean selectgoods;       // only send the goods to station if a train has been there
	public boolean longbridges;				// allow 100 tile long bridges
	public BooleanPatchVariable gotodepot = new BooleanPatchVariable(true);					// allow goto depot in orders
	public BooleanPatchVariable build_rawmaterial_ind = new BooleanPatchVariable(true);	 // allow building raw material industries
	public boolean multiple_industry_per_town;	// allow many industries of the same type per town
	public boolean same_industry_close;	// allow same type industries to be built close to each other
	public int lost_train_days = 180;	// if a train doesn't switch order in this amount of days, a train is lost warning is shown
	public byte order_review_system = 2;
	public boolean train_income_warn; // if train is generating little income, show a warning
	//public boolean status_long_date;		// always show long date in status bar
	public BooleanPatchVariable status_long_date = new BooleanPatchVariable();		// always show long date in status bar
	public boolean signal_side = true;				// show signals on right side
	public BooleanPatchVariable show_finances = new BooleanPatchVariable();			// show finances at end of year
	public boolean new_nonstop;				// ttdpatch compatible nonstop handling
	public boolean roadveh_queue = true;			// buggy road vehicle queueing
	public boolean autoscroll = true;				// scroll when moving mouse to the edge.
	public IntegerPatchVariable errmsg_duration = new IntegerPatchVariable(20);		// duration of error message
	public byte snow_line_height = 7;	// a number 0-15 that configured snow line height
	public boolean bribe = true;							// enable bribing the local authority
	public boolean nonuniform_stations;// allow nonuniform train stations
	public boolean always_small_airport = true; // always allow small airports
	public BooleanPatchVariable allow_municipal_airports = new BooleanPatchVariable(); // allow town to build airports
	public int municipal_airports_tax; // tax rate of municipal airports
	public boolean realistic_acceleration = true; // realistic acceleration for trains
	public boolean wagon_speed_limits; // enable wagon speed limits
	public boolean forbid_90_deg; // forbid trains to make 90 deg turns
	public boolean invisible_trees = true; // don't show trees when buildings are transparent
	public boolean no_servicing_if_no_breakdowns; // dont send vehicles to depot when breakdowns are disabled
	public boolean link_terraform_toolbar = true; // display terraform toolbar when displaying rail, road, water and airport toolbars
	//public boolean reverse_scroll = true; // Right-Click-Scrolling scrolls in the opposite direction

	public byte toolbar_pos;			// position of toolbars, 0=left, 1=center, 2=right
	public IntegerPatchVariable window_snap_radius = new IntegerPatchVariable(6); // Windows snap at each other if closer than this

	public int max_trains = 1000;				//max trains in game per player (these are 16bit because the unitnumber field can't hold more)
	public int max_roadveh = 1000;				//max trucks in game per player
	public int max_aircraft = 1000;			//max planes in game per player
	public int max_ships = 1000;					//max ships in game per player

	public boolean servint_ispercent;	// service intervals are in percents
	public int servint_trains = 20;	// service interval for trains
	public int servint_roadveh = 20;	// service interval for road vehicles
	public int servint_aircraft = 20;// service interval for aircraft
	public int servint_ships = 40;		// service interval for ships

	public BooleanPatchVariable autorenew = new BooleanPatchVariable(true);
	public int autorenew_months = 10;
	public long autorenew_money = 1000;

	public byte pf_maxdepth = 16;				// maximum recursion depth when searching for a train route for new pathfinder
	public int pf_maxlength = 512;		// maximum length when searching for a train route for new pathfinder


	public boolean bridge_pillars = true;		// show bridge pillars for high bridges

	public BooleanPatchVariable ai_disable_veh_train = new BooleanPatchVariable();		// disable types for AI
	public BooleanPatchVariable ai_disable_veh_roadveh = new BooleanPatchVariable();		// disable types for AI
	public BooleanPatchVariable ai_disable_veh_aircraft = new BooleanPatchVariable();		// disable types for AI
	public BooleanPatchVariable ai_disable_veh_ship = new BooleanPatchVariable();		// disable types for AI
	public int starting_date;		// starting date
	public int ending_date = 2051;		// end of the game (just show highscore)
	public int colored_news_date = 1980; // when does newspaper become colored?

	public boolean keep_all_autosave;		// name the autosave in a different way.
	public boolean autosave_on_exit = true;		// save an autosave when you quit the game, but do not ask "Do you really want to quit?"
	public byte max_num_autosaves = 10;		// controls how many autosavegames are made before the game starts to overwrite (names them 0 to max_num_autosaves - 1)
	public boolean extra_dynamite = true;			// extra dynamite

	public BooleanPatchVariable never_expire_vehicles = new BooleanPatchVariable(); // never expire vehicles
	public byte extend_vehicle_life;	// extend vehicle life by this many years

	public boolean auto_euro;						// automatically switch to euro in 2002
	public BooleanPatchVariable serviceathelipad = new BooleanPatchVariable(true);	// service helicopters at helipads automatically (no need to send to depot)
	public BooleanPatchVariable smooth_economy = new BooleanPatchVariable(true);		// smooth economy
	public boolean allow_shares;			// allow the buying/selling of shares
	public byte dist_local_authority;		// distance for town local authority, default 20

	public byte wait_oneway_signal;	//waitingtime in days before a oneway signal
	public byte wait_twoway_signal;	//waitingtime in days before a twoway signal

	public int map_x = 8; // Size of map
	public int map_y = 8;

	public byte drag_signals_density = 5; // many signals density
	public boolean ainew_active;  // Is the new AI active?
	public boolean ai_in_multiplayer; // Do we allow AIs in multiplayer

	public boolean aircraft_queueing = true; // Aircraft queueing patch
	public int aircraft_speed_coeff = 8; // Coefficient of aircraft speed, based on Benben's patch

	/*
	 * New Path Finding
	 */
	public boolean new_pathfinding_all = true; /* Use the newest pathfinding algorithm for all */

	/**
	 * The maximum amount of search nodes a single NPF run should take. This
	 * limit should make sure performance stays at acceptable levels at the cost
	 * of not being perfect anymore. This will probably be fixed in a more
	 * sophisticated way sometime soon
	 */
	public int npf_max_search_nodes = 10000;

	public int npf_rail_firstred_penalty = 10 * Npf.NPF_TILE_LENGTH; /* The penalty for when the first signal is red (and it is not an exit or combo signal) */
	public int npf_rail_firstred_exit_penalty = 100 * Npf.NPF_TILE_LENGTH; /* The penalty for when the first signal is red (and it is an exit or combo signal) */
	public int npf_rail_lastred_penalty = 10 * Npf.NPF_TILE_LENGTH; /* The penalty for when the last signal is red */
	public int npf_rail_station_penalty = 1 * Npf.NPF_TILE_LENGTH; /* The penalty for station tiles */
	public int npf_rail_slope_penalty = 1 * Npf.NPF_TILE_LENGTH; /* The penalty for sloping upwards */
	public int npf_rail_curve_penalty = 1; /* The penalty for curves */
	public int npf_rail_depot_reverse_penalty = 50 * Npf.NPF_TILE_LENGTH; /* The penalty for reversing in depots */
	public int npf_buoy_penalty = 1 * Npf.NPF_TILE_LENGTH; /* The penalty for going over (through) a buoy */
	public int npf_water_curve_penalty = Npf.NPF_TILE_LENGTH/4; /* The penalty for curves */
	public int npf_road_curve_penalty = 1; /* The penalty for curves */
 	public int npf_crossing_penalty = 2 * Npf.NPF_TILE_LENGTH; /* The penalty for level crossings */

	public boolean population_in_label = true; // Show the population of a town in his label?
	public byte day_length = 1;		// Multiplyer for length of one day

}
