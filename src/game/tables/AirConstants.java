package game.tables;

public class AirConstants 
{

	// Airport types
	public static final int AT_SMALL = 0;
	public static final int AT_LARGE = 1;
	public static final int AT_HELIPORT = 2;
	public static final int AT_METROPOLITAN = 3;
	public static final int AT_INTERNATIONAL = 4;
	public static final int AT_OILRIG = 15;

	// do not change unless you change v.subtype too. This aligns perfectly with its current setting

	public static final int AIRCRAFT_ONLY = 0;
	public static final int ALL = 1;
	public static final int HELICOPTERS_ONLY = 2;
	

	
	
	
	
	static final int TERM1_block			= 1 << 0;
	static final int TERM2_block    	= 1 << 1;
	static final int TERM3_block    	= 1 << 2;
	static final int TERM4_block    	= 1 << 3;
	static final int TERM5_block    	= 1 << 4;
	static final int TERM6_block    	= 1 << 5;
	static final int HELIPAD1_block						= 1 << 6;
	static final int HELIPAD2_block						= 1 << 7;
	static final int RUNWAY_IN_OUT_block				= 1 << 8;
	static final int RUNWAY_IN_block						= 1 << 8;
	static final int AIRPORT_BUSY_block				= 1 << 8;
	static final int RUNWAY_OUT_block					= 1 << 9;
	static final int TAXIWAY_BUSY_block				= 1 << 10;
	static final int OUT_WAY_block    = 1 << 11;
	static final int IN_WAY_block    = 1 << 12;
	static final int AIRPORT_ENTRANCE_block		= 1 << 13;
	static final int TERM_GROUP1_block					= 1 << 14;
	static final int TERM_GROUP2_block					= 1 << 15;
	static final int HANGAR2_AREA_block				= 1 << 16;
	static final int TERM_GROUP2_ENTER1_block	= 1 << 17;
	static final int TERM_GROUP2_ENTER2_block	= 1 << 18;
	static final int TERM_GROUP2_EXIT1_block		= 1 << 19;
	static final int TERM_GROUP2_EXIT2_block		= 1 << 20;
	static final int PRE_HELIPAD_block					= 1 << 21;
	static final int NOTHING_block    = 1 << 30;
	

///////////////////////////////////////////////////////////////////////
///////***********Movement States on Airports********************//////
//headings target
	
	
	static final int TO_ALL = 0;
	static final int HANGAR = 1;
	static final int TERM1 = 2;
	static final int TERM2 = 3;
	static final int TERM3 = 4;
	static final int TERM4 = 5;
	static final int TERM5 = 6;
	static final int TERM6 = 7;
	static final int HELIPAD1 = 8;
	static final int HELIPAD2 = 9;
	static final int TAKEOFF = 10;
	static final int STARTTAKEOFF = 11;
	static final int ENDTAKEOFF = 12;
	static final int HELITAKEOFF = 13;
	static final int FLYING = 14;
	static final int LANDING = 15;
	static final int ENDLANDING = 16;
	static final int HELILANDING = 17;
	static final int HELIENDLANDING = 18;
	
	public static final int MAX_ELEMENTS = 255;
	
	
}
