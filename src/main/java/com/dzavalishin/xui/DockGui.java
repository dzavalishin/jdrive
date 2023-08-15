package com.dzavalishin.xui;

import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.Terraform;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.WaterCmd;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ifaces.OnButtonClick;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.Sound;

public class DockGui 
{

	//static void ShowBuildDockStationPicker();
	//static void ShowBuildDocksDepotPicker();

	static int _ship_depot_direction;

	public static void CcBuildDocks(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			Sound.SndPlayTileFx(Snd.SND_02_SPLAT, tile);
			ViewPort.ResetObjectToPlace();
		}
	}

	public static void CcBuildCanal(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) Sound.SndPlayTileFx(Snd.SND_02_SPLAT, tile);
	}


	static void PlaceDocks_Dock(TileIndex tile)
	{
		Cmd.DoCommandP(tile, 0, 0, DockGui::CcBuildDocks, Cmd.CMD_BUILD_DOCK | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_9802_CAN_T_BUILD_DOCK_HERE));
	}

	static void PlaceDocks_Depot(TileIndex tile)
	{
		Cmd.DoCommandP(tile, _ship_depot_direction, 0, DockGui::CcBuildDocks, Cmd.CMD_BUILD_SHIP_DEPOT | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_3802_CAN_T_BUILD_SHIP_DEPOT));
	}

	static void PlaceDocks_Buoy(TileIndex tile)
	{
		Cmd.DoCommandP(tile, 0, 0, DockGui::CcBuildDocks, Cmd.CMD_BUILD_BUOY | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_9835_CAN_T_POSITION_BUOY_HERE));
	}

	static void PlaceDocks_DemolishArea(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_AND_Y | Gui.GUI_PlaceProc_DemolishArea);
	}

	static void PlaceDocks_BuildCanal(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_OR_Y);
	}

	static void PlaceDocks_BuildLock(TileIndex tile)
	{
		Cmd.DoCommandP(tile, 0, 0, DockGui::CcBuildDocks, Cmd.CMD_BUILD_LOCK | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_CANT_BUILD_LOCKS));
	}


	static void BuildDocksClick_Canal(Window w)
	{
		Gui.HandlePlacePushButton(w, 3, Sprite.SPR_CURSOR_CANAL, 1, DockGui::PlaceDocks_BuildCanal);
	}

	static void BuildDocksClick_Lock(Window w)
	{
		Gui.HandlePlacePushButton(w, 4, Sprite.SPR_CURSOR_LOCK, 1, DockGui::PlaceDocks_BuildLock);
	}

	static void BuildDocksClick_Demolish(Window w)
	{
		Gui.HandlePlacePushButton(w, 6, Sprite.ANIMCURSOR_DEMOLISH, 1, DockGui::PlaceDocks_DemolishArea);
	}

	static void BuildDocksClick_Depot(Window w)
	{
		if (Gui.HandlePlacePushButton(w, 7, Sprite.SPR_CURSOR_SHIP_DEPOT, 1, DockGui::PlaceDocks_Depot)) ShowBuildDocksDepotPicker();
	}

	static void BuildDocksClick_Dock(Window w)
	{

		if (Gui.HandlePlacePushButton(w, 8, Sprite.SPR_CURSOR_DOCK, 3, DockGui::PlaceDocks_Dock)) ShowBuildDockStationPicker();
	}

	static void BuildDocksClick_Buoy(Window w)
	{
		Gui.HandlePlacePushButton(w, 9, Sprite.SPR_CURSOR_BOUY, 1, DockGui::PlaceDocks_Buoy);
	}

	static void BuildDocksClick_Landscaping(Window w)
	{
		Terraform.ShowTerraformToolbar();
	}

	//typedef void OnButtonClick(Window w);
	static final OnButtonClick[] _build_docks_button_proc = {
			DockGui::BuildDocksClick_Canal,
			DockGui::BuildDocksClick_Lock,
			null,
			DockGui::BuildDocksClick_Demolish,
			DockGui::BuildDocksClick_Depot,
			DockGui::BuildDocksClick_Dock,
			DockGui::BuildDocksClick_Buoy,
			DockGui::BuildDocksClick_Landscaping,
	};

	static void BuildDocksToolbWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT:
			w.DrawWindowWidgets();
			break;

		case WE_CLICK:
			if (e.widget - 3 >= 0 && e.widget != 5) _build_docks_button_proc[e.widget - 3].accept(w);
			break;

		case WE_KEYPRESS:
			switch (e.keycode) {
			case '1': BuildDocksClick_Canal(w); break;
			case '2': BuildDocksClick_Lock(w); break;
			case '3': BuildDocksClick_Demolish(w); break;
			case '4': BuildDocksClick_Depot(w); break;
			case '5': BuildDocksClick_Dock(w); break;
			case '6': BuildDocksClick_Buoy(w); break;
			case 'l': BuildDocksClick_Landscaping(w); break;
			default:  return;
			}
			break;

		case WE_PLACE_OBJ:
			Global._place_proc.accept(e.tile);
			break;

		case WE_PLACE_DRAG: {
			ViewPort.VpSelectTilesWithMethod(e.pt.x, e.pt.y, e.userdata);
			return;
		}

		case WE_PLACE_MOUSEUP:
			if (e.pt.x != -1) {
				if ((e.userdata & 0xF) == ViewPort.VPM_X_AND_Y) { // dragged actions
					Terraform.GUIPlaceProcDragXY(e);
				} else if (e.userdata == ViewPort.VPM_X_OR_Y) {
					Cmd.DoCommandP(e.tile, e.starttile.getTile(), 0, DockGui::CcBuildCanal, Cmd.CMD_BUILD_CANAL | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_CANT_BUILD_CANALS));
				}
			}
			break;

		case WE_ABORT_PLACE_OBJ:
			w.UnclickWindowButtons();
			w.SetWindowDirty();

			w = Window.FindWindowById(Window.WC_BUILD_STATION, 0);
			if (w != null) w.as_def_d().close = true;

			w = Window.FindWindowById(Window.WC_BUILD_DEPOT, 0);
			if (w != null) w.as_def_d().close = true;
			break;

		case WE_PLACE_PRESIZE: {
			TileIndex tile_from;
			TileIndex tile_to;

			tile_from = tile_to = e.tile;
			switch ( tile_from.GetTileSlope(null)) {
			case  3: tile_to = tile_to.iadd(-1,  0); break;
			case  6: tile_to = tile_to.iadd( 0, -1); break;
			case  9: tile_to = tile_to.iadd( 0,  1); break;
			case 12: tile_to = tile_to.iadd( 1,  0); break;
			}
			ViewPort.VpSetPresizeRange(tile_from, tile_to);
		} break;

		case WE_DESTROY:
			if (Global._patches.link_terraform_toolbar) Window.DeleteWindowById(Window.WC_SCEN_LAND_GEN, 0);
			break;
		default:
			break;
		}
	}

	static final Widget _build_docks_toolb_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,										Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   145,     0,    13, Str.STR_9801_DOCK_CONSTRUCTION,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7,   146,   157,     0,    13, 0x0,                         Str.STR_STICKY_BUTTON),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,    21,    14,    35, Sprite.SPR_IMG_BUILD_CANAL,					Str.STR_BUILD_CANALS_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    22,    43,    14,    35, Sprite.SPR_IMG_BUILD_LOCK,					Str.STR_BUILD_LOCKS_TIP),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    44,    47,    14,    35, 0x0,													Str.STR_NULL),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    48,    69,    14,    35, 703,													Str.STR_018D_DEMOLISH_BUILDINGS_ETC),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    70,    91,    14,    35, 748,													Str.STR_981E_BUILD_SHIP_DEPOT_FOR_BUILDING),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    92,   113,    14,    35, 746,													Str.STR_981D_BUILD_SHIP_DOCK),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   114,   135,    14,    35, 693,													Str.STR_9834_POSITION_BUOY_WHICH_CAN),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   136,   157,    14,    35, Sprite.SPR_IMG_LANDSCAPING,				Str.STR_LANDSCAPING_TOOLBAR_TIP),
	};

	static final WindowDesc _build_docks_toolbar_desc = new WindowDesc(
			640-158, 22, 158, 36,
			Window.WC_BUILD_TOOLBAR,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
			_build_docks_toolb_widgets,
			DockGui::BuildDocksToolbWndProc
			);

	static void ShowBuildDocksToolbar()
	{
		if (PlayerID.getCurrent().isSpectator()) return;
		Window.DeleteWindowById(Window.WC_BUILD_TOOLBAR, 0);
		Window.AllocateWindowDesc(_build_docks_toolbar_desc);
		if (Global._patches.link_terraform_toolbar) 
			Terraform.ShowTerraformToolbar();
	}

	static void BuildDockStationWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int rad;

			if (w.as_def_d().close) return;
			w.click_state = (1<<3) << Gui._station_show_coverage;
			w.DrawWindowWidgets();

			rad = (Global._patches.modified_catchment) ? Station.CA_DOCK : 4;

			if (Gui._station_show_coverage!=0) {
				ViewPort.SetTileSelectBigSize(-rad, -rad, 2 * rad, 2 * rad);
			} else {
				ViewPort.SetTileSelectBigSize(0, 0, 0, 0);
			}

			Gfx.DrawStringCentered(74, 17, Str.STR_3066_COVERAGE_AREA_HIGHLIGHT, 0);
			MiscGui.DrawStationCoverageAreaText(4, 50, -1, rad);
			break;
		}

		case WE_CLICK:
			switch (e.widget) {
			case 3:
			case 4:
				Gui._station_show_coverage = e.widget - 3;
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;
			}
			break;

		case WE_MOUSELOOP:
			if (w.as_def_d().close) {
				w.DeleteWindow();
				return;
			}

			MiscGui.CheckRedrawStationCoverage(w);
			break;

		case WE_DESTROY:
			if (!w.as_def_d().close) ViewPort.ResetObjectToPlace();
			break;
		default:
			break;
		}
	}

	static final Widget _build_dock_station_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,			Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   147,     0,    13, Str.STR_3068_DOCK,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   147,    14,    74, 0x0,						Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    14,    73,    30,    40, Str.STR_02DB_OFF,	Str.STR_3065_DON_T_HIGHLIGHT_COVERAGE),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    74,   133,    30,    40, Str.STR_02DA_ON,		Str.STR_3064_HIGHLIGHT_COVERAGE_AREA),
	};

	static final WindowDesc _build_dock_station_desc = new WindowDesc(
			-1, -1, 148, 75,
			Window.WC_BUILD_STATION,Window.WC_BUILD_TOOLBAR,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_dock_station_widgets,
			DockGui::BuildDockStationWndProc
			);

	static void ShowBuildDockStationPicker()
	{
		Window.AllocateWindowDesc(_build_dock_station_desc);
	}

	static void UpdateDocksDirection()
	{
		if (_ship_depot_direction != 0) {
			ViewPort.SetTileSelectSize(1, 2);
		} else {
			ViewPort.SetTileSelectSize(2, 1);
		}
	}

	static void BuildDocksDepotWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT:
			w.click_state = (1<<3) << _ship_depot_direction;
			w.DrawWindowWidgets();

			WaterCmd.DrawShipDepotSprite(67, 35, 0);
			WaterCmd.DrawShipDepotSprite(35, 51, 1);
			WaterCmd.DrawShipDepotSprite(135, 35, 2);
			WaterCmd.DrawShipDepotSprite(167, 51, 3);
			return;

		case WE_CLICK: {
			switch (e.widget) {
			case 3:
			case 4:
				_ship_depot_direction =  (e.widget - 3);
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				UpdateDocksDirection();
				w.SetWindowDirty();
				break;
			}
		} break;

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

	static final Widget _build_docks_depot_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,												Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   203,     0,    13, Str.STR_3800_SHIP_DEPOT_ORIENTATION,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   203,    14,    85, 0x0,															Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,   100,    17,    82, 0x0,															Str.STR_3803_SELECT_SHIP_DEPOT_ORIENTATION),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   103,   200,    17,    82, 0x0,															Str.STR_3803_SELECT_SHIP_DEPOT_ORIENTATION),
	};

	static final WindowDesc _build_docks_depot_desc = new WindowDesc(
			-1, -1, 204, 86,
			Window.WC_BUILD_DEPOT,Window.WC_BUILD_TOOLBAR,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_docks_depot_widgets,
			DockGui::BuildDocksDepotWndProc
			);


	static void ShowBuildDocksDepotPicker()
	{
		Window.AllocateWindowDesc(_build_docks_depot_desc);
		UpdateDocksDirection();
	}


	public static void InitializeDockGui()
	{
		_ship_depot_direction = 0;
	}

}
