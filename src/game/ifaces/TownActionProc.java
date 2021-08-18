package game.ifaces;

import java.util.function.BiConsumer;

import game.Town;

@FunctionalInterface
public
interface TownActionProc extends BiConsumer<Town,Integer> {}