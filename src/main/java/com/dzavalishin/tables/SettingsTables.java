package com.dzavalishin.tables;

import com.dzavalishin.game.GameDifficulty;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Str;
import com.dzavalishin.xui.SettingsGui;

public class SettingsTables 
{

	protected static final /*StringID*/ int _distances_dropdown[] = {
			Str.STR_0139_IMPERIAL_MILES,
			Str.STR_013A_METRIC_KILOMETERS,
			Str.INVALID_STRING
	};

	protected static final /*StringID*/ int _driveside_dropdown[] = {
			Str.STR_02E9_DRIVE_ON_LEFT,
			Str.STR_02EA_DRIVE_ON_RIGHT,
			Str.INVALID_STRING
	};

	protected static final /*StringID*/ int _autosave_dropdown[] = {
			Str.STR_02F7_OFF,
			Str.STR_AUTOSAVE_1_MONTH,
			Str.STR_02F8_EVERY_3_MONTHS,
			Str.STR_02F9_EVERY_6_MONTHS,
			Str.STR_02FA_EVERY_12_MONTHS,
			Str.INVALID_STRING,
	};

	protected static final /*StringID*/ int _designnames_dropdown[] = {
			Str.STR_02BE_DEFAULT,
			Str.STR_02BF_CUSTOM,
			Str.INVALID_STRING
	};


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
	 * /
	protected static final int _default_game_diff[][] = { 
	 // A, B, C, D,   E, F, G, H, I, J, K, L, M, N, O, P, Q, R
			{2, 2, 1, 3, 300, 2, 0, 2, 0, 1, 2, 0, 1, 0, 0, 0, 0, 0},	//easy
			{4, 1, 1, 2, 150, 3, 1, 3, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1},	//medium
			{7, 0, 2, 2, 100, 4, 1, 3, 2, 2, 0, 2, 3, 2, 1, 1, 1, 2},	//hard
	}; */


	public static final GameDifficulty _default_game_diff[] = { /*
			 A, B, C, D,   E, F, G, H, I, J, K, L, M, N, O, P, Q, R*/
			new GameDifficulty( 2, 2, 1, 3, 300, 2, 0, 2, 0, 1, 2, 0, 1, 0, 0, 0, 0, 0 ),	//easy
			new GameDifficulty( 4, 1, 1, 2, 150, 3, 1, 3, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1 ),	//medium
			new GameDifficulty( 7, 0, 2, 2, 100, 4, 1, 3, 2, 2, 0, 2, 3, 2, 1, 1, 1, 2 ),	//hard
	};


	/*

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







	 */

	// selector flags
	public static final int PF_0ISDIS       = 1 << 0; // a value of zero means the feature is disabled
	public static final int PF_NOCOMMA      = 1 << 1; // number without any thousand seperators
	public static final int PF_MULTISTRING  = 1 << 2; // string but only a limited number of options, so don't open editobx
	public static final int PF_PLAYERBASED  = 1 << 3; // This has to match the entries that are in settings.c, patch_player_settings
	public static final int PF_NETWORK_ONLY = 1 << 4; // this setting only applies to network games

	static final PatchEntry _patches_ui[] = {
			//new PatchEntry(PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_VEHICLESPEED,		"vehicle_speed",	Global._patches.vehicle_speed,		0,  0,  0, null),
			new PatchEntry(PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_LONGDATE,				"long_date",		Global._patches.status_long_date,	0,  0,  0, null),
			new PatchEntry(PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_SHOWFINANCES,			"show_finances",	Global._patches.show_finances,		0,  0,  0, null),
			//new PatchEntry(PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_AUTOSCROLL,			"autoscroll",	Global._patches.autoscroll,			0,  0,  0, null),
			//new PatchEntry(PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_REVERSE_SCROLLING, "reverse_scroll",	Global._patches.reverse_scroll, 	0, 0, 0, null ),


			new PatchEntry(PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_ERRMSG_DURATION,		"errmsg_duration",	Global._patches.errmsg_duration,	0, 20,  1, null),

			//{PE_UINT8,	PF_MULTISTRING | PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_TOOLBAR_POS, "toolbar_pos", &Global._patches.toolbar_pos,			0,  2,  1, &v_PositionMainToolbar},
			new PatchEntry(PF_0ISDIS | PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_SNAP_RADIUS, "window_snap_radius", Global._patches.window_snap_radius,     1, 32,  1, null),
			//{PE_BOOL,		PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_INVISIBLE_TREES,	"invisible_trees", &Global._patches.invisible_trees,					0,  1,  1, &InvisibleTreesActive},
			//{PE_BOOL,		PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_POPULATION_IN_LABEL, "population_in_label", &Global._patches.population_in_label, 0, 1, 1, &PopulationInLabelActive},

			//{PE_INT32, 0, Str.STR_CONFIG_PATCHES_MAP_X, "map_x", &Global._patches.map_x, 6, 11, 1, null},
			//{PE_INT32, 0, Str.STR_CONFIG_PATCHES_MAP_Y, "map_y", &Global._patches.map_y, 6, 11, 1, null},

			//{BOOL,   PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_LINK_TERRAFORM_TOOLBAR, "link_terraform_toolbar", &Global._patches.link_terraform_toolbar, 0, 1, 1, null},
	};

