package game;

public class SettingsGui 
{

	/* $Id: settings_gui.c 3323 2005-12-20 21:24:50Z Darkvater $ */





















	static int _difficulty_click_a;
	static int _difficulty_click_b;
	static byte _difficulty_timeout;

	static final StringID _distances_dropdown[] = {
		Str.STR_0139_IMPERIAL_MILES,
		Str.STR_013A_METRIC_KILOMETERS,
		INVALID_STRING_ID
	};

	static final StringID _driveside_dropdown[] = {
		Str.STR_02E9_DRIVE_ON_LEFT,
		Str.STR_02EA_DRIVE_ON_RIGHT,
		INVALID_STRING_ID
	};

	static final StringID _autosave_dropdown[] = {
		Str.STR_02F7_OFF,
		Str.STR_AUTOSAVE_1_MONTH,
		Str.STR_02F8_EVERY_3_MONTHS,
		Str.STR_02F9_EVERY_6_MONTHS,
		Str.STR_02FA_EVERY_12_MONTHS,
		INVALID_STRING_ID,
	};

	static final StringID _designnames_dropdown[] = {
		Str.STR_02BE_DEFAULT,
		Str.STR_02BF_CUSTOM,
		INVALID_STRING_ID
	};

	static StringID *BuildDynamicDropdown(StringID base, int num)
	{
		static StringID buf[32 + 1];
		StringID *p = buf;
		while (--num>=0) *p++ = base++;
		*p = INVALID_STRING_ID;
		return buf;
	}

	static int GetCurRes()
	{
		int i;

		for (i = 0; i != _num_resolutions; i++) {
			if (_resolutions[i][0] == _screen.width &&
					_resolutions[i][1] == _screen.height) {
				break;
			}
		}
		return i;
	}

	static inline boolean RoadVehiclesAreBuilt()
	{
		final Vehicle  v;

		FOR_ALL_VEHICLES(v) {
			if (v.type == Vehicle.VEH_Road) return true;
		}
		return false;
	}

	static void GameOptionsWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT: {
			int i;
			StringID str = Str.STR_02BE_DEFAULT;
			w.disabled_state = (_vehicle_design_names & 1) ? (++str, 0) : (1 << 21);
			Global.SetDParam(0, str);
			Global.SetDParam(1, _currency_string_list[GameOptions._opt_ptr.currency]);
			Global.SetDParam(2, GameOptions._opt_ptr.kilometers + Str.STR_0139_IMPERIAL_MILES);
			Global.SetDParam(3, Str.STR_02E9_DRIVE_ON_LEFT + GameOptions._opt_ptr.road_side);
			Global.SetDParam(4, Str.STR_TOWNNAME_ORIGINAL_ENGLISH + GameOptions._opt_ptr.town_name);
			Global.SetDParam(5, _autosave_dropdown[GameOptions._opt_ptr.autosave]);
			Global.SetDParam(6, SPECStr.STR_LANGUAGE_START + _dynlang.curr);
			i = GetCurRes();
			Global.SetDParam(7, i == _num_resolutions ? Str.STR_RES_OTHER : SPECStr.STR_RESOLUTION_START + i);
			Global.SetDParam(8, SPECStr.STR_SCREENSHOT_START + _cur_screenshot_format);
			(_fullscreen) ? SETBIT(w.click_state, 28) : CLRBIT(w.click_state, 28); // fullscreen button

			DrawWindowWidgets(w);
			DrawString(20, 175, Str.STR_OPTIONS_FULLSCREEN, 0); // fullscreen
		}	break;

		case WindowEvents.WE_CLICK:
			switch (e.click.widget) {
			case 4: case 5: /* Setup currencies dropdown */
				ShowDropDownMenu(w, _currency_string_list, GameOptions._opt_ptr.currency, 5, Global._game_mode == GameModes.GM_MENU ? 0 : ~GetMaskOfAllowedCurrencies(), 0);
				return;
			case 7: case 8: /* Setup distance unit dropdown */
				ShowDropDownMenu(w, _distances_dropdown, GameOptions._opt_ptr.kilometers, 8, 0, 0);
				return;
			case 10: case 11: { /* Setup road-side dropdown */
				int i = 0;

				/* You can only change the drive side if you are in the menu or ingame with
				 * no vehicles present. In a networking game only the server can change it */
				if ((Global._game_mode != GameModes.GM_MENU && RoadVehiclesAreBuilt()) || (_networking && !_network_server))
					i = (-1) ^ (1 << GameOptions._opt_ptr.road_side); // disable the other value

				ShowDropDownMenu(w, _driveside_dropdown, GameOptions._opt_ptr.road_side, 11, i, 0);
			} return;
			case 13: case 14: { /* Setup townname dropdown */
				int i = GameOptions._opt_ptr.town_name;
				ShowDropDownMenu(w, BuildDynamicDropdown(Str.STR_TOWNNAME_ORIGINAL_ENGLISH, SPECStr.STR_TOWNNAME_LAST - SPECStr.STR_TOWNNAME_START + 1), i, 14, (Global._game_mode == GameModes.GM_MENU) ? 0 : (-1) ^ (1 << i), 0);
				return;
			}
			case 16: case 17: /* Setup autosave dropdown */
				ShowDropDownMenu(w, _autosave_dropdown, GameOptions._opt_ptr.autosave, 17, 0, 0);
				return;
			case 19: case 20: /* Setup customized vehicle-names dropdown */
				ShowDropDownMenu(w, _designnames_dropdown, (_vehicle_design_names & 1) ? 1 : 0, 20, (_vehicle_design_names & 2) ? 0 : 2, 0);
				return;
			case 21: /* Save customized vehicle-names to disk */
				return;
			case 23: case 24: /* Setup interface language dropdown */
				ShowDropDownMenu(w, _dynlang.dropdown, _dynlang.curr, 24, 0, 0);
				return;
			case 26: case 27: /* Setup resolution dropdown */
				ShowDropDownMenu(w, BuildDynamicDropdown(SPECStr.STR_RESOLUTION_START, _num_resolutions), GetCurRes(), 27, 0, 0);
				return;
			case 28: /* Click fullscreen on/off */
				(_fullscreen) ? CLRBIT(w.click_state, 28) : SETBIT(w.click_state, 28);
				ToggleFullScreen(!_fullscreen); // toggle full-screen on/off
				SetWindowDirty(w);
				return;
			case 30: case 31: /* Setup screenshot format dropdown */
				ShowDropDownMenu(w, BuildDynamicDropdown(SPECStr.STR_SCREENSHOT_START, _num_screenshot_formats), _cur_screenshot_format, 31, 0, 0);
				return;
			}
			break;

		case WindowEvents.WE_DROPDOWN_SELECT:
			switch (e.dropdown.button) {
			case 20: /* Vehicle design names */
				if (e.dropdown.index == 0) {
					DeleteCustomEngineNames();
					MarkWholeScreenDirty();
				} else if (!(_vehicle_design_names & 1)) {
					LoadCustomEngineNames();
					MarkWholeScreenDirty();
				}
				break;
			case 5: /* Currency */
				if (e.dropdown.index == 23)
					ShowCustCurrency();
				GameOptions._opt_ptr.currency = e.dropdown.index;
				MarkWholeScreenDirty();
				break;
			case 8: /* Distance units */
				GameOptions._opt_ptr.kilometers = e.dropdown.index;
				MarkWholeScreenDirty();
				break;
			case 11: /* Road side */
				if (GameOptions._opt_ptr.road_side != e.dropdown.index) { // only change if setting changed
					DoCommandP(0, e.dropdown.index, 0, null, Cmd.CMD_SET_ROAD_DRIVE_SIDE | Cmd.CMD_MSG(Str.STR_00B4_CAN_T_DO_THIS));
					MarkWholeScreenDirty();
				}
				break;
			case 14: /* Town names */
				if (Global._game_mode == GameModes.GM_MENU) {
					GameOptions._opt_ptr.town_name = e.dropdown.index;
					Window.InvalidateWindow(Window.WC_GAME_OPTIONS, 0);
				}
				break;
			case 17: /* Autosave options */
				GameOptions._opt_ptr.autosave = e.dropdown.index;
				SetWindowDirty(w);
				break;
			case 24: /* Change interface language */
				ReadLanguagePack(e.dropdown.index);
				MarkWholeScreenDirty();
				break;
			case 27: /* Change resolution */
				if (e.dropdown.index < _num_resolutions && ChangeResInGame(_resolutions[e.dropdown.index][0],_resolutions[e.dropdown.index][1]))
					SetWindowDirty(w);
				break;
			case 31: /* Change screenshot format */
				SetScreenshotFormat(e.dropdown.index);
				SetWindowDirty(w);
				break;
			}
			break;

