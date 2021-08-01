package game;

public class TrainGui 
{



	static int _traininfo_vehicle_pitch = 0;

	/**
	 * Draw the purchase info details of train engine at a given location.
	 * @param x,y location where to draw the info
	 * @param engine_number the engine of which to draw the info of
	 */
	void DrawTrainEnginePurchaseInfo(int x, int y, EngineID engine_number)
	{
		final RailVehicleInfo rvi = RailVehInfo(engine_number);
		final Engine  e = GetEngine(engine_number);
		int multihead = (rvi.flags&RVI_MULTIHEAD?1:0);
		YearMonthDay ymd;
		ConvertDayToYMD(&ymd, e.intro_date);

		/* Purchase Cost - Engine weight */
		Global.SetDParam(0, rvi.base_cost * (Global._price.build_railvehicle >> 3) >> 5);
		Global.SetDParam(1, rvi.weight << multihead);
		DrawString(x,y, Str.STR_PURCHASE_INFO_COST_WEIGHT, 0);
		y += 10;

		/* Max speed - Engine power */
		Global.SetDParam(0, rvi.max_speed * 10 >> 4);
		Global.SetDParam(1, rvi.power << multihead);
		DrawString(x,y, Str.STR_PURCHASE_INFO_SPEED_POWER, 0);
		y += 10;

		/* Running cost */
		Global.SetDParam(0, (rvi.running_cost_base * Global._price.running_rail[rvi.engclass] >> 8) << multihead);
		DrawString(x,y, Str.STR_PURCHASE_INFO_RUNNINGCOST, 0);
		y += 10;

		/* Powered wagons power - Powered wagons extra weight */
		if (rvi.pow_wag_power != 0) {
			Global.SetDParam(0, rvi.pow_wag_power);
			Global.SetDParam(1, rvi.pow_wag_weight);
			DrawString(x,y, Str.STR_PURCHASE_INFO_PWAGPOWER_PWAGWEIGHT, 0);
			y += 10;
		};

		/* Cargo type + capacity, or N/A */
		Global.SetDParam(0, Str.STR_8838_N_A);
		Global.SetDParam(2, Str.STR_EMPTY);
		if (rvi.capacity != 0) {
			Global.SetDParam(0, _cargoc.names_long[rvi.cargo_type]);
			Global.SetDParam(1, rvi.capacity << multihead);
			Global.SetDParam(2, Str.STR_9842_REFITTABLE);
		}
		DrawString(x,y, Str.STR_PURCHASE_INFO_CAPACITY, 0);
		y += 10;

		/* Design date - Life length */
		Global.SetDParam(0, ymd.year + 1920);
		Global.SetDParam(1, e.lifelength);
		DrawString(x,y, Str.STR_PURCHASE_INFO_DESIGNED_LIFE, 0);
		y += 10;

		/* Reliability */
		Global.SetDParam(0, e.reliability * 100 >> 16);
		DrawString(x,y, Str.STR_PURCHASE_INFO_RELIABILITY, 0);
		y += 10;
	}

	/**
	 * Draw the purchase info details of a train wagon at a given location.
	 * @param x,y location where to draw the info
	 * @param engine_number the engine of which to draw the info of
	 */
	void DrawTrainWagonPurchaseInfo(int x, int y, EngineID engine_number)
	{
		final RailVehicleInfo rvi = RailVehInfo(engine_number);
		boolean refittable = (_engine_info[engine_number].refit_mask != 0);

		/* Purchase cost */
		Global.SetDParam(0, (rvi.base_cost * Global._price.build_railwagon) >> 8);
		DrawString(x, y, Str.STR_PURCHASE_INFO_COST, 0);
		y += 10;

		/* Wagon weight - (including cargo) */
		Global.SetDParam(0, rvi.weight);
		Global.SetDParam(1, (_cargoc.weights[rvi.cargo_type] * rvi.capacity >> 4) + rvi.weight);
		DrawString(x, y, Str.STR_PURCHASE_INFO_WEIGHT_CWEIGHT, 0);
		y += 10;

		/* Cargo type + capacity */
		Global.SetDParam(0, _cargoc.names_long[rvi.cargo_type]);
		Global.SetDParam(1, rvi.capacity);
		Global.SetDParam(2, refittable ? Str.STR_9842_REFITTABLE : Str.STR_EMPTY);
		DrawString(x, y, Str.STR_PURCHASE_INFO_CAPACITY, 0);
		y += 10;

		/* Wagon speed limit, displayed if above zero */
		if (rvi.max_speed > 0 && Global._patches.wagon_speed_limits) {
			Global.SetDParam(0, rvi.max_speed * 10 >> 4);
			DrawString(x,y, Str.STR_PURCHASE_INFO_SPEED, 0);
			y += 10;
		}
	}

	void CcBuildWagon(boolean success, TileIndex tile, int p1, int p2)
	{
		Vehicle v,*found;

		if (!success)
			return;

		// find a locomotive in the depot.
		found = null;
		FOR_ALL_VEHICLES(v) {
			if (v.type == Vehicle.VEH_Train && IsFrontEngine(v) &&
					v.tile == tile &&
					v.u.rail.track == 0x80) {
				if (found != null) // must be exactly one.
					return;
				found = v;
			}
		}

		// if we found a loco,
		if (found != null) {
			found = GetLastVehicleInChain(found);
			// put the new wagon at the end of the loco.
			DoCommandP(0, _new_wagon_id | (found.index<<16), 0, null, Cmd.CMD_MOVE_RAIL_VEHICLE);
			RebuildVehicleLists();
		}
	}

	void CcBuildLoco(boolean success, TileIndex tile, int p1, int p2)
	{
		final Vehicle  v;

		if (!success) return;

		v = GetVehicle(_new_train_id);
		if (tile == _backup_orders_tile) {
			_backup_orders_tile = 0;
			RestoreVehicleOrders(v, _backup_orders_data);
		}
		ShowTrainViewWindow(v);
	}

	void CcCloneTrain(boolean success, int tile, int p1, int p2)
	{
		if (success) ShowTrainViewWindow(GetVehicle(_new_train_id));
	}

	static void engine_drawing_loop(int [] x, int [] y, int [] pos, int [] sel,
		/*EngineID*/ int [] selected_id, RailType railtype, byte show_max, boolean is_engine)
	{
		EngineID i;

		for (i = 0; i < Global.NUM_TRAIN_ENGINES; i++) {
			final Engine e = GetEngine(i);
			final RailVehicleInfo rvi = RailVehInfo(i);

			if (!IsCompatibleRail(e.railtype, railtype) || !(rvi.flags & RVI_WAGON) != is_engine ||
					!BitOps.HASBIT(e.player_avail, Global._local_player))
				continue;

			if (*sel == 0)
				*selected_id = i;

			if (BitOps.IS_INT_INSIDE(--*pos, -show_max, 0)) {
				DrawString(*x + 59, *y + 2, GetCustomEngineName(i), *sel == 0 ? 0xC : 0x10);
				DrawTrainEngine(*x + 29, *y + 6, i,
					SPRITE_PALETTE(PLAYER_SPRITE_COLOR(Global._local_player)));
				*y += 14;
			}
			--*sel;
		}
	}

	static void NewRailVehicleWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WindowEvents.WE_PAINT:

			if (w.window_number == 0)
				SETBIT(w.disabled_state, 5);

			{
				int count = 0;
				RailType railtype = WP(w,buildtrain_d).railtype;
				EngineID i;

				for (i = 0; i < Global.NUM_TRAIN_ENGINES; i++) {
					final Engine e = GetEngine(i);
					if (IsCompatibleRail(e.railtype, railtype)
					    && BitOps.HASBIT(e.player_avail, Global._local_player))
						count++;
				}
				SetVScrollCount(w, count);
			}

			Global.SetDParam(0, WP(w,buildtrain_d).railtype + Str.STR_881C_NEW_RAIL_VEHICLES);
			DrawWindowWidgets(w);

			{
				RailType railtype = WP(w,buildtrain_d).railtype;
				int sel = WP(w,buildtrain_d).sel_index;
				int pos = w.vscroll.pos;
				int x = 1;
				int y = 15;
				EngineID selected_id = INVALID_ENGINE;

				/* Ensure that custom engines which substituted wagons
				 * are sorted correctly.
				 * XXX - DO NOT EVER DO THIS EVER AGAIN! GRRR hacking in wagons as
				 * engines to get more types.. Stays here until we have our own format
				 * then it is exit!!! */
				engine_drawing_loop(&x, &y, &pos, &sel, &selected_id, railtype, w.vscroll.cap, true); // True engines
				engine_drawing_loop(&x, &y, &pos, &sel, &selected_id, railtype, w.vscroll.cap, false); // Feeble wagons

				WP(w,buildtrain_d).sel_engine = selected_id;

				if (selected_id != INVALID_ENGINE) {
					final RailVehicleInfo rvi = RailVehInfo(selected_id);

					if (!(rvi.flags & RVI_WAGON)) {
						/* it's an engine */
						DrawTrainEnginePurchaseInfo(2, w.widget[4].top + 1,selected_id);
					} else {
						/* it's a wagon */
						DrawTrainWagonPurchaseInfo(2, w.widget[4].top + 1, selected_id);
					}
				}
			}
		break;

		case WindowEvents.WE_CLICK: {
			switch(e.click.widget) {
			case 2: {
				int i = (e.click.pt.y - 14) / 14;
				if (i < w.vscroll.cap) {
					WP(w,buildtrain_d).sel_index = i + w.vscroll.pos;
					SetWindowDirty(w);
				}
			} break;
			case 5: {
				EngineID sel_eng = WP(w,buildtrain_d).sel_engine;
				if (sel_eng != INVALID_ENGINE)
					DoCommandP(w.window_number, sel_eng, 0, (RailVehInfo(sel_eng).flags & RVI_WAGON) ? CcBuildWagon : CcBuildLoco, Cmd.CMD_BUILD_RAIL_VEHICLE | Cmd.CMD_MSG(Str.STR_882B_CAN_T_BUILD_RAILROAD_VEHICLE));
			}	break;
			case 6: { /* rename */
				EngineID sel_eng = WP(w,buildtrain_d).sel_engine;
				if (sel_eng != INVALID_ENGINE) {
					WP(w,buildtrain_d).rename_engine = sel_eng;
					ShowQueryString(GetCustomEngineName(sel_eng),
						Str.STR_886A_RENAME_TRAIN_VEHICLE_TYPE, 31, 160, w.window_class, w.window_number);
				}
			} break;
			}
		} break;

		case WindowEvents.WE_4:
			if (w.window_number != 0 && !FindWindowById(Window.WC_VEHICLE_DEPOT, w.window_number)) {
				DeleteWindow(w);
			}
			break;

		case WindowEvents.WE_ON_EDIT_TEXT: {
			if (e.edittext.str[0] != '\0') {
				Global._cmd_text = e.edittext.str;
				DoCommandP(0, WP(w,buildtrain_d).rename_engine, 0, null,
					Cmd.CMD_RENAME_ENGINE | Cmd.CMD_MSG(Str.STR_886B_CAN_T_RENAME_TRAIN_VEHICLE));
			}
		} break;

