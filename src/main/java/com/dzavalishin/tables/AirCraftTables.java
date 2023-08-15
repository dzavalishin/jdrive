package com.dzavalishin.tables;

import com.dzavalishin.struct.TileIndexDiffC;

public class AirCraftTables extends AirConstants
{


	///////////////////////////////////////////////////////////////////////
	/////**********Movement Machine on Airports*********************///////
	//first element of depots array tells us how many depots there are (to know size of array)
	//this may be changed later when airports are moved to external file
	static public final TileIndexDiffC _airport_depots_country[] = { new TileIndexDiffC(3, 0) };

	//static public final int  _airport_terminal_country[] = {1, 2};
	static public final byte  _airport_terminal_country[] = {1, 2};
	
	static public final AirportFTAbuildup _airport_fta_country[] = {
			new AirportFTAbuildup( 0,HANGAR,NOTHING_block,1),
			new AirportFTAbuildup( 1,255,AIRPORT_BUSY_block,0), new AirportFTAbuildup(1,HANGAR,0,0), new AirportFTAbuildup(1,TERM1,TERM1_block,2), new AirportFTAbuildup(1,TERM2,0,4), new AirportFTAbuildup(1,HELITAKEOFF,0,19), new AirportFTAbuildup(1,0,0,6),
			new AirportFTAbuildup( 2,TERM1,TERM1_block,1),
			new AirportFTAbuildup( 3,TERM2,TERM2_block,5),
			new AirportFTAbuildup( 4,255,AIRPORT_BUSY_block,0), new AirportFTAbuildup(4,TERM2,0,5), new AirportFTAbuildup(4,HANGAR,0,1), new AirportFTAbuildup(4,TAKEOFF,0,6), new AirportFTAbuildup(4,HELITAKEOFF,0,1),
			new AirportFTAbuildup( 5,255,AIRPORT_BUSY_block,0), new AirportFTAbuildup(5,TERM2,TERM2_block,3), new AirportFTAbuildup(5,0,0,4),
			new AirportFTAbuildup( 6,0,AIRPORT_BUSY_block,7),
			// takeoff
			new AirportFTAbuildup( 7,TAKEOFF,AIRPORT_BUSY_block,8),
			new AirportFTAbuildup( 8,STARTTAKEOFF,NOTHING_block,9),
			new AirportFTAbuildup( 9,ENDTAKEOFF,NOTHING_block,0),
			// landing
			new AirportFTAbuildup(10,FLYING,NOTHING_block,15), new AirportFTAbuildup(10,LANDING,0,11), new AirportFTAbuildup(10,HELILANDING,0,20),
			new AirportFTAbuildup(11,LANDING,AIRPORT_BUSY_block,12),
			new AirportFTAbuildup(12,0,AIRPORT_BUSY_block,13),
			new AirportFTAbuildup(13,ENDLANDING,AIRPORT_BUSY_block,14), new AirportFTAbuildup(13,TERM2,0,5), new AirportFTAbuildup(13,0,0,14),
			new AirportFTAbuildup(14,0,AIRPORT_BUSY_block,1),
			// In air
			new AirportFTAbuildup(15,0,NOTHING_block,16),
			new AirportFTAbuildup(16,0,NOTHING_block,17),
			new AirportFTAbuildup(17,0,NOTHING_block,18),
			new AirportFTAbuildup(18,0,NOTHING_block,10),
			new AirportFTAbuildup(19,HELITAKEOFF,NOTHING_block,0),
			new AirportFTAbuildup(20,HELILANDING,AIRPORT_BUSY_block,21),
			new AirportFTAbuildup(21,HELIENDLANDING,AIRPORT_BUSY_block,1),
			new AirportFTAbuildup(MAX_ELEMENTS,0,0,0) // end marker. DO NOT REMOVE
	};

