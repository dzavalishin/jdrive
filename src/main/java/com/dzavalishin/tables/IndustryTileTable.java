package com.dzavalishin.tables;

import com.dzavalishin.struct.TileIndexDiffC;

public class IndustryTileTable {
	
	public final TileIndexDiffC ti;
	public final int map5;

	public IndustryTileTable( int x, int y, int m) {
		ti = new TileIndexDiffC(x,y);
		map5 = 0xFF & m;
	}

}
