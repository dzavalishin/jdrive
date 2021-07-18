package game;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Terraform {


	void CcTerraform(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			// TODO SndPlayTileFx(SND_1F_SPLAT, tile);
		} else {
			SetRedErrorSquare(_terraform_err_tile);
		}
	}

	static void GenericRaiseLowerLand(TileIndex tile, int mode)
	{
		if (mode!=0) {
			DoCommandP(tile, 8, (int)mode, CcTerraform, CMD_TERRAFORM_LAND | CMD_AUTO | CMD_MSG(STR_0808_CAN_T_RAISE_LAND_HERE));
		} else {
			DoCommandP(tile, 8, (int)mode, CcTerraform, CMD_TERRAFORM_LAND | CMD_AUTO | CMD_MSG(STR_0809_CAN_T_LOWER_LAND_HERE));
		}
	}

	/** Scenario editor command that generates desert areas */
	static void GenerateDesertArea(TileIndex end, TileIndex start)
	{
		int size_x, size_y;
		int sx = start.TileX();
		int sy = start.TileY();
		int ex = end.TileX();
		int ey = end.TileY();

		if (Global._game_mode != GameModes.GM_EDITOR) return;

		if (ex < sx) intswap(ex, sx);
		if (ey < sy) intswap(ey, sy);
		size_x = (ex - sx) + 1;
		size_y = (ey - sy) + 1;

		_generating_world = true;
		BEGIN_TILE_LOOP(tile, size_x, size_y, TileXY(sx, sy)) {
			if (GetTileType(tile) != MP_WATER) {
				SetMapExtraBits(tile, (_ctrl_pressed) ? 0 : 1);
				DoCommandP(tile, 0, 0, null, CMD_LANDSCAPE_CLEAR);
				MarkTileDirtyByTile(tile);
			}
		} END_TILE_LOOP(tile, size_x, size_y, 0);
		_generating_world = false;
	}

	/** Scenario editor command that generates desert areas */
	static void GenerateRockyArea(TileIndex end, TileIndex start)
	{
		int size_x, size_y;
		boolean success = false;
		int sx = start.TileX();
		int sy = start.TileY();
		int ex = end.TileX();
		int ey = end.TileY();

		if (Global._game_mode != GameModes.GM_EDITOR) return;

		if (ex < sx) intswap(ex, sx);
		if (ey < sy) intswap(ey, sy);
		size_x = (ex - sx) + 1;
		size_y = (ey - sy) + 1;

		BEGIN_TILE_LOOP(tile, size_x, size_y, TileXY(sx, sy)) {
			if (IsTileType(tile, MP_CLEAR) || IsTileType(tile, MP_TREES)) {
				ModifyTile(tile, MP_SETTYPE(MP_CLEAR) | MP_MAP5, (_m[tile].m5 & ~0x1C) | 0xB);
				success = true;
			}
		} END_TILE_LOOP(tile, size_x, size_y, 0);

		// TODO if (success) SndPlayTileFx(SND_1F_SPLAT, end);
	}

	/**
	 * A central place to handle all X_AND_Y dragged GUI functions.
	 * @param we @WindowEvent variable holding in its higher bits (excluding the lower
	 * 4, since that defined the X_Y drag) the type of action to be performed
	 * @return Returns true if the action was found and handled, and false otherwise. This
	 * allows for additional implements that are more local. For example X_Y drag
	 * of convertrail which belongs in rail_gui.c and not terraform_gui.c
	 **/
	boolean GUIPlaceProcDragXY(final WindowEvent we)
	{
		TileIndex start_tile = we.starttile;
		TileIndex end_tile = we.tile;

		switch (we.userdata >> 4) {
		case GUI_PlaceProc_DemolishArea >> 4:
			DoCommandP(end_tile, start_tile, 0, CcPlaySound10, CMD_CLEAR_AREA | CMD_MSG(STR_00B5_CAN_T_CLEAR_THIS_AREA));
			break;
		case GUI_PlaceProc_LevelArea >> 4:
			DoCommandP(end_tile, start_tile, 0, CcPlaySound10, CMD_LEVEL_LAND | CMD_AUTO);
			break;
		case GUI_PlaceProc_RockyArea >> 4:
			GenerateRockyArea(end_tile, start_tile);
			break;
		case GUI_PlaceProc_DesertArea >> 4:
			GenerateDesertArea(end_tile, start_tile);
			break;
		case GUI_PlaceProc_WaterArea >> 4:
			DoCommandP(end_tile, start_tile, 0, CcBuildCanal, CMD_BUILD_CANAL | CMD_AUTO | CMD_MSG(STR_CANT_BUILD_CANALS));
			break;
		default: return false;
		}

		return true;
	}

	//typedef void OnButtonClick(Window w);

	static final int _terraform_keycodes[] = {
		'Q',
		'W',
		'E',
		'D',
		'U',
		'I',
		'O',
	};

	void PlaceProc_DemolishArea(TileIndex tile)
	{
		VpStartPlaceSizing(tile, VPM_X_AND_Y | GUI_PlaceProc_DemolishArea);
	}

	void PlaceProc_RaiseLand(TileIndex tile)
	{
		GenericRaiseLowerLand(tile, 1);
	}

	void PlaceProc_LowerLand(TileIndex tile)
	{
		GenericRaiseLowerLand(tile, 0);
	}

	void PlaceProc_LevelLand(TileIndex tile)
	{
		VpStartPlaceSizing(tile, VPM_X_AND_Y | GUI_PlaceProc_LevelArea);
	}

	static void PlaceProc_PlantTree(TileIndex tile) {}

	static void TerraformClick_Lower(Window w)
	{
		HandlePlacePushButton(w, 4, ANIMCURSOR_LOWERLAND, 2, PlaceProc_LowerLand);
	}

	static void TerraformClick_Raise(Window w)
	{
		HandlePlacePushButton(w, 5, ANIMCURSOR_RAISELAND, 2, PlaceProc_RaiseLand);
	}

	static void TerraformClick_Level(Window w)
	{
		HandlePlacePushButton(w, 6, SPR_CURSOR_LEVEL_LAND, 2, PlaceProc_LevelLand);
	}

	static void TerraformClick_Dynamite(Window w)
	{
		HandlePlacePushButton(w, 7, ANIMCURSOR_DEMOLISH , 1, PlaceProc_DemolishArea);
	}

	static void TerraformClick_BuyLand(Window w)
	{
		HandlePlacePushButton(w, 8, SPR_CURSOR_BUY_LAND, 1, PlaceProc_BuyLand);
	}

	static void TerraformClick_Trees(Window w)
	{
		if (HandlePlacePushButton(w, 9, SPR_CURSOR_MOUSE, 1, PlaceProc_PlantTree)) ShowBuildTreesToolbar();
	}

	static void TerraformClick_PlaceSign(Window w)
	{
		HandlePlacePushButton(w, 10, SPR_CURSOR_SIGN, 1, PlaceProc_Sign);
	}

	static final Consumer <Window>  _terraform_button_proc[] = {
		Terraform::TerraformClick_Lower,
		Terraform::TerraformClick_Raise,
		Terraform::TerraformClick_Level,
		Terraform::TerraformClick_Dynamite,
		Terraform::TerraformClick_BuyLand,
		Terraform::TerraformClick_Trees,
		Terraform::TerraformClick_PlaceSign,
	};

	static void TerraformToolbWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT:
			DrawWindowWidgets(w);
			break;

		case WE_CLICK:
			if (e.widget >= 4) _terraform_button_proc[e.click.widget - 4](w);
			break;

		case WE_KEYPRESS: {
			uint i;

			for (i = 0; i != _terraform_keycodes.length; i++) {
				if (e.keycode == _terraform_keycodes[i]) {
					e.cont = false;
					_terraform_button_proc[i](w);
					break;
				}
			}
			break;
		}

		case WE_PLACE_OBJ:
			_place_proc(e.tile);
			return;

		case WE_PLACE_DRAG:
			VpSelectTilesWithMethod(e.place.pt.x, e.place.pt.y, e.place.userdata & 0xF);
			break;

		case WE_PLACE_MOUSEUP:
			if (e.click.pt.x != -1) {
				if ((e.place.userdata & 0xF) == VPM_X_AND_Y) // dragged actions
					GUIPlaceProcDragXY(e);
			}
			break;

		case WE_ABORT_PLACE_OBJ:
			UnclickWindowButtons(w);
			SetWindowDirty(w);

			w = FindWindowById(WC_BUILD_STATION, 0);
			if (w != null) WP(w,def_d).close=true;
			w = FindWindowById(WC_BUILD_DEPOT, 0);
			if (w != null) WP(w,def_d).close=true;
			break;

		case WE_PLACE_PRESIZE: {
		} break;
		}
	}

	static final Widget _terraform_widgets[] = {
	new Widget(  WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,   0,  10,   0,  13, STR_00C5,                STR_018B_CLOSE_WINDOW),
	new Widget(   WWT_CAPTION,   Window.RESIZE_NONE,     7,  11, 145,   0,  13, STR_LANDSCAPING_TOOLBAR, STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget( WWT_STICKYBOX,   Window.RESIZE_NONE,     7, 146, 157,   0,  13, STR_NULL,                STR_STICKY_BUTTON),

	new Widget(     WWT_PANEL,   Window.RESIZE_NONE,     7,  66,  69,  14,  35, STR_NULL,                STR_NULL),
	new Widget(     WWT_PANEL,   Window.RESIZE_NONE,     7,   0,  21,  14,  35, SPR_IMG_TERRAFORM_DOWN,  STR_018E_LOWER_A_CORNER_OF_LAND),
	new Widget(     WWT_PANEL,   Window.RESIZE_NONE,     7,  22,  43,  14,  35, SPR_IMG_TERRAFORM_UP,    STR_018F_RAISE_A_CORNER_OF_LAND),
	new Widget(     WWT_PANEL,   Window.RESIZE_NONE,     7,  44,  65,  14,  35, SPR_IMG_LEVEL_LAND,      STR_LEVEL_LAND_TOOLTIP),
	new Widget(     WWT_PANEL,   Window.RESIZE_NONE,     7,  70,  91,  14,  35, SPR_IMG_DYNAMITE,        STR_018D_DEMOLISH_BUILDINGS_ETC),
	new Widget(     WWT_PANEL,   Window.RESIZE_NONE,     7,  92, 113,  14,  35, SPR_IMG_BUY_LAND,        STR_0329_PURCHASE_LAND_FOR_FUTURE),
	new Widget(     WWT_PANEL,   Window.RESIZE_NONE,     7, 114, 135,  14,  35, SPR_IMG_PLANTTREES,      STR_0185_PLANT_TREES_PLACE_SIGNS),
	new Widget(     WWT_PANEL,   Window.RESIZE_NONE,     7, 136, 157,  14,  35, SPR_IMG_PLACE_SIGN,      STR_0289_PLACE_SIGN),

	new Widget(    WIDGETS_END),
	};

	static final WindowDesc _terraform_desc = new WindowDesc(
		640-157, 22+36, 158, 36,
		Window.WC_SCEN_LAND_GEN,0,
		WindowDesc.WDF_STD_TOOLTIPS | 
		WindowDesc.WDF_STD_BTN | 
		WindowDesc.WDF_DEF_WIDGET | 
		WindowDesc.WDF_STICKY_BUTTON,
		_terraform_widgets,
		Terraform::TerraformToolbWndProc
	);

	void ShowTerraformToolbar()
	{
		if (Global._current_player.id == Owner.OWNER_SPECTATOR) return;
		AllocateWindowDescFront(_terraform_desc, 0);
	}
	
	
}
