package com.dzavalishin.xui;

import com.dzavalishin.game.Airport;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Depot;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.Terraform;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ifaces.OnButtonClick;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Sound;

public class AirportGui 
{
	static int _selected_airport_type;


	public static void CcBuildAirport(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			Sound.SndPlayTileFx(Snd.SND_1F_SPLAT, tile);
			ViewPort.ResetObjectToPlace();
		}
	}

	static void PlaceAirport(TileIndex tile)
	{
		Cmd.DoCommandP(tile, _selected_airport_type, 0, AirportGui::CcBuildAirport, Cmd.CMD_BUILD_AIRPORT | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_A001_CAN_T_BUILD_AIRPORT_HERE));
	}

	static void PlaceAir_DemolishArea(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, 4);
	}


	static void BuildAirClick_Airport(Window w)
	{
		if (Gui.HandlePlacePushButton(w, 3, Sprite.SPR_CURSOR_AIRPORT, 1, AirportGui::PlaceAirport)) ShowBuildAirportPicker();
	}

	static void BuildAirClick_Demolish(Window w)
	{
		Gui.HandlePlacePushButton(w, 4, Sprite.ANIMCURSOR_DEMOLISH, 1, AirportGui::PlaceAir_DemolishArea);
	}

	static void BuildAirClick_Landscaping(Window w)
	{
		Terraform.ShowTerraformToolbar();
	}

	//typedef void OnButtonClick(Window w);
	static final OnButtonClick _build_air_button_proc[] = {
			AirportGui::BuildAirClick_Airport,
			AirportGui::BuildAirClick_Demolish,
			AirportGui::BuildAirClick_Landscaping
	};

	static void BuildAirToolbWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT:
			w.DrawWindowWidgets();
			break;

		case WE_CLICK:
			if (e.widget - 3 >= 0)
				_build_air_button_proc[e.widget - 3].accept(w);
			break;

		case WE_KEYPRESS: {
			switch (e.keycode) {
				case '1': BuildAirClick_Airport(w); break;
				case '2': BuildAirClick_Demolish(w); break;
				case 'l': BuildAirClick_Landscaping(w); break;
				default: return;
			}
		} break;

		case WE_PLACE_OBJ:
			Global._place_proc.accept(e.tile);
			break;

		case WE_PLACE_DRAG: {
			ViewPort.VpSelectTilesWithMethod(e.pt.x, e.pt.y, e.userdata);
			return;
		}

		case WE_PLACE_MOUSEUP:
			if (e.pt.x != -1) {
				Cmd.DoCommandP(e.tile, e.starttile.getTile(), 0, /*CcPlaySound10*/ null, Cmd.CMD_CLEAR_AREA | Cmd.CMD_MSG(Str.STR_00B5_CAN_T_CLEAR_THIS_AREA));
			}
			break;

		case WE_ABORT_PLACE_OBJ:
			w.UnclickWindowButtons();
			w.SetWindowDirty();
			w = Window.FindWindowById(Window.WC_BUILD_STATION, 0);
			if (w != null)
				w.as_def_d().close = true;
			break;

		case WE_DESTROY:
			if (Global._patches.link_terraform_toolbar) Window.DeleteWindowById(Window.WC_SCEN_LAND_GEN, 0);
			break;
		default:
			break;
		}
	}

	static final Widget _air_toolbar_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,    73,     0,    13, Str.STR_A000_AIRPORTS,			Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7,    74,    85,     0,    13, 0x0,                   Str.STR_STICKY_BUTTON),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,    41,    14,    35, 0x2E8,									Str.STR_A01E_BUILD_AIRPORT),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    42,    63,    14,    35, 0x2BF,									Str.STR_018D_DEMOLISH_BUILDINGS_ETC),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    64,    85,    14,    35, Sprite.SPR_IMG_LANDSCAPING,	Str.STR_LANDSCAPING_TOOLBAR_TIP),
	};


	static final WindowDesc _air_toolbar_desc = new WindowDesc(
		640-86, 22, 86, 36,
		Window.WC_BUILD_TOOLBAR,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
		_air_toolbar_widgets,
		AirportGui::BuildAirToolbWndProc
	);

	static void ShowBuildAirToolbar()
	{
		if (PlayerID.getCurrent().isSpectator()) return;
		Window.DeleteWindowById(Window.WC_BUILD_TOOLBAR, 0);
		Window.AllocateWindowDescFront(_air_toolbar_desc, 0);
		if (Global._patches.link_terraform_toolbar) Terraform.ShowTerraformToolbar();
	}

	static void BuildAirportPickerWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int sel;
			int rad = 4; // default catchment radious
			int avail_airports;

			if (w.as_def_d().close) return;

			sel = _selected_airport_type;
			avail_airports = Airport.GetValidAirports();

			if (!BitOps.HASBIT(avail_airports, 0) && sel == Airport.AT_SMALL) sel = Airport.AT_LARGE;
			if (!BitOps.HASBIT(avail_airports, 1) && sel == Airport.AT_LARGE) sel = Airport.AT_SMALL;

			/* 'Country Airport' starts at widget 3, and if its bit is set, it is
			 * available, so take its opposite value to set the disabled_state. There
			 * are only 5 available airports, so XOR with 0x1F (1 1111) */
			w.disabled_state = (avail_airports ^ 0x1F) << 3;

			_selected_airport_type = sel;
			// select default the coverage area to 'Off' (8)
			w.click_state = ((1<<3) << sel) | ((1<<8) << Gui._station_show_coverage);
			ViewPort.SetTileSelectSize(Station._airport_size_x[sel],Station._airport_size_y[sel]);

			if (Global._patches.modified_catchment) {
				switch (sel) {
					case Airport.AT_OILRIG:        rad = Station.CA_AIR_OILPAD;   break;
					case Airport.AT_HELIPORT:      rad = Station.CA_AIR_HELIPORT; break;
					case Airport.AT_SMALL:         rad = Station.CA_AIR_SMALL;    break;
					case Airport.AT_LARGE:         rad = Station.CA_AIR_LARGE;    break;
					case Airport.AT_METROPOLITAN:  rad = Station.CA_AIR_METRO;    break;
					case Airport.AT_INTERNATIONAL: rad = Station.CA_AIR_INTER;    break;
				}
			}

			if (Gui._station_show_coverage != 0) ViewPort.SetTileSelectBigSize(-rad, -rad, 2 * rad, 2 * rad);

			w.DrawWindowWidgets();
	    // strings such as 'Size' and 'Coverage Area'
			Gfx.DrawStringCentered(74, 16, Str.STR_305B_SIZE, 0);
			Gfx.DrawStringCentered(74, 78, Str.STR_3066_COVERAGE_AREA_HIGHLIGHT, 0);
			MiscGui.DrawStationCoverageAreaText(2, 104, -1, rad);
			break;
		}

		case WE_CLICK: {
			switch (e.widget) {
			case 3: case 4: case 5: case 6: case 7:
				_selected_airport_type = e.widget - 3;
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;
			case 8: case 9:
				Gui._station_show_coverage = e.widget - 8;
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;
			}
		} break;

		case WE_MOUSELOOP: {
			if (w.as_def_d().close) {
				w.DeleteWindow();
				return;
			}

			MiscGui.CheckRedrawStationCoverage(w);
		} break;

		case WE_DESTROY:
			if (!w.as_def_d().close) ViewPort.ResetObjectToPlace();
			break;
		case WE_4:
		case WE_ABORT_PLACE_OBJ:
		case WE_CREATE:
		case WE_DRAGDROP:
		case WE_DROPDOWN_SELECT:
		case WE_KEYPRESS:
		case WE_MESSAGE:
		case WE_MOUSEOVER:
		case WE_ON_EDIT_TEXT:
		case WE_ON_EDIT_TEXT_CANCEL:
		case WE_PLACE_DRAG:
		case WE_PLACE_MOUSEUP:
		case WE_PLACE_OBJ:
		case WE_PLACE_PRESIZE:
		case WE_POPUPMENU_OVER:
		case WE_POPUPMENU_SELECT:
		case WE_RCLICK:
		case WE_RESIZE:
		case WE_TICK:
		case WE_TIMEOUT:
		default:
			break;
		}
	}

	static final Widget _build_airport_picker_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,										Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   147,     0,    13, Str.STR_3001_AIRPORT_SELECTION,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   147,    14,   130, 0x0,													Str.STR_NULL),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_NONE,    14,     2,    73,    27,    38, Str.STR_3059_SMALL,							Str.STR_3058_SELECT_SIZE_TYPE_OF_AIRPORT),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_NONE,    14,    74,   145,    27,    38, Str.STR_305A_LARGE,							Str.STR_3058_SELECT_SIZE_TYPE_OF_AIRPORT),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_NONE,    14,     2,   145,    63,    74, Str.STR_306B_HELIPORT,						Str.STR_3058_SELECT_SIZE_TYPE_OF_AIRPORT),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_NONE,    14,     2,   145,    39,    50, Str.STR_305AA_LARGE,	  					Str.STR_3058_SELECT_SIZE_TYPE_OF_AIRPORT),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_NONE,    14,     2,   145,    51,    62, Str.STR_305AB_LARGE,	  					Str.STR_3058_SELECT_SIZE_TYPE_OF_AIRPORT),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    14,    73,    88,    98, Str.STR_02DB_OFF,								Str.STR_3065_DON_T_HIGHLIGHT_COVERAGE),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    74,   133,    88,    98, Str.STR_02DA_ON,									Str.STR_3064_HIGHLIGHT_COVERAGE_AREA),
	};

	static final WindowDesc _build_airport_desc = new WindowDesc(
		-1, -1, 148, 131, // height, 130+1
		Window.WC_BUILD_STATION,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_build_airport_picker_widgets,
		AirportGui::BuildAirportPickerWndProc
	);

	static void ShowBuildAirportPicker()
	{
		Window.AllocateWindowDesc(_build_airport_desc);
	}

	public static void InitializeAirportGui()
	{
		_selected_airport_type = Airport.AT_SMALL;
		Depot._last_built_aircraft_depot_tile = null;
	}
	
	
	
}
