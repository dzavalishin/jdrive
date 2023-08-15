package com.dzavalishin.util;

public class Sprites {


	/** @file sprites.h
		This file contains all sprite-related enums and defines. These consist mainly of
	  the sprite numbers and a bunch of masks and macros to handle sprites and to get
	  rid of all the magic numbers in the code.

	@NOTE:
		ALL SPRITE NUMBERS BELOW 5126 are in the main files
		SPR_CANALS_BASE is in canalsw.grf
		SPR_SLOPES_BASE is in trkfoundw.grf
		SPR_OPENTTD_BASE is in openttd.grf

		All elements which consist of two elements should
		have the same name and then suffixes
			_GROUND and _BUILD for building-type sprites
			_REAR and _FRONT for transport-type sprites (tiles where vehicles are on)
		These sprites are split because of the Z order of the elements
			(like some parts of a bridge are behind the vehicle, while others are before)


		All sprites which are described here are referenced only one to a handful of times
		throughout the code. When introducing new sprite enums, use meaningful names.
		Don't be lazy and typing, and only use abbrevations when their meaning is clear or
		the length of the enum would get out of hand. In that case EXPLAIN THE ABBREVATION
		IN THIS FILE, and perhaps add some comments in the code where it is used.
		Now, don't whine about this being too much typing work if the enums are like
		30 characters in length. If your editor doen't help you simplifying your work,
		get a proper editor. If your Operating Systems don't have any decent editors,
		get a proper Operating System.

		@todo Split the "Sprites" enum into smaller chunks and document them
	 */


	//enum Sprites {
	public static final int SPR_SELECT_TILE  = 752;
	public static final int SPR_DOT          = 774; // corner marker for lower/raise land
	public static final int SPR_DOT_SMALL    = 4078;
	public static final int SPR_WHITE_POINT  = 4079;

	/* ASCII */
	public static final int SPR_ASCII_SPACE       = 2;
	public static final int SPR_ASCII_SPACE_SMALL = 226;
	public static final int SPR_ASCII_SPACE_BIG   = 450;

	/* Extra graphic spritenumbers */
	public static final int SPR_CANALS_BASE   = 5382;
	public static final int SPR_SLOPES_BASE   = SPR_CANALS_BASE + 70;
	public static final int SPR_AUTORAIL_BASE = SPR_SLOPES_BASE + 78;
	public static final int SPR_OPENTTD_BASE  = SPR_AUTORAIL_BASE + 55;

	public static final int SPR_BLOT = SPR_OPENTTD_BASE + 29; // colored circle (mainly used as vehicle profit marker and for sever compatibility)

	public static final int SPR_PIN_UP        = SPR_OPENTTD_BASE + 55;   // pin icon
	public static final int SPR_PIN_DOWN      = SPR_OPENTTD_BASE + 56;
	public static final int SPR_BOX_EMPTY     = SPR_OPENTTD_BASE + 59;
	public static final int SPR_BOX_CHECKED   = SPR_OPENTTD_BASE + 60;
	public static final int SPR_WINDOW_RESIZE = SPR_OPENTTD_BASE + 87;   // resize icon
	public static final int SPR_HOUSE_ICON    = SPR_OPENTTD_BASE + 94;
	// arrow icons pointing in all 4 directions
	public static final int SPR_ARROW_DOWN    = SPR_OPENTTD_BASE + 88;
	public static final int SPR_ARROW_UP      = SPR_OPENTTD_BASE + 89;
	public static final int SPR_ARROW_LEFT    = SPR_OPENTTD_BASE + 90;
	public static final int SPR_ARROW_RIGHT   = SPR_OPENTTD_BASE + 91;

	/* Clone vehicles stuff */
	public static final int SPR_CLONE_AIRCRAFT = SPR_OPENTTD_BASE + 92;
	public static final int SPR_CLONE_ROADVEH  = SPR_OPENTTD_BASE + 92;
	public static final int SPR_CLONE_SHIP     = SPR_OPENTTD_BASE + 92;
	public static final int SPR_CLONE_TRAIN    = SPR_OPENTTD_BASE + 92;

	/* Network GUI sprites */
	public static final int SPR_SEMA = SPR_OPENTTD_BASE +24;
	public static final int SPR_SQUARE = SPR_OPENTTD_BASE + 23;     // colored square (used for newgrf compatibility)
	public static final int SPR_LOCK = SPR_OPENTTD_BASE + 22;       // lock icon (for password protected servers)
	public static final int SPR_FLAGS_BASE = SPR_OPENTTD_BASE + 90; // start of the flags block (in same order as enum NetworkLanguage)

	/* Manager face sprites */
	public static final int SPR_GRADIENT = 874; // background gradient behind manager face

	/* is itself no foundation sprite; because tileh 0 has no foundation */
	public static final int SPR_FOUNDATION_BASE = 989;

	/* Shadow cell */
	public static final int SPR_SHADOW_CELL = 1004;

	/* Sliced view shadow cells */
	/* Maybe we have different ones in the future */
	public static final int SPR_MAX_SLICE = SPR_OPENTTD_BASE + 64;
	public static final int SPR_MIN_SLICE = SPR_OPENTTD_BASE + 64;

	/* Unmovables spritenumbers */
	public static final int SPR_UNMOVABLE_TRANSMITTER 	= 2601;
	public static final int SPR_UNMOVABLE_LIGHTHOUSE		= 2602;
	public static final int SPR_TINYHQ_NORTH						= 2603;
	public static final int SPR_TINYHQ_EAST							= 2604;
	public static final int SPR_TINYHQ_WEST							= 2605;
	public static final int SPR_TINYHQ_SOUTH						= 2606;
	public static final int SPR_SMALLHQ_NORTH						= 2607;
	public static final int SPR_SMALLHQ_EAST						= 2608;
	public static final int SPR_SMALLHQ_WEST						= 2609;
	public static final int SPR_SMALLHQ_SOUTH						= 2610;
	public static final int SPR_MEDIUMHQ_NORTH					= 2611;
	public static final int SPR_MEDIUMHQ_NORTH_WALL			= 2612;
	public static final int SPR_MEDIUMHQ_EAST						= 2613;
	public static final int SPR_MEDIUMHQ_EAST_WALL			= 2614;
	public static final int SPR_MEDIUMHQ_WEST						= 2615;
	public static final int SPR_MEDIUMHQ_WEST_WALL			= 2616;	//very tiny piece of wall
	public static final int SPR_MEDIUMHQ_SOUTH					= 2617;
	public static final int SPR_LARGEHQ_NORTH_GROUND		= 2618;
	public static final int SPR_LARGEHQ_NORTH_BUILD			= 2619;
	public static final int SPR_LARGEHQ_EAST_GROUND			= 2620;
	public static final int SPR_LARGEHQ_EAST_BUILD			= 2621;
	public static final int SPR_LARGEHQ_WEST_GROUND			= 2622;
	public static final int SPR_LARGEHQ_WEST_BUILD			= 2623;
	public static final int SPR_LARGEHQ_SOUTH						= 2624;
	public static final int SPR_HUGEHQ_NORTH_GROUND			= 2625;
	public static final int SPR_HUGEHQ_NORTH_BUILD			= 2626;
	public static final int SPR_HUGEHQ_EAST_GROUND			= 2627;
	public static final int SPR_HUGEHQ_EAST_BUILD				=	2628;
	public static final int SPR_HUGEHQ_WEST_GROUND			= 2629;
	public static final int SPR_HUGEHQ_WEST_BUILD				= 2630;
	public static final int SPR_HUGEHQ_SOUTH						= 2631;
	public static final int SPR_STATUE_GROUND						= 1420;
	public static final int SPR_STATUE_COMPANY          = 2632;
	public static final int SPR_BOUGHT_LAND							= 4790;

	/* sprites for rail and rail stations*/
	public static final int SPR_RAIL_SNOW_OFFSET             = 26;
	public static final int SPR_MONO_SNOW_OFFSET             = 26;
	public static final int SPR_MGLV_SNOW_OFFSET             = 26;

