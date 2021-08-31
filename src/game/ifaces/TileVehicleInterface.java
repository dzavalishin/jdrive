package game.ifaces;

import java.io.Serializable;

import game.TileIndex;
import game.Vehicle;

@FunctionalInterface
public interface TileVehicleInterface extends Serializable 
{
	int apply(Vehicle v, TileIndex tile, int x, int y);
}