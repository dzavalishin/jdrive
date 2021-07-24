package game;

/* Meant to be stored in AyStar.targetdata */
public class NPFFindStationOrTileData 
{
	TileIndex dest_coords; /* An indication of where the station is, for heuristic purposes, or the target tile */
	int station_index; /* station index we're heading for, or -1 when we're heading for a tile */
}


