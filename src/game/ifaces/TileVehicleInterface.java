package game.ifaces;

import game.TileIndex;
import game.Vehicle;

@FunctionalInterface
public interface TileVehicleInterface {
	int apply(Vehicle v, TileIndex tile, int x, int y);
}