	static final PatchEntry _patches_construction[] = {
			//{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_BUILDONSLOPES,					"build_on_slopes",					&Global._patches.build_on_slopes,				0,  0,  0, null},
			//{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_EXTRADYNAMITE,					"extra_dynamite",					&Global._patches.extra_dynamite,				0,  0,  0, null},
			//{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_LONGBRIDGES,						"long_bridges",						&Global._patches.longbridges,					0,  0,  0, null},
			//{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SIGNALSIDE,						"signal_side",						&Global._patches.signal_side,					0,  0,  0, null},
			new PatchEntry(0, Str.STR_MA_CONFIG_PATCHES_MUNICIPAL_AIRPORTS,				"allow_municipal_airports",			Global._patches.allow_municipal_airports,		0,	0,	0, null),
			//{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SMALL_AIRPORTS,					"always_small_airport",				&Global._patches.always_small_airport,			0,  0,  0, null},
			//{PE_UINT8,	PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_DRAG_SIGNALS_DENSITY,	"drag_signals_density",				&Global._patches.drag_signals_density,			1, 20,  1, null},
			//{PE_BOOL,		0, Str.STR_CONFIG_AUTO_PBS_PLACEMENT,						"auto_pbs_placement",				&Global._patches.auto_pbs_placement,			1, 20,  1, null},

			//{PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SMALL_AIRPORTS,		"always_small_airport", &Global._patches.always_small_airport,			0,  0,  0, null},
			//{PE_UINT8,	PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_DRAG_SIGNALS_DENSITY, "drag_signals_density", &Global._patches.drag_signals_density, 1, 20,  1, null},
			//{PE_BOOL,		0, Str.STR_CONFIG_AUTO_PBS_PLACEMENT, "auto_pbs_placement", &Global._patches.auto_pbs_placement, 1, 20,  1, null},
	};	

	static final PatchEntry _patches_vehicles[] = {
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_REALISTICACCEL,		"realistic_acceleration", &Global._patches.realistic_acceleration,		0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_FORBID_90_DEG,		"forbid_90_deg", 		&Global._patches.forbid_90_deg,						0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_MAMMOTHTRAINS,		"mammoth_trains", 	&Global._patches.mammoth_trains,						0,  0,  0, null),
			new PatchEntry(0, Str.STR_CONFIG_PATCHES_GOTODEPOT,				"goto_depot", 			Global._patches.gotodepot,								0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_ROADVehicle.VEH_QUEUE,		"roadveh_queue", 		&Global._patches.roadveh_queue,						0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_NEW_PATHFINDING_ALL, "new_pathfinding_all", &Global._patches.new_pathfinding_all,		0,  0,  0, null),

			//PE_BOOL,		PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_WARN_INCOME_LESS, "train_income_warn", &Global._patches.train_income_warn,				0,  0,  0, null),
			//PE_UINT8,	PF_MULTISTRING | PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_ORDER_REVIEW, "order_review_system", &Global._patches.order_review_system,0,2,  1, null),
			new PatchEntry(0, Str.STR_CONFIG_PATCHES_NEVER_EXPIRE_VEHICLES, "never_expire_vehicles", Global._patches.never_expire_vehicles,0,0,0, null),

