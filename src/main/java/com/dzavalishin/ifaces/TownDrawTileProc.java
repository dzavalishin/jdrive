package com.dzavalishin.ifaces;

import java.util.function.Consumer;

import com.dzavalishin.game.TileInfo;

@FunctionalInterface
public interface TownDrawTileProc extends Consumer<TileInfo> {}