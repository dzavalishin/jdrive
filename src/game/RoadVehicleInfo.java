package game;

public class RoadVehicleInfo {
	public final int image_index;
	public final int base_cost;
	public final int running_cost;
	public final int sfx;
	public final int max_speed;
	public final int capacity;
	public final int cargo_type;

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
