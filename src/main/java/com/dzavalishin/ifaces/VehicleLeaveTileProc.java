package com.dzavalishin.ifaces;

import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Vehicle;

//typedef void VehicleLeaveTileProc(Vehicle *v, TileIndex tile, int x, int y);

@FunctionalInterface
public interface VehicleLeaveTileProc
{
	void accept (Vehicle v, TileIndex tile, int x, int y);
}
