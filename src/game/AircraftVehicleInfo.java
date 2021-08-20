package game;

public class AircraftVehicleInfo {
	public final int image_index;
	public final int base_cost;
	public final int running_cost;
	public final int subtype;
	public final int sfx;
	public final int acceleration;
	public final int max_speed;
	public final int mail_capacity;
	public final int passenger_capacity;

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
