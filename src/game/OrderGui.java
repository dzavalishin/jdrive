package game;

public class OrderGui {


	static int OrderGetSel(final Window  w)
	{
		final Vehicle  v = Vehicle.GetVehicle(w.window_number);
		int num = w.as_order_d().sel;

		return (num >= 0 && num < v.num_orders) ? num : v.num_orders;
	}

	static /*StringID*/ int StationOrderStrings[] = {
		Str.STR_8806_GO_TO,
		Str.STR_8807_GO_TO_TRANSFER,
		Str.STR_8808_GO_TO_UNLOAD,
		Str.STR_8809_GO_TO_TRANSFER_UNLOAD,
		Str.STR_880A_GO_TO_LOAD,
		Str.STR_880B_GO_TO_TRANSFER_LOAD,
		Str.STR_NULL,
		Str.STR_NULL,
		Str.STR_880C_GO_NON_STOP_TO,
		Str.STR_880D_GO_TO_NON_STOP_TRANSFER,
		Str.STR_880E_GO_NON_STOP_TO_UNLOAD,
		Str.STR_880F_GO_TO_NON_STOP_TRANSFER_UNLOAD,
		Str.STR_8810_GO_NON_STOP_TO_LOAD,
		Str.STR_8811_GO_TO_NON_STOP_TRANSFER_LOAD,
		Str.STR_NULL
	};

	static void DrawOrdersWindow(Window w)
	{
		final Vehicle v;
		final Order order;
		StringID str;
		int sel;
		int y, i;
		boolean shared_orders;
		byte color;

		v = GetVehicle(w.window_number);

		w.disabled_state = (v.owner == Global._local_player) ? 0 : (
			1 << 4 |   //skip
			1 << 5 |   //delete
			1 << 6 |   //non-stop
			1 << 7 |   //go-to
			1 << 8 |   //full load
			1 << 9 |   //unload
			1 << 10    //transfer
			);

		if (v.type != Vehicle.VEH_Train)
			SETBIT(w.disabled_state, 6); //disable non-stop for non-trains

		shared_orders = IsOrderListShared(v);

		if ((int)v.num_orders + (shared_orders?1:0) <= (int)w.as_order_d().sel)
			SETBIT(w.disabled_state, 5); /* delete */

		if (v.num_orders == 0)
			SETBIT(w.disabled_state, 4); /* skip */

		SetVScrollCount(w, v.num_orders + 1);

		sel = OrderGetSel(w);
		Global.SetDParam(2, Str.STR_8827_FULL_LOAD);

		order = GetVehicleOrder(v, sel);

		if (order != null) {
			switch (order.type) {
				case OT_GOTO_STATION:
					break;

				case OT_GOTO_DEPOT:
					SETBIT(w.disabled_state, 9);	/* unload */
					SETBIT(w.disabled_state, 10); /* transfer */
					Global.SetDParam(2,Str.STR_SERVICE);
					break;

				case OT_GOTO_WAYPOINT:
					SETBIT(w.disabled_state, 8); /* full load */
					SETBIT(w.disabled_state, 9); /* unload */
					SETBIT(w.disabled_state, 10); /* transfer */
					break;

				default:
					SETBIT(w.disabled_state, 6); /* nonstop */
					SETBIT(w.disabled_state, 8);	/* full load */
					SETBIT(w.disabled_state, 9);	/* unload */
			}
		} else {
			SETBIT(w.disabled_state, 6); /* nonstop */
			SETBIT(w.disabled_state, 8);	/* full load */
			SETBIT(w.disabled_state, 9);	/* unload */
			SETBIT(w.disabled_state, 10); /* transfer */
		}

		Global.SetDParam(0, v.string_id);
		Global.SetDParam(1, v.unitnumber);
		DrawWindowWidgets(w);

		y = 15;

		i = w.vscroll.pos;
		order = GetVehicleOrder(v, i);
		while (order != null) {
			str = (v.cur_order_index == i) ? Str.STR_8805 : Str.STR_8804;

			if (i - w.vscroll.pos < w.vscroll.cap) {
				Global.SetDParam(1, 6);

				switch (order.type) {
					case OT_GOTO_STATION:
						Global.SetDParam(1, StationOrderStrings[order.flags]);
						Global.SetDParam(2, order.station);
						break;

					case OT_GOTO_DEPOT: {
						StringID s = Str.STR_NULL;

						if (v.type == Vehicle.VEH_Aircraft) {
							s = Str.STR_GO_TO_AIRPORT_HANGAR;
							Global.SetDParam(2, order.station);
						} else {
							Global.SetDParam(2, GetDepot(order.station).town_index);

							switch (v.type) {
								case Vehicle.VEH_Train: s = (order.flags & OF_NON_STOP) ? Str.STR_880F_GO_NON_STOP_TO_TRAIN_DEPOT : Str.STR_GO_TO_TRAIN_DEPOT; break;
								case Vehicle.VEH_Road:  s = Str.STR_9038_GO_TO_ROADVehicle.VEH_DEPOT; break;
								case Vehicle.VEH_Ship:  s = Str.STR_GO_TO_SHIP_DEPOT; break;
								default: break;
							}
						}

						if (order.flags & OF_FULL_LOAD) s++; /* service at */

						Global.SetDParam(1, s);
						break;
					}

					case OT_GOTO_WAYPOINT:
						Global.SetDParam(1, (order.flags & OF_NON_STOP) ? Str.STR_GO_NON_STOP_TO_WAYPOINT : Str.STR_GO_TO_WAYPOINT);
						Global.SetDParam(2, order.station);
						break;
				}

				color = (i == w.as_order_d().sel) ? 0xC : 0x10;
				Global.SetDParam(0, i + 1);
				if (order.type != OT_DUMMY) {
					DrawString(2, y, str, color);
				} else {
					Global.SetDParam(1, Str.STR_INVALID_ORDER);
					Global.SetDParam(2, order.station);
					DrawString(2, y, str, color);
				}
				y += 10;
			}

			i++;
			order = order.next;
		}

		if (i - w.vscroll.pos < w.vscroll.cap) {
			str = shared_orders ? Str.STR_END_OF_SHARED_ORDERS : Str.STR_882A_END_OF_ORDERS;
			color = (i == w.as_order_d().sel) ? 0xC : 0x10;
			DrawString(2, y, str, color);
		}
	}

