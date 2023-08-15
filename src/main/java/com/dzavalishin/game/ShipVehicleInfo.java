package com.dzavalishin.game;

public class ShipVehicleInfo 
{
	// TODO GRFFile modifies us so final is removed, redo!
	
	public int image_index;
	public int base_cost;
	public int max_speed;
	public int cargo_type;
	public int capacity;
	public int running_cost;
	public int sfx;
	public int refittable;

	public ShipVehicleInfo(
			int i, int j, int k, 
			int l, int m, int n, int o, int p) 
	{
		image_index =  i;
		base_cost =  j;
		max_speed = k;
		cargo_type =  l;
		capacity = m;
		running_cost =  n;
		sfx =  o;
		refittable =  p;
	}

}
