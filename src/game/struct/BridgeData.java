package game.struct;

import game.Bridge;
import game.TileIndex;

public class BridgeData 
{
	public int count;
	public TileIndex start_tile;
	public TileIndex end_tile;
	public int type;
	public byte [] indexes = new byte[Bridge.MAX_BRIDGES];
	public int  [] costs  = new int[Bridge.MAX_BRIDGES];
}
