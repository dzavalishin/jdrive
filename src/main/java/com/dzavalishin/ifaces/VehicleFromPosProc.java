package com.dzavalishin.ifaces;

import com.dzavalishin.game.Vehicle;

@FunctionalInterface
public interface VehicleFromPosProc {

	/**
	 * Applied to Vehicle by VehicleFromPos
	 *
	 * @param t Vehicle to process
	 * @param u User object
	 * @return Whatever.
	 */
	Object apply(Vehicle v, Object o);
}
