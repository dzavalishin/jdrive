package com.dzavalishin.xui;

import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.AirCraft;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Engine;
import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Order;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.RailVehicleInfo;
import com.dzavalishin.game.RoadVehCmd;
import com.dzavalishin.game.Ship;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.Train;
import com.dzavalishin.game.TrainCmd;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.ids.CargoID;
import com.dzavalishin.ids.EngineID;
import com.dzavalishin.sort.AbstractVehicleSorter;
import com.dzavalishin.sort.VehicleAgeSorter;
import com.dzavalishin.sort.VehicleCargoSorter;
import com.dzavalishin.sort.VehicleMaxSpeedSorter;
import com.dzavalishin.sort.VehicleNameSorter;
import com.dzavalishin.sort.VehicleNumberSorter;
import com.dzavalishin.sort.VehicleProfitLastYearSorter;
import com.dzavalishin.sort.VehicleProfitThisYearSorter;
import com.dzavalishin.sort.VehicleReliabilitySorter;
import com.dzavalishin.sort.VehicleUnsortedSorter;
import com.dzavalishin.struct.EngineInfo;
import com.dzavalishin.struct.SortStruct;
import com.dzavalishin.struct.Sorting;
import com.dzavalishin.tables.EngineTables;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.wcustom.vehiclelist_d;

import java.util.Arrays;
import java.util.Iterator;

public class VehicleGui 
{
	public static final Sorting _sorting = new Sorting();

	static int _internal_name_sorter_id; // internal StringID for default vehicle-names
	static int _last_vehicle_idx;        // cached index to hopefully speed up name-sorting
	public static boolean   _internal_sort_order;     // descending/ascending
	//static int   _internal_sort_order;     // descending/ascending

	static final int [] _player_num_engines = new int[Global.TOTAL_NUM_ENGINES];
	static /* RailType */ int _railtype_selected_in_replace_gui;

	public static final int PLY_WND_PRC__OFFSET_TOP_WIDGET = 26;
	public static final int PLY_WND_PRC__SIZE_OF_ROW_SMALL = 26;
	public static final int PLY_WND_PRC__SIZE_OF_ROW_BIG   = 36;

	public static final int PERIODIC_RESORT_DAYS = 10;
	public static final int SORT_BY_UNSORTED  = 0;


	//enum VehicleSortTypes {
	public static final int VEHTRAIN     = 0;
	public static final int VEHROAD      = 1;
	public static final int VEHSHIP      = 2;
	public static final int VEHAIRCRAFT  = 3;



	static final AbstractVehicleSorter[] _vehicle_sorter = {
			new VehicleUnsortedSorter(),
			new VehicleNumberSorter(),
			new VehicleNameSorter(),
			new VehicleAgeSorter(),
			new VehicleProfitThisYearSorter(),
			new VehicleProfitLastYearSorter(),
			new VehicleCargoSorter(),
			new VehicleReliabilitySorter(),
			new VehicleMaxSpeedSorter()
	};

	public final static int _vehicle_sort_listing[] = {
			Str.STR_SORT_BY_UNSORTED,
			Str.STR_SORT_BY_NUMBER,
			Str.STR_SORT_BY_DROPDOWN_NAME,
			Str.STR_SORT_BY_AGE,
			Str.STR_SORT_BY_PROFIT_THIS_YEAR,
			Str.STR_SORT_BY_PROFIT_LAST_YEAR,
			Str.STR_SORT_BY_TOTAL_CAPACITY_PER_CARGOTYPE,
			Str.STR_SORT_BY_RELIABILITY,
			Str.STR_SORT_BY_MAX_SPEED,
	};

	static final int[] _rail_types_list = {
			Str.STR_RAIL_VEHICLES,
			Str.STR_MONORAIL_VEHICLES,
			Str.STR_MAGLEV_VEHICLES,
	};

	public static void RebuildVehicleLists()
	{
		Iterator<Window> it = Window.getIterator();
		while(it.hasNext())
		{
			Window w = it.next();

			switch (w.getWindow_class()) {
			case Window.WC_TRAINS_LIST: case Window.WC_ROADVEH_LIST:
			case Window.WC_SHIPS_LIST:  case Window.WC_AIRCRAFT_LIST:
				w.as_vehiclelist_d().flags |= Vehicle.VL_REBUILD;
				w.SetWindowDirty();
				break;
			default: break;
			}
		}
	}

	public static void ResortVehicleLists()
	{
		Iterator<Window> it = Window.getIterator();
		while(it.hasNext())
		{
			Window w = it.next();

			switch (w.getWindow_class()) {
			case Window.WC_TRAINS_LIST: case Window.WC_ROADVEH_LIST:
			case Window.WC_SHIPS_LIST:  case Window.WC_AIRCRAFT_LIST:
				w.as_vehiclelist_d().flags |= Vehicle.VL_RESORT;
				w.SetWindowDirty();			break;
			default: break;
			}
		}
	}

	static SortStruct[] _vehicle_sort;

	public static void BuildVehicleList(vehiclelist_d vl, int type, /*PlayerID*/ int owner, /*StationID*/ int station)
	{
		int subtype = (type != Vehicle.VEH_Aircraft) ? Vehicle.Train_Front : 2;
		int n[] = {0};
		int i;

		if (0 ==(vl.flags & Vehicle.VL_REBUILD)) return;

		/* Create array for sorting */
		_vehicle_sort = new SortStruct[Vehicle.GetVehiclePoolSize()]; 

		Global.DEBUG_misc(1, "Building vehicle list for player %d station %d...",
				owner, station);

		if (station != Station.INVALID_STATION) {

			Iterator<Vehicle> it = Vehicle.getIterator();
			while(it.hasNext())
			{
				Vehicle v = it.next();
				if (v.getType() == type && (
						(type == Vehicle.VEH_Train && v.IsFrontEngine()) ||
						(type != Vehicle.VEH_Train && v.getSubtype() <= subtype))) {

					Iterator<Order> voi = v.getOrdersIterator();
					while(voi.hasNext())
					{
						final Order order = voi.next();
						if (order != null && order.getType() == Order.OT_GOTO_STATION && order.getStation() == station) 
						{
							_vehicle_sort[n[0]] = new SortStruct();
							_vehicle_sort[n[0]].index = v.index;
							_vehicle_sort[n[0]].owner = v.getOwner().id;
							++n[0];
							break;
						}
					}
				}
			}
		} else {
			Vehicle.forEach( (v) ->
			{
				if (v.getType() == type && v.getOwner().id == owner && (
						(type == Vehicle.VEH_Train && v.IsFrontEngine()) ||
						(type != Vehicle.VEH_Train && v.getSubtype() <= subtype))) 
				{
					if(_vehicle_sort[n[0]] == null) _vehicle_sort[n[0]] = new SortStruct();
					_vehicle_sort[n[0]].index = v.index;
					_vehicle_sort[n[0]].owner = v.getOwner().id;
					++n[0];
				}
			});
		}

		vl.sort_list = new SortStruct[n[0]];
		vl.list_length = n[0];

		for (i = 0; i < n[0]; ++i) vl.sort_list[i] = _vehicle_sort[i];

		vl.flags &= ~Vehicle.VL_REBUILD;
		vl.flags |= Vehicle.VL_RESORT;
	}

