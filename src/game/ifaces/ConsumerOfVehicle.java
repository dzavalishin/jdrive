package game.ifaces;

import java.util.function.Consumer;

import game.Vehicle;

@FunctionalInterface
public interface ConsumerOfVehicle extends Consumer<Vehicle> {}
