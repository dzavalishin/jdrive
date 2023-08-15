package com.dzavalishin.game;
import com.dzavalishin.ids.CargoID;
import com.dzavalishin.ids.EngineID;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.UnitID;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.struct.GetNewVehiclePosResult;
import com.dzavalishin.struct.NPFFindStationOrTileData;
import com.dzavalishin.struct.TileIndexDiffC;
import com.dzavalishin.tables.EngineTables2;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.xui.EngineGui;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.VehicleGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

public class Ship {

	static final int _ship_sprites[] = {0x0E5D, 0x0E55, 0x0E65, 0x0E6D};
	static final byte _ship_sometracks[] = {0x19, 0x16, 0x25, 0x2A};

	static public final int STATUS_BAR = Vehicle.STATUS_BAR;

	static int GetTileShipTrackStatus(TileIndex tile)
	{
		int r = tile.GetTileTrackStatus(TransportType.Water);
		return  (r | r >>> 8);
	}

	public static void DrawShipEngine(int x, int y, /*EngineID*/ int engine, int image_ormod)
	{
		int spritenum = EngineGui.ShipVehInfo(engine).image_index;

		if (Sprite.is_custom_sprite(spritenum)) {
			int sprite = Engine.GetCustomVehicleIcon(engine, 6);

			if (sprite != 0) {
				Gfx.DrawSprite(sprite | image_ormod, x, y);
				return;
			}
			spritenum = EngineTables2.orig_ship_vehicle_info[engine - Global.SHIP_ENGINES_INDEX].image_index;
		}/**/
		Gfx.DrawSprite((6 + _ship_sprites[spritenum]) | image_ormod, x, y);
	}

	public static int GetShipImage(final Vehicle v, int direction)
	{
		int spritenum = v.spritenum;


		if (Sprite.is_custom_sprite(spritenum)) {
			int sprite = Engine.GetCustomVehicleSprite(v, direction);

			if (sprite != 0) return sprite;
			spritenum = EngineTables2.orig_ship_vehicle_info[v.engine_type.id - Global.SHIP_ENGINES_INDEX].image_index;
		} /**/
		return _ship_sprites[spritenum] + direction;
	}

	static Depot  FindClosestShipDepot(final Vehicle  v)
	{
		Depot [] best_depot = { null };
		//int dist;
		int [] best_dist = { -1 };
		//TileIndex tile;
		TileIndex tile2 = v.tile;

		if (Global._patches.new_pathfinding_all) 
		{
			NPFFoundTargetData ftd;
			int trackdir = v.GetVehicleTrackdir();
			ftd = Npf.NPFRouteToDepotTrialError(v.tile, trackdir, TransportType.Water, v.owner, Rail.INVALID_RAILTYPE);
			if (ftd.best_bird_dist == 0) {
				best_depot[0] = Depot.GetDepotByTile(ftd.node.tile); /* Found target */
			} else {
				best_depot[0] = null; /* Did not find target */
			}
		} else {
			Depot.forEach( (depot) ->
			{
				TileIndex tile = depot.xy;
				if (depot.isValid() && tile.IsTileDepotType(TransportType.Water) && tile.IsTileOwner(v.owner)) {
					int dist = Map.DistanceManhattan(tile, tile2);
					if (dist < best_dist[0]) {
						best_dist[0] = dist;
						best_depot[0] = depot;
					}
				}
			});
		}
		return best_depot[0];
	}

	static void CheckIfShipNeedsService(Vehicle v)
	{
		final Depot  depot;

		if (Global._patches.servint_ships == 0) return;
		if (!v.VehicleNeedsService())     return;
		if(v.isStopped())   return;

		if ( (v.getCurrent_order().type == Order.OT_GOTO_DEPOT) &&
				0 != (v.getCurrent_order().flags & Order.OF_HALT_IN_DEPOT))
			return;

		if (Global._patches.gotodepot.get() && v.VehicleHasDepotOrders()) return;

		depot = FindClosestShipDepot(v);

		if (depot == null || Map.DistanceManhattan(v.tile, depot.xy) > 12) {
			if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT) {
				v.getCurrent_order().type = Order.OT_DUMMY;
				v.getCurrent_order().flags = 0;
				Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
			}
			return;
		}

