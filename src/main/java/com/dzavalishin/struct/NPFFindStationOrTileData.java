package com.dzavalishin.struct;

import com.dzavalishin.game.TileIndex;

/* Meant to be stored in AyStar.targetdata */
public class NPFFindStationOrTileData 
{
	public TileIndex dest_coords; /* An indication of where the station is, for heuristic purposes, or the target tile */
	public int station_index; /* station index we're heading for, or -1 when we're heading for a tile */
	
	public boolean isValid() {		
		return station_index != -1 || dest_coords != null;
	}
}


