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

}
