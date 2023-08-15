package com.dzavalishin.game;
import java.util.Iterator;

import com.dzavalishin.ids.CargoID;
import com.dzavalishin.ids.EngineID;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StationID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.ids.UnitID;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.ifaces.AircraftStateHandler;
import com.dzavalishin.struct.GetNewVehiclePosResult;
import com.dzavalishin.struct.Point;
import com.dzavalishin.tables.AirConstants;
import com.dzavalishin.tables.AirCraftTables;
import com.dzavalishin.tables.AirportMovingData;
import com.dzavalishin.tables.EngineTables2;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.YearMonthDay;
import com.dzavalishin.wcustom.vehiclelist_d;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.MiscGui;
import com.dzavalishin.xui.OrderGui;
import com.dzavalishin.xui.VehicleGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Widget;
import com.dzavalishin.xui.Window;
import com.dzavalishin.xui.WindowDesc;
import com.dzavalishin.xui.WindowEvent;


public class AirCraft extends AirCraftTables {



	static public final int STATUS_BAR = Vehicle.STATUS_BAR;



	static final /*SpriteID*/ int _aircraft_sprite[] = {
			0x0EB5, 0x0EBD, 0x0EC5, 0x0ECD,
			0x0ED5, 0x0EDD, 0x0E9D, 0x0EA5,
			0x0EAD, 0x0EE5, 0x0F05, 0x0F0D,
			0x0F15, 0x0F1D, 0x0F25, 0x0F2D,
			0x0EED, 0x0EF5, 0x0EFD, 0x0F35,
			0x0E9D, 0x0EA5, 0x0EAD, 0x0EB5,
			0x0EBD, 0x0EC5
	};

	/**
	 *  Find the nearest hangar to v
	 * 
	 * @return Station ID or INVALID_STATION if the player does not have any suitable airports (like helipads only)
	 */
	static StationID FindNearestHangar(final Vehicle v)
	{
		int best = 0;
		int index = StationID.getInvalid().id;

		Iterator<Station> ii = Station.getIterator();
		while(ii.hasNext())
		{
			Station st = ii.next();

			if (st.owner.equals(v.owner) && 0 != (st.facilities & Station.FACIL_AIRPORT) &&
					Airport.GetAirport(st.airport_type).nof_depots() > 0) {
				int distance;

				// don't crash the plane if we know it can't land at the airport
				if (BitOps.HASBIT(v.subtype, 1) && st.airport_type == Airport.AT_SMALL &&
						!Global._cheats.no_jetcrash.value)
					continue;

				distance = Map.DistanceSquare(v.tile, st.airport_tile);
				if (distance < best || index == Station.INVALID_STATION) {
					best = distance;
					index = st.index;
				}
			}
		}
		return StationID.get(index);
	}

	/*
	// returns true if vehicle v have an airport in the schedule, that has a hangar
	static boolean HaveHangarInOrderList(Vehicle v)
	{
		final Order order;

		FOR_VEHICLE_ORDERS(v, order) {
			final Station st = Station.GetStation(order.station);
			if (st.owner == v.owner && st.facilities & FACIL_AIRPORT) {
				// If an airport doesn't have terminals (so no landing space for airports),
				// it surely doesn't have any hangars
				if (GetAirport(st.airport_type).terminals != null)
					return true;
			}
		}

		return false;
	}
	 */

	public static int GetAircraftImage(final Vehicle v, int direction)
	{
		int spritenum = v.spritenum;

		if (Sprite.is_custom_sprite(spritenum)) {
			int sprite = Engine.GetCustomVehicleSprite(v, direction);

			if (sprite != 0) return sprite;
			spritenum = EngineTables2.orig_aircraft_vehicle_info[v.engine_type.id - Global.AIRCRAFT_ENGINES_INDEX].image_index;
		} 
		return direction + _aircraft_sprite[spritenum];
	}

	public static void DrawAircraftEngine(int x, int y, int engine, int image_ormod)
	{
		int spritenum = Engine.AircraftVehInfo(engine).image_index;
		int sprite = (6 + _aircraft_sprite[spritenum]);


		if (Sprite.is_custom_sprite(spritenum)) {
			sprite = Engine.GetCustomVehicleIcon(engine, 6);
			if (0==sprite)
				spritenum = EngineTables2.orig_aircraft_vehicle_info[engine - Global.AIRCRAFT_ENGINES_INDEX].image_index;
		}

		Gfx.DrawSprite(sprite | image_ormod, x, y);

		if ((Engine.AircraftVehInfo(engine).subtype & 1) == 0) {
			Gfx.DrawSprite(Sprite.SPR_ROTOR_STOPPED, x, y - 5);
		}
	}

	static int EstimateAircraftCost(EngineID engine_type)
	{
		return Engine.AircraftVehInfo(engine_type.id).base_cost * (((int)Global._price.aircraft_base)>>3)>>5;
	}


	/** 
	 * Build an aircraft.
	 * 
	 * @param x,y tile coordinates of depot where aircraft is built
	 * @param p1 aircraft type being built (engine)
	 * @param p2 unused
	 */
	static int CmdBuildAircraft(int x, int y, int flags, int p1, int p2)
	{
		int value;
		Vehicle v, u, w;
		Vehicle [] vl  = new Vehicle[3];
		UnitID unit_num;
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		final AircraftVehicleInfo avi;
		Engine e;

		if (!Engine.IsEngineBuildable(p1, Vehicle.VEH_Aircraft)) return Cmd.CMD_ERROR;

		value = EstimateAircraftCost( EngineID.get(p1) );

		// to just query the cost, it is not neccessary to have a valid tile (automation/AI)
		if(0 != (flags & Cmd.DC_QUERY_COST)) return value;

		if (!IsAircraftHangarTile(tile) || !tile.IsTileOwner(PlayerID.getCurrent())) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_NEW_VEHICLES);

		avi = Engine.AircraftVehInfo(p1);
		// allocate 2 or 3 vehicle structs, depending on type
		if (!Vehicle.AllocateVehicles(vl, (avi.subtype & 1) == 0 ? 3 : 2) ) { // ||Order.IsOrderPoolFull()) {
			return Cmd.return_cmd_error(Str.STR_00E1_TOO_MANY_VEHICLES_IN_GAME);
		}

		unit_num = Vehicle.GetFreeUnitNumber(Vehicle.VEH_Aircraft);
		if (unit_num.id > Global._patches.max_aircraft)
			return Cmd.return_cmd_error(Str.STR_00E1_TOO_MANY_VEHICLES_IN_GAME);

		if(0 != (flags & Cmd.DC_EXEC)) {
			v = vl[0];
			u = vl[1];

			v.unitnumber = unit_num;
			v.type = u.type = Vehicle.VEH_Aircraft;
			v.direction = 3;

			v.owner = u.owner = PlayerID.getCurrent();

			v.tile = tile;
			//			u.tile = 0;

			x = tile.TileX() * 16 + 5;
			y = tile.TileY() * 16 + 3;

			v.x_pos = u.x_pos = x;
			v.y_pos = u.y_pos = y;

			u.z_pos = Landscape.GetSlopeZ(x, y);
			v.z_pos = u.z_pos + 1;

			v.x_offs = v.y_offs = -1;
			//			u.delta_x = u.delta_y = 0;

			v.sprite_width = v.sprite_height = 2;
			v.z_height = 5;

			u.sprite_width = u.sprite_height = 2;
			u.z_height = 1;

			v.assignStatus( Vehicle.VS_HIDDEN | Vehicle.VS_STOPPED | Vehicle.VS_DEFPAL );
			u.assignStatus( Vehicle.VS_HIDDEN | Vehicle.VS_UNCLICKABLE | Vehicle.VS_DISASTER );

			v.spritenum = avi.image_index;
			//			v.cargo_count = u.number_of_pieces = 0;

			v.cargo_cap = avi.passenger_capacity;
			u.cargo_cap = avi.mail_capacity;

			v.cargo_type = AcceptedCargo.CT_PASSENGERS;
			u.cargo_type = AcceptedCargo.CT_MAIL;

			v.string_id = Str.STR_SV_AIRCRAFT_NAME;
			//			v.next_order_param = v.next_order = 0;

			//			v.load_unload_time_rem = 0;
			//			v.progress = 0;
			v.last_station_visited = Station.INVALID_STATION;
			//			v.destination_coords = 0;

			v.max_speed = avi.max_speed;
			v.acceleration = avi.acceleration;
			v.engine_type = EngineID.get( p1 );

			v.subtype = (avi.subtype & 1) == 0 ? 0 : 2;
			v.value = value;

			u.subtype = 4;

			e = Engine.GetEngine(p1);
			v.reliability = e.getReliability();
			v.reliability_spd_dec = e.reliability_spd_dec;
			v.max_age = e.getLifelength() * 366;

			Global._new_aircraft_id = VehicleID.get( v.index );
			Global._new_vehicle_id = VehicleID.get( v.index );

			v.air.pos = Airport.MAX_ELEMENTS;

			/* When we click on hangar we know the tile it is on. By that we know
			 * its position in the array of depots the airport has.....we can search
			 * layout for #th position of depot. Since layout must start with a listing
			 * of all depots, it is simple */
			{
				final Station  st = Station.GetStation(tile.getMap().m2);
				final Airport apc = Airport.GetAirport(st.airport_type);
				int i;

				int nof_depots = apc.airport_depots.length;

				for (i = 0; i < /*apc.*/nof_depots; i++) {
					if( st.airport_tile.iadd( TileIndex.ToTileIndexDiff(apc.airport_depots[i])).equals(tile) ) {
						assert(apc.getLayoutItem(i).heading == Airport.HANGAR);
						v.air.pos = apc.getLayoutItem(i).position;
						break;
					}
				}
				// to ensure v.air.pos has been given a value
				assert(v.air.pos != Airport.MAX_ELEMENTS);
			}

			v.air.state = Airport.HANGAR;
			v.air.previous_pos = v.air.pos;
			v.air.targetairport = tile.getMap().m2;
			v.next = u;

			v.service_interval = Global._patches.servint_aircraft;

			v.date_of_last_service = Global.get_date();
			v.build_year = u.build_year =  Global.get_cur_year();

			v.cur_image = u.cur_image = 0xEA0;

			v.random_bits = Vehicle.VehicleRandomBits();
			u.random_bits = Vehicle.VehicleRandomBits();

			v.VehiclePositionChanged();
			u.VehiclePositionChanged();

			// Aircraft with 3 vehicles (chopper)?
			if (v.subtype == 0) {
				w = vl[2];

				u.next = w;

				w.type = Vehicle.VEH_Aircraft;
				w.direction = 0;
				w.owner = PlayerID.getCurrent();
				w.x_pos = v.getX_pos();
				w.y_pos = v.getY_pos();
				w.z_pos = v.z_pos + 5;
				w.x_offs = w.y_offs = -1;
				w.sprite_width = w.sprite_height = 2;
				w.z_height = 1;
				w.assignStatus( Vehicle.VS_HIDDEN | Vehicle.VS_UNCLICKABLE );
				w.subtype = 6;
				w.cur_image = Sprite.SPR_ROTOR_STOPPED;
				w.random_bits = Vehicle.VehicleRandomBits();
				w.VehiclePositionChanged();
			}

			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
			VehicleGui.RebuildVehicleLists();
			Window.InvalidateWindow(Window.WC_COMPANY, v.owner.id);
			if (Player.IsLocalPlayer())
				Window.InvalidateWindow(Window.WC_REPLACE_VEHICLE, Vehicle.VEH_Aircraft); //updates the replace Aircraft window
		}

