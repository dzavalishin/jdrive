package game;

public class RailVehicleInfo {

	public final int image_index;
	private int flags; /* 1=multihead engine, 2=wagon */
	public final int base_cost;
	public final int max_speed;
	public final int power;
	public final int weight;
	public final int running_cost_base;
	public final int engclass; // 0: steam, 1: diesel, 2: electric
	public final int capacity;
	public final int cargo_type;
	public final int callbackmask; // see CallbackMask enum
	public final int pow_wag_power;
	public final int pow_wag_weight;
	
	public final int visual_effect; 
	// NOTE: this is not 100% implemented yet, at the
	// moment it is only used as a 'fallback' value
	// for when the 'powered wagon' callback fails. 
	// But it should really also determine what
	// kind of visual effect to generate for a vehicle 
	// (default, steam, diesel, electric).
	//  Same goes for the callback result, which 
	// atm is only used to check if a wagon is powered.
	
	public final int shorten_factor;	// length on main map for this type is 8 - shorten_factor

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

	public boolean isMulttihead() { return  0 != (flags&Engine.RVI_MULTIHEAD);	}
	public boolean isWagon() { return 0!=(flags & Engine.RVI_WAGON); }
	
}
