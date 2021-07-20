package game;

public abstract class Vehicle implements IPoolItem 
{
	static public final int INVALID_VEHICLE = -1; //0xFFFF; // TODO -1?
	private static final int INVALID_COORD = -0x8000;


	private static int GEN_HASH(int x, int y) { return (((x & 0x1F80)>>7) + ((y & 0xFC0))); }
	static VehicleID _vehicle_position_hash[] = new VehicleID[0x1000];

	byte type;	// type, ie roadven,train,ship,aircraft,special
	byte subtype;     // subtype (Filled with values from EffectVehicles or TrainSubTypes)

	VehicleID index;	// NOSAVE: Index in vehicle array

	Vehicle next;		// next
	Vehicle first;   // NOSAVE: pointer to the first vehicle in the chain
	Vehicle depot_list;	//NOSAVE: linked list to tell what vehicles entered a depot during the last tick. Used by autoreplace

	StringID string_id; // Displayed string

	UnitID unitnumber;	// unit number, for display purposes only
	PlayerID owner;				// which player owns the vehicle?

	TileIndex tile;		// Current tile index
	TileIndex dest_tile; // Heading for this tile

	int x_pos;			// coordinates
	int y_pos;
	int z_pos;		// Was byte, changed for aircraft queueing
	byte direction;		// facing

	byte spritenum; // currently displayed sprite index
	// 0xfd == custom sprite, 0xfe == custom second head sprite
	// 0xff == reserved for another custom sprite
	int cur_image; // sprite number for this vehicle
	byte sprite_width;// width of vehicle sprite
	byte sprite_height;// height of vehicle sprite
	byte z_height;		// z-height of vehicle sprite
	int x_offs;			// x offset for vehicle sprite
	int y_offs;			// y offset for vehicle sprite

	EngineID engine_type;

	// for randomized variational spritegroups
	// bitmask used to resolve them; parts of it get reseeded when triggers
	// of corresponding spritegroups get matched
	byte random_bits;
	byte waiting_triggers; // triggers to be yet matched

	int max_speed;	// maximum speed
	int cur_speed;	// current speed
	byte subspeed;		// fractional speed
	int acceleration; // used by train & aircraft
	int progress;

	byte vehstatus;		// Status
	int last_station_visited;

	byte cargo_type;	// type of cargo this vehicle is carrying
	byte cargo_days; // how many days have the pieces been in transit
	int cargo_source;// source of cargo
	int cargo_cap;	// total capacity
	int cargo_count;// how many pieces are used

	byte day_counter; // increased by one for each day
	byte tick_counter;// increased by one for each tick

	/* Begin Order-stuff */
	Order current_order;     //! The current order (+ status, like: loading)
	OrderID cur_order_index; //! The index to the current order

	Order orders;           //! Pointer to the first order for this vehicle
	OrderID num_orders;      //! How many orders there are in the list

	Vehicle next_shared;    //! If not null, this points to the next vehicle that shared the order
	Vehicle prev_shared;    //! If not null, this points to the prev vehicle that shared the order
	/* End Order-stuff */

	// Boundaries for the current position in the world and a next hash link.
	// NOSAVE: All of those can be updated with VehiclePositionChanged()
	int left_coord;
	int top_coord;
	int right_coord;
	int bottom_coord;
	VehicleID next_hash;

	// Related to age and service time
	int age;				// Age in days
	int max_age;		// Maximum age
	int date_of_last_service;
	int service_interval;
	int reliability;
	int reliability_spd_dec;
	byte breakdown_ctr;
	byte breakdown_delay;
	byte breakdowns_since_last_service;
	byte breakdown_chance;
	byte build_year;

	boolean leave_depot_instantly;	// NOSAVE: stores if the vehicle needs to leave the depot it just entered. Used by autoreplace

	int load_unload_time_rem;

	int profit_this_year;
	int profit_last_year;
	int value;