	static public final TileIndexDiffC _airport_depots_city[] = {new TileIndexDiffC(5, 0)};
	static public final byte _airport_terminal_city[] = {1, 3};
	static public final AirportFTAbuildup _airport_fta_city[] = {
			new AirportFTAbuildup( 0,HANGAR,NOTHING_block,1), new AirportFTAbuildup(0,TAKEOFF,OUT_WAY_block,1), new AirportFTAbuildup(0,0,0,1),
			new AirportFTAbuildup( 1,255,TAXIWAY_BUSY_block,0), new AirportFTAbuildup(1,HANGAR,0,0), new AirportFTAbuildup(1,TERM2,0,6), new AirportFTAbuildup(1,TERM3,0,6), new AirportFTAbuildup(1,0,0,7), // for all else, go to 7
			new AirportFTAbuildup( 2,TERM1,TERM1_block,7), new AirportFTAbuildup(2,TAKEOFF,OUT_WAY_block,7), new AirportFTAbuildup(2,0,0,7),
			new AirportFTAbuildup( 3,TERM2,TERM2_block,5), new AirportFTAbuildup(3,TAKEOFF,OUT_WAY_block,5), new AirportFTAbuildup(3,0,0,5),
			new AirportFTAbuildup( 4,TERM3,TERM3_block,5), new AirportFTAbuildup(4,TAKEOFF,OUT_WAY_block,5), new AirportFTAbuildup(4,0,0,5),
			new AirportFTAbuildup( 5,255,TAXIWAY_BUSY_block,0), new AirportFTAbuildup(5,TERM2,TERM2_block,3), new AirportFTAbuildup(5,TERM3,TERM3_block,4), new AirportFTAbuildup(5,0,0,6),
			new AirportFTAbuildup( 6,255,TAXIWAY_BUSY_block,0), new AirportFTAbuildup(6,TERM2,0,5), new AirportFTAbuildup(6,TERM3,0,5), new AirportFTAbuildup(6,HANGAR,0,1), new AirportFTAbuildup(6,0,0,7),
			new AirportFTAbuildup( 7,255,TAXIWAY_BUSY_block,0), new AirportFTAbuildup(7,TERM1,TERM1_block,2), new AirportFTAbuildup(7,TAKEOFF,OUT_WAY_block,8), new AirportFTAbuildup(7,HELITAKEOFF,0,22), new AirportFTAbuildup(7,HANGAR,0,1), new AirportFTAbuildup(7,0,0,6),
			new AirportFTAbuildup( 8,0,OUT_WAY_block,9),
			new AirportFTAbuildup( 9,0,RUNWAY_IN_OUT_block,10),
			// takeoff
			new AirportFTAbuildup(10,TAKEOFF,RUNWAY_IN_OUT_block,11),
			new AirportFTAbuildup(11,STARTTAKEOFF,NOTHING_block,12),
			new AirportFTAbuildup(12,ENDTAKEOFF,NOTHING_block,0),
			// landing
			new AirportFTAbuildup(13,FLYING,NOTHING_block,18), new AirportFTAbuildup(13,LANDING,0,14), new AirportFTAbuildup(13,HELILANDING,0,23),
			new AirportFTAbuildup(14,LANDING,RUNWAY_IN_OUT_block,15),
			new AirportFTAbuildup(15,0,RUNWAY_IN_OUT_block,16),
			new AirportFTAbuildup(16,0,RUNWAY_IN_OUT_block,17),
			new AirportFTAbuildup(17,ENDLANDING,IN_WAY_block,7),
			// In Air
			new AirportFTAbuildup(18,0,NOTHING_block,19),
			new AirportFTAbuildup(19,0,NOTHING_block,20),
			new AirportFTAbuildup(20,0,NOTHING_block,21),
			new AirportFTAbuildup(21,0,NOTHING_block,13),
			// helicopter
			new AirportFTAbuildup(22,HELITAKEOFF,NOTHING_block,0),
			new AirportFTAbuildup(23,HELILANDING,IN_WAY_block,24),
			new AirportFTAbuildup(24,HELIENDLANDING,IN_WAY_block,17),
			new AirportFTAbuildup(MAX_ELEMENTS,0,0,0) // end marker. DO NOT REMOVE
	};

