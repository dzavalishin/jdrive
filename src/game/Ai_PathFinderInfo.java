package game;

public class Ai_PathFinderInfo {

	TileIndex start_tile_tl; // tl = top-left
	TileIndex start_tile_br; // br = bottom-right
	TileIndex end_tile_tl; // tl = top-left
	TileIndex end_tile_br; // br = bottom-right
	byte start_direction; // 0 to 3 or AI_PATHFINDER_NO_DIRECTION
	byte end_direction; // 0 to 3 or AI_PATHFINDER_NO_DIRECTION

	TileIndex route[];
	byte route_extra[]; // Some extra information about the route like bridge/tunnel
	
	int route_length;
	int position; // Current position in the build-path, needed to build the path

	boolean rail_or_road; // true = rail, false = road
	
	
	public Ai_PathFinderInfo() {
		route = new TileIndex[500];
		route_extra = new byte[500];
	}
}
