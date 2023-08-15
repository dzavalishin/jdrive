package com.dzavalishin.xui;

import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.SwitchModes;
import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Main;
import com.dzavalishin.game.Str;
import com.dzavalishin.net.NetGui;

public class IntroGui 
{


	static final Widget _select_game_widgets[] = {
	new Widget(    Window.WWT_CAPTION, Window.RESIZE_NONE, 13,   0, 335,   0,  13, Str.STR_0307_OPENTTD,       Str.STR_NULL),
	new Widget(     Window.WWT_IMGBTN, Window.RESIZE_NONE, 13,   0, 335,  14, 196, Str.STR_NULL,               Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN, Window.RESIZE_NONE, 12,  10, 167,  22,  33, Str.STR_0140_NEW_GAME,      Str.STR_02FB_START_A_NEW_GAME),
	new Widget( Window.WWT_PUSHTXTBTN, Window.RESIZE_NONE, 12, 168, 325,  22,  33, Str.STR_0141_LOAD_GAME,     Str.STR_02FC_LOAD_A_SAVED_GAME),
	new Widget( Window.WWT_PUSHTXTBTN, Window.RESIZE_NONE, 12,  10, 167,  40,  51, Str.STR_0220_CREATE_SCENARIO,Str.STR_02FE_CREATE_A_CUSTOMIZED_GAME),
	new Widget( Window.WWT_PUSHTXTBTN, Window.RESIZE_NONE, 12, 168, 325,  40,  51, Str.STR_029A_PLAY_SCENARIO, Str.STR_0303_START_A_NEW_GAME_USING),
	new Widget(    Window.WWT_PANEL_2, Window.RESIZE_NONE, 12,  10,  86,  59, 113, 0x1312,                 Str.STR_030E_SELECT_TEMPERATE_LANDSCAPE),
	new Widget(    Window.WWT_PANEL_2, Window.RESIZE_NONE, 12,  90, 166,  59, 113, 0x1314,                 Str.STR_030F_SELECT_SUB_ARCTIC_LANDSCAPE),
	new Widget(    Window.WWT_PANEL_2, Window.RESIZE_NONE, 12, 170, 246,  59, 113, 0x1316,                 Str.STR_0310_SELECT_SUB_TROPICAL_LANDSCAPE),
	new Widget(    Window.WWT_PANEL_2, Window.RESIZE_NONE, 12, 250, 326,  59, 113, 0x1318,                 Str.STR_0311_SELECT_TOYLAND_LANDSCAPE),

	new Widget(      Window.WWT_PANEL, Window.RESIZE_NONE, 12, 219, 254, 120, 131, Str.STR_NULL,               Str.STR_NULL),
	new Widget(    Window.WWT_TEXTBTN, Window.RESIZE_NONE, 12, 255, 266, 120, 131, Str.STR_0225,               Str.STR_NULL),
	new Widget(      Window.WWT_PANEL, Window.RESIZE_NONE, 12, 279, 314, 120, 131, Str.STR_NULL,               Str.STR_NULL),
	new Widget(    Window.WWT_TEXTBTN, Window.RESIZE_NONE, 12, 315, 326, 120, 131, Str.STR_0225,               Str.STR_NULL),

	new Widget( Window.WWT_PUSHTXTBTN, Window.RESIZE_NONE, 12,  10, 167, 138, 149, Str.STR_SINGLE_PLAYER,      Str.STR_02FF_SELECT_SINGLE_PLAYER_GAME),
	new Widget( Window.WWT_PUSHTXTBTN, Window.RESIZE_NONE, 12, 168, 325, 138, 149, Str.STR_MULTIPLAYER,        Str.STR_0300_SELECT_MULTIPLAYER_GAME),
	new Widget( Window.WWT_PUSHTXTBTN, Window.RESIZE_NONE, 12,  10, 167, 159, 170, Str.STR_0148_GAME_OPTIONS,  Str.STR_0301_DISPLAY_GAME_OPTIONS),
	new Widget( Window.WWT_PUSHTXTBTN, Window.RESIZE_NONE, 12, 168, 325, 159, 170, Str.STR_01FE_DIFFICULTY,    Str.STR_0302_DISPLAY_DIFFICULTY_OPTIONS),
	new Widget( Window.WWT_PUSHTXTBTN, Window.RESIZE_NONE, 12,  10, 167, 177, 188, Str.STR_CONFIG_PATCHES,     Str.STR_CONFIG_PATCHES_TIP),
	new Widget( Window.WWT_PUSHTXTBTN, Window.RESIZE_NONE, 12, 168, 325, 177, 188, Str.STR_0304_QUIT,          Str.STR_0305_QUIT_OPENTTD),
	};


	static  void CreateScenario() {
		Global._switch_mode = SwitchModes.SM_EDITOR;}

