package com.dzavalishin.aystar;

import com.dzavalishin.struct.OpenListNode;

/**
 * This function is called to check if the end-tile is found
 *  return values can be:
 *	AYSTAR_FOUND_END_NODE : indicates this is the end tile
 *	AYSTAR_DONE : indicates this is not the end tile (or direction was wrong)
 *
 *
 * The 2nd parameter should be OpenListNode, and NOT AyStarNode. AyStarNode is
 * part of OpenListNode and so it could be accessed without any problems.
 * The good part about OpenListNode is, and how AIs use it, that you can
 * access the parent of the current node, and so check if you, for example
 * don't try to enter the file tile with a 90-degree curve. So please, leave
 * this an OpenListNode, it works just fine -- TrueLight
 */
@FunctionalInterface
public interface AyStar_EndNodeCheck {
	int apply(AyStar as, OpenListNode current);
}