	// Current position in a vehicle queue - can only belong to one queue at a time
	VQueueItem queue_item;

	/*
	union {
		VehicleRail rail;
		VehicleAir air;
		VehicleRoad road;
		VehicleSpecial special;
		VehicleDisaster disaster;
		VehicleShip ship;
	} u; */


	//abstract Vehicle AllocateVehicle();
	//abstract Vehicle[] AllocateVehicles(int num);
	//abstract Vehicle ForceAllocateVehicle();
	//abstract Vehicle ForceAllocateSpecialVehicle();

	abstract void VehicleTickProc();
	//typedef void *VehicleFromPosProc(Vehicle *v, void *data);

	void VehicleServiceInDepot()
	{
		if (tile.GetTileOwner() == Owner.OWNER_TOWN) 
			MA_Tax(value, this);

		date_of_last_service = Global._date;
		breakdowns_since_last_service = 0;
		reliability = GetEngine(engine_type).reliability;
	}

	/* TODO fixme
	void UpdateVehiclePosHash(int x, int y)
	{
		VehicleID *old_hash, *new_hash;
		int old_x = left_coord;
		int old_y = top_coord;
		Vehicle u;

		new_hash = (x == INVALID_COORD) ? null : &_vehicle_position_hash[GEN_HASH(x,y)];
		old_hash = (old_x == INVALID_COORD) ? null : &_vehicle_position_hash[GEN_HASH(old_x, old_y)];

		if (old_hash == new_hash) return;

		// remove from hash table? 
		if (old_hash != null) {
			Vehicle last = null;
			VehicleID idx = *old_hash;
			while ((u = GetVehicle(idx)) != v) {
				idx = u.next_hash;
				assert(idx != INVALID_VEHICLE);
				last = u;
			}

			if (last == null) {
	 *old_hash = next_hash;
			} else {
				last.next_hash = next_hash;
			}
		}

		// insert into hash table? 
		if (new_hash != null) {
			next_hash = *new_hash;
	 *new_hash = index;
		}
	}
	 */

	void VehiclePositionChanged()
	{
		int img = cur_image;
		Point pt = Point.RemapCoords(x_pos + x_offs, y_pos + y_offs, z_pos);
		final Sprite spr = GetSprite(img);

		pt.x += spr.x_offs;
		pt.y += spr.y_offs;

		UpdateVehiclePosHash(pt.x, pt.y);

		left_coord = pt.x;
		top_coord = pt.y;
		right_coord = pt.x + spr.width + 2;
		bottom_coord = pt.y + spr.height + 2;
	}



	Vehicle GetFirstVehicleInChain()
	{
		Vehicle v = this;
		Vehicle u;

		assert(v != null);

		if (v.first != null) {
			if (v.first.IsFrontEngine()) return v.first;

			DEBUG(misc, 0) ("v.first cache faulty. We shouldn't be here, rebuilding cache!");
		}

		/* It is the fact (currently) that newly built vehicles do not have
		 * their .first pointer set. When this is the case, go up to the
		 * first engine and set the pointers correctly. Also the first pointer
		 * is not saved in a savegame, so this has to be fixed up after loading */

		/* Find the 'locomotive' or the first wagon in a chain */
		while ((u = GetPrevVehicleInChain_bruteforce(v)) != null) v = u;

		/* Set the first pointer of all vehicles in that chain to the first wagon */
		if( v.IsFrontEngine())
			for (u = v; u != null; u = u.next) u.first = v;

		return (Vehicle)v;
	}

	int CountVehiclesInChain()
	{
		Vehicle v = this;
		int count = 0;
		do count++; while ((v = v.next) != null);
		return count;		
	}

	static void DeleteVehicleChain(Vehicle v)
	{
		do {
			Vehicle u = v;
			v = v.GetNextVehicle();
			DeleteVehicle(u);
		} while (v != null);
	}