		case WindowEvents.WE_DESTROY:
			Window.DeleteWindowById(Window.WC_CUSTOM_CURRENCY, 0);
			break;
		}

	}

	/** Change the side of the road vehicles drive on (server only).
	 * @param x,y unused
	 * @param p1 the side of the road; 0 = left side and 1 = right side
	 * @param p2 unused
	 */
	int CmdSetRoadDriveSide(int x, int y, int flags, int p1, int p2)
	{
		/* Check boundaries and you can only change this if NO vehicles have been built yet,
		 * except in the intro-menu where of course it's always possible to do so. */
		if (p1 > 1 || (Global._game_mode != GameModes.GM_MENU && RoadVehiclesAreBuilt())) return Cmd.CMD_ERROR;

		if (flags & Cmd.DC_EXEC) {
			GameOptions._opt_ptr.road_side = p1;
			Window.InvalidateWindow(Window.WC_GAME_OPTIONS,0);
		}
		return 0;
	}

	static final Widget _gameGameOptions._options_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   369,     0,    13, Str.STR_00B1_GAME_OPTIONS,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   369,    14,   238, 0x0,											Str.STR_NULL},
	{      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,    10,   179,    20,    55, Str.STR_02E0_CURRENCY_UNITS,	Str.STR_NULL},
	{          Window.WWT_6,   Window.RESIZE_NONE,    14,    20,   169,    34,    45, Str.STR_02E1,								Str.STR_02E2_CURRENCY_UNITS_SELECTION},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   158,   168,    35,    44, Str.STR_0225,								Str.STR_02E2_CURRENCY_UNITS_SELECTION},
	{      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,   190,   359,    20,    55, Str.STR_02E3_DISTANCE_UNITS,	Str.STR_NULL},
	{          Window.WWT_6,   Window.RESIZE_NONE,    14,   200,   349,    34,    45, Str.STR_02E4,								Str.STR_02E5_DISTANCE_UNITS_SELECTION},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   338,   348,    35,    44, Str.STR_0225,								Str.STR_02E5_DISTANCE_UNITS_SELECTION},
	{      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,    10,   179,    62,    97, Str.STR_02E6_ROAD_VEHICLES,	Str.STR_NULL},
	{          Window.WWT_6,   Window.RESIZE_NONE,    14,    20,   169,    76,    87, Str.STR_02E7,								Str.STR_02E8_SELEAcceptedCargo.CT_SIDE_OF_ROAD_FOR},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   158,   168,    77,    86, Str.STR_0225,								Str.STR_02E8_SELEAcceptedCargo.CT_SIDE_OF_ROAD_FOR},
	{      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,   190,   359,    62,    97, Str.STR_02EB_TOWN_NAMES,			Str.STR_NULL},
	{          Window.WWT_6,   Window.RESIZE_NONE,    14,   200,   349,    76,    87, Str.STR_02EC,								Str.STR_02ED_SELEAcceptedCargo.CT_STYLE_OF_TOWN_NAMES},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   338,   348,    77,    86, Str.STR_0225,								Str.STR_02ED_SELEAcceptedCargo.CT_STYLE_OF_TOWN_NAMES},
	{      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,    10,   179,   104,   139, Str.STR_02F4_AUTOSAVE,				Str.STR_NULL},
	{          Window.WWT_6,   Window.RESIZE_NONE,    14,    20,   169,   118,   129, Str.STR_02F5,								Str.STR_02F6_SELEAcceptedCargo.CT_INTERVAL_BETWEEN},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   158,   168,   119,   128, Str.STR_0225,								Str.STR_02F6_SELEAcceptedCargo.CT_INTERVAL_BETWEEN},

	{      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,    10,   359,   194,   228, Str.STR_02BC_VEHICLE_DESIGN_NAMES,				Str.STR_NULL},
	{          Window.WWT_6,   Window.RESIZE_NONE,    14,    20,   119,   207,   218, Str.STR_02BD,								Str.STR_02C1_VEHICLE_DESIGN_NAMES_SELECTION},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   108,   118,   208,   217, Str.STR_0225,								Str.STR_02C1_VEHICLE_DESIGN_NAMES_SELECTION},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   130,   349,   207,   218, Str.STR_02C0_SAVE_CUSTOM_NAMES,	Str.STR_02C2_SAVE_CUSTOMIZED_VEHICLE},

	{      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,   190,   359,   104,   139, Str.STR_OPTIONS_LANG,				Str.STR_NULL},
	{          Window.WWT_6,   Window.RESIZE_NONE,    14,   200,   349,   118,   129, Str.STR_OPTIONS_LANG_CBO,		Str.STR_OPTIONS_LANG_TIP},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   338,   348,   119,   128, Str.STR_0225,								Str.STR_OPTIONS_LANG_TIP},

	{      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,    10,   179,   146,   190, Str.STR_OPTIONS_RES,					Str.STR_NULL},
	{          Window.WWT_6,   Window.RESIZE_NONE,    14,    20,   169,   160,   171, Str.STR_OPTIONS_RES_CBO,			Str.STR_OPTIONS_RES_TIP},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   158,   168,   161,   170, Str.STR_0225,								Str.STR_OPTIONS_RES_TIP},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   149,   169,   176,   184, Str.STR_EMPTY,								Str.STR_OPTIONS_FULLSCREEN_TIP},

	{      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,   190,   359,   146,   190, Str.STR_OPTIONS_SCREENSHOT_FORMAT,				Str.STR_NULL},
	{          Window.WWT_6,   Window.RESIZE_NONE,    14,   200,   349,   160,   171, Str.STR_OPTIONS_SCREENSHOT_FORMAT_CBO,		Str.STR_OPTIONS_SCREENSHOT_FORMAT_TIP},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   338,   348,   161,   170, Str.STR_0225,								Str.STR_OPTIONS_SCREENSHOT_FORMAT_TIP},

	{   WIDGETS_END},
	};

	static final WindowDesc _gameGameOptions._options_desc = {
		Window.WDP_CENTER, Window.WDP_CENTER, 370, 239,
		Window.WC_GAME_OPTIONS,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_gameGameOptions._options_widgets,
		GameOptionsWndProc
	};


	void ShowGameOptions()
	{
		Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
		AllocateWindowDesc(&_gameGameOptions._options_desc);
	}

	class {
		int min;
		int max;
		int step;
		StringID str;
	} GameSettingData;

	static final GameSettingData _game_setting_info[] = {
		{  0,   7,  1, Str.STR_NULL},
		{  0,   3,  1, Str.STR_6830_IMMEDIATE},
		{  0,   2,  1, Str.STR_6816_LOW},
		{  0,   3,  1, Str.STR_26816_NONE},
		{100, 500, 50, Str.STR_NULL},
		{  2,   4,  1, Str.STR_NULL},
		{  0,   2,  1, Str.STR_6820_LOW},
		{  0,   4,  1, Str.STR_681B_VERY_SLOW},
		{  0,   2,  1, Str.STR_6820_LOW},
		{  0,   2,  1, Str.STR_6823_NONE},
		{  0,   3,  1, Str.STR_6826_X1_5},
		{  0,   2,  1, Str.STR_6820_LOW},
		{  0,   3,  1, Str.STR_682A_VERY_FLAT},
		{  0,   3,  1, Str.STR_VERY_LOW},
		{  0,   1,  1, Str.STR_682E_STEADY},
		{  0,   1,  1, Str.STR_6834_AT_END_OF_LINE_AND_AT_STATIONS},
		{  0,   1,  1, Str.STR_6836_OFF},
		{  0,   2,  1, Str.STR_6839_PERMISSIVE},
	};

	static inline boolean GetBitAndShift(int *b)
	{
		int x = *b;
		*b >>= 1;
		return BitOps.HASBIT(x, 0);
	}

	/*
		A: competitors
		B: start time in months / 3
		C: town count (2 = high, 0 = low)
		D: industry count (3 = high, 0 = none)
		E: inital loan / 1000 (in GBP)
		F: interest rate
		G: running costs (0 = low, 2 = high)
		H: finalruction speed of competitors (0 = very slow, 4 = very fast)
		I: intelligence (0-2)
		J: breakdowns(0 = off, 2 = normal)
		K: subsidy multiplier (0 = 1.5, 3 = 4.0)
		L: finalruction cost (0-2)
		M: terrain type (0 = very flat, 3 = mountainous)
		N: amount of water (0 = very low, 3 = high)
		O: economy (0 = steady, 1 = fluctuating)
		P: Train reversing (0 = end of line + stations, 1 = end of line)
		Q: disasters
		R: area restructuring (0 = permissive, 2 = hostile)
	*/
	static final int _default_game_diff[3][GAME_DIFFICULTY_NUM] = { /*
		 A, B, C, D,   E, F, G, H, I, J, K, L, M, N, O, P, Q, R*/
		{2, 2, 1, 3, 300, 2, 0, 2, 0, 1, 2, 0, 1, 0, 0, 0, 0, 0},	//easy
		{4, 1, 1, 2, 150, 3, 1, 3, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1},	//medium
		{7, 0, 2, 2, 100, 4, 1, 3, 2, 2, 0, 2, 3, 2, 1, 1, 1, 2},	//hard
	};

	void SetDifficultyLevel(int mode, GameOptions *gmGameOptions._opt)
	{
		int i;
		assert(mode <= 3);

		gmGameOptions._opt.diff_level = mode;
		if (mode != 3) { // not custom
			for (i = 0; i != GAME_DIFFICULTY_NUM; i++)
				((int*)&gmGameOptions._opt.diff)[i] = _default_game_diff[mode][i];
		}
	}

	extern void StartupEconomy();

	enum {
		GAMEDIFF_WND_TOP_OFFSET = 45,
		GAMEDIFF_WND_ROWSIZE    = 9
	};

	// Temporary holding place of values in the difficulty window until 'Save' is clicked
	static GameOptions GameOptions._opt_mod_temp;
	// 0x383E = (1 << 13) | (1 << 12) | (1 << 11) | (1 << 5) | (1 << 4) | (1 << 3) | (1 << 2) | (1 << 1)
	#define DIFF_INGAME_DISABLED_BUTTONS 0x383E

	static void GameDifficultyWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
			case WindowEvents.WE_CREATE: /* Setup disabled buttons when creating window */
			// disable all other difficulty buttons during gameplay except for 'custom'
			w.disabled_state = (Global._game_mode != GameModes.GM_NORMAL) ? 0 : (1 << 3) | (1 << 4) | (1 << 5) | (1 << 6);

			if (Global._game_mode == GameModes.GM_EDITOR) SETBIT(w.disabled_state, 7);

			if (_networking) {
				SETBIT(w.disabled_state, 7); // disable highscore chart in multiplayer
				if (!_network_server)
					SETBIT(w.disabled_state, 10); // Disable save-button in multiplayer (and if client)
			}
			break;
		case WindowEvents.WE_PAINT: {
			int click_a, click_b, disabled;
			int i;
			int y, value;

			w.click_state = (1 << 3) << GameOptions._opt_mod_temp.diff_level; // have current difficulty button clicked
			DrawWindowWidgets(w);

			click_a = _difficulty_click_a;
			click_b = _difficulty_click_b;

			/* XXX - Disabled buttons in normal gameplay. Bitshifted for each button to see if
			 * that bit is set. If it is set, the button is disabled */
			disabled = (Global._game_mode == GameModes.GM_NORMAL) ? DIFF_INGAME_DISABLED_BUTTONS : 0;

			y = GAMEDIFF_WND_TOP_OFFSET;
			for (i = 0; i != GAME_DIFFICULTY_NUM; i++) {
				Gfx.DrawFrameRect( 5, y,  5 + 8, y + 8, 3, GetBitAndShift(&click_a) ? (1 << 5) : 0);
				Gfx.DrawFrameRect(15, y, 15 + 8, y + 8, 3, GetBitAndShift(&click_b) ? (1 << 5) : 0);
				if (GetBitAndShift(&disabled) || (_networking && !_network_server)) {
					int color = PALETTE_MODIFIER_GREYOUT | Global._color_list[3].unk2;
					Gfx.GfxFillRect( 6, y + 1,  6 + 8, y + 8, color);
					Gfx.GfxFillRect(16, y + 1, 16 + 8, y + 8, color);
				}

				DrawStringCentered(10, y, Str.STR_6819, 0);
				DrawStringCentered(20, y, Str.STR_681A, 0);


				value = _game_setting_info[i].str + ((int*)&GameOptions._opt_mod_temp.diff)[i];
				if (i == 4) value *= 1000; // XXX - handle currency option
				Global.SetDParam(0, value);
				DrawString(30, y, Str.STR_6805_MAXIMUM_NO_COMPETITORS + i, 0);

				y += GAMEDIFF_WND_ROWSIZE + 2; // space items apart a bit
			}
		} break;

		case WindowEvents.WE_CLICK:
			switch (e.click.widget) {
			case 8: { /* Difficulty settings widget, decode click */
				final GameSettingData *info;
				int x, y;
				int btn, dis;
				int val;

				// Don't allow clients to make any changes
				if  (_networking && !_network_server)
					return;

				x = e.click.pt.x - 5;
				if (!BitOps.IS_INT_INSIDE(x, 0, 21)) // Button area
					return;

				y = e.click.pt.y - GAMEDIFF_WND_TOP_OFFSET;
				if (y < 0)
					return;

				// Get button from Y coord.
				btn = y / (GAMEDIFF_WND_ROWSIZE + 2);
				if (btn >= GAME_DIFFICULTY_NUM || y % (GAMEDIFF_WND_ROWSIZE + 2) >= 9)
					return;

				// Clicked disabled button?
				dis = (Global._game_mode == GameModes.GM_NORMAL) ? DIFF_INGAME_DISABLED_BUTTONS : 0;

				if (BitOps.HASBIT(dis, btn))
					return;

				_difficulty_timeout = 5;

				val = ((int*)&GameOptions._opt_mod_temp.diff)[btn];

				info = &_game_setting_info[btn]; // get information about the difficulty setting
				if (x >= 10) {
					// Increase button clicked
					val = Math.min(val + info.step, info.max);
					SETBIT(_difficulty_click_b, btn);
				} else {
					// Decrease button clicked
					val = Math.max(val - info.step, info.min);
					SETBIT(_difficulty_click_a, btn);
				}

				// save value in temporary variable
				((int*)&GameOptions._opt_mod_temp.diff)[btn] = val;
				SetDifficultyLevel(3, &GameOptions._opt_mod_temp); // set difficulty level to custom
				SetWindowDirty(w);
			}	break;
			case 3: case 4: case 5: case 6: /* Easy / Medium / Hard / Custom */
				// temporarily change difficulty level
				SetDifficultyLevel(e.click.widget - 3, &GameOptions._opt_mod_temp);
				SetWindowDirty(w);
				break;
			case 7: /* Highscore Table */
				ShowHighscoreTable(GameOptions._opt_mod_temp.diff_level, -1);
				break;
			case 10: { /* Save button - save changes */
				int btn, val;
				for (btn = 0; btn != GAME_DIFFICULTY_NUM; btn++) {
					val = ((int*)&GameOptions._opt_mod_temp.diff)[btn];
					// if setting has changed, change it
					if (val != ((int*)&GameOptions._opt_ptr.diff)[btn])
						DoCommandP(0, btn, val, null, Cmd.CMD_CHANGE_DIFFICULTY_LEVEL);
				}
				DoCommandP(0, -1, GameOptions._opt_mod_temp.diff_level, null, Cmd.CMD_CHANGE_DIFFICULTY_LEVEL);
				DeleteWindow(w);
				// If we are in the editor, we should reload the economy.
				//  This way when you load a game, the max loan and interest rate
				//  are loaded correctly.
				if (Global._game_mode == GameModes.GM_EDITOR)
					StartupEconomy();
				break;
			}
			case 11: /* Cancel button - close window, abandon changes */
				DeleteWindow(w);
				break;
		} break;

		case WindowEvents.WE_MOUSELOOP: /* Handle the visual 'clicking' of the buttons */
			if (_difficulty_timeout != 0 && !--_difficulty_timeout) {
				_difficulty_click_a = 0;
				_difficulty_click_b = 0;
				SetWindowDirty(w);
			}
			break;
		}
	}

	#undef DIFF_INGAME_DISABLED_BUTTONS

	static final Widget _game_difficulty_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    10,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,    10,    11,   369,     0,    13, Str.STR_6800_DIFFICULTY_LEVEL,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    10,     0,   369,    14,    29, 0x0,												Str.STR_NULL},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,    10,    96,    16,    27, Str.STR_6801_EASY,							Str.STR_NULL},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,    97,   183,    16,    27, Str.STR_6802_MEDIUM,						Str.STR_NULL},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,   184,   270,    16,    27, Str.STR_6803_HARD,							Str.STR_NULL},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,   271,   357,    16,    27, Str.STR_6804_CUSTOM,						Str.STR_NULL},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    10,     0,   369,    30,    41, Str.STR_6838_SHOW_HI_SCORE_CHART,Str.STR_NULL},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    10,     0,   369,    42,   262, 0x0,												Str.STR_NULL},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    10,     0,   369,   263,   278, 0x0,												Str.STR_NULL},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,   105,   185,   265,   276, Str.STR_OPTIONS_SAVE_CHANGES,	Str.STR_NULL},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,   186,   266,   265,   276, Str.STR_012E_CANCEL,						Str.STR_NULL},
	{   WIDGETS_END},
	};

	static final WindowDesc _game_difficulty_desc = {
		Window.WDP_CENTER, Window.WDP_CENTER, 370, 279,
		Window.WC_GAME_OPTIONS,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_game_difficulty_widgets,
		GameDifficultyWndProc
	};

	void ShowGameDifficulty()
	{
		Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
		/* Copy current settings (ingame or in intro) to temporary holding place
		 * change that when setting stuff, copy back on clicking 'OK' */
		memcpy(&GameOptions._opt_mod_temp, GameOptions._opt_ptr, sizeof(GameOptions));
		AllocateWindowDesc(&_game_difficulty_desc);
	}

	// virtual PositionMainToolbar function, calls the right one.
	static int v_PositionMainToolbar(int p1)
	{
		if (Global._game_mode != GameModes.GM_MENU) PositionMainToolbar(null);
		return 0;
	}

	static int AiNew_PatchActive_Warning(int p1)
	{
		if (p1 == 1) ShowErrorMessage(INVALID_STRING_ID, TETileTypes.MP_AI_ACTIVATED, 0, 0);
		return 0;
	}

	static int Ai_In_Multiplayer_Warning(int p1)
	{
		if (p1 == 1) {
			ShowErrorMessage(INVALID_STRING_ID, TETileTypes.MP_AI_MULTIPLAYER, 0, 0);
			Global._patches.ainew_active = true;
		}
		return 0;
	}

	static int PopulationInLabelActive(int p1)
	{
		Town* t;

		FOR_ALL_TOWNS(t) {
			if (t.xy != 0) UpdateTownVirtCoord(t);
		}
		return 0;
	}

	static int InvisibleTreesActive(int p1)
	{
		MarkWholeScreenDirty();
		return 0;
	}

	static int InValidateDetailsWindow(int p1)
	{
		InvalidateWindowClasses(Window.WC_VEHICLE_DETAILS);
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
		if (p1) {
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
			ShowErrorMessage(INVALID_STRING_ID, Str.STR_CONFIG_PATCHES_SERVICE_INTERVAL_INCOMPATIBLE, 0, 0);

		return InValidateDetailsWindow(0);
	}

	static int EngineRenewUpdate(int p1)
	{
		DoCommandP(0, 0, Global._patches.autorenew, null, Cmd.CMD_REPLACE_VEHICLE);
		return 0;
	}

	static int EngineRenewMonthsUpdate(int p1)
	{
		DoCommandP(0, 1, Global._patches.autorenew_months, null, Cmd.CMD_REPLACE_VEHICLE);
		return 0;
	}

	static int EngineRenewMoneyUpdate(int p1)
	{
		DoCommandP(0, 2, Global._patches.autorenew_money, null, Cmd.CMD_REPLACE_VEHICLE);
		return 0;
	}

	typedef int PatchButtonClick(int);

	class PatchEntry {
		byte type;                    // type of selector
		byte flags;                   // selector flags
		StringID str;                 // string with descriptive text
		char console_name[40];        // the name this patch has in console
		void* variable;               // pointer to the variable
		int min, max;               // range for spinbox setting
		int step;                  // step for spinbox
		PatchButtonClick* click_proc; // callback procedure
	} PatchEntry;

	enum {
		PE_BOOL			= 0,
		PE_UINT8		= 1,
		PE_INT16		= 2,
		PE_UINT16		= 3,
		PE_INT32		= 4,
		PE_CURRENCY	= 5,
		// selector flags
		PF_0ISDIS       = 1 << 0, // a value of zero means the feature is disabled
		PF_NOCOMMA      = 1 << 1, // number without any thousand seperators
		PF_MULTISTRING  = 1 << 2, // string but only a limited number of options, so don't open editobx
		PF_PLAYERBASED  = 1 << 3, // This has to match the entries that are in settings.c, patch_player_settings
		PF_NETWORK_ONLY = 1 << 4, // this setting only applies to network games
	};

	static final PatchEntry Global._patches_ui[] = {
		{PE_BOOL,		PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_VEHICLESPEED,		"vehicle_speed",		&Global._patches.vehicle_speed,						0,  0,  0, null},
		{PE_BOOL,		PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_LONGDATE,				"long_date",				&Global._patches.status_long_date,					0,  0,  0, null},
		{PE_BOOL,		PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_SHOWFINANCES,		"show_finances",		&Global._patches.show_finances,						0,  0,  0, null},
		{PE_BOOL,		PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_AUTOSCROLL,			"autoscroll",				&Global._patches.autoscroll,								0,  0,  0, null},
		{PE_BOOL,   PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_REVERSE_SCROLLING, "reverse_scroll", &Global._patches.reverse_scroll, 0, 0, 0, null },

		{PE_UINT8,	PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_ERRMSG_DURATION,	"errmsg_duration",	&Global._patches.errmsg_duration,					0, 20,  1, null},

		{PE_UINT8,	PF_MULTISTRING | PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_TOOLBAR_POS, "toolbar_pos", &Global._patches.toolbar_pos,			0,  2,  1, &v_PositionMainToolbar},
		{PE_UINT8,	PF_0ISDIS | PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_SNAP_RADIUS, "window_snap_radius", &Global._patches.window_snap_radius,     1, 32,  1, null},
		{PE_BOOL,		PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_INVISIBLE_TREES,	"invisible_trees", &Global._patches.invisible_trees,					0,  1,  1, &InvisibleTreesActive},
		{PE_BOOL,		PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_POPULATION_IN_LABEL, "population_in_label", &Global._patches.population_in_label, 0, 1, 1, &PopulationInLabelActive},

		{PE_INT32, 0, Str.STR_CONFIG_PATCHES_MAP_X, "map_x", &Global._patches.map_x, 6, 11, 1, null},
		{PE_INT32, 0, Str.STR_CONFIG_PATCHES_MAP_Y, "map_y", &Global._patches.map_y, 6, 11, 1, null},

		{PE_BOOL,   PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_LINK_TERRAFORM_TOOLBAR, "link_terraform_toolbar", &Global._patches.link_terraform_toolbar, 0, 1, 1, null},
	};

	static final PatchEntry Global._patches_finalruction[] = {
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_BUILDONSLOPES,					"build_on_slopes",					&Global._patches.build_on_slopes,				0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_EXTRADYNAMITE,					"extra_dynamite",					&Global._patches.extra_dynamite,				0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_LONGBRIDGES,						"long_bridges",						&Global._patches.longbridges,					0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SIGNALSIDE,						"signal_side",						&Global._patches.signal_side,					0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_MA_CONFIG_PATCHES_MUNICIPAL_AIRPORTS,				"allow_municipal_airports",			&Global._patches.allow_municipal_airports,		0,	0,	0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SMALL_AIRPORTS,					"always_small_airport",				&Global._patches.always_small_airport,			0,  0,  0, null},
		{PE_UINT8,	PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_DRAG_SIGNALS_DENSITY,	"drag_signals_density",				&Global._patches.drag_signals_density,			1, 20,  1, null},
		{PE_BOOL,		0, Str.STR_CONFIG_AUTO_PBS_PLACEMENT,						"auto_pbs_placement",				&Global._patches.auto_pbs_placement,			1, 20,  1, null},

		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SMALL_AIRPORTS,		"always_small_airport", &Global._patches.always_small_airport,			0,  0,  0, null},
		{PE_UINT8,	PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_DRAG_SIGNALS_DENSITY, "drag_signals_density", &Global._patches.drag_signals_density, 1, 20,  1, null},
		{PE_BOOL,		0, Str.STR_CONFIG_AUTO_PBS_PLACEMENT, "auto_pbs_placement", &Global._patches.auto_pbs_placement, 1, 20,  1, null},
	};

	static final PatchEntry Global._patches_vehicles[] = {
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_REALISTICACCEL,		"realistic_acceleration", &Global._patches.realistic_acceleration,		0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_FORBID_90_DEG,		"forbid_90_deg", 		&Global._patches.forbid_90_deg,						0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_MAMMOTHTRAINS,		"mammoth_trains", 	&Global._patches.mammoth_trains,						0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_GOTODEPOT,				"goto_depot", 			&Global._patches.gotodepot,								0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_ROADVehicle.VEH_QUEUE,		"roadveh_queue", 		&Global._patches.roadveh_queue,						0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_NEW_PATHFINDING_ALL, "new_pathfinding_all", &Global._patches.new_pathfinding_all,		0,  0,  0, null},

		{PE_BOOL,		PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_WARN_INCOME_LESS, "train_income_warn", &Global._patches.train_income_warn,				0,  0,  0, null},
		{PE_UINT8,	PF_MULTISTRING | PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_ORDER_REVIEW, "order_review_system", &Global._patches.order_review_system,0,2,  1, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_NEVER_EXPIRE_VEHICLES, "never_expire_vehicles", &Global._patches.never_expire_vehicles,0,0,0, null},

		{PE_UINT16, PF_0ISDIS | PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_LOST_TRAIN_DAYS, "lost_train_days", &Global._patches.lost_train_days,	180,720, 60, null},
		{PE_BOOL,     PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_AUTORENEW_VEHICLE, "autorenew",        &Global._patches.autorenew,                   0, 0, 0, &EngineRenewUpdate},
		{PE_INT16,	  PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_AUTORENEW_MONTHS,  "autorenew_months", &Global._patches.autorenew_months,         -12, 12, 1, &EngineRenewMonthsUpdate},
		{PE_CURRENCY, PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_AUTORENEW_MONEY,   "autorenew_money",  &Global._patches.autorenew_money,  0, 2000000, 100000, &EngineRenewMoneyUpdate},

		{PE_UINT16,	0, Str.STR_CONFIG_PATCHES_MAX_TRAINS,				"max_trains", &Global._patches.max_trains,								0,5000, 50, null},
		{PE_UINT16,	0, Str.STR_CONFIG_PATCHES_MAX_ROADVEH,			"max_roadveh", &Global._patches.max_roadveh,							0,5000, 50, null},
		{PE_UINT16,	0, Str.STR_CONFIG_PATCHES_MAX_AIRCRAFT,			"max_aircraft", &Global._patches.max_aircraft,						0,5000, 50, null},
		{PE_UINT16,	0, Str.STR_CONFIG_PATCHES_MAX_SHIPS,				"max_ships", &Global._patches.max_ships,									0,5000, 50, null},

		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SERVINT_ISPERCENT,"servint_isperfect",&Global._patches.servint_ispercent,				0,  0,  0, &CheckInterval},
		{PE_UINT16, PF_0ISDIS, Str.STR_CONFIG_PATCHES_SERVINT_TRAINS,		"servint_trains",   &Global._patches.servint_trains,		5,800,  5, &InValidateDetailsWindow},
		{PE_UINT16, PF_0ISDIS, Str.STR_CONFIG_PATCHES_SERVINT_ROADVEH,	"servint_roadveh",  &Global._patches.servint_roadveh,	5,800,  5, &InValidateDetailsWindow},
		{PE_UINT16, PF_0ISDIS, Str.STR_CONFIG_PATCHES_SERVINT_AIRCRAFT, "servint_aircraft", &Global._patches.servint_aircraft, 5,800,  5, &InValidateDetailsWindow},
		{PE_UINT16, PF_0ISDIS, Str.STR_CONFIG_PATCHES_SERVINT_SHIPS,		"servint_ships",    &Global._patches.servint_ships,		5,800,  5, &InValidateDetailsWindow},
		{PE_BOOL,   0,         Str.STR_CONFIG_PATCHES_NOSERVICE,        "no_servicing_if_no_breakdowns", &Global._patches.no_servicing_if_no_breakdowns, 0, 0, 0, null},
		{PE_BOOL,   0, Str.STR_CONFIG_PATCHES_WAGONSPEEDLIMITS, "wagon_speed_limits", &Global._patches.wagon_speed_limits, 0, 0, 0, null},
		{PE_UINT16,   0,         Str.STR_CONFIG_PATCHES_AIR_COEFF,        "aircraft_speed_coeff", &Global._patches.aircraft_speed_coeff, 1, 8, 1, null},
	};

	static final PatchEntry Global._patches_stations[] = {
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_JOINSTATIONS,			"join_stations", &Global._patches.join_stations,						0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_FULLLOADANY,			"full_load_any", &Global._patches.full_load_any,						0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_IMPROVEDLOAD,			"improved_load", &Global._patches.improved_load,						0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SELECTGOODS,			"select_goods",  &Global._patches.selectgoods,							0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_NEW_NONSTOP,			"new_nonstop", &Global._patches.new_nonstop,							0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_NONUNIFORM_STATIONS, "nonuniform_stations", &Global._patches.nonuniform_stations,		0,  0,  0, null},
		{PE_UINT8,	0, Str.STR_CONFIG_PATCHES_STATION_SPREAD,		"station_spread", &Global._patches.station_spread,						4, 64,  1, &InvalidateStationBuildWindow},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SERVICEATHELIPAD, "service_at_helipad", &Global._patches.serviceathelipad,					0,  0,  0, null},
		{PE_BOOL, 0, Str.STR_CONFIG_PATCHES_CATCHMENT, "modified_catchment", &Global._patches.modified_catchment, 0, 0, 0, null},
		{PE_BOOL, 0, Str.STR_CONFIG_PATCHES_AIRQUEUE, "aircraft_queueing", &Global._patches.aircraft_queueing, 0, 0, 0, null},

	};

	static final PatchEntry Global._patches_economy[] = {
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_INFLATION,				"inflation", &Global._patches.inflation,								0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_BUILDXTRAIND,			"build_rawmaterial", &Global._patches.build_rawmaterial_ind,		0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_MULTIPINDTOWN,		"multiple_industry_per_town", &Global._patches.multiple_industry_per_town,0, 0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SAMEINDCLOSE,			"same_industry_close", &Global._patches.same_industry_close,			0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_BRIBE,						"bribe", &Global._patches.bribe,										0,  0,  0, null},
		{PE_UINT8,	0, Str.STR_CONFIG_PATCHES_SNOWLINE_HEIGHT,	"snow_line_height", &Global._patches.snow_line_height,					2, 13,  1, null},

		{PE_INT32,	PF_NOCOMMA, Str.STR_CONFIG_PATCHES_COLORED_NEWS_DATE, "colored_new_data", &Global._patches.colored_news_date, 1900, 2200, 5, null},
		{PE_INT32,	PF_NOCOMMA, Str.STR_CONFIG_PATCHES_STARTING_DATE, "starting_date", &Global._patches.starting_date,	 MAX_YEAR_BEGIN_REAL, MAX_YEAR_END_REAL, 1, null},
		{PE_INT32,	PF_NOCOMMA | PF_NETWORK_ONLY, Str.STR_CONFIG_PATCHES_ENDING_DATE, "ending_date", &Global._patches.ending_date,	 MAX_YEAR_BEGIN_REAL, MAX_YEAR_END_REAL, 1, null},

		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SMOOTH_ECONOMY,		"smooth_economy", &Global._patches.smooth_economy,						0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_ALLOW_SHARES,			"allow_shares", &Global._patches.allow_shares,						0,  0,  0, null},
		{PE_UINT8,		0, Str.STR_CONFIG_PATCHES_DAY_LENGTH,			"day_length", &Global._patches.day_length,						1, 32, 1, null},
	};

	static final PatchEntry Global._patches_ai[] = {
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_AINEW_ACTIVE, "ainew_active", &Global._patches.ainew_active, 0, 1, 1, &AiNew_PatchActive_Warning},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_AI_IN_MULTIPLAYER, "ai_in_multiplayer", &Global._patches.ai_in_multiplayer, 0, 1, 1, &Ai_In_Multiplayer_Warning},

		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_AI_BUILDS_TRAINS, "ai_disable_veh_train", &Global._patches.ai_disable_veh_train,			0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_AI_BUILDS_ROADVEH,"ai_disable_veh_roadveh",&Global._patches.ai_disable_veh_roadveh,		0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_AI_BUILDS_AIRCRAFT,"ai_disable_veh_aircraft",&Global._patches.ai_disable_veh_aircraft,0,  0,  0, null},
		{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_AI_BUILDS_SHIPS,"ai_disable_veh_ship",&Global._patches.ai_disable_veh_ship,			0,  0,  0, null},
	};

	class PatchPage {
		final PatchEntry *entries;
		int num;
	} PatchPage;

	static final PatchPage Global._patches_page[] = {
		{Global._patches_ui,						lengthof(Global._patches_ui) },
		{Global._patches_finalruction, lengthof(Global._patches_finalruction) },
		{Global._patches_vehicles,			lengthof(Global._patches_vehicles) },
		{Global._patches_stations,			lengthof(Global._patches_stations) },
		{Global._patches_economy,			lengthof(Global._patches_economy) },
		{Global._patches_ai,						lengthof(Global._patches_ai) },
	};


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

		/* useless, but avoids compiler warning this way */
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

	static void PatchesSelectionWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT: {
			int x,y;
			final PatchEntry *pe;
			final PatchPage *page;
			int clk;
			int val;
			int i;

			w.click_state = 1 << (WP(w,def_d).data_1 + 4);

			DrawWindowWidgets(w);

			x = 0;
			y = 46;
			clk = WP(w,def_d).data_2;
			page = &Global._patches_page[WP(w,def_d).data_1];
			for (i = 0, pe = page.entries; i != page.num; i++, pe++) {
				boolean disabled = false;
				boolean editable = true;

				if ((pe.flags & PF_NETWORK_ONLY) && !_networking)
					editable = false;

				// We do not allow changes of some items when we are a client in a networkgame
				if (!(pe.flags & PF_PLAYERBASED) && _networking && !_network_server)
					editable = false;
				if (pe.type == PE_BOOL) {
					if (editable)
						Gfx.DrawFrameRect(x+5, y+1, x+15+9, y+9, (*(boolean*)pe.variable) ? 6 : 4, (*(boolean*)pe.variable) ? FR_LOWERED : 0);
					else
						Gfx.DrawFrameRect(x+5, y+1, x+15+9, y+9, (*(boolean*)pe.variable) ? 7 : 9, (*(boolean*)pe.variable) ? FR_LOWERED : 0);
					Global.SetDParam(0, *(boolean*)pe.variable ? Str.STR_CONFIG_PATCHES_ON : Str.STR_CONFIG_PATCHES_OFF);
				} else {
					Gfx.DrawFrameRect(x+5, y+1, x+5+9, y+9, 3, clk == i*2+1 ? FR_LOWERED : 0);
					Gfx.DrawFrameRect(x+15, y+1, x+15+9, y+9, 3, clk == i*2+2 ? FR_LOWERED : 0);
					if (!editable) {
						int color = PALETTE_MODIFIER_GREYOUT | Global._color_list[3].unk2;
						Gfx.GfxFillRect(x+6, y+2, x+6+8, y+9, color);
						Gfx.GfxFillRect(x+16, y+2, x+16+8, y+9, color);
					}
					DrawStringCentered(x+10, y+1, Str.STR_6819, 0);
					DrawStringCentered(x+20, y+1, Str.STR_681A, 0);

					val = ReadPE(pe);
					if (pe.type == PE_CURRENCY) val /= _currency.rate;
					disabled = ((val == 0) && (pe.flags & PF_0ISDIS));
					if (disabled) {
						Global.SetDParam(0, Str.STR_CONFIG_PATCHES_DISABLED);
					} else {
						Global.SetDParam(1, val);
						if (pe.type == PE_CURRENCY)
							Global.SetDParam(0, Str.STR_CONFIG_PATCHES_CURRENCY);
						else {
							if (pe.flags & PF_MULTISTRING)
								Global.SetDParam(0, pe.str + val + 1);
							else
								Global.SetDParam(0, pe.flags & PF_NOCOMMA ? Str.STR_CONFIG_PATCHES_INT32 : Str.STR_7024);
						}
					}
				}
				DrawString(30, y+1, (pe.str)+disabled, 0);
				y += 11;
			}
			break;
		}

		case WindowEvents.WE_CLICK:
			switch(e.click.widget) {
			case 3: {
				int x,y;
				int btn;
				final PatchPage *page;
				final PatchEntry *pe;

				y = e.click.pt.y - 46 - 1;
				if (y < 0) return;

				btn = y / 11;
				if (y % 11 > 9) return;

				page = &Global._patches_page[WP(w,def_d).data_1];
				if (btn >= page.num) return;
				pe = &page.entries[btn];

				x = e.click.pt.x - 5;
				if (x < 0) return;

				if (((pe.flags & PF_NETWORK_ONLY) && !_networking) || // return if action is only active in network
						(!(pe.flags & PF_PLAYERBASED) && _networking && !_network_server)) // return if only server can change it
					return;

				if (x < 21) { // clicked on the icon on the left side. Either scroller or boolean on/off
					int val = ReadPE(pe), oval = val;

					switch(pe.type) {
					case PE_BOOL:
						val ^= 1;
						break;
					case PE_UINT8:
					case PE_INT16:
					case PE_UINT16:
					case PE_INT32:
					case PE_CURRENCY:
						// don't allow too fast scrolling
						if ((w.flags4 & WF_TIMEOUT_MASK) > 2 << WF_TIMEOUT_SHL) {
							_left_button_clicked = false;
							return;
						}

						if (x >= 10) {
							//increase
							if (pe.flags & PF_0ISDIS && val == 0)
								val = pe.min;
							else
								val += pe.step;
							if (val > pe.max) val = pe.max;
						} else {
							// decrease
							if (val <= pe.min && pe.flags & PF_0ISDIS) {
								val = 0;
							} else {
								val -= pe.step;
								if (val < pe.min) val = pe.min;
							}
						}

						if (val != oval) {
							WP(w,def_d).data_2 = btn * 2 + 1 + ((x>=10) ? 1 : 0);
							w.flags4 |= 5 << WF_TIMEOUT_SHL;
							_left_button_clicked = false;
						}
						break;
					}
					if (val != oval) {
						// To make patch-changes network-safe
						if (pe.type == PE_CURRENCY) val /= _currency.rate;
						// If an item is playerbased, we do not send it over the network (if any)
						if (pe.flags & PF_PLAYERBASED) {
							WritePE(pe, val);
						} else {
							// Else we do
							DoCommandP(0, (byte)WP(w,def_d).data_1 + ((byte)btn << 8), val, null, Cmd.CMD_CHANGE_PATCH_SETTING);
						}
						SetWindowDirty(w);

						if (pe.click_proc != null) // call callback function
							pe.click_proc(val);
					}
				} else {
					if (pe.type != PE_BOOL && !(pe.flags & PF_MULTISTRING)) { // do not open editbox
						WP(w,def_d).data_3 = btn;
						Global.SetDParam(0, ReadPE(pe));
						ShowQueryString(Str.STR_CONFIG_PATCHES_INT32, Str.STR_CONFIG_PATCHES_QUERY_CAPT, 10, 100, Window.WC_GAME_OPTIONS, 0);
					}
				}

				break;
			}
			case 4: case 5: case 6: case 7: case 8: case 9:
				WP(w,def_d).data_1 = e.click.widget - 4;
				Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
				SetWindowDirty(w);
				break;
			}
			break;

		case WindowEvents.WE_TIMEOUT:
			WP(w,def_d).data_2 = 0;
			SetWindowDirty(w);
			break;

		case WindowEvents.WE_ON_EDIT_TEXT: {
			if (*e.edittext.str) {
				final PatchPage *page = &Global._patches_page[WP(w,def_d).data_1];
				final PatchEntry *pe = &page.entries[WP(w,def_d).data_3];
				int val;
				val = atoi(e.edittext.str);
				if (pe.type == PE_CURRENCY) val /= _currency.rate;
				// If an item is playerbased, we do not send it over the network (if any)
				if (pe.flags & PF_PLAYERBASED) {
					WritePE(pe, val);
				} else {
					// Else we do
					DoCommandP(0, (byte)WP(w,def_d).data_1 + ((byte)WP(w,def_d).data_3 << 8), val, null, Cmd.CMD_CHANGE_PATCH_SETTING);
				}
				SetWindowDirty(w);

				if (pe.click_proc != null) // call callback function
					pe.click_proc(*(int*)pe.variable);
			}
			break;
		}

		case WindowEvents.WE_DESTROY:
			Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
			break;
		}
	}

	/** Network-safe changing of patch-settings.
	 * @param p1 various bitstuffed elements
	 * - p1 = (bit 0- 7) - the patches type (page) that is being changed (finalruction, network, ai)
	 * - p2 = (bit 8-15) - the actual patch (entry) being set inside the category
	 * @param p2 the new value for the patch
	 * @todo check that the new value is a valid one. Awful lot of work, but since only
	 * the server is allowed to do this, we trust it on this one :)
	 */
	int CmdChangePatchSetting(int x, int y, int flags, int p1, int p2)
	{
		byte pcat = BitOps.GB(p1, 0, 8);
		byte pel  = BitOps.GB(p1, 8, 8);

		if (pcat >= lengthof(Global._patches_page)) return Cmd.CMD_ERROR;
		if (pel >= Global._patches_page[pcat].num) return Cmd.CMD_ERROR;

		if (flags & Cmd.DC_EXEC) {
			final PatchEntry *pe = &Global._patches_page[pcat].entries[pel];
			WritePE(pe, (int)p2);

			Window.InvalidateWindow(Window.WC_GAME_OPTIONS, 0);
		}

		return 0;
	}

	static final PatchEntry *IConsoleGetPatch(final char *name, int *page, int *entry)
	{
		final PatchPage *pp;
		final PatchEntry *pe;

		for (*page = 0; *page < lengthof(Global._patches_page); (*page)++) {
			pp = &Global._patches_page[*page];
			for (*entry = 0; *entry < pp.num; (*entry)++) {
				pe = &pp.entries[*entry];
				if (strncmp(pe.console_name, name, sizeof(pe.console_name)) == 0)
					return pe;
			}
		}

		return null;
	}

	/* Those 2 functions need to be here, else we have to make some stuff non-static
	    and besides, it is also better to keep stuff like this at the same place */
	void IConsoleSetPatchSetting(final char *name, final char *value)
	{
		final PatchEntry *pe;
		int page, entry;
		int val;

		pe = IConsoleGetPatch(name, &page, &entry);

		if (pe == null) {
			IConsolePrintF(_icolour_warn, "'%s' is an unknown patch setting.", name);
			return;
		}

		sscanf(value, "%d", &val);

		if (pe.type == PE_CURRENCY) // currency can be different on each client
			val /= _currency.rate;

		// If an item is playerbased, we do not send it over the network (if any)
		if (pe.flags & PF_PLAYERBASED) {
			WritePE(pe, val);
		} else // Else we do
			DoCommandP(0, page + (entry << 8), val, null, Cmd.CMD_CHANGE_PATCH_SETTING);

		{
			char tval[20];
			final char *tval2 = value;
			if (pe.type == PE_BOOL) {
				snprintf(tval, sizeof(tval), (val == 1) ? "on" : "off");
				tval2 = tval;
			}

			IConsolePrintF(_icolour_warn, "'%s' changed to:  %s", name, tval2);
		}
	}

	void IConsoleGetPatchSetting(final char *name)
	{
		char value[20];
		int page, entry;
		final PatchEntry *pe = IConsoleGetPatch(name, &page, &entry);

		/* We did not find the patch setting */
		if (pe == null) {
			IConsolePrintF(_icolour_warn, "'%s' is an unknown patch setting.", name);
			return;
		}

		if (pe.type == PE_BOOL) {
			snprintf(value, sizeof(value), (ReadPE(pe) == 1) ? "on" : "off");
		} else {
			snprintf(value, sizeof(value), "%d", ReadPE(pe));
		}

		IConsolePrintF(_icolour_warn, "Current value for '%s' is: '%s'", name, value);
	}

	static final Widget Global._patches_selection_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    10,     0,    10,     0,    13, Str.STR_00C5,												Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,    10,    11,   369,     0,    13, Str.STR_CONFIG_PATCHES_CAPTION,			Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    10,     0,   369,    14,    41, 0x0,															Str.STR_NULL},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    10,     0,   369,    42,   336, 0x0,															Str.STR_NULL},

	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,    10,    96,    16,    27, Str.STR_CONFIG_PATCHES_GUI,					Str.STR_NULL},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,    97,   183,    16,    27, Str.STR_CONFIG_PATCHES_CONSTRUCTION,	Str.STR_NULL},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,   184,   270,    16,    27, Str.STR_CONFIG_PATCHES_VEHICLES,			Str.STR_NULL},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,   271,   357,    16,    27, Str.STR_CONFIG_PATCHES_STATIONS,			Str.STR_NULL},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,    10,    96,    28,    39, Str.STR_CONFIG_PATCHES_ECONOMY,			Str.STR_NULL},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,    97,   183,    28,    39, Str.STR_CONFIG_PATCHES_AI,						Str.STR_NULL},
	{   WIDGETS_END},
	};

	static final WindowDesc Global._patches_selection_desc = {
		Window.WDP_CENTER, Window.WDP_CENTER, 370, 337,
		Window.WC_GAME_OPTIONS,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		Global._patches_selection_widgets,
		PatchesSelectionWndProc,
	};

	void ShowPatchesSelection()
	{
		Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
		AllocateWindowDesc(&Global._patches_selection_desc);
	}

	enum {
		NEWGRF_WND_PROC_OFFSET_TOP_WIDGET = 14,
		NEWGRF_WND_PROC_ROWSIZE = 14
	};

	static void NewgrfWndProc(Window w, WindowEvent e)
	{
		static GRFFile *_sel_grffile;
		switch (e.event) {
		case WindowEvents.WE_PAINT: {
			int x, y = NEWGRF_WND_PROC_OFFSET_TOP_WIDGET;
			int i = 0;
			GRFFile *c = _first_grffile;

			DrawWindowWidgets(w);

			if (_first_grffile == null) { // no grf sets installed
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
					Gfx.DoDrawString(c.filename, 25, y + 2, h ? 0xC : 0x10);
					Gfx.DrawSprite(SPRITE_PALETTE(Sprite.SPR_SQUARE | PALETTE_TO_RED), 5, y + 2);
					y += NEWGRF_WND_PROC_ROWSIZE;
				}

				c = c.next;
				if (++i == w.vscroll.cap + w.vscroll.pos) break; // stop after displaying 12 items
			}

//	 		Gfx.DoDrawString(_sel_grffile.setname, 120, 200, 0x01); // draw grf name

			if (_sel_grffile == null) { // no grf file selected yet
				Gfx.DrawStringMultiCenter(140, 210, Str.STR_NEWGRF_TIP, 250);
			} else {
				// draw filename
				x = DrawString(5, 199, Str.STR_NEWGRF_FILENAME, 0);
				Gfx.DoDrawString(_sel_grffile.filename, x + 2, 199, 0x01);

				// draw grf id
				x = DrawString(5, 209, Str.STR_NEWGRF_GRF_ID, 0);
				snprintf(_userstring, lengthof(_userstring), "%08X", _sel_grffile.grfid);
				DrawString(x + 2, 209, Str.STR_SPEC_USERSTRING, 0x01);
			}
		} break;

		case WindowEvents.WE_CLICK:
			switch(e.click.widget) {
			case 3: { // select a grf file
				int y = (e.click.pt.y - NEWGRF_WND_PROC_OFFSET_TOP_WIDGET) / NEWGRF_WND_PROC_ROWSIZE;

				if (y >= w.vscroll.cap) return; // click out of bounds

				y += w.vscroll.pos;

				if (y >= w.vscroll.count) return;

				_sel_grffile = _first_grffile;
				// get selected grf-file
				while (y-- != 0) _sel_grffile = _sel_grffile.next;

				SetWindowDirty(w);
			} break;
			case 9: /* Cancel button */
				Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
				break;
			} break;

	/* Parameter edit box not used yet
		case WindowEvents.WE_TIMEOUT:
			WP(w,def_d).data_2 = 0;
			SetWindowDirty(w);
			break;

		case WindowEvents.WE_ON_EDIT_TEXT: {
			if (*e.edittext.str) {
				SetWindowDirty(w);
			}
			break;
		}
	*/
		case WindowEvents.WE_DESTROY:
			_sel_grffile = null;
			Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
			break;
		}
	}

	static final Widget _newgrf_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,										Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   279,     0,    13, Str.STR_NEWGRF_SETTINGS_CAPTION,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   279,   183,   276, 0x0,													Str.STR_NULL},

	{     Window.WWT_MATRIX,   Window.RESIZE_NONE,    14,     0,   267,    14,   182, 0xC01,/*small rows*/					Str.STR_NEWGRF_TIP},
	{  Window.WWT_SCROLLBAR,   Window.RESIZE_NONE,    14,   268,   279,    14,   182, 0x0,													Str.STR_0190_SCROLL_BAR_SCROLLS_LIST},

	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   147,   158,   244,   255, Str.STR_0188,	Str.STR_NULL},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   159,   170,   244,   255, Str.STR_0189,	Str.STR_NULL},
	{    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   175,   274,   244,   255, Str.STR_NEWGRF_SET_PARAMETERS,		Str.STR_NULL},

	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,     5,   138,   261,   272, Str.STR_NEWGRF_APPLY_CHANGES,		Str.STR_NULL},
	{ Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     3,   142,   274,   261,   272, Str.STR_012E_CANCEL,							Str.STR_NULL},
	{   WIDGETS_END},
	};

	static final WindowDesc _newgrf_desc = {
		Window.WDP_CENTER, Window.WDP_CENTER, 280, 277,
		Window.WC_GAME_OPTIONS,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_newgrf_widgets,
		NewgrfWndProc,
	};

	void ShowNewgrf()
	{
		final GRFFile* c;
		Window w;
		int count;

		Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
		w = AllocateWindowDesc(&_newgrf_desc);

		count = 0;
		for (c = _first_grffile; c != null; c = c.next) count++;

		w.vscroll.cap = 12;
		w.vscroll.count = count;
		w.vscroll.pos = 0;
		w.disabled_state = (1 << 5) | (1 << 6) | (1 << 7);
	}

	/* state: 0 = none clicked, 0x01 = first clicked, 0x02 = second clicked */
	void DrawArrowButtons(int x, int y, int state)
	{
		Gfx.DrawFrameRect(x, y+1, x+9, y+9, 3, (state & 0x01) ? FR_LOWERED : 0);
		Gfx.DrawFrameRect(x+10, y+1, x+19, y+9, 3, (state & 0x02) ? FR_LOWERED : 0);
		DrawStringCentered(x+5, y+1, Str.STR_6819, 0);
		DrawStringCentered(x+15, y+1, Str.STR_681A, 0);
	}

	static char _str_separator[2];

	static void CustCurrencyWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WindowEvents.WE_PAINT: {
			int x=35, y=20, i=0;
			int clk = WP(w,def_d).data_1;
			DrawWindowWidgets(w);

			// exchange rate
			DrawArrowButtons(10, y, (clk >> (i*2)) & 0x03);
			Global.SetDParam(0, 1);
			Global.SetDParam(1, 1);
			DrawString(x, y + 1, Str.STR_CURRENCY_EXCHANGE_RATE, 0);
			x = 35;
			y+=12;
			i++;

			// separator
			Gfx.DrawFrameRect(10, y+1, 29, y+9, 0, ((clk >> (i*2)) & 0x03) ? FR_LOWERED : 0);
			x = DrawString(x, y + 1, Str.STR_CURRENCY_SEPARATOR, 0);
			Gfx.DoDrawString(_str_separator, x + 4, y + 1, 6);
			x = 35;
			y+=12;
			i++;

			// prefix
			Gfx.DrawFrameRect(10, y+1, 29, y+9, 0, ((clk >> (i*2)) & 0x03) ? FR_LOWERED : 0);
			x = DrawString(x, y + 1, Str.STR_CURRENCY_PREFIX, 0);
			Gfx.DoDrawString(_custom_currency.prefix, x + 4, y + 1, 6);
			x = 35;
			y+=12;
			i++;

			// suffix
			Gfx.DrawFrameRect(10, y+1, 29, y+9, 0, ((clk >> (i*2)) & 0x03) ? FR_LOWERED : 0);
			x = DrawString(x, y + 1, Str.STR_CURRENCY_SUFFIX, 0);
			Gfx.DoDrawString(_custom_currency.suffix, x + 4, y + 1, 6);
			x = 35;
			y+=12;
			i++;

			// switch to euro
			DrawArrowButtons(10, y, (clk >> (i*2)) & 0x03);
			Global.SetDParam(0, _custom_currency.to_euro);
			DrawString(x, y + 1, (_custom_currency.to_euro != CF_NOEURO) ? Str.STR_CURRENCY_SWITCH_TO_EURO : Str.STR_CURRENCY_SWITCH_TO_EURO_NEVER, 0);
			x = 35;
			y+=12;
			i++;

			// Preview
			y+=12;
			Global.SetDParam(0, 10000);
			DrawString(x, y + 1, Str.STR_CURRENCY_PREVIEW, 0);
		} break;

		case WindowEvents.WE_CLICK: {
			boolean edittext = false;
			int line = (e.click.pt.y - 20)/12;
			int len = 0;
			int x = e.click.pt.x;
			StringID str = 0;

			switch ( line ) {
				case 0: // rate
					if ( BitOps.IS_INT_INSIDE(x, 10, 30) ) { // clicked buttons
						if (x < 20) {
							if (_custom_currency.rate > 1) _custom_currency.rate--;
							WP(w,def_d).data_1 =  (1 << (line * 2 + 0));
						} else {
							if (_custom_currency.rate < 5000) _custom_currency.rate++;
							WP(w,def_d).data_1 =  (1 << (line * 2 + 1));
						}
					} else { // enter text
						Global.SetDParam(0, _custom_currency.rate);
						str = Str.STR_CONFIG_PATCHES_INT32;
						len = 4;
						edittext = true;
					}
				break;
				case 1: // separator
					if ( BitOps.IS_INT_INSIDE(x, 10, 30) )  // clicked button
						WP(w,def_d).data_1 =  (1 << (line * 2 + 1));
					str = BindCString(_str_separator);
					len = 1;
					edittext = true;
				break;
				case 2: // prefix
					if ( BitOps.IS_INT_INSIDE(x, 10, 30) )  // clicked button
						WP(w,def_d).data_1 =  (1 << (line * 2 + 1));
					str = BindCString(_custom_currency.prefix);
					len = 12;
					edittext = true;
				break;
				case 3: // suffix
					if ( BitOps.IS_INT_INSIDE(x, 10, 30) )  // clicked button
						WP(w,def_d).data_1 =  (1 << (line * 2 + 1));
					str = BindCString(_custom_currency.suffix);
					len = 12;
					edittext = true;
				break;
				case 4: // to euro
					if ( BitOps.IS_INT_INSIDE(x, 10, 30) ) { // clicked buttons
						if (x < 20) {
							_custom_currency.to_euro = (_custom_currency.to_euro <= 2000) ?
								CF_NOEURO : _custom_currency.to_euro - 1;
							WP(w,def_d).data_1 = (1 << (line * 2 + 0));
						} else {
							_custom_currency.to_euro =
								clamp(_custom_currency.to_euro + 1, 2000, MAX_YEAR_END_REAL);
							WP(w,def_d).data_1 = (1 << (line * 2 + 1));
						}
					} else { // enter text
						Global.SetDParam(0, _custom_currency.to_euro);
						str = Str.STR_CONFIG_PATCHES_INT32;
						len = 4;
						edittext = true;
					}
				break;
			}

			if (edittext) {
				WP(w,def_d).data_2 = line;
				ShowQueryString(
				str,
				Str.STR_CURRENCY_CHANGE_PARAMETER,
				len + 1, // maximum number of characters OR
				250, // characters up to this width pixels, whichever is satisfied first
				w.window_class,
				w.window_number);
			}

			w.flags4 |= 5 << WF_TIMEOUT_SHL;
			SetWindowDirty(w);
		} break;

		case WindowEvents.WE_ON_EDIT_TEXT: {
				int val;
				final char *b = e.edittext.str;
				switch (WP(w,def_d).data_2) {
					case 0: /* Exchange rate */
						val = atoi(b);
						val = clamp(val, 1, 5000);
						_custom_currency.rate = val;
						break;

					case 1: /* Thousands seperator */
						_custom_currency.separator = (b[0] == '\0') ? ' ' : b[0];
						ttd_strlcpy(_str_separator, b, lengthof(_str_separator));
						break;

					case 2: /* Currency prefix */
						ttd_strlcpy(_custom_currency.prefix, b, lengthof(_custom_currency.prefix));
						break;

					case 3: /* Currency suffix */
						ttd_strlcpy(_custom_currency.suffix, b, lengthof(_custom_currency.suffix));
						break;

					case 4: /* Year to switch to euro */
						val = atoi(b);
						val = clamp(val, 1999, MAX_YEAR_END_REAL);
						if (val == 1999) val = 0;
						_custom_currency.to_euro = val;
						break;
				}
			MarkWholeScreenDirty();


		} break;

		case WindowEvents.WE_TIMEOUT:
			WP(w,def_d).data_1 = 0;
			SetWindowDirty(w);
			break;

		case WindowEvents.WE_DESTROY:
			Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
			MarkWholeScreenDirty();
			break;
		}
	}

	static final Widget _cust_currency_widgets[] = {
	{   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,						Str.STR_018B_CLOSE_WINDOW},
	{    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   229,     0,    13, Str.STR_CURRENCY_WINDOW,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS},
	{      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   229,    14,   119, 0x0,									Str.STR_NULL},
	{   WIDGETS_END},
	};

	static final WindowDesc _cust_currency_desc = {
		Window.WDP_CENTER, Window.WDP_CENTER, 230, 120,
		Window.WC_CUSTOM_CURRENCY, 0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_cust_currency_widgets,
		CustCurrencyWndProc,
	};

	void ShowCustCurrency()
	{
		_str_separator[0] = _custom_currency.separator;
		_str_separator[1] = '\0';

		Window.DeleteWindowById(Window.WC_CUSTOM_CURRENCY, 0);
		AllocateWindowDesc(&_cust_currency_desc);
	}
	
	
}