	static Order GetOrderCmdFromTile(final Vehicle v, TileIndex tile)
	{
		Order order;
		int st_index;

		// check depot first
		if (Global._patches.gotodepot) {
			switch (GetTileType(tile)) {
			case TileTypes.MP_RAILWAY:
				if (v.type == Vehicle.VEH_Train && IsTileOwner(tile, Global._local_player)) {
					if ((tile.getMap().m5&0xFC)==0xC0) {
						order.type = OT_GOTO_DEPOT;
						order.flags = OF_PART_OF_ORDERS;
						order.station = GetDepotByTile(tile).index;
						return order;
					}
				}
				break;

			case TileTypes.MP_STREET:
				if ((tile.getMap().m5 & 0xF0) == 0x20 && v.type == Vehicle.VEH_Road && IsTileOwner(tile, Global._local_player)) {
					order.type = OT_GOTO_DEPOT;
					order.flags = OF_PART_OF_ORDERS;
					order.station = GetDepotByTile(tile).index;
					return order;
				}
				break;

			case TileTypes.MP_STATION:
				if (v.type != Vehicle.VEH_Aircraft) break;
				if (IsAircraftHangarTile(tile) && IsTileOwner(tile, Global._local_player)) {
					order.type = OT_GOTO_DEPOT;
					order.flags = OF_PART_OF_ORDERS;
					order.station = tile.getMap().m2;
					return order;
				}
				break;

			case TileTypes.MP_WATER:
				if (v.type != Vehicle.VEH_Ship) break;
				if (IsTileDepotType(tile, TRANSPORT_WATER) &&
						IsTileOwner(tile, Global._local_player)) {
					switch (tile.getMap().m5) {
						case 0x81: tile -= TileDiffXY(1, 0); break;
						case 0x83: tile -= TileDiffXY(0, 1); break;
					}
					order.type = OT_GOTO_DEPOT;
					order.flags = OF_PART_OF_ORDERS;
					order.station = GetDepotByTile(tile).index;
					return order;
				}

				default:
					break;
			}
		}

		// check waypoint
		if (tile.IsTileType( TileTypes.MP_RAILWAY) &&
				v.type == Vehicle.VEH_Train &&
				IsTileOwner(tile, Global._local_player) &&
				IsRailWaypoint(tile)) {
			order.type = OT_GOTO_WAYPOINT;
			order.flags = 0;
			order.station = GetWaypointByTile(tile).index;
			return order;
		}

		if (tile.IsTileType( TileTypes.MP_STATION)) {
			final Station  st = Station.GetStation(st_index = tile.getMap().m2);
			
			if (st.owner == Global._current_player || st.owner == Owner.OWNER_NONE || MA_OwnerHandler(st.owner)) {
				byte facil;
				(facil=FACIL_DOCK, v.type == Vehicle.VEH_Ship) ||
				(facil=FACIL_TRAIN, v.type == Vehicle.VEH_Train) ||
				(facil=FACIL_AIRPORT, v.type == Vehicle.VEH_Aircraft) ||
				(facil=FACIL_BUS_STOP, v.type == Vehicle.VEH_Road && v.cargo_type == AcceptedCargo.CT_PASSENGERS) ||
				(facil=FACIL_TRUCK_STOP, 1);
				if (st.facilities & facil) {
					order.type = OT_GOTO_STATION;
					order.flags = 0;
					order.station = st_index;
					return order;
				}
			}
		}

		// not found
		order.type = OT_NOTHING;
		order.flags = 0;
		return order;
	}

