package game;

public class RoadGui 
{

	/* $Id: road_gui.c 3298 2005-12-14 06:28:48Z tron $ */















	//needed for catchments



	static void ShowBusStationPicker();
	static void ShowTruckStationPicker();
	static void ShowRoadDepotPicker();

	static boolean _remove_button_clicked;

	static byte _place_road_flag;

	static byte _road_depot_orientation;
	static byte _road_station_picker_orientation;

	void CcPlaySound1D(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) SndPlayTileFx(SND_1F_SPLAT, tile);
	}

	static void PlaceRoad_NE(TileIndex tile)
	{
		_place_road_flag = (_tile_fract_coords.y >= 8) + 4;
		VpStartPlaceSizing(tile, VPM_FIX_X);
	}

	static void PlaceRoad_NW(TileIndex tile)
	{
		_place_road_flag = (_tile_fract_coords.x >= 8) + 0;
		VpStartPlaceSizing(tile, VPM_FIX_Y);
	}

	static void PlaceRoad_Bridge(TileIndex tile)
	{
		VpStartPlaceSizing(tile, VPM_X_OR_Y);
	}


	void CcBuildRoadTunnel(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			SndPlayTileFx(SND_20_SPLAT_2, tile);
			ResetObjectToPlace();
		} else {
			SetRedErrorSquare(_build_tunnel_endtile);
		}
	}

	static void PlaceRoad_Tunnel(TileIndex tile)
	{
		DoCommandP(tile, 0x200, 0, CcBuildRoadTunnel, Cmd.CMD_BUILD_TUNNEL | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_5016_CAN_T_BUILD_TUNNEL_HERE));
	}

	static void BuildRoadOutsideStation(TileIndex tile, int direction)
	{
		static final byte _roadbits_by_dir[4] = {2,1,8,4};
		tile += TileOffsByDir(direction);
		// if there is a roadpiece just outside of the station entrance, build a connecting route
		if (tile.IsTileType( TileTypes.MP_STREET) && !(tile.getMap().m5 & 0x20)) {
			DoCommandP(tile, _roadbits_by_dir[direction], 0, null, Cmd.CMD_BUILD_ROAD);
		}
	}

	void CcRoadDepot(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			SndPlayTileFx(SND_1F_SPLAT, tile);
			ResetObjectToPlace();
			BuildRoadOutsideStation(tile, (int)p1);
		}
	}

	static void PlaceRoad_Depot(TileIndex tile)
	{
		DoCommandP(tile, _road_depot_orientation, 0, CcRoadDepot, Cmd.CMD_BUILD_ROAD_DEPOT | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1807_CAN_T_BUILD_ROAD_VEHICLE));
	}

	static void PlaceRoad_BusStation(TileIndex tile)
	{
		DoCommandP(tile, _road_station_picker_orientation, RS_BUS, CcRoadDepot, Cmd.CMD_BUILD_ROAD_STOP | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1808_CAN_T_BUILD_BUS_STATION));
	}

	static void PlaceRoad_TruckStation(TileIndex tile)
	{
		DoCommandP(tile, _road_station_picker_orientation, RS_TRUCK, CcRoadDepot, Cmd.CMD_BUILD_ROAD_STOP | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1809_CAN_T_BUILD_TRUCK_STATION));
	}

	static void PlaceRoad_DemolishArea(TileIndex tile)
	{
		VpStartPlaceSizing(tile, 4);
	}

	typedef void OnButtonClick(Window w);

	static void BuildRoadClick_NE(Window w)
	{
		HandlePlacePushButton(w, 3, Sprite.SPR_CURSOR_ROAD_NESW, 1, PlaceRoad_NE);
	}

	static void BuildRoadClick_NW(Window w)
	{
		HandlePlacePushButton(w, 4, Sprite.SPR_CURSOR_ROAD_NWSE, 1, PlaceRoad_NW);
	}


	static void BuildRoadClick_Demolish(Window w)
	{
		HandlePlacePushButton(w, 5, ANIMCURSOR_DEMOLISH, 1, PlaceRoad_DemolishArea);
	}

	static void BuildRoadClick_Depot(Window w)
	{
		if (Global._game_mode == GameModes.GM_EDITOR) return;
		if (HandlePlacePushButton(w, 6, Sprite.SPR_CURSOR_ROAD_DEPOT, 1, PlaceRoad_Depot)) ShowRoadDepotPicker();
	}

	static void BuildRoadClick_BusStation(Window w)
	{
		if (Global._game_mode == GameModes.GM_EDITOR) return;
		if (HandlePlacePushButton(w, 7, Sprite.SPR_CURSOR_BUS_STATION, 1, PlaceRoad_BusStation)) ShowBusStationPicker();
	}

	static void BuildRoadClick_TruckStation(Window w)
	{
		if (Global._game_mode == GameModes.GM_EDITOR) return;
		if (HandlePlacePushButton(w, 8, Sprite.SPR_CURSOR_TRUCK_STATION, 1, PlaceRoad_TruckStation)) ShowTruckStationPicker();
	}

	static void BuildRoadClick_Bridge(Window w)
	{
		HandlePlacePushButton(w, 9, Sprite.SPR_CURSOR_BRIDGE, 1, PlaceRoad_Bridge);
	}

	static void BuildRoadClick_Tunnel(Window w)
	{
		HandlePlacePushButton(w, 10, Sprite.SPR_CURSOR_ROAD_TUNNEL, 3, PlaceRoad_Tunnel);
	}

	static void BuildRoadClick_Remove(Window w)
	{
		if (BitOps.HASBIT(w.disabled_state, 11)) return;
		SetWindowDirty(w);
		SndPlayFx(SND_15_BEEP);
		TOGGLEBIT(w.click_state, 11);
		SetSelectionRed(BitOps.HASBIT(w.click_state, 11) != 0);
	}

	static void BuildRoadClick_Landscaping(Window w)
	{
		ShowTerraformToolbar();
	}

	static OnButtonClick* final _build_road_button_proc[] = {
		BuildRoadClick_NE,
		BuildRoadClick_NW,
		BuildRoadClick_Demolish,
		BuildRoadClick_Depot,
		BuildRoadClick_BusStation,
		BuildRoadClick_TruckStation,
		BuildRoadClick_Bridge,
		BuildRoadClick_Tunnel,
		BuildRoadClick_Remove,
		BuildRoadClick_Landscaping,
	};

	static void BuildRoadToolbWndProc(Window  w, WindowEvent  e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT:
			w.disabled_state &= ~(1 << 11);
			if (!(w.click_state & ((1<<3)|(1<<4)))) {
				w.disabled_state |= (1 << 11);
				w.click_state &= ~(1<<11);
			}
			DrawWindowWidgets(w);
			break;

		case WindowEvents.WE_CLICK: {
			if (e.click.widget >= 3) _build_road_button_proc[e.click.widget - 3](w);
		}	break;

		case WindowEvents.WE_KEYPRESS:
			switch (e.keypress.keycode) {
				case '1': BuildRoadClick_NE(w);           break;
				case '2': BuildRoadClick_NW(w);           break;
				case '3': BuildRoadClick_Demolish(w);     break;
				case '4': BuildRoadClick_Depot(w);        break;
				case '5': BuildRoadClick_BusStation(w);   break;
				case '6': BuildRoadClick_TruckStation(w); break;
				case 'B': BuildRoadClick_Bridge(w);       break;
				case 'T': BuildRoadClick_Tunnel(w);       break;
				case 'R': BuildRoadClick_Remove(w);       break;
				case 'L': BuildRoadClick_Landscaping(w);  break;
				default: return;
			}
			MarkTileDirty(_thd.pos.x, _thd.pos.y); // redraw tile selection
			e.keypress.cont = false;
			break;

		case WindowEvents.WE_PLACE_OBJ:
			_remove_button_clicked = (w.click_state & (1 << 11)) != 0;
			_place_proc(e.place.tile);
			break;

		case WindowEvents.WE_ABORT_PLACE_OBJ:
			UnclickWindowButtons(w);
			SetWindowDirty(w);

			w = FindWindowById(Window.WC_BUS_STATION, 0);
			if (w != null) WP(w,def_d).close = true;
			w = FindWindowById(Window.WC_TRUCK_STATION, 0);
			if (w != null) WP(w,def_d).close = true;
			w = FindWindowById(Window.WC_BUILD_DEPOT, 0);
			if (w != null) WP(w,def_d).close = true;
			break;

		case WindowEvents.WE_PLACE_DRAG: {
			int sel_method;
			if (e.place.userdata == 1) {
				sel_method = VPM_FIX_X;
				_place_road_flag = (_place_road_flag&~2) | ((e.place.pt.y&8)>>2);
			} else if (e.place.userdata == 2) {
				sel_method = VPM_FIX_Y;
				_place_road_flag = (_place_road_flag&~2) | ((e.place.pt.x&8)>>2);
			} else if (e.place.userdata == 4) {
				sel_method = VPM_X_AND_Y;
			} else {
				sel_method = VPM_X_OR_Y;
			}

			VpSelectTilesWithMethod(e.place.pt.x, e.place.pt.y, sel_method);
			return;
		}

		case WindowEvents.WE_PLACE_MOUSEUP:
			if (e.place.pt.x != -1) {
				TileIndex start_tile = e.place.starttile;
				TileIndex end_tile = e.place.tile;

				if (e.place.userdata == 0) {
					ResetObjectToPlace();
					ShowBuildBridgeWindow(start_tile, end_tile, 0x80);
				} else if (e.place.userdata != 4) {
					DoCommandP(end_tile, start_tile, _place_road_flag, CcPlaySound1D,
						_remove_button_clicked ?
						Cmd.CMD_REMOVE_LONG_ROAD | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1805_CAN_T_REMOVE_ROAD_FROM) :
						Cmd.CMD_BUILD_LONG_ROAD | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1804_CAN_T_BUILD_ROAD_HERE));
				} else {
					DoCommandP(end_tile, start_tile, _place_road_flag, CcPlaySound10, Cmd.CMD_CLEAR_AREA | Cmd.CMD_MSG(Str.STR_00B5_CAN_T_CLEAR_THIS_AREA));
				}
			}
			break;

		case WindowEvents.WE_PLACE_PRESIZE: {
			TileIndex tile = e.place.tile;

			DoCommandByTile(tile, 0x200, 0, Cmd.DC_AUTO, Cmd.CMD_BUILD_TUNNEL);
			VpSetPresizeRange(tile, _build_tunnel_endtile==0?tile:_build_tunnel_endtile);
			break;
		}

		case WindowEvents.WE_DESTROY:
			if (Global._patches.link_terraform_toolbar) Window.DeleteWindowById(Window.WC_SCEN_LAND_GEN, 0);
			break;
		}
	}

	static final Widget _build_road_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   227,     0,    13, Str.STR_1802_ROAD_CONSTRUCTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7,   228,   239,     0,    13, 0x0,                   Str.STR_STICKY_BUTTON},

	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,    21,    14,    35, Sprite.SPR_IMG_ROAD_NW,				Str.STR_180B_BUILD_ROAD_SECTION},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    22,    43,    14,    35, Sprite.SPR_IMG_ROAD_NE,				Str.STR_180B_BUILD_ROAD_SECTION},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    44,    65,    14,    35, Sprite.SPR_IMG_DYNAMITE,			Str.STR_018D_DEMOLISH_BUILDINGS_ETC},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    66,    87,    14,    35, Sprite.SPR_IMG_ROAD_DEPOT,		Str.STR_180C_BUILD_ROAD_VEHICLE_DEPOT},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    88,   109,    14,    35, Sprite.SPR_IMG_BUS_STATION,		Str.STR_180D_BUILD_BUS_STATION},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   110,   131,    14,    35, Sprite.SPR_IMG_TRUCK_BAY,			Str.STR_180E_BUILD_TRUCK_LOADING_BAY},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   132,   173,    14,    35, Sprite.SPR_IMG_BRIDGE,				Str.STR_180F_BUILD_ROAD_BRIDGE},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   174,   195,    14,    35, Sprite.SPR_IMG_ROAD_TUNNEL,		Str.STR_1810_BUILD_ROAD_TUNNEL},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   196,   217,    14,    35, Sprite.SPR_IMG_REMOVE, 				Str.STR_1811_TOGGLE_BUILD_REMOVE_FOR},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   218,   239,    14,    35, Sprite.SPR_IMG_LANDSCAPING, Str.STR_LANDSCAPING_TOOLBAR_TIP},
	{   WIDGETS_END},
	};

	static final WindowDesc _build_road_desc = {
		640-240, 22, 240, 36,
		Window.WC_BUILD_TOOLBAR,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
		_build_road_widgets,
		BuildRoadToolbWndProc
	};

	void ShowBuildRoadToolbar()
	{
		if (Global._current_player == Owner.OWNER_SPECTATOR) return;
		Window.DeleteWindowById(Window.WC_BUILD_TOOLBAR, 0);
		AllocateWindowDesc(&_build_road_desc);
		if (Global._patches.link_terraform_toolbar) ShowTerraformToolbar();
	}

	static final Widget _build_road_scen_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,	Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   161,     0,    13, Str.STR_1802_ROAD_CONSTRUCTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7,   162,   173,     0,    13, 0x0,                   Str.STR_STICKY_BUTTON},

	{     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,     0,    21,    14,    35, 0x51D,			Str.STR_180B_BUILD_ROAD_SECTION},
	{     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,    22,    43,    14,    35, 0x51E,			Str.STR_180B_BUILD_ROAD_SECTION},
	{     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,    44,    65,    14,    35, 0x2BF,			Str.STR_018D_DEMOLISH_BUILDINGS_ETC},
	{      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,				Str.STR_NULL},
	{      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,				Str.STR_NULL},
	{      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,				Str.STR_NULL},
	{     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,    66,   107,    14,    35, 0xA22,			Str.STR_180F_BUILD_ROAD_BRIDGE},
	{     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,   108,   129,    14,    35, 0x97D,			Str.STR_1810_BUILD_ROAD_TUNNEL},
	{     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,   130,   151,    14,    35, 0x2CA,			Str.STR_1811_TOGGLE_BUILD_REMOVE_FOR},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   152,   173,    14,    35, Sprite.SPR_IMG_LANDSCAPING, Str.STR_LANDSCAPING_TOOLBAR_TIP},
	{   WIDGETS_END},
	};

	static final WindowDesc _build_road_scen_desc = {
		-1, -1, 174, 36,
		Window.WC_SCEN_BUILD_ROAD,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
		_build_road_scen_widgets,
		BuildRoadToolbWndProc
	};

	void ShowBuildRoadScenToolbar()
	{
		AllocateWindowDescFront(&_build_road_scen_desc, 0);
	}

	static void BuildRoadDepotWndProc(Window  w, WindowEvent  e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT:
			w.click_state = (1<<3) << _road_depot_orientation;
			DrawWindowWidgets(w);

			DrawRoadDepotSprite(70, 17, 0);
			DrawRoadDepotSprite(70, 69, 1);
			DrawRoadDepotSprite( 2, 69, 2);
			DrawRoadDepotSprite( 2, 17, 3);
			break;

		case WindowEvents.WE_CLICK: {
			switch (e.click.widget) {
			case 3: case 4: case 5: case 6:
				_road_depot_orientation = e.click.widget - 3;
				SndPlayFx(SND_15_BEEP);
				SetWindowDirty(w);
				break;
			}
		}	break;

		case WindowEvents.WE_MOUSELOOP:
			if (WP(w,def_d).close) DeleteWindow(w);
			break;

		case WindowEvents.WE_DESTROY:
			if (!WP(w,def_d).close) ResetObjectToPlace();
			break;
		}
	}

	static final Widget _build_road_depot_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   139,     0,    13, Str.STR_1806_ROAD_DEPOT_ORIENTATION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   139,    14,   121, 0x0,			Str.STR_NULL},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    17,    66, 0x0,			Str.STR_1813_SELEAcceptedCargo.CT_ROAD_VEHICLE_DEPOT},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    69,   118, 0x0,			Str.STR_1813_SELEAcceptedCargo.CT_ROAD_VEHICLE_DEPOT},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    69,   118, 0x0,			Str.STR_1813_SELEAcceptedCargo.CT_ROAD_VEHICLE_DEPOT},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    17,    66, 0x0,			Str.STR_1813_SELEAcceptedCargo.CT_ROAD_VEHICLE_DEPOT},
	{   WIDGETS_END},
	};

	static final WindowDesc _build_road_depot_desc = {
		-1,-1, 140, 122,
		Window.WC_BUILD_DEPOT,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_build_road_depot_widgets,
		BuildRoadDepotWndProc
	};

	static void ShowRoadDepotPicker()
	{
		AllocateWindowDesc(&_build_road_depot_desc);
	}

	static void RoadStationPickerWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WindowEvents.WE_PAINT: {
			int image;

			if (WP(w,def_d).close) return;

			w.click_state = ((1<<3) << _road_station_picker_orientation)	|
											 ((1<<7) << _station_show_coverage);
			DrawWindowWidgets(w);

			if (_station_show_coverage) {
				int rad = Global._patches.modified_catchment ? CA_TRUCK /* = CA_BUS */ : 4;
				SetTileSelectBigSize(-rad, -rad, 2 * rad, 2 * rad);
			} else
				SetTileSelectSize(1, 1);

			image = (w.window_class == Window.WC_BUS_STATION) ? 0x47 : 0x43;

			StationPickerGfx.DrawSprite(103, 35, 0, image);
			StationPickerGfx.DrawSprite(103, 85, 0, image+1);
			StationPickerGfx.DrawSprite(35, 85, 0, image+2);
			StationPickerGfx.DrawSprite(35, 35, 0, image+3);

			DrawStringCentered(70, 120, Str.STR_3066_COVERAGE_AREA_HIGHLIGHT, 0);
			DrawStationCoverageAreaText(2, 146,
				((w.window_class == Window.WC_BUS_STATION) ? (1<<AcceptedCargo.CT_PASSENGERS) : ~(1<<AcceptedCargo.CT_PASSENGERS)),
				3);

		} break;

		case WindowEvents.WE_CLICK: {
			switch (e.click.widget) {
			case 3: case 4: case 5: case 6:
				_road_station_picker_orientation = e.click.widget - 3;
				SndPlayFx(SND_15_BEEP);
				SetWindowDirty(w);
				break;
			case 7: case 8:
				_station_show_coverage = e.click.widget - 7;
				SndPlayFx(SND_15_BEEP);
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

	static final Widget _bus_station_picker_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,		Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   139,     0,    13, Str.STR_3042_BUS_STATION_ORIENTATION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   139,    14,   176, 0x0,					Str.STR_NULL},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    17,    66, 0x0,					Str.STR_3051_SELEAcceptedCargo.CT_BUS_STATION_ORIENTATION},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    69,   118, 0x0,					Str.STR_3051_SELEAcceptedCargo.CT_BUS_STATION_ORIENTATION},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    69,   118, 0x0,					Str.STR_3051_SELEAcceptedCargo.CT_BUS_STATION_ORIENTATION},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    17,    66, 0x0,					Str.STR_3051_SELEAcceptedCargo.CT_BUS_STATION_ORIENTATION},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    10,    69,   133,   144, Str.STR_02DB_OFF,Str.STR_3065_DON_T_HIGHLIGHT_COVERAGE},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    70,   129,   133,   144, Str.STR_02DA_ON,	Str.STR_3064_HIGHLIGHT_COVERAGE_AREA},
	{   WIDGETS_END},
	};

	static final WindowDesc _bus_station_picker_desc = {
		-1,-1, 140, 177,
		Window.WC_BUS_STATION,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_bus_station_picker_widgets,
		RoadStationPickerWndProc
	};

	static void ShowBusStationPicker()
	{
		AllocateWindowDesc(&_bus_station_picker_desc);
	}

	static final Widget _truck_station_picker_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,		Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   139,     0,    13, Str.STR_3043_TRUCK_STATION_ORIENT, Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   139,    14,   176, 0x0,					Str.STR_NULL},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    17,    66, 0x0,					Str.STR_3052_SELEAcceptedCargo.CT_TRUCK_LOADING_BAY},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    69,   118, 0x0,					Str.STR_3052_SELEAcceptedCargo.CT_TRUCK_LOADING_BAY},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    69,   118, 0x0,					Str.STR_3052_SELEAcceptedCargo.CT_TRUCK_LOADING_BAY},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    17,    66, 0x0,					Str.STR_3052_SELEAcceptedCargo.CT_TRUCK_LOADING_BAY},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    10,    69,   133,   144, Str.STR_02DB_OFF, Str.STR_3065_DON_T_HIGHLIGHT_COVERAGE},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    70,   129,   133,   144, Str.STR_02DA_ON,	Str.STR_3064_HIGHLIGHT_COVERAGE_AREA},
	{   WIDGETS_END},
	};

	static final WindowDesc _truck_station_picker_desc = {
		-1,-1, 140, 177,
		Window.WC_TRUCK_STATION,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_truck_station_picker_widgets,
		RoadStationPickerWndProc
	};

	static void ShowTruckStationPicker()
	{
		AllocateWindowDesc(&_truck_station_picker_desc);
	}

	void InitializeRoadGui()
	{
		_road_depot_orientation = 3;
		_road_station_picker_orientation = 3;
	}
	
	
}
