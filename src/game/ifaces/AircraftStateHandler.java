package game.ifaces;

import java.util.function.BiConsumer;

import game.AirportFTAClass;
import game.Vehicle;

//typedef void AircraftStateHandler(Vehicle v, final AirportFTAClass Airport);

@FunctionalInterface
public interface AircraftStateHandler extends BiConsumer<Vehicle, AirportFTAClass> {}

