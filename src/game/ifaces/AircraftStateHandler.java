package game.ifaces;

import java.util.function.BiConsumer;

import game.Airport;
import game.Vehicle;

//typedef void AircraftStateHandler(Vehicle v, final Airport Airport);

@FunctionalInterface
public interface AircraftStateHandler extends BiConsumer<Vehicle, Airport> {}

