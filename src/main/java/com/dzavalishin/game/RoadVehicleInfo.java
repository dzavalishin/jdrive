package com.dzavalishin.game;

public class RoadVehicleInfo 
{
	// TODO GRFFile modifies us so final modifier is off. Redo!

	public int image_index;
	public int base_cost;
	public int running_cost;
	public int sfx;
	public int max_speed;
	public int capacity;
	public int cargo_type;

	public RoadVehicleInfo(
			int i, int j, int k, 
			int snd, int l, int m, 
			int ct) 
	{
		 image_index = i;
		 base_cost = j;
		 running_cost = k;
		 sfx = snd;
		 max_speed = l;
		 capacity = m;
		 cargo_type = ct;		
	}
	
}