	static public final TileIndexDiffC _airport_depots_metropolitan[] = {new TileIndexDiffC(5, 0) };
	static public final byte _airport_terminal_metropolitan[] = {1, 3};
	static public final AirportFTAbuildup _airport_fta_metropolitan[] = {
			new AirportFTAbuildup( 0,HANGAR,NOTHING_block,1),
			new AirportFTAbuildup( 1,255,TAXIWAY_BUSY_block,0), new AirportFTAbuildup(1,HANGAR,0,0), new AirportFTAbuildup(1,TERM2,0,6), new AirportFTAbuildup(1,TERM3,0,6), new AirportFTAbuildup(1,0,0,7), // for all else, go to 7
			new AirportFTAbuildup( 2,TERM1,TERM1_block,7),
			new AirportFTAbuildup( 3,TERM2,TERM2_block,5),
			new AirportFTAbuildup( 4,TERM3,TERM3_block,5),
			new AirportFTAbuildup( 5,255,TAXIWAY_BUSY_block,0), new AirportFTAbuildup(5,TERM2,TERM2_block,3), new AirportFTAbuildup(5,TERM3,TERM3_block,4), new AirportFTAbuildup(5,0,0,6),
			new AirportFTAbuildup( 6,255,TAXIWAY_BUSY_block,0), new AirportFTAbuildup(6,TERM2,0,5), new AirportFTAbuildup(6,TERM3,0,5), new AirportFTAbuildup(6,HANGAR,0,1), new AirportFTAbuildup(6,0,0,7),
			new AirportFTAbuildup( 7,255,TAXIWAY_BUSY_block,0), new AirportFTAbuildup(7,TERM1,TERM1_block,2), new AirportFTAbuildup(7,TAKEOFF,0,8), new AirportFTAbuildup(7,HELITAKEOFF,0,23), new AirportFTAbuildup(7,HANGAR,0,1), new AirportFTAbuildup(7,0,0,6),
			new AirportFTAbuildup( 8,0,OUT_WAY_block,9),
			new AirportFTAbuildup( 9,0,RUNWAY_OUT_block,10),
			// takeoff
			new AirportFTAbuildup(10,TAKEOFF,RUNWAY_OUT_block,11),
			new AirportFTAbuildup(11,STARTTAKEOFF,NOTHING_block,12),
			new AirportFTAbuildup(12,ENDTAKEOFF,NOTHING_block,0),
			// landing
			new AirportFTAbuildup(13,FLYING,NOTHING_block,19), new AirportFTAbuildup(13,LANDING,0,14), new AirportFTAbuildup(13,HELILANDING,0,25),
			new AirportFTAbuildup(14,LANDING,RUNWAY_IN_block,15),
			new AirportFTAbuildup(15,0,RUNWAY_IN_block,16),
			new AirportFTAbuildup(16,255,RUNWAY_IN_block,0), new AirportFTAbuildup(16,ENDLANDING,IN_WAY_block,17),
			new AirportFTAbuildup(17,255,RUNWAY_OUT_block,0), new AirportFTAbuildup(17,ENDLANDING,IN_WAY_block,18),
			new AirportFTAbuildup(18,ENDLANDING,IN_WAY_block,7),
			// In Air
			new AirportFTAbuildup(19,0,NOTHING_block,20),
			new AirportFTAbuildup(20,0,NOTHING_block,21),
			new AirportFTAbuildup(21,0,NOTHING_block,22),
			new AirportFTAbuildup(22,0,NOTHING_block,13),
			// helicopter
			new AirportFTAbuildup(23,0,NOTHING_block,24),
			new AirportFTAbuildup(24,HELITAKEOFF,NOTHING_block,0),
			new AirportFTAbuildup(25,HELILANDING,IN_WAY_block,26),
			new AirportFTAbuildup(26,HELIENDLANDING,IN_WAY_block,18),
			new AirportFTAbuildup(MAX_ELEMENTS,0,0,0) // end marker. DO NOT REMOVE
	};

