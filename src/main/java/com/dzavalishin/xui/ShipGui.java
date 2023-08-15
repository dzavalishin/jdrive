package com.dzavalishin.xui;

import java.util.Iterator;

import com.dzavalishin.enums.TransportType;
import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Depot;
import com.dzavalishin.game.Engine;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Order;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.Ship;
import com.dzavalishin.game.ShipVehicleInfo;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StationID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.YearMonthDay;
import com.dzavalishin.wcustom.vehiclelist_d;

public class ShipGui 
{

	/**
	 * Draw the purchase info details of a ship at a given location.
	 * @param x,y location where to draw the info
	 * @param engine_number the engine of which to draw the info of
	 */
	static void DrawShipPurchaseInfo(int x, int y, /*EngineID*/ int engine_number)
	{
		final ShipVehicleInfo svi = Engine.ShipVehInfo(engine_number);
		final Engine  e;

		/* Purchase cost - Max speed */
		Global.SetDParam(0, ((int)(svi.base_cost * (Global._price.ship_base/8)))>>5);
		Global.SetDParam(1, svi.max_speed * 10 >> 5);
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_COST_SPEED, 0);
		y += 10;

		/* Cargo type + capacity */
		Global.SetDParam(0, Global._cargoc.names_long[svi.cargo_type]);
		Global.SetDParam(1, svi.capacity);
		Global.SetDParam(2, svi.refittable!=0 ? Str.STR_9842_REFITTABLE : Str.STR_EMPTY);
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_CAPACITY, 0);
		y += 10;

		/* Running cost */
		Global.SetDParam(0, ((int)(svi.running_cost * Global._price.ship_running)) >> 8);
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_RUNNINGCOST, 0);
		y += 10;

		/* Design date - Life length */
		e = Engine.GetEngine(engine_number);
		YearMonthDay ymd = new YearMonthDay(e.getIntro_date());
		//YearMonthDay.ConvertDayToYMD(ymd, e.getIntro_date());
		Global.SetDParam(0, ymd.year + 1920);
		Global.SetDParam(1, e.getLifelength());
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_DESIGNED_LIFE, 0);
		y += 10;

		/* Reliability */
		Global.SetDParam(0, e.getReliability() * 100 >> 16);
		Gfx.DrawString(x,y, Str.STR_PURCHASE_INFO_RELIABILITY, 0);
		y += 10;
	}

	static void DrawShipImage(final Vehicle v, int x, int y, /*VehicleID*/ int selection)
	{
		int image = Ship.GetShipImage(v, 6);
		int ormod = Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(v.getOwner()));
		Gfx.DrawSprite(image | ormod, x + 32, y + 10);

		if (v.index == selection) {
			Gfx.DrawFrameRect(x - 5, y - 1, x + 67, y + 21, 15, Window.FR_BORDERONLY);
		}
	}

	static void ShipRefitWndProc(Window w, WindowEvent e)
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
				int cost = Cmd.DoCommandByTile(v.getTile(), v.index, w.as_refit_d().cargo, Cmd.DC_QUERY_COST, Cmd.CMD_REFIT_SHIP);
				if (!Cmd.CmdFailed(cost)) {
					Global.SetDParam(2, cost);
					Global.SetDParam(0, Global._cargoc.names_long[w.as_refit_d().cargo]);
					Global.SetDParam(1, v.getCargo_cap());
					Gfx.DrawString(1, 137, Str.STR_9840_NEW_CAPACITY_COST_OF_REFIT, 0);
				}
			}
		}	break;

		case WE_CLICK:
			switch (e.widget) {
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
					if (Cmd.DoCommandP(v.getTile(), v.index, w.as_refit_d().cargo, null, Cmd.CMD_REFIT_SHIP | Cmd.CMD_MSG(Str.STR_9841_CAN_T_REFIT_SHIP)))
						w.DeleteWindow();
				}
				break;
			}
			break;
		default:
			break;
		}
	}


	static final Widget _ship_refit_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,						Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   239,     0,    13, Str.STR_983B_REFIT,			Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   239,    14,   135, 0x0,									Str.STR_983D_SELECT_TYPE_OF_CARGO_FOR),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   239,   136,   157, 0x0,									Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,   239,   158,   169, Str.STR_983C_REFIT_SHIP,	Str.STR_983E_REFIT_SHIP_TO_CARRY_HIGHLIGHTED),
	};

	static final WindowDesc _ship_refit_desc = new WindowDesc(
			-1,-1, 240, 170,
			Window.WC_VEHICLE_REFIT,Window.WC_VEHICLE_VIEW,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_ship_refit_widgets,
			ShipGui::ShipRefitWndProc
	);

	static void ShowShipRefitWindow(final Vehicle  v)
	{
		Window w;

		Window.DeleteWindowById(Window.WC_VEHICLE_REFIT, v.index);

		//_alloc_wnd_parent_num = v.index;
		w = Window.AllocateWindowDesc(_ship_refit_desc,v.index);
		w.window_number = v.index;
		w.caption_color = 0xFF & v.getOwner().id;
		w.as_refit_d().sel = -1;
	}

	static void ShipDetailsWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			final Vehicle v = Vehicle.GetVehicle(w.window_number);
			//StringID 
			int str;

			w.disabled_state = v.getOwner().isLocalPlayer() ? 0 : (1 << 2);
			if (0==Global._patches.servint_ships) // disable service-scroller when interval is set to disabled
				w.disabled_state |= (1 << 5) | (1 << 6);

			Global.SetDParam(0, v.getString_id());
			Global.SetDParam(1, v.getUnitnumber().id);
			w.DrawWindowWidgets();

			/* Draw running cost */
			{
				int year = v.getAge() / 366;

				Global.SetDParam(1, year);

				Global.SetDParam(0, (v.getAge() + 365 < v.getMax_age()) ? Str.STR_AGE : Str.STR_AGE_RED);
				Global.SetDParam(2, v.getMax_age() / 366);
				Global.SetDParam(3, Engine.ShipVehInfo(v.getEngine_type().id).running_cost * Global._price.ship_running / 256 );
				Gfx.DrawString(2, 15, Str.STR_9812_AGE_RUNNING_COST_YR, 0);
			}

			/* Draw max speed */
			{
				Global.SetDParam(0, v.getMax_speed() * 10 >> 5);
				Gfx.DrawString(2, 25, Str.STR_9813_MAX_SPEED, 0);
			}

			/* Draw profit */
			{
				Global.SetDParam(0, v.getProfit_this_year());
				Global.SetDParam(1, v.getProfit_last_year());
				Gfx.DrawString(2, 35, Str.STR_9814_PROFIT_THIS_YEAR_LAST_YEAR, 0);
			}

			/* Draw breakdown & reliability */
			{
				Global.SetDParam(0, v.getReliability() * 100 >> 16);
				Global.SetDParam(1, v.getBreakdowns_since_last_service());
				Gfx.DrawString(2, 45, Str.STR_9815_RELIABILITY_BREAKDOWNS, 0);
			}

			/* Draw service interval text */
			{
				Global.SetDParam(0, v.getService_interval());
				Global.SetDParam(1, v.date_of_last_service);
				Gfx.DrawString(13, 90, Global._patches.servint_ispercent?Str.STR_SERVICING_INTERVAL_PERCENT:Str.STR_883C_SERVICING_INTERVAL_DAYS, 0);
			}

			DrawShipImage(v, 3, 57, Vehicle.INVALID_VEHICLE);

			Global.SetDParam(1, 1920 + v.getBuild_year());
			Global.SetDParam(0, Engine.GetCustomEngineName(v.getEngine_type().id).id);
			Global.SetDParam(2, v.getValue());
			Gfx.DrawString(74, 57, Str.STR_9816_BUILT_VALUE, 0);

			Global.SetDParam(0, Global._cargoc.names_long[v.getCargo_type()]);
			Global.SetDParam(1, v.getCargo_cap());
			Gfx.DrawString(74, 67, Str.STR_9817_CAPACITY, 0);

			str = Str.STR_8812_EMPTY;
			if (v.getCargo_count() != 0) {
				Global.SetDParam(0, v.getCargo_type());
				Global.SetDParam(1, v.getCargo_count());
				Global.SetDParam(2, v.getCargo_source());
				str = Str.STR_8813_FROM;
			}
			Gfx.DrawString(74, 78, str, 0);
		} break;

		case WE_CLICK: {
			int mod;
			final Vehicle v;
			switch (e.widget) {
			case 2: /* rename */
				v = Vehicle.GetVehicle(w.window_number);
				Global.SetDParam(0, v.getUnitnumber().id);
				MiscGui.ShowQueryString( new StringID(v.getString_id()), new StringID(Str.STR_9831_NAME_SHIP), 31, 150, w.getWindow_class(), w.window_number);
				break;
			case 5: /* increase int */
			case 6: /* decrease int */
				if( e.widget == 6)
					mod = Global._ctrl_pressed?- 5 : -10;
				else // 5
					mod = Global._ctrl_pressed? 5 : 10;

					v = Vehicle.GetVehicle(w.window_number);

				mod = Depot.GetServiceIntervalClamped(mod + v.getService_interval());
				if (mod == v.getService_interval()) return;

				Cmd.DoCommandP(v.getTile(), v.index, mod, null, Cmd.CMD_CHANGE_SHIP_SERVICE_INT | Cmd.CMD_MSG(Str.STR_018A_CAN_T_CHANGE_SERVICING));
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
						Cmd.CMD_NAME_VEHICLE | Cmd.CMD_MSG(Str.STR_9832_CAN_T_NAME_SHIP));
			}
			break;
		default:
			break;
		}
	}


	static final Widget _ship_details_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,				Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   364,     0,    13, Str.STR_9811_DETAILS,Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   365,   404,     0,    13, Str.STR_01AA_NAME,		Str.STR_982F_NAME_SHIP),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   404,    14,    55, 0x0,							Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   404,    56,    88, 0x0,							Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    10,    89,    94, Str.STR_0188,				Str.STR_884D_INCREASE_SERVICING_INTERVAL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    10,    95,   100, Str.STR_0189,				Str.STR_884E_DECREASE_SERVICING_INTERVAL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    11,   404,    89,   100, 0x0,							Str.STR_NULL),
	};

	static final WindowDesc _ship_details_desc = new WindowDesc(
			-1,-1, 405, 101,
			Window.WC_VEHICLE_DETAILS,Window.WC_VEHICLE_VIEW,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_ship_details_widgets,
			ShipGui::ShipDetailsWndProc
	);

	static void ShowShipDetailsWindow(final Vehicle  v)
	{
		Window w;
		//VehicleID 
		int veh = v.index;

		Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, veh);
		Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, veh);
		//_alloc_wnd_parent_num = veh;
		w = Window.AllocateWindowDesc(_ship_details_desc, veh);
		w.window_number = veh;
		w.caption_color = 0xFF & v.getOwner().id;
	}

	public static void CcBuildShip(boolean success, TileIndex tile, int p1, int p2)
	{
		final Vehicle  v;
		if (!success) return;

		v = Vehicle.GetVehicle(Global._new_ship_id);
		if (v.getTile().equals(Global._backup_orders_tile)) {
			Global._backup_orders_tile = null;
			Vehicle.RestoreVehicleOrders(v, Global._backup_orders_data[0]);
		}
		ShowShipViewWindow(v);
	}

	public static void CcCloneShip(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) ShowShipViewWindow(Vehicle.GetVehicle(Global._new_ship_id));
	}

	static void NewShipWndProc(Window w, WindowEvent we)
	{
		switch (we.event) {
		case WE_PAINT:
			if (w.window_number == 0) w.disabled_state = 1 << 5;

			// Setup scroll count
			{
				int count = 0;
				int num = Global.NUM_SHIP_ENGINES;
				int i = 0;

				do {
					final Engine e = Engine.GetEngine(Global.SHIP_ENGINES_INDEX + i++);
					if (e.isAvailableToMe()) count++;
				} while (--num > 0);
				w.SetVScrollCount( count);
			}

			w.DrawWindowWidgets();

			{
				int num = Global.NUM_SHIP_ENGINES;
				int x = 2;
				int y = 15;
				int sel = w.as_buildtrain_d().sel_index;
				int pos = w.vscroll.pos;
				//EngineID 
				int engine_id = Global.SHIP_ENGINES_INDEX;
				//EngineID 
				int selected_id = Engine.INVALID_ENGINE;
				int ei = 0;
				do {
					final Engine  e = Engine.GetEngine(Global.SHIP_ENGINES_INDEX + ei++);
					if (e.isAvailableToMe()) {
						if (sel==0) selected_id = engine_id;
						if (BitOps.IS_INT_INSIDE(--pos, -w.vscroll.getCap(), 0))
						{
							Gfx.DrawString(x+75, y+7, Engine.GetCustomEngineName(engine_id), sel==0 ? 0xC : 0x10);
							Ship.DrawShipEngine(x+35, y+10, engine_id, Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)));
							y += 24;
						}
						sel--;
					}
					++engine_id;
				} while (--num > 0);

				w.as_buildtrain_d().sel_engine = selected_id;

				if (selected_id != Engine.INVALID_ENGINE) {
					DrawShipPurchaseInfo(2, w.widget.get(4).top + 1, selected_id);
				}
			}
			break;

		case WE_CLICK:
			switch(we.widget) {
			case 2: { /* listbox */
				int i = (we.pt.y - 14) / 24;
				if (i < w.vscroll.getCap()) {
					w.as_buildtrain_d().sel_index =  (i + w.vscroll.pos);
					w.SetWindowDirty();
				}
			} break;
			case 5: { /* build */
				//EngineID 
				int sel_eng = w.as_buildtrain_d().sel_engine;
				if (sel_eng != Engine.INVALID_ENGINE)
					Cmd.DoCommandP( TileIndex.get( w.window_number ), sel_eng, 0, ShipGui::CcBuildShip, Cmd.CMD_BUILD_SHIP | Cmd.CMD_MSG(Str.STR_980D_CAN_T_BUILD_SHIP));
			} break;

			case 6:	{ /* rename */
				//EngineID 
				int sel_eng = w.as_buildtrain_d().sel_engine;
				if (sel_eng != Engine.INVALID_ENGINE) {
					w.as_buildtrain_d().rename_engine = sel_eng;
					MiscGui.ShowQueryString(Engine.GetCustomEngineName(sel_eng),
							new StringID( Str.STR_9838_RENAME_SHIP_TYPE ), 31, 160, w.getWindow_class(), w.window_number);
				}
			}	break;
			}
			break;

		case WE_4:
			/* TODO Why? 
			if (null != Window.FindWindowById(Window.WC_VEHICLE_DEPOT, w.window_number)) 
			{
				w.DeleteWindow();
			} */
			break;

		case WE_ON_EDIT_TEXT:
			if (we.str != null) {
				Global._cmd_text = we.str;
				Cmd.DoCommandP(null, w.as_buildtrain_d().rename_engine, 0, null,
						Cmd.CMD_RENAME_ENGINE | Cmd.CMD_MSG(Str.STR_9839_CAN_T_RENAME_SHIP_TYPE));
			}
			break;

		case WE_RESIZE:
			w.vscroll.setCap(w.vscroll.getCap() + we.diff.y / 24);
			w.widget.get(2).unkA = (w.vscroll.getCap() << 8) + 1;
			break;
		default:
			break;

		}
	}

	static final Widget _new_ship_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,						Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   254,     0,    13, Str.STR_9808_NEW_SHIPS,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_MATRIX, Window.RESIZE_BOTTOM,    14,     0,   242,    14,   109, 0x401,								Str.STR_9825_SHIP_SELECTION_LIST_CLICK),
			new Widget(  Window.WWT_SCROLLBAR, Window.RESIZE_BOTTOM,    14,   243,   254,    14,   109, 0x0,									Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(     Window.WWT_IMGBTN,     Window.RESIZE_TB,    14,     0,   254,   110,   161, 0x0,									Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   121,   162,   173, Str.STR_9809_BUILD_SHIP,	Str.STR_9826_BUILD_THE_HIGHLIGHTED_SHIP),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   122,   242,   162,   173, Str.STR_9836_RENAME,			Str.STR_9837_RENAME_SHIP_TYPE),
			new Widget(  Window.WWT_RESIZEBOX,     Window.RESIZE_TB,    14,   243,   254,   162,   173, 0x0,											Str.STR_RESIZE_BUTTON),
	};

	static final WindowDesc _new_ship_desc = new WindowDesc(
			-1, -1, 255, 174,
			Window.WC_BUILD_VEHICLE,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
			_new_ship_widgets,
			ShipGui::NewShipWndProc
	);


	static void ShowBuildShipWindow(TileIndex tile)
	{
		Window w;

		int wn = tile == null ? -1 : tile.getTile();
		
		Window.DeleteWindowById(Window.WC_BUILD_VEHICLE, wn );

		w = Window.AllocateWindowDesc(_new_ship_desc);
		w.window_number = wn;
		w.vscroll.setCap(4);
		w.widget.get(2).unkA = (w.vscroll.getCap() << 8) + 1;

		w.resize.step_height = 24;

		if (tile != null) {
			w.caption_color = 0xFF & tile.GetTileOwner().id;
		} else {
			w.caption_color = 0xFF & Global.gs._local_player.id;
		}

	}


	static void ShipViewWndProc(Window w, WindowEvent e) {
		switch(e.event) {
		case WE_PAINT: {
			Vehicle v = Vehicle.GetVehicle(w.window_number);
			int disabled = 1<<8;
			//StringID 
			int str;

			// Possible to refit?
			if (Engine.ShipVehInfo(v.getEngine_type().id).refittable != 0 &&
					v.isStopped() &&
					v.ship.isInDepot() &&
					Depot.IsTileDepotType(v.getTile(), TransportType.Water))
				disabled = 0;

			if (!v.getOwner().isLocalPlayer())
				disabled |= 1<<8 | 1<<7;
			w.disabled_state = disabled;

			/* draw widgets & caption */
			Global.SetDParam(0, v.getString_id());
			Global.SetDParam(1, v.getUnitnumber().id);
			w.DrawWindowWidgets();

			if (v.isBroken()) {
				str = Str.STR_885C_BROKEN_DOWN;
			} else if(v.isStopped()) {
				str = Str.STR_8861_STOPPED;
			} else {
				int vehicle_speed = Global._patches.vehicle_speed ? 1 : 0;
				switch (v.getCurrent_order().getType()) {
				case Order.OT_GOTO_STATION: {
					Global.SetDParam(0, v.getCurrent_order().getStation());
					Global.SetDParam(1, v.getCur_speed() * 10 >> 5);
					str = Str.STR_HEADING_FOR_STATION + vehicle_speed;
				} break;

				case Order.OT_GOTO_DEPOT: {
					Depot depot = Depot.GetDepot(v.getCurrent_order().getStation());
					Global.SetDParam(0, depot.getTownIndex());
					Global.SetDParam(1, v.getCur_speed() * 10 >> 5);
					str = Str.STR_HEADING_FOR_SHIP_DEPOT + vehicle_speed;
				} break;

				case Order.OT_LOADING:
				case Order.OT_LEAVESTATION:
					str = Str.STR_882F_LOADING_UNLOADING;
					break;

				default:
					if (v.getNum_orders() == 0) {
						str = Str.STR_NO_ORDERS + vehicle_speed;
						Global.SetDParam(0, v.getCur_speed() * 10 >> 5);
					} else
						str = Str.STR_EMPTY;
					break;
				}
			}

			/* draw the flag plus orders */
			Gfx.DrawSprite(v.isStopped() ? Sprite.SPR_FLAG_VEH_STOPPED : Sprite.SPR_FLAG_VEH_RUNNING, 2, w.widget.get(5).top + 1);
			Gfx.DrawStringCenteredTruncated(w.widget.get(5).left + 8, w.widget.get(5).right, w.widget.get(5).top + 1, new StringID(str), 0);
			ViewPort.DrawWindowViewport(w);
		} break;

		case WE_CLICK: {
			final Vehicle  v = Vehicle.GetVehicle(w.window_number);

			switch (e.widget) {
			case 5: /* start stop */
				Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_START_STOP_SHIP | Cmd.CMD_MSG(Str.STR_9818_CAN_T_STOP_START_SHIP));
				break;
			case 6: /* center main view */
				ViewPort.ScrollMainWindowTo(v.getX_pos(), v.getY_pos());
				break;
			case 7: /* goto hangar */
				Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_SEND_SHIP_TO_DEPOT | Cmd.CMD_MSG(Str.STR_9819_CAN_T_SEND_SHIP_TO_DEPOT));
				break;
			case 8: /* refit */
				ShowShipRefitWindow(v);
				break;
			case 9: /* show orders */
				OrderGui.ShowOrdersWindow(v);
				break;
			case 10: /* show details */
				ShowShipDetailsWindow(v);
				break;
			case 11: {
				/* clone vehicle */
				Cmd.DoCommandP(v.getTile(), v.index, Global._ctrl_pressed ? 1 : 0, ShipGui::CcCloneShip, Cmd.CMD_CLONE_VEHICLE | Cmd.CMD_MSG(Str.STR_980D_CAN_T_BUILD_SHIP));
			} break;
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
			Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, w.window_number);
			Window.DeleteWindowById(Window.WC_VEHICLE_REFIT, w.window_number);
			Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, w.window_number);
			break;

		case WE_MOUSELOOP:
		{
			Vehicle v;
			int h;
			v = Vehicle.GetVehicle(w.window_number);
			h = Depot.IsTileDepotType(v.getTile(), TransportType.Water) && v.isHidden() ? (1<< 7) : (1 << 11);
			if (h != w.hidden_state) {
				w.hidden_state = h;
				w.SetWindowDirty();
			}
		}
		break;
		
		default:
			break;
		}
	}

	static final Widget _ship_view_widgets[] = {
			new Widget( Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,  14,   0,  10,   0,  13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW),
			new Widget( Window.WWT_CAPTION,    Window.RESIZE_RIGHT, 14,  11, 237,   0,  13, Str.STR_980F, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_STICKYBOX,  Window.RESIZE_LR,    14, 238, 249,   0,  13, 0x0,      Str.STR_STICKY_BUTTON),
			new Widget( Window.WWT_IMGBTN,     Window.RESIZE_RB,    14,   0, 231,  14, 103, 0x0,      Str.STR_NULL),
			new Widget( Window.WWT_6,          Window.RESIZE_RB,    14,   2, 229,  16, 101, 0x0,      Str.STR_NULL),
			new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_RTB,   14,   0, 237, 104, 115, 0x0,      Str.STR_9827_CURRENT_SHIP_ACTION_CLICK),
			new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  14,  31, 0x2AB,    Str.STR_9829_CENTER_MAIN_VIEW_ON_SHIP),
			new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  32,  49, 0x2B0,    Str.STR_982A_SEND_SHIP_TO_DEPOT),
			new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  50,  67, 0x2B4,    Str.STR_983A_REFIT_CARGO_SHIP_TO_CARRY),
			new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  68,  85, 0x2B2,    Str.STR_9828_SHOW_SHIP_S_ORDERS),
			new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  86, 103, 0x2B3,    Str.STR_982B_SHOW_SHIP_DETAILS),
			new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  32,  49, Sprite.SPR_CLONE_SHIP,      Str.STR_CLONE_SHIP_INFO),
			new Widget( Window.WWT_PANEL,      Window.RESIZE_LRB,   14, 232, 249, 104, 103, 0x0,      Str.STR_NULL ),
			new Widget( Window.WWT_RESIZEBOX,  Window.RESIZE_LRTB,  14, 238, 249, 104, 115, 0x0,      Str.STR_NULL ),
	};

	static final WindowDesc _ship_view_desc = new WindowDesc(
			-1,-1, 250, 116,
			Window.WC_VEHICLE_VIEW,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_ship_view_widgets,
			ShipGui::ShipViewWndProc
	);

	static void ShowShipViewWindow(final Vehicle  v)
	{
		Window  w = Window.AllocateWindowDescFront(_ship_view_desc, v.index);

		if (w != null) {
			w.caption_color = 0xFF & v.getOwner().id;
			ViewPort.AssignWindowViewport(w, 3, 17, 0xE2, 0x54, w.window_number | (1 << 31), 0);
		}
	}

	static void DrawShipDepotWindow(Window w)
	{
		TileIndex tile;
		Depot depot;

		tile = TileIndex.get( w.window_number );

		/* setup disabled buttons */
		w.disabled_state =
				tile.IsTileOwner(Global.gs._local_player) ? 0 : ((1 << 4) | (1 << 7));

		/* determine amount of items for scroller */
		int [] num = {0};

		Vehicle.forEach( (v) ->
		{
			if(v.getType() == Vehicle.VEH_Ship && v.ship.isInDepot() && v.getTile().equals(tile))
				num[0]++;
		});
		
		w.SetVScrollCount( (num[0] + w.hscroll.getCap() - 1) / w.hscroll.getCap());

		/* locate the depot struct */
		depot = Depot.GetDepotByTile(tile);
		assert(depot != null);

		Global.SetDParam(0, depot.getTownIndex());
		w.DrawWindowWidgets();

		int [] x = {2};
		int [] y = {15};
		num[0] = w.vscroll.pos * w.hscroll.getCap();

		Vehicle.forEach( (v) ->
		{
			if (v.getType() == Vehicle.VEH_Ship && v.ship.isInDepot() && v.getTile().equals(tile) &&
					--num[0] < 0 && num[0] >= -w.vscroll.getCap() * w.hscroll.getCap()) {
				DrawShipImage(v, x[0]+19, y[0], w.as_traindepot_d().sel);

				Global.SetDParam(0, v.getUnitnumber().id);
				Gfx.DrawString(x[0], y[0]+2, (v.getMax_age()-366) >= v.getAge() ? Str.STR_00E2 : Str.STR_00E3, 0);

				Gfx.DrawSprite(v.isStopped() ? Sprite.SPR_FLAG_VEH_STOPPED : Sprite.SPR_FLAG_VEH_RUNNING, x[0], y[0] + 9);

				if ((x[0] += 90) == 2 + 90 * w.hscroll.getCap()) {
					x[0] = 2;
					y[0] += 24;
				}
			}
		});
	}

	static int GetVehicleFromShipDepotWndPt(final Window w, int x, int y, Vehicle [] veh)
	{
		int xt,row,xm,ym;
		//Vehicle v;
		int pos;

		xt = x / 90;
		xm = x % 90;
		if (xt >= w.hscroll.getCap())
			return 1;

		row = (y - 14) / 24;
		ym = (y - 14) % 24;
		if (row >= w.vscroll.getCap())
			return 1;

		pos = (row + w.vscroll.pos) * w.hscroll.getCap() + xt;

		TileIndex tile = TileIndex.get( w.window_number );

		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();
			if (v.getType() == Vehicle.VEH_Ship && v.isHidden() && v.getTile().equals(tile) &&
					--pos < 0) {
				veh[0] = v;
				if (xm >= 19) return 0;
				if (ym <= 10) return -1; /* show window */
				return -2; /* start stop */
			}
		}

		return 1; /* outside */

	}

	static void ShipDepotClick(Window w, int x, int y)
	{
		Vehicle [] v = {null};
		
		int mode = GetVehicleFromShipDepotWndPt(w, x, y, v);

		// share / copy orders
		if (ViewPort._thd.place_mode != 0 && mode <= 0) { Global._place_clicked_vehicle = v[0]; return; }

		switch (mode) {
		case 1: // invalid
			return;

		case 0: // start dragging of vehicle
			if (v != null) {
				w.as_traindepot_d().sel = v[0].index;
				w.SetWindowDirty();
				ViewPort.SetObjectToPlaceWnd( Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(v[0].getOwner())) +
						Ship.GetShipImage(v[0], 6), 4, w);
			}
			break;

		case -1: // show info window
			ShowShipViewWindow(v[0]);
			break;

		case -2: // click start/stop flag
			Cmd.DoCommandP(v[0].getTile(), v[0].index, 0, null, Cmd.CMD_START_STOP_SHIP | Cmd.CMD_MSG(Str.STR_9818_CAN_T_STOP_START_SHIP));
			break;

		default:
			///NOT_REACHED();
			assert false;
		}
	}

	/**
	 * Clones a ship
	 * @param v is the original vehicle to clone
	 * @param w is the window of the depot where the clone is build
	 */
	static void HandleCloneVehClick(final Vehicle  v, final Window  w)
	{
		if (v == null || v.getType() != Vehicle.VEH_Ship) return;

		Cmd.DoCommandP( TileIndex.get( w.window_number ), v.index, Global._ctrl_pressed ? 1 : 0, ShipGui::CcCloneShip,
				Cmd.CMD_CLONE_VEHICLE | Cmd.CMD_MSG(Str.STR_980D_CAN_T_BUILD_SHIP)
				);

		ViewPort.ResetObjectToPlace();
	}

	static void ClonePlaceObj(TileIndex tile, final Window  w)
	{
		final Vehicle  v = ViewPort.CheckMouseOverVehicle();

		if (v != null) HandleCloneVehClick(v, w);
	}

	static void ShipDepotWndProc(Window  w, WindowEvent  e)
	{
		switch (e.event) {
		case WE_PAINT:
			DrawShipDepotWindow(w);
			break;

		case WE_CLICK:
			switch(e.widget) {
			case 5:
				ShipDepotClick(w, e.pt.x, e.pt.y);
				break;

			case 7:
				ViewPort.ResetObjectToPlace();
				ShowBuildShipWindow( TileIndex.get(  w.window_number ) );
				break;

			case 8: /* clone button */
				w.InvalidateWidget( 8);
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
			ClonePlaceObj( TileIndex.get(  w.window_number ), w);
		} break;

		case WE_ABORT_PLACE_OBJ: {
			w.click_state = BitOps.RETCLRBIT(w.click_state, 8);
			w.InvalidateWidget( 8);
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
				Vehicle[] v = {null};
				//VehicleID 
				int sel = w.as_traindepot_d().sel;

				w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;
				w.SetWindowDirty();

				if (GetVehicleFromShipDepotWndPt(w, e.pt.x, e.pt.y, v) == 0 &&
						v != null &&
						sel == v[0].index) {
					ShowShipViewWindow(v[0]);
				}
			} break;

			case 4:
				if (!BitOps.HASBIT(w.disabled_state, 4) &&
						w.as_traindepot_d().sel != Vehicle.INVALID_VEHICLE)	{
					Vehicle v;

					w.HandleButtonClick( 4);

					v = Vehicle.GetVehicle(w.as_traindepot_d().sel);
					w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;

					Global._backup_orders_tile = v.getTile();
					Vehicle.BackupVehicleOrders(v, Global._backup_orders_data[0]);

					if (!Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_SELL_SHIP | Cmd.CMD_MSG(Str.STR_980C_CAN_T_SELL_SHIP)))
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
			w.hscroll.setCap(w.hscroll.getCap() + e.diff.x / 90);
			w.widget.get(5).unkA = (w.vscroll.getCap() << 8) + w.hscroll.getCap();
			break;
		default:
			break;
		}
	}

	static final Widget _ship_depot_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   292,     0,    13, Str.STR_9803_SHIP_DEPOT,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   293,   304,     0,    13, 0x0,										Str.STR_STICKY_BUTTON),
			new Widget(      Window.WWT_PANEL,    Window.RESIZE_LRB,    14,   270,   292,    14,    13, 0x0,										Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    14,   270,   292,    14,    61, 0x2A9,									Str.STR_9821_DRAG_SHIP_TO_HERE_TO_SELL),

			new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   269,    14,    61, 0x203,									Str.STR_981F_SHIPS_CLICK_ON_SHIP_FOR),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   293,   304,    14,    61, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,    96,    62,    73, Str.STR_9804_NEW_SHIPS,			Str.STR_9820_BUILD_NEW_SHIP),
			new Widget(Window.WWT_NODISTXTBTN,     Window.RESIZE_TB,    14,    97,   194,    62,    73, Str.STR_CLONE_SHIP,		Str.STR_CLONE_SHIP_DEPOT_INFO),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   195,   292,    62,    73, Str.STR_00E4_LOCATION,			Str.STR_9822_CENTER_MAIN_VIEW_ON_SHIP),
			new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,   293,   292,    62,    73, 0x0,													Str.STR_NULL),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   293,   304,    62,    73, 0x0,										Str.STR_RESIZE_BUTTON),
	};

	static final WindowDesc _ship_depot_desc = new WindowDesc(
			-1, -1, 305, 74,
			Window.WC_VEHICLE_DEPOT,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_ship_depot_widgets,
			ShipGui::ShipDepotWndProc
	);

	public static void ShowShipDepotWindow(TileIndex tile)
	{
		Window  w = Window.AllocateWindowDescFront(_ship_depot_desc,tile.getTile());

		if (w != null) {
			w.caption_color = 0xFF & TileIndex.get(w.window_number).GetTileOwner().id;
			w.vscroll.setCap(2);
			w.hscroll.setCap(3);
			w.resize.step_width = 90;
			w.resize.step_height = 24;
			w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;
			Global._backup_orders_tile = null;
		}
	}


	static void DrawSmallOrderList(final Vehicle  v, int x, int y)
	{
		//final Order order;
		int sel, i = 0;

		sel = v.getCur_order_index();

		//FOR_VEHICLE_ORDERS(v, order) 
		//v.forEachOrder( (order) ->
		Iterator<Order> ii = v.getOrdersIterator();
		while(ii.hasNext())
		{
			Order order = ii.next();
			if (sel == 0) {
				Gfx._stringwidth_base = 0xE0;
				//Gfx.DoDrawString("\xAF", x - 6, y, 16);
				Gfx.DoDrawString( String.valueOf((char)0xAF), x - 6, y, 16);
				Gfx._stringwidth_base = 0;
			}
			sel--;

			if (order.getType() == Order.OT_GOTO_STATION) {
				if (!Station.GetStation(order.getStation()).IsBuoy()){
					Global.SetDParam(0, order.getStation());
					Gfx.DrawString(x, y, Str.STR_A036, 0);

					y += 6;
					if (++i == 4) break;
				}
			}
		}
		
		
	}


	static final Widget _player_ships_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   247,     0,    13, Str.STR_9805_SHIPS,				Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   248,   259,     0,    13, 0x0,                   Str.STR_STICKY_BUTTON),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    80,    14,    25, Str.SRT_SORT_BY,						Str.STR_SORT_ORDER_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    81,   235,    14,    25, 0x0,										Str.STR_SORT_CRITERIA_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   236,   247,    14,    25, Str.STR_0225,							Str.STR_SORT_CRITERIA_TIP),
			new Widget(      Window.WWT_PANEL,  Window.RESIZE_RIGHT,    14,   248,   259,    14,    25, 0x0,										Str.STR_NULL),
			new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   247,    26,   169, 0x401,									Str.STR_9823_SHIPS_CLICK_ON_SHIP_FOR),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   248,   259,    26,   169, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   124,   170,   181, Str.STR_9804_NEW_SHIPS,		Str.STR_9824_BUILD_NEW_SHIPS_REQUIRES),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   125,   247,   170,   181, Str.STR_REPLACE_VEHICLES,					Str.STR_REPLACE_HELP),
			new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,   248,   247,   170,   181, 0x0,											Str.STR_NULL),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   248,   259,   170,   181, 0x0,											Str.STR_RESIZE_BUTTON),
	};

	static final Widget _other_player_ships_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   247,     0,    13, Str.STR_9805_SHIPS,				Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   248,   259,     0,    13, 0x0,                   Str.STR_STICKY_BUTTON),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    80,    14,    25, Str.SRT_SORT_BY,						Str.STR_SORT_ORDER_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    81,   235,    14,    25, 0x0,										Str.STR_SORT_CRITERIA_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   236,   247,    14,    25, Str.STR_0225,							Str.STR_SORT_CRITERIA_TIP),
			new Widget(      Window.WWT_PANEL,  Window.RESIZE_RIGHT,    14,   248,   259,    14,    25, 0x0,										Str.STR_NULL),
			new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   247,    26,   169, 0x401,									Str.STR_9823_SHIPS_CLICK_ON_SHIP_FOR),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   248,   259,    26,   169, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,     0,   247,   170,   181, 0x0,											Str.STR_NULL),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   248,   259,   170,   181, 0x0,											Str.STR_RESIZE_BUTTON),
	};

	static void PlayerShipsWndProc(Window w, WindowEvent e)
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

			VehicleGui.BuildVehicleList(vl, Vehicle.VEH_Ship, owner, station);
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
					w.widget.get(1).unkA = Str.STR_9805_SHIPS;
				} else {
					/* Station Name -- (###) Trains */
					Global.SetDParam(0, station);
					Global.SetDParam(1, w.vscroll.getCount());
					w.widget.get(1).unkA = Str.STR_SCHEDULED_SHIPS;
				}
				w.DrawWindowWidgets();
			}
			/* draw sorting criteria string */
			Gfx.DrawString(85, 15, VehicleGui._vehicle_sort_listing[vl.sort_type], 0x10);
			/* draw arrow pointing up/down for ascending/descending sorting */
			Gfx.DoDrawString( (vl.flags & Vehicle.VL_DESC) != 0 ? Gfx.DOWNARROW : Gfx.UPARROW, 69, 15, 0x10);

			max = Math.min(w.vscroll.pos + w.vscroll.getCap(), vl.list_length);
			for (i = w.vscroll.pos; i < max; ++i) {
				Vehicle v = Vehicle.GetVehicle(vl.sort_list[i].index);
				//StringID 
				int str;

				assert(v.getType() == Vehicle.VEH_Ship);

				DrawShipImage(v, x + 19, y + 6, Vehicle.INVALID_VEHICLE);
				VehicleGui.DrawVehicleProfitButton(v, x, y + 13);

				Global.SetDParam(0, v.getUnitnumber().id);
				
				if (Depot.IsTileDepotType(v.getTile(), TransportType.Water) && v.isHidden())
					str = Str.STR_021F;
				else
					str = v.getAge() > v.getMax_age() - 366 ? Str.STR_00E3 : Str.STR_00E2;
					
				Gfx.DrawString(x, y + 2, str, 0);

				Global.SetDParam(0, v.getProfit_this_year());
				Global.SetDParam(1, v.getProfit_last_year());
				Gfx.DrawString(x + 12, y + 28, Str.STR_0198_PROFIT_THIS_YEAR_LAST_YEAR, 0);

				if (v.getString_id() != Str.STR_SV_SHIP_NAME) {
					Global.SetDParam(0, v.getString_id());
					Gfx.DrawString(x + 12, y, Str.STR_01AB, 0);
				}

				DrawSmallOrderList(v, x + 138, y);

				y += VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_BIG;
			}
		}	break;

		case WE_CLICK: {
			switch(e.widget) {
			case 3: /* Flip sorting method ascending/descending */
				vl.flags ^= Vehicle.VL_DESC;
				vl.flags |= Vehicle.VL_RESORT;
				VehicleGui._sorting.ship.order = 0 !=(vl.flags & Vehicle.VL_DESC);
				w.SetWindowDirty();
				break;
			case 4: case 5:/* Select sorting criteria dropdown menu */
				Window.ShowDropDownMenu(w, VehicleGui._vehicle_sort_listing, vl.sort_type, 5, 0, 0);
				return;
			case 7: { /* Matrix to show vehicles */
				int id_v = (e.pt.y - VehicleGui.PLY_WND_PRC__OFFSET_TOP_WIDGET) / VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_BIG;

				if (id_v >= w.vscroll.getCap()) return; // click out of bounds

				id_v += w.vscroll.pos;

				{
					Vehicle v;

					if (id_v >= vl.list_length) return; // click out of list bound

					v	= Vehicle.GetVehicle(vl.sort_list[id_v].index);

					assert(v.getType() == Vehicle.VEH_Ship);

					ShowShipViewWindow(v);
				}
			} break;

			case 9: { /* Build new Vehicle */
			TileIndex tile;

			if (!Window.IsWindowOfPrototype(w, _player_ships_widgets)) break;

			tile = Depot._last_built_ship_depot_tile;
			if( tile == null)
			{
				//ShowBuildShipWindow(null);
				// TODO XXX [dz] show error no depot
				break;
			}
			
			do {
				if (Depot.IsTileDepotType(tile, TransportType.Water) && tile.IsTileOwner(Global.gs._local_player)) 
				{
					ShowShipDepotWindow(tile);
					ShowBuildShipWindow(tile);
					return;
				}

				tile = tile.iadd(1);
				tile.TILE_MASK();
			} while(!tile.equals(Depot._last_built_ship_depot_tile));

			ShowBuildShipWindow(null);
		} break;

		case 10: {
			if (!Window.IsWindowOfPrototype(w, _player_ships_widgets)) break;

			VehicleGui.ShowReplaceVehicleWindow(Vehicle.VEH_Ship);
			break;
		}
	}
	}	break;

	case WE_DROPDOWN_SELECT: /* we have selected a dropdown item in the list */
				if (vl.sort_type != e.index) {
					// value has changed . resort
					vl.flags |= Vehicle.VL_RESORT;
					vl.sort_type =  e.index;
					VehicleGui._sorting.ship.criteria = vl.sort_type;

					// enable 'Sort By' if a sorter criteria is chosen
					if (vl.sort_type != VehicleGui.SORT_BY_UNSORTED)
						w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 3);
				}
				w.SetWindowDirty();
				break;

			case WE_CREATE: /* set up resort timer */
				vl.sort_list = null;
				vl.flags = Vehicle.VL_REBUILD | ( (VehicleGui._sorting.ship.order ? 1 : 0) << (Vehicle.VL_DESC - 1));
				vl.sort_type =  VehicleGui._sorting.ship.criteria;
				vl.resort_timer = Global.DAY_TICKS * VehicleGui.PERIODIC_RESORT_DAYS;
				break;

			case WE_DESTROY:
				//free(vl.sort_list);
				break;

			case WE_TICK: /* resort the list every 20 seconds orso (10 days) */
				if (--vl.resort_timer == 0) {
					Global.DEBUG_misc( 1, "Periodic resort ships list player %d station %d",
							owner, station);
					vl.resort_timer = Global.DAY_TICKS * VehicleGui.PERIODIC_RESORT_DAYS;
					vl.flags |= Vehicle.VL_RESORT;
					w.SetWindowDirty();
				}
				break;

			case WE_RESIZE:
				/* Update the scroll + matrix */
				w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_BIG);
				w.widget.get(7).unkA = (w.vscroll.getCap() << 8) + 1;
				break;
		default:
			break;
			}
			}

			static final WindowDesc _player_ships_desc = new WindowDesc(
					-1, -1, 260, 182,
					Window.WC_SHIPS_LIST,0,
					WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
					_player_ships_widgets,
					ShipGui::PlayerShipsWndProc
			);

			static final WindowDesc _other_player_ships_desc = new WindowDesc(
					-1, -1, 260, 182,
					Window.WC_SHIPS_LIST,0,
					WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
					_other_player_ships_widgets,
					ShipGui::PlayerShipsWndProc
			);

			static void ShowPlayerShips(PlayerID player, StationID station)
			{
				ShowPlayerShips( player.id, station.id);
			}

			static void ShowPlayerShips(int player, int station)
			{
				Window w;

				if (player == Global.gs._local_player.id) {
					w = Window.AllocateWindowDescFront(_player_ships_desc, (station<< 16) | player);
				} else  {
					w = Window.AllocateWindowDescFront(_other_player_ships_desc, (station<< 16) | player);
				}
				if (w != null) {
					w.caption_color = 0xff & w.window_number;
					w.vscroll.setCap(4);
					w.widget.get(7).unkA = (w.vscroll.getCap() << 8) + 1;
					w.resize.step_height = VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_BIG;
				}
			}


		}
