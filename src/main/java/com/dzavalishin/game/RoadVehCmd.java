package com.dzavalishin.game;

import java.util.Comparator;
import java.util.List;

import com.dzavalishin.ids.EngineID;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.UnitID;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.RoadStopType;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.struct.FindRoadToChooseData;
import com.dzavalishin.struct.GetNewVehiclePosResult;
import com.dzavalishin.struct.NPFFindStationOrTileData;
import com.dzavalishin.struct.OvertakeData;
import com.dzavalishin.struct.Point;
import com.dzavalishin.struct.RoadFindDepotData;
import com.dzavalishin.struct.RoadVehFindData;
import com.dzavalishin.tables.EngineTables2;
import com.dzavalishin.tables.RoadVehCmdTables;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.ShortSounds;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.VehicleGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

public class RoadVehCmd extends RoadVehCmdTables {


	public static final int STATUS_BAR = AirCraft.STATUS_BAR;

	public static int GetRoadVehImage(final Vehicle v, int direction)
	{
		int img = v.spritenum;
		int image;

		if (Sprite.is_custom_sprite(img)) {
			image = Engine.GetCustomVehicleSprite(v, direction);
			if (image != 0) return image;
			img = EngineTables2.orig_road_vehicle_info[v.engine_type.id - Global.ROAD_ENGINES_INDEX].image_index;
		}

		image = direction + _roadveh_images[img];
		if (v.cargo_count >= (v.getCargo_cap() >> 1))
			image += _roadveh_full_adder[img];
		return image;
	}

	public static void DrawRoadVehEngine(int x, int y, /*EngineID*/ int engine, int image_ormod)
	{
		int spritenum = Engine.RoadVehInfo(engine).image_index;

		if (Sprite.is_custom_sprite(spritenum)) {
			int sprite = Engine.GetCustomVehicleIcon(engine, 6);

			if (sprite != 0) {
				Gfx.DrawSprite(sprite | image_ormod, x, y);
				return;
			}
			spritenum = EngineTables2.orig_road_vehicle_info[engine - Global.ROAD_ENGINES_INDEX].image_index;
		}
		Gfx.DrawSprite((6 + _roadveh_images[spritenum]) | image_ormod, x, y);
	}

	static int EstimateRoadVehCost(/*EngineID*/ int engine_type)
	{
		return ((int)(((Global._price.roadveh_base / 8) * Engine.RoadVehInfo(engine_type).base_cost))) >> 5;
	}

	/** Build a road vehicle.
	 * @param x,y tile coordinates of depot where road vehicle is built
	 * @param p1 bus/truck type being built (engine)
	 * @param p2 unused
	 */
	static int CmdBuildRoadVeh(int x, int y, int flags, int p1, int p2)
	{
		int cost;
		Vehicle v;
		UnitID unit_num;
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		Engine e;

		if (!Engine.IsEngineBuildable(p1, Vehicle.VEH_Road)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_NEW_VEHICLES);

		cost = EstimateRoadVehCost(p1);
		if(0 != (flags & Cmd.DC_QUERY_COST)) return cost;

		/* The ai_new queries the vehicle cost before building the route,
		 * so we must check against cheaters no sooner than now. --pasky */
		if (!Depot.IsTileDepotType(tile, TransportType.Road)) return Cmd.CMD_ERROR;
		if (!tile.IsTileOwner(PlayerID.getCurrent())) return Cmd.CMD_ERROR;

		v = Vehicle.AllocateVehicle();
		if (v == null ) // || IsOrderPoolFull())
			return Cmd.return_cmd_error(Str.STR_00E1_TOO_MANY_VEHICLES_IN_GAME);

		/* find the first free roadveh id */
		unit_num = Vehicle.GetFreeUnitNumber(Vehicle.VEH_Road);
		if (unit_num.id > Global._patches.max_roadveh)
			return Cmd.return_cmd_error(Str.STR_00E1_TOO_MANY_VEHICLES_IN_GAME);

		if(0!= (flags & Cmd.DC_EXEC)) {
			final RoadVehicleInfo rvi = Engine.RoadVehInfo(p1);

			v.unitnumber = unit_num;
			v.direction = 0;
			v.owner = PlayerID.getCurrent();

			v.tile = tile;
			x = tile.TileX() * 16 + 8;
			y = tile.TileY() * 16 + 8;
			v.x_pos = x;
			v.y_pos = y;
			v.z_pos = Landscape.GetSlopeZ(x,y);
			v.z_height = 6;

			v.road.setInDepot(); //state = 254;
			v.assignStatus( Vehicle.VS_HIDDEN|Vehicle.VS_STOPPED|Vehicle.VS_DEFPAL );

			v.spritenum = rvi.image_index;
			v.cargo_type = rvi.cargo_type;
			v.cargo_cap = rvi.capacity;
			//		v.cargo_count = 0;
			v.value = cost;
			//		v.day_counter = 0;
			//		v.next_order_param = v.next_order = 0;
			//		v.load_unload_time_rem = 0;
			//		v.progress = 0;

			//	v.road.unk2 = 0;
			//	v.road.overtaking = 0;

			v.road.slot = null;
			v.road.slotindex = 0;
			v.road.slot_age = 0;

			v.last_station_visited = Station.INVALID_STATION;
			v.max_speed = rvi.max_speed;
			v.engine_type = EngineID.get(p1);

			e = Engine.GetEngine(p1);
			v.reliability = e.getReliability();
			v.reliability_spd_dec = e.reliability_spd_dec;
			v.max_age = e.getLifelength() * 366;
			Global._new_roadveh_id = VehicleID.get( v.index );
			Global._new_vehicle_id = VehicleID.get( v.index );

			v.string_id = Str.STR_SV_ROADVEH_NAME;

			v.service_interval = Global._patches.servint_roadveh;

			v.date_of_last_service = Global.get_date();
			v.build_year =  Global.get_cur_year();

			v.type = Vehicle.VEH_Road;
			v.cur_image = 0xC15;
			v.random_bits = Vehicle.VehicleRandomBits();

			v.VehiclePositionChanged();

			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
			VehicleGui.RebuildVehicleLists();
			Window.InvalidateWindow(Window.WC_COMPANY, v.owner);
			if (Player.IsLocalPlayer())
				Window.InvalidateWindow(Window.WC_REPLACE_VEHICLE, Vehicle.VEH_Road); // updates the replace Road window
		}

		return cost;
	}

	/** Start/Stop a road vehicle.
	 * @param x,y unused
	 * @param p1 road vehicle ID to start/stop
	 * @param p2 unused
	 */
	static int CmdStartStopRoadVeh(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Road || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if (0 !=(flags & Cmd.DC_EXEC)) {
			v.toggleStopped();
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
		}

		return 0;
	}

	static void ClearSlot(Vehicle v, RoadStop rs)
	{
		v.road.slot = null;
		v.road.slot_age = 0;
		if (rs != null) {
			Global.DEBUG_ms( 3, "Multistop: Clearing slot %d at %s", v.road.slotindex, rs.xy.toString());
			// check that the slot is indeed assigned to the same vehicle
			assert(rs.slot[v.road.slotindex] == v.index);
			rs.slot[v.road.slotindex] = Station.INVALID_SLOT;
		}
		else
			Global.DEBUG_ms( 3, "Multistop: Clearing slot %d at (null)", v.road.slotindex);

	}

