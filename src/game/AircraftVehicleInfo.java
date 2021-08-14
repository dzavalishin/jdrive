package game;

public class AircraftVehicleInfo {
	int image_index;
	int base_cost;
	int running_cost;
	int subtype;
	int sfx;
	int acceleration;
	int max_speed;
	int mail_capacity;
	int passenger_capacity;

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
