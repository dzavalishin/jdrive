package com.dzavalishin.game;

public class VehicleRoad extends VehicleChild {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int state;
	int frame;
	int unk2;
	int overtaking;
	int overtaking_ctr;
	int crashed_ctr;
	int reverse_ctr;
	RoadStop slot;
	int slotindex;
	int slot_age;

	@Override
	void clear() {
		slot = null;

		state = frame = unk2 = overtaking =
				overtaking_ctr = crashed_ctr = reverse_ctr =
				slotindex = slot_age = 0;
	}

	public VehicleRoad() {
		clear();
	}

	public void setInDepot() {
		state = 254;		
	}

	public boolean isInDepot() {
		return state == 254;		
	}

	public void setInTunnel() {
		state = 255;		
	}

	public boolean isInTunnel() {
		return state == 255;		
	}
	
	
}
