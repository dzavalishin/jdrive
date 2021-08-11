package game;

public class RoadVehicleInfo {
	int image_index;
	int base_cost;
	int running_cost;
	int sfx;
	int max_speed;
	int capacity;
	int cargo_type;

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
