package com.dzavalishin.game;

public class VehicleShip extends VehicleChild {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int IN_DEPOT = 0x80;
	/**
	 * 0x80 = depot?
	 */
	
	int state;

	public VehicleShip() {
		clear();
	}
	
	@Override
	void clear() {
		state = 0;		
	}
	
	public void setInDepot(boolean b) {
		if(b) state |= IN_DEPOT;
		else state &= ~IN_DEPOT;
	}

	public boolean isInDepot() {
		//return 0 != (state & IN_DEPOT);
		return state == IN_DEPOT;
	}

	public void forceInDepot() {
		state = IN_DEPOT;		
	}

}
