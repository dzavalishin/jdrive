package game;

public class ShipVehicleInfo {
	byte image_index;
	byte base_cost;
	int max_speed;
	byte cargo_type;
	int capacity;
	byte running_cost;
	byte sfx;
	byte refittable;

	public ShipVehicleInfo(
			int i, int j, int k, 
			int l, int m, int n, int o, int p) 
	{
		image_index = (byte) i;
		base_cost = (byte) j;
		max_speed = k;
		cargo_type = (byte) l;
		capacity = m;
		running_cost = (byte) n;
		sfx = (byte) o;
		refittable = (byte) p;
	}

}
