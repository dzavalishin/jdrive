package com.dzavalishin.xui;

import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.AirCraft;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Depot;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Order;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.game.WayPoint;
import com.dzavalishin.game.mAirport;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.util.BitOps;

public class OrderGui {


	static int OrderGetSel(final Window  w)
	{
		final Vehicle  v = Vehicle.GetVehicle(w.window_number);
		int num = w.as_order_d().sel;

		return (num >= 0 && num < v.getNum_orders()) ? num : v.getNum_orders();
	}

	static final /*StringID*/ int[] StationOrderStrings = {
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
		Order order;
		//StringID str;
		int sel;
		int y, i;
		boolean shared_orders;
		byte color;

		v = Vehicle.GetVehicle(w.window_number);

		w.disabled_state = (v.getOwner().isLocalPlayer()) ? 0 : (
			1 << 4 |   //skip
			1 << 5 |   //delete
			1 << 6 |   //non-stop
			1 << 7 |   //go-to
			1 << 8 |   //full load
			1 << 9 |   //unload
			1 << 10    //transfer
			);

		if (v.getType() != Vehicle.VEH_Train)
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 6); //disable non-stop for non-trains

		shared_orders = v.IsOrderListShared();

		if (v.getNum_orders() + (shared_orders?1:0) <= w.as_order_d().sel)
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 5); /* delete */

		if (v.getNum_orders() == 0)
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 4); /* skip */

		w.SetVScrollCount( v.getNum_orders() + 1);

		sel = OrderGetSel(w);
		Global.SetDParam(2, Str.STR_8827_FULL_LOAD);

		order = v.GetVehicleOrder(sel);

		if (order != null) {
			switch (order.getType()) {
				case Order.OT_GOTO_STATION:
					break;

				case Order.OT_GOTO_DEPOT:
					w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 9);	/* unload */
					w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 10); /* transfer */
					Global.SetDParam(2,Str.STR_SERVICE);
					break;

				case Order.OT_GOTO_WAYPOINT:
					w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 8); /* full load */
					w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 9); /* unload */
					w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 10); /* transfer */
					break;

				default:
					w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 6); /* nonstop */
					w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 8);	/* full load */
					w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 9);	/* unload */
			}
		} else {
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 6); /* nonstop */
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 8);	/* full load */
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 9);	/* unload */
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 10); /* transfer */
		}

		Global.SetDParam(0, v.getString_id());
		Global.SetDParam(1, v.getUnitnumber().id);
		w.DrawWindowWidgets();

		y = 15;

		i = w.vscroll.pos;
		order = v.GetVehicleOrder(i);
		while (order != null) {
			int str = (v.getCur_order_index() == i) ? Str.STR_8805 : Str.STR_8804;

			if (i - w.vscroll.pos < w.vscroll.getCap()) {
				Global.SetDParam(1, 6);

				switch (order.getType()) {
					case Order.OT_GOTO_STATION:
						Global.SetDParam(1, StationOrderStrings[order.getFlags()]);
						Global.SetDParam(2, order.getStation());
						break;

					case Order.OT_GOTO_DEPOT: {
						//StringID 
						int s = Str.STR_NULL;

						if (v.getType() == Vehicle.VEH_Aircraft) {
							s = Str.STR_GO_TO_AIRPORT_HANGAR;
							Global.SetDParam(2, order.getStation());
						} else {
							Global.SetDParam(2, Depot.GetDepot(order.getStation()).getTownIndex());

							switch (v.getType()) {
								case Vehicle.VEH_Train: s = 0 != (order.getFlags() & Order.OF_NON_STOP) ? Str.STR_880F_GO_NON_STOP_TO_TRAIN_DEPOT : Str.STR_GO_TO_TRAIN_DEPOT; break;
								case Vehicle.VEH_Road:  s = Str.STR_9038_GO_TO_ROADVEH_DEPOT; break;
								case Vehicle.VEH_Ship:  s = Str.STR_GO_TO_SHIP_DEPOT; break;
								default: break;
							}
						}

						if(order.hasFlags(Order.OF_FULL_LOAD)) s++; /* service at */

						Global.SetDParam(1, s);
						break;
					}

					case Order.OT_GOTO_WAYPOINT:
						Global.SetDParam(1, order.isNonStop() ? Str.STR_GO_NON_STOP_TO_WAYPOINT : Str.STR_GO_TO_WAYPOINT);
						Global.SetDParam(2, order.getStation());
						break;
				}

				color = (byte) ((i == w.as_order_d().sel) ? 0xC : 0x10);
				Global.SetDParam(0, i + 1);
				if (order.getType() != Order.OT_DUMMY) {
					Gfx.DrawString(2, y, str, color);
				} else {
					Global.SetDParam(1, Str.STR_INVALID_ORDER);
					Global.SetDParam(2, order.getStation());
					Gfx.DrawString(2, y, str, color);
				}
				y += 10;
			}

			i++;
			order = order.getNext();
		}

		if (i - w.vscroll.pos < w.vscroll.getCap()) {
			int str = shared_orders ? Str.STR_END_OF_SHARED_ORDERS : Str.STR_882A_END_OF_ORDERS;
			color = (byte) ((i == w.as_order_d().sel) ? 0xC : 0x10);
			Gfx.DrawString(2, y, str, color);
		}
	}

	static Order GetOrderCmdFromTile(final Vehicle v, TileIndex tile)
	{
		//Order order = new Order();
		int st_index;

		// check depot first
		if (Global._patches.gotodepot.get()) {
			switch (tile.GetTileType()) {
			case MP_RAILWAY:
				if (v.getType() == Vehicle.VEH_Train && tile.IsTileOwner(Global.gs._local_player)) {
					if ((tile.getMap().m5&0xFC)==0xC0) 
					{
						/*
						order.type = Order.OT_GOTO_DEPOT;
						order.flags = Order.OF_PART_OF_ORDERS;
						order.station = Depot.GetDepotByTile(tile).index;
						return order;*/
						return new Order( Order.OT_GOTO_DEPOT, Order.OF_PART_OF_ORDERS,  Depot.GetDepotByTile(tile).getIndex() );
					}
				}
				break;

			case MP_STREET:
				if ((tile.getMap().m5 & 0xF0) == 0x20 && v.getType() == Vehicle.VEH_Road && tile.IsTileOwner(Global.gs._local_player)) {
					/*order.type = Order.OT_GOTO_DEPOT;
					order.flags = Order.OF_PART_OF_ORDERS;
					order.station = Depot.GetDepotByTile(tile).index;
					return order;*/
					return new Order( Order.OT_GOTO_DEPOT, Order.OF_PART_OF_ORDERS,  Depot.GetDepotByTile(tile).getIndex() );
				}
				break;

			case MP_STATION:
				if (v.getType() != Vehicle.VEH_Aircraft) break;
				if (AirCraft.IsAircraftHangarTile(tile) && tile.IsTileOwner(Global.gs._local_player)) {
					/*order.type = Order.OT_GOTO_DEPOT;
					order.flags = Order.OF_PART_OF_ORDERS;
					order.station = tile.getMap().m2;
					return order; */
					return new Order( Order.OT_GOTO_DEPOT, Order.OF_PART_OF_ORDERS, tile.getMap().m2 );
				}
				break;

			case MP_WATER:
				if (v.getType() != Vehicle.VEH_Ship) break;
				if (Depot.IsTileDepotType(tile, TransportType.Water) &&
						tile.IsTileOwner(Global.gs._local_player)) {
					switch (tile.getMap().m5) {
						case 0x81: tile = tile.isub(TileIndex.TileDiffXY(1, 0)); break;
						case 0x83: tile = tile.isub(TileIndex.TileDiffXY(0, 1)); break;
					}
					/*order.type = Order.OT_GOTO_DEPOT;
					order.flags = Order.OF_PART_OF_ORDERS;
					order.station = Depot.GetDepotByTile(tile).index;
					return order;*/
					return new Order( Order.OT_GOTO_DEPOT, Order.OF_PART_OF_ORDERS,  Depot.GetDepotByTile(tile).getIndex() );
				}

				default:
					break;
			}
		}

		// check waypoint
		if (tile.IsTileType( TileTypes.MP_RAILWAY) &&
				v.getType() == Vehicle.VEH_Train &&
						tile.IsTileOwner(Global.gs._local_player) &&
				WayPoint.IsRailWaypoint(tile)) {
			/*order.type = Order.OT_GOTO_WAYPOINT;
			order.flags = 0;
			order.station = WayPoint.GetWaypointByTile(tile).index;
			return order;*/
			return new Order( Order.OT_GOTO_WAYPOINT, 0,  WayPoint.GetWaypointByTile(tile).index );
		}

		if (tile.IsTileType( TileTypes.MP_STATION)) {
			final Station st = Station.GetStation(st_index = tile.getMap().m2);
			
			if (st.getOwner().equals(PlayerID.getCurrent()) || st.getOwner().isNone() || mAirport.MA_OwnerHandler(st.getOwner())) {
				byte facil;
				
				/*
				(facil=FACIL_DOCK, v.type == Vehicle.VEH_Ship) ||
				(facil=FACIL_TRAIN, v.type == Vehicle.VEH_Train) ||
				(facil=FACIL_AIRPORT, v.type == Vehicle.VEH_Aircraft) ||
				(facil=FACIL_BUS_STOP, v.type == Vehicle.VEH_Road && v.cargo_type == AcceptedCargo.CT_PASSENGERS) ||
				(facil=FACIL_TRUCK_STOP, 1);
				*/
				
				switch (v.getType()) {
				case Vehicle.VEH_Ship:		facil=Station.FACIL_DOCK;	break;
				case Vehicle.VEH_Train: 	facil=Station.FACIL_TRAIN;	break;
				case Vehicle.VEH_Aircraft:	facil=Station.FACIL_AIRPORT;break;
					
				default:
					if(v.getType() == Vehicle.VEH_Road && v.getCargo_type() == AcceptedCargo.CT_PASSENGERS)
						facil=Station.FACIL_BUS_STOP;
					else
						facil=Station.FACIL_TRUCK_STOP;
					break;
				}
				
				
				if(0 != (st.getFacilities() & facil)) {
					/*order.type = Order.OT_GOTO_STATION;
					order.flags = 0;
					order.station = st_index;
					return order;*/
					return new Order( Order.OT_GOTO_STATION, 0, st_index );
				}
			}
		}

		// not found
		/*order.type = Order.OT_NOTHING;
		order.flags = 0;
		return order;*/
		return new Order( Order.OT_NOTHING, 0, 0 );
	}

	static boolean HandleOrderVehClick(final Vehicle  v, Vehicle  u, Window  w)
	{
		if (u.getType() != v.getType()) return false;

		if (u.getType() == Vehicle.VEH_Train && !u.IsFrontEngine()) {
			u = u.GetFirstVehicleInChain();
			if (!u.IsFrontEngine()) return false;
		}

		// v is vehicle getting orders. Only copy/clone orders if vehicle doesn't have any orders yet
		// obviously if you press CTRL on a non-empty orders vehicle you know what you are doing
		if (v.getNum_orders() != 0 && !Global._ctrl_pressed) return false;

		if (Cmd.DoCommandP(v.getTile(), v.index | (u.index << 16), Global._ctrl_pressed ? 0 : 1, null,
				Global._ctrl_pressed ? Cmd.CMD_CLONE_ORDER | Cmd.CMD_MSG(Str.STR_CANT_SHARE_ORDER_LIST) : Cmd.CMD_CLONE_ORDER | Cmd.CMD_MSG(Str.STR_CANT_COPY_ORDER_LIST))) {
			w.as_order_d().sel = -1;
			ViewPort.ResetObjectToPlace();
		}

		return true;
	}

	static void OrdersPlaceObj(final Vehicle  v, TileIndex tile, Window  w)
	{
		Order cmd;
		final Vehicle  u;

		// check if we're clicking on a vehicle first.. clone orders in that case.
		u = ViewPort.CheckMouseOverVehicle();
		if (u != null && HandleOrderVehClick(v, u, w)) return;

		cmd = GetOrderCmdFromTile(v, tile);
		if (cmd.getType() == Order.OT_NOTHING) return;

		if (Cmd.DoCommandP(v.getTile(), v.index + (OrderGetSel(w) << 16), Order.PackOrder(cmd), null, Cmd.CMD_INSERT_ORDER | Cmd.CMD_MSG(Str.STR_8833_CAN_T_INSERT_NEW_ORDER))) {
			if (w.as_order_d().sel != -1) w.as_order_d().sel++;
			ViewPort.ResetObjectToPlace();
		}
	}

	static void OrderClick_Goto(Window  w, final Vehicle  v)
	{
		w.InvalidateWidget(7);
		w.click_state = BitOps.RETTOGGLEBIT(w.click_state, 7);
		if (BitOps.HASBIT(w.click_state, 7)) {
			Global._place_clicked_vehicle = null;
			ViewPort.SetObjectToPlaceWnd(Sprite.ANIMCURSOR_PICKSTATION, 1, w);
		} else {
			ViewPort.ResetObjectToPlace();
		}
	}

	static void OrderClick_FullLoad(Window  w, final Vehicle  v)
	{
		Cmd.DoCommandP(v.getTile(), v.index + (OrderGetSel(w) << 16), Order.OFB_FULL_LOAD, null, Cmd.CMD_MODIFY_ORDER | Cmd.CMD_MSG(Str.STR_8835_CAN_T_MODIFY_THIS_ORDER));
	}

	static void OrderClick_Unload(Window  w, final Vehicle  v)
	{
		Cmd.DoCommandP(v.getTile(), v.index + (OrderGetSel(w) << 16), Order.OFB_UNLOAD,    null, Cmd.CMD_MODIFY_ORDER | Cmd.CMD_MSG(Str.STR_8835_CAN_T_MODIFY_THIS_ORDER));
	}

	static void OrderClick_Nonstop(Window  w, final Vehicle  v)
	{
		Cmd.DoCommandP(v.getTile(), v.index + (OrderGetSel(w) << 16), Order.OFB_NON_STOP,  null, Cmd.CMD_MODIFY_ORDER | Cmd.CMD_MSG(Str.STR_8835_CAN_T_MODIFY_THIS_ORDER));
	}

	static void OrderClick_Transfer(Window  w, final Vehicle  v)
	{
		Cmd.DoCommandP(v.getTile(), v.index + (OrderGetSel(w) <<  16), Order.OFB_TRANSFER, null, Cmd.CMD_MODIFY_ORDER | Cmd.CMD_MSG(Str.STR_8835_CAN_T_MODIFY_THIS_ORDER));
	}

	static void OrderClick_Skip(Window  w, final Vehicle  v)
	{
		Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_SKIP_ORDER);
	}

	static void OrderClick_Delete(Window  w, final Vehicle  v)
	{
		Cmd.DoCommandP(v.getTile(), v.index, OrderGetSel(w), null, Cmd.CMD_DELETE_ORDER | Cmd.CMD_MSG(Str.STR_8834_CAN_T_DELETE_THIS_ORDER));
	}


	static final OnVehicleButtonClick _order_button_proc[] = {
			OrderGui::OrderClick_Skip,
			OrderGui::OrderClick_Delete,
			OrderGui::OrderClick_Nonstop,
			OrderGui::OrderClick_Goto,
			OrderGui::OrderClick_FullLoad,
			OrderGui::OrderClick_Unload,
			OrderGui::OrderClick_Transfer
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
		case WE_PAINT:
			DrawOrdersWindow(w);
			break;

		case WE_CLICK: {
			Vehicle v = Vehicle.GetVehicle(w.window_number);
			switch(e.widget) {
			case 2: { /* orders list */
				int sel;
				sel = (e.pt.y - 15) / 10;

				if (sel >= w.vscroll.getCap())
					return;

				sel += w.vscroll.pos;

				if (Global._ctrl_pressed && sel < v.getNum_orders()) 
				{
					final Order ord = v.GetVehicleOrder(sel);
					TileIndex xy = ord.getTargetXy();

					if (xy != null)
						ViewPort.ScrollMainWindowToTile(xy);

					return;
				}

				if (sel == w.as_order_d().sel) sel = -1;
				w.as_order_d().sel = sel;
				w.SetWindowDirty();
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

		case WE_KEYPRESS: {
			Vehicle v = Vehicle.GetVehicle(w.window_number);
			int i;

			for(i = 0; i < _order_keycodes.length; i++) {
				if (e.keycode == _order_keycodes[i]) {
					e.cont = false;
					//see if the button is disabled
					if (!(BitOps.HASBIT(w.disabled_state, (i + 4)))) {
						_order_button_proc[i].accept(w, v);
					}
					break;
				}
			}
			break;
		}

		case WE_RCLICK: {
			final Vehicle  v = Vehicle.GetVehicle(w.window_number);
			int s = OrderGetSel(w);

			if (e.widget != 8) break;
			if (s == v.getNum_orders() || v.GetVehicleOrder(s).getType() != Order.OT_GOTO_DEPOT) {
				MiscGui.GuiShowTooltips(Str.STR_8857_MAKE_THE_HIGHLIGHTED_ORDER);
			} else {
				MiscGui.GuiShowTooltips(Str.STR_SERVICE_HINT);
			}
		} break;

		case WE_4: {
			if (Window.FindWindowById(Window.WC_VEHICLE_VIEW, w.window_number) == null)
				w.DeleteWindow();
		} break;

		case WE_PLACE_OBJ: {
			OrdersPlaceObj(Vehicle.GetVehicle(w.window_number), e.tile, w);
		} break;

		case WE_ABORT_PLACE_OBJ: {
			w.click_state = BitOps.RETCLRBIT(w.click_state, 7);
			w.InvalidateWidget(7);
		} break;

		// check if a vehicle in a depot was clicked..
		case WE_MOUSELOOP: {
			final Vehicle  v = Global._place_clicked_vehicle;
			/*
			 * Check if we clicked on a vehicle
			 * and if the GOTO button of this window is pressed
			 * This is because of all open order windows WindowEvents.WE_MOUSELOOP is called
			 * and if you have 3 windows open, and this check is not done
			 * the order is copied to the last open window instead of the
			 * one where GOTO is enalbed
			 */
			if (v != null && BitOps.HASBIT(w.click_state, 7)) {
				Global._place_clicked_vehicle = null;
				HandleOrderVehClick(Vehicle.GetVehicle(w.window_number), v, w);
			}
		} break;

		case WE_RESIZE:
			/* Update the scroll + matrix */
			w.vscroll.setCap((w.widget.get(2).bottom - w.widget.get(2).top) / 10);
			break;
		default:
			break;
		}

	}

	static final Widget _orders_train_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_RIGHT,   14,    11,   384,     0,    13, Str.STR_8829_ORDERS,					Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_RB,      14,     0,   372,    14,    75, 0x0,											Str.STR_8852_ORDERS_LIST_CLICK_ON_ORDER),
	new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_LRB,     14,   373,   384,    14,    75, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,     0,    52,    76,    87, Str.STR_8823_SKIP,						Str.STR_8853_SKIP_THE_CURRENT_ORDER),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,    53,   105,    76,    87, Str.STR_8824_DELETE,					Str.STR_8854_DELETE_THE_HIGHLIGHTED),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   106,   158,    76,    87, Str.STR_8825_NON_STOP,				Str.STR_8855_MAKE_THE_HIGHLIGHTED_ORDER),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_TB,      14,   159,   211,    76,    87, Str.STR_8826_GO_TO,						Str.STR_8856_INSERT_A_NEW_ORDER_BEFORE),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   212,   264,    76,    87, Str.STR_FULLLOAD_OR_SERVICE,	Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   265,   319,    76,    87, Str.STR_8828_UNLOAD,					Str.STR_8858_MAKE_THE_HIGHLIGHTED_ORDER),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   320,   372,    76,    87, Str.STR_886F_TRANSFER, Str.STR_886D_MAKE_THE_HIGHLIGHTED_ORDER),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_RTB,     14,   373,   372,    76,    87, 0x0,											Str.STR_NULL),
	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   373,   384,    76,    87, 0x0,											Str.STR_RESIZE_BUTTON),
	};

	static final WindowDesc _orders_train_desc = new WindowDesc(
		-1,-1, 385, 88,
		Window.WC_VEHICLE_ORDERS,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
		_orders_train_widgets,
		OrderGui::OrdersWndProc
	);

	static final Widget _orders_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_RIGHT,   14,    11,   395,     0,    13, Str.STR_8829_ORDERS,					Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_RB,      14,     0,   383,    14,    75, 0x0,											Str.STR_8852_ORDERS_LIST_CLICK_ON_ORDER),
	new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_LRB,     14,   384,   395,    14,    75, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,     0,    63,    76,    87, Str.STR_8823_SKIP,						Str.STR_8853_SKIP_THE_CURRENT_ORDER),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,    64,   128,    76,    87, Str.STR_8824_DELETE,					Str.STR_8854_DELETE_THE_HIGHLIGHTED),
	new Widget(      Window.WWT_EMPTY,   Window.RESIZE_TB,      14,     0,     0,    76,    87, 0x0,											0x0),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_TB,      14,   129,   192,    76,    87, Str.STR_8826_GO_TO,						Str.STR_8856_INSERT_A_NEW_ORDER_BEFORE),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   193,   256,    76,    87, Str.STR_FULLLOAD_OR_SERVICE,	Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,      14,   257,   319,    76,    87, Str.STR_8828_UNLOAD,					Str.STR_8858_MAKE_THE_HIGHLIGHTED_ORDER),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_TB,    14,   320,   383,    76,    87, Str.STR_886F_TRANSFER, Str.STR_886D_MAKE_THE_HIGHLIGHTED_ORDER),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_RTB,     14,   384,   383,    76,    87, 0x0,											Str.STR_NULL),
	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   384,   395,    76,    87, 0x0,											Str.STR_RESIZE_BUTTON),
	};

	static final WindowDesc _orders_desc = new WindowDesc(
		-1,-1, 396, 88,
		Window.WC_VEHICLE_ORDERS,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
		_orders_widgets,
		OrderGui::OrdersWndProc
	);

	static final Widget _other_orders_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,					Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_RIGHT,   14,    11,   331,     0,    13, Str.STR_A00B_ORDERS,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_RB,      14,     0,   319,    14,    75, 0x0,							Str.STR_8852_ORDERS_LIST_CLICK_ON_ORDER),
	new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_LRB,     14,   320,   331,    14,    75, 0x0,							Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL),
	new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL),
	new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL),
	new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL),
	new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL),
	new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,    14,     0,   319,    76,    87, 0x0,                     Str.STR_NULL),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_RTB,     14,     0,   319,    76,    87, 0x0,							Str.STR_NULL),
	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   320,   331,    76,    87, 0x0,							Str.STR_RESIZE_BUTTON),
	};

	static final WindowDesc _other_orders_desc = new WindowDesc(
		-1,-1, 332, 88,
		Window.WC_VEHICLE_ORDERS,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
		_other_orders_widgets,
		OrderGui::OrdersWndProc
	);

	public static void ShowOrdersWindow(final Vehicle  v)
	{
		Window w;
		//VehicleID
		int veh = v.index;

		Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, veh);
		Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, veh);

		if (!v.getOwner().isLocalPlayer()) {
			w = Window.AllocateWindowDesc(_other_orders_desc, veh);
		} else {
			w = Window.AllocateWindowDesc((v.getType() == Vehicle.VEH_Train) ? _orders_train_desc : _orders_desc, veh);
		}

		if (w != null) {
			w.window_number = veh;
			w.caption_color = (byte) v.getOwner().id;
			w.vscroll.setCap(6);
			w.resize.step_height = 10;
			w.as_order_d().sel = -1;
		}
	}

}


//typedef void OnVehicleButtonClick(Window  w, final Vehicle  v);
@FunctionalInterface
interface OnVehicleButtonClick
{
	void accept(Window w, Vehicle v);
}