	static void SetNewLandscapeType(int landscape)
	{
		GameOptions._opt_newgame.landscape = (byte) landscape;
		Window.InvalidateWindowClasses(Window.WC_SELECT_GAME);
	}

	static final int mapsizes[] = {Str.STR_64, Str.STR_128, Str.STR_256, Str.STR_512, Str.STR_1024, Str.STR_2048, Str.INVALID_STRING };

	static void SelectGameWndProc(Window w, WindowEvent e)
	{
		/* We do +/- 6 for the map_xy because 64 is 2^6, but it is the lowest available element */

		switch (e.event) {
		case WE_PAINT:
			w.click_state = (w.click_state & ~(1 << 14) & ~(0xF << 6)) | (1 << (GameOptions._opt_newgame.landscape + 6)) | (1 << 14);
			Global.SetDParam(0, Str.STR_6801_EASY + GameOptions._opt_newgame.diff_level);
			w.DrawWindowWidgets();

			Gfx.DrawStringRightAligned(216, 121, Str.STR_MAPSIZE, 0);
			Gfx.DrawString(223, 121, mapsizes[Global._patches.map_x - 6], 0x10);
			//Gfx.DrawString(223, 121, mapsizes[0], 0x10);
			Gfx.DrawString(270, 121, Str.STR_BY, 0);
			Gfx.DrawString(283, 121, mapsizes[Global._patches.map_y - 6], 0x10);
			//Gfx.DrawString(283, 121, mapsizes[0], 0x10);
			break;

		case WE_KEYPRESS:
			if( e.keycode == Window.WKC_RETURN )
				MiscGui.AskForNewGameToStart(); 
			break;
			
		case WE_CLICK:
			switch (e.widget) {
			case 2: MiscGui.AskForNewGameToStart(); break;
			case 3: MiscGui.ShowSaveLoadDialog(Global.SLD_LOAD_GAME); break;
			case 4: CreateScenario(); break;
			case 5: MiscGui.ShowSaveLoadDialog(Global.SLD_LOAD_SCENARIO); break;
			case 6: case 7: case 8: case 9:
				SetNewLandscapeType(e.widget - 6);
				break;
			case 10: case 11: /* Mapsize X */
				Window.ShowDropDownMenu(w, mapsizes, Global._patches.map_x - 6, 11, 0, 0);
				break;
			case 12: case 13: /* Mapsize Y */
				Window.ShowDropDownMenu(w, mapsizes, Global._patches.map_y - 6, 13, 0, 0);
				break;
			case 15:

				if (!Global._network_available) {
					//ShowErrorMessage(INVALID_STRING_ID, Str.STR_NETWORK_ERR_NOTAVAILABLE, 0, 0);
					Global.ShowErrorMessage(Str.INVALID_STRING , Str.STR_NETWORK_ERR_NOTAVAILABLE, 0, 0);
				} else
					NetGui.ShowNetworkGameWindow();
	//#else
				Global.ShowErrorMessage(Str.INVALID_STRING , Str.STR_NETWORK_ERR_NOTAVAILABLE, 0, 0);
	//#endif
				break;
			case 16: SettingsGui.ShowGameOptions(); break;
			case 17: SettingsGui.ShowGameDifficulty(); break;
			case 18: SettingsGui.ShowPatchesSelection(); break;
			case 19: AskExitGame(); break;
			}
			break;

		case WE_ON_EDIT_TEXT: Gui.HandleOnEditText(e); break;
		case WE_ON_EDIT_TEXT_CANCEL: Gui.HandleOnEditTextCancel(); break;

		case WE_DROPDOWN_SELECT: /* Mapsize selection */
			switch (e.button) {
				case 11: Global._patches.map_x = e.index + 6; break;
				case 13: Global._patches.map_y = e.index + 6; break;
			}
			w.SetWindowDirty();
			break;
		default:
			break;
		}

	}

	static final WindowDesc _select_game_desc = new WindowDesc(
		Window.WDP_CENTER, Window.WDP_CENTER, 336, 197,
		Window.WC_SELECT_GAME,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_select_game_widgets,
		IntroGui::SelectGameWndProc
	);

	static void ShowSelectGameWindow()
	{
		Window.AllocateWindowDesc(_select_game_desc);
	}

	public static void GenRandomNewGame(int rnd1, int rnd2)
	{
		Global._random_seeds[0][0] = rnd1;
		Global._random_seeds[0][1] = rnd2;

		Main.SwitchMode(SwitchModes.SM_NEWGAME);
	}

	public static void StartScenarioEditor(int rnd1, int rnd2)
	{
		Global._random_seeds[0][0] = rnd1;
		Global._random_seeds[0][1] = rnd2;

		Main.SwitchMode(SwitchModes.SM_START_SCENARIO);
	}

