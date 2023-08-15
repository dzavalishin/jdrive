package com.dzavalishin.game;

import java.util.function.Consumer;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.Sound;
import com.dzavalishin.xui.DockGui;
import com.dzavalishin.xui.Gui;
import com.dzavalishin.xui.MiscGui;
import com.dzavalishin.xui.RailGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Widget;
import com.dzavalishin.xui.Window;
import com.dzavalishin.xui.WindowDesc;
import com.dzavalishin.xui.WindowEvent;

public class Terraform {


	public static void CcTerraform(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			Sound.SndPlayTileFx(Snd.SND_1F_SPLAT, tile);
		} else {
			ViewPort.SetRedErrorSquare(Global._terraform_err_tile);
		}
	}

	static void GenericRaiseLowerLand(TileIndex tile, int mode)
	{
		if (mode!=0) {
			Cmd.DoCommandP(tile, 8, mode, Terraform::CcTerraform, Cmd.CMD_TERRAFORM_LAND | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_0808_CAN_T_RAISE_LAND_HERE));
		} else {
			Cmd.DoCommandP(tile, 8, mode, Terraform::CcTerraform, Cmd.CMD_TERRAFORM_LAND | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_0809_CAN_T_LOWER_LAND_HERE));
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

		if (ex < sx) { int t = sx; sx = ex; ex = t; } // intswap(ex, sx);
		if (ey < sy) { int t = sy; sy = ey; ey = t; } // intswap(ey, sy);
		size_x = (ex - sx) + 1;
		size_y = (ey - sy) + 1;

		Global._generating_world = true;
		//BEGIN_TILE_LOOP(tile, size_x, size_y, TileXY(sx, sy)) 
		TileIndex.forAll( size_x, size_y, TileIndex.TileXY(sx, sy), (tile) ->
		{
			if (tile.GetTileType() != TileTypes.MP_WATER) {
				tile.SetMapExtraBits( (Global._ctrl_pressed) ? 0 : TileInfo.EXTRABITS_DESERT);
				Cmd.DoCommandP(tile, 0, 0, null, Cmd.CMD_LANDSCAPE_CLEAR);
				tile.MarkTileDirtyByTile();
			}
			return false;
		}); //END_TILE_LOOP(tile, size_x, size_y, 0);
		Global._generating_world = false;
	}

	/** tor command that generates desert areas */
	static void GenerateRockyArea(TileIndex end, TileIndex start)
	{
		int size_x, size_y;
		boolean [] success = { false };
		int sx = start.TileX();
		int sy = start.TileY();
		int ex = end.TileX();
		int ey = end.TileY();

		if (Global._game_mode != GameModes.GM_EDITOR) return;

		if (ex < sx) { int t = sx; sx = ex; ex = t; } // intswap(ex, sx);
		if (ey < sy) { int t = sy; sy = ey; ey = t; } // intswap(ey, sy);
		size_x = (ex - sx) + 1;
		size_y = (ey - sy) + 1;

		//BEGIN_TILE_LOOP(tile, size_x, size_y, TileXY(sx, sy)) 
		TileIndex.forAll( size_x, size_y, TileIndex.TileXY(sx, sy), (tile) ->
		{
			if (tile.IsTileType(TileTypes.MP_CLEAR) || tile.IsTileType( TileTypes.MP_TREES)) {
				Landscape.ModifyTile(tile, TileTypes.MP_CLEAR,
						//TileTypes.MP_SETTYPE(TileTypes.MP_CLEAR) | 
						TileTypes.MP_MAP5, (tile.getMap().m5 & ~0x1C) | 0xB);
				success[0] = true;
			}
			return false;
		}); // END_TILE_LOOP(tile, size_x, size_y, 0);

		if (success[0]) Sound.SndPlayTileFx(Snd.SND_1F_SPLAT, end);
	}

	/**
	 * A central place to handle all X_AND_Y dragged GUI functions.
	 * @param we @WindowEvent variable holding in its higher bits (excluding the lower
	 * 4, since that defined the X_Y drag) the type of action to be performed
	 * @return Returns true if the action was found and handled, and false otherwise. This
	 * allows for additional implements that are more local. For example X_Y drag
	 * of convertrail which belongs in rail_gui.c and not terraform_gui.c
	 **/
	public static boolean GUIPlaceProcDragXY(final WindowEvent we)
	{
		TileIndex start_tile = we.starttile;
		TileIndex end_tile = we.tile;

		switch (we.userdata >> 4) {
		case Gui.GUI_PlaceProc_DemolishArea >> 4:
			Cmd.DoCommandP(end_tile, start_tile.tile, 0, Gui::CcPlaySound10, Cmd.CMD_CLEAR_AREA | Cmd.CMD_MSG(Str.STR_00B5_CAN_T_CLEAR_THIS_AREA));
			break;
		case Gui.GUI_PlaceProc_LevelArea >> 4:
			Cmd.DoCommandP(end_tile, start_tile.tile, 0, Gui::CcPlaySound10, Cmd.CMD_LEVEL_LAND | Cmd.CMD_AUTO);
			break;
		case Gui.GUI_PlaceProc_RockyArea >> 4:
			GenerateRockyArea(end_tile, start_tile);
			break;
		case Gui.GUI_PlaceProc_DesertArea >> 4:
			GenerateDesertArea(end_tile, start_tile);
			break;
		case Gui.GUI_PlaceProc_WaterArea >> 4:
			Cmd.DoCommandP(end_tile, start_tile.tile, 0, DockGui::CcBuildCanal, Cmd.CMD_BUILD_CANAL | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_CANT_BUILD_CANALS));
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

	public static void PlaceProc_DemolishArea(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_AND_Y | Gui.GUI_PlaceProc_DemolishArea);
	}

	public static void PlaceProc_RaiseLand(TileIndex tile)
	{
		GenericRaiseLowerLand(tile, 1);
	}

	public static void PlaceProc_LowerLand(TileIndex tile)
	{
		GenericRaiseLowerLand(tile, 0);
	}

	public static void PlaceProc_LevelLand(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_AND_Y | Gui.GUI_PlaceProc_LevelArea);
	}

	public static void PlaceProc_PlantTree(TileIndex tile) {}

	public static void TerraformClick_Lower(Window w)
	{
		Gui.HandlePlacePushButton(w, 4, Sprite.ANIMCURSOR_LOWERLAND, 2, Terraform::PlaceProc_LowerLand);
	}

	public static void TerraformClick_Raise(Window w)
	{
		Gui.HandlePlacePushButton(w, 5, Sprite.ANIMCURSOR_RAISELAND, 2, Terraform::PlaceProc_RaiseLand);
	}

	public static void TerraformClick_Level(Window w)
	{
		Gui.HandlePlacePushButton(w, 6, Sprite.SPR_CURSOR_LEVEL_LAND, 2, Terraform::PlaceProc_LevelLand);
	}

	public static void TerraformClick_Dynamite(Window w)
	{
		Gui.HandlePlacePushButton(w, 7, Sprite.ANIMCURSOR_DEMOLISH , 1, Terraform::PlaceProc_DemolishArea);
	}

	public static void TerraformClick_BuyLand(Window w)
	{
		Gui.HandlePlacePushButton(w, 8, Sprite.SPR_CURSOR_BUY_LAND, 1, RailGui::PlaceProc_BuyLand);
	}

	public static void TerraformClick_Trees(Window w)
	{
		if (Gui.HandlePlacePushButton(w, 9, Sprite.SPR_CURSOR_MOUSE, 1, Terraform::PlaceProc_PlantTree)) MiscGui.ShowBuildTreesToolbar();
	}

	public static void TerraformClick_PlaceSign(Window w)
	{
		Gui.HandlePlacePushButton(w, 10, Sprite.SPR_CURSOR_SIGN, 1, SignStruct::PlaceProc_Sign);
	}

	static final ButtonProc  _terraform_button_proc[] = {
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
			w.DrawWindowWidgets();
			break;

		case WE_CLICK:
			if (e.widget >= 4) _terraform_button_proc[e.widget - 4].accept(w);
			break;

		case WE_KEYPRESS: {
			int i;

			for (i = 0; i != _terraform_keycodes.length; i++) {
				if (e.keycode == _terraform_keycodes[i]) {
					e.cont = false;
					_terraform_button_proc[i].accept(w);
					break;
				}
			}
			break;
		}

		case WE_PLACE_OBJ:
			Global._place_proc.accept(e.tile);
			return;

		case WE_PLACE_DRAG:
			ViewPort.VpSelectTilesWithMethod(e.pt.x, e.pt.y, e.userdata & 0xF);
			break;

		case WE_PLACE_MOUSEUP:
			if (e.pt.x != -1) {
				if ((e.userdata & 0xF) == ViewPort.VPM_X_AND_Y) // dragged actions
					GUIPlaceProcDragXY(e);
			}
			break;

		case WE_ABORT_PLACE_OBJ:
			w.UnclickWindowButtons();
			w.SetWindowDirty();

			w = Window.FindWindowById(Window.WC_BUILD_STATION, 0);
			if (w != null) w.as_def_d().close=true;
			w = Window.FindWindowById(Window.WC_BUILD_DEPOT, 0);
			if (w != null) w.as_def_d().close=true;
			break;

		case WE_PLACE_PRESIZE: 
			break;
			
		default:
			break;
		}
	}

	static final Widget _terraform_widgets[] = {
	new Widget(  Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,   0,  10,   0,  13, Str.STR_00C5,                Str.STR_018B_CLOSE_WINDOW),
	new Widget(   Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,  11, 145,   0,  13, Str.STR_LANDSCAPING_TOOLBAR, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget( Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7, 146, 157,   0,  13, Str.STR_NULL,                Str.STR_STICKY_BUTTON),

	new Widget(     Window.WWT_PANEL,   Window.RESIZE_NONE,     7,  66,  69,  14,  35, Str.STR_NULL,                Str.STR_NULL),
	new Widget(     Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   0,  21,  14,  35, Sprite.SPR_IMG_TERRAFORM_DOWN,  Str.STR_018E_LOWER_A_CORNER_OF_LAND),
	new Widget(     Window.WWT_PANEL,   Window.RESIZE_NONE,     7,  22,  43,  14,  35, Sprite.SPR_IMG_TERRAFORM_UP,    Str.STR_018F_RAISE_A_CORNER_OF_LAND),
	new Widget(     Window.WWT_PANEL,   Window.RESIZE_NONE,     7,  44,  65,  14,  35, Sprite.SPR_IMG_LEVEL_LAND,      Str.STR_LEVEL_LAND_TOOLTIP),
	new Widget(     Window.WWT_PANEL,   Window.RESIZE_NONE,     7,  70,  91,  14,  35, Sprite.SPR_IMG_DYNAMITE,        Str.STR_018D_DEMOLISH_BUILDINGS_ETC),
	new Widget(     Window.WWT_PANEL,   Window.RESIZE_NONE,     7,  92, 113,  14,  35, Sprite.SPR_IMG_BUY_LAND,        Str.STR_0329_PURCHASE_LAND_FOR_FUTURE),
	new Widget(     Window.WWT_PANEL,   Window.RESIZE_NONE,     7, 114, 135,  14,  35, Sprite.SPR_IMG_PLANTTREES,      Str.STR_0185_PLANT_TREES_PLACE_SIGNS),
	new Widget(     Window.WWT_PANEL,   Window.RESIZE_NONE,     7, 136, 157,  14,  35, Sprite.SPR_IMG_PLACE_SIGN,      Str.STR_0289_PLACE_SIGN),

	//new Widget(    WIDGETS_END),
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

	public static void ShowTerraformToolbar()
	{
		if (PlayerID.getCurrent().isSpectator()) return;
		Window.AllocateWindowDescFront(_terraform_desc, 0);
	}
	
	
}

@FunctionalInterface
interface ButtonProc extends Consumer <Window>{}
