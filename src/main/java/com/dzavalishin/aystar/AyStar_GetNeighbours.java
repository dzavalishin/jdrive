package com.dzavalishin.aystar;

import com.dzavalishin.struct.OpenListNode;

/**
 * This function request the tiles around the current tile and put them in tiles_around
 *  tiles_around is never resetted, so if you are not using directions, just leave it alone.
 *<br>
 * Warning: never add more tiles_around than memory allocated for it.
 */
@FunctionalInterface
public interface AyStar_GetNeighbours {
	void apply(AyStar as, OpenListNode current);
}