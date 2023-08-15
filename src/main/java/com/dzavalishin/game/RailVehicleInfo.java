package com.dzavalishin.game;

public class RailVehicleInfo 
{
	// TODO GRFFile modifies us so final modifier is off. Redo!
	
	public int image_index;
	private int flags; /* 1=multihead engine, 2=wagon */
	public int base_cost;
	private int max_speed;
	public int power;
	public int weight;
	public int running_cost_base;
	public int engclass; // 0: steam, 1: diesel, 2: electric
	public int capacity;
	public int cargo_type;
	public int callbackmask; // see CallbackMask enum
	public int pow_wag_power;
	public int pow_wag_weight;
	
	public int visual_effect; 
	// NOTE: this is not 100% implemented yet, at the
	// moment it is only used as a 'fallback' value
	// for when the 'powered wagon' callback fails. 
	// But it should really also determine what
	// kind of visual effect to generate for a vehicle 
	// (default, steam, diesel, electric).
	//  Same goes for the callback result, which 
	// atm is only used to check if a wagon is powered.
	
	public int shorten_factor;	// length on main map for this type is 8 - shorten_factor

	public RailVehicleInfo(
			int i, int j, int k, int ms, int m, 
			int n, int o, int p, int q, int r, 
			int s, int t, int u, int v, int w) 
	{
		 image_index =  i;
		 flags =  j; 
		 base_cost =  k;
		 max_speed = ms;
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

	public void setWagon(boolean b) {
		if(b)
			flags |= Engine.RVI_WAGON;
		else
			flags &= ~Engine.RVI_WAGON;		
	}

	public void setMultihead(boolean b) {
		if(b)
			flags |= Engine.RVI_MULTIHEAD;
		else
			flags &= ~Engine.RVI_MULTIHEAD;
	}


	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public int getWeight() {		return weight;	}
	public void setWeight(int weight) {		this.weight = weight;	}

	public int getEngclass() {		return engclass;	}
	public void setEngclass(int engclass) {		this.engclass = engclass;	}

	public int getCapacity() {		return capacity;	}
	public void setCapacity(int capacity) {		this.capacity = capacity;	}

	public int getCallbackmask() {		return callbackmask;	}
	public void setCallbackmask(int callbackmask) {		this.callbackmask = callbackmask;	}

	public int getMax_speed() {		return max_speed;	}
	public void setMax_speed(int speed) { 
		max_speed = speed; 
		}
	
}