	public static final int SPR_RAIL_SINGLE_Y           = 1005;
	public static final int SPR_RAIL_SINGLE_X           = 1006;
	public static final int SPR_RAIL_SINGLE_NORTH       = 1007;
	public static final int SPR_RAIL_SINGLE_SOUTH       = 1008;
	public static final int SPR_RAIL_SINGLE_EAST        = 1009;
	public static final int SPR_RAIL_SINGLE_WEST        = 1010;
	public static final int SPR_RAIL_TRACK_Y						= 1011;
	public static final int SPR_RAIL_TRACK_X						= 1012;
	public static final int SPR_RAIL_TRACK_BASE         = 1018;
	public static final int SPR_RAIL_TRACK_N_S          = 1035;
	public static final int SPR_RAIL_TRACK_Y_SNOW				= 1037;
	public static final int SPR_RAIL_TRACK_X_SNOW				= 1038;
	public static final int SPR_RAIL_DEPOT_SE_1					= 1063;
	public static final int SPR_RAIL_DEPOT_SE_2					= 1064;
	public static final int SPR_RAIL_DEPOT_SW_1					= 1065;
	public static final int SPR_RAIL_DEPOT_SW_2					= 1066;
	public static final int SPR_RAIL_DEPOT_NE						= 1067;
	public static final int SPR_RAIL_DEPOT_NW						= 1068;
	public static final int SPR_RAIL_PLATFORM_Y_FRONT					= 1069;
	public static final int SPR_RAIL_PLATFORM_X_REAR					= 1070;
	public static final int SPR_RAIL_PLATFORM_Y_REAR					= 1071;
	public static final int SPR_RAIL_PLATFORM_X_FRONT					= 1072;
	public static final int SPR_RAIL_PLATFORM_BUILDING_X			= 1073;
	public static final int SPR_RAIL_PLATFORM_BUILDING_Y			= 1074;
	public static final int SPR_RAIL_PLATFORM_PILLARS_Y_FRONT	= 1075;
	public static final int SPR_RAIL_PLATFORM_PILLARS_X_REAR	= 1076;
	public static final int SPR_RAIL_PLATFORM_PILLARS_Y_REAR	= 1077;
	public static final int SPR_RAIL_PLATFORM_PILLARS_X_FRONT	= 1078;
	public static final int SPR_RAIL_ROOF_STRUCTURE_X_TILE_A	= 1079;	//First half of the roof structure
	public static final int SPR_RAIL_ROOF_STRUCTURE_Y_TILE_A	= 1080;
	public static final int SPR_RAIL_ROOF_STRUCTURE_X_TILE_B	= 1081;	//Second half of the roof structure
	public static final int SPR_RAIL_ROOF_STRUCTURE_Y_TILE_B	= 1082;
	public static final int SPR_RAIL_ROOF_GLASS_X_TILE_A			= 1083;	//First half of the roof glass
	public static final int SPR_RAIL_ROOF_GLASS_Y_TILE_A			= 1084;
	public static final int SPR_RAIL_ROOF_GLASS_X_TILE_B			= 1085;	//second half of the roof glass
	public static final int SPR_RAIL_ROOF_GLASS_Y_TILE_B			= 1086;
	public static final int SPR_MONO_SINGLE_Y                 = 1087;
	public static final int SPR_MONO_SINGLE_X                 = 1088;
	public static final int SPR_MONO_SINGLE_NORTH             = 1089;
	public static final int SPR_MONO_SINGLE_SOUTH             = 1090;
	public static final int SPR_MONO_SINGLE_EAST              = 1091;
	public static final int SPR_MONO_SINGLE_WEST              = 1092;
	public static final int SPR_MONO_TRACK_Y                  = 1093;
	public static final int SPR_MONO_TRACK_BASE               = 1100;
	public static final int SPR_MONO_TRACK_N_S                = 1117;
	public static final int SPR_MGLV_SINGLE_Y                 = 1169;
	public static final int SPR_MGLV_SINGLE_X                 = 1170;
	public static final int SPR_MGLV_SINGLE_NORTH             = 1171;
	public static final int SPR_MGLV_SINGLE_SOUTH             = 1172;
	public static final int SPR_MGLV_SINGLE_EAST              = 1173;
	public static final int SPR_MGLV_SINGLE_WEST              = 1174;
	public static final int SPR_MGLV_TRACK_Y                  = 1175;
	public static final int SPR_MGLV_TRACK_BASE               = 1182;
	public static final int SPR_MGLV_TRACK_N_S                = 1199;
	public static final int SPR_WAYPOINT_X_1				= SPR_OPENTTD_BASE + 15;
	public static final int SPR_WAYPOINT_X_2				= SPR_OPENTTD_BASE + 16;
	public static final int SPR_WAYPOINT_Y_1				= SPR_OPENTTD_BASE + 17;
	public static final int SPR_WAYPOINT_Y_2				= SPR_OPENTTD_BASE + 18;
	public static final int OFFSET_TILEH_IMPOSSIBLE			= 0;
	public static final int OFFSET_TILEH_1							= 14;
	public static final int OFFSET_TILEH_2							= 15;
	public static final int OFFSET_TILEH_3							= 22;
	public static final int OFFSET_TILEH_4							= 13;
	public static final int OFFSET_TILEH_6							= 21;
	public static final int OFFSET_TILEH_7							= 17;
	public static final int OFFSET_TILEH_8							= 12;
	public static final int OFFSET_TILEH_9							= 23;
	public static final int OFFSET_TILEH_11							= 18;
	public static final int OFFSET_TILEH_12							= 20;
	public static final int OFFSET_TILEH_13							= 19;
	public static final int OFFSET_TILEH_14							= 16;

	/* sprites for airports and airfields*/
	/* Small airports are AIRFIELD; everything else is AIRPORT */
	public static final int SPR_HELIPORT										= 2633;
	public static final int SPR_AIRPORT_APRON								= 2634;
	public static final int SPR_AIRPORT_AIRCRAFT_STAND			= 2635;
	public static final int SPR_AIRPORT_TAXIWAY_NS_WEST			= 2636;
	public static final int SPR_AIRPORT_TAXIWAY_EW_SOUTH		=	2637;
	public static final int SPR_AIRPORT_TAXIWAY_XING_SOUTH	= 2638;
	public static final int SPR_AIRPORT_TAXIWAY_XING_WEST		= 2639;
	public static final int SPR_AIRPORT_TAXIWAY_NS_CTR			= 2640;
	public static final int SPR_AIRPORT_TAXIWAY_XING_EAST		= 2641;
	public static final int SPR_AIRPORT_TAXIWAY_NS_EAST			= 2642;
	public static final int SPR_AIRPORT_TAXIWAY_EW_NORTH		= 2643;
	public static final int SPR_AIRPORT_TAXIWAY_EW_CTR			= 2644;
	public static final int SPR_AIRPORT_RUNWAY_EXIT_A				= 2645;
	public static final int SPR_AIRPORT_RUNWAY_EXIT_B				= 2646;
	public static final int SPR_AIRPORT_RUNWAY_EXIT_C				= 2647;
	public static final int SPR_AIRPORT_RUNWAY_EXIT_D				= 2648;
	public static final int SPR_AIRPORT_RUNWAY_END					= 2649;	//We should have different ends
	public static final int SPR_AIRPORT_TERMINAL_A					= 2650;
	public static final int SPR_AIRPORT_TOWER								= 2651;
	public static final int SPR_AIRPORT_CONCOURSE						= 2652;
	public static final int SPR_AIRPORT_TERMINAL_B					= 2653;
	public static final int SPR_AIRPORT_TERMINAL_C					= 2654;
	public static final int SPR_AIRPORT_HANGAR_FRONT				= 2655;
	public static final int SPR_AIRPORT_HANGAR_REAR					= 2656;
	public static final int SPR_AIRFIELD_HANGAR_FRONT				= 2657;
	public static final int SPR_AIRFIELD_HANGAR_REAR				= 2658;
	public static final int SPR_AIRPORT_JETWAY_1						= 2659;
	public static final int SPR_AIRPORT_JETWAY_2						= 2660;
	public static final int SPR_AIRPORT_JETWAY_3						= 2661;
	public static final int SPR_AIRPORT_PASSENGER_TUNNEL		= 2662;
	public static final int SPR_AIRPORT_FENCE_Y							= 2663;
	public static final int SPR_AIRPORT_FENCE_X							= 2664;
	public static final int SPR_AIRFIELD_TERM_A							= 2665;
	public static final int SPR_AIRFIELD_TERM_B							= 2666;
	public static final int SPR_AIRFIELD_TERM_C_GROUND			= 2667;
	public static final int SPR_AIRFIELD_TERM_C_BUILD				= 2668;
	public static final int SPR_AIRFIELD_APRON_A						= 2669;
	public static final int SPR_AIRFIELD_APRON_B						= 2670;
	public static final int SPR_AIRFIELD_APRON_C						= 2671;
	public static final int SPR_AIRFIELD_APRON_D						= 2672;
	public static final int SPR_AIRFIELD_RUNWAY_NEAR_END		= 2673;
	public static final int SPR_AIRFIELD_RUNWAY_MIDDLE			= 2674;
	public static final int SPR_AIRFIELD_RUNWAY_FAR_END			= 2675;
	public static final int SPR_AIRFIELD_WIND_1							= 2676;
	public static final int SPR_AIRFIELD_WIND_2							= 2677;
	public static final int SPR_AIRFIELD_WIND_3							= 2678;
	public static final int SPR_AIRFIELD_WIND_4							= 2679;
	public static final int SPR_AIRPORT_RADAR_1							= 2680;
	public static final int SPR_AIRPORT_RADAR_2							= 2681;
	public static final int SPR_AIRPORT_RADAR_3							= 2682;
	public static final int SPR_AIRPORT_RADAR_4							= 2683;
	public static final int SPR_AIRPORT_RADAR_5							= 2684;
	public static final int SPR_AIRPORT_RADAR_6							= 2685;
	public static final int SPR_AIRPORT_RADAR_7							= 2686;
	public static final int SPR_AIRPORT_RADAR_8							= 2687;
	public static final int SPR_AIRPORT_RADAR_9							= 2688;
	public static final int SPR_AIRPORT_RADAR_A							= 2689;
	public static final int SPR_AIRPORT_RADAR_B							= 2690;
	public static final int SPR_AIRPORT_RADAR_C							= 2691;
	public static final int SPR_AIRPORT_HELIPAD							= SPR_OPENTTD_BASE + 28;

	/* Road Stops */
	/* Road stops have a ground tile and 3 buildings; one on each side
				(except the side where the entry is). These are marked _A _B and _C
	 */
	public static final int SPR_BUS_STOP_NE_GROUND					= 2692;
	public static final int SPR_BUS_STOP_SE_GROUND					= 2693;
	public static final int SPR_BUS_STOP_SW_GROUND					= 2694;
	public static final int SPR_BUS_STOP_NW_GROUND					= 2695;
	public static final int SPR_BUS_STOP_NE_BUILD_A					= 2696;
	public static final int SPR_BUS_STOP_SE_BUILD_A					= 2697;
	public static final int SPR_BUS_STOP_SW_BUILD_A					= 2698;
	public static final int SPR_BUS_STOP_NW_BUILD_A					= 2699;
	public static final int SPR_BUS_STOP_NE_BUILD_B					= 2700;
	public static final int SPR_BUS_STOP_SE_BUILD_B					= 2701;
	public static final int SPR_BUS_STOP_SW_BUILD_B					= 2702;
	public static final int SPR_BUS_STOP_NW_BUILD_B					= 2703;
	public static final int SPR_BUS_STOP_NE_BUILD_C					= 2704;
	public static final int SPR_BUS_STOP_SE_BUILD_C					= 2705;
	public static final int SPR_BUS_STOP_SW_BUILD_C					= 2706;
	public static final int SPR_BUS_STOP_NW_BUILD_C					= 2707;
	public static final int SPR_TRUCK_STOP_NE_GROUND				= 2708;
	public static final int SPR_TRUCK_STOP_SE_GROUND				= 2709;
	public static final int SPR_TRUCK_STOP_SW_GROUND				= 2710;
	public static final int SPR_TRUCK_STOP_NW_GROUND				= 2711;
	public static final int SPR_TRUCK_STOP_NE_BUILD_A				= 2712;
	public static final int SPR_TRUCK_STOP_SE_BUILD_A				= 2713;
	public static final int SPR_TRUCK_STOP_SW_BUILD_A				= 2714;
	public static final int SPR_TRUCK_STOP_NW_BUILD_A				= 2715;
	public static final int SPR_TRUCK_STOP_NE_BUILD_B				= 2716;
	public static final int SPR_TRUCK_STOP_SE_BUILD_B				= 2717;
	public static final int SPR_TRUCK_STOP_SW_BUILD_B				= 2718;
	public static final int SPR_TRUCK_STOP_NW_BUILD_B				= 2719;
	public static final int SPR_TRUCK_STOP_NE_BUILD_C				= 2720;
	public static final int SPR_TRUCK_STOP_SE_BUILD_C				= 2721;
	public static final int SPR_TRUCK_STOP_SW_BUILD_C				= 2722;
	public static final int SPR_TRUCK_STOP_NW_BUILD_C				= 2723;

