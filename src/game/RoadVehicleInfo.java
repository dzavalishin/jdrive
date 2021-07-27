package game;

public class RoadVehicleInfo {
	byte image_index;
	byte base_cost;
	byte running_cost;
	byte sfx;
	byte max_speed;
	byte capacity;
	byte cargo_type;

	public RoadVehicleInfo(
			int i, int j, int k, 
			int snd, int l, int m, 
			int ct) 
	{

		 image_index = (byte) i;
		 base_cost = (byte) j;
		 running_cost = (byte) k;
		 sfx = (byte) snd;
		 max_speed = (byte) l;
		 capacity = (byte) m;
		 cargo_type = (byte) ct;
		
	}
	
}
