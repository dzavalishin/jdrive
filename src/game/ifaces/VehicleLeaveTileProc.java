package game.ifaces;

import game.TileIndex;
import game.Vehicle;

//typedef void VehicleLeaveTileProc(Vehicle *v, TileIndex tile, int x, int y);

@FunctionalInterface
public interface VehicleLeaveTileProc
{
	void accept (Vehicle v, TileIndex tile, int x, int y);
}