	static public final TileIndexDiffC _airport_depots_international[] = {
			new TileIndexDiffC(0, 3), 
			new TileIndexDiffC(6, 1)
			};
	
	static public final byte _airport_terminal_international[] = {2, 3, 3};
	static public final byte _airport_helipad_international[] = {1, 2};
	static public final AirportFTAbuildup _airport_fta_international[] = {
			new AirportFTAbuildup( 0,HANGAR,NOTHING_block,2), new AirportFTAbuildup(0,255,TERM_GROUP1_block,0), new AirportFTAbuildup(0,255,TERM_GROUP2_ENTER1_block,1), new AirportFTAbuildup(0,HELITAKEOFF,HELIPAD1_block,2), new AirportFTAbuildup(0,0,0,2),
			new AirportFTAbuildup( 1,HANGAR,NOTHING_block,3), new AirportFTAbuildup(1,255,HANGAR2_AREA_block,1), new AirportFTAbuildup(1,HELITAKEOFF,HELIPAD2_block,3), new AirportFTAbuildup(1,0,0,3),
			new AirportFTAbuildup( 2,255,AIRPORT_ENTRANCE_block,0), new AirportFTAbuildup(2,HANGAR,0,0), new AirportFTAbuildup(2,TERM4,0,12), new AirportFTAbuildup(2,TERM5,0,12), new AirportFTAbuildup(2,TERM6,0,12), new AirportFTAbuildup(2,HELIPAD1,0,12), new AirportFTAbuildup(2,HELIPAD2,0,12), new AirportFTAbuildup(2,HELITAKEOFF,0,12), new AirportFTAbuildup(2,0,0,23),
			new AirportFTAbuildup( 3,255,HANGAR2_AREA_block,0), new AirportFTAbuildup(3,HANGAR,0,1), new AirportFTAbuildup(3,0,0,18),
			new AirportFTAbuildup( 4,TERM1,TERM1_block,23), new AirportFTAbuildup(4,HANGAR,AIRPORT_ENTRANCE_block,23), new AirportFTAbuildup(4,0,0,23),
			new AirportFTAbuildup( 5,TERM2,TERM2_block,24), new AirportFTAbuildup(5,HANGAR,AIRPORT_ENTRANCE_block,24), new AirportFTAbuildup(5,0,0,24),
			new AirportFTAbuildup( 6,TERM3,TERM3_block,25), new AirportFTAbuildup(6,HANGAR,AIRPORT_ENTRANCE_block,25), new AirportFTAbuildup(6,0,0,25),
			new AirportFTAbuildup( 7,TERM4,TERM4_block,16), new AirportFTAbuildup(7,HANGAR,HANGAR2_AREA_block,16), new AirportFTAbuildup(7,0,0,16),
			new AirportFTAbuildup( 8,TERM5,TERM5_block,17), new AirportFTAbuildup(8,HANGAR,HANGAR2_AREA_block,17), new AirportFTAbuildup(8,0,0,17),
			new AirportFTAbuildup( 9,TERM6,TERM6_block,18), new AirportFTAbuildup(9,HANGAR,HANGAR2_AREA_block,18), new AirportFTAbuildup(9,0,0,18),
			new AirportFTAbuildup(10,HELIPAD1,HELIPAD1_block,10), new AirportFTAbuildup(10,HANGAR,HANGAR2_AREA_block,16), new AirportFTAbuildup(10,HELITAKEOFF,0,47),
			new AirportFTAbuildup(11,HELIPAD2,HELIPAD2_block,11), new AirportFTAbuildup(11,HANGAR,HANGAR2_AREA_block,17), new AirportFTAbuildup(11,HELITAKEOFF,0,48),
			new AirportFTAbuildup(12,0,TERM_GROUP2_ENTER1_block,13),
			new AirportFTAbuildup(13,0,TERM_GROUP2_ENTER1_block,14),
			new AirportFTAbuildup(14,0,TERM_GROUP2_ENTER2_block,15),
			new AirportFTAbuildup(15,0,TERM_GROUP2_ENTER2_block,16),
			new AirportFTAbuildup(16,255,TERM_GROUP2_block,0), new AirportFTAbuildup(16,TERM4,TERM4_block,7), new AirportFTAbuildup(16,HELIPAD1,HELIPAD1_block,10), new AirportFTAbuildup(16,HELITAKEOFF,HELIPAD1_block,10), new AirportFTAbuildup(16,0,0,17),
			new AirportFTAbuildup(17,255,TERM_GROUP2_block,0), new AirportFTAbuildup(17,TERM5,TERM5_block,8), new AirportFTAbuildup(17,TERM4,0,16), new AirportFTAbuildup(17,HELIPAD1,0,16), new AirportFTAbuildup(17,HELIPAD2,HELIPAD2_block,11), new AirportFTAbuildup(17,HELITAKEOFF,HELIPAD2_block,11), new AirportFTAbuildup(17,0,0,18),
			new AirportFTAbuildup(18,255,TERM_GROUP2_block,0), new AirportFTAbuildup(18,TERM6,TERM6_block,9), new AirportFTAbuildup(18,TAKEOFF,0,19), new AirportFTAbuildup(18,HANGAR,HANGAR2_AREA_block,3), new AirportFTAbuildup(18,0,0,17),
			new AirportFTAbuildup(19,0,TERM_GROUP2_EXIT1_block,20),
			new AirportFTAbuildup(20,0,TERM_GROUP2_EXIT1_block,21),
			new AirportFTAbuildup(21,0,TERM_GROUP2_EXIT2_block,22),
			new AirportFTAbuildup(22,0,TERM_GROUP2_EXIT2_block,26),
			new AirportFTAbuildup(23,255,TERM_GROUP1_block,0), new AirportFTAbuildup(23,TERM1,TERM1_block,4), new AirportFTAbuildup(23,HANGAR,AIRPORT_ENTRANCE_block,2), new AirportFTAbuildup(23,0,0,24),
			new AirportFTAbuildup(24,255,TERM_GROUP1_block,0), new AirportFTAbuildup(24,TERM2,TERM2_block,5), new AirportFTAbuildup(24,TERM1,0,23), new AirportFTAbuildup(24,HANGAR,0,23), new AirportFTAbuildup(24,0,0,25),
			new AirportFTAbuildup(25,255,TERM_GROUP1_block,0), new AirportFTAbuildup(25,TERM3,TERM3_block,6), new AirportFTAbuildup(25,TAKEOFF,0,26), new AirportFTAbuildup(25,0,0,24),
			new AirportFTAbuildup(26,255,TAXIWAY_BUSY_block,0), new AirportFTAbuildup(26,TAKEOFF,0,27), new AirportFTAbuildup(26,0,0,25),
			new AirportFTAbuildup(27,0,OUT_WAY_block,28),
			// takeoff
			new AirportFTAbuildup(28,TAKEOFF,OUT_WAY_block,29),
			new AirportFTAbuildup(29,0,RUNWAY_OUT_block,30),
			new AirportFTAbuildup(30,STARTTAKEOFF,NOTHING_block,31),
			new AirportFTAbuildup(31,ENDTAKEOFF,NOTHING_block,0),
			// landing
			new AirportFTAbuildup(32,FLYING,NOTHING_block,37), new AirportFTAbuildup(32,LANDING,0,33), new AirportFTAbuildup(32,HELILANDING,0,41),
			new AirportFTAbuildup(33,LANDING,RUNWAY_IN_block,34),
			new AirportFTAbuildup(34,0,RUNWAY_IN_block,35),
			new AirportFTAbuildup(35,0,RUNWAY_IN_block,36),
			new AirportFTAbuildup(36,ENDLANDING,IN_WAY_block,36), new AirportFTAbuildup(36,255,TERM_GROUP1_block,0), new AirportFTAbuildup(36,255,TERM_GROUP2_ENTER1_block,1), new AirportFTAbuildup(36,TERM4,0,12), new AirportFTAbuildup(36,TERM5,0,12), new AirportFTAbuildup(36,TERM6,0,12), new AirportFTAbuildup(36,0,0,2),
			// In Air
			new AirportFTAbuildup(37,0,NOTHING_block,38),
			new AirportFTAbuildup(38,0,NOTHING_block,39),
			new AirportFTAbuildup(39,0,NOTHING_block,40),
			new AirportFTAbuildup(40,0,NOTHING_block,32),
			// Helicopter -- stay in air in special place as a buffer to choose from helipads
			new AirportFTAbuildup(41,HELILANDING,PRE_HELIPAD_block,42),
			new AirportFTAbuildup(42,HELIENDLANDING,PRE_HELIPAD_block,42), new AirportFTAbuildup(42,HELIPAD1,0,43), new AirportFTAbuildup(42,HELIPAD2,0,44), new AirportFTAbuildup(42,HANGAR,0,49),
			new AirportFTAbuildup(43,0,NOTHING_block,45),
			new AirportFTAbuildup(44,0,NOTHING_block,46),
			// landing
			new AirportFTAbuildup(45,255,NOTHING_block,0), new AirportFTAbuildup(45,HELIPAD1,HELIPAD1_block,10),
			new AirportFTAbuildup(46,255,NOTHING_block,0), new AirportFTAbuildup(46,HELIPAD2,HELIPAD2_block,11),
			// Helicopter -- takeoff
			new AirportFTAbuildup(47,HELITAKEOFF,NOTHING_block,0),
			new AirportFTAbuildup(48,HELITAKEOFF,NOTHING_block,0),
			new AirportFTAbuildup(49,0,HANGAR2_AREA_block,50), // need to go to hangar when waiting in air
			new AirportFTAbuildup(50,0,HANGAR2_AREA_block,3),
			new AirportFTAbuildup(MAX_ELEMENTS,0,0,0) // end marker. DO NOT REMOVE
	};

