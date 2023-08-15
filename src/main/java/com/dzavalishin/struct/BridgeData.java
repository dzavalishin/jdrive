package com.dzavalishin.struct;

import com.dzavalishin.game.Bridge;
import com.dzavalishin.game.TileIndex;

public class BridgeData 
{
	public int count;
	public TileIndex start_tile;
	public TileIndex end_tile;
	public int type;
	public final int [] indexes = new int[Bridge.MAX_BRIDGES];
	public final int  [] costs  = new int[Bridge.MAX_BRIDGES];
}
