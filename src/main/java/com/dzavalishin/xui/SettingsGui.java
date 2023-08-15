package com.dzavalishin.xui;

import java.util.Iterator;

import com.dzavalishin.console.DefaultConsole;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Currency;
import com.dzavalishin.game.Engine;
import com.dzavalishin.game.GRFFile;
import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.Town;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.struct.GameSettingData;
import com.dzavalishin.tables.BooleanPatchVariable;
import com.dzavalishin.tables.CurrencyPatchVariable;
import com.dzavalishin.tables.CurrencySpec;
import com.dzavalishin.tables.PatchEntry;
import com.dzavalishin.tables.PatchPage;
import com.dzavalishin.tables.PatchVariable;
import com.dzavalishin.tables.SettingsTables;
import com.dzavalishin.util.BinaryString;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.ScreenShot;
import com.dzavalishin.util.Strings;

public class SettingsGui extends SettingsTables
{

	static int _difficulty_click_a;
	static int _difficulty_click_b;
	static byte _difficulty_timeout;

	static final int DIFF_INGAME_DISABLED_BUTTONS  = 0x383E;

	private static CurrencySpec _custom_currency() { return CurrencySpec._currency_specs[23]; }

	static int [] BuildDynamicDropdown(/*StringID*/ int base, int num)
	{
		//static /*StringID*/ int buf[32 + 1];
		int [] buf = new int[num + 1];
		//StringID *p = buf;
		//while (--num>=0) *p++ = base++;
		//*p = Str.INVALID_STRING;
		int i;
		for( i = 0; i < num; i++ )
			buf[i] = base++;

		buf[i] = Str.INVALID_STRING;

		return buf;
	}

	static int GetCurRes()
	{
		/*
		int i;

		for (i = 0; i != _num_resolutions; i++) {
			if (_resolutions[i][0] == _screen.width &&
					_resolutions[i][1] == _screen.height) {
				break;
			}
		}
		return i;
		 */

		return 0;
	}

