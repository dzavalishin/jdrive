package game;
import game.util.BitOps;

//Finite sTate mAchine -. FTA
public class AirportFTAClass extends Airport // bring in constants
{
	int nofelements;							// number of positions the airport consists of
	final byte terminals[];
	final byte helipads[];
	int entry_point;							// when an airplane arrives at this airport, enter it at position entry_point
	int acc_planes;							// accept airplanes or helicopters or both
	final TileIndexDiffC[] airport_depots;	// gives the position of the depots on the airports
	//int nof_depots;							// number of depots this airport has
	AirportFTA layout[];		// state machine for airport


	public static final int MAX_ELEMENTS = 255;
	public static final int MAX_HEADINGS = 18;
	
	
	public static final int MAX_TERMINALS = 6;
	public static final int MAX_HELIPADS  = 2;


	
	
	
	static AirportFTAClass CountryAirport;
	static AirportFTAClass CityAirport;
	static AirportFTAClass Oilrig;
	static AirportFTAClass Heliport;
	static AirportFTAClass MetropolitanAirport;
	static AirportFTAClass InternationalAirport;
	
	
	
	
	/** Get buildable airport bitmask.
	 * @return get all buildable airports at this given time, bitmasked.
	 * Bit 0 means the small airport is buildable, etc.
	 * @todo set availability of airports by year, instead of airplane
	 */
	int GetValidAirports()	{
		int bytemask = Global._avail_aircraft; /// sets the first 3 bytes, 0 - 2, @see AdjustAvailAircraft()

		// 1980-1-1 is -. 21915
		// 1990-1-1 is -. 25568
		if (Global._date >= 21915) bytemask = BitOps.RETSETBIT(bytemask, 3); // metropilitan airport 1980
		if (Global._date >= 25568) bytemask = BitOps.RETSETBIT(bytemask, 4); // international airport 1990
		return bytemask;
	}






	static void InitializeAirports()
	{
		// country airport
		CountryAirport = new AirportFTAClass(
				//CountryAirport,
				AirportFTAbuildup._airport_terminal_country,
				null,
				16,
				ALL,
				AirportFTAbuildup._airport_fta_country,
				AirportFTAbuildup._airport_depots_country
				//_airport_depots_country.length
				);

		// city airport
		CityAirport = new AirportFTAClass(
				//CityAirport,
				AirportFTAbuildup._airport_terminal_city,
				null,
				19,
				ALL,
				AirportFTAbuildup._airport_fta_city,
				AirportFTAbuildup._airport_depots_city
				//lengthof(_airport_depots_city)
				);

		// metropolitan airport
		MetropolitanAirport = new AirportFTAClass(
				//MetropolitanAirport,
				AirportFTAbuildup._airport_terminal_metropolitan,
				null,
				20,
				ALL,
				AirportFTAbuildup._airport_fta_metropolitan,
				AirportFTAbuildup._airport_depots_metropolitan
				//lengthof(_airport_depots_metropolitan)
				);

		// international airport
		InternationalAirport = new AirportFTAClass(
				//InternationalAirport,
				AirportFTAbuildup._airport_terminal_international,
				AirportFTAbuildup._airport_helipad_international,
				37,
				ALL,
				AirportFTAbuildup._airport_fta_international,
				AirportFTAbuildup._airport_depots_international
				//lengthof(_airport_depots_international)
				);

		// heliport, oilrig
		Heliport = new AirportFTAClass(
				//Heliport,
				null,
				AirportFTAbuildup._airport_helipad_heliport_oilrig,
				7,
				HELICOPTERS_ONLY,
				AirportFTAbuildup._airport_fta_heliport_oilrig,
				null
				//0
				);

		Oilrig = Heliport;  // exactly the same structure for heliport/oilrig, so share state machine
	}

	static void UnInitializeAirports()
	{
		CountryAirport.AirportFTAClass_Destructor();
		CityAirport.AirportFTAClass_Destructor();
		Heliport.AirportFTAClass_Destructor();
		MetropolitanAirport.AirportFTAClass_Destructor();
		InternationalAirport.AirportFTAClass_Destructor();
	}