			//PE_UINT16, PF_0ISDIS | PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_LOST_TRAIN_DAYS, "lost_train_days", &Global._patches.lost_train_days,	180,720, 60, null),
			new PatchEntry(PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_AUTORENEW_VEHICLE, "autorenew",        Global._patches.autorenew,                   0, 0, 0, SettingsGui::EngineRenewUpdate),
			//PE_INT16,	  PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_AUTORENEW_MONTHS,  "autorenew_months", &Global._patches.autorenew_months,         -12, 12, 1, &EngineRenewMonthsUpdate),
			//PE_CURRENCY, PF_PLAYERBASED, Str.STR_CONFIG_PATCHES_AUTORENEW_MONEY,   "autorenew_money",  &Global._patches.autorenew_money,  0, 2000000, 100000, &EngineRenewMoneyUpdate),

			//PE_UINT16,	0, Str.STR_CONFIG_PATCHES_MAX_TRAINS,				"max_trains", &Global._patches.max_trains,								0,5000, 50, null),
			//PE_UINT16,	0, Str.STR_CONFIG_PATCHES_MAX_ROADVEH,			"max_roadveh", &Global._patches.max_roadveh,							0,5000, 50, null),
			//PE_UINT16,	0, Str.STR_CONFIG_PATCHES_MAX_AIRCRAFT,			"max_aircraft", &Global._patches.max_aircraft,						0,5000, 50, null),
			//PE_UINT16,	0, Str.STR_CONFIG_PATCHES_MAX_SHIPS,				"max_ships", &Global._patches.max_ships,									0,5000, 50, null),

			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SERVINT_ISPERCENT,"servint_isperfect",&Global._patches.servint_ispercent,				0,  0,  0, &CheckInterval),
			//PE_UINT16, PF_0ISDIS, Str.STR_CONFIG_PATCHES_SERVINT_TRAINS,		"servint_trains",   &Global._patches.servint_trains,		5,800,  5, &InValidateDetailsWindow),
			//PE_UINT16, PF_0ISDIS, Str.STR_CONFIG_PATCHES_SERVINT_ROADVEH,	"servint_roadveh",  &Global._patches.servint_roadveh,	5,800,  5, &InValidateDetailsWindow),
			//PE_UINT16, PF_0ISDIS, Str.STR_CONFIG_PATCHES_SERVINT_AIRCRAFT, "servint_aircraft", &Global._patches.servint_aircraft, 5,800,  5, &InValidateDetailsWindow),
			//PE_UINT16, PF_0ISDIS, Str.STR_CONFIG_PATCHES_SERVINT_SHIPS,		"servint_ships",    &Global._patches.servint_ships,		5,800,  5, &InValidateDetailsWindow),
			//PE_BOOL,   0,         Str.STR_CONFIG_PATCHES_NOSERVICE,        "no_servicing_if_no_breakdowns", &Global._patches.no_servicing_if_no_breakdowns, 0, 0, 0, null),
			//PE_BOOL,   0, Str.STR_CONFIG_PATCHES_WAGONSPEEDLIMITS, "wagon_speed_limits", &Global._patches.wagon_speed_limits, 0, 0, 0, null),
			//PE_UINT16,   0,         Str.STR_CONFIG_PATCHES_AIR_COEFF,        "aircraft_speed_coeff", &Global._patches.aircraft_speed_coeff, 1, 8, 1, null),
	};	

	static final PatchEntry _patches_stations[] = {
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_JOINSTATIONS,			"join_stations", &Global._patches.join_stations,						0,  0,  0, null),
			new PatchEntry(0, Str.STR_CONFIG_PATCHES_FULLLOADANY,			"full_load_any", Global._patches.full_load_any,						0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_IMPROVEDLOAD,			"improved_load", &Global._patches.improved_load,						0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SELECTGOODS,			"select_goods",  &Global._patches.selectgoods,							0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_NEW_NONSTOP,			"new_nonstop", &Global._patches.new_nonstop,							0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_NONUNIFORM_STATIONS, "nonuniform_stations", &Global._patches.nonuniform_stations,		0,  0,  0, null),
			//PE_UINT8,	0, Str.STR_CONFIG_PATCHES_STATION_SPREAD,		"station_spread", &Global._patches.station_spread,						4, 64,  1, &InvalidateStationBuildWindow),
			new PatchEntry(0, Str.STR_CONFIG_PATCHES_SERVICEATHELIPAD, "service_at_helipad", Global._patches.serviceathelipad,					0,  0,  0, null),
			//PE_BOOL, 0, Str.STR_CONFIG_PATCHES_CATCHMENT, "modified_catchment", &Global._patches.modified_catchment, 0, 0, 0, null),
			//PE_BOOL, 0, Str.STR_CONFIG_PATCHES_AIRQUEUE, "aircraft_queueing", &Global._patches.aircraft_queueing, 0, 0, 0, null),
	};	

	static final PatchEntry _patches_economy[] = {
			new PatchEntry(0, Str.STR_CONFIG_PATCHES_INFLATION,				"inflation", 				Global._patches.inflation,					0,  0,  0, null),
			new PatchEntry(0, Str.STR_CONFIG_PATCHES_BUILDXTRAIND,			"build_rawmaterial", 		Global._patches.build_rawmaterial_ind,		0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_MULTIPINDTOWN,		"multiple_industry_per_town", &Global._patches.multiple_industry_per_town,0, 0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_SAMEINDCLOSE,			"same_industry_close", &Global._patches.same_industry_close,			0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_BRIBE,						"bribe", &Global._patches.bribe,										0,  0,  0, null),
			//PE_UINT8,	0, Str.STR_CONFIG_PATCHES_SNOWLINE_HEIGHT,	"snow_line_height", &Global._patches.snow_line_height,					2, 13,  1, null),

			//PE_INT32,	PF_NOCOMMA, Str.STR_CONFIG_PATCHES_COLORED_NEWS_DATE, "colored_new_data", &Global._patches.colored_news_date, 1900, 2200, 5, null),
			//PE_INT32,	PF_NOCOMMA, Str.STR_CONFIG_PATCHES_STARTING_DATE, "starting_date", &Global._patches.starting_date,	 MAX_YEAR_BEGIN_REAL, MAX_YEAR_END_REAL, 1, null),
			//PE_INT32,	PF_NOCOMMA | PF_NETWORK_ONLY, Str.STR_CONFIG_PATCHES_ENDING_DATE, "ending_date", &Global._patches.ending_date,	 MAX_YEAR_BEGIN_REAL, MAX_YEAR_END_REAL, 1, null),

			new PatchEntry(0, Str.STR_CONFIG_PATCHES_SMOOTH_ECONOMY,		"smooth_economy", 			Global._patches.smooth_economy,				0,  0,  0, null),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_ALLOW_SHARES,			"allow_shares", &Global._patches.allow_shares,						0,  0,  0, null),
			//PE_UINT8,		0, Str.STR_CONFIG_PATCHES_DAY_LENGTH,			"day_length", &Global._patches.day_length,						1, 32, 1, null),
	};	

	static final PatchEntry _patches_ai[] = {
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_AINEW_ACTIVE, "ainew_active", &Global._patches.ainew_active, 0, 1, 1, &AiNew_PatchActive_Warning),
			//PE_BOOL,		0, Str.STR_CONFIG_PATCHES_AI_IN_MULTIPLAYER, "ai_in_multiplayer", &Global._patches.ai_in_multiplayer, 0, 1, 1, &Ai_In_Multiplayer_Warning),

			//new PatchEntry(0, Str.STR_CONFIG_PATCHES_AI_BUILDS_TRAINS,	"ai_disable_veh_train", 	Global._patches.ai_disable_veh_train,		0,  0,  0, null),
			new PatchEntry(0, Str.STR_CONFIG_PATCHES_AI_BUILDS_ROADVEH,		"ai_disable_veh_roadveh",	Global._patches.ai_disable_veh_roadveh,		0,  0,  0, null),
			//new PatchEntry(0, Str.STR_CONFIG_PATCHES_AI_BUILDS_AIRCRAFT,	"ai_disable_veh_aircraft",	Global._patches.ai_disable_veh_aircraft,	0,  0,  0, null),
			new PatchEntry(0, Str.STR_CONFIG_PATCHES_AI_BUILDS_SHIPS,		"ai_disable_veh_ship",		Global._patches.ai_disable_veh_ship,		0,  0,  0, null),
	};	

	protected static final PatchPage _patches_page[] = {
			new PatchPage(_patches_ui),
			new PatchPage(_patches_construction),
			new PatchPage(_patches_vehicles),
			new PatchPage(_patches_stations),
			new PatchPage(_patches_economy),
			new PatchPage(_patches_ai),
	};

}
