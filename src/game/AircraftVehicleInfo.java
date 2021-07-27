package game;

public class AircraftVehicleInfo {
	byte image_index;
	byte base_cost;
	byte running_cost;
	byte subtype;
	byte sfx;
	byte acceleration;
	byte max_speed;
	byte mail_capacity;
	int passenger_capacity;

	public AircraftVehicleInfo(
			int i, int j, int k, 
			int l, int snd, int m, 
			int n, int o, int p) {

		image_index = (byte) i;
		base_cost = (byte) j;
		running_cost = (byte) k;
		subtype = (byte) l;
		sfx = (byte) snd;
		acceleration = (byte) m;
		max_speed = (byte) n;
		mail_capacity = (byte) o;
		passenger_capacity = p;

	}
}