		v.getCurrent_order().type = Order.OT_GOTO_DEPOT;
		v.getCurrent_order().flags = Order.OF_NON_STOP;
		v.getCurrent_order().station = depot.index;
		v.dest_tile = depot.xy;
		Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
	}

	public static void OnNewDay_Ship(Vehicle v)
	{
		int cost;

		if ((++v.day_counter & 7) == 0)
			v.DecreaseVehicleValue();

		v.CheckVehicleBreakdown();
		v.AgeVehicle();
		CheckIfShipNeedsService(v);

		Order.CheckOrders(v.index, Order.OC_INIT);

		if( v.isStopped()) return;

		cost = (int) (EngineGui.ShipVehInfo(v.getEngine_type().id).running_cost * Global._price.ship_running / 364);
		v.profit_this_year -= cost >> 8;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_SHIP_RUN);
		Player.SubtractMoneyFromPlayerFract(v.owner, cost);

		Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
		//we need this for the profit
		Window.InvalidateWindowClasses(Window.WC_SHIPS_LIST);
	}

	static void HandleBrokenShip(Vehicle v)
	{
		if (v.breakdown_ctr != 1) {
			v.breakdown_ctr = 1;
			v.cur_speed = 0;

			if (v.breakdowns_since_last_service != 255)
				v.breakdowns_since_last_service++;

			Window.InvalidateWindow(Window.WC_VEHICLE_VIEW, v.index);
			Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);

			v.SndPlayVehicleFx((GameOptions._opt.landscape != Landscape.LT_CANDY) ?				
				Snd.SND_10_TRAIN_BREAKDOWN : Snd.SND_3A_COMEDY_BREAKDOWN_2);

			if(!v.isHidden()) {
				Vehicle u = v.CreateEffectVehicleRel(4, 4, 5, Vehicle.EV_BREAKDOWN_SMOKE);
				if (u != null) u.special.unk0 = v.breakdown_delay * 2;
			}
		}

		if (0 == (v.tick_counter & 1)) {
			if (0 == --v.breakdown_delay) {
				v.breakdown_ctr = 0;
				Window.InvalidateWindow(Window.WC_VEHICLE_VIEW, v.index);
			}
		}
	}

	static void MarkShipDirty(Vehicle v)
	{
		v.cur_image = GetShipImage(v, v.direction);
		ViewPort.MarkAllViewportsDirty(v.left_coord, v.top_coord, v.right_coord + 1, v.bottom_coord + 1);
	}

	static void PlayShipSound(Vehicle v)
	{
		v.SndPlayVehicleFx(EngineGui.ShipVehInfo(v.engine_type.id).sfx);
	}

	static final TileIndexDiffC _dock_offs[] = {
		new TileIndexDiffC( 2,  0),
		new TileIndexDiffC(-2,  0),
		new TileIndexDiffC( 0,  2),
		new TileIndexDiffC( 2,  0),
		new TileIndexDiffC( 0, -2),
		new TileIndexDiffC( 0,  0),
		new TileIndexDiffC( 0,  0),
		new TileIndexDiffC( 0,  0)
	};

	static void ProcessShipOrder(Vehicle v)
	{
		final Order order;

		if (v.getCurrent_order().type >= Order.OT_GOTO_DEPOT &&
				v.getCurrent_order().type <= Order.OT_LEAVESTATION) {
			if (v.getCurrent_order().type != Order.OT_GOTO_DEPOT ||
					0 == (v.getCurrent_order().flags & Order.OF_UNLOAD))
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
			v.getCurrent_order().type  = Order.OT_NOTHING;
			v.getCurrent_order().flags = 0;
			v.dest_tile = null;
			return;
		}

		if (order.type    == v.getCurrent_order().type &&
				order.flags   == v.getCurrent_order().flags &&
				order.station == v.getCurrent_order().station)
			return;

		v.setCurrent_order(new Order( order ));

		if (order.type == Order.OT_GOTO_STATION) {
			final Station st;

			if (order.station == v.last_station_visited)
				v.last_station_visited = Station.INVALID_STATION;

			st = Station.GetStation(order.station);
			if (st.dock_tile != null) {
				//v.dest_tile = TILE_ADD(st.dock_tile, ToTileIndexDiff(_dock_offs[_m[st.dock_tile].m5-0x4B]));
				v.dest_tile = st.dock_tile.iadd( TileIndex.ToTileIndexDiff(_dock_offs[st.dock_tile.getMap().m5-0x4B]));
			}
		} else if (order.type == Order.OT_GOTO_DEPOT) {
			v.dest_tile = Depot.GetDepot(order.station).xy;
		} else {
			v.dest_tile = null;
		}

		v.InvalidateVehicleOrder();

		Window.InvalidateWindowClasses(Window.WC_SHIPS_LIST);
	}

	static void HandleShipLoading(Vehicle v)
	{
		if (v.getCurrent_order().type == Order.OT_NOTHING) return;

		if (v.getCurrent_order().type != Order.OT_DUMMY) {
			if (v.getCurrent_order().type != Order.OT_LOADING) return;
			if (--v.load_unload_time_rem > 0) return;

			if (0 != (v.getCurrent_order().flags & Order.OF_FULL_LOAD) && v.CanFillVehicle()) {
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_SHIP_INC);
				if (Economy.LoadUnloadVehicle(v)!=0) {
					Window.InvalidateWindow(Window.WC_SHIPS_LIST, v.owner.id);
					MarkShipDirty(v);
				}
				return;
			}
			PlayShipSound(v);

			{
				Order b = new Order( v.getCurrent_order() );
				v.getCurrent_order().type = Order.OT_LEAVESTATION;
				v.getCurrent_order().flags = 0;
				if (0 == (b.flags & Order.OF_NON_STOP)) return;
			}
		}

		v.cur_order_index++;
		v.InvalidateVehicleOrder();
	}

	
	private static int MKIT(int d, int c, int b, int a) { return ((a&0xFF)<<24) | ((b&0xFF)<<16) | ((c&0xFF)<<8) | ((d&0xFF)<<0); }
	private static final int _delta_xy_table[] = {
		MKIT( -3, -3,  6,  6),
		MKIT(-16, -3, 32,  6),
		MKIT( -3, -3,  6,  6),
		MKIT( -3,-16,  6, 32),
		MKIT( -3, -3,  6,  6),
		MKIT(-16, -3, 32,  6),
		MKIT( -3, -3,  6,  6),
		MKIT( -3,-16,  6, 32),
	};

	
	
	static void UpdateShipDeltaXY(Vehicle v, int dir)
	{
		int x = _delta_xy_table[dir];
		v.x_offs        = (byte)BitOps.GB(x,  0, 8); // [dz] NB! Signed byte! 
		v.y_offs        = (byte)BitOps.GB(x,  8, 8); // [dz] NB! Signed byte!
		v.sprite_width  =  BitOps.GB(x, 16, 8);
		v.sprite_height =  BitOps.GB(x, 24, 8);
	}

	static void RecalcShipStuff(Vehicle v)
	{
		UpdateShipDeltaXY(v, v.direction);
		v.cur_image = GetShipImage(v, v.direction);
		MarkShipDirty(v);
		Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.tile);
	}

	static final TileIndexDiffC _ship_leave_depot_offs[] = {
		new TileIndexDiffC(-1,  0),
		new TileIndexDiffC( 0, -1)
	};

	static void CheckShipLeaveDepot(Vehicle v)
	{
		TileIndex tile;
		int d;
		int m;

		if (v.ship.state != 0x80) return;

		tile = v.tile;
		d = 0 != (tile.getMap().m5&2) ? 1 : 0;

		// Check first side
		TileIndex tile_ADD = TileIndex.TILE_ADD(tile, TileIndex.ToTileIndexDiff(_ship_leave_depot_offs[d]));
		if(0 != (_ship_sometracks[d] & GetTileShipTrackStatus(tile_ADD))) {
			m = (d==0) ? 0x101 : 0x207;
		// Check second side
		} else if(0 != (_ship_sometracks[d+2] & GetTileShipTrackStatus(TileIndex.TILE_ADD(tile, -2 * TileIndex.ToTileIndexDiff(_ship_leave_depot_offs[d]).diff ) )) ) {
			m = (d==0) ? 0x105 : 0x203;
		} else {
			return;
		}
		v.direction    = BitOps.GB(m, 0, 8);
		v.ship.state =  BitOps.GB(m, 8, 8);
		v.setHidden(false);

		v.cur_speed = 0;
		RecalcShipStuff(v);

		PlayShipSound(v);
		v.VehicleServiceInDepot();
		Window.InvalidateWindowClasses(Window.WC_SHIPS_LIST);
	}

	static boolean ShipAccelerate(Vehicle v)
	{
		int spd;
		int t; // was unsigned byte

		spd = Math.min(v.cur_speed + 1, v.getMax_speed());

		//updates statusbar only if speed have changed to save CPU time
		if (spd != v.cur_speed) {
			v.cur_speed = spd;
			if (Global._patches.vehicle_speed)
				Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
		}

		// Decrease somewhat when turning
		if (0 ==(v.direction & 1)) spd = spd * 3 / 4;

		if (spd == 0) return false;
		if (++spd == 0) return true;

		t =  v.progress & 0xFF;
		v.progress = BitOps.uint16Wrap( t - spd );

		return (t < v.progress);
	}

	static int EstimateShipCost(/*EngineID*/ int engine_type)
	{
		return ((int)(EngineGui.ShipVehInfo(engine_type).base_cost * (Global._price.ship_base/8)))>>5;
	}

	static void ShipEnterDepot(Vehicle v)
	{
		v.ship.forceInDepot();
		v.setHidden(true);
		v.cur_speed = 0;
		RecalcShipStuff(v);

		v.VehicleServiceInDepot();

		Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);

		v.TriggerVehicle(Engine.VEHICLE_TRIGGER_DEPOT);

		if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT) 
		{
			Window.InvalidateWindow(Window.WC_VEHICLE_VIEW, v.index);

			Order t = new Order( v.getCurrent_order() );
			v.getCurrent_order().type = Order.OT_DUMMY;
			v.getCurrent_order().flags = 0;

			if (BitOps.HASBIT(t.flags, Order.OFB_PART_OF_ORDERS)) {
				v.cur_order_index++;
			} else if (BitOps.HASBIT(t.flags, Order.OFB_HALT_IN_DEPOT)) {
				v.setStopped(true);
				if (v.owner.isLocalPlayer()) 
				{
					Global.SetDParam(0, v.unitnumber.id);
					NewsItem.AddNewsItem(
						Str.STR_981C_SHIP_IS_WAITING_IN_DEPOT,
						NewsItem.NEWS_FLAGS(NewsItem.NM_SMALL, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ADVICE, 0),
						v.index,
						0);
				}
			}
		}
		Window.InvalidateWindowClasses(Window.WC_SHIPS_LIST);
	}

	static void ShipArrivesAt(final Vehicle  v, Station  st)
	{
		/* Check if station was ever visited before */
		if (0 ==(st.had_vehicle_of_type & Station.HVOT_SHIP)) {
			int flags;

			st.had_vehicle_of_type |= Station.HVOT_SHIP;

			Global.SetDParam(0, st.index);
			flags = v.owner.isLocalPlayer() ? NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ARRIVAL_PLAYER, 0) : NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ARRIVAL_OTHER, 0);
			NewsItem.AddNewsItem(
				Str.STR_9833_CITIZENS_CELEBRATE_FIRST,
				flags,
				v.index,
				0);
		}
	}

	// state unused
	static boolean ShipTrackFollower(TileIndex tile, Object o, int track, int length, int []state)
	{
		PathFindShip pfs = (PathFindShip) o;
		
		// Found dest?
		if (tile.equals(pfs.dest_coords)) {
			pfs.best_bird_dist = 0;
			pfs.best_length = (int) BitOps.minu(pfs.best_length, length);
			return true;
		}

		// Skip this tile in the calculation
		if (!tile.equals(pfs.skiptile)) {
			pfs.best_bird_dist = (int) BitOps.minu(pfs.best_bird_dist, Map.DistanceMaxPlusManhattan(pfs.dest_coords, tile));
		}

		return false;
	}

	static final byte _ship_search_directions[][] = {
		{ 0, 9, 2, 9 },
		{ 9, 1, 9, 3 },
		{ 9, 0, 3, 9 },
		{ 1, 9, 9, 2 },
		{ 3, 2, 9, 9 },
		{ 9, 9, 1, 0 },
	};

	static final byte _pick_shiptrack_table[] = {1, 3, 2, 2, 0, 0};

	private static boolean TryTrack(int best_track, int i, PathFindShip pfs, int ship_dir, int best_length, int best_bird_dist)
	{
		if (best_track >= 0) {
			if (pfs.best_bird_dist != 0) {
				/* neither reached the destination, pick the one with the smallest bird dist */
				if (pfs.best_bird_dist > best_bird_dist) return false;
				if (pfs.best_bird_dist < best_bird_dist) return true;
			} else {
				if (pfs.best_length > best_length) return false;
				if (pfs.best_length < best_length) return true;
			}

			/* if we reach this position, there's two paths of equal value so far.
			 * pick one randomly. */
			int r = BitOps.GB(Hal.Random(), 0, 8);
			if (_pick_shiptrack_table[i] == ship_dir) r += 80;
			if (_pick_shiptrack_table[best_track] == ship_dir) r -= 80;
			if (r <= 127) return false;
		}
		
		return true;
	}
	
	static int FindShipTrack(Vehicle v, TileIndex tile, int dir, int bits, TileIndex skiptile, int [] track)
	{
		PathFindShip pfs = new PathFindShip();
		int i, best_track;
		int best_bird_dist = 0;
		int best_length    = 0;
		//int r;
		int ship_dir =  (v.direction & 3);

		pfs.dest_coords = v.dest_tile;
		pfs.skiptile = skiptile;

		best_track = -1;

		do {
			i = BitOps.FIND_FIRST_BIT(bits);
			bits = BitOps.KILL_FIRST_BIT(bits);

			pfs.best_bird_dist = -1;
			pfs.best_length = -1;

			//FollowTrack(tile, 0x3800 | TransportType.Water, _ship_search_directions[i][dir], (TPFEnumProc*)ShipTrackFollower, null, &pfs);
			Pathfind.FollowTrack(tile, TransportType.Water, 0x3800, _ship_search_directions[i][dir], Ship::ShipTrackFollower, null, pfs);

			if( TryTrack(best_track, i, pfs, ship_dir, best_length, best_bird_dist) )
			{
			best_track = i;
			best_bird_dist = pfs.best_bird_dist;
			best_length = pfs.best_length;
			}
		} while (bits != 0);

		track[0] = best_track;
		return best_bird_dist;
	}

	/* returns the track to choose on the next tile, or -1 when it's better to
	 * reverse. The tile given is the tile we are about to enter, enterdir is the
	 * direction in which we are entering the tile */
	static int ChooseShipTrack(Vehicle v, TileIndex tile, int enterdir, int tracks)
	{
		assert(enterdir>=0 && enterdir<=3);

		if (Global._patches.new_pathfinding_all) {
			NPFFindStationOrTileData fstd = new NPFFindStationOrTileData();
			NPFFoundTargetData ftd;
			//TileIndex src_tile = TILE_ADD(tile, TileIndex.TileOffsByDir(RailtypeInfo.ReverseDiagdir(enterdir)));
			TileIndex src_tile = tile.iadd(TileIndex.TileOffsByDir(Rail.ReverseDiagdir(enterdir)));
			int trackdir = v.GetVehicleTrackdir();
			assert (trackdir != 0xFF); /* Check that we are not in a Depot */

			Npf.NPFFillWithOrderData(fstd, v);

			ftd = Npf.NPFRouteToStationOrTile(src_tile, trackdir, fstd, TransportType.Water, v.owner, Rail.INVALID_RAILTYPE, Pbs.PBS_MODE_NONE);

			if (ftd.best_trackdir != 0xff) {
				/* If ftd.best_bird_dist is 0, we found our target and ftd.best_trackdir contains
				the direction we need to take to get there, if ftd.best_bird_dist is not 0,
				we did not find our target, but ftd.best_trackdir contains the direction leading
				to the tile closest to our target. */
				return ftd.best_trackdir & 7; /* TODO: Wrapper function? */
			} else {
				return -1; /* Already at target, reverse? */
			}
		} else {
			int b;
			int tot_dist, dist;
			int [] track = { 0 };
			TileIndex tile2;

			//tile2 = TILE_ADD(tile, -TileOffsByDir(enterdir));
			tile2 = tile.isub( TileIndex.TileOffsByDir(enterdir) );
			tot_dist = -1;

			/* Let's find out how far it would be if we would reverse first */
			b = GetTileShipTrackStatus(tile2) & _ship_sometracks[Rail.ReverseDiagdir(enterdir)] & v.ship.state;
			if (b != 0) {
				dist = FindShipTrack(v, tile2, Rail.ReverseDiagdir(enterdir), b, tile, track);
				if (dist != -1)
					tot_dist = dist + 1;
			}
			/* And if we would not reverse? */
			dist = FindShipTrack(v, tile, enterdir, tracks, null, track);
			if (dist > tot_dist)
				/* We could better reverse */
				return -1;
			return track[0];
		}
	}

	static final byte _new_vehicle_direction_table[] = {
		0, 7, 6, 0,
		1, 0, 5, 0,
		2, 3, 4,
	};

	static int ShipGetNewDirectionFromTiles(TileIndex new_tile, TileIndex old_tile)
	{
		int offs = (new_tile.TileY() - old_tile.TileY() + 1) * 4 +
				new_tile.TileX() - old_tile.TileX() + 1;
		assert(offs < 11 && offs != 3 && offs != 7);
		return _new_vehicle_direction_table[offs];
	}

	static int ShipGetNewDirection(Vehicle v, int x, int y)
	{
		int offs = (y - v.getY_pos() + 1) * 4 + (x - v.getX_pos() + 1);
		assert(offs < 11 && offs != 3 && offs != 7);
		return _new_vehicle_direction_table[offs];
	}

	static int GetAvailShipTracks(TileIndex tile, int dir)
	{
		int r = Landscape.GetTileTrackStatus(tile, TransportType.Water);
		return  (r | r >>> 8) & _ship_sometracks[dir];
	}

	static final byte _ship_subcoord[][][] = {
		{
			{15, 8, 1},
			{ 0, 0, 0},
			{ 0, 0, 0},
			{15, 8, 2},
			{15, 7, 0},
			{ 0, 0, 0},
		},
		{
			{ 0, 0, 0},
			{ 8, 0, 3},
			{ 7, 0, 2},
			{ 0, 0, 0},
			{ 8, 0, 4},
			{ 0, 0, 0},
		},
		{
			{ 0, 8, 5},
			{ 0, 0, 0},
			{ 0, 7, 6},
			{ 0, 0, 0},
			{ 0, 0, 0},
			{ 0, 8, 4},
		},
		{
			{ 0, 0, 0},
			{ 8,15, 7},
			{ 0, 0, 0},
			{ 8,15, 6},
			{ 0, 0, 0},
			{ 7,15, 0},
		}
	};

	static void ShipController(Vehicle v)
	{
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();
		int r;
		//final byte *b;
		int dir,track,tracks;

		v.tick_counter++;

		if (v.breakdown_ctr != 0) {
			if (v.breakdown_ctr <= 2) {
				HandleBrokenShip(v);
				return;
			}
			v.breakdown_ctr--;
		}

		if(v.isStopped()) return;

		ProcessShipOrder(v);
		HandleShipLoading(v);

		if (v.getCurrent_order().type == Order.OT_LOADING) return;

		CheckShipLeaveDepot(v);

		if (!ShipAccelerate(v)) return;

		v.BeginVehicleMove();

		//if (GetNewVehiclePos(v, &gp)) 
		if (v.GetNewVehiclePos(gp)) 
		{
			// staying in tile
			if (v.ship.state == 0x80) {
				gp.x = v.getX_pos();
				gp.y = v.getY_pos();
			} else {
				/* isnot inside Depot */
				r = v.VehicleEnterTile(gp.new_tile, gp.x, gp.y);
				if(0 != (r & 0x8)) //goto reverse_direction;
				{
					ShipController_reverse_direction(v);
					return;
				}

				/* A leave station order only needs one tick to get processed, so we can
				 * always skip ahead. */
				if (v.getCurrent_order().type == Order.OT_LEAVESTATION) {
					v.getCurrent_order().type = Order.OT_NOTHING;
					v.getCurrent_order().flags = 0;
					Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
				} else if (v.dest_tile != null) {
					/* We have a target, let's see if we reached it... */
					if (v.getCurrent_order().type == Order.OT_GOTO_STATION &&
							v.dest_tile.IsBuoyTile() &&
							Map.DistanceManhattan(v.dest_tile, gp.new_tile) <= 3) {
						/* We got within 3 tiles of our target buoy, so let's skip to our
						 * next Order */
						v.cur_order_index++;
						v.getCurrent_order().type = Order.OT_DUMMY;
						v.InvalidateVehicleOrder();
					} else {
						/* Non-buoy orders really need to reach the tile */
						if (v.dest_tile.equals(gp.new_tile)) {
							if (v.getCurrent_order().type == Order.OT_GOTO_DEPOT) {
								if ((gp.x&0xF)==8 && (gp.y&0xF)==8) {
									ShipEnterDepot(v);
									return;
								}
							} else if (v.getCurrent_order().type == Order.OT_GOTO_STATION) {
								Station st;

								v.last_station_visited = v.getCurrent_order().station;

								/* Process station in the orderlist. */
								st = Station.GetStation(v.getCurrent_order().station);
								if(0 != (st.facilities & Station.FACIL_DOCK)) { /* ugly, ugly workaround for problem with ships able to drop off cargo at wrong stations */
									v.getCurrent_order().type = Order.OT_LOADING;
									v.getCurrent_order().flags &= Order.OF_FULL_LOAD | Order.OF_UNLOAD;
									v.getCurrent_order().flags |= Order.OF_NON_STOP;
									ShipArrivesAt(v, st);

									Player.SET_EXPENSES_TYPE(Player.EXPENSES_SHIP_INC);
									if (0 != Economy.LoadUnloadVehicle(v)) {
										Window.InvalidateWindow(Window.WC_SHIPS_LIST, v.owner);
										MarkShipDirty(v);
									}
									Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
								} else { /* leave stations without docks right aways */
									v.getCurrent_order().type = Order.OT_LEAVESTATION;
									v.getCurrent_order().flags = 0;
									v.cur_order_index++;
									v.InvalidateVehicleOrder();
								}
							}
						}
					}
				}
			}
		} else {
			// new tile
			if (gp.new_tile.TileX() >= Global.MapMaxX() || gp.new_tile.TileY() >= Global.MapMaxY())
			{
				//goto reverse_direction;
				ShipController_reverse_direction(v);
				return;
			}
			
			dir = ShipGetNewDirectionFromTiles(gp.new_tile, gp.old_tile);
			assert(dir == 1 || dir == 3 || dir == 5 || dir == 7);
			dir>>=1;
			tracks = GetAvailShipTracks(gp.new_tile, dir);
			if (tracks == 0)
			{
				//goto reverse_direction;
				ShipController_reverse_direction(v);
				return;
			}

			// Choose a direction, and continue if we find one
			track = ChooseShipTrack(v, gp.new_tile, dir, tracks);
			if (track < 0)
			{
				//goto reverse_direction;
				ShipController_reverse_direction(v);
				return;
			}

			byte [] b = _ship_subcoord[dir][track];

			gp.x = (gp.x&~0xF) | b[0];
			gp.y = (gp.y&~0xF) | b[1];

			/* Call the landscape function and tell it that the vehicle entered the tile */
			r = v.VehicleEnterTile(gp.new_tile, gp.x, gp.y);
			if(0 != (r&0x8) ) 
			{
				//goto reverse_direction;
				ShipController_reverse_direction(v);
				return;
			}

			if (0 == (r&0x4)) {
				v.tile = gp.new_tile;
				v.ship.state =  (1 << track);
			}

			v.direction = b[2];
		}

		/* update image of ship, as well as delta XY */
		dir = ShipGetNewDirection(v, gp.x, gp.y);
		v.x_pos = gp.x;
		v.y_pos = gp.y;
		v.z_pos = Landscape.GetSlopeZ(gp.x, gp.y);
		
		ShipController_getout( v, dir );
		
		/* to func 
	getout:
		UpdateShipDeltaXY(v, dir);
		v.cur_image = GetShipImage(v, dir);
		VehiclePositionChanged(v);
		EndVehicleMove(v);
		return;
		*/
		/* to func 
	reverse_direction:
		dir = v.direction ^ 4;
		v.direction = dir;
		goto getout;
		*/
	}

	private static void ShipController_reverse_direction(Vehicle v)
	{
		int dir;
		dir = v.direction ^ 4;
		v.direction = dir;
		ShipController_getout( v, dir );
	}
	
	private static void ShipController_getout(Vehicle v, int dir )
	{
		UpdateShipDeltaXY(v, dir);
		v.cur_image = GetShipImage(v, dir);
		v.VehiclePositionChanged();
		v.EndVehicleMove();
	}

	
	static void AgeShipCargo(Vehicle v)
	{
		if (Global._age_cargo_skip_counter != 0) return;
		if (v.cargo_days != 255) v.cargo_days++;
	}

	public static void Ship_Tick(Vehicle v)
	{
		AgeShipCargo(v);
		ShipController(v);
	}


	public static void ShipsYearlyLoop()
	{
		Vehicle.forEach( (v) ->
		{
			if (v.type == Vehicle.VEH_Ship) {
				v.profit_last_year = v.profit_this_year;
				v.profit_this_year = 0;
				Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
			}
		});
	}

	/** Build a ship.
	 * @param x,y tile coordinates of depot where ship is built
	 * @param p1 ship type being built (engine)
	 * @param p2 unused
	 */
	static int CmdBuildShip(int x, int y, int flags, int p1, int p2)
	{
		int value;
		Vehicle v;
		UnitID unit_num;
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		Engine e;

		if (!Engine.IsEngineBuildable(p1, Vehicle.VEH_Ship)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_NEW_VEHICLES);

		value = EstimateShipCost(p1);
		if(0!=(flags & Cmd.DC_QUERY_COST)) return value;

		/* The ai_new queries the vehicle cost before building the route,
		 * so we must check against cheaters no sooner than now. --pasky */
		if (!tile.IsTileDepotType(TransportType.Water)) return Cmd.CMD_ERROR;
		//if (!tile.IsTileOwner( Global.gs._current_player.id)) return Cmd.CMD_ERROR;
		if (!tile.IsTileOwner(PlayerID.getCurrent())) return Cmd.CMD_ERROR;

		v = Vehicle.AllocateVehicle(); // TODO can pass type or make subobject for ship
		if (v == null || /* Order.IsOrderPoolFull() || */
				(unit_num = Vehicle.GetFreeUnitNumber(Vehicle.VEH_Ship)).id > Global._patches.max_ships)
			return Cmd.return_cmd_error(Str.STR_00E1_TOO_MANY_VEHICLES_IN_GAME);

		if( 0 != (flags & Cmd.DC_EXEC)) {
			final ShipVehicleInfo svi = EngineGui.ShipVehInfo(p1);

			v.unitnumber = unit_num;

			v.owner = PlayerID.getCurrent();
			v.tile = tile;
			x = tile.TileX() * 16 + 8;
			y = tile.TileY() * 16 + 8;
			v.x_pos = x;
			v.y_pos = y;
			v.z_pos = Landscape.GetSlopeZ(x,y);

			v.z_height = 6;
			v.sprite_width = 6;
			v.sprite_height = 6;
			v.x_offs = -3;
			v.y_offs = -3;
			v.assignStatus( Vehicle.VS_HIDDEN | Vehicle.VS_STOPPED | Vehicle.VS_DEFPAL );

			v.spritenum = svi.image_index;
			v.cargo_type = svi.cargo_type;
			v.cargo_cap = svi.capacity;
			v.value = value;

			v.last_station_visited = Station.INVALID_STATION;
			v.max_speed = svi.max_speed;
			v.engine_type = EngineID.get( p1 );

			e = Engine.GetEngine(p1);
			v.reliability = e.getReliability();
			v.reliability_spd_dec = e.reliability_spd_dec;
			v.max_age = e.getLifelength() * 366;
			
			Global._new_ship_id = //v.index;
			Global._new_vehicle_id = VehicleID.get( v.index );

			v.string_id = Str.STR_SV_SHIP_NAME;
			v.ship.forceInDepot();//state = 0x80;

			v.service_interval = Global._patches.servint_ships;
			v.date_of_last_service = Global.get_date();
			v.build_year =  Global.get_cur_year();
			v.cur_image = 0x0E5E;
			v.type = Vehicle.VEH_Ship;
			v.random_bits = Vehicle.VehicleRandomBits();

			v.VehiclePositionChanged();

			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.getTile());
			VehicleGui.RebuildVehicleLists();
			Window.InvalidateWindow(Window.WC_COMPANY, v.owner.id);
			if (Player.IsLocalPlayer())
				Window.InvalidateWindow(Window.WC_REPLACE_VEHICLE, Vehicle.VEH_Ship); // updates the replace Ship window
		}

		return value;
	}

	/** Sell a ship.
	 * @param x,y unused
	 * @param p1 vehicle ID to be sold
	 * @param p2 unused
	 */
	static int CmdSellShip(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Ship || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_NEW_VEHICLES);

		//if (!v.tile.IsTileDepotType(TransportType.Water) || v.road.state != 0x80 || 0==(v.vehstatus&Vehicle.VS_STOPPED))
		if (!v.tile.IsTileDepotType(TransportType.Water) || !v.ship.isInDepot() || !v.isStopped())
			return Cmd.return_cmd_error(Str.STR_980B_SHIP_MUST_BE_STOPPED_IN);

		if(0 != (flags & Cmd.DC_EXEC)) {
			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.getTile());
			VehicleGui.RebuildVehicleLists();
			Window.InvalidateWindow(Window.WC_COMPANY, v.owner.id);
			Window.DeleteWindowById(Window.WC_VEHICLE_VIEW, v.index);
			Vehicle.DeleteVehicle(v);
			if (Player.IsLocalPlayer())
				Window.InvalidateWindow(Window.WC_REPLACE_VEHICLE, Vehicle.VEH_Ship); // updates the replace Ship window
		}

		return -v.value;
	}

	/** Start/Stop a ship.
	 * @param x,y unused
	 * @param p1 ship ID to start/stop
	 * @param p2 unused
	 */
	static int CmdStartStopShip(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Ship || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if( 0 != (flags & Cmd.DC_EXEC)) {
			v.toggleStopped();
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
			Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.tile.getTile());
			Window.InvalidateWindowClasses(Window.WC_SHIPS_LIST);
		}

		return 0;
	}

	/** Send a ship to the depot.
	 * @param x,y unused
	 * @param p1 vehicle ID to send to the depot
	 * @param p2 unused
	 */
	static int CmdSendShipToDepot(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		final Depot dep;

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Ship || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if(v.isCrashed()) return Cmd.CMD_ERROR;

		/* If the current orders are already goto-Depot */
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

		dep = FindClosestShipDepot(v);
		if (dep == null)
			return Cmd.return_cmd_error(Str.STR_981A_UNABLE_TO_FIND_LOCAL_DEPOT);

		if(0 != (flags & Cmd.DC_EXEC)) {
			v.dest_tile = dep.xy;
			v.getCurrent_order().type = Order.OT_GOTO_DEPOT;
			v.getCurrent_order().flags = Order.OF_NON_STOP | Order.OF_HALT_IN_DEPOT;
			v.getCurrent_order().station = dep.index;
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, STATUS_BAR);
		}

		return 0;
	}

	/** Change the service interval for ships.
	 * @param x,y unused
	 * @param p1 vehicle ID that is being service-interval-changed
	 * @param p2 new service interval
	 */
	static int CmdChangeShipServiceInt(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		int serv_int = Depot.GetServiceIntervalClamped(p2); /* Double check the service interval from the user-input */

		if (serv_int != p2 || !Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Ship || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if(0!=(flags & Cmd.DC_EXEC)) {
			v.service_interval = serv_int;
			Window.InvalidateWindowWidget(Window.WC_VEHICLE_DETAILS, v.index, 7);
		}

		return 0;
	}

	/** Refits a ship to the specified cargo type.
	 * @param x,y unused
	 * @param p1 vehicle ID of the ship to refit
	 * @param p2 various bitstuffed elements
	 * - p2 = (bit 0-7) - the new cargo type to refit to (p2 & 0xFF)
	 */
	static int CmdRefitShip(int x, int y, int flags, int p1, int p2)
	{
		Vehicle v;
		int cost;
		CargoID new_cid = CargoID.get(p2 & 0xFF); //gets the cargo number

		if (!Vehicle.IsVehicleIndex(p1)) return Cmd.CMD_ERROR;

		v = Vehicle.GetVehicle(p1);

		if (v.type != Vehicle.VEH_Ship || !Player.CheckOwnership(v.owner)) return Cmd.CMD_ERROR;

		if (!Depot.IsTileDepotType(v.tile, TransportType.Water) || !v.isStopped() || !v.ship.isInDepot())
				return Cmd.return_cmd_error(Str.STR_980B_SHIP_MUST_BE_STOPPED_IN);


		/* Check cargo */
		if (0==Engine.ShipVehInfo(v.getEngine_type().id).refittable) return Cmd.CMD_ERROR;
		if (new_cid.id > AcceptedCargo.NUM_CARGO || !Vehicle.CanRefitTo(v.getEngine_type(), new_cid)) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_SHIP_RUN);

		cost = 0;
		if (v.owner.IS_HUMAN_PLAYER() && new_cid.id != v.getCargo_type()) {
			cost = ((int)Global._price.ship_base) >> 7;
		}

		if(0!= (flags & Cmd.DC_EXEC)) {
			v.cargo_count = 0;
			v.cargo_type = new_cid.id;
			Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);
		}

		return cost;

	}

}

class PathFindShip {
	TileIndex skiptile;
	TileIndex dest_coords;
	int best_bird_dist;
	int best_length;
} 