	public static void SortVehicleList(vehiclelist_d vl)
	{
		if (0 == (vl.flags & Vehicle.VL_RESORT)) return;

		_internal_sort_order = 0 != (vl.flags & Vehicle.VL_DESC);
		_internal_name_sorter_id = Str.STR_SV_TRAIN_NAME;

		Arrays.sort( vl.sort_list, _vehicle_sorter[vl.sort_type] );

		vl.resort_timer = Global.DAY_TICKS * PERIODIC_RESORT_DAYS;
		vl.flags &= ~Vehicle.VL_RESORT;
	}


	/* General Vehicle GUI based procedures that are independent of vehicle types */
	public static void InitializeVehiclesGuiList()
	{
		_railtype_selected_in_replace_gui = Train.RAILTYPE_RAIL;
	}

	// draw the vehicle profit button in the vehicle list window.
	public static void DrawVehicleProfitButton(final Vehicle v, int x, int y)
	{
		// draw profit-based colored icons
		int ormod = v.encodeColor();
		Gfx.DrawSprite(Sprite.SPR_BLOT | ormod, x, y);
	}


	/* macro
private static void show_cargo(ctype) { 
	byte colour = 16; 
	if (sel == 0) { 
		cargo = ctype; 
		colour = 12; 
	} 
	sel--; 
	Gfx.DrawString(6, y, _cargoc.names_s[ctype], colour); 
	y += 10; 
}*/



	/** Draw the list of available refit options for a consist.
	 * Draw the list and highlight the selected refit option (if any)
	 * @param v first vehicle in consist to get the refit-options of
	 * @param sel selected refit cargo-type in the window
	 * @return the cargo type that is hightlighted, AcceptedCargo.CT_INVALID if none
	 */
	public static CargoID DrawVehicleRefitWindow(final Vehicle v, int sel)
	{
		int cmask;
		//CargoID cid, cargo = AcceptedCargo.CT_INVALID;
		int cid, cargo = AcceptedCargo.CT_INVALID;
		int y = 25;
		Vehicle  u;

		/* Check if vehicle has custom refit or normal ones, and get its bitmasked value.
		 * If its a train, 'or' this with the refit masks of the wagons. Now just 'and'
		 * it with the bitmask of available cargo on the current landscape, and
		 * where the bits are set: those are available */
		cmask = 0;
		u = v;
		do {
			cmask |= Global._engine_info[u.getEngine_type().id].refit_mask;
			u = u.getNext();
		} while (v.getType() == Vehicle.VEH_Train && u != null);

		/* Check which cargo has been selected from the refit window and draw list */
		for (cid = 0; cmask != 0; cmask >>= 1, cid++) {
			if (BitOps.HASBIT(cmask, 0)) // vehicle is refittable to this cargo
			{
				//show_cargo(_local_cargo_id_ctype[cid]);
				byte colour = 16; 
				if (sel == 0) { 
					cargo = EngineTables._local_cargo_id_ctype[cid];
					colour = 12; 
				} 
				sel--; 
				Gfx.DrawString(6, y, Global._cargoc.names_s[cargo], colour); 
				y += 10; 
			}
		}
		return CargoID.get( cargo );
	}




	// this define is to match engine.c, but engine.c keeps it to itself
	// ENGINE_AVAILABLE is used in ReplaceVehicleWndProc
	//#define ENGINE_AVAILABLE ((e.flags & 1 && BitOps.HASBIT(info.climates, GameOptions._opt.landscape)) || BitOps.HASBIT(e.player_avail, Global.gs._local_player))

	static boolean ENGINE_IS_AVAILABLE(Engine e, EngineInfo info)
	{
		return (( e.flagIsAvailable() && BitOps.HASBIT(info.climates, GameOptions._opt.landscape))
				|| e.isAvailableToMe());
	}