	static boolean HandleOrderVehClick(final Vehicle  v, final Vehicle  u, Window  w)
	{
		if (u.type != v.type) return false;

		if (u.type == Vehicle.VEH_Train && !IsFrontEngine(u)) {
			u = GetFirstVehicleInChain(u);
			if (!IsFrontEngine(u)) return false;
		}

		// v is vehicle getting orders. Only copy/clone orders if vehicle doesn't have any orders yet
		// obviously if you press CTRL on a non-empty orders vehicle you know what you are doing
		if (v.num_orders != 0 && _ctrl_pressed == 0) return false;

		if (DoCommandP(v.tile, v.index | (u.index << 16), _ctrl_pressed ? 0 : 1, null,
			_ctrl_pressed ? Cmd.CMD_CLONE_ORDER | Cmd.CMD_MSG(Str.STR_CANT_SHARE_ORDER_LIST) : Cmd.CMD_CLONE_ORDER | Cmd.CMD_MSG(Str.STR_CANT_COPY_ORDER_LIST))) {
			w.as_order_d().sel = -1;
			ResetObjectToPlace();
		}

		return true;
	}

	static void OrdersPlaceObj(final Vehicle  v, TileIndex tile, Window  w)
	{
		Order cmd;
		final Vehicle  u;

		// check if we're clicking on a vehicle first.. clone orders in that case.
		u = CheckMouseOverVehicle();
		if (u != null && HandleOrderVehClick(v, u, w)) return;

		cmd = GetOrderCmdFromTile(v, tile);
		if (cmd.type == OT_NOTHING) return;

		if (DoCommandP(v.tile, v.index + (OrderGetSel(w) << 16), PackOrder(&cmd), null, Cmd.CMD_INSERT_ORDER | Cmd.CMD_MSG(Str.STR_8833_CAN_T_INSERT_NEW_ORDER))) {
			if (w.as_order_d().sel != -1) w.as_order_d().sel++;
			ResetObjectToPlace();
		}
	}

	static void OrderClick_Goto(Window  w, final Vehicle  v)
	{
		InvalidateWidget(w, 7);
		TOGGLEBIT(w.click_state, 7);
		if (BitOps.HASBIT(w.click_state, 7)) {
			_place_clicked_vehicle = null;
			SetObjectToPlaceWnd(ANIMCURSOR_PICKSTATION, 1, w);
		} else {
			ResetObjectToPlace();
		}
	}