	private AirportFTAClass(
			//AirportFTAClass Airport,
			final byte terminals[], final byte helipads[],
			final int entry_point, final int acc_planes,
			final AirportFTAbuildup FA[],
			final TileIndexDiffC depots[] ) //, final byte nof_depots)
	{
		int nofterminals, nofhelipads;
		int nofterminalgroups = 0;
		int nofhelipadgroups = 0;
		//final byte [] curr;
		int icurr;
		int i;
		nofterminals = nofhelipads = 0;

		//now we read the number of terminals we have
		if (terminals != null) {
			i = terminals[0];
			nofterminalgroups = i;
			//curr = terminals;
			icurr = 0;
			while (i-- > 0) {
				icurr++;
				assert(terminals[icurr] != 0);	//we don't want to have an empty group
				nofterminals += terminals[icurr];
			}

		}
		this.terminals = terminals;

		//read helipads
		if (helipads != null) {
			i = helipads[0];
			nofhelipadgroups = i;
			//curr = helipads;
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
		AirportBuildAutomata(this, FA);
		Global.DEBUG_misc( 1, "#Elements %2d; #Terminals %2d in %d group(s); #Helipads %2d in %d group(s); Entry Point %d",
				this.nofelements, nofterminals, nofterminalgroups, nofhelipads, nofhelipadgroups, this.entry_point
				);


		{
			int ret = AirportTestFTA(this);
			if (ret != MAX_ELEMENTS) Global.error("ERROR with element: %d\n", ret - 1);
			assert(ret == MAX_ELEMENTS);
		}
		// print out full information
		// true  -- full info including heading, block, etc
		// false -- short info, only position and next position
		//AirportPrintOut(Airport, false);
	}

	private void AirportFTAClass_Destructor()
	{
		int i;
		AirportFTA current, next;

		for (i = 0; i < this.nofelements; i++) {
			current = this.layout[i].next_in_chain;
			while (current != null) {
				next = current.next_in_chain;
				//free(current);
				current = next;
			};
		}
		//free(Airport.layout);
		//free(Airport);
	}

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

	static void AirportBuildAutomata(AirportFTAClass Airport, final AirportFTAbuildup[] FA)
	{
		AirportFTA []FAutomata;
		AirportFTA current;
		int internalcounter, i;
		FAutomata = new AirportFTA[ Airport.nofelements];
		Airport.layout = FAutomata;
		internalcounter = 0;

		for (i = 0; i < Airport.nofelements; i++) {
			current = Airport.layout[i];
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

	static int AirportTestFTA(final AirportFTAClass Airport)
	{
		int position, i, next_element;
		AirportFTA temp;
		next_element = 0;

		for (i = 0; i < Airport.nofelements; i++) {
			position = Airport.layout[i].position;
			if (position != next_element) return i;
			temp = Airport.layout[i];

			do {
				if (temp.heading > MAX_HEADINGS && temp.heading != 255) return i;
				if (temp.heading == 0 && temp.next_in_chain != null) return i;
				if (position != temp.position) return i;
				if (temp.next_position >= Airport.nofelements) return i;
				temp = temp.next_in_chain;
			} while (temp != null);
			next_element++;
		}
		return MAX_ELEMENTS;
	}

	/*
static final char* final _airport_heading_strings[] = {
	"TO_ALL",
	"HANGAR",
	"TERM1",
	"TERM2",
	"TERM3",
	"TERM4",
	"TERM5",
	"TERM6",
	"HELIPAD1",
	"HELIPAD2",
	"TAKEOFF",
	"STARTTAKEOFF",
	"ENDTAKEOFF",
	"HELITAKEOFF",
	"FLYING",
	"LANDING",
	"ENDLANDING",
	"HELILANDING",
	"HELIENDLANDING",
	"DUMMY"	// extra heading for 255
};

static void AirportPrintOut(final AirportFTAClass *Airport, final bool full_report)
{
	AirportFTA *temp;
	int i;
	byte heading;

	printf("(P = Current Position; NP = Next Position)\n");
	for (i = 0; i < Airport.nofelements; i++) {
		temp = &Airport.layout[i];
		if (full_report) {
			heading = (temp.heading == 255) ? MAX_HEADINGS+1 : temp.heading;
			printf("Pos:%2d NPos:%2d Heading:%15s Block:%2d\n", temp.position, temp.next_position,
						 _airport_heading_strings[heading], AirportBlockToString(temp.block));
		} else {
			printf("P:%2d NP:%2d", temp.position, temp.next_position);
		}
		while (temp.next_in_chain != null) {
			temp = temp.next_in_chain;
			if (full_report) {
				heading = (temp.heading == 255) ? MAX_HEADINGS+1 : temp.heading;
				printf("Pos:%2d NPos:%2d Heading:%15s Block:%2d\n", temp.position, temp.next_position,
							_airport_heading_strings[heading], AirportBlockToString(temp.block));
			} else {
				printf("P:%2d NP:%2d", temp.position, temp.next_position);
			}
		}
		printf("\n");
	}
}


static byte AirportBlockToString(int block)
{
	byte i = 0;
	if (block & 0xffff0000) { block >>= 16; i += 16; }
	if (block & 0x0000ff00) { block >>= 8;  i += 8; }
	if (block & 0x000000f0) { block >>= 4;  i += 4; }
	if (block & 0x0000000c) { block >>= 2;  i += 2; }
	if (block & 0x00000002) { i += 1; }
	return i;
}
	 */

	final AirportFTAClass GetAirport(final int airport_type)
	{
		AirportFTAClass Airport = null;
		//FIXME -- AircraftNextAirportPos_and_Order . Needs something nicer, don't like this code
		// needs constant change if more airports are added
		switch (airport_type) {
		case AT_SMALL: Airport = CountryAirport; break;
		case AT_LARGE: Airport = CityAirport; break;
		case AT_METROPOLITAN: Airport = MetropolitanAirport; break;
		case AT_HELIPORT: Airport = Heliport; break;
		case AT_OILRIG: Airport = Oilrig; break;
		case AT_INTERNATIONAL: Airport = InternationalAirport; break;
		default:
			/* #ifdef DEBUG__
			printf("Airport AircraftNextAirportPos_and_Order not yet implemented\n");
			#endif */
			assert(airport_type <= AT_INTERNATIONAL);
		}
		return Airport;
	}



}

//internal structure used in openttd - Finite sTate mAchine -. FTA
class AirportFTA {
	int position;										// the position that an airplane is at
	int next_position;								// next position from this position
	int block;	// 32 bit blocks (st.airport_flags), should be enough for the most complex airports
	int heading;	// heading (current orders), guiding an airplane to its target on an airport
	AirportFTA next_in_chain;	// possible extra movement choices from this position
}