	/* Sprites for docks */
	/* Docks consist of two tiles; the sloped one and the flat one */
	public static final int SPR_DOCK_SLOPE_NE						= 2727;
	public static final int SPR_DOCK_SLOPE_SE						= 2728;
	public static final int SPR_DOCK_SLOPE_SW						= 2729;
	public static final int SPR_DOCK_SLOPE_NW						= 2730;
	public static final int SPR_DOCK_FLAT_X 						= 2731;	//for NE and SW
	public static final int SPR_DOCK_FLAT_Y							= 2732;	//for NW and SE
	public static final int SPR_BUOY								= 4076;	//XXX this sucks; because it displays wrong stuff on canals (no canal borders, just water)

	/* Sprites for road */
	public static final int SPR_ROAD_Y								= 1332;
	public static final int SPR_ROAD_X								= 1333;
	public static final int SPR_ROAD_Y_SNOW							= 1351;
	public static final int SPR_ROAD_X_SNOW							= 1352;

	public static final int SPR_EXCAVATION_X = 1414;
	public static final int SPR_EXCAVATION_Y = 1415;

	/* Landscape sprites */
	public static final int SPR_FLAT_BARE_LAND					= 3924;
	public static final int SPR_FLAT_1_THIRD_GRASS_TILE	= 3943;
	public static final int SPR_FLAT_2_THIRD_GRASS_TILE	= 3962;
	public static final int SPR_FLAT_GRASS_TILE					= 3981;
	public static final int SPR_FLAT_ROUGH_LAND					= 4000;
	public static final int SPR_FLAT_ROUGH_LAND_1				= 4019;
	public static final int SPR_FLAT_ROUGH_LAND_2				= 4020;
	public static final int SPR_FLAT_ROUGH_LAND_3				= 4021;
	public static final int SPR_FLAT_ROUGH_LAND_4				= 4022;
	public static final int SPR_FLAT_ROCKY_LAND_1				= 4023;
	public static final int SPR_FLAT_ROCKY_LAND_2				= 4042;
	public static final int SPR_FLAT_WATER_TILE					= 4061;
	public static final int SPR_FLAT_1_QUART_SNOWY_TILE	= 4493;
	public static final int SPR_FLAT_2_QUART_SNOWY_TILE	= 4512;
	public static final int SPR_FLAT_3_QUART_SNOWY_TILE	= 4531;
	public static final int SPR_FLAT_SNOWY_TILE					= 4550;

	/* Hedge; Farmland-fence sprites */
	public static final int SPR_HEDGE_BUSHES						= 4090;
	public static final int SPR_HEDGE_BUSHES_WITH_GATE	= 4096;
	public static final int SPR_HEDGE_FENCE							= 4102;
	public static final int SPR_HEDGE_BLOOMBUSH_YELLOW	= 4108;
	public static final int SPR_HEDGE_BLOOMBUSH_RED			= 4114;
	public static final int SPR_HEDGE_STONE							= 4120;

	/* Farmland sprites; only flat tiles listed; various stages */
	public static final int SPR_FARMLAND_BARE						= 4126;
	public static final int SPR_FARMLAND_STATE_1				= 4145;
	public static final int SPR_FARMLAND_STATE_2				= 4164;
	public static final int SPR_FARMLAND_STATE_3				= 4183;
	public static final int SPR_FARMLAND_STATE_4				= 4202;
	public static final int SPR_FARMLAND_STATE_5				= 4221;
	public static final int SPR_FARMLAND_STATE_6				= 4240;
	public static final int SPR_FARMLAND_STATE_7				= 4259;
	public static final int SPR_FARMLAND_HAYPACKS				= 4278;

	/* Shores */
	public static final int SPR_NO_SHORE								= 0;	//used for tileh which have no shore
	public static final int SPR_SHORE_TILEH_4						= 4062;
	public static final int SPR_SHORE_TILEH_1						= 4063;
	public static final int SPR_SHORE_TILEH_2						= 4064;
	public static final int SPR_SHORE_TILEH_8						= 4065;
	public static final int SPR_SHORE_TILEH_6						= 4066;
	public static final int SPR_SHORE_TILEH_12					= 4067;
	public static final int SPR_SHORE_TILEH_3						= 4068;
	public static final int SPR_SHORE_TILEH_9						= 4069;

	/* Water-related sprites */
	public static final int SPR_SHIP_DEPOT_SE_FRONT			= 4070;
	public static final int SPR_SHIP_DEPOT_SW_FRONT			= 4071;
	public static final int SPR_SHIP_DEPOT_NW						= 4072;
	public static final int SPR_SHIP_DEPOT_NE						= 4073;
	public static final int SPR_SHIP_DEPOT_SE_REAR			= 4074;
	public static final int SPR_SHIP_DEPOT_SW_REAR			= 4075;
	//here come sloped water sprites
	public static final int SPR_WATER_SLOPE_Y_UP				= SPR_CANALS_BASE + 5; //Water flowing negative Y direction
	public static final int SPR_WATER_SLOPE_X_DOWN			= SPR_CANALS_BASE + 6; //positive X
	public static final int SPR_WATER_SLOPE_X_UP				= SPR_CANALS_BASE + 7; //negative X
	public static final int SPR_WATER_SLOPE_Y_DOWN			= SPR_CANALS_BASE + 8;	//positive Y
	//sprites for the shiplifts
	//there are 4 kinds of shiplifts; each of them is 3 tiles long.
	//the four kinds are running in the X and Y direction and
	//are "lowering" either in the "+" or the "-" direction.
	//the three tiles are the center tile (where the slope is)
	//and a bottom and a top tile
	public static final int SPR_SHIPLIFT_Y_UP_CENTER_REAR			= SPR_CANALS_BASE + 9;
	public static final int SPR_SHIPLIFT_X_DOWN_CENTER_REAR		= SPR_CANALS_BASE + 10;
	public static final int SPR_SHIPLIFT_X_UP_CENTER_REAR			= SPR_CANALS_BASE + 11;
	public static final int SPR_SHIPLIFT_Y_DOWN_CENTER_REAR		= SPR_CANALS_BASE + 12;
	public static final int SPR_SHIPLIFT_Y_UP_CENTER_FRONT		= SPR_CANALS_BASE + 13;
	public static final int SPR_SHIPLIFT_X_DOWN_CENTER_FRONT 	= SPR_CANALS_BASE + 14;
	public static final int SPR_SHIPLIFT_X_UP_CENTER_FRONT		= SPR_CANALS_BASE + 15;
	public static final int SPR_SHIPLIFT_Y_DOWN_CENTER_FRONT	= SPR_CANALS_BASE + 16;
	public static final int SPR_SHIPLIFT_Y_UP_BOTTOM_REAR			= SPR_CANALS_BASE + 17;
	public static final int SPR_SHIPLIFT_X_DOWN_BOTTOM_REAR		= SPR_CANALS_BASE + 18;
	public static final int SPR_SHIPLIFT_X_UP_BOTTOM_REAR			= SPR_CANALS_BASE + 19;
	public static final int SPR_SHIPLIFT_Y_DOWN_BOTTOM_REAR		= SPR_CANALS_BASE + 20;
	public static final int SPR_SHIPLIFT_Y_UP_BOTTOM_FRONT		= SPR_CANALS_BASE + 21;
	public static final int SPR_SHIPLIFT_X_DOWN_BOTTOM_FRONT	= SPR_CANALS_BASE + 22;
	public static final int SPR_SHIPLIFT_X_UP_BOTTOM_FRONT		= SPR_CANALS_BASE + 23;
	public static final int SPR_SHIPLIFT_Y_DOWN_BOTTOM_FRONT	= SPR_CANALS_BASE + 24;
	public static final int SPR_SHIPLIFT_Y_UP_TOP_REAR			  = SPR_CANALS_BASE + 25;
	public static final int SPR_SHIPLIFT_X_DOWN_TOP_REAR			= SPR_CANALS_BASE + 26;
	public static final int SPR_SHIPLIFT_X_UP_TOP_REAR				= SPR_CANALS_BASE + 27;
	public static final int SPR_SHIPLIFT_Y_DOWN_TOP_REAR			= SPR_CANALS_BASE + 28;
	public static final int SPR_SHIPLIFT_Y_UP_TOP_FRONT				= SPR_CANALS_BASE + 29;
	public static final int SPR_SHIPLIFT_X_DOWN_TOP_FRONT			= SPR_CANALS_BASE + 30;
	public static final int SPR_SHIPLIFT_X_UP_TOP_FRONT				= SPR_CANALS_BASE + 31;
	public static final int SPR_SHIPLIFT_Y_DOWN_TOP_FRONT			= SPR_CANALS_BASE + 32;

