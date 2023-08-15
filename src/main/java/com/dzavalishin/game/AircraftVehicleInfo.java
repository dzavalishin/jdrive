package com.dzavalishin.game;

public class AircraftVehicleInfo {
	public int image_index;
	public int base_cost;
	public int running_cost;
	public int subtype;
	public int sfx;
	public int acceleration;
	public int max_speed;
	public int mail_capacity;
	public int passenger_capacity;

	public AircraftVehicleInfo(
			int i, int j, int k, 
			int l, int snd, int m, 
			int n, int o, int p) {

		image_index =  i;
		base_cost =  j;
		running_cost =  k;
		subtype =  l;
		sfx =  snd;
		acceleration =  m;
		max_speed =  n;
		mail_capacity =  o;
		passenger_capacity = p;

	}

}
