package com.dzavalishin.ifaces;

import com.dzavalishin.game.TileIndex;

@FunctionalInterface
public interface CommandCallback {

	void accept(boolean success, TileIndex tile, int p1, int p2);

}
