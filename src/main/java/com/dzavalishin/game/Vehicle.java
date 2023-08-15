package com.dzavalishin.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.dzavalishin.ids.CargoID;
import com.dzavalishin.ids.EngineID;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.ids.UnitID;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.ConsumerOfVehicle;
import com.dzavalishin.ifaces.IPoolItem;
import com.dzavalishin.ifaces.IPoolItemFactory;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.ifaces.TileVehicleInterface;
import com.dzavalishin.ifaces.VehicleFromPosProc;
import com.dzavalishin.struct.BackuppedOrders;
import com.dzavalishin.struct.GetNewVehiclePosResult;
import com.dzavalishin.struct.Point;
import com.dzavalishin.struct.Rect;
import com.dzavalishin.struct.VQueueItem;
import com.dzavalishin.tables.BubbleMovement;
import com.dzavalishin.tables.EngineTables;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Sound;
import com.dzavalishin.xui.DrawPixelInfo;
import com.dzavalishin.xui.MiscGui;
import com.dzavalishin.xui.VehicleGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

public class Vehicle implements IPoolItem 
{
	private static final long serialVersionUID = 1L;
	

	int type;				// type, ie roadven,train,ship,aircraft,special
	int subtype;     		// subtype (Filled with values from EffectVehicles or TrainSubTypes)

	//VehicleID index;		// NOSAVE: Index in vehicle array
	public int index;		// NOSAVE: Index in vehicle array

	Vehicle next;			// next
	Vehicle first;			// NOSAVE: pointer to the first vehicle in the chain
	Vehicle depot_list;		//NOSAVE: linked list to tell what vehicles entered a depot during the last tick. Used by autoreplace

	//StringID string_id;	// Displayed string
	int string_id;			// Displayed string

	UnitID unitnumber;		// unit number, for display purposes only
	PlayerID owner;			// which player owns the vehicle?

	TileIndex tile;			// Current tile index
	TileIndex dest_tile;	// Heading for this tile

	int x_pos;				// coordinates
	int y_pos;
	int z_pos;				// Was byte, changed for aircraft queueing
	int direction;			// facing

	int spritenum; 			// currently displayed sprite index
	// 0xfd == custom sprite, 0xfe == custom second head sprite
	// 0xff == reserved for another custom sprite
	int cur_image; 			// sprite number for this vehicle
	int sprite_width;		// width of vehicle sprite
	int sprite_height;		// height of vehicle sprite
	int z_height;			// z-height of vehicle sprite
	int x_offs;				// x offset for vehicle sprite
	int y_offs;				// y offset for vehicle sprite

	EngineID engine_type;

	// for randomized variational spritegroups
	// bitmask used to resolve them; parts of it get reseeded when triggers
	// of corresponding spritegroups get matched
	int random_bits;
	int waiting_triggers; 	// triggers to be yet matched

	int max_speed;			// maximum speed
	int cur_speed;			// current speed
	int subspeed;			// fractional speed
	int acceleration; 		// used by train & aircraft
	int progress;

	private int vehstatus;			// Status
	int last_station_visited;

	int cargo_type;			// type of cargo this vehicle is carrying
	int cargo_days; 		// how many days have the pieces been in transit
	int cargo_source;		// source of cargo
	int cargo_cap;			// total capacity
	int cargo_count;		// how many pieces are used

	int day_counter; 		// increased by one for each day
	int tick_counter;		// increased by one for each tick

	/* Begin Order-stuff */
	private Order current_order = new Order();     //! The current order (+ status, like: loading)
	//OrderID cur_order_index; //! The index to the current order
	int cur_order_index; 	//! The index to the current order

	Order orders;           //! Pointer to the first order for this vehicle
	int num_orders;      	//! How many orders there are in the list 

	Vehicle next_shared;    //! If not null, this points to the next vehicle that shared the order
	Vehicle prev_shared;    //! If not null, this points to the prev vehicle that shared the order
	/* End Order-stuff */

	// Boundaries for the current position in the world and a next hash link.
	// NOSAVE: All of those can be updated with VehiclePositionChanged()
	public int left_coord;
	public int top_coord;
	int right_coord;
	int bottom_coord;
	//VehicleID next_hash;

	// Related to age and service time
	int age;				// Age in days
	int max_age;			// Maximum age
	public int date_of_last_service;
	int service_interval;
	int reliability;
	int reliability_spd_dec;
	int breakdown_ctr;
	int breakdown_delay;
	int breakdowns_since_last_service;
	int breakdown_chance;
	int build_year;

	boolean leave_depot_instantly;	// NOSAVE: stores if the vehicle needs to leave the depot it just entered. Used by autoreplace

	int load_unload_time_rem;

	int profit_this_year;
	int profit_last_year;
	int value;

	// Current position in a vehicle queue - can only belong to one queue at a time
	public VQueueItem queue_item;


	// TODO temp we create all of them, redo
	public VehicleRail rail = new VehicleRail();
	public VehicleAir air = new VehicleAir();
	public VehicleRoad road = new VehicleRoad();
	public VehicleSpecial special = new VehicleSpecial();
	public VehicleDisaster disaster;  
	public VehicleShip ship = new VehicleShip();


	static public final int INVALID_VEHICLE = -1; //0xFFFF; // TODO -1?
	static public final int INVALID_ENGINE  = -1;
	private static final int INVALID_COORD = -0x8000;

	/* A lot of code calls for the invalidation of the status bar, which is widget 5.
	 * Best is to have a virtual value for it when it needs to change again */
	static public final int STATUS_BAR = 5;


	private void InitializeVehicle()
	{
		//int indexc = index;

		type = 0;	
		subtype = 0;


		first = null;
		next = null;
		depot_list  = null;

		//string_id = null;
		string_id = 0;

		unitnumber = null;
		owner = PlayerID.getNone();//  PlayerID.get(-1); // TO DO value?

		tile = null;	
		dest_tile = null;

		x_pos=y_pos=z_pos = 9;
		direction = 0;		// facing

		spritenum = 0;
		cur_image = 0;
		sprite_width = sprite_height = z_height = 0;
		x_offs=y_offs = 0;

		engine_type = EngineID.get(0); // TODO correct?

		// for randomized variational spritegroups
		// bitmask used to resolve them; parts of it get reseeded when triggers
		// of corresponding spritegroups get matched
		random_bits = 0;
		waiting_triggers = 0; // triggers to be yet matched

		max_speed=cur_speed = 0;	// current speed
		subspeed = 0;		// fractional speed
		acceleration = progress = 0;

		vehstatus = 0;		// Status
		last_station_visited = 0;

		cargo_type = 0;	// type of cargo this vehicle is carrying
		cargo_days = 0; // how many days have the pieces been in transit
		cargo_source = 0;// source of cargo
		cargo_cap = 0;	// total capacity
		cargo_count = 0;// how many pieces are used

		day_counter = 0; // increased by one for each day
		tick_counter = 0;// increased by one for each tick

		/* Begin Order-stuff */
		current_order = new Order();     //! The current order (+ status, like: loading)
		cur_order_index = 0; //! The index to the current order

		orders = null;
		num_orders = 0;      //! How many orders there are in the list 

		next_shared = null;
		prev_shared = null;
		/* End Order-stuff */

		// Boundaries for the current position in the world and a next hash link.
		// NOSAVE: All of those can be updated with VehiclePositionChanged()
		left_coord = INVALID_COORD;
		top_coord = INVALID_COORD;
		right_coord = INVALID_COORD;
		bottom_coord = INVALID_COORD;

		//next_hash = new VehicleID(INVALID_VEHICLE);

		// Related to age and service time
		age = 0;				// Age in days
		max_age = 0;		// Maximum age
		date_of_last_service = 0;
		service_interval = 0;
		reliability = 0;
		reliability_spd_dec = 0;
		breakdown_ctr = 0;
		breakdown_delay = 0;
		breakdowns_since_last_service = 0;
		breakdown_chance = 0;
		build_year = 0;

		leave_depot_instantly = false;

		load_unload_time_rem = 0;

		profit_this_year = 0;
		profit_last_year = 0;
		value = 0;

		queue_item = null;

		rail = new VehicleRail();
		air = new VehicleAir();
		road = new VehicleRoad();
		special = new VehicleSpecial();
		disaster = null; 
		ship = new VehicleShip();

		air.desired_speed = 1;

		//index = indexc;
	}

	public Vehicle() {
		InitializeVehicle();
	}


	public static final int VEH_Train = 0x10;
	public static final int VEH_Road = 0x11;
	public static final int VEH_Ship = 0x12;
	public static final int VEH_Aircraft = 0x13;
	public static final int VEH_Special = 0x14;
	public static final int VEH_Disaster = 0x15;



	public static final int VS_HIDDEN = 1;
	public static final int VS_STOPPED = 2;
	public static final int VS_UNCLICKABLE = 4;
	public static final int VS_DEFPAL = 0x8;
	public static final int VS_TRAIN_SLOWING = 0x10;
	public static final int VS_DISASTER = 0x20;
	public static final int VS_AIRCRAFT_BROKEN = 0x40;
	public static final int VS_CRASHED = 0x80;



	public static final int EV_CHIMNEY_SMOKE   = 0;
	public static final int EV_STEAM_SMOKE     = 1;
	public static final int EV_DIESEL_SMOKE    = 2;
	public static final int EV_ELECTRIC_SPARK  = 3;
	public static final int EV_SMOKE           = 4;
	public static final int EV_EXPLOSION_LARGE = 5;
	public static final int EV_BREAKDOWN_SMOKE = 6;
	public static final int EV_EXPLOSION_SMALL = 7;
	public static final int EV_BULLDOZER       = 8;
	public static final int EV_BUBBLE          = 9;


	/*
	 *	These command macros are used to call vehicle type specific commands with non type specific commands
	 *	it should be used like: Cmd.DoCommandP(x, y, p1, p2, flags, CMD_STARTSTOP_VEH(v.type))
	 *	that line will start/stop a vehicle no matter what type it is
	 *	VEH_Train is used as an offset because the vehicle type values doesn't start with 0
	 */

	private static int CMD_BUILD_VEH(int x) { return _veh_build_proc_table[ x - VEH_Train]; }
	private static int CMD_SELL_VEH(int x)	{ return _veh_sell_proc_table[ x - VEH_Train]; }
	private static int CMD_REFIT_VEH(int x)	{ return _veh_refit_proc_table[ x - VEH_Train]; }


	/* Gone to enum
	public static final int VRF_REVERSING = 0;

	// used to calculate if train is going up or down
	public static final int VRF_GOINGUP   = 1;
	public static final int VRF_GOINGDOWN = 2;

	// used to store if a wagon is powered or not
	public static final int VRF_POWEREDWAGON = 3;
	*/

	// public enum VehicleListFlags {
	public static final int VL_DESC    = 0x01;
	public static final int VL_RESORT  = 0x02;
	public static final int VL_REBUILD = 0x04;







