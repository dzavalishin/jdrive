package com.dzavalishin.game;


import com.dzavalishin.struct.TileIndexDiffC;
import com.dzavalishin.tables.AirConstants;
import com.dzavalishin.tables.AirCraftTables;
import com.dzavalishin.tables.AirportFTAbuildup;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.xui.Window;

public class Airport extends AirConstants 
{
	private int nofelements;				// number of positions the airport consists of
	final byte terminals[];
	final byte helipads[];
	private int entry_point;				// when an airplane arrives at this airport, enter it at position entry_point
	private int acc_planes;					// accept airplanes or helicopters or both
	final TileIndexDiffC[] airport_depots;	// gives the position of the depots on the airports
	private AirportFTA layout[];			// state machine for airport

	


	public static final int MAX_ELEMENTS = 255;
	public static final int MAX_HEADINGS = 18;
	
	
	public static final int MAX_TERMINALS = 6;
	public static final int MAX_HELIPADS  = 2;

	
	private Airport(
			final byte terminals[], final byte helipads[],
			final int entry_point, final int acc_planes,
			final AirportFTAbuildup FA[],
			final TileIndexDiffC depots[] )
	{
		int nofterminals, nofhelipads;
		int nofterminalgroups = 0;
		int nofhelipadgroups = 0;
		int icurr;
		int i;
		nofterminals = nofhelipads = 0;

		// now we read the number of terminals we have
		if (terminals != null) {
			i = terminals[0];
			nofterminalgroups = i;
			icurr = 0;
			while (i-- > 0) {
				icurr++;
				assert(terminals[icurr] != 0);	//we don't want to have an empty group
				nofterminals += terminals[icurr];
			}

		}
		this.terminals = terminals;

		// read helipads
		if (helipads != null) {
			i = helipads[0];
			nofhelipadgroups = i;
			icurr = 0;
			while (i-- > 0) {
				icurr++;
				assert(helipads[icurr] != 0); //no empty groups please
				nofhelipads += helipads[icurr];
			}

		}
		this.helipads = helipads;

		// if there are more terminals than 6, internal variables have to be changed, so don't allow that
		// same goes for helipads
		if (nofterminals > MAX_TERMINALS) { Global.error("Currently only maximum of %2d terminals are supported (you wanted %2d)\n", MAX_TERMINALS, nofterminals);}
		if (nofhelipads > MAX_HELIPADS) { Global.error("Currently only maximum of %2d helipads are supported (you wanted %2d)\n", MAX_HELIPADS, nofhelipads);}
		// terminals/helipads are divided into groups. Groups are computed by dividing the number
		// of terminals by the number of groups. Half in half. If #terminals is uneven, first group
		// will get the less # of terminals

		assert(nofterminals <= MAX_TERMINALS);
		assert(nofhelipads <= MAX_HELIPADS);

		this.nofelements = AirportGetNofElements(FA);
		// check
		if (entry_point >= this.nofelements) {Global.error("Entry point (%2d) must be within the airport positions (which is max %2d)\n", entry_point, this.nofelements);}
		assert(entry_point < this.nofelements);

		this.acc_planes = acc_planes;
		this.entry_point = entry_point;
		this.airport_depots = depots;
		//this.nof_depots = nof_depots;


		// build the state machine
		AirportBuildAutomata(FA);
		Global.DEBUG_misc( 1, "#Elements %2d; #Terminals %2d in %d group(s); #Helipads %2d in %d group(s); Entry Point %d",
				this.nofelements, nofterminals, nofterminalgroups, nofhelipads, nofhelipadgroups, this.entry_point
				);


		{
			int ret = AirportTestFTA();
			if (ret != MAX_ELEMENTS) Global.error("ERROR with element: %d\n", ret - 1);
			assert(ret == MAX_ELEMENTS);
		}
		// print out full information
		// true  -- full info including heading, block, etc
		// false -- short info, only position and next position
		//AirportPrintOut(Airport, false);
	}
	
	
	public int nof_depots() {
		return airport_depots.length;
	}


	
	
	
	public int getNofElements() {
		return nofelements;
	}


	public int getEntryPoint() {
		return entry_point;
	}


	public int getAccPlanes() {
		return acc_planes;
	}


	private void AirportBuildAutomata(final AirportFTAbuildup[] FA)
	{
		//AirportFTA []FAutomata;
		//FAutomata = new AirportFTA[nofelements];
		layout = new AirportFTA[nofelements];//FAutomata;
		int internalcounter = 0;

		for(int i = 0; i < nofelements; i++) {
			AirportFTA current = new AirportFTA();
			//current = Airport.layout[i];
			layout[i] = current;
			current.position = FA[internalcounter].position;
			current.heading  = FA[internalcounter].heading;
			current.block    = FA[internalcounter].block;
			current.next_position = FA[internalcounter].next_in_chain;

			// outgoing nodes from the same position, create linked list
			while (current.position == FA[internalcounter + 1].position) {
				AirportFTA newNode = new AirportFTA();

				newNode.position = FA[internalcounter + 1].position;
				newNode.heading  = FA[internalcounter + 1].heading;
				newNode.block    = FA[internalcounter + 1].block;
				newNode.next_position = FA[internalcounter + 1].next_in_chain;
				// create link
				current.next_in_chain = newNode;
				current = current.next_in_chain;
				internalcounter++;
			} // while
			current.next_in_chain = null;
			internalcounter++;
		}
	}
	
	
	
