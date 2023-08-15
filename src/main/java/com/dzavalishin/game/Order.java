package com.dzavalishin.game;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.xui.VehicleGui;
import com.dzavalishin.xui.Window;

//public class Order implements IPoolItem, Serializable 
public class Order implements Serializable 
{
	private static final long serialVersionUID = 1L;

	public int getType() {		return type;	}
	public int getFlags() {		return flags;	}
	public int getStation() {	return station;	}
	public Order getNext() {	return next;	}

	int  type;
	int  flags;
	int  station;

	Order next;   //! Pointer to next order. If null, end of list

	//int index;         //! Index of the order, is not saved or anything, just for reference

	/**
	 *
	 * Assign data to an order (from an other order)
	 *   This function makes sure that the index is maintained correctly
	 *
	 * @param to assign to
	 *
	 * @implNote It doesn't touch linked list 'next' field
	 */
	static void AssignOrder(Order to, Order from)
	{
		to.type    = from.type;
		to.flags   = from.flags;
		to.station = from.station;
	}

	private void clean() 
	{
		type    = OT_NOTHING;
		flags   = 0;
		station = Station.INVALID_STATION;
		next = null;
		//index = 0;
	}

	public Order() {
		clean();
	}

	public Order( Order src )
	{
		AssignOrder(this, src);
		//index = src.index;
		next = src.next; // TODO Do we need it?
	}

	public Order(int type, int flags, int st) 
	{
		this.type = type;
		this.flags = flags;
		station = st;
	}

	public Order(int t) {
		clean();
		type = t;
	}

	public static final int OT_NOTHING       = 0;
	public static final int OT_GOTO_STATION  = 1;
	public static final int OT_GOTO_DEPOT    = 2;
	public static final int OT_LOADING       = 3;
	public static final int OT_LEAVESTATION  = 4;
	public static final int OT_DUMMY         = 5;
	public static final int OT_GOTO_WAYPOINT = 6;

	/** Order flag masks - these are for direct bit operations */

	//Flags for stations:
	/** vehicle will transfer cargo (i. e. not deliver to nearby industry/town even if accepted there) */
	public static final int OF_TRANSFER           = 0x1;
	/** If OF_TRANSFER is not set, drop any cargo loaded. If accepted, deliver, otherwise cargo remains at the station.
	 * No new cargo is loaded onto the vehicle whatsoever */
	public static final int OF_UNLOAD             = 0x2;
	/** Wait for full load of all vehicles, or of at least one cargo type, depending on patch setting
	 * TODO make this two different flags */
	public static final int OF_FULL_LOAD          = 0x4;

	//Flags for depots:
	/** The current depot-order was initiated because it was in the vehicle's order list */
	public static final int OF_PART_OF_ORDERS	  = 0x2;
	/** if OF_PART_OF_ORDERS is not set, this will cause the vehicle to be stopped in the depot */
	public static final int OF_HALT_IN_DEPOT      = 0x4;
	/** if OF_PART_OF_ORDERS is set, this will cause the order only be come active if the vehicle needs servicing */
	public static final int OF_SERVICE_IF_NEEDED  = 0x4; //used when OF_PART_OF_ORDERS is set.

	//Common flags
	/** This causes the vehicle not to stop at intermediate OR the destination station (depending on patch settings)
	 * TODO make this two different flags */
	public static final int OF_NON_STOP           = 0x8;



	/** Order flags bits - these are for the *BIT macros
	 * for descrption of flags, see OrderFlagMasks
	 * @see OrderFlagMasks
	 */
	public static final int OFB_TRANSFER          = 0;
	public static final int OFB_UNLOAD            = 1;
	public static final int OFB_FULL_LOAD         = 2;
	public static final int OFB_PART_OF_ORDERS    = 1;
	public static final int OFB_HALT_IN_DEPOT     = 2;
	public static final int OFB_SERVICE_IF_NEEDED = 2;
	public static final int OFB_NON_STOP          = 3;
	


	/* Possible clone options */
	public static final int CO_SHARE   = 0;
	public static final int CO_COPY    = 1;
	public static final int CO_UNSHARE = 2;

	/* Modes for the order checker */
	public static final int OC_INIT     = 0; //the order checker can initialize a news message
	public static final int OC_VALIDATE = 1; //the order checker validates a news message




	/**
	 *
	 * Unpacks a order from savegames made with TTD(Patch)
	 *
	 * /
	Order UnpackOldOrder(int packed)
	{
		Order order = new Order();
		order.type    = BitOps.GB(packed, 0, 4);
		order.flags   = BitOps.GB(packed, 4, 4);
		order.station = BitOps.GB(packed, 8, 8);
		order.next    = null;

		// Sanity check
		// TTD stores invalid orders as OT_NOTHING with non-zero flags/station
		if (order.type == OT_NOTHING && (order.flags != 0 || order.station != 0)) {
			order.type = OT_DUMMY;
			order.flags = 0;
		}

		return order;
	}

	/**
	 *
	 * Unpacks a order from savegames with version 4 and lower
	 *
	 * /
	Order UnpackVersion4Order(int packed)
	{
		Order order = new Order();
		order.type    = BitOps.GB(packed, 0, 4);
		order.flags   = BitOps.GB(packed, 4, 4);
		order.station = BitOps.GB(packed, 8, 8);
		order.next    = null;
		order.index   = 0; // avoid compiler warning
		return order;
	}


	/**
	 *
	 * Swap two orders
	 *
	 */
	static void SwapOrders(Order order1, Order order2)
	{
		Order temp_order = new Order();
		AssignOrder(temp_order, order1);
		Order temp_next = order1.next;

		AssignOrder(order1, order2);
		order1.next = order2.next;

		AssignOrder(order2, temp_order);
		order2.next = temp_next; //temp_next.next;
	}

