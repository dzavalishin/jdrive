package com.dzavalishin.game;

import com.dzavalishin.enums.StationClassID;
import com.dzavalishin.struct.DrawTileSprites;
import com.dzavalishin.tables.EngineTables;

public class StationSpec 
{
	int grfid; ///< ID of GRF file station belongs to.
	int localidx; ///< Index within GRF file of station.

	StationClassID sclass; ///< The class to which this spec belongs.

	/**
	 * Bitmask of number of platforms available for the station.
	 * 0..6 correpsond to 1..7, while bit 7 corresponds to >7 platforms.
	 */
	int allowed_platforms;
	/**
	 * Bitmask of platform lengths available for the station.
	 * 0..6 correpsond to 1..7, while bit 7 corresponds to >7 tiles long.
	 */
	int allowed_lengths;

	/** Number of tile layouts.
	 * A minimum of 8 is required is required for stations.
	 * 0-1 = plain platform
	 * 2-3 = platform with building
	 * 4-5 = platform with roof, left side
	 * 6-7 = platform with roof, right side
	 */
	int tiles;
	DrawTileSprites [] renderdata; ///< Array of tile layouts.

	byte lengths;
	byte []platforms;
	
	/* Station layout for given dimensions - it is a two-dimensional array
	 * where index is computed as (x * platforms) + platform. */
	//typedef byte *StationLayout;
	
	//StationLayout [][] layouts;
	byte [][][] layouts;

	/**
	 * NUM_GLOBAL_CID sprite groups.
	 * Used for obtaining the sprite offset of custom sprites, and for
	 * evaluating callbacks.
	 */
    final SpriteGroup [] spritegroup = new SpriteGroup[EngineTables.NUM_GLOBAL_CID];

}
