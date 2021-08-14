package game;

public class ShipVehicleInfo {
	int image_index;
	int base_cost;
	int max_speed;
	int cargo_type;
	int capacity;
	int running_cost;
	int sfx;
	int refittable;

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
