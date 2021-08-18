package game.ifaces;

import java.util.function.Consumer;

import game.TileInfo;

@FunctionalInterface
public interface TownDrawTileProc extends Consumer<TileInfo> {}