package com.dzavalishin.ifaces;

import com.dzavalishin.game.TileIndex;
import com.dzavalishin.ids.PlayerID;

@FunctionalInterface
public interface ChangeOwnerInterface {
	void apply(TileIndex tile, PlayerID old_player, PlayerID new_player);
}