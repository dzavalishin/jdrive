package com.dzavalishin.xui;

import java.util.Iterator;

import com.dzavalishin.enums.TransportType;
import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Depot;
import com.dzavalishin.game.Engine;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.Rail;
import com.dzavalishin.game.RailVehicleInfo;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.TrainCmd;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.struct.GetDepotVehiclePtData;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.YearMonthDay;
import com.dzavalishin.wcustom.vehiclelist_d;

public class TrainGui 
{



	public static int _traininfo_vehicle_pitch = 0;

	/**
	 * Draw the purchase info details of train engine at a given location.
	 * @param x,y location where to draw the info
	 * @param engine_number the engine of which to draw the info of
	 */
	static void DrawTrainEnginePurchaseInfo(int x, int y, /*EngineID*/ int engine_number)
	{
		final RailVehicleInfo rvi = Engine.RailVehInfo(engine_number);
		final Engine  e = Engine.GetEngine(engine_number);
		int multihead = rvi.isMulttihead() ? 1:0;
		YearMonthDay ymd = new YearMonthDay(e.getIntro_date());
		//YearMonthDay.ConvertDayToYMD(ymd, e.getIntro_date());

		/* Purchase Cost - Engine weight */
		Global.SetDParam(0, ((int)(rvi.base_cost * (Global._price.build_railvehicle / 8))) >> 5);
		Global.SetDParam(1, rvi.weight << multihead);
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_COST_WEIGHT, 0);
		y += 10;

