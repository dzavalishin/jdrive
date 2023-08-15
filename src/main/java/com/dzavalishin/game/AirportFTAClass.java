package com.dzavalishin.game;

//public class Airport extends Airport // bring in constants
//{



	
	
	
	
	
	
	








	/*
	private void Airport_Destructor()
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
	}*/




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

static void AirportPrintOut(final Airport *Airport, final bool full_report)
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










//}

