package com.dzavalishin.game;

public class VehicleAir extends VehicleChild 
{
	private static final long serialVersionUID = 1L;
	
	int crashed_counter;
	int pos;
	int previous_pos;
	int targetairport;
	int state;
	int desired_speed;	
	// Speed aircraft desires to maintain, used to
	// decrease traffic to busy airports.

	@Override
	void clear() 
	{
		crashed_counter = pos = previous_pos =
		targetairport = state = desired_speed = 0;	
	}
}
