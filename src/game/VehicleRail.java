package game;

public class VehicleRail extends VehicleChild {
	int last_speed;		// NOSAVE: only used in UI
	int crash_anim_pos;
	int days_since_order_progr;

	// cached values, recalculated on load and each time a vehicle is added to/removed from the consist.
	int cached_max_speed;  // max speed of the consist. (minimum of the max speed of all vehicles in the consist)
	int cached_power;      // total power of the consist.
	int cached_veh_length;  // length of this vehicle in units of 1/8 of normal length, cached because this can be set by a callback
	int cached_total_length; ///< Length of the whole train, valid only for first engine.

	// cached values, recalculated when the cargo on a train changes (in addition to the conditions above)
	int cached_weight;     // total weight of the consist.
	int cached_veh_weight; // weight of the vehicle.
	/**
	 * Position/type of visual effect.
	 * bit 0 - 3 = position of effect relative to vehicle. (0 = front, 8 = centre, 15 = rear)
	 * bit 4 - 5 = type of effect. (0 = default for engine class, 1 = steam, 2 = diesel, 3 = electric)
	 * bit     6 = disable visual effect.
	 * bit     7 = disable powered wagons.
	 */
	int cached_vis_effect;

	// NOSAVE: for wagon override - id of the first engine in train
	// 0xffff == not in train
	EngineID first_engine;

	int track;
	int force_proceed;
	int railtype;

	int flags;

	int pbs_status;
	TileIndex pbs_end_tile;
	/*Trackdir*/ int pbs_end_trackdir;

	/**
	 * stuff to figure out how long a train should be. Used by autoreplace
	 * first byte holds the length of the shortest station. Updated each time order 0 is reached
	 * last byte is the shortest station reached this round though the orders. It can be invalidated by
	 *   skip station and alike by setting it to 0. That way we will ensure that a complete loop is used to find the shortest station
	 */
	int [] shortest_platform = new int[2];

	// Link between the two ends of a multiheaded engine
	Vehicle other_multiheaded_part;


	static public final int VRF_REVERSING = 0;

	// used to calculate if train is going up or down
	static public final int VRF_GOINGUP   = 1;
	static public final int VRF_GOINGDOWN = 2;

	// used to store if a wagon is powered or not
	static public final int VRF_POWEREDWAGON = 3;

	@Override
	void clear() 
	{
		last_speed= 		
		crash_anim_pos = 
		days_since_order_progr = 
		cached_max_speed = 
		cached_power =     
		cached_veh_length = 
		cached_total_length = 
		cached_weight =      
		cached_veh_weight =  
		cached_vis_effect = 0;


		track =
		force_proceed =
		railtype =
		flags =
		pbs_status = 0;
		
		pbs_end_tile = null;
		shortest_platform = new int[2];
		other_multiheaded_part = null;
		first_engine = null;
	}


}