		/* Max speed - Engine power */
		Global.SetDParam(0, rvi.getMax_speed() * 10 >> 4);
		Global.SetDParam(1, rvi.power << multihead);
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_SPEED_POWER, 0);
		y += 10;

		/* Running cost */
		Global.SetDParam(0, ((int)((rvi.running_cost_base * Global._price.running_rail[rvi.engclass] / 256))) << multihead);
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_RUNNINGCOST, 0);
		y += 10;

		/* Powered wagons power - Powered wagons extra weight */
		if (rvi.pow_wag_power != 0) {
			Global.SetDParam(0, rvi.pow_wag_power);
			Global.SetDParam(1, rvi.pow_wag_weight);
			Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_PWAGPOWER_PWAGWEIGHT, 0);
			y += 10;
		}

		/* Cargo type + capacity, or N/A */
		Global.SetDParam(0, Str.STR_8838_N_A);
		Global.SetDParam(2, Str.STR_EMPTY);
		if (rvi.capacity != 0) {
			Global.SetDParam(0, Global._cargoc.names_long[rvi.cargo_type]);
			Global.SetDParam(1, rvi.capacity << multihead);
			Global.SetDParam(2, Str.STR_9842_REFITTABLE);
		}
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_CAPACITY, 0);
		y += 10;

		/* Design date - Life length */
		Global.SetDParam(0, ymd.year + 1920);
		Global.SetDParam(1, e.getLifelength());
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_DESIGNED_LIFE, 0);
		y += 10;

		/* Reliability */
		Global.SetDParam(0, e.getReliability() * 100 >> 16);
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_RELIABILITY, 0);
		y += 10;
	}

	/**
	 * Draw the purchase info details of a train wagon at a given location.
	 * @param x,y location where to draw the info
	 * @param engine_number the engine of which to draw the info of
	 */
	static void DrawTrainWagonPurchaseInfo(int x, int y, /*EngineID*/int engine_number)
	{
		final RailVehicleInfo rvi = Engine.RailVehInfo(engine_number);
		boolean refittable = (Global._engine_info[engine_number].refit_mask != 0);

		/* Purchase cost */
		Global.SetDParam(0, (rvi.base_cost * Global._price.build_railwagon) / 256);
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_COST, 0);
		y += 10;

		/* Wagon weight - (including cargo) */
		Global.SetDParam(0, rvi.weight);
		Global.SetDParam(1, (Global._cargoc.weights[rvi.cargo_type] * rvi.capacity >> 4) + rvi.weight);
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_WEIGHT_CWEIGHT, 0);
		y += 10;

		/* Cargo type + capacity */
		Global.SetDParam(0, Global._cargoc.names_long[rvi.cargo_type]);
		Global.SetDParam(1, rvi.capacity);
		Global.SetDParam(2, refittable ? Str.STR_9842_REFITTABLE : Str.STR_EMPTY);
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_CAPACITY, 0);
		y += 10;

		/* Wagon speed limit, displayed if above zero */
		if (rvi.getMax_speed() > 0 && Global._patches.wagon_speed_limits) {
			Global.SetDParam(0, rvi.getMax_speed() * 10 >> 4);
			Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_SPEED, 0);
			y += 10;
		}
	}

	public static void CcBuildWagon(boolean success, TileIndex tile, int p1, int p2)
	{
		Vehicle found;

		if (!success)
			return;

		// find a locomotive in the depot.
		found = null;

		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();
		
			if (v.getType() == Vehicle.VEH_Train && v.IsFrontEngine() &&
					v.getTile().equals(tile) &&
					v.rail.isInDepot()) {
				if (found != null) // must be exactly one.
					return;
				found = v;
			}
		}

		// if we found a loco,
		if (found != null) {
			found = found.GetLastVehicleInChain();
			// put the new wagon at the end of the loco.
			Cmd.DoCommandP(null, Global._new_wagon_id.id | (found.index<<16), 0, null, Cmd.CMD_MOVE_RAIL_VEHICLE);
			VehicleGui.RebuildVehicleLists();
		}
	}

	public static void CcBuildLoco(boolean success, TileIndex tile, int p1, int p2)
	{
		final Vehicle  v;

		if (!success) return;

		v = Vehicle.GetVehicle(Global._new_train_id);
		if (tile.equals(Global._backup_orders_tile)) {
			Global._backup_orders_tile = null;
			Vehicle.RestoreVehicleOrders(v, Global._backup_orders_data[0]);
		}
		ShowTrainViewWindow(v);
	}

	public static void CcCloneTrain(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) ShowTrainViewWindow(Vehicle.GetVehicle(Global._new_train_id));
	}

	static void engine_drawing_loop(int [] x, int [] y, int [] pos, int [] sel,
		/*EngineID*/ int [] selected_id, /*RailType*/ int railtype, int show_max, boolean is_engine)
	{
		//EngineID
		int i;

		for (i = 0; i < Global.NUM_TRAIN_ENGINES; i++) {
			final Engine e = Engine.GetEngine(i);
			final RailVehicleInfo rvi = Engine.RailVehInfo(i);

			if (!Rail.IsCompatibleRail(e.getRailtype(), railtype) || (!rvi.isWagon()) != is_engine ||
					!e.isAvailableToMe())
				continue;

			if (sel[0] == 0)
				selected_id[0] = i;

			if (BitOps.IS_INT_INSIDE(--pos[0], -show_max, 0)) {
				Gfx.DrawString(x[0] + 59, y[0] + 2, Engine.GetCustomEngineName(i), sel[0] == 0 ? 0xC : 0x10);
				TrainCmd.DrawTrainEngine(x[0] + 29, y[0] + 6, i,
					Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)));
				y[0] += 14;
			}
			--sel[0];
		}
	}

	static void NewRailVehicleWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT:

			if (w.window_number == 0) // [dz] was null - ok with 0?
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 5);

			{
				int count = 0;
				/*RailType*/ int railtype = w.as_buildtrain_d().railtype;
				//EngineID 
				int i;

				for (i = 0; i < Global.NUM_TRAIN_ENGINES; i++) {
					final Engine ee = Engine.GetEngine(i);
					if (Rail.IsCompatibleRail(ee.getRailtype(), railtype)
					    && ee.isAvailableToMe())
						count++;
				}
				w.SetVScrollCount( count);
			}

			Global.SetDParam(0, w.as_buildtrain_d().railtype + Str.STR_881C_NEW_RAIL_VEHICLES);
			w.DrawWindowWidgets();

			{
				/*RailType*/ int railtype = w.as_buildtrain_d().railtype;
				int [] sel = { w.as_buildtrain_d().sel_index };
				int [] pos = { w.vscroll.pos };
				int [] x = { 1 };
				int [] y = { 15 };
				//EngineID selected_id = Engine.INVALID_ENGINE;
				int [] selected_id = { Engine.INVALID_ENGINE };

				/* Ensure that custom engines which substituted wagons
				 * are sorted correctly.
				 * XXX - DO NOT EVER DO THIS EVER AGAIN! GRRR hacking in wagons as
				 * engines to get more types.. Stays here until we have our own format
				 * then it is exit!!! */
				engine_drawing_loop(x, y, pos, sel, selected_id, railtype, w.vscroll.getCap(), true); // True engines
				engine_drawing_loop(x, y, pos, sel, selected_id, railtype, w.vscroll.getCap(), false); // Feeble wagons

				w.as_buildtrain_d().sel_engine = selected_id[0];

				if (selected_id[0] != Engine.INVALID_ENGINE) {
					final RailVehicleInfo rvi = Engine.RailVehInfo(selected_id[0]);

					if (!rvi.isWagon()) {
						/* it's an engine */
						DrawTrainEnginePurchaseInfo(2, w.widget.get(4).top + 1,selected_id[0]);
					} else {
						/* it's a wagon */
						DrawTrainWagonPurchaseInfo(2, w.widget.get(4).top + 1, selected_id[0]);
					}
				}
			}
		break;

		case WE_CLICK: {
			switch(e.widget) {
			case 2: {
				int i = (e.pt.y - 14) / 14;
				if (i < w.vscroll.getCap()) {
					w.as_buildtrain_d().sel_index =  (i + w.vscroll.pos);
					w.SetWindowDirty();
				}
			} break;
			case 5: {
				//EngineID 
				int sel_eng = w.as_buildtrain_d().sel_engine;
				if (sel_eng != Engine.INVALID_ENGINE)
					Cmd.DoCommandP(TileIndex.get(w.window_number), sel_eng, 0, 
							(Engine.RailVehInfo(sel_eng).isWagon()) ? TrainGui::CcBuildWagon : TrainGui::CcBuildLoco, Cmd.CMD_BUILD_RAIL_VEHICLE | Cmd.CMD_MSG(Str.STR_882B_CAN_T_BUILD_RAILROAD_VEHICLE));
			}	break;
			case 6: { /* rename */
				//EngineID 
				int sel_eng = w.as_buildtrain_d().sel_engine;
				if (sel_eng != Engine.INVALID_ENGINE) {
					w.as_buildtrain_d().rename_engine = sel_eng;
					MiscGui.ShowQueryString(Engine.GetCustomEngineName(sel_eng),
						new StringID( Str.STR_886A_RENAME_TRAIN_VEHICLE_TYPE ), 31, 160, w.getWindow_class(), w.window_number);
				}
			} break;
			}
		} break;

		case WE_4:
			if (w.window_number != 0 && null == Window.FindWindowById(Window.WC_VEHICLE_DEPOT, w.window_number)) {
				w.DeleteWindow();
			}
			break;

		case WE_ON_EDIT_TEXT: {
			if (e.str != null) {
				Global._cmd_text = e.str;
				Cmd.DoCommandP(null, w.as_buildtrain_d().rename_engine, 0, null,
					Cmd.CMD_RENAME_ENGINE | Cmd.CMD_MSG(Str.STR_886B_CAN_T_RENAME_TRAIN_VEHICLE));
			}
		} break;

		case WE_RESIZE: {
			if (e.diff.y == 0)
				break;

			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / 14);
			w.widget.get(2).unkA = (w.vscroll.getCap() << 8) + 1;
		} break;
		default:
			break;
		}
	}

	static final Widget _new_rail_vehicle_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   227,     0,    13, Str.STR_JUST_STRING,					Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(     Window.WWT_MATRIX, Window.RESIZE_BOTTOM,    14,     0,   215,    14,   125, 0x801,										Str.STR_8843_TRAIN_VEHICLE_SELECTION),
	new Widget(  Window.WWT_SCROLLBAR, Window.RESIZE_BOTTOM,    14,   216,   227,    14,   125, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,     0,   227,   126,   197, 0x0,											Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   107,   198,   209, Str.STR_881F_BUILD_VEHICLE,		Str.STR_8844_BUILD_THE_HIGHLIGHTED_TRAIN),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   108,   215,   198,   209, Str.STR_8820_RENAME,					Str.STR_8845_RENAME_TRAIN_VEHICLE_TYPE),
	new Widget(  Window.WWT_RESIZEBOX,     Window.RESIZE_TB,    14,   216,   227,   198,   209, 0x0,											Str.STR_RESIZE_BUTTON),

	};

	static final WindowDesc _new_rail_vehicle_desc = new WindowDesc(
		-1, -1, 228, 210,
		Window.WC_BUILD_VEHICLE,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
		_new_rail_vehicle_widgets,
		TrainGui::NewRailVehicleWndProc
	);

	static void ShowBuildTrainWindow(TileIndex tile)
	{
		Window w;

		int wn = tile == null ? -1 : tile.getTile();
		
		Window.DeleteWindowById(Window.WC_BUILD_VEHICLE, wn);

		w = Window.AllocateWindowDesc(_new_rail_vehicle_desc);
		w.window_number = wn;
		w.vscroll.setCap(8);
		w.widget.get(2).unkA = (w.vscroll.getCap() << 8) + 1;

		w.resize.step_height = 14;
		w.resize.height = w.height - 14 * 4; /* Minimum of 4 vehicles in the display */

		if (tile != null) {
			w.caption_color = tile.GetTileOwner().id;
			w.as_buildtrain_d().railtype =  BitOps.GB(tile.getMap().m3, 0, 4);
		} else {
			w.caption_color =  Global.gs._local_player.id;
			w.as_buildtrain_d().railtype =  Rail.GetBestRailtype(Player.GetPlayer(Global.gs._local_player));
		}
	}

	/**
	 * Get the number of pixels for the given wagon length.
	 * @param len Length measured in 1/8ths of a standard wagon.
	 * @return Number of pixels across.
	 */
	static int WagonLengthToPixels(int len) {
		return (len * 29) / 8;
	}

	static void DrawTrainImage(Vehicle v, int x, int y, int count, int skip, VehicleID selection)
	{
		int dx = 0;
		count *= 8;

		do {
			if (--skip < 0) {
				int image = TrainCmd.GetTrainImage(v, 6);
				int ormod = Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(v.getOwner()));
				int width = v.rail.getCached_veh_length();

				if (dx + width <= count) {
					if(v.isCrashed())
						ormod = Sprite.PALETTE_CRASH;
					Gfx.DrawSprite(image | ormod, x + 14 + WagonLengthToPixels(dx), y + 6 + (Sprite.is_custom_sprite(Engine.RailVehInfo(v.getEngine_type().id).image_index) ? _traininfo_vehicle_pitch : 0));
					if (v.index == selection.id)
						Gfx.DrawFrameRect(x - 1 + WagonLengthToPixels(dx), y - 1, x + WagonLengthToPixels(dx + width) - 1, y + 12, 15, Window.FR_BORDERONLY);
				}
				dx += width;
			}

			v = v.getNext();
		} while (dx < count && v != null);
	}

	static void DrawTrainDepotWindow(Window w)
	{
		Vehicle u;
		int num,x,y,i, hnum;
		Depot depot;

		TileIndex tile = new TileIndex( w.window_number );

		/* setup disabled buttons */
		w.disabled_state =
				tile.IsTileOwner(Global.gs._local_player) ? 0 : ((1 << 4) | (1 << 5) | (1 << 8) | (1<<9));

		/* determine amount of items for scroller */
		num = 0;
		hnum = 8;

		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();
			if (v.getType() == Vehicle.VEH_Train &&
				  (v.IsFrontEngine() || v.IsFreeWagon()) &&
					v.getTile().equals(tile) &&
					v.rail.isInDepot()) {
				num++;
				// determine number of items in the X direction.
				if (v.IsFrontEngine()) {
					hnum = Math.max(hnum, v.rail.getCached_total_length());
				}
			}
		}

		/* Always have 1 empty row, so people can change the setting of the train */
		num++;

		w.SetVScrollCount( num);
		w.SetHScrollCount((hnum + 7) / 8);

		/* locate the depot struct */
		depot = Depot.GetDepotByTile(tile);
		assert(depot != null);

		Global.SetDParam(0, depot.getTownIndex());
		w.DrawWindowWidgets();

		x = 2;
		y = 15;
		num = w.vscroll.pos;

		// draw all trains
		ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();
			if (v.getType() == Vehicle.VEH_Train && v.IsFrontEngine() &&
					v.getTile().equals(tile) && v.rail.isInDepot() &&
					--num < 0 && num >= -w.vscroll.getCap()) {
				DrawTrainImage(v, x+21, y, w.hscroll.getCap(), w.hscroll.pos, VehicleID.get( w.as_traindepot_d().sel ));
				/* Draw the train number */
				Global.SetDParam(0, v.getUnitnumber().id);
				Gfx.DrawString(x, y, (v.getMax_age() - 366 < v.getAge()) ? Str.STR_00E3 : Str.STR_00E2, 0);

				// Number of wagons relative to a standard length wagon (rounded up)
				Global.SetDParam(0, (v.rail.getCached_total_length() + 7) / 8);
				Gfx.DrawStringRightAligned(w.widget.get(6).right - 1, y + 4, Str.STR_TINY_BLACK, 0);	//Draw the counter

				/* Draw the pretty flag */
				Gfx.DrawSprite( v.isStopped() ? Sprite.SPR_FLAG_VEH_STOPPED : Sprite.SPR_FLAG_VEH_RUNNING, x + 15, y);

				y += 14;
			}
		}

		// draw all remaining vehicles
		ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();
			if (v.getType() == Vehicle.VEH_Train && v.IsFreeWagon() &&
					v.getTile().equals(tile) && v.rail.isInDepot() &&
					--num < 0 && num >= -w.vscroll.getCap()) {
				DrawTrainImage(v, x+50, y, w.hscroll.getCap() - 1, 0, VehicleID.get( w.as_traindepot_d().sel) );
				Gfx.DrawString(x, y+2, Str.STR_8816, 0);

				/*Draw the train counter */
				i = 0;
				u = v;
				do i++; while ( (u=u.getNext()) != null);		//Determine length of train
				Global.SetDParam(0, i);				//Set the counter
				Gfx.DrawStringRightAligned(w.widget.get(6).right - 1, y + 4, Str.STR_TINY_BLACK, 0);	//Draw the counter
				y += 14;
			}
		}
	}

	
	private static int found_it(GetDepotVehiclePtData d, Vehicle v, int x, int skip)
	{
		d.head = d.wagon = v;

		/* either pressed the flag or the number, but only when it's a loco */
		if (x < 0 && v.IsFrontEngine())
			return (x >= -10) ? -2 : -1;

		// skip vehicles that are scrolled off the left side
		while (skip-- > 0) v = v.getNext();

		/* find the vehicle in this row that was clicked */
		while ((x -= WagonLengthToPixels(v.rail.getCached_veh_length())) >= 0) {
			v = v.getNext();
			if (v == null) break;
		}

		// if an articulated part was selected, find its parent
		while (v != null && v.IsArticulatedPart()) 
			v = v.GetPrevVehicleInChain();

		d.wagon = v;
		return 0;		
	}

	static int GetVehicleFromTrainDepotWndPt(final Window w, int x, int y, GetDepotVehiclePtData d)
	{
		int row;
		int skip = 0;

		x = x - 23;

		row = (y - 14) / 14;
		if(row >= w.vscroll.getCap())
			return 1; /* means err */

		row += w.vscroll.pos;

		/* go through all the locomotives */
		Iterator<Vehicle> ii;
		
		ii = Vehicle.getIterator();
		while(ii.hasNext())
		{		
			Vehicle v = ii.next();
			if (v.getType() == Vehicle.VEH_Train &&
					v.IsFrontEngine() &&
					v.getTile().getTile() == w.window_number &&
					v.rail.isInDepot() &&
					--row < 0) {
						skip = w.hscroll.pos;
						//goto found_it;
						return found_it(d,v,x,skip);
			}
		}

		x -= 29; /* free wagons don't have an initial loco. */

		/* and then the list of free wagons */
		ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();
			if (v.getType() == Vehicle.VEH_Train &&
					v.IsFreeWagon() &&
					v.getTile().getTile() == w.window_number &&
					v.rail.isInDepot() &&
					--row < 0)
			{
						//goto found_it;
						return found_it(d,v,x,skip);
			}
		}

		d.head = null;
		d.wagon = null;

		/* didn't find anything, get out */
		return 0;

		/*
	found_it:
		d.head = d.wagon = v;

		// either pressed the flag or the number, but only when it's a loco 
		if (x < 0 && IsFrontEngine(v))
			return (x >= -10) ? -2 : -1;

		// skip vehicles that are scrolled off the left side
		while (skip--) v = v.next;

		// find the vehicle in this row that was clicked 
		while ((x -= WagonLengthToPixels(v.rail.cached_veh_length)) >= 0) {
			v = v.next;
			if (v == null) break;
		}

		// if an articulated part was selected, find its parent
		while (v != null && IsArticulatedPart(v)) v = GetPrevVehicleInChain(v);

		d.wagon = v;

		return 0;
		*/
	}

	static void TrainDepotMoveVehicle(Vehicle wagon, VehicleID sel, Vehicle head)
	{
		Vehicle v;

		v = Vehicle.GetVehicle(sel);

		if (v == wagon)
			return;

		if (wagon == null) {
			if (head != null)
				wagon = head.GetLastVehicleInChain();
		} else  {
			wagon = wagon.GetPrevVehicleInChain();
			if (wagon == null)
				return;
		}

		if (wagon == v)
			return;

		Cmd.DoCommandP(v.getTile(), v.index + ((wagon == null ? Vehicle.INVALID_VEHICLE : wagon.index) << 16), 
				Global._ctrl_pressed ? 1 : 0, null, Cmd.CMD_MOVE_RAIL_VEHICLE | Cmd.CMD_MSG(Str.STR_8837_CAN_T_MOVE_VEHICLE));
	}

	static void TrainDepotClickTrain(Window w, int x, int y)
	{
		GetDepotVehiclePtData gdvp = new GetDepotVehiclePtData();
		int mode;
		Vehicle v;

		mode = GetVehicleFromTrainDepotWndPt(w, x, y, gdvp);

		// share / copy orders
		if (ViewPort._thd.place_mode != 0 && mode <= 0) { Global._place_clicked_vehicle = gdvp.head; return; }

		v = gdvp.wagon;

		switch(mode) {
		case 0: { // start dragging of vehicle
			VehicleID sel = VehicleID.get( w.as_traindepot_d().sel );

			if (sel.id != Vehicle.INVALID_VEHICLE) {
				w.as_traindepot_d().sel = VehicleID.getInvalid().id;
				TrainDepotMoveVehicle(v, sel, gdvp.head);
			} else if (v != null) {
				w.as_traindepot_d().sel = VehicleID.get(v.index).id;
				ViewPort.SetObjectToPlaceWnd( Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(v.getOwner())) + TrainCmd.GetTrainImage(v, 6), 4, w);
				w.SetWindowDirty();
			}
			break;
		}

		case -1: // show info window
			ShowTrainViewWindow(v);
			break;

		case -2: // click start/stop flag
			Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_START_STOP_TRAIN | Cmd.CMD_MSG(Str.STR_883B_CAN_T_STOP_START_TRAIN));
			break;
		}
	}

	/**
	 * Clones a train
	 * @param v is the original vehicle to clone
	 * @param w is the window of the depot where the clone is build
	 */
	static void HandleCloneVehClick(Vehicle  v, final Window  w)
	{
		if (v == null || v.getType() != Vehicle.VEH_Train) return;

		// for train vehicles: subtype 0 for locs and not zero for others
		if (!v.IsFrontEngine()) {
			v = v.GetFirstVehicleInChain();
			// Do nothing when clicking on a train in depot with no loc attached
			if (!v.IsFrontEngine()) return;
		}

		Cmd.DoCommandP( TileIndex.get( w.window_number ), v.index, Global._ctrl_pressed ? 1 : 0, TrainGui::CcCloneTrain,
			Cmd.CMD_CLONE_VEHICLE | Cmd.CMD_MSG(Str.STR_882B_CAN_T_BUILD_RAILROAD_VEHICLE)
		);

		ViewPort.ResetObjectToPlace();
	}

	static void ClonePlaceObj(TileIndex tile, final Window  w)
	{
		Vehicle  v = ViewPort.CheckMouseOverVehicle();

		if (v != null) HandleCloneVehClick(v, w);
	}

	static void TrainDepotWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT:
			DrawTrainDepotWindow(w);
			break;

		case WE_CLICK: {
			switch(e.widget) {
			case 8:
				ViewPort.ResetObjectToPlace();
				ShowBuildTrainWindow(TileIndex.get(w.window_number));
				break;
			case 10:
				ViewPort.ResetObjectToPlace();
				ViewPort.ScrollMainWindowToTile(new TileIndex(w.window_number) );
				break;
			case 6:
				TrainDepotClickTrain(w, e.pt.x, e.pt.y);
				break;
			case 9: /* clone button */
				w.InvalidateWidget(9);
				w.click_state = BitOps.RETTOGGLEBIT(w.click_state, 9);

				if (BitOps.HASBIT(w.click_state, 9)) {
					Global._place_clicked_vehicle = null;
					ViewPort.SetObjectToPlaceWnd(Sprite.SPR_CURSOR_CLONE, ViewPort.VHM_RECT, w);
				} else {
					ViewPort.ResetObjectToPlace();
				}
				break;

	 		}
	 	} break;

		case WE_PLACE_OBJ: {
			ClonePlaceObj(e.tile, w);
		} break;

		case WE_ABORT_PLACE_OBJ: {
			w.click_state = BitOps.RETCLRBIT(w.click_state, 9);
			w.InvalidateWidget(9);
		} break;

		// check if a vehicle in a depot was clicked..
		case WE_MOUSELOOP: {
			final Vehicle  v = Global._place_clicked_vehicle;

			// since OTTD checks all open depot windows, we will make sure that it triggers the one with a clicked clone button
			if (v != null && BitOps.HASBIT(w.click_state, 9)) {
				Global._place_clicked_vehicle = null;
				HandleCloneVehClick(v, w);
			}
		} break;


		case WE_DESTROY:
			Window.DeleteWindowById(Window.WC_BUILD_VEHICLE, w.window_number);
			break;

		case WE_DRAGDROP: {
			switch(e.widget) {
			case 4: case 5: {
				Vehicle v;
				int sell_cmd;

				/* sell vehicle */
				if(0 != (w.disabled_state & (1 << e.widget)) )
					return;

				if(w.as_traindepot_d().sel == Vehicle.INVALID_VEHICLE)
					return;

				v = Vehicle.GetVehicle(w.as_traindepot_d().sel);

				w.as_traindepot_d().sel = VehicleID.getInvalid().id;
				w.SetWindowDirty();

				w.HandleButtonClick(e.widget);

				sell_cmd = (e.widget == 5 || Global._ctrl_pressed) ? 1 : 0;

				if (!v.IsFrontEngine()) {
					Cmd.DoCommandP(v.getTile(), v.index, sell_cmd, null, Cmd.CMD_SELL_RAIL_WAGON | Cmd.CMD_MSG(Str.STR_8839_CAN_T_SELL_RAILROAD_VEHICLE));
				} else {
					Global._backup_orders_tile = v.getTile();
					Vehicle.BackupVehicleOrders(v, Global._backup_orders_data[0]);
					if (!Cmd.DoCommandP(v.getTile(), v.index, sell_cmd, null, Cmd.CMD_SELL_RAIL_WAGON | Cmd.CMD_MSG(Str.STR_8839_CAN_T_SELL_RAILROAD_VEHICLE)))
						Global._backup_orders_tile = null;
				}
			}	break;

			case 6: {
					GetDepotVehiclePtData gdvp = new GetDepotVehiclePtData();
					VehicleID sel = VehicleID.get( w.as_traindepot_d().sel );

					w.as_traindepot_d().sel = VehicleID.getInvalid().id;
					w.SetWindowDirty();

					if (GetVehicleFromTrainDepotWndPt(w, e.pt.x, e.pt.y, gdvp) == 0 &&
							sel.id != Vehicle.INVALID_VEHICLE) {
						if (gdvp.wagon == null || gdvp.wagon.index != sel.id) {
							TrainDepotMoveVehicle(gdvp.wagon, sel, gdvp.head);
						} else if (gdvp.head != null && gdvp.head.IsFrontEngine()) {
							ShowTrainViewWindow(gdvp.head);
						}
					}
				} break;

			default:
				w.as_traindepot_d().sel = VehicleID.getInvalid().id;
				w.SetWindowDirty();
				break;
			}
			} break;
		case WE_RESIZE: {
			/* Update the scroll + matrix */
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / 14);
			w.hscroll.setCap(w.hscroll.getCap() + e.diff.x / 29);
			w.widget.get(6).unkA = (w.vscroll.getCap() << 8) + 1;
		} break;
		default:
			break;
		}
	}

	static final Widget _train_depot_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   348,     0,    13, Str.STR_8800_TRAIN_DEPOT,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   349,   360,     0,    13, 0x0,										Str.STR_STICKY_BUTTON),

	new Widget(      Window.WWT_PANEL,    Window.RESIZE_LRB,    14,   326,   348,    14,    13, 0x0,										Str.STR_NULL),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_LRTB,    14,   326,   348,    14,    54, 0x2A9,									Str.STR_8841_DRAG_TRAIN_VEHICLE_TO_HERE),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_LRTB,    14,   326,   348,    55,   109, 0x2BF,									Str.STR_DRAG_WHOLE_TRAIN_TO_SELL_TIP),

	new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   325,    14,    97, 0x601,									Str.STR_883F_TRAINS_CLICK_ON_TRAIN_FOR),
	new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   349,   360,    14,   109, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   116,   110,   121, Str.STR_8815_NEW_VEHICLES,	Str.STR_8840_BUILD_NEW_TRAIN_VEHICLE),
	new Widget(Window.WWT_NODISTXTBTN,     Window.RESIZE_TB,    14,   117,   232,   110,   121, Str.STR_CLONE_TRAIN,		Str.STR_CLONE_TRAIN_DEPOT_INFO),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   233,   348,   110,   121, Str.STR_00E4_LOCATION,			Str.STR_8842_CENTER_MAIN_VIEW_ON_TRAIN),


	new Widget( Window.WWT_HSCROLLBAR,    Window.RESIZE_RTB,    14,     0,   325,    98,   109, 0x0,										Str.STR_HSCROLL_BAR_SCROLLS_LIST),
	new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,   349,   348,   110,   121, 0x0,										Str.STR_NULL),

	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   349,   360,   110,   121, 0x0,										Str.STR_RESIZE_BUTTON),
	};

	static final WindowDesc _train_depot_desc = new WindowDesc(
		-1, -1, 361, 122,
		Window.WC_VEHICLE_DEPOT,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_train_depot_widgets,
		TrainGui::TrainDepotWndProc
	);


	public static void ShowTrainDepotWindow(TileIndex tile)
	{
		Window w;

		w = Window.AllocateWindowDescFront(_train_depot_desc, tile.getTile());
		if (null != w) {
			TileIndex wt = TileIndex.get(w.window_number);
		
			w.caption_color = wt.GetTileOwner().id;
			w.vscroll.setCap(6);
			w.hscroll.setCap(10);
			w.resize.step_width = 29;
			w.resize.step_height = 14;
			w.as_traindepot_d().sel = VehicleID.getInvalid().id;
			Global._backup_orders_tile = null;
		}
	}

	static void RailVehicleRefitWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			final Vehicle v = Vehicle.GetVehicle(w.window_number);

			Global.SetDParam(0, v.getString_id());
			Global.SetDParam(1, v.getUnitnumber().id);
			w.DrawWindowWidgets();

			Gfx.DrawString(1, 15, Str.STR_983F_SELECT_CARGO_TYPE_TO_CARRY, 0);

			/* TODO: Support for custom GRFSpecial-specified refitting! --pasky */
			w.as_refit_d().cargo = VehicleGui.DrawVehicleRefitWindow(v, w.as_refit_d().sel).id;

			if (w.as_refit_d().cargo != AcceptedCargo.CT_INVALID) {
				int cost = Cmd.DoCommandByTile(v.getTile(), v.index, w.as_refit_d().cargo, Cmd.DC_QUERY_COST, Cmd.CMD_REFIT_RAIL_VEHICLE);
				if (!Cmd.CmdFailed(cost)) {
					Global.SetDParam(2, cost);
					Global.SetDParam(0, Global._cargoc.names_long[w.as_refit_d().cargo]);
					Global.SetDParam(1, Global._returned_refit_amount);
					Gfx.DrawString(1, 137, Str.STR_9840_NEW_CAPACITY_COST_OF_REFIT, 0);
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
					if (Cmd.DoCommandP(v.getTile(), v.index, w.as_refit_d().cargo, null, Cmd.CMD_REFIT_RAIL_VEHICLE | Cmd.CMD_MSG(Str.STR_RAIL_CAN_T_REFIT_VEHICLE)))
						w.DeleteWindow();
				}
				break;
			}
			break;
		default:
			break;
		}
	}


	static final Widget _rail_vehicle_refit_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   239,     0,    13, Str.STR_983B_REFIT,				Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   239,    14,   135, 0x0,										Str.STR_RAIL_SELECT_TYPE_OF_CARGO_FOR),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   239,   136,   157, 0x0,										Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,   239,   158,   169, Str.STR_RAIL_REFIT_VEHICLE,Str.STR_RAIL_REFIT_TO_CARRY_HIGHLIGHTED),
	};

	static final WindowDesc _rail_vehicle_refit_desc = new WindowDesc(
		-1,-1, 240, 170,
		Window.WC_VEHICLE_REFIT,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_rail_vehicle_refit_widgets,
		TrainGui::RailVehicleRefitWndProc
	);

	static void ShowRailVehicleRefitWindow(Vehicle v)
	{
		Window w;
		Window.DeleteWindowById(Window.WC_VEHICLE_REFIT, v.index);
		//Global._alloc_wnd_parent_num = v.index;
		w = Window.AllocateWindowDesc(_rail_vehicle_refit_desc, v.index);
		w.window_number = v.index;
		w.caption_color =  v.getOwner().id;
		w.as_refit_d().sel = -1;
	}

	static final Widget _train_view_widgets[] = {
	new Widget( Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,  14,   0,  10,   0,  13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW ),
	new Widget( Window.WWT_CAPTION,    Window.RESIZE_RIGHT, 14,  11, 237,   0,  13, Str.STR_882E, Str.STR_018C_WINDOW_TITLE_DRAG_THIS ),
	new Widget( Window.WWT_STICKYBOX,  Window.RESIZE_LR,    14, 238, 249,   0,  13, 0x0,      Str.STR_STICKY_BUTTON ),
	new Widget( Window.WWT_PANEL,      Window.RESIZE_RB,    14,   0, 231,  14, 121, 0x0,      Str.STR_NULL ),
	new Widget( Window.WWT_6,          Window.RESIZE_RB,    14,   2, 229,  16, 119, 0x0,      Str.STR_NULL ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_RTB,   14,   0, 237, 122, 133, 0x0,      Str.STR_8846_CURRENT_TRAIN_ACTION_CLICK ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  14,  31, 0x2AB,    Str.STR_8848_CENTER_MAIN_VIEW_ON_TRAIN ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  32,  49, 0x2AD,    Str.STR_8849_SEND_TRAIN_TO_DEPOT ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  50,  67, 0x2B1,    Str.STR_884A_FORCE_TRAIN_TO_PROCEED ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  68,  85, 0x2CB,    Str.STR_884B_REVERSE_DIRECTION_OF_TRAIN ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  86, 103, 0x2B2,    Str.STR_8847_SHOW_TRAIN_S_ORDERS ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249, 104, 121, 0x2B3,    Str.STR_884C_SHOW_TRAIN_DETAILS ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  68,  85, 0x2B4,    Str.STR_RAIL_REFIT_VEHICLE_TO_CARRY ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  32,  49, Sprite.SPR_CLONE_TRAIN,      Str.STR_CLONE_TRAIN_INFO ),
	new Widget( Window.WWT_PANEL,      Window.RESIZE_LRB,   14, 232, 249, 122, 121, 0x0,      Str.STR_NULL ),
	new Widget( Window.WWT_RESIZEBOX,  Window.RESIZE_LRTB,  14, 238, 249, 122, 133, 0x0,      Str.STR_NULL ),
	};

	//static void ShowTrainDetailsWindow(final Vehicle  v);

	static void TrainViewWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			Vehicle v, u;
			//StringID 
			int str;

			v = Vehicle.GetVehicle(w.window_number);

			w.disabled_state = (v.getOwner().isLocalPlayer()) ? 0 : 0x380;
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 12);

			/* See if any vehicle can be refitted */
			for ( u = v; u != null; u = u.getNext()) {
				if (Global._engine_info[u.getEngine_type().id].refit_mask != 0 ||
							 (!Engine.RailVehInfo(v.getEngine_type().id).isWagon() && v.getCargo_cap() != 0)) {
					w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 12);
					/* We have a refittable carriage, bail out */
					break;
				}
			}

			/* draw widgets & caption */
			Global.SetDParam(0, v.getString_id());
			Global.SetDParam(1, v.getUnitnumber().id);
			w.DrawWindowWidgets();

			str = v.generateTrainDescription();

			/* draw the flag plus orders */
			Gfx.DrawSprite(v.isStopped() ? Sprite.SPR_FLAG_VEH_STOPPED : Sprite.SPR_FLAG_VEH_RUNNING, 2, w.widget.get(5).top + 1);
			Gfx.DrawStringCenteredTruncated(w.widget.get(5).left + 8, w.widget.get(5).right, w.widget.get(5).top + 1, new StringID(str), 0);
			w.DrawWindowViewport();
		}	break;

		case WE_CLICK: {
			int wid = e.widget;
			Vehicle v = Vehicle.GetVehicle(w.window_number);

			switch(wid) {
			case 5: /* start/stop train */
				Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_START_STOP_TRAIN | Cmd.CMD_MSG(Str.STR_883B_CAN_T_STOP_START_TRAIN));
				break;
			case 6:	/* center main view */
				ViewPort.ScrollMainWindowTo(v.getX_pos(), v.getY_pos());
				break;
			case 7:	/* goto depot */
				/* TrainGotoDepot has a nice randomizer in the pathfinder, which causes desyncs... */
				Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_TRAIN_GOTO_DEPOT | Cmd.CMD_NO_TEST_IF_IN_NETWORK | Cmd.CMD_MSG(Str.STR_8830_CAN_T_SEND_TRAIN_TO_DEPOT));
				break;
			case 8: /* force proceed */
				Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_FORCE_TRAIN_PROCEED | Cmd.CMD_MSG(Str.STR_8862_CAN_T_MAKE_TRAIN_PASS_SIGNAL));
				break;
			case 9: /* reverse direction */
				Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_REVERSE_TRAIN_DIRECTION | Cmd.CMD_MSG(Str.STR_8869_CAN_T_REVERSE_DIRECTION));
				break;
			case 10: /* show train orders */
				OrderGui.ShowOrdersWindow(v);
				break;
			case 11: /* show train details */
				ShowTrainDetailsWindow(v);
				break;
			case 12:
				ShowRailVehicleRefitWindow(v);
				break;
			case 13:
				Cmd.DoCommandP(v.getTile(), v.index, Global._ctrl_pressed ? 1 : 0, null, Cmd.CMD_CLONE_VEHICLE | Cmd.CMD_MSG(Str.STR_882B_CAN_T_BUILD_RAILROAD_VEHICLE));
				break;
			}
		} break;

		case WE_RESIZE:
			w.resizeViewPort(e);
			//w.viewport.width  += e.diff.x;
			//w.viewport.height += e.diff.y;
			//w.viewport.virtual_width  += e.diff.x;
			//w.viewport.virtual_height += e.diff.y;
			break;

		case WE_DESTROY:
			Window.DeleteWindowById(Window.WC_VEHICLE_REFIT, w.window_number);
			Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, w.window_number);
			Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, w.window_number);
			break;

		case WE_MOUSELOOP: {
			Vehicle v;
			int h;

			v = Vehicle.GetVehicle(w.window_number);
			assert(v.getType() == Vehicle.VEH_Train);
			h = TrainCmd.CheckTrainStoppedInDepot(v) >= 0 ? (1 << 9)| (1 << 7) : (1 << 12) | (1 << 13);
			if (h != w.hidden_state) {
				w.hidden_state = h;
				w.SetWindowDirty();
			}
			break;
		}
		default:
			break;

		}
	}

	static final WindowDesc _train_view_desc = new WindowDesc(
		-1,-1, 250, 134,
		Window.WC_VEHICLE_VIEW,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_train_view_widgets,
		TrainGui::TrainViewWndProc
	);

	public static void ShowTrainViewWindow(final Vehicle  v)
	{
		Window  w = Window.AllocateWindowDescFront(_train_view_desc,v.index);

		if (w != null) {
			w.caption_color =  v.getOwner().id;
			ViewPort.AssignWindowViewport(w, 3, 17, 0xE2, 0x66, w.window_number | (1 << 31), 0);
		}
	}

	static void TrainDetailsCargoTab(final Vehicle v, int x, int y)
	{
		int num;
		//StringID 
		int str;

		if (v.getCargo_cap() != 0) {
			num = v.getCargo_count();
			str = Str.STR_8812_EMPTY;
			if (num != 0) {
				Global.SetDParam(0, v.getCargo_type());
				Global.SetDParam(1, num);
				Global.SetDParam(2, v.getCargo_source());
				str = Str.STR_8813_FROM;
			}
			Gfx.DrawString(x, y, str, 0);
		}
	}

	static void TrainDetailsInfoTab(final Vehicle v, int x, int y)
	{
		final RailVehicleInfo rvi = Engine.RailVehInfo(v.getEngine_type().id);

		if (!rvi.isWagon()) {
			Global.SetDParam(0, Engine.GetCustomEngineName(v.getEngine_type().id).id);
			Global.SetDParam(1, v.getBuild_year() + 1920);
			Global.SetDParam(2, v.getValue());
			Gfx.DrawString(x, y, Str.STR_882C_BUILT_VALUE, 0x10);
		} else {
			Global.SetDParam(0, Engine.GetCustomEngineName(v.getEngine_type().id).id);
			Global.SetDParam(1, v.getValue());
			Gfx.DrawString(x, y, Str.STR_882D_VALUE, 0x10);
		}
	}

	static void TrainDetailsCapacityTab(final Vehicle v, int x, int y)
	{
		if (v.getCargo_cap() != 0) {
			Global.SetDParam(0, Global._cargoc.names_long[v.getCargo_type()]);
			Global.SetDParam(1, v.getCargo_cap());
			Gfx.DrawString(x, y, Str.STR_013F_CAPACITY, 0);
		}
	}


	static final TrainDetailsDrawerProc  _train_details_drawer_proc[] = {
		TrainGui::TrainDetailsCargoTab,
		TrainGui::TrainDetailsInfoTab,
		TrainGui::TrainDetailsCapacityTab,
	};

	static void DrawTrainDetailsWindow(Window w)
	{
		Vehicle v, u;
		int i,num,x,y,sel;
		int det_tab = w.as_traindetails_d().tab;

		int [][] tot_cargo = new int[AcceptedCargo.NUM_CARGO][2];	// count total cargo ([0]-actual cargo, [1]-total cargo)
		
		/* Count number of vehicles */
		num = 0;

		// det_tab == 3 <-- Total Cargo tab
		// TOD it's clear! if (det_tab == 3)	// reset tot_cargo array to 0 values
		//	memset(tot_cargo, 0, sizeof(tot_cargo));

		u = v = Vehicle.GetVehicle(w.window_number);
		do {
			if (det_tab != 3)
				num++;
			else {
				tot_cargo[u.getCargo_type()][0] += u.getCargo_count();
				tot_cargo[u.getCargo_type()][1] += u.getCargo_cap();
			}
		} while ((u = u.GetNextVehicle()) != null);

		/*	set scroll-amount seperately from counting, as to not
				compute num double for more carriages of the same type
		*/
		if (det_tab == 3) {
			for (i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
				if (tot_cargo[i][1] > 0)	// only count carriages that the train has
					num++;
			}
			num++;	// needs one more because first line is description string
		}

		w.SetVScrollCount( num);

		w.disabled_state = 1 << (det_tab + 9);
		if (!v.getOwner().isLocalPlayer())
			w.disabled_state |= (1 << 2);

		if (0==Global._patches.servint_trains) // disable service-scroller when interval is set to disabled
			w.disabled_state |= (1 << 6) | (1 << 7);

		Global.SetDParam(0, v.getString_id());
		Global.SetDParam(1, v.getUnitnumber().id);
		w.DrawWindowWidgets();

		num = v.getAge() / 366;
		Global.SetDParam(1, num);

		x = 2;

		Global.SetDParam(0, (v.getAge() + 365 < v.getMax_age()) ? Str.STR_AGE : Str.STR_AGE_RED);
		Global.SetDParam(2, v.getMax_age() / 366);
		Global.SetDParam(3, TrainCmd.GetTrainRunningCost(v) >> 8);
		Gfx.DrawString(x, 15, Str.STR_885D_AGE_RUNNING_COST_YR, 0);

		Global.SetDParam(2, v.rail.getCached_max_speed() * 10 >> 4);
		Global.SetDParam(1, v.rail.getCached_power());
		Global.SetDParam(0, v.rail.getCached_weight());
		Gfx.DrawString(x, 25, Str.STR_885E_WEIGHT_T_POWER_HP_MAX_SPEED, 0);

		Global.SetDParam(0, v.getProfit_this_year());
		Global.SetDParam(1, v.getProfit_last_year());
		Gfx.DrawString(x, 35, Str.STR_885F_PROFIT_THIS_YEAR_LAST_YEAR, 0);

		Global.SetDParam(0, 100 * (v.getReliability()>>8) >> 8);
		Global.SetDParam(1, v.getBreakdowns_since_last_service());
		Gfx.DrawString(x, 45, Str.STR_8860_RELIABILITY_BREAKDOWNS, 0);

		Global.SetDParam(0, v.getService_interval());
		Global.SetDParam(1, v.date_of_last_service);
		Gfx.DrawString(x + 11, 141, Global._patches.servint_ispercent?Str.STR_SERVICING_INTERVAL_PERCENT:Str.STR_883C_SERVICING_INTERVAL_DAYS, 0);

		x = 1;
		y = 57;
		sel = w.vscroll.pos;

		// draw the first 3 details tabs
		if (det_tab != 3) {
			for(;;) {
				if (--sel < 0 && sel >= -6) {
					int dx = 0;
					u = v;
					do {
						DrawTrainImage(u, x + WagonLengthToPixels(dx), y, 1, 0, VehicleID.getInvalid());
						dx += u.rail.getCached_veh_length();
						u = u.getNext();
					} while (u != null && u.IsArticulatedPart());
					_train_details_drawer_proc[w.as_traindetails_d().tab].accept(v, x + WagonLengthToPixels(dx) + 2, y + 2);
					y += 14;
				}
				if ((v = v.GetNextVehicle()) == null)
					return;
			}
		}
		else {	// draw total cargo tab
			i = 0;
			Gfx.DrawString(x, y + 2, Str.STR_013F_TOTAL_CAPACITY_TEXT, 0);
			do {
				if (tot_cargo[i][1] > 0 && --sel < 0 && sel >= -5) {
					y += 14;
					// Str.STR_013F_TOTAL_CAPACITY			:{LTBLUE}- {CARGO} ({SHORTCARGO})
					Global.SetDParam(0, i);								// {CARGO} #1
					Global.SetDParam(1, tot_cargo[i][0]);	// {CARGO} #2
					Global.SetDParam(2, i);								// {SHORTCARGO} #1
					Global.SetDParam(3, tot_cargo[i][1]);	// {SHORTCARGO} #2
					Gfx.DrawString(x, y, Str.STR_013F_TOTAL_CAPACITY, 0);
				}
			} while (++i != AcceptedCargo.NUM_CARGO);
		}
	}

	static void TrainDetailsWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT:
			DrawTrainDetailsWindow(w);
			break;
		case WE_CLICK: {
			int mod;
			final Vehicle v;
			switch (e.widget) {
			case 2: /* name train */
				v = Vehicle.GetVehicle(w.window_number);
				Global.SetDParam(0, v.getUnitnumber().id);
				MiscGui.ShowQueryString( new StringID( v.getString_id() ), new StringID( Str.STR_8865_NAME_TRAIN ), 31, 150, w.getWindow_class(), w.window_number);
				break;
			/*	
			case 6:	// inc serv interval 
				mod = Global._ctrl_pressed? 5 : 10;
				goto do_change_service_int;

			case 7: /// dec serv interval 
				mod = Global._ctrl_pressed? -5 : -10;
	do_change_service_int:
				v = Vehicle.GetVehicle(w.window_number);

				mod = GetServiceIntervalClamped(mod + v.service_interval);
				if (mod == v.service_interval) return;

				Cmd.DoCommandP(v.tile, v.index, mod, null, Cmd.CMD_CHANGE_TRAIN_SERVICE_INT | Cmd.CMD_MSG(Str.STR_018A_CAN_T_CHANGE_SERVICING));
				break;
			*/
			case 6:	// inc serv interval 
			case 7: /// dec serv interval 

				if( e.widget == 6 )
					mod = Global._ctrl_pressed? 5 : 10;
				else // 7
					mod = Global._ctrl_pressed? -5 : -10;
				
				v = Vehicle.GetVehicle(w.window_number);

				mod = Depot.GetServiceIntervalClamped(mod + v.getService_interval());
				if (mod == v.getService_interval()) return;

				Cmd.DoCommandP(v.getTile(), v.index, mod, null, Cmd.CMD_CHANGE_TRAIN_SERVICE_INT | Cmd.CMD_MSG(Str.STR_018A_CAN_T_CHANGE_SERVICING));
				break;
				
			/* details buttons*/
			case 9:		// Cargo
			case 10:	// Information
			case 11:	// Capacities
			case 12:	// Total cargo
				w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 9);
				w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 10);
				w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 11);
				w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 12);
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, e.widget);
				w.as_traindetails_d().tab = (byte) (e.widget - 9);
				w.SetWindowDirty();
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
					Cmd.CMD_NAME_VEHICLE | Cmd.CMD_MSG(Str.STR_8866_CAN_T_NAME_TRAIN));
			}
			break;
		default:
			break;
		}
	}

	static final Widget _train_details_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,				Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   329,     0,    13, Str.STR_8802_DETAILS,Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   330,   369,     0,    13, Str.STR_01AA_NAME,		Str.STR_8867_NAME_TRAIN),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   369,    14,    55, 0x0,							Str.STR_NULL),
	new Widget(     Window.WWT_MATRIX,   Window.RESIZE_NONE,    14,     0,   357,    56,   139, 0x601,						Str.STR_NULL),
	new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_NONE,    14,   358,   369,    56,   139, 0x0,							Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    10,   140,   145, Str.STR_0188,				Str.STR_884D_INCREASE_SERVICING_INTERVAL),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    10,   146,   151, Str.STR_0189,				Str.STR_884E_DECREASE_SERVICING_INTERVAL),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    11,   369,   140,   151, 0x0,							Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    92,   152,   163, Str.STR_013C_CARGO,	Str.STR_884F_SHOW_DETAILS_OF_CARGO_CARRIED),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,    93,   184,   152,   163, Str.STR_013D_INFORMATION,	Str.STR_8850_SHOW_DETAILS_OF_TRAIN_VEHICLES),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   185,   277,   152,   163, Str.STR_013E_CAPACITIES,		Str.STR_8851_SHOW_CAPACITIES_OF_EACH),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   278,   369,   152,   163, Str.STR_013E_TOTAL_CARGO,	Str.STR_8852_SHOW_TOTAL_CARGO),

	};


	static final WindowDesc _train_details_desc = new WindowDesc(
		-1,-1, 370, 164,
		Window.WC_VEHICLE_DETAILS,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_train_details_widgets,
		TrainGui::TrainDetailsWndProc
	);


	static void ShowTrainDetailsWindow(final Vehicle  v)
	{
		Window w;
		//VehicleID 
		int veh = v.index;

		Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, veh);
		Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, veh);

		//Window._alloc_wnd_parent_num = veh;
		w = Window.AllocateWindowDesc(_train_details_desc, veh);

		w.window_number = veh;
		w.caption_color =  v.getOwner().id;
		w.vscroll.setCap(6);
		w.as_traindetails_d().tab = 0;
	}

	static final Widget _player_trains_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   312,     0,    13, Str.STR_881B_TRAINS,				Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   313,   324,     0,    13, 0x0,										Str.STR_STICKY_BUTTON),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    80,    14,    25, Str.SRT_SORT_BY,						Str.STR_SORT_ORDER_TIP),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    81,   235,    14,    25, 0x0,										Str.STR_SORT_CRITERIA_TIP),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   236,   247,    14,    25, Str.STR_0225,							Str.STR_SORT_CRITERIA_TIP),
	new Widget(      Window.WWT_PANEL,  Window.RESIZE_RIGHT,    14,   248,   324,    14,    25, 0x0,										Str.STR_NULL),
	new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   312,    26,   207, 0x701,									Str.STR_883D_TRAINS_CLICK_ON_TRAIN_FOR),
	new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   313,   324,    26,   207, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   156,   208,   219, Str.STR_8815_NEW_VEHICLES,	Str.STR_883E_BUILD_NEW_TRAINS_REQUIRES),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   157,   312,   208,   219, Str.STR_REPLACE_VEHICLES,    Str.STR_REPLACE_HELP),
	new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,   313,   312,   208,   219, 0x0,										Str.STR_NULL),
	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   313,   324,   208,   219, 0x0,										Str.STR_RESIZE_BUTTON),

	};

	static final Widget _other_player_trains_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   312,     0,    13, Str.STR_881B_TRAINS,				Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   313,   324,     0,    13, 0x0,										Str.STR_STICKY_BUTTON),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    80,    14,    25, Str.SRT_SORT_BY,						Str.STR_SORT_ORDER_TIP),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    81,   235,    14,    25, 0x0,										Str.STR_SORT_CRITERIA_TIP),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   236,   247,    14,    25, Str.STR_0225,							Str.STR_SORT_CRITERIA_TIP),
	new Widget(      Window.WWT_PANEL,  Window.RESIZE_RIGHT,    14,   248,   324,    14,    25, 0x0,										Str.STR_NULL),
	new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   312,    26,   207, 0x701,									Str.STR_883D_TRAINS_CLICK_ON_TRAIN_FOR),
	new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   313,   324,    26,   207, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,     0,   312,   208,   219, 0x0,										Str.STR_NULL),
	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   313,   324,   208,   219, 0x0,										Str.STR_RESIZE_BUTTON),

	};

	static void PlayerTrainsWndProc(Window w, WindowEvent e)
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

			VehicleGui.BuildVehicleList(vl, Vehicle.VEH_Train, owner, station);
			VehicleGui.SortVehicleList(vl);

			w.SetVScrollCount( vl.list_length);

			// disable 'Sort By' tooltip on Unsorted sorting criteria
			if (vl.sort_type == VehicleGui.SORT_BY_UNSORTED)
				w.disabled_state |= (1 << 3);

			/* draw the widgets */
			{
				final Player p = Player.GetPlayer(owner);
				if (station == Station.INVALID_STATION) {
					/* Company Name -- (###) Trains */
					Global.SetDParam(0, p.getName_1());
					Global.SetDParam(1, p.getName_2());
					Global.SetDParam(2, w.vscroll.getCount());
					w.widget.get(1).unkA = Str.STR_881B_TRAINS;
				} else {
					/* Station Name -- (###) Trains */
					Global.SetDParam(0, station);
					Global.SetDParam(1, w.vscroll.getCount());
					w.widget.get(1).unkA = Str.STR_SCHEDULED_TRAINS;
				}
				w.DrawWindowWidgets();
			}
			/* draw sorting criteria string */
			Gfx.DrawString(85, 15, VehicleGui._vehicle_sort_listing[vl.sort_type], 0x10);
			/* draw arrow pointing up/down for ascending/descending sorting */
			Gfx.DoDrawString(0 != (vl.flags & Vehicle.VL_DESC) ? Gfx.DOWNARROW : Gfx.UPARROW, 69, 15, 0x10);

			max = Math.min(w.vscroll.pos + w.vscroll.getCap(), vl.list_length);
			for (i = w.vscroll.pos; i < max; ++i) {
				Vehicle v = Vehicle.GetVehicle(vl.sort_list[i].index);
				//StringID 
				int str;

				assert(v.getType() == Vehicle.VEH_Train && v.getOwner().id == owner);

				DrawTrainImage(v, x + 21, y + 6, w.hscroll.getCap(), 0, VehicleID.getInvalid());
				VehicleGui.DrawVehicleProfitButton(v, x, y + 13);

				Global.SetDParam(0, v.getUnitnumber().id);
				if (Depot.IsTileDepotType(v.getTile(), TransportType.Rail) && v.isHidden())
					str = Str.STR_021F;
				else
					str = v.getAge() > v.getMax_age() - 366 ? Str.STR_00E3 : Str.STR_00E2;
				Gfx.DrawString(x, y + 2, str, 0);

				Global.SetDParam(0, v.getProfit_this_year());
				Global.SetDParam(1, v.getProfit_last_year());
				Gfx.DrawString(x + 21, y + 18, Str.STR_0198_PROFIT_THIS_YEAR_LAST_YEAR, 0);

				if (v.getString_id() != Str.STR_SV_TRAIN_NAME) {
					Global.SetDParam(0, v.getString_id());
					Gfx.DrawString(x + 21, y, Str.STR_01AB, 0);
				}

				y += VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_SMALL;
			}
			break;
		}

		case WE_CLICK: {
			switch(e.widget) {
			case 3: /* Flip sorting method ascending/descending */
				vl.flags ^= Vehicle.VL_DESC;
				vl.flags |= Vehicle.VL_RESORT;
				VehicleGui._sorting.train.order = 0 != (vl.flags & Vehicle.VL_DESC);
				w.SetWindowDirty();
				break;

			case 4: case 5:/* Select sorting criteria dropdown menu */
				Window.ShowDropDownMenu(w, VehicleGui._vehicle_sort_listing, vl.sort_type, 5, 0, 0);
				return;

			case 7: { /* Matrix to show vehicles */
				int id_v = (e.pt.y - VehicleGui.PLY_WND_PRC__OFFSET_TOP_WIDGET) / VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_SMALL;

				if (id_v >= w.vscroll.getCap()) return; // click out of bounds

				id_v += w.vscroll.pos;

				{
					Vehicle v;

					if (id_v >= vl.list_length) return; // click out of list bound

					v = Vehicle.GetVehicle(vl.sort_list[id_v].index);

					assert(v.getType() == Vehicle.VEH_Train && v.IsFrontEngine() && v.getOwner().id == owner);

					ShowTrainViewWindow(v);
				}
			} break;

			case 9: { /* Build new Vehicle */
				TileIndex tile;

				if (!Window.IsWindowOfPrototype(w, _player_trains_widgets))
					break;

				tile = Depot._last_built_train_depot_tile;
				if( tile == null )
				{
					// TODO print error no depot
					break;
				}
				do {
					if (Depot.IsTileDepotType(tile, TransportType.Rail) && tile.IsTileOwner(Global.gs._local_player)) {
						ShowTrainDepotWindow(tile);
						ShowBuildTrainWindow(tile);
						return;
					}

					tile = tile.iadd(1);
					tile.TILE_MASK();
				} while(!tile.equals(Depot._last_built_train_depot_tile));

				ShowBuildTrainWindow(null);
			} break;
			case 10: {
				if (!Window.IsWindowOfPrototype(w, _player_trains_widgets))
					break;

				VehicleGui.ShowReplaceVehicleWindow(Vehicle.VEH_Train);
				break;
	 		}

			}
		}	break;

		case WE_DROPDOWN_SELECT: /* we have selected a dropdown item in the list */
			if (vl.sort_type != e.index) {
				// value has changed . resort
				vl.flags |= Vehicle.VL_RESORT;
				vl.sort_type =  e.index;
				VehicleGui._sorting.train.criteria = vl.sort_type;

				// enable 'Sort By' if a sorter criteria is chosen
				if (vl.sort_type != VehicleGui.SORT_BY_UNSORTED)
					w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 3);
			}
			w.SetWindowDirty();
			break;

		case WE_CREATE: /* set up resort timer */
			vl.sort_list = null;
			vl.flags = Vehicle.VL_REBUILD | ( BitOps.b2i( VehicleGui._sorting.train.order ) << (Vehicle.VL_DESC - 1));
			vl.sort_type =  VehicleGui._sorting.train.criteria;
			vl.resort_timer = Global.DAY_TICKS * VehicleGui.PERIODIC_RESORT_DAYS;
			break;

		case WE_DESTROY:
			//free(vl.sort_list);
			break;

		case WE_TICK: /* resort the list every 20 seconds orso (10 days) */
			if (--vl.resort_timer == 0) {
				Global.DEBUG_misc(1, "Periodic resort trains list player %d station %d",
					owner, station);
				vl.resort_timer = Global.DAY_TICKS * VehicleGui.PERIODIC_RESORT_DAYS;
				vl.flags |= Vehicle.VL_RESORT;
				w.SetWindowDirty();
			}
			break;

		case WE_RESIZE:
			/* Update the scroll + matrix */
			w.hscroll.setCap(w.hscroll.getCap() + e.diff.x / 29);
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_SMALL);
			w.widget.get(7).unkA = (w.vscroll.getCap() << 8) + 1;
			break;
		default:
			break;
		}
	}

	static final WindowDesc _player_trains_desc = new WindowDesc(
		-1, -1, 325, 220,
		Window.WC_TRAINS_LIST,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_player_trains_widgets,
		TrainGui::PlayerTrainsWndProc
	);

	static final WindowDesc _other_player_trains_desc = new WindowDesc(
		-1, -1, 325, 220,
		Window.WC_TRAINS_LIST,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_other_player_trains_widgets,
		TrainGui::PlayerTrainsWndProc
	);

	static void ShowPlayerTrains(/*PlayerID*/ int player, /*StationID*/int station)
	{
		Window w;

		if (player == Global.gs._local_player.id) {
			w = Window.AllocateWindowDescFront(_player_trains_desc, (station << 16) | player);
		} else {
			w = Window.AllocateWindowDescFront(_other_player_trains_desc, (station << 16) | player);
		}
		if (null != w) {
			w.caption_color =  player;
			w.hscroll.setCap(10);
			w.vscroll.setCap(7); // maximum number of vehicles shown
			w.widget.get(7).unkA = (w.vscroll.getCap() << 8) + 1;
			w.resize.step_height = VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_SMALL;
			w.resize.step_width = 29;
			w.resize.height = 220 - (VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_SMALL * 3); /* Minimum of 4 vehicles */
		}
	}
	
	
}

		
		
// typedef void TrainDetailsDrawerProc(final Vehicle v, int x, int y);
		
@FunctionalInterface
interface TrainDetailsDrawerProc
{
	void accept(Vehicle v, int x, int y);
}
		