	static void DeleteVehicle(Vehicle v)
	{
		Vehicle u;
		boolean has_artic_part = false;

		do {
			u = v.next;
			has_artic_part = v.EngineHasArticPart();
			Global.DeleteName(v.string_id);
			v.type = 0;
			UpdateVehiclePosHash(v, INVALID_COORD, 0);
			v.next_hash = Vehicle.INVALID_VEHICLE;

			if (v.orders != null)
				v.DeleteVehicleOrders();
			v = u;
		} while (v != null && has_artic_part);
	}




	/** Get the next real (non-articulated part) vehicle in the consist.
	 * @param v Vehicle.
	 * @return Next vehicle in the consist.
	 */
	Vehicle GetNextVehicle()
	{
		Vehicle u = next;
		while (u != null && u.IsArticulatedPart()) {
			u = u.next;
		}
		return u;
	}



	boolean HASBIT_subtype(int bit) {return 0 != (subtype &   (1 << bit)); }
	void SETBIT_subtype(int bit) { subtype |=  (1 << bit); }
	void CLRBIT_subtype(int bit) { subtype &= ~(1 << bit); }


	/*
	 * enum to handle train subtypes
	 * Do not access it directly unless you have to. Use the access functions below
	 * This is an enum to tell what bit to access as it is a bitmask
	 */

	//enum TrainSubtypes {
	static public final int Train_Front             = 0; // Leading engine of a train
	static public final int Train_Articulated_Part  = 1; // Articulated part of an engine
	static public final int Train_Wagon             = 2; // Wagon
	static public final int Train_Engine            = 3; // Engine, that can be front engines, but might be placed behind another engine
	static public final int Train_Free_Wagon        = 4; // First in a wagon chain (in depot)
	static public final int Train_Multiheaded       = 5; // Engine is a multiheaded
	//} TrainSubtype;



	/** Check if a vehicle is front engine
	 * @param v vehicle to check
	 * @return Returns true if vehicle is a front engine
	 */
	boolean IsFrontEngine()
	{
		return HASBIT_subtype( Train_Front);
	}

	/** Set front engine state
	 * @param v vehicle to change
	 */
	void SetFrontEngine()
	{
		SETBIT_subtype( Train_Front);
	}

	/** Remove the front engine state
	 * @param v vehicle to change
	 */
	void ClearFrontEngine()
	{
		CLRBIT_subtype( Train_Front);
	}

	/** Check if a vehicle is an articulated part of an engine
	 * @param v vehicle to check
	 * @return Returns true if vehicle is an articulated part
	 */
	boolean IsArticulatedPart()
	{
		return HASBIT_subtype( Train_Articulated_Part);
	}

	/** Set a vehicle to be an articulated part
	 * @param v vehicle to change
	 */
	void SetArticulatedPart()
	{
		SETBIT_subtype( Train_Articulated_Part);
	}

	/** Clear a vehicle from being an articulated part
	 * @param v vehicle to change
	 */
	void ClearArticulatedPart()
	{
		CLRBIT_subtype( Train_Articulated_Part);
	}

	/** Check if a vehicle is a wagon
	 * @param v vehicle to check
	 * @return Returns true if vehicle is a wagon
	 */
	boolean IsTrainWagon()
	{
		return HASBIT_subtype( Train_Wagon);
	}

	/** Set a vehicle to be a wagon
	 * @param v vehicle to change
	 */
	void SetTrainWagon()
	{
		SETBIT_subtype( Train_Wagon);
	}

	/** Clear wagon property
	 * @param v vehicle to change
	 */
	void ClearTrainWagon()
	{
		CLRBIT_subtype( Train_Wagon);
	}

	/** Check if a vehicle is an engine (can be first in a train)
	 * @param v vehicle to check
	 * @return Returns true if vehicle is an engine
	 */
	boolean IsTrainEngine()
	{
		return HASBIT_subtype( Train_Engine);
	}

	/** Set engine status
	 * @param v vehicle to change
	 */
	void SetTrainEngine()
	{
		SETBIT_subtype( Train_Engine);
	}

