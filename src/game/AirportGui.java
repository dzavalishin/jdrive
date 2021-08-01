package game;

public class AirportGui 
{


	static byte _selected_airport_type;

	//static void ShowBuildAirportPicker();


	void CcBuildAirport(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			//SndPlayTileFx(SND_1F_SPLAT, tile);
			ResetObjectToPlace();
		}
	}

	static void PlaceAirport(TileIndex tile)
	{
		DoCommandP(tile, _selected_airport_type, 0, CcBuildAirport, Cmd.CMD_BUILD_AIRPORT | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_A001_CAN_T_BUILD_AIRPORT_HERE));
	}

	static void PlaceAir_DemolishArea(TileIndex tile)
	{
		VpStartPlaceSizing(tile, 4);
	}


	static void BuildAirClick_Airport(Window w)
	{
		if (HandlePlacePushButton(w, 3, Sprite.SPR_CURSOR_AIRPORT, 1, PlaceAirport)) ShowBuildAirportPicker();
	}

	static void BuildAirClick_Demolish(Window w)
	{
		HandlePlacePushButton(w, 4, ANIMCURSOR_DEMOLISH, 1, PlaceAir_DemolishArea);
	}

	static void BuildAirClick_Landscaping(Window w)
	{
		ShowTerraformToolbar();
	}

	//typedef void OnButtonClick(Window w);
	static OnButtonClick  final _build_air_button_proc[] = {
		BuildAirClick_Airport,
		BuildAirClick_Demolish,
		BuildAirClick_Landscaping,
	};

	static void BuildAirToolbWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT:
			w.DrawWindowWidgets();
			break;

		case WindowEvents.WE_CLICK:
			if (e.click.widget - 3 >= 0)
				_build_air_button_proc[e.click.widget - 3](w);
			break;

		case WindowEvents.WE_KEYPRESS: {
			switch (e.keypress.keycode) {
				case '1': BuildAirClick_Airport(w); break;
				case '2': BuildAirClick_Demolish(w); break;
				case 'l': BuildAirClick_Landscaping(w); break;
				default: return;
			}
		} break;

		case WindowEvents.WE_PLACE_OBJ:
			_place_proc(e.place.tile);
			break;

		case WindowEvents.WE_PLACE_DRAG: {
			VpSelectTilesWithMethod(e.place.pt.x, e.place.pt.y, e.place.userdata);
			return;
		}

		case WindowEvents.WE_PLACE_MOUSEUP:
			if (e.place.pt.x != -1) {
				DoCommandP(e.place.tile, e.place.starttile, 0, CcPlaySound10, Cmd.CMD_CLEAR_AREA | Cmd.CMD_MSG(Str.STR_00B5_CAN_T_CLEAR_THIS_AREA));
			}
			break;

		case WindowEvents.WE_ABORT_PLACE_OBJ:
			UnclickWindowButtons(w);
			SetWindowDirty(w);
			w = FindWindowById(Window.WC_BUILD_STATION, 0);
			if (w != 0)
				WP(w,def_d).close = true;
			break;

		case WindowEvents.WE_DESTROY:
			if (Global._patches.link_terraform_toolbar) Window.DeleteWindowById(Window.WC_SCEN_LAND_GEN, 0);
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

	void ShowBuildAirToolbar()
	{
		if (Global._current_player.id == Owner.OWNER_SPECTATOR) return;
		Window.DeleteWindowById(Window.WC_BUILD_TOOLBAR, 0);
		Window.AllocateWindowDescFront(_air_toolbar_desc, 0);
		if (Global._patches.link_terraform_toolbar) ShowTerraformToolbar();
	}

	static void BuildAirportPickerWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT: {
			int sel;
			int rad = 4; // default catchment radious
			int avail_airports;

			if (WP(w,def_d).close) return;

			sel = _selected_airport_type;
			avail_airports = GetValidAirports();

			if (!BitOps.HASBIT(avail_airports, 0) && sel == AT_SMALL) sel = AT_LARGE;
			if (!BitOps.HASBIT(avail_airports, 1) && sel == AT_LARGE) sel = AT_SMALL;

			/* 'Country Airport' starts at widget 3, and if its bit is set, it is
			 * available, so take its opposite value to set the disabled_state. There
			 * are only 5 available airports, so XOR with 0x1F (1 1111) */
			w.disabled_state = (avail_airports ^ 0x1F) << 3;

			_selected_airport_type = sel;
			// select default the coverage area to 'Off' (8)
			w.click_state = ((1<<3) << sel) | ((1<<8) << _station_show_coverage);
			SetTileSelectSize(_airport_size_x[sel],_airport_size_y[sel]);

			if (Global._patches.modified_catchment) {
				switch (sel) {
					case AT_OILRIG:        rad = CA_AIR_OILPAD;   break;
					case AT_HELIPORT:      rad = CA_AIR_HELIPORT; break;
					case AT_SMALL:         rad = CA_AIR_SMALL;    break;
					case AT_LARGE:         rad = CA_AIR_LARGE;    break;
					case AT_METROPOLITAN:  rad = CA_AIR_METRO;    break;
					case AT_INTERNATIONAL: rad = CA_AIR_INTER;    break;
				}
			}

			if (_station_show_coverage) SetTileSelectBigSize(-rad, -rad, 2 * rad, 2 * rad);

			DrawWindowWidgets(w);
	    // strings such as 'Size' and 'Coverage Area'
			DrawStringCentered(74, 16, Str.STR_305B_SIZE, 0);
			DrawStringCentered(74, 78, Str.STR_3066_COVERAGE_AREA_HIGHLIGHT, 0);
			DrawStationCoverageAreaText(2, 104, (int)-1, rad);
			break;
		}

		case WindowEvents.WE_CLICK: {
			switch (e.click.widget) {
			case 3: case 4: case 5: case 6: case 7:
				_selected_airport_type = e.click.widget - 3;
				SndPlayFx(SND_15_BEEP);
				SetWindowDirty(w);
				break;
			case 8: case 9:
				_station_show_coverage = e.click.widget - 8;
				//SndPlayFx(SND_15_BEEP);
				SetWindowDirty(w);
				break;
			}
		} break;

		case WindowEvents.WE_MOUSELOOP: {
			if (WP(w,def_d).close) {
				DeleteWindow(w);
				return;
			}

			CheckRedrawStationCoverage(w);
		} break;

		case WindowEvents.WE_DESTROY:
			if (!WP(w,def_d).close) ResetObjectToPlace();
			break;
		}
	}

	static final Widget _build_airport_picker_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,										Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   147,     0,    13, Str.STR_3001_AIRPORT_SELECTION,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   147,    14,   130, 0x0,													Str.STR_NULL),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_NONE,    14,     2,    73,    27,    38, Str.STR_3059_SMALL,							Str.STR_3058_SELEAcceptedCargo.CT_SIZE_TYPE_OF_AIRPORT),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_NONE,    14,    74,   145,    27,    38, Str.STR_305A_LARGE,							Str.STR_3058_SELEAcceptedCargo.CT_SIZE_TYPE_OF_AIRPORT),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_NONE,    14,     2,   145,    63,    74, Str.STR_306B_HELIPORT,						Str.STR_3058_SELEAcceptedCargo.CT_SIZE_TYPE_OF_AIRPORT),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_NONE,    14,     2,   145,    39,    50, Str.STR_305AA_LARGE,	  					Str.STR_3058_SELEAcceptedCargo.CT_SIZE_TYPE_OF_AIRPORT),
	new Widget(Window.WWT_NODISTXTBTN,   Window.RESIZE_NONE,    14,     2,   145,    51,    62, Str.STR_305AB_LARGE,	  					Str.STR_3058_SELEAcceptedCargo.CT_SIZE_TYPE_OF_AIRPORT),
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

	void InitializeAirportGui()
	{
		_selected_airport_type = AT_SMALL;
		_last_built_aircraft_depot_tile = 0;
	}
	
	
	
}