		return value;
	}

	public static boolean IsAircraftHangarTile(TileIndex tile)
	{
		// 0x56 - hangar facing other way international airport (86)
		// 0x20 - hangar large airport (32)
		// 0x41 - hangar small airport (65)
		return tile != null && tile.IsTileType( TileTypes.MP_STATION) &&
				(tile.getMap().m5 == 32 || tile.getMap().m5 == 65 || tile.getMap().m5 == 86);
	}

	static boolean CheckStoppedInHangar(final Vehicle  v)
	{
		if(!v.isStopped() || !IsAircraftHangarTile(v.tile)) {
			Global._error_message = Str.STR_A01B_AIRCRAFT_MUST_BE_STOPPED;
			return false;
		}

		return true;
	}


	static void DoDeleteAircraft(Vehicle v)
	{
		Window.DeleteWindowById(Window.WC_VEHICLE_VIEW, v.index);
		VehicleGui.RebuildVehicleLists();
		Window.InvalidateWindow(Window.WC_COMPANY, v.owner.id);
		v.DeleteVehicleChain();
		Window.InvalidateWindowClasses(Window.WC_AIRCRAFT_LIST);
	}

	/** Sell an aircraft.
	 * @param x,y unused
	 * @param p1 vehicle ID to be sold
	 * @param p2 unused
	 */
	static int CmdSellAircraft(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Aircraft || !Player.CheckOwnership(v.owner) || !CheckStoppedInHangar(v))
			return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_NEW_VEHICLES);

		if(0 != (flags & Cmd.DC_EXEC)) {
			// Invalidate depot
			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
			DoDeleteAircraft(v);
			if (Player.IsLocalPlayer())
				Window.InvalidateWindow(Window.WC_REPLACE_VEHICLE, Vehicle.VEH_Aircraft); // updates the replace Aircraft window
		}

		return -v.value;
	}

	/** Start/Stop an aircraft.
	 * @param x,y unused
	 * @param p1 aircraft ID to start/stop
	 * @param p2 unused
	 */
	static int CmdStartStopAircraft(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Aircraft || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if (Global._patches.allow_municipal_airports.get() 
				&& mAirport.MA_VehicleIsAtMunicipalAirport(v) 
				&& !v.isStopped())
			return Cmd.return_cmd_error(Str.STR_MA_CANT_STOP_AT_MUNICIPAL);

		// cannot stop airplane when in flight, or when taking off / landing
		if (v.air.state >= Airport.STARTTAKEOFF)
			return Cmd.return_cmd_error(Str.STR_A017_AIRCRAFT_IS_IN_FLIGHT);

		if(0 != (flags & Cmd.DC_EXEC)) {
			v.toggleStopped();
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
			Window.InvalidateWindowClasses(Window.WC_AIRCRAFT_LIST);
		}

		return 0;
	}

	/** Send an aircraft to the hangar.
	 * @param x,y unused
	 * @param p1 vehicle ID to send to the hangar
	 * @param p2 various bitmasked elements
	 * - p2 = 0      - aircraft goes to the depot and stays there (user command)
	 * - p2 non-zero - aircraft will try to goto a depot, but not stop there (eg forced servicing)
	 * - p2 (bit 17) - aircraft will try to goto a depot at the next airport
	 */
	static int CmdSendAircraftToHangar(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;
		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Aircraft || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT && p2 == 0) {
			if(0 != (flags & Cmd.DC_EXEC)) {
				if(0 !=  (v.getCurrent_order().flags & Order.OF_UNLOAD)) 
					v.cur_order_index++;

				v.getCurrent_order().type = Order.OT_DUMMY;
				v.getCurrent_order().flags = 0;
				Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
			}
		} else {
			boolean next_airport_has_hangar = true;
			/* XXX - I don't think p2 is any valid station cause all calls use either 0, 1, or 1<<16!!!!!!!!! */
			/*StationID*/ int next_airport_index = (BitOps.HASBIT(p2, 17)) ? p2 : v.air.targetairport;
			Station st = Station.GetStation(next_airport_index);
			// If an airport doesn't have terminals (so no landing space for airports),
			// it surely doesn't have any hangars
			if (!st.IsValidStation() || st.airport_tile == null || Airport.GetAirport(st.airport_type).nof_depots() == 0) {
				StationID station;

				if (p2 != 0) return Cmd.CMD_ERROR;
				// the aircraft has to search for a hangar on its own
				station = FindNearestHangar(v);

				next_airport_has_hangar = false;
				if (station.id == Station.INVALID_STATION) return Cmd.CMD_ERROR;
				st = Station.GetStation(station.id);
				next_airport_index = station.id;

			}

			if(0 != (flags & Cmd.DC_EXEC)) {
				v.getCurrent_order().type = Order.OT_GOTO_DEPOT;
				v.getCurrent_order().flags = BitOps.HASBIT(p2, 16) ? 0 : Order.OF_NON_STOP | Order.OF_FULL_LOAD;
				v.getCurrent_order().station = next_airport_index;
				Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
				if (BitOps.HASBIT(p2, 17) || (p2 == 0 && v.air.state == Airport.FLYING && !next_airport_has_hangar)) {
					// the aircraft is now heading for a different hangar than the next in the orders
					AircraftNextAirportPos_and_Order(v);
					v.air.targetairport = next_airport_index;
				}
			}
		}

		return 0;
	}

	/** Change the service interval for aircraft.
	 * @param x,y unused
	 * @param p1 vehicle ID that is being service-interval-changed
	 * @param p2 new service interval
	 */
	static int CmdChangeAircraftServiceInt(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		int serv_int = Depot.GetServiceIntervalClamped(p2); /* Double check the service interval from the user-input */

		if (serv_int != p2 || !Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Aircraft || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			v.service_interval = serv_int;
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_DETAILS, v.index, 7);
		}

		return 0;
	}

	/** Refits an aircraft to the specified cargo type.
	 * @param x,y unused
	 * @param p1 vehicle ID of the aircraft to refit
	 * @param p2 various bitstuffed elements
	 * - p2 = (bit 0-7) - the new cargo type to refit to
	 */
	static int CmdRefitAircraft(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		int pass, mail;
		int cost;
		CargoID new_cid = CargoID.get( BitOps.GB(p2, 0, 8) );
		final AircraftVehicleInfo avi;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Aircraft || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;
		if (!CheckStoppedInHangar(v)) return Cmd.return_cmd_error(Str.STR_A01B_AIRCRAFT_MUST_BE_STOPPED);

		avi = Engine.AircraftVehInfo(v.getEngine_type().id);

		/* Check cargo */
		if (new_cid.id > AcceptedCargo.NUM_CARGO || !Vehicle.CanRefitTo(v.getEngine_type(), new_cid)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_AIRCRAFT_RUN);

		switch (new_cid.id) {
		case AcceptedCargo.CT_PASSENGERS:
			pass = avi.passenger_capacity;
			break;
		case AcceptedCargo.CT_MAIL:
			pass = avi.passenger_capacity + avi.mail_capacity;
			break;
		case AcceptedCargo.CT_GOODS:
			pass = avi.passenger_capacity + avi.mail_capacity;
			pass /= 2;
			break;
		default:
			pass = avi.passenger_capacity + avi.mail_capacity;
			pass /= 4;
			break;
		}
		Global._aircraft_refit_capacity = pass;

		cost = 0;
		if (v.owner.IS_HUMAN_PLAYER() && new_cid.id != v.getCargo_type()) {
			cost = ((int)Global._price.aircraft_base) >> 7;
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			Vehicle u;
			v.cargo_cap = pass;

			u = v.next;
			mail = (new_cid.id != AcceptedCargo.CT_PASSENGERS) ? 0 : avi.mail_capacity;
			u.cargo_cap = mail;
			v.cargo_count = u.cargo_count = 0;
			v.cargo_type = new_cid.id;
			Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
		}

		return cost;
	}


	static void CheckIfAircraftNeedsService(Vehicle v)
	{
		final Station  st;

		if (Global._patches.servint_aircraft == 0) return;
		if (!v.VehicleNeedsService()) return;
		if(v.isStopped()) return;

		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT && 0 !=
				(v.getCurrent_order().flags & Order.OF_HALT_IN_DEPOT))
			return;

		if (Global._patches.gotodepot.get() && v.VehicleHasDepotOrders()) return;

		st = Station.GetStation(v.getCurrent_order().station);

		// only goto depot if the target airport has terminals (eg. it is airport)
		if (st.getXy() != null && st.airport_tile != null && Airport.GetAirport(st.airport_type).terminals != null) {
			//			printf("targetairport = %d, st.index = %d\n", v.air.targetairport, st.index);
			//			v.air.targetairport = st.index;
			v.getCurrent_order().type = Order.OT_GOTO_DEPOT;
			v.getCurrent_order().flags = Order.OF_NON_STOP;
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
		} else if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT) {
			v.getCurrent_order().type = Order.OT_DUMMY;
			v.getCurrent_order().flags = 0;
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
		}
	}

	public static void OnNewDay_Aircraft(Vehicle v)
	{
		if (v.subtype > 2) return;

		if ((++v.day_counter & 7) == 0) v.DecreaseVehicleValue();

		Order.CheckOrders(v.index, Order.OC_INIT);

		v.CheckVehicleBreakdown();
		v.AgeVehicle();
		CheckIfAircraftNeedsService(v);

		if(v.isStopped()) return;

		int cost = (int) (Engine.AircraftVehInfo(v.getEngine_type().id).running_cost * Global._price.aircraft_running / 364);

		v.profit_this_year -= cost >> 8;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_AIRCRAFT_RUN);
		Player.SubtractMoneyFromPlayerFract(v.owner, cost);

		Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
		Window.InvalidateWindowClasses(Window.WC_AIRCRAFT_LIST);
	}

	public static void AircraftYearlyLoop()
	{
		Vehicle.forEach( (v) ->
		{
			if (v.type == Vehicle.VEH_Aircraft && v.subtype <= 2) {
				v.profit_last_year = v.profit_this_year;
				v.profit_this_year = 0;
				Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
			}
		});
	}

	static void AgeAircraftCargo(Vehicle v)
	{
		if (Global._age_cargo_skip_counter != 0) return;

		do {
			if (v.cargo_days != 0xFF) v.cargo_days++;
			v = v.next;
		} while (v != null);
	}

	static void HelicopterTickHandler(Vehicle v)
	{
		Vehicle u;
		int tick,spd;
		int img;

		u = v.next.next;

		if( u.isHidden() ) return;

		// if true, helicopter rotors do not rotate. This should only be the case if a helicopter is
		// loading/unloading at a terminal or stopped
		if (v.getCurrent_order().type == Order.OT_LOADING || v.isStopped()) {
			if (u.cur_speed != 0) {
				u.cur_speed++;
				if (u.cur_speed >= 0x80 && u.cur_image == Sprite.SPR_ROTOR_MOVING_3) {
					u.cur_speed = 0;
				}
			}
		} else {
			if (u.cur_speed == 0)
				u.cur_speed = 0x70;

			if (u.cur_speed >= 0x50)
				u.cur_speed--;
		}

		tick = ++u.tick_counter;
		spd = u.cur_speed >> 4;

		if (spd == 0) {
			img = Sprite.SPR_ROTOR_STOPPED;
			if (u.cur_image == img) return;
		} else if (tick >= spd) {
			u.tick_counter = 0;
			img = u.cur_image + 1;
			if (img > Sprite.SPR_ROTOR_MOVING_3) img = Sprite.SPR_ROTOR_MOVING_1;
		} else {
			return;
		}

		u.cur_image = img;

		u.BeginVehicleMove();
		u.VehiclePositionChanged();
		u.EndVehicleMove();
	}

	static void SetAircraftPosition(Vehicle v, int x, int y, int z)
	{
		Vehicle u;
		int yt;

		v.x_pos = x;
		v.y_pos = y;
		v.z_pos = z;

		v.cur_image = GetAircraftImage(v, v.direction);

		v.BeginVehicleMove();
		v.VehiclePositionChanged();
		v.EndVehicleMove();

		u = v.next;

		yt = y - ((v.z_pos-Landscape.GetSlopeZ(x, y-1)) >> 3);
		u.x_pos = x;
		u.y_pos = yt;
		u.z_pos = Landscape.GetSlopeZ(x,yt);
		u.cur_image = v.cur_image;

		u.BeginVehicleMove();
		u.VehiclePositionChanged();
		u.EndVehicleMove();

		u = u.next;
		if (u != null) {
			u.x_pos = x;
			u.y_pos = y;
			u.z_pos = z + 5;

			u.BeginVehicleMove();
			u.VehiclePositionChanged();
			u.EndVehicleMove();
		}
	}

	static void ServiceAircraft(Vehicle v)
	{
		Vehicle u;

		v.cur_speed = 0;
		v.subspeed = 0;
		v.progress = 0;
		v.setHidden(true);

		u = v.next;
		u.setHidden(true);
		u = u.next;
		if (u != null) {
			u.setHidden(true);
			u.cur_speed = 0;
		}


		SetAircraftPosition(v, v.getX_pos(), v.getY_pos(), v.z_pos);
		Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);

		v.VehicleServiceInDepot();
		Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
	}

	static void PlayAircraftSound(final Vehicle  v)
	{
		v.SndPlayVehicleFx(Engine.AircraftVehInfo(v.engine_type.id).sfx);
	}

	static int UpdateAircraftSpeed(Vehicle v)
	{
		int spd = v.acceleration * 2;
		int t;
		int new_speed;

		new_speed = v.getMax_speed() * Global._patches.aircraft_speed_coeff;

		// Don't go faster than max
		if(v.air.desired_speed > new_speed) {
			v.air.desired_speed = new_speed;
		}

		//spd = v.cur_speed + v.acceleration;
		t = 0xFF & v.subspeed;
		v.subspeed = 0xFF & (t + spd);
		spd = 0xFFFF & Math.min( v.cur_speed + (spd >> 8) + ((v.subspeed < t) ? 1 : 0), new_speed);

		// adjust speed for broken vehicles
		if(v.isAircraftBroken()) spd = Math.min(spd, v.getMax_speed() / 3 * Global._patches.aircraft_speed_coeff);

		if(v.air.state == Airport.FLYING && v.subtype == 0 && v.air.desired_speed == 0) {
			if(spd > 0)
				spd = 0;
		}

		// If landing, do not speed up!
		if((v.air.state == Airport.LANDING || v.air.state == Airport.ENDLANDING) && spd > 15 * Global._patches.aircraft_speed_coeff)
			spd = Math.min(v.cur_speed, spd);

		//updates status bar only if speed have changed to save CPU time
		if (spd != v.cur_speed) {
			v.cur_speed = spd;
			if (Global._patches.vehicle_speed)
				Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
		}

		if (0==(v.direction & 1)) spd = spd * 3 / 4;

		if (spd == 0) return 0; //false;

		if ( ((++spd) & 0xFFFF) == 0)
			return 1; //true;


		spd += v.progress;
		spd &= 0xFFFF;
		v.progress = 0xFF & spd;
		return (spd >> 8);
	}

	// get Aircraft running altitude
	static int GetAircraftFlyingAltitude(Vehicle v)
	{
		int queue_adjust;
		int maxz;

		queue_adjust = 0;
		if(v.queue_item != null)
			queue_adjust = 32 * v.queue_item.queue.getPos(v)-1;

		maxz = 162;
		if(v.getMax_speed() > 37 * Global._patches.aircraft_speed_coeff) {
			maxz = 171;
			if(v.getMax_speed() > 74 * Global._patches.aircraft_speed_coeff) {
				maxz = 180;
			}
		}

		return maxz + queue_adjust;
	}


	private static final int _delta_coord[] = {
			-1,-1,-1, 0, 1, 1, 1, 0, /* x */
			-1, 0, 1, 1, 1, 0,-1,-1, /* y */
	};

	/* returns true if staying in the same tile */
	static boolean GetNewAircraftPos(Vehicle v, GetNewVehiclePosResult gp, int tilesMoved)
	{
		//tilesMoved /= 256;
		
		int x = v.getX_pos() + _delta_coord[v.direction] * tilesMoved;
		int y = v.getY_pos() + _delta_coord[v.direction + 8] * tilesMoved;

		gp.x = x;
		gp.y = y;
		gp.old_tile = v.tile;
		gp.new_tile = TileIndex.TileVirtXY(x,y);
		return gp.old_tile.equals( gp.new_tile );
	}

	static boolean AircraftController(Vehicle v)
	{
		Station st;
		final AirportMovingData amd;
		Vehicle u;
		int dirdiff,newdir;
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();
		int dist, desired_dist;
		int x,y;
		int tilesMoved;
		int z,maxz,curz;

		st = Station.GetStation(v.air.targetairport);

		// prevent going to 0,0 if airport is deleted.
		{
			TileIndex tile = st.airport_tile;

			if (tile == null) tile = st.getXy();
			// xy of destination
			x = tile.TileX() * 16;
			y = tile.TileY() * 16;
		}

		// get airport moving data
		assert(v.air.pos < Airport.GetAirport(st.airport_type).getNofElements());
		amd = _airport_moving_datas[st.airport_type][v.air.pos];

		// Helicopter raise
		if(0 != (amd.flag & AirportMovingData.AMED_HELI_RAISE)) {
			u = v.next.next;

			// Make sure the rotors don't rotate too fast
			if (u.cur_speed > 32) {
				v.cur_speed = 0;
				if (--u.cur_speed == 32) v.SndPlayVehicleFx(Snd.SND_18_HELICOPTER);
			} else {
				u.cur_speed = 32;
				if (UpdateAircraftSpeed(v) >= 1) {
					v.tile = null;

					// Reached altitude?
					if (v.z_pos >= 184) {
						v.cur_speed = 0;
						return true;
					}
					SetAircraftPosition(v, v.getX_pos(), v.getY_pos(), v.z_pos+1);
				}
			}
			return false;
		}

		// Helicopter landing.
		if(0 != (amd.flag & AirportMovingData.AMED_HELI_LOWER)) {
			if (UpdateAircraftSpeed(v) >= 1) {
				if (st.airport_tile == null) {
					// FIXME - AircraftController . if station no longer exists, do not land
					// helicopter will circle until sign disappears, then go to next order
					// * what to do when it is the only order left, right now it just stays in 1 place
					v.air.state = Airport.FLYING;
					AircraftNextAirportPos_and_Order(v);
					return false;
				}

				// Vehicle is now at the airport.
				v.tile = st.airport_tile;

				// Find altitude of landing position.
				z = Landscape.GetSlopeZ(x, y) + 1;
				if (st.airport_type == Airport.AT_OILRIG) z += 54;
				if (st.airport_type == Airport.AT_HELIPORT) z += 60;

				if (z == v.z_pos) {
					u = v.next.next;

					// Increase speed of rotors. When speed is 80, we've landed.
					if (u.cur_speed >= 80) return true;
					u.cur_speed += 4;
				} else if (v.z_pos > z) {
					SetAircraftPosition(v, v.getX_pos(), v.getY_pos(), v.z_pos-1);
				} else {
					SetAircraftPosition(v, v.getX_pos(), v.getY_pos(), v.z_pos+1);
				}
			}
			return false;
		}

		// Get distance from destination pos to current pos.
		dist = Math.abs(x + amd.x - v.getX_pos()) +  Math.abs(y + amd.y - v.getY_pos());

		// Clear queues when there's no patch
		if(!Global._patches.aircraft_queueing && v.queue_item != null) {
			v.queue_item.queue.del(v);
			v.z_pos = GetAircraftFlyingAltitude(v);		
		}

		// If target airport is VERY busy (queue larger than 5), always add to queue
		if(st.airport_queue.size > 5  && v.air.state == Airport.FLYING) {
			// If it's already in the queue, don't re-add it
			// Otherwise, add it to queue - but don't add helicopters!
			// otherwise, helicopters will be part of the queue and can't land separately!
			if(v.queue_item == null && (Global._patches.aircraft_queueing && v.subtype != 0)) {
				// Add to queue
				assert(st.airport_queue.push(v));
			}
		}

		// If the aircraft is flying and is within range of an airport, add it to the queue
		if(dist < 1000 && v.air.state == Airport.FLYING) {
			// If it's already in the queue, don't re-add it
			// Otherwise, add it to queue - but don't add helicopters!
			// otherwise, helicopters will be part of the queue and can't land separately!
			if(v.queue_item == null && Global._patches.aircraft_queueing && v.subtype != 0) {
				// Add to queue
				st.airport_queue.push(v);
			}
		}

		// Calculate desired distance
		if(v.subtype != 0) {
			if(v.queue_item != null)
				desired_dist = v.queue_item.queue.getPos(v) * 250;
			else
				desired_dist = st.airport_queue.size * 250;
		} else {
			// Helicopters
			if(v.queue_item != null)
				desired_dist = v.queue_item.queue.getPos(v) * 75;
			else
				desired_dist = st.helicopter_queue.size * 75;
		}

		// Add helicopters to their own queue, if in range of airport
		if(dist < 1000 && v.air.state == Airport.FLYING && v.subtype == 0 && v.queue_item == null) {
			st.helicopter_queue.push(v);
		}

		// Try to reach desired distance
		if(Math.abs(desired_dist - dist) < 10) {
			// At or close to desired distance, maintain a good cruising speed
			v.air.desired_speed = Math.min(v.getMax_speed() * Global._patches.aircraft_speed_coeff, 36 * Global._patches.aircraft_speed_coeff);
		} else {
			if(dist < desired_dist && v.queue_item != null) {
				// Too close, slow down, but only if not near end of queue
				if(v.queue_item.queue.getPos(v) > 2)
					v.air.desired_speed = Math.min(v.getMax_speed() * Global._patches.aircraft_speed_coeff, 15 * Global._patches.aircraft_speed_coeff);
				else
					v.air.desired_speed = v.air.desired_speed = Math.min(v.getMax_speed() * Global._patches.aircraft_speed_coeff, 36 * Global._patches.aircraft_speed_coeff);
			} else {
				// Too far, speed up
				v.air.desired_speed = v.getMax_speed() * Global._patches.aircraft_speed_coeff;
			}
		}

		// All helicopters other than one in front stay in line
		if(v.queue_item != null) {
			if(v.air.state == Airport.FLYING && v.subtype == 0
					&& v.queue_item.queue.getPos(v) != 1) {
				if(dist < desired_dist) {
					v.cur_speed = 0;
					v.air.desired_speed = 0;
				}
			}
		}

		// Slow down if above desired speed
		if(v.air.state == Airport.FLYING && v.cur_speed > v.air.desired_speed)
			v.cur_speed--;

		// Need exact position?
		if (0==(amd.flag & AirportMovingData.AMED_EXACTPOS) && dist <= (((amd.flag & AirportMovingData.AMED_SLOWTURN) != 0) ? 8 : 4))
			return true;

		// At final pos?
		if (dist == 0) {
			if (v.cur_speed > 12) v.cur_speed = 12;

			// Change direction smoothly to final direction.
			dirdiff =  (amd.direction - v.direction);
			// if distance is 0, and plane points in right direction, no point in calling
			// UpdateAircraftSpeed(). So do it only afterwards
			if (dirdiff == 0) {
				v.cur_speed = 0;
				return true;
			}

			if (UpdateAircraftSpeed(v) < 1)
				return false;

			v.direction = (v.direction+((dirdiff&7)<5?1:-1)) & 7;
			v.cur_speed >>= 1;

			SetAircraftPosition(v, v.getX_pos(), v.getY_pos(), v.z_pos);
			return false;
		}

		if (0==(amd.flag & AirportMovingData.AMED_NOSPDCLAMP) && v.cur_speed > 12) v.cur_speed = 12;

		tilesMoved = UpdateAircraftSpeed(v);
		if(tilesMoved < 1)
			return false;

		if (v.load_unload_time_rem != 0) v.load_unload_time_rem--;

		// Turn. Do it slowly if in the air.
		newdir =  Vehicle.GetDirectionTowards(v, x + amd.x, y + amd.y);
		if (newdir != v.direction)
		{
			v.direction = newdir;
			if(0 != (amd.flag & AirportMovingData.AMED_SLOWTURN)) {
				if (v.load_unload_time_rem == 0) v.load_unload_time_rem = 8;
			} else {
				v.cur_speed >>= 1;
				//if(v.air.state != Airport.FLYING)
				/*{
				// [dz] fix rotational movement on taxi to takeoff point
				// if direction is still wrong slow down more
				if(v.direction != Vehicle.GetDirectionTowards(v, x + amd.x, y + amd.y))
				{
					v.cur_speed >>= 1;
					v.subspeed = 0;
					v.progress = 0;
				}
				}*/
			}
		}

		// Move vehicle.
		GetNewAircraftPos(v, gp, tilesMoved);
		v.tile = gp.new_tile;

		// If vehicle is in the air, use tile coordinate 0.
		if(0 != (amd.flag & (AirportMovingData.AMED_TAKEOFF | AirportMovingData.AMED_SLOWTURN | AirportMovingData.AMED_LAND))) 
			v.tile = TileIndex.get(0); //null; // [dz] comment above said '0', not invalid

		// Adjust Z for land or takeoff?
		z = v.z_pos;

		if(0 != (amd.flag & AirportMovingData.AMED_TAKEOFF)) {
			z += 2;
			maxz = GetAircraftFlyingAltitude(v);
			if (z > maxz) z = maxz;
		}

		if(0 != (amd.flag & AirportMovingData.AMED_LAND)) {
			if (st.airport_tile == null) {
				v.air.state = Airport.FLYING;
				AircraftNextAirportPos_and_Order(v);
				// get aircraft back on running altitude
				SetAircraftPosition(v, gp.x, gp.y, GetAircraftFlyingAltitude(v));
				return false;
			}

			curz = Landscape.GetSlopeZ(x, y) + 1;

			if (curz > z) {
				z++;
			} else {
				int t = Math.max(1, dist - 4);

				z -= ((z - curz) + t - 1) / t;
				if (z < curz) z = curz;
			}
		}

		// We've landed. Decrase speed when we're reaching end of runway.
		if(0 != (amd.flag & AirportMovingData.AMED_BRAKE)) {
			curz = Landscape.GetSlopeZ(x, y) + 1;

			if (z > curz) {
				z--;
			} else if (z < curz) {
				z++;
			}

			if (dist < 64 && v.cur_speed > 12) v.cur_speed -= 4;
		}

		curz = z;
		if(v.queue_item != null)
		{
			curz = GetAircraftFlyingAltitude(v);
		}

		if(curz < z)
		{
			z--;
		} else if(curz > z)
		{
			z++;
		}

		SetAircraftPosition(v, gp.x, gp.y, z);
		return false;
	}

	static final int _crashed_aircraft_moddir[] = {
			-1,0,0,1
	};

	static void HandleCrashedAircraft(Vehicle v)
	{
		int r;
		Station st;
		int z;

		v.air.crashed_counter++;

		st = Station.GetStation(v.air.targetairport);

		// make aircraft crash down to the ground
		if (v.air.crashed_counter < 500 && st.airport_tile==null && ((v.air.crashed_counter % 3) == 0) ) {
			z = Landscape.GetSlopeZ(v.getX_pos(), v.getY_pos());
			v.z_pos -= 1;
			if (v.z_pos == z) {
				v.air.crashed_counter = 500;
				v.z_pos++;
			}
		}

		if (v.air.crashed_counter < 650) {
			if (BitOps.CHANCE16(1,32)) 
			{
				r = Hal.Random();
				v.direction = (v.direction + _crashed_aircraft_moddir[BitOps.GB(r, 16, 2)]) & 7;
				SetAircraftPosition(v, v.getX_pos(), v.getY_pos(), v.z_pos);
				r = Hal.Random();
				v.CreateEffectVehicleRel(
						BitOps.GB(r, 0, 4) + 4,
						BitOps.GB(r, 4, 4) + 4,
						BitOps.GB(r, 8, 4),
						Vehicle.EV_EXPLOSION_SMALL);
			}
		} else if (v.air.crashed_counter >= 10000) {
			// remove rubble of crashed airplane

			// clear runway-in on all airports, set by crashing plane
			// small airports use AIRPORT_BUSY, city airports use RUNWAY_IN_OUT_block, etc.
			// but they all share the same number
			//st.airport_flags = BitOps.RETCLRBITS(st.airport_flags, Airport.RUNWAY_IN_block);
			st.resetAirportBlocks(Airport.RUNWAY_IN_block);

			v.BeginVehicleMove();
			v.EndVehicleMove();

			DoDeleteAircraft(v);
		}
	}

	static void HandleBrokenAircraft(Vehicle v)
	{
		if (v.breakdown_ctr != 1) {
			v.breakdown_ctr = 1;
			v.setAircraftBroken(true);

			if (v.breakdowns_since_last_service != 255)
				v.breakdowns_since_last_service++;
			Window.InvalidateWindow(Window.WC_VEHICLE_VIEW, v.index);
			Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
		}
	}


	static final Point [] smoke_pos = {
			new Point(  5,  5 ),
			new Point(  6,  0 ),
			new Point(  5, -5 ),
			new Point(  0, -6 ),
			new Point( -5, -5 ),
			new Point( -6,  0 ),
			new Point( -5,  5 ),
			new Point(  0,  6 )
	};

	static void HandleAircraftSmoke(Vehicle v)
	{

		if (!v.isAircraftBroken()) return;

		if (v.cur_speed < 10) {
			v.setAircraftBroken(false);
			v.breakdown_ctr = 0;
			return;
		}

		if ((v.tick_counter & 0x1F) == 0) {
			v.CreateEffectVehicleRel(
					smoke_pos[v.direction].x,
					smoke_pos[v.direction].y,
					2,
					Vehicle.EV_SMOKE
					);
		}
	}

	static void ProcessAircraftOrder(Vehicle v)
	{
		final Order order;

		// Order.OT_GOTO_DEPOT, Order.OT_LOADING
		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT ||
				v.getCurrent_order().type == Order.OT_LOADING) {
			if (v.getCurrent_order().type != Order.OT_GOTO_DEPOT ||
					0==(v.getCurrent_order().flags & Order.OF_UNLOAD))
				return;
		}

		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT &&
				(v.getCurrent_order().flags & (Order.OF_PART_OF_ORDERS | Order.OF_SERVICE_IF_NEEDED)) == (Order.OF_PART_OF_ORDERS | Order.OF_SERVICE_IF_NEEDED) &&
				!v.VehicleNeedsService()) {
			v.cur_order_index++;
		}

		if (v.cur_order_index >= v.num_orders) v.cur_order_index = 0;

		order = v.GetVehicleOrder(v.cur_order_index);

		if (order == null) {
			v.getCurrent_order().type = Order.OT_NOTHING;
			v.getCurrent_order().flags = 0;
			return;
		}

		if (order.type == Order.OT_DUMMY && !v.CheckForValidOrders())
			CrashAirplane(v);

		if (order.type    == v.getCurrent_order().type   &&
				order.flags   == v.getCurrent_order().flags  &&
				order.station == v.getCurrent_order().station)
			return;

		v.setCurrent_order(new Order( order ));

		// orders are changed in flight, ensure going to the right station
		if (order.type == Order.OT_GOTO_STATION && v.air.state == Airport.FLYING) {
			AircraftNextAirportPos_and_Order(v);
			v.air.targetairport = order.station;
		}

		v.InvalidateVehicleOrder();

		Window.InvalidateWindowClasses(Window.WC_AIRCRAFT_LIST);
	}

	static void HandleAircraftLoading(Vehicle v, int mode)
	{
		if (v.getCurrent_order().type == Order.OT_NOTHING) return;

		if (v.getCurrent_order().type != Order.OT_DUMMY) {
			if (v.getCurrent_order().type != Order.OT_LOADING) return;
			if (mode != 0) return;
			if (--v.load_unload_time_rem != 0) return;

			if ( (v.getCurrent_order().flags & Order.OF_FULL_LOAD) != 0 && v.CanFillVehicle()) {
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_AIRCRAFT_INC);
				Economy.LoadUnloadVehicle(v);
				return;
			}

			{
				Order b = new Order( v.getCurrent_order() );
				v.getCurrent_order().type = Order.OT_NOTHING;
				v.getCurrent_order().flags = 0;
				if (0==(b.flags & Order.OF_NON_STOP)) return;
			}
		}
		v.cur_order_index++;
		v.InvalidateVehicleOrder();
	}

	static void CrashAirplane(Vehicle v)
	{
		int amt;
		Station st;
		//StringID 
		int newsitem;

		v.setCrashed(true);
		v.air.crashed_counter = 0;

		v.CreateEffectVehicleRel(4, 4, 8, Vehicle.EV_EXPLOSION_LARGE);

		Window.InvalidateWindow(Window.WC_VEHICLE_VIEW, v.index);

		amt = 2;
		if (v.getCargo_type() == AcceptedCargo.CT_PASSENGERS) amt += v.cargo_count;
		Global.SetDParam(0, amt);

		v.cargo_count = 0;
		v.next.cargo_count = 0;
		st = Station.GetStation(v.air.targetairport);

		if (st.airport_tile == null) {
			newsitem = Str.STR_PLANE_CRASH_OUT_OF_FUEL;
		} else {
			Global.SetDParam(1, st.index);
			newsitem = Str.STR_A034_PLANE_CRASH_DIE_IN_FIREBALL;
		}

		Global.SetDParam(1, st.index);
		NewsItem.AddNewsItem(newsitem,
				NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ACCIDENT, 0),
				v.index,
				0);

		v.SndPlayVehicleFx(Snd.SND_12_EXPLOSION);
	}

	static void MaybeCrashAirplane(Vehicle v)
	{
		Station st;
		int prob;
		int i;

		st = Station.GetStation(v.air.targetairport);

		//FIXME -- MaybeCrashAirplane . increase crashing chances of very modern airplanes on smaller than AT_METROPOLITAN airports
		prob = 0x10000 / 1500;
		if (st.airport_type == Airport.AT_SMALL 
				&& (0 !=(Engine.AircraftVehInfo(v.getEngine_type().id).subtype & 2)) 
				&& !Global._cheats.no_jetcrash.value) {
			prob = 0x10000 / 20;
		}

		if (BitOps.GB(Hal.Random(), 0, 16) > prob) return;

		// Crash the airplane. Remove all goods stored at the station.
		for (i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
			st.goods[i].rating = 1;
			st.goods[i].waiting_acceptance = BitOps.RETSB(st.goods[i].waiting_acceptance, 0, 12, 0);
		}

		CrashAirplane(v);
	}

	// we've landed and just arrived at a terminal
	static void AircraftEntersTerminal(Vehicle v)
	{
		Station st;
		Order old_order;

		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT) return;

		st = Station.GetStation(v.air.targetairport);
		v.last_station_visited = v.air.targetairport;

		/* Check if station was ever visited before */
		if (0==(st.had_vehicle_of_type & Station.HVOT_AIRCRAFT)) {
			int flags;

			st.had_vehicle_of_type |= Station.HVOT_AIRCRAFT;
			Global.SetDParam(0, st.index);
			// show newsitem of celebrating citizens
			flags = (v.owner.equals(Global.gs._local_player)) ? NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ARRIVAL_PLAYER, 0) : NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ARRIVAL_OTHER, 0);
			NewsItem.AddNewsItem(
					Str.STR_A033_CITIZENS_CELEBRATE_FIRST,
					flags,
					v.index,
					0);
		}

		old_order = new Order( v.getCurrent_order() );
		v.getCurrent_order().type = Order.OT_LOADING;
		v.getCurrent_order().flags = 0;

		if (old_order.type == Order.OT_GOTO_STATION &&
				v.getCurrent_order().station == v.last_station_visited) {
			v.getCurrent_order().flags =
					(old_order.flags & (Order.OF_FULL_LOAD | Order.OF_UNLOAD)) | Order.OF_NON_STOP;
		}

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_AIRCRAFT_INC);
		Economy.LoadUnloadVehicle(v);
		Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
		Window.InvalidateWindowClasses(Window.WC_AIRCRAFT_LIST);
	}

	static boolean ValidateAircraftInHangar(int data_a, int data_b)
	{
		final Vehicle  v = Vehicle.GetVehicle(data_a);

		return (IsAircraftHangarTile(v.tile) && v.isStopped());
	}

	static void AircraftEnterHangar(Vehicle v)
	{
		Order old_order;

		ServiceAircraft(v);
		Window.InvalidateWindowClasses(Window.WC_AIRCRAFT_LIST);

		v.TriggerVehicle(Engine.VEHICLE_TRIGGER_DEPOT);

		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT) {
			Window.InvalidateWindow(Window.WC_VEHICLE_VIEW, v.index);

			old_order = new Order( v.getCurrent_order() );
			v.getCurrent_order().type = Order.OT_NOTHING;
			v.getCurrent_order().flags = 0;

			if (BitOps.HASBIT(old_order.flags, Order.OFB_PART_OF_ORDERS)) {
				v.cur_order_index++;
			} else if (BitOps.HASBIT(old_order.flags, Order.OFB_HALT_IN_DEPOT)) { // force depot visit
				v.setStopped(true);
				Window.InvalidateWindowClasses(Window.WC_AIRCRAFT_LIST);

				if (v.owner.isLocalPlayer()) {
					Global.SetDParam(0, v.unitnumber.id);
					NewsItem.AddValidatedNewsItem(
							Str.STR_A014_AIRCRAFT_IS_WAITING_IN,
							NewsItem.NEWS_FLAGS(NewsItem.NM_SMALL, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ADVICE, 0),
							v.index,
							0,
							AirCraft::ValidateAircraftInHangar);
				}
			}
		}
	}

	static void AircraftLand(Vehicle v)
	{
		v.sprite_width = v.sprite_height = 2;
	}

	static void AircraftLandAirplane(Vehicle v)
	{
		AircraftLand(v);
		v.SndPlayVehicleFx(Snd.SND_17_SKID_PLANE);
		MaybeCrashAirplane(v);
	}

	// set the right pos when heading to other airports after takeoff
	static void AircraftNextAirportPos_and_Order(Vehicle v)
	{
		final Station  st;
		final Airport airport;

		if (v.getCurrent_order().type == Order.OT_GOTO_STATION ||
				v.getCurrent_order().type == Order.OT_GOTO_DEPOT)
			v.air.targetairport = v.getCurrent_order().station;

		st = Station.GetStation(v.air.targetairport);
		airport = Airport.GetAirport(st.airport_type);
		v.air.pos = v.air.previous_pos = airport.getEntryPoint();
	}

	static void AircraftLeaveHangar(Vehicle v)
	{
		v.cur_speed = 0;
		v.subspeed = 0;
		v.progress = 0;
		v.direction = 3;
		v.setHidden(false);
		{
			Vehicle u = v.next;
			u.setHidden(false);

			// Rotor blades
			u = u.next;
			if (u != null) {
				u.setHidden(false);
				u.cur_speed = 80;
			}
		}

		v.VehicleServiceInDepot();
		SetAircraftPosition(v, v.getX_pos(), v.getY_pos(), v.z_pos);
		Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
		Window.InvalidateWindowClasses(Window.WC_AIRCRAFT_LIST);
	}


	////////////////////////////////////////////////////////////////////////////////
	///////////////////   AIRCRAFT MOVEMENT SCHEME  ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	static void AircraftEventHandler_EnterTerminal(Vehicle v, final Airport airport)
	{
		AircraftEntersTerminal(v);
		v.air.state = airport.getLayoutItem(v.air.pos).heading;
	}

	static void AircraftEventHandler_EnterHangar(Vehicle v, final Airport airport)
	{
		AircraftEnterHangar(v);
		v.air.state = airport.getLayoutItem(v.air.pos).heading;
	}

	// In an Airport Hangar
	static void AircraftEventHandler_InHangar(Vehicle v, final Airport airport)
	{
		// if we just arrived, execute EnterHangar first
		if (v.air.previous_pos != v.air.pos) {
			AircraftEventHandler_EnterHangar(v, airport);
			return;
		}

		// if we were sent to the depot, stay there
		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT && v.isStopped()) {
			v.getCurrent_order().type = Order.OT_NOTHING;
			v.getCurrent_order().flags = 0;
			return;
		}

		if (v.getCurrent_order().type != Order.OT_GOTO_STATION &&
				v.getCurrent_order().type != Order.OT_GOTO_DEPOT)
			return;

		// if the block of the next position is busy, stay put
		if (AirportHasBlock(v, airport.getLayoutItem(v.air.pos), airport)) return;

		// We are already at the target airport, we need to find a terminal
		if (v.getCurrent_order().station == v.air.targetairport) {
			// FindFreeTerminal:
			// 1. Find a free terminal, 2. Occupy it, 3. Set the vehicle's state to that terminal
			if (v.subtype != 0) {
				if (!AirportFindFreeTerminal(v, airport)) return; // airplane
			} else {
				if (!AirportFindFreeHelipad(v, airport)) return; // helicopter
			}
		} else { // Else prepare for launch.
			// airplane goto state takeoff, helicopter to helitakeoff
			v.air.state = (v.subtype != 0) ? Airport.TAKEOFF : Airport.HELITAKEOFF;
		}
		AircraftLeaveHangar(v);
		AirportMove(v, airport);
	}

	// At one of the Airport's Terminals
	static void AircraftEventHandler_AtTerminal(Vehicle v, final Airport airport)
	{
		// if we just arrived, execute EnterTerminal first
		if (v.air.previous_pos != v.air.pos) {
			AircraftEventHandler_EnterTerminal(v, airport);
			// on an airport with helipads, a helicopter will always land there
			// and get serviced at the same time - patch setting
			if (Global._patches.serviceathelipad.get()) {
				if (v.subtype == 0 && airport.helipads != null) {
					// an exerpt of ServiceAircraft, without the invisibility stuff
					v.date_of_last_service = Global.get_date();
					v.breakdowns_since_last_service = 0;
					v.reliability = Engine.GetEngine(v.getEngine_type()).getReliability();
					Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
				}
			}
			return;
		}

		if (v.getCurrent_order().type == Order.OT_NOTHING) return;

		// if the block of the next position is busy, stay put
		if (AirportHasBlock(v, airport.getLayoutItem(v.air.pos), airport)) {
			return;
		}

		// airport-road is free. We either have to go to another airport, or to the hangar
		// --. start moving

		switch (v.getCurrent_order().type) {
		case Order.OT_GOTO_STATION: // ready to fly to another airport
			// airplane goto state takeoff, helicopter to helitakeoff
			v.air.state = (v.subtype != 0) ? Airport.TAKEOFF : Airport.HELITAKEOFF;
			break;
		case Order.OT_GOTO_DEPOT:   // visit hangar for serivicing, sale, etc.
			if (v.getCurrent_order().station == v.air.targetairport) {
				v.air.state = Airport.HANGAR;
			} else {
				v.air.state = (v.subtype != 0) ? Airport.TAKEOFF : Airport.HELITAKEOFF;
			}
			break;
		default:  // orders have been deleted (no orders), goto depot and don't bother us
			v.getCurrent_order().type = Order.OT_NOTHING;
			v.getCurrent_order().flags = 0;
			v.air.state = Airport.HANGAR;
		}

		AirportMove(v, airport);
	}

	static void AircraftEventHandler_General(Vehicle v, final Airport Airport)
	{
		Global.DEBUG_misc( 0, "OK, you shouldn't be here, check your Airport Scheme!");
		assert false;
	}

	static void AircraftEventHandler_TakeOff(Vehicle v, final Airport airport) {
		PlayAircraftSound(v); // play takeoffsound for airplanes
		v.air.state = Airport.STARTTAKEOFF;
	}

	static void AircraftEventHandler_StartTakeOff(Vehicle v, final Airport airport)
	{
		v.sprite_width = v.sprite_height = 24; // ??? no idea what this is
		v.air.state = Airport.ENDTAKEOFF;
	}

	static void AircraftEventHandler_EndTakeOff(Vehicle v, final Airport airport)
	{
		v.air.state = Airport.FLYING;
		// get the next position to go to, differs per airport
		AircraftNextAirportPos_and_Order(v);
	}

	static void AircraftEventHandler_HeliTakeOff(Vehicle v, final Airport airport)
	{
		final Player  p = Player.GetPlayer(v.owner);
		v.sprite_width = v.sprite_height = 24; // ??? no idea what this is
		v.air.state = Airport.FLYING;
		// get the next position to go to, differs per airport
		AircraftNextAirportPos_and_Order(v);

		// check if the aircraft needs to be replaced or renewed and send it to a hangar if needed
		if (v.owner.isLocalPlayer() && (
				p.EngineHasReplacement(v.getEngine_type()) ||
				(p.isEngine_renew() && v.age - v.max_age > p.getEngine_renew_months() * 30)
				)) {
			PlayerID.setCurrent( Global.gs._local_player );
			Cmd.DoCommandP(v.tile, v.index, 1, null, Cmd.CMD_SEND_AIRCRAFT_TO_HANGAR | Cmd.CMD_SHOW_NO_ERROR);
			PlayerID.setCurrentToNone();
		}
	}

	static void AircraftEventHandler_Flying(Vehicle v, final Airport airport)
	{
		Station st;
		int landingtype;
		AirportFTA current;
		int tcur_speed, tsubspeed;
		boolean can_land;
		can_land = false;

		// Get the target airport
		st = Station.GetStation(v.air.targetairport);
		// flying device is accepted at this station
		// small airport -. no helicopters (AIRCRAFT_ONLY)
		// all other airports -. all types of flying devices (ALL)
		// heliport/oilrig, etc -. no airplanes (HELICOPTERS_ONLY)
		// runway busy or not allowed to use this airstation, circle
		if (! (v.subtype == airport.getAccPlanes() ||
				st.airport_tile == null || 
				(st.owner.isNotNone() && !st.owner.equals(v.owner) && !mAirport.MA_OwnerHandler(st.owner)) )) {

			// {32,FLYING,NOTHING_block,37}, {32,LANDING,N,33}, {32,HELILANDING,N,41},
			// if it is an airplane, look for LANDING, for helicopter HELILANDING
			// it is possible to choose from multiple landing runways, so loop until a free one is found
			landingtype = (v.subtype != 0) ? AirConstants.LANDING : AirConstants.HELILANDING;
			current = airport.getLayoutItem(v.air.pos).next_in_chain;
			while (current != null) {
				if (current.heading == landingtype) {


					// Check to see if we're going to land at an airport.

					// Fisrt, check queue - if we are on top, or if it's empty,
					// we can land.

					// Just in case the code in AircraftController code misses,
					// We check before the aircraft lands.

					// If it's already in the queue, don't re-add it
					// Otherwise, add it to queue - but do helicopters seperately!
					// Otherwise, helicopters will be part of the queue and can't land separately!
					if(v.queue_item == null && (Global._patches.aircraft_queueing && v.subtype != 0)) {
						// Add to queue
						assert(st.airport_queue.push(v));
					}

					if(v.queue_item == null && (Global._patches.aircraft_queueing && v.subtype == 0)) {
						// Add to queue
						assert(st.helicopter_queue.push(v));
					}
					// save speed before, since if AirportHasBlock is false, it resets them to 0
					// we don't want that for plane in air
					// hack for speed thingie
					tcur_speed = v.cur_speed;
					tsubspeed = v.subspeed;

					// If we're on top, go in
					if(st.airport_queue.getTop() == v && (Global._patches.aircraft_queueing && v.subtype != 0)) {
						if (!AirportHasBlock(v, current, airport)) {			
							can_land = true;
							st.airport_queue.pop();
						} else {
							can_land = false;
						}
					}

					// Helicopters have their own queue
					if(v.subtype == 0 && st.helicopter_queue.getTop() == v && Global._patches.aircraft_queueing) {
						if (!AirportHasBlock(v, current, airport)) {
							can_land = true;
							st.helicopter_queue.pop();
						} else {
							can_land = false;
						}
					} else {
						if(v.subtype == 0) {
							can_land = false;
							if(st.helicopter_queue.getPos(v) != 1)
							{
								v.air.desired_speed = 0;
							}
						}
					}

					if(!Global._patches.aircraft_queueing) { // || v.subtype == 0
						/*if (!AirportHasBlock(v, current, Airport)) {
							can_land = true;
						} else {
							can_land = false;
						}*/
						can_land = !AirportHasBlock(v, current, airport);
					}


					if(can_land) {
						v.air.state = landingtype; // LANDING / HELILANDING
						// it's a bit dirty, but I need to set position to next position, otherwise
						// if there are multiple runways, plane won't know which one it took (because
						// they all have heading LANDING). And also occupy that block!
						v.air.pos = current.next_position;
						//st.airport_flags = BitOps.RETSETBITS(st.airport_flags, airport.getLayoutItem(v.air.pos).block);
						st.setAirportBlocks(airport.getLayoutItem(v.air.pos).block);
						return;
					}
					v.cur_speed = tcur_speed;
					v.subspeed = tsubspeed;
				}
				current = current.next_in_chain;
			}
		}
		v.air.state = AirConstants.FLYING;
		v.air.pos = airport.getLayoutItem(v.air.pos).next_position;
	}

	static void AircraftEventHandler_Landing(Vehicle v, final Airport airport)
	{
		final Player  p = Player.GetPlayer(v.owner);
		AircraftLandAirplane(v);  // maybe crash airplane
		v.air.state = Airport.ENDLANDING;
		// check if the aircraft needs to be replaced or renewed and send it to a hangar if needed
		if (v.getCurrent_order().type != Order.OT_GOTO_DEPOT && v.owner.isLocalPlayer()) {
			// only the vehicle owner needs to calculate the rest (locally)
			if (p.EngineHasReplacement(v.getEngine_type()) ||
					(p.isEngine_renew() && v.age - v.max_age > (p.getEngine_renew_months() * 30))) {
				// send the aircraft to the hangar at next airport (bit 17 set)
				PlayerID.setCurrent( Global.gs._local_player );
				Cmd.DoCommandP(v.tile, v.index, 1 << 16, null, Cmd.CMD_SEND_AIRCRAFT_TO_HANGAR | Cmd.CMD_SHOW_NO_ERROR);
				PlayerID.setCurrentToNone();
			}
		}
	}

	static void AircraftEventHandler_HeliLanding(Vehicle v, final Airport airport)
	{
		AircraftLand(v); // helicopters don't crash
		v.air.state = Airport.HELIENDLANDING;
	}

	static void AircraftEventHandler_EndLanding(Vehicle v, final Airport airport)
	{
		// next block busy, don't do a thing, just wait
		if (AirportHasBlock(v, airport.getLayoutItem(v.air.pos), airport)) return;

		// if going to terminal (Order.OT_GOTO_STATION) choose one
		// 1. in case all terminals are busy AirportFindFreeTerminal() returns false or
		// 2. not going for terminal (but depot, no order),
		// -. get out of the way to the hangar.
		if (v.getCurrent_order().type == Order.OT_GOTO_STATION) {
			if (AirportFindFreeTerminal(v, airport)) return;
		}
		v.air.state = Airport.HANGAR;

	}

	static void AircraftEventHandler_HeliEndLanding(Vehicle v, final Airport airport)
	{
		// next block busy, don't do a thing, just wait
		if (AirportHasBlock(v, airport.getLayoutItem(v.air.pos), airport)) return;

		// if going to helipad (Order.OT_GOTO_STATION) choose one. If airport doesn't have helipads, choose terminal
		// 1. in case all terminals/helipads are busy (AirportFindFreeHelipad() returns false) or
		// 2. not going for terminal (but depot, no order),
		// -. get out of the way to the hangar IF there are terminals on the airport.
		// -. else TAKEOFF
		// the reason behind this is that if an airport has a terminal, it also has a hangar. Airplanes
		// must go to a hangar.
		if (v.getCurrent_order().type == Order.OT_GOTO_STATION) {
			if (AirportFindFreeHelipad(v, airport)) return;
		}
		v.air.state = (airport.terminals != null) ? AirConstants.HANGAR : AirConstants.HELITAKEOFF;
	}

	static final AircraftStateHandler [] _aircraft_state_handlers = {
			AirCraft::AircraftEventHandler_General,			// TO_ALL         =  0
			AirCraft::AircraftEventHandler_InHangar,		// HANGAR         =  1
			AirCraft::AircraftEventHandler_AtTerminal,		// TERM1          =  2
			AirCraft::AircraftEventHandler_AtTerminal,		// TERM2          =  3
			AirCraft::AircraftEventHandler_AtTerminal,		// TERM3          =  4
			AirCraft::AircraftEventHandler_AtTerminal,		// TERM4          =  5
			AirCraft::AircraftEventHandler_AtTerminal,		// TERM5          =  6
			AirCraft::AircraftEventHandler_AtTerminal,		// TERM6          =  7
			AirCraft::AircraftEventHandler_AtTerminal,		// HELIPAD1       =  8
			AirCraft::AircraftEventHandler_AtTerminal,		// HELIPAD2       =  9
			AirCraft::AircraftEventHandler_TakeOff,			// TAKEOFF        = 10
			AirCraft::AircraftEventHandler_StartTakeOff,	// STARTTAKEOFF   = 11
			AirCraft::AircraftEventHandler_EndTakeOff,		// ENDTAKEOFF     = 12
			AirCraft::AircraftEventHandler_HeliTakeOff,		// HELITAKEOFF    = 13
			AirCraft::AircraftEventHandler_Flying,			// FLYING         = 14
			AirCraft::AircraftEventHandler_Landing,			// LANDING        = 15
			AirCraft::AircraftEventHandler_EndLanding,		// ENDLANDING     = 16
			AirCraft::AircraftEventHandler_HeliLanding,		// HELILANDING    = 17
			AirCraft::AircraftEventHandler_HeliEndLanding,	// HELIENDLANDING = 18
	};

	static void AirportClearBlock(final Vehicle  v, final Airport airport)
	{
		// we have left the previous block, and entered the new one. Free the previous block
		if (airport.getLayoutItem(v.air.previous_pos).block != airport.getLayoutItem(v.air.pos).block) {
			Station  st = Station.GetStation(v.air.targetairport);

			//st.airport_flags = BitOps.RETCLRBITS(st.airport_flags, airport.getLayoutItem(v.air.previous_pos).block);
			st.resetAirportBlocks(airport.getLayoutItem(v.air.previous_pos).block);
		}
	}

	static void AirportGoToNextPosition(Vehicle v, final Airport airport)
	{
		// if aircraft is not in position, wait until it is
		if (!AircraftController(v)) return;

		AirportClearBlock(v, airport);
		AirportMove(v, airport); // move aircraft to next position
	}

	// gets pos from vehicle and next orders
	static boolean AirportMove(Vehicle v, final Airport airport)
	{
		AirportFTA current;
		int prev_pos;
		boolean retval = false;


		// error handling
		if (v.air.pos >= airport.getNofElements()) {
			Global.DEBUG_misc( 0, "position %d is not valid for current airport. Max position is %d", v.air.pos, airport.getNofElements()-1);
			assert(v.air.pos < airport.getNofElements());
		}

		current = airport.getLayoutItem(v.air.pos);
		// we have arrived in an important state (eg terminal, hangar, etc.)
		if (current.heading == v.air.state) {
			prev_pos =  v.air.pos; // location could be changed in state, so save it before-hand
			_aircraft_state_handlers[v.air.state].accept(v, airport);
			if (v.air.state != AirConstants.FLYING) v.air.previous_pos = prev_pos;
			return true;
		}

		v.air.previous_pos = v.air.pos; // save previous location

		// there is only one choice to move to
		if (current.next_in_chain == null) {
			if (AirportSetBlocks(v, current, airport)) {
				v.air.pos = current.next_position;
			} // move to next position
			return retval;
		}

		// there are more choices to choose from, choose the one that
		// matches our heading
		do {
			if (v.air.state == current.heading || current.heading == AirConstants.TO_ALL) {
				if (AirportSetBlocks(v, current, airport)) {
					v.air.pos = current.next_position;
				} // move to next position
				return retval;
			}
			current = current.next_in_chain;
		} while (current != null);

		Global.DEBUG_misc( 0, "Cannot move further on Airport...! pos:%d state:%d", v.air.pos, v.air.state);
		Global.DEBUG_misc( 0, "Airport entry point: %d, Vehicle: %d", airport.getEntryPoint(), v.index);
		assert false;
		return false;
	}

	// returns true if the road ahead is busy, eg. you must wait before proceeding
	static boolean AirportHasBlock(Vehicle v, AirportFTA current_pos, final Airport airport)
	{
		final AirportFTA reference = airport.getLayoutItem(v.air.pos);
		final AirportFTA next = airport.getLayoutItem(current_pos.next_position);

		// same block, then of course we can move
		if (airport.getLayoutItem(current_pos.position).block != next.block) {
			final Station  st = Station.GetStation(v.air.targetairport);
			int airport_flags = next.block;

			// check additional possible extra blocks
			if (current_pos != reference && current_pos.block != AirConstants.NOTHING_block) {
				airport_flags |= current_pos.block;
			}

			//if (BitOps.HASBITS(st.airport_flags, airport_flags)) 
			if(st.hasAirportBlocks(airport_flags)) 
			{
				v.cur_speed = 0;
				v.subspeed = 0;
				return true;
			}
		}
		return false;
	}

	// returns true on success. Eg, next block was free and we have occupied it
	static boolean AirportSetBlocks(Vehicle v, AirportFTA current_pos, final Airport airport)
	{
		AirportFTA next = airport.getLayoutItem(current_pos.next_position);
		AirportFTA reference = airport.getLayoutItem(v.air.pos);
		AirportFTA current;

		// if the next position is in another block, check it and wait until it is free
		if (airport.getLayoutItem(current_pos.position).block != next.block) {
			int airport_flags = next.block;
			Station  st = Station.GetStation(v.air.targetairport);

			//search for all all elements in the list with the same state, and blocks != N
			// this means more blocks should be checked/set
			current = current_pos;
			if (current == reference) current = current.next_in_chain;
			while (current != null) {
				if (current.heading == current_pos.heading && current.block != 0) {
					airport_flags |= current.block;
					break;
				}
				current = current.next_in_chain;
			}

			// if the block to be checked is in the next position, then exclude that from
			// checking, because it has been set by the airplane before
			if (current_pos.block == next.block) airport_flags ^= next.block;

			//if (BitOps.HASBITS(st.airport_flags, airport_flags)) 
			if(st.hasAirportBlocks(airport_flags)) 
			{
				v.cur_speed = 0;
				v.subspeed = 0;
				return false;
			}

			if (next.block != AirConstants.NOTHING_block) {
				//st.airport_flags = BitOps.RETSETBITS(st.airport_flags, airport_flags); // occupy next block
				st.setAirportBlocks(airport_flags);
			}
		}
		return true;
	}

	static boolean FreeTerminal(Vehicle v, int i, int last_terminal)
	{
		Station st = Station.GetStation(v.air.targetairport);
		for (; i < last_terminal; i++) {
			//if (!BitOps.HASBIT(st.airport_flags, i)) 
			if (!st.hasAirportBlock(i)) 
			{
				// TERMINAL# HELIPAD#
				v.air.state = i + Airport.TERM1; // start moving to that terminal/helipad
				//st.airport_flags = BitOps.RETSETBIT(st.airport_flags, i); // occupy terminal/helipad
				st.setAirportBlock(i); // occupy terminal/helipad
				return true;
			}
		}
		return false;
	}


	static boolean AirportFindFreeTerminal(Vehicle v, final Airport airport)
	{
		AirportFTA temp;
		Station st;

		/* example of more terminalgroups
			{0,HANGAR,NOTHING_block,1}, {0,255,TERM_GROUP1_block,0}, {0,255,TERM_GROUP2_ENTER_block,1}, {0,0,N,1},
			Heading 255 denotes a group. We see 2 groups here:
			1. group 0 -- TERM_GROUP1_block (check block)
			2. group 1 -- TERM_GROUP2_ENTER_block (check block)
			First in line is checked first, group 0. If the block (TERM_GROUP1_block) is free, it
			looks	at the corresponding terminals of that group. If no free ones are found, other
			possible groups are checked	(in this case group 1, since that is after group 0). If that
			fails, then attempt fails and plane waits
		 */
		if (airport.terminals[0] > 1) {
			st = Station.GetStation(v.air.targetairport);
			temp = airport.getLayoutItem(v.air.pos).next_in_chain;
			while (temp != null) {
				if (temp.heading == 255) {
					//if (!BitOps.HASBITS(st.airport_flags, temp.block)) 
					if (!st.hasAirportBlocks(temp.block)) 
					{
						int target_group;
						int i;
						int group_start = 0;
						int group_end;

						//read which group do we want to go to?
						//(the first free group)
						target_group = temp.next_position + 1;

						//at what terminal does the group start?
						//that means, sum up all terminals of
						//groups with lower number
						for (i = 1; i < target_group; i++)
							group_start += airport.terminals[i];

						group_end = group_start + airport.terminals[target_group];
						if (FreeTerminal(v, group_start, group_end)) return true;
					}
				} else {
					/* once the heading isn't 255, we've exhausted the possible blocks.
					 * So we cannot move */
					return false;
				}
				temp = temp.next_in_chain;
			}
		}

		// if there is only 1 terminalgroup, all terminals are checked (starting from 0 to max)
		return FreeTerminal(v, 0, airport.GetNumTerminals());
	}



	static boolean AirportFindFreeHelipad(Vehicle v, final Airport airport)
	{
		Station st;
		AirportFTA temp;

		// if an airport doesn't have helipads, use terminals
		if (airport.helipads == null) return AirportFindFreeTerminal(v, airport);

		// if there are more helicoptergroups, pick one, just as in AirportFindFreeTerminal()
		if (airport.helipads[0] > 1) {
			st = Station.GetStation(v.air.targetairport);
			temp = airport.getLayoutItem(v.air.pos).next_in_chain;
			while (temp != null) {
				if (temp.heading == 255) {
					//if (!BitOps.HASBITS(st.airport_flags, temp.block)) 
					if (!st.hasAirportBlocks(temp.block)) 
					{
						int target_group;
						int i;
						int group_start = 0;
						int group_end;

						//read which group do we want to go to?
						//(the first free group)
						target_group = temp.next_position + 1;

						//at what terminal does the group start?
						//that means, sum up all terminals of
						//groups with lower number
						for(i = 1; i < target_group; i++)
							group_start += airport.helipads[i];

						group_end = group_start + airport.helipads[target_group];
						if (FreeTerminal(v, group_start, group_end)) return true;
					}
				} else {
					/* once the heading isn't 255, we've exhausted the possible blocks.
					 * So we cannot move */
					return false;
				}
				temp = temp.next_in_chain;
			}
		} else {
			// only 1 helicoptergroup, check all helipads
			// The blocks for helipads start after the last terminal (MAX_TERMINALS)
			return FreeTerminal(v, Airport.MAX_TERMINALS, airport.GetNumHelipads() + Airport.MAX_TERMINALS);
		}
		return false;	// it shouldn't get here anytime, but just to be sure
	}

	static void AircraftEventHandler(Vehicle v, int loop)
	{
		v.tick_counter++;

		if(v.isCrashed()) {
			HandleCrashedAircraft(v);
			return;
		}

		if(v.isStopped()) return;

		/* aircraft is broken down? */
		if (v.breakdown_ctr != 0) {
			if (v.breakdown_ctr <= 2) {
				HandleBrokenAircraft(v);
			} else {
				v.breakdown_ctr--;
			}
		}

		HandleAircraftSmoke(v);
		ProcessAircraftOrder(v);
		HandleAircraftLoading(v, loop);

		if (v.getCurrent_order().type >= Order.OT_LOADING) return;

		// pass the right airport structure to the functions
		// DEREF_STATION gets target airport (Station st), its type is passed to GetAirport
		// that returns the correct layout depending on type
		AirportGoToNextPosition(v, Airport.GetAirport(Station.GetStation(v.air.targetairport).airport_type));
	}

	static void Aircraft_Tick(Vehicle v)
	{
		int i;

		if (v.subtype > 2) return;

		if (v.subtype == 0) HelicopterTickHandler(v);

		AgeAircraftCargo(v);

		for (i = 0; i != 6; i++) {
			AircraftEventHandler(v, i);
			if (v.type != Vehicle.VEH_Aircraft) // In case it was deleted
				break;
		}
	}

	/*static void UpdateOilRig()
	{
		Station.forEach( (st) ->
		{
			if (st.airport_type == 5) st.airport_type = Airport.AT_OILRIG;
		});
	}*/

	// need to be called to load aircraft from old version
	/*static void UpdateOldAircraft()
	{
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();

		// set airport_flags to 0 for all airports just to be sure
		Station.forEach( (st) ->
		{
			st.airport_flags = 0; // reset airport
			// type of oilrig has been moved, update it (3-5)
			if (st.airport_type == 3) st.airport_type = Airport.AT_OILRIG;
		});

		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v_oldstyle = ii.next();
			// airplane has another vehicle with subtype 4 (shadow), helicopter also has 3 (rotor)
			// skip those
			if (v_oldstyle.type == Vehicle.VEH_Aircraft && v_oldstyle.subtype <= 2) {
				// airplane in terminal stopped doesn't hurt anyone, so goto next
				if(v_oldstyle.isStopped() && v_oldstyle.air.state == 0) {
					v_oldstyle.air.state = Airport.HANGAR;
					continue;
				}

				AircraftLeaveHangar(v_oldstyle); // make airplane visible if it was in a depot for example
				v_oldstyle.setStopped(false); // make airplane moving
				v_oldstyle.air.state = Airport.FLYING;
				AircraftNextAirportPos_and_Order(v_oldstyle); // move it to the entry point of the airport
				v_oldstyle.GetNewVehiclePos(gp); // get the position of the plane (to be used for setting)
				v_oldstyle.tile = null; // aircraft in air is tile=0

				// correct speed of helicopter-rotors
				if (v_oldstyle.subtype == 0) v_oldstyle.next.next.cur_speed = 32;

				// set new position x,y,z
				SetAircraftPosition(v_oldstyle, gp.x, gp.y, GetAircraftFlyingAltitude(v_oldstyle));
			}
		}
	}*/

	static void UpdateAirplanesOnNewStation(Station st)
	{
		// only 1 station is updated per function call, so it is enough to get entry_point once
		final Airport ap = Airport.GetAirport(st.airport_type);

		Vehicle.forEach( (v) -> updateOneAirplaneOnNewStation(st, ap, v) );
	}

	private static void updateOneAirplaneOnNewStation(
			Station st, final Airport ap, Vehicle v) {
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();

		if (v.type != Vehicle.VEH_Aircraft || v.subtype > 2)
			return;

		if (v.air.targetairport != st.index) 	// if heading to this airport
			return;

		/*
		 * Update position of airplane. If plane is not flying, 
		 * landing, or taking off you cannot delete airport, 
		 * so it doesn't matter
		 */
		if (v.air.state >= Airport.FLYING) 
		{	// circle around
			v.air.pos = v.air.previous_pos = ap.getEntryPoint();
			v.air.state = Airport.FLYING;
			// landing plane needs to be reset to flying height (only if in pause mode upgrade,
			// in normal mode, plane is reset in AircraftController. It doesn't hurt for FLYING
			v.GetNewVehiclePos( gp);
			// set new position x,y,z
			SetAircraftPosition(v, gp.x, gp.y, GetAircraftFlyingAltitude(v));
		} else {
			assert(v.air.state == Airport.ENDTAKEOFF || v.air.state == Airport.HELITAKEOFF);
			int takeofftype = (v.subtype == 0) ? Airport.HELITAKEOFF : Airport.ENDTAKEOFF;
			// search in airportdata for that heading
			// easiest to do, since this doesn't happen a lot
			for (int cnt = 0; cnt < ap.getNofElements(); cnt++) 
			{
				if (ap.getLayoutItem(cnt).heading == takeofftype) {
					v.air.pos = ap.getLayoutItem(cnt).position;
					break;
				}
			}
		}


	}




























	/* $Id: aircraft_gui.c 3346 2005-12-27 16:37:50Z peter1138 $ */





















	/**
	 * Draw the purchase info details of an aircraft at a given location.
	 * @param x,y location where to draw the info
	 * @param engine_number the engine of which to draw the info of
	 */
	public static void DrawAircraftPurchaseInfo(int x, int y, EngineID engine_number)
	{
		final AircraftVehicleInfo avi = Engine.AircraftVehInfo(engine_number.id);
		final Engine  e = Engine.GetEngine(engine_number);
		YearMonthDay ymd = new YearMonthDay(e.getIntro_date());
		//YearMonthDay.ConvertDayToYMD(ymd, e.getIntro_date());

		/* Purchase cost - Max speed */
		Global.SetDParam(0, avi.base_cost * (((int)Global._price.aircraft_base)>>3)>>5);
		Global.SetDParam(1, avi.max_speed * 8);
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_COST_SPEED, 0);
		y += 10;

		/* Cargo capacity */
		Global.SetDParam(0, avi.passenger_capacity);
		Global.SetDParam(1, avi.mail_capacity);
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_AIRCRAFT_CAPACITY, 0);
		y += 10;

		/* Running cost */
		Global.SetDParam(0, avi.running_cost * ((int)Global._price.aircraft_running) >> 8);
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_RUNNINGCOST, 0);
		y += 10;

		/* Design date - Life length */
		Global.SetDParam(0, ymd.year + 1920);
		Global.SetDParam(1, e.getLifelength());
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_DESIGNED_LIFE, 0);
		y += 10;

		/* Reliability */
		Global.SetDParam(0, e.getReliability() * 100 >> 16);
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_RELIABILITY, 0);
		y += 10;
	}


	static void DrawAircraftImage(final Vehicle v, int x, int y, VehicleID selection)
	{
		DrawAircraftImage(v, x, y,  selection.id );
	}

	static void DrawAircraftImage(final Vehicle v, int x, int y, int selection)
	{
		int image = GetAircraftImage(v, 6);
		int ormod = Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(v.owner));
		if(v.isCrashed()) ormod = Sprite.PALETTE_CRASH;
		Gfx.DrawSprite(image | ormod, x + 25, y + 10);
		if (v.subtype == 0) Gfx.DrawSprite(Sprite.SPR_ROTOR_STOPPED, x + 25, y + 5);
		if (v.index == selection) {
			Gfx.DrawFrameRect(x - 1, y - 1, x + 58, y + 21, 0xF, Window.FR_BORDERONLY);
		}
	}

	public static void CcBuildAircraft(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			final Vehicle  v = Vehicle.GetVehicle(Global._new_aircraft_id);

			if (v.tile.equals( Global._backup_orders_tile )) {
				Global._backup_orders_tile = null;
				Vehicle.RestoreVehicleOrders(v, Global._backup_orders_data[0]);
			}
			ShowAircraftViewWindow(v);
		}
	}

	public static void CcCloneAircraft(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) ShowAircraftViewWindow(Vehicle.GetVehicle(Global._new_aircraft_id));
	}

	static void NewAircraftWndProc(Window w, WindowEvent we)
	{
		switch (we.event) {
		case WE_PAINT: {
			if (w.window_number == 0) 
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 5);

			{
				int count = 0;
				int num = Global.NUM_AIRCRAFT_ENGINES;
				//final Engine  e = GetEngine(AIRCRAFT_ENGINES_INDEX);
				int ei = Global.AIRCRAFT_ENGINES_INDEX;
				do {
					final Engine e = Engine.GetEngine(ei++);
					if (e.isAvailableToMe()) count++;
				} while (--num > 0);
				w.SetVScrollCount( count);
			}

			w.DrawWindowWidgets();

			{
				int num = Global.NUM_AIRCRAFT_ENGINES;
				//final Engine  e = GetEngine(AIRCRAFT_ENGINES_INDEX);
				int ei = Global.AIRCRAFT_ENGINES_INDEX;
				int x = 2;
				int y = 15;
				int sel = w.as_buildtrain_d().sel_index;
				int pos = w.vscroll.getPos();
				/*EngineID*/ int engine_id = Global.AIRCRAFT_ENGINES_INDEX;
				//EngineID 
				int selected_id = Engine.INVALID_ENGINE;

				do {
					final Engine e = Engine.GetEngine(ei++);
					if (e.isAvailableToMe()) {
						if (sel==0) selected_id = engine_id;
						if (BitOps.IS_INT_INSIDE(--pos, -w.vscroll.getCap(), 0)) {
							Gfx.DrawString(x+62, y+7, Engine.GetCustomEngineName(engine_id), sel==0 ? 0xC : 0x10);
							DrawAircraftEngine(x+29, y+10, engine_id, Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)));
							y += 24;
						}
						sel--;
					}
					++engine_id;
					//++e;
				} while (--num > 0);

				w.as_buildtrain_d().sel_engine =selected_id;

				if (selected_id != Engine.INVALID_ENGINE) {
					DrawAircraftPurchaseInfo(2, w.getWidget(4).top + 1, EngineID.get(selected_id) );
				}
			}
		} break;

		case WE_CLICK:
			switch(we.widget) {
			case 2: { /* listbox */
				int i = (we.pt.y - 14) / 24;
				if (i < w.vscroll.getCap()) {
					w.as_buildtrain_d().sel_index =  (i + w.vscroll.getPos());
					w.SetWindowDirty();
				}
			} break;

			case 5: { /* build */
				//EngineID 
				int sel_eng = w.as_buildtrain_d().sel_engine;
				if(sel_eng != Engine.INVALID_ENGINE) 
					Cmd.DoCommandP(TileIndex.get(w.window_number), sel_eng, 0, AirCraft::CcBuildAircraft, Cmd.CMD_BUILD_AIRCRAFT | Cmd.CMD_MSG(Str.STR_A008_CAN_T_BUILD_AIRCRAFT));
			} break;

			case 6:	{ /* rename */
				//EngineID 
				int sel_eng = w.as_buildtrain_d().sel_engine;
				if (sel_eng != Engine.INVALID_ENGINE) {
					w.as_buildtrain_d().rename_engine = sel_eng;
					MiscGui.ShowQueryString(Engine.GetCustomEngineName(sel_eng),
							new StringID(Str.STR_A039_RENAME_AIRCRAFT_TYPE), 31, 160, w.getWindowClass(), w.window_number);
				}
			} break;
			}
			break;

		case WE_4:
			if (w.window_number != 0 && null==Window.FindWindowById(Window.WC_VEHICLE_DEPOT, w.window_number)) {
				w.DeleteWindow();
			}
			break;

		case WE_ON_EDIT_TEXT: {
			if (we.str != null) {
				Global._cmd_text = we.str;
				Cmd.DoCommandP(null, w.as_buildtrain_d().rename_engine, 0, null,
						Cmd.CMD_RENAME_ENGINE | Cmd.CMD_MSG(Str.STR_A03A_CAN_T_RENAME_AIRCRAFT_TYPE));
			}
		} break;

		case WE_RESIZE:
			w.vscroll.setCap(w.vscroll.getCap() + we.diff.y / 24);
			w.getWidget(2).unkA = (w.vscroll.getCap() << 8) + 1;
			break;
		default:
			break;
		}
	}

	static final Widget _new_aircraft_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   239,     0,    13, Str.STR_A005_NEW_AIRCRAFT,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_MATRIX, Window.RESIZE_BOTTOM,    14,     0,   227,    14,   109, 0x401,										Str.STR_A025_AIRCRAFT_SELECTION_LIST),
			new Widget(  Window.WWT_SCROLLBAR, Window.RESIZE_BOTTOM,    14,   228,   239,    14,   109, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(     Window.WWT_IMGBTN,     Window.RESIZE_TB,    14,     0,   239,   110,   161, 0x0,											Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   114,   162,   173, Str.STR_A006_BUILD_AIRCRAFT,	Str.STR_A026_BUILD_THE_HIGHLIGHTED_AIRCRAFT),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   115,   227,   162,   173, Str.STR_A037_RENAME,					Str.STR_A038_RENAME_AIRCRAFT_TYPE),
			new Widget(  Window.WWT_RESIZEBOX,     Window.RESIZE_TB,    14,   228,   239,   162,   173, 0x0,											Str.STR_RESIZE_BUTTON),
			//new Widget(   WIDGETS_END),
	};

	static final WindowDesc _new_aircraft_desc = new WindowDesc(
			-1, -1, 240, 174,
			Window.WC_BUILD_VEHICLE,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
			_new_aircraft_widgets,
			AirCraft::NewAircraftWndProc
			);

	static void ShowBuildAircraftWindow(TileIndex tile)
	{
		final int wn = tile == null ? 0 : tile.tile; // TODO check use of window_number

		Window.DeleteWindowById(Window.WC_BUILD_VEHICLE, wn);

		Window w = Window.AllocateWindowDesc(_new_aircraft_desc);
		w.window_number = wn; 
		w.vscroll.setCap(4);
		w.getWidget(2).unkA = (w.vscroll.getCap() << 8) + 1;

		w.resize.step_height = 24;

		if (tile != null) {
			w.caption_color =  tile.GetTileOwner().id;
		} else {
			w.caption_color =  Global.gs._local_player.id;
		}
	}

	static void AircraftRefitWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			final Vehicle v = Vehicle.GetVehicle(w.window_number);

			Global.SetDParam(0, v.string_id);
			Global.SetDParam(1, v.unitnumber.id);
			w.DrawWindowWidgets();

			Gfx.DrawString(1, 15, Str.STR_A040_SELECT_CARGO_TYPE_TO_CARRY, 0);

			/* TODO: Support for custom GRFSpecial-specified refitting! --pasky */
			w.as_refit_d().cargo = VehicleGui.DrawVehicleRefitWindow(v, w.as_refit_d().sel).id;

			if (w.as_refit_d().cargo != AcceptedCargo.CT_INVALID) {
				int cost = Cmd.DoCommandByTile(v.tile, v.index, w.as_refit_d().cargo, Cmd.DC_QUERY_COST, Cmd.CMD_REFIT_AIRCRAFT);
				if (!Cmd.CmdFailed(cost)) {
					Global.SetDParam(2, cost);
					Global.SetDParam(0, Global._cargoc.names_long[w.as_refit_d().cargo]);
					Global.SetDParam(1, Global._aircraft_refit_capacity);
					Gfx.DrawString(1, 147, Str.STR_A041_NEW_CAPACITY_COST_OF_REFIT, 0);
				}
			}
		}	break;

		case WE_CLICK:
			switch(e.widget) {
			case 2: { /* listbox */
				int y = e.pt.y - 25;
				if (y >= 0) {
					w.as_refit_d().sel = y / 10;
					w.SetWindowDirty();
				}
			} break;
			case 4: /* refit button */
				if (w.as_refit_d().cargo != AcceptedCargo.CT_INVALID) {
					final Vehicle v = Vehicle.GetVehicle(w.window_number);
					if (Cmd.DoCommandP(v.tile, v.index, w.as_refit_d().cargo, null, Cmd.CMD_REFIT_AIRCRAFT | Cmd.CMD_MSG(Str.STR_A042_CAN_T_REFIT_AIRCRAFT)))
						w.DeleteWindow();
				}
				break;
			}
			break;
		default:
			break;
		}
	}

	static final Widget _aircraft_refit_widgets[] = {
			new Widget(    Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,				Str.STR_018B_CLOSE_WINDOW),
			new Widget(     Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   239,     0,    13, Str.STR_A03C_REFIT,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   239,    14,   145, 0x0,							Str.STR_A03E_SELECT_TYPE_OF_CARGO_FOR),
			new Widget(      Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   239,   146,   167, 0x0,							Str.STR_NULL),
			new Widget(  Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,   239,   168,   179, Str.STR_A03D_REFIT_AIRCRAFT, Str.STR_A03F_REFIT_AIRCRAFT_TO_CARRY),
			//{   WIDGETS_END},
	};

	static final WindowDesc _aircraft_refit_desc = new WindowDesc(
			-1,-1, 240, 180,
			Window.WC_VEHICLE_REFIT,Window.WC_VEHICLE_VIEW,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_aircraft_refit_widgets,
			AirCraft::AircraftRefitWndProc
			);

	static void ShowAircraftRefitWindow(final Vehicle  v)
	{
		Window w;

		Window.DeleteWindowById(Window.WC_VEHICLE_REFIT, v.index);

		//Global._alloc_wnd_parent_num = v.index;
		w = Window.AllocateWindowDesc(_aircraft_refit_desc, v.index);
		w.window_number = v.index;
		w.caption_color =  v.owner.id;
		w.as_refit_d().sel = -1;
	}

	static void AircraftDetailsWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			Vehicle v = Vehicle.GetVehicle(w.window_number);

			w.disabled_state = v.owner.isLocalPlayer() ? 0 : (1 << 2);
			if (0==Global._patches.servint_aircraft) // disable service-scroller when interval is set to disabled
				w.disabled_state |= (1 << 5) | (1 << 6);

			Global.SetDParam(0, v.string_id);
			Global.SetDParam(1, v.unitnumber.id);
			w.DrawWindowWidgets();

			/* Draw running cost */
			{
				int year = v.age / 366;

				Global.SetDParam(1, year);

				Global.SetDParam(0, (v.age + 365 < v.max_age) ? Str.STR_AGE : Str.STR_AGE_RED);
				Global.SetDParam(2, v.max_age / 366);
				Global.SetDParam(3, ((int)Global._price.aircraft_running) * Engine.AircraftVehInfo(v.getEngine_type().id).running_cost >> 8);
				Gfx.DrawString(2, 15, Str.STR_A00D_AGE_RUNNING_COST_YR, 0);
			}

			/* Draw max speed */
			{
				Global.SetDParam(0, v.getMax_speed() * 8);
				Gfx.DrawString(2, 25, Str.STR_A00E_MAX_SPEED, 0);
			}

			/* Draw profit */
			{
				Global.SetDParam(0, v.profit_this_year);
				Global.SetDParam(1, v.profit_last_year);
				Gfx.DrawString(2, 35, Str.STR_A00F_PROFIT_THIS_YEAR_LAST_YEAR, 0);
			}

			/* Draw breakdown & reliability */
			{
				Global.SetDParam(0, v.reliability * 100 >> 16);
				Global.SetDParam(1, v.breakdowns_since_last_service);
				Gfx.DrawString(2, 45, Str.STR_A010_RELIABILITY_BREAKDOWNS, 0);
			}

			/* Draw service interval text */
			{
				Global.SetDParam(0, v.service_interval);
				Global.SetDParam(1, v.date_of_last_service);
				Gfx.DrawString(13, 103, Global._patches.servint_ispercent?Str.STR_SERVICING_INTERVAL_PERCENT:Str.STR_883C_SERVICING_INTERVAL_DAYS, 0);
			}

			DrawAircraftImage(v, 3, 57, VehicleID.getInvalid() );

			{
				Vehicle u;
				int y = 57;

				do {
					if (v.subtype <= 2) {
						Global.SetDParam(0, Engine.GetCustomEngineName(v.getEngine_type().id).id);
						Global.SetDParam(1, 1920 + v.getBuild_year());
						Global.SetDParam(2, v.value);
						Gfx.DrawString(60, y, Str.STR_A011_BUILT_VALUE, 0);
						y += 10;

						Global.SetDParam(0, Global._cargoc.names_long[v.getCargo_type()]);
						Global.SetDParam(1, v.getCargo_cap());
						u = v.next;
						Global.SetDParam(2, Global._cargoc.names_long[u.getCargo_type()]);
						Global.SetDParam(3, u.getCargo_cap());
						Gfx.DrawString(60, y, (u.getCargo_cap() != 0) ? Str.STR_A019_CAPACITY : Str.STR_A01A_CAPACITY, 0);
						y += 14;
					}

					if (v.cargo_count != 0) {

						/* Cargo names (fix pluralness) */
						Global.SetDParam(0, v.getCargo_type());
						Global.SetDParam(1, v.cargo_count);
						Global.SetDParam(2, v.getCargo_source());
						Gfx.DrawString(60, y, Str.STR_8813_FROM, 0);

						y += 10;
					}
				} while ( (v=v.next) != null);
			}
		} break;

		case WE_CLICK: {
			int mod;
			final Vehicle v;
			switch (e.widget) {
			case 2: /* rename */
				v = Vehicle.GetVehicle(w.window_number);
				Global.SetDParam(0, v.unitnumber.id);
				MiscGui.ShowQueryString( new StringID(v.string_id), new StringID(Str.STR_A030_NAME_AIRCRAFT), 31, 150, w.getWindowClass(), w.window_number);
				break;
				/*	
			case 5: // increase int 
				mod = _ctrl_pressed? 5 : 10;
				goto do_change_service_int;
			case 6: // decrease int 
				mod = _ctrl_pressed?- 5 : -10;
				do_change_service_int:
				 */
			case 5: // increase int 
			case 6: // decrease int 

				if(e.widget == 5)
					mod = Global._ctrl_pressed? 5 : 10;
				else
					mod = Global._ctrl_pressed? -5 : -10;

				v = Vehicle.GetVehicle(w.window_number);

				mod = Depot.GetServiceIntervalClamped(mod + v.service_interval);
				if (mod == v.service_interval) return;

				Cmd.DoCommandP(v.tile, v.index, mod, null, Cmd.CMD_CHANGE_AIRCRAFT_SERVICE_INT | Cmd.CMD_MSG(Str.STR_018A_CAN_T_CHANGE_SERVICING));
				break;
			}
		} break;

		case WE_4:
			if (Window.FindWindowById(Window.WC_VEHICLE_VIEW, w.window_number) == null)
				w.DeleteWindow();
			break;

		case WE_ON_EDIT_TEXT:
			if (e.str != null) {
				Global._cmd_text = e.str;
				Cmd.DoCommandP(null, w.window_number, 0, null,
						Cmd.CMD_NAME_VEHICLE | Cmd.CMD_MSG(Str.STR_A031_CAN_T_NAME_AIRCRAFT));
			}
			break;
		default:
			break;
		}
	}


	static final Widget _aircraft_details_widgets[] = {
			new Widget(    Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,					Str.STR_018B_CLOSE_WINDOW),
			new Widget(     Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   349,     0,    13, Str.STR_A00C_DETAILS,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   350,   389,     0,    13, Str.STR_01AA_NAME,			Str.STR_A032_NAME_AIRCRAFT),
			new Widget(      Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   389,    14,    55, 0x0,								Str.STR_NULL),
			new Widget(      Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   389,    56,   101, 0x0,								Str.STR_NULL),
			new Widget(  Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    10,   102,   107, Str.STR_0188,					Str.STR_884D_INCREASE_SERVICING_INTERVAL),
			new Widget(  Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    10,   108,   113, Str.STR_0189,					Str.STR_884E_DECREASE_SERVICING_INTERVAL),
			new Widget(      Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    11,   389,   102,   113, 0x0,								Str.STR_NULL),
			//{   WIDGETS_END},
	};

	static final WindowDesc _aircraft_details_desc = new WindowDesc(
			-1,-1, 390, 114,
			Window.WC_VEHICLE_DETAILS,Window.WC_VEHICLE_VIEW,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_aircraft_details_widgets,
			AirCraft::AircraftDetailsWndProc
			);


	static void ShowAircraftDetailsWindow(final Vehicle  v)
	{
		Window w;
		/*VehicleID*/ int veh = v.index;

		Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, veh);
		Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, veh);

		//_alloc_wnd_parent_num = veh;
		w = Window.AllocateWindowDesc(_aircraft_details_desc, veh);
		w.window_number = veh;
		w.caption_color =  v.owner.id;
		//		w.vscroll.cap = 6;
		//		w.traindetails_d.tab = 0;
	}


	static final Widget _aircraft_view_widgets[] = {
			new Widget(  Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,  14,   0,  10,   0,  13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW ),
			new Widget(  Window.WWT_CAPTION,    Window.RESIZE_RIGHT, 14,  11, 237,   0,  13, Str.STR_A00A, Str.STR_018C_WINDOW_TITLE_DRAG_THIS ),
			new Widget(  Window.WWT_STICKYBOX,  Window.RESIZE_LR,    14, 238, 249,   0,  13, 0x0,      Str.STR_STICKY_BUTTON ),
			new Widget(  Window.WWT_IMGBTN,     Window.RESIZE_RB,    14,   0, 231,  14, 103, 0x0,      Str.STR_NULL ),
			new Widget(  Window.WWT_6,          Window.RESIZE_RB,    14,   2, 229,  16, 101, 0x0,      Str.STR_NULL ),
			new Widget(  Window.WWT_PUSHIMGBTN, Window.RESIZE_RTB,   14,   0, 237, 104, 115, 0x0,      Str.STR_A027_CURRENT_AIRCRAFT_ACTION ),
			new Widget(  Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  14,  31, 0x2AB,    Str.STR_A029_CENTER_MAIN_VIEW_ON_AIRCRAFT ),
			new Widget(  Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  32,  49, 0x2AF,    Str.STR_A02A_SEND_AIRCRAFT_TO_HANGAR ),
			new Widget(  Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  50,  67, 0x2B4,    Str.STR_A03B_REFIT_AIRCRAFT_TO_CARRY ),
			new Widget(  Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  68,  85, 0x2B2,    Str.STR_A028_SHOW_AIRCRAFT_S_ORDERS ),
			new Widget(  Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  86, 103, 0x2B3,    Str.STR_A02B_SHOW_AIRCRAFT_DETAILS ),
			new Widget(  Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  32,  49, Sprite.SPR_CLONE_AIRCRAFT,      Str.STR_CLONE_AIRCRAFT_INFO ),
			new Widget(  Window.WWT_PANEL,      Window.RESIZE_LRB,   14, 232, 249, 104, 103, 0x0,      Str.STR_NULL ),
			new Widget(  Window.WWT_RESIZEBOX,  Window.RESIZE_LRTB,  14, 238, 249, 104, 115, 0x0,      Str.STR_NULL ),
			//{ WIDGETS_END }
	};



	static void AircraftViewWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			final Vehicle  v = Vehicle.GetVehicle(w.window_number);
			int disabled = 1 << 8;
			//StringID 
			int str;

			if (v.isStopped() && IsAircraftHangarTile(v.tile)) {
				disabled = 0;
			}

			if (!v.owner.isLocalPlayer()) disabled |= 1 << 8 | 1 << 7;
			w.disabled_state = disabled;

			/* draw widgets & caption */
			Global.SetDParam(0, v.string_id);
			Global.SetDParam(1, v.unitnumber.id);
			w.DrawWindowWidgets();

			if(v.isCrashed()) {
				str = Str.STR_8863_CRASHED;
			} else if(v.isStopped()) {
				str = Str.STR_8861_STOPPED;
			} else {
				switch (v.getCurrent_order().type) {
				case Order.OT_GOTO_STATION: {
					Global.SetDParam(0, v.getCurrent_order().station);
					Global.SetDParam(1, v.cur_speed * 8 / Global._patches.aircraft_speed_coeff);
					str = Str.STR_HEADING_FOR_STATION + (Global._patches.vehicle_speed ? 1 : 0);
				} break;

				case Order.OT_GOTO_DEPOT: {
					Global.SetDParam(0, v.getCurrent_order().station);
					Global.SetDParam(1, v.cur_speed * 8 / Global._patches.aircraft_speed_coeff);
					str = Str.STR_HEADING_FOR_HANGAR + (Global._patches.vehicle_speed ? 1 : 0);
				} break;

				case Order.OT_LOADING:
					Global.SetDParam(0, v.getCurrent_order().station);
					Global.SetDParam(1, v.cur_speed * 8 / Global._patches.aircraft_speed_coeff);
					str = Str.STR_882F_LOADING_UNLOADING;
					break;

				default:
					if (v.num_orders == 0) {
						str = Str.STR_NO_ORDERS + (Global._patches.vehicle_speed ? 1 : 0);
						Global.SetDParam(0, v.cur_speed * 8 / Global._patches.aircraft_speed_coeff);
					} else {
						str = Str.STR_EMPTY;
					}
					break;
				}
			}

			/* draw the flag plus orders */
			Gfx.DrawSprite((v.isStopped()) ? Sprite.SPR_FLAG_VEH_STOPPED : Sprite.SPR_FLAG_VEH_RUNNING, 2, w.getWidget(5).top + 1);
			Gfx.DrawStringCenteredTruncated(w.getWidget(5).left + 8, w.getWidget(5).right, w.getWidget(5).top + 1, new StringID(str), 0);
			w.DrawWindowViewport();
		} break;

		case WE_CLICK: {
			final Vehicle  v = Vehicle.GetVehicle(w.window_number);

			switch (e.widget) {
			case 5: /* start stop */
				Cmd.DoCommandP(v.tile, v.index, 0, null, Cmd.CMD_START_STOP_AIRCRAFT | Cmd.CMD_MSG(Str.STR_A016_CAN_T_STOP_START_AIRCRAFT));
				break;
			case 6: /* center main view */
				ViewPort.ScrollMainWindowTo(v.getX_pos(), v.getY_pos());
				break;
			case 7: /* goto hangar */
				Cmd.DoCommandP(v.tile, v.index, 0, null, Cmd.CMD_SEND_AIRCRAFT_TO_HANGAR | Cmd.CMD_MSG(Str.STR_A012_CAN_T_SEND_AIRCRAFT_TO));
				break;
			case 8: /* refit */
				ShowAircraftRefitWindow(v);
				break;
			case 9: /* show orders */
				OrderGui.ShowOrdersWindow(v);
				break;
			case 10: /* show details */
				ShowAircraftDetailsWindow(v);
				break;
			case 11:
				/* clone vehicle */
				Cmd.DoCommandP(v.tile, v.index, Global._ctrl_pressed ? 1 : 0, AirCraft::CcCloneAircraft, Cmd.CMD_CLONE_VEHICLE | Cmd.CMD_MSG(Str.STR_A008_CAN_T_BUILD_AIRCRAFT));
				break;
			}
		} break;

		case WE_RESIZE:
			w.resizeViewPort(e);
			break;

		case WE_DESTROY:
			Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, w.window_number);
			Window.DeleteWindowById(Window.WC_VEHICLE_REFIT, w.window_number);
			Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, w.window_number);
			break;

		case WE_MOUSELOOP: {
			final Vehicle  v = Vehicle.GetVehicle(w.window_number);
			int h = CheckStoppedInHangar(v) ? (1 << 7) : (1 << 11);

			if (h != w.hidden_state) {
				w.hidden_state = h;
				w.SetWindowDirty();
			}
		} break;
		default:
			break;
		}
	}


	static final WindowDesc _aircraft_view_desc = new WindowDesc(
			-1,-1, 250, 116,
			Window.WC_VEHICLE_VIEW ,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_aircraft_view_widgets,
			AirCraft::AircraftViewWndProc
			);


	public static void ShowAircraftViewWindow(final Vehicle  v)
	{
		Window  w = Window.AllocateWindowDescFront(_aircraft_view_desc, v.index);

		if (w != null) {
			w.caption_color =  v.owner.id;
			ViewPort.AssignWindowViewport(w, 3, 17, 0xE2, 0x54, w.window_number | (1 << 31), 0);
		}
	}

	static void DrawAircraftDepotWindow(Window w)
	{
		int num,x,y;

		TileIndex tile = new TileIndex(w.window_number);

		/* setup disabled buttons */
		w.disabled_state =
				tile.IsTileOwner(Global.gs._local_player) ? 0 : ((1<<4) | (1<<7) | (1<<8));

		/* determine amount of items for scroller */
		num = 0;

		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();

			if (v.type == Vehicle.VEH_Aircraft &&
					v.subtype <= 2 &&
					v.isHidden() &&
					v.tile.getTile() == tile.getTile() ) {
				num++;
			}
		}

		w.SetVScrollCount( (num + w.hscroll.getCap() - 1) / w.hscroll.getCap());

		Global.SetDParam(0, tile.getMap().m2);
		w.DrawWindowWidgets();

		x = 2;
		y = 15;
		num = w.vscroll.getPos() * w.hscroll.getCap();

		Iterator<Vehicle> ii1 = Vehicle.getIterator();
		while(ii1.hasNext())
		{
			Vehicle v = ii1.next();

			if (v.type == Vehicle.VEH_Aircraft &&
					v.subtype <= 2 &&
					v.isHidden() &&
					v.tile.getTile() == tile.getTile() &&
					--num < 0 && num >= -w.vscroll.getCap() * w.hscroll.getCap()) {

				DrawAircraftImage(v, x+12, y, VehicleID.get( w.as_traindepot_d().sel ) );

				Global.SetDParam(0, v.unitnumber.id);
				Gfx.DrawString(x, y+2, (v.max_age-366) >= v.age ? Str.STR_00E2 : Str.STR_00E3, 0);

				Gfx.DrawSprite( v.isStopped() ? Sprite.SPR_FLAG_VEH_STOPPED : Sprite.SPR_FLAG_VEH_RUNNING, x, y + 12);

				if ((x+=74) == 2 + 74 * w.hscroll.getCap()) {
					x = 2;
					y += 24;
				}
			}
		}
	}

	static int GetVehicleFromAircraftDepotWndPt(final Window w, int x, int y, Vehicle[] veh) 
	{
		int xt,row,xm,ym;
		int pos;

		xt = x / 74;
		xm = x % 74;
		if (xt >= w.hscroll.getCap())
			return 1;

		row = (y - 14) / 24;
		ym = (y - 14) % 24;
		if (row >= w.vscroll.getCap())
			return 1;

		pos = (row + w.vscroll.getPos()) * w.hscroll.getCap() + xt;

		int tile = w.window_number;

		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();
			if (v.type == Vehicle.VEH_Aircraft && v.subtype <= 2 &&
					v.isHidden() && v.tile.tile == tile &&
					--pos < 0) {
				veh[0] = v;
				if (xm >= 12) return 0;
				if (ym <= 12) return -1; /* show window */
				return -2; /* start stop */
			}
		}
		return 1; /* outside */
	}

	static void AircraftDepotClickAircraft(Window w, int x, int y)
	{
		Vehicle [] v = { null };
		int mode = GetVehicleFromAircraftDepotWndPt(w, x, y, v);

		// share / copy orders
		if (ViewPort._thd.place_mode != 0 && mode <= 0) {
			Global._place_clicked_vehicle = v[0];
			return;
		}

		switch (mode) {
		case 1:
			return;

		case 0: // start dragging of vehicle
			if (v[0] != null) {
				w.as_traindepot_d().sel = v[0].index;
				w.SetWindowDirty();
				ViewPort.SetObjectToPlaceWnd( Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(v[0].owner)) + GetAircraftImage(v[0], 6), 4, w);
			}
			break;

		case -1: // show info window
			ShowAircraftViewWindow(v[0]);
			break;

		case -2: // click start/stop flag
			Cmd.DoCommandP(v[0].tile, v[0].index, 0, null, Cmd.CMD_START_STOP_AIRCRAFT | Cmd.CMD_MSG(Str.STR_A016_CAN_T_STOP_START_AIRCRAFT));
			break;

		default:
			//NOT_REACHED();
			assert false;
		}
	}

	/**
	 * Clones an aircraft
	 * @param v is the original vehicle to clone
	 * @param w is the window of the hangar where the clone is build
	 */
	static void HandleCloneVehClick(final Vehicle  v, final Window  w)
	{
		if (v == null || v.type != Vehicle.VEH_Aircraft) return;

		Cmd.DoCommandP(TileIndex.get(w.window_number), v.index, Global._ctrl_pressed ? 1 : 0,
				AirCraft::CcCloneAircraft, Cmd.CMD_CLONE_VEHICLE | Cmd.CMD_MSG(Str.STR_A008_CAN_T_BUILD_AIRCRAFT)
				);

		ViewPort.ResetObjectToPlace();
	}

	static void ClonePlaceObj(TileIndex tile, final Window  w)
	{
		final Vehicle  v = ViewPort.CheckMouseOverVehicle();

		if (v != null) HandleCloneVehClick(v, w);
	}


	static void AircraftDepotWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT:
			DrawAircraftDepotWindow(w);
			break;

		case WE_CLICK:
			switch(e.widget) {
			case 5: /* click aircraft */
				AircraftDepotClickAircraft(w, e.pt.x, e.pt.y);
				break;

			case 7: /* show build aircraft window */
				ViewPort.ResetObjectToPlace();
				ShowBuildAircraftWindow(new TileIndex( w.window_number ));
				break;

			case 8: /* clone button */
				w.InvalidateWidget(8);
				w.click_state = BitOps.RETTOGGLEBIT(w.click_state, 8);

				if (BitOps.HASBIT(w.click_state, 8)) {
					Global._place_clicked_vehicle = null;
					ViewPort.SetObjectToPlaceWnd(Sprite.SPR_CURSOR_CLONE, ViewPort.VHM_RECT, w);
				} else {
					ViewPort.ResetObjectToPlace();
				}
				break;

			case 9: /* scroll to tile */
				ViewPort.ResetObjectToPlace();
				ViewPort.ScrollMainWindowToTile( TileIndex.get( w.window_number ) );
				break;
			}
			break;

		case WE_PLACE_OBJ: {
			ClonePlaceObj(e.tile, w);
		} break;

		case WE_ABORT_PLACE_OBJ: {
			w.click_state = BitOps.RETCLRBIT(w.click_state, 8);
			w.InvalidateWidget(8);
		} break;

		// check if a vehicle in a depot was clicked..
		case WE_MOUSELOOP: {
			final Vehicle  v = Global._place_clicked_vehicle;

			// since OTTD checks all open depot windows, we will make sure that it triggers the one with a clicked clone button
			if (v != null && BitOps.HASBIT(w.click_state, 8)) {
				Global._place_clicked_vehicle = null;
				HandleCloneVehClick(v, w);
			}
		} break;

		case WE_DESTROY:
			Window.DeleteWindowById(Window.WC_BUILD_VEHICLE, w.window_number);
			break;

		case WE_DRAGDROP:
			switch(e.widget) {
			case 5: {
				Vehicle [] vp = { null };
				//VehicleID 
				int sel = w.as_traindepot_d().sel;

				w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;
				w.SetWindowDirty();

				if (GetVehicleFromAircraftDepotWndPt(w, e.pt.x, e.pt.y, vp) == 0 &&
						vp[0] != null &&
						sel == vp[0].index) 
				{
					ShowAircraftViewWindow(vp[0]);
				}
			} break;

			case 4:
				if (!BitOps.HASBIT(w.disabled_state, 4) &&
						w.as_traindepot_d().sel != Vehicle.INVALID_VEHICLE)	{
					Vehicle v;

					w.HandleButtonClick( 4);

					v = Vehicle.GetVehicle(w.as_traindepot_d().sel);
					w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;

					Global._backup_orders_tile = v.tile;
					Vehicle.BackupVehicleOrders(v, Global._backup_orders_data[0]);

					if (!Cmd.DoCommandP(v.tile, v.index, 0, null,  Cmd.CMD_SELL_AIRCRAFT | Cmd.CMD_MSG(Str.STR_A01C_CAN_T_SELL_AIRCRAFT)))
						Global._backup_orders_tile = null;
				}
				break;
			default:
				w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;
				w.SetWindowDirty();
			}
			break;

		case WE_RESIZE:
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / 24);
			w.hscroll.setCap(w.hscroll.getCap() + e.diff.x / 74);
			w.getWidget(5).unkA = (w.vscroll.getCap() << 8) + w.hscroll.getCap();
			break;
		default:
			break;
		}
	}

	static final Widget _aircraft_depot_widgets[] = {
			new Widget(    Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW),
			new Widget(     Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   318,     0,    13, Str.STR_A002_AIRCRAFT_HANGAR,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(   Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   319,   330,     0,    13, 0x0,											Str.STR_STICKY_BUTTON),
			new Widget(       Window.WWT_PANEL,    Window.RESIZE_LRB,    14,   296,   318,    14,    13, 0x0,													Str.STR_NULL),
			new Widget(      Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    14,   296,   318,    14,    61, 0x2A9,										Str.STR_A023_DRAG_AIRCRAFT_TO_HERE_TO),

			new Widget(      Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   295,    14,    61, 0x204,										Str.STR_A021_AIRCRAFT_CLICK_ON_AIRCRAFT),
			new Widget(   Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   319,   330,    14,    61, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   105,    62,    73, Str.STR_A003_NEW_AIRCRAFT,		Str.STR_A022_BUILD_NEW_AIRCRAFT),
			new Widget( Window.WWT_NODISTXTBTN,     Window.RESIZE_TB,    14,   106,   212,    62,    73, Str.STR_CLONE_AIRCRAFT,		Str.STR_CLONE_AIRCRAFT_INFO_HANGAR_WINDOW),
			new Widget(  Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   213,   318,    62,    73, Str.STR_00E4_LOCATION,				Str.STR_A024_CENTER_MAIN_VIEW_ON_HANGAR),
			new Widget(       Window.WWT_PANEL,    Window.RESIZE_RTB,    14,   319,   318,    62,    73, 0x0,													Str.STR_NULL),
			new Widget(   Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   319,   330,    62,    73, 0x0,											Str.STR_RESIZE_BUTTON),
			//{   WIDGETS_END},
	};

	static final WindowDesc _aircraft_depot_desc = new WindowDesc(
			-1, -1, 331, 74,
			Window.WC_VEHICLE_DEPOT,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_aircraft_depot_widgets,
			AirCraft::AircraftDepotWndProc
			);


	static void ShowAircraftDepotWindow(TileIndex tile)
	{
		Window w;

		w = Window.AllocateWindowDescFront(_aircraft_depot_desc, tile.tile);
		if (w != null) {
			w.caption_color =  tile.GetTileOwner().id;
			w.vscroll.setCap(2);
			w.hscroll.setCap(4);
			w.resize.step_width = 74;
			w.resize.step_height = 24;
			w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;
			Global._backup_orders_tile = null;
		}
	}

	static void DrawSmallOrderList(final Vehicle v, int x, int y) {
		int sel, i = 0;

		sel = v.cur_order_index;

		Iterator<Order> oi = v.getOrdersIterator();
		while(oi.hasNext())
		{
			Order order = oi.next();
			String ss = String.valueOf(0xAF); //"\xAF"
			if (sel == 0) {
				Gfx._stringwidth_base = 0xE0;
				Gfx.DoDrawString( ss, x-6, y, 16);
				Gfx._stringwidth_base = 0;
			}
			sel--;

			if (order.type == Order.OT_GOTO_STATION) {
				Global.SetDParam(0, order.station);
				Gfx.DrawString(x, y, Str.STR_A036, 0);

				y += 6;
				if (++i == 4) break;
			}
		}
	}


	static final Widget _player_aircraft_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   247,     0,    13, Str.STR_A009_AIRCRAFT,			Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   248,   259,     0,    13, 0x0,                   Str.STR_STICKY_BUTTON),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    80,    14,    25, Str.SRT_SORT_BY,						Str.STR_SORT_ORDER_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    81,   235,    14,    25, 0x0,										Str.STR_SORT_CRITERIA_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   236,   247,    14,    25, Str.STR_0225,							Str.STR_SORT_CRITERIA_TIP),
			new Widget(      Window.WWT_PANEL,  Window.RESIZE_RIGHT,    14,   248,   259,    14,    25, 0x0,										Str.STR_NULL),
			new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   247,    26,   169, 0x401,									Str.STR_A01F_AIRCRAFT_CLICK_ON_AIRCRAFT),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   248,   259,    26,   169, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   124,   170,   181, Str.STR_A003_NEW_AIRCRAFT,	Str.STR_A020_BUILD_NEW_AIRCRAFT_REQUIRES),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   125,   247,   170,   181, Str.STR_REPLACE_VEHICLES,						Str.STR_REPLACE_HELP),
			new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,   248,   247,   170,   181, 0x0,											Str.STR_NULL),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   248,   259,   170,   181, 0x0,											Str.STR_RESIZE_BUTTON),
			//new Widget(   WIDGETS_END),
	};

	static final Widget _other_player_aircraft_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   247,     0,    13, Str.STR_A009_AIRCRAFT,			Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   248,   259,     0,    13, 0x0,                   Str.STR_STICKY_BUTTON),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    80,    14,    25, Str.SRT_SORT_BY,						Str.STR_SORT_ORDER_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    81,   235,    14,    25, 0x0,										Str.STR_SORT_CRITERIA_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   236,   247,    14,    25, Str.STR_0225,							Str.STR_SORT_CRITERIA_TIP),
			new Widget(      Window.WWT_PANEL,  Window.RESIZE_RIGHT,    14,   248,   259,    14,    25, 0x0,										Str.STR_NULL),
			new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   247,    26,   169, 0x401,									Str.STR_A01F_AIRCRAFT_CLICK_ON_AIRCRAFT),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   248,   259,    26,   169, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,     0,   247,   170,   181, 0x0,											Str.STR_NULL),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   248,   259,   170,   181, 0x0,											Str.STR_RESIZE_BUTTON),
			//new Widget(   WIDGETS_END),
	};

	static void PlayerAircraftWndProc(Window w, WindowEvent e)
	{
		//StationID 
		int station = BitOps.GB(w.window_number, 16, 16);
		//PlayerID 
		int owner = BitOps.GB(w.window_number, 0, 8);
		vehiclelist_d vl = w.as_vehiclelist_d();

		switch(e.event) {
		case WE_PAINT: {
			int x = 2;
			int y = VehicleGui.PLY_WND_PRC__OFFSET_TOP_WIDGET;
			int max;
			int i;

			VehicleGui.BuildVehicleList(vl, Vehicle.VEH_Aircraft, owner, station);
			VehicleGui.SortVehicleList(vl);

			w.SetVScrollCount( vl.list_length);

			// disable 'Sort By' tooltip on Unsorted sorting criteria
			if (vl.sort_type == VehicleGui.SORT_BY_UNSORTED) w.disabled_state |= (1 << 3);

			/* draw the widgets */
			{
				final Player p = Player.GetPlayer(owner);
				if (station == Station.INVALID_STATION) {
					/* Company Name -- (###) Aircraft */
					Global.SetDParam(0, p.name_1);
					Global.SetDParam(1, p.name_2);
					Global.SetDParam(2, w.vscroll.getCount());
					w.getWidget(1).unkA = Str.STR_A009_AIRCRAFT;
				} else {
					/* Station Name -- (###) Aircraft */
					Global.SetDParam(0, station);
					Global.SetDParam(1, w.vscroll.getCount());
					w.getWidget(1).unkA = Str.STR_SCHEDULED_AIRCRAFT;
				}
				w.DrawWindowWidgets();
			}
			/* draw sorting criteria string */
			Gfx.DrawString(85, 15, VehicleGui._vehicle_sort_listing[vl.sort_type], 0x10);
			/* draw arrow pointing up/down for ascending/descending sorting */
			Gfx.DoDrawString( (vl.flags & Vehicle.VL_DESC) != 0 ? Gfx.DOWNARROW : Gfx.UPARROW, 69, 15, 0x10);

			max = Math.min(w.vscroll.getPos() + w.vscroll.getCap(), vl.list_length);
			for (i = w.vscroll.getPos(); i < max; ++i) {
				Vehicle v = Vehicle.GetVehicle(vl.sort_list[i].index);
				//StringID 
				int str;

				assert(v.type == Vehicle.VEH_Aircraft && v.subtype <= 2);

				DrawAircraftImage(v, x + 19, y + 6, Vehicle.INVALID_VEHICLE);
				VehicleGui.DrawVehicleProfitButton(v, x, y + 13);

				Global.SetDParam(0, v.unitnumber.id);
				if(IsAircraftHangarTile(v.tile) && v.isHidden()) {
					str = Str.STR_021F;
				} else {
					str = v.age > v.max_age - 366 ? Str.STR_00E3 : Str.STR_00E2;
				}
				Gfx.DrawString(x, y + 2, str, 0);

				Global.SetDParam(0, v.profit_this_year);
				Global.SetDParam(1, v.profit_last_year);
				Gfx.DrawString(x + 19, y + 28, Str.STR_0198_PROFIT_THIS_YEAR_LAST_YEAR, 0);

				if (v.string_id != Str.STR_SV_AIRCRAFT_NAME) {
					Global.SetDParam(0, v.string_id);
					Gfx.DrawString(x + 19, y, Str.STR_01AB, 0);
				}

				DrawSmallOrderList(v, x + 136, y);

				y += VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_BIG;
			}
		}	break;

		case WE_CLICK: {
			switch(e.widget) {
			case 3: /* Flip sorting method ascending/descending */
				vl.flags ^= Vehicle.VL_DESC;
				vl.flags |= Vehicle.VL_RESORT;
				VehicleGui._sorting.aircraft.order = 0 != (vl.flags & Vehicle.VL_DESC);
				w.SetWindowDirty();
				break;

			case 4: case 5:/* Select sorting criteria dropdown menu */
				Window.ShowDropDownMenu(w, VehicleGui._vehicle_sort_listing, vl.sort_type, 5, 0, 0);
				return;

			case 7: { /* Matrix to show vehicles */
				int id_v = (e.pt.y - VehicleGui.PLY_WND_PRC__OFFSET_TOP_WIDGET) / VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_BIG;

				if (id_v >= w.vscroll.getCap()) return; // click out of bounds

				id_v += w.vscroll.getPos();

				{
					Vehicle v;

					if (id_v >= vl.list_length) return; // click out of list bound

					v = Vehicle.GetVehicle(vl.sort_list[id_v].index);

					assert(v.type == Vehicle.VEH_Aircraft && v.subtype <= 2);

					ShowAircraftViewWindow(v);
				}
			} break;

			case 9: { /* Build new Vehicle */
				TileIndex tile;

				if (!Window.IsWindowOfPrototype(w, _player_aircraft_widgets))
					break;

				tile = Depot._last_built_aircraft_depot_tile;
				if( tile == null )
				{
					// TODO err no hangar
					break;
				}
				do {
					if (IsAircraftHangarTile(tile) && tile.IsTileOwner(Global.gs._local_player)) {
						ShowAircraftDepotWindow(tile);
						ShowBuildAircraftWindow(tile);
						return;
					}

					tile = tile.iadd(1);
					tile.TILE_MASK();
				} while(!tile.equals(Depot._last_built_aircraft_depot_tile));

				ShowBuildAircraftWindow(null);
			} break;

			case 10:
				if (!Window.IsWindowOfPrototype(w, _player_aircraft_widgets))
					break;

				VehicleGui.ShowReplaceVehicleWindow(Vehicle.VEH_Aircraft);
				break;

			}
		}	break;

		case WE_DROPDOWN_SELECT: /* we have selected a dropdown item in the list */
			if (vl.sort_type != e.index) {
				// value has changed . resort
				vl.flags |= Vehicle.VL_RESORT;
				vl.sort_type =  e.index;
				VehicleGui._sorting.aircraft.criteria = vl.sort_type;

				// enable 'Sort By' if a sorter criteria is chosen
				if (vl.sort_type != VehicleGui.SORT_BY_UNSORTED) 
					w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 3);
			}
			w.SetWindowDirty();
			break;

		case WE_CREATE: /* set up resort timer */
			vl.sort_list = null;
			vl.flags = Vehicle.VL_REBUILD | ((VehicleGui._sorting.aircraft.order ? 1 : 0) << (Vehicle.VL_DESC - 1));
			vl.sort_type =  VehicleGui._sorting.aircraft.criteria;
			vl.resort_timer = Global.DAY_TICKS * VehicleGui.PERIODIC_RESORT_DAYS;
			break;

		case WE_DESTROY:
			//free(vl.sort_list);
			break;

		case WE_TICK: /* resort the list every 20 seconds orso (10 days) */
			if (--vl.resort_timer == 0) {
				Global.DEBUG_misc( 1, "Periodic resort aircraft list player %d station %d",
						owner, station);
				vl.resort_timer = Global.DAY_TICKS * VehicleGui.PERIODIC_RESORT_DAYS;
				vl.flags |= Vehicle.VL_RESORT;
				w.SetWindowDirty();
			}
			break;

		case WE_RESIZE:
			/* Update the scroll + matrix */
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_BIG);
			w.getWidget(7).unkA = (w.vscroll.getCap() << 8) + 1;
			break;
		default:
			break;
		}
	}

	static final WindowDesc _player_aircraft_desc = new WindowDesc(
			-1, -1, 260, 182,
			Window.WC_AIRCRAFT_LIST,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_player_aircraft_widgets,
			AirCraft::PlayerAircraftWndProc
			);

	static final WindowDesc _other_player_aircraft_desc = new WindowDesc(
			-1, -1, 260, 182,
			Window.WC_AIRCRAFT_LIST,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_other_player_aircraft_widgets,
			AirCraft::PlayerAircraftWndProc
			);

	static void ShowPlayerAircraft(PlayerID player, StationID station)
	{
		ShowPlayerAircraft(player.id, station.id);
	}

	public static void ShowPlayerAircraft(int player, int station)
	{
		Window w;

		if (player == Global.gs._local_player.id) {
			w = Window.AllocateWindowDescFront(_player_aircraft_desc, (station << 16) | player);
		} else  {
			w = Window.AllocateWindowDescFront(_other_player_aircraft_desc, (station << 16) | player);
		}

		if (w != null) {
			w.caption_color =  w.window_number;
			w.vscroll.setCap(4);
			w.getWidget(7).unkA = (w.vscroll.getCap() << 8) + 1;
			w.resize.step_height = VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_BIG;
		}
	}


}



