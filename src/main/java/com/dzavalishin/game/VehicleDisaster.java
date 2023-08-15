package com.dzavalishin.game;

public class VehicleDisaster extends VehicleChild {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int image_override;
	int unk2;

	@Override
	void clear() {
		image_override = 0;
		unk2 = 0;
	}

}