	//heliports, oilrigs don't have depots
	static public final byte _airport_helipad_heliport_oilrig[] = {1, 1};
	static public final AirportFTAbuildup _airport_fta_heliport_oilrig[] = {
			new AirportFTAbuildup(0,HELIPAD1,HELIPAD1_block,1),
			new AirportFTAbuildup(1,HELITAKEOFF,NOTHING_block,0), // takeoff
			new AirportFTAbuildup(2,255,AIRPORT_BUSY_block,0), new AirportFTAbuildup(2,HELILANDING,0,3), new AirportFTAbuildup(2,HELITAKEOFF,0,1),
			new AirportFTAbuildup(3,HELILANDING,AIRPORT_BUSY_block,4),
			new AirportFTAbuildup(4,HELIENDLANDING,AIRPORT_BUSY_block,4), new AirportFTAbuildup(4,HELIPAD1,HELIPAD1_block,0), new AirportFTAbuildup(4,HELITAKEOFF,0,2),
			// In Air
			new AirportFTAbuildup(5,0,NOTHING_block,6),
			new AirportFTAbuildup(6,0,NOTHING_block,7),
			new AirportFTAbuildup(7,0,NOTHING_block,8),
			new AirportFTAbuildup(8,FLYING,NOTHING_block,5), new AirportFTAbuildup(8,HELILANDING,HELIPAD1_block,2), // landing
			new AirportFTAbuildup(MAX_ELEMENTS,0,0,0) // end marker. DO NOT REMOVE
	};

	static public final AirportMovingData [][] _airport_moving_datas = {
			AirportMovingData._airport_moving_data_country,				// Country Airfield (small) 4x3
			AirportMovingData._airport_moving_data_town,					// City Airport (large) 6x6
			AirportMovingData._airport_moving_data_heliport,			// Heliport
			AirportMovingData._airport_moving_data_metropolitan,	// Metropolitain Airport (large) - 2 runways
			AirportMovingData._airport_moving_data_international,	// International Airport (xlarge) - 2 runways
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			AirportMovingData._airport_moving_data_oilrig					// Oilrig
	};


}