	/*  if show_outdated is selected, it do not sort psudo engines properly but it draws all engines
	 *	if used compined with show_cars set to false, it will work as intended. Replace window do it like that
	 *  this was a big hack even before show_outdated was added. Stupid newgrf :p										*/
	static void train_engine_drawing_loop(int [] x, int [] y, int [] pos, int []sel, 
			int []selected_id, /* RailType */ int railtype,
			int lines_drawn, boolean is_engine, boolean show_cars, boolean show_outdated)
	{
		//EngineID i;
		int i;
		int colour;
		final Player p = Player.GetPlayer(Global.gs._local_player);

		for (i = 0; i < Global.NUM_TRAIN_ENGINES; i++) 
		{
			final Engine e = Engine.GetEngine(i);
			final RailVehicleInfo rvi = Engine.RailVehInfo(i);
			final EngineInfo info = Global._engine_info[i];

			if (!p.EngineHasReplacement(EngineID.get(i) ) && _player_num_engines[i] == 0 && show_outdated) continue;

			if (rvi.power == 0 && !show_cars)   // disables display of cars (works since they do not have power)
				continue;

			if (sel[0] == 0) selected_id[0] = i;


			colour = sel[0] == 0 ? 0xC : 0x10;
			if (!(ENGINE_IS_AVAILABLE(e, info) && show_outdated && (0 != Engine.RailVehInfo(i).power) && e.getRailtype() == railtype)) 
			{
				//if (e.railtype != railtype || 0==(rvi.flags & Engine.RVI_WAGON) != is_engine ||
				if (e.getRailtype() != railtype || rvi.isWagon() == is_engine ||
						!e.isAvailableToMe())
					continue;
			} /*else {
			// TODO find a nice red colour for vehicles being replaced
				if ( _autoreplace_array[i] != i )
					colour = *sel == 0 ? 0x44 : 0x45;
			} */

			if (BitOps.IS_INT_INSIDE(--pos[0], -lines_drawn, 0)) {
				Gfx.DrawString(x[0] + 59, y[0] + 2, Engine.GetCustomEngineName(i), colour);
				// show_outdated is true only for left side, which is where we show old replacements
				TrainCmd.DrawTrainEngine(x[0] + 29, y[0] + 6, i, (_player_num_engines[i] == 0 && show_outdated) ?
						Sprite.PALETTE_CRASH : Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)));
				if ( show_outdated ) {
					Global.SetDParam(0, _player_num_engines[i]);
					Gfx.DrawStringRightAligned(213, y[0]+5, Str.STR_TINY_BLACK, 0);
				}
				y[0] += 14;
			}
			--sel[0];
		}
	}


	static void SetupScrollStuffForReplaceWindow(Window w)
	{
		/* RailType */ int railtype;
		EngineID selected_id[] = { Engine.INVALID_ENGINE_ID, Engine.INVALID_ENGINE_ID };
		int [] sel = new int[2];
		int count = 0;
		int count2 = 0;
		//EngineID engine_id;
		int engine_id;
		final Player p = Player.GetPlayer(Global.gs._local_player);

		sel[0] = w.as_replaceveh_d().sel_index[0];
		sel[1] = w.as_replaceveh_d().sel_index[1];

		switch (w.as_replaceveh_d().vehicletype) {
		case Vehicle.VEH_Train: {
			railtype = _railtype_selected_in_replace_gui;
			w.widget.get(13).color = Global.gs._player_colors[Global.gs._local_player.id];	// sets the colour of that art thing
			w.widget.get(16).color = Global.gs._player_colors[Global.gs._local_player.id];	// sets the colour of that art thing
			for (engine_id = 0; engine_id < Global.NUM_TRAIN_ENGINES; engine_id++) {
				final Engine e = Engine.GetEngine(engine_id);
				final EngineInfo info = Global._engine_info[engine_id];

				if (ENGINE_IS_AVAILABLE(e,info) && (0 != Engine.RailVehInfo(engine_id).power) && e.getRailtype() == railtype) {
					if (_player_num_engines[engine_id] > 0 || p.EngineHasReplacement(EngineID.get(engine_id))) {
						if (sel[0] == 0) selected_id[0] = EngineID.get(engine_id);
						count++;
						sel[0]--;
					}
					if (e.isAvailableToMe()) {
						if (sel[1] == 0) selected_id[1] = EngineID.get(engine_id);
						count2++;
						sel[1]--;
					}
				}
			}
			break;
		}
		case Vehicle.VEH_Road: {
			int num = Global.NUM_ROAD_ENGINES;
			//final Engine  e = GetEngine(Global.ROAD_ENGINES_INDEX);
			int cargo;
			engine_id = Global.ROAD_ENGINES_INDEX;

			do {
				EngineID eid = EngineID.get(engine_id);
				if (_player_num_engines[engine_id] > 0 || p.EngineHasReplacement(eid)) {
					if (sel[0] == 0) selected_id[0] = eid;
					count++;
					sel[0]--;
				}
				++engine_id;
				//++e;
			} while (--num > 0);

			if (selected_id[0] != Engine.INVALID_ENGINE_ID) { // only draw right array if we have anything in the left one
				num = Global.NUM_ROAD_ENGINES;
				engine_id = Global.ROAD_ENGINES_INDEX;
				cargo = Engine.RoadVehInfo(selected_id[0].id).cargo_type;

				do {
					final Engine e = Engine.GetEngine(engine_id);
					if (cargo == EngineGui.RoadVehInfo(engine_id).cargo_type && e.isAvailableToMe()) {
						count2++;
						if (sel[1] == 0) selected_id[1] = EngineID.get(engine_id);
						sel[1]--;
					}
					++engine_id;
					//++e;
				} while (--num > 0);
			}
			break;
		}

		case Vehicle.VEH_Ship: {
			int num = Global.NUM_SHIP_ENGINES;
			Engine  e; // = GetEngine(Global.SHIP_ENGINES_INDEX);
			int cargo, refittable;
			engine_id = Global.SHIP_ENGINES_INDEX;

			do {
				e = Engine.GetEngine(engine_id);
				EngineID eid = EngineID.get(engine_id);
				if (_player_num_engines[engine_id] > 0 || p.EngineHasReplacement(eid)) {
					if (sel[0] == 0) selected_id[0] = eid;
					count++;
					sel[0]--;
				}
				++engine_id;
				//++e;
			} while (--num > 0);

			if (selected_id[0] != Engine.INVALID_ENGINE_ID) {
				num = Global.NUM_SHIP_ENGINES;
				//e = GetEngine(Global.SHIP_ENGINES_INDEX);
				engine_id = Global.SHIP_ENGINES_INDEX;
				cargo = Engine.ShipVehInfo(selected_id[0].id).cargo_type;
				refittable = Engine.ShipVehInfo(selected_id[0].id).refittable;

				do {
					e = Engine.GetEngine(engine_id);
					if (e.isAvailableToMe() &&
							(cargo == Engine.ShipVehInfo(engine_id).cargo_type 
							|| 0 != (refittable & Engine.ShipVehInfo(engine_id).refittable)) 
							) {
						if (sel[1] == 0) selected_id[1] = EngineID.get( engine_id );
						sel[1]--;
						count2++;
					}
					++engine_id;//++e;
				} while (--num > 0);
			}
			break;
		}   //end of ship

		case Vehicle.VEH_Aircraft:{
			int num = Global.NUM_AIRCRAFT_ENGINES;
			int subtype;
			//final Engine  e = GetEngine(Global.AIRCRAFT_ENGINES_INDEX);
			engine_id = Global.AIRCRAFT_ENGINES_INDEX;

			do {
				if (_player_num_engines[engine_id] > 0 || p.EngineHasReplacement(EngineID.get(engine_id))) {
					count++;
					if (sel[0] == 0) selected_id[0] = EngineID.get( engine_id );
					sel[0]--;
				}
				++engine_id;
				//++e;
			} while (--num > 0);

			if (selected_id[0] != Engine.INVALID_ENGINE_ID) {
				num = Global.NUM_AIRCRAFT_ENGINES;
				//e = GetEngine(Global.AIRCRAFT_ENGINES_INDEX);
				subtype = Engine.AircraftVehInfo(selected_id[0].id).subtype;
				engine_id = Global.AIRCRAFT_ENGINES_INDEX;
				do {
					Engine e = Engine.GetEngine(engine_id);
					if (e.isAvailableToMe()) {
						if (BitOps.HASBIT(subtype, 0) == BitOps.HASBIT(Engine.AircraftVehInfo(engine_id).subtype, 0)) {
							count2++;
							if (sel[1] == 0) selected_id[1] = EngineID.get(engine_id );
							sel[1]--;
						}
					}
					++engine_id;
					//++e;
				} while (--num > 0);
			}
			break;
		}
		}
		// sets up the number of items in each list
		w.SetVScrollCount( count);
		w.SetVScroll2Count(count2);
		w.as_replaceveh_d().sel_engine[0] = selected_id[0];
		w.as_replaceveh_d().sel_engine[1] = selected_id[1];

		w.as_replaceveh_d().count[0] = count;
		w.as_replaceveh_d().count[1] = count2;
	}


	static void DrawEngineArrayInReplaceWindow(Window w, int px, int py, int px2, int py2, int ppos, int ppos2,
			int psel1, int psel2, EngineID pselected_id1, EngineID pselected_id2)
	{
		final Player p = Player.GetPlayer(Global.gs._local_player);

		/*
	int [] sel = new int[2];
	//EngineID selected_id[2];
	int selected_id= new int[2];
	sel[0] = sel1;
	sel[1] = sel2;
	selected_id[0] = selected_id1;
	selected_id[1] = selected_id2;
		 */

		int [] x = { px };
		int [] y = { py };
		int [] x2 = { px2 };
		int [] y2 = { py2 };

		int [] pos  = { ppos };
		int [] pos2 = { ppos2 };

		int [] sel0 = { psel1 };
		int [] sel1 = { psel2 };

		int [] selected_id0 = { pselected_id1.id };
		int [] selected_id1 = { pselected_id2.id };


		switch (w.as_replaceveh_d().vehicletype) {
		case Vehicle.VEH_Train: {
			/* RailType */ int railtype = _railtype_selected_in_replace_gui;
			Gfx.DrawString(157, 99 + (14 * w.vscroll.getCap()), _rail_types_list[railtype], 0x10);
			/* draw sorting criteria string */

			/* Ensure that custom engines which substituted wagons
			 * are sorted correctly.
			 * XXX - DO NOT EVER DO THIS EVER AGAIN! GRRR hacking in wagons as
			 * engines to get more types.. Stays here until we have our own format
			 * then it is exit!!! */
			train_engine_drawing_loop(x,  y,  pos,  sel0, selected_id0, railtype, w.vscroll.getCap(), true, false, true); // True engines
			train_engine_drawing_loop(x2, y2, pos2, sel1, selected_id1, railtype, w.vscroll.getCap(), true, false, false); // True engines
			train_engine_drawing_loop(x2, y2, pos2, sel1, selected_id1, railtype, w.vscroll.getCap(), false, false, false); // Feeble wagons
			break;
		}

		case Vehicle.VEH_Road: {
			int num = Global.NUM_ROAD_ENGINES;
			int engine_id = Global.ROAD_ENGINES_INDEX;
			int cargo;

			if (selected_id0[0] >= Global.ROAD_ENGINES_INDEX && selected_id0[0] < Global.SHIP_ENGINES_INDEX) {
				cargo = Engine.RoadVehInfo(selected_id0[0]).cargo_type;

				do {
					final Engine  e = Engine.GetEngine( engine_id );

					if (_player_num_engines[engine_id] > 0 || p.EngineHasReplacement(EngineID.get( engine_id ))) {
						if (BitOps.IS_INT_INSIDE(--pos[0], -w.vscroll.getCap(), 0)) {
							Gfx.DrawString(x[0]+59, y[0]+2, Engine.GetCustomEngineName(engine_id), sel0[0]==0 ? 0xC : 0x10);
							RoadVehCmd.DrawRoadVehEngine(x[0]+29, y[0]+6, engine_id,
									_player_num_engines[engine_id] > 0 ? Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)) : Sprite.PALETTE_CRASH);
							Global.SetDParam(0, _player_num_engines[engine_id]);
							Gfx.DrawStringRightAligned(213, y[0]+5, Str.STR_TINY_BLACK, 0);
							y[0] += 14;
						}
						sel0[0]--;
					}

					if (Engine.RoadVehInfo(engine_id).cargo_type == cargo && e.isAvailableToMe()) {
						if (BitOps.IS_INT_INSIDE(--pos2[0], -w.vscroll.getCap(), 0) && Engine.RoadVehInfo(engine_id).cargo_type == cargo) {
							Gfx.DrawString(x2[0]+59, y2[0]+2, Engine.GetCustomEngineName(engine_id), sel1[0]==0 ? 0xC : 0x10);
							RoadVehCmd.DrawRoadVehEngine(x2[0]+29, y2[0]+6, engine_id, Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)));
							y2[0] += 14;
						}
						sel1[0]--;
					}
					++engine_id;
					//++e;
				} while (--num > 0);
			}
			break;
		}

		case Vehicle.VEH_Ship: {
			int num = Global.NUM_SHIP_ENGINES;
			EngineID engine_id = EngineID.get( Global.SHIP_ENGINES_INDEX );
			int cargo, refittable;

			if (selected_id0[0] != Engine.INVALID_ENGINE_ID.id) {
				cargo = Engine.ShipVehInfo(selected_id0[0]).cargo_type;
				refittable = Engine.ShipVehInfo(selected_id0[0]).refittable;

				do {
					final Engine  e = Engine.GetEngine(engine_id);

					if (_player_num_engines[engine_id.id] > 0 || p.EngineHasReplacement(engine_id)) {
						if (BitOps.IS_INT_INSIDE(--pos[0], -w.vscroll.getCap(), 0)) {
							Gfx.DrawString(x[0]+75, y[0]+7, Engine.GetCustomEngineName(engine_id.id), sel0[0]==0 ? 0xC : 0x10);
							Ship.DrawShipEngine(x[0]+35, y[0]+10,  engine_id.id ,
									_player_num_engines[engine_id.id] > 0 ? 
											Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)) : 
												Sprite.PALETTE_CRASH);
							Global.SetDParam(0, _player_num_engines[engine_id.id]);
							Gfx.DrawStringRightAligned(213, y[0]+15, Str.STR_TINY_BLACK, 0);
							y[0] += 24;
						}
						sel0[0]--;
					}
					if (selected_id0[0] != Engine.INVALID_ENGINE_ID.id) {
						if (e.isAvailableToMe() && 
								( cargo == Engine.ShipVehInfo(engine_id.id).cargo_type || 0 != (refittable & Engine.ShipVehInfo(engine_id.id).refittable) )) 
						{
							if (BitOps.IS_INT_INSIDE(--pos2[0], -w.vscroll.getCap(), 0)) {
								Gfx.DrawString(x2[0]+75, y2[0]+7, Engine.GetCustomEngineName(engine_id.id), sel1[0]==0 ? 0xC : 0x10);
								Ship.DrawShipEngine(x2[0]+35, y2[0]+10, engine_id.id, Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)));
								y2[0] += 24;
							}
							sel1[0]--;
						}
					}
					//++engine_id; //, ++e,
					engine_id = EngineID.get(engine_id.id + 1);
				} while (--num > 0);
			}
			break;
		}   //end of ship

		case Vehicle.VEH_Aircraft: {
			if (selected_id0[0] != Engine.INVALID_ENGINE_ID.id) {
				int num = Global.NUM_AIRCRAFT_ENGINES;
				//final Engine  e = GetEngine(Global.AIRCRAFT_ENGINES_INDEX);
				//EngineID engine_id = EngineID.get( Global.AIRCRAFT_ENGINES_INDEX );
				int eid = Global.AIRCRAFT_ENGINES_INDEX;
				int subtype = Engine.AircraftVehInfo(selected_id0[0]).subtype;

				do {
					final Engine  e = Engine.GetEngine(eid);//engine_id);
					EngineID engine_id = EngineID.get( eid );

					if (_player_num_engines[engine_id.id] > 0 || p.EngineHasReplacement(engine_id)) {
						if (sel0[0] == 0) selected_id0[0] = engine_id.id;
						if (BitOps.IS_INT_INSIDE(--pos[0], -w.vscroll.getCap(), 0)) {
							Gfx.DrawString(x[0]+62, y[0]+7, Engine.GetCustomEngineName(engine_id.id), sel0[0]==0 ? 0xC : 0x10);
							AirCraft.DrawAircraftEngine(x[0]+29, y[0]+10, engine_id.id, _player_num_engines[engine_id.id] > 0 ? Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)) : Sprite.PALETTE_CRASH);
							Global.SetDParam(0, _player_num_engines[engine_id.id]);
							Gfx.DrawStringRightAligned(213, y[0]+15, Str.STR_TINY_BLACK, 0);
							y[0] += 24;
						}
						sel0[0]--;
					}
					if (BitOps.HASBIT(subtype, 0) == BitOps.HASBIT(Engine.AircraftVehInfo(engine_id.id).subtype, 0) 
							&& e.isAvailableToMe()) {
						if (sel1[0] == 0) selected_id1[0] = engine_id.id;
						if (BitOps.IS_INT_INSIDE(--pos2[0], -w.vscroll.getCap(), 0)) {
							Gfx.DrawString(x2[0]+62, y2[0]+7, Engine.GetCustomEngineName(engine_id.id), sel1[0]==0 ? 0xC : 0x10);
							AirCraft.DrawAircraftEngine(x2[0]+29, y2[0]+10, engine_id.id, Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)));
							y2[0] += 24;
						}
						sel1[0]--;
					}
					//++engine_id; //, ++e,
					++ eid;
				} while (--num > 0);
			}
			break;
		}   // end of aircraft
		}
	}


	private static final int _vehicle_type_names[] = {
			Str.STR_019F_TRAIN,
			Str.STR_019C_ROAD_VEHICLE,
			Str.STR_019E_SHIP,
			Str.STR_019D_AIRCRAFT
	};

	static void ReplaceVehicleWndProc(Window w, WindowEvent e)
	{
		final Player p = Player.GetPlayer(Global.gs._local_player);

		switch (e.event) {
		case WE_PAINT: {
			int pos = w.vscroll.pos;
			EngineID selected_id[] = { Engine.INVALID_ENGINE_ID, Engine.INVALID_ENGINE_ID };
			int x = 1;
			int y = 15;
			int pos2 = w.vscroll2.pos;
			int x2 = 1 + 228;
			int y2 = 15;
			int [] sel = new int[2];
			sel[0] = w.as_replaceveh_d().sel_index[0];
			sel[1] = w.as_replaceveh_d().sel_index[1];

			{
				int i;

				for (i = 0; i < _player_num_engines.length; i++) {
					_player_num_engines[i] = 0;
				}

				Iterator<Vehicle> ii = Vehicle.getIterator();
				while(ii.hasNext())
				{
					Vehicle vehicle = ii.next();
					
					if (vehicle.getOwner().isLocalPlayer()) 
					{
						if (vehicle.getType() == Vehicle.VEH_Aircraft && vehicle.getSubtype() > 2) continue;

						// do not count the vehicles, that contains only 0 in all var
						if (vehicle.getEngine_type() == null && vehicle.getSpritenum() == 0) continue;

						if (vehicle.getType() != Engine.GetEngine(vehicle.getEngine_type()).getType()) continue;

						_player_num_engines[vehicle.getEngine_type().id]++;
					}
				}
			}

			SetupScrollStuffForReplaceWindow(w);

			selected_id[0] = w.as_replaceveh_d().sel_engine[0];
			selected_id[1] = w.as_replaceveh_d().sel_engine[1];

			// sets the selected left item to the top one if it's greater than the number of vehicles in the left side

			if (w.as_replaceveh_d().count[0] <= sel[0]) {
				if (w.as_replaceveh_d().count[0] != 0) {
					sel[0] = 0;
					w.as_replaceveh_d().sel_index[0] = 0;
					w.vscroll.pos = 0;
					// now we go back to set selected_id[1] properly
					w.SetWindowDirty();
					return;
				} else { //there are no vehicles in the left window
					selected_id[1] = Engine.INVALID_ENGINE_ID;
				}
			}

			if (w.as_replaceveh_d().count[1] <= sel[1]) {
				if (w.as_replaceveh_d().count[1] != 0) {
					sel[1] = 0;
					w.as_replaceveh_d().sel_index[1] = 0;
					w.vscroll2.pos = 0;
					// now we go back to set selected_id[1] properly
					w.SetWindowDirty();
					return;
				} else { //there are no vehicles in the right window
					selected_id[1] = Engine.INVALID_ENGINE_ID;
				}
			}

			// Disable the "Start Replacing" button if:
			//    Either list is empty
			// or Both lists have the same vehicle selected
			// or The selected replacement engine has a replacement (to prevent loops)
			// or The right list (new replacement) has the existing replacement vehicle selected
			if (selected_id[0] == Engine.INVALID_ENGINE_ID ||
					selected_id[1] == Engine.INVALID_ENGINE_ID ||
					selected_id[0].equals(selected_id[1]) ||
					p.EngineReplacement(selected_id[1]) != Engine.INVALID_ENGINE_ID ||
					p.EngineReplacement(selected_id[0]).equals(selected_id[1])) 
			{
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 4);
			} else {
				w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 4);
			}

			// Disable the "Stop Replacing" button if:
			//    The left list (existing vehicle) is empty
			// or The selected vehicle has no replacement set up
			if (selected_id[0] == Engine.INVALID_ENGINE_ID ||
					!p.EngineHasReplacement(selected_id[0])) {
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 6);
			} else {
				w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 6);
			}

			// now the actual drawing of the window itself takes place
			Global.SetDParam(0, _vehicle_type_names[w.as_replaceveh_d().vehicletype - Vehicle.VEH_Train]);

			if (w.as_replaceveh_d().vehicletype == Vehicle.VEH_Train) {
				// set on/off for renew_keep_length
				Global.SetDParam(1, p.isRenew_keep_length() ? Str.STR_CONFIG_PATCHES_ON : Str.STR_CONFIG_PATCHES_OFF);
			}

			w.DrawWindowWidgets();

			// sets up the string for the vehicle that is being replaced to
			if (selected_id[0] != Engine.INVALID_ENGINE_ID) {
				if (!p.EngineHasReplacement(selected_id[0])) {
					Global.SetDParam(0, Str.STR_NOT_REPLACING);
				} else {
					Global.SetDParam(0, Engine.GetCustomEngineName(p.EngineReplacement(selected_id[0]).id ).id);
				}
			} else {
				Global.SetDParam(0, Str.STR_NOT_REPLACING_VEHICLE_SELECTED);
			}

			Gfx.DrawString(145, (w.resize.step_height == 24 ? 67 : 87) + w.resize.step_height * w.vscroll.getCap(), Str.STR_02BD, 0x10);

			/*	now we draw the two arrays according to what we just counted */
			DrawEngineArrayInReplaceWindow(w, x, y, x2, y2, pos, pos2, sel[0], sel[1], selected_id[0], selected_id[1]);

			w.as_replaceveh_d().sel_engine[0] = selected_id[0];
			w.as_replaceveh_d().sel_engine[1] = selected_id[1];
			/* now we draw the info about the vehicles we selected */
			switch (w.as_replaceveh_d().vehicletype) {
			case Vehicle.VEH_Train: {
				byte i = 0;
				int offset = 0;

				for (i = 0 ; i < 2 ; i++) {
					if (i > 0) offset = 228;
					if (selected_id[i] != Engine.INVALID_ENGINE_ID) {
						if (!Engine.RailVehInfo(selected_id[i].id).isWagon()) {
							/* it's an engine */
							TrainGui.DrawTrainEnginePurchaseInfo(2 + offset, 15 + (14 * w.vscroll.getCap()), selected_id[i].id);
						} else {
							/* it's a wagon. Train cars are not replaced with the current GUI, but this code is ready for newgrf if anybody adds that*/
							TrainGui.DrawTrainWagonPurchaseInfo(2 + offset, 15 + (14 * w.vscroll.getCap()), selected_id[i].id);
						}
					}
				}
				break;
			}   //end if case  Vehicle.VEH_Train

			case Vehicle.VEH_Road: {
				if (selected_id[0] != Engine.INVALID_ENGINE_ID) {
					RoadVehGui.DrawRoadVehPurchaseInfo(2, 15 + (14 * w.vscroll.getCap()), selected_id[0].id);
					if (selected_id[1] != Engine.INVALID_ENGINE_ID) {
						RoadVehGui.DrawRoadVehPurchaseInfo(2 + 228, 15 + (14 * w.vscroll.getCap()), selected_id[1].id);
					}
				}
				break;
			}   // end of Vehicle.VEH_Road

			case Vehicle.VEH_Ship: {
				if (selected_id[0] != Engine.INVALID_ENGINE_ID) {
					ShipGui.DrawShipPurchaseInfo(2, 15 + (24 * w.vscroll.getCap()), selected_id[0].id);
					if (selected_id[1] != Engine.INVALID_ENGINE_ID) {
						ShipGui.DrawShipPurchaseInfo(2 + 228, 15 + (24 * w.vscroll.getCap()), selected_id[1].id);
					}
				}
				break;
			}   // end of Vehicle.VEH_Ship

			case Vehicle.VEH_Aircraft: {
				if (selected_id[0] != Engine.INVALID_ENGINE_ID) {
					AirCraft.DrawAircraftPurchaseInfo(2, 15 + (24 * w.vscroll.getCap()), selected_id[0]);
					if (selected_id[1] != Engine.INVALID_ENGINE_ID) {
						AirCraft.DrawAircraftPurchaseInfo(2 + 228, 15 + (24 * w.vscroll.getCap()), selected_id[1]);
					}
				}
				break;
			}   // end of Vehicle.VEH_Aircraft
			}
		} break;   // end of paint

		case WE_CLICK: {
			// these 3 variables is used if any of the lists is clicked
			int click_scroll_pos = w.vscroll2.pos;
			int click_scroll_cap = w.vscroll2.getCap();
			byte click_side = 1;

			switch (e.widget) {
			case 14: case 15: { /* Select sorting criteria dropdown menu */
				Window.ShowDropDownMenu(w, _rail_types_list, _railtype_selected_in_replace_gui, 15, 0, ~Player.GetPlayer(Global.gs._local_player).avail_railtypes);
				break;
			}
			case 17: { /* toggle renew_keep_length */
				Cmd.DoCommandP( null, 5, p.isRenew_keep_length() ? 0 : 1, null, Cmd.CMD_REPLACE_VEHICLE);
			} break;
			case 4: { /* Start replacing */
				EngineID veh_from = w.as_replaceveh_d().sel_engine[0];
				EngineID veh_to = w.as_replaceveh_d().sel_engine[1];
				Cmd.DoCommandP( null, 3, veh_from.id + (veh_to.id << 16), null, Cmd.CMD_REPLACE_VEHICLE);
				w.SetWindowDirty();
				break;
			}

			case 6: { /* Stop replacing */
				EngineID veh_from = w.as_replaceveh_d().sel_engine[0];
				Cmd.DoCommandP( null, 3, veh_from.id + (Engine.INVALID_ENGINE << 16), null, Cmd.CMD_REPLACE_VEHICLE);
				w.SetWindowDirty();
				break;
			}

			case 7:
				// sets up that the left one was clicked. The default values are for the right one (9)
				// this way, the code for 9 handles both sides
				click_scroll_pos = w.vscroll.pos;
				click_scroll_cap = w.vscroll.getCap();
				click_side = 0;
			case 9: {
				int i = (e.pt.y - 14) / w.resize.step_height;
				if (i < click_scroll_cap) {
					w.as_replaceveh_d().sel_index[click_side] = i + click_scroll_pos;
					w.SetWindowDirty();
				}
			} break;
			}

		} break;

		case WE_DROPDOWN_SELECT: { /* we have selected a dropdown item in the list */
			_railtype_selected_in_replace_gui = e.index;
			w.SetWindowDirty();
		} break;

		case WE_RESIZE: {
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / w.resize.step_height);
			w.vscroll2.setCap(w.vscroll2.getCap() + e.diff.y / w.resize.step_height);

			w.widget.get(7).unkA = (w.vscroll.getCap()  << 8) + 1;
			w.widget.get(9).unkA = (w.vscroll2.getCap() << 8) + 1;
		} break;
		default:
			break;
		}
	}

	static final Widget _replace_rail_vehicle_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,       Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   443,     0,    13, Str.STR_REPLACE_VEHICLES_WHITE, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,    14,   444,   455,     0,    13, Str.STR_NULL,       Str.STR_STICKY_BUTTON),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,     0,   227,   126,   197, Str.STR_NULL,       Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   138,   210,   221, Str.STR_REPLACE_VEHICLES_START, Str.STR_REPLACE_HELP_START_BUTTON),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,   139,   316,   198,   209, Str.STR_NULL,       Str.STR_REPLACE_HELP_REPLACE_INFO_TAB),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   306,   443,   210,   221, Str.STR_REPLACE_VEHICLES_STOP,  Str.STR_REPLACE_HELP_STOP_BUTTON),
			new Widget(     Window.WWT_MATRIX, Window.RESIZE_BOTTOM,    14,     0,   215,    14,   125, 0x801,          Str.STR_REPLACE_HELP_LEFT_ARRAY),
			new Widget(  Window.WWT_SCROLLBAR, Window.RESIZE_BOTTOM,    14,   216,   227,    14,   125, Str.STR_NULL,       Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(     Window.WWT_MATRIX, Window.RESIZE_BOTTOM,    14,   228,   443,    14,   125, 0x801,          Str.STR_REPLACE_HELP_RIGHT_ARRAY),
			new Widget( Window.WWT_SCROLL2BAR, Window.RESIZE_BOTTOM,    14,   444,   455,    14,   125, Str.STR_NULL,       Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,   228,   455,   126,   197, Str.STR_NULL,       Str.STR_NULL),
			// train specific stuff
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,     0,   138,   198,   209, Str.STR_NULL,       Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,   139,   153,   210,   221, Str.STR_NULL,       Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,   154,   277,   210,   221, Str.STR_NULL,       Str.STR_REPLACE_HELP_RAILTYPE),
			new Widget(    Window.WWT_TEXTBTN,     Window.RESIZE_TB,    14,   278,   289,   210,   221, Str.STR_0225,       Str.STR_REPLACE_HELP_RAILTYPE),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,   290,   305,   210,   221, Str.STR_NULL,       Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   317,   455,   198,   209, Str.STR_REPLACE_REMOVE_WAGON,       Str.STR_REPLACE_REMOVE_WAGON_HELP),
			// end of train specific stuff
			new Widget(  Window.WWT_RESIZEBOX,     Window.RESIZE_TB,    14,   444,   455,   210,   221, Str.STR_NULL,       Str.STR_RESIZE_BUTTON),
			//{   WIDGETS_END},
	};

	static final Widget _replace_road_vehicle_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,        Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   443,     0,    13, Str.STR_REPLACE_VEHICLES_WHITE,  Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,    14,   444,   455,     0,    13, Str.STR_NULL,       Str.STR_STICKY_BUTTON),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,     0,   227,   126,   197, Str.STR_NULL,       Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   138,   198,   209, Str.STR_REPLACE_VEHICLES_START,  Str.STR_REPLACE_HELP_START_BUTTON),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,   139,   305,   198,   209, Str.STR_NULL,       Str.STR_REPLACE_HELP_REPLACE_INFO_TAB),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   306,   443,   198,   209, Str.STR_REPLACE_VEHICLES_STOP,   Str.STR_REPLACE_HELP_STOP_BUTTON),
			new Widget(     Window.WWT_MATRIX, Window.RESIZE_BOTTOM,    14,     0,   215,    14,   125, 0x801,          Str.STR_REPLACE_HELP_LEFT_ARRAY),
			new Widget(  Window.WWT_SCROLLBAR, Window.RESIZE_BOTTOM,    14,   216,   227,    14,   125, Str.STR_NULL,       Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(     Window.WWT_MATRIX, Window.RESIZE_BOTTOM,    14,   228,   443,    14,   125, 0x801,          Str.STR_REPLACE_HELP_RIGHT_ARRAY),
			new Widget( Window.WWT_SCROLL2BAR, Window.RESIZE_BOTTOM,    14,   444,   455,    14,   125, Str.STR_NULL,       Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,   228,   455,   126,   197, Str.STR_NULL,       Str.STR_NULL),
			new Widget(  Window.WWT_RESIZEBOX,     Window.RESIZE_TB,    14,   444,   455,   198,   209, Str.STR_NULL,       Str.STR_RESIZE_BUTTON),

	};

	static final Widget _replace_ship_aircraft_vehicle_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,       Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   443,     0,    13, Str.STR_REPLACE_VEHICLES_WHITE,  Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,    14,   444,   455,     0,    13, Str.STR_NULL,       Str.STR_STICKY_BUTTON),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,     0,   227,   110,   161, Str.STR_NULL,       Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   138,   162,   173, Str.STR_REPLACE_VEHICLES_START,  Str.STR_REPLACE_HELP_START_BUTTON),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,   139,   305,   162,   173, Str.STR_NULL,       Str.STR_REPLACE_HELP_REPLACE_INFO_TAB),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   306,   443,   162,   173, Str.STR_REPLACE_VEHICLES_STOP,   Str.STR_REPLACE_HELP_STOP_BUTTON),
			new Widget(     Window.WWT_MATRIX, Window.RESIZE_BOTTOM,    14,     0,   215,    14,   109, 0x401,          Str.STR_REPLACE_HELP_LEFT_ARRAY),
			new Widget(  Window.WWT_SCROLLBAR, Window.RESIZE_BOTTOM,    14,   216,   227,    14,   109, Str.STR_NULL,       Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(     Window.WWT_MATRIX, Window.RESIZE_BOTTOM,    14,   228,   443,    14,   109, 0x401,          Str.STR_REPLACE_HELP_RIGHT_ARRAY),
			new Widget( Window.WWT_SCROLL2BAR, Window.RESIZE_BOTTOM,    14,   444,   455,    14,   109, Str.STR_NULL,       Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,   228,   455,   110,   161, Str.STR_NULL,       Str.STR_NULL),
			new Widget(  Window.WWT_RESIZEBOX,     Window.RESIZE_TB,    14,   444,   455,   162,   173, Str.STR_NULL,       Str.STR_RESIZE_BUTTON),

	};

	static final WindowDesc _replace_rail_vehicle_desc = new WindowDesc(
			-1, -1, 456, 222,
			Window.WC_REPLACE_VEHICLE,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_replace_rail_vehicle_widgets,
			VehicleGui::ReplaceVehicleWndProc
			);

	static final WindowDesc _replace_road_vehicle_desc = new WindowDesc(
			-1, -1, 456, 210,
			Window.WC_REPLACE_VEHICLE,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_replace_road_vehicle_widgets,
			VehicleGui::ReplaceVehicleWndProc
			);

	static final WindowDesc _replace_ship_aircraft_vehicle_desc = new WindowDesc(
			-1, -1, 456, 174,
			Window.WC_REPLACE_VEHICLE,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_replace_ship_aircraft_vehicle_widgets,
			VehicleGui::ReplaceVehicleWndProc
			);


	public static void ShowReplaceVehicleWindow(int vehicletype)
	{
		Window w;

		Window.DeleteWindowById(Window.WC_REPLACE_VEHICLE, vehicletype);

		switch (vehicletype) {
		case Vehicle.VEH_Train:
			w = Window.AllocateWindowDescFront(_replace_rail_vehicle_desc, vehicletype);
			w.vscroll.setCap(8);
			w.resize.step_height = 14;
			break;
		case Vehicle.VEH_Road:
			w = Window.AllocateWindowDescFront(_replace_road_vehicle_desc, vehicletype);
			w.vscroll.setCap(8);
			w.resize.step_height = 14;
			break;
		case Vehicle.VEH_Ship:
		case Vehicle.VEH_Aircraft:
			w = Window.AllocateWindowDescFront(_replace_ship_aircraft_vehicle_desc, vehicletype);
			w.vscroll.setCap(4);
			w.resize.step_height = 24;
			break;
		default: return;
		}
		w.caption_color = (byte) Global.gs._local_player.id;
		w.as_replaceveh_d().vehicletype = vehicletype;
		w.vscroll2.setCap(w.vscroll.getCap());   // these two are always the same
	}

	public static void InitializeGUI()
	{
		//memset(&_sorting, 0, sizeof(_sorting));
	}

	/** Assigns an already open vehicle window to a new vehicle.
	 * Assigns an already open vehicle window to a new vehicle. If the vehicle got
	 * any sub window open (orders and so on) it will change owner too.
	 * @param from_v the current owner of the window
	 * @param to_v the new owner of the window
	 */
	public static void ChangeVehicleViewWindow(final Vehicle from_v, final Vehicle to_v)
	{
		Window w;

		w = Window.FindWindowById(Window.WC_VEHICLE_VIEW, from_v.index);
		if (w != null) {
			w.window_number = to_v.index;
			w.as_vp_d().follow_vehicle = to_v.index;
			w.SetWindowDirty();

			w = Window.FindWindowById(Window.WC_VEHICLE_ORDERS, from_v.index);
			if (w != null) {
				w.window_number =  to_v.index;
				w.SetWindowDirty();
			}

			w = Window.FindWindowById(Window.WC_VEHICLE_REFIT, from_v.index);
			if (w != null) {
				w.window_number = to_v.index;
				w.SetWindowDirty();
			}

			w = Window.FindWindowById(Window.WC_VEHICLE_DETAILS, from_v.index);
			if (w != null) {
				w.window_number = to_v.index;
				w.SetWindowDirty();
			}
		}
	}

}