	/* Sprites for tunnels and bridges */
	public static final int SPR_TUNNEL_ENTRY_REAR_RAIL   = 2365;
	public static final int SPR_TUNNEL_ENTRY_REAR_MONO   = 2373;
	public static final int SPR_TUNNEL_ENTRY_REAR_MAGLEV = 2381;
	public static final int SPR_TUNNEL_ENTRY_REAR_ROAD   = 2389;

	/* Level crossings */
	public static final int SPR_CROSSING_OFF_X_RAIL   = 1370;
	public static final int SPR_CROSSING_OFF_X_MONO   = 1382;
	public static final int SPR_CROSSING_OFF_X_MAGLEV = 1394;

	/* bridge type sprites */
	public static final int SPR_PILLARS_BASE = SPR_OPENTTD_BASE + 30;

	/* Wooden bridge (type 0) */
	public static final int SPR_BTWDN_RAIL_Y_REAR				= 2545;
	public static final int SPR_BTWDN_RAIL_X_REAR				= 2546;
	public static final int SPR_BTWDN_ROAD_Y_REAR				= 2547;
	public static final int SPR_BTWDN_ROAD_X_REAR				= 2548;
	public static final int SPR_BTWDN_Y_FRONT						= 2549;
	public static final int SPR_BTWDN_X_FRONT						= 2550;
	public static final int SPR_BTWDN_Y_PILLAR					= 2551;
	public static final int SPR_BTWDN_X_PILLAR					= 2552;
	public static final int SPR_BTWDN_MONO_Y_REAR				= 4361;
	public static final int SPR_BTWDN_MONO_X_REAR				= 4362;
	public static final int SPR_BTWDN_MGLV_Y_REAR				= 4400;
	public static final int SPR_BTWDN_MGLV_X_REAR				= 4401;
	/* ramps */
	public static final int SPR_BTWDN_ROAD_RAMP_Y_DOWN	= 2529;
	public static final int SPR_BTWDN_ROAD_RAMP_X_DOWN	= 2530;
	public static final int SPR_BTWDN_ROAD_RAMP_X_UP		= 2531;	//for some weird reason the order is swapped
	public static final int SPR_BTWDN_ROAD_RAMP_Y_UP		= 2532;	//between X and Y.
	public static final int SPR_BTWDN_ROAD_Y_SLOPE_UP		= 2533;
	public static final int SPR_BTWDN_ROAD_X_SLOPE_UP		= 2534;
	public static final int SPR_BTWDN_ROAD_Y_SLOPE_DOWN	= 2535;
	public static final int SPR_BTWDN_ROAD_X_SLOPE_DOWN = 2536;
	public static final int SPR_BTWDN_RAIL_RAMP_Y_DOWN	= 2537;
	public static final int SPR_BTWDN_RAIL_RAMP_X_DOWN	= 2538;
	public static final int SPR_BTWDN_RAIL_RAMP_X_UP		= 2539;	//for some weird reason the order is swapped
	public static final int SPR_BTWDN_RAIL_RAMP_Y_UP		= 2540;	//between X and Y.
	public static final int SPR_BTWDN_RAIL_Y_SLOPE_UP		= 2541;
	public static final int SPR_BTWDN_RAIL_X_SLOPE_UP		= 2542;
	public static final int SPR_BTWDN_RAIL_Y_SLOPE_DOWN	= 2543;
	public static final int SPR_BTWDN_RAIL_X_SLOPE_DOWN = 2544;
	public static final int SPR_BTWDN_MONO_RAMP_Y_DOWN	= 4352;
	public static final int SPR_BTWDN_MONO_RAMP_X_DOWN	= 4353;
	public static final int SPR_BTWDN_MONO_RAMP_X_UP		= 4354;	//for some weird reason the order is swapped
	public static final int SPR_BTWDN_MONO_RAMP_Y_UP		= 4355;	//between X and Y.
	public static final int SPR_BTWDN_MONO_Y_SLOPE_UP		= 4356;
	public static final int SPR_BTWDN_MONO_X_SLOPE_UP		= 4357;
	public static final int SPR_BTWDN_MONO_Y_SLOPE_DOWN	= 4358;
	public static final int SPR_BTWDN_MONO_X_SLOPE_DOWN = 4359;
	public static final int SPR_BTWDN_MGLV_RAMP_Y_DOWN	= 4392;
	public static final int SPR_BTWDN_MGLV_RAMP_X_DOWN	= 4393;
	public static final int SPR_BTWDN_MGLV_RAMP_X_UP		= 4394;	//for some weird reason the order is swapped
	public static final int SPR_BTWDN_MGLV_RAMP_Y_UP		= 4395;	//between X and Y.
	public static final int SPR_BTWDN_MGLV_Y_SLOPE_UP		= 4396;
	public static final int SPR_BTWDN_MGLV_X_SLOPE_UP		= 4397;
	public static final int SPR_BTWDN_MGLV_Y_SLOPE_DOWN	= 4398;
	public static final int SPR_BTWDN_MGLV_X_SLOPE_DOWN = 4399;

	/* Steel Girder with arches */
	/* BTSGA == Bridge Type Steel Girder Arched */
	/* This is bridge type number 2 */
	public static final int SPR_BTSGA_RAIL_X_REAR				= 2499;
	public static final int SPR_BTSGA_RAIL_Y_REAR				= 2500;
	public static final int SPR_BTSGA_ROAD_X_REAR				= 2501;
	public static final int SPR_BTSGA_ROAD_Y_REAR				= 2502;
	public static final int SPR_BTSGA_X_FRONT						= 2503;
	public static final int SPR_BTSGA_Y_FRONT						= 2504;
	public static final int SPR_BTSGA_X_PILLAR					= 2505;
	public static final int SPR_BTSGA_Y_PILLAR					= 2606;
	public static final int SPR_BTSGA_MONO_X_REAR				= 4324;
	public static final int SPR_BTSGA_MONO_Y_REAR				= 4325;
	public static final int SPR_BTSGA_MGLV_X_REAR				= 4364;
	public static final int SPR_BTSGA_MGLV_Y_REAR				= 4365;

	/* BTSUS == Suspension bridge */
	/* TILE_* denotes the different tiles a suspension bridge
			can have
			TILE_A and TILE_B are the "beginnings" and "ends" of the
				suspension system. they have small rectangluar endcaps
		 	TILE_C and TILE_D look almost identical to TILE_A and
				TILE_B; but they do not have the "endcaps". They form the
				middle part
			TILE_E is a condensed configuration of two pillars. while they
				are usually 2 pillars apart; they only have 1 pillar separation
				here
			TILE_F is an extended configuration of pillars. they are
				plugged in when pillars should be 3 tiles apart

	 */
	public static final int SPR_BTSUS_ROAD_Y_REAR_TILE_A	= 2453;
	public static final int SPR_BTSUS_ROAD_Y_REAR_TILE_B	= 2454;
	public static final int SPR_BTSUS_Y_FRONT_TILE_A			= 2455;
	public static final int SPR_BTSUS_Y_FRONT_TILE_B			= 2456;
	public static final int SPR_BTSUS_ROAD_Y_REAR_TILE_D	= 2457;
	public static final int SPR_BTSUS_ROAD_Y_REAR_TILE_C	= 2458;
	public static final int SPR_BTSUS_Y_FRONT_TILE_D			= 2459;
	public static final int SPR_BTSUS_Y_FRONT_TILE_C			= 2460;
	public static final int SPR_BTSUS_ROAD_X_REAR_TILE_A	= 2461;
	public static final int SPR_BTSUS_ROAD_X_REAR_TILE_B	= 2462;
	public static final int SPR_BTSUS_X_FRONT_TILE_A			= 2463;
	public static final int SPR_BTSUS_X_FRONT_TILE_B			= 2464;
	public static final int SPR_BTSUS_ROAD_X_TILE_D				= 2465;
	public static final int SPR_BTSUS_ROAD_X_TILE_C				= 2466;
	public static final int SPR_BTSUS_X_FRONT_TILE_D			= 2467;
	public static final int SPR_BTSUS_X_FRONT_TILE_C			= 2468;
	public static final int SPR_BTSUS_RAIL_Y_REAR_TILE_A	= 2469;
	public static final int SPR_BTSUS_RAIL_Y_REAR_TILE_B	= 2470;
	public static final int SPR_BTSUS_RAIL_Y_REAR_TILE_D	= 2471;
	public static final int SPR_BTSUS_RAIL_Y_REAR_TILE_C	= 2472;
	public static final int SPR_BTSUS_RAIL_X_REAR_TILE_A	= 2473;
	public static final int SPR_BTSUS_RAIL_X_REAR_TILE_B 	= 2474;
	public static final int SPR_BTSUS_RAIL_X_REAR_TILE_D 	= 2475;
	public static final int SPR_BTSUS_RAIL_X_REAR_TILE_C	= 2476;
	public static final int SPR_BTSUS_Y_PILLAR_TILE_A			= 2477;
	public static final int SPR_BTSUS_Y_PILLAR_TILE_B			= 2478;
	public static final int SPR_BTSUS_Y_PILLAR_TILE_D			= 2479;
	public static final int SPR_BTSUS_Y_PILLAR_TILE_C			= 2480;
	public static final int SPR_BTSUS_X_PILLAR_TILE_A			= 2481;
	public static final int SPR_BTSUS_X_PILLAR_TILE_B			= 2482;
	public static final int SPR_BTSUS_X_PILLAR_TILE_D			= 2483;
	public static final int SPR_BTSUS_X_PILLAR_TILE_C			= 2484;
	public static final int SPR_BTSUS_RAIL_Y_REAR_TILE_E	= 2485;
	public static final int SPR_BTSUS_RAIL_X_REAR_TILE_E	= 2486;
	public static final int SPR_BTSUS_ROAD_Y_REAR_TILE_E	= 2487;
	public static final int SPR_BTSUS_ROAD_X_REAR_TILE_E	= 2488;
	public static final int SPR_BTSUS_Y_FRONT_TILE_E			= 2489;
	public static final int SPR_BTSUS_X_FRONT_TILE_E			= 2490;
	public static final int SPR_BTSUS_Y_PILLAR_TILE_E			= 2491;
	public static final int SPR_BTSUS_X_PILLAR_TILE_E			= 2492;
	public static final int SPR_BTSUS_RAIL_X_REAR_TILE_F	= 2493;
	public static final int SPR_BTSUS_RAIL_Y_REAR_TILE_F	= 2494;
	public static final int SPR_BTSUS_ROAD_X_REAR_TILE_F	= 2495;
	public static final int SPR_BTSUS_ROAD_Y_REAR_TILE_F	= 2496;
	public static final int SPR_BTSUS_Y_FRONT							= 2497;
	public static final int SPR_BTSUS_X_FRONT							= 2498;
	public static final int SPR_BTSUS_MONO_Y_REAR_TILE_A	= 4334;
	public static final int SPR_BTSUS_MONO_Y_REAR_TILE_B	= 4335;
	public static final int SPR_BTSUS_MONO_Y_REAR_TILE_D	= 4336;
	public static final int SPR_BTSUS_MONO_Y_REAR_TILE_C	= 4337;
	public static final int SPR_BTSUS_MONO_X_REAR_TILE_A	= 4338;
	public static final int SPR_BTSUS_MONO_X_REAR_TILE_B	= 4339;
	public static final int SPR_BTSUS_MONO_X_REAR_TILE_D	= 4340;
	public static final int SPR_BTSUS_MONO_X_REAR_TILE_C	= 4341;
	public static final int SPR_BTSUS_MONO_Y_REAR_TILE_E	= 4342;
	public static final int SPR_BTSUS_MONO_X_REAR_TILE_E	= 4343;
	public static final int SPR_BTSUS_MONO_X_REAR_TILE_F	= 4344;
	public static final int SPR_BTSUS_MONO_Y_REAR_TILE_F	= 4345;
	public static final int SPR_BTSUS_MGLV_Y_REAR_TILE_A 	=	4374;
	public static final int SPR_BTSUS_MGLV_Y_REAR_TILE_B 	=	4375;
	public static final int SPR_BTSUS_MGLV_Y_REAR_TILE_D 	=	4376;
	public static final int SPR_BTSUS_MGLV_Y_REAR_TILE_C	= 4377;
	public static final int SPR_BTSUS_MGLV_X_REAR_TILE_A	= 4378;
	public static final int SPR_BTSUS_MGLV_X_REAR_TILE_B	= 4379;
	public static final int SPR_BTSUS_MGLV_X_REAR_TILE_D	= 4380;
	public static final int SPR_BTSUS_MGLV_X_REAR_TILE_C	= 4381;
	public static final int SPR_BTSUS_MGLV_Y_REAR_TILE_E	= 4382;
	public static final int SPR_BTSUS_MGLV_X_REAR_TILE_E	= 4383;
	public static final int SPR_BTSUS_MGLV_X_REAR_TILE_F	= 4384;
	public static final int SPR_BTSUS_MGLV_Y_REAR_TILE_F	= 4385;

