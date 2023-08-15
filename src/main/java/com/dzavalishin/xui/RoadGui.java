package com.dzavalishin.xui;

import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.RoadStopType;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.Bridge;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Road;
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
import com.dzavalishin.util.Sprites;

public class RoadGui 
{


	//needed for catchments



	static boolean _remove_button_clicked;
	static byte _place_road_flag;

	static byte _road_depot_orientation;
	static byte _road_station_picker_orientation;

	public static void CcPlaySound1D(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) Sound.SndPlayTileFx(Snd.SND_1F_SPLAT, tile);
	}

	static void PlaceRoad_NE(TileIndex tile)
	{
		_place_road_flag = (byte) ((BitOps.b2i( ViewPort._tile_fract_coords.y >= 8) ) + 4);
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_FIX_X);
	}

	static void PlaceRoad_NW(TileIndex tile)
	{
		_place_road_flag = (byte) (BitOps.b2i(ViewPort._tile_fract_coords.x >= 8) + 0);
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_FIX_Y);
	}

	static void PlaceRoad_Bridge(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_OR_Y);
	}


	public static void CcBuildRoadTunnel(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			Sound.SndPlayTileFx(Snd.SND_20_SPLAT_2, tile);
			ViewPort.ResetObjectToPlace();
		} else {
			ViewPort.SetRedErrorSquare(Global._build_tunnel_endtile);
		}
	}

	static void PlaceRoad_Tunnel(TileIndex tile)
	{
		Cmd.DoCommandP(tile, 0x200, 0, RoadGui::CcBuildRoadTunnel, Cmd.CMD_BUILD_TUNNEL | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_5016_CAN_T_BUILD_TUNNEL_HERE));
	}

	static final byte _roadbits_by_dir[] = {2,1,8,4};
	static void BuildRoadOutsideStation(TileIndex tile, int direction)
	{
		tile = tile.iadd( TileIndex.TileOffsByDir(direction) );
		// if there is a roadpiece just outside of the station entrance, build a connecting route
		if (tile.IsTileType( TileTypes.MP_STREET) && 0==(tile.getMap().m5 & 0x20)) {
			Cmd.DoCommandP(tile, _roadbits_by_dir[direction], 0, null, Cmd.CMD_BUILD_ROAD);
		}
	}

	public static void CcRoadDepot(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			Sound.SndPlayTileFx(Snd.SND_1F_SPLAT, tile);
			ViewPort.ResetObjectToPlace();
			BuildRoadOutsideStation(tile, p1);
		}
	}

	static void PlaceRoad_Depot(TileIndex tile)
	{
		Cmd.DoCommandP(tile, _road_depot_orientation, 0, RoadGui::CcRoadDepot, Cmd.CMD_BUILD_ROAD_DEPOT | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1807_CAN_T_BUILD_ROAD_VEHICLE));
	}

	static void PlaceRoad_BusStation(TileIndex tile)
	{
		Cmd.DoCommandP(tile, _road_station_picker_orientation, RoadStopType.RS_BUS.ordinal(), RoadGui::CcRoadDepot, Cmd.CMD_BUILD_ROAD_STOP | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1808_CAN_T_BUILD_BUS_STATION));
	}

	static void PlaceRoad_TruckStation(TileIndex tile)
	{
		Cmd.DoCommandP(tile, _road_station_picker_orientation, RoadStopType.RS_TRUCK.ordinal(), RoadGui::CcRoadDepot, Cmd.CMD_BUILD_ROAD_STOP | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1809_CAN_T_BUILD_TRUCK_STATION));
	}

	static void PlaceRoad_DemolishArea(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, 4);
	}

	//typedef void OnButtonClick(Window w);

	static void BuildRoadClick_NE(Window w)
	{
		Gui.HandlePlacePushButton(w, 3, Sprite.SPR_CURSOR_ROAD_NESW, 1, RoadGui::PlaceRoad_NE);
	}

	static void BuildRoadClick_NW(Window w)
	{
		Gui.HandlePlacePushButton(w, 4, Sprite.SPR_CURSOR_ROAD_NWSE, 1, RoadGui::PlaceRoad_NW);
	}


	static void BuildRoadClick_Demolish(Window w)
	{
		Gui.HandlePlacePushButton(w, 5, Sprites.ANIMCURSOR_DEMOLISH, 1, RoadGui::PlaceRoad_DemolishArea);
	}

	static void BuildRoadClick_Depot(Window w)
	{
		if (Global._game_mode == GameModes.GM_EDITOR) return;
		if (Gui.HandlePlacePushButton(w, 6, Sprite.SPR_CURSOR_ROAD_DEPOT, 1, RoadGui::PlaceRoad_Depot)) ShowRoadDepotPicker();
	}

	static void BuildRoadClick_BusStation(Window w)
	{
		if (Global._game_mode == GameModes.GM_EDITOR) return;
		if (Gui.HandlePlacePushButton(w, 7, Sprite.SPR_CURSOR_BUS_STATION, 1, RoadGui::PlaceRoad_BusStation)) ShowBusStationPicker();
	}

	static void BuildRoadClick_TruckStation(Window w)
	{
		if (Global._game_mode == GameModes.GM_EDITOR) return;
		if (Gui.HandlePlacePushButton(w, 8, Sprite.SPR_CURSOR_TRUCK_STATION, 1, RoadGui::PlaceRoad_TruckStation)) ShowTruckStationPicker();
	}

	static void BuildRoadClick_Bridge(Window w)
	{
		Gui.HandlePlacePushButton(w, 9, Sprite.SPR_CURSOR_BRIDGE, 1, RoadGui::PlaceRoad_Bridge);
	}

	static void BuildRoadClick_Tunnel(Window w)
	{
		Gui.HandlePlacePushButton(w, 10, Sprite.SPR_CURSOR_ROAD_TUNNEL, 3, RoadGui::PlaceRoad_Tunnel);
	}

	static void BuildRoadClick_Remove(Window w)
	{
		if (BitOps.HASBIT(w.disabled_state, 11)) return;
		w.SetWindowDirty();
		Sound.SndPlayFx(Snd.SND_15_BEEP);
		w.click_state = BitOps.RETTOGGLEBIT(w.click_state, 11);
		ViewPort.SetSelectionRed(BitOps.HASBIT(w.click_state, 11));
	}

	static void BuildRoadClick_Landscaping(Window w)
	{
		Terraform.ShowTerraformToolbar();
	}

	static final OnButtonClick _build_road_button_proc[] = {
		RoadGui::BuildRoadClick_NE,
		RoadGui::BuildRoadClick_NW,
		RoadGui::BuildRoadClick_Demolish,
		RoadGui::BuildRoadClick_Depot,
		RoadGui::BuildRoadClick_BusStation,
		RoadGui::BuildRoadClick_TruckStation,
		RoadGui::BuildRoadClick_Bridge,
		RoadGui::BuildRoadClick_Tunnel,
		RoadGui::BuildRoadClick_Remove,
		RoadGui::BuildRoadClick_Landscaping,
	};

	static void BuildRoadToolbWndProc(Window  w, WindowEvent  e)
	{
		switch (e.event) {
		case WE_PAINT:
			w.disabled_state &= ~(1 << 11);
			if (0==(w.click_state & ((1<<3)|(1<<4)))) {
				w.disabled_state |= (1 << 11);
				w.click_state &= ~(1<<11);
			}
			w.DrawWindowWidgets();
			break;

		case WE_CLICK: {
			if (e.widget >= 3) _build_road_button_proc[e.widget - 3].accept(w);
		}	break;

		case WE_KEYPRESS:
			switch (e.keycode) {
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
			ViewPort.MarkTileDirty(ViewPort._thd.pos.x, ViewPort._thd.pos.y); // redraw tile selection
			e.cont = false;
			break;

		case WE_PLACE_OBJ:
			_remove_button_clicked = (w.click_state & (1 << 11)) != 0;
			Global._place_proc.accept(e.tile);
			break;

		case WE_ABORT_PLACE_OBJ:
			w.UnclickWindowButtons();
			w.SetWindowDirty();

			w = Window.FindWindowById(Window.WC_BUS_STATION, 0);
			if (w != null) w.as_def_d().close = true;
			w = Window.FindWindowById(Window.WC_TRUCK_STATION, 0);
			if (w != null) w.as_def_d().close = true;
			w = Window.FindWindowById(Window.WC_BUILD_DEPOT, 0);
			if (w != null) w.as_def_d().close = true;
			break;

		case WE_PLACE_DRAG: {
			int sel_method;
			if (e.userdata == 1) {
				sel_method = ViewPort.VPM_FIX_X;
				_place_road_flag = (byte) ((_place_road_flag&~2) | ((e.pt.y&8)>>2));
			} else if (e.userdata == 2) {
				sel_method = ViewPort.VPM_FIX_Y;
				_place_road_flag = (byte) ((_place_road_flag&~2) | ((e.pt.x&8)>>2));
			} else if (e.userdata == 4) {
				sel_method = ViewPort.VPM_X_AND_Y;
			} else {
				sel_method = ViewPort.VPM_X_OR_Y;
			}

			ViewPort.VpSelectTilesWithMethod(e.pt.x, e.pt.y, sel_method);
			return;
		}

		case WE_PLACE_MOUSEUP:
			if (e.pt.x != -1) {
				TileIndex start_tile = e.starttile;
				TileIndex end_tile = e.tile;

				if (e.userdata == 0) {
					ViewPort.ResetObjectToPlace();
					Bridge.ShowBuildBridgeWindow(start_tile, end_tile, 0x80);
				} else if (e.userdata != 4) {
					Cmd.DoCommandP(end_tile, start_tile.getTile(), _place_road_flag, null/*CcPlaySound1D*/,
						_remove_button_clicked ?
						Cmd.CMD_REMOVE_LONG_ROAD | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1805_CAN_T_REMOVE_ROAD_FROM) :
						Cmd.CMD_BUILD_LONG_ROAD | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1804_CAN_T_BUILD_ROAD_HERE));
				} else {
					Cmd.DoCommandP(end_tile, start_tile.getTile(), _place_road_flag, null/*CcPlaySound10*/, Cmd.CMD_CLEAR_AREA | Cmd.CMD_MSG(Str.STR_00B5_CAN_T_CLEAR_THIS_AREA));
				}
			}
			break;

		case WE_PLACE_PRESIZE: {
			TileIndex tile = e.tile;

			Cmd.DoCommandByTile(tile, 0x200, 0, Cmd.DC_AUTO, Cmd.CMD_BUILD_TUNNEL);
			ViewPort.VpSetPresizeRange(tile, Global._build_tunnel_endtile==null?tile:Global._build_tunnel_endtile);
			break;
		}

		case WE_DESTROY:
			if (Global._patches.link_terraform_toolbar) Window.DeleteWindowById(Window.WC_SCEN_LAND_GEN, 0);
			break;
		default:
			break;
		}
	}

	static final Widget _build_road_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   227,     0,    13, Str.STR_1802_ROAD_CONSTRUCTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7,   228,   239,     0,    13, 0x0,                   Str.STR_STICKY_BUTTON),

	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,    21,    14,    35, Sprite.SPR_IMG_ROAD_NW,				Str.STR_180B_BUILD_ROAD_SECTION),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    22,    43,    14,    35, Sprite.SPR_IMG_ROAD_NE,				Str.STR_180B_BUILD_ROAD_SECTION),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    44,    65,    14,    35, Sprite.SPR_IMG_DYNAMITE,			Str.STR_018D_DEMOLISH_BUILDINGS_ETC),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    66,    87,    14,    35, Sprite.SPR_IMG_ROAD_DEPOT,		Str.STR_180C_BUILD_ROAD_VEHICLE_DEPOT),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    88,   109,    14,    35, Sprite.SPR_IMG_BUS_STATION,		Str.STR_180D_BUILD_BUS_STATION),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   110,   131,    14,    35, Sprite.SPR_IMG_TRUCK_BAY,			Str.STR_180E_BUILD_TRUCK_LOADING_BAY),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   132,   173,    14,    35, Sprite.SPR_IMG_BRIDGE,				Str.STR_180F_BUILD_ROAD_BRIDGE),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   174,   195,    14,    35, Sprite.SPR_IMG_ROAD_TUNNEL,		Str.STR_1810_BUILD_ROAD_TUNNEL),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   196,   217,    14,    35, Sprite.SPR_IMG_REMOVE, 				Str.STR_1811_TOGGLE_BUILD_REMOVE_FOR),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   218,   239,    14,    35, Sprite.SPR_IMG_LANDSCAPING, Str.STR_LANDSCAPING_TOOLBAR_TIP),
	};

	static final WindowDesc _build_road_desc = new WindowDesc(
		640-240, 22, 240, 36,
		Window.WC_BUILD_TOOLBAR,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
		_build_road_widgets,
		RoadGui::BuildRoadToolbWndProc
	);

	static void ShowBuildRoadToolbar()
	{
		if (PlayerID.getCurrent().isSpectator()) return;
		Window.DeleteWindowById(Window.WC_BUILD_TOOLBAR, 0);
		Window.AllocateWindowDesc(_build_road_desc);
		if (Global._patches.link_terraform_toolbar) Terraform.ShowTerraformToolbar();
	}

	static final Widget _build_road_scen_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,	Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   161,     0,    13, Str.STR_1802_ROAD_CONSTRUCTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7,   162,   173,     0,    13, 0x0,                   Str.STR_STICKY_BUTTON),

	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,     0,    21,    14,    35, 0x51D,			Str.STR_180B_BUILD_ROAD_SECTION),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,    22,    43,    14,    35, 0x51E,			Str.STR_180B_BUILD_ROAD_SECTION),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,    44,    65,    14,    35, 0x2BF,			Str.STR_018D_DEMOLISH_BUILDINGS_ETC),
	new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,				Str.STR_NULL),
	new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,				Str.STR_NULL),
	new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,				Str.STR_NULL),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,    66,   107,    14,    35, 0xA22,			Str.STR_180F_BUILD_ROAD_BRIDGE),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,   108,   129,    14,    35, 0x97D,			Str.STR_1810_BUILD_ROAD_TUNNEL),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,   130,   151,    14,    35, 0x2CA,			Str.STR_1811_TOGGLE_BUILD_REMOVE_FOR),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   152,   173,    14,    35, Sprite.SPR_IMG_LANDSCAPING, Str.STR_LANDSCAPING_TOOLBAR_TIP),

	};

	static final WindowDesc _build_road_scen_desc = new WindowDesc(
		-1, -1, 174, 36,
		Window.WC_SCEN_BUILD_ROAD,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
		_build_road_scen_widgets,
		RoadGui::BuildRoadToolbWndProc
	);

	static void ShowBuildRoadScenToolbar()
	{
		Window.AllocateWindowDescFront(_build_road_scen_desc, 0);
	}

	static void BuildRoadDepotWndProc(Window  w, WindowEvent  e)
	{
		switch (e.event) {
		case WE_PAINT:
			w.click_state = (1<<3) << _road_depot_orientation;
			w.DrawWindowWidgets();

			Road.DrawRoadDepotSprite(70, 17, 0);
			Road.DrawRoadDepotSprite(70, 69, 1);
			Road.DrawRoadDepotSprite( 2, 69, 2);
			Road.DrawRoadDepotSprite( 2, 17, 3);
			break;

		case WE_CLICK: {
			switch (e.widget) {
			case 3: case 4: case 5: case 6:
				_road_depot_orientation = (byte) (e.widget - 3);
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;
			}
		}	break;

		case WE_MOUSELOOP:
			if (w.as_def_d().close) w.DeleteWindow();
			break;

		case WE_DESTROY:
			if (!w.as_def_d().close) ViewPort.ResetObjectToPlace();
			break;
		default:
			break;
		}
	}

	static final Widget _build_road_depot_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   139,     0,    13, Str.STR_1806_ROAD_DEPOT_ORIENTATION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   139,    14,   121, 0x0,			Str.STR_NULL),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    17,    66, 0x0,			Str.STR_1813_SELECT_ROAD_VEHICLE_DEPOT),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    69,   118, 0x0,			Str.STR_1813_SELECT_ROAD_VEHICLE_DEPOT),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    69,   118, 0x0,			Str.STR_1813_SELECT_ROAD_VEHICLE_DEPOT),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    17,    66, 0x0,			Str.STR_1813_SELECT_ROAD_VEHICLE_DEPOT),
	};

	static final WindowDesc _build_road_depot_desc = new WindowDesc(
		-1,-1, 140, 122,
		Window.WC_BUILD_DEPOT,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_build_road_depot_widgets,
		RoadGui::BuildRoadDepotWndProc
	);

	static void ShowRoadDepotPicker()
	{
		Window.AllocateWindowDesc(_build_road_depot_desc);
	}

	static void RoadStationPickerWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			int image;

			if (w.as_def_d().close) return;

			w.click_state = ((1<<3) << _road_station_picker_orientation)	|
											 ((1<<7) << Gui._station_show_coverage);
			w.DrawWindowWidgets();

			if (Gui._station_show_coverage!=0) {
				int rad = Global._patches.modified_catchment ? Station.CA_TRUCK /* = CA_BUS */ : 4;
				ViewPort.SetTileSelectBigSize(-rad, -rad, 2 * rad, 2 * rad);
			} else
				ViewPort.SetTileSelectSize(1, 1);

			image = (w.getWindow_class() == Window.WC_BUS_STATION) ? 0x47 : 0x43;

			Station.StationPickerDrawSprite(103, 35, 0, image);
			Station.StationPickerDrawSprite(103, 85, 0, image+1);
			Station.StationPickerDrawSprite(35, 85, 0, image+2);
			Station.StationPickerDrawSprite(35, 35, 0, image+3);

			Gfx.DrawStringCentered(70, 120, Str.STR_3066_COVERAGE_AREA_HIGHLIGHT, 0);
			MiscGui.DrawStationCoverageAreaText(2, 146,
				((w.getWindow_class() == Window.WC_BUS_STATION) ? (1<< AcceptedCargo.CT_PASSENGERS) : ~(1<<AcceptedCargo.CT_PASSENGERS)),
				3);

		} break;

		case WE_CLICK: {
			switch (e.widget) {
			case 3: case 4: case 5: case 6:
				_road_station_picker_orientation = (byte) (e.widget - 3);
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;
			case 7: case 8:
				Gui._station_show_coverage = e.widget - 7;
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
		default:
			break;
		}
	}

	static final Widget _bus_station_picker_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,		Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   139,     0,    13, Str.STR_3042_BUS_STATION_ORIENTATION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   139,    14,   176, 0x0,					Str.STR_NULL),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    17,    66, 0x0,					Str.STR_3051_SELECT_BUS_STATION_ORIENTATION),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    69,   118, 0x0,					Str.STR_3051_SELECT_BUS_STATION_ORIENTATION),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    69,   118, 0x0,					Str.STR_3051_SELECT_BUS_STATION_ORIENTATION),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    17,    66, 0x0,					Str.STR_3051_SELECT_BUS_STATION_ORIENTATION),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    10,    69,   133,   144, Str.STR_02DB_OFF,Str.STR_3065_DON_T_HIGHLIGHT_COVERAGE),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    70,   129,   133,   144, Str.STR_02DA_ON,	Str.STR_3064_HIGHLIGHT_COVERAGE_AREA),
	};

	static final WindowDesc _bus_station_picker_desc = new WindowDesc(
		-1,-1, 140, 177,
		Window.WC_BUS_STATION,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_bus_station_picker_widgets,
		RoadGui::RoadStationPickerWndProc
	);

	static void ShowBusStationPicker()
	{
		Window.AllocateWindowDesc(_bus_station_picker_desc);
	}

	static final Widget _truck_station_picker_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,		Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   139,     0,    13, Str.STR_3043_TRUCK_STATION_ORIENT, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   139,    14,   176, 0x0,					Str.STR_NULL),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    17,    66, 0x0,					Str.STR_3052_SELECT_TRUCK_LOADING_BAY),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    69,   118, 0x0,					Str.STR_3052_SELECT_TRUCK_LOADING_BAY),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    69,   118, 0x0,					Str.STR_3052_SELECT_TRUCK_LOADING_BAY),
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    17,    66, 0x0,					Str.STR_3052_SELECT_TRUCK_LOADING_BAY),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    10,    69,   133,   144, Str.STR_02DB_OFF, Str.STR_3065_DON_T_HIGHLIGHT_COVERAGE),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    70,   129,   133,   144, Str.STR_02DA_ON,	Str.STR_3064_HIGHLIGHT_COVERAGE_AREA),
	};

	static final WindowDesc _truck_station_picker_desc = new WindowDesc(
		-1,-1, 140, 177,
		Window.WC_TRUCK_STATION,Window.WC_BUILD_TOOLBAR,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_truck_station_picker_widgets,
		RoadGui::RoadStationPickerWndProc
	);

	static void ShowTruckStationPicker()
	{
		Window.AllocateWindowDesc(_truck_station_picker_desc);
	}

	public static void InitializeRoadGui()
	{
		_road_depot_orientation = 3;
		_road_station_picker_orientation = 3;
	}
	
	
}