	static  boolean RoadVehiclesAreBuilt()
	{
		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			final Vehicle v = ii.next();
			if (v.getType() == Vehicle.VEH_Road) return true;
		}
		return false;
	}

	static void GameOptionsWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			//int i;
			/*StringID*/ int str = Str.STR_02BE_DEFAULT;
			//w.disabled_state = (Global._vehicle_design_names & 1) != 0 ? (++str, 0) : (1 << 21);

			w.disabled_state = 1 << 21;

			if((Global._vehicle_design_names & 1) != 0)
			{
				++str;			
				w.disabled_state = 0;
			}

			if((Global._vehicle_design_names & 1)!=0)
			{
				++str;
				w.disabled_state = 0;
			}
			else
			{
				w.disabled_state = (1 << 21);
			}

			Global.SetDParam(0, str);
			Global.SetDParam(1, Currency._currency_string_list[GameOptions._opt_ptr.currency]);
			Global.SetDParam(2, (GameOptions._opt_ptr.kilometers ? 1 : 0) + Str.STR_0139_IMPERIAL_MILES);
			Global.SetDParam(3, Str.STR_02E9_DRIVE_ON_LEFT + GameOptions._opt_ptr.road_side);
			Global.SetDParam(4, Str.STR_TOWNNAME_ORIGINAL_ENGLISH + GameOptions._opt_ptr.town_name);
			Global.SetDParam(5, _autosave_dropdown[GameOptions._opt_ptr.autosave]);
			Global.SetDParam(6, Strings.SPECSTR_LANGUAGE_START + Strings._dynlang.curr);
			//i = GetCurRes();
			Global.SetDParam(7, Str.STR_RES_OTHER); // i == _num_resolutions ? Str.STR_RES_OTHER : Strings.SPECSTR_RESOLUTION_START + i);
			Global.SetDParam(8, Strings.SPECSTR_SCREENSHOT_START + ScreenShot._cur_screenshot_format);

			// (_fullscreen) ? SETBIT(w.click_state, 28) : CLRBIT(w.click_state, 28); // fullscreen button
			fullScreenSwitch(w);

			w.DrawWindowWidgets();
			Gfx.DrawString(20, 175, Str.STR_OPTIONS_FULLSCREEN, 0); // fullscreen
		}	break;

		case WE_CLICK:
			switch (e.widget) {
			case 4: case 5: /* Setup currencies dropdown */
				Window.ShowDropDownMenu(w, Currency._currency_string_list, GameOptions._opt_ptr.currency, 5, Global._game_mode == GameModes.GM_MENU ? 0 : ~Currency.GetMaskOfAllowedCurrencies(), 0);
				return;
			case 7: case 8: /* Setup distance unit dropdown */
				Window.ShowDropDownMenu(w, _distances_dropdown, GameOptions._opt_ptr.kilometers ? 1 : 0, 8, 0, 0);
				return;
			case 10: case 11: { /* Setup road-side dropdown */
				int i = 0;

				/* You can only change the drive side if you are in the menu or ingame with
				 * no vehicles present. In a networking game only the server can change it */
				if ((Global._game_mode != GameModes.GM_MENU && RoadVehiclesAreBuilt()) || (Global._networking && !Global._network_server))
					i = (-1) ^ (1 << GameOptions._opt_ptr.road_side); // disable the other value

				Window.ShowDropDownMenu(w, _driveside_dropdown, GameOptions._opt_ptr.road_side, 11, i, 0);
			} return;
			case 13: case 14: { /* Setup townname dropdown */
				int i = GameOptions._opt_ptr.town_name;
				Window.ShowDropDownMenu(w, BuildDynamicDropdown(Str.STR_TOWNNAME_ORIGINAL_ENGLISH, Strings.SPECSTR_TOWNNAME_LAST - Strings.SPECSTR_TOWNNAME_START + 1), i, 14, (Global._game_mode == GameModes.GM_MENU) ? 0 : (-1) ^ (1 << i), 0);
				return;
			}
			case 16: case 17: /* Setup autosave dropdown */
				Window.ShowDropDownMenu(w, _autosave_dropdown, GameOptions._opt_ptr.autosave, 17, 0, 0);
				return;
			case 19: case 20: /* Setup customized vehicle-names dropdown */
				Window.ShowDropDownMenu(w, _designnames_dropdown, (Global._vehicle_design_names & 1) != 0 ? 1 : 0, 20, (Global._vehicle_design_names & 2) != 0 ? 0 : 2, 0);
				return;
			case 21: /* Save customized vehicle-names to disk */
				return;
			case 23: case 24: /* Setup interface language dropdown */
				Window.ShowDropDownMenu(w, Strings._dynlang.dropdown, Strings._dynlang.curr, 24, 0, 0);
				return;
			case 26: case 27: /* Setup resolution dropdown */
				Window.ShowDropDownMenu(w, BuildDynamicDropdown(Strings.SPECSTR_RESOLUTION_START, 1/*Global._num_resolutions*/), GetCurRes(), 27, 0, 0);
				return;
			case 28: /* Click fullscreen on/off */
				// (_fullscreen) ? CLRBIT(w.click_state, 28) : SETBIT(w.click_state, 28);
				fullScreenSwitch(w);

				Global.hal.ToggleFullScreen(!Global._fullscreen); // toggle full-screen on/off
				w.SetWindowDirty();
				return;
			case 30: case 31: /* Setup screenshot format dropdown */
				Window.ShowDropDownMenu(w, BuildDynamicDropdown(Strings.SPECSTR_SCREENSHOT_START, ScreenShot._num_screenshot_formats), ScreenShot._cur_screenshot_format, 31, 0, 0);
				return;
			}
			break;

		case WE_DROPDOWN_SELECT:
			switch (e.button) {
			case 20: /* Vehicle design names */
				if (e.index == 0) {
					Engine.DeleteCustomEngineNames();
					Hal.MarkWholeScreenDirty();
				} else if (0==(Global._vehicle_design_names & 1)) {
					Engine.LoadCustomEngineNames();
					Hal.MarkWholeScreenDirty();
				}
				break;
			case 5: /* Currency */
				if (e.index == 23)
					ShowCustCurrency();
				GameOptions._opt_ptr.currency =  e.index;
				Hal.MarkWholeScreenDirty();
				break;
			case 8: /* Distance units */
				GameOptions._opt_ptr.kilometers = e.index != 0;
				Hal.MarkWholeScreenDirty();
				break;
			case 11: /* Road side */
				if (GameOptions._opt_ptr.road_side != e.index) { // only change if setting changed
					Cmd.DoCommandP(null, e.index, 0, null, Cmd.CMD_SET_ROAD_DRIVE_SIDE | Cmd.CMD_MSG(Str.STR_00B4_CAN_T_DO_THIS));
					Hal.MarkWholeScreenDirty();
				}
				break;
			case 14: /* Town names */
				if (Global._game_mode == GameModes.GM_MENU) {
					GameOptions._opt_ptr.town_name =  e.index;
					Window.InvalidateWindow(Window.WC_GAME_OPTIONS, 0);
				}
				break;
			case 17: /* Autosave options */
				GameOptions._opt_ptr.autosave =  e.index;
				w.SetWindowDirty();
				break;
			case 24: /* Change interface language */
				Strings.ReadLanguagePack(e.index);
				Hal.MarkWholeScreenDirty();
				break;
			case 27: /* Change resolution */
				// TODO if (e.index < _num_resolutions && ChangeResInGame(_resolutions[e.index][0],_resolutions[e.index][1])) w.SetWindowDirty();
				break;
			case 31: /* Change screenshot format */
				ScreenShot.SetScreenshotFormat(e.index);
				w.SetWindowDirty();
				break;
			}
			break;

		case WE_DESTROY:
			Window.DeleteWindowById(Window.WC_CUSTOM_CURRENCY, 0);
			break;
		default:
			break;
		}

	}

	private static void fullScreenSwitch(Window w) {
		if(Global._fullscreen) 
			w.click_state = BitOps.RETSETBIT(w.click_state, 28);
		else 
			w.click_state = BitOps.RETCLRBIT(w.click_state, 28); // fullscreen button
	}

	/** Change the side of the road vehicles drive on (server only).
	 * @param x,y unused
	 * @param p1 the side of the road; 0 = left side and 1 = right side
	 * @param p2 unused
	 */
	public static int CmdSetRoadDriveSide(int x, int y, int flags, int p1, int p2)
	{
		/* Check boundaries and you can only change this if NO vehicles have been built yet,
		 * except in the intro-menu where of course it's always possible to do so. */
		if (p1 > 1 || (Global._game_mode != GameModes.GM_MENU && RoadVehiclesAreBuilt())) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC) ) {
			GameOptions._opt_ptr.road_side =  p1;
			Window.InvalidateWindow(Window.WC_GAME_OPTIONS,0);
		}
		return 0;
	}

	static final Widget _game_options_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   369,     0,    13, Str.STR_00B1_GAME_OPTIONS,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   369,    14,   238, 0x0,											Str.STR_NULL),
			new Widget(      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,    10,   179,    20,    55, Str.STR_02E0_CURRENCY_UNITS,	Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    14,    20,   169,    34,    45, Str.STR_02E1,								Str.STR_02E2_CURRENCY_UNITS_SELECTION),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   158,   168,    35,    44, Str.STR_0225,								Str.STR_02E2_CURRENCY_UNITS_SELECTION),
			new Widget(      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,   190,   359,    20,    55, Str.STR_02E3_DISTANCE_UNITS,	Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    14,   200,   349,    34,    45, Str.STR_02E4,								Str.STR_02E5_DISTANCE_UNITS_SELECTION),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   338,   348,    35,    44, Str.STR_0225,								Str.STR_02E5_DISTANCE_UNITS_SELECTION),
			new Widget(      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,    10,   179,    62,    97, Str.STR_02E6_ROAD_VEHICLES,	Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    14,    20,   169,    76,    87, Str.STR_02E7,								Str.STR_02E8_SELECT_SIDE_OF_ROAD_FOR),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   158,   168,    77,    86, Str.STR_0225,								Str.STR_02E8_SELECT_SIDE_OF_ROAD_FOR),
			new Widget(      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,   190,   359,    62,    97, Str.STR_02EB_TOWN_NAMES,			Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    14,   200,   349,    76,    87, Str.STR_02EC,								Str.STR_02ED_SELECT_STYLE_OF_TOWN_NAMES),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   338,   348,    77,    86, Str.STR_0225,								Str.STR_02ED_SELECT_STYLE_OF_TOWN_NAMES),
			new Widget(      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,    10,   179,   104,   139, Str.STR_02F4_AUTOSAVE,				Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    14,    20,   169,   118,   129, Str.STR_02F5,								Str.STR_02F6_SELECT_INTERVAL_BETWEEN),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   158,   168,   119,   128, Str.STR_0225,								Str.STR_02F6_SELECT_INTERVAL_BETWEEN),

			new Widget(      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,    10,   359,   194,   228, Str.STR_02BC_VEHICLE_DESIGN_NAMES,				Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    14,    20,   119,   207,   218, Str.STR_02BD,								Str.STR_02C1_VEHICLE_DESIGN_NAMES_SELECTION),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   108,   118,   208,   217, Str.STR_0225,								Str.STR_02C1_VEHICLE_DESIGN_NAMES_SELECTION),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   130,   349,   207,   218, Str.STR_02C0_SAVE_CUSTOM_NAMES,	Str.STR_02C2_SAVE_CUSTOMIZED_VEHICLE),

			new Widget(      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,   190,   359,   104,   139, Str.STR_OPTIONS_LANG,				Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    14,   200,   349,   118,   129, Str.STR_OPTIONS_LANG_CBO,		Str.STR_OPTIONS_LANG_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   338,   348,   119,   128, Str.STR_0225,								Str.STR_OPTIONS_LANG_TIP),

			new Widget(      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,    10,   179,   146,   190, Str.STR_OPTIONS_RES,					Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    14,    20,   169,   160,   171, Str.STR_OPTIONS_RES_CBO,			Str.STR_OPTIONS_RES_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   158,   168,   161,   170, Str.STR_0225,								Str.STR_OPTIONS_RES_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   149,   169,   176,   184, Str.STR_EMPTY,								Str.STR_OPTIONS_FULLSCREEN_TIP),

			new Widget(      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,   190,   359,   146,   190, Str.STR_OPTIONS_SCREENSHOT_FORMAT,				Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    14,   200,   349,   160,   171, Str.STR_OPTIONS_SCREENSHOT_FORMAT_CBO,		Str.STR_OPTIONS_SCREENSHOT_FORMAT_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   338,   348,   161,   170, Str.STR_0225,								Str.STR_OPTIONS_SCREENSHOT_FORMAT_TIP),

	};

	static final WindowDesc _game_options_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 370, 239,
			Window.WC_GAME_OPTIONS,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_game_options_widgets,
			SettingsGui::GameOptionsWndProc
			);


	static void ShowGameOptions()
	{
		Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
		Window.AllocateWindowDesc(_game_options_desc);
	}


	static final GameSettingData _game_setting_info[] = {
			new GameSettingData(  0,   7,  1, Str.STR_NULL),
			new GameSettingData(  0,   3,  1, Str.STR_6830_IMMEDIATE),
			new GameSettingData(  0,   2,  1, Str.STR_6816_LOW),
			new GameSettingData(  0,   3,  1, Str.STR_26816_NONE),
			new GameSettingData(100, 500, 50, Str.STR_NULL),
			new GameSettingData(  2,   4,  1, Str.STR_NULL),
			new GameSettingData(  0,   2,  1, Str.STR_6820_LOW),
			new GameSettingData(  0,   4,  1, Str.STR_681B_VERY_SLOW),
			new GameSettingData(  0,   2,  1, Str.STR_6820_LOW),
			new GameSettingData(  0,   2,  1, Str.STR_6823_NONE),
			new GameSettingData(  0,   3,  1, Str.STR_6826_X1_5),
			new GameSettingData(  0,   2,  1, Str.STR_6820_LOW),
			new GameSettingData(  0,   3,  1, Str.STR_682A_VERY_FLAT),
			new GameSettingData(  0,   3,  1, Str.STR_VERY_LOW),
			new GameSettingData(  0,   1,  1, Str.STR_682E_STEADY),
			new GameSettingData(  0,   1,  1, Str.STR_6834_AT_END_OF_LINE_AND_AT_STATIONS),
			new GameSettingData(  0,   1,  1, Str.STR_6836_OFF),
			new GameSettingData(  0,   2,  1, Str.STR_6839_PERMISSIVE),
	};


	static  boolean GetBitAndShift(int [] b)
	{
		int x = b[0];
		b[0] >>= 1;
				return BitOps.HASBIT(x, 0);
	}


	public static void SetDifficultyLevel(int mode, GameOptions gm_opt)
	{

		//int i;
		assert(mode <= 3);

		gm_opt.diff_level =  mode;
		if (mode != 3) { // not custom
			/*
			for (i = 0; i != Global.GAME_DIFFICULTY_NUM; i++)
			{
				//((int*)&gm_opt.diff)[i] = _default_game_diff[mode][i];
			}
			 */
			gm_opt.diff = _default_game_diff[mode];
		}

	}

	//extern void StartupEconomy();

	//enum {
	private static final int GAMEDIFF_WND_TOP_OFFSET = 45;
	private static final int GAMEDIFF_WND_ROWSIZE    = 9;
	//};

	// Temporary holding place of values in the difficulty window until 'Save' is clicked
	static GameOptions _opt_mod_temp = new GameOptions();
	// 0x383E = (1 << 13) | (1 << 12) | (1 << 11) | (1 << 5) | (1 << 4) | (1 << 3) | (1 << 2) | (1 << 1)
	//#define DIFF_INGAME_DISABLED_BUTTONS 0x383E

	static void GameDifficultyWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_CREATE: /* Setup disabled buttons when creating window */
			// disable all other difficulty buttons during gameplay except for 'custom'
			w.disabled_state = (Global._game_mode != GameModes.GM_NORMAL) ? 0 : (1 << 3) | (1 << 4) | (1 << 5) | (1 << 6);

			if (Global._game_mode == GameModes.GM_EDITOR) w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 7);

			if (Global._networking) {
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 7); // disable highscore chart in multiplayer
				if (!Global._network_server)
					w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 10); // Disable save-button in multiplayer (and if client)
			}
			break;
		case WE_PAINT: {
			//int click_a, click_b, 
			int i;
			int y, value;

			w.click_state = (1 << 3) << _opt_mod_temp.diff_level; // have current difficulty button clicked
			w.DrawWindowWidgets();

			int [] click_a = { _difficulty_click_a };
			int [] click_b = { _difficulty_click_b };
			int [] disabled = { 0 };

			/* XXX - Disabled buttons in normal gameplay. Bitshifted for each button to see if
			 * that bit is set. If it is set, the button is disabled */
			disabled[0] = (Global._game_mode == GameModes.GM_NORMAL) ? DIFF_INGAME_DISABLED_BUTTONS : 0;
			/* GAME_DIFFICULTY */
			y = GAMEDIFF_WND_TOP_OFFSET;
			for (i = 0; i != Global.GAME_DIFFICULTY_NUM; i++) {
				Gfx.DrawFrameRect( 5, y,  5 + 8, y + 8, 3, GetBitAndShift(click_a) ? (1 << 5) : 0);
				Gfx.DrawFrameRect(15, y, 15 + 8, y + 8, 3, GetBitAndShift(click_b) ? (1 << 5) : 0);
				if (GetBitAndShift(disabled) || (Global._networking && !Global._network_server)) {
					int color = Sprite.PALETTE_MODIFIER_GREYOUT | Global._color_list[3].unk2;
					Gfx.GfxFillRect( 6, y + 1,  6 + 8, y + 8, color);
					Gfx.GfxFillRect(16, y + 1, 16 + 8, y + 8, color);
				}

				Gfx.DrawStringCentered(10, y, Str.STR_6819, 0);
				Gfx.DrawStringCentered(20, y, Str.STR_681A, 0);


				//value =  _game_setting_info[i].str + ((int*)&_opt_mod_temp.diff)[i];
				value =  _game_setting_info[i].str + _opt_mod_temp.diff.getAsInt(i);
				if (i == 4) value *= 1000; // XXX - handle currency option
				Global.SetDParam(0, value);
				Gfx.DrawString(30, y, Str.STR_6805_MAXIMUM_NO_COMPETITORS + i, 0);

				y += GAMEDIFF_WND_ROWSIZE + 2; // space items apart a bit
			} 
		} break;

		case WE_CLICK:
			switch (e.widget) {
			case 8: { /* Difficulty settings widget, decode click */
				final GameSettingData info;
				int x, y;
				int btn, dis;
				int val;

				// Don't allow clients to make any changes
				if  (Global._networking && !Global._network_server)
					return;

				x = e.pt.x - 5;
				if (!BitOps.IS_INT_INSIDE(x, 0, 21)) // Button area
					return;

				y = e.pt.y - GAMEDIFF_WND_TOP_OFFSET;
				if (y < 0)
					return;

				// Get button from Y coord.
				btn = y / (GAMEDIFF_WND_ROWSIZE + 2);
				if (btn >= Global.GAME_DIFFICULTY_NUM || y % (GAMEDIFF_WND_ROWSIZE + 2) >= 9)
					return;

				// Clicked disabled button?
				dis = (Global._game_mode == GameModes.GM_NORMAL) ? DIFF_INGAME_DISABLED_BUTTONS : 0;

				if (BitOps.HASBIT(dis, btn))
					return;

				_difficulty_timeout = 5;

				//val = ((int*)&_opt_mod_temp.diff)[btn];
				val = _opt_mod_temp.diff.getAsInt(btn);

				info = _game_setting_info[btn]; // get information about the difficulty setting
				if (x >= 10) {
					// Increase button clicked
					val = Math.min(val + info.step, info.max);
					_difficulty_click_b = BitOps.RETSETBIT(_difficulty_click_b, btn);
				} else {
					// Decrease button clicked
					val = Math.max(val - info.step, info.min);
					_difficulty_click_a = BitOps.RETSETBIT(_difficulty_click_a, btn);
				}

				// save value in temporary variable
				//((int*)&_opt_mod_temp.diff)[btn] = val;
				_opt_mod_temp.diff.setAsInt(btn,val);
				SetDifficultyLevel(3, _opt_mod_temp); // set difficulty level to custom

				w.SetWindowDirty();
			}	break;
			case 3: case 4: case 5: case 6: /* Easy / Medium / Hard / Custom */
				// temporarily change difficulty level
				SetDifficultyLevel(e.widget - 3, _opt_mod_temp);
				w.SetWindowDirty();
				break;
			case 7: /* Highscore Table */
				PlayerGui.ShowHighscoreTable(_opt_mod_temp.diff_level, -1);
				break;
			case 10: { /* Save button - save changes */
				int btn, val;
				/* difficulty */
				for (btn = 0; btn != Global.GAME_DIFFICULTY_NUM; btn++) {
					//val = ((int*)&_opt_mod_temp.diff)[btn];
					val = _opt_mod_temp.diff.getAsInt(btn);
					// if setting has changed, change it
					//if (val != ((int*)&_opt_ptr.diff)[btn])
					if (val != GameOptions._opt_ptr.diff.getAsInt(btn))
						Cmd.DoCommandP(null, btn, val, null, Cmd.CMD_CHANGE_DIFFICULTY_LEVEL);
				} 
				Cmd.DoCommandP(null, -1, _opt_mod_temp.diff_level, null, Cmd.CMD_CHANGE_DIFFICULTY_LEVEL);
				w.DeleteWindow();
				// If we are in the editor, we should reload the economy.
				//  This way when you load a game, the max loan and interest rate
				//  are loaded correctly.
				if (Global._game_mode == GameModes.GM_EDITOR)
					Global.gs._economy.StartupEconomy();
				break;
			}
			case 11: /* Cancel button - close window, abandon changes */
				w.DeleteWindow();
				break;
			} break;

		case WE_MOUSELOOP: /* Handle the visual 'clicking' of the buttons */
			if (_difficulty_timeout != 0 && 0 == --_difficulty_timeout) {
				_difficulty_click_a = 0;
				_difficulty_click_b = 0;
				w.SetWindowDirty();
			}
			break;
		default:
			break;
		}
	}

	//#undef DIFF_INGAME_DISABLED_BUTTONS

	static final Widget _game_difficulty_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    10,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    10,    11,   369,     0,    13, Str.STR_6800_DIFFICULTY_LEVEL,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    10,     0,   369,    14,    29, 0x0,												Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,    10,    96,    16,    27, Str.STR_6801_EASY,							Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,    97,   183,    16,    27, Str.STR_6802_MEDIUM,						Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,   184,   270,    16,    27, Str.STR_6803_HARD,							Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,   271,   357,    16,    27, Str.STR_6804_CUSTOM,						Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    10,     0,   369,    30,    41, Str.STR_6838_SHOW_HI_SCORE_CHART,Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    10,     0,   369,    42,   262, 0x0,												Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    10,     0,   369,   263,   278, 0x0,												Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,   105,   185,   265,   276, Str.STR_OPTIONS_SAVE_CHANGES,	Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,   186,   266,   265,   276, Str.STR_012E_CANCEL,						Str.STR_NULL),

	};

	static final WindowDesc _game_difficulty_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 370, 279,
			Window.WC_GAME_OPTIONS,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_game_difficulty_widgets,
			SettingsGui::GameDifficultyWndProc
			);

	public static void ShowGameDifficulty()
	{
		Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
		/* Copy current settings (ingame or in intro) to temporary holding place
		 * change that when setting stuff, copy back on clicking 'OK' */
		// memcpy(&_opt_mod_temp, GameOptions._opt_ptr, sizeof(GameOptions));
		_opt_mod_temp = new GameOptions();
		_opt_mod_temp.assign( GameOptions._opt_ptr );
		Window.AllocateWindowDesc(_game_difficulty_desc);
	}

	// virtual PositionMainToolbar function, calls the right one.
	static int v_PositionMainToolbar(int p1)
	{
		if (Global._game_mode != GameModes.GM_MENU) Window.PositionMainToolbar(null);
		return 0;
	}

	static int AiNew_PatchActive_Warning(int p1)
	{
		if (p1 == 1) Global.ShowErrorMessage(Str.INVALID_STRING, Str.TEMP_AI_ACTIVATED, 0, 0);
		return 0;
	}

	static int Ai_In_Multiplayer_Warning(int p1)
	{
		if (p1 == 1) {
			Global.ShowErrorMessage(Str.INVALID_STRING, Str.TEMP_AI_MULTIPLAYER, 0, 0);
			Global._patches.ainew_active = true;
		}
		return 0;
	}

	static int PopulationInLabelActive(int p1)
	{
		Town.forEach( (t) -> { if (t.getXy() != null) t.UpdateTownVirtCoord(); });
		return 0;
	}

	static int InvisibleTreesActive(int p1)
	{
		Hal.MarkWholeScreenDirty();
		return 0;
	}

	static int InValidateDetailsWindow(int p1)
	{
		Window.InvalidateWindowClasses(Window.WC_VEHICLE_DETAILS);
		return 0;
	}

	static int InvalidateStationBuildWindow(int p1)
	{
		Window.InvalidateWindow(Window.WC_BUILD_STATION, 0);
		return 0;
	}

	/* Check service intervals of vehicles, p1 is value of % or day based servicing */
	static int CheckInterval(int p1)
	{
		boolean warning;
		if (p1!=0) {
			warning = ( (BitOps.IS_INT_INSIDE(Global._patches.servint_trains,   5, 90+1) || Global._patches.servint_trains   == 0) &&
					(BitOps.IS_INT_INSIDE(Global._patches.servint_roadveh,  5, 90+1) || Global._patches.servint_roadveh  == 0) &&
					(BitOps.IS_INT_INSIDE(Global._patches.servint_aircraft, 5, 90+1) || Global._patches.servint_aircraft == 0) &&
					(BitOps.IS_INT_INSIDE(Global._patches.servint_ships,    5, 90+1) || Global._patches.servint_ships    == 0) );
		} else {
			warning = ( (BitOps.IS_INT_INSIDE(Global._patches.servint_trains,   30, 800+1) || Global._patches.servint_trains   == 0) &&
					(BitOps.IS_INT_INSIDE(Global._patches.servint_roadveh,  30, 800+1) || Global._patches.servint_roadveh  == 0) &&
					(BitOps.IS_INT_INSIDE(Global._patches.servint_aircraft, 30, 800+1) || Global._patches.servint_aircraft == 0) &&
					(BitOps.IS_INT_INSIDE(Global._patches.servint_ships,    30, 800+1) || Global._patches.servint_ships    == 0) );
		}

		if (!warning)
			Global.ShowErrorMessage(Str.INVALID_STRING, Str.STR_CONFIG_PATCHES_SERVICE_INTERVAL_INCOMPATIBLE, 0, 0);

		return InValidateDetailsWindow(0);
	}

	public static void EngineRenewUpdate(PatchVariable pv)
	{
		Cmd.DoCommandP(null, 0, BitOps.b2i( Global._patches.autorenew.get() ), null, Cmd.CMD_REPLACE_VEHICLE);
	}

	static int EngineRenewMonthsUpdate(int p1)
	{
		Cmd.DoCommandP(null, 1, Global._patches.autorenew_months, null, Cmd.CMD_REPLACE_VEHICLE);
		return 0;
	}

	static int EngineRenewMoneyUpdate(int p1)
	{
		// TODO (int)? Pass long?
		Cmd.DoCommandP(null, 2, (int)Global._patches.autorenew_money, null, Cmd.CMD_REPLACE_VEHICLE);
		return 0;
	}


	/*
	static int ReadPE(final PatchEntry*pe)
	{
		switch (pe.type) {
		case PE_BOOL:   return *(boolean*)pe.variable;
		case PE_UINT8:  return *(byte*)pe.variable;
		case PE_INT16:  return *(int*)pe.variable;
		case PE_UINT16: return *(int*)pe.variable;
		case PE_INT32:  return *(int*)pe.variable;
		case PE_CURRENCY:  return (*(int*)pe.variable) * _currency.rate;
		default: NOT_REACHED();
		}

		// useless, but avoids compiler warning this way 
		return 0;
	}

	static void WritePE(final PatchEntry* p, int v)
	{
		if ((p.flags & PF_0ISDIS) && v <= 0) {
			switch (p.type) {
			case PE_BOOL:     *(boolean*  )p.variable = false; break;
			case PE_UINT8:    *(byte* )p.variable = 0;     break;
			case PE_INT16:    *(int* )p.variable = 0;     break;
			case PE_UINT16:   *(int*)p.variable = 0;     break;
			case PE_CURRENCY: *(int* )p.variable = 0;     break;
			case PE_INT32:    *(int* )p.variable = 0;     break;
			}
			return;
		}

		// "clamp" 'disabled' value to smallest type
		switch (p.type) {
		case PE_BOOL:     *(boolean*  )p.variable = (v != 0); break;
		case PE_UINT8:    *(byte* )p.variable = clamp(v, p.min, p.max); break;
		case PE_INT16:    *(int* )p.variable = clamp(v, p.min, p.max); break;
		case PE_UINT16:   *(int*)p.variable = clamp(v, p.min, p.max); break;
		case PE_CURRENCY: *(int* )p.variable = clamp(v, p.min, p.max); break;
		case PE_INT32:    *(int* )p.variable = clamp(v, p.min, p.max); break;
		default: NOT_REACHED();
		}
	}
	 */

	static void PatchesSelectionWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			//int x,y;
			//final PatchEntry pe;
			//final PatchPage page;
			//int clk;
			//int val;
			//int i;

			w.click_state = 1 << (w.as_def_d().data_1 + 4);

			w.DrawWindowWidgets();

			int x = 0;
			int y = 46;
			int clk = w.as_def_d().data_2;

			PatchPage page = _patches_page[w.as_def_d().data_1];

			for (int i = 0; i != page.entries.length; i++) 
			{
				boolean disabled = false;
				boolean editable = true;
				PatchEntry pe = page.entries[i];

				if ((pe.isNetworkOnly()) && !Global._networking)
					editable = false;

				// We do not allow changes of some items when we are a client in a networkgame
				if (!(pe.isPlayerBased()) && Global._networking && !Global._network_server)
					editable = false;

				if (pe.getVariable() instanceof BooleanPatchVariable) {
					BooleanPatchVariable bv = (BooleanPatchVariable) pe.getVariable();
					if (editable)
						Gfx.DrawFrameRect(x+5, y+1, x+15+9, y+9, bv.get() ? 6 : 4, bv.get() ? Window.FR_LOWERED : 0);
					else
						Gfx.DrawFrameRect(x+5, y+1, x+15+9, y+9, bv.get() ? 7 : 9, bv.get() ? Window.FR_LOWERED : 0);
					Global.SetDParam(0, bv.get() ? Str.STR_CONFIG_PATCHES_ON : Str.STR_CONFIG_PATCHES_OFF);
				} else {
					Gfx.DrawFrameRect(x+5, y+1, x+5+9, y+9, 3, clk == i*2+1 ? Window.FR_LOWERED : 0);
					Gfx.DrawFrameRect(x+15, y+1, x+15+9, y+9, 3, clk == i*2+2 ? Window.FR_LOWERED : 0);
					if (!editable) {
						int color = Sprite.PALETTE_MODIFIER_GREYOUT | Global._color_list[3].unk2;
						Gfx.GfxFillRect(x+6, y+2, x+6+8, y+9, color);
						Gfx.GfxFillRect(x+16, y+2, x+16+8, y+9, color);
					}
					Gfx.DrawStringCentered(x+10, y+1, Str.STR_6819, 0);
					Gfx.DrawStringCentered(x+20, y+1, Str.STR_681A, 0);

					int val = pe.ReadPE();
					// TODO if (pe.getVariable() instanceof CurrencyPatchVariable) val /= Currency._currency.rate;
					disabled = ((val == 0) && (pe.zeroIsDisable()));

					if (disabled) {
						Global.SetDParam(0, Str.STR_CONFIG_PATCHES_DISABLED);
					} else {
						Global.SetDParam(1, val);
						if (pe.getVariable() instanceof CurrencyPatchVariable)
							Global.SetDParam(0, Str.STR_CONFIG_PATCHES_CURRENCY);
						else {
							if (pe.isMultistring())
								Global.SetDParam(0, pe.getString() + val + 1);
							else
								Global.SetDParam(0, pe.isNoComma() ? Str.STR_CONFIG_PATCHES_INT32 : Str.STR_7024);
						}
					}
				}
				Gfx.DrawString(30, y+1, (pe.getString())+BitOps.b2i(disabled) , 0);
				y += 11;
			}
			break;
			/* */
		}

		case WE_CLICK:
			switch(e.widget) {
			case 3: {
				int y = e.pt.y - 46 - 1;
				if (y < 0) return;

				int btn = y / 11;
				if (y % 11 > 9) return;

				final PatchPage page = _patches_page[w.as_def_d().data_1];
				if (btn >= page.entries.length) return;
				final PatchEntry pe = page.entries[btn];

				int x = e.pt.x - 5;
				if (x < 0) return;

				if (((pe.isNetworkOnly()) && !Global._networking) || // return if action is only active in network
						(!(pe.isPlayerBased()) && Global._networking && !Global._network_server)) // return if only server can change it
					return;

				if (x < 21) { // clicked on the icon on the left side. Either scroller or boolean on/off
					int val = pe.ReadPE();
					int oval = val;

					if( pe.isBoolean() )
						val ^= 1;

					// don't allow too fast scrolling
					if ((w.flags4 & Window.WF_TIMEOUT_MASK) > 2 << Window.WF_TIMEOUT_SHL) {
						Window._left_button_clicked = false;
						return;
					}

					if (x >= 10) {
						//increase
						if (pe.zeroIsDisable() && val == 0)
							val = pe.min;
						else
							val += pe.step;
						if (val > pe.max) val = pe.max;
					} else {
						// decrease
						if (val <= pe.min && pe.zeroIsDisable()) {
							val = 0;
						} else {
							val -= pe.step;
							if (val < pe.min) val = pe.min;
						}
					}

					if (val != oval) {
						w.as_def_d().data_2 = btn * 2 + 1 + ((x>=10) ? 1 : 0);
						w.flags4 |= 5 << Window.WF_TIMEOUT_SHL;
						Window._left_button_clicked = false;
					}


					if (val != oval) {
						// To make patch-changes network-safe
						// TODO if (pe.isCurrency()) val /= _currency.rate;
						// If an item is playerbased, we do not send it over the network (if any)
						if (pe.isPlayerBased()) {
							pe.WritePE(val);
						} else {
							// Else we do
							Cmd.DoCommandP( null, (byte)w.as_def_d().data_1 + ((byte)btn << 8), val, null, Cmd.CMD_CHANGE_PATCH_SETTING);
						}
						w.SetWindowDirty();

						//if (pe.click_proc != null)							pe.click_proc(val);
						// call callback function
						pe.onClick();
					}
				} else {
					if (!pe.isBoolean() && !(pe.isMultistring())) { // do not open editbox
						w.as_def_d().data_3 = btn;
						Global.SetDParam(0, pe.ReadPE());
						MiscGui.ShowQueryString(Str.STR_CONFIG_PATCHES_INT32, Str.STR_CONFIG_PATCHES_QUERY_CAPT, 10, 100, Window.WC_GAME_OPTIONS, 0);
					}
				}

				break;
			} /* */
			case 4: case 5: case 6: case 7: case 8: case 9:
				w.as_def_d().data_1 = e.widget - 4;
				Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
				w.SetWindowDirty();
				break;
			} 
			break;

		case WE_TIMEOUT:
			w.as_def_d().data_2 = 0;
			w.SetWindowDirty();
			break;

		case WE_ON_EDIT_TEXT: {
			if (e.str != null) {
				final PatchPage page = _patches_page[w.as_def_d().data_1];
				final PatchEntry pe = page.entries[w.as_def_d().data_3];
				int val;
				val = Integer.parseInt(e.str);
				// TODO if (pe.type == PE_CURRENCY) val /= _currency.rate;
				// If an item is playerbased, we do not send it over the network (if any)
				if (pe.isPlayerBased()) {
					pe.WritePE(val);
				} else {
					// Else we do
					Cmd.DoCommandP( null, (byte)w.as_def_d().data_1 + ((byte)w.as_def_d().data_3 << 8), val, null, Cmd.CMD_CHANGE_PATCH_SETTING);
				} 
				w.SetWindowDirty();

				pe.onClick();
			}
			break;
		}

		case WE_DESTROY:
			Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
			break;
		default:
			break;
		}
	}

	/** Network-safe changing of patch-settings.
	 * @param p1 various bitstuffed elements
	 * - p1 = (bit 0- 7) - the patches type (page) that is being changed (construction, network, ai)
	 * - p2 = (bit 8-15) - the actual patch (entry) being set inside the category
	 * @param p2 the new value for the patch
	 *
	 * TODO check that the new value is a valid one. Awful lot of work, but since only
	 * the server is allowed to do this, we trust it on this one :)
	 */
	public static int CmdChangePatchSetting(int x, int y, int flags, int p1, int p2)
	{
		byte pcat = (byte) BitOps.GB(p1, 0, 8);
		byte pel  = (byte) BitOps.GB(p1, 8, 8);

		if (pcat >= _patches_page.length) return Cmd.CMD_ERROR;
		if (pel >= _patches_page[pcat].entries.length) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			final PatchEntry pe = _patches_page[pcat].entries[pel];
			pe.WritePE((int)p2);

			Window.InvalidateWindow(Window.WC_GAME_OPTIONS, 0);
		}

		return 0;
		/* */
		//return Cmd.CMD_ERROR;
	}

	static final PatchEntry IConsoleGetPatch(final String name, int [] page, int [] entry)
	{
		for (page[0] = 0; page[0] < _patches_page.length; page[0]++) {
			final PatchPage pp = _patches_page[page[0]];
			for (entry[0] = 0; entry[0] < pp.entries.length; entry[0]++) {
				final PatchEntry pe = pp.entries[entry[0]];
				if (pe.nameIs(name))
					return pe;
			}
		}

		return null;
	}

	/* Those 2 functions need to be here, else we have to make some stuff non-static
	    and besides, it is also better to keep stuff like this at the same place */
	public static void IConsoleSetPatchSetting(final String name, final String value)
	{
		int [] page = {-1};
		int [] entry = {-1};
		int val;

		final PatchEntry pe = IConsoleGetPatch(name, page, entry);

		if (pe == null) {
			DefaultConsole.IConsolePrintF(DefaultConsole._icolour_warn, "'%s' is an unknown patch setting.", name);
			return;
		}

		//sscanf(value, "%d", &val);
		val = Integer.parseInt(value);

		// currency can be different on each client
		// TODO if (pe.type == PE_CURRENCY) 			val /= _currency.rate;

		// If an item is playerbased, we do not send it over the network (if any)
		if (pe.isPlayerBased()) {
			pe.WritePE(val);
		} else // Else we do
			Cmd.DoCommandP(null, page[0] + (entry[0] << 8), val, null, Cmd.CMD_CHANGE_PATCH_SETTING);

		{
			String tval = value;
			if (pe.isBoolean()) 
				tval = (val == 1) ? "on" : "off";

			DefaultConsole.IConsolePrintF(DefaultConsole._icolour_warn, "'%s' changed to:  %s", name, tval);
		}
	}


	public static void IConsoleGetPatchSetting(final String name)
	{

		String value;
		int [] page = {-1};
		int [] entry = {-1};
		final PatchEntry pe = IConsoleGetPatch(name, page, entry);

		// We did not find the patch setting 
		if (pe == null) {
			DefaultConsole.IConsolePrintF(DefaultConsole._icolour_warn, "'%s' is an unknown patch setting.", name);
			return;
		}

		if (pe.isBoolean()) {
			value = (pe.ReadPE() != 0) ? "on" : "off";
		} else {
			value = Integer.toString(pe.ReadPE());
		}

		DefaultConsole.IConsolePrintF(DefaultConsole._icolour_warn, "Current value for '%s' is: '%s'", name, value);
	}

	static final Widget _patches_selection_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    10,     0,    10,     0,    13, Str.STR_00C5,												Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    10,    11,   369,     0,    13, Str.STR_CONFIG_PATCHES_CAPTION,			Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    10,     0,   369,    14,    41, 0x0,															Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    10,     0,   369,    42,   336, 0x0,															Str.STR_NULL),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,    10,    96,    16,    27, Str.STR_CONFIG_PATCHES_GUI,					Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,    97,   183,    16,    27, Str.STR_CONFIG_PATCHES_CONSTRUCTION,	Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,   184,   270,    16,    27, Str.STR_CONFIG_PATCHES_VEHICLES,			Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,   271,   357,    16,    27, Str.STR_CONFIG_PATCHES_STATIONS,			Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,    10,    96,    28,    39, Str.STR_CONFIG_PATCHES_ECONOMY,			Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,    97,   183,    28,    39, Str.STR_CONFIG_PATCHES_AI,						Str.STR_NULL),
	};

	static final WindowDesc _patches_selection_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 370, 337,
			Window.WC_GAME_OPTIONS,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_patches_selection_widgets,
			SettingsGui::PatchesSelectionWndProc
			);

	static void ShowPatchesSelection()
	{
		Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
		Window.AllocateWindowDesc(_patches_selection_desc);
	}

	//enum {
	static final int NEWGRF_WND_PROC_OFFSET_TOP_WIDGET = 14;
	static final int NEWGRF_WND_PROC_ROWSIZE = 14;
	//};
	static GRFFile _sel_grffile;

	static void NewgrfWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int x, y = NEWGRF_WND_PROC_OFFSET_TOP_WIDGET;
			int i = 0;
			GRFFile c = GRFFile._first_grffile;

			w.DrawWindowWidgets();

			if (GRFFile._first_grffile == null) { // no grf sets installed
				Gfx.DrawStringMultiCenter(140, 210, Str.STR_NEWGRF_NO_FILES_INSTALLED, 250);
				break;
			}

			// draw list of all grf files
			while (c != null) {
				if (i >= w.vscroll.pos) { // draw files according to scrollbar position
					boolean h = (_sel_grffile == c);
					// show highlighted item with a different background and highlighted text
					if (h) Gfx.GfxFillRect(1, y + 1, 267, y + 12, 156);
					// XXX - will be grf name later
					Gfx.DoDrawString(c.getFilename(), 25, y + 2, h ? 0xC : 0x10);
					Gfx.DrawSprite(Sprite.SPRITE_PALETTE(Sprite.SPR_SQUARE | Sprite.PALETTE_TO_RED), 5, y + 2);
					y += NEWGRF_WND_PROC_ROWSIZE;
				}

				c = c.getNext();
				if (++i == w.vscroll.getCap() + w.vscroll.pos) break; // stop after displaying 12 items
			}

			// TODO Gfx.DoDrawString(_sel_grffile.setname, 120, 200, 0x01); // draw grf name

			if (_sel_grffile == null) { // no grf file selected yet
				Gfx.DrawStringMultiCenter(140, 210, Str.STR_NEWGRF_TIP, 250);
			} else {
				// draw filename
				x = Gfx.DrawString(5, 199, Str.STR_NEWGRF_FILENAME, 0);
				Gfx.DoDrawString(_sel_grffile.getFilename(), x + 2, 199, 0x01);

				// draw grf id
				x = Gfx.DrawString(5, 209, Str.STR_NEWGRF_GRF_ID, 0);
				Strings._userstring = new BinaryString( String.format("%08X", _sel_grffile.getGrfid()) );
				Gfx.DrawString(x + 2, 209, Strings.STR_SPEC_USERSTRING, 0x01);
			}

		} break;

		case WE_CLICK:
			switch(e.widget) {
			case 3: { // select a grf file
				int y = (e.pt.y - NEWGRF_WND_PROC_OFFSET_TOP_WIDGET) / NEWGRF_WND_PROC_ROWSIZE;

				if (y >= w.vscroll.getCap()) return; // click out of bounds

				y += w.vscroll.pos;

				if (y >= w.vscroll.getCount()) return;

				_sel_grffile = GRFFile._first_grffile;
				// get selected grf-file
				while (y-- != 0) _sel_grffile = _sel_grffile.getNext();

				w.SetWindowDirty();
			} break;
			case 9: /* Cancel button */
				Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
				break;
			} break;

			/* Parameter edit box not used yet
		case WindowEvents.WE_TIMEOUT:
			w.as_def_d().data_2 = 0;
			w.SetWindowDirty();
			break;

		case WindowEvents.WE_ON_EDIT_TEXT: {
			if (*e.edittext.str) {
				w.SetWindowDirty();
			}
			break;
		}
			 */
		case WE_DESTROY:
			_sel_grffile = null;
			Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
			break;
		default:
			break;
		}
	}

	static final Widget _newgrf_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,										Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   279,     0,    13, Str.STR_NEWGRF_SETTINGS_CAPTION,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   279,   183,   276, 0x0,													Str.STR_NULL),

			new Widget(     Window.WWT_MATRIX,   Window.RESIZE_NONE,    14,     0,   267,    14,   182, 0xC01,/*small rows*/					Str.STR_NEWGRF_TIP),
			new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_NONE,    14,   268,   279,    14,   182, 0x0,													Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   147,   158,   244,   255, Str.STR_0188,	Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   159,   170,   244,   255, Str.STR_0189,	Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   175,   274,   244,   255, Str.STR_NEWGRF_SET_PARAMETERS,		Str.STR_NULL),

			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,     5,   138,   261,   272, Str.STR_NEWGRF_APPLY_CHANGES,		Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,   142,   274,   261,   272, Str.STR_012E_CANCEL,							Str.STR_NULL),

	};

	static final WindowDesc _newgrf_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 280, 277,
			Window.WC_GAME_OPTIONS,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_newgrf_widgets,
			SettingsGui::NewgrfWndProc
			);

	static void ShowNewgrf()
	{

		GRFFile c;
		Window w;
		int count;

		Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
		w = Window.AllocateWindowDesc(_newgrf_desc);

		count = 0;
		for (c = GRFFile._first_grffile; c != null; c = c.getNext()) count++;

		w.vscroll.setCap(12);
		w.vscroll.setCount(count);
		w.vscroll.pos = 0;
		w.disabled_state = (1 << 5) | (1 << 6) | (1 << 7);

	}

	/* state: 0 = none clicked, 0x01 = first clicked, 0x02 = second clicked */
	public static void DrawArrowButtons(int x, int y, int state)
	{
		Gfx.DrawFrameRect(x, y+1, x+9, y+9, 3, (state & 0x01) != 0 ? Window.FR_LOWERED : 0);
		Gfx.DrawFrameRect(x+10, y+1, x+19, y+9, 3, (state & 0x02) != 0 ? Window.FR_LOWERED : 0);
		Gfx.DrawStringCentered(x+5, y+1, Str.STR_6819, 0);
		Gfx.DrawStringCentered(x+15, y+1, Str.STR_681A, 0);
	}

	static char [] _str_separator = new char[2];

	static void CustCurrencyWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int x=35, y=20, i=0;
			int clk = w.as_def_d().data_1;
			w.DrawWindowWidgets();

			// exchange rate
			DrawArrowButtons(10, y, (clk >> (i*2)) & 0x03);
			Global.SetDParam(0, 1);
			Global.SetDParam(1, 1);
			Gfx.DrawString(x, y + 1, Str.STR_CURRENCY_EXCHANGE_RATE, 0);
			x = 35;
			y+=12;
			i++;

			// separator
			Gfx.DrawFrameRect(10, y+1, 29, y+9, 0, ((clk >> (i*2)) & 0x03) != 0 ? Window.FR_LOWERED : 0);
			x = Gfx.DrawString(x, y + 1, Str.STR_CURRENCY_SEPARATOR, 0);
			Gfx.DoDrawString( String.valueOf( _str_separator ), x + 4, y + 1, 6);
			x = 35;
			y+=12;
			i++;

			// prefix
			Gfx.DrawFrameRect(10, y+1, 29, y+9, 0, ((clk >> (i*2)) & 0x03) != 0 ? Window.FR_LOWERED : 0);
			x = Gfx.DrawString(x, y + 1, Str.STR_CURRENCY_PREFIX, 0);
			Gfx.DoDrawString(_custom_currency().prefix, x + 4, y + 1, 6);
			x = 35;
			y+=12;
			i++;

			// suffix
			Gfx.DrawFrameRect(10, y+1, 29, y+9, 0, ((clk >> (i*2)) & 0x03) != 0 ? Window.FR_LOWERED : 0);
			x = Gfx.DrawString(x, y + 1, Str.STR_CURRENCY_SUFFIX, 0);
			Gfx.DoDrawString(_custom_currency().suffix, x + 4, y + 1, 6);
			x = 35;
			y+=12;
			i++;

			// switch to euro
			DrawArrowButtons(10, y, (clk >> (i*2)) & 0x03);
			Global.SetDParam(0, (int) _custom_currency().to_euro);
			Gfx.DrawString(x, y + 1, (_custom_currency().to_euro != Currency.CF_NOEURO) ? Str.STR_CURRENCY_SWITCH_TO_EURO : Str.STR_CURRENCY_SWITCH_TO_EURO_NEVER, 0);
			x = 35;
			y+=12;
			i++;

			// Preview
			y+=12;
			Global.SetDParam(0, 10000);
			Gfx.DrawString(x, y + 1, Str.STR_CURRENCY_PREVIEW, 0);
		} break;

		case WE_CLICK: {
			boolean edittext = false;
			int line = (e.pt.y - 20)/12;
			int len = 0;
			int x = e.pt.x;
			/*StringID*/ int str = 0;

			switch ( line ) {
			case 0: // rate
				if ( BitOps.IS_INT_INSIDE(x, 10, 30) ) { // clicked buttons
					if (x < 20) {
						if (_custom_currency().rate > 1) _custom_currency().rate--;
						w.as_def_d().data_1 =  (1 << (line * 2 + 0));
					} else {
						if (_custom_currency().rate < 5000) _custom_currency().rate++;
						w.as_def_d().data_1 =  (1 << (line * 2 + 1));
					}
				} else { // enter text
					Global.SetDParam(0, _custom_currency().rate);
					str = Str.STR_CONFIG_PATCHES_INT32;
					len = 4;
					edittext = true;
				}
				break;
			case 1: // separator
				if ( BitOps.IS_INT_INSIDE(x, 10, 30) )  // clicked button
					w.as_def_d().data_1 =  (1 << (line * 2 + 1));
				str = Strings.BindCString( String.valueOf( _str_separator ) );
				len = 1;
				edittext = true;
				break;
			case 2: // prefix
				if ( BitOps.IS_INT_INSIDE(x, 10, 30) )  // clicked button
					w.as_def_d().data_1 =  (1 << (line * 2 + 1));
				str = Strings.BindCString(_custom_currency().prefix);
				len = 12;
				edittext = true;
				break;
			case 3: // suffix
				if ( BitOps.IS_INT_INSIDE(x, 10, 30) )  // clicked button
					w.as_def_d().data_1 =  (1 << (line * 2 + 1));
				str = Strings.BindCString(_custom_currency().suffix);
				len = 12;
				edittext = true;
				break;
			case 4: // to euro
				if ( BitOps.IS_INT_INSIDE(x, 10, 30) ) { // clicked buttons
					if (x < 20) {
						_custom_currency().to_euro = (_custom_currency().to_euro <= 2000) ?
								Currency.CF_NOEURO : _custom_currency().to_euro - 1;
						w.as_def_d().data_1 = (1 << (line * 2 + 0));
					} else {
						_custom_currency().to_euro =
								BitOps.clamp((int)_custom_currency().to_euro + 1, 2000, Global.MAX_YEAR_END_REAL);
						w.as_def_d().data_1 = (1 << (line * 2 + 1));
					}
				} else { // enter text
					Global.SetDParam(0, (int)_custom_currency().to_euro);
					str = Str.STR_CONFIG_PATCHES_INT32;
					len = 4;
					edittext = true;
				}
				break;
			}

			if (edittext) {
				w.as_def_d().data_2 = line;
				MiscGui.ShowQueryString(
						new StringID( str ),
						new StringID( Str.STR_CURRENCY_CHANGE_PARAMETER ),
						len + 1, // maximum number of characters OR
						250, // characters up to this width pixels, whichever is satisfied first
						w.getWindow_class(),
						w.window_number);
			}

			w.flags4 |= 5 << Window.WF_TIMEOUT_SHL;
			w.SetWindowDirty();
		} break;

		case WE_ON_EDIT_TEXT: {
			int val;
			final char [] b = e.str.toCharArray();
			switch (w.as_def_d().data_2) {
			case 0: /* Exchange rate */
				val = Integer.parseInt(e.str);
				val = BitOps.clamp(val, 1, 5000);
				_custom_currency().rate = val;
				break;

			case 1: /* Thousands seperator */
				_custom_currency().separator = (b[0] == '\0') ? ' ' : b[0];
				_str_separator = b;
				break;

			case 2: /* Currency prefix */
				_custom_currency().prefix = e.str;
				break;

			case 3: /* Currency suffix */
				_custom_currency().suffix = e.str;
				break;

			case 4: /* Year to switch to euro */
				val = Integer.parseInt(e.str);
				val = BitOps.clamp(val, 1999, Global.MAX_YEAR_END_REAL);
				if (val == 1999) val = 0;
				_custom_currency().to_euro = val;
				break;
			}
			Hal.MarkWholeScreenDirty();


		} break;

		case WE_TIMEOUT:
			w.as_def_d().data_1 = 0;
			w.SetWindowDirty();
			break;

		case WE_DESTROY:
			Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
			Hal.MarkWholeScreenDirty();
			break;
		default:
			break;
		}
	}

	static final Widget _cust_currency_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,						Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   229,     0,    13, Str.STR_CURRENCY_WINDOW,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   229,    14,   119, 0x0,									Str.STR_NULL),

	};

	static final WindowDesc _cust_currency_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 230, 120,
			Window.WC_CUSTOM_CURRENCY, 0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_cust_currency_widgets,
			SettingsGui::CustCurrencyWndProc
			);

	static void ShowCustCurrency()
	{
		_str_separator[0] = _custom_currency().separator;
		_str_separator[1] = '\0';

		Window.DeleteWindowById(Window.WC_CUSTOM_CURRENCY, 0);
		Window.AllocateWindowDesc(_cust_currency_desc);
	}


}