	/** Clear engine status
	 * @param v vehicle to change
	 */
	void ClearTrainEngine()
	{
		CLRBIT_subtype( Train_Engine);
	}

	/** Check if a vehicle is a free wagon (got no engine in front of it)
	 * @param v vehicle to check
	 * @return Returns true if vehicle is a free wagon
	 */
	boolean IsFreeWagon()
	{
		return HASBIT_subtype( Train_Free_Wagon);
	}

	/** Set if a vehicle is a free wagon
	 * @param v vehicle to change
	 */
	void SetFreeWagon()
	{
		SETBIT_subtype( Train_Free_Wagon);
	}

	/** Clear a vehicle from being a free wagon
	 * @param v vehicle to change
	 */
	void ClearFreeWagon()
	{
		CLRBIT_subtype( Train_Free_Wagon);
	}

	/** Check if a vehicle is a multiheaded engine
	 * @param v vehicle to check
	 * @return Returns true if vehicle is a multiheaded engine
	 */
	boolean IsMultiheaded()
	{
		return HASBIT_subtype( Train_Multiheaded);
	}

	/** Set if a vehicle is a multiheaded engine
	 * @param v vehicle to change
	 */
	void SetMultiheaded()
	{
		SETBIT_subtype( Train_Multiheaded);
	}

	/** Clear multiheaded engine property
	 * @param v vehicle to change
	 */
	void ClearMultiheaded()
	{
		CLRBIT_subtype( Train_Multiheaded);
	}





	/** Check if an engine has an articulated part.
	 * @param v Vehicle.
	 * @return True if the engine has an articulated part.
	 */
	boolean EngineHasArticPart()
	{
		return (next != null) && next.IsArticulatedPart();
	}

	/** Get the last part of a multi-part engine.
	 * @param v Vehicle.
	 * @return Last part of the engine.
	 */
	Vehicle GetLastEnginePart()
	{
		Vehicle v = this;
		while (v.EngineHasArticPart()) v = v.next;
		return v;
	}

	/**
	 * Check if a Vehicle really exists.
	 */
	private boolean IsValidVehicle()
	{
		return type != 0;
	}

	/* ERROR FIXME text Returns order 'index' of a vehicle or null when it doesn't exists */
	private Order GetVehicleOrder(int index)
	{
		Order order = orders;

		if (index < 0) return null;

		while (order != null && index-- > 0)
			order = order.next;

		return order;
	}

	/* Returns the last order of a vehicle, or null if it doesn't exists */
	private Order GetLastVehicleOrder()
	{
		Order order = orders;

		if (order == null) return null;

		while (order.next != null)
			order = order.next;

		return order;
	}


	/* Get the first vehicle of a shared-list, so we only have to walk forwards */
	private Vehicle GetFirstVehicleFromSharedList()
	{
		Vehicle u = this;
		while (u.prev_shared != null)
			u = u.prev_shared;

		return u;
	}

	static IPoolItemFactory<Vehicle> factory = new IPoolItemFactory<Vehicle>() 
	{		
		@Override
		public Vehicle createObject() {
			return new Vehicle();
		}
	}; 

	private final static MemoryPool<Vehicle> _vehicle_pool = new MemoryPool<Vehicle>(factory);

	/**
	 * Get the pointer to the vehicle with index 'index'
	 */
	private Vehicle getVehicle(VehicleID index)
	{
		return _vehicle_pool.GetItemFromPool(index.id);
	}

	/**
	 * Get the current size of the VehiclePool
	 */
	static private int GetVehiclePoolSize()
	{
		return _vehicle_pool.total_items();
	}

	/**
	 * Check if an index is a vehicle-index (so between 0 and max-vehicles)
	 *
	 * @return Returns true if the vehicle-id is in range
	 */
	private static boolean IsVehicleIndex(int index)
	{
		return (index >= 0) && (index < GetVehiclePoolSize());
	}



}
