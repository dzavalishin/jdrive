package com.dzavalishin.aystar;

import com.dzavalishin.struct.OpenListNode;

/**
 * This function is called to calculate the G-value for AyStar Algorithm.
 *  return values can be:
 *	AYSTAR_INVALID_NODE : indicates an item is not valid (e.g.: unwalkable)
 *	Any value >= 0 : the g-value for this tile
 */

@FunctionalInterface
public interface AyStar_CalculateG {
	int apply(AyStar as, AyStarNode current, OpenListNode parent);
}