	private int AirportTestFTA()
	{
		int position, i, next_element;
		AirportFTA temp;
		next_element = 0;

		for (i = 0; i < nofelements; i++) {
			position = layout[i].position;
			if (position != next_element) return i;
			temp = layout[i];

			do {
				if (temp.heading > MAX_HEADINGS && temp.heading != 255) return i;
				if (temp.heading == 0 && temp.next_in_chain != null) return i;
				if (position != temp.position) return i;
				if (temp.next_position >= nofelements) return i;
				temp = temp.next_in_chain;
			} while (temp != null);
			next_element++;
		}
		return MAX_ELEMENTS;
	}
	

	public int GetNumTerminals()
	{
		int num = 0;

		for (int i = terminals[0]; i > 0; i--) num += terminals[i];

		return num;
	}

	public int GetNumHelipads()
	{
		int num = 0;

		for (int i = helipads[0]; i > 0; i--) num += helipads[i];

		return num;
	}
	
	public AirportFTA getLayoutItem(int pos)
	{
		return layout[pos];
	}
	
	// -------------------------------------------------------------------
	// Static stuff
	// -------------------------------------------------------------------
	
	
	


	static int AirportGetNofElements(final AirportFTAbuildup [] FA)
	{
		int i;
		int nofelements = 0;
		int temp = FA[0].position;

		for (i = 0; i < MAX_ELEMENTS; i++) {
			if (temp != FA[i].position) {
				nofelements++;
				temp = FA[i].position;
			}
			if (FA[i].position == MAX_ELEMENTS) break;
		}
		return nofelements;
	}


	
	private static Airport CountryAirport;
	private static Airport CityAirport;
	private static Airport Oilrig;
	private static Airport Heliport;
	private static Airport MetropolitanAirport;
	private static Airport InternationalAirport;

	
	static void InitializeAirports()
	{
		// country airport
		CountryAirport = new Airport(
				//CountryAirport,
				AirCraftTables._airport_terminal_country,
				null,
				16,
				ALL,
				AirCraftTables._airport_fta_country,
				AirCraftTables._airport_depots_country
				//_airport_depots_country.length
				);

		// city airport
		CityAirport = new Airport(
				//CityAirport,
				AirCraftTables._airport_terminal_city,
				null,
				19,
				ALL,
				AirCraftTables._airport_fta_city,
				AirCraftTables._airport_depots_city
				//lengthof(_airport_depots_city)
				);

		// metropolitan airport
		MetropolitanAirport = new Airport(
				//MetropolitanAirport,
				AirCraftTables._airport_terminal_metropolitan,
				null,
				20,
				ALL,
				AirCraftTables._airport_fta_metropolitan,
				AirCraftTables._airport_depots_metropolitan
				//lengthof(_airport_depots_metropolitan)
				);

		// international airport
		InternationalAirport = new Airport(
				//InternationalAirport,
				AirCraftTables._airport_terminal_international,
				AirCraftTables._airport_helipad_international,
				37,
				ALL,
				AirCraftTables._airport_fta_international,
				AirCraftTables._airport_depots_international
				//lengthof(_airport_depots_international)
				);

		// heliport, oilrig
		Heliport = new Airport(
				//Heliport,
				null,
				AirCraftTables._airport_helipad_heliport_oilrig,
				7,
				HELICOPTERS_ONLY,
				AirCraftTables._airport_fta_heliport_oilrig,
				null
				//0
				);

		Oilrig = Heliport;  // exactly the same structure for heliport/oilrig, so share state machine
	}

	static Airport GetAirport(final int airport_type)
	{
		// -- AircraftNextAirportPos_and_Order . Needs something nicer, don't like this code
		// needs constant change if more airports are added
		switch (airport_type) {
		case AT_SMALL: return CountryAirport; 
		case AT_LARGE: return CityAirport; 
		case AT_METROPOLITAN: return MetropolitanAirport; 
		case AT_HELIPORT: return Heliport; 
		case AT_OILRIG: return Oilrig; 
		case AT_INTERNATIONAL: return InternationalAirport; 
		default:
			assert(airport_type <= AT_INTERNATIONAL);
		}
		return null;
	}


	// Available aircraft types
	private static int _avail_aircraft = 0;
	
	/** Get buildable airport bitmask.
	 * @return get all buildable airports at this given time, bitmasked.
	 * Bit 0 means the small airport is buildable, etc.
	 *
	 * TODO set availability of airports by year, instead of airplane
	 */
	public static int GetValidAirports()	{
		int bytemask = _avail_aircraft; /// sets the first 3 bytes, 0 - 2, @see AdjustAvailAircraft()

		// 1980-1-1 is -. 21915
		// 1990-1-1 is -. 25568
		if (Global.get_date() >= 21915) bytemask = BitOps.RETSETBIT(bytemask, 3); // metropilitan airport 1980
		if (Global.get_date() >= 25568) bytemask = BitOps.RETSETBIT(bytemask, 4); // international airport 1990
		return bytemask;
	}

	static void AdjustAvailAircraft()
	{
		int date = Global.get_date();
		byte avail = 0;
		if (date >= 12784) avail |= 2; // big airport
		if (date < 14610 || Global._patches.always_small_airport) avail |= 1;  // small airport
		if (date >= 15706) avail |= 4; // enable heliport
	
		if (avail != _avail_aircraft) {
			_avail_aircraft = avail;
			Window.InvalidateWindow(Window.WC_BUILD_STATION, 0);
		}
	}
	
}



//Finite sTate mAchine --> FTA
class AirportFTA {
	int position;				// the position that an airplane is at
	int next_position;			// next position from this position
	int block;					// 32 bit blocks (st.airport_flags), should be enough for the most complex airports
	int heading;				// heading (current orders), guiding an airplane to its target on an airport
	AirportFTA next_in_chain;	// possible extra movement choices from this position
}
