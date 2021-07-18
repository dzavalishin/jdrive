package game;

public class BridgeData {
	int count;
	TileIndex start_tile;
	TileIndex end_tile;
	byte type;
	byte [] indexes = new byte[Bridge.MAX_BRIDGES];
	int  [] costs  = new int[Bridge.MAX_BRIDGES];

}
