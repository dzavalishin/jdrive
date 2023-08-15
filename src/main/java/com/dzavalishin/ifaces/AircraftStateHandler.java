package com.dzavalishin.ifaces;

import java.util.function.BiConsumer;

import com.dzavalishin.game.Airport;
import com.dzavalishin.game.Vehicle;

//typedef void AircraftStateHandler(Vehicle v, final Airport Airport);

@FunctionalInterface
public interface AircraftStateHandler extends BiConsumer<Vehicle, Airport> {}

