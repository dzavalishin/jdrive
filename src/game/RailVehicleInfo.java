package game;

public class RailVehicleInfo {

	int image_index;
	int flags; /* 1=multihead engine, 2=wagon */
	int base_cost;
	int max_speed;
	int power;
	int weight;
	int running_cost_base;
	int engclass; // 0: steam, 1: diesel, 2: electric
	int capacity;
	int cargo_type;
	int callbackmask; // see CallbackMask enum
	int pow_wag_power;
	int pow_wag_weight;
	
	int visual_effect; 
	// NOTE: this is not 100% implemented yet, at the
	// moment it is only used as a 'fallback' value
	// for when the 'powered wagon' callback fails. 
	// But it should really also determine what
	// kind of visual effect to generate for a vehicle 
	// (default, steam, diesel, electric).
	//  Same goes for the callback result, which 
	// atm is only used to check if a wagon is powered.
	
	int shorten_factor;	// length on main map for this type is 8 - shorten_factor

	public RailVehicleInfo(
			int i, int j, int k, int l, int m, 
			int n, int o, int p, int q, int r, 
			int s, int t, int u, int v, int w) 
	{
		 image_index =  i;
		 flags =  j; 
		 base_cost =  k;
		 max_speed = l;
		 power = m;
		 weight = n;
		 running_cost_base =  o;
		 engclass =  p;
		 capacity =  q;
		 cargo_type =  r;
		 callbackmask =  s;
		 pow_wag_power = t;
		 pow_wag_weight =  u;		
		 visual_effect =  v; 	
		 shorten_factor =  w;
	}
	
}
