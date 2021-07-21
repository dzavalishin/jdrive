package game;

public class VehicleAir extends VehicleChild {

	int crashed_counter;
	byte pos;
	byte previous_pos;
	int targetairport;
	byte state;
	int desired_speed;	
	// Speed aircraft desires to maintain, used to
	// decrease traffic to busy airports.

}
