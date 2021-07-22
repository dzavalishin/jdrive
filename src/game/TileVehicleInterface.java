package game;

@FunctionalInterface
public interface TileVehicleInterface {
	int apply(Vehicle v, TileIndex tile, int x, int y);
}