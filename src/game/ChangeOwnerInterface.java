package game;

@FunctionalInterface
public interface ChangeOwnerInterface {
	void apply(TileIndex tile, PlayerID old_player, PlayerID new_player);
}