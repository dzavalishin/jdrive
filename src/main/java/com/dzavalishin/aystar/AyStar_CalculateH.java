package com.dzavalishin.aystar;

import com.dzavalishin.struct.OpenListNode;

/**
 * This function is called to calculate the H-value for AyStar Algorithm.
 *  Mostly, this must result the distance (Manhattan way) between the
 *   current point and the end point
 *  return values can be:
 *	Any value >= 0 : the h-value for this tile
 */
@FunctionalInterface
public interface AyStar_CalculateH {
	int apply(AyStar as, AyStarNode current, OpenListNode parent);
}