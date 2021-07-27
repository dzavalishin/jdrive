package game;

public class RailVehicleInfo {

	byte image_index;
	byte flags; /* 1=multihead engine, 2=wagon */
	byte base_cost;
	int max_speed;
	int power;
	int weight;
	byte running_cost_base;
	byte engclass; // 0: steam, 1: diesel, 2: electric
	byte capacity;
	byte cargo_type;
	byte callbackmask; // see CallbackMask enum
	int pow_wag_power;
	byte pow_wag_weight;
	
	byte visual_effect; 
	// NOTE: this is not 100% implemented yet, at the
	// moment it is only used as a 'fallback' value
	// for when the 'powered wagon' callback fails. 
	// But it should really also determine what
	// kind of visual effect to generate for a vehicle 
	// (default, steam, diesel, electric).
	//  Same goes for the callback result, which 
	// atm is only used to check if a wagon is powered.
	
	byte shorten_factor;	// length on main map for this type is 8 - shorten_factor

	public RailVehicleInfo(
			int i, int j, int k, int l, int m, 
			int n, int o, int p, int q, int r, 
			int s, int t, int u, int v, int w) 
	{
		 image_index = (byte) i;
		 flags = (byte) j; 
		 base_cost = (byte) k;
		 max_speed = l;
		 power = m;
		 weight = n;
		 running_cost_base = (byte) o;
		 engclass = (byte) p;
		 capacity = (byte) q;
		 cargo_type = (byte) r;
		 callbackmask = (byte) s;
		 pow_wag_power = t;
		 pow_wag_weight = (byte) u;		
		 visual_effect = (byte) v; 	
		 shorten_factor = (byte) w;
	}
	
}
