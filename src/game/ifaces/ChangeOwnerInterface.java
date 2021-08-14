package game.ifaces;

import game.TileIndex;
import game.ids.PlayerID;

@FunctionalInterface
public interface ChangeOwnerInterface {
	void apply(TileIndex tile, PlayerID old_player, PlayerID new_player);
}