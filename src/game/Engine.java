package game;

public class Engine {
	int intro_date;
	int age;
	int reliability;
	int reliability_spd_dec;
	int reliability_start, reliability_max, reliability_final;
	int duration_phase_1, duration_phase_2, duration_phase_3;
	byte lifelength;
	byte flags;
	byte preview_player;
	byte preview_wait;
	byte railtype;
	byte player_avail;
	
	// type, ie VEH_Road, VEH_Train, etc. Same as in vehicle.h
	byte type;				

}
