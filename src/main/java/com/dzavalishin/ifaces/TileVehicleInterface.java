package com.dzavalishin.ifaces;

import java.io.Serializable;

import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Vehicle;

@FunctionalInterface
public interface TileVehicleInterface extends Serializable 
{
	int apply(Vehicle v, TileIndex tile, int x, int y);
}