	/* cantilever bridges */
	/* They have three different kinds of tiles:
			END(ing); MID(dle); BEG(gining)
	 */
	public static final int SPR_BTCAN_RAIL_X_BEG					= 2507;
	public static final int SPR_BTCAN_RAIL_X_MID					= 2508;
	public static final int SPR_BTCAN_RAIL_X_END					= 2509;
	public static final int SPR_BTCAN_RAIL_Y_END					= 2510;
	public static final int SPR_BTCAN_RAIL_Y_MID					= 2511;
	public static final int SPR_BTCAN_RAIL_Y_BEG					= 2512;
	public static final int SPR_BTCAN_ROAD_X_BEG					= 2513;
	public static final int SPR_BTCAN_ROAD_X_MID					= 2514;
	public static final int SPR_BTCAN_ROAD_X_END					= 2515;
	public static final int SPR_BTCAN_ROAD_Y_END					= 2516;
	public static final int SPR_BTCAN_ROAD_Y_MID					= 2517;
	public static final int SPR_BTCAN_ROAD_Y_BEG					= 2518;
	public static final int SPR_BTCAN_X_FRONT_BEG					= 2519;
	public static final int SPR_BTCAN_X_FRONT_MID					= 2520;
	public static final int SPR_BTCAN_X_FRONT_END					= 2521;
	public static final int SPR_BTCAN_Y_FRONT_END					= 2522;
	public static final int SPR_BTCAN_Y_FRONT_MID					= 2523;
	public static final int SPR_BTCAN_Y_FRONT_BEG					= 2524;
	public static final int SPR_BTCAN_X_PILLAR_BEG				= 2525;
	public static final int SPR_BTCAN_X_PILLAR_MID				= 2526;
	public static final int SPR_BTCAN_Y_PILLAR_MID				= 2527;
	public static final int SPR_BTCAN_Y_PILLAR_BEG				= 2528;
	public static final int SPR_BTCAN_MONO_X_BEG					= 4346;
	public static final int SPR_BTCAN_MONO_X_MID					= 4347;
	public static final int SPR_BTCAN_MONO_X_END					= 4348;
	public static final int SPR_BTCAN_MONO_Y_END					= 4349;
	public static final int SPR_BTCAN_MONO_Y_MID					= 4350;
	public static final int SPR_BTCAN_MONO_Y_BEG					= 4351;
	public static final int SPR_BTCAN_MGLV_X_BEG					= 4386;
	public static final int SPR_BTCAN_MGLV_X_MID					= 4387;
	public static final int SPR_BTCAN_MGLV_X_END					= 4388;
	public static final int SPR_BTCAN_MGLV_Y_END					= 4389;
	public static final int SPR_BTCAN_MGLV_Y_MID					= 4390;
	public static final int SPR_BTCAN_MGLV_Y_BEG					= 4391;

	/* little concrete bridge */
	public static final int SPR_BTCON_RAIL_X				= 2493;
	public static final int SPR_BTCON_RAIL_Y				= 2494;
	public static final int SPR_BTCON_ROAD_X				= 2495;
	public static final int SPR_BTCON_ROAD_Y				= 2496;
	public static final int SPR_BTCON_X_FRONT				= 2497;
	public static final int SPR_BTCON_Y_FRONT				= 2498;
	public static final int SPR_BTCON_X_PILLAR			= 2505;
	public static final int SPR_BTCON_Y_PILLAR			= 2506;
	public static final int SPR_BTCON_MONO_X				= 4344;
	public static final int SPR_BTCON_MONO_Y				= 4345;
	public static final int SPR_BTCON_MGLV_X				= 4384;
	public static final int SPR_BTCON_MGLV_Y				= 4385;

	/* little steel girder bridge */
	public static final int SPR_BTGIR_RAIL_X				= 2553;
	public static final int SPR_BTGIR_RAIL_Y				= 2554;
	public static final int SPR_BTGIR_ROAD_X				= 2555;
	public static final int SPR_BTGIR_ROAD_Y				= 2556;
	public static final int SPR_BTGIR_X_FRONT				= 2557;
	public static final int SPR_BTGIR_Y_FRONT				= 2558;
	public static final int SPR_BTGIR_X_PILLAR			= 2505;
	public static final int SPR_BTGIR_Y_PILLAR			= 2506;
	public static final int SPR_BTGIR_MONO_X				= 4362;
	public static final int SPR_BTGIR_MONO_Y				= 4363;
	public static final int SPR_BTGIR_MGLV_X				= 4402;
	public static final int SPR_BTGIR_MGLV_Y				= 4403;

	/* tubular bridges */
	/* tubular bridges have 3 kinds of tiles:
				a start tile (with only half a tube on the far side; marked _BEG
				a middle tile (full tunnel); marked _MID
				and an end tile (half a tube on the near side; maked _END
	 */
	public static final int SPR_BTTUB_X_FRONT_BEG				= 2559;
	public static final int SPR_BTTUB_X_FRONT_MID				= 2660;
	public static final int SPR_BTTUB_X_FRONT_END				= 2561;
	public static final int SPR_BTTUB_Y_FRONT_END				= 2562;
	public static final int SPR_BTTUB_Y_FRONT_MID				= 2563;
	public static final int SPR_BTTUB_Y_FRONT_BEG				= 2564;
	public static final int SPR_BTTUB_X_RAIL_REAR_BEG		= 2569;
	public static final int SPR_BTTUB_X_RAIL_REAR_MID		= 2570;
	public static final int SPR_BTTUB_X_RAIL_REAR_END		= 2571;