	/**
	 *
	 * Allocate a new order
	 *
	 * @return Order if a free space is found, else null.
	 *
	 */
	static Order AllocateOrder()
	{
		return new Order();
	}



	/** Add an order to the order list of a vehicle.
	 * @param x,y unused
	 * @param p1 various bitstuffed elements
	 * - p1 = (bit  0 - 15) - ID of the vehicle
	 * - p1 = (bit 16 - 31) - the selected order (if any). If the last order is given,
	 *                        the order will be inserted before that one
	 *                        only the first 8 bits used currently (bit 16 - 23) (max 255)
	 * @param p2 packed order to insert
	 */
	static int CmdInsertOrder(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		VehicleID veh   = VehicleID.get( BitOps.GB(p1,  0, 16) );
		//OrderID sel_ord = OrderID.get( BitOps.GB(p1, 16, 16) );
		int sel_ord = BitOps.GB(p1, 16, 16);
		Order new_order = UnpackOrder(p2);

		if (!veh.IsVehicleIndex()) 
			return Cmd.CMD_ERROR;
		v = Vehicle.GetVehicle(veh);
		if (v.type == 0 || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		/* Check if the inserted order is to the correct destination (owner, type),
		 * and has the correct flags if any */
		switch (new_order.type) {
		case OT_GOTO_STATION: {
			final Station st;

			if (!Station.IsStationIndex(new_order.station)) return Cmd.CMD_ERROR;
			st = Station.GetStation(new_order.station);

			if (!st.IsValidStation() ||
					(st.airport_type != Airport.AT_OILRIG && !(st.IsBuoy()) && !Player.CheckOwnership(st.owner) && !mAirport.MA_OwnerHandler(st.owner)))
				return Cmd.CMD_ERROR;

			//MA checks
			if(Global._patches.allow_municipal_airports.get() && !mAirport.MA_WithinVehicleQuota(Station.GetStation(new_order.station))) {
				Global._error_message = Str.STR_MA_EXCEED_MAX_QUOTA;
				return Cmd.CMD_ERROR;
				//End MA checks

			}


			switch (v.type) {
			case Vehicle.VEH_Train:
				if (0 == (st.facilities & Station.FACIL_TRAIN)) return Cmd.CMD_ERROR;
				break;

			case Vehicle.VEH_Road:
				if (v.getCargo_type() == AcceptedCargo.CT_PASSENGERS) {
					if (0 == (st.facilities & Station.FACIL_BUS_STOP)) return Cmd.CMD_ERROR;
				} else {
					if (0 == (st.facilities & Station.FACIL_TRUCK_STOP)) return Cmd.CMD_ERROR;
				}
				break;

			case Vehicle.VEH_Ship:
				if (0 == (st.facilities & Station.FACIL_DOCK)) return Cmd.CMD_ERROR;
				break;

			case Vehicle.VEH_Aircraft:
				if (0 == (st.facilities & Station.FACIL_AIRPORT)) return Cmd.CMD_ERROR;
				break;

			default: return Cmd.CMD_ERROR;
			}

			/* Order flags can be any of the following for stations:
			 * [full-load | unload] [+ transfer] [+ non-stop]
			 * non-stop orders (if any) are only valid for trains */
			switch (new_order.flags) {
			case 0:
			case OF_FULL_LOAD:
			case OF_FULL_LOAD | OF_TRANSFER:
			case OF_UNLOAD:
			case OF_UNLOAD | OF_TRANSFER:
			case OF_TRANSFER:
				break;

			case OF_NON_STOP:
			case OF_NON_STOP | OF_FULL_LOAD:
			case OF_NON_STOP | OF_FULL_LOAD | OF_TRANSFER:
			case OF_NON_STOP | OF_UNLOAD:
			case OF_NON_STOP | OF_UNLOAD | OF_TRANSFER:
			case OF_NON_STOP | OF_TRANSFER:
				if (v.type != Vehicle.VEH_Train) return Cmd.CMD_ERROR;
				break;

			default: return Cmd.CMD_ERROR;
			}
			break;
		}

		case OT_GOTO_DEPOT: {
			if (v.type == Vehicle.VEH_Aircraft) {
				final Station st;

				if (!Station.IsStationIndex(new_order.station)) return Cmd.CMD_ERROR;
				st = Station.GetStation(new_order.station);

				if (!st.IsValidStation() ||
						(st.airport_type != Airport.AT_OILRIG && !Player.CheckOwnership(st.owner)) ||
						0 == (st.facilities & Station.FACIL_AIRPORT) ||
						Airport.GetAirport(st.airport_type).nof_depots() == 0) {
					return Cmd.CMD_ERROR;
				}
			} else {
				final Depot dp;

				if (!Depot.IsDepotIndex(new_order.station)) return Cmd.CMD_ERROR;
				dp = Depot.GetDepot(new_order.station);

				if (!dp.isValid() || !Player.CheckOwnership(dp.xy.GetTileOwner()))
					return Cmd.CMD_ERROR;

				switch (v.type) {
				case Vehicle.VEH_Train:
					if (!Depot.IsTileDepotType(dp.xy, TransportType.Rail)) return Cmd.CMD_ERROR;
					break;

				case Vehicle.VEH_Road:
					if (!Depot.IsTileDepotType(dp.xy, TransportType.Road)) return Cmd.CMD_ERROR;
					break;

				case Vehicle.VEH_Ship:
					if (!Depot.IsTileDepotType(dp.xy, TransportType.Water)) return Cmd.CMD_ERROR;
					break;

				default: return Cmd.CMD_ERROR;
				}
			}

			/* Order flags can be any of the following for depots:
			 * order [+ halt] [+ non-stop]
			 * non-stop orders (if any) are only valid for trains */
			switch (new_order.flags) {
			case OF_PART_OF_ORDERS:
			case OF_PART_OF_ORDERS | OF_HALT_IN_DEPOT:
				break;

			case OF_NON_STOP | OF_PART_OF_ORDERS:
			case OF_NON_STOP | OF_PART_OF_ORDERS | OF_HALT_IN_DEPOT:
				if (v.type != Vehicle.VEH_Train) return Cmd.CMD_ERROR;
				break;

			default: return Cmd.CMD_ERROR;
			}
			break;
		}

		case OT_GOTO_WAYPOINT: {
			WayPoint wp;

			if (v.type != Vehicle.VEH_Train) return Cmd.CMD_ERROR;

			if (!WayPoint.IsWaypointIndex(new_order.station)) return Cmd.CMD_ERROR;
			wp = WayPoint.GetWaypoint(new_order.station);

			if (!Player.CheckOwnership(wp.xy.GetTileOwner())) return Cmd.CMD_ERROR;

			/* Order flags can be any of the following for waypoints:
			 * [non-stop]
			 * non-stop orders (if any) are only valid for trains */
			switch (new_order.flags) {
			case 0: break;

			case OF_NON_STOP:
				if (v.type != Vehicle.VEH_Train) return Cmd.CMD_ERROR;
				break;

			default: return Cmd.CMD_ERROR;
			}
			break;
		}

		default: return Cmd.CMD_ERROR;
		}

		if (sel_ord > v.num_orders) return Cmd.CMD_ERROR;

		/* For ships, make sure that the station is not too far away from the
		 * previous destination, for human players with new pathfinding disabled */
		if (v.type == Vehicle.VEH_Ship && v.owner.IS_HUMAN_PLAYER() &&
				sel_ord != 0 && v.GetVehicleOrder(sel_ord - 1).type == OT_GOTO_STATION
				&& !Global._patches.new_pathfinding_all) {

			int dist = Map.DistanceManhattan(
					Station.GetStation(v.GetVehicleOrder(sel_ord - 1).station).getXy(),
					Station.GetStation(new_order.station).getXy()
					);
			if (dist >= 130)
				return Cmd.return_cmd_error(Str.STR_0210_TOO_FAR_FROM_PREVIOUS_DESTINATIO);
		}

		if (0 != (flags & Cmd.DC_EXEC)) {
			Vehicle u;
			Order newo = AllocateOrder();
			AssignOrder(newo, new_order);

			/* Create new order and link in list */
			if (v.orders == null) {
				v.orders = newo;
			} else {
				/* Try to get the previous item (we are inserting above the
				    selected) */
				Order order = v.GetVehicleOrder(sel_ord - 1);

				if (order == null && v.GetVehicleOrder(sel_ord) != null) {
					/* There is no previous item, so we are altering v.orders itself
					    But because the orders can be shared, we copy the info over
					    the v.orders, so we don't have to change the pointers of
					    all vehicles */
					SwapOrders(v.orders, newo);
					/* Now update the next pointers */
					v.orders.next = newo;
				} else if (order == null) {
					/* 'sel' is a non-existing order, add him to the end */
					order = v.GetLastVehicleOrder();
					order.next = newo;
				} else {
					/* Put the new order in between */
					newo.next = order.next;
					order.next = newo;
				}
			}

			u = v.GetFirstVehicleFromSharedList();
			while (u != null) {
				/* Increase amount of orders */
				u.num_orders++;

				/* If the orderlist was empty, assign it */
				if (u.orders == null) u.orders = v.orders;

				assert(v.orders == u.orders);

				/* If there is added an order before the current one, we need
				to update the selected Order */
				if (sel_ord <= u.cur_order_index) {
					int cur = u.cur_order_index + 1;
					/* Check if we don't go out of bound */
					if (cur < u.num_orders)
						u.cur_order_index = cur;// OrderID.get( cur );
				}
				/* Update any possible open window of the Vehicle */
				u.InvalidateVehicleOrder();
				if (u.type == Vehicle.VEH_Train) u.rail.shortest_platform[1] = 0; // we changed the orders so we invalidate the station length collector

				u = u.next_shared;
			}

			/* Make sure to rebuild the whole list */
			VehicleGui.RebuildVehicleLists();
		}

		return 0;
	}

	/** Declone an order-list
	 * @param dst delete the orders of this vehicle
	 * @param flags execution flags
	 */
	static int DecloneOrder(Vehicle dst, int flags)
	{
		if (0 != (flags & Cmd.DC_EXEC)) {
			dst.DeleteVehicleOrders();
			dst.InvalidateVehicleOrder();
			VehicleGui.RebuildVehicleLists();
		}
		return 0;
	}

	/** Delete an order from the order list of a vehicle.
	 * @param x,y unused
	 * @param p1 the ID of the vehicle
	 * @param p2 the order to delete (max 255)
	 */
	static int CmdDeleteOrder(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v, u;
		VehicleID veh_id = VehicleID.get( p1 );
		//OrderID sel_ord = OrderID.get( p2 );
		final int sel_ord = p2;
		Order order;

		if (!Vehicle.IsVehicleIndex(veh_id.id)) return Cmd.CMD_ERROR;
		v = Vehicle.GetVehicle(veh_id);
		if (v.type == 0 || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		/* If we did not select an order, we maybe want to de-clone the orders */
		if (sel_ord >= v.num_orders)
			return DecloneOrder(v, flags);

		order = v.GetVehicleOrder(sel_ord);
		if (order == null) return Cmd.CMD_ERROR;

		if (0 != (flags & Cmd.DC_EXEC)) {
			if (v.GetVehicleOrder(sel_ord - 1) == null) {
				if (v.GetVehicleOrder(sel_ord + 1) != null) {
					/* First item, but not the last, so we need to alter v.orders
					    Because we can have shared order, we copy the data
					    from the next item over the deleted */
					order = v.GetVehicleOrder(sel_ord + 1);
					SwapOrders(v.orders, order);
				} else {
					/* Last item, so clean the list */
					v.orders = null;
				}
			} else {
				v.GetVehicleOrder(sel_ord - 1).next = order.next;
			}

			/* Give the item free */
			order.type = OT_NOTHING;
			order.next = null;

			if (v.type == Vehicle.VEH_Aircraft) {
				/* Take out of airport queue
				 */
				if(v.queue_item != null)
				{
					v.queue_item.queue.del(v);
				}
			}


			u = v.GetFirstVehicleFromSharedList();
			while (u != null) {
				u.num_orders--;

				if (u.type == Vehicle.VEH_Aircraft) {
					/* Take out of airport queue
					 */
					if(u.queue_item != null)
					{
						v.queue_item.queue.del(v);
					}
				}

				if (sel_ord < u.cur_order_index)
					u.cur_order_index--;

				/* If we removed the last order, make sure the shared vehicles
				 * also set their orders to null */
				if (v.orders == null) u.orders = null;

				assert(v.orders == u.orders);

				/* NON-stop flag is misused to see if a train is in a station that is
				 * on his order list or not */
				if (sel_ord == u.cur_order_index && u.getCurrent_order().type == OT_LOADING &&
						BitOps.HASBIT(u.getCurrent_order().flags, OFB_NON_STOP)) {
					u.getCurrent_order().flags = 0;
				}

				/* Update any possible open window of the Vehicle */
				u.InvalidateVehicleOrder();

				u = u.next_shared;
			}

			VehicleGui.RebuildVehicleLists();
		}

		return 0;
	}

	/** Goto next order of order-list.
	 * @param x,y unused
	 * @param p1 The ID of the vehicle which order is skipped
	 * @param p2 unused
	 */
	static int CmdSkipOrder(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		VehicleID veh_id = VehicleID.get( p1 );

		if (!veh_id.IsVehicleIndex()) return Cmd.CMD_ERROR;
		v = Vehicle.GetVehicle(veh_id);
		if (v.type == 0 || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if (0 != (flags & Cmd.DC_EXEC)) {
			/* Goto next Order*/
			//OrderID b = OrderID.get( v.cur_order_index + 1 ); 
			//if (b.id >= v.num_orders) b = null;
			//v.cur_order_index = b.id;
			v.cur_order_index++;
			if (v.cur_order_index >= v.num_orders) 
				v.cur_order_index = 0;

			if (v.type == Vehicle.VEH_Train) v.rail.days_since_order_progr = 0;

			if (v.type == Vehicle.VEH_Road) RoadVehCmd.ClearSlot(v, v.road.slot);

			/* NON-stop flag is misused to see if a train is in a station that is
			 * on his order list or not */
			if (v.getCurrent_order().type == OT_LOADING && BitOps.HASBIT(v.getCurrent_order().flags, OFB_NON_STOP))
				v.getCurrent_order().flags = 0;

			if (v.type == Vehicle.VEH_Train) v.rail.shortest_platform[1] = 0; // we changed the orders so we invalidate the station length collector
			v.InvalidateVehicleOrder();
		}

		/* We have an aircraft/ship, they have a mini-schedule, so update them all */
		if (v.type == Vehicle.VEH_Aircraft) {
			Window.InvalidateWindowClasses(Window.WC_AIRCRAFT_LIST);
			/* Take out of airport queue
			 */
			if(v.queue_item != null)
			{
				v.queue_item.queue.del(v);
			}
		}
		if (v.type == Vehicle.VEH_Ship) Window.InvalidateWindowClasses(Window.WC_SHIPS_LIST);

		return 0;
	}


	/** Modify an order in the orderlist of a vehicle.
	 * @param x,y unused
	 * @param p1 various bitstuffed elements
	 * - p1 = (bit  0 - 15) - ID of the vehicle
	 * - p1 = (bit 16 - 31) - the selected order (if any). If the last order is given,
	 *                        the order will be inserted before that one
	 *                        only the first 8 bits used currently (bit 16 - 23) (max 255)
	 * @param p2 mode to change the order to (always set)
	 */
	static int CmdModifyOrder(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		Order order;
		//OrderID sel_ord = OrderID.get(BitOps.GB(p1, 16, 16)); // X XX - automatically truncated to 8 bits.
		int sel_ord = BitOps.GB(p1, 16, 16); // XXX - automatically truncated to 8 bits. [dz] I don't get it
		VehicleID veh   = VehicleID.get( BitOps.GB(p1,  0, 16) );

		if (!veh.IsVehicleIndex()) return Cmd.CMD_ERROR;
		if (p2 != OFB_FULL_LOAD && p2 != OFB_UNLOAD && p2 != OFB_NON_STOP && p2 != OFB_TRANSFER) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(veh);
		if (v.type == 0 || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		/* Is it a valid order? */
		if (sel_ord >= v.num_orders) return Cmd.CMD_ERROR;

		order = v.GetVehicleOrder(sel_ord);
		if (order.type != OT_GOTO_STATION &&
				(order.type != OT_GOTO_DEPOT || p2 == OFB_UNLOAD) &&
				(order.type != OT_GOTO_WAYPOINT || p2 != OFB_NON_STOP))
			return Cmd.CMD_ERROR;

		if (0 != (flags & Cmd.DC_EXEC)) {
			switch (p2) {
			case OFB_FULL_LOAD:
				order.flags = BitOps.RETTOGGLEBIT(order.flags, OFB_FULL_LOAD);
				if (order.type != OT_GOTO_DEPOT) order.flags = BitOps.RETCLRBIT(order.flags, OFB_UNLOAD);
				break;
			case OFB_UNLOAD:
				order.flags = BitOps.RETTOGGLEBIT(order.flags, OFB_UNLOAD);
				order.flags = BitOps.RETCLRBIT(order.flags, OFB_FULL_LOAD);
				break;
			case OFB_NON_STOP:
				order.flags = BitOps.RETTOGGLEBIT(order.flags, OFB_NON_STOP);
				break;
			case OFB_TRANSFER:
				order.flags = BitOps.RETTOGGLEBIT(order.flags, OFB_TRANSFER);
				break;
			default: assert false;
			}

			/* Update the windows and full load flags, also for vehicles that share the same order list */
			{
				Vehicle u = v.GetFirstVehicleFromSharedList();
				while (u != null) {
					/* toggle u.current_order "Full load" flag if it changed */
					if (sel_ord == u.cur_order_index &&
							BitOps.HASBIT(u.getCurrent_order().flags, OFB_FULL_LOAD) != BitOps.HASBIT(order.flags, OFB_FULL_LOAD))
						u.getCurrent_order().flags = BitOps.RETTOGGLEBIT(u.getCurrent_order().flags, OFB_FULL_LOAD);
					u.InvalidateVehicleOrder();
					u = u.next_shared;
				}
			}
		}

		return 0;
	}

	/** Clone/share/copy an order-list of an other vehicle.
	 * @param p1 various bitstuffed elements
	 * - p1 = (bit  0-15) - destination vehicle to clone orders to (p1 & 0xFFFF)
	 * - p1 = (bit 16-31) - source vehicle to clone orders from, if any (none for CO_UNSHARE)
	 * @param p2 mode of cloning: CO_SHARE, CO_COPY, or CO_UNSHARE
	 */
	static int CmdCloneOrder(int x, int y, int flags, int p1, int p2)
	{
		Vehicle dst;
		VehicleID veh_src = VehicleID.get( BitOps.GB(p1, 16, 16) );
		VehicleID veh_dst = VehicleID.get( BitOps.GB(p1,  0, 16) );

		//MA Vars;
		Station st;
		int i;
		//End MA Vars;

		if (!veh_dst.IsVehicleIndex()) return Cmd.CMD_ERROR;

		dst = Vehicle.GetVehicle(veh_dst);

		if (dst.type == 0 || !Player.CheckOwnership(dst.owner)) return Cmd.CMD_ERROR;

		//MA checks
		if(mAirport.MA_VehicleServesMS(Vehicle.GetVehicle(veh_src)) > 0) 
		{
			for(i =  1; i <= mAirport.MA_VehicleServesMS(Vehicle.GetVehicle(veh_src)) ; i++) 
			{
				st = Station.GetStation(mAirport.MA_Find_MS_InVehicleOrders(Vehicle.GetVehicle(veh_src), i).id);
				if(!mAirport.MA_WithinVehicleQuota(st)) 
				{ 
					Global._error_message = Str.STR_MA_EXCEED_MAX_QUOTA;
					return Cmd.CMD_ERROR;
				}//if
			}//for
		}//if
		//End MA checks;

		switch (p2) {
		case CO_SHARE: {
			Vehicle src;

			if (!veh_src.IsVehicleIndex()) return Cmd.CMD_ERROR;

			src = Vehicle.GetVehicle(veh_src);

			/* Sanity checks */
			if (src.type == 0 || !Player.CheckOwnership(src.owner) || dst.type != src.type || dst == src)
				return Cmd.CMD_ERROR;

			/* Trucks can't share orders with busses (and visa versa) */
			if (src.type == Vehicle.VEH_Road) {
				if (src.getCargo_type() != dst.getCargo_type() && (src.getCargo_type() == AcceptedCargo.CT_PASSENGERS || dst.getCargo_type() == AcceptedCargo.CT_PASSENGERS))
					return Cmd.CMD_ERROR;
			}

			/* Is the vehicle already in the shared list? */
			{
				Vehicle u = src.GetFirstVehicleFromSharedList();
				while (u != null) {
					if (u == dst)
						return Cmd.CMD_ERROR;
					u = u.next_shared;
				}
			}

			if (0 != (flags & Cmd.DC_EXEC)) {
				/* If the destination vehicle had a OrderList, destroy it */
				dst.DeleteVehicleOrders();

				dst.orders = src.orders;
				dst.num_orders = src.num_orders;

				/* Link this vehicle in the shared-list */
				dst.next_shared = src.next_shared;
				dst.prev_shared = src;
				if (src.next_shared != null)
					src.next_shared.prev_shared = dst;
				src.next_shared = dst;

				dst.InvalidateVehicleOrder();
				src.InvalidateVehicleOrder();

				VehicleGui.RebuildVehicleLists();
				if (dst.type == Vehicle.VEH_Train) dst.rail.shortest_platform[1] = 0; // we changed the orders so we invalidate the station length collector
			}
		} break;

		case CO_COPY: {
			Vehicle src;
			

			if (!veh_src.IsVehicleIndex()) return Cmd.CMD_ERROR;

			src = Vehicle.GetVehicle(veh_src);

			/* Sanity checks */
			if (src.type == 0 || !Player.CheckOwnership(src.owner) || dst.type != src.type || dst == src)
				return Cmd.CMD_ERROR;

			/* Trucks can't copy all the orders from busses (and visa versa) */
			if (src.type == Vehicle.VEH_Road) {
				//final Order order;
				TileIndex required_dst = TileIndex.INVALID_TILE;
 
				for(Order order = src.orders; order != null; order = order.next )
				{
					if (order.type == OT_GOTO_STATION) {
						final Station st1 = Station.GetStation(order.station);
						if (dst.getCargo_type() == AcceptedCargo.CT_PASSENGERS) {
							if (st1.bus_stops != null) required_dst = st1.bus_stops.get(0).xy; // TODO why first?
						} else {
							if (st1.truck_stops != null) required_dst = st1.truck_stops.get(0).xy;
						}
						/* This station has not the correct road-bay, so we can't copy! */
						if (required_dst == TileIndex.INVALID_TILE)
							return Cmd.CMD_ERROR;
					}
				}
			}

			/* make sure there are orders available */
			//int delta = dst.IsOrderListShared() ? src.num_orders + 1 : src.num_orders - dst.num_orders;
			//if (!HasOrderPoolFree(delta))					return_cmd_error(Str.STR_8831_NO_MORE_SPACE_FOR_ORDERS);

			if (0 != (flags & Cmd.DC_EXEC)) {
				//final Order order;
				Order order_dst;

				/* If the destination vehicle had a OrderList, destroy it */
				dst.DeleteVehicleOrders();

				if(src.orders != null)
				{
					dst.orders = AllocateOrder();
					AssignOrder(dst.orders, src.orders);
				}

				order_dst = dst.orders;

				for(Order order_src = src.orders; order_src != null && order_src.next != null; order_src = order_src.next )
				{
					order_dst.next = AllocateOrder();
					AssignOrder(order_dst.next, order_src.next);
					order_dst = order_dst.next;
				}

				dst.num_orders = src.num_orders;

				dst.InvalidateVehicleOrder();

				VehicleGui.RebuildVehicleLists();
				if (dst.type == Vehicle.VEH_Train) dst.rail.shortest_platform[1] = 0; // we changed the orders so we invalidate the station length collector
			}
		} break;

		case CO_UNSHARE: return DecloneOrder(dst, flags);
		default: return Cmd.CMD_ERROR;
		}

		return 0;
	}



	/** Restore the current order-index of a vehicle and sets service-interval.
	 * @param x,y unused
	 * @param p1 the ID of the vehicle
	 * @param p2 various bistuffed elements
	 * - p2 = (bit  0-15) - current order-index (p2 & 0xFFFF)
	 * - p2 = (bit 16-31) - service interval (p2 >> 16)
	 * <br>
	 * TODO Unfortunately you cannot safely restore the unitnumber or the old vehicle
	 * as far as I can see. We can store it in BackuppedOrders, and restore it, but
	 * but we have no way of seeing it has been tampered with or not, as we have no
	 * legit way of knowing what that ID was.@n
	 * If we do want to backup/restore it, just add UnitID uid to BackuppedOrders, and
	 * restore it as parameter 'y' (ugly hack I know) for example. "v.unitnumber = y;"
	 */
	static int CmdRestoreOrderIndex(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		//OrderID cur_ord = OrderID.get( BitOps.GB(p2,  0, 16) );
		int cur_ord = BitOps.GB(p2,  0, 16);
		int serv_int = BitOps.GB(p2, 16, 16);

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);
		/* Check the vehicle type and ownership, and if the service interval and order are in range */
		if (v.type == 0 || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;
		if (serv_int != Depot.GetServiceIntervalClamped(serv_int) || cur_ord >= v.num_orders) return Cmd.CMD_ERROR;

		if (0 != (flags & Cmd.DC_EXEC)) {
			v.cur_order_index = cur_ord;
			v.service_interval = serv_int;
		}

		return 0;
	}

	/**
	 *
	 * Check the orders of a vehicle, to see if there are invalid orders and stuff
	 *
	 */
	static boolean CheckOrders(int data_a, int data_b)
	{
		final Vehicle  v = Vehicle.GetVehicle(data_a);

		/* Does the user wants us to check things? */
		if (Global._patches.order_review_system == 0) return false;

		/* Do nothing for crashed vehicles */
		if(v.isCrashed()) return false;

		/* Do nothing for stopped vehicles if setting is '1' */
		if (Global._patches.order_review_system == 1 && v.isStopped() )
			return false;

		/* do nothing we we're not the first vehicle in a share-chain */
		if (v.next_shared != null) return false;

		/* Only check every 20 days, so that we don't flood the message log */
		if (v.owner.equals(Global.gs._local_player) && v.day_counter % 20 == 0) {
			int n_st, problem_type = -1;
			//final Order order;
			Station st;
			//int message = 0;

			/* Check the order list */
			n_st = 0;

			/*if (data_b == OC_INIT) {
				DEBUG(misc, 3) ("CheckOrder called in mode 0 (initiation mode) for %d", v.index);
			} else {
				DEBUG(misc, 3) ("CheckOrder called in mode 1 (validation mode) for %d", v.index);
			}*/

			for(Order order = v.orders; order != null; order = order.next )
			{
				/* Dummy order? */
				if (order.type == OT_DUMMY) {
					problem_type = 1;
					break;
				}
				/* Does station have a load-bay for this vehicle? */
				if (order.type == OT_GOTO_STATION) {
					TileIndex required_tile;

					n_st++;
					st = Station.GetStation(order.station);
					required_tile = v.GetStationTileForVehicle(st);
					if (required_tile == null) problem_type = 3;
				}
			}

			/* Check if the last and the first order are the same */
			if (v.num_orders > 1 &&
					v.orders.type    == v.GetLastVehicleOrder().type &&
					v.orders.flags   == v.GetLastVehicleOrder().flags &&
					v.orders.station == v.GetLastVehicleOrder().station)
				problem_type = 2;

			/* Do we only have 1 station in our order list? */
			if (n_st < 2 && problem_type == -1) problem_type = 0;

			/* We don't have a problem */
			if (problem_type < 0) {
				/*if (data_b == OC_INIT) {
					DEBUG(misc, 3) ("CheckOrder mode 0: no problems found for %d", v.index);
				} else {
					DEBUG(misc, 3) ("CheckOrder mode 1: news item surpressed for %d", v.index);
				}*/
				return false;
			}

			/* we have a problem, are we're just in the validation process
			   so don't display an error message */
			if (data_b == OC_VALIDATE) {
				/*DEBUG(misc, 3) ("CheckOrder mode 1: new item validated for %d", v.index);*/
				return true;
			}

			int message = Str.STR_TRAIN_HAS_TOO_FEW_ORDERS + ((v.type - Vehicle.VEH_Train) << 2) + problem_type;
			/*DEBUG(misc, 3) ("Checkorder mode 0: Triggered News Item for %d", v.index);*/

			Global.SetDParam(0, v.unitnumber.id);
			NewsItem.AddValidatedNewsItem(
					message,
					NewsItem.NEWS_FLAGS(NewsItem.NM_SMALL, NewsItem.NF_VIEWPORT | NewsItem.NF_VEHICLE, NewsItem.NT_ADVICE, 0),
					v.index,
					OC_VALIDATE,	//next time, just validate the orders
					Order::CheckOrders);
		}

		return true;
	}

	/**
	 *
	 * Delete a destination (like station, waypoint, ..) from the orders of vehicles
	 *
	 * @param dest type and station has to be set. This order will be removed from all orders of vehicles
	 *
	 */
	static void DeleteDestinationFromVehicleOrder(Order dest)
	{
		boolean [] need_invalidate = {false};

		/* Go through all vehicles */
		Vehicle.forEach( (v) ->
		{
			if (v.type == 0 || v.orders == null)
				return; //continue; 

			/* Forget about this station if this station is removed */
			if (v.last_station_visited == dest.station && dest.type == OT_GOTO_STATION)
				v.last_station_visited = Station.INVALID_STATION;

			/* Check the current Order */
			if (v.getCurrent_order().type    == dest.type &&
					v.getCurrent_order().station == dest.station) {
				/* Mark the order as DUMMY */
				v.getCurrent_order().type = OT_DUMMY;
				v.getCurrent_order().flags = 0;
				Window.InvalidateWindow(Window.WC_VEHICLE_VIEW, v.index);
			}

			/* Clear the order from the order-list */
			need_invalidate[0] = false;

			v.forEachOrder( (order) ->
			{
				if (order.type == dest.type && order.station == dest.station) {
					/* Mark the order as DUMMY */
					order.type = OT_DUMMY;
					order.flags = 0;

					if (v.type == Vehicle.VEH_Aircraft) {
						/* Take out of airport queue
						 */
						if(v.queue_item != null)
						{
							v.queue_item.queue.del(v);
						}
					}

					need_invalidate[0] = true;
				}
			});

			/* Only invalidate once, and if needed */
			if (need_invalidate[0])
				Window.InvalidateWindow(Window.WC_VEHICLE_ORDERS, v.index);
		});
	}


	/**
	 *
	 * Check if we share our orders with an other vehicle
	 *
	 * @return Returns the vehicle who has the same order
	 *
	 */
	static boolean IsOrderListShared(final Vehicle v)
	{
		return v.next_shared != null || v.prev_shared != null;
	}


	static void InitializeOrders()
	{
		Global._backup_orders_tile = null;
	}



	// TODO @Deprecated
	public static int PackOrder(final Order order)
	{
		return (0xFFFF & order.station) << 16 | (0xFF & order.flags) << 8 | (0xFF & order.type);
	}

	// TODO @Deprecated
	public static Order UnpackOrder(int packed)
	{
		Order order = new Order();
		order.type    = BitOps.GB(packed,  0,  8);
		order.flags   = BitOps.GB(packed,  8,  8);
		order.station = BitOps.GB(packed, 16, 16);
		order.next    = null;
		//order.index   = 0; // avoid compiler warning
		return order;
	}






	/*
	static final SaveLoad _order_desc[] = {
		SLE_VAR(Order,type,					SLE_UINT8),
		SLE_VAR(Order,flags,				SLE_UINT8),
		SLE_VAR(Order,station,			SLE_UINT16),
		SLE_REF(Order,next,					REF_ORDER),

		// reserve extra space in savegame here. (currently 10 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U8 | SLE_VAR_NULL, 10, 5, 255),
		SLE_END()
	};

	static void Save_ORDR()
	{
		Order order;

		FOR_ALL_ORDERS(order) {
			if (order.type != OT_NOTHING) {
				SlSetArrayIndex(order.index);
				SlObject(order, _order_desc);
			}
		}
	}

	static void Load_ORDR()
	{
		if (CheckSavegameVersionOldStyle(5, 2)) {
			// Version older than 5.2 did not have a .next pointer. Convert them
			//    (in the old days, the orderlist was 5000 items big) 
			int len = SlGetFieldLength();
			int i;

			if (CheckSavegameVersion(5)) {
				// Pre-version 5 had an other layout for orders
				//    (int instead of int) * /
				int orders[5000];

				len /= sizeof(int);
				assert (len <= lengthof(orders));

				SlArray(orders, len, SLE_UINT16);

				for (i = 0; i < len; ++i) {
					if (!AddBlockIfNeeded(&_order_pool, i))
						error("Orders: failed loading savegame: too many orders");

					AssignOrder(GetOrder(i), UnpackVersion4Order(orders[i]));
				}
			} else if (CheckSavegameVersionOldStyle(5, 2)) {
				int orders[5000];

				len /= sizeof(int);
				assert (len <= lengthof(orders));

				SlArray(orders, len, SLE_UINT32);

				for (i = 0; i < len; ++i) {
					if (!AddBlockIfNeeded(&_order_pool, i))
						error("Orders: failed loading savegame: too many orders");

					AssignOrder(GetOrder(i), UnpackOrder(orders[i]));
				}
			}

			// Update all the next pointer 
			for (i = 1; i < len; ++i) {
				// The orders were built like this:
				//     Vehicle one had order[0], and as long as order++.type was not
				//     OT_NOTHING, it was part of the order-list of that vehicle 
				if (GetOrder(i).type != OT_NOTHING)
					GetOrder(i - 1).next = GetOrder(i);
			}
		} else {
			int index;

			while ((index = SlIterateArray()) != -1) {
				Order order;

				if (!AddBlockIfNeeded(&_order_pool, index))
					error("Orders: failed loading savegame: too many orders");

				order = GetOrder(index);
				SlObject(order, _order_desc);
			}
		}
	}

	final Chunk Handler _order_chunk_handlers[] = {
		{ 'ORDR', Save_ORDR, Load_ORDR, CH_ARRAY | CH_LAST},
	};

	 */

	public static void loadGame(ObjectInputStream oin) {
		//_order_pool = (MemoryPool<Order>) oin.readObject();
		Global._backup_orders_tile = null; // TODO we must restore it too	
	}

	public static void saveGame(ObjectOutputStream oos) {
		//oos.writeObject(_order_pool);		
	}
	
	public TileIndex getTargetXy() 
	{
		switch(getType()) 
		{
		case Order.OT_GOTO_STATION:			/* station order */
			return Station.GetStation(station).getXy() ;

		case Order.OT_GOTO_DEPOT:				/* goto depot order */
			return Depot.GetDepot(station).xy;

		case Order.OT_GOTO_WAYPOINT:	/* goto waypoint order */
			return WayPoint.GetWaypoint(station).xy;
		}		
		return null;
	}

	public boolean typeIs(int t) {
		return type == t;
	}

	public boolean isNonStop() {		
		return 0 != (flags & OF_NON_STOP);
	}

	public boolean hasFlag(int flag) {
		return BitOps.HASBITS(flags, flag);
	}

	public boolean hasFlags(int flag) {
		return BitOps.HASBITS(flags, flag);
	}

	public void setFlags(int flag) {
		flags |= flag;
	}

	public void resetFlags(int flag) {
		flags &= ~flag;
	}


	private String[] orderTypeNames = {
			"NOTHING (0)",
			"GOTO_STATION (1)",
			"GOTO_DEPOT (2)",
			"LOADING (3)",
			"LEAVESTATION (4)",
			"DUMMY (5)",
			"GOTO_WAYPOINT (6)",
	};

	private String[] orderFlagNames = {
			"TRANSFER (1)",
			"UNLOAD/PART_OF_ORDERS (2)",
			"FULL_LOAD/HALT_IN_DEPOT/SERVICE_IF_NEEDED (4)",
			"NON_STOP (8)"
	};


	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Order ");
		sb.append(orderTypeNames[type]);

		if( flags != 0 )
		{
			sb.append(" flags: ");
			for( int i = 0; i < 31; i++ )
			{
				if(0 != ( flags & (1<<i) ) )
					sb.append(orderFlagNames[i]+" ");
			}
		}

		return sb.toString();
	}

}
