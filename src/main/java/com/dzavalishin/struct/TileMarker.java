package com.dzavalishin.struct;

import com.dzavalishin.game.TileIndex;
import com.dzavalishin.xui.ViewPort;

public class TileMarker 
{
	private TileIndex tile;
	private int color;

	private TileMarker(TileIndex t, int color) 
	{
		this.tile = t;
		this.color = color;
		
		ViewPort.markTiles.add(this);
	}

	public TileIndex getTile() {
		return tile;
	}

	public int getColor() {
		return color;
	}
	

	public static TileMarker markFlashRed(TileIndex tile) { return new TileMarker(tile,239); }
	public static TileMarker markFlashBlue(TileIndex tile) { return new TileMarker(tile,222); }
	public static TileMarker mark(TileIndex tile, int color) { return new TileMarker(tile,color); }
	
	public static void clearAll() { ViewPort.markTiles.clear(); }
	
}