	/* ramps (for all bridges except wood and tubular?)*/
	public static final int SPR_BTGEN_RAIL_X_SLOPE_DOWN = 2437;
	public static final int SPR_BTGEN_RAIL_X_SLOPE_UP		= 2438;
	public static final int SPR_BTGEN_RAIL_Y_SLOPE_DOWN	= 2439;
	public static final int SPR_BTGEN_RAIL_Y_SLOPE_UP		= 2440;
	public static final int SPR_BTGEN_RAIL_RAMP_X_UP		= 2441;
	public static final int SPR_BTGEN_RAIL_RAMP_X_DOWN	= 2442;
	public static final int SPR_BTGEN_RAIL_RAMP_Y_UP		= 2443;
	public static final int SPR_BTGEN_RAIL_RAMP_Y_DOWN	= 2444;
	public static final int SPR_BTGEN_ROAD_X_SLOPE_DOWN = 2445;
	public static final int SPR_BTGEN_ROAD_X_SLOPE_UP		= 2446;
	public static final int SPR_BTGEN_ROAD_Y_SLOPE_DOWN	= 2447;
	public static final int SPR_BTGEN_ROAD_Y_SLOPE_UP		= 2448;
	public static final int SPR_BTGEN_ROAD_RAMP_X_UP		= 2449;
	public static final int SPR_BTGEN_ROAD_RAMP_X_DOWN	= 2450;
	public static final int SPR_BTGEN_ROAD_RAMP_Y_UP		= 2451;
	public static final int SPR_BTGEN_ROAD_RAMP_Y_DOWN	= 2452;
	public static final int SPR_BTGEN_MONO_X_SLOPE_DOWN = 4326;
	public static final int SPR_BTGEN_MONO_X_SLOPE_UP		= 4327;
	public static final int SPR_BTGEN_MONO_Y_SLOPE_DOWN	= 4328;
	public static final int SPR_BTGEN_MONO_Y_SLOPE_UP		= 4329;
	public static final int SPR_BTGEN_MONO_RAMP_X_UP		= 4330;
	public static final int SPR_BTGEN_MONO_RAMP_X_DOWN	= 4331;
	public static final int SPR_BTGEN_MONO_RAMP_Y_UP		= 4332;
	public static final int SPR_BTGEN_MONO_RAMP_Y_DOWN	= 4333;
	public static final int SPR_BTGEN_MGLV_X_SLOPE_DOWN = 4366;
	public static final int SPR_BTGEN_MGLV_X_SLOPE_UP		= 4367;
	public static final int SPR_BTGEN_MGLV_Y_SLOPE_DOWN	= 4368;
	public static final int SPR_BTGEN_MGLV_Y_SLOPE_UP		= 4369;
	public static final int SPR_BTGEN_MGLV_RAMP_X_UP		= 4370;
	public static final int SPR_BTGEN_MGLV_RAMP_X_DOWN	= 4371;
	public static final int SPR_BTGEN_MGLV_RAMP_Y_UP		= 4372;
	public static final int SPR_BTGEN_MGLV_RAMP_Y_DOWN	= 4373;


	/* Vehicle sprite-flags (red/green) */
	public static final int SPR_FLAG_VEH_STOPPED	= 3090;
	public static final int SPR_FLAG_VEH_RUNNING	= 3091;

	/* Rotor sprite numbers */
	public static final int SPR_ROTOR_STOPPED		= 3901;
	public static final int SPR_ROTOR_MOVING_1	= 3902;
	public static final int SPR_ROTOR_MOVING_3	= 3904;

	/* Town/house sprites */
	public static final int SPR_LIFT = 1443;

	/* Easter egg/disaster sprites */
	public static final int SPR_BLIMP                  = 3905; // Zeppelin
	public static final int SPR_BLIMP_CRASHING         = 3906;
	public static final int SPR_BLIMP_CRASHED          = 3907;
	public static final int SPR_UFO_SMALL_SCOUT        = 3908; // XCOM - UFO Defense
	public static final int SPR_UFO_SMALL_SCOUT_DARKER = 3909;
	public static final int SPR_SUB_SMALL_NE           = 3910; // Silent Service
	public static final int SPR_SUB_SMALL_SE           = 3911;
	public static final int SPR_SUB_SMALL_SW           = 3912;
	public static final int SPR_SUB_SMALL_NW           = 3913;
	public static final int SPR_SUB_LARGE_NE           = 3914;
	public static final int SPR_SUB_LARGE_SE           = 3915;
	public static final int SPR_SUB_LARGE_SW           = 3916;
	public static final int SPR_SUB_LARGE_NW           = 3917;
	public static final int SPR_F_15                   = 3918; // F-15 Strike Eagle
	public static final int SPR_F_15_FIRING            = 3919;
	public static final int SPR_UFO_HARVESTER          = 3920; // XCOM - UFO Defense
	public static final int SPR_XCOM_SKYRANGER         = 3921;
	public static final int SPR_AH_64A                 = 3922; // Gunship
	public static final int SPR_AH_64A_FIRING          = 3923;

	/* main_gui.c */
	public static final int SPR_IMG_TERRAFORM_UP    = 694;
	public static final int SPR_IMG_TERRAFORM_DOWN  = 695;
	public static final int SPR_IMG_DYNAMITE        = 703;
	public static final int SPR_IMG_ROCKS           = 4084;
	public static final int SPR_IMG_LIGHTHOUSE_DESERT = 4085; // XXX - is Desert image on the desert-climate
	public static final int SPR_IMG_TRANSMITTER     = 4086;
	public static final int SPR_IMG_LEVEL_LAND      = SPR_OPENTTD_BASE + 61;
	public static final int SPR_IMG_BUILD_CANAL     = SPR_OPENTTD_BASE + 58;
	public static final int SPR_IMG_BUILD_LOCK      = SPR_CANALS_BASE + 69;
	public static final int SPR_IMG_PLACE_SIGN      = SPR_OPENTTD_BASE + 63;
	public static final int SPR_IMG_PAUSE           = 726;
	public static final int SPR_IMG_FASTFORWARD     = SPR_OPENTTD_BASE + 54;
	public static final int SPR_IMG_SETTINGS        = 751;
	public static final int SPR_IMG_SAVE            = 724;
	public static final int SPR_IMG_SMALLMAP        = 708;
	public static final int SPR_IMG_TOWN            = 4077;
	public static final int SPR_IMG_SUBSIDIES       = 679;
	public static final int SPR_IMG_COMPANY_LIST    = 1299;
	public static final int SPR_IMG_COMPANY_FINANCE = 737;
	public static final int SPR_IMG_COMPANY_GENERAL = 743;
	public static final int SPR_IMG_GRAPHS          = 745;
	public static final int SPR_IMG_COMPANY_LEAGUE  = 684;
	public static final int SPR_IMG_SHOW_COUNTOURS  = 738;
	public static final int SPR_IMG_SHOW_VEHICLES   = 739;
	public static final int SPR_IMG_SHOW_ROUTES     = 740;
	public static final int SPR_IMG_INDUSTRY        = 741;
	public static final int SPR_IMG_PLANTTREES      = 742;
	public static final int SPR_IMG_TRAINLIST       = 731;
	public static final int SPR_IMG_TRUCKLIST       = 732;
	public static final int SPR_IMG_SHIPLIST        = 733;
	public static final int SPR_IMG_AIRPLANESLIST   = 734;
	public static final int SPR_IMG_ZOOMIN          = 735;
	public static final int SPR_IMG_ZOOMOUT         = 736;
	public static final int SPR_IMG_BUILDRAIL       = 727;
	public static final int SPR_IMG_BUILDROAD       = 728;
	public static final int SPR_IMG_BUILDWATER      = 729;
	public static final int SPR_IMG_BUILDAIR        = 730;
	public static final int SPR_IMG_LANDSCAPING     = 4083;
	public static final int SPR_IMG_MUSIC           = 713;
	public static final int SPR_IMG_MESSAGES        = 680;
	public static final int SPR_IMG_QUERY           = 723;
	public static final int SPR_IMG_SIGN            = 4082;
	public static final int SPR_IMG_BUY_LAND        = 4791;

	/* OPEN TRANSPORT TYCOON in gamescreen */
	public static final int SPR_OTTD_O                = 4842;
	public static final int SPR_OTTD_P                = 4841;
	public static final int SPR_OTTD_E                = SPR_OPENTTD_BASE + 13;
	public static final int SPR_OTTD_D                = SPR_OPENTTD_BASE + 14;
	public static final int SPR_OTTD_N                = 4839;
	public static final int SPR_OTTD_T                = 4836;
	public static final int SPR_OTTD_R                = 4837;
	public static final int SPR_OTTD_A                = 4838;
	public static final int SPR_OTTD_S                = 4840;
	public static final int SPR_OTTD_Y                = 4843;
	public static final int SPR_OTTD_C                = 4844;

	public static final int SPR_HIGHSCORE_CHART_BEGIN = 4804;
	public static final int SPR_TYCOON_IMG1_BEGIN     = 4814;
	public static final int SPR_TYCOON_IMG2_BEGIN     = 4824;

	/* Effect vehciles */
	public static final int SPR_BULLDOZER_NE = 1416;
	public static final int SPR_BULLDOZER_SE = 1417;
	public static final int SPR_BULLDOZER_SW = 1418;
	public static final int SPR_BULLDOZER_NW = 1419;

	public static final int SPR_SMOKE_0 = 2040;
	public static final int SPR_SMOKE_1 = 2041;
	public static final int SPR_SMOKE_2 = 2042;
	public static final int SPR_SMOKE_3 = 2043;
	public static final int SPR_SMOKE_4 = 2044;

	public static final int SPR_DIESEL_SMOKE_0 = 3073;
	public static final int SPR_DIESEL_SMOKE_1 = 3074;
	public static final int SPR_DIESEL_SMOKE_2 = 3075;
	public static final int SPR_DIESEL_SMOKE_3 = 3076;
	public static final int SPR_DIESEL_SMOKE_4 = 3077;
	public static final int SPR_DIESEL_SMOKE_5 = 3078;

	public static final int SPR_STEAM_SMOKE_0 = 3079;
	public static final int SPR_STEAM_SMOKE_1 = 3080;
	public static final int SPR_STEAM_SMOKE_2 = 3081;
	public static final int SPR_STEAM_SMOKE_3 = 3082;
	public static final int SPR_STEAM_SMOKE_4 = 3083;

	public static final int SPR_ELECTRIC_SPARK_0 = 3084;
	public static final int SPR_ELECTRIC_SPARK_1 = 3085;
	public static final int SPR_ELECTRIC_SPARK_2 = 3086;
	public static final int SPR_ELECTRIC_SPARK_3 = 3087;
	public static final int SPR_ELECTRIC_SPARK_4 = 3088;
	public static final int SPR_ELECTRIC_SPARK_5 = 3089;

	public static final int SPR_CHIMNEY_SMOKE_0 = 3701;
	public static final int SPR_CHIMNEY_SMOKE_1 = 3702;
	public static final int SPR_CHIMNEY_SMOKE_2 = 3703;
	public static final int SPR_CHIMNEY_SMOKE_3 = 3704;
	public static final int SPR_CHIMNEY_SMOKE_4 = 3705;
	public static final int SPR_CHIMNEY_SMOKE_5 = 3706;
	public static final int SPR_CHIMNEY_SMOKE_6 = 3707;
	public static final int SPR_CHIMNEY_SMOKE_7 = 3708;