	//calculates tax
	public void MA_Tax(int income)
	{
		int old_expenses_type = Global.gs._yearly_expenses_type;
		assert income >= 0;

		if(Global._patches.allow_municipal_airports.get()) {
			double tax = (income / 100.0) * 20; //_patches.municipal_airports_tax;

			MiscGui.ShowCostOrIncomeAnimation(x_pos ,y_pos ,z_pos - 13, (int)tax);

			switch(type) {

			case VEH_Aircraft:	
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_AIRCRAFT_RUN);
				break;
			case VEH_Train:		
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_TRAIN_RUN);
				break;
			case VEH_Ship:		
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_SHIP_RUN);				
				break;
			case VEH_Road:		
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_ROADVEH_RUN);				
				break;

			}

			Player.SubtractMoneyFromPlayer((int)tax);
			Global.gs._yearly_expenses_type = old_expenses_type;
		}
	}


	void VehicleServiceInDepot()
	{
		if (tile.GetTileOwner().isTown()) 
			MA_Tax(value);

		date_of_last_service = Global.get_date();
		breakdowns_since_last_service = 0;
		reliability = Engine.GetEngine(engine_type).getReliability();
	}

	
	void UpdateVehiclePosHash(int x, int y)
	{
		if( x == INVALID_COORD )
			Global.gs._vehicle_hash.remove( new Point(left_coord, top_coord ), this );
		else
			Global.gs._vehicle_hash.update( new Point(left_coord, top_coord ), new Point(x,y), this );
	}
	

	void VehiclePositionChanged()
	{
		int img = cur_image;
		Point pt = Point.RemapCoords(x_pos + x_offs, y_pos + y_offs, z_pos);
		final Sprite spr = SpriteCache.GetSprite(img);

		pt.x += spr.x_offs;
		pt.y += spr.y_offs;

		UpdateVehiclePosHash(pt.x, pt.y);

		left_coord = pt.x;
		top_coord = pt.y;
		right_coord = pt.x + spr.width + 2;
		bottom_coord = pt.y + spr.height + 2;
	}



	public Vehicle GetFirstVehicleInChain()
	{
		Vehicle v = this;
		Vehicle u;

		//assert(v != null);

		if (v.first != null) {
			if (v.first.IsFrontEngine()) return v.first;

			Global.DEBUG_misc( 0, "v.first cache faulty. We shouldn't be here, rebuilding cache!");
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

		return v;
	}

	int CountVehiclesInChain()
	{
		Vehicle v = this;
		int count = 0;
		do count++; while ((v = v.next) != null);
		return count;		
	}


	public void DeleteVehicleChain()
	{
		DeleteVehicleChain(this);
	}

	static void DeleteVehicleChain(Vehicle v)
	{
		do {
			Vehicle u = v;
			v = v.GetNextVehicle();
			DeleteVehicle(u);
		} while (v != null);
	}

	void DeleteVehicle()
	{
		DeleteVehicle(this);
	}
	static void DeleteVehicle(Vehicle v)
	{
		Vehicle u;
		boolean has_artic_part;// = false;

		do {
			u = v.next;
			has_artic_part = v.EngineHasArticPart();
			Global.DeleteName(v.string_id);
			v.type = 0;
			v.UpdateVehiclePosHash(INVALID_COORD, 0);
			//_hash.remove(v);
			
			v.DeleteVehicleOrders();
			
			v = u;
		} while (v != null && has_artic_part);
	}




	/**
	 * Get the next real (non-articulated part) vehicle in the consist.
	 *
	 * @return Next vehicle in the consist.
	 */
	public Vehicle GetNextVehicle()
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



	/**
	 * Check if a vehicle is front engine
	 *
	 * @return Returns true if vehicle is a front engine
	 */
	public boolean IsFrontEngine()
	{
		return HASBIT_subtype(Train_Front);
	}

	/**
	 * Set front engine state
	 */
	public void SetFrontEngine()
	{
		SETBIT_subtype(Train_Front);
	}

	/**
	 * Remove the front engine state
	 */
	public void ClearFrontEngine()
	{
		CLRBIT_subtype(Train_Front);
	}

	/**
	 * Check if a vehicle is an articulated part of an engine
	 *
	 * @return Returns true if vehicle is an articulated part
	 */
	public boolean IsArticulatedPart()
	{
		return HASBIT_subtype(Train_Articulated_Part);
	}

	/**
	 * Set a vehicle to be an articulated part
	 */
	public void SetArticulatedPart()
	{
		SETBIT_subtype(Train_Articulated_Part);
	}

	/**
	 * Clear a vehicle from being an articulated part
	 */
	public void ClearArticulatedPart()
	{
		CLRBIT_subtype(Train_Articulated_Part);
	}

	/**
	 * Check if a vehicle is a wagon
	 *
	 * @return Returns true if vehicle is a wagon
	 */
	public boolean IsTrainWagon()
	{
		return HASBIT_subtype(Train_Wagon);
	}

	/**
	 * Set a vehicle to be a wagon
	 */
	public void SetTrainWagon()
	{
		SETBIT_subtype(Train_Wagon);
	}

	/**
	 * Clear wagon property
	 */
	public void ClearTrainWagon()
	{
		CLRBIT_subtype(Train_Wagon);
	}

	/**
	 * Check if a vehicle is an engine (can be first in a train)

	 * @return Returns true if vehicle is an engine
	 */
	public boolean IsTrainEngine()
	{
		return HASBIT_subtype(Train_Engine);
	}

	/**
	 * Set engine status
	 */
	public void SetTrainEngine()
	{
		SETBIT_subtype(Train_Engine);
	}

	/** Clear engine status
	 */
	public void ClearTrainEngine()
	{
		CLRBIT_subtype(Train_Engine);
	}

	/** Check if a vehicle is a free wagon (got no engine in front of it)
	 * @return Returns true if vehicle is a free wagon
	 */
	public boolean IsFreeWagon()
	{
		return HASBIT_subtype(Train_Free_Wagon);
	}

	/** Set if a vehicle is a free wagon
	 */
	public void SetFreeWagon()
	{
		SETBIT_subtype(Train_Free_Wagon);
	}

	/** Clear a vehicle from being a free wagon
	 */
	public void ClearFreeWagon()
	{
		CLRBIT_subtype(Train_Free_Wagon);
	}

	/** Check if a vehicle is a multiheaded engine
	 * @return Returns true if vehicle is a multiheaded engine
	 */
	public boolean IsMultiheaded()
	{
		return HASBIT_subtype(Train_Multiheaded);
	}

	/** Set if a vehicle is a multiheaded engine
	 */
	public void SetMultiheaded()
	{
		SETBIT_subtype(Train_Multiheaded);
	}

	/** Clear multiheaded engine property
	 */
	public void ClearMultiheaded()
	{
		CLRBIT_subtype(Train_Multiheaded);
	}





	/** Check if an engine has an articulated part.
	 * @return True if the engine has an articulated part.
	 */
	public boolean EngineHasArticPart()
	{
		return (next != null) && next.IsArticulatedPart();
	}

	/** Get the last part of a multi-part engine.
	 * @return Last part of the engine.
	 */
	public Vehicle GetLastEnginePart()
	{
		Vehicle v = this;
		while (v.EngineHasArticPart()) v = v.next;
		return v;
	}

	/**
	 * Check if a Vehicle really exists.
	 */
	public boolean isValid()
	{
		return type != 0;
	}

	/**
	 * 
	 * Returns order of a vehicle or null when order with given index doesn't exists
	 * 
	 * @param index Order index
	 * @return Order or null
	 * 
	 */
	public Order GetVehicleOrder(int index)
	{
		Order order = orders;

		if (index < 0) return null;

		while (order != null && index-- > 0)
			order = order.next;

		return order;
	}


	/* Returns the last order of a vehicle, or null if it doesn't exists */
	public Order GetLastVehicleOrder()
	{
		Order order = orders;

		if (order == null) return null;

		while (order.next != null)
			order = order.next;

		return order;
	}


	/* Get the first vehicle of a shared-list, so we only have to walk forwards */
	public Vehicle GetFirstVehicleFromSharedList()
	{
		Vehicle u = this;
		while (u.prev_shared != null)
			u = u.prev_shared;

		return u;
	}

	static final IPoolItemFactory<Vehicle> factory = new IPoolItemFactory<Vehicle>()
	{		
		private static final long serialVersionUID = 1L;

		@Override
		public Vehicle createObject() {
			return new Vehicle();
		}
	}; 

	/**
	 * Get the pointer to the vehicle with index 'index'
	 */
	public static Vehicle GetVehicle(VehicleID index)
	{
		return Global.gs._vehicles.GetItemFromPool(index.id);
	}

	/**
	 * Get the pointer to the vehicle with index 'index'
	 */
	public static Vehicle GetVehicle(int index)
	{
		return Global.gs._vehicles.GetItemFromPool(index);
	}

	/**
	 * Get the current size of the VehiclePool
	 */
	public static int GetVehiclePoolSize()
	{
		return Global.gs._vehicles.total_items();
	}

	/**
	 * Check if an index is a vehicle-index (so between 0 and max-vehicles)
	 *
	 * @return Returns true if the vehicle-id is in range
	 */
	public static boolean IsVehicleIndex(int index)
	{
		return (index >= 0) && (index < GetVehiclePoolSize());
	}


	public static void forEach( Consumer<Vehicle> c )
	{
		Global.gs._vehicles.forEach(c);
	}

	public void forEachOrder(Consumer<Order> c)
	{
		for( Order o = orders; o != null; o = o.next )
			c.accept(o);
	}

	public static Iterator<Vehicle> getIterator()
	{
		return Global.gs._vehicles.getIterator();
	}

	private static Iterator<Vehicle> getIteratorFrom(int id) {
		Iterator<Vehicle> ii = getIterator();

		while( ii.hasNext() )
		{
			Vehicle v = ii.next();
			if( v.index == id )
				return ii;
		}

		return null;
	}



	//public static final int INVALID_COORD = (-0x8000);
	//#define GEN_HASH(x,y) (((x & 0x1F80)>>7) + ((y & 0xFC0)))
	//int GEN_HASH(int x, int y) { return  ((x & 0x1F80)>>7) + (y & 0xFC0); }
	//int GEN_HASH(int x, int y) { return  (x + y) & 0xFFFF; }


	static final int _veh_build_proc_table[] = {
			Cmd.CMD_BUILD_RAIL_VEHICLE,
			Cmd.CMD_BUILD_ROAD_VEH,
			Cmd.CMD_BUILD_SHIP,
			Cmd.CMD_BUILD_AIRCRAFT,
	};
	static final int _veh_sell_proc_table[] = {
			Cmd.CMD_SELL_RAIL_WAGON,
			Cmd.CMD_SELL_ROAD_VEH,
			Cmd.CMD_SELL_SHIP,
			Cmd.CMD_SELL_AIRCRAFT,
	};

	static final int _veh_refit_proc_table[] = {
			Cmd.CMD_REFIT_RAIL_VEHICLE,
			0,	// road vehicles can't be refitted
			Cmd.CMD_REFIT_SHIP,
			Cmd.CMD_REFIT_AIRCRAFT,
	};



	//enum {
	// max vehicles: 64000 (512 * 125) 
	//public static final int VEHICLES_POOL_BLOCK_SIZE_BITS = 9;       // In bits, so (1 << 9) == 512 
	//public static final int VEHICLES_POOL_MAX_BLOCKS      = 125;

	//public static final int BLOCKS_FOR_SPECIAL_VEHICLES   = 2; //! Blocks needed for special vehicles
	//};*/


	/*
	void VehicleServiceInDepot(Vehicle v)
	{
		if (GetTileOwner(v.tile) == OWNER_TOWN) 
			MA_Tax(v.value, v);

		v.date_of_last_service = _date;
		v.breakdowns_since_last_service = 0;
		v.reliability = GetEngine(v.engine_type).reliability;
	}*/

	public boolean VehicleNeedsService()
	{
		if(isCrashed())
			return false; /* Crashed vehicles don't need service anymore */

		if (Player.GetPlayer(owner).engine_replacement[engine_type.id] != INVALID_ENGINE)
			return true; /* Vehicle is due to be replaced */

		if (Global._patches.no_servicing_if_no_breakdowns && GameOptions._opt.diff.vehicle_breakdowns == 0)
			return false;

		return Global._patches.servint_ispercent ?
				(reliability < Engine.GetEngine(engine_type).getReliability() * (100 - service_interval) / 100) :
					(date_of_last_service + service_interval < Global.get_date());
	}

	public void VehicleInTheWayErrMsg()
	{
		switch (type) {
		case VEH_Train:    Global._error_message = Str.STR_8803_TRAIN_IN_THE_WAY;        break;
		case VEH_Road:     Global._error_message = Str.STR_9000_ROAD_VEHICLE_IN_THE_WAY; break;
		case VEH_Aircraft: Global._error_message = Str.STR_A015_AIRCRAFT_IN_THE_WAY;     break;
		default:           Global._error_message = Str.STR_980E_SHIP_IN_THE_WAY;         break;
		}
	}

	public Object EnsureNoVehicleProc(Object data)
	{
		if (tile == null || !tile.equals(data) || type == VEH_Disaster)
			return null;

		VehicleInTheWayErrMsg();
		return this;
	}

	public static boolean EnsureNoVehicle(TileIndex tile)
	{
		return VehicleFromPos(tile, tile, Vehicle::EnsureNoVehicleProc) == null;
	}

	public Object EnsureNoVehicleProcZ(Object data)
	{
		final TileInfo ti = (TileInfo) data;

		if (!tile.equals(ti.tile) || z_pos != ti.z || type == VEH_Disaster)
			return null;

		VehicleInTheWayErrMsg();
		return this;
	}

	private static int Correct_Z(int tileh)
	{
		// needs z correction for slope-type graphics that have the NORTHERN tile lowered
		// 1, 2, 3, 4, 5, 6 and 7
		return TileIndex.CorrectZ(tileh) ? 8 : 0;
	}

	public static int GetCorrectTileHeight(TileIndex tile)
	{
		return Correct_Z(tile.GetTileSlope(null));
	}

	public static boolean EnsureNoVehicleZ(TileIndex tile, int z)
	{
		TileInfo ti = new TileInfo();

		Landscape.FindLandscapeHeightByTile(ti, tile);
		ti.z = z + Correct_Z(ti.tileh);

		return VehicleFromPos(tile, ti, Vehicle::EnsureNoVehicleProcZ) == null;
	}

	public static Vehicle FindVehicleOnTileZ(TileIndex tile, int z)
	{
		TileInfo ti = new TileInfo();

		ti.tile = tile;
		ti.z = z;

		return (Vehicle) VehicleFromPos(tile, ti, Vehicle::EnsureNoVehicleProcZ);
	}

	public static Vehicle FindVehicleBetween(TileIndex from, TileIndex to, int z)
	{
		int x1 = from.TileX();
		int y1 = from.TileY();
		int x2 = to.TileX();
		int y2 = to.TileY();
		Vehicle [] ret = {null};

		/* Make sure x1 < x2 or y1 < y2 */
		if (x1 > x2 || y1 > y2) {
			int t;
			t = x1; x1 = x2; x2 = t;
			t = y1; y1 = y2; y2 = t;
			//intswap(x1,x2);
			//intswap(y1,y2);
		}
		int fx1 = x1;
		int fx2 = x2;
		int fy1 = y1;
		int fy2 = y2;

		Global.gs._vehicles.forEach( veh ->
		{
			if ((veh.type == VEH_Train || veh.type == VEH_Road) && (z==0xFF || veh.z_pos == z)) {
				if ((veh.x_pos>>4) >= fx1 && (veh.x_pos>>4) <= fx2 &&
						(veh.y_pos>>4) >= fy1 && (veh.y_pos>>4) <= fy2) {
					ret[0] = veh;
				}
			}
		});

		return ret[0];
	}

	/*
	void VehiclePositionChanged(Vehicle v)
	{
		int img = v.cur_image;
		Point pt = RemapCoords(v.x_pos + v.x_offs, v.y_pos + v.y_offs, v.z_pos);
		final Sprite* spr = GetSprite(img);

		pt.x += spr.x_offs;
		pt.y += spr.y_offs;

		UpdateVehiclePosHash(v, pt.x, pt.y);

		v.left_coord = pt.x;
		v.top_coord = pt.y;
		v.right_coord = pt.x + spr.width + 2;
		v.bottom_coord = pt.y + spr.height + 2;
	}
	 */

	// Called after load to update coordinates
	public static void AfterLoadVehicles()
	{

		Global.gs._vehicles.forEach( v ->
		{
			v.first = null;
			if (v.type != 0) {
				switch (v.type) {
				case VEH_Train: v.cur_image = TrainCmd.GetTrainImage(v, v.direction); break;
				case VEH_Road: v.cur_image = RoadVehCmd.GetRoadVehImage(v, v.direction); break;
				case VEH_Ship: v.cur_image = Ship.GetShipImage(v, v.direction); break;
				case VEH_Aircraft:
					if (v.subtype == 0 || v.subtype == 2) {
						v.cur_image = AirCraft.GetAircraftImage(v, v.direction);
						if (v.next != null) v.next.cur_image = v.cur_image;
					}
					break;
				default: break;
				}

				v.left_coord = INVALID_COORD;
				v.VehiclePositionChanged();

				if (v.type == VEH_Train && (v.IsFrontEngine() || v.IsFreeWagon()))
					TrainCmd.TrainConsistChanged(v);
			}
		});
	}


	/**
	 * Get a value for a vehicle's random_bits.
	 * @return A random value from 0 to 255.
	 */
	public static int VehicleRandomBits()
	{
		return  BitOps.GB(Hal.Random(), 0, 8);
	}

	public static Vehicle ForceAllocateSpecialVehicle()
	{
		return AllocateVehicle();
		
		/* This stays a strange story.. there should always be room for special
		 * vehicles (special effects all over the map), but with 65k of vehicles
		 * is this realistic to double-check for that? For now we just reserve
		 * BLOCKS_FOR_SPECIAL_VEHICLES times block_size vehicles that may only
		 * be used for special vehicles.. should work nicely :) * /

		Vehicle [] ret = {null};

		Global.gs._vehicles.forEach( (ii,v) ->
		{
			// TO DO speedup No more room for the special vehicles, return null 
			//if (v.index >= (1 << _vehicle_pool.block_size_bits) * BLOCKS_FOR_SPECIAL_VEHICLES)
			//	return null;

			if (v.type == 0)
			{
				v.InitializeVehicle();
				ret[0] = v;
			}
		});

		return ret[0]; */
	}

	/*
	 * finds a free vehicle in the memory or allocates a new one
	 * returns a pointer to the first free vehicle or null if all vehicles are in use
	 * *skip_vehicles is an offset to where in the array we should begin looking
	 * this is to avoid looping though the same vehicles more than once after we learned that they are not free
	 * this feature is used by AllocateVehicles() since it need to allocate more than one and when
	 * another block is added to _vehicle_pool, since we only do that when we know it's already full
	 */
	//private static Vehicle AllocateSingleVehicle(VehicleID[] skip_vehicles)
	private static Vehicle AllocateSingleVehicle(int [] skip_vehicles)
	{
		/* See note by ForceAllocateSpecialVehicle() why we skip the
		 * first blocks */
		//final int offset = (1 << VEHICLES_POOL_BLOCK_SIZE_BITS) * BLOCKS_FOR_SPECIAL_VEHICLES;

		if (skip_vehicles[0] < (Global.gs._vehicles.total_items() )) //- offset)) 
		{	// make sure the offset in the array is not larger than the array itself

			//Iterator<Vehicle> ii = getIteratorFrom(offset + skip_vehicles[0]);
			Iterator<Vehicle> ii = getIteratorFrom(skip_vehicles[0]);
			while(ii != null && ii.hasNext())
			{
				Vehicle v = ii.next();

				skip_vehicles[0]++;
				if (v.type == 0)
				{
					v.InitializeVehicle();
					return v;
				}
			}
		}

		/* Check if we can add a block to the pool */
		if (Global.gs._vehicles.AddBlockToPool())
			return AllocateSingleVehicle(skip_vehicles);

		return null;
	}


	static int[] allocatorStartCounter = { 0 }; // TODO unused? Was used to keep space for special vehicles?
	public static Vehicle AllocateVehicle()
	{
		return AllocateSingleVehicle(allocatorStartCounter);
	}


	/** Allocates a lot of vehicles and frees them again
	 * @param vl pointer to an array of vehicles to get allocated. Can be null if the vehicles aren't needed (makes it test only)
	 * @param num number of vehicles to allocate room for
	 *	returns true if there is room to allocate all the vehicles
	 */
	public static boolean AllocateVehicles(Vehicle[] vl, int num)
	{
		int i;
		Vehicle v;
		int [] counter = { 0 };

		for(i = 0; i != num; i++) {
			v = AllocateSingleVehicle(counter);
			if (v == null) {
				return false;
			}
			if (vl != null) {
				vl[i] = v;
			}
		}

		return true;
	}



	public static Object VehicleFromPos(TileIndex tile, Object data, VehicleFromPosProc proc)
	{
		//int x,y,x2,y2;
		Point pt = Point.RemapCoords(tile.TileX() * 16, tile.TileY() * 16, 0);

		List<VehicleID> list = Global.gs._vehicle_hash.get(pt.x - 174, pt.y - 294, pt.x + 104, pt.y + 56);
		

		for(VehicleID vi : list) {
			Vehicle v = GetVehicle(vi);
			Object a;
			
			if(!v.isValid()) 
				continue;
			
			a = proc.apply(v, data);
			if (a != null) return a;
		}		
		
		return null;
	}


	public static void InitializeVehicles()
	{
		//int i;

		/* Clean the vehicle pool, and reserve enough blocks
		 *  for the special vehicles, plus one for all the other
		 *  vehicles (which is increased on-the-fly) */
		Global.gs._vehicles.CleanPool();
		Global.gs._vehicles.AddBlockToPool();

		//for (i = 0; i < BLOCKS_FOR_SPECIAL_VEHICLES; i++)			Global.gs._vehicles.AddBlockToPool();

		Global.gs._vehicle_hash.clear();
	}

	public Vehicle GetLastVehicleInChain()	
	{
		Vehicle v = this;
		while (v.next != null) v = v.next;
		return v;
	}

	/** Finds the previous vehicle in a chain, by a brute force search.
	 * This old function is REALLY slow because it searches through all vehicles to
	 * find the previous vehicle, but if v.first has not been set, then this function
	 * will need to be used to find the previous one. This function should never be
	 * called by anything but GetFirstVehicleInChain
	 */
	static Vehicle GetPrevVehicleInChain_bruteforce(final Vehicle v)
	{
		Vehicle [] ret = {null};

		Global.gs._vehicles.forEach( u ->
		{
			if (u.type == VEH_Train && u.next == v) ret[0] = u;
		});

		return ret[0];
	}

	/** Find the previous vehicle in a chain, by using the v.first cache.
	 * While this function is fast, it cannot be used in the GetFirstVehicleInChain
	 * function, otherwise you'll end up in an infinite loop call
	 */
	public Vehicle GetPrevVehicleInChain()
	{
		Vehicle u;

		u = GetFirstVehicleInChain();

		// Check to see if this is the first
		if (this == u) return null;

		do {
			if (u.next == this) return u;
		} while ( ( u = u.next) != null);

		return null;
	}

	/** Finds the first vehicle in a chain.
	 * This function reads out the v.first cache. Should the cache be dirty,
	 * it determines the first vehicle in a chain, and updates the cache.
	 * /
	Vehicle GetFirstVehicleInChain(final Vehicle v)
	{
		Vehicle  u;

		assert(v != null);

		if (v.first != null) {
			if (IsFrontEngine(v.first)) return v.first;

			Global.DEBUG_misc( 0, "v.first cache faulty. We shouldn't be here, rebuilding cache!");
		}

		/* It is the fact (currently) that newly built vehicles do not have
	 * their .first pointer set. When this is the case, go up to the
	 * first engine and set the pointers correctly. Also the first pointer
	 * is not saved in a savegame, so this has to be fixed up after loading */

	/* Find the 'locomotive' or the first wagon in a chain * /
		while ((u = GetPrevVehicleInChain_bruteforce(v)) != null) v = u;

		/* Set the first pointer of all vehicles in that chain to the first wagon * /
		if (IsFrontEngine(v))
			for (u = (Vehicle )v; u != null; u = u.next) u.first = (Vehicle )v;

		return (Vehicle )v;
	}
	/*
	int CountVehiclesInChain(final Vehicle  v)
	{
		int count = 0;
		do count++; while ((v = v.next) != null);
		return count;
	}


	void DeleteVehicle(Vehicle v)
	{
		Vehicle u;
		boolean has_artic_part = false;

		do {
			u = v.next;
			has_artic_part = EngineHasArticPart(v);
			DeleteName(v.string_id);
			v.type = 0;
			UpdateVehiclePosHash(v, INVALID_COORD, 0);
			v.next_hash = INVALID_VEHICLE;

			if (v.orders != null)
				DeleteVehicleOrders(v);
			v = u;
		} while (v != null && has_artic_part);
	}
	 * /

	void DeleteVehicleChain(Vehicle v)
	{
		do {
			Vehicle u = v;
			v = GetNextVehicle(v);
			DeleteVehicle(u);
		} while (v != null);
	}
	 */

	/*
	void Aircraft_Tick(Vehicle v);
	void RoadVeh_Tick(Vehicle v);
	void Ship_Tick(Vehicle v);
	void Train_Tick(Vehicle v);
	static void EffectVehicle_Tick(Vehicle v);
	void DisasterVehicle_Tick(Vehicle v);
	static void MaybeReplaceVehicle(Vehicle v);
	 */
	// head of the linked list to tell what vehicles that visited a depot in a tick
	static Vehicle  _first_veh_in_depot_list;

	/**
	 * Adds this vehicle to the list of vehicles, that visited a depot this tick
	 */
	void VehicleEnteredDepotThisTick()
	{
		Vehicle v = this;
		// we need to set v.leave_depot_instantly as we have no control of it's contents at this time
		//if (BitOps.HASBIT(v.current_order.flags, Order.OFB_HALT_IN_DEPOT) && !BitOps.HASBIT(v.current_order.flags, Order.OFB_PART_OF_ORDERS) && v.current_order.type == Order.OT_GOTO_DEPOT) 
		if (v.current_order.hasFlag(Order.OF_HALT_IN_DEPOT) 
				&& !v.current_order.hasFlag(Order.OF_PART_OF_ORDERS) 
				&& v.current_order.typeIs(Order.OT_GOTO_DEPOT) ) 
		{
			// we keep the vehicle in the depot since the user ordered it to stay
			v.leave_depot_instantly = false;
		} else {
			// the vehicle do not plan on stopping in the depot, so we stop it to ensure that it will not reserve the path
			// out of the depot before we might autoreplace it to a different engine. The new engine would not own the reserved path
			// we store that we stopped the vehicle, so autoreplace can start it again
			v.stop();
			v.leave_depot_instantly = true;
		}

		if (_first_veh_in_depot_list == null) {
			_first_veh_in_depot_list = v;
		} else {
			Vehicle w = _first_veh_in_depot_list;
			while (w.depot_list != null) w = w.depot_list;
			w.depot_list = v;
		}
	}

	static final ConsumerOfVehicle[] _vehicle_tick_procs = {
			TrainCmd::Train_Tick,
			RoadVehCmd::RoadVeh_Tick,
			Ship::Ship_Tick,
			AirCraft::Aircraft_Tick,
			Vehicle::EffectVehicle_Tick,
			DisasterCmd::DisasterVehicle_Tick,
	};

	static void CallVehicleTicks()
	{
		_first_veh_in_depot_list = null;	// now we are sure it's initialized at the start of each tick

		/*Global.gs._vehicles.forEach( (ii,v) ->
		{
			if (v.type != 0) {
				_vehicle_tick_procs[v.type - 0x10].accept(v);
			}
		});*/

		Global.gs._vehicles.forEachValid( v -> { _vehicle_tick_procs[v.type - 0x10].accept(v); });
		
		// now we handle all the vehicles that entered a depot this tick
		Vehicle v = _first_veh_in_depot_list;
		while (v != null) {
			Vehicle w = v.depot_list;
			v.depot_list = null;	// it should always be null at the end of each tick
			MaybeReplaceVehicle(v);
			v = w;
		}
	}

	static boolean CanFillVehicle_FullLoadAny(Vehicle v)
	{
		int full = 0, not_full = 0;

		//special handling of aircraft

		//if the aircraft carries passengers and is NOT full, then
		//continue loading, no matter how much mail is in
		if ((v.type == VEH_Aircraft) && (v.cargo_type == AcceptedCargo.CT_PASSENGERS) && (v.cargo_cap != v.cargo_count)) {
			return true;
		}

		// patch should return "true" to continue loading, i.e. when there is no cargo type that is fully loaded.
		do {
			//Should never happen, but just in case future additions change this
			assert(v.cargo_type<32);

			if (v.cargo_cap != 0) {
				int mask = 1 << v.cargo_type;
				if (v.cargo_cap == v.cargo_count) full |= mask; else not_full |= mask;
			}
		} while ( (v=v.next) != null);

		// continue loading if there is a non full cargo type and no cargo type that is full
		return (not_full != 0) && (full & ~not_full) == 0;
	}

	boolean CanFillVehicle()
	{
		if (tile.IsTileType(TileTypes.MP_STATION) ||
				(type == VEH_Ship && (
						tile.iadd(TileIndex.TileDiffXY(1,  0)).IsTileType(TileTypes.MP_STATION) ||
						tile.iadd(TileIndex.TileDiffXY(-1, 0)).IsTileType(TileTypes.MP_STATION) ||
						tile.iadd(TileIndex.TileDiffXY(0,  1)).IsTileType(TileTypes.MP_STATION) ||
						tile.iadd(TileIndex.TileDiffXY(0, -1)).IsTileType(TileTypes.MP_STATION) ||
						tile.iadd(TileIndex.TileDiffXY(-2, 0)).IsTileType(TileTypes.MP_STATION)
						))) {

			// If patch is active, use alternative CanFillVehicle-function
			if (Global._patches.full_load_any.get())
				return CanFillVehicle_FullLoadAny(this);

			Vehicle v = this;
			
			do {
				if (v.cargo_count != v.cargo_cap)
					return true;
			} while ( (v=v.next) != null);
		}
		return false;
	}

	/** Check if a given engine type can be refitted to a given cargo
	 * @param engine_type Engine type to check
	 * @param cid_to check refit to this cargo-type
	 * @return true if it is possible, false otherwise
	 */
	public static boolean CanRefitTo(EngineID engine_type, CargoID cid_to)
	{
		//CargoID 
		int cid = EngineTables._global_cargo_id[GameOptions._opt_ptr.landscape][cid_to.id];
		return BitOps.HASBIT(Global._engine_info[engine_type.id].refit_mask, cid);
	}

	public static void DoDrawVehicle(final Vehicle v)
	{
		int image = v.cur_image;

		if(0!=(v.vehstatus & VS_DISASTER)) {
			image = Sprite.RET_MAKE_TRANSPARENT(image);
		} else if( 0 != (v.vehstatus & VS_DEFPAL) ) {
			image |= v.isCrashed() ? Sprite.PALETTE_CRASH : Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(v.owner));
		}

		ViewPort.AddSortableSpriteToDraw(image, v.x_pos + v.x_offs, v.y_pos + v.y_offs,
				v.sprite_width, v.sprite_height, v.z_height, v.z_pos);
	}

	public static void ViewportAddVehicles(DrawPixelInfo dpi)
	{
		
		List<VehicleID> found = Global.gs._vehicle_hash.get( 
				dpi.left - 70, dpi.top - 70, 
				dpi.left+dpi.width, dpi.top+dpi.height );
		 
		for( VehicleID vi : found )
		{
			Vehicle v  = Vehicle.GetVehicle(vi);

			if( v != null && !v.isHidden() &&
					dpi.left <= v.right_coord &&
					dpi.top <= v.bottom_coord &&
					dpi.left + dpi.width >= v.left_coord &&
					dpi.top + dpi.height >= v.top_coord ) {
				DoDrawVehicle(v);
			}
		}
		
		/*
		int x,xb, y, x2, y2;
		VehicleID veh;
		Vehicle v;

		x  = ((dpi.left - 70) & 0x1F80) >> 7;
				x2 = ((dpi.left + dpi.width) & 0x1F80) >> 7;

			y  = ((dpi.top - 70) & 0xFC0);
			y2 = ((dpi.top + dpi.height) & 0xFC0);

			for(;;) {
				xb = x;
				for(;;) {
					//veh = _vehicle_position_hash[(x + y) & 0xFFFF];
					//veh = _hash.get(x, y);
					veh = _hash.get((x + y) & 0xFFFF);
					//while(veh.id != INVALID_VEHICLE) 
					if( veh != null )
					{
						v = GetVehicle(veh);
						// we keep one veh per hash point! Fix!

						if( v != null &&
								0 != (v.vehstatus & VS_HIDDEN) &&
								dpi.left <= v.right_coord &&
								dpi.top <= v.bottom_coord &&
								dpi.left + dpi.width >= v.left_coord &&
								dpi.top + dpi.height >= v.top_coord) {
							DoDrawVehicle(v);
						}
						//	veh = v.next_hash;
					}

					if (x == x2)
						break;
					x = (x + 1) & 0x3F;
				}
				x = xb;

				if (y == y2)
					break;
				y = (y + 0x40) & ((0x3F) << 6);
			}
			*/
	}

	static void ChimneySmokeInit(Vehicle v)
	{
		int r = Hal.Random();
		v.cur_image = Sprite.SPR_CHIMNEY_SMOKE_0 + BitOps.GB(r, 0, 3);
		v.progress = BitOps.GB(r, 16, 3);
	}

	static void ChimneySmokeTick(Vehicle v)
	{
		if (v.progress > 0) {
			v.progress--;
		} else {
			TileIndex tile;

			v.BeginVehicleMove();

			tile = TileIndex.TileVirtXY(v.x_pos, v.y_pos);
			if (!tile.IsTileType(TileTypes.MP_INDUSTRY)) {
				v.EndVehicleMove();
				DeleteVehicle(v);
				return;
			}

			if (v.cur_image != Sprite.SPR_CHIMNEY_SMOKE_7) {
				v.cur_image++;
			} else {
				v.cur_image = Sprite.SPR_CHIMNEY_SMOKE_0;
			}
			v.progress = 7;
			v.VehiclePositionChanged();
			v.EndVehicleMove();
		}
	}

	static void SteamSmokeInit(Vehicle v)
	{
		v.cur_image = Sprite.SPR_STEAM_SMOKE_0;
		v.progress = 12;
	}

	static void SteamSmokeTick(Vehicle v)
	{
		boolean moved = false;

		v.BeginVehicleMove();

		v.progress++;

		if ((v.progress & 7) == 0) {
			v.z_pos++;
			moved = true;
		}

		if ((v.progress & 0xF) == 4) {
			if (v.cur_image != Sprite.SPR_STEAM_SMOKE_4) {
				v.cur_image++;
			} else {
				v.EndVehicleMove();
				DeleteVehicle(v);
				return;
			}
			moved = true;
		}

		if (moved) {
			v.VehiclePositionChanged();
			v.EndVehicleMove();
		}
	}

	static void DieselSmokeInit(Vehicle v)
	{
		v.cur_image = Sprite.SPR_DIESEL_SMOKE_0;
		v.progress = 0;
	}

	static void DieselSmokeTick(Vehicle v)
	{
		v.progress++;

		if ((v.progress & 3) == 0) {
			v.BeginVehicleMove();
			v.z_pos++;
			v.VehiclePositionChanged();
			v.EndVehicleMove();
		} else if ((v.progress & 7) == 1) {
			v.BeginVehicleMove();
			if (v.cur_image != Sprite.SPR_DIESEL_SMOKE_5) {
				v.cur_image++;
				v.VehiclePositionChanged();
				v.EndVehicleMove();
			} else {
				v.EndVehicleMove();
				DeleteVehicle(v);
			}
		}
	}

	static void ElectricSparkInit(Vehicle v)
	{
		v.cur_image = Sprite.SPR_ELECTRIC_SPARK_0;
		v.progress = 1;
	}

	static void ElectricSparkTick(Vehicle v)
	{
		if (v.progress < 2) {
			v.progress++;
		} else {
			v.progress = 0;
			v.BeginVehicleMove();
			if (v.cur_image != Sprite.SPR_ELECTRIC_SPARK_5) {
				v.cur_image++;
				v.VehiclePositionChanged();
				v.EndVehicleMove();
			} else {
				v.EndVehicleMove();
				DeleteVehicle(v);
			}
		}
	}

	static void SmokeInit(Vehicle v)
	{
		v.cur_image = Sprite.SPR_SMOKE_0;
		v.progress = 12;
	}

	static void SmokeTick(Vehicle v)
	{
		boolean moved = false;

		v.BeginVehicleMove();

		v.progress++;

		if ((v.progress & 3) == 0) {
			v.z_pos++;
			moved = true;
		}

		if ((v.progress & 0xF) == 4) {
			if (v.cur_image != Sprite.SPR_SMOKE_4) {
				v.cur_image++;
			} else {
				v.EndVehicleMove();
				DeleteVehicle(v);
				return;
			}
			moved = true;
		}

		if (moved) {
			v.VehiclePositionChanged();
			v.EndVehicleMove();
		}
	}

	static void ExplosionLargeInit(Vehicle v)
	{
		v.cur_image = Sprite.SPR_EXPLOSION_LARGE_0;
		v.progress = 0;
	}

	static void ExplosionLargeTick(Vehicle v)
	{
		v.progress++;
		if ((v.progress & 3) == 0) {
			v.BeginVehicleMove();
			if (v.cur_image != Sprite.SPR_EXPLOSION_LARGE_F) {
				v.cur_image++;
				v.VehiclePositionChanged();
				v.EndVehicleMove();
			} else {
				v.EndVehicleMove();
				DeleteVehicle(v);
			}
		}
	}

	static void BreakdownSmokeInit(Vehicle v)
	{
		v.cur_image = Sprite.SPR_BREAKDOWN_SMOKE_0;
		v.progress = 0;
	}

	static void BreakdownSmokeTick(Vehicle v)
	{
		v.progress++;
		if ((v.progress & 7) == 0) {
			v.BeginVehicleMove();
			if (v.cur_image != Sprite.SPR_BREAKDOWN_SMOKE_3) {
				v.cur_image++;
			} else {
				v.cur_image = Sprite.SPR_BREAKDOWN_SMOKE_0;
			}
			v.VehiclePositionChanged();
			v.EndVehicleMove();
		}

		v.special.unk0--;
		if (v.special.unk0 == 0) {
			v.BeginVehicleMove();
			v.EndVehicleMove();
			DeleteVehicle(v);
		}
	}

	static void ExplosionSmallInit(Vehicle v)
	{
		v.cur_image = Sprite.SPR_EXPLOSION_SMALL_0;
		v.progress = 0;
	}

	static void ExplosionSmallTick(Vehicle v)
	{
		v.progress++;
		if ((v.progress & 3) == 0) {
			v.BeginVehicleMove();
			if (v.cur_image != Sprite.SPR_EXPLOSION_SMALL_B) {
				v.cur_image++;
				v.VehiclePositionChanged();
				v.EndVehicleMove();
			} else {
				v.EndVehicleMove();
				DeleteVehicle(v);
			}
		}
	}

	static void BulldozerInit(Vehicle v)
	{
		v.cur_image = Sprite.SPR_BULLDOZER_NE;
		v.progress = 0;
		v.special.unk0 = 0;
		v.special.unk2 = 0;
	}

	static class BulldozerMovement {
		final int direction;
		final int image;
		final int duration;

		public BulldozerMovement(int direction, int image, int dur) {
			this.direction = direction;
			this.image = image;
			duration = dur;
		}
	} 

	static final BulldozerMovement [] _bulldozer_movement = {
			new BulldozerMovement(  0, 0, 4 ),
			new BulldozerMovement(  3, 3, 4 ),
			new BulldozerMovement(  2, 2, 7 ),
			new BulldozerMovement(  0, 2, 7 ),
			new BulldozerMovement(  1, 1, 3 ),
			new BulldozerMovement(  2, 2, 7 ),
			new BulldozerMovement(  0, 2, 7 ),
			new BulldozerMovement(  1, 1, 3 ),
			new BulldozerMovement(  2, 2, 7 ),
			new BulldozerMovement(  0, 2, 7 ),
			new BulldozerMovement(  3, 3, 6 ),
			new BulldozerMovement(  2, 2, 6 ),
			new BulldozerMovement(  1, 1, 7 ),
			new BulldozerMovement(  3, 1, 7 ),
			new BulldozerMovement(  0, 0, 3 ),
			new BulldozerMovement(  1, 1, 7 ),
			new BulldozerMovement(  3, 1, 7 ),
			new BulldozerMovement(  0, 0, 3 ),
			new BulldozerMovement(  1, 1, 7 ),
			new BulldozerMovement(  3, 1, 7 )
	};


	static final Point[]_inc_by_dir = {
			new Point(  -1,  0 ),
			new Point(   0,  1 ),
			new Point(   1,  0 ),
			new Point(   0, -1 )
	};

	static void BulldozerTick(Vehicle v)
	{
		v.progress++;
		if ((v.progress & 7) == 0) {
			final BulldozerMovement b = _bulldozer_movement[v.special.unk0];

			v.BeginVehicleMove();

			v.cur_image = Sprite.SPR_BULLDOZER_NE + b.image;

			v.x_pos += _inc_by_dir[b.direction].x;
			v.y_pos += _inc_by_dir[b.direction].y;

			v.special.unk2++;
			if (v.special.unk2 >= b.duration) {
				v.special.unk2 = 0;
				v.special.unk0++;
				if (v.special.unk0 == _bulldozer_movement.length) {
					v.EndVehicleMove();
					DeleteVehicle(v);
					return;
				}
			}
			v.VehiclePositionChanged();
			v.EndVehicleMove();
		}
	}

	static void BubbleInit(Vehicle v)
	{
		v.cur_image = Sprite.SPR_BUBBLE_GENERATE_0;
		v.spritenum = 0;
		v.progress = 0;
	}


	//#define MK(x, y, z, i) { x, y, z, i }
	//#define ME(i) { i, 4, 0, 0 }

	static final BubbleMovement _bubble_float_sw[] = {
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(1,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(1,0,1,2),
			new BubbleMovement(1),//ME(1)
	};


	static final BubbleMovement _bubble_float_ne[] = {
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(-1,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(-1,0,1,2),
			new BubbleMovement(1),//ME(1)
	};

	static final BubbleMovement _bubble_float_se[] = {
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,1,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,1,1,2),
			new BubbleMovement(1),//ME(1)
	};

	static final BubbleMovement _bubble_float_nw[] = {
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,-1,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,-1,1,2),
			new BubbleMovement(1),//ME(1)
	};

	static final BubbleMovement _bubble_burst[] = {
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,7),
			new BubbleMovement(0,0,1,8),
			new BubbleMovement(0,0,1,9),
			new BubbleMovement(0),//ME(0)
	};

	static final BubbleMovement _bubble_absorb[] = {
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(0,0,1,1),
			new BubbleMovement(2,1,3,0),
			new BubbleMovement(1,1,3,1),
			new BubbleMovement(2,1,3,0),
			new BubbleMovement(1,1,3,2),
			new BubbleMovement(2,1,3,0),
			new BubbleMovement(1,1,3,1),
			new BubbleMovement(2,1,3,0),
			new BubbleMovement(1,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(1,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(1,0,1,2),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(1,0,1,1),
			new BubbleMovement(0,0,1,0),
			new BubbleMovement(1,0,1,2),
			new BubbleMovement(2),//ME(2),
			new BubbleMovement(0,0,0,0xA),
			new BubbleMovement(0,0,0,0xB),
			new BubbleMovement(0,0,0,0xC),
			new BubbleMovement(0,0,0,0xD),
			new BubbleMovement(0,0,0,0xE),
			new BubbleMovement(0),// ME(0)
	};
	//#undef ME
	//#undef MK

	static final BubbleMovement[][] _bubble_movement = {
			_bubble_float_sw,
			_bubble_float_ne,
			_bubble_float_se,
			_bubble_float_nw,
			_bubble_burst,
			_bubble_absorb,
	};

	static void BubbleTick(Vehicle v)
	{
		/*
		 * Warning: those effects can NOT use Random(), and have to use
		 *  InteractiveRandom(), because somehow someone forgot to save
		 *  spritenum to the savegame, and so it will cause desyncs in
		 *  multiplayer!! (that is: in ToyLand)
		 */
		int et;
		BubbleMovement b;

		v.progress++;
		if ((v.progress & 3) != 0)
			return;

		v.BeginVehicleMove();

		if (v.spritenum == 0) {
			v.cur_image++;
			if (v.cur_image < Sprite.SPR_BUBBLE_GENERATE_3) {
				v.VehiclePositionChanged();
				v.EndVehicleMove();
				return;
			}
			if (v.special.unk2 != 0) {
				v.spritenum =  (BitOps.GB(Hal.InteractiveRandom(), 0, 2) + 1);
			} else {
				v.spritenum = 6;
			}
			et = 0;
		} else {
			et = v.engine_type.id + 1;
		}

		b = _bubble_movement[v.spritenum - 1][et];

		if (b.y == 4 && b.x == 0) {
			v.EndVehicleMove();
			DeleteVehicle(v);
			return;
		}

		if (b.y == 4 && b.x == 1) {
			if (v.z_pos > 180 || BitOps.CHANCE16I(1, 96, Hal.InteractiveRandom())) {
				v.spritenum = 5;
				v.SndPlayVehicleFx(Snd.SND_2F_POP);
			}
			et = 0;
		}

		if (b.y == 4 && b.x == 2) {
			TileIndex tile;

			et++;
			v.SndPlayVehicleFx(Snd.SND_31_EXTRACT);

			tile = TileIndex.TileVirtXY(v.x_pos, v.y_pos);
			if (tile.IsTileType( TileTypes.MP_INDUSTRY) && tile.getMap().m5 == 0xA2) 
				TextEffect.AddAnimatedTile(tile);
		}

		v.engine_type = EngineID.get(et);
		b = _bubble_movement[v.spritenum - 1][et];

		v.x_pos += b.x;
		v.y_pos += b.y;
		v.z_pos += b.z;
		v.cur_image = Sprite.SPR_BUBBLE_0 + b.image;

		v.VehiclePositionChanged();
		v.EndVehicleMove();
	}


	//typedef void EffectInitProc(Vehicle v);
	//typedef void EffectTickProc(Vehicle v);

	static final ConsumerOfVehicle []  _effect_init_procs = {
			Vehicle::ChimneySmokeInit,
			Vehicle::SteamSmokeInit,
			Vehicle::DieselSmokeInit,
			Vehicle::ElectricSparkInit,
			Vehicle::SmokeInit,
			Vehicle::ExplosionLargeInit,
			Vehicle::BreakdownSmokeInit,
			Vehicle::ExplosionSmallInit,
			Vehicle::BulldozerInit,
			Vehicle::BubbleInit,
	};

	static final ConsumerOfVehicle  _effect_tick_procs[] = {
			Vehicle::ChimneySmokeTick,
			Vehicle::SteamSmokeTick,
			Vehicle::DieselSmokeTick,
			Vehicle::ElectricSparkTick,
			Vehicle::SmokeTick,
			Vehicle::ExplosionLargeTick,
			Vehicle::BreakdownSmokeTick,
			Vehicle::ExplosionSmallTick,
			Vehicle::BulldozerTick,
			Vehicle::BubbleTick,
	};


	//Vehicle CreateEffectVehicle(int x, int y, int z, EffectVehicle type)
	static Vehicle CreateEffectVehicle(int x, int y, int z, int type)
	{
		Vehicle v;

		v = ForceAllocateSpecialVehicle();
		if (v != null) {
			v.type = VEH_Special;
			v.subtype =  type;
			v.x_pos = x;
			v.y_pos = y;
			v.z_pos = z;
			v.z_height = v.sprite_width = v.sprite_height = 1;
			v.x_offs = v.y_offs = 0;
			v.tile = new TileIndex(0);
			v.vehstatus = VS_UNCLICKABLE;

			_effect_init_procs[type].accept(v);

			v.VehiclePositionChanged();
			v.BeginVehicleMove();
			v.EndVehicleMove();
		}
		return v;
	}

	//Vehicle CreateEffectVehicleAbove(int x, int y, int z, EffectVehicle type)
	static Vehicle CreateEffectVehicleAbove(int x, int y, int z, int type)
	{
		return CreateEffectVehicle(x, y, Landscape.GetSlopeZ(x, y) + z, type);
	}

	//Vehicle CreateEffectVehicleRel(final Vehicle v, int x, int y, int z, EffectVehicle type)
	Vehicle CreateEffectVehicleRel(int x, int y, int z, int type)
	{
		final Vehicle v = this;
		return CreateEffectVehicle(v.x_pos + x, v.y_pos + y, v.z_pos + z, type);
	}

	static void EffectVehicle_Tick(Vehicle v)
	{
		_effect_tick_procs[v.subtype].accept(v);
	}

	public static Vehicle CheckClickOnVehicle(final ViewPort vp, int x, int y)
	{
		Vehicle [] found = {null}; //, v;
		int [] best_dist = {Integer.MAX_VALUE};

		if ( (x -= vp.getLeft()) >= vp.getWidth() ||
				(y -= vp.getTop()) >= vp.getHeight())
			return null;

		x = (x << vp.getZoom()) + vp.getVirtual_left();
		y = (y << vp.getZoom()) + vp.getVirtual_top();

		int fx = x;
		int fy = y;

		Vehicle.forEach( (v) ->
		{
			if (v.type != 0 && (v.vehstatus & (VS_HIDDEN|VS_UNCLICKABLE)) == 0 &&
					fx >= v.left_coord && fx <= v.right_coord &&
					fy >= v.top_coord && fy <= v.bottom_coord) {

				int dist = Math.max(
						/*myabs*/ Math.abs( ((v.left_coord + v.right_coord)>>1) - fx ),
						Math.abs( ((v.top_coord + v.bottom_coord)>>1) - fy )
						);

				if (dist < best_dist[0]) {
					found[0] = v;
					best_dist[0] = dist;
				}
			}
		});

		return found[0];
	}


	void DecreaseVehicleValue()
	{
		value -= value >> 8;
				Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, index);
	}

	static final int _breakdown_chance[] = {
			3, 3, 3, 3, 3, 3, 3, 3,
			4, 4, 5, 5, 6, 6, 7, 7,
			8, 8, 9, 9, 10, 10, 11, 11,
			12, 13, 13, 13, 13, 14, 15, 16,
			17, 19, 21, 25, 28, 31, 34, 37,
			40, 44, 48, 52, 56, 60, 64, 68,
			72, 80, 90, 100, 110, 120, 130, 140,
			150, 170, 190, 210, 230, 250, 250, 250,
	};

	void CheckVehicleBreakdown()
	{
		int rel, rel_old;
		int r;
		int chance;
		Vehicle v = this;

		/* decrease reliability */
		v.reliability = rel = Math.max((rel_old = v.reliability) - v.reliability_spd_dec, 0);
		if ((rel_old >> 8) != (rel >> 8))
			Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);

		if (v.breakdown_ctr != 0 || v.isStopped() ||
				v.cur_speed < 5 || Global._game_mode == GameModes.GM_MENU) {
			return;
		}

		r = Hal.Random();

		/* increase chance of failure */
		chance = v.breakdown_chance + 1;
		if (BitOps.CHANCE16I(1,25,r)) chance += 25;
		v.breakdown_chance =  Math.min(255, chance);

		/* calculate reliability value to use in comparison */
		rel = v.reliability;
		if (v.type == VEH_Ship) rel += 0x6666;

		/* disabled breakdowns? */
		if (GameOptions._opt.diff.vehicle_breakdowns < 1) return;

		/* reduced breakdowns? */
		if (GameOptions._opt.diff.vehicle_breakdowns == 1) rel += 0x6666;

		/* check if to break down */
		if (_breakdown_chance[Math.min(rel, 0xffff) >> 10] <= v.breakdown_chance) {
			v.breakdown_ctr    =  (BitOps.GB(r, 16, 6) + 0x3F);
			v.breakdown_delay  =  (BitOps.GB(r, 24, 7) + 0x80);
			v.breakdown_chance = 0;
		}
	}

	static final int _vehicle_type_names[] = {
			Str.STR_019F_TRAIN,
			Str.STR_019C_ROAD_VEHICLE,
			Str.STR_019E_SHIP,
			Str.STR_019D_AIRCRAFT,
	};

	static void ShowVehicleGettingOld(Vehicle v, /*StringID*/ int msg)
	{
		if(!v.owner.isLocalPlayer()) return;

		// Do not show getting-old message if autorenew is active
		if (Player.GetPlayer(v.owner).isEngine_renew()) return;

		Global.SetDParam(0, _vehicle_type_names[v.type - 0x10]);
		Global.SetDParam(1, v.unitnumber.id);
		NewsItem.AddNewsItem(msg, NewsItem.NEWS_FLAGS(NewsItem.NM_SMALL, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ADVICE, 0), v.index, 0);
	}

	void AgeVehicle()
	{
		Vehicle v = this;

		if (v.age < 65535)
			v.age++;

		int agep = v.age - v.max_age;
		if (agep == 366*0 || agep == 366*1 || agep == 366*2 || agep == 366*3 || agep == 366*4)
			v.reliability_spd_dec <<= 1;

		Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);

		if (agep == -366) {
			ShowVehicleGettingOld(v, Str.STR_01A0_IS_GETTING_OLD);
		} else if (agep == 0) {
			ShowVehicleGettingOld(v, Str.STR_01A1_IS_GETTING_VERY_OLD);
		} else if (agep == 366*1 || agep == 366*2 || agep == 366*3 || agep == 366*4 || agep == 366*5) {
			ShowVehicleGettingOld(v, Str.STR_01A2_IS_GETTING_VERY_OLD_AND);
		}
	}

	/** Clone a vehicle. If it is a train, it will clone all the cars too
	 * @param x,y depot where the cloned vehicle is build
	 * @param p1 the original vehicle's index
	 * @param p2 1 = shared orders, else copied orders
	 */
	static int CmdCloneVehicle(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v_front, v;
		Vehicle w_front, w, w_rear;
		int cost, total_cost = 0;

		if (!IsVehicleIndex(p1)) return Cmd.CMD_ERROR;
		v = Vehicle.GetVehicle(p1);
		v_front = v;
		//w = null;
		w_front = null;
		w_rear = null;


		/*
		 * v_front is the front engine in the original vehicle
		 * v is the car/vehicle of the original vehicle, that is currently being copied
		 * w_front is the front engine of the cloned vehicle
		 * w is the car/vehicle currently being cloned
		 * w_rear is the rear end of the cloned train. It's used to add more cars and is only used by trains
		 */

		if (!Player.CheckOwnership(PlayerID.get( v.owner.id ) )) return Cmd.CMD_ERROR;

		if (v.type == VEH_Train && !v.IsFrontEngine()) return Cmd.CMD_ERROR;

		// check that we can allocate enough vehicles
		if (0 == (flags & Cmd.DC_EXEC)) {
			int veh_counter = 0;
			do {
				veh_counter++;
			} while ((v = v.next) != null);

			if (!AllocateVehicles(null, veh_counter)) {
				return Cmd.return_cmd_error(Str.STR_00E1_TOO_MANY_VEHICLES_IN_GAME);
			}
		}

		v = v_front;

		do {

			if (v.IsMultiheaded() && !v.IsTrainEngine()) {
				/* we build the rear ends of multiheaded trains with the front ones */
				continue;
			}

			cost = Cmd.DoCommand(x, y, v.engine_type.id, 1, flags, CMD_BUILD_VEH(v.type));

			if (Cmd.CmdFailed(cost)) return cost;

			total_cost += cost;

			if(0 != (flags & Cmd.DC_EXEC)) {
				w = GetVehicle(Global._new_vehicle_id);

				if (v.type != VEH_Road) { // road vehicles can't be refitted
					if (v.cargo_type != w.cargo_type) {
						Cmd.DoCommand(x, y, w.index, v.cargo_type, flags, CMD_REFIT_VEH(v.type));
					}
				}

				if (v.type == VEH_Train && !v.IsFrontEngine()) {
					// this s a train car
					// add this unit to the end of the train
					Cmd.DoCommand(x, y, (w_rear.index << 16) | w.index, 1, flags, Cmd.CMD_MOVE_RAIL_VEHICLE);
				} else {
					// this is a front engine or not a train. It need orders
					w_front = w;
					Cmd.DoCommand(x, y, (v.index << 16) | w.index, (p2 & 1) != 0 ? Order.CO_SHARE : Order.CO_COPY, flags, Cmd.CMD_CLONE_ORDER);
				}
				w_rear = w;	// trains needs to know the last car in the train, so they can add more in next loop
			}
		} while (v.type == VEH_Train && (v = v.GetNextVehicle()) != null);

		if( (0 != (flags & Cmd.DC_EXEC) && v_front.type == VEH_Train)) {
			// _new_train_id needs to be the front engine due to the callback function
			Global._new_train_id = VehicleID.get( w_front.index );
		}
		return total_cost;
	}

	/*
	 * move the cargo from one engine to another if possible
	 */
	static void MoveVehicleCargo(Vehicle dest, Vehicle source)
	{
		Vehicle v = dest;
		int units_moved;

		do {
			do {
				if (source.cargo_type != dest.cargo_type)
					continue;	// cargo not compatible

				if (dest.cargo_count == dest.cargo_cap)
					continue;	// the destination vehicle is already full

				units_moved = Math.min(source.cargo_count, dest.cargo_cap - dest.cargo_count);
				source.cargo_count -= units_moved;
				dest.cargo_count   += units_moved;
				dest.cargo_source   = source.cargo_source;

				// copy the age of the cargo
				dest.cargo_days   = source.cargo_days;
				dest.day_counter  = source.day_counter;
				dest.tick_counter = source.tick_counter;

			} while (source.cargo_count > 0 && (dest = dest.next) != null);
			dest = v;
		} while ((source = source.next) != null);
	}

	/* Replaces a vehicle (used to be called autorenew)
	 * This function is only called from MaybeReplaceVehicle()
	 * Must be called with _current_player set to the owner of the vehicle
	 * @param w pointer to (1-item array) Vehicle to replace
	 * @param flags is the flags to use when calling Cmd.DoCommand(). Mainly DC_EXEC counts
	 * @return value is cost of the replacement or Cmd.CMD_ERROR
	 */
	static int ReplaceVehicle(Vehicle [] w, byte flags)
	{
		int cost;
		Vehicle old_v = w[0];
		final Player p = Player.GetPlayer(old_v.owner);
		EngineID new_engine_type;
		final UnitID cached_unitnumber = old_v.unitnumber;
		boolean new_front = false;
		Vehicle new_v = null;
		String vehicle_name = null;

		new_engine_type = p.EngineReplacement(old_v.engine_type);
		if (new_engine_type.id == INVALID_ENGINE) new_engine_type = old_v.engine_type;

		cost = Cmd.DoCommand(old_v.x_pos, old_v.y_pos, new_engine_type.id, 1, flags, CMD_BUILD_VEH(old_v.type));
		if (Cmd.CmdFailed(cost)) return cost;

		if(0 != (flags & Cmd.DC_EXEC)) {
			new_v = Vehicle.GetVehicle(Global._new_vehicle_id);
			w[0] = new_v;	//we changed the vehicle, so MaybeReplaceVehicle needs to work on the new one. Now we tell it what the new one is

			/* refit if needed */
			if (new_v.type != VEH_Road) { // road vehicles can't be refitted
				if (old_v.cargo_type != new_v.cargo_type && old_v.cargo_cap != 0 && new_v.cargo_cap != 0) {// some train engines do not have cargo capacity
					Cmd.DoCommand(0, 0, new_v.index, old_v.cargo_type, Cmd.DC_EXEC, CMD_REFIT_VEH(new_v.type));
				}
			}


			if (old_v.type == VEH_Train && !old_v.IsFrontEngine()) {
				/* this is a railcar. We need to move the car into the train
				 * We add the new engine after the old one instead of replacing it. It will give the same result anyway when we
				 * sell the old engine in a moment
				 */
				Cmd.DoCommand(0, 0, (old_v.GetPrevVehicleInChain().index << 16) | new_v.index, 1, Cmd.DC_EXEC, Cmd.CMD_MOVE_RAIL_VEHICLE);
				/* Now we move the old one out of the train */
				Cmd.DoCommand(0, 0, (INVALID_VEHICLE << 16) | old_v.index, 0, Cmd.DC_EXEC, Cmd.CMD_MOVE_RAIL_VEHICLE);
			} else {
				// copy/clone the orders
				Cmd.DoCommand(0, 0, (old_v.index << 16) | new_v.index, old_v.IsOrderListShared() ? Order.CO_SHARE : Order.CO_COPY, Cmd.DC_EXEC, Cmd.CMD_CLONE_ORDER);
				new_v.cur_order_index = old_v.cur_order_index;
				VehicleGui.ChangeVehicleViewWindow(old_v, new_v);
				new_v.profit_this_year = old_v.profit_this_year;
				new_v.profit_last_year = old_v.profit_last_year;
				new_front = true;

				new_v.current_order = old_v.current_order;
				if (old_v.type == VEH_Train){
					// move the entire train to the new engine, including the old engine. It will be sold in a moment anyway
					if (old_v.GetNextVehicle() != null) {
						Cmd.DoCommand(0, 0, (new_v.index << 16) | old_v.GetNextVehicle().index, 1, Cmd.DC_EXEC, Cmd.CMD_MOVE_RAIL_VEHICLE);
					}
					new_v.rail.shortest_platform[0] = old_v.rail.shortest_platform[0];
					new_v.rail.shortest_platform[1] = old_v.rail.shortest_platform[1];
				}
			}
			/* We are done setting up the new vehicle. Now we move the cargo from the old one to the new one */
			MoveVehicleCargo(new_v.type == VEH_Train ? new_v.GetFirstVehicleInChain() : new_v, old_v);

			// Get the name of the old vehicle if it has a custom name.
			if ((old_v.string_id & 0xF800) != 0x7800) {
				vehicle_name = null;
			} else {
				vehicle_name = Global.GetName(old_v.string_id & 0x7FF);
			}
		}

		// sell the engine/ find out how much you get for the old engine
		cost += Cmd.DoCommand(0, 0, old_v.index, 0, flags, CMD_SELL_VEH(old_v.type));

		if (new_front) {
			// now we assign the old unitnumber to the new vehicle
			new_v.unitnumber = cached_unitnumber;
		}

		// Transfer the name of the old vehicle.
		if ( (0 != (flags & Cmd.DC_EXEC)) && vehicle_name != null) {
			Global._cmd_text = vehicle_name;
			Cmd.DoCommand(0, 0, new_v.index, 0, Cmd.DC_EXEC, Cmd.CMD_NAME_VEHICLE);
		}

		return cost;
	}

	/** replaces a vehicle if it's set for autoreplace or is too old
	 * (used to be called autorenew)
	 * @param v The vehicle to replace
	 *	if the vehicle is a train, v needs to be the front engine
	 *	return value is a pointer to the new vehicle, which is the same as the argument if nothing happened
	 * @return 
	 */
	static int MaybeReplaceVehicle(Vehicle v)
	{
		Vehicle w;
		final Player p = Player.GetPlayer(v.owner);
		byte flags = 0;
		int cost, temp_cost = 0;
		boolean stopped = false;
		boolean train_fits_in_station = false;


		PlayerID.setCurrent( v.owner );

		assert(v.type == VEH_Train || v.type == VEH_Road || v.type == VEH_Ship || v.type == VEH_Aircraft);

		assert v.isStopped();	// the vehicle should have been stopped in VehicleEnteredDepotThisTick() if needed

		if (v.leave_depot_instantly) {
			// we stopped the vehicle to do this, so we have to remember to start it again when we are done
			// we need to store this info as the engine might be replaced and lose this info
			stopped = true;
		}

		if (
				v.type == VEH_Train && 
				v.rail.shortest_platform[0]*16 <= v.rail.cached_total_length && 
				Player.GetPlayer(v.owner).isRenew_keep_length()) 
		{
			// the train is not too long for the stations it visits. We should try to keep it that way if we change anything
			train_fits_in_station = true;
		}

		for (;;) {
			cost = 0;
			w = v;
			do {
				if (w.type == VEH_Train && w.IsMultiheaded() && !w.IsTrainEngine()) {
					/* we build the rear ends of multiheaded trains with the front ones */
					continue;
				}

				// check if the vehicle should be replaced
				if (!p.isEngine_renew() ||
						w.age - w.max_age < (p.getEngine_renew_months() * 30) || // replace if engine is too old
						w.max_age == 0) { // rail cars got a max age of 0
					if (!p.EngineHasReplacement(w.engine_type)) // updates to a new model
						continue;
				}

				/* Now replace the Vehicle */
				Vehicle[] repl = { w };
				temp_cost = ReplaceVehicle(repl, flags);
				w = repl[0];

				if ( (0 != (flags & Cmd.DC_EXEC)) &&
						(w.type != VEH_Train || w.rail.first_engine.id == INVALID_VEHICLE)) {
					/* now we bought a new engine and sold the old one. We need to fix the
					 * pointers in order to avoid pointing to the old one for trains: these
					 * pointers should point to the front engine and not the cars
					 */
					v = w;
				}

				if (Cmd.CmdFailed(temp_cost)) break;

				cost += temp_cost;
			} while (w.type == VEH_Train && (w = w.GetNextVehicle()) != null);

			if (0 == (flags & Cmd.DC_EXEC) && (Cmd.CmdFailed(temp_cost) || p.money64 < (int)(cost + p.getEngine_renew_money()) || cost == 0)) {
				if (p.money64 < (int)(cost + p.getEngine_renew_money()) && ( Global.gs._local_player.equals(v.owner) ) && cost != 0) {
					int message;
					Global.SetDParam(0, v.unitnumber.id);
					switch (v.type) {
					case VEH_Train:    message = Str.STR_TRAIN_AUTORENEW_FAILED;       break;
					case VEH_Road:     message = Str.STR_ROADVEHICLE_AUTORENEW_FAILED; break;
					case VEH_Ship:     message = Str.STR_SHIP_AUTORENEW_FAILED;        break;
					case VEH_Aircraft: message = Str.STR_AIRCRAFT_AUTORENEW_FAILED;    break;
					// This should never happen
					default: assert false; message = 0; break;
					}

					NewsItem.AddNewsItem(message, NewsItem.NEWS_FLAGS(NewsItem.NM_SMALL, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ADVICE, 0), v.index, 0);
				}
				if (stopped) v.setStopped(false);
				PlayerID.setCurrentToNone();
				return 0;
			}

			//MA CHECKS
			if(mAirport.MA_VehicleServesMS(v) > 0) 
			{
				for(int i =  1; i <= mAirport.MA_VehicleServesMS(v) ; i++) {
					Station st = Station.GetStation(mAirport.MA_Find_MS_InVehicleOrders(v, i).id);
					if(!mAirport.MA_WithinVehicleQuota(st)) {
						Global._error_message = Str.STR_MA_EXCEED_MAX_QUOTA;
						return Cmd.CMD_ERROR;
					}
				}
				//END MA CHECKS

			}
			if( 0 != (flags & Cmd.DC_EXEC)) {
				break;	// we are done replacing since the loop ran once with DC_EXEC
			}
			// now we redo the loop, but this time we actually do stuff since we know that we can do it
			flags |= Cmd.DC_EXEC;
		}

		if (train_fits_in_station) {
			// the train fitted in the stations it got in it's orders, so we should make sure that it still do
			Vehicle temp;
			w = v;
			while (v.rail.shortest_platform[0]*16 < v.rail.cached_total_length) {
				// the train is too long. We will remove cars one by one from the start of the train until it's short enough
				while (w != null && !(Engine.RailVehInfo(w.engine_type.id).isWagon()) ) {
					w = w.GetNextVehicle();
				}
				if (w == null) {
					// we failed to make the train short enough
					Global.SetDParam(0, v.unitnumber.id);
					NewsItem.AddNewsItem(Str.STR_TRAIN_TOO_LONG_AFTER_REPLACEMENT, NewsItem.NEWS_FLAGS(NewsItem.NM_SMALL, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ADVICE, 0), v.index, 0);
					break;
				}
				temp = w;
				w = w.GetNextVehicle();
				Cmd.DoCommand(0, 0, (INVALID_VEHICLE << 16) | temp.index, 0, Cmd.DC_EXEC, Cmd.CMD_MOVE_RAIL_VEHICLE);
				MoveVehicleCargo(v, temp);
				cost += Cmd.DoCommand(0, 0, temp.index, 0, flags, CMD_SELL_VEH(temp.type));
			}
		}

		if (Player.IsLocalPlayer()) MiscGui.ShowCostOrIncomeAnimation(v.x_pos, v.y_pos, v.z_pos, cost);

		if (stopped) v.setStopped(false);
		PlayerID.setCurrentToNone();

		return 0;
	}


	/** Give a custom name to your vehicle
	 * @param x,y unused
	 * @param p1 vehicle ID to name
	 * @param p2 unused
	 */
	public static int CmdNameVehicle(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;

		if (!IsVehicleIndex(p1) || Global._cmd_text == null) return Cmd.CMD_ERROR;
		v = GetVehicle(p1);
		if (!Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		StringID str = Global.AllocateNameUnique(Global._cmd_text, 2);
		//if (str == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			StringID old_str = new StringID( v.string_id );
			v.string_id = str.id;
			Global.DeleteName(old_str);
			VehicleGui.ResortVehicleLists();
			Hal.MarkWholeScreenDirty();
		} else {
			Global.DeleteName(str);
		}

		return 0;
	}



	static final Rect _old_vehicle_coords = new Rect();

	public void BeginVehicleMove() {
		_old_vehicle_coords.left = left_coord;
		_old_vehicle_coords.top = top_coord;
		_old_vehicle_coords.right = right_coord;
		_old_vehicle_coords.bottom = bottom_coord;
	}

	public void EndVehicleMove()
	{
		ViewPort.MarkAllViewportsDirty(
				Math.min(_old_vehicle_coords.left,left_coord),
				Math.min(_old_vehicle_coords.top,top_coord),
				Math.max(_old_vehicle_coords.right,right_coord)+1,
				Math.max(_old_vehicle_coords.bottom,bottom_coord)+1
				);
		//Hal.MarkWholeScreenDirty();

	}

	static final int _delta_coord[] = {
			-1,-1,-1, 0, 1, 1, 1, 0, /* x */
			-1, 0, 1, 1, 1, 0,-1,-1, /* y */
	};

	/* returns true if staying in the same tile */
	boolean GetNewVehiclePos(GetNewVehiclePosResult gp)
	{

		int x = x_pos + _delta_coord[direction];
		int y = y_pos + _delta_coord[direction + 8];

		gp.x = x;
		gp.y = y;
		gp.old_tile = tile;
		gp.new_tile = TileIndex.TileVirtXY(x, y);
		return gp.old_tile.equals(gp.new_tile);
	}

	static final byte _new_direction_table[] = {
			0, 7, 6,
			1, 3, 5,
			2, 3, 4,
	};

	static int GetDirectionTowards(final Vehicle v, int x, int y)
	{
		int dirdiff, dir;
		int i = 0;

		if (y >= v.y_pos) {
			if (y != v.y_pos) i+=3;
			i+=3;
		}

		if (x >= v.x_pos) {
			if (x != v.x_pos) i++;
			i++;
		}

		dir = v.direction;

		dirdiff =  (_new_direction_table[i] - dir);
		if (dirdiff == 0)
			return dir;
		return  ((dir+((dirdiff&7)<5?1:-1)) & 7);
		//return _new_direction_table[i];
	}

	/* Trackdir */ int GetVehicleTrackdir()
	{
		//final Vehicle  v = this;

		if(isCrashed())
			return Rail.INVALID_TRACKDIR;
		//return Trackdir.INVALID_TRACKDIR;

		switch(type)
		{
		case VEH_Train:
			if (rail.isInDepot()) /* We'll assume the train is facing outwards */
				return Rail.DiagdirToDiagTrackdir(Depot.GetDepotDirection(tile, TransportType.Rail)); /* Train in depot */

			if (rail.isInTunnel()) /* train in tunnel, so just use his direction and assume a diagonal track */
				return Rail.DiagdirToDiagTrackdir((direction >> 1) & 3);

			return Rail.TrackDirectionToTrackdir(BitOps.FIND_FIRST_BIT(rail.track),direction);

		case VEH_Ship:
			//if (v.ship.state == 0x80)  /* Inside a depot? */
			if (ship.isInDepot())  /* Inside a depot? */
				/* We'll assume the ship is facing outwards */
				return Rail.DiagdirToDiagTrackdir(Depot.GetDepotDirection(tile, TransportType.Water)); /* Ship in depot */

			return Rail.TrackDirectionToTrackdir(BitOps.FIND_FIRST_BIT(ship.state),direction);

		case VEH_Road:
			if (road.isInDepot()) /* We'll assume the road vehicle is facing outwards */
				return Rail.DiagdirToDiagTrackdir(Depot.GetDepotDirection(tile, TransportType.Road)); /* Road vehicle in depot */

			if (tile.IsRoadStationTile()) /* We'll assume the road vehicle is facing outwards */
				return Rail.DiagdirToDiagTrackdir(Station.GetRoadStationDir(tile)); /* Road vehicle in a station */

			return Rail.DiagdirToDiagTrackdir((direction >> 1) & 3);

			/* case VEH_Aircraft: case VEH_Special: case VEH_Disaster: */
		default: return 0xFF;
		}
	}
	/**
	 * Return value has bit 0x2 set, when the vehicle enters a station. Then,
	 * result << 8 contains the id of the station entered. If the return value has
	 * bit 0x8 set, the vehicle could not and did not enter the tile. Are there
	 * other bits that can be set?
	 * 
	 *  @param itile tile we are entering
	 *  
	**/
	int VehicleEnterTile(TileIndex itile, int x, int y)
	{
		TileIndex old_tile = tile;
		final int ordinal = itile.GetTileType().ordinal();
		final TileTypeProcs func = Landscape._tile_type_procs[ordinal];
		int result = func.vehicle_enter_tile_proc.apply(this, itile, x, y);

		/* When vehicle_enter_tile_proc returns 8, that apparently means that
		 * we cannot enter the tile at all. In that case, don't call
		 * leave_tile. */
		if (0 == (result & 8) && !old_tile.equals(itile)) {
			TileVehicleInterface proc = Landscape._tile_type_procs[old_tile.GetTileType().ordinal()].vehicle_leave_tile_proc;
			if (proc != null)
				proc.apply(this, old_tile, x, y);
		}
		return result;
	}

	public static UnitID GetFreeUnitNumber(int type)
	{
		int unit_num = 0;

		boolean restart; // = false;
		while(true)
		{
			unit_num++;
			restart = false;

			for( Iterator<Vehicle> i = Global.gs._vehicles.getIterator(); i.hasNext(); )
			{
				Vehicle u = i.next();

				if (u.type == type && u.owner.isCurrentPlayer() 
						&& u.unitnumber != null
						&& unit_num == u.unitnumber.id)
				{
					restart = true;
					break;
				}
			}
			if(!restart) break;
		}

		return UnitID.get(unit_num);
	}


	// ----------------------------- Orders

	/**
	 *
	 * Check if a vehicle has any valid orders
	 *
	 * @return false if there are no valid orders
	 *
	 */
	public boolean CheckForValidOrders()
	{
		for(Order order = orders; order != null; order = order.next )
			if (order.type != Order.OT_DUMMY) 
				return true;

		return false;
	}



	/**
	 *
	 * Delete all orders from a vehicle
	 *
	 */
	public void DeleteVehicleOrders()
	{
		Order order, cur;

		if(orders == null) return;
		
		/* If we have a shared order-list, don't delete the list, but just
		    remove our pointer */
		if (Order.IsOrderListShared(this)) {
			Vehicle u = this;

			orders = null;
			num_orders = 0;

			/* Unlink ourself */
			if (prev_shared != null) {
				prev_shared.next_shared = next_shared;
				u = prev_shared;
			}
			if (next_shared != null) {
				next_shared.prev_shared = prev_shared;
				u = next_shared;
			}
			prev_shared = null;
			next_shared = null;

			/* We only need to update this-one, because if there is a third
			    vehicle which shares the same order-list, nothing will change. If
			    this is the last vehicle, the last line of the order-window
			    will change from Shared order list, to Order list, so it needs
			    an update */
			u.InvalidateVehicleOrder();
			return;
		}

		/* Remove the orders */
		cur = orders;
		orders = null;
		num_orders = 0;

		if (type == VEH_Aircraft) {
			/* Take out of airport queue
			 */
			if(queue_item != null)
			{
				queue_item.queue.del(this);
			}
		}

		order = null;
		while (cur != null) {
			if (order != null) {
				order.type = Order.OT_NOTHING;
				order.next = null;
			}

			order = cur;
			cur = cur.next;
		}

		if (order != null) {
			order.type = Order.OT_NOTHING;
			order.next = null;
		}
	}

	/**
	 *
	 * Checks if a vehicle has a GOTO_DEPOT in his order list
	 *
	 * @return True if this is true (lol ;))
	 *
	 */
	public boolean VehicleHasDepotOrders()
	{
		for(Order order = orders; order != null; order = order.next )
		{
			if (order.type == Order.OT_GOTO_DEPOT)
				return true;
		}

		return false;
	}



	/**
	 *
	 * Updates the widgets of a vehicle which contains the order-data
	 *
	 */
	public void InvalidateVehicleOrder()
	{
		Window.InvalidateWindow(Window.WC_VEHICLE_VIEW,   index);
		Window.InvalidateWindow(Window.WC_VEHICLE_ORDERS, index);
	}



	/**
	 *
	 * Backup a vehicle order-list, so you can replace a vehicle
	 *  without loosing the order-list
	 *
	 */
	public static void BackupVehicleOrders(final Vehicle v, BackuppedOrders bak)
	{
		/* Save general info */
		bak.currentOrderIndex = v.cur_order_index;
		bak.service_interval  = v.service_interval;

		/* Safe custom string, if any */
		if ((v.string_id & 0xF800) != 0x7800) {
			bak.name = null;
		} else {
			bak.name = Global.GetName(v.string_id & 0x7FF);
		}

		/* If we have shared orders, store it on a special way */
		if (v.IsOrderListShared()) {
			final Vehicle u = (v.next_shared != null) ? v.next_shared : v.prev_shared;

			bak.clone = VehicleID.get( u.index );
		} else {
			/* Else copy the orders */

			/* We do not have shared orders */
			bak.clone = VehicleID.getInvalid();

			/* Copy the orders */
			for(Order order = v.orders; order != null; order = order.next )
				bak.order.add(new Order(order) );
			
			/* End the list with an Order.OT_NOTHING [dz] no, not an array, no need for marker
			//dest.type = Order.OT_NOTHING;
			//dest.next = null;
			Order empty_order = new Order(Order.OT_NOTHING);
			//empty_order.type = Order.OT_NOTHING;
			//empty_order.next = null;
			bak.order.add(empty_order);
			*/
		}
	}

	public boolean IsOrderListShared() {
		return Order.IsOrderListShared(this);
	}

	/**
	 *
	 * Restore vehicle orders that are backupped via BackupVehicleOrders
	 *
	 */
	public static void RestoreVehicleOrders(final Vehicle  v, final BackuppedOrders bak)
	{
		/* If we have a custom name, process that */
		if (bak.name != null) {
			Global._cmd_text = bak.name;
			Cmd.DoCommandP(null, v.index, 0, null, Cmd.CMD_NAME_VEHICLE);
		}

		/* If we had shared orders, recover that */
		if (bak.clone.id != INVALID_VEHICLE) {
			Cmd.DoCommandP(null, v.index | (bak.clone.id << 16), 0, null, Cmd.CMD_CLONE_ORDER);
			return;
		}

		/* CMD_NO_TEST_IF_IN_NETWORK is used here, because CMD_INSERT_ORDER checks if the
		    order number is one more than the current amount of orders, and because
		    in network the commands are queued before send, the second insert always
		    fails in test mode. By bypassing the test-mode, that no longer is a problem. */
		//for (i = 0; bak.order[i].type != Order.OT_NOTHING; i++)
		int i = 0;
		for( Order bo : bak.order)
		{
			//if (!Cmd.DoCommandP(0, v.index + (i << 16), PackOrder(bak.order[i]), null, CMD_INSERT_ORDER | CMD_NO_TEST_IF_IN_NETWORK))
			if (!Cmd.DoCommandP(null, v.index + (i << 16), Order.PackOrder(bo), null, Cmd.CMD_INSERT_ORDER | Cmd.CMD_NO_TEST_IF_IN_NETWORK))
				break;
			i++;
		}

		/* Restore vehicle order-index and service interval */
		Cmd.DoCommandP(null, v.index, bak.currentOrderIndex | (bak.service_interval << 16) , null, Cmd.CMD_RESTORE_ORDER_INDEX);
	}


	@Override
	public void setIndex(int index) {
		this.index = index;		
	}

	public Iterator<Order> getOrdersIterator() 
	{
		return new Iterator<Order>() 
		{
			Order curr = orders; 
			
			@Override
			public boolean hasNext() {
				return curr != null;
			}

			@Override
			public Order next() 
			{
				if( curr == null ) 
					throw new NoSuchElementException();
				
				Order ret = curr;
				curr = curr.next;
				return ret;
			}

		};
	}

	//public void TriggerVehicle(VehicleTrigger trigger)
	public void TriggerVehicle(int trigger)
	{
		Engine.TriggerVehicle(this, trigger);
	}

	public TileIndex GetStationTileForVehicle(Station st) {
		return Station.GetStationTileForVehicle(this, st);
	}

	public void forEachWagon(Consumer<Vehicle> c) 
	{
		Vehicle v = GetFirstVehicleInChain();

		while(v != null) 
		{ 
			c.accept(v); 
			v = v.next;  
		}

	}

	public int getType() { return type; }
	public int getSubtype() { return subtype;	}
	public PlayerID getOwner() {		return owner;	}

	
	public TileIndex getTile() { return tile; }

	/*
	// Save and load of vehicles
	final SaveLoad _common_veh_desc[] = {
		SLE_VAR(Vehicle,subtype,					SLE_UINT8),

		SLE_REF(Vehicle,next,							REF_VEHICLE_OLD),
		SLE_VAR(Vehicle,string_id,				SLE_STRINGID),
		SLE_CONDVAR(Vehicle,unitnumber,				SLE_FILE_U8 | SLE_VAR_U16, 0, 7),
		SLE_CONDVAR(Vehicle,unitnumber,				SLE_UINT16, 8, 255),
		SLE_VAR(Vehicle,owner,						SLE_UINT8),
		SLE_CONDVAR(Vehicle,tile,					SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Vehicle,tile,					SLE_UINT32, 6, 255),
		SLE_CONDVAR(Vehicle,dest_tile,		SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Vehicle,dest_tile,		SLE_UINT32, 6, 255),

		SLE_CONDVAR(Vehicle,x_pos,				SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Vehicle,x_pos,				SLE_UINT32, 6, 255),
		SLE_CONDVAR(Vehicle,y_pos,				SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Vehicle,y_pos,				SLE_UINT32, 6, 255),
		SLE_VAR(Vehicle,z_pos,						SLE_UINT8),
		SLE_VAR(Vehicle,direction,				SLE_UINT8),

		SLE_VAR(Vehicle,cur_image,				SLE_UINT16),
		SLE_VAR(Vehicle,spritenum,				SLE_UINT8),
		SLE_VAR(Vehicle,sprite_width,			SLE_UINT8),
		SLE_VAR(Vehicle,sprite_height,		SLE_UINT8),
		SLE_VAR(Vehicle,z_height,					SLE_UINT8),
		SLE_VAR(Vehicle,x_offs,						SLE_INT8),
		SLE_VAR(Vehicle,y_offs,						SLE_INT8),
		SLE_VAR(Vehicle,engine_type,			SLE_UINT16),

		SLE_VAR(Vehicle,max_speed,				SLE_UINT16),
		SLE_VAR(Vehicle,cur_speed,				SLE_UINT16),
		SLE_VAR(Vehicle,subspeed,					SLE_UINT8),
		SLE_VAR(Vehicle,acceleration,			SLE_UINT8),
		SLE_VAR(Vehicle,progress,					SLE_UINT8),

		SLE_VAR(Vehicle,vehstatus,				SLE_UINT8),
		SLE_CONDVAR(Vehicle,last_station_visited, SLE_FILE_U8 | SLE_VAR_U16, 0, 4),
		SLE_CONDVAR(Vehicle,last_station_visited, SLE_UINT16, 5, 255),

		SLE_VAR(Vehicle,cargo_type,				SLE_UINT8),
		SLE_VAR(Vehicle,cargo_days,				SLE_UINT8),
		SLE_CONDVAR(Vehicle,cargo_source,			SLE_FILE_U8 | SLE_VAR_U16, 0, 6),
		SLE_CONDVAR(Vehicle,cargo_source,			SLE_UINT16, 7, 255),
		SLE_VAR(Vehicle,cargo_cap,				SLE_UINT16),
		SLE_VAR(Vehicle,cargo_count,			SLE_UINT16),

		SLE_VAR(Vehicle,day_counter,			SLE_UINT8),
		SLE_VAR(Vehicle,tick_counter,			SLE_UINT8),

		SLE_VAR(Vehicle,cur_order_index,	SLE_UINT8),
		SLE_VAR(Vehicle,num_orders,				SLE_UINT8),

		//* This next line is for version 4 and prior compatibility.. it temporarily reads
		//    type and flags (which were both 4 bits) into type. Later on this is
		//    converted correctly 
		SLE_CONDVARX(offsetof(Vehicle, current_order) + offsetof(Order, type),    SLE_UINT8,  0, 4),
		SLE_CONDVARX(offsetof(Vehicle, current_order) + offsetof(Order, station), SLE_FILE_U8 | SLE_VAR_U16, 0, 4),

		// Orders for version 5 and on 
		SLE_CONDVARX(offsetof(Vehicle, current_order) + offsetof(Order, type),    SLE_UINT8,  5, 255),
		SLE_CONDVARX(offsetof(Vehicle, current_order) + offsetof(Order, flags),   SLE_UINT8,  5, 255),
		SLE_CONDVARX(offsetof(Vehicle, current_order) + offsetof(Order, station), SLE_UINT16, 5, 255),

		SLE_REF(Vehicle,orders,						REF_ORDER),

		SLE_VAR(Vehicle,age,							SLE_UINT16),
		SLE_VAR(Vehicle,max_age,					SLE_UINT16),
		SLE_VAR(Vehicle,date_of_last_service,SLE_UINT16),
		SLE_VAR(Vehicle,service_interval,	SLE_UINT16),
		SLE_VAR(Vehicle,reliability,			SLE_UINT16),
		SLE_VAR(Vehicle,reliability_spd_dec,SLE_UINT16),
		SLE_VAR(Vehicle,breakdown_ctr,		SLE_UINT8),
		SLE_VAR(Vehicle,breakdown_delay,	SLE_UINT8),
		SLE_VAR(Vehicle,breakdowns_since_last_service,	SLE_UINT8),
		SLE_VAR(Vehicle,breakdown_chance,	SLE_UINT8),
		SLE_VAR(Vehicle,build_year,				SLE_UINT8),

		SLE_VAR(Vehicle,load_unload_time_rem,	SLE_UINT16),

		SLE_VAR(Vehicle,profit_this_year,	SLE_INT32),
		SLE_VAR(Vehicle,profit_last_year,	SLE_INT32),
		SLE_VAR(Vehicle,value,						SLE_UINT32),

		SLE_VAR(Vehicle,random_bits,       SLE_UINT8),
		SLE_VAR(Vehicle,waiting_triggers,  SLE_UINT8),

		SLE_REF(Vehicle,next_shared,				REF_VEHICLE),
		SLE_REF(Vehicle,prev_shared,				REF_VEHICLE),

		// reserve extra space in savegame here. (currently 10 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U8 | SLE_VAR_NULL, 2, 2, 255), // 2 
		SLE_CONDARR(NullStruct,null,SLE_FILE_U32 | SLE_VAR_NULL, 2, 2, 255), // 8 

		SLE_END()
	};


	static final SaveLoad _train_desc[] = {
		SLE_WRITEBYTE(Vehicle,type,VEH_Train, 0), // Train type. VEH_Train in mem, 0 in file.
		SLE_INCLUDEX(0, INC_VEHICLE_COMMON),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRail,crash_anim_pos), SLE_UINT16),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRail,force_proceed), SLE_UINT8),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRail,railtype), SLE_UINT8),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRail,track), SLE_UINT8),

		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleRail,flags), SLE_UINT8, 2, 255),
		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleRail,days_since_order_progr), SLE_UINT16, 2, 255),

		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleRail,pbs_status), SLE_UINT8, 2, 255),
		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleRail,pbs_end_tile), SLE_UINT32, 2, 255),
		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleRail,pbs_end_trackdir), SLE_UINT8, 2, 255),
		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleRail,shortest_platform[0]), SLE_UINT8, 2, 255),	// added with 16.1, but was blank since 2
		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleRail,shortest_platform[1]), SLE_UINT8, 2, 255),	// added with 16.1, but was blank since 2
		SLE_CONDREFX(offsetof(Vehicle,u)+offsetof(VehicleRail,other_multiheaded_part), REF_VEHICLE, 2, 255),	// added with 17.1, but was blank since 2
		// reserve extra space in savegame here. (currently 3 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U8 | SLE_VAR_NULL, 3, 2, 255),

		SLE_END()
	};

	static final SaveLoad _roadveh_desc[] = {
		SLE_WRITEBYTE(Vehicle,type,VEH_Road, 1), // Road type. VEH_Road in mem, 1 in file.
		SLE_INCLUDEX(0, INC_VEHICLE_COMMON),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRoad,state),					SLE_UINT8),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRoad,frame),					SLE_UINT8),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRoad,unk2),					SLE_UINT16),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRoad,overtaking),		SLE_UINT8),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRoad,overtaking_ctr),SLE_UINT8),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRoad,crashed_ctr),		SLE_UINT16),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleRoad,reverse_ctr),			SLE_UINT8),

		SLE_CONDREFX(offsetof(Vehicle,u)+offsetof(VehicleRoad,slot), REF_ROADSTOPS, 6, 255),
		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleRoad,slotindex), SLE_UINT8, 6, 255),
		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleRoad,slot_age), SLE_UINT8, 6, 255),
		// reserve extra space in savegame here. (currently 16 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U64 | SLE_VAR_NULL, 2, 2, 255),

		SLE_END()
	};

	static final SaveLoad _ship_desc[] = {
		SLE_WRITEBYTE(Vehicle,type,VEH_Ship, 2), // Ship type. VEH_Ship in mem, 2 in file.
		SLE_INCLUDEX(0, INC_VEHICLE_COMMON),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleShip,state),				SLE_UINT8),

		// reserve extra space in savegame here. (currently 16 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U64 | SLE_VAR_NULL, 2, 2, 255),

		SLE_END()
	};

	static final SaveLoad _aircraft_desc[] = {
		SLE_WRITEBYTE(Vehicle,type,VEH_Aircraft, 3), // Aircraft type. VEH_Aircraft in mem, 3 in file.
		SLE_INCLUDEX(0, INC_VEHICLE_COMMON),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleAir,crashed_counter),	SLE_UINT16),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleAir,pos),							SLE_UINT8),

		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleAir,targetairport),		SLE_FILE_U8 | SLE_VAR_U16, 0, 4),
		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleAir,targetairport),		SLE_UINT16, 5, 255),

		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleAir,state),						SLE_UINT8),

		SLE_CONDVARX(offsetof(Vehicle,u)+offsetof(VehicleAir,previous_pos),			SLE_UINT8, 2, 255),

		// reserve extra space in savegame here. (currently 15 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U8 | SLE_VAR_NULL, 15, 2, 255),

		SLE_END()
	};

	static final SaveLoad _special_desc[] = {
		SLE_WRITEBYTE(Vehicle,type,VEH_Special, 4),

		SLE_VAR(Vehicle,subtype,					SLE_UINT8),

		SLE_CONDVAR(Vehicle,tile,					SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Vehicle,tile,					SLE_UINT32, 6, 255),

		SLE_CONDVAR(Vehicle,x_pos,				SLE_FILE_I16 | SLE_VAR_I32, 0, 5),
		SLE_CONDVAR(Vehicle,x_pos,				SLE_INT32, 6, 255),
		SLE_CONDVAR(Vehicle,y_pos,				SLE_FILE_I16 | SLE_VAR_I32, 0, 5),
		SLE_CONDVAR(Vehicle,y_pos,				SLE_INT32, 6, 255),
		SLE_VAR(Vehicle,z_pos,						SLE_UINT8),

		SLE_VAR(Vehicle,cur_image,				SLE_UINT16),
		SLE_VAR(Vehicle,sprite_width,			SLE_UINT8),
		SLE_VAR(Vehicle,sprite_height,		SLE_UINT8),
		SLE_VAR(Vehicle,z_height,					SLE_UINT8),
		SLE_VAR(Vehicle,x_offs,						SLE_INT8),
		SLE_VAR(Vehicle,y_offs,						SLE_INT8),
		SLE_VAR(Vehicle,progress,					SLE_UINT8),
		SLE_VAR(Vehicle,vehstatus,				SLE_UINT8),

		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleSpecial,unk0),	SLE_UINT16),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleSpecial,unk2),	SLE_UINT8),

		// reserve extra space in savegame here. (currently 16 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U64 | SLE_VAR_NULL, 2, 2, 255),

		SLE_END()
	};

	static final SaveLoad _disaster_desc[] = {
		SLE_WRITEBYTE(Vehicle,type,VEH_Disaster, 5),

		SLE_REF(Vehicle,next,							REF_VEHICLE_OLD),

		SLE_VAR(Vehicle,subtype,					SLE_UINT8),
		SLE_CONDVAR(Vehicle,tile,					SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Vehicle,tile,					SLE_UINT32, 6, 255),
		SLE_CONDVAR(Vehicle,dest_tile,		SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Vehicle,dest_tile,		SLE_UINT32, 6, 255),

		SLE_CONDVAR(Vehicle,x_pos,				SLE_FILE_I16 | SLE_VAR_I32, 0, 5),
		SLE_CONDVAR(Vehicle,x_pos,				SLE_INT32, 6, 255),
		SLE_CONDVAR(Vehicle,y_pos,				SLE_FILE_I16 | SLE_VAR_I32, 0, 5),
		SLE_CONDVAR(Vehicle,y_pos,				SLE_INT32, 6, 255),
		SLE_VAR(Vehicle,z_pos,						SLE_UINT8),
		SLE_VAR(Vehicle,direction,				SLE_UINT8),

		SLE_VAR(Vehicle,x_offs,						SLE_INT8),
		SLE_VAR(Vehicle,y_offs,						SLE_INT8),
		SLE_VAR(Vehicle,sprite_width,			SLE_UINT8),
		SLE_VAR(Vehicle,sprite_height,		SLE_UINT8),
		SLE_VAR(Vehicle,z_height,					SLE_UINT8),
		SLE_VAR(Vehicle,owner,						SLE_UINT8),
		SLE_VAR(Vehicle,vehstatus,				SLE_UINT8),
		SLE_CONDVARX(offsetof(Vehicle, current_order) + offsetof(Order, station), SLE_FILE_U8 | SLE_VAR_U16, 0, 4),
		SLE_CONDVARX(offsetof(Vehicle, current_order) + offsetof(Order, station), SLE_UINT16, 5, 255),

		SLE_VAR(Vehicle,cur_image,				SLE_UINT16),
		SLE_VAR(Vehicle,age,							SLE_UINT16),

		SLE_VAR(Vehicle,tick_counter,			SLE_UINT8),

		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleDisaster,image_override),	SLE_UINT16),
		SLE_VARX(offsetof(Vehicle,u)+offsetof(VehicleDisaster,unk2),						SLE_UINT16),

		// reserve extra space in savegame here. (currently 16 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U64 | SLE_VAR_NULL, 2, 2, 255),

		SLE_END()
	};


	static final void *_veh_descs[] = {
		_train_desc,
		_roadveh_desc,
		_ship_desc,
		_aircraft_desc,
		_special_desc,
		_disaster_desc,
	};

	// Will be called when the vehicles need to be saved.
	static void Save_VEHS()
	{
		Vehicle v;
		// Write the vehicles
		FOR_ALL_VEHICLES(v) {
			if (v.type != 0) {
				SlSetArrayIndex(v.index);
				SlObject(v, _veh_descs[v.type - 0x10]);
			}
		}
	}


	 //*  Converts all trains to the new subtype format introduced in savegame 16.2
	 //*  It also links multiheaded engines or make them forget they are multiheaded if no suitable partner is found

	static  void ConvertOldMultiheadToNew()
	{
		Vehicle v;
		FOR_ALL_VEHICLES(v) {
			if (v.type == VEH_Train) {
				v.rail.other_multiheaded_part = null;
				SETBIT(v.subtype, 7);	// indicates that it's the old format and needs to be converted in the next loop
			}
		}

		FOR_ALL_VEHICLES(v) {
			if (v.type == VEH_Train) {
				if (BitOps.HASBIT(v.subtype, 7) && ((v.subtype & ~0x80) == 0 || (v.subtype & ~0x80) == 4)) {
					Vehicle u = v;

					BEGIN_ENUM_WAGONS(u)
						final RailVehicleInfo *rvi = RailVehInfo(u.engine_type);
					CLRBIT(u.subtype, 7);
					switch (u.subtype) {
						case 0:	// TS_Front_Engine 
							if (rvi.flags & RVI_MULTIHEAD) {
								SetMultiheaded(u);
							}
							SetFrontEngine(u);
							SetTrainEngine(u);
							break;
						case 1:	// TS_Artic_Part 
							u.subtype = 0;
							SetArticulatedPart(u);
							break;
						case 2:	// TS_Not_First 
							u.subtype = 0;
							if (rvi.flags & RVI_WAGON) {
								// normal wagon
								SetTrainWagon(u);
								break;
							}
								if (rvi.flags & RVI_MULTIHEAD && rvi.image_index == u.spritenum - 1) {
									// rear end of a multiheaded engine
									SetMultiheaded(u);
									break;
								}
								if (rvi.flags & RVI_MULTIHEAD) {
									SetMultiheaded(u);
								}
								SetTrainEngine(u);
							break;
						case 4:	// TS_Free_Car 
							u.subtype = 0;
							SetTrainWagon(u);
							SetFreeWagon(u);
							break;
						default: NOT_REACHED(); break;
					}
					END_ENUM_WAGONS(u)
						u = v;
					BEGIN_ENUM_WAGONS(u)
						final RailVehicleInfo *rvi = RailVehInfo(u.engine_type);

					if (u.rail.other_multiheaded_part != null) continue;

					if (rvi.flags & RVI_MULTIHEAD) {
						if (!IsTrainEngine(u)) {
							// we got a rear car without a front car. We will convert it to a front one 
							SetTrainEngine(u);
							u.spritenum--;
						}

						{
							Vehicle w;

							for(w = u.next; w != null && (w.engine_type != u.engine_type || w.rail.other_multiheaded_part != null); w = GetNextVehicle(w));
							if (w != null) {
								// we found a car to partner with this engine. Now we will make sure it face the right way 
								if (IsTrainEngine(w)) {
									ClearTrainEngine(w);
									w.spritenum++;
								}
							}

							if (w != null) {
								w.rail.other_multiheaded_part = u;
								u.rail.other_multiheaded_part = w;
							} else {
								// we got a front car and no rear cars. We will fake this one for forget that it should have been multiheaded 
								ClearMultiheaded(u);
							}
						}
					}
					END_ENUM_WAGONS(u)
				}
			}
		}
	}

	// Will be called when vehicles need to be loaded.
	static void Load_VEHS()
	{
		int index;
		Vehicle v;

		while ((index = SlIterateArray()) != -1) {
			Vehicle v;

			if (!AddBlockIfNeeded(&_vehicle_pool, index))
				error("Vehicles: failed loading savegame: too many vehicles");

			v = GetVehicle(index);
			SlObject(v, _veh_descs[SlReadByte()]);

			// Old savegames used 'last_station_visited = 0xFF' 
			if (CheckSavegameVersion(5) && v.last_station_visited == 0xFF)
				v.last_station_visited = INVALID_STATION;

			if (CheckSavegameVersion(5)) {
				// Convert the current_order.type (which is a mix of type and flags, because
				//    in those versions, they both were 4 bits big) to type and flags 
				v.current_order.flags = (v.current_order.type & 0xF0) >> 4;
				v.current_order.type  =  v.current_order.type & 0x0F;
			}
		}

		// Check for shared order-lists (we now use pointers for that) 
		if (CheckSavegameVersionOldStyle(5, 2)) {
			FOR_ALL_VEHICLES(v) {
				Vehicle u;

				if (v.type == 0)
					continue;

				FOR_ALL_VEHICLES_FROM(u, v.index + 1) {
					if (u.type == 0)
						continue;

					/// If a vehicle has the same orders, add the link to eachother
					//    in both vehicles 
					if (v.orders == u.orders) {
						v.next_shared = u;
						u.prev_shared = v;
						break;
					}
				}
			}
		}

		// Connect front and rear engines of multiheaded trains and converts subtype to the new format 
		if (CheckSavegameVersionOldStyle(17, 1)) {
			ConvertOldMultiheadToNew();
		}
	}

	final ChunkHandler _veh_chunk_handlers[] = {
		{ 'VEHS', Save_VEHS, Load_VEHS, CH_SPARSE_ARRAY | CH_LAST},
	};
	 */
	
	public static void loadGame(ObjectInputStream oin) throws ClassNotFoundException, IOException
	{
		//_vehicle_pool = (MemoryPool<Vehicle>) oin.readObject();
	}

	public static void saveGame(ObjectOutputStream oos) throws IOException 
	{
		//oos.writeObject(_vehicle_pool);		
	}

	/**
	 * Color for profit-based colored icons
	 * @return Color to paint icon with 
	 */

	public int encodeColor() {
		if (age <= 365 * 2) {
			return Sprite.PALETTE_TO_GREY;
		} else if (profit_last_year < 0) {
			return Sprite.PALETTE_TO_RED;
		} else if (profit_last_year < 10000) {
			return Sprite.PALETTE_TO_YELLOW;
		} else {
			return Sprite.PALETTE_TO_GREEN;
		}
	}

	public int infoString() {
		int str;
		if (road.crashed_ctr != 0) {
			str = Str.STR_8863_CRASHED;
		} else if (isBroken()) {
			str = Str.STR_885C_BROKEN_DOWN;
		} else if(isStopped()) {
			str = Str.STR_8861_STOPPED;
		} else {
			if (num_orders == 0) {
				str = Str.STR_NO_ORDERS + (Global._patches.vehicle_speed ? 1 : 0);
				Global.SetDParam(0, cur_speed * 10 >> 5);
				return str;
			}
			int i = null == current_order ? -1 : current_order.type;
			
			switch (i) {
			case Order.OT_GOTO_STATION: {
				Global.SetDParam(0, current_order.station);
				Global.SetDParam(1, cur_speed * 10 >> 5);
				str = Str.STR_HEADING_FOR_STATION + (Global._patches.vehicle_speed ? 1 : 0);
			} break;

			case Order.OT_GOTO_DEPOT: {
				Depot depot = Depot.GetDepot(current_order.station);
				Global.SetDParam(0, depot.town_index);
				Global.SetDParam(1, cur_speed * 10 >> 5);
				str = Str.STR_HEADING_FOR_ROAD_DEPOT + (Global._patches.vehicle_speed ? 1 : 0);
			} break;

			case Order.OT_LOADING:
			case Order.OT_LEAVESTATION:
				str = Str.STR_882F_LOADING_UNLOADING;
				break;

			default:
				str = Str.STR_EMPTY;
				break;
			}
		}
		return str;
	}

	
	public void stop() {
		if (type == Vehicle.VEH_Train)
			rail.days_since_order_progr = 0;
		vehstatus |= Vehicle.VS_STOPPED;		
	}

	
	
	public boolean isStopped() 			{ return 0 != (vehstatus & VS_STOPPED); }
	public boolean isCrashed() 			{ return 0 != (vehstatus & VS_CRASHED); }
	public boolean isHidden() 			{ return 0 != (vehstatus & VS_HIDDEN); }
	public boolean isUnclickable() 		{ return 0 != (vehstatus & VS_UNCLICKABLE); }
	public boolean isTrainSlowing() 	{ return 0 != (vehstatus & VS_TRAIN_SLOWING); }
	public boolean isAircraftBroken() 	{ return 0 != (vehstatus & VS_AIRCRAFT_BROKEN); }
	

	public boolean isBroken() 			{ return breakdown_ctr == 1; }

	
	public void setHidden(boolean b)			{ setResetStatus(VS_HIDDEN, b); }
	public void setStopped(boolean b)			{ setResetStatus(VS_STOPPED, b); }
	public void setCrashed(boolean b)			{ setResetStatus(VS_CRASHED, b); }
	public void setTrainSlowing(boolean b)		{ setResetStatus(VS_TRAIN_SLOWING, b); }
	public void setDisaster(boolean b)			{ setResetStatus(VS_DISASTER, b); }
	public void setAircraftBroken(boolean b)	{ setResetStatus(VS_AIRCRAFT_BROKEN, b); }
	
	public void toggleStopped() { vehstatus ^= Vehicle.VS_STOPPED; }

	
	private void setResetStatus(int f, boolean b) 
	{
		if(b) vehstatus |= f;
		else vehstatus &= ~f;		
	}

	public void assignStatus(int s) { vehstatus = s; }
	public int getStatus() { return vehstatus; }

	
	public UnitID getUnitnumber() { return unitnumber; }
	public int getAge() { return age; }
	public int getReliability() { return reliability; }

	public AcceptedCargo countTotalCargo() 
	{
		AcceptedCargo cargo = new AcceptedCargo();

		for (Vehicle v = this; v != null; v = v.next) 
			cargo.ct[v.cargo_type] += v.cargo_cap;
		
		return cargo;
	}

	public int getProfit_this_year() { return profit_this_year; }
	public int getProfit_last_year() { return profit_last_year; }
	public int getString_id() { return string_id; }

	/**
	 * For train finds out lowest max speed of all cars.
	 * @return Maximum speed
	 */
	public int getRealMaxSpeed() 
	{
		if (getType() == Vehicle.VEH_Train ) 
		{
			Vehicle u = this;
			int mSpeed = Integer.MAX_VALUE;
			
			do {
				final int ms = Engine.RailVehInfo(engine_type.id).getMax_speed();
				if (ms != 0)
					mSpeed = Math.min(mSpeed, ms);
			} while ((u = u.next) != null);
			
			return mSpeed;
		}
		else
			return max_speed;
	}

	
	public int getX_pos() { return x_pos;	}
	public int getY_pos() { return y_pos;	}
	public int getZ_pos() { return z_pos;	}

	
	public int getMax_age() { return max_age; }
	public Vehicle getNext() { return next; }
	public int getService_interval() { return service_interval; }
	public EngineID getEngine_type() {		return engine_type;	}
	public int getBuild_year() {		return build_year;	}
	public int getValue() { return value; }
	public int getCargo_cap() {		return cargo_cap;	}
	public int getCargo_type() {		return cargo_type;	}
	public int getCargo_count() { return cargo_count; }

	public int getBreakdowns_since_last_service() { return breakdowns_since_last_service; }

	public int generateTrainDescription() 
	{
		int psp = Global._patches.vehicle_speed ? 1 : 0;
		int str;// = Str.INVALID_STRING;
		
		if (rail.crash_anim_pos != 0) {
			str = Str.STR_8863_CRASHED;
		} else if (isBroken()) {
			str = Str.STR_885C_BROKEN_DOWN;
		} else if(isStopped()) {
			if (rail.last_speed == 0) {
				str = Str.STR_8861_STOPPED;
			} else {
				Global.SetDParam(0, rail.last_speed * 10 >> 4);
				str = Str.STR_TRAIN_STOPPING + psp;
			}
		} else {
			switch (current_order.type) {
			case Order.OT_GOTO_STATION: {
				str = Str.STR_HEADING_FOR_STATION + psp;
				Global.SetDParam(0, current_order.station);
				Global.SetDParam(1, rail.last_speed * 10 >> 4);
			} break;

			case Order.OT_GOTO_DEPOT: {
				Depot dep = Depot.GetDepot(current_order.station);
				Global.SetDParam(0, dep.getTownIndex());
				str = Str.STR_HEADING_FOR_TRAIN_DEPOT + psp;
				Global.SetDParam(1, rail.last_speed * 10 >> 4);
			} break;

			case Order.OT_LOADING:
			case Order.OT_LEAVESTATION:
				str = Str.STR_882F_LOADING_UNLOADING;
				break;

			case Order.OT_GOTO_WAYPOINT: {
				Global.SetDParam(0, current_order.station);
				str = Str.STR_HEADING_FOR_WAYPOINT + psp;
				Global.SetDParam(1, rail.last_speed * 10 >> 4);
				break;
			}

			default:
				if (num_orders == 0) {
					str = Str.STR_NO_ORDERS + psp;
					Global.SetDParam(0, rail.last_speed * 10 >> 4);
				} else
					str = Str.STR_EMPTY;
				break;
			}
		}
		
		return str;
	}

	public int getSpritenum() { return spritenum; }
	public int getCargo_source() {		return cargo_source;	}
	public int getMax_speed() {		return max_speed;	}
	public int getNum_orders() { return num_orders; }
	public int getCur_order_index() { return cur_order_index; }
	public int getCur_speed() { return cur_speed;	}
	public int getCargo_days() { return cargo_days;	}

	public Order getCurrent_order() {
		return current_order;
	}

	public void setCurrent_order(Order order) {
		//Global.debug("set %s", order);
		this.current_order = new Order( order );
	}


	// Called on breakdown
	public void SndPlayVehicleFx(/*SoundFx*/ Snd snd)
	{
		switch(type) {

		case VEH_Road:
			//ShortSounds.playMotorSound();
			//break;
		case VEH_Aircraft:	
		case VEH_Train:		
		case VEH_Ship:		
			SndPlayVehicleFx(snd.ordinal());
			break;

		}
		
	}

	void SndPlayVehicleFx(/*SoundFx*/ int snd)
	{
		Sound.SndPlayScreenCoordFx(snd,
			(left_coord + right_coord) / 2,
			(top_coord + bottom_coord) / 2
		);
	}




	
}
