package com.dzavalishin.xui;

import java.util.Iterator;

import com.dzavalishin.enums.TransportType;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Depot;
import com.dzavalishin.game.Engine;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.RoadVehCmd;
import com.dzavalishin.game.RoadVehicleInfo;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.YearMonthDay;
import com.dzavalishin.wcustom.vehiclelist_d;

public class RoadVehGui 
{

	/**
	 * Draw the purchase info details of road vehicle at a given location.
	 * @param x,y location where to draw the info
	 * @param engine_number the engine of which to draw the info of
	 */
	static void DrawRoadVehPurchaseInfo(int x, int y, /*EngineID*/ int engine_number)
	{
		final RoadVehicleInfo rvi = Engine.RoadVehInfo(engine_number);
		final Engine  e = Engine.GetEngine(engine_number);
		YearMonthDay ymd = new YearMonthDay(e.getIntro_date());
		//YearMonthDay.ConvertDayToYMD(ymd, e.getIntro_date());

		/* Purchase cost - Max speed */
		Global.SetDParam(0, rvi.base_cost * (((int)Global._price.roadveh_base)>>3)>>5);
		Global.SetDParam(1, rvi.max_speed * 10 >> 5);
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_COST_SPEED, 0);
		y += 10;

		/* Running cost */
		Global.SetDParam(0, rvi.running_cost * ((int)Global._price.roadveh_running) >> 8);
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_RUNNINGCOST, 0);
		y += 10;

		/* Cargo type + capacity */
		Global.SetDParam(0, Global._cargoc.names_long[rvi.cargo_type]);
		Global.SetDParam(1, rvi.capacity);
		Global.SetDParam(2, Str.STR_EMPTY);
		Gfx.DrawString(x, y, Str.STR_PURCHASE_INFO_CAPACITY, 0);
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

	static void DrawRoadVehImage(final Vehicle v, int x, int y, /*VehicleID*/ int selection)
	{
		int image = RoadVehCmd.GetRoadVehImage(v, 6);
		int ormod = Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(v.getOwner()));
		if(v.isCrashed()) ormod = Sprite.PALETTE_CRASH;
		Gfx.DrawSprite(image | ormod, x + 14, y + 6);

		if (v.index == selection) {
			Gfx.DrawFrameRect(x - 1, y - 1, x + 28, y + 12, 15, Window.FR_BORDERONLY);
		}
	}

	static void RoadVehDetailsWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			final Vehicle v = Vehicle.GetVehicle(w.window_number);
			//StringID 
			int str;

			w.disabled_state = v.getOwner().isLocalPlayer() ? 0 : (1 << 2);
			if (0==Global._patches.servint_roadveh) // disable service-scroller when interval is set to disabled
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
				Global.SetDParam(3, ((int)(Engine.RoadVehInfo(v.getEngine_type().id).running_cost * Global._price.roadveh_running)) >> 8);
				Gfx.DrawString(2, 15, Str.STR_900D_AGE_RUNNING_COST_YR, 0);
			}

			/* Draw max speed */
			{
				Global.SetDParam(0, v.getMax_speed() * 10 >> 5);
				Gfx.DrawString(2, 25, Str.STR_900E_MAX_SPEED, 0);
			}

			/* Draw profit */
			{
				Global.SetDParam(0, v.getProfit_this_year());
				Global.SetDParam(1, v.getProfit_last_year());
				Gfx.DrawString(2, 35, Str.STR_900F_PROFIT_THIS_YEAR_LAST_YEAR, 0);
			}

			/* Draw breakdown & reliability */
			{
				Global.SetDParam(0, v.getReliability() * 100 >> 16);
				Global.SetDParam(1, v.getBreakdowns_since_last_service());
				Gfx.DrawString(2, 45, Str.STR_9010_RELIABILITY_BREAKDOWNS, 0);
			}

			/* Draw service interval text */
			{
				Global.SetDParam(0, v.getService_interval());
				Global.SetDParam(1, v.date_of_last_service);
				Gfx.DrawString(13, 90, Global._patches.servint_ispercent?Str.STR_SERVICING_INTERVAL_PERCENT:Str.STR_883C_SERVICING_INTERVAL_DAYS, 0);
			}

			DrawRoadVehImage(v, 3, 57, Vehicle.INVALID_VEHICLE);

			Global.SetDParam(0, Engine.GetCustomEngineName(v.getEngine_type().id).id);
			Global.SetDParam(1, 1920 + v.getBuild_year());
			Global.SetDParam(2, v.getValue());
			Gfx.DrawString(34, 57, Str.STR_9011_BUILT_VALUE, 0);

			Global.SetDParam(0, Global._cargoc.names_long[v.getCargo_type()]);
			Global.SetDParam(1, v.getCargo_cap());
			Gfx.DrawString(34, 67, Str.STR_9012_CAPACITY, 0);

			str = Str.STR_8812_EMPTY;
			if (v.getCargo_count() != 0) {
				Global.SetDParam(0, v.getCargo_type());
				Global.SetDParam(1, v.getCargo_count());
				Global.SetDParam(2, v.getCargo_source());
				str = Str.STR_8813_FROM;
			}
			Gfx.DrawString(34, 78, str, 0);
		} break;

		case WE_CLICK: {
			int mod;
			final Vehicle v;
			switch (e.widget) {
			case 2: /* rename */
				v = Vehicle.GetVehicle(w.window_number);
				Global.SetDParam(0, v.getUnitnumber().id);
				MiscGui.ShowQueryString( new StringID(v.getString_id()), new StringID(Str.STR_902C_NAME_ROAD_VEHICLE), 31, 150, w.getWindow_class(), w.window_number);
				break;

			case 5: /* increase int */
			case 6: /* decrease int */
				if(e.widget == 6)
					mod = Global._ctrl_pressed? -5 : -10;
				else // 5
					mod = Global._ctrl_pressed? 5 : 10;
				
				v = Vehicle.GetVehicle(w.window_number);

				mod = Depot.GetServiceIntervalClamped(mod + v.getService_interval());
				if (mod == v.getService_interval()) return;

				Cmd.DoCommandP(v.getTile(), v.index, mod, null, Cmd.CMD_CHANGE_ROADVEH_SERVICE_INT | Cmd.CMD_MSG(Str.STR_018A_CAN_T_CHANGE_SERVICING));
				break;
			}
		} break;

		case WE_4:
			if (Window.FindWindowById(Window.WC_VEHICLE_VIEW, w.window_number) == null)
				w.DeleteWindow();
			break;

		case WE_ON_EDIT_TEXT: {
			if (e.str != null) {
				Global._cmd_text = e.str;
				Cmd.DoCommandP(null, w.window_number, 0, null,
					Cmd.CMD_NAME_VEHICLE | Cmd.CMD_MSG(Str.STR_902D_CAN_T_NAME_ROAD_VEHICLE));
			}
		} break;
		default:
			break;

		}
	}

	static final Widget _roadveh_details_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,				Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   339,     0,    13, Str.STR_900C_DETAILS,Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   340,   379,     0,    13, Str.STR_01AA_NAME,		Str.STR_902E_NAME_ROAD_VEHICLE),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   379,    14,    55, 0x0,							Str.STR_NULL),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   379,    56,    88, 0x0,							Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    10,    89,    94, Str.STR_0188,				Str.STR_884D_INCREASE_SERVICING_INTERVAL),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    10,    95,   100, Str.STR_0189,				Str.STR_884E_DECREASE_SERVICING_INTERVAL),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    11,   379,    89,   100, 0x0,							Str.STR_NULL),
	};

	static final WindowDesc _roadveh_details_desc = new WindowDesc(
		-1,-1, 380, 101,
		Window.WC_VEHICLE_DETAILS,Window.WC_VEHICLE_VIEW,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_roadveh_details_widgets,
		RoadVehGui::RoadVehDetailsWndProc
	);

	static void ShowRoadVehDetailsWindow(final Vehicle  v)
	{
		Window w;
		//VehicleID 
		int veh = v.index;

		Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, veh);
		Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, veh);
		//_alloc_wnd_parent_num = veh;
		w = Window.AllocateWindowDesc(_roadveh_details_desc, veh);
		w.window_number = veh;
		w.caption_color = (byte) v.getOwner().id;
	}

	public static void CcCloneRoadVeh(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) ShowRoadVehViewWindow(Vehicle.GetVehicle(Global._new_roadveh_id));
	}

	static void RoadVehViewWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			Vehicle v = Vehicle.GetVehicle(w.window_number);
			//StringID 
			int str;

			w.disabled_state = (!v.getOwner().isLocalPlayer()) ? (1<<8 | 1<<7) : 0;

			/* draw widgets & caption */
			Global.SetDParam(0, v.getString_id());
			Global.SetDParam(1, v.getUnitnumber().id);
			w.DrawWindowWidgets();

			str = v.infoString();

			/* draw the flag plus orders */
			Gfx.DrawSprite( v.isStopped() ? Sprite.SPR_FLAG_VEH_STOPPED : Sprite.SPR_FLAG_VEH_RUNNING, 2, w.widget.get(5).top + 1);
			Gfx.DrawStringCenteredTruncated(w.widget.get(5).left + 8, w.widget.get(5).right, w.widget.get(5).top + 1, new StringID(str), 0);
			w.DrawWindowViewport();
		} break;

		case WE_CLICK: {
			final Vehicle  v = Vehicle.GetVehicle(w.window_number);

			switch (e.widget) {
			case 5: /* start stop */
				Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_START_STOP_ROADVEH | Cmd.CMD_MSG(Str.STR_9015_CAN_T_STOP_START_ROAD_VEHICLE));
				break;
			case 6: /* center main view */
				ViewPort.ScrollMainWindowTo(v.getX_pos(), v.getY_pos());
				break;
			case 7: /* goto hangar */
				Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_SEND_ROADVEH_TO_DEPOT | Cmd.CMD_MSG(Str.STR_9018_CAN_T_SEND_VEHICLE_TO_DEPOT));
				break;
			case 8: /* turn around */
				Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_TURN_ROADVEH | Cmd.CMD_MSG(Str.STR_9033_CAN_T_MAKE_VEHICLE_TURN));
				break;
			case 9: /* show orders */
				OrderGui.ShowOrdersWindow(v);
				break;
			case 10: /* show details */
				ShowRoadVehDetailsWindow(v);
				break;
			case 11: {
				/* clone vehicle */
				Cmd.DoCommandP(v.getTile(), v.index, Global._ctrl_pressed ? 1 : 0, RoadVehGui::CcCloneRoadVeh, Cmd.CMD_CLONE_VEHICLE | Cmd.CMD_MSG(Str.STR_9009_CAN_T_BUILD_ROAD_VEHICLE));
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
			Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, w.window_number);
			break;

		case WE_MOUSELOOP:
			{
				Vehicle v;
				int h;
				v = Vehicle.GetVehicle(w.window_number);
				h = (Depot.IsTileDepotType(v.getTile(), TransportType.Road) && v.isStopped()) ? (1<< 7) : (1 << 11);
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


	static final Widget _roadveh_view_widgets[] = {
	new Widget( Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,  14,   0,  10,   0,  13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW ),
	new Widget( Window.WWT_CAPTION,    Window.RESIZE_RIGHT, 14,  11, 237,   0,  13, Str.STR_9002, Str.STR_018C_WINDOW_TITLE_DRAG_THIS ),
	new Widget( Window.WWT_STICKYBOX,  Window.RESIZE_LR,    14, 238, 249,   0,  13, 0x0,      Str.STR_STICKY_BUTTON ),
	new Widget( Window.WWT_IMGBTN,     Window.RESIZE_RB,    14,   0, 231,  14, 103, 0x0,      Str.STR_NULL ),
	new Widget( Window.WWT_6,          Window.RESIZE_RB,    14,   2, 229,  16, 101, 0x0,      Str.STR_NULL ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_RTB,   14,   0, 237, 104, 115, 0x0,      Str.STR_901C_CURRENT_VEHICLE_ACTION ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  14,  31, 0x2AB,    Str.STR_901E_CENTER_MAIN_VIEW_ON_VEHICLE ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  32,  49, 0x2AE,    Str.STR_901F_SEND_VEHICLE_TO_DEPOT ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  50,  67, 0x2CB,    Str.STR_9020_FORCE_VEHICLE_TO_TURN_AROUND ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  68,  85, 0x2B2,    Str.STR_901D_SHOW_VEHICLE_S_ORDERS ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  86, 103, 0x2B3,    Str.STR_9021_SHOW_ROAD_VEHICLE_DETAILS ),
	new Widget( Window.WWT_PUSHIMGBTN, Window.RESIZE_LR,    14, 232, 249,  32,  49, Sprite.SPR_CLONE_ROADVEH,      Str.STR_CLONE_ROAD_VEHICLE_INFO ),
	new Widget( Window.WWT_PANEL,      Window.RESIZE_LRB,   14, 232, 249, 104, 103, 0x0,      Str.STR_NULL ),
	new Widget( Window.WWT_RESIZEBOX,  Window.RESIZE_LRTB,  14, 238, 249, 104, 115, 0x0,      Str.STR_NULL ),
	};

	static final WindowDesc _roadveh_view_desc = new WindowDesc(
		-1,-1, 250, 116,
		Window.WC_VEHICLE_VIEW,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_roadveh_view_widgets,
		RoadVehGui::RoadVehViewWndProc
	);

	static void ShowRoadVehViewWindow(final Vehicle  v)
	{
		Window  w = Window.AllocateWindowDescFront(_roadveh_view_desc, v.index);

		if (w != null) {
			w.caption_color = (byte) v.getOwner().id;
			ViewPort.AssignWindowViewport(w, 3, 17, 0xE2, 0x54, w.window_number | (1 << 31), 0);
		}
	}


	static void DrawNewRoadVehWindow(Window w)
	{
		if (w.window_number == 0)
			w.disabled_state = 1 << 5;

		// setup scroller
		{
			int count = 0;
			int num = Global.NUM_ROAD_ENGINES;
			int i = 0;
			do {
				final Engine e = Engine.GetEngine(Global.ROAD_ENGINES_INDEX + i++);
				if (e.isAvailableToMe())
					count++;
			} while (--num > 0);
			w.SetVScrollCount( count);
		}

		w.DrawWindowWidgets();

		{
			int num = Global.NUM_ROAD_ENGINES;
			int x = 1;
			int y = 15;
			int sel = w.as_buildtrain_d().sel_index;
			int pos = w.vscroll.pos;
			//EngineID 
			int engine_id = Global.ROAD_ENGINES_INDEX;
			//EngineID 
			int selected_id = Engine.INVALID_ENGINE;
			int ei = 0;
			do {
				final Engine  e = Engine.GetEngine(Global.ROAD_ENGINES_INDEX + ei++ );

				if (e.isAvailableToMe()) {
					if (sel==0) selected_id = engine_id;
					if (BitOps.IS_INT_INSIDE(--pos, -w.vscroll.getCap(), 0)) {
						Gfx.DrawString(x+59, y+2, Engine.GetCustomEngineName(engine_id), sel==0 ? 0xC : 0x10);
						RoadVehCmd.DrawRoadVehEngine(x+29, y+6, engine_id, Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player)));
						y += 14;
					}
					sel--;
				}
				//ei++;
				++engine_id;
			} while (--num > 0);

			w.as_buildtrain_d().sel_engine = selected_id;
			if (selected_id != Engine.INVALID_ENGINE) {
				DrawRoadVehPurchaseInfo(2, w.widget.get(4).top + 1, selected_id);
			}
		}
	}

	public static void CcBuildRoadVeh(boolean success, TileIndex tile, int p1, int p2)
	{
		final Vehicle  v;

		if (!success) return;

		v = Vehicle.GetVehicle(Global._new_roadveh_id);
		if (v.getTile().equals(Global._backup_orders_tile)) {
			Global._backup_orders_tile = null;
			Vehicle.RestoreVehicleOrders(v, Global._backup_orders_data[0]);
		}
		ShowRoadVehViewWindow(v);
	}

	static void NewRoadVehWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT:
			DrawNewRoadVehWindow(w);
			break;

		case WE_CLICK:
			switch(e.widget) {
			case 2: { /* listbox */
				int i = (e.pt.y - 14) / 14;
				if (i < w.vscroll.getCap()) {
					w.as_buildtrain_d().sel_index =  (i + w.vscroll.pos);
					w.SetWindowDirty();
				}
			} break;

			case 5: { /* build */
				//EngineID 
				int sel_eng = w.as_buildtrain_d().sel_engine;
				if (sel_eng != Engine.INVALID_ENGINE)
					Cmd.DoCommandP( TileIndex.get( w.window_number ), sel_eng, 0, RoadVehGui::CcBuildRoadVeh, Cmd.CMD_BUILD_ROAD_VEH | Cmd.CMD_MSG(Str.STR_9009_CAN_T_BUILD_ROAD_VEHICLE));
			} break;

			case 6: { /* rename */
				//EngineID 
				int sel_eng = w.as_buildtrain_d().sel_engine;
				if (sel_eng != Engine.INVALID_ENGINE) {
					w.as_buildtrain_d().rename_engine = sel_eng;
					MiscGui.ShowQueryString(Engine.GetCustomEngineName(sel_eng),
						new StringID( Str.STR_9036_RENAME_ROAD_VEHICLE_TYPE ), 31, 160, w.getWindow_class(), w.window_number);
				}
			}	break;
			}
			break;

		case WE_4:
			if (w.window_number != 0 && null == Window.FindWindowById(Window.WC_VEHICLE_DEPOT, w.window_number)) {
				w.DeleteWindow();
			}
			break;

		case WE_ON_EDIT_TEXT:
			if (e.str != null) {
				Global._cmd_text = e.str;
				Cmd.DoCommandP(null, w.as_buildtrain_d().rename_engine, 0, null,
					Cmd.CMD_RENAME_ENGINE | Cmd.CMD_MSG(Str.STR_9037_CAN_T_RENAME_ROAD_VEHICLE));
			}
			break;

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

	static final Widget _new_road_veh_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   247,     0,    13, Str.STR_9006_NEW_ROAD_VEHICLES, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(     Window.WWT_MATRIX, Window.RESIZE_BOTTOM,    14,     0,   235,    14,   125, 0x801,									Str.STR_9026_ROAD_VEHICLE_SELECTION),
	new Widget(  Window.WWT_SCROLLBAR, Window.RESIZE_BOTTOM,    14,   236,   247,    14,   125, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget(     Window.WWT_IMGBTN,     Window.RESIZE_TB,    14,     0,   247,   126,   177, 0x0,										Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   117,   178,   189, Str.STR_9007_BUILD_VEHICLE,Str.STR_9027_BUILD_THE_HIGHLIGHTED_ROAD),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   118,   235,   178,   189, Str.STR_9034_RENAME,				Str.STR_9035_RENAME_ROAD_VEHICLE_TYPE),
	new Widget(  Window.WWT_RESIZEBOX,     Window.RESIZE_TB,    14,   236,   247,   178,   189, 0x0,										Str.STR_RESIZE_BUTTON),
	};

	static final WindowDesc _new_road_veh_desc = new WindowDesc(
		-1, -1, 248, 190,
		Window.WC_BUILD_VEHICLE,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
		_new_road_veh_widgets,
		RoadVehGui::NewRoadVehWndProc
	);

	static void ShowBuildRoadVehWindow(TileIndex tile)
	{
		final int wn = tile != null ? tile.getTile() : -1;
		Window.DeleteWindowById(Window.WC_BUILD_VEHICLE, wn);

		Window w = Window.AllocateWindowDesc(_new_road_veh_desc);
		w.window_number = wn;
		w.vscroll.setCap(8);
		w.widget.get(2).unkA = (w.vscroll.getCap() << 8) + 1;

		w.resize.step_height = 14;
		w.resize.height = w.height - 14 * 4; /* Minimum of 4 vehicles in the display */

		if (tile != null) {
			w.caption_color = (byte) tile.GetTileOwner().id;
		} else {
			w.caption_color = (byte) Global.gs._local_player.id;
		}
	}

	static void DrawRoadDepotWindow(Window w)
	{
		TileIndex tile;
		//Vehicle v;
		int x,y;
		Depot depot;

		tile = TileIndex.get( w.window_number );

		/* setup disabled buttons */
		w.disabled_state =
			tile.IsTileOwner( Global.gs._local_player) ? 0 : ((1<<4) | (1<<7) | (1<<8));

		/* determine amount of items for scroller */
		int [] num = {0};

		Vehicle.forEach( (v) ->
		{
			if (v.getType() == Vehicle.VEH_Road && v.road.isInDepot() && v.getTile().equals(tile))
				num[0]++;
		});

		w.SetVScrollCount( (num[0] + w.hscroll.getCap() - 1) / w.hscroll.getCap());

		/* locate the depot struct */
		depot = Depot.GetDepotByTile(tile);
		assert(depot != null);

		Global.SetDParam(0, depot.getTownIndex());
		w.DrawWindowWidgets();

		x = 2;
		y = 15;
		num[0] = w.vscroll.pos * w.hscroll.getCap();

		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();
			
			if (v.getType() == Vehicle.VEH_Road && v.road.isInDepot() && v.getTile().equals(tile) &&
					--num[0] < 0 && num[0] >=	-w.vscroll.getCap() * w.hscroll.getCap()) {
				DrawRoadVehImage(v, x+24, y, w.as_traindepot_d().sel);

				Global.SetDParam(0, v.getUnitnumber().id);
				Gfx.DrawString(x, y+2, (v.getMax_age()-366) >= v.getAge() ? Str.STR_00E2 : Str.STR_00E3, 0);

				Gfx.DrawSprite( v.isStopped() ? Sprite.SPR_FLAG_VEH_STOPPED : Sprite.SPR_FLAG_VEH_RUNNING, x + 16, y);

				if ((x+=56) == 2 + 56 * w.hscroll.getCap()) {
					x = 2;
					y += 14;
				}
			}
		}
	}

	static int GetVehicleFromRoadDepotWndPt(final Window w, int x, int y, Vehicle []veh)
	{
		int xt,row,xm;
		TileIndex tile;
		//Vehicle v;
		int pos;

		xt = x / 56;
		xm = x % 56;
		if (xt >= w.hscroll.getCap())
			return 1;

		row = (y - 14) / 14;
		if (row >= w.vscroll.getCap())
			return 1;

		pos = (row + w.vscroll.pos) * w.hscroll.getCap() + xt;

		tile = TileIndex.get( w.window_number );

		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle v = ii.next();
			if (v.getType() == Vehicle.VEH_Road && v.road.isInDepot() && v.getTile().equals(tile) &&
					--pos < 0) {
				veh[0] = v;
				if (xm >= 24) return 0;
				if (xm <= 16) return -1; /* show window */
				return -2; /* start stop */
			}
		}

		return 1; /* outside */
	}

	static void RoadDepotClickVeh(Window w, int x, int y)
	{
		Vehicle [] v = {null};
		int mode;

		mode = GetVehicleFromRoadDepotWndPt(w, x, y, v);
		if (mode > 0) return;

		// share / copy orders
		if (ViewPort._thd.place_mode != 0 && mode <= 0) { Global._place_clicked_vehicle = v[0]; return; }

		switch (mode) {
		case 0: // start dragging of vehicle
			if (v[0] != null) {
				w.as_traindepot_d().sel = v[0].index;
				w.SetWindowDirty();
				ViewPort.SetObjectToPlaceWnd( Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(v[0].getOwner())) + RoadVehCmd.GetRoadVehImage(v[0], 6), 4, w);
			}
			break;

		case -1: // show info window
			ShowRoadVehViewWindow(v[0]);
			break;

		case -2: // click start/stop flag
			Cmd.DoCommandP(v[0].getTile(), v[0].index, 0, null, Cmd.CMD_START_STOP_ROADVEH | Cmd.CMD_MSG(Str.STR_9015_CAN_T_STOP_START_ROAD_VEHICLE));
			break;

		default:
			//NOT_REACHED();
			assert false;
		}
	}

	/**
	 * Clones a road vehicle
	 * @param v is the original vehicle to clone
	 * @param w is the window of the depot where the clone is build
	 */
	static void HandleCloneVehClick(final Vehicle  v, final Window  w)
	{
		if (v == null || v.getType() != Vehicle.VEH_Road) return;

		Cmd.DoCommandP( TileIndex.get( w.window_number ), v.index, Global._ctrl_pressed ? 1 : 0, RoadVehGui::CcCloneRoadVeh,
			Cmd.CMD_CLONE_VEHICLE | Cmd.CMD_MSG(Str.STR_9009_CAN_T_BUILD_ROAD_VEHICLE)
		);

		ViewPort.ResetObjectToPlace();
	}

	static void ClonePlaceObj(TileIndex tile, final Window  w)
	{
		final Vehicle  v = ViewPort.CheckMouseOverVehicle();

		if (v != null) HandleCloneVehClick(v, w);
	}

	static void RoadDepotWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT:
			DrawRoadDepotWindow(w);
			break;

		case WE_CLICK: {
			switch(e.widget) {
			case 5:
				RoadDepotClickVeh(w, e.pt.x, e.pt.y);
				break;

			case 7:
				ViewPort.ResetObjectToPlace();
				ShowBuildRoadVehWindow( TileIndex.get( w.window_number ) );
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
					ViewPort.ScrollMainWindowToTile( TileIndex.get(  w.window_number ) );
						break;
			}
		} break;

		case WE_PLACE_OBJ: {
			ClonePlaceObj(e.tile, w);
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
				Vehicle [] v = {null};
				//VehicleID 
				int sel = w.as_traindepot_d().sel;

				w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;
				w.SetWindowDirty();

				if (GetVehicleFromRoadDepotWndPt(w, e.pt.x, e.pt.y, v) == 0 &&
						v != null &&
						sel == v[0].index) {
					ShowRoadVehViewWindow(v[0]);
				}
			} break;

			case 4:
				if (!BitOps.HASBIT(w.disabled_state, 4) &&
						w.as_traindepot_d().sel != Vehicle.INVALID_VEHICLE)	{
					Vehicle v;

					w.HandleButtonClick(4);

					v = Vehicle.GetVehicle(w.as_traindepot_d().sel);
					w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;

					Global._backup_orders_tile = v.getTile();
					Vehicle.BackupVehicleOrders(v, Global._backup_orders_data[0]);

					if (!Cmd.DoCommandP(v.getTile(), v.index, 0, null, Cmd.CMD_SELL_ROAD_VEH | Cmd.CMD_MSG(Str.STR_9014_CAN_T_SELL_ROAD_VEHICLE)))
						Global._backup_orders_tile = null;
				}
				break;
			default:
				w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;
				w.SetWindowDirty();
			}
			break;

		case WE_RESIZE: {
			/* Update the scroll + matrix */
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / 14);
			w.hscroll.setCap(w.hscroll.getCap() + e.diff.x / 56);
			w.widget.get(5).unkA = (w.vscroll.getCap() << 8) + w.hscroll.getCap();

		} break;
		default:
			break;

		}

	}

	static final Widget _road_depot_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5, 										Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   302,     0,    13, Str.STR_9003_ROAD_VEHICLE_DEPOT,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   303,   314,     0,    13, 0x0,													Str.STR_STICKY_BUTTON),
	new Widget(      Window.WWT_PANEL,    Window.RESIZE_LRB,    14,   280,   302,    14,    13, 0x0,													Str.STR_NULL),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    14,   280,   302,    14,    55, 0x2A9,												Str.STR_9024_DRAG_ROAD_VEHICLE_TO_HERE),

	new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   279,    14,    55, 0x305,												Str.STR_9022_VEHICLES_CLICK_ON_VEHICLE),
	new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   303,   314,    14,    55, 0x0,													Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   100,    56,    67, Str.STR_9004_NEW_VEHICLES,				Str.STR_9023_BUILD_NEW_ROAD_VEHICLE),
	new Widget(Window.WWT_NODISTXTBTN,     Window.RESIZE_TB,    14,   101,   200,    56,    67, Str.STR_CLONE_ROAD_VEHICLE,		Str.STR_CLONE_ROAD_VEHICLE_DEPOT_INFO),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   201,   302,    56,    67, Str.STR_00E4_LOCATION,						Str.STR_9025_CENTER_MAIN_VIEW_ON_ROAD),
	new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,   303,   302,    56,    67, 0x0,													Str.STR_NULL),
	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   303,   314,    56,    67, 0x0,													Str.STR_RESIZE_BUTTON),
	};

	static final WindowDesc _road_depot_desc = new WindowDesc(
		-1, -1, 315, 68,
		Window.WC_VEHICLE_DEPOT,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_road_depot_widgets,
		RoadVehGui::RoadDepotWndProc
	);

	public static void ShowRoadDepotWindow(TileIndex tile)
	{
		Window w;

		w = Window.AllocateWindowDescFront(_road_depot_desc, tile.getTile());
		if (w!=null) {
			w.caption_color = (byte) TileIndex.get(w.window_number).GetTileOwner().id;
			w.hscroll.setCap(5);
			w.vscroll.setCap(3);
			w.resize.step_width = 56;
			w.resize.step_height = 14;
			w.as_traindepot_d().sel = Vehicle.INVALID_VEHICLE;
			Global._backup_orders_tile = null;
		}
	}

	static final Widget _player_roadveh_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   247,     0,    13, Str.STR_9001_ROAD_VEHICLES,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   248,   259,     0,    13, 0x0,                     Str.STR_STICKY_BUTTON),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    80,    14,    25, Str.SRT_SORT_BY,							Str.STR_SORT_ORDER_TIP),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    81,   235,    14,    25, 0x0,											Str.STR_SORT_CRITERIA_TIP),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   236,   247,    14,    25, Str.STR_0225,								Str.STR_SORT_CRITERIA_TIP),
	new Widget(      Window.WWT_PANEL,  Window.RESIZE_RIGHT,    14,   248,   259,    14,    25, 0x0,											Str.STR_NULL),
	new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   247,    26,   207, 0x701,										Str.STR_901A_ROAD_VEHICLES_CLICK_ON),
	new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   248,   259,    26,   207, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	/* only for our road list, a 'Build Vehicle' button that opens the depot of the last built depot */
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   124,   208,   219, Str.STR_8815_NEW_VEHICLES,		Str.STR_901B_BUILD_NEW_ROAD_VEHICLES),
	new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   125,   247,   208,   219, Str.STR_REPLACE_VEHICLES,    Str.STR_REPLACE_HELP),
	new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,   248,   247,   208,   219, 0x0,											Str.STR_NULL),
	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   248,   259,   208,   219, 0x0,											Str.STR_RESIZE_BUTTON),
	};

	static final Widget _other_player_roadveh_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   247,     0,    13, Str.STR_9001_ROAD_VEHICLES,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   248,   259,     0,    13, 0x0,                     Str.STR_STICKY_BUTTON),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    80,    14,    25, Str.SRT_SORT_BY,							Str.STR_SORT_ORDER_TIP),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    81,   235,    14,    25, 0x0,											Str.STR_SORT_CRITERIA_TIP),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   236,   247,    14,    25, Str.STR_0225,								Str.STR_SORT_CRITERIA_TIP),
	new Widget(      Window.WWT_PANEL,  Window.RESIZE_RIGHT,    14,   248,   259,    14,    25, 0x0,											Str.STR_NULL),
	new Widget(     Window.WWT_MATRIX,     Window.RESIZE_RB,    14,     0,   247,    26,   207, 0x701,										Str.STR_901A_ROAD_VEHICLES_CLICK_ON),
	new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   248,   259,    26,   207, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,     0,   247,   208,   219, 0x0,											Str.STR_NULL),
	new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   248,   259,   208,   219, 0x0,											Str.STR_RESIZE_BUTTON),
	};

	static void PlayerRoadVehWndProc(Window w, WindowEvent e)
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

			VehicleGui.BuildVehicleList(vl, Vehicle.VEH_Road, owner, station);
			VehicleGui.SortVehicleList(vl);

			w.SetVScrollCount( vl.list_length);

			// disable 'Sort By' tooltip on Unsorted sorting criteria
			if (vl.sort_type == VehicleGui.SORT_BY_UNSORTED)
				w.disabled_state |= (1 << 3);

			/* draw the widgets */
			{
				final Player p = Player.GetPlayer(owner);
				if (station == Station.INVALID_STATION) {
					/* Company Name -- (###) Road vehicles */
					Global.SetDParam(0, p.getName_1());
					Global.SetDParam(1, p.getName_2());
					Global.SetDParam(2, w.vscroll.getCount());
					w.widget.get(1).unkA = Str.STR_9001_ROAD_VEHICLES;
				} else {
					/* Station Name -- (###) Road vehicles */
					Global.SetDParam(0, station);
					Global.SetDParam(1, w.vscroll.getCount());
					w.widget.get(1).unkA = Str.STR_SCHEDULED_ROAD_VEHICLES;
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

				assert(v.getType() == Vehicle.VEH_Road && v.getOwner().id == owner);

				DrawRoadVehImage(v, x + 22, y + 6, Vehicle.INVALID_VEHICLE);
				VehicleGui.DrawVehicleProfitButton(v, x, y + 13);

				Global.SetDParam(0, v.getUnitnumber().id);
				if (Depot.IsTileDepotType(v.getTile(), TransportType.Road) && v.isHidden())
					str = Str.STR_021F;
				else
					str = v.getAge() > v.getMax_age() - 366 ? Str.STR_00E3 : Str.STR_00E2;
				Gfx.DrawString(x, y + 2, str, 0);

				Global.SetDParam(0, v.getProfit_this_year());
				Global.SetDParam(1, v.getProfit_last_year());
				Gfx.DrawString(x + 24, y + 18, Str.STR_0198_PROFIT_THIS_YEAR_LAST_YEAR, 0);

				if (v.getString_id() != Str.STR_SV_ROADVEH_NAME) {
					Global.SetDParam(0, v.getString_id());
					Gfx.DrawString(x + 24, y, Str.STR_01AB, 0);
				}

				y += VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_SMALL;
			}
			}	break;

		case WE_CLICK: {
			switch(e.widget) {
			case 3: /* Flip sorting method ascending/descending */
				vl.flags ^= Vehicle.VL_DESC;
				vl.flags |= Vehicle.VL_RESORT;
				VehicleGui._sorting.roadveh.order = 0 != (vl.flags & Vehicle.VL_DESC);
				w.SetWindowDirty();
				break;

			case 4: case 5:/* Select sorting criteria dropdown menu */
				Window.ShowDropDownMenu( w, VehicleGui._vehicle_sort_listing, vl.sort_type, 5, 0, 0);
				return;
			case 7: { /* Matrix to show vehicles */
				int id_v = (e.pt.y - VehicleGui.PLY_WND_PRC__OFFSET_TOP_WIDGET) / VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_SMALL;

				if (id_v >= w.vscroll.getCap()) return; // click out of bounds

				id_v += w.vscroll.pos;

				{
					Vehicle v;

					if (id_v >= vl.list_length) return; // click out of list bound

					v	= Vehicle.GetVehicle(vl.sort_list[id_v].index);

					assert(v.getType() == Vehicle.VEH_Road && v.getOwner().id == owner);

					ShowRoadVehViewWindow(v);
				}
			} break;

			case 9: { /* Build new Vehicle */
				TileIndex tile;

				if (!Window.IsWindowOfPrototype(w, _player_roadveh_widgets))
					break;

				tile = Depot._last_built_road_depot_tile;
				do {
					if (Depot.IsTileDepotType(tile, TransportType.Road) && tile.IsTileOwner( Global.gs._local_player)) {
						ShowRoadDepotWindow(tile);
						ShowBuildRoadVehWindow(tile);
						return;
					}

					tile = tile.iadd(1);
					tile.TILE_MASK();
				} while(!tile.equals(Depot._last_built_road_depot_tile));

				ShowBuildRoadVehWindow(null);
			} break;
			case 10: {
				if (!Window.IsWindowOfPrototype( w, _player_roadveh_widgets))
					break;

				VehicleGui.ShowReplaceVehicleWindow(Vehicle.VEH_Road);
				break;
			}
			}
		}	break;

		case WE_DROPDOWN_SELECT: /* we have selected a dropdown item in the list */
			if (vl.sort_type != e.index) {
				// value has changed . resort
				vl.flags |= Vehicle.VL_RESORT;
				vl.sort_type = (byte) e.index;
				VehicleGui._sorting.roadveh.criteria = vl.sort_type;

				// enable 'Sort By' if a sorter criteria is chosen
				if (vl.sort_type != VehicleGui.SORT_BY_UNSORTED)
					w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 3);
			}
			w.SetWindowDirty();
			break;

		case WE_CREATE: /* set up resort timer */
			vl.sort_list = null;
			vl.flags = Vehicle.VL_REBUILD | ((VehicleGui._sorting.roadveh.order ? 1 : 0) << (Vehicle.VL_DESC - 1));
			vl.sort_type = (byte) VehicleGui._sorting.roadveh.criteria;
			vl.resort_timer = Global.DAY_TICKS * VehicleGui.PERIODIC_RESORT_DAYS;
			break;

		case WE_DESTROY:
			//free(vl.sort_list);
			break;

		case WE_TICK: /* resort the list every 20 seconds orso (10 days) */
			if (--vl.resort_timer == 0) {
				Global.DEBUG_misc(1, "Periodic resort road vehicles list player %d station %d",
					owner, station);
				vl.resort_timer = Global.DAY_TICKS * VehicleGui.PERIODIC_RESORT_DAYS;
				vl.flags |= Vehicle.VL_RESORT;
				w.SetWindowDirty();
			}
			break;

		case WE_RESIZE:
			/* Update the scroll + matrix */
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_SMALL);
			w.widget.get(7).unkA = (w.vscroll.getCap() << 8) + 1;
			break;
		default:
			break;
		}
	}

	static final WindowDesc _player_roadveh_desc = new WindowDesc(
		-1, -1, 260, 220,
		Window.WC_ROADVEH_LIST,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_player_roadveh_widgets,
		RoadVehGui::PlayerRoadVehWndProc
	);

	static final WindowDesc _other_player_roadveh_desc = new WindowDesc(
		-1, -1, 260, 220,
		Window.WC_ROADVEH_LIST,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
		_other_player_roadveh_widgets,
		RoadVehGui::PlayerRoadVehWndProc
	);


	//void ShowPlayerRoadVehicles(PlayerID player, StationID station)
	static void ShowPlayerRoadVehicles(int player, int station)
	{
		Window w;

		if ( player == Global.gs._local_player.id) {
			w = Window.AllocateWindowDescFront(_player_roadveh_desc, (station<< 16) | player);
		} else  {
			w = Window.AllocateWindowDescFront(_other_player_roadveh_desc, (station<< 16) | player);
		}
		if (w != null) {
			w.caption_color = (byte) player;
			w.vscroll.setCap(7); // maximum number of vehicles shown
			w.widget.get(7).unkA = (w.vscroll.getCap() << 8) + 1;
			w.resize.step_height = VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_SMALL;
			w.resize.height = 220 - (VehicleGui.PLY_WND_PRC__SIZE_OF_ROW_SMALL * 3); /* Minimum of 4 vehicles */
		}
	}
	
	
}