	public static final int SPR_EXPLOSION_LARGE_0 = 3709;
	public static final int SPR_EXPLOSION_LARGE_1 = 3710;
	public static final int SPR_EXPLOSION_LARGE_2 = 3711;
	public static final int SPR_EXPLOSION_LARGE_3 = 3712;
	public static final int SPR_EXPLOSION_LARGE_4 = 3713;
	public static final int SPR_EXPLOSION_LARGE_5 = 3714;
	public static final int SPR_EXPLOSION_LARGE_6 = 3715;
	public static final int SPR_EXPLOSION_LARGE_7 = 3716;
	public static final int SPR_EXPLOSION_LARGE_8 = 3717;
	public static final int SPR_EXPLOSION_LARGE_9 = 3718;
	public static final int SPR_EXPLOSION_LARGE_A = 3719;
	public static final int SPR_EXPLOSION_LARGE_B = 3720;
	public static final int SPR_EXPLOSION_LARGE_C = 3721;
	public static final int SPR_EXPLOSION_LARGE_D = 3722;
	public static final int SPR_EXPLOSION_LARGE_E = 3723;
	public static final int SPR_EXPLOSION_LARGE_F = 3724;

	public static final int SPR_EXPLOSION_SMALL_0 = 3725;
	public static final int SPR_EXPLOSION_SMALL_1 = 3726;
	public static final int SPR_EXPLOSION_SMALL_2 = 3727;
	public static final int SPR_EXPLOSION_SMALL_3 = 3728;
	public static final int SPR_EXPLOSION_SMALL_4 = 3729;
	public static final int SPR_EXPLOSION_SMALL_5 = 3730;
	public static final int SPR_EXPLOSION_SMALL_6 = 3731;
	public static final int SPR_EXPLOSION_SMALL_7 = 3732;
	public static final int SPR_EXPLOSION_SMALL_8 = 3733;
	public static final int SPR_EXPLOSION_SMALL_9 = 3734;
	public static final int SPR_EXPLOSION_SMALL_A = 3735;
	public static final int SPR_EXPLOSION_SMALL_B = 3736;

	public static final int SPR_BREAKDOWN_SMOKE_0 = 3737;
	public static final int SPR_BREAKDOWN_SMOKE_1 = 3738;
	public static final int SPR_BREAKDOWN_SMOKE_2 = 3739;
	public static final int SPR_BREAKDOWN_SMOKE_3 = 3740;

	public static final int SPR_BUBBLE_0 = 4748;
	public static final int SPR_BUBBLE_1 = 4749;
	public static final int SPR_BUBBLE_2 = 4750;
	public static final int SPR_BUBBLE_GENERATE_0 = 4751;
	public static final int SPR_BUBBLE_GENERATE_1 = 4752;
	public static final int SPR_BUBBLE_GENERATE_2 = 4753;
	public static final int SPR_BUBBLE_GENERATE_3 = 4754;
	public static final int SPR_BUBBLE_BURST_0 = 4755;
	public static final int SPR_BUBBLE_BURST_1 = 4756;
	public static final int SPR_BUBBLE_BURST_2 = 4757;
	public static final int SPR_BUBBLE_ABSORB_0 = 4758;
	public static final int SPR_BUBBLE_ABSORB_1 = 4759;
	public static final int SPR_BUBBLE_ABSORB_2 = 4760;
	public static final int SPR_BUBBLE_ABSORB_3 = 4761;
	public static final int SPR_BUBBLE_ABSORB_4 = 4762;

	/* road_gui.c */
	public static final int SPR_IMG_ROAD_NW				= 1309;
	public static final int SPR_IMG_ROAD_NE				= 1310;
	public static final int SPR_IMG_ROAD_DEPOT		= 1295;
	public static final int SPR_IMG_BUS_STATION		= 749;
	public static final int SPR_IMG_TRUCK_BAY			= 750;
	public static final int SPR_IMG_BRIDGE				= 2594;
	public static final int SPR_IMG_ROAD_TUNNEL		= 2429;
	public static final int SPR_IMG_REMOVE				= 714;

	/* rail_gui.c */
	public static final int SPR_IMG_AUTORAIL   = SPR_OPENTTD_BASE + 0;
	public static final int SPR_IMG_AUTOMONO   = SPR_OPENTTD_BASE + 1;
	public static final int SPR_IMG_AUTOMAGLEV = SPR_OPENTTD_BASE + 2;

	public static final int SPR_IMG_WAYPOINT = SPR_OPENTTD_BASE + 3;

	public static final int SPR_IMG_DEPOT_RAIL   = 1294;
	public static final int SPR_IMG_DEPOT_MONO   = SPR_OPENTTD_BASE + 9;
	public static final int SPR_IMG_DEPOT_MAGLEV = SPR_OPENTTD_BASE + 10;

	public static final int SPR_IMG_TUNNEL_RAIL   = 2430;
	public static final int SPR_IMG_TUNNEL_MONO   = 2431;
	public static final int SPR_IMG_TUNNEL_MAGLEV = 2432;

	public static final int SPR_IMG_CONVERT_RAIL   = SPR_OPENTTD_BASE + 22;
	public static final int SPR_IMG_CONVERT_MONO   = SPR_OPENTTD_BASE + 24;
	public static final int SPR_IMG_CONVERT_MAGLEV = SPR_OPENTTD_BASE + 26;
			//};

			/** Cursor sprite numbers */
			//typedef enum CursorSprites {
			/* Terraform */
			/* Cursors */
			public static final int SPR_CURSOR_MOUSE          = 0;
	public static final int SPR_CURSOR_ZZZ            = 1;
	public static final int SPR_CURSOR_BOUY           = 702;
	public static final int SPR_CURSOR_QUERY          = 719;
	public static final int SPR_CURSOR_HQ             = 720;
	public static final int SPR_CURSOR_SHIP_DEPOT     = 721;
	public static final int SPR_CURSOR_SIGN           = 722;

	public static final int SPR_CURSOR_TREE           = 2010;
	public static final int SPR_CURSOR_BUY_LAND       = 4792;
	public static final int SPR_CURSOR_LEVEL_LAND     =SPR_OPENTTD_BASE + 62;

	public static final int SPR_CURSOR_TOWN           = 4080;
	public static final int SPR_CURSOR_INDUSTRY       = 4081;
	public static final int SPR_CURSOR_ROCKY_AREA     = 4087;
	public static final int SPR_CURSOR_LIGHTHOUSE     = 4088;
	public static final int SPR_CURSOR_TRANSMITTER    = 4089;

	/* airport cursors */
	public static final int SPR_CURSOR_AIRPORT        = 2724;

	/* dock cursors */
	public static final int SPR_CURSOR_DOCK           = 3668;
	public static final int SPR_CURSOR_CANAL          =SPR_OPENTTD_BASE + 8;
	public static final int SPR_CURSOR_LOCK           =SPR_OPENTTD_BASE + 57;

	/* shared road & rail cursors */
	public static final int SPR_CURSOR_BRIDGE         = 2593;

	/* rail cursors */
	public static final int SPR_CURSOR_NS_TRACK       = 1263;
	public static final int SPR_CURSOR_SWNE_TRACK     = 1264;
	public static final int SPR_CURSOR_EW_TRACK       = 1265;
	public static final int SPR_CURSOR_NWSE_TRACK     = 1266;

	public static final int SPR_CURSOR_NS_MONO        = 1267;
	public static final int SPR_CURSOR_SWNE_MONO      = 1268;
	public static final int SPR_CURSOR_EW_MONO        = 1269;
	public static final int SPR_CURSOR_NWSE_MONO      = 1270;

	public static final int SPR_CURSOR_NS_MAGLEV      = 1271;
	public static final int SPR_CURSOR_SWNE_MAGLEV    = 1272;
	public static final int SPR_CURSOR_EW_MAGLEV      = 1273;
	public static final int SPR_CURSOR_NWSE_MAGLEV    = 1274;

	public static final int SPR_CURSOR_RAIL_STATION   = 1300;

	public static final int SPR_CURSOR_TUNNEL_RAIL    = 2434;
	public static final int SPR_CURSOR_TUNNEL_MONO    = 2435;
	public static final int SPR_CURSOR_TUNNEL_MAGLEV  = 2436;

	public static final int SPR_CURSOR_AUTORAIL       = SPR_OPENTTD_BASE + 4;
	public static final int SPR_CURSOR_AUTOMONO       = SPR_OPENTTD_BASE + 5;
	public static final int SPR_CURSOR_AUTOMAGLEV     = SPR_OPENTTD_BASE + 6;

	public static final int SPR_CURSOR_WAYPOINT       = SPR_OPENTTD_BASE + 7;

	public static final int SPR_CURSOR_RAIL_DEPOT     = 1296;
	public static final int SPR_CURSOR_MONO_DEPOT     = SPR_OPENTTD_BASE + 11;
	public static final int SPR_CURSOR_MAGLEV_DEPOT   = SPR_OPENTTD_BASE + 12;

	public static final int SPR_CURSOR_CONVERT_RAIL   = SPR_OPENTTD_BASE + 23;
	public static final int SPR_CURSOR_CONVERT_MONO   = SPR_OPENTTD_BASE + 25;
	public static final int SPR_CURSOR_CONVERT_MAGLEV = SPR_OPENTTD_BASE + 27;

	/* road cursors */
	public static final int SPR_CURSOR_ROAD_NESW      = 1311;
	public static final int SPR_CURSOR_ROAD_NWSE      = 1312;

	public static final int SPR_CURSOR_ROAD_DEPOT     = 1297;
	public static final int SPR_CURSOR_BUS_STATION    = 2725;
	public static final int SPR_CURSOR_TRUCK_STATION  = 2726;
	public static final int SPR_CURSOR_ROAD_TUNNEL    = 2433;

	public static final int SPR_CURSOR_CLONE = SPR_OPENTTD_BASE + 93;
	//} CursorSprite;

