package com.dzavalishin.ifaces;

import java.util.function.BiConsumer;

import com.dzavalishin.game.Town;

@FunctionalInterface
public
interface TownActionProc extends BiConsumer<Town,Integer> {}