	static final Widget _ask_abandon_game_widgets[] = {
	new Widget( Window.WWT_CLOSEBOX, Window.RESIZE_NONE,  4,   0,  10,   0,  13, Str.STR_00C5,      Str.STR_018B_CLOSE_WINDOW),
	new Widget(  Window.WWT_CAPTION, Window.RESIZE_NONE,  4,  11, 179,   0,  13, Str.STR_00C7_QUIT, Str.STR_NULL),
	new Widget(   Window.WWT_IMGBTN, Window.RESIZE_NONE,  4,   0, 179,  14,  91, 0x0,           Str.STR_NULL),
	new Widget(  Window.WWT_TEXTBTN, Window.RESIZE_NONE, 12,  25,  84,  72,  83, Str.STR_00C9_NO,   Str.STR_NULL),
	new Widget(  Window.WWT_TEXTBTN, Window.RESIZE_NONE, 12,  95, 154,  72,  83, Str.STR_00C8_YES,  Str.STR_NULL),
	};

	static void AskAbandonGameWndProc(Window  w, WindowEvent  e)
	{
		switch (e.event) {
		case WE_PAINT:
			w.DrawWindowWidgets();
			Global.SetDParam(0, Str.STR_0134_UNIX); // TODO name me 
			Gfx.DrawStringMultiCenter(90, 38, Str.STR_00CA_ARE_YOU_SURE_YOU_WANT_TO, 178);
			return;

		case WE_CLICK:
			switch (e.widget) {
				case 3: w.DeleteWindow();   break;
				case 4: Global._exit_game = true; break;
			}
			break;

		case WE_KEYPRESS: /* Exit game on pressing 'Enter' */
			switch (e.keycode) {
				case Window.WKC_RETURN:
				case Window.WKC_NUM_ENTER:
					Global._exit_game = true;
					break;
			}
			break;
		default:
			break;
		}
	}

	static final WindowDesc _ask_abandon_game_desc = new WindowDesc(
		Window.WDP_CENTER, Window.WDP_CENTER, 180, 92,
		Window.WC_ASK_ABANDON_GAME,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_UNCLICK_BUTTONS,
		_ask_abandon_game_widgets,
		IntroGui::AskAbandonGameWndProc
	);

	static void AskExitGame()
	{
		Window.AllocateWindowDescFront(_ask_abandon_game_desc, 0);
	}


	static final Widget _ask_quit_game_widgets[] = {
	new Widget( Window.WWT_CLOSEBOX, Window.RESIZE_NONE,  4,   0,  10,   0,  13, Str.STR_00C5,           Str.STR_018B_CLOSE_WINDOW),
	new Widget(  Window.WWT_CAPTION, Window.RESIZE_NONE,  4,  11, 179,   0,  13, Str.STR_0161_QUIT_GAME, Str.STR_NULL),
	new Widget(   Window.WWT_IMGBTN, Window.RESIZE_NONE,  4,   0, 179,  14,  91, 0x0,                	 Str.STR_NULL),
	new Widget(  Window.WWT_TEXTBTN, Window.RESIZE_NONE, 12,  25,  84,  72,  83, Str.STR_00C9_NO,        Str.STR_NULL),
	new Widget(  Window.WWT_TEXTBTN, Window.RESIZE_NONE, 12,  95, 154,  72,  83, Str.STR_00C8_YES,       Str.STR_NULL),
	};

	static void AskQuitGameWndProc(Window  w, WindowEvent  e)
	{
		switch (e.event) {
			case WE_PAINT:
				w.DrawWindowWidgets();
				Gfx.DrawStringMultiCenter(
					90, 38,
					Global._game_mode != GameModes.GM_EDITOR ?
						Str.STR_0160_ARE_YOU_SURE_YOU_WANT_TO : Str.STR_029B_ARE_YOU_SURE_YOU_WANT_TO,
					178
				);
				break;

			case WE_CLICK:
				switch (e.widget) {
					case 3: w.DeleteWindow();        break;
					case 4: Global._switch_mode = SwitchModes.SM_MENU; break;
				}
				break;

			case WE_KEYPRESS: /* Return to main menu on pressing 'Enter' */
				if (e.keycode == Window.WKC_RETURN) Global._switch_mode = SwitchModes.SM_MENU;
				break;
		default:
			break;
		}
	}

	static final WindowDesc _ask_quit_game_desc = new WindowDesc(
		Window.WDP_CENTER, Window.WDP_CENTER, 180, 92,
		Window.WC_QUIT_GAME,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_UNCLICK_BUTTONS,
		_ask_quit_game_widgets,
		IntroGui::AskQuitGameWndProc
	);


	static void AskExitToGameMenu()
	{
		Window.AllocateWindowDescFront(_ask_quit_game_desc, 0);
	}

}