	/// Animation macro in table/animcursors.h (_animcursors[])
	//enum AnimCursors {
	public static final int ANIMCURSOR_DEMOLISH     = -1;	///<  704 -  707 - demolish dynamite
	public static final int ANIMCURSOR_LOWERLAND    = -2;	///<  699 -  701 - lower land tool
	public static final int ANIMCURSOR_RAISELAND    = -3;	///<  696 -  698 - raise land tool
	public static final int ANIMCURSOR_PICKSTATION  = -4;	///<  716 -  718 - goto-order icon
	public static final int ANIMCURSOR_BUILDSIGNALS	= -5;	///< 1292 - 1293 - build signal
	//};

	/**
	 * Bitmask setup. For the graphics system, 32 bits are used to define
	 * the sprite to be displayed. This variable contains various information:<p>
	 * <ul><li> SPRITE_WIDTH is the number of bits used for the actual sprite to be displayed.
	 * This always starts at bit 0.</li>
	 * <li> TRANSPARENT_BIT is the bit number which toggles sprite transparency</li>
	 * <li> RECOLOR_BIT toggles the recoloring system</li>
	 * <li> PALETTE_SPRITE_WIDTH and PALETTE_SPRITE_START determine the position and number of
	 * bits used for the recoloring process. For transparency, it must be 0x322.</li>
	 */
	//enum SpriteSetup {
	public static final int TRANSPARENT_BIT = 31;       ///< toggles transparency in the sprite
	public static final int RECOLOR_BIT = 15;           ///< toggles recoloring in the sprite
	public static final int PALETTE_SPRITE_START = 16;  ///< number of the first bit of the sprite containing the recolor palette
	public static final int PALETTE_SPRITE_WIDTH = 11;  ///< number of bits of the sprite containing the recolor palette
	public static final int SPRITE_WIDTH = 14;          ///< number of bits for the sprite number
	//};

	/**
	 * these masks change the colors of the palette for a sprite.
	 * Apart from this bit, a sprite number is needed to define
	 * the palette used for recoloring. This palette is stored
	 * in the bits marked by PALETTE_SPRITE_MASK.
	 * @note Do not modify this enum. Alter SpriteSetup instead
	 * @see SpriteSetup
	 */
	//enum Modifiers {
		///when a sprite is to be displayed transparently, this bit needs to be set.
	public static final int PALETTE_MODIFIER_TRANSPARENT 	= 1 << TRANSPARENT_BIT;
				///this bit is set when a recoloring process is in action
	public static final int PALETTE_MODIFIER_COLOR 				= 1 << RECOLOR_BIT;

				//This is used for the GfxFillRect function
				///Used to draw a "grey out" rectangle. @see GfxFillRect
	public static final int PALETTE_MODIFIER_GREYOUT        = 1 << TRANSPARENT_BIT;
				///Set when a colortable mode is used. @see GfxFillRect
	public static final int USE_COLORTABLE                  = 1 << RECOLOR_BIT;
	//};

	/** Masks needed for sprite operations.
	 * @note Do not modify this enum. Alter SpriteSetup instead
	 * @see SpriteSetup
	 */
	//enum SpriteMasks {
	///Maximum number of sprites that can be loaded at a given time.
	public static final int MAX_SPRITES = (1 << SPRITE_WIDTH) - 1;
	///The mask to for the main sprite
	public static final int SPRITE_MASK = MAX_SPRITES;
	///The mask for the auxiliary sprite (the one that takes care of recoloring)
	public static final int PALETTE_SPRITE_MASK = ((1 << PALETTE_SPRITE_WIDTH) - 1) << PALETTE_SPRITE_START;
	///Mask for the auxiliary sprites if it is locate in the LSBs
	public static final int COLORTABLE_MASK = (1 << PALETTE_SPRITE_WIDTH) - 1;
	//};

	/*
	assert_compile( (1 << TRANSPARENT_BIT & SPRITE_MASK) == 0 );
	assert_compile( (1 << RECOLOR_BIT & SPRITE_MASK) == 0 );
	assert_compile( TRANSPARENT_BIT != RECOLOR_BIT );
	assert_compile( (1 << TRANSPARENT_BIT & PALETTE_SPRITE_MASK) == 0);
	assert_compile( (1 << RECOLOR_BIT & PALETTE_SPRITE_MASK) == 0 );
	assert_compile( (PALETTE_SPRITE_MASK & SPRITE_MASK) == 0 );
	assert_compile( SPRITE_WIDTH + PALETTE_SPRITE_WIDTH <= 30 );
	 */

	static int PALETTE_RECOLOR_SPRITE(int a) { return (a << PALETTE_SPRITE_START | PALETTE_MODIFIER_COLOR); }

	//enum PaletteSprites {
	//note: these numbers are already the modified once the renderer needs.
	//the actual sprite number is the upper 16 bits of the number

	///Here a puslating red tile is drawn if you try to build a wrong tunnel or raise/lower land where it is not possible
	public static final int PALETTE_TILE_RED_PULSATING 	= PALETTE_RECOLOR_SPRITE(0x303);
	///makes a square red. is used when removing rails or other stuff
	public static final int PALETTE_SEL_TILE_RED 				= PALETTE_RECOLOR_SPRITE(0x304);
	///This draws a blueish square (catchment areas for example)
	public static final int PALETTE_SEL_TILE_BLUE 			= PALETTE_RECOLOR_SPRITE(0x305);
	//0x306 is a real sprite (the little dot you get when you try to raise/lower a corner of the map
	//here the color switches begin
	//use this if you add stuff to the value, so that the resulting color
	//is not a fixed value.
	//NOTE THAT THE SWITCH 0x8000 is NOT present in _TO_COLORS yet!
	public static final int PALETTE_TO_COLORS 					= 0x307 << PALETTE_SPRITE_START;
	public static final int PALETTE_TO_DARK_BLUE 				= PALETTE_RECOLOR_SPRITE(0x307);
	public static final int PALETTE_TO_PALE_GREEN 			= PALETTE_RECOLOR_SPRITE(0x308);
	public static final int PALETTE_TO_PINK 						= PALETTE_RECOLOR_SPRITE(0x309);
	public static final int PALETTE_TO_YELLOW 					= PALETTE_RECOLOR_SPRITE(0x30A);
	public static final int PALETTE_TO_RED 							= PALETTE_RECOLOR_SPRITE(0x30B);
	public static final int PALETTE_TO_LIGHT_BLUE 			= PALETTE_RECOLOR_SPRITE(0x30C);
	public static final int PALETTE_TO_GREEN 						= PALETTE_RECOLOR_SPRITE(0x30D);
	public static final int PALETTE_TO_DARK_GREEN 			= PALETTE_RECOLOR_SPRITE(0x30E);
	public static final int PALETTE_TO_BLUE 						= PALETTE_RECOLOR_SPRITE(0x30F);
	public static final int PALETTE_TO_CREAM 						= PALETTE_RECOLOR_SPRITE(0x310);
	//maybe don't use as player color because it doesn't display in the graphs?
	public static final int PALETTE_TO_MAUVE 						= PALETTE_RECOLOR_SPRITE(0x311);
	public static final int PALETTE_TO_PURPLE 					= PALETTE_RECOLOR_SPRITE(0x312);
	public static final int PALETTE_TO_ORANGE 					= PALETTE_RECOLOR_SPRITE(0x313);
	public static final int PALETTE_TO_BROWN 						= PALETTE_RECOLOR_SPRITE(0x314);
	public static final int PALETTE_TO_GREY 						= PALETTE_RECOLOR_SPRITE(0x315);
	public static final int PALETTE_TO_WHITE 						= PALETTE_RECOLOR_SPRITE(0x316);
	//sets color to bare land stuff; for rail and road (and crossings)
	public static final int PALETTE_TO_BARE_LAND 				= PALETTE_RECOLOR_SPRITE(0x317);
	//XXX is 318-31A really not used?
	public static final int PALETTE_TO_STRUCT_BLUE      = PALETTE_RECOLOR_SPRITE(0x31B);
	//structure color to something brownish (for the cantilever bridges for example)
	public static final int PALETTE_TO_STRUCT_BROWN 		= PALETTE_RECOLOR_SPRITE(0x31C);
	public static final int PALETTE_TO_STRUCT_WHITE     = PALETTE_RECOLOR_SPRITE(0x31D);
	//sets bridge or structure to red; little concrete one and cantilever use this one for example
	public static final int PALETTE_TO_STRUCT_RED 			= PALETTE_RECOLOR_SPRITE(0x31E);
	public static final int PALETTE_TO_STRUCT_GREEN     = PALETTE_RECOLOR_SPRITE(0x31F);
	public static final int PALETTE_TO_STRUCT_CONCRETE 	= PALETTE_RECOLOR_SPRITE(0x320);  //Sets the suspension bridge to concrete; also other strucutures use it
	public static final int PALETTE_TO_STRUCT_YELLOW 		= PALETTE_RECOLOR_SPRITE(0x321);    //Sets the bridge color to yellow (suspension and tubular)
	public static final int PALETTE_TO_TRANSPARENT 			= 0x322 << PALETTE_SPRITE_START | PALETTE_MODIFIER_TRANSPARENT;	//This sets the sprite to transparent
	//This is used for changing the tubular bridges to the silicon display; or some grayish color
	public static final int PALETTE_TO_STRUCT_GREY 			= PALETTE_RECOLOR_SPRITE(0x323);
	public static final int PALETTE_CRASH 							= PALETTE_RECOLOR_SPRITE(0x324);	//this changes stuff to the "crash color"
	//XXX another place where structures are colored.
	//I'm not sure which colors these are
	public static final int PALETTE_59E 								= PALETTE_RECOLOR_SPRITE(0x59E);
	public static final int PALETTE_59F 								= PALETTE_RECOLOR_SPRITE(0x59F);

	public static int RET_MAKE_TRANSPARENT(int img) { return (img & SPRITE_MASK) | PALETTE_TO_TRANSPARENT; }

}

//#undef PALETTE_RECOLOR_SPRITE

//#define 

//#endif /* SPRITES_H */