	static void OrderClick_FullLoad(Window  w, final Vehicle  v)
	{
		DoCommandP(v.tile, v.index + (OrderGetSel(w) << 16), OFB_FULL_LOAD, null, Cmd.CMD_MODIFY_ORDER | Cmd.CMD_MSG(Str.STR_8835_CAN_T_MODIFY_THIS_ORDER));
	}

	static void OrderClick_Unload(Window  w, final Vehicle  v)
	{
		DoCommandP(v.tile, v.index + (OrderGetSel(w) << 16), OFB_UNLOAD,    null, Cmd.CMD_MODIFY_ORDER | Cmd.CMD_MSG(Str.STR_8835_CAN_T_MODIFY_THIS_ORDER));
	}

	static void OrderClick_Nonstop(Window  w, final Vehicle  v)
	{
		DoCommandP(v.tile, v.index + (OrderGetSel(w) << 16), OFB_NON_STOP,  null, Cmd.CMD_MODIFY_ORDER | Cmd.CMD_MSG(Str.STR_8835_CAN_T_MODIFY_THIS_ORDER));
	}

	static void OrderClick_Transfer(Window  w, final Vehicle  v)
	{
		DoCommandP(v.tile, v.index + (OrderGetSel(w) <<  16), OFB_TRANSFER, null, Cmd.CMD_MODIFY_ORDER | Cmd.CMD_MSG(Str.STR_8835_CAN_T_MODIFY_THIS_ORDER));
	}

	static void OrderClick_Skip(Window  w, final Vehicle  v)
	{
		DoCommandP(v.tile, v.index, 0, null, Cmd.CMD_SKIP_ORDER);
	}

	static void OrderClick_Delete(Window  w, final Vehicle  v)
	{
		DoCommandP(v.tile, v.index, OrderGetSel(w), null, Cmd.CMD_DELETE_ORDER | Cmd.CMD_MSG(Str.STR_8834_CAN_T_DELETE_THIS_ORDER));
	}

	typedef void OnButtonClick(Window  w, final Vehicle  v);

	static OnButtonClick* final _order_button_proc[] = {
		OrderClick_Skip,
		OrderClick_Delete,
		OrderClick_Nonstop,
		OrderClick_Goto,
		OrderClick_FullLoad,
		OrderClick_Unload,
		OrderClick_Transfer
	};

	static final int _order_keycodes[] = {
		'D', //skip order
		'F', //delete order
		'G', //non-stop
		'H', //goto order
		'J', //full load
		'K'  //unload
	};

	static void OrdersWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT:
			DrawOrdersWindow(w);
			break;

		case WindowEvents.WE_CLICK: {
			Vehicle v = GetVehicle(w.window_number);
			switch(e.click.widget) {
			case 2: { /* orders list */
				int sel;
				sel = (e.click.pt.y - 15) / 10;

				if ((int)sel >= w.vscroll.cap)
					return;

				sel += w.vscroll.pos;

				if (_ctrl_pressed && sel < v.num_orders) {
					final Order ord = GetVehicleOrder(v, sel);
					int xy = 0;
					switch (ord.type) {
					case OT_GOTO_STATION:			/* station order */
						xy = Station.GetStation(ord.station).xy ;
						break;
					case OT_GOTO_DEPOT:				/* goto depot order */
						xy = GetDepot(ord.station).xy;
						break;
					case OT_GOTO_WAYPOINT:	/* goto waypoint order */
						xy = GetWaypoint(ord.station).xy;
					}

					if (xy)
						ScrollMainWindowToTile(xy);

					return;
				}

				if (sel == w.as_order_d().sel) sel = -1;
				w.as_order_d().sel = sel;
				SetWindowDirty(w);
			}	break;

			case 4: /* skip button */
				OrderClick_Skip(w, v);
				break;

			case 5: /* delete button */
				OrderClick_Delete(w, v);
				break;

			case 6: /* non stop button */
				OrderClick_Nonstop(w, v);
				break;

			case 7: /* goto button */
				OrderClick_Goto(w, v);
				break;

			case 8: /* full load button */
				OrderClick_FullLoad(w, v);
				break;

			case 9: /* unload button */
				OrderClick_Unload(w, v);
				break;
			case 10: /* transfer button */
				OrderClick_Transfer(w, v);
				break;
			}
		} break;

