package game.ifaces;

import game.TileIndex;
import game.Vehicle;

//typedef uint32 VehicleEnterTileProc(Vehicle *v, TileIndex tile, int x, int y);

@FunctionalInterface
public interface VehicleEnterTileProc
{
	int accept (Vehicle v, TileIndex tile, int x, int y);
}
