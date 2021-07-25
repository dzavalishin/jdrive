package game;

public class VehicleRoad extends VehicleChild {
	
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
		RoadStop slot = null;
		
		 state = frame = unk2 = overtaking =
		 overtaking_ctr = crashed_ctr = reverse_ctr =
		 slotindex = slot_age = 0;
	}

	public VehicleRoad() {
		clear();
	}
	
}