		case WindowEvents.WE_KEYPRESS: {
			Vehicle v = GetVehicle(w.window_number);
			int i;

			for(i = 0; i < lengthof(_order_keycodes); i++) {
				if (e.keypress.keycode == _order_keycodes[i]) {
					e.keypress.cont = false;
					//see if the button is disabled
					if (!(BitOps.HASBIT(w.disabled_state, (i + 4)))) {
						_order_button_proc[i](w, v);
					}
					break;
				}
			}
			break;
		}

		case WindowEvents.WE_RCLICK: {
			final Vehicle  v = GetVehicle(w.window_number);
			int s = OrderGetSel(w);

			if (e.click.widget != 8) break;
			if (s == v.num_orders || GetVehicleOrder(v, s).type != OT_GOTO_DEPOT) {
				GuiShowTooltips(Str.STR_8857_MAKE_THE_HIGHLIGHTED_ORDER);
			} else {
				GuiShowTooltips(Str.STR_SERVICE_HINT);
			}
		} break;

		case WindowEvents.WE_4: {
			if (FindWindowById(Window.WC_VEHICLE_VIEW, w.window_number) == null)
				DeleteWindow(w);
		} break;

		case WindowEvents.WE_PLACE_OBJ: {
			OrdersPlaceObj(GetVehicle(w.window_number), e.place.tile, w);
		} break;

		case WindowEvents.WE_ABORT_PLACE_OBJ: {
			CLRBIT(w.click_state, 7);
			InvalidateWidget(w, 7);
		} break;

		// check if a vehicle in a depot was clicked..
		case WindowEvents.WE_MOUSELOOP: {
			final Vehicle  v = _place_clicked_vehicle;
			/*
			 * Check if we clicked on a vehicle
			 * and if the GOTO button of this window is pressed
			 * This is because of all open order windows WindowEvents.WE_MOUSELOOP is called
			 * and if you have 3 windows open, and this check is not done
			 * the order is copied to the last open window instead of the
			 * one where GOTO is enalbed
			 */
			if (v != null && BitOps.HASBIT(w.click_state, 7)) {
				_place_clicked_vehicle = null;
				HandleOrderVehClick(GetVehicle(w.window_number), v, w);
			}
		} break;

