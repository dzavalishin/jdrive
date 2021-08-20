package game;

public class ShipVehicleInfo {
	public final int image_index;
	public final int base_cost;
	public final int max_speed;
	public final int cargo_type;
	public final int capacity;
	public final int running_cost;
	public final int sfx;
	public final int refittable;

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