	/** Sell a road vehicle.
	 * @param x,y unused
	 * @param p1 vehicle ID to be sold
	 * @param p2 unused
	 */
	static int CmdSellRoadVeh(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Road || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_NEW_VEHICLES);

		if (!Depot.IsTileDepotType(v.tile, TransportType.Road) || !v.road.isInDepot() || !v.isStopped())
			return Cmd.return_cmd_error(Str.STR_9013_MUST_BE_STOPPED_INSIDE);

		if(0 != (flags & Cmd.DC_EXEC)) {
			// Invalidate depot
			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
			VehicleGui.RebuildVehicleLists();
			Window.InvalidateWindow(Window.WC_COMPANY, v.owner);
			Window.DeleteWindowById(Window.WC_VEHICLE_VIEW, v.index);
			ClearSlot(v, v.road.slot);
			v.DeleteVehicle();
			if (Player.IsLocalPlayer())
				Window.InvalidateWindow(Window.WC_REPLACE_VEHICLE, Vehicle.VEH_Road); // updates the replace Road window
		}

		return -v.value;
	}



	static boolean EnumRoadSignalFindDepot(TileIndex tile, Object o, int track, int length, int [] state)
	{
		RoadFindDepotData rfdd = (RoadFindDepotData) o;
		tile = tile.iadd( TileIndex.TileOffsByDir(_road_pf_directions[track]) );

		if (tile.IsTileType( TileTypes.MP_STREET) &&
				BitOps.GB(tile.getMap().m5, 4, 4) == 2 &&
				tile.IsTileOwner(rfdd.owner)) {

			if (length < rfdd.best_length) {
				rfdd.best_length = length;
				rfdd.tile = tile;
			}
		}
		return false;
	}

	static Depot FindClosestRoadDepot(Vehicle v)
	{
		TileIndex tile = v.tile;
		int i;

		if (v.road.isInTunnel()) tile = TunnelBridgeCmd.GetVehicleOutOfTunnelTile(v);

		//TileMarker.mark(tile, 209);

		if (Global._patches.new_pathfinding_all) {
			NPFFoundTargetData ftd;
			/* See where we are now */
			/*Trackdir*/ int trackdir = v.GetVehicleTrackdir();

			ftd = Npf.NPFRouteToDepotBreadthFirst(v.tile, trackdir, TransportType.Road, v.owner, Rail.INVALID_RAILTYPE);
			if (ftd.best_bird_dist == 0)
				return Depot.GetDepotByTile(ftd.node.tile); /* Target found */
			else
				return null; /* Target not found */
			/* We do not search in two directions here, why should we? We can't reverse right now can we? */
		} else {
			RoadFindDepotData rfdd = new RoadFindDepotData();
			rfdd.owner = v.owner.id;
			rfdd.best_length = -1;

			/* search in all directions */
			for(i=0; i!=4; i++)
				Pathfind.FollowTrack(tile, TransportType.Road, 0x2000, i, RoadVehCmd::EnumRoadSignalFindDepot, null, rfdd);

			if (rfdd.best_length == -1)
				return null;

			return Depot.GetDepotByTile(rfdd.tile);
		}
	}

	/** Send a road vehicle to the depot.
	 * @param x,y unused
	 * @param p1 vehicle ID to send to the depot
	 * @param p2 unused
	 */
	static int CmdSendRoadVehToDepot(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		final Depot dep;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Road || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if(v.isCrashed()) return Cmd.CMD_ERROR;

		/* If the current orders are already goto-depot */
		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT) {
			if(0 != (flags & Cmd.DC_EXEC)) {
				/* If the orders to 'goto depot' are in the orders list (forced servicing),
				 * then skip to the next order; effectively cancelling this forced service */
				if (BitOps.HASBIT(v.getCurrent_order().flags, Order.OFB_PART_OF_ORDERS))
					v.cur_order_index++;

				v.getCurrent_order().type = Order.OT_DUMMY;
				v.getCurrent_order().flags = 0;
				Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
			}
			return 0;
		}

		dep = FindClosestRoadDepot(v);
		if (dep == null) return Cmd.return_cmd_error(Str.STR_9019_UNABLE_TO_FIND_LOCAL_DEPOT);

		if(0 != (flags & Cmd.DC_EXEC)) {
			v.getCurrent_order().type = Order.OT_GOTO_DEPOT;
			v.getCurrent_order().flags = Order.OF_NON_STOP | Order.OF_HALT_IN_DEPOT;
			v.getCurrent_order().station = dep.index;
			v.dest_tile = dep.xy;
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
		}

		return 0;
	}

	/** Turn a roadvehicle around.
	 * @param x,y unused
	 * @param p1 vehicle ID to turn
	 * @param p2 unused
	 */
	static int CmdTurnRoadVeh(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Road || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if ( v.isHidden() || v.isStopped() ||
				v.road.crashed_ctr != 0 || v.breakdown_ctr != 0 ||
				v.road.overtaking != 0 || v.cur_speed < 5) {
			return Cmd.CMD_ERROR;
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			v.road.reverse_ctr = 180;
		}

		return 0;
	}

	/** Change the service interval for road vehicles.
	 * @param x,y unused
	 * @param p1 vehicle ID that is being service-interval-changed
	 * @param p2 new service interval
	 */
	static int CmdChangeRoadVehServiceInt(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		int serv_int = Depot.GetServiceIntervalClamped(p2); /* Double check the service interval from the user-input */

		if (serv_int != p2 || !Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Road || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			v.service_interval = serv_int;
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_DETAILS, v.index, 7);
		}

		return 0;
	}


	static void MarkRoadVehDirty(Vehicle v)
	{
		v.cur_image = GetRoadVehImage(v, v.direction);
		ViewPort.MarkAllViewportsDirty(v.left_coord, v.top_coord, v.right_coord + 1, v.bottom_coord + 1);
	}

	static void UpdateRoadVehDeltaXY(Vehicle v)
	{
		int x = _delta_xy_table[v.direction];
		v.x_offs        = (byte) BitOps.GB(x,  0, 8); // NB! Signed byte!
		v.y_offs        = (byte) BitOps.GB(x,  8, 8);
		v.sprite_width  =  BitOps.GB(x, 16, 8);
		v.sprite_height =  BitOps.GB(x, 24, 8);
	}

	static void ClearCrashedStation(Vehicle v)
	{
		RoadStop rs = RoadStop.GetRoadStopByTile(v.tile, RoadStop.GetRoadStopType(v.tile));

		// mark station as not busy
		rs.status = BitOps.RETCLRBIT(rs.status, 7);

		// free parking bay
		rs.status = BitOps.RETSETBIT(rs.status, BitOps.HASBIT(v.road.state, 1) ? 1 : 0);
	}

	static void RoadVehDelete(Vehicle v)
	{
		Window.DeleteWindowById(Window.WC_VEHICLE_VIEW, v.index);
		Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);

		VehicleGui.RebuildVehicleLists();
		Window.InvalidateWindow(Window.WC_COMPANY, v.owner);

		if (v.tile.IsTileType(TileTypes.MP_STATION))
			ClearCrashedStation(v);

		v.BeginVehicleMove();
		v.EndVehicleMove();

		ClearSlot(v, v.road.slot);
		Vehicle.DeleteVehicle(v);
	}

	static int SetRoadVehPosition(Vehicle v, int x, int y)
	{
		int new_z, old_z;

		// need this hint so it returns the right z coordinate on bridges.
		Global._get_z_hint = v.z_pos;
		new_z = Landscape.GetSlopeZ(v.x_pos=x, v.y_pos=y);
		Global._get_z_hint = 0;

		old_z = v.z_pos;
		v.z_pos = new_z;

		v.VehiclePositionChanged();
		v.EndVehicleMove();
		return old_z;
	}

	static void RoadVehSetRandomDirection(Vehicle v)
	{
		int r = Hal.Random();
		v.direction = (v.direction+_turn_prob[r&3])&7;
		v.BeginVehicleMove();
		UpdateRoadVehDeltaXY(v);
		v.cur_image = GetRoadVehImage(v, v.direction);
		SetRoadVehPosition(v, v.getX_pos(), v.getY_pos());
	}

	static void RoadVehIsCrashed(Vehicle v)
	{
		v.road.crashed_ctr++;
		if (v.road.crashed_ctr == 2) {
			v.CreateEffectVehicleRel(4, 4, 8, Vehicle.EV_EXPLOSION_LARGE);
		} else if (v.road.crashed_ctr <= 45) {
			if ((v.tick_counter&7)==0)
				RoadVehSetRandomDirection(v);
		} else if (v.road.crashed_ctr >= 2220) {
			RoadVehDelete(v);
		}
	}

	static /*Vehicle*/ Object EnumCheckRoadVehCrashTrain(Vehicle v, Object o)
	{
		Vehicle u = (Vehicle) o;
		if (v.type != Vehicle.VEH_Train ||
				Math.abs(v.z_pos - u.z_pos) > 6 ||
				Math.abs(v.getX_pos() - u.getX_pos()) > 4 ||
				Math.abs(v.getY_pos() - u.getY_pos()) > 4)
			return null;
		return v;
	}

	static void RoadVehCrash(Vehicle v)
	{
		int pass;

		v.road.crashed_ctr++;
		v.setCrashed(true);

		Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);

		pass = 1;
		if (v.getCargo_type() == 0)
			pass += v.cargo_count;
		v.cargo_count = 0;
		Global.SetDParam(0, pass);

		NewsItem.AddNewsItem(
				(pass == 1) ?
						Str.STR_9031_ROAD_VEHICLE_CRASH_DRIVER : Str.STR_9032_ROAD_VEHICLE_CRASH_DIE,
						NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ACCIDENT, 0),
						v.index,
						0);

		Station.ModifyStationRatingAround(v.tile, v.owner, -160, 22);
		v.SndPlayVehicleFx(Snd.SND_12_EXPLOSION);
	}

	static void RoadVehCheckTrainCrash(Vehicle v)
	{
		TileIndex tile;

		if (v.road.isInTunnel())
			return;

		tile = v.tile;

		// Make sure it's a road/rail crossing
		if (!tile.IsTileType( TileTypes.MP_STREET) || !tile.IsLevelCrossing())
			return;

		if (Vehicle.VehicleFromPos(tile, v, RoadVehCmd::EnumCheckRoadVehCrashTrain) != null)
			RoadVehCrash(v);
	}

	static void HandleBrokenRoadVeh(Vehicle v)
	{
		if (v.breakdown_ctr != 1) {
			v.breakdown_ctr = 1;
			v.cur_speed = 0;

			if (v.breakdowns_since_last_service != 255)
				v.breakdowns_since_last_service++;

			Window.InvalidateWindow(Window.WC_VEHICLE_VIEW, v.index);
			Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);

			v.SndPlayVehicleFx((GameOptions._opt.landscape != Landscape.LT_CANDY) ?					
					Snd.SND_0F_VEHICLE_BREAKDOWN : Snd.SND_35_COMEDY_BREAKDOWN);

			if(!v.isHidden()) {
				Vehicle u = v.CreateEffectVehicleRel(4, 4, 5, Vehicle.EV_BREAKDOWN_SMOKE);
				if (u != null)
					u.special.unk0 = v.breakdown_delay * 2;
			}
		}

		if (0==(v.tick_counter & 1)) {
			if (0==--v.breakdown_delay) {
				v.breakdown_ctr = 0;
				Window.InvalidateWindow(Window.WC_VEHICLE_VIEW, v.index);
			}
		}
	}

	static void ProcessRoadVehOrder(Vehicle v)
	{
		final Order order;
		final Station st;

		if (v.getCurrent_order().type >= Order.OT_GOTO_DEPOT && v.getCurrent_order().type <= Order.OT_LEAVESTATION) {
			// Let a depot order in the orderlist interrupt.
			if (v.getCurrent_order().type != Order.OT_GOTO_DEPOT ||
					0==(v.getCurrent_order().flags & Order.OF_UNLOAD))
				return;
		}

		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT &&
				(v.getCurrent_order().flags & (Order.OF_PART_OF_ORDERS | Order.OF_SERVICE_IF_NEEDED)) == (Order.OF_PART_OF_ORDERS | Order.OF_SERVICE_IF_NEEDED) &&
				!v.VehicleNeedsService()) {
			v.cur_order_index++;
		}

		if (v.cur_order_index >= v.num_orders)
			v.cur_order_index = 0;

		order = v.GetVehicleOrder(v.cur_order_index);

		if (order == null) {
			v.getCurrent_order().type = Order.OT_NOTHING;
			v.getCurrent_order().flags = 0;
			v.dest_tile = null;
			return;
		}

		if (order.type    == v.getCurrent_order().type &&
				order.flags   == v.getCurrent_order().flags &&
				order.station == v.getCurrent_order().station)
			return;

		v.setCurrent_order(new Order( order ));

		v.dest_tile = null;

		if (order.type == Order.OT_GOTO_STATION) {
			if (order.station == v.last_station_visited)
				v.last_station_visited = Station.INVALID_STATION;
			st = Station.GetStation(order.station);

			{
				int mindist = Integer.MAX_VALUE;
				RoadStopType type;

				type = (v.getCargo_type() == AcceptedCargo.CT_PASSENGERS) ? RoadStopType.RS_BUS : RoadStopType.RS_TRUCK;
				List<RoadStop> rsl = RoadStop.GetPrimaryRoadStop(st, type);

				if (rsl == null || rsl.isEmpty()) {
					//There is no stop left at the station, so don't even TRY to go there
					v.cur_order_index++;
					v.InvalidateVehicleOrder();

					return;
				}

				//for (rsl = RoadStop.GetPrimaryRoadStop(st, type); rsl != null; rsl = rsl.next) 
				for(RoadStop rs : RoadStop.GetPrimaryRoadStop(st, type)) 
				{
					if (Map.DistanceManhattan(v.tile, rs.xy) < mindist) 
						v.dest_tile = rs.xy;
				}

			}
		} else if (order.type == Order.OT_GOTO_DEPOT) {
			v.dest_tile = Depot.GetDepot(order.station).xy;
		}

		v.InvalidateVehicleOrder();
	}

	static void HandleRoadVehLoading(Vehicle v)
	{
		if (v.getCurrent_order().type == Order.OT_NOTHING)
			return;

		if (v.getCurrent_order().type != Order.OT_DUMMY) {
			if (v.getCurrent_order().type != Order.OT_LOADING)
				return;

			if (--v.load_unload_time_rem > 0)
				return;

			if(v.getCurrent_order().hasFlag(Order.OF_FULL_LOAD) && v.CanFillVehicle()) {
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_ROADVEH_INC);
				if (0 != Economy.LoadUnloadVehicle(v)) {
					Window.InvalidateWindow(Window.WC_ROADVEH_LIST, v.owner);
					MarkRoadVehDirty(v);
				}
				return;
			}

			{
				Order b = new Order( v.getCurrent_order() );
				v.getCurrent_order().type = Order.OT_LEAVESTATION;
				v.getCurrent_order().flags = 0;
				if (0==(b.flags & Order.OF_NON_STOP))
					return;
			}
		}

		v.cur_order_index++;
		v.InvalidateVehicleOrder();
	}

	static void StartRoadVehSound(Vehicle v)
	{

		/*SoundFx*/ int s = Engine.RoadVehInfo(v.engine_type.id).sfx; //trainSfx;
		
		if (s == Snd.SND_19_BUS_START_PULL_AWAY.ordinal() && (v.tick_counter & 3) == 0)
			s = Snd.SND_1A_BUS_START_PULL_AWAY_WITH_HORN.ordinal();
		
		//v.SndPlayVehicleFx(s);
		ShortSounds.playMotorSound();

	}


	static Object EnumCheckRoadVehClose(Vehicle v, Object o)
	{
		RoadVehFindData rvf = (RoadVehFindData) o;

		int x_diff = v.getX_pos() - rvf.x;
		int y_diff = v.getY_pos() - rvf.y;

		if (rvf.veh == v ||
				v.type != Vehicle.VEH_Road ||
				v.road.isInDepot() ||
				Math.abs(v.z_pos - rvf.veh.z_pos) > 6 ||
				v.direction != rvf.dir ||
				(_dists[v.direction] < 0 && (x_diff <= _dists[v.direction] || x_diff > 0)) ||
				(_dists[v.direction] > 0 && (x_diff >= _dists[v.direction] || x_diff < 0)) ||
				(_dists[v.direction+8] < 0 && (y_diff <= _dists[v.direction+8] || y_diff > 0)) ||
				(_dists[v.direction+8] > 0 && (y_diff >= _dists[v.direction+8] || y_diff < 0)))
			return null;

		return v;
	}

	static Vehicle RoadVehFindCloseTo(Vehicle v, int x, int y, int dir)
	{
		RoadVehFindData rvf = new RoadVehFindData();
		Vehicle u;

		if (v.road.reverse_ctr != 0)
			return null;

		rvf.x = x;
		rvf.y = y;
		rvf.dir = dir;
		rvf.veh = v;
		u = (Vehicle) Vehicle.VehicleFromPos(TileIndex.TileVirtXY(x, y), rvf, RoadVehCmd::EnumCheckRoadVehClose);

		// This code protects a roadvehicle from being blocked for ever
		//  If more than 1480 / 74 days a road vehicle is blocked, it will
		//  drive just through it. The ultimate backup-code of TTD.
		// It can be disabled.
		if (u == null) {
			v.road.unk2 = 0;
			return null;
		}

		if (++v.road.unk2 > 1480)
			return null;

		return u;
	}

	static void RoadVehArrivesAt(final Vehicle  v, Station  st)
	{
		if (v.getCargo_type() == AcceptedCargo.CT_PASSENGERS) {
			/* Check if station was ever visited before */
			if (0==(st.had_vehicle_of_type & Station.HVOT_BUS)) {
				int flags;

				st.had_vehicle_of_type |= Station.HVOT_BUS;
				Global.SetDParam(0, st.index);
				flags = v.owner.isLocalPlayer() ? NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ARRIVAL_PLAYER, 0) : NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ARRIVAL_OTHER, 0);
				NewsItem.AddNewsItem(
						Str.STR_902F_CITIZENS_CELEBRATE_FIRST,
						flags,
						v.index,
						0);
			}
		} else {
			/* Check if station was ever visited before */
			if (0==(st.had_vehicle_of_type & Station.HVOT_TRUCK)) {
				int flags;

				st.had_vehicle_of_type |= Station.HVOT_TRUCK;
				Global.SetDParam(0, st.index);
				flags = v.owner.isLocalPlayer() ? NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ARRIVAL_PLAYER, 0) : NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ARRIVAL_OTHER, 0);
				NewsItem.AddNewsItem(
						Str.STR_9030_CITIZENS_CELEBRATE_FIRST,
						flags,
						v.index,
						0);
			}
		}
	}

	static boolean RoadVehAccelerate(Vehicle v)
	{
		int spd = v.cur_speed + 1 + ((v.road.overtaking != 0)?1:0);

		// Clamp
		spd = Math.min(spd, v.getMax_speed());

		//updates statusbar only if speed have changed to save CPU time
		if (spd != v.cur_speed) {
			v.cur_speed = spd;
			if (Global._patches.vehicle_speed)
				Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
		}

		// Decrease somewhat when turning
		if (0==(v.direction&1))
			spd = spd * 3 >> 2;

			if (spd == 0)
				return false;

			if ((byte)++spd == 0)
				return true;

			int t = 0xff & v.progress;
			v.progress = BitOps.uint16Wrap( t - spd );

			return (t < v.progress);
	}

	static int RoadVehGetNewDirection(Vehicle v, int x, int y)
	{

		x = x - v.getX_pos() + 1;
		y = y - v.getY_pos() + 1;

		if (x > 2 || y > 2 || x < 0 || y < 0)
			return v.direction;
		return _roadveh_new_dir[y*4+x];
	}

	static int RoadVehGetSlidingDirection(Vehicle v, int x, int y)
	{
		int b = RoadVehGetNewDirection(v,x,y);
		int d = v.direction;
		if (b == d) return d;
		d = (d+1)&7;
		if (b==d) return d;
		d = (d-2)&7;
		if (b==d) return d;
		if (b==((d-1)&7)) return d;
		if (b==((d-2)&7)) return d;
		return (d+2)&7;
	}



	static Object EnumFindVehToOvertake(Vehicle v, Object o)
	{
		OvertakeData od = (OvertakeData) o;

		if (!v.tile.equals(od.tile) || v.type != Vehicle.VEH_Road || v == od.u || v == od.v)
			return null;
		return v;
	}

	static boolean FindRoadVehToOvertake(OvertakeData od)
	{
		int bits;

		bits = Landscape.GetTileTrackStatus(od.tile, TransportType.Road)&0x3F;

		if (0==(od.tilebits & bits) || 0!=(bits&0x3C) || 0!=(bits & 0x3F3F0000))
			return true;
		return Vehicle.VehicleFromPos(od.tile, od, RoadVehCmd::EnumFindVehToOvertake) != null;
	}

	static void RoadVehCheckOvertake(Vehicle v, Vehicle u)
	{
		OvertakeData od = new OvertakeData();
		int tt;

		od.v = v;
		od.u = u;

		if (u.getMax_speed() >= v.getMax_speed() && !u.isStopped() &&
				u.cur_speed != 0)
			return;

		if (v.direction != u.direction || 0==(v.direction&1))
			return;

		if (v.road.state >= 32 || (v.road.state&7) > 1 )
			return;

		tt = Landscape.GetTileTrackStatus(v.tile, TransportType.Road) & 0x3F;
		if ((tt & 3) == 0)
			return;
		if ((tt & 0x3C) != 0)
			return;

		if (tt == 3) {
			tt = (0!=(v.direction&2))?2:1;
		}
		od.tilebits = tt;

		od.tile = v.tile;
		if (FindRoadVehToOvertake(od))
			return;

		od.tile = v.tile.iadd( TileIndex.TileOffsByDir(v.direction >> 1) );
		if (FindRoadVehToOvertake(od))
			return;

		v.road.overtaking = 0x10;
		if(od.u.cur_speed == 0 || od.u.isStopped()) {
			v.road.overtaking_ctr = 0x11;
		} else {
			//		if (FindRoadVehToOvertake(&od))
			//			return;
			v.road.overtaking_ctr = 0;
		}
	}

	static void RoadZPosAffectSpeed(Vehicle v, int old_z)
	{
		if (old_z == v.z_pos)
			return;

		if (old_z < v.z_pos) {
			v.cur_speed = v.cur_speed * 232 >> 8;
		} else {
			int spd = v.cur_speed + 2;
			if (spd <= v.getMax_speed())
				v.cur_speed = spd;
		}
	}

	static int PickRandomBit(int bits)
	{
		int num = 0;
		int b = bits;
		int i;

		do {
			if(0!= (b & 1) )
				num++;
		} while ((b >>= 1) != 0);

		num = Hal.RandomRange(num);

		for(i=0; !((bits & 1)!=0 && (--num < 0)); bits>>=1,i++)
			;
		return i;
	}


	static boolean EnumRoadTrackFindDist(TileIndex tile, Object o, int track, int length, int [] state)
	{
		FindRoadToChooseData frd = (FindRoadToChooseData) o;

		int dist = Map.DistanceManhattan(tile, frd.dest);
		if (dist <= frd.mindist) {
			if (dist != frd.mindist || length < frd.maxtracklen) {
				frd.maxtracklen = length;
			}
			frd.mindist = dist;
		}
		return false;
	}

	private static int return_track(int best_track, int signal) 
	{
		if (BitOps.HASBIT(signal, best_track))
			return -1;

		return best_track;

	}

	// Returns direction to choose
	// or -1 if the direction is currently blocked
	static int RoadFindPathToDest(Vehicle v, TileIndex tile, int enterdir)
	{

		int signal;
		int bitmask;
		TileIndex desttile;
		FindRoadToChooseData frd = new FindRoadToChooseData();
		int best_track;
		int best_dist, best_maxlen;
		int i;
		int m5;

		{
			int r;
			r = Landscape.GetTileTrackStatus(tile, TransportType.Road);
			signal  = BitOps.GB(r, 16, 16);
			bitmask = BitOps.GB(r,  0, 16);
		}

		if (tile.IsTileType( TileTypes.MP_STREET)) {
			if (BitOps.GB(tile.getMap().m5, 4, 4) == 2 && tile.IsTileOwner(v.owner)) {
				/* Road depot */
				bitmask |= _road_veh_fp_ax_or[BitOps.GB(tile.getMap().m5, 0, 2)];
			}
		} else if (tile.IsTileType( TileTypes.MP_STATION)) {
			if (tile.IsTileOwner(Owner.OWNER_NONE) || tile.IsTileOwner(v.owner)) {
				/* Our station */
				final Station  st = Station.GetStation(tile.getMap().m2);
				int val = tile.getMap().m5;
				// TODO why .get(0) is ok?
				if (v.getCargo_type() != AcceptedCargo.CT_PASSENGERS) {
					if (BitOps.IS_INT_INSIDE(val, 0x43, 0x47) && (Global._patches.roadveh_queue || 0!=(st.truck_stops.get(0).status&3)))
						bitmask |= _road_veh_fp_ax_or[(val-0x43)&3];
				} else {
					if (BitOps.IS_INT_INSIDE(val, 0x47, 0x4B) && (Global._patches.roadveh_queue || 0!=(st.bus_stops.get(0).status&3)))
						bitmask |= _road_veh_fp_ax_or[(val-0x47)&3];
				}
			}
		}
		/* The above lookups should be moved to GetTileTrackStatus in the
		 * future, but that requires more changes to the pathfinder and other
		 * stuff, probably even more arguments to GTTS.
		 */

		/* remove unreachable tracks */
		bitmask &= _road_veh_fp_ax_and[enterdir];
		if (bitmask == 0) {
			/* No reachable tracks, so we'll reverse */
			return return_track(_road_reverse_table[enterdir],signal);
		}

		if (v.road.reverse_ctr != 0) {
			/* What happens here?? */
			v.road.reverse_ctr = 0;
			if (!v.tile.equals(tile)) {
				return return_track(_road_reverse_table[enterdir],signal);
			}
		}

		desttile = v.dest_tile;
		if (desttile == null) {
			// Pick a random track
			return return_track(PickRandomBit(bitmask),signal);
		}

		// Only one track to choose between?
		if (0==(BitOps.KillFirstBit2x64(bitmask))) {
			return return_track(BitOps.FindFirstBit2x64(bitmask),signal);
		}

		if (Global._patches.new_pathfinding_all) {
			NPFFindStationOrTileData fstd = new NPFFindStationOrTileData();
			NPFFoundTargetData ftd;
			byte trackdir;

			Npf.NPFFillWithOrderData(fstd, v);
			trackdir = (byte) Rail.DiagdirToDiagTrackdir(enterdir);
			//debug("Finding path. Enterdir: %d, Trackdir: %d", enterdir, trackdir);

			ftd = Npf.NPFRouteToStationOrTile(tile.isub( TileIndex.TileOffsByDir(enterdir) ), trackdir, fstd, TransportType.Road, v.owner, Rail.INVALID_RAILTYPE, Pbs.PBS_MODE_NONE);
			if (ftd.best_trackdir == 0xff) {
				/* We are already at our target. Just do something */
				//TODO: maybe display error?
				//TODO: go straight ahead if possible?
				return return_track(BitOps.FindFirstBit2x64(bitmask),signal);
			} else {
				/* If ftd.best_bird_dist is 0, we found our target and ftd.best_trackdir contains
			the direction we need to take to get there, if ftd.best_bird_dist is not 0,
			we did not find our target, but ftd.best_trackdir contains the direction leading
			to the tile closest to our target. */
				return return_track(ftd.best_trackdir,signal);
			}
		} else {
			if (desttile.IsTileType(TileTypes.MP_STREET)) {
				m5 = desttile.M().m5;
				if ((m5&0xF0) == 0x20)
				{
					/* We are heading for a Depot */
					//goto do_it;
					/* When we are heading for a depot or station, we just
					 * pretend we are heading for the tile in front, we'll
					 * see from there */
					desttile = desttile.iadd(TileIndex.TileOffsByDir(m5 & 3));
					if( desttile.equals(tile) && 0 != (bitmask&_road_pf_table_3[m5&3]) ) {
						/* If we are already in front of the
						 * station/depot and we can get in from here,
						 * we enter */
						return return_track(BitOps.FindFirstBit2x64(bitmask&_road_pf_table_3[m5&3]),signal);
					}
				}
			} else if (desttile.IsTileType(TileTypes.MP_STATION)) {
				m5 = desttile.M().m5;
				if (BitOps.IS_INT_INSIDE(m5, 0x43, 0x4B)) {
					/* We are heading for a station */
					m5 -= 0x43;
					//do_it:;
					/* When we are heading for a depot or station, we just
					 * pretend we are heading for the tile in front, we'll
					 * see from there */
					desttile = desttile.iadd( TileIndex.TileOffsByDir(m5 & 3) );
					if( desttile.equals(tile) && 0 != (bitmask&_road_pf_table_3[m5&3]) ) {
						/* If we are already in front of the
						 * station/depot and we can get in from here,
						 * we enter */
						return return_track(BitOps.FindFirstBit2x64(bitmask&_road_pf_table_3[m5&3]),signal);
					}
				}
			}
			// do pathfind
			frd.dest = desttile;

			best_track = -1;
			best_dist = -1;
			best_maxlen = -1;
			i = 0;
			do {
				if(0!= (bitmask & 1)) {
					if (best_track == -1) best_track = i; // in case we don't find the path, just pick a track
					frd.maxtracklen = -1;
					frd.mindist = -1;
					Pathfind.FollowTrack(tile, TransportType.Road, 0x3000, _road_pf_directions[i], RoadVehCmd::EnumRoadTrackFindDist, null, frd);

					if (frd.mindist < best_dist || (frd.mindist==best_dist && frd.maxtracklen < best_maxlen)) {
						best_dist = frd.mindist;
						best_maxlen = frd.maxtracklen;
						best_track = i;
					}
				}
				++i;
			} while ((bitmask>>=1) != 0);
		}

		//found_best_track:;

		if (BitOps.HASBIT(signal, best_track))
			return -1;

		return best_track;
	}


	static int RoadFindPathToStation(final Vehicle v, TileIndex tile)
	{
		NPFFindStationOrTileData fstd = new NPFFindStationOrTileData();
		int trackdir = v.GetVehicleTrackdir();
		assert(trackdir != 0xFF);

		fstd.dest_coords = tile;
		fstd.station_index = -1;	// indicates that the destination is a tile, not a station

		return Npf.NPFRouteToStationOrTile(v.tile, trackdir, fstd, TransportType.Road, v.owner, Rail.INVALID_RAILTYPE, Pbs.PBS_MODE_NONE).best_path_dist;
	}

	/*
class RoadDriveEntry {
	byte x,y;
} RoadDriveEntry;
	 */



	static void RoadVehController(Vehicle v)
	{
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();
		int new_dir, old_dir;
		//RoadDriveEntry rd;
		Point rd;
		int x,y;
		Station st;
		int r1;
		Vehicle u;

		// decrease counters
		v.tick_counter++;
		if (v.road.reverse_ctr != 0)
			v.road.reverse_ctr--;

		// handle crashed
		if (v.road.crashed_ctr != 0) {
			RoadVehIsCrashed(v);
			return;
		}

		RoadVehCheckTrainCrash(v);

		// road vehicle has broken down?
		if (v.breakdown_ctr != 0) {
			if (v.breakdown_ctr <= 2) {
				HandleBrokenRoadVeh(v);
				return;
			}
			v.breakdown_ctr--;
		}

		// exit if vehicle is stopped
		if(v.isStopped())
			return;

		ProcessRoadVehOrder(v);
		HandleRoadVehLoading(v);

		if (v.getCurrent_order().type == Order.OT_LOADING)
			return;

		if (v.road.isInDepot()) {
			int dir;
			//final RoadDriveEntry rdp;
			final Point[] rdp;
			byte rd2;

			v.cur_speed = 0;

			dir = BitOps.GB(v.tile.M().m5, 0, 2);
			v.direction = dir*2+1;

			rd2 = _roadveh_data_2[dir];
			rdp = _road_drive_data[(GameOptions._opt.road_side<<4) + rd2];

			x = v.tile.TileX() * 16 + (rdp[6].x & 0xF);
			y = v.tile.TileY() * 16 + (rdp[6].y & 0xF);

			if (RoadVehFindCloseTo(v,x,y,v.direction) != null)
				return;

			v.VehicleServiceInDepot();

			StartRoadVehSound(v);

			v.BeginVehicleMove();

			v.setHidden(false);
			v.road.state = rd2;
			v.road.frame = 6;

			v.cur_image = GetRoadVehImage(v, v.direction);
			UpdateRoadVehDeltaXY(v);
			SetRoadVehPosition(v,x,y);

			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
			return;
		}

		if (!RoadVehAccelerate(v))
			return;

		if (v.road.overtaking != 0)  {
			if (++v.road.overtaking_ctr >= 35)
				v.road.overtaking = 0;
		}

		v.BeginVehicleMove();

		if (v.road.isInTunnel()) {
			v.GetNewVehiclePos(gp);

			if (RoadVehFindCloseTo(v, gp.x, gp.y, v.direction) != null) {
				v.cur_speed = 0;
				return;
			}

			if (gp.new_tile.IsTileType(TileTypes.MP_TUNNELBRIDGE) &&
					BitOps.GB(gp.new_tile.M().m5, 4, 4) == 0 &&
					(v.VehicleEnterTile( gp.new_tile, gp.x, gp.y)&4) != 0 ) {

				//new_dir = RoadGetNewDirection(v, gp.x, gp.y)
				v.cur_image = GetRoadVehImage(v, v.direction);
				UpdateRoadVehDeltaXY(v);
				SetRoadVehPosition(v,gp.x,gp.y);
				return;
			}

			v.x_pos = gp.x;
			v.y_pos = gp.y;
			v.VehiclePositionChanged();
			return;
		}

		rd = _road_drive_data[(v.road.state + (GameOptions._opt.road_side<<4)) ^ v.road.overtaking][v.road.frame+1];

		// switch to another tile
		if(0 !=  (rd.x & 0x80) ) 
		{
			TileIndex tile = v.tile.iadd( TileIndex.TileOffsByDir(rd.x & 3) );
			int dir = RoadFindPathToDest(v, tile, rd.x&3);
			int tmp;
			int r;
			int newdir;
			//final RoadDriveEntry rdp;
			Point[] rdp;

			if (dir == -1) {
				v.cur_speed = 0;
				return;
			}

			boolean do_goto; // = false;
			//again:
			do { // goto target
				do_goto = false;

				if ((dir & 7) >= 6) {
					/* Turning around */
					tile = v.tile;
				}

				tmp = (dir+(GameOptions._opt.road_side<<4))^v.road.overtaking;
				rdp = _road_drive_data[tmp];

				tmp &= ~0x10;

				x = tile.TileX() * 16 + rdp[0].x;
				y = tile.TileY() * 16 + rdp[0].y;

				newdir=RoadVehGetSlidingDirection(v, x, y);
				if (RoadVehFindCloseTo(v, x, y, newdir) != null)
					return;

				r = v.VehicleEnterTile( tile, x, y);
				if(0 != (r & 8)) {
					if (!tile.IsTileType( TileTypes.MP_TUNNELBRIDGE)) {
						v.cur_speed = 0;
						return;
					}
					dir = _road_reverse_table[rd.x&3];
					//goto again;
					do_goto = true;
					//continue;
				}
			} while(do_goto); // goto target do end

			if (BitOps.IS_INT_INSIDE(v.road.state, 0x20, 0x30) && v.tile.IsTileType(TileTypes.MP_STATION)) {
				if ((tmp&7) >= 6) { v.cur_speed = 0; return; }
				if (BitOps.IS_INT_INSIDE(v.tile.M().m5, 0x43, 0x4B)) {
					RoadStop rs = RoadStop.GetRoadStopByTile(v.tile, RoadStop.GetRoadStopType(v.tile));
					//byte *b = &rs.status;

					//we have reached a loading bay, mark it as used
					//and clear the usage bit (0x80) of the stop
					//*b = (*b | ((v.road.state&2)?2:1)) & ~0x80;
					rs.status |= ((v.road.state&2)!=0?2:1);
					rs.status &= ~0x80;
				}
			}

			if (0==(r & 4)) {
				v.tile = tile;
				v.road.state = tmp;
				v.road.frame = 0;
			}
			if (newdir != v.direction) {
				v.direction = newdir;
				v.cur_speed -= v.cur_speed >> 2;
			}

			v.cur_image = GetRoadVehImage(v, newdir);
			UpdateRoadVehDeltaXY(v);
			RoadZPosAffectSpeed(v, SetRoadVehPosition(v, x, y));
			return;
		}

		if(0 != (rd.x & 0x40)) {
			int dir = RoadFindPathToDest(v, v.tile,	rd.x&3);
			int r;
			int tmp;
			int newdir;
			final RoadDriveEntry [] rdp;

			if (dir == -1) {
				v.cur_speed = 0;
				return;
			}

			tmp = (GameOptions._opt.road_side<<4) + dir;
			rdp = _road_drive_data[tmp];

			x = v.tile.TileX() * 16 + rdp[1].x;
			y = v.tile.TileY() * 16 + rdp[1].y;

			if (null != RoadVehFindCloseTo(v, x, y, newdir=RoadVehGetSlidingDirection(v, x, y)))
				return;

			r = v.VehicleEnterTile( v.tile, x, y);
			if(0!= (r & 8)) {
				v.cur_speed = 0;
				return;
			}

			v.road.state = tmp & ~16;
			v.road.frame = 1;

			if (newdir != v.direction) {
				v.direction = newdir;
				v.cur_speed -= v.cur_speed >> 2;
			}

			v.cur_image = GetRoadVehImage(v, newdir);
			UpdateRoadVehDeltaXY(v);
			RoadZPosAffectSpeed(v, SetRoadVehPosition(v, x, y));
			return;
		}

		x = (v.getX_pos()&~15)+(rd.x&15);
		y = (v.getY_pos()&~15)+(rd.y&15);

		new_dir = RoadVehGetSlidingDirection(v, x, y);

		if (!BitOps.IS_INT_INSIDE(v.road.state, 0x20, 0x30) && (u=RoadVehFindCloseTo(v, x, y, new_dir)) != null) {
			if (v.road.overtaking == 0)
				RoadVehCheckOvertake(v, u);
			return;
		}

		old_dir = v.direction;
		if (new_dir != old_dir) {
			v.direction = new_dir;
			v.cur_speed -= (v.cur_speed >> 2);
			if (old_dir != v.road.state) {
				v.cur_image = GetRoadVehImage(v, new_dir);
				UpdateRoadVehDeltaXY(v);
				SetRoadVehPosition(v, v.getX_pos(), v.getY_pos());
				return;
			}
		}

		if (v.road.state >= 0x20 &&
				_road_veh_data_1[v.road.state - 0x20 + (GameOptions._opt.road_side<<4)] == v.road.frame) {
			RoadStop rs = RoadStop.GetRoadStopByTile(v.tile, RoadStop.GetRoadStopType(v.tile));
			//byte *b = &rs.status;

			st = Station.GetStation(v.tile.M().m2);

			if (v.getCurrent_order().type != Order.OT_LEAVESTATION &&
					v.getCurrent_order().type != Order.OT_GOTO_DEPOT) {
				Order old_order;

				//*b &= ~0x80;
				rs.status &= ~0x80;


				v.last_station_visited = v.tile.M().m2;

				RoadVehArrivesAt(v, st);

				old_order = new Order( v.getCurrent_order() );
				v.getCurrent_order().type = Order.OT_LOADING;
				v.getCurrent_order().flags = 0;

				if (old_order.type == Order.OT_GOTO_STATION &&
						v.getCurrent_order().station == v.last_station_visited) {
					v.getCurrent_order().flags =
							(old_order.flags & (Order.OF_FULL_LOAD | Order.OF_UNLOAD | Order.OF_TRANSFER)) | Order.OF_NON_STOP;
				}

				Player.SET_EXPENSES_TYPE(Player.EXPENSES_ROADVEH_INC);
				if (0!=Economy.LoadUnloadVehicle(v)) {
					Window.InvalidateWindow(Window.WC_ROADVEH_LIST, v.owner);
					MarkRoadVehDirty(v);
				}
				Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
				return;
			}

			if (v.getCurrent_order().type != Order.OT_GOTO_DEPOT) {
				if(0 != (rs.status & 0x80)) {
					v.cur_speed = 0;
					return;
				}
				v.getCurrent_order().type = Order.OT_NOTHING;
				v.getCurrent_order().flags = 0;
			}
			rs.status |= 0x80;

			if (rs == v.road.slot) {
				//we have arrived at the correct station
				ClearSlot(v, rs);
			} else if (v.road.slot != null) {
				//we have arrived at the wrong station
				//XXX The question is .. what to do? Actually we shouldn't be here
				//but I guess we need to clear the slot
				Global.DEBUG_ms( 1, "Multistop: Wrong station, force a slot clearing. Vehicle %d at %s, should go to %s of station %d (%s), destination %s", 
						v.unitnumber.id, v.tile.toString(), v.road.slot.xy.toString(), st.index, st.getXy().toString(), v.dest_tile.toString());
				ClearSlot(v, v.road.slot);
			}

			StartRoadVehSound(v);
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
		}

		ViewPort.SetRedErrorSquare(v.tile);
		r1 = v.VehicleEnterTile( v.tile, x, y);
		if(0!= (r1 & 8)) {
			v.cur_speed = 0;
			return;
		}

		if ((r1 & 4) == 0) {
			v.road.frame++;
		}

		v.cur_image = GetRoadVehImage(v, v.direction);
		UpdateRoadVehDeltaXY(v);
		RoadZPosAffectSpeed(v, SetRoadVehPosition(v, x, y));
	}

	static void RoadVehEnterDepot(Vehicle v)
	{
		v.road.setInDepot();
		v.setHidden(true);

		Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);

		v.VehicleServiceInDepot();

		v.TriggerVehicle(Engine.VEHICLE_TRIGGER_DEPOT);

		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT) 
		{
			Window.InvalidateWindow(Window.WC_VEHICLE_VIEW, v.index);

			Order t = new Order( v.getCurrent_order() );
			v.getCurrent_order().type = Order.OT_DUMMY;
			v.getCurrent_order().flags = 0;

			// Part of the orderlist?
			if (BitOps.HASBIT(t.flags, Order.OFB_PART_OF_ORDERS)) {
				v.cur_order_index++;
			} else if (BitOps.HASBIT(t.flags, Order.OFB_HALT_IN_DEPOT)) {
				v.setStopped(true);
				if (v.owner.isLocalPlayer()) 
				{
					Global.SetDParam(0, v.unitnumber.id);
					NewsItem.AddNewsItem(
							Str.STR_9016_ROAD_VEHICLE_IS_WAITING,
							NewsItem.NEWS_FLAGS(NewsItem.NM_SMALL, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ADVICE, 0),
							v.index,
							0);
				}
			}
		}

		Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
		Window.InvalidateWindowClasses(Window.WC_ROADVEH_LIST);
	}

	static void AgeRoadVehCargo(Vehicle v)
	{
		if (Global._age_cargo_skip_counter != 0)
			return;
		if (v.cargo_days != 255)
			v.cargo_days++;
	}

	static void RoadVeh_Tick(Vehicle v)
	{
		AgeRoadVehCargo(v);
		RoadVehController(v);
	}

	static void CheckIfRoadVehNeedsService(Vehicle v)
	{
		Depot depot;

		if (Global._patches.servint_roadveh == 0)
			return;

		if (!v.VehicleNeedsService())
			return;

		if(v.isStopped())
			return;

		if (Global._patches.gotodepot.get() && v.VehicleHasDepotOrders())
			return;

		// Don't interfere with a depot visit scheduled by the user, or a
		// depot visit by the order list.
		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT &&
				(v.getCurrent_order().flags & (Order.OF_HALT_IN_DEPOT | Order.OF_PART_OF_ORDERS)) != 0)
			return;

		//If we already got a slot at a stop, use that FIRST, and go to a depot later
		if (v.road.slot != null)
			return;

		depot = FindClosestRoadDepot(v);

		if (depot == null || Map.DistanceManhattan(v.tile, depot.xy) > 12) {
			if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT) {
				v.getCurrent_order().type = Order.OT_DUMMY;
				v.getCurrent_order().flags = 0;
				Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
			}
			return;
		}

		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT &&
				0 != (v.getCurrent_order().flags & Order.OF_NON_STOP) &&
				!BitOps.CHANCE16(1,20))
			return;

		v.getCurrent_order().type = Order.OT_GOTO_DEPOT;
		v.getCurrent_order().flags = Order.OF_NON_STOP;
		v.getCurrent_order().station = depot.index;
		v.dest_tile = depot.xy;
		Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
	}

	static class dist_compare implements Comparator<Integer> {
		public int compare(Integer a, Integer b) {
			return a - b;
		}
	}

	public static void OnNewDay_RoadVeh(Vehicle v)
	{
		int cost;
		Station st;

		if ((++v.day_counter & 7) == 0)
			v.DecreaseVehicleValue();

		if (v.road.unk2 == 0)
			v.CheckVehicleBreakdown();

		v.AgeVehicle();
		CheckIfRoadVehNeedsService(v);

		Order.CheckOrders(v.index, Order.OC_INIT);

		/* update destination */
		if ( v.getCurrent_order() != null
				&& v.getCurrent_order().type == Order.OT_GOTO_STATION 
				&& !v.isCrashed()) 
		{
			RoadStopType type = (v.getCargo_type() == AcceptedCargo.CT_PASSENGERS) ? RoadStopType.RS_BUS : RoadStopType.RS_TRUCK;

			st = Station.GetStation(v.getCurrent_order().station);

			//Current slot has expired
			if ( (v.road.slot_age++ <= 0) && (v.road.slot != null))
				ClearSlot(v, v.road.slot);

			//We do not have a slot, so make one
			if (v.road.slot == null) {
				List<RoadStop> rsl = RoadStop.GetPrimaryRoadStop(st, type);
				RoadStop first_stop = rsl.get(0);
				RoadStop best_stop = null;
				int mindist = 12, dist; // 12 is threshold distance.

				//first we need to find out how far our stations are away.
				Global.DEBUG_ms( 2, "Multistop: Attempting to obtain a slot for vehicle %d at station %d (%d.%d)", v.unitnumber.id, st.index, st.getXy().getX(), st.getXy().getY() );

				for( RoadStop rs : rsl )
				{
					// Only consider those with at least a free slot.
					if (!(rs.slot[0] == Station.INVALID_SLOT || rs.slot[1] == Station.INVALID_SLOT))
						continue;

					// Previously the NPF pathfinder was used here even if NPF is OFF.. WTF?
					//assert(Station.NUM_SLOTS == 2);
					dist = Map.DistanceManhattan(v.tile, rs.xy);

					// Check if the station is located BEHIND the vehicle..
					// In that case, add penalty.
					switch(v.direction) {
					case 1: // going north east,x position decreasing
						if (v.getX_pos() <= rs.xy.TileX() * 16 + 15)
							dist += 6;
						break;
					case 3: // Going south east, y position increasing
						if (v.getY_pos() >= rs.xy.TileY() * 16)
							dist += 6;
						break;
					case 5: // Going south west, x position increasing
						if (v.getX_pos() >= rs.xy.TileX() * 16)
							dist += 6;
						break;
					case 7: // Going north west, y position decrasing.
						if (v.getY_pos() <= rs.xy.TileY() * 16 + 15)
							dist += 6;
						break;
					}

					// Remember the one with the shortest distance
					if (dist < mindist) {
						mindist = dist;
						best_stop = rs;
					}
					Global.DEBUG_ms( 3, "Multistop: Distance to stop at %d.%d is %d", rs.xy.getX(), rs.xy.getY(), dist);
				}

				// best_stop now contains the best stop we found.
				if (best_stop!=null) {
					int slot;
					// Find a free slot in this stop. We know that at least one is free.
					assert(best_stop.slot[0] == Station.INVALID_SLOT || best_stop.slot[1] == Station.INVALID_SLOT);
					slot = (best_stop.slot[0] == Station.INVALID_SLOT) ? 0 : 1;
					best_stop.slot[slot] = v.index;
					v.road.slot = best_stop;
					v.dest_tile = best_stop.xy;
					v.road.slot_age = -5;
					v.road.slotindex = slot;
					Global.DEBUG_ms( 1, "Multistop: Slot %d at %s assigned to vehicle %d (%s)", slot, best_stop.xy.toString(), v.unitnumber.id, v.tile.toString());
				} else if (first_stop!=null) {
					//now we couldn't assign a slot for one reason or another.
					//so we just go towards the first station
					Global.DEBUG_ms( 1, "Multistop: No free slot found for vehicle %d, going to default station", v.unitnumber.id);
					v.dest_tile = first_stop.xy;
				}
			}
		}

		if(v.isStopped())
			return;

		cost = (int) (Engine.RoadVehInfo(v.getEngine_type().id).running_cost * Global._price.roadveh_running / 364);

		v.profit_this_year -= cost >> 8;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_ROADVEH_RUN);
		Player.SubtractMoneyFromPlayerFract(v.owner, cost);

		Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
		Window.InvalidateWindowClasses(Window.WC_ROADVEH_LIST);
	}


	public static void RoadVehiclesYearlyLoop()
	{
		Vehicle.forEach( (v) ->
		{
			if (v.type == Vehicle.VEH_Road) {
				v.profit_last_year = v.profit_this_year;
				v.profit_this_year = 0;
				Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
			}
		});
	}

}