		case WindowEvents.WE_RESIZE:
			/* Update the scroll + matrix */
			w.vscroll.cap = (w.widget[2].bottom - w.widget[2].top) / 10;
			break;
		}

	}

	static final Widget _orders_train_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_RIGHT,   14,    11,   384,     0,    13, Str.STR_8829_ORDERS,					Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_RB,      14,     0,   372,    14,    75, 0x0,											Str.STR_8852_ORDERS_LIST_CLICK_ON_ORDER},
	{  Window.WWT_SCROLLBAR,   Window.RESIZE_LRB,     14,   373,   384,    14,    75, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,     0,    52,    76,    87, Str.STR_8823_SKIP,						Str.STR_8853_SKIP_THE_CURRENT_ORDER},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,    53,   105,    76,    87, Str.STR_8824_DELETE,					Str.STR_8854_DELETE_THE_HIGHLIGHTED},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   106,   158,    76,    87, Str.STR_8825_NON_STOP,				Str.STR_8855_MAKE_THE_HIGHLIGHTED_ORDER},
	{Window.WWT_NODISTXTBTN,   Window.RESIZE_TB,      14,   159,   211,    76,    87, Str.STR_8826_GO_TO,						Str.STR_8856_INSERT_A_NEW_ORDER_BEFORE},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   212,   264,    76,    87, Str.STR_FULLLOAD_OR_SERVICE,	Str.STR_NULL},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   265,   319,    76,    87, Str.STR_8828_UNLOAD,					Str.STR_8858_MAKE_THE_HIGHLIGHTED_ORDER},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   320,   372,    76,    87, Str.STR_886F_TRANSFER, Str.STR_886D_MAKE_THE_HIGHLIGHTED_ORDER},
	{      Window.WWT_PANEL,   Window.RESIZE_RTB,     14,   373,   372,    76,    87, 0x0,											Str.STR_NULL},
	{  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   373,   384,    76,    87, 0x0,											Str.STR_Window.RESIZE_BUTTON},
	{   WIDGETS_END},
	};

	static final WindowDesc _orders_train_desc = {
		-1,-1, 385, 88,
		Window.WC_VEHICLE_ORDERS,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
		_orders_train_widgets,
		OrdersWndProc
	};

	static final Widget _orders_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_RIGHT,   14,    11,   395,     0,    13, Str.STR_8829_ORDERS,					Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_RB,      14,     0,   383,    14,    75, 0x0,											Str.STR_8852_ORDERS_LIST_CLICK_ON_ORDER},
	{  Window.WWT_SCROLLBAR,   Window.RESIZE_LRB,     14,   384,   395,    14,    75, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,     0,    63,    76,    87, Str.STR_8823_SKIP,						Str.STR_8853_SKIP_THE_CURRENT_ORDER},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,    64,   128,    76,    87, Str.STR_8824_DELETE,					Str.STR_8854_DELETE_THE_HIGHLIGHTED},
	{      Window.WWT_EMPTY,   Window.RESIZE_TB,      14,     0,     0,    76,    87, 0x0,											0x0},
	{Window.WWT_NODISTXTBTN,   Window.RESIZE_TB,      14,   129,   192,    76,    87, Str.STR_8826_GO_TO,						Str.STR_8856_INSERT_A_NEW_ORDER_BEFORE},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   193,   256,    76,    87, Str.STR_FULLLOAD_OR_SERVICE,	Str.STR_NULL},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   257,   319,    76,    87, Str.STR_8828_UNLOAD,					Str.STR_8858_MAKE_THE_HIGHLIGHTED_ORDER},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,    14,   320,   383,    76,    87, Str.STR_886F_TRANSFER, Str.STR_886D_MAKE_THE_HIGHLIGHTED_ORDER},
	{      Window.WWT_PANEL,   Window.RESIZE_RTB,     14,   384,   383,    76,    87, 0x0,											Str.STR_NULL},
	{  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   384,   395,    76,    87, 0x0,											Str.STR_Window.RESIZE_BUTTON},
	{   WIDGETS_END},
	};

	static final WindowDesc _orders_desc = {
		-1,-1, 396, 88,
		Window.WC_VEHICLE_ORDERS,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
		_orders_widgets,
		OrdersWndProc
	};

	static final Widget _other_orders_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,					Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_RIGHT,   14,    11,   331,     0,    13, Str.STR_A00B_ORDERS,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_RB,      14,     0,   319,    14,    75, 0x0,							Str.STR_8852_ORDERS_LIST_CLICK_ON_ORDER},
	{  Window.WWT_SCROLLBAR,   Window.RESIZE_LRB,     14,   320,   331,    14,    75, 0x0,							Str.STR_0190_SCROLL_BAR_SCROLLS_LIST},
	{      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL},
	{      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL},
	{      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL},
	{      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL},
	{      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL},
	{      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL},
	{      Window.WWT_PANEL,   Window.RESIZE_RTB,     14,     0,   319,    76,    87, 0x0,							Str.STR_NULL},
	{  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   320,   331,    76,    87, 0x0,							Str.STR_Window.RESIZE_BUTTON},
	{   WIDGETS_END},
	};

	static final WindowDesc _other_orders_desc = {
		-1,-1, 332, 88,
		Window.WC_VEHICLE_ORDERS,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
		_other_orders_widgets,
		OrdersWndProc
	};

	void ShowOrdersWindow(final Vehicle  v)
	{
		Window w;
		VehicleID veh = v.index;

		Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, veh);
		Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, veh);

		_alloc_wnd_parent_num = veh;

		if (v.owner != Global._local_player) {
			w = AllocateWindowDesc(&_other_orders_desc);
		} else {
			w = AllocateWindowDesc((v.type == Vehicle.VEH_Train) ? &_orders_train_desc : &_orders_desc);
		}

		if (w != null) {
			w.window_number = veh;
			w.caption_color = v.owner;
			w.vscroll.cap = 6;
			w.resize.step_height = 10;
			w.as_order_d().sel = -1;
		}
	}

}
