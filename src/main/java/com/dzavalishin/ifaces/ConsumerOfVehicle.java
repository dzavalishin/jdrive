package com.dzavalishin.ifaces;

import java.util.function.Consumer;

import com.dzavalishin.game.Vehicle;

@FunctionalInterface
public interface ConsumerOfVehicle extends Consumer<Vehicle> {}
