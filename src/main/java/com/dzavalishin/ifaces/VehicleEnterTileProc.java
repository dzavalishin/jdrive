package com.dzavalishin.ifaces;

import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Vehicle;

//typedef uint32 VehicleEnterTileProc(Vehicle *v, TileIndex tile, int x, int y);

@FunctionalInterface
public interface VehicleEnterTileProc
{
	int accept (Vehicle v, TileIndex tile, int x, int y);
}