		case WindowEvents.WE_RESIZE: {
			if (e.sizing.diff.y == 0)
				break;

			w.vscroll.cap += e.sizing.diff.y / 14;
			w.widget[2].unkA = (w.vscroll.cap << 8) + 1;
		} break;
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
	new Widget(  Window.WWT_RESIZEBOX,     Window.RESIZE_TB,    14,   216,   227,   198,   209, 0x0,											Str.STR_Window.RESIZE_BUTTON),
	{   WIDGETS_END},
	};

	static final WindowDesc _new_rail_vehicle_desc = {
		-1, -1, 228, 210,
		Window.WC_BUILD_VEHICLE,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
		_new_rail_vehicle_widgets,
		NewRailVehicleWndProc
	};

	static void ShowBuildTrainWindow(TileIndex tile)
	{
		Window w;

		Window.DeleteWindowById(Window.WC_BUILD_VEHICLE, tile);

		w = AllocateWindowDesc(&_new_rail_vehicle_desc);
		w.window_number = tile;
		w.vscroll.cap = 8;
		w.widget[2].unkA = (w.vscroll.cap << 8) + 1;

		w.resize.step_height = 14;
		w.resize.height = w.height - 14 * 4; /* Minimum of 4 vehicles in the display */

		if (tile != 0) {
			w.caption_color = GetTileOwner(tile);
			WP(w,buildtrain_d).railtype = BitOps.GB(tile.getMap().m3, 0, 4);
		} else {
			w.caption_color = Global._local_player;
			WP(w,buildtrain_d).railtype = GetBestRailtype(GetPlayer(Global._local_player));
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

	static void DrawTrainImage(final Vehicle v, int x, int y, int count, int skip, VehicleID selection)
	{
		int dx = 0;
		count *= 8;

		do {
			if (--skip < 0) {
				int image = GetTrainImage(v, 6);
				int ormod = SPRITE_PALETTE(PLAYER_SPRITE_COLOR(v.owner));
				int width = v.u.rail.cached_veh_length;

				if (dx + width <= count) {
					if (v.vehstatus & VS_CRASHED)
						ormod = PALETTE_CRASH;
					Gfx.DrawSprite(image | ormod, x + 14 + WagonLengthToPixels(dx), y + 6 + (is_custom_sprite(RailVehInfo(v.engine_type).image_index) ? _traininfo_vehicle_pitch : 0));
					if (v.index == selection)
						Gfx.DrawFrameRect(x - 1 + WagonLengthToPixels(dx), y - 1, x + WagonLengthToPixels(dx + width) - 1, y + 12, 15, FR_BORDERONLY);
				}
				dx += width;
			}

			v = v.next;
		} while (dx < count && v != null);
	}

	static void DrawTrainDepotWindow(Window w)
	{
		TileIndex tile;
		Vehicle v, *u;
		int num,x,y,i, hnum;
		Depot depot;

		tile = w.window_number;

		/* setup disabled buttons */
		w.disabled_state =
			IsTileOwner(tile, Global._local_player) ? 0 : ((1 << 4) | (1 << 5) | (1 << 8) | (1<<9));

		/* determine amount of items for scroller */
		num = 0;
		hnum = 8;
		FOR_ALL_VEHICLES(v) {
			if (v.type == Vehicle.VEH_Train &&
				  (IsFrontEngine(v) || IsFreeWagon(v)) &&
					v.tile == tile &&
					v.u.rail.track == 0x80) {
				num++;
				// determine number of items in the X direction.
				if (IsFrontEngine(v)) {
					hnum = Math.max(hnum, v.u.rail.cached_total_length);
				}
			}
		}

		/* Always have 1 empty row, so people can change the setting of the train */
		num++;

		SetVScrollCount(w, num);
		SetHScrollCount(w, (hnum + 7) / 8);

		/* locate the depot struct */
		depot = GetDepotByTile(tile);
		assert(depot != null);

		Global.SetDParam(0, depot.town_index);
		DrawWindowWidgets(w);

		x = 2;
		y = 15;
		num = w.vscroll.pos;

		// draw all trains
		FOR_ALL_VEHICLES(v) {
			if (v.type == Vehicle.VEH_Train && IsFrontEngine(v) &&
					v.tile == tile && v.u.rail.track == 0x80 &&
					--num < 0 && num >= -w.vscroll.cap) {
				DrawTrainImage(v, x+21, y, w.hscroll.cap, w.hscroll.pos, WP(w,traindepot_d).sel);
				/* Draw the train number */
				Global.SetDParam(0, v.unitnumber);
				DrawString(x, y, (v.max_age - 366 < v.age) ? Str.STR_00E3 : Str.STR_00E2, 0);

				// Number of wagons relative to a standard length wagon (rounded up)
				Global.SetDParam(0, (v.u.rail.cached_total_length + 7) / 8);
				DrawStringRightAligned(w.widget[6].right - 1, y + 4, Str.STR_TINY_BLACK, 0);	//Draw the counter

				/* Draw the pretty flag */
				Gfx.DrawSprite(v.vehstatus & VS_STOPPED ? Sprite.SPR_FLAG_Vehicle.VEH_STOPPED : Sprite.SPR_FLAG_Vehicle.VEH_RUNNING, x + 15, y);

				y += 14;
			}
		}

		// draw all remaining vehicles
		FOR_ALL_VEHICLES(v) {
			if (v.type == Vehicle.VEH_Train && IsFreeWagon(v) &&
					v.tile == tile && v.u.rail.track == 0x80 &&
					--num < 0 && num >= -w.vscroll.cap) {
				DrawTrainImage(v, x+50, y, w.hscroll.cap - 1, 0, WP(w,traindepot_d).sel);
				DrawString(x, y+2, Str.STR_8816, 0);

				/*Draw the train counter */
				i = 0;
				u = v;
				do i++; while ( (u=u.next) != null);		//Determine length of train
				Global.SetDParam(0, i);				//Set the counter
				DrawStringRightAligned(w.widget[6].right - 1, y + 4, Str.STR_TINY_BLACK, 0);	//Draw the counter
				y += 14;
			}
		}
	}

	class GetDepotVehiclePtData {
		Vehicle head;
		Vehicle wagon;
	} GetDepotVehiclePtData;

	static int GetVehicleFromTrainDepotWndPt(final Window w, int x, int y, GetDepotVehiclePtData *d)
	{
		int row;
		int skip = 0;
		Vehicle v;

		x = x - 23;

		row = (y - 14) / 14;
		if ( (int) row >= w.vscroll.cap)
			return 1; /* means err */

		row += w.vscroll.pos;

		/* go through all the locomotives */
		FOR_ALL_VEHICLES(v) {
			if (v.type == Vehicle.VEH_Train &&
					IsFrontEngine(v) &&
					v.tile == w.window_number &&
					v.u.rail.track == 0x80 &&
					--row < 0) {
						skip = w.hscroll.pos;
						goto found_it;
			}
		}

		x -= 29; /* free wagons don't have an initial loco. */

		/* and then the list of free wagons */
		FOR_ALL_VEHICLES(v) {
			if (v.type == Vehicle.VEH_Train &&
					IsFreeWagon(v) &&
					v.tile == w.window_number &&
					v.u.rail.track == 0x80 &&
					--row < 0)
						goto found_it;
		}

		d.head = null;
		d.wagon = null;

		/* didn't find anything, get out */
		return 0;

	found_it:
		d.head = d.wagon = v;

		/* either pressed the flag or the number, but only when it's a loco */
		if (x < 0 && IsFrontEngine(v))
			return (x >= -10) ? -2 : -1;

		// skip vehicles that are scrolled off the left side
		while (skip--) v = v.next;

		/* find the vehicle in this row that was clicked */
		while ((x -= WagonLengthToPixels(v.u.rail.cached_veh_length)) >= 0) {
			v = v.next;
			if (v == null) break;
		}

		// if an articulated part was selected, find its parent
		while (v != null && IsArticulatedPart(v)) v = GetPrevVehicleInChain(v);

		d.wagon = v;

		return 0;
	}

	static void TrainDepotMoveVehicle(Vehicle wagon, VehicleID sel, Vehicle head)
	{
		Vehicle v;

		v = GetVehicle(sel);

		if (v == wagon)
			return;

		if (wagon == null) {
			if (head != null)
				wagon = GetLastVehicleInChain(head);
		} else  {
			wagon = GetPrevVehicleInChain(wagon);
			if (wagon == null)
				return;
		}

		if (wagon == v)
			return;

		DoCommandP(v.tile, v.index + ((wagon == null ? INVALID_VEHICLE : wagon.index) << 16), _ctrl_pressed ? 1 : 0, null, Cmd.CMD_MOVE_RAIL_VEHICLE | Cmd.CMD_MSG(Str.STR_8837_CAN_T_MOVE_VEHICLE));
	}

	static void TrainDepotClickTrain(Window w, int x, int y)
	{
		GetDepotVehiclePtData gdvp;
		int mode;
		Vehicle v;

		mode = GetVehicleFromTrainDepotWndPt(w, x, y, &gdvp);

		// share / copy orders
		if (_thd.place_mode && mode <= 0) { _place_clicked_vehicle = gdvp.head; return; }

		v = gdvp.wagon;

		switch(mode) {
		case 0: { // start dragging of vehicle
			VehicleID sel = WP(w, traindepot_d).sel;

			if (sel != INVALID_VEHICLE) {
				WP(w,traindepot_d).sel = INVALID_VEHICLE;
				TrainDepotMoveVehicle(v, sel, gdvp.head);
			} else if (v != null) {
				WP(w,traindepot_d).sel = v.index;
				SetObjectToPlaceWnd( SPRITE_PALETTE(PLAYER_SPRITE_COLOR(v.owner)) + GetTrainImage(v, 6), 4, w);
				SetWindowDirty(w);
			}
			break;
		}

		case -1: // show info window
			ShowTrainViewWindow(v);
			break;

		case -2: // click start/stop flag
			DoCommandP(v.tile, v.index, 0, null, Cmd.CMD_START_STOP_TRAIN | Cmd.CMD_MSG(Str.STR_883B_CAN_T_STOP_START_TRAIN));
			break;
		}
	}

	/**
	 * Clones a train
	 * @param *v is the original vehicle to clone
	 * @param *w is the window of the depot where the clone is build
	 */
	static void HandleCloneVehClick(final Vehicle  v, final Window  w)
	{
		if (v == null || v.type != Vehicle.VEH_Train) return;

		// for train vehicles: subtype 0 for locs and not zero for others
		if (!IsFrontEngine(v)) {
			v = GetFirstVehicleInChain(v);
			// Do nothing when clicking on a train in depot with no loc attached
			if (!IsFrontEngine(v)) return;
		}

		DoCommandP(w.window_number, v.index, _ctrl_pressed ? 1 : 0, CcCloneTrain,
			Cmd.CMD_CLONE_VEHICLE | Cmd.CMD_MSG(Str.STR_882B_CAN_T_BUILD_RAILROAD_VEHICLE)
		);

		ResetObjectToPlace();
	}

	static void ClonePlaceObj(TileIndex tile, final Window  w)
	{
		Vehicle  v = CheckMouseOverVehicle();

		if (v != null) HandleCloneVehClick(v, w);
	}

	static void TrainDepotWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WindowEvents.WE_PAINT:
			DrawTrainDepotWindow(w);
			break;

		case WindowEvents.WE_CLICK: {
			switch(e.click.widget) {
			case 8:
				ResetObjectToPlace();
				ShowBuildTrainWindow(w.window_number);
				break;
			case 10:
				ResetObjectToPlace();
				ScrollMainWindowToTile(w.window_number);
				break;
			case 6:
				TrainDepotClickTrain(w, e.click.pt.x, e.click.pt.y);
				break;
			case 9: /* clone button */
				InvalidateWidget(w, 9);
				TOGGLEBIT(w.click_state, 9);

				if (BitOps.HASBIT(w.click_state, 9)) {
					_place_clicked_vehicle = null;
					SetObjectToPlaceWnd(Sprite.SPR_CURSOR_CLONE, VHM_RECT, w);
				} else {
					ResetObjectToPlace();
				}
				break;

	 		}
	 	} break;

		case WindowEvents.WE_PLACE_OBJ: {
			ClonePlaceObj(e.place.tile, w);
		} break;

		case WindowEvents.WE_ABORT_PLACE_OBJ: {
			CLRBIT(w.click_state, 9);
			InvalidateWidget(w, 9);
		} break;

		// check if a vehicle in a depot was clicked..
		case WindowEvents.WE_MOUSELOOP: {
			final Vehicle  v = _place_clicked_vehicle;

			// since OTTD checks all open depot windows, we will make sure that it triggers the one with a clicked clone button
			if (v != null && BitOps.HASBIT(w.click_state, 9)) {
				_place_clicked_vehicle = null;
				HandleCloneVehClick(v, w);
			}
		} break;


		case WindowEvents.WE_DESTROY:
			Window.DeleteWindowById(Window.WC_BUILD_VEHICLE, w.window_number);
			break;

		case WindowEvents.WE_DRAGDROP: {
			switch(e.click.widget) {
			case 4: case 5: {
				Vehicle v;
				int sell_cmd;

				/* sell vehicle */
				if (w.disabled_state & (1 << e.click.widget))
					return;

				if (WP(w,traindepot_d).sel == INVALID_VEHICLE)
					return;

				v = GetVehicle(WP(w,traindepot_d).sel);

				WP(w,traindepot_d).sel = INVALID_VEHICLE;
				SetWindowDirty(w);

				HandleButtonClick(w, e.click.widget);

				sell_cmd = (e.click.widget == 5 || _ctrl_pressed) ? 1 : 0;

				if (!IsFrontEngine(v)) {
					DoCommandP(v.tile, v.index, sell_cmd, null, Cmd.CMD_SELL_RAIL_WAGON | Cmd.CMD_MSG(Str.STR_8839_CAN_T_SELL_RAILROAD_VEHICLE));
				} else {
					_backup_orders_tile = v.tile;
					BackupVehicleOrders(v, _backup_orders_data);
					if (!DoCommandP(v.tile, v.index, sell_cmd, null, Cmd.CMD_SELL_RAIL_WAGON | Cmd.CMD_MSG(Str.STR_8839_CAN_T_SELL_RAILROAD_VEHICLE)))
						_backup_orders_tile = 0;
				}
			}	break;

			case 6: {
					GetDepotVehiclePtData gdvp;
					VehicleID sel = WP(w,traindepot_d).sel;

					WP(w,traindepot_d).sel = INVALID_VEHICLE;
					SetWindowDirty(w);

					if (GetVehicleFromTrainDepotWndPt(w, e.dragdrop.pt.x, e.dragdrop.pt.y, &gdvp) == 0 &&
							sel != INVALID_VEHICLE) {
						if (gdvp.wagon == null || gdvp.wagon.index != sel) {
							TrainDepotMoveVehicle(gdvp.wagon, sel, gdvp.head);
						} else if (gdvp.head != null && IsFrontEngine(gdvp.head)) {
							ShowTrainViewWindow(gdvp.head);
						}
					}
				} break;

			default:
				WP(w,traindepot_d).sel = INVALID_VEHICLE;
				SetWindowDirty(w);
				break;
			}
			} break;
		case WindowEvents.WE_RESIZE: {
			/* Update the scroll + matrix */
			w.vscroll.cap += e.sizing.diff.y / 14;
			w.hscroll.cap += e.sizing.diff.x / 29;
			w.widget[6].unkA = (w.vscroll.cap << 8) + 1;
		} break;
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

	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   349,   360,   110,   121, 0x0,										Str.STR_Window.RESIZE_BUTTON),
	{   WIDGETS_END},
	};

	static final WindowDesc _train_depot_desc = {
		-1, -1, 361, 122,
		Window.WC_VEHICLE_DEPOT,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_train_depot_widgets,
		TrainDepotWndProc
	};


	void ShowTrainDepotWindow(TileIndex tile)
	{
		Window w;

		w = AllocateWindowDescFront(&_train_depot_desc, tile);
		if (w) {
			w.caption_color = GetTileOwner(w.window_number);
			w.vscroll.cap = 6;
			w.hscroll.cap = 10;
			w.resize.step_width = 29;
			w.resize.step_height = 14;
			WP(w,traindepot_d).sel = INVALID_VEHICLE;
			_backup_orders_tile = 0;
		}
	}

	static void RailVehicleRefitWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT: {
			final Vehicle v = GetVehicle(w.window_number);

			Global.SetDParam(0, v.string_id);
			Global.SetDParam(1, v.unitnumber);
			DrawWindowWidgets(w);

			DrawString(1, 15, Str.STR_983F_SELEAcceptedCargo.CT_CARGO_TYPE_TO_CARRY, 0);

			/* TODO: Support for custom GRFSpecial-specified refitting! --pasky */
			WP(w,refit_d).cargo = DrawVehicleRefitWindow(v, WP(w, refit_d).sel);

			if (WP(w,refit_d).cargo != AcceptedCargo.CT_INVALID) {
				int cost = DoCommandByTile(v.tile, v.index, WP(w,refit_d).cargo, Cmd.DC_QUERY_COST, Cmd.CMD_REFIT_RAIL_VEHICLE);
				if (!CmdFailed(cost)) {
					Global.SetDParam(2, cost);
					Global.SetDParam(0, _cargoc.names_long[WP(w,refit_d).cargo]);
					Global.SetDParam(1, _returned_refit_amount);
					DrawString(1, 137, Str.STR_9840_NEW_CAPACITY_COST_OF_REFIT, 0);
				}
			}
		}	break;

		case WindowEvents.WE_CLICK:
			switch(e.click.widget) {
			case 2: { /* listbox */
				int y = e.click.pt.y - 25;
				if (y >= 0) {
					WP(w,refit_d).sel = y / 10;
					SetWindowDirty(w);
				}
			} break;
			case 4: /* refit button */
				if (WP(w,refit_d).cargo != AcceptedCargo.CT_INVALID) {
					final Vehicle v = GetVehicle(w.window_number);
					if (DoCommandP(v.tile, v.index, WP(w,refit_d).cargo, null, Cmd.CMD_REFIT_RAIL_VEHICLE | Cmd.CMD_MSG(Str.STR_RAIL_CAN_T_REFIT_VEHICLE)))
						DeleteWindow(w);
				}
				break;
			}
			break;
		}
	}


	static final Widget _rail_vehicle_refit_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   239,     0,    13, Str.STR_983B_REFIT,				Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   239,    14,   135, 0x0,										Str.STR_RAIL_SELEAcceptedCargo.CT_TYPE_OF_CARGO_FOR),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   239,   136,   157, 0x0,										Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,   239,   158,   169, Str.STR_RAIL_REFIT_VEHICLE,Str.STR_RAIL_REFIT_TO_CARRY_HIGHLIGHTED),
	{   WIDGETS_END},
	};

	static final WindowDesc _rail_vehicle_refit_desc = {
		-1,-1, 240, 170,
		Window.WC_VEHICLE_REFIT,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_rail_vehicle_refit_widgets,
		RailVehicleRefitWndProc,
	};

	static void ShowRailVehicleRefitWindow(Vehicle v)
	{
		Window w;
		Window.DeleteWindowById(Window.WC_VEHICLE_REFIT, v.index);
		_alloc_wnd_parent_num = v.index;
		w = AllocateWindowDesc(&_rail_vehicle_refit_desc);
		w.window_number = v.index;
		w.caption_color = v.owner;
		WP(w,refit_d).sel = -1;
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
	{ WIDGETS_END }
	};

	static void ShowTrainDetailsWindow(final Vehicle  v);

	static void TrainViewWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT: {
			final Vehicle v, *u;
			StringID str;

			v = GetVehicle(w.window_number);

			w.disabled_state = (v.owner == Global._local_player) ? 0 : 0x380;

			SETBIT(w.disabled_state, 12);

			/* See if any vehicle can be refitted */
			for ( u = v; u != null; u = u.next) {
				if (_engine_info[u.engine_type].refit_mask != 0 ||
							 (!(RailVehInfo(v.engine_type).flags & RVI_WAGON) && v.cargo_cap != 0)) {
					CLRBIT(w.disabled_state, 12);
					/* We have a refittable carriage, bail out */
					break;
				}
			}

			/* draw widgets & caption */
			Global.SetDParam(0, v.string_id);
			Global.SetDParam(1, v.unitnumber);
			DrawWindowWidgets(w);

			if (v.u.rail.crash_anim_pos != 0) {
				str = Str.STR_8863_CRASHED;
			} else if (v.breakdown_ctr == 1) {
				str = Str.STR_885C_BROKEN_DOWN;
			} else if (v.vehstatus & VS_STOPPED) {
				if (v.u.rail.last_speed == 0) {
					str = Str.STR_8861_STOPPED;
				} else {
					Global.SetDParam(0, v.u.rail.last_speed * 10 >> 4);
					str = Str.STR_TRAIN_STOPPING + Global._patches.vehicle_speed;
				}
			} else {
				switch (v.current_order.type) {
				case OT_GOTO_STATION: {
					str = Str.STR_HEADING_FOR_STATION + Global._patches.vehicle_speed;
					Global.SetDParam(0, v.current_order.station);
					Global.SetDParam(1, v.u.rail.last_speed * 10 >> 4);
				} break;

				case OT_GOTO_DEPOT: {
					Depot dep = GetDepot(v.current_order.station);
					Global.SetDParam(0, dep.town_index);
					str = Str.STR_HEADING_FOR_TRAIN_DEPOT + Global._patches.vehicle_speed;
					Global.SetDParam(1, v.u.rail.last_speed * 10 >> 4);
				} break;

				case OT_LOADING:
				case OT_LEAVESTATION:
					str = Str.STR_882F_LOADING_UNLOADING;
					break;

				case OT_GOTO_WAYPOINT: {
					Global.SetDParam(0, v.current_order.station);
					str = Str.STR_HEADING_FOR_WAYPOINT + Global._patches.vehicle_speed;
					Global.SetDParam(1, v.u.rail.last_speed * 10 >> 4);
					break;
				}

				default:
					if (v.num_orders == 0) {
						str = Str.STR_NO_ORDERS + Global._patches.vehicle_speed;
						Global.SetDParam(0, v.u.rail.last_speed * 10 >> 4);
					} else
						str = Str.STR_EMPTY;
					break;
				}
			}

			/* draw the flag plus orders */
			Gfx.DrawSprite(v.vehstatus & VS_STOPPED ? Sprite.SPR_FLAG_Vehicle.VEH_STOPPED : Sprite.SPR_FLAG_Vehicle.VEH_RUNNING, 2, w.widget[5].top + 1);
			DrawStringCenteredTruncated(w.widget[5].left + 8, w.widget[5].right, w.widget[5].top + 1, str, 0);
			DrawWindowViewport(w);
		}	break;

		case WindowEvents.WE_CLICK: {
			int wid = e.click.widget;
			Vehicle v = GetVehicle(w.window_number);

			switch(wid) {
			case 5: /* start/stop train */
				DoCommandP(v.tile, v.index, 0, null, Cmd.CMD_START_STOP_TRAIN | Cmd.CMD_MSG(Str.STR_883B_CAN_T_STOP_START_TRAIN));
				break;
			case 6:	/* center main view */
				ScrollMainWindowTo(v.x_pos, v.y_pos);
				break;
			case 7:	/* goto depot */
				/* TrainGotoDepot has a nice randomizer in the pathfinder, which causes desyncs... */
				DoCommandP(v.tile, v.index, 0, null, Cmd.CMD_TRAIN_GOTO_DEPOT | Cmd.CMD_NO_TEST_IF_IN_NETWORK | Cmd.CMD_MSG(Str.STR_8830_CAN_T_SEND_TRAIN_TO_DEPOT));
				break;
			case 8: /* force proceed */
				DoCommandP(v.tile, v.index, 0, null, Cmd.CMD_FORCE_TRAIN_PROCEED | Cmd.CMD_MSG(Str.STR_8862_CAN_T_MAKE_TRAIN_PASS_SIGNAL));
				break;
			case 9: /* reverse direction */
				DoCommandP(v.tile, v.index, 0, null, Cmd.CMD_REVERSE_TRAIN_DIRECTION | Cmd.CMD_MSG(Str.STR_8869_CAN_T_REVERSE_DIRECTION));
				break;
			case 10: /* show train orders */
				ShowOrdersWindow(v);
				break;
			case 11: /* show train details */
				ShowTrainDetailsWindow(v);
				break;
			case 12:
				ShowRailVehicleRefitWindow(v);
				break;
			case 13:
				DoCommandP(v.tile, v.index, _ctrl_pressed ? 1 : 0, null, Cmd.CMD_CLONE_VEHICLE | Cmd.CMD_MSG(Str.STR_882B_CAN_T_BUILD_RAILROAD_VEHICLE));
				break;
			}
		} break;

		case WindowEvents.WE_RESIZE:
			w.viewport.width  += e.sizing.diff.x;
			w.viewport.height += e.sizing.diff.y;
			w.viewport.virtual_width  += e.sizing.diff.x;
			w.viewport.virtual_height += e.sizing.diff.y;
			break;

		case WindowEvents.WE_DESTROY:
			Window.DeleteWindowById(Window.WC_VEHICLE_REFIT, w.window_number);
			Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, w.window_number);
			Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, w.window_number);
			break;

		case WindowEvents.WE_MOUSELOOP: {
			Vehicle v;
			int h;

			v = GetVehicle(w.window_number);
			assert(v.type == Vehicle.VEH_Train);
			h = CheckTrainStoppedInDepot(v) >= 0 ? (1 << 9)| (1 << 7) : (1 << 12) | (1 << 13);
			if (h != w.hidden_state) {
				w.hidden_state = h;
				SetWindowDirty(w);
			}
			break;
		}

		}
	}

	static final WindowDesc _train_view_desc = {
		-1,-1, 250, 134,
		Window.WC_VEHICLE_VIEW,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_train_view_widgets,
		TrainViewWndProc
	};

	void ShowTrainViewWindow(final Vehicle  v)
	{
		Window  w = AllocateWindowDescFront(&_train_view_desc,v.index);

		if (w != null) {
			w.caption_color = v.owner;
			AssignWindowViewport(w, 3, 17, 0xE2, 0x66, w.window_number | (1 << 31), 0);
		}
	}

	static void TrainDetailsCargoTab(final Vehicle v, int x, int y)
	{
		int num;
		StringID str;

		if (v.cargo_cap != 0) {
			num = v.cargo_count;
			str = Str.STR_8812_EMPTY;
			if (num != 0) {
				Global.SetDParam(0, v.cargo_type);
				Global.SetDParam(1, num);
				Global.SetDParam(2, v.cargo_source);
				str = Str.STR_8813_FROM;
			}
			DrawString(x, y, str, 0);
		}
	}

	static void TrainDetailsInfoTab(final Vehicle v, int x, int y)
	{
		final RailVehicleInfo rvi = RailVehInfo(v.engine_type);

		if (!(rvi.flags & RVI_WAGON)) {
			Global.SetDParam(0, GetCustomEngineName(v.engine_type));
			Global.SetDParam(1, v.build_year + 1920);
			Global.SetDParam(2, v.value);
			DrawString(x, y, Str.STR_882C_BUILandscape.LT_VALUE, 0x10);
		} else {
			Global.SetDParam(0, GetCustomEngineName(v.engine_type));
			Global.SetDParam(1, v.value);
			DrawString(x, y, Str.STR_882D_VALUE, 0x10);
		}
	}

	static void TrainDetailsCapacityTab(final Vehicle v, int x, int y)
	{
		if (v.cargo_cap != 0) {
			Global.SetDParam(0, _cargoc.names_long[v.cargo_type]);
			Global.SetDParam(1, v.cargo_cap);
			DrawString(x, y, Str.STR_013F_CAPACITY, 0);
		}
	}

	typedef void TrainDetailsDrawerProc(final Vehicle v, int x, int y);

	static TrainDetailsDrawerProc * final _train_details_drawer_proc[3] = {
		TrainDetailsCargoTab,
		TrainDetailsInfoTab,
		TrainDetailsCapacityTab,
	};

	static void DrawTrainDetailsWindow(Window w)
	{
		final Vehicle v, *u;
		int tot_cargo[NUM_CARGO][2];	// count total cargo ([0]-actual cargo, [1]-total cargo)
		int i,num,x,y,sel;
		byte det_tab = WP(w, traindetails_d).tab;

		/* Count number of vehicles */
		num = 0;

		// det_tab == 3 <-- Total Cargo tab
		if (det_tab == 3)	// reset tot_cargo array to 0 values
			memset(tot_cargo, 0, sizeof(tot_cargo));

		u = v = GetVehicle(w.window_number);
		do {
			if (det_tab != 3)
				num++;
			else {
				tot_cargo[u.cargo_type][0] += u.cargo_count;
				tot_cargo[u.cargo_type][1] += u.cargo_cap;
			}
		} while ((u = GetNextVehicle(u)) != null);

		/*	set scroll-amount seperately from counting, as to not
				compute num double for more carriages of the same type
		*/
		if (det_tab == 3) {
			for (i = 0; i != NUM_CARGO; i++) {
				if (tot_cargo[i][1] > 0)	// only count carriages that the train has
					num++;
			}
			num++;	// needs one more because first line is description string
		}

		SetVScrollCount(w, num);

		w.disabled_state = 1 << (det_tab + 9);
		if (v.owner != Global._local_player)
			w.disabled_state |= (1 << 2);

		if (!Global._patches.servint_trains) // disable service-scroller when interval is set to disabled
			w.disabled_state |= (1 << 6) | (1 << 7);

		Global.SetDParam(0, v.string_id);
		Global.SetDParam(1, v.unitnumber);
		DrawWindowWidgets(w);

		num = v.age / 366;
		Global.SetDParam(1, num);

		x = 2;

		Global.SetDParam(0, (v.age + 365 < v.max_age) ? Str.STR_AGE : Str.STR_AGE_RED);
		Global.SetDParam(2, v.max_age / 366);
		Global.SetDParam(3, GetTrainRunningCost(v) >> 8);
		DrawString(x, 15, Str.STR_885D_AGE_RUNNING_COST_YR, 0);

		Global.SetDParam(2, v.u.rail.cached_max_speed * 10 >> 4);
		Global.SetDParam(1, v.u.rail.cached_power);
		Global.SetDParam(0, v.u.rail.cached_weight);
		DrawString(x, 25, Str.STR_885E_WEIGHT_T_POWER_HP_MAX_SPEED, 0);

		Global.SetDParam(0, v.profit_this_year);
		Global.SetDParam(1, v.profit_last_year);
		DrawString(x, 35, Str.STR_885F_PROFIT_THIS_YEAR_LAST_YEAR, 0);

		Global.SetDParam(0, 100 * (v.reliability>>8) >> 8);
		Global.SetDParam(1, v.breakdowns_since_last_service);
		DrawString(x, 45, Str.STR_8860_RELIABILITY_BREAKDOWNS, 0);

		Global.SetDParam(0, v.service_interval);
		Global.SetDParam(1, v.date_of_last_service);
		DrawString(x + 11, 141, Global._patches.servint_ispercent?Str.STR_SERVICING_INTERVAL_PERCENT:Str.STR_883C_SERVICING_INTERVAL_DAYS, 0);

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
						DrawTrainImage(u, x + WagonLengthToPixels(dx), y, 1, 0, INVALID_VEHICLE);
						dx += u.u.rail.cached_veh_length;
						u = u.next;
					} while (u != null && IsArticulatedPart(u));
					_train_details_drawer_proc[WP(w,traindetails_d).tab](v, x + WagonLengthToPixels(dx) + 2, y + 2);
					y += 14;
				}
				if ((v = GetNextVehicle(v)) == null)
					return;
			}
		}
		else {	// draw total cargo tab
			i = 0;
			DrawString(x, y + 2, Str.STR_013F_TOTAL_CAPACITY_TEXT, 0);
			do {
				if (tot_cargo[i][1] > 0 && --sel < 0 && sel >= -5) {
					y += 14;
					// Str.STR_013F_TOTAL_CAPACITY			:{LTBLUE}- {CARGO} ({SHORTCARGO})
					Global.SetDParam(0, i);								// {CARGO} #1
					Global.SetDParam(1, tot_cargo[i][0]);	// {CARGO} #2
					Global.SetDParam(2, i);								// {SHORTCARGO} #1
					Global.SetDParam(3, tot_cargo[i][1]);	// {SHORTCARGO} #2
					DrawString(x, y, Str.STR_013F_TOTAL_CAPACITY, 0);
				}
			} while (++i != NUM_CARGO);
		}
	}

	static void TrainDetailsWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT:
			DrawTrainDetailsWindow(w);
			break;
		case WindowEvents.WE_CLICK: {
			int mod;
			final Vehicle v;
			switch (e.click.widget) {
			case 2: /* name train */
				v = GetVehicle(w.window_number);
				Global.SetDParam(0, v.unitnumber);
				ShowQueryString(v.string_id, Str.STR_8865_NAME_TRAIN, 31, 150, w.window_class, w.window_number);
				break;
			case 6:	/* inc serv interval */
				mod = _ctrl_pressed? 5 : 10;
				goto do_change_service_int;

			case 7: /* dec serv interval */
				mod = _ctrl_pressed? -5 : -10;
	do_change_service_int:
				v = GetVehicle(w.window_number);

				mod = GetServiceIntervalClamped(mod + v.service_interval);
				if (mod == v.service_interval) return;

				DoCommandP(v.tile, v.index, mod, null, Cmd.CMD_CHANGE_TRAIN_SERVICE_INT | Cmd.CMD_MSG(Str.STR_018A_CAN_T_CHANGE_SERVICING));
				break;
			/* details buttons*/
			case 9:		// Cargo
			case 10:	// Information
			case 11:	// Capacities
			case 12:	// Total cargo
				CLRBIT(w.disabled_state, 9);
				CLRBIT(w.disabled_state, 10);
				CLRBIT(w.disabled_state, 11);
				CLRBIT(w.disabled_state, 12);
				SETBIT(w.disabled_state, e.click.widget);
				WP(w,traindetails_d).tab = e.click.widget - 9;
				SetWindowDirty(w);
				break;
			}
		} break;

		case WindowEvents.WE_4:
			if (FindWindowById(Window.WC_VEHICLE_VIEW, w.window_number) == null)
				DeleteWindow(w);
			break;

		case WindowEvents.WE_ON_EDIT_TEXT:
			if (e.edittext.str[0] != '\0') {
				Global._cmd_text = e.edittext.str;
				DoCommandP(0, w.window_number, 0, null,
					Cmd.CMD_NAME_VEHICLE | Cmd.CMD_MSG(Str.STR_8866_CAN_T_NAME_TRAIN));
			}
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
	{   WIDGETS_END},
	};


	static final WindowDesc _train_details_desc = {
		-1,-1, 370, 164,
		Window.WC_VEHICLE_DETAILS,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_train_details_widgets,
		TrainDetailsWndProc
	};


	static void ShowTrainDetailsWindow(final Vehicle  v)
	{
		Window w;
		VehicleID veh = v.index;

		Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, veh);
		Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, veh);

		_alloc_wnd_parent_num = veh;
		w = AllocateWindowDesc(&_train_details_desc);

		w.window_number = veh;
		w.caption_color = v.owner;
		w.vscroll.cap = 6;
		WP(w,traindetails_d).tab = 0;
	}

	static final Widget _player_trains_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   312,     0,    13, Str.STR_881B_TRAINS,				Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   313,   324,     0,    13, 0x0,										Str.STR_STICKY_BUTTON),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    80,    14,    25, SRT_SORT_BY,						Str.STR_SORT_ORDER_TIP),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    81,   235,    14,    25, 0x0,										Str.STR_SORT_CRITERIA_TIP),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   236,   247,    14,    25, Str.STR_0225,							Str.STR_SORT_CRITERIA_TIP),
	new Widget(      Window.WWT_PANEL,  Window.RESIZE_RIGHT,    14,   248,   324,    14,    25, 0x0,										Str.STR_NULL),
	new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   312,    26,   207, 0x701,									Str.STR_883D_TRAINS_CLICK_ON_TRAIN_FOR),
	new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   313,   324,    26,   207, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   156,   208,   219, Str.STR_8815_NEW_VEHICLES,	Str.STR_883E_BUILD_NEW_TRAINS_REQUIRES),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   157,   312,   208,   219, Str.STR_REPLACE_VEHICLES,    Str.STR_REPLACE_HELP),
	new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,   313,   312,   208,   219, 0x0,										Str.STR_NULL),
	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   313,   324,   208,   219, 0x0,										Str.STR_Window.RESIZE_BUTTON),
	{   WIDGETS_END},
	};

	static final Widget _other_player_trains_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   312,     0,    13, Str.STR_881B_TRAINS,				Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   313,   324,     0,    13, 0x0,										Str.STR_STICKY_BUTTON),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    80,    14,    25, SRT_SORT_BY,						Str.STR_SORT_ORDER_TIP),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    81,   235,    14,    25, 0x0,										Str.STR_SORT_CRITERIA_TIP),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   236,   247,    14,    25, Str.STR_0225,							Str.STR_SORT_CRITERIA_TIP),
	new Widget(      Window.WWT_PANEL,  Window.RESIZE_RIGHT,    14,   248,   324,    14,    25, 0x0,										Str.STR_NULL),
	new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   312,    26,   207, 0x701,									Str.STR_883D_TRAINS_CLICK_ON_TRAIN_FOR),
	new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   313,   324,    26,   207, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,     0,   312,   208,   219, 0x0,										Str.STR_NULL),
	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   313,   324,   208,   219, 0x0,										Str.STR_Window.RESIZE_BUTTON),
	{   WIDGETS_END},
	};

	static void PlayerTrainsWndProc(Window w, WindowEvent e)
	{
		StationID station = BitOps.GB(w.window_number, 16, 16);
		PlayerID owner = BitOps.GB(w.window_number, 0, 8);
		vehiclelist_d *vl = &WP(w, vehiclelist_d);

		switch(e.event) {
		case WindowEvents.WE_PAINT: {
			int x = 2;
			int y = PLY_WND_PRC__OFFSET_TOP_WIDGET;
			int max;
			int i;

			BuildVehicleList(vl, Vehicle.VEH_Train, owner, station);
			SortVehicleList(vl);

			SetVScrollCount(w, vl.list_length);

			// disable 'Sort By' tooltip on Unsorted sorting criteria
			if (vl.sort_type == SORT_BY_UNSORTED)
				w.disabled_state |= (1 << 3);

			/* draw the widgets */
			{
				final Player p = GetPlayer(owner);
				if (station == INVALID_STATION) {
					/* Company Name -- (###) Trains */
					Global.SetDParam(0, p.name_1);
					Global.SetDParam(1, p.name_2);
					Global.SetDParam(2, w.vscroll.count);
					w.widget[1].unkA = Str.STR_881B_TRAINS;
				} else {
					/* Station Name -- (###) Trains */
					Global.SetDParam(0, station);
					Global.SetDParam(1, w.vscroll.count);
					w.widget[1].unkA = Str.STR_SCHEDULED_TRAINS;
				}
				DrawWindowWidgets(w);
			}
			/* draw sorting criteria string */
			DrawString(85, 15, _vehicle_sort_listing[vl.sort_type], 0x10);
			/* draw arrow pointing up/down for ascending/descending sorting */
			Gfx.DoDrawString(vl.flags & VL_DESC ? DOWNARROW : UPARROW, 69, 15, 0x10);

			max = Math.min(w.vscroll.pos + w.vscroll.cap, vl.list_length);
			for (i = w.vscroll.pos; i < max; ++i) {
				Vehicle v = GetVehicle(vl.sort_list[i].index);
				StringID str;

				assert(v.type == Vehicle.VEH_Train && v.owner == owner);

				DrawTrainImage(v, x + 21, y + 6, w.hscroll.cap, 0, INVALID_VEHICLE);
				DrawVehicleProfitButton(v, x, y + 13);

				Global.SetDParam(0, v.unitnumber);
				if (IsTileDepotType(v.tile, TRANSPORT_RAIL) && (v.vehstatus & VS_HIDDEN))
					str = Str.STR_021F;
				else
					str = v.age > v.max_age - 366 ? Str.STR_00E3 : Str.STR_00E2;
				DrawString(x, y + 2, str, 0);

				Global.SetDParam(0, v.profit_this_year);
				Global.SetDParam(1, v.profit_last_year);
				DrawString(x + 21, y + 18, Str.STR_0198_PROFIT_THIS_YEAR_LAST_YEAR, 0);

				if (v.string_id != Str.STR_SV_TRAIN_NAME) {
					Global.SetDParam(0, v.string_id);
					DrawString(x + 21, y, Str.STR_01AB, 0);
				}

				y += PLY_WND_PRC__SIZE_OF_ROW_SMALL;
			}
			break;
		}

		case WindowEvents.WE_CLICK: {
			switch(e.click.widget) {
			case 3: /* Flip sorting method ascending/descending */
				vl.flags ^= VL_DESC;
				vl.flags |= VL_RESORT;
				_sorting.train.order = !!(vl.flags & VL_DESC);
				SetWindowDirty(w);
				break;

			case 4: case 5:/* Select sorting criteria dropdown menu */
				ShowDropDownMenu(w, _vehicle_sort_listing, vl.sort_type, 5, 0, 0);
				return;

			case 7: { /* Matrix to show vehicles */
				int id_v = (e.click.pt.y - PLY_WND_PRC__OFFSET_TOP_WIDGET) / PLY_WND_PRC__SIZE_OF_ROW_SMALL;

				if (id_v >= w.vscroll.cap) return; // click out of bounds

				id_v += w.vscroll.pos;

				{
					Vehicle v;

					if (id_v >= vl.list_length) return; // click out of list bound

					v = GetVehicle(vl.sort_list[id_v].index);

					assert(v.type == Vehicle.VEH_Train && IsFrontEngine(v) && v.owner == owner);

					ShowTrainViewWindow(v);
				}
			} break;

			case 9: { /* Build new Vehicle /
				TileIndex tile;

				if (!IsWindowOfPrototype(w, _player_trains_widgets))
					break;

				tile = _last_built_train_depot_tile;
				do {
					if (IsTileDepotType(tile, TRANSPORT_RAIL) && IsTileOwner(tile, Global._local_player)) {
						ShowTrainDepotWindow(tile);
						ShowBuildTrainWindow(tile);
						return;
					}

					tile = TILE_MASK(tile + 1);
				} while(tile != _last_built_train_depot_tile);

				ShowBuildTrainWindow(0);
			} break;
			case 10: {
				if (!IsWindowOfPrototype(w, _player_trains_widgets))
					break;

				ShowReplaceVehicleWindow(Vehicle.VEH_Train);
				break;
	 		}

			}
		}	break;

		case WindowEvents.WE_DROPDOWN_SELECT: /* we have selected a dropdown item in the list */
			if (vl.sort_type != e.dropdown.index) {
				// value has changed . resort
				vl.flags |= VL_RESORT;
				vl.sort_type = e.dropdown.index;
				_sorting.train.criteria = vl.sort_type;

				// enable 'Sort By' if a sorter criteria is chosen
				if (vl.sort_type != SORT_BY_UNSORTED)
					CLRBIT(w.disabled_state, 3);
			}
			SetWindowDirty(w);
			break;

		case WindowEvents.WE_CREATE: /* set up resort timer */
			vl.sort_list = null;
			vl.flags = VL_REBUILD | (_sorting.train.order << (VL_DESC - 1));
			vl.sort_type = _sorting.train.criteria;
			vl.resort_timer = DAY_TICKS * PERIODIC_RESORT_DAYS;
			break;

		case WindowEvents.WE_DESTROY:
			free(vl.sort_list);
			break;

		case WindowEvents.WE_TICK: /* resort the list every 20 seconds orso (10 days) */
			if (--vl.resort_timer == 0) {
				DEBUG(misc, 1) ("Periodic resort trains list player %d station %d",
					owner, station);
				vl.resort_timer = DAY_TICKS * PERIODIC_RESORT_DAYS;
				vl.flags |= VL_RESORT;
				SetWindowDirty(w);
			}
			break;

		case WindowEvents.WE_RESIZE:
			/* Update the scroll + matrix */
			w.hscroll.cap += e.sizing.diff.x / 29;
			w.vscroll.cap += e.sizing.diff.y / PLY_WND_PRC__SIZE_OF_ROW_SMALL;
			w.widget[7].unkA = (w.vscroll.cap << 8) + 1;
			break;
		}
	}

	static final WindowDesc _player_trains_desc = {
		-1, -1, 325, 220,
		Window.WC_TRAINS_LIST,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_player_trains_widgets,
		PlayerTrainsWndProc
	};

	static final WindowDesc _other_player_trains_desc = {
		-1, -1, 325, 220,
		Window.WC_TRAINS_LIST,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_other_player_trains_widgets,
		PlayerTrainsWndProc
	};

	void ShowPlayerTrains(PlayerID player, StationID station)
	{
		Window w;

		if (player == Global._local_player) {
			w = AllocateWindowDescFront(&_player_trains_desc, (station << 16) | player);
		} else {
			w = AllocateWindowDescFront(&_other_player_trains_desc, (station << 16) | player);
		}
		if (w) {
			w.caption_color = player;
			w.hscroll.cap = 10;
			w.vscroll.cap = 7; // maximum number of vehicles shown
			w.widget[7].unkA = (w.vscroll.cap << 8) + 1;
			w.resize.step_height = PLY_WND_PRC__SIZE_OF_ROW_SMALL;
			w.resize.step_width = 29;
			w.resize.height = 220 - (PLY_WND_PRC__SIZE_OF_ROW_SMALL * 3); /* Minimum of 4 vehicles */
		}
	}
